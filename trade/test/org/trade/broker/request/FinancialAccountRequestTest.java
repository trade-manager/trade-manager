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
package org.trade.broker.request;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.core.util.CoreUtils;
import org.trade.dictionary.valuetype.AccountType;
import org.trade.dictionary.valuetype.Currency;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.TradeAccount;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class FinancialAccountRequestTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(FinancialAccountRequestTest.class);
	private PersistentModel tradePersistentModel = null;
	private Tradestrategy tradestrategy = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		tradePersistentModel = (PersistentModel) ClassFactory
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
	public void testAliasRequest() {

		try {

			final AccountAliasRequest financialAccountRequest = new AccountAliasRequest();
			final Aspects aspects = (Aspects) financialAccountRequest
					.fromXML(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"org/trade/broker/request/aliases.xml"));

			for (Aspect aspect : aspects.getAspect()) {
				TradeAccount account = (TradeAccount) aspect;
				account.setAccountType(AccountType.INDIVIDUAL);
				account.setCurrency(Currency.USD);
				account.setName(account.getAccountNumber());
				tradePersistentModel.persistAspect(account);
				_log.info("Aspect: \n" + CoreUtils.toFormattedXMLString(aspect));
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testGroupRequest() {

		try {

			final AccountAliasRequest financialAccountRequest = new AccountAliasRequest();
			final Aspects aspects = (Aspects) financialAccountRequest
					.fromXML(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"org/trade/broker/request/groups.xml"));

			for (Aspect aspect : aspects.getAspect()) {
				TradeAccount account = (TradeAccount) aspect;
				account.setAccountType(AccountType.INDIVIDUAL);
				account.setCurrency(Currency.USD);
				account.setName(account.getAccountNumber());
				tradePersistentModel.persistAspect(account);
				_log.info("Aspect: \n" + CoreUtils.toFormattedXMLString(aspect));
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testAllocationRequest() {

		try {

			final AccountAliasRequest financialAccountRequest = new AccountAliasRequest();
			final Aspects aspects = (Aspects) financialAccountRequest
					.fromXML(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"org/trade/broker/request/allocation.xml"));

			for (Aspect aspect : aspects.getAspect()) {
				TradeAccount account = (TradeAccount) aspect;
				account.setAccountType(AccountType.INDIVIDUAL);
				account.setCurrency(Currency.USD);
				account.setName(account.getAccountNumber());
				tradePersistentModel.persistAspect(account);
				_log.info("Aspect: \n" + CoreUtils.toFormattedXMLString(aspect));
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}
}
