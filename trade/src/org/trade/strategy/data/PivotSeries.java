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
import java.util.ArrayList;
import java.util.Hashtable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.core.util.Pair;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.dictionary.valuetype.DAOEntryLimit;
import org.trade.dictionary.valuetype.Side;
import org.trade.persistent.dao.CodeValue;
import org.trade.persistent.dao.Entrylimit;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.base.RegularTimePeriod;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.candle.CandlePeriod;
import org.trade.strategy.data.pivot.PivotCalculator;
import org.trade.strategy.data.pivot.PivotItem;

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
@DiscriminatorValue("PivotSeries")
public class PivotSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;
	private Integer bars;
	private Boolean side;
	private Boolean quadratic;

	private PivotCalculator calcPivot = new PivotCalculator(2, 0.6);

	public static final String BARS = "Bars";
	public static final String QUADRATIC = "Quadratic";
	public static final String SIDE = "Side";

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
	public PivotSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for PivotSeries.
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
	 * @param side
	 *            boolean
	 * @param quadratic
	 *            boolean
	 * @param bars
	 *            Integer
	 */
	public PivotSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart, boolean side, boolean quadratic, Integer bars) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.side = side;
		this.quadratic = quadratic;
		this.bars = bars;

	}

	public PivotSeries() {
		super(IndicatorSeries.PivotSeries);
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
		final PivotItem item = (PivotItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param pivotPrice
	 *            the pivotPrice-value.
	 * @param pivotSide
	 *            the pivotSide-value.
	 */
	public void add(RegularTimePeriod period, BigDecimal pivotPrice,
			String pivotSide) {
		if (!this.isEmpty()) {
			PivotItem item0 = (PivotItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new PivotItem(period, pivotPrice, pivotSide), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            PivotItem
	 */
	public void add(PivotItem dataItem, boolean notify) {
		if (!this.isEmpty()) {
			PivotItem item0 = (PivotItem) this.getDataItem(0);
			if (!dataItem.getPeriod().getClass()
					.equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(dataItem, notify);
	}

	/**
	 * Method getBars.
	 * 
	 * @return Integer
	 */
	@Transient
	public Integer getBars() {
		try {
			if (null == this.bars)
				this.bars = (Integer) CodeValue.getValueCode(BARS,
						this.getCodeValues());
		} catch (Exception e) {
			this.bars = null;
		}
		return this.bars;
	}

	/**
	 * Method setBars.
	 * 
	 * @param bars
	 *            Integer
	 */
	public void setBars(Integer bars) {
		this.bars = bars;
	}

	/**
	 * Method getSide.
	 * 
	 * @return Boolean
	 */
	@Transient
	public Boolean getSide() {
		try {
			if (null == this.side)
				this.side = (Boolean) CodeValue.getValueCode(SIDE,
						this.getCodeValues());
		} catch (Exception e) {
			this.side = null;
		}
		return this.side;
	}

	/**
	 * Method setSide.
	 * 
	 * @param side
	 *            Boolean
	 */
	public void setSide(Boolean side) {
		this.side = side;
	}

	/**
	 * Method getQuadratic.
	 * 
	 * @return Boolean
	 */
	@Transient
	public Boolean getQuadratic() {
		try {
			if (null == this.quadratic)
				this.quadratic = (Boolean) CodeValue.getValueCode(QUADRATIC,
						this.getCodeValues());
		} catch (Exception e) {
			this.quadratic = null;
		}
		return this.quadratic;
	}

	/**
	 * Method setQuadratic.
	 * 
	 * @param quadratic
	 *            Boolean
	 */
	public void setQuadratic(Boolean quadratic) {
		this.quadratic = quadratic;
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

		for (int i = 0; i < candleDataset.getSeries(seriesIndex).getItemCount(); i++) {
			this.updateSeries(candleDataset.getSeries(seriesIndex), i, true);
		}
	}

	/**
	 * Checks to see if the previous number or bars make a V shape using the x,y
	 * vwap values. Take the Vwap of an odd number of prev candles i.e 3,5,7
	 * (values is configurable I use 5) calculate the quadratic curve between
	 * the points (config value), (uses 2nd order) now if the correlation coeff
	 * is > 0.6 from the x values calculate the new y values and now check the 5
	 * values form a V shape. No pivot can be within 5 bars of another pivot.
	 * 
	 * 
	 * @param source
	 *            CandleSeries
	 * @param skip
	 *            int
	 * @param newBar
	 *            boolean
	 * @throws ValueTypeException
	 */

	public void updateSeries(CandleSeries source, int skip, boolean newBar) {

		if (source == null) {
			throw new IllegalArgumentException("Null source (CandleSeries).");
		}

		if (!newBar)
			return;

		PivotItem dataItem = null;
		Hashtable<Long, Pair> userDataVector = new Hashtable<Long, Pair>();
		DAOEntryLimit entryLimits = new DAOEntryLimit();

		boolean pivot = false;
		String side = null;
		String pivotSide = null;
		CandleItem pivotCandle = null;
		CandleItem pivotRangeCandle = null;
		/*
		 * Start with the previous bar and work back
		 */
		int middleBar = (this.getBars() - 1) / 2;
		int startBar = skip - 1;
		if ((startBar + 1) >= this.getBars()) {
			// Get the x,y pairs
			for (int i = startBar; i > (startBar - this.getBars()); i--) {
				CandleItem candleItem = (CandleItem) source.getDataItem(i);
				// TODO next line just for debugging
				// if (((BarPeriod) candle.getPeriod()).getDaySerialIndex() ==
				// 690) {
				// _log.info("At day index: " + candle.getPeriod());
				// }
				/*
				 * If there is a pivot in the last five then there cannot be
				 * another for 5 bars.
				 */

				long time = (((CandlePeriod) candleItem.getPeriod())
						.getDaySerialIndex());
				userDataVector.put(time, new Pair(time, candleItem.getVwap()));
			}

			/*
			 * Calculate the new y points with the curve.
			 */
			if (this.getQuadratic()) {
				calcPivot.calculatePivot(new ArrayList<Pair>(userDataVector
						.values()));
			}

			CandleItem prevCandle = null;
			for (int i = startBar; i > (startBar - this.getBars()); i--) {
				CandleItem candle = (CandleItem) source.getDataItem(i);

				pivot = false;

				if (null != prevCandle) {
					/*
					 * Set the side based on the Vwap
					 */
					if (null == side) {
						if ((userDataVector.get(((CandlePeriod) candle
								.getPeriod()).getDaySerialIndex()).y) < (userDataVector
								.get(((CandlePeriod) prevCandle.getPeriod())
										.getDaySerialIndex()).y)) {
							side = Side.BOT;
							pivotSide = Side.BOT;
						} else {
							side = Side.SLD;
							pivotSide = Side.SLD;
						}
						/*
						 * Is the first candle in the right direction
						 */
						if (!prevCandle.isSide(side) && this.getSide()) {
							break;
						}
					}

					/*
					 * Reset the Side as we pivot at the middle index of the
					 * bars
					 */
					if (i == (startBar - middleBar - 1)) {
						if (side.equals(Side.SLD)) {
							side = Side.BOT;
						} else {
							side = Side.SLD;
						}
					}

					if (candle.getPeriod().getStart().getYear() == prevCandle
							.getPeriod().getStart().getYear()
							&& candle.getPeriod().getStart().getDayOfYear() == prevCandle
									.getPeriod().getStart().getDayOfYear()) {

						if (side.equals(Side.BOT)) {
							if ((userDataVector.get(((CandlePeriod) candle
									.getPeriod()).getDaySerialIndex()).y) < (userDataVector
									.get(((CandlePeriod) prevCandle.getPeriod())
											.getDaySerialIndex()).y)) {
								if (this.getSide()) {

									if (i == (startBar - middleBar)) {
										pivot = true;
									} else {
										if (candle.isSide(side)) {
											pivot = true;
										}
									}
								} else {
									pivot = true;
								}
							}
						} else {
							if ((userDataVector.get(((CandlePeriod) candle
									.getPeriod()).getDaySerialIndex()).y) > (userDataVector
									.get(((CandlePeriod) prevCandle.getPeriod())
											.getDaySerialIndex()).y)) {
								if (this.getSide()) {
									if (i == (startBar - middleBar)) {
										pivot = true;
									} else {
										if (candle.isSide(side)) {
											pivot = true;
										}
									}
								} else {
									pivot = true;
								}
							}
						}
						/*
						 * Find the highest and lowest vwap for the series we
						 * are testing.
						 */
						if (pivotSide.equals(Side.BOT)) {
							if (candle.getVwap() < prevCandle.getVwap()) {
								pivotCandle = candle;
							}
							if (candle.getVwap() > pivotRangeCandle.getVwap()) {
								pivotRangeCandle = candle;
							}
						} else {
							if (candle.getVwap() > prevCandle.getVwap()) {
								pivotCandle = candle;
							}
							if (candle.getVwap() < pivotRangeCandle.getVwap()) {
								pivotRangeCandle = candle;
							}
						}
					}
					if (!pivot) {
						break;
					}

				} else {
					pivotRangeCandle = candle;
				}
				prevCandle = candle;
			}
			if (pivot && (null != pivotCandle)) {

				try {
					Entrylimit entryLimit = entryLimits.getValue(new Money(
							pivotCandle.getVwap()));
					Money pivotRange = new Money(Math.abs((pivotRangeCandle
							.getVwap() - pivotCandle.getVwap())));

					// _log.info("Pivot Date: "
					// + pivotCandle.getPeriod()
					// + " Period Index: "
					// + ((CandlePeriod) pivotCandle.getPeriod())
					// .getDaySerialIndex() + " Pivot Price: "
					// + pivotCandle.getVwap() + " Pivot Range Date: "
					// + pivotRangeCandle.getPeriod() + " Pivot H/L:"
					// + pivotRangeCandle.getVwap() + " Pivot range: "
					// + entryLimit.getPivotRange() + " Pivot Side: "
					// + pivotSide);
					// _log.info("Pivot: " + calcPivot.toString());

					if (null != entryLimit
							&& entryLimit.getPivotRange().doubleValue() <= pivotRange
									.doubleValue()) {
						Money pivotPrice = new Money(pivotCandle.getVwap());
						dataItem = new PivotItem(pivotCandle.getPeriod(),
								pivotPrice.getBigDecimalValue(), pivotSide);
					}
				} catch (Exception ex) {
					_log.error(
							"Error find Pivot Range Msg: " + ex.getMessage(),
							ex);
				}
			}
		}
		if (null != dataItem) {
			if (this.indexOf(dataItem.getPeriod()) < 0) {
				this.add(dataItem, false);
			} else {
				PivotItem currDataItem = (PivotItem) this.getDataItem(this
						.indexOf(dataItem.getPeriod()));
				currDataItem.setPivotPrice(dataItem.getPivotPrice());
				currDataItem.setPivotSide(dataItem.getPivotSide());
			}
		}
	}

	/**
	 * Method printSeries.
	 */
	public void printSeries() {
		for (int i = 0; i < this.getItemCount(); i++) {
			PivotItem dataItem = (PivotItem) this.getDataItem(i);
			_log.debug("Type: " + this.getType() + " Time: "
					+ dataItem.getPeriod().getStart() + " Pivot: "
					+ dataItem.getPivotPrice() + " Side: "
					+ dataItem.getPivotSide());
		}
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		PivotSeries clone = (PivotSeries) super.clone();
		return clone;
	}
}
