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
package org.trade.ui.base;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * This class performs some useful reflection type functionality when the target
 * method is overloaded by other methods or when method signatures are
 * reflective of a class hierarchy.
 * 
 * @author Wayne Milsted
 * @version $Id: Parametric.java,v 1.1 2001/10/18 01:32:15 simon Exp $
 */
public class Parametric {
	/**
	 * Constructor for Parametric.
	 * 
	 * @param aClassToReflect
	 *            Class<?>
	 */
	public Parametric(Class<?> aClassToReflect) {
		super();

		m_class = aClassToReflect;
	}

	/**
	 * Method getClassToReflect.
	 * 
	 * @return Class<?>
	 */
	public Class<?> getClassToReflect() {
		return m_class;
	}

	/**
	 * Method findMethod.
	 * 
	 * @param methodName
	 *            String
	 * @param parameters
	 *            Class<?>[]
	 * @return Method
	 */
	public Method findMethod(String methodName, Class<?>[] parameters) {
		Method theReturn = null;
		Class<?> currentClass = m_class;

		while (true) {
			try {
				Method[] methods = currentClass.getDeclaredMethods();

				for (Method method : methods) {
					int modifiers = method.getModifiers();

					if (method.getName().equals(methodName)
							&& Modifier.isPublic(modifiers)) {
						if (isTargetSignature(method, parameters)) {
							theReturn = method;

							break;
						}
					}
				} // end for loop
			} // end try block
			catch (Throwable t) {
				break;
			}

			currentClass = currentClass.getSuperclass();

			if (null == currentClass) {
				break; // we've reached beyond Object
			}
		} // end while loop

		return theReturn;
	}

	/**
	 * Method findField.
	 * 
	 * @param fieldName
	 *            String
	 * @return Field
	 */
	public Field findField(String fieldName) {
		Field theReturn = null;
		Class<?> currentClass = m_class;

		while (true) {
			try {
				Field[] fields = currentClass.getDeclaredFields();

				for (Field field : fields) {
					int modifiers = field.getModifiers();

					if (field.getName().equals(fieldName)
							&& Modifier.isPublic(modifiers)) {
						theReturn = field;

						break;
					}
				} // end for loop
			} // end try block
			catch (Throwable t) {
				break;
			}

			currentClass = currentClass.getSuperclass();

			if (null == currentClass) {
				break; // we've reached beyond Object
			}
		} // end while loop

		return theReturn;
	}

	/**
	 * Method isTargetSignature.
	 * 
	 * @param aMethod
	 *            Method
	 * @param parameters
	 *            Class<?>[]
	 * @return boolean
	 */
	private boolean isTargetSignature(Method aMethod, Class<?>[] parameters) {
		boolean theReturn = false;
		Class<?>[] thisMethodsParameters = aMethod.getParameterTypes();

		// no need to check further if the number of parameters
		// are unequal
		if (thisMethodsParameters.length == parameters.length) {
			for (int i = 0; i < parameters.length; i++) {
				Class<?> thisParm = thisMethodsParameters[i];
				Class<?> target = parameters[i];

				theReturn = thisParm.equals(target)
						|| thisParm.isAssignableFrom(target);

				if (!theReturn) {
					break;
				}
			}
		}

		return theReturn;
	}

	private Class<?> m_class = null;
}
