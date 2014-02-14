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
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 */
public class PosMgrFHXRBHYRStrategy extends AbstractStrategyRule {

	/**
	 * 1/ If the open position is filled create a STP and 2 Targets (LMT) OCA
	 * orders at xR and yR with 50% of the filled quantity for each. Use the
	 * open position fill quantity, price and stop price to determine the target
	 * price. The STP orders take an initial risk of 2R.
	 * 
	 * 2/ Target/Stop prices should be round over/under whole/half numbers when
	 * ever they are calculated..
	 * 
	 * 3/ After 9:35 and before 15:30 if the current VWAP crosses the 9:35
	 * candles VWAP move the STP price on each of the STP order to break even.
	 * 
	 * 4/ At 15:30 move the STP order to the average fill price of the filled
	 * open order.
	 * 
	 * 5/ Move stop to B.E when target one hit (Optional see code).
	 * 
	 * 6/ When target one hit trail back half(BH) on 1min bars (Optional).
	 * 
	 * 7/ Close any open positions at 15:58.
	 * 
	 */

	private static final long serialVersionUID = -6717691162128305191L;
	private final static Logger _log = LoggerFactory
			.getLogger(PosMgrFHXRBHYRStrategy.class);

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

	public PosMgrFHXRBHYRStrategy(BrokerModel brokerManagerModel,
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
			CandleItem prevCandleItem = null;
			Date startPeriod = currentCandleItem.getPeriod().getStart();
			if (newBar && getCurrentCandleCount() > 0) {
				prevCandleItem = (CandleItem) candleSeries
						.getDataItem(getCurrentCandleCount() - 1);
				// AbstractStrategyRule
				// .logCandle(this, prevCandleItem.getCandle());
			}

			// AbstractStrategyRule.logCandle(this,
			// currentCandleItem.getCandle());

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
				 * and stop orders for the open quantity. Two targets at 2R and
				 * 2R Stop and 2X actual stop this will be managed to 1R below
				 * 
				 * Make the stop -2R and manage to the Vwap MA of the opening
				 * bar.
				 */
				Integer quantity = this.getOpenPositionOrder()
						.getFilledQuantity();
				Integer tgt1Qty = quantity / 2;
				Integer tgt2Qty = quantity - tgt1Qty;
				// Integer tgt3Qty = quantity - (tgt1Qty + tgt2Qty);

				createStopAndTargetOrder(getOpenPositionOrder(), 2, 0.01, 6,
						0.01, tgt1Qty, true);

				createStopAndTargetOrder(getOpenPositionOrder(), 2, 0.01, 6,
						0.01, tgt2Qty, true);
				// createStopAndTargetOrder(getOpenPositionOrder(),
				// 2,0.01,4,0.01, tgt3Qty, true);
				_log.info("Open position submit Stop/Tgt orders created Symbol: "
						+ getSymbol() + " Time:" + startPeriod);

			}

			/*
			 * TODO We the first pivot as a point to take profits.
			 * 
			 * Note this is just an example need refining.
			 */

			// if
			// (startPeriod.after(TradingCalendar.getSpecificTime(startPeriod,
			// 9, 45)) && newBar) {
			// PivotDataset dataset = (PivotDataset) getTradestrategy()
			// .getStrategyData().getIndicatorByType(
			// IndicatorSeries.PivotSeries);
			// PivotSeries pivotSeries = dataset.getSeries(0);
			// // Start with the current bar and work back
			// int itemCount = pivotSeries.getItemCount() - 1;
			//
			// if (itemCount > 0 && this.isThereOpenPosition()) {
			// for (int i = itemCount; i > 1; i--) {
			// PivotItem pivot = (PivotItem) pivotSeries.getDataItem(i);
			// if (pivot
			// .getPeriod()
			// .getStart()
			// .after(TradingCalendar.getSpecificTime(
			// startPeriod, 9, 45))
			// && newBar) {
			// _log.error("startPeriod: "
			// + startPeriod
			// + " Pivot time: "
			// + pivot.getPeriod().getStart()
			// + " value: "
			// + pivot.getPivotPrice()
			// + " Time 9:45"
			// + TradingCalendar.getSpecificTime(
			// startPeriod, 9, 45));
			// double avgfillPrice = this.getOpenPositionOrder()
			// .getAverageFilledPrice().doubleValue();
			//
			// double stopPrice = this.getOpenPositionOrder()
			// .getStopPrice().doubleValue();
			//
			// if (Side.BOT.equals(getOpenTradePosition()
			// .getSide())) {
			// if (pivot.getPivotPrice() > avgfillPrice
			// && Side.SLD
			// .equals(pivot.getPivotSide())) {
			// moveStopOCAPrice(new Money(
			// currentCandleItem.getVwap()), true);
			// }
			// } else {
			// if (pivot.getPivotPrice() < avgfillPrice
			// && Side.BOT
			// .equals(pivot.getPivotSide())) {
			// moveStopOCAPrice(new Money(
			// currentCandleItem.getVwap()), true);
			// }
			// }
			// break;
			// } else {
			// break;
			// }
			// }
			// }
			// }

			/*
			 * TODO this check will plot the last three vwaps as y = a + bx +
			 * cx^2 with a correlation coeff of 0.6 then extrapolate the next
			 * vwap. If that is beyond the stop it will move the stop to b.e.
			 * 
			 * Note this is just an example need refining.
			 */

			// if
			// (startPeriod.after(TradingCalendar.getSpecificTime(startPeriod,
			// 9, 50)) && newBar) {
			//
			// _log.info("Symbol: " + this.getSymbol() + " Current Time: "
			// + currentCandleItem.getPeriod().getStart() + " vwap: "
			// + currentCandleItem.getVwap());
			// MatrixFunctions matrixFunctions = new MatrixFunctions();
			// int barBack = 3;
			// int polyOrder = 2;
			// double _minCorrelationCoeff = 0.6;
			// if (candleSeries.getItemCount() < barBack)
			// return;
			//
			// List<Pair> pairs = new ArrayList<Pair>();
			//
			// int startBar = candleSeries.indexOf(prevCandleItem.getPeriod())
			// - (barBack - 1);
			// Long startTime = ((CandleItem) candleSeries
			// .getDataItem(startBar)).getPeriod().getStart()
			// .getTime();
			// double prevY = Double.MAX_VALUE;
			// for (int i = startBar; i < (startBar + barBack); i++) {
			// CandleItem candleItem = (CandleItem) candleSeries
			// .getDataItem(i);
			// pairs.add(new Pair(
			// ((double) (candleItem.getPeriod().getStart()
			// .getTime() - startTime) / (1000 * 60 * 60)),
			// candleItem.getVwap()));
			// _log.info("Symbol: "
			// + this.getSymbol()
			// + " Time: "
			// + candleItem.getPeriod().getStart()
			// + " vwap: "
			// + candleItem.getVwap()
			// + " diff: "
			// + (prevY != Double.MAX_VALUE ? (candleItem
			// .getVwap() - prevY) : 0));
			// prevY = candleItem.getVwap();
			// }
			// Collections.sort(pairs, Pair.X_VALUE_ASC);
			// Pair[] pairsArray = pairs.toArray(new Pair[] {});
			// double[] terms = matrixFunctions.solve(pairsArray, polyOrder);
			// double correlationCoeff = matrixFunctions
			// .getCorrelationCoefficient(pairsArray, terms);
			// double standardError = matrixFunctions.getStandardError(
			// pairsArray, terms);
			// _log.info("Symbol: " + this.getSymbol() + " correlationCoeff: "
			// + correlationCoeff + " standardError: " + standardError);
			// if (correlationCoeff > _minCorrelationCoeff) {
			//
			// Entrylimit entryLimit = this.getEntryLimit().getValue(
			// new Money(prevCandleItem.getVwap()));
			//
			// Money pivotRange = new Money(
			// Math.abs((pairs.get(0).y - pairs.get(pairs.size() - 1).y)));
			// if (null != entryLimit
			// && (entryLimit.getPivotRange().doubleValue()) <= pivotRange
			// .doubleValue()) {
			//
			// Pair prevPair = null;
			// boolean biggerDiff = true;
			// // double diffAmt = entryLimit.getPivotRange()
			// // .doubleValue();
			// double diffAmt = Double.MAX_VALUE;
			// if (Side.BOT.equals(getOpenTradePosition().getSide()))
			// diffAmt = diffAmt * -1;
			//
			// for (Pair pair : pairs) {
			// if (null != prevPair) {
			// if (diffAmt != Double.MAX_VALUE) {
			// double diff = prevPair.y - pair.y;
			// if (diffAmt > (prevPair.y - pair.y)) {
			// biggerDiff = false;
			// break;
			// }
			// }
			// diffAmt = prevPair.y - pair.y;
			// }
			// prevPair = pair;
			// }
			// if (biggerDiff) {
			// double nextTime = (double) (currentCandleItem
			// .getPeriod().getStart().getTime() - startTime)
			// / (1000 * 60 * 60);
			// double nextY = MatrixFunctions.fx(nextTime, terms);
			//
			// pairs.add(new Pair(
			// ((double) (currentCandleItem.getPeriod()
			// .getStart().getTime() - startTime) / (1000 * 60 * 60)),
			// nextY));
			//
			// for (Pair pair : pairs) {
			// double y = MatrixFunctions.fx(pair.x, terms);
			// pair.y = y;
			// _log.info("Symbol: " + this.getSymbol()
			// + " New Values x: " + pair.x + " y: "
			// + pair.y);
			// }
			// double avgfillPrice = this.getOpenPositionOrder()
			// .getAverageFilledPrice().doubleValue();
			//
			// double stopPrice = this.getOpenPositionOrder()
			// .getStopPrice().doubleValue();
			//
			// _log.info("*** Symbol: " + this.getSymbol()
			// + " Move stop avgfillPrice: "
			// + avgfillPrice + " x: "
			// + currentCandleItem.getPeriod().getStart()
			// + " nextY: " + nextY);
			//
			// if (Side.BOT.equals(getOpenTradePosition()
			// .getSide())) {
			// if (nextY <= avgfillPrice) {
			// moveStopOCAPrice(new Money(
			// (avgfillPrice - stopPrice) / 2),
			// true);
			// }
			// } else {
			// if (nextY >= avgfillPrice) {
			// moveStopOCAPrice(new Money(
			// (stopPrice - avgfillPrice) / 2),
			// true);
			// }
			// }
			// }
			// }
			// }
			// }
			/*
			 * Manage the stop orders if the current bars Vwap crosses the Vwap
			 * of the first 5min bar then move the stop price ( currently -2R)
			 * to the average fill price i.e. break even. This allows for tails
			 * that break the 5min high/low between 9:40 thru 15:30.
			 */

			if (startPeriod.before(TradingCalendar.getSpecificTime(startPeriod,
					15, 30))
					&& startPeriod.after(TradingCalendar.getSpecificTime(
							startPeriod, 9, 35))) {

				CandleItem firstCandle = this.getCandle(TradingCalendar
						.getSpecificTime(this.getTradestrategy()
								.getTradingday().getOpen(), startPeriod));

				if (Side.BOT.equals(getOpenTradePosition().getSide())) {
					if (currentCandleItem.getVwap() < firstCandle.getVwap()) {
						Money stopPrice = addPennyAndRoundStop(this
								.getOpenPositionOrder().getAverageFilledPrice()
								.doubleValue(), getOpenTradePosition()
								.getSide(), Action.SELL, 0.01);
						moveStopOCAPrice(stopPrice, true);
						_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
								+ getSymbol() + " Time:" + startPeriod
								+ " Price: " + stopPrice + " first bar Vwap: "
								+ firstCandle.getVwap() + " Curr Vwap: "
								+ currentCandleItem.getVwap());
					}
				} else {

					if (currentCandleItem.getVwap() > firstCandle.getVwap()) {
						Money stopPrice = addPennyAndRoundStop(this
								.getOpenPositionOrder().getAverageFilledPrice()
								.doubleValue(), getOpenTradePosition()
								.getSide(), Action.BUY, 0.01);
						moveStopOCAPrice(stopPrice, true);
						_log.info("Move Stop to b.e. Strategy Mgr Symbol: "
								+ getSymbol() + " Time:" + startPeriod
								+ " Price: " + stopPrice + " first bar Vwap: "
								+ firstCandle.getVwap() + " Curr Vwap: "
								+ currentCandleItem.getVwap());
					}
				}
			}

			/*
			 * At 15:30 Move stop order to b.e. i.e. the average fill price of
			 * the open order.
			 */
			if (startPeriod.equals(TradingCalendar.getSpecificTime(startPeriod,
					15, 30)) && newBar) {

				_log.info("Rule move stop to b.e.. Symbol: " + getSymbol()
						+ " Time: " + startPeriod);
				String action = Action.SELL;
				double avgPrice = this.getOpenTradePosition()
						.getTotalBuyValue().doubleValue()
						/ this.getOpenTradePosition().getTotalBuyQuantity()
								.doubleValue();

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
			 * Move stock to b.e. when target one hit.
			 */

			if (null != getTargetOneOrder()) {
				if (this.getTargetOneOrder().getIsFilled() && newBar) {

					_log.info("Rule move stop to b.e. after target one hit Symbol: "
							+ getSymbol() + " Time: " + startPeriod);
					String action = Action.SELL;
					if (Side.SLD.equals(getOpenTradePosition().getSide()))
						action = Action.BUY;
					Money newStop = addPennyAndRoundStop(this
							.getTargetOneOrder().getAverageFilledPrice()
							.doubleValue(), getOpenTradePosition().getSide(),
							action, 0.01);

					if (!newStop.equals(this.getStopPriceMinUnfilled())) {
						// moveStopOCAPrice(newStop, true);
					}
				}
			}

			/*
			 * We have sold the first half of the position try to trail BH on
			 * one minute bars.
			 */
			if (null != getTargetOneOrder()) {
				if (this.getTargetOneOrder().getIsFilled()) {

					Money newStop = getOneMinuteTrailStop(candleSeries,
							this.getStopPriceMinUnfilled(), currentCandleItem);
					if (!newStop.equals(new Money(this
							.getStopPriceMinUnfilled()))) {
						_log.info("PositionManagerStrategy OneMinuteTrail: "
								+ getSymbol() + " Trail Price: " + newStop
								+ " Time: " + startPeriod + " Side: "
								+ this.getOpenTradePosition().getSide());
						// moveStopOCAPrice(newStop, true);
					}
				}
			}

			/*
			 * Close any opened positions with a market order at the end of the
			 * day.
			 */
			if (!currentCandleItem.getLastUpdateDate().before(
					TradingCalendar.getSpecificTime(
							currentCandleItem.getLastUpdateDate(), 15, 58))) {
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
