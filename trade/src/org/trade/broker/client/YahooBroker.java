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
package org.trade.broker.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.strategy.data.candle.CandlePeriod;

/**
 */
public class YahooBroker extends SwingWorker<Void, Void> {

	private final static Logger _log = LoggerFactory
			.getLogger(YahooBroker.class);

	private Integer reqId = null;
	private Contract contract = null;
	private String durationStr = null;
	private String barSizeSetting = null;
	private String endDateTime = null;
	private ClientWrapper brokerModel = null;
	private static final SimpleDateFormat _sdfLocal = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss");

	/**
	 * Constructor for YahooBroker.
	 * 
	 * @param contract
	 *            Contract
	 * @param endDateTime
	 *            String
	 * @param durationStr
	 *            String
	 * @param barSizeSetting
	 *            String
	 * @param brokerModel
	 *            ClientWrapper
	 * 
	 */
	public YahooBroker(Integer reqId, Contract contract, String endDateTime,
			String durationStr, String barSizeSetting, ClientWrapper brokerModel) {
		this.reqId = reqId;
		this.contract = contract;
		this.barSizeSetting = barSizeSetting;
		this.durationStr = durationStr;
		this.endDateTime = endDateTime;
		this.brokerModel = brokerModel;
	}

	/**
	 * Method doInBackground.
	 * 
	 * @return Void
	 */
	public Void doInBackground() {

		try {
			setYahooContractDetails(contract);

			this.brokerModel
					.contractDetails(contract.getIdContract(), contract);

			Date endDate = _sdfLocal.parse(this.endDateTime);
			ChartDays chartDays = ChartDays.newInstance();
			chartDays.setDisplayName(this.durationStr);

			BarSize barSize = BarSize.newInstance();
			barSize.setDisplayName(this.barSizeSetting);

			Date startDate = TradingCalendar.addDays(endDate,
					(Integer.parseInt(chartDays.getCode()) - 1) * -1);
			startDate = TradingCalendar.getMostRecentTradingDay(startDate);
			startDate = TradingCalendar.getSpecificTime(startDate, 0, 0);

			_log.info("YahooBroker.doInBackground Start Date: " + startDate
					+ " End Date: " + endDate + " BarSize: "
					+ barSize.getCode() + " ChartDays: " + chartDays.getCode());

			if (BarSize.DAY == Integer.parseInt(barSize.getCode())) {
				this.setYahooPriceDataDay(this.reqId,
						this.contract.getSymbol(), startDate, endDate);
			} else {
				this.setYahooPriceDataIntraday(this.reqId,
						this.contract.getSymbol(),
						Integer.parseInt(chartDays.getCode()), startDate,
						endDate);
			}
			this.brokerModel.historicalData(this.contract.getIdContract(),
					"finished- at yyyyMMdd HH:mm:ss", 0, 0, 0, 0, 0, 0, 0,
					false);

		} catch (Exception ex) {
			_log.error("Error YahooBroker Symbol: " + contract.getSymbol()
					+ " Msg: " + ex.getMessage(), ex);
		}
		return null;
	}

	public void done() {
		brokerModel.onCancelRealtimeBars(this.contract);
		brokerModel.onCancelBrokerData(this.contract);
		_log.info("YahooBroker done for: " + contract.getSymbol());
	}

	/**
	 * Method getYahooContractDetails.
	 * 
	 * @param reqId
	 *            int
	 * @param symbol
	 *            String
	 * @return ContractDetails
	 * @throws IOException
	 */
	private void setYahooContractDetails(Contract contract) throws IOException {

		/*
		 * Yahoo finance http://finance.yahoo.com/d/quotes.csv?s=XOM&f=n
		 */
		String strUrl = "http://finance.yahoo.com/d/quotes.csv?s="
				+ contract.getSymbol() + "&f=n";

		// _log.info("URL : " + strUrl);
		URL url = new URL(strUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			StringTokenizer scanLine = new StringTokenizer(inputLine, ",");
			while (scanLine.hasMoreTokens()) {
				contract.setDescription(scanLine.nextToken().replaceAll("\"",
						""));
			}
		}
		in.close();
	}

	/**
	 * Method getYahooPriceDataIntraday.
	 * 
	 * @param reqId
	 *            int
	 * @param symbol
	 *            String
	 * @param chartDays
	 *            int
	 * @param startDate
	 *            Date
	 * @throws IOException
	 */
	private void setYahooPriceDataIntraday(int reqId, String symbol,
			int chartDays, Date startDate, Date endDate) throws IOException {

		/*
		 * Yahoo finance http://chartapi.finance.yahoo.com/instrument/1.0/IBM
		 * /chartdata;type=quote;range=1d/csv/
		 */
		int days = TradingCalendar.daysDiff(startDate, new Date());
		String strUrl = "http://chartapi.finance.yahoo.com/instrument/1.0/"
				+ symbol + "/chartdata;type=quote;range=" + days + "d/csv/";

		_log.info("URL : " + strUrl);
		URL url = new URL(strUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		in.readLine();
		while ((inputLine = in.readLine()) != null) {

			if (inputLine.indexOf(":") == -1) {
				StringTokenizer scanLine = new StringTokenizer(inputLine, ",");
				while (scanLine.hasMoreTokens()) {
					String dateString = scanLine.nextToken();
					Date time = new Date(Long.parseLong(dateString) * 1000);
					// values:Timestamp,close,high,low,open,volume
					double close = Double.parseDouble(scanLine.nextToken());
					double high = Double.parseDouble(scanLine.nextToken());
					double low = Double.parseDouble(scanLine.nextToken());
					double open = Double.parseDouble(scanLine.nextToken());
					long volume = Long.parseLong(scanLine.nextToken());
					// _log.info("Time : " + time + " Open: " + open + " High: "
					// + high + " Low: " + low + " Close: " + close
					// + " Volume: " + volume);

					if ((time.after(startDate) || time.equals(startDate))
							&& time.before(endDate)) {
						this.brokerModel
								.historicalData(reqId, dateString, open, high,
										low, close, ((int) volume / 100),
										((int) volume / 100),
										(open + close) / 2, false);
					}
				}
			}
		}
		in.close();
	}

	/**
	 * Method getYahooPriceDataDay.
	 * 
	 * @param reqId
	 *            int
	 * @param symbol
	 *            String
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @throws IOException
	 * @throws ParseException
	 */
	private void setYahooPriceDataDay(int reqId, String symbol, Date startDate,
			Date endDate) throws IOException, ParseException {

		/*
		 * Yahoo finance So IBM form 1/1/2012 thru 06/30/2012
		 * http://ichart.finance .yahoo.com/table.csv?s=IBM&a=0&b=1&c=2012&d=5
		 * &e=30&f=2012&ignore=.csv"
		 */
		DateFormat df = new SimpleDateFormat("y-M-d");
		List<Candle> candles = new ArrayList<Candle>();

		String strUrl = "http://ichart.finance.yahoo.com/table.csv?s=" + symbol
				+ "&a=" + TradingCalendar.getMonth(startDate) + "&b="
				+ TradingCalendar.getDayOfMonth(startDate) + "&c="
				+ TradingCalendar.getYear(startDate) + "&d="
				+ TradingCalendar.getMonth(endDate) + "&e="
				+ TradingCalendar.getDayOfMonth(endDate) + "&f="
				+ TradingCalendar.getYear(endDate) + "&ignore=.csv";

		// _log.info(strUrl);
		URL url = new URL(strUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		in.readLine();
		while ((inputLine = in.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(inputLine, ",");
			Date time = TradingCalendar.getSpecificTime(startDate,
					df.parse(st.nextToken()));
			double open = Double.parseDouble(st.nextToken());
			double high = Double.parseDouble(st.nextToken());
			double low = Double.parseDouble(st.nextToken());
			double close = Double.parseDouble(st.nextToken());
			long volume = Long.parseLong(st.nextToken());
			// double adjClose = Double.parseDouble( st.nextToken() );
			// _log.info("Time : " + time + " Open: " + open + " High: "
			// + high + " Low: " + low + " Close: " + close
			// + " Volume: " + volume);

			CandlePeriod period = new CandlePeriod(time,
					TradingCalendar.addSeconds(
							TradingCalendar.getSpecificTime(endDate, time), -1));

			Candle candle = new Candle(null, period, open, high, low, close,
					(volume / 100), (open + close) / 2, ((int) volume / 100),
					new Date());

			candle.setLastUpdateDate(candle.getStartPeriod());
			candles.add(candle);

		}
		in.close();
		Collections.reverse(candles);
		for (Candle candle : candles) {
			this.brokerModel.historicalData(reqId, String.valueOf(candle
					.getStartPeriod().getTime() / 1000), candle.getOpen()
					.doubleValue(), candle.getHigh().doubleValue(), candle
					.getLow().doubleValue(), candle.getClose().doubleValue(),
					candle.getVolume().intValue(), candle.getTradeCount(),
					candle.getVwap().doubleValue(), false);
		}
	}
}
