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
import org.trade.persistent.dao.Trade;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.HeikinAshiDataset;
import org.trade.strategy.data.HeikinAshiSeries;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.heikinashi.HeikinAshiItem;

/**
 */
public class PosMgrFH3RBHHeikinStrategy extends AbstractStrategyRule {

	/**
	 * 1/ If the open position is filled create a STP and 2 Targets (LMT) OCA
	 * orders at 4R and 7R with 50% of the filled quantity for each. Use the
	 * open position fill quantity, price and stop price to determine the target
	 * price. The STP orders take an initial risk of 2R.
	 * 
	 * 2/ Target/Stop prices should be round over/under whole/half numbers when
	 * ever they are calculated..
	 * 
	 * 3/ After 9:35 and before 10:30 if the current VWAP crosses the 9:35
	 * candles VWAP move the STP price on each of the STP order to break even.
	 * 
	 * 4/ At 10:30 move the STP order to the average fill price of the filled
	 * open order.
	 * 
	 * 5/ When target one has been reached trail the second half using prev two
	 * Heikin-Ashi bars. i.e. STP price is moved up to the Low of current
	 * Heikin-Ashi bar -2 as long as the prev bars low is higher than the prev
	 * bar -1 low.
	 * 
	 * 6/ Close any open positions at 15:55.
	 * 
	 */

	private static final long serialVersionUID = -6717691162128305191L;
	private final static Logger _log = LoggerFactory
			.getLogger(PosMgrFH3RBHHeikinStrategy.class);

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

	public PosMgrFH3RBHHeikinStrategy(BrokerModel brokerManagerModel,
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
					 * target and stop orders for the open quantity. Two targets
					 * at 4R and 7R Stop and 2X actual stop this will be managed
					 * to 1R below
					 * 
					 * Make the stop -2R and manage to the Vwap MA of the
					 * opening bar.
					 */
					Money targetOnePrice = createStopAndTargetOrder(
							getOpenPositionOrder(), 2, 4, 50, true);
					setTargetPrice(targetOnePrice);
					createStopAndTargetOrder(getOpenPositionOrder(), 2, 7, 50,
							true);
					_log.info("Open position submit Stop/Tgt orders created Symbol: "
							+ getSymbol() + " Time:" + startPeriod);

				}

				/*
				 * Manage the stop orders if the current bars Vwap crosses the
				 * Vwap of the first 5min bar then move the stop price (
				 * currently -2R) to the average fill price i.e. break even.
				 * This allows for tails that break the 5min high/low between
				 * 9:40 thru 10:30.
				 */

				if (startPeriod.before(TradingCalendar.getSpecificTime(
						startPeriod, 10, 30))
						&& startPeriod.after(TradingCalendar.getSpecificTime(
								startPeriod, 9, 35))) {

					CandleItem firstCandle = this.getCandle(TradingCalendar
							.getSpecificTime(this.getTradestrategy()
									.getTradingday().getOpen(), startPeriod));

					if (Side.BOT.equals(getTrade().getSide())) {
						if (currentCandle.getVwap() < firstCandle.getVwap()) {
							Money stopPrice = addPennyAndRoundStop(getTrade()
									.getOpenPosition().getAverageFilledPrice()
									.doubleValue(), getTrade().getSide(),
									Action.SELL, 0.01);
							moveStopOCAPrice(stopPrice, true);
							_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
									+ getSymbol() + " Time:" + startPeriod
									+ " Price: " + stopPrice);
						}
					} else {

						if (currentCandle.getVwap() > firstCandle.getVwap()) {
							Money stopPrice = addPennyAndRoundStop(getTrade()
									.getOpenPosition().getAverageFilledPrice()
									.doubleValue(), getTrade().getSide(),
									Action.BUY, 0.01);
							moveStopOCAPrice(stopPrice, true);
							_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
									+ getSymbol() + " Time:" + startPeriod
									+ " Price: " + stopPrice);
						}
					}
				}

				/*
				 * At 10:30 Move stop order to b.e. i.e. the average fill price
				 * of the open order.
				 */
				if (startPeriod.equals(TradingCalendar.getSpecificTime(
						startPeriod, 10, 30)) && newBar) {

					_log.info("Rule move stop to b.e.. Symbol: " + getSymbol()
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
				 * We have sold the first half of the position try to trail BH
				 * on Heikin-Ashi above target 1 with a two bar trail.
				 */
				if ((null != getTargetPrice()) && newBar) {
					if (setHiekinAshiTrail(getTrade(), 2)) {
						_log.info("PositionManagerStrategy HiekinAshiTrail: "
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
			error(1,
					40,
					"Error Position Manager exception: "
							+ ex.getLocalizedMessage());
		}
	}

	/*
	 * This method is used to trail on Heikin-Ashi bars. Note trail is on the
	 * low/high of the bar and assumes the bar are in the direction of the trade
	 * i.e. side.
	 * 
	 * @param trade The trade that has the open position.
	 * 
	 * @param bars The number of bars to trail on.
	 */

	/**
	 * Method setHiekinAshiTrail.
	 * 
	 * @param trade
	 *            Trade
	 * @param bars
	 *            int
	 * @return boolean
	 * @throws StrategyRuleException
	 */
	public boolean setHiekinAshiTrail(Trade trade, int bars)
			throws StrategyRuleException {
		boolean trail = false;
		Money newStop = getTargetPrice();

		HeikinAshiDataset dataset = (HeikinAshiDataset) getTradestrategy()
				.getDatasetContainer().getIndicatorByType(
						IndicatorSeries.HeikinAshiSeries);
		if (null == dataset) {
			throw new StrategyRuleException(1, 110,
					"Error no Hiekin-Ashi indicator defined for this strategy");
		} else {

			HeikinAshiSeries series = dataset.getSeries(0);
			// Start with the previous bar and work back
			int itemCount = (series.getItemCount());
			if (itemCount > (2 + bars)) {
				itemCount = itemCount - 2;
				for (int i = itemCount; i > (itemCount - bars); i--) {
					HeikinAshiItem candle = (HeikinAshiItem) series
							.getDataItem(i);
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
