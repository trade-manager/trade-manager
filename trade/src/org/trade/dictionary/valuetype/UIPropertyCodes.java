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

import org.trade.ui.base.BaseUIPropertyCodes;

/**
 * Example implementation of how to subclass the CodeDecodeValueType Object this
 * object represents the State codes and Descriptions in the US.
 * 
 * @version $Id: BaseUIPropertyCodes.java,v 1.15 2002/01/22 22:48:21 simon Exp $
 * @author Simon Allen
 */
public class UIPropertyCodes extends BaseUIPropertyCodes {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2178313262496336078L;

	public final static String UI_WIDGET_PROP = "UI_WIDGET_PROP";
	public final static String UI_WIDGET = "UI_WIDGET";
	public final static String UI_WIDGET_TOOL_TIP = "UI_WIDGET_TOOL_TIP";
	public final static String UI_WIDGET_ENABLED = "UI_WIDGET_ENABLED";
	public final static String UI_WIDGET_MNEMONIC = "UI_WIDGET_MNEMONIC";
	public final static String UI_WIDGET_IMAGE = "UI_WIDGET_IMAGE";
	public final static String UI_WIDGET_METHOD = "UI_WIDGET_METHOD";

	public final static String COMPILE = "COMPILE";
	public final static String REASSIGN = "REASSIGN";
	public final static String STRATEGY_PARMS = "STRATEGY_PARMS";

	/**
	 * Default Constructor
	 */
	public UIPropertyCodes() {
		super(UI_WIDGET_PROP, UI_WIDGET);
	}

	/**
	 * Method isEnabled.
	 * 
	 * @return boolean
	 */
	public boolean isEnabled() {
		boolean enabled = false;

		if (getValue(UI_WIDGET_ENABLED).equalsIgnoreCase("true")) {
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
		return getValue(UI_WIDGET_TOOL_TIP);
	}

	/**
	 * Method getImage.
	 * 
	 * @return String
	 */
	public String getImage() {
		return getValue(UI_WIDGET_IMAGE);
	}

	/**
	 * Method getMethod.
	 * 
	 * @return String
	 */
	public String getMethod() {
		return getValue(UI_WIDGET_METHOD);
	}

	/**
	 * Method getMnemonic.
	 * 
	 * @return int
	 */
	public int getMnemonic() {
		int returnValue = 0;

		if ((null != getValue(UI_WIDGET_MNEMONIC)) && (getValue(UI_WIDGET_MNEMONIC).length() > 0)) {
			returnValue = getValue(UI_WIDGET_MNEMONIC).charAt(0);
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
	 * @return UIPropertyCodes
	 * @exception *
	 * 				@see
	 */
	public static UIPropertyCodes newInstance(String code) {
		UIPropertyCodes returnInstance = null;
		returnInstance = new UIPropertyCodes();
		returnInstance.setValue(code);
		return returnInstance;
	}
}
