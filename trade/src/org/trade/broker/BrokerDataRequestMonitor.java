package org.trade.broker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.PersistentModelException;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.strategy.data.CandleDataset;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.IndicatorSeries;
import org.trade.strategy.data.StrategyData;

public class BrokerDataRequestMonitor extends SwingWorker<Void, String> {

	private final static Logger _log = LoggerFactory
			.getLogger(BrokerDataRequestMonitor.class);

	private BrokerModel brokerModel;
	private PersistentModel tradePersistentModel = null;
	private Tradingdays tradingdays = null;
	private int grandTotal = 0;
	private long startTime = 0;
	private Integer backTestBarSize = 0;
	private final Integer TIME_BETWEEN_SUBMIT = new Integer(4);
	private AtomicInteger timerRunning = null;
	private final Object lockCoreUtilsTest = new Object();
	private Timer timer = null;
	private LinkedList<Long> submitTimes = new LinkedList<Long>();
	private final ConcurrentHashMap<String, Contract> contractRequests = new ConcurrentHashMap<String, Contract>();
	private final ConcurrentHashMap<Integer, Tradestrategy> indicatorRequests = new ConcurrentHashMap<Integer, Tradestrategy>();

	/**
	 * Constructor for BrokerDataRequestProgressMonitor.
	 * 
	 * @param brokerManagerModel
	 *            BrokerModel
	 * @param tradingdays
	 *            Tradingdays
	 * @throws IOException
	 */
	public BrokerDataRequestMonitor(BrokerModel brokerModel,
			PersistentModel tradePersistentModel, Tradingdays tradingdays)
			throws IOException {
		this.brokerModel = brokerModel;
		this.tradePersistentModel = tradePersistentModel;
		this.tradingdays = tradingdays;
		this.backTestBarSize = ConfigProperties
				.getPropAsInt("trade.backtest.barSize");
		this.timer = new Timer(250, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				synchronized (lockCoreUtilsTest) {
					timerRunning.addAndGet(250);
					lockCoreUtilsTest.notifyAll();
				}
			}
		});
	}

	/**
	 * Method doInBackground.
	 * 
	 * @return Void
	 */
	public Void doInBackground() {

		String message = null;
		int totalSumbitted = 0;
		int reSumbittedAt = 20;
		this.startTime = System.currentTimeMillis();
		this.submitTimes.clear();
		ConcurrentHashMap<Integer, Tradingday> runningContractRequests = new ConcurrentHashMap<Integer, Tradingday>();

		// Initialize the progress bar
		setProgress(0);

		try {
			this.grandTotal = calculateTotalTradestrategiesToProcess(this.startTime);

			Collections.sort(tradingdays.getTradingdays(),
					Tradingday.DATE_ORDER_ASC);

			for (Tradingday tradingday : tradingdays.getTradingdays()) {

				Tradingday toProcessTradingday = (Tradingday) tradingday
						.clone();
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					tradestrategy.setStrategyData(StrategyData
							.create(tradestrategy));
					toProcessTradingday.addTradestrategy(tradestrategy);
					addIndicatorTradestrategyToTradingday(toProcessTradingday,
							tradestrategy);
				}

				totalSumbitted = processTradingday(
						getTradingdayToProcess(toProcessTradingday,
								runningContractRequests), totalSumbitted);
				/*
				 * Every reSumbittedAt value try to run any that could not be
				 * run due to a conflict. Run them in asc date order.
				 */
				if (totalSumbitted > reSumbittedAt) {
					reSumbittedAt = totalSumbitted + reSumbittedAt;
					totalSumbitted = reProcessTradingdays(tradingdays,
							runningContractRequests, totalSumbitted);
				}
			}

			/*
			 * If we are getting data for back testing and the backTestBarSize
			 * is set. Then get the candles for the tradestrategy tradingday
			 * with the new bar size setting. The backTestBroker will use these
			 * candles to build up the candle on the Tradestrategy/BarSize.
			 */
			if (backTestBarSize > 0 && this.brokerModel.isBrokerDataOnly()) {
				Collections.sort(tradingdays.getTradingdays(),
						Tradingday.DATE_ORDER_ASC);
				for (Tradingday itemTradingday : tradingdays.getTradingdays()) {
					if (TradingCalendar.isTradingDay(itemTradingday.getOpen())
							&& TradingCalendar
									.sameDay(
											itemTradingday.getOpen(),
											TradingCalendar
													.getZonedDateTimeFromMilli(this.startTime))
							&& !TradingCalendar.isAfterHours(TradingCalendar
									.getZonedDateTimeFromMilli(this.startTime)))
						continue;

					Tradingday tradingday = (Tradingday) itemTradingday.clone();
					for (Tradestrategy itemTradestrategy : itemTradingday
							.getTradestrategies()) {
						if (backTestBarSize < itemTradestrategy.getBarSize()) {
							try {
								Tradestrategy tradestrategy = (Tradestrategy) itemTradestrategy
										.clone();
								tradestrategy.setBarSize(backTestBarSize);
								tradestrategy.setChartDays(1);
								tradestrategy
										.setIdTradeStrategy(this.brokerModel
												.getNextRequestId());
								tradestrategy.setStrategyData(null);
								tradestrategy.setStrategyData(StrategyData
										.create(tradestrategy));

								if (this.brokerModel
										.validateBrokerData(tradestrategy)) {

									/*
									 * Refresh the data set container as these
									 * may have changed.
									 */
									tradingday.addTradestrategy(tradestrategy);
									addIndicatorTradestrategyToTradingday(
											tradingday, tradestrategy);
								}
							} catch (BrokerModelException ex) {
								// Do nothing the Barsize/Charts Days are
								// not valid.
								continue;
							}
						}
					}
					totalSumbitted = processTradingday(
							getTradingdayToProcess(tradingday,
									runningContractRequests), totalSumbitted);

				}
			}

			/*
			 * Every reSumbittedAt value submitted contracts try to run any that
			 * could not be run due to a conflict. Run then in asc date order
			 * value.
			 */

			totalSumbitted = reProcessTradingdays(tradingdays,
					runningContractRequests, totalSumbitted);

		} catch (InterruptedException ex) {
			// Do nothing
			_log.error("doInBackground interupted Msg: ", ex.getMessage());
		} catch (Exception ex) {
			_log.error("Error getting history data Msg: ", ex.getMessage());
			this.firePropertyChange("error", new String("OK"), ex);
		} finally {
			synchronized (this.brokerModel.getHistoricalData()) {
				while ((this.brokerModel.getHistoricalData().size() > 0)
						&& !this.isCancelled()) {
					try {
						this.brokerModel.getHistoricalData().wait();
						int percent = (int) (((double) (getGrandTotal() - this.brokerModel
								.getHistoricalData().size()) / getGrandTotal()) * 100d);
						setProgress(percent);
					} catch (InterruptedException ex) {
						// Do nothing
						_log.error("doInBackground finally interupted Msg: ",
								ex.getMessage());
					}
				}
			}
			setProgress(100);
			message = "Completed Historical data total contracts processed: "
					+ totalSumbitted + " in : "
					+ ((System.currentTimeMillis() - this.startTime) / 1000)
					+ " Seconds.";
			_log.debug(message);
			publish(message);

		}
		return null;
	}

	/**
	 * Method submitBrokerRequest.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @param totalSumbitted
	 *            int
	 * @return int
	 * @throws InterruptedException
	 * @throws BrokerModelException
	 */
	private int submitBrokerRequest(Tradestrategy tradestrategy,
			ZonedDateTime endDate, int totalSumbitted)
			throws InterruptedException, BrokerModelException {

		if (this.brokerModel.isHistoricalDataRunning(tradestrategy
				.getContract()) || this.isCancelled()) {
			_log.error("submitBrokerRequest contract already running: "
					+ tradestrategy.getContract().getSymbol() + " endDate: "
					+ endDate + " barSize: " + tradestrategy.getBarSize()
					+ " chartDays: " + tradestrategy.getChartDays());
			return totalSumbitted;
		}
		_log.debug("submitBrokerRequest: "
				+ tradestrategy.getContract().getSymbol() + " endDate: "
				+ endDate + " barSize: " + tradestrategy.getBarSize()
				+ " chartDays:" + tradestrategy.getChartDays());

		/*
		 * Get the contract details.
		 */
		if (contractRequests.containsKey(tradestrategy.getContract()
				.getSymbol())) {
			this.brokerModel.onContractDetails(tradestrategy.getContract());
			contractRequests.remove(tradestrategy.getContract().getSymbol());
		}

		this.brokerModel.onBrokerData(tradestrategy, endDate);

		totalSumbitted++;
		hasSubmittedInSeconds();

		/*
		 * This can happen if there is the same indicator contract but in
		 * different barSize/duration.
		 */

		if (totalSumbitted > getGrandTotal())
			incrementGrandTotal();

		int percent = (int) (((double) (totalSumbitted - this.brokerModel
				.getHistoricalData().size()) / getGrandTotal()) * 100d);
		setProgress(percent);

		/*
		 * Need to slow things down as limit is 60 including real time bars
		 * requests. When connected to TWS. Note only TWSManager return true for
		 * connected.
		 */
		if (((Math.floor(totalSumbitted / 58d) == (totalSumbitted / 58d)) && (totalSumbitted > 0))
				&& this.brokerModel.isConnected()) {

			timerRunning = new AtomicInteger(0);
			timer.start();
			synchronized (lockCoreUtilsTest) {
				while (timerRunning.get() / 1000 < 601 && !this.isCancelled()) {
					if ((timerRunning.get() % 60000) == 0) {
						String message = "Please wait "
								+ (10 - (timerRunning.get() / 1000 / 60))
								+ " minutes as there are more than 60 data requests.";
						publish(message);
					}
					lockCoreUtilsTest.wait();
				}
			}
			timer.stop();
			_log.debug("Finished wait 10min wait");
		}

		/*
		 * The SwingWorker has a maximum of 10 threads to run and this process
		 * uses one so we have 9 left for the BrokerWorkers. So wait while the
		 * BrokerWorkers threads complete.
		 */
		if (!this.isCancelled()) {
			synchronized (this.brokerModel.getHistoricalData()) {
				while (this.brokerModel.getHistoricalData().size() > 8) {
					this.brokerModel.getHistoricalData().wait();
				}
			}
		}
		return totalSumbitted;
	}

	/**
	 * Method addIndicatorTradestrategyToTradingday. For the tradingday find all
	 * the indicators and share them across like tradestrategies add the unique
	 * ones to the tradeingday for processing.
	 * 
	 * @param tradingday
	 *            Tradingday
	 * @param tradestrategy
	 *            Tradestrategy
	 * 
	 * @return boolean
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 * @throws CloneNotSupportedException
	 */
	private boolean addIndicatorTradestrategyToTradingday(
			Tradingday tradingday, Tradestrategy tradestrategy)
			throws BrokerModelException, PersistentModelException,
			CloneNotSupportedException {

		boolean addedIndicator = false;

		CandleDataset candleDataset = (CandleDataset) tradestrategy
				.getStrategyData().getIndicatorByType(
						IndicatorSeries.CandleSeries);

		if (null != candleDataset) {
			for (int seriesIndex = 0; seriesIndex < candleDataset
					.getSeriesCount(); seriesIndex++) {

				CandleSeries series = candleDataset.getSeries(seriesIndex);
				Tradestrategy indicatorTradestrategy = getIndicatorTradestrategy(
						tradestrategy, series);
				candleDataset.setSeries(seriesIndex, indicatorTradestrategy
						.getStrategyData().getBaseCandleSeries());
				if (!indicatorRequests.containsKey(indicatorTradestrategy
						.getIdTradeStrategy())) {
					if (this.brokerModel.isConnected()
							|| this.brokerModel.isBrokerDataOnly()) {
						indicatorRequests.put(
								indicatorTradestrategy.getIdTradeStrategy(),
								indicatorTradestrategy);
						tradingday.addTradestrategy(indicatorTradestrategy);
						addedIndicator = true;
					}
				}
			}
		}

		return addedIndicator;
	}

	/**
	 * Method hasSubmittedInSeconds. Make sure no more than six requests every 2
	 * seconds.
	 * 
	 * 162 - Historical Market Data Service error message: Historical data
	 * request pacing violation
	 * 
	 * The following conditions can cause a pacing violation:
	 * 
	 * Making identical historical data requests within 15 seconds;
	 * 
	 * Making six or more historical data requests for the same Contract,
	 * Exchange and Tick Type within two seconds.
	 * 
	 * Also, observe the following limitation when requesting historical data:
	 * 
	 * Do not make more than 60 historical data requests in any ten-minute
	 * period.
	 * 
	 * @throws InterruptedException
	 */
	private void hasSubmittedInSeconds() throws InterruptedException {

		this.submitTimes.addFirst(new Long(System.currentTimeMillis()));

		if (this.submitTimes.size() == 5 && this.brokerModel.isConnected()) {

			if ((this.submitTimes.getFirst() - this.submitTimes.getLast()) < (TIME_BETWEEN_SUBMIT * 1000)) {
				_log.debug("hasSubmittedInSeconds 5 in: "
						+ ((this.submitTimes.getFirst() - this.submitTimes
								.getLast()) / 1000d));
				timerRunning = new AtomicInteger(0);
				timer.start();
				synchronized (lockCoreUtilsTest) {
					while (((this.submitTimes.getFirst() - this.submitTimes
							.getLast()) + timerRunning.get()) < (TIME_BETWEEN_SUBMIT * 1000)
							&& !this.isCancelled()) {
						_log.debug("Please wait "
								+ (TIME_BETWEEN_SUBMIT - (timerRunning.get() / 1000))
								+ " seconds.");
						lockCoreUtilsTest.wait();
					}
				}
				timer.stop();
			}
			this.submitTimes.removeLast();
		}
	}

	/**
	 * Method reProcessTradingdays. Every reSumbittedAt value submitted
	 * contracts try to run any that could not be run due to a conflict. Run
	 * then in asc date order value.
	 * 
	 * @param runningContractRequests
	 *            ConcurrentHashMap<Integer, Tradingday>
	 * @param totalSumbitted
	 *            int
	 * @return int
	 * @throws Exception
	 */

	private int reProcessTradingdays(Tradingdays tradingdays,
			ConcurrentHashMap<Integer, Tradingday> runningContractRequests,
			int totalSumbitted) throws Exception {

		while (!this.isCancelled() && !runningContractRequests.isEmpty()) {

			/*
			 * If nothing submitted wait for all the processes to finish.
			 * Usually means we are submitting identical contracts.
			 */
			if (!this.isCancelled()) {
				synchronized (this.brokerModel.getHistoricalData()) {
					while (this.brokerModel.getHistoricalData().size() > 0) {
						this.brokerModel.getHistoricalData().wait();
						int percent = (int) (((double) (totalSumbitted - this.brokerModel
								.getHistoricalData().size()) / getGrandTotal()) * 100d);
						setProgress(percent);
					}
				}
			}

			for (Tradingday item : tradingdays.getTradingdays()) {
				for (Integer idTradeingday : runningContractRequests.keySet()) {
					Tradingday reProcessTradingday = runningContractRequests
							.get(idTradeingday);
					if (item.equals(reProcessTradingday)) {
						totalSumbitted = processTradingday(
								getTradingdayToProcess(reProcessTradingday,
										runningContractRequests),
								totalSumbitted);
						break;
					}
				}
			}
		}
		return totalSumbitted;
	}

	/**
	 * Method process.This method process the publish method from
	 * doInBackground().
	 * 
	 * @param messages
	 *            List<String>
	 */
	protected void process(List<String> messages) {
		String message = messages.get(messages.size() - 1);
		this.firePropertyChange("information", new String("OK"), message);
	}

	public void done() {
		contractRequests.clear();
		indicatorRequests.clear();
		String message = "Completed Historical data total contracts processed: "
				+ this.getGrandTotal()
				+ " in : "
				+ ((System.currentTimeMillis() - this.startTime) / 1000)
				+ " Seconds.";
		this.firePropertyChange("information", new String("OK"), message);
	}

	/**
	 * Method getIndicatorTradestrategy. For any child indicators that are
	 * candle based create a Tradestrategy that will get the data. If this
	 * tradestrategy already exist share this with any other tradestrategy that
	 * requires this.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @param series
	 *            CandleSeries
	 * @return Tradestrategy
	 * @throws BrokerModelException
	 * @throws PersistentModelException
	 * @throws CloneNotSupportedException
	 */
	private Tradestrategy getIndicatorTradestrategy(
			Tradestrategy tradestrategy, CandleSeries series)
			throws BrokerModelException, PersistentModelException,
			CloneNotSupportedException {

		Tradestrategy indicatorTradestrategy = null;
		for (Tradestrategy indicator : indicatorRequests.values()) {
			if (indicator.getContract().equals(series.getContract())
					&& indicator.getTradingday().equals(
							tradestrategy.getTradingday())
					&& indicator.getBarSize()
							.equals(tradestrategy.getBarSize())
					&& indicator.getChartDays().equals(
							tradestrategy.getChartDays())
					&& indicator.getPortfolio().equals(
							tradestrategy.getPortfolio())) {
				indicatorTradestrategy = indicator;
				break;
			}
		}
		if (null == indicatorTradestrategy) {
			Contract contract = series.getContract();
			if (null == series.getContract().getIdContract()) {
				contract = this.tradePersistentModel.findContractByUniqueKey(
						series.getContract().getSecType(), series.getContract()
								.getSymbol(), series.getContract()
								.getExchange(), series.getContract()
								.getCurrency(), series.getContract()
								.getExpiry());
				if (null == contract) {
					contract = this.tradePersistentModel.persistAspect(series
							.getContract());
				}
			}
			indicatorTradestrategy = new Tradestrategy(contract,
					tradestrategy.getTradingday(), new Strategy("Indicator"),
					tradestrategy.getPortfolio(), new BigDecimal(0), null,
					null, false, tradestrategy.getChartDays(),
					tradestrategy.getBarSize());
			indicatorTradestrategy.setIdTradeStrategy(this.brokerModel
					.getNextRequestId());
			indicatorTradestrategy.setDirty(false);
		}
		if (null == indicatorTradestrategy.getStrategyData()) {
			indicatorTradestrategy.setStrategyData(StrategyData
					.create(indicatorTradestrategy));
		}

		CandleSeries childSeries = indicatorTradestrategy.getStrategyData()
				.getBaseCandleSeries();
		childSeries.setDisplaySeries(series.getDisplaySeries());
		childSeries.setSeriesRGBColor(series.getSeriesRGBColor());
		childSeries.setSubChart(series.getSubChart());
		childSeries.setSymbol(series.getSymbol());
		childSeries.setSecType(series.getSecType());
		childSeries.setCurrency(series.getCurrency());
		childSeries.setExchange(series.getExchange());

		return indicatorTradestrategy;
	}

	/**
	 * Method processTradingday.
	 * 
	 * @param tradingday
	 *            Tradingday
	 * @param totalSumbitted
	 *            int
	 * @return int
	 * @throws InterruptedException
	 * @throws BrokerModelException
	 */
	private int processTradingday(Tradingday tradingday, int totalSumbitted)
			throws BrokerModelException, InterruptedException {

		if (tradingday.getTradestrategies().isEmpty())
			return totalSumbitted;

		for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {

			if (!this.brokerModel.isRealtimeBarsRunning(tradestrategy)) {

				/*
				 * Fire all the requests to TWS to get chart data After data has
				 * been retrieved save the data Only allow a maximum of 60
				 * requests in a 10min period to avoid TWS pacing errors
				 */
				totalSumbitted = submitBrokerRequest(tradestrategy,
						tradingday.getClose(), totalSumbitted);
			}
		}

		return totalSumbitted;
	}

	/**
	 * Method getGrandTotal.
	 * 
	 * @return int
	 */
	private int getGrandTotal() {
		return this.grandTotal;
	}

	/**
	 * Method incrementGrandTotal.
	 * 
	 */
	private void incrementGrandTotal() {
		this.grandTotal++;
	}

	/**
	 * Method getTradingdayToProcess. Get a tradingdays worth of strategies that
	 * have contracts with many tradestrategies. If the contract is already
	 * running add it to the set to be reprocessed later.
	 * 
	 * @param tradingday
	 *            Tradingday
	 * 
	 * @param runningContractRequests
	 *            ConcurrentHashMap<Integer, Tradingday>
	 * 
	 * @return Tradingday
	 * @throws CloneNotSupportedException
	 */

	private Tradingday getTradingdayToProcess(Tradingday tradingday,
			ConcurrentHashMap<Integer, Tradingday> runningContractRequests)
			throws CloneNotSupportedException {

		if (tradingday.getTradestrategies().isEmpty())
			return tradingday;

		Collections.sort(tradingday.getTradestrategies(),
				Tradestrategy.TRADINGDAY_CONTRACT);
		Tradingday reProcessTradingday = null;
		if (runningContractRequests.containsKey(tradingday.getIdTradingDay())) {
			reProcessTradingday = runningContractRequests.get(tradingday
					.getIdTradingDay());
		} else {
			reProcessTradingday = (Tradingday) tradingday.clone();
		}
		Tradingday toProcessTradingday = (Tradingday) tradingday.clone();
		Contract currContract = null;

		for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
			if (this.brokerModel.isHistoricalDataRunning(tradestrategy
					.getContract())) {
				if (!reProcessTradingday.existTradestrategy(tradestrategy))
					reProcessTradingday.addTradestrategy(tradestrategy);
			} else {
				if (tradestrategy.getContract().equals(currContract)) {
					if (!reProcessTradingday.existTradestrategy(tradestrategy))
						reProcessTradingday.addTradestrategy(tradestrategy);
				} else {
					currContract = tradestrategy.getContract();
					toProcessTradingday.addTradestrategy(tradestrategy);
				}
			}
		}

		for (Tradestrategy tradestrategy : toProcessTradingday
				.getTradestrategies()) {
			if (reProcessTradingday.existTradestrategy(tradestrategy))
				reProcessTradingday.removeTradestrategy(tradestrategy);
		}
		if (reProcessTradingday.getTradestrategies().isEmpty()) {
			runningContractRequests.remove(reProcessTradingday
					.getIdTradingDay());
		}
		if (!reProcessTradingday.getTradestrategies().isEmpty()) {
			runningContractRequests.put(reProcessTradingday.getIdTradingDay(),
					reProcessTradingday);
		}
		return toProcessTradingday;
	}

	/**
	 * Method Calculate the total number of tradestrategies to be processed.
	 * This will be all the tradestrategies plus all the indicators that are of
	 * type candleSeries plus all the tradestrategies that are on a lower
	 * timeframe. i.e trade.backtest.barSize is less than tradestrategy barSize.
	 * 
	 * Also find all the unique contract symbols. This is used to insure we only
	 * process contract details once per contract.
	 * 
	 * @param startTime
	 *            long
	 * 
	 * @return Integer The total number of tradestrategies to process.
	 */

	private Integer calculateTotalTradestrategiesToProcess(long startTime) {

		Integer total = new Integer(0);
		ConcurrentHashMap<String, Contract> contracts = new ConcurrentHashMap<String, Contract>();

		for (Tradingday tradingday : this.tradingdays.getTradingdays()) {

			/*
			 * Total for tradestrategies.
			 */
			total = total + tradingday.getTradestrategies().size();

			if (this.brokerModel.isBrokerDataOnly()
					|| this.brokerModel.isConnected()) {
				/*
				 * If we are getting broker data only () or we are connected (to
				 * a broker interface)we will have indicators to get, contract
				 * details and data on lower time frames if the backTestBarSize
				 * is greater than zero.
				 */
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					/*
					 * Refresh the data set container as these may have changed.
					 */
					tradestrategy.setStrategyData(StrategyData
							.create(tradestrategy));
					CandleDataset candleDataset = (CandleDataset) tradestrategy
							.getStrategyData().getIndicatorByType(
									IndicatorSeries.CandleSeries);

					if (null != candleDataset) {
						for (int seriesIndex = 0; seriesIndex < candleDataset
								.getSeriesCount(); seriesIndex++) {
							CandleSeries series = candleDataset
									.getSeries(seriesIndex);
							Contract contract = series.getContract();
							/*
							 * Add the contract requests this allows us to only
							 * request contract details once per contract in the
							 * range of tradingdays to be processed.
							 */
							if (!contractRequests.containsKey(contract
									.getSymbol()))
								contractRequests.put(contract.getSymbol(),
										contract);
							/*
							 * Total for indicator contracts
							 */
							if (!contracts.containsKey(contract.getSymbol()))
								contracts.put(contract.getSymbol(), contract);
						}
					}
					/*
					 * Add the contract requests this allows us to only request
					 * contract details once per contract in the range of
					 * tradingdays to be processed.
					 */
					if (!contractRequests.containsKey(tradestrategy
							.getContract().getSymbol()))
						contractRequests.put(tradestrategy.getContract()
								.getSymbol(), tradestrategy.getContract());

				}

				/*
				 * Total for indicator contracts
				 */
				total = total + contracts.size();
				contracts.clear();
				/*
				 * Get the total for lower barsize timeframes.
				 */
				if (backTestBarSize > 0) {
					if (TradingCalendar.isTradingDay(tradingday.getOpen())
							&& TradingCalendar
									.sameDay(
											tradingday.getOpen(),
											TradingCalendar
													.getZonedDateTimeFromMilli(startTime))
							&& !TradingCalendar.isAfterHours(TradingCalendar
									.getZonedDateTimeFromMilli(startTime)))
						continue;

					for (Tradestrategy tradestrategy : tradingday
							.getTradestrategies()) {
						if (backTestBarSize < tradestrategy.getBarSize())
							total++;

						if (null == tradestrategy.getStrategyData()) {
							tradestrategy.setStrategyData(StrategyData
									.create(tradestrategy));
						}
						CandleDataset candleDataset = (CandleDataset) tradestrategy
								.getStrategyData().getIndicatorByType(
										IndicatorSeries.CandleSeries);

						if (null != candleDataset) {
							for (int seriesIndex = 0; seriesIndex < candleDataset
									.getSeriesCount(); seriesIndex++) {
								CandleSeries series = candleDataset
										.getSeries(seriesIndex);
								Contract contract = series.getContract();

								/*
								 * Total for indicator contracts
								 */
								if (!contracts
										.containsKey(contract.getSymbol()))
									contracts.put(contract.getSymbol(),
											contract);
							}
						}
					}
					/*
					 * Total for indicator contracts
					 */
					total = total + contracts.size();
					contracts.clear();
				}
			}
		}
		return total;
	}
}
