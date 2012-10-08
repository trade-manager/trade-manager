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
package org.trade.core.lookup;

import java.util.Enumeration;
import java.util.Hashtable;
/**
 * Used to describe the access of dynamic key value pairs that can be used by a
 * LookupServiceProvider to constrain the lookup returned.
 * 

 * 
 * @version $Id: LookupQualifier.java,v 1.1 2001/11/06 16:52:41 simon Exp $
 * @author Simon Allen
 */
public class LookupQualifier {
	//
	// Private Attributes
	//
	private Hashtable<String, Object> m_values = new Hashtable<String, Object>();

	//
	// Public Methods
	//
	/**
	 * Default Constructor
	 */
	public LookupQualifier() {
	}

	/**
	
	 * @return An Enumeration of all of the qualifying keys. */
	public Enumeration<String> getKeys() {
		return (m_values.keys());
	}

	/**
	
	 * @return An Enumeration of all of the qualifying values. */
	public Enumeration<Object> getValues() {
		return (m_values.elements());
	}

	/**
	 * Get the specified value.
	 * 
	
	
	 * @param key String
	 * @return The value if it exists, null otherwise. */
	public Object getValue(String key) {
		return (m_values.get(key));
	}

	/**
	 * Set the specified value.
	 * 
	
	
	 * @param key String
	 * @param value Object
	 */
	public void setValue(String key, Object value) {
		m_values.put(key, value);
	}

	/**
	 * Remove the specified value from the qualifier.
	 * 
	
	 * @param key String
	 */
	public void removeValue(String key) {
		m_values.remove(key);
	}

	/**
	 * Method toString.
	 * @return String
	 */
	public String toString() {
		return m_values.toString();
	}

	/**
	 * Method equals.
	 * @param objectToCompare Object
	 * @return boolean
	 */
	public boolean equals(Object objectToCompare) {
		if (this == objectToCompare) {
			return true;
		}
		if (objectToCompare == null) {
			return false;
		}
		if (!(objectToCompare instanceof LookupQualifier)) {
			return false;
		}

		LookupQualifier other = (LookupQualifier) objectToCompare;

		if (toString().equals(other.toString())) {
			return true;
		}

		return false;
	}

	/**
	 * Method hashcode.
	 * @return int
	 */
	public int hashcode() {
		return toString().hashCode();
	}
}
