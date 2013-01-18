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

import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.Decimal;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Quantity;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.MarketBar;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.Tier;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.dao.TradelogDetail;
import org.trade.persistent.dao.TradelogReport;
import org.trade.ui.base.TableModel;

/**
 */
public class TradelogDetailTableModel extends TableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	public static final String DATE = "   Date    ";
	public static final String SYMBOL = "Symbol";
	public static final String LONGSHORT = "Long/Short";
	public static final String TIER = "Tier";
	public static final String MARKET_BIAS = "Mkt Bias";
	public static final String MARKET_BAR = "Mkt Bar";
	public static final String STRATEGY = "   Strategy   ";
	public static final String STATUS = "    Status    ";
	public static final String SIDE = "Side";
	public static final String ACTION = "Action";
	public static final String STOP_PRICE = "Stop Price";
	public static final String ORDER_STATUS = "Order Status";
	public static final String FILLED_DATE = "Trade Time";
	public static final String QUANTITY = "Quantity";
	public static final String AVG_FILL_PRICE = "Avg Price";
	public static final String COMMISION = "Comms";
	public static final String PROFIT_LOSS = "Net P/L Amt";

	private TradelogReport m_data = null;

	/**
	 * OrderModel() -
	 * 
	 * 
	 * @exception * @see
	 */
	public TradelogDetailTableModel() {
		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[17];
		columnNames[0] = DATE;
		columnNames[1] = SYMBOL;
		columnNames[2] = LONGSHORT;
		columnNames[3] = TIER;
		columnNames[4] = MARKET_BIAS;
		columnNames[5] = MARKET_BAR;
		columnNames[6] = STRATEGY;
		columnNames[7] = STATUS;
		columnNames[8] = SIDE;
		columnNames[9] = ACTION;
		columnNames[10] = STOP_PRICE;
		columnNames[11] = ORDER_STATUS;
		columnNames[12] = FILLED_DATE;
		columnNames[13] = QUANTITY;
		columnNames[14] = AVG_FILL_PRICE;
		columnNames[15] = COMMISION;
		columnNames[16] = PROFIT_LOSS;
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
	 * Method setData.
	 * 
	 * @param data
	 *            TradelogReport
	 */
	public void setData(TradelogReport data) {

		this.m_data = data;
		this.clearAll();
		if (!getData().getTradelogDetail().isEmpty()) {
			for (final TradelogDetail element : getData().getTradelogDetail()) {
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
	 * @param value
	 *            Object
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @exception * @see
	 */
	public void populateDAO(Object value, int row, int column) {
		final TradelogDetail element = getData().getTradelogDetail().get(row);

		switch (column) {

		case 1: {
			element.setOpen((String) value);
			break;
		}
		case 2: {
			element.setSymbol((String) value);
			break;
		}
		case 3: {
			element.setLongShort(((Side) value).getCode());
			break;
		}
		case 4: {
			element.setTier(((Tier) value).getCode());
			break;
		}
		case 5: {
			element.setMarketBias(((MarketBar) value).getCode());
			break;
		}
		case 6: {
			element.setMarketBar(((MarketBar) value).getCode());
			break;
		}
		case 7: {
			element.setName((String) value);
			break;
		}
		case 8: {
			element.setStatus(((TradestrategyStatus) value).getCode());
			break;
		}
		case 9: {
			element.setSide(((Side) value).getCode());
			break;
		}
		case 10: {
			element.setAction(((Action) value).getCode());
			break;
		}
		case 11: {
			element.setStopPrice(((Money) value).getBigDecimalValue());
			break;
		}
		case 12: {
			element.setOrderStatus(((OrderStatus) value).getCode());
			break;
		}
		case 13: {
			element.setFilledDate(((Date) value).getDate());
			break;
		}
		case 14: {
			element.setQuantity(((Quantity) value).getIntegerValue());
			break;
		}
		case 15: {
			element.setAverageFilledPrice(((Decimal) value)
					.getBigDecimalValue());
			break;
		}
		case 16: {
			element.setCommission(((Money) value).getBigDecimalValue());
			break;
		}
		case 17: {
			element.setProfitLoss(((Money) value).getBigDecimalValue());
			break;
		}
		default: {
		}
		}
	}

	/**
	 * deleteRow() -
	 * 
	 * @param selectedRow
	 *            int
	 * @exception * @see
	 */
	public void deleteRow(int selectedRow) {

		int i = 0;
		for (final TradelogDetail element : getData().getTradelogDetail()) {
			if (i == selectedRow) {
				getData().getTradelogDetail().remove(element);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				this.fireTableRowsDeleted(selectedRow, selectedRow);
				break;
			}
			i++;
		}
	}

	/**
	 * Method addRow.
	 * 
	 * @param element
	 *            TradelogDetail
	 */
	public void addRow(TradelogDetail element) {

		getData().getTradelogDetail().add(element);
		final Vector<Object> newRow = new Vector<Object>();

		getNewRow(newRow, element);
		rows.add(newRow);
		// Tell the listeners a new table has arrived.
		this.fireTableRowsInserted(rows.size() - 1, rows.size() - 1);

	}

	public void addRow() {

		final TradelogDetail element = new TradelogDetail();
		getData().getTradelogDetail().add(element);
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
	 *            TradelogDetail
	 */
	public void getNewRow(Vector<Object> newRow, TradelogDetail element) {

		newRow.addElement(element.getOpen());
		if (null == element.getSymbol()) {
			newRow.addElement("");
		} else {
			newRow.addElement(element.getSymbol());
		}
		if (null == element.getLongShort()) {
			newRow.addElement(new Side());
		} else {
			newRow.addElement(Side.newInstance(element.getLongShort()));
		}

		if (null == element.getTier()) {
			newRow.addElement(new Tier());
		} else {
			newRow.addElement(Tier.newInstance(element.getTier()));
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
		if (null == element.getName()) {
			newRow.addElement("");
		} else {
			newRow.addElement(DAOStrategy.newInstance(element.getName()));
		}
		if (null == element.getStatus()) {
			newRow.addElement(new TradestrategyStatus());
		} else {
			newRow.addElement(TradestrategyStatus.newInstance((element
					.getStatus())));
		}
		if (null == element.getSide()) {
			newRow.addElement(new Side());
		} else {
			newRow.addElement(Side.newInstance((element.getSide())));
		}
		if (null == element.getAction()) {
			newRow.addElement(new Action());
		} else {
			newRow.addElement(Action.newInstance(element.getAction()));
		}
		if (null == element.getStopPrice()) {
			newRow.addElement(new Money());
		} else {
			newRow.addElement(new Money(element.getStopPrice()));
		}
		if (null == element.getOrderStatus()) {
			newRow.addElement(new OrderStatus());
		} else {
			newRow.addElement(OrderStatus.newInstance(element.getOrderStatus()));
		}
		if (null == element.getFilledDate()) {
			newRow.addElement(new Date());
		} else {
			newRow.addElement(new Date(element.getFilledDate()));
		}
		newRow.addElement(new Quantity(element.getQuantity()));

		if (null == element.getAverageFilledPrice()) {
			newRow.addElement(new Decimal(3));
		} else {
			newRow.addElement(new Decimal(element.getAverageFilledPrice(), 3));
		}
		if (null == element.getCommission()) {
			newRow.addElement(new Money());
		} else {
			newRow.addElement(new Money(element.getCommission()));
		}
		if (null == element.getProfitLoss()) {
			newRow.addElement(new Money());
		} else {
			newRow.addElement(new Money(element.getProfitLoss()));
		}
	}
}
