/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2014, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ----------------------
 * RegularTimePeriod.java
 * ----------------------
 * (C) Copyright 2001-2014, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 11-Oct-2001 : Version 1 (DG);
 * 26-Feb-2002 : Changed getStart(), getMiddle() and getEnd() methods to
 *               evaluate with reference to a particular time zone (DG);
 * 29-May-2002 : Implemented MonthConstants interface, so that these constants
 *               are conveniently available (DG);
 * 10-Sep-2002 : Added getSerialIndex() method (DG);
 * 10-Jan-2003 : Renamed TimePeriod --> RegularTimePeriod (DG);
 * 13-Mar-2003 : Moved to com.jrefinery.data.time package (DG);
 * 29-Apr-2004 : Changed getMiddleMillisecond() methods to fix bug 943985 (DG);
 * 25-Nov-2004 : Added utility methods (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 06-Oct-2006 : Deprecated the WORKING_CALENDAR field and several methods,
 *               added new peg() method (DG);
 * 16-Sep-2008 : Deprecated DEFAULT_TIME_ZONE (DG);
 * 23-Feb-2014 : Added getMillisecond() method (DG);
 * 
 */

package org.trade.strategy.data.base;

import java.time.ZonedDateTime;

import org.trade.core.util.TradingCalendar;

/**
 * An abstract class representing a unit of time. Convenient methods are
 * provided for calculating the next and previous time periods. Conversion
 * methods are defined that return the first and last milliseconds of the time
 * period. The results from these methods are timezone dependent.
 * <P>
 * This class is immutable, and all subclasses should be immutable also.
 */
public abstract class RegularTimePeriod implements TimePeriod,
		Comparable<TimePeriod> {

	private static final String DATETIMEFORMAT = "MM/dd/yyyy HH:mm:ss";

	protected ZonedDateTime startOfPeriod = null;

	protected ZonedDateTime endOfPeriod = null;

	/**
	 * Returns the time period preceding this one, or <code>null</code> if some
	 * lower limit has been reached.
	 *
	 * @return The previous time period (possibly <code>null</code>).
	 */
	public abstract RegularTimePeriod previous();

	/**
	 * Returns the time period following this one, or <code>null</code> if some
	 * limit has been reached.
	 *
	 * @return The next time period (possibly <code>null</code>).
	 */
	public abstract RegularTimePeriod next();

	/**
	 * Returns a serial index number for the time unit.
	 *
	 * @return The serial index number.
	 */
	public abstract long getSerialIndex();

	/**
	 * Returns the date/time that marks the start of the time period. This
	 * method returns a new <code>Date</code> instance every time it is called.
	 *
	 * @return The start date/time.
	 *
	 * @see #getFirstMillisecond()
	 */
	public ZonedDateTime getStart() {
		return this.startOfPeriod;
	}

	/**
	 * Returns the date/time that marks the end of the time period. This method
	 * returns a new <code>Date</code> instance every time it is called.
	 *
	 * @return The end date/time.
	 *
	 * @see #getLastMillisecond()
	 */

	public ZonedDateTime getEnd() {
		return this.endOfPeriod;
	}

	/**
	 * Returns the first millisecond of the time period. This will be determined
	 * relative to the time zone specified in the constructor, or in the
	 * calendar instance passed in the most recent call to the
	 * {@link #peg(Calendar)} method.
	 *
	 * @return The first millisecond of the time period.
	 *
	 * @see #getLastMillisecond()
	 */
	public abstract long getFirstMillisecond();

	/**
	 * Returns the last millisecond of the time period. This will be determined
	 * relative to the time zone specified in the constructor, or in the
	 * calendar instance passed in the most recent call to the
	 * {@link #peg(Calendar)} method.
	 *
	 * @return The last millisecond of the time period.
	 *
	 * @see #getFirstMillisecond()
	 */
	public abstract long getLastMillisecond();

	/**
	 * Returns the millisecond closest to the middle of the time period.
	 *
	 * @return The middle millisecond.
	 */
	public long getMiddleMillisecond() {
		long m1 = getFirstMillisecond();
		long m2 = getLastMillisecond();
		return m1 + (m2 - m1) / 2;
	}

	/**
	 * Returns the millisecond (relative to the epoch) corresponding to the
	 * specified <code>anchor</code> using the supplied <code>calendar</code>
	 * (which incorporates a time zone).
	 * 
	 * @param anchor
	 *            the anchor (<code>null</code> not permitted).
	 * @param calendar
	 *            the calendar (<code>null</code> not permitted).
	 * 
	 * @return Milliseconds since the epoch.
	 * 
	 * @since 1.0.18
	 */
	public long getMillisecond(TimePeriodAnchor anchor) {
		if (anchor.equals(TimePeriodAnchor.START)) {
			return getFirstMillisecond();
		} else if (anchor.equals(TimePeriodAnchor.MIDDLE)) {
			return getMiddleMillisecond();
		} else if (anchor.equals(TimePeriodAnchor.END)) {
			return getLastMillisecond();
		} else {
			throw new IllegalStateException("Unrecognised anchor: " + anchor);
		}
	}

	/**
	 * Returns a string representation of the time period.
	 *
	 * @return The string.
	 */
	public boolean equals(TimePeriod obj) {
		if (obj == this) {
			return true;
		}
		if (this.getStart().equals(obj.getStart())
				&& this.getEnd().equals(obj.getEnd()))
			return true;
		return false;
	}

	/**
	 * Returns an integer indicating the order of this second object relative to
	 * the specified object:
	 * 
	 * negative == before, zero == same, positive == after.
	 * 
	 * @param o1
	 *            object to compare.
	 * 
	 * 
	 * @return negative == before, zero == same, positive == after.
	 */
	public int compareTo(TimePeriod o1) {

		int result = this.getStart().compareTo(o1.getStart());
		if (result == 0)
			return this.getEnd().compareTo(o1.getEnd());

		return result;
	}

	/**
	 * Returns a string representation of the time period.
	 *
	 * @return The string.
	 */

	public String toString() {
		return TradingCalendar
				.getFormattedDate(this.getStart(), DATETIMEFORMAT);
	}

}
