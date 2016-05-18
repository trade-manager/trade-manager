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
package org.trade.ui.tradingday;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.ui.widget.ComboItem;
import org.trade.ui.widget.DecodeComboBoxRenderer;

/**
 */
public class FilterBackTestPane extends JPanel {

	private static final long serialVersionUID = -4696247761711464150L;

	private JComboBox<ComboItem> strategyBarSizeChartHistComboBox = null;
	private JList<ComboItem> contractsHistList = null;
	private JSpinner spinnerStart = new JSpinner();
	private JSpinner spinnerEnd = new JSpinner();

	private ComboItem comboItemAll = new ComboItem(null, "All");

	private static final String DATEFORMAT = "MM/dd/yyyy";

	public FilterBackTestPane(ZonedDateTime startDate, ZonedDateTime endDate,
			List<Tradestrategy> strategyBarSizeChartHistItems, List<Tradestrategy> contractItems)
					throws ValueTypeException {

		Vector<ComboItem> items = new Vector<ComboItem>();
		for (Tradestrategy item : strategyBarSizeChartHistItems) {
			String label = item.getStrategy().getName() + " " + BarSize.newInstance(item.getBarSize()).getDisplayName()
					+ " " + ChartDays.newInstance(item.getChartDays()).getDisplayName();
			ComboItem comboItem = new ComboItem(item, label.trim());
			items.add(comboItem);
		}

		DefaultListModel<ComboItem> listModel = new DefaultListModel<ComboItem>();

		listModel.addElement(comboItemAll);
		for (Tradestrategy item : contractItems) {
			String label = item.getContract().getSymbol();
			ComboItem comboItem = new ComboItem(item, label);
			listModel.addElement(comboItem);
		}

		JLabel dateStartLabel = new JLabel("From Date: ");
		dateStartLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		dateStartLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		JLabel dateEndLabel = new JLabel("To Date: ");
		dateEndLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		dateEndLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
		JLabel jLabel1 = new JLabel("Combinations: ");
		jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel1.setHorizontalTextPosition(SwingConstants.RIGHT);
		JLabel jLabel2 = new JLabel("Contracts: ");
		jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel2.setHorizontalTextPosition(SwingConstants.RIGHT);

		spinnerStart.setModel(new SpinnerDateModel());
		JSpinner.DateEditor de = new JSpinner.DateEditor(spinnerStart, DATEFORMAT);
		spinnerStart.setEditor(de);
		spinnerStart.setValue((new Date(startDate)).getDate());

		spinnerEnd.setModel(new SpinnerDateModel());
		JSpinner.DateEditor de1 = new JSpinner.DateEditor(spinnerEnd, DATEFORMAT);
		spinnerEnd.setEditor(de1);
		spinnerEnd.setValue((new Date(endDate)).getDate());

		strategyBarSizeChartHistComboBox = new JComboBox<ComboItem>(items);
		strategyBarSizeChartHistComboBox.setRenderer(new DecodeComboBoxRenderer());
		strategyBarSizeChartHistComboBox.setEditable(true);

		contractsHistList = new JList<ComboItem>(listModel);
		contractsHistList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		contractsHistList.setLayoutOrientation(JList.VERTICAL);
		contractsHistList.setVisibleRowCount(-1);
		contractsHistList.setCellRenderer(new DecodeComboBoxRenderer());
		contractsHistList.setSelectedValue(comboItemAll, true);

		JScrollPane listScroller = new JScrollPane(contractsHistList);
		listScroller.setPreferredSize(new Dimension(50, 150));

		GridBagLayout gridBagLayout1 = new GridBagLayout();
		JPanel jPanel1 = new JPanel(gridBagLayout1);
		this.setLayout(new BorderLayout());

		jPanel1.add(dateStartLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 20, 5));
		jPanel1.add(dateEndLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 20, 5));
		jPanel1.add(jLabel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 20, 5));
		jPanel1.add(jLabel2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 20, 5));

		jPanel1.add(spinnerStart, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 20), 20, 5));
		jPanel1.add(spinnerEnd, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 20), 20, 5));
		jPanel1.add(strategyBarSizeChartHistComboBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 20), 20, 5));
		jPanel1.add(listScroller, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 20), 20, 5));
		this.add(jPanel1);
	}

	/**
	 * Method getSelectedStrategyBarSizeChartHist
	 * 
	 * @return Tradestrategy
	 */
	public Tradestrategy getSelectedStrategyBarSizeChartHist() {
		ComboItem comboItem = (ComboItem) strategyBarSizeChartHistComboBox.getSelectedItem();
		if (null == comboItem)
			return null;

		Tradestrategy tradestrategy = ((Tradestrategy) comboItem.getValue());
		return tradestrategy;
	}

	/**
	 * Method getSelectedContracts
	 * 
	 * @return List<Contract>
	 */
	public List<Contract> getSelectedContracts() {
		List<Contract> contracts = new ArrayList<Contract>(0);
		List<ComboItem> comboItems = contractsHistList.getSelectedValuesList();

		if (null == comboItems)
			return contracts;
		for (ComboItem item : comboItems) {
			if (comboItemAll.equals(item))
				return new ArrayList<Contract>(0);
			Tradestrategy tradestrategy = (Tradestrategy) item.getValue();
			contracts.add(tradestrategy.getContract());
		}

		return contracts;
	}

	/**
	 * Method getSelectedStartDate
	 * 
	 * @return ZonedDateTime
	 */
	public ZonedDateTime getSelectedStartDate() {

		ZonedDateTime startDate = TradingCalendar
				.getZonedDateTimeFromMilli(((java.util.Date) spinnerStart.getValue()).getTime());
		startDate = TradingCalendar.getDateAtTime(startDate, 0, 0, 0);

		ZonedDateTime endDate = TradingCalendar
				.getZonedDateTimeFromMilli(((java.util.Date) spinnerEnd.getValue()).getTime());
		endDate = TradingCalendar.getDateAtTime(endDate, 23, 59, 59);

		if (endDate.isBefore(startDate)) {
			startDate = TradingCalendar.getDateAtTime(endDate, 0, 0, 0);
			spinnerStart.setValue((new Date(startDate)).getDate());
		}

		return startDate;
	}

	/**
	 * Method getSelectedEndDate
	 * 
	 * @return ZonedDateTime
	 */
	public ZonedDateTime getSelectedEndDate() {
		ZonedDateTime startDate = TradingCalendar
				.getZonedDateTimeFromMilli(((java.util.Date) spinnerStart.getValue()).getTime());
		startDate = TradingCalendar.getDateAtTime(startDate, 0, 0, 0);

		ZonedDateTime endDate = TradingCalendar
				.getZonedDateTimeFromMilli(((java.util.Date) spinnerEnd.getValue()).getTime());
		endDate = TradingCalendar.getDateAtTime(endDate, 23, 59, 59);
		if (endDate.isBefore(startDate)) {
			startDate = TradingCalendar.getDateAtTime(endDate, 0, 0, 0);
			spinnerStart.setValue((new Date(startDate)).getDate());
		}

		return endDate;
	}
}
