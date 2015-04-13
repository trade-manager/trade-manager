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
package org.trade.strategy.data.macd;

import java.math.BigDecimal;

import org.jfree.data.ComparableObjectItem;
import org.trade.strategy.data.base.RegularTimePeriod;

/**
 * An item representing data for MACD.
 * 
 * @since 1.0.4
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class MACDItem extends ComparableObjectItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3888996139640449109L;

	/**
	 * Creates a new instance of <code>MACDItem</code>.
	 * 
	 * @param period
	 *            the time period.
	 * 
	 * 
	 * @param MACD
	 *            BigDecimal
	 */
	public MACDItem(RegularTimePeriod period, BigDecimal MACD,
			BigDecimal signalLine, BigDecimal MACDHistogram) {
		super(period, new MACD(MACD, signalLine, MACDHistogram));
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
		return getMACD();
	}

	/**
	 * Set the MACD value.
	 * 
	 * 
	 * @param MACD
	 *            double
	 */
	public void setMACD(double MACD) {
		MACD dataItem = (MACD) getObject();
		if (dataItem != null) {
			dataItem.setMACD(new BigDecimal(MACD));
		}

	}

	/**
	 * Returns the MACD value.
	 * 
	 * 
	 * @return The MACD value.
	 */
	public double getMACD() {
		MACD dataItem = (MACD) getObject();
		if (dataItem != null) {
			if (null == dataItem.getMACD()) {
				return 0;
			}
			return dataItem.getMACD().doubleValue();
		} else {
			return 0;
		}
	}

	/**
	 * Set the SignalLine value.
	 * 
	 * 
	 * @param SignalLine
	 *            double
	 */
	public void setSignalLine(double SignalLine) {
		MACD dataItem = (MACD) getObject();
		if (dataItem != null) {
			dataItem.setSignalLine(new BigDecimal(SignalLine));
		}

	}

	/**
	 * Returns the SignalLine value.
	 * 
	 * 
	 * @return The SignalLine value.
	 */
	public double getSignalLine() {
		MACD dataItem = (MACD) getObject();
		if (dataItem != null) {
			if (null == dataItem.getSignalLine()) {
				return 0;
			}
			return dataItem.getSignalLine().doubleValue();
		} else {
			return 0;
		}
	}

	/**
	 * Set the MACDHistogram value.
	 * 
	 * 
	 * @param MACD
	 *            double
	 */
	public void setMACDHistogram(double MACD) {
		MACD dataItem = (MACD) getObject();
		if (dataItem != null) {
			dataItem.setMACDHistogram(new BigDecimal(MACD));
		}

	}

	/**
	 * Returns the MACDHistogram value.
	 * 
	 * 
	 * @return The MACDHistogram value.
	 */
	public double getMACDHistogram() {
		MACD dataItem = (MACD) getObject();
		if (dataItem != null) {
			if (null == dataItem.getMACDHistogram()) {
				return 0;
			}
			return dataItem.getMACDHistogram().doubleValue();
		} else {
			return 0;
		}
	}
}
