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

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.volume.VolumeItem;

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
@DiscriminatorValue("VolumeSeries")
public class VolumeSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	private long barWidthInMilliseconds;

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

	public VolumeSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for VolumeSeries.
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
	 * @param barWidthInMilliseconds
	 *            long
	 */
	public VolumeSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart, long barWidthInMilliseconds) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.barWidthInMilliseconds = barWidthInMilliseconds;
	}

	public VolumeSeries() {
		super(IndicatorSeries.VolumeSeries);
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
		final VolumeItem item = (VolumeItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * 
	 * 
	 * @param volume
	 *            Long
	 * @param side
	 *            boolean
	 */
	public void add(RegularTimePeriod period, Long volume, boolean side) {
		if (getItemCount() > 0) {
			VolumeItem item0 = (VolumeItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new VolumeItem(period, volume, side), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            VolumeItem
	 */
	public void add(VolumeItem dataItem, boolean notify) {
		if (getItemCount() > 0) {
			VolumeItem item0 = (VolumeItem) this.getDataItem(0);
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
			VolumeItem item = (VolumeItem) this.data.get(i - 1);
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
	 * Method createSeries.
	 * 
	 * @param candleDataset
	 *            CandleDataset
	 * @param seriesIndex
	 *            int
	 */
	public void createSeries(CandleDataset candleDataset, int seriesIndex) {

		if (candleDataset.getSeries(seriesIndex) == null) {
			throw new IllegalArgumentException("Null source (XYDataset).");
		}

		this.updateSeries(candleDataset.getSeries(seriesIndex), 0);

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
		/*
		 * Do not want to add the new bar.
		 */
		for (int i = skip; i < source.getItemCount(); i++) {
			if (i >= 0) {
				CandleItem candleItem = (CandleItem) source.getDataItem(i);
				/*
				 * If the item does not exist in the series then this is a new
				 * time period and so we need to remove the last in the set and
				 * add the new periods values. Otherwise we just update the last
				 * value in the set.
				 */
				int index = this.indexOf(candleItem.getPeriod());
				if (index < 0) {
					VolumeItem dataItem = new VolumeItem(
							candleItem.getPeriod(), new Long(
									candleItem.getVolume()),
							candleItem.getSide());
					this.add(dataItem, true);
				} else {
					VolumeItem dataItem = (VolumeItem) this.getDataItem(index);
					dataItem.setVolume(candleItem.getVolume());
					dataItem.setSide(candleItem.getSide());
				}
			}
		}
	}

	/**
	 * Method getBarWidthInMilliseconds.
	 * 
	 * @return long
	 */
	@Transient
	public long getBarWidthInMilliseconds() {
		return this.barWidthInMilliseconds;
	}

	/**
	 * Method setBarWidthInMilliseconds.
	 * 
	 * @param barWidthInMilliseconds
	 *            long
	 */
	public void setBarWidthInMilliseconds(long barWidthInMilliseconds) {
		this.barWidthInMilliseconds = barWidthInMilliseconds;
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		VolumeSeries clone = (VolumeSeries) super.clone();
		return clone;
	}
}
