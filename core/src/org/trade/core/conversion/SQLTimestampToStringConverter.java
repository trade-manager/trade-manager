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
package org.trade.core.conversion;

import java.sql.Timestamp;

/**
 * This class converts instances of java.sql.Timestamp to instances of
 * java.lang.String. Conversion is done using the toString() method of the
 * java.sql.Timestamp class.
 * 
 * An instance of this class is registered as a default converter with the
 * JavaTypeTranslator class.
 * 
 * @see java.sql.Timestamp
 * @see com.cbsinc.esc.devtools.conversion.JavaTypeConverter
 * @version $Id: SQLTimestampToStringConverter.java,v 1.1 2001/09/19 20:31:03
 *          clay Exp $
 * @author Simon Allen
 */
public class SQLTimestampToStringConverter implements JavaTypeConverter {
	/**
	 * Default constructor.
	 */
	public SQLTimestampToStringConverter() {
	}

	//
	// JavaTypeConverter interface methods
	//
	/**
	 * This method is used by the JavaTypeTranslator to convert a source object
	 * of type java.sql.Timestamp to an instance of type java.lang.String.
	 * 
	 * Conversion is done using the toString() method of the java.sql.Timestamp
	 * class.
	 * 
	 * @param valueToConvert
	 *            the java.sql.Timestamp value to convert
	
	
	 * @return Object the String representation of the valueToConvert * @exception IllegalArgumentException
	 *                thrown if the valueToConvert is not of type
	 *                java.sql.Timestamp * @see org.trade.core.conversion.JavaTypeConverter#convert(Object)
	 */
	public Object convert(Object valueToConvert)
			throws IllegalArgumentException {
		if (valueToConvert instanceof java.sql.Timestamp) {
			return ((Timestamp) valueToConvert).toString();
		}

		throw new IllegalArgumentException(
				"The source object must be of type: "
						+ getSourceType().getName());
	}

	/**
	 * This method returns the source type or class that the converter converts
	 * from. In this case java.sql.Timestamp .
	 * 
	
	 * @return Class the class of the source value which will be converted * @see org.trade.core.conversion.JavaTypeConverter#getSourceType()
	 */
	public Class<?> getSourceType() {
		return java.sql.Timestamp.class;
	}

	/**
	 * This method returns the target type or class that the converter converts
	 * to. In this case java.lang.String .
	 * 
	
	 * @return Class the class the source value will be converted to * @see org.trade.core.conversion.JavaTypeConverter#getTargetType()
	 */
	public Class<?> getTargetType() {
		return java.lang.String.class;
	}
}
