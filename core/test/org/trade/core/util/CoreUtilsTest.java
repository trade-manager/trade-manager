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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.valuetype.Money;

/**
 * Some tests for the {@link TradingCalendar} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class CoreUtilsTest {

	private final static Logger _log = LoggerFactory
			.getLogger(CoreUtilsTest.class);

	private static final int SCALE = 5;

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
	public void testIsBetween() {
		try {

			TestCase.assertTrue(CoreUtils.isBetween(new BigDecimal(12.20),
					new BigDecimal(12.24), new BigDecimal(12.23)));

			TestCase.assertTrue(CoreUtils.isBetween(new Integer(12),
					new Integer(18), new Integer(15)));

			TestCase.assertFalse(CoreUtils.isBetween(new Integer(12),
					new Integer(18), new Integer(6)));

			TestCase.assertTrue(CoreUtils.isBetween(12.20d, 12.24d, 12.23d));

			TestCase.assertTrue(CoreUtils.isBetween(12.20d, 12.26d, 12.26d));

			TestCase.assertTrue(CoreUtils.isBetween(12.20d, 12.26d, 12.20d));

			TestCase.assertTrue(CoreUtils.isBetween(12.24d, 12.20d, 12.23d));

			TestCase.assertTrue(CoreUtils.isBetween(12.26d, 12.20d, 12.26d));

			TestCase.assertTrue(CoreUtils.isBetween(12.26d, 12.20d, 12.20d));

			TestCase.assertTrue(CoreUtils.isBetween(12.20d, 12.20d, 12.20d));
			TestCase.assertFalse(CoreUtils.isBetween(12, 14, 11));

			TestCase.assertFalse(CoreUtils.isBetween(12, 14, 15));

		} catch (Exception ex) {
			_log.error("Error testIsBetween: " + ex.getMessage(), ex);
			fail("Error testIsBetween: " + ex.getCause().getMessage());
		}
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

			returnVal = CoreUtils.nullSafeComparator(new BigDecimal(-1.23),
					new BigDecimal(-1.24));
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

			Money avgFilledPrice = new Money(186.75);
			Money lastPrice = new Money(186.78);
			Money auxPrice = new Money(186.68);

			Money stopTriggerAmount = new Money(0.03);
			Money stopMoveAmount = new Money(0.02);

			Money initialStopTriggerAmount = new Money(0.02);
			Money initialStopMoveAmount = new Money(0.01);

			int buySellMultiplier = 1;

			if (CoreUtils.nullSafeComparator(auxPrice, avgFilledPrice) == -1
					* buySellMultiplier) {
				if ((CoreUtils.nullSafeComparator(
						lastPrice.getBigDecimalValue(),
						avgFilledPrice.getBigDecimalValue().add(
								initialStopTriggerAmount.getBigDecimalValue()
										.multiply(
												new BigDecimal(
														buySellMultiplier)))) == 1 * buySellMultiplier)
						|| (CoreUtils
								.nullSafeComparator(
										lastPrice.getBigDecimalValue(),
										avgFilledPrice
												.getBigDecimalValue()
												.add(initialStopTriggerAmount
														.getBigDecimalValue()
														.multiply(
																new BigDecimal(
																		buySellMultiplier)))) == 0)) {

					auxPrice = new Money(
							avgFilledPrice
									.getBigDecimalValue()
									.add(initialStopMoveAmount
											.getBigDecimalValue().multiply(
													new BigDecimal(
															buySellMultiplier))));

				}
			}
			TestCase.assertEquals(
					new Money(
							avgFilledPrice
									.getBigDecimalValue()
									.add(initialStopMoveAmount
											.getBigDecimalValue().multiply(
													new BigDecimal(
															buySellMultiplier)))),
					auxPrice);

			lastPrice = new Money(lastPrice.getBigDecimalValue().add(
					stopTriggerAmount.getBigDecimalValue().multiply(
							new BigDecimal(buySellMultiplier))));

			if (CoreUtils.nullSafeComparator(auxPrice, avgFilledPrice) == 1 * buySellMultiplier) {
				if ((CoreUtils.nullSafeComparator(
						lastPrice.getBigDecimalValue(),
						auxPrice.getBigDecimalValue().add(
								stopTriggerAmount.getBigDecimalValue()
										.multiply(
												new BigDecimal(
														buySellMultiplier)))) == 1 * buySellMultiplier)
						|| (CoreUtils
								.nullSafeComparator(
										lastPrice.getBigDecimalValue(),
										auxPrice.getBigDecimalValue()
												.add(stopTriggerAmount
														.getBigDecimalValue()
														.multiply(
																new BigDecimal(
																		buySellMultiplier)))) == 0)) {
					auxPrice = new Money(
							lastPrice
									.getBigDecimalValue()
									.subtract(
											stopMoveAmount
													.getBigDecimalValue()
													.multiply(
															new BigDecimal(
																	buySellMultiplier))));
				}
			}
			TestCase.assertEquals(
					new Money(lastPrice.getBigDecimalValue().subtract(
							stopMoveAmount.getBigDecimalValue().multiply(
									new BigDecimal(buySellMultiplier)))),
					auxPrice);

		} catch (Exception ex) {
			_log.error("Error testNullSafe: " + ex.getMessage(), ex);
			fail("Error testNullSafe: " + ex.getCause().getMessage());
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
			_log.error("Error test10MinTimer: " + ex.getMessage(), ex);
			fail("Error test10MinTimer: " + ex.getCause().getMessage());
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
			_log.error("Error testIntRounding: " + ex.getMessage(), ex);
			fail("Error testIntRounding: " + ex.getCause().getMessage());
		}
	}
}
