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
package org.trade.persistent.dao;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import org.trade.core.dao.Aspect;
import org.trade.core.lookup.DBTableLookupServiceProvider;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.BarSize;
import org.trade.dictionary.valuetype.ChartDays;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOPortfolio;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.Tier;
import org.trade.persistent.PersistentModelException;

/**
 */
public class Tradingdays extends Aspect implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3388042483785305102L;
	// private ConcurrentHashMap<Date, Tradingday> tradingdays = new
	// ConcurrentHashMap<Date, Tradingday>(
	// 0);
	private List<Tradingday> tradingdays = Collections.synchronizedList(new ArrayList<Tradingday>(0));

	public Tradingdays() {
	}

	/**
	 * Constructor for Tradingdays.
	 * 
	 * @param idTradingdays
	 *            Integer
	 */
	public Tradingdays(Integer idTradingdays) {
		this.id = idTradingdays;
	}

	/**
	 * Constructor for Tradingdays.
	 * 
	 * @param idTradingdays
	 *            Integer
	 * @param tradingdays
	 *            List<Tradingday>
	 */
	public Tradingdays(Integer idTradingdays, List<Tradingday> tradingdays) {
		this.id = idTradingdays;
		for (Tradingday instance : tradingdays) {
			this.tradingdays.add(instance);
		}
	}

	/**
	 * Method getIdTradingdays.
	 * 
	 * @return Integer
	 */
	public Integer getIdTradingdays() {
		return this.id;
	}

	/**
	 * Method setIdTradingdays.
	 * 
	 * @param idTradingdays
	 *            Integer
	 */
	public void setIdTradingdays(Integer idTradingdays) {
		this.id = idTradingdays;
	}

	/**
	 * Method add.
	 * 
	 * @param tradingday
	 *            Tradingday
	 */
	public void add(Tradingday tradingday) {
		this.tradingdays.add(tradingday);
	}

	/**
	 * Method remove.
	 * 
	 * @param tradingday
	 *            Tradingday
	 */
	public void remove(Tradingday tradingday) {
		synchronized (this.tradingdays) {
			for (ListIterator<Tradingday> itemIter = this.tradingdays.listIterator(); itemIter.hasNext();) {
				Tradingday item = itemIter.next();
				if (item.equals(tradingday)) {
					itemIter.remove();
					break;
				}
			}
		}
	}

	/**
	 * Method remove.
	 * 
	 * @param open
	 *            ZonedDateTime
	 * 
	 * @param close
	 *            ZonedDateTime
	 */
	public void remove(ZonedDateTime open, ZonedDateTime close) {
		synchronized (this.tradingdays) {
			for (ListIterator<Tradingday> itemIter = this.tradingdays.listIterator(); itemIter.hasNext();) {
				Tradingday item = itemIter.next();
				if (item.getOpen().compareTo(open) == 0 && item.getClose().compareTo(close) == 0) {
					itemIter.remove();
					break;
				}
			}
		}
	}

	/**
	 * Method getTradingdays.
	 * 
	 * @return ConcurrentHashMap<Date,Tradingday>
	 */
	public List<Tradingday> getTradingdays() {
		return this.tradingdays;
	}

	/**
	 * Method setTradingdays.
	 * 
	 * @param tradingdays
	 *            ConcurrentHashMap<Date,Tradingday>
	 */
	public void setTradingdays(List<Tradingday> tradingdays) {
		this.tradingdays = Collections.synchronizedList(tradingdays);
	}

	/**
	 * Method getTradingday.
	 * 
	 * @param open
	 *            Date
	 * @param close
	 *            Date
	 * @return Tradingday
	 */
	public Tradingday getTradingday(ZonedDateTime open, ZonedDateTime close) {
		synchronized (this.tradingdays) {
			for (Tradingday tradingday : this.tradingdays) {
				if (tradingday.getOpen().compareTo(open) == 0 && tradingday.getClose().compareTo(close) == 0)
					return tradingday;
			}
		}
		return null;
	}

	/**
	 * Method containsTradingday.
	 * 
	 * @param open
	 *            Date
	 * @param close
	 *            Date
	 * @return boolean
	 */
	public boolean containsTradingday(Tradingday tradingday) {
		synchronized (this.tradingdays) {
			for (Tradingday item : this.tradingdays) {
				if (item.equals(tradingday))
					return true;
			}
		}
		return false;
	}

	/**
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	public boolean isDirty() {
		synchronized (this.tradingdays) {
			for (Tradingday tradingday : this.tradingdays) {
				if (tradingday.isDirty())
					return true;
			}
		}
		return false;
	}

	/**
	 * Method getContract.
	 * 
	 * @param symbol
	 *            String
	 * @return Contract
	 */
	public Contract getContract(String symbol) {
		Contract contract = null;
		synchronized (this.tradingdays) {
			for (Tradingday tradingday : this.tradingdays) {
				for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
					if (tradestrategy.getContract().getSymbol().equals(symbol)) {
						contract = tradestrategy.getContract();
						break;
					}
				}
			}
		}
		return contract;
	}

	/**
	 * Method getTradestrategy.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * @return Tradestrategy
	 */
	public Tradestrategy getTradestrategy(Integer idTradestrategy) {
		synchronized (this.tradingdays) {
			for (Tradingday tradingday : this.tradingdays) {
				for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
					if (tradestrategy.getIdTradeStrategy().equals(idTradestrategy)) {
						return tradestrategy;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Method replaceTradingday.
	 * 
	 * @param newTradingday
	 *            Tradingday
	 */
	public void replaceTradingday(Tradingday newTradingday) {
		synchronized (this.tradingdays) {
			for (ListIterator<Tradingday> itemIter = this.tradingdays.listIterator(); itemIter.hasNext();) {
				Tradingday item = itemIter.next();
				if (item.equals(newTradingday)) {
					itemIter.set(newTradingday);
					break;
				}
			}
		}
	}

	/**
	 * Method replaceTradestrategy.
	 * 
	 * @param newTradestrategy
	 *            Tradestrategy
	 */
	public void replaceTradestrategy(Tradestrategy newTradestrategy) {
		synchronized (this.tradingdays) {
			for (Tradingday tradingday : this.tradingdays) {
				for (ListIterator<Tradestrategy> itemIter = tradingday.getTradestrategies().listIterator(); itemIter
						.hasNext();) {
					Tradestrategy tradestrategy = itemIter.next();
					if (tradestrategy.equals(newTradestrategy)) {
						itemIter.set(newTradestrategy);
					}
				}
			}
		}
	}

	/**
	 * Method hasTradestrategies.
	 * 
	 * @param tradingdays
	 *            Tradingdays
	 * @return boolean
	 */
	public static boolean hasTradestrategies(Tradingdays tradingdays) {
		synchronized (tradingdays.getTradingdays()) {
			for (Tradingday tradingday : tradingdays.getTradingdays()) {
				if (!tradingday.getTradestrategies().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Method hasTradeOrders.
	 * 
	 * @param tradingday
	 *            Tradingday
	 * @return boolean
	 */
	public static boolean hasTradeOrders(Tradingday tradingday) {
		for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
			if (!tradestrategy.getTradeOrders().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method hasOpenTrades.
	 * 
	 * @param tradingday
	 *            Tradingday
	 * @return boolean
	 */
	public static boolean hasOpenOrders(Tradingday tradingday) {
		for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
			for (TradeOrder tradeOrder : tradestrategy.getTradeOrders()) {
				if (tradeOrder.isActive()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Method populateDataFromFile.
	 * 
	 * @param fileName
	 *            String
	 * @throws Exception
	 */
	public synchronized void populateDataFromFile(String fileName, Tradingday tradingday) throws Exception {

		/*
		 * CSV file format CSV file format is: DES, Underlying, Sec Type,
		 * Exchange, Expiration (yyyyMM, or yyyyMMdd opt),Strike
		 * Price(opt),PUT/CALL
		 * (opt),Multiplier(opt),|BOT/SLD(opt)|DATE(MM/dd/yyyy) (opt)|
		 * Tier(Opt)| Mkt Gap(opt)| Mkt Bias(opt)| Mkt Bar(opt)");
		 */
		// FileReader fileReader = null;
		// BufferedReader bufferedReader = null;
		/*
		 * Refresh the decode tables.
		 */
		DBTableLookupServiceProvider.clearLookup();
		try (FileReader fileReader = new FileReader(fileName);
				BufferedReader bufferedReader = new BufferedReader(fileReader)) {

			if ((fileName == null) || fileName.equals("")) {
				return;
			}

			Integer chartDays = ConfigProperties.getPropAsInt("trade.backfill.duration");
			if (!ChartDays.newInstance(chartDays).isValid())
				chartDays = new Integer(2);

			String tierDefault = ConfigProperties.getPropAsString("trade.tier.default");
			if (!Tier.newInstance(tierDefault).isValid())
				tierDefault = null;

			Integer barSize = ConfigProperties.getPropAsInt("trade.backfill.barsize");
			if (!BarSize.newInstance(barSize).isValid())
				barSize = new Integer(300);

			Integer riskAmount = ConfigProperties.getPropAsInt("trade.risk");
			String strategyName = ConfigProperties.getPropAsString("trade.strategy.default");
			if (!DAOStrategy.newInstance(strategyName).isValid())
				strategyName = DAOStrategy.newInstance().getCode();

			Strategy strategy = (Strategy) DAOStrategy.newInstance(strategyName).getObject();

			Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance().getObject();
			String strLine = "";

			// read comma separated file line by line

			while ((strLine = bufferedReader.readLine()) != null) {
				Tradestrategy tradestrategy = Tradingdays.parseContractLine(strLine);

				if (null != tradestrategy) {

					Contract contract = this.getContract(tradestrategy.getContract().getSymbol());
					if (null != contract) {
						tradestrategy.setContract(contract);
					}

					if (null == tradestrategy.getTradingday()) {
						if (null == tradingday) {
							throw new PersistentModelException("Please select a Tradingday");
						}
						tradestrategy.setTradingday(tradingday);
					} else {
						Tradingday currTradingday = this.getTradingday(tradestrategy.getTradingday().getOpen(),
								tradestrategy.getTradingday().getClose());

						if (null != currTradingday) {
							if (null != tradestrategy.getTradingday().getMarketGap()
									&& null == currTradingday.getMarketGap()) {
								currTradingday.setMarketGap(tradestrategy.getTradingday().getMarketGap());
							}
							if (null != tradestrategy.getTradingday().getMarketBias()
									&& null == currTradingday.getMarketBias()) {
								currTradingday.setMarketBias(tradestrategy.getTradingday().getMarketBias());
							}
							if (null != tradestrategy.getTradingday().getMarketBar()
									&& null == currTradingday.getMarketBar()) {
								currTradingday.setMarketBar(tradestrategy.getTradingday().getMarketBar());
							}
							tradestrategy.setTradingday(currTradingday);
						}
					}
					/*
					 * Do not load tradestrategies for trading holidays.
					 */
					if (TradingCalendar.isHoliday(tradestrategy.getTradingday().getOpen())) {
						continue;
					}
					tradestrategy.setRiskAmount(new BigDecimal(riskAmount));
					tradestrategy.setBarSize(barSize);
					tradestrategy.setChartDays(chartDays);
					tradestrategy.setTier(tierDefault);
					tradestrategy.setTrade(true);
					tradestrategy.setDirty(true);
					tradestrategy.setStrategy(strategy);
					tradestrategy.setPortfolio(portfolio);
					if (!tradestrategy.getTradingday().existTradestrategy(tradestrategy))
						tradestrategy.getTradingday().addTradestrategy(tradestrategy);

					if (!this.containsTradingday(tradestrategy.getTradingday())) {
						this.add(tradestrategy.getTradingday());
					}
					Collections.sort(tradestrategy.getTradingday().getTradestrategies(), Tradestrategy.DATE_ORDER_ASC);
				}
			}

		} catch (Exception ex) {
			throw new PersistentModelException(1, 200, ex.getMessage());
		}
	}

	/**
	 * Method parseContractLine.
	 * 
	 * @param csvLine
	 *            String
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 * @throws ParseException
	 */
	public static Tradestrategy parseContractLine(String csvLine) throws PersistentModelException, ParseException {
		Tradestrategy tradestrategy = null;
		Contract contract = null;
		Tradingday tradingday = null;

		// break comma separated line using ","
		Scanner scanLine = new Scanner(csvLine);
		scanLine.useDelimiter("\\,");
		int tokenNumber = 0;

		while (scanLine.hasNext()) {
			// display csv values
			tokenNumber++;
			String token = scanLine.next().trim();
			if (token.length() == 0)
				continue;

			switch (tokenNumber) {
			case 1: {
				if ("DES".equals(token.toUpperCase())) {
					tradestrategy = new Tradestrategy();
					contract = new Contract();
					contract.setCurrency(Currency.USD);
					tradestrategy.setContract(contract);
				} else {
					throw new PersistentModelException(1, 200,
							"Incorrect file format! CSV file format is: DES, Underlying, Sec Type, Exchange, Expiration (yyyyMM, or yyyyMMdd opt),Strike Price(opt),PUT/CALL(opt),Multiplier(opt),BOT/SLD(opt)|DATE(MM/dd/yyyy) (opt)| Tier(Opt)| Mkt Bias(opt)| Mkt Bar(opt)| Mkt Gap(opt)");
				}
				break;
			}
			case 2: {
				contract.setSymbol(token.toUpperCase());
				break;
			}
			case 3: {
				contract.setSecType(token.toUpperCase());
				break;
			}
			case 4: {
				if (token.toUpperCase().equals("SMART/ARCA")) {
					contract.setExchange("SMART");
				} else {
					contract.setExchange(token.toUpperCase());
				}
				break;
			}
			case 5: {
				if (token.length() == 6) {
					contract.setExpiry(TradingCalendar.getZonedDateTimeFromDateString(token, "yyyyMM",
							TradingCalendar.MKT_TIMEZONE));
				} else if (token.length() == 8) {
					contract.setExpiry(TradingCalendar.getZonedDateTimeFromDateString(token, "yyyyMMdd",
							TradingCalendar.MKT_TIMEZONE));
				}
				break;
			}
			case 6: {
				// Strike
				break;
			}
			case 7: {
				// Put/Call
				break;
			}
			case 8: {
				// Multiplier
				if (token.length() > 0)
					contract.setPriceMultiplier(new BigDecimal(token));
				break;
			}
			case 9: {
				// Currency
				if (token.length() > 0)
					contract.setCurrency(token);
				break;
			}
			case 10: {
				Scanner custScan = new Scanner(token);
				custScan.useDelimiter("\\|");
				int custTokenNumber = 0;
				while (custScan.hasNext()) {
					// display csv values
					custTokenNumber++;
					String custToken = custScan.next().trim();
					if (custToken.length() == 0)
						continue;

					switch (custTokenNumber) {
					case 1: {
						tradestrategy.setSide(custToken.toUpperCase());
						break;
					}
					case 2: {
						;
						ZonedDateTime todayOpen = TradingCalendar.getTradingDayStart(TradingCalendar
								.getZonedDateTimeFromDateString(custToken, "MM/dd/yyyy", TradingCalendar.MKT_TIMEZONE));
						tradingday = Tradingday.newInstance(todayOpen);
						tradestrategy.setTradingday(tradingday);
						break;
					}
					case 3: {
						tradestrategy.setTier(custToken.toUpperCase());
						break;
					}
					case 4: {
						tradingday.setMarketGap(custToken.toUpperCase());
						break;
					}
					case 5: {
						tradingday.setMarketBias(custToken.toUpperCase());
						break;
					}
					case 6: {
						tradingday.setMarketBar(custToken.toUpperCase());
						break;
					}
					default: {
					}
					}
				}
				custScan.close();
				break;
			}
			default: {
			}
			}
		}
		scanLine.close();
		return tradestrategy;
	}

	/**
	 * Method main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		String inputFileDef = "db/CreateLoadFileDef.csv";
		String outPutFileName = "C:\\Temp\\CCILoadFile.csv";
		Tradingdays.createLoadFile(inputFileDef, outPutFileName);

	}

	/**
	 * Method createLoadFile. Create a file that cn be inported into the
	 * tradeManager for the symbols specified in the input file.
	 * 
	 * CSV file format CSV file format is: 01/01/2013 (From date),01/01/2014 (To
	 * Date),DES, Underlying, Sec Type, Exchange, AAPL, AMZN e.t.c comma
	 * separated list of stocks.
	 * 
	 * @param inputFileDef
	 *            String
	 * @param outPutFileName
	 *            String
	 */
	private static void createLoadFile(String inputFileDef, String outPutFileName) {
		/*
		 * CSV file format CSV file format is: 01/01/2013 (From date),01/01/2014
		 * (To Date),DES, Underlying, Sec Type, Exchange, AAPL, AMZN e.t.c comma
		 * separated list of stocks.
		 */
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;

		try {

			if ((inputFileDef == null) || inputFileDef.equals("")) {
				return;
			}
			fileReader = new FileReader(inputFileDef);
			bufferedReader = new BufferedReader(fileReader);

			String csvLine = "";
			// read comma separated file line by line
			LinkedList<String> contracts = new LinkedList<String>();
			ZonedDateTime startDate = null;
			ZonedDateTime endDate = null;
			String des = null;
			String secType = null;
			String exchange = null;
			while ((csvLine = bufferedReader.readLine()) != null) {

				// break comma separated line using ","
				Scanner scanLine = new Scanner(csvLine);
				scanLine.useDelimiter("\\,");
				int tokenNumber = 0;

				while (scanLine.hasNext()) {
					// display csv values
					tokenNumber++;
					String token = scanLine.next().trim();
					if (token.length() == 0)
						continue;

					switch (tokenNumber) {
					case 1: {
						if (token.length() == 10) {
							startDate = TradingCalendar.getZonedDateTimeFromDateTimeString(token, "MM/dd/yyyy");
						}
						break;
					}
					case 2: {
						if (token.length() == 10) {
							endDate = TradingCalendar.getZonedDateTimeFromDateTimeString(token, "MM/dd/yyyy");
						}
						break;
					}
					case 3: {
						des = token.toUpperCase();
						break;
					}
					case 4: {
						secType = token.toUpperCase();
						break;
					}
					case 5: {
						exchange = token.toUpperCase();
						break;
					}
					default: {
						contracts.add(token.toUpperCase());
					}
					}
				}
				scanLine.close();
			}
			StringBuffer outPutFile = new StringBuffer();
			while (startDate.isBefore(TradingCalendar.addTradingDays(endDate, 1))) {

				if (TradingCalendar.isTradingDay(startDate)) {
					for (String symbol : contracts) {
						outPutFile.append(des + "," + symbol + "," + secType + "," + exchange + ",,,,,,||"
								+ TradingCalendar.getFormattedDate(startDate, "MM/dd/yyyy") + "|\n");
					}
				}

				startDate = TradingCalendar.getNextTradingDay(startDate);
			}
			if (null != outPutFileName) {
				OutputStream out = new FileOutputStream(outPutFileName);
				out.write(outPutFile.toString().getBytes());
				out.flush();
				out.close();
			}
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();

		} finally {
			if (null != fileReader)
				try {
					fileReader.close();
					if (null != bufferedReader)
						bufferedReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
	}

}
