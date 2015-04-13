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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.base.RegularTimePeriod;
import org.trade.strategy.data.vwap.VwapItem;

/**
 * Volume-Weighted Average Price (VWAP) is exactly what it sounds like: the
 * average price weighted by volume. VWAP equals the dollar value of all trading
 * periods divided by the total trading volume for the current day. Calculation
 * starts when trading opens and ends when trading closes. Because it is good
 * for the current trading day only, intraday periods and data are used in the
 * calculation.
 * 
 * Cumulative(Volume x Typical Price)/Cumulative(Volume)
 * 
 * @since 1.0.4
 * 
 * @see OHLCSeriesCollection
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
@Entity
@DiscriminatorValue("VwapSeries")
public class VwapSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

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
	public VwapSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	public VwapSeries() {
		super(IndicatorSeries.VwapSeries);
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
		final VwapItem item = (VwapItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * 
	 * 
	 * @param vwapPrice
	 *            BigDecimal
	 */
	public void add(RegularTimePeriod period, BigDecimal vwapPrice) {
		if (!this.isEmpty()) {
			VwapItem item0 = (VwapItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new VwapItem(period, vwapPrice), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            VwapItem
	 */
	public void add(VwapItem dataItem, boolean notify) {
		if (!this.isEmpty()) {
			VwapItem item0 = (VwapItem) this.getDataItem(0);
			if (!dataItem.getPeriod().getClass()
					.equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(dataItem, notify);
	}

	/**
	 * Method createSeries.
	 * 
	 * @param candleDataset
	 *            CandleDataset
	 * @param seriesIndex
	 *            int
	 */
	public void createSeries(CandleDataset source, int seriesIndex) {

		if (source.getSeries(seriesIndex) == null) {
			throw new IllegalArgumentException("Null source (XYDataset).");
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

		if (source.getItemCount() > skip) {

			/*
			 * If the item does not exist in the series then this is a new time
			 * period and so we need to remove the last in the set and add the
			 * new periods values. Otherwise we just update the last value in
			 * the set.
			 */
			if (newBar) {
				VwapItem dataItem = new VwapItem(source.getRollingCandle()
						.getPeriod(), new BigDecimal(source.getRollingCandle()
						.getVwap()));
				this.add(dataItem, false);
			} else {
				VwapItem dataItem = (VwapItem) this.getDataItem(this
						.getItemCount() - 1);
				dataItem.setVwapPrice(source.getRollingCandle().getVwap());
			}
		}
	}

	/**
	 * Method printSeries.
	 * 
	 */
	public void printSeries() {
		for (int i = 0; i < this.getItemCount(); i++) {
			VwapItem dataItem = (VwapItem) this.getDataItem(i);
			_log.debug("Type: " + this.getType() + " Time: "
					+ dataItem.getPeriod().getStart() + " Value: "
					+ dataItem.getVwapPrice());
		}
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		VwapSeries clone = (VwapSeries) super.clone();
		return clone;
	}
}
