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
import java.util.LinkedList;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.persistent.dao.CodeValue;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.base.RegularTimePeriod;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.macd.MACDItem;

/**
 * Developed by Gerald Appel in the late seventies, the Moving Average
 * Convergence-Divergence (MACD) indicator is one of the simplest and most
 * effective momentum indicators available. The MACD turns two trend-following
 * indicators, moving averages, into a momentum oscillator by subtracting the
 * longer moving average from the shorter moving average. As a result, the MACD
 * offers the best of both worlds: trend following and momentum. The MACD
 * fluctuates above and below the zero line as the moving averages converge,
 * cross and diverge. Traders can look for signal line crossovers, centerline
 * crossovers and divergences to generate signals. Because the MACD is
 * unbounded, it is not particularly useful for identifying over bought and over
 * sold levels.
 * 
 * MACD Line: (12-day EMA - 26-day EMA)
 * 
 * Signal Line: 9-day EMA of MACD Line
 * 
 * MACD Histogram: MACD Line - Signal Line
 * 
 * @since 1.0.4
 * 
 * @see OHLCSeriesCollection
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */

@Entity
@DiscriminatorValue("MACDSeries")
public class MACDSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	public static final String FAST_LENGTH = "Fast Length";
	public static final String SLOW_LENGTH = "Slow Length";
	public static final String SIGNAL_SMOOTHING = "Signal Smoothing";
	public static final String SMA_TYPE = "Simple Smoothing MA";

	private Boolean simpleMAType;
	private Integer fastLength;
	private Integer slowLength;
	private Integer signalSmoothing;

	private double fastSum = 0.0;
	private double prevFastEMA = 0;
	private double fastMultiplyer = Double.MAX_VALUE;
	private LinkedList<Double> fastYYValues = new LinkedList<Double>();

	private double slowSum = 0.0;
	private double prevSlowEMA = 0;
	private double slowMultiplyer = Double.MAX_VALUE;
	private LinkedList<Double> slowYYValues = new LinkedList<Double>();

	private double signalSmoothingSum = 0.0;
	private double prevSignalSmoothingEMA = 0;
	private double signalSmoothingMultiplyer = Double.MAX_VALUE;
	private LinkedList<Double> signalSmoothingYYValues = new LinkedList<Double>();

	/**
	 * Creates a new empty series. By default, items added to the series will be
	 * sorted into ascending order by period, and duplicate periods will not be
	 * allowed.
	 * 
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
	public MACDSeries(Strategy strategy, String name, String type, String description, Boolean displayOnChart,
			Integer chartRGBColor, Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor, subChart);
	}

	/**
	 * Constructor for MACDSeries.
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
	 * @param numberOfSTD
	 *            BigDecimal
	 * @param length
	 *            Integer
	 */
	public MACDSeries(Strategy strategy, String name, String type, String description, Boolean displayOnChart,
			Integer chartRGBColor, Boolean subChart, Integer fastLength, Integer slowLength, Integer signalSmoothing) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor, subChart);
		this.fastLength = fastLength;
		this.slowLength = slowLength;
		this.signalSmoothing = signalSmoothing;
	}

	public MACDSeries() {
		super(IndicatorSeries.MACDSeries);
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		MACDSeries clone = (MACDSeries) super.clone();
		clone.fastYYValues = new LinkedList<Double>();
		return clone;
	}

	/**
	 * Removes all data items from the series and, unless the series is already
	 * empty, sends a {@link SeriesChangeEvent} to all registered listeners.
	 * Clears down and resets all the local calculated fields.
	 */
	public void clear() {
		super.clear();
		fastSum = 0.0;
		fastMultiplyer = Double.MAX_VALUE;
		fastYYValues.clear();
		slowSum = 0.0;
		slowMultiplyer = Double.MAX_VALUE;
		slowYYValues.clear();
		signalSmoothingSum = 0.0;
		signalSmoothingMultiplyer = Double.MAX_VALUE;
		signalSmoothingYYValues.clear();
		prevFastEMA = 0;
		prevSlowEMA = 0;
		prevSignalSmoothingEMA = 0;
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
		final MACDItem item = (MACDItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param MACD
	 *            the MACD.
	 */
	public void add(RegularTimePeriod period, BigDecimal MACD, BigDecimal signalLine, BigDecimal MACDHistogram) {
		if (!this.isEmpty()) {
			MACDItem item0 = (MACDItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException("Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new MACDItem(period, MACD, signalLine, MACDHistogram), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            MACDItem
	 */
	public void add(MACDItem dataItem, boolean notify) {
		if (!this.isEmpty()) {
			MACDItem item0 = (MACDItem) this.getDataItem(0);
			if (!dataItem.getPeriod().getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException("Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(dataItem, notify);
	}

	/**
	 * Method getFastLength.
	 * 
	 * @return Integer
	 */
	@Transient
	public Integer getFastLength() {
		try {
			if (null == this.fastLength)
				this.fastLength = (Integer) CodeValue.getValueCode(FAST_LENGTH, this.getCodeValues());
		} catch (Exception e) {
			this.fastLength = null;
		}
		return this.fastLength;
	}

	/**
	 * Method setFastLength.
	 * 
	 * @param fastLength
	 *            Integer
	 */
	public void setFastLength(Integer fastLength) {
		this.fastLength = fastLength;
	}

	/**
	 * Method getSlowLength.
	 * 
	 * @return Integer
	 */
	@Transient
	public Integer getSlowLength() {
		try {
			if (null == this.slowLength)
				this.slowLength = (Integer) CodeValue.getValueCode(SLOW_LENGTH, this.getCodeValues());
		} catch (Exception e) {
			this.slowLength = null;
		}
		return this.slowLength;
	}

	/**
	 * Method setSlowLength.
	 * 
	 * @param slowLength
	 *            Integer
	 */
	public void setSlowLength(Integer slowLength) {
		this.slowLength = slowLength;
	}

	/**
	 * Method getSignalSmoothing.
	 * 
	 * @return Integer
	 */
	@Transient
	public Integer getSignalSmoothing() {
		try {
			if (null == this.signalSmoothing)
				this.signalSmoothing = (Integer) CodeValue.getValueCode(SIGNAL_SMOOTHING, this.getCodeValues());
		} catch (Exception e) {
			this.signalSmoothing = null;
		}
		return this.signalSmoothing;
	}

	/**
	 * Method setSignalSmoothing.
	 * 
	 * @param signalSmoothing
	 *            Integer
	 */
	public void setSignalSmoothing(Integer signalSmoothing) {
		this.signalSmoothing = signalSmoothing;
	}

	/**
	 * Method getSimpleMAType.
	 * 
	 * @return Boolean
	 */
	@Transient
	public Boolean getSimpleMAType() {
		try {
			if (null == this.simpleMAType)
				this.simpleMAType = (Boolean) CodeValue.getValueCode(SMA_TYPE, this.getCodeValues());
		} catch (Exception e) {
			this.simpleMAType = null;
		}
		return this.simpleMAType;
	}

	/**
	 * Method setSimpleMAType.
	 * 
	 * @param simpleMAType
	 *            Boolean
	 */
	public void setSimpleMAType(Boolean simpleMAType) {
		this.simpleMAType = simpleMAType;
	}

	/**
	 * Method createSeries.
	 * 
	 * @param source
	 *            CandleDataset
	 * @param seriesIndex
	 *            int
	 */
	public void createSeries(CandleDataset source, int seriesIndex) {

		if (source.getSeries(seriesIndex) == null) {
			throw new IllegalArgumentException("Null source (CandleDataset).");
		}

		for (int i = 0; i < source.getSeries(seriesIndex).getItemCount(); i++) {
			this.updateSeries(source.getSeries(seriesIndex), i, true);
		}
	}

	/**
	 * Method updateSeries.
	 * 
	 * @param source
	 *            CandleSeries
	 * @param skip
	 *            int
	 * @param newBar
	 *            boolean
	 */
	public void updateSeries(CandleSeries source, int skip, boolean newBar) {

		if (source == null) {
			throw new IllegalArgumentException("Null source (CandleSeries).");
		}
		if (getFastLength() == null || getFastLength() < 1) {
			throw new IllegalArgumentException("Fast MA must be greater than zero.");
		}

		if (getSlowLength() == null || getSlowLength() < 1) {
			throw new IllegalArgumentException("Slow MA must be greater than zero.");
		}

		if (getSignalSmoothing() == null || getSignalSmoothing() < 1) {
			throw new IllegalArgumentException("Signal Smoothing must be greater than zero.");
		}

		if (getSlowLength() < getFastLength()) {
			throw new IllegalArgumentException("Fast MA must be greater than Slow MA.");
		}

		if (source.getItemCount() > skip) {

			// get the current data item...
			CandleItem candleItem = (CandleItem) source.getDataItem(skip);

			// work out the average for the earlier values...
			Number yy = candleItem.getY();

			if (null != yy) {
				if (this.fastYYValues.size() == getFastLength()) {
					/*
					 * If the item does not exist in the series then this is a
					 * new time period and so we need to remove the last in the
					 * set and add the new periods values. Otherwise we just
					 * update the last value in the set. Sum is just used for
					 * performance save having to sum the last set of values
					 * each time.
					 */
					if (newBar) {
						fastSum = fastSum - this.fastYYValues.getLast() + yy.doubleValue();
						this.fastYYValues.removeLast();
						this.fastYYValues.addFirst(yy.doubleValue());

					} else {
						fastSum = fastSum - this.fastYYValues.getFirst() + yy.doubleValue();
						this.fastYYValues.removeFirst();
						this.fastYYValues.addFirst(yy.doubleValue());
					}
				} else {
					if (newBar) {
						fastSum = fastSum + yy.doubleValue();
						this.fastYYValues.addFirst(yy.doubleValue());
					} else {
						fastSum = fastSum + yy.doubleValue() - this.fastYYValues.getFirst();
						this.fastYYValues.removeFirst();
						this.fastYYValues.addFirst(yy.doubleValue());
					}
				}
				if (this.slowYYValues.size() == getSlowLength()) {
					/*
					 * If the item does not exist in the series then this is a
					 * new time period and so we need to remove the last in the
					 * set and add the new periods values. Otherwise we just
					 * update the last value in the set. Sum is just used for
					 * performance save having to sum the last set of values
					 * each time.
					 */
					if (newBar) {
						slowSum = slowSum - this.slowYYValues.getLast() + yy.doubleValue();
						this.slowYYValues.removeLast();
						this.slowYYValues.addFirst(yy.doubleValue());

					} else {
						slowSum = slowSum - this.slowYYValues.getFirst() + yy.doubleValue();
						this.slowYYValues.removeFirst();
						this.slowYYValues.addFirst(yy.doubleValue());
					}
				} else {
					if (newBar) {
						slowSum = slowSum + yy.doubleValue();
						this.slowYYValues.addFirst(yy.doubleValue());
					} else {
						slowSum = slowSum + yy.doubleValue() - this.slowYYValues.getFirst();
						this.slowYYValues.removeFirst();
						this.slowYYValues.addFirst(yy.doubleValue());
					}
				}

				if (this.slowYYValues.size() == getSlowLength()) {

					double fastEMA = 0;
					if (fastMultiplyer == Double.MAX_VALUE) {
						fastEMA = fastSum / this.getFastLength();
						fastMultiplyer = 2 / (this.getFastLength() + 1.0d);
					} else {
						fastEMA = ((this.fastYYValues.getFirst() - prevFastEMA) * fastMultiplyer) + prevFastEMA;
					}
					prevFastEMA = fastEMA;
					double slowEMA = 0;
					if (slowMultiplyer == Double.MAX_VALUE) {
						slowEMA = slowSum / this.getSlowLength();
						slowMultiplyer = 2 / (this.getSlowLength() + 1.0d);
					} else {
						slowEMA = ((this.slowYYValues.getFirst() - prevSlowEMA) * slowMultiplyer) + prevSlowEMA;
					}
					prevSlowEMA = slowEMA;
					double MACD = fastEMA - slowEMA;
					if (this.signalSmoothingYYValues.size() == this.getSignalSmoothing()) {
						/*
						 * If the item does not exist in the series then this is
						 * a new time period and so we need to remove the last
						 * in the set and add the new periods values. Otherwise
						 * we just update the last value in the set. Sum is just
						 * used for performance save having to sum the last set
						 * of values each time.
						 */

						if (newBar) {
							signalSmoothingSum = signalSmoothingSum - this.signalSmoothingYYValues.getLast() + MACD;
							this.signalSmoothingYYValues.removeLast();
							this.signalSmoothingYYValues.addFirst(MACD);

						} else {
							signalSmoothingSum = signalSmoothingSum - this.signalSmoothingYYValues.getFirst() + MACD;
							this.signalSmoothingYYValues.removeFirst();
							this.signalSmoothingYYValues.addFirst(MACD);
						}

					} else {
						if (newBar) {
							signalSmoothingSum = signalSmoothingSum + MACD;
							this.signalSmoothingYYValues.addFirst(MACD);
						} else {
							signalSmoothingSum = signalSmoothingSum + MACD - this.signalSmoothingYYValues.getFirst();
							this.signalSmoothingYYValues.removeFirst();
							this.signalSmoothingYYValues.addFirst(MACD);
						}
					}
					double signalLine = Double.MAX_VALUE;
					if (this.signalSmoothingYYValues.size() == getSignalSmoothing()) {

						signalLine = calculateSmoothingMA(this.signalSmoothingYYValues.getFirst(),
								this.prevSignalSmoothingEMA, this.signalSmoothingSum);
						this.prevSignalSmoothingEMA = signalLine;
					}
					if (newBar) {
						MACDItem dataItem = new MACDItem(candleItem.getPeriod(), new BigDecimal(MACD),
								(signalLine == Double.MAX_VALUE ? null : new BigDecimal(signalLine)),
								(signalLine == Double.MAX_VALUE ? null : new BigDecimal(MACD - signalLine)));
						this.add(dataItem, false);

					} else {
						MACDItem dataItem = (MACDItem) this.getDataItem(this.getItemCount() - 1);
						dataItem.setMACD(MACD);
						if (signalLine == Double.MAX_VALUE) {
							dataItem.setSignalLine(signalLine);
							dataItem.setMACDHistogram(MACD - signalLine);
						}
					}
				}
			}
		}
	}

	/**
	 * Method calculateMA.
	 * 
	 * @param calcType
	 *            String
	 * @param yyValues
	 *            LinkedList<Double>
	 * @param volValues
	 *            LinkedList<Long>
	 * @param sum
	 *            Double
	 * @return double
	 */
	private double calculateSmoothingMA(double close, double prevSignalSmoothingEMA, Double sum) {

		double ma = 0;
		if (this.getSimpleMAType()) {
			ma = sum / getSignalSmoothing();
		} else {
			/*
			 * Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818
			 * (18.18%). EMA: {Close - EMA(previous day)} x * multiplier +
			 * EMA(previous day).
			 */
			if (this.signalSmoothingMultiplyer == Double.MAX_VALUE) {
				ma = sum / getSignalSmoothing();
				this.signalSmoothingMultiplyer = 2 / (getSignalSmoothing() + 1.0d);
			} else {
				ma = ((close - prevSignalSmoothingEMA) * this.signalSmoothingMultiplyer) + prevSignalSmoothingEMA;
			}

		}
		return ma;
	}

	/**
	 * Method printSeries.
	 * 
	 */
	public void printSeries() {
		for (int i = 0; i < this.getItemCount(); i++) {
			MACDItem dataItem = (MACDItem) this.getDataItem(i);
			_log.debug("Type: " + this.getType() + " Time: " + dataItem.getPeriod().getStart() + " Value: "
					+ dataItem.getMACD());
		}
	}
}
