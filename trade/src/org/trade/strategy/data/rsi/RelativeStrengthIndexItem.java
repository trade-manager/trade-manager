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
package org.trade.strategy.data.rsi;

import java.math.BigDecimal;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.time.RegularTimePeriod;

/**
 * An item representing data in the form (period, open, high, low, close).
 * 
 * @since 1.0.4
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class RelativeStrengthIndexItem extends ComparableObjectItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3888996139640449109L;

	/**
	 * Creates a new instance of <code>RelativeStrengthIndexItem</code>.
	 * 
	 * @param period
	 *            the time period.
	 * @param relativeStrengthIndex
	 *            the relativeStrengthIndex.
	 */
	public RelativeStrengthIndexItem(RegularTimePeriod period,
			BigDecimal relativeStrengthIndex) {
		super(period, new RelativeStrengthIndex(relativeStrengthIndex));
	}

	/**
	 * Returns the period.
	 * 
	
	 * @return The period (never <code>null</code>). */
	public RegularTimePeriod getPeriod() {
		return (RegularTimePeriod) getComparable();
	}

	/**
	 * Returns the y-value.
	 * 
	
	 * @return The y-value. */
	public double getY() {
		return getRelativeStrengthIndex();
	}

	/**
	 * Set the relativeStrengthIndex value.
	 * 
	
	 * @param relativeStrengthIndex double
	 */
	public void setRelativeStrengthIndex(double relativeStrengthIndex) {
		RelativeStrengthIndex dataItem = (RelativeStrengthIndex) getObject();
		if (dataItem != null) {
			dataItem.setRelativeStrengthIndex(new BigDecimal(
					relativeStrengthIndex));
		}

	}

	/**
	 * Returns the relativeStrengthIndex value.
	 * 
	
	 * @return The relativeStrengthIndex value. */
	public double getRelativeStrengthIndex() {
		RelativeStrengthIndex dataItem = (RelativeStrengthIndex) getObject();
		if (dataItem != null) {
			if (null == dataItem.getRelativeStrengthIndex()) {
				return 0;
			}
			return dataItem.getRelativeStrengthIndex().doubleValue();
		} else {
			return 0;
		}
	}
}
