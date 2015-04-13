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
package org.trade.strategy.data.atr;

import java.math.BigDecimal;

import org.jfree.data.ComparableObjectItem;
import org.trade.strategy.data.base.RegularTimePeriod;

/**
 * An item representing data in the form (period, open, high, low, close).
 * 
 * @since 1.0.4
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class AverageTrueRangeItem extends ComparableObjectItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3888996139640449109L;

	/**
	 * Creates a new instance of <code>CandleItem</code>.
	 * 
	 * @param period
	 *            the time period.
	 * @param averageTrueRange
	 *            the AverageTrueRange.
	 */
	public AverageTrueRangeItem(RegularTimePeriod period,
			BigDecimal averageTrueRange) {
		super(period, new AverageTrueRange(averageTrueRange));
	}

	/**
	 * Returns the period.
	 * 
	 * 
	 * @return The period (never <code>null</code>).
	 */
	public RegularTimePeriod getPeriod() {
		return (RegularTimePeriod) getComparable();
	}

	/**
	 * Returns the y-value.
	 * 
	 * 
	 * @return The y-value.
	 */
	public double getY() {
		return getAverageTrueRange();
	}

	/**
	 * Set the averageTrueRange value.
	 * 
	 * 
	 * @param averageTrueRange
	 *            double
	 */
	public void setAverageTrueRange(double averageTrueRange) {
		AverageTrueRange dataItem = (AverageTrueRange) getObject();
		if (dataItem != null) {
			dataItem.setAverageTrueRange(new BigDecimal(averageTrueRange));
		}

	}

	/**
	 * Returns the averageTrueRange value.
	 * 
	 * 
	 * @return The averageTrueRange value.
	 */
	public double getAverageTrueRange() {
		AverageTrueRange dataItem = (AverageTrueRange) getObject();
		if (dataItem != null) {
			if (null == dataItem.getAverageTrueRange()) {
				return 0;
			}
			return dataItem.getAverageTrueRange().doubleValue();
		} else {
			return 0;
		}
	}
}
