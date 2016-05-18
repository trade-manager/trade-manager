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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.Comparator;

import org.trade.core.conversion.JavaTypeTranslator;
import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageFactory;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.validator.DateValidator;
import org.trade.core.validator.ExceptionMessageListener;
import org.trade.core.validator.Validator;

/**
 */
public class Date extends ValueType implements Comparator<Date>, Comparable<Date> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5122615819171831028L;

	public final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HHmmss'Z'";

	public final static String DATE_FORMAT = "yyyyMMdd";

	public final static int LEN_STRING_IN_DATE_TIME_FORMAT = 18;

	public final static int LEN_STRING_IN_DATE_FORMAT = 8;

	public static final Date NULLIPDATE = new Date(
			ZonedDateTime.of(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC), ZoneOffset.UTC.normalized()));

	static {
		// Register the appropriate converters
		JavaTypeTranslator.registerDynamicTypeConverter(new ObjectToDate());
		JavaTypeTranslator.registerDynamicTypeConverter(new DateToObject());
	}

	private ZonedDateTime m_date = null;

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
	 * 
	 * @param date
	 *            ZonedDateTime
	 */
	public Date(ZonedDateTime date) {
		m_date = date;
	}

	/**
	 * Constructor
	 * 
	 * 
	 * @param date
	 *            java.uti.Date
	 */
	public Date(java.util.Date date) {
		m_date = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	/**
	 * Parse the stringified date using the DATE_TIME_FORMAT
	 * 
	 * 
	 * @param date
	 *            String
	 */
	public Date(String date) {
		this(date, null);
	}

	/**
	 * Parse the string field date using the incoming date form
	 * 
	 * 
	 * @param date
	 *            String
	 * @param dateFormat
	 *            String
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
			m_date = TradingCalendar.getZonedDateTimeFromDateTimeString(date.trim(), m_format);
			m_invalidDate = null;
		}
	}

	/**
	 * 
	 * @return The Date this Date is representing
	 */
	public ZonedDateTime getZonedDateTime() {
		return (m_date);
	}

	/**
	 * 
	 * @return The Date this Date is representing
	 */
	public java.util.Date getDate() {
		Instant instant = null;
		if (null != getZonedDateTime()) {
			instant = getZonedDateTime().toInstant();
			return (java.util.Date) java.util.Date.from(instant);
		}
		return null;
	}

	/**
	 * Method equals.
	 * 
	 * @param objectToCompare
	 *            Object
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
			java.time.ZonedDateTime cmpTo = null;

			if (objectToCompare instanceof org.trade.core.valuetype.Date) {
				cmpTo = ((org.trade.core.valuetype.Date) objectToCompare).m_date;
			} else if (objectToCompare instanceof java.util.Date) {
				cmpTo = (java.time.ZonedDateTime) objectToCompare;
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

	/**
	 * Method compareTo.
	 * 
	 * @param other
	 *            Date
	 * @return int
	 */
	public int compareTo(final Date other) {
		return CoreUtils.nullSafeComparator(this.getZonedDateTime(), other.getZonedDateTime());
	}

	/**
	 * Method compare.
	 * 
	 * @param o1
	 *            Date
	 * @param o2
	 *            Date
	 * @return int
	 */
	public int compare(Date o1, Date o2) {
		return CoreUtils.nullSafeComparator(o1.getZonedDateTime(), o2.getZonedDateTime());
	}

	/**
	 * 
	 * @return String
	 */
	public String toString() {
		if (null != this.getZonedDateTime())
			return TradingCalendar.getFormattedDate(this.getZonedDateTime(), DATE_TIME_FORMAT);
		return null;
	}

	/**
	 * Method rightLengthForDateTime.
	 * 
	 * @param dateAndTime
	 *            String
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
	 * 
	 * @return boolean
	 */
	public boolean isEmpty() {
		if (null == m_date) {
			return true;
		}
		return false;
	}

	/**
	 * Compares dates ignoring time.
	 * 
	 * @param otherDate
	 *            org.trade.core.valuetype.Date
	 * @return the value 0 if the argument is a Date equal to this Date; a value
	 *         less than 0 if the argument is a Date after this Date; and a
	 *         value greater than 0 if the argument is a Date before this Date.
	 */
	public int compareDates(org.trade.core.valuetype.Date otherDate) {
		return compareDates(otherDate.getZonedDateTime());
	}

	/**
	 * Compares dates ignoring time.
	 * 
	 * @param otherDate
	 *            java.util.Date
	 * @return the value 0 if the argument is a Date equal to this Date; a value
	 *         less than 0 if the argument is a Date after this Date; and a
	 *         value greater than 0 if the argument is a Date before this Date.
	 */
	public int compareDates(ZonedDateTime otherDate) {
		return this.getZonedDateTime().compareTo(otherDate);
	}

	/**
	 * 
	 * @param value
	 *            Object
	 * @throws ValueTypeException
	 * @see com.cbsinc.esc.devtools.valuetype.ValueType
	 */
	public void setValue(Object value) throws ValueTypeException {
		if (value instanceof org.trade.core.valuetype.Date) {
			setDate(((org.trade.core.valuetype.Date) value).m_date);
		} else {
			try {
				setValue(JavaTypeTranslator.convert(org.trade.core.valuetype.Date.class, value));
			} catch (Exception ex) {
				throw new ValueTypeException(ex);
			}
		}
	}

	/**
	 * Method isValid.
	 * 
	 * @return boolean
	 */
	public boolean isValid() {
		return isValid(getDefaultOptionalValidator(MessageFactory.SYSTEM_ERROR), null);
	}

	/**
	 * Method isValid.
	 * 
	 * @param validator
	 *            Validator
	 * @param receiver
	 *            ExceptionMessageListener
	 * @return boolean
	 */
	public boolean isValid(Validator validator, ExceptionMessageListener receiver) {
		return validator.isValid(m_date, m_invalidDate, m_format, receiver);
	}

	/**
	 * Method getDefaultOptionalValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @return Validator
	 */
	public Validator getDefaultOptionalValidator(IMessageFactory messageFactory) {
		return getDefaultValidator(messageFactory, true);
	}

	/**
	 * Method getDefaultMandatoryValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @return Validator
	 */
	public Validator getDefaultMandatoryValidator(IMessageFactory messageFactory) {
		return getDefaultValidator(messageFactory, true);
	}

	/**
	 * Method getDefaultValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @param isMandatory
	 *            boolean
	 * @return Validator
	 */
	public Validator getDefaultValidator(IMessageFactory messageFactory, boolean isMandatory) {
		return new DateValidator(messageFactory, isMandatory);
	}

	/**
	 * Method getError. as long as the date was created by the conversion util,
	 * it will be valid or the error will already be created
	 * 
	 * @return String
	 */
	public String getError() {
		if (m_date == null) {
			return "Date not set";
		}
		return null;
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws java.lang.CloneNotSupportedException
	 */
	public Object clone() throws java.lang.CloneNotSupportedException {
		return (super.clone());
	}

	/**
	 * Method setDate.
	 * 
	 * @param date
	 *            java.time.ZonedDateTime
	 */
	private void setDate(java.time.ZonedDateTime date) {
		m_date = date;
	}
}
