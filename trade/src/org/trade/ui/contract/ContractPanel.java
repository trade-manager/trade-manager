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
package org.trade.ui.contract;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.DAOGroup;
import org.trade.dictionary.valuetype.DAOProfile;
import org.trade.dictionary.valuetype.AllocationMethod;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.Tier;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingdays;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.ui.base.BaseButton;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.BaseUIPropertyCodes;
import org.trade.ui.base.TabbedAppPanel;
import org.trade.ui.base.TabbedCloseButton;
import org.trade.ui.base.Table;
import org.trade.ui.base.TextDialog;
import org.trade.ui.base.Tree;
import org.trade.ui.chart.CandlestickChart;
import org.trade.ui.models.TradeOrderTableModel;
import org.trade.ui.models.TradingdayTreeModel;
import org.trade.ui.tables.TradeOrderTable;
import org.trade.ui.tables.renderer.TradingdayTreeCellRenderer;
import org.trade.ui.widget.ButtonEditor;
import org.trade.ui.widget.ButtonRenderer;
import org.trade.ui.widget.DecodeComboBoxEditor;
import org.trade.ui.widget.DecodeComboBoxRenderer;

/**
 */
public class ContractPanel extends BasePanel implements TreeSelectionListener,
		ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4026209743607182423L;

	private Tradingdays m_tradingdays = null;
	private PersistentModel m_tradePersistentModel = null;
	private final JTabbedPane m_jTabbedPaneContract = new JTabbedPane();
	private TradingdayTreeModel m_treeModel = null;
	private Tree m_tree = null;
	private Table m_tradeOrderTable = null;
	private TradeOrderTableModel m_tradeOrderModel = null;
	private JEditorPane m_tradeLabel = null;
	private JEditorPane m_strategyLabel = null;
	private BaseButton executeButton = null;
	private BaseButton brokerDataButton = null;
	private BaseButton cancelButton = null;
	private BaseButton cancelStrategiesButton = null;
	private BaseButton refreshButton = null;
	private BaseButton closeAllButton = null;
	private BaseButton propertiesButton = null;
	private DecodeComboBoxEditor periodEditorComboBox = null;
	private Integer backfillOffsetDays = 0;
	private Boolean connected = new Boolean(false);
	private static final NumberFormat currencyFormater = NumberFormat
			.getCurrencyInstance();
	private static final SimpleAttributeSet bold = new SimpleAttributeSet();
	private static final SimpleAttributeSet colorRedAttr = new SimpleAttributeSet();
	private static final SimpleAttributeSet colorGreenAttr = new SimpleAttributeSet();

	/**
	 * Constructor for ContractPanel.
	 * 
	 * @param tradingdays
	 *            Tradingdays
	 * @param controller
	 *            TabbedAppPanel
	 * @param tradePersistentModel
	 *            PersistentModel
	 */

	public ContractPanel(Tradingdays tradingdays, TabbedAppPanel controller,
			PersistentModel tradePersistentModel) {

		try {
			if (null != getMenu())
				getMenu().addMessageListener(this);
			this.setLayout(new BorderLayout());
			m_tradePersistentModel = tradePersistentModel;
			m_tradingdays = tradingdays;

			currencyFormater.setMinimumFractionDigits(2);
			StyleConstants.setBold(bold, true);
			StyleConstants.setBackground(colorRedAttr, Color.RED);
			StyleConstants.setBackground(colorGreenAttr, Color.GREEN);

			backfillOffsetDays = ConfigProperties
					.getPropAsInt("trade.backfill.offsetDays");
			propertiesButton = new BaseButton(this,
					BaseUIPropertyCodes.PROPERTIES, 0);
			propertiesButton.setEnabled(false);
			executeButton = new BaseButton(controller,
					BaseUIPropertyCodes.EXECUTE);
			brokerDataButton = new BaseButton(controller,
					BaseUIPropertyCodes.DATA);
			brokerDataButton.setToolTipText("Get Chart data");
			cancelButton = new BaseButton(controller,
					BaseUIPropertyCodes.CANCEL);
			cancelButton.setToolTipText("Cancel Order");
			cancelStrategiesButton = new BaseButton(controller,
					BaseUIPropertyCodes.CANCEL);
			cancelStrategiesButton.setToolTipText("Cancel Strategy");
			refreshButton = new BaseButton(this, BaseUIPropertyCodes.REFRESH);
			closeAllButton = new BaseButton(this, BaseUIPropertyCodes.CLOSE_ALL);
			m_tradeOrderModel = new TradeOrderTableModel();
			m_tradeOrderTable = new TradeOrderTable(m_tradeOrderModel);
			m_tradeOrderTable.getSelectionModel().addListSelectionListener(
					new TradeOrderTableRowListener());
			m_tradeOrderTable.setDefaultEditor(TradeOrder.class,
					new ButtonEditor(propertiesButton));
			m_tradeOrderTable.setDefaultRenderer(TradeOrder.class,
					new ButtonRenderer(BaseUIPropertyCodes.PROPERTIES));
			m_treeModel = new TradingdayTreeModel(m_tradingdays);
			m_tree = new Tree(m_treeModel);
			// Listen for when the selection changes.
			m_tree.addTreeSelectionListener(this);
			m_tree.setCellRenderer(new TradingdayTreeCellRenderer());
			ToolTipManager.sharedInstance().registerComponent(m_tree);

			JPanel jPanel1 = new JPanel(new BorderLayout());
			JScrollPane jScrollPane1Tree = new JScrollPane();
			jScrollPane1Tree.getViewport().add(m_tree, BorderLayout.CENTER);
			JPanel jPanel2 = new JPanel(new BorderLayout());
			jPanel2.add(jScrollPane1Tree, BorderLayout.CENTER);
			jPanel2.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Tradingday"),
					BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			jPanel1.setBorder(new BevelBorder(BevelBorder.LOWERED));
			jPanel2.add(jPanel1, BorderLayout.NORTH);

			// Chart Panel
			// JLabel jLabelPeriod = new JLabel("Period:");
			periodEditorComboBox = new DecodeComboBoxEditor(
					(new BarSize()).getCodesDecodes());
			DecodeComboBoxRenderer periodRenderer = new DecodeComboBoxRenderer();
			periodEditorComboBox.setRenderer(periodRenderer);
			periodEditorComboBox.setItem(BarSize.newInstance(BarSize.FIVE_MIN));
			periodEditorComboBox.addItemListener(this);
			JPanel jPanel6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanel6.setBorder(new BevelBorder(BevelBorder.RAISED));
			jPanel6.add(closeAllButton, null);
			jPanel6.add(brokerDataButton, null);
			jPanel6.add(cancelStrategiesButton, null);
			// jPanel6.add(jLabelPeriod, null);
			// jPanel6.add(periodEditorComboBox, null);
			JToolBar jToolBar = new JToolBar();
			jToolBar.setLayout(new BorderLayout());
			jToolBar.add(jPanel6);

			m_strategyLabel = new JEditorPane("text/rtf", "");
			m_strategyLabel.setAutoscrolls(false);
			m_strategyLabel.setEditable(false);
			m_tradeLabel = new JEditorPane("text/rtf", "");
			m_tradeLabel.setAutoscrolls(false);
			m_tradeLabel.setEditable(false);

			JPanel jPanel12 = new JPanel(new BorderLayout());
			jPanel12.add(m_strategyLabel, null);
			JPanel jPanel18 = new JPanel(new BorderLayout());
			jPanel18.add(jToolBar, BorderLayout.WEST);
			JPanel jPanel11 = new JPanel(new BorderLayout());
			jPanel11.add(jPanel18, BorderLayout.WEST);
			jPanel11.add(jPanel12, BorderLayout.CENTER);
			JPanel jPanel7 = new JPanel(new BorderLayout());
			jPanel7.add(m_jTabbedPaneContract, BorderLayout.CENTER);
			JScrollPane jScrollPane3 = new JScrollPane();
			jScrollPane3.getViewport().add(jPanel7, BorderLayout.CENTER);
			JPanel jPanel9 = new JPanel(new BorderLayout());
			jPanel9.add(jScrollPane3, BorderLayout.CENTER);
			jPanel9.add(jPanel11, BorderLayout.NORTH);
			// Order Panel
			m_tradeOrderTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
			JPanel jPanel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			jPanel5.add(executeButton, null);
			jPanel5.add(cancelButton, null);
			jPanel5.add(refreshButton, null);
			jPanel5.setBorder(new BevelBorder(BevelBorder.RAISED));
			JToolBar jToolBar1 = new JToolBar();
			jToolBar1.setLayout(new BorderLayout());
			jToolBar1.add(jPanel5);
			JPanel jPanel19 = new JPanel(new BorderLayout());
			jPanel19.add(jToolBar1, BorderLayout.WEST);
			JScrollPane jScrollPane2 = new JScrollPane();
			jScrollPane2.getViewport().add(m_tradeOrderTable,
					BorderLayout.CENTER);
			jScrollPane2.setBorder(new BevelBorder(BevelBorder.LOWERED));
			JPanel jPanel16 = new JPanel(new BorderLayout());
			Dimension d = m_tradeOrderTable.getPreferredSize();
			// Make changes to [i]d[/i] if you like...
			m_tradeOrderTable.setPreferredScrollableViewportSize(d);
			jScrollPane2.addMouseListener(m_tradeOrderTable);
			JPanel jPanel17 = new JPanel(new BorderLayout());
			jPanel17.add(jPanel19, BorderLayout.WEST);
			jPanel17.add(m_tradeLabel, BorderLayout.CENTER);
			jPanel16.add(jPanel17, BorderLayout.NORTH);
			jPanel16.add(jScrollPane2, BorderLayout.CENTER);

			// use the new JSplitPane to dynamically resize...
			JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
					jPanel9, jPanel16);
			split.setOneTouchExpandable(true);
			split.setResizeWeight(0.8d);
			JPanel jPanel15 = new JPanel(new BorderLayout());
			jPanel15.add(split, BorderLayout.CENTER);

			JSplitPane mainSplitPane = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT, true, jPanel2, jPanel15);
			mainSplitPane.setOneTouchExpandable(true);
			mainSplitPane.setResizeWeight(0.15d);
			this.add(mainSplitPane, BorderLayout.CENTER);
			m_jTabbedPaneContract.addChangeListener(this);
			this.reFreshTab();
		} catch (Exception ex) {
			this.setErrorMessage("Error During Initialization.",
					ex.getMessage(), ex);
		}
	}

	public void doOpen() {
		try {
			m_treeModel.setData(m_tradingdays);
		} catch (Exception ex) {
			this.setErrorMessage("Error opening all tabs.", ex.getMessage(), ex);
		}
	}

	/**
	 * Method doProperties.
	 * 
	 * @param series
	 *            IndicatorSeries
	 */

	public void doProperties(TradeOrder instance) {
		try {

			if (null == instance.getTradestrategy().getPortfolio()
					.getIndividualAccount()) {
				AllocationMethodPanel allocationMethodPanel = new AllocationMethodPanel(
						instance);
				if (null != allocationMethodPanel) {
					TextDialog dialog = new TextDialog(this.getFrame(),
							"FA Account Properties", true,
							allocationMethodPanel);
					dialog.setLocationRelativeTo(this);
					dialog.setVisible(true);
					if (!dialog.getCancel()) {
						if (null != instance.getFAProfile()) {
							instance.setFAGroup(null);
							instance.setFAMethod(null);
							instance.setFAPercent(null);
							instance.setAccountNumber(null);
						} else {
							if (null != instance.getFAGroup()) {
								instance.setAccountNumber(null);
							} else {
								instance.setAccountNumber(instance
										.getTradestrategy().getPortfolio()
										.getIndividualAccount()
										.getAccountNumber());
							}
						}
					}
				}
			} else {
				this.setStatusBarMessage(
						"No properties for Individual accounts ...\n",
						BasePanel.INFORMATION);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error setting FA properties.",
					ex.getMessage(), ex);
		}
	}

	public void doCloseAll() {
		try {
			int tabsCount = m_jTabbedPaneContract.getTabCount();
			for (int index = 0; index < tabsCount; index++) {
				doClose(new Integer(0));
			}
			m_tree.clearSelection();
		} catch (Exception ex) {
			this.setErrorMessage("Error removing all tabs.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method doClose.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void doClose(Tradestrategy tradestrategy) {
		for (int index = 0; index < m_jTabbedPaneContract.getTabCount(); index++) {
			ChartPanel chart = (ChartPanel) m_jTabbedPaneContract
					.getComponentAt(index);
			if ((null != chart)
					&& chart.getTradestrategy().getIdTradeStrategy()
							.equals(tradestrategy.getIdTradeStrategy())) {
				doClose(index);
				break;
			}
		}
	}

	/**
	 * Method doClose.
	 * 
	 * @param index
	 *            Integer
	 */
	public void doClose(Integer index) {
		ChartPanel chartPanel = (ChartPanel) m_jTabbedPaneContract
				.getComponentAt(index);
		TabbedCloseButton tabbedCloseButton = (TabbedCloseButton) m_jTabbedPaneContract
				.getTabComponentAt(index);
		tabbedCloseButton.removeMessageListener(this);
		chartPanel.getCandlestickChart().removeChart();
		chartPanel = null;
		m_jTabbedPaneContract.remove(index);
		m_tree.clearSelection();
	}

	public void doDelete() {
	}

	public void doExecute() {
	}

	public void doWindowOpen() {

	}

	public void doWindowClose() {

	}

	public void doWindowActivated() {
		try {
			Object selectedObject = brokerDataButton.getTransferObject();
			if (null == selectedObject) {
				DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) m_tree
						.getLastSelectedPathComponent();
				if (null != selectedNode)
					selectedObject = selectedNode.getUserObject();
			}
			m_treeModel.setData(m_tradingdays);
			// Expand the tree
			for (int i = 0; i < m_tree.getRowCount(); i++) {
				m_tree.expandRow(i);
			}
			TreePath path = m_tree.findTreePathByObject(selectedObject);

			if (null != path) {
				m_tree.setSelectionPath(path);
				m_tree.scrollPathToVisible(path);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error window activated.", ex.getMessage(), ex);
		}
	}

	/**
	 * Method doWindowDeActivated.
	 * 
	 * @return boolean
	 */
	public boolean doWindowDeActivated() {
		return true;
	}

	public void doRefresh() {
		ChartPanel currentTab = (ChartPanel) m_jTabbedPaneContract
				.getSelectedComponent();
		if (null != currentTab)
			doRefresh(currentTab.getTradestrategy());
	}

	/**
	 * Method doRefresh.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void doRefresh(final Tradestrategy tradestrategy) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						getFrame().setCursor(
								Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						ChartPanel currentTab = (ChartPanel) m_jTabbedPaneContract
								.getSelectedComponent();
						if (null != currentTab) {
							if (currentTab.getTradestrategy().equals(
									tradestrategy)) {
								reFreshTab();
							}
						}
					} finally {
						getFrame().setCursor(Cursor.getDefaultCursor());
					}
				}
			});

		} catch (Exception ex) {
			setErrorMessage("Error refreshing Tradestrategy.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method valueChanged.
	 * 
	 * @param e
	 *            TreeSelectionEvent
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {

		try {
			/*
			 * Returns the last path element of the selection.This method is
			 * useful only when the selection model allows a single selection.
			 */
			TreePath path = evt.getNewLeadSelectionPath();
			if (null == path) {
				// Nothing is selected.
				return;
			}

			Object nodeInfo = ((DefaultMutableTreeNode) path
					.getLastPathComponent()).getUserObject();

			if (nodeInfo instanceof Tradestrategy) {
				Tradestrategy tradestrategy = (Tradestrategy) nodeInfo;
				periodEditorComboBox.setItem(BarSize.newInstance(tradestrategy
						.getBarSize()));
				int currentTabIndex = -1;
				for (int index = 0; index < m_jTabbedPaneContract.getTabCount(); index++) {
					ChartPanel chartPanel = (ChartPanel) m_jTabbedPaneContract
							.getComponentAt(index);
					if ((null != chartPanel)
							&& chartPanel.getTradestrategy()
									.getIdTradeStrategy()
									.equals(tradestrategy.getIdTradeStrategy())) {
						currentTabIndex = index;
						break;
					}
				}
				if (currentTabIndex == -1) {
					ChartPanel chartPanel = createChartPanel(tradestrategy);
					m_jTabbedPaneContract.add(chartPanel.getCandlestickChart()
							.getName(), chartPanel);
					currentTabIndex = m_jTabbedPaneContract.getTabCount() - 1;
					m_jTabbedPaneContract.setTabComponentAt(currentTabIndex,
							new TabbedCloseButton(m_jTabbedPaneContract, this));
				}
				m_jTabbedPaneContract.setSelectedIndex(currentTabIndex);
			}
		} catch (PersistentModelException ex) {
			setErrorMessage("Error refreshing Tradestrategy.", ex.getMessage(),
					ex);
		} catch (Exception ex) {
			setErrorMessage("Error enabling chart.", ex.getMessage(), ex);
		}
	}

	/**
	 * Method doTransfer. This method may be called from this panel or the
	 * Portfolio Tab or Trading Tab.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void doTransfer(Tradestrategy tradestrategy) {
		brokerDataButton.setTransferObject(tradestrategy);
	}

	/**
	 * Method stateChanged. Different tab selected.
	 * 
	 * @param evt
	 *            ChangeEvent
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent evt) {
		// When a different tab is selected set the index
		if (evt.getSource() instanceof JTabbedPane) {
			JTabbedPane selectedTab = (JTabbedPane) evt.getSource();
			if (selectedTab.isShowing()) {
				this.reFreshTab();
			}
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
		this.reFreshTab();
	}

	/**
	 * Method itemStateChanged.
	 * 
	 * @param e
	 *            ItemEvent
	 * @see java.awt.event.ItemListener#itemStateChanged(ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			ChartPanel currentTab = (ChartPanel) m_jTabbedPaneContract
					.getSelectedComponent();
			Integer newPeriod = new Integer(((BarSize) e.getItem()).getCode());
			if (newPeriod.equals(BarSize.DAY)) {
				newPeriod = currentTab.getTradestrategy().getBarSize();
			}

			if (null != currentTab) {
				if (newPeriod.compareTo(currentTab.getTradestrategy()
						.getBarSize()) > -1) {
					currentTab.getTradestrategy().getDatasetContainer()
							.changeCandleSeriesPeriod(newPeriod);
					this.clearStatusBarMessage();
				} else {
					this.setStatusBarMessage(
							"Time period not supported by candle series",
							BasePanel.WARNING);
				}
			}
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
	 * Method createChartPanel.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return ChartPanel
	 * @throws PersistentModelException
	 */
	private ChartPanel createChartPanel(Tradestrategy tradestrategy)
			throws PersistentModelException {

		Date startDate = null;
		Date endDate = null;

		if (tradestrategy.getDatasetContainer().getBaseCandleSeries().isEmpty()) {
			endDate = TradingCalendar.getSpecificTime(tradestrategy
					.getTradingday().getClose(), TradingCalendar
					.getMostRecentTradingDay(TradingCalendar.addBusinessDays(
							tradestrategy.getTradingday().getClose(),
							backfillOffsetDays)));
			startDate = TradingCalendar.addDays(endDate,
					(-1 * (tradestrategy.getChartDays() - 1)));
			startDate = TradingCalendar.getMostRecentTradingDay(startDate);
			startDate = TradingCalendar.getSpecificTime(tradestrategy
					.getTradingday().getOpen(), startDate);
			List<Candle> candles = m_tradePersistentModel
					.findCandlesByContractDateRangeBarSize(tradestrategy
							.getContract().getIdContract(), startDate, endDate,
							tradestrategy.getBarSize());
			if (candles.isEmpty()) {
				this.setStatusBarMessage("No chart data available for "
						+ tradestrategy.getContract().getSymbol(),
						BasePanel.INFORMATION);
			} else {
				// Populate the candle series.
				CandleDataset.populateSeries(
						tradestrategy.getDatasetContainer(), candles);
				candles.clear();
				populateIndicatorCandleSeries(tradestrategy, startDate, endDate);
			}
		}

		ChartPanel chartPanel = new ChartPanel(tradestrategy);
		return chartPanel;
	}

	/**
	 * Method populateIndicatorCandleSeries. For any child indicators that are
	 * candle based create a Tradestrategy that will get the data. If this
	 * tradestrategy already exist share this with any other tradestrategy that
	 * requires this.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @throws PersistentModelException
	 */
	private void populateIndicatorCandleSeries(Tradestrategy tradestrategy,
			Date startDate, Date endDate) throws PersistentModelException {

		CandleDataset candleDataset = (CandleDataset) tradestrategy
				.getDatasetContainer().getIndicatorByType(
						IndicatorSeries.CandleSeries);
		if (null != candleDataset) {
			for (int seriesIndex = 0; seriesIndex < candleDataset
					.getSeriesCount(); seriesIndex++) {

				CandleSeries series = candleDataset.getSeries(seriesIndex);

				Contract contract = m_tradePersistentModel
						.findContractByUniqueKey(series.getSecType(),
								series.getSymbol(), series.getExchange(),
								series.getCurrency(), null);
				if (null != contract) {
					Tradestrategy childTradestrategy = new Tradestrategy(
							contract, tradestrategy.getTradingday(),
							new Strategy(), tradestrategy.getPortfolio(),
							new BigDecimal(0), null, null, false,
							tradestrategy.getChartDays(),
							tradestrategy.getBarSize());
					childTradestrategy.setDirty(false);

					List<Candle> indicatorCandles = m_tradePersistentModel
							.findCandlesByContractDateRangeBarSize(
									childTradestrategy.getContract()
											.getIdContract(), startDate,
									endDate, childTradestrategy.getBarSize());
					if (indicatorCandles.isEmpty()) {
						this.setStatusBarMessage("No chart data available for "
								+ childTradestrategy.getContract().getSymbol(),
								BasePanel.INFORMATION);
					} else {
						CandleDataset.populateSeries(
								childTradestrategy.getDatasetContainer(),
								indicatorCandles);
						indicatorCandles.clear();

						CandleSeries childSeries = childTradestrategy
								.getDatasetContainer().getBaseCandleSeries();
						childSeries.setDisplaySeries(series.getDisplaySeries());
						childSeries.setSeriesRGBColor(series
								.getSeriesRGBColor());
						childSeries.setSubChart(series.getSubChart());
						childSeries.setSymbol(series.getSymbol());
						childSeries.setSecType(series.getSecType());
						childSeries.setCurrency(series.getCurrency());
						childSeries.setExchange(series.getExchange());
						candleDataset.setSeries(seriesIndex, childSeries);
					}
				}
			}
		}
	}

	/**
	 */
	private class TradeOrderTableRowListener implements ListSelectionListener {
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
					int row = m_tradeOrderTable.convertRowIndexToModel(model
							.getLeadSelectionIndex());

					int i = 0;

					for (TradeOrder tradeOrder : m_tradeOrderModel.getData()
							.getTradeOrders()) {
						if (i == row) {
							cancelButton.setTransferObject(tradeOrder);
							executeButton.setTransferObject(tradeOrder);
							propertiesButton.setTransferObject(tradeOrder);
							break;
						}
						i++;
					}
				}
			}
		}
	}

	/**
	 * Method setStrategyLabel.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @param candlestickChart
	 *            CandlestickChart
	 * @throws BadLocationException
	 */
	private void setStrategyLabel(Tradestrategy tradestrategy) {
		try {
			m_strategyLabel.setText(null);
			String primaryExchange = "";
			String industry = "";
			String strategyDesc = "";
			if (null != tradestrategy) {
				primaryExchange = (tradestrategy.getContract()
						.getPrimaryExchange() == null ? "No Data Available"
						: tradestrategy.getContract().getPrimaryExchange());
				industry = (tradestrategy.getContract().getIndustry() == null ? "No Data Available"
						: tradestrategy.getContract().getIndustry());
				strategyDesc = (tradestrategy.getStrategy().getDescription() == null ? "No Data Available"
						: tradestrategy.getStrategy().getDescription());
			}

			CoreUtils.setDocumentText(m_strategyLabel.getDocument(),
					"Primary Exch: ", false, bold);
			CoreUtils.setDocumentText(m_strategyLabel.getDocument(),
					CoreUtils.padRight(primaryExchange, 8), false, null);
			CoreUtils.setDocumentText(m_strategyLabel.getDocument(),
					" Industry:", false, bold);
			CoreUtils.setDocumentText(m_strategyLabel.getDocument(),
					CoreUtils.padRight(industry, 30), false, null);
			CoreUtils.setDocumentText(m_strategyLabel.getDocument(),
					" Strategy:", false, bold);
			CoreUtils.setDocumentText(m_strategyLabel.getDocument(),
					CoreUtils.padRight(strategyDesc, 30), false, null);

		} catch (Exception ex) {
			this.setErrorMessage("Error setting Tradestrategy Label.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method reFreshTab.
	 * 
	 * @throws BadLocationException
	 */
	private void reFreshTab() {
		try {
			Tradestrategy tradestrategy = null;
			ChartPanel currentTab = (ChartPanel) m_jTabbedPaneContract
					.getSelectedComponent();
			if (null == currentTab) {
				m_tradeOrderModel.setData(new Tradestrategy());
			} else {
				/*
				 * Refresh the Tradestrategy this will get the latest orders.
				 */
				tradestrategy = m_tradePersistentModel
						.findTradestrategyById(currentTab.getTradestrategy());
				currentTab.setTradestrategy(tradestrategy);
				m_tradeOrderModel.setData(tradestrategy);
				RowSorter<?> rsDetail = m_tradeOrderTable.getRowSorter();
				rsDetail.setSortKeys(null);
				periodEditorComboBox.setItem(BarSize.newInstance(tradestrategy
						.getBarSize()));
			}
			/*
			 * Refresh the header label above the chart and buttons.
			 */
			setStrategyLabel(tradestrategy);
			enableChartButtons(tradestrategy);

			double netValue = 0;
			double commision = 0;
			String symbol = "";
			String side = "";
			String tier = "";
			String status = "";
			String portfolio = "";
			String risk = "";
			if (null != tradestrategy) {
				symbol = tradestrategy.getContract().getSymbol();
				side = (tradestrategy.getSide() == null ? "" : Side
						.newInstance(tradestrategy.getSide()).getDisplayName());
				tier = (tradestrategy.getTier() == null ? "" : Tier
						.newInstance(tradestrategy.getTier()).getDisplayName());
				status = (tradestrategy.getStatus() == null ? ""
						: TradestrategyStatus.newInstance(
								tradestrategy.getStatus()).getDisplayName());
				portfolio = tradestrategy.getPortfolio().getName();
				risk = currencyFormater
						.format((tradestrategy.getRiskAmount() == null ? 0
								: tradestrategy.getRiskAmount().doubleValue()));

				// Collections.sort(trade.getTradeOrders(), new
				// TradeOrder());

				/*
				 * Sum up orders that are filled and at the same time add the
				 * fill price. This happens when orders stop out as there are
				 * multiple stop orders for a position with multiple targets.
				 */

				TradeOrder prevTradeOrder = null;
				Integer prevIdTradePosition = null;

				for (TradeOrder order : tradestrategy.getTradeOrders()) {

					if (order.getIsFilled()) {
						Integer quantity = order.getFilledQuantity();
						if (null == prevIdTradePosition
								|| prevIdTradePosition != order
										.getTradePosition()
										.getIdTradePosition()) {

							netValue = netValue
									+ order.getTradePosition()
											.getTotalNetValue().doubleValue();

							commision = commision
									+ order.getTradePosition()
											.getTotalCommission().doubleValue();

							prevIdTradePosition = order.getTradePosition()
									.getIdTradePosition();
						}

						if (null != prevTradeOrder) {
							if (prevTradeOrder.getIsFilled()
									&& prevTradeOrder.getFilledDate().equals(
											order.getFilledDate())
									&& prevTradeOrder.getAverageFilledPrice()
											.equals(order
													.getAverageFilledPrice())) {
								quantity = quantity
										+ prevTradeOrder.getFilledQuantity();
							}
						}
						currentTab.getCandlestickChart().addBuySellTradeArrow(
								order.getAction(),
								new Money(order.getAverageFilledPrice()),
								order.getFilledDate(), quantity);
					}
					prevTradeOrder = order;
				}
			}

			netValue = netValue - commision;

			m_tradeLabel.setText(null);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(), "Symbol:",
					false, bold);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					CoreUtils.padRight(symbol, 10), false, null);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(), " Side:",
					false, bold);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					CoreUtils.padRight(side, 6), false, null);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(), " Tier:",
					false, bold);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					CoreUtils.padRight(tier, 6), false, null);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(), " Status:",
					false, bold);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					CoreUtils.padRight(status, 20), false, null);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					" Portfolio:", false, bold);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					CoreUtils.padRight(portfolio, 15), false, null);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(), " Risk:",
					false, bold);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					CoreUtils.padLeft(risk, 10), false, null);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					" Net Total:", false, bold);
			if (netValue < 0) {
				CoreUtils.setDocumentText(m_tradeLabel.getDocument(), CoreUtils
						.padLeft(currencyFormater.format(netValue), 10), false,
						colorRedAttr);
			} else if (netValue > 0) {
				CoreUtils.setDocumentText(m_tradeLabel.getDocument(), CoreUtils
						.padLeft(currencyFormater.format(netValue), 10), false,
						colorGreenAttr);
			} else {
				CoreUtils.setDocumentText(m_tradeLabel.getDocument(), CoreUtils
						.padLeft(currencyFormater.format(netValue), 10), false,
						null);
			}
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(), " Comms:",
					false, bold);
			CoreUtils.setDocumentText(m_tradeLabel.getDocument(),
					CoreUtils.padLeft(currencyFormater.format(commision), 10),
					false, null);

		} catch (Exception ex) {
			this.setErrorMessage("Error refreshing Tab.", ex.getMessage(), ex);
		}
	}

	/**
	 * Method enableChartButtons.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @throws Exception
	 */
	private void enableChartButtons(Tradestrategy tradestrategy)
			throws Exception {
		propertiesButton.setEnabled(false);
		executeButton.setEnabled(false);
		brokerDataButton.setEnabled(false);
		cancelButton.setEnabled(false);
		cancelStrategiesButton.setEnabled(false);
		m_tradeOrderTable.enablePopupMenu(false);
		periodEditorComboBox.setEnabled(false);
		refreshButton.setEnabled(false);
		brokerDataButton.setTransferObject(tradestrategy);
		cancelStrategiesButton.setTransferObject(tradestrategy);
		refreshButton.setTransferObject(tradestrategy);

		if (null != tradestrategy) {
			propertiesButton.setEnabled(true);
			cancelStrategiesButton.setEnabled(true);
			brokerDataButton.setEnabled(true);
			periodEditorComboBox.setEnabled(true);
			if (this.isConnected()) {
				executeButton.setEnabled(true);
				refreshButton.setEnabled(true);
				cancelButton.setEnabled(true);
				m_tradeOrderTable.enablePopupMenu(true);
			}
		}
	}

	/**
	 */
	class ChartPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6151552506157648783L;
		private Tradestrategy tradestrategy = null;
		private CandlestickChart candlestickChart = null;

		/**
		 * Constructor for ChartPanel.
		 * 
		 * @param tradestrategy
		 *            Tradestrategy
		 */
		ChartPanel(Tradestrategy tradestrategy) {
			this.tradestrategy = tradestrategy;
			this.setLayout(new BorderLayout());

			String ledgend = "("
					+ tradestrategy.getContract().getSymbol()
					+ ") "
					+ (tradestrategy.getContract().getDescription() == null ? "Contract details not available."
							: tradestrategy.getContract().getDescription());
			this.candlestickChart = new CandlestickChart(ledgend,
					tradestrategy.getDatasetContainer(),
					tradestrategy.getTradingday());
			this.candlestickChart.setName(tradestrategy.getContract()
					.getSymbol());
			this.add(this.candlestickChart);
		}

		/**
		 * Method getTradestrategy.
		 * 
		 * @return Tradestrategy
		 */
		public Tradestrategy getTradestrategy() {
			return this.tradestrategy;
		}

		/**
		 * Method setTradestrategy.
		 * 
		 * @param tradestrategy
		 *            Tradestrategy
		 */
		public void setTradestrategy(Tradestrategy tradestrategy) {
			this.tradestrategy = tradestrategy;
		}

		/**
		 * Method getCandlestickChart.
		 * 
		 * @return CandlestickChart
		 */
		public CandlestickChart getCandlestickChart() {
			return this.candlestickChart;
		}
	}

	/**
	 */
	class AllocationMethodPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5972331201407363985L;

		/**
		 * Constructor for FAPropertiesPanel.
		 * 
		 * @param tradeOrder
		 *            TradeOrder
		 * @throws Exception
		 */

		public AllocationMethodPanel(final TradeOrder tradeOrder)
				throws Exception {

			GridBagLayout gridBagLayout1 = new GridBagLayout();
			this.setLayout(gridBagLayout1);
			this.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder("Select Profile or Group"),
					BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			JLabel profileLabel = new JLabel("Profile");
			JLabel groupLabel = new JLabel("Group");
			JLabel mthodLabel = new JLabel("Method");
			JLabel percentLabel = new JLabel("Percent");

			DecodeComboBoxEditor profileEditorComboBox = new DecodeComboBoxEditor(
					DAOProfile.newInstance().getCodesDecodes());
			DecodeComboBoxRenderer profileTableRenderer = new DecodeComboBoxRenderer();
			profileEditorComboBox.setRenderer(profileTableRenderer);
			if (null != tradeOrder.getFAProfile())
				profileEditorComboBox.setItem(DAOProfile.newInstance(tradeOrder
						.getFAProfile()));
			profileEditorComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (!Decode.NONE.equals(((DAOProfile) e.getItem())
								.getDisplayName())) {
							tradeOrder
									.setFAProfile(((Portfolio) ((DAOProfile) e
											.getItem()).getObject()).getName());
						} else {
							tradeOrder.setFAProfile(null);
						}
					}
				}
			});

			DecodeComboBoxEditor groupEditorComboBox = new DecodeComboBoxEditor(
					DAOGroup.newInstance().getCodesDecodes());
			DecodeComboBoxRenderer groupTableRenderer = new DecodeComboBoxRenderer();
			groupEditorComboBox.setRenderer(groupTableRenderer);
			if (null != tradeOrder.getFAGroup())
				groupEditorComboBox.setItem(DAOGroup.newInstance(tradeOrder
						.getFAGroup()));
			groupEditorComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (!Decode.NONE.equals(((DAOGroup) e.getItem())
								.getDisplayName())) {
							tradeOrder.setFAGroup(((Portfolio) ((DAOGroup) e
									.getItem()).getObject()).getName());
						} else {
							tradeOrder.setFAGroup(null);
						}
					}
				}
			});

			DecodeComboBoxEditor methodEditorComboBox = new DecodeComboBoxEditor(
					AllocationMethod.newInstance().getCodesDecodes());
			DecodeComboBoxRenderer methodTableRenderer = new DecodeComboBoxRenderer();
			methodEditorComboBox.setRenderer(methodTableRenderer);
			if (null != tradeOrder.getFAMethod())
				methodEditorComboBox.setItem(AllocationMethod
						.newInstance(tradeOrder.getFAMethod()));
			methodEditorComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (!Decode.NONE.equals(((AllocationMethod) e.getItem())
								.getDisplayName())) {
							tradeOrder.setFAMethod(((AllocationMethod) e
									.getItem()).getCode());
						} else {
							tradeOrder.setFAMethod(null);
						}
					}
				}
			});
			NumberFormat percentFormat = NumberFormat.getNumberInstance();
			percentFormat.setMinimumFractionDigits(2);
			final JFormattedTextField percentTextField = new JFormattedTextField(
					percentFormat);
			if (null != tradeOrder.getFAPercent())
				percentTextField.setText(Integer.toString(new Integer(
						tradeOrder.getFAPercent().intValue())));
			percentTextField
					.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent e) {
							Object source = e.getSource();
							if ("value".equals(e.getPropertyName())) {
								if (source == percentTextField) {
									if (percentTextField.isEditValid()
											&& null != e.getNewValue()) {
										Number rate = ((Number) percentTextField
												.getValue()).doubleValue();
										tradeOrder.setFAPercent(new BigDecimal(
												rate.doubleValue()));
									}
								}
							}
						}
					});
			this.add(profileLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 0, 0), 20, 5));
			this.add(groupLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 0, 0), 20, 5));
			this.add(mthodLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 0, 0), 20, 5));
			this.add(percentLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(1, 1, 0, 0), 20, 5));

			this.add(profileEditorComboBox, new GridBagConstraints(1, 1, 1, 1,
					1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 43),
					196, 0));
			this.add(groupEditorComboBox, new GridBagConstraints(1, 2, 1, 1,
					1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 43),
					196, 0));
			this.add(methodEditorComboBox, new GridBagConstraints(1, 3, 1, 1,
					1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 43),
					196, 0));
			this.add(percentTextField, new GridBagConstraints(1, 4, 1, 1, 1.0,
					0.0, GridBagConstraints.WEST,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 43),
					196, 0));
		}
	}
}
