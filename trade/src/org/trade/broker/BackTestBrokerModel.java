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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ListIterator;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.trade.persistent.dao.TradeAccount;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.candle.CandleItem;

import com.ib.client.ContractDetails;
import com.ib.client.Execution;

/**
 */
public class BackTestBrokerModel extends AbstractBrokerModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3191422640254347940L;

	private final static Logger _log = LoggerFactory
			.getLogger(BackTestBrokerModel.class);

	// Candle series this is listened to by the chart panel
	// and main controller for updates.
	private static final ConcurrentHashMap<Integer, Contract> m_historyDataRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_realTimeBarsRequests = new ConcurrentHashMap<Integer, Contract>();
	private static final ConcurrentHashMap<Integer, Contract> m_contractRequests = new ConcurrentHashMap<Integer, Contract>();
	private PersistentModel m_tradePersistentModel = null;

	private AtomicInteger orderKey = null;

	private Integer backfillDateFormat = 2;
	private String backfillWhatToShow;
	private Integer backfillOffsetDays = 0;

	private static final SimpleDateFormat m_sdfGMT = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss z");

	public BackTestBrokerModel() {

		try {
			m_tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			backfillWhatToShow = ConfigProperties
					.getPropAsString("trade.backfill.whatToShow");
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
	 * Method isConnected.
	 * 
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isConnected()
	 */
	public boolean isConnected() {
		return false;
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

	}

	/**
	 * Method disconnect.
	 * 
	 * @throws BrokerModelException
	 * @see org.trade.broker.BrokerModel#disconnect()
	 */
	public void disconnect() throws BrokerModelException {
		if (isConnected()) {
			onCancelAllRealtimeData();
		}
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
	public void onBrokerData(Contract contract, Date startDate, Date endDate,
			Integer barSize, Integer chartDays) throws BrokerModelException {

		try {
			if (this.isHistoricalDataRunning(contract)) {
				throw new BrokerModelException(contract.getIdContract(), 3010,
						"Data request is already in progress for: "
								+ contract.getSymbol()
								+ " Please wait or cancel.");
			}
			/*
			 * When running data via the TWS API we start the DatasetContainers
			 * internal thread to process candle updates and all indicator
			 * updates. That reduces the delay to the broker interface thread
			 * for messages coming in.
			 */
			// if (!tradestrategy.getDatasetContainer().isRunning())
			// tradestrategy.getDatasetContainer().execute();
			synchronized (m_historyDataRequests) {
				m_historyDataRequests.put(contract.getIdContract(), contract);
			}

			if (this.isBrokerDataOnly()) {
				/*
				 * This will use the Yahoo API to get the data.
				 */
				for (Tradestrategy tradestrategy : contract
						.getTradestrategies()) {

					if (null == contract.getDescription()) {
						Integer reqId = getNextRequestId();
						m_contractRequests.put(reqId, contract);

						TWSBrokerModel.logContract(TWSBrokerModel
								.getIBContract(contract));
						tradestrategy
								.getDatasetContainer()
								.getBackTestWorker()
								.reqContractDetails(reqId,
										TWSBrokerModel.getIBContract(contract));
					}

					endDate = TradingCalendar.getSpecificTime(endDate,
							TradingCalendar
									.getMostRecentTradingDay(TradingCalendar
											.addBusinessDays(endDate,
													backfillOffsetDays)));
					m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
					String endDateTime = m_sdfGMT.format(endDate);

					tradestrategy
							.getDatasetContainer()
							.getBackTestWorker()
							.reqHistoricalData(
									contract.getIdContract(),
									TWSBrokerModel.getIBContract(contract),
									endDateTime,
									ChartDays.newInstance(chartDays)
											.getDisplayName(),
									BarSize.newInstance(barSize)
											.getDisplayName(),
									backfillWhatToShow, 1, backfillDateFormat);
				}
			} else {
				this.historicalData(contract.getIdContract(),
						"finished- at yyyyMMdd HH:mm:ss", 0, 0, 0, 0, 0, 0, 0,
						false);
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
				if (contract.getTradestrategies().isEmpty()) {
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
			openOrder(tradeOrder.getOrderKey(), null,
					TWSBrokerModel.getIBOrder(tradeOrder), orderState);
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

			if (BackTestBrokerModel.updateTradeOrder(order, orderState,
					transientInstance)) {

				if (OrderStatus.FILLED.equals(transientInstance.getStatus())) {

					_log.info("Open order filled Order Key:"
							+ transientInstance.getOrderKey());
					BackTestBrokerModel.logOrderState(orderState);
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
					BackTestBrokerModel.logOrderState(orderState);
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
				error(orderId, 3090,
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
				transientInstance.setStatus(status.toUpperCase());
				transientInstance.setWhyHeld(whyHeld);
				_log.info("Order Status changed. Status:" + status);
				TWSBrokerModel.logOrderStatus(orderId, status, filled,
						remaining, avgFillPrice, permId, parentId,
						lastFillPrice, clientId, whyHeld);

				m_tradePersistentModel.persistTradeOrder(transientInstance);
				if (OrderStatus.CANCELLED.equals(transientInstance.getStatus())) {
					// Let the controller know a position was closed
					this.fireTradeOrderCancelled(transientInstance);
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
	 * Method bondContractDetails.
	 * 
	 * @param reqId
	 *            int
	 * @param contractDetails
	 *            ContractDetails
	 */
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		_log.info("bondContractDetails:" + reqId);
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
			// Check to see if the trading day is today and this
			// strategy is selected to trade and that the market is open
			if (m_historyDataRequests.containsKey(reqId)) {
				Contract contract = m_historyDataRequests.get(reqId);
				if (dateString.contains("finished-")) {

					try {

						Tradestrategy tradestrategy = contract
								.getTradestrategies().get(0);
						CandleSeries candleSeries = tradestrategy
								.getDatasetContainer().getBaseCandleSeries();
						m_tradePersistentModel
								.persistCandleSeries(candleSeries);
					} catch (Exception ex) {
						error(reqId, 3240, ex.getMessage());
					}
					for (ListIterator<Tradestrategy> itemIter = contract
							.getTradestrategies().listIterator(); itemIter
							.hasNext();) {
						Tradestrategy tradestrategy = itemIter.next();
						if (tradestrategy.getTrade()) {
							this.fireHistoricalDataComplete(tradestrategy);
							onReqRealTimeBars(contract, false);
						} else {
							itemIter.remove();
						}
					}
				} else {

					Date date = null;
					try {
						/*
						 * There is a bug in the TWS interface format for dates
						 * should always be milli sec but when 1 day is selected
						 * as the period the dates come through as yyyyMMdd.
						 */
						if (dateString.length() == 8) {
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyyyMMdd");
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
							date = TradingCalendar.getSpecificTime(
									tradestrategy.getTradingday().getOpen(),
									date);
						}
						if (TradingCalendar.isMarketHours(tradestrategy
								.getTradingday().getOpen(), tradestrategy
								.getTradingday().getClose(), date)) {
							tradestrategy.getDatasetContainer().buildCandle(
									date, open, high, low, close, volume, vwap,
									tradeCount, 1);
						}
					}

				}
				synchronized (m_historyDataRequests) {
					if (contract.getTradestrategies().isEmpty()) {
						m_historyDataRequests.remove(contract.getIdContract());
						m_historyDataRequests.notifyAll();
					}
				}
			}

		} catch (BrokerModelException ex) {
			error(reqId, 3130, ex.getMessage());
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
	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double vwap, int tradeCount) {
		// Called when a candle finishes
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
			_log.debug(" Symbol: " + series.getContract().getSymbol()
					+ " Time: " + candle.getPeriod().getStart() + " Open: "
					+ candle.getOpen() + " Close: " + candle.getClose()
					+ " High: " + candle.getHigh() + " Low: " + candle.getLow()
					+ " Volume: " + candle.getVolume());
		}
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
	public static boolean updateTradeOrder(com.ib.client.Order ibOrder,
			OrderState ibOrderState, TradeOrder order) throws ParseException {

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
					.nullSafeComparator(comms, new Money(Double.MAX_VALUE)) != 0
					&& CoreUtils.nullSafeComparator(order.getCommission(),
							comms.getBigDecimalValue()) != 0) {
				order.setCommission(comms.getBigDecimalValue());
				changed = true;

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
	 */
	public class OrderState {

		public String m_status;
		public String m_initMargin;
		public String m_maintMargin;
		public String m_equityWithLoan;
		public double m_commission;
		public double m_minCommission;
		public double m_maxCommission;
		public String m_commissionCurrency;
		public String m_warningText;

		public OrderState() {
			this(null, null, null, null, 0.0, 0.0, 0.0, null, null);
		}

		/**
		 * Constructor for OrderState.
		 * 
		 * @param status
		 *            String
		 * @param initMargin
		 *            String
		 * @param maintMargin
		 *            String
		 * @param equityWithLoan
		 *            String
		 * @param commission
		 *            double
		 * @param minCommission
		 *            double
		 * @param maxCommission
		 *            double
		 * @param commissionCurrency
		 *            String
		 * @param warningText
		 *            String
		 */
		public OrderState(String status, String initMargin, String maintMargin,
				String equityWithLoan, double commission, double minCommission,
				double maxCommission, String commissionCurrency,
				String warningText) {

			m_initMargin = initMargin;
			m_maintMargin = maintMargin;
			m_equityWithLoan = equityWithLoan;
			m_commission = commission;
			m_minCommission = minCommission;
			m_maxCommission = maxCommission;
			m_commissionCurrency = commissionCurrency;
			m_warningText = warningText;
		}

		/**
		 * Method equals.
		 * 
		 * @param objectToCompare
		 *            Object
		 * @return boolean
		 */
		public boolean equals(Object objectToCompare) {

			if (this == objectToCompare) {
				return true;
			}

			if (objectToCompare == null) {
				return false;
			}
			if (!(objectToCompare instanceof OrderState)) {
				return false;
			}
			OrderState state = (OrderState) objectToCompare;

			if (CoreUtils.nullSafeComparator(new Money(m_commission),
					new Money(state.m_commission)) != 0
					|| (CoreUtils.nullSafeComparator(
							new Money(m_minCommission), new Money(
									state.m_minCommission)) != 0)
					|| (CoreUtils.nullSafeComparator(
							new Money(m_maxCommission), new Money(
									state.m_maxCommission)) != 0)) {
				return false;
			}

			if ((CoreUtils.nullSafeComparator(m_status, state.m_status) != 0)
					|| (CoreUtils.nullSafeComparator(m_initMargin,
							state.m_initMargin) != 0)
					|| (CoreUtils.nullSafeComparator(m_maintMargin,
							state.m_maintMargin) != 0)
					|| (CoreUtils.nullSafeComparator(m_equityWithLoan,
							state.m_equityWithLoan) != 0)
					|| (CoreUtils.nullSafeComparator(m_commissionCurrency,
							state.m_commissionCurrency) != 0)) {
				return false;
			}
			return true;
		}
	}
}
