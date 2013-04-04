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

import java.math.BigDecimal;
import java.util.Date;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.AspectHome;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.SECType;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class TradestrategyTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(TradestrategyTest.class);

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
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
			Tradestrategy tradestrategy = TradestrategyTest
					.getTestTradestrategy();
			TestCase.assertNotNull(tradestrategy);
			_log.info("testTradingdaysSave IdTradeStrategy:"
					+ tradestrategy.getIdTradeStrategy());

		} catch (Exception e) {
			TestCase.fail("Error testAddTradestrategy Msg: " + e.getMessage());
		}
	}

	@Test
	public void testUpdateTradeStrategy() {

		try {
			Date open = TradingCalendar.getBusinessDayStart(TradingCalendar
					.getMostRecentTradingDay(new Date()));
			TradingdayHome tradingdayHome = new TradingdayHome();
			Tradingdays tradingdays = tradingdayHome
					.findTradingdaysByDateRange(open, open);
			for (Tradingday tradingday : tradingdays.getTradingdays()) {
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					tradestrategy.setStatus(TradestrategyStatus.OPEN);
				}
				tradingdayHome.persist(tradingday);

				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {

					_log.info("testTradingdaysUpdate IdTradeStrategy:"
							+ tradestrategy.getIdTradeStrategy() + "  Status: "
							+ tradestrategy.getStatus());
					TestCase.assertEquals(TradestrategyStatus.OPEN,
							tradestrategy.getStatus());
				}
			}
		} catch (Exception e) {
			TestCase.fail("Error update row " + e.getMessage());
		}
	}

	@Test
	public void testReadAndSavefileMultipleDayTradestrategy() {

		try {
			AspectHome aspectHome = new AspectHome();
			TradingdayHome tradingdayHome = new TradingdayHome();
			Tradingdays tradingdays = new Tradingdays();
			Tradingday instance = Tradingday.newInstance(TradingCalendar
					.getMostRecentTradingDay(new Date()));
			tradingdays.add(instance);

			String fileName = "db/GappersToDay10Test.csv";
			tradingdays.populateDataFromFile(fileName, instance);
			TestCase.assertFalse(tradingdays.getTradingdays().isEmpty());
			for (Tradingday tradingday : tradingdays.getTradingdays()) {
				tradingdayHome.persist(tradingday);
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					_log.info("testTradingdaysUpdate IdTradeStrategy:"
							+ tradestrategy.getIdTradeStrategy());
					aspectHome.remove(tradestrategy);
					aspectHome.remove(tradestrategy.getContract());
				}
			}
		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testReadAndSavefileOneDayTradestrategy() {

		try {
			AspectHome aspectHome = new AspectHome();
			TradingdayHome tradingdayHome = new TradingdayHome();
			Tradingdays tradingdays = new Tradingdays();
			Tradingday instance = Tradingday.newInstance(TradingCalendar
					.getMostRecentTradingDay(new Date()));
			tradingdays.add(instance);

			String fileName = "db/GapperstodayJust1Test.csv";

			tradingdays.populateDataFromFile(fileName, instance);
			TestCase.assertFalse(tradingdays.getTradingdays().isEmpty());
			for (Tradingday tradingday : tradingdays.getTradingdays()) {
				tradingdayHome.persist(tradingday);
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {

					_log.info("testTradingdaysUpdate IdTradeStrategy:"
							+ tradestrategy.getIdTradeStrategy());
					aspectHome.remove(tradestrategy);
					aspectHome.remove(tradestrategy.getContract());
				}
			}

		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}

	/**
	 * Method getTestTradestrategy.
	 * 
	 * @return Tradestrategy
	 * @throws Exception
	 */
	public static Tradestrategy getTestTradestrategy() throws Exception {
		String symbol = "TEST";
		ContractHome contractHome = new ContractHome();
		PortfolioHome portfolioHome = new PortfolioHome();

		TradestrategyHome tradestrategyHome = new TradestrategyHome();
		AspectHome aspectHome = new AspectHome();

		Tradestrategy tradestrategy = null;
		Strategy strategy = (Strategy) DAOStrategy.newInstance().getObject();
		Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
				.getObject();
		portfolio = portfolioHome.findByName(portfolio.getName());
		Account account = new Account("Test", "T123456", Currency.USD);
		account.setAvailableFunds(new BigDecimal(25000));
		account.setBuyingPower(new BigDecimal(100000));
		account.setCashBalance(new BigDecimal(25000));
		PortfolioAccount portfolioAccount = new PortfolioAccount(portfolio,
				account);
		portfolio.getPortfolioAccounts().add(portfolioAccount);
		portfolio = (Portfolio) aspectHome.persist(portfolio);
		Date open = TradingCalendar.getBusinessDayStart(TradingCalendar
				.getMostRecentTradingDay(new Date()));

		Contract contract = contractHome.findByUniqueKey(SECType.STOCK, symbol,
				Exchange.SMART, Currency.USD, null);
		if (null == contract) {
			contract = new Contract(SECType.STOCK, symbol, Exchange.SMART,
					Currency.USD, null, null);
			contract = (Contract) aspectHome.persist(contract);

		} else {
			tradestrategy = tradestrategyHome.findTradestrategyByUniqueKeys(
					open, strategy.getName(), contract.getIdContract(),
					portfolio.getName());
			if (null != tradestrategy) {
				tradestrategy = tradestrategyHome.findById(tradestrategy
						.getIdTradeStrategy());
				for (Trade trade : tradestrategy.getTrades()) {
					aspectHome.remove(trade);
				}
				tradestrategy.setStatus(null);
				tradestrategy.getTrades().clear();
				tradestrategy = (Tradestrategy) aspectHome
						.persist(tradestrategy);
				return tradestrategy;
			}
		}
		TradingdayHome tradingdayHome = new TradingdayHome();
		Tradingday tradingday = Tradingday.newInstance(open);
		Tradingday instance = tradingdayHome.findByOpenCloseDate(
				tradingday.getOpen(), tradingday.getClose());
		if (null != instance) {
			tradingday.getTradestrategies().clear();
			tradingday = instance;
		}
		tradestrategy = new Tradestrategy(contract, tradingday, strategy,
				portfolio, new BigDecimal(100), "BUY", "0", true,
				ChartDays.TWO_DAYS, BarSize.FIVE_MIN);
		tradingday.addTradestrategy(tradestrategy);
		tradingdayHome.persist(tradingday);
		return tradestrategyHome.findById(tradestrategy.getIdTradeStrategy());
	}

	/**
	 * Method removeTestTradestrategy.
	 * 
	 * @throws Exception
	 */
	public static void removeTestTradestrategy() throws Exception {
		String symbol = "TEST";
		ContractHome contractHome = new ContractHome();
		PortfolioHome portfolioHome = new PortfolioHome();
		TradestrategyHome tradestrategyHome = new TradestrategyHome();
		AspectHome aspectHome = new AspectHome();
		Contract contract = contractHome.findByUniqueKey(SECType.STOCK, symbol,
				Exchange.SMART, Currency.USD, null);
		if (null != contract) {
			Date open = TradingCalendar.getBusinessDayStart(TradingCalendar
					.getMostRecentTradingDay(new Date()));
			Strategy strategy = (Strategy) DAOStrategy.newInstance()
					.getObject();
			Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
					.getObject();
			portfolio = portfolioHome.findByName(portfolio.getName());
			Account account = portfolio.getIndividualAccount();
			if (null != account) {
				aspectHome.remove(account);
			}
			Tradestrategy tradestrategy = tradestrategyHome
					.findTradestrategyByUniqueKeys(open, strategy.getName(),
							contract.getIdContract(), portfolio.getName());
			if (null != tradestrategy) {
				aspectHome.remove(tradestrategy);
				aspectHome.remove(tradestrategy.getContract());
				aspectHome.remove(tradestrategy.getTradingday());
			}
		}
	}

	/**
	 * Method removeTrades.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return Tradestrategy
	 * @throws Exception
	 */
	public static Tradestrategy removeTrades(Tradestrategy tradestrategy)
			throws Exception {
		TradestrategyHome tradestrategyHome = new TradestrategyHome();
		AspectHome aspectHome = new AspectHome();
		tradestrategy = tradestrategyHome.findById(tradestrategy
				.getIdTradeStrategy());
		if (null != tradestrategy) {
			for (Trade trade : tradestrategy.getTrades()) {
				aspectHome.remove(trade);
			}
			tradestrategy.setStatus(null);
			tradestrategy.getTrades().clear();
			aspectHome.persist(tradestrategy);
		}
		return tradestrategyHome.findById(tradestrategy.getIdTradeStrategy());
	}
}
