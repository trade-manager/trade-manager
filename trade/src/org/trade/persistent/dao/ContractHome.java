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
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.trade.core.dao.EntityManagerHelper;
import org.trade.core.util.TradingCalendar;

/**
 */
@Stateless
public class ContractHome {

	public ContractHome() {

	}

	/**
	 * Method findByUniqueKey.
	 * 
	 * @param SECType
	 *            String
	 * @param symbol
	 *            String
	 * @param exchange
	 *            String
	 * @param currency
	 *            String
	 * @param expiryDate
	 *            Date
	 * @return Contract
	 */
	public Contract findByUniqueKey(String SECType, String symbol,
			String exchange, String currency, Date expiryDate) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Contract> query = builder.createQuery(Contract.class);
			Root<Contract> from = query.from(Contract.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();

			if (null != SECType) {
				Predicate predicate = builder.equal(from.get("secType"),
						SECType);
				predicates.add(predicate);
			}
			if (null != symbol) {
				Predicate predicate = builder.equal(from.get("symbol"), symbol);
				predicates.add(predicate);
			}
			if (null != exchange) {
				Predicate predicate = builder.equal(from.get("exchange"),
						exchange);
				predicates.add(predicate);
			}
			if (null != currency) {
				Predicate predicate = builder.equal(from.get("currency"),
						currency);
				predicates.add(predicate);
			}
			if (null != expiryDate) {

				Integer yearExpiry = TradingCalendar.getYear(expiryDate);
				Expression<Integer> year = builder.function("year",
						Integer.class, from.get("expiry"));
				Predicate predicateYear = builder.equal(year, yearExpiry);
				predicates.add(predicateYear);

				Integer monthExpiry = TradingCalendar.getMonth(expiryDate);
				Expression<Integer> month = builder.function("month",
						Integer.class, from.get("expiry"));
				Predicate predicateMonth = builder.equal(month, new Integer(
						1 + monthExpiry.intValue()));
				predicates.add(predicateMonth);
			}
			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<Contract> typedQuery = entityManager.createQuery(query);
			List<Contract> items = typedQuery.getResultList();
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
	 * Method findById.
	 * 
	 * @param id
	 *            Integer
	 * @return Contract
	 */
	public Contract findById(Integer id) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			Contract instance = entityManager.find(Contract.class, id);
			if (null != instance) {
				instance.getTradePositions().size();
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
	 * Method findByContractId.
	 * 
	 * @param id
	 *            Integer
	 * @return ContractId
	 */
	public ContractId findByContractId(Integer id) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			ContractId instance = entityManager.find(ContractId.class, id);
			// if (null != instance) {
			// instance.getTradePositions().size();
			// }
			entityManager.getTransaction().commit();
			return instance;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}
}
