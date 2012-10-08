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
package org.trade.strategy.data.volume;

/**
 */
public class Volume implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7644763985378994305L;

	private Long volume;
	private boolean side = false;

	public Volume() {
	}

	/**
	 * Constructor for Volume.
	 * @param volume Long
	 * @param side boolean
	 */
	public Volume(Long volume, boolean side) {
		this.volume = volume;
		this.side = side;
	}

	/**
	 * Method getVolume.
	 * @return Long
	 */
	public Long getVolume() {
		return this.volume;
	}

	/**
	 * Method setVolume.
	 * @param volume Long
	 */
	public void setVolume(Long volume) {
		this.volume = volume;
	}

	/**
	 * Method isSide.
	 * @return boolean
	 */
	public boolean isSide() {
		return this.side;
	}

	/**
	 * Method setSide.
	 * @param side boolean
	 */
	public void setSide(boolean side) {
		this.side = side;
	}
}
