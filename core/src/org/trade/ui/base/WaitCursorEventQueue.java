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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.MenuContainer;

import javax.swing.SwingUtilities;

/**
 */
public class WaitCursorEventQueue extends EventQueue {
	/**
	 * Constructor for WaitCursorEventQueue.
	 * 
	 * @param delay
	 *            int
	 */
	public WaitCursorEventQueue(int delay) {
		this.delay = delay;
		waitTimer = new WaitCursorTimer();
		waitTimer.setDaemon(true);
		waitTimer.start();
	}

	/**
	 * Method dispatchEvent.
	 * 
	 * @param event
	 *            AWTEvent
	 */
	protected void dispatchEvent(AWTEvent event) {
		waitTimer.startTimer(event.getSource());

		try {
			super.dispatchEvent(event);
		} finally {
			waitTimer.stopTimer();
		}
	}

	private int delay;

	private WaitCursorTimer waitTimer;

	/**
	 */
	private class WaitCursorTimer extends Thread {
		/**
		 * Method startTimer.
		 * 
		 * @param source
		 *            Object
		 */
		synchronized void startTimer(Object source) {
			this.source = source;

			notify();
		}

		synchronized void stopTimer() {
			if (parent == null) {
				interrupt();
			} else {
				parent.setCursor(null);

				parent = null;
			}
		}

		/**
		 * Method run.
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public synchronized void run() {
			while (true) {
				try { // wait for notification from startTimer()
					wait();
					// wait for event processing to reach the threshold, or
					// interruption from stopTimer()
					wait(delay);

					if (source instanceof Component) {
						parent = SwingUtilities.getRoot((Component) source);
					} else if (source instanceof MenuComponent) {
						MenuContainer mParent = ((MenuComponent) source)
								.getParent();

						if (mParent instanceof Component) {
							parent = SwingUtilities
									.getRoot((Component) mParent);
						}
					}

					if ((parent != null) && parent.isShowing()) {
						parent.setCursor(Cursor
								.getPredefinedCursor(Cursor.WAIT_CURSOR));
					}
				} catch (InterruptedException ie) {
				}
			}
		}

		private Object source;
		private Component parent;
	}
}
