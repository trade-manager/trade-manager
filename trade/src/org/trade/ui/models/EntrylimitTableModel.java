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
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Percent;
import org.trade.core.valuetype.Quantity;
import org.trade.persistent.dao.Entrylimit;

/**
 */
public class EntrylimitTableModel extends AspectTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	private static final String START_PRICE = "Price From*";
	private static final String END_PRICE = "Price To*";
	private static final String STP_LMT_AMOUNT = "Stop Lmt Amt";
	private static final String PERCENT_OF_PRICE = "% Range Bar";
	private static final String PERCENT_OF_MARGIN = "% Of Margin";
	private static final String ROUND_SHARES = "Round Shares";
	private static final String ROUND_PRICE = "Round Price";
	private static final String PIVOT_RANGE = "Pivot Range";

	private static final String[] columnHeaderToolTip = { "Price range start",
			"Price range end", "For Stop/Limit orders the price range",
			"The % of contract price for a bars range from H/L",
			"The max % of margin to use for a trade",
			"Rounding of shares on a calculated open position",
			"# cents before/after whole/half number to round over/under",
			"The min H/L between pivots" };

	private Aspects m_data = null;

	public EntrylimitTableModel() {
		super(columnHeaderToolTip);
		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[8];
		columnNames[0] = START_PRICE;
		columnNames[1] = END_PRICE;
		columnNames[2] = STP_LMT_AMOUNT;
		columnNames[3] = PERCENT_OF_PRICE;
		columnNames[4] = PERCENT_OF_MARGIN;
		columnNames[5] = ROUND_SHARES;
		columnNames[6] = ROUND_PRICE;
		columnNames[7] = PIVOT_RANGE;
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
	 * @throws Exception
	 */
	public void setData(Aspects data) throws Exception {

		this.m_data = data;
		this.clearAll();
		if (!getData().getAspect().isEmpty()) {

			for (final Aspect element : getData().getAspect()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, (Entrylimit) element);
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

		final Entrylimit element = (Entrylimit) getData().getAspect().get(row);

		switch (column) {
		case 0: {
			element.setStartPrice(((Money) value).getBigDecimalValue());
			break;
		}
		case 1: {
			element.setEndPrice(((Money) value).getBigDecimalValue());
			break;
		}
		case 2: {
			element.setLimitAmount(((Money) value).getBigDecimalValue());
			break;
		}
		case 3: {
			element.setPercentOfPrice(((Percent) value).getBigDecimalValue());
			break;
		}
		case 4: {
			element.setPercentOfMargin(((Percent) value).getBigDecimalValue());
			break;
		}
		case 5: {
			element.setShareRound(((Quantity) value).getIntegerValue());
			break;
		}
		case 6: {
			element.setPriceRound(((Money) value).getBigDecimalValue());
			break;
		}
		case 7: {
			element.setPivotRange(((Money) value).getBigDecimalValue());
			break;
		}
		default: {
		}
		}
	}

	/**
	 * Method deleteRow.
	 * 
	 * @param selectedRow
	 *            int
	 */
	public void deleteRow(int selectedRow) {

		int i = 0;
		for (final Aspect element : getData().getAspect()) {
			if (i == selectedRow) {
				getData().getAspect().remove(element);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				fireTableChanged(new TableModelEvent(this));
				break;
			}
			i++;
		}
	}

	public void addRow() {

		final Entrylimit element = new Entrylimit();
		getData().getAspect().add(element);

		final Vector<Object> newRow = new Vector<Object>();
		getNewRow(newRow, element);
		rows.add(newRow);

		// Tell the listeners a new table has arrived.
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * Method getNewRow.
	 * 
	 * @param newRow
	 *            Vector<Object>
	 * @param element
	 *            Entrylimit
	 */
	public void getNewRow(Vector<Object> newRow, Entrylimit element) {
		newRow.addElement(new Money(element.getStartPrice()));
		newRow.addElement(new Money(element.getEndPrice()));
		newRow.addElement(new Money(element.getLimitAmount()));
		newRow.addElement(new Percent(element.getPercentOfPrice()));
		newRow.addElement(new Percent(element.getPercentOfMargin()));
		newRow.addElement(new Quantity(element.getShareRound()));
		newRow.addElement(new Money(element.getPriceRound()));
		newRow.addElement(new Money(element.getPivotRange()));
	}
}
