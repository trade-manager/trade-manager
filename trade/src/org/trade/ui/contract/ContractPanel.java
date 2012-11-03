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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.Tier;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Trade;
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
import org.trade.ui.base.Tree;
import org.trade.ui.chart.CandlestickChart;
import org.trade.ui.models.TradeOrderTableModel;
import org.trade.ui.models.TradingdayTreeModel;
import org.trade.ui.tables.TradeOrderTable;
import org.trade.ui.tables.renderer.TradingdayTreeCellRenderer;
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
	private JLabel m_strategyLabel = null;
	private BaseButton executeButton = null;
	private BaseButton brokerDataButton = null;
	private BaseButton cancelButton = null;
	private BaseButton cancelStrategiesButton = null;
	private BaseButton refreshButton = null;
	private BaseButton closeAllButton = null;
	private DecodeComboBoxEditor periodEditorComboBox = null;
	private Integer backfillOffsetDays = 0;
	private Boolean connected = new Boolean(false);
	private static final NumberFormat formater = NumberFormat
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
	@SuppressWarnings("unchecked")
	public ContractPanel(Tradingdays tradingdays, TabbedAppPanel controller,
			PersistentModel tradePersistentModel) {

		try {
			formater.setMinimumFractionDigits(2);
			StyleConstants.setBold(bold, true);
			StyleConstants.setBackground(colorRedAttr, Color.RED);
			StyleConstants.setBackground(colorGreenAttr, Color.GREEN);

			backfillOffsetDays = ConfigProperties
					.getPropAsInt("trade.backfill.offsetDays");
			this.setLayout(new BorderLayout());
			m_tradePersistentModel = tradePersistentModel;
			m_tradingdays = tradingdays;
			// This allows the controller to listen to these events
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
					new RowListener());

			m_treeModel = new TradingdayTreeModel(m_tradingdays);
			m_tree = new Tree(m_treeModel);
			// Listen for when the selection changes.
			m_tree.addTreeSelectionListener(this);
			m_tree.setCellRenderer(new TradingdayTreeCellRenderer());

			JPanel jPanel1 = new JPanel(new BorderLayout());
			FlowLayout flowLayout1 = new FlowLayout();
			flowLayout1.setAlignment(FlowLayout.RIGHT);
			JPanel jPanel3 = new JPanel(flowLayout1);
			jPanel3.add(closeAllButton, null);
			// Contract Panel
			JLabel jLabel1 = new JLabel("Trading Day");
			jPanel1.add(jLabel1, BorderLayout.WEST);
			jPanel1.add(jPanel3, BorderLayout.EAST);
			JScrollPane jScrollPane1 = new JScrollPane();
			jScrollPane1.getViewport().add(m_tree, BorderLayout.CENTER);
			JPanel jPanel2 = new JPanel(new BorderLayout());
			jPanel2.add(jScrollPane1, BorderLayout.CENTER);
			jPanel2.add(jPanel1, BorderLayout.NORTH);

			// Chart Panel
			JLabel jLabel4 = new JLabel("Period:");
			periodEditorComboBox = new DecodeComboBoxEditor(
					(new BarSize()).getCodesDecodes());
			DecodeComboBoxRenderer periodRenderer = new DecodeComboBoxRenderer();
			periodEditorComboBox.setRenderer(periodRenderer);
			periodEditorComboBox.setItem(BarSize.newInstance(BarSize.FIVE_MIN));
			periodEditorComboBox.addItemListener(this);
			FlowLayout flowLayout2 = new FlowLayout();
			flowLayout2.setAlignment(FlowLayout.LEFT);
			JPanel jPanel6 = new JPanel(flowLayout2);
			jPanel6.add(brokerDataButton, null);
			jPanel6.add(cancelStrategiesButton, null);
			jPanel6.add(jLabel4, null);
			jPanel6.add(periodEditorComboBox, null);

			FlowLayout flowLayout3 = new FlowLayout();
			flowLayout3.setAlignment(FlowLayout.RIGHT);
			JPanel jPanel12 = new JPanel(flowLayout3);

			m_strategyLabel = new JLabel("Strategy: ");
			jPanel12.add(m_strategyLabel, null);
			JPanel jPanel11 = new JPanel(new BorderLayout());
			jPanel11.add(jPanel6, BorderLayout.WEST);
			jPanel11.add(jPanel12, BorderLayout.EAST);
			JPanel jPanel7 = new JPanel(new BorderLayout());
			jPanel7.add(m_jTabbedPaneContract, BorderLayout.CENTER);
			JScrollPane jScrollPane3 = new JScrollPane();
			jScrollPane3.getViewport().add(jPanel7, BorderLayout.CENTER);
			JPanel jPanel9 = new JPanel(new BorderLayout());
			jPanel9.add(jScrollPane3, BorderLayout.CENTER);
			jPanel9.add(jPanel11, BorderLayout.SOUTH);
			// Order Panel
			m_tradeOrderTable.setFont(new Font("Monospaced", Font.PLAIN, 12));
			FlowLayout flowLayout4 = new FlowLayout();
			flowLayout4.setAlignment(FlowLayout.LEFT);
			JPanel jPanel5 = new JPanel(flowLayout4);
			jPanel5.add(executeButton, null);
			jPanel5.add(cancelButton, null);
			jPanel5.add(refreshButton, null);

			JPanel jPanel10 = new JPanel(new BorderLayout());
			jPanel10.add(jPanel5, BorderLayout.WEST);
			JScrollPane jScrollPane2 = new JScrollPane();
			jScrollPane2.getViewport().add(m_tradeOrderTable,
					BorderLayout.CENTER);
			jScrollPane2.setBorder(new BevelBorder(BevelBorder.LOWERED));
			JPanel jPanel16 = new JPanel(new BorderLayout());
			Dimension d = m_tradeOrderTable.getPreferredSize();
			// Make changes to [i]d[/i] if you like...
			m_tradeOrderTable.setPreferredScrollableViewportSize(d);

			m_tradeLabel = new JEditorPane("text/rtf", "");
			m_tradeLabel.setAutoscrolls(false);
			m_tradeLabel.setEditable(false);
			jPanel16.add(m_tradeLabel, BorderLayout.NORTH);
			jPanel16.add(jScrollPane2, BorderLayout.CENTER);
			jPanel16.add(jPanel10, BorderLayout.SOUTH);

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
			mainSplitPane.setResizeWeight(0.05d);
			this.add(mainSplitPane, BorderLayout.CENTER);
			m_jTabbedPaneContract.addChangeListener(this);
			enableChartButtons(null);

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

	public void doCloseAll() {
		try {
			int tabsCount = m_jTabbedPaneContract.getTabCount();
			for (int index = 0; index < tabsCount; index++) {
				doClose(new Integer(0));
			}

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
						if (null != tradestrategy.getIdTradeStrategy()) {
							Tradestrategy refreshedTradestrategy = m_tradePersistentModel
									.findTradestrategyById(tradestrategy);
							m_tradeOrderModel.setData(refreshedTradestrategy);
							RowSorter<?> rsDetail = m_tradeOrderTable
									.getRowSorter();
							rsDetail.setSortKeys(null);
							ChartPanel currentTab = (ChartPanel) m_jTabbedPaneContract
									.getSelectedComponent();
							if (null != currentTab) {
								currentTab
										.setTradestrategy(refreshedTradestrategy);
								setTradeLabel(refreshedTradestrategy,
										currentTab.getCandlestickChart());
							}
						}

					} catch (PersistentModelException ex) {
						setErrorMessage("Error saving Trade Strategies.",
								ex.getMessage(), ex);
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
	public void valueChanged(TreeSelectionEvent e) {
		// Returns the last path element of the selection.
		// This method is useful only when the selection model allows a single
		// selection.
		try {
			TreePath path = e.getNewLeadSelectionPath();
			if (null == path) {
				// Nothing is selected.
				return;
			}

			Object nodeInfo = ((DefaultMutableTreeNode) path
					.getLastPathComponent()).getUserObject();

			if (nodeInfo instanceof Tradestrategy) {
				Tradestrategy tradestrategy = (Tradestrategy) nodeInfo;
				/*
				 * Refresh the tradestrategy this will get any orders that are
				 * there. Then add the chart panel.
				 */
				tradestrategy = m_tradePersistentModel
						.findTradestrategyById(tradestrategy);
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
			} else {
				enableChartButtons(null);
			}
		} catch (PersistentModelException ex) {
			setErrorMessage("Error refreshing Tradestrategy.", ex.getMessage(),
					ex);
		} catch (Exception ex) {
			setErrorMessage("Error enabling chart.", ex.getMessage(), ex);
		}
	}

	/*
	 * This method may be called from this panel or the Portfolio Tab or Trading
	 * Tab.
	 */

	/**
	 * Method doTransfer.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void doTransfer(Tradestrategy tradestrategy) {
		brokerDataButton.setTransferObject(tradestrategy);
	}

	/**
	 * Method stateChanged.
	 * 
	 * @param evt
	 *            ChangeEvent
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent evt) {
		try {
			// When a different tab is selected set the index
			if (evt.getSource() instanceof JTabbedPane) {
				JTabbedPane selectedTab = (JTabbedPane) evt.getSource();
				if (selectedTab.isShowing()) {
					ChartPanel currentTab = (ChartPanel) selectedTab
							.getSelectedComponent();
					if (null != currentTab) {
						enableChartButtons(currentTab.getTradestrategy());
					} else {
						enableChartButtons(null);
					}
				}
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error changing chart timeframe.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method setConnected.
	 * 
	 * @param connected
	 *            Boolean
	 */
	public void setConnected(Boolean connected) {
		try {
			this.connected = connected;
			if (m_jTabbedPaneContract.getTabCount() > 0) {
				ChartPanel chart = (ChartPanel) m_jTabbedPaneContract
						.getSelectedComponent();
				if (null != chart) {
					enableChartButtons(chart.getTradestrategy());
					return;
				}
			}
			enableChartButtons(null);

		} catch (Exception ex) {
			this.setErrorMessage("Error setting connection.", ex.getMessage(),
					ex);
		}
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

		if (tradestrategy.getDatasetContainer().getBaseCandleSeries().isEmpty()) {

			Date endDate = TradingCalendar.getBusinessDayEnd(TradingCalendar
					.getMostRecentTradingDay(TradingCalendar.addBusinessDays(
							tradestrategy.getTradingday().getClose(),
							backfillOffsetDays)));
			Date startDate = TradingCalendar.addDays(endDate,
					(-1 * (tradestrategy.getChartDays() - 1)));
			startDate = TradingCalendar.getMostRecentTradingDay(startDate);
			if (startDate.after(tradestrategy.getTradingday().getOpen())) {
				startDate = tradestrategy.getTradingday().getOpen();
			}
			List<Candle> candles = m_tradePersistentModel
					.findCandlesByContractAndDateRange(tradestrategy
							.getContract().getIdContract(), startDate, endDate);
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

	/*
	 * For any child indicators that are candle based create a Tradestrategy
	 * that will get the data. If this tradestrategy already exist share this
	 * with any other tradestrategy that requires this.
	 */
	/**
	 * Method populateIndicatorCandleSeries.
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
				.getDatasetContainer().getIndicators(
						IndicatorSeries.CandleSeries);
		if (null != candleDataset) {
			for (int seriesIndex = 0; seriesIndex < candleDataset
					.getSeriesCount(); seriesIndex++) {

				CandleSeries series = candleDataset.getSeries(seriesIndex);

				Contract contract = m_tradePersistentModel
						.findContractByUniqueKey(series.getSecType(),
								series.getSymbol(), series.getExchange(),
								series.getCurrency());
				if (null != contract) {
					Tradestrategy childTradestrategy = new Tradestrategy(
							contract, tradestrategy.getTradingday(),
							new Strategy(), tradestrategy.getTradeAccount(),
							new BigDecimal(0), null, null, false,
							tradestrategy.getChartDays(),
							tradestrategy.getBarSize());
					childTradestrategy.setDirty(false);

					List<Candle> indicatorCandles = m_tradePersistentModel
							.findCandlesByContractAndDateRange(
									childTradestrategy.getContract()
											.getIdContract(), startDate,
									endDate);
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
	private class RowListener implements ListSelectionListener {
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
					for (Trade trade : m_tradeOrderModel.getData().getTrades()) {
						for (TradeOrder tradeOrder : trade.getTradeOrders()) {
							if (i == row) {
								cancelButton.setTransferObject(tradeOrder);
								executeButton.setTransferObject(tradeOrder);
								break;
							}
							i++;
						}
					}
				}
			}
		}
	}

	/**
	 * Method setTradeLabel.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @param candlestickChart
	 *            CandlestickChart
	 */
	private void setTradeLabel(Tradestrategy tradestrategy,
			CandlestickChart candlestickChart) {
		try {

			double profitLoss = 0;
			double commision = 0;
			setMessageText("Symbol:", false, false, bold);
			setMessageText(CoreUtils.padRight(tradestrategy.getContract()
					.getSymbol(), 10), true, false, null);
			setMessageText(" Side:", true, false, bold);
			setMessageText(CoreUtils.padRight(
					Side.newInstance(tradestrategy.getSide()).getDisplayName(),
					6), true, false, null);
			setMessageText(" Tier:", true, false, bold);
			setMessageText(CoreUtils.padRight(
					(tradestrategy.getTier() == null ? "" : Tier.newInstance(
							tradestrategy.getTier()).getDisplayName()), 6),
					true, false, null);
			setMessageText(" Status:", true, false, bold);
			setMessageText(
					CoreUtils.padRight(
							(tradestrategy.getStatus() == null ? ""
									: TradestrategyStatus.newInstance(
											tradestrategy.getStatus())
											.getDisplayName()), 20), true,
					false, null);
			setMessageText(" Account:", true, false, bold);
			setMessageText(CoreUtils.padRight(tradestrategy.getTradeAccount()
					.toString(), 10), true, false, null);
			setMessageText(" Risk:", true, false, bold);
			setMessageText(CoreUtils.padLeft(formater.format((tradestrategy
					.getRiskAmount() == null ? 0 : tradestrategy
					.getRiskAmount()).doubleValue()), 10), true, false, null);
			for (Trade trade : tradestrategy.getTrades()) {
				if (!trade.getIsOpen()) {

					if (null != trade.getProfitLoss()) {
						profitLoss = profitLoss
								+ trade.getProfitLoss().doubleValue();
					}
					if (null != trade.getTotalCommission()) {
						commision = commision
								+ trade.getTotalCommission().doubleValue();
					}
				}
				// Collections.sort(trade.getTradeOrders(), new TradeOrder());
				TradeOrder prevTradeOrder = null;

				/*
				 * Sum up orders that are filled and at the same time add the
				 * fill price. This happens when orders stop out as there are
				 * multiple stop orders for a position with multiple targets.
				 */
				for (TradeOrder order : trade.getTradeOrders()) {
					Integer quantity = order.getFilledQuantity();
					if (order.getIsFilled()) {
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
						candlestickChart.addBuySellTradeArrow(
								order.getAction(),
								new Money(order.getAverageFilledPrice()),
								order.getFilledDate(), quantity);
					}
					prevTradeOrder = order;
				}
			}
			setMessageText(" Profit:", true, false, bold);
			if (profitLoss < 0) {
				setMessageText(
						CoreUtils.padLeft(formater.format(profitLoss), 10),
						true, false, colorRedAttr);
			} else if (profitLoss > 0) {
				setMessageText(
						CoreUtils.padLeft(formater.format(profitLoss), 10),
						true, false, colorGreenAttr);
			} else {
				setMessageText(
						CoreUtils.padLeft(formater.format(profitLoss), 10),
						true, false, null);
			}
			setMessageText(" Comms:", true, false, bold);
			setMessageText(CoreUtils.padLeft(formater.format(commision), 10),
					true, false, null);

		} catch (ValueTypeException ex) {
			this.setErrorMessage("Error initializing valueTypes.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method setMessageText.
	 * 
	 * @param content
	 *            String
	 * @param append
	 *            boolean
	 * @param newLine
	 *            boolean
	 * @param attrSet
	 *            SimpleAttributeSet
	 */
	private void setMessageText(String content, boolean append,
			boolean newLine, SimpleAttributeSet attrSet) {
		if (!append)
			m_tradeLabel.setText(null);
		if (null != content) {
			Document doc = m_tradeLabel.getDocument();
			try {
				doc.insertString(doc.getLength(), content, attrSet);
				if (newLine)
					doc.insertString(doc.getLength(), "\n", null);
			} catch (BadLocationException ex1) {
				this.setErrorMessage("Exception setting messge: ",
						ex1.getMessage(), ex1);
			}
		}
	}

	/**
	 * Method enableChartButtons.
	 * 
	 * @param transferObject
	 *            Tradestrategy
	 * @throws Exception
	 */
	private void enableChartButtons(Tradestrategy transferObject)
			throws Exception {

		boolean enabled = false;
		if (this.isConnected()) {
			if (null != transferObject) {
				enabled = true;
			}
			executeButton.setEnabled(enabled);
			brokerDataButton.setEnabled(enabled);
			cancelButton.setEnabled(enabled);
			cancelStrategiesButton.setEnabled(enabled);
			m_tradeOrderTable.enablePopupMenu(enabled);

		} else {
			executeButton.setEnabled(enabled);
			brokerDataButton.setEnabled(enabled);
			cancelButton.setEnabled(enabled);
			cancelStrategiesButton.setEnabled(enabled);
			m_tradeOrderTable.enablePopupMenu(enabled);
		}
		if (null != transferObject) {
			enabled = true;
		}
		refreshButton.setEnabled(enabled);
		periodEditorComboBox.setEnabled(enabled);
		brokerDataButton.setTransferObject(transferObject);
		cancelStrategiesButton.setTransferObject(transferObject);
		refreshButton.setTransferObject(transferObject);

		if (null != transferObject) {
			m_tradeOrderModel.setData(transferObject);
			StringBuilder result = new StringBuilder("<html>");
			result.append("<b>Primary Exch: </b> ")
					.append((transferObject.getContract().getPrimaryExchange() == null ? "No data"
							: transferObject.getContract().getPrimaryExchange()));
			result.append("<b> Industry: </b> ")
					.append((transferObject.getContract().getIndustry() == null ? "No data"
							: transferObject.getContract().getIndustry()));
			result.append("<b> Strategy: </b> ")
					.append((transferObject.getStrategy().getDescription() == null ? transferObject
							.getStrategy().getName() : transferObject
							.getStrategy().getDescription()));

			m_strategyLabel.setText(result.toString());
			ChartPanel currentTab = (ChartPanel) m_jTabbedPaneContract
					.getSelectedComponent();
			setTradeLabel(transferObject, currentTab.getCandlestickChart());
			periodEditorComboBox.setItem(BarSize.newInstance(transferObject
					.getBarSize()));
		} else {
			m_tradeOrderModel.setData(new Tradestrategy());
			m_strategyLabel.setText("Strategy: ");
			m_tradeLabel.setText("Symbol: ");
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
			String ledgend = "("
					+ tradestrategy.getContract().getSymbol()
					+ ") "
					+ (tradestrategy.getContract().getDescription() == null ? "Contract details not available."
							: tradestrategy.getContract().getDescription());
			this.candlestickChart = new CandlestickChart(ledgend,
					tradestrategy.getDatasetContainer());
			candlestickChart.setName(tradestrategy.getContract().getSymbol());
			this.setLayout(new BorderLayout());
			this.add(candlestickChart, null);
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
}
