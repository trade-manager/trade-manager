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
 * This class converts instances of java.lang.Object to instances of
 * com.cbsinc.esc.devtools.valuetype.base.CodeDecodeValueType. The conversion
 * will set the value passed as the code that this valuetype represents.
 * 
 * @version $Id: ObjectToDecode.java,v 1.1 2001/11/06 16:51:55 simon Exp $
 * @author Simon Allen
 */
public class ObjectToDecode implements JavaDynamicTypeConverter {

	public ObjectToDecode() {
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

		if (Decode.class.isAssignableFrom(targetType)) {
			try {
				Decode vt = (Decode) targetType.newInstance();

				// Assign the value for the valuetype
				vt.setValue(valueToConvert);

				rVal = vt;
			} catch (Exception ex) {
				throw new JavaTypeTranslatorException(ex, "Unable to set code");
			}
		} else {
			throw new JavaTypeTranslatorException(
					"Target type must be a com.aceva.devtools.valuetype.base.Decode");
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

		if (Decode.class.isAssignableFrom(targetType)) {
			rVal = true;
		}

		return (rVal);
	}
}
