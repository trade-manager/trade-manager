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

import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.trade.core.valuetype.Percent;

/**
 * 
 * @version $Id: PercentRenderer.java,v 1.3 2002/01/24 01:16:08 simon Exp $
 * @author Simon Allen
 */
public class PercentRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1531259426058273458L;
	private NumberFormat m_formater = null;

	public PercentRenderer() {
		super();
		setHorizontalAlignment(SwingConstants.RIGHT);
		m_formater = NumberFormat.getPercentInstance();
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
			if (value instanceof Percent) {
				if (null == ((Percent) value).getBigDecimalValue()) {
					setText(value.toString());
				} else {
					setText(m_formater.format(((Percent) value).getBigDecimalValue()));
				}
			} else {
				setText(value.toString());
			}
		}
	}

	/**
	 * Method getTableCellRendererComponent.
	 * 
	 * @param table
	 *            JTable
	 * @param object
	 *            Object
	 * @param isSelected
	 *            boolean
	 * @param hasFocus
	 *            boolean
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @return Component
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable,
	 *      Object, boolean, boolean, int, int)
	 */
	public synchronized Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected,
			boolean hasFocus, int row, int column) {

		synchronized (object) {
			setBackground(null);
			super.getTableCellRendererComponent(table, object, isSelected, hasFocus, row, column);
			Percent percent = (Percent) object;
			if (row > -1) {
				if (!isSelected) {
					if (percent.doubleValue() > 0) {
						setBackground(Color.GREEN);
					}
					if (percent.doubleValue() < 0) {
						setBackground(Color.RED);
					}
				}
			}
			return this;
		}
	}
}
