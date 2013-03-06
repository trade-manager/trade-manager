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

import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Account;

import org.trade.persistent.dao.PortfolioAccount;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class TWSFinancialAccountRequestTest extends TestCase {

	private PersistentModel tradePersistentModel = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		tradePersistentModel = (PersistentModel) ClassFactory
				.getServiceForInterface(PersistentModel._persistentModel, this);
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		Aspects portfolioAccounts = tradePersistentModel
				.findAspectsByClassName(PortfolioAccount.class.getName());
		for (Aspect aspect : portfolioAccounts.getAspect()) {
			tradePersistentModel.removeAspect(aspect);
		}

		Aspects accounts = tradePersistentModel
				.findAspectsByClassName(Account.class.getName());
		for (Aspect aspect : accounts.getAspect()) {
			tradePersistentModel.removeAspect(aspect);
		}
	}

	@Test
	public void testAliasRequest() {

		try {

			final TWSAccountAliasRequest request = new TWSAccountAliasRequest();
			final Aspects aspects = (Aspects) request
					.fromXML(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"org/trade/broker/request/aliases.xml"));
			for (Aspect aspect : aspects.getAspect()) {
				Account account = (Account) aspect;
				tradePersistentModel.persistAccount(account);
			}
		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testGroupRequest() {

		try {

			final TWSGroupRequest request = new TWSGroupRequest();
			final Aspects aspects = (Aspects) request
					.fromXML(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"org/trade/broker/request/groups.xml"));
			for (Aspect aspect : aspects.getAspect()) {
				PortfolioAccount item = (PortfolioAccount) aspect;
				tradePersistentModel.persistPortfolioAccount(item);
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testAllocationRequest() {

		try {

			final TWSAllocationRequest request = new TWSAllocationRequest();
			final Aspects aspects = (Aspects) request.fromXML(Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"org/trade/broker/request/allocation.xml"));
			for (Aspect aspect : aspects.getAspect()) {
				PortfolioAccount item = (PortfolioAccount) aspect;
				tradePersistentModel.persistPortfolioAccount(item);
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testAliasEmptyRequest() {

		try {

			final TWSAccountAliasRequest request = new TWSAccountAliasRequest();

			// String xml =
			// "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ListOfAccountAliases/>";
			// ByteArrayInputStream inputSource = new ByteArrayInputStream(
			// xml.getBytes("utf-8"));
			// final Aspects aspects = (Aspects) request.fromXML(inputSource);
			final Aspects aspects = (Aspects) request.fromXML(Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"org/trade/broker/request/aliasesEmpty.xml"));
			for (Aspect aspect : aspects.getAspect()) {
				Account account = (Account) aspect;
				tradePersistentModel.persistAccount(account);
			}
		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testGroupEmptyRequest() {

		try {

			final TWSGroupRequest request = new TWSGroupRequest();
			final Aspects aspects = (Aspects) request.fromXML(Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"org/trade/broker/request/groupsEmpty.xml"));
			for (Aspect aspect : aspects.getAspect()) {
				PortfolioAccount item = (PortfolioAccount) aspect;
				tradePersistentModel.persistPortfolioAccount(item);
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testAllocationEmptyRequest() {

		try {

			final TWSAllocationRequest request = new TWSAllocationRequest();
			final Aspects aspects = (Aspects) request.fromXML(Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"org/trade/broker/request/allocationEmpty.xml"));

			for (Aspect aspect : aspects.getAspect()) {
				PortfolioAccount item = (PortfolioAccount) aspect;
				tradePersistentModel.persistPortfolioAccount(item);
			}
		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}
}
