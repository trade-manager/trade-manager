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
package org.trade.broker.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.dictionary.valuetype.Action;
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
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.strategy.StrategyChangeListener;
import org.trade.strategy.StrategyRuleException;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

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
	private ClientWrapper brokerModel = null;
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
			Integer idTradestrategy, ClientWrapper brokerModel) {
		this.idTradestrategy = idTradestrategy;
		this.brokerModel = brokerModel;
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
	 * Method strategyComplete.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#strategyComplete(Tradestrategy)
	 */
	public synchronized void strategyComplete(String key,
			Tradestrategy tradestrategy) {
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
				this.tradestrategy.getDatasetContainer().buildCandle(
						candle.getStartPeriod(),
						candle.getOpen().doubleValue(),
						candle.getHigh().doubleValue(),
						candle.getLow().doubleValue(),
						candle.getClose().doubleValue(), candle.getVolume(),
						candle.getVwap().doubleValue(), candle.getTradeCount(),
						1);
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
					if (!this.tradestrategy.getDatasetContainer()
							.getBaseCandleSeries().isEmpty()) {
						CandleItem candleItem = (CandleItem) this.tradestrategy
								.getDatasetContainer()
								.getBaseCandleSeries()
								.getDataItem(
										this.tradestrategy
												.getDatasetContainer()
												.getBaseCandleSeries()
												.getItemCount() - 1);
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
		this.tradestrategy.setDatasetContainer(null);
		_log.info("BackTestBroker done for: "
				+ tradestrategy.getContract().getSymbol()
				+ " idTradestrategy: "
				+ this.tradestrategy.getIdTradeStrategy());
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
				OrderState orderState = new OrderState();
				orderState.m_status = OrderStatus.SUBMITTED;
				this.brokerModel.openOrder(order.getOrderKey(), contract,
						order, orderState);
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

		TradeOrderfill execution = new TradeOrderfill();
		execution.setTradeOrder(order);
		execution.setAveragePrice(filledPrice);
		execution.setCumulativeQuantity(order.getQuantity());
		execution.setExchange("BATS");
		execution.setPrice(filledPrice);
		execution.setTime(date);
		if (Action.BUY.equals(order.getAction())) {
			execution.setSide(Side.BOT);
		} else {
			execution.setSide(Side.SLD);
		}
		execution.setQuantity(order.getQuantity());
		execution.setExecId(String.valueOf(execId++));
		this.brokerModel.execDetails(execution.getTradeOrder().getOrderKey(),
				contract, execution);
		OrderState orderState = new OrderState();
		orderState.m_status = OrderStatus.FILLED;
		orderState.m_commission = execution.getQuantity() * 0.005d;
		if (orderState.m_commission < 1) {
			orderState.m_commission = 1;
		}
		this.brokerModel.openOrder(order.getOrderKey(), contract, order,
				orderState);
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
		OrderState orderState = new OrderState();
		orderState.m_status = OrderStatus.CANCELLED;
		this.brokerModel.openOrder(order.getOrderKey(), contract, order,
				orderState);
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
				.getDatasetContainer().getIndicatorByType(
						IndicatorSeries.CandleSeries);
		if (null != candleDataset) {
			for (int seriesIndex = 0; seriesIndex < candleDataset
					.getSeriesCount(); seriesIndex++) {

				CandleSeries series = candleDataset.getSeries(seriesIndex);

				Contract contract = this.tradePersistentModel
						.findContractByUniqueKey(series.getSecType(),
								series.getSymbol(), series.getExchange(),
								series.getCurrency(), null);
				if (null == contract)
					continue;

				Tradestrategy childTradestrategy = new Tradestrategy(contract,
						tradestrategy.getTradingday(), new Strategy(),
						tradestrategy.getPortfolio(), new BigDecimal(0), null,
						null, false, tradestrategy.getChartDays(),
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
}
