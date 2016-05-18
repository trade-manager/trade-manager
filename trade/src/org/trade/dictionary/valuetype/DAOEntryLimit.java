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

import java.util.Enumeration;
import java.util.Vector;

import org.trade.core.valuetype.DAODecode;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.persistent.dao.Entrylimit;

/**
 */
public class DAOEntryLimit extends DAODecode {

	private static final long serialVersionUID = -5381026427696898592L;
	public static final String DECODE = "ENTRY_LIMIT";
	public static final String _TABLE = "_TABLE";
	public static final String _TABLE_ID = "_TABLE_ID";
	public static final String _COLUMN = "_COLUMN";

	public DAOEntryLimit() {
		super(DECODE);
	}

	/**
	 * Method newInstance.
	 * 
	 * @return DAOEntryLimit
	 */
	public static DAOEntryLimit newInstance() {
		final DAOEntryLimit returnInstance = new DAOEntryLimit();
		returnInstance.setDefaultCode();
		return returnInstance;
	}

	/**
	 * Method getValue.
	 * 
	 * @param price
	 *            Money
	 * @return Entrylimit
	 */
	public Entrylimit getValue(Money price) {

		Vector<Decode> decodes;
		try {
			decodes = this.getCodesDecodes();
			final Enumeration<Decode> enumDAODecode = decodes.elements();
			while (enumDAODecode.hasMoreElements()) {
				final Decode decode = enumDAODecode.nextElement();
				final Entrylimit entryLimit = (Entrylimit) decode.getObject();
				if ((entryLimit.getStartPrice().subtract(price.getBigDecimalValue()).doubleValue() <= 0)
						&& (entryLimit.getEndPrice().subtract(price.getBigDecimalValue()).doubleValue() >= 0)) {
					return entryLimit;
				}
			}
		} catch (final ValueTypeException e) {
			/*
			 * Do nothing is no code just report to log.
			 */

		}
		return null;
	}
}