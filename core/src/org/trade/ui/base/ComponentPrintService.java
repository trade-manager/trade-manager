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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.MessageFormat;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JComponent;
import javax.swing.JTable.PrintMode;
import javax.swing.SwingUtilities;

/**
 * 
 * @version $Id: JComponentVista.java,v 1.2 2001/10/22 18:55:37 simon Exp $
 * @author Simon Allen
 */
public class ComponentPrintService extends PrintPage implements Printable {

	private double m_ScaleX;

	private double m_ScaleY;

	/**
	 * The Swing component to print.
	 */
	private Component m_component = null;

	private Throwable printError;

	/**
	 * Create a Pageable that can print a Swing JComponent over multiple pages.
	 * 
	 * @param c
	 *            The swing JComponent to be printed.
	 * 
	 * @param format
	 *            The size of the pages over which the componenent will be
	 *            printed.
	 */
	public ComponentPrintService(Component c, PageFormat format) {
		setPageFormat(format);
		setPrintable(this);
		setComponent(c);

		/*
		 * Tell the Vista we subclassed the size of the canvas.
		 */

		Rectangle componentBounds = c.getBounds(null);

		setSize(componentBounds.width, componentBounds.height);
		setScale(1, 1);
	}

	/**
	 * Method setComponent.
	 * 
	 * @param c
	 *            Component
	 */
	protected void setComponent(Component c) {
		m_component = c;
	}

	/**
	 * Method setScale.
	 * 
	 * @param scaleX
	 *            double
	 * @param scaleY
	 *            double
	 */
	protected void setScale(double scaleX, double scaleY) {
		m_ScaleX = scaleX;
		m_ScaleY = scaleY;
	}

	public void scaleToFitX() {
		PageFormat format = getPageFormat();
		Rectangle componentBounds = m_component.getBounds(null);
		double scaleX = format.getImageableWidth() / componentBounds.width;
		double scaleY = scaleX;

		if (scaleX < 1) {
			setSize((float) format.getImageableWidth(),
					(float) (componentBounds.height * scaleY));
			setScale(scaleX, scaleY);
		}
	}

	public void scaleToFitY() {
		PageFormat format = getPageFormat();
		Rectangle componentBounds = m_component.getBounds(null);
		double scaleY = format.getImageableHeight() / componentBounds.height;
		double scaleX = scaleY;

		if (scaleY < 1) {
			setSize((float) (componentBounds.width * scaleX),
					(float) format.getImageableHeight());
			setScale(scaleX, scaleY);
		}
	}

	/**
	 * Method scaleToFit.
	 * 
	 * @param useSymmetricScaling
	 *            boolean
	 */
	public void scaleToFit(boolean useSymmetricScaling) {
		PageFormat format = getPageFormat();
		Rectangle componentBounds = m_component.getBounds(null);
		double scaleX = format.getImageableWidth() / componentBounds.width;
		double scaleY = format.getImageableHeight() / componentBounds.height;

		if ((scaleX < 1) || (scaleY < 1)) {
			if (useSymmetricScaling) {
				if (scaleX < scaleY) {
					scaleY = scaleX;
				} else {
					scaleX = scaleY;
				}
			}

			setSize((float) (componentBounds.width * scaleX),
					(float) (componentBounds.height * scaleY));
			setScale(scaleX, scaleY);
		}
	}

	/**
	 * Method print.
	 * 
	 * @param graphics
	 *            Graphics
	 * @param pageFormat
	 *            PageFormat
	 * @param pageIndex
	 *            int
	 * @return int
	 * @throws PrinterException
	 * @see java.awt.print.Printable#print(Graphics, PageFormat, int)
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		if (pageIndex > 0) {
			return NO_SUCH_PAGE;
		}
		Graphics2D g2 = (Graphics2D) graphics;

		g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

		Rectangle componentBounds = m_component.getBounds(null);

		g2.translate(-componentBounds.x, -componentBounds.y);
		g2.scale(m_ScaleX, m_ScaleY);

		boolean wasBuffered = disableDoubleBuffering(m_component);
		m_component.printAll(g2);
		restoreDoubleBuffering(m_component, wasBuffered);
		return PAGE_EXISTS;
	}

	/**
	 * Method print.
	 * 
	 * @return boolean
	 * @throws Exception
	 */
	public boolean print() throws Exception {
		boolean showDialogs = !GraphicsEnvironment.isHeadless();

		return print(PrintMode.FIT_WIDTH, null, null, showDialogs, null,
				showDialogs, null);
	}

	/**
	 * Method print.
	 * 
	 * @param printMode
	 *            PrintMode
	 * @param headerFormat
	 *            MessageFormat
	 * @param footerFormat
	 *            MessageFormat
	 * @param showPrintDialog
	 *            boolean
	 * @param attr
	 *            PrintRequestAttributeSet
	 * @param interactive
	 *            boolean
	 * @param service
	 *            PrintService
	 * @return boolean
	 * @throws Exception
	 */
	public boolean print(PrintMode printMode, MessageFormat headerFormat,
			MessageFormat footerFormat, boolean showPrintDialog,
			PrintRequestAttributeSet attr, boolean interactive,
			PrintService service) throws Exception {
		// complain early if an invalid parameter is specified for headless mode
		boolean isHeadless = GraphicsEnvironment.isHeadless();
		if (isHeadless) {
			if (showPrintDialog) {
				throw new HeadlessException("Can't show print dialog.");
			}

			if (interactive) {
				throw new HeadlessException("Can't run interactively.");
			}
		}

		// Get a PrinterJob.
		// Do this before anything with side-effects since it may throw a
		// security exception - in which case we don't want to do anything else.
		final PrinterJob job = PrinterJob.getPrinterJob();

		if (attr == null) {
			attr = new HashPrintRequestAttributeSet();
		}

		// fetch the Printable
		Printable printable = this;

		DocFlavor doc_flavor = DocFlavor.INPUT_STREAM.JPEG;
		PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
		PrintService[] services = PrintServiceLookup.lookupPrintServices(
				doc_flavor, attr_set);
		if (services.length > 0) {
			service = services[1];
		}

		if (interactive) {
			// wrap the Printable so that we can print on another thread
			printable = new ThreadSafePrintable(printable);
		}

		// set the printable on the PrinterJob
		job.setPrintable(printable);

		// if specified, set the PrintService on the PrinterJob
		if (service != null) {
			job.setPrintService(service);
		}

		// if requested, show the print dialog
		if (showPrintDialog && !job.printDialog(attr)) {
			// the user cancelled the print dialog
			return false;
		}

		// if not interactive, just print on this thread (no dialog)
		if (!interactive) {
			// do the printing
			job.print(attr);

			// we're done
			return true;
		}

		// make sure this is clear since we'll check it after
		printError = null;

		// to synchronize on
		final Object lock = new Object();

		// copied so we can access from the inner class
		final PrintRequestAttributeSet copyAttr = attr;

		// this runnable will be used to do the printing
		// (and save any throwables) on another thread
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					// do the printing
					job.print(copyAttr);
				} catch (Throwable t) {
					// save any Throwable to be rethrown
					synchronized (lock) {
						printError = t;
					}
				}
			}
		};

		// start printing on another thread
		Thread th = new Thread(runnable, "PrinterThread");
		th.start();

		// look for any error that the printing may have generated
		Throwable pe;
		synchronized (lock) {
			pe = printError;
			printError = null;
		}

		// check the type of error and handle it
		if (pe != null) {
			// a subclass of PrinterException meaning the job was aborted,
			// in this case, by the user
			if (pe instanceof PrinterAbortException) {
				return false;
			} else if (pe instanceof PrinterException) {
				throw (PrinterException) pe;
			} else if (pe instanceof Exception) {
				throw (Exception) pe;
			} else if (pe instanceof Error) {
				throw (Error) pe;
			}

			// can not happen
			throw new AssertionError(pe);
		}

		return true;

	}

	/**
	 * Method disableDoubleBuffering.
	 * 
	 * @param c
	 *            Component
	 * @return boolean
	 */
	private boolean disableDoubleBuffering(Component c) {
		if (!(c instanceof JComponent))
			return false;
		JComponent jc = (JComponent) c;
		boolean wasBuffered = jc.isDoubleBuffered();
		jc.setDoubleBuffered(false);
		return wasBuffered;
	}

	/**
	 * Method restoreDoubleBuffering.
	 * 
	 * @param c
	 *            Component
	 * @param wasBuffered
	 *            boolean
	 */
	private void restoreDoubleBuffering(Component c, boolean wasBuffered) {
		if (c instanceof JComponent)
			((JComponent) c).setDoubleBuffered(wasBuffered);
	}

	/**
	 */
	private class ThreadSafePrintable implements Printable {

		/** The delegate <code>Printable</code>. */
		private Printable printDelegate;

		/**
		 * To communicate any return value when delegating.
		 */
		private int retVal;

		/**
		 * To communicate any <code>Throwable</code> when delegating.
		 */
		private Throwable retThrowable;

		/**
		 * Construct a <code>ThreadSafePrintable</code> around the given
		 * delegate.
		 * 
		 * @param printDelegate
		 *            the <code>Printable</code> to delegate to
		 */
		public ThreadSafePrintable(Printable printDelegate) {
			this.printDelegate = printDelegate;
		}

		/**
		 * Prints the specified page into the given {@link Graphics} context, in
		 * the specified format.
		 * <p>
		 * Regardless of what thread this method is called on, all calls into
		 * the delegate will be done on the event-dispatch thread.
		 * 
		 * @param graphics
		 *            the context into which the page is drawn
		 * @param pageFormat
		 *            the size and orientation of the page being drawn
		 * @param pageIndex
		 *            the zero based index of the page to be drawn
		 * 
		 * 
		 * @return PAGE_EXISTS if the page is rendered successfully, or
		 *         NO_SUCH_PAGE if a non-existent page index is specified * @throws
		 *         PrinterException if an error causes printing to be aborted * @see
		 *         java.awt.print.Printable#print(Graphics, PageFormat, int)
		 */
		public int print(final Graphics graphics, final PageFormat pageFormat,
				final int pageIndex) throws PrinterException {

			// We'll use this Runnable
			Runnable runnable = new Runnable() {
				public synchronized void run() {
					try {
						// call into the delegate and save the return value
						retVal = printDelegate.print(graphics, pageFormat,
								pageIndex);
					} catch (Throwable throwable) {
						// save any Throwable to be rethrown
						retThrowable = throwable;
					} finally {
						// notify the caller that we're done
						notifyAll();
					}
				}
			};

			synchronized (runnable) {
				// make sure these are initialized
				retVal = -1;
				retThrowable = null;

				// call into the EDT
				SwingUtilities.invokeLater(runnable);

				// wait for the runnable to finish
				while (retVal == -1 && retThrowable == null) {
					try {
						runnable.wait();
					} catch (InterruptedException ie) {
						// short process, safe to ignore interrupts
					}
				}

				// if the delegate threw a throwable, rethrow it here
				if (retThrowable != null) {
					if (retThrowable instanceof PrinterException) {
						throw (PrinterException) retThrowable;
					} else if (retThrowable instanceof Exception) {
						throw (RuntimeException) retThrowable;
					} else if (retThrowable instanceof Error) {
						throw (Error) retThrowable;
					}

					// can not happen
					throw new AssertionError(retThrowable);
				}

				return retVal;
			}
		}
	}
}
