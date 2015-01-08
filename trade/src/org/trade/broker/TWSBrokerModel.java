/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.client.BackTestBroker;
import org.trade.broker.request.TWSAccountAliasRequest;
import org.trade.broker.request.TWSAllocationRequest;
import org.trade.broker.request.TWSGroupRequest;
import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Percent;
import org.trade.dictionary.valuetype.AccountType;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.OverrideConstraints;
import org.trade.dictionary.valuetype.SECType;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TimeInForce;
import org.trade.dictionary.valuetype.TriggerMethod;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.Portfolio;
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
import com.ib.client.TagValue;
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

	private static final SimpleDateFormat _sdfLocal = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss");
	private static final SimpleDateFormat _sdfExpiry = new SimpleDateFormat(
			"yyyyMMdd");

	// Candle series this is listened to by the chart panel
	// and main controller for updates.
	private static final ConcurrentHashMap<Integer, Tradestrategy> m_historyDataRequests = new ConcurrentHashMap<Integer, Tradestrategy>();
	private static final ConcurrentHashMap<Integer, Contract> m_realTimeBarsRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_marketDataRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_contractRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<String, Account> m_accountRequests = new ConcurrentHashMap<String, Account>();

	private static final ConcurrentHashMap<Integer, TradeOrder> openOrders = new ConcurrentHashMap<Integer, TradeOrder>();
	private static final ConcurrentHashMap<Integer, TradeOrder> tradeOrdersExecutions = new ConcurrentHashMap<Integer, TradeOrder>();
	private static final ConcurrentHashMap<String, Execution> executionDetails = new ConcurrentHashMap<String, Execution>();
	private static final ConcurrentHashMap<String, CommissionReport> commissionDetails = new ConcurrentHashMap<String, CommissionReport>();

	private EClientSocket m_client = null;
	private PersistentModel m_tradePersistentModel = null;
	private AtomicInteger reqId = null;
	private AtomicInteger orderKey = null;
	private Integer m_clientId = null;

	private static final int SCALE = 5;
	private static final int minOrderId = 100000;

	private static final String AVAILABLE_FUNDS = "AvailableFunds";
	private static final String ACCOUNTTYPE = "AccountType";
	private static final String BUYING_POWER = "BuyingPower";
	private static final String CASH_BALANCE = "CashBalance";
	private static final String CURRENCY = "Currency";
	private static final String GROSS_POSITION_VALUE = "GrossPositionValue";
	private static final String REALIZED_P_L = "RealizedPnL";
	private static final String UNREALIZED_P_L = "UnrealizedPnL";
	private static final String STOCK_MKT_VALUE = "StockMarketValue";

	/*
	 * TWS socket values see config.properties
	 * 
	 * Determines the date format applied to returned bars. Valid values
	 * include:
	 * 
	 * 1 - dates applying to bars returned in the format:
	 * yyyymmdd{space}{space}hh:mm:dd
	 * 
	 * 2 - dates are returned as a long integer specifying the number of seconds
	 * since 1/1/1970 GMT.
	 */

	private static Integer backfillDateFormat = 2;
	private static Integer backfillUseRTH = 1;
	private static String backfillWhatToShow;
	private static Integer backfillOffsetDays = 0;
	private static String genericTicklist = "233";
	private static boolean marketUpdateOnClose = false;

	static {
		try {
			backfillUseRTH = ConfigProperties
					.getPropAsInt("trade.backfill.useRTH");
			backfillWhatToShow = ConfigProperties
					.getPropAsString("trade.backfill.whatToShow");
			backfillOffsetDays = ConfigProperties
					.getPropAsInt("trade.backfill.offsetDays");
			genericTicklist = ConfigProperties
					.getPropAsString("trade.marketdata.genericTicklist");
			marketUpdateOnClose = ConfigProperties
					.getPropAsBoolean("trade.marketdata.realtime.updateClose");

		} catch (Exception ex) {
			throw new IllegalArgumentException(
					"Error initializing BrokerModel Msg: " + ex.getMessage());
		}
	}

	public TWSBrokerModel() {
		try {
			m_client = new EClientSocket(this);
			m_tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			reqId = new AtomicInteger(
					(int) (System.currentTimeMillis() / 1000d));

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
	 * @return ConcurrentHashMap<Integer,Tradestrategy>
	 * @see org.trade.broker.BrokerModel#getHistoricalData()
	 */
	public ConcurrentHashMap<Integer, Tradestrategy> getHistoricalData() {
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
	public void onConnect(String host, Integer port, Integer clientId) {
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
	public void onDisconnect() {
		onCancelAllRealtimeData();
		if (m_client.isConnected()) {
			for (String accountNumber : m_accountRequests.keySet()) {
				this.onCancelAccountUpdates(accountNumber);
			}
			m_client.eDisconnect();
		}
		this.fireConnectionClosed(false);
	}

	/**
	 * Method connectionClosed.
	 * 
	 * @see com.ib.client.AnyWrapper#connectionClosed()
	 */
	public void connectionClosed() {
		_log.error("TWS Broker Model connectionClosed ");
		onCancelAllRealtimeData();
		this.fireConnectionClosed(true);
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
	 * Method onReqFinancialAccount.
	 * 
	 * @see org.trade.broker.onReqFinancialAccount()
	 */
	public void onReqFinancialAccount() {
		try {
			if (m_client.isConnected()) {
				m_client.requestFA(EClientSocket.ALIASES);
			} else {
				throw new BrokerModelException(0, 3010,
						"Not conected Financial Account data cannot be retrieved");
			}
		} catch (Exception ex) {
			error(0,
					3295,
					"Error requesting Financial Account Msg: "
							+ ex.getMessage());
		}
	}

	/**
	 * Method onReqReplaceFinancialAccount.
	 * 
	 * @param xml
	 *            String
	 * @param faDataType
	 *            int
	 * 
	 * @see org.trade.broker.onReqReplaceFinancialAccount()
	 */
	public void onReqReplaceFinancialAccount(int faDataType, String xml)
			throws BrokerModelException {
		try {
			if (m_client.isConnected()) {
				m_client.replaceFA(faDataType, xml);
			} else {
				throw new BrokerModelException(0, 3010,
						"Not conected Financial Account data cannot be replaced");
			}
		} catch (Exception ex) {
			error(0, 3295,
					"Error replacing Financial Account Msg: " + ex.getMessage());
		}
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
	 * @param accountNumber
	 *            String
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onSubscribeAccountUpdates(boolean,
	 *      Account)
	 */
	public void onSubscribeAccountUpdates(boolean subscribe,
			String accountNumber) throws BrokerModelException {
		try {
			Account account = m_tradePersistentModel
					.findAccountByNumber(accountNumber);
			m_accountRequests.put(accountNumber, account);
			if (m_client.isConnected()) {
				m_client.reqAccountUpdates(subscribe, accountNumber);
			} else {
				throw new BrokerModelException(0, 3010,
						"Not conected to TWS historical account data cannot be retrieved");
			}

		} catch (Exception ex) {
			error(0, 3290, "Error requesting Account: " + accountNumber
					+ " Msg: " + ex.getMessage());
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
	 * @throws IOException
	 * @see org.trade.broker.BrokerModel#onReqAllExecutions(Date)
	 */
	public void onReqAllExecutions(Date mktOpenDate)
			throws BrokerModelException {
		try {
			/*
			 * Request execution reports based on the supplied filter criteria
			 */

			if (m_client.isConnected()) {
				tradeOrdersExecutions.clear();
				Integer reqId = this.getNextRequestId();
				m_client.reqExecutions(reqId, TWSBrokerModel
						.getIBExecutionFilter(m_clientId, mktOpenDate, null,
								null));
			} else {
				throw new BrokerModelException(0, 3020,
						"Not conected to TWS historical data cannot be retrieved");
			}
		} catch (Exception ex) {
			throw new BrokerModelException(0, 3020,
					"Error request executions for Date: " + mktOpenDate
							+ " Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method onReqExecutions.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * 
	 * @param addOrders
	 *            boolean
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqExecutions(Tradestrategy)
	 */
	public void onReqExecutions(Tradestrategy tradestrategy, boolean addOrders)
			throws BrokerModelException {
		try {
			/*
			 * Request execution reports based on the supplied filter criteria
			 */
			Integer clientId = m_clientId;
			if (m_client.isConnected()) {
				tradeOrdersExecutions.clear();
				commissionDetails.clear();
				executionDetails.clear();
				/*
				 * This will get all orders i.e. those created by this client
				 * and those created by other clients in TWS.
				 */
				if (addOrders)
					clientId = 0;

				Integer reqId = tradestrategy.getIdTradeStrategy();
				m_client.reqExecutions(reqId, TWSBrokerModel
						.getIBExecutionFilter(clientId, tradestrategy
								.getTradingday().getOpen(), tradestrategy
								.getContract().getSecType(), tradestrategy
								.getContract().getSymbol()));
			} else {
				throw new BrokerModelException(
						tradestrategy.getIdTradeStrategy(), 3020,
						"Not conected to TWS historical data cannot be retrieved");
			}
		} catch (Exception ex) {
			throw new BrokerModelException(tradestrategy.getIdTradeStrategy(),
					3020, "Error request executions for symbol: "
							+ tradestrategy.getContract().getSymbol()
							+ " Msg: " + ex.getMessage());
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
							3030,
							"RealtimeBars request is already in progress for: "
									+ contract.getSymbol()
									+ " Please wait or cancel.");
				}
				m_realTimeBarsRequests.put(contract.getIdContract(), contract);

				/*
				 * Bar interval is set to 5= 5sec this is the only thing
				 * supported by TWS for live data.
				 */
				Vector<TagValue> realTimeBarOptions = new Vector<TagValue>();
				m_client.reqRealTimeBars(contract.getIdContract(),
						TWSBrokerModel.getIBContract(contract), 5,
						backfillWhatToShow, (backfillUseRTH > 0),
						realTimeBarOptions);

				if (mktData) {
					onReqMarketData(contract, genericTicklist, false);
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
	 * Method onReqMarketData.
	 * 
	 * @param contract
	 *            Contract
	 * @param genericTicklist
	 *            String
	 * @param snapshot
	 *            boolean
	 * @throws BrokerModelException
	 */
	public void onReqMarketData(Contract contract, String genericTicklist,
			boolean snapshot) throws BrokerModelException {
		try {
			if (m_client.isConnected()) {
				if (this.isMarketDataRunning(contract)) {
					throw new BrokerModelException(contract.getIdContract(),
							3030,
							"MarketData request is already in progress for: "
									+ contract.getSymbol()
									+ " Please wait or cancel.");
				}
				List<TagValue> mktDataOptions = new ArrayList<TagValue>();
				m_marketDataRequests.put(contract.getIdContract(), contract);
				m_client.reqMktData(contract.getIdContract(),
						TWSBrokerModel.getIBContract(contract),
						genericTicklist, snapshot, mktDataOptions);

			} else {
				throw new BrokerModelException(contract.getIdContract(), 3040,
						"Not conected to TWS market data cannot be retrieved");
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
						"Not conected to TWS contract data cannot be retrieved");
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
	 * @param tradestrategy
	 *            Tradestrategy
	 * @param Date
	 *            endDate
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onBrokerData(Contract , String , String
	 *      )
	 */
	public void onBrokerData(Tradestrategy tradestrategy, Date endDate)
			throws BrokerModelException {

		try {

			if (m_client.isConnected()) {
				if (this.isHistoricalDataRunning(tradestrategy)) {
					throw new BrokerModelException(
							tradestrategy.getIdTradeStrategy(), 3010,
							"HistoricalData request is already in progress for: "
									+ tradestrategy.getContract().getSymbol()
									+ " Please wait or cancel.");
				}

				/*
				 * When running data via the TWS API we start the
				 * DatasetContainers internal thread to process candle updates
				 * and all indicator updates. That reduces the delay to the
				 * broker interface thread for messages coming in.
				 */
				if (!tradestrategy.getStrategyData().isRunning())
					tradestrategy.getStrategyData().execute();

				m_historyDataRequests.put(tradestrategy.getIdTradeStrategy(),
						tradestrategy);

				endDate = TradingCalendar.getSpecificTime(endDate,
						TradingCalendar.getMostRecentTradingDay(TradingCalendar
								.addBusinessDays(endDate, backfillOffsetDays)));

				String endDateTime = _sdfLocal.format(endDate);

				/*
				 * TWS API data has a limit of one calendar year of data. So
				 * apply this limit to the chartDays.
				 */
				Integer chartDays = tradestrategy.getChartDays();
				if (TradingCalendar.daysDiff(
						TradingCalendar.addDays(endDate,
								(tradestrategy.getChartDays() * -1)),
						new Date()) > TradingCalendar.getDaysInYear(endDate)) {
					chartDays = TradingCalendar.getDaysInYear(endDate)
							- TradingCalendar.daysDiff(endDate, new Date());
				}

				_log.debug("onBrokerData Req Id: "
						+ tradestrategy.getIdTradeStrategy() + " Symbol: "
						+ tradestrategy.getContract().getSymbol()
						+ " end Time: " + endDateTime + " Period length: "
						+ tradestrategy.getChartDays() + " Bar size: "
						+ tradestrategy.getBarSize() + " WhatToShow: "
						+ backfillWhatToShow + " Regular Trading Hrs: "
						+ backfillUseRTH + " Date format: "
						+ backfillDateFormat);
				List<TagValue> chartOptions = new ArrayList<TagValue>();

				m_client.reqHistoricalData(tradestrategy.getIdTradeStrategy(),
						TWSBrokerModel.getIBContract(tradestrategy
								.getContract()), endDateTime, ChartDays
								.newInstance(chartDays).getDisplayName(),
						BarSize.newInstance(tradestrategy.getBarSize())
								.getDisplayName(), backfillWhatToShow,
						backfillUseRTH, backfillDateFormat, chartOptions);

			} else {
				throw new BrokerModelException(
						tradestrategy.getIdTradeStrategy(), 3100,
						"Not conected to TWS historical data cannot be retrieved");
			}
		} catch (Exception ex) {
			throw new BrokerModelException(tradestrategy.getIdTradeStrategy(),
					3110, "Error broker data Symbol: "
							+ tradestrategy.getContract().getSymbol()
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
		if (m_accountRequests.containsKey(accountNumber)) {
			return true;
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
		for (Tradestrategy item : m_historyDataRequests.values()) {
			if (contract.equals(item.getContract())) {
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
		if (m_historyDataRequests.containsKey(tradestrategy
				.getIdTradeStrategy())) {
			return true;
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
		return false;
	}

	/**
	 * Method isMarketDataRunning.
	 * 
	 * @param contract
	 *            Contract
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isRealtimeBarsRunning(Contract)
	 */
	public boolean isMarketDataRunning(Contract contract) {
		if (m_client.isConnected()) {
			if (m_marketDataRequests.containsKey(contract.getIdContract())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method isMarketDataRunning.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return boolean
	 */
	public boolean isMarketDataRunning(Tradestrategy tradestrategy) {
		if (m_marketDataRequests.containsKey(tradestrategy.getContract()
				.getIdContract())) {
			Contract contract = m_marketDataRequests.get(tradestrategy
					.getContract().getIdContract());
			for (Tradestrategy item : contract.getTradestrategies()) {
				if (item.equals(tradestrategy)) {
					return true;
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
			for (Tradestrategy tradestrategy : m_historyDataRequests.values()) {
				this.onCancelBrokerData(tradestrategy);
			}
			for (Contract contract : m_realTimeBarsRequests.values()) {
				this.onCancelRealtimeBars(contract);
			}
			for (Contract contract : m_marketDataRequests.values()) {
				this.onCancelMarketData(contract);
			}
			for (Contract contract : m_contractRequests.values()) {
				this.onCancelContractDetails(contract);
			}
		}
		m_contractRequests.clear();
		m_historyDataRequests.clear();
		m_realTimeBarsRequests.clear();
		m_marketDataRequests.clear();

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
	 * @see org.trade.broker.BrokerModel#onCancelRealtimeBars(Contract)
	 */
	public void onCancelBrokerData(Tradestrategy tradestrategy) {
		synchronized (m_historyDataRequests) {
			if (m_historyDataRequests.containsKey(tradestrategy
					.getIdTradeStrategy())) {
				if (m_client.isConnected())
					m_client.cancelHistoricalData(tradestrategy
							.getIdTradeStrategy());
				m_historyDataRequests
						.remove(tradestrategy.getIdTradeStrategy());
				m_historyDataRequests.notifyAll();
			}
		}
	}

	/**
	 * Method onCancelBrokerData.
	 * 
	 * @param contract
	 *            Contract
	 * @see org.trade.broker.BrokerModel#onCancelRealtimeBars(Contract)
	 */
	public void onCancelBrokerData(Contract contract) {
		synchronized (m_historyDataRequests) {
			for (Tradestrategy tradestrategy : m_historyDataRequests.values()) {
				if (contract.equals(tradestrategy.getContract())) {
					if (m_client.isConnected())
						m_client.cancelHistoricalData(tradestrategy
								.getIdTradeStrategy());
					m_historyDataRequests.remove(tradestrategy
							.getIdTradeStrategy());
					m_historyDataRequests.notifyAll();
				}
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
					onCancelMarketData(contract);
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
				if (m_client.isConnected())
					m_client.cancelRealTimeBars(contract.getIdContract());
				m_realTimeBarsRequests.remove(contract.getIdContract());
				m_realTimeBarsRequests.notifyAll();
			}
		}
	}

	/**
	 * Method onCancelMarketData.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void onCancelMarketData(Tradestrategy tradestrategy) {
		synchronized (m_marketDataRequests) {
			if (m_marketDataRequests.containsKey(tradestrategy.getContract()
					.getIdContract())) {
				Contract contract = m_marketDataRequests.get(tradestrategy
						.getContract().getIdContract());
				for (Tradestrategy item : contract.getTradestrategies()) {
					if (item.equals(tradestrategy)) {
						contract.removeTradestrategy(tradestrategy);
						break;
					}
				}
				if (contract.getTradestrategies().isEmpty()) {
					onCancelMarketData(contract);
				}
			}
		}
	}

	/**
	 * Method onCancelMarketData.
	 * 
	 * @param contract
	 *            Contract
	 * @see org.trade.broker.BrokerModel#onCancelRealtimeBars(Contract)
	 */
	public void onCancelMarketData(Contract contract) {
		synchronized (m_marketDataRequests) {
			if (m_marketDataRequests.containsKey(contract.getIdContract())) {
				if (m_client.isConnected())
					m_client.cancelMktData(contract.getIdContract());
				m_marketDataRequests.remove(contract.getIdContract());
				m_marketDataRequests.notifyAll();
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

					_log.debug("Order Placed Key: " + tradeOrder.getOrderKey());
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
					.findTradeOrderByKey(new Integer(Math
							.abs(execution.m_orderId)));
			if (null == transientInstance) {
				/*
				 * If the executionDetails is null and the order does not exist
				 * then we have made a request for order executions with a
				 * different clientId than the one which created this order.
				 */
				if (null == m_tradePersistentModel
						.findTradeOrderfillByExecId(execution.m_execId)) {
					executionDetails.put(execution.m_execId, execution);
				}
				return;
			}

			/*
			 * We already have this order fill.
			 */

			if (transientInstance.existTradeOrderfill(execution.m_execId))
				return;

			TradeOrderfill tradeOrderfill = new TradeOrderfill();
			TWSBrokerModel.populateTradeOrderfill(execution, tradeOrderfill);
			tradeOrderfill.setTradeOrder(transientInstance);
			transientInstance.addTradeOrderfill(tradeOrderfill);
			transientInstance.setAverageFilledPrice(tradeOrderfill
					.getAveragePrice());
			transientInstance.setFilledQuantity(tradeOrderfill
					.getCumulativeQuantity());
			transientInstance.setFilledDate(tradeOrderfill.getTime());
			boolean isFilled = transientInstance.getIsFilled();
			transientInstance = m_tradePersistentModel
					.persistTradeOrderfill(transientInstance);
			// Let the controller know an order was filled
			if (transientInstance.getIsFilled() && !isFilled)
				this.fireTradeOrderFilled(transientInstance);

			tradeOrdersExecutions.put(transientInstance.getOrderKey(),
					transientInstance);
			_log.error("execDetails tradeOrdersExecutions reqId: " + reqId);

		} catch (Exception ex) {
			error(reqId, 3160, "Errors saving execution: " + ex.getMessage());
		}
	}

	/**
	 * Method execDetailsEnd. Only deal with these when we dont have the
	 * tradeOrder in the TradeManager. Use the permId as orderKey i.e. TWS
	 * internal order id to reconcile orders.
	 * 
	 * @param reqId
	 *            int
	 * @see com.ib.client.EWrapper#execDetailsEnd(int)
	 */
	public void execDetailsEnd(int reqId) {

		try {
			for (Integer key : tradeOrdersExecutions.keySet()) {
				TradeOrder tradeorder = tradeOrdersExecutions.get(key);
				if (tradeorder.getIsFilled()) {
					if (tradeorder.hasTradePosition()
							&& !tradeorder.getTradePosition().isOpen()) {
						// Let the controller know a position was closed
						this.firePositionClosed(tradeorder.getTradePosition());
					}
				}
			}

			if (!executionDetails.isEmpty()) {
				/*
				 * If the tradestrategy exists for this request then we must
				 * create the traderOrders and tradeOrderfills that have been
				 * request and that do not already exist. Note executionDetails
				 * only contains executions for tradeOrders that do not exist.
				 */
				Tradestrategy tradestrategy = m_tradePersistentModel
						.findTradestrategyById(reqId);
				
				/*
				 * Internal created order have Integer.MAX_VALUE or are negative
				 * as their value, so change the m_orderId to nextOrderKey.
				 */
				int nextOrderKey = orderKey.getAndIncrement();
				for (String key : executionDetails.keySet()) {
					Execution execution = executionDetails.get(key);
					if (execution.m_orderId == Integer.MAX_VALUE
							|| execution.m_orderId < 0) {
						execution.m_orderId = nextOrderKey;
					} else {
						continue;
					}
					// Multiple executions for the same order.
					for (String key1 : executionDetails.keySet()) {
						Execution execution1 = executionDetails.get(key1);
						if (execution1.m_permId == execution.m_permId) {
							execution1.m_orderId = nextOrderKey;
						}
					}
					nextOrderKey = orderKey.getAndIncrement();
				}

				/*
				 * Create the tradeOrder for these executions.
				 */
				ConcurrentHashMap<Integer, TradeOrder> tradeOrders = new ConcurrentHashMap<Integer, TradeOrder>();
				for (String key : executionDetails.keySet()) {
					Execution execution = executionDetails.get(key);

					if (tradeOrders.containsKey(execution.m_orderId))
						continue;

					TradeOrderfill tradeOrderfill = new TradeOrderfill();
					TWSBrokerModel.populateTradeOrderfill(execution,
							tradeOrderfill);

					String action = Action.SELL;
					if (Side.BOT.equals(execution.m_side))
						action = Action.BUY;

					Integer quantity = tradeOrderfill.getQuantity();
					TradeOrder tradeOrder = new TradeOrder(tradestrategy,
							action, tradeOrderfill.getTime(), OrderType.MKT,
							quantity, null, null, OverrideConstraints.YES,
							TimeInForce.DAY, TriggerMethod.DEFAULT);
					tradeOrder.setClientId(execution.m_clientId);
					tradeOrder.setPermId(execution.m_permId);
					tradeOrder.setOrderKey(execution.m_orderId);
					for (String key1 : executionDetails.keySet()) {
						Execution execution1 = executionDetails.get(key1);
						if (execution1.m_permId == execution.m_permId
								&& !execution1.m_execId
										.equals(execution.m_execId)) {
							TradeOrderfill tradeOrderfill1 = new TradeOrderfill();
							TWSBrokerModel.populateTradeOrderfill(execution1,
									tradeOrderfill1);
							quantity = quantity + tradeOrderfill1.getQuantity();
							// Make sure the create date for the order is the
							// earliest time.
							if (tradeOrder.getCreateDate().after(
									tradeOrderfill1.getTime()))
								tradeOrder.setCreateDate(tradeOrderfill1
										.getTime());
						}
					}
					tradeOrder.setQuantity(quantity);
					tradeOrders.put(tradeOrder.getOrderKey(), tradeOrder);
				}

				List<TradeOrder> orders = new ArrayList<TradeOrder>();
				for (Integer orderKey : tradeOrders.keySet()) {
					TradeOrder tradeOrder = tradeOrders.get(orderKey);
					orders.add(tradeOrder);
				}
				Collections.sort(orders, TradeOrder.CREATE_ORDER);

				for (TradeOrder tradeOrder : orders) {
					tradeOrder = m_tradePersistentModel
							.persistTradeOrder(tradeOrder);
					double totalComms = 0;
					for (String key : executionDetails.keySet()) {
						Execution execution = executionDetails.get(key);
						if (tradeOrder.getPermId().equals(execution.m_permId)) {
							TradeOrderfill tradeOrderfill = new TradeOrderfill();
							TWSBrokerModel.populateTradeOrderfill(execution,
									tradeOrderfill);
							/*
							 * Commissions are sent through via the
							 * commissionReport call. This happens when an order
							 * is executed or a call to OnReqExecutions.
							 */
							CommissionReport comms = commissionDetails.get(key);

							if (null != comms) {
								totalComms = totalComms + comms.m_commission;
								tradeOrderfill.setCommission(new BigDecimal(
										comms.m_commission));
							}
							tradeOrderfill.setTradeOrder(tradeOrder);
							tradeOrder.addTradeOrderfill(tradeOrderfill);
						}
					}
					tradeOrder.setCommission(new BigDecimal(totalComms));
					tradeOrder = m_tradePersistentModel
							.persistTradeOrderfill(tradeOrder);
					TradeOrder transientInstance = m_tradePersistentModel
							.findTradeOrderByKey(tradeOrder.getOrderKey());
					// Let the controller know an order was filled
					if (tradeOrder.getIsFilled())
						this.fireTradeOrderFilled(transientInstance);
				}
			}

			/*
			 * Let the controller know there are execution details.
			 */
			this.fireExecutionDetailsEnd(tradeOrdersExecutions);

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

			TWSBrokerModel.logOrderState(orderState);
			TWSBrokerModel.logTradeOrder(order);

			TradeOrder transientInstance = m_tradePersistentModel
					.findTradeOrderByKey(new Integer(order.m_orderId));

			if (null == transientInstance) {
				error(orderId, 3170, "Warning Order not found for Order Key: "
						+ order.m_orderId + " make sure Client ID: "
						+ this.m_clientId
						+ " is not the master in TWS. On openOrder update.");
				transientInstance = new TradeOrder();
				transientInstance.setOrderKey(order.m_orderId);
				transientInstance.setCreateDate(TradingCalendar.getDate());
				TWSBrokerModel.updateTradeOrder(order, orderState,
						transientInstance);
				openOrders.put(transientInstance.getOrderKey(),
						transientInstance);
				return;
			}

			/*
			 * Check to see if anything has changed as this method gets fired
			 * twice on order fills.
			 */

			if (TWSBrokerModel.updateTradeOrder(order, orderState,
					transientInstance)) {

				if (OrderStatus.FILLED.equals(transientInstance.getStatus())) {
					_log.debug("Open order filled Order Key:"
							+ transientInstance.getOrderKey());
					transientInstance = m_tradePersistentModel
							.persistTradeOrder(transientInstance);

					if (transientInstance.hasTradePosition()
							&& !transientInstance.getTradePosition().isOpen()) {
						// Let the controller know a position was closed
						this.firePositionClosed(transientInstance
								.getTradePosition());
					}
				} else {
					_log.debug("Open order state changed. Status:"
							+ orderState.m_status);
					transientInstance = m_tradePersistentModel
							.persistTradeOrder(transientInstance);
					if (OrderStatus.CANCELLED.equals(transientInstance
							.getStatus())) {
						// Let the controller know a position was closed
						this.fireTradeOrderCancelled(transientInstance);
					} else {
						this.fireTradeOrderStatusChanged(transientInstance);
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
		_log.debug("openOrderEnd");
		// Let the controller know there are open orders
		for (TradeOrder openOrder : openOrders.values()) {
			_log.debug("openOrderEnd Open Order Key: "
					+ openOrder.getOrderKey() + " Order status: "
					+ openOrder.getStatus());
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
				error(orderId, 3170, "Warning Order not found for Order Key: "
						+ orderId + " make sure Client ID: " + this.m_clientId
						+ " is not the master in TWS. On orderStatus update.");
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
			/*
			 * If filled qty is greater than current filled qty set the new
			 * value.
			 */
			if (CoreUtils.nullSafeComparator(new Integer(filled),
					transientInstance.getFilledQuantity()) == 1) {
				if (filled > 0) {
					transientInstance.setAverageFilledPrice(new BigDecimal(
							avgFillPrice));
					transientInstance.setFilledQuantity(filled);
					changed = true;
				}
			}

			if (changed) {
				transientInstance.setLastUpdateDate(TradingCalendar
						.getDate((new Date()).getTime()));
				transientInstance.setStatus(status.toUpperCase());
				transientInstance.setWhyHeld(whyHeld);
				_log.debug("Order Status changed. Status: " + status);
				TWSBrokerModel.logOrderStatus(orderId, status, filled,
						remaining, avgFillPrice, permId, parentId,
						lastFillPrice, clientId, whyHeld);

				boolean isFilled = transientInstance.getIsFilled();
				transientInstance = m_tradePersistentModel
						.persistTradeOrder(transientInstance);

				if (OrderStatus.CANCELLED.equals(transientInstance.getStatus())) {
					// Let the controller know a position was closed
					this.fireTradeOrderCancelled(transientInstance);
				} else {
					this.fireTradeOrderStatusChanged(transientInstance);
					// Let the controller know an order was filled
					if (transientInstance.getIsFilled() && !isFilled)
						this.fireTradeOrderFilled(transientInstance);
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
		_log.error("BrokerModel error msg: " + ex.getMessage());
		// this.fireBrokerError(new BrokerManagerModelException(ex));
	}

	/**
	 * Method error.
	 * 
	 * @param msg
	 *            String
	 * @see com.ib.client.AnyWrapper#error(String)
	 */
	public void error(String msg) {
		_log.error("BrokerModel error str: " + msg);
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
		if (m_contractRequests.containsKey(id)) {
			symbol = m_contractRequests.get(id).getSymbol();
			synchronized (m_contractRequests) {
				m_contractRequests.remove(id);
				m_contractRequests.notifyAll();
			}
		}
		if (m_historyDataRequests.containsKey(id)) {
			Tradestrategy tradestrategy = m_historyDataRequests.get(id);
			symbol = tradestrategy.getContract().getSymbol();
			if (code == 162) {
				symbol = tradestrategy.getContract().getSymbol()
						+ " pacing violation Tradingday: "
						+ tradestrategy.getTradingday().getOpen()
						+ " BarSize: "
						+ tradestrategy.getBarSize()
						+ " ChartDays: "
						+ tradestrategy.getChartDays()
						+ "  \n"
						+ "The following conditions can cause a pacing violation: \n"
						+ "1/ Making identical historical data requests within 15 seconds. \n"
						+ "2/ Making six or more historical data requests for the same Contract, Exchange and Tick Type within two seconds. \n"
						+ "3/ Making more than 60 historical data requests in any ten-minute period.  \n";
			}
			synchronized (m_historyDataRequests) {
				m_historyDataRequests.remove(id);
				m_historyDataRequests.notifyAll();
			}
		}
		if (m_realTimeBarsRequests.containsKey(id)) {
			symbol = m_realTimeBarsRequests.get(id).getSymbol();
		}
		if (m_marketDataRequests.containsKey(id)) {
			symbol = m_marketDataRequests.get(id).getSymbol();
		}

		/*
		 * Error code 162 (Historical data request pacing violation)and 366 (No
		 * historical data query found for ticker id) are error code for no
		 * market or historical data found.
		 * 
		 * Error code 202, Order cancelled 201, Order rejected
		 * 
		 * Error code 321 Error validating request:-'jd' : cause - FA data
		 * operations ignored for non FA customers.
		 * 
		 * Error code 502, Couldn't connect to TWS. Confirm that API is enabled
		 * in TWS via the Configure>API menu command.
		 */
		String errorMsg = "Req/Order Id: " + id + " Code: " + code
				+ " Symbol: " + symbol + " Msg: " + msg;

		if (((code > 1999) && (code < 3000)) || ((code >= 200) && (code < 299))
				|| (code == 366) || (code == 162) || (code == 321)
				|| (code == 3170)) {

			if (((code > 1999) && (code < 3000))) {
				_log.info(errorMsg);
				brokerModelException = new BrokerModelException(3, code,
						errorMsg);
			} else if (code == 202 || code == 201 || code == 3170) {
				_log.warn(errorMsg);
				brokerModelException = new BrokerModelException(2, code,
						errorMsg);
			} else if (code == 321) {
				_log.info(errorMsg);
				return;
			} else {
				_log.warn(errorMsg);
				brokerModelException = new BrokerModelException(2, code,
						errorMsg);
			}

		} else {
			if (m_realTimeBarsRequests.containsKey(id)) {
				synchronized (m_realTimeBarsRequests) {
					m_realTimeBarsRequests.remove(id);
					m_realTimeBarsRequests.notifyAll();
				}
			}
			if (m_marketDataRequests.containsKey(id)) {
				synchronized (m_marketDataRequests) {
					m_marketDataRequests.remove(id);
					m_marketDataRequests.notifyAll();
				}
			}

			_log.error(errorMsg);
			brokerModelException = new BrokerModelException(1, code, errorMsg);

		}
		this.fireBrokerError(brokerModelException);

		/*
		 * If onConnect() fails error 502 will be fired. This needs to tell the
		 * main controller that we could not connect and so return the app to
		 * test mode.
		 */
		if (502 == code)
			this.fireConnectionClosed(false);
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
	public void tickPrice(int reqId, int field, double value, int canAutoExecute) {

		try {

			BigDecimal price = (new BigDecimal(value)).setScale(SCALE,
					BigDecimal.ROUND_HALF_EVEN);
			synchronized (price) {
				// _log.warn("tickPrice Field: " + field + " value :" + value
				// + " time: " + System.currentTimeMillis());
				if (!m_marketDataRequests.containsKey(new Integer(reqId)))
					return;
				Contract contract = m_marketDataRequests.get(reqId);

				/*
				 * Make sure the lastPrice is between the current Bid/Ask as
				 * prints can come in late in T/S i.e. bad ticks that are
				 * outside the current Bid/Ask.
				 */

				for (Tradestrategy tradestrategy : contract
						.getTradestrategies()) {
					Contract seriesContract = tradestrategy.getStrategyData()
							.getBaseCandleSeries().getContract();

					switch (field) {
					case TickType.ASK: {
						seriesContract.setLastAskPrice(price);
						break;
					}
					case TickType.BID: {
						seriesContract.setLastBidPrice(price);
						break;
					}
					case TickType.LAST: {
						seriesContract.setLastPrice(price);
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
				//
				// for (Tradestrategy tradestrategy : contract
				// .getTradestrategies()) {
				// StrategyData datasetContainer = tradestrategy
				// .getDatasetContainer();
				// synchronized (datasetContainer) {
				// if (datasetContainer.getBaseCandleSeries()
				// .getItemCount() > 0) {
				// CandleItem candle = (CandleItem) datasetContainer
				// .getBaseCandleSeries().getDataItem(
				// datasetContainer
				// .getBaseCandleSeries()
				// .getItemCount() - 1);
				// candle.setVolume(value * 100);
				// candle.setLastUpdateDate(new Date());
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
	public void tickString(int reqId, int field, String value) {

		try {

			/*
			 * 48 = RTVolume String = last trade price;last trade size;last
			 * trade time;total volume;vwap;single trade flag
			 */

			// _log.info("tickString reqId: " + reqId + " field: " + field
			// + " value: " + value);

			synchronized (value) {

				if (!m_marketDataRequests.containsKey(new Integer(reqId)))
					return;

				switch (field) {
				case TickType.RT_VOLUME: {
					/*
					 * If there is no price ignore this value.
					 */
					if (value.startsWith(";"))
						return;

					StringTokenizer st = new StringTokenizer(value, ";");
					int tokenNumber = 0;
					BigDecimal price = new BigDecimal(0);
					Date time = null;
					while (st.hasMoreTokens()) {
						tokenNumber++;
						String token = st.nextToken();
						switch (tokenNumber) {
						case 1: {
							price = (new BigDecimal(Double.parseDouble(token)))
									.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);
							break;
						}
						case 2: {
							_log.debug("TickString Trade Size: "
									+ Integer.parseInt(token));
							break;
						}
						case 3: {
							time = new Date(Long.parseLong(token));
							break;
						}
						case 4: {
							_log.debug("TickString Total Volume: "
									+ Integer.parseInt(token) * 100);
							break;
						}
						case 5: {
							_log.debug("TickString Total Vwap: " + token);
							break;
						}
						case 6: {
							break;
						}
						default: {
							break;
						}
						}
					}
					Contract contract = m_marketDataRequests.get(reqId);
					// _log.warn("TickString ReqId: " + reqId + " Field: "
					// + field + " String: " + value);
					for (Tradestrategy tradestrategy : contract
							.getTradestrategies()) {

						Contract seriesContract = tradestrategy
								.getStrategyData().getBaseCandleSeries()
								.getContract();
						int index = tradestrategy.getStrategyData()
								.getBaseCandleSeries().indexOf(time);
						if (index < 0)
							return;
						CandleItem candleItem = (CandleItem) tradestrategy
								.getStrategyData().getBaseCandleSeries()
								.getDataItem(index);
						if (seriesContract.getLastAskPrice().doubleValue() > 0
								&& seriesContract.getLastBidPrice()
										.doubleValue() > 0
								&& (price.doubleValue() <= seriesContract
										.getLastAskPrice().doubleValue() && price
										.doubleValue() >= seriesContract
										.getLastBidPrice().doubleValue())) {

							if (marketUpdateOnClose) {
								if (price.doubleValue() > 0
										&& (price.doubleValue() != candleItem
												.getClose())) {

									candleItem.setClose(price.doubleValue());
									candleItem.setLastUpdateDate(time);
									/*
									 * Note if you want you can fire the series
									 * change here this will fire runStrategy.
									 * Could cause problems if the method is not
									 * synchronized in the strategy when the
									 * stock is fast running.
									 */
									tradestrategy.getStrategyData()
											.getBaseCandleSeries()
											.fireSeriesChanged();
									// _log.info("TickString Symbol: "
									// + seriesContract.getSymbol()
									// + " Trade Time: " + time
									// + " Price: " + price + " Bid: "
									// + seriesContract.getLastBidPrice()
									// + " Ask: "
									// + seriesContract.getLastAskPrice());
								}
							} else {
								if (price.doubleValue() > 0

										&& (price.doubleValue() > candleItem
												.getHigh() || price
												.doubleValue() < candleItem
												.getLow())) {
									candleItem.setClose(price.doubleValue());
									candleItem.setLastUpdateDate(time);
									/*
									 * Note if you want you can fire the series
									 * change here this will fire runStrategy.
									 * Could cause problems if the method is not
									 * synchronized in the strategy when the
									 * stock is fast running.
									 */
									tradestrategy.getStrategyData()
											.getBaseCandleSeries()
											.fireSeriesChanged();
									// _log.info("TickString Symbol: "
									// + seriesContract.getSymbol()
									// + " Trade Time: " + time
									// + " Price: " + price + " Bid: "
									// + seriesContract.getLastBidPrice()
									// + " Ask: "
									// + seriesContract.getLastAskPrice());
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
		_log.debug("tickOptionComputation:" + reqId);
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
		_log.debug("tickGeneric: " + reqId + " tickType: " + tickType
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
		_log.debug("tickEFP:" + reqId);
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
		_log.debug("updatePortfolio Account#: " + accountNumber + " contract:"
				+ contract.m_symbol + " position:" + position + " marketPrice:"
				+ marketPrice + " marketValue:" + marketValue + " averageCost:"
				+ averageCost + " unrealizedPNL:" + unrealizedPNL
				+ " realizedPNL:" + realizedPNL);
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

			_log.debug("updateAccountValue Account#: " + accountNumber
					+ " Key:" + key + " Value:" + value + " Currency:"
					+ currency);
			if (m_accountRequests.containsKey(accountNumber)) {
				Account account = m_accountRequests.get(accountNumber);
				if (key.equals(TWSBrokerModel.ACCOUNTTYPE)) {
					account.setAccountType(value);
					account.setDirty(true);
				}
				if (account.getCurrency().equals(currency)) {
					if (key.equals(TWSBrokerModel.AVAILABLE_FUNDS)) {
						account.setAvailableFunds(new BigDecimal(value));
						account.setDirty(true);
					}
					if (key.equals(TWSBrokerModel.BUYING_POWER)) {
						account.setBuyingPower(new BigDecimal(value));
						account.setDirty(true);
					}
					if (key.equals(TWSBrokerModel.CASH_BALANCE)) {
						account.setCashBalance(new BigDecimal(value));
						account.setDirty(true);
					}
					if (key.equals(TWSBrokerModel.CURRENCY)) {
						account.setCurrency(value);
						account.setDirty(true);
					}
					if (key.equals(TWSBrokerModel.GROSS_POSITION_VALUE)
							|| key.equals(TWSBrokerModel.STOCK_MKT_VALUE)) {
						account.setGrossPositionValue(new BigDecimal(value));
						account.setDirty(true);
					}
					if (key.equals(TWSBrokerModel.REALIZED_P_L)) {
						account.setRealizedPnL(new BigDecimal(value));
						account.setDirty(true);
					}
					if (key.equals(TWSBrokerModel.UNREALIZED_P_L)) {
						account.setUnrealizedPnL(new BigDecimal(value));
						account.setDirty(true);
					}
				}
			}
		}
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
			_log.debug("updateAccountTime:" + timeStamp);
			for (String accountNumber : m_accountRequests.keySet()) {
				Account account = m_accountRequests.get(accountNumber);
				synchronized (account) {
					/*
					 * Don't use the incoming time stamp as this does not show
					 * seconds just HH:mm format.
					 */
					if (account.isDirty()) {
						account.setLastUpdateDate(new Date());
						account = m_tradePersistentModel.persistAspect(account,
								true);
						m_accountRequests.replace(accountNumber, account);
						this.fireUpdateAccountTime(accountNumber);
					}
				}
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
		_log.debug("accountDownloadEnd: " + accountNumber);
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
			_log.debug("nextValidId: " + orderId);
			int maxKey = m_tradePersistentModel.findTradeOrderByMaxKey();
			if (maxKey < minOrderId) {
				maxKey = minOrderId;
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
					Contract contract = m_contractRequests.get(reqId);
					TWSBrokerModel.logContractDetails(contractDetails);
					if (TWSBrokerModel.populateContract(contractDetails,
							contract)) {
						m_tradePersistentModel.persistContract(contract);
					}
				} else {
					error(reqId, 3220, "Contract details not found for reqId: "
							+ reqId + " Symbol: "
							+ contractDetails.m_summary.m_symbol);
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
		_log.debug("bondContractDetails:" + reqId);
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
			if (m_contractRequests.containsKey(reqId)) {
				m_contractRequests.remove(reqId);
			}
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
		try {
			_log.debug("Managed accounts: " + accountNumbers);
			this.fireManagedAccountsUpdated(accountNumbers);

		} catch (Exception ex) {
			error(0, 3315,
					"Error updating Managed Accounts: " + ex.getMessage());
		} finally {
			/*
			 * Call FA Accounts to see if we are Financial Advisor.
			 */
			onReqFinancialAccount();
		}
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
		ByteArrayInputStream inputSource = null;
		try {
			inputSource = new ByteArrayInputStream(xml.getBytes("utf-8"));
			switch (faDataType) {
			case EClientSocket.ALIASES: {
				_log.debug("Aliases: /n" + xml);
				final TWSAccountAliasRequest request = new TWSAccountAliasRequest();
				final Aspects aspects = (Aspects) request.fromXML(inputSource);
				for (Aspect aspect : aspects.getAspect()) {
					Account item = (Account) aspect;
					Account account = m_tradePersistentModel
							.findAccountByNumber(item.getAccountNumber());
					if (null == account) {
						account = new Account(item.getAccountNumber(),
								item.getAccountNumber(), Currency.USD,
								AccountType.INDIVIDUAL);
					}
					account.setAlias(item.getAlias());
					account.setLastUpdateDate(new Date());
					m_tradePersistentModel.persistAspect(account);
				}
				m_client.requestFA(EClientSocket.GROUPS);
				break;
			}
			case EClientSocket.PROFILES: {
				_log.debug("Profiles: /n" + xml);
				final TWSAllocationRequest request = new TWSAllocationRequest();
				final Aspects aspects = (Aspects) request.fromXML(inputSource);
				for (Aspect aspect : aspects.getAspect()) {
					m_tradePersistentModel.persistPortfolio((Portfolio) aspect);
				}
				this.fireFAAccountsCompleted();
				break;
			}
			case EClientSocket.GROUPS: {
				_log.debug("Groups: /n" + xml);
				final TWSGroupRequest request = new TWSGroupRequest();
				final Aspects aspects = (Aspects) request.fromXML(inputSource);
				for (Aspect aspect : aspects.getAspect()) {
					m_tradePersistentModel.persistPortfolio((Portfolio) aspect);
				}
				m_client.requestFA(EClientSocket.PROFILES);
				break;
			}
			default: {

			}
			}
		} catch (Exception ex) {
			error(faDataType, 3235, ex.getMessage());
		} finally {
			try {
				if (null != inputSource)
					inputSource.close();
			} catch (IOException ex) {
				error(faDataType, 3236, ex.getMessage());
			}
		}
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
		try {
			volume = volume * 100;
			if (m_historyDataRequests.containsKey(reqId)) {
				Tradestrategy tradestrategy = m_historyDataRequests.get(reqId);

				if (dateString.contains("finished-")) {

					CandleSeries candleSeries = tradestrategy.getStrategyData()
							.getBaseCandleSeries();

					_log.debug("HistoricalData complete Req Id: "
							+ reqId
							+ " Symbol: "
							+ tradestrategy.getContract().getSymbol()
							+ " Tradingday: "
							+ tradestrategy.getTradingday().getOpen()
							+ " candles to saved: "
							+ candleSeries.getItemCount()
							+ " Contract Tradestrategies size:: "
							+ tradestrategy.getContract().getTradestrategies()
									.size());

					m_tradePersistentModel.persistCandleSeries(candleSeries);

					/*
					 * The last one has arrived the reqId is the
					 * tradeStrategyId. Remove this from the processing vector.
					 */

					synchronized (m_historyDataRequests) {
						m_historyDataRequests.remove(reqId);
						m_historyDataRequests.notifyAll();
					}

					/*
					 * Check to see if the trading day is today and this
					 * strategy is selected to trade and that the market is open
					 */
					synchronized (tradestrategy.getContract()
							.getTradestrategies()) {

						this.fireHistoricalDataComplete(tradestrategy);
						if (tradestrategy
								.getTradingday()
								.getClose()
								.after(TradingCalendar.getDate((new Date())
										.getTime()))) {
							if (!this.isRealtimeBarsRunning(tradestrategy
									.getContract())) {
								tradestrategy.getContract().addTradestrategy(
										tradestrategy);
								this.onReqRealTimeBars(tradestrategy
										.getContract(), tradestrategy
										.getStrategy().getMarketData());
							} else {
								Contract contract = m_realTimeBarsRequests
										.get(tradestrategy.getContract()
												.getIdContract());
								contract.addTradestrategy(tradestrategy);
							}
						}
					}

				} else {

					Date date = null;
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

					/*
					 * For daily bars set the time to the open time.
					 */
					if (tradestrategy.getBarSize() > 3600) {
						date = TradingCalendar.getSpecificTime(tradestrategy
								.getTradingday().getOpen(), date);
					}
					if (tradestrategy.getTradingday().getClose().after(date)) {

						if (backfillUseRTH == 1
								&& !TradingCalendar.isMarketHours(tradestrategy
										.getTradingday().getOpen(),
										tradestrategy.getTradingday()
												.getClose(), date))
							return;
						BigDecimal price = (new BigDecimal(close)).setScale(
								SCALE, BigDecimal.ROUND_HALF_EVEN);
						tradestrategy.getStrategyData().getBaseCandleSeries()
								.getContract().setLastAskPrice(price);
						tradestrategy.getStrategyData().getBaseCandleSeries()
								.getContract().setLastBidPrice(price);
						tradestrategy.getStrategyData().getBaseCandleSeries()
								.getContract().setLastPrice(price);
						tradestrategy.getStrategyData().buildCandle(date, open,
								high, low, close, volume, vwap, tradeCount, 1,
								null);
					}
				}
			}
		} catch (Exception ex) {
			error(reqId, 3260, ex.getMessage());
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
					boolean updateCandleDB = true;
					for (Tradestrategy tradestrategy : contract
							.getTradestrategies()) {
						StrategyData strategyData = tradestrategy
								.getStrategyData();

						if (TradingCalendar.isMarketHours(tradestrategy
								.getTradingday().getOpen(), tradestrategy
								.getTradingday().getClose(), date)) {

							if (!this.isMarketDataRunning(contract)) {
								BigDecimal price = (new BigDecimal(close))
										.setScale(SCALE,
												BigDecimal.ROUND_HALF_EVEN);
								strategyData.getBaseCandleSeries()
										.getContract().setLastAskPrice(price);
								strategyData.getBaseCandleSeries()
										.getContract().setLastBidPrice(price);
								strategyData.getBaseCandleSeries()
										.getContract().setLastPrice(price);
							}
							Date lastUpdateDate = new Date(
									date.getTime() + 4999);

							strategyData.buildCandle(date, open, high, low,
									close, volume, vwap, tradeCount,
									(tradestrategy.getBarSize() / 5),
									lastUpdateDate);

							if (!strategyData.getBaseCandleSeries().isEmpty()) {
								CandleItem candleItem = (CandleItem) strategyData
										.getBaseCandleSeries().getDataItem(
												strategyData
														.getBaseCandleSeries()
														.getItemCount() - 1);
								if (updateCandleDB) {
									m_tradePersistentModel
											.persistCandle(candleItem
													.getCandle());
									updateCandleDB = false;
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
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
	 * Method commissionReport. This will only ever be called when an execution
	 * occurs or when OnRequestExecutions is called for this clientID.
	 * 
	 * @param commsReport
	 *            com.ib.client.CommissionReport
	 */
	public void commissionReport(CommissionReport commsReport) {

		try {
			TWSBrokerModel.logCommissionReport(commsReport);

			TradeOrderfill transientInstance = m_tradePersistentModel
					.findTradeOrderfillByExecId(commsReport.m_execId);
			if (null != transientInstance) {
				TradeOrder tradeOrder = m_tradePersistentModel
						.findTradeOrderByKey(transientInstance.getTradeOrder()
								.getOrderKey());
				for (TradeOrderfill tradeOrderfill : tradeOrder
						.getTradeOrderfills()) {
					if (tradeOrderfill.getExecId().equals(commsReport.m_execId)) {
						tradeOrderfill.setCommission(new BigDecimal(
								commsReport.m_commission));
						m_tradePersistentModel
								.persistTradeOrderfill(tradeOrderfill
										.getTradeOrder());
						return;
					}
				}

			} else {
				commissionDetails.put(commsReport.m_execId, commsReport);
			}

		} catch (Exception ex) {
			error(1, 3280, "Errors saving execution: " + ex.getMessage());
		}
	}

	/**
	 * Method accountSummary.
	 * 
	 * @param reqId
	 *            int The ID of the data request.
	 * @param account
	 *            String The account ID.
	 * @param tag
	 *            String The tag from the data request. Available tags are:
	 * 
	 *            AccountType TotalCashValue - Total cash including futures pnl
	 *            SettledCash - For cash accounts, this is the same as
	 *            TotalCashValue AccruedCash - Net accrued interest BuyingPower
	 *            - The maximum amount of marginable US stocks the account can
	 *            buy EquityWithLoanValue - Cash + stocks + bonds + mutual funds
	 *            PreviousEquityWithLoanValue GrossPositionValue - The sum of
	 *            the absolute value of all stock and equity option positions
	 *            RegTEquity RegTMargin SMA - Special Memorandum Account
	 *            InitMarginReq MaintMarginReq AvailableFunds ExcessLiquidity
	 *            Cushion - Excess liquidity as a percentage of net liquidation
	 *            value FullInitMarginReq FullMaintMarginReq FullAvailableFunds
	 *            FullExcessLiquidity LookAheadNextChange - Time when look-ahead
	 *            values take effect LookAheadInitMarginReq
	 *            LookAheadMaintMarginReq LookAheadAvailableFunds
	 *            LookAheadExcessLiquidity HighestSeverity - A measure of how
	 *            close the account is to liquidation DayTradesRemaining - The
	 *            Number of Open/Close trades a user could put on before Pattern
	 *            Day Trading is detected. A value of "-1" means that the user
	 *            can put on unlimited day trades. Leverage - GrossPositionValue
	 *            / NetLiquidation
	 * @param value
	 *            String The value of the tag. currency
	 * @param String
	 *            The currency of the tag.
	 */
	public void accountSummary(int arg0, String arg1, String arg2, String arg3,
			String arg4) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method accountSummaryEnd.
	 * 
	 * @param reqId
	 *            integer
	 */
	public void accountSummaryEnd(int reqId) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method position.
	 * 
	 * @param account
	 *            String The account.
	 * @param contract
	 *            Contract This structure contains a full description of the
	 *            contract that was executed.
	 * @param pos
	 *            double The position.
	 */
	public void position(String arg0, com.ib.client.Contract arg1, int arg2,
			double arg3) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method positionEnd.
	 * 
	 */
	public void positionEnd() {
		// TODO Auto-generated method stub

	}

	/**
	 * Method displayGroupList.
	 * 
	 */
	public void displayGroupList(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method displayGroupUpdated.
	 * 
	 */
	public void displayGroupUpdated(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method verifyCompleted.
	 * 
	 */
	public void verifyCompleted(boolean arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method verifyMessageAPI.
	 * 
	 */
	public void verifyMessageAPI(String arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Method validateBrokerData.
	 * 
	 * 1 Y 1 day
	 * 
	 * 6 M 1 day
	 * 
	 * 3 M 1 day
	 * 
	 * 1 M 1 day, 1 hour
	 * 
	 * 1 W 1 day, 1 hour, 30 mins, 15 mins 2 D 1 hour, 30 mins, 15 mins, 3 mins,
	 * 2 mins, 1 min
	 * 
	 * 1 D 1 hour, 30 mins, 15 mins, 5 mins 3 mins, 2 mins, 1 min, 30 secs
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * 
	 * @return boolean
	 * @throws BrokerModelException
	 */

	public boolean validateBrokerData(Tradestrategy tradestrategy)
			throws BrokerModelException {

		boolean valid = true;
		String errorMsg = "Symbol: "
				+ tradestrategy.getContract().getSymbol()
				+ " Bar Size/Chart Days combination was not valid for TWS API, these values have been updated.\n Please validate and save.\n "
				+ "Note Chart Days/BarSize combinations for IB TWS:\n "
				+ "Chart Hist/Bar Size 1 Y/1 day, 6 M/1 day, 3 M/1 day 1 M/(1 day, 1 hour)\n "
				+ "Chart Hist 1 W/ Bar Size(1 day, 1 hour, 30 mins, 15 mins 2 D 1 hour, 30 mins, 15 mins, 3 mins, 2 mins, 1 min)\n "
				+ "Chart Hist 1 D/ Bar Size(1 hour, 30 mins, 15 mins, 5 mins 3 mins, 2 mins, 1 min, 30 secs)\n ";
		if (tradestrategy.getChartDays() > 1
				&& (tradestrategy.getBarSize() < 60)) {
			tradestrategy.setBarSize(60);
			valid = false;
		} else if (tradestrategy.getChartDays() > 7
				&& tradestrategy.getBarSize() < 3600) {
			tradestrategy.setBarSize(3600);
			valid = false;
		} else if (tradestrategy.getChartDays() > 30) {
			tradestrategy.setBarSize(1);
			valid = false;
		}

		if (tradestrategy.getBarSize() == 30
				&& tradestrategy.getChartDays() > 1) {
			tradestrategy.setChartDays(1);
			valid = false;
		} else if (tradestrategy.getBarSize() <= 1800
				&& tradestrategy.getChartDays() > 7) {
			tradestrategy.setChartDays(7);
			valid = false;
		} else if (tradestrategy.getBarSize() == 3600
				&& tradestrategy.getChartDays() > 30) {
			tradestrategy.setChartDays(30);
			valid = false;
		}
		if (!valid) {
			tradestrategy.setDirty(true);
			throw new BrokerModelException(1, 3901, errorMsg);
		}

		return valid;
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
			// ibContract.m_conId = contract.getIdContractIB();
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
				_sdfExpiry.setTimeZone(TimeZone.getTimeZone("GMT"));
				ibContract.m_expiry = _sdfExpiry.format(contract.getExpiry())
						.substring(0, 6);
			}
		}
		if (null != contract.getCurrency()) {
			ibContract.m_currency = contract.getCurrency();
		}

		if (null != contract.getLocalSymbol()) {
			ibContract.m_localSymbol = contract.getLocalSymbol();
		}
		if (null != contract.getSecIdType()) {
			ibContract.m_secIdType = contract.getSecIdType();
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
		if (null != order.getGoodAfterTime()) {
			ibOrder.m_goodAfterTime = _sdfLocal
					.format(order.getGoodAfterTime());
		}
		if (null != order.getGoodTillTime()) {
			ibOrder.m_goodTillDate = _sdfLocal.format(order.getGoodTillTime());
		}
		if (null != order.getOverrideConstraints()) {
			ibOrder.m_overridePercentageConstraints = (order
					.getOverrideConstraints() == 0) ? false : true;
		}
		if (null != order.getAllOrNothing()) {
			ibOrder.m_allOrNone = order.getAllOrNothing();
		}
		if (null != order.getFAProfile()) {
			ibOrder.m_faProfile = order.getFAProfile();
		}
		if (null != order.getFAGroup()) {
			ibOrder.m_faGroup = order.getFAGroup();
		}
		if (null != order.getFAMethod()) {
			ibOrder.m_faMethod = order.getFAMethod();
		}
		if (null != order.getFAPercent()) {
			Percent faPercent = new Percent(order.getFAPercent());
			ibOrder.m_faPercentage = faPercent.getBigDecimalValue().toString();
		}
		if (null != order.getAccountNumber()) {
			ibOrder.m_account = order.getAccountNumber();
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
			if (CoreUtils.nullSafeComparator(order.getAccountNumber(),
					ibOrder.m_account) != 0) {
				order.setAccountNumber(ibOrder.m_account);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getFAGroup(),
					ibOrder.m_faGroup) != 0) {
				order.setFAGroup(ibOrder.m_faGroup);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getFAMethod(),
					ibOrder.m_faMethod) != 0) {
				order.setFAMethod(ibOrder.m_faMethod);
				changed = true;
			}
			Money faPercent = new Money(ibOrder.m_faPercentage);
			if (CoreUtils.nullSafeComparator(order.getFAPercent(),
					faPercent.getBigDecimalValue()) != 0) {
				order.setFAPercent(faPercent.getBigDecimalValue());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getFAProfile(),
					ibOrder.m_faProfile) != 0) {
				order.setFAProfile(ibOrder.m_faProfile);
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
				Date goodAfterTime = _sdfLocal.parse(ibOrder.m_goodAfterTime);
				if (CoreUtils.nullSafeComparator(order.getGoodAfterTime(),
						goodAfterTime) != 0) {
					order.setGoodAfterTime(goodAfterTime);
					changed = true;
				}
			}

			if (null != ibOrder.m_goodTillDate) {
				Date goodTillDate = _sdfLocal.parse(ibOrder.m_goodTillDate);
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
				order.setLastUpdateDate(TradingCalendar.getDate((new Date())
						.getTime()));
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
	public static boolean populateContract(
			com.ib.client.ContractDetails contractDetails,
			Contract transientContract) throws ParseException {

		boolean changed = false;
		/*
		 * For stock the localsymbol must match. For futues they will not e.g
		 * Symbol ES Local will be ES06. TODO Need to find out how to handle
		 * same symbol different local symbols when using exchange SMART.
		 */
		if (CoreUtils.nullSafeComparator(transientContract.getSymbol(),
				contractDetails.m_summary.m_localSymbol) != 0
				&& SECType.STOCK.equals(transientContract.getSecType())) {
			return changed;

		}
		if (CoreUtils.nullSafeComparator(transientContract.getSymbol(),
				contractDetails.m_summary.m_symbol) == 0) {
			if (CoreUtils.nullSafeComparator(
					transientContract.getLocalSymbol(),
					contractDetails.m_summary.m_localSymbol) != 0) {
				transientContract
						.setLocalSymbol(contractDetails.m_summary.m_localSymbol);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getIdContractIB(),
					contractDetails.m_summary.m_conId) != 0) {
				transientContract
						.setIdContractIB(contractDetails.m_summary.m_conId);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getPrimaryExchange(),
					contractDetails.m_summary.m_primaryExch) != 0) {
				transientContract
						.setPrimaryExchange(contractDetails.m_summary.m_primaryExch);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getExchange(),
					contractDetails.m_summary.m_exchange) != 0) {
				transientContract
						.setExchange(contractDetails.m_summary.m_exchange);
				changed = true;
			}
			if (null != contractDetails.m_summary.m_expiry) {
				Date expiryDate = _sdfExpiry
						.parse(contractDetails.m_summary.m_expiry);
				if (CoreUtils.nullSafeComparator(transientContract.getExpiry(),
						expiryDate) != 0) {
					transientContract.setExpiry(expiryDate);
					changed = true;
				}
			}
			if (CoreUtils.nullSafeComparator(transientContract.getSecIdType(),
					contractDetails.m_summary.m_secIdType) != 0) {
				transientContract
						.setSecIdType(contractDetails.m_summary.m_secIdType);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getLongName(),
					contractDetails.m_longName) != 0) {
				transientContract.setLongName(contractDetails.m_longName);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getCurrency(),
					contractDetails.m_summary.m_currency) != 0) {
				transientContract
						.setCurrency(contractDetails.m_summary.m_currency);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getCategory(),
					contractDetails.m_category) != 0) {
				transientContract.setCategory(contractDetails.m_category);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getIndustry(),
					contractDetails.m_industry) != 0) {
				transientContract.setIndustry(contractDetails.m_industry);
				changed = true;
			}
			Money minTick = new Money(contractDetails.m_minTick);
			if (CoreUtils.nullSafeComparator(minTick, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(
							transientContract.getMinTick(),
							minTick.getBigDecimalValue()) != 0) {
				transientContract.setMinTick(minTick.getBigDecimalValue());
				changed = true;
			}
			Money priceMagnifier = new Money(contractDetails.m_priceMagnifier);
			if (CoreUtils.nullSafeComparator(priceMagnifier, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(
							transientContract.getPriceMagnifier(),
							priceMagnifier.getBigDecimalValue()) != 0) {
				transientContract.setPriceMagnifier(priceMagnifier
						.getBigDecimalValue());
				changed = true;
			}

			Money multiplier = new Money(contractDetails.m_summary.m_multiplier);
			if (CoreUtils.nullSafeComparator(multiplier, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(
							transientContract.getPriceMultiplier(),
							multiplier.getBigDecimalValue()) != 0) {
				transientContract.setPriceMultiplier(multiplier
						.getBigDecimalValue());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getSubCategory(),
					contractDetails.m_subcategory) != 0) {
				transientContract.setSubCategory(contractDetails.m_subcategory);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getTradingClass(),
					contractDetails.m_summary.m_tradingClass) != 0) {
				transientContract
						.setTradingClass(contractDetails.m_summary.m_tradingClass);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getComboLegDescription(),
					contractDetails.m_summary.m_comboLegsDescrip) != 0) {
				transientContract
						.setComboLegDescription(contractDetails.m_summary.m_comboLegsDescrip);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getContractMonth(),
					contractDetails.m_contractMonth) != 0) {
				transientContract
						.setContractMonth(contractDetails.m_contractMonth);
				changed = true;
			}
			Money evMultiplier = new Money(contractDetails.m_evMultiplier);
			if (CoreUtils.nullSafeComparator(evMultiplier, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(
							transientContract.getEvMultiplier(),
							evMultiplier.getBigDecimalValue()) != 0) {
				transientContract.setEvMultiplier(evMultiplier
						.getBigDecimalValue());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getEvRule(),
					contractDetails.m_evRule) != 0) {
				transientContract.setEvRule(contractDetails.m_evRule);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract
					.getIncludeExpired(), new Boolean(
					contractDetails.m_summary.m_includeExpired)) != 0) {
				transientContract.setIncludeExpired(new Boolean(
						contractDetails.m_summary.m_includeExpired));
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getLiquidHours(),
					contractDetails.m_liquidHours) != 0) {
				transientContract.setLiquidHours(contractDetails.m_liquidHours);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getMarketName(),
					contractDetails.m_marketName) != 0) {
				transientContract.setMarketName(contractDetails.m_marketName);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getOrderTypes(),
					contractDetails.m_orderTypes) != 0) {
				String orderTypes = "MKT";
				if (contractDetails.m_orderTypes.contains("STP")) {
					orderTypes = orderTypes + ",STP";
					changed = true;
				}
				if (contractDetails.m_orderTypes.contains("STPLMT")) {
					orderTypes = orderTypes + ",STPLMT";
					changed = true;
				}
				if (contractDetails.m_orderTypes.contains("LMT")) {
					orderTypes = orderTypes + ",LMT";
					changed = true;
				}
				transientContract.setOrderTypes(orderTypes);

			}
			if (CoreUtils.nullSafeComparator(transientContract.getSecId(),
					contractDetails.m_summary.m_secId) != 0) {
				transientContract.setSecId(contractDetails.m_summary.m_secId);
				changed = true;
			}
			Money strike = new Money(contractDetails.m_summary.m_strike);
			if (CoreUtils.nullSafeComparator(strike,
					new Money(Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(
							transientContract.getStrike(),
							strike.getBigDecimalValue()) != 0) {
				transientContract.setStrike(strike.getBigDecimalValue());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getTimeZoneId(),
					contractDetails.m_timeZoneId) != 0) {
				transientContract.setTimeZoneId(contractDetails.m_timeZoneId);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getTradingHours(),
					contractDetails.m_tradingHours) != 0) {
				transientContract
						.setTradingHours(contractDetails.m_tradingHours);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getUnderConId(),
					new Integer(contractDetails.m_underConId)) != 0) {
				transientContract.setUnderConId(new Integer(
						contractDetails.m_underConId));
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getValidExchanges(),
					contractDetails.m_validExchanges) != 0) {
				transientContract
						.setValidExchanges(contractDetails.m_validExchanges);
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getOptionType(),
					contractDetails.m_summary.m_right) != 0) {
				transientContract
						.setOptionType(contractDetails.m_summary.m_right);
				changed = true;
			}
		}

		return changed;
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
		tradeOrderfill.setAccountNumber(execution.m_acctNumber);
		tradeOrderfill.setCumulativeQuantity(execution.m_cumQty);
		tradeOrderfill.setExecId(execution.m_execId);
		tradeOrderfill.setOrderReference(execution.m_orderRef);
		tradeOrderfill.setPermId(execution.m_permId);
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
	 * @throws IOException
	 */
	public static com.ib.client.ExecutionFilter getIBExecutionFilter(
			Integer clientId, Date mktOpen, String secType, String symbol)
			throws IOException {

		com.ib.client.ExecutionFilter executionFilter = new com.ib.client.ExecutionFilter();
		if (null != secType)
			executionFilter.m_secType = secType;

		if (null != symbol)
			executionFilter.m_symbol = symbol;
		if (null != mktOpen) {
			TimeZone twsTimeZone = TimeZone.getTimeZone(ConfigProperties
					.getPropAsString("trade.tws.timezone"));
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			sdf.setTimeZone(twsTimeZone);
			executionFilter.m_time = sdf.format(mktOpen);
		}

		if (null != clientId)
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

		_log.debug("OrderKey: " + +order.m_orderId + " ClientId: "
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
				+ order.m_allOrNone + " Account: " + order.m_account
				+ " FAGroup: " + order.m_faGroup + " FAMethod: "
				+ order.m_faMethod + " FAPercent: " + order.m_faPercentage
				+ " FAProfile: " + order.m_faProfile);
	}

	/**
	 * Method logContract.
	 * 
	 * @param contect
	 *            com.ib.client.Contract
	 */
	public static void logContract(com.ib.client.Contract contract) {
		_log.debug("Symbol: " + contract.m_symbol + " Sec Type: "
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
		_log.debug("Symbol: " + contractDetails.m_summary.m_symbol
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
		_log.debug("Status: " + orderState.m_status + " Comms Amt: "
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
		_log.debug("execDetails OrderId: " + execution.m_orderId
				+ " ClientId: " + execution.m_clientId + " PermId: "
				+ execution.m_permId + " ExecId: " + execution.m_execId
				+ " Time: " + execution.m_time + " CumQty: "
				+ execution.m_cumQty);
	}

	/**
	 * Method logCommissionReport.
	 * 
	 * @param commissionReport
	 *            com.ib.client.CommissionReport
	 */
	public static void logCommissionReport(
			com.ib.client.CommissionReport commissionReport) {
		_log.debug("execDetails ExecId: " + commissionReport.m_execId
				+ " Commission: " + commissionReport.m_commission
				+ " Currency: " + commissionReport.m_currency
				+ " RealizedPNL: " + commissionReport.m_realizedPNL
				+ " yieldRedemptionDate: "
				+ commissionReport.m_yieldRedemptionDate + " Yield: "
				+ commissionReport.m_yield);
	}
}
