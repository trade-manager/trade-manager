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
public class CodeTypeHome {

	public CodeTypeHome() {

	}

	/**
	 * Method findById.
	 * 
	 * @param idCodeType
	 *            Integer
	 * @return CodeType
	 */
	public CodeType findById(Integer idCodeType) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CodeType instance = entityManager.find(CodeType.class, idCodeType);
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
	 * Method findByName.
	 * 
	 * @param name
	 *            String
	 * @return CodeType
	 */
	public CodeType findByName(String name) {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<CodeType> query = builder.createQuery(CodeType.class);
			Root<CodeType> from = query.from(CodeType.class);
			query.select(from);
			query.where(builder.equal(from.get("name"), name));
			List<CodeType> items = entityManager.createQuery(query)
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
	 * Method findByAttributeName.
	 * 
	 * @param codeTypeName
	 *            String
	 * @param codeAttributeName
	 *            String
	 * @return CodeValue
	 */
	public CodeValue findByAttributeName(String codeTypeName,
			String codeAttributeName) {

		try {

			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<CodeValue> query = builder
					.createQuery(CodeValue.class);
			Root<CodeValue> from = query.from(CodeValue.class);
			query.select(from);
			List<Predicate> predicates = new ArrayList<Predicate>();
			if (null != codeAttributeName) {
				Join<CodeValue, CodeAttribute> codeAttribute = from
						.join("codeAttribute");
				Predicate predicate = builder.equal(codeAttribute.get("name"),
						codeAttributeName);
				predicates.add(predicate);
				Join<CodeAttribute, CodeType> codeType = codeAttribute
						.join("codeType");
				Predicate predicate1 = builder.equal(codeType.get("name"),
						codeTypeName);
				predicates.add(predicate1);
			}

			query.where(predicates.toArray(new Predicate[] {}));
			TypedQuery<CodeValue> typedQuery = entityManager.createQuery(query);
			List<CodeValue> items = typedQuery.getResultList();
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
	 * Method findAll.
	 * 
	 * @return List<CodeType>
	 */
	public List<CodeType> findAll() {

		try {
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<CodeType> query = builder.createQuery(CodeType.class);
			Root<CodeType> from = query.from(CodeType.class);
			query.select(from);
			List<CodeType> items = entityManager.createQuery(query)
					.getResultList();
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
