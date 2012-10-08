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

import org.trade.core.conversion.JavaTypeTranslator;
import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageFactory;
import org.trade.core.validator.ExceptionMessageListener;
import org.trade.core.validator.Validator;

/**
 */
public abstract class StringWrapper extends ValueType {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8867039181191953504L;

	static {
		// Register the appropriate converters
		JavaTypeTranslator
				.registerDynamicTypeConverter(new ObjectToStringWrapper());
		JavaTypeTranslator
				.registerDynamicTypeConverter(new StringWrapperToObject());
	}

	private String m_value = "";

	/**
	 * Default Constructor
	 */
	public StringWrapper() {
	}

	/**
	 * Constructor for StringWrapper.
	 * @param value String
	 */
	public StringWrapper(String value) {
		setInternalValue(value);
	}

	/**
	 * Convenience method. Validate using default validator and discard
	 * messages. Assumes the value is optional.
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
		return validator.isValid(m_value, null, null, receiver);
	}

	/**
	 * Method getDefaultOptionalValidator.
	 * @param messageFactory IMessageFactory
	 * @return Validator
	 */
	public Validator getDefaultOptionalValidator(IMessageFactory messageFactory) {
		return getDefaultValidator(messageFactory, false);
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
	protected abstract Validator getDefaultValidator(
			IMessageFactory messageFactory, boolean isMandatory);

	/**
	 * Method isEmpty.
	 * @return boolean
	 */
	public boolean isEmpty() {
		return (m_value.length() == 0);
	}

	/**
	 * Method equals.
	 * @param objectToCompare Object
	 * @return boolean
	 */
	public boolean equals(Object objectToCompare) {
		if (this == objectToCompare) {
			return true;
		}
		if (objectToCompare == null) {
			return false;
		}

		if (!(objectToCompare instanceof StringWrapper)) {
			return false;
		}

		return (getInternalValue().equals(((StringWrapper) objectToCompare)
				.getInternalValue()));
	}

	/**
	 * Method hashCode.
	 * @return int
	 */
	public int hashCode() {
		return this.toString().hashCode();
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return (getInternalValue());
	}

	/**
	 * Method setValue.
	 * @param value String
	 */
	public void setValue(String value) {
		setInternalValue(value);
	}

	/**
	 * Method setValue.
	 * @param value StringWrapper
	 */
	public void setValue(StringWrapper value) {
		if (null != value) {
			setInternalValue(value.m_value);
		} else {
			setInternalValue(null);
		}
	}

	//
	// Protected Methods
	//
	/**
	 * Method getInternalValue.
	 * @return String
	 */
	protected String getInternalValue() {
		return (m_value);
	}

	/**
	 * Method setInternalValue.
	 * @param value String
	 */
	protected void setInternalValue(String value) {
		if (null == value) {
			m_value = "";
		} else {
			m_value = value.trim();
		}
	}
}
