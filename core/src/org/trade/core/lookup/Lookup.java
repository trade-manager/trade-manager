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
package org.trade.core.lookup;

/**
 * Interface describing a Lookup - essentially it represents a single/multi
 * comlumn table/matrix that can be accessed by column id/name and row.
 * 
 * Note : This API has borrowed heavily from the javax.swing.table.TableModel
 * interface so that it is easier to hook it up to a GUI.
 * 
 * @version $Id: Lookup.java,v 1.1 2001/11/06 16:52:41 simon Exp $
 * @author Simon Allen
 */
public interface Lookup {
	/**
	 * The number of columns that exist in the Lookup.
	 * 
	
	 * 
	
	 * @return Number of columns in this Lookup. * @exception LookupException */
	int getColumnCount() throws LookupException;

	/**
	 * The number of rows in the Lookup.
	 * 
	
	 * 
	
	 * @return Number of rows in the Lookup. * @exception LookupException */
	int getRowCount() throws LookupException;

	/**
	 * Retrieves the object from the current row and indicated column position.
	 * 
	
	 * 
	
	 * 
	
	 * 
	 * @param colPos int
	 * @return Returns the Object from the current row and column position. * @exception LookupException
	 *                Thrown if the column position or row position is out of
	 *                range. */
	Object getValueAt(int colPos) throws LookupException;

	/**
	 * Retrieves the object from the current row and indicated column name.
	 * 
	
	 * 
	
	 * 
	
	 * @param colName String
	 * @return Returns the Object from the current row and named column. * @exception LookupException
	 *                Thrown if the column name does not exist or the row
	 *                position is out of range. */
	Object getValueAt(String colName) throws LookupException;

	/**
	 * Retrieves the object from the indicated row and col.
	 * 
	
	
	 * 
	
	 * 
	
	 * @param row int
	 * @param col int
	 * @return Returns the Object from the row and column. * @exception LookupException
	 *                Thrown if the column name does not exist or the row
	 *                position is out of range. */
	Object getValueAt(int row, int col) throws LookupException;

	/**
	 * Retrieves the column name from the indicated column position.
	 * 
	
	 * 
	
	 * 
	
	 * @param colPos int
	 * @return Returns the name of the column position. * @exception LookupException
	 *                Thrown if the column position is out of range. */
	String getColumnName(int colPos) throws LookupException;

	/**
	 * The current row position is set to the row with the corresponding value
	 * in the named column. If the value is not found a default position will no
	 * longer be set in the object - in which case a null will be returned from
	 * the single parameter getValue() methods.
	 * 
	
	
	 * 
	
	 * 
	
	 * @param colValue Object
	 * @param colName String
	 * @return True if the value is found in the column name. * @exception LookupException
	 *                Thrown if the column name does not exist. */
	boolean setPos(Object colValue, String colName) throws LookupException;

	/**
	 * The current row position is set to the row with the corresponding value
	 * in the named column. If the value is not found a default position will no
	 * longer be set in the object - in which case a null will be returned from
	 * the single parameter getValue() methods.
	 * 
	
	 * 
	
	 * 
	
	 * @param colName String
	 * @return True if the value is found in the column name. * @exception LookupException
	 *                Thrown if the column name does not exist. */
	boolean setDefaultPos(String colName) throws LookupException;

	/**
	 * The current row position is set to the row with the corresponding value
	 * in the named column. If the value is not found a default position will no
	 * longer be set in the object - in which case a null will be returned from
	 * the single parameter getValue() methods.
	 * 
	
	
	 * 
	
	 * 
	
	 * @param colValue Object
	 * @param colPos int
	 * @return True if the value is found in the column position. * @exception LookupException
	 *                Thrown if the column position is out of range. */
	boolean setPos(Object colValue, int colPos) throws LookupException;

	/**
	
	 * @return A copy of this object. */
	Object clone();
}
