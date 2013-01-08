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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * This class converts instances of java.lang.String to instances of
 * java.util.Date. Conversion is done if the String instance is in the correct
 * format, and it uses the parse() method of the java.text.DateFormat class.
 * 
 * An instance of this class is registered as a default converter with the
 * JavaTypeTranslator class.
 * 
 * @see java.text.DateFormat
 * @author Simon Allen
 */
public class StringToDateConverter extends StringToObjectConverter {
	/**
	 * Default constructor.
	 */
	public StringToDateConverter() {
		// Default is short version of date and time:
		// MM/DD/YY HH:MI:SS AM|PM TZ
		// Timezone is assumed to be the local system timezone.
		m_dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT,
				DateFormat.LONG);
	}

	//
	// JavaTypeConverter interface methods
	//
	/**
	 * This method returns the target type or class that the converter converts
	 * to. In this case java.util.Date .
	 * 
	
	 * @return Class the class the source value will be converted to * @see org.trade.core.conversion.JavaTypeConverter#getTargetType()
	 */
	public Class<?> getTargetType() {
		return Date.class;
	}

	//
	// Methods which need to be overridden
	//
	/**
	 * This method converts the String value to a java.util.Date by using the
	 * parse() method of the java.text.DateFormat class.
	 * 
	 * @param aString
	 *            the String to be converted
	
	
	 * @return Object the String converted to a java.util.Date * @exception IllegalArgumentException
	 *                thrown if the String to convert is not in the correct
	 *                format */
	protected Object getConvertedString(String aString)
			throws IllegalArgumentException {
		if ((aString == null) || ((aString.trim().length()) == 0)) {
			return null; // Return A null Date
		} else {
			try {
				return m_dateFormatter.parse(aString);
			} catch (ParseException pe) {
				throw new IllegalArgumentException(pe.getMessage());
			}
		}
	}

	// Private
	DateFormat m_dateFormatter = null;
}
