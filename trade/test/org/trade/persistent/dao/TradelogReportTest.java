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

import org.jfree.data.DataUtilities;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.ui.TradeAppLoadConfig;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TradelogReportTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(TradelogReportTest.class);

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
	}

	@Test
	public void testTradelogDetails() {

		try {
			TradelogHome tradelogHome = new TradelogHome();
			Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
					.getObject();
			TradelogReport tradelogReport = tradelogHome.findByTradelogDetail(
					portfolio, TradingCalendar.getYearStart(),
					TradingCalendar.getTodayBusinessDayEnd(), false);
			TestCase.assertFalse(!tradelogReport.getTradelogDetail().isEmpty());
			for (TradelogDetail tradelogDetail : tradelogReport
					.getTradelogDetail()) {
				_log.info("testTradelogDetails tradelogDetail: " + " getOpen:"
						+ tradelogDetail.getOpen() + " getAction:"
						+ tradelogDetail.getAction() + " getMarketBias:"
						+ tradelogDetail.getMarketBias() + " getName:"
						+ tradelogDetail.getName() + " getSymbol:"
						+ tradelogDetail.getSymbol() + " getQuantity:"
						+ tradelogDetail.getQuantity() + " getLongShort:"
						+ tradelogDetail.getLongShort()
						+ " getAverageFilledPrice:"
						+ tradelogDetail.getAverageFilledPrice()
						+ " getFilledDate:" + tradelogDetail.getFilledDate());
			}

		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}

	@Test
	public void testTradelogSummary() {

		try {
			TradelogHome tradelogHome = new TradelogHome();
			Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
					.getObject();
			TradelogReport tradelogReport = tradelogHome.findByTradelogSummary(
					portfolio, TradingCalendar.getYearStart(),
					TradingCalendar.getTodayBusinessDayEnd());
			TestCase.assertFalse(!tradelogReport.getTradelogSummary().isEmpty());
			for (TradelogSummary tradelogSummary : tradelogReport
					.getTradelogSummary()) {
				_log.info("testTradelogSummary tradelogDetail: " + "getPeriod:"
						+ tradelogSummary.getPeriod() + "getBattingAverage:"
						+ tradelogSummary.getBattingAverage()
						+ "getSimpleSharpeRatio:"
						+ tradelogSummary.getSimpleSharpeRatio()
						+ "getQuantity:" + tradelogSummary.getQuantity()
						+ "getGrossProfitLoss:"
						+ tradelogSummary.getGrossProfitLoss() + "getQuantity:"
						+ tradelogSummary.getQuantity() + "getNetProfitLoss:"
						+ tradelogSummary.getNetProfitLoss());
			}

		} catch (Exception e) {
			TestCase.fail("Error adding row " + e.getMessage());
		}
	}
}
