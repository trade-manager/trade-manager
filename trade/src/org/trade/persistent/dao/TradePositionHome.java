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
public class TradePositionHome {

	private EntityManager entityManager = null;

	public TradePositionHome() {

	}

	/**
	 * Method remove.
	 * 
	 * @param transientInstance
	 *            TradePosition
	 */
	public void remove(TradePosition transientInstance) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			TradePosition tradePosition = entityManager
					.find(TradePosition.class,
							transientInstance.getIdTradePosition());
			if (null != tradePosition) {
				entityManager.remove(tradePosition);
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
	 * @return TradePosition
	 */
	public TradePosition findById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			TradePosition instance = entityManager
					.find(TradePosition.class, id);
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
	 * Method findOpenTradePositionByContractId.
	 * 
	 * @param id
	 *            Integer
	 * @return TradePosition
	 */
	public TradePosition findOpenTradePositionByContractId(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<TradePosition> query = builder
					.createQuery(TradePosition.class);
			Root<TradePosition> from = query.from(TradePosition.class);
			query.select(from);

			Join<TradePosition, Contract> contract = from.join("contract");
			Predicate contractId = builder
					.equal(contract.get("idContract"), id);

			Predicate isOpenTrue = builder.equal(from.get("isOpen"),
					new Boolean("true"));
			query.where(builder.and(contractId, isOpenTrue));

			TypedQuery<TradePosition> typedQuery = entityManager
					.createQuery(query);
			List<TradePosition> items = typedQuery.getResultList();
			for (TradePosition tradePosition : items) {
				tradePosition.getTradeOrders().size();
			}
			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return (TradePosition) items.get(0);
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
