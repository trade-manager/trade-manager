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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.util.Properties;

import javax.swing.JFrame;

/**
 * 
 * @version $Id: PrintController.java,v 1.3 2001/10/22 18:57:58 simon Exp $
 * @author Simon Allen
 */
public class PrintController {
	Properties props = new Properties();

	public PrintController() {
	}

	/**
	 * printComponent() - constructor
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param frame
	 *            Frame
	 * @param comp
	 *            Component
	 * @param printJobName
	 *            String
	 * @exception * @see
	 */
	public void printComponent(Frame frame, Component comp, String printJobName) {
		if (printJobName == null) {
			printJobName = comp.getClass().getName();
		}

		if ((frame != null) && (comp != null)) {
			PrintJob pj = Toolkit.getDefaultToolkit().getPrintJob(frame,
					printJobName, props);

			if (pj != null) {
				Graphics g = pj.getGraphics();
				Dimension od = comp.getSize();
				Dimension pd = pj.getPageDimension();

				g.translate((pd.width - od.width) / 2,
						(pd.height - od.height) / 2);

				if (comp instanceof JFrame) {
					comp.printAll(g);
				} else {
					comp.print(g);
				}

				g.dispose();
				pj.end();
			}
		}
	}
}
