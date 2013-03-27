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
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.text.MaskFormatter;

import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.DAOStrategyManager;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.SECIdType;
import org.trade.dictionary.valuetype.SECType;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.Tier;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.dao.Contract;
import org.trade.strategy.StrategyRule;
import org.trade.ui.base.Table;
import org.trade.ui.base.TableModel;
import org.trade.ui.models.TradestrategyTableModel;
import org.trade.ui.tables.renderer.DAOStrategyManagerRenderer;
import org.trade.ui.tables.renderer.DAOStrategyRenderer;
import org.trade.ui.widget.DateEditor;
import org.trade.ui.widget.DateField;
import org.trade.ui.widget.DateRenderer;
import org.trade.ui.widget.DecodeTableEditor;
import org.trade.ui.widget.StringEditor;
import org.trade.ui.widget.StringField;
import org.trade.ui.widget.StringRenderer;

/**
 */
public class TradestrategyTable extends Table {

	private static final long serialVersionUID = 1132297931453070904L;
	private static final String MASK = "**********";
	private static final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789. ";
	private static final String PLACE_HOLDER = " ";
	private static final String DATETIMEFORMAT = "MM/yyyy";

	/**
	 * Constructor for TradestrategyTable.
	 * 
	 * @param model
	 *            TableModel
	 * @param strategyWorkers
	 *            ConcurrentHashMap<String,StrategyRule>
	 * @throws ValueTypeException
	 * @throws ParseException
	 */

	public TradestrategyTable(TableModel model,
			ConcurrentHashMap<String, StrategyRule> strategyWorkers)
			throws ValueTypeException, ParseException {
		super(model);
		DateRenderer rDate = new DateRenderer(DATETIMEFORMAT);
		DateEditor eDate = new DateEditor(new DateField(DATETIMEFORMAT),
				new org.trade.core.valuetype.Date(new Date()), DATETIMEFORMAT,
				Calendar.MONTH);

		this.getColumn("Expiry").setCellEditor(eDate);
		this.getColumn("Expiry").setCellRenderer(rDate);
		DecodeTableEditor currencyEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new Currency()).getCodesDecodes()));
		DecodeTableEditor exchangeEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new Exchange()).getCodesDecodes()));
		DecodeTableEditor sECIdTypeEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new SECIdType()).getCodesDecodes()));
		DecodeTableEditor sECTypeEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new SECType()).getCodesDecodes()));
		DecodeTableEditor sideEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new Side()).getCodesDecodes()));
		DecodeTableEditor tradestrategyStatusEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new TradestrategyStatus())
								.getCodesDecodes()));
		DecodeTableEditor tierEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new Tier()).getCodesDecodes()));

		DAOStrategyRenderer dAOStrategyRenderer = new DAOStrategyRenderer(
				strategyWorkers);
		DAOStrategyManagerRenderer dAOStrategyManagerRenderer = new DAOStrategyManagerRenderer(
				strategyWorkers);
		DecodeTableEditor strategyEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new DAOStrategy()).getCodesDecodes()));
		JComboBox<Decode> strategyManagerComboBox = new JComboBox<Decode>(
				(Vector<Decode>) (new DAOStrategyManager()).getCodesDecodes());
		DecodeTableEditor strategyManagerEditor = new DecodeTableEditor(
				strategyManagerComboBox);
		DecodeTableEditor portfolioEditor = new DecodeTableEditor(
				new JComboBox<Decode>((Vector<Decode>) (new DAOPortfolio())
						.getCodesDecodes()));
		DecodeTableEditor chartDaysEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new ChartDays()).getCodesDecodes()));
		DecodeTableEditor barSizeEditor = new DecodeTableEditor(
				new JComboBox<Decode>(
						(Vector<Decode>) (new BarSize()).getCodesDecodes()));

		StringEditor eString = new StringEditor(new StringField(
				new MaskFormatter(MASK), VALID_CHARS, PLACE_HOLDER));
		this.setDefaultEditor(String.class, eString);
		StringRenderer rString = new StringRenderer();
		this.setDefaultRenderer(String.class, rString);
		this.setDefaultEditor(DAOStrategy.class, strategyEditor);
		this.setDefaultEditor(DAOStrategyManager.class, strategyManagerEditor);
		this.setDefaultEditor(DAOPortfolio.class, portfolioEditor);
		this.setDefaultEditor(Currency.class, currencyEditor);
		this.setDefaultEditor(Exchange.class, exchangeEditor);
		this.setDefaultEditor(SECIdType.class, sECIdTypeEditor);
		this.setDefaultEditor(SECType.class, sECTypeEditor);
		this.setDefaultEditor(Side.class, sideEditor);
		this.setDefaultEditor(Tier.class, tierEditor);
		this.setDefaultEditor(TradestrategyStatus.class,
				tradestrategyStatusEditor);
		this.setDefaultRenderer(DAOStrategy.class, dAOStrategyRenderer);
		this.setDefaultRenderer(DAOStrategyManager.class,
				dAOStrategyManagerRenderer);
		this.setDefaultEditor(ChartDays.class, chartDaysEditor);
		this.setDefaultEditor(BarSize.class, barSizeEditor);
		this.setFont(new Font("Monospaced", Font.PLAIN, 12));
		this.setPreferredScrollableViewportSize(new Dimension(300, 200));
		this.setFillsViewportHeight(true);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	/**
	 * Method getToolTipText. Implement table cell tool tips.
	 * 
	 * @param e
	 *            MouseEvent
	 * 
	 * @return String
	 */
	public String getToolTipText(MouseEvent e) {
		StringBuilder result = null;
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		if (rowIndex > -1) {
			int realRowIndex = convertRowIndexToModel(rowIndex);
			int realColumnIndex = convertColumnIndexToModel(colIndex);

			Contract contract = ((TradestrategyTableModel) this.getModel())
					.getData().getTradestrategies().get(realRowIndex)
					.getContract();

			if (realColumnIndex == 2) { // Symbol column result = new
				result = new StringBuilder("<html>");
				result.append("<b>Symbol: </b> ").append(contract.getSymbol())
						.append("<br/>");
				result.append("<b>Descrption: </b> ")
						.append(contract.getDescription()).append("<br/>");
				result.append("<b>Primary Exch: </b> ")
						.append(contract.getPrimaryExchange()).append("<br/>");
				result.append("<b>Category: </b> ")
						.append(contract.getCategory()).append("<br/>");
				result.append("<b>Industry: </b> ")
						.append(contract.getIndustry()).append("<br/>");
				result.append("<b>Sub Category: </b> ")
						.append(contract.getSubCategory()).append("<br/>");
				result.append("<b>Min Tick: </b> ")
						.append(contract.getMinTick()).append("<br/>");
				result.append("<b>Trading Class: </b> ")
						.append(contract.getTradingClass())
						.append("<br/></html>");
				return result.toString();
			}
		}
		return null;
	}
}
