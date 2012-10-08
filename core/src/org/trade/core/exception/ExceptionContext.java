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
package org.trade.core.exception;

/**
 * Describes the context in which an exception occurred. Contains the
 * information for constructing the message about a specific exception.
 * 
 * @version $Id: ExceptionContext.java 1.2 2001/01/23 23:05:48Z Garrick.Olson
 *          dev $
 * @author Simon Allen
 */
public class ExceptionContext implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4837522639316151345L;

	// constants
	private final static String NULL_VALUE = "";

	// member variables
	private String m_parameterName;

	private String m_value;

	/**
	 * Constructor.
	 * @param parameterName String
	 * @param value Object
	 */
	public ExceptionContext(String parameterName, Object value) {
		m_parameterName = parameterName;
		setValue(value);
	}

	/**
	 * Copy constructor.
	 * @param other ExceptionContext
	 */
	public ExceptionContext(ExceptionContext other) {
		m_parameterName = other.m_parameterName;
		m_value = other.m_value;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 *            the object to be copied
	 * @param value
	 *            the new value for this context (overrides the value of the
	 *            object being copied
	 */
	public ExceptionContext(ExceptionContext other, Object value) {
		m_parameterName = other.m_parameterName;
		setValue(value);
	}

	/**
	 * Represents the name used within exception messages to refer to this
	 * context.
	 * @return String
	 */
	public String getParameterName() {
		return m_parameterName;
	}

	/**
	 * Represents the context of the exception. This value should be directly
	 * substituted into a named parameter in an exception message.
	 * @return String
	 */
	public String getValue() {
		return m_value;
	}

	/**
	 * Method setValue.
	 * @param value Object
	 */
	private void setValue(Object value) {
		if (null == value) {
			value = NULL_VALUE;
		}

		m_value = value.toString();
	}
}
