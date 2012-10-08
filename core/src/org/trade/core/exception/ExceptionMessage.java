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
package org.trade.core.exception;

import java.util.StringTokenizer;

/**
 */
public class ExceptionMessage implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1095613543601005491L;

	// constants

	private final static String charsStartString = "contains invalid characters: [";

	private final static String charsEndString = "] -- check api specification";

	private final static String lengthStartString = "length is not to exceed ";

	// member variables
	private ExceptionCode m_code;

	private String m_message;

	/**
	 * Copy constructor.
	 * @param other ExceptionMessage
	 */
	public ExceptionMessage(ExceptionMessage other) {
		m_code = other.m_code;
		m_message = other.m_message;
	}

	/**
	 * Constructor allowing the exception code and message to be provided.
	 * 
	 * @param code
	 *            the code assigned to the new exception message
	 * @param message
	 *            the message (may contain context markup in the form of
	 *            #parameters#
	 */
	public ExceptionMessage(ExceptionCode code, String message) {
		m_code = code;
		m_message = message;
	}

	/**
	 * Constructor that copies an existing exception message but assigns the new
	 * message the provided exception code.
	 * 
	 * @param code
	 *            the code assigned to the new exception message
	 * @param message
	 *            the existing exception message from which to copy any existing
	 *            message content/context
	 */
	public ExceptionMessage(ExceptionCode code, ExceptionMessage message) {
		m_code = code;
		m_message = message.m_message;
	}

	/**
	
	 * @return The Exception Code associated with the message */
	public ExceptionCode getExceptionCode() {
		return m_code;
	}

	/**
	 * The exception context represents a named parameter within this message.
	 * 
	 * Rules:
	 * <p>
	 * 1. If two contexts are added with the same parameter name, the first
	 * context has precedence.<br>
	 * @param exceptionContext ExceptionContext
	 */
	public void addExceptionContext(ExceptionContext exceptionContext) {
		if (null != m_message) {
			StringTokenizer tokenizer = new StringTokenizer(m_message, "#");

			StringBuffer buf = new StringBuffer();

			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				String token = tokenizer.nextToken();

				if ((i % 2) == 0) // This is not a parameter.
				{
					// Append the token as is, because the token is part of the
					// message.
					buf.append(token);
				} else
				// We have a parameter.
				{
					// Validate the parameter name.
					if (exceptionContext.getParameterName().equals(token)) {
						buf.append(exceptionContext.getValue());
					} else {
						// We don't want to lose the unused parameter
						buf.append('#');
						buf.append(token);
						buf.append('#');
					}
				}
			}

			m_message = buf.toString();
		}
	}

	/**
	
	 * @return The message that describes the exception. */
	public String getMessage() {
		StringBuffer buf = new StringBuffer();

		// If there are any remaining parameters not substituted we will
		// remove them. Note we do not change the real message, so parameters
		// could still be added in the future.
		if (null != m_message) {
			StringTokenizer tokenizer = new StringTokenizer(m_message, "#");

			for (int i = 0; tokenizer.hasMoreTokens(); i++) {
				String token = tokenizer.nextToken();

				if ((i % 2) == 0) // This is not a parameter.
				{
					// Append the token as is, because the token is part of the
					// message.
					buf.append(token);
				} else {
					// This is a parameter so we ignore it.
				}
			}
		}

		return buf.toString();
	}

	/**
	 * this method allows the exception message translator to look up parameters
	 * in an exception message without using reflection or having this class
	 * implement dictionary
	 * 
	 * this currently only implements message - same as getMessage() description
	 * - everything in the message after the first colon
	 * 
	 * @param lookup
	 *            the string to use for the look up
	
	 * 
	 * @return Object the result of the look up, or null if nothing is found
	 *         (most likely a string) */
	// TODO: get rid of this
	public Object get(String lookup) {
		if ("description".equals(lookup)) {
			return getDescription();
		}
		if ("message".equals(lookup)) {
			return getMessage();
		}
		if ("max_length".equals(lookup)) {
			return getInvalidLength();
		}
		if ("invalid_chars".equals(lookup)) {
			return getInvalidChars();
		}
		if ("empty".equals(lookup)) {
			return getEmpty();
		}
		return null;
	}

	// TODO: get rid of this
	/**
	 * Method getEmpty.
	 * @return String
	 */
	private String getEmpty() {
		int emptyPos = getMessage().indexOf("Empty");
		if (-1 != emptyPos) {
			return "empty";
		}
		int manditoryPos = getMessage().indexOf("Mandatory"); // the internal
		// response uses
		// a capitol M
		if (-1 != manditoryPos) {
			return "empty";
		}
		return null;
	}

	// TODO: get rid of this
	/**
	 * Method getInvalidChars.
	 * @return String
	 */
	private String getInvalidChars() {
		int startLength = getMessage().indexOf(charsStartString);
		if (startLength < 0) {
			return null; // not found
		}
		startLength = startLength + charsStartString.length();
		int endLength = getMessage().indexOf(charsEndString);
		if (endLength < 0) {
			return null; // not found
		}
		if (startLength > endLength) {
			return null;
		}

		return getMessage().substring(startLength, endLength);
	}

	// TODO: get rid of this
	/**
	 * Method getInvalidLength.
	 * @return String
	 */
	private String getInvalidLength() {
		int startLength = getMessage().indexOf(lengthStartString);
		if (-1 == startLength) {
			return null; // not found
		}
		startLength = startLength + lengthStartString.length();

		return getMessage().substring(startLength);
	}

	// TODO: get rid of this
	/**
	 * Method getDescription.
	 * @return String
	 */
	private String getDescription() {
		// the description is everything in the message after the first colon
		int firstColon = getMessage().indexOf(":");
		return getMessage().substring(firstColon + 1).trim();
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
		if (!(objectToCompare instanceof ExceptionMessage)) {
			return false;
		}
		boolean equal = false;

		ExceptionMessage otherExceptionMessage = (ExceptionMessage) objectToCompare;

		boolean codeMatches = false;
		boolean messageMatches = false;

		if (null == m_code) {
			codeMatches = (null == otherExceptionMessage.m_code);
		} else {
			codeMatches = (m_code.equals(otherExceptionMessage.m_code));
		}

		if (null == m_message) {
			messageMatches = (null == otherExceptionMessage.m_message);
		} else {
			messageMatches = (m_message.equals(otherExceptionMessage.m_message));
		}

		if (codeMatches && messageMatches) {
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
				+ (m_message == null ? 0 : m_message.hashCode());
		return hash;
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return "code: [" + m_code + "] message: [" + m_message + "]";
	}
}
