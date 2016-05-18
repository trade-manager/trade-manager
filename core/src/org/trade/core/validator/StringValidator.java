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
package org.trade.core.validator;

import org.trade.core.exception.ExceptionMessage;
import org.trade.core.message.IMessageFactory;
import org.trade.core.message.MessageContextFactory;
import org.trade.core.message.MessageFactory;

/**
 */
public class StringValidator implements Validator {
	// Use these to indicate sets of valid characters
	public final static int NONE = 0;

	public final static int DIGITS = 1;

	public final static int SPACES = 2;

	public final static int LETTERS = 4;

	public final static int PUNCTUATION = 8;

	public final static int ANY = 16;

	public final static int ALPHANUMERIC = DIGITS + SPACES + LETTERS;

	public final static int ALPHA = LETTERS + SPACES;

	private IMessageFactory m_messageFactory;

	private int m_minLength;

	private int m_maxLength;

	private int m_permittedCharacterSet;

	private boolean m_isMandatory;

	private String m_additionalPermittedCharacters;

	/**
	 * Constructor for StringValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @param maxLength
	 *            int
	 * @param permittedCharacterSet
	 *            int
	 * @param additionalPermittedCharacters
	 *            String
	 * @param isMandatory
	 *            boolean
	 */
	public StringValidator(IMessageFactory messageFactory, int maxLength, int permittedCharacterSet,
			String additionalPermittedCharacters, boolean isMandatory) {
		m_messageFactory = messageFactory;
		m_maxLength = maxLength;
		m_permittedCharacterSet = permittedCharacterSet;
		m_isMandatory = isMandatory;
		m_additionalPermittedCharacters = additionalPermittedCharacters;
	}

	/**
	 * Constructor for StringValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @param minLength
	 *            int
	 * @param maxLength
	 *            int
	 * @param permittedCharacterSet
	 *            int
	 * @param additionalPermittedCharacters
	 *            String
	 * @param isMandatory
	 *            boolean
	 */
	public StringValidator(IMessageFactory messageFactory, int minLength, int maxLength, int permittedCharacterSet,
			String additionalPermittedCharacters, boolean isMandatory) {
		this(messageFactory, maxLength, permittedCharacterSet, additionalPermittedCharacters, isMandatory);
		m_minLength = minLength;
	}

	/**
	 * Constructor for StringValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @param minLength
	 *            int
	 * @param maxLength
	 *            int
	 * @param permittedCharacterSet
	 *            int
	 * @param isMandatory
	 *            boolean
	 */
	public StringValidator(IMessageFactory messageFactory, int minLength, int maxLength, int permittedCharacterSet,
			boolean isMandatory) {
		this(messageFactory, maxLength, permittedCharacterSet, null, isMandatory);
		m_minLength = minLength;
	}

	/**
	 * Constructor for StringValidator.
	 * 
	 * @param messageFactory
	 *            IMessageFactory
	 * @param maxLength
	 *            int
	 * @param permittedCharacterSet
	 *            int
	 * @param isMandatory
	 *            boolean
	 */
	public StringValidator(IMessageFactory messageFactory, int maxLength, int permittedCharacterSet,
			boolean isMandatory) {
		this(messageFactory, maxLength, permittedCharacterSet, null, isMandatory);
	}

	/**
	 * Method getMessageFactory.
	 * 
	 * @return IMessageFactory
	 */
	protected IMessageFactory getMessageFactory() {
		if (null == m_messageFactory) {
			m_messageFactory = MessageFactory.SYSTEM_ERROR;
		}

		return m_messageFactory;
	}

	/**
	 * Method isMandatory.
	 * 
	 * @return boolean
	 */
	public boolean isMandatory() {
		return (m_isMandatory);
	}

	/**
	 * Method isValid.
	 * 
	 * @param value
	 *            Object
	 * @param invalidValue
	 *            String
	 * @param expectedFormat
	 *            String
	 * @param receiver
	 *            ExceptionMessageListener
	 * @return boolean
	 * @see org.trade.core.validator.Validator#isValid(Object, String, String,
	 *      ExceptionMessageListener)
	 */
	public boolean isValid(Object value, String invalidValue, String expectedFormat,
			ExceptionMessageListener receiver) {
		if (null == receiver) {
			receiver = new ExceptionMessageListener() {
				public void addExceptionMessage(ExceptionMessage e) {
				}
			};
		}

		boolean valid = true;

		if (null == value) {
			value = "";
		}

		if (0 == ((String) value).length()) // Optional/mandatory check
		{
			if (m_isMandatory) {
				valid = false;
				receiver.addExceptionMessage(
						getMessageFactory().create(MessageContextFactory.MANDATORY_VALUE_NOT_PROVIDED.create()));
			}
		} else if (((String) value).length() > m_maxLength) // Max length check
		{
			valid = false;
			receiver.addExceptionMessage(getMessageFactory().create(MessageContextFactory.MAX_LENGTH_EXCEEDED
					.create(MessageContextFactory.MAX_LENGTH.create(new Integer(m_maxLength)))));
		} else if (((String) value).length() < m_minLength) // Min length check
		{
			valid = false;
			receiver.addExceptionMessage(getMessageFactory().create(MessageContextFactory.MIN_LENGTH_FAILED
					.create(MessageContextFactory.MIN_LENGTH.create(new Integer(m_minLength)))));
		} else
		// 0 < length < max length so check valid characters
		{
			String invalidCharacters = checkForInvalidCharacters(((String) value), m_permittedCharacterSet,
					m_additionalPermittedCharacters);

			if (null != invalidCharacters) {
				valid = false;
				receiver.addExceptionMessage(
						getMessageFactory().create(MessageContextFactory.CONTAINS_INVALID_CHARACTERS
								.create(MessageContextFactory.INVALID_CHARACTERS.create(invalidCharacters))));
			}
		}

		return valid;
	}

	/**
	 * Checks a string for invalid characters. Any invalid characters will be
	 * returned.
	 * 
	 * 
	 * @param toStrip
	 *            String
	 * @param whatToKeep
	 *            int
	 * @param whatElseToKeep
	 *            String
	 * @return String A list of each invalid character; null if all characters
	 *         are permitted.
	 */
	public static String checkForInvalidCharacters(String toStrip, int whatToKeep, String whatElseToKeep) {
		if ((null == toStrip) || (toStrip.length() == 0)) {
			return null;
		}

		String invalidChars = "";

		int toStripLength = toStrip.length();
		for (int i = 0; i < toStripLength; i++) {
			if ((!isValidChar(toStrip.charAt(i), whatToKeep)) && (!isValidChar(toStrip.charAt(i), whatElseToKeep))) {
				if (invalidChars.indexOf(toStrip.charAt(i)) == -1) {
					invalidChars = invalidChars + toStrip.substring(i, i + 1);
				}
			}
		}

		if ("".equals(invalidChars)) {
			return null;
		} else {
			return invalidChars;
		}
	}

	/**
	 * Method isValidChar.
	 * 
	 * @param toCheck
	 *            char
	 * @param whatToKeep
	 *            int
	 * @return boolean
	 */
	protected static boolean isValidChar(char toCheck, int whatToKeep) {
		if ((whatToKeep & ANY) > 0) {
			return true;
		}

		boolean spaces = ((whatToKeep & SPACES) > 0);
		if (toCheck == ' ') {
			return spaces;
		}

		boolean digits = ((whatToKeep & DIGITS) > 0);
		if ((toCheck >= '0') && (toCheck <= '9')) {
			return digits;
		}

		boolean letters = ((whatToKeep & LETTERS) > 0);
		if (((toCheck >= 'a') && (toCheck <= 'z')) || ((toCheck >= 'A') && (toCheck <= 'Z'))) {
			return letters;
		}

		boolean punctuation = ((whatToKeep & PUNCTUATION) > 0);
		if (punctuation) {
			return isValidChar(toCheck, "`~!@#$%^&*()-_=+\\|]}[{;:,<.>/?\"\'");
		} else {
			return false;
		}
	}

	/**
	 * Method isValidChar.
	 * 
	 * @param toCheck
	 *            char
	 * @param whatToKeep
	 *            String
	 * @return boolean
	 */
	protected static boolean isValidChar(char toCheck, String whatToKeep) {
		if (null != whatToKeep) {
			int whatToKeepLength = whatToKeep.length();
			for (int i = 0; i < whatToKeepLength; i++) {
				if (whatToKeep.charAt(i) == toCheck) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Method stripSpecificChars.
	 * 
	 * @param toStrip
	 *            String
	 * @param whatToStrip
	 *            String
	 * @return String
	 */
	public static String stripSpecificChars(String toStrip, String whatToStrip) {
		if ((null == toStrip) || (toStrip.length() == 0)) {
			return toStrip;
		}

		StringBuffer bufferedResult = new StringBuffer("");

		int toStripLength = toStrip.length();
		for (int i = 0; i < toStripLength; i++) {
			// if the character is valid where the valid chars come from
			// toStrip,
			// then the character is of of the chars the need to be stripped
			if (!isValidChar(toStrip.charAt(i), whatToStrip)) {
				bufferedResult.append(toStrip.substring(i, i + 1));
			}
		}

		return bufferedResult.toString();
	}
}
