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

import java.util.Date;
import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;

import org.trade.broker.client.BackTestBroker;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.TradeAccount;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;

/**
 */
public interface BrokerModel {

	public final static String _brokerTest = "BrokerTest";

	public final static String _broker = "Broker";

	/**
	 * Method addMessageListener.
	 * 
	 * @param listener
	 *            BrokerChangeListener
	 */
	void addMessageListener(BrokerChangeListener listener);

	/**
	 * Method removeMessageListener.
	 * 
	 * @param listener
	 *            BrokerChangeListener
	 */
	void removeMessageListener(BrokerChangeListener listener);

	/**
	 * Method hasListener.
	 * 
	 * @param listener
	 *            EventListener
	 * @return boolean
	 */
	boolean hasListener(EventListener listener);

	/**
	 * Method error.
	 * 
	 * @param id
	 *            int
	 * @param code
	 *            int
	 * @param msg
	 *            String
	 */
	void error(int id, int code, String msg);

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
	 */
	void onConnect(String host, Integer port, Integer clientId)
			throws BrokerModelException;

	/**
	 * Method isConnected.
	 * 
	 * @return boolean
	 */
	boolean isConnected();

	/**
	 * Method disconnect.
	 * 
	 * @throws BrokerModelException
	 */
	void disconnect() throws BrokerModelException;

	/**
	 * Method getNextRequestId.
	 * 
	 * @return Integer
	 */
	Integer getNextRequestId();

	/**
	 * Method getBackTestBroker.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * @see org.trade.broker.BrokerModel#getBackTestBroker(Integer)
	 */
	BackTestBroker getBackTestBroker(Integer idTradestrategy);

	/**
	 * Method onSubscribeAccountUpdates.
	 * 
	 * @param subscribe
	 *            boolean
	 * @param tradeAccounte
	 *            TradeAccount
	 * @throws BrokerModelException
	 */
	void onSubscribeAccountUpdates(boolean subscribe, TradeAccount tradeAccounte)
			throws BrokerModelException;

	/**
	 * Method onCancelAccountUpdates.
	 * 
	 * @param accountNumber
	 *            String
	 */
	void onCancelAccountUpdates(String accountNumber);

	/**
	 * Method onReqManagedAccount.
	 * 
	 * @throws BrokerModelException
	 */
	void onReqManagedAccount() throws BrokerModelException;

	/**
	 * Method onReqAllOpenOrders.
	 * 
	 * @throws BrokerModelException
	 */
	void onReqAllOpenOrders() throws BrokerModelException;

	/**
	 * Method onReqOpenOrders.
	 * 
	 * @throws BrokerModelException
	 */
	void onReqOpenOrders() throws BrokerModelException;

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
			Integer chartDays) throws BrokerModelException;

	/**
	 * Method onReqRealTimeBars.
	 * 
	 * @param contract
	 *            Contract
	 * @throws BrokerModelException
	 */
	void onReqRealTimeBars(Contract contract, boolean mktData)
			throws BrokerModelException;

	/**
	 * Method onReqAllExecutions.
	 * 
	 * @param mktOpenDate
	 *            Date
	 * @throws BrokerModelException
	 */
	void onReqAllExecutions(Date mktOpenDate) throws BrokerModelException;

	/**
	 * Method onReqExecutions.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @throws BrokerModelException
	 */
	void onReqExecutions(Tradestrategy tradestrategy)
			throws BrokerModelException;

	/**
	 * Method isRealtimeBarsRunning.
	 * 
	 * @param contract
	 *            Contract
	 * @return boolean
	 */
	boolean isRealtimeBarsRunning(Contract contract);

	/**
	 * Method isHistoricalDataRunning.
	 * 
	 * @param contract
	 *            Contract
	 * @return boolean
	 */
	boolean isHistoricalDataRunning(Contract contract);

	/**
	 * Method isRealtimeBarsRunning.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return boolean
	 */
	boolean isRealtimeBarsRunning(Tradestrategy tradestrategy);

	/**
	 * Method isHistoricalDataRunning.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return boolean
	 */
	boolean isHistoricalDataRunning(Tradestrategy tradestrategy);

	/**
	 * Method isAccountUpdatesRunning.
	 * 
	 * @param accountNumber
	 *            String
	 * @return boolean
	 */
	boolean isAccountUpdatesRunning(String accountNumber);

	void onCancelAllRealtimeData();

	/**
	 * Method onCancelRealtimeBars.
	 * 
	 * @param contract
	 *            Contract
	 */
	void onCancelRealtimeBars(Contract contract);

	/**
	 * Method onCancelRealtimeBars.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	void onCancelRealtimeBars(Tradestrategy tradestrategy);

	/**
	 * Method onCancelBrokerData.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	void onCancelBrokerData(Tradestrategy tradestrategy);

	/**
	 * Method onCancelContractDetails.
	 * 
	 * @param contract
	 *            Contract
	 */
	void onCancelContractDetails(Contract contract);

	/**
	 * Method onContractDetails.
	 * 
	 * @param contract
	 *            Contract
	 * @throws BrokerModelException
	 */
	void onContractDetails(Contract contract) throws BrokerModelException;

	/**
	 * Method getHistoricalData.
	 * 
	 * @return ConcurrentHashMap<Integer,Contract>
	 */
	ConcurrentHashMap<Integer, Contract> getHistoricalData();

	/**
	 * Method onPlaceOrder.
	 * 
	 * @param contract
	 *            Contract
	 * @param tradeOrder
	 *            TradeOrder
	 * @return TradeOrder
	 * @throws BrokerModelException
	 */
	TradeOrder onPlaceOrder(Contract contract, TradeOrder tradeOrder)
			throws BrokerModelException;

	/**
	 * Method onCancelOrder.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @throws BrokerModelException
	 */
	void onCancelOrder(TradeOrder tradeOrder) throws BrokerModelException;

	/**
	 * Method isBrokerDataOnly.
	 * 
	 * @return boolean
	 */
	boolean isBrokerDataOnly();

	/**
	 * Method setBrokerDataOnly.
	 * 
	 * @param brokerDataOnly
	 *            boolean
	 */
	void setBrokerDataOnly(boolean brokerDataOnly);

}
