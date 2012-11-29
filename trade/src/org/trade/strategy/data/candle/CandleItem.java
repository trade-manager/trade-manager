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
package org.trade.strategy.data.candle;

import java.math.BigDecimal;
import java.util.Date;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.time.RegularTimePeriod;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Tradingday;

/**
 * An item representing data in the form (period, open, high, low, close).
 * 
 * @since 1.0.4
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class CandleItem extends ComparableObjectItem {

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
	 * @param volume
	 *            the volume value.
	 * @param vwap
	 *            the volume weighted price.
	 * 
	 * @param contract
	 *            Contract
	 * @param count
	 *            int
	 * @param lastUpdateDate
	 *            Date
	 */
	public CandleItem(Contract contract, Tradingday tradingday,
			RegularTimePeriod period, double open, double high, double low,
			double close, long volume, double vwap, int count,
			Date lastUpdateDate) {
		super(period, new Candle(contract, tradingday, period, open, high, low,
				close, volume, vwap, count, lastUpdateDate));
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
	 * Returns the period.
	 * 
	 * 
	 * @return The period (never <code>null</code>).
	 */
	public Candle getCandle() {
		return (Candle) getObject();
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
		if (getCandle() != null) {
			return getCandle().getOpen().doubleValue();
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
		if (getCandle() != null) {
			return getCandle().getHigh().doubleValue();
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
		if (getCandle() != null) {
			return getCandle().getLow().doubleValue();
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
		if (getCandle() != null) {
			return getCandle().getClose().doubleValue();
		} else {
			return Double.NaN;
		}
	}

	/**
	 * Returns the volume value.
	 * 
	 * 
	 * @return The volume value.
	 */
	public long getVolume() {
		if (getCandle() != null) {
			return getCandle().getVolume().longValue();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the volume value.
	 * 
	 * 
	 * @return The volume value.
	 */
	public int getCount() {
		if (getCandle() != null) {
			return getCandle().getTradeCount().intValue();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the volume value.
	 * 
	 * 
	 * @return The volume value.
	 */
	public double getVwap() {
		if (getCandle() != null) {
			return getCandle().getVwap().doubleValue();
		} else {
			return 0;
		}
	}

	/**
	 * Set the open value.
	 * 
	 * 
	 * @param open
	 *            double
	 */
	public void setOpen(double open) {
		if (getCandle() != null) {
			getCandle().setOpen(new BigDecimal(open));
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
		if (getCandle() != null) {
			getCandle().setClose(new BigDecimal(close));
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
		if (getCandle() != null) {
			getCandle().setHigh(new BigDecimal(high));
		}
	}

	/**
	 * Set the high value.
	 * 
	 * 
	 * @param count
	 *            int
	 */
	public void setCount(int count) {
		if (getCandle() != null) {
			getCandle().setTradeCount(new Integer(count));
		}
	}

	/**
	 * Set the high value.
	 * 
	 * 
	 * @param vwap
	 *            double
	 */
	public void setVwap(double vwap) {
		if (getCandle() != null) {
			getCandle().setVwap(new BigDecimal(vwap));
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
		if (getCandle() != null) {
			getCandle().setLow(new BigDecimal(low));
		}
	}

	/**
	 * Set the volume value.
	 * 
	 * 
	 * @param volume
	 *            long
	 */
	public void setVolume(long volume) {
		if (getCandle() != null) {
			getCandle().setVolume(new Long(volume));
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
		if (Side.BOT.equals(side)) {
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
	 *            Date
	 */
	public void setLastUpdateDate(Date lastUpdateDate) {
		if (getCandle() != null) {
			getCandle().setLastUpdateDate(lastUpdateDate);
		}
	}

	/**
	 * Returns the lastUpdateDate value.
	 * 
	 * 
	 * @return The lastUpdateDate value.
	 */
	public Date getLastUpdateDate() {
		if (getCandle() != null) {
			return getCandle().getLastUpdateDate();
		} else {
			return null;
		}
	}
}
