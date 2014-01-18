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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.persistent.dao.Strategy;
import org.trade.ui.widget.DAODecodeComboBoxEditor;
import org.trade.ui.widget.DecodeComboBoxEditor;
import org.trade.ui.widget.DecodeComboBoxRenderer;

/**
 */
public class FilterTradestrategyPane extends JPanel {

	private static final long serialVersionUID = -4696247761711464150L;

	private DAODecodeComboBoxEditor filterStrategyEditorComboBox = null;
	private DecodeComboBoxEditor filterChartDaysEditorComboBox = null;
	private DecodeComboBoxEditor filterBarSizeEditorComboBox = null;

	public FilterTradestrategyPane() throws ValueTypeException {

		filterStrategyEditorComboBox = new DAODecodeComboBoxEditor(
				(new DAOStrategy()).getCodesDecodes());
		filterStrategyEditorComboBox.setRenderer(new DecodeComboBoxRenderer());
		filterStrategyEditorComboBox.setEditable(true);

		filterChartDaysEditorComboBox = new DecodeComboBoxEditor(
				(new ChartDays()).getCodesDecodes());
		filterChartDaysEditorComboBox.setRenderer(new DecodeComboBoxRenderer());
		filterChartDaysEditorComboBox.setEditable(true);

		filterBarSizeEditorComboBox = new DecodeComboBoxEditor(
				(new ChartDays()).getCodesDecodes());
		filterBarSizeEditorComboBox.setRenderer(new DecodeComboBoxRenderer());
		filterBarSizeEditorComboBox.setEditable(true);

		GridBagLayout gridBagLayout1 = new GridBagLayout();
		JPanel jPanel1 = new JPanel(gridBagLayout1);
		this.setLayout(new BorderLayout());
		JLabel jLabel1 = new JLabel("Strategy:");
		jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel1.setHorizontalTextPosition(SwingConstants.RIGHT);
		JLabel jLabel2 = new JLabel("Chart Days:");
		jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabel2.setHorizontalTextPosition(SwingConstants.RIGHT);
		JLabel jLabel3 = new JLabel("Bar Size:");
		jLabel3.setHorizontalTextPosition(SwingConstants.RIGHT);
		jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);

		jPanel1.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(1,
						1, 0, 0), 20, 5));
		jPanel1.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						1, 0, 0), 20, 5));
		jPanel1.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						1, 0, 0), 20, 5));

		jPanel1.add(filterStrategyEditorComboBox, new GridBagConstraints(1, 0,
				1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 43), 196, 0));
		jPanel1.add(filterStrategyEditorComboBox, new GridBagConstraints(1, 1,
				1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(1, 0, 0, 43), 196, 0));
		jPanel1.add(filterBarSizeEditorComboBox, new GridBagConstraints(1, 2,
				1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 43), 196, 0));
		this.add(jPanel1);
	}

	/**
	 * Method getStrategy.
	 * 
	 * @return Strategy
	 */
	public Strategy getStrategy() {
		return ((Strategy) ((DAOStrategy) filterStrategyEditorComboBox
				.getSelectedItem()).getObject());
	}

	/**
	 * Method getChartDays
	 * 
	 * @return Integer
	 */
	public Integer getChartDays() {
		String chartDays = ((String) ((ChartDays) filterChartDaysEditorComboBox
				.getSelectedItem()).getObject());
		if (chartDays.length() > 0)
			return Integer.parseInt(chartDays);
		return null;
	}

	/**
	 * Method getBarSize
	 * 
	 * @return Integer
	 */
	public Integer getBarSize() {
		String chartDays = ((String) ((ChartDays) filterBarSizeEditorComboBox
				.getSelectedItem()).getObject());
		if (chartDays.length() > 0)
			return Integer.parseInt(chartDays);
		return null;
	}
}
