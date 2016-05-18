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
package org.trade.core.conversion;

/**
 * This is an abstract class which is inherited by all java.lang.Number to
 * java.lang.Number subclass converter classes.
 * 
 * @author Simon Allen
 */
public abstract class NumberToNumberConverter implements JavaTypeConverter {
	/**
	 * Default constructor.
	 */
	public NumberToNumberConverter() {
	}

	//
	// JavaTypeConverter interface methods
	//
	/**
	 * This method is used by the JavaTypeTranslator to convert a source object
	 * of type java.lang.Number to an instance of a subclass of type
	 * java.lang.Number.
	 * 
	 * Subclasses must implement the getConvertedNumber() method for it to work
	 * properly.
	 * 
	 * @param valueToConvert
	 *            the java.lang.Number value to convert
	 * 
	 * 
	 * @return Object the converted value * @exception IllegalArgumentException
	 *         thrown if the valueToConvert is not of type java.lang.Number
	 *         * @see
	 *         org.trade.core.conversion.JavaTypeConverter#convert(Object)
	 */
	public Object convert(Object valueToConvert) throws IllegalArgumentException {
		if (valueToConvert instanceof java.lang.Number) {
			return getConvertedNumber((Number) valueToConvert);
		}

		throw new IllegalArgumentException("The source object must be of type: " + getSourceType().getName());
	}

	/**
	 * This method returns the source type or class that the converter converts
	 * from. In this case java.lang.Number .
	 * 
	 * 
	 * @return Class the class of the source value which will be converted
	 *         * @see
	 *         org.trade.core.conversion.JavaTypeConverter#getSourceType()
	 */
	public Class<?> getSourceType() {
		return java.lang.Number.class;
	}

	//
	// Methods which need to be overridden
	//
	/**
	 * This method should be implemented by a subclass such that it returns the
	 * converted value of the Number.
	 * 
	 * @param aNumber
	 *            the number to be converted
	 * 
	 * @return Number the converted Number
	 */
	protected abstract Number getConvertedNumber(Number aNumber);
}
