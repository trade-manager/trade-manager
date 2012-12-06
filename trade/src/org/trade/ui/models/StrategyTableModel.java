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
import org.trade.core.valuetype.YesNo;
import org.trade.dictionary.valuetype.DAOStrategyManager;
import org.trade.persistent.dao.Strategy;

/**
 */
public class StrategyTableModel extends AspectTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	private static final String NAME = "Name*";
	private static final String DESCRIPTION = "                      Description                     ";
	private static final String MARKET_DATA = "MarketData";
	private static final String CLASSNAME = "    Class Name*  ";
	private static final String STRATEGY_MANAGER_NAME = "Strategy Mgr Name";

	private static final String[] columnHeaderToolTip = {
			"The name of the strategy",
			null,
			"<html>The java class name for the strategy.<br>"
					+ "This file is stored in the strategy dir.<br>"
					+ "Note the dir is set in the config.properties (<b>trade.strategy.default.dir</b>)</html>",
			"The strategy manager used to managed the open position",
			"<html>If checked then TWS Mkt data api will run.<br>"
					+ "This will cause the strategy to fire if last price<br>"
					+ "falls outside the currents bars H/L</html>" };

	private Aspects m_data = null;

	public StrategyTableModel() {
		super(columnHeaderToolTip);

		// Get the column names and cache them.
		// Then we can close the connection.
		columnNames = new String[5];
		columnNames[0] = NAME;
		columnNames[1] = DESCRIPTION;
		columnNames[2] = CLASSNAME;
		columnNames[3] = STRATEGY_MANAGER_NAME;
		columnNames[4] = MARKET_DATA;
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
				getNewRow(newRow, (Strategy) element);
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

		final Strategy element = (Strategy) getData().getAspect().get(row);

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
			element.setClassName((String) value);
			break;
		}
		case 3: {
			if (value instanceof DAOStrategyManager) {
				element.setStrategyManager((Strategy) ((DAOStrategyManager) value)
						.getObject());
			} else {
				element.setStrategyManager(null);
			}
			break;
		}
		case 4: {
			element.setMarketData(new Boolean(((YesNo) value).getCode()));
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

		final Strategy element = new Strategy();
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
	 *            Strategy
	 */
	public void getNewRow(Vector<Object> newRow, Strategy element) {
		newRow.addElement(element.getName());
		newRow.addElement(element.getDescription());
		newRow.addElement(element.getClassName());
		if (null == element.getStrategyManager()) {
			newRow.addElement("");
		} else {
			newRow.addElement(DAOStrategyManager.newInstance(element
					.getStrategyManager().getName()));
		}
		if (null == element.getMarketData()) {
			newRow.addElement(new YesNo());
		} else {
			newRow.addElement(YesNo.newInstance(element.getMarketData()));
		}
	}
}
