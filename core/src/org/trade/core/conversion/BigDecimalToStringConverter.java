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

import java.math.BigDecimal;

/**
 * This class converts instances of java.math.BigDecimal to instances of
 * java.lang.String. Conversion is done using the toString() method of the
 * java.math.BigDecimal class.
 * 
 * An instance of this class is registered as a default converter with the
 * JavaTypeTranslator class.
 * 
 * @author Simon Allen
 */
public class BigDecimalToStringConverter implements JavaTypeConverter {
	/**
	 * Default constructor.
	 */
	public BigDecimalToStringConverter() {
	}

	/**
	 * This method is used by the JavaTypeTranslator to convert a source object
	 * of type java.math.BigDecimal to an instance of type java.lang.String.
	 * 
	 * Conversion is done using the toString() method of the
	 * java.math.BigDecimal class.
	 * 
	 * @param valueToConvert
	 *            the java.math.BigDecimal value to convert
	
	
	 * @return Object the String representation of the valueToConvert * @exception IllegalArgumentException
	 *                thrown if the valueToConvert is not of type
	 *                java.math.BigDecimal * @see org.trade.core.conversion.JavaTypeConverter#convert(Object)
	 */
	public Object convert(Object valueToConvert)
			throws IllegalArgumentException {
		if (valueToConvert instanceof BigDecimal) {
			return valueToConvert.toString();
		}

		throw new IllegalArgumentException(
				"The source object must be of type: "
						+ getSourceType().getName());
	}

	/**
	 * This method returns the source type or class that the converter converts
	 * from. In this case java.util.Date .
	 * 
	
	 * @return Class the class of the source value which will be converted * @see org.trade.core.conversion.JavaTypeConverter#getSourceType()
	 */
	public Class<?> getSourceType() {
		return BigDecimal.class;
	}

	/**
	 * This method returns the target type or class that the converter converts
	 * to. In this case java.lang.String .
	 * 
	
	 * @return Class the class the source value will be converted to * @see org.trade.core.conversion.JavaTypeConverter#getTargetType()
	 */
	public Class<?> getTargetType() {
		return String.class;
	}
}
