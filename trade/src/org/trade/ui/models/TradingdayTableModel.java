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

import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Date;
import org.trade.dictionary.valuetype.MarketBar;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.ui.base.TableModel;

/**
 */
public class TradingdayTableModel extends TableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	private static final String OPEN = "       Open*       ";
	private static final String CLOSE = "       Close*      ";
	private static final String MKTGAP = "    Market Gap     ";
	private static final String MKTBIAS = "    Market Bias    ";
	private static final String MKTBAR = "    Market Bar     ";

	private static final String[] columnHeaderToolTip = {
			"<html>The start time of your trading time-frame.<br>"
					+ "This period must cover any rules defined in your strategy<br>"
					+ "Note the default is set in the config.properties (<b>trade.market.open</b>)<br>"
					+ "If trading outside regular market hrs set the following property to zero <br>"
					+ "in the config.properties (<b>trade.backfill.useRTH</b>)</html>",
			"<html>The end time of your trading time-frame.<br>"
					+ "Note the default is set in the config.properties (<b>trade.market.close</b>)</html>",
			"<html>Market gap from prev close <br>"
					+ "For stocks this is usually referes to S&P500 bar<br>"
					+ "<b>+NRB</b>=Green narrow range bar<br>"
					+ "<b>-NRB TT</b>=Red narrow range bar with topping tail<br>"
					+ "<b>-WRB</b>=Red wide range bar<br>"
					+ "<b>Darling Doji</b>=Green cross</html>",
			"<html>Your Market bias for the day<br>"
					+ "For stocks this is usually referes to S&P500<br>"
					+ "<b>+NRB</b>=Green narrow range bar<br>"
					+ "<b>-NRB TT</b>=Red narrow range bar with topping tail<br>"
					+ "<b>-WRB</b>=Red wide range bar<br>"
					+ "<b>Darling Doji</b>=Green cross</html>",

			"<html>Actual market bar i.e. S&P500<br>"
					+ "<b>+NRB</b>=Green narrow range bar<br>"
					+ "<b>-NRB TT</b>=Red narrow range bar with topping tail<br>"
					+ "<b>-WRB</b>=Red wide range bar<br>"
					+ "<b>Darling Doji</b>=Green cross</html>" };

	private Tradingdays m_data = null;

	public TradingdayTableModel() {
		super(columnHeaderToolTip);
		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[5];
		columnNames[0] = OPEN;
		columnNames[1] = CLOSE;
		columnNames[2] = MKTGAP;
		columnNames[3] = MKTBIAS;
		columnNames[4] = MKTBAR;
	}

	/**
	 * Method getData.
	 * 
	 * @return Tradingdays
	 */
	public Tradingdays getData() {
		return m_data;
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
		Date openDate = (Date) this.getValueAt(row, 0);
		Date closeDate = (Date) this.getValueAt(row, 1);
		Tradingday tradingday = getData().getTradingday(openDate.getDate(),
				closeDate.getDate());
		if (null != tradingday && Tradingdays.hasTrades(tradingday)) {
			if ((columnNames[column] == OPEN) || (columnNames[column] == CLOSE)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method setData.
	 * 
	 * @param data
	 *            Tradingdays
	 */
	public void setData(Tradingdays data) {

		this.m_data = data;
		this.clearAll();
		if (!getData().getTradingdays().isEmpty()) {
			for (Tradingday element : getData().getTradingdays()) {
				Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, element);
				rows.add(newRow);
			}
			fireTableDataChanged();
		}
	}

	/**
	 * Method getValueAt.
	 * 
	 * 
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @return value Object
	 */

	public Object getValueAt(int row, int column) {
		if (columnNames[column] == CLOSE) {
			Date closeDate = ((Date) super.getValueAt(row, column));
			Date openDate = ((Date) super.getValueAt(row, 0));
			if (null != closeDate && null != openDate) {
				if (closeDate.getDate().before(openDate.getDate())) {
					Date date = new Date(TradingCalendar.addBusinessDays(
							closeDate.getDate(), 1));
					if (TradingCalendar.daysDiff(openDate.getDate(),
							date.getDate()) > 1)
						date = openDate;
					this.setValueAt(date, row, column);
					return date;
				}

				if (((closeDate.getDate().getTime() - openDate.getDate()
						.getTime()) / 1000) > (60 * 60 * 24)) {
					Date date = new Date(TradingCalendar.getSpecificTime(
							closeDate.getDate(), openDate.getDate()));
					this.setValueAt(date, row, column);
					return date;
				}
			}
		}
		return super.getValueAt(row, column);
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
		if (null != value && !value.equals(super.getValueAt(row, column))) {
			this.populateDAO(value, row, column);
			Vector<Object> dataRow = rows.get(row);
			dataRow.setElementAt(value, column);
			fireTableCellUpdated(row, column);
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

		Date openDate = (Date) super.getValueAt(row, 0);
		Date closeDate = (Date) super.getValueAt(row, 1);
		Tradingday element = getData().getTradingday(openDate.getDate(),
				closeDate.getDate());

		switch (column) {
		case 0: {
			element.setOpen(((Date) value).getDate());
			getData().remove(openDate.getDate(), closeDate.getDate());
			getData().add(element);
			break;
		}
		case 1: {
			element.setClose(((Date) value).getDate());
			break;
		}
		case 2: {
			element.setMarketGap(((MarketBar) value).getCode());
			break;
		}
		case 3: {
			element.setMarketBias(((MarketBar) value).getCode());
			break;
		}
		case 4: {
			element.setMarketBar(((MarketBar) value).getCode());
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

		Date open = (Date) this.getValueAt(selectedRow, 0);
		Date close = (Date) this.getValueAt(selectedRow, 1);
		Tradingday element = getData().getTradingday(open.getDate(),
				close.getDate());
		if (!element.getTradestrategies().isEmpty())
			element.getTradestrategies().clear();
		getData().remove(element);
		element.setDirty(true);
		final Vector<Object> currRow = rows.get(selectedRow);
		rows.remove(currRow);
		this.fireTableRowsDeleted(selectedRow, selectedRow);
	}

	public void addRow() {

		java.util.Date date = new java.util.Date();
		if (!TradingCalendar.isTradingDay(date)) {
			date = TradingCalendar.getNextTradingDay(date);
		}
		while (getData().containsTradingday(
				TradingCalendar.getBusinessDayStart(date),
				TradingCalendar.getBusinessDayEnd(date))) {
			date = TradingCalendar.getNextTradingDay(date);
		}
		Tradingday element = Tradingday.newInstance(date);
		getData().add(element);

		Vector<Object> newRow = new Vector<Object>();
		getNewRow(newRow, element);
		rows.add(newRow);
		this.fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
	}

	/**
	 * Method getNewRow.
	 * 
	 * @param newRow
	 *            Vector<Object>
	 * @param element
	 *            Tradingday
	 */
	public void getNewRow(Vector<Object> newRow, Tradingday element) {
		newRow.addElement(new Date(element.getOpen()));
		newRow.addElement(new Date(element.getClose()));
		if (null == element.getMarketGap()) {
			newRow.addElement(new MarketBar());
		} else {
			newRow.addElement(MarketBar.newInstance((element.getMarketGap())));
		}
		if (null == element.getMarketBias()) {
			newRow.addElement(new MarketBar());
		} else {
			newRow.addElement(MarketBar.newInstance((element.getMarketBias())));
		}
		if (null == element.getMarketBar()) {
			newRow.addElement(new MarketBar());
		} else {
			newRow.addElement(MarketBar.newInstance((element.getMarketBar())));
		}
	}
}
