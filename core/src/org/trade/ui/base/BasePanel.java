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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.util.Reflector;

/**
 * 
 * @version $Id: BasePanel.java,v 1.21 2002/01/22 21:59:35 simon Exp $
 * @author Simon Allen
 */
public abstract class BasePanel extends JPanel implements MessageListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7015215807608484202L;

	private final static Logger _log = LoggerFactory.getLogger(BasePanel.class);

	public static final int INFORMATION = 1;
	public static final int WARNING = 2;
	public static final int ERROR = 3;
	public static final int PROGRESS = 4;

	private static final int CLEAR = 0;
	private static BasePanelMenu menuBar = null;
	private static Frame m_frame = null;
	private static JTextField m_statusBar = null;
	private static JProgressBar m_progressBar = null;
	private boolean m_isSelected = false;

	public BasePanel() {

		Dimension size = new Dimension(400, 300);
		this.setPreferredSize(size);

	}

	/**
	 * Method setSelected.
	 * 
	 * @param selected
	 *            boolean
	 */
	public void setSelected(boolean selected) {
		m_isSelected = selected;
	}

	/**
	 * Method isSelected.
	 * 
	 * @return boolean
	 */
	public boolean isSelected() {
		return m_isSelected;
	}

	/**
	 * Method setStatusBar.
	 * 
	 * @param statusBar
	 *            JTextField
	 */
	public void setStatusBar(JTextField statusBar) {
		if (m_statusBar == null) {
			m_statusBar = statusBar;
		}
	}

	/**
	 * Method setProgressBar.
	 * 
	 * @param progressBar
	 *            JProgressBar
	 */
	public void setProgressBar(JProgressBar progressBar) {
		if (m_progressBar == null) {
			m_progressBar = progressBar;
		}
	}

	/**
	 * Method getProgressBar.
	 * 
	 * @return JProgressBar
	 */
	public static JProgressBar getProgressBar() {
		return m_progressBar;
	}

	public void clearStatusBarMessage() {
		setStatusBarMessage("", BasePanel.CLEAR);
	}

	/**
	 * Method setStatusBarMessage.
	 * 
	 * @param message
	 *            String
	 * @param state
	 *            int
	 */
	public void setStatusBarMessage(String message, int state) {
		switch (state) {
		case 0: {
			m_statusBar.setBackground(Color.white);
			m_statusBar.setText("");
			break;
		}
		case 1: {
			_log.info(message);
			m_statusBar.setBackground(Color.green);
			break;
		}
		case 2: {
			_log.warn(message);
			m_statusBar.setBackground(Color.yellow);
			break;
		}
		case 3: {
			_log.error(message);
			m_statusBar.setBackground(Color.red);
			break;
		}
		case 4: {
			m_statusBar.setBackground(Color.yellow);
			break;
		}
		default: {
			m_statusBar.setBackground(Color.white);
		}
		}

		if ((message != null) && (state != BasePanel.CLEAR)) {
			m_statusBar.setText(message);
		}
	}

	/**
	 * Method getFrame.
	 * 
	 * @return Frame
	 */
	protected Frame getFrame() {
		if (m_frame == null) {
			Component parent = this;
			while ((parent != null) && !(parent instanceof JFrame)) {
				parent = parent.getParent();
			}
			m_frame = (Frame) parent;
		}
		return m_frame;
	}

	/**
	 * Method handleEvent.
	 * 
	 * @param e
	 *            MessageEvent
	 * @param parm
	 *            Vector<Object>
	 * @see org.trade.ui.base.MessageListener#handleEvent(MessageEvent,
	 *      Vector<Object>)
	 */
	public void handleEvent(MessageEvent e, Vector<Object> parm) {

		if ((e.getSource() instanceof String) && m_isSelected) {
			String method = (String) e.getSource();
			// _log.info("Fire Method: " + method + " in Class: "
			// + this.getClass().getName());
			doFireMethod(method, parm);
		}
	}

	public abstract void doWindowClose();

	public abstract void doWindowActivated();

	/**
	 * Method doWindowDeActivated.
	 * 
	 * @return boolean
	 */
	public abstract boolean doWindowDeActivated();

	public abstract void doWindowOpen();

	/**
	 * Method doFireMethod.
	 * 
	 * @param methodName
	 *            String
	 * @param parm
	 *            Vector<Object>
	 */
	protected synchronized void doFireMethod(String methodName,
			Vector<Object> parm) {

		int vectorSize = 0;
		vectorSize = parm.size();

		Class<?>[] parms = new Class[vectorSize];
		Object[] objects = new Object[vectorSize];
		StringBuffer classes = new StringBuffer();

		for (Object object : parm) {
			Object obj = object;
			classes.append(obj.getClass().getName() + "\n");
			parms[parm.indexOf(obj)] = obj.getClass();
			objects[parm.indexOf(obj)] = obj;
		}

		try {

			Method method = Reflector.findMethod(this.getClass(), methodName,
					parms);

			if (null != method) {
				method.invoke(this, objects);
			}
		} catch (Exception e) {
			// Do nothing this panel is not actively listening for this event
			_log.error("Exception in reflection BasePanel method: "
					+ methodName + " Parms #: " + vectorSize + " Method "
					+ methodName + " Parms class: " + classes
					+ " not found in class: " + this.getClass().getName()
					+ " Error Msg: " + e.getMessage());
			setStatusBarMessage(e.getMessage(), ERROR);
		}
	}

	/**
	 * Method getMenu.
	 * 
	 * @return BasePanelMenu
	 */
	public static BasePanelMenu getMenu() {
		return menuBar;
	}

	/**
	 * Method setMenu.
	 * 
	 * @param menu
	 *            BasePanelMenu
	 */
	public void setMenu(final BasePanelMenu menu) {
		menuBar = menu;
	}

	/**
	 * Method setErrorMessage.
	 * 
	 * @param title
	 *            String
	 * @param message
	 *            String
	 * @param ex
	 *            Exception
	 */
	public void setErrorMessage(String title, String message, Exception ex) {
		this.setStatusBarMessage(message, ERROR);
		JOptionPane.showMessageDialog(getFrame(), message
				+ " See log for details.", title, JOptionPane.ERROR_MESSAGE);
		this.clearStatusBarMessage();
	}
}
