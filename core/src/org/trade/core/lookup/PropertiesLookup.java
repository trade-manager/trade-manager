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
package org.trade.core.lookup;

import java.util.Vector;

/**
 * Implementation of the Lookup interface that uses data from the
 * ConfigProperties object for providing its Lookup information.
 * 
 * @version $Id: ConfigPropertiesLookup.java,v 1.1 2001/11/06 16:52:41 simon Exp
 *          $
 * @author Simon Allen
 */
public class PropertiesLookup implements Lookup, Cloneable,
		java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5263608853348477640L;

	//
	// Private Attributes
	//
	private Vector<?> m_data = null;

	private Vector<?> m_columnNames = null;

	private int m_currentRowPos = -1;

	/**
	 * Constructor
	 * 
	 * 
	 * 
	 * @param columnNames
	 *            Vector<?>
	 * @param data
	 *            Vector<?>
	 */
	public PropertiesLookup(Vector<?> columnNames, Vector<?> data) {
		m_columnNames = columnNames;
		m_data = data;

		// A precaustion to make sure that calls to my API won't throw
		// nulls
		if (null == m_columnNames) {
			m_columnNames = new Vector<Object>();
		}

		if (null == m_data) {
			m_data = new Vector<Object>();
		}
	}

	/**
	 * Method getColumnCount.
	 * 
	 * @return int
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#getColumnCount()
	 */
	public int getColumnCount() throws LookupException {
		return (m_columnNames.size());
	}

	/**
	 * Method getRowCount.
	 * 
	 * @return int
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#getRowCount()
	 */
	public int getRowCount() throws LookupException {
		return (m_data.size());
	}

	/**
	 * Method getValueAt.
	 * 
	 * @param col
	 *            int
	 * @return Object
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#getValueAt(int)
	 */
	public Object getValueAt(int col) throws LookupException {
		return (doGetValue(m_currentRowPos, col));
	}

	/**
	 * Method getValueAt.
	 * 
	 * @param colName
	 *            String
	 * @return Object
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#getValueAt(String)
	 */
	public Object getValueAt(String colName) throws LookupException {
		return (doGetValue(m_currentRowPos, doGetColPos(colName)));
	}

	/**
	 * Method getValueAt.
	 * 
	 * @param row
	 *            int
	 * @param col
	 *            int
	 * @return Object
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) throws LookupException {
		return (doGetValue(row, col));
	}

	/**
	 * Method getColumnName.
	 * 
	 * @param colPos
	 *            int
	 * @return String
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#getColumnName(int)
	 */
	public String getColumnName(int colPos) throws LookupException {
		String colName = null;

		try {
			colName = "" + m_columnNames.elementAt(colPos);
		} catch (Throwable t) {
			throw new LookupException(t, "Not a valid column position");
		}

		return (colName);
	}

	/**
	 * Method setDefaultPos.
	 * 
	 * @param colName
	 *            String
	 * @return boolean
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#setDefaultPos(String)
	 */
	public boolean setDefaultPos(String colName) throws LookupException {
		return (doSetPos(doGetValue(0, doGetColPos(colName)),
				doGetColPos(colName)));
	}

	/**
	 * Method setPos.
	 * 
	 * @param colValue
	 *            Object
	 * @param colName
	 *            String
	 * @return boolean
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#setPos(Object, String)
	 */
	public boolean setPos(Object colValue, String colName)
			throws LookupException {
		return (doSetPos(colValue, doGetColPos(colName)));
	}

	/**
	 * Method setPos.
	 * 
	 * @param colValue
	 *            Object
	 * @param col
	 *            int
	 * @return boolean
	 * @throws LookupException
	 * @see org.trade.core.lookup.Lookup#setPos(Object, int)
	 */
	public boolean setPos(Object colValue, int col) throws LookupException {
		return (doSetPos(colValue, col));
	}

	/**
	 * Method clone.
	 * 
	 * @return Object
	 * @see org.trade.core.lookup.Lookup#clone()
	 */
	public Object clone() {
		return (new PropertiesLookup(m_columnNames, m_data));
	}

	//
	// Private Methods
	//
	/**
	 * Method doGetColPos.
	 * 
	 * @param colName
	 *            String
	 * @return int
	 * @throws LookupException
	 */
	private int doGetColPos(String colName) throws LookupException {
		int pos = -1;
		int columnNamesSize = m_columnNames.size();

		for (int i = 0; i < columnNamesSize; i++) {
			if (m_columnNames.elementAt(i).equals(colName)) {
				// Have found the position
				pos = i;
				break;
			}
		}

		if (-1 == pos) {
			throw new LookupException("Invalid Column Name");
		}

		return (pos);
	}

	/**
	 * Method doGetValue.
	 * 
	 * @param rowPos
	 *            int
	 * @param colPos
	 *            int
	 * @return Object
	 * @throws LookupException
	 */
	private Object doGetValue(int rowPos, int colPos) throws LookupException {
		Object rVal = null;

		if (rowPos != -1) // i.e a setPos was not performed.
		{
			try {
				Vector<?> row = (Vector<?>) m_data.elementAt(rowPos);
				rVal = row.elementAt(colPos);
			} catch (Throwable t) {
				throw new LookupException(t, "Out of bounds");
			}
		}
		return (rVal);
	}

	/**
	 * Method doSetPos.
	 * 
	 * @param colValue
	 *            Object
	 * @param col
	 *            int
	 * @return boolean
	 */
	private boolean doSetPos(Object colValue, int col) {
		boolean rVal = false;

		m_currentRowPos = -1;

		int dataSize = m_data.size();

		for (int i = 0; i < dataSize; i++) {
			Vector<?> row = (Vector<?>) m_data.elementAt(i);

			if (row.elementAt(col).equals(colValue)) {
				m_currentRowPos = i;
				rVal = true;

				break;
			}
		}
		return (rVal);
	}
}
