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

import java.util.Vector;

import javax.swing.event.EventListenerList;

/**
 * 
 * @version $Id: MessageNotifier.java,v 1.1 2001/10/18 01:32:15 simon Exp $
 * @author Simon Allen
 */
public class MessageNotifier {
	private EventListenerList listeners;

	/**
	 * MessageNotifier() - constructor
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @exception *
	 * 				@see
	 */
	public MessageNotifier() {
		this.listeners = new EventListenerList();
	}

	/**
	 * addMessageListener() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param listener
	 *            MessageListener
	 * @exception *
	 * 				@see
	 */
	public void add(MessageListener listener) {
		this.listeners.add(MessageListener.class, listener);
	}

	/**
	 * removeMessageListener() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param listener
	 *            MessageListener
	 * @exception *
	 * 				@see
	 */
	public void remove(MessageListener listener) {
		this.listeners.remove(MessageListener.class, listener);

	}

	/**
	 * removeMessageListener() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @exception *
	 * 				@see
	 */
	public void removeAll() {
		Object[] listenerList = this.listeners.getListenerList();
		for (int i = listenerList.length - 2; i >= 0; i -= 2) {
			if (listenerList[i] == MessageListener.class) {
				remove(((MessageListener) listenerList[i + 1]));
			}
		}
	}

	/**
	 * notifyEvent() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param e
	 *            MessageEvent
	 * @param parm
	 *            Vector<Object>
	 * @exception *
	 * 				@see
	 */
	public void notifyEvent(MessageEvent e, Vector<Object> parm) {
		Object[] listenerList = this.listeners.getListenerList();
		for (int i = listenerList.length - 2; i >= 0; i -= 2) {
			if (listenerList[i] == MessageListener.class) {
				((MessageListener) listenerList[i + 1]).handleEvent(e, parm);
			}
		}
	}
}
