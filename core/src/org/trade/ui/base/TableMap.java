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

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * 
 * @version $Id: TableMap.java,v 1.1 2001/10/18 01:32:14 simon Exp $
 * @author Simon Allen
 */
public class TableMap extends AbstractTableModel implements TableModelListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5933237472572427135L;
	protected TableModel model;

	/**
	 * Method getModel.
	 * 
	 * @return TableModel
	 */
	public TableModel getModel() {
		return model;
	}

	/**
	 * Method setModel.
	 * 
	 * @param model
	 *            TableModel
	 */
	public void setModel(TableModel model) {
		this.model = model;

		model.addTableModelListener(this);
	}

	/**
	 * Method getValueAt. By default, Implement TableModel by forwarding all
	 * messages to the model.
	 * 
	 * @param aRow
	 *            int
	 * @param aColumn
	 *            int
	 * @return Object
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int aRow, int aColumn) {
		return model.getValueAt(aRow, aColumn);
	}

	/**
	 * Method setValueAt.
	 * 
	 * @param aValue
	 *            Object
	 * @param aRow
	 *            int
	 * @param aColumn
	 *            int
	 * @see javax.swing.table.TableModel#setValueAt(Object, int, int)
	 */
	public void setValueAt(Object aValue, int aRow, int aColumn) {
		model.setValueAt(aValue, aRow, aColumn);
	}

	/**
	 * Method getRowCount.
	 * 
	 * @return int
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return (model == null) ? 0 : model.getRowCount();
	}

	/**
	 * Method getColumnCount.
	 * 
	 * @return int
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return (model == null) ? 0 : model.getColumnCount();
	}

	/**
	 * Method getColumnName.
	 * 
	 * @param aColumn
	 *            int
	 * @return String
	 * @see javax.swing.table.TableModel#getColumnName(int)
	 */
	public String getColumnName(int aColumn) {
		return model.getColumnName(aColumn);
	}

	/**
	 * Method getColumnClass.
	 * 
	 * @param aColumn
	 *            int
	 * @return Class<?>
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int aColumn) {
		return model.getColumnClass(aColumn);
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
		return model.isCellEditable(row, column);
	}

	/**
	 * Method tableChanged. Implementation of the TableModelListener interface,
	 * By default forward all events to all the listeners.
	 * 
	 * @param e
	 *            TableModelEvent
	 * @see javax.swing.event.TableModelListener#tableChanged(TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}
}
