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

import java.util.Collections;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModel;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.OrderType;
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
public class PosMgrHeikinTrailStrategy extends AbstractStrategyRule {

	/**
	 * 
	 * 1/ trail the open position using prev two Heikin-Ashi bars. i.e. STP
	 * price is moved up to the Low of current Heikin-Ashi bar -2 as long as the
	 * prev bars low is higher than the prev bar -1 low.
	 * 
	 * 2/ Close any open positions at 15:58.
	 * 
	 */

	private static final long serialVersionUID = -6717691162128305191L;
	private final static Logger _log = LoggerFactory
			.getLogger(PosMgrHeikinTrailStrategy.class);

	/**
	 * Default Constructor Note if you use class variables remember these will
	 * need to be initialized if the strategy is restarted i.e. if they are
	 * created on startup under a constraint you must find a way to populate
	 * that value if the strategy were to be restarted and the constraint is not
	 * met.
	 * 
	 * @param brokerManagerModel
	 *            BrokerModel
	 * @param strategyData
	 *            StrategyData
	 * @param idTradestrategy
	 *            Integer
	 */

	public PosMgrHeikinTrailStrategy(BrokerModel brokerManagerModel,
			StrategyData strategyData, Integer idTradestrategy) {
		super(brokerManagerModel, strategyData, idTradestrategy);
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
			CandleItem currentCandleItem = this.getCurrentCandle();
			// AbstractStrategyRule.logCandle(this,
			// currentCandleItem.getCandle());
			Date startPeriod = currentCandleItem.getPeriod().getStart();

			/*
			 * Get the current open trade. If no trade is open this Strategy
			 * will be closed down.
			 */

			if (!this.isThereOpenPosition()) {
				_log.info("Reuest the open position Symbol: " + getSymbol()
						+ " Time:" + startPeriod);
				requestOrderExecutions(this.getTradestrategy());
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
				 * and stop orders for the open quantity. 
				 * 
				 * Make the stop -1R and manage to the Vwap MA of the opening
				 * bar.
				 */
				Integer quantity = this.getOpenPositionOrder()
						.getFilledQuantity();

				createStopAndTargetOrder(getOpenPositionOrder(), 1, 0.01, 8,
						0.01, quantity, true);

				_log.info("Open position submit Stop/Tgt orders created Symbol: "
						+ getSymbol() + " Time:" + startPeriod);

			}

			/*
			 * At 15:30 Move stop order to b.e. i.e. the average fill price of
			 * the open order.
			 */
			if (startPeriod.equals(TradingCalendar.addMinutes(this
					.getTradestrategy().getTradingday().getClose(), -30))
					&& newBar) {

				_log.info("Rule move stop to b.e.. Symbol: " + getSymbol()
						+ " Time: " + startPeriod);
				String action = Action.SELL;
				double avgPrice = this.getOpenTradePosition()
						.getTotalBuyValue().doubleValue()
						/ this.getOpenTradePosition().getTotalBuyQuantity()
								.doubleValue();

				CandleItem prevCandleItem = null;
				if (getCurrentCandleCount() > 0) {
					prevCandleItem = (CandleItem) candleSeries
							.getDataItem(getCurrentCandleCount() - 1);
					// AbstractStrategyRule
					// .logCandle(this, prevCandleItem.getCandle());
				}

				if (avgPrice < prevCandleItem.getLow())
					avgPrice = prevCandleItem.getLow();

				if (Side.SLD.equals(getOpenTradePosition().getSide())) {
					action = Action.BUY;
					avgPrice = this.getOpenTradePosition().getTotalSellValue()
							.doubleValue()
							/ this.getOpenTradePosition()
									.getTotalSellQuantity().doubleValue();

					if (avgPrice > prevCandleItem.getHigh())
						avgPrice = prevCandleItem.getHigh();
				}

				Money stopPrice = addPennyAndRoundStop(avgPrice,
						getOpenTradePosition().getSide(), action, 0.01);
				moveStopOCAPrice(stopPrice, true);
			}

			/*
			 * Trail on Heikin-Ashi above target 1 with a two bar trail.
			 */
			if (newBar) {
				Money newStop = getHiekinAshiTrailStop(
						this.getStopPriceMinUnfilled(), 2);
				if (!newStop.equals(this.getStopPriceMinUnfilled())) {
					_log.info("PositionManagerStrategy HiekinAshiTrail: "
							+ getSymbol() + " Trail Price: " + newStop
							+ " Time: " + startPeriod + " Side: "
							+ this.getOpenTradePosition().getSide());
					moveStopOCAPrice(newStop, true);
				}
			}

			/*
			 * Close any opened positions with a market order at the end of the
			 * day.
			 */
			if (!currentCandleItem.getLastUpdateDate().before(
					TradingCalendar.addMinutes(this.getTradestrategy()
							.getTradingday().getClose(), -2))) {
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
				.getStrategyData().getIndicatorByType(
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
	 * Method getTargetOneOrder.
	 * 
	 * This method is used to get target one order.
	 * 
	 * @return TradeOrder target one tradeOrder.
	 * @throws StrategyRuleException
	 */

	public TradeOrder getTargetOneOrder() {
		if (this.isThereOpenPosition()) {
			Collections.sort(this.getTradestrategyOrders().getTradeOrders(),
					TradeOrder.ORDER_KEY);
			for (TradeOrder tradeOrder : this.getTradestrategyOrders()
					.getTradeOrders()) {
				if (!tradeOrder.getIsOpenPosition()) {
					if (OrderType.LMT.equals(tradeOrder.getOrderType())
							&& null != tradeOrder.getOcaGroupName()) {
						return tradeOrder;
					}
				}
			}
		}
		return null;
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

	public Money getOneMinuteTrailStop(CandleSeries candleSeries,
			Money stopPrice, CandleItem currentCandle)
			throws StrategyRuleException {

		if (!(59 == TradingCalendar
				.getSecond(currentCandle.getLastUpdateDate())))
			return stopPrice;

		if (Side.BOT.equals(this.getOpenTradePosition().getSide())) {

			if (stopPrice.isLessThan(new Money(candleSeries
					.getPreviousRollingCandle().getVwap())))
				return new Money(candleSeries.getPreviousRollingCandle()
						.getVwap());

			if (candleSeries.getPreviousRollingCandle().getVwap() < candleSeries
					.getRollingCandle().getVwap())
				return new Money(candleSeries.getPreviousRollingCandle()
						.getVwap());

		} else {

			if (stopPrice.isGreaterThan(new Money(candleSeries
					.getPreviousRollingCandle().getVwap())))
				return new Money(candleSeries.getPreviousRollingCandle()
						.getVwap());

			if (candleSeries.getPreviousRollingCandle().getVwap() > candleSeries
					.getRollingCandle().getVwap())
				return new Money(candleSeries.getPreviousRollingCandle()
						.getVwap());
		}

		// if (Side.BOT.equals(this.getOpenTradePosition().getSide())) {
		// if (null == candleHighLow
		// || currentCandle.getLow() > candleHighLow.doubleValue())
		// candleHighLow = new Money(currentCandle.getLow());
		//
		// if (stopPrice.isLessThan(candleHighLow))
		// return candleHighLow;
		//
		// } else {
		// if (null == candleHighLow
		// || currentCandle.getHigh() < candleHighLow.doubleValue())
		// candleHighLow = new Money(currentCandle.getHigh());
		//
		// if (stopPrice.isGreaterThan(candleHighLow))
		// return candleHighLow;
		// }

		return stopPrice;
	}
}
