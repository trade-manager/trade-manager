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

import java.util.Date;

import junit.framework.TestCase;

import org.jfree.data.DataUtilities;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.ui.TradeAppLoadConfig;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TWSBrokerModelTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(TWSBrokerModelTest.class);

	private BrokerModel m_brokerModel;
	private PersistentModel tradePersistentModel = null;
	private Integer clientId;
	private Integer port = null;
	private String host = null;
	private final static String _broker = BrokerModel._broker;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		try {
			TradeAppLoadConfig.loadAppProperties();
			this.tradePersistentModel = (PersistentModel) ClassFactory
					.getServiceForInterface(PersistentModel._persistentModel,
							this);
			m_brokerModel = (BrokerModel) ClassFactory.getServiceForInterface(
					_broker, this);
			clientId = ConfigProperties.getPropAsInt("trade.tws.clientId");
			port = new Integer(
					ConfigProperties.getPropAsString("trade.tws.port"));
			host = ConfigProperties.getPropAsString("trade.tws.host");
			m_brokerModel.onConnect(host, port, clientId);
			try {
				do {
					Thread.sleep(3000);
				} while (!m_brokerModel.isConnected());
				assertTrue("Connected to TWS", m_brokerModel.isConnected());

			} catch (InterruptedException e) {
				_log.info(" Thread interrupt: " + e.getMessage());
			}

		} catch (Exception e) {
			TestCase.fail("Error on setup " + e.getMessage());
		}
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		m_brokerModel.disconnect();

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
	}

	@Test
	public void testOnBrokerDataOneSymbol() {

		try {
			if (m_brokerModel.isConnected()) {

				String fileName = "trade/test/org/trade/broker/OneSymbol.csv";
				Date tradingDay = new Date();
				tradingDay = TradingCalendar.getPrevTradingDay(tradingDay);

				Tradingday tradingday = new Tradingday(
						TradingCalendar.getBusinessDayStart(tradingDay),
						TradingCalendar.getBusinessDayEnd(tradingDay));
				Tradingdays tradingdays = new Tradingdays();

				tradingdays.populateDataFromFile(fileName, tradingday);

				tradePersistentModel.persistTradingday(tradingday);

				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {

					if (tradestrategy.getContract().getTradestrategies()
							.isEmpty()) {
						tradestrategy.getContract().getTradestrategies()
								.add(tradestrategy);
					}

					m_brokerModel.onBrokerData(tradestrategy.getContract(),
							tradestrategy.getTradingday().getClose(),
							tradestrategy.getBarSize(),
							tradestrategy.getChartDays());
				}

			}
		} catch (Exception e) {
			TestCase.fail("Error testOnBrokerData Msg: " + e.getMessage());
		} finally {

		}
	}
}
