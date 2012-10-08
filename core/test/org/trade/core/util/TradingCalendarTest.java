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
package org.trade.core.util;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some tests for the {@link TradingCalendar} class.
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TradingCalendarTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(TradingCalendarTest.class);

	/**
	 * Method setUp.
	 * @throws Exception
	 */
	protected void setUp() throws Exception {

	}

	/**
	 * Method tearDown.
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {

	}

	@Test
	public void testAddBusinessDays() {
		try {
			Date date = new Date();
			_log.info("Date: " + date);
			_log.info("Date: " + TradingCalendar.addBusinessDays(date, -4));
			_log.info("Date: " + TradingCalendar.addBusinessDays(date, -5));

		} catch (Exception ex) {
			_log.error("Error creating class: " + ex.getMessage(), ex);
			fail("Error creating class: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testNextRequestId() {
		AtomicInteger reqId = null;
		Date date = new Date();
		_log.info("date: " + date.getTime());
		reqId = new AtomicInteger((int) (date.getTime() / 1000d));
		_log.info("reqId: " + reqId);
		_log.info("reqId: " + reqId.incrementAndGet());
	}
}
