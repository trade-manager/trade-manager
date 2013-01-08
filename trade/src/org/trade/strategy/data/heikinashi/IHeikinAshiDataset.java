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
package org.trade.strategy.data.heikinashi;

import org.jfree.data.xy.XYDataset;

/**
 * An interface that defines data in the form of (x, high, low, open, close)
 * tuples.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public interface IHeikinAshiDataset extends XYDataset {

	/**
	 * Returns the high-value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The value.
	 */
	public Number getHigh(int series, int item);

	/**
	 * Returns the high-value (as a double primitive) for an item within a
	 * series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The high-value.
	 */
	public double getHighValue(int series, int item);

	/**
	 * Returns the low-value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The value.
	 */
	public Number getLow(int series, int item);

	/**
	 * Returns the low-value (as a double primitive) for an item within a
	 * series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The low-value.
	 */
	public double getLowValue(int series, int item);

	/**
	 * Returns the open-value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The value.
	 */
	public Number getOpen(int series, int item);

	/**
	 * Returns the open-value (as a double primitive) for an item within a
	 * series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The open-value.
	 */
	public double getOpenValue(int series, int item);

	/**
	 * Returns the y-value for the specified series and item.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The value.
	 */
	public Number getClose(int series, int item);

	/**
	 * Returns the close-value (as a double primitive) for an item within a
	 * series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * @param item
	 *            the item (zero-based index).
	 * 
	 * 
	 * @return The close-value.
	 */
	public double getCloseValue(int series, int item);

}
