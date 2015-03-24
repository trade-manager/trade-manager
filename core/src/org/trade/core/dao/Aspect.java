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

import java.io.Serializable;

/**
 */
public abstract class Aspect extends Object implements Serializable {

	/**
	 * 
	 */
	protected static Boolean m_ascending = new Boolean(true);
	protected Integer id;
	protected Integer version;
	private static final long serialVersionUID = -2295788967071036093L;
	private Object m_context;
	private boolean dirty = false;

	public Aspect() {
	}

	/**
	 * Constructor for Aspect.
	 * 
	 * @param context
	 *            Object
	 */
	public Aspect(Object context) {
		m_context = context;
	}

	/**
	 * Method getContext.
	 * 
	 * @return Object
	 */
	public Object getContext() {
		return (m_context);
	}

	/**
	 * Method getId.
	 * 
	 * @return Integer
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Method setId.
	 * 
	 * @param id
	 *            Integer
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Method getVersion.
	 * 
	 * @return Integer
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * Method setVersion.
	 * 
	 * @param version
	 *            Integer
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Method setDirty.
	 * 
	 * @param dirty
	 *            boolean
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	/**
	 * Method equals.
	 * 
	 * @param objectToCompare
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object objectToCompare) {

		if (this == objectToCompare) {
			return true;
		}
		if (objectToCompare == null) {
			return false;
		}
		if (!(objectToCompare instanceof Aspect)) {
			return false;
		}
		if (null == this.getId())
			return false;
		if (this.getClass().equals(objectToCompare.getClass())) {
			if (this.getId().equals(((Aspect) objectToCompare).getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method hashCode.
	 * 
	 * For every field tested in the equals-Method, calculate a hash code c by:
	 * 
	 * If the field f is a boolean: calculate * (f ? 0 : 1);
	 * 
	 * If the field f is a byte, char, short or int: calculate (int)f;
	 * 
	 * If the field f is a long: calculate (int)(f ^ (f >>> 32));
	 * 
	 * If the field f is a float: calculate Float.floatToIntBits(f);
	 * 
	 * If the field f is a double: calculate Double.doubleToLongBits(f) and
	 * handle the return value like every long value;
	 * 
	 * If the field f is an object: Use the result of the hashCode() method or 0
	 * if f == null;
	 * 
	 * If the field f is an array: See every field as separate element and
	 * calculate the hash value in a recursive fashion and combine the values as
	 * described next.
	 * 
	 * @return int
	 */
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + (this.getId() == null ? 0 : this.getId().hashCode());
		hash = hash
				* 31
				+ (this.getVersion() == null ? 0 : this.getVersion().hashCode());
		return hash;
	}
}
