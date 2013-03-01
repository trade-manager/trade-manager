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

import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.util.CoreUtils;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.YesNo;
import org.trade.dictionary.valuetype.DAOAccount;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.Portfolio;

/**
 */
public class PortfolioTableModel extends AspectTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	private static final String NAME = "Name*";
	private static final String PORTFOLIO_ALIAS = "Alias";
	private static final String DESCRIPTION = "Description";
	private static final String MASTER_ACCT_NUMBER = "Master Acct #*";
	private static final String IS_DEFAULT = "Default";

	private static final String[] columnHeaderToolTip = { null, null, null,
			"The account that is subscribed to", null };

	private Aspects m_data = null;

	public PortfolioTableModel() {
		super(columnHeaderToolTip);
		/*
		 * Get the column names and cache them. Then we can close the
		 * connection.
		 */
		columnNames = new String[5];
		columnNames[0] = NAME;
		columnNames[1] = PORTFOLIO_ALIAS;
		columnNames[2] = DESCRIPTION;
		columnNames[3] = MASTER_ACCT_NUMBER;
		columnNames[4] = IS_DEFAULT;
	}

	/**
	 * Method getData.
	 * 
	 * @return Aspects
	 */
	public Aspects getData() {
		return m_data;
	}

	/**
	 * Method setData.
	 * 
	 * @param data
	 *            Aspects
	 */
	public void setData(Aspects data) {

		this.m_data = data;
		this.clearAll();
		if (!getData().getAspect().isEmpty()) {
			for (final Aspect element : getData().getAspect()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, (Portfolio) element);
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

		final Portfolio element = (Portfolio) getData().getAspect().get(row);

		switch (column) {
		case 0: {
			element.setName((String) value);
			break;
		}
		case 1: {
			element.setAlias((String) value);
			break;
		}
		case 2: {
			element.setDescription((String) value);
			break;
		}
		case 3: {
			Account account = (Account) ((DAOAccount) value).getObject();
			element.setMasterAccountNumber(account);
			break;
		}
		case 4: {
			element.setIsDefault(new Boolean(((YesNo) value).getCode()));
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
		if (getData().getAspect().size() > 1) {
			String name = (String) this.getValueAt(selectedRow, 0);
			for (final Aspect element : getData().getAspect()) {
				if (CoreUtils.nullSafeComparator(
						((Portfolio) element).getName(), name) == 0) {
					getData().remove(element);
					getData().setDirty(true);
					final Vector<Object> currRow = rows.get(selectedRow);
					rows.remove(currRow);
					this.fireTableRowsDeleted(selectedRow, selectedRow);
					break;
				}
			}
		}
	}

	public void addRow() {
		final Portfolio element = new Portfolio();
		getData().getAspect().add(element);

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
	 *            Portfolio
	 */
	public void getNewRow(Vector<Object> newRow, Portfolio element) {
		newRow.addElement(element.getName());
		newRow.addElement(element.getAlias());
		newRow.addElement(element.getDescription());
		if (null == element.getMasterAccountNumber()) {
			newRow.addElement(DAOAccount.newInstance(Decode.NONE));
		} else {
			newRow.addElement(DAOAccount.newInstance(element
					.getMasterAccountNumber()));
		}
		newRow.addElement(YesNo.newInstance(element.getIsDefault()));
	}
}
