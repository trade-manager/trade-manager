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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A generic facade that provides easy access to a JPA persistence unit using
 * static methods.
 * </p>
 * <p>
 * This static class is designed so that it can be used with any JPA
 * application.
 * </p>
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class EntityManagerHelper {

	/**
	 * <p>
	 * Declare the persistence unit for this EntityManagerHelper ("entity").
	 * </p>
	 * <p>
	 * This is the only setting that might need to be changed between
	 * applications. Otherwise, this class can be dropped into any JPA
	 * application.
	 * </p>
	 */
	static final String PERSISTENCE_UNIT = "dbresource";

	private static final EntityManagerFactory factory;
	private static final ThreadLocal<EntityManager> threadLocal;
	private final static Logger _log = LoggerFactory
			.getLogger(EntityManagerHelper.class);

	static {
		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
		threadLocal = new ThreadLocal<EntityManager>();
	}

	/**
	 * <p>
	 * Provide a per-thread EntityManager "singleton" instance.
	 * </p>
	 * <p>
	 * This method can be called as many times as needed per thread, and it will
	 * return the same EntityManager instance, until the manager is closed.
	 * </p>
	 * 
	 * 
	 * @return EntityManager singleton for this thread
	 */
	public static EntityManager getEntityManager() {
		EntityManager manager = threadLocal.get();
		if ((manager == null) || !manager.isOpen()) {
			manager = factory.createEntityManager();
			threadLocal.set(manager);
		}
		return manager;
	}

	/**
	 * <p>
	 * Provide a per-thread EntityManager "singleton" instance.
	 * </p>
	 * <p>
	 * This method can be called as many times as needed per thread, and it will
	 * return the same EntityManager instance, until the manager is closed.
	 * </p>
	 * 
	 * 
	 * @return EntityManager local entity manager instance. This instance should
	 *         be method managed. i.e. begin, commit, close transaction.
	 */
	public static EntityManager getLocalEntityManager() {
		EntityManager manager = factory.createEntityManager();
		return manager;
	}

	/**
	 * <p>
	 * Close the EntityManager and set the thread's instance to null.
	 * </p>
	 */
	public static void close() {
		EntityManager em = threadLocal.get();
		if (em != null) {
			em.close();
		}
		threadLocal.remove();
	}

	/**
	 * <p>
	 * Initiate a transaction for the EntityManager on this thread.
	 * </p>
	 * <p>
	 * The Transaction will remain open until commit or closeEntityManager is
	 * called.
	 * </p>
	 */
	public static void begin() {
		getEntityManager().getTransaction().begin();
	}

	/**
	 * <p>
	 * Submit the changes to the persistance layer.
	 * </p>
	 * <p>
	 * Until commit is called, rollback can be used to undo the transaction.
	 * </p>
	 */
	public static void commit() {
		getEntityManager().getTransaction().commit();
	}

	/**
	 * <p>
	 * Create a query for the EntityManager on this thread.
	 * </p>
	 * 
	 * @param query
	 *            String
	 * @return Query
	 */
	public static Query createQuery(String query) {
		return getEntityManager().createQuery(query);
	}

	/**
	 * <p>
	 * Flush the EntityManager state on this thread.
	 * </p>
	 */
	public static void flush() {
		if (getEntityManager().getTransaction().isActive()) {
			getEntityManager().flush();
		}
	}

	/**
	 * <p>
	 * Flush the EntityManager state on this thread.
	 * </p>
	 */
	public static void clear() {
		getEntityManager().clear();
	}

	/**
	 * <p>
	 * Write an error message to the logging system.
	 * </p>
	 * 
	 * @param info
	 *            String
	 * @param ex
	 *            Throwable
	 */
	public static void logError(String info, Throwable ex) {
		_log.error(info, ex);
	}

	/**
	 * <p>
	 * Undo an uncommitted transaction, in the event of an error or other
	 * problem.
	 * </p>
	 */
	public static void rollback() {
		if ((getEntityManager().getTransaction() != null)
				&& getEntityManager().getTransaction().isActive()) {
			getEntityManager().getTransaction().rollback();
		}
	}
}
