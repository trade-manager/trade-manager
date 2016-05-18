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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterGraphics;

/**
 * A simple Pageable class that can split a large drawing canvas over multiple
 * pages.
 * 
 * The pages in a canvas are laid out on pages going left to right and then top
 * to bottom.
 * 
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class PrintPage implements Pageable {
	private int m_NumPagesX;

	private int m_NumPagesY;

	private int m_NumPages;

	private Printable m_Painter = null;

	private PageFormat m_Format = null;

	/**
	 * Create a java.awt.Pageable that will print a canvas over as many pages as
	 * are needed. A Vista can be passed to PrinterJob.setPageable.
	 * 
	 * @param width
	 *            The width, in 1/72nds of an inch, of the vist's canvas.
	 * 
	 * @param height
	 *            The height, in 1/72nds of an inch, of the vista's canvas.
	 * 
	 * @param painter
	 *            The object that will drawn the contents of the canvas.
	 * 
	 * @param format
	 *            The description of the pages on to which the canvas will be
	 *            drawn.
	 */
	public PrintPage(float width, float height, Printable painter, PageFormat format) {
		setPrintable(painter);
		setPageFormat(format);
		setSize(width, height);
	}

	/**
	 * Create a vista over a canvas whose width and height are zero and whose
	 * Printable and PageFormat are null.
	 */
	protected PrintPage() {
	}

	/**
	 * Set the object responsible for drawing the canvas.
	 * 
	 * @param painter
	 *            Printable
	 */
	protected void setPrintable(Printable painter) {
		m_Painter = painter;
	}

	/**
	 * Set the page format for the pages over which the canvas will be drawn.
	 * 
	 * @param pageFormat
	 *            PageFormat
	 */
	protected void setPageFormat(PageFormat pageFormat) {
		m_Format = pageFormat;
	}

	/**
	 * Set the size of the canvas to be drawn.
	 * 
	 * @param width
	 *            The width, in 1/72nds of an inch, of the vist's canvas.
	 * 
	 * @param height
	 *            The height, in 1/72nds of an inch, of the vista's canvas.
	 */
	protected void setSize(float width, float height) {
		m_NumPagesX = (int) (((width + m_Format.getImageableWidth()) - 1) / m_Format.getImageableWidth());
		m_NumPagesY = (int) (((height + m_Format.getImageableHeight()) - 1) / m_Format.getImageableHeight());
		m_NumPages = m_NumPagesX * m_NumPagesY;
	}

	/**
	 * Returns the number of pages over which the canvas will be drawn.
	 * 
	 * @return int
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */
	public int getNumberOfPages() {
		return m_NumPages;
	}

	/**
	 * Method getPageFormat.
	 * 
	 * @return PageFormat
	 */
	protected PageFormat getPageFormat() {
		return m_Format;
	}

	/**
	 * Returns the PageFormat of the page specified by pageIndex. For a Vista
	 * the PageFormat is the same for all pages.
	 * 
	 * @param pageIndex
	 *            the zero based index of the page whose PageFormat is being
	 *            requested
	 * 
	 * 
	 * @return the PageFormat describing the size and orientation. * @exception
	 *         IndexOutOfBoundsException the Pageable does not contain the
	 *         requested page. * @see java.awt.print.Pageable#getPageFormat(int)
	 */
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		if (pageIndex >= m_NumPages) {
			throw new IndexOutOfBoundsException();
		}

		return getPageFormat();
	}

	/**
	 * Returns the <code>Printable</code> instance responsible for rendering the
	 * page specified by <code>pageIndex</code>. In a Vista, all of the pages
	 * are drawn with the same Printable. This method however creates a
	 * Printable which calls the canvas's Printable. This new Printable is
	 * responsible for translating the coordinate system so that the desired
	 * part of the canvas hits the page.
	 * 
	 * The Vista's pages cover the canvas by going left to right and then top to
	 * bottom. In order to change this behavior, override this method.
	 * 
	 * @param pageIndex
	 *            the zero based index of the page whose Printable is being
	 *            requested
	 * 
	 * 
	 * @return the Printable that renders the page. * @exception
	 *         IndexOutOfBoundsException the Pageable does not contain the
	 *         requested page. * @see java.awt.print.Pageable#getPrintable(int)
	 */
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		if (pageIndex >= m_NumPages) {
			throw new IndexOutOfBoundsException();
		}

		double originX = (pageIndex % m_NumPagesX) * m_Format.getImageableWidth();
		double originY = (pageIndex / (double) m_NumPagesX) * m_Format.getImageableHeight();
		Point2D.Double origin = new Point2D.Double(originX, originY);

		return new TranslatedPrintable(m_Painter, origin);
	}

	/**
	 * This inner class's sole responsibility is to translate the coordinate
	 * system before invoking a canvas's painter. The coordinate system is
	 * translated in order to get the desired portion of a canvas to line up
	 * with the top of a page.
	 * 
	 * @author Simon Allen
	 * @version $Revision: 1.0 $
	 */
	public static final class TranslatedPrintable implements Printable {
		/**
		 * The object that will draw the canvas.
		 */
		private Printable m_Painter;

		/**
		 * The upper-left corner of the part of the canvas that will be
		 * displayed on this page. This corner is lined up with the upper-left
		 * of the imageable area of the page.
		 */
		private Point2D m_Origin;

		/**
		 * Create a new Printable that will translate the drawing done by
		 * painter on to the imageable area of a page.
		 * 
		 * @param painter
		 *            The object responsible for drawing the canvas
		 * 
		 * @param origin
		 *            The point in the canvas that will be mapped to the
		 *            upper-left corner of the page's imageable area.
		 */
		public TranslatedPrintable(Printable painter, Point2D origin) {
			m_Painter = painter;
			m_Origin = origin;
		}

		/**
		 * Prints the page at the specified index into the specified
		 * {@link Graphics} context in the specified format. A PrinterJob calls
		 * the Printableinterface to request that a page be rendered into the
		 * context specified by graphics. The format of the page to be drawn is
		 * specified by pageFormat. The zero based index If the requested page
		 * does not exist then this method returns NO_SUCH_PAGE; otherwise
		 * PAGE_EXISTS is returned. The Graphics class or subclass implements
		 * the {@link PrinterGraphics} interface to provide additional
		 * information. If the Printable object aborts the print job then it
		 * throws a {@link PrinterException}.
		 * 
		 * @param graphics
		 *            the context into which the page is drawn
		 * @param pageFormat
		 *            the size and orientation of the page being drawn
		 * @param pageIndex
		 *            the zero based index of the page to be drawn
		 * 
		 * 
		 * @return PAGE_EXISTS if the page is rendered successfully or
		 *         NO_SUCH_PAGE if pageIndex specifies a non-existent page.
		 *         * @throws PrinterException
		 * @exception java.awt.print.PrinterException
		 *                thrown when the print job is terminated. * @see
		 *                java.awt.print.Printable#print(Graphics, PageFormat,
		 *                int)
		 */
		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
			Graphics2D g2 = (Graphics2D) graphics;
			g2.translate(-m_Origin.getX(), -m_Origin.getY());
			return m_Painter.print(g2, pageFormat, pageIndex);
		}
	}
}
