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

import java.awt.BorderLayout;
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
	BorderLayout borderLayout1 = new BorderLayout();
	TradeAppFrame frame = null;

	// Construct the application
	public TradeAppMain() {
		frame = new TradeAppFrame();

		frame.setSize(1200, 900);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

		frame.setLocation((d.width - frame.getSize().width) / 2,
				(d.height - frame.getSize().height) / 2);
		frame.setIconImage(ImageBuilder.getImage("trade.gif"));
		frame.validate();
		frame.repaint();
		frame.setVisible(true);
		frame.mainPanel.openTradingdayView();
		frame.mainPanel.openContractView();
		frame.mainPanel.openPortfolioView();
		frame.mainPanel.openConfigurationView();
		frame.mainPanel.openStrategyView();

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
