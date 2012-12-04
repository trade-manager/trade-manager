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
package org.trade.ui.models;

import java.util.Vector;
import javax.swing.event.TableModelEvent;

import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.persistent.dao.CodeType;

/**
 */
public class CodeTypeTableModel extends AspectTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;
	private static final String NAME = "Name*";
	private static final String DESCRIPTION = "Description";

	Aspects m_data = null;

	public CodeTypeTableModel() {

		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[2];
		columnNames[0] = NAME;
		columnNames[1] = DESCRIPTION;

	}

	/**
	 * Method getData.
	 * @return Aspects
	 */
	public Aspects getData() {
		return m_data;
	}

	/**
	 * Method setData.
	 * @param data Aspects
	 */
	public void setData(Aspects data) {

		this.m_data = data;
		this.clearAll();
		if (!getData().getAspect().isEmpty()) {
			for (final Aspect element : getData().getAspect()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, (CodeType) element);
				rows.add(newRow);
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Method populateDAO.
	 * @param value Object
	 * @param row int
	 * @param column int
	 */
	public void populateDAO(Object value, int row, int column) {

		final CodeType element = (CodeType) getData().getAspect().get(row);

		switch (column) {
		case 0: {
			element.setName((String) value);
			break;
		}
		case 1: {
			element.setDescription((String) value);
			break;
		}
		default: {
		}
		}
	}

	/**
	 * Method deleteRow.
	 * @param selectedRow int
	 */
	public void deleteRow(int selectedRow) {

		int i = 0;
		for (final Aspect element : getData().getAspect()) {
			if (i == selectedRow) {
				getData().remove(element);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				fireTableChanged(new TableModelEvent(this));
				break;
			}
			i++;
		}
	}

	public void addRow() {

		final CodeType element = new CodeType("", "");
		getData().add(element);
		final Vector<Object> newRow = new Vector<Object>();
		getNewRow(newRow, element);
		rows.add(newRow);

		// Tell the listeners a new table has arrived.
		fireTableChanged(new TableModelEvent(this));

	}

	/**
	 * Method getNewRow.
	 * @param newRow Vector<Object>
	 * @param element CodeType
	 */
	public void getNewRow(Vector<Object> newRow, CodeType element) {
		newRow.addElement(element.getName());
		newRow.addElement(element.getDescription());

	}
}
