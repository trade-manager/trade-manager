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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import org.trade.core.dao.Aspect;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.TradingCalendar;
import org.trade.dictionary.valuetype.Currency;
import org.trade.dictionary.valuetype.DAOStrategy;
import org.trade.dictionary.valuetype.DAOTradeAccount;
import org.trade.persistent.PersistentModelException;

/**
 */
public class Tradingdays extends Aspect implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3388042483785305102L;
	private ConcurrentHashMap<Date, Tradingday> tradingdays = new ConcurrentHashMap<Date, Tradingday>(
			0);

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
			this.tradingdays.put(instance.getOpen(), instance);
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
		this.tradingdays.put(tradingday.getOpen(), tradingday);
	}

	/**
	 * Method remove.
	 * 
	 * @param tradingday
	 *            Tradingday
	 */
	public void remove(Tradingday tradingday) {
		this.tradingdays.remove(tradingday.getOpen());
	}

	/**
	 * Method getTradingdays.
	 * 
	 * @return ConcurrentHashMap<Date,Tradingday>
	 */
	public ConcurrentHashMap<Date, Tradingday> getTradingdays() {
		return this.tradingdays;
	}

	/**
	 * Method setTradingdays.
	 * 
	 * @param tradingdays
	 *            ConcurrentHashMap<Date,Tradingday>
	 */
	public void setTradingdays(ConcurrentHashMap<Date, Tradingday> tradingdays) {
		this.tradingdays = tradingdays;
	}

	/**
	 * Method getTradingday.
	 * 
	 * @param open
	 *            Date
	 * @return Tradingday
	 */
	public Tradingday getTradingday(Date open) {
		return this.tradingdays.get(open);
	}

	/**
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	public boolean isDirty() {
		for (Tradingday currTradingday : getTradingdays().values()) {
			if (currTradingday.isDirty()) {
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

		for (Tradingday tradingday : getTradingdays().values()) {
			for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
				if (tradestrategy.getContract().getSymbol().equals(symbol)) {
					contract = tradestrategy.getContract();
					break;
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
		for (Tradingday tradingday : getTradingdays().values()) {
			for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
				if (tradestrategy.getIdTradeStrategy().equals(idTradestrategy)) {
					return tradestrategy;
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
		this.tradingdays.replace(newTradingday.getOpen(), newTradingday);
	}

	/**
	 * Method replaceTradestrategy.
	 * 
	 * @param newTradestrategy
	 *            Tradestrategy
	 */
	public void replaceTradestrategy(Tradestrategy newTradestrategy) {
		for (Tradingday tradingday : getTradingdays().values()) {
			for (ListIterator<Tradestrategy> itemIter = tradingday
					.getTradestrategies().listIterator(); itemIter.hasNext();) {
				Tradestrategy tradestrategy = itemIter.next();
				if (tradestrategy.equals(newTradestrategy)) {
					itemIter.set(newTradestrategy);
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
		for (Tradingday tradingday : tradingdays.getTradingdays().values()) {
			if (!tradingday.getTradestrategies().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method hasTrades.
	 * 
	 * @param tradingday
	 *            Tradingday
	 * @return boolean
	 */
	public static boolean hasTrades(Tradingday tradingday) {
		for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
			if (!tradestrategy.getTrades().isEmpty()) {
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
	public static boolean hasOpenTrades(Tradingday tradingday) {
		for (Tradestrategy tradestrategy : tradingday.getTradestrategies()) {
			for (Trade trades : tradestrategy.getTrades()) {
				if (trades.getIsOpen()) {
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
	public synchronized void populateDataFromFile(String fileName)
			throws Exception {

		/*
		 * CSV file format CSV file format is: DES, Underlying, Sec Type,
		 * Exchange, Expiration (yyyyMM, or yyyyMMdd opt),Strike
		 * Price(opt),PUT/CALL
		 * (opt),Multiplier(opt),BOT/SLD(opt)|DATE(MM/dd/yyyy) (opt)| Tier(Opt)|
		 * Mkt Bias(opt)| Mkt Bar(opt)| Mkt Gap(opt)");
		 */
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		try {

			if ((fileName == null) || fileName.equals("")) {
				return;
			}
			fileReader = new FileReader(fileName);
			bufferedReader = new BufferedReader(fileReader);

			Integer chartDays = ConfigProperties
					.getPropAsInt("trade.backfill.duration");
			Integer barSize = ConfigProperties
					.getPropAsInt("trade.backfill.barsize");
			Integer riskAmount = ConfigProperties.getPropAsInt("trade.risk");
			String strategyName = ConfigProperties
					.getPropAsString("trade.strategy.default");
			Strategy strategy = (Strategy) DAOStrategy
					.newInstance(strategyName).getObject();
			TradeAccount tradeAccount = (TradeAccount) DAOTradeAccount
					.newInstance().getObject();
			String strLine = "";

			Tradingday toDayTradingday = this.getTradingday(TradingCalendar
					.getMostRecentTradingDay(new Date()));
			if (null == toDayTradingday) {
				toDayTradingday = Tradingday.newInstance(TradingCalendar
						.getMostRecentTradingDay(new Date()));
			}
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
						tradestrategy.setTradingday(toDayTradingday);
						toDayTradingday.addTradestrategy(tradestrategy);
					} else {
						Tradingday tradingday = this
								.getTradingday(tradestrategy.getTradingday()
										.getOpen());
						if (null != tradingday) {
							if (null != tradestrategy.getTradingday()
									.getMarketBar()
									&& null == tradingday.getMarketBar()) {
								tradingday.setMarketBar(tradestrategy
										.getTradingday().getMarketBar());
							}
							if (null != tradestrategy.getTradingday()
									.getMarketBias()
									&& null == tradingday.getMarketBias()) {
								tradingday.setMarketBias(tradestrategy
										.getTradingday().getMarketBias());
							}
							if (null != tradestrategy.getTradingday()
									.getMarketGap()
									&& null == tradingday.getMarketGap()) {
								tradingday.setMarketGap(tradestrategy
										.getTradingday().getMarketGap());
							}
							tradestrategy.setTradingday(tradingday);
							tradingday.addTradestrategy(tradestrategy);
						} else {
							tradestrategy.getTradingday().addTradestrategy(
									tradestrategy);
						}
					}
					tradestrategy.setRiskAmount(new BigDecimal(riskAmount));
					tradestrategy.setBarSize(barSize);
					tradestrategy.setChartDays(chartDays);
					tradestrategy.setTrade(true);
					tradestrategy.setDirty(true);
					tradestrategy.setStrategy(strategy);
					tradestrategy.setTradeAccount(tradeAccount);

					if (!this.getTradingdays().containsKey(
							tradestrategy.getTradingday().getOpen())) {
						this.add(tradestrategy.getTradingday());
					}
					Collections.sort(tradestrategy.getTradingday()
							.getTradestrategies(), Tradestrategy.DATE_ORDER);
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
			String token = scanLine.next();

			switch (tokenNumber) {
			case 1: {
				if ("DES".equals(token.toUpperCase())) {
					tradestrategy = new Tradestrategy();
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
				if (token.toUpperCase().equals("SMART/ISLAND")) {
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
				break;
			}
			case 9: {
				Scanner custScan = new Scanner(token);
				custScan.useDelimiter("\\|");
				int custTokenNumber = 0;
				while (custScan.hasNext()) {
					// display csv values
					custTokenNumber++;
					String custToken = custScan.next();

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
