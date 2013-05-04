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
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class Vwap5MinSideGapBarStrategy extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2138009534354123773L;
	private final static Logger _log = LoggerFactory
			.getLogger(Vwap5MinSideGapBarStrategy.class);

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

	public Vwap5MinSideGapBarStrategy(BrokerModel brokerManagerModel,
			StrategyData datasetContainer, Integer idTradestrategy) {
		super(brokerManagerModel, datasetContainer, idTradestrategy);
	}

	/*
	 * Enter over/under the 9:35am 5min bar if the bar is in the direction of
	 * the Tradestrategy side and the Vwap is also in that direction. Note the
	 * strategy will run until either: 1/ The above conditions are not met,
	 * cancel this strategy. 2/ The open position is filled, cancel this
	 * strategy. 3/ 10:30 comes cancel open position and cancel this strategy.
	 * 4/ Open position is filled, cancel this strategy.
	 * 
	 * @param candleSeries the series of candles that has been updated.
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
				CandleItem currentCandleItem = this.getCurrentCandle();
				Date startPeriod = currentCandleItem.getPeriod().getStart();

				/*
				 * Trade is open kill this Strategy as its job is done.
				 */
				if (this.isThereOpenPosition()) {
					_log.info("Vwap5MinSideGapBarStrategy complete open position filled symbol: "
							+ getSymbol() + " startPeriod: " + startPeriod);
					this.cancel();
					return;
				}

				// AbstractStrategyRule.logCandle(currentCandleItem.getCandle());

				CandleItem prevCandleItem = (CandleItem) candleSeries
						.getDataItem(getCurrentCandleCount() - 1);
				/*
				 * Is it the the 9:35 candle? and we have not created an open
				 * position trade.
				 */
				if (startPeriod.equals(TradingCalendar.getSpecificTime(
						startPeriod, 9, 35)) && newBar) {

					/*
					 * Is the candle in the direction of the Tradestrategy side
					 * i.e. a long play should have a green 5min candle
					 */
					if (prevCandleItem.isSide(getTradestrategy().getSide())) {

						if ((Side.BOT.equals(getTradestrategy().getSide()) && prevCandleItem
								.getVwap() < currentCandleItem.getVwap())
								|| (Side.SLD.equals(getTradestrategy()
										.getSide()) && prevCandleItem.getVwap() > currentCandleItem
										.getVwap())) {
							/*
							 * Based on the prev bar create the stop, entry
							 * price using the 9:35 5min bar high/low.
							 */
							Money price = new Money(prevCandleItem.getHigh());
							Money priceStop = new Money(prevCandleItem.getLow());
							String action = Action.BUY;
							if (Side.SLD.equals(getTradestrategy().getSide())) {
								price = new Money(prevCandleItem.getLow());
								priceStop = new Money(prevCandleItem.getHigh());
								action = Action.SELL;
							}

							/*
							 * Create an open position order.
							 */
							createRiskOpenPosition(action, price, priceStop,
									true, null, null, null, null);

						} else {
							_log.info("Rule Vwap 5 min Side Gap bar. Vwap not in direction of side. Symbol: "
									+ getSymbol() + " Time: " + startPeriod);

							if (Side.SLD.equals(getTradestrategy().getSide())) {
								this.updateTradestrategyStatus(TradestrategyStatus.GB);
							} else {
								this.updateTradestrategyStatus(TradestrategyStatus.RB);
							}

							// Cancel this process we are done!
							this.cancel();
						}

					} else {
						_log.info("Rule 5 min Red/Green bar opposite to trade direction. Symbol: "
								+ getSymbol() + " Time: " + startPeriod);

						if (Side.SLD.equals(getTradestrategy().getSide())) {
							this.updateTradestrategyStatus(TradestrategyStatus.GB);
						} else {
							this.updateTradestrategyStatus(TradestrategyStatus.RB);
						}

						// Cancel this process we are done!
						this.cancel();
					}
				} else if (!startPeriod.before(TradingCalendar.getSpecificTime(
						startPeriod, 10, 30))) {

					if (!this.isThereOpenPosition()
							&& !TradestrategyStatus.CANCELLED
									.equals(getTradestrategy().getStatus())) {
						this.updateTradestrategyStatus(TradestrategyStatus.TO);
						this.cancelAllOrders();
						// No trade we timed out
						_log.info("Rule 10:30:00 bar, time out unfilled open position Symbol: "
								+ getSymbol() + " Time: " + startPeriod);
					}
					this.cancel();
				}
			}

		} catch (Exception ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 20, "Error  runRule exception: " + ex.getMessage());
		}
	}
}
