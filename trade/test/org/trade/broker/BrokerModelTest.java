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

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.TestCase;

import org.jfree.data.DataUtilities;
import org.jfree.data.time.RegularTimePeriod;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.factory.ClassFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.OrderType;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.movingaverage.MovingAverageItem;
import org.trade.strategy.data.vwap.VwapItem;
import org.trade.ui.TradeAppLoadConfig;

/**
 * Some tests for the {@link DataUtilities} class.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class BrokerModelTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(BrokerModelTest.class);

	private String symbol = "TEST";
	private BrokerModel m_brokerModel;
	private Integer clientId;
	private BigDecimal price = new BigDecimal(108.85);
	private Tradestrategy tradestrategy = null;
	private Integer port = null;
	private String host = null;
	private final static String _broker = BrokerModel._brokerTest;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		try {
			TradeAppLoadConfig.loadAppProperties();
			m_brokerModel = (BrokerModel) ClassFactory.getServiceForInterface(
					_broker, this);
			clientId = ConfigProperties.getPropAsInt("trade.tws.clientId");
			port = new Integer(
					ConfigProperties.getPropAsString("trade.tws.port"));
			ConfigProperties.getPropAsString("trade.tws.host");
			m_brokerModel.onConnect(host, port, clientId);
			this.tradestrategy = TradestrategyTest.getTestTradestrategy(symbol);
			TestCase.assertNotNull(this.tradestrategy);

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
		m_brokerModel.onDisconnect();
		TradestrategyTest.clearDBData();
	}

	@Test
	public void testSubmitBuyOrder() {

		try {

			TradeOrder tradeOrder = new TradeOrder(this.tradestrategy,
					Action.BUY, OrderType.STPLMT, 100, price,
					price.add(new BigDecimal(0.02)), new Date());
			tradeOrder.setClientId(clientId);
			tradeOrder.setTransmit(new Boolean(true));
			tradeOrder.setStatus(OrderStatus.UNSUBMIT);

			tradeOrder = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder);

			_log.info("IdTradeOrder: " + tradeOrder.getIdTradeOrder()
					+ " OrderKey: " + tradeOrder.getOrderKey());
			TestCase.assertNotNull(tradeOrder);

		} catch (Exception e) {
			TestCase.fail("Error testSubmitBuyOrder Msg: " + e.getMessage());
		}
	}

	@Test
	public void testSubmitSellShortOrder() {

		try {
			_log.info("Symbol: " + this.tradestrategy.getContract().getSymbol());

			TradeOrder tradeOrder = new TradeOrder(this.tradestrategy,
					Action.SELL, OrderType.STPLMT, 100,
					price.subtract(new BigDecimal(0.70)),
					price.subtract(new BigDecimal(0.73)), new Date());

			tradeOrder.setClientId(clientId);
			tradeOrder.setTransmit(new Boolean(true));
			tradeOrder.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder);

			_log.info("IdTradeOrder: " + tradeOrder.getIdTradeOrder()
					+ " OrderKey: " + tradeOrder.getOrderKey());
			TestCase.assertNotNull(tradeOrder.getIdTradeOrder());

		} catch (Exception e) {
			TestCase.fail("Error testSubmitSellShortOrder Msg: "
					+ e.getMessage());
		}
	}

	@Test
	public void testSubmitComboOrder() {

		try {
			String ocaID = new String(Integer.toString((new BigDecimal(Math
					.random() * 1000000)).intValue()));

			TradeOrder tradeOrder = new TradeOrder(this.tradestrategy,
					Action.SELL, OrderType.LMT, 50, null,
					price.add(new BigDecimal(1.0)), new Date());

			tradeOrder.setClientId(clientId);
			tradeOrder.setOcaType(2);
			tradeOrder.setOcaGroupName(ocaID);
			tradeOrder.setTransmit(true);
			tradeOrder.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder);
			TestCase.assertNotNull(tradeOrder.getIdTradeOrder());

			TradeOrder tradeOrder1 = new TradeOrder(this.tradestrategy,
					Action.SELL, OrderType.LMT, 50,
					price.subtract(new BigDecimal(1.0)),
					price.add(new BigDecimal(2.0)), new Date());

			tradeOrder1.setClientId(clientId);
			tradeOrder1.setOcaType(2);
			tradeOrder1.setOcaGroupName(ocaID);
			tradeOrder1.setTransmit(false);
			tradeOrder1.setStatus(OrderStatus.UNSUBMIT);

			tradeOrder1 = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder1);
			TestCase.assertNotNull(tradeOrder1.getIdTradeOrder());

			TradeOrder tradeOrder2 = new TradeOrder(this.tradestrategy,
					Action.SELL, OrderType.STP, 50,
					price.subtract(new BigDecimal(1.0)), null, new Date());
			ocaID = ocaID + "abc";
			tradeOrder2.setClientId(clientId);
			tradeOrder2.setOcaType(2);
			tradeOrder2.setOcaGroupName(ocaID);
			tradeOrder2.setTransmit(true);
			tradeOrder2.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder2 = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder2);
			TestCase.assertNotNull(tradeOrder2.getIdTradeOrder());

			TradeOrder tradeOrder3 = new TradeOrder(this.tradestrategy,
					Action.SELL, OrderType.STP, 50,
					price.subtract(new BigDecimal(2.0)), null, new Date());
			tradeOrder3.setClientId(clientId);
			tradeOrder3.setOcaType(2);
			tradeOrder3.setOcaGroupName(ocaID);
			tradeOrder3.setTransmit(false);
			tradeOrder3.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder3 = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder3);
			TestCase.assertNotNull(tradeOrder3.getIdTradeOrder());
			_log.info("IdTradeOrder: " + tradeOrder3.getIdTradeOrder()
					+ " OrderKey2: " + tradeOrder2.getOrderKey()
					+ " OrderKey2 Price: " + tradeOrder2.getLimitPrice()
					+ " OrderKey3: " + tradeOrder3.getOrderKey()
					+ " OrderKey3 Price: " + tradeOrder3.getAuxPrice());
			// Update the Stop price
			tradeOrder2.setAuxPrice(price.subtract(new BigDecimal(0.9)));
			tradeOrder2.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder2 = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder2);

			tradeOrder3.setAuxPrice(price.subtract(new BigDecimal(0.9)));
			tradeOrder3.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder3 = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder3);
			_log.info("IdTradeOrder: " + tradeOrder3.getIdTradeOrder()
					+ " OrderKey2: " + tradeOrder2.getOrderKey()
					+ " OrderKey2 Price: " + tradeOrder2.getLimitPrice()
					+ " OrderKey3: " + tradeOrder3.getOrderKey()
					+ " OrderKey3 Price: " + tradeOrder3.getAuxPrice());

			tradeOrder3.setTransmit(new Boolean(true));
			tradeOrder3.setStatus(OrderStatus.UNSUBMIT);
			tradeOrder3 = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder3);

			_log.info("IdTradeOrder: " + tradeOrder2.getIdTradeOrder()
					+ " OrderKey: " + tradeOrder3.getOrderKey());
			TestCase.assertNotNull(tradeOrder3);

		} catch (Exception e) {
			TestCase.fail("Error testSubmitComboOrder Msg: " + e.getMessage());
		}
	}

	@Test
	public void testOnBrokerData() {

		try {

			StrategyData.doDummyData(this.tradestrategy.getStrategyData()
					.getCandleDataset().getSeries(0),
					tradestrategy.getTradingday(),
					tradestrategy.getChartDays(), tradestrategy.getBarSize(),
					true, 0);
			m_brokerModel.onBrokerData(tradestrategy, tradestrategy
					.getTradingday().getClose());

			TestCase.assertFalse(this.tradestrategy.getStrategyData()
					.getCandleDataset().getSeries(0).isEmpty());

			IndicatorSeries candleseries = this.tradestrategy.getStrategyData()
					.getCandleDataset().getSeries(0);
			IndicatorSeries sma1Series = this.tradestrategy.getStrategyData()
					.getIndicatorByType(IndicatorSeries.MovingAverageSeries)
					.getSeries(0);
			IndicatorSeries sma2Series = this.tradestrategy.getStrategyData()
					.getIndicatorByType(IndicatorSeries.MovingAverageSeries)
					.getSeries(1);
			IndicatorSeries vwapSeries = this.tradestrategy.getStrategyData()
					.getIndicatorByType(IndicatorSeries.VwapSeries)
					.getSeries(0);
			IndicatorSeries heikinAshiSeries = this.tradestrategy
					.getStrategyData()
					.getIndicatorByType(IndicatorSeries.HeikinAshiSeries)
					.getSeries(0);

			for (int i = 0; i < candleseries.getItemCount(); i++) {
				CandleItem candle = (CandleItem) candleseries.getDataItem(i);
				RegularTimePeriod period = candle.getPeriod();

				CandleItem heikinAshiCandle = null;
				VwapItem vwap = null;
				MovingAverageItem sma1 = null;
				MovingAverageItem sma2 = null;

				int b = heikinAshiSeries.indexOf(period);
				if (b > -1) {
					heikinAshiCandle = (CandleItem) heikinAshiSeries
							.getDataItem(b);
				}

				int c = vwapSeries.indexOf(new Long(period
						.getMiddleMillisecond()));
				if (c > -1) {
					vwap = (VwapItem) vwapSeries.getDataItem(c);
				}
				int d = sma1Series.indexOf(new Long(period
						.getMiddleMillisecond()));
				if (d > -1) {
					sma1 = (MovingAverageItem) sma1Series.getDataItem(d);
				}
				int e = sma2Series.indexOf(new Long(period
						.getMiddleMillisecond()));
				if (e > -1) {
					sma2 = (MovingAverageItem) sma2Series.getDataItem(e);
				}
				if (null != candle) {
					_log.info("    Period Start: " + period.getStart()
							+ " Period End: " + period.getEnd() + " H: "
							+ new Money(candle.getHigh()) + " L: "
							+ new Money(candle.getLow()) + " O: "
							+ new Money(candle.getOpen()) + " C: "
							+ new Money(candle.getClose()) + " Vol: "
							+ new Money(candle.getVolume()) + " Vwap: "
							+ new Money(candle.getVwap()));
				}
				if (null != heikinAshiCandle) {
					_log.info("HA  Period Start: " + period.getStart()
							+ " Period End: " + period.getEnd() + " HA H: "
							+ new Money(heikinAshiCandle.getHigh()) + " HA L: "
							+ new Money(heikinAshiCandle.getLow()) + " HA O: "
							+ new Money(heikinAshiCandle.getOpen()) + " HA C: "
							+ new Money(heikinAshiCandle.getClose())
							+ " HA Vol: "
							+ new Money(heikinAshiCandle.getVolume())
							+ " HA Vwap: "
							+ new Money(heikinAshiCandle.getVwap()));
				}
				if (null != vwap) {
					_log.info("Vwp Period Start: " + period + " Vwap: "
							+ new Money(vwap.getY()));
				}
				if (null != sma1) {
					_log.info("S8  Period Start: " + period + " Sma 8: "
							+ new Money(sma1.getY()));
				}
				if (null != sma2) {
					_log.info("S20 Period Start: " + period + " Sma 20: "
							+ new Money(sma2.getY()));
				}
			}

		} catch (Exception e) {
			TestCase.fail("Error testOnBrokerData Msg: " + e.getMessage());
		}
	}

	@Test
	public void testOnConnect() {
		try {
			m_brokerModel.onConnect(host, port, clientId);
			if (_broker.equals(BrokerModel._brokerTest)) {
				TestCase.assertFalse(m_brokerModel.isConnected());
			} else {
				TestCase.assertTrue(m_brokerModel.isConnected());
			}

		} catch (Exception ex) {
			TestCase.fail("Error testOnConnect Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testDisconnect() {

		try {
			m_brokerModel.onDisconnect();
			if (_broker.equals(BrokerModel._brokerTest)) {
				TestCase.assertFalse(m_brokerModel.isConnected());
			} else {
				TestCase.assertTrue(m_brokerModel.isConnected());
			}
		} catch (Exception ex) {
			TestCase.fail("Error testDisconnect Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testGetNextRequestId() {

		try {
			Integer id = m_brokerModel.getNextRequestId();
			TestCase.assertNotNull(id);
		} catch (Exception ex) {
			TestCase.fail("Error testGetNextRequestId Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testOnSubscribeAccountUpdates() {

		try {
			m_brokerModel.onSubscribeAccountUpdates(true, tradestrategy
					.getPortfolio().getIndividualAccount().getAccountNumber());
			TestCase.assertFalse(m_brokerModel
					.isAccountUpdatesRunning(tradestrategy.getPortfolio()
							.getIndividualAccount().getAccountNumber()));

		} catch (Exception ex) {
			TestCase.fail("Error testOnSubscribeAccountUpdates Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnCancelAccountUpdates() {

		try {
			m_brokerModel.onSubscribeAccountUpdates(true, tradestrategy
					.getPortfolio().getIndividualAccount().getAccountNumber());
			m_brokerModel.onCancelAccountUpdates(tradestrategy.getPortfolio()
					.getIndividualAccount().getAccountNumber());
			TestCase.assertFalse(m_brokerModel
					.isAccountUpdatesRunning(tradestrategy.getPortfolio()
							.getIndividualAccount().getAccountNumber()));
		} catch (Exception ex) {
			TestCase.fail("Error testOnCancelAccountUpdates Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnReqManagedAccount() {

		try {
			m_brokerModel.onReqManagedAccount();
			TestCase.assertFalse(m_brokerModel
					.isAccountUpdatesRunning(tradestrategy.getPortfolio()
							.getIndividualAccount().getAccountNumber()));
		} catch (Exception ex) {
			TestCase.fail("Error testOnReqManagedAccount Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnReqAllOpenOrders() {

		try {
			m_brokerModel.onReqAllOpenOrders();
		} catch (Exception ex) {
			TestCase.fail("Error testOnReqAllOpenOrders Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnReqOpenOrders() {

		try {
			m_brokerModel.onReqOpenOrders();
		} catch (Exception ex) {
			TestCase.fail("Error testOnReqOpenOrders Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testOnReqRealTimeBars() {

		try {

			this.tradestrategy.getContract().addTradestrategy(
					this.tradestrategy);
			m_brokerModel.onReqRealTimeBars(this.tradestrategy.getContract(),
					false);
			TestCase.assertFalse(m_brokerModel
					.isRealtimeBarsRunning(tradestrategy));
		} catch (Exception ex) {
			TestCase.fail("Error testOnReqRealTimeBars Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testOnReqAllExecutions() {

		try {
			m_brokerModel.onReqAllExecutions(this.tradestrategy.getTradingday()
					.getOpen());
		} catch (Exception ex) {
			TestCase.fail("Error testOnReqAllExecutions Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnReqExecutions() {

		try {
			m_brokerModel.onReqExecutions(this.tradestrategy);
		} catch (Exception ex) {
			TestCase.fail("Error testOnReqExecutions Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testIsRealtimeBarsRunning() {

		try {
			m_brokerModel.onCancelRealtimeBars(this.tradestrategy);
			TestCase.assertFalse(m_brokerModel
					.isRealtimeBarsRunning(this.tradestrategy));
		} catch (Exception ex) {
			TestCase.fail("Error testIsRealtimeBarsRunning Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testIsAccountUpdatesRunning() {

		try {
			m_brokerModel.onCancelAccountUpdates(tradestrategy.getPortfolio()
					.getIndividualAccount().getAccountNumber());
			TestCase.assertFalse(m_brokerModel
					.isAccountUpdatesRunning(tradestrategy.getPortfolio()
							.getIndividualAccount().getAccountNumber()));
		} catch (Exception ex) {
			TestCase.fail("Error testIsRealtimeBarsRunning Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testIsHistoricalDataRunningTradestrategy() {

		try {
			m_brokerModel.onCancelBrokerData(this.tradestrategy);
			TestCase.assertFalse(m_brokerModel
					.isHistoricalDataRunning(this.tradestrategy));
		} catch (Exception ex) {
			TestCase.fail("Error testIsHistoricalDataRunning Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testIsHistoricalDataRunningContract() {

		try {
			m_brokerModel.onCancelBrokerData(this.tradestrategy.getContract());
			TestCase.assertFalse(m_brokerModel
					.isHistoricalDataRunning(this.tradestrategy.getContract()));
		} catch (Exception ex) {
			TestCase.fail("Error testIsHistoricalDataRunning Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnCancelAllRealtimeData() {

		try {
			m_brokerModel.onCancelAllRealtimeData();
			TestCase.assertFalse(m_brokerModel
					.isRealtimeBarsRunning(this.tradestrategy));
		} catch (Exception ex) {
			TestCase.fail("Error testOnCancelAllRealtimeData Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnCancelRealtimeBars() {

		try {
			m_brokerModel.onCancelRealtimeBars(this.tradestrategy);
			TestCase.assertFalse(m_brokerModel
					.isRealtimeBarsRunning(this.tradestrategy));
		} catch (Exception ex) {
			TestCase.fail("Error testOnCancelRealtimeBars Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnCancelBrokerData() {

		try {
			m_brokerModel.onCancelBrokerData(this.tradestrategy);
			TestCase.assertFalse(m_brokerModel
					.isHistoricalDataRunning(this.tradestrategy));
		} catch (Exception ex) {
			TestCase.fail("Error testOnCancelBrokerData Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnCancelContractDetails() {

		try {
			m_brokerModel.onCancelContractDetails(this.tradestrategy
					.getContract());
		} catch (Exception ex) {
			TestCase.fail("Error testOnCancelContractDetails Msg: "
					+ ex.getMessage());
		}
	}

	@Test
	public void testOnContractDetails() {

		try {
			m_brokerModel.onContractDetails(this.tradestrategy.getContract());
		} catch (Exception ex) {
			TestCase.fail("Error testOnContractDetails Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testGetHistoricalData() {

		try {
			ConcurrentHashMap<Integer, Tradestrategy> historicalDataList = m_brokerModel
					.getHistoricalData();
			TestCase.assertNotNull(historicalDataList);
		} catch (Exception ex) {
			TestCase.fail("Error testGetHistoricalData Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testOnPlaceOrder() {

		try {
			TradeOrder tradeOrder = new TradeOrder(this.tradestrategy,
					Action.BUY, OrderType.MKT, 1000, null, null, new Date());
			tradeOrder = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder);
			TestCase.assertNotNull(tradeOrder);
		} catch (Exception ex) {
			TestCase.fail("Error testOnPlaceOrder Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testOnCancelOrder() {

		try {

			TradeOrder tradeOrder = new TradeOrder(this.tradestrategy,
					Action.BUY, OrderType.MKT, 1000, null, null, new Date());
			tradeOrder = m_brokerModel.onPlaceOrder(
					this.tradestrategy.getContract(), tradeOrder);
			TestCase.assertNotNull(tradeOrder);
			m_brokerModel.onCancelOrder(tradeOrder);
		} catch (Exception ex) {
			TestCase.fail("Error testOnCancelOrder Msg: " + ex.getMessage());
		}
	}

	@Test
	public void testIsBrokerDataOnly() {

		try {
			boolean result = m_brokerModel.isBrokerDataOnly();
			TestCase.assertFalse(result);
		} catch (Exception ex) {
			TestCase.fail("Error testIsBrokerDataOnly Msg: " + ex.getMessage());
		}
	}
}
