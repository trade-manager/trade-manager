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

import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageContextFactory;

/**
 */
public class NumericRangeValidator extends StringValidator {
	private long m_minValue;

	private long m_maxValue;

	/**
	 * Constructor for NumericRangeValidator.
	 * @param messageFactory IMessageFactory
	 * @param maxLength int
	 * @param minValue long
	 * @param maxValue long
	 * @param isMandatory boolean
	 */
	public NumericRangeValidator(IMessageFactory messageFactory, int maxLength,
			long minValue, long maxValue, boolean isMandatory) {
		super(messageFactory, maxLength, StringValidator.DIGITS, isMandatory);

		m_minValue = minValue;
		m_maxValue = maxValue;
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
		boolean valid = true;

		if (null == value) {
			value = "";
		}

		// This will check length, etc.
		valid = super.isValid(value, invalidValue, expectedFormat, receiver);

		// Now we perform the email specific checks
		if (valid && (((String) value).length() != 0)) {
			try {
				long i = Long.parseLong(((String) value));
				if (i < m_minValue) {
					valid = false;
					receiver.addExceptionMessage(getMessageFactory().create(
							MessageContextFactory.BELOW_MIN_VALUE
									.create(MessageContextFactory.MIN_VALUE
											.create(new Long(m_minValue)))));
				}

				if (i > m_maxValue) {
					valid = false;
					receiver.addExceptionMessage(getMessageFactory().create(
							MessageContextFactory.EXCEEDS_MAX_VALUE
									.create(MessageContextFactory.MAX_VALUE
											.create(new Long(m_maxValue)))));
				}
			} catch (Exception ex) {
				// Should not happen as I already have checked it for
				// being numeric
				throw new Error(
						"Coding error - received an exception attempting to conver "
								+ value
								+ " to a long.  This should never happen");
			}
		}

		return valid;
	}
}
