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

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.properties.ConfigProperties;

/**
 * Some tests for the {@link TradingCalendar} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TradingCalendarTest {

	private final static Logger _log = LoggerFactory
			.getLogger(TradingCalendarTest.class);

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
	public void testAddBusinessDays() {
		try {
			Date date = new Date();
			_log.info("Date: " + date);
			_log.info("Date: " + TradingCalendar.addBusinessDays(date, -4));
			_log.info("Date: " + TradingCalendar.addBusinessDays(date, -5));
			assertNotNull(date);

		} catch (Exception ex) {
			_log.error("Error creating class: " + ex.getMessage(), ex);
			fail("Error creating class: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testGetSpecificTime() {
		Date date = TradingCalendar.addDays(new Date(), -1);
		_log.info("date: " + date);
		Date busdayStartDate = TradingCalendar.getBusinessDayStart(new Date());
		_log.info("busdayStartDate: " + busdayStartDate);
		Date setDate = TradingCalendar.getSpecificTime(busdayStartDate, date);

		_log.info("setDate: " + setDate);
		assertEquals(TradingCalendar.getHourOfDay(busdayStartDate),
				TradingCalendar.getHourOfDay(setDate));
		assertEquals(TradingCalendar.getMinute(busdayStartDate),
				TradingCalendar.getMinute(setDate));

	}

	@Test
	public void testGetGMTSpecificTime() {

		SimpleDateFormat m_sdfGMT = new SimpleDateFormat("yyyyMMdd HH:mm:ss z");
		Date endDate = TradingCalendar.getBusinessDayEnd(new Date());
		_log.info("BusinessDayEnd: " + endDate);
		if (TradingCalendar.inDaylightTime(endDate)) {
			endDate = TradingCalendar.addHours(endDate, 1);
		}
		m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		String setDate = m_sdfGMT.format(endDate);
		_log.info("BusinessDayEnd GMT: " + setDate);
	}

	@Test
	public void testGetExecutionFillTime() {
		try {

			String tz = ConfigProperties.getPropAsString("trade.tws.timezone");
			// tz = "America/Vancouver";
			TimeZone twsTimeZone = TimeZone.getTimeZone(tz);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			sdf.setTimeZone(twsTimeZone);
			Date date = sdf.parse("20130701 15:58:00");
			_log.info("Date: " + date + " TZ: " + tz);
			assertEquals(15, TradingCalendar.getHourOfDay(date));
			assertEquals(58, TradingCalendar.getMinute(date));

		} catch (Exception ex) {
			_log.error("Error parsing date: " + ex.getMessage(), ex);
			fail("Error parsing date: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testPrintTimeZones() {
		try {

			String[] timeZones = TimeZone.getAvailableIDs();

			for (int i = 0; i < timeZones.length; i++) {
				_log.info("TZ: " + timeZones[i]);
			}

		} catch (Exception ex) {
			_log.error("Error parsing date: " + ex.getMessage(), ex);
			fail("Error parsing date: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testIsMarketHours() {

		// TradingCalendar.getFormattedDate("20130401 09:30", "yyyyMMdd HH:mm");
		Date openDate = TradingCalendar.getBusinessDayStart(new Date());
		Date closeDate = TradingCalendar.getBusinessDayEnd(new Date());
		Date date = TradingCalendar.addBusinessDays(new Date(), -1);
		date = TradingCalendar.getSpecificTime(date, 9, 30);
		_log.info("Business day openDate: " + openDate
				+ " Business day closeDate: " + closeDate + " Date: " + date);
		assertTrue(TradingCalendar.isMarketHours(openDate, closeDate, date));
		date = TradingCalendar.getSpecificTime(date, 16, 00);
		_log.info("Business day openDate: " + openDate
				+ " Business day closeDate: " + closeDate + " Date: " + date);
		assertFalse(TradingCalendar.isMarketHours(openDate, closeDate, date));

		date = TradingCalendar.getSpecificTime(date, 15, 00);
		_log.info("Business day openDate: " + openDate
				+ " Business day closeDate: " + closeDate + " Date: " + date);
		assertTrue(TradingCalendar.isMarketHours(openDate, closeDate, date));

		date = TradingCalendar.getSpecificTime(date, 17, 00);
		_log.info("Business day openDate: " + openDate
				+ " Business day closeDate: " + closeDate + " Date: " + date);
		assertFalse(TradingCalendar.isMarketHours(openDate, closeDate, date));
	}

	@Test
	public void testIsTradingday() {

		Date date = TradingCalendar.getMostRecentTradingDay(new Date());
		assertTrue(TradingCalendar.isTradingDay(date));
		date = TradingCalendar.getSpecificTime(date, Calendar.SUNDAY);
		assertFalse(TradingCalendar.isTradingDay(date));
	}

	@Test
	public void testIsGreaterThan365() {
		Integer chartDays = 365;
		Date endDate = TradingCalendar.addDays(new Date(), 0);
		if (TradingCalendar.daysDiff(
				TradingCalendar.addDays(endDate, (chartDays * -1)), new Date()) > TradingCalendar
				.getDaysInYear(endDate)) {
			chartDays = TradingCalendar.getDaysInYear(endDate)
					- TradingCalendar.daysDiff(endDate, new Date());
		}
		_log.info("chartDays: " + chartDays);
		assertEquals(365, chartDays.intValue());
	}

	@Test
	public void testNextRequestId() {
		AtomicInteger reqId = null;
		Date date = new Date();
		_log.info("date: " + date.getTime());
		reqId = new AtomicInteger((int) (date.getTime() / 1000d));
		_log.info("reqId: " + reqId);
		_log.info("reqId: " + reqId.incrementAndGet());
		assertNotNull(reqId);
	}

	@Test
	public void testGetDateParts() {
		try {
			SimpleDateFormat m_sdfGMT = new SimpleDateFormat(
					"yyyy/MM/dd HH:mm:ss z");
			m_sdfGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = m_sdfGMT.parse("2013/04/23 23:29:59 GMT");

			/*
			 * Takes a GMT dateTime converts to Java JM date time Use args to
			 * run this
			 * 
			 * -Duser.timezone=EST5EDT
			 * -Dlog4j.configuration=file:"config.properties"
			 */

			assertEquals(23, TradingCalendar.getDayOfMonth(date));
			assertEquals(19, TradingCalendar.getHourOfDay(date));
			assertEquals(7, TradingCalendar.getHour(date));
			assertEquals(29, TradingCalendar.getMinute(date));
			assertEquals(59, TradingCalendar.getSecond(date));
			assertEquals(2013, TradingCalendar.getYear(date));

			// System.out.println(date.toString());
			// boolean ans =
			// date.toString().matches(".+\\d\\d:\\d\\d::(00\\.0)");
			// assertTrue(ans);

		} catch (Exception ex) {
			_log.error("Error parsing date: " + ex.getMessage(), ex);
			fail("Error parsing date: " + ex.getCause().getMessage());
		}
	}
}
