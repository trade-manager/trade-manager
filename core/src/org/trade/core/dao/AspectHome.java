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
package org.trade.core.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 */
@Stateless
public class AspectHome {

	private EntityManager entityManager = null;

	public AspectHome() {

	}

	/**
	 * Method persist.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @return Aspect
	 */
	public synchronized  Aspect persist(Aspect transientInstance) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			if (null == transientInstance.getId()) {
				entityManager.persist(transientInstance);
				entityManager.getTransaction().commit();
				return transientInstance;
			} else {
				Aspect instance = entityManager.merge(transientInstance);
				entityManager.getTransaction().commit();
				transientInstance.setVersion(instance.getVersion());
				return instance;
			}

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method remove.
	 * 
	 * @param transientInstance
	 *            Aspect
	 */
	public synchronized void remove(Aspect transientInstance) {

		try {
			if (null != transientInstance.getId()) {
				entityManager = EntityManagerHelper.getEntityManager();
				entityManager.getTransaction().begin();
				Object aspect = entityManager
						.find(transientInstance.getClass(),
								transientInstance.getId());
				if (null != aspect) {
					entityManager.remove(aspect);
				}
				entityManager.getTransaction().commit();
			}

		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findByClassName.
	 * 
	 * @param className
	 *            String
	 * @return Aspects
	 * @throws ClassNotFoundException
	 */
	public Aspects findByClassName(String className)
			throws ClassNotFoundException {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Aspects aspects = new Aspects();
			Class<?> c = Class.forName(className);
			CriteriaBuilder criteriaBuilder = entityManager
					.getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
			Root<?> from = criteriaQuery.from(c);
			CriteriaQuery<Object> select = criteriaQuery.select(from);
			TypedQuery<Object> typedQuery = entityManager.createQuery(select);
			List<Object> items = typedQuery.getResultList();
			for (Object item : items) {
				aspects.add((Aspect) item);
			}
			entityManager.getTransaction().commit();
			return aspects;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} catch (ClassNotFoundException e) {
			throw e;

		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findByClassNameFieldName.
	 * 
	 * @param className
	 *            String
	 * @param fieldname
	 *            String
	 * @param value
	 *            String
	 * @return Aspects
	 * @throws ClassNotFoundException
	 */
	public Aspects findByClassNameFieldName(String className, String fieldname,
			String value) throws ClassNotFoundException {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Aspects aspects = new Aspects();
			Class<?> c = Class.forName(className);
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery = builder.createQuery();
			Root<?> from = criteriaQuery.from(c);
			CriteriaQuery<Object> query = criteriaQuery.select(from);
			if (null != fieldname) {
				query.where(builder.equal(from.get(fieldname), value));
			}
			TypedQuery<Object> typedQuery = entityManager.createQuery(query);
			List<Object> items = typedQuery.getResultList();
			for (Object item : items) {
				aspects.add((Aspect) item);
			}
			entityManager.getTransaction().commit();
			return aspects;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} catch (ClassNotFoundException e) {
			throw e;

		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method findById.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @return Aspect
	 */
	public <T extends Aspect> Aspect findById(Aspect transientInstance) {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Object aspect = entityManager.find(transientInstance.getClass(),
					transientInstance.getId());
			entityManager.getTransaction().commit();
			return (Aspect) aspect;
		} catch (RuntimeException re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}
}
