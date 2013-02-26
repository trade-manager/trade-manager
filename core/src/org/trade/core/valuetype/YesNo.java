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

/**
 */
public class YesNo extends BaseDecode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1288975993214301679L;

	public static final String NO = "false";
	public static final String YES = "true";
	public static final String DECODE = "YES_NO";

	public YesNo() {
		super(DECODE, false);
	}

	/**
	 * is the YES
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return boolean
	 * @exception * @see
	 */

	public boolean isYes() {

		return YES.equals(getCode());
	}

	/**
	 * is the NO
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return boolean
	 * @exception * @see
	 */

	public boolean isNo() {

		return NO.equals(getCode());
	}

	/**
	 * Create a new instance of this object
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param code
	 *            String
	 * @return YesNo
	 * @exception * @see
	 */

	public static YesNo newInstance(String code) {
		final YesNo returnInstance = new YesNo();
		returnInstance.setValue(code);
		return returnInstance;
	}

	/**
	 * Method newInstance.
	 * 
	 * @param code
	 *            Boolean
	 * @return YesNo
	 */
	public static YesNo newInstance(Boolean code) {
		final YesNo returnInstance = new YesNo();
		returnInstance.setValue(code.toString());
		return returnInstance;
	}

	/**
	 * Method convertToUppercase.
	 * 
	 * @return boolean
	 */
	protected boolean convertToUppercase() {
		return false;
	}

}