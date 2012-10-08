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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is used to convert values of one type to another type. It
 * delegates conversion to registered converter instances which implement a
 * common interface: JavaTypeConverter.
 * 
 * To make a conversion, execute something along the lines of the following:
 * 
 * <code>
 * <p>NewClass newValue = (NewClass) JavaTypeTranslator(NewClass.class, oldValue);
 * </code>
 * 
 * New converters can be registered with the JavaTypeTranslator to either change
 * existing conversion behaviour or to enable previously unsupported
 * conversions. To register a new converter simply execute the following:
 * 
 * <code>
 * <p>JavaTypeTranslator.registerConverter(new MyConverter());
 * </code>
 * 
 * @version $Id: JavaTypeTranslator.java,v 1.1 2001/09/19 20:31:04 clay Exp $
 * @author Simon Allen
 */
public class JavaTypeTranslator {
	/**
	 * A hashtable of hashtables. The outer hashtable consists of a set of keys
	 * representing target types. The values at these keys consist of inner
	 * hashtables whose keys represent source types. The inner hashtables'
	 * values are the actual converter instances used by the JavaTypeTranslator.
	 */
	private static Hashtable<Class<?>, Hashtable<Class<?>, JavaTypeConverter>> m_converters = new Hashtable<Class<?>, Hashtable<Class<?>, JavaTypeConverter>>();

	private static Hashtable<?, ?> m_noBaseConverters = new Hashtable<Object, Object>();

	private static Vector<Object> m_dynConverters = new Vector<Object>();

	/**
	 * Flag indicating whether supertype conversions are supported by the
	 * JavaTypeTranslator. True is the default.
	 */
	private static boolean m_allowSupertypeConversions = true;

	static {
		// when the class is loaded register a number of
		// default converter instances
		registerConverter(new StringToBooleanConverter());
		registerConverter(new NumberToByteConverter());
		registerConverter(new StringToByteConverter());
		registerConverter(new StringToCharacterConverter());
		registerConverter(new StringToDateConverter());
		registerConverter(new StringToBigDecimalConverter());
		registerConverter(new BigDecimalToStringConverter());
		registerConverter(new NumberToDoubleConverter());
		registerConverter(new StringToDoubleConverter());
		registerConverter(new NumberToFloatConverter());
		registerConverter(new StringToFloatConverter());
		registerConverter(new NumberToIntegerConverter());
		registerConverter(new StringToIntegerConverter());
		registerConverter(new NumberToLongConverter());
		registerConverter(new StringToLongConverter());
		registerConverter(new NumberToShortConverter());
		registerConverter(new StringToShortConverter());
		registerConverter(new StringToSQLDateConverter());
		registerConverter(new StringToSQLTimeConverter());
		registerConverter(new StringToSQLTimestampConverter());
		registerConverter(new ObjectToStringConverter());
		registerConverter(new DateToStringConverter());
		registerConverter(new SQLDateToStringConverter());
		registerConverter(new SQLTimeToStringConverter());
		registerConverter(new SQLTimestampToStringConverter());
	}

	/**
	 * This method may be used to convert a specified object to the specified
	 * class type.
	 * 
	 * @param targetType
	 *            the type to be converted to
	 * @param sourceValue
	 *            the object value to convert
	
	
	 * @return Object the converted object * @exception JavaTypeTranslatorException
	 *                thrown if any exception occurs */
	public static Object convert(Class<?> targetType, Object sourceValue)
			throws JavaTypeTranslatorException {
		// before: simply return the value if it is already of the correct type
		// (meaning that null simply returns null, which is always the correct
		// type
		// now: return an empty object of the target type, created by
		// converting "" to the type.
		// (if that doesn't work, just create a new istance and don't set the
		// value
		if (targetType.isInstance(sourceValue)) {
			return sourceValue;
		}

		if (null == sourceValue) {
			try {
				return convert(targetType, "");
			} catch (JavaTypeTranslatorException x) {
				try {
					return targetType.newInstance();
				} catch (Exception illegalX) // ille
				{
					throw new JavaTypeTranslatorException(illegalX,
							"permission is denied to create an instance of "
									+ targetType.toString());
				}
			}
		}

		// Perform an additional check for JavaFormatForObject source instances
		// if the object it is representing is null then return null as null by
		// default should not be formatted
		if (sourceValue instanceof JavaFormatForObject) {
			if (null == ((JavaFormatForObject) sourceValue).getForObject()) {
				return (null);
			}
		}

		// locate the proper converter
		Hashtable<?, ?> innerTable = m_converters.get(targetType);

		// No base converters registered to deal with the conversion
		if (innerTable == null) {
			innerTable = m_noBaseConverters;
		}

		boolean topClass = true;
		Class<?> sourceType = sourceValue.getClass();

		while (sourceType != null) {
			JavaTypeConverter converter = (JavaTypeConverter) innerTable
					.get(sourceType);

			if (converter != null) {
				// there is a converter for the given targetType and sourceType,
				// try converting the source value with it
				try {
					return converter.convert(sourceValue);
				} catch (IllegalArgumentException iae) {
					throw new JavaTypeTranslatorException(
							"The source value, of type "
									+ sourceValue.getClass().getName()
									+ ", cannot be converted to "
									+ targetType.getName()
									+ " because it is not in the proper format");
				}
			}

			if (m_allowSupertypeConversions) {
				// Try the registered dynamic converters
				if (topClass) {
					// Check the Dynamic Converters
					Enumeration<Object> en = m_dynConverters.elements();
					while (en.hasMoreElements()) {
						JavaDynamicTypeConverter dc = (JavaDynamicTypeConverter) en
								.nextElement();

						if (dc.supportsConversion(targetType, sourceValue)) {
							// The first matching dynamic converter will be used
							return dc.convert(targetType, sourceValue);
						}
					}
				}

				// there is no converter for the given targetType and sourceType
				// so see if there is one for the given targetType and
				// sourceType
				// superclass
				sourceType = sourceType.getSuperclass();
				// Have already tried the dynamic converters
				topClass = false;
			} else {
				// there is no converter for the given targetType and sourceType
				// so bail and throw an exception
				sourceType = null;
			}
		}

		// If I get to here - there are no registered converters to handle the
		// conversion
		throw new JavaTypeTranslatorException(
				"There is no converter for converting "
						+ sourceValue.getClass().getName() + " values to "
						+ targetType.getName());
	}

	/**
	 * This method may be used to register a converter with the
	 * JavaTypeTranslator. Any converter which has previously been registered
	 * with the JavaTypeTranslator that has the same source and target types
	 * will be replaced and the new converter being registered will be used for
	 * those types instead.
	 * 
	 * @param theConverter
	 *            the converter instance to register
	 */
	public static void registerConverter(JavaTypeConverter theConverter) {
		Class<?> targetType = theConverter.getTargetType();
		Class<?> sourceType = theConverter.getSourceType();
		Hashtable<Class<?>, JavaTypeConverter> innerTable = m_converters
				.get(targetType);

		if (innerTable != null) {
			// add the converter to the existing list of
			// converters for its targetType, replacing
			// any converter which has the same sourceType
			// as well
			innerTable.put(sourceType, theConverter);
		} else {
			// there is no list of existing converters for
			// this converter's targetType, create one and
			// add the converter to it
			innerTable = new Hashtable<Class<?>, JavaTypeConverter>(10);

			innerTable.put(sourceType, theConverter);
			m_converters.put(targetType, innerTable);
		}
	}

	/**
	 * This method may be used to register a dynamic converter with the
	 * JavaTypeTranslator.
	 * 
	 * @param theConverter
	 *            the converter instance to register
	 */
	public static void registerDynamicTypeConverter(
			JavaDynamicTypeConverter theConverter) {
		if (!m_dynConverters.contains(theConverter)) {
			m_dynConverters.addElement(theConverter);
		}
	}

	/**
	 * This method may be used to set whether the JavaTypeTranslator will use a
	 * converter whose source type is a superclass of a source type being
	 * converted from, if there is no converter available for that specific
	 * source type. For example, it determines whether a Number to String
	 * converter will be used if no Double to String converter is available.
	 * 
	 * @param allowSupertypes
	 *            flag setting whether supertype conversions are supported. True
	 *            indicates supertype conversions are supported, false indicates
	 *            they are not.
	 */
	public static void setAllowSupertypeConversions(boolean allowSupertypes) {
		m_allowSupertypeConversions = allowSupertypes;
	}

	/**
	 * This method may be used to verify whether the JavaTypeTranslator will use
	 * a converter whose source type is a superclass of a source type being
	 * converted from if there is no converter available for that specific
	 * source type. For example, it indicates whether a Number to String
	 * converter will be used if no Double to String converter is available.
	 * 
	
	 * @return boolean Flag indicating whether supertype conversions are
	 *         supported. True indicates supertype conversions are supported,
	 *         false indicates they are not. True is the default. */
	public static boolean isAllowSupertypeConversions() {
		return m_allowSupertypeConversions;
	}
}
