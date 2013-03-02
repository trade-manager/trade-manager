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
package org.trade.persistent;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.jfree.data.DataUtilities;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.TWSBrokerModel;
import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.AccountType;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.SECType;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.CodeType;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.PortfolioAccount;
import org.trade.persistent.dao.Rule;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Trade;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.TradelogReport;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.IndicatorDataset;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.candle.CandlePeriod;
import org.trade.ui.TradeAppLoadConfig;
import org.trade.ui.models.TradingdayTableModel;
import org.trade.ui.tables.TradingdayTable;

import com.ib.client.Execution;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TradePersistentModelTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(TradePersistentModelTest.class);

	private PersistentModel tradePersistentModel = null;
	private Tradestrategy tradestrategy = null;
	private Integer clientId = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		clientId = ConfigProperties.getPropAsInt("trade.tws.clientId");
		this.tradePersistentModel = (PersistentModel) ClassFactory
				.getServiceForInterface(PersistentModel._persistentModel, this);
		this.tradestrategy = TradestrategyTest.getTestTradestrategy();
		TestCase.assertNotNull(this.tradestrategy);
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		TradestrategyTest.removeTestTradestrategy();
	}

	@Test
	public void testAddTradestrategy() {

		try {

			Strategy strategy = (Strategy) DAOStrategy.newInstance()
					.getObject();
			Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
					.getObject();

			String symbol = "TEST1";
			Contract contract = new Contract(SECType.STOCK, symbol,
					Exchange.SMART, Currency.USD, null, null);

			Date open = TradingCalendar.getBusinessDayStart(TradingCalendar
					.getMostRecentTradingDay(new Date()));
			Date close = TradingCalendar.getBusinessDayEnd(open);
			Tradingdays tradingdays = this.tradePersistentModel
					.findTradingdaysByDateRange(open, open);
			Tradingday tradingday = tradingdays.getTradingday(open, close);
			if (null == tradingday) {
				tradingday = Tradingday.newInstance(open);
				tradingdays.add(tradingday);
			}

			Tradestrategy tradestrategy = new Tradestrategy(contract,
					tradingday, strategy, portfolio, new BigDecimal(100),
					"BUY", "0", true, ChartDays.TWO_DAYS, BarSize.FIVE_MIN);
			if (tradingday.existTradestrategy(tradestrategy)) {
				_log.info("Tradestrategy Sysmbol: "
						+ tradestrategy.getContract().getSymbol()
						+ " already exists.");
			} else {
				tradingday.addTradestrategy(tradestrategy);
				this.tradePersistentModel.persistTradingday(tradingday);
				_log.info("testTradingdaysSave IdTradeStrategy:"
						+ tradestrategy.getIdTradeStrategy());
			}
			tradingday.getTradestrategies().remove(tradestrategy);
			this.tradePersistentModel.persistTradingday(tradingday);
			_log.info("testTradingdaysRemoce IdTradeStrategy:"
					+ tradestrategy.getIdTradeStrategy());
			TestCase.assertNotNull(tradingday.getIdTradingDay());
		} catch (Exception e) {
			TestCase.fail("Error testAddTradestrategy Msg: " + e.getMessage());
		}
	}

	@Test
	public void testOpenTradeByTradestrategyId() {

		try {

			Trade trade = this.tradePersistentModel
					.findOpenTradeByTradestrategyId(this.tradestrategy
							.getIdTradeStrategy());
			if (null == trade) {
				trade = new Trade(this.tradestrategy, Side.BOT);
				trade.setIsOpen(true);
				this.tradePersistentModel.persistAspect(trade);
				trade = this.tradePersistentModel
						.findOpenTradeByTradestrategyId(this.tradestrategy
								.getIdTradeStrategy());
			}
			TestCase.assertNotNull(trade);

		} catch (Exception e) {
			TestCase.fail("Error testFindTradeByTradestrategyId Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testLifeCycleTradeOrder() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			String side = this.tradestrategy.getSide();
			String action = Action.BUY;
			if (side.equals(Side.SLD)) {
				action = Action.SELL;
			}

			Trade trade = new Trade(this.tradestrategy, side);
			this.tradestrategy.addTrade(trade);

			/*
			 * Create an order for the trade.
			 */
			double risk = this.tradestrategy.getRiskAmount().doubleValue();

			double stop = 0.20;
			BigDecimal price = new BigDecimal(20);
			int quantity = (int) ((int) risk / stop);
			Date createDate = TradingCalendar.addMinutes(trade
					.getTradestrategy().getTradingday().getOpen(), 5);
			TradeOrder tradeOrder = new TradeOrder(trade, Action.BUY,
					OrderType.STPLMT, quantity, price,
					price.add(new BigDecimal(4)), createDate);

			tradeOrder.setIsOpenPosition(true);
			tradeOrder.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			/*
			 * Save the trade order i.e. doPlaceOrder()
			 */
			tradeOrder = this.tradePersistentModel
					.persistTradeOrder(tradeOrder);
			TestCase.assertNotNull(tradeOrder.getIdTradeOrder());
			/*
			 * Update the order to Submitted via openOrder(), orderStatus
			 */
			TradeOrder tradeOrderOpenPosition = this.tradePersistentModel
					.findTradeOrderByKey(tradeOrder.getOrderKey());
			tradeOrderOpenPosition.setStatus(OrderStatus.SUBMITTED);

			tradeOrderOpenPosition = this.tradePersistentModel
					.persistTradeOrder(tradeOrderOpenPosition);
			TestCase.assertNotNull(tradeOrderOpenPosition.getIdTradeOrder());
			/*
			 * Fill the order via execDetails()
			 */
			TradeOrder tradeOrderFilled = this.tradePersistentModel
					.findTradeOrderByKey(tradeOrderOpenPosition.getOrderKey());
			Execution execution = new Execution();
			execution.m_side = "BOT";
			execution.m_time = TradingCalendar.getFormattedDate(new Date());
			execution.m_exchange = "ISLAND";
			execution.m_shares = tradeOrder.getQuantity();
			execution.m_price = tradeOrder.getLimitPrice().doubleValue();
			execution.m_avgPrice = tradeOrder.getLimitPrice().doubleValue();
			execution.m_cumQty = tradeOrder.getQuantity();
			execution.m_execId = "1234";
			TradeOrderfill tradeOrderfill = new TradeOrderfill();
			TWSBrokerModel.populateTradeOrderfill(execution, tradeOrderfill);
			tradeOrderfill.setTradeOrder(tradeOrderFilled);

			tradeOrderFilled.addTradeOrderfill(tradeOrderfill);
			tradeOrderFilled.setAverageFilledPrice(tradeOrderfill
					.getAveragePrice());
			tradeOrderFilled.setFilledQuantity(tradeOrderfill
					.getCumulativeQuantity());
			tradeOrderFilled.setFilledDate(tradeOrderfill.getTime());
			tradeOrderFilled = this.tradePersistentModel
					.persistTradeOrder(tradeOrderFilled);
			TestCase.assertNotNull(tradeOrderFilled.getTradeOrderfills().get(0)
					.getIdTradeOrderFill());

			/*
			 * Update the status to filled. Check to see if anything has changed
			 * as this method gets fired twice on order fills.
			 */
			TradeOrder tradeOrderFilledStatus = this.tradePersistentModel
					.findTradeOrderByKey(tradeOrder.getOrderKey());
			tradeOrderFilledStatus.setStatus(OrderStatus.FILLED);
			double commisionAmt = tradeOrderFilledStatus.getFilledQuantity() * 0.005d;

			if (OrderStatus.FILLED.equals(tradeOrderFilledStatus.getStatus())
					&& !tradeOrderFilledStatus.getIsFilled()
					&& !((new Money(commisionAmt)).equals(new Money(
							Double.MAX_VALUE)))) {
				tradeOrderFilledStatus.setIsFilled(true);
				tradeOrderFilledStatus.setCommission(new BigDecimal(
						commisionAmt));
				tradeOrderFilledStatus = this.tradePersistentModel
						.persistTradeOrder(tradeOrderFilledStatus);
			}

			/*
			 * Add the stop and target orders.
			 */
			Tradestrategy tradestrategyStpTgt = this.tradePersistentModel
					.findTradestrategyById(this.tradestrategy
							.getIdTradeStrategy());
			Trade openTrade = tradestrategyStpTgt.getOpenTrade();
			TestCase.assertNotNull("No open trade", openTrade);

			int buySellMultiplier = 1;
			if (action.equals(Action.BUY)) {
				action = Action.SELL;

			} else {
				action = Action.BUY;
				buySellMultiplier = -1;
			}

			TradeOrder tradeOrderTgt1 = new TradeOrder(openTrade, action,
					OrderType.LMT, quantity / 2, null,
					price.add(new BigDecimal((stop * 3) * buySellMultiplier)),
					createDate);

			tradeOrderTgt1.setClientId(clientId);
			tradeOrderTgt1.setOrderKey((new BigDecimal(
					(Math.random() * 1000000))).intValue());
			tradeOrderTgt1.setOcaType(2);
			tradeOrderTgt1.setOcaGroupName(this.tradestrategy
					.getIdTradeStrategy() + "q1w2e3");
			tradeOrderTgt1.setTransmit(true);
			tradeOrderTgt1.setStatus(OrderStatus.UNSUBMIT);

			tradeOrderTgt1 = this.tradePersistentModel
					.persistTradeOrder(tradeOrderTgt1);

			TradeOrder tradeOrderTgt2 = new TradeOrder(openTrade, action,
					OrderType.LMT, quantity / 2, null,
					price.add(new BigDecimal((stop * 4) * buySellMultiplier)),
					createDate);
			tradeOrderTgt2.setClientId(clientId);
			tradeOrderTgt2.setOrderKey((new BigDecimal(
					(Math.random() * 1000000))).intValue());
			tradeOrderTgt2.setOcaType(2);
			tradeOrderTgt2.setOcaGroupName(this.tradestrategy
					.getIdTradeStrategy() + "w2e3r4");
			tradeOrderTgt2.setTransmit(true);
			tradeOrderTgt2.setStatus(OrderStatus.UNSUBMIT);

			tradeOrderTgt2 = this.tradePersistentModel
					.persistTradeOrder(tradeOrderTgt2);

			TradeOrder tradeOrderStp1 = new TradeOrder(openTrade, action,
					OrderType.STP, quantity / 2, price.add(new BigDecimal(stop
							* buySellMultiplier * -1)), null, createDate);

			tradeOrderStp1.setClientId(clientId);
			tradeOrderStp1.setOrderKey((new BigDecimal(
					(Math.random() * 1000000))).intValue());
			tradeOrderStp1.setOcaType(2);
			tradeOrderStp1.setOcaGroupName(this.tradestrategy
					.getIdTradeStrategy() + "q1w2e3");
			tradeOrderStp1.setTransmit(true);
			tradeOrderStp1.setStatus(OrderStatus.UNSUBMIT);

			tradeOrderStp1 = this.tradePersistentModel
					.persistTradeOrder(tradeOrderStp1);

			TradeOrder tradeOrderStp2 = new TradeOrder(openTrade, action,
					OrderType.STP, quantity / 2, price.add(new BigDecimal(stop
							* buySellMultiplier * -1)), null, createDate);

			tradeOrderStp2.setClientId(clientId);
			tradeOrderStp2.setOrderKey((new BigDecimal(
					(Math.random() * 1000000))).intValue());
			tradeOrderStp2.setOcaType(2);
			tradeOrderStp2.setOcaGroupName(this.tradestrategy
					.getIdTradeStrategy() + "w2e3r4");
			tradeOrderStp2.setTransmit(true);
			tradeOrderStp2.setStatus(OrderStatus.UNSUBMIT);

			tradeOrderStp2 = this.tradePersistentModel
					.persistTradeOrder(tradeOrderStp2);

			/*
			 * Update Stop/target orders to Submitted.
			 */
			for (TradeOrder tradeOrderOca : openTrade.getTradeOrders()) {
				TradeOrder tradeOrderOcaUnsubmit = this.tradePersistentModel
						.findTradeOrderByKey(tradeOrderOca.getOrderKey());
				if (tradeOrderOcaUnsubmit.getStatus().equals(
						OrderStatus.UNSUBMIT)
						&& (null != tradeOrderOcaUnsubmit.getOcaGroupName())) {
					tradeOrderOcaUnsubmit.setStatus(OrderStatus.SUBMITTED);
					tradeOrderOcaUnsubmit = this.tradePersistentModel
							.persistTradeOrder(tradeOrderOcaUnsubmit);
				}
			}

			/*
			 * Fill the stop orders.
			 */
			for (TradeOrder tradeOrderOca : openTrade.getTradeOrders()) {
				TradeOrder tradeOrderOcaSubmit = this.tradePersistentModel
						.findTradeOrderByKey(tradeOrderOca.getOrderKey());
				if (OrderStatus.SUBMITTED.equals(tradeOrderOcaSubmit
						.getStatus())
						&& (null != tradeOrderOcaSubmit.getOcaGroupName())) {
					if (OrderType.STP
							.equals(tradeOrderOcaSubmit.getOrderType())) {
						Execution executionOCA = new Execution();
						executionOCA.m_side = tradeOrderOcaSubmit.getTrade()
								.getSide();
						executionOCA.m_time = TradingCalendar
								.getFormattedDate(new Date());
						executionOCA.m_exchange = "ISLAND";
						executionOCA.m_shares = tradeOrderOcaSubmit
								.getQuantity();
						executionOCA.m_price = tradeOrderOcaSubmit
								.getLimitPrice().doubleValue();
						executionOCA.m_avgPrice = tradeOrderOcaSubmit
								.getLimitPrice().doubleValue();
						executionOCA.m_cumQty = tradeOrderOcaSubmit
								.getQuantity();
						executionOCA.m_execId = "1234";
						TradeOrderfill tradeOrderfillOCA = new TradeOrderfill();
						TWSBrokerModel.populateTradeOrderfill(executionOCA,
								tradeOrderfillOCA);
						tradeOrderfillOCA.setTradeOrder(tradeOrderOcaSubmit);
						tradeOrderOcaSubmit
								.addTradeOrderfill(tradeOrderfillOCA);
						tradeOrderOcaSubmit
								.setAverageFilledPrice(tradeOrderfillOCA
										.getAveragePrice());
						tradeOrderOcaSubmit.setFilledQuantity(tradeOrderfillOCA
								.getCumulativeQuantity());
						tradeOrderOcaSubmit.setFilledDate(tradeOrderfillOCA
								.getTime());
						this.tradePersistentModel
								.persistTradeOrder(tradeOrderOcaSubmit);
						TestCase.assertNotNull(tradeOrderfillOCA
								.getIdTradeOrderFill());
					}
				}
			}
			/*
			 * Update Stop/target orders status to filled and cancelled.
			 */
			for (TradeOrder tradeOrderOca : openTrade.getTradeOrders()) {
				TradeOrder tradeOrderOcaSubmit = this.tradePersistentModel
						.findTradeOrderByKey(tradeOrderOca.getOrderKey());
				if (tradeOrderOcaSubmit.getStatus().equals(
						OrderStatus.SUBMITTED)
						&& (null != tradeOrderOcaSubmit.getOcaGroupName())) {
					if (tradeOrderOcaSubmit.getOrderType()
							.equals(OrderType.STP)) {

						tradeOrderOcaSubmit.setStatus(OrderStatus.FILLED);
						tradeOrderOcaSubmit
								.setCommission(new BigDecimal(
										tradeOrderOcaSubmit.getFilledQuantity() * 0.005d));
						tradeOrderOcaSubmit.setIsFilled(true);
					} else {
						tradeOrderOcaSubmit.setStatus(OrderStatus.CANCELLED);
					}
					tradeOrderOcaSubmit = this.tradePersistentModel
							.persistTradeOrder(tradeOrderOcaSubmit);

					if (!trade.getIsOpen()) {
						_log.info("Trade closed: "
								+ tradeOrderOcaSubmit.getTrade().getIdTrade());
					}
				}
			}

		} catch (Exception e) {
			TestCase.fail("Error testLifeCycleTradeOrder Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testPersistTradingday() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			this.tradePersistentModel.persistTradingday(this.tradestrategy
					.getTradingday());
			TestCase.assertNotNull(this.tradestrategy.getTradingday()
					.getIdTradingDay());
		} catch (Exception e) {
			TestCase.fail("Error testPersistTradingday Msg: " + e.getMessage());
		}
	}

	@Test
	public void testPersistTradestrategy() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Tradestrategy result = this.tradePersistentModel
					.persistTradestrategy(this.tradestrategy);
			TestCase.assertNotNull(result.getId());
		} catch (Exception e) {
			TestCase.fail("Error testPersistTradestrategy Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testPersistContract() {

		try {
			Contract result = this.tradePersistentModel
					.persistContract(this.tradestrategy.getContract());
			TestCase.assertNotNull(result.getId());
		} catch (Exception e) {
			TestCase.fail("Error testPersistContract Msg: " + e.getMessage());
		}
	}

	@Test
	public void testResetDefaultPortfolio() {

		try {
			this.tradePersistentModel.resetDefaultPortfolio(this.tradestrategy
					.getPortfolio());
			TestCase.assertTrue(this.tradestrategy.getPortfolio()
					.getIsDefault());
		} catch (Exception e) {
			TestCase.fail("Error testResetDefaultTradeAccount Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testResetDefaultAccount() {

		try {

			Account account = new Account("Test1", "DU12366",
					AccountType.INDIVIDUAL, Currency.USD, true);
			Portfolio portfolio = this.tradestrategy.getPortfolio();
			portfolio = this.tradePersistentModel.findPortfolioByName(portfolio
					.getName());
			Account defaultAccount = portfolio.getMasterAccount();
			PortfolioAccount portfolioAccount = new PortfolioAccount(portfolio,
					account);
			portfolio.getPortfolioAccounts().add(portfolioAccount);
			portfolio = (Portfolio) this.tradePersistentModel
					.persistAspect(portfolio);
			account = this.tradePersistentModel.findAccountByNumber(account
					.getAccountNumber());
			this.tradePersistentModel.resetDefaultAccount(
					this.tradestrategy.getPortfolio(), account);
			TestCase.assertTrue(account.getIsDefault());
			TestCase.assertFalse(defaultAccount.getIsDefault());
			for (PortfolioAccount pa : portfolio.getPortfolioAccounts()) {
				if (pa.getAccount().getAccountNumber()
						.equals(account.getAccountNumber())) {
					this.tradePersistentModel.removeAspect(pa);
					this.tradePersistentModel.removeAspect(account);
					break;
				}
			}
			defaultAccount.setIsDefault(true);
			tradePersistentModel.persistAspect(defaultAccount);
		} catch (Exception e) {
			TestCase.fail("Error testResetDefaultTradeAccount Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testPersistTradeOrder() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Trade trade = new Trade(this.tradestrategy, Side.BOT);
			TradeOrder tradeOrder = new TradeOrder(trade, Action.BUY,
					OrderType.MKT, 1000, null, null, new Date());
			tradeOrder.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			TradeOrder result = this.tradePersistentModel
					.persistTradeOrder(tradeOrder);
			TestCase.assertNotNull(result.getId());
		} catch (Exception e) {
			TestCase.fail("Error testPersistTradeOrder Msg: " + e.getMessage());
		}
	}

	@Test
	public void testPersistTradeOrderFilled() {

		try {

			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);

			Trade trade = new Trade(this.tradestrategy, Side.BOT);
			BigDecimal price = new BigDecimal(100.00);
			TradeOrder tradeOrderBuy = new TradeOrder(trade, Action.BUY,
					OrderType.STPLMT, 1000, price,
					price.add(new BigDecimal(2)), new Date());
			tradeOrderBuy
					.setOrderKey((new BigDecimal((Math.random() * 1000000)))
							.intValue());
			tradeOrderBuy = this.tradePersistentModel
					.persistTradeOrder(tradeOrderBuy);
			tradeOrderBuy.setStatus(OrderStatus.SUBMITTED);
			tradeOrderBuy = this.tradePersistentModel
					.persistTradeOrder(tradeOrderBuy);

			TradeOrderfill orderfill = new TradeOrderfill(tradeOrderBuy, price,
					tradeOrderBuy.getQuantity() / 2, "ISLAND", "1a", price,
					tradeOrderBuy.getQuantity() / 2,
					this.tradestrategy.getSide(), new Date());
			tradeOrderBuy.addTradeOrderfill(orderfill);

			tradeOrderBuy = this.tradePersistentModel
					.persistTradeOrderfill(tradeOrderBuy);

			TradeOrderfill orderfill1 = new TradeOrderfill(tradeOrderBuy,
					tradeOrderBuy.getLimitPrice(), tradeOrderBuy.getQuantity(),
					"BATS", "1b", tradeOrderBuy.getLimitPrice(),
					tradeOrderBuy.getQuantity() / 2,
					this.tradestrategy.getSide(), new Date());
			tradeOrderBuy.addTradeOrderfill(orderfill1);
			tradeOrderBuy.setCommission(new BigDecimal(5.0));

			tradeOrderBuy = this.tradePersistentModel
					.persistTradeOrderfill(tradeOrderBuy);

			TradeOrder tradeOrderSell = new TradeOrder(
					tradeOrderBuy.getTrade(), Action.SELL, OrderType.LMT,
					tradeOrderBuy.getQuantity(), null, new BigDecimal(105.00),
					new Date());
			tradeOrderSell.setOrderKey((new BigDecimal(
					(Math.random() * 1000000))).intValue());
			tradeOrderSell = this.tradePersistentModel
					.persistTradeOrder(tradeOrderSell);
			tradeOrderSell.setStatus(OrderStatus.SUBMITTED);
			tradeOrderSell = this.tradePersistentModel
					.persistTradeOrder(tradeOrderSell);

			TradeOrderfill orderfill2 = new TradeOrderfill(tradeOrderSell,
					tradeOrderSell.getLimitPrice(),
					tradeOrderSell.getQuantity() / 2, "ISLAND", "2a",
					tradeOrderSell.getLimitPrice(),
					tradeOrderSell.getQuantity() / 2,
					this.tradestrategy.getSide(), new Date());
			tradeOrderSell.addTradeOrderfill(orderfill2);
			tradeOrderSell = this.tradePersistentModel
					.persistTradeOrderfill(tradeOrderSell);

			TradeOrderfill orderfill3 = new TradeOrderfill(tradeOrderSell,
					tradeOrderSell.getLimitPrice(),
					tradeOrderSell.getQuantity(), "BATS", "2b",
					tradeOrderSell.getLimitPrice(),
					tradeOrderSell.getQuantity() / 2,
					this.tradestrategy.getSide(), new Date());
			tradeOrderSell.addTradeOrderfill(orderfill3);
			tradeOrderSell.setCommission(new BigDecimal(5.0));

			TradeOrder result = this.tradePersistentModel
					.persistTradeOrderfill(tradeOrderSell);
			TestCase.assertFalse(result.getTrade().getIsOpen());
			TestCase.assertEquals((new Money(4000.00)).getBigDecimalValue(),
					result.getTrade().getProfitLoss());
			TestCase.assertEquals((new Money(4.00)).getBigDecimalValue(),
					(new Money(result.getTrade().getAveragePrice()))
							.getBigDecimalValue());
			TestCase.assertEquals(new Integer(2000), result.getTrade()
					.getTotalQuantity());
			TestCase.assertEquals(new Integer(0), result.getTrade()
					.getOpenQuantity());

		} catch (Exception e) {
			TestCase.fail("Error testPersistTradeOrder Msg: " + e.getMessage());
		}
	}

	@Test
	public void testPersistTrade() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Trade trade = new Trade(this.tradestrategy, Side.BOT);
			Trade result = this.tradePersistentModel.persistTrade(trade);
			TestCase.assertNotNull(result.getId());
		} catch (Exception e) {
			TestCase.fail("Error testPersistTrade Msg: " + e.getMessage());
		}
	}

	@Test
	public void testPersistCandleSeries() {

		try {

			CandleSeries candleSeries = new CandleSeries(this.tradestrategy
					.getDatasetContainer().getBaseCandleSeries(),
					BarSize.FIVE_MIN, this.tradestrategy.getTradingday()
							.getOpen(), this.tradestrategy.getTradingday()
							.getClose());
			StrategyData.doDummyData(candleSeries,
					this.tradestrategy.getTradingday(), 5, BarSize.FIVE_MIN,
					true, 0);
			long timeStart = System.currentTimeMillis();
			this.tradePersistentModel.persistCandleSeries(candleSeries);
			_log.info("Total time: " + (System.currentTimeMillis() - timeStart)
					/ 1000);
			TestCase.assertFalse(candleSeries.isEmpty());
			TestCase.assertNotNull(((CandleItem) candleSeries.getDataItem(0))
					.getCandle().getIdCandle());
		} catch (Exception e) {
			TestCase.fail("Error testPersistCandleSeries Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testPersistCandleItem() {

		try {
			CandleItem candleItem = new CandleItem(
					this.tradestrategy.getContract(),
					this.tradestrategy.getTradingday(), new CandlePeriod(),
					100.23, 100.23, 100.23, 100.23, 10000000L, 100.23, 100,
					new Date());
			this.tradePersistentModel.persistCandleItem(candleItem);
			TestCase.assertNotNull(candleItem.getCandle().getIdCandle());
		} catch (Exception e) {
			TestCase.fail("Error testPersistCandleItem Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindAccountById() {

		try {
			Portfolio result = this.tradePersistentModel
					.findPortfolioById(this.tradestrategy.getPortfolio()
							.getIdPortfolio());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradeAccountById Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindAccountByNumber() {

		try {
			Account result = this.tradePersistentModel
					.findAccountByNumber(this.tradestrategy.getPortfolio()
							.getMasterAccount().getAccountNumber());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradeAccountByNumber Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindContractById() {

		try {
			Contract result = this.tradePersistentModel
					.findContractById(this.tradestrategy.getContract()
							.getIdContract());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindContractById Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindContractByUniqueKey() {

		try {
			Contract result = this.tradePersistentModel
					.findContractByUniqueKey(this.tradestrategy.getContract()
							.getSecType(), this.tradestrategy.getContract()
							.getSymbol(), this.tradestrategy.getContract()
							.getExchange(), this.tradestrategy.getContract()
							.getCurrency(), null);
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindContractByUniqueKey Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradestrategyByTradestrategy() {

		try {
			Tradestrategy result = this.tradePersistentModel
					.findTradestrategyById(this.tradestrategy);
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradestrategyByTradestrategy Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradestrategyById() {

		try {
			Tradestrategy result = this.tradePersistentModel
					.findTradestrategyById(this.tradestrategy
							.getIdTradeStrategy());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradestrategyById Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradestrategyByUniqueKeys() {

		try {
			Tradestrategy result = this.tradePersistentModel
					.findTradestrategyByUniqueKeys(this.tradestrategy
							.getTradingday().getOpen(), this.tradestrategy
							.getStrategy().getName(), this.tradestrategy
							.getContract().getIdContract(), this.tradestrategy
							.getPortfolio().getName());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradestrategyByUniqueKeys Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindAllTradestrategies() {

		try {
			List<Tradestrategy> result = this.tradePersistentModel
					.findAllTradestrategies();
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindAllTradestrategies Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradeById() {

		try {
			Trade trade = new Trade(this.tradestrategy,
					this.tradestrategy.getSide());
			Trade resultTrade = this.tradePersistentModel.persistTrade(trade);
			Trade result = this.tradePersistentModel.findTradeById(resultTrade
					.getIdTrade());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradeById Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindOpenTradeByTradestrategyId() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Trade trade = new Trade(this.tradestrategy, Side.BOT);
			trade.setIsOpen(true);
			tradePersistentModel.persistTrade(trade);
			Trade result = this.tradePersistentModel
					.findOpenTradeByTradestrategyId(this.tradestrategy
							.getIdTradeStrategy());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindOpenTradeByTradestrategyId Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradesByTradestrategyId() {

		try {
			List<Trade> result = this.tradePersistentModel
					.findTradesByTradestrategyId(this.tradestrategy
							.getIdTradeStrategy());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradesByTradestrategyId Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testRemoveTradingdayTrades() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Trade trade = new Trade(this.tradestrategy,
					this.tradestrategy.getSide());
			this.tradePersistentModel.persistTrade(trade);
			Tradingday result = this.tradePersistentModel
					.findTradingdayById(this.tradestrategy.getTradingday()
							.getIdTradingDay());
			TestCase.assertNotNull(result);
			this.tradePersistentModel.removeTradingdayTrades(result);
		} catch (Exception e) {
			TestCase.fail("Error testRemoveTradingdayTrades Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testRemoveTradestrategyTrades() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Trade trade = new Trade(this.tradestrategy,
					this.tradestrategy.getSide());
			this.tradePersistentModel.persistTrade(trade);
			Tradestrategy result = this.tradePersistentModel
					.findTradestrategyById(this.tradestrategy
							.getIdTradeStrategy());
			TestCase.assertNotNull(result);
			this.tradePersistentModel.removeTradestrategyTrades(result);
		} catch (Exception e) {
			TestCase.fail("Error testRemoveTradestrategyTrades Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradeOrderByKey() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Trade trade = new Trade(this.tradestrategy, Side.BOT);
			BigDecimal price = new BigDecimal(100.00);
			TradeOrder tradeOrder = new TradeOrder(trade, Action.BUY,
					OrderType.STPLMT, 1000, price,
					price.add(new BigDecimal(4)), new Date());
			tradeOrder.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			TradeOrder resultTradeOrder = this.tradePersistentModel
					.persistTradeOrder(tradeOrder);
			TradeOrder result = this.tradePersistentModel
					.findTradeOrderByKey(resultTradeOrder.getOrderKey());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradeOrderByKey Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradeOrderfillByExecId() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			Trade trade = new Trade(this.tradestrategy, Side.BOT);
			BigDecimal price = new BigDecimal(100.00);
			TradeOrder tradeOrder = new TradeOrder(trade, Action.BUY,
					OrderType.STPLMT, 1000, price,
					price.add(new BigDecimal(4)), new Date());
			tradeOrder.setOrderKey((new BigDecimal((Math.random() * 1000000)))
					.intValue());
			TradeOrderfill tradeOrderfill = new TradeOrderfill(tradeOrder,
					new BigDecimal(100.23), new Integer(1000), Exchange.SMART,
					"123efgr567", new BigDecimal(100.23), new Integer(1000),
					Side.BOT, new Date());
			tradeOrder.addTradeOrderfill(tradeOrderfill);
			TradeOrder resultTradeOrder = this.tradePersistentModel
					.persistTradeOrder(tradeOrder);
			TradeOrderfill result = this.tradePersistentModel
					.findTradeOrderfillByExecId(resultTradeOrder
							.getTradeOrderfills().get(0).getExecId());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradeOrderfillByExecId Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradeOrderByMaxKey() {

		try {
			Integer result = this.tradePersistentModel.findTradeOrderByMaxKey();
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradeOrderByMaxKey Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradingdayById() {

		try {
			Tradingday result = this.tradePersistentModel
					.findTradingdayById(this.tradestrategy.getTradingday()
							.getIdTradingDay());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradingdayById Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindTradingdayByOpenDate() {

		try {
			Tradingday result = this.tradePersistentModel
					.findTradingdayByOpenCloseDate(this.tradestrategy
							.getTradingday().getOpen(), this.tradestrategy
							.getTradingday().getClose());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradingdayByOpenDate Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradingdaysByDateRange() {

		try {
			Tradingdays result = this.tradePersistentModel
					.findTradingdaysByDateRange(this.tradestrategy
							.getTradingday().getOpen(), this.tradestrategy
							.getTradingday().getOpen());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradingdaysByDateRange Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindTradelogReport() {

		try {
			TradelogReport result = this.tradePersistentModel
					.findTradelogReport(this.tradestrategy.getPortfolio(),
							TradingCalendar.getYearStart(), this.tradestrategy
									.getTradingday().getClose(), true);
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindTradelogReport Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindCandlesByContractAndDateRange() {

		try {
			List<Candle> result = this.tradePersistentModel
					.findCandlesByContractDateRangeBarSize(this.tradestrategy
							.getContract().getIdContract(), this.tradestrategy
							.getTradingday().getOpen(), this.tradestrategy
							.getTradingday().getClose(), this.tradestrategy
							.getBarSize());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindCandlesByContractAndDateRange Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindCandleCount() {

		try {
			Long result = this.tradePersistentModel.findCandleCount(
					this.tradestrategy.getTradingday().getIdTradingDay(),
					this.tradestrategy.getContract().getIdContract());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindCandleCount Msg: " + e.getMessage());
		}
	}

	@Test
	public void testPersistRule() {

		try {
			Integer version = this.tradePersistentModel
					.findRuleByMaxVersion(this.tradestrategy.getStrategy()) + 1;
			Rule rule = new Rule(this.tradestrategy.getStrategy(), version,
					"Test", new Date(), new Date());
			Aspect result = this.tradePersistentModel.persistRule(rule);
			TestCase.assertNotNull(result);
			this.tradePersistentModel.removeRule(rule);
		} catch (Exception e) {
			TestCase.fail("Error testPersistRule Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindRuleById() {

		try {
			Integer version = this.tradePersistentModel
					.findRuleByMaxVersion(this.tradestrategy.getStrategy()) + 1;
			Rule rule = new Rule(this.tradestrategy.getStrategy(), version,
					"Test", new Date(), new Date());
			Aspect resultAspect = this.tradePersistentModel.persistRule(rule);
			TestCase.assertNotNull(resultAspect);
			Rule result = this.tradePersistentModel.findRuleById(resultAspect
					.getId());
			TestCase.assertNotNull(result);
			this.tradePersistentModel.removeRule(rule);
		} catch (Exception e) {
			TestCase.fail("Error testFindRuleById Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindRuleByMaxVersion() {

		try {
			Integer result = this.tradePersistentModel
					.findRuleByMaxVersion(this.tradestrategy.getStrategy());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindRuleByMaxVersion Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindStrategyById() {

		try {
			Strategy result = this.tradePersistentModel
					.findStrategyById(this.tradestrategy.getStrategy()
							.getIdStrategy());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindStrategyById Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindStrategyByName() {

		try {
			Strategy result = this.tradePersistentModel
					.findStrategyByName(this.tradestrategy.getStrategy()
							.getName());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindStrategyByName Msg: " + e.getMessage());
		}
	}

	@Test
	public void testRemoveRule() {

		try {
			Integer version = this.tradePersistentModel
					.findRuleByMaxVersion(this.tradestrategy.getStrategy()) + 1;
			Rule rule = new Rule(this.tradestrategy.getStrategy(), version,
					"Test", new Date(), new Date());
			Rule resultAspect = (Rule) this.tradePersistentModel
					.persistRule(rule);
			TestCase.assertNotNull(resultAspect);
			this.tradePersistentModel.removeRule(resultAspect);
		} catch (Exception e) {
			TestCase.fail("Error testRemoveRule Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindStrategies() {

		try {
			List<Strategy> result = this.tradePersistentModel.findStrategies();
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindStrategies Msg: " + e.getMessage());
		}
	}

	@Test
	public void testFindAspectsByClassName() {

		try {
			Aspects result = this.tradePersistentModel
					.findAspectsByClassName(this.tradestrategy.getClass()
							.getName());
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindAspectsByClassName Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindAspectsByClassNameFieldName() {

		try {
			for (IndicatorDataset indicator : this.tradestrategy
					.getDatasetContainer().getIndicators()) {
				IndicatorSeries series = indicator.getSeries(0);
				String indicatorName = series.getType().substring(0,
						series.getType().indexOf("Series"));
				Aspects result = this.tradePersistentModel
						.findAspectsByClassNameFieldName(
								CodeType.class.getName(), "name", indicatorName);
				TestCase.assertNotNull(result);
			}

		} catch (Exception e) {
			TestCase.fail("Error testFindAspectsByClassNameFieldName Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testFindAspectById() {

		try {
			Aspect result = this.tradePersistentModel
					.findAspectById(this.tradestrategy);
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testFindAspectById Msg: " + e.getMessage());
		}
	}

	@Test
	public void testPersistAspect() {

		try {
			Aspect result = this.tradePersistentModel
					.persistAspect(this.tradestrategy);
			TestCase.assertNotNull(result);
		} catch (Exception e) {
			TestCase.fail("Error testPersistAspect Msg: " + e.getMessage());
		}
	}

	@Test
	public void testRemoveAspect() {
		Aspect result = null;
		try {
			this.tradePersistentModel.removeAspect(this.tradestrategy);
			result = this.tradePersistentModel
					.findAspectById(this.tradestrategy);
		} catch (PersistentModelException e) {

		} finally {
			TestCase.assertNull(result);
		}
	}

	@Test
	public void testReassignStrategy() {

		try {
			Tradingday tradingday = this.tradePersistentModel
					.findTradingdayById(this.tradestrategy.getTradingday()
							.getIdTradingDay());
			TestCase.assertFalse(tradingday.getTradestrategies().isEmpty());
			Strategy toStrategy = (Strategy) DAOStrategy.newInstance()
					.getObject();
			toStrategy = this.tradePersistentModel.findStrategyById(toStrategy
					.getIdStrategy());
			this.tradePersistentModel.reassignStrategy(
					this.tradestrategy.getStrategy(), toStrategy, tradingday);
			TestCase.assertEquals(toStrategy, tradingday.getTradestrategies()
					.get(0).getStrategy());
		} catch (Exception e) {
			TestCase.fail("Error testReassignStrategy Msg: " + e.getMessage());
		}
	};

	@Test
	public void testReplaceTradingday() {

		try {
			Tradingdays tradingdays = new Tradingdays();

			Tradingday instance1 = tradePersistentModel
					.findTradingdayById(this.tradestrategy.getTradingday()
							.getIdTradingDay());
			tradingdays.add(instance1);

			TradingdayTableModel tradingdayModel = new TradingdayTableModel();
			tradingdayModel.setData(tradingdays);
			TradingdayTable tradingdayTable = new TradingdayTable(
					tradingdayModel);
			tradingdayTable.setRowSelectionInterval(0, 0);

			this.tradestrategy.getContract().setIndustry("Computer");
			Contract result = this.tradePersistentModel
					.persistContract(this.tradestrategy.getContract());
			assertNotNull(result);
			Tradingday instance2 = tradePersistentModel
					.findTradingdayById(this.tradestrategy.getTradingday()
							.getIdTradingDay());
			tradingdays.replaceTradingday(instance1.getOpen(), instance2);
			int selectedRow = tradingdayTable.getSelectedRow();
			tradingdayModel.setData(tradingdays);
			if (selectedRow > -1) {
				tradingdayTable.setRowSelectionInterval(selectedRow,
						selectedRow);
			}
			org.trade.core.valuetype.Date openDate = (org.trade.core.valuetype.Date) tradingdayModel
					.getValueAt(tradingdayTable.convertRowIndexToModel(0), 0);
			org.trade.core.valuetype.Date closeDate = (org.trade.core.valuetype.Date) tradingdayModel
					.getValueAt(tradingdayTable.convertRowIndexToModel(0), 1);
			Tradingday transferObject = tradingdayModel.getData()
					.getTradingday(openDate.getDate(), closeDate.getDate());
			TestCase.assertNotNull(transferObject);

			TestCase.assertNotNull(tradingdays.getTradingday(
					instance1.getOpen(), instance1.getClose()));
			String industry = transferObject.getTradestrategies().get(0)
					.getContract().getIndustry();
			TestCase.assertNotNull(industry);

		} catch (Exception e) {
			TestCase.fail("Error testReplaceTradingday Msg: " + e.getMessage());
		}
	}
}
