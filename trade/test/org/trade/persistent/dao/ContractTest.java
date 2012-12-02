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
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.SECType;

/**
 */
public class ContractTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(ContractTest.class);

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {

	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
	}

	@Test
	public void testFindDeleteAddContract() {

		try {

			// Create new instance of Strategy and set
			// values in it by reading them from form object
			AspectHome aspectHome = new AspectHome();
			ContractHome contractHome = new ContractHome();
			Contract transientInstance = new Contract(SECType.STOCK, "QQQ",
					Exchange.SMART, Currency.USD, null, null);

			transientInstance = (Contract) aspectHome
					.persist(transientInstance);
			_log.info("Contract added Id:" + transientInstance.getIdContract());

			Contract contract = contractHome.findByUniqueKey(
					transientInstance.getSecType(),
					transientInstance.getSymbol(),
					transientInstance.getExchange(),
					transientInstance.getCurrency(), null);
			assertNotNull("Contract not found: " + contract.getSymbol(),
					contract);

			if (null != contract) {
				aspectHome.remove(contract);
				_log.info("Contract deleted Id:"
						+ transientInstance.getIdContract());

			}

		} catch (Exception e) {
			fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testFindDeleteAddFuture() {

		try {

			// Create new instance of Strategy and set
			// values in it by reading them from form object
			AspectHome aspectHome = new AspectHome();
			ContractHome contractHome = new ContractHome();
			Date expiry = TradingCalendar.addMonth(new Date(), 1);
			expiry = TradingCalendar.getSpecificTime(expiry, 19, 0, 0, 0);
			_log.info("Expiry Date: " + expiry);
			Contract transientInstance = new Contract(SECType.FUTURE, "ES",
					Exchange.SMART, Currency.USD, expiry, new BigDecimal(50));
			transientInstance = (Contract) aspectHome
					.persist(transientInstance);
			_log.info("Contract added Id:" + transientInstance.getIdContract());

			expiry = TradingCalendar.addDays(expiry, 1);
			_log.info("Expiry Date: " + expiry);
			Contract contract = contractHome.findByUniqueKey(
					transientInstance.getSecType(),
					transientInstance.getSymbol(),
					transientInstance.getExchange(),
					transientInstance.getCurrency(), expiry);
			assertNotNull("Contract not found: " + contract.getSymbol(),
					contract);

			if (null != contract) {
				aspectHome.remove(contract);
				_log.info("Contract deleted Id:"
						+ transientInstance.getIdContract());

			}

			_log.info("Contract added Id:" + transientInstance.getIdContract());

		} catch (Exception e) {
			fail("Error adding row " + e.getMessage());
		}
	}
}
