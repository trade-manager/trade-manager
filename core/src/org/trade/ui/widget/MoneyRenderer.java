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
package org.trade.ui.widget;

import java.text.NumberFormat;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.trade.core.valuetype.Money;

/**
 * 
 * @version $Id: MoneyRenderer.java,v 1.3 2002/01/24 01:16:08 simon Exp $
 * @author Simon Allen
 */
public class MoneyRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6325763792561257469L;
	private NumberFormat m_formater = null;

	public MoneyRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.RIGHT);
		m_formater = NumberFormat.getCurrencyInstance();
		m_formater.setMinimumFractionDigits(2);
	}

	/**
	 * Method setValue.
	 * 
	 * @param value
	 *            Object
	 */
	protected void setValue(Object value) {
		if (value == null) {
			setText("");
		} else {
			if (value instanceof Money) {

				if (null == ((Money) value).getBigDecimalValue()) {
					setText(value.toString());
				} else {
					setText(m_formater.format(((Money) value).getBigDecimalValue()));
				}

			} else {
				setText(value.toString());
			}
		}
	}
}
