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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.trade.core.properties.ConfigProperties;

/**
 * Implementation of the LookupServiceProvider interface that uses the
 * devtool.properties.ConfigProperties object for obtaining Lookup information.
 * 
 * @author Simon Allen
 */
public class PropertyFileLookupServiceProvider implements LookupServiceProvider {
	// This will be a hashtable of hashtables of Lookup objects. The first
	// key is the lookup name and the second key is the LookupQualifier.
	private static Hashtable<String, Hashtable<String, Lookup>> _lookups = new Hashtable<String, Hashtable<String, Lookup>>();

	/**
	 * Default Constructor
	 */
	public PropertyFileLookupServiceProvider() {
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
	public Lookup getLookup(String lookupName, LookupQualifier qualifier, boolean none)
			throws LookupException {
		Lookup lookup = getCachedLookup(lookupName, qualifier);

		if (null == lookup) {
			try {
				Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
				Vector<String> colNames = new Vector<String>();
				Enumeration<?> en = ConfigProperties
						.getPropAsEnumeration(lookupName + "_PropertyFile");

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

		// Need to clone the object otherwise changes in position in
		// the object returned would effect everyone using the object
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
}
