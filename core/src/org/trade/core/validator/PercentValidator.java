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

import java.math.BigDecimal;

import org.trade.core.exception.ExceptionMessage;
import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageContextFactory;
import org.trade.core.message.MessageFactory;

/**
 */
public class PercentValidator implements Validator {
	private IMessageFactory m_messageFactory;

	private boolean m_isMandatory;

	private boolean m_allowNegative;

	private boolean m_allowZero;

	private int m_maxNonDecimalLength;

	private int m_maxDecimalLength;

	/**
	 * Constructor for PercentValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @param allowNegative
	 *            boolean
	 * @param allowZero
	 *            boolean
	 * @param maxNonDecimalLength
	 *            int
	 * @param maxDecimalLength
	 *            int
	 * @param isMandatory
	 *            boolean
	 */
	public PercentValidator(IMessageFactory messageFactory,
			boolean allowNegative, boolean allowZero, int maxNonDecimalLength,
			int maxDecimalLength, boolean isMandatory) {
		m_messageFactory = messageFactory;
		m_allowNegative = allowNegative;
		m_allowZero = allowZero;
		m_maxNonDecimalLength = maxNonDecimalLength;
		m_maxDecimalLength = maxDecimalLength;
		m_isMandatory = isMandatory;
	}

	/**
	 * Method getMessageFactory.
	 * 
	 * @return IMessageFactory
	 */
	protected IMessageFactory getMessageFactory() {
		if (null == m_messageFactory) {
			m_messageFactory = MessageFactory.SYSTEM_ERROR;
		}

		return m_messageFactory;
	}

	// from IPercentValidator
	/**
	 * Method isValid.
	 * 
	 * @param value
	 *            Object
	 * @param invalidValue
	 *            String
	 * @param expectedFormat
	 *            String
	 * @param receiver
	 *            ExceptionMessageListener
	 * @return boolean
	 * @see org.trade.core.validator.Validator#isValid(Object, String, String,
	 *      ExceptionMessageListener)
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

		if ((null == value) && (null == invalidValue)) {
			// Enforce optional/mandatory
			if (m_isMandatory) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.MANDATORY_VALUE_NOT_PROVIDED
								.create()));
			}
		} else if (null == invalidValue) // Able to parse input into a
		// BigDecimal
		{
			String stringValue = value.toString();

			int indexOfDot = stringValue.indexOf(".");
			String decimalString = "";
			String nonDecimalString = stringValue;
			if (-1 != indexOfDot) {
				decimalString = stringValue.substring(indexOfDot + 1,
						stringValue.length());
				nonDecimalString = stringValue.substring(0, indexOfDot);
			}

			if (nonDecimalString.length() > 0
					&& nonDecimalString.charAt(0) == '-') {
				nonDecimalString = nonDecimalString.substring(1,
						nonDecimalString.length());
			}

			long nonDecimalLength = nonDecimalString.length();

			// Note that the decimal length will be 1 for 00-09.
			long decimalLength = decimalString.length();

			// Enforce length of portion to right of decimal point
			if (decimalLength > m_maxDecimalLength) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.PERCENT_RIGHT_OF_DECIMAL_TOO_LONG
								.create(MessageContextFactory.MAX_LENGTH
										.create("" + m_maxDecimalLength))));
			}

			// Enforce length of portion to left of decimal point
			if (nonDecimalLength > m_maxNonDecimalLength) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.PERCENT_LEFT_OF_DECIMAL_TOO_LONG
								.create(MessageContextFactory.MAX_LENGTH
										.create("" + m_maxNonDecimalLength))));
			}

			// Disallow zero for certain formats
			if (!m_allowZero
					&& (0 == ((BigDecimal) value).compareTo(new BigDecimal(0)))) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory()
						.create(MessageContextFactory.PERCENT_ZERO_NOT_ALLOWED
								.create()));
			}

			// Disallow negative numbers
			if (!m_allowNegative
					&& (((BigDecimal) value).compareTo(new BigDecimal(0)) < 0)) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.PERCENT_NEGATIVE_NOT_ALLOWED
								.create()));
			}
		} else
		// Percent was not able to parse invalidValue into a BigDecimal
		{
			Validator validator;

			if (m_allowNegative) {
				validator = new StringValidator(getMessageFactory(), 1,
						m_maxNonDecimalLength + m_maxDecimalLength + 2,
						StringValidator.DIGITS, "-.", m_isMandatory);
			} else {
				validator = new StringValidator(getMessageFactory(), 1,
						m_maxNonDecimalLength + m_maxDecimalLength + 1,
						StringValidator.DIGITS, ".", m_isMandatory);
			}

			if (invalidValue.equals(".")) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.PERCENT_DOT_WITH_NO_NUMBERS
								.create()));
			}

			if (invalidValue.indexOf(".") != invalidValue.lastIndexOf(".")) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.PERCENT_MULTIPLE_DOTS.create()));
			}

			if (m_allowNegative
					&& (invalidValue.indexOf("-") != invalidValue
							.lastIndexOf("-"))) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.PERCENT_MULTIPLE_DASHES.create()));
			}

			if (m_allowNegative && (invalidValue.indexOf("-") != -1)
					&& (invalidValue.indexOf("-") != 0)) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.PERCENT_DASH_NOT_FIRST_CHARACTER
								.create()));
			}

			valid = validator.isValid(invalidValue, invalidValue,
					expectedFormat, receiver);
			if (valid) {
				valid = false;
				// TODO: Log this
			}
		}

		return valid;
	}
}
