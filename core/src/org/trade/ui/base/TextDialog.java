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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 
 * @version $Id: TextDialog.java,v 1.3 2001/12/20 17:11:29 simon Exp $
 * @author Simon Allen
 */
public class TextDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3288606526317779365L;

	private String m_text = null;
	private boolean m_cancel = true;
	private JComponent m_component = null;
	private JButton buttonOk = new JButton();
	private JButton buttonCancel = new JButton();

	/**
	 * Constructor for TextDialog.
	 * 
	 * @param frame
	 *            Frame
	 * @param title
	 *            String
	 * @param modal
	 *            boolean
	 * @param component
	 *            JComponent
	 */
	public TextDialog(Frame frame, String title, boolean modal,
			JComponent component) {
		super(frame, title, modal);

		if (component == null) {
			m_component = new JTextArea();
		} else {
			m_component = component;
		}
		JScrollPane detailArea = new JScrollPane();
		buttonOk.setText("OK");
		buttonOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (m_component instanceof JTextArea) {
					setText(((JTextArea) m_component).getText().trim());
				}
				setCancel(false);
				dispose();
			}
		});
		buttonCancel.setText("Cancel");
		buttonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setCancel(true);
				dispose();
			}
		});
		JPanel jPanel = new JPanel(new BorderLayout());
		JPanel jPanel1 = new JPanel();
		JPanel jPanel2 = new JPanel(new BorderLayout());
		JPanel jPanel3 = new JPanel(new GridLayout());
		jPanel3.add(buttonOk, null);
		jPanel3.add(buttonCancel, null);
		detailArea.getViewport().add(m_component, null);
		jPanel2.add(detailArea, BorderLayout.CENTER);
		jPanel1.add(jPanel3, BorderLayout.CENTER);
		jPanel.add(jPanel2, BorderLayout.CENTER);
		jPanel.add(jPanel1, BorderLayout.SOUTH);
		this.getContentPane().add(jPanel);
		pack();
	}

	/**
	 * Constructor for TextDialog.
	 * 
	 * @param frame
	 *            Frame
	 * @param title
	 *            String
	 */
	public TextDialog(Frame frame, String title) {
		this(frame, title, false, null);
	}

	/**
	 * Constructor for TextDialog.
	 * 
	 * @param frame
	 *            Frame
	 */
	public TextDialog(Frame frame) {
		this(frame, "", false, null);
	}

	/**
	 * Method this_windowClosing.
	 * 
	 * @param e
	 *            WindowEvent
	 */
	void this_windowClosing(WindowEvent e) {
		setCancel(true);
		dispose();
	}

	/**
	 * Method getText.
	 * 
	 * @return String
	 */
	public String getText() {
		return m_text;
	}

	/**
	 * Method setText.
	 * 
	 * @param text
	 *            String
	 */
	public void setText(String text) {
		m_text = text;
	}

	/**
	 * Method getCancel.
	 * 
	 * @return String
	 */
	public boolean getCancel() {
		return m_cancel;
	}

	/**
	 * Method getOKButton.
	 * 
	 * @return JButton
	 */
	public JButton getOKButton() {
		return buttonOk;
	}

	/**
	 * Method getCancelButton.
	 * 
	 * @return JButton
	 */
	public JButton getCancelButton() {
		return buttonCancel;
	}

	/**
	 * Method setCancel.
	 * 
	 * @param cancel
	 *            boolean
	 */
	public void setCancel(boolean cancel) {
		m_cancel = cancel;
	}
}
