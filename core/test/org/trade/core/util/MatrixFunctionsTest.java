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

import java.util.ArrayList;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.strategy.data.candle.CandlePeriod;

/**
 * Some tests for the {@link TradingCalendar} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class MatrixFunctionsTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(MatrixFunctionsTest.class);

	MatrixFunctions matrixFunctions = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		matrixFunctions = new MatrixFunctions();
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
	}

	@Test
	public void testAngle() {
		try {
			List<Pair> pairs = new ArrayList<Pair>();
			int polyOrder = 2;
			double vwap = 30.94;
			int longShort = -1;

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
				_log.error("x: " + pair.x + " y: " + pair.y);
			}
			Pair[] pairsArray = pairs.toArray(new Pair[] {});
			double[] terms = matrixFunctions.solve(pairsArray, polyOrder);
			double correlationCoeff = matrixFunctions
					.getCorrelationCoefficient(pairsArray, terms);
			double standardError = matrixFunctions.getStandardError(pairsArray,
					terms);
			String output = MatrixFunctions.toPrint(polyOrder,
					correlationCoeff, standardError, terms, pairsArray.length);
			_log.error("Pivot Calc: " + output);

			for (Pair pair : pairs) {
				double y = MatrixFunctions.fx(pair.x, terms);
				pair.y = y;
				_log.error("x: " + pair.x + " y: " + pair.y);
			}
			Pair startXY = pairs.get(0);
			Pair endXY = pairs.get(pairs.size() - 1);
			double angle = Math.atan((endXY.y - startXY.y)
					/ ((endXY.x - startXY.x)));

			_log.error("angle: " + (angle * 180) / Math.PI);
			TestCase.assertFalse(false);

		} catch (Exception ex) {
			_log.error("Error testAngle: " + ex.getMessage(), ex);
			fail("Error testAngle: " + ex.getCause().getMessage());
		}
	}
}
