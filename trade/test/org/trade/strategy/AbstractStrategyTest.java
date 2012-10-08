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

import java.io.File;
import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BackTestBrokerModel;
import org.trade.broker.BrokerModel;
import org.trade.broker.BrokerModelException;
import org.trade.broker.TWSBrokerModel;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.DynamicCode;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.DAOEntryLimit;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.OverrideConstraints;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TimeInForce;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.dictionary.valuetype.TriggerMethod;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Trade;
import org.trade.persistent.dao.TradeAccount;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.ui.TradeAppLoadConfig;

import com.ib.client.Execution;

/**
 */
public class AbstractStrategyTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(AbstractStrategyTest.class);

	private BrokerModel m_brokerModel = null;
	private PersistentModel tradePersistentModel = null;
	private Tradestrategy tradestrategy = null;
	private String m_templateName = null;
	private String m_strategyDir = null;
	private StrategyRuleTest strategyProxy = null;

	/**
	 * Method setUp.
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		try {
			TradeAppLoadConfig.loadAppProperties();
			m_brokerModel = (BrokerModel) ClassFactory.getServiceForInterface(
					BrokerModel._brokerTest, this);
			tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			m_templateName = ConfigProperties
					.getPropAsString("trade.strategy.template");
			m_strategyDir = ConfigProperties
					.getPropAsString("trade.strategy.default.dir");
			Integer clientId = ConfigProperties
					.getPropAsInt("trade.tws.clientId");
			Integer port = new Integer(
					ConfigProperties.getPropAsString("trade.tws.port"));
			String host = ConfigProperties.getPropAsString("trade.tws.host");
			m_brokerModel.onConnect(host, port, clientId);
			this.tradestrategy = TradestrategyTest.getTestTradestrategy();
			this.strategyProxy = new StrategyRuleTest(m_brokerModel,
					this.tradestrategy.getDatasetContainer(),
					this.tradestrategy.getIdTradeStrategy());

			strategyProxy.execute();
			try {
				do {
					Thread.sleep(1000);
				} while (!strategyProxy.isWaiting());

			} catch (InterruptedException e) {
				_log.info(" Thread interupt: " + e.getMessage());
			}
			_log.info(" Test Initialized");

		} catch (Exception e) {
			fail("Error on setup " + e.getMessage());
		}
	}

	/**
	 * Method tearDown.
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		m_brokerModel.disconnect();
		strategyProxy.cancel();
		TradestrategyTest.removeTestTradestrategy();
	}

	@Test
	public void testEntryRuleNoEntryByRT() {

		try {
			tradestrategy.setTrade(true);

			Vector<Object> parm = new Vector<Object>(0);
			parm.add(m_brokerModel);
			parm.add(this.tradestrategy.getDatasetContainer());
			parm.add(this.tradestrategy.getIdTradeStrategy());
			DynamicCode dynacode = new DynamicCode();
			dynacode.addSourceDir(new File(m_strategyDir));
			StrategyRule strategyProxy = (StrategyRule) dynacode
					.newProxyInstance(StrategyRule.class, StrategyRule.PACKAGE
							+ m_templateName, parm);

			strategyProxy.execute();
			try {
				do {
					Thread.sleep(1000);
				} while (!strategyProxy.isWaiting());

			} catch (InterruptedException e) {
				_log.info(" Thread interupt: " + e.getMessage());
			}
			if (Side.BOT.equals(this.tradestrategy.getSide())) {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						this.tradestrategy.getTradingday(), 1,
						BarSize.FIVE_MIN, true, 0);

			} else {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						this.tradestrategy.getTradingday(), 1,
						BarSize.FIVE_MIN, false, 0);

			}
			strategyProxy.cancel();

		} catch (Exception ex) {
			fail("Error testEntryRuleNoEntryByRT Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testEntryRuleMoveStopToBE() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);

			Money price = new Money(37.99);
			TradeOrder openOrder = strategyProxy.createRiskOpenPosition(
					Action.BUY, price, price.subtract(new Money(0.2)), true);
			openOrder.setAverageFilledPrice(price.getBigDecimalValue());
			openOrder.setCommission((new Money(1.0)).getBigDecimalValue());
			openOrder.setFilledDate(new Date());
			openOrder.setIsFilled(true);
			openOrder.setFilledQuantity(openOrder.getQuantity());
			Execution execution = new Execution();
			execution.m_time = TradingCalendar.getFormattedDate(new Date());
			execution.m_exchange = "SMART";
			execution.m_side = "BOT";
			execution.m_shares = openOrder.getQuantity().intValue();
			execution.m_price = openOrder.getAverageFilledPrice().doubleValue();
			execution.m_avgPrice = openOrder.getAverageFilledPrice()
					.doubleValue();
			execution.m_cumQty = openOrder.getQuantity().intValue();
			execution.m_orderId = openOrder.getOrderKey();
			tradePersistentModel.persistTrade(openOrder.getTrade());

			((BackTestBrokerModel) m_brokerModel)
					.execDetails(openOrder.getOrderKey(), TWSBrokerModel
							.getIBContract(this.tradestrategy.getContract()),
							execution);

			// Position has been open
			// submit the target and stop orders.
			if (openOrder.getTrade().getIsOpen()) {
				if (null != openOrder.getTrade().getOpenQuantity()) {
					// Position has been opened
					// submit the target and stop orders.
					// Two targets at 3R and 6R
					_log.info("Open position submit Stop/Tgt orders Symbol: "
							+ openOrder.getTrade().getTradestrategy()
									.getContract().getSymbol());
					Money targetPrice = strategyProxy.createStopAndTargetOrder(

					strategyProxy.getOpenPositionOrder(), 1, 3, 50, true);
					strategyProxy.setTargetPrice(targetPrice);
					strategyProxy.createStopAndTargetOrder(

					strategyProxy.getOpenPositionOrder(), 1, 6, 50, true);
					for (TradeOrder newOrder : openOrder.getTrade()
							.getTradeOrders()) {
						if (OrderType.LMT.equals(newOrder.getOrderType())) {
							assertEquals(targetPrice.doubleValue(), newOrder
									.getLimitPrice().doubleValue(), 0);
						}

						if (OrderType.STPLMT.equals(newOrder.getOrderType())) {
							newOrder.setAverageFilledPrice(price
									.getBigDecimalValue());
						}
					}
				}
			}

			if (Side.BOT.equals(this.tradestrategy.getSide())) {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						this.tradestrategy.getTradingday(), 1,
						BarSize.FIVE_MIN, true, 0);
			} else {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						this.tradestrategy.getTradingday(), 1,
						BarSize.FIVE_MIN, false, 0);
			}

			strategyProxy.cancel();

		} catch (Exception ex) {
			fail("Error testEntryRuleMoveStopToBE Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testAddPennyAndRoundStop() {
		try {

			// Buy entry Long position
			Money price = new Money(19.99);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.BUY, 0);
			assertEquals(20.01, price.doubleValue(), 0);

			// Target Long position
			price = new Money(21.01);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			assertEquals(20.99, price.doubleValue(), 0);

			// Stop Long position
			price = new Money(19.01);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			assertEquals(18.99, price.doubleValue(), 0);

			// Short entry Short position
			price = new Money(24.01);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.SELL, 0);
			assertEquals(23.99, price.doubleValue(), 0);

			// Target Short position
			price = new Money(22.99);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			assertEquals(23.01, price.doubleValue(), 0);

			// Stop Short position
			price = new Money(24.99);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			assertEquals(25.01, price.doubleValue(), 0);

			// Buy entry Long position
			price = new Money(19.49);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.BUY, 0);
			assertEquals(19.51, price.doubleValue(), 0);

			// Target Long position
			price = new Money(21.51);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			assertEquals(21.49, price.doubleValue(), 0);

			// Stop Long position
			price = new Money(18.51);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			assertEquals(18.49, price.doubleValue(), 0);

			// Short entry short position
			price = new Money(24.51);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.SELL, 0);
			assertEquals(24.49, price.doubleValue(), 0);

			// Target Short position
			price = new Money(22.49);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			assertEquals(22.51, price.doubleValue(), 0);

			// Stop Short position
			price = new Money(25.49);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			assertEquals(25.51, price.doubleValue(), 0);

			// Short entry short position
			price = new Money(34.00);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.SELL, 0);
			assertEquals(33.99, price.doubleValue(), 0);

			// Target Short position
			price = new Money(32.00);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			assertEquals(32.01, price.doubleValue(), 0);

			// Stop Short position
			price = new Money(35.00);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			assertEquals(35.01, price.doubleValue(), 0);

			// Buy entry Long position
			price = new Money(19.19);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.BUY, 0);
			assertEquals(19.19, price.doubleValue(), 0);

			// Target Long position
			price = new Money(21.62);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			assertEquals(21.62, price.doubleValue(), 0);

			// Stop Long position
			price = new Money(18.57);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			assertEquals(18.57, price.doubleValue(), 0);

		} catch (Exception ex) {
			fail("Error testAddPennyAndRoundStop Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testClosePosition() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			this.strategyProxy.closePosition(true);
		} catch (Exception ex) {
			fail("Error testClosePosition Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateOrder() {
		try {
			TradeOrder result = this.strategyProxy.createOrder(Action.BUY,
					OrderType.STPLMT, new Money(100.04), new Money(100.01),
					1000, null, TriggerMethod.DEFAULT, OverrideConstraints.YES,
					TimeInForce.DAY, true, true);
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testCreateOrder Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateOpenPosition() {
		try {
			TradeOrder result = this.strategyProxy.createRiskOpenPosition(
					Action.BUY, new Money(100.00), new Money(99.00), true);
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testCreateOpenPosition Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCancelOrder() {
		try {
			this.createOpenPosition(new Money(37.99), false);
			this.strategyProxy.cancelOrder(this.strategyProxy
					.getOpenPositionOrder());
			pockStrategyRuleTest();
			assertEquals(OrderStatus.CANCELLED, this.strategyProxy
					.getOpenPositionOrder().getStatus());

		} catch (Exception ex) {
			fail("Error testCancelOrder Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testIsTradeConvered() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			boolean result = this.strategyProxy.isPositionConvered();
			assertFalse(result);
		} catch (Exception ex) {
			fail("Error testIsTradeConvered Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateStopAndTargetOrder() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			Money price = this.strategyProxy.createStopAndTargetOrder(
					new Money(99.0), new Money(103.99), 100, true);
			assertNotNull(price);
		} catch (Exception ex) {
			fail("Error testCreateStopAndTargetOrder Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateStopAndTargetOrderPercentQty() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			Money price = this.strategyProxy.createStopAndTargetOrder(

			this.strategyProxy.getOpenPositionOrder(), 2, 4, 50, true);
			assertNotNull(price);
		} catch (Exception ex) {
			fail("Error testCreateStopAndTargetOrderPercentQty Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testGetStopPriceForPositionRisk() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			Money price = this.strategyProxy.getStopPriceForPositionRisk(

			this.strategyProxy.getOpenPositionOrder(), 2);
			assertNotNull(price);
		} catch (Exception ex) {
			fail("Error testGetStopPriceForPositionRisk Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCloseAllOpenPositions() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			this.strategyProxy.closeAllOpenPositions();
		} catch (Exception ex) {
			fail("Error testCloseAllOpenPositions Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testMoveStopOCAPrice() {
		try {
			this.createOpenPosition(new Money(100), true);
			Money price = this.strategyProxy.createStopAndTargetOrder(
					new Money(99.0), new Money(103.99), this.strategyProxy
							.getOpenPositionOrder().getQuantity() / 2, true);
			assertNotNull(price);
			pockStrategyRuleTest();
			Money price1 = this.strategyProxy.createStopAndTargetOrder(
					new Money(99.0), new Money(105.99), this.strategyProxy
							.getOpenPositionOrder().getQuantity() / 2, true);
			assertNotNull(price1);
			pockStrategyRuleTest();
			this.strategyProxy.moveStopOCAPrice(new Money(this.strategyProxy
					.getTrade().getAveragePrice()), true);
		} catch (Exception ex) {
			fail("Error testMoveStopOCAPrice Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCancelAllOrders() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			this.strategyProxy.cancelAllOrders();
		} catch (Exception ex) {
			fail("Error testCancelAllOrders Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testIsTradeOpen() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			boolean result = this.strategyProxy.isPositionOpen();
			assertTrue(result);
		} catch (Exception ex) {
			fail("Error testIsTradeOpen Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testIsTradeCancelled() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			boolean result = this.strategyProxy.isPositionCancelled();
			assertFalse(result);
		} catch (Exception ex) {
			fail("Error testIsTradeCancelled Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetCurrentCandleCount() {
		try {
			if (Side.BOT.equals(this.tradestrategy.getSide())) {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						this.tradestrategy.getTradingday(), 1,
						BarSize.HOUR_MIN, true, 1);

			} else {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						this.tradestrategy.getTradingday(), 1,
						BarSize.HOUR_MIN, false, 1);

			}
			int count = this.strategyProxy.getCurrentCandleCount();
			assertEquals(-1, count);
		} catch (Exception ex) {
			fail("Error testGetCurrentCandleCount Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetCandle() {
		try {
			CandleItem candleItem = this.strategyProxy
					.getCandle(this.tradestrategy.getTradingday().getOpen());
			assertNotNull(candleItem);
		} catch (Exception ex) {
			// fail("Error testGetCandle Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testUpdateTradestrategyStatus() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			this.strategyProxy
					.updateTradestrategyStatus(TradestrategyStatus.CLOSED);
		} catch (Exception ex) {
			fail("Error testUpdateTradestrategyStatus Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetEntryLimit() {
		try {
			DAOEntryLimit result = this.strategyProxy.getEntryLimit();
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testCreateOrder Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetTradestrategy() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			Tradestrategy result = this.strategyProxy.getTradestrategy();
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testGetTradestrategy Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetTradeAccount() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			TradeAccount result = this.strategyProxy.getTradeAccount();
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testGetTradeAccount Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetTrade() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			Trade result = this.strategyProxy.getTrade();
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testGetTrade Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetSymbol() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			String result = this.strategyProxy.getSymbol();
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testGetSymbol Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetOpenPosition() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			TradeOrder result = this.strategyProxy.getOpenPositionOrder();
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testGetOpenPosition Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testIsThereOpenPosition() {
		try {
			this.createOpenPosition(new Money(37.99), true);
			boolean result = this.strategyProxy.isThereOpenPositionOrder();
			assertTrue(result);
		} catch (Exception ex) {
			fail("Error testIsThereOpenPosition Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetTargetPrice() {
		try {
			Money tgtPriceResult = this.strategyProxy.setTargetPrice(new Money(
					107.99));
			Money result = this.strategyProxy.getTargetPrice();
			assertEquals(tgtPriceResult, result);
		} catch (Exception ex) {
			fail("Error testGetTargetPrice Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testSetTargetPrice() {
		try {
			Money result = this.strategyProxy.setTargetPrice(new Money(107.99));
			assertNotNull(result);
		} catch (Exception ex) {
			fail("Error testSetTargetPrice Msg:" + ex.getMessage());
		}
	}

	/**
	 * Method createOpenPosition.
	 * @param price Money
	 * @param fillOpenPosition boolean
	 * @throws ValueTypeException
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 */
	private void createOpenPosition(Money price, boolean fillOpenPosition)
			throws ValueTypeException, BrokerModelException,
			PersistentModelException {
		if (!strategyProxy.getTradestrategy().getTrades().isEmpty()) {
			tradePersistentModel.removeTradestrategyTrades(strategyProxy
					.getTradestrategy());
		}

		TradeOrder openOrder = strategyProxy.createRiskOpenPosition(Action.BUY,
				price, price.subtract(new Money(0.2)), true);
		openOrder.setStatus(OrderStatus.SUBMITTED);
		if (fillOpenPosition) {
			openOrder.setCommission((new Money(1.0)).getBigDecimalValue());
			openOrder.setStatus(OrderStatus.FILLED);
			TradeOrderfill tradeOrderfill = new TradeOrderfill(openOrder,
					price.getBigDecimalValue(), openOrder.getQuantity(),
					Exchange.SMART, "12345rty6788", price.getBigDecimalValue(),
					openOrder.getQuantity(), openOrder.getTrade().getSide(),
					new Date());
			openOrder.addTradeOrderfill(tradeOrderfill);
		}
		openOrder = tradePersistentModel.persistTradeOrderfill(openOrder);

		pockStrategyRuleTest();
	}

	private void pockStrategyRuleTest() {
		/*
		 * Fire an event on the BaseCandleSeries this will trigger a refresh of
		 * the Trade in the StrategyRule. We need to wait until the StrategyRule
		 * is back in a wait state.
		 */
		strategyProxy.getTradestrategy().getDatasetContainer()
				.getBaseCandleSeries().fireSeriesChanged();
		try {
			do {
				Thread.sleep(200);
			} while (!strategyProxy.isWaiting());

		} catch (InterruptedException e) {
			_log.info(" Thread interupt: " + e.getMessage());
		}
	}

	/**
	 */
	public class StrategyRuleTest extends AbstractStrategyRule {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3345516391123859703L;

		/**
		 * Default Constructor
		 * 
		
		
		
		
		 * @param brokerManagerModel BrokerModel
		 * @param datasetContainer StrategyData
		 * @param idTradestrategy Integer
		 */

		public StrategyRuleTest(BrokerModel brokerManagerModel,
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
						_log.info("Strategy complete open position filled symbol: "
								+ getSymbol() + " startPeriod: " + startPeriod);
						this.cancel();
						return;
					}
					/*
					 * Only manage trades when the market is open and the candle
					 * is for this Tradestrategies trading day.
					 */
					if (TradingCalendar.isMarketHours(startPeriod)
							&& TradingCalendar.sameDay(getTradestrategy()
									.getTradingday().getOpen(), startPeriod)) {

						// _log.info(getTradestrategy().getStrategy().getClassName()
						// + " symbol: " + getSymbol() + " startPeriod: "
						// + startPeriod);

						// Is it the the 9:35 candle?
						if (startPeriod.equals(TradingCalendar.getSpecificTime(
								startPeriod, 9, 35)) && newBar) {

						} else if (startPeriod.equals(TradingCalendar
								.getSpecificTime(startPeriod, 10, 30))) {

						} else if (startPeriod.after(TradingCalendar
								.getSpecificTime(startPeriod, 10, 30))) {
							_log.info("Rule after 10:30:00 bar, close the "
									+ getTradestrategy().getStrategy()
											.getClassName() + " Symbol: "
									+ getSymbol());
							// Kill this process we are done!
							this.cancel();
						}
					}
				}
			} catch (Exception ex) {
				_log.error("Error  runRule exception: " + ex.getMessage(), ex);
				error(1, 10, "Error  runRule exception: " + ex.getMessage());
			}
		}
	}
}
