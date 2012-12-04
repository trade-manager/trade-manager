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
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.broker.BrokerModelException;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Tradestrategy;

public class ClientSocket {

	private final static Logger _log = LoggerFactory
			.getLogger(ClientSocket.class);

	private static final ConcurrentHashMap<Integer, BackTestBroker> m_backTestBroker = new ConcurrentHashMap<Integer, BackTestBroker>();
	private ClientWrapper m_client = null;
	private static final SimpleDateFormat m_sdfGMT = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss z");

	public ClientSocket(ClientWrapper client) {
		m_client = client;
	}

	/**
	 * Method reqHistoricalData.
	 * 
	 * @param reqId
	 *            int
	 * @param ibContract
	 *            com.ib.client.Contract
	 * @param endDateTime
	 *            String
	 * @param durationStr
	 *            String
	 * @param barSizeSetting
	 *            String
	 * @param whatToShow
	 *            String
	 * @param useRTH
	 *            int
	 * @param formatDateInteger
	 *            int
	 * @throws BrokerModelException
	 */
	public void reqHistoricalData(int reqId, Contract contract,
			String endDateTime, String durationStr, String barSizeSetting,
			String whatToShow, int useRTH, int formatDateInteger)
			throws BrokerModelException {

		try {

			if (null != endDateTime) {
				Date endDate = m_sdfGMT.parse(endDateTime);
				ChartDays chartDays = ChartDays.newInstance();
				chartDays.setDisplayName(durationStr);

				BarSize barSize = BarSize.newInstance();
				barSize.setDisplayName(barSizeSetting);

				Date startDate = TradingCalendar.addBusinessDays(endDate,
						(Integer.parseInt(chartDays.getCode()) - 1) * -1);
				startDate = TradingCalendar.getMostRecentTradingDay(startDate);
				startDate = TradingCalendar.getSpecificTime(startDate, 0, 0);

				_log.info(" Start Date: " + startDate + " End Date: " + endDate
						+ " BarSize: " + barSize.getCode() + " ChartDays: "
						+ chartDays.getCode());

				if (BarSize.DAY == Integer.parseInt(barSize.getCode())) {
					this.getYahooPriceDataDay(reqId, contract.getSymbol(),
							startDate, endDate);
				} else {
					this.getYahooPriceDataIntraday(reqId, contract.getSymbol(),
							Integer.parseInt(chartDays.getCode()), startDate);
				}
			} else {
				for (Tradestrategy tradestrategy : contract
						.getTradestrategies()) {
					if (tradestrategy.getTrade()) {
						BackTestBroker backTestBroker = new BackTestBroker(
								tradestrategy.getDatasetContainer(),
								tradestrategy.getIdTradeStrategy(), m_client);
						m_backTestBroker.put(
								tradestrategy.getIdTradeStrategy(),
								backTestBroker);
						backTestBroker.execute();
					}
				}
			}
			m_client.historicalData(reqId, "finished- at yyyyMMdd HH:mm:ss", 0,
					0, 0, 0, 0, 0, 0, false);

		} catch (Exception ex) {
			throw new BrokerModelException(0, 6000,
					"Error initializing BackTestBroker Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method removeBackTestBroker.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 */

	public void removeBackTestBroker(Integer idTradestrategy) {
		synchronized (m_backTestBroker) {
			BackTestBroker backTestBroker = m_backTestBroker
					.get(idTradestrategy);
			if (null != backTestBroker) {
				if (backTestBroker.isDone() || backTestBroker.isCancelled()) {
					m_backTestBroker.remove(idTradestrategy);
				}
			}
		}
	}

	/**
	 * Method getBackTestBroker.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * 
	 * @return BackTestBroker
	 */

	public BackTestBroker getBackTestBroker(Integer idTradestrategy) {
		return m_backTestBroker.get(idTradestrategy);
	}

	/**
	 * Method reqContractDetails.
	 * 
	 * @param reqId
	 *            int
	 * @param ibContract
	 *            com.ib.client.Contract
	 * @throws BrokerModelException
	 */
	public void reqContractDetails(int reqId, Contract contract)
			throws BrokerModelException {
		try {
			Contract contractDetails = getYahooContractDetails(reqId,
					contract.getSymbol());

			m_client.contractDetails(reqId, contractDetails);
		} catch (Exception ex) {
			throw new BrokerModelException(0, 6000,
					"Error initializing BackTestBroker Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method getBackTestBroker.
	 * 
	 * @param reqId
	 *            int
	 * @param contract
	 *            Contract
	 * @param barSize
	 *            int
	 * @param whatToShow
	 *            String
	 * @param useRTH
	 *            boolean
	 */
	public void reqRealTimeBars(int reqId, Contract contract, int barSize,
			String whatToShow, boolean useRTH) {
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
	private Contract getYahooContractDetails(int reqId, String symbol)
			throws IOException {

		/*
		 * Yahoo finance http://finance.yahoo.com/d/quotes.csv?s=XOM&f=n
		 */
		Contract contractDetails = new Contract();

		String strUrl = "http://finance.yahoo.com/d/quotes.csv?s=" + symbol
				+ "&f=n";

		// _log.info("URL : " + strUrl);
		URL url = new URL(strUrl);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			StringTokenizer scanLine = new StringTokenizer(inputLine, ",");
			while (scanLine.hasMoreTokens()) {
				contractDetails.setDescription(scanLine.nextToken().replaceAll(
						"\"", ""));
			}
		}
		in.close();
		return contractDetails;
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
	private void getYahooPriceDataIntraday(int reqId, String symbol,
			int chartDays, Date startDate) throws IOException {

		/*
		 * Yahoo finance http://chartapi.finance.yahoo.com/instrument/1.0/IBM
		 * /chartdata;type=quote;range=1d/csv/
		 */
		int days = TradingCalendar.daysDiff(startDate, new Date());
		String strUrl = "http://chartapi.finance.yahoo.com/instrument/1.0/"
				+ symbol + "/chartdata;type=quote;range=" + days + "d/csv/";

		// _log.info("URL : " + strUrl);
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

					if (startDate.before(time)) {
						m_client.historicalData(reqId, dateString, open, high,
								low, close, ((int) volume / 100),
								((int) volume / 100), (open + close) / 2, false);
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
	private void getYahooPriceDataDay(int reqId, String symbol, Date startDate,
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
			Candle candle = new Candle(null, open, high, low, close,
					(volume / 100), (open + close) / 2, ((int) volume / 100),
					new Date());
			candle.setStartPeriod(time);
			candle.setPeriod(time.toString());
			candle.setEndPeriod(TradingCalendar.addSeconds(
					TradingCalendar.getSpecificTime(endDate, time), -1));
			candle.setLastUpdateDate(candle.getStartPeriod());
			candles.add(candle);

		}
		in.close();
		Collections.reverse(candles);
		for (Candle candle : candles) {
			m_client.historicalData(reqId, String.valueOf(candle
					.getStartPeriod().getTime() / 1000), candle.getOpen()
					.doubleValue(), candle.getHigh().doubleValue(), candle
					.getLow().doubleValue(), candle.getClose().doubleValue(),
					candle.getVolume().intValue(), candle.getTradeCount(),
					candle.getVwap().doubleValue(), false);
		}
	}
}
