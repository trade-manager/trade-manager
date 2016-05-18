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

import java.util.EventListener;
import java.util.concurrent.ConcurrentHashMap;

import org.trade.persistent.dao.TradePosition;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;

/**
 * The interface that must be supported by classes that wish to receive
 * notification of changes to a dataset.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public interface BrokerChangeListener extends EventListener {

	/**
	 * Receives notification of an brokerManagerModel change event.
	 * 
	 */

	public void connectionOpened();

	public void connectionClosed(boolean forced);

	/**
	 * Method executionDetailsEnd.
	 * 
	 * @param execDetails
	 *            ConcurrentHashMap<Integer,TradeOrder>
	 */
	public void executionDetailsEnd(ConcurrentHashMap<Integer, TradeOrder> execDetails);

	/**
	 * Method historicalDataComplete.
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void historicalDataComplete(Tradestrategy tradestrategy);

	/**
	 * Method managedAccountsUpdated.
	 * 
	 * @param accountNumber
	 *            String
	 */
	public void managedAccountsUpdated(String accountNumber);

	/**
	 * Method fAAccountsCompleted. Notifies all registered listeners that the
	 * brokerManagerModel has received all FA Accounts information.
	 * 
	 */
	public void fAAccountsCompleted();

	/**
	 * Method updateAccountTime.
	 * 
	 * @param accountNumber
	 *            String
	 */
	public void updateAccountTime(String accountNumber);

	/**
	 * Method brokerError.
	 * 
	 * @param brokerError
	 *            BrokerModelException
	 */
	public void brokerError(BrokerModelException brokerError);

	/**
	 * Method tradeOrderFilled.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderFilled(TradeOrder tradeOrder);

	/**
	 * Method tradeOrderCancelled.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderCancelled(TradeOrder tradeOrder);

	/**
	 * Method tradeOrderStatusChanged.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderStatusChanged(TradeOrder tradeOrder);

	/**
	 * Method positionClosed.
	 * 
	 * @param tradePosition
	 *            TradePosition
	 */
	public void positionClosed(TradePosition tradePosition);

	/**
	 * Method openOrderEnd.
	 * 
	 * @param openOrders
	 *            ConcurrentHashMap<Integer,TradeOrder>
	 */
	public void openOrderEnd(ConcurrentHashMap<Integer, TradeOrder> openOrders);

}
