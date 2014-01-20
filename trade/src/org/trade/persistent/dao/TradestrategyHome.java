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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.trade.core.dao.EntityManagerHelper;

/**
 */
@Stateless
public class TradestrategyHome {

	public TradestrategyHome() {
	}

	/**
	 * Method findById.
	 * 
	 * @param id
	 *            Integer
	 * @return Tradestrategy
	 */
	public synchronized Tradestrategy findById(Integer id) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			Tradestrategy instance = entityManager
					.find(Tradestrategy.class, id);

			if (null != instance) {
				instance.getStrategy().getIndicatorSeries().size();
				instance.getTradeOrders().size();
				instance.getPortfolio().getPortfolioAccounts().size();
			}
			entityManager.getTransaction().commit();
			return instance;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findVersionById.
	 * 
	 * @param id
	 *            Integer
	 * @return Integer
	 */
	public synchronized Integer findVersionById(Integer id) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<TradestrategyLite> query = builder
					.createQuery(TradestrategyLite.class);
			Root<TradestrategyLite> from = query.from(TradestrategyLite.class);

			CriteriaQuery<TradestrategyLite> select = query.multiselect(
					from.get("idTradeStrategy"), from.get("version"));
			Predicate predicate = builder
					.equal(from.get("idTradeStrategy"), id);
			query.where(predicate);
			TypedQuery<TradestrategyLite> typedQuery = entityManager
					.createQuery(select);
			List<TradestrategyLite> items = typedQuery.getResultList();

			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return items.get(0).getVersion();
			}
			return null;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findPositionOrdersByTradestrategyId.
	 * 
	 * @param idTradestrategy
	 *            Integer
	 * @return PositionOrders
	 */

	public synchronized PositionOrders findPositionOrdersByTradestrategyId(
			Integer idTradestrategy) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			PositionOrders instance = entityManager.find(PositionOrders.class,
					idTradestrategy);
			entityManager.getTransaction().commit();
			return instance;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findByTradeId.
	 * 
	 * @param idTrade
	 *            Integer
	 * @return Tradestrategy
	 */
	public synchronized Tradestrategy findByTradeOrderId(Integer idTradeOrder) {

		try {
			Tradestrategy tradestrategy = null;
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			TradeOrder instance = entityManager.find(TradeOrder.class,
					idTradeOrder);
			if (null != instance) {
				tradestrategy = instance.getTradestrategy();
				tradestrategy.getContract();
			}
			entityManager.getTransaction().commit();
			return tradestrategy;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Just used for testing.
	 * 
	 * 
	 * 
	 * @return List<Tradestrategy> a list of tradestrategies * @throws
	 *         RuntimeException if there is a problem finding data
	 */
	public List<Tradestrategy> findAll() {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradestrategy> query = builder
					.createQuery(Tradestrategy.class);
			Root<Tradestrategy> from = query.from(Tradestrategy.class);
			query.select(from);
			List<Tradestrategy> items = entityManager.createQuery(query)
					.getResultList();
			for (Tradestrategy tradestrategy : items) {
				tradestrategy.getTradingday().getCandles().size();
				for (TradeOrder tradeOrder : tradestrategy.getTradeOrders()) {
					tradeOrder.getTradeOrderfills().size();
				}
			}
			entityManager.getTransaction().commit();
			return items;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findTradestrategyByUniqueKeys.
	 * 
	 * @param open
	 *            Date
	 * @param strategyName
	 *            String
	 * @param idContract
	 *            Integer
	 * @param portfolioName
	 *            String
	 * @return Tradestrategy
	 */
	public Tradestrategy findTradestrategyByUniqueKeys(Date open,
			String strategyName, Integer idContract, String portfolioName) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradestrategy> query = builder
					.createQuery(Tradestrategy.class);
			Root<Tradestrategy> from = query.from(Tradestrategy.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();
			if (null != strategyName) {
				Join<Tradestrategy, Strategy> strategies = from
						.join("strategy");
				Predicate predicate = builder.equal(strategies.get("name"),
						strategyName);
				predicates.add(predicate);
			}
			if (null != portfolioName) {
				Join<Tradestrategy, Portfolio> portfolio = from
						.join("portfolio");
				Predicate predicate = builder.equal(portfolio.get("name"),
						portfolioName);
				predicates.add(predicate);
			}
			if (null != open) {
				Join<Tradestrategy, Tradingday> tradingday = from
						.join("tradingday");
				Predicate predicate = builder.equal(tradingday.get("open"),
						open);
				predicates.add(predicate);
			}
			if (null != idContract) {
				Join<Tradestrategy, Contract> contract = from.join("contract");
				Predicate predicate = builder.equal(contract.get("idContract"),
						idContract);
				predicates.add(predicate);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Tradestrategy> typedQuery = entityManager
					.createQuery(query);
			List<Tradestrategy> items = typedQuery.getResultList();
			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return items.get(0);
			}
			return null;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findTradestrategyDistinctByDateRange.
	 * 
	 * @param fromOpen
	 *            Date
	 * @param toOpen
	 *            Date
	 * @return Vector<ComboItem>
	 */
	public List<Tradestrategy> findTradestrategyDistinctByDateRange(
			Date fromOpen, Date toOpen) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Tradestrategy> query = builder
					.createQuery(Tradestrategy.class);
			Root<Tradestrategy> from = query.from(Tradestrategy.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();

			// Query query =
			// entityManager.createQuery("select s.id,s.pbyte from SimpleBean s ");
			// List listExpected = query.getResultList();
			if (null != fromOpen) {
				Join<Tradestrategy, Tradingday> tradingday = from
						.join("tradingday");
				Predicate predicate = builder.equal(tradingday.get("open"),
						fromOpen);
				predicates.add(predicate);
			}
			if (null != toOpen) {
				Join<Tradestrategy, Tradingday> tradingday = from
						.join("tradingday");
				Predicate predicate = builder.equal(tradingday.get("open"),
						toOpen);
				predicates.add(predicate);
			}

			query.multiselect(from.get("barSize"), from.get("chartDays"),
					from.join("strategy")).distinct(true);

			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Tradestrategy> typedQuery = entityManager
					.createQuery(query);
			List<Tradestrategy> items = typedQuery.getResultList();
			entityManager.getTransaction().commit();
			return items;

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}
}
