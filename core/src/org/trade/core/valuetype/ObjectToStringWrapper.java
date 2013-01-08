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
public class ObjectToStringWrapper implements JavaDynamicTypeConverter {
	/**
	 * Default constructor.
	 */
	public ObjectToStringWrapper() {
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
	public Object convert(Class<?> targetType, Object valueToConvert)
			throws JavaTypeTranslatorException {
		Object rVal = null;

		if (StringWrapper.class.isAssignableFrom(targetType)) {
			try {

				StringWrapper vt = (StringWrapper) targetType.newInstance();

				if (valueToConvert instanceof String) {
					vt.setValue((String) valueToConvert);
				} else {
					throw new JavaTypeTranslatorException(
							"The ObjectToStringWrapper convertor only supports strings at the moment");
				}

				rVal = vt;
			} catch (Exception ex) {
				throw new JavaTypeTranslatorException(ex,
						"Unable to set value for StringWrapper");
			}
		} else {
			throw new JavaTypeTranslatorException(
					"Target type must be a StringWrapper");
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
	 * @see 
	 *      org.trade.core.conversion.JavaDynamicTypeConverter#supportsConversion
	 *      (Class<?>, Object)
	 */
	public boolean supportsConversion(Class<?> targetType, Object valueToConvert) {
		boolean rVal = false;
		if (StringWrapper.class.isAssignableFrom(targetType)) {
			rVal = true;
		}
		return (rVal);
	}
}
