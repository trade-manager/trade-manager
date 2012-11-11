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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SpinnerDateModel;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

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

	private PersistentModel m_tradePersistentModel = null;
	private TradingdayTableModel m_tradingdayModel = null;
	private Table m_tradestrategyTable = null;
	private TradestrategyTableModel m_tradestrategyModel = null;
	private Table m_tradingdayTable = null;
	private Tradingdays m_tradingdays = null;

	private String m_defaultDir = null;
	private BaseButton ordersButton = null;
	private BaseButton deleteTradeOrderButton = null;
	private BaseButton refreshButton = null;
	private BaseButton searchButton = null;
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
			PersistentModel tradePersistentModel,
			ConcurrentHashMap<String, StrategyRule> strategyWorkers) {
		try {
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
			deleteTradeOrderButton = new BaseButton(controller,
					BaseUIPropertyCodes.DELETE);
			deleteTradeOrderButton.setToolTipText("Delete Orders");
			refreshButton = new BaseButton(this, BaseUIPropertyCodes.REFRESH);
			searchButton = new BaseButton(this, BaseUIPropertyCodes.SEARCH);
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
			reAssignButton = new BaseButton(controller,
					UIPropertyCodes.newInstance(UIPropertyCodes.REASSIGN));
			reAssignButton.addMessageListener(this);

			this.setLayout(new BorderLayout());
			m_tradestrategyModel = new TradestrategyTableModel();
			Tradingday tradingday = null;
			for (Tradingday instance : m_tradingdays.getTradingdays().values()) {
				tradingday = instance;
				break;
			}
			m_tradestrategyModel.setData(tradingday);
			m_tradestrategyTable = new TradestrategyTable(m_tradestrategyModel,
					strategyWorkers);
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

			JPanel jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());

			JPanel jPanel2 = new JPanel();
			jPanel2.setLayout(new BorderLayout());

			JPanel jPanel3 = new JPanel(new BorderLayout());
			JPanel jPanel4 = new JPanel();
			jPanel4.setLayout(new BorderLayout());

			JPanel jPanel5 = new JPanel();
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setAlignment(FlowLayout.LEFT);
			jPanel5.setLayout(flowLayout2);

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

			JPanel jPanel6 = new JPanel();
			FlowLayout flowLayout3 = new FlowLayout();
			flowLayout3.setAlignment(FlowLayout.RIGHT);
			jPanel6.setLayout(flowLayout3);

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

			JLabel dateStartLabel = new JLabel("From Date:");
			JLabel dateEndLabel = new JLabel("To Date:");
			jPanel5.add(brokerDataButton, null);
			jPanel5.add(testStrategyButton, null);
			jPanel5.add(runStrategyButton, null);
			jPanel5.add(cancelStrategiesButton, null);
			jPanel5.add(deleteTradeOrderButton, null);
			jPanel5.add(refreshButton, null);
			jPanel5.add(searchButton, null);
			jPanel5.add(dateStartLabel, null);
			jPanel5.add(spinnerStart, null);
			jPanel5.add(dateEndLabel, null);
			jPanel5.add(spinnerEnd, null);
			jPanel6.add(fromStrategy, null);
			jPanel6.add(strategyFromEditorComboBox, null);
			jPanel6.add(toStrategy, null);
			jPanel6.add(strategyToEditorComboBox, null);
			jPanel6.add(reAssignButton, null);
			jPanel6.add(ordersButton, null);
			jPanel3.add(jPanel5, BorderLayout.WEST);
			jPanel3.add(jPanel6, BorderLayout.EAST);
			jPanel4.add(jScrollPane, BorderLayout.CENTER);
			jPanel4.add(jPanel3, BorderLayout.SOUTH);

			JScrollPane jScrollPane1 = new JScrollPane();
			jScrollPane1.getViewport().add(m_tradingdayTable,
					BorderLayout.NORTH);
			jScrollPane1.setBorder(new BevelBorder(BevelBorder.LOWERED));
			Dimension tradingdayTableDimension = m_tradingdayTable
					.getPreferredSize();
			// Make changes to [i]d[/i] if you like...
			m_tradingdayTable
					.setPreferredScrollableViewportSize(tradingdayTableDimension);

			tradeAccountLabel = new JEditorPane("text/rtf", "");
			tradeAccountLabel.setAutoscrolls(false);
			tradeAccountLabel.setEditable(false);
			jPanel2.add(tradeAccountLabel, BorderLayout.NORTH);
			jPanel2.add(jScrollPane1, BorderLayout.CENTER);
			JSplitPane jSplitPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					true, jPanel2, jPanel4);
			jSplitPane1.setResizeWeight(0.2d);
			jSplitPane1.setOneTouchExpandable(true);
			jPanel1.add(jSplitPane1);
			this.add(jPanel1, null);
			DAOTradeAccount account = DAOTradeAccount.newInstance();
			this.setTradeAccountLabel((TradeAccount) account.getObject());
			enableTradestrategyButtons(null);
		} catch (Exception ex) {
			this.setErrorMessage("Error During Initialization.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method getTradestrategyTable.
	 * 
	 * @return Table
	 */
	public Table getTradestrategyTable() {
		return this.m_tradestrategyTable;
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
		if (m_tradingdays.isDirty()
				&& Tradingdays.hasTradestrategies(m_tradingdays)) {
			setStatusBarMessage(
					"Please Save or Refresh as changed are pending",
					BasePanel.WARNING);
			return false;
		}
		return true;
	}

	public void doWindowOpen() {
		try {
			doSearch();
			if (m_tradingdays.getTradingdays().isEmpty()) {
				m_tradingdays.add(Tradingday.newInstance(TradingCalendar
						.getMostRecentTradingDay(new Date())));
				m_tradingdayModel.setData(m_tradingdays);
				if (m_tradingdays.getTradingdays().size() > 0) {
					m_tradingdayTable.setRowSelectionInterval(0, 0);
				}
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error opening window.", ex.getMessage(), ex);
		}
	}

	public void doWindowClose() {

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
			Date startDate = TradingCalendar
					.getBusinessDayStart(((Date) spinnerStart.getValue()));
			Date endDate = TradingCalendar
					.getBusinessDayStart(((Date) spinnerEnd.getValue()));
			if (endDate.before(startDate)) {
				startDate = endDate;
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
			Tradingday todayTradingday = tradingdays
					.getTradingday(TradingCalendar.getTodayBusinessDayStart());
			if (null != todayTradingday) {
				Tradingday currTodayTradingday = m_tradingdays
						.getTradingday(TradingCalendar
								.getTodayBusinessDayStart());
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
	 * This is fired when the tool-bar Refresh button is pressed.
	 * 
	 * @param tradingday
	 *            Tradingday the selected tradingday to be refreshed.
	 * 
	 */
	public void doRefresh(Tradingday tradingday) {
		try {
			this.clearStatusBarMessage();
			tradingday = m_tradingdays.getTradingday(tradingday.getOpen());
			if (null != tradingday) {
				if (null != tradingday.getIdTradingDay()) {
					Tradingday instance = m_tradePersistentModel
							.findTradingdayById(tradingday.getIdTradingDay());
					instance.populateDatasetContainer(tradingday);
					m_tradingdays.replaceTradingday(tradingday.getOpen(),
							instance);
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
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error finding Tradingday.", ex.getMessage(),
					ex);
		}
	}

	public void doRefresh() {
		if (null != refreshButton.getTransferObject())
			doRefresh((Tradingday) refreshButton.getTransferObject());
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

				m_tradingdays.populateDataFromFile(fileName);
				m_tradingdayModel.setData(m_tradingdays);
				if (m_tradingdays.getTradingdays().size() > 0) {
					m_tradingdayTable.setRowSelectionInterval(0, 0);
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
					CoreUtils.padRight(tradeAccount.toString(), 10), false,
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

		deleteTradeOrderButton.setEnabled(enable);
		deleteTradeOrderButton.setTransferObject(transferObject);
		cancelStrategiesButton.setEnabled(enable);
		cancelStrategiesButton.setTransferObject(transferObject);
		testStrategyButton.setTransferObject(transferObject);
		ordersButton.setTransferObject(transferObject);
		brokerDataButton.setTransferObject(transferObject);
		runStrategyButton.setTransferObject(transferObject);
		enable = false;
		if (isConnected()) {
			testStrategyButton.setEnabled(false);
			if (null != transferObject) {
				enable = true;
			}
			brokerDataButton.setEnabled(enable);
			runStrategyButton.setEnabled(enable);
			ordersButton.setEnabled(enable);

		} else {
			brokerDataButton.setEnabled(false);
			runStrategyButton.setEnabled(false);
			ordersButton.setEnabled(false);
			if (null != transferObject) {
				enable = true;
			}
			testStrategyButton.setEnabled(enable);
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
						Tradingday transferObject = m_tradingdayModel.getData()
								.getTradingday(openDate.getDate());

						m_tradestrategyModel.setData(transferObject);
						refreshButton.setTransferObject(transferObject);
						m_tradestrategyTable.enablePopupMenu(true);
						enableTradestrategyButtons(null);

					} else {
						refreshButton.setTransferObject(null);
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
}
