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
package org.trade.strategy.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Percent;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.candle.CandlePeriod;

/**
 * A list of (RegularTimePeriod, open, high, low, close) data items.
 * 
 * @since 1.0.4
 * 
 * @see OHLCSeriesCollection
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
@Entity
@DiscriminatorValue("CandleSeries")
public class CandleSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	private final static Logger _log = LoggerFactory
			.getLogger(CandleSeries.class);

	public static final String SYMBOL = "Symbol";
	public static final String CURRENCY = "Currency";
	public static final String EXCHANGE = "Exchange";
	public static final String SEC_TYPE = "SECType";

	private Contract contract;
	private String symbol;
	private String currency;
	private String exchange;
	private String secType;
	private Date startTime;
	private Date endTime;
	private int barSize = 0;
	private Double sumVwap = new Double(0);
	private Long sumVolume = new Long(0);
	private Double vwapHigh = new Double(0);
	private Double vwapLow = new Double(99999);
	private Percent percentChangeFromClose = new Percent(0);
	private Percent percentChangeFromOpen = new Percent(0);
	private int rollupInterval = 0;
	private Candle candleBar = null;
	private LinkedList<Double> vwapValues = new LinkedList<Double>();
	private LinkedList<Long> volumeValues = new LinkedList<Long>();
	private LinkedList<Double> vwapCalculatedValues = new LinkedList<Double>();

	public CandleSeries() {
		super(IndicatorSeries.CandleSeries, true, 0, false);
	}

	/**
	 * Creates a new empty series. By default, items added to the series will be
	 * sorted into ascending order by period, and duplicate periods will not be
	 * allowed.
	 * 
	 * @param contract
	 *            the Contract for this candle series.
	 * @param barSize
	 *            the length in minutes for each bar ie. 5, 15, 30, 60
	 * 
	 */
	public CandleSeries(CandleSeries series, int barSize, Date startTime,
			Date endTime) {
		super(series.getContract().getSymbol(), IndicatorSeries.CandleSeries,
				series.getDisplaySeries(), 0, series.getSubChart());
		this.symbol = series.getContract().getSymbol();
		this.contract = series.getContract();
		this.barSize = series.getBarSize();
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Creates a new empty series. By default, items added to the series will be
	 * sorted into ascending order by period, and duplicate periods will not be
	 * allowed.
	 * 
	 * @param legend
	 *            the title that appears on the bottom of the chart.
	 * @param contract
	 *            the Contract for this candle series.
	 * @param barSize
	 *            the length in minutes for each bar ie. 5, 15, 30, 60
	 * 
	 * 
	 */

	public CandleSeries(String legend, Contract contract, int barSize,
			Date startTime, Date endTime) {
		super(legend, IndicatorSeries.CandleSeries, true, 0, false);
		this.contract = contract;
		this.symbol = contract.getSymbol();
		this.barSize = barSize;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * Constructor for CandleSeries.
	 * 
	 * @param strategy
	 *            Strategy
	 * @param name
	 *            String
	 * @param type
	 *            String
	 * @param description
	 *            String
	 * @param displayOnChart
	 *            Boolean
	 * @param chartRGBColor
	 *            Integer
	 * @param subChart
	 *            Boolean
	 */
	public CandleSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Returns the contract ID.
	 * 
	 * 
	 * @return contractId.
	 */
	@Transient
	public Contract getContract() {
		if (null == this.contract) {
			this.contract = new Contract(this.getSecType(), this.getSymbol(),
					this.getExchange(), this.getCurrency(), null, null);
		}
		return this.contract;
	}

	/**
	 * Method getStartTime.
	 * 
	 * @return Date
	 */
	@Transient
	public Date getStartTime() {
		return this.startTime;
	}

	/**
	 * Method setStartTime.
	 * 
	 * @param startTime
	 *            Date
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	/**
	 * Method getEndTime.
	 * 
	 * @return Date
	 */
	@Transient
	public Date getEndTime() {
		return this.endTime;
	}

	/**
	 * Method setEndTime.
	 * 
	 * @param endTime
	 *            Date
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * Method getSymbol.
	 * 
	 * @return String
	 */
	@Transient
	public String getSymbol() {
		try {
			if (null == this.symbol)
				this.symbol = (String) this.getValueCode(SYMBOL);
		} catch (Exception e) {
			this.symbol = null;
		}
		return this.symbol;
	}

	/**
	 * Method setSymbol.
	 * 
	 * @param symbol
	 *            String
	 */
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	/**
	 * Method getCurrency.
	 * 
	 * @return String
	 */
	@Transient
	public String getCurrency() {
		try {
			if (null == this.currency)
				this.currency = (String) this.getValueCode(CURRENCY);
		} catch (Exception e) {
			this.currency = null;
		}
		return this.currency;
	}

	/**
	 * Method setCurrency.
	 * 
	 * @param currency
	 *            String
	 */
	public void setCurrency(String currency) {
		this.currency = currency;
	}

	/**
	 * Method getExchange.
	 * 
	 * @return String
	 */
	@Transient
	public String getExchange() {
		try {
			if (null == this.exchange)
				this.exchange = (String) this.getValueCode(EXCHANGE);
		} catch (Exception e) {
			this.exchange = null;
		}
		return this.exchange;
	}

	/**
	 * Method setExchange.
	 * 
	 * @param exchange
	 *            String
	 */
	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	/**
	 * Method getSecType.
	 * 
	 * @return String
	 */
	@Transient
	public String getSecType() {
		try {
			if (null == this.secType)
				this.secType = (String) this.getValueCode(SEC_TYPE);
		} catch (Exception e) {
			this.secType = null;
		}
		return this.secType;
	}

	/**
	 * Method setSecType.
	 * 
	 * @param secType
	 *            String
	 */
	public void setSecType(String secType) {
		this.secType = secType;
	}

	/**
	 * Returns the time period for the specified item.
	 * 
	 * @param index
	 *            the item index.
	 * 
	 * 
	 * @return The time period.
	 */
	public RegularTimePeriod getPeriod(int index) {
		final CandleItem item = (CandleItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Returns the time period for the specified item.
	 * 
	 * 
	 * 
	 * 
	 * @return The time period.
	 */
	@Transient
	public int getBarSize() {
		return this.barSize;
	}

	/**
	 * Returns the time period for the specified item.
	 * 
	 * 
	 * 
	 * 
	 * @param barSize
	 *            Integer
	 */
	public void setBarSize(Integer barSize) {
		this.barSize = barSize;
	}

	/**
	 * Returns the data item at the specified index.
	 * 
	 * @param index
	 *            the item index.
	 * 
	 * 
	 * @return The data item.
	 */
	public ComparableObjectItem getDataItem(int index) {
		return super.getDataItem(index);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param open
	 *            the open-value.
	 * @param high
	 *            the high-value.
	 * @param low
	 *            the low-value.
	 * @param close
	 *            the close-value.
	 * @param volume
	 *            the volume-value.
	 * @param vwap
	 *            the vwap-value.
	 * @param tradeCount
	 *            the tradeCount-value.
	 * @param contract
	 *            Contract
	 * @param lastUpdateDate
	 *            Date
	 */
	public void add(Contract contract, Tradingday tradingday,
			RegularTimePeriod period, double open, double high, double low,
			double close, long volume, double vwap, int tradeCount,
			Date lastUpdateDate) {
		if (getItemCount() > 0) {
			CandleItem item0 = (CandleItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new CandleItem(contract, tradingday, period, open, high, low,
				close, volume, vwap, tradeCount, lastUpdateDate), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param candleItem
	 *            CandleItem
	 * @param notify
	 *            boolean
	 */
	public void add(CandleItem candleItem, boolean notify) {
		if (getItemCount() > 0) {
			CandleItem item0 = (CandleItem) this.getDataItem(0);
			if (!candleItem.getPeriod().getClass()
					.equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(candleItem, notify);
	}

	/**
	 * Returns the true/false if the date falls within a period.
	 * 
	 * @param date
	 *            the date for which we want a period.
	 * 
	 * 
	 * @return exists
	 */
	public int indexOf(Date date) {

		for (int i = this.data.size(); i > 0; i--) {
			CandleItem item = (CandleItem) this.data.get(i - 1);
			if (date.getTime() > item.getPeriod().getLastMillisecond()) {
				break;
			}
			if ((date.getTime() >= item.getPeriod().getFirstMillisecond())
					&& (date.getTime() <= item.getPeriod().getLastMillisecond())) {
				return i - 1;
			}

		}
		return -1;
	}

	/**
	 * Returns the last completed candle or -1 if still building.
	 * 
	 * @param time
	 *            the date for which we want a candle period to be updated.
	 * 
	 * @param open
	 *            the open-value.
	 * @param high
	 *            the high-value.
	 * @param low
	 *            the low-value.
	 * @param close
	 *            the close-value.
	 * @param volume
	 *            the volume value.
	 * @param vwap
	 *            the volume weighted price.
	 * @param tradeCount
	 *            the number of trades.
	 * 
	 * @param rollupInterval
	 *            the interval to roll up Vwap
	 * 
	 * @return completedCandle the last completed candle or -1 if still building
	 */
	int buildCandle(Date time, double open, double high, double low,
			double close, long volume, double vwap, int tradeCount,
			int rollupInterval) {

		int index = this.indexOf(time);
		// _log.info("Symbol :" + this.getSymbol() + "Bar Time: " + time
		// + " Index: " + index + " open: " + open + " high: " + high
		// + " low: " + low + " close: " + close + " volume: " + volume
		// + " vwap: " + vwap + " tradeCount: " + tradeCount
		// + " rollupInterval: " + rollupInterval);

		this.setCalcVwap(rollupInterval, volume, vwap);
		CandleItem candle = null;
		if (index >= 0) {

			candle = (CandleItem) this.getDataItem(index);

			if (candle.getHigh() < high) {
				candle.setHigh(high);
			}
			if (candle.getLow() > low) {
				candle.setLow(low);
			}
			candle.setClose(close);
			if (rollupInterval > 1) {
				candle.setVolume(candle.getVolume() + volume);
				candle.setCount(candle.getCount() + tradeCount);
			} else {
				candle.setVolume(volume);
				candle.setCount(tradeCount);
			}
			candle.setVwap((this.getVwap() == 0 ? close : this.getVwap()));
			candle.setLastUpdateDate(time);
		} else {

			/*
			 * If the Vwap is zero use the close price. This should not happen
			 * but some data from brokers does not always have a value.
			 * 
			 * 
			 * For 60min time period start the clock at 9:00am. This matches
			 * most charting platforms.
			 */
			Date startBusDate = TradingCalendar.getSpecificTime(
					this.getStartTime(), time);
			if (3600 == this.getBarSize()) {
				startBusDate = TradingCalendar.addMinutes(startBusDate, -30);
				if (TradingCalendar.getMinute(time) == 30
						&& startBusDate.equals(time)) {
					time = TradingCalendar.addMinutes(time, -30);
				}
			}
			long periods = (time.getTime() / 1000 - startBusDate.getTime() / 1000)
					/ this.getBarSize();
			long startPeriod = startBusDate.getTime()
					+ (periods * this.getBarSize() * 1000);
			Date start = new Date(startPeriod);
			Tradingday tradingday = new Tradingday(
					TradingCalendar.getSpecificTime(this.getStartTime(), start),
					TradingCalendar.getSpecificTime(this.getEndTime(), start));
			candle = new CandleItem(this.getContract(), tradingday,
					new CandlePeriod(start, this.getBarSize()), open, high,
					low, close, volume, (this.getVwap() == 0 ? close
							: this.getVwap()), tradeCount, time);
			this.add(candle, false);
			index = this.indexOf(start);
		}
		return index;
	}

	/**
	 * Removes all data items from the series and, unless the series is already
	 * empty, sends a {@link SeriesChangeEvent} to all registered listeners.
	 * Clears down and resets all the Vwap calculated fields.
	 */
	public void clear() {
		super.clear();
		clearVwap();

	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		CandleSeries clone = (CandleSeries) super.clone();
		clone.vwapValues = new LinkedList<Double>();
		clone.volumeValues = new LinkedList<Long>();
		clone.vwapCalculatedValues = new LinkedList<Double>();
		clone.sumVwap = new Double(0);
		clone.sumVolume = new Long(0);
		clone.vwapHigh = new Double(0);
		clone.vwapLow = new Double(99999);
		return clone;
	}

	private void clearVwap() {
		vwapValues.clear();
		volumeValues.clear();
		vwapCalculatedValues.clear();
		sumVwap = new Double(0);
		sumVolume = new Long(0);
		vwapHigh = new Double(0);
		vwapLow = new Double(99999);
	}

	/**
	 * Uses a rollup Vwap based on a greater time period. So 5 sec bars rolled
	 * up to 5min bars will rollup interval of 5min/5sec = 60.
	 * 
	 * 
	 * @param rollupInterval
	 *            the rollup Interval.
	 * @param volume
	 *            long
	 * @param vwap
	 *            double
	 */

	public void setCalcVwap(int rollupInterval, long volume, double vwap) {

		if (this.rollupInterval != rollupInterval) {
			this.clearVwap();
			this.rollupInterval = rollupInterval;
		}

		if (rollupInterval == this.vwapValues.size()) {
			Long volSum = this.volumeValues.removeLast();
			Double vwapSum = this.vwapValues.removeLast();
			this.vwapCalculatedValues.removeLast();
			sumVwap = sumVwap - vwapSum;
			sumVolume = sumVolume - volSum;
		}
		this.volumeValues.addFirst(volume);
		sumVolume = sumVolume + volume;

		this.vwapValues.addFirst((volume * vwap));
		sumVwap = sumVwap + (volume * vwap);

		this.vwapCalculatedValues.addFirst(getVwap());

		if (vwapLow > getVwap()) {
			vwapLow = getVwap();
		}

		if (vwapHigh < getVwap()) {
			vwapHigh = getVwap();
		}
	}

	/**
	 * Method getVwapHigh.
	 * 
	 * @return double
	 */
	@Transient
	public double getVwapHigh() {
		return vwapHigh;
	}

	/**
	 * Method getVwapLow.
	 * 
	 * @return double
	 */
	@Transient
	public double getVwapLow() {
		return vwapLow;
	}

	/**
	 * Method getVwap.
	 * 
	 * @return double
	 */
	@Transient
	public double getVwap() {
		double vwap = 0;
		if (sumVolume > 0) {
			vwap = sumVwap / sumVolume;
		}
		return vwap;
	}

	/**
	 * Checks to see if the previous number or bars make a V shape using the x,y
	 * vwap values.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param source
	 *            CandleSeries
	 * @param skip
	 *            int
	 * @throws ValueTypeException
	 */

	public void updateSeries(CandleSeries source, int skip) {

		if (source == null) {
			throw new IllegalArgumentException("Null source (CandleSeries).");
		}

		for (int i = skip; i < source.getItemCount(); i++) {
			if (i >= 0) {
				CandleItem candle = (CandleItem) source.getDataItem(i);
				buildCandle(candle.getLastUpdateDate(), candle.getOpen(),
						candle.getHigh(), candle.getLow(), candle.getClose(),
						candle.getVolume(), candle.getVwap(),
						candle.getCount(),
						this.getBarSize() / source.getBarSize());
			}
		}
	}

	/**
	 * Method getAverageBar.
	 * 
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @param wieghted
	 *            boolean
	 * @return Candle
	 */
	public Candle getAverageBar(Date startDate, Date endDate, boolean wieghted) {

		int itemCount = this.getItemCount() - 1;
		long sumVolume = 0;
		double sunHighPriceXVolume = 0;
		double sunLowPriceXVolume = 0;
		double sunOpenPriceXVolume = 0;
		double sunClosePriceXVolume = 0;
		double sunClosePriceXVolumeVwap = 0;
		double numberOfCandles = 0;
		CandleItem candle = null;
		for (int i = itemCount; i > -1; i--) {
			candle = (CandleItem) this.getDataItem(i);
			if ((candle.getPeriod().getStart().equals(startDate) || candle
					.getPeriod().getStart().after(startDate))
					&& (candle.getPeriod().getStart().equals(endDate) || candle
							.getPeriod().getStart().before(endDate))) {

				if (candle.getVolume() > 0)
					numberOfCandles++;

				sunHighPriceXVolume = sunHighPriceXVolume
						+ ((wieghted ? candle.getVolume() : 1) * candle
								.getHigh());
				sunLowPriceXVolume = sunLowPriceXVolume
						+ ((wieghted ? candle.getVolume() : 1) * candle
								.getLow());
				sunOpenPriceXVolume = sunOpenPriceXVolume
						+ ((wieghted ? candle.getVolume() : 1) * candle
								.getOpen());
				sunClosePriceXVolume = sunClosePriceXVolume
						+ ((wieghted ? candle.getVolume() : 1) * candle
								.getClose());
				sunClosePriceXVolumeVwap = sunClosePriceXVolumeVwap
						+ (candle.getVolume() * candle.getClose());
				sumVolume = sumVolume + candle.getVolume();
			}
		}
		if (numberOfCandles > 0 && sumVolume > 0) {
			Candle avgCandle = new Candle(getContract(), 0, 0, 0, 99999,
					new Date());
			avgCandle.setStartPeriod(startDate);
			avgCandle.setEndPeriod(endDate);
			avgCandle.setHigh(new BigDecimal(
					(sunHighPriceXVolume / (wieghted ? sumVolume
							: numberOfCandles))));
			avgCandle.setLow(new BigDecimal(
					(sunLowPriceXVolume / (wieghted ? sumVolume
							: numberOfCandles))));
			avgCandle.setOpen(new BigDecimal(
					(sunOpenPriceXVolume / (wieghted ? sumVolume
							: numberOfCandles))));
			avgCandle.setClose(new BigDecimal(
					(sunClosePriceXVolume / (wieghted ? sumVolume
							: numberOfCandles))));
			avgCandle.setVwap(new BigDecimal(sunClosePriceXVolumeVwap
					/ sumVolume));
			avgCandle.setVolume(sumVolume);
			return avgCandle;
		}
		return null;
	}

	/**
	 * Method getBar.
	 * 
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @return Candle
	 */
	public Candle getBar(Date startDate, Date endDate) {

		if (null != this.candleBar) {
			if (this.candleBar.getStartPeriod().equals(startDate)
					&& this.candleBar.getEndPeriod().equals(endDate)) {
				return this.candleBar;
			} else {
				this.candleBar = null;
			}
		}

		int itemCount = this.getItemCount() - 1;
		long sumVolume = 0;
		double sunClosePriceXVolumeVwap = 0;
		CandleItem candle = null;
		for (int i = itemCount; i > -1; i--) {
			candle = (CandleItem) this.getDataItem(i);
			if ((candle.getPeriod().getStart().equals(startDate) || candle
					.getPeriod().getStart().after(startDate))
					&& (candle.getPeriod().getStart().before(endDate))) {
				if (null == this.candleBar) {
					this.candleBar = new Candle(getContract(), 0, 0, 99999, 0,
							new Date());
					this.candleBar.setEndPeriod(candle.getPeriod().getEnd());
				}

				if (this.candleBar.getClose().doubleValue() == 0)
					this.candleBar.setClose(new BigDecimal(candle.getClose()));

				if (this.candleBar.getHigh().doubleValue() < candle.getHigh())
					this.candleBar.setHigh(new BigDecimal(candle.getHigh()));

				if (this.candleBar.getLow().doubleValue() > candle.getLow())
					this.candleBar.setLow(new BigDecimal(candle.getLow()));

				sunClosePriceXVolumeVwap = sunClosePriceXVolumeVwap
						+ (candle.getVolume() * candle.getClose());
				sumVolume = sumVolume + candle.getVolume();
			}
		}
		if (null != candle) {
			this.candleBar.setStartPeriod(candle.getPeriod().getStart());
			this.candleBar.setOpen(new BigDecimal(candle.getOpen()));
			if (sumVolume > 0) {
				this.candleBar.setVwap(new BigDecimal(sunClosePriceXVolumeVwap
						/ sumVolume));
				this.candleBar.setVolume(sumVolume);
			} else {
				this.candleBar
						.setVwap(new BigDecimal(sunClosePriceXVolumeVwap));
				this.candleBar.setVolume(0L);
			}

		}
		return this.candleBar;
	}

	/**
	 * Method getPercentChangeFromClose.
	 * 
	 * @return Percent
	 */
	@Transient
	public Percent getPercentChangeFromClose() {
		return percentChangeFromClose;
	}

	/**
	 * Method getPercentChangeFromOpen.
	 * 
	 * @return Percent
	 */
	@Transient
	public Percent getPercentChangeFromOpen() {
		return percentChangeFromOpen;
	}

	/**
	 * Method updatePercentChanged.
	 * 
	 * @param candleItem
	 *            CandleItem
	 */
	public void updatePercentChanged(CandleItem candleItem) {

		Date prevDay = TradingCalendar.getPrevTradingDay(candleItem.getPeriod()
				.getStart());
		Date prevDayEnd = TradingCalendar.getSpecificTime(this.getEndTime(),
				prevDay);
		prevDayEnd = TradingCalendar.addSeconds(prevDayEnd, -1);
		Date prevDayStart = TradingCalendar.getSpecificTime(
				this.getStartTime(), prevDay);
		Date todayOpen = TradingCalendar.getSpecificTime(this.getStartTime(),
				candleItem.getPeriod().getStart());
		int index = this.indexOf(todayOpen);
		if (index > -1) {
			CandleItem openCandleItem = (CandleItem) this.getDataItem(index);
			try {
				percentChangeFromOpen.setValue(new Percent((candleItem
						.getClose() - openCandleItem.getOpen())
						/ openCandleItem.getOpen()));
			} catch (ValueTypeException ex) {
				_log.error("Could not set ValueType Msg: " + ex.getMessage(),
						ex);
			}
			if (candleItem.getPeriod().getStart().after(prevDayEnd)) {
				if (this.indexOf(prevDayStart) > -1
						&& this.indexOf(prevDayEnd) > -1) {
					Candle prevDayCandle = this
							.getBar(prevDayStart, prevDayEnd);
					// _log.info("prevDayCandle Start:"
					// + prevDayCandle.getStartPeriod() + " End period: "
					// + prevDayCandle.getEndPeriod() + " Open:"
					// + prevDayCandle.getOpen() + " High: "
					// + prevDayCandle.getHigh() + " Low:"
					// + prevDayCandle.getLow() + " Close: "
					// + prevDayCandle.getClose());
					try {
						percentChangeFromClose.setValue(new Percent((candleItem
								.getClose() - prevDayCandle.getClose()
								.doubleValue())
								/ prevDayCandle.getClose().doubleValue()));
					} catch (ValueTypeException ex) {
						_log.error(
								"Could not set ValueType Msg: "
										+ ex.getMessage(), ex);
					}
				}
			}
		}
	}

	/**
	 * Method createSeries.
	 * 
	 * @param source
	 *            CandleDataset
	 * @param seriesIndex
	 *            int
	 */
	@Override
	public void createSeries(CandleDataset source, int seriesIndex) {
		// TODO Auto-generated method stub

	}
}
