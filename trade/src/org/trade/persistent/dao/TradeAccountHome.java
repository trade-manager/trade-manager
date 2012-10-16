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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.trade.core.dao.EntityManagerHelper;

/**
 */
@Stateless
public class TradeAccountHome {
	private EntityManager entityManager = null;

	public TradeAccountHome() {

	}

	/**
	 * Method findById.
	 * @param id Integer
	 * @return TradeAccount
	 */
	public TradeAccount findById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			TradeAccount instance = entityManager.find(TradeAccount.class, id);
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
	 * Method findByAccountNumber.
	 * @param accountNumber String
	 * @return TradeAccount
	 */
	public TradeAccount findByAccountNumber(String accountNumber) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<TradeAccount> query = builder
					.createQuery(TradeAccount.class);
			Root<TradeAccount> from = query.from(TradeAccount.class);
			query.select(from);
			query.where(builder.equal(from.get("accountNumber"), accountNumber));
			List<TradeAccount> items = entityManager.createQuery(query)
					.getResultList();
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
	 * Method resetDefaultTradeAccount.
	 * @param defaultTradeAccount TradeAccount
	 */
	public void resetDefaultTradeAccount(TradeAccount defaultTradeAccount) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<TradeAccount> query = builder
					.createQuery(TradeAccount.class);
			Root<TradeAccount> from = query.from(TradeAccount.class);
			query.select(from);
			List<TradeAccount> items = entityManager.createQuery(query)
					.getResultList();
			for (TradeAccount tradeAccount : items) {
				if (tradeAccount.getIsDefault()
						&& !defaultTradeAccount.getIdTradeAccount().equals(
								tradeAccount.getIdTradeAccount())) {
					tradeAccount.setIsDefault(false);
					entityManager.persist(tradeAccount);					
				}
			}
			entityManager.getTransaction().commit();
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}
}