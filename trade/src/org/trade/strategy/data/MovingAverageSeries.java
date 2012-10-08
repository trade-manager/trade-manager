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
import java.util.Date;
import java.util.LinkedList;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.trade.dictionary.valuetype.CalculationType;
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.movingaverage.MovingAverageItem;

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
@DiscriminatorValue("MovingAverageSeries")
public class MovingAverageSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	public static final String LENGTH = "Length";
	public static final String MA_TYPE = "MAType";

	private String MAType;
	private Integer length;
	/*
	 * Vales used to calculate MA's. These need to be reset when the series is
	 * cleared.
	 */
	private double sum = 0.0;
	private double multiplyer = 0;
	private LinkedList<Double> yyValues = new LinkedList<Double>();
	private LinkedList<Long> volValues = new LinkedList<Long>();

	/**
	 * Creates a new empty series. By default, items added to the series will be
	 * sorted into ascending order by period, and duplicate periods will not be
	 * allowed.
	 * 
	
	 * 
	 * @param strategy Strategy
	 * @param name String
	 * @param type String
	 * @param description String
	 * @param displayOnChart Boolean
	 * @param chartRGBColor Integer
	 * @param subChart Boolean
	 */
	public MovingAverageSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for MovingAverageSeries.
	 * @param strategy Strategy
	 * @param name String
	 * @param type String
	 * @param description String
	 * @param displayOnChart Boolean
	 * @param chartRGBColor Integer
	 * @param subChart Boolean
	 * @param MAType String
	 * @param length Integer
	 */
	public MovingAverageSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart, String MAType, Integer length) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.MAType = MAType;
		this.length = length;
	}

	public MovingAverageSeries() {
		super(IndicatorSeries.MovingAverageSeries);
	}

	/**
	 * Method clone.
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		MovingAverageSeries clone = (MovingAverageSeries) super.clone();
		clone.yyValues = new LinkedList<Double>();
		clone.volValues = new LinkedList<Long>();
		return clone;
	}

	/**
	 * Returns the time period for the specified item.
	 * 
	 * @param index
	 *            the item index.
	 * 
	
	 * @return The time period. */
	public RegularTimePeriod getPeriod(int index) {
		final MovingAverageItem item = (MovingAverageItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param movingAverage
	 *            the movingAverage.
	
	
	 */
	public void add(RegularTimePeriod period, BigDecimal movingAverage) {
		if (getItemCount() > 0) {
			MovingAverageItem item0 = (MovingAverageItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new MovingAverageItem(period, movingAverage), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem MovingAverageItem
	 */
	public void add(MovingAverageItem dataItem, boolean notify) {
		if (getItemCount() > 0) {
			MovingAverageItem item0 = (MovingAverageItem) this.getDataItem(0);
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
	
	 * @return exists */
	public int indexOf(Date date) {

		for (int i = this.data.size(); i > 0; i--) {
			MovingAverageItem item = (MovingAverageItem) this.data.get(i - 1);
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
	 * Method getLength.
	 * @return Integer
	 */
	@Transient
	public Integer getLength() {
		try {
			if (null == this.length)
				this.length = (Integer) this.getValueCode(LENGTH);
		} catch (Exception e) {
			this.length = null;
		}
		return this.length;
	}

	/**
	 * Method setLength.
	 * @param length Integer
	 */
	public void setLength(Integer length) {
		this.length = length;
	}

	/**
	 * Method getMAType.
	 * @return String
	 */
	@Transient
	public String getMAType() {
		try {
			if (null == this.MAType)
				this.MAType = (String) this.getValueCode(MA_TYPE);
		} catch (Exception e) {
			this.MAType = null;
		}
		return this.MAType;
	}

	/**
	 * Method setMAType.
	 * @param MAType String
	 */
	public void setMAType(String MAType) {
		this.MAType = MAType;
	}

	/**
	 * Method createSeries.
	 * @param source CandleDataset
	 * @param seriesIndex int
	 */
	public void createSeries(CandleDataset source, int seriesIndex) {

		if (source.getSeries(seriesIndex) == null) {
			throw new IllegalArgumentException("Null source (CandleDataset).");
		}

		for (int i = 0; i < source.getSeries(seriesIndex).getItemCount(); i++) {
			this.updateSeries(source.getSeries(seriesIndex), i);
		}

	}

	/**
	 * Method updateSeries.
	 * @param source CandleSeries
	 * @param skip int
	 */
	public void updateSeries(CandleSeries source, int skip) {

		if (source == null) {
			throw new IllegalArgumentException("Null source (CandleSeries).");
		}
		if (getLength() < Double.MIN_VALUE) {
			throw new IllegalArgumentException("period must be positive.");
		}

		if (skip == 0) {
			sum = 0.0;
			multiplyer = 0;
			this.yyValues.clear();
			this.volValues.clear();
		}
		if (source.getItemCount() > skip) {

			// get the current data item...
			CandleItem candleItem = (CandleItem) source.getDataItem(skip);
			int index = this.indexOf(candleItem.getPeriod());
			// work out the average for the earlier values...
			Number yy = candleItem.getY();

			if (null != yy) {
				if (this.yyValues.size() == getLength()) {
					/*
					 * If the item does not exist in the series then this is a
					 * new time period and so we need to remove the last in the
					 * set and add the new periods values. Otherwise we just
					 * update the last value in the set.
					 */
					if (index < 0) {
						/*
						 * sum is just used for performance save having to sum
						 * the last set of values each time.
						 */
						sum = sum - this.yyValues.getLast() + yy.doubleValue();
						this.yyValues.removeLast();
						this.yyValues.addFirst(yy.doubleValue());
						this.volValues.removeLast();
						this.volValues.addFirst(candleItem.getVolume());
					} else {
						sum = sum - this.yyValues.getFirst() + yy.doubleValue();
						this.yyValues.removeFirst();
						this.yyValues.addFirst(yy.doubleValue());
					}
				} else {
					sum = sum + yy.doubleValue();
					this.yyValues.addFirst(yy.doubleValue());
					this.volValues.addFirst(candleItem.getVolume());
				}

				if (this.yyValues.size() == getLength()) {
					double ma = calculateMA(this.getMAType(), this.yyValues,
							this.volValues, sum);
					if (index < 0) {
						MovingAverageItem dataItem = new MovingAverageItem(
								candleItem.getPeriod(), new BigDecimal(ma));
						this.add(dataItem, false);

					} else {
						MovingAverageItem currDataItem = (MovingAverageItem) this
								.getDataItem(this.indexOf(candleItem
										.getPeriod()));
						currDataItem.setMovingAverage(ma);
					}
				}
			}
		}
	}

	/**
	 * Method calculateMA.
	 * @param calcType String
	 * @param yyValues LinkedList<Double>
	 * @param volValues LinkedList<Long>
	 * @param sum Double
	 * @return double
	 */
	private double calculateMA(String calcType, LinkedList<Double> yyValues,
			LinkedList<Long> volValues, Double sum) {

		double ma = 0;
		if (CalculationType.LINEAR.equals(calcType)) {
			ma = sum / getLength();
		} else if (CalculationType.EXPONENTIAL.equals(calcType)) {
			/*
			 * Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818
			 * (18.18%). EMA: {Close - EMA(previous day)} x * multiplier +
			 * EMA(previous day).
			 */
			if (multiplyer == 0) {
				ma = sum / getLength();
				multiplyer = 2 / (getLength() + 1.0d);
			} else {
				ma = ((yyValues.getFirst() - yyValues.get(1)) * multiplyer)
						+ yyValues.get(1);
			}
			/*
			 * Use the EMA in the stored values as we need the previous one for
			 * the calc.
			 */
			yyValues.removeFirst();
			yyValues.addFirst(ma);

		} else if (CalculationType.WEIGHTED.equals(calcType)) {

			double sumYY = 0;
			int count = 0;
			for (int i = yyValues.size(); i > 0; i--) {
				count = count + (getLength() + 1 - i);
				sumYY = sumYY + (yyValues.get(i - 1) * (getLength() + 1 - i));
			}
			ma = sumYY / count;

		} else if (CalculationType.WEIGHTED_VOLUME.equals(calcType)) {

			double sumYY = 0;
			double count = 0;
			for (int i = yyValues.size(); i > 0; i--) {
				count = count + ((getLength() + 1 - i) * volValues.get(i - 1));
				sumYY = sumYY
						+ (yyValues.get(i - 1) * volValues.get(i - 1) * (getLength() + 1 - i));
			}
			ma = sumYY / count;
		}
		return ma;
	}
}
