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
package org.trade.dictionary.valuetype;

import org.trade.core.valuetype.Decode;

/**
 */
public class OrderType extends Decode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5381026427696898592L;
	public static final String DECODE = "ORDER_TYPE";
	public static final String LMT = "LMT";
	public static final String STP = "STP";
	public static final String STPLMT = "STPLMT";
	public static final String MKT = "MKT";
	public static final String TRAIL = "TRAIL";
	public static final String TRAILLIMIT = "TRAILLIMIT ";

	public OrderType() {
		super(DECODE);
	}

	/**
	 * Method newInstance.
	 * 
	 * @param value
	 *            String
	 * @return OrderType
	 */
	public static OrderType newInstance(String value) {
		final OrderType returnInstance = new OrderType();
		returnInstance.setValue(value);
		return returnInstance;
	}

	/**
	 * Method newInstance.
	 * 
	 * @return OrderType
	 */
	public static OrderType newInstance() {
		final OrderType returnInstance = new OrderType();
		returnInstance.setDefaultCode();
		return returnInstance;
	}
}