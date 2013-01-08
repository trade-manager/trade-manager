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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * 
 * @version $Id: FilePreviewer.java,v 1.1 2001/10/18 01:32:16 simon Exp $
 * @author Simon Allen
 */
public class FilePreviewer extends JComponent implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2163573903688220675L;

	ImageIcon thumbnail = null;

	File f = null;

	/**
	 * Constructor for FilePreviewer.
	 * 
	 * @param fc
	 *            JFileChooser
	 */
	public FilePreviewer(JFileChooser fc) {
		setPreferredSize(new Dimension(100, 50));
		fc.addPropertyChangeListener(this);
	}

	public void loadImage() {
		if (f != null) {
			ImageIcon tmpIcon = new ImageIcon(f.getPath());

			if (tmpIcon.getIconWidth() > 90) {
				thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(
						90, -1, Image.SCALE_DEFAULT));
			} else {
				thumbnail = tmpIcon;
			}
		}
	}

	/**
	 * Method propertyChange.
	 * 
	 * @param e
	 *            PropertyChangeEvent
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (prop.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
			f = (File) e.getNewValue();

			if (isShowing()) {
				loadImage();
				repaint();
			}
		}
	}

	/**
	 * Method paint.
	 * 
	 * @param g
	 *            Graphics
	 */
	public void paint(Graphics g) {
		if (thumbnail == null) {
			loadImage();
		}

		if (thumbnail != null) {
			int x = (getWidth() / 2) - (thumbnail.getIconWidth() / 2);
			int y = (getHeight() / 2) - (thumbnail.getIconHeight() / 2);

			if (y < 0) {
				y = 0;
			}

			if (x < 5) {
				x = 5;
			}

			thumbnail.paintIcon(this, g, x, y);
		}
	}
}
