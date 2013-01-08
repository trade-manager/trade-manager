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
package org.trade.core.exception;

/**
 * ExceptionCode is used as the key for retrieving an exception message.
 * 
 * Objects of this type are immutable (cannot be altered).
 * 
 * @author Simon Allen
 */
public class ExceptionCode implements java.io.Serializable {
	// WARNING: Do not add setters to this class because it is IMMUTABLE.
	// doing so will break code (e.g. ExceptionMessage which depend upon this
	// class not changing).

	// ----- Constants -----//

	/**
	 * 
	 */
	private static final long serialVersionUID = 1429333155399564179L;

	private static final String FIELD_SEQUENCE_SEPARATOR = "_";

	// ----- Private attributes -----//

	private String m_code = null;

	private String m_fieldRef = null;

	// ----- Constructors and public methods -----//

	/**
	 * Constructor for ExceptionCode.
	 * @param code String
	 */
	public ExceptionCode(String code) {
		m_code = code;
	}

	/**
	 * Constructor for ExceptionCode.
	 * @param code String
	 * @param fieldRef String
	 */
	public ExceptionCode(String code, String fieldRef) {
		m_code = code;
		m_fieldRef = fieldRef;
	}

	/**
	 * This can be used to generate a new ExceptionCode object where the field
	 * reference has the specified sequence number appended to it. It may be
	 * used when repeating groups of data are being validated.
	 * @param sequence int
	 * @return ExceptionCode
	 */
	public ExceptionCode createSequencedCode(int sequence) {
		ExceptionCode newExceptionCode;

		if (null == m_fieldRef) {
			newExceptionCode = this; // Okay because this class is immutable.
		} else {
			newExceptionCode = new ExceptionCode(m_code, m_fieldRef
					+ FIELD_SEQUENCE_SEPARATOR + sequence);
		}

		return newExceptionCode;
	}

	/**
	 * Method getCode.
	 * @return String
	 */
	public String getCode() {
		return m_code;
	}

	/**
	 * Method getFieldReference.
	 * @return String
	 */
	public String getFieldReference() {
		return m_fieldRef;
	}

	/**
	 * Method equals.
	 * @param objectToCompare Object
	 * @return boolean
	 */
	public boolean equals(Object objectToCompare) {

		if (this == objectToCompare) {
			return true;
		}
		if (objectToCompare == null) {
			return false;
		}
		if (!(objectToCompare instanceof ExceptionCode)) {
			return false;
		}
		boolean equal = false;

		ExceptionCode otherExceptionCode = (ExceptionCode) objectToCompare;

		boolean codeMatches = false;
		boolean fieldMatches = false;

		if (null == m_code) {
			codeMatches = (null == otherExceptionCode.m_code);
		} else {
			codeMatches = (m_code.equals(otherExceptionCode.m_code));
		}

		if (null == m_fieldRef) {
			fieldMatches = (null == otherExceptionCode.m_fieldRef);
		} else {
			fieldMatches = (m_fieldRef.equals(otherExceptionCode.m_fieldRef));
		}

		if (codeMatches && fieldMatches) {
			equal = true;
		}

		return equal;
	}
	
	/**
	 * Method hashCode.
	 * @return int
	 */
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + m_code.hashCode();
		hash = hash
				* 31
				+ (m_fieldRef == null ? 0 : m_fieldRef.hashCode());
		return hash;
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return m_code;
	}
}
