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

import org.trade.core.util.CoreUtils;
import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.Decimal;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Quantity;
import org.trade.core.valuetype.YesNo;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.DAOEntryLimit;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.dao.Entrylimit;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradePosition;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.ui.base.TableModel;

/**
 */
public class TradeOrderTableModel extends TableModel {

	private static final long serialVersionUID = 3087514589731145479L;

	private static final String SYMBOL = "Symbol*";
	private static final String ORDER_KEY = " Order Key ";
	private static final String QUANTITY = "Qty*";
	private static final String ACTION = "Action*";
	private static final String ORDER_TYPE = "Type*";
	private static final String LMT_PRICE = "Lmt Price";
	private static final String AUX_PRICE = "Aux Price";
	private static final String TRANSMIT = "Transmit";
	private static final String STATUS = "   Status   ";
	private static final String OCA_GRP_NAME = "OCA Id";
	private static final String AVG_PRICE = "Avg Price";
	private static final String FILLED_DATE = "Filled Time";
	private static final String FILLED_QTY = "Filled Qty";
	private static final String STOP_PRICE = "Stop Price";
	private static final String PROPERTIES = "Alloc Props";

	private static final String[] columnHeaderToolTip = { null,
			"System generated key", "Buy/Sell/Short", null, null, null,
			"Stop/Mkt price", "Transmit to mkt in TWS", null,
			"One Cancels Another(OCA) id must be unique for day", null, null,
			null, null, "FA account assignment" };
	private Tradestrategy m_data = null;

	public TradeOrderTableModel() {
		super(columnHeaderToolTip);
		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[15];
		columnNames[0] = SYMBOL;
		columnNames[1] = ORDER_KEY;
		columnNames[2] = ACTION;
		columnNames[3] = ORDER_TYPE;
		columnNames[4] = QUANTITY;
		columnNames[5] = LMT_PRICE;
		columnNames[6] = AUX_PRICE;
		columnNames[7] = TRANSMIT;
		columnNames[8] = STATUS;
		columnNames[9] = OCA_GRP_NAME;
		columnNames[10] = AVG_PRICE;
		columnNames[11] = FILLED_DATE;
		columnNames[12] = FILLED_QTY;
		columnNames[13] = STOP_PRICE;
		columnNames[14] = PROPERTIES;

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
		OrderStatus orderStatus = (OrderStatus) this.getValueAt(row, 8);
		if (null != orderStatus) {
			if (OrderStatus.CANCELLED.equals(orderStatus.getCode())
					|| OrderStatus.FILLED.equals(orderStatus.getCode())
					|| OrderStatus.INACTIVE.equals(orderStatus.getCode())) {
				return false;
			}
		}
		if ((columnNames[column] == SYMBOL)
				|| (columnNames[column] == ORDER_KEY)
				|| (columnNames[column] == STATUS)
				|| (columnNames[column] == AVG_PRICE)
				|| (columnNames[column] == FILLED_DATE)
				|| (columnNames[column] == FILLED_QTY)
				|| (columnNames[column] == STOP_PRICE)) {
			return false;
		}
		return true;
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

		if (columnNames[column] == LMT_PRICE
				&& null != super.getValueAt(row, column)) {
			if (((OrderType) super.getValueAt(row, 3)).isValid()) {
				if (((OrderType) super.getValueAt(row, 3)).getCode().equals(
						OrderType.MKT)
						|| ((OrderType) super.getValueAt(row, 3)).getCode()
								.equals(OrderType.STP)) {
					if (((Money) super.getValueAt(row, column)).doubleValue() != 0) {
						Money price = new Money(0);
						this.setValueAt(price, row, column);
						return price;
					}
				}
				if (((OrderType) super.getValueAt(row, 3)).getCode().equals(
						OrderType.LMT)
						|| ((OrderType) super.getValueAt(row, 3)).getCode()
								.equals(OrderType.STPLMT)) {
					if (((Money) super.getValueAt(row, column)).doubleValue() == 0) {
						Money price = new Money(this.getData()
								.getDatasetContainer().getBaseCandleSeries()
								.getContract().getLastPrice());
						this.setValueAt(price, row, column);
						return price;
					}
				}
			}
		}
		if (columnNames[column] == AUX_PRICE
				&& null != super.getValueAt(row, column)) {
			if (((OrderType) super.getValueAt(row, 3)).isValid()) {
				if (((OrderType) super.getValueAt(row, 3)).getCode().equals(
						OrderType.MKT)
						|| ((OrderType) super.getValueAt(row, 3)).getCode()
								.equals(OrderType.LMT)) {
					if (((Money) super.getValueAt(row, column)).doubleValue() != 0) {
						Money price = new Money(0);
						this.setValueAt(price, row, column);
						return price;
					}
				}
				if (((OrderType) super.getValueAt(row, 3)).getCode().equals(
						OrderType.STP)
						|| ((OrderType) super.getValueAt(row, 3)).getCode()
								.equals(OrderType.STPLMT)) {
					if (((Money) super.getValueAt(row, column)).doubleValue() == 0) {
						Money price = new Money(this.getData()
								.getDatasetContainer().getBaseCandleSeries()
								.getContract().getLastPrice());
						this.setValueAt(price, row, column);
						return price;
					}
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
	 * Method getData.
	 * 
	 * @return Tradestrategy
	 */
	public Tradestrategy getData() {
		return m_data;
	}

	/**
	 * Method setData.
	 * 
	 * @param data
	 *            Tradestrategy
	 */
	public void setData(Tradestrategy data) {
		this.m_data = data;
		this.clearAll();
		if (!getData().getTradeOrders().isEmpty()) {
			for (final TradeOrder tradeOrder : getData().getTradeOrders()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, tradeOrder);
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

		TradeOrder element = null;

		int i = 0;
		for (final TradeOrder tradeOrder : getData().getTradeOrders()) {
			if (i == row) {
				element = tradeOrder;
				break;
			}
			i++;
		}
		switch (column) {
		case 0: {
			element.getTradestrategy().getContract().setSymbol((String) value);
			break;
		}
		case 1: {
			element.setOrderKey(((Quantity) value).getIntegerValue());
			break;
		}
		case 2: {
			element.setAction(((Action) value).getCode());
			break;
		}
		case 3: {
			element.setOrderType(((OrderType) value).getCode());
			break;
		}
		case 4: {
			element.setQuantity(((Quantity) value).getIntegerValue());
			break;
		}
		case 5: {
			element.setLimitPrice(((Money) value).getBigDecimalValue());
			break;
		}
		case 6: {
			element.setAuxPrice(((Money) value).getBigDecimalValue());
			break;
		}
		case 7: {
			element.setTransmit(new Boolean(((YesNo) value).getCode()));
			break;
		}
		case 8: {
			element.setStatus(((OrderStatus) value).getCode());
			break;
		}
		case 9: {
			element.setOcaGroupName((String) value);
			break;
		}
		case 10: {
			element.setAverageFilledPrice(((Decimal) value)
					.getBigDecimalValue());
			break;
		}
		case 11: {
			element.setFilledDate(((Date) value).getDate());
			break;
		}
		case 12: {
			element.setFilledQuantity(((Quantity) value).getIntegerValue());
			break;
		}
		case 13: {
			element.setStopPrice(((Money) value).getBigDecimalValue());
			break;
		}
		case 14: {
			if (value instanceof TradeOrder) {
				element.setFAProfile(((TradeOrder) value).getFAProfile());
				element.setFAGroup(((TradeOrder) value).getFAGroup());
				element.setFAMethod(((TradeOrder) value).getFAMethod());
				element.setFAPercent(((TradeOrder) value).getFAPercent());
			} else {
				element.setFAProfile(null);
				element.setFAGroup(null);
				element.setFAMethod(null);
				element.setFAPercent(null);
			}
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

		Integer orderKey = ((Quantity) this.getValueAt(selectedRow, 1))
				.getIntegerValue();
		for (final TradeOrder tradeOrder : getData().getTradeOrders()) {
			if (CoreUtils
					.nullSafeComparator(tradeOrder.getOrderKey(), orderKey) == 0) {
				getData().getTradeOrders().remove(tradeOrder);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				this.fireTableRowsDeleted(selectedRow, selectedRow);
				break;
			}
		}
	}

	/**
	 * Method addRow.
	 * 
	 * @param element
	 *            TradeOrder
	 */
	public void addRow(TradeOrder element) {
		getData().addTradeOrder(element);
		final Vector<Object> newRow = new Vector<Object>();
		getNewRow(newRow, element);
		rows.add(newRow);
		this.fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
	}

	public void addRow() {
		final Tradestrategy tradestrategy = getData();

		String side = tradestrategy.getSide();
		if (null == side)
			side = Side.BOT;
		String orderType = OrderType.STPLMT;
		String action = Action.BUY;
		if (Side.SLD.equals(side)) {
			action = Action.SELL;
		}
		double risk = tradestrategy.getRiskAmount().doubleValue();
		double stop = 1.0d;

		Money price = new Money(tradestrategy.getDatasetContainer()
				.getBaseCandleSeries().getContract().getLastPrice());

		Date createDate = new Date(new java.util.Date());

		final Entrylimit entrylimit = DAOEntryLimit.newInstance().getValue(
				price);

		Money limitPrice = Action.BUY.equals(action) ? price.add(new Money(
				entrylimit.getLimitAmount().doubleValue())) : price
				.subtract(new Money(entrylimit.getLimitAmount().doubleValue()));

		int quantity = (int) ((int) risk / stop);
		if (tradestrategy.isThereOpenTradePosition()) {
			for (TradeOrder item : tradestrategy.getTradeOrders()) {
				if (item.hasTradePosition()) {
					TradePosition position = item.getTradePosition();
					if (position.getIsOpen()) {
						quantity = Math.abs(position.getOpenQuantity());
						side = position.getSide();
						action = Action.BUY;
						if (Side.BOT.equals(side)) {
							action = Action.SELL;
						}
						orderType = OrderType.LMT;
						limitPrice = price;
					}
				}
			}
		}

		final TradeOrder tradeOrder = new TradeOrder(tradestrategy, action,
				orderType, quantity, price.getBigDecimalValue(),
				limitPrice.getBigDecimalValue(), createDate.getDate());
		tradeOrder.setOcaGroupName("");
		tradeOrder.setTransmit(true);
		tradeOrder.setStatus(OrderStatus.UNSUBMIT);
		tradestrategy.addTradeOrder(tradeOrder);

		final Vector<Object> newRow = new Vector<Object>();
		getNewRow(newRow, tradeOrder);
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
	 *            TradeOrder
	 */
	public void getNewRow(Vector<Object> newRow, TradeOrder element) {

		newRow.addElement(element.getTradestrategy().getContract().getSymbol());
		newRow.addElement(new Quantity(element.getOrderKey()));
		if (null == element.getAction()) {
			newRow.addElement(new Action());
		} else {
			newRow.addElement(Action.newInstance(element.getAction()));
		}
		if (null == element.getOrderType()) {
			newRow.addElement(new OrderType());
		} else {
			newRow.addElement(OrderType.newInstance(element.getOrderType()));
		}

		newRow.addElement(new Quantity(element.getQuantity()));
		newRow.addElement(new Money(element.getLimitPrice()));
		newRow.addElement(new Money(element.getAuxPrice()));
		newRow.addElement(YesNo.newInstance(element.getTransmit()));
		if (null == element.getStatus()) {
			newRow.addElement(new OrderStatus());
		} else {
			newRow.addElement(OrderStatus.newInstance(element.getStatus()));
		}
		if (null == element.getOcaGroupName()) {
			newRow.addElement("");
		} else {
			newRow.addElement(element.getOcaGroupName());
		}
		if (null == element.getAverageFilledPrice()) {
			newRow.addElement(new Decimal(3));
		} else {
			newRow.addElement(new Decimal(element.getAverageFilledPrice(), 3));
		}
		if (null == element.getFilledDate()) {
			newRow.addElement(new Date());
		} else {
			newRow.addElement(new Date(element.getFilledDate()));
		}
		newRow.addElement(new Quantity(element.getFilledQuantity()));
		newRow.addElement(new Money(element.getStopPrice()));
		TradeOrder tradeOrder = new TradeOrder();
		tradeOrder.setIdTradeOrder(1);
		if (null == element.getFAProfile()) {
			newRow.addElement(tradeOrder);
		} else {
			tradeOrder.setFAProfile(element.getFAProfile());
			tradeOrder.setFAGroup(element.getFAGroup());
			tradeOrder.setFAMethod(element.getFAMethod());
			tradeOrder.setFAPercent(element.getFAPercent());
			newRow.addElement(tradeOrder);
		}
	}
}
