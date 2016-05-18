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
package org.trade.strategy;

import org.trade.persistent.dao.TradeOrder;
import org.trade.strategy.data.CandleSeries;

/**
 */
public interface StrategyRule {

	public final String PACKAGE = "org.trade.strategy.";

	/**
	 * Method runStrategy.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @param newBar
	 *            boolean
	 * @throws StrategyRuleException
	 */
	void runStrategy(CandleSeries candleSeries, boolean newBar) throws StrategyRuleException;

	/**
	 * Method error.
	 * 
	 * @param id
	 *            int
	 * @param errorCode
	 *            int
	 * @param errorMsg
	 *            String
	 */
	void error(int id, int errorCode, String errorMsg);

	void execute();

	void cancel();

	/**
	 * Method isCancelled.
	 * 
	 * @return boolean
	 */
	boolean isCancelled();

	/**
	 * Method isDone.
	 * 
	 * @return boolean
	 */
	boolean isDone();

	/**
	 * Method isRunning.
	 * 
	 * @return boolean
	 */
	boolean isRunning();

	/**
	 * Method isWaiting.
	 * 
	 * @return boolean
	 */
	boolean isWaiting();

	/**
	 * Method addMessageListener.
	 * 
	 * @param listener
	 *            StrategyChangeListener
	 */
	void addMessageListener(StrategyChangeListener listener);

	/**
	 * Method removeMessageListener.
	 * 
	 * @param listener
	 *            StrategyChangeListener
	 */
	void removeMessageListener(StrategyChangeListener listener);

	/**
	 * Method tradeOrderFilled.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	void tradeOrderFilled(TradeOrder tradeOrder);
}
