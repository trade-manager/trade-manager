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

import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * An interface that defines data in the form of (x, high, low, open, close)
 * tuples. This interface also defines how the data-set should be plotted.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public interface IndicatorDataset {

	public static final String PACKAGE = "org.trade.strategy.data.";

	/**
	 * Method updateDataset.
	 * 
	 * @param source
	 *            CandleDataset
	 * @param seriesIndex
	 *            int
	 */
	void updateDataset(CandleDataset source, int seriesIndex);

	void clear();

	/**
	 * Method getRenderer.
	 * 
	 * @return XYItemRenderer
	 */
	XYItemRenderer getRenderer();

	/**
	 * Method getSeriesColor.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return Color
	 */
	Color getSeriesColor(int seriesIndex);

	/**
	 * Method getDisplaySeries.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return boolean
	 */
	boolean getDisplaySeries(int seriesIndex);

	/**
	 * Method getSubChart.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return boolean
	 */
	boolean getSubChart(int seriesIndex);

	/**
	 * Method getType.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return String
	 */
	String getType(int seriesIndex);

	/**
	 * Method getSeries.
	 * 
	 * @param seriesIndex
	 *            int
	 * @return IndicatorSeries
	 */
	IndicatorSeries getSeries(int seriesIndex);

	/**
	 * Method addSeries.
	 * 
	 * @param series
	 *            IndicatorSeries
	 */
	void addSeries(IndicatorSeries series);

	/**
	 * Method removeSeries.
	 * 
	 * @param series
	 *            IndicatorSeries
	 */
	void removeSeries(IndicatorSeries series);

	/**
	 * Method setSeries.
	 * 
	 * @param index
	 *            int
	 * @param series
	 *            IndicatorSeries
	 */
	void setSeries(int index, IndicatorSeries series);

}
