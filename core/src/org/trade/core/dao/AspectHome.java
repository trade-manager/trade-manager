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
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Path.Node;

/**
 */
@Stateless
public class AspectHome {

	public AspectHome() {

	}

	/**
	 * Method persist.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @return Aspect
	 * @throws Exception
	 */
	public synchronized <T extends Aspect> T persist(T transientInstance)
			throws Exception {
		return persist(transientInstance, false);
	}

	/**
	 * Method persist.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @param overrideVersion
	 *            boolean
	 * @return Aspect
	 * @throws Exception
	 */

	public synchronized <T extends Aspect> T persist(T transientInstance,
			boolean overrideVersion) throws Exception {

		try {

			validate(transientInstance);
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();

			if (null == transientInstance.getId()) {
				entityManager.persist(transientInstance);
				entityManager.getTransaction().commit();
				transientInstance.setDirty(false);
				return transientInstance;
			} else {
				if (overrideVersion) {
					Aspect aspect = entityManager.find(
							transientInstance.getClass(),
							transientInstance.getId());
					transientInstance.setVersion(aspect.getVersion());
				}
				T instance = entityManager.merge(transientInstance);
				entityManager.getTransaction().commit();
				instance.setDirty(false);
				return instance;
			}

		} catch (Exception re) {
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
				EntityManager entityManager = EntityManagerHelper
						.getEntityManager();
				entityManager.getTransaction().begin();
				Object aspect = entityManager
						.find(transientInstance.getClass(),
								transientInstance.getId());
				if (null != aspect) {
					entityManager.remove(aspect);
				}
				entityManager.getTransaction().commit();
			}

		} catch (Exception re) {
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
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
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
		} catch (Exception re) {
			EntityManagerHelper.rollback();
			throw re;
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
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
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
		} catch (Exception re) {
			EntityManagerHelper.rollback();
			throw re;
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
			EntityManager entityManager = EntityManagerHelper
					.getEntityManager();
			entityManager.getTransaction().begin();
			Object aspect = entityManager.find(transientInstance.getClass(),
					transientInstance.getId());
			entityManager.getTransaction().commit();
			return (Aspect) aspect;
		} catch (Exception re) {
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
	}

	/**
	 * Method validate.
	 * 
	 * @param transientInstance
	 *            Aspect
	 * @return boolean
	 * @throws Exception
	 */

	public boolean validate(Aspect transientInstance) throws Exception {

		Set<ConstraintViolation<Aspect>> constraintViolations = EntityManagerHelper
				.getValidator().validate(transientInstance);

		if (!constraintViolations.isEmpty()) {
			String errorMsg = "";
			for (ConstraintViolation<Aspect> error : constraintViolations) {
				Path path = error.getPropertyPath();
				for (Node node : path) {
					errorMsg = errorMsg + " Column: " + node.getName()
							+ " Value: " + error.getInvalidValue();
				}
				errorMsg = errorMsg + " " + error.getMessage();
			}
			throw new Exception(errorMsg);
		}
		return true;
	}
}
