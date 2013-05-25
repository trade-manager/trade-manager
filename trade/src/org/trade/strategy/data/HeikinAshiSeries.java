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
		for (int i = 0; i < source.getSeries(seriesIndex).getItemCount(); i++) {
			this.updateSeries(source.getSeries(seriesIndex), i, true);
		}
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
	 * Method updateSeries. Heikin Ashi charts calculate their own open (HAO),
	 * high (HAH), low (HAL), and close (HAC), using the actual open (O), high
	 * (H), low (L), and close (C), of the time frame (e.g. the open, high, low,
	 * and close, of each five minutes).
	 * 
	 * HAO = (HAO-1 + HAC-1) / 2
	 * 
	 * HAC = (O + H + L + C) / 4
	 * 
	 * HAH = Highest(H, HAO, HAC)
	 * 
	 * HAL = Lowest(L, HAO, HAC)
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
			if (source.getItemCount() > 1) {
				/*
				 * Get the prev candle the new candle may be just forming or
				 * completed if back testing. Hiekin-Ashi bats must be formed
				 * from completed bars.
				 */
				int index = this.indexOf(source.getRollingCandle().getPeriod());
				double xOpenPrev = 0;
				double xClosePrev = 0;
				if (index < 0) {
					if (this.isEmpty()) {
						xOpenPrev = source.getRollingCandle().getOpen();
						xClosePrev = source.getRollingCandle().getClose();
					} else {
						HeikinAshiItem prevItem = (HeikinAshiItem) this
								.getDataItem(this.getItemCount() - 1);
						xClosePrev = prevItem.getClose();
						xOpenPrev = prevItem.getOpen();
					}
				} else {
					HeikinAshiItem prevItem = (HeikinAshiItem) this
							.getDataItem(this.getItemCount() - 2);
					xClosePrev = prevItem.getClose();
					xOpenPrev = prevItem.getOpen();
				}

				double xClose = (source.getRollingCandle().getOpen()
						+ source.getRollingCandle().getHigh()
						+ source.getRollingCandle().getLow() + source
						.getRollingCandle().getClose()) / 4;

				double xOpen = (xOpenPrev + xClosePrev) / 2;

				double xHigh = Math.max(source.getRollingCandle().getHigh(),
						Math.max(xClosePrev, xOpenPrev));

				double xLow = Math.min(source.getRollingCandle().getLow(),
						Math.min(xClosePrev, xOpenPrev));

				if (index < 0) {
					this.add(new HeikinAshiItem(source.getContract(), source
							.getRollingCandle().getPeriod(), xOpen, xHigh,
							xLow, xClose, source.getRollingCandle()
									.getLastUpdateDate()), false);
				} else {
					HeikinAshiItem currDataItem = (HeikinAshiItem) this
							.getDataItem(index);
					currDataItem.setOpen(xOpen);
					currDataItem.setHigh(xHigh);
					currDataItem.setLow(xLow);
					currDataItem.setClose(xClose);
					currDataItem.setLastUpdateDate(source.getRollingCandle()
							.getLastUpdateDate());
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
			HeikinAshiItem dataItem = (HeikinAshiItem) this.getDataItem(i);
			_log.info("Type: " + this.getType() + " Time: "
					+ dataItem.getPeriod().getStart() + " Open: "
					+ dataItem.getOpen() + " Close: " + dataItem.getClose()
					+ " High: " + dataItem.getHigh() + " Low: "
					+ dataItem.getLow());
		}
	}
}
