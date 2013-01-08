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
package org.trade.core.util;

import org.trade.core.exception.NestingException;

/**
 * This class servers as a general purpose exception for the identity service
 * component.
 * 
 * @author Simon Allen
 */
public class IdentityServiceException extends NestingException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5500569043989306886L;

	public IdentityServiceException() {
		super();
	}

	/**
	 * Constructor for IdentityServiceException.
	 * 
	 * @param t
	 *            Throwable
	 */
	public IdentityServiceException(Throwable t) {
		super(t);
	}

	/**
	 * Constructor for IdentityServiceException.
	 * 
	 * @param message
	 *            String
	 */
	public IdentityServiceException(String message) {
		super(message);
	}
}
