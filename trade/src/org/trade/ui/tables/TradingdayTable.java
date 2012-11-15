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
package org.trade.ui.tables;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.MarketBar;
import org.trade.ui.base.Table;
import org.trade.ui.base.TableModel;
import org.trade.ui.widget.DateEditor;
import org.trade.ui.widget.DateRenderer;
import org.trade.ui.widget.DecodeTableEditor;

/**
 */
public class TradingdayTable extends Table {

	private static final long serialVersionUID = 1132297931453070904L;

	private static final String DATETIMEFORMAT = "MM/dd/yyyy HH:mm";

	/**
	 * Constructor for TradingdayTable.
	 * 
	 * @param model
	 *            TableModel
	 * @throws ValueTypeException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TradingdayTable(TableModel model) throws ValueTypeException {
		super(model);
		DecodeTableEditor marketBarEditor = new DecodeTableEditor(
				new JComboBox((new MarketBar()).getCodesDecodes()));
		this.setDefaultEditor(MarketBar.class, marketBarEditor);
		DateRenderer rDate = new DateRenderer(DATETIMEFORMAT);
		DateEditor eDate = new DateEditor(new org.trade.core.valuetype.Date(
				new Date()), DATETIMEFORMAT, Calendar.MINUTE);
		this.setDefaultRenderer(org.trade.core.valuetype.Date.class, rDate);
		this.setDefaultEditor(org.trade.core.valuetype.Date.class, eDate);
		this.setFont(new Font("Monospaced", Font.PLAIN, 12));
		this.setPreferredScrollableViewportSize(new Dimension(250, 40));
		this.setFillsViewportHeight(true);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				model);
		this.setRowSorter(sorter);
		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
		sorter.setSortKeys(sortKeys);
	}
}
