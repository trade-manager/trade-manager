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
package org.trade.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.trade.ui.base.Parametric;

/**
 * 
 * @version $Id: Reflector.java,v 1.1 2001/10/18 01:32:15 simon Exp $
 * @author Simon Allen
 */
public class Reflector extends Object {
	/**
	 * Find the specified method by walking the classes class hierarchy.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param cl
	 *            Class<?>
	 * @param name
	 *            String
	 * @param params
	 *            Class<?>[]
	 * @return The specified method if found, null otherwise.
	 */
	public static Method findMethod(Class<?> cl, String name, Class<?>[] params) {
		Method m = null;
		boolean notFound = true;
		Class<?> currentCl = cl;

		while (notFound) {
			try {
				m = currentCl.getDeclaredMethod(name, params);
				notFound = false;
			} catch (Exception e) {
				currentCl = currentCl.getSuperclass();

				if (currentCl == null) {
					// Break the loop - the method does not exist in the classes
					// class hierarchy
					notFound = false;

					break;
				}
			}
		}

		// if the method was not found, perform a more expensive polymorphic
		// test
		if (null == m) {
			Parametric p = new Parametric(cl);

			m = p.findMethod(name, params);
		}

		return (m);
	}

	/**
	 * Find the specified method by walking the classes class hierarchy.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param cl
	 *            Class<?>
	 * @param name
	 *            String
	 * @return The specified method if found, null otherwise.
	 */
	public static Field findField(Class<?> cl, String name) {
		Field m = null;
		boolean notFound = true;
		Class<?> currentCl = cl;

		while (notFound) {
			try {
				m = currentCl.getDeclaredField(name);
				notFound = false;
			} catch (Exception e) {
				currentCl = currentCl.getSuperclass();

				if (currentCl == null) {
					// Break the loop - the Field does not exist in the classes
					// class hierarchy
					notFound = false;

					break;
				}
			}
		}

		// if the Field was not found, perform a more expensive polymorphic test
		if (null == m) {
			Parametric p = new Parametric(cl);

			m = p.findField(name);
		}

		return (m);
	}
}
