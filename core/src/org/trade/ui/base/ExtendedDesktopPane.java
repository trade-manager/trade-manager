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
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * 
 * @version $Id: ExtendedDesktopPane.java,v 1.1 2001/10/18 01:32:16 simon Exp $
 * @author Simon Allen
 */
public class ExtendedDesktopPane extends JDesktopPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4349765706181132905L;

	/**
	 * Method addCascaded.
	 * @param comp Component
	 * @param layer Integer
	 */
	public void addCascaded(Component comp, Integer layer) {
		// First add the component in the correct layer
		this.add(comp, layer);

		// Now do the cascading
		if (comp instanceof JInternalFrame) {
			this.cascade(comp);
		}

		// Move it to the front
		this.moveToFront(comp);
	}

	public void cascadeAll() {
		Component[] comps = getComponents();
		int count = comps.length;

		nextX = 0;
		nextY = 0;

		for (int i = count - 1; i >= 0; i--) {
			Component comp = comps[i];

			if ((comp instanceof JInternalFrame) && comp.isVisible()) {
				cascade(comp);
			}
		}
	}

	public void tileAll() {
		DesktopManager manager = getDesktopManager();

		if (manager == null) {
			// No desktop manager - do nothing
			return;
		}

		Component[] comps = getComponents();
		Component comp;
		int count = 0;

		// Count and handle only the internal frames
		for (Component comp2 : comps) {
			comp = comp2;

			if ((comp instanceof JInternalFrame) && comp.isVisible()) {
				count++;
			}
		}

		if (count != 0) {
			double root = Math.sqrt(count);
			int rows = (int) root;
			int columns = count / rows;
			int spares = count - (columns * rows);
			Dimension paneSize = getSize();
			int columnWidth = paneSize.width / columns;
			// We leave some space at the bottom that doesn't get covered
			int availableHeight = paneSize.height - UNUSED_HEIGHT;
			int mainHeight = availableHeight / rows;
			int smallerHeight = availableHeight / (rows + 1);
			int rowHeight = mainHeight;
			int x = 0;
			int y = 0;
			int thisRow = rows;
			int normalColumns = columns - spares;

			for (int i = comps.length - 1; i >= 0; i--) {
				comp = comps[i];

				if ((comp instanceof JInternalFrame) && comp.isVisible()) {
					if (((JInternalFrame) comp).isResizable()) {
						manager.setBoundsForFrame((JComponent) comp, x, y,
								columnWidth, rowHeight);
					} else {
						manager.setBoundsForFrame((JComponent) comp, x, y,
								comp.getSize().width, comp.getSize().height);
					}

					y += rowHeight;

					if (--thisRow == 0) {
						// Filled the row
						y = 0;
						x += columnWidth;

						// Switch to smaller rows if necessary
						if (--normalColumns <= 0) {
							thisRow = rows + 1;
							rowHeight = smallerHeight;
						} else {
							thisRow = rows;
						}
					}
				}
			}
		}
	}

	/**
	 * Method setCascadeOffsets.
	 * @param offsetX int
	 * @param offsetY int
	 */
	public void setCascadeOffsets(int offsetX, int offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	/**
	 * Method setCascadeOffsets.
	 * @param pt Point
	 */
	public void setCascadeOffsets(Point pt) {
		this.offsetX = pt.x;
		this.offsetY = pt.y;
	}

	/**
	 * Method getCascadeOffsets.
	 * @return Point
	 */
	public Point getCascadeOffsets() {
		return new Point(offsetX, offsetY);
	}

	/**
	 * Method cascade.
	 * @param comp Component
	 */
	protected void cascade(Component comp) {
		Dimension paneSize = getSize();
		int targetWidth = (3 * paneSize.width) / 4;
		int targetHeight = (3 * paneSize.height) / 4;
		DesktopManager manager = getDesktopManager();

		if (manager == null) {
			comp.setBounds(0, 0, targetWidth, targetHeight);

			return;
		}

		if (((nextX + targetWidth) > paneSize.width)
				|| ((nextY + targetHeight) > paneSize.height)) {
			nextX = 0;
			nextY = 0;
		}

		if (((JInternalFrame) comp).isResizable()) {
			manager.setBoundsForFrame((JComponent) comp, nextX, nextY,
					targetWidth, targetHeight);
		} else {
			manager.setBoundsForFrame((JComponent) comp, nextX, nextY,
					comp.getSize().width, comp.getSize().height);
		}

		// manager.setBoundsForFrame((JComponent)comp, nextX, nextY,
		// targetWidth, targetHeight);
		nextX += offsetX;
		nextY += offsetY;
	}

	protected int nextX; // Next X position

	protected int nextY; // Next Y position

	protected int offsetX = DEFAULT_OFFSETX;

	protected int offsetY = DEFAULT_OFFSETY;

	protected static final int DEFAULT_OFFSETX = 24;

	protected static final int DEFAULT_OFFSETY = 24;

	protected static final int UNUSED_HEIGHT = 48;
}
