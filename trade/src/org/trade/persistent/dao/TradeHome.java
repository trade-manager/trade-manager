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
public class TradeHome {

	private EntityManager entityManager = null;

	public TradeHome() {

	}

	/**
	 * Method remove.
	 * 
	 * @param transientInstance
	 *            Trade
	 */
	public void remove(Trade transientInstance) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Trade trade = entityManager.find(Trade.class,
					transientInstance.getIdTrade());
			if (null != trade) {
				entityManager.remove(trade);
			}
			entityManager.getTransaction().commit();

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findById.
	 * 
	 * @param id
	 *            Integer
	 * @return Trade
	 */
	public Trade findById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Trade instance = entityManager.find(Trade.class, id);
			if (null != instance)
				instance.getTradeOrders().size();
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
	 * Method findOpenTradeByTradestrategyId.
	 * 
	 * @param id
	 *            Integer
	 * @return Trade
	 */
	public Trade findOpenTradeByTradestrategyId(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Trade> query = builder.createQuery(Trade.class);
			Root<Trade> from = query.from(Trade.class);
			query.select(from);

			Join<Trade, Tradestrategy> tradestrategy = from
					.join("tradestrategy");
			Predicate tradestrategyId = builder.equal(
					tradestrategy.get("idTradeStrategy"), id);

			Predicate isOpenTrue = builder.equal(from.get("isOpen"),
					new Boolean("true"));

			Predicate isOpenFalse = builder.equal(from.get("isOpen"),
					new Boolean("false"));
			Predicate totalQuantityNull = builder.isNull(from
					.get("totalQuantity"));

			Predicate predicate1 = builder.and(isOpenFalse, totalQuantityNull);
			Predicate predicate2 = builder.or(isOpenTrue, predicate1);

			query.where(builder.and(tradestrategyId, predicate2));

			TypedQuery<Trade> typedQuery = entityManager.createQuery(query);
			List<Trade> items = typedQuery.getResultList();
			for (Trade trade : items) {
				trade.getTradeOrders().size();
			}
			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return (Trade) items.get(0);
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
	 * Method findOpenTradeByContractId.
	 * 
	 * @param id
	 *            Integer
	 * @return Trade
	 */
	public Trade findOpenTradeByContractId(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Trade> query = builder.createQuery(Trade.class);
			Root<Trade> from = query.from(Trade.class);
			query.select(from);

			Join<Trade, Tradestrategy> tradestrategy = from
					.join("tradestrategy");

			Join<Tradestrategy, Contract> contract = tradestrategy
					.join("contract");

			Predicate contractId = builder
					.equal(contract.get("idContract"), id);

			Predicate isOpenTrue = builder.equal(from.get("isOpen"),
					new Boolean("true"));

			Predicate isOpenFalse = builder.equal(from.get("isOpen"),
					new Boolean("false"));
			Predicate totalQuantityNull = builder.isNull(from
					.get("totalQuantity"));

			Predicate predicate1 = builder.and(isOpenFalse, totalQuantityNull);
			Predicate predicate2 = builder.or(isOpenTrue, predicate1);

			query.where(builder.and(contractId, predicate2));

			TypedQuery<Trade> typedQuery = entityManager.createQuery(query);
			List<Trade> items = typedQuery.getResultList();
			for (Trade trade : items) {
				trade.getTradeOrders().size();
			}
			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return (Trade) items.get(0);
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
	 * Method findByTradestrategyId.
	 * 
	 * @param id
	 *            Integer
	 * @return List<Trade>
	 */
	public List<Trade> findByTradestrategyId(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Trade> query = builder.createQuery(Trade.class);
			Root<Trade> from = query.from(Trade.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();
			if (null != id) {
				Join<Trade, Tradestrategy> tradestrategy = from
						.join("tradestrategy");
				Predicate predicate = builder.equal(
						tradestrategy.get("idTradeStrategy"), id);
				predicates.add(predicate);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Trade> typedQuery = entityManager.createQuery(query);
			List<Trade> items = typedQuery.getResultList();
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
