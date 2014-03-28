/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2013, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ---------------------------
 * StandardXYItemRenderer.java
 * ---------------------------
 * (C) Copyright 2001-2013, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Mark Watson (www.markwatson.com);
 *                   Jonathan Nash;
 *                   Andreas Schneider;
 *                   Norbert Kiesel (for TBD Networks);
 *                   Christian W. Zuckschwerdt;
 *                   Bill Kelemen;
 *                   Nicolas Brodu (for Astrium and EADS Corporate Research
 *                   Center);
 *
 * Changes:
 * --------
 * 19-Oct-2001 : Version 1, based on code by Mark Watson (DG);
 * 22-Oct-2001 : Renamed DataSource.java --> Dataset.java etc. (DG);
 * 21-Dec-2001 : Added working line instance to improve performance (DG);
 * 22-Jan-2002 : Added code to lock crosshairs to data points.  Based on code
 *               by Jonathan Nash (DG);
 * 23-Jan-2002 : Added DrawInfo parameter to drawItem() method (DG);
 * 28-Mar-2002 : Added a property change listener mechanism so that the
 *               renderer no longer needs to be immutable (DG);
 * 02-Apr-2002 : Modified to handle null values (DG);
 * 09-Apr-2002 : Modified draw method to return void.  Removed the translated
 *               zero from the drawItem method.  Override the initialise()
 *               method to calculate it (DG);
 * 13-May-2002 : Added code from Andreas Schneider to allow changing
 *               shapes/colors per item (DG);
 * 24-May-2002 : Incorporated tooltips into chart entities (DG);
 * 25-Jun-2002 : Removed redundant code (DG);
 * 05-Aug-2002 : Incorporated URLs for HTML image maps into chart entities (RA);
 * 08-Aug-2002 : Added discontinuous lines option contributed by
 *               Norbert Kiesel (DG);
 * 20-Aug-2002 : Added user definable default values to be returned by
 *               protected methods unless overridden by a subclass (DG);
 * 23-Sep-2002 : Updated for changes in the XYItemRenderer interface (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem() method signature (DG);
 * 15-May-2003 : Modified to take into account the plot orientation (DG);
 * 29-Jul-2003 : Amended code that doesn't compile with JDK 1.2.2 (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 24-Aug-2003 : Added null/NaN checks in drawItem (BK);
 * 08-Sep-2003 : Fixed serialization (NB);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 21-Jan-2004 : Override for getLegendItem() method (DG);
 * 27-Jan-2004 : Moved working line into state object (DG);
 * 10-Feb-2004 : Changed drawItem() method to make cut-and-paste overriding
 *               easier (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState.  Renamed
 *               XYToolTipGenerator --> XYItemLabelGenerator (DG);
 * 08-Jun-2004 : Modified to use getX() and getY() methods (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 25-Aug-2004 : Created addEntity() method in superclass (DG);
 * 08-Oct-2004 : Added 'gapThresholdType' as suggested by Mike Watts (DG);
 * 11-Nov-2004 : Now uses ShapeUtilities to translate shapes (DG);
 * 23-Feb-2005 : Fixed getLegendItem() method to show lines.  Fixed bug
 *               1077108 (shape not visible for first item in series) (DG);
 * 10-Apr-2005 : Fixed item label positioning with horizontal orientation (DG);
 * 20-Apr-2005 : Use generators for legend tooltips and URLs (DG);
 * 27-Apr-2005 : Use generator for series label in legend (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 15-Jun-2006 : Fixed bug (1380480) for rendering series as path (DG);
 * 06-Feb-2007 : Fixed bug 1086307, crosshairs with multiple axes (DG);
 * 14-Mar-2007 : Fixed problems with the equals() and clone() methods (DG);
 * 23-Mar-2007 : Clean-up of shapesFilled attributes (DG);
 * 20-Apr-2007 : Updated getLegendItem() and drawItem() for renderer
 *               change (DG);
 * 17-May-2007 : Set datasetIndex and seriesIndex in getLegendItem()
 *               method (DG);
 * 18-May-2007 : Set dataset and seriesKey for LegendItem (DG);
 * 08-Jun-2007 : Fixed bug in entity creation (DG);
 * 21-Nov-2007 : Deprecated override flag methods (DG);
 * 02-Jun-2008 : Fixed tooltips for data items at lower edges of data area (DG);
 * 17-Jun-2008 : Apply legend shape, font and paint attributes (DG);
 * 03-Jul-2013 : Use ParamChecks (DG);
 *
 */

package org.trade.ui.chart.renderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.UnitType;
import org.trade.strategy.data.MACDDataset;
import org.trade.strategy.data.macd.MACDItem;

/**
 * Standard item renderer for an {@link XYPlot}. This class can draw (a) shapes
 * at each point, or (b) lines between points, or (c) both shapes and lines.
 * <P>
 * This renderer has been retained for historical reasons and, in general, you
 * should use the {@link XYLineAndShapeRenderer} class instead.
 */
public class MACDItemRenderer extends StandardXYItemRenderer {

	/** For serialization. */
	private static final long serialVersionUID = -3271351259436865995L;

	/** Specifies how the gap threshold value is interpreted. */
	private UnitType gapThresholdType = UnitType.RELATIVE;

	/** Threshold for deciding when to discontinue a line. */
	private double gapThreshold = 1.0;

	/**
	 * A flag that controls whether or not each series is drawn as a single
	 * path.
	 */
	private boolean drawSeriesLineAsPath;

	/**
	 * Constructs a new renderer.
	 */
	public MACDItemRenderer() {
		super(LINES, null);
	}

	/**
	 * Constructs a new renderer. To specify the type of renderer, use one of
	 * the constants: {@link #SHAPES}, {@link #LINES} or
	 * {@link #SHAPES_AND_LINES}.
	 * 
	 * @param type
	 *            the type.
	 */
	public MACDItemRenderer(int type) {
		super(type, null);
	}

	/**
	 * Constructs a new renderer. To specify the type of renderer, use one of
	 * the constants: {@link #SHAPES}, {@link #LINES} or
	 * {@link #SHAPES_AND_LINES}.
	 * 
	 * @param type
	 *            the type of renderer.
	 * @param toolTipGenerator
	 *            the item label generator (<code>null</code> permitted).
	 */
	public MACDItemRenderer(int type, XYToolTipGenerator toolTipGenerator) {
		super(type, toolTipGenerator, null);
	}

	/**
	 * Constructs a new renderer. To specify the type of renderer, use one of
	 * the constants: {@link #SHAPES}, {@link #LINES} or
	 * {@link #SHAPES_AND_LINES}.
	 * 
	 * @param type
	 *            the type of renderer.
	 * @param toolTipGenerator
	 *            the item label generator (<code>null</code> permitted).
	 * @param urlGenerator
	 *            the URL generator.
	 */
	public MACDItemRenderer(int type, XYToolTipGenerator toolTipGenerator,
			XYURLGenerator urlGenerator) {
		super();
	}

	/**
	 * Draws the visual representation of a single data item.
	 * 
	 * @param g2
	 *            the graphics device.
	 * @param state
	 *            the renderer state.
	 * @param dataArea
	 *            the area within which the data is being drawn.
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
	 */
	@Override
	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
			int series, int item, CrosshairState crosshairState, int pass) {

		// get the data point...
		MACDDataset mACDDataset = (MACDDataset) dataset;
		MACDItem mACDItem = (MACDItem) mACDDataset.getSeries(series)
				.getDataItem(item);
		double x1 = dataset.getXValue(series, item);
		double y1 = mACDItem.getMACD();
		double x0 = 0;
		double y0 = 0;
		int lastItem = 0;
		MACDItem prevMACDItem = null;

		if (item != 0) {
			prevMACDItem = (MACDItem) mACDDataset.getSeries(series)
					.getDataItem(item - 1);
			x0 = mACDDataset.getXValue(series, item - 1);
			y0 = prevMACDItem.getMACD();
			lastItem = mACDDataset.getItemCount(series) - 1;
		}
		int numX = mACDDataset.getItemCount(series);
		double minX = mACDDataset.getXValue(series, 0);
		double maxX = mACDDataset.getXValue(series, numX - 1);
		/*
		 * Draw MACD.
		 */

		drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, x0,
				y0, x1, y1, lastItem, series, item, crosshairState, pass, numX,
				minX, maxX, mACDDataset.getSeriesColor(0), dataset);

		y1 = mACDItem.getSignalLine();
		if (item != 0) {
			y0 = prevMACDItem.getSignalLine();
		}
		/*
		 * Draw MACD smoothing.
		 */
		drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, x0,
				y0, x1, y1, lastItem, series, item, crosshairState, pass, numX,
				minX, maxX, Color.RED, dataset);
		y1 = mACDItem.getMACDHistogram();
		if (item != 0) {
			y0 = prevMACDItem.getMACDHistogram();
		}
		/*
		 * Draw MACD histogram.
		 */
		y0 = 0;
		x0 = x1;
		drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis, x0,
				y0, x1, y1, lastItem, series, item, crosshairState, pass, numX,
				minX, maxX, Color.BLACK, dataset);

	}

	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, double x0, double y0,
			double x1, double y1, int lastItem, int series, int item,
			CrosshairState crosshairState, int pass, int numX, double minX,
			double maxX, Paint color, XYDataset dataset) {

		boolean itemVisible = getItemVisible(series, item);

		// setup for collecting optional entity info...
		Shape entityArea = null;
		EntityCollection entities = null;
		if (info != null) {
			entities = info.getOwner().getEntityCollection();
		}

		PlotOrientation orientation = plot.getOrientation();
		Paint paint = getItemPaint(series, item);
		paint = color;
		Stroke seriesStroke = getItemStroke(series, item);
		g2.setPaint(paint);
		g2.setStroke(seriesStroke);

		RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
		double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

		if (getPlotLines()) {
			if (this.drawSeriesLineAsPath) {
				State s = (State) state;
				if (s.getSeriesIndex() != series) {
					// we are starting a new series path
					s.seriesPath.reset();
					s.lastPointGood = false;
					s.setSeriesIndex(series);
				}

				// update path to reflect latest point
				if (itemVisible && !Double.isNaN(transX1)
						&& !Double.isNaN(transY1)) {
					float x = (float) transX1;
					float y = (float) transY1;
					if (orientation == PlotOrientation.HORIZONTAL) {
						x = (float) transY1;
						y = (float) transX1;
					}
					if (s.isLastPointGood()) {
						// TODO: check threshold
						s.seriesPath.lineTo(x, y);
					} else {
						s.seriesPath.moveTo(x, y);
					}
					s.setLastPointGood(true);
				} else {
					s.setLastPointGood(false);
				}
				if (item == lastItem) {
					if (s.seriesIndex == series) {
						// draw path
						g2.setStroke(lookupSeriesStroke(series));
						g2.setPaint(lookupSeriesPaint(series));
						g2.draw(s.seriesPath);
					}
				}
			}

			else if (item != 0 && itemVisible) {
				// get the previous data point...

				if (!Double.isNaN(x0) && !Double.isNaN(y0)) {
					boolean drawLine = true;
					if (getPlotDiscontinuous()) {
						// only draw a line if the gap between the current and
						// previous data point is within the threshold
						if (this.gapThresholdType == UnitType.ABSOLUTE) {
							drawLine = Math.abs(x1 - x0) <= this.gapThreshold;
						} else {
							drawLine = Math.abs(x1 - x0) <= ((maxX - minX)
									/ numX * getGapThreshold());
						}
					}
					if (drawLine) {
						double transX0 = domainAxis.valueToJava2D(x0, dataArea,
								xAxisLocation);
						double transY0 = rangeAxis.valueToJava2D(y0, dataArea,
								yAxisLocation);

						// only draw if we have good values
						if (Double.isNaN(transX0) || Double.isNaN(transY0)
								|| Double.isNaN(transX1)
								|| Double.isNaN(transY1)) {
							return;
						}

						if (orientation == PlotOrientation.HORIZONTAL) {
							state.workingLine.setLine(transY0, transX0,
									transY1, transX1);
						} else if (orientation == PlotOrientation.VERTICAL) {
							state.workingLine.setLine(transX0, transY0,
									transX1, transY1);
						}

						if (state.workingLine.intersects(dataArea)) {
							g2.draw(state.workingLine);
						}
					}
				}
			}
		}

		// we needed to get this far even for invisible items, to ensure that
		// seriesPath updates happened, but now there is nothing more we need
		// to do for non-visible items...
		if (!itemVisible) {
			return;
		}

		if (getBaseShapesVisible()) {

			Shape shape = getItemShape(series, item);
			if (orientation == PlotOrientation.HORIZONTAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, transY1,
						transX1);
			} else if (orientation == PlotOrientation.VERTICAL) {
				shape = ShapeUtilities.createTranslatedShape(shape, transX1,
						transY1);
			}
			if (shape.intersects(dataArea)) {
				if (getItemShapeFilled(series, item)) {
					g2.fill(shape);
				} else {
					g2.draw(shape);
				}
			}
			entityArea = shape;

		}

		if (getPlotImages()) {
			Image image = getImage(plot, series, item, transX1, transY1);
			if (image != null) {
				Point hotspot = getImageHotspot(plot, series, item, transX1,
						transY1, image);
				g2.drawImage(image, (int) (transX1 - hotspot.getX()),
						(int) (transY1 - hotspot.getY()), null);
				entityArea = new Rectangle2D.Double(transX1 - hotspot.getX(),
						transY1 - hotspot.getY(), image.getWidth(null),
						image.getHeight(null));
			}

		}

		double xx = transX1;
		double yy = transY1;
		if (orientation == PlotOrientation.HORIZONTAL) {
			xx = transY1;
			yy = transX1;
		}

		// draw the item label if there is one...
		if (isItemLabelVisible(series, item)) {
			drawItemLabel(g2, orientation, dataset, series, item, xx, yy,
					(y1 < 0.0));
		}

		int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
		int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
		updateCrosshairValues(crosshairState, x1, y1, domainAxisIndex,
				rangeAxisIndex, transX1, transY1, orientation);

		// add an entity for the item...
		if (entities != null && isPointInRect(dataArea, xx, yy)) {
			addEntity(entities, entityArea, dataset, series, item, xx, yy);
		}
	}

	/**
	 * Records the state for the renderer. This is used to preserve state
	 * information between calls to the drawItem() method for a single chart
	 * drawing.
	 */
	public static class State extends XYItemRendererState {

		/** The path for the current series. */
		public GeneralPath seriesPath;

		/** The series index. */
		private int seriesIndex;

		/**
		 * A flag that indicates if the last (x, y) point was 'good' (non-null).
		 */
		private boolean lastPointGood;

		/**
		 * Creates a new state instance.
		 * 
		 * @param info
		 *            the plot rendering info.
		 */
		public State(PlotRenderingInfo info) {
			super(info);
		}

		/**
		 * Returns a flag that indicates if the last point drawn (in the current
		 * series) was 'good' (non-null).
		 * 
		 * @return A boolean.
		 */
		public boolean isLastPointGood() {
			return this.lastPointGood;
		}

		/**
		 * Sets a flag that indicates if the last point drawn (in the current
		 * series) was 'good' (non-null).
		 * 
		 * @param good
		 *            the flag.
		 */
		public void setLastPointGood(boolean good) {
			this.lastPointGood = good;
		}

		/**
		 * Returns the series index for the current path.
		 * 
		 * @return The series index for the current path.
		 */
		public int getSeriesIndex() {
			return this.seriesIndex;
		}

		/**
		 * Sets the series index for the current path.
		 * 
		 * @param index
		 *            the index.
		 */
		public void setSeriesIndex(int index) {
			this.seriesIndex = index;
		}
	}

}
