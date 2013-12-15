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
package org.trade.strategy.data.mfi;

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
public class MoneyFlowIndexItem extends ComparableObjectItem {

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
	 * @param moneyFlowIndex
	 *            BigDecimal
	 */
	public MoneyFlowIndexItem(RegularTimePeriod period,
			BigDecimal moneyFlowIndex) {
		super(period, new MoneyFlowIndex(moneyFlowIndex));
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
		return getMoneyFlowIndex();
	}

	/**
	 * Set the Money Flow Index value.
	 * 
	 * 
	 * @param moneyFlowIndex
	 *            double
	 */
	public void setMoneyFlowIndex(double moneyFlowIndex) {
		MoneyFlowIndex dataItem = (MoneyFlowIndex) getObject();
		if (dataItem != null) {
			dataItem.setMoneyFlowIndex(new BigDecimal(moneyFlowIndex));
		}

	}

	/**
	 * Returns the Money Flow Index.
	 * 
	 * @return The Money Flow Index value.
	 */
	public double getMoneyFlowIndex() {
		MoneyFlowIndex dataItem = (MoneyFlowIndex) getObject();
		if (dataItem != null) {
			if (null == dataItem.getMoneyFlowIndex()) {
				return 0;
			}
			return dataItem.getMoneyFlowIndex().doubleValue();
		} else {
			return 0;
		}
	}
}
