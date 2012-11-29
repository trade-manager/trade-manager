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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.jfree.data.time.RegularTimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BackTestBroker;
import org.trade.core.factory.ClassFactory;
import org.trade.core.util.TradingCalendar;
import org.trade.core.util.Worker;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.strategy.data.candle.CandleItem;
import org.trade.strategy.data.candle.CandlePeriod;

/**
 */
public class StrategyData extends Worker {

	private final static Logger _log = LoggerFactory
			.getLogger(StrategyData.class);

	private CandleDataset baseCandleDataset = null;
	private CandleDataset candleDataset = null;
	private BackTestBroker backTestWorker = null;
	private Strategy strategy = null;
	private List<IndicatorDataset> indicators = new ArrayList<IndicatorDataset>();

	private boolean seriesChanged = true;
	private final Object lockStrategyWorker = new Object();
	private int currentBaseCandleCount = -1;
	private int lastBaseCandleProcessed = -1;

	/**
	 * Constructor for StrategyData.
	 * 
	 * @param strategy
	 *            Strategy
	 * @param baseCandleDataset
	 *            CandleDataset
	 */
	public StrategyData(Strategy strategy, CandleDataset baseCandleDataset) {

		this.baseCandleDataset = baseCandleDataset;
		this.strategy = strategy;
		candleDataset = new CandleDataset();
		candleDataset.addSeries(CandleDataset.createSeries(baseCandleDataset,
				0, getBaseCandleSeries().getContract(), getBaseCandleSeries()
						.getBarSize(), getBaseCandleSeries().getStartTime()));
		for (IndicatorSeries indicator : strategy.getIndicatorSeries()) {

			try {
				/*
				 * For each indicator create a series that is a clone for this
				 * trade strategy.
				 */
				IndicatorSeries series = (IndicatorSeries) indicator.clone();
				series.setKey(series.getName());
				series.createSeries(candleDataset, 0);
				IndicatorDataset indicatorDataset = this
						.getIndicators(indicator.getType());
				if (null == indicatorDataset) {
					/*
					 * Data-set and Series names should have the same name with
					 * applicable extension of Series/Dataset. this allows
					 * substitution and does not require us to have another
					 * table in the DB to represent the Dataset which is just a
					 * holder for series and is required by the Chart API.
					 */
					String datasetName = indicator.getType().replaceAll(
							"Series", "Dataset");
					Vector<Object> parm = new Vector<Object>();
					indicatorDataset = (IndicatorDataset) ClassFactory
							.getCreateClass(IndicatorDataset.PACKAGE
									+ datasetName, parm, this);
					indicators.add(indicatorDataset);
				}
				indicatorDataset.addSeries(series);

			} catch (Exception ex) {
				throw new IllegalArgumentException(
						"Could not construct StrategyData Object. Either indicator was not found or was not clonable Msg: "
								+ ex.getMessage());
			}
		}
	}

	/*
	 * The main process thread. This will run until it is either canceled or is
	 * done.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.trade.strategy.impl.Worker#doInBackground()
	 */

	protected Void doInBackground() {
		/*
		 * We initialize here to keep this instances as part of this worker
		 * thread
		 */
		try {

			this.seriesChanged = false;
			do {
				/*
				 * Lock until a candle arrives. First time in we process the
				 * current candle.
				 */
				synchronized (lockStrategyWorker) {
					while ((!this.seriesChanged && this.lastBaseCandleProcessed == this.currentBaseCandleCount)
							|| this.currentBaseCandleCount == -1) {
						lockStrategyWorker.wait();
					}
					this.seriesChanged = false;
				}

				if (!this.isCancelled()) {

					if (this.currentBaseCandleCount > -1) {

						/*
						 * Another candle has been added. Add the new candle to
						 * the base series in the dataset.
						 */
						if (this.currentBaseCandleCount > this.lastBaseCandleProcessed) {
							this.lastBaseCandleProcessed++;
						}

						CandleItem candle = (CandleItem) this
								.getBaseCandleSeries().getDataItem(
										this.lastBaseCandleProcessed);
						this.getBaseCandleSeries().updatePercentChanged(candle);
						updateDatasetSeries(candle);
						/*
						 * Fire the change to the base series now the chart
						 * candle series has been updated and all the indicators
						 * are up to date. Note the strategies listen to the
						 * base candle series.
						 */
						this.getBaseCandleSeries().fireSeriesChanged();
					}
				}

			} while (!this.isDone() && !this.isCancelled());

		} catch (InterruptedException interExp) {
			// Do nothing.
		} catch (Exception ex1) {
			_log.error("Error processing indicators symbol: "
					+ this.getBaseCandleSeries().getSymbol()
					+ " Candle series size: "
					+ this.getBaseCandleSeries().getItemCount()
					+ " last candle processed: " + this.lastBaseCandleProcessed
					+ " current candle: " + this.currentBaseCandleCount
					+ " Message: " + ex1.getMessage(), ex1);

		} finally {
			/*
			 * Ok we are complete clean up.
			 */
		}
		return null;
	}

	public void cancel() {
		this.setIsCancelled(true);
		/*
		 * Unlock the doInBackground that may be waiting for a candle. This will
		 * cause a clean finish to the process.
		 */
		synchronized (lockStrategyWorker) {
			seriesChanged = true;
			lockStrategyWorker.notifyAll();
		}
	}

	protected void done() {
		// Free some memory!!
		this.clearBaseCandleSeries();
	}

	/**
	 * Method changeCandleSeriesPeriod.
	 * 
	 * @param newPeriod
	 *            int
	 */
	public void changeCandleSeriesPeriod(int newPeriod) {
		/*
		 * Clear down the dependent data sets and re populate from the base
		 * candle series.
		 */
		synchronized (getBaseCandleSeries()) {
			clearChartDatasets();
			this.getCandleDataset().getSeries(0).setBarSize(newPeriod);
			for (int i = 0; i < getBaseCandleSeries().getItemCount(); i++) {
				CandleItem candle = (CandleItem) getBaseCandleSeries()
						.getDataItem(i);
				updateDatasetSeries(candle);
			}
		}
	}

	/**
	 * Method buildCandle.
	 * 
	 * @param time
	 *            Date
	 * @param open
	 *            double
	 * @param high
	 *            double
	 * @param low
	 *            double
	 * @param close
	 *            double
	 * @param volume
	 *            long
	 * @param vwap
	 *            double
	 * @param tradeCount
	 *            int
	 * @param rollupInterval
	 *            int
	 * @return int
	 */
	public int buildCandle(Date time, double open, double high, double low,
			double close, long volume, double vwap, int tradeCount,
			int rollupInterval) {

		this.currentBaseCandleCount = this.getBaseCandleSeries().buildCandle(
				time, open, high, low, close, volume, vwap, tradeCount,
				rollupInterval);
		/*
		 * If thread Indicators the updates to all indicators and the subsequent
		 * firing of base series changed is performed via the worker thread.
		 * This should be used when this method is called from a broker thread
		 * i.e. messaged bus thread.
		 */
		if (this.isRunning()) {
			/*
			 * Unlock the doInBackground that may be waiting for a candle. This
			 * will cause a clean finish to the process.
			 */
			synchronized (lockStrategyWorker) {
				this.seriesChanged = true;
				lockStrategyWorker.notifyAll();
			}
			// _log.info("buildCandle symbol: "
			// + this.getBaseCandleSeries().getSymbol() + " Count: "
			// + this.currentCandleCount);
		} else {

			/*
			 * Another candle has been added. Add the new candle to the base
			 * series in the dataset.
			 */
			CandleItem candle = (CandleItem) this.getBaseCandleSeries()
					.getDataItem(this.currentBaseCandleCount);
			this.getBaseCandleSeries().updatePercentChanged(candle);
			updateDatasetSeries(candle);
			/*
			 * Fire the change to the base series now the chart candle series
			 * has been updated and all the indicators are up to date. Note the
			 * strategies listen to the base candle series.
			 */this.getBaseCandleSeries().fireSeriesChanged();
		}
		return this.currentBaseCandleCount;
	}

	/*
	 * Update the chart data set with the new candle from the base data set.
	 * Note there will only ever be one Series in the candle data set. Then
	 * update all the indicators before notifying any strategy workers of this
	 * even.
	 * 
	 * @param candle the new or updated candle.
	 */

	/**
	 * Method updateDatasetSeries.
	 * 
	 * @param candle
	 *            CandleItem
	 */
	private synchronized void updateDatasetSeries(CandleItem candle) {

		for (int i = 0; i < getCandleDataset().getSeriesCount(); i++) {
			CandleSeries series = getCandleDataset().getSeries(i);
			series.buildCandle(candle.getLastUpdateDate(), candle.getOpen(),
					candle.getHigh(), candle.getLow(), candle.getClose(),
					candle.getVolume(), candle.getVwap(), candle.getCount(),
					series.getBarSize() / getBaseCandleSeries().getBarSize());
			for (IndicatorDataset indicator : indicators) {
				/*
				 * CandleSeries are only updated via the API i.e. these are not
				 * true indicators and are shared across Data-sets.
				 */
				if (!IndicatorSeries.CandleSeries.equals(indicator.getType(0))) {
					indicator.updateDataset(getCandleDataset(), i);
				}
			}
			series.fireSeriesChanged();
		}
	}

	public void clearBaseCandleSeries() {
		this.currentBaseCandleCount = -1;
		this.lastBaseCandleProcessed = this.currentBaseCandleCount;
		getBaseCandleSeries().clear();
		clearChartDatasets();
	}

	public void clearChartDatasets() {
		getCandleDataset().clear();
		for (IndicatorDataset indicator : indicators) {
			if (!IndicatorSeries.CandleSeries.equals(indicator.getType(0))) {
				indicator.clear();
			}
		}
	}

	/**
	 * Method getIndicators.
	 * 
	 * @return List<IndicatorDataset>
	 */
	public List<IndicatorDataset> getIndicators() {
		return indicators;
	}

	/**
	 * Method getIndicators.
	 * 
	 * @param type
	 *            String
	 * @return IndicatorDataset
	 */
	public IndicatorDataset getIndicators(String type) {
		for (int index = 0; index < indicators.size(); index++) {
			IndicatorDataset series = indicators.get(index);
			if (series.getType(0).equals(type)) {
				return series;
			}
		}
		return null;
	}

	/**
	 * Method getBaseCandleSeries.
	 * 
	 * @return CandleSeries
	 */
	public CandleSeries getBaseCandleSeries() {
		return baseCandleDataset.getSeries(0);
	}

	/**
	 * Method getCandleDataset.
	 * 
	 * @return CandleDataset
	 */
	public CandleDataset getCandleDataset() {
		return candleDataset;
	}

	/**
	 * Method getStrategy.
	 * 
	 * @return Strategy
	 */
	public Strategy getStrategy() {
		return this.strategy;
	}

	/**
	 * Method getBackTestWorker.
	 * 
	 * @return BackTestBroker
	 */
	public BackTestBroker getBackTestWorker() {
		return this.backTestWorker;
	}

	/**
	 * Method setBackTestWorker.
	 * 
	 * @param backTestWorker
	 *            BackTestBroker
	 */
	public void setBackTestWorker(BackTestBroker backTestWorker) {
		this.backTestWorker = backTestWorker;
	}

	/**
	 * Method doDummyData.
	 * 
	 * @param series
	 *            CandleSeries
	 * @param start
	 *            Tradingday
	 * @param noDays
	 *            int
	 * @param barSize
	 *            int
	 * @param longTrade
	 *            boolean
	 * @param delaySecond
	 *            int
	 */
	public static void doDummyData(CandleSeries series, Tradingday start,
			int noDays, int barSize, boolean longTrade, int delaySecond) {

		double high = 33.98;
		double low = 33.84;
		double open = 33.90;
		double close = 33.95;
		double vwap = 34.94;
		int longShort = 1;
		if (!longTrade) {
			high = 34.15;
			low = 34.01;
			open = 34.10;
			close = 34.03;
			vwap = 34.02;
			longShort = -1;
		}
		long volume = 100000;
		int tradeCount = 100;

		long count = (((start.getClose().getTime() / 1000) - (start.getOpen()
				.getTime() / 1000)) / barSize) * noDays;

		RegularTimePeriod period = new CandlePeriod(start.getOpen(), barSize);
		series.clear();
		for (int i = 0; i < count; i++) {
			series.buildCandle(period.getStart(), open, high, low, close,
					volume, vwap, tradeCount, 1);
			high = high + (0.02 * longShort);
			low = low + (0.02 * longShort);
			open = open + (0.02 * longShort);
			close = close + (0.02 * longShort);
			vwap = vwap + (0.02 * longShort);
			period = period.next();
			if (period.getStart().equals(start.getClose())) {
				period = new CandlePeriod(
						TradingCalendar.getBusinessDayStart(TradingCalendar
								.getNextTradingDay(period.getStart())), barSize);
			}
			try {
				if (delaySecond > 0)
					Thread.sleep(delaySecond * 1000);

			} catch (InterruptedException e) {
				_log.info(" Thread interupt: " + e.getMessage());
			}
		}
	}
}
