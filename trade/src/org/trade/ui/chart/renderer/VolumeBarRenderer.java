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

package org.trade.ui.chart.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.trade.core.valuetype.Quantity;
import org.trade.strategy.data.VolumeDataset;
import org.trade.strategy.data.volume.VolumeItem;

/**
 */
public class VolumeBarRenderer extends XYBarRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6586658399843459145L;
	/** The colors. */
	private Paint color = null;
	private final DateFormat TOOLTIP_DATE_FORMAT = new SimpleDateFormat(
			"H:mma MM/dd/yy");

	/**
	 * Creates a new renderer.
	 * 
	 */
	public VolumeBarRenderer() {
		configureToolTips();
	}

	private void configureToolTips() {
		setBaseToolTipGenerator(new XYToolTipGenerator() {
			public String generateToolTip(XYDataset dataset, int series,
					int item) {
				StringBuilder result = new StringBuilder("<html>");
				if (dataset instanceof VolumeDataset) {
					VolumeDataset d = (VolumeDataset) dataset;
					Number time = d.getX(series, item);
					Number volume = d.getVolume(series, item);
					result.append("<b>Volume:</b> ")
							.append(new Quantity(volume.intValue()))
							.append("<br/>");
					result.append("<b>Date:</b> ")
							.append(TOOLTIP_DATE_FORMAT.format(time))
							.append("<br/>");
				}
				return result.toString();
			}
		});
	}

	/**
	 * Returns the paint for an item. Overrides the default behaviour inherited
	 * from AbstractSeriesRenderer.
	 * 
	 * @param row
	 *            the series.
	 * @param column
	 *            the category.
	 * 
	 * 
	 * @return The item color. * @see
	 *         org.jfree.chart.renderer.xy.XYItemRenderer#getItemPaint(int, int)
	 */
	public Paint getItemPaint(final int row, final int column) {
		return this.color;
	}

	/**
	 * Draws the visual representation of a single data item.
	 * 
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the area within which the plot is being drawn.
	 * @param info
	 *            collects information about the drawing.
	 * @param plot
	 *            the plot (can be used to obtain standard color information
	 *            etc).
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param dataset
	 *            the dataset.
	 * @param series
	 *            the series index (zero-based).
	 * @param item
	 *            the item index (zero-based).
	 * @param crosshairState
	 *            crosshair information for the plot (<code>null</code>
	 *            permitted).
	 * @param pass
	 *            the pass index.
	 * @see org.jfree.chart.renderer.xy.XYItemRenderer#drawItem(Graphics2D,
	 *      XYItemRendererState, Rectangle2D, PlotRenderingInfo, XYPlot,
	 *      ValueAxis, ValueAxis, XYDataset, int, int, CrosshairState, int)
	 */
	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
			int series, int item, CrosshairState crosshairState, int pass) {

		if (!getItemVisible(series, item)) {
			return;
		}

		VolumeDataset volumeDataset = (VolumeDataset) dataset;
		VolumeItem volumeItem = (VolumeItem) volumeDataset.getSeries(series)
				.getDataItem(item);

		if (volumeItem.isSide()) {
			this.color = Color.GREEN;
		} else {
			this.color = Color.RED;
		}

		double value0;
		double value1;
		if (this.getUseYInterval()) {
			value0 = volumeDataset.getStartYValue(series, item);
			value1 = volumeDataset.getEndYValue(series, item);
		} else {
			value0 = this.getBase();
			value1 = volumeDataset.getYValue(series, item);
		}
		if (Double.isNaN(value0) || Double.isNaN(value1)) {
			return;
		}
		if (value0 <= value1) {
			if (!rangeAxis.getRange().intersects(value0, value1)) {
				return;
			}
		} else {
			if (!rangeAxis.getRange().intersects(value1, value0)) {
				return;
			}
		}

		double translatedValue0 = rangeAxis.valueToJava2D(value0, dataArea,
				plot.getRangeAxisEdge());
		double translatedValue1 = rangeAxis.valueToJava2D(value1, dataArea,
				plot.getRangeAxisEdge());
		double bottom = Math.min(translatedValue0, translatedValue1);
		double top = Math.max(translatedValue0, translatedValue1);

		double startX = volumeItem.getPeriod().getFirstMillisecond();
		if (Double.isNaN(startX)) {
			return;
		}
		double endX = volumeItem.getPeriod().getLastMillisecond();
		if (Double.isNaN(endX)) {
			return;
		}

		if (startX <= endX) {
			if (!domainAxis.getRange().intersects(startX, endX)) {
				return;
			}
		} else {
			if (!domainAxis.getRange().intersects(endX, startX)) {
				return;
			}
		}

		// is there an alignment adjustment to be made?
		if (this.getBarAlignmentFactor() >= 0.0
				&& this.getBarAlignmentFactor() <= 1.0) {
			double x = volumeDataset.getXValue(series, item);
			double interval = endX - startX;
			startX = x - interval * this.getBarAlignmentFactor();
			endX = startX + interval;
		}

		RectangleEdge location = plot.getDomainAxisEdge();
		double translatedStartX = domainAxis.valueToJava2D(startX, dataArea,
				location);
		double translatedEndX = domainAxis.valueToJava2D(endX, dataArea,
				location);

		double translatedWidth = Math.max(1,
				Math.abs(translatedEndX - translatedStartX));

		RectangleEdge domainEdge = plot.getDomainAxisEdge();
		double xx = domainAxis.valueToJava2D(startX, dataArea, domainEdge);

		if (getMargin() > 0.0) {
			double cut = translatedWidth * getMargin();
			translatedWidth = translatedWidth - cut;
		}

		Rectangle2D bar = null;
		PlotOrientation orientation = plot.getOrientation();
		if (orientation == PlotOrientation.HORIZONTAL) {
			// clip left and right bounds to data area
			bottom = Math.max(bottom, dataArea.getMinX());
			top = Math.min(top, dataArea.getMaxX());
			bar = new Rectangle2D.Double(bottom, xx, top - bottom,
					translatedWidth);
		} else if (orientation == PlotOrientation.VERTICAL) {
			// clip top and bottom bounds to data area
			bottom = Math.max(bottom, dataArea.getMinY());
			top = Math.min(top, dataArea.getMaxY());
			bar = new Rectangle2D.Double(xx - (translatedWidth / 2), bottom,
					translatedWidth, top - bottom);
		}

		boolean positive = (value1 > 0.0);
		boolean inverted = rangeAxis.isInverted();
		RectangleEdge barBase;
		if (orientation == PlotOrientation.HORIZONTAL) {
			if (positive && inverted || !positive && !inverted) {
				barBase = RectangleEdge.RIGHT;
			} else {
				barBase = RectangleEdge.LEFT;
			}
		} else {
			if (positive && !inverted || !positive && inverted) {
				barBase = RectangleEdge.BOTTOM;
			} else {
				barBase = RectangleEdge.TOP;
			}
		}
		if (getShadowsVisible()) {
			this.getBarPainter().paintBarShadow(g2, this, series, item, bar,
					barBase, !this.getUseYInterval());
		}
		this.getBarPainter().paintBar(g2, this, series, item, bar, barBase);

		if (isItemLabelVisible(series, item)) {
			XYItemLabelGenerator generator = getItemLabelGenerator(series, item);
			drawItemLabel(g2, dataset, series, item, plot, generator, bar,
					value1 < 0.0);
		}

		// update the cross hair point
		double x1 = dataset.getXValue(series, item);
		double y1 = dataset.getYValue(series, item);
		double transX1 = domainAxis.valueToJava2D(x1, dataArea, location);
		double transY1 = rangeAxis.valueToJava2D(y1, dataArea,
				plot.getRangeAxisEdge());
		int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
		int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
		updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
				rangeAxisIndex, transX1, transY1, plot.getOrientation());

		EntityCollection entities = state.getEntityCollection();
		// add an entity for the item...
		if (entities != null) {
			String tip = null;
			XYToolTipGenerator generator = getToolTipGenerator(series, item);
			if (generator != null) {
				tip = generator.generateToolTip(dataset, series, item);
			}
			XYItemEntity entity = new XYItemEntity(bar, dataset, series, item,
					tip, null);

			entities.add(entity);
		}
	}
}
