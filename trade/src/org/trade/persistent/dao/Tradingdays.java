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
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
	private List<Tradingday> tradingdays = Collections
			.synchronizedList(new ArrayList<Tradingday>(0));

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
			for (ListIterator<Tradingday> itemIter = this.tradingdays
					.listIterator(); itemIter.hasNext();) {
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
	 *            Date
	 * 
	 * @param close
	 *            Date
	 */
	public void remove(Date open, Date close) {
		synchronized (this.tradingdays) {
			for (ListIterator<Tradingday> itemIter = this.tradingdays
					.listIterator(); itemIter.hasNext();) {
				Tradingday item = itemIter.next();
				if (item.getOpen().compareTo(open) == 0
						&& item.getClose().compareTo(close) == 0) {
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
	public Tradingday getTradingday(Date open, Date close) {
		synchronized (this.tradingdays) {
			for (Tradingday tradingday : this.tradingdays) {
				if (tradingday.getOpen().compareTo(open) == 0
						&& tradingday.getClose().compareTo(close) == 0)
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
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
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
				for (Tradestrategy tradestrategy : tradingday
						.getTradestrategies()) {
					if (tradestrategy.getIdTradeStrategy().equals(
							idTradestrategy)) {
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
			for (ListIterator<Tradingday> itemIter = this.tradingdays
					.listIterator(); itemIter.hasNext();) {
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
				for (ListIterator<Tradestrategy> itemIter = tradingday
						.getTradestrategies().listIterator(); itemIter
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
	public synchronized void populateDataFromFile(String fileName,
			Tradingday tradingday) throws Exception {

		/*
		 * CSV file format CSV file format is: DES, Underlying, Sec Type,
		 * Exchange, Expiration (yyyyMM, or yyyyMMdd opt),Strike
		 * Price(opt),PUT/CALL
		 * (opt),Multiplier(opt),|BOT/SLD(opt)|DATE(MM/dd/yyyy) (opt)|
		 * Tier(Opt)| Mkt Bias(opt)| Mkt Bar(opt)| Mkt Gap(opt)");
		 */
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		/*
		 * Refresh the decode tables.
		 */
		DBTableLookupServiceProvider.clearLookup();
		try {

			if ((fileName == null) || fileName.equals("")) {
				return;
			}
			fileReader = new FileReader(fileName);
			bufferedReader = new BufferedReader(fileReader);

			Integer chartDays = ConfigProperties
					.getPropAsInt("trade.backfill.duration");
			if (!ChartDays.newInstance(chartDays).isValid())
				chartDays = new Integer(2);

			Integer barSize = ConfigProperties
					.getPropAsInt("trade.backfill.barsize");
			if (!BarSize.newInstance(barSize).isValid())
				barSize = new Integer(300);

			Integer riskAmount = ConfigProperties.getPropAsInt("trade.risk");
			String strategyName = ConfigProperties
					.getPropAsString("trade.strategy.default");
			if (!DAOStrategy.newInstance(strategyName).isValid())
				strategyName = DAOStrategy.newInstance().getCode();

			Strategy strategy = (Strategy) DAOStrategy
					.newInstance(strategyName).getObject();

			Portfolio portfolio = (Portfolio) DAOPortfolio.newInstance()
					.getObject();
			String strLine = "";

			// read comma separated file line by line

			while ((strLine = bufferedReader.readLine()) != null) {
				Tradestrategy tradestrategy = Tradingdays
						.parseContractLine(strLine);
				if (null != tradestrategy) {

					Contract contract = this.getContract(tradestrategy
							.getContract().getSymbol());
					if (null != contract) {
						tradestrategy.setContract(contract);
					}

					if (null == tradestrategy.getTradingday()) {
						if (null == tradingday) {
							throw new PersistentModelException(
									"Please select a Tradingday");
						}
						tradestrategy.setTradingday(tradingday);
					} else {
						Tradingday currTradingday = this.getTradingday(
								tradestrategy.getTradingday().getOpen(),
								tradestrategy.getTradingday().getClose());
						if (null != currTradingday) {
							if (null != tradestrategy.getTradingday()
									.getMarketBar()
									&& null == currTradingday.getMarketBar()) {
								currTradingday.setMarketBar(tradestrategy
										.getTradingday().getMarketBar());
							}
							if (null != tradestrategy.getTradingday()
									.getMarketBias()
									&& null == currTradingday.getMarketBias()) {
								currTradingday.setMarketBias(tradestrategy
										.getTradingday().getMarketBias());
							}
							if (null != tradestrategy.getTradingday()
									.getMarketGap()
									&& null == currTradingday.getMarketGap()) {
								currTradingday.setMarketGap(tradestrategy
										.getTradingday().getMarketGap());
							}
							tradestrategy.setTradingday(currTradingday);
						}
					}
					tradestrategy.setRiskAmount(new BigDecimal(riskAmount));
					tradestrategy.setBarSize(barSize);
					tradestrategy.setChartDays(chartDays);
					tradestrategy.setTrade(true);
					tradestrategy.setDirty(true);
					tradestrategy.setStrategy(strategy);
					tradestrategy.setPortfolio(portfolio);
					if (!tradestrategy.getTradingday().existTradestrategy(
							tradestrategy))
						tradestrategy.getTradingday().addTradestrategy(
								tradestrategy);

					if (!this.containsTradingday(tradestrategy.getTradingday())) {
						this.add(tradestrategy.getTradingday());
					}
					Collections
							.sort(tradestrategy.getTradingday()
									.getTradestrategies(),
									Tradestrategy.DATE_ORDER_ASC);
				}
			}

		} catch (Exception ex) {
			throw new PersistentModelException(1, 200, ex.getMessage());
		} finally {
			if (null != fileReader)
				fileReader.close();
			if (null != bufferedReader)
				bufferedReader.close();
		}
	}

	/**
	 * Method parseContractLine.
	 * 
	 * @param csvLine
	 *            String
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 */
	public static Tradestrategy parseContractLine(String csvLine)
			throws PersistentModelException {
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
					tradestrategy.setLastUpdateDate(TradingCalendar
							.getDate((new Date()).getTime()));
					contract = new Contract();
					contract.setCurrency(Currency.USD);
					tradestrategy.setContract(contract);
				} else {
					throw new PersistentModelException(
							1,
							200,
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
				Date expiryDate = null;
				if (token.length() == 6) {
					expiryDate = TradingCalendar.getFormattedDate(token,
							"yyyyMM");
				} else if (token.length() == 8) {
					expiryDate = TradingCalendar.getFormattedDate(token,
							"yyyyMMdd");
				} else {
					expiryDate = null;
				}
				contract.setExpiry(expiryDate);
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
				// TODO
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
						Date todayOpen = TradingCalendar
								.getBusinessDayStart(TradingCalendar
										.getFormattedDate(custToken,
												"MM/dd/yyyy"));
						tradingday = Tradingday.newInstance(todayOpen);
						tradestrategy.setTradingday(tradingday);
						break;
					}
					case 3: {
						tradestrategy.setTier(custToken.toUpperCase());
						break;
					}
					case 4: {
						tradingday.setMarketBias(custToken.toUpperCase());
						break;
					}
					case 5: {
						tradingday.setMarketBar(custToken.toUpperCase());
						break;
					}
					case 6: {
						tradingday.setMarketGap(custToken.toUpperCase());
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
}
