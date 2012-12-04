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

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.client.BackTestBroker;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.SECType;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.TradeAccount;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

import com.ib.client.CommissionReport;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.OrderState;
import com.ib.client.TickType;
import com.ib.client.UnderComp;

/**
 */
public class TWSBrokerModel extends AbstractBrokerModel implements EWrapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 595280836716405557L;

	private final static Logger _log = LoggerFactory
			.getLogger(TWSBrokerModel.class);

	// Candle series this is listened to by the chart panel
	// and main controller for updates.
	private static final ConcurrentHashMap<Integer, Contract> m_historyDataRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_realTimeBarsRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_contractRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<String, TradeAccount> m_accountRequests = new ConcurrentHashMap<String, TradeAccount>();

	private static final ConcurrentHashMap<Integer, TradeOrder> openOrders = new ConcurrentHashMap<Integer, TradeOrder>();
	private static final ConcurrentHashMap<Integer, TradeOrder> execDetails = new ConcurrentHashMap<Integer, TradeOrder>();;

	private EClientSocket m_client = null;
	private PersistentModel m_tradePersistentModel = null;
	private AtomicInteger reqId = null;
	private AtomicInteger orderKey = null;
	private Integer m_clientId = null;

	// TWS socket values see config.properties
	// Determines the date format applied to returned bars. Valid values
	// include:
	// 1 - dates applying to bars returned in the format:
	// yyyymmdd{space}{space}hh:mm:dd
	// 2 - dates are returned as a long integer specifying the number of seconds
	// since 1/1/1970 GMT.
	private Integer backfillDateFormat = 2;
	private Integer backfillUseRTH = 1;
	private String backfillWhatToShow;
	private Integer backfillOffsetDays = 0;
	// private final static String ALL_GENERIC_TICK_TAGS = "221,233";
	private final static String ALL_GENERIC_TICK_TAGS = "233";
	private static final String AVAILABLE_FUNDS = "AvailableFunds";
	private static final String BUYING_POWER = "BuyingPower";
	private static final String CASH_BALANCE = "CashBalance";
	private static final String CURRENCY = "Currency";
	private static final String GROSS_POSITION_VALUE = "GrossPositionValue";
	private static final String REALIZED_P_L = "RealizedPnL";
	private static final String UNREALIZED_P_L = "UnrealizedPnL";
	private static final String STOCK_MKT_VALUE = "StockMarketValue";

	private static final SimpleDateFormat m_sdfGMT = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss z");
	private static final SimpleDateFormat m_sdfExpiry = new SimpleDateFormat(
			"yyyyMMdd");

	public TWSBrokerModel() {
		try {
			m_client = new EClientSocket(this);
			m_tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			backfillUseRTH = ConfigProperties
					.getPropAsInt("trade.backfill.useRTH");
			backfillWhatToShow = ConfigProperties
					.getPropAsString("trade.backfill.whatToShow");
			backfillOffsetDays = ConfigProperties
					.getPropAsInt("trade.backfill.offsetDays");
			Date date = new Date();
			reqId = new AtomicInteger((int) (date.getTime() / 1000d));

		} catch (Exception ex) {
			throw new IllegalArgumentException(
					"Error initializing BrokerModel Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method isConnected.
	 * 
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isConnected()
	 */
	public boolean isConnected() {
		return m_client.isConnected();
	}

	/**
	 * Method getHistoricalData.
	 * 
	 * @return ConcurrentHashMap<Integer,Contract>
	 * @see org.trade.broker.BrokerModel#getHistoricalData()
	 */
	public ConcurrentHashMap<Integer, Contract> getHistoricalData() {
		return m_historyDataRequests;
	}

	/**
	 * Method onConnect.
	 * 
	 * @param host
	 *            String
	 * @param port
	 *            Integer
	 * @param clientId
	 *            Integer
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onConnect(String, Integer, Integer)
	 */
	public void onConnect(String host, Integer port, Integer clientId)
			throws BrokerModelException {
		this.m_clientId = clientId;
		m_client.eConnect(host, port, clientId);
		openOrders.clear();

	}

	/**
	 * Method disconnect.
	 * 
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#disconnect()
	 */
	public void disconnect() throws BrokerModelException {
		onCancelAllRealtimeData();
		if (m_client.isConnected()) {
			for (String accountNumber : m_accountRequests.keySet()) {
				this.onCancelAccountUpdates(accountNumber);
			}
			m_client.eDisconnect();
		}
		this.fireConnectionClosed();
	}

	/**
	 * Method connectionClosed.
	 * 
	 * @see com.ib.client.AnyWrapper#connectionClosed()
	 */
	public void connectionClosed() {

		onCancelAllRealtimeData();
		this.fireConnectionClosed();
		error(0, 1101, "Error Connection was closed! ");
	}

	/**
	 * Method getBackTestBroker.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * @see org.trade.broker.BrokerModel#getBackTestBroker(Integer)
	 */
	public BackTestBroker getBackTestBroker(Integer idTradestrategy) {
		return null;
	}

	/**
	 * Method onReqManagedAccount.
	 * 
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqManagedAccount()
	 */
	public void onReqManagedAccount() throws BrokerModelException {
		// request list of all open orders
		if (m_client.isConnected()) {
			m_client.reqManagedAccts();
		} else {
			throw new BrokerModelException(0, 3010,
					"Not conected to TWS historical data cannot be retrieved");
		}
	}

	/**
	 * Method onSubscribeAccountUpdates.
	 * 
	 * @param subscribe
	 *            boolean
	 * @param tradeAccount
	 *            TradeAccount
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onSubscribeAccountUpdates(boolean,
	 *      TradeAccount)
	 */
	public void onSubscribeAccountUpdates(boolean subscribe,
			TradeAccount tradeAccount) throws BrokerModelException {
		try {

			m_accountRequests
					.put(tradeAccount.getAccountNumber(), tradeAccount);
			if (m_client.isConnected()) {
				m_client.reqAccountUpdates(subscribe,
						tradeAccount.getAccountNumber());
			} else {
				throw new BrokerModelException(0, 3010,
						"Not conected to TWS historical account data cannot be retrieved");
			}

		} catch (Exception ex) {
			error(0,
					3290,
					"Error requesting Trade Account: "
							+ tradeAccount.getAccountNumber() + " Msg: "
							+ ex.getMessage());
		}

	}

	/**
	 * Method onReqAllOpenOrders.
	 * 
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqAllOpenOrders()
	 */
	public void onReqAllOpenOrders() throws BrokerModelException {
		// request list of all open orders
		if (m_client.isConnected()) {
			openOrders.clear();
			m_client.reqAllOpenOrders();
		} else {
			throw new BrokerModelException(0, 3010,
					"Not conected to TWS historical data cannot be retrieved");
		}
	}

	/**
	 * Method onReqOpenOrders.
	 * 
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqOpenOrders()
	 */
	public void onReqOpenOrders() throws BrokerModelException {
		// request list of all open orders
		if (m_client.isConnected()) {
			openOrders.clear();
			m_client.reqOpenOrders();
		} else {
			throw new BrokerModelException(0, 3010,
					"Not conected to TWS historical data cannot be retrieved");
		}
	}

	/**
	 * Method onReqAllExecutions.
	 * 
	 * @param mktOpenDate
	 *            Date
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqAllExecutions(Date)
	 */
	public void onReqAllExecutions(Date mktOpenDate)
			throws BrokerModelException {

		/*
		 * Request execution reports based on the supplied filter criteria
		 */

		if (m_client.isConnected()) {
			execDetails.clear();
			Integer reqId = this.getNextRequestId();
			m_client.reqExecutions(reqId, TWSBrokerModel.getIBExecutionFilter(
					m_clientId, mktOpenDate, null, null));
		} else {
			throw new BrokerModelException(0, 3020,
					"Not conected to TWS historical data cannot be retrieved");
		}
	}

	/**
	 * Method onReqExecutions.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqExecutions(Tradestrategy)
	 */
	public void onReqExecutions(Tradestrategy tradestrategy)
			throws BrokerModelException {

		/*
		 * Request execution reports based on the supplied filter criteria
		 */
		if (m_client.isConnected()) {
			execDetails.clear();
			Integer reqId = this.getNextRequestId();
			m_client.reqExecutions(reqId, TWSBrokerModel.getIBExecutionFilter(
					m_clientId, tradestrategy.getTradingday().getOpen(),
					tradestrategy.getContract().getSecType(), tradestrategy
							.getContract().getSymbol()));
		} else {
			throw new BrokerModelException(tradestrategy.getIdTradeStrategy(),
					3020,
					"Not conected to TWS historical data cannot be retrieved");
		}
	}

	/**
	 * Method onReqRealTimeBars.
	 * 
	 * @param contract
	 *            Contract
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqRealTimeBars(Contract)
	 */
	public void onReqRealTimeBars(Contract contract, boolean mktData)
			throws BrokerModelException {
		try {
			if (m_client.isConnected()) {

				if (this.isRealtimeBarsRunning(contract)) {
					throw new BrokerModelException(contract.getIdContract(),
							3030, "Data request is already in progress for: "
									+ contract.getSymbol()
									+ " Please wait or cancel.");
				}
				if (!m_realTimeBarsRequests.containsKey(contract
						.getIdContract())) {
					m_realTimeBarsRequests.put(contract.getIdContract(),
							contract);

					/*
					 * Bar interval is set to 5= 5sec this is the only thing
					 * supported by TWS for live data.
					 */
					m_client.reqRealTimeBars(contract.getIdContract(),
							TWSBrokerModel.getIBContract(contract), 5,
							backfillWhatToShow, (backfillUseRTH > 0));

					if (mktData) {
						m_client.reqMktData(contract.getIdContract(),
								TWSBrokerModel.getIBContract(contract),
								ALL_GENERIC_TICK_TAGS, false);
					}
				}
			} else {
				throw new BrokerModelException(contract.getIdContract(), 3040,
						"Not conected to TWS historical data cannot be retrieved");
			}
		} catch (Exception ex) {
			throw new BrokerModelException(contract.getIdContract(), 3050,
					"Error broker data Symbol: " + contract.getSymbol()
							+ " Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method onContractDetails.
	 * 
	 * @param contract
	 *            Contract
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onContractDetails(Contract)
	 */
	public void onContractDetails(Contract contract)
			throws BrokerModelException {
		try {
			if (m_client.isConnected()) {
				Integer reqId = contract.getIdContract();
				if (null == reqId)
					reqId = this.getNextRequestId();

				if (!m_contractRequests.containsKey(reqId)) {
					/*
					 * Null the IB Contract Id as these sometimes change. This
					 * will force a get of the IB data via the
					 * Exchange/Symbol/Currency.
					 */
					contract.setIdContractIB(null);
					m_contractRequests.put(reqId, contract);
					TWSBrokerModel.logContract(TWSBrokerModel
							.getIBContract(contract));
					m_client.reqContractDetails(reqId,
							TWSBrokerModel.getIBContract(contract));
				}
			} else {
				throw new BrokerModelException(contract.getIdContract(), 3080,
						"Not conected to TWS historical data cannot be retrieved");
			}
		} catch (Exception ex) {
			throw new BrokerModelException(contract.getIdContract(), 3090,
					"Error broker data Symbol: " + contract.getSymbol()
							+ " Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method onBrokerData.
	 * 
	 * @param contract
	 *            Contract
	 * @param Date
	 *            startDate
	 * @param Date
	 *            endDate
	 * @param Integer
	 *            barSize
	 * @param Integer
	 *            chartDays
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onBrokerData(Contract , String , String
	 *      )
	 */
	public void onBrokerData(Contract contract, Date endDate, Integer barSize,
			Integer chartDays) throws BrokerModelException {

		try {

			if (m_client.isConnected()) {

				if (this.isHistoricalDataRunning(contract)) {
					throw new BrokerModelException(contract.getIdContract(),
							3010, "Data request is already in progress for: "
									+ contract.getSymbol()
									+ " Please wait or cancel.");
				}

				/*
				 * When running data via the TWS API we start the
				 * DatasetContainers internal thread to process candle updates
				 * and all indicator updates. That reduces the delay to the
				 * broker interface thread for messages coming in.
				 */
				for (Tradestrategy tradestrategy : contract
						.getTradestrategies()) {
					if (!tradestrategy.getDatasetContainer().isRunning())
						tradestrategy.getDatasetContainer().execute();
				}
				m_historyDataRequests.put(contract.getIdContract(), contract);

				/*
				 * Get the contract details.
				 */
				this.onContractDetails(contract);

				endDate = TradingCalendar.getSpecificTime(endDate,
						TradingCalendar.getMostRecentTradingDay(TradingCalendar
								.addBusinessDays(endDate, backfillOffsetDays)));
				m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
				String endDateTime = m_sdfGMT.format(endDate);

				_log.info("onBrokerData Symbol: " + contract.getSymbol()
						+ " end Time: " + endDateTime + " Period length: "
						+ chartDays + " Bar size: " + barSize + " WhatToShow: "
						+ backfillWhatToShow + " Regular Trading Hrs: "
						+ backfillUseRTH + " Date format: "
						+ backfillDateFormat);

				m_client.reqHistoricalData(contract.getIdContract(),
						TWSBrokerModel.getIBContract(contract), endDateTime,
						ChartDays.newInstance(chartDays).getDisplayName(),
						BarSize.newInstance(barSize).getDisplayName(),
						backfillWhatToShow, backfillUseRTH, backfillDateFormat);

			} else {
				throw new BrokerModelException(contract.getIdContract(), 3100,
						"Not conected to TWS historical data cannot be retrieved");
			}
		} catch (Exception ex) {
			throw new BrokerModelException(contract.getIdContract(), 3110,
					"Error broker data Symbol: " + contract.getSymbol()
							+ " Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method isAccountUpdatesRunning.
	 * 
	 * @param accountNumber
	 *            String
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isAccountUpdatesRunning(String)
	 */
	public boolean isAccountUpdatesRunning(String accountNumber) {
		synchronized (m_accountRequests) {
			if (m_accountRequests.containsKey(accountNumber)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method isHistoricalDataRunning.
	 * 
	 * @param contract
	 *            Contract
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isHistoricalDataRunning(Contract)
	 */
	public boolean isHistoricalDataRunning(Contract contract) {
		synchronized (m_historyDataRequests) {
			if (m_historyDataRequests.containsKey(contract.getIdContract())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method isHistoricalDataRunning.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return boolean
	 */
	public boolean isHistoricalDataRunning(Tradestrategy tradestrategy) {
		synchronized (m_historyDataRequests) {
			if (m_historyDataRequests.containsKey(tradestrategy.getContract()
					.getIdContract())) {
				Contract contract = m_historyDataRequests.get(tradestrategy
						.getContract().getIdContract());
				for (Tradestrategy item : contract.getTradestrategies()) {
					if (item.equals(tradestrategy)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Method isRealtimeBarsRunning.
	 * 
	 * @param contract
	 *            Contract
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isRealtimeBarsRunning(Contract)
	 */
	public boolean isRealtimeBarsRunning(Contract contract) {
		if (m_client.isConnected()) {
			if (m_realTimeBarsRequests.containsKey(contract.getIdContract())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method isRealtimeBarsRunning.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return boolean
	 */
	public boolean isRealtimeBarsRunning(Tradestrategy tradestrategy) {
		synchronized (m_realTimeBarsRequests) {
			if (m_realTimeBarsRequests.containsKey(tradestrategy.getContract()
					.getIdContract())) {
				Contract contract = m_realTimeBarsRequests.get(tradestrategy
						.getContract().getIdContract());
				for (Tradestrategy item : contract.getTradestrategies()) {
					if (item.equals(tradestrategy)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Method onCancelAllRealtimeData.
	 * 
	 * @see org.trade.broker.BrokerModel#onCancelAllRealtimeData()
	 */
	public void onCancelAllRealtimeData() {

		if (m_client.isConnected()) {
			for (Integer reqId : m_historyDataRequests.keySet()) {
				m_client.cancelHistoricalData(reqId);
			}
			for (Integer reqId : m_realTimeBarsRequests.keySet()) {
				m_client.cancelRealTimeBars(reqId);
			}
		}
		m_contractRequests.clear();
		m_historyDataRequests.clear();
		m_realTimeBarsRequests.clear();

	}

	/**
	 * Method onCancelAccountUpdates.
	 * 
	 * @param accountNumber
	 *            String
	 * @see org.trade.broker.BrokerModel#onCancelAccountUpdates(String)
	 */
	public void onCancelAccountUpdates(String accountNumber) {
		synchronized (m_accountRequests) {
			if (m_accountRequests.containsKey(accountNumber)) {
				if (m_client.isConnected()) {
					m_client.reqAccountUpdates(false, accountNumber);
				}
				m_accountRequests.remove(accountNumber);
			}
		}
	}

	/**
	 * Method onCancelContractDetails.
	 * 
	 * @param contract
	 *            Contract
	 * @see org.trade.broker.BrokerModel#onCancelContractDetails(Contract)
	 */
	public void onCancelContractDetails(Contract contract) {
		if (m_client.isConnected()) {
			for (Integer reqId : m_contractRequests.keySet()) {
				Contract value = m_contractRequests.get(reqId);
				if (contract.equals(value)) {
					m_contractRequests.remove(reqId);
					break;
				}
			}
		}
	}

	/**
	 * Method onCancelBrokerData.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void onCancelBrokerData(Tradestrategy tradestrategy) {
		synchronized (m_historyDataRequests) {
			if (m_historyDataRequests.containsKey(tradestrategy.getContract()
					.getIdContract())) {
				Contract contract = m_historyDataRequests.get(tradestrategy
						.getContract().getIdContract());
				for (Tradestrategy item : contract.getTradestrategies()) {
					if (item.equals(tradestrategy)) {
						contract.removeTradestrategy(tradestrategy);
						break;
					}
				}
				if (contract.getTradestrategies().isEmpty()) {
					if (m_client.isConnected()) {
						m_client.cancelHistoricalData(contract.getIdContract());
					}
					m_historyDataRequests.remove(contract.getIdContract());
					m_historyDataRequests.notifyAll();
				}
			}
		}
	}

	/**
	 * Method onCancelRealtimeBars.
	 * 
	 * @param contract
	 *            Contract
	 * @see org.trade.broker.BrokerModel#onCancelRealtimeBars(Contract)
	 */
	public void onCancelRealtimeBars(Contract contract) {
		synchronized (m_realTimeBarsRequests) {
			if (m_realTimeBarsRequests.containsKey(contract.getIdContract())) {
				boolean mktData = false;
				for (Tradestrategy item : contract.getTradestrategies()) {
					if (item.getStrategy().getMarketData()) {
						mktData = true;
						break;
					}
				}
				if (m_client.isConnected()) {
					m_client.cancelRealTimeBars(contract.getIdContract());
					if (mktData)
						m_client.cancelMktData(contract.getIdContract());
				}
				m_realTimeBarsRequests.remove(contract.getIdContract());
				m_realTimeBarsRequests.notifyAll();
			}
		}
	}

	/**
	 * Method onCancelRealtimeBars.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void onCancelRealtimeBars(Tradestrategy tradestrategy) {
		synchronized (m_realTimeBarsRequests) {
			if (m_realTimeBarsRequests.containsKey(tradestrategy.getContract()
					.getIdContract())) {
				Contract contract = m_realTimeBarsRequests.get(tradestrategy
						.getContract().getIdContract());
				for (Tradestrategy item : contract.getTradestrategies()) {
					if (item.equals(tradestrategy)) {
						contract.removeTradestrategy(tradestrategy);
						break;
					}
				}
				if (contract.getTradestrategies().isEmpty()) {
					onCancelRealtimeBars(contract);
				}
			}
		}
	}

	/**
	 * Method onPlaceOrder.
	 * 
	 * @param contract
	 *            Contract
	 * @param tradeOrder
	 *            TradeOrder
	 * @return TradeOrder
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onPlaceOrder(Contract, TradeOrder)
	 */
	public TradeOrder onPlaceOrder(Contract contract, TradeOrder tradeOrder)
			throws BrokerModelException {
		try {
			if (m_client.isConnected()) {
				synchronized (tradeOrder) {
					if (null == tradeOrder.getOrderKey()) {
						tradeOrder.setOrderKey(orderKey.getAndIncrement());
					}
					if (null == tradeOrder.getClientId()) {
						tradeOrder.setClientId(this.m_clientId);
					}
					tradeOrder = m_tradePersistentModel
							.persistTradeOrder(tradeOrder);

					_log.info("Order Placed Key: " + tradeOrder.getOrderKey());
					logContract(TWSBrokerModel.getIBContract(contract));
					logTradeOrder(TWSBrokerModel.getIBOrder(tradeOrder));

					m_client.placeOrder(tradeOrder.getOrderKey(),
							TWSBrokerModel.getIBContract(contract),
							TWSBrokerModel.getIBOrder(tradeOrder));
				}
				return tradeOrder;

			} else {
				throw new BrokerModelException(tradeOrder.getOrderKey(), 3120,
						"Client not conected to TWS order cannot be placed");
			}
		} catch (Exception ex) {
			throw new BrokerModelException(tradeOrder.getOrderKey(), 3130,
					"Could not save or place TradeOrder: "
							+ tradeOrder.getOrderKey() + " Msg: "
							+ ex.getMessage());
		}
	}

	/**
	 * Method onCancelOrder.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onCancelOrder(TradeOrder)
	 */
	public void onCancelOrder(TradeOrder tradeOrder)
			throws BrokerModelException {
		if (m_client.isConnected()) {
			if (null != tradeOrder.getOrderKey()) {
				m_client.cancelOrder(tradeOrder.getOrderKey());
			}
		} else {
			throw new BrokerModelException(tradeOrder.getOrderKey(), 3140,
					"Not conected to TWS order cannot be placed");
		}
	}

	/**
	 * Method execDetails.
	 * 
	 * When orders are filled the the exceDetails is fired followed by
	 * openOrder() and orderStatus() the order methods fire twice. openOrder
	 * gives us the commission amount on the second fire and order status from
	 * both.
	 * 
	 * @param reqId
	 *            int
	 * @param contractIB
	 *            com.ib.client.Contract
	 * @param execution
	 *            Execution
	 * @see com.ib.client.EWrapper#execDetails(int, com.ib.client.Contract,
	 *      Execution)
	 */
	public void execDetails(int reqId, com.ib.client.Contract contractIB,
			Execution execution) {
		try {

			TWSBrokerModel.logExecution(execution);

			TradeOrder transientInstance = m_tradePersistentModel
					.findTradeOrderByKey(new Integer(execution.m_orderId));
			if (null == transientInstance) {
				error(reqId, 3320,
						"Error Trade Order not found for Order Key: "
								+ execution.m_orderId);
				return;
			}

			if (!transientInstance.getIsFilled()) {
				/*
				 * We already have this order fill.
				 */
				if (transientInstance.existTradeOrderfill(execution.m_execId))
					return;

				TradeOrderfill tradeOrderfill = new TradeOrderfill();
				TWSBrokerModel
						.populateTradeOrderfill(execution, tradeOrderfill);
				tradeOrderfill.setTradeOrder(transientInstance);
				transientInstance.addTradeOrderfill(tradeOrderfill);
				transientInstance.setAverageFilledPrice(tradeOrderfill
						.getAveragePrice());
				transientInstance.setFilledQuantity(tradeOrderfill
						.getCumulativeQuantity());
				transientInstance.setFilledDate(tradeOrderfill.getTime());
				transientInstance = m_tradePersistentModel
						.persistTradeOrderfill(transientInstance);
				execDetails.put(transientInstance.getOrderKey(),
						transientInstance);
				_log.info("Exec Details for order key: "
						+ transientInstance.getOrderKey() + " AvgPrice: "
						+ execution.m_avgPrice + " CumQty: "
						+ execution.m_cumQty + " Shares: " + execution.m_shares
						+ " Price: " + execution.m_price);
			}

		} catch (Exception ex) {
			error(reqId, 3160, "Errors saving execution: " + ex.getMessage());
		}
	}

	/**
	 * Method execDetailsEnd.
	 * 
	 * @param reqId
	 *            int
	 * @see com.ib.client.EWrapper#execDetailsEnd(int)
	 */
	public void execDetailsEnd(int reqId) {

		try {

			for (Integer key : execDetails.keySet()) {
				TradeOrder tradeorder = execDetails.get(key);
				if (!tradeorder.getIsFilled()) {
					if (tradeorder.getQuantity().equals(
							tradeorder.getFilledQuantity())) {
						tradeorder.setStatus(OrderStatus.FILLED);
						tradeorder.setIsFilled(true);
						tradeorder = m_tradePersistentModel
								.persistTradeOrder(tradeorder);
						execDetails.replace(key, tradeorder);
						/*
						 * Check for each order that was filled if the trade
						 * should now be closed.
						 */
						// Let the controller know an order was filled
						this.fireTradeOrderFilled(tradeorder);

						if (!tradeorder.getTrade().getIsOpen()) {
							// Let the controller know a position was closed
							this.firePositionClosed(tradeorder.getTrade());
						}
					}
				}
			}

			/*
			 * Let the controller know there are execution details.
			 */
			this.fireExecutionDetailsEnd(execDetails);

		} catch (Exception ex) {
			error(reqId, 3330, "Errors updating open order: " + ex.getMessage());
		}
	}

	/**
	 * Method openOrder.
	 * 
	 * This method is called to feed in open orders.
	 * 
	 * @param orderId
	 *            int
	 * @param contractIB
	 *            com.ib.client.Contract
	 * @param order
	 *            com.ib.client.Order
	 * @param orderState
	 *            OrderState
	 * @see http://www.interactivebrokers.com/php/apiUsersGuide/apiguide.htm
	 */
	public void openOrder(int orderId, com.ib.client.Contract contractIB,
			com.ib.client.Order order, OrderState orderState) {

		try {

			TradeOrder transientInstance = m_tradePersistentModel
					.findTradeOrderByKey(new Integer(order.m_orderId));
			if (null == transientInstance) {
				error(orderId, 3170,
						"Error openOrder not found for Order Key: "
								+ order.m_orderId);
				return;
			}

			/*
			 * Check to see if anything has changed as this method gets fired
			 * twice on order fills.
			 */

			if (TWSBrokerModel.updateTradeOrder(order, orderState,
					transientInstance)) {

				if (OrderStatus.FILLED.equals(transientInstance.getStatus())) {
					_log.info("Open order filled Order Key:"
							+ transientInstance.getOrderKey());
					TWSBrokerModel.logOrderState(orderState);
					TWSBrokerModel.logTradeOrder(order);
					transientInstance = m_tradePersistentModel
							.persistTradeOrder(transientInstance);

					// Let the controller know an order was filled
					this.fireTradeOrderFilled(transientInstance);

					if (!transientInstance.getTrade().getIsOpen()) {
						// Let the controller know a position was closed
						this.firePositionClosed(transientInstance.getTrade());
					}

				} else {
					_log.info("Open order state changed. Status:"
							+ orderState.m_status);
					TWSBrokerModel.logOrderState(orderState);
					TWSBrokerModel.logTradeOrder(order);
					transientInstance = m_tradePersistentModel
							.persistTradeOrder(transientInstance);
					if (OrderStatus.CANCELLED.equals(transientInstance
							.getStatus())) {
						// Let the controller know a position was closed
						this.fireTradeOrderCancelled(transientInstance);
					}
				}
			}
			openOrders.put(transientInstance.getOrderKey(), transientInstance);
		} catch (Exception ex) {
			error(orderId, 3180,
					"Errors updating open order: " + ex.getMessage());
		}
	}

	/*
	 * This method gets called only if there are open orders on startup or
	 * onReqAllOpenOrders.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see com.ib.client.EWrapper#openOrderEnd()
	 */
	public void openOrderEnd() {
		_log.info("openOrderEnd");
		// Let the controller know there are open orders
		for (TradeOrder openOrder : openOrders.values()) {
			_log.info("openOrderEnd Open Order Key: " + openOrder.getOrderKey()
					+ " Order status: " + openOrder.getStatus());
		}
		this.fireOpenOrderEnd(openOrders);
	}

	/**
	 * Method orderStatus.
	 * 
	 * This method is called whenever the status of an order changes. It is also
	 * fired after reconnecting to TWS if the client has any open orders.
	 * 
	 * @param orderId
	 *            int
	 * @param status
	 *            String
	 * @param filled
	 *            int
	 * @param remaining
	 *            int
	 * @param avgFillPrice
	 *            double
	 * @param permId
	 *            int
	 * @param parentId
	 *            int
	 * @param lastFillPrice
	 *            double
	 * @param clientId
	 *            int
	 * @param whyHeld
	 *            String
	 * @see http://www.interactivebrokers.com/php/apiUsersGuide/apiguide.htm
	 */
	public void orderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) {
		try {

			TradeOrder transientInstance = m_tradePersistentModel
					.findTradeOrderByKey(new Integer(orderId));
			if (null == transientInstance) {
				error(orderId, 3190,
						"Error Execution Details order not found for Order Key: "
								+ orderId);
				return;
			}
			/*
			 * Check to see if anything has changed as this method gets fired
			 * twice on order fills.
			 */
			boolean changed = false;
			if (CoreUtils.nullSafeComparator(transientInstance.getStatus(),
					status.toUpperCase()) != 0) {
				transientInstance.setStatus(status.toUpperCase());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientInstance.getWhyHeld(),
					whyHeld) != 0) {
				transientInstance.setWhyHeld(whyHeld);
				changed = true;
			}

			if (changed) {
				if (filled > 0) {
					transientInstance.setAverageFilledPrice(new BigDecimal(
							avgFillPrice));
					transientInstance.setFilledQuantity(filled);
				}
				transientInstance.setStatus(status.toUpperCase());
				transientInstance.setWhyHeld(whyHeld);
				_log.info("Order Status changed. Status: " + status);
				TWSBrokerModel.logOrderStatus(orderId, status, filled,
						remaining, avgFillPrice, permId, parentId,
						lastFillPrice, clientId, whyHeld);

				transientInstance = m_tradePersistentModel
						.persistTradeOrder(transientInstance);
				if (OrderStatus.CANCELLED.equals(transientInstance.getStatus())) {
					// Let the controller know a position was closed
					this.fireTradeOrderCancelled(transientInstance);
				}
			}
		} catch (Exception ex) {
			error(orderId, 3200,
					"Errors updating open order status: " + ex.getMessage());
		}
	}

	/**
	 * Method error.
	 * 
	 * @param ex
	 *            Exception
	 * @see com.ib.client.AnyWrapper#error(Exception)
	 */
	public void error(Exception ex) {
		_log.error("BrokerModel error ex: " + ex.getMessage());
		// this.fireBrokerError(new BrokerManagerModelException(ex));
	}

	/**
	 * Method error.
	 * 
	 * @param str
	 *            String
	 * @see com.ib.client.AnyWrapper#error(String)
	 */
	public void error(String str) {
		_log.error("BrokerModel error str: " + str);
		// this.fireBrokerError(new BrokerManagerModelException(str));
	}

	/**
	 * 
	 * 0 - 999 are IB TWS error codes for Orders or data 1000 - 1999 are IB TWS
	 * System error 2000 - 2999 are IB TWS Warning 4000 - 4999 are application
	 * warnings 5000 - 5999 are application information
	 * 
	 * 
	 * @param id
	 *            int
	 * @param code
	 *            int
	 * @param msg
	 *            String
	 * @see com.ib.client.AnyWrapper#error(int, int, String)
	 */
	public void error(int id, int code, String msg) {
		String symbol = "N/A";
		BrokerModelException brokerModelException = null;

		if (m_historyDataRequests.containsKey(id)) {
			symbol = m_historyDataRequests.get(id).getSymbol();
			synchronized (m_historyDataRequests) {
				m_historyDataRequests.remove(id);
				m_historyDataRequests.notifyAll();
			}
		}
		if (m_realTimeBarsRequests.containsKey(id)) {
			symbol = m_realTimeBarsRequests.get(id).getSymbol();
			synchronized (m_realTimeBarsRequests) {
				m_realTimeBarsRequests.remove(id);
				m_realTimeBarsRequests.notifyAll();
			}
		}
		if (m_contractRequests.containsKey(id)) {
			symbol = m_contractRequests.get(id).getSymbol();
			synchronized (m_contractRequests) {
				m_contractRequests.remove(id);
				m_contractRequests.notifyAll();
			}
		}

		/*
		 * Error code 162 (Historical data request pacing violation)and 366 (No
		 * historical data query found for ticker id) are error code for no
		 * market or historical data found.
		 */
		if (((code > 1999) && (code < 3000)) || ((code >= 200) && (code < 299))
				|| (code == 366) || (code == 162)) {
			if (((code > 1999) && (code < 3000))) {
				_log.info("BrokerModel Req Id: " + id + " Code: " + code
						+ " Msg: " + msg);
				brokerModelException = new BrokerModelException(3, code,
						"Code: " + code + " " + msg);
			} else if (code == 202 || code == 201) {
				_log.warn("BrokerModel Order Id: " + id + " Code: " + code
						+ " Msg: " + msg);
				brokerModelException = new BrokerModelException(2, code,
						"Order Id: " + id + " Code: " + code + " " + msg);
			} else {
				_log.warn("BrokerModel symbol: " + symbol + " Req Id: " + id
						+ " Code: " + code + " Msg: " + msg);
				brokerModelException = new BrokerModelException(2, code,
						"Req Id: " + id + " Code: " + code + " Symbol: "
								+ symbol + " " + msg);
			}

		} else {
			_log.error("BrokerModel symbol: " + symbol + " Req Id: " + id
					+ " Code: " + code + " Msg: " + msg);
			brokerModelException = new BrokerModelException(1, code, "Req Id: "
					+ id + " Error Code: " + code + " Symbol: " + symbol + " "
					+ msg);
		}
		this.fireBrokerError(brokerModelException);
	}

	/**
	 * Method tickPrice.
	 * 
	 * @param reqId
	 *            int
	 * @param field
	 *            int
	 * @param value
	 *            double
	 * @param canAutoExecute
	 *            int
	 * @see com.ib.client.EWrapper#tickPrice(int, int, double, int)
	 */
	public synchronized void tickPrice(int reqId, int field, double value,
			int canAutoExecute) {
		try {
			if (m_realTimeBarsRequests.containsKey(new Integer(reqId))) {
				Contract contract = m_realTimeBarsRequests.get(reqId);
				synchronized (contract) {

					/*
					 * Make sure the lastPrice is between the current Bid/Ask as
					 * prints can come in late in T/S i.e. bad ticks that are
					 * outside the current Bid/Ask.
					 */
					switch (field) {
					case TickType.ASK: {
						contract.setLastAskPrice(new BigDecimal(value));
						break;
					}
					case TickType.BID: {
						contract.setLastBidPrice(new BigDecimal(value));
						break;
					}
					case TickType.LAST: {

						// for (Tradestrategy tradestrategy : contract
						// .getTradestrategies()) {
						// StrategyData datasetContainer = tradestrategy
						// .getDatasetContainer();
						// if (datasetContainer.getBaseCandleSeries()
						// .getItemCount() > 0) {
						// CandleItem candle = (CandleItem) datasetContainer
						// .getBaseCandleSeries().getDataItem(
						// datasetContainer
						// .getBaseCandleSeries()
						// .getItemCount() - 1);
						// if (contract.getLastAskPrice().doubleValue() > 0
						// && contract.getLastBidPrice()
						// .doubleValue() > 0
						// && (value <= contract.getLastAskPrice()
						// .doubleValue() && value >= contract
						// .getLastBidPrice()
						// .doubleValue())) {
						// if ((value > candle.getHigh() || value < candle
						// .getLow())) {
						// candle.setClose(value);
						// datasetContainer.getBaseCandleSeries()
						// .fireSeriesChanged();
						// _log.info("TickPrice Symbol: "
						// + tradestrategy.getContract()
						// .getSymbol() + " "
						// + TickType.getField(field)
						// + " : " + value);
						// }
						//
						// }
						// }
						// }

						break;
					}
					default: {
						break;
					}
					}
				}
			}

		} catch (Exception ex) {
			error(reqId, 3210, ex.getMessage());
		}
	}

	/**
	 * Method tickSize.
	 * 
	 * @param reqId
	 *            int
	 * @param field
	 *            int
	 * @param value
	 *            int
	 * @see com.ib.client.EWrapper#tickSize(int, int, int)
	 */
	public synchronized void tickSize(int reqId, int field, int value) {
		try {
			switch (field) {
			case TickType.VOLUME: {

				// if (m_realTimeBarsRequests.containsKey(new Integer(reqId))) {
				// Contract contract = m_realTimeBarsRequests.get(reqId);
				// synchronized (contract) {
				// for (Tradestrategy tradestrategy : contract
				// .getTradestrategies()) {
				// StrategyData datasetContainer = tradestrategy
				// .getDatasetContainer();
				// if (datasetContainer.getBaseCandleSeries()
				// .getItemCount() > 0) {
				// CandleItem candle = (CandleItem) datasetContainer
				// .getBaseCandleSeries().getDataItem(
				// datasetContainer
				// .getBaseCandleSeries()
				// .getItemCount() - 1);
				// candle.setVolume(value * 100);
				// datasetContainer.getBaseCandleSeries()
				// .fireSeriesChanged();
				// _log.info("TickSize  Symbol: "
				// + tradestrategy.getContract()
				// .getSymbol() + " "
				// + TickType.getField(field) + " : "
				// + (value * 100));
				// }
				// }
				// }
				// }

				break;
			}
			default: {
				break;
			}
			}
		} catch (Exception ex) {
			error(reqId, 3210, ex.getMessage());
		}
	}

	/**
	 * Method tickString.
	 * 
	 * @param reqId
	 *            int
	 * @param field
	 *            int
	 * @param value
	 *            String
	 * @see com.ib.client.EWrapper#tickString(int, int, String)
	 */
	public synchronized void tickString(int reqId, int field, String value) {

		try {

			/*
			 * 48 = RTVolume String = last trade price;last trade size;last
			 * trade time;total volume;vwap;single trade flag
			 */
			synchronized (value) {
				switch (field) {
				case TickType.RT_VOLUME: {
					if (m_realTimeBarsRequests.containsKey(new Integer(reqId))) {
						StringTokenizer st = new StringTokenizer(value, ";");
						int tokenNumber = 0;
						double price = 0;
						Date time = null;
						while (st.hasMoreTokens()) {
							tokenNumber++;
							String token = st.nextToken();

							switch (tokenNumber) {
							case 1: {
								price = Double.parseDouble(token);
								break;
							}
							// case 2: {
							// _log.info("TickString Trade Size: "
							// + Integer.parseInt(token));
							// break;
							// }
							case 3: {
								time = new Date(Long.parseLong(token));
								break;
							}
							// case 4: {
							// _log.info("TickString Total Volume: "
							// + Integer.parseInt(token) * 100);
							// break;
							// }
							// case 5: {
							// _log.info("TickString Total Vwap: " + token);
							// break;
							// }
							// case 6: {
							// break;
							// }
							default: {
								break;
							}
							}
						}
						Contract contract = m_realTimeBarsRequests.get(reqId);
						synchronized (contract) {
							for (Tradestrategy tradestrategy : contract
									.getTradestrategies()) {

								StrategyData datasetContainer = tradestrategy
										.getDatasetContainer();

								int index = datasetContainer
										.getBaseCandleSeries().indexOf(time);
								if (index > 0) {
									CandleItem candle = (CandleItem) datasetContainer
											.getBaseCandleSeries().getDataItem(
													index);
									if (contract.getLastAskPrice()
											.doubleValue() > 0
											&& contract.getLastBidPrice()
													.doubleValue() > 0
											&& (price <= contract
													.getLastAskPrice()
													.doubleValue() && price >= contract
													.getLastBidPrice()
													.doubleValue())) {

										if (price > 0
												&& (price > candle.getHigh() || price < candle
														.getLow())) {
											candle.setClose(price);
											datasetContainer
													.getBaseCandleSeries()
													.fireSeriesChanged();
											// _log.info("TickString Symbol: "
											// + contract.getSymbol()
											// + " Trade Time: " + time
											// + " Price: " + price);
										}
									}
								}
							}
						}
					}
					break;
				}
				default: {
					break;
				}
				}
			}

		} catch (Exception ex) {
			error(reqId, 3210, ex.getMessage());
		}

	}

	/**
	 * Method tickOptionComputation.
	 * 
	 * @param reqId
	 *            int
	 * @param field
	 *            int
	 * @param impliedVol
	 *            double
	 * @param delta
	 *            double
	 * @param optPrice
	 *            double
	 * @param pvDividend
	 *            double
	 * @param gamma
	 *            double
	 * @param vega
	 *            double
	 * @param theta
	 *            double
	 * @param undPrice
	 *            double
	 * @see com.ib.client.EWrapper#tickOptionComputation(int, int, double,
	 *      double, double, double, double, double, double, double)
	 */
	public void tickOptionComputation(int reqId, int field, double impliedVol,
			double delta, double optPrice, double pvDividend, double gamma,
			double vega, double theta, double undPrice) {
		_log.info("tickOptionComputation:" + reqId);
	}

	/**
	 * Method tickGeneric.
	 * 
	 * @param reqId
	 *            int
	 * @param tickType
	 *            int
	 * @param value
	 *            double
	 * @see com.ib.client.EWrapper#tickGeneric(int, int, double)
	 */
	public void tickGeneric(int reqId, int tickType, double value) {
		_log.info("tickGeneric: " + reqId + " tickType: " + tickType
				+ " tickValue: " + value);
	}

	/**
	 * Method tickEFP.
	 * 
	 * @param reqId
	 *            int
	 * @param tickType
	 *            int
	 * @param basisPoints
	 *            double
	 * @param formattedBasisPoints
	 *            String
	 * @param impliedFuture
	 *            double
	 * @param holdDays
	 *            int
	 * @param futureExpiry
	 *            String
	 * @param dividendImpact
	 *            double
	 * @param dividendsToExpiry
	 *            double
	 * @see com.ib.client.EWrapper#tickEFP(int, int, double, String, double,
	 *      int, String, double, double)
	 */
	public void tickEFP(int reqId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		_log.info("tickEFP:" + reqId);
	}

	/**
	 * Method updateAccountValue.
	 * 
	 * @param key
	 *            String
	 * @param value
	 *            String
	 * @param currency
	 *            String
	 * @param accountNumber
	 *            String
	 * @see com.ib.client.EWrapper#updateAccountValue(String, String, String,
	 *      String)
	 */
	public void updateAccountValue(String key, String value, String currency,
			String accountNumber) {

		synchronized (key) {

			// _log.info("updateAccountValue Account#: " + accountNumber +
			// " Key:"
			// + key + " Value:" + value + " Currency:" + currency);
			if (m_accountRequests.containsKey(accountNumber)) {
				TradeAccount tradeAccount = m_accountRequests
						.get(accountNumber);
				if (tradeAccount.getCurrency().equals(currency)) {
					if (key.equals(TWSBrokerModel.AVAILABLE_FUNDS)) {
						tradeAccount.setAvailableFunds(new BigDecimal(value));
					}
					if (key.equals(TWSBrokerModel.BUYING_POWER)) {
						tradeAccount.setBuyingPower(new BigDecimal(value));
					}
					if (key.equals(TWSBrokerModel.CASH_BALANCE)) {
						tradeAccount.setCashBalance(new BigDecimal(value));
					}
					if (key.equals(TWSBrokerModel.CURRENCY)) {
						tradeAccount.setCurrency(value);
					}
					if (key.equals(TWSBrokerModel.GROSS_POSITION_VALUE)
							|| key.equals(TWSBrokerModel.STOCK_MKT_VALUE)) {
						tradeAccount
								.setGrossPositionValue(new BigDecimal(value));
					}
					if (key.equals(TWSBrokerModel.REALIZED_P_L)) {
						tradeAccount.setRealizedPnL(new BigDecimal(value));
					}
					if (key.equals(TWSBrokerModel.UNREALIZED_P_L)) {
						tradeAccount.setUnrealizedPnL(new BigDecimal(value));
					}
				}
			}
		}
	}

	/**
	 * Method updatePortfolio.
	 * 
	 * @param contract
	 *            com.ib.client.Contract
	 * @param position
	 *            int
	 * @param marketPrice
	 *            double
	 * @param marketValue
	 *            double
	 * @param averageCost
	 *            double
	 * @param unrealizedPNL
	 *            double
	 * @param realizedPNL
	 *            double
	 * @param accountNumber
	 *            String
	 * @see com.ib.client.EWrapper#updatePortfolio(com.ib.client.Contract, int,
	 *      double, double, double, double, double, String)
	 */
	public void updatePortfolio(com.ib.client.Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountNumber) {
		// _log.info("updatePortfolio Account#: " + accountNumber + " contract:"
		// + contract.m_symbol + " position:" + position + " marketPrice:"
		// + marketPrice + " marketValue:" + marketValue + " averageCost:"
		// + averageCost + " unrealizedPNL:" + unrealizedPNL
		// + " realizedPNL:" + realizedPNL);
	}

	/**
	 * Method updateAccountTime.
	 * 
	 * @param timeStamp
	 *            String
	 * @see com.ib.client.EWrapper#updateAccountTime(String)
	 */
	public void updateAccountTime(String timeStamp) {

		try {
			// _log.info("updateAccountTime:" + updateDate);
			for (String accountNumber : m_accountRequests.keySet()) {
				TradeAccount tradeAccount = m_accountRequests
						.get(accountNumber);
				/*
				 * Don't use the incoming time stamp as this does not show
				 * seconds just HH:mm format.
				 */
				tradeAccount.setUpdateDate(new Date());
				tradeAccount = (TradeAccount) m_tradePersistentModel
						.persistAspect(tradeAccount);
				m_accountRequests.replace(accountNumber, tradeAccount);
				this.fireUpdateAccountTime(accountNumber);
			}
		} catch (Exception ex) {
			error(0, 3310, "Errors updating Trade Account: " + ex.getMessage());
		}
	}

	/**
	 * Method accountDownloadEnd.
	 * 
	 * @param accountNumber
	 *            String
	 * @see com.ib.client.EWrapper#accountDownloadEnd(String)
	 */
	public void accountDownloadEnd(String accountNumber) {
		// _log.info("accountDownloadEnd:" + accountNumber);
	}

	/**
	 * Method getNextRequestId.
	 * 
	 * @return Integer
	 * @see org.trade.broker.BrokerModel#getNextRequestId()
	 */
	public Integer getNextRequestId() {
		return new Integer(reqId.incrementAndGet());
	}

	/**
	 * Method nextValidId.
	 * 
	 * @param orderId
	 *            int
	 * @see com.ib.client.EWrapper#nextValidId(int)
	 */
	public void nextValidId(int orderId) {
		try {
			int maxKey = m_tradePersistentModel.findTradeOrderByMaxKey();
			if (maxKey < 100000) {
				maxKey = 100000;
			}
			if (maxKey < orderId) {
				orderKey = new AtomicInteger(orderId);
			} else {
				orderKey = new AtomicInteger(maxKey + 1);
			}
			this.fireConnectionOpened();

		} catch (Exception ex) {
			error(orderId, 3210, ex.getMessage());
		}
	}

	/**
	 * Method contractDetails.
	 * 
	 * @param reqId
	 *            int
	 * @param contractDetails
	 *            ContractDetails
	 * @see com.ib.client.EWrapper#contractDetails(int, ContractDetails)
	 */
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		try {
			synchronized (m_contractRequests) {
				if (m_contractRequests.containsKey(reqId)) {
					Contract transientContract = m_contractRequests.get(reqId);
					TWSBrokerModel.logContractDetails(contractDetails);
					TWSBrokerModel.populateContract(contractDetails,
							transientContract);
					m_tradePersistentModel.persistContract(transientContract);
				} else {
					error(reqId, 3220, "Contract details not found for reqId: "
							+ reqId);
				}
			}
		} catch (Exception ex) {
			error(reqId, 3230, ex.getMessage());
		}
	}

	/**
	 * Method bondContractDetails.
	 * 
	 * @param reqId
	 *            int
	 * @param contractDetails
	 *            ContractDetails
	 * @see com.ib.client.EWrapper#bondContractDetails(int, ContractDetails)
	 */
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		_log.info("bondContractDetails:" + reqId);
	}

	/**
	 * Method contractDetailsEnd.
	 * 
	 * @param reqId
	 *            int
	 * @see com.ib.client.EWrapper#contractDetailsEnd(int)
	 */
	public void contractDetailsEnd(int reqId) {
		synchronized (m_contractRequests) {
			m_contractRequests.remove(reqId);
		}
	}

	/**
	 * Method updateMktDepth.
	 * 
	 * @param tickerId
	 *            int
	 * @param position
	 *            int
	 * @param operation
	 *            int
	 * @param side
	 *            int
	 * @param price
	 *            double
	 * @param size
	 *            int
	 * @see com.ib.client.EWrapper#updateMktDepth(int, int, int, int, double,
	 *      int)
	 */
	public void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) {

	}

	/**
	 * Method updateMktDepthL2.
	 * 
	 * @param tickerId
	 *            int
	 * @param position
	 *            int
	 * @param marketMaker
	 *            String
	 * @param operation
	 *            int
	 * @param side
	 *            int
	 * @param price
	 *            double
	 * @param size
	 *            int
	 * @see com.ib.client.EWrapper#updateMktDepthL2(int, int, String, int, int,
	 *      double, int)
	 */
	public void updateMktDepthL2(int tickerId, int position,
			String marketMaker, int operation, int side, double price, int size) {

	}

	/**
	 * Method updateNewsBulletin.
	 * 
	 * @param msgId
	 *            int
	 * @param msgType
	 *            int
	 * @param message
	 *            String
	 * @param origExchange
	 *            String
	 * @see com.ib.client.EWrapper#updateNewsBulletin(int, int, String, String)
	 */
	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {

	}

	/**
	 * Method managedAccounts.
	 * 
	 * @param accountNumber
	 *            String
	 * @see com.ib.client.EWrapper#managedAccounts(String)
	 */
	public void managedAccounts(String accountNumbers) {
		_log.info("Managed accounts: " + accountNumbers);
		this.fireManagedAccountsUpdated(accountNumbers);
	}

	/**
	 * Method receiveFA.
	 * 
	 * @param faDataType
	 *            int
	 * @param xml
	 *            String
	 * @see com.ib.client.EWrapper#receiveFA(int, String)
	 */
	public void receiveFA(int faDataType, String xml) {

	}

	/**
	 * Method marketDataType.
	 * 
	 * @param reqId
	 *            int
	 * @param marketDataType
	 *            int
	 * @see com.ib.client.EWrapper#marketDataType(int, int)
	 */
	public void marketDataType(int reqId, int marketDataType) {

	}

	/**
	 * Method historicalData.
	 * 
	 * @param reqId
	 *            int
	 * @param dateString
	 *            String
	 * @param open
	 *            double
	 * @param high
	 *            double
	 * @param low
	 *            double
	 * @param close
	 *            double
	 * @param volume
	 *            int
	 * @param tradeCount
	 *            int
	 * @param vwap
	 *            double
	 * @param hasGaps
	 *            boolean
	 * @see com.ib.client.EWrapper#historicalData(int, String, double, double,
	 *      double, double, int, int, double, boolean)
	 */
	public void historicalData(int reqId, String dateString, double open,
			double high, double low, double close, int volume, int tradeCount,
			double vwap, boolean hasGaps) {

		volume = volume * 100;
		if (m_historyDataRequests.containsKey(reqId)) {
			Contract contract = m_historyDataRequests.get(reqId);

			if (dateString.contains("finished-")) {
				// _log.info("HistoricalData complete: "
				// + tradestrategy.getContract().getSymbol());

				/*
				 * The last one has arrived the reqId is the tradeStrategyId.
				 * Remove this from the processing vector.
				 */

				synchronized (m_historyDataRequests) {
					m_historyDataRequests.remove(reqId);
					m_historyDataRequests.notifyAll();
					_log.info("Historical data complete for: " + reqId);
				}

				try {

					Tradestrategy tradestrategy = contract.getTradestrategies()
							.get(0);
					CandleSeries candleSeries = tradestrategy
							.getDatasetContainer().getBaseCandleSeries();
					m_tradePersistentModel.persistCandleSeries(candleSeries);
				} catch (Exception ex) {
					error(reqId, 3240, ex.getMessage());
				}
				/*
				 * Check to see if the trading day is today and this strategy is
				 * selected to trade and that the market is open
				 */
				for (Tradestrategy tradestrategy : contract
						.getTradestrategies()) {
					this.fireHistoricalDataComplete(tradestrategy);
					if (tradestrategy.getTradingday().getClose()
							.after(new Date())) {
						if (!this.isRealtimeBarsRunning(contract)) {
							try {
								this.onReqRealTimeBars(contract, tradestrategy
										.getStrategy().getMarketData());
							} catch (BrokerModelException ex) {
								error(reqId, 3250, ex.getMessage());
							}
						}
					} else {
						tradestrategy.getDatasetContainer().cancel();
					}
				}

			} else {

				Date date = null;
				try {
					/*
					 * There is a bug in the TWS interface format for dates
					 * should always be milli sec but when 1 day is selected as
					 * the period the dates come through as yyyyMMdd.
					 */
					if (dateString.length() == 8) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
						date = sdf.parse(dateString);

					} else {
						date = TradingCalendar.getDate(Long
								.parseLong(dateString) * 1000);
					}
				} catch (Exception ex) {
					error(reqId, 3260, ex.getMessage());
					return;
				}

				for (Tradestrategy tradestrategy : contract
						.getTradestrategies()) {
					/*
					 * For daily bars set the time to the open time.
					 */
					if (tradestrategy.getBarSize() == 1) {
						date = TradingCalendar.getSpecificTime(tradestrategy
								.getTradingday().getOpen(), date);
					}

					if (TradingCalendar.isMarketHours(tradestrategy
							.getTradingday().getOpen(), tradestrategy
							.getTradingday().getClose(), date)) {
						tradestrategy.getDatasetContainer().buildCandle(date,
								open, high, low, close, volume, vwap,
								tradeCount, 1);
					}
				}
			}
		}
	}

	/**
	 * Method scannerParameters.
	 * 
	 * @param xml
	 *            String
	 * @see com.ib.client.EWrapper#scannerParameters(String)
	 */
	public void scannerParameters(String xml) {

	}

	/**
	 * Method scannerData.
	 * 
	 * @param reqId
	 *            int
	 * @param rank
	 *            int
	 * @param contractDetails
	 *            ContractDetails
	 * @param distance
	 *            String
	 * @param benchmark
	 *            String
	 * @param projection
	 *            String
	 * @param legsStr
	 *            String
	 * @see com.ib.client.EWrapper#scannerData(int, int, ContractDetails,
	 *      String, String, String, String)
	 */
	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {

	}

	/**
	 * Method scannerDataEnd.
	 * 
	 * @param reqId
	 *            int
	 * @see com.ib.client.EWrapper#scannerDataEnd(int)
	 */
	public void scannerDataEnd(int reqId) {

	}

	/**
	 * Method realtimeBar.
	 * 
	 * @param reqId
	 *            int
	 * @param time
	 *            long
	 * @param open
	 *            double
	 * @param high
	 *            double
	 * @param low
	 *            double
	 * @param close
	 *            double
	 * @param volume
	 *            long
	 * @param vwap
	 *            double
	 * @param tradeCount
	 *            int
	 * @see com.ib.client.EWrapper#realtimeBar(int, long, double, double,
	 *      double, double, long, double, int)
	 */

	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double vwap, int tradeCount) {
		// Called when a candle finishes
		try {
			volume = volume * 100;
			Date date = TradingCalendar.getDate(time * 1000);
			// Only store data that is during mkt hours
			if (m_realTimeBarsRequests.containsKey(reqId)) {
				Contract contract = m_realTimeBarsRequests.get(reqId);
				synchronized (contract) {
					Collections.sort(contract.getTradestrategies(),
							Tradestrategy.TRADINGDAY_CONTRACT);
					Integer prevBarSize = null;
					for (Tradestrategy tradestrategy : contract
							.getTradestrategies()) {
						StrategyData datasetContainer = tradestrategy
								.getDatasetContainer();
						/*
						 * RollupInterval is the number of bar that rollup to
						 * make the bar. So for real time we have 5sec bars
						 * rolling up to the barSize so for 5min bars calc is
						 * 5min *60/5 seconds = 60. i.e 60 values summed
						 * together give us the rolling vwap and volume values.
						 */
						if (TradingCalendar.isMarketHours(tradestrategy
								.getTradingday().getOpen(), tradestrategy
								.getTradingday().getClose(), date)) {
							int dataItemIndex = datasetContainer.buildCandle(
									date, open, high, low, close, volume, vwap,
									tradeCount,
									(tradestrategy.getBarSize() / 5));

							if (dataItemIndex > -1) {
								CandleItem candleItem = (CandleItem) datasetContainer
										.getBaseCandleSeries().getDataItem(
												dataItemIndex);
								if (!candleItem.getCandle().getBarSize()
										.equals(prevBarSize)) {
									m_tradePersistentModel
											.persistCandleItem(candleItem);
									prevBarSize = candleItem.getCandle()
											.getBarSize();
								}
							}
						}
					}
				}
			}
		} catch (Throwable ex) {
			error(reqId, 3270, ex.getMessage());
		}
	}

	/**
	 * Method currentTime.
	 * 
	 * @param time
	 *            long
	 * @see com.ib.client.EWrapper#currentTime(long)
	 */
	public void currentTime(long time) {

	}

	/**
	 * Method fundamentalData.
	 * 
	 * @param reqId
	 *            int
	 * @param data
	 *            String
	 * @see com.ib.client.EWrapper#fundamentalData(int, String)
	 */
	public void fundamentalData(int reqId, String data) {

	}

	/**
	 * Method deltaNeutralValidation.
	 * 
	 * @param reqId
	 *            int
	 * @param underComp
	 *            UnderComp
	 * @see com.ib.client.EWrapper#deltaNeutralValidation(int, UnderComp)
	 */
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {

	}

	/**
	 * Method tickSnapshotEnd.
	 * 
	 * @param reqId
	 *            int
	 * @see com.ib.client.EWrapper#tickSnapshotEnd(int)
	 */
	public void tickSnapshotEnd(int reqId) {

	}

	/**
	 * Method commissionReport.
	 * 
	 * @param commsReport
	 *            com.ib.client.CommissionReport
	 */
	public void commissionReport(CommissionReport commsReport) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method printCandles.
	 * 
	 * @param series
	 *            CandleSeries
	 */
	@SuppressWarnings("unused")
	private void printCandles(CandleSeries series) {
		for (int i = 0; i < series.getItemCount(); i++) {
			CandleItem candle = (CandleItem) series.getDataItem(i);
			_log.debug(" symbol: " + series.getContract().getSymbol()
					+ " Time: " + candle.getPeriod().getStart() + " Open: "
					+ candle.getOpen() + " Close: " + candle.getClose()
					+ " High: " + candle.getHigh() + " Low: " + candle.getLow()
					+ " Volume: " + candle.getVolume());
		}
	}

	/**
	 * Method getIBContract.
	 * 
	 * @param contract
	 *            Contract
	 * @return com.ib.client.Contract
	 * @throws IOException
	 */
	public static com.ib.client.Contract getIBContract(Contract contract)
			throws IOException {
		com.ib.client.Contract ibContract = new com.ib.client.Contract();
		if (null != contract.getIdContractIB()) {
			ibContract.m_conId = contract.getIdContractIB();
		}
		if (null != contract.getSymbol()) {
			ibContract.m_symbol = contract.getSymbol();
		}
		if (null != contract.getSecType()) {
			ibContract.m_secType = contract.getSecType();
		}
		if (null != contract.getExchange()) {
			ibContract.m_exchange = contract.getExchange();
		}
		if (null != contract.getPrimaryExchange()) {
			ibContract.m_primaryExch = contract.getPrimaryExchange();
		}
		if (null != contract.getExpiry()) {
			if (SECType.FUTURE.equals(contract.getSecType())) {
				m_sdfExpiry.setTimeZone(TimeZone.getTimeZone("GMT"));
				ibContract.m_expiry = m_sdfExpiry.format(contract.getExpiry())
						.substring(0, 6);
			}
		}
		if (null != contract.getCurrency()) {
			ibContract.m_currency = contract.getCurrency();
		}

		if (null != contract.getLocalSymbol()) {
			ibContract.m_localSymbol = contract.getLocalSymbol();
		}
		if (null != contract.getSecTypeId()) {
			ibContract.m_secIdType = contract.getSecTypeId();
		}

		return ibContract;
	}

	/**
	 * Method getIBOrder.
	 * 
	 * @param order
	 *            TradeOrder
	 * @return com.ib.client.Order
	 */
	public static com.ib.client.Order getIBOrder(TradeOrder order) {
		com.ib.client.Order ibOrder = new com.ib.client.Order();

		if (null != order.getOrderKey()) {
			ibOrder.m_orderId = order.getOrderKey();
		}
		if (null != order.getClientId()) {
			ibOrder.m_clientId = order.getClientId();
		}
		if (null != order.getPermId()) {
			ibOrder.m_permId = order.getPermId();
		}
		if (null != order.getParentId()) {
			ibOrder.m_parentId = order.getParentId();
		}
		if (null != order.getAction()) {
			ibOrder.m_action = order.getAction();
		}
		if (null != order.getQuantity()) {
			ibOrder.m_totalQuantity = order.getQuantity();
		}
		if (null != order.getOrderType()) {
			ibOrder.m_orderType = order.getOrderType();
		}
		if (null != order.getLimitPrice()) {
			ibOrder.m_lmtPrice = order.getLimitPrice().doubleValue();
		}
		if (null != order.getAuxPrice()) {
			ibOrder.m_auxPrice = order.getAuxPrice().doubleValue();
		}

		if (null != order.getTimeInForce()) {
			ibOrder.m_tif = order.getTimeInForce();
		}
		if (null != order.getOcaGroupName()) {
			ibOrder.m_ocaGroup = order.getOcaGroupName(); // one cancels all
		}
		// group
		// name
		if (null != order.getOcaType()) {
			ibOrder.m_ocaType = order.getOcaType(); // 1 = CANCEL_WITH_BLOCK, 2
		}
		// =
		// REDUCE_WITH_BLOCK, 3 =
		// REDUCE_NON_BLOCK
		if (null != order.getOrderReference()) {
			ibOrder.m_orderRef = order.getOrderReference();
		}
		if (null != order.getTransmit()) {
			ibOrder.m_transmit = order.getTransmit(); // if false, order will be
		}
		if (null != order.getDisplayQuantity()) {
			ibOrder.m_displaySize = order.getDisplayQuantity();
		}
		if (null != order.getTriggerMethod()) {
			ibOrder.m_triggerMethod = order.getTriggerMethod(); // 0=Default
		}

		if (null != order.getHidden()) {
			ibOrder.m_hidden = order.getHidden();
		}
		m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		if (null != order.getGoodAfterTime()) {
			ibOrder.m_goodAfterTime = m_sdfGMT.format(order.getGoodAfterTime());
		}
		if (null != order.getGoodTillTime()) {
			ibOrder.m_goodTillDate = m_sdfGMT.format(order.getGoodTillTime());
		}
		if (null != order.getOverrideConstraints()) {
			ibOrder.m_overridePercentageConstraints = (order
					.getOverrideConstraints() == 0) ? false : true;
		}
		if (null != order.getAllOrNothing()) {
			ibOrder.m_allOrNone = order.getAllOrNothing();
		}
		return ibOrder;
	}

	/**
	 * Method updateTradeOrder.
	 * 
	 * @param ibOrder
	 *            com.ib.client.Order
	 * @param ibOrderState
	 *            com.ib.client.OrderState
	 * @param order
	 *            TradeOrder
	 * @return boolean
	 * @throws ParseException
	 */
	public static boolean updateTradeOrder(com.ib.client.Order ibOrder,
			com.ib.client.OrderState ibOrderState, TradeOrder order)
			throws ParseException {

		boolean changed = false;
		m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (CoreUtils
				.nullSafeComparator(order.getOrderKey(), ibOrder.m_orderId) == 0) {
			if (CoreUtils.nullSafeComparator(order.getStatus(),
					ibOrderState.m_status.toUpperCase()) != 0) {
				order.setStatus(ibOrderState.m_status.toUpperCase());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getWarningMessage(),
					ibOrderState.m_warningText) != 0) {
				order.setWarningMessage(ibOrderState.m_warningText);
				changed = true;
			}
			Money comms = new Money(ibOrderState.m_commission);
			if (CoreUtils
					.nullSafeComparator(comms, new Money(Double.MAX_VALUE)) != 0) {
				if (CoreUtils.nullSafeComparator(order.getCommission(),
						comms.getBigDecimalValue()) != 0) {
					order.setCommission(comms.getBigDecimalValue());
					changed = true;
				}
			}
			if (CoreUtils.nullSafeComparator(order.getClientId(),
					ibOrder.m_clientId) != 0) {
				order.setClientId(ibOrder.m_clientId);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getAction(),
					ibOrder.m_action) != 0) {
				order.setAction(ibOrder.m_action);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getQuantity(),
					ibOrder.m_totalQuantity) != 0) {
				order.setQuantity(ibOrder.m_totalQuantity);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOrderType(),
					ibOrder.m_orderType.replaceAll("\\s+", "")) != 0) {
				order.setOrderType(ibOrder.m_orderType.replaceAll("\\s+", ""));
				changed = true;
			}

			Money lmtPrice = new Money(ibOrder.m_lmtPrice);
			if (CoreUtils.nullSafeComparator(lmtPrice, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(order.getLimitPrice(),
							lmtPrice.getBigDecimalValue()) != 0) {
				order.setLimitPrice(lmtPrice.getBigDecimalValue());
				changed = true;
			}
			Money auxPrice = new Money(ibOrder.m_auxPrice);
			if (CoreUtils.nullSafeComparator(auxPrice, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(order.getAuxPrice(),
							auxPrice.getBigDecimalValue()) != 0) {
				order.setAuxPrice(auxPrice.getBigDecimalValue());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getTimeInForce(),
					ibOrder.m_tif) != 0) {
				order.setTimeInForce(ibOrder.m_tif);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOcaGroupName(),
					ibOrder.m_ocaGroup) != 0) {
				order.setOcaGroupName(ibOrder.m_ocaGroup);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOcaType(),
					ibOrder.m_ocaType) != 0) {
				order.setOcaType(ibOrder.m_ocaType);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOrderReference(),
					ibOrder.m_orderRef) != 0) {
				order.setOrderReference(ibOrder.m_orderRef);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getPermId(),
					ibOrder.m_permId) != 0) {
				order.setPermId(ibOrder.m_permId);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getParentId(),
					ibOrder.m_parentId) != 0) {
				order.setParentId(ibOrder.m_parentId);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getTransmit(),
					ibOrder.m_transmit) != 0) {
				order.setTransmit(ibOrder.m_transmit);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getDisplayQuantity(),
					ibOrder.m_displaySize) != 0) {
				order.setDisplayQuantity(ibOrder.m_displaySize);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getTriggerMethod(),
					ibOrder.m_triggerMethod) != 0) {
				order.setTriggerMethod(ibOrder.m_triggerMethod);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getHidden(),
					ibOrder.m_hidden) != 0) {
				order.setHidden(ibOrder.m_hidden);
				changed = true;
			}
			if (null != ibOrder.m_goodAfterTime) {
				Date goodAfterTime = m_sdfGMT.parse(ibOrder.m_goodAfterTime);
				if (CoreUtils.nullSafeComparator(order.getGoodAfterTime(),
						goodAfterTime) != 0) {
					order.setGoodAfterTime(goodAfterTime);
					changed = true;
				}
			}

			if (null != ibOrder.m_goodTillDate) {
				Date goodTillDate = m_sdfGMT.parse(ibOrder.m_goodTillDate);
				if (CoreUtils.nullSafeComparator(order.getGoodTillTime(),
						goodTillDate) != 0) {
					order.setGoodTillTime(goodTillDate);
					changed = true;
				}
			}
			Integer overridePercentageConstraints = new Integer(
					(ibOrder.m_overridePercentageConstraints ? 1 : 0));
			if (CoreUtils.nullSafeComparator(order.getOverrideConstraints(),
					overridePercentageConstraints) != 0) {
				order.setOverrideConstraints(overridePercentageConstraints);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getAllOrNothing(),
					ibOrder.m_allOrNone) != 0) {
				order.setAllOrNothing(ibOrder.m_allOrNone);
				changed = true;
			}
			if (changed)
				order.setUpdateDate(new Date());
		}
		return changed;
	}

	/**
	 * Method populateContract.
	 * 
	 * @param contractDetails
	 *            com.ib.client.ContractDetails
	 * @param transientContract
	 *            Contract
	 * @throws ParseException
	 */
	public static void populateContract(
			com.ib.client.ContractDetails contractDetails,
			Contract transientContract) throws ParseException {
		if (null != contractDetails.m_summary.m_localSymbol) {
			transientContract
					.setLocalSymbol(contractDetails.m_summary.m_localSymbol);
		}
		if (0 != contractDetails.m_summary.m_conId) {
			transientContract
					.setIdContractIB(contractDetails.m_summary.m_conId);
		}
		if (null != contractDetails.m_summary.m_primaryExch) {
			transientContract
					.setPrimaryExchange(contractDetails.m_summary.m_primaryExch);
		}
		if (null != contractDetails.m_summary.m_exchange) {
			transientContract.setExchange(contractDetails.m_summary.m_exchange);
		}

		if (null != contractDetails.m_summary.m_expiry) {
			m_sdfExpiry.setTimeZone(TimeZone.getTimeZone("GMT"));
			transientContract.setExpiry(m_sdfExpiry
					.parse(contractDetails.m_summary.m_expiry));
		}
		if (null != contractDetails.m_summary.m_secIdType) {
			transientContract
					.setSecTypeId(contractDetails.m_summary.m_secIdType);
		}
		if (null != contractDetails.m_longName) {
			transientContract.setDescription(contractDetails.m_longName);
		}
		if (null != contractDetails.m_summary.m_currency) {
			transientContract.setCurrency(contractDetails.m_summary.m_currency);
		}
		if (null != contractDetails.m_category) {
			transientContract.setCategory(contractDetails.m_category);
		}
		if (null != contractDetails.m_industry) {
			transientContract.setIndustry(contractDetails.m_industry);
		}
		if (0 != contractDetails.m_minTick) {
			transientContract.setMinTick(new BigDecimal(
					contractDetails.m_minTick));
		}
		if (0 != contractDetails.m_priceMagnifier) {
			transientContract.setPriceMagnifier(new BigDecimal(
					contractDetails.m_priceMagnifier));
		}
		if (null != contractDetails.m_summary.m_multiplier) {
			transientContract.setPriceMultiplier(new BigDecimal(
					contractDetails.m_summary.m_multiplier));
		}
		if (null != contractDetails.m_subcategory) {
			transientContract.setSubCategory(contractDetails.m_subcategory);
		}
		if (null != contractDetails.m_tradingClass) {
			transientContract.setTradingClass(contractDetails.m_tradingClass);
		}
	}

	/**
	 * Method populateTradeOrderfill.
	 * 
	 * @param execution
	 *            com.ib.client.Execution
	 * @param tradeOrderfill
	 *            TradeOrderfill
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void populateTradeOrderfill(
			com.ib.client.Execution execution, TradeOrderfill tradeOrderfill)
			throws ParseException, IOException {
		TimeZone twsTimeZone = TimeZone.getTimeZone(ConfigProperties
				.getPropAsString("trade.tws.timezone"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		sdf.setTimeZone(twsTimeZone);
		Date date = sdf.parse(execution.m_time);
		tradeOrderfill.setTime(date);
		tradeOrderfill.setExchange(execution.m_exchange);
		tradeOrderfill.setSide(execution.m_side);
		tradeOrderfill.setQuantity(execution.m_shares);
		tradeOrderfill.setPrice(new BigDecimal(execution.m_price));
		tradeOrderfill.setAveragePrice(new BigDecimal(execution.m_avgPrice));
		tradeOrderfill.setCumulativeQuantity(execution.m_cumQty);
		tradeOrderfill.setExecId(execution.m_execId);
	}

	/**
	 * Method getIBExecutionFilter.
	 * 
	 * @param clientId
	 *            Integer
	 * @param mktOpen
	 *            Date
	 * @param secType
	 *            String
	 * @param symbol
	 *            String
	 * @return com.ib.client.ExecutionFilter
	 */
	public static com.ib.client.ExecutionFilter getIBExecutionFilter(
			Integer clientId, Date mktOpen, String secType, String symbol) {

		com.ib.client.ExecutionFilter executionFilter = new com.ib.client.ExecutionFilter();
		if (null != secType) {
			executionFilter.m_secType = secType;
		}
		if (null != symbol) {
			executionFilter.m_symbol = symbol;
		}
		m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		executionFilter.m_time = m_sdfGMT.format(mktOpen);
		executionFilter.m_clientId = clientId;
		return executionFilter;
	}

	/**
	 * Method logOrderStatus.
	 * 
	 * @param orderId
	 *            int
	 * @param status
	 *            String
	 * @param filled
	 *            int
	 * @param remaining
	 *            int
	 * @param avgFillPrice
	 *            double
	 * @param permId
	 *            int
	 * @param parentId
	 *            int
	 * @param lastFillPrice
	 *            double
	 * @param clientId
	 *            int
	 * @param whyHeld
	 *            String
	 */
	public static void logOrderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) {

		_log.info("orderId: " + orderId + " status: " + status + " filled: "
				+ filled + " remaining: " + remaining + " avgFillPrice: "
				+ avgFillPrice + " permId: " + permId + " parentId: "
				+ parentId + " lastFillPrice: " + lastFillPrice + " clientId: "
				+ clientId + " whyHeld: " + whyHeld);
	}

	/**
	 * Method logTradeOrder.
	 * 
	 * @param order
	 *            com.ib.client.Order
	 */
	public static void logTradeOrder(com.ib.client.Order order) {

		_log.info("OrderKey: " + +order.m_orderId + " ClientId: "
				+ order.m_clientId + " PermId: " + order.m_permId + " Action: "
				+ order.m_action + " TotalQuantity: " + order.m_totalQuantity
				+ " OrderType: " + order.m_orderType + " LmtPrice: "
				+ order.m_lmtPrice + " AuxPrice: " + order.m_auxPrice
				+ " Tif: " + order.m_tif + " OcaGroup: " + order.m_ocaGroup
				+ " OcaType: " + order.m_ocaType + " OrderRef: "
				+ order.m_orderRef + " Transmit: " + order.m_transmit
				+ " DisplaySize: " + order.m_displaySize + " TriggerMethod: "
				+ order.m_triggerMethod + " Hidden: " + order.m_hidden
				+ " ParentId: " + order.m_parentId + " GoodAfterTime: "
				+ order.m_goodAfterTime + " GoodTillDate: "
				+ order.m_goodTillDate + " OverridePercentageConstraints: "
				+ order.m_overridePercentageConstraints + " AllOrNone: "
				+ order.m_allOrNone);
	}

	/**
	 * Method logContract.
	 * 
	 * @param contect
	 *            com.ib.client.Contract
	 */
	public static void logContract(com.ib.client.Contract contract) {
		_log.info("Symbol: " + contract.m_symbol + " Sec Type: "
				+ contract.m_secType + " Exchange: " + contract.m_exchange
				+ " Con Id: " + contract.m_conId + " Currency: "
				+ contract.m_currency + " SecIdType: " + contract.m_secIdType
				+ " Primary Exch: " + contract.m_primaryExch
				+ " Local Symbol: " + contract.m_localSymbol + " SecId: "
				+ contract.m_secId + " Multiplier: " + contract.m_multiplier
				+ " Expiry: " + contract.m_expiry);
	}

	/**
	 * Method logContractDetails.
	 * 
	 * @param contect
	 *            com.ib.client.ContractDetails
	 */
	public static void logContractDetails(
			com.ib.client.ContractDetails contractDetails) {
		_log.info("Symbol: " + contractDetails.m_summary.m_symbol
				+ " Sec Type: " + contractDetails.m_summary.m_secType
				+ " Exchange: " + contractDetails.m_summary.m_exchange
				+ " Con Id: " + contractDetails.m_summary.m_conId
				+ " Currency: " + contractDetails.m_summary.m_currency
				+ " SecIdType: " + contractDetails.m_summary.m_secIdType
				+ " Primary Exch: " + contractDetails.m_summary.m_primaryExch
				+ " Local Symbol: " + contractDetails.m_summary.m_localSymbol
				+ " SecId: " + contractDetails.m_summary.m_secId
				+ " Multiplier: " + contractDetails.m_summary.m_multiplier
				+ " Category: " + contractDetails.m_category + " Expiry: "
				+ contractDetails.m_summary.m_expiry + " ContractMonth: "
				+ contractDetails.m_contractMonth + " Cusip: "
				+ contractDetails.m_cusip + " Industry: "
				+ contractDetails.m_industry + " IssueDate: "
				+ contractDetails.m_issueDate + " MarketName: "
				+ contractDetails.m_marketName + " MinTick: "
				+ contractDetails.m_minTick + " PriceMagnifier: "
				+ contractDetails.m_priceMagnifier);
	}

	/**
	 * Method logOrderState.
	 * 
	 * @param orderState
	 *            com.ib.client.OrderState
	 */
	public static void logOrderState(com.ib.client.OrderState orderState) {
		_log.info("Status: " + orderState.m_status + " Comms Amt: "
				+ orderState.m_commission + " Comms Currency: "
				+ orderState.m_commissionCurrency + " Warning txt: "
				+ orderState.m_warningText + " Init Margin: "
				+ orderState.m_initMargin + " Maint Margin: "
				+ orderState.m_maintMargin + " Min Comms: "
				+ orderState.m_minCommission + " Max Comms: "
				+ orderState.m_maxCommission);
	}

	/**
	 * Method logExecution.
	 * 
	 * @param execution
	 *            com.ib.client.Execution
	 */
	public static void logExecution(com.ib.client.Execution execution) {
		_log.info("execDetails OrderId: " + execution.m_orderId + " ClientId: "
				+ execution.m_clientId + " PermId: " + execution.m_permId
				+ " ExecId: " + execution.m_execId + " Time: "
				+ execution.m_time + " CumQty: " + execution.m_cumQty);
	}
}
