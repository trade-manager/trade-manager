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
package org.trade.strategy.data;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.util.MatrixFunctions;
import org.trade.core.util.Pair;
import org.trade.core.util.TradingCalendar;
import org.trade.strategy.data.candle.CandlePeriod;

/**
 * Some tests for the {@link TradingCalendar} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class MatrixFunctionsTest {

	private final static Logger _log = LoggerFactory
			.getLogger(MatrixFunctionsTest.class);

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
	public void testAngle() {
		try {
			List<Pair> pairs = new ArrayList<Pair>();
			int polyOrder = 2;
			double vwap = 30.94;
			int longShort = 1;

			CandlePeriod period = new CandlePeriod(
					TradingCalendar.getBusinessDayStart(new Date()), 300);
			Long startPeriod = period.getStart().getTime();
			Long endPeriod = null;
			pairs.add(new Pair(0, vwap));

			for (int i = 0; i < 3; i++) {
				vwap = vwap + (0.1 * longShort)
						+ (((double) i * longShort / 10));
				period = (CandlePeriod) period.next();
				endPeriod = period.getStart().getTime();
				pairs.add(new Pair(
						((double) (endPeriod - startPeriod) / (1000 * 60 * 60)),
						vwap));
			}

			Collections.sort(pairs, Pair.X_VALUE_ASC);
			for (Pair pair : pairs) {
				_log.info("x: " + pair.x + " y: " + pair.y);
			}
			Pair[] pairsArray = pairs.toArray(new Pair[] {});
			double[] terms = MatrixFunctions.solve(pairsArray, polyOrder);
			double correlationCoeff = MatrixFunctions
					.getCorrelationCoefficient(pairsArray, terms);
			double standardError = MatrixFunctions.getStandardError(pairsArray,
					terms);
			String output = MatrixFunctions.toPrint(polyOrder,
					correlationCoeff, standardError, terms, pairsArray.length);
			_log.info("Pivot Calc: " + output);

			for (Pair pair : pairs) {
				double y = MatrixFunctions.fx(pair.x, terms);
				pair.y = y;
				_log.info("x: " + pair.x + " y: " + pair.y);
			}
			Pair startXY = pairs.get(0);
			Pair endXY = pairs.get(pairs.size() - 1);
			double atan = Math.atan((endXY.y - startXY.y)
					/ ((endXY.x - startXY.x)));
			double angle = (atan * 180) / Math.PI;
			_log.info("angle: " + angle);
			assertEquals(new BigDecimal(67.38).setScale(2, RoundingMode.HALF_UP),
					new BigDecimal(angle).setScale(2, RoundingMode.HALF_UP));

		} catch (Exception ex) {
			_log.error("Error testAngle: " + ex.getMessage(), ex);
			fail("Error testAngle: " + ex.getCause().getMessage());
		}
	}
}
