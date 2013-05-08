/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
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
package org.trade.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerChangeListener;
import org.trade.broker.BrokerModel;
import org.trade.broker.BrokerModelException;
import org.trade.core.factory.ClassFactory;
import org.trade.core.lookup.DBTableLookupServiceProvider;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.DynamicCode;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.PortfolioAccount;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.TradePosition;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.strategy.StrategyChangeListener;
import org.trade.strategy.StrategyRule;
import org.trade.strategy.StrategyRuleException;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.ComponentPrintService;
import org.trade.ui.base.TabbedAppPanel;
import org.trade.ui.base.TextDialog;
import org.trade.ui.configuration.ConfigurationPanel;
import org.trade.ui.contract.ContractPanel;
import org.trade.ui.portfolio.PortfolioPanel;
import org.trade.ui.strategy.StrategyPanel;
import org.trade.ui.tradingday.ConnectionPane;
import org.trade.ui.tradingday.TradingdayPanel;

/**
 * 
 * Main application classes are ..
 * 
 * org.trade.ui.TradeMainControllerPanel
 * 
 * The applications main controller this listens to TWSBrokerModel and handles
 * all UI events that are common across all tabs. Each tab has its own
 * controller to handle specific Tab related UI events e.g. Get Data/Run
 * Strategy. Otherwise they are handled in the controller of the individual Tab
 * e.g. Save/Search.
 * 
 * org.trade.broker.TWSBrokerModel
 * 
 * This handles all the requests/responses from IB TWS. This class is listened
 * to by the following TradeMainControllerPanel AbstractStrategyRule
 * 
 * org.trade.strategy.AbstractStrategyRule
 * 
 * Base strategy class. All strategies inherit from this class. Implemented
 * AbstractStrategyRule listen to the BaseCandleSeries which is the series that
 * is updated as new candle data is received via the TWSBrokerModel.
 * 
 * org.trade.strategy.data.StrategyData
 * 
 * This class contains datasets for a specific strategy. The datasets are a
 * BaseCandleSeries this is the dataset that received data from the TWS API and
 * is listened to by any running strategies. The second Candle series is used to
 * display charts. Other series are used for indicators that have been setup for
 * the strategy.
 * 
 */
public class TradeMainControllerPanel extends TabbedAppPanel implements
		BrokerChangeListener, StrategyChangeListener {

	private static final long serialVersionUID = -7717664255656430982L;

	private final static Logger _log = LoggerFactory
			.getLogger(TradeMainControllerPanel.class);

	private static Tradingdays m_tradingdays = null;
	private BrokerModel m_brokerModel = null;
	private PersistentModel m_tradePersistentModel = null;
	private BrokerDataRequestProgressMonitor brokerDataRequestProgressMonitor = null;
	private static final ConcurrentHashMap<Integer, Tradestrategy> _indicatorTradestrategy = new ConcurrentHashMap<Integer, Tradestrategy>();

	private TradingdayPanel tradingdayPanel = null;
	private ContractPanel contractPanel = null;
	private ConfigurationPanel configurationPanel = null;
	private StrategyPanel strategyPanel = null;
	private PortfolioPanel portfolioPanel = null;
	private DynamicCode dynacode = null;

	/**
	 * The main application controller which interacts between the view and the
	 * applications underlying models. This controller also listens to events
	 * from the broker model.
	 * <p>
	 * 
	 * @param frame
	 *            the main application Frame.
	 * 
	 */

	public TradeMainControllerPanel(Frame frame) {
		super(frame);
		try {

			/*
			 * Create the customized application Menu/Tool Bar.
			 */
			setMenu(new TradeMainPanelMenu(this));
			/*
			 * This allows the main controller to receive all events as it is
			 * allways considered selected.
			 */
			setSelected(true);
			m_tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			Tradingday tradingday = Tradingday.newInstance(TradingCalendar
					.getMostRecentTradingDay(new Date()));
			Tradingday todayTradingday = m_tradePersistentModel
					.findTradingdayByOpenCloseDate(tradingday.getOpen(),
							tradingday.getClose());
			if (null != todayTradingday)
				tradingday = todayTradingday;
			m_tradingdays = new Tradingdays();
			m_tradingdays.add(tradingday);
			String strategyDir = ConfigProperties
					.getPropAsString("trade.strategy.default.dir");
			dynacode = new DynamicCode();
			dynacode.addSourceDir(new File(strategyDir));

			/**
			 * Constructs a new Trading tab that contains all information
			 * related to the tradeingday i.e. which strategy to trade, contract
			 * information whether to trade. This is the tab used to load
			 * contracts and decide how to trade them.
			 * 
			 */

			tradingdayPanel = new TradingdayPanel(m_tradingdays, this,
					m_tradePersistentModel);
			/**
			 * Constructs a new Contract tab that contains all information
			 * related to the Tradestrategy i.e. charts, Orders for a particular
			 * trading day.
			 * 
			 */

			contractPanel = new ContractPanel(m_tradingdays, this,
					m_tradePersistentModel);

			/**
			 * Constructs a new Portfolio tab that contains all information
			 * related to a portfolio. This tab allows you to see the results of
			 * trading activity. It records the summary information for each
			 * month i.e. Batting avg, Simple Sharpe ratio and P/L information.
			 * 
			 */

			portfolioPanel = new PortfolioPanel(this, m_tradePersistentModel);

			/**
			 * Constructs a new Configuration tab that contains all information
			 * related to configuration of Default entry parms, strategies,
			 * indicators, accounts.
			 * 
			 */

			configurationPanel = new ConfigurationPanel(m_tradePersistentModel);

			/**
			 * Constructs a new Strategy tab that contains all information
			 * related to a Strategy. This tab allows you to see the java code
			 * of a strategy. It will be replaced in the future with Drools and
			 * this will be where you can edit the strategies and deploy them.
			 * 
			 */

			strategyPanel = new StrategyPanel(m_tradePersistentModel);

			this.addTab("Tradingday", tradingdayPanel);
			this.addTab("Contract Details", contractPanel);
			this.addTab("Portfolio", portfolioPanel);
			this.addTab("Configuration", configurationPanel);
			this.addTab("Strategies", strategyPanel);
			this.setSelectPanel(tradingdayPanel);
			simulatedMode(true);
		} catch (IOException ex) {
			this.setErrorMessage(
					"Error During Initialization. Please make sure config.properties file is in the root dir.",
					ex.getMessage(), ex);
			System.exit(0);
		} catch (Exception ex1) {
			this.setErrorMessage("Error During Initialization.",
					ex1.getMessage(), ex1);
			System.exit(0);
		}
	}

	/**
	 * This is fired when the menu item to open a file is fired.
	 * 
	 */

	public void doOpen() {
	}

	/**
	 * This is fired from the Tradingday Tab when the Request Executions button
	 * is pressed. This should be used to fetch orders that have executed at the
	 * broker while the system was down.
	 * 
	 * @param tradestrategy
	 *            the Tradestrategy for which you are requesting trade
	 *            executions
	 * 
	 */

	public void doFetch(final Tradestrategy tradestrategy) {
		try {
			if (null != tradestrategy.getIdTradeStrategy()) {
				m_brokerModel.onReqExecutions(tradestrategy);
			}
		} catch (BrokerModelException ex) {
			setErrorMessage("Error getting executions.", ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired from the main menu when the Broker data button is pressed.
	 * This will run the Strategy for all the tradingdays.
	 * 
	 * 
	 */

	public void doData() {
		if (m_tradingdays.isDirty()) {
			this.setStatusBarMessage(
					"Please save before running strategy ...\n",
					BasePanel.WARNING);
		} else {
			runStrategy(m_tradingdays, true);
		}
	}

	/**
	 * This is fired from the Contract/Tradingday Tab when the Broker data
	 * button is pressed. It is also fired doExceutionDetailEnd(). This should
	 * be used to fetch executions for orders that may have been filled while
	 * the system was down.
	 * 
	 * @param tradestrategy
	 *            the Tradestrategy for which you are requesting historical
	 *            data.
	 * 
	 */

	public void doData(final Tradestrategy tradestrategy) {
		if (tradestrategy.isDirty()) {
			this.setStatusBarMessage(
					"Please save or refresh before running strategy ...\n",
					BasePanel.WARNING);
		} else {
			Tradingdays tradingdays = new Tradingdays();
			Tradingday tradingday = Tradingday.newInstance(tradestrategy
					.getTradingday().getOpen());
			tradingday.setIdTradingDay(Integer.MAX_VALUE);
			tradingday.addTradestrategy(tradestrategy);
			tradingdays.add(tradingday);
			runStrategy(tradingdays, true);
		}
	}

	/**
	 * This is fired from the Contract Tab when the Execute Order button is
	 * pressed. This should be used to execute orders to the broker platform.
	 * 
	 * @param instance
	 *            TradeOrder
	 */

	public void doExecute(TradeOrder instance) {

		try {
			this.getFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			TradeOrder tradeOrder = m_tradePersistentModel
					.findTradeOrderByKey(instance.getOrderKey());
			if (null != tradeOrder) {
				if (!tradeOrder.getVersion().equals(instance.getVersion())) {
					this.setStatusBarMessage(
							"Please refresh order before sumbitting change ...\n",
							BasePanel.WARNING);
				}
			}
			Tradestrategy tradestrategy = m_tradePersistentModel
					.findTradestrategyById(instance.getTradestrategy());
			instance = m_brokerModel.onPlaceOrder(tradestrategy.getContract(),
					instance);
			setStatusBarMessage("Order sent to broker.\n",
					BasePanel.INFORMATION);

		} catch (Exception ex) {
			this.setErrorMessage(
					"Error submitting Order " + instance.getOrderKey(),
					ex.getMessage(), ex);
		} finally {
			this.getFrame().setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * This is fired from the main menu when the Run Strategy button is pressed.
	 * This will run the Strategy for all the tradingdays.
	 * 
	 * 
	 */

	public void doRun() {
		try {
			if (m_tradingdays.isDirty()) {
				this.setStatusBarMessage(
						"Please save or refresh before running strategy ...\n",
						BasePanel.WARNING);
			} else {
				runStrategy(m_tradingdays, false);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error running Trade Strategies.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired from the Tradingday Tab when the Run Strategy button is
	 * pressed. This will run the Strategy for all the tradingdays.
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */

	public void doRun(final Tradestrategy tradestrategy) {
		try {
			if (tradestrategy.isDirty()) {
				this.setStatusBarMessage(
						"Please save or refresh before running strategy ...\n",
						BasePanel.WARNING);
			} else {
				Tradingdays tradingdays = new Tradingdays();
				Tradingday tradingday = Tradingday.newInstance(tradestrategy
						.getTradingday().getOpen());
				tradingday.setIdTradingDay(Integer.MAX_VALUE);
				tradingday.addTradestrategy(tradestrategy);
				tradingdays.add(tradingday);
				runStrategy(tradingdays, false);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error running Trade Strategies.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This is fired from the main menu when the Back Test Strategy button is
	 * pressed. This will run the Strategy for all the tradingdays.
	 * 
	 * 
	 */

	public void doTest() {
		if (m_tradingdays.isDirty()) {
			this.setStatusBarMessage(
					"Please save before running strategy ...\n",
					BasePanel.WARNING);
		} else {
			contractPanel.doCloseAll();
			runStrategy(m_tradingdays, false);
		}
	}

	/**
	 * This is fired from the Tradingday Tab when the Back Test Strategy button
	 * is pressed. This will run the Strategy for the selected tradingday.
	 * 
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */

	public void doTest(Tradestrategy tradestrategy) {

		if (tradestrategy.isDirty()) {
			this.setStatusBarMessage(
					"Please save before running strategy ...\n",
					BasePanel.WARNING);
		} else {
			contractPanel.doClose(tradestrategy);
			Tradingdays tradingdays = new Tradingdays();
			Tradingday tradingday = Tradingday.newInstance(tradestrategy
					.getTradingday().getOpen());
			tradingday.setIdTradingDay(Integer.MAX_VALUE);
			tradingday.addTradestrategy(tradestrategy);
			tradingdays.add(tradingday);
			runStrategy(tradingdays, false);
		}
	}

	/**
	 * This method is fired when the system connects to TWS, if there are open
	 * orders. i.e from a BrokerModel event. If todays orders are not in the
	 * openTradeOrders then we cancel then order.
	 * 
	 * @param openTradeOrders
	 *            Hashtable<Integer, TradeOrder> the open orders that are from
	 *            IB TWS.
	 * 
	 * @see 
	 *      org.trade.broker.BrokerChangeListener#openOrderEnd(ConcurrentHashMap<
	 *      Integer,TradeOrder>)
	 */

	public void openOrderEnd(
			ConcurrentHashMap<Integer, TradeOrder> openTradeOrders) {
		try {

			_log.info("Open orders received from TWS: "
					+ openTradeOrders.size());
			Tradingday todayTradingday = m_tradingdays.getTradingday(
					TradingCalendar.getTodayBusinessDayStart(),
					TradingCalendar.getTodayBusinessDayEnd());
			if (null == todayTradingday) {
				return;
			}
			/*
			 * Cancel any orders that were open and not filled.
			 */
			for (Tradestrategy tradestrategy : todayTradingday
					.getTradestrategies()) {
				Tradestrategy instance = m_tradePersistentModel
						.findTradestrategyById(tradestrategy);
				for (TradeOrder todayTradeOrder : instance.getTradeOrders()) {
					if (!todayTradeOrder.getIsFilled()
							&& OrderStatus.CANCELLED.equals(todayTradeOrder
									.getStatus())) {
						if (!openTradeOrders.containsKey(todayTradeOrder
								.getOrderKey())) {
							todayTradeOrder.setStatus(OrderStatus.CANCELLED);
							m_tradePersistentModel
									.persistTradeOrder(todayTradeOrder);
						}
					}
				}
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error reconciling open orders.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired when the Brokermodel has completed the request for
	 * Execution Details see doFetchExecution or connectionOpened i.e from a
	 * BrokerModel event all executions for the filter have now been received.
	 * Check to see if we need to close any trades for these order fills.
	 * 
	 * @param tradeOrders
	 *            Hashtable<Integer, TradeOrder> the executed and open orders
	 *            that are from IB TWS.
	 * 
	 * 
	 * @see org.trade.broker.BrokerChangeListener#executionDetailsEnd(
	 *      ConcurrentHashMap<Integer,TradeOrder>)
	 */
	public void executionDetailsEnd(
			ConcurrentHashMap<Integer, TradeOrder> tradeOrders) {
		try {
			Tradingday todayTradingday = m_tradingdays.getTradingday(
					TradingCalendar.getTodayBusinessDayStart(),
					TradingCalendar.getTodayBusinessDayEnd());
			if (null == todayTradingday) {
				return;
			}
			m_brokerModel.onReqOpenOrders();

		} catch (Exception ex) {
			this.setErrorMessage("Error starting PositionManagerRule.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired when the Brokermodel has completed
	 * executionDetails() or openOrder() and the order that was FILLED. If the
	 * order opens a position and the stop price is set then this is an open
	 * order created via a strategy. Check to see that we have a strategy
	 * manager if so start the manager and close the strategy that opened the
	 * position.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @see org.trade.broker.BrokerChangeListener#tradeOrderFilled(TradeOrder)
	 */
	public void tradeOrderFilled(TradeOrder tradeOrder) {

		try {
			Tradestrategy tradestrategy = m_tradingdays
					.getTradestrategy(tradeOrder.getTradestrategyId()
							.getIdTradeStrategy());

			if (null == tradestrategy) {
				this.setStatusBarMessage(
						"Warning position opened but Tradestrategy not found for Order Key: "
								+ tradeOrder.getOrderKey()
								+ " in the current Tradingday Tab selection.",
						BasePanel.WARNING);
				return;
			}
			if (!tradestrategy.getTrade()) {
				this.setStatusBarMessage(
						"Warning position opened for Symbol: "
								+ tradestrategy.getContract().getSymbol()
								+ "  but this tradestrategy is not set to trade. A manual order was created Key: "
								+ tradeOrder.getOrderKey(), BasePanel.WARNING);
				return;
			}
			if (contractPanel.isSelected()) {
				contractPanel.doRefresh(tradestrategy);
			}

			/*
			 * If the order opens a position and the stop price is set then this
			 * is an open order created via a strategy. Check to see that we
			 * have a strategy manager if so start the manager and close the
			 * strategy that opened the position.
			 */
			if (tradeOrder.getIsOpenPosition()
					&& null != tradeOrder.getStopPrice()) {

				/*
				 * If this Strategy has a manager start the Strategy Manager.
				 */

				if (tradestrategy.getStrategy().hasStrategyManager()) {

					if (!tradingdayPanel.isStrategyWorkerRunning(tradestrategy
							.getStrategy().getStrategyManager().getClassName()
							+ tradestrategy.getIdTradeStrategy())) {
						/*
						 * Kill the worker that got us in if still running its
						 * job is done.
						 */

						tradingdayPanel.killStrategyWorker(tradestrategy
								.getStrategy().getClassName()
								+ tradestrategy.getIdTradeStrategy());

						_log.info("Start PositionManagerStrategy: "
								+ tradestrategy.getContract().getSymbol());
						_log.info("tradeOrderFilled TradePosition Id: "
								+ tradeOrder.getTradePosition()
										.getIdTradePosition() + " Version: "
								+ tradeOrder.getTradePosition().getVersion());
						createStrategy(tradestrategy.getStrategy()
								.getStrategyManager().getClassName(),
								tradestrategy);
					}
				} else {
					String key = tradestrategy.getStrategy().getClassName()
							+ tradestrategy.getIdTradeStrategy();
					StrategyRule strategy = tradingdayPanel
							.getStrategyWorker(key);
					strategy.tradeOrderFilled(tradeOrder);
				}
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error starting PositionManagerRule.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired when the Brokermodel has completed
	 * executionDetails() or openOrder() and the order that was CANCELLED.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @see org.trade.broker.BrokerChangeListener#tradeOrderCancelled(TradeOrder)
	 */
	public void tradeOrderCancelled(TradeOrder tradeOrder) {

		try {

			if (m_brokerModel.isConnected() && contractPanel.isSelected()) {
				Tradestrategy tradestrategy = m_tradingdays
						.getTradestrategy(tradeOrder.getTradestrategyId()
								.getIdTradeStrategy());
				if (null == tradestrategy) {
					this.setStatusBarMessage(
							"Warning position opened but Tradestrategy not found for Order Key: "
									+ tradeOrder.getOrderKey()
									+ " in the current Tradingday Tab selection.",
							BasePanel.WARNING);
					return;
				}

				_log.info("Trade Order cancelled for Symbol: "
						+ tradestrategy.getContract().getSymbol()
						+ " order key: " + tradeOrder.getOrderKey());

				contractPanel.doRefresh(tradestrategy);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error starting PositionManagerRule.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired when the Brokermodel has completed orderStatus().
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @see org.trade.broker.BrokerChangeListener#tradeOrderCancelled(TradeOrder)
	 */
	public void tradeOrderStatusChanged(TradeOrder tradeOrder) {

		try {
			if (m_brokerModel.isConnected() && contractPanel.isSelected()) {
				Tradestrategy tradestrategy = m_tradingdays
						.getTradestrategy(tradeOrder.getTradestrategyId()
								.getIdTradeStrategy());
				if (null == tradestrategy) {
					this.setStatusBarMessage(
							"Warning position opened but Tradestrategy not found for Order Key: "
									+ tradeOrder.getOrderKey()
									+ " in the current Tradingday Tab selection.",
							BasePanel.WARNING);
					return;
				}
				_log.info("Trade Order cancelled for Symbol: "
						+ tradestrategy.getContract().getSymbol()
						+ " order key: " + tradeOrder.getOrderKey());
				contractPanel.doRefresh(tradestrategy);
			}

		} catch (Exception ex) {
			this.setErrorMessage("Error starting PositionManagerRule.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired when the Brokermodel has completed
	 * executionDetails() or openOrder() and the position was closed by the
	 * order.
	 * 
	 * @param tradePosition
	 *            TradePosition
	 * @see org.trade.broker.BrokerChangeListener#positionClosed(TradePosition)
	 */
	public void positionClosed(TradePosition tradePosition) {
		try {
			if (m_brokerModel.isConnected()) {
				tradePosition = m_tradePersistentModel
						.findTradePositionById(tradePosition
								.getIdTradePosition());
				for (TradeOrder tradeOrder : tradePosition.getTradeOrders()) {
					Tradestrategy tradestrategy = m_tradePersistentModel
							.findTradestrategyById(tradeOrder
									.getTradestrategyId().getIdTradeStrategy());
					_log.info("TradePosition closed for Symbol: "
							+ tradePosition.getContract().getSymbol()
							+ " Profit/Loss: "
							+ tradePosition.getTotalNetValue());
					m_tradingdays.getTradestrategy(
							tradestrategy.getIdTradeStrategy()).setStatus(
							tradestrategy.getStatus());
					if (contractPanel.isSelected())
						contractPanel.doRefresh(tradestrategy);
				}
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error position closed : ", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method strategyComplete.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#strategyComplete(Tradestrategy)
	 */
	public void strategyComplete(String strategyClassName,
			Tradestrategy tradestrategy) {

		try {
			if (m_brokerModel.isConnected()) {
				tradestrategy = m_tradePersistentModel
						.findTradestrategyById(tradestrategy
								.getIdTradeStrategy());
				m_tradingdays.getTradestrategy(
						tradestrategy.getIdTradeStrategy()).setStatus(
						tradestrategy.getStatus());
				if (contractPanel.isSelected())
					contractPanel.doRefresh(tradestrategy);
			}
			tradingdayPanel.removeStrategyWorker(strategyClassName
					+ tradestrategy.getIdTradeStrategy());

		} catch (Exception ex) {
			this.setErrorMessage("Error strategyComplete : ", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method strategyStarted.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#strategyStarted(Tradestrategy)
	 */
	public void strategyStarted(Tradestrategy tradestrategy) {

	}

	/**
	 * Method ruleComplete.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#ruleComplete(Tradestrategy)
	 */
	public void ruleComplete(Tradestrategy tradestrategy) {

	}

	/**
	 * Method positionCovered.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see org.trade.strategy.StrategyChangeListener#positionCovered(Tradestrategy)
	 */
	public void positionCovered(Tradestrategy tradestrategy) {
		try {
			if (m_brokerModel.isConnected() && contractPanel.isSelected())
				contractPanel.doRefresh(tradestrategy);
		} catch (Exception ex) {
			this.setErrorMessage("Error positionCovered : ", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method strategyError.
	 * 
	 * @param ex
	 *            StrategyRuleException
	 * @see org.trade.strategy.StrategyChangeListener#strategyError(StrategyRuleException)
	 */
	public void strategyError(StrategyRuleException ex) {
		if (ex.getErrorId() == 1) {
			this.setErrorMessage("Error: " + ex.getErrorCode(),
					ex.getMessage(), ex);
		} else if (ex.getErrorId() == 2) {
			this.setStatusBarMessage("Warning: " + ex.getMessage(),
					BasePanel.WARNING);
		} else if (ex.getErrorId() == 3) {
			this.setStatusBarMessage("Information: " + ex.getMessage(),
					BasePanel.INFORMATION);
		} else {

			this.setErrorMessage("Unknown Error Id Code: " + ex.getErrorCode(),
					ex.getMessage(), ex);
		}
		this.getFrame().setCursor(Cursor.getDefaultCursor());
	}

	public void doHelp() {
		doAbout();
	}

	public void doDisclaimer() {
		try {
			File file = new File("docs/Disclaimer.html");
			JEditorPane disclaimerText;

			disclaimerText = new JEditorPane(file.toURI().toURL());
			disclaimerText.setEditable(false);
			TextDialog disclaimer = new TextDialog(this.getFrame(),
					"Disclaimer", false, disclaimerText);
			disclaimer.pack();
			disclaimer.setSize(new Dimension((int) (this.getFrame().getSize()
					.getWidth() * 2 / 3), (int) (this.getFrame().getSize()
					.getHeight() * 2 / 3)));
			disclaimer.setLocationRelativeTo(this);
			disclaimer.setVisible(true);
		} catch (Exception ex) {
			this.setErrorMessage("Could not load about help.", ex.getMessage(),
					ex);
		}

	}

	/**
	 * This method is fired from the main menu. It displays the application
	 * version.
	 * 
	 */
	public void doAbout() {
		try {
			StringBuffer message = new StringBuffer();
			message.append("Product version: ");
			message.append(ConfigProperties
					.getPropAsString("component.name.version"));
			message.append("\nBuild Label:     ");
			message.append(ConfigProperties
					.getPropAsString("component.name.base"));
			message.append("\nBuild Time:      ");
			message.append(ConfigProperties
					.getPropAsString("component.name.date"));
			JOptionPane.showMessageDialog(this, message, "About Help",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			this.setErrorMessage("Could not load about help.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * This method is fired from the Broker API on completion of broker data
	 * request. Note if this is the current trading day for this trade strategy
	 * real time data has been started by the broker interface. Check to see if
	 * a trade is already open for this trade strategy. If so fire up a trade
	 * manager. If not fire of the strategy.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy that has completed the request for historical
	 *            data
	 * 
	 * 
	 * @see org.trade.broker.BrokerChangeListener#historicalDataComplete(Tradestrategy)
	 */

	public void historicalDataComplete(Tradestrategy tradestrategy) {
		try {
			/*
			 * Now we have the history data complete and the request for real
			 * time data has started, so fire of the strategy for this
			 * tradestrategy.
			 */
			if (!m_brokerModel.isBrokerDataOnly()) {
				if (tradestrategy.getTrade()) {
					if (null != tradestrategy.getTradePosition()
							&& tradestrategy.getTradePosition().getIsOpen()) {
						int result = JOptionPane.showConfirmDialog(this
								.getFrame(), "Position is open for: "
								+ tradestrategy.getContract().getSymbol()
								+ " do you want to run the Strategy ?",
								"Information", JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							if (tradestrategy.getStrategy()
									.hasStrategyManager()) {
								createStrategy(tradestrategy.getStrategy()
										.getStrategyManager().getClassName(),
										tradestrategy);
							} else {
								createStrategy(tradestrategy.getStrategy()
										.getClassName(), tradestrategy);
							}

						} else {
							int result1 = JOptionPane.showConfirmDialog(this
									.getFrame(), "Position is open for: "
									+ tradestrategy.getContract().getSymbol()
									+ " do you want to delete all Orders?",
									"Information", JOptionPane.YES_NO_OPTION);
							if (result1 == JOptionPane.YES_OPTION) {
								m_tradePersistentModel
										.removeTradestrategyTradeOrders(tradestrategy);
							}
						}

					} else {
						createStrategy(tradestrategy.getStrategy()
								.getClassName(), tradestrategy);
					}
				}
			}

		} catch (Exception ex) {
			this.setErrorMessage("Could not start strategy: "
					+ tradestrategy.getStrategy().getName() + " for Symbol: "
					+ tradestrategy.getContract().getSymbol(), ex.getMessage(),
					ex);
		}
	}

	/**
	 * This method connects to the Broker Platform and is fired when the main
	 * menu item connect is pressed..
	 * 
	 */

	public void doConnect() {
		try {

			if ((null != m_brokerModel) && m_brokerModel.isConnected()) {
				int result = JOptionPane.showConfirmDialog(this.getFrame(),
						"Already connected. Do you want to disconnect?",
						"Information", JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					doDisconnect();
				}
			}
			ConnectionPane connectionPane = new ConnectionPane();
			TextDialog dialog = new TextDialog(this.getFrame(),
					"Connect to TWS", true, connectionPane);
			dialog.getCancelButton().setText("Cancel");
			dialog.getOKButton().setText("Connect");
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
			/*
			 * Update the default portfolio.
			 */
			m_tradePersistentModel.resetDefaultPortfolio(connectionPane
					.getPortfolio());
			DBTableLookupServiceProvider.clearLookup();

			if (!dialog.getCancel()) {
				m_brokerModel = (BrokerModel) ClassFactory
						.getServiceForInterface(BrokerModel._broker, this);
				this.getFrame().setCursor(
						Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				this.setStatusBarMessage("Please wait while login proceeds",
						BasePanel.INFORMATION);

				/*
				 * Controller listens for problems from the TWS interface see
				 * doError()
				 */
				m_brokerModel.addMessageListener(this);
				m_brokerModel.onConnect(connectionPane.getHost(),
						connectionPane.getPort(), connectionPane.getClientId());
			} else {
				this.setStatusBarMessage("Running in test.",
						BasePanel.INFORMATION);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Could Not Connect/Disconnect From TWS",
					ex.getMessage(), ex);

		} finally {
			this.getFrame().setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * This method is fired after the tab has been created and placed in the tab
	 * controller.
	 * 
	 */

	public void doWindowOpen() {
		doConnect();
	}

	/**
	 * This method is fired when the tab closes.
	 * 
	 */

	public void doWindowClose() {
		tradingdayPanel.killAllStrategyWorker();
		doDisconnect();
		doExit();
	}

	/**
	 * This method is fired from an event in the Broker Model. All exception
	 * reported back from the broker interface are received here.
	 * 
	 * 0 - 999 are IB TWS error codes for Orders or data 1000 - 1999 are IB TWS
	 * System error 2000 - 2999 are IB TWS Warning 4000 - 4999 are application
	 * warnings 5000 - 5999 are application information
	 * 
	 * @param ex
	 *            BrokerManagerModelException the broker exception
	 * @see org.trade.broker.BrokerChangeListener#brokerError(BrokerModelException)
	 */

	public void brokerError(BrokerModelException ex) {

		if (ex.getErrorId() == 1) {
			this.setErrorMessage("Error: " + ex.getErrorCode(),
					ex.getMessage(), ex);
		} else if (ex.getErrorId() == 2) {
			this.setStatusBarMessage("Warning: " + ex.getMessage(),
					BasePanel.WARNING);
		} else if (ex.getErrorId() == 3) {
			this.setStatusBarMessage("Information: " + ex.getMessage(),
					BasePanel.INFORMATION);
		} else {
			this.setErrorMessage("Unknown Error Id Code: " + ex.getErrorCode(),
					ex.getMessage(), ex);
		}
		this.getFrame().setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * This method is disconnects from the Broker Platform and is fired when the
	 * main menu item disconnect is pressed..
	 * 
	 */

	public void doDisconnect() {
		try {
			tradingdayPanel.killAllStrategyWorker();
			if (m_brokerModel.isConnected()) {
				if ((null != brokerDataRequestProgressMonitor)
						&& !brokerDataRequestProgressMonitor.isDone()) {
					brokerDataRequestProgressMonitor.cancel(true);
				}
				m_brokerModel.onDisconnect();
				_indicatorTradestrategy.clear();
				refreshTradingdays(m_tradingdays);
			} else {
				tradingdayPanel.setConnected(false);
				contractPanel.setConnected(false);
				simulatedMode(true);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Could Not Disconnect From TWS",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired from an event in the Broker Model. A connection has
	 * been opened.
	 * 
	 * @see org.trade.broker.BrokerChangeListener#connectionOpened()
	 */

	public void connectionOpened() {

		try {

			tradingdayPanel.setConnected(true);
			contractPanel.setConnected(true);
			simulatedMode(false);
			Tradingday todayTradingday = m_tradingdays.getTradingday(
					TradingCalendar.getTodayBusinessDayStart(),
					TradingCalendar.getTodayBusinessDayEnd());

			/*
			 * Request all the executions for today. This will result in updates
			 * to any trade orders that were filled while we were disconnected.
			 */
			if (null != todayTradingday) {
				m_brokerModel.onReqAllExecutions(todayTradingday.getOpen());
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error finding excecutions.", ex.getMessage(),
					ex);
		} finally {
			this.setStatusBarMessage("Running live.", BasePanel.INFORMATION);
		}
	}

	/**
	 * This method is fired from an event in the Broker Model. A connection has
	 * been closed.
	 * 
	 * @see org.trade.broker.BrokerChangeListener#connectionClosed()
	 */
	public void connectionClosed(boolean forced) {

		try {

			/*
			 * If the connection was lost to TWS and it was not a doDisconnect()
			 * i.e. it was forced. Try to reconnect.
			 */

			if (forced) {
				if (!m_brokerModel.isConnected()) {
					doConnect();
				}
			} else {
				tradingdayPanel.setConnected(false);
				contractPanel.setConnected(false);
				simulatedMode(true);
				this.setStatusBarMessage("Connected to Broker was closed.",
						BasePanel.WARNING);
			}

		} catch (Exception ex) {
			this.setErrorMessage("Error finding connection closed.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired from an event in the Broker Model. The managed
	 * accounts for this connection. Note each instance of TWS is connected to
	 * one master account only unless you are a Financial Adviser. The list of
	 * accounts is parsed.
	 * 
	 * @param accountNumber
	 *            String csv list of managed accounts.
	 * @see org.trade.broker.BrokerChangeListener#managedAccountsUpdated(String)
	 */

	public void managedAccountsUpdated(String accountNumbers) {
		Scanner scanLine = new Scanner(accountNumbers);
		scanLine.useDelimiter("\\,");

		try {

			int tokens = accountNumbers.replaceAll("[^,]", "").length();

			Portfolio defaultPortfolio = m_tradePersistentModel
					.findPortfolioDefault();

			while (scanLine.hasNext()) {
				String accountNumber = scanLine.next().trim();
				if (accountNumber.length() > 0) {
					Account account = m_tradePersistentModel
							.findAccountByNumber(accountNumber);

					if (null == account) {
						account = new Account(accountNumber, accountNumber,
								Currency.USD);
					}
					/*
					 * If there is only one account in the incoming string and
					 * the default portfolio has no accounts add this account to
					 * the default portfolio.
					 */
					if (defaultPortfolio.getPortfolioAccounts().isEmpty()
							&& tokens == 0) {
						PortfolioAccount portfolioAccount = new PortfolioAccount(
								defaultPortfolio, account);
						defaultPortfolio.getPortfolioAccounts().add(
								portfolioAccount);
						defaultPortfolio = (Portfolio) m_tradePersistentModel
								.persistPortfolio(defaultPortfolio);
						/*
						 * Update the account (key) to the current account only
						 * when the default Portfolio has no accounts.
						 */
						defaultPortfolio.setName(account.getAccountNumber());
						defaultPortfolio = (Portfolio) m_tradePersistentModel
								.persistAspect(defaultPortfolio);

					} else {
						Portfolio portfolio = new Portfolio(
								account.getAccountNumber(),
								account.getAccountNumber());
						PortfolioAccount portfolioAccount = new PortfolioAccount(
								portfolio, account);
						portfolio.getPortfolioAccounts().add(portfolioAccount);
						portfolio = (Portfolio) m_tradePersistentModel
								.persistPortfolio(portfolio);
						if (tokens == 0) {
							/*
							 * Update the default portfolio.
							 */
							m_tradePersistentModel
									.resetDefaultPortfolio(portfolio);
						}
					}
				}
			}

			DBTableLookupServiceProvider.clearLookup();
			tradingdayPanel.doWindowActivated();
			defaultPortfolio = m_tradePersistentModel
					.findPortfolioByName(defaultPortfolio.getName());
			for (PortfolioAccount item : defaultPortfolio
					.getPortfolioAccounts()) {
				m_brokerModel.onSubscribeAccountUpdates(true, item.getAccount()
						.getAccountNumber());
			}

			this.setStatusBarMessage(
					"Connected to IB and subscribed to updates for default portfolio: "
							+ defaultPortfolio.getName(), BasePanel.INFORMATION);
		} catch (Exception ex) {
			this.setErrorMessage("Could not retreive account data Msg: ",
					ex.getMessage(), ex);
		} finally {
			scanLine.close();
		}
	}

	/**
	 * Method updateAccountTime.
	 * 
	 * @param accountNumber
	 *            String
	 * @see org.trade.broker.BrokerChangeListener#updateAccountTime(String)
	 */
	public void updateAccountTime(String accountNumber) {
		try {
			Account account = m_tradePersistentModel
					.findAccountByNumber(accountNumber);
			Portfolio portfolio = account.getDefaultPortfolio();
			if (null != portfolio) {
				portfolio = m_tradePersistentModel.findPortfolioById(portfolio
						.getId());
				tradingdayPanel.setPortfolioLabel(portfolio);
				this.setStatusBarMessage("Account: " + accountNumber
						+ " information updated.", BasePanel.INFORMATION);
			}

		} catch (Exception ex) {
			this.setErrorMessage("Could not retreive account data Msg: ",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method fAAccountsCompleted. The brokerManagerModel has received all FA
	 * Accounts information.
	 * 
	 */
	public void fAAccountsCompleted() {
		DBTableLookupServiceProvider.clearLookup();
	}

	/**
	 * This method retrieves all the details about a contract.
	 * 
	 */

	public void doProperties() {
		try {
			for (Tradingday tradingday : m_tradingdays.getTradingdays()) {
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					m_brokerModel
							.onContractDetails(tradestrategy.getContract());
				}
			}
		} catch (BrokerModelException ex) {
			this.setErrorMessage("Could not disconnect From TWS",
					ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired from the Contract Tab when the Cancel Order button
	 * is pressed. This should be used to cancel orders in the broker platform.
	 * 
	 * @param order
	 *            the TradeOrder that you would like to cancel.
	 * 
	 */

	public void doCancel(TradeOrder order) {

		if (!order.getIsFilled()) {
			try {
				m_brokerModel.onCancelOrder(order);
			} catch (BrokerModelException ex) {
				this.setErrorMessage(
						"Error cancelling Order " + order.getOrderKey(),
						ex.getMessage(), ex);
			}

		} else {
			this.setStatusBarMessage("Order is filled and cannot be cancelled",
					BasePanel.INFORMATION);
		}
	}

	/**
	 * This method is fired from the Cancel Live data button on the main tool
	 * bar. This will cancel all live data and all strategies that are running.
	 * 
	 * 
	 */

	public void doCancel() {

		// Cancel the candleWorker if running
		m_brokerModel.onCancelAllRealtimeData();
		if ((null != brokerDataRequestProgressMonitor)
				&& !brokerDataRequestProgressMonitor.isDone()) {
			brokerDataRequestProgressMonitor.cancel(true);
		}
		tradingdayPanel.killAllStrategyWorker();
		_indicatorTradestrategy.clear();
		refreshTradingdays(m_tradingdays);
		this.setStatusBarMessage(
				"Strategies and live data have been cancelled.",
				BasePanel.INFORMATION);
	}

	/**
	 * This method is fired from the Contract Tab or Trading Tab when the Cancel
	 * Strategy button is pressed. This should be used to cancel strategies in
	 * the broker platform.
	 * 
	 * @param tradestrategy
	 *            the Tradestrategy that you would like to cancel.
	 * 
	 */

	public void doCancel(Tradestrategy tradestrategy) {
		try {
			if (m_brokerModel.isRealtimeBarsRunning(tradestrategy)) {
				m_brokerModel.onCancelRealtimeBars(tradestrategy);
				this.setStatusBarMessage(
						"Realtime data has been cancelled for Symbol: "
								+ tradestrategy.getContract().getSymbol(),
						BasePanel.INFORMATION);
			}
			// Cancel the StrategyWorker if running
			if (tradingdayPanel.isStrategyWorkerRunning(tradestrategy)) {
				tradingdayPanel
						.killAllStrategyWorkersForTradestrategy(tradestrategy);
				this.setStatusBarMessage(
						"Strategy has been cancelled for Symbol: "
								+ tradestrategy.getContract().getSymbol(),
						BasePanel.INFORMATION);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Could not cancel strategy.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * This method is fired from the Main menu and will allow you to setup the
	 * printer setting.
	 */

	public void doPrintSetup() {
		doPrint();
	}

	/**
	 * This method is fired from the Main menu and will allow you to preview a
	 * print of the current tab.
	 */
	public void doPrintPreview() {
		doPrint();
	}

	/**
	 * This method is fired from the Main menu and will allow you to print the
	 * current tab.
	 */
	public void doPrint() {
		try {

			PrinterJob pj = PrinterJob.getPrinterJob();
			PageFormat pageFormat = new PageFormat();
			ComponentPrintService vista = new ComponentPrintService(
					((JFrame) this.getFrame()).getContentPane(), pageFormat);
			vista.scaleToFit(true);

			pj.validatePage(pageFormat);
			pj.setPageable(vista);

			if (pj.printDialog()) {
				pj.print();
			}

		} catch (Exception ex) {
			_log.error("Error printing msg: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Method doTransfer.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 */
	public void doTransfer(Integer idTradestrategy) {
		try {
			Tradestrategy tradestrategy = m_tradingdays
					.getTradestrategy(idTradestrategy);
			if (null == tradestrategy) {
				tradestrategy = m_tradePersistentModel
						.findTradestrategyById(idTradestrategy);
			}
			if (null == m_tradingdays.getTradingday(tradestrategy
					.getTradingday().getOpen(), tradestrategy.getTradingday()
					.getClose())) {
				Tradingday tradingday = m_tradePersistentModel
						.findTradingdayById(tradestrategy.getTradingday()
								.getIdTradingDay());
				m_tradingdays.add(tradingday);
			}
			if (tradestrategy.isDirty()) {
				setStatusBarMessage("Please save ...\n", BasePanel.WARNING);
			} else {
				contractPanel.doTransfer(tradestrategy);
				this.setSelectPanel(contractPanel);
			}
		} catch (PersistentModelException ex) {
			this.setErrorMessage("Error finding Tradingday.", ex.getMessage(),
					ex);
		}
	}

	/**
	 * Method tabChanged.
	 * 
	 * @param currBasePanel
	 *            BasePanel
	 * @param newBasePanel
	 *            BasePanel
	 */
	public void tabChanged(BasePanel currBasePanel, BasePanel newBasePanel) {
		getMenu().setEnabledDelete(false, "Delete all Order");
		getMenu().setEnabledRunStrategy(false);
		getMenu().setEnabledBrokerData(false);
		getMenu().setEnabledTestStrategy(false);
		if (m_brokerModel.isConnected()) {
			getMenu().setEnabledConnect(false);
		} else {
			getMenu().setEnabledConnect(true);
		}
		if (tradingdayPanel == newBasePanel) {
			if (null == brokerDataRequestProgressMonitor
					|| brokerDataRequestProgressMonitor.isDone()) {
				getMenu().setEnabledDelete(true, "Delete all Order");
				if (m_brokerModel.isConnected()) {
					getMenu().setEnabledRunStrategy(true);
				} else {
					getMenu().setEnabledTestStrategy(true);
				}
				getMenu().setEnabledBrokerData(true);
			}
		} else if (strategyPanel == newBasePanel) {
			getMenu().setEnabledDelete(true, "Delete rule");
		}
	}

	/**
	 * Method runStrategy.
	 * 
	 * @param tradingdays
	 *            Tradingdays
	 * @param brokerDataOnly
	 *            boolean
	 */
	private void runStrategy(Tradingdays tradingdays, boolean brokerDataOnly) {
		try {
			m_brokerModel.setBrokerDataOnly(brokerDataOnly);
			if ((null != brokerDataRequestProgressMonitor)
					&& !brokerDataRequestProgressMonitor.isDone()) {
				this.setStatusBarMessage(
						"Strategies already running please wait or cancel ...",
						BasePanel.INFORMATION);
				return;
			} else {
				if (brokerDataOnly && !m_brokerModel.isConnected()) {
					int result = JOptionPane
							.showConfirmDialog(
									this.getFrame(),
									"Yahoo Finance will be used to retrieve candle data."
											+ "\n"
											+ "Do you want to continue ?"
											+ "\n"
											+ "Note there is a 20min delay to data. This option should "
											+ " \n"
											+ "only be used 30mins after market close."
											+ "\n"
											+ "Valid Bar Size/Chart Hist vales are:"
											+ "\n"
											+ "Chart Hist = 1 D, Bar Size >= 1min"
											+ "\n"
											+ "Chart Hist > 1 D to 1 W, Bar Size >= 5min"
											+ "\n"
											+ "Chart Hist > 1 D to 3 M, Bar Size = 1 day",
									"Information", JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.NO_OPTION) {
						return;
					}
				}
				for (Tradingday tradingday : tradingdays.getTradingdays()) {
					if (tradingdayPanel.isStrategyWorkerRunning(tradingday)) {
						this.setStatusBarMessage(
								"Strategies already running please wait or cancel ...",
								BasePanel.INFORMATION);
						return;
					}
					if (Tradingdays.hasTradeOrders(tradingday)
							&& !brokerDataOnly) {
						int result = JOptionPane
								.showConfirmDialog(
										this.getFrame(),
										"Trading strategy cannot be run as Trading day:"
												+ tradingday.getOpen()
												+ " has trades. Do you want to delete trades?",
										"Information",
										JOptionPane.YES_NO_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							m_tradePersistentModel
									.removeTradingdayTradeOrders(tradingday);
						} else {
							if (!m_brokerModel.isConnected()) {
								return;
							}
						}
					}
					for (Tradestrategy tradestrategy : tradingday
							.getTradestrategies()) {
						if (m_brokerModel.isRealtimeBarsRunning(tradestrategy)) {
							int result = JOptionPane.showConfirmDialog(this
									.getFrame(),
									"A real time data request is already running for Symbol: "
											+ tradestrategy.getContract()
													.getSymbol()
											+ " Do you want to cancel?",
									"Information", JOptionPane.YES_NO_OPTION);
							if (result == JOptionPane.YES_OPTION) {
								m_brokerModel
										.onCancelRealtimeBars(tradestrategy);
							} else {
								return;
							}
						}
						if (brokerDataOnly && !m_brokerModel.isConnected()) {
							Date endDate = TradingCalendar
									.getSpecificTime(
											tradestrategy.getTradingday()
													.getClose(),
											TradingCalendar
													.getMostRecentTradingDay(TradingCalendar
															.addBusinessDays(
																	tradestrategy
																			.getTradingday()
																			.getClose(),
																	0)));
							Date startDate = TradingCalendar.addDays(endDate,
									(-1 * (tradestrategy.getChartDays() - 1)));
							startDate = TradingCalendar
									.getMostRecentTradingDay(startDate);

							List<Candle> candles = m_tradePersistentModel
									.findCandlesByContractDateRangeBarSize(
											tradestrategy.getContract()
													.getIdContract(),
											startDate, endDate, tradestrategy
													.getBarSize());
							if (!candles.isEmpty()) {
								int result = JOptionPane.showConfirmDialog(this
										.getFrame(),
										"Candle data already exists for Symbol: "
												+ tradestrategy.getContract()
														.getSymbol()
												+ " Do you want to delete?",
										"Information",
										JOptionPane.YES_NO_OPTION);
								if (result == JOptionPane.YES_OPTION) {
									for (Candle item : candles) {
										m_tradePersistentModel
												.removeAspect(item);
									}
								} else {
									return;
								}
							}
						}
					}
				}
			}

			this.getFrame().setCursor(
					Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			this.setStatusBarMessage("Runing strategy please wait ...",
					BasePanel.INFORMATION);
			if (m_brokerModel.isConnected()) {
				getMenu().setEnabledBrokerData(false);
				getMenu().setEnabledRunStrategy(false);
				getMenu().setEnabledConnect(false);
			} else {
				getMenu().setEnabledTestStrategy(false);
			}
			getMenu().setEnabledSearchDeleteRefreshSave(false);
			tradingdayPanel.cleanStrategyWorker();
			contractPanel.doCloseAll();
			/*
			 * Now run a thread that gets and saves historical data from IB TWS.
			 */
			brokerDataRequestProgressMonitor = new BrokerDataRequestProgressMonitor(
					m_brokerModel, m_tradePersistentModel, tradingdays);
			brokerDataRequestProgressMonitor
					.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent evt) {
							if ("progress".equals(evt.getPropertyName())) {
								int progress = (Integer) evt.getNewValue();
								setProgressBarProgress(progress,
										brokerDataRequestProgressMonitor);
							}
						}
					});
			brokerDataRequestProgressMonitor.execute();

		} catch (Exception ex) {
			this.setErrorMessage("Error running Trade Strategies.",
					ex.getMessage(), ex);
		} finally {
			this.getFrame().setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * Method createStrategy.
	 * 
	 * @param strategyClassName
	 *            String
	 * @param tradestrategy
	 *            Tradestrategy
	 * @throws Exception
	 */
	private synchronized void createStrategy(String strategyClassName,
			Tradestrategy tradestrategy) throws Exception {

		String key = strategyClassName + tradestrategy.getIdTradeStrategy();

		// Only allow one strategy worker per tradestrategy
		if (tradingdayPanel.isStrategyWorkerRunning(key)) {
			throw new StrategyRuleException(1, 100,
					"Strategy already running: "
							+ strategyClassName
							+ " Symbol: "
							+ tradestrategy.getContract().getSymbol()
							+ " Key: "
							+ key
							+ " seriesCount: "
							+ tradestrategy.getDatasetContainer()
									.getBaseCandleSeries().getItemCount());
		}

		Vector<Object> parm = new Vector<Object>(0);
		parm.add(m_brokerModel);
		parm.add(tradestrategy.getDatasetContainer());
		parm.add(tradestrategy.getIdTradeStrategy());

		StrategyRule strategy = (StrategyRule) dynacode.newProxyInstance(
				StrategyRule.class, StrategyRule.PACKAGE + strategyClassName,
				parm);

		strategy.addMessageListener(this);

		if (!m_brokerModel.isConnected()) {
			/*
			 * For back test the back tester listens to the strategy for orders
			 * being created/completed.
			 */
			strategy.addMessageListener(m_brokerModel
					.getBackTestBroker(tradestrategy.getIdTradeStrategy()));
		}
		strategy.execute();
		tradingdayPanel.addStrategyWorker(key, strategy);
		_log.info("Start: "
				+ strategyClassName
				+ " Symbol: "
				+ tradestrategy.getContract().getSymbol()
				+ " seriesCount: "
				+ tradestrategy.getDatasetContainer().getBaseCandleSeries()
						.getItemCount());
	}

	/**
	 * Method simulatedMode.
	 * 
	 * @param simulated
	 *            boolean
	 */
	private void simulatedMode(boolean simulated) {

		try {
			if (simulated) {
				m_brokerModel = (BrokerModel) ClassFactory
						.getServiceForInterface(BrokerModel._brokerTest, this);
				/*
				 * Controller listens for problems from the TWS interface see
				 * doError()
				 */
				m_brokerModel.addMessageListener(this);
				getMenu().setEnabledBrokerData(true);
				getMenu().setEnabledRunStrategy(false);
				getMenu().setEnabledTestStrategy(true);
				getMenu().setEnabledConnect(true);
				this.setStatusBarMessage("Running in simulated mode",
						BasePanel.INFORMATION);
			} else {
				getMenu().setEnabledBrokerData(true);
				getMenu().setEnabledRunStrategy(true);
				getMenu().setEnabledTestStrategy(false);
				getMenu().setEnabledConnect(false);
			}
		} catch (Exception ex) {
			this.setErrorMessage("Error running Simulated Mode.",
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method refreshTradingdays.
	 * 
	 * @param tradingdays
	 *            Tradingdays
	 */
	private void refreshTradingdays(Tradingdays tradingdays) {

		Collections.sort(tradingdays.getTradingdays(),
				Tradingday.DATE_ORDER_DESC);
		for (Tradingday tradingday : tradingdays.getTradingdays()) {
			tradingdayPanel.doRefresh(tradingday);
		}
		if (m_brokerModel.isConnected()) {
			getMenu().setEnabledBrokerData(true);
			getMenu().setEnabledRunStrategy(true);
			getMenu().setEnabledConnect(false);
		} else {
			getMenu().setEnabledTestStrategy(true);
			getMenu().setEnabledConnect(true);
			tradingdayPanel.cleanStrategyWorker();
		}
		getMenu().setEnabledSearchDeleteRefreshSave(true);
	}

	public static TradeMainPanelMenu getMenu() {
		return (TradeMainPanelMenu) TabbedAppPanel.getMenu();
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
			String message = String.format("Completed %d%%.", progress);
			setStatusBarMessage(message, BasePanel.PROGRESS);
		}

		if (worker.isDone() || (progress == 100)) {
			Toolkit.getDefaultToolkit().beep();
			if (worker.isCancelled()) {
				setStatusBarMessage("Process canceled.", BasePanel.INFORMATION);
			} else {
				setStatusBarMessage("Process completed.", BasePanel.INFORMATION);
				getProgressBar().setMaximum(0);
				getProgressBar().setMinimum(0);
			}
		}
	}

	/**
	 */
	private class BrokerDataRequestProgressMonitor extends
			SwingWorker<Void, String> {

		private BrokerModel brokerManagerModel;
		private PersistentModel tradePersistentModel = null;
		private Tradingdays tradingdays = null;
		private int grandTotal = 0;
		private long startTime = 0;
		private long last6SubmittedTime = 0;
		private Integer backTestBarSize = 0;
		private AtomicInteger timerRunning = null;
		private final Object lockCoreUtilsTest = new Object();
		private Timer timer = null;

		/**
		 * Constructor for BrokerDataRequestProgressMonitor.
		 * 
		 * @param brokerManagerModel
		 *            BrokerModel
		 * @param tradingdays
		 *            Tradingdays
		 * @throws IOException
		 */
		public BrokerDataRequestProgressMonitor(BrokerModel brokerManagerModel,
				PersistentModel tradePersistentModel, Tradingdays tradingdays)
				throws IOException {
			this.brokerManagerModel = brokerManagerModel;
			this.tradePersistentModel = tradePersistentModel;
			this.tradingdays = tradingdays;
			backTestBarSize = ConfigProperties
					.getPropAsInt("trade.backtest.barSize");
			timer = new Timer(250, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized (lockCoreUtilsTest) {
						timerRunning.addAndGet(250);
						lockCoreUtilsTest.notifyAll();
					}
				}
			});
		}

		/**
		 * Method doInBackground.
		 * 
		 * @return Void
		 */
		public Void doInBackground() {
			String message = null;
			int totalSumbitted = 0;
			int reSumbittedAt = 20;
			ConcurrentHashMap<Integer, Tradingday> runningContractRequests = new ConcurrentHashMap<Integer, Tradingday>();
			this.grandTotal = 0;

			try {

				for (Tradingday tradingday : this.tradingdays.getTradingdays()) {
					this.grandTotal = this.getGrandTotal()
							+ tradingday.getTradestrategies().size();
				}
				this.startTime = System.currentTimeMillis();
				this.last6SubmittedTime = startTime;
				// Initialize the progress bar
				getProgressBar().setMaximum(100);
				setProgress(0);

				Collections.sort(tradingdays.getTradingdays(),
						Tradingday.DATE_ORDER_ASC);

				for (Tradingday tradingday : tradingdays.getTradingdays()) {

					totalSumbitted = processTradingday(
							getTradingdayToProcess(tradingday,
									runningContractRequests), totalSumbitted);
					/*
					 * Every reSumbittedAt value submitted contracts try to run
					 * any that could not be run due to a conflict. Run then in
					 * asc date order value.
					 */
					if (totalSumbitted > reSumbittedAt) {
						reSumbittedAt = totalSumbitted + reSumbittedAt;
						totalSumbitted = reProcessTradingdays(tradingdays,
								runningContractRequests, totalSumbitted);
					}
				}
				/*
				 * If we are getting data for back testing and the
				 * backTestBarSize is set. Then get the candles for the
				 * tradestrategy tradingday with the new bar size setting. The
				 * backTestBroker will use these candles to build up the candle
				 * on the Tradestrategy/BarSize.
				 */
				if (backTestBarSize > 0
						&& this.brokerManagerModel.isBrokerDataOnly()) {
					Collections.sort(tradingdays.getTradingdays(),
							Tradingday.DATE_ORDER_ASC);
					for (Tradingday itemTradingday : tradingdays
							.getTradingdays()) {
						/*
						 * If its today and its a tradingday it must be after
						 * hours if we are to get backtest data on a lower
						 * timeframe.
						 */
						if (TradingCalendar.isTradingDay(TradingCalendar
								.getDate())
								&& TradingCalendar.sameDay(
										itemTradingday.getOpen(),
										TradingCalendar.getDate())
								&& !TradingCalendar
										.isAfterHours(TradingCalendar.getDate()))
							continue;

						if (itemTradingday.getTradestrategies().isEmpty())
							continue;
						Tradingday tradingday = (Tradingday) itemTradingday
								.clone();
						for (Tradestrategy itemTradestrategy : itemTradingday
								.getTradestrategies()) {
							if (backTestBarSize < itemTradestrategy
									.getBarSize()) {
								Tradestrategy tradestrategy = (Tradestrategy) itemTradestrategy
										.clone();
								tradestrategy.setBarSize(backTestBarSize);
								tradestrategy.setChartDays(1);
								tradestrategy
										.setIdTradeStrategy(this.brokerManagerModel
												.getNextRequestId());
								tradingday.addTradestrategy(tradestrategy);
								this.grandTotal++;
							}
						}
						totalSumbitted = processTradingday(
								getTradingdayToProcess(tradingday,
										runningContractRequests),
								totalSumbitted);
					}
				}

				/*
				 * Every reSumbittedAt value submitted contracts try to run any
				 * that could not be run due to a conflict. Run then in asc date
				 * order value.
				 */

				totalSumbitted = reProcessTradingdays(tradingdays,
						runningContractRequests, totalSumbitted);

			} catch (InterruptedException ex) {
				// Do nothing
				_log.error("doInBackground interupted Msg: ", ex.getMessage());
			} catch (Exception ex) {
				_log.error("Error getting history data Msg: ", ex.getMessage());
				setErrorMessage("Error getting history data.", ex.getMessage(),
						ex);
			} finally {
				synchronized (this.brokerManagerModel.getHistoricalData()) {
					while ((this.brokerManagerModel.getHistoricalData().size() > 0)
							&& !this.isCancelled()) {
						int percent = (int) (((double) (getGrandTotal() - this.brokerManagerModel
								.getHistoricalData().size()) / getGrandTotal()) * 100d);
						setProgress(percent);
						try {
							this.brokerManagerModel.getHistoricalData().wait();
						} catch (InterruptedException ex) {
							// Do nothing
							_log.error(
									"doInBackground finally interupted Msg: ",
									ex.getMessage());
						}
					}
				}
				setProgress(100);
				message = "Completed Historical data total contracts processed: "
						+ totalSumbitted
						+ " in : "
						+ ((System.currentTimeMillis() - this.startTime) / 1000)
						+ " Seconds.";
				_log.info(message);
				publish(message);

			}
			return null;
		}

		/**
		 * Method submitBrokerRequest.
		 * 
		 * @param tradestrategy
		 *            Tradestrategy
		 * @param totalSumbitted
		 *            int
		 * @return int
		 * @throws InterruptedException
		 * @throws BrokerModelException
		 */
		private int submitBrokerRequest(Contract contract, Date endDate,
				Integer barSize, Integer chartDays, int totalSumbitted)
				throws InterruptedException, BrokerModelException {

			if (this.brokerManagerModel.isHistoricalDataRunning(contract)
					|| this.isCancelled()) {
				_log.error("submitBrokerRequest contract already running: "
						+ contract.getSymbol() + " endDate: " + endDate
						+ " barSize: " + barSize);
				return totalSumbitted;
			}
			_log.info("submitBrokerRequest: " + contract.getSymbol()
					+ " endDate: " + endDate);

			totalSumbitted++;
			int percent = (int) (((double) (totalSumbitted - this.brokerManagerModel
					.getHistoricalData().size()) / getGrandTotal()) * 100d);
			setProgress(percent);

			hasSubmittedInSeconds(totalSumbitted);
			this.brokerManagerModel.onBrokerData(contract, endDate, barSize,
					chartDays);

			_log.info("Total: " + getGrandTotal() + " totalSumbitted: "
					+ totalSumbitted);

			/*
			 * Need to slow things down as limit is 60 including real time bars
			 * requests. When connected to TWS. Note only TWSManager return true
			 * for connected.
			 */
			if (((Math.floor(totalSumbitted / 58d) == (totalSumbitted / 58d)) && (totalSumbitted > 0))
					&& this.brokerManagerModel.isConnected()) {

				timerRunning = new AtomicInteger(0);
				timer.start();
				synchronized (lockCoreUtilsTest) {
					while (timerRunning.get() / 1000 < 601
							&& !this.isCancelled()) {
						if ((timerRunning.get() % 60000) == 0) {
							String message = "Please wait "
									+ (10 - (timerRunning.get() / 1000 / 60))
									+ " minutes as there are more than 60 data requests.";
							publish(message);
						}
						lockCoreUtilsTest.wait();
					}
				}
				timer.stop();
			}

			/*
			 * The SwingWorker has a maximum of 10 threads to run and this
			 * process uses one so we have 9 left for the BrokerWorkers. So wait
			 * while the BrokerWorkers threads complete.
			 */
			synchronized (this.brokerManagerModel.getHistoricalData()) {
				while ((this.brokerManagerModel.getHistoricalData().size() > 8)
						&& !this.isCancelled()) {
					this.brokerManagerModel.getHistoricalData().wait();
				}
			}
			return totalSumbitted;
		}

		/**
		 * Method hasSubmittedInSeconds. Make sure no more than six requests
		 * every 2 seconds.
		 * 
		 * 162 - Historical Market Data Service error message: Historical data
		 * request pacing violation
		 * 
		 * The following conditions can cause a pacing violation:
		 * 
		 * Making identical historical data requests within 15 seconds;
		 * 
		 * Making six or more historical data requests for the same Contract,
		 * Exchange and Tick Type within two seconds.
		 * 
		 * Also, observe the following limitation when requesting historical
		 * data:
		 * 
		 * Do not make more than 60 historical data requests in any ten-minute
		 * period.
		 * 
		 * @param totalSumbitted
		 *            int
		 * @throws InterruptedException
		 */
		private void hasSubmittedInSeconds(int totalSumbitted)
				throws InterruptedException {

			if (((Math.floor(totalSumbitted / 6d) == (totalSumbitted / 6d)) && (totalSumbitted > 0))
					&& this.brokerManagerModel.isConnected()) {
				long currentTime = System.currentTimeMillis();
				int waitTime = 5;
				_log.info("hasSubmittedInSeconds 6 in: "
						+ ((currentTime - this.last6SubmittedTime) / 1000d));
				if ((currentTime - this.last6SubmittedTime) < (waitTime * 1000)) {
					timerRunning = new AtomicInteger(0);
					timer.start();
					synchronized (lockCoreUtilsTest) {
						while (timerRunning.get() < (waitTime * 1000)
								&& !this.isCancelled()) {
							_log.info("Please wait "
									+ (waitTime - (timerRunning.get() / 1000))
									+ " seconds.");
							lockCoreUtilsTest.wait();
						}
					}
					timer.stop();
				}
				this.last6SubmittedTime = currentTime;
			}
		}

		/**
		 * Method reProcessTradingdays. Every reSumbittedAt value submitted
		 * contracts try to run any that could not be run due to a conflict. Run
		 * then in asc date order value.
		 * 
		 * @param runningContractRequests
		 *            ConcurrentHashMap<Integer, Tradingday>
		 * @param totalSumbitted
		 *            int
		 * @return int
		 * @throws Exception
		 */

		private int reProcessTradingdays(Tradingdays tradingdays,
				ConcurrentHashMap<Integer, Tradingday> runningContractRequests,
				int totalSumbitted) throws Exception {

			int submitted = totalSumbitted;

			while (!this.isCancelled() && !runningContractRequests.isEmpty()) {

				for (Tradingday item : tradingdays.getTradingdays()) {
					for (Integer idTradeingday : runningContractRequests
							.keySet()) {
						Tradingday reProcessTradingday = runningContractRequests
								.get(idTradeingday);
						if (item.equals(reProcessTradingday)) {
							totalSumbitted = processTradingday(
									getTradingdayToProcess(reProcessTradingday,
											runningContractRequests),
									totalSumbitted);
							break;
						}
					}
				}
				/*
				 * If nothing submitted wait for all the processes to finish.
				 * Usually means we are submitting identical contracts.
				 */

				synchronized (this.brokerManagerModel.getHistoricalData()) {
					if (submitted == totalSumbitted) {
						while (this.brokerManagerModel.getHistoricalData()
								.size() > 0 && !this.isCancelled()) {
							_log.info("reProcessTradingdays Wait HistoricalDataSize: "
									+ this.brokerManagerModel
											.getHistoricalData().size());
							this.brokerManagerModel.getHistoricalData().wait();
						}
					}
				}
				if (submitted < totalSumbitted)
					submitted = totalSumbitted;
			}
			return totalSumbitted;
		}

		/**
		 * Method process.This method process the publish method from
		 * doInBackground().
		 * 
		 * @param messages
		 *            List<String>
		 */
		protected void process(List<String> messages) {
			setStatusBarMessage(messages.get(messages.size() - 1),
					BasePanel.INFORMATION);
		}

		public void done() {
			refreshTradingdays(this.tradingdays);
			String message = "Completed Historical data total contracts processed: "
					+ this.getGrandTotal()
					+ " in : "
					+ ((System.currentTimeMillis() - this.startTime) / 1000)
					+ " Seconds.";
			setStatusBarMessage(message, BasePanel.INFORMATION);
		}

		/**
		 * Method getIndicatorTradestrategy. For any child indicators that are
		 * candle based create a Tradestrategy that will get the data. If this
		 * tradestrategy already exist share this with any other tradestrategy
		 * that requires this.
		 * 
		 * @param tradestrategy
		 *            Tradestrategy
		 * @param series
		 *            CandleSeries
		 * @return Tradestrategy
		 * @throws BrokerModelException
		 * @throws PersistentModelException
		 * @throws CloneNotSupportedException
		 */
		private Tradestrategy getIndicatorTradestrategy(
				Tradestrategy tradestrategy, CandleSeries series)
				throws BrokerModelException, PersistentModelException,
				CloneNotSupportedException {

			Tradestrategy indicatorTradestrategy = null;
			for (Tradestrategy indicator : _indicatorTradestrategy.values()) {
				if (indicator.getContract().equals(series.getContract())
						&& indicator.getTradingday().equals(
								tradestrategy.getTradingday())
						&& indicator.getBarSize().equals(
								tradestrategy.getBarSize())
						&& indicator.getChartDays().equals(
								tradestrategy.getChartDays())
						&& indicator.getPortfolio().equals(
								tradestrategy.getPortfolio())) {
					indicatorTradestrategy = indicator;
					break;
				}
			}
			if (null == indicatorTradestrategy) {
				Contract contract = series.getContract();
				if (null == series.getContract().getIdContract()) {
					contract = this.tradePersistentModel
							.findContractByUniqueKey(series.getContract()
									.getSecType(), series.getContract()
									.getSymbol(), series.getContract()
									.getExchange(), series.getContract()
									.getCurrency(), series.getContract()
									.getExpiry());
					if (null == contract) {
						contract = (Contract) this.tradePersistentModel
								.persistAspect(series.getContract());
					}
				}
				indicatorTradestrategy = new Tradestrategy(contract,
						tradestrategy.getTradingday(),
						new Strategy("Indicator"),
						tradestrategy.getPortfolio(), new BigDecimal(0), null,
						null, false, tradestrategy.getChartDays(),
						tradestrategy.getBarSize());
				indicatorTradestrategy
						.setIdTradeStrategy(this.brokerManagerModel
								.getNextRequestId());
				indicatorTradestrategy.setDirty(false);
			}

			CandleSeries childSeries = indicatorTradestrategy
					.getDatasetContainer().getBaseCandleSeries();
			childSeries.setDisplaySeries(series.getDisplaySeries());
			childSeries.setSeriesRGBColor(series.getSeriesRGBColor());
			childSeries.setSubChart(series.getSubChart());
			childSeries.setSymbol(series.getSymbol());
			childSeries.setSecType(series.getSecType());
			childSeries.setCurrency(series.getCurrency());
			childSeries.setExchange(series.getExchange());

			return indicatorTradestrategy;
		}

		private int processTradingday(Tradingday tradingday, int totalSumbitted)
				throws BrokerModelException, InterruptedException,
				CloneNotSupportedException, PersistentModelException {

			if (tradingday.getTradestrategies().isEmpty())
				return totalSumbitted;
			/*
			 * Remove those that are not running as these do not need to be
			 * shared.
			 */
			for (Tradestrategy tradestrategy : _indicatorTradestrategy.values()) {
				if (!this.brokerManagerModel
						.isRealtimeBarsRunning(tradestrategy)
						&& !this.brokerManagerModel
								.isHistoricalDataRunning(tradestrategy)) {
					_indicatorTradestrategy.remove(tradestrategy
							.getIdTradeStrategy());
				}
			}
			Contract prevContract = null;
			Integer prevBarSize = null;
			Integer prevChartDays = null;
			for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {

				if (null == prevContract) {
					prevContract = tradestrategy.getContract();
					prevBarSize = tradestrategy.getBarSize();
					prevChartDays = tradestrategy.getChartDays();
					_indicatorTradestrategy.put(
							tradestrategy.getIdTradeStrategy(), tradestrategy);
				}
				/*
				 * Refresh the data set container as these may have changed.
				 */
				tradestrategy.setDatasetContainer(null);

				if (!this.brokerManagerModel
						.isRealtimeBarsRunning(tradestrategy)) {

					/*
					 * Fire all the requests to TWS to get chart data After data
					 * has been retrieved save the data Only allow a maximum of
					 * 60 requests in a 10min period to avoid TWS pacing errors
					 */

					if (!prevContract.equals(tradestrategy.getContract())
							|| !prevBarSize.equals(tradestrategy.getBarSize())
							|| !prevChartDays.equals(tradestrategy
									.getChartDays())) {
						totalSumbitted = submitBrokerRequest(prevContract,
								tradingday.getClose(), prevBarSize,
								prevChartDays, totalSumbitted);
						prevContract = tradestrategy.getContract();
						prevBarSize = tradestrategy.getBarSize();
						prevChartDays = tradestrategy.getChartDays();
						_indicatorTradestrategy.put(
								tradestrategy.getIdTradeStrategy(),
								tradestrategy);
					}
				}
			}
			if (null != prevContract) {
				totalSumbitted = submitBrokerRequest(prevContract,
						tradingday.getClose(), prevBarSize, prevChartDays,
						totalSumbitted);
			}
			/*
			 * Now process the indicators that are candle based.
			 */
			for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
				CandleDataset candleDataset = (CandleDataset) tradestrategy
						.getDatasetContainer().getIndicatorByType(
								IndicatorSeries.CandleSeries);

				if (null != candleDataset) {
					for (int seriesIndex = 0; seriesIndex < candleDataset
							.getSeriesCount(); seriesIndex++) {

						CandleSeries series = candleDataset
								.getSeries(seriesIndex);
						Tradestrategy indicatorTradestrategy = getIndicatorTradestrategy(
								tradestrategy, series);
						candleDataset.setSeries(seriesIndex,
								indicatorTradestrategy.getDatasetContainer()
										.getBaseCandleSeries());
						if (!_indicatorTradestrategy
								.containsKey(indicatorTradestrategy
										.getIdTradeStrategy())) {
							if (this.brokerManagerModel.isConnected()
									|| this.brokerManagerModel
											.isBrokerDataOnly()) {
								_indicatorTradestrategy.put(
										indicatorTradestrategy
												.getIdTradeStrategy(),
										indicatorTradestrategy);
								this.grandTotal++;
								indicatorTradestrategy.getContract()
										.addTradestrategy(
												indicatorTradestrategy);
								totalSumbitted = submitBrokerRequest(
										indicatorTradestrategy.getContract(),
										tradingday.getClose(),
										indicatorTradestrategy.getBarSize(),
										indicatorTradestrategy.getChartDays(),
										totalSumbitted);
							}
						}
					}
				}
			}
			return totalSumbitted;
		}

		/**
		 * Method getGrandTotal.
		 * 
		 * @return int
		 */
		private int getGrandTotal() {
			return this.grandTotal;
		}

		/**
		 * Method getTradingdayToProcess. Get a tradingdays worth of strategies
		 * that have contracts with many tradestrategies. If the contract is
		 * already running add it to the set to be reprocessed later.
		 * 
		 * @param tradingday
		 *            Tradingday
		 * 
		 * @param runningContractRequests
		 *            ConcurrentHashMap<Integer, Tradingday>
		 * 
		 * @return Tradingday
		 * @throws CloneNotSupportedException
		 */

		private Tradingday getTradingdayToProcess(Tradingday tradingday,
				ConcurrentHashMap<Integer, Tradingday> runningContractRequests)
				throws CloneNotSupportedException {

			if (tradingday.getTradestrategies().isEmpty())
				return tradingday;

			Collections.sort(tradingday.getTradestrategies(),
					Tradestrategy.TRADINGDAY_CONTRACT);
			Tradingday reProcessTradingday = null;
			if (runningContractRequests.containsKey(tradingday
					.getIdTradingDay())) {
				reProcessTradingday = runningContractRequests.get(tradingday
						.getIdTradingDay());
			} else {
				reProcessTradingday = (Tradingday) tradingday.clone();
			}
			Tradingday toProcessTradingday = (Tradingday) tradingday.clone();
			Contract currContract = null;

			for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
				if (this.brokerManagerModel
						.isHistoricalDataRunning(tradestrategy.getContract())) {
					if (!reProcessTradingday.existTradestrategy(tradestrategy))
						reProcessTradingday.addTradestrategy(tradestrategy);
				} else {

					if (tradestrategy.getContract().equals(currContract)) {
						if (!reProcessTradingday
								.existTradestrategy(tradestrategy))
							reProcessTradingday.addTradestrategy(tradestrategy);
					} else {
						currContract = tradestrategy.getContract();
						currContract.addTradestrategy(tradestrategy);
						toProcessTradingday.addTradestrategy(tradestrategy);
					}
				}
			}

			for (Tradestrategy tradestrategy : toProcessTradingday
					.getTradestrategies()) {
				if (reProcessTradingday.existTradestrategy(tradestrategy))
					reProcessTradingday.removeTradestrategy(tradestrategy);
			}
			if (reProcessTradingday.getTradestrategies().isEmpty()) {
				runningContractRequests.remove(reProcessTradingday
						.getIdTradingDay());
			}
			if (!reProcessTradingday.getTradestrategies().isEmpty()) {
				runningContractRequests.put(
						reProcessTradingday.getIdTradingDay(),
						reProcessTradingday);
			}
			return toProcessTradingday;
		}
	}
}
