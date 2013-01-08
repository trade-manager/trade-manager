/* ===========================================================
 * TradeManager : a application to trade strategies for the Java(tm) platform
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

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ObjectUtilities;
import org.trade.strategy.data.volume.IVolumeDataset;
import org.trade.strategy.data.volume.VolumeItem;
import org.trade.ui.chart.renderer.VolumeBarRenderer;

/**
 */
public class VolumeDataset extends AbstractIntervalXYDataset implements
		IVolumeDataset, IndicatorDataset, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3931818830267435673L;

	/** Storage for the data series. */
	private List<IndicatorSeries> data;

	private TimePeriodAnchor xPosition = TimePeriodAnchor.START;

	public VolumeDataset() {
		this.data = new ArrayList<IndicatorSeries>();
	}

	/**
	 * Returns the position within each time period that is used for the X value
	 * when the collection is used as an {@link XYDataset}.
	 * 
	 * 
	 * 
	 * @since 1.0.11
	 * @return The anchor position (never <code>null</code>).
	 */
	public TimePeriodAnchor getXPosition() {
		return this.xPosition;
	}

	/**
	 * Sets the position within each time period that is used for the X values
	 * when the collection is used as an {@link XYDataset}, then sends a
	 * {@link DatasetChangeEvent} is sent to all registered listeners.
	 * 
	 * @param anchor
	 *            the anchor position (<code>null</code> not permitted).
	 * 
	 * @since 1.0.11
	 */
	public void setXPosition(TimePeriodAnchor anchor) {
		if (anchor == null) {
			throw new IllegalArgumentException("Null 'anchor' argument.");
		}
		this.xPosition = anchor;
		notifyListeners(new DatasetChangeEvent(this, this));
	}

	/**
	 * Adds a series to the collection and sends a {@link DatasetChangeEvent} to
	 * all registered listeners.
	 * 
	 * @param series
	 *            the series (<code>null</code> not permitted).
	 * @see org.trade.strategy.data.IndicatorDataset#addSeries(IndicatorSeries)
	 */
	public void addSeries(IndicatorSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Null 'series' argument.");
		}
		this.data.add(series);
		series.addChangeListener(this);
		fireDatasetChanged();
	}

	/**
	 * Removes a series to the collection and sends a {@link DatasetChangeEvent}
	 * to all registered listeners.
	 * 
	 * @param series
	 *            the series (<code>null</code> not permitted).
	 * @see org.trade.strategy.data.IndicatorDataset#removeSeries(IndicatorSeries)
	 */
	public void removeSeries(IndicatorSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Null series argument.");
		}
		this.data.remove(series);
		series.removeChangeListener(this);
		fireDatasetChanged();
	}

	/**
	 * Replace a series to the collection and sends a {@link DatasetChangeEvent}
	 * to all registered listeners.
	 * 
	 * @param series
	 *            the series (<code>null</code> not permitted).
	 * @param index
	 *            int
	 * @see org.trade.strategy.data.IndicatorDataset#setSeries(int,
	 *      IndicatorSeries)
	 */
	public void setSeries(int index, IndicatorSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Null series argument.");
		}
		this.data.get(index).removeChangeListener(this);
		this.data.set(index, series);
		series.addChangeListener(this);
		fireDatasetChanged();
	}

	/**
	 * Adds a series to the collection and sends a {@link DatasetChangeEvent} to
	 * all registered listeners.
	 * 
	 */
	public void seriesUpdated() {
		fireDatasetChanged();
	}

	/**
	 * Returns the number of series in the collection.
	 * 
	 * 
	 * @return The series count. * @see
	 *         org.jfree.data.general.SeriesDataset#getSeriesCount()
	 */
	public int getSeriesCount() {
		return this.data.size();
	}

	/**
	 * Returns a series from the collection.
	 * 
	 * @param series
	 *            the series index (zero-based).
	 * 
	 * 
	 * 
	 * 
	 * @return The series. * @throws IllegalArgumentException if
	 *         <code>series</code> is not in the range <code>0</code> to
	 *         <code>getSeriesCount() - 1</code>. * @see
	 *         org.trade.strategy.data.IndicatorDataset#getSeries(int)
	 */
	public VolumeSeries getSeries(int series) {
		if ((series < 0) || (series >= getSeriesCount())) {
			throw new IllegalArgumentException("Series index out of bounds");
		}
		return (VolumeSeries) this.data.get(series);
	}

	/**
	 * Returns the key for a series.
	 * 
	 * @param series
	 *            the series index (in the range <code>0</code> to
	 *            <code>getSeriesCount() - 1</code>).
	 * 
	 * 
	 * 
	 * 
	 * @return The key for a series. * @throws IllegalArgumentException if
	 *         <code>series</code> is not in the specified range. * @see
	 *         org.jfree.data.general.SeriesDataset#getSeriesKey(int)
	 */
	public Comparable<?> getSeriesKey(int series) {
		// defer argument checking
		return getSeries(series).getKey();
	}

	/**
	 * Returns the number of items in the specified series.
	 * 
	 * @param series
	 *            the series (zero-based index).
	 * 
	 * 
	 * 
	 * 
	 * @return The item count. * @throws IllegalArgumentException if
	 *         <code>series</code> is not in the range <code>0</code> to
	 *         <code>getSeriesCount() - 1</code>. * @see
	 *         org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	public int getItemCount(int series) {
		// defer argument checking
		return getSeries(series).getItemCount();
	}

	/**
	 * Method getStartX.
	 * 
	 * @param series
	 *            int
	 * @param item
	 *            int
	 * @return Number
	 * @see org.jfree.data.xy.IntervalXYDataset#getStartX(int, int)
	 */
	public Number getStartX(int series, int item) {
		return getX(series, item).doubleValue()
				- getSeries(series).getBarWidthInMilliseconds() / 2d;
	}

	/**
	 * Method getEndX.
	 * 
	 * @param series
	 *            int
	 * @param item
	 *            int
	 * @return Number
	 * @see org.jfree.data.xy.IntervalXYDataset#getEndX(int, int)
	 */
	public Number getEndX(int series, int item) {
		return getX(series, item).doubleValue()
				+ getSeries(series).getBarWidthInMilliseconds() / 2d;
	}

	/**
	 * Method getStartY.
	 * 
	 * @param series
	 *            int
	 * @param item
	 *            int
	 * @return Number
	 * @see org.jfree.data.xy.IntervalXYDataset#getStartY(int, int)
	 */
	public Number getStartY(int series, int item) {
		return new Double(0.0);
	}

	/**
	 * Method getEndY.
	 * 
	 * @param series
	 *            int
	 * @param item
	 *            int
	 * @return Number
	 * @see org.jfree.data.xy.IntervalXYDataset#getEndY(int, int)
	 */
	public Number getEndY(int series, int item) {
		return getVolume(series, item);
	}

	/**
	 * Returns the x-value for a time period.
	 * 
	 * @param period
	 *            the time period (<code>null</code> not permitted).
	 * 
	 * 
	 * @return The x-value.
	 */
	protected synchronized long getX(RegularTimePeriod period) {
		long result = 0L;
		if (this.xPosition == TimePeriodAnchor.START) {
			result = period.getFirstMillisecond();
		} else if (this.xPosition == TimePeriodAnchor.MIDDLE) {
			result = period.getMiddleMillisecond();
		} else if (this.xPosition == TimePeriodAnchor.END) {
			result = period.getLastMillisecond();
		}
		return result;
	}

	/**
	 * Returns the x-value for an item within a series.
	 * 
	 * @param series
	 *            the series index.
	 * @param item
	 *            the item index.
	 * 
	 * 
	 * @return The x-value. * @see org.jfree.data.xy.XYDataset#getXValue(int,
	 *         int)
	 */
	public double getXValue(int series, int item) {
		VolumeSeries s = (VolumeSeries) this.data.get(series);
		VolumeItem di = (VolumeItem) s.getDataItem(item);
		RegularTimePeriod period = di.getPeriod();
		return getX(period);
	}

	/**
	 * Returns the x-value for an item within a series.
	 * 
	 * @param series
	 *            the series index.
	 * @param item
	 *            the item index.
	 * 
	 * 
	 * @return The x-value. * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int series, int item) {
		return new Double(getXValue(series, item));
	}

	/**
	 * Returns the y-value for an item within a series.
	 * 
	 * @param series
	 *            the series index.
	 * @param item
	 *            the item index.
	 * 
	 * 
	 * @return The y-value. * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int series, int item) {
		VolumeSeries s = (VolumeSeries) this.data.get(series);
		VolumeItem di = (VolumeItem) s.getDataItem(item);
		return new Double(di.getY());
	}

	/**
	 * Returns the Pivot for an item within a series.
	 * 
	 * @param series
	 *            the series index.
	 * @param item
	 *            the item index.
	 * 
	 * 
	 * @return The Pivot. * @see
	 *         org.trade.strategy.data.volume.IVolumeDataset#getVolumeValue(int,
	 *         int)
	 */

	public double getVolumeValue(int series, int item) {
		VolumeSeries s = (VolumeSeries) this.data.get(series);
		VolumeItem di = (VolumeItem) s.getDataItem(item);
		return di.getVolume();
	}

	/**
	 * Returns the Pivot for an item within a series.
	 * 
	 * @param series
	 *            the series index.
	 * @param item
	 *            the item index.
	 * 
	 * 
	 * @return The Pivot. * @see
	 *         org.trade.strategy.data.volume.IVolumeDataset#getVolume(int, int)
	 */
	public Number getVolume(int series, int item) {
		return new Double(getVolumeValue(series, item));
	}

	/**
	 * Tests this instance for equality with an arbitrary object.
	 * 
	 * @param obj
	 *            the object (<code>null</code> permitted).
	 * 
	 * 
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof VolumeDataset)) {
			return false;
		}
		VolumeDataset that = (VolumeDataset) obj;
		if (!this.xPosition.equals(that.xPosition)) {
			return false;
		}
		return ObjectUtilities.equal(this.data, that.data);
	}

	/**
	 * Returns a clone of this instance.
	 * 
	 * 
	 * 
	 * 
	 * @return A clone. * @throws CloneNotSupportedException if there is a
	 *         problem.
	 */
	@SuppressWarnings("unchecked")
	public Object clone() throws CloneNotSupportedException {
		VolumeDataset clone = (VolumeDataset) super.clone();
		clone.data = (List<IndicatorSeries>) ObjectUtilities
				.deepClone(this.data);
		return clone;
	}

	/**
	 * Method updateDataset.
	 * 
	 * @param source
	 *            CandleDataset
	 * @param seriesIndex
	 *            int
	 * @see org.trade.strategy.data.IndicatorDataset#updateDataset(CandleDataset,
	 *      int)
	 */
	public void updateDataset(CandleDataset source, int seriesIndex) {

		if (source == null) {
			throw new IllegalArgumentException("Null source (CandleDataset).");
		}

		for (int i = 0; i < this.getSeriesCount(); i++) {
			VolumeSeries series = this.getSeries(i);
			series.updateSeries(source.getSeries(seriesIndex), source
					.getSeries(seriesIndex).getItemCount() - 1);
		}
	}

	/**
	 * Method clear.
	 * 
	 * @see org.trade.strategy.data.IndicatorDataset#clear()
	 */
	public void clear() {
		for (int i = 0; i < this.getSeriesCount(); i++) {
			this.getSeries(i).clear();
		}
	}

	/**
	 * Method getRenderer.
	 * 
	 * @return XYItemRenderer
	 * @see org.trade.strategy.data.IndicatorDataset#getRenderer()
	 */
	public XYItemRenderer getRenderer() {
		VolumeBarRenderer volumeRenderer = new VolumeBarRenderer();
		volumeRenderer.setBarPainter(new StandardXYBarPainter());
		volumeRenderer.setShadowVisible(false);
		volumeRenderer.setDrawBarOutline(true);
		volumeRenderer.setMargin(0.25);
		return volumeRenderer;
	}

	/**
	 * Method getSeriesColor.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return Color
	 * @see org.trade.strategy.data.IndicatorDataset#getSeriesColor(int)
	 */
	public Color getSeriesColor(int seriesIndex) {
		return this.getSeries(seriesIndex).getSeriesColor();
	}

	/**
	 * Method getDisplaySeries.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return boolean
	 * @see org.trade.strategy.data.IndicatorDataset#getDisplaySeries(int)
	 */
	public boolean getDisplaySeries(int seriesIndex) {
		return this.getSeries(seriesIndex).getDisplaySeries();
	}

	/**
	 * Method getSubChart.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return boolean
	 * @see org.trade.strategy.data.IndicatorDataset#getSubChart(int)
	 */
	public boolean getSubChart(int seriesIndex) {
		return this.getSeries(seriesIndex).getSubChart();
	}

	/**
	 * Method getType.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return String
	 * @see org.trade.strategy.data.IndicatorDataset#getType(int)
	 */
	public String getType(int seriesIndex) {
		return this.data.get(seriesIndex).getType();
	}
}
