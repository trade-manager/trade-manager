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
package org.trade.persistent.dao;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Date;

import org.jfree.data.DataUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.TWSBrokerModel;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.PersistentModel;
import org.trade.ui.TradeAppLoadConfig;

import com.ib.client.Execution;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TradeOrderTest {

	private final static Logger _log = LoggerFactory
			.getLogger(TradeOrderTest.class);

	private String symbol = "TEST";
	private PersistentModel tradePersistentModel = null;
	private TradeOrderHome tradeOrderHome = null;
	private Tradestrategy tradestrategy = null;
	private Integer clientId = null;

	/**
	 * Method setUpBeforeClass.
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * Method setUp.
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		clientId = ConfigProperties.getPropAsInt("trade.tws.clientId");
		tradeOrderHome = new TradeOrderHome();
		this.tradePersistentModel = (PersistentModel) ClassFactory
				.getServiceForInterface(PersistentModel._persistentModel, this);
		this.tradestrategy = TradestrategyTest.getTestTradestrategy(symbol);
		assertNotNull(this.tradestrategy);
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		TradestrategyTest.clearDBData();
	}

	/**
	 * Method tearDownAfterClass.
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testAddTradeOrder() {

		try {
			String side = this.tradestrategy.getSide();
			String action = Action.BUY;
			if (Side.SLD.equals(side)) {
				action = Action.SELL;
			}

			double risk = this.tradestrategy.getRiskAmount().doubleValue();

			double stop = 0.20;
			BigDecimal price = new BigDecimal(20);
			int quantity = (int) ((int) risk / stop);
			Date createDate = TradingCalendar.addMinutes(this.tradestrategy
					.getTradingday().getOpen(), 5);

			TradeOrder tradeOrder = new TradeOrder(this.tradestrategy, action,
					OrderType.STPLMT, quantity, price,
					price.add(new BigDecimal(0.004)), new Date());
			tradeOrder.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			tradeOrder.setClientId(clientId);
			tradeOrder.setTransmit(true);
			tradeOrder.setStatus("SUBMITTED");
			tradeOrder.validate();
			tradeOrder = tradeOrderHome.persist(tradeOrder);
			assertNotNull(tradeOrder);
			_log.info("IdOrder: " + tradeOrder.getIdTradeOrder());

			TradeOrder tradeOrder1 = new TradeOrder(this.tradestrategy,
					Action.SELL, OrderType.STP, quantity,
					price.subtract(new BigDecimal(1)), null, createDate);

			tradeOrder1.setAuxPrice(price.subtract(new BigDecimal(1)));
			tradeOrder1.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			tradeOrder1.setClientId(clientId);
			tradeOrder1.setTransmit(true);
			tradeOrder1.setStatus("SUBMITTED");
			tradeOrder1.validate();
			tradeOrder1 = tradeOrderHome.persist(tradeOrder1);
			assertNotNull(tradeOrder1);

		} catch (Exception e) {
			fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testAddOpenStopTargetTradeOrder() {

		try {
			String side = this.tradestrategy.getSide();
			String action = Action.BUY;
			if (Side.SLD.equals(side)) {
				action = Action.SELL;
			}

			double risk = this.tradestrategy.getRiskAmount().doubleValue();

			double stop = 0.20;
			BigDecimal price = new BigDecimal(20);
			int quantity = (int) ((int) risk / stop);
			Date createDate = TradingCalendar.addMinutes(this.tradestrategy
					.getTradingday().getOpen(), 5);

			TradeOrder tradeOrder1 = new TradeOrder(this.tradestrategy, action,
					OrderType.STPLMT, quantity, price, price, createDate);
			tradeOrder1.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			tradeOrder1.setClientId(clientId);
			tradeOrder1.setOcaGroupName("");
			tradeOrder1.setTransmit(true);
			tradeOrder1.setStatus("SUBMITTED");
			tradeOrder1.validate();
			tradeOrder1 = tradeOrderHome.persist(tradeOrder1);
			int buySellMultiplier = 1;
			if (action.equals(Action.BUY)) {
				action = Action.SELL;

			} else {
				action = Action.BUY;
				buySellMultiplier = -1;
			}

			TradeOrder tradeOrder2 = new TradeOrder(this.tradestrategy, action,
					OrderType.LMT, quantity / 2, null,
					price.add(new BigDecimal((stop * 3) * buySellMultiplier)),
					createDate);

			tradeOrder2.setClientId(clientId);
			tradeOrder2.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			tradeOrder2.setOcaType(2);
			tradeOrder2.setOcaGroupName(this.tradestrategy.getIdTradeStrategy()
					+ "q1w2e3");
			tradeOrder2.setTransmit(true);
			tradeOrder2.setStatus("SUBMITTED");
			tradeOrder2.validate();
			tradeOrder2 = tradeOrderHome.persist(tradeOrder2);

			TradeOrder tradeOrder3 = new TradeOrder(this.tradestrategy, action,
					OrderType.LMT, quantity / 2, null,
					price.add(new BigDecimal((stop * 4) * buySellMultiplier)),
					createDate);

			tradeOrder3.setClientId(clientId);
			tradeOrder3.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			tradeOrder3.setOcaType(2);
			tradeOrder3.setOcaGroupName(this.tradestrategy.getIdTradeStrategy()
					+ "q1w2e3");
			tradeOrder3.setTransmit(true);
			tradeOrder3.setStatus("SUBMITTED");
			tradeOrder3.validate();
			tradeOrder3 = tradeOrderHome.persist(tradeOrder3);

			TradeOrder tradeOrder4 = new TradeOrder(this.tradestrategy, action,
					OrderType.STP, quantity, price.add(new BigDecimal(stop
							* buySellMultiplier * -1)), null, createDate);
			tradeOrder4.setLimitPrice(new BigDecimal(0));
			tradeOrder4.setAuxPrice(price.add(new BigDecimal(stop
					* buySellMultiplier * -1)));
			tradeOrder4.setClientId(clientId);
			tradeOrder4.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			tradeOrder4.setOcaType(2);
			tradeOrder4.setOcaGroupName(this.tradestrategy.getIdTradeStrategy()
					+ "q1w2e3");
			tradeOrder4.setTransmit(true);
			tradeOrder4.setStatus("SUBMITTED");
			tradeOrder4.validate();
			tradeOrder4 = tradeOrderHome.persist(tradeOrder4);

			_log.info("IdOrder: " + tradeOrder1.getIdTradeOrder());

		} catch (Exception e) {
			fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testAddTradeOrderFill() {

		try {
			testAddTradeOrder();
			boolean stopped = true;
			int minute = 25;

			for (TradeOrder tradeOrder : this.tradestrategy.getTradeOrders()) {

				tradeOrder = tradeOrderHome.findTradeOrderByKey(tradeOrder
						.getOrderKey());
				minute = minute + 3;
				Date filledDate = TradingCalendar.addMinutes(this.tradestrategy
						.getTradingday().getOpen(), minute);
				if (tradeOrder.getIsOpenPosition()) {

					TradeOrderfill orderfill = new TradeOrderfill(tradeOrder,
							"Paper", tradeOrder.getLimitPrice(),
							tradeOrder.getQuantity() / 2, "ISLAND", "1234",
							tradeOrder.getLimitPrice(),
							tradeOrder.getQuantity() / 2,
							this.tradestrategy.getSide(), filledDate);

					tradeOrder.addTradeOrderfill(orderfill);

					TradeOrderfill orderfill1 = new TradeOrderfill(tradeOrder,
							"Paper", tradeOrder.getLimitPrice(),
							tradeOrder.getQuantity() / 2, "ISLAND", "12345",
							tradeOrder.getLimitPrice(),
							tradeOrder.getQuantity() / 2,
							this.tradestrategy.getSide(),
							TradingCalendar.addMinutes(filledDate, 3));
					tradeOrder.addTradeOrderfill(orderfill1);
					tradeOrder.setIsFilled(true);
					tradeOrder.setFilledQuantity(tradeOrder.getQuantity());
					tradeOrder.setStatus("FILLED");
					tradeOrder.setFilledDate(TradingCalendar.addMinutes(
							filledDate, 3));
					tradeOrder
							.setAverageFilledPrice(tradeOrder.getLimitPrice());
					tradeOrder.setCommission(new BigDecimal(tradeOrder
							.getQuantity() * 0.005));

				} else {
					if (stopped) {
						if (OrderType.STP.equals(tradeOrder.getOrderType())) {

							TradeOrderfill orderfill = new TradeOrderfill(
									tradeOrder, "Paper",
									tradeOrder.getAuxPrice(),
									tradeOrder.getQuantity(), "ISLAND",
									"12345", tradeOrder.getAuxPrice(),
									tradeOrder.getQuantity(),
									this.tradestrategy.getSide(),
									TradingCalendar.addMinutes(filledDate, 5));
							tradeOrder.addTradeOrderfill(orderfill);
							tradeOrder.setIsFilled(true);
							tradeOrder.setStatus(OrderStatus.FILLED);
							tradeOrder.setAverageFilledPrice(tradeOrder
									.getAuxPrice());
							tradeOrder.setFilledDate(TradingCalendar
									.addMinutes(filledDate, 5));
							tradeOrder.setCommission(new BigDecimal(tradeOrder
									.getQuantity() * 0.005));
							tradeOrder.setFilledQuantity(tradeOrder
									.getQuantity());

						} else {
							tradeOrder.setStatus(OrderStatus.CANCELLED);
						}
					} else {
						if (OrderType.LMT.equals(tradeOrder.getOrderType())) {

							TradeOrderfill orderfill = new TradeOrderfill(
									tradeOrder, "Paper",
									tradeOrder.getLimitPrice(),
									tradeOrder.getQuantity() / 2, "ISLAND",
									"12345", tradeOrder.getLimitPrice(),
									tradeOrder.getQuantity() / 2,
									this.tradestrategy.getSide(),
									TradingCalendar.addMinutes(filledDate, 5));
							tradeOrder.addTradeOrderfill(orderfill);
							TradeOrderfill orderfill1 = new TradeOrderfill(
									tradeOrder, "Paper",
									tradeOrder.getLimitPrice(),
									tradeOrder.getQuantity() / 2, "ISLAND",
									"12345", tradeOrder.getLimitPrice(),
									tradeOrder.getQuantity() / 2,
									this.tradestrategy.getSide(),
									TradingCalendar.addMinutes(filledDate, 6));
							orderfill1.setTradeOrder(tradeOrder);
							tradeOrder.addTradeOrderfill(orderfill1);
							tradeOrder.setIsFilled(true);
							tradeOrder.setStatus(OrderStatus.FILLED);
							tradeOrder.setFilledQuantity(tradeOrder
									.getQuantity());
							tradeOrder.setFilledDate(TradingCalendar
									.addMinutes(filledDate, 15));
							tradeOrder.setAverageFilledPrice(tradeOrder
									.getLimitPrice());
							tradeOrder.setCommission(new BigDecimal(tradeOrder
									.getQuantity() * 0.005));

						} else {
							tradeOrder.setStatus(OrderStatus.CANCELLED);
						}
					}
				}

				tradeOrder = tradeOrderHome.persist(tradeOrder);
				_log.info("IdOrder: " + tradeOrder.getIdTradeOrder()
						+ " Action:" + tradeOrder.getAction() + " OrderType:"
						+ tradeOrder.getOrderType() + " Status:"
						+ tradeOrder.getStatus() + " filledDate:" + filledDate);
			}

		} catch (Exception e) {
			fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testAddDetachedTradeOrder() {
		try {
			String side = this.tradestrategy.getSide();
			String action = Action.BUY;
			if (Side.SLD.equals(side)) {
				action = Action.SELL;
			}

			TradeOrder tradeOrder = new TradeOrder(this.tradestrategy, action,
					OrderType.STPLMT, 100, new BigDecimal(20.20),
					new BigDecimal(20.23), new Date());
			tradeOrder.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			// Save new order with detached trade
			tradeOrder = tradeOrderHome.persist(tradeOrder);
			Execution execution = new Execution();
			execution.m_side = side;
			execution.m_time = TradingCalendar.getFormattedDate(new Date());
			execution.m_exchange = "ISLAND";
			execution.m_shares = tradeOrder.getQuantity();
			execution.m_price = tradeOrder.getLimitPrice().doubleValue();
			execution.m_avgPrice = tradeOrder.getLimitPrice().doubleValue();
			execution.m_cumQty = tradeOrder.getQuantity();
			execution.m_execId = "1234";
			TradeOrderfill orderfill = new TradeOrderfill();
			TWSBrokerModel.populateTradeOrderfill(execution, orderfill);
			orderfill.setTradeOrder(tradeOrder);
			tradeOrder.addTradeOrderfill(orderfill);
			// Save a detached order with a new order fill
			tradeOrder = tradeOrderHome.persist(tradeOrder);
			if (action.equals(Action.BUY)) {
				action = Action.SELL;

			} else {
				action = Action.BUY;
			}
			TradeOrder tradeOrder1 = new TradeOrder(this.tradestrategy, action,
					OrderType.LMT, 300, null, new BigDecimal(23.41), new Date());
			tradeOrder1.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			tradeOrder1 = tradeOrderHome.persist(tradeOrder1);

			Execution execution1 = new Execution();
			execution1.m_side = side;
			execution1.m_time = TradingCalendar.getFormattedDate(new Date());
			execution1.m_exchange = "ISLAND";
			execution1.m_shares = tradeOrder1.getQuantity();
			execution1.m_price = tradeOrder1.getLimitPrice().doubleValue();
			execution1.m_avgPrice = tradeOrder1.getLimitPrice().doubleValue();
			execution1.m_cumQty = tradeOrder1.getQuantity();
			execution1.m_execId = "1234";
			TradeOrderfill orderfill1 = new TradeOrderfill();
			TWSBrokerModel.populateTradeOrderfill(execution1, orderfill1);
			orderfill1.setTradeOrder(tradeOrder1);
			tradeOrder1.addTradeOrderfill(orderfill1);
			tradeOrder1 = tradeOrderHome.persist(tradeOrder1);

		} catch (Exception ex) {
			fail("Error adding row " + ex.getMessage());
		}
	}

	@Test
	public void testFindTradeOrderByMaxKey() {
		try {

			Integer orderKey = tradePersistentModel.findTradeOrderByMaxKey();
			_log.info("Max Order key: " + orderKey);

		} catch (Exception ex) {
			fail("Error adding row " + ex.getMessage());
		}
	}
}
