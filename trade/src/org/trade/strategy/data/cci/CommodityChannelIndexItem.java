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
package org.trade.strategy.data.cci;

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
public class CommodityChannelIndexItem extends ComparableObjectItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3888996139640449109L;

	/**
	 * Creates a new instance of <code>CandleItem</code>.
	 * 
	 * @param period
	 *            the time period.
	 * 
	 * 
	 * @param movingAverage
	 *            BigDecimal
	 */
	public CommodityChannelIndexItem(RegularTimePeriod period,
			BigDecimal cciAverage) {
		super(period, new CommodityChannelIndex(cciAverage));
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
		return getCommodityChannelIndex();
	}

	/**
	 * Set the moving Average value.
	 * 
	 * 
	 * @param movingAverage
	 *            double
	 */
	public void setCommodityChannelIndex(double cciAverage) {
		CommodityChannelIndex dataItem = (CommodityChannelIndex) getObject();
		if (dataItem != null) {
			dataItem.setCommodityChannelIndex(new BigDecimal(cciAverage));
		}

	}

	/**
	 * Returns the moving Average value.
	 * 
	 * 
	 * @return The moving Average value.
	 */
	public double getCommodityChannelIndex() {
		CommodityChannelIndex dataItem = (CommodityChannelIndex) getObject();
		if (dataItem != null) {
			if (null == dataItem.getCommodityChannelIndex()) {
				return 0;
			}
			return dataItem.getCommodityChannelIndex().doubleValue();
		} else {
			return 0;
		}
	}
}
