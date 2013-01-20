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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.DAOStrategyManager;
import org.trade.dictionary.valuetype.DAOTradeAccount;
import org.trade.dictionary.valuetype.UIPropertyCodes;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.TradeAccount;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.strategy.StrategyRule;
import org.trade.ui.base.BaseButton;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.BaseUIPropertyCodes;
import org.trade.ui.base.Table;
import org.trade.ui.models.TradestrategyTableModel;
import org.trade.ui.models.TradingdayTableModel;
import org.trade.ui.tables.TradestrategyTable;
import org.trade.ui.tables.TradingdayTable;
import org.trade.ui.widget.DAODecodeComboBoxEditor;
import org.trade.ui.widget.DecodeComboBoxRenderer;
import org.trade.ui.widget.DecodeTableEditor;

/**
 */
public class TradingdayPanel extends BasePanel implements ItemListener {

	private static final long serialVersionUID = 8543984162821384818L;

	private final static Logger _log = LoggerFactory
			.getLogger(TradingdayPanel.class);

	private PersistentModel m_tradePersistentModel = null;
	private TradingdayTableModel m_tradingdayModel = null;
	private Table m_tradestrategyTable = null;
	private TradestrategyTableModel m_tradestrategyModel = null;
	private Table m_tradingdayTable = null;
	private Tradingdays m_tradingdays = null;
	private static final ConcurrentHashMap<String, StrategyRule> m_strategyWorkers = new ConcurrentHashMap<String, StrategyRule>();

	private String m_defaultDir = null;
	private BaseButton ordersButton = null;
	private BaseButton deleteTradeOrderButton = null;
	private BaseButton runStrategyButton = null;
	private BaseButton testStrategyButton = null;
	private BaseButton brokerDataButton = null;
	private BaseButton cancelStrategiesButton = null;
	private BaseButton reAssignButton = null;
	private BaseButton transferButton = null;
	private DAODecodeComboBoxEditor strategyFromEditorComboBox = null;
	private DAODecodeComboBoxEditor strategyToEditorComboBox = null;
	private JSpinner spinnerStart = new JSpinner();
	private JSpinner spinnerEnd = new JSpinner();
	private Boolean connected = new Boolean(false);
	private JEditorPane tradeAccountLabel = null;
	private static final NumberFormat currencyFormater = NumberFormat
			.getCurrencyInstance();
	private final SimpleDateFormat dateFormater = new SimpleDateFormat(
			"MM/dd/yy HH:mm:ss", Locale.getDefault());

	private static final SimpleAttributeSet bold = new SimpleAttributeSet();
	private static final SimpleAttributeSet colorRedAttr = new SimpleAttributeSet();
	private static final SimpleAttributeSet colorGreenAttr = new SimpleAttributeSet();

	private static final String DATEFORMAT = "MM/dd/yyyy";

	/**
	 * Constructor
	 * 
	 * @param tradingdays
	 *            Tradingdays
	 * @param controller
	 *            BasePanel
	 * @param tradePersistentModel
	 *            PersistentModel
	 * @param strategyWorkers
	 *            ConcurrentHashMap<String,StrategyRule>
	 */
	@SuppressWarnings("unchecked")
	public TradingdayPanel(Tradingdays tradingdays, BasePanel controller,
			PersistentModel tradePersistentModel) {
		try {
			getMenu().addMessageListener(this);
			this.setLayout(new BorderLayout());

			m_tradingdays = tradingdays;
			m_tradePersistentModel = tradePersistentModel;
			m_defaultDir = ConfigProperties
					.getPropAsString("trade.csv.default.dir");
			currencyFormater.setMinimumFractionDigits(2);
			dateFormater.setLenient(false);
			StyleConstants.setBold(bold, true);
			StyleConstants.setBackground(colorRedAttr, Color.RED);
			StyleConstants.setBackground(colorGreenAttr, Color.GREEN);

			// This allows the controller to listen to these events
			transferButton = new BaseButton(controller,
					BaseUIPropertyCodes.TRANSFER);
			ordersButton = new BaseButton(controller, BaseUIPropertyCodes.FETCH);
			ordersButton.setToolTipText("Fetch Executed Orders");
			deleteTradeOrderButton = new BaseButton(this,
					BaseUIPropertyCodes.DELETE);
			deleteTradeOrderButton.setToolTipText("Delete Orders");
			cancelStrategiesButton = new BaseButton(controller,
					BaseUIPropertyCodes.CANCEL);
			cancelStrategiesButton.setToolTipText("Cancel Strategy");
			runStrategyButton = new BaseButton(controller,
					BaseUIPropertyCodes.RUN);
			runStrategyButton.setToolTipText("Run Strategy");
			testStrategyButton = new BaseButton(controller,
					BaseUIPropertyCodes.TEST);
			testStrategyButton.setToolTipText("Test Strategy");
			brokerDataButton = new BaseButton(controller,
					BaseUIPropertyCodes.DATA);
			brokerDataButton.setToolTipText("Get Chart Data");
			reAssignButton = new BaseButton(this,
					UIPropertyCodes.newInstance(UIPropertyCodes.REASSIGN));
			m_tradestrategyModel = new TradestrategyTableModel();
			Tradingday tradingday = null;
			for (Tradingday instance : m_tradingdays.getTradingdays().values()) {
				tradingday = instance;
				break;
			}
			m_tradestrategyModel.setData(tradingday);
			m_tradestrategyTable = new TradestrategyTable(m_tradestrategyModel,
					m_strategyWorkers);
			ToolTipManager.sharedInstance().registerComponent(
					m_tradestrategyTable);
			m_tradestrategyTable.getSelectionModel().addListSelectionListener(
					new TradestrategyTableRowListener());
			m_tradestrategyTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						transferButton.doClick();
					}
				}
			});
			m_tradingdayModel = new TradingdayTableModel();
			m_tradingdayModel.setData(m_tradingdays);
			m_tradingdayTable = new TradingdayTable(m_tradingdayModel);
			m_tradingdayTable.getSelectionModel().addListSelectionListener(
					new TradingdayTableRowListener());

			JPanel jPanel2 = new JPanel(new BorderLayout());
			JPanel jPanel3 = new JPanel(new BorderLayout());
			JPanel jPanel4 = new JPanel(new BorderLayout());

			JLabel fromStrategy = new JLabel("From Strategy:");
			strategyFromEditorComboBox = new DAODecodeComboBoxEditor(
					(new DAOStrategy()).getCodesDecodes());
			strategyFromEditorComboBox
					.setRenderer(new DecodeComboBoxRenderer());
			strategyFromEditorComboBox.setEditable(true);
			strategyFromEditorComboBox.addItemListener(this);

			JLabel toStrategy = new JLabel("To Strategy:");
			strategyToEditorComboBox = new DAODecodeComboBoxEditor(
					(new DAOStrategy()).getCodesDecodes());
			strategyToEditorComboBox.setRenderer(new DecodeComboBoxRenderer());
			strategyToEditorComboBox.setEditable(true);
			strategyToEditorComboBox.addItemListener(this);

			JScrollPane jScrollPane = new JScrollPane();
			jScrollPane.getViewport().add(m_tradestrategyTable,
					BorderLayout.CENTER);
			jScrollPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
			Dimension tradestrategyTableDimension = m_tradestrategyTable
					.getPreferredSize();
			// Make changes to [i]d[/i] if you like...
			m_tradestrategyTable
					.setPreferredScrollableViewportSize(tradestrategyTableDimension);
			jScrollPane.addMouseListener(m_tradestrategyTable);

			spinnerStart.setModel(new SpinnerDateModel());
			JSpinner.DateEditor de = new JSpinner.DateEditor(spinnerStart,
					DATEFORMAT);
			spinnerStart.setEditor(de);
			spinnerStart.setValue(tradingday.getOpen());

			spinnerEnd.setModel(new SpinnerDateModel());
			JSpinner.DateEditor de1 = new JSpinner.DateEditor(spinnerEnd,
					DATEFORMAT);
			spinnerEnd.setEditor(de1);
			spinnerEnd.setValue(tradingday.getOpen());
			tradeAccountLabel = new JEditorPane("text/rtf", "");
			tradeAccountLabel.setAutoscrolls(false);
			tradeAccountLabel.setEditable(false);

			JPanel jPanel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JPanel jPanel6 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			JPanel jPanel7 = new JPanel(new FlowLayout(FlowLayout.LEFT));

			JLabel dateStartLabel = new JLabel("From Date:");
			JLabel dateEndLabel = new JLabel("To Date:");
			jPanel5.add(brokerDataButton, null);
			jPanel5.add(testStrategyButton, null);
			jPanel5.add(runStrategyButton, null);
			jPanel5.add(cancelStrategiesButton, null);
			jPanel5.add(deleteTradeOrderButton, null);
			jPanel5.setBorder(new BevelBorder(BevelBorder.RAISED));
			JToolBar jToolBar1 = new JToolBar();
			jToolBar1.setLayout(new BorderLayout());
			jToolBar1.add(jPanel5, BorderLayout.WEST);

			jPanel6.add(dateStartLabel, null);
			jPanel6.add(spinnerStart, null);
			jPanel6.add(dateEndLabel, null);
			jPanel6.add(spinnerEnd, null);
			jPanel6.setBorder(new BevelBorder(BevelBorder.RAISED));
			jPanel7.add(fromStrategy, null);
			jPanel7.add(strategyFromEditorComboBox, null);
			jPanel7.add(toStrategy, null);
			jPanel7.add(strategyToEditorComboBox, null);
			jPanel7.add(reAssignButton, null);
			jPanel7.add(ordersButton, null);
			jPanel7.setBorder(new BevelBorder(BevelBorder.RAISED));

			jPanel3.setBorder(new BevelBorder(BevelBorder.LOWERED));
			jPanel3.add(jPanel6, BorderLayout.WEST);
			jPanel3.add(jPanel7, BorderLayout.EAST);

			jPanel4.add(jToolBar1, BorderLayout.NORTH);
			jPanel4.add(jScrollPane, BorderLayout.CENTER);

			JScrollPane jScrollPane1 = new JScrollPane();
			jScrollPane1.getViewport().add(m_tradingdayTable,
					BorderLayout.NORTH);
			jScrollPane1.setBorder(new BevelBorder(BevelBorder.LOWERED));
			Dimension tradingdayTableDimension = m_tradingdayTable
					.getPreferredSize();
			// Make changes to [i]d[/i] if you like...
			m_tradingdayTable
					.setPreferredScrollableViewportSize(tradingdayTableDimension);

			JPanel jPanel8 = new JPanel(new BorderLayout());
			jPanel8.add(tradeAccountLabel, BorderLayout.NORTH);
			jPanel8.add(jPanel3, BorderLayout.SOUTH);
			jPanel2.add(jPanel8, BorderLayout.NORTH);
			jPanel2.add(jScrollPane1, BorderLayout.CENTER);
			JSplitPane jSplitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					true, jPanel2, jPanel4);
			jSplitPane1.setResizeWeight(0.2d);
			jSplitPane1.setOneTouchExpandable(true);
			this.add(jSplitPane1);
			DAOTradeAccount account = DAOTradeAccount.newInstance();
			this.setTradeAccountLabel((TradeAccount) account.getObject());
			enableTradestrategyButtons(null);
		} catch (Exception ex) {
			this.setErrorMessage("Error During Initialization.",
					ex.getMessage(), ex);
		}
	}

	public void doWindowActivated() {
		try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			DecodeTableEditor tradeAccountEditor = new DecodeTableEditor(
					new JComboBox((new DAOTradeAccount()).getCodesDecodes()));
			m_tradestrategyTable.setDefaultEditor(DAOTradeAccount.class,
					tradeAccountEditor);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			DecodeTableEditor strategyEditor = new DecodeTableEditor(
					new JComboBox((new DAOStrategy()).getCodesDecodes()));
			m_tradestrategyTable.setDefaultEditor(DAOStrategy.class,
					strategyEditor);

			@SuppressWarnings({ "rawtypes", "unchecked" })
			DecodeTableEditor strategyManagerEditor = new DecodeTableEditor(
					new JComboBox((new DAOStrategyManager()).getCodesDecodes()));
			m_tradestrategyTable.setDefaultEditor(DAOStrategyManager.class,
					strategyManagerEditor);

			resetStrategyComboBox(strategyFromEditorComboBox);
			resetStrategyComboBox(strategyToEditorComboBox);

		} catch (ValueTypeException ex) {
			this.setErrorMessage("Error activating window.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method doWindowDeActivated.
	 * 
	 * @return boolean
	 */
	public boolean doWindowDeActivated() {
		if (m_tradingdays.isDirty()) {
			setStatusBarMessage(
					"Please Save or Refresh as changed are pending",
					BasePanel.WARNING);
			return false;
		}
		return true;
	}

	public void doWindowOpen() {
	}

	public void doWindowClose() {
	}

	/**
	 * This is fired from any Tab when the Delete button is pressed. This should
	 * be used to delete all the trading orders for the current trading days.
	 * 
	 */

	public void doDelete() {
		try {

			int result = JOptionPane.showConfirmDialog(this.getFrame(),
					"Are you sure you want to delete all Trade Orders?",
					"Warning", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				deleteTradeOrders(m_tradingdays);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error deleting TradeOrders.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired from the Trading Tab when the Delete button is pressed.
	 * This should be used to delete a trade orders for a selected
	 * tradestrategy.
	 * 
	 * @param tradestrategy
	 *            the Tradestrategy that you would like to delete tradeorders
	 *            for.
	 * 
	 */

	public void doDelete(final Tradestrategy tradestrategy) {
		try {
			int result = JOptionPane
					.showConfirmDialog(
							this.getFrame(),
							"Do you want to delete order for the selected Tradestrategy?",
							"Information", JOptionPane.YES_NO_OPTION);
			if (result == JOptionPane.YES_OPTION) {
				Tradingdays tradingdays = new Tradingdays();
				Tradingday tradingday = Tradingday.newInstance(tradestrategy
						.getTradingday().getOpen());
				tradingday.addTradestrategy(tradestrategy);
				tradingdays.add(tradingday);
				deleteTradeOrders(tradingdays);
			}

		} catch (Exception ex) {
			this.setErrorMessage("Error deleting TradeOrders.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired from the Tradingday Tab when the Save button on the toolbar
	 * is pressed. This will save all the tradingdays/tradestrategies.
	 * 
	 */

	public void doSave() {
		try {

			this.setStatusBarMessage("Save in progress ...\n",
					BasePanel.INFORMATION);

			// Save the Trading days
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						getFrame().setCursor(
								Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						boolean dirty = false;
						for (Tradingday tradingday : m_tradingdays
								.getTradingdays().values()) {
							if (tradingday.getClose().before(
									tradingday.getOpen())
									|| tradingday.getClose().equals(
											tradingday.getOpen())) {
								String msg = "Tradingday Open "
										+ tradingday.getOpen()
										+ " cannot be after trading day close "
										+ tradingday.getClose();
								setErrorMessage("Error Tradingday", msg,
										new PersistentModelException(msg));
							}
							if (tradingday.isDirty()) {
								dirty = true;
								m_tradePersistentModel
										.persistTradingday(tradingday);
							}
						}
						if (dirty)
							doRefresh();
						clearStatusBarMessage();
						getFrame().setCursor(Cursor.getDefaultCursor());
					} catch (PersistentModelException ex) {
						setErrorMessage("Error saving Trade Strategies.",
								ex.getMessage(), ex);
					}
				}
			});

		} catch (Exception ex) {
			this.setErrorMessage("Error saving Trade Strategies.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired when the tool-bar Search button is pressed or this when
	 * this panel is opened. Note this call to the persistent layer clears the
	 * entity Manager session. this is the only place in the application this
	 * happens. m_tradingdays variable is common across all tabs and carries the
	 * references to all data used by the application.
	 * 
	 */

	public void doSearch() {
		try {
			this.clearStatusBarMessage();
			Date startDate = TradingCalendar.getSpecificTime(
					(Date) spinnerStart.getValue(), 0, 0, 0);
			Date endDate = TradingCalendar.getSpecificTime(
					(Date) spinnerEnd.getValue(), 23, 59, 59);
			if (endDate.before(startDate)) {
				startDate = TradingCalendar.getSpecificTime(endDate, 0, 0, 0);
				spinnerStart.setValue(startDate);
			}
			/*
			 * Check to see if in the new search criteria do we have todays
			 * trading day if we do and todays tradingday is in the previous
			 * search hand over the DatasetContainers. We do this as these
			 * Datasets may have live data running into them.
			 */
			Tradingdays tradingdays = m_tradePersistentModel
					.findTradingdaysByDateRange(startDate, endDate);
			Tradingday todayTradingday = tradingdays.getTradingday(
					TradingCalendar.getTodayBusinessDayStart(),
					TradingCalendar.getTodayBusinessDayEnd());
			if (null != todayTradingday) {
				Tradingday currTodayTradingday = m_tradingdays.getTradingday(
						TradingCalendar.getTodayBusinessDayStart(),
						TradingCalendar.getTodayBusinessDayEnd());
				if (null != currTodayTradingday
						&& !currTodayTradingday.getTradestrategies().isEmpty()
						&& this.isConnected()) {
					todayTradingday
							.populateDatasetContainer(currTodayTradingday);
				}
			}
			m_tradingdays.getTradingdays().clear();

			if (tradingdays.getTradingdays().isEmpty()) {
				m_tradestrategyModel.setData(new Tradingday());
				this.setStatusBarMessage(
						"Did not find data for period From Date: "
								+ TradingCalendar.getFormattedDate(startDate,
										DATEFORMAT)
								+ " To Date: "
								+ TradingCalendar.getFormattedDate(endDate,
										DATEFORMAT), BasePanel.INFORMATION);

			} else {
				for (Tradingday tradingday : tradingdays.getTradingdays()
						.values()) {
					m_tradingdays.add(tradingday);
				}
			}
			m_tradingdayModel.setData(m_tradingdays);
			List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
			sortKeys.add(new RowSorter.SortKey(0, SortOrder.DESCENDING));
			RowSorter<?> rsDetail = m_tradingdayTable.getRowSorter();
			rsDetail.setSortKeys(sortKeys);
			RowSorter<?> rsSummary = m_tradestrategyTable.getRowSorter();
			rsSummary.setSortKeys(null);
			((TableRowSorter<?>) m_tradingdayTable.getRowSorter()).sort();
			if (m_tradingdays.getTradingdays().size() > 0) {
				m_tradingdayTable.setRowSelectionInterval(0, 0);

			} else {
				m_tradestrategyModel.setData(new Tradingday());
			}
			m_tradestrategyTable.enablePopupMenu(true);
			enableTradestrategyButtons(null);
		} catch (Exception ex) {
			this.setErrorMessage("Error finding Tradingday.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * This is fired from the main menu when the assigning Strategy button is
	 * pressed. This will re assign all the tradestrategies..
	 * 
	 * 
	 * @param strategies
	 *            List<Strategy>
	 */

	public void doReAssign(List<Strategy> strategies) {

		try {
			/*
			 * Check to see if any of the selected trading days has open
			 * positions. If they do kill the strategy worker before deleting
			 * trades.
			 */
			for (Tradingday tradingday : m_tradingdays.getTradingdays()
					.values()) {
				if (Tradingdays.hasTrades(tradingday)) {
					JOptionPane
							.showMessageDialog(
									this.getFrame(),
									"Tradingday: "
											+ tradingday.getOpen()
											+ " has trades. Please delete all trades before re-asigning strategies.",
									"Warning", JOptionPane.OK_OPTION);
					return;
				}
			}

			int result = JOptionPane
					.showConfirmDialog(
							this.getFrame(),
							"Are you sure you want to re-assign strategies for selected trading days?",
							"Warning", JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				this.setStatusBarMessage("Reassign in progress ...\n",
						BasePanel.INFORMATION);
				final ReAssignProgressMonitor reAssignProgressMonitor = new ReAssignProgressMonitor(
						m_tradePersistentModel, m_tradingdays,
						strategies.get(0), strategies.get(1));
				reAssignProgressMonitor
						.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if ("progress".equals(evt.getPropertyName())) {
									int progress = (Integer) evt.getNewValue();
									setProgressBarProgress(progress,
											reAssignProgressMonitor);
								}
							}
						});
				reAssignProgressMonitor.execute();
			}

		} catch (Exception ex) {
			this.setErrorMessage("Error re-assigning Strategies.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired when the tool-bar Refresh button is pressed.
	 * 
	 * @param tradingday
	 *            Tradingday the selected tradingday to be refreshed.
	 * 
	 */
	public void doRefresh(Tradingday tradingday) {
		try {
			this.clearStatusBarMessage();
			tradingday = m_tradingdays.getTradingday(tradingday.getOpen(),
					tradingday.getClose());
			if (null != tradingday && null != tradingday.getIdTradingDay()) {
				Tradingday instance = m_tradePersistentModel
						.findTradingdayById(tradingday.getIdTradingDay());
				instance.populateDatasetContainer(tradingday);
				m_tradingdays.replaceTradingday(tradingday.getOpen(), instance);
				int selectedRow = m_tradingdayTable.getSelectedRow();
				m_tradingdayModel.setData(m_tradingdays);
				if (selectedRow > -1) {
					m_tradingdayTable.setRowSelectionInterval(selectedRow,
							selectedRow);
				} else {
					m_tradestrategyModel.setData(instance);
					enableTradestrategyButtons(null);
				}
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error finding Tradingday.", ex.getMessage(),
					ex);
		}
	}

	public void doRefresh() {
		int row = m_tradingdayTable.getSelectedRow();
		if (row > -1) {
			org.trade.core.valuetype.Date openDate = (org.trade.core.valuetype.Date) m_tradingdayModel
					.getValueAt(m_tradingdayTable.convertRowIndexToModel(row),
							0);
			org.trade.core.valuetype.Date closeDate = (org.trade.core.valuetype.Date) m_tradingdayModel
					.getValueAt(m_tradingdayTable.convertRowIndexToModel(row),
							1);
			Tradingday tradingday = m_tradingdayModel.getData().getTradingday(
					openDate.getDate(), closeDate.getDate());
			doRefresh(tradingday);
		}
	}

	/**
	 * This is fired when the tool-bar File open button is pressed or the main
	 * menu Open File.
	 * 
	 * 
	 */
	public void doOpen() {
		try {
			this.setStatusBarMessage(
					"CSV file format is: SYM,Symbol,SMART,BOT/SLD(opt),DATE(MM/dd/yyyy) (opt), Tier(Opt), Mkt Bias(opt), Mkt Bar(opt), Mkt Gap(opt)",
					BasePanel.INFORMATION);

			JFileChooser fileView = new JFileChooser();
			fileView.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileView.addChoosableFileFilter(new CVSFilter());
			fileView.setAcceptAllFileFilterUsed(false);
			if (null == m_defaultDir) {
				fileView.setCurrentDirectory(new File(System
						.getProperty("user.dir")));
			} else {
				fileView.setCurrentDirectory(new File(m_defaultDir));
			}

			int returnVal = fileView.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fileName = fileView.getSelectedFile().getPath();

				if (null == fileName) {
					this.setStatusBarMessage("No file selected ",
							BasePanel.INFORMATION);
					return;
				}
				int selectedRow = 0;
				Tradingday tradingday = null;
				if (m_tradingdayTable.getSelectionModel()
						.getLeadSelectionIndex() == -1) {
					if (m_tradingdays.getTradingdays().size() > 0) {
						m_tradingdayTable.setRowSelectionInterval(0, 0);
					}
				}
				if (m_tradingdayTable.getSelectionModel()
						.getLeadSelectionIndex() > -1) {
					selectedRow = m_tradingdayTable.getSelectionModel()
							.getLeadSelectionIndex();
					org.trade.core.valuetype.Date openDate = (org.trade.core.valuetype.Date) m_tradingdayModel
							.getValueAt(m_tradingdayTable
									.convertRowIndexToModel(selectedRow), 0);
					org.trade.core.valuetype.Date closeDate = (org.trade.core.valuetype.Date) m_tradingdayModel
							.getValueAt(m_tradingdayTable
									.convertRowIndexToModel(selectedRow), 1);
					tradingday = m_tradingdayModel.getData().getTradingday(
							openDate.getDate(), closeDate.getDate());
				}
				if (null == tradingday) {
					this.setStatusBarMessage(
							"Please select or add a Tradingday.",
							BasePanel.WARNING);
					return;
				}

				m_tradingdays.populateDataFromFile(fileName, tradingday);
				m_tradingdayModel.setData(m_tradingdays);
				if (m_tradingdays.getTradingdays().size() > 0) {
					m_tradingdayTable.setRowSelectionInterval(selectedRow,
							selectedRow);
					spinnerEnd
							.setValue(((org.trade.core.valuetype.Date) m_tradingdayModel
									.getValueAt(m_tradingdayTable
											.convertRowIndexToModel(0), 0))
									.getDate());

					spinnerStart
							.setValue(((org.trade.core.valuetype.Date) m_tradingdayModel.getValueAt(
									m_tradingdayTable
											.convertRowIndexToModel(m_tradingdayModel
													.getRowCount() - 1), 1))
									.getDate());
				}
			}
			this.clearStatusBarMessage();
		} catch (Exception ex) {
			this.setErrorMessage("Exception while reading csv file.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired from the dropdown Strategy list is selected.
	 * 
	 * 
	 * @param e
	 *            ItemEvent
	 * @see java.awt.event.ItemListener#itemStateChanged(ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {
			Strategy fromStrategy = ((Strategy) ((DAOStrategy) strategyFromEditorComboBox
					.getSelectedItem()).getObject());
			Strategy toStrategy = ((Strategy) ((DAOStrategy) strategyToEditorComboBox
					.getSelectedItem()).getObject());
			List<Strategy> item = new ArrayList<Strategy>();
			item.add(fromStrategy);
			item.add(toStrategy);
			reAssignButton.setTransferObject(item);
		}
	}

	/**
	 * Method setConnected.
	 * 
	 * @param connected
	 *            Boolean
	 */
	public void setConnected(Boolean connected) {
		this.connected = connected;
		if (m_tradestrategyTable.getSelectedRow() > -1) {
			Tradestrategy transferObject = m_tradestrategyModel
					.getData()
					.getTradestrategies()
					.get(m_tradestrategyTable
							.convertRowIndexToModel(m_tradestrategyTable
									.getSelectedRow()));
			enableTradestrategyButtons(transferObject);
		}
	}

	/**
	 * Method setTradeAccountLabel.
	 * 
	 * @param tradeAccount
	 *            TradeAccount
	 */
	public void setTradeAccountLabel(TradeAccount tradeAccount) {
		try {
			tradeAccountLabel.setText(null);
			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					"Acct #:", false, bold);
			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					CoreUtils.padRight(tradeAccount.toString(), 15), false,
					null);

			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					" Avail Bal:", false, bold);
			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					CoreUtils.padLeft(currencyFormater.format((tradeAccount
							.getAvailableFunds() == null ? 0 : tradeAccount
							.getAvailableFunds().doubleValue())), 13), false,
					null);

			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					" Margin:", false, bold);
			CoreUtils
					.setDocumentText(tradeAccountLabel.getDocument(), CoreUtils
							.padLeft(currencyFormater.format((tradeAccount
									.getBuyingPower() == null ? 0
									: tradeAccount.getBuyingPower()
											.doubleValue())), 13), false, null);

			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					" Pos Val:", false, bold);
			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					CoreUtils.padLeft(currencyFormater.format((tradeAccount
							.getGrossPositionValue() == null ? 0 : tradeAccount
							.getGrossPositionValue().doubleValue())), 13),
					false, null);

			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					" Realized P/L:", false, bold);
			double realizedPnL = (tradeAccount.getRealizedPnL() == null ? 0
					: tradeAccount.getRealizedPnL().doubleValue());
			if (realizedPnL < 0) {
				CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
						CoreUtils.padLeft(currencyFormater.format(realizedPnL),
								13), false, colorRedAttr);
			} else if (realizedPnL > 0) {
				CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
						CoreUtils.padLeft(currencyFormater.format(realizedPnL),
								13), false, colorGreenAttr);
			} else {
				CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
						CoreUtils.padLeft(currencyFormater.format(realizedPnL),
								13), false, null);
			}
			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					" Unrealized P/L:", false, bold);
			double unRealizedPnL = (tradeAccount.getUnrealizedPnL() == null ? 0
					: tradeAccount.getUnrealizedPnL().doubleValue());
			if (unRealizedPnL < 0) {
				CoreUtils.setDocumentText(
						tradeAccountLabel.getDocument(),
						CoreUtils.padLeft(
								currencyFormater.format(unRealizedPnL), 13),
						false, colorRedAttr);
			} else if (unRealizedPnL > 0) {
				CoreUtils.setDocumentText(
						tradeAccountLabel.getDocument(),
						CoreUtils.padLeft(
								currencyFormater.format(unRealizedPnL), 13),
						false, colorGreenAttr);
			} else {
				CoreUtils.setDocumentText(
						tradeAccountLabel.getDocument(),
						CoreUtils.padLeft(
								currencyFormater.format(unRealizedPnL), 13),
						false, null);
			}
			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					" Date:", false, bold);
			CoreUtils.setDocumentText(tradeAccountLabel.getDocument(),
					CoreUtils.padRight(dateFormater.format((tradeAccount
							.getUpdateDate() == null ? new Date()
							: tradeAccount.getUpdateDate())), 17), false, null);

		} catch (Exception ex) {
			this.setErrorMessage("Error setting Trade Account Label.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method isStrategyWorkerRunning.
	 * 
	 * @param tradingday
	 *            Tradingday
	 * @return boolean
	 */
	public boolean isStrategyWorkerRunning(Tradingday tradingday) {
		for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
			if (isStrategyWorkerRunning(tradestrategy)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method isStrategyWorkerRunning.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return boolean
	 */
	public boolean isStrategyWorkerRunning(Tradestrategy tradestrategy) {

		String key = tradestrategy.getStrategy().getClassName()
				+ tradestrategy.getIdTradeStrategy();
		if (isStrategyWorkerRunning(key)) {
			return true;
		}
		if (null != tradestrategy.getStrategy().getStrategyManager()) {
			key = tradestrategy.getStrategy().getStrategyManager()
					.getClassName()
					+ tradestrategy.getIdTradeStrategy();
			if (isStrategyWorkerRunning(key)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Method isStrategyWorkerRunning.
	 * 
	 * @param key
	 *            String
	 * @return boolean
	 */
	public boolean isStrategyWorkerRunning(String key) {
		if (m_strategyWorkers.containsKey(key)) {
			StrategyRule strategy = m_strategyWorkers.get(key);
			if (!strategy.isDone()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method addStrategyWorker.
	 * 
	 * @param key
	 *            String
	 * @param strategy
	 *            StrategyRule
	 */
	public void addStrategyWorker(String key, StrategyRule strategy) {
		m_strategyWorkers.put(key, strategy);
	}

	/**
	 * Method addStrategyWorker.
	 * 
	 * @param key
	 *            String
	 * @param strategy
	 *            StrategyRule
	 */
	public void removeStrategyWorker(String key) {
		if (m_strategyWorkers.containsKey(key)) {
			m_strategyWorkers.remove(key);
		}
	}

	/**
	 * Method killAllStrategyWorkersForTradestrategy.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void killAllStrategyWorkersForTradestrategy(
			Tradestrategy tradestrategy) {
		String key = tradestrategy.getStrategy().getClassName()
				+ tradestrategy.getIdTradeStrategy();
		if (isStrategyWorkerRunning(key)) {
			killStrategyWorker(key);
		}
		key = tradestrategy.getStrategy().getStrategyManager().getClassName()
				+ tradestrategy.getIdTradeStrategy();
		if (isStrategyWorkerRunning(key)) {
			killStrategyWorker(key);
		}
	}

	/**
	 * Method killStrategyWorker.
	 * 
	 * @param key
	 *            String
	 */
	public void killStrategyWorker(String key) {
		if (m_strategyWorkers.containsKey(key)) {
			StrategyRule strategy = m_strategyWorkers.get(key);
			if (!strategy.isDone()) {
				strategy.cancel();
			}
		}
	}

	/**
	 * Method killAllStrategyWorker.
	 * 
	 */
	public void killAllStrategyWorker() {
		for (String key : m_strategyWorkers.keySet()) {
			killStrategyWorker(key);
		}
	}

	/**
	 * Method cleanStrategyWorker.
	 * 
	 */
	public void cleanStrategyWorker() {
		for (String key : m_strategyWorkers.keySet()) {
			if (m_strategyWorkers.get(key).isDone()) {
				m_strategyWorkers.remove(key);
			}
		}
	}

	/**
	 * Method deleteTradeOrders.
	 * 
	 * @param tradingdays
	 *            Tradingdays
	 */
	private void deleteTradeOrders(Tradingdays tradingdays) {

		/*
		 * Check to see if any of the selected trading days has open positions.
		 * If they do kill the strategy worker before deleting trades.
		 */
		for (Tradingday tradingday : tradingdays.getTradingdays().values()) {
			if (Tradingdays.hasOpenTrades(tradingday)) {
				int result = JOptionPane
						.showConfirmDialog(
								this.getFrame(),
								"Tradingday: "
										+ tradingday.getOpen()
										+ " has open positions. Do you want to continue",
								"Warning", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					for (Tradestrategy tradestrategy : tradingday
							.getTradestrategies()) {
						killAllStrategyWorkersForTradestrategy(tradestrategy);
					}
				} else {
					return;
				}
			}
		}

		this.killAllStrategyWorker();

		this.setStatusBarMessage("Delete in progress ...\n",
				BasePanel.INFORMATION);
		final DeleteProgressMonitor deleteProgressMonitor = new DeleteProgressMonitor(
				m_tradePersistentModel, tradingdays);
		deleteProgressMonitor
				.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						if ("progress".equals(evt.getPropertyName())) {
							int progress = (Integer) evt.getNewValue();
							setProgressBarProgress(progress,
									deleteProgressMonitor);
						}
					}
				});
		deleteProgressMonitor.execute();
	}

	/**
	 * Method setProgressBarProgress.
	 * 
	 * @param progress
	 *            int
	 * @param worker
	 *            SwingWorker<Void,String>
	 */
	private void setProgressBarProgress(int progress,
			SwingWorker<Void, String> worker) {

		getProgressBar().setValue(progress);
		if (getProgressBar().getMaximum() > 0) {
			String message = String.format("Completed %d%%.\n", progress);
			setStatusBarMessage(message, BasePanel.WARNING);
		}

		if (worker.isDone() || (progress == 100)) {
			Toolkit.getDefaultToolkit().beep();
			if (worker.isCancelled()) {
				setStatusBarMessage("Process canceled.\n",
						BasePanel.INFORMATION);
			} else {
				setStatusBarMessage("Process completed.\n",
						BasePanel.INFORMATION);
				getProgressBar().setMaximum(0);
				getProgressBar().setMinimum(0);
			}
		}
	}

	/**
	 * Method enableTradestrategyButtons.
	 * 
	 * @param transferObject
	 *            Tradestrategy
	 */
	private void enableTradestrategyButtons(Tradestrategy transferObject) {
		boolean enable = false;
		if (null != transferObject) {
			enable = true;
			transferButton.setTransferObject(transferObject
					.getIdTradeStrategy());
		} else {
			transferButton.setTransferObject(null);
		}

		deleteTradeOrderButton.setTransferObject(transferObject);
		cancelStrategiesButton.setTransferObject(transferObject);
		testStrategyButton.setTransferObject(transferObject);
		ordersButton.setTransferObject(transferObject);
		brokerDataButton.setTransferObject(transferObject);
		runStrategyButton.setTransferObject(transferObject);

		deleteTradeOrderButton.setEnabled(enable);
		cancelStrategiesButton.setEnabled(enable);
		testStrategyButton.setEnabled(enable);
		brokerDataButton.setEnabled(enable);
		ordersButton.setEnabled(enable);
		runStrategyButton.setEnabled(false);
		if (this.isConnected() && null != transferObject) {
			runStrategyButton.setEnabled(true);
			testStrategyButton.setEnabled(false);
		}
	}

	/**
	 * Method isConnected.
	 * 
	 * @return boolean
	 */
	private boolean isConnected() {
		return this.connected;
	}

	/**
	 * Method resetStrategyComboBox.
	 * 
	 * @param editorComboBox
	 *            DAODecodeComboBoxEditor
	 * @throws ValueTypeException
	 */
	@SuppressWarnings("unchecked")
	private void resetStrategyComboBox(DAODecodeComboBoxEditor editorComboBox)
			throws ValueTypeException {

		Vector<Decode> codesNew = ((new DAOStrategy()).getCodesDecodes());
		@SuppressWarnings("rawtypes")
		DefaultComboBoxModel model = new DefaultComboBoxModel(codesNew);
		editorComboBox.setModel(model);
		editorComboBox.setRenderer(new DecodeComboBoxRenderer());
	}

	/**
	 */
	private class TradestrategyTableRowListener implements
			ListSelectionListener {
		/**
		 * Method valueChanged.
		 * 
		 * @param event
		 *            ListSelectionEvent
		 * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent event) {
			if (!event.getValueIsAdjusting()) {
				ListSelectionModel model = (ListSelectionModel) event
						.getSource();
				if (model.getLeadSelectionIndex() > -1) {

					Tradestrategy transferObject = m_tradestrategyModel
							.getData()
							.getTradestrategies()
							.get(m_tradestrategyTable
									.convertRowIndexToModel(model
											.getLeadSelectionIndex()));
					enableTradestrategyButtons(transferObject);
				} else {
					enableTradestrategyButtons(null);
				}
			}
		}
	}

	/**
	 */
	private class TradingdayTableRowListener implements ListSelectionListener {
		/**
		 * Method valueChanged.
		 * 
		 * @param event
		 *            ListSelectionEvent
		 * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent event) {
			try {
				if (!event.getValueIsAdjusting()) {
					ListSelectionModel model = (ListSelectionModel) event
							.getSource();

					if (model.getLeadSelectionIndex() > -1) {
						org.trade.core.valuetype.Date openDate = (org.trade.core.valuetype.Date) m_tradingdayModel
								.getValueAt(m_tradingdayTable
										.convertRowIndexToModel(model
												.getLeadSelectionIndex()), 0);
						org.trade.core.valuetype.Date closeDate = (org.trade.core.valuetype.Date) m_tradingdayModel
								.getValueAt(m_tradingdayTable
										.convertRowIndexToModel(model
												.getLeadSelectionIndex()), 1);
						Tradingday transferObject = m_tradingdayModel.getData()
								.getTradingday(openDate.getDate(),
										closeDate.getDate());

						m_tradestrategyModel.setData(transferObject);
						m_tradestrategyTable.enablePopupMenu(true);
						enableTradestrategyButtons(null);

					} else {
						m_tradestrategyModel.setData(null);
						m_tradestrategyTable.enablePopupMenu(false);
					}
				}
			} catch (Exception ex) {
				setErrorMessage("Exception changing value.", ex.getMessage(),
						ex);
			}
		}
	}

	/**
	 */
	public class CVSFilter extends FileFilter {

		public final static String csv = "csv";

		// Accept all directories and all csv files.
		/**
		 * Method accept.
		 * 
		 * @param f
		 *            File
		 * @return boolean
		 */
		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}
			String extension = getExtension(f);
			if (extension != null) {
				return extension.equals(csv);
			}
			return false;
		}

		/**
		 * Method getExtension.
		 * 
		 * @param f
		 *            File
		 * @return String
		 */
		public String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if ((i > 0) && (i < (s.length() - 1))) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}

		// The description of this filter
		/**
		 * Method getDescription.
		 * 
		 * @return String
		 */
		public String getDescription() {
			return "CSV Files";
		}
	}

	/**
	 */
	private class DeleteProgressMonitor extends SwingWorker<Void, String> {

		private PersistentModel tradeManagerModel = null;
		private Tradingdays tradingdays = null;
		private int grandtotal = 0;
		private long startTime = 0;

		/**
		 * Constructor for DeleteProgressMonitor.
		 * 
		 * @param tradeManagerModel
		 *            PersistentModel
		 * @param tradingdays
		 *            Tradingdays
		 */
		public DeleteProgressMonitor(PersistentModel tradeManagerModel,
				Tradingdays tradingdays) {
			this.tradingdays = tradingdays;
			this.tradeManagerModel = tradeManagerModel;
		}

		/**
		 * Method doInBackground.
		 * 
		 * @return Void
		 */
		public Void doInBackground() {

			try {
				grandtotal = tradingdays.getTradingdays().size();
				this.startTime = System.currentTimeMillis();
				int totalComplete = 0;
				// Initialize the progress bar
				getProgressBar().setMaximum(100);
				setProgress(0);
				String message = null;
				for (Tradingday tradingday : tradingdays.getTradingdays()
						.values()) {
					this.tradeManagerModel.removeTradingdayTrades(tradingday);
					totalComplete++;
					int percent = (int) (((double) (totalComplete) / grandtotal) * 100d);
					setProgress(percent);
				}
				setProgress(100);
				message = "Completed delete of Trade Order data total days processed: "
						+ totalComplete
						+ " in : "
						+ ((System.currentTimeMillis() - this.startTime) / 1000)
						+ " Seconds.";
				_log.info(message);
				publish(message);
			} catch (Exception ex) {
				setErrorMessage("Error deleting Trade Orders.",
						ex.getMessage(), ex);
			}
			return null;
		}

		/*
		 * This method process the publish method from doInBackground().
		 */
		/**
		 * Method process.
		 * 
		 * @param messages
		 *            List<String>
		 */
		protected void process(List<String> messages) {
			setStatusBarMessage(messages.get(messages.size() - 1),
					BasePanel.INFORMATION);
		}

		public void done() {
			doRefresh();
			String message = "Completed delete of Trade Order data total days processed: "
					+ grandtotal
					+ " in : "
					+ ((System.currentTimeMillis() - this.startTime) / 1000)
					+ " Seconds.";
			setStatusBarMessage(message, BasePanel.INFORMATION);
		}
	}

	/**
	 */
	private class ReAssignProgressMonitor extends SwingWorker<Void, String> {

		private PersistentModel tradeManagerModel = null;
		private Tradingdays tradingdays = null;
		private int grandtotal = 0;
		private long startTime = 0;
		private Strategy fromStrategy = null;
		private Strategy toStrategy = null;

		/**
		 * Constructor for ReAssignProgressMonitor.
		 * 
		 * @param tradeManagerModel
		 *            PersistentModel
		 * @param tradingdays
		 *            Tradingdays
		 * @param fromStrategy
		 *            Strategy
		 * @param toStrategy
		 *            Strategy
		 */
		public ReAssignProgressMonitor(PersistentModel tradeManagerModel,
				Tradingdays tradingdays, Strategy fromStrategy,
				Strategy toStrategy) {
			this.tradingdays = tradingdays;
			this.tradeManagerModel = tradeManagerModel;
			this.fromStrategy = fromStrategy;
			this.toStrategy = toStrategy;
		}

		/**
		 * Method doInBackground.
		 * 
		 * @return Void
		 */
		public Void doInBackground() {

			try {
				this.grandtotal = tradingdays.getTradingdays().size();
				this.startTime = System.currentTimeMillis();
				int totalComplete = 0;
				// Initialize the progress bar
				getProgressBar().setMaximum(100);
				setProgress(0);
				String message = null;
				this.toStrategy = this.tradeManagerModel
						.findStrategyById(this.toStrategy.getIdStrategy());
				for (Tradingday tradingday : tradingdays.getTradingdays()
						.values()) {
					this.tradeManagerModel.reassignStrategy(this.fromStrategy,
							this.toStrategy, tradingday);

					totalComplete++;
					int percent = (int) (((double) (totalComplete) / this.grandtotal) * 100d);
					setProgress(percent);
				}
				setProgress(100);
				message = "Complete re-assign of Strategies total days processed: "
						+ totalComplete
						+ " in : "
						+ ((System.currentTimeMillis() - this.startTime) / 1000)
						+ " Seconds.";
				_log.info(message);
				publish(message);

			} catch (Exception ex) {
				setErrorMessage("Error reassigning strategy.", ex.getMessage(),
						ex);
			}
			return null;
		}

		/*
		 * This method process the publish method from doInBackground().
		 */
		/**
		 * Method process.
		 * 
		 * @param messages
		 *            List<String>
		 */
		protected void process(List<String> messages) {
			setStatusBarMessage(messages.get(messages.size() - 1),
					BasePanel.INFORMATION);
		}

		public void done() {
			doRefresh();
			String message = "Complete re-assign of Strategies total days processed: "
					+ grandtotal
					+ " in : "
					+ ((System.currentTimeMillis() - this.startTime) / 1000)
					+ " Seconds.";
			setStatusBarMessage(message, BasePanel.INFORMATION);
		}
	}
}
