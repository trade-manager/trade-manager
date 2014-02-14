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
package org.trade.core.util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * 
 * @author Simon Allen
 */
public final class MatrixFunctions {

	private static int listingForm = 0;

	public MatrixFunctions() {

	}

	/**
	 * Method solve.
	 * 
	 * @param userDataVector
	 *            Hashtable<Long,Pair>
	 * @param polyOrder
	 *            Integer
	 * @return double[]
	 */
	public static synchronized double[] solve(Pair[] pairs, Integer polyOrder) {
		int size = pairs.length;
		if (size > 1) {
			return getCalculatedCoeffients(pairs, polyOrder);
		}
		return null;
	}

	/**
	 * Method updateXYPairs.
	 * 
	 * @param userDataVector
	 *            Hashtable<Long,Pair>
	 * @param terms
	 *            double[]
	 * @return boolean
	 */
	public static synchronized boolean updateXYPairs(
			Hashtable<Long, Pair> userDataVector, double[] terms) {
		boolean updated = false;
		for (Enumeration<Pair> enumPairs = userDataVector.elements(); enumPairs
				.hasMoreElements();) {
			Pair pair = enumPairs.nextElement();
			double y = fx(pair.x, terms);
			pair.y = y;
			updated = true;
		}
		return updated;
	}

	/**
	 * Method getCorrelationCoefficient.
	 * 
	 * @param data
	 *            Pair[]
	 * @param terms
	 *            double[]
	 * @return double
	 */
	public static synchronized double getCorrelationCoefficient(Pair[] data,
			double[] terms) {
		double r = 0;
		int n = data.length;
		double sx = 0, sx2 = 0, sy = 0, sy2 = 0, sxy = 0;
		double x, y;
		for (Pair pr : data) {
			x = fx(pr.x, terms);
			y = pr.y;
			sx += x;
			sy += y;
			sxy += x * y;
			sx2 += x * x;
			sy2 += y * y;
		}
		double div = Math.sqrt((sx2 - ((sx * sx) / n))
				* (sy2 - ((sy * sy) / n)));
		if (div != 0) {
			r = Math.pow((sxy - ((sx * sy) / n)) / div, 2);
		}
		return r;
	}

	/**
	 * Method getStandardError.
	 * 
	 * @param data
	 *            Pair[]
	 * @param terms
	 *            double[]
	 * @return double
	 */
	public static synchronized double getStandardError(Pair[] data,
			double[] terms) {
		double r = 0;
		int n = data.length;
		if (n > 2) {
			double a = 0;
			for (Pair pr : data) {
				a += Math.pow((fx(pr.x, terms) - pr.y), 2);
			}
			r = Math.sqrt(a / (n - 2));
		}
		return r;
	}

	/**
	 * Method getCalculatedCoeffients.
	 * 
	 * @param data
	 *            Pair[]
	 * @param p
	 *            int
	 * @return double[]
	 */
	public static synchronized double[] getCalculatedCoeffients(Pair[] data,
			int p) {
		p += 1;
		int n = data.length;
		int r, c;
		int rs = (2 * p) - 1;
		//
		// by request: read each datum only once
		// not the most efficient processing method
		// but required if the data set is huge
		//
		// create square matrix with added RH column
		double[][] m = new double[p][p + 1];
		// create array of precalculated matrix data
		double[] mpc = new double[rs];
		mpc[0] = n;
		for (Pair pr : data) {
			// process precalculation array
			for (r = 1; r < rs; r++) {
				mpc[r] += Math.pow(pr.x, r);
			}
			// process RH column cells
			m[0][p] += pr.y;
			for (r = 1; r < p; r++) {
				m[r][p] += Math.pow(pr.x, r) * pr.y;
			}
		}
		// populate square matrix section
		for (r = 0; r < p; r++) {
			for (c = 0; c < p; c++) {
				m[r][c] = mpc[r + c];
			}
		}
		// parent.show_mat(m);
		// reduce matrix
		gj_echelonize(m);
		// parent.show_mat(m);
		// extract rh column
		double[] result = new double[p];
		for (int j = 0; j < p; j++) {
			result[j] = m[j][p];
		}
		return result;
	}

	/**
	 * Method fx.
	 * 
	 * @param x
	 *            double
	 * @param terms
	 *            double[]
	 * @return double
	 */
	public static synchronized double fx(double x, double[] terms) {
		double a = 0;
		int e = 0;
		for (double i : terms) {
			a += i * Math.pow(x, e);
			e++;
		}
		return a;
	}

	/**
	 * Method gj_divide.
	 * 
	 * @param A
	 *            double[][]
	 * @param i
	 *            int
	 * @param j
	 *            int
	 * @param m
	 *            int
	 */
	private static synchronized void gj_divide(double[][] A, int i, int j, int m) {
		for (int q = j + 1; q < m; q++) {
			A[i][q] /= A[i][j];
		}
		A[i][j] = 1;
	}

	/**
	 * Method gj_eliminate.
	 * 
	 * @param A
	 *            double[][]
	 * @param i
	 *            int
	 * @param j
	 *            int
	 * @param n
	 *            int
	 * @param m
	 *            int
	 */
	private static synchronized void gj_eliminate(double[][] A, int i, int j,
			int n, int m) {
		for (int k = 0; k < n; k++) {
			if ((k != i) && (A[k][j] != 0)) {
				for (int q = j + 1; q < m; q++) {
					A[k][q] -= A[k][j] * A[i][q];
				}
				A[k][j] = 0;
			}
		}
	}

	/**
	 * Method gj_echelonize.
	 * 
	 * @param A
	 *            double[][]
	 */
	private static synchronized void gj_echelonize(double[][] A) {
		int n = A.length;
		int m = A[0].length;
		int i = 0;
		int j = 0;
		int k;
		double temp[];
		while ((i < n) && (j < m)) {
			// look for non-zero entries in col j at or below row i
			k = i;
			while ((k < n) && (A[k][j] == 0)) {
				k++;
			}
			// if an entry is found at row k
			if (k < n) {
				// if k is not i, then swap row i with row k
				if (k != i) {
					temp = A[i];
					A[i] = A[k];
					A[k] = temp;
				}
				// if A[i][j] is != 1, divide row i by A[i][j]
				if (A[i][j] != 1) {
					gj_divide(A, i, j, m);
				}
				// eliminate all other non-zero entries
				gj_eliminate(A, i, j, n, m);
				i++;
			}
			j++;
		}
	}

	/**
	 * Method toPrint.
	 * 
	 * @param polyOrder
	 *            int
	 * @param result_cc
	 *            double
	 * @param result_se
	 *            double
	 * @param terms
	 *            double[]
	 * @param dataPoints
	 *            int
	 * @return String
	 */
	public static synchronized String toPrint(int polyOrder,
			double correlationCoeff, double standardDeviation, double[] terms,
			int dataPoints) {

		String styleTag[] = { "", "pow", "Math.pow" };
		int n = dataPoints;
		String text = "Degree " + polyOrder + ", " + n + " x,y pairs. ";
		text += "Corr. coeff. (r^2) = " + formatNum(correlationCoeff, false)
				+ ". ";
		text += "SE = " + formatNum(standardDeviation, false) + "\n\n";
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
	 * 
	 * @param n
	 *            double
	 * @param wide
	 *            boolean
	 * @return String
	 */
	private static synchronized String formatNum(double n, boolean wide) {
		String w = (wide) ? "21" : "";
		return String.format("%" + w + ".12e", n);
	}
}
