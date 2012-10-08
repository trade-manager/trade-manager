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
package org.trade.core.util;

import javax.swing.SwingUtilities;

/**
 * The StrategyWorker is an abstract class that you subclass to perform work in
 * a dedicated thread. For instructions on using this class, see:
 * 
 * StrategyWorker does all the dirty work of implementing a background thread.
 * Although many programs don't need background threads, background threads are
 * sometimes useful for performing time-consuming operations, which can improve
 * the perceived performance of a program.
 * 
 * To use the StrategyWorker class, you first create a subclass of it. In the
 * subclass, you must implement the doInBackground() method so that it contains
 * the code to perform your lengthy operation. When you instantiate your
 * StrategyWorker subclass, the StrategyWorker creates a thread but does not
 * start it. You invoke start() on your StrategyWorker object to start the
 * thread, which then calls your doInBackground() method. When you need the
 * object returned by the doInBackground() method, you call the StrategyWorker
 * get() method. Here's an example of using SwingWorker:
 * 
 * ...//in the main method: final StrategyWorker worker = new StrategyWorker() {
 * public Object doInBackground() { return new expensiveDialogComponent(); } };
 * worker.execute();
 * 
 * Note that the API changed slightly in the 3rd version: You must now invoke
 * start() on the StrategyWorker after creating it.
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public abstract class Worker {
	private Object value; // see getValue(), setValue()
	public Thread thread;
	protected boolean isDone = false;
	protected boolean isCancelled = false;
	protected static int threadCount = 0;

	/**
	 * Class to maintain reference to current worker thread under separate
	 * synchronization control.
	 * @author Simon Allen
	 * @version $Revision: 1.0 $
	 */
	private static class ThreadVar {
		private Thread thread;

		/**
		 * Constructor for ThreadVar.
		 * @param t Thread
		 */
		ThreadVar(Thread t) {
			thread = t;
		}

		/**
		 * Method get.
		 * @return Thread
		 */
		synchronized Thread get() {
			return thread;
		}

		synchronized void clear() {
			thread = null;
		}
	}

	private ThreadVar threadVar;

	/**
	 * Get the value produced by the worker thread, or null if it hasn't been
	 * constructed yet.
	 * @return Object
	 */
	protected synchronized Object getValue() {
		return value;
	}

	/**
	 * Set the value produced by worker thread
	 * @param x Object
	 */
	private synchronized void setValue(Object x) {
		value = x;
	}

	/**
	 * Compute the value to be returned by the <code>get</code> method.
	 * @return Object
	 */
	protected abstract Object doInBackground();

	/**
	 * Called on the event dispatching thread (not on the worker thread) after
	 * the <code>doInBackground</code> method has returned.
	 */
	protected abstract void done();

	/**
	 * Method isDone.
	 * @return boolean
	 */
	public boolean isDone() {
		return isDone;
	}

	/**
	 * A thread state. A thread can be in one of the following states:
	 * 
	 * NEW A thread that has not yet started is in this state.
	 * 
	 * RUNNABLE A thread executing in the Java virtual machine is in this state.
	 * 
	 * BLOCKED A thread that is blocked waiting for a monitor lock is in this
	 * state.
	 * 
	 * WAITING A thread that is waiting indefinitely for another thread to
	 * perform a particular action is in this state.
	 * 
	 * TIMED_WAITING A thread that is waiting for another thread to perform an
	 * action for up to a specified waiting time is in this state.
	 * 
	 * TERMINATED A thread that has exited is in this state. A thread can be in
	 * only one state at a given point in time.
	 * 
	 * These states are virtual machine states which do not reflect any
	 * operating system thread states.
	 * 
	
	 * @return the boolean running/dead. */

	public boolean isRunning() {
		Thread t = threadVar.get();
		if (t != null) {
			return !(t.getState().compareTo(Thread.State.NEW) == 0 || t
					.getState().compareTo(Thread.State.TERMINATED) == 0);
		}
		return false;
	}

	/**
	 * A thread state. A thread can be in one of the following states:
	 * 
	 * NEW A thread that has not yet started is in this state.
	 * 
	 * RUNNABLE A thread executing in the Java virtual machine is in this state.
	 * 
	 * BLOCKED A thread that is blocked waiting for a monitor lock is in this
	 * state.
	 * 
	 * WAITING A thread that is waiting indefinitely for another thread to
	 * perform a particular action is in this state.
	 * 
	 * TIMED_WAITING A thread that is waiting for another thread to perform an
	 * action for up to a specified waiting time is in this state.
	 * 
	 * TERMINATED A thread that has exited is in this state. A thread can be in
	 * only one state at a given point in time.
	 * 
	 * These states are virtual machine states which do not reflect any
	 * operating system thread states.
	 * 
	
	 * @return the boolean running/dead. */

	public boolean isWaiting() {
		Thread t = threadVar.get();
		if (t != null) {
			return t.getState().compareTo(Thread.State.WAITING) == 0
					|| t.getState().compareTo(Thread.State.TIMED_WAITING) == 0;
		}
		return false;
	}

	/**
	 * A new method that interrupts the worker thread. Call this method to force
	 * the worker to stop what it's doing.
	 */
	public void cancel() {
		isCancelled = true;
		Thread t = threadVar.get();
		if (t != null) {
			t.interrupt();
		}
		threadVar.clear();
	}

	/**
	 * Method isCancelled.
	 * @return boolean
	 */
	public boolean isCancelled() {
		return isCancelled;
	}

	/**
	 * Method setIsCancelled.
	 * @param isCancelled boolean
	 */
	public void setIsCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * Return the value created by the <code>construct</code> method. Returns
	 * null if either the constructing thread or the current thread was
	 * interrupted before a value was produced.
	 * 
	
	 * @return the value created by the <code>construct</code> method */
	public Object get() {
		while (true) {
			Thread t = threadVar.get();
			if (t == null) {
				return getValue();
			}
			try {
				t.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	}

	/**
	 * Start a thread that will call the <code>doInBackground</code> method and
	 * then exit.
	 */
	public Worker() {
		final Runnable doFinished = new Runnable() {
			public void run() {
				isDone = true;
				done();
			}
		};

		Runnable doConstruct = new Runnable() {
			public void run() {
				try {
					setValue(doInBackground());
				} finally {
					threadVar.clear();
				}
				SwingUtilities.invokeLater(doFinished);
			}
		};
		Thread t = new Thread(doConstruct, "WorkerThread" + threadCount++);
		threadVar = new ThreadVar(t);
	}

	/**
	 * Start the worker thread.
	 */
	public void execute() {
		Thread t = threadVar.get();
		if (t != null) {
			isDone = false;
			isCancelled = false;
			t.start();
		} else {

		}
	}
}
