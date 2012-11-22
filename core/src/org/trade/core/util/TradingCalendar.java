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
package org.trade.core.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.properties.ConfigProperties;

/**
 * 
 * @version $Id: TradingCalendar.java,v 1.1 2001/10/18 01:35:48 simon Exp $
 * @author Simon Allen
 */
public class TradingCalendar {

	/*
	 * This will use the timezone that was set on the command line to start the
	 * add i.e. -Duser.timezone=EST
	 */

	private final static Logger _log = LoggerFactory
			.getLogger(TradingCalendar.class);

	private static final TimeZone TIMEZONE = TimeZone.getDefault();
	public static final Date NULLDATE = (new GregorianCalendar(0, 0, 0, 0, 0, 0))
			.getTime();
	public static final HashMap<Integer, int[]> HOLIDAYS = new HashMap<Integer, int[]>();
	private static GregorianCalendar CALENDAR_NY;
	private static SimpleDateFormat dateFormat;
	private static Integer openHour = new Integer(9);
	private static Integer openMinute = new Integer(30);
	private static Integer closeHour = new Integer(16);
	private static Integer closeMinute = new Integer(0);
	private static Integer closeDayOffset = new Integer(0);

	/*
	 * Initialize the calendar form the properties file. If values are not found
	 * defaults will be used.
	 */
	static {
		CALENDAR_NY = new GregorianCalendar(TIMEZONE, Locale.getDefault());
		CALENDAR_NY.setLenient(false);
		dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		dateFormat.setLenient(false);
		dateFormat.setTimeZone(TIMEZONE);
		try {
			String open = ConfigProperties.getPropAsString("trade.market.open");
			openHour = new Integer(open.substring(0, open.indexOf(":")));
			openMinute = new Integer(open.substring(open.indexOf(":") + 1,
					open.length()));
		} catch (IOException ex) {
			_log.warn("Property trade.market.open not set in config.properties will use default 9:30am EST");
		}
		try {
			String close = ConfigProperties
					.getPropAsString("trade.market.close");
			closeHour = new Integer(close.substring(0, close.indexOf(":")));
			closeMinute = new Integer(close.substring(close.indexOf(":") + 1,
					close.length()));
			/*
			 * If the close time if before or equal to the open time assume its
			 * the next day.
			 */
			if (closeHour <= openHour && closeMinute <= openMinute) {
				closeDayOffset++;
			}
		} catch (IOException ex) {
			_log.warn("Property trade.market.close not set in config.properties will use default 4:00pm EST");
		}
		try {
			String thisyear = ConfigProperties
					.getPropAsString("trade.holidays.thisyear");
			setHolidays(thisyear);
			String lastyear = ConfigProperties
					.getPropAsString("trade.holidays.lastyear");
			setHolidays(lastyear);
		} catch (IOException ex) {
			_log.warn("Property trade.holidays.lastyear/trade.holidays.thisyear not set in config.properties");
		}
	}

	/**
	 * Add Years to a date-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return boolean
	 * @exception * @see
	 */
	public static boolean inDaylightTime(Date date) {
		synchronized (CALENDAR_NY) {
			return CALENDAR_NY.getTimeZone().inDaylightTime(date);
		}
	}

	/**
	 * Add Years to a date-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @param noYears
	 *            int
	 * @return Date
	 * @exception * @see
	 */
	public static Date addYear(Date date, int noYears) {
		if ((date != null) && (noYears != 0)) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				CALENDAR_NY.add(Calendar.YEAR, noYears);
				return CALENDAR_NY.getTime();
			}
		} else {
			return date;
		}
	}

	/**
	 * Add months to a date-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @param noMonths
	 *            int
	 * @return Date
	 * @exception * @see
	 */
	public static Date addMonth(Date date, int noMonths) {
		if ((date != null) && (noMonths != 0)) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				CALENDAR_NY.add(Calendar.MONTH, noMonths);
				return CALENDAR_NY.getTime();
			}
		} else {
			return date;
		}
	}

	/**
	 * Add days to a date-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @param noDays
	 *            int
	 * @return Date
	 * @exception * @see
	 */
	public static Date addDays(Date date, int noDays) {
		if ((date != null) && (noDays != 0)) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				CALENDAR_NY.add(Calendar.DAY_OF_YEAR, noDays);

				return CALENDAR_NY.getTime();
			}
		} else {
			return date;
		}
	}

	/**
	 * Method addBusinessDays.
	 * 
	 * @param date
	 *            Date
	 * @param noDays
	 *            int
	 * @return Date
	 */
	public static Date addBusinessDays(Date date, int noDays) {
		if ((date != null) && (noDays != 0)) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				if (noDays > 0) {
					for (int i = 0; i < noDays; i++) {
						CALENDAR_NY.setTime(addDays(CALENDAR_NY.getTime(), 1));
						if ((CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
								|| (CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
								|| isHoliday(CALENDAR_NY.getTime())) {
							noDays++;
						}
					}
					return CALENDAR_NY.getTime();
				} else {
					for (int i = 0; i > noDays; i--) {
						CALENDAR_NY.setTime(addDays(CALENDAR_NY.getTime(), -1));
						if ((CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
								|| (CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
								|| isHoliday(CALENDAR_NY.getTime())) {
							noDays--;
						}
					}
					return CALENDAR_NY.getTime();
				}
			}
		} else {
			return date;
		}
	}

	/**
	 * Add days to a date-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @param noHours
	 *            int
	 * @return Date
	 * @exception * @see
	 */
	public static Date addHours(Date date, int noHours) {
		if ((date != null) && (noHours != 0)) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				CALENDAR_NY.add(Calendar.HOUR_OF_DAY, noHours);
				return CALENDAR_NY.getTime();
			}
		} else {
			return date;
		}
	}

	/**
	 * Add days to a date-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @param noMinutes
	 *            int
	 * @return Date
	 * @exception * @see
	 */
	public static Date addMinutes(Date date, int noMinutes) {
		if ((date != null) && (noMinutes != 0)) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				CALENDAR_NY.add(Calendar.MINUTE, noMinutes);
				return CALENDAR_NY.getTime();
			}
		} else {
			return date;
		}
	}

	/**
	 * Add days to a date-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @param noSeconds
	 *            int
	 * @return Date
	 * @exception * @see
	 */
	public static Date addSeconds(Date date, int noSeconds) {
		if ((date != null) && (noSeconds != 0)) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				CALENDAR_NY.add(Calendar.SECOND, noSeconds);
				return CALENDAR_NY.getTime();
			}
		} else {
			return date;
		}
	}

	/**
	 * Returns the difference in days-
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date1
	 *            Date
	 * @param date2
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int daysDiff(Date date1, Date date2) {
		if ((date1 != null) && (date2 != null)) {
			Double value = new Double((new BigDecimal(
					(date2.getTime() - date1.getTime())
							/ (60 * 60 * 24 * 1000d))).setScale(0,
					BigDecimal.ROUND_HALF_EVEN).doubleValue());
			return value.intValue();
		}
		return 0;
	}

	/**
	 * Return the Year for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getYear(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.YEAR);
			}
		}
		return 0;
	}

	/**
	 * Return the Second for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getSecond(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.SECOND);
			}
		}
		return 0;
	}

	/**
	 * Return the Minute for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getMinute(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.MINUTE);
			}
		}
		return 0;
	}

	/**
	 * Return the Hour of day (24 hour clock )for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getHourOfDay(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.HOUR_OF_DAY);
			}
		}
		return 0;
	}

	/**
	 * Return the AM or PM for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return String
	 * @exception * @see
	 */
	public static String getAMPM(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				if (CALENDAR_NY.get(Calendar.AM_PM) == 0) {
					return "AM";
				} else {
					return "PM";
				}
			}
		}
		return null;
	}

	/**
	 * Return the Hour for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getHour(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.HOUR);
			}
		}
		return 0;
	}

	/**
	 * Return the Month for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getMonth(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.MONTH);
			}
		}
		return 0;
	}

	/**
	 * Return the Day for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param millis
	 *            long
	 * @return Date
	 * @exception * @see
	 */
	public static Date getDate(long millis) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTimeInMillis(millis);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Return the Day for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getDayOfYear(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.DAY_OF_YEAR);
			}
		}
		return 0;
	}

	/**
	 * Return the Day for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getDayOfMonth(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.DAY_OF_MONTH);
			}
		}
		return 0;
	}

	/**
	 * Return the Day for this date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return int
	 * @exception * @see
	 */
	public static int getDayOfWeek(Date date) {
		if (date != null) {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(date);
				return CALENDAR_NY.get(Calendar.DAY_OF_WEEK);
			}
		}
		return 0;
	}

	/**
	 * Get the date formated to the standard format string
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return String
	 * @exception * @see
	 */
	public static String getFormattedDate(Date date) {
		return dateFormat.format(date);
	}

	/**
	 * Get the date formated to the standard format string
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @param format
	 *            String
	 * @return String
	 * @exception * @see
	 */
	public static String getFormattedDate(Date date, String format) {
		if (format != null) {
			SimpleDateFormat newDateFormat = new SimpleDateFormat(format);
			return newDateFormat.format(date);
		} else {
			return getFormattedDate(date);
		}
	}

	/**
	 * Set the date in the standard CALENDAR_NY to the dtring date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            String
	 * @param format
	 *            String
	 * @return Date
	 * @exception * @see
	 */
	public static Date getFormattedDate(String date, String format) {
		Date newDate = null;

		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			newDate = dateFormat.parse(date);
		} catch (ParseException e) {
			newDate = NULLDATE;
		}

		return newDate;
	}

	/**
	 * Set the date in the standard calendar to the dtring date
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            String
	 * @return Date
	 * @exception * @see
	 */
	public static Date getFormattedDate(String date) {
		Date newDate = null;

		try {
			synchronized (CALENDAR_NY) {
				CALENDAR_NY.setTime(dateFormat.parse(date));
				newDate = CALENDAR_NY.getTime();
			}
		} catch (ParseException e) {
			newDate = NULLDATE;
		}
		return newDate;
	}

	/**
	 * Get the date equal to zero
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return Date
	 * @exception * @see
	 */
	public static Date getNullJavaDate() {
		return NULLDATE;
	}

	/**
	 * Is the date equal to zero
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param date
	 *            Date
	 * @return boolean
	 * @exception * @see
	 */
	public static boolean isNullDate(Date date) {
		return date.equals(NULLDATE);
	}

	/**
	 * Method setHolidays.
	 * 
	 * @param yearAndDays
	 *            String
	 */
	private static void setHolidays(String yearAndDays) {
		StringTokenizer st = new StringTokenizer(yearAndDays, ",");
		if (st.countTokens() > 0) {

			int[] dates = new int[(st.countTokens() - 1)];
			Integer year = null;
			int i = 0;
			while (st.hasMoreTokens()) {
				if (i == 0) {
					year = new Integer(st.nextToken());
				} else {
					dates[(i - 1)] = Integer.parseInt(st.nextToken());
				}
				i++;
			}
			HOLIDAYS.put(year, dates);
		}
	}

	/**
	 * Method getTodayBusinessDayStart.
	 * 
	 * @return Date
	 */
	public static Date getTodayBusinessDayStart() {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(new Date());
			CALENDAR_NY.set(Calendar.HOUR_OF_DAY, openHour);
			CALENDAR_NY.set(Calendar.MINUTE, openMinute);
			CALENDAR_NY.set(Calendar.SECOND, 0);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method getTodayBusinessDayEnd.
	 * 
	 * @return Date
	 */
	public static Date getTodayBusinessDayEnd() {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(new Date());
			CALENDAR_NY.set(Calendar.HOUR_OF_DAY, closeHour);
			CALENDAR_NY.set(Calendar.MINUTE, closeMinute);
			CALENDAR_NY.set(Calendar.SECOND, 0);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method getBusinessDayStart.
	 * 
	 * @param date
	 *            Date
	 * @return Date
	 */
	public static Date getBusinessDayStart(Date date) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(date);
			CALENDAR_NY.set(Calendar.HOUR_OF_DAY, openHour);
			CALENDAR_NY.set(Calendar.MINUTE, openMinute);
			CALENDAR_NY.set(Calendar.SECOND, 0);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method getBusinessDayEnd.
	 * 
	 * @param date
	 *            Date
	 * @return Date
	 */
	public static Date getBusinessDayEnd(Date date) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(TradingCalendar.addBusinessDays(date,
					closeDayOffset));
			CALENDAR_NY.set(Calendar.HOUR_OF_DAY, closeHour);
			CALENDAR_NY.set(Calendar.MINUTE, closeMinute);
			CALENDAR_NY.set(Calendar.SECOND, 0);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method getSpecificTime.
	 * 
	 * @param date
	 *            Date
	 * @param hrs
	 *            int
	 * @param minutes
	 *            int
	 * @return Date
	 */
	public static Date getSpecificTime(Date date, int hrs, int minutes) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(date);
			CALENDAR_NY.set(Calendar.HOUR_OF_DAY, hrs);
			CALENDAR_NY.set(Calendar.MINUTE, minutes);
			CALENDAR_NY.set(Calendar.SECOND, 0);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method getSpecificTime.
	 * 
	 * @param date
	 *            Date
	 * @param hrs
	 *            int
	 * @param minutes
	 *            int
	 * @param seconds
	 *            int
	 * @return Date
	 */
	public static Date getSpecificTime(Date date, int hrs, int minutes,
			int seconds) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(date);
			CALENDAR_NY.set(Calendar.HOUR_OF_DAY, hrs);
			CALENDAR_NY.set(Calendar.MINUTE, minutes);
			CALENDAR_NY.set(Calendar.SECOND, seconds);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method getYearStart.
	 * 
	 * @return Date
	 */
	public static Date getYearStart() {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(new Date());
			CALENDAR_NY.set(Calendar.YEAR, CALENDAR_NY.get(Calendar.YEAR));
			CALENDAR_NY.set(Calendar.MONTH, 0);
			CALENDAR_NY.set(Calendar.DAY_OF_MONTH, 1);
			CALENDAR_NY.set(Calendar.HOUR_OF_DAY, openHour);
			CALENDAR_NY.set(Calendar.MINUTE, openMinute);
			CALENDAR_NY.set(Calendar.SECOND, 0);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method isTradingDay.
	 * 
	 * @param date
	 *            Date
	 * @return boolean
	 */
	public static boolean isTradingDay(Date date) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(date);
			if ((CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
					|| (CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
					|| isHoliday(date)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Method isMarketHours.
	 * 
	 * @param date
	 *            Date
	 * @return boolean
	 */
	public static boolean isMarketHours(Date date) {
		if (!isAfterHours(date) && !isPreMarket(date)) {
			return true;
		}
		return false;
	}

	/**
	 * Method isPreMarket.
	 * 
	 * @return boolean
	 */
	public static boolean isPreMarket() {
		return getTodayBusinessDayStart().after(new Date());
	}

	/**
	 * Method isPreMarket.
	 * 
	 * @param date
	 *            Date
	 * @return boolean
	 */
	public static boolean isPreMarket(Date date) {
		if (getBusinessDayStart(date).after(date)) {
			return true;
		}
		return false;
	}

	/**
	 * Method isAfterHours.
	 * 
	 * @return boolean
	 */
	public static boolean isAfterHours() {
		if (getTodayBusinessDayEnd().before(new Date())
				|| (getTodayBusinessDayEnd().compareTo(new Date()) == 0)) {
			return true;
		}
		return false;
	}

	/**
	 * Method isAfterHours.
	 * 
	 * @param date
	 *            Date
	 * @return boolean
	 */
	public static boolean isAfterHours(Date date) {
		if (getBusinessDayEnd(date).before(date)
				|| (getBusinessDayEnd(date).compareTo(date) == 0)) {
			return true;
		}
		return false;
	}

	/**
	 * Method sameDay.
	 * 
	 * @param date1
	 *            Date
	 * @param date2
	 *            Date
	 * @return boolean
	 */
	public static boolean sameDay(Date date1, Date date2) {
		GregorianCalendar cal1 = new GregorianCalendar();
		cal1.setTime(date2);
		GregorianCalendar cal2 = new GregorianCalendar();
		cal2.setTime(date1);
		return sameDay(cal1, cal2);
	}

	/**
	 * Method sameDay.
	 * 
	 * @param date1
	 *            Calendar
	 * @param date2
	 *            Calendar
	 * @return boolean
	 */
	public static boolean sameDay(Calendar date1, Calendar date2) {
		return (date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH))
				&& (date1.get(Calendar.DAY_OF_MONTH) == date2
						.get(Calendar.DAY_OF_MONTH))
				&& (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR));
	}

	/**
	 * Method getMostRecentTradingDay.
	 * 
	 * @param input
	 *            Date
	 * @return Date
	 */
	public static Date getMostRecentTradingDay(Date input) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(TradingCalendar.getBusinessDayStart(input));
			while ((CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
					|| (CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
					|| isHoliday(CALENDAR_NY.getTime())) {
				CALENDAR_NY.setTime(addDays(CALENDAR_NY.getTime(), -1));
			}
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method getPrevTradingDay.
	 * 
	 * @param input
	 *            Date
	 * @return Date
	 */
	public static Date getPrevTradingDay(Date input) {
		synchronized (CALENDAR_NY) {
			Date preTradingday = addDays(input, -1);
			return getMostRecentTradingDay(preTradingday);
		}
	}

	/**
	 * Method getNextTradingDay.
	 * 
	 * @param input
	 *            Date
	 * @return Date
	 */
	public static Date getNextTradingDay(Date input) {
		synchronized (CALENDAR_NY) {
			Date nextTradingday = addDays(input, 1);
			CALENDAR_NY.setTime(TradingCalendar
					.getBusinessDayStart(nextTradingday));
			while ((CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
					|| (CALENDAR_NY.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
					|| isHoliday(CALENDAR_NY.getTime())) {
				CALENDAR_NY.setTime(addDays(CALENDAR_NY.getTime(), 1));
			}
			return CALENDAR_NY.getTime();
		}
	}

	/**
	 * Method isHoliday.
	 * 
	 * @param date
	 *            Date
	 * @return boolean
	 */
	public static boolean isHoliday(Date date) {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.setTime(date);
			int[] hols = HOLIDAYS.get(CALENDAR_NY.get(Calendar.YEAR));
			if (null != hols) {
				for (int hol : hols) {
					if (hol == CALENDAR_NY.get(Calendar.DAY_OF_YEAR)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	/**
	 * Method firstMondayAfter2010.
	 * 
	 * @return long
	 */
	public static long firstMondayAfter2010() {
		synchronized (CALENDAR_NY) {
			CALENDAR_NY.set(2010, 0, 1, 0, 0, 0);
			CALENDAR_NY.set(Calendar.MILLISECOND, 0);
			while (CALENDAR_NY.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
				CALENDAR_NY.add(Calendar.DATE, 1);
			}
			// return cal.getTimeInMillis();
			// preceding code won't work with JDK 1.3
			return CALENDAR_NY.getTime().getTime();
		}
	}
}
