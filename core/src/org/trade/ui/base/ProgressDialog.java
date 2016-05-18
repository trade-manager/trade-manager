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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ProgressDialog extends JDialog {

	private static final long serialVersionUID = -4399430258481031373L;

	private final static Logger _log = LoggerFactory.getLogger(ProgressDialog.class);

	private static JTextField jTextFieldStatus = new JTextField();
	private static JProgressBar jProgressBar1 = new JProgressBar();

	public ProgressDialog() {
		super(new Frame(), "Please wait .....", false);

		try {
			JPanel jPanel0 = new JPanel();
			jPanel0.setLayout(new BorderLayout());
			JPanel jPanel1 = new JPanel();
			jPanel1.setLayout(new BorderLayout());
			JPanel jPanel2 = new JPanel();
			jPanel2.setLayout(new FlowLayout());
			JPanel jPanel3 = new JPanel();
			jPanel3.setLayout(new GridLayout());

			jTextFieldStatus.setEditable(false);
			jPanel3.add(jTextFieldStatus, null);
			jPanel0.add(jPanel3, BorderLayout.SOUTH);

			jProgressBar1.setStringPainted(true);
			jPanel2.add(jProgressBar1, null);
			jPanel1.add(jPanel2, BorderLayout.CENTER);
			jPanel0.add(jPanel1, BorderLayout.CENTER);
			jPanel0.setOpaque(true);

			this.getContentPane().add(jPanel0, BorderLayout.CENTER);

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation((d.width - this.getSize().width) / 2, (d.height - this.getSize().height) / 2);
			this.setSize(new Dimension(250, 85));
			// this.pack();
			// this.repaint();

		} catch (Exception ex) {
			_log.debug("Error initalizing progress Dialog", ex);
		}
	}

	/**
	 * Method main.
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String[] args) {
		try {
			ProgressDialog frame = new ProgressDialog();
			frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method setStatus.
	 * 
	 * @param message
	 *            String
	 */
	public void setStatus(String message) {
		jTextFieldStatus.setText(message);
	}

	/**
	 * Method setValueRange.
	 * 
	 * @param min
	 *            int
	 * @param max
	 *            int
	 */
	public void setValueRange(int min, int max) {
		jProgressBar1.setMaximum(max);
		jProgressBar1.setMinimum(min);
	}

	/**
	 * Method setValue.
	 * 
	 * @param value
	 *            int
	 */
	public void setValue(int value) {
		jProgressBar1.setValue(value);
	}

	/**
	 * Method getValue.
	 * 
	 * @return int
	 */
	public int getValue() {
		return jProgressBar1.getValue();
	}

	public synchronized void close() {
		this.dispose();
	}
}
