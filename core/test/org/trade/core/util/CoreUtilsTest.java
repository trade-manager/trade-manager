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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some tests for the {@link TradingCalendar} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class CoreUtilsTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(CoreUtilsTest.class);

	private static final int SCALE = 5;

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
	public void testNullSafe() {
		try {

			int returnVal = CoreUtils.nullSafeComparator(null, new BigDecimal(
					1.23));
			TestCase.assertEquals(-1, returnVal);

			returnVal = CoreUtils
					.nullSafeComparator(new BigDecimal(1.23), null);
			TestCase.assertEquals(1, returnVal);

			returnVal = CoreUtils.nullSafeComparator(null, null);
			TestCase.assertEquals(0, returnVal);

			returnVal = CoreUtils.nullSafeComparator(new BigDecimal(1.23),
					new BigDecimal(1.24));
			TestCase.assertEquals(-1, returnVal);

			returnVal = CoreUtils.nullSafeComparator(new BigDecimal(1.25),
					new BigDecimal(1.24));
			TestCase.assertEquals(1, returnVal);

			returnVal = CoreUtils.nullSafeComparator(null, 1);
			TestCase.assertEquals(-1, returnVal);

			returnVal = CoreUtils.nullSafeComparator(null, new Integer(0));
			TestCase.assertEquals(-1, returnVal);

			returnVal = CoreUtils.nullSafeComparator(new Integer(0),
					new Integer(0));
			TestCase.assertEquals(0, returnVal);

			returnVal = CoreUtils.nullSafeComparator(new Integer(1),
					new Integer(0));
			TestCase.assertEquals(1, returnVal);

		} catch (Exception ex) {
			_log.error("Error creating class: " + ex.getMessage(), ex);
			fail("Error creating class: " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testBigDecimalRounding() {
		BigDecimal avgFillPrice = new BigDecimal("35.34567897").setScale(SCALE,
				BigDecimal.ROUND_HALF_EVEN);
		TestCase.assertEquals(new BigDecimal("35.34568"), avgFillPrice);
		avgFillPrice = new BigDecimal("35.34567344").setScale(SCALE,
				BigDecimal.ROUND_HALF_EVEN);
		TestCase.assertEquals(new BigDecimal("35.34567"), avgFillPrice);

		TestCase.assertEquals(0,
				BigDecimal.ZERO.compareTo(new BigDecimal(0.00)));

		TestCase.assertEquals(-1,
				BigDecimal.ZERO.compareTo(new BigDecimal(0.01)));

		TestCase.assertEquals(1,
				BigDecimal.ZERO.compareTo(new BigDecimal(-0.01)));
	}

	private AtomicInteger timerRunning = null;
	private final Object lockCoreUtilsTest = new Object();

	@Test
	public void test10MinTimer() {

		try {

			Timer timer = new Timer(250, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized (lockCoreUtilsTest) {
						timerRunning.addAndGet(250);
						lockCoreUtilsTest.notifyAll();
					}
				}
			});

			timerRunning = new AtomicInteger(0);
			int sleeptime = 5;
			timer.start();
			synchronized (lockCoreUtilsTest) {
				while (timerRunning.get() < (1000 * sleeptime)) {
					String message = "Please wait "
							+ (sleeptime - (timerRunning.get() / 1000))
							+ " seconds.";
					_log.info(message);
					lockCoreUtilsTest.wait();
				}
			}
			timer.stop();
		} catch (Exception ex) {
			_log.error("Error : " + ex.getMessage(), ex);
			fail("Error : " + ex.getCause().getMessage());
		}
	}

	@Test
	public void testIntRounding() {

		try {
			int barSize = 900;
			int[] barSizes = { 3600, 1800, 900, 300, 120, 60, 30 };
			for (int element : barSizes) {
				if (element <= barSize) {
					if ((Math.floor(barSize / (double) element) == (barSize / (double) element))) {
						_log.info("BarSize integer devisable : " + element);
					}
				}
			}
		} catch (Exception ex) {
			_log.error("Error : " + ex.getMessage(), ex);
			fail("Error : " + ex.getCause().getMessage());
		}
	}
}
