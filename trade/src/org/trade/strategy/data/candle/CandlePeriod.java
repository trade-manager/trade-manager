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
package org.trade.strategy.data.candle;

import java.io.Serializable;
import java.time.ZonedDateTime;

import org.trade.core.util.TradingCalendar;
import org.trade.strategy.data.base.RegularTimePeriod;

/**
 * Represents a minute. This class is immutable, which is a requirement for all
 * {@link RegularTimePeriod} subclasses.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class CandlePeriod extends RegularTimePeriod implements Serializable {

	/** For serialization. */
	private static final long serialVersionUID = 2144572840034842871L;

	private int secondsLength = 0;

	/**
	 * Constructs a new seconds Period, based on the system date/time. with
	 * duration of 5min
	 */
	public CandlePeriod() {
		this(TradingCalendar.getDateTimeNowMarketTimeZone(), TradingCalendar.getDateTimeNowMarketTimeZone());

	}

	/**
	 * Constructs a new seconds Period, based on the system date/time.
	 * 
	 * @param startOfPeriod
	 *            ZonedDateTime
	 * @param secondsLength
	 *            int
	 */
	public CandlePeriod(ZonedDateTime startOfPeriod, int secondsLength) {
		if (null == startOfPeriod) {
			throw new IllegalArgumentException("Null startOfPeriod argument.");
		}
		if (secondsLength == 0) {
			throw new IllegalArgumentException("Null 'secondsLength' argument.");
		}
		this.startOfPeriod = startOfPeriod;
		this.secondsLength = secondsLength;
		// this.endOfPeriod = this.startOfPeriod
		// .plusSeconds((this.secondsLength - 1));

		this.endOfPeriod = this.startOfPeriod.plusNanos((this.secondsLength * 1000000000l) - 1000000l);
	}

	/**
	 * Constructs a new second, based on the date/time.
	 * 
	 * @param startOfPeriod
	 *            ZonedDateTime
	 */
	public CandlePeriod(ZonedDateTime startOfPeriod) {
		this(startOfPeriod, startOfPeriod);
	}

	/**
	 * Constructs a new second, based on the supplied date/time and timezone.
	 * 
	 * @param startOfPeriod
	 *            ZonedDateTime
	 * @param endOfPeriod
	 *            ZonedDateTime
	 */
	public CandlePeriod(ZonedDateTime startOfPeriod, ZonedDateTime endOfPeriod) {
		if (startOfPeriod == null) {
			throw new IllegalArgumentException("Null startPeriod argument.");
		}
		if (endOfPeriod == null) {
			throw new IllegalArgumentException("Null endOfPeriod argument.");
		}
		this.startOfPeriod = startOfPeriod;
		this.endOfPeriod = endOfPeriod;
		this.secondsLength = (int) TradingCalendar.getDurationInSeconds(startOfPeriod, endOfPeriod);
	}

	/**
	 * Returns the second preceding this one.
	 * 
	 * 
	 * @return The second preceding this one.
	 */
	public RegularTimePeriod previous() {
		return new CandlePeriod(this.getStart().minusSeconds(secondsLength), secondsLength);

	}

	/**
	 * Returns the second following this one.
	 * 
	 * 
	 * @return The second following this one.
	 */
	public RegularTimePeriod next() {
		return new CandlePeriod(this.getStart().plusSeconds(secondsLength), secondsLength);
	}

	/**
	 * Returns a serial index number for the second.
	 * 
	 * 
	 * @return The serial index number.
	 */
	public long getSerialIndex() {

		long hourIndex = (this.getStart().getDayOfYear() * 24L) + this.getStart().getHour();
		return (hourIndex * 60L) + (this.getStart().getMinute() * 60L) + this.getStart().getSecond();
	}

	/**
	 * Returns a serial index number for the minute.
	 * 
	 * 
	 * @return The serial index number.
	 */
	public long getDaySerialIndex() {

		long hourIndex = this.getStart().getHour();
		return ((((hourIndex * 60L) + this.getStart().getMinute()) * 60L) + this.getStart().getSecond()) / 60;
	}

	/**
	 * Returns a hash code for this object instance. The approach described by
	 * Joshua Bloch in "Effective Java" has been used here:
	 * <p>
	 * <code>http://developer.java.sun.com/developer/Books/effectivejava
	 * /Chapter3.pdf</code>
	 * 
	 * 
	 * @return A hash code.
	 */
	public int hashCode() {
		int result = 17;
		if (null != this.getStart())
			result = result + this.getStart().hashCode();
		if (null != this.getEnd())
			result = result + this.getEnd().hashCode();
		return result;
	}

	public long getFirstMillisecond() {
		return TradingCalendar.geMillisFromZonedDateTime(this.startOfPeriod);
	}

	public long getLastMillisecond() {
		return TradingCalendar.geMillisFromZonedDateTime(this.endOfPeriod);
	}
}
