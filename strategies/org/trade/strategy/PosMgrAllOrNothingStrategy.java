/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade test
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
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 */
public class PosMgrAllOrNothingStrategy extends AbstractStrategyRule {

	/**
	 * 1/ If the open position is filled create a STP (transmit=false see 2/ )
	 * and 1 Target (LMT) OCA order at 3R with 100% of the filled quantity. Use
	 * the open position fill quantity, price and stop price to determine the
	 * target price. The STP order take an initial risk of 1R.
	 * 
	 * 2/ The STP order should not be transmitted unless the current bars Vwap
	 * crosses the stop price. Note this will result in more than a 1R stop.
	 * This help with quick tails below the 5min low.
	 * 
	 * 3/ Target/Stop prices should be round over/under whole/half numbers when
	 * ever they are calculated..
	 * 
	 * 4/ At 10:30 move the STP order to the average fill price of the filled
	 * open order.
	 * 
	 * 5/ Close any open positions at 15:55.
	 * 
	 */

	private static final long serialVersionUID = 5998132222691879078L;
	private final static Logger _log = LoggerFactory
			.getLogger(PosMgrAllOrNothingStrategy.class);

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

	public PosMgrAllOrNothingStrategy(BrokerModel brokerManagerModel,
			StrategyData datasetContainer, Integer idTradestrategy) {
		super(brokerManagerModel, datasetContainer, idTradestrategy);
	}

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

			/* Get the current candle */
			CandleItem currentCandle = (CandleItem) candleSeries
					.getDataItem(getCurrentCandleCount());
			Date startPeriod = currentCandle.getPeriod().getStart();

			/*
			 * Only manage trades when the market is open and the candle is for
			 * the Tradestrategies trading day.
			 */
			if (isDuringTradingday(startPeriod)) {

				// _log.info("PositionManagerStrategy symbol: " +
				// getSymbol()
				// + " startPeriod: " + startPeriod);

				/*
				 * Get the current open trade. If no trade is open this Strategy
				 * will be closed down.
				 */

				if (!getTrade().getIsOpen()) {
					this.cancel();
					return;
				}
				/*
				 * If all trades are closed shut down the position manager
				 * 
				 * Note this strategy is run as soon as we enter a position.
				 * 
				 * Check to see if the open position is filled and the open
				 * quantity is > 0 also check to see if we already have this
				 * position covered.
				 */

				if (getTrade().getIsOpen() && !this.isPositionConvered()) {

					/*
					 * Position has been opened and not covered submit the
					 * target and stop orders for the open quantity. One target
					 * at 3R.
					 */

					_log.info("Open position submit Stop/Tgt orders Symbol: "
							+ getSymbol() + " Time:" + startPeriod);
					Money targetPrice = createStopAndTargetOrder(
							getOpenPositionOrder(), 1, 3, 100, false);
					setTargetPrice(targetPrice);
				}
				/*
				 * Manage the stop orders if the close/open of this bar breaks
				 * the stop price transmit the stop order this allows for tails
				 * that break the 5min low before 10:30am
				 */

				if (startPeriod.before(TradingCalendar.getSpecificTime(
						startPeriod, 10, 30))) {

					if (Side.BOT.equals(getTrade().getSide())) {
						if (currentCandle.getVwap() < getOpenPositionOrder()
								.getStopPrice().doubleValue()) {
							Money stopPrice = addPennyAndRoundStop(
									getOpenPositionOrder().getStopPrice()
											.doubleValue(), getTrade()
											.getSide(), Action.SELL, 0.01);
							moveStopOCAPrice(stopPrice, true);
							_log.info("Move Stop to b.e. Strategy Mgr cancelled Symbol: "
									+ getSymbol()
									+ " Time:"
									+ startPeriod
									+ " Price: " + stopPrice);
						}
					} else {

						if (currentCandle.getVwap() > getOpenPositionOrder()
								.getStopPrice().doubleValue()) {
							Money stopPrice = addPennyAndRoundStop(
									getOpenPositionOrder().getStopPrice()
											.doubleValue(), getTrade()
											.getSide(), Action.BUY, 0.01);
							moveStopOCAPrice(stopPrice, true);
							_log.info("Move Stop to b.e. Strategy Mgr cancelled Symbol: "
									+ getSymbol()
									+ " Time:"
									+ startPeriod
									+ " Price: " + stopPrice);
						}
					}
				}

				/*
				 * 10:30 Move stop order to b.e.
				 */
				if (startPeriod.equals(TradingCalendar.getSpecificTime(
						startPeriod, 10, 30))) {

					_log.info("Rule move stop to b.e.. Symbol:" + getSymbol()
							+ " Time: " + startPeriod);
					String action = Action.SELL;
					if (getTrade().getSide().equals(Side.SLD)) {
						action = Action.BUY;
					}
					moveStopOCAPrice(

							addPennyAndRoundStop(getOpenPositionOrder()
									.getAverageFilledPrice().doubleValue(),
									getTrade().getSide(), action, 0.01), true);
				}
				/*
				 * Close any opened positions with a market order at the end of
				 * the day.
				 */
				if (startPeriod.equals(TradingCalendar.getSpecificTime(
						startPeriod, 15, 55))) {
					closeAllOpenPositions();
					_log.info("PositionManagerStrategy 15:55:00 done: "
							+ getSymbol() + " Time: " + startPeriod);
					this.cancel();
				}
			}

		} catch (Exception ex) {
			_log.error("Error Position Manager exception: " + ex.getMessage(),
					ex);
			error(1, 30,
					"Error  Position Manager exception: " + ex.getMessage());
		}
	}
}
