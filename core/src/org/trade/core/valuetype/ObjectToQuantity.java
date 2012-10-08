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

import org.trade.core.conversion.JavaDynamicTypeConverter;
import org.trade.core.conversion.JavaTypeTranslatorException;

/**
 */
public class ObjectToQuantity implements JavaDynamicTypeConverter {
	/**
	 * Default constructor.
	 */
	public ObjectToQuantity() {
	}

	/**
	 * Method convert.
	 * @param targetType Class<?>
	 * @param valueToConvert Object
	 * @return Object
	 * @throws JavaTypeTranslatorException
	 * @see org.trade.core.conversion.JavaDynamicTypeConverter#convert(Class<?>, Object)
	 */
	public Object convert(Class<?> targetType, Object valueToConvert)
			throws JavaTypeTranslatorException {
		Quantity rVal = null;

		if (valueToConvert == null) {
			throw new JavaTypeTranslatorException(
					"Null passed toObjectToMoney.convert()");
		}

		// If we get a string we will convert it using the default money format
		// MONEY_NONNEGATIVE_11_2.
		if (valueToConvert instanceof String) {
			String stringValue = (String) valueToConvert;
			rVal = new Quantity(stringValue);
		} else {
			throw new JavaTypeTranslatorException(
					"internal error parsing value");
		}

		return (rVal);
	}

	// from com.cbsinc.esc.devtools.conversion.JavaDynamicTypeConverter
	/**
	 * Method supportsConversion.
	 * @param targetType Class<?>
	 * @param valueToConvert Object
	 * @return boolean
	 * @see org.trade.core.conversion.JavaDynamicTypeConverter#supportsConversion(Class<?>, Object)
	 */
	public boolean supportsConversion(Class<?> targetType, Object valueToConvert) {
		boolean rVal = false;
		// TODO: This is not strictly correct.
		if (Quantity.class.equals(targetType)) {
			rVal = true;
		}

		return (rVal);
	}
}
