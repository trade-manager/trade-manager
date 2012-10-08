/***************************************************************************
 *   Copyright (C) 2009 by Paul Lutus                                      *
 *   lutusp@arachnoid.com                                                  *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/
package org.trade.core.util;

/**
 * 
 * @author lutusp
 * @version $Revision: 1.0 $
 */
public final class Pair {

	public double x = 0, y = 0;

	/**
	 * Constructor for Pair.
	 * @param x double
	 * @param y double
	 */
	public Pair(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Pair() {
	}

	/**
	 * Method toString.
	 * @return String
	 */
	@Override
	public String toString() {
		return x + "," + y;
	}
}
