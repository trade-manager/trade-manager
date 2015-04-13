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
import java.time.ZonedDateTime;

import org.jfree.data.ComparableObjectItem;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Tradingday;
import org.trade.strategy.data.base.RegularTimePeriod;

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
			ZonedDateTime lastUpdateDate) {
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
		if (null != getCandle())
			return getCandle().getOpen().doubleValue();
		return Double.NaN;
	}

	/**
	 * Returns the high value.
	 * 
	 * 
	 * @return The high value.
	 */
	public double getHigh() {
		if (null != getCandle())
			return getCandle().getHigh().doubleValue();
		return Double.NaN;
	}

	/**
	 * Returns the low value.
	 * 
	 * 
	 * @return The low value.
	 */
	public double getLow() {
		if (null != getCandle())
			return getCandle().getLow().doubleValue();
		return Double.NaN;

	}

	/**
	 * Returns the close value.
	 * 
	 * 
	 * @return The close value.
	 */
	public double getClose() {
		if (null != getCandle())
			return getCandle().getClose().doubleValue();
		return Double.NaN;

	}

	/**
	 * Returns the volume value.
	 * 
	 * 
	 * @return The volume value.
	 */
	public long getVolume() {
		if (null != getCandle())
			return getCandle().getVolume().longValue();
		return 0;
	}

	/**
	 * Returns the trade count value.
	 * 
	 * 
	 * @return The trade count value.
	 */
	public int getCount() {
		if (null != getCandle())
			return getCandle().getTradeCount().intValue();
		return 0;
	}

	/**
	 * Returns the Vwap value.
	 * 
	 * 
	 * @return The Vwap value.
	 */
	public double getVwap() {
		if (null != getCandle())
			return getCandle().getVwap().doubleValue();
		return 0;
	}

	/**
	 * Set the vwap value.
	 * 
	 * 
	 * @param vwap
	 *            double
	 */
	public void setVwap(double vwap) {
		if (null != getCandle())
			getCandle().setVwap(new BigDecimal(vwap));
	}

	/**
	 * Set the open value.
	 * 
	 * 
	 * @param open
	 *            double
	 */
	public void setOpen(double open) {
		if (null != getCandle())
			getCandle().setOpen(new BigDecimal(open));
	}

	/**
	 * Set the close value.
	 * 
	 * 
	 * @param close
	 *            double
	 */
	public void setClose(double close) {
		if (null != getCandle())
			getCandle().setClose(new BigDecimal(close));
	}

	/**
	 * Set the high value.
	 * 
	 * 
	 * @param high
	 *            double
	 */
	public void setHigh(double high) {
		if (null != getCandle())
			getCandle().setHigh(new BigDecimal(high));
	}

	/**
	 * Set the count value.
	 * 
	 * 
	 * @param count
	 *            int
	 */
	public void setCount(int count) {
		if (null != getCandle())
			getCandle().setTradeCount(new Integer(count));
	}

	/**
	 * Set the low value.
	 * 
	 * 
	 * @param low
	 *            double
	 */
	public void setLow(double low) {
		if (null != getCandle())
			getCandle().setLow(new BigDecimal(low));
	}

	/**
	 * Set the volume value.
	 * 
	 * 
	 * @param volume
	 *            long
	 */
	public void setVolume(long volume) {
		if (null != getCandle())
			getCandle().setVolume(new Long(volume));
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
		if (Side.BOT.equals(side))
			return getSide();
		return (getSide() ? false : true);
	}

	/**
	 * Set the lastUpdateDate value.
	 * 
	 * 
	 * @param lastUpdateDate
	 *            ZonedDateTime
	 */
	public void setLastUpdateDate(ZonedDateTime lastUpdateDate) {
		if (null != getCandle())
			getCandle().setLastUpdateDate(lastUpdateDate);
	}

	/**
	 * Returns the lastUpdateDate value.
	 * 
	 * 
	 * @return The lastUpdateDate value.
	 */
	public ZonedDateTime getLastUpdateDate() {
		if (null != getCandle())
			return getCandle().getLastUpdateDate();
		return null;

	}

	/**
	 * Set the version value.
	 * 
	 * @param version
	 *            Integer
	 */
	public void setVersion(Integer version) {
		if (null != getCandle())
			getCandle().setVersion(version);
	}

	/**
	 * Returns the version value.
	 * 
	 * 
	 * @return The version value.
	 */
	public Integer getVersion() {
		if (null != getCandle())
			return getCandle().getVersion();
		return null;

	}
}
