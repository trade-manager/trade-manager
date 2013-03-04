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
public class PortfolioHome {
	private EntityManager entityManager = null;

	public PortfolioHome() {

	}

	/**
	 * Method findById.
	 * 
	 * @param id
	 *            Integer
	 * @return Portfolio
	 */
	public Portfolio findById(Integer id) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Portfolio instance = entityManager.find(Portfolio.class, id);
			instance.getPortfolioAccounts().size();
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
	 * Method findAll.
	 * 
	 * @return List<Portfolio>
	 */
	public List<Portfolio> findAll() {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Portfolio> query = builder
					.createQuery(Portfolio.class);
			Root<Portfolio> from = query.from(Portfolio.class);
			query.select(from);
			List<Portfolio> items = entityManager.createQuery(query)
					.getResultList();
			for (Portfolio portfolio : items) {
				portfolio.getPortfolioAccounts().size();
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
	 * Method findByName.
	 * 
	 * @param name
	 *            String
	 * @return Portfolio
	 */
	public Portfolio findByName(String name) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Portfolio> query = builder
					.createQuery(Portfolio.class);
			Root<Portfolio> from = query.from(Portfolio.class);
			query.select(from);
			query.where(builder.equal(from.get("name"), name));
			List<Portfolio> items = entityManager.createQuery(query)
					.getResultList();
			for (Portfolio item : items) {
				item.getPortfolioAccounts().size();
			}
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
	 * Method findByMasterAccountNumber.
	 * 
	 * @param accountNumber
	 *            String
	 * @return Portfolio
	 */
	public Portfolio findByMasterAccountNumber(String accountNumber) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Portfolio> query = builder
					.createQuery(Portfolio.class);
			Root<Portfolio> from = query.from(Portfolio.class);
			query.select(from);
			query.where(builder.equal(from.get("masterAccountNumber"),
					accountNumber));
			List<Portfolio> items = entityManager.createQuery(query)
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
	 * Method resetDefaultPortfolio.
	 * 
	 * @param defaultPortfolio
	 *            Portfolio
	 */
	public void resetDefaultPortfolio(Portfolio defaultPortfolio) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Portfolio> query = builder
					.createQuery(Portfolio.class);
			Root<Portfolio> from = query.from(Portfolio.class);
			query.select(from);
			List<Portfolio> items = entityManager.createQuery(query)
					.getResultList();
			for (Portfolio portfolio : items) {
				if (portfolio.getIsDefault()
						&& !defaultPortfolio.getIdPortfolio().equals(
								portfolio.getIdPortfolio())) {
					portfolio.setIsDefault(false);
					entityManager.persist(portfolio);
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
