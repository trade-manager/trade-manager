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

import java.util.Comparator;

/**
 * 
 * @author Simon Allen
 */
public final class Pair {

	public double x = 0;
	public double y = 0;

	/**
	 * Constructor for Pair.
	 * 
	 * @param x
	 *            double
	 * @param y
	 *            double
	 */
	public Pair(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		return x + "," + y;
	}

	public static final Comparator<Pair> X_VALUE_ASC = new Comparator<Pair>() {
		public int compare(Pair o1, Pair o2) {
			return CoreUtils.nullSafeComparator(o1.x, o2.x);
		}
	};
}
