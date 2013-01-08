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
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.dao.Trade;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 */
public class PosMgrAll5MinBarStrategy extends AbstractStrategyRule {

	/**
	 * 1/ If the open position is filled create a STP and 1 Target (LMT) OCA
	 * order at 6R with 100% of the filled quantity. Use the open position fill
	 * quantity, price and stop price to determine the target price. The STP
	 * order take an initial risk of 1R.
	 * 
	 * 2/ Target/Stop prices should be round over/under whole/half numbers when
	 * ever they are calculated..
	 * 
	 * 4/ After 9:40 trail the whole position under/over the previous bar.
	 * 
	 * 5/ Close any open positions at 15:55.
	 * 
	 */

	private static final long serialVersionUID = -1802229646519981959L;
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

			/*
			 * Get the current candle
			 */
			CandleItem currentCandle = (CandleItem) candleSeries
					.getDataItem(getCurrentCandleCount());
			Date startPeriod = currentCandle.getPeriod().getStart();

			/*
			 * Only manage trades when the market is open and the candle is for
			 * the Tradestrategies trading day.
			 */
			if (isDuringTradingday(startPeriod)) {
				/*
				 * _log.info("PositionManagerStrategy symbol: " + getSymbol() +
				 * " startPeriod: " + startPeriod + " Close Price: " +
				 * currentCandle.getClose() + " Vwap: " +
				 * currentCandle.getVwap());
				 */

				/*
				 * Get the current open trade. If no trade is open this Strategy
				 * will be closed down.
				 */

				if (!getTrade().getIsOpen()) {
					_log.info("No open position so Cancel Strategy Mgr Symbol: "
							+ getSymbol() + " Time:" + startPeriod);
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
					 * target and stop orders for the open quantity.
					 */

					Money targetOnePrice = createStopAndTargetOrder(
							getOpenPositionOrder(), 1, 6, 100, true);
					setTargetPrice(targetOnePrice);
					_log.info("Open position submit Stop/Tgt orders created Symbol: "
							+ getSymbol() + " Time:" + startPeriod);

				}

				/*
				 * Trail the whole position at the low of the previous 5min bar.
				 */
				if ((null != getTargetPrice())
						&& newBar
						&& startPeriod.after(TradingCalendar.getSpecificTime(
								startPeriod, 9, 40))) {

					if (set5MinBarTrail(getTrade(), 1)) {
						_log.info("PositionManagerStrategy 5min Candle: "
								+ getSymbol() + " Trail Price: "
								+ getTargetPrice() + " Time: " + startPeriod);
						moveStopOCAPrice(getTargetPrice(), true);
					}
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
			error(1, 40, "Error Position Manager exception: " + ex.getMessage());
		}
	}

	/*
	 * This method is used to trail on candle bars. Note trail is on the
	 * low/high of the bar and assumes the bar are in the direction of the trade
	 * i.e. side.
	 * 
	 * @param trade The trade that has the open position.
	 * 
	 * @param bars The number of bars to trail on.
	 */

	/**
	 * Method set5MinBarTrail.
	 * 
	 * @param trade
	 *            Trade
	 * @param bars
	 *            int
	 * @return boolean
	 * @throws StrategyRuleException
	 */
	public boolean set5MinBarTrail(Trade trade, int bars)
			throws StrategyRuleException {
		boolean trail = false;
		Money newStop = new Money(this.getOpenPositionOrder()
				.getAverageFilledPrice());

		CandleDataset dataset = getTradestrategy().getDatasetContainer()
				.getCandleDataset();
		if (null == dataset) {
			throw new StrategyRuleException(1, 110,
					"Error no Candle indicator defined for this strategy");
		} else {

			CandleSeries series = dataset.getSeries(0);
			// Start with the previous bar and work back
			int itemCount = (series.getItemCount());
			if (itemCount > (2 + bars)) {
				itemCount = itemCount - 2;
				for (int i = itemCount; i > (itemCount - bars); i--) {
					CandleItem candle = (CandleItem) series.getDataItem(i);
					trail = false;
					if (Side.BOT.equals(trade.getSide())) {
						if ((candle.getLow() > newStop.doubleValue())
								&& (candle.getOpen() < candle.getClose())) {
							newStop = new Money(candle.getLow());
							trail = true;
						}
					} else {
						if ((candle.getHigh() < newStop.doubleValue())
								&& (candle.getOpen() > candle.getClose())) {
							newStop = new Money(candle.getHigh());
							trail = true;
						}
					}
					if (!trail) {
						break;
					}
				}
				if (trail) {
					setTargetPrice(newStop);
				}
			}
		}
		return trail;
	}
}
