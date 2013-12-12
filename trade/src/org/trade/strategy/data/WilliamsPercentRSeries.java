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
import org.trade.strategy.data.williamspercentr.WilliamsPercentRItem;

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
@DiscriminatorValue("WilliamsPercentRSeries")
public class WilliamsPercentRSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	public static final String LENGTH = "Length";

	private Integer length;
	/*
	 * Vales used to calculate WilliamsPercentR. These need to be reset when the
	 * series is cleared.
	 */
	private double sum = 0.0;
	private LinkedList<Double> yyValues = new LinkedList<Double>();

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
	public WilliamsPercentRSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for WilliamsPercentRSeries.
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
	public WilliamsPercentRSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart, Integer length) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.length = length;
	}

	public WilliamsPercentRSeries() {
		super(IndicatorSeries.WilliamsPercentRSeries);
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		WilliamsPercentRSeries clone = (WilliamsPercentRSeries) super.clone();
		clone.yyValues = new LinkedList<Double>();
		return clone;
	}

	/**
	 * Removes all data items from the series and, unless the series is already
	 * empty, sends a {@link SeriesChangeEvent} to all registered listeners.
	 * Clears down and resets all the local calculated fields.
	 */
	public void clear() {
		super.clear();
		sum = 0.0;
		yyValues.clear();
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
		final WilliamsPercentRItem item = (WilliamsPercentRItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param williamsPercentR
	 *            the WilliamsPercentR.
	 */
	public void add(RegularTimePeriod period, BigDecimal williamsPercentR) {
		if (!this.isEmpty()) {
			WilliamsPercentRItem item0 = (WilliamsPercentRItem) this
					.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new WilliamsPercentRItem(period, williamsPercentR), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            WilliamsPercentRItem
	 */
	public void add(WilliamsPercentRItem dataItem, boolean notify) {
		if (!this.isEmpty()) {
			WilliamsPercentRItem item0 = (WilliamsPercentRItem) this
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
			WilliamsPercentRItem item = (WilliamsPercentRItem) this.data
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
			// get the current data item...
			CandleItem candleItem = (CandleItem) source.getDataItem(skip);
			if (0 != candleItem.getClose()) {
				if (this.yyValues.size() == getLength()) {
					/*
					 * If the item does not exist in the series then this is a
					 * new time period and so we need to remove the last in the
					 * set and add the new periods values. Otherwise we just
					 * update the last value in the set. Sum is just used for
					 * performance save having to sum the last set of values
					 * each time.
					 */
					if (newBar) {
						sum = sum - this.yyValues.getLast()
								+ candleItem.getClose();
						this.yyValues.removeLast();
						this.yyValues.addFirst(candleItem.getClose());
					} else {
						sum = sum - this.yyValues.getFirst()
								+ candleItem.getClose();
						this.yyValues.removeFirst();
						this.yyValues.addFirst(candleItem.getClose());
					}
				} else {
					if (newBar) {
						sum = sum + candleItem.getClose();
						this.yyValues.addFirst(candleItem.getClose());
					} else {
						sum = sum + candleItem.getClose()
								- this.yyValues.getFirst();
						this.yyValues.removeFirst();
						this.yyValues.addFirst(candleItem.getClose());

					}
				}

				if (this.yyValues.size() == getLength()) {
					double ma = sum / getLength();
					if (newBar) {
						WilliamsPercentRItem dataItem = new WilliamsPercentRItem(
								candleItem.getPeriod(), new BigDecimal(ma));
						this.add(dataItem, false);

					} else {
						WilliamsPercentRItem dataItem = (WilliamsPercentRItem) this
								.getDataItem(this.getItemCount() - 1);
						dataItem.setWilliamsPercentR(ma);
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
			WilliamsPercentRItem dataItem = (WilliamsPercentRItem) this
					.getDataItem(i);
			_log.info("Type: " + this.getType() + " Time: "
					+ dataItem.getPeriod().getStart() + " MA: "
					+ dataItem.getWilliamsPercentR());
		}
	}

}
