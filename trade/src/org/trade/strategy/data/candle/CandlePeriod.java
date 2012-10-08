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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.trade.core.util.TradingCalendar;

/**
 * Represents a minute. This class is immutable, which is a requirement for all
 * {@link RegularTimePeriod} subclasses.
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class CandlePeriod extends RegularTimePeriod implements Serializable {

	/** For serialization. */
	private static final long serialVersionUID = 2144572840034842871L;

	/** Useful constant for the first minute in a day. */
	public static final int FIRST_MINUTE_IN_HOUR = 0;

	/** Useful constant for the last minute in a day. */
	public static final int LAST_MINUTE_IN_HOUR = 59;

	/** Useful constant for the first minute in a day. */
	public static final int FIRST_SECOND_IN_MINUTE = 0;

	/** Useful constant for the last minute in a day. */
	public static final int LAST_SECOND_IN_MINUTE = 59;

	private static final String DATETIMEFORMAT = "MM/dd/yyyy HH:mm:ss";

	public static final TimeZone TIMEZONE_NY = TimeZone
			.getTimeZone("America/New_York");

	public static final Locale locale = Locale.getDefault();

	/** The day. */
	private Day day;

	/** The hour in which the minute falls. */
	private byte startHour;

	/** The hour in which the minute falls. */
	private byte endHour;

	/** The start minute. */
	private byte startMinute;

	/** The end minute. */
	private byte endMinute;

	/** The start second. */
	private byte startSecond;

	/** The end second. */
	private byte endSecond;

	/** The first millisecond. */
	private long firstMillisecond;

	/** The last millisecond. */
	private long lastMillisecond;

	/** Period length */
	private int secondsLength = 0;

	/**
	 * Constructs a new seconds Period, based on the system date/time. with
	 * duration of 5min
	 */
	public CandlePeriod() {
		this(new Date(), new Date());

	}

	/**
	 * Constructs a new seconds Period, based on the system date/time.
	 * @param startTime Date
	 * @param secondsLength int
	 */
	public CandlePeriod(Date startTime, int secondsLength) {
		if (null == startTime) {
			throw new IllegalArgumentException("Null 'time' argument.");
		}
		if (secondsLength == 0) {
			throw new IllegalArgumentException("Null 'minutesLength' argument.");
		}
		this.secondsLength = secondsLength;

		Calendar calendar = new GregorianCalendar(TIMEZONE_NY, locale);
		calendar.setTime(startTime);
		this.startHour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		this.startMinute = (byte) calendar.get(Calendar.MINUTE);
		this.startSecond = (byte) calendar.get(Calendar.SECOND);

		calendar.add(Calendar.SECOND, (secondsLength - 1));
		this.endHour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		this.endMinute = (byte) calendar.get(Calendar.MINUTE);
		this.endSecond = (byte) calendar.get(Calendar.SECOND);
		this.day = new Day(startTime, TIMEZONE_NY, locale);
		peg(calendar);

	}

	/**
	 * Constructs a new second, based on the date/time.
	 * @param date Date
	 */
	public CandlePeriod(Date date) {
		this(date, date);
	}

	/**
	 * Constructs a new second.
	 * 
	
	
	 * @param startSecond int
	 * @param endSecond int
	 * @param startMinute int
	 * @param endMinute int
	 * @param startHour int
	 * @param endHour int
	 * @param day Day
	 */
	public CandlePeriod(int startSecond, int endSecond, int startMinute,
			int endMinute, int startHour, int endHour, Day day) {
		this.startHour = (byte) startHour;
		this.startMinute = (byte) startMinute;
		this.startSecond = (byte) startSecond;
		this.endHour = (byte) endHour;
		this.endMinute = (byte) endMinute;
		this.endSecond = (byte) endSecond;
		this.secondsLength = (((this.endHour * 3600) + (this.endMinute * 60) + this.endSecond) - ((this.startHour * 3600)
				+ (this.startMinute * 60) + this.startSecond)) + 1;
		this.day = day;
		peg(Calendar.getInstance());
	}

	/**
	 * Constructs a new second, based on the supplied date/time and timezone.
	 * 
	
	
	
	 * 
	 * @since 1.0.13
	 * @param startTime Date
	 * @param endTime Date
	 */
	public CandlePeriod(Date startTime, Date endTime) {
		if (startTime == null) {
			throw new IllegalArgumentException("Null 'time' argument.");
		}
		if (endTime == null) {
			throw new IllegalArgumentException("Null 'time' argument.");
		}
		if (locale == null) {
			throw new IllegalArgumentException("Null 'locale' argument.");
		}
		Calendar calendar = new GregorianCalendar(TIMEZONE_NY, locale);
		calendar.setTime(startTime);
		this.startHour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		this.startMinute = (byte) calendar.get(Calendar.MINUTE);
		this.startSecond = (byte) calendar.get(Calendar.SECOND);
		calendar.setTime(endTime);
		this.endHour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
		this.endMinute = (byte) calendar.get(Calendar.MINUTE);
		this.endSecond = (byte) calendar.get(Calendar.SECOND);
		this.secondsLength = (int) (endTime.getTime() - startTime.getTime()) / 1000 + 1;
		this.day = new Day(startTime, TIMEZONE_NY, locale);
		peg(calendar);
	}

	/**
	 * Creates a new second.
	 * 
	
	
	 * @param day
	 *            the day (1-31).
	 * @param month
	 *            the month (1-12).
	 * @param year
	 *            the year (1900-9999).
	 * @param startSecond int
	 * @param endSecond int
	 * @param startMinute int
	 * @param endMinute int
	 * @param startHour int
	 * @param endHour int
	 */
	public CandlePeriod(int startSecond, int endSecond, int startMinute,
			int endMinute, int startHour, int endHour, int day, int month,
			int year) {
		this(startSecond, endSecond, startMinute, endMinute, startHour,
				endHour, new Day(day, month, year));
	}

	/**
	 * Returns the day.
	 * 
	
	 * 
	 * @since 1.0.3
	 * @return The day. */
	public int getNumberOfSeconds() {
		return this.secondsLength;
	}

	/**
	 * Returns the day.
	 * 
	
	 * 
	 * @since 1.0.3
	 * @return The day. */
	public Day getDay() {
		return this.day;
	}

	/**
	 * Returns the hour.
	 * 
	
	 * @return The hour (never <code>null</code>). */
	public Hour getStartHour() {
		return new Hour(this.startHour, this.day);
	}

	/**
	 * Returns the hour.
	 * 
	
	 * @return The hour (never <code>null</code>). */
	public Hour getEndHour() {
		return new Hour(this.endHour, this.day);
	}

	/**
	 * Returns the minute.
	 * 
	
	 * @return The minute. */
	public Minute getStartMinute() {
		return new Minute(this.startMinute, new Hour(this.startHour, this.day));
	}

	/**
	 * Returns the minute.
	 * 
	
	 * @return The minute. */
	public Minute getEndMinute() {
		return new Minute(this.endMinute, new Hour(this.endHour, this.day));
	}

	/**
	 * Returns the second.
	 * 
	
	 * @return The second. */
	public int getStartSecond() {
		return this.startSecond;
	}

	/**
	 * Returns the second.
	 * 
	
	 * @return The second. */
	public int getEndSecond() {
		return this.endSecond;
	}

	/**
	 * Returns the first millisecond of the minute. This will be determined
	 * relative to the time zone specified in the constructor, or in the
	 * calendar instance passed in the most recent call to the
	 * {@link #peg(Calendar)} method.
	 * 
	
	 * 
	
	 * @return The first millisecond of the minute. * @see #getLastMillisecond() */
	public long getFirstMillisecond() {
		return this.firstMillisecond;
	}

	/**
	 * Returns the last millisecond of the minute. This will be determined
	 * relative to the time zone specified in the constructor, or in the
	 * calendar instance passed in the most recent call to the
	 * {@link #peg(Calendar)} method.
	 * 
	
	 * 
	
	 * @return The last millisecond of the minute. * @see #getFirstMillisecond() */
	public long getLastMillisecond() {
		return this.lastMillisecond;
	}

	/**
	 * Recalculates the start date/time and end date/time for this time period
	 * relative to the supplied calendar (which incorporates a time zone).
	 * 
	 * @param calendar
	 *            the calendar (<code>null</code> not permitted).
	 * 
	 * @since 1.0.3
	 */
	public void peg(Calendar calendar) {
		this.firstMillisecond = getFirstMillisecond(calendar);
		this.lastMillisecond = getLastMillisecond(calendar);
	}

	/**
	 * Returns the second preceding this one.
	 * 
	
	 * @return The second preceding this one. */
	public RegularTimePeriod previous() {
		CandlePeriod result = null;
		Date prevDate = TradingCalendar.addSeconds(this.getStart(), -1
				* secondsLength);
		if (prevDate
				.before(TradingCalendar.getBusinessDayStart(this.getStart()))) {
			prevDate = TradingCalendar.getPrevTradingDay(this.getStart());
		}
		result = new CandlePeriod(prevDate, secondsLength);

		return result;
	}

	/**
	 * Returns the second following this one.
	 * 
	
	 * @return The second following this one. */
	public RegularTimePeriod next() {
		CandlePeriod result = null;

		Date nextDate = TradingCalendar.addSeconds(this.getStart(),
				secondsLength);
		if (nextDate.after(TradingCalendar.getBusinessDayEnd(this.getStart()))) {
			nextDate = TradingCalendar.getNextTradingDay(this.getStart());
		}
		result = new CandlePeriod(nextDate, secondsLength);

		return result;
	}

	/**
	 * Returns a serial index number for the second.
	 * 
	
	 * @return The serial index number. */
	public long getSerialIndex() {

		long hourIndex = (this.day.getSerialIndex() * 24L) + this.startHour;
		return (hourIndex * 60L) + (this.startMinute * 60L) + this.startSecond;
	}

	/**
	 * Returns a serial index number for the minute.
	 * 
	
	 * @return The serial index number. */
	public long getDaySerialIndex() {

		long hourIndex = this.startHour;
		return ((((hourIndex * 60L) + this.startMinute) * 60L) + this.startSecond) / 60;
	}

	/**
	 * Returns the first millisecond of the second.
	 * 
	 * @param calendar
	 *            the calendar which defines the timezone (<code>null</code> not
	 *            permitted).
	 * 
	
	 * 
	
	 * @return The first millisecond. * @throws NullPointerException
	 *             if <code>calendar</code> is <code>null</code>. */
	public long getFirstMillisecond(Calendar calendar) {
		int year = this.day.getYear();
		int month = this.day.getMonth() - 1;
		int day = this.day.getDayOfMonth();

		calendar.clear();
		calendar.set(year, month, day, this.startHour, this.startMinute,
				this.startSecond);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime().getTime();
	}

	/**
	 * Returns the last millisecond of the second.
	 * 
	 * @param calendar
	 *            the calendar / timezone (<code>null</code> not permitted).
	 * 
	
	 * 
	
	 * @return The last millisecond. * @throws NullPointerException
	 *             if <code>calendar</code> is <code>null</code>. */
	public long getLastMillisecond(Calendar calendar) {
		int year = this.day.getYear();
		int month = this.day.getMonth() - 1;
		int day = this.day.getDayOfMonth();

		calendar.clear();
		calendar.set(year, month, day, this.endHour, this.endMinute,
				this.endSecond);
		calendar.set(Calendar.MILLISECOND, 999);

		return calendar.getTime().getTime();
	}

	/**
	 * Tests the equality of this object against an arbitrary Object.
	 * <P>
	 * This method will return true ONLY if the object is a second object
	 * representing the same second as this instance.
	 * 
	 * @param obj
	 *            the object to compare (<code>null</code> permitted).
	 * 
	
	 * @return <code>true</code> if the minute and hour value of this and the
	 *         object are the same. */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof CandlePeriod)) {
			return false;
		}
		CandlePeriod that = (CandlePeriod) obj;
		if ((this.startSecond != that.startSecond)
				&& (this.endSecond != that.endSecond)) {
			return false;
		}
		if (this.startMinute != that.startMinute) {
			return false;
		}
		if (this.endMinute != that.endMinute) {
			return false;
		}
		if (this.startHour != that.startHour) {
			return false;
		}
		if (this.endHour != that.endHour) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a hash code for this object instance. The approach described by
	 * Joshua Bloch in "Effective Java" has been used here:
	 * <p>
	 * <code>http://developer.java.sun.com/developer/Books/effectivejava
	 * /Chapter3.pdf</code>
	 * 
	
	 * @return A hash code. */
	public int hashCode() {
		int result = 17;
		result = (37 * result) + this.startSecond;
		result = (37 * result) + this.endSecond;
		result = (37 * result) + this.startMinute;
		result = (37 * result) + this.endMinute;
		result = (37 * result) + this.startHour;
		result = (37 * result) + this.endHour;
		result = (37 * result) + this.day.hashCode();
		return result;
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
	
	 * @return negative == before, zero == same, positive == after. */
	public int compareTo(Object o1) {
		int result;

		// CASE 1 : Comparing to another Minute object
		// -------------------------------------------
		if (o1 instanceof CandlePeriod) {
			CandlePeriod m = (CandlePeriod) o1;
			result = getStartHour().compareTo(m.getStartHour());
			if (result == 0) {
				result = getEndHour().compareTo(m.getEndHour());
				if (result == 0) {
					result = getStartMinute().compareTo(m.getStartMinute());
					if (result == 0) {
						result = getEndMinute().compareTo(m.getEndMinute());
						if (result == 0) {
							result = this.startSecond - m.getStartSecond();
							if (result == 0) {
								result = this.endSecond - m.getEndSecond();
							}
						}
					}
				}
			}
		}

		// CASE 2 : Comparing to another TimePeriod object
		// -----------------------------------------------
		else if (o1 instanceof RegularTimePeriod) {
			// more difficult case - evaluate later...
			result = 0;
		}

		// CASE 3 : Comparing to a non-TimePeriod object
		// ---------------------------------------------
		else {
			// consider time periods to be ordered after general objects
			result = 1;
		}

		return result;
	}

	/**
	 * Creates a Minute instance by parsing a string. The string is assumed to
	 * be in the format "YYYY-MM-DD HH:MM:SS", perhaps with leading or trailing
	 * whitespace.
	 * 
	 * @param s
	 *            the minute string to parse.
	 * 
	
	 * @return <code>null</code>, if the string is not parseable, the minute
	 *         otherwise. */
	public static CandlePeriod parseMinute(String s) {
		CandlePeriod result = null;
		s = s.trim();

		String daystr = s.substring(0, Math.min(10, s.length()));
		Day day = Day.parseDay(daystr);
		if (day != null) {
			String hmstr = s.substring(
					Math.min(daystr.length() + 1, s.length()), s.length());
			hmstr = hmstr.trim();

			String hourstr = hmstr.substring(0, Math.min(2, hmstr.length()));
			int hour = Integer.parseInt(hourstr);

			if ((hour >= 0) && (hour <= 23)) {
				String minstr = hmstr.substring(
						Math.min(hourstr.length() + 1, hmstr.length()),
						hmstr.length());
				int minute = Integer.parseInt(minstr);
				/*
				 * TODO fix this
				 */
				if ((minute >= 0) && (minute <= 59)) {
					result = new CandlePeriod(0, 59, 0, 59, 9, 16, day);
				}
			}
		}
		return result;
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		DateFormat df = new SimpleDateFormat(DATETIMEFORMAT);
		return df.format(this.getStart());

	}
}
