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
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.trade.core.valuetype.Money;
import org.trade.strategy.data.HeikinAshiDataset;

/**
 */
public class HeikinAshiRenderer extends CandlestickRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6761905072507196822L;

	final DateFormat TOOLTIP_DATE_FORMAT = new SimpleDateFormat(
			"H:mma MM/dd/yy");
	public boolean nightMode = false;

	Paint upPaint;
	Paint downPaint;

	/**
	 * Constructor for HeikinAshiRenderer.
	 * 
	 * @param nightMode
	 *            boolean
	 */
	public HeikinAshiRenderer(boolean nightMode) {
		this.nightMode = nightMode;
		configureToolTips();
		if (nightMode) {
			upPaint = Color.green;
			downPaint = Color.red;
		} else {
			upPaint = Color.green;
			downPaint = Color.red;
		}
	}

	private void configureToolTips() {
		setBaseToolTipGenerator(new XYToolTipGenerator() {
			public String generateToolTip(XYDataset dataset, int series,
					int item) {
				StringBuilder result = new StringBuilder("<html>");
				if (dataset instanceof HeikinAshiDataset) {
					HeikinAshiDataset d = (HeikinAshiDataset) dataset;
					Number time = d.getX(series, item);
					Number high = d.getHigh(series, item);
					Number low = d.getLow(series, item);
					Number open = d.getOpen(series, item);
					Number close = d.getClose(series, item);

					result.append("<b>Open:</b> ")
							.append(new Money(open.doubleValue()))
							.append("<br/>");
					result.append("<b>High:</b> ")
							.append(new Money(high.doubleValue()))
							.append("<br/>");
					result.append("<b>Low:</b> ")
							.append(new Money(low.doubleValue()))
							.append("<br/>");
					result.append("<b>Close:</b> ")
							.append(new Money(close.doubleValue()))
							.append("<br/>");
					result.append("<b>Date:</b> ")
							.append(TOOLTIP_DATE_FORMAT.format(time))
							.append("<br/>");
				} else {

				}
				return result.toString();
			}

		});
	}

	/** Temporary storage for the maximum candle width. */
	private double maxCandleWidth;

	/**
	 * Method drawItem.
	 * 
	 * @param g2
	 *            Graphics2D
	 * @param state
	 *            XYItemRendererState
	 * @param dataArea
	 *            Rectangle2D
	 * @param info
	 *            PlotRenderingInfo
	 * @param plot
	 *            XYPlot
	 * @param domainAxis
	 *            ValueAxis
	 * @param rangeAxis
	 *            ValueAxis
	 * @param dataset
	 *            XYDataset
	 * @param series
	 *            int
	 * @param item
	 *            int
	 * @param crosshairState
	 *            CrosshairState
	 * @param pass
	 *            int
	 * @see org.jfree.chart.renderer.xy.XYItemRenderer#drawItem(Graphics2D,
	 *      XYItemRendererState, Rectangle2D, PlotRenderingInfo, XYPlot,
	 *      ValueAxis, ValueAxis, XYDataset, int, int, CrosshairState, int)
	 */
	public void drawItem(Graphics2D g2, XYItemRendererState state,
			Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
			ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
			int series, int item, CrosshairState crosshairState, int pass) {

		if (dataset instanceof HeikinAshiDataset) {

			// setup for collecting optional entity info...
			EntityCollection entities = null;
			if (info != null) {
				entities = info.getOwner().getEntityCollection();
			}

			HeikinAshiDataset highLowData = (HeikinAshiDataset) dataset;

			double x = highLowData.getXValue(series, item);
			double yHigh = highLowData.getHighValue(series, item);
			double yLow = highLowData.getLowValue(series, item);
			double yOpen = highLowData.getOpenValue(series, item);
			double yClose = highLowData.getCloseValue(series, item);

			RectangleEdge domainEdge = plot.getDomainAxisEdge();
			double xx = domainAxis.valueToJava2D(x, dataArea, domainEdge);

			RectangleEdge edge = plot.getRangeAxisEdge();
			double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea, edge);
			double yyLow = rangeAxis.valueToJava2D(yLow, dataArea, edge);
			double yyOpen = rangeAxis.valueToJava2D(yOpen, dataArea, edge);
			double yyClose = rangeAxis.valueToJava2D(yClose, dataArea, edge);

			int itemCount = highLowData.getItemCount(series);
			double xxWidth = dataArea.getWidth() / itemCount;

			xxWidth -= 2 * this.getAutoWidthGap();
			xxWidth *= this.getAutoWidthFactor();
			xxWidth = Math.min(xxWidth, this.maxCandleWidth);
			double stickWidth = Math.max(Math.min(3, this.maxCandleWidth),
					xxWidth);

			Paint outlinePaint = null;
			outlinePaint = getItemOutlinePaint(series, item);
			g2.setStroke(getItemStroke(series, item));
			g2.setPaint(outlinePaint);

			double yyMaxOpenClose = Math.max(yyOpen, yyClose);
			double yyMinOpenClose = Math.min(yyOpen, yyClose);
			double maxOpenClose = Math.max(yOpen, yClose);
			double minOpenClose = Math.min(yOpen, yClose);

			Shape body = null;
			boolean highlight = highlight(series, item);
			/**********************************
			 * draw the upper shadow START
			 **********************************/

			if (yHigh > maxOpenClose) {
				if (highlight) {
					body = new Rectangle2D.Double(xx - (stickWidth / 2),
							yyHigh - 10, stickWidth,
							(yyMaxOpenClose - yyHigh) + 10);
					g2.setPaint(Color.YELLOW);
					g2.fill(body);
					g2.draw(body);
				}
			}

			if (yHigh > maxOpenClose) {
				if (nightMode) {
					if (yClose > yOpen) {
						g2.setPaint(upPaint);
					} else {
						g2.setPaint(downPaint);
					}
				} else {
					g2.setPaint(Color.black);
				}

				g2.draw(new Line2D.Double(xx, yyHigh, xx, yyMaxOpenClose));
			}

			/**********************************
			 * draw the lower shadow START
			 **********************************/
			if (yLow < minOpenClose) {
				if (highlight) {
					body = new Rectangle2D.Double(xx - (stickWidth / 2),
							yyMinOpenClose, stickWidth,
							(yyLow - yyMinOpenClose) + 10);
					g2.setPaint(Color.YELLOW);
					g2.fill(body);
					g2.draw(body);
				}
				if (yLow < minOpenClose) {
					if (nightMode) {
						if (yClose > yOpen) {
							g2.setPaint(upPaint);
						} else {
							g2.setPaint(downPaint);
						}
					} else {
						g2.setPaint(Color.BLACK);
					}
					g2.draw(new Line2D.Double(xx, yyLow, xx, yyMinOpenClose));
				}
			}

			/**********************************
			 * draw the body
			 **********************************/

			body = new Rectangle2D.Double(xx - (stickWidth / 2),
					yyMinOpenClose, stickWidth, yyMaxOpenClose - yyMinOpenClose);

			if (nightMode) {
				g2.setPaint(Color.white);
			} else {
				if (yClose > yOpen) {
					g2.setPaint(upPaint);
				} else {
					g2.setPaint(downPaint);
				}
			}

			g2.fill(body);
			g2.draw(body);

			if (nightMode) {
				if (yClose > yOpen) {
					g2.setPaint(upPaint);
				} else {
					g2.setPaint(downPaint);
				}
			} else {
				g2.setPaint(outlinePaint);
			}
			g2.draw(body);
			// add an entity for the item...
			if (entities != null) {
				String tip = null;
				XYToolTipGenerator generator = getToolTipGenerator(series, item);
				if (generator != null) {
					tip = generator.generateToolTip(dataset, series, item);
				}

				XYItemEntity entity = new XYItemEntity(body, dataset, series,
						item, tip, null);

				entities.add(entity);
			}
		}
	}

	/**
	 * Method initialise.
	 * 
	 * @param g2
	 *            Graphics2D
	 * @param dataArea
	 *            Rectangle2D
	 * @param plot
	 *            XYPlot
	 * @param dataset
	 *            XYDataset
	 * @param info
	 *            PlotRenderingInfo
	 * @return XYItemRendererState
	 * @see org.jfree.chart.renderer.xy.XYItemRenderer#initialise(Graphics2D,
	 *      Rectangle2D, XYPlot, XYDataset, PlotRenderingInfo)
	 */
	public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
			XYPlot plot, XYDataset dataset, PlotRenderingInfo info) {

		ValueAxis axis = plot.getDomainAxis();
		double x1 = axis.getLowerBound();
		double x2 = x1 + getMaxCandleWidthInMilliseconds();
		RectangleEdge edge = plot.getDomainAxisEdge();
		double xx1 = axis.valueToJava2D(x1, dataArea, edge);
		double xx2 = axis.valueToJava2D(x2, dataArea, edge);
		maxCandleWidth = Math.abs(xx2 - xx1);
		return new XYItemRendererState(info);
	}

	/**
	 * Method highlight.
	 * 
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @return boolean
	 */
	public boolean highlight(int row, int column) {
		XYPlot plot = getPlot();
		OHLCDataset highLowData = (OHLCDataset) plot.getDataset();
		int total_elements = highLowData.getItemCount(0);
		boolean isLast = column == (total_elements - 1);
		if (isLast) {
			return true;
		}
		return false;
	}

}
