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
package org.trade.core.validator;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.trade.core.exception.ExceptionContext;
import org.trade.core.exception.ExceptionMessage;
import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageContextFactory;
import org.trade.core.message.MessageFactory;

/**
 */
public class DateValidator implements Validator {
	public final static String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HHmmss'Z'";

	private IMessageFactory m_messageFactory;

	private boolean m_isMandatory;

	/**
	 * Constructor for DateValidator.
	 * @param messageFactory IMessageFactory
	 * @param isMandatory boolean
	 */
	public DateValidator(IMessageFactory messageFactory, boolean isMandatory) {
		m_messageFactory = messageFactory;
		m_isMandatory = isMandatory;
	}

	/**
	 * Method getMessageFactory.
	 * @return IMessageFactory
	 */
	protected IMessageFactory getMessageFactory() {
		if (null == m_messageFactory) {
			m_messageFactory = MessageFactory.SYSTEM_ERROR;
		}

		return m_messageFactory;
	}

	/**
	 * Method isValid.
	 * @param value Object
	 * @param invalidValue String
	 * @param expectedFormat String
	 * @param receiver ExceptionMessageListener
	 * @return boolean
	 * @see org.trade.core.validator.Validator#isValid(Object, String, String, ExceptionMessageListener)
	 */
	public boolean isValid(Object value, String invalidValue,
			String expectedFormat, ExceptionMessageListener receiver) {
		if (null == receiver) {
			receiver = new ExceptionMessageListener() {
				public void addExceptionMessage(ExceptionMessage e) {
				}
			};
		}

		boolean valid = true;

		if (null != invalidValue) {
			valid = false;

			if (expectedFormat == null) {
				expectedFormat = DATE_TIME_FORMAT;
			}
			receiver.addExceptionMessage(getMessageFactory().create(
					new ExceptionContext("edit_check",
							"Value is not in the following format: "
									+ expectedFormat)));
		} else if (null == value) {
			if (m_isMandatory) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.MANDATORY_VALUE_NOT_PROVIDED
								.create()));
			}
		} else {
			String errorMessage = validateDateValue((java.util.Date) value);

			if (errorMessage != null) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						new ExceptionContext("edit_check", errorMessage)));
			}
		}

		return valid;
	}

	/**
	 * Method validateDateValue.
	 * @param date java.util.Date
	 * @return String
	 */
	private String validateDateValue(java.util.Date date) {
		int day, month, year;

		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);

		day = cal.get(Calendar.DAY_OF_MONTH);
		month = cal.get(Calendar.MONTH) + 1;
		year = cal.get(Calendar.YEAR);

		/**
		 * None of this checks will ever fail. Leaving it here just to be
		 * absolutely sure.
		 */
		if ((day < 1) || (day > 31)) {
			return "day field of date is < 1 or > 31";
		}

		if ((month < 1) || (month > 12)) {
			return "month field of date is < 1 or > 12";
		}

		if ((day > 30)
				&& ((month == 9) || (month == 4) || (month == 6) || (month == 11))) {
			return "day field of date is invalid - 31st of a day with 30 days";
		}

		if (month == 2) {
			int days = 28;

			if (new GregorianCalendar().isLeapYear(year)) {
				days = 29;
			}

			if (day > days) {
				return "day field of date is invalid - no day " + day
						+ " in February of " + year;
			}
		}

		return null;
	}
}
