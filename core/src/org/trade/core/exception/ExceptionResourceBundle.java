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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.PropertyResourceBundle;

/**
 * ExceptionResourceBundle handles storing messages for each Exception.
 * 
 * @author Simon Allen
 */
public class ExceptionResourceBundle extends PropertyResourceBundle implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3004803155454107541L;

	/**
	 * Takes an InputStream to the properties file where the Exception messages
	 * are stored
	 * 
	 * @param resourceStream
	 *            the input stream to the resource
	 * 
	 * @throws IOException
	 * @exception java.io.IOException
	 *                : thrown when the input stream doesn't find the resource
	 */
	public ExceptionResourceBundle(InputStream resourceStream) throws IOException {
		super(resourceStream);
	}

	/**
	 * Returns the message for the given <code>code</code>
	 * 
	 * @param code
	 *            exception code
	 * 
	 * @return the exception message
	 */
	public String getMessage(ExceptionCode code) {
		String message = getString(code.getCode());

		// return the package default message if no class message is available
		if (message == null) {
			message = getMessage();
		}

		return message;
	}

	/**
	 * Returns the default exception message for the package
	 * 
	 * 
	 * @return the exception message
	 */
	public String getMessage() {
		return getString(DEFAULT);
	}

	// constants
	private final static String DEFAULT = "default";

} // end ExceptionResourceBundle
