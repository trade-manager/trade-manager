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
package org.trade.ui.base;

import org.trade.core.valuetype.Decode;

/**
 * Example implementation of how to subclass the CodeDecodeValueType Object this
 * object represents the State codes and Descriptions in the US.
 * 
 * @version $Id: BasePropertyCodes.java,v 1.15 2002/01/22 22:48:21 simon Exp $
 * @author Simon Allen
 */
public class BaseUIPropertyCodes extends Decode {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2178313262496336078L;

	public final static String BASE_UI_WIDGET_PROP = "BASE_UI_WIDGET_PROP";
	public final static String BASE_UI_WIDGET = "BASE_UI_WIDGET";
	public final static String BASE_UI_WIDGET_TOOL_TIP = "BASE_UI_WIDGET_TOOL_TIP";
	public final static String BASE_UI_WIDGET_ENABLED = "BASE_UI_WIDGET_ENABLED";
	public final static String BASE_UI_WIDGET_MNEMONIC = "BASE_UI_WIDGET_MNEMONIC";
	public final static String BASE_UI_WIDGET_IMAGE = "BASE_UI_WIDGET_IMAGE";
	public final static String BASE_UI_WIDGET_METHOD = "BASE_UI_WIDGET_METHOD";

	public final static String ABOUT = "ABOUT";
	public final static String CALCULATE = "CALCULATE";
	public final static String CANCEL = "CANCEL";
	public final static String CASCADE = "CASCADE";
	public final static String CASCADE_ALL = "CASCADE_ALL";
	public final static String CLEAR = "CLEAR";
	public final static String CLOSE = "CLOSE";
	public final static String CLOSE_ALL = "CLOSE_ALL";
	public final static String CLOSE_FILE = "CLOSE_FILE";
	public final static String CONTENTS = "CONTENTS";
	public final static String COPY = "COPY";
	public final static String COMMIT = "COMMIT";
	public final static String CUT = "CUT";
	public final static String CONNECT = "CONNECT";
	public final static String DELETE = "DELETE";
	public final static String DISCONNECT = "DISCONNECT";
	public final static String DISCLAIMER = "DISCLAIMER";
	public final static String EXECUTE = "EXECUTE";
	public final static String EXECUTE_STATEMENT = "EXECUTE_STATEMENT";
	public final static String EXIT = "EXIT";
	public final static String FIND = "FIND";
	public final static String FETCH = "FETCH";
	public final static String HELP = "HELP";
	public final static String INSERT = "INSERT";
	public final static String NEW = "NEW";
	public final static String NEXT = "NEXT";
	public final static String OPEN_FILE = "OPEN_FILE";
	public final static String PASTE = "PASTE";
	public final static String PREV = "PREV";
	public final static String PRINT = "PRINT";
	public final static String PRINT_PREVIEW = "PRINT_PREVIEW";
	public final static String PRINT_OPTIONS = "PRINT_OPTIONS";
	public final static String REDO = "REDO";
	public final static String REPLACE = "REPLACE";
	public final static String REFRESH = "REFRESH";
	public final static String RESULTS = "RESULTS";
	public final static String RETRIEVE = "RETRIEVE";
	public final static String SAVE = "SAVE";
	public final static String SAVE_AS = "SAVE_AS";
	public final static String SEARCH = "SEARCH";
	public final static String TABLE_LIST = "TABLE_LIST";
	public final static String TILE_ALL = "TILE_ALL";
	public final static String UNDO = "UNDO";
	public final static String VALID = "VALID";
	public final static String VALID_ALL = "VALID_ALL";
	public final static String PROPERTIES = "PROPERTIES";
	public final static String CLEAR_ERROR = "CLEAR_ERROR";
	public final static String RUN = "RUN";
	public final static String DATA = "DATA";
	public final static String TEST = "TEST";
	public final static String TRANSFER = "TRANSFER";
	public final static String REMOVE = "REMOVE";

	/**
	 * Default Constructor
	 */
	public BaseUIPropertyCodes() {
		super(BASE_UI_WIDGET_PROP, BASE_UI_WIDGET, false);
	}

	/**
	 * Constructor for BaseUIPropertyCodes.
	 * 
	 * @param propertyType
	 *            String
	 * @param propertyCode
	 *            String
	 */
	public BaseUIPropertyCodes(String propertyType, String propertyCode) {
		super(propertyType, propertyCode, false);
	}

	/**
	 * Method isEnabled.
	 * 
	 * @return boolean
	 */
	public boolean isEnabled() {
		boolean enabled = false;

		if (getValue(BASE_UI_WIDGET_ENABLED).equalsIgnoreCase("true")) {
			enabled = true;
		}

		return enabled;
	}

	/**
	 * Method getToolTip.
	 * 
	 * @return String
	 */
	public String getToolTip() {
		return getValue(BASE_UI_WIDGET_TOOL_TIP);
	}

	/**
	 * Method getImage.
	 * 
	 * @return String
	 */
	public String getImage() {
		return getValue(BASE_UI_WIDGET_IMAGE);
	}

	/**
	 * Method getMethod.
	 * 
	 * @return String
	 */
	public String getMethod() {
		return getValue(BASE_UI_WIDGET_METHOD);
	}

	/**
	 * Method getMnemonic.
	 * 
	 * @return int
	 */
	public int getMnemonic() {
		int returnValue = 0;

		if ((null != getValue(BASE_UI_WIDGET_MNEMONIC)) && (getValue(BASE_UI_WIDGET_MNEMONIC).length() > 0)) {
			returnValue = getValue(BASE_UI_WIDGET_MNEMONIC).charAt(0);
		}

		return returnValue;
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
	 * @return BaseUIPropertyCodes
	 * @exception *
	 * 				@see
	 */
	public static BaseUIPropertyCodes newInstance(String code) {
		BaseUIPropertyCodes returnInstance = null;
		returnInstance = new BaseUIPropertyCodes();
		returnInstance.setValue(code);
		return returnInstance;
	}
}
