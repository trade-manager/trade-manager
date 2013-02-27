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

import java.util.Comparator;
import java.util.Vector;

import org.trade.core.conversion.JavaTypeTranslator;
import org.trade.core.dao.Aspect;
import org.trade.core.lookup.Lookup;
import org.trade.core.lookup.LookupQualifier;
import org.trade.core.lookup.LookupService;
import org.trade.core.lookup.PropertiesLookup;
import org.trade.core.util.CoreUtils;

/**
 * This class is suppose to represent a base class for a specialized CodeDecode
 * type object e.g. US State Codes and Descriptions.
 * 
 * Note : This object is not intended to be used directly.
 * 
 * This object will use a LookupService in order to obtain a Lookup containing
 * all of the Systems CODE_DECODE values for a specific Type.
 * 
 * @version $Id: Decode.java,v 1.1 2001/11/06 16:51:54 simon Exp $
 * @author Simon Allen
 */
public class Decode extends ValueType implements Comparator<Decode>,
		Comparable<Decode> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5356057478795774210L;

	/**
	 * The Lookup Identifier for CODE_DECODE values
	 */
	public static final String CODE_DECODE_IDENTIFIER = "CODE_DECODE";
	public static final String NONE = " ";

	static {
		// Register the appropriate converters
		JavaTypeTranslator.registerDynamicTypeConverter(new ObjectToDecode());
		JavaTypeTranslator.registerDynamicTypeConverter(new DecodeToObject());
	}

	/**
	 * The Column identifers that should be returned in the Lookup
	 */
	public static final String _TYPE = "_TYPE";
	public static final String _CODE = "_CODE";
	public static final String _DISPLAY_NAME = "_DISPLAY_NAME";

	private String m_codeDecodeType = "";
	private String m_codeDecodeIdentifier = "";

	private Lookup m_lookup = null;
	private Object m_badValue = null;

	protected static Boolean m_ascending = new Boolean(true);

	/**
	 * Default Constructor
	 * 
	 */
	public Decode() {

	}

	/**
	 * Default Constructor
	 * 
	 * 
	 * @param codeDecodeType
	 *            String
	 * @param columnNames
	 *            Vector<String>
	 * @param values
	 *            Vector<Object>
	 * @param identifier
	 *            String
	 */
	public Decode(String codeDecodeType, Vector<String> columnNames,
			Vector<Object> values, String identifier) {

		m_codeDecodeType = codeDecodeType;
		m_codeDecodeIdentifier = identifier;
		m_lookup = new PropertiesLookup(columnNames, values);
	}

	/**
	 * Default Constructor
	 * 
	 * 
	 * @param codeDecodeType
	 *            String
	 * @param columnNames
	 *            Vector<String>
	 * @param values
	 *            Vector<Object>
	 */
	public Decode(String codeDecodeType, Vector<String> columnNames,
			Vector<Object> values) {

		m_codeDecodeType = codeDecodeType;
		m_codeDecodeIdentifier = CODE_DECODE_IDENTIFIER;
		m_lookup = new PropertiesLookup(columnNames, values);
	}

	/**
	 * Default Constructor
	 * 
	 * 
	 * @param codeDecodeType
	 *            String
	 */
	public Decode(String codeDecodeType) {
		this(codeDecodeType, false);
	}

	/**
	 * Default Constructor
	 * 
	 * 
	 * @param codeDecodeType
	 *            String
	 * @param optional
	 *            boolean
	 */
	public Decode(String codeDecodeType, boolean optional) {
		m_codeDecodeType = codeDecodeType;
		m_codeDecodeIdentifier = CODE_DECODE_IDENTIFIER;

		final LookupQualifier qualifier = new LookupQualifier();

		qualifier.setValue(m_codeDecodeIdentifier + _TYPE, m_codeDecodeType);

		try {
			m_lookup = LookupService.getLookup(m_codeDecodeIdentifier,
					qualifier, optional);
		} catch (final Exception ex) {
			m_lookup = new PropertiesLookup(null, null);
		}
	}

	/**
	 * Default Constructor
	 * 
	 * 
	 * @param codeDecodeType
	 *            String
	 * @param identifier
	 *            String
	 * @param optional
	 *            boolean
	 */
	public Decode(String codeDecodeType, String identifier, boolean optional) {
		m_codeDecodeType = codeDecodeType;
		m_codeDecodeIdentifier = identifier;

		final LookupQualifier qualifier = new LookupQualifier();

		qualifier.setValue(m_codeDecodeIdentifier + _TYPE, m_codeDecodeType);

		try {
			m_lookup = LookupService.getLookup(m_codeDecodeIdentifier,
					qualifier, optional);
		} catch (final Exception ex) {
			m_lookup = new PropertiesLookup(null, null);
		}
	}

	/**
	 * 
	 * @return true, if the code within this object is empty. false if the code
	 *         is empty because it was given an invalid string (but getError
	 *         will give the error message and isValid will be false
	 */
	public boolean isEmpty() {
		if ((getCode().equals(""))
				&& ((null == m_badValue) || (m_badValue.equals("")))) {
			return (true);
		}

		return (false);
	}

	/**
	 * 
	 * @return true, if this object represents a valid code, false otherwise
	 */
	public boolean isValid() {
		if (isEmpty()) {
			return true;
		}

		if (getCode().equals("")) {
			return (false);
		}

		return (true);
	}

	/**
	 * Method getError.
	 * 
	 * @return String
	 */
	public String getError() {
		if (!(isValid())) {
			if (m_badValue != null) {
				return m_badValue + " does not represent a valid "
						+ m_codeDecodeType;
			} else {
				return " does not represent a valid " + m_codeDecodeType;
			}
		}

		return null;
	}

	/**
	 * 
	 * @return The current code for the object, "" if not set.
	 */
	public Object getObject() {
		Object val = null;

		try {
			val = m_lookup.getValueAt(m_codeDecodeIdentifier + _CODE);

		} catch (final Exception ex) {
			// ignore
		}

		return (val);
	}

	/**
	 * 
	 * @return The current code for the object, "" if not set.
	 */
	public String getCode() {
		String rVal = "";

		try {
			final Object val = m_lookup.getValueAt(m_codeDecodeIdentifier
					+ _CODE);

			if (val != null) {
				rVal += val;
			}
		} catch (final Exception ex) {
			// ignore
		}

		return (rVal);
	}

	/**
	 * 
	 * @param key
	 *            String
	 * @return The current code for the object, "" if not set.
	 */
	public String getValue(String key) {
		String rVal = "";

		try {
			final Object val = m_lookup.getValueAt(key);

			if (val != null) {
				rVal += val;
			}
		} catch (final Exception ex) {
			// ignore
		}

		return (rVal);
	}

	/**
	 * Method getLookup.
	 * 
	 * @return Lookup
	 */
	protected Lookup getLookup() {
		return m_lookup;
	}

	/**
	 * Method getDisplayName.
	 * 
	 * @return String
	 */
	public String getDisplayName() {
		String rVal = "";

		try {
			final Object val = m_lookup.getValueAt(m_codeDecodeIdentifier
					+ _DISPLAY_NAME);

			if (val != null) {
				rVal += val;
			}
		} catch (final Exception ex) {
			// ignore
		}

		return (rVal);
	}

	/**
	 * Method equalsCode.
	 * 
	 * @param code
	 *            String
	 * @return boolean
	 */
	public boolean equalsCode(String code) {
		boolean equals = false;

		if (getCode().equalsIgnoreCase(code)) {
			equals = true;
		}

		return equals;
	}

	/**
	 * Method equals. Use Default Implementations for these 3 methods at the
	 * moment
	 * 
	 * @param objectToCompare
	 *            Object
	 * @return boolean
	 * @see java.util.Comparator#equals(Object)
	 */
	public boolean equals(Object objectToCompare) {

		if (this == objectToCompare) {
			return true;
		}

		if (objectToCompare == null) {
			return false;
		}
		if (objectToCompare instanceof Decode) {
			if (null == this.getCode())
				return false;
			if (this.getCode().equals(((Decode) objectToCompare).getCode())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method hashCode.
	 * 
	 * @return int
	 */
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + m_codeDecodeIdentifier.hashCode();
		hash = hash * 31
				+ (m_codeDecodeType == null ? 0 : m_codeDecodeType.hashCode());
		return hash;
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws java.lang.CloneNotSupportedException
	 */
	public Object clone() throws java.lang.CloneNotSupportedException {
		return (super.clone());
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		/*
		 * in cases where the value is not valid, toString will return the
		 * invalid value. (getCode still returns "" if the value is not valid.)
		 */

		if (isValid()) {
			return (getDisplayName());
		} else {
			if ((null != m_badValue) && (null != m_badValue.toString())) {
				return m_badValue.toString();
			} else {
				return "";
			}
		}
	}

	/**
	 * Method getCodesDecodes.
	 * 
	 * @return Vector<Decode>
	 * @throws ValueTypeException
	 */
	public Vector<Decode> getCodesDecodes() throws ValueTypeException {
		Vector<Decode> decodes = new Vector<Decode>();

		try {
			final int columns = getLookup().getColumnCount();
			final int rows = getLookup().getRowCount();
			String columnName = null;
			Decode newDecode = null;

			for (int i = 0; i < columns; i++) {
				columnName = getLookup().getColumnName(i);

				if ((m_codeDecodeIdentifier + _CODE).equals(columnName)) {
					for (int y = 0; y < rows; y++) {
						final Class<?> c = Class.forName(this.getClass()
								.getName());

						// construct a new business object
						newDecode = (Decode) c.newInstance();
						newDecode.m_lookup = (Lookup) getLookup().clone();

						newDecode.setValue(getLookup().getValueAt(y, i));
						decodes.add(newDecode);
					}
				}
			}
		} catch (final Exception e) {
			throw new ValueTypeException("Error getting decodes "
					+ this.getClass().getName() + " error message "
					+ e.getMessage());
		}

		return decodes;
	}

	/**
	 * Method setValue.
	 * 
	 * @param value
	 *            Object
	 */
	public void setValue(Object value) {
		if (value instanceof Decode) {
			setCode(((Decode) value).getCode());
		} else if (value instanceof Aspect) {
			setCode(value);
		} else {

			try {
				setCode((String) JavaTypeTranslator
						.convert(String.class, value));

			} catch (final Exception ex) {

			}
		}
	}

	//
	/**
	 * Method convertToUppercase. override this method for value types that need
	 * to distinguish upper case from lowercase
	 * 
	 * @return boolean
	 */
	protected boolean convertToUppercase() {
		return true;
	}

	/**
	 * Method setCode.
	 * 
	 * @param code
	 *            String
	 */
	private void setCode(String code) {
		try {
			String codeToLookup = null;

			if (convertToUppercase()) {
				codeToLookup = code.toUpperCase();
			} else {
				codeToLookup = code;
			}

			if (!m_lookup.setPos(codeToLookup, m_codeDecodeIdentifier + _CODE)) {
				m_badValue = code;
			}
		} catch (Exception ex) {
			m_badValue = code;
		}
	}

	/**
	 * Method setCode.
	 * 
	 * @param code
	 *            Object
	 */
	private void setCode(Object code) {
		try {

			if (!(m_lookup.setPos(code, m_codeDecodeIdentifier + _CODE))) {
				m_badValue = code;
			}
		} catch (final Exception ex) {

			m_badValue = code;
		}
	}

	public void setDefaultCode() {
		try {

			if (!m_lookup.setDefaultPos(m_codeDecodeIdentifier + _CODE)) {
				m_badValue = null;
			}
		} catch (Exception ex) {
			m_badValue = null;
		}
	}

	/**
	 * Method setDisplayName.
	 * 
	 * @param displayName
	 *            String
	 */
	public void setDisplayName(String displayName) {
		try {
			String displayNameToLookup = null;

			if (convertToUppercase()) {
				displayNameToLookup = displayName.toUpperCase();
			} else {
				displayNameToLookup = displayName;
			}

			if (!(m_lookup.setPos(displayNameToLookup, m_codeDecodeIdentifier
					+ _DISPLAY_NAME))) {
				/*
				 * save the original code to return as part of an error message
				 * + distinguish bad value from empty
				 */
				m_badValue = displayName;
			}
		} catch (Exception ex) {

			m_badValue = displayName;
		}
	}

	/**
	 * Method compareTo. primarily by name, secondarily by value; null-safe;
	 * case-insensitive
	 * 
	 * @param other
	 *            Decode
	 * @return int
	 */
	public int compareTo(final Decode other) {
		return CoreUtils.nullSafeComparator(this.getDisplayName(),
				other.getDisplayName());
	}

	/**
	 * Method compare.
	 * 
	 * @param o1
	 *            Decode
	 * @param o2
	 *            Decode
	 * @return int
	 */
	public int compare(Decode o1, Decode o2) {
		int returnVal = CoreUtils.nullSafeComparator(o1.getDisplayName(),
				o2.getDisplayName());
		if (m_ascending.equals(Boolean.FALSE)) {
			returnVal = returnVal * -1;
		}
		return returnVal;
	}
}
