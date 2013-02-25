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
public class FinancialAccountHome {
	private EntityManager entityManager = null;

	public FinancialAccountHome() {

	}

	/**
	 * Method findById.
	 * 
	 * @param id
	 *            Integer
	 * @return FinancialAccount
	 */
	public FinancialAccount findById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			FinancialAccount instance = entityManager.find(
					FinancialAccount.class, id);
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
	 * Method findByProgileName.
	 * 
	 * @param profileName
	 *            String
	 * @return FinancialAccount
	 */
	public FinancialAccount findByProfileName(String profileName) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<FinancialAccount> query = builder
					.createQuery(FinancialAccount.class);
			Root<FinancialAccount> from = query.from(FinancialAccount.class);
			query.select(from);
			query.where(builder.equal(from.get("profileName"), profileName));
			List<FinancialAccount> items = entityManager.createQuery(query)
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
	 * Method findByGroupName.
	 * 
	 * @param groupName
	 *            String
	 * @return FinancialAccount
	 */
	public FinancialAccount findByGroupName(String groupName) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<FinancialAccount> query = builder
					.createQuery(FinancialAccount.class);
			Root<FinancialAccount> from = query.from(FinancialAccount.class);
			query.select(from);
			query.where(builder.equal(from.get("groupName"), groupName));
			List<FinancialAccount> items = entityManager.createQuery(query)
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
}
