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
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */

public class StrategyRuleTemplate extends AbstractStrategyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2281013751087462982L;
	private final static Logger _log = LoggerFactory
			.getLogger(StrategyRuleTemplate.class);

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

	public StrategyRuleTemplate(BrokerModel brokerManagerModel,
			StrategyData strategyData, Integer idTradestrategy) {
		super(brokerManagerModel, strategyData, idTradestrategy);
	}

	/**
	 * Method runStrategy. Note the current candle is just forming Enter a tier
	 * 1-3 gap in first 5min bar direction, with a 3R target and stop @ 5min
	 * high/low.
	 * 
	 * TradeOrders create TradePositions that are associated to Contracts.
	 * TradeOrders are associated to TradePositions and the Tradestrategy that
	 * created them. A TradePosition may have TradeOrders from multiple
	 * Tradestrategies.
	 * 
	 * TradePositions are created when there is no open TradePosition and a
	 * TradeOrder is either filled or partially filled.
	 * 
	 * Note TradePositions are closed when the open quantity is zero. The new
	 * TradePosition is associated to the Contract with the 1 to 1 relationship
	 * from Contract to TradePosition. The TradeOrder that opened the
	 * TradePosition is marked as the open order @see
	 * org.trade.persistent.dao.TradeOrder.getIsOpenPosition()
	 * 
	 * TradePosition will have the Side set to either BOT/SLD i.e. Long/Short.
	 * If an open position changes from Long to Short dues to an over Sell/Buy
	 * order the side will switch.
	 * 
	 * 
	 * @param candleSeries
	 *            CandleSeries the series of candles that has been updated.
	 * @param newBar
	 *            boolean has a new bar just started.
	 * @see org.trade.strategy.AbstractStrategyRule#runStrategy(CandleSeries,
	 *      boolean)
	 */
	public void runStrategy(CandleSeries candleSeries, boolean newBar) {

		try {
			// Get the current candle
			CandleItem currentCandleItem = this.getCurrentCandle();
			Date startPeriod = currentCandleItem.getPeriod().getStart();

			/*
			 * Position is open kill this Strategy as its job is done. In this
			 * example we would manage the position with a strategy manager.
			 * This strategy is just used to create the order that would open
			 * the position.
			 */
			if (this.isThereOpenPosition()) {
				_log.info("Strategy complete open position filled symbol: "
						+ getSymbol() + " startPeriod: " + startPeriod);
				/*
				 * If the order is partial filled check if the risk goes beyond
				 * 1 risk unit. If it does cancel the openPositionOrder this
				 * will cause it to be marked as filled.
				 */
				if (OrderStatus.PARTIALFILLED.equals(this
						.getOpenPositionOrder().getStatus())) {
					if (isRiskViolated(currentCandleItem.getClose(), this
							.getTradestrategy().getRiskAmount(), this
							.getOpenPositionOrder().getQuantity(), this
							.getOpenPositionOrder().getAverageFilledPrice())) {
						this.cancelOrder(this.getOpenPositionOrder());
					}
				}
				this.cancel();
				return;
			}

			/*
			 * Create code here to create orders based on your conditions/rules.
			 */

			if (startPeriod.equals(TradingCalendar.addMinutes(this
					.getTradestrategy().getTradingday().getOpen(), 5))
					&& newBar) {

				/*
				 * Example On start of the 9:35 candle check the 9:30 candle and
				 * buy over under in the direction of the bar.
				 */
			}

			/*
			 * Close any opened positions with a market order at the end of the
			 * day.
			 */
			if (!currentCandleItem.getLastUpdateDate().before(
					TradingCalendar.addMinutes(this.getTradestrategy()
							.getTradingday().getClose(), -5))) {
				cancelOrdersClosePosition(true);
				_log.info("Rule 15:55:00 close all open positions: "
						+ getSymbol() + " Time: " + startPeriod);
				this.cancel();
			}
		} catch (StrategyRuleException ex) {
			_log.error("Error  runRule exception: " + ex.getMessage(), ex);
			error(1, 10, "Error  runRule exception: " + ex.getMessage());
		}
	}
}
