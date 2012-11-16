/* ===========================================================
 * TradeManager : a application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.broker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BackTestBrokerModel.OrderState;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Trade;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.strategy.StrategyChangeListener;
import org.trade.strategy.StrategyRuleException;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

import com.ib.client.ContractDetails;
import com.ib.client.Execution;

/**
 */
public class BackTestBroker extends SwingWorker<Void, Void> implements
		StrategyChangeListener {

	private final static Logger _log = LoggerFactory
			.getLogger(BackTestBroker.class);

	private PersistentModel tradePersistentModel = null;
	private StrategyData datasetContainer = null;
	private Tradestrategy tradestrategy = null;
	private Integer idTradestrategy = null;
	private BackTestBrokerModel brokerModel = null;
	private static TimeZone localTimeZone = null;
	private static final SimpleDateFormat m_sdf = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss");
	private static final SimpleDateFormat m_sdfGMT = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss z");
	private AtomicInteger ruleComplete = new AtomicInteger(0);
	private AtomicInteger strategiesRunning = new AtomicInteger(0);
	private AtomicBoolean positionCovered = new AtomicBoolean(false);
	private final Object lockBackTestWorker = new Object();
	private long execId = new Date().getTime();

	/**
	 * Constructor for BackTestBroker.
	 * 
	 * @param datasetContainer
	 *            StrategyData
	 * @param idTradestrategy
	 *            Integer
	 * @param brokerModel
	 *            BrokerModel
	 */
	public BackTestBroker(StrategyData datasetContainer,
			Integer idTradestrategy, BrokerModel brokerModel) {
		this.idTradestrategy = idTradestrategy;
		this.brokerModel = (BackTestBrokerModel) brokerModel;
		this.datasetContainer = datasetContainer;
		try {
			localTimeZone = TimeZone.getTimeZone((ConfigProperties
					.getPropAsString("trade.tws.timezone")));
			m_sdf.setTimeZone(localTimeZone);
			m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		} catch (IOException ex) {
			throw new IllegalArgumentException(
					"Error initializing BackTestBroker Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method reqContractDetails.
	 * 
	 * @param reqId
	 *            int
	 * @param ibContract
	 *            com.ib.client.Contract
	 * @throws BrokerModelException
	 */
	public void reqContractDetails(int reqId, com.ib.client.Contract ibContract)
			throws BrokerModelException {
		try {
			ContractDetails contractDetails = getYahooContractDetails(reqId,
					ibContract.m_symbol);

			this.brokerModel.contractDetails(reqId, contractDetails);
		} catch (Exception ex) {
			throw new BrokerModelException(0, 6000,
					"Error initializing BackTestBroker Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method reqHistoricalData.
	 * 
	 * @param reqId
	 *            int
	 * @param ibContract
	 *            com.ib.client.Contract
	 * @param endDateTime
	 *            String
	 * @param durationStr
	 *            String
	 * @param barSizeSetting
	 *            String
	 * @param whatToShow
	 *            String
	 * @param useRTH
	 *            int
	 * @param formatDateInteger
	 *            int
	 * @throws BrokerModelException
	 */
	public void reqHistoricalData(int reqId, com.ib.client.Contract ibContract,
			String endDateTime, String durationStr, String barSizeSetting,
			String whatToShow, int useRTH, int formatDateInteger)
			throws BrokerModelException {

		try {
			Date endDate = m_sdfGMT.parse(endDateTime);
			ChartDays chartDays = ChartDays.newInstance();
			chartDays.setDisplayName(durationStr);

			BarSize barSize = BarSize.newInstance();
			barSize.setDisplayName(barSizeSetting);

			Date startDate = TradingCalendar.addDays(endDate,
					(Integer.parseInt(chartDays.getCode()) - 1) * -1);
			startDate = TradingCalendar.getMostRecentTradingDay(startDate);

			// _log.info(" Start Date: " + startDate + " End Date: " + endDate
			// + " BarSize: " + barSize.getCode() + " ChartDays: "
			// + chartDays.getCode());

			if (BarSize.DAY == Integer.parseInt(barSize.getCode())) {
				this.getYahooPriceDataDay(reqId, ibContract.m_symbol,
						startDate, endDate);
			} else {
				this.getYahooPriceDataIntraday(reqId, ibContract.m_symbol,
						Integer.parseInt(chartDays.getCode()), startDate);
			}

			this.brokerModel.historicalData(reqId,
					"finished- at yyyyMMdd HH:mm:ss", 0, 0, 0, 0, 0, 0, 0,
					false);

		} catch (Exception ex) {
			throw new BrokerModelException(0, 6000,
					"Error initializing BackTestBroker Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method strategyComplete.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#strategyComplete(Tradestrategy)
	 */
	public synchronized void strategyComplete(Tradestrategy tradestrategy) {
		synchronized (lockBackTestWorker) {
			strategiesRunning.getAndDecrement();
			lockBackTestWorker.notifyAll();
		}
	}

	/**
	 * Method strategyStarted.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#strategyStarted(Tradestrategy)
	 */
	public synchronized void strategyStarted(Tradestrategy tradestrategy) {
		synchronized (lockBackTestWorker) {
			strategiesRunning.getAndIncrement();
			lockBackTestWorker.notifyAll();
		}
	}

	/**
	 * Method ruleComplete.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#ruleComplete(Tradestrategy)
	 */
	public synchronized void ruleComplete(Tradestrategy tradestrategy) {
		synchronized (lockBackTestWorker) {
			ruleComplete.getAndIncrement();
			lockBackTestWorker.notifyAll();
		}
	}

	/**
	 * Method positionCovered.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#positionCovered(Tradestrategy)
	 */
	public synchronized void positionCovered(Tradestrategy tradestrategy) {
		synchronized (lockBackTestWorker) {
			positionCovered.set(true);
			lockBackTestWorker.notifyAll();
		}
	}

	/**
	 * Method strategyError.
	 * 
	 * @param strategyError
	 *            StrategyRuleException
	 * @see org.trade.strategy.StrategyChangeListener#strategyError(StrategyRuleException)
	 */
	public void strategyError(StrategyRuleException strategyError) {
		this.cancel(true);
	}

	/**
	 * Method doInBackground.
	 * 
	 * @return Void
	 */
	public Void doInBackground() {

		try {

			this.tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			this.tradestrategy = this.tradePersistentModel
					.findTradestrategyById(this.idTradestrategy);
			this.datasetContainer.clearBaseCandleSeries();
			this.tradestrategy.setDatasetContainer(this.datasetContainer);

			List<Candle> candles = tradePersistentModel
					.findCandlesByContractDateRangeBarSize(this.tradestrategy
							.getContract().getIdContract(), this.tradestrategy
							.getTradingday().getOpen(), this.tradestrategy
							.getTradingday().getOpen(), this.tradestrategy
							.getBarSize());
			/*
			 * Populate any child datasets.
			 */
			populateIndicatorCandleSeries(tradestrategy, this.tradestrategy
					.getTradingday().getOpen(), this.tradestrategy
					.getTradingday().getOpen());

			Trade openTrade = null;

			/*
			 * Wait for the strategy to start.
			 */
			synchronized (lockBackTestWorker) {
				while (strategiesRunning.get() < 1) {
					lockBackTestWorker.wait();
				}
			}

			for (Candle candle : candles) {
				/*
				 * We use the direct add to BaseCandle data-set rather than
				 * going via the BrokerModel because the BrokerModel is in
				 * another thread and so this thread tends to be blocked by
				 * other activities.
				 */
				ruleComplete.set(0);
				int candleIndex = this.tradestrategy.getDatasetContainer()
						.buildCandle(candle.getStartPeriod(),
								candle.getOpen().doubleValue(),
								candle.getHigh().doubleValue(),
								candle.getLow().doubleValue(),
								candle.getClose().doubleValue(),
								candle.getVolume(),
								candle.getVwap().doubleValue(),
								candle.getTradeCount(), 1);
				/*
				 * Wait for the candle to be processed by the strategy.
				 */
				synchronized (lockBackTestWorker) {
					/*
					 * Wait for the rule to be completed by the strategy. note
					 * this worker is listening to the strategy worker.
					 */
					while ((strategiesRunning.get() > 0)
							&& (ruleComplete.get() < 1)) {
						lockBackTestWorker.wait();
					}
				}

				if (null == openTrade) {
					openTrade = this.tradePersistentModel
							.findOpenTradeByTradestrategyId(this.tradestrategy
									.getIdTradeStrategy());
				} else {
					openTrade = this.tradePersistentModel
							.findTradeById(openTrade.getIdTrade());
				}

				/*
				 * The new candle may create an order so this call fills it and
				 * return whether this is opening a position.
				 */
				if (filledOrdersOpenPosition(this.tradestrategy.getContract(),
						openTrade, candle)) {
					/*
					 * Need to recall fillOrders as this is a new open position
					 * and an OCA order may now be ready to be filled this
					 * happens when we have an engulfing bar. Only need to wait
					 * if this is a new open position. This gives time for the
					 * PositionManagerStrategy to start and create the OCA
					 * order. so we wait to see if those new orders need to be
					 * filled.
					 */
					synchronized (lockBackTestWorker) {
						/*
						 * Wait for the strategy to create the OCA order.
						 */

						while (!positionCovered.get()) {
							lockBackTestWorker.wait();
						}
					}
					/*
					 * Check to see if the strategy needs to update the OCA
					 * orders if the current bar requires the stop to be moved.
					 * This check is only done if the current candle is against
					 * the bar. i.e. we assume if we had a candle that
					 * encompassed the entry and stop and is in the direction of
					 * the trade that we weren't stopped out on the entry
					 * candle.
					 */
					openTrade = this.tradePersistentModel
							.findTradeById(openTrade.getIdTrade());
					if (candleIndex > -1) {
						CandleItem candleItem = (CandleItem) this.tradestrategy
								.getDatasetContainer().getBaseCandleSeries()
								.getDataItem(candleIndex);
						if (!candleItem.isSide(openTrade.getSide())) {
							filledOrdersOpenPosition(
									this.tradestrategy.getContract(),
									openTrade, candle);
						}
					}
					positionCovered.set(false);
					/*
					 * We now have an open position so we wait for the strategy
					 * that got us into this position to close.
					 */
					synchronized (lockBackTestWorker) {
						while (strategiesRunning.get() > 1) {
							lockBackTestWorker.wait();
						}
					}
				}
				if (strategiesRunning.get() == 0 && null == openTrade)
					break;

				if (strategiesRunning.get() == 0 && !openTrade.getIsOpen())
					break;
			}
			candles.clear();

		} catch (InterruptedException interExp) {
			// Do nothing.
		} catch (Exception ex) {
			_log.error("Error BackTestBroker Symbol: "
					+ this.tradestrategy.getContract().getSymbol() + " Msg: "
					+ ex.getMessage(), ex);
		}
		return null;
	}

	public void done() {
		brokerModel.onCancelRealtimeBars(this.tradestrategy);
		brokerModel.onCancelBrokerData(this.tradestrategy);
		// Free some memory!!
		this.tradestrategy.getDatasetContainer().clearBaseCandleSeries();
		this.tradestrategy.setDatasetContainer(null);
		_log.info("BackTestBroker done for: "
				+ tradestrategy.getContract().getSymbol());
	}

	/**
	 * Method filledOrdersOpenPosition.
	 * 
	 * @param contract
	 *            Contract
	 * @param trade
	 *            Trade
	 * @param candle
	 *            Candle
	 * @return boolean
	 * @throws Exception
	 */
	private boolean filledOrdersOpenPosition(Contract contract, Trade trade,
			Candle candle) throws Exception {

		boolean openPosition = false;
		if (null == trade) {
			return openPosition;
		}

		for (TradeOrder order : trade.getTradeOrders()) {
			if (OrderStatus.UNSUBMIT.equals(order.getStatus())) {
				/*
				 * Can't use the com.ib.client.OrderState as constructor is no
				 * visible.
				 */
				OrderState orderState = this.brokerModel.new OrderState();
				orderState.m_status = OrderStatus.SUBMITTED;
				this.brokerModel.openOrder(order.getOrderKey(),
						TWSBrokerModel.getIBContract(contract),
						TWSBrokerModel.getIBOrder(order), orderState);
				/*
				 * TODO we should read the orders back after any call to the
				 * broker interface.
				 */
				order.setStatus(OrderStatus.SUBMITTED);
			}
		}
		for (TradeOrder order : trade.getTradeOrders()) {
			if (OrderStatus.SUBMITTED.equals(order.getStatus())
					&& order.getTransmit()) {

				BigDecimal filledPrice = getFilledPrice(order, candle);
				if (null != filledPrice) {
					// If OCA cancel other side
					if (null == order.getOcaGroupName()) {
						createOrderExecution(contract, order, filledPrice,
								candle.getStartPeriod());
					} else {

						for (TradeOrder orderOCA : trade.getTradeOrders()) {

							if (order.getOcaGroupName().equals(
									orderOCA.getOcaGroupName())
									&& !order.getOrderKey().equals(
											orderOCA.getOrderKey())
									&& !orderOCA.getIsFilled()) {
								BigDecimal orderOCAFilledPrice = getFilledPrice(
										orderOCA, candle);

								if (null != orderOCAFilledPrice) {
									/*
									 * Other side of order could have been
									 * filled on this bar also. So assume the if
									 * a green bar we went open/low/high/close.
									 */
									if (candle.getClose().compareTo(
											candle.getOpen()) > 0) {
										// Green bar
										if (filledPrice
												.compareTo(orderOCAFilledPrice) > 0) {
											cancelOrder(contract, order);
											createOrderExecution(contract,
													orderOCA,
													orderOCAFilledPrice,
													candle.getStartPeriod());
											break;
										}
									} else {
										if (filledPrice
												.compareTo(orderOCAFilledPrice) < 0) {
											cancelOrder(contract, order);
											createOrderExecution(contract,
													orderOCA,
													orderOCAFilledPrice,
													candle.getStartPeriod());
											break;
										}
									}
								}
								cancelOrder(contract, orderOCA);
								createOrderExecution(contract, order,
										filledPrice, candle.getStartPeriod());
								break;
							}
						}
					}
					if (order.getIsOpenPosition()) {
						openPosition = true;
					}
				}
			}
		}

		return openPosition;
	}

	/**
	 * Method getFilledPrice.
	 * 
	 * @param order
	 *            TradeOrder
	 * @param candle
	 *            Candle
	 * @return BigDecimal
	 */
	private BigDecimal getFilledPrice(TradeOrder order, Candle candle) {

		BigDecimal filledPrice = null;
		boolean filled = false;

		if (OrderType.MKT.equals(order.getOrderType())) {
			filled = true;
			filledPrice = candle.getOpen();
		} else {

			if (OrderType.STP.equals(order.getOrderType())
					|| OrderType.STPLMT.equals(order.getOrderType())) {
				filledPrice = order.getAuxPrice();
			} else {
				filledPrice = order.getLimitPrice();
			}
			if (order.getIsOpenPosition()) {
				if ((filledPrice.compareTo(candle.getLow()) > -1)
						&& (filledPrice.compareTo(candle.getHigh()) < 1)) {
					filled = true;
				}

			} else {
				if (Action.SELL.equals(order.getAction())) {
					if (OrderType.STP.equals(order.getOrderType())) {
						if (candle.getLow().compareTo(filledPrice) < 1) {
							if (candle.getOpen().compareTo(filledPrice) < 1) {
								filledPrice = candle.getOpen();
							}
							filled = true;
						}
					} else if (OrderType.LMT.equals(order.getOrderType())) {
						if (candle.getHigh().compareTo(filledPrice) > -1) {
							filled = true;
						}
					}

				} else {
					if (OrderType.STP.equals(order.getOrderType())) {
						if (candle.getHigh().compareTo(filledPrice) > -1) {
							if (candle.getOpen().compareTo(filledPrice) > -1) {
								filledPrice = candle.getOpen();
							}
							filled = true;
						}
					} else if (OrderType.LMT.equals(order.getOrderType())) {
						if (candle.getLow().compareTo(filledPrice) < 1) {
							filled = true;
						}
					}
				}
			}
		}
		if (filled) {
			return filledPrice;
		} else {
			return null;
		}
	}

	/**
	 * Method createOrderExecution.
	 * 
	 * @param contract
	 *            Contract
	 * @param order
	 *            TradeOrder
	 * @param filledPrice
	 *            BigDecimal
	 * @param date
	 *            Date
	 * @throws IOException
	 */
	private void createOrderExecution(Contract contract, TradeOrder order,
			BigDecimal filledPrice, Date date) throws IOException {

		Execution execution = new Execution();
		execution.m_avgPrice = filledPrice.doubleValue();
		execution.m_cumQty = order.getQuantity().intValue();
		execution.m_clientId = order.getClientId().intValue();
		execution.m_exchange = "BATS";
		execution.m_price = filledPrice.doubleValue();
		execution.m_orderId = order.getOrderKey();
		execution.m_time = m_sdf.format(date);
		if (Action.BUY.equals(order.getAction())) {
			execution.m_side = Side.BOT;
		} else {
			execution.m_side = Side.SLD;
		}
		execution.m_shares = order.getQuantity().intValue();
		execution.m_execId = String.valueOf(execId++);
		this.brokerModel.execDetails(execution.m_orderId,
				TWSBrokerModel.getIBContract(contract), execution);
		OrderState orderState = this.brokerModel.new OrderState();
		orderState.m_status = OrderStatus.FILLED;
		orderState.m_commission = execution.m_shares * 0.005d;
		if (orderState.m_commission < 1) {
			orderState.m_commission = 1;
		}
		this.brokerModel.openOrder(order.getOrderKey(),
				TWSBrokerModel.getIBContract(contract),
				TWSBrokerModel.getIBOrder(order), orderState);
	}

	/**
	 * Method cancelOrder.
	 * 
	 * @param contract
	 *            Contract
	 * @param order
	 *            TradeOrder
	 * @throws IOException
	 */
	private void cancelOrder(Contract contract, TradeOrder order)
			throws IOException {
		OrderState orderState = this.brokerModel.new OrderState();
		orderState.m_status = OrderStatus.CANCELLED;
		this.brokerModel.openOrder(order.getOrderKey(),
				TWSBrokerModel.getIBContract(contract),
				TWSBrokerModel.getIBOrder(order), orderState);
		order.setStatus(OrderStatus.CANCELLED);
	}

	/*
	 * For any child indicators that are candle based create a Tradestrategy
	 * that will get the data. If this tradestrategy already exist share this
	 * with any other tradestrategy that requires this.
	 */
	/**
	 * Method populateIndicatorCandleSeries.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @throws PersistentModelException
	 */
	private void populateIndicatorCandleSeries(Tradestrategy tradestrategy,
			Date startDate, Date endDate) throws PersistentModelException {

		CandleDataset candleDataset = (CandleDataset) tradestrategy
				.getDatasetContainer().getIndicators(
						IndicatorSeries.CandleSeries);
		if (null != candleDataset) {
			for (int seriesIndex = 0; seriesIndex < candleDataset
					.getSeriesCount(); seriesIndex++) {

				CandleSeries series = candleDataset.getSeries(seriesIndex);

				Contract contract = this.tradePersistentModel
						.findContractByUniqueKey(series.getSecType(),
								series.getSymbol(), series.getExchange(),
								series.getCurrency());
				Tradestrategy childTradestrategy = new Tradestrategy(contract,
						tradestrategy.getTradingday(), new Strategy(),
						tradestrategy.getTradeAccount(), new BigDecimal(0),
						null, null, false, tradestrategy.getChartDays(),
						tradestrategy.getBarSize());
				childTradestrategy.setDirty(false);

				List<Candle> indicatorCandles = this.tradePersistentModel
						.findCandlesByContractDateRangeBarSize(
								childTradestrategy.getContract()
										.getIdContract(), startDate, endDate,
								childTradestrategy.getBarSize());
				if (indicatorCandles.isEmpty()) {
					_log.info("No chart data available for "
							+ childTradestrategy.getContract().getSymbol());
				} else {
					CandleDataset.populateSeries(
							childTradestrategy.getDatasetContainer(),
							indicatorCandles);
					indicatorCandles.clear();

					CandleSeries childSeries = childTradestrategy
							.getDatasetContainer().getBaseCandleSeries();
					childSeries.setDisplaySeries(series.getDisplaySeries());
					childSeries.setSeriesRGBColor(series.getSeriesRGBColor());
					childSeries.setSymbol(series.getSymbol());
					childSeries.setSecType(series.getSecType());
					childSeries.setCurrency(series.getCurrency());
					childSeries.setExchange(series.getExchange());
					candleDataset.setSeries(seriesIndex, childSeries);
				}
			}
		}
	}

	/**
	 * Method getYahooContractDetails.
	 * 
	 * @param reqId
	 *            int
	 * @param symbol
	 *            String
	 * @return ContractDetails
	 * @throws IOException
	 */
	private ContractDetails getYahooContractDetails(int reqId, String symbol)
			throws IOException {

		/*
		 * Yahoo finance http://finance.yahoo.com/d/quotes.csv?s=XOM&f=n
		 */
		ContractDetails contractDetails = new ContractDetails();

		String strUrl = "http://finance.yahoo.com/d/quotes.csv?s=" + symbol
				+ "&f=n";

		// _log.info("URL : " + strUrl);
		URL url = new URL(strUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			StringTokenizer scanLine = new StringTokenizer(inputLine, ",");
			while (scanLine.hasMoreTokens()) {
				contractDetails.m_longName = scanLine.nextToken().replaceAll(
						"\"", "");
			}
		}
		in.close();
		return contractDetails;
	}

	/**
	 * Method getYahooPriceDataIntraday.
	 * 
	 * @param reqId
	 *            int
	 * @param symbol
	 *            String
	 * @param chartDays
	 *            int
	 * @param startDate
	 *            Date
	 * @throws IOException
	 */
	private void getYahooPriceDataIntraday(int reqId, String symbol,
			int chartDays, Date startDate) throws IOException {

		/*
		 * Yahoo finance http://chartapi.finance.yahoo.com/instrument/1.0/IBM
		 * /chartdata;type=quote;range=1d/csv/
		 */

		String strUrl = "http://chartapi.finance.yahoo.com/instrument/1.0/"
				+ symbol + "/chartdata;type=quote;range=" + chartDays
				+ "d/csv/";

		// _log.info("URL : " + strUrl);
		URL url = new URL(strUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		in.readLine();
		while ((inputLine = in.readLine()) != null) {

			if (inputLine.indexOf(":") == -1) {
				StringTokenizer scanLine = new StringTokenizer(inputLine, ",");
				while (scanLine.hasMoreTokens()) {
					String dateString = scanLine.nextToken();
					Date time = new Date(Long.parseLong(dateString) * 1000);
					// values:Timestamp,close,high,low,open,volume
					double close = Double.parseDouble(scanLine.nextToken());
					double high = Double.parseDouble(scanLine.nextToken());
					double low = Double.parseDouble(scanLine.nextToken());
					double open = Double.parseDouble(scanLine.nextToken());
					long volume = Long.parseLong(scanLine.nextToken());
					// _log.info("Time : " + time + " Open: " + open + " High: "
					// + high + " Low: " + low + " Close: " + close
					// + " Volume: " + volume);

					if (startDate.before(time)) {
						this.brokerModel
								.historicalData(reqId, dateString, open, high,
										low, close, ((int) volume / 100),
										((int) volume / 100),
										(open + close) / 2, false);
					}
				}
			}
		}
		in.close();
	}

	// http://finance.yahoo.com/d/quotes.csv?s=XOM+BBDb.TO+JNJ+MSFT&f=n

	/**
	 * Method getYahooPriceDataDay.
	 * 
	 * @param reqId
	 *            int
	 * @param symbol
	 *            String
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @throws IOException
	 * @throws ParseException
	 */
	private void getYahooPriceDataDay(int reqId, String symbol, Date startDate,
			Date endDate) throws IOException, ParseException {

		/*
		 * Yahoo finance So IBM form 1/1/2012 thru 06/30/2012
		 * http://ichart.finance .yahoo.com/table.csv?s=IBM&a=0&b=1&c=2012&d=5
		 * &e=30&f=2012&ignore=.csv"
		 */
		DateFormat df = new SimpleDateFormat("y-M-d");
		List<Candle> candles = new ArrayList<Candle>();

		String strUrl = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol
				+ "&a=" + TradingCalendar.getMonth(startDate) + "&b="
				+ TradingCalendar.getDayOfMonth(startDate) + "&c="
				+ TradingCalendar.getYear(startDate) + "&d="
				+ TradingCalendar.getMonth(endDate) + "&e="
				+ TradingCalendar.getDayOfMonth(endDate) + "&f="
				+ TradingCalendar.getYear(endDate) + "&ignore=.csv";

		// _log.info(strUrl);
		URL url = new URL(strUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		in.readLine();
		while ((inputLine = in.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(inputLine, ",");
			Date time = TradingCalendar.getBusinessDayStart(df.parse(st
					.nextToken()));
			double open = Double.parseDouble(st.nextToken());
			double high = Double.parseDouble(st.nextToken());
			double low = Double.parseDouble(st.nextToken());
			double close = Double.parseDouble(st.nextToken());
			long volume = Long.parseLong(st.nextToken());
			// double adjClose = Double.parseDouble( st.nextToken() );
			// _log.info("Time : " + time + " Open: " + open + " High: "
			// + high + " Low: " + low + " Close: " + close
			// + " Volume: " + volume);
			Candle candle = new Candle(null, open, high, low, close,
					(volume / 100), (open + close) / 2, ((int) volume / 100),
					new Date());
			candle.setStartPeriod(TradingCalendar.getBusinessDayStart(time));
			candle.setPeriod(TradingCalendar.getBusinessDayStart(time)
					.toString());
			candle.setEndPeriod(TradingCalendar.addSeconds(
					TradingCalendar.getBusinessDayEnd(time), -1));
			candle.setLastUpdateDate(candle.getStartPeriod());
			candles.add(candle);

		}
		in.close();
		Collections.reverse(candles);
		for (Candle candle : candles) {
			this.brokerModel.historicalData(reqId, String.valueOf(candle
					.getStartPeriod().getTime() / 1000), candle.getOpen()
					.doubleValue(), candle.getHigh().doubleValue(), candle
					.getLow().doubleValue(), candle.getClose().doubleValue(),
					candle.getVolume().intValue(), candle.getTradeCount(),
					candle.getVwap().doubleValue(), false);
		}
	}
}
