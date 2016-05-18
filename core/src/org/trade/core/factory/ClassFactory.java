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
package org.trade.core.factory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.properties.ConfigProperties;
import org.trade.core.properties.PropertyNotFoundException;

/**
 * Provides a simple class factory. Provide a string name for an interface, and
 * this class will provide the implementation.
 * 
 * @author Simon Allen
 */
public class ClassFactory {

	private ClassFactory() {
	}

	private final static Logger _log = LoggerFactory.getLogger(ClassFactory.class);

	/**
	 * Method getServiceForInterface.
	 * 
	 * @param theInterface
	 *            String
	 * @param context
	 *            Object
	 * @return Object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public static Object getServiceForInterface(String theInterface, Object context)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		String className = ConfigProperties.getPropAsString(theInterface);

		if (className == null) {
			throw new PropertyNotFoundException("The interface \"" + theInterface + "\" could not be located.  Check "
					+ "to see if the property file \"" + ConfigProperties.getDeploymentPropertyFileName()
					+ "\" is installed and available.");
		}
		Class<?>[] args = new Class[1];
		args[0] = Object.class;

		Vector<Object> parm = new Vector<Object>();
		return getCreateClass(className, parm, context);
	}

	/**
	 * Method getCreateClass.
	 * 
	 * @param className
	 *            String
	 * @param parm
	 *            Vector<Object>
	 * @param context
	 *            Object
	 * @return Object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public static Object getCreateClass(String className, Vector<Object> parm, Object context)
			throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		int vectorSize = 0;
		vectorSize = parm.size();
		Object instance = null;

		Class<?>[] parms = new Class[vectorSize];
		Object[] object = new Object[vectorSize];
		StringBuffer classes = new StringBuffer();
		int i = 0;
		for (Object obj : parm) {
			if (classes.length() == 0) {
				classes.append(obj.getClass().getName());
			} else {
				classes.append("," + obj.getClass().getName());
			}
			parms[i] = obj.getClass();
			object[i] = obj;
			i++;
		}

		Constructor<?> constructor = null;
		Class<?> c = Class.forName(className);

		try {
			constructor = c.getDeclaredConstructor(parms);
			instance = constructor.newInstance(object);
		} catch (Exception e) {

			_log.debug("Could not find constructor for default parms[" + classes + "] will test all constructors.");
			Constructor<?>[] constructors = c.getConstructors();
			for (Constructor<?> constructor2 : constructors) {
				try {
					instance = constructor2.newInstance(object);
					if (null != instance) {
						_log.info(
								"Found constructor: " + constructor2.toGenericString() + " for parms[" + classes + "]");
						break;
					}
				} catch (Exception ex) {
					_log.error("Constructor: " + constructor2.toGenericString() + " failed!!");
				}
			}
		}
		if (null == instance) {
			instance = c.newInstance();
		}

		return instance;
	}
}
