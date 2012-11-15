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

import org.trade.core.valuetype.Decimal;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Percent;
import org.trade.core.valuetype.Quantity;
import org.trade.persistent.dao.TradelogReport;
import org.trade.persistent.dao.TradelogSummary;
import org.trade.ui.base.TableModel;

/**
 */
public class TradelogSummaryTableModel extends TableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	public static final String PERIOD = "Period";
	public static final String BATTING_AVERAGE = "Batting Avg";
	public static final String SHARPE_RATIO = "Sharpe Ratio";
	public static final String GROSS_PL = "Gross P/L";
	public static final String QUANTITY = "Quantity";
	public static final String COMMISSION = "Commission";
	public static final String NET_PL = "Net P/L";
	public static final String WIN_COUNT = "Wins";
	public static final String WIN_AMOUNT = "Profit Amount";
	public static final String LOSS_COUNT = "Losses";
	public static final String LOSS_AMOUNT = "Loss Amount";
	public static final String TRADE_COUNT = "Trades";
	public static final String CONTRACT_COUNT = "Contracts";

	private static final String[] columnHeaderToolTip = { null,
			"% wins vs loss",
			"Simple sharpe ratio (sum $wins/#wins)/(sum $loss/#loss)", null,
			null, null, null, null, null, null, null, null, };

	private TradelogReport m_data = null;

	/**
	 * OrderModel() -
	 * 
	 * 
	 * @exception * @see
	 */
	public TradelogSummaryTableModel() {
		super(columnHeaderToolTip);
		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[13];
		columnNames[0] = PERIOD;
		columnNames[1] = BATTING_AVERAGE;
		columnNames[2] = SHARPE_RATIO;
		columnNames[3] = GROSS_PL;
		columnNames[4] = QUANTITY;
		columnNames[5] = COMMISSION;
		columnNames[6] = NET_PL;
		columnNames[7] = WIN_COUNT;
		columnNames[8] = WIN_AMOUNT;
		columnNames[9] = LOSS_COUNT;
		columnNames[10] = LOSS_AMOUNT;
		columnNames[11] = TRADE_COUNT;
		columnNames[12] = CONTRACT_COUNT;
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
		return false;
	}

	/**
	 * Method getData.
	 * 
	 * @return TradelogReport
	 */
	public TradelogReport getData() {
		return m_data;
	}

	/**
	 * setData() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param data
	 *            TradelogReport
	 * @exception * @see
	 */

	public void setData(TradelogReport data) {

		this.m_data = data;
		this.clearAll();
		if (!getData().getTradelogSummary().isEmpty()) {

			for (final TradelogSummary element : getData().getTradelogSummary()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, element);
				rows.add(newRow);
			}
			fireTableDataChanged();
		}
	}

	/**
	 * getData() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param value
	 *            Object
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @exception * @see
	 */

	public void populateDAO(Object value, int row, int column) {
		final TradelogSummary element = getData().getTradelogSummary().get(row);

		switch (column) {

		case 1: {
			element.setPeriod((String) value);
			break;
		}
		case 2: {
			element.setBattingAverage(((Percent) value).getBigDecimalValue());
			break;
		}
		case 3: {
			element.setSimpleSharpeRatio(((Decimal) value).getBigDecimalValue());
			break;
		}
		case 4: {
			element.setGrossProfitLoss(((Money) value).getBigDecimalValue());
			break;
		}
		case 5: {
			element.setQuantity(((Quantity) value).getIntegerValue());
			break;
		}
		case 6: {
			element.setCommission(((Money) value).getBigDecimalValue());
			break;
		}
		case 7: {
			element.setNetProfitLoss(((Money) value).getBigDecimalValue());
			break;
		}
		case 8: {
			element.setWinCount(((Quantity) value).getIntegerValue());
			break;
		}
		case 9: {
			element.setProfitAmount(((Money) value).getBigDecimalValue());
			break;
		}
		case 10: {
			element.setLossCount(((Quantity) value).getIntegerValue());
			break;
		}
		case 11: {
			element.setLossAmount(((Money) value).getBigDecimalValue());
			break;
		}
		case 12: {
			element.setTradeCount(((Quantity) value).getIntegerValue());
			break;
		}
		case 13: {
			element.setTradestrategyCount(((Quantity) value).getIntegerValue());
			break;
		}
		default: {
		}
		}
	}

	/**
	 * deleteRow() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param selectedRow
	 *            int
	 * @exception * @see
	 */
	public void deleteRow(int selectedRow) {

		int i = 0;
		for (final TradelogSummary element : getData().getTradelogSummary()) {
			if (i == selectedRow) {
				getData().getTradelogSummary().remove(element);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				fireTableChanged(new TableModelEvent(this));
				break;
			}
			i++;
		}
	}

	/**
	 * Method addRow.
	 * 
	 * @param element
	 *            TradelogSummary
	 */
	public void addRow(TradelogSummary element) {

		getData().getTradelogSummary().add(element);
		final Vector<Object> newRow = new Vector<Object>();

		getNewRow(newRow, element);
		rows.add(newRow);

		// Tell the listeners a new table has arrived.

		fireTableChanged(new TableModelEvent(this));

	}

	public void addRow() {
		final TradelogSummary element = new TradelogSummary();
		getData().getTradelogSummary().add(element);
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
	 *            TradelogSummary
	 */
	public void getNewRow(Vector<Object> newRow, TradelogSummary element) {

		newRow.addElement(element.getPeriod());
		newRow.addElement(new Percent(element.getBattingAverage()));
		newRow.addElement(new Decimal(element.getSimpleSharpeRatio()));
		newRow.addElement(new Money(element.getGrossProfitLoss()));
		newRow.addElement(new Quantity(element.getQuantity()));
		newRow.addElement(new Money(element.getCommission()));
		newRow.addElement(new Money(element.getNetProfitLoss()));
		newRow.addElement(new Quantity(element.getWinCount()));
		newRow.addElement(new Money(element.getProfitAmount()));
		newRow.addElement(new Quantity(element.getLossCount()));
		newRow.addElement(new Money(element.getLossAmount()));
		newRow.addElement(new Quantity(element.getTradeCount()));
		newRow.addElement(new Quantity(element.getTradestrategyCount()));
	}
}
