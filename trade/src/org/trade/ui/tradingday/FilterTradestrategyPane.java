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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.ui.widget.ComboItem;
import org.trade.ui.widget.DecodeComboBoxRenderer;

/**
 */
public class FilterTradestrategyPane extends JPanel {

	private static final long serialVersionUID = -4696247761711464150L;

	private JComboBox<ComboItem> comboBox = null;

	public FilterTradestrategyPane(List<Tradestrategy> values)
			throws ValueTypeException {
		Vector<ComboItem> items = new Vector<ComboItem>();
		for (Tradestrategy item : values) {
			String label = item.getStrategy().getName()
					+ " "
					+ BarSize.newInstance(item.getBarSize()).getDisplayName()
					+ " "
					+ ChartDays.newInstance(item.getChartDays())
							.getDisplayName();
			ComboItem comboItem = new ComboItem(item, label);
			items.add(comboItem);
		}
		comboBox = new JComboBox<ComboItem>(items);
		comboBox.setRenderer(new DecodeComboBoxRenderer());
		comboBox.setEditable(true);

		GridBagLayout gridBagLayout1 = new GridBagLayout();
		JPanel jPanel1 = new JPanel(gridBagLayout1);
		this.setLayout(new BorderLayout());
		JLabel jLabel1 = new JLabel("Combinations:");
		jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel1.setHorizontalTextPosition(SwingConstants.RIGHT);

		jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 0, 0), 20, 5));

		jPanel1.add(comboBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 43), 196, 0));
		this.add(jPanel1);
	}

	/**
	 * Method getSelectedValue
	 * 
	 * @return Tradestrategy
	 */
	public Tradestrategy getSelectedValue() {
		Tradestrategy tradestrategy = ((Tradestrategy) ((ComboItem) comboBox
				.getSelectedItem()).getValue());
		return tradestrategy;
	}
}
