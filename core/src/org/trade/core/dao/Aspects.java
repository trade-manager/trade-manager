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

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Aspects implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3388042483785305102L;
	private Integer idAspects;
	private boolean dirty = false;
	private List<Aspect> aspect = new ArrayList<Aspect>(0);

	public Aspects() {
	}

	/**
	 * Constructor for Aspects.
	 * 
	 * @param idAspects
	 *            Integer
	 */
	public Aspects(Integer idAspects) {
		this.idAspects = idAspects;
	}

	/**
	 * Constructor for Aspects.
	 * 
	 * @param aspect
	 *            List<Aspect>
	 */
	public Aspects(List<Aspect> aspect) {
		this.aspect = aspect;
	}

	/**
	 * Constructor for Aspects.
	 * 
	 * @param idAspects
	 *            Integer
	 * @param aspect
	 *            List<Aspect>
	 */
	public Aspects(Integer idAspects, List<Aspect> aspect) {
		this.idAspects = idAspects;
		this.aspect = aspect;
	}

	/**
	 * Method getIdAspects.
	 * 
	 * @return Integer
	 */
	public Integer getIdAspects() {
		return this.idAspects;
	}

	/**
	 * Method setIdAspects.
	 * 
	 * @param idAspects
	 *            Integer
	 */
	public void setIdAspects(Integer idAspects) {
		this.idAspects = idAspects;
	}

	/**
	 * Method add.
	 * 
	 * @param aspect
	 *            Aspect
	 */
	public void add(Aspect aspect) {
		this.aspect.add(aspect);
	}

	/**
	 * Method remove.
	 * 
	 * @param aspect
	 *            Aspect
	 */
	public void remove(Aspect aspect) {
		this.aspect.remove(aspect);
	}

	/**
	 * Method getAspect.
	 * 
	 * @return List<Aspect>
	 */
	public List<Aspect> getAspect() {
		return this.aspect;
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
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	public boolean isDirty() {
		for (Aspect aspect : this.getAspect()) {
			if (aspect.isDirty()) {
				return true;
			}
		}
		return this.dirty;
	}

	public void clear() {
		getAspect().clear();
	}
}
