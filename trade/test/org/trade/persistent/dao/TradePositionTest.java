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

import java.util.Date;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.AspectHome;
import org.trade.dictionary.valuetype.Side;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class TradePositionTest {

	private final static Logger _log = LoggerFactory
			.getLogger(TradePositionTest.class);

	private String symbol = "TEST";
	private TradePositionHome tradePositionHome = null;
	private AspectHome aspectHome = null;
	private Tradestrategy tradestrategy = null;

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
		tradePositionHome = new TradePositionHome();
		aspectHome = new AspectHome();
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
	public void testAddRemoveTradePosition() {

		try {
			TradePosition instance = new TradePosition(
					this.tradestrategy.getContract(), new Date(), Side.BOT);

			TradePosition tradePosition = aspectHome.persist(instance);

			assertNotNull(tradePosition.getIdTradePosition());
			_log.info("testAddTradePosition IdTradeStrategy: "
					+ this.tradestrategy.getIdTradeStrategy()
					+ "IdTradePosition: " + tradePosition.getIdTradePosition());

			tradePositionHome.remove(tradePosition);
			_log.info("testDeleteTradePosition IdTradeStrategy: "
					+ tradestrategy.getIdTradeStrategy());
			tradePosition = tradePositionHome.findById(tradePosition
					.getIdTradePosition());
			assertNull(tradePosition);

		} catch (Exception e) {
			fail("Error adding row " + e.getMessage());
		}
	}
}
