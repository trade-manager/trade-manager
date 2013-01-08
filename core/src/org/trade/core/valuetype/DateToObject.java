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

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.trade.core.conversion.JavaDynamicTypeConverter;
import org.trade.core.conversion.JavaFormatForObject;
import org.trade.core.conversion.JavaTypeTranslator;
import org.trade.core.conversion.JavaTypeTranslatorException;

/**
 */
public class DateToObject implements JavaDynamicTypeConverter {
	public DateToObject() {
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

		if (valueToConvert instanceof Date) {
			// If converting to a String want to send it back as a preformatted
			// string in GMT
			if (String.class.equals(targetType)) {
				rVal = ((Date) valueToConvert).toString();
			} else {
				rVal = JavaTypeTranslator.convert(targetType,
						((Date) valueToConvert).getDate());
			}
		} else if (valueToConvert instanceof JavaFormatForObject) {
			// If the target is a Formatted object
			// Get the object and translate it to a formatted date
			// representation
			// before trying to translate it to the target type
			Object getFor = ((JavaFormatForObject) valueToConvert)
					.getForObject();
			String format = ((JavaFormatForObject) valueToConvert).getFormat();
			if (getFor instanceof Date) {
				try {
					SimpleDateFormat formatter = new SimpleDateFormat(format);
					formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
					String sDate = formatter.format(((Date) getFor).getDate());

					rVal = JavaTypeTranslator.convert(targetType, sDate);
				} catch (Exception ex) {
					throw new JavaTypeTranslatorException(ex,
							"Unable to convert Date to GMT Date representation using format '"
									+ format + "'");
				}
			} else {
				throw new JavaTypeTranslatorException(
						"Value of JavaFormatForObject.getForObject() must be a Date");
			}
		} else {
			throw new JavaTypeTranslatorException(
					"Value to convert must be a Date");
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
		if ((valueToConvert instanceof Date)
				|| ((valueToConvert instanceof JavaFormatForObject) && (((JavaFormatForObject) valueToConvert)
						.getForObject() instanceof Date))) {
			rVal = true;
		}

		return (rVal);
	}
}
