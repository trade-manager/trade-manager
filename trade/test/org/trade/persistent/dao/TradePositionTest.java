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

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.AspectHome;
import org.trade.dictionary.valuetype.Side;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class TradePositionTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(TradePositionTest.class);

	private String symbol = "TEST";
	private TradePositionHome tradePositionHome = null;
	private AspectHome aspectHome = null;
	private Tradestrategy tradestrategy = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		tradePositionHome = new TradePositionHome();
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
	public void testAddTradePosition() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTradeOrders(this.tradestrategy);

			TradePosition instance = new TradePosition(
					this.tradestrategy.getContract(), new Date(), Side.BOT);
			instance.setIsOpen(true);
			TradePosition tradePosition = (TradePosition) aspectHome
					.persist(instance);

			TestCase.assertNotNull(tradePosition.getIdTradePosition());
			_log.info("testAddTradePosition IdTradeStrategy: "
					+ this.tradestrategy.getIdTradeStrategy()
					+ "IdTradePosition: " + tradePosition.getIdTradePosition());

		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testDeleteTradePosition() {

		try {
			this.tradestrategy = TradestrategyTest
					.removeTradeOrders(this.tradestrategy);
			testAddTradePosition();
			TradePosition instance = tradePositionHome
					.findOpenTradePositionByContractId(this.tradestrategy
							.getContract().getIdContract());
			tradePositionHome.remove(instance);
			_log.info("testDeleteTradePosition IdTradeStrategy: "
					+ tradestrategy.getIdTradeStrategy());

		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}
}
