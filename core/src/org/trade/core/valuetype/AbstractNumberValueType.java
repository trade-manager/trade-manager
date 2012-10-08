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

import org.trade.core.exception.ExceptionContext;
import org.trade.core.message.IMessageFactory;
import org.trade.core.validator.ExceptionMessageListener;
import org.trade.core.validator.StringValidator;
import org.trade.core.validator.Validator;

/**
 */
public abstract class AbstractNumberValueType extends StringWrapper {

	private static final long serialVersionUID = 2808106869428773106L;

	public AbstractNumberValueType() {
	}

	/**
	 * Constructor for AbstractNumberValueType.
	 * @param value String
	 */
	public AbstractNumberValueType(String value) {
		super(value);
	}

	/**
	 * Constructor for AbstractNumberValueType.
	 * @param value long
	 */
	public AbstractNumberValueType(long value) {
		super(notNull(value));
	}

	/**
	 * Constructor for AbstractNumberValueType.
	 * @param value Long
	 */
	public AbstractNumberValueType(Long value) {
		super(notNull(value));
	}

	/**
	 * Method getNumber.
	 * @return long
	 */
	public long getNumber() {
		long value = 0;

		if (!isEmpty()) {
			try {
				value = Long.parseLong(getInternalValue());
			} catch (NumberFormatException t) {
				throw t;
			}
		}

		return (value);
	}

	/**
	 * Method getLong.
	 * @return Long
	 */
	public Long getLong() {
		return (new Long(getNumber()));
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		if (!isEmpty()) {
			return (getInternalValue());
		} else {
			return ("");
		}
	}

	/**
	 * Returns validator of this value type.
	 * 
	
	
	 * @param messageFactory IMessageFactory
	 * @return validator * @see <code>IStringValidator</code> */
	public Validator getDefaultMandatoryValidator(IMessageFactory messageFactory) {
		return getDefaultValidator(messageFactory, true);
	}

	/**
	 * Returns validator of this value type.
	 * 
	
	
	 * @param messageFactory IMessageFactory
	 * @return validator * @see <code>IStringValidator</code> */
	public Validator getDefaultOptionalValidator(IMessageFactory messageFactory) {
		return getDefaultValidator(messageFactory, false);
	}

	/**
	 * Returns the maximum length. This method should be overwritten by
	 * subclasses.
	 * 
	
	 * @return int */
	protected abstract int getMaximumLength();

	/**
	 * Method getMaximumValue.
	 * @return Long
	 */
	protected abstract Long getMaximumValue();

	/**
	 * Method getMinimumValue.
	 * @return Long
	 */
	protected abstract Long getMinimumValue();

	/**
	 * Returns validator of this value type.
	 * 
	
	
	 * @param messageFactory IMessageFactory
	 * @param isMandatory boolean
	 * @return validator * @see <code>Validator</code> */
	protected Validator getDefaultValidator(IMessageFactory messageFactory,
			boolean isMandatory) {
		return new StringValidator(messageFactory, getMaximumLength(),
				StringValidator.DIGITS, "", isMandatory) {
			public boolean isValid(Object value, String invalidValue,
					String expectedFormat, ExceptionMessageListener receiver) {
				boolean valid = super.isValid(value, invalidValue,
						expectedFormat, receiver);

				do {
					if (valid && !AbstractNumberValueType.this.isEmpty()) {
						Long max = AbstractNumberValueType.this
								.getMaximumValue();
						Long min = AbstractNumberValueType.this
								.getMinimumValue();
						long longValue = 0;

						try {
							longValue = Long.parseLong((String) value);
						} catch (Throwable t) {
							receiver.addExceptionMessage(getMessageFactory()
									.create(new ExceptionContext(
											"edit_check",
											"Value ["
													+ value
													+ "] is not in correct number format.")));
							break;
						}

						if ((max != null) && (longValue > max.longValue())) {
							receiver.addExceptionMessage(getMessageFactory()
									.create(new ExceptionContext("edit_check",
											"Value can not be greater than "
													+ max)));

						}
						if ((min != null) && (longValue < min.longValue())) {
							receiver.addExceptionMessage(getMessageFactory()
									.create(new ExceptionContext("edit_check",
											"Value can not be less than " + min)));
						}
					}
				} while (false);

				return (valid);
			}

		};
	}

	/**
	 * Method notNull.
	 * @param value Long
	 * @return String
	 */
	private static String notNull(Long value) {
		if (value == null) {
			return ("");
		} else {
			return (value.toString());
		}
	}

	/**
	 * Method notNull.
	 * @param value long
	 * @return String
	 */
	private static String notNull(long value) {
		if (value == 0) {
			return ("");
		} else {
			return (Long.toString(value));
		}
	}
}