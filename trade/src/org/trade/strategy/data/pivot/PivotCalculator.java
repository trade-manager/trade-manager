/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
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
package org.trade.strategy.data.pivot;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.util.MatrixFunctions;
import org.trade.core.util.Pair;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */
public class PivotCalculator {

	private final static Logger _log = LoggerFactory
			.getLogger(PivotCalculator.class);

	private static int _polyOrder = 2; // default order
	private static double _minCorrelationCoeff = 0.6;

	public PivotCalculator(int polyOrder, double minCorrelationCoeff) {
		_polyOrder = polyOrder;
		_minCorrelationCoeff = minCorrelationCoeff;
	}

	/**
	 * Method calculatePivot.
	 * 
	 * @param userDataVector
	 *            Hashtable<Long,Pair>
	 * @return boolean
	 */
	public boolean calculatePivot(List<Pair> pairs) {

		boolean isPivot = false;

		Collections.sort(pairs, Pair.X_VALUE_ASC);

		int size = pairs.size();
		if (size > 1) {
			Pair[] userData = pairs.toArray(new Pair[] {});
			double[] terms = MatrixFunctions.getCalculatedCoeffients(userData,
					_polyOrder);
			double correlationCoeff = MatrixFunctions
					.getCorrelationCoefficient(userData, terms);
			double standardError = MatrixFunctions.getStandardError(userData,
					terms);
			if (correlationCoeff > _minCorrelationCoeff) {
				isPivot = true;
				String output = MatrixFunctions
						.toPrint(_polyOrder, correlationCoeff, standardError,
								terms, userData.length);
				_log.info("Pivot Calc: " + output);
				for (Pair pair : pairs) {
					double y = MatrixFunctions.fx(pair.x, terms);
					pair.y = y;
				}
			}
		}
		return isPivot;
	}
}
