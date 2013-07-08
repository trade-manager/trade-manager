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
import org.trade.persistent.dao.TradeOrder;
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
	 * 6/ Close any open positions at 15:58.
	 * 
	 */

	private static final long serialVersionUID = -6717691162128305191L;
	private final static Logger _log = LoggerFactory
			.getLogger(PosMgrFH3RBHHeikinStrategy.class);

	private Integer targetOneOrderKey = new Integer(0);

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
			CandleItem currentCandle = this.getCurrentCandle();
			Date startPeriod = currentCandle.getPeriod().getStart();
			if (newBar && getCurrentCandleCount() > 0) {
				CandleItem prevCandleItem = (CandleItem) candleSeries
						.getDataItem(getCurrentCandleCount() - 1);
				AbstractStrategyRule.logCandle(prevCandleItem.getCandle());
			}

			/*
			 * Get the current open trade. If no trade is open this Strategy
			 * will be closed down.
			 */

			if (!this.isThereOpenPosition()) {
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
			 * Check to see if the open position is filled and the open quantity
			 * is > 0 also check to see if we already have this position
			 * covered.
			 */
			if (this.isThereOpenPosition() && !this.isPositionCovered()) {
				/*
				 * Position has been opened and not covered submit the target
				 * and stop orders for the open quantity. Two targets at 4R and
				 * 7R Stop and 2X actual stop this will be managed to 1R below
				 * 
				 * Make the stop -2R and manage to the Vwap MA of the opening
				 * bar.
				 */
				Integer quantity = this.getOpenPositionOrder()
						.getFilledQuantity();
				Integer tgt1Qty = quantity / 2;
				Integer tgt2Qty = quantity - tgt1Qty;
				// Integer tgt3Qty = quantity - (tgt1Qty + tgt2Qty);

				TradeOrder orderTarget = createStopAndTargetOrder(
						getOpenPositionOrder(), 2, 2, tgt1Qty, true);
				targetOneOrderKey = orderTarget.getOrderKey();

				createStopAndTargetOrder(getOpenPositionOrder(), 2, 2, tgt2Qty,
						true);
				// createStopAndTargetOrder(getOpenPositionOrder(), 2, 3,
				// tgt3Qty,
				// true);
				_log.info("Open position submit Stop/Tgt orders created Symbol: "
						+ getSymbol() + " Time:" + startPeriod);

			}

			/*
			 * Manage the stop orders if the current bars Vwap crosses the Vwap
			 * of the first 5min bar then move the stop price ( currently -2R)
			 * to the average fill price i.e. break even. This allows for tails
			 * that break the 5min high/low between 9:40 thru 10:30.
			 */

			if (startPeriod.before(TradingCalendar.getSpecificTime(startPeriod,
					10, 40))
					&& startPeriod.after(TradingCalendar.getSpecificTime(
							startPeriod, 9, 35))) {

				CandleItem firstCandle = this.getCandle(TradingCalendar
						.getSpecificTime(this.getTradestrategy()
								.getTradingday().getOpen(), startPeriod));

				if (Side.BOT.equals(getOpenTradePosition().getSide())) {
					if (currentCandle.getVwap() < firstCandle.getVwap()
							&& newBar) {
						Money stopPrice = addPennyAndRoundStop(this
								.getOpenPositionOrder().getAverageFilledPrice()
								.doubleValue(), getOpenTradePosition()
								.getSide(), Action.SELL, 0.01);
						moveStopOCAPrice(stopPrice, true);
						_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
								+ getSymbol() + " Time:" + startPeriod
								+ " Price: " + stopPrice + " first bar Vwap: "
								+ firstCandle.getVwap() + " Curr Vwap: "
								+ currentCandle.getVwap());
					}
				} else {

					if (currentCandle.getVwap() > firstCandle.getVwap()
							&& newBar) {
						Money stopPrice = addPennyAndRoundStop(this
								.getOpenPositionOrder().getAverageFilledPrice()
								.doubleValue(), getOpenTradePosition()
								.getSide(), Action.BUY, 0.01);
						moveStopOCAPrice(stopPrice, true);
						_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
								+ getSymbol() + " Time:" + startPeriod
								+ " Price: " + stopPrice + " first bar Vwap: "
								+ firstCandle.getVwap() + " Curr Vwap: "
								+ currentCandle.getVwap());
					}
				}
			}

			/*
			 * At 10:30 Move stop order to b.e. i.e. the average fill price of
			 * the open order.
			 */
			if (startPeriod.equals(TradingCalendar.getSpecificTime(startPeriod,
					10, 40)) && newBar) {

				_log.info("Rule move stop to b.e.. Symbol: " + getSymbol()
						+ " Time: " + startPeriod);
				String action = Action.SELL;
				double avgPrice = this.getOpenTradePosition()
						.getTotalBuyValue().doubleValue()
						/ this.getOpenTradePosition().getTotalBuyQuantity()
								.doubleValue();
				if (Side.SLD.equals(getOpenTradePosition().getSide())) {
					action = Action.BUY;
					avgPrice = this.getOpenTradePosition().getTotalSellValue()
							.doubleValue()
							/ this.getOpenTradePosition()
									.getTotalSellQuantity().doubleValue();
				}
				moveStopOCAPrice(
						addPennyAndRoundStop(avgPrice, getOpenTradePosition()
								.getSide(), action, 0.01), true);
			}
			// if (this.getTradeOrder(targetOneOrderKey).getIsFilled() &&
			// newBar) {
			//
			// _log.info("Rule move stop to b.e.. Symbol: " + getSymbol()
			// + " Time: " + startPeriod);
			// String action = Action.SELL;
			// if (Side.SLD.equals(getOpenTradePosition().getSide()))
			// action = Action.BUY;
			// moveStopOCAPrice(
			// addPennyAndRoundStop(
			// this.getTradeOrder(targetOneOrderKey)
			// .getAverageFilledPrice().doubleValue(),
			// getOpenTradePosition().getSide(), action, 0.01),
			// true);
			// }
			/*
			 * We have sold the first half of the position try to trail BH on
			 * Heikin-Ashi above target 1 with a two bar trail.
			 */
			if (this.getTradeOrder(targetOneOrderKey).getIsFilled() && newBar) {
				Money newStop = getHiekinAshiTrailStop(
						this.getStopPriceMinUnfilled(), 2);
				if (!newStop.equals(this.getStopPriceMinUnfilled())) {
					_log.info("PositionManagerStrategy HiekinAshiTrail: "
							+ getSymbol() + " Trail Price: " + newStop
							+ " Time: " + startPeriod + " Side: "
							+ this.getOpenTradePosition().getSide());
					// moveStopOCAPrice(newStop, true);
				}
			}

			/*
			 * We have sold the first half of the position try to trail BH on
			 * one minute bars.
			 */
			if (this.getTradeOrder(targetOneOrderKey).getIsFilled()) {
				Money newStop = getOneMinuteTrailStop(
						this.getStopPriceMinUnfilled(), currentCandle);
				if (!newStop.equals(this.getStopPriceMinUnfilled())) {
					_log.info("PositionManagerStrategy OneMinuteTrail: "
							+ getSymbol() + " Trail Price: " + newStop
							+ " Time: " + startPeriod + " Side: "
							+ this.getOpenTradePosition().getSide());
				//	moveStopOCAPrice(newStop, true);
				}
			}
			/*
			 * Close any opened positions with a market order at the end of the
			 * day.
			 */
			if (!currentCandle.getLastUpdateDate().before(
					TradingCalendar.getSpecificTime(
							currentCandle.getLastUpdateDate(), 15, 58))) {
				cancelOrdersClosePosition(true);
				_log.info("PositionManagerStrategy 15:58:00 done: "
						+ getSymbol() + " Time: " + startPeriod);
				this.cancel();
			}
		} catch (StrategyRuleException ex) {
			_log.error("Error Position Manager exception: " + ex.getMessage(),
					ex);
			error(1,
					40,
					"Error Position Manager exception: "
							+ ex.getLocalizedMessage());
		}
	}

	/**
	 * Method getHiekinAshiTrailStop.
	 * 
	 * * This method is used to trail on Heikin-Ashi bars. Note trail is on the
	 * low/high of the bar and assumes the bar are in the direction of the trade
	 * i.e. side.
	 * 
	 * @param stopPrice
	 *            Money
	 * @param bars
	 *            int
	 * @return Money new stop or orginal if not trail.
	 * @throws StrategyRuleException
	 */
	public Money getHiekinAshiTrailStop(Money stopPrice, int bars)
			throws StrategyRuleException {
		boolean trail = false;

		HeikinAshiDataset dataset = (HeikinAshiDataset) getTradestrategy()
				.getDatasetContainer().getIndicatorByType(
						IndicatorSeries.HeikinAshiSeries);
		if (null == dataset) {
			throw new StrategyRuleException(1, 110,
					"Error no Hiekin-Ashi indicator defined for this strategy");
		}
		HeikinAshiSeries series = dataset.getSeries(0);
		// Start with the previous bar and work back
		int itemCount = series.getItemCount() - 2;

		if (itemCount > (2 + bars) && this.isThereOpenPosition()) {
			itemCount = itemCount - 2;
			for (int i = itemCount; i > (itemCount - bars); i--) {
				HeikinAshiItem candle = (HeikinAshiItem) series.getDataItem(i);
				// AbstractStrategyRule.logCandle(candle.getCandle());
				trail = false;
				if (Side.BOT.equals(this.getOpenTradePosition().getSide())) {
					if ((candle.getLow() > stopPrice.doubleValue())
							&& (candle.getOpen() < candle.getClose())) {
						stopPrice = new Money(candle.getLow());
						trail = true;
					}
				} else {
					if ((candle.getHigh() < stopPrice.doubleValue())
							&& (candle.getOpen() > candle.getClose())) {
						stopPrice = new Money(candle.getHigh());
						trail = true;
					}
				}
				if (!trail) {
					break;
				}
			}
		}
		return stopPrice;
	}

	/**
	 * Method getOneMinuteTrailStop.
	 * 
	 * This method is used to trail on one minute bars over the first target.
	 * 
	 * @param stopPrice
	 *            Money
	 * @param bars
	 *            int
	 * @return Money new stop or orginal if not trail.
	 * @throws StrategyRuleException
	 */
	Money candleHighLow = null;

	public Money getOneMinuteTrailStop(Money stopPrice, CandleItem currentCandle)
			throws StrategyRuleException {

		if (null == candleHighLow) {
			if (Side.BOT.equals(this.getOpenTradePosition().getSide())) {
				candleHighLow = new Money(0);
			} else {
				candleHighLow = new Money(Double.MAX_VALUE);
			}
		}

		if (Side.BOT.equals(this.getOpenTradePosition().getSide())) {
			if (currentCandle.getLow() > candleHighLow.doubleValue())
				candleHighLow = new Money(currentCandle.getLow());
			if (stopPrice.isLessThan(candleHighLow)
					&& (59 == TradingCalendar.getSecond(currentCandle
							.getLastUpdateDate())))
				return candleHighLow;
		} else {
			if (currentCandle.getHigh() < candleHighLow.doubleValue())
				candleHighLow = new Money(currentCandle.getHigh());
			if (stopPrice.isGreaterThan(candleHighLow)
					&& (59 == TradingCalendar.getSecond(currentCandle
							.getLastUpdateDate())))
				return candleHighLow;
		}

		return stopPrice;
	}

}
