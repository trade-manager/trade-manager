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

import java.time.ZonedDateTime;

import org.jfree.data.DataUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.AspectHome;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.MarketBar;
import org.trade.ui.TradeAppLoadConfig;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TradingdayTest {

	private final static Logger _log = LoggerFactory.getLogger(TradingdayTest.class);
	@Rule
	public TestName name = new TestName();

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
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
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
	public void testAddTradingday() {

		// Create new instance of Strategy and set
		// values in it by reading them from form object

		try {
			_log.debug("Adding Tradingday");

			TradingdayHome tradingdayHome = new TradingdayHome();
			AspectHome aspectHome = new AspectHome();
			ZonedDateTime open = TradingCalendar.getTradingDayStart(
					TradingCalendar.getPrevTradingDay(TradingCalendar.getDateTimeNowMarketTimeZone()));
			Tradingday transientInstance = tradingdayHome.findByOpenCloseDate(open,
					TradingCalendar.getTradingDayEnd(open));
			if (null == transientInstance) {
				transientInstance = Tradingday.newInstance(open);
			}
			tradingdayHome.persist(transientInstance);
			_log.info("Tradingday added Id = " + transientInstance.getIdTradingDay());
			assertNotNull(transientInstance.getIdTradingDay());
			aspectHome.remove(transientInstance);
		} catch (Exception | AssertionError ex) {
			String msg = "Error running " + name.getMethodName() + " msg: " + ex.getMessage();
			_log.error(msg);
			fail(msg);
		}
	}

	@Test
	public void testUpdateTradingday() {

		// Create new instance of Strategy and set
		// values in it by reading them from form object

		try {
			_log.debug("Updating Tradingday");

			TradingdayHome tradingdayHome = new TradingdayHome();
			AspectHome aspectHome = new AspectHome();
			ZonedDateTime open = TradingCalendar.getTradingDayStart(
					TradingCalendar.getPrevTradingDay(TradingCalendar.getDateTimeNowMarketTimeZone()));
			Tradingday transientInstance = tradingdayHome.findByOpenCloseDate(open,
					TradingCalendar.getTradingDayEnd(open));
			if (null == transientInstance) {
				transientInstance = Tradingday.newInstance(open);
			}
			transientInstance.setMarketBar(MarketBar.newInstance("+WRB").getCode());
			tradingdayHome.persist(transientInstance);
			_log.info("Tradingday Update Id = " + transientInstance.getIdTradingDay());
			assertNotNull(transientInstance.getIdTradingDay());
			aspectHome.remove(transientInstance);
		} catch (Exception | AssertionError ex) {
			String msg = "Error running " + name.getMethodName() + " msg: " + ex.getMessage();
			_log.error(msg);
			fail(msg);
		}
	}
}
