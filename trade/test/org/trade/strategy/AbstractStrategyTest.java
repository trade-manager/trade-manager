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
import java.math.BigDecimal;
import java.util.Date;
import java.util.Vector;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BackTestBrokerModel;
import org.trade.broker.BrokerModel;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.DynamicCode;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.DAOEntryLimit;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.OverrideConstraints;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TimeInForce;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.dictionary.valuetype.TriggerMethod;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Entrylimit;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class AbstractStrategyTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(AbstractStrategyTest.class);

	private String symbol = "TEST";
	private BrokerModel m_brokerModel = null;
	private PersistentModel tradePersistentModel = null;
	private Tradestrategy tradestrategy = null;
	private String m_templateName = null;
	private String m_strategyDir = null;
	private StrategyRuleTest strategyProxy = null;

	/**
	 * Method setUp.
	 * 
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
			this.tradestrategy = TradestrategyTest.getTestTradestrategy(symbol);
			TestCase.assertNotNull(this.tradestrategy);

			this.strategyProxy = new StrategyRuleTest(m_brokerModel,
					this.tradestrategy.getDatasetContainer(),
					this.tradestrategy.getIdTradeStrategy());
			TestCase.assertNotNull(this.strategyProxy);
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
			TestCase.fail("Error on setup " + e.getMessage());
		}
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		m_brokerModel.onDisconnect();
		strategyProxy.cancel();
		TradestrategyTest.removeTestTradestrategy(symbol);
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
			TestCase.fail("Error testEntryRuleNoEntryByRT Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testEntryRuleMoveStopToBE() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTradeOrders(this.tradestrategy);

			Money price = new Money(37.99);
			TradeOrder openOrder = strategyProxy.createRiskOpenPosition(
					Action.BUY, price, price.subtract(new Money(0.2)), true,
					null, null, null, null);

			TradeOrderfill execution = new TradeOrderfill();
			execution.setTradeOrder(openOrder);
			execution.setTime(new Date());
			execution.setExchange("SMART");
			execution.setSide(Side.BOT);
			execution.setQuantity(openOrder.getQuantity());
			execution.setAveragePrice(price.getBigDecimalValue());
			execution.setPrice(price.getBigDecimalValue());
			execution.setCumulativeQuantity(openOrder.getQuantity());

			((BackTestBrokerModel) m_brokerModel).execDetails(
					openOrder.getOrderKey(), this.tradestrategy.getContract(),
					execution);
			this.reFreshPositionOrders();

			TestCase.assertNotNull(strategyProxy.getOpenPositionOrder());
			/*
			 * Position has been open submit the target and stop orders.
			 */
			if (strategyProxy.isThereOpenPosition()) {
				if (null != strategyProxy.getOpenTradePosition()
						.getOpenQuantity()) {
					/*
					 * Position has been opened submit the target and stop
					 * orders. Two targets at 3R and 6R
					 */
					_log.info("Open position submit Stop/Tgt orders Symbol: "
							+ openOrder.getTradestrategy().getContract()
									.getSymbol());
					Money targetPrice = strategyProxy.createStopAndTargetOrder(
							strategyProxy.getOpenPositionOrder(), 1, 3, 50,
							true);
					strategyProxy.setTargetPrice(targetPrice);

					strategyProxy.createStopAndTargetOrder(
							strategyProxy.getOpenPositionOrder(), 1, 3, 50,
							true);

					for (TradeOrder newOrder : strategyProxy
							.getPositionOrders().getTradeOrders()) {
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
			TestCase.fail("Error testEntryRuleMoveStopToBE Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testAddPennyAndRoundStop() {
		try {

			// Buy entry Long position
			Money price = new Money(19.99);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.BUY, 0);
			TestCase.assertEquals(20.01, price.doubleValue(), 0);

			// Target Long position
			price = new Money(21.01);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			TestCase.assertEquals(20.99, price.doubleValue(), 0);

			// Stop Long position
			price = new Money(19.01);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			TestCase.assertEquals(18.99, price.doubleValue(), 0);

			// Short entry Short position
			price = new Money(24.01);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.SELL, 0);
			TestCase.assertEquals(23.99, price.doubleValue(), 0);

			// Target Short position
			price = new Money(22.99);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			TestCase.assertEquals(23.01, price.doubleValue(), 0);

			// Stop Short position
			price = new Money(24.99);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			TestCase.assertEquals(25.01, price.doubleValue(), 0);

			// Buy entry Long position
			price = new Money(19.49);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.BUY, 0);
			TestCase.assertEquals(19.51, price.doubleValue(), 0);

			// Target Long position
			price = new Money(21.51);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			TestCase.assertEquals(21.49, price.doubleValue(), 0);

			// Stop Long position
			price = new Money(18.51);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			TestCase.assertEquals(18.49, price.doubleValue(), 0);

			// Short entry short position
			price = new Money(24.51);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.SELL, 0);
			TestCase.assertEquals(24.49, price.doubleValue(), 0);

			// Target Short position
			price = new Money(22.49);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			TestCase.assertEquals(22.51, price.doubleValue(), 0);

			// Stop Short position
			price = new Money(25.49);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			TestCase.assertEquals(25.51, price.doubleValue(), 0);

			// Short entry short position
			price = new Money(34.00);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.SELL, 0);
			TestCase.assertEquals(33.99, price.doubleValue(), 0);

			// Target Short position
			price = new Money(32.00);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			TestCase.assertEquals(32.01, price.doubleValue(), 0);

			// Stop Short position
			price = new Money(35.00);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.SLD, Action.BUY, 0);
			TestCase.assertEquals(35.01, price.doubleValue(), 0);

			// Buy entry Long position
			price = new Money(19.19);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.BUY, 0);
			TestCase.assertEquals(19.19, price.doubleValue(), 0);

			// Target Long position
			price = new Money(21.62);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			TestCase.assertEquals(21.62, price.doubleValue(), 0);

			// Stop Long position
			price = new Money(18.57);
			price = strategyProxy.addPennyAndRoundStop(price.doubleValue(),
					Side.BOT, Action.SELL, 0);
			TestCase.assertEquals(18.57, price.doubleValue(), 0);

		} catch (Exception ex) {
			TestCase.fail("Error testAddPennyAndRoundStop Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testClosePosition() {
		try {
			createOpenBuyPosition(new Money(100), true);
			TradeOrder order = this.strategyProxy.closePosition(true);
			TestCase.assertNotNull(order);
		} catch (Exception ex) {
			TestCase.fail("Error testClosePosition Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateOrder() {
		try {
			TradeOrder result = this.strategyProxy.createOrder(Action.BUY,
					OrderType.STPLMT, new Money(100.04), new Money(100.01),
					1000, null, TriggerMethod.DEFAULT, OverrideConstraints.YES,
					TimeInForce.DAY, true, true, null, null, null, null);
			TestCase.assertNotNull(result);
		} catch (Exception ex) {
			TestCase.fail("Error testCreateOrder Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateRiskOpenPosition() {
		try {
			TradeOrder result = this.strategyProxy.createRiskOpenPosition(
					Action.BUY, new Money(100.00), new Money(99.00), true,
					null, null, null, null);
			TestCase.assertNotNull(result);
		} catch (Exception ex) {
			TestCase.fail("Error testCreateOpenPosition Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateRiskOpenPositionMargin() {
		try {
			/*
			 * Standard test account has $100,000 margin and % of Margin 50% i.e
			 * $50,000 with $100 risk. So 2cent stop after round over whole
			 * number give 3cent stop. Risk/Stop = 3333 shares * $20 = $66,666
			 * which is > than 50% of $100,000 So we should see it adjust to
			 * $50,000/$20.01 = 2498 rounded to nearest 100 i.e Quantity = 2500.
			 */
			Money price = new Money(20.00);
			DAOEntryLimit entryLimits = new DAOEntryLimit();
			Entrylimit entryLimit = entryLimits.getValue(price);
			entryLimit.setPercentOfMargin(new BigDecimal(0.5));
			tradePersistentModel.persistAspect(entryLimit);

			TradeOrder result = this.strategyProxy.createRiskOpenPosition(
					Action.BUY, new Money(20.00), new Money(19.98), true, null,
					null, null, null);

			TestCase.assertEquals(2500, result.getQuantity(), 0);
			entryLimit.setPercentOfMargin(new BigDecimal(0));
			entryLimit = (Entrylimit) tradePersistentModel
					.persistAspect(entryLimit);
			TestCase.assertEquals(entryLimit.getPercentOfMargin(),
					new BigDecimal(0));

		} catch (Exception ex) {
			TestCase.fail("Error testCreateRiskOpenPositionMargin Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testCreateRiskOpenPositionMargin1() {
		try {
			/*
			 * Standard test account has $100,000 margin and % of Margin 50% i.e
			 * $50,000 with $100 risk. So 2cent stop after round over whole
			 * number give 3cent stop. Risk/Stop = 3333 shares * $20 = $66,666
			 * which is > than 50% of $100,000 So we should see it adjust to
			 * $50,000/$20.01 = 2498 rounded to nearest 100 i.e Quantity = 2500.
			 */
			Money price = new Money(45.75);
			DAOEntryLimit entryLimits = new DAOEntryLimit();
			Entrylimit entryLimit = entryLimits.getValue(price);
			entryLimit.setPercentOfMargin(new BigDecimal(0.5));
			tradePersistentModel.persistAspect(entryLimit);

			TradeOrder openOrder = this.strategyProxy.createRiskOpenPosition(
					Action.SELL, new Money(45.75), new Money(46.00), true,
					null, null, null, null);

			TestCase.assertEquals(400, openOrder.getQuantity(), 0);

			TradeOrderfill orderFill = new TradeOrderfill(openOrder,
					new BigDecimal(45.74), openOrder.getQuantity(),
					this.tradestrategy.getContract().getExchange(), "1234567",
					new BigDecimal(45.74), openOrder.getQuantity(), Side.SLD,
					new Date());
			openOrder.addTradeOrderfill(orderFill);
			openOrder.setStatus(OrderStatus.FILLED);
			tradePersistentModel.persistTradeOrderfill(openOrder);

			reFreshPositionOrders();

			TestCase.assertNotNull(this.strategyProxy.getOpenPositionOrder());

			/*
			 * Position has been opened and not covered submit the target and
			 * stop orders for the open quantity. Two targets at 4R and 7R Stop
			 * and 2X actual stop this will be managed to 1R below
			 * 
			 * Make the stop -2R and manage to the Vwap MA of the opening bar.
			 */
			Money targetOnePrice = this.strategyProxy.createStopAndTargetOrder(
					this.strategyProxy.getOpenPositionOrder(), 1, 4, 50, true);
			TestCase.assertNotNull(targetOnePrice);
			this.strategyProxy.setTargetPrice(targetOnePrice);
			this.strategyProxy.createStopAndTargetOrder(
					this.strategyProxy.getOpenPositionOrder(), 1, 7, 50, true);
			for (TradeOrder order : this.strategyProxy.getTradestrategy()
					.getTradeOrders()) {
				_log.info("Key: " + order.getOrderKey() + " Qty: "
						+ order.getQuantity() + " Aux Price: "
						+ order.getAuxPrice() + " Lmt Price: "
						+ order.getLimitPrice() + " Stop Price: "
						+ order.getStopPrice());
			}
			TestCase.assertNotNull(this.strategyProxy.isPositionCovered());
			entryLimit.setPercentOfMargin(new BigDecimal(0));
			tradePersistentModel.persistAspect(entryLimit);

		} catch (Exception ex) {
			TestCase.fail("Error testCreateRiskOpenPositionMargin1 Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testCancelOrder() {
		try {
			this.tradestrategy = TradestrategyTest
					.removeTradeOrders(this.tradestrategy);

			Money price = new Money(37.99);
			TradeOrder openOrder = strategyProxy.createRiskOpenPosition(
					Action.BUY, price, price.subtract(new Money(0.2)), true,
					null, null, null, null);
			reFreshPositionOrders();
			this.strategyProxy.cancelOrder(openOrder);
			reFreshPositionOrders();
			openOrder = tradePersistentModel.findTradeOrderByKey(openOrder
					.getOrderKey());
			TestCase.assertEquals(OrderStatus.CANCELLED, openOrder.getStatus());

		} catch (Exception ex) {
			TestCase.fail("Error testCancelOrder Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testIsTradeConvered() {
		try {
			createOpenBuyPosition(new Money(100), false);
			TestCase.assertFalse(this.strategyProxy.isPositionCovered());
		} catch (Exception ex) {
			TestCase.fail("Error testIsTradeConvered Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCreateStopAndTargetOrder() {
		try {
			createOpenBuyPosition(new Money(100), true);
			Money price = this.strategyProxy.createStopAndTargetOrder(
					new Money(99.0), new Money(103.99), 100, true);
			TestCase.assertNotNull(price);
		} catch (Exception ex) {
			TestCase.fail("Error testCreateStopAndTargetOrder Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testCreateStopAndTargetOrderPercentQty() {
		try {
			createOpenBuyPosition(new Money(100), true);
			Money price = this.strategyProxy.createStopAndTargetOrder(
					this.strategyProxy.getOpenPositionOrder(), 2, 4, 50, true);
			TestCase.assertNotNull(price);
		} catch (Exception ex) {
			TestCase.fail("Error testCreateStopAndTargetOrderPercentQty Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testGetStopPriceForPositionRisk() {
		try {
			createOpenBuyPosition(new Money(100), true);
			Money price = this.strategyProxy.getStopPriceForPositionRisk(
					this.strategyProxy.getOpenPositionOrder(), 2);
			TestCase.assertNotNull(price);
		} catch (Exception ex) {
			TestCase.fail("Error testGetStopPriceForPositionRisk Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testCloseOpenPosition() {
		try {
			createOpenBuyPosition(new Money(100), true);
			this.strategyProxy.closeOpenPosition(true);
			this.reFreshPositionOrders();
			TestCase.assertTrue(this.strategyProxy.isPositionCovered());
		} catch (Exception ex) {
			TestCase.fail("Error testCloseAllOpenPositions Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testMoveStopOCAPrice() {
		try {
			this.createOpenBuyPosition(new Money(100), true);
			Money price = this.strategyProxy.createStopAndTargetOrder(
					new Money(99.0), new Money(103.99), this.strategyProxy
							.getOpenPositionOrder().getQuantity() / 2, true);
			TestCase.assertNotNull(price);
			reFreshPositionOrders();
			Money price1 = this.strategyProxy.createStopAndTargetOrder(
					new Money(99.0), new Money(105.99), this.strategyProxy
							.getOpenPositionOrder().getQuantity() / 2, true);
			TestCase.assertNotNull(price1);
			reFreshPositionOrders();
			double avgPrice = this.strategyProxy.getOpenTradePosition()
					.getTotalBuyValue().doubleValue()
					/ this.strategyProxy.getOpenTradePosition()
							.getTotalBuyQuantity();
			this.strategyProxy.moveStopOCAPrice(new Money(avgPrice), true);
			reFreshPositionOrders();
			TestCase.assertTrue(this.strategyProxy.isPositionCovered());
		} catch (Exception ex) {
			TestCase.fail("Error testMoveStopOCAPrice Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testCancelAllOrders() {
		try {
			createOpenBuyPosition(new Money(100), false);
			this.strategyProxy.cancelAllOrders();
			TestCase.assertFalse(this.strategyProxy.isThereOpenPosition());
		} catch (Exception ex) {
			TestCase.fail("Error testCancelAllOrders Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testIsTradeOpen() {
		try {
			createOpenBuyPosition(new Money(100), true);
			TestCase.assertTrue(this.strategyProxy.isThereOpenPosition());
		} catch (Exception ex) {
			TestCase.fail("Error testIsTradeOpen Msg:" + ex.getMessage());
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
			TestCase.assertEquals(-1, count);
		} catch (Exception ex) {
			TestCase.fail("Error testGetCurrentCandleCount Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testGetCandle() {
		try {
			this.tradestrategy.getDatasetContainer().buildCandle(
					this.tradestrategy.getTradingday().getOpen(), 100d, 101d,
					99d, 100d, 100000l, 100d, 100, 1, null);
			CandleItem candleItem = this.strategyProxy
					.getCandle(this.tradestrategy.getTradingday().getOpen());
			TestCase.assertNotNull(candleItem);
		} catch (Exception ex) {
			TestCase.fail("Error testGetCandle Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testUpdateTradestrategyStatus() {
		try {
			createOpenBuyPosition(new Money(100), false);
			this.strategyProxy
					.updateTradestrategyStatus(TradestrategyStatus.CLOSED);
		} catch (Exception ex) {
			TestCase.fail("Error testUpdateTradestrategyStatus Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testGetEntryLimit() {
		try {
			DAOEntryLimit result = this.strategyProxy.getEntryLimit();
			TestCase.assertNotNull(result);
		} catch (Exception ex) {
			TestCase.fail("Error testCreateOrder Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetTradestrategy() {
		try {
			createOpenBuyPosition(new Money(100), false);
			TestCase.assertNotNull(this.strategyProxy.getTradestrategy());
		} catch (Exception ex) {
			TestCase.fail("Error testGetTradestrategy Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetTradeAccount() {
		try {
			createOpenBuyPosition(new Money(100), false);
			TestCase.assertNotNull(this.strategyProxy.getIndividualAccount());
		} catch (Exception ex) {
			TestCase.fail("Error testGetTradeAccount Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetTradePosition() {
		try {
			createOpenBuyPosition(new Money(100), true);
			TestCase.assertNotNull(this.strategyProxy.getOpenTradePosition());
		} catch (Exception ex) {
			TestCase.fail("Error testGetTrade Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetSymbol() {
		try {
			createOpenBuyPosition(new Money(100), false);
			TestCase.assertNotNull(this.strategyProxy.getSymbol());
		} catch (Exception ex) {
			TestCase.fail("Error testGetSymbol Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testGetOpenPositionOrder() {
		try {
			createOpenBuyPosition(new Money(100), true);
			TestCase.assertNotNull(this.strategyProxy.getOpenPositionOrder());
		} catch (Exception ex) {
			TestCase.fail("Error testGetOpenPosition Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testHasActiveOrders() {
		try {
			createOpenBuyPosition(new Money(100), false);
			TestCase.assertTrue(this.strategyProxy.hasActiveOrders());
		} catch (Exception ex) {
			TestCase.fail("Error testIsThereOpenPosition Msg:"
					+ ex.getMessage());
		}
	}

	@Test
	public void testGetTargetPrice() {
		try {
			Money tgtPriceResult = this.strategyProxy.setTargetPrice(new Money(
					107.99));
			Money result = this.strategyProxy.getTargetPrice();
			TestCase.assertEquals(tgtPriceResult, result);
		} catch (Exception ex) {
			TestCase.fail("Error testGetTargetPrice Msg:" + ex.getMessage());
		}
	}

	@Test
	public void testSetTargetPrice() {
		try {
			Money result = this.strategyProxy.setTargetPrice(new Money(107.99));
			TestCase.assertNotNull(result);
		} catch (Exception ex) {
			TestCase.fail("Error testSetTargetPrice Msg:" + ex.getMessage());
		}
	}

	/**
	 * Method createOpenPosition.
	 * 
	 * @param price
	 *            Money
	 * @param fillOpenPosition
	 *            boolean
	 * 
	 * @throws StrategyRuleException
	 * @throws PersistentModelException
	 */
	private void createOpenBuyPosition(Money price, boolean fillOpenPosition)
			throws StrategyRuleException, PersistentModelException {
		if (!strategyProxy.getTradestrategy().getTradeOrders().isEmpty()) {
			tradePersistentModel.removeTradestrategyTradeOrders(strategyProxy
					.getTradestrategy());
		}
		TradeOrder tradeOrder = this.strategyProxy.createOrder(Action.BUY,
				OrderType.STPLMT, price, price.subtract(new Money(0.2)), 1000,
				null, TriggerMethod.DEFAULT, OverrideConstraints.YES,
				TimeInForce.DAY, true, true, null, null, null, null);

		if (fillOpenPosition) {
			String side = (Action.BUY.equals(tradeOrder.getAction()) ? Side.BOT
					: Side.SLD);

			TestCase.assertNotNull(tradeOrder);
			TradeOrderfill execution = new TradeOrderfill();
			execution.setTradeOrder(tradeOrder);
			execution.setTime(new Date());
			execution.setExchange(this.tradestrategy.getContract()
					.getExchange());
			execution.setSide(side);
			execution.setQuantity(tradeOrder.getQuantity());
			execution.setAveragePrice((new Money(100.02)).getBigDecimalValue());
			execution.setPrice((new Money(100.02)).getBigDecimalValue());
			execution.setCumulativeQuantity(tradeOrder.getQuantity());
			((BackTestBrokerModel) m_brokerModel).execDetails(
					tradeOrder.getOrderKey(), this.tradestrategy.getContract(),
					execution);
			this.reFreshPositionOrders();
			TestCase.assertNotNull(strategyProxy.getOpenPositionOrder());

		} else {

			((BackTestBrokerModel) m_brokerModel).orderStatus(
					tradeOrder.getOrderKey(), OrderStatus.SUBMITTED, 0, 0, 0,
					0, 0, 0, tradeOrder.getClientId(), tradeOrder.getWhyHeld());

		}
	}

	private void reFreshPositionOrders() {
		try {
			/*
			 * Fire an event on the BaseCandleSeries this will trigger a refresh
			 * of the Trade in the StrategyRule. We need to wait until the
			 * StrategyRule is back in a wait state.
			 */
			strategyProxy.reFreshPositionOrders();

		} catch (Exception e) {
			_log.info("Error refreshing Position Orders: " + e.getMessage());
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
		 * 
		 * 
		 * 
		 * 
		 * @param brokerManagerModel
		 *            BrokerModel
		 * @param datasetContainer
		 *            StrategyData
		 * @param idTradestrategy
		 *            Integer
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
		 * 
		 * @param candleSeries
		 *            CandleSeries
		 * @param newBar
		 *            boolean
		 * @see org.trade.strategy.StrategyRule#runStrategy(CandleSeries,
		 *      boolean)
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
					if (this.isThereOpenPosition()) {
						_log.info("Strategy complete open position filled symbol: "
								+ getSymbol() + " startPeriod: " + startPeriod);
						this.cancel();
						return;
					}
					/*
					 * Only manage trades when the market is open and the candle
					 * is for this Tradestrategies trading day.
					 */
					if (TradingCalendar.isMarketHours(getTradestrategy()
							.getTradingday().getOpen(), getTradestrategy()
							.getTradingday().getClose(), startPeriod)
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
