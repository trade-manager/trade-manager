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
package org.trade.broker.client;

import org.trade.core.util.CoreUtils;
import org.trade.core.valuetype.Money;

/**
 */
public class OrderState {

	public String m_status;
	public String m_initMargin;
	public String m_maintMargin;
	public String m_equityWithLoan;
	public double m_commission;
	public double m_minCommission;
	public double m_maxCommission;
	public String m_commissionCurrency;
	public String m_warningText;

	public OrderState() {
		this(null, null, null, null, 0.0, 0.0, 0.0, null, null);
	}

	/**
	 * Constructor for OrderState.
	 * 
	 * @param status
	 *            String
	 * @param initMargin
	 *            String
	 * @param maintMargin
	 *            String
	 * @param equityWithLoan
	 *            String
	 * @param commission
	 *            double
	 * @param minCommission
	 *            double
	 * @param maxCommission
	 *            double
	 * @param commissionCurrency
	 *            String
	 * @param warningText
	 *            String
	 */
	public OrderState(String status, String initMargin, String maintMargin, String equityWithLoan, double commission,
			double minCommission, double maxCommission, String commissionCurrency, String warningText) {

		m_initMargin = initMargin;
		m_maintMargin = maintMargin;
		m_equityWithLoan = equityWithLoan;
		m_commission = commission;
		m_minCommission = minCommission;
		m_maxCommission = maxCommission;
		m_commissionCurrency = commissionCurrency;
		m_warningText = warningText;
	}

	/**
	 * Method equals.
	 * 
	 * @param objectToCompare
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object objectToCompare) {

		if (this == objectToCompare) {
			return true;
		}

		if (objectToCompare == null) {
			return false;
		}
		if (!(objectToCompare instanceof OrderState)) {
			return false;
		}
		OrderState state = (OrderState) objectToCompare;

		if (CoreUtils.nullSafeComparator(new Money(m_commission), new Money(state.m_commission)) != 0
				|| (CoreUtils.nullSafeComparator(new Money(m_minCommission), new Money(state.m_minCommission)) != 0)
				|| (CoreUtils.nullSafeComparator(new Money(m_maxCommission), new Money(state.m_maxCommission)) != 0)) {
			return false;
		}

		if ((CoreUtils.nullSafeComparator(m_status, state.m_status) != 0)
				|| (CoreUtils.nullSafeComparator(m_initMargin, state.m_initMargin) != 0)
				|| (CoreUtils.nullSafeComparator(m_maintMargin, state.m_maintMargin) != 0)
				|| (CoreUtils.nullSafeComparator(m_equityWithLoan, state.m_equityWithLoan) != 0)
				|| (CoreUtils.nullSafeComparator(m_commissionCurrency, state.m_commissionCurrency) != 0)) {
			return false;
		}
		return true;
	}
}
