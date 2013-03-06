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

import java.util.Vector;

import org.trade.core.valuetype.DAODecode;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.persistent.dao.Portfolio;

/**
 */
public class DAOGroup extends DAODecode {

	private static final long serialVersionUID = -5381026427696898592L;
	public static final String DECODE = "GROUP_DATA";
	public static final String _TABLE = "_TABLE";
	public static final String _TABLE_ID = "_TABLE_ID";
	public static final String _COLUMN = "_COLUMN";

	public DAOGroup() {
		super(DECODE, true);
	}

	/**
	 * Method getCodesDecodes.
	 * 
	 * @return Vector<Decode>
	 * @throws ValueTypeException
	 */

	public Vector<Decode> getCodesDecodes() throws ValueTypeException {
		final Vector<Decode> decodes = new Vector<Decode>();
		final Vector<Decode> decodesAll = super.getCodesDecodes();
		for (final Decode decode : decodesAll) {
			final Portfolio portfolio = (Portfolio) decode.getObject();
			if (null != portfolio.getAllocationMethod()) {
				Integer value = null;
				try {
					value = Integer.parseInt(portfolio.getAllocationMethod());
				} catch (NumberFormatException ex) {
					// Do nothing
				}
				if (null == value) {
					decodes.add(decode);
				}
			} else {
				if (Decode.NONE.equals(decode.getDisplayName())) {
					decodes.add(decode);
				}
			}
		}
		return decodes;
	}

	/**
	 * Method newInstance.
	 * 
	 * @param value
	 *            String
	 * @return DAOTradeAccount
	 */
	public static DAOGroup newInstance(String displayName) {
		final DAOGroup returnInstance = new DAOGroup();
		returnInstance.setDisplayName(displayName);
		return returnInstance;
	}

	/**
	 * Method newInstance.
	 * 
	 * @return DAOGroup
	 */
	public static DAOGroup newInstance() {
		final DAOGroup returnInstance = new DAOGroup();
		returnInstance.setDefaultCode();
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