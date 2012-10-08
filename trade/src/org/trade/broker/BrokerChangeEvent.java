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
package org.trade.broker;

/**
 * A change event that encapsulates information about a change to a dataset.
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class BrokerChangeEvent extends java.util.EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6068031665553697870L;
	/**
	 * The brokerManagerModel that generated the change event.
	 */
	private BrokerModel brokerManagerModel;

	/**
	 * Constructs a new event. The source is either the brokerManagerModel or
	 * the class. The brokerManagerModel can be <code>null</code> (in this case
	 * the source will be the class).
	 * 
	 * @param source
	 *            the source of the event.
	
	 * @param brokerManagerModel BrokerModel
	 */
	public BrokerChangeEvent(Object source,
			BrokerModel brokerManagerModel) {
		super(source);
		this.brokerManagerModel = brokerManagerModel;
	}

	/**
	 * Returns the brokerManagerModel that generated the event. Note that the brokerManagerModel may
	 * be <code>null</code> since adding a <code>null</code> brokerManagerModel to a plot
	 * will generated a change event.
	 * 
	
	 * @return The brokerManagerModel (possibly <code>null</code>). */
	public BrokerModel getBrokerManagerModel() {
		return this.brokerManagerModel;
	}

}
