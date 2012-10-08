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
package org.trade.core.valuetype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.trade.core.conversion.JavaTypeTranslator;
import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageFactory;
import org.trade.core.util.CoreUtils;
import org.trade.core.validator.DateValidator;
import org.trade.core.validator.ExceptionMessageListener;
import org.trade.core.validator.Validator;

/**
 */
public class Date extends ValueType implements Comparator<Date>,
		Comparable<Date> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5122615819171831028L;

	public final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HHmmss'Z'";

	public final static String DATE_FORMAT = "yyyyMMdd";

	public final static int LEN_STRING_IN_DATE_TIME_FORMAT = 18;

	public final static int LEN_STRING_IN_DATE_FORMAT = 8;

	public static final Date NULLIPDATE = new Date((new GregorianCalendar(0, 0,
			0, 0, 0, 0)).getTime());

	static {
		// Register the appropriate converters
		JavaTypeTranslator.registerDynamicTypeConverter(new ObjectToDate());
		JavaTypeTranslator.registerDynamicTypeConverter(new DateToObject());
	}

	private java.util.Date m_date = null;

	private String m_invalidDate = null;

	private String m_format = null;

	protected static Boolean m_ascending = new Boolean(true);

	/**
	 * Default Constructor
	 */
	public Date() {
		m_date = null;
	}

	/**
	 * Constructor
	 * 
	
	 * @param date java.util.Date
	 */
	public Date(java.util.Date date) {
		m_date = date;
	}

	/**
	 * Parse the stringified date using the DATE_TIME_FORMAT
	 * 
	
	 * @param date String
	 */
	public Date(String date) {
		this(date, null);
	}

	/**
	 * Parse the stringified date using the incoming date form
	 * 
	
	 * @param date String
	 * @param dateFormat String
	 */
	public Date(String date, String dateFormat) {
		if ((date == null) || (date.length() == 0)) {
			return;
		}

		if (dateFormat == null) {
			m_format = DATE_TIME_FORMAT;

			if (date.length() == LEN_STRING_IN_DATE_FORMAT) {
				m_format = DATE_FORMAT;
			}
		} else {
			m_format = dateFormat;
		}

		if ((m_format.equals(DATE_TIME_FORMAT) && !rightLengthForDateTime(date))
				|| (m_format.equals(DATE_FORMAT) && (date.length() != LEN_STRING_IN_DATE_FORMAT))) {
			m_invalidDate = date;
		} else {
			SimpleDateFormat formatter = new SimpleDateFormat(m_format);

			formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
			formatter.setLenient(false);

			try {
				m_date = formatter.parse(date.trim());
				m_invalidDate = null;
			} catch (ParseException e) {
				m_invalidDate = date;
			}
		}
	}

	/**
	
	 * @return The Date this Date is representing */
	public java.util.Date getDate() {
		return (m_date);
	}

	/**
	 * Method getCurrentDate.
	 * @return org.trade.core.valuetype.Date
	 */
	public static org.trade.core.valuetype.Date getCurrentDate() {
		return new org.trade.core.valuetype.Date(new java.util.Date());
	}

	/**
	 * Method equals.
	 * @param objectToCompare Object
	 * @return boolean
	 * @see java.util.Comparator#equals(Object)
	 */
	public boolean equals(Object objectToCompare) {
		
		if (this == objectToCompare) {
			return true;
		}
		if (objectToCompare == null) {
			return false;
		}

		boolean rVal = false;

		// Do not compare on nulls
		if ((objectToCompare != null) && (m_date != null)) {
			java.util.Date cmpTo = null;

			if (objectToCompare instanceof org.trade.core.valuetype.Date) {
				cmpTo = ((org.trade.core.valuetype.Date) objectToCompare).m_date;
			} else if (objectToCompare instanceof java.util.Date) {
				cmpTo = (java.util.Date) objectToCompare;
			}

			// Do not compare on nulls
			if (cmpTo != null) {
				if (m_date.equals(cmpTo)) {
					rVal = true;
				}
			}
		}

		return (rVal);
	}

	// public int compareTo(Date o1) {
	// return this.compare(this, o1);
	// }
	/**
	 * Method compareTo.
	 * @param other Date
	 * @return int
	 */
	public int compareTo(final Date other) {
		int result = CoreUtils.nullSafeDateComparator(this.getDate(),
				other.getDate());
		if (result != 0) {
			return result;
		}

		return CoreUtils
				.nullSafeDateComparator(this.getDate(), other.getDate());
	}

	/**
	 * Method compare.
	 * @param o1 Date
	 * @param o2 Date
	 * @return int
	 */
	public int compare(Date o1, Date o2) {
		int returnVal = CoreUtils.nullSafeDateComparator(o1.getDate(),
				o2.getDate());
		if (m_ascending.equals(Boolean.FALSE)) {
			returnVal = returnVal * -1;
		}
		return returnVal;
	}

	/**
	 * See description of superclass method. Overrode functionality to return
	 * the Date this object is using internally.
	 * @return Object
	 */
	public Object getSQLObject() {
		return (getDate());
	}

	/**
	 * See description of superclass method. Overrode functionality to return
	 * the Date this object is using internally.
	 * @return Class<?>
	 */
	public Class<?> getSQLObjectType() {
		return java.util.Date.class;
	}

	/**
	
	 * @return String
	 * @see com.cbsinc.esc.devtools.valuetype.ValueType */
	public String toString() {
		// the default is to return in date in the DATE_TIME_FORMAT and GMT
		// TimeZone
		return toString(DATE_TIME_FORMAT, TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Formats the Date to String in the given format for the specified TimeZone
	 * @param formatString String
	 * @param timeZone TimeZone
	 * @return String
	 */
	public String toString(String formatString, TimeZone timeZone) {
		if (m_invalidDate != null) {
			return m_invalidDate;
		}

		if (getDate() == null) {
			return "";
		}

		String rVal = "";

		try {
			SimpleDateFormat formatter = new SimpleDateFormat(formatString);

			formatter.setTimeZone(timeZone);

			rVal = formatter.format(getDate());
		} catch (Exception ex) {
			// Should not happen
			rVal = getDate().toString();
		}

		return (rVal);
	}

	// this method is called before the conversion if the date has to include
	// the time
	/**
	 * Method rightLengthForDateTime.
	 * @param dateAndTime String
	 * @return boolean
	 */
	public static boolean rightLengthForDateTime(String dateAndTime) {
		int length = dateAndTime.length();
		// do not count quotes
		int maxLength = DATE_TIME_FORMAT.length() - 4; // "yyyy-MM-dd'T'HHmmss'Z'";
		// month and day can be one digit : 2000-1-1T235959Z
		int minLength = maxLength - 2; // "yyyy-M-d'T'HHmmss'Z'";

		if ((length <= maxLength) && (length >= minLength)) {
			return true;
		}

		return false;
	}

	/**
	 * Method isEmpty.
	 * @return boolean
	 */
	public boolean isEmpty() {
		if (null == m_date) {
			return true;
		}

		return false;
	}

	// Calculates no. of days in between Jan 1st of smallDate to Jan 1st of
	// biggerDate
	/**
	 * Method getDaysBetweenYears.
	 * @param smallDate Calendar
	 * @param biggerDate Calendar
	 * @return int
	 */
	private int getDaysBetweenYears(Calendar smallDate, Calendar biggerDate) {
		int days = 0;
		Calendar indexDate = new GregorianCalendar();

		indexDate.set(Calendar.MONTH, Calendar.JANUARY);
		indexDate.set(Calendar.DATE, 1);
		indexDate.set(Calendar.YEAR, smallDate.get(Calendar.YEAR));
		System.out.println(" Samller year " + indexDate.get(Calendar.YEAR));
		System.out.println(" Bigger year " + biggerDate.get(Calendar.YEAR));

		while (indexDate.get(Calendar.YEAR) < biggerDate.get(Calendar.YEAR)) {
			days += indexDate.getActualMaximum(Calendar.DAY_OF_YEAR);

			indexDate.add(Calendar.YEAR, 1);
		}

		return days;
	}

	// Calculates no. of days in between Jan 1st of a year to a given date on
	// the same year
	/**
	 * Method getDaysFromYearStart.
	 * @param date Calendar
	 * @return int
	 */
	private int getDaysFromYearStart(Calendar date) {
		int days = date.get(Calendar.DAY_OF_MONTH);
		Calendar indexDate = new GregorianCalendar();

		indexDate.set(Calendar.MONTH, Calendar.JANUARY);
		indexDate.set(Calendar.DATE, 1);
		indexDate.set(Calendar.YEAR, date.get(Calendar.YEAR));

		while (indexDate.get(Calendar.MONTH) < date.get(Calendar.MONTH)) {
			days += indexDate.getActualMaximum(Calendar.DAY_OF_MONTH);

			indexDate.add(Calendar.MONTH, 1);
		}

		return days;
	}

	/*
	 * This function returns no. of days in between two given dates
	 */

	/**
	 * Method getDaysInBetween.
	 * @param otherDate Date
	 * @return int
	 */
	public int getDaysInBetween(Date otherDate) {
		Calendar smallDate = new GregorianCalendar();
		Calendar bigDate = new GregorianCalendar();
		int result = compareDates(otherDate);

		if (result > 0) {
			smallDate.setTime(otherDate.getDate());
			bigDate.setTime(getDate());
		} else if (result < 0) {
			smallDate.setTime(getDate());
			bigDate.setTime(otherDate.getDate());
		} else {
			return 0;
		}

		/*
		 * Calculation logic No.of days from Jan 1 st of bigger date's year to
		 * bigger date + No. of days in between Jan 1 st of smaller date year to
		 * bigger date - No.of days from Jan 1 st of smaller date's year to
		 * smaller date
		 */

		int days = (getDaysFromYearStart(bigDate) + getDaysBetweenYears(
				smallDate, bigDate)) - getDaysFromYearStart(smallDate);

		return days;
	}

	/**
	 * Compares dates ingnoring time.
	 * 
	
	
	 * @param otherDate org.trade.core.valuetype.Date
	 * @return the value 0 if the argument is a Date equal to this Date; a value
	 *         less than 0 if the argument is a Date after this Date; and a
	 *         value greater than 0 if the argument is a Date before this Date. */
	public int compareDates(org.trade.core.valuetype.Date otherDate) {
		return compareDates(otherDate.getDate());
	}

	/**
	 * Compares dates ingnoring time.
	 * 
	
	
	 * @param otherDate java.util.Date
	 * @return the value 0 if the argument is a Date equal to this Date; a value
	 *         less than 0 if the argument is a Date after this Date; and a
	 *         value greater than 0 if the argument is a Date before this Date. */
	public int compareDates(java.util.Date otherDate) {
		Calendar compDate = new GregorianCalendar();
		Calendar thisDate = new GregorianCalendar();

		compDate.setTime(otherDate);
		thisDate.setTime(getDate());

		int result = 0;

		result = thisDate.get(Calendar.YEAR) - compDate.get(Calendar.YEAR);

		if (result != 0) {
			return (result);
		}

		result = thisDate.get(Calendar.MONTH) - compDate.get(Calendar.MONTH);

		if (result != 0) {
			return (result);
		}

		result = thisDate.get(Calendar.DAY_OF_MONTH)
				- compDate.get(Calendar.DAY_OF_MONTH);

		if (result != 0) {
			return (result);
		}

		return (0);
	}

	/**
	
	 * @param value Object
	 * @throws ValueTypeException
	 * @see com.cbsinc.esc.devtools.valuetype.ValueType */
	public void setValue(Object value) throws ValueTypeException {
		if (value instanceof org.trade.core.valuetype.Date) {
			setDate(((org.trade.core.valuetype.Date) value).m_date);
		} else {
			try {
				setValue(JavaTypeTranslator.convert(
						org.trade.core.valuetype.Date.class, value));
			} catch (Exception ex) {
				throw new ValueTypeException(ex);
			}
		}
	}

	/**
	 * Method isValid.
	 * @return boolean
	 */
	public boolean isValid() {
		return isValid(
				getDefaultOptionalValidator(MessageFactory.SYSTEM_ERROR), null);
	}

	/**
	 * Method isValid.
	 * @param validator Validator
	 * @param receiver ExceptionMessageListener
	 * @return boolean
	 */
	public boolean isValid(Validator validator,
			ExceptionMessageListener receiver) {
		return validator.isValid(m_date, m_invalidDate, m_format, receiver);
	}

	/**
	 * Method getDefaultOptionalValidator.
	 * @param messageFactory IMessageFactory
	 * @return Validator
	 */
	public Validator getDefaultOptionalValidator(IMessageFactory messageFactory) {
		return getDefaultValidator(messageFactory, true);
	}

	/**
	 * Method getDefaultMandatoryValidator.
	 * @param messageFactory IMessageFactory
	 * @return Validator
	 */
	public Validator getDefaultMandatoryValidator(IMessageFactory messageFactory) {
		return getDefaultValidator(messageFactory, true);
	}

	/**
	 * Method getDefaultValidator.
	 * @param messageFactory IMessageFactory
	 * @param isMandatory boolean
	 * @return Validator
	 */
	public Validator getDefaultValidator(IMessageFactory messageFactory,
			boolean isMandatory) {
		return new DateValidator(messageFactory, isMandatory);
	}

	// as long as the date was created by the conversion util, it will be valid
	// or the error will
	// already be created
	/**
	 * Method getError.
	 * @return String
	 */
	public String getError() {
		if (m_date == null) {
			return null;
		} else {
			return null;
			// return ValidationUtil.checkDate(m_date.getDay(),
			// m_date.getMonth()+1, m_date.getYear()+1900);
		}
	}

	/**
	 * Method clone.
	 * @return Object
	 * @throws java.lang.CloneNotSupportedException
	 */
	public Object clone() throws java.lang.CloneNotSupportedException {
		return (super.clone());
	}

	/**
	 * Method after.
	 * @param date org.trade.core.valuetype.Date
	 * @return boolean
	 */
	public boolean after(org.trade.core.valuetype.Date date) {
		return (this.getDate().after(date.getDate()));
	}

	/**
	 * Method before.
	 * @param date org.trade.core.valuetype.Date
	 * @return boolean
	 */
	public boolean before(org.trade.core.valuetype.Date date) {
		return (this.getDate().before(date.getDate()));
	}

	//
	// Private Methods
	//
	/**
	 * Method setDate.
	 * @param date java.util.Date
	 */
	private void setDate(java.util.Date date) {
		m_date = date;
	}
}
