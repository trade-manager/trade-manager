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

import org.trade.core.util.CoreUtils;
import org.trade.dictionary.valuetype.DataType;
import org.trade.persistent.dao.CodeAttribute;
import org.trade.persistent.dao.CodeType;
import org.trade.ui.base.TableModel;

/**
 */
public class CodeAttributeTableModel extends TableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;
	private static final String NAME = "Name*";
	private static final String DESCRIPTION = "Description";
	private static final String DEFAULT_VALUE = "Default Value*";
	private static final String CLASS_NAME = "Data Type*";
	private static final String CLASS_EDITOR_NAME = "Data Type Editor";

	CodeType m_data = null;

	public CodeAttributeTableModel() {

		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[5];
		columnNames[0] = NAME;
		columnNames[1] = DESCRIPTION;
		columnNames[2] = DEFAULT_VALUE;
		columnNames[3] = CLASS_NAME;
		columnNames[4] = CLASS_EDITOR_NAME;
	}

	/**
	 * Method getData.
	 * 
	 * @return CodeType
	 */
	public CodeType getData() {
		return m_data;
	}

	/**
	 * Method setData.
	 * 
	 * @param data
	 *            CodeType
	 */
	public void setData(CodeType data) {

		this.m_data = data;
		this.clearAll();
		if (!getData().getCodeAttribute().isEmpty()) {

			for (final CodeAttribute element : getData().getCodeAttribute()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, element);
				rows.add(newRow);
			}
			fireTableDataChanged();
		}
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
	public void populateDAO(Object value, int row, int column) {

		final CodeAttribute element = getData().getCodeAttribute().get(row);

		switch (column) {
		case 0: {
			element.setName((String) value);
			break;
		}
		case 1: {
			element.setDescription((String) value);
			break;
		}
		case 2: {
			element.setDefaultValue((String) value);
			break;
		}
		case 3: {
			element.setClassName(((DataType) value).getCode());
			break;
		}
		case 4: {
			element.setEditorClassName((String) value);
			break;
		}
		default: {
		}
		}
		element.setDirty(true);
	}

	/**
	 * Method deleteRow.
	 * 
	 * @param selectedRow
	 *            int
	 */
	public void deleteRow(int selectedRow) {

		String name = (String) this.getValueAt(selectedRow, 0);
		for (final CodeAttribute element : getData().getCodeAttribute()) {
			if (CoreUtils.nullSafeComparator(element.getName(), name) == 0) {
				getData().getCodeAttribute().remove(element);
				getData().setDirty(true);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				this.fireTableRowsDeleted(selectedRow, selectedRow);
				break;
			}
		}
	}

	public void addRow() {

		final CodeAttribute element = new CodeAttribute(this.m_data, "", "",
				null, "", null);
		getData().getCodeAttribute().add(element);
		getData().setDirty(true);
		final Vector<Object> newRow = new Vector<Object>();
		getNewRow(newRow, element);
		rows.add(newRow);

		// Tell the listeners a new table has arrived.
		this.fireTableRowsInserted(rows.size() - 1, rows.size() - 1);

	}

	/**
	 * Method getNewRow.
	 * 
	 * @param newRow
	 *            Vector<Object>
	 * @param element
	 *            CodeAttribute
	 */
	public void getNewRow(Vector<Object> newRow, CodeAttribute element) {
		newRow.addElement(element.getName());
		newRow.addElement(element.getDescription());
		newRow.addElement(element.getDefaultValue());
		newRow.addElement(DataType.newInstance(element.getClassName()));
		newRow.addElement(element.getEditorClassName());
	}
}
