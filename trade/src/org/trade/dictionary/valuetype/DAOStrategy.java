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
import org.trade.persistent.dao.Strategy;

/**
 */
public class DAOStrategy extends DAODecode {

	private static final long serialVersionUID = -5381026427696898592L;
	public static final String DECODE = "STRATEGY";
	public static final String _TABLE = "_TABLE";
	public static final String _TABLE_ID = "_TABLE_ID";
	public static final String _COLUMN = "_COLUMN";

	public DAOStrategy() {
		super(DECODE);
	}

	/**
	 * Method getCodesDecodes.
	 * 
	 * @return Vector<Decode>
	 * @throws ValueTypeException
	 */
	@Override
	public Vector<Decode> getCodesDecodes() throws ValueTypeException {
		final Vector<Decode> decodes = new Vector<Decode>();

		final Vector<Decode> decodesAll = super.getCodesDecodes();
		for (final Decode decode : decodesAll) {
			final Strategy strategy = (Strategy) decode.getObject();
			boolean isMgr = false;
			if (!strategy.hasStrategyManager()) {
				for (final Decode mgrdecode : decodesAll) {
					final Strategy strategyMgr = (Strategy) mgrdecode
							.getObject();
					if (strategyMgr.hasStrategyManager()) {
						if (strategyMgr.getStrategyManager().getIdStrategy()
								.equals(strategy.getIdStrategy())) {
							isMgr = true;
							break;
						}
					}
				}
			}
			if (!isMgr) {
				decodes.add(decode);
			}
		}
		return decodes;
	}

	/**
	 * Method newInstance.
	 * 
	 * @param displayName
	 *            String
	 * @return DAOStrategy
	 */
	public static DAOStrategy newInstance(String displayName) {
		final DAOStrategy returnInstance = new DAOStrategy();
		returnInstance.setDisplayName(displayName);
		return returnInstance;
	}

	/**
	 * Method newInstance.
	 * 
	 * @return DAOStrategy
	 */
	public static DAOStrategy newInstance() {
		final DAOStrategy returnInstance = new DAOStrategy();
		returnInstance.setDefaultCode();
		return returnInstance;
	}
}