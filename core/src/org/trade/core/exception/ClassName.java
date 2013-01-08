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
package org.trade.core.exception;

public class ClassName {

	/**
	 * This method exists strictly to test this class
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out
					.println("Usage = java com.aceva.devtool.ClassName <full class name>");
			System.exit(1);
		} else {
			try {
				Class<?> aClass = Class.forName(args[0]);
				ClassName className = new ClassName(aClass);
				System.out.println("Entire Package Name = "
						+ className.getEntirePackageName());
				System.out.println("PackageName = "
						+ className.getPackageName());
				System.out.println("Entire Class Name = "
						+ className.getEntireClassName());
				System.out.println("Class Name = " + className.getClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String m_entireClassName = null;

	private String m_className = null;

	private String m_packageName = null;

	private String m_entirePackageName = null;

	/**
	 * Constructor for ClassName.
	 * 
	 * @param aClass
	 *            Class<?>
	 */
	public ClassName(Class<?> aClass) {
		super();

		_parseClassName(aClass.getName());
	}

	/**
	 * Method _extractClassName.
	 * 
	 * @param className
	 *            String
	 * @return String
	 */
	private String _extractClassName(String className) {
		String name = null;

		int index = className.lastIndexOf('.');

		if (index != -1) {
			name = className.substring(index + 1, className.length());
		} else {
			name = className;
		}

		return name;
	}

	/**
	 * Method _extractEntireClassName.
	 * 
	 * @param className
	 *            String
	 * @return String
	 */
	private String _extractEntireClassName(String className) {
		return className;
	}

	/**
	 * Method _extractEntirePackageName.
	 * 
	 * @param className
	 *            String
	 * @return String
	 */
	private String _extractEntirePackageName(String className) {
		String packageName = null;
		int index = className.lastIndexOf('.');

		if (index != -1) {
			packageName = className.substring(0, index);
		}
		return packageName;
	}

	/**
	 * Method _extractPackageName.
	 * 
	 * @param className
	 *            String
	 * @return String
	 */
	private String _extractPackageName(String className) {
		String packageName = null;
		int index = className.lastIndexOf('.');

		if (index != -1) {
			packageName = className.substring(0, index);
			index = packageName.lastIndexOf('.');

			packageName = packageName.substring(++index);
		}

		return packageName;
	}

	/**
	 * Method _parseClassName.
	 * 
	 * @param className
	 *            String
	 */
	private void _parseClassName(String className) {
		m_entirePackageName = _extractEntirePackageName(className);
		m_packageName = _extractPackageName(className);
		m_entireClassName = _extractEntireClassName(className);
		m_className = _extractClassName(className);
	}

	/**
	 * Returns the short class name
	 * 
	 * @return String
	 */
	public String getClassName() {
		return m_className;
	}

	/**
	 * Returns the entire class name
	 * 
	 * @return String
	 */

	public String getEntireClassName() {
		return m_entireClassName;
	}

	/**
	 * Returns the entire package name
	 * 
	 * @return String
	 */
	public String getEntirePackageName() {
		return m_entirePackageName;
	}

	/**
	 * Returns the short package name
	 * 
	 * @return String
	 */
	public String getPackageName() {
		return m_packageName;
	}

}
