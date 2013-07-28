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
package org.trade.persistent;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.OptimisticLockException;

import org.trade.core.dao.Aspect;
import org.trade.core.dao.AspectHome;
import org.trade.core.dao.Aspects;
import org.trade.core.util.CoreUtils;
import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Money;
import org.trade.dictionary.valuetype.Action;
import org.trade.dictionary.valuetype.OrderStatus;
import org.trade.dictionary.valuetype.Side;
import org.trade.dictionary.valuetype.TradestrategyStatus;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.CandleHome;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.ContractHome;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.PortfolioHome;
import org.trade.persistent.dao.PositionOrders;
import org.trade.persistent.dao.Rule;
import org.trade.persistent.dao.RuleHome;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.StrategyHome;
import org.trade.persistent.dao.TradePosition;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.AccountHome;
import org.trade.persistent.dao.TradePositionHome;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderHome;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.TradeOrderfillHome;
import org.trade.persistent.dao.TradelogHome;
import org.trade.persistent.dao.TradelogReport;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.TradestrategyHome;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.TradingdayHome;
import org.trade.persistent.dao.Tradingdays;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.candle.CandleItem;

/**
 */
public class TradePersistentModel implements PersistentModel {

	private ContractHome m_contractHome = null;
	private StrategyHome m_strategyHome = null;
	private TradingdayHome m_tradingdayHome = null;
	private TradeOrderHome m_tradeOrderHome = null;
	private TradeOrderfillHome m_tradeOrderfillHome = null;
	private TradePositionHome m_tradePositionHome = null;
	private TradelogHome m_tradelogHome = null;
	private AccountHome m_accountHome = null;
	private PortfolioHome m_portfolioHome = null;
	private TradestrategyHome m_tradestrategyHome = null;
	private CandleHome m_candleHome = null;
	private AspectHome m_aspectHome = null;
	private RuleHome m_ruleHome = null;

	private static final int SCALE = 5;

	public TradePersistentModel() {
		m_contractHome = new ContractHome();
		m_strategyHome = new StrategyHome();
		m_tradingdayHome = new TradingdayHome();
		m_tradeOrderHome = new TradeOrderHome();
		m_tradeOrderfillHome = new TradeOrderfillHome();
		m_tradePositionHome = new TradePositionHome();
		m_tradelogHome = new TradelogHome();
		m_accountHome = new AccountHome();
		m_portfolioHome = new PortfolioHome();
		m_tradestrategyHome = new TradestrategyHome();
		m_candleHome = new CandleHome();
		m_aspectHome = new AspectHome();
		m_ruleHome = new RuleHome();
	}

	/**
	 * Method findTradelogReport.
	 * 
	 * @param portfolio
	 *            Portfolio
	 * @param start
	 *            Date
	 * @param end
	 *            Date
	 * @param filter
	 *            boolean
	 * @param symbol
	 *            String
	 * @param winLossAmount
	 *            BigDecimal
	 * @return TradelogReport
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradelogReport(Account,
	 *      Date, Date, boolean)
	 */
	public TradelogReport findTradelogReport(Portfolio portfolio, Date start,
			Date end, boolean filter, String symbol, BigDecimal winLossAmount)
			throws PersistentModelException {
		return m_tradelogHome.findByTradelogReport(portfolio, start, end,
				filter, symbol, winLossAmount);
	}

	/**
	 * Method findAccountById.
	 * 
	 * @param id
	 *            Integer
	 * @return Account
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findAccountById(Integer)
	 */
	public Account findAccountById(Integer id) throws PersistentModelException {
		Account instance = m_accountHome.findById(id);
		if (null == instance)
			throw new PersistentModelException("Account not found for id: "
					+ id);
		return instance;
	}

	/**
	 * Method findAccountByNumber.
	 * 
	 * @param accountNumber
	 *            String
	 * @return Account
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findAccountByNumber(String)
	 */
	public Account findAccountByNumber(String accountNumber)
			throws PersistentModelException {
		return m_accountHome.findByAccountNumber(accountNumber);
	}

	/**
	 * Method findTradingdayById.
	 * 
	 * @param id
	 *            Integer
	 * @return Tradingday
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradingdayById(Integer)
	 */
	public Tradingday findTradingdayById(Integer id)
			throws PersistentModelException {
		Tradingday instance = m_tradingdayHome.findTradingdayById(id);
		if (null == instance)
			throw new PersistentModelException("Tradingday not found for id: "
					+ id);
		return instance;
	}

	/**
	 * Method findTradingdayByOpenDate.
	 * 
	 * @param date
	 *            Date
	 * @return Tradingday
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradingdayByOpenDate(Date)
	 */
	public Tradingday findTradingdayByOpenCloseDate(Date openDate,
			Date closeDate) throws PersistentModelException {
		return m_tradingdayHome.findByOpenCloseDate(openDate, closeDate);
	}

	/**
	 * Method findContractById.
	 * 
	 * @param id
	 *            Integer
	 * @return Contract
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findContractById(Integer)
	 */
	public Contract findContractById(Integer id)
			throws PersistentModelException {
		Contract instance = m_contractHome.findById(id);
		if (null == instance)
			throw new PersistentModelException("Contract not found for id: "
					+ id);
		return instance;
	}

	/**
	 * Method findContractByUniqueKey.
	 * 
	 * @param SECType
	 *            String
	 * @param symbol
	 *            String
	 * @param exchange
	 *            String
	 * @param currency
	 *            String
	 * @return Contract
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findContractByUniqueKey(String,
	 *      String, String, String)
	 */
	public Contract findContractByUniqueKey(String SECType, String symbol,
			String exchange, String currency, Date expiry)
			throws PersistentModelException {
		return m_contractHome.findByUniqueKey(SECType, symbol, exchange,
				currency, expiry);
	}

	/**
	 * Method findTradestrategyById.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradestrategyById(Tradestrategy)
	 */
	public Tradestrategy findTradestrategyById(Tradestrategy tradestrategy)
			throws PersistentModelException {
		if (null == tradestrategy.getIdTradeStrategy())
			throw new PersistentModelException(
					"Please save Tradestrategy for symbol: "
							+ tradestrategy.getContract().getSymbol());

		Tradestrategy instance = m_tradestrategyHome.findById(tradestrategy
				.getIdTradeStrategy());
		if (null == instance)
			throw new PersistentModelException(
					"Tradestrategy not found for id: "
							+ tradestrategy.getIdTradeStrategy());

		instance.setDatasetContainer(tradestrategy.getDatasetContainer());
		return instance;
	}

	/**
	 * Method findPositionOrdersByTradestrategyId.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * 
	 * @return PositionOrders
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findPositionOrdersById(Integer)
	 */
	public PositionOrders findPositionOrdersByTradestrategyId(
			Integer idTradestrategy) throws PersistentModelException {

		PositionOrders instance = m_tradestrategyHome
				.findPositionOrdersByTradestrategyId(idTradestrategy);
		if (null == instance)
			throw new PersistentModelException(
					"Tradestrategy not found for id: " + idTradestrategy);
		return instance;
	}

	/**
	 * Method findTradestrategyById.
	 * 
	 * @param id
	 *            Integer
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradestrategyById(Integer)
	 */
	public Tradestrategy findTradestrategyById(Integer id)
			throws PersistentModelException {
		Tradestrategy instance = m_tradestrategyHome.findById(id);
		if (null == instance)
			throw new PersistentModelException(
					"Tradestrategy not found for id: " + id);
		return instance;
	}

	/**
	 * Method findTradePositionById.
	 * 
	 * @param id
	 *            Integer
	 * @return TradePosition
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradeById(Integer)
	 */
	public TradePosition findTradePositionById(Integer id)
			throws PersistentModelException {
		TradePosition instance = m_tradePositionHome.findById(id);
		if (null == instance)
			throw new PersistentModelException(
					"TradePosition not found for id: " + id);
		return instance;
	}

	/**
	 * Method findOpenTradePositionByContractId.
	 * 
	 * @param id
	 *            Integer
	 * @return TradePosition
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findOpenTradePositionByContractId(Integer)
	 */
	public TradePosition findOpenTradePositionByContractId(Integer id) {
		return m_tradePositionHome.findOpenTradePositionByContractId(id);
	}

	/**
	 * Method findPortfolioById.
	 * 
	 * @param id
	 *            Integer
	 * @return Portfolio
	 * @throws PersistentModelException
	 */
	public Portfolio findPortfolioById(Integer id)
			throws PersistentModelException {
		Portfolio instance = m_portfolioHome.findById(id);
		if (null == instance)
			throw new PersistentModelException("Portfolio not found for id: "
					+ id);
		return instance;
	}

	/**
	 * Method findPortfolioByName.
	 * 
	 * @param name
	 *            String
	 * @return Portfolio
	 * @throws PersistentModelException
	 */
	public Portfolio findPortfolioByName(String name)
			throws PersistentModelException {
		return m_portfolioHome.findByName(name);
	}

	/**
	 * Method findPortfolioDefault.
	 * 
	 * @return Portfolio
	 * @throws PersistentModelException
	 */
	public Portfolio findPortfolioDefault() throws PersistentModelException {
		return m_portfolioHome.findDefault();
	}

	/**
	 * Method resetDefaultPortfolio.
	 * 
	 * @param transientInstance
	 *            Portfolio
	 * @throws PersistentModelException
	 */
	public void resetDefaultPortfolio(Portfolio transientInstance)
			throws PersistentModelException {
		try {
			m_portfolioHome.resetDefaultPortfolio(transientInstance);
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error setting default portfolio. Please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving Portfolio: "
					+ transientInstance.getName() + "\n Msg: " + e.getMessage());
		}

	}

	/**
	 * Method findAllTradestrategies.
	 * 
	 * @return List<Tradestrategy>
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findAllTradestrategies()
	 */
	public List<Tradestrategy> findAllTradestrategies()
			throws PersistentModelException {
		return m_tradestrategyHome.findAll();

	}

	/**
	 * Method findTradestrategyByUniqueKeys.
	 * 
	 * @param open
	 *            Date
	 * @param strategy
	 *            String
	 * @param idContract
	 *            Integer
	 * @param portfolioName
	 *            String
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradestrategyByUniqueKeys(Date,
	 *      String, Integer, String)
	 */
	public Tradestrategy findTradestrategyByUniqueKeys(Date open,
			String strategy, Integer idContract, String portfolioName)
			throws PersistentModelException {
		return m_tradestrategyHome.findTradestrategyByUniqueKeys(open,
				strategy, idContract, portfolioName);
	}

	/**
	 * Method removeTradingdayTrades.
	 * 
	 * @param transientInstance
	 *            Tradingday
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#removeTradingdayTrades(Tradingday)
	 */
	public void removeTradingdayTradeOrders(Tradingday transientInstance)
			throws PersistentModelException {
		for (Tradestrategy tradestrategy : transientInstance
				.getTradestrategies()) {
			this.removeTradestrategyTradeOrders(tradestrategy);
		}
	}

	/**
	 * Method removeTradestrategyTradeOrders.
	 * 
	 * @param transientInstance
	 *            Tradestrategy
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#removeTradestrategyTrades(Tradestrategy)
	 */
	public void removeTradestrategyTradeOrders(Tradestrategy transientInstance)
			throws PersistentModelException {

		try {
			transientInstance.setStatus(null);
			m_aspectHome.persist(transientInstance);

			Hashtable<Integer, TradePosition> tradePositions = new Hashtable<Integer, TradePosition>();
			for (TradeOrder tradeOrder : transientInstance.getTradeOrders()) {
				if (tradeOrder.hasTradePosition())
					tradePositions.put(tradeOrder.getTradePosition()
							.getIdTradePosition(), tradeOrder
							.getTradePosition());

				if (null != tradeOrder.getIdTradeOrder()) {
					Aspect instance = m_aspectHome.findById(tradeOrder);
					m_aspectHome.remove(instance);
				}
			}
			for (TradePosition tradePosition : tradePositions.values()) {
				tradePosition = this.findTradePositionById(tradePosition
						.getIdTradePosition());
				m_aspectHome.remove(tradePosition);
			}

			transientInstance.getTradeOrders().clear();
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error removing Tradestrategy TradePositions. Please refresh before remove.");

		} catch (Exception ex) {
			throw new PersistentModelException(
					"Error removing Tradestrategy TradePositions: "
							+ transientInstance.getContract().getSymbol()
							+ "\n Msg: " + ex.getMessage());
		}
	}

	/**
	 * Method findTradeOrderByKey.
	 * 
	 * @param orderKey
	 *            Integer
	 * @return TradeOrder
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradeOrderByKey(Integer)
	 */
	public TradeOrder findTradeOrderByKey(Integer orderKey)
			throws PersistentModelException {
		return m_tradeOrderHome.findTradeOrderByKey(orderKey);
	}

	/**
	 * Method findTradeOrderfillByExecId.
	 * 
	 * @param execId
	 *            String
	 * @return TradeOrderfill
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradeOrderfillByExecId(String)
	 */
	public TradeOrderfill findTradeOrderfillByExecId(String execId)
			throws PersistentModelException {
		return m_tradeOrderfillHome.findOrderFillByExecId(execId);
	}

	/**
	 * Method findTradeOrderByMaxKey.
	 * 
	 * @return Integer
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradeOrderByMaxKey()
	 */
	public Integer findTradeOrderByMaxKey() throws PersistentModelException {
		return m_tradeOrderHome.findTradeOrderByMaxKey();
	}

	/**
	 * Method findTradingdaysByDateRange.
	 * 
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @return Tradingdays
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findTradingdaysByDateRange(Date,
	 *      Date)
	 */
	public Tradingdays findTradingdaysByDateRange(Date startDate, Date endDate)
			throws PersistentModelException {
		return m_tradingdayHome.findTradingdaysByDateRange(startDate, endDate);
	}

	/**
	 * Method findCandlesByContractDateRangeBarSize.
	 * 
	 * @param idContract
	 *            Integer
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @param barSize
	 *            Integer
	 * @return List<Candle>
	 * @throws PersistentModelException
	 */
	public List<Candle> findCandlesByContractDateRangeBarSize(
			Integer idContract, Date startDate, Date endDate, Integer barSize)
			throws PersistentModelException {
		return m_candleHome.findCandlesByContractDateRangeBarSize(idContract,
				startDate, endDate, barSize);
	}

	/**
	 * Method findCandleCount.
	 * 
	 * @param idTradingday
	 *            Integer
	 * @param idContract
	 *            Integer
	 * @return Long
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findCandleCount(Integer,
	 *      Integer)
	 */
	public Long findCandleCount(Integer idTradingday, Integer idContract)
			throws PersistentModelException {
		return m_candleHome.findCandleCount(idTradingday, idContract);
	}

	/**
	 * Method persistTradingday.
	 * 
	 * @param transientInstance
	 *            Tradingday
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#persistTradingday(Tradingday)
	 */
	public void persistTradingday(Tradingday transientInstance)
			throws PersistentModelException {

		try {
			m_tradingdayHome.persist(transientInstance);
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error saving Tradingday please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving Tradingday: "
					+ transientInstance.getOpen() + "\n Msg: " + e.getMessage());
		}
	}

	/**
	 * Method persistContract.
	 * 
	 * @param transientInstance
	 *            Contract
	 * @return Contract
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#persistContract(Contract)
	 */
	public Contract persistContract(Contract transientInstance)
			throws PersistentModelException {

		try {
			if (null == transientInstance.getIdContract()) {
				Contract currentContract = m_contractHome.findByUniqueKey(
						transientInstance.getSecType(),
						transientInstance.getSymbol(),
						transientInstance.getExchange(),
						transientInstance.getCurrency(),
						transientInstance.getExpiry());
				if (null != currentContract) {
					transientInstance.setIdContract(currentContract
							.getIdContract());
				}
			}

			return m_aspectHome.persist(transientInstance);
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error saving Contract please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving Contract: "
					+ transientInstance.getSymbol() + "\n Msg: "
					+ e.getMessage());
		}
	}

	/**
	 * Method persistCandleSeries.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#persistCandleSeries(CandleSeries)
	 */
	public void persistCandleSeries(CandleSeries candleSeries)
			throws PersistentModelException {
		try {
			/*
			 * This can happen when an indicator is a contract that has never
			 * been used.
			 */
			if (null == candleSeries.getContract().getIdContract()) {
				Contract contract = this.persistContract(candleSeries
						.getContract());
				candleSeries.getContract().setIdContract(
						contract.getIdContract());
				candleSeries.getContract().setVersion(contract.getVersion());
			}
			m_candleHome.persistCandleSeries(candleSeries);
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error saving CandleSeries please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving CandleSeries: "
					+ candleSeries.getDescription() + "\n Msg: "
					+ e.getMessage());
		}
	}

	/**
	 * Method persistCandleItem.
	 * 
	 * @param candleItem
	 *            CandleItem
	 * @return Candle
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#persistCandleItem(CandleItem)
	 */
	public Candle persistCandleItem(CandleItem candleItem)
			throws PersistentModelException {
		try {
			synchronized (candleItem) {
				if (null == candleItem.getCandle().getTradingday()
						.getIdTradingDay()) {

					Tradingday tradingday = this.findTradingdayByOpenCloseDate(
							candleItem.getCandle().getTradingday().getOpen(),
							candleItem.getCandle().getTradingday().getClose());

					if (null == tradingday) {
						tradingday = m_aspectHome.persist(candleItem
								.getCandle().getTradingday());
					}
					candleItem.getCandle().setTradingday(tradingday);
				}
				if (null == candleItem.getCandle().getIdCandle()) {
					Candle currCandle = m_candleHome.findByUniqueKey(candleItem
							.getCandle().getTradingday().getIdTradingDay(),
							candleItem.getCandle().getContract()
									.getIdContract(), candleItem.getCandle()
									.getStartPeriod(), candleItem.getCandle()
									.getEndPeriod(), candleItem.getCandle()
									.getBarSize());
					/*
					 * Candle exists set the id and version so we can merge the
					 * incoming candle.
					 */
					if (null != currCandle) {
						candleItem.getCandle().setIdCandle(
								currCandle.getIdCandle());
						candleItem.getCandle().setVersion(
								currCandle.getVersion());
					}
				}
				return m_aspectHome.persist(candleItem.getCandle());
			}
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error saving Candle please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving CandleItem: "
					+ candleItem.getOpen() + "\n Msg: " + e.getMessage());
		}
	}

	/**
	 * Method persistTradeOrder.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @return TradeOrder
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#persistTradeOrder(TradeOrder)
	 */

	public synchronized TradeOrder persistTradeOrder(TradeOrder tradeOrder)
			throws PersistentModelException {
		try {

			if (null == tradeOrder.getOrderKey()) {
				throw new PersistentModelException("Order key cannot be null.");
			}

			/*
			 * This is a new order set the status to UNSUBMIT
			 */
			if (null == tradeOrder.getIdTradeOrder()
					&& null == tradeOrder.getFilledQuantity()) {
				tradeOrder.setStatus(OrderStatus.UNSUBMIT);
			}

			if (!tradeOrder.getIsFilled()
					&& CoreUtils.nullSafeComparator(tradeOrder.getQuantity(),
							tradeOrder.getFilledQuantity()) == 0) {
				tradeOrder.setIsFilled(true);
				tradeOrder.setStatus(OrderStatus.FILLED);
			}

			Integer tradestrategyId = null;
			if (null == tradeOrder.getTradestrategyId()) {
				tradestrategyId = tradeOrder.getTradestrategy()
						.getIdTradeStrategy();
			} else {
				tradestrategyId = tradeOrder.getTradestrategyId()
						.getIdTradeStrategy();
			}
			TradePosition tradePosition = null;
			PositionOrders positionOrders = null;
			/*
			 * If the filled qty is > 0 and we have no TradePosition then create
			 * one.
			 */

			if (!tradeOrder.hasTradePosition()) {
				if (CoreUtils.nullSafeComparator(
						tradeOrder.getFilledQuantity(), new Integer(0)) == 1) {

					positionOrders = this
							.findPositionOrdersByTradestrategyId(tradestrategyId);

					if (positionOrders.hasOpenTradePosition()) {
						tradePosition = this
								.findOpenTradePositionByContractId(positionOrders
										.getContract().getIdContract());
						if (!tradePosition.containsTradeOrder(tradeOrder))
							tradePosition.addTradeOrder(tradeOrder);
					} else {
						/*
						 * Note Order status can be fired before execDetails
						 * this could result in a new tradeposition. OrderStatus
						 * does not contain the filled date so we must set it
						 * here.
						 */
						Date positionOpenDate = tradeOrder.getFilledDate();
						if (null == positionOpenDate)
							positionOpenDate = TradingCalendar
									.getDate((new Date()).getTime());
						tradePosition = new TradePosition(
								positionOrders.getContract(),
								positionOpenDate,
								(Action.BUY.equals(tradeOrder.getAction()) ? Side.BOT
										: Side.SLD));
						tradeOrder.setIsOpenPosition(true);
						positionOrders.setStatus(TradestrategyStatus.OPEN);
						this.persistAspect(positionOrders);
						tradePosition.addTradeOrder(tradeOrder);
						tradePosition = this.persistAspect(tradePosition);
					}
					tradeOrder.setTradePosition(tradePosition);
				} else {
					/*
					 * If the order has not been filled and it has no
					 * TradePosition this is the first order that has just been
					 * update.
					 */
					return (TradeOrder) this.persistAspect(tradeOrder);
				}
			} else {
				tradePosition = this.findTradePositionById(tradeOrder
						.getTradePosition().getIdTradePosition());
				tradeOrder.setTradePosition(tradePosition);
			}

			boolean allOrdersCancelled = true;
			int totalBuyQuantity = 0;
			int totalSellQuantity = 0;
			double totalCommission = 0;
			double totalBuyValue = 0;
			double totalSellValue = 0;

			for (TradeOrder order : tradePosition.getTradeOrders()) {

				if (order.getOrderKey().equals(tradeOrder.getOrderKey())) {
					order = tradeOrder;
				}

				/*
				 * If all orders are cancelled and not filled then we need to
				 * update the tradestrategy status to cancelled.
				 */
				if (!OrderStatus.CANCELLED.equals(order.getStatus())) {
					allOrdersCancelled = false;
				}

				if (null != order.getFilledQuantity()) {

					if (Action.BUY.equals(order.getAction())) {
						totalBuyQuantity = totalBuyQuantity
								+ order.getFilledQuantity();
						totalBuyValue = totalBuyValue
								+ (order.getAverageFilledPrice().doubleValue() * order
										.getFilledQuantity().doubleValue());
					} else {
						totalSellQuantity = totalSellQuantity
								+ order.getFilledQuantity();
						totalSellValue = totalSellValue
								+ (order.getAverageFilledPrice().doubleValue() * order
										.getFilledQuantity().doubleValue());
					}
					if (null != order.getCommission()) {
						totalCommission = totalCommission
								+ order.getCommission().doubleValue();
					}
				}
			}
			/*
			 * totalFilledQuantity has changed for the trade update the trade
			 * values.
			 */
			Money comms = new Money(totalCommission);
			if (CoreUtils.nullSafeComparator(new Integer(totalBuyQuantity),
					tradePosition.getTotalBuyQuantity()) != 0
					|| CoreUtils.nullSafeComparator(new Integer(
							totalSellQuantity), tradePosition
							.getTotalSellQuantity()) != 0) {
				tradePosition
						.setOpenQuantity((totalBuyQuantity - totalSellQuantity));

				if ((totalBuyQuantity - totalSellQuantity) > 0)
					tradePosition.setSide(Side.BOT);
				if ((totalBuyQuantity - totalSellQuantity) < 0)
					tradePosition.setSide(Side.SLD);

				tradePosition.setIsOpen(true);
				if ((totalBuyQuantity - totalSellQuantity) == 0) {
					tradePosition.setPositionCloseDate(tradeOrder
							.getFilledDate());
					tradePosition.setIsOpen(false);
				}

				tradePosition.setTotalBuyQuantity(totalBuyQuantity);
				tradePosition.setTotalBuyValue((new BigDecimal(totalBuyValue))
						.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN));
				tradePosition.setTotalSellQuantity(totalSellQuantity);
				tradePosition
						.setTotalSellValue((new BigDecimal(totalSellValue))
								.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN));
				tradePosition.setTotalNetValue((new BigDecimal(totalSellValue
						- totalBuyValue)).setScale(SCALE,
						BigDecimal.ROUND_HALF_EVEN));
				tradePosition.setTotalCommission(comms.getBigDecimalValue());
				// Partial fills case.
				if (null == positionOrders)
					positionOrders = this
							.findPositionOrdersByTradestrategyId(tradestrategyId);

				if (!tradePosition.getIsOpen()
						&& !TradestrategyStatus.CLOSED.equals(positionOrders
								.getStatus())) {
					positionOrders.setStatus(TradestrategyStatus.CLOSED);
					this.persistAspect(positionOrders);
				}

				tradePosition.setUpdateDate(new Date());
				tradePosition = (TradePosition) this
						.persistAspect(tradePosition);

			} else {
				if (allOrdersCancelled) {
					if (null == positionOrders)
						positionOrders = this
								.findPositionOrdersByTradestrategyId(tradestrategyId);
					if (!TradestrategyStatus.CANCELLED.equals(positionOrders
							.getStatus())) {
						if (null == positionOrders.getStatus()) {
							positionOrders
									.setStatus(TradestrategyStatus.CANCELLED);
							this.persistAspect(positionOrders);
						}
					}
				}
				/*
				 * If the commissions (note these are updated by the orderState
				 * event after the order may have been filled) have changed
				 * update the trade.
				 */
				if (CoreUtils.nullSafeComparator(comms.getBigDecimalValue(),
						tradePosition.getTotalCommission()) == 1) {
					tradePosition
							.setTotalCommission(comms.getBigDecimalValue());
					tradePosition.setUpdateDate(new Date());
					tradePosition = (TradePosition) this
							.persistAspect(tradePosition);
				}
			}

			return (TradeOrder) this.persistAspect(tradeOrder);

		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error saving TradeOrder please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving TradeOrder: "
					+ tradeOrder.getOrderKey() + "\n Msg: " + e.getMessage());
		}
	}

	/**
	 * Method persistTradeOrderfill.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @return TradeOrder
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#persistTradeOrderfill(TradeOrder)
	 */
	public synchronized TradeOrder persistTradeOrderfill(TradeOrder tradeOrder)
			throws PersistentModelException {
		try {

			Date filledDate = null;
			double filledValue = 0;
			int filledQuantity = 0;
			for (TradeOrderfill tradeOrderfill : tradeOrder
					.getTradeOrderfills()) {

				filledQuantity = filledQuantity + tradeOrderfill.getQuantity();
				filledValue = filledValue
						+ (tradeOrderfill.getAveragePrice().doubleValue() * tradeOrderfill
								.getQuantity());
				filledDate = tradeOrderfill.getTime();
			}

			if (filledQuantity > 0) {
				BigDecimal avgFillPrice = new BigDecimal(filledValue
						/ filledQuantity);
				avgFillPrice.setScale(SCALE, BigDecimal.ROUND_HALF_EVEN);
				/*
				 * If filled qty is greater than current filled qty set the new
				 * value. Note openOrder can update the filled order quantity
				 * before the orderFills have arrived.
				 */
				if (CoreUtils.nullSafeComparator(new Integer(filledQuantity),
						tradeOrder.getFilledQuantity()) == 1) {
					tradeOrder.setAverageFilledPrice(avgFillPrice);
					tradeOrder.setFilledQuantity(filledQuantity);
					tradeOrder.setFilledDate(filledDate);
				}
			}
			return persistTradeOrder(tradeOrder);

		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException(
					"Error saving TradeOrderfill please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving TradeOrderfill: "
					+ tradeOrder.getOrderKey() + "\n Msg: " + e.getMessage());
		}
	}

	/**
	 * Method findRuleById.
	 * 
	 * @param id
	 *            Integer
	 * @return Rule
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findRuleById(Integer)
	 */
	public Rule findRuleById(Integer id) throws PersistentModelException {
		Rule instance = m_ruleHome.findById(id);
		if (null == instance)
			throw new PersistentModelException("Rule not found for Id: " + id);
		return instance;
	}

	/**
	 * Method findRuleByMaxVersion.
	 * 
	 * @param strategy
	 *            Strategy
	 * @return Integer
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findRuleByMaxVersion(Strategy)
	 */
	public Integer findRuleByMaxVersion(Strategy strategy)
			throws PersistentModelException {
		return m_ruleHome.findByMaxVersion(strategy);
	}

	/**
	 * Method findStrategyById.
	 * 
	 * @param id
	 *            Integer
	 * @return Strategy
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findStrategyById(Integer)
	 */
	public Strategy findStrategyById(Integer id)
			throws PersistentModelException {
		Strategy instance = m_strategyHome.findById(id);
		if (null == instance)
			throw new PersistentModelException("Strategy not found for Id: "
					+ id);
		return instance;
	}

	/**
	 * Method findStrategyByName.
	 * 
	 * @param name
	 *            String
	 * @return Strategy
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findStrategyByName(String)
	 */
	public Strategy findStrategyByName(String name)
			throws PersistentModelException {
		return m_strategyHome.findByName(name);
	}

	/**
	 * Method findStrategies.
	 * 
	 * @return List<Strategy>
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findStrategies()
	 */
	public List<Strategy> findStrategies() throws PersistentModelException {
		return m_strategyHome.findAll();
	}

	/**
	 * Method findAspectsByClassName.
	 * 
	 * @param aspectClassName
	 *            String
	 * @return Aspects
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findAspectsByClassName(String)
	 */
	public Aspects findAspectsByClassName(String aspectClassName)
			throws PersistentModelException {
		try {

			if ("org.trade.persistent.dao.Strategy".equals(aspectClassName)) {
				/*
				 * TODO Eager does not work on the relationship Strategy ->
				 * IndicatorSeries so hack needed.
				 */
				List<Strategy> strategies = m_strategyHome.findAll();
				Aspects aspects = new Aspects();
				for (Object item : strategies) {
					aspects.add((Aspect) item);
				}
				aspects.setDirty(false);
				return aspects;
			} else if ("org.trade.persistent.dao.Portfolio"
					.equals(aspectClassName)) {
				/*
				 * TODO Eager does not work on the relationship Portfolio ->
				 * PortfilioAccount so hack needed.
				 */
				List<Portfolio> portfolios = m_portfolioHome.findAll();
				Aspects aspects = new Aspects();
				for (Object item : portfolios) {
					aspects.add((Aspect) item);
				}
				aspects.setDirty(false);
				return aspects;
			} else {

				return m_aspectHome.findByClassName(aspectClassName);
			}

		} catch (Exception ex) {
			throw new PersistentModelException("Error finding Aspects: "
					+ ex.getMessage());
		}
	}

	/**
	 * Method findAspectsByClassNameFieldName.
	 * 
	 * @param className
	 *            String
	 * @param fieldname
	 *            String
	 * @param value
	 *            String
	 * @return Aspects
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findAspectsByClassNameFieldName(String,
	 *      String, String)
	 */
	public Aspects findAspectsByClassNameFieldName(String className,
			String fieldname, String value) throws PersistentModelException {
		try {
			return m_aspectHome.findByClassNameFieldName(className, fieldname,
					value);
		} catch (Exception ex) {
			throw new PersistentModelException("Error finding Aspects: "
					+ ex.getMessage());
		}
	}

	/**
	 * Method findAspectById.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @return Aspect
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#findAspectById(Aspect)
	 */
	public Aspect findAspectById(Aspect transientInstance)
			throws PersistentModelException {
		Aspect instance = m_aspectHome.findById(transientInstance);
		if (null == instance)
			throw new PersistentModelException("Aspect not found for Id: "
					+ transientInstance.getId());
		return instance;
	}

	/**
	 * Method persistPortfolio.
	 * 
	 * @param instance
	 *            Portfolio
	 * @throws PersistentModelException
	 */

	public Portfolio persistPortfolio(Portfolio instance)
			throws PersistentModelException {
		try {
			return m_portfolioHome.persistPortfolio(instance);
		} catch (Exception ex) {
			throw new PersistentModelException(
					"Error saving PortfolioAccount: " + ex.getMessage());
		}
	}

	/**
	 * Method persistAspect.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @return Aspect
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#persistAspect(Aspect)
	 */
	public <T extends Aspect> T persistAspect(T transientInstance)
			throws PersistentModelException {
		try {
			return m_aspectHome.persist(transientInstance);
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException("Error saving "
					+ transientInstance.getClass().getSimpleName()
					+ " please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving  "
					+ transientInstance.getClass().getSimpleName() + " : "
					+ e.getMessage());
		}
	}

	/**
	 * Method persistAspect.
	 * 
	 * @param aspect
	 *            Aspect
	 * @param overrideVersion
	 *            boolean
	 * @return Aspect
	 * @throws PersistentModelException
	 * 
	 * @see org.trade.persistent.PersistentModel#persistAspect(Aspect)
	 */
	public <T extends Aspect> T persistAspect(T transientInstance,
			boolean overrideVersion) throws PersistentModelException {
		try {
			return m_aspectHome.persist(transientInstance, overrideVersion);
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException("Error saving "
					+ transientInstance.getClass().getSimpleName()
					+ " please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error saving  "
					+ transientInstance.getClass().getSimpleName() + " : "
					+ e.getMessage());
		}
	};

	/**
	 * Method removeAspect.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#removeAspect(Aspect)
	 */
	public void removeAspect(Aspect transientInstance)
			throws PersistentModelException {
		try {
			m_aspectHome.remove(transientInstance);
		} catch (OptimisticLockException ex1) {
			throw new PersistentModelException("Error removing "
					+ transientInstance.getClass().getSimpleName()
					+ " please refresh before save.");
		} catch (Exception e) {
			throw new PersistentModelException("Error removing  "
					+ transientInstance.getClass().getSimpleName() + " : "
					+ e.getMessage());
		}
	}

	/**
	 * Method reassignStrategy.
	 * 
	 * @param fromStrategy
	 *            Strategy
	 * @param toStrategy
	 *            Strategy
	 * @param tradingday
	 *            Tradingday
	 * @throws PersistentModelException
	 * @see org.trade.persistent.PersistentModel#reassignStrategy(Strategy,
	 *      Strategy, Tradingday)
	 */
	public void reassignStrategy(Strategy fromStrategy, Strategy toStrategy,
			Tradingday tradingday) throws PersistentModelException {

		try {
			for (Tradestrategy item : tradingday.getTradestrategies()) {
				if (item.getStrategy().getIdStrategy()
						.equals(fromStrategy.getIdStrategy())) {
					item.setStrategy(toStrategy);
					item.setDirty(true);
					item.setDatasetContainer(null);
					m_aspectHome.persist(item);
				}
			}

		} catch (Exception ex) {
			throw new PersistentModelException("Error reassign Strategy: "
					+ ex.getMessage());
		}
	}
}
