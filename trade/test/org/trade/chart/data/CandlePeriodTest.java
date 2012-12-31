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
package org.trade.chart.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.jfree.data.time.RegularTimePeriod;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.factory.ClassFactory;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyTest;
import org.trade.persistent.dao.Tradingday;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandlePeriod;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class CandlePeriodTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(CandlePeriodTest.class);
	private PersistentModel tradePersistentModel = null;
	private Tradestrategy tradestrategy = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		tradePersistentModel = (PersistentModel) ClassFactory
				.getServiceForInterface(PersistentModel._persistentModel, this);
		this.tradestrategy = TradestrategyTest.getTestTradestrategy();
		TestCase.assertNotNull(this.tradestrategy);
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		TradestrategyTest.removeTestTradestrategy();
	}

	@Test
	public void testGetCandleBar() {

		try {

			Date startPeriod = this.tradestrategy.getTradingday().getOpen();
			Date prevTradingday = TradingCalendar.addDays(tradestrategy
					.getTradingday().getOpen(), (-1 * (tradestrategy
					.getChartDays() - 1)));
			prevTradingday = TradingCalendar
					.getMostRecentTradingDay(prevTradingday);
			List<Candle> candles = tradePersistentModel
					.findCandlesByContractDateRangeBarSize(this.tradestrategy
							.getContract().getIdContract(), prevTradingday,
							this.tradestrategy.getTradingday().getOpen(),
							this.tradestrategy.getBarSize());

			if (candles.isEmpty()) {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						Tradingday.newInstance(prevTradingday), 2,
						BarSize.FIVE_MIN, true, 0);
			} else {
				CandleDataset.populateSeries(
						this.tradestrategy.getDatasetContainer(), candles);
			}
			TestCase.assertFalse(this.tradestrategy.getDatasetContainer()
					.getBaseCandleSeries().isEmpty());
			Candle candle = this.tradestrategy
					.getDatasetContainer()
					.getBaseCandleSeries()
					.getBar(TradingCalendar.getSpecificTime(this.tradestrategy
							.getTradingday().getOpen(), TradingCalendar
							.getPrevTradingDay(startPeriod)),
							TradingCalendar.getSpecificTime(this.tradestrategy
									.getTradingday().getClose(),
									TradingCalendar
											.getPrevTradingDay(startPeriod)));
			_log.info("Bar for Contract: " + candle.getContract().getSymbol()
					+ " Start Period: " + candle.getPeriod() + " Open: "
					+ candle.getOpen() + " High: " + candle.getHigh()
					+ " Low: " + candle.getLow() + " Close: "
					+ candle.getClose() + " Vwap: " + candle.getVwap()
					+ " Volume: " + candle.getVolume());

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testGetAvgCandleBar() {

		try {

			Date startPeriod = this.tradestrategy.getTradingday().getOpen();
			Date prevTradingday = TradingCalendar.addDays(this.tradestrategy
					.getTradingday().getOpen(), (-1 * (this.tradestrategy
					.getChartDays() - 1)));
			prevTradingday = TradingCalendar
					.getMostRecentTradingDay(prevTradingday);
			List<Candle> candles = tradePersistentModel
					.findCandlesByContractDateRangeBarSize(this.tradestrategy
							.getContract().getIdContract(), prevTradingday,
							this.tradestrategy.getTradingday().getOpen(),
							this.tradestrategy.getBarSize());
			if (candles.isEmpty()) {
				StrategyData.doDummyData(this.tradestrategy
						.getDatasetContainer().getBaseCandleSeries(),
						Tradingday.newInstance(prevTradingday), 2,
						BarSize.FIVE_MIN, true, 0);
			} else {
				CandleDataset.populateSeries(
						this.tradestrategy.getDatasetContainer(), candles);
			}
			TestCase.assertFalse(this.tradestrategy.getDatasetContainer()
					.getBaseCandleSeries().isEmpty());
			Candle candle = this.tradestrategy
					.getDatasetContainer()
					.getBaseCandleSeries()
					.getAverageBar(
							TradingCalendar.getSpecificTime(this.tradestrategy
									.getTradingday().getOpen(), TradingCalendar
									.getPrevTradingDay(startPeriod)),
							TradingCalendar.getSpecificTime(this.tradestrategy
									.getTradingday().getClose(),
									TradingCalendar
											.getPrevTradingDay(startPeriod)),
							false);
			_log.info("Non wieghted avg bar for Contract: "
					+ candle.getContract().getSymbol() + " Start Period: "
					+ candle.getPeriod() + " Open: " + candle.getOpen()
					+ " High: " + candle.getHigh() + " Low: " + candle.getLow()
					+ " Close: " + candle.getClose() + " Vwap: "
					+ candle.getVwap() + " Volume: " + candle.getVolume());

			candle = this.tradestrategy
					.getDatasetContainer()
					.getBaseCandleSeries()
					.getAverageBar(
							TradingCalendar.getSpecificTime(this.tradestrategy
									.getTradingday().getOpen(), TradingCalendar
									.getPrevTradingDay(startPeriod)),
							TradingCalendar.getSpecificTime(this.tradestrategy
									.getTradingday().getClose(),
									TradingCalendar
											.getPrevTradingDay(startPeriod)),
							true);
			_log.info("Wieghted avg bar for Contract: "
					+ candle.getContract().getSymbol() + " Start Period: "
					+ candle.getPeriod() + " Open: " + candle.getOpen()
					+ " High: " + candle.getHigh() + " Low: " + candle.getLow()
					+ " Close: " + candle.getClose() + " Vwap: "
					+ candle.getVwap() + " Volume: " + candle.getVolume());

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testDateConversion() {
		try {
			String dateString = "20111129  06:35:11";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("PST"));
			Date date = sdf.parse(dateString);
			_log.info("Date GMT time: " + date);
			sdf.setTimeZone(TimeZone.getTimeZone("EST"));
			_log.info("Date EST time: " + sdf.format(date));
			TestCase.assertNotNull(date);
		} catch (ParseException e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testSecondsNext() {

		int size = 100;
		int secondsLength = 3600;

		RegularTimePeriod period = new CandlePeriod(
				TradingCalendar.getTodayBusinessDayStart(), secondsLength);

		for (int i = 0; i < size; i++) {
			_log.info("Time is : " + period.toString() + " Start: "
					+ period.getStart() + " End: " + period.getEnd());
			period = period.next();
			TestCase.assertNotNull(period);
		}
	}

	@Test
	public void testSecondsPrev() {

		int size = 100;
		int secondsLength = 3600;

		RegularTimePeriod period = new CandlePeriod(
				TradingCalendar.getTodayBusinessDayEnd(), secondsLength);

		for (int i = 0; i < size; i++) {
			_log.info("Time is : " + period.toString() + " Start: "
					+ period.getStart() + " End: " + period.getEnd());
			period = period.previous();
			TestCase.assertNotNull(period);
		}
	}

	@Test
	public void testFindCurrentTimePeriod() {

		int secondsLength = 300;
		Date now = new Date();
		Date startBusDate = TradingCalendar.getBusinessDayStart(now);
		long periods = (now.getTime() / 1000 - startBusDate.getTime() / 1000)
				/ secondsLength;
		long startPeriod = (startBusDate.getTime())
				+ (periods * secondsLength * 1000);

		RegularTimePeriod period = new CandlePeriod(new Date(startPeriod),
				secondsLength);
		_log.info("\n Bus Day Start : " + startBusDate.toString()
				+ "\n Start: " + period.getStart() + "\n End: "
				+ period.getEnd() + "\n Periods: " + periods);
		TestCase.assertNotNull(period);
	}
}
