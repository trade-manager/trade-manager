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

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.heikinashi.HeikinAshiItem;

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
@DiscriminatorValue("HeikinAshiSeries")
public class HeikinAshiSeries extends IndicatorSeries {

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

	public HeikinAshiSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);

	}

	public HeikinAshiSeries() {
		super(IndicatorSeries.HeikinAshiSeries);
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
		final HeikinAshiItem item = (HeikinAshiItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * 
	 * 
	 * @param contract
	 *            Contract
	 * @param open
	 *            double
	 * @param high
	 *            double
	 * @param low
	 *            double
	 * @param close
	 *            double
	 * @param lastUpdateDate
	 *            Date
	 */
	public void add(Contract contract, RegularTimePeriod period, double open,
			double high, double low, double close, Date lastUpdateDate) {
		if (!this.isEmpty()) {
			HeikinAshiItem item0 = (HeikinAshiItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new HeikinAshiItem(contract, period, open, high, low, close,
				lastUpdateDate), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            HeikinAshiItem
	 */
	public void add(HeikinAshiItem dataItem, boolean notify) {
		if (!this.isEmpty()) {
			HeikinAshiItem item0 = (HeikinAshiItem) this.getDataItem(0);
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
			HeikinAshiItem item = (HeikinAshiItem) this.data.get(i - 1);
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
	 * @param source
	 *            CandleDataset
	 * @param seriesIndex
	 *            int
	 */
	public void createSeries(CandleDataset source, int seriesIndex) {

		if (source.getSeries(seriesIndex) == null) {
			throw new IllegalArgumentException("Null source (CandleDataset).");
		}

		this.updateSeries(source.getSeries(seriesIndex), 0, true);
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		HeikinAshiSeries clone = (HeikinAshiSeries) super.clone();
		return clone;
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
		if (source.getItemCount() < 2) {
			return;
		}

		HeikinAshiItem dataItem = null;
		HeikinAshiItem prevHeikinAshiCandleItem = null;

		if (!this.isEmpty()) {
			prevHeikinAshiCandleItem = (HeikinAshiItem) this.getDataItem(this
					.getItemCount() - 1);
		}

		for (int i = skip; i < source.getItemCount(); i++) {
			if (i >= 0) {
				CandleItem candleItem = (CandleItem) source.getDataItem(i);

				double xClose = (candleItem.getOpen() + candleItem.getHigh()
						+ candleItem.getLow() + candleItem.getClose()) / 4;

				double xOpenPrev = 0;
				double xClosePrev = 0;
				if (null == prevHeikinAshiCandleItem) {
					if (xOpenPrev == 0) {
						CandleItem prevCandleItem = (CandleItem) source
								.getDataItem(i);
						xOpenPrev = prevCandleItem.getOpen();
						xClosePrev = prevCandleItem.getClose();
					}
				} else {
					xClosePrev = prevHeikinAshiCandleItem.getClose();
					xOpenPrev = prevHeikinAshiCandleItem.getOpen();
				}
				double xOpen = (xOpenPrev + xClosePrev) / 2;

				double xHigh = xClose;
				if (xHigh < xOpen) {
					xHigh = xOpen;
				}
				if (xHigh < candleItem.getHigh()) {
					xHigh = candleItem.getHigh();
				}

				double xLow = xClose;
				if (xLow > xOpen) {
					xLow = xOpen;
				}
				if (xLow > candleItem.getLow()) {
					xLow = candleItem.getLow();
				}

				dataItem = new HeikinAshiItem(source.getContract(),
						candleItem.getPeriod(), xOpen, xHigh, xLow, xClose,
						candleItem.getLastUpdateDate());
				int index = this.indexOf(dataItem.getPeriod());
				if (index < 0) {
					this.add(dataItem, false);
				} else {
					HeikinAshiItem currDataItem = (HeikinAshiItem) this
							.getDataItem(index);
					currDataItem.setOpen(dataItem.getOpen());
					currDataItem.setHigh(dataItem.getHigh());
					currDataItem.setLow(dataItem.getLow());
					currDataItem.setClose(dataItem.getClose());
				}
				prevHeikinAshiCandleItem = dataItem;
			}
		}
	}
}
