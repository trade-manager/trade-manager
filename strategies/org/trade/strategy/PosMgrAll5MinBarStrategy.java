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
package org.trade.strategy;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModel;
import org.trade.core.util.TradingCalendar;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class PosMgrAll5MinBarStrategy extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2281013751087462982L;
	private final static Logger _log = LoggerFactory
			.getLogger(PosMgrAll5MinBarStrategy.class);

	/**
	 * Default Constructor
	 * 
	 * @param brokerManagerModel
	 *            BrokerModel
	 * @param datasetContainer
	 *            StrategyData
	 * @param idTradestrategy
	 *            Integer
	 */

	public PosMgrAll5MinBarStrategy(BrokerModel brokerManagerModel,
			StrategyData datasetContainer, Integer idTradestrategy) {
		super(brokerManagerModel, datasetContainer, idTradestrategy);
	}

	/*
	 * Note the current candle is just forming Enter a tier 1-3 gap in first
	 * 5min bar direction, with a 3R target and stop @ 5min high/low
	 * 
	 * @param candleSeries the series of candels that has been updated.
	 * 
	 * @param newBar has a new bar just started.
	 */
	/**
	 * Method runStrategy.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @param newBar
	 *            boolean
	 * @see org.trade.strategy.StrategyRule#runStrategy(CandleSeries, boolean)
	 */
	public void runStrategy(CandleSeries candleSeries, boolean newBar) {

		try {
			if (getCurrentCandleCount() > 0) {
				// Get the current candle
				CandleItem currentCandleItem = (CandleItem) candleSeries
						.getDataItem(getCurrentCandleCount());
				Date startPeriod = currentCandleItem.getPeriod().getStart();

				// _log.info(getTradestrategy().getStrategy().getClassName()
				// + " symbol: " + getSymbol() + " startPeriod: "
				// + startPeriod);

				// Is it the the 9:35 candle?
				if (startPeriod.equals(TradingCalendar.getSpecificTime(
						startPeriod, 9, 35)) && newBar) {

				} else if (startPeriod.equals(TradingCalendar.getSpecificTime(
						startPeriod, 10, 30))) {

				} else if (startPeriod.after(TradingCalendar.getSpecificTime(
						startPeriod, 10, 30))) {
					_log.info("Rule after 10:30:00 bar, close the "
							+ getTradestrategy().getStrategy().getClassName()
							+ " Symbol: " + getSymbol());
					// Kill this process we are done!
					this.cancel();
				}

				/*
				 * Close any opened positions with a market order at the end of
				 * the day.
				 */
				if (!startPeriod.before(TradingCalendar.getSpecificTime(
						startPeriod, 15, 58))) {
					closeOpenPosition();
					_log.info("Rule 15:58:00 close all open positions: "
							+ getSymbol() + " Time: " + startPeriod);
					this.cancel();
				}
			}

		} catch (StrategyRuleException ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 10, "Error  runRule exception: " + ex.getMessage());
		}
	}
}
