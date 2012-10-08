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
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**

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
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel3 = new JPanel();
	private JPanel jPanel = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private JComponent m_component = null;
	private JScrollPane detailArea = new JScrollPane();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JButton buttonOk = new JButton();
	private JButton buttonCancel = new JButton();
	private BorderLayout borderLayout2 = new BorderLayout();
	private GridLayout gridLayout1 = new GridLayout();

	/**
	 * Constructor for TextDialog.
	 * @param frame Frame
	 * @param title String
	 * @param modal boolean
	 * @param component JComponent
	 */
	public TextDialog(Frame frame, String title, boolean modal,
			JComponent component) {
		super(frame, title, modal);

		if (component == null) {
			m_component = new JTextArea();
		} else {
			m_component = component;
		}
		buttonOk.setText("OK");
		buttonOk.addActionListener(new OKButtonAdapter(this));
		buttonCancel.setText("Cancel");
		buttonCancel.addActionListener(new CancelButtonAdapter(this));
		jPanel2.setLayout(borderLayout1);
		jPanel3.setLayout(gridLayout1);
		jPanel3.add(buttonOk, null);
		jPanel3.add(buttonCancel, null);
		detailArea.getViewport().add(m_component, null);
		jPanel2.add(detailArea, BorderLayout.CENTER);
		jPanel.setLayout(borderLayout2);
		jPanel1.add(jPanel3, BorderLayout.CENTER);
		jPanel.add(jPanel2, BorderLayout.CENTER);
		jPanel.add(jPanel1, BorderLayout.SOUTH);
		this.getContentPane().add(jPanel);
		pack();

	}
	
	/**
	 * Method getOKButton.
	 * @return JButton
	 */
	public JButton getOKButton(){
		return buttonOk;
	}

	/**
	 * Method getCancelButton.
	 * @return JButton
	 */
	public JButton getCancelButton(){
		return buttonCancel;
	}
	
	
	/**
	 * Constructor for TextDialog.
	 * @param frame Frame
	 * @param title String
	 */
	public TextDialog(Frame frame, String title) {
		this(frame, title, false, null);
	}

	/**
	 * Constructor for TextDialog.
	 * @param frame Frame
	 */
	public TextDialog(Frame frame) {
		this(frame, "", false, null);
	}

	/**
	 * Method doOK.
	 * @param e ActionEvent
	 */
	void doOK(ActionEvent e) {
		if (m_component instanceof JTextArea) {
			this.setText(((JTextArea) m_component).getText().trim());
		}
		setCancel(false);
		dispose();
	}

	/**
	 * Method doCancel.
	 * @param e ActionEvent
	 */
	void doCancel(ActionEvent e) {
		setCancel(true);
		dispose();
	}

	/**
	 * Method this_windowClosing.
	 * @param e WindowEvent
	 */
	void this_windowClosing(WindowEvent e) {
		setCancel(true);
		dispose();
	}

	/**
	 * Method getText.
	 * @return String
	 */
	public String getText() {
		return m_text;
	}

	/**
	 * Method setText.
	 * @param text String
	 */
	public void setText(String text) {
		m_text = text;
	}

	/**
	 * Method getCancel.
	 * @return boolean
	 */
	public boolean getCancel() {
		return m_cancel;
	}

	/**
	 * Method setCancel.
	 * @param cancel boolean
	 */
	public void setCancel(boolean cancel) {
		m_cancel = cancel;
	}
}

/**
 */
class CancelButtonAdapter implements java.awt.event.ActionListener {
	TextDialog adaptee;

	/**
	 * Constructor for CancelButtonAdapter.
	 * @param adaptee TextDialog
	 */
	CancelButtonAdapter(TextDialog adaptee) {
		this.adaptee = adaptee;
	}

	/**
	 * Method actionPerformed.
	 * @param e ActionEvent
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		adaptee.doCancel(e);
	}
}

/**
 */
class OKButtonAdapter implements java.awt.event.ActionListener {
	TextDialog adaptee;

	/**
	 * Constructor for OKButtonAdapter.
	 * @param adaptee TextDialog
	 */
	OKButtonAdapter(TextDialog adaptee) {
		this.adaptee = adaptee;
	}

	/**
	 * Method actionPerformed.
	 * @param e ActionEvent
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		adaptee.doOK(e);
	}
}
