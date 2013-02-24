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
import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.YesNo;
import org.trade.dictionary.valuetype.AccountType;
import org.trade.dictionary.valuetype.Currency;
import org.trade.persistent.dao.TradeAccount;

/**
 */
public class TradeAccountTableModel extends AspectTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	private static final String NAME = "Name*";
	private static final String ACCT_NUMBER = "Acct #";
	private static final String ACCT_TYPE = "Type";
	private static final String ACCT_ALIAS = "Alias";
	private static final String IS_DEFAULT = "Default";
	private static final String CURRENCY = "Currency";
	private static final String AVAILABLE_FUNDS = "Availble Funds";
	private static final String BUYING_POWER = " Buying Power";
	private static final String CASH_BALANCE = "Cash Bal";
	private static final String GROSS_POSITION_VALUE = "Gross Pos Val";
	private static final String REALIZED_PL = "Realized P/L";
	private static final String UNREALIZED_PL = "Unrealized P/L";
	private static final String LAST_UPDATED = "  Last Update  ";

	private Aspects m_data = null;

	public TradeAccountTableModel() {

		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[13];
		columnNames[0] = NAME;
		columnNames[1] = ACCT_NUMBER;
		columnNames[2] = ACCT_TYPE;
		columnNames[3] = ACCT_ALIAS;
		columnNames[4] = IS_DEFAULT;
		columnNames[5] = CURRENCY;
		columnNames[6] = AVAILABLE_FUNDS;
		columnNames[7] = BUYING_POWER;
		columnNames[8] = CASH_BALANCE;
		columnNames[9] = GROSS_POSITION_VALUE;
		columnNames[10] = REALIZED_PL;
		columnNames[11] = UNREALIZED_PL;
		columnNames[12] = LAST_UPDATED;
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
				getNewRow(newRow, (TradeAccount) element);
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

		final TradeAccount element = (TradeAccount) getData().getAspect().get(
				row);

		switch (column) {
		case 0: {
			element.setName((String) value);
			break;
		}
		case 1: {
			element.setAccountNumber((String) value);
			break;
		}
		case 2: {
			element.setAccountType(((AccountType) value).getCode());
			break;
		}
		case 3: {
			element.setAlias((String) value);
			break;
		}
		case 4: {
			element.setIsDefault(new Boolean(((YesNo) value).getCode()));
			break;
		}
		case 5: {
			element.setCurrency(((Currency) value).getCode());
			break;
		}
		case 6: {
			element.setAvailableFunds(((Money) value).getBigDecimalValue());
			break;
		}
		case 7: {
			element.setBuyingPower(((Money) value).getBigDecimalValue());
			break;
		}
		case 8: {
			element.setCashBalance(((Money) value).getBigDecimalValue());
			break;
		}
		case 9: {
			element.setGrossPositionValue(((Money) value).getBigDecimalValue());
			break;
		}
		case 10: {
			element.setRealizedPnL(((Money) value).getBigDecimalValue());
			break;
		}
		case 11: {
			element.setUnrealizedPnL(((Money) value).getBigDecimalValue());
			break;
		}
		case 12: {
			element.setUpdateDate(((Date) value).getDate());
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

		String acctNumber = (String) this.getValueAt(selectedRow, 1);
		for (final Aspect element : getData().getAspect()) {
			if (CoreUtils.nullSafeComparator(
					((TradeAccount) element).getAccountNumber(), acctNumber) == 0) {
				getData().remove(element);
				getData().setDirty(true);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				this.fireTableRowsDeleted(selectedRow, selectedRow);
				break;
			}
		}
	}

	public void addRow() {
		final TradeAccount element = new TradeAccount();
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
	 *            TradeAccount
	 */
	public void getNewRow(Vector<Object> newRow, TradeAccount element) {
		newRow.addElement(element.getName());
		newRow.addElement(element.getAccountNumber());
		if (null == element.getAccountType()) {
			newRow.addElement(new AccountType());
		} else {
			newRow.addElement(AccountType.newInstance(element.getAccountType()));
		}
		newRow.addElement(element.getAlias());
		newRow.addElement(YesNo.newInstance(element.getIsDefault()));
		newRow.addElement(Currency.newInstance(element.getCurrency()));
		if (null == element.getAvailableFunds()) {
			newRow.addElement(new Money(0));
		} else {
			newRow.addElement(new Money(element.getAvailableFunds()));
		}
		if (null == element.getBuyingPower()) {
			newRow.addElement(new Money(0));
		} else {
			newRow.addElement(new Money(element.getBuyingPower()));
		}
		if (null == element.getCashBalance()) {
			newRow.addElement(new Money(0));
		} else {
			newRow.addElement(new Money(element.getCashBalance()));
		}
		if (null == element.getGrossPositionValue()) {
			newRow.addElement(new Money(0));
		} else {
			newRow.addElement(new Money(element.getGrossPositionValue()));
		}
		if (null == element.getRealizedPnL()) {
			newRow.addElement(new Money(0));
		} else {
			newRow.addElement(new Money(element.getRealizedPnL()));
		}
		if (null == element.getUnrealizedPnL()) {
			newRow.addElement(new Money(0));
		} else {
			newRow.addElement(new Money(element.getUnrealizedPnL()));
		}
		if (null == element.getUpdateDate()) {
			newRow.addElement(new Date());
		} else {
			newRow.addElement(new Date(element.getUpdateDate()));
		}
	}
}
