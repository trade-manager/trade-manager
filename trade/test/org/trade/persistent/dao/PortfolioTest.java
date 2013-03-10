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

import org.trade.core.dao.Aspect;
import org.trade.core.dao.AspectHome;
import org.trade.core.dao.Aspects;

import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class PortfolioTest extends TestCase {

	private AspectHome aspectHome = new AspectHome();

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
		Aspects accounts = aspectHome.findByClassName(Account.class.getName());
		for (Aspect aspect : accounts.getAspect()) {
			aspectHome.remove(aspect);
		}
	}

	@Test
	public void testCreateAccount() {

		try {
			PortfolioHome portfolioHome = new PortfolioHome();
			Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
					.getObject();
			portfolio = portfolioHome.findByName(portfolio.getName());
			Account account = new Account("Test", "T123456", Currency.USD);
			PortfolioAccount portfolioAccount = new PortfolioAccount(portfolio,
					account);
			portfolio.getPortfolioAccounts().add(portfolioAccount);
			portfolio = (Portfolio) aspectHome.persist(portfolio);
			TestCase.assertNotNull(portfolio.getIndividualAccount());

		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}
}
