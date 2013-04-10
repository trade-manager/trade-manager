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

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.AspectHome;
import org.trade.dictionary.valuetype.Side;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class TradeTest extends TestCase {

	private final static Logger _log = LoggerFactory.getLogger(TradeTest.class);

	private String symbol = "TEST";
	private TradeHome tradeHome = null;
	private AspectHome aspectHome = null;
	private Tradestrategy tradestrategy = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		tradeHome = new TradeHome();
		aspectHome = new AspectHome();
		this.tradestrategy = TradestrategyTest.getTestTradestrategy(symbol);
		TestCase.assertNotNull(this.tradestrategy);
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		TradestrategyTest.removeTestTradestrategy(symbol);
	}

	@Test
	public void testAddTrade() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);

			Trade instance = new Trade(this.tradestrategy, Side.BOT);
			this.tradestrategy.addTrade(instance);
			for (Trade trade : this.tradestrategy.getTrades()) {
				trade = (Trade) aspectHome.persist(trade);
				TestCase.assertNotNull(trade.getIdTrade());
				_log.info("testAddTrade IdTradeStrategy: "
						+ this.tradestrategy.getIdTradeStrategy() + "IdTrade: "
						+ trade.getIdTrade());
			}
		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testDeleteTrade() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTrades(this.tradestrategy);
			testAddTrade();
			for (Trade trade : this.tradestrategy.getTrades()) {
				tradeHome.remove(trade);
				_log.info("testDeleteTrade IdTradeStrategy: "
						+ tradestrategy.getIdTradeStrategy());
			}
		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}
}
