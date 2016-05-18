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
import org.trade.core.conversion.JavaTypeTranslator;
import org.trade.core.conversion.JavaTypeTranslatorException;

/**
 */
public class QuantityToObject implements JavaDynamicTypeConverter {

	public QuantityToObject() {
	}

	/**
	 * Method convert.
	 * 
	 * @param targetType
	 *            Class<?>
	 * @param valueToConvert
	 *            Object
	 * @return Object
	 * @throws JavaTypeTranslatorException
	 * @see org.trade.core.conversion.JavaDynamicTypeConverter#convert(Class<?>,
	 *      Object)
	 */
	public Object convert(Class<?> targetType, Object valueToConvert) throws JavaTypeTranslatorException {
		Object rVal = null;

		if (valueToConvert instanceof Quantity) {
			rVal = JavaTypeTranslator.convert(targetType, valueToConvert.toString());
		} else {
			throw new JavaTypeTranslatorException("Value to convert must be a Quantity");
		}

		return (rVal);
	}

	/**
	 * Method supportsConversion.
	 * 
	 * @param targetType
	 *            Class<?>
	 * @param valueToConvert
	 *            Object
	 * @return boolean
	 * @see org.trade.core.conversion.JavaDynamicTypeConverter#
	 *      supportsConversion (Class<?>, Object)
	 */
	public boolean supportsConversion(Class<?> targetType, Object valueToConvert) {
		boolean rVal = false;
		if (valueToConvert instanceof Quantity) {
			rVal = true;
		}

		return (rVal);
	}
}
