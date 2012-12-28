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
import org.trade.strategy.data.IndicatorSeries;

/**
 */
@Stateless
public class TradestrategyHome {

	private EntityManager entityManager = null;

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
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Tradestrategy instance = entityManager
					.find(Tradestrategy.class, id);
			if (null != instance) {
				for (IndicatorSeries indicatorSeries : instance.getStrategy()
						.getIndicatorSeries()) {
					indicatorSeries.getCodeValues().size();
				}
				for (Trade trade : instance.getTrades()) {
					trade.getTradeOrders().size();
				}
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
	 * Method findByTradeId.
	 * 
	 * @param idTrade
	 *            Integer
	 * @return Tradestrategy
	 */
	public synchronized Tradestrategy findByTradeId(Integer idTrade) {

		try {
			Tradestrategy tradestrategy = null;
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Trade instance = entityManager.find(Trade.class, idTrade);
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
			entityManager = EntityManagerHelper.getEntityManager();
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
				for (Trade trade : tradestrategy.getTrades()) {
					for (TradeOrder tradeOrder : trade.getTradeOrders()) {
						tradeOrder.getTradeOrderfills().size();
					}
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
	 * @param accountNumber
	 *            String
	 * @return Tradestrategy
	 */
	public Tradestrategy findTradestrategyByUniqueKeys(Date open,
			String strategyName, Integer idContract, String accountNumber) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
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
			if (null != accountNumber) {
				Join<Tradestrategy, TradeAccount> tradeAccount = from
						.join("tradeAccount");
				Predicate predicate = builder.equal(
						tradeAccount.get("accountNumber"), accountNumber);
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
}
