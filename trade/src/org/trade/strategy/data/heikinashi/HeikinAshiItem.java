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

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.jfree.data.ComparableObjectItem;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.strategy.data.base.RegularTimePeriod;

/**
 * An item representing data in the form (period, open, high, low, close).
 * 
 * @since 1.0.4
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class HeikinAshiItem extends ComparableObjectItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3888996139640449109L;

	/**
	 * Creates a new instance of <code>CandleItem</code>.
	 * 
	 * 
	 * @param period
	 *            the time period.
	 * @param open
	 *            the open-value.
	 * @param high
	 *            the high-value.
	 * @param low
	 *            the low-value.
	 * @param close
	 *            the close-value.
	 * 
	 * 
	 * 
	 * @param contract
	 *            Contract
	 * @param lastUpdateDate
	 *            ZonedDateTime
	 */
	public HeikinAshiItem(Contract contract, RegularTimePeriod period,
			double open, double high, double low, double close,
			ZonedDateTime lastUpdateDate) {
		super(period, new Candle(contract, period, open, high, low, close,
				lastUpdateDate));
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
		return getClose();
	}

	/**
	 * Returns the open value.
	 * 
	 * 
	 * @return The open value.
	 */
	public double getOpen() {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			return dataItem.getOpen().doubleValue();
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Returns the high value.
	 * 
	 * 
	 * @return The high value.
	 */
	public double getHigh() {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			return dataItem.getHigh().doubleValue();
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Returns the low value.
	 * 
	 * 
	 * @return The low value.
	 */
	public double getLow() {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			return dataItem.getLow().doubleValue();
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Returns the close value.
	 * 
	 * 
	 * @return The close value.
	 */
	public double getClose() {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			return dataItem.getClose().doubleValue();
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Returns the period.
	 * 
	 * 
	 * @return The period (never <code>null</code>).
	 */
	public Candle getCandle() {
		return (Candle) getObject();
	}

	/**
	 * Set the open value.
	 * 
	 * 
	 * @param open
	 *            double
	 */
	public void setOpen(double open) {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			dataItem.setOpen(new BigDecimal(open));
		}

	}

	/**
	 * Set the close value.
	 * 
	 * 
	 * @param close
	 *            double
	 */
	public void setClose(double close) {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			dataItem.setClose(new BigDecimal(close));
		}

	}

	/**
	 * Set the high value.
	 * 
	 * 
	 * @param high
	 *            double
	 */
	public void setHigh(double high) {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			dataItem.setHigh(new BigDecimal(high));
		}

	}

	/**
	 * Set the low value.
	 * 
	 * 
	 * @param low
	 *            double
	 */
	public void setLow(double low) {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			dataItem.setLow(new BigDecimal(low));
		}

	}

	/**
	 * Set the bars side true is green false is red.
	 * 
	 * 
	 * @return boolean The side of the bar true is green false is red.
	 */
	public boolean getSide() {

		return this.getClose() >= this.getOpen();
	}

	/**
	 * Method isSide.
	 * 
	 * @param side
	 *            String
	 * @return boolean
	 */
	public boolean isSide(String side) {

		if (side.equals(Side.BOT)) {
			return getSide();

		} else {
			return (getSide() ? false : true);
		}
	}

	/**
	 * Set the lastUpdateDate value.
	 * 
	 * 
	 * @param lastUpdateDate
	 *            ZonedDateTime
	 */
	public void setLastUpdateDate(ZonedDateTime lastUpdateDate) {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			dataItem.setLastUpdateDate(lastUpdateDate);
		}

	}

	/**
	 * Returns the lastUpdateDate value.
	 * 
	 * 
	 * @return The lastUpdateDate value.
	 */
	public ZonedDateTime getLastUpdateDate() {
		Candle dataItem = (Candle) getObject();
		if (dataItem != null) {
			return dataItem.getLastUpdateDate();
		} else {
			return null;
		}
	}

}
