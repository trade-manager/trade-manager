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

/**
 * This interface must be implemented by any converter class which is to be used
 * with the JavaTypeTranslator.
 * 
 * @see com.cbsinc.esc.devtools.conversion.JavaTypeTranslator
 * @version $Id: JavaTypeConverter.java,v 1.1 2001/09/19 20:31:04 clay Exp $
 * @author Simon Allen
 */
public interface JavaTypeConverter {
	/**
	 * This method is used by the JavaTypeTranslator to convert a source object
	 * to a given target type or class.
	 * 
	 * @param valueToConvert
	 *            the object value to convert
	
	
	 * @return Object the converted object * @exception IllegalArgumentException */
	Object convert(Object valueToConvert) throws IllegalArgumentException;

	/**
	 * This method returns the target type or class that the converter converts
	 * to. It is used by the JavaTypeTranslator to determine, upon registration
	 * of a particular converter, what target type the converter creates.
	 * 
	
	 * @return Class the class the source value will be converted to */
	Class<?> getTargetType();

	/**
	 * This method returns the source type or class that the converter converts
	 * from. It is used by the JavaTypeTranslator to determine, upon
	 * registration of a particular converter, what source type the converter
	 * accepts.
	 * 
	
	 * @return Class the class of the source value which will be converted */
	Class<?> getSourceType();
}
