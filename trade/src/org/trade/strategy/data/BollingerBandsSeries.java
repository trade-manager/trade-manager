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
import org.trade.persistent.dao.Strategy;
import org.trade.strategy.data.bollingerbands.BollingerBandsItem;
import org.trade.strategy.data.candle.CandleItem;

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
@DiscriminatorValue("BollingerBandsSeries")
public class BollingerBandsSeries extends IndicatorSeries {

	private static final long serialVersionUID = 20183087035446657L;

	public static final String LENGTH = "Length";
	public static final String NUMBER_OF_STD = "NumberOfSTD";

	private BigDecimal numberOfSTD;
	private Integer length;
	private boolean isUpper;
	/*
	 * Vales used to calculate MA's. These need to be reset when the series is
	 * cleared.
	 */
	private double sum = 0.0;
	private LinkedList<Double> yyValues = new LinkedList<Double>();

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
	public BollingerBandsSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
	}

	/**
	 * Constructor for BollingerBandsSeries.
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
	 * @param numberOfSTD
	 *            BigDecimal
	 * @param length
	 *            Integer
	 */
	public BollingerBandsSeries(Strategy strategy, String name, String type,
			String description, Boolean displayOnChart, Integer chartRGBColor,
			Boolean subChart, BigDecimal numberOfSTD, Integer length) {
		super(strategy, name, type, description, displayOnChart, chartRGBColor,
				subChart);
		this.numberOfSTD = numberOfSTD;
		this.length = length;
	}

	public BollingerBandsSeries() {
		super(IndicatorSeries.BollingerBandsIndexSeries);
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @throws CloneNotSupportedException
	 */
	public Object clone() throws CloneNotSupportedException {
		BollingerBandsSeries clone = (BollingerBandsSeries) super.clone();
		clone.yyValues = new LinkedList<Double>();
		return clone;
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
		final BollingerBandsItem item = (BollingerBandsItem) getDataItem(index);
		return item.getPeriod();
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * @param period
	 *            the period.
	 * @param bollingerBands
	 *            the bollingerBands.
	 */
	public void add(RegularTimePeriod period, BigDecimal bollingerBands) {
		if (getItemCount() > 0) {
			BollingerBandsItem item0 = (BollingerBandsItem) this.getDataItem(0);
			if (!period.getClass().equals(item0.getPeriod().getClass())) {
				throw new IllegalArgumentException(
						"Can't mix RegularTimePeriod class types.");
			}
		}
		super.add(new BollingerBandsItem(period, bollingerBands), true);
	}

	/**
	 * Adds a data item to the series.
	 * 
	 * 
	 * @param notify
	 *            the notify listeners.
	 * @param dataItem
	 *            BollingerBandsItem
	 */
	public void add(BollingerBandsItem dataItem, boolean notify) {
		if (getItemCount() > 0) {
			BollingerBandsItem item0 = (BollingerBandsItem) this.getDataItem(0);
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
			BollingerBandsItem item = (BollingerBandsItem) this.data.get(i - 1);
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
	 * 
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
	 * 
	 * @param length
	 *            Integer
	 */
	public void setLength(Integer length) {
		this.length = length;
	}

	/**
	 * Method getNumberOfSTD.
	 * 
	 * @return BigDecimal
	 */
	@Transient
	public BigDecimal getNumberOfSTD() {
		try {
			if (null == this.numberOfSTD)
				this.numberOfSTD = (BigDecimal) this
						.getValueCode(NUMBER_OF_STD);
		} catch (Exception e) {
			this.numberOfSTD = null;
		}
		return this.numberOfSTD;
	}

	/**
	 * Method setNumberOfSTD.
	 * 
	 * @param MAType
	 *            String
	 */
	public void setNumberOfSTD(BigDecimal numberOfSTD) {
		this.numberOfSTD = numberOfSTD;
	}

	/**
	 * Method getIsUpper.
	 * 
	 * @return boolean
	 */
	@Transient
	public boolean getIsUpper() {
		return this.isUpper;
	}

	/**
	 * Method setLength.
	 * 
	 * @param isUpper
	 *            boolean
	 */
	public void setIsUpper(boolean isUpper) {
		this.isUpper = isUpper;
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
			this.updateSeries(source.getSeries(seriesIndex), i);
		}

	}

	/**
	 * Method updateSeries.
	 * 
	 * @param source
	 *            CandleSeries
	 * @param skip
	 *            int
	 */
	public void updateSeries(CandleSeries source, int skip) {

		if (source == null) {
			throw new IllegalArgumentException("Null source (CandleSeries).");
		}
		if (getLength() == null || getLength() < 1) {
			throw new IllegalArgumentException(
					"MA period must be greater than zero.");
		}

		if (getNumberOfSTD() == null || getNumberOfSTD().doubleValue() < 1) {
			throw new IllegalArgumentException(
					"Number of STD's must be greater than zero.");
		}

		if (skip == 0) {
			sum = 0.0;
			this.yyValues.clear();
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
					} else {
						sum = sum - this.yyValues.getFirst() + yy.doubleValue();
						this.yyValues.removeFirst();
						this.yyValues.addFirst(yy.doubleValue());
					}
				} else {
					sum = sum + yy.doubleValue();
					this.yyValues.addFirst(yy.doubleValue());
				}

				if (this.yyValues.size() == getLength()) {
					double ma = calculateBBands(this.getNumberOfSTD(),
							this.yyValues, sum);
					if (index < 0) {
						BollingerBandsItem dataItem = new BollingerBandsItem(
								candleItem.getPeriod(), new BigDecimal(ma));
						this.add(dataItem, false);

					} else {
						BollingerBandsItem currDataItem = (BollingerBandsItem) this
								.getDataItem(this.indexOf(candleItem
										.getPeriod()));
						currDataItem.setBollingerBands(ma);
					}
				}
			}
		}
	}

	/**
	 * Method calculateMA.
	 * 
	 * @param numberOfSTD
	 *            BigDecimal
	 * @param yyValues
	 *            LinkedList<Double>
	 * @param volValues
	 *            LinkedList<Long>
	 * @param sum
	 *            Double
	 * @return double
	 */
	private double calculateBBands(BigDecimal numberOfSTD,
			LinkedList<Double> yyValues, Double sum) {

		if (this.isUpper) {
			return ((sum / this.getLength()) + (standardDeviation(yyValues, sum) * this
					.getNumberOfSTD().doubleValue()));
		} else {
			return ((sum / this.getLength()) - (standardDeviation(yyValues, sum) * this
					.getNumberOfSTD().doubleValue()));
		}
	}

	public double standardDeviation(LinkedList<Double> a, Double sum) {

		double sumTotal = 0;
		double mean = sum / (a.size() * 1.0);
		for (Double i : a)
			sumTotal += Math.pow((i - mean), 2);
		return Math.sqrt(sumTotal / (a.size() - 1)); // sample
	}
}
