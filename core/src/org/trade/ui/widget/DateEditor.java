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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.TableCellEditor;

import org.trade.core.valuetype.Date;

/**
 */
public class DateEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8851345801047150318L;

	final JSpinner spinner = new JSpinner();

	// Initializes the spinner.
	/**
	 * Constructor for DateEditor.
	 * @param date Date
	 * @param mask String
	 * @param field int
	 */
	public DateEditor(Date date, String mask, int field) {
		spinner.setModel(new SpinnerDateModel(date.getDate(), null, null, field));
		JSpinner.DateEditor de = new JSpinner.DateEditor(spinner, mask);
		spinner.setEditor(de);
	}

	// Prepares the spinner component and returns it.
	/**
	 * Method getTableCellEditorComponent.
	 * @param table JTable
	 * @param value Object
	 * @param isSelected boolean
	 * @param row int
	 * @param column int
	 * @return Component
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(JTable, Object, boolean, int, int)
	 */
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		if (null == ((Date) value).getDate()) {
			spinner.setValue(new java.util.Date());
		} else {
			spinner.setValue(((Date) value).getDate());
		}
		return spinner;
	}

	// Enables the editor only for double-clicks.
	/**
	 * Method isCellEditable.
	 * @param evt EventObject
	 * @return boolean
	 * @see javax.swing.CellEditor#isCellEditable(EventObject)
	 */
	public boolean isCellEditable(EventObject evt) {
		if (evt instanceof MouseEvent) {
			return ((MouseEvent) evt).getClickCount() >= 2;
		}
		return true;
	}

	// Returns the spinners current value.
	/**
	 * Method getCellEditorValue.
	 * @return Object
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	public Object getCellEditorValue() {
		return new Date((java.util.Date) spinner.getValue());
	}

}