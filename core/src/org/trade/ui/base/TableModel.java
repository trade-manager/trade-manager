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

import java.util.ArrayList;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 */
public abstract class TableModel extends AbstractTableModel {

	private static final long serialVersionUID = 7428125408630828769L;
	protected String[] columnNames = {};
	protected String[] columnHeaderToolTip = {};
	protected Class<?>[] columnTypes = {};
	protected ArrayList<Vector<Object>> rows = new ArrayList<Vector<Object>>(0);

	public TableModel() {
	}

	public TableModel(String[] columnHeaderToolTip) {
		this.columnHeaderToolTip = columnHeaderToolTip;
	}

	/**
	 * Method populateDAO.
	 * 
	 * @param value
	 *            Object
	 * @param row
	 *            int
	 * @param column
	 *            int
	 */
	public abstract void populateDAO(Object value, int row, int column);

	public abstract void addRow();

	/**
	 * Method deleteRow.
	 * 
	 * @param selectedRow
	 *            int
	 */
	public abstract void deleteRow(int selectedRow);

	public void clearAll() {
		int rowSize = rows.size() - 1;
		if (rowSize > -1) {
			rows = new ArrayList<Vector<Object>>();
			this.fireTableRowsDeleted(0, rowSize);
		}
	}

	/**
	 * Method getColumnHeaderToolTip.
	 * 
	 * @param column
	 *            int
	 * @return String
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnHeaderToolTip(int column) {
		if (columnHeaderToolTip.length > column) {
			return columnHeaderToolTip[column];
		} else {
			return "";
		}
	}

	/**
	 * Method getColumnName.
	 * 
	 * @param column
	 *            int
	 * @return String
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		if (columnNames[column] != null) {
			return columnNames[column];
		} else {
			return "";
		}
	}

	/**
	 * Method getColumnClass.
	 * 
	 * @param column
	 *            int
	 * @return Class<?>
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int column) {
		if (null == getValueAt(0, column)) {
			return String.class;
		} else {
			return getValueAt(0, column).getClass();
		}

	}

	/**
	 * Method isCellEditable.
	 * 
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @return boolean
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	/**
	 * Method getColumnCount.
	 * 
	 * @return int
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Method getRowCount.
	 * 
	 * @return int
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return rows.size();
	}

	/**
	 * Method getValueAt.
	 * 
	 * @param aRow
	 *            int
	 * @param aColumn
	 *            int
	 * @return Object
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int aRow, int aColumn) {
		if (!rows.isEmpty()) {
			Vector<Object> row = rows.get(aRow);
			return row.elementAt(aColumn);
		}
		return null;
	}

	/**
	 * Method setValueAt.
	 * 
	 * @param value
	 *            Object
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object value, int row, int column) {
		if (!getValueAt(row, column).equals(value)) {
			Object newValue = getColumnDataValue(getValueAt(row, column), value);
			this.populateDAO(newValue, row, column);
			Vector<Object> dataRow = rows.get(row);
			dataRow.setElementAt(newValue, column);
			fireTableCellUpdated(row, column);
		}
	}

	/**
	 * Method getColumnDataValue.
	 * 
	 * @param currValue
	 *            Object
	 * @param newValue
	 *            Object
	 * @return Object
	 */
	public Object getColumnDataValue(Object currValue, Object newValue) {
		Object returnValue = currValue;
		if (null != newValue) {
			returnValue = newValue;
		}
		return returnValue;
	}
}
