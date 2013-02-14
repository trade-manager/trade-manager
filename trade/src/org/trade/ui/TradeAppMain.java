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
package org.trade.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.SwingUtilities;

import org.trade.ui.base.ImageBuilder;
import org.trade.ui.base.WaitCursorEventQueue;

/**
 * @author Simon Allen
 * @version $Revision: 1.0 $
 */
public class TradeAppMain {

	// Construct the application
	public TradeAppMain() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		double appWidth = screenSize.getWidth() * 0.9;
		double appHieght = screenSize.getHeight() * 0.9;
		if (appHieght > 900)
			appHieght = 900;

		if (appWidth > 1200)
			appWidth = 1200;
		TradeAppFrame frame = new TradeAppFrame();
		frame.setIconImage(ImageBuilder.getImage("trade.gif"));
		frame.setSize((int) appWidth, (int) appHieght);
		frame.setLocation((int) ((screenSize.getWidth() - frame.getSize()
				.getWidth()) / 2), (int) ((screenSize.getHeight() - frame
				.getSize().getHeight()) / 2));
		frame.validate();
		frame.repaint();
		frame.setVisible(true);
		EventQueue waitQue = new WaitCursorEventQueue(500);
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQue);
	}

	/**
	 * Method main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new TradeAppMain();
			}
		});
	}
}
