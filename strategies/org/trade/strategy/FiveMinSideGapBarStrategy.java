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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModel;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.dao.Entrylimit;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class FiveMinSideGapBarStrategy extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1629661431267666769L;
	private final static Logger _log = LoggerFactory
			.getLogger(FiveMinSideGapBarStrategy.class);

	/**
	 * Default Constructor
	 * 
	
	
	
	
	 * @param brokerManagerModel BrokerModel
	 * @param datasetContainer StrategyData
	 * @param idTradestrategy Integer
	 */

	public FiveMinSideGapBarStrategy(BrokerModel brokerManagerModel,
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
	 * @param candleSeries CandleSeries
	 * @param newBar boolean
	 * @see org.trade.strategy.StrategyRule#runStrategy(CandleSeries, boolean)
	 */
	public void runStrategy(CandleSeries candleSeries, boolean newBar) {

		try {
			if (getCurrentCandleCount() > 0) {
				// Get the current candle
				CandleItem currentCandleItem = (CandleItem) candleSeries
						.getDataItem(getCurrentCandleCount());
				Date startPeriod = currentCandleItem.getPeriod().getStart();

				/*
				 * Trade is open kill this Strategy as its job is done.
				 */
				if (this.isPositionOpen() || this.isPositionCancelled()) {
					_log.info("FiveMinSideGapBarStrategy complete open position filled symbol: "
							+ getSymbol() + " startPeriod: " + startPeriod);
					this.cancel();
					return;
				}
				// Only manage trades when the market is open
				// and the candle is for the Tradestrategies trading day.
				if (TradingCalendar.isMarketHours(startPeriod)
						&& TradingCalendar.sameDay(getTradestrategy()
								.getTradingday().getOpen(), startPeriod)) {

					// _log.info(getTradestrategy().getStrategy().getClassName()
					// + " symbol: " + getSymbol() +
					// " startPeriod: " + startPeriod);

					CandleItem prevCandleItem = (CandleItem) candleSeries
							.getDataItem(getCurrentCandleCount() - 1);

					// If the 5min low is broken cancel orders as
					// trade no longer valid.
					if (null != this.getTrade()) {
						CandleItem openCandle = this.getCandle(this
								.getTradestrategy().getTradingday().getOpen());
						if (!this.getOpenPositionOrder().getIsFilled()) {
							if (Side.BOT.equals(getTradestrategy().getSide())) {
								if (openCandle.getLow() > prevCandleItem
										.getLow()) {
									_log.info("Rule 5min low broken. Symbol: "
											+ getSymbol() + " Time: "
											+ startPeriod);
									cancelOrder(this.getOpenPositionOrder());
									updateTradestrategyStatus(TradestrategyStatus.FIVE_MIN_LOW_BROKEN);
									this.cancel();
									return;

								}
							} else {
								if (openCandle.getHigh() < prevCandleItem
										.getHigh()) {
									_log.info("Rule 5min high broken. Symbol: "
											+ getSymbol() + " Time: "
											+ startPeriod);
									cancelOrder(this.getOpenPositionOrder());
									updateTradestrategyStatus(TradestrategyStatus.FIVE_MIN_HIGH_BROKEN);
									this.cancel();
									return;
								}
							}
						}
					}

					/*
					 * Is it the the 9:35 candle? and we have not created an
					 * open position trade.
					 */
					if (startPeriod.equals(TradingCalendar.getSpecificTime(
							startPeriod, 9, 35)) && newBar) {

						Money price = new Money(prevCandleItem.getHigh());
						Money priceStop = new Money(prevCandleItem.getLow());
						String action = Action.BUY;
						if (Side.SLD.equals(getTradestrategy().getSide())) {
							price = new Money(prevCandleItem.getLow());
							priceStop = new Money(prevCandleItem.getHigh());
							action = Action.SELL;
						}
						// Is the candle in the direction of the trade i.e.
						// a long play should have a green 5min candle
						if (prevCandleItem.isSide(getTradestrategy().getSide())) {
							Money priceClose = new Money(
									prevCandleItem.getClose());
							Entrylimit entrylimit = getEntryLimit().getValue(
									priceClose);

							double percentChange = Math.abs(prevCandleItem
									.getHigh() - prevCandleItem.getLow())
									/ prevCandleItem.getClose();
							// If the candle less than the entry limit %
							if (percentChange < entrylimit.getPercent()
									.doubleValue()) {
								// TODO add the tails as a % of the body.
								_log.info(" We have a trade!!  Symbol: "
										+ getSymbol() + " Time: " + startPeriod);

								/*
								 * Create an open position.
								 */
								createRiskOpenPosition(action, price,
										priceStop, true);

							} else {
								_log.info("Rule 9:35 5min bar outside % limits. Symbol: "
										+ getSymbol() + " Time: " + startPeriod);
								updateTradestrategyStatus(TradestrategyStatus.PERCENT);
								// Kill this process we are done!
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

							// Kill this process we are done!
							this.cancel();
						}
					} else if (startPeriod.equals(TradingCalendar
							.getSpecificTime(startPeriod, 10, 30))
							|| startPeriod.after(TradingCalendar
									.getSpecificTime(startPeriod, 10, 30))) {

						if (!this.isPositionOpen()
								&& !TradestrategyStatus.CANCELLED
										.equals(getTradestrategy().getStatus())) {
							this.updateTradestrategyStatus(TradestrategyStatus.TO);
							cancelOrder(this.getOpenPositionOrder());
							// No trade we timed out
							_log.info("Rule 10:30:00 bar, time out unfilled open position Symbol: "
									+ getSymbol() + " Time: " + startPeriod);
						}
						this.cancel();
					}
				}
			}

		} catch (Exception ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 20, "Error  runRule exception: " + ex.getMessage());
		}
	}
}
