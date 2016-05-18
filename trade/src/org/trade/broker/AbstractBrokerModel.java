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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.event.EventListenerList;

import org.trade.persistent.dao.TradePosition;
import org.trade.persistent.dao.TradeOrder;
import org.trade.persistent.dao.Tradestrategy;

/**
 * An abstract implementation of the {@link BrokerModel} interface, containing a
 * mechanism for registering change listeners.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public abstract class AbstractBrokerModel implements BrokerModel, Cloneable, Serializable, ObjectInputValidation {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3286930063989668002L;
	/** Storage for registered change listeners. */
	private transient EventListenerList listenerList;
	private boolean brokerDataOnly = false;

	/**
	 * Constructs a broker.
	 */
	protected AbstractBrokerModel() {
		this.listenerList = new EventListenerList();
	}

	/**
	 * Registers an object to receive notification of changes to the
	 * brokerManagerModel.
	 * 
	 * @param listener
	 *            the object to register.
	 * 
	 * 
	 * @see #removeChangeListener(BrokerChangeListener)
	 */
	public void addMessageListener(BrokerChangeListener listener) {
		this.listenerList.add(BrokerChangeListener.class, listener);
	}

	/**
	 * Deregisters an object so that it no longer receives notification of
	 * changes to the brokerManagerModel.
	 * 
	 * @param listener
	 *            the object to deregister.
	 * 
	 * 
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	public void removeMessageListener(BrokerChangeListener listener) {
		this.listenerList.remove(BrokerChangeListener.class, listener);
	}

	/**
	 * Returns <code>true</code> if the specified object is registered with the
	 * brokerManagerModel as a listener. Most applications won't need to call
	 * this method, it exists mainly for use by unit testing code.
	 * 
	 * @param listener
	 *            the listener.
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return A boolean. * @see #addChangeListener(BrokerChangeListener) * @see
	 *         #removeChangeListener(BrokerChangeListener)
	 */
	public boolean hasListener(EventListener listener) {
		List<Object> list = Arrays.asList(this.listenerList.getListenerList());
		return list.contains(listener);
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has opened
	 * a connection.
	 * 
	 * 
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireConnectionOpened() {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).connectionOpened();
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has closed
	 * a connection.
	 * 
	 * 
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireConnectionClosed(boolean forced) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).connectionClosed(forced);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has
	 * received all the order executions.
	 * 
	 * 
	 * @param execDetails
	 *            ConcurrentHashMap<Integer,TradeOrder>
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireExecutionDetailsEnd(ConcurrentHashMap<Integer, TradeOrder> execDetails) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).executionDetailsEnd(execDetails);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has
	 * complete the historical data request for this tradestrategy.
	 * 
	 * 
	 * @param tradestrategy
	 *            Tradestrategy
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireHistoricalDataComplete(Tradestrategy tradestrategy) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).historicalDataComplete(tradestrategy);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel received an
	 * update to managed accounts.
	 * 
	 * 
	 * @param accountNumber
	 *            String
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireManagedAccountsUpdated(String accountNumber) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).managedAccountsUpdated(accountNumber);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has
	 * received all FA Accounts information.
	 * 
	 * 
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireFAAccountsCompleted() {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).fAAccountsCompleted();
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel received an
	 * update to an account.
	 * 
	 * 
	 * @param accountNumber
	 *            String
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireUpdateAccountTime(String accountNumber) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).updateAccountTime(accountNumber);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel received an
	 * error.
	 * 
	 * 
	 * @param brokerError
	 *            BrokerModelException
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireBrokerError(BrokerModelException brokerError) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).brokerError(brokerError);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has opened
	 * a trade position.
	 * 
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireTradeOrderFilled(TradeOrder tradeOrder) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).tradeOrderFilled(tradeOrder);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has opened
	 * a trade position.
	 * 
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireTradeOrderStatusChanged(TradeOrder tradeOrder) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).tradeOrderStatusChanged(tradeOrder);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has closed
	 * a trade position.
	 * 
	 * 
	 * @param tradePosition
	 *            TradePosition
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void firePositionClosed(TradePosition tradePosition) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).positionClosed(tradePosition);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has
	 * received a cancelled order
	 * 
	 * 
	 * @param tradeOrder
	 *            TradeOrder
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireTradeOrderCancelled(TradeOrder tradeOrder) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).tradeOrderCancelled(tradeOrder);
			}
		}
	}

	/**
	 * Notifies all registered listeners that the brokerManagerModel has
	 * received all the open orders.
	 * 
	 * 
	 * @param openOrders
	 *            ConcurrentHashMap<Integer,TradeOrder>
	 * @see #addChangeListener(BrokerChangeListener)
	 */
	protected void fireOpenOrderEnd(ConcurrentHashMap<Integer, TradeOrder> openOrders) {
		Object[] listeners = this.listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == BrokerChangeListener.class) {
				((BrokerChangeListener) listeners[i + 1]).openOrderEnd(openOrders);
			}
		}
	}

	/**
	 * Method isBrokerDataOnly.
	 * 
	 * @return boolean
	 * @see org.trade.broker.BrokerModel#isBrokerDataOnly()
	 */
	public boolean isBrokerDataOnly() {
		return this.brokerDataOnly;
	}

	/**
	 * Method setBrokerDataOnly.
	 * 
	 * @param brokerDataOnly
	 *            boolean
	 * @see org.trade.broker.BrokerModel#setBrokerDataOnly(boolean)
	 */
	public void setBrokerDataOnly(boolean brokerDataOnly) {
		this.brokerDataOnly = brokerDataOnly;
	}

	/**
	 * Returns a clone of the brokerManagerModel. The cloned brokerManagerModel
	 * will NOT include the {@link BrokerChangeListener} references that have
	 * been registered with this brokerManagerModel.
	 * 
	 * 
	 * 
	 * 
	 * @return A clone. * @throws CloneNotSupportedException if the dataset does
	 *         not support cloning.
	 */
	public Object clone() throws CloneNotSupportedException {
		AbstractBrokerModel clone = (AbstractBrokerModel) super.clone();
		clone.listenerList = new EventListenerList();
		return clone;
	}

	/**
	 * Handles serialization.
	 * 
	 * @param stream
	 *            the output stream.
	 * 
	 * 
	 * @throws IOException
	 *             if there is an I/O problem.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
	}

	/**
	 * Restores a serialized object.
	 * 
	 * @param stream
	 *            the input stream.
	 * 
	 * 
	 * 
	 * @throws IOException
	 *             if there is an I/O problem. * @throws ClassNotFoundException
	 *             if there is a problem loading a class.
	 */
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		this.listenerList = new EventListenerList();
		stream.registerValidation(this, 10); // see comments about priority of
												// 10 in validateObject()
	}

	/**
	 * Validates the object. We use this opportunity to call listeners who have
	 * registered during the deserialization process, as listeners are not
	 * serialized. This method is called by the serialization system after the
	 * entire graph is read.
	 * 
	 * This object has registered itself to the system with a priority of 10.
	 * Other callbacks may register with a higher priority number to be called
	 * before this object, or with a lower priority number to be called after
	 * the listeners were notified.
	 * 
	 * All listeners are supposed to have register by now, either in their
	 * readObject or validateObject methods. Notify them that this
	 * brokerManagerModel has changed.
	 * 
	 * 
	 * @exception InvalidObjectException
	 *                If the object cannot validate itself. * @see
	 *                java.io.ObjectInputValidation#validateObject()
	 */
	public void validateObject() throws InvalidObjectException {
		fireConnectionOpened();
	}
}
