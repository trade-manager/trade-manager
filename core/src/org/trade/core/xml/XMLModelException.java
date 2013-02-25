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
package org.trade.core.xml;

import org.trade.core.exception.ExceptionMessage;
import org.trade.core.exception.NestingException;

public class XMLModelException extends NestingException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3222050041414383567L;

	public XMLModelException() {
		super();
	}

	/**
	 * Constructor allowing a reference to another exception to be embedded.
	 * 
	 * @param t
	 *            The <code>Throwable</code> to be nested.
	 */
	public XMLModelException(Throwable t) {
		super(t, t.getMessage());
	}

	/**
	 * Constructor that allows the user to set the exception message.
	 * 
	 * @param message
	 *            The desired message text.
	 */
	public XMLModelException(String message) {
		super(message);
	}

	public XMLModelException(ExceptionMessage exceptionMessage,
			String gruesomeDetails) {
		super(exceptionMessage, gruesomeDetails);
	}

	public XMLModelException(ExceptionMessage exceptionMessage,
			String gruesomeDetails, Throwable t) {
		super(exceptionMessage, gruesomeDetails, t);
	}

	public XMLModelException(ExceptionMessage exceptionMessage) {
		super(exceptionMessage);
	}

	public XMLModelException(ExceptionMessage exceptionMessage, Throwable t) {
		super(exceptionMessage, t);
	}
}