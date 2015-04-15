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
import org.trade.strategy.data.mfi.MoneyFlowIndexItem;

/**
 * The Money Flow Index (MFI) is an oscillator that uses both price and volume
 * to measure buying and selling pressure. Created by Gene Quong and Avrum
 * Soudack, MFI is also known as volume-weighted RSI. MFI starts with the
 * typical price for each period. Money flow is positive when the typical price
 * rises (buying pressure) and negative when the typical price declines (selling
 * pressure). A ratio of positive and negative money flow is then plugged into
 * an RSI formula to create an oscillator that moves between zero and one
 * hundred. As a momentum oscillator tied to volume, the Money Flow Index (MFI)
 * is best suited to identify reversals and price extremes with a variety of
 * signals.
 * 
 * There are a several steps involved in the Money Flow Index calculation. The
 * example below is based on a 14-period Money Flow Index, which is the default
 * setting in SharpCharts and the setting recommended by the creators.
 * 
 * 1. Typical Price = (High + Low + Close)/3
 * 
 * 2. Raw Money Flow = Typical Price x Volume
 * 
 * 3. Money Flow Ratio = (14-period Positive Money Flow)/(14-period Negative
 * Money Flow)
 * 
 * 4. Money Flow Index = 100 - 100/(1 + Money Flow Ratio)
 * 
 * @since 1.0.4
 * 
 * @see OHLCSeriesCollection
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */

@Entity
@DiscriminatorValue("MoneyFlowIndexSeries")
public class MoneyFlowIndexSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	public static final String LENGTH = "Length";
	public static final String ROLLING_CANDLE = "RollingCandle";

	private Integer length;
	private Boolean rollingCandle;

	private double positiveSum = 0.0;
	private double negativeSum = 0.0;

	private LinkedList<Double> yyValues = new LinkedList<Double>();
	private LinkedList<Long> volValues = new LinkedList<Long>();

	/**
	 * Creates a new empty series. By default, items added to the series will be
	 * sorted into ascending order by period, and duplicate periods will not be
	 * allowed.
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
	public MoneyFlowIndexSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for MovingAverageSeries.
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
	 * @param length
	 *            Integer
	 */
	public MoneyFlowIndexSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart, Integer length) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.length = length;
	}

	public MoneyFlowIndexSeries() {
		super(IndicatorSeries.MoneyFlowIndexSeries);
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		MoneyFlowIndexSeries clone = (MoneyFlowIndexSeries) super.clone();
		clone.yyValues = new LinkedList<Double>();
		clone.volValues = new LinkedList<Long>();
		return clone;
	}

	/**
	 * Removes all data items from the series and, unless the series is already
	 * empty, sends a {@link SeriesChangeEvent} to all registered listeners.
	 * Clears down and resets all the local calculated fields.
	 */
	public void clear() {
		super.clear();
		positiveSum = 0.0;
		negativeSum = 0.0;
		yyValues.clear();
		volValues.clear();
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
		final MoneyFlowIndexItem item = (MoneyFlowIndexItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param moneyFlowIndex
	 *            the moneyFlowIndex.
	 */
	public void add(RegularTimePeriod period, BigDecimal moneyFlowIndex) {
		if (!this.isEmpty()) {
			MoneyFlowIndexItem item0 = (MoneyFlowIndexItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new MoneyFlowIndexItem(period, moneyFlowIndex), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            MoneyFlowIndexItem
	 */
	public void add(MoneyFlowIndexItem dataItem, boolean notify) {
		if (!this.isEmpty()) {
			MoneyFlowIndexItem item0 = (MoneyFlowIndexItem) this.getDataItem(0);
			if (!dataItem.getPeriod().getClass()
					.equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(dataItem, notify);
	}

	/**
	 * Method getLength.
	 * 
	 * @return Integer
	 */
	@Transient
	public Integer getLength() {
		try {
			if (null == this.length)
				this.length = (Integer) CodeValue.getValueCode(LENGTH,
						this.getCodeValues());
		} catch (Exception e) {
			this.length = null;
		}
		return this.length;
	}

	/**
	 * Method setLength.
	 * 
	 * @param length
	 *            Integer
	 */
	public void setLength(Integer length) {
		this.length = length;
	}

	/**
	 * Method getRollingCandle.
	 * 
	 * @return Boolean
	 */
	@Transient
	public Boolean getRollingCandle() {
		try {
			if (null == this.rollingCandle)
				this.rollingCandle = (Boolean) CodeValue.getValueCode(
						ROLLING_CANDLE, this.getCodeValues());
		} catch (Exception e) {
			this.rollingCandle = null;
		}
		return this.rollingCandle;
	}

	/**
	 * Method setRollingCandle.
	 * 
	 * @param rollingCandle
	 *            Boolean
	 */
	public void setRollingCandle(Boolean rollingCandle) {
		this.rollingCandle = rollingCandle;
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
		if (getLength() == null || getLength() < 1) {
			throw new IllegalArgumentException(
					"MA period must be greater than zero.");
		}

		if (source.getItemCount() > skip) {
			if (source.getItemCount() > 1) {

				// Get the current data item...
				CandleItem candleItem = (CandleItem) source.getDataItem(skip);
				// Get the previous candle.
				CandleItem prevCandleItem = (CandleItem) source
						.getDataItem(skip - 1);

				double prevTypicalPrice = (prevCandleItem.getHigh()
						+ prevCandleItem.getLow() + prevCandleItem.getClose()) / 3;
				if (this.getRollingCandle()) {
					prevTypicalPrice = (source.getPreviousRollingCandle()
							.getClose()
							+ source.getPreviousRollingCandle().getHigh() + source
							.getPreviousRollingCandle().getLow()) / 3;
				}

				if (0 != candleItem.getClose()) {

					double typicalPrice = (candleItem.getClose()
							+ candleItem.getHigh() + candleItem.getLow()) / 3;
					double value = typicalPrice * candleItem.getVolume();
					if (typicalPrice < prevTypicalPrice)
						value = typicalPrice * candleItem.getVolume() * -1;

					if (this.getRollingCandle()) {
						typicalPrice = (source.getRollingCandle().getClose()
								+ source.getRollingCandle().getHigh() + source
								.getRollingCandle().getLow()) / 3;
						value = typicalPrice
								* source.getRollingCandle().getVolume();
						if (typicalPrice < prevTypicalPrice)
							value = typicalPrice
									* source.getRollingCandle().getVolume()
									* -1;
					}
					if (value > 0) {
						positiveSum = positiveSum + value;
					} else {
						negativeSum = negativeSum + Math.abs(value);
					}
					if (this.yyValues.size() == getLength()) {
						/*
						 * If the item does not exist in the series then this is
						 * a new time period and so we need to remove the last
						 * in the set and add the new periods values. Otherwise
						 * we just update the last value in the set. Sum is just
						 * used for performance save having to sum the last set
						 * of values each time.
						 */

						if (newBar) {
							if (this.yyValues.getLast() > 0) {
								positiveSum = positiveSum
										- this.yyValues.getLast();
							} else {
								negativeSum = negativeSum
										- Math.abs(this.yyValues.getLast());
							}

							this.yyValues.removeLast();
							this.yyValues.addFirst(value);
							this.volValues.removeLast();
							this.volValues.addFirst(candleItem.getVolume());
						} else {
							if (this.yyValues.getFirst() > 0) {
								positiveSum = positiveSum
										- this.yyValues.getFirst();
							} else {
								negativeSum = negativeSum
										- Math.abs(this.yyValues.getFirst());
							}
							this.yyValues.removeFirst();
							this.yyValues.addFirst(value);
						}
					} else {
						if (newBar) {
							this.yyValues.addFirst(value);
							this.volValues.addFirst(candleItem.getVolume());
						} else {
							if (this.yyValues.getFirst() > 0) {
								positiveSum = positiveSum
										- this.yyValues.getFirst();
							} else {
								negativeSum = negativeSum
										- Math.abs(this.yyValues.getFirst());
							}
							this.yyValues.removeFirst();
							this.yyValues.addFirst(value);
							this.volValues.removeFirst();
							this.volValues.addFirst(candleItem.getVolume());
						}
					}

					if (this.yyValues.size() == this.getLength()) {
						if (negativeSum == 0)
							negativeSum = 1;
						double mfi = 100 - (100 / (1 + (positiveSum / negativeSum)));
						if (newBar) {
							MoneyFlowIndexItem dataItem = new MoneyFlowIndexItem(
									candleItem.getPeriod(), new BigDecimal(mfi));
							this.add(dataItem, false);

						} else {
							MoneyFlowIndexItem dataItem = (MoneyFlowIndexItem) this
									.getDataItem(this.getItemCount() - 1);
							dataItem.setMoneyFlowIndex(mfi);
						}
					}
				}
			}
		}
	}

	/**
	 * Method printSeries.
	 * 
	 */
	public void printSeries() {
		for (int i = 0; i < this.getItemCount(); i++) {
			MoneyFlowIndexItem dataItem = (MoneyFlowIndexItem) this
					.getDataItem(i);
			_log.debug("Type: " + this.getType() + " Time: "
					+ dataItem.getPeriod().getStart() + " Value: "
					+ dataItem.getMoneyFlowIndex());
		}
	}

}
