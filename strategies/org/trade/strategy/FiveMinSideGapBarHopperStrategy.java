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

import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModel;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.TradeOrder;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class FiveMinSideGapBarHopperStrategy extends AbstractStrategyRule {

	/**
	 * 1/ Enter in the opposite direction of the previous day bar over the first
	 * 5 min bar.
	 * 
	 * 2/ Size with a stop at the first 5min bars Low (so for Long position
	 * Quantity= Risk/(High-Low) include rounding). Use a STPLMT order with a
	 * range from the Entry Limit table.
	 * 
	 * 3/ If the position is not filled by 15:30 cancel the order.
	 * 
	 * 4/ If the first 5min bar High/Low is broken (so for Side=Long a break of
	 * the Low) before entry, cancel the open order position.
	 * 
	 * 5/ Add 1c to the entry price and round over/under whole/half numbers in
	 * the direction of the trade, same for stop price.
	 * 
	 * E.g. If first 5min bar is H=21.50 L=21.15 Open= 21.2 Close=21.40 and the
	 * Side is Long, then position order will be Buy STPLMT=21.51-21.55 (STPLMT
	 * range 0.04 from EntryLimit table and as we are at 21.5 we also buy
	 * over/under whole/half numbers), Quantity=Risk/(21.51-21.2) rounded to
	 * +/-100 shares (see EntryLimit table).
	 * 
	 */

	private static final long serialVersionUID = -1629661431267666769L;
	private final static Logger _log = LoggerFactory.getLogger(FiveMinSideGapBarHopperStrategy.class);

	private String side = null;
	private Integer openPositionOrderKey = null;

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

	public FiveMinSideGapBarHopperStrategy(BrokerModel brokerManagerModel, StrategyData strategyData,
			Integer idTradestrategy) {
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
			// Get the current candle
			CandleItem currentCandleItem = this.getCurrentCandle();
			// AbstractStrategyRule.logCandle(this,
			// currentCandleItem.getCandle());
			ZonedDateTime startPeriod = currentCandleItem.getPeriod().getStart();

			/*
			 * Trade is open kill this Strategy as its job is done.
			 */
			if (this.isThereOpenPosition()) {
				_log.info("Strategy complete open position filled symbol: " + getSymbol() + " startPeriod: "
						+ startPeriod);
				/*
				 * If the order is partial filled check and if the risk goes
				 * beyond 1 risk unit cancel the openPositionOrder this will
				 * cause it to be marked as filled.
				 */
				if (OrderStatus.PARTIALFILLED.equals(this.getOpenPositionOrder().getStatus())) {
					if (isRiskViolated(currentCandleItem.getClose(), this.getTradestrategy().getRiskAmount(),
							this.getOpenPositionOrder().getQuantity(),
							this.getOpenPositionOrder().getAverageFilledPrice())) {
						this.cancelOrder(this.getOpenPositionOrder());
					}
				}
				this.cancel();
				return;
			}

			/*
			 * Open position order was cancelled kill this Strategy as its job
			 * is done.
			 */
			if (null != openPositionOrderKey && !this.getTradeOrder(openPositionOrderKey).isActive()) {
				_log.info("Strategy complete open position cancelled symbol: " + getSymbol() + " startPeriod: "
						+ startPeriod);
				updateTradestrategyStatus(TradestrategyStatus.CANCELLED);
				this.cancel();
				return;
			}

			/*
			 * If the 5min low is broken cancel orders as trade no longer valid.
			 */

			CandleItem openCandle = this.getCandle(this.getTradestrategy().getTradingday().getOpen());

			/*
			 * Is it the the 9:35 candle? and we have not created an open
			 * position trade.
			 */
			if (startPeriod.equals(this.getTradestrategy().getTradingday().getOpen()
					.plusMinutes(this.getTradestrategy().getBarSize() / 60)) && newBar) {

				ZonedDateTime preTradingDate = TradingCalendar
						.getPrevTradingDay(this.getTradestrategy().getTradingday().getOpen());

				ZonedDateTime prevDayStart = TradingCalendar.getTradingDayStart(preTradingDate);
				ZonedDateTime prevDayEnd = TradingCalendar.getTradingDayEnd(preTradingDate);
				// _log.error("prevDayStart: " + prevDayStart
				// + " prevDayEnd: " + prevDayEnd);

				Candle prevDayCandle = candleSeries.getBar(prevDayStart, prevDayEnd);

				AbstractStrategyRule.logCandle(this, prevDayCandle);

				if (prevDayCandle.getSide() && openCandle.getOpen() < prevDayCandle.getLow().doubleValue()) {
					side = Side.SLD;
				}

				if (!prevDayCandle.getSide() && openCandle.getOpen() > prevDayCandle.getHigh().doubleValue()) {
					side = Side.BOT;
				}

				if (null == side) {
					_log.info("Strategy complete no prev day bar hop symbol: " + getSymbol() + " startPeriod: "
							+ startPeriod);
					this.cancel();
					return;
				}

				CandleItem prevCandleItem = null;
				if (getCurrentCandleCount() > 0) {
					prevCandleItem = (CandleItem) candleSeries.getDataItem(getCurrentCandleCount() - 1);
					// AbstractStrategyRule
					// .logCandle(this, prevCandleItem.getCandle());
				}

				Money price = new Money(prevCandleItem.getHigh());
				Money priceStop = new Money(prevCandleItem.getLow());
				String action = Action.BUY;
				if (Side.SLD.equals(side)) {
					price = new Money(prevCandleItem.getLow());
					priceStop = new Money(prevCandleItem.getHigh());
					action = Action.SELL;
				}

				_log.info(" We have a trade!!  Symbol: " + getSymbol() + " Time: " + startPeriod);

				/*
				 * Create an open position.
				 */
				TradeOrder tradeOrder = createRiskOpenPosition(action, price, priceStop, true, null, null, null, null);
				openPositionOrderKey = tradeOrder.getOrderKey();

			} else {

				if (startPeriod.isBefore(this.getTradestrategy().getTradingday().getClose().minusMinutes(30))
						&& startPeriod.isAfter(this.getTradestrategy().getTradingday().getOpen().plusMinutes(5))) {

					CandleItem prevCandleItem = null;
					if (getCurrentCandleCount() > 0) {
						prevCandleItem = (CandleItem) candleSeries.getDataItem(getCurrentCandleCount() - 1);
						// AbstractStrategyRule
						// .logCandle(this, prevCandleItem.getCandle());
					}
					if (!this.isThereOpenPosition()) {
						if (Side.BOT.equals(this.side)) {
							if (openCandle.getLow() > prevCandleItem.getLow()) {
								_log.info("Rule 5min low broken. Symbol: " + getSymbol() + " Time: " + startPeriod);
								this.cancelAllOrders();
								updateTradestrategyStatus(TradestrategyStatus.FIVE_MIN_LOW_BROKEN);
								this.cancel();
								return;

							}
						}
						if (Side.SLD.equals(this.side)) {
							if (openCandle.getHigh() < prevCandleItem.getHigh()) {
								_log.info("Rule 5min high broken. Symbol: " + getSymbol() + " Time: " + startPeriod);
								this.cancelAllOrders();
								updateTradestrategyStatus(TradestrategyStatus.FIVE_MIN_HIGH_BROKEN);
								this.cancel();
								return;
							}
						}
					}
				}
			}

			if (!startPeriod.isBefore(this.getTradestrategy().getTradingday().getClose().minusMinutes(30))) {
				_log.info("Rule 15:30:00 bar, time out unfilled open position Symbol: " + getSymbol() + " Time: "
						+ startPeriod);
				if (!this.isThereOpenPosition()
						&& !TradestrategyStatus.CANCELLED.equals(getTradestrategy().getStatus())) {
					updateTradestrategyStatus(TradestrategyStatus.TO);
					this.cancelAllOrders();
					// No trade we timed out
					_log.info("Rule 11:30:00 bar, time out unfilled open position Symbol: " + getSymbol() + " Time: "
							+ startPeriod);
				}
				this.cancel();
			}
		} catch (StrategyRuleException ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 20, "Error  runRule exception: " + ex.getMessage());
		}
	}
}
