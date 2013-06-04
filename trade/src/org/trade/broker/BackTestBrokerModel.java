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
import java.util.Date;
import java.util.ListIterator;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.client.BackTestBroker;
import org.trade.broker.client.ClientSocket;
import org.trade.broker.client.ClientWrapper;
import org.trade.broker.client.OrderState;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.strategy.data.CandleSeries;

import com.ib.client.ContractDetails;

/**
 */
public class BackTestBrokerModel extends AbstractBrokerModel implements
		ClientWrapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3191422640254347940L;

	private final static Logger _log = LoggerFactory
			.getLogger(BackTestBrokerModel.class);

	// Candle series this is listened to by the chart panel and main controller
	// for updates.
	private static final ConcurrentHashMap<Integer, Contract> m_historyDataRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_realTimeBarsRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_contractRequests = new ConcurrentHashMap<Integer, Contract>();
	private PersistentModel m_tradePersistentModel = null;

	private ClientSocket m_client = null;

	private static final int SCALE = 5;

	private AtomicInteger orderKey = null;

	private Integer backfillDateFormat = 2;
	private String backfillWhatToShow;
	private Integer backfillOffsetDays = 0;
	private Integer backfillUseRTH = 1;

	private static final SimpleDateFormat m_sdfGMT = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss z");

	public BackTestBrokerModel() {

		try {
			m_client = new ClientSocket(this);
			m_tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			backfillWhatToShow = ConfigProperties
					.getPropAsString("trade.backfill.whatToShow");
			backfillUseRTH = ConfigProperties
					.getPropAsInt("trade.backfill.useRTH");
			int maxKey = m_tradePersistentModel.findTradeOrderByMaxKey();
			if (maxKey < 100000) {
				maxKey = 100000;
			}
			orderKey = new AtomicInteger(maxKey + 1);
		} catch (Exception ex) {
			throw new IllegalArgumentException(
					"Error initializing BrokerModel Msg: " + ex.getMessage());
		}
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
	 * Method isConnected.
	 * 
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isConnected()
	 */
	public boolean isConnected() {
		return false;
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

	}

	/**
	 * Method connectionClosed.
	 * 
	 * @see com.ib.client.AnyWrapper#connectionClosed()
	 */
	public void connectionClosed() {

		onCancelAllRealtimeData();
		this.fireConnectionClosed(true);
		error(0, 1101, "Error Connection was closed! ");
	}

	/**
	 * Method disconnect.
	 * 
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#disconnect()
	 */
	public void onDisconnect() {
		if (isConnected()) {
			onCancelAllRealtimeData();
		}
	}

	/**
	 * Method getBackTestBroker.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * @see org.trade.broker.BrokerModel#getBackTestBroker(Integer)
	 */
	public BackTestBroker getBackTestBroker(Integer idTradestrategy) {
		return (BackTestBroker) m_client.getBackTestBroker(idTradestrategy);
	}

	/**
	 * Method getNextRequestId.
	 * 
	 * @return Integer
	 * @see org.trade.broker.BrokerModel#getNextRequestId()
	 */
	public Integer getNextRequestId() {
		return new Integer(orderKey.incrementAndGet());
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
	 * Method onSubscribeAccountUpdates.
	 * 
	 * @param subscribe
	 *            boolean
	 * @param account
	 *            Account
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onSubscribeAccountUpdates(boolean,
	 *      account)
	 */
	public void onSubscribeAccountUpdates(boolean subscribe,
			String accountNumber) throws BrokerModelException {
	}

	/**
	 * Method onCancelAccountUpdates.
	 * 
	 * @param accountNumber
	 *            String
	 * @see org.trade.broker.BrokerModel#onCancelAccountUpdates(String)
	 */
	public void onCancelAccountUpdates(String accountNumber) {
	}

	/**
	 * Method onReqFinancialAccount.
	 * 
	 * @see org.trade.broker.onReqFinancialAccount()
	 */
	public void onReqFinancialAccount() {
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
	}

	/**
	 * Method onReqManagedAccount.
	 * 
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#onReqManagedAccount()
	 */
	public void onReqManagedAccount() throws BrokerModelException {
	}

	/**
	 * Method onReqAllOpenOrders.
	 * 
	 * @see org.trade.broker.BrokerModel#onReqAllOpenOrders()
	 */
	public void onReqAllOpenOrders() {
		// request list of all open orders
		// m_client.reqAllOpenOrders();
	}

	/**
	 * Method onReqOpenOrders.
	 * 
	 * @see org.trade.broker.BrokerModel#onReqOpenOrders()
	 */
	public void onReqOpenOrders() {
		// request list of all open orders
		// m_client.reqOpenOrders();
	}

	/**
	 * Method onReqExecutions.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.broker.BrokerModel#onReqExecutions(Tradestrategy)
	 */
	public void onReqExecutions(Tradestrategy tradestrategy) {

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
	}

	/**
	 * Method onReqRealTimeBars.
	 * 
	 * @param contract
	 *            Contract
	 * @throws BrokerModelException
	 */
	public void onReqRealTimeBars(Contract contract, boolean mktData)
			throws BrokerModelException {
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
			if (this.isHistoricalDataRunning(contract)) {
				throw new BrokerModelException(contract.getIdContract(), 3010,
						"Data request is already in progress for: "
								+ contract.getSymbol()
								+ " Please wait or cancel.");
			}
			synchronized (m_historyDataRequests) {
				m_historyDataRequests.put(contract.getIdContract(), contract);
			}
			if (this.isBrokerDataOnly()) {
				/*
				 * This will use the Yahoo API to get the data.
				 */
				synchronized (m_contractRequests) {
					m_contractRequests.put(contract.getIdContract(), contract);
				}
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

				m_client.reqHistoricalData(contract.getIdContract(), contract,
						endDateTime, ChartDays.newInstance(chartDays)
								.getDisplayName(), BarSize.newInstance(barSize)
								.getDisplayName(), backfillWhatToShow,
						backfillUseRTH, backfillDateFormat);
			} else {
				m_client.reqHistoricalData(contract.getIdContract(), contract,
						null,
						ChartDays.newInstance(chartDays).getDisplayName(),
						BarSize.newInstance(barSize).getDisplayName(),
						backfillWhatToShow, backfillUseRTH, backfillDateFormat);
			}

		} catch (Throwable ex) {
			throw new BrokerModelException(contract.getIdContract(), 3020,
					"Error broker data Symbol: " + contract.getSymbol()
							+ " Msg: " + ex.getMessage());
		}
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
		if (m_historyDataRequests.containsKey(contract.getIdContract())) {
			return true;
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
		if (m_realTimeBarsRequests.containsKey(contract.getIdContract())) {
			return true;
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
		return false;
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
		return false;
	}

	/**
	 * Method onCancelAllRealtimeData.
	 * 
	 * @see org.trade.broker.BrokerModel#onCancelAllRealtimeData()
	 */
	public void onCancelAllRealtimeData() {
		m_historyDataRequests.clear();
		m_realTimeBarsRequests.clear();
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
	}

	/**
	 * Method onCancelContractDetails.
	 * 
	 * @param contract
	 *            Contract
	 * @see org.trade.broker.BrokerModel#onCancelContractDetails(Contract)
	 */
	public void onCancelContractDetails(Contract contract) {
	}

	/**
	 * Method onCancelRealtimeBars.
	 * 
	 * @param contract
	 *            Contract
	 * @see org.trade.broker.BrokerModel#onCancelRealtimeBars(Contract)
	 */
	public void onCancelBrokerData(Contract contract) {
		synchronized (m_historyDataRequests) {
			if (m_historyDataRequests.containsKey(contract.getIdContract())) {
				m_historyDataRequests.remove(contract.getIdContract());
				m_historyDataRequests.notifyAll();
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
				contract.removeTradestrategy(tradestrategy);
				if (contract.getTradestrategies().isEmpty())
					onCancelBrokerData(contract);
			}
		}
		m_client.removeBackTestBroker(tradestrategy.getIdTradeStrategy());
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
				if (contract.getTradestrategies().isEmpty())
					onCancelRealtimeBars(contract);
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
	}

	/**
	 * Method onCancelMarketData.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void onCancelMarketData(Tradestrategy tradestrategy) {
	}

	/**
	 * Method onPlaceOrd
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
			synchronized (tradeOrder) {
				if (null == tradeOrder.getOrderKey()) {
					tradeOrder.setOrderKey(getNextRequestId());
				}
				if (null == tradeOrder.getClientId()) {
					tradeOrder.setClientId(999);
				}
				tradeOrder = m_tradePersistentModel
						.persistTradeOrder(tradeOrder);
				// Debug logging
				_log.info("Order Placed Key: " + tradeOrder.getOrderKey());
				TWSBrokerModel.logContract(TWSBrokerModel
						.getIBContract(contract));
				TWSBrokerModel.logTradeOrder(TWSBrokerModel
						.getIBOrder(tradeOrder));

				/*
				 * Call to broker interface should be next
				 */
			}
			return tradeOrder;
		} catch (Exception ex) {
			throw new BrokerModelException(tradeOrder.getOrderKey(), 3030,
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
		try {

			OrderState orderState = new OrderState();
			orderState.m_status = OrderStatus.CANCELLED;
			openOrder(tradeOrder.getOrderKey(), null, tradeOrder, orderState);
		} catch (Exception ex) {
			throw new BrokerModelException(tradeOrder.getOrderKey(), 3040,
					"Could not CancelOrder: " + ex.getMessage());
		}
	}

	/**
	 * Method execDetails.
	 * 
	 * When orders are filled the the exceDetails is fired followed by
	 * openOrder() and orderStatus() the order methods fire twice. openOrder
	 * gives us the commission amount on the second fire and order status from
	 * both. Apart from that I have no idea why they fire twice. I assume its to
	 * do with the margin and account updates.
	 * 
	 * @param reqId
	 *            int
	 * @param contractIB
	 *            com.ib.client.Contract
	 * @param execution
	 *            Execution
	 * @see http://www.interactivebrokers.com/php/apiUsersGuide/apiguide.htm
	 */
	public void execDetails(int reqId, Contract contractIB,
			TradeOrderfill execution) {
		try {

			BackTestBrokerModel.logExecution(execution);

			TradeOrder transientInstance = m_tradePersistentModel
					.findTradeOrderByKey(execution.getTradeOrder()
							.getOrderKey());
			if (null == transientInstance) {
				error(execution.getTradeOrder().getOrderKey(),
						3170,
						"Warning Order not found for Order Key: "
								+ execution.getTradeOrder().getOrderKey()
								+ " make sure Client ID: "
								+ 0
								+ " is not the master in TWS. On execDetails update.");
				return;
			}

			/*
			 * We already have this order fill.
			 */
			if (transientInstance.existTradeOrderfill(execution.getExecId()))
				return;

			TradeOrderfill tradeOrderfill = new TradeOrderfill();
			BackTestBrokerModel.populateTradeOrderfill(execution,
					tradeOrderfill);
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

		} catch (Exception ex) {
			error(reqId, 3160, "Errors saving execution: " + ex.getMessage());
		}
	}

	/**
	 * Method execDetailsEnd.
	 * 
	 * @param reqId
	 *            int
	 */
	public void execDetailsEnd(int reqId) {

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
	public void openOrder(int orderId, Contract contract, TradeOrder order,
			OrderState orderState) {

		try {

			TradeOrder transientInstance = m_tradePersistentModel
					.findTradeOrderByKey(order.getOrderKey());
			if (null == transientInstance) {
				error(orderId, 3170, "Warning Order not found for Order Key: "
						+ orderId + " make sure Client ID: " + 0
						+ " is not the master in TWS. On openOrder update.");
				return;
			}

			/*
			 * Check to see if anything has changed as this method gets fired
			 * twice on order fills.
			 */

			if (BackTestBrokerModel.updateTradeOrder(order, orderState,
					transientInstance)) {

				if (OrderStatus.FILLED.equals(transientInstance.getStatus())) {

					_log.info("Order Key: " + transientInstance.getOrderKey()
							+ " filled.");
					BackTestBrokerModel.logOrderState(orderState);
					BackTestBrokerModel.logTradeOrder(order);

					transientInstance = m_tradePersistentModel
							.persistTradeOrder(transientInstance);

					if (transientInstance.hasTradePosition()
							&& !transientInstance.getTradePosition()
									.getIsOpen()) {
						// Let the controller know a position was closed
						this.firePositionClosed(transientInstance
								.getTradePosition());
					}
				} else {
					_log.info("Order key: " + transientInstance.getOrderKey()
							+ " state changed. Status:" + orderState.m_status);
					BackTestBrokerModel.logOrderState(orderState);
					BackTestBrokerModel.logTradeOrder(order);
					transientInstance = m_tradePersistentModel
							.persistTradeOrder(transientInstance);
					if (OrderStatus.CANCELLED.equals(transientInstance
							.getStatus())) {
						// Let the controller know a position was closed
						this.fireTradeOrderCancelled(transientInstance);
					}
				}
			}
		} catch (Exception ex) {
			error(orderId, 3180,
					"Errors updating open order: " + ex.getMessage());
		}
	}

	public void openOrderEnd() {
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
	 * 
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
						+ orderId + " make sure Client ID: " + 0
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
				transientInstance.setStatus(status.toUpperCase());
				transientInstance.setWhyHeld(whyHeld);
				_log.info("Order Status changed. Status: " + status);
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
			error(orderId, 3100,
					"Errors updating open order status: " + ex.getMessage());
		}
	}

	/**
	 * Method error.
	 * 
	 * @param e
	 *            Exception
	 */
	public void error(Exception e) {
		_log.error("BrokerModel error ex: " + e.getMessage());
		// this.fireBrokerError(new BrokerManagerModelException(e));
	}

	/**
	 * Method error.
	 * 
	 * @param str
	 *            String
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
	 * @see org.trade.broker.BrokerModel#error(int, int, String)
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
			symbol = m_historyDataRequests.get(id).getSymbol();
			synchronized (m_historyDataRequests) {
				m_historyDataRequests.remove(id);
				m_historyDataRequests.notifyAll();
			}
		}
		if (m_realTimeBarsRequests.containsKey(id)) {
			symbol = m_realTimeBarsRequests.get(id).getSymbol();
		}

		/*
		 * Error code 162 (Historical data request pacing violation)and 366 (No
		 * historical data query found for ticker id) are error code for no
		 * market or historical data found.
		 * 
		 * Error code 202, Order rejected 201 Order cancelled
		 * 
		 * Error code 321 Error validating request:-'jd' : cause - FA data
		 * operations ignored for non FA customers.
		 * 
		 * Error code 502, Couldn't connect to TWS. Confirm that API is enabled
		 * in TWS via the Configure>API menu command.
		 */
		if (((code > 1999) && (code < 3000)) || ((code >= 200) && (code < 299))
				|| (code == 366) || (code == 162) || (code == 321)
				|| (code == 3170)) {
			if (((code > 1999) && (code < 3000))) {
				_log.info("BrokerModel Req Id: " + id + " Code: " + code
						+ " Msg: " + msg);
				brokerModelException = new BrokerModelException(3, code,
						"Code: " + code + " " + msg);
			} else if (code == 202 || code == 201 || code == 3170) {
				_log.warn("BrokerModel Order Id: " + id + " Code: " + code
						+ " Msg: " + msg);
				brokerModelException = new BrokerModelException(2, code,
						"Order Id: " + id + " Code: " + code + " " + msg);
			} else if (code == 321) {
				_log.info("BrokerModel Req Id: " + id + " Code: " + code
						+ " Msg: " + msg);
				return;
			} else {
				_log.warn("BrokerModel symbol: " + symbol + " Req Id: " + id
						+ " Code: " + code + " Msg: " + msg);
				brokerModelException = new BrokerModelException(2, code,
						"Req Id: " + id + " Code: " + code + " Symbol: "
								+ symbol + " " + msg);
			}

		} else {
			if (m_realTimeBarsRequests.containsKey(id)) {
				synchronized (m_realTimeBarsRequests) {
					m_realTimeBarsRequests.remove(id);
					m_realTimeBarsRequests.notifyAll();
				}
			}
			/*
			 * Error code 502, Couldn't connect to TWS. Confirm that API is
			 * enabled in TWS via the Configure>API menu command.
			 */
			if (code == 502)
				this.fireConnectionClosed(false);
			_log.error("BrokerModel symbol: " + symbol + " Req Id: " + id
					+ " Code: " + code + " Msg: " + msg);

			brokerModelException = new BrokerModelException(1, code, "Req Id: "
					+ id + " Error Code: " + code + " Symbol: " + symbol + " "
					+ msg);
		}
		this.fireBrokerError(brokerModelException);
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
	public void contractDetails(int reqId, Contract contractDetails) {
		try {
			synchronized (m_contractRequests) {
				if (m_contractRequests.containsKey(reqId)) {
					Contract transientInstance = m_contractRequests.get(reqId);
					if (null != transientInstance.getIdContract())
						transientInstance = m_tradePersistentModel
								.findContractById(transientInstance
										.getIdContract());
					BackTestBrokerModel.logContract(contractDetails);
					if (BackTestBrokerModel.populateContract(contractDetails,
							transientInstance)) {
						m_tradePersistentModel
								.persistContract(transientInstance);
						m_contractRequests.remove(reqId);
					}
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
	 * Method contractDetailsEnd.
	 * 
	 * @param reqId
	 *            int
	 */
	public void contractDetailsEnd(int reqId) {
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
	 */
	public void historicalData(int reqId, String dateString, double open,
			double high, double low, double close, int volume, int tradeCount,
			double vwap, boolean hasGaps) {
		try {
			volume = volume * 100;
			/*
			 * Check to see if the trading day is today and this strategy is
			 * selected to trade and that the market is open
			 */
			if (m_historyDataRequests.containsKey(reqId)) {
				Contract contract = m_historyDataRequests.get(reqId);
				if (dateString.contains("finished-")) {

					/*
					 * The last one has arrived the reqId is the
					 * tradeStrategyId. Remove this from the processing vector.
					 */
					Tradestrategy tradestrategy = contract.getTradestrategies()
							.get(0);
					CandleSeries candleSeries = tradestrategy
							.getDatasetContainer().getBaseCandleSeries();
					m_tradePersistentModel.persistCandleSeries(candleSeries);

					_log.info("HistoricalData complete Req Id: " + reqId
							+ " Symbol: " + contract.getSymbol()
							+ " Tradingday: "
							+ tradestrategy.getTradingday().getOpen()
							+ " candles to saved: "
							+ candleSeries.getItemCount()
							+ " Contract Tradestrategies size:: "
							+ contract.getTradestrategies().size());

					/*
					 * Check to see if the trading day is today and this
					 * strategy is selected to trade and that the market is open
					 */
					synchronized (contract.getTradestrategies()) {
						for (ListIterator<Tradestrategy> iterItem = contract
								.getTradestrategies().listIterator(); iterItem
								.hasNext();) {
							Tradestrategy item = iterItem.next();
							if (item.getTrade() && !this.isBrokerDataOnly()) {
								this.fireHistoricalDataComplete(item);
							} else {
								if (iterItem.equals(tradestrategy))
									iterItem.remove();
							}
						}
					}
					if (contract.getTradestrategies().isEmpty()) {
						synchronized (m_historyDataRequests) {
							m_historyDataRequests.remove(reqId);
							m_historyDataRequests.notifyAll();
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

					for (Tradestrategy tradestrategy : contract
							.getTradestrategies()) {
						/*
						 * For daily bars set the time to the open time.
						 */
						if (tradestrategy.getBarSize() > 3600) {
							date = TradingCalendar.getSpecificTime(
									tradestrategy.getTradingday().getOpen(),
									date);
						}
						if (tradestrategy.getTradingday().getClose()
								.after(date)) {
							if (backfillUseRTH == 1
									&& !TradingCalendar.isMarketHours(
											tradestrategy.getTradingday()
													.getOpen(),
											tradestrategy.getTradingday()
													.getClose(), date))
								return;

							tradestrategy.getDatasetContainer().buildCandle(
									date, open, high, low, close, volume, vwap,
									tradeCount, 1);
							BigDecimal price = (new BigDecimal(close))
									.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);
							tradestrategy.getDatasetContainer()
									.getBaseCandleSeries().getContract()
									.setLastAskPrice(price);
							tradestrategy.getDatasetContainer()
									.getBaseCandleSeries().getContract()
									.setLastBidPrice(price);
							tradestrategy.getDatasetContainer()
									.getBaseCandleSeries().getContract()
									.setLastPrice(price);
						}
					}
				}
			}
		} catch (Exception ex) {
			error(reqId, 3260, ex.getMessage());
		}
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
	 */
	public synchronized void realtimeBar(int reqId, long time, double open,
			double high, double low, double close, long volume, double vwap,
			int tradeCount) {
	}

	/**
	 * Method logOrderState.
	 * 
	 * @param orderState
	 *            OrderState
	 */
	public static void logOrderState(OrderState orderState) {
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
	 * Method updateTradeOrder.
	 * 
	 * @param ibOrder
	 *            com.ib.client.Order
	 * @param ibOrderState
	 *            OrderState
	 * @param order
	 *            TradeOrder
	 * @return boolean
	 * @throws ParseException
	 */
	public static boolean updateTradeOrder(TradeOrder clientOrder,
			OrderState clientOrderState, TradeOrder order)
			throws ParseException {

		boolean changed = false;
		m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));

		if (CoreUtils.nullSafeComparator(order.getOrderKey(),
				clientOrder.getOrderKey()) == 0) {
			if (CoreUtils.nullSafeComparator(order.getStatus(),
					clientOrderState.m_status.toUpperCase()) != 0) {
				order.setStatus(clientOrderState.m_status.toUpperCase());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getWarningMessage(),
					clientOrderState.m_warningText) != 0) {
				order.setWarningMessage(clientOrderState.m_warningText);
				changed = true;
			}
			Money comms = new Money(clientOrderState.m_commission);
			if (CoreUtils
					.nullSafeComparator(comms, new Money(Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(order.getCommission(),
							comms.getBigDecimalValue()) != 0) {
				order.setCommission(comms.getBigDecimalValue());
				changed = true;

			}
			if (CoreUtils.nullSafeComparator(order.getClientId(),
					clientOrder.getClientId()) != 0) {
				order.setClientId(clientOrder.getClientId());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getAction(),
					clientOrder.getAction()) != 0) {
				order.setAction(clientOrder.getAction());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getQuantity(),
					clientOrder.getQuantity()) != 0) {
				order.setQuantity(clientOrder.getQuantity());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOrderType(),
					clientOrder.getOrderType()) != 0) {
				order.setOrderType(clientOrder.getOrderType());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					new Money(clientOrder.getLimitPrice()), new Money(
							Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(order.getLimitPrice(),
							clientOrder.getLimitPrice()) != 0) {
				order.setLimitPrice(clientOrder.getLimitPrice());
				changed = true;
			}

			if (CoreUtils.nullSafeComparator(
					new Money(clientOrder.getAuxPrice()), new Money(
							Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(order.getAuxPrice(),
							clientOrder.getAuxPrice()) != 0) {
				order.setAuxPrice(clientOrder.getAuxPrice());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getTimeInForce(),
					clientOrder.getTimeInForce()) != 0) {
				order.setTimeInForce(clientOrder.getTimeInForce());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOcaGroupName(),
					clientOrder.getOcaGroupName()) != 0) {
				order.setOcaGroupName(clientOrder.getOcaGroupName());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOcaType(),
					clientOrder.getOcaType()) != 0) {
				order.setOcaType(clientOrder.getOcaType());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getOrderReference(),
					clientOrder.getOrderReference()) != 0) {
				order.setOrderReference(clientOrder.getOrderReference());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getPermId(),
					clientOrder.getPermId()) != 0) {
				order.setPermId(clientOrder.getPermId());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getParentId(),
					clientOrder.getParentId()) != 0) {
				order.setParentId(clientOrder.getParentId());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getTransmit(),
					clientOrder.getTransmit()) != 0) {
				order.setTransmit(clientOrder.getTransmit());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getDisplayQuantity(),
					clientOrder.getDisplayQuantity()) != 0) {
				order.setDisplayQuantity(clientOrder.getDisplayQuantity());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getTriggerMethod(),
					clientOrder.getTriggerMethod()) != 0) {
				order.setTriggerMethod(clientOrder.getTriggerMethod());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getHidden(),
					clientOrder.getHidden()) != 0) {
				order.setHidden(clientOrder.getHidden());
				changed = true;
			}
			if (null != clientOrder.getGoodAfterTime()) {
				if (CoreUtils.nullSafeComparator(order.getGoodAfterTime(),
						clientOrder.getGoodAfterTime()) != 0) {
					order.setGoodAfterTime(clientOrder.getGoodAfterTime());
					changed = true;
				}
			}

			if (null != clientOrder.getGoodTillTime()) {
				if (CoreUtils.nullSafeComparator(order.getGoodTillTime(),
						clientOrder.getGoodTillTime()) != 0) {
					order.setGoodTillTime(clientOrder.getGoodTillTime());
					changed = true;
				}
			}

			if (CoreUtils.nullSafeComparator(order.getOverrideConstraints(),
					clientOrder.getOverrideConstraints()) != 0) {
				order.setOverrideConstraints(clientOrder
						.getOverrideConstraints());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(order.getAllOrNothing(),
					clientOrder.getAllOrNothing()) != 0) {
				order.setAllOrNothing(clientOrder.getAllOrNothing());
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
	public static boolean populateContract(Contract contractDetails,
			Contract transientContract) throws ParseException {

		boolean changed = false;
		if (CoreUtils.nullSafeComparator(transientContract.getSymbol(),
				contractDetails.getSymbol()) == 0) {
			if (CoreUtils.nullSafeComparator(
					transientContract.getLocalSymbol(),
					contractDetails.getLocalSymbol()) != 0) {
				transientContract.setLocalSymbol(contractDetails
						.getLocalSymbol());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getIdContractIB(),
					contractDetails.getIdContractIB()) != 0) {
				transientContract.setIdContractIB(contractDetails
						.getIdContractIB());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getPrimaryExchange(),
					contractDetails.getPrimaryExchange()) != 0) {
				transientContract.setPrimaryExchange(contractDetails
						.getPrimaryExchange());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getExchange(),
					contractDetails.getExchange()) != 0) {
				transientContract.setExchange(contractDetails.getExchange());
				changed = true;
			}
			if (null != contractDetails.getExpiry()) {
				if (CoreUtils.nullSafeComparator(transientContract.getExpiry(),
						contractDetails.getExpiry()) != 0) {
					transientContract.setExpiry(contractDetails.getExpiry());
					changed = true;
				}
			}
			if (CoreUtils.nullSafeComparator(transientContract.getSecTypeId(),
					contractDetails.getSecTypeId()) != 0) {
				transientContract.setSecTypeId(contractDetails.getSecTypeId());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getDescription(),
					contractDetails.getDescription()) != 0) {
				transientContract.setDescription(contractDetails
						.getDescription());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getCurrency(),
					contractDetails.getCurrency()) != 0) {
				transientContract.setCurrency(contractDetails.getCurrency());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getCategory(),
					contractDetails.getCategory()) != 0) {
				transientContract.setCategory(contractDetails.getCategory());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(transientContract.getIndustry(),
					contractDetails.getIndustry()) != 0) {
				transientContract.setIndustry(contractDetails.getIndustry());
				changed = true;
			}
			Money minTick = new Money(contractDetails.getMinTick());
			if (CoreUtils.nullSafeComparator(minTick, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(
							transientContract.getMinTick(),
							minTick.getBigDecimalValue()) != 0) {
				transientContract.setMinTick(minTick.getBigDecimalValue());
				changed = true;
			}
			Money priceMagnifier = new Money(
					contractDetails.getPriceMagnifier());
			if (CoreUtils.nullSafeComparator(priceMagnifier, new Money(
					Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(
							transientContract.getPriceMagnifier(),
							priceMagnifier.getBigDecimalValue()) != 0) {
				transientContract.setPriceMagnifier(priceMagnifier
						.getBigDecimalValue());
				changed = true;
			}

			Money multiplier = new Money(contractDetails.getPriceMultiplier());
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
					contractDetails.getSubCategory()) != 0) {
				transientContract.setSubCategory(contractDetails
						.getSubCategory());
				changed = true;
			}
			if (CoreUtils.nullSafeComparator(
					transientContract.getTradingClass(),
					contractDetails.getTradingClass()) != 0) {
				transientContract.setTradingClass(contractDetails
						.getTradingClass());
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
	public static void populateTradeOrderfill(TradeOrderfill execution,
			TradeOrderfill tradeOrderfill) throws ParseException, IOException {

		tradeOrderfill.setTime(execution.getTime());
		tradeOrderfill.setExchange(execution.getExchange());
		tradeOrderfill.setSide(execution.getSide());
		tradeOrderfill.setQuantity(execution.getQuantity());
		tradeOrderfill.setPrice(execution.getPrice());
		tradeOrderfill.setAveragePrice(execution.getAveragePrice());
		tradeOrderfill.setCumulativeQuantity(execution.getCumulativeQuantity());
		tradeOrderfill.setExecId(execution.getExecId());
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
	 *            TradeOrder
	 */
	public static void logTradeOrder(TradeOrder order) {

		_log.info("OrderKey: " + +order.getOrderKey() + " ClientId: "
				+ order.getClientId() + " PermId: " + order.getPermId()
				+ " Action: " + order.getAction() + " TotalQuantity: "
				+ order.getQuantity() + " OrderType: " + order.getOrderType()
				+ " LmtPrice: " + order.getLimitPrice() + " AuxPrice: "
				+ order.getAuxPrice() + " Tif: " + order.getTimeInForce()
				+ " OcaGroup: " + order.getOcaGroupName() + " OcaType: "
				+ order.getOcaType() + " OrderRef: "
				+ order.getOrderReference() + " Transmit: "
				+ order.getTransmit() + " DisplaySize: "
				+ order.getDisplayQuantity() + " TriggerMethod: "
				+ order.getTriggerMethod() + " Hidden: " + order.getHidden()
				+ " ParentId: " + order.getParentId() + " GoodAfterTime: "
				+ order.getGoodAfterTime() + " GoodTillDate: "
				+ order.getGoodTillTime() + " OverridePercentageConstraints: "
				+ order.getOverrideConstraints() + " AllOrNone: "
				+ order.getAllOrNothing());
	}

	/**
	 * Method logContract.
	 * 
	 * @param contect
	 *            com.ib.client.Contract
	 */
	public static void logContract(Contract contract) {
		_log.info("Symbol: " + contract.getSymbol() + " Sec Type: "
				+ contract.getSecType() + " Exchange: "
				+ contract.getExchange() + " Con Id: "
				+ contract.getIdContractIB() + " Currency: "
				+ contract.getCurrency() + " SecIdType: "
				+ contract.getSecTypeId() + " Primary Exch: "
				+ contract.getPrimaryExchange() + " Local Symbol: "
				+ contract.getLocalSymbol() + " Multiplier: "
				+ contract.getPriceMultiplier() + " Expiry: "
				+ contract.getExpiry() + " Category: " + contract.getCategory()
				+ " Industry: " + contract.getIndustry() + " Description: "
				+ contract.getDescription());
	}

	/**
	 * Method logExecution.
	 * 
	 * @param execution
	 *            com.ib.client.Execution
	 */
	public static void logExecution(TradeOrderfill execution) {
		_log.info("execDetails OrderId: "
				+ execution.getTradeOrder().getIdTradeOrder() + " Exchange: "
				+ execution.getExchange() + " Side: " + execution.getSide()
				+ " ExecId: " + execution.getExecId() + " Time: "
				+ execution.getTime() + " Qty: " + execution.getQuantity()
				+ " AveragePrice: " + execution.getAveragePrice() + " Price: "
				+ execution.getPrice() + " CumulativeQuantity: "
				+ execution.getCumulativeQuantity());
	}
}
