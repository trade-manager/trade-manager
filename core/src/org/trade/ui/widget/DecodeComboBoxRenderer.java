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
import java.awt.Insets;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;

/**
 */
public class DecodeComboBoxRenderer extends DefaultListCellRenderer {

	private static final long serialVersionUID = 6927205466904515527L;

	public DecodeComboBoxRenderer() {
		setOpaque(true);
		this.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));
	}

	/**
	 * Method getListCellRendererComponent.
	 * 
	 * @param list
	 *            JList
	 * @param value
	 *            Object
	 * @param index
	 *            int
	 * @param isSelected
	 *            boolean
	 * @param cellHasFocus
	 *            boolean
	 * @return Component
	 */
	public Component getListCellRendererComponent(
			@SuppressWarnings("rawtypes") JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {
		if (value != null) {
			this.setText(value.toString());
			setBackground(isSelected ? Color.red : Color.white);
			setForeground(isSelected ? Color.white : Color.black);
		}

		return this;
	}
}
