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

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;

import org.trade.core.util.MatrixFunctions;
import org.trade.core.util.Pair;

/**
 * @author Simon Allen
 * 
 * @version $Revision: 1.0 $
 */
public class PivotCalculator {

	private int polyOrder = 2; // default order
	private double minCorrelationCoeff = 0.6;
	private int listingForm = 0;
	private MatrixFunctions m_matrixFunctions = new MatrixFunctions();
	String text = null;

	public PivotCalculator() {

	}

	/**
	 * Method calculatePivot.
	 * @param userDataVector Hashtable<Long,Pair>
	 * @return boolean
	 */
	public boolean calculatePivot(Hashtable<Long, Pair> userDataVector) {

		boolean isPivot = false;
		polyOrder = (polyOrder < 0) ? 0 : polyOrder;
		polyOrder = (polyOrder > 512) ? 512 : polyOrder;

		int size = userDataVector.size();
		if (size > 1) {

			Collection<Pair> pairs = userDataVector.values();
			Pair[] userData = pairs.toArray(new Pair[] {});
			double[] terms = m_matrixFunctions.getCalculatedCoeffients(
					userData, polyOrder);
			double correlationCoeff = m_matrixFunctions
					.getCorrelationCoefficient(userData, terms);
			double standardError = m_matrixFunctions.getStandardError(userData,
					terms);
			if (correlationCoeff > minCorrelationCoeff) {
				isPivot = true;
				toPrint(polyOrder, correlationCoeff, standardError, terms,
						userData.length);
				for (Enumeration<Pair> enumPairs = userDataVector.elements(); enumPairs
						.hasMoreElements();) {
					Pair pair = enumPairs.nextElement();
					double y = fx(pair.x, terms);
					pair.y = y;
				}
			}
		}

		return isPivot;
	}

	/**
	 * Method getPrint.
	 * @return String
	 */
	public String getPrint() {
		return text;
	}

	/**
	 * Method toPrint.
	 * @param polyOrder int
	 * @param result_cc double
	 * @param result_se double
	 * @param terms double[]
	 * @param dataPoints int
	 * @return String
	 */
	private String toPrint(int polyOrder, double result_cc, double result_se,
			double[] terms, int dataPoints) {

		String styleTag[] = { "", "pow", "Math.pow" };
		int n = dataPoints;
		text = "Degree " + polyOrder + ", " + n + " x,y pairs. ";
		text += "Corr. coeff. (r^2) = " + formatNum(result_cc, false) + ". ";
		text += "SE = " + formatNum(result_se, false) + "\n\n";
		text += (listingForm > 0) ? "double f(double x) {\n    return"
				: "f(x) =";
		for (int i = 0; i <= polyOrder; i++) {
			double a = terms[i];
			if (i > 0) {
				if (listingForm > 0) {
					text += "    ";
				}
				text += "     +";
			}
			text += formatNum(a, true);
			if (i == 1) {
				text += " * x";
			}
			if (i > 1) {
				if (listingForm > 0) {
					text += (" * " + styleTag[listingForm] + "(x," + i + ")");
				} else {
					text += (" * x^" + i);
				}
			}
			if (i < polyOrder) {
				text += "\n";
			}
		}
		if (listingForm > 0) {
			text += ";\n}";
		}
		if (polyOrder > (n - 1)) {
			text += "\n\nWarning: Polynomial degree exceeds data size - 1.";
		}

		return text;
	}

	/**
	 * Method formatNum.
	 * @param n double
	 * @param wide boolean
	 * @return String
	 */
	private String formatNum(double n, boolean wide) {
		String w = (wide) ? "21" : "";
		return String.format("%" + w + ".12e", n);
	}

	/*
	 * private void changeListingStyle() { listingForm++; listingForm %= 3;
	 * process(); }
	 */

	/**
	 * Method fx.
	 * @param x double
	 * @param terms double[]
	 * @return double
	 */
	private double fx(double x, double[] terms) {
		double a = 0;
		int e = 0;
		for (double i : terms) {
			a += i * Math.pow(x, e);
			e++;
		}
		return a;
	}
}
