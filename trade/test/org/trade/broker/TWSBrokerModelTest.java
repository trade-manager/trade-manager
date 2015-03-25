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
package org.trade.broker;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

import org.jfree.data.DataUtilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradePosition;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.ui.TradeAppLoadConfig;
import org.trade.ui.base.BasePanel;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TWSBrokerModelTest implements BrokerChangeListener {

	private final static Logger _log = LoggerFactory
			.getLogger(TWSBrokerModelTest.class);

	private Tradingdays tradingdays = null;
	private BrokerModel brokerManagerModel;
	private PersistentModel tradePersistentModel = null;
	private static Integer clientId;
	private static Integer port = null;
	private static String host = null;
	private static int testCaseGrandTotal = 0;
	private static Timer timer = null;
	private boolean connectionFailed = false;
	private static AtomicInteger timerRunning = null;
	private final static Object lockCoreUtilsTest = new Object();
	private final static String _broker = BrokerModel._broker;

	private BrokerDataRequestMonitor brokerDataRequestProgressMonitor = null;

	/**
	 * Method setUpBeforeClass.
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			TradeAppLoadConfig.loadAppProperties();

			clientId = ConfigProperties.getPropAsInt("trade.tws.clientId");
			port = new Integer(
					ConfigProperties.getPropAsString("trade.tws.port"));
			host = ConfigProperties.getPropAsString("trade.tws.host");

			timer = new Timer(250, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchronized (lockCoreUtilsTest) {
						timerRunning.addAndGet(250);
						lockCoreUtilsTest.notifyAll();
					}
				}
			});

		} catch (Exception e) {
			fail("Error on setup " + e.getMessage());
		}
	}

	/**
	 * Method setUp. Try to connect to the Broker for these tests that but
	 * candle data from the broker and test the throtle monitor.
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
			tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			brokerManagerModel = (BrokerModel) ClassFactory
					.getServiceForInterface(_broker, this);
			brokerManagerModel.addMessageListener(this);
			brokerManagerModel.onConnect(host, port, clientId);
			timerRunning = new AtomicInteger(0);
			timer.start();
			synchronized (lockCoreUtilsTest) {
				while (!brokerManagerModel.isConnected() && !connectionFailed) {
					lockCoreUtilsTest.wait();
				}
			}
			timer.stop();
			if (!brokerManagerModel.isConnected())
				_log.warn("Could not connect to TWS test will be ignored.",
						brokerManagerModel.isConnected());

		} catch (InterruptedException e) {
			_log.info("Thread interrupt: " + e.getMessage());
		}
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		deleteData();
		if (brokerManagerModel.isConnected())
			brokerManagerModel.onDisconnect();

		/*
		 * Wait 10min between each test run to avoid pacing violations.
		 */
		if (((Math.floor(testCaseGrandTotal / 58d) == (testCaseGrandTotal / 58d)) && (testCaseGrandTotal > 0))
				&& brokerManagerModel.isConnected()) {
			timerRunning = new AtomicInteger(0);
			timer.start();
			synchronized (lockCoreUtilsTest) {
				while (timerRunning.get() / 1000 < 601) {
					if ((timerRunning.get() % 60000) == 0) {
						String message = "Please wait "
								+ (10 - (timerRunning.get() / 1000 / 60))
								+ " minutes as there are more than 60 data requests.";
						_log.warn(message);
					}
					lockCoreUtilsTest.wait();
				}
			}
			timer.stop();
		}
	}

	/**
	 * Method tearDownAfterClass.
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testOneSymbolTodayOnBrokerData() {
		tradingdays = new Tradingdays();
		try {
			if (brokerManagerModel.isConnected()) {

				String fileName = "trade/test/org/trade/broker/OneSymbolToday.csv";
				Date tradingDay = new Date();
				tradingDay = TradingCalendar.getPrevTradingDay(tradingDay);

				Tradingday tradingday = new Tradingday(
						TradingCalendar.getBusinessDayStart(tradingDay),
						TradingCalendar.getBusinessDayEnd(tradingDay));

				tradingdays.populateDataFromFile(fileName, tradingday);

				for (Tradingday item : tradingdays.getTradingdays()) {
					tradePersistentModel.persistTradingday(item);
				}
				brokerDataRequestProgressMonitor = new BrokerDataRequestMonitor(
						brokerManagerModel, tradePersistentModel, tradingdays);
				brokerDataRequestProgressMonitor
						.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if ("progress".equals(evt.getPropertyName())) {
									int progress = (Integer) evt.getNewValue();
									String message = String.format(
											"Completed %d%%.", progress);
									_log.warn(message);
								} else if ("information".equals(evt
										.getPropertyName())) {
									_log.warn("Information message: "
											+ (String) evt.getNewValue());
									if (brokerDataRequestProgressMonitor
											.isDone()) {
										String message = String.format(
												"Completed %d%%.", 100);
										_log.warn(message);
									}

								} else if ("error".equals(evt.getPropertyName())) {
									_log.error("Error getting history data."
											+ ((Exception) evt.getNewValue())
													.getMessage());
								}
							}
						});
				brokerDataRequestProgressMonitor.execute();
				synchronized (lockCoreUtilsTest) {
					while (brokerManagerModel.isConnected()
							&& !connectionFailed
							&& !brokerDataRequestProgressMonitor.isDone()) {
						lockCoreUtilsTest.wait(1000);
					}
				}
			}
		} catch (Exception ex) {
			fail("Error testOneSymbolOnBrokerData Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testOneMonthContractsOnBrokerData() {
		tradingdays = new Tradingdays();
		try {
			if (brokerManagerModel.isConnected()) {

				String fileName = "trade/test/org/trade/broker/OneMonthContracts.csv";
				Date tradingDay = new Date();
				tradingDay = TradingCalendar.getPrevTradingDay(tradingDay);

				Tradingday tradingday = new Tradingday(
						TradingCalendar.getBusinessDayStart(tradingDay),
						TradingCalendar.getBusinessDayEnd(tradingDay));

				tradingdays.populateDataFromFile(fileName, tradingday);

				for (Tradingday item : tradingdays.getTradingdays()) {
					tradePersistentModel.persistTradingday(item);
				}
				brokerDataRequestProgressMonitor = new BrokerDataRequestMonitor(
						brokerManagerModel, tradePersistentModel, tradingdays);
				brokerDataRequestProgressMonitor
						.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if ("progress".equals(evt.getPropertyName())) {
									int progress = (Integer) evt.getNewValue();
									String message = String.format(
											"Completed %d%%.", progress);
									_log.warn(message);
								} else if ("information".equals(evt
										.getPropertyName())) {
									_log.warn("Information message: "
											+ (String) evt.getNewValue());
									if (brokerDataRequestProgressMonitor
											.isDone()) {
										String message = String.format(
												"Completed %d%%.", 100);
										_log.warn(message);
									}

								} else if ("error".equals(evt.getPropertyName())) {
									_log.error("Error getting history data."
											+ ((Exception) evt.getNewValue())
													.getMessage());
								}
							}
						});
				brokerDataRequestProgressMonitor.execute();
				synchronized (lockCoreUtilsTest) {
					while (brokerManagerModel.isConnected()
							&& !connectionFailed
							&& !brokerDataRequestProgressMonitor.isDone()) {
						lockCoreUtilsTest.wait(1000);
					}
				}
			}
		} catch (Exception ex) {
			fail("Error testMarch2013OnBrokerData Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testOneSymbolTwoMthsOnBrokerData() {
		tradingdays = new Tradingdays();
		try {
			if (brokerManagerModel.isConnected()) {

				String fileName = "trade/test/org/trade/broker/OneSymbolTwoMths.csv";
				Date tradingDay = new Date();
				tradingDay = TradingCalendar.getPrevTradingDay(tradingDay);

				Tradingday tradingday = new Tradingday(
						TradingCalendar.getBusinessDayStart(tradingDay),
						TradingCalendar.getBusinessDayEnd(tradingDay));

				tradingdays.populateDataFromFile(fileName, tradingday);
				/*
				 * Set the chart days to one day so no over lap.
				 */
				for (Tradingday item : tradingdays.getTradingdays()) {
					for (Tradestrategy tradestrategy : item
							.getTradestrategies()) {
						tradestrategy.setChartDays(1);
					}
				}

				for (Tradingday item : tradingdays.getTradingdays()) {
					tradePersistentModel.persistTradingday(item);
				}

				brokerDataRequestProgressMonitor = new BrokerDataRequestMonitor(
						brokerManagerModel, tradePersistentModel, tradingdays);
				brokerDataRequestProgressMonitor
						.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if ("progress".equals(evt.getPropertyName())) {
									int progress = (Integer) evt.getNewValue();
									String message = String.format(
											"Completed %d%%.", progress);
									_log.warn(message);
								} else if ("information".equals(evt
										.getPropertyName())) {
									_log.warn("Information message: "
											+ (String) evt.getNewValue());
									if (brokerDataRequestProgressMonitor
											.isDone()) {
										String message = String.format(
												"Completed %d%%.", 100);
										_log.warn(message);
									}

								} else if ("error".equals(evt.getPropertyName())) {
									_log.error("Error getting history data."
											+ ((Exception) evt.getNewValue())
													.getMessage());
								}
							}
						});
				brokerDataRequestProgressMonitor.execute();
				synchronized (lockCoreUtilsTest) {
					while (brokerManagerModel.isConnected()
							&& !connectionFailed
							&& !brokerDataRequestProgressMonitor.isDone()) {
						lockCoreUtilsTest.wait(1000);
					}
				}
			}
		} catch (Exception ex) {
			fail("Error testOneSymbolMarch2013OnBrokerData Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testMultiContractsMultiDaysOnBrokerData() {
		tradingdays = new Tradingdays();
		try {
			if (brokerManagerModel.isConnected()) {

				String fileName = "trade/test/org/trade/broker/MultiContractsMultiDays.csv";
				Date tradingDay = new Date();
				tradingDay = TradingCalendar.getPrevTradingDay(tradingDay);

				Tradingday tradingday = new Tradingday(
						TradingCalendar.getBusinessDayStart(tradingDay),
						TradingCalendar.getBusinessDayEnd(tradingDay));

				tradingdays.populateDataFromFile(fileName, tradingday);
				/*
				 * Set the chart days to one day so no over lap.
				 */
				for (Tradingday item : tradingdays.getTradingdays()) {
					for (Tradestrategy tradestrategy : item
							.getTradestrategies()) {
						tradestrategy.setChartDays(1);
					}
				}

				for (Tradingday item : tradingdays.getTradingdays()) {
					tradePersistentModel.persistTradingday(item);
				}

				brokerDataRequestProgressMonitor = new BrokerDataRequestMonitor(
						brokerManagerModel, tradePersistentModel, tradingdays);
				brokerDataRequestProgressMonitor
						.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent evt) {
								if ("progress".equals(evt.getPropertyName())) {
									int progress = (Integer) evt.getNewValue();
									String message = String.format(
											"Completed %d%%.", progress);
									_log.warn(message);
								} else if ("information".equals(evt
										.getPropertyName())) {
									_log.warn("Information message: "
											+ (String) evt.getNewValue());
									if (brokerDataRequestProgressMonitor
											.isDone()) {
										String message = String.format(
												"Completed %d%%.", 100);
										_log.warn(message);
									}

								} else if ("error".equals(evt.getPropertyName())) {
									_log.error("Error getting history data."
											+ ((Exception) evt.getNewValue())
													.getMessage());
								}
							}
						});
				brokerDataRequestProgressMonitor.execute();
				synchronized (lockCoreUtilsTest) {
					while (brokerManagerModel.isConnected()
							&& !connectionFailed
							&& !brokerDataRequestProgressMonitor.isDone()) {
						lockCoreUtilsTest.wait(1000);
					}
				}
			}
		} catch (Exception ex) {
			fail("Error testOneSymbolMarch2013OnBrokerData Msg: "
					+ ex.getMessage());
		}
	}

	/**
	 * Method deleteData. Clean the test data added.
	 * 
	 * 
	 */
	private void deleteData() {

		try {

			Aspects candles = tradePersistentModel
					.findAspectsByClassName(Candle.class.getName());
			for (Aspect item : candles.getAspect()) {
				tradePersistentModel.removeAspect(item);
			}

			Aspects tradestrategies = tradePersistentModel
					.findAspectsByClassName(Tradestrategy.class.getName());
			for (Aspect item : tradestrategies.getAspect()) {
				tradePersistentModel.removeAspect(item);
			}

			Aspects contracts = tradePersistentModel
					.findAspectsByClassName(Contract.class.getName());
			for (Aspect item : contracts.getAspect()) {
				tradePersistentModel.removeAspect(item);
			}

			Aspects tradingdays = tradePersistentModel
					.findAspectsByClassName(Tradingday.class.getName());
			for (Aspect item : tradingdays.getAspect()) {
				tradePersistentModel.removeAspect(item);
			}
		} catch (Exception e) {
			fail("Error deleteData Msg: " + e.getMessage());
		} finally {
			_log.info("All data deleted");
		}
	}

	public void connectionOpened() {
		_log.info("Connection opened");
	}

	public void connectionClosed(boolean forced) {
		connectionFailed = true;
		_log.info("Connection closed");
	}

	/**
	 * Method executionDetailsEnd.
	 * 
	 * @param execDetails
	 *            ConcurrentHashMap<Integer,TradeOrder>
	 */
	public void executionDetailsEnd(
			ConcurrentHashMap<Integer, TradeOrder> execDetails) {

	}

	/**
	 * Method historicalDataComplete.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 */
	public void historicalDataComplete(Tradestrategy tradestrategy) {

		try {
			_log.info("Symbol: "
					+ tradestrategy.getContract().getSymbol()
					+ " Candles  saved: "
					+ tradePersistentModel.findCandleCount(tradestrategy
							.getTradingday().getIdTradingDay(), tradestrategy
							.getContract().getIdContract()));
		} catch (PersistentModelException ex) {
			_log.error("Error historicalDataComplete Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method managedAccountsUpdated.
	 * 
	 * @param accountNumber
	 *            String
	 */
	public void managedAccountsUpdated(String accountNumber) {

	}

	/**
	 * Method fAAccountsCompleted. Notifies all registered listeners that the
	 * brokerManagerModel has received all FA Accounts information.
	 * 
	 */
	public void fAAccountsCompleted() {

	}

	/**
	 * Method updateAccountTime.
	 * 
	 * @param accountNumber
	 *            String
	 */
	public void updateAccountTime(String accountNumber) {

	}

	/**
	 * Method brokerError.
	 * 
	 * @param brokerError
	 *            BrokerModelException
	 */
	public void brokerError(BrokerModelException ex) {
		if (502 == ex.getErrorCode()) {
			_log.info("TWS is not running test will not be run");
			return;
		}
		if (ex.getErrorId() == 1) {
			_log.error("Error: " + ex.getErrorCode(), ex.getMessage(), ex);
		} else if (ex.getErrorId() == 2) {
			_log.warn("Warning: " + ex.getMessage(), BasePanel.WARNING);
		} else if (ex.getErrorId() == 3) {
			_log.info("Information: " + ex.getMessage(), BasePanel.INFORMATION);
		} else {
			_log.error("Unknown Error Id Code: " + ex.getErrorCode(),
					ex.getMessage(), ex);
		}
	}

	/**
	 * Method tradeOrderFilled.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderFilled(TradeOrder tradeOrder) {

	}

	/**
	 * Method tradeOrderCancelled.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderCancelled(TradeOrder tradeOrder) {

	}

	/**
	 * Method tradeOrderStatusChanged.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 */
	public void tradeOrderStatusChanged(TradeOrder tradeOrder) {

	}

	/**
	 * Method positionClosed.
	 * 
	 * @param trade
	 *            Trade
	 */
	public void positionClosed(TradePosition trade) {

	}

	/**
	 * Method openOrderEnd.
	 * 
	 * @param openOrders
	 *            ConcurrentHashMap<Integer,TradeOrder>
	 */
	public void openOrderEnd(ConcurrentHashMap<Integer, TradeOrder> openOrders) {

	}
}
