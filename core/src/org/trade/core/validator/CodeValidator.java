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
package org.trade.core.validator;

import java.util.Collection;

import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageContextFactory;

/**
 */
public class CodeValidator implements Validator {
	private IMessageFactory m_messageFactory;

	private Collection<?> m_acceptableValues;

	private boolean m_isMandatory;

	/**
	 * Constructor for CodeValidator.
	 * @param messageFactory IMessageFactory
	 * @param acceptableValues Collection<?>
	 * @param isMandatory boolean
	 */
	public CodeValidator(IMessageFactory messageFactory,
			Collection<?> acceptableValues, boolean isMandatory) {
		m_messageFactory = messageFactory;
		m_acceptableValues = acceptableValues;
		m_isMandatory = isMandatory;
	}

	/**
	 * Method getMessageFactory.
	 * @return IMessageFactory
	 */
	protected final IMessageFactory getMessageFactory() {
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
		boolean valid = true;

		if (null == value) {
			value = "";
		}

		if (0 == ((String) value).length()) // Optional/mandatory check
		{
			if (m_isMandatory) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.MANDATORY_VALUE_NOT_PROVIDED
								.create()));
			}
		} else
		// 0 < length so check valid values
		{
			if (!m_acceptableValues.contains(value)) {
				valid = false;
				receiver.addExceptionMessage(getMessageFactory().create(
						MessageContextFactory.CODE_NOT_VALID
								.create(MessageContextFactory.INVALID_CODE
										.create(value))));
			}
		}

		return valid;
	}
}
