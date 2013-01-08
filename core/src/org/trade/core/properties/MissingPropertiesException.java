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
package org.trade.core.properties;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Exception thrown by PropertyUtils class.
 * 
 * @author : Simon Allen
 */
public class MissingPropertiesException extends java.lang.Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5377864368236421685L;
	private Vector<String> m_missingProperties = null;

	public MissingPropertiesException() {
		super();
	}

	/**
	 * Method addProperty.
	 * 
	 * @param p
	 *            String
	 */
	public void addProperty(String p) {
		if (m_missingProperties == null) {
			m_missingProperties = new Vector<String>();
		}

		m_missingProperties.addElement(p);
	}

	/**
	 * Method getMessage.
	 * 
	 * @return String
	 */
	public String getMessage() {
		StringBuffer message = new StringBuffer(
				"The following properties are missing: ");
		Enumeration<String> missingProperties = getMissingProperties();

		if (null == missingProperties) {
			message.append("No properties missing!");
		} else {
			boolean first = true;

			while (missingProperties.hasMoreElements()) {
				if (first) {
					first = false;
				} else {
					message.append(", ");
				}

				message.append(missingProperties.nextElement());
			}
		}

		return message.toString();
	}

	/**
	 * Method getMissingProperties.
	 * 
	 * @return Enumeration<String>
	 */
	public Enumeration<String> getMissingProperties() {
		if (m_missingProperties == null) {
			m_missingProperties = new Vector<String>();
		}

		return m_missingProperties.elements();
	}

	/**
	 * Method toString.
	 * 
	 * @return String
	 */
	public String toString() {
		if (m_missingProperties == null) {
			return "No properties missing";
		}

		StringBuffer sb = new StringBuffer("The following [");

		sb.append(m_missingProperties.size());
		sb.append("] properties are missing: ");

		int missingPropSize = m_missingProperties.size();

		for (int ii = 0; ii < missingPropSize; ii++) {
			sb.append(m_missingProperties.elementAt(ii));
			sb.append(", ");
		}

		sb.append('.');

		return sb.toString();
	}
}
