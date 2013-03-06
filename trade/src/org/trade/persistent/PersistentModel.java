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

import java.util.Date;
import java.util.List;

import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.persistent.dao.Candle;
import org.trade.persistent.dao.Contract;
import org.trade.persistent.dao.Portfolio;
import org.trade.persistent.dao.Rule;
import org.trade.persistent.dao.Strategy;
import org.trade.persistent.dao.Trade;
import org.trade.persistent.dao.Account;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.TradeOrderfill;
import org.trade.persistent.dao.TradelogReport;
import org.trade.persistent.dao.Tradestrategy;
import org.trade.persistent.dao.Tradingday;
import org.trade.persistent.dao.Tradingdays;
import org.trade.strategy.data.CandleSeries;
import org.trade.strategy.data.candle.CandleItem;

/**
 */
public interface PersistentModel {

	public final static String _persistentModel = "PersistentModel";

	/**
	 * Method persistTradingday.
	 * 
	 * @param transientInstance
	 *            Tradingday
	 * @throws PersistentModelException
	 */
	void persistTradingday(Tradingday transientInstance)
			throws PersistentModelException;

	/**
	 * Method persistTradestrategy.
	 * 
	 * @param transientInstance
	 *            Tradestrategy
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 */
	Tradestrategy persistTradestrategy(Tradestrategy transientInstance)
			throws PersistentModelException;

	/**
	 * Method persistContract.
	 * 
	 * @param transientInstance
	 *            Contract
	 * @return Contract
	 * @throws PersistentModelException
	 */
	Contract persistContract(Contract transientInstance)
			throws PersistentModelException;

	/**
	 * Method resetDefaultAccount.
	 * 
	 * @param account
	 *            Account
	 * @param portfolio
	 *            Portfolio
	 * @throws PersistentModelException
	 */
	void resetDefaultAccount(Portfolio portfolio, Account account)
			throws PersistentModelException;

	/**
	 * Method persistTradeOrder.
	 * 
	 * @param transientInstance
	 *            TradeOrder
	 * @return TradeOrder
	 * @throws PersistentModelException
	 */
	TradeOrder persistTradeOrder(TradeOrder transientInstance)
			throws PersistentModelException;

	/**
	 * Method persistTradeOrderfill.
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @return TradeOrder
	 * @throws PersistentModelException
	 */
	TradeOrder persistTradeOrderfill(TradeOrder tradeOrder)
			throws PersistentModelException;

	/**
	 * Method persistTrade.
	 * 
	 * @param transientInstance
	 *            Trade
	 * @return Trade
	 * @throws PersistentModelException
	 */
	Trade persistTrade(Trade transientInstance) throws PersistentModelException;

	/**
	 * Method persistCandleSeries.
	 * 
	 * @param candleSeries
	 *            CandleSeries
	 * @throws PersistentModelException
	 */
	void persistCandleSeries(CandleSeries candleSeries)
			throws PersistentModelException;

	/**
	 * Method persistCandleItem.
	 * 
	 * @param candleItem
	 *            CandleItem
	 * @throws PersistentModelException
	 */
	void persistCandleItem(CandleItem candleItem)
			throws PersistentModelException;

	/**
	 * Method findAccountById.
	 * 
	 * @param id
	 *            Integer
	 * @return Account
	 * @throws PersistentModelException
	 */
	Account findAccountById(Integer id) throws PersistentModelException;

	/**
	 * Method findAccountByNumber.
	 * 
	 * @param accountNumber
	 *            String
	 * @return Account
	 * @throws PersistentModelException
	 */
	Account findAccountByNumber(String accountNumber)
			throws PersistentModelException;

	/**
	 * Method findContractById.
	 * 
	 * @param idContract
	 *            Integer
	 * @return Contract
	 * @throws PersistentModelException
	 */
	Contract findContractById(Integer idContract)
			throws PersistentModelException;

	/**
	 * Method persistAccount.
	 * 
	 * @param instance
	 *            Account
	 * @return Account
	 * @throws PersistentModelException
	 */
	Account persistAccount(Account instance) throws PersistentModelException;

	/**
	 * Method persistPortfolio.
	 * 
	 * @param instance
	 *            Portfolio
	 * @return Portfolio
	 * @throws PersistentModelException
	 */

	Portfolio persistPortfolio(Portfolio instance)
			throws PersistentModelException;

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
	 */
	Contract findContractByUniqueKey(String SECType, String symbol,
			String exchange, String currency, Date expiry)
			throws PersistentModelException;

	/**
	 * Method findTradestrategyById.
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 */
	Tradestrategy findTradestrategyById(Tradestrategy tradestrategy)
			throws PersistentModelException;

	/**
	 * Method findTradestrategyById.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * @return Tradestrategy
	 * @throws PersistentModelException
	 */
	Tradestrategy findTradestrategyById(Integer idTradestrategy)
			throws PersistentModelException;

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
	 */
	Tradestrategy findTradestrategyByUniqueKeys(Date open, String strategy,
			Integer idContract, String portfolioName)
			throws PersistentModelException;

	/**
	 * Method findAllTradestrategies.
	 * 
	 * @return List<Tradestrategy>
	 * @throws PersistentModelException
	 */
	List<Tradestrategy> findAllTradestrategies()
			throws PersistentModelException;

	/**
	 * Method findTradeById.
	 * 
	 * @param idTrade
	 *            Integer
	 * @return Trade
	 * @throws PersistentModelException
	 */
	Trade findTradeById(Integer idTrade) throws PersistentModelException;

	/**
	 * Method findOpenTradeByTradestrategyId.
	 * 
	 * @param id
	 *            Integer
	 * @return Trade
	 * @throws PersistentModelException
	 */
	Trade findOpenTradeByTradestrategyId(Integer id)
			throws PersistentModelException;

	/**
	 * Method findPortfolioById.
	 * 
	 * @param id
	 *            Integer
	 * @return Portfolio
	 * @throws PersistentModelException
	 */
	Portfolio findPortfolioById(Integer id) throws PersistentModelException;

	/**
	 * Method findPortfolioByName.
	 * 
	 * @param name
	 *            String
	 * @return Portfolio
	 * @throws PersistentModelException
	 */

	Portfolio findPortfolioByName(String name) throws PersistentModelException;

	/**
	 * Method resetDefaultPortfolio.
	 * 
	 * @param transientInstance
	 *            Portfolio
	 * @return Portfolio
	 * @throws PersistentModelException
	 */
	Portfolio resetDefaultPortfolio(Portfolio transientInstance)
			throws PersistentModelException;

	/**
	 * Method findTradesByTradestrategyId.
	 * 
	 * @param id
	 *            Integer
	 * @return List<Trade>
	 * @throws PersistentModelException
	 */
	List<Trade> findTradesByTradestrategyId(Integer id)
			throws PersistentModelException;

	/**
	 * Method removeTradingdayTrades.
	 * 
	 * @param transientInstance
	 *            Tradingday
	 * @throws PersistentModelException
	 */
	void removeTradingdayTrades(Tradingday transientInstance)
			throws PersistentModelException;

	/**
	 * Method removeTradestrategyTrades.
	 * 
	 * @param transientInstance
	 *            Tradestrategy
	 * @throws PersistentModelException
	 */
	void removeTradestrategyTrades(Tradestrategy transientInstance)
			throws PersistentModelException;

	/**
	 * Method findTradeOrderByKey.
	 * 
	 * @param orderKey
	 *            Integer
	 * @return TradeOrder
	 * @throws PersistentModelException
	 */
	TradeOrder findTradeOrderByKey(Integer orderKey)
			throws PersistentModelException;

	/**
	 * Method findTradeOrderfillByExecId.
	 * 
	 * @param execId
	 *            String
	 * @return TradeOrderfill
	 * @throws PersistentModelException
	 */
	TradeOrderfill findTradeOrderfillByExecId(String execId)
			throws PersistentModelException;

	/**
	 * Method findTradeOrderByMaxKey.
	 * 
	 * @return Integer
	 * @throws PersistentModelException
	 */
	Integer findTradeOrderByMaxKey() throws PersistentModelException;

	/**
	 * Method findTradingdayById.
	 * 
	 * @param idTradingday
	 *            Integer
	 * @return Tradingday
	 * @throws PersistentModelException
	 */
	Tradingday findTradingdayById(Integer idTradingday)
			throws PersistentModelException;

	/**
	 * Method findTradingdayByOpenDate.
	 * 
	 * @param date
	 *            Date
	 * @return Tradingday
	 * @throws PersistentModelException
	 */
	Tradingday findTradingdayByOpenCloseDate(Date openDate, Date closeDate)
			throws PersistentModelException;

	/**
	 * Method findTradingdaysByDateRange.
	 * 
	 * @param startDate
	 *            Date
	 * @param endDate
	 *            Date
	 * @return Tradingdays
	 * @throws PersistentModelException
	 */
	Tradingdays findTradingdaysByDateRange(Date startDate, Date endDate)
			throws PersistentModelException;

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
	 * @return TradelogReport
	 * @throws PersistentModelException
	 */
	TradelogReport findTradelogReport(Portfolio portfolio, Date start,
			Date end, boolean filter) throws PersistentModelException;

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
	List<Candle> findCandlesByContractDateRangeBarSize(Integer idContract,
			Date startDate, Date endDate, Integer barSize)
			throws PersistentModelException;

	/**
	 * Method findCandleCount.
	 * 
	 * @param idTradingday
	 *            Integer
	 * @param idContract
	 *            Integer
	 * @return Long
	 * @throws PersistentModelException
	 */
	Long findCandleCount(Integer idTradingday, Integer idContract)
			throws PersistentModelException;

	/**
	 * Method persistRule.
	 * 
	 * @param transientInstance
	 *            Rule
	 * @return Aspect
	 * @throws PersistentModelException
	 */
	Aspect persistRule(Rule transientInstance) throws PersistentModelException;

	/**
	 * Method findRuleById.
	 * 
	 * @param idRule
	 *            Integer
	 * @return Rule
	 * @throws PersistentModelException
	 */
	Rule findRuleById(Integer idRule) throws PersistentModelException;

	/**
	 * Method findRuleByMaxVersion.
	 * 
	 * @param strategy
	 *            Strategy
	 * @return Integer
	 * @throws PersistentModelException
	 */
	Integer findRuleByMaxVersion(Strategy strategy)
			throws PersistentModelException;

	/**
	 * Method findStrategyById.
	 * 
	 * @param idStrategy
	 *            Integer
	 * @return Strategy
	 * @throws PersistentModelException
	 */
	Strategy findStrategyById(Integer idStrategy)
			throws PersistentModelException;

	/**
	 * Method findStrategyByName.
	 * 
	 * @param name
	 *            String
	 * @return Strategy
	 * @throws PersistentModelException
	 */
	Strategy findStrategyByName(String name) throws PersistentModelException;

	/**
	 * Method removeRule.
	 * 
	 * @param rule
	 *            Rule
	 * @throws PersistentModelException
	 */
	void removeRule(Rule rule) throws PersistentModelException;

	/**
	 * Method findStrategies.
	 * 
	 * @return List<Strategy>
	 * @throws PersistentModelException
	 */
	List<Strategy> findStrategies() throws PersistentModelException;

	/**
	 * Method findAspectsByClassName.
	 * 
	 * @param aspectClassName
	 *            String
	 * @return Aspects
	 * @throws PersistentModelException
	 */
	Aspects findAspectsByClassName(String aspectClassName)
			throws PersistentModelException;

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
	 */
	Aspects findAspectsByClassNameFieldName(String className, String fieldname,
			String value) throws PersistentModelException;

	/**
	 * Method findAspectById.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @return Aspect
	 * @throws PersistentModelException
	 */
	Aspect findAspectById(Aspect transientInstance)
			throws PersistentModelException;

	/**
	 * Method persistAspect.
	 * 
	 * @param aspect
	 *            Aspect
	 * @return Aspect
	 * @throws PersistentModelException
	 */
	Aspect persistAspect(Aspect aspect) throws PersistentModelException;

	/**
	 * Method persistAspect.
	 * 
	 * @param aspect
	 *            Aspect
	 * @param overrideVersion
	 *            boolean
	 * @return Aspect
	 * @throws PersistentModelException
	 */
	Aspect persistAspect(Aspect aspect, boolean overrideVersion)
			throws PersistentModelException;

	/**
	 * Method removeAspect.
	 * 
	 * @param aspect
	 *            Aspect
	 * @throws PersistentModelException
	 */
	void removeAspect(Aspect aspect) throws PersistentModelException;

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
	 */
	void reassignStrategy(Strategy fromStrategy, Strategy toStrategy,
			Tradingday tradingday) throws PersistentModelException;
}
