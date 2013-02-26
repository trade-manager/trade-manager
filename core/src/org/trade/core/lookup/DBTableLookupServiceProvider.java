/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
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
package org.trade.core.lookup;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.EntityManagerHelper;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.util.Reflector;

import com.sun.xml.internal.bind.v2.ClassFactory;

/**
 * Implementation of the LookupServiceProvider interface that uses the
 * devtool.properties.ConfigProperties object for obtaining Lookup information.
 * 
 * @author Simon Allen
 */
public class DBTableLookupServiceProvider implements LookupServiceProvider {
	/*
	 * This will be a hashtable of hashtables of Lookup objects. The first key
	 * is the lookup name and the second key is the LookupQualifier.
	 */
	private final static Logger _log = LoggerFactory
			.getLogger(DBTableLookupServiceProvider.class);
	private static Hashtable<String, Hashtable<String, Lookup>> _lookups = new Hashtable<String, Hashtable<String, Lookup>>();
	private EntityManager entityManager = null;

	/**
	 * Default Constructor
	 */
	public DBTableLookupServiceProvider() {
	}

	public static void clearLookup() {
		_lookups.clear();
	}

	/**
	 * Method getLookup.
	 * 
	 * @param lookupName
	 *            String
	 * @param qualifier
	 *            LookupQualifier
	 * @return Lookup
	 * @throws LookupException
	 * @see org.trade.core.lookup.LookupServiceProvider#getLookup(String,
	 *      LookupQualifier)
	 */
	public synchronized Lookup getLookup(String lookupName,
			LookupQualifier qualifier, boolean none) throws LookupException {
		Lookup lookup = getCachedLookup(lookupName, qualifier);

		if (null == lookup) {
			try {
				Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
				Vector<String> colNames = new Vector<String>();
				Enumeration<?> en = ConfigProperties
						.getPropAsEnumeration(lookupName + "_DBTable");

				while (en.hasMoreElements()) {
					colNames.addElement((String) en.nextElement());
				}

				// Have all of the columns - want to get a vector for each
				// column value
				Vector<Enumeration<?>> colRows = new Vector<Enumeration<?>>();
				int i;
				int colNamesSize = colNames.size();

				for (i = 0; i < colNamesSize; i++) {
					colRows.addElement(ConfigProperties
							.getPropAsEnumeration(colNames.elementAt(i)));
				}

				// Now construct a Vector Vector - representing the table of
				// data
				boolean exit = false;

				do {
					Vector<Object> row = new Vector<Object>();
					boolean foundOne = false;
					boolean addIt = true;
					int colRowsSize = colRows.size();

					for (i = 0; i < colRowsSize; i++) {
						Object value = null;

						en = colRows.elementAt(i);

						if (en.hasMoreElements()) {
							foundOne = true;
							value = en.nextElement();

							row.addElement(value);
						} else {
							// Represent an empty value
							row.addElement("");
						}

						// Check to see if the returned lookup is to be
						// constrained
						if (foundOne && (qualifier != null)) {
							Object qualVal = qualifier.getValue(""
									+ colNames.elementAt(i));

							if (null != qualVal) {
								if (!qualVal.equals(value)) {
									addIt = false;
								}
							}
						}
					}

					if (foundOne) {
						if (addIt) {
							rows.addElement(row);
						}
					} else {
						exit = true;
					}
				} while (!exit);

				// There should be only one row per table that
				// contains the DAO name and method name for the display name
				String dao = null;
				String type = null;
				String methodName = null;
				int rowsSize = rows.size();
				for (i = 0; i < rowsSize; i++) {
					Vector<Object> row = rows.elementAt(i);
					int rowSize = row.size();

					for (int y = 0; y < rowSize; y++) {

						if ("DAO_DECODE_TYPE".equals(colNames.elementAt(y))) {
							type = (String) row.elementAt(y);

						} else if ("DAO_DECODE_CODE".equals(colNames
								.elementAt(y))) {
							dao = (String) row.elementAt(y);
						} else if ("DAO_DECODE_DISPLAY_NAME".equals(colNames
								.elementAt(y))) {
							methodName = (String) row.elementAt(y);
						}
					}
					// Clear the first row and add the objects and display name
					// from the DB
					rows.clear();
					/*
					 * Add the None selected row.
					 */
					if(none){
						Vector<Object> newRowNone = new Vector<Object>();
						Class<?> clazz = Class.forName(dao);
						Object daoObjectNone = ClassFactory.create(clazz);
						newRowNone.add(type);
						newRowNone.add(daoObjectNone);
						newRowNone.add("None");
						rows.add(newRowNone);		
					}

					List<?> codes = getCodes(dao);
					for (Object daoObject : codes) {
						Vector<Object> newRow = new Vector<Object>();
						Method method = Reflector.findMethod(
								daoObject.getClass(), methodName, null);
						if (null != method) {
							Object[] o = new Object[0];
							Object displayNameValue = method.invoke(daoObject,
									o);
							newRow.add(type);
							newRow.add(daoObject);
							newRow.add(displayNameValue);
						}
						rows.add(newRow);
					}
				}

				// If rows where found then I managed to provide the lookup
				if (rows.size() > 0) {
					lookup = new PropertiesLookup(colNames, rows);
				}
			} catch (Throwable t) {
				// If this occurs means this provider is unable to provide
				// the lookup ignore the exception.
			}
			if (null != lookup) {
				addLookupToCache(lookupName, qualifier, lookup);
			}
		}

		return lookup;
	}

	/**
	 * Returns null if the lookup is not in the cache.
	 * 
	 * @param lookupName
	 *            String
	 * @param qualifier
	 *            LookupQualifier
	 * @return Lookup
	 */
	private Lookup getCachedLookup(String lookupName, LookupQualifier qualifier) {
		Lookup lookup = null;
		Hashtable<?, ?> lookupsByQualifier = _lookups.get(lookupName);

		if (null != lookupsByQualifier) {
			lookup = (Lookup) lookupsByQualifier.get(qualifier.toString());
		}

		/*
		 * Need to clone the object otherwise changes in position in the object
		 * returned would effect everyone using the object.
		 */
		if (null != lookup) {
			lookup = (Lookup) lookup.clone();
		}

		return (lookup);
	}

	/**
	 * Method addLookupToCache.
	 * 
	 * @param lookupName
	 *            String
	 * @param qualifier
	 *            LookupQualifier
	 * @param lookup
	 *            Lookup
	 */
	private synchronized void addLookupToCache(String lookupName,
			LookupQualifier qualifier, Lookup lookup) {
		Hashtable<String, Lookup> lookupsByQualifier = _lookups.get(lookupName);

		if (null == lookupsByQualifier) {
			lookupsByQualifier = new Hashtable<String, Lookup>();
			_lookups.put(lookupName, lookupsByQualifier);
		}

		lookupsByQualifier.put(qualifier.toString(), lookup);
	}

	/**
	 * Method getCodes.
	 * 
	 * @param className
	 *            String
	 * @return List<?>
	 * @throws ClassNotFoundException
	 */
	private synchronized List<?> getCodes(String className)
			throws ClassNotFoundException {

		try {
			entityManager = EntityManagerHelper.getEntityManager();
			entityManager.getTransaction().begin();
			Class<?> c = Class.forName(className);
			CriteriaBuilder criteriaBuilder = entityManager
					.getCriteriaBuilder();
			CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery();
			Root<?> from = criteriaQuery.from(c);
			CriteriaQuery<Object> select = criteriaQuery.select(from);
			TypedQuery<Object> typedQuery = entityManager.createQuery(select);
			List<Object> items = typedQuery.getResultList();
			entityManager.getTransaction().commit();
			if (items.size() > 0) {
				return items;
			}

		} catch (RuntimeException re) {
			_log.error("Error : " + re.getMessage(), re);
			EntityManagerHelper.rollback();
			throw re;
		} finally {
			EntityManagerHelper.close();
		}
		return new ArrayList<Object>(0);
	}
}
