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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.Timer;

import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Percent;
import org.trade.core.valuetype.YesNo;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.DAOStrategyManager;
import org.trade.dictionary.valuetype.DAOTradeAccount;
import org.trade.dictionary.valuetype.Exchange;
import org.trade.dictionary.valuetype.SECType;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.Tier;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.TradeAccount;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.ui.base.TableModel;

/**
 */
public class TradestrategyTableModel extends TableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3087514589731145479L;

	private static final String TRADE = "Trade";
	private static final String SYMBOL = "Symbol*";
	private static final String DATE = "Date*";
	private static final String SIDE = "Side";
	private static final String TIER = "Tier";
	private static final String STRATEGY = "      Strategy*     ";
	private static final String STRATEGY_MGR = "   Strategy Manager ";
	private static final String ACCOUNT = "Account*";
	private static final String BAR_SIZE = "Bar Size*";
	private static final String CHART_HISTORY = "Chart Hist*";
	private static final String RISK_AMOUNT = "Risk Amt*";
	private static final String PERCENTCHGFRCLOSE = "%Chg Close";
	private static final String PERCENTCHGFROPEN = "%Chg Open";
	private static final String STATUS = "     Status     ";
	private static final String CURRENCY = "Currency*";
	private static final String EXCHANGE = "Exchange*";
	private static final String SEC_TYPE = "SEC Type*";
	private static final String EXPIRY = "Expiry";

	private static final String[] columnHeaderToolTip = { "Trading day",
			"Run strategy", null, "Trade bias", "For gaps the grade", null,
			null, "Trading account", "Bar size for strategy",
			"Historical data to pull in i.e 2D is today + yesterday",
			"Risk amount for trade used to calculate position size",
			"% Change from close", "% Change from open",
			"Tradestrategy status", null, null, null,
			"Expiry date for future contracts" };

	private Tradingday m_data = null;
	private Timer timer = null;

	public TradestrategyTableModel() {
		super(columnHeaderToolTip);
		columnNames = new String[18];
		columnNames[0] = DATE;
		columnNames[1] = TRADE;
		columnNames[2] = SYMBOL;
		columnNames[3] = SIDE;
		columnNames[4] = TIER;
		columnNames[5] = STRATEGY;
		columnNames[6] = STRATEGY_MGR;
		columnNames[7] = ACCOUNT;
		columnNames[8] = BAR_SIZE;
		columnNames[9] = CHART_HISTORY;
		columnNames[10] = RISK_AMOUNT;
		columnNames[11] = PERCENTCHGFRCLOSE;
		columnNames[12] = PERCENTCHGFROPEN;
		columnNames[13] = STATUS;
		columnNames[14] = CURRENCY;
		columnNames[15] = EXCHANGE;
		columnNames[16] = SEC_TYPE;
		columnNames[17] = EXPIRY;

		/*
		 * Create a 5sec timer to refresh the data this is used for the % chg,
		 * strategy and status fields.
		 */
		timer = new Timer(5000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < getRowCount(); i++) {
					fireTableCellUpdated(i, 5);
					fireTableCellUpdated(i, 6);
					fireTableCellUpdated(i, 11);
					fireTableCellUpdated(i, 12);
					fireTableCellUpdated(i, 13);
				}
			}
		});
	}

	/**
	 * Method getData.
	 * 
	 * @return Tradingday
	 */
	public Tradingday getData() {
		return this.m_data;
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

		Tradestrategy element = getData().getTradestrategies().get(row);
		if (null != element) {
			if (!element.getTrades().isEmpty()) {
				return false;
			}
		}

		if ((columnNames[column] == DATE)
				|| (columnNames[column] == STRATEGY_MGR)
				|| (columnNames[column] == PERCENTCHGFRCLOSE)
				|| (columnNames[column] == PERCENTCHGFROPEN)
				|| (columnNames[column] == STATUS)) {
			return false;
		}
		return true;
	}

	/**
	 * Method getValueAt.
	 * 
	 * 1 Y 1 day
	 * 
	 * 6 M 1 day
	 * 
	 * 3 M 1 day
	 * 
	 * 1 M 1 day, 1 hour
	 * 
	 * 1 W 1 day, 1 hour, 30 mins, 15 mins 2 D 1 hour, 30 mins, 15 mins, 3 mins,
	 * 2 mins, 1 min
	 * 
	 * 1 D 1 hour, 30 mins, 15 mins, 5 mins 3 mins, 2 mins, 1 min, 30 secs
	 * 
	 * 
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @return value Object
	 */

	public Object getValueAt(int row, int column) {
		if (columnNames[column] == CHART_HISTORY) {
			Integer barSize = new Integer(
					((BarSize) super.getValueAt(row, 8)).getCode());
			ChartDays chartDays = (ChartDays) super.getValueAt(row, column);
			Integer period = new Integer(chartDays.getCode());
			if (null != barSize && null != period) {
				if (barSize.equals(new Integer(30)) && period > 1) {
					chartDays = ChartDays.newInstance(new Integer(1));
				} else if ((barSize <= 1800 || barSize == 1) && period > 5) {
					chartDays = ChartDays.newInstance(new Integer(5));
				} else if ((barSize == 3600 || barSize == 1) && period > 30) {
					chartDays = ChartDays.newInstance(new Integer(30));
				}
				this.populateDAO(chartDays, row, column);
				return chartDays;
			}
		}
		if (columnNames[column] == BAR_SIZE) {
			BarSize barSize = (BarSize) super.getValueAt(row, column);
			Integer bar = new Integer(barSize.getCode());
			Integer period = new Integer(
					((ChartDays) super.getValueAt(row, 9)).getCode());
			if (null != barSize && null != period) {
				if (period > 1 && (bar < 60 && bar != 1)) {
					barSize = BarSize.newInstance(new Integer(60));
				} else if (period > 5 && (bar < 3600 && bar != 1)) {
					barSize = BarSize.newInstance(new Integer(3600));
				} else if (period > 30 && bar != 1) {
					barSize = BarSize.newInstance(new Integer(1));
				}
				this.populateDAO(barSize, row, column);
				return barSize;
			}
		}
		return super.getValueAt(row, column);
	}

	/**
	 * Method setData.
	 * 
	 * @param data
	 *            Tradingday
	 */
	public void setData(Tradingday data) {
		if (timer.isRunning())
			timer.stop();
		this.m_data = data;
		this.clearAll();
		if (null != getData().getTradestrategies()
				&& !getData().getTradestrategies().isEmpty()) {
			for (final Tradestrategy element : getData().getTradestrategies()) {
				final Vector<Object> newRow = new Vector<Object>();
				getNewRow(newRow, element);
				rows.add(newRow);
			}
			fireTableDataChanged();
		}
		timer.start();
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
		Tradestrategy element = getData().getTradestrategies().get(row);

		switch (column) {
		case 0: {
			element.getTradingday().setOpen(((Date) value).getDate());
			break;
		}
		case 1: {
			element.setTrade(new Boolean(((YesNo) value).getCode()));
			break;
		}
		case 2: {
			element.getContract().setSymbol(
					((String) value).trim().toUpperCase());
			break;
		}
		case 3: {
			element.setSide(((Side) value).getCode());
			break;
		}
		case 4: {
			element.setTier(((Tier) value).getCode());
			break;
		}
		case 5: {
			final Strategy strategy = (Strategy) ((DAOStrategy) value)
					.getObject();
			element.setStrategy(strategy);

			this.setValueAt(DAOStrategyManager.newInstance(strategy
					.getStrategyManager().getName()), row, column + 1);
			break;
		}
		case 6: {
			element.getStrategy().setStrategyManager(
					(Strategy) ((DAOStrategyManager) value).getObject());
			break;
		}
		case 7: {
			TradeAccount tradeAccount = (TradeAccount) ((DAOTradeAccount) value)
					.getObject();
			element.setTradeAccount(tradeAccount);
			break;
		}
		case 8: {
			element.setBarSize(new Integer(((BarSize) value).getCode()));
			break;
		}
		case 9: {
			element.setChartDays(new Integer(((ChartDays) value).getCode()));
			break;
		}
		case 10: {
			element.setRiskAmount(((Money) value).getBigDecimalValue());
			break;
		}
		case 11: {
			// element.setPercentChangeFromClose((((Percent) value)
			// .getBigDecimalValue()));
			break;
		}
		case 12: {
			// element.setPercentChangeFromOpen((((Percent) value)
			// .getBigDecimalValue()));
			break;
		}
		case 13: {
			element.setStatus(((TradestrategyStatus) value).getCode());
			break;
		}
		case 14: {
			element.getContract().setCurrency(((Currency) value).getCode());
			break;
		}
		case 15: {
			element.getContract().setExchange(((Exchange) value).getCode());
			break;
		}
		case 16: {
			element.getContract().setSecType(((SECType) value).getCode());
			break;
		}
		case 17: {
			element.getContract().setExpiry(
					TradingCalendar.addDays(TradingCalendar.addMonth(
							((Date) value).getDate(), 1), -1));
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

		int i = 0;
		for (final Tradestrategy element : getData().getTradestrategies()) {
			if (i == selectedRow) {
				getData().getTradestrategies().remove(element);
				final Vector<Object> currRow = rows.get(selectedRow);
				rows.remove(currRow);
				getData().setDirty(true);
				fireTableChanged(new TableModelEvent(this));
				break;
			}
			i++;
		}
	}

	public void addRow() {

		Tradingday tradingday = getData();
		Tradestrategy tradestrategy = null;
		String strategyName = null;
		Strategy strategy = (Strategy) DAOStrategy.newInstance().getObject();
		Integer chartDays = ChartDays.TWO_DAYS;
		Integer barSize = BarSize.FIVE_MIN;
		Integer riskAmount = new Integer(0);
		if (null != tradingday) {
			try {

				chartDays = ConfigProperties
						.getPropAsInt("trade.backfill.duration");
				barSize = ConfigProperties
						.getPropAsInt("trade.backfill.barsize");
				riskAmount = ConfigProperties.getPropAsInt("trade.risk");
				strategyName = ConfigProperties
						.getPropAsString("trade.strategy.default");
				if (null != strategyName) {
					strategy = (Strategy) DAOStrategy.newInstance(strategyName)
							.getObject();
				}
				tradestrategy = Tradingdays.parseContractLine(ConfigProperties
						.getPropAsString("trade.tradingtab.default.add"));

			} catch (Exception e) {
				// Do nothing
			}

			TradeAccount tradeAccount = (TradeAccount) DAOTradeAccount
					.newInstance().getObject();
			if (null == tradestrategy) {
				tradestrategy = new Tradestrategy(new Contract(SECType.STOCK,
						"", Exchange.SMART, Currency.USD, null, null),
						tradingday, strategy, tradeAccount, new BigDecimal(
								riskAmount), null, null, true, chartDays,
						barSize);
			} else {
				tradestrategy.setTradingday(tradingday);
			}

			tradestrategy.setRiskAmount(new BigDecimal(riskAmount));
			tradestrategy.setBarSize(barSize);
			tradestrategy.setChartDays(chartDays);
			tradestrategy.setTrade(true);
			tradestrategy.setDirty(true);
			tradestrategy.setStrategy(strategy);
			tradestrategy.setTradeAccount(tradeAccount);

			getData().getTradestrategies().add(tradestrategy);
			Vector<Object> newRow = new Vector<Object>();

			getNewRow(newRow, tradestrategy);
			rows.add(newRow);
			// Tell the listeners a new table has arrived.
			fireTableChanged(new TableModelEvent(this));
		}
	}

	/**
	 * Method getNewRow.
	 * 
	 * @param newRow
	 *            Vector<Object>
	 * @param element
	 *            Tradestrategy
	 */
	public void getNewRow(Vector<Object> newRow, Tradestrategy element) {

		newRow.addElement(new Date(element.getTradingday().getOpen()));
		newRow.addElement(YesNo.newInstance(element.getTrade()));
		newRow.addElement(element.getContract().getSymbol());
		if (null == element.getSide()) {
			newRow.addElement(new Side());
		} else {
			newRow.addElement(Side.newInstance(element.getSide()));
		}
		if (null == element.getTier()) {
			newRow.addElement(new Tier());
		} else {
			newRow.addElement(Tier.newInstance(element.getTier()));
		}
		newRow.addElement(DAOStrategy.newInstance(element.getStrategy()
				.getName()));
		newRow.addElement(DAOStrategyManager.newInstance(element.getStrategy()
				.getStrategyManager().getName()));
		newRow.addElement(DAOTradeAccount.newInstance(element.getTradeAccount()
				.getName()));
		newRow.addElement(BarSize.newInstance(element.getBarSize()));
		newRow.addElement(ChartDays.newInstance(element.getChartDays()));
		newRow.addElement(new Money(element.getRiskAmount()));
		/*
		 * TODO If the id is null then this element has not been saved and so
		 * the DatasetContainer cannot be created. This is due to an issue with
		 * hibernate and Eager fetch.
		 */
		if (null != element.getIdTradeStrategy()) {
			newRow.addElement(element.getDatasetContainer()
					.getBaseCandleSeries().getPercentChangeFromClose());
			newRow.addElement(element.getDatasetContainer()
					.getBaseCandleSeries().getPercentChangeFromOpen());
		} else {
			newRow.addElement(new Percent(0));
			newRow.addElement(new Percent(0));
		}
		newRow.addElement(element.getTradestrategyStatus());
		newRow.addElement(Currency.newInstance(element.getContract()
				.getCurrency()));
		newRow.addElement(Exchange.newInstance(element.getContract()
				.getExchange()));
		newRow.addElement(SECType.newInstance(element.getContract()
				.getSecType()));
		if (null == element.getContract().getExpiry()) {
			newRow.addElement(new Date());
		} else {
			newRow.addElement(new Date(element.getContract().getExpiry()));
		}
	}
}
