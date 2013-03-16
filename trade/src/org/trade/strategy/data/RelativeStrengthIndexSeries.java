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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.rsi.RelativeStrengthIndexItem;

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
@DiscriminatorValue("RelativeStrengthIndexSeries")
public class RelativeStrengthIndexSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	public static final String LENGTH = "Length";

	private Integer length;
	/*
	 * Vales used to calculate RelativeStrengthIndex. These need to be reset
	 * when the series is cleared.
	 */
	private double posSumCloseDiff = 0;
	private double negSumCloseDiff = 0;
	private double preDiffCloseValue = 0;
	private double avgLossRSI = 0;
	private double avgGainRSI = 0;
	private double prevAvgLossRSI = 0;
	private double prevAvgGainRSI = 0;
	private double currentRSI = Double.MAX_VALUE;

	/**
	 * Creates a new empty series. By default, items added to the series will be
	 * sorted into ascending order by period, and duplicate periods will not be
	 * allowed.
	 * 
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
	public RelativeStrengthIndexSeries(Strategy strategy, String name,
			String type, String description, Boolean displayOnChart,
			Integer chartRGBColor, Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for RelativeStrengthIndexSeries.
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
	public RelativeStrengthIndexSeries(Strategy strategy, String name,
			String type, String description, Boolean displayOnChart,
			Integer chartRGBColor, Boolean subChart, Integer length) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.length = length;
	}

	public RelativeStrengthIndexSeries() {
		super(IndicatorSeries.RelativeStrengthIndexSeries);
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		RelativeStrengthIndexSeries clone = (RelativeStrengthIndexSeries) super
				.clone();
		return clone;
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
		final RelativeStrengthIndexItem item = (RelativeStrengthIndexItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * 
	 * 
	 * 
	 * @param relativeStrengthIndex
	 *            BigDecimal
	 */
	public void add(RegularTimePeriod period, BigDecimal relativeStrengthIndex) {
		if (getItemCount() > 0) {
			RelativeStrengthIndexItem item0 = (RelativeStrengthIndexItem) this
					.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new RelativeStrengthIndexItem(period, relativeStrengthIndex),
				true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param dataItem
	 *            the RelativeStrengthIndex.
	 * @param notify
	 *            the notify listeners.
	 */
	public void add(RelativeStrengthIndexItem dataItem, boolean notify) {
		if (getItemCount() > 0) {
			RelativeStrengthIndexItem item0 = (RelativeStrengthIndexItem) this
					.getDataItem(0);
			if (!dataItem.getPeriod().getClass()
					.equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(dataItem, notify);
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
			RelativeStrengthIndexItem item = (RelativeStrengthIndexItem) this.data
					.get(i - 1);
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
	 * Method getLength.
	 * 
	 * @return Integer
	 */
	@Transient
	public Integer getLength() {
		try {
			if (null == this.length)
				this.length = (Integer) this.getValueCode(LENGTH);
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
			this.updateSeries(source.getSeries(seriesIndex), i);
		}

	}

	/**
	 * Method updateSeries.
	 * 
	 * @param source
	 *            CandleSeries
	 * @param skip
	 *            int
	 */
	public void updateSeries(CandleSeries source, int skip) {

		if (source == null) {
			throw new IllegalArgumentException("Null source (CandleSeries).");
		}
		if (getLength() == null || getLength() < 1) {
			throw new IllegalArgumentException(
					"RSI period must be  greater than zero.");
		}

		if (skip == 0) {
			posSumCloseDiff = 0;
			negSumCloseDiff = 0;
			avgGainRSI = 0;
			avgLossRSI = 0;
			prevAvgLossRSI = 0;
			prevAvgGainRSI = 0;
			currentRSI = Double.MAX_VALUE;
		}
		if (source.getItemCount() > skip) {

			// get the current data item...
			CandleItem candleItem = (CandleItem) source.getDataItem(skip);
			int index = this.indexOf(candleItem.getPeriod());
			double diffCloseValue = 0;
			if (source.getItemCount() > 1) {
				CandleItem prevCandleItem = (CandleItem) source
						.getDataItem(skip - 1);
				diffCloseValue = candleItem.getClose()
						- prevCandleItem.getClose();

				/*
				 * If the item does not exist in the series then this is a new
				 * time period and so we need to remove the last in the set and
				 * add the new periods values. Otherwise we just update the last
				 * value in the set.
				 */

				if (index < 0) {
					/*
					 * sum is just used for performance save having to sum the
					 * last set of values each time.
					 */
					if (diffCloseValue > 0) {
						posSumCloseDiff = posSumCloseDiff
								+ Math.abs(diffCloseValue);

					} else {
						negSumCloseDiff = negSumCloseDiff
								+ Math.abs(diffCloseValue);
					}
					prevAvgLossRSI = avgLossRSI;
					prevAvgGainRSI = avgGainRSI;
					preDiffCloseValue = diffCloseValue;

				} else {
					if (diffCloseValue > 0 && preDiffCloseValue > 0) {
						posSumCloseDiff = posSumCloseDiff
								+ Math.abs(diffCloseValue)
								- Math.abs(preDiffCloseValue);

					} else if (diffCloseValue > 0 && preDiffCloseValue < 0) {
						posSumCloseDiff = posSumCloseDiff
								+ Math.abs(diffCloseValue);
						negSumCloseDiff = negSumCloseDiff
								- Math.abs(preDiffCloseValue);
					} else if (diffCloseValue < 0 && preDiffCloseValue < 0) {
						negSumCloseDiff = negSumCloseDiff
								+ Math.abs(diffCloseValue)
								- Math.abs(preDiffCloseValue);

					} else if (diffCloseValue < 0 && preDiffCloseValue > 0) {
						negSumCloseDiff = negSumCloseDiff
								+ Math.abs(diffCloseValue);
						posSumCloseDiff = posSumCloseDiff
								- Math.abs(preDiffCloseValue);
					}
				}
			}
			if (skip >= getLength()) {
				if (currentRSI == Double.MAX_VALUE) {
					avgGainRSI = posSumCloseDiff / getLength();
					avgLossRSI = negSumCloseDiff / getLength();
					currentRSI = 100 - (100 / (1 + (avgGainRSI / avgLossRSI)));
				} else {
					if (preDiffCloseValue > 0) {
						avgGainRSI = (((prevAvgGainRSI * (getLength() - 1)) + Math
								.abs(preDiffCloseValue))) / getLength();
						avgLossRSI = (((prevAvgLossRSI * (getLength() - 1)) + 0))
								/ getLength();

					} else {
						avgGainRSI = (((prevAvgGainRSI * (getLength() - 1)) + 0))
								/ getLength();
						avgLossRSI = (((prevAvgLossRSI * (getLength() - 1)) + Math
								.abs(preDiffCloseValue))) / getLength();
					}
					currentRSI = 100 - (100 / (1 + (avgGainRSI / avgLossRSI)));
				}

				if (index < 0) {
					RelativeStrengthIndexItem dataItem = new RelativeStrengthIndexItem(
							candleItem.getPeriod(), new BigDecimal(currentRSI));
					this.add(dataItem, false);
				} else {
					RelativeStrengthIndexItem dataItem = (RelativeStrengthIndexItem) this
							.getDataItem(index);
					dataItem.setRelativeStrengthIndex(currentRSI);
				}
			}
		}
	}
}
