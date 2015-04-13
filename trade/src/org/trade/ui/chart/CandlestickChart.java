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
package org.trade.ui.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Stroke;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.axis.SegmentedTimeline.Segment;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.EmptyBlock;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.Action;
import org.trade.persistent.dao.Tradingday;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.IndicatorDataset;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.strategy.data.StrategyData;
import org.trade.strategy.data.candle.CandleItem;

/**
 */
public class CandlestickChart extends JPanel implements SeriesChangeListener {

	private static final long serialVersionUID = 2842422936659217811L;

	private JFreeChart chart = null;
	private static final String TIME_FORMAT = "HH:mm:ss";
	private static final String TIME_FORMAT_SHORT = "HH:mm";
	private final TextTitle titleLegend1 = new TextTitle(" Time: 0, Price :0.0");
	private final TextTitle titleLegend2 = new TextTitle(
			"Time:00:00 Open: 0.0 High: 0.0 Low: 0.0 Close: 0.0 Vwap: 0.0");

	private Stroke stroke = null;
	private ValueMarker valueMarker = null;
	private XYTextAnnotation closePriceLine = null;
	private XYTextAnnotation clickCrossHairs = null;
	private StrategyData strategyData = null;

	/**
	 * A demonstration application showing a candlestick chart.
	 * 
	 * @param title
	 *            the frame title.
	 * @param strategyData
	 *            StrategyData
	 */
	public CandlestickChart(final String title, StrategyData strategyData,
			Tradingday tradingday) {

		this.strategyData = strategyData;
		this.setLayout(new BorderLayout());
		// Used to mark the current price
		stroke = new BasicStroke(1, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 10, 3 }, 0);
		valueMarker = new ValueMarker(0.00, Color.black, stroke);

		this.chart = createChart(this.strategyData, title, tradingday);

		BlockContainer container = new BlockContainer(new BorderArrangement());
		container.add(titleLegend1, RectangleEdge.LEFT);
		container.add(titleLegend2, RectangleEdge.RIGHT);
		container.add(new EmptyBlock(2000, 0));
		CompositeTitle legends = new CompositeTitle(container);
		legends.setPosition(RectangleEdge.BOTTOM);
		this.chart.addSubtitle(legends);

		final ChartPanel chartPanel = new ChartPanel(this.chart);
		chartPanel.setFillZoomRectangle(true);
		chartPanel.setMouseZoomable(true, true);
		chartPanel.setRefreshBuffer(true);
		chartPanel.setDoubleBuffered(true);
		chartPanel.setVerticalAxisTrace(true);
		chartPanel.setHorizontalAxisTrace(true);
		chartPanel.addChartMouseListener(new ChartMouseListener() {

			public void chartMouseMoved(ChartMouseEvent e) {
			}

			public void chartMouseClicked(final ChartMouseEvent e) {
				CombinedDomainXYPlot combinedXYplot = (CombinedDomainXYPlot) e
						.getChart().getPlot();
				@SuppressWarnings("unchecked")
				List<XYPlot> subplots = combinedXYplot.getSubplots();
				if (e.getTrigger().getClickCount() == 2) {
					double xItem = 0;
					double yItem = 0;
					if (e.getEntity() instanceof XYItemEntity) {
						XYItemEntity xYItemEntity = ((XYItemEntity) e
								.getEntity());
						xItem = xYItemEntity.getDataset().getXValue(
								xYItemEntity.getSeriesIndex(),
								xYItemEntity.getItem());
						yItem = xYItemEntity.getDataset().getYValue(
								xYItemEntity.getSeriesIndex(),
								xYItemEntity.getItem());
					} else {
						PlotEntity plotEntity = ((PlotEntity) e.getEntity());
						XYPlot plot = (XYPlot) plotEntity.getPlot();
						xItem = plot.getDomainCrosshairValue();
						yItem = plot.getRangeCrosshairValue();
					}

					for (XYPlot xyplot : subplots) {

						double x = xyplot.getDomainCrosshairValue();
						double y = xyplot.getRangeCrosshairValue();

						/*
						 * If the cross hair is from a right-hand y axis we need
						 * to convert this to a left-hand y axis.
						 */
						String rightAxisName = ", Price: ";
						double rangeLowerLeft = 0;
						double rangeUpperLeft = 0;
						double rangeLowerRight = 0;
						double rangeUpperRight = 0;
						double yRightLocation = 0;
						for (int index = 0; index < xyplot.getRangeAxisCount(); index++) {
							AxisLocation axisLocation = xyplot
									.getRangeAxisLocation(index);
							Range range = xyplot.getRangeAxis(index).getRange();

							if (axisLocation
									.equals(AxisLocation.BOTTOM_OR_LEFT)
									|| axisLocation
											.equals(AxisLocation.TOP_OR_LEFT)) {
								rangeLowerLeft = range.getLowerBound();
								rangeUpperLeft = range.getUpperBound();
								rightAxisName = ", "
										+ xyplot.getRangeAxis(index).getLabel()
										+ ": ";
							}
							if (y >= range.getLowerBound()
									&& y <= range.getUpperBound()
									&& (axisLocation
											.equals(AxisLocation.BOTTOM_OR_RIGHT) || axisLocation
											.equals(AxisLocation.TOP_OR_RIGHT))) {
								rangeUpperRight = range.getUpperBound();
								rangeLowerRight = range.getLowerBound();
							}
						}
						if ((rangeUpperRight - rangeLowerRight) > 0) {
							yRightLocation = rangeLowerLeft
									+ ((rangeUpperLeft - rangeLowerLeft) * ((y - rangeLowerRight) / (rangeUpperRight - rangeLowerRight)));
						} else {
							yRightLocation = y;
						}

						String text = " Time: "
								+ TradingCalendar.getFormattedDate(
										TradingCalendar
												.getZonedDateTimeFromMilli((long) (x)),
										TIME_FORMAT_SHORT) + rightAxisName
								+ new Money(y);
						if (x == xItem && y == yItem) {
							titleLegend1.setText(text);
							if (null == clickCrossHairs) {
								clickCrossHairs = new XYTextAnnotation(text, x,
										yRightLocation);
								clickCrossHairs
										.setTextAnchor(TextAnchor.BOTTOM_LEFT);
								xyplot.addAnnotation(clickCrossHairs);
							} else {
								clickCrossHairs.setText(text);
								clickCrossHairs.setX(x);
								clickCrossHairs.setY(yRightLocation);
							}
						}
					}
				} else if (e.getTrigger().getClickCount() == 1
						&& null != clickCrossHairs) {
					for (XYPlot xyplot : subplots) {
						if (xyplot.removeAnnotation(clickCrossHairs)) {
							clickCrossHairs = null;
							titleLegend1.setText(" Time: 0, Price :0.0");
							break;
						}
					}
				}
			}
		});
		this.add(chartPanel, null);
		this.strategyData.getCandleDataset().getSeries(0)
				.addChangeListener(this);
	}

	public void removeChart() {
		this.strategyData.getCandleDataset().getSeries(0)
				.removeChangeListener(this);
		this.chart.getXYPlot().clearAnnotations();
		this.chart.getXYPlot().clearDomainAxes();
		this.chart.getXYPlot().clearDomainMarkers();
		this.chart.getXYPlot().clearRangeAxes();
		this.chart.getXYPlot().clearRangeMarkers();
	}

	/**
	 * Method getChart.
	 * 
	 * @return JFreeChart
	 */
	public JFreeChart getChart() {
		return this.chart;
	}

	/**
	 * Method createChart.
	 * 
	 * @param strategyData
	 *            StrategyData
	 * @param title
	 *            String
	 * @return JFreeChart
	 */
	private JFreeChart createChart(StrategyData strategyData, String title,
			Tradingday tradingday) {

		DateAxis dateAxis = new DateAxis("Date");
		dateAxis.setVerticalTickLabels(true);
		dateAxis.setDateFormatOverride(new SimpleDateFormat("dd/MM hh:mm"));
		dateAxis.setTickMarkPosition(DateTickMarkPosition.START);
		NumberAxis priceAxis = new NumberAxis("Price");
		priceAxis.setAutoRange(true);
		priceAxis.setAutoRangeIncludesZero(false);
		XYPlot pricePlot = new XYPlot(strategyData.getCandleDataset(),
				dateAxis, priceAxis, strategyData.getCandleDataset()
						.getRenderer());
		pricePlot.setOrientation(PlotOrientation.VERTICAL);
		pricePlot.setDomainPannable(true);
		pricePlot.setRangePannable(true);
		pricePlot.setDomainCrosshairVisible(true);
		pricePlot.setDomainCrosshairLockedOnData(true);
		pricePlot.setRangeCrosshairVisible(true);
		pricePlot.setRangeCrosshairLockedOnData(true);
		pricePlot.setRangeGridlinePaint(new Color(204, 204, 204));
		pricePlot.setDomainGridlinePaint(new Color(204, 204, 204));
		pricePlot.setBackgroundPaint(Color.white);

		/*
		 * Calculate the number of 15min segments in this trading day. i.e.
		 * 6.5hrs/15min = 26 and there are a total of 96 = one day
		 */
		int segments15min = (int) (TradingCalendar.getDurationInSeconds(
				tradingday.getOpen(), tradingday.getClose()) / (60 * 15));

		SegmentedTimeline segmentedTimeline = new SegmentedTimeline(
				SegmentedTimeline.FIFTEEN_MINUTE_SEGMENT_SIZE, segments15min,
				(96 - segments15min));

		ZonedDateTime startDate = tradingday.getOpen();
		ZonedDateTime endDate = tradingday.getClose();

		if (!strategyData.getCandleDataset().getSeries(0).isEmpty()) {
			startDate = ((CandleItem) strategyData.getCandleDataset()
					.getSeries(0).getDataItem(0)).getPeriod().getStart();
			startDate = TradingCalendar.getDateAtTime(startDate,
					tradingday.getOpen());
			endDate = ((CandleItem) strategyData
					.getCandleDataset()
					.getSeries(0)
					.getDataItem(
							strategyData.getCandleDataset().getSeries(0)
									.getItemCount() - 1)).getPeriod()
					.getStart();
			endDate = TradingCalendar.getDateAtTime(endDate,
					tradingday.getClose());
		}

		segmentedTimeline.setStartTime(TradingCalendar
				.geMillisFromZonedDateTime(startDate));
		segmentedTimeline.addExceptions(getNonTradingPeriods(startDate,
				endDate, tradingday.getOpen(), tradingday.getClose(),
				segmentedTimeline));
		dateAxis.setTimeline(segmentedTimeline);

		// Build Combined Plot
		CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
		mainPlot.add(pricePlot, 4);

		int axixIndex = 0;
		int datasetIndex = 0;

		/*
		 * Change the List of indicators so that the candle dataset is the first
		 * one in the list. The main chart must be plotted first.
		 */
		List<IndicatorDataset> indicators = new ArrayList<IndicatorDataset>(0);
		for (IndicatorDataset item : strategyData.getIndicators()) {
			if (IndicatorSeries.CandleSeries.equals(item.getType(0))) {
				indicators.add(item);
			}
		}
		for (IndicatorDataset item : strategyData.getIndicators()) {
			if (!IndicatorSeries.CandleSeries.equals(item.getType(0))) {
				indicators.add(item);
			}
		}
		for (int i = 0; i < indicators.size(); i++) {
			IndicatorDataset indicator = indicators.get(i);
			if (indicator.getDisplaySeries(0)) {

				if (indicator.getSubChart(0)) {
					String axisName = "Price";
					if (IndicatorSeries.CandleSeries.equals(indicator
							.getType(0))) {
						axisName = ((CandleSeries) indicator.getSeries(0))
								.getSymbol();
					} else {
						org.trade.dictionary.valuetype.IndicatorSeries code = org.trade.dictionary.valuetype.IndicatorSeries
								.newInstance(indicator.getType(0));
						axisName = code.getDisplayName();
					}
					NumberAxis subPlotAxis = new NumberAxis(axisName);
					subPlotAxis.setAutoRange(true);
					subPlotAxis.setAutoRangeIncludesZero(false);

					XYPlot subPlot = new XYPlot((XYDataset) indicator,
							dateAxis, subPlotAxis, indicator.getRenderer());

					subPlot.setOrientation(PlotOrientation.VERTICAL);
					subPlot.setDomainPannable(true);
					subPlot.setRangePannable(true);
					subPlot.setDomainCrosshairVisible(true);
					subPlot.setDomainCrosshairLockedOnData(true);
					subPlot.setRangeCrosshairVisible(true);
					subPlot.setRangeCrosshairLockedOnData(true);
					subPlot.setRangeGridlinePaint(new Color(204, 204, 204));
					subPlot.setDomainGridlinePaint(new Color(204, 204, 204));
					subPlot.setBackgroundPaint(Color.white);
					XYItemRenderer renderer = subPlot
							.getRendererForDataset((XYDataset) indicator);
					for (int seriesIndex = 0; seriesIndex < ((XYDataset) indicator)
							.getSeriesCount(); seriesIndex++) {
						renderer.setSeriesPaint(seriesIndex,
								indicator.getSeriesColor(seriesIndex));
					}
					mainPlot.add(subPlot, 1);

				} else {
					datasetIndex++;
					pricePlot.setDataset(datasetIndex, (XYDataset) indicator);
					if (IndicatorSeries.CandleSeries.equals(indicator
							.getType(0))) {
						// add secondary axis
						axixIndex++;

						final NumberAxis axis2 = new NumberAxis(
								((CandleSeries) indicator.getSeries(0))
										.getSymbol());
						axis2.setAutoRange(true);
						axis2.setAutoRangeIncludesZero(false);
						pricePlot.setRangeAxis(datasetIndex, axis2);
						pricePlot.setRangeAxisLocation(i + 1,
								AxisLocation.BOTTOM_OR_RIGHT);
						pricePlot
								.mapDatasetToRangeAxis(datasetIndex, axixIndex);
						pricePlot.setRenderer(datasetIndex,
								new StandardXYItemRenderer());
					} else {
						pricePlot.setRenderer(datasetIndex,
								indicator.getRenderer());
					}
					XYItemRenderer renderer = pricePlot
							.getRendererForDataset((XYDataset) indicator);

					for (int seriesIndex = 0; seriesIndex < ((XYDataset) indicator)
							.getSeriesCount(); seriesIndex++) {
						renderer.setSeriesPaint(seriesIndex,
								indicator.getSeriesColor(seriesIndex));
					}
				}
			}
		}
		JFreeChart jfreechart = new JFreeChart(title, null, mainPlot, true);
		jfreechart.setAntiAlias(false);
		return jfreechart;
	}

	/**
	 * Method seriesChanged.
	 * 
	 * @param event
	 *            SeriesChangeEvent
	 * @see org.jfree.data.general.SeriesChangeListener#seriesChanged(SeriesChangeEvent)
	 */
	public void seriesChanged(SeriesChangeEvent event) {

		Object series = event.getSource();
		if (series instanceof CandleSeries) {

			CandleSeries candleSeries = (CandleSeries) series;
			if (!candleSeries.isEmpty()) {
				CombinedDomainXYPlot combinedXYplot = (CombinedDomainXYPlot) this.chart
						.getPlot();
				@SuppressWarnings("unchecked")
				List<XYPlot> subplots = combinedXYplot.getSubplots();
				XYPlot xyplot = subplots.get(0);

				CandleItem candleItem = (CandleItem) candleSeries
						.getDataItem(candleSeries.getItemCount() - 1);
				String msg = "Time: "
						+ TradingCalendar.getFormattedDate(
								candleItem.getLastUpdateDate(), TIME_FORMAT)
						+ " Open: " + new Money(candleItem.getOpen())
						+ " High: " + new Money(candleItem.getHigh())
						+ " Low: " + new Money(candleItem.getLow())
						+ " Close: " + new Money(candleItem.getClose())
						+ " Vwap: " + new Money(candleItem.getVwap());
				titleLegend2.setText(msg);
				valueMarker.setValue(candleItem.getClose());

				double x = TradingCalendar
						.geMillisFromZonedDateTime(TradingCalendar
								.getDateAtTime(candleItem.getPeriod()
										.getStart(), candleSeries
										.getStartTime()));
				String annotationText = "("
						+ TradingCalendar.getFormattedDate(
								candleItem.getLastUpdateDate(), TIME_FORMAT)
						+ ", " + new Money(candleItem.getClose()) + ")";
				if (null == closePriceLine) {
					closePriceLine = new XYTextAnnotation(annotationText, x,
							candleItem.getY());
					closePriceLine.setTextAnchor(TextAnchor.BOTTOM_RIGHT);
					xyplot.addAnnotation(closePriceLine);
					xyplot.addRangeMarker(valueMarker);
				} else {
					closePriceLine.setText(annotationText);
					closePriceLine.setX(x);
					closePriceLine.setY(candleItem.getY());
				}

				this.chart.fireChartChanged();
			}
		}
	}

	/**
	 * Method addBuySellTradeArrow.
	 * 
	 * @param action
	 *            String
	 * @param price
	 *            Money
	 * @param time
	 *            ZonedDateTime
	 * @param quantity
	 *            Integer
	 * @throws ValueTypeException
	 */
	public void addBuySellTradeArrow(String action, Money price,
			ZonedDateTime time, Integer quantity) throws ValueTypeException {
		String label = Action.newInstance(action) + " " + quantity + "@"
				+ price;
		XYPointerAnnotation arrow = new XYPointerAnnotation(label,
				TradingCalendar.geMillisFromZonedDateTime(time),
				price.doubleValue(), 90d);
		arrow.setLabelOffset(5.0);
		arrow.setBackgroundPaint(Color.GREEN);
		if (action.equals(Action.SELL)) {
			arrow.setAngle(-90d);
			arrow.setBackgroundPaint(Color.RED);
		}
		CombinedDomainXYPlot combinedXYplot = (CombinedDomainXYPlot) this.chart
				.getPlot();
		@SuppressWarnings("unchecked")
		List<XYPlot> subplots = combinedXYplot.getSubplots();
		XYPlot xyplot = subplots.get(0);
		xyplot.addAnnotation(arrow);
		this.chart.fireChartChanged();
	}

	/**
	 * Method setNonTradingPeriods.
	 * 
	 * @param start
	 *            ZonedDateTime
	 * @param end
	 *            ZonedDateTime
	 * 
	 * @param openDate
	 *            ZonedDateTime
	 * 
	 * @param closeDate
	 *            ZonedDateTime
	 * 
	 * @param segments15min
	 *            int
	 * @return List<Date>
	 */
	private List<java.util.Date> getNonTradingPeriods(ZonedDateTime startDate,
			ZonedDateTime endDate, ZonedDateTime openDate,
			ZonedDateTime closeDate, SegmentedTimeline segmentedTimeline) {
		/*
		 * Add all 15min periods that are not trading times.
		 */

		List<java.util.Date> noneTradingSegments = new ArrayList<>();
		do {
			/*
			 * 96 15min periods per day
			 */
			for (int j = 0; j < 96; j++) {
				ZonedDateTime segmentStartDate = TradingCalendar.getDateAtTime(
						startDate, openDate);
				segmentStartDate = segmentStartDate.plusMinutes(j * 15);

				if (!TradingCalendar.isTradingDay(segmentStartDate)
						|| !TradingCalendar.isMarketHours(openDate, closeDate,
								segmentStartDate)) {
					Segment segment = segmentedTimeline
							.getSegment(TradingCalendar
									.geMillisFromZonedDateTime(segmentStartDate));
					if (segment.inIncludeSegments()) {
						noneTradingSegments
								.add(new java.util.Date(
										TradingCalendar
												.geMillisFromZonedDateTime(segmentStartDate)));
					}
				}
			}
			startDate = startDate.plusDays(1);
		} while (endDate.isAfter(startDate));

		return noneTradingSegments;
	}
}
