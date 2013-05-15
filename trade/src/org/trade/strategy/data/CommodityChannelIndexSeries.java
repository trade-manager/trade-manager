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

import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.cci.CommodityChannelIndexItem;

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
@DiscriminatorValue("CommodityChannelIndexSeries")
public class CommodityChannelIndexSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	public static final String LENGTH = "Length";

	private Integer length;
	/*
	 * Vales used to calculate CommodityChannelIndex's. These need to be reset
	 * when the series is cleared.
	 */
	private double sumTypicalPrice = 0;
	private LinkedList<Double> typicalPriceValues = new LinkedList<Double>();

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
	public CommodityChannelIndexSeries(Strategy strategy, String name,
			String type, String description, Boolean displayOnChart,
			Integer chartRGBColor, Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for CommodityChannelIndexSeries.
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
	public CommodityChannelIndexSeries(Strategy strategy, String name,
			String type, String description, Boolean displayOnChart,
			Integer chartRGBColor, Boolean subChart, Integer length) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.length = length;
	}

	public CommodityChannelIndexSeries() {
		super(IndicatorSeries.CommodityChannelIndexSeries);
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		CommodityChannelIndexSeries clone = (CommodityChannelIndexSeries) super
				.clone();
		clone.typicalPriceValues = new LinkedList<Double>();
		return clone;
	}

	/**
	 * Removes all data items from the series and, unless the series is already
	 * empty, sends a {@link SeriesChangeEvent} to all registered listeners.
	 * Clears down and resets all the local calculated fields.
	 */
	public void clear() {
		super.clear();
		sumTypicalPrice = 0;
		typicalPriceValues.clear();
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
		final CommodityChannelIndexItem item = (CommodityChannelIndexItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param movingAverage
	 *            the movingAverage.
	 */
	public void add(RegularTimePeriod period, BigDecimal cciAverage) {
		if (!this.isEmpty()) {
			CommodityChannelIndexItem item0 = (CommodityChannelIndexItem) this
					.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new CommodityChannelIndexItem(period, cciAverage), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            MovingAverageItem
	 */
	public void add(CommodityChannelIndexItem dataItem, boolean notify) {
		if (!this.isEmpty()) {
			CommodityChannelIndexItem item0 = (CommodityChannelIndexItem) this
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
			CommodityChannelIndexItem item = (CommodityChannelIndexItem) this.data
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
			this.updateSeries(source.getSeries(seriesIndex), i, true);
		}

	}

	/**
	 * Method printSeries.
	 */
	public void printSeries() {
		for (int i = 0; i < this.getItemCount(); i++) {
			CommodityChannelIndexItem dataItem = (CommodityChannelIndexItem) this
					.getDataItem(i);
			_log.info("Type: " + this.getType() + " Time: "
					+ dataItem.getPeriod().getStart() + " CCI: "
					+ dataItem.getCommodityChannelIndex());
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
					"CCI period must be  greater than zero.");
		}

		if (source.getItemCount() > skip) {

			// get the current data item...
			CandleItem candleItem = (CandleItem) source.getDataItem(skip);

			double typicalPrice = (source.getRollingCandle().getClose()
					+ source.getRollingCandle().getHigh() + source
					.getRollingCandle().getLow()) / 3;
			if (0 != typicalPrice) {
				if (typicalPriceValues.size() == getLength()) {
					/*
					 * If the item does not exist in the series then this is a
					 * new time period and so we need to remove the last in the
					 * set and add the new periods values. Otherwise we just
					 * update the last value in the set. Sum is just used for
					 * performance save having to sum the last set of values
					 * each time.
					 */
					if (newBar) {
						sumTypicalPrice = sumTypicalPrice
								- typicalPriceValues.getLast() + typicalPrice;
						typicalPriceValues.removeLast();
						typicalPriceValues.addFirst(typicalPrice);
					} else {
						sumTypicalPrice = sumTypicalPrice
								- typicalPriceValues.getFirst() + typicalPrice;
						typicalPriceValues.removeFirst();
						typicalPriceValues.addFirst(typicalPrice);
					}
				} else {
					if (newBar) {
						sumTypicalPrice = sumTypicalPrice + typicalPrice;
						typicalPriceValues.addFirst(typicalPrice);
					} else {
						sumTypicalPrice = sumTypicalPrice + typicalPrice
								- typicalPriceValues.getFirst();
						typicalPriceValues.removeFirst();
						typicalPriceValues.addFirst(typicalPrice);
					}
				}

				if (typicalPriceValues.size() == getLength()) {
					double cci = calculateCCI(sumTypicalPrice,
							typicalPriceValues);
					// _log.info("Period: " + candleItem.getPeriod() + " CCI: "
					// + cci + " newBar: " + newBar);
					if (newBar) {
						CommodityChannelIndexItem dataItem = new CommodityChannelIndexItem(
								candleItem.getPeriod(), new BigDecimal(cci));
						this.add(dataItem, false);

					} else {
						CommodityChannelIndexItem dataItem = (CommodityChannelIndexItem) this
								.getDataItem(this.getItemCount() - 1);
						dataItem.setCommodityChannelIndex(cci);
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
	private double calculateCCI(Double sumTypicalPrice,
			LinkedList<Double> typicalPriceValues) {
		double typicalPriceSMA = sumTypicalPrice / getLength();
		double sumMeanDeviation = 0;
		for (double typicalPrice : typicalPriceValues) {
			sumMeanDeviation = sumMeanDeviation
					+ Math.abs(typicalPriceSMA - typicalPrice);
		}
		if (sumMeanDeviation == 0)
			return 0;
		return (typicalPriceValues.getFirst() - typicalPriceSMA)
				/ (0.015 * (sumMeanDeviation / getLength()));
	}
}
