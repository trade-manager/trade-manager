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

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.JMenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @version $Id: BaseMenuItem.java,v 1.6 2001/11/09 18:24:58 garrick Exp $
 * @author Simon Allen
 */
public class BaseMenuItem extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5816221538464868893L;

	private final static Logger _log = LoggerFactory.getLogger(BaseMenuItem.class);

	protected MessageNotifier m_notifier = new MessageNotifier();

	private String m_method = null;

	/**
	 * BaseMenuItem() - constructor
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param p
	 *            BasePanel
	 * @param basePropertyCodes
	 *            BaseUIPropertyCodes
	 * @exception *
	 * 				@see
	 */
	public BaseMenuItem(BasePanel p, BaseUIPropertyCodes basePropertyCodes) {
		try {
			if (p != null) {
				this.addMessageListener(p);
			}

			if (basePropertyCodes.getDisplayName().length() == 0) {
				setIcon(ImageBuilder.getImageIcon(basePropertyCodes.getImage()));
			} else {
				setText(basePropertyCodes.getDisplayName());
				setMnemonic(basePropertyCodes.getMnemonic());
			}

			setMargin(new Insets(2, 2, 2, 2));
			setHorizontalTextPosition(0);
			setToolTipText(basePropertyCodes.getToolTip());
			setEnabled(basePropertyCodes.isEnabled());
			setMethod(basePropertyCodes.getMethod());
			this.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					buttonPressed();
				}
			});
		} catch (Exception ex) {
			_log.error(" Error instanciating Base Menu Item ", ex);
		}
	}

	/**
	 * BaseMenuItem() - constructor
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param p
	 *            BasePanel
	 * @param UICode
	 *            String
	 * @exception *
	 * 				@see
	 */
	public BaseMenuItem(BasePanel p, String UICode) {
		try {
			if (p != null) {
				this.addMessageListener(p);
			}

			BaseUIPropertyCodes basePropertyCodes = BaseUIPropertyCodes.newInstance(UICode);

			if (basePropertyCodes.getDisplayName().length() == 0) {
				setIcon(ImageBuilder.getImageIcon(basePropertyCodes.getImage()));
			} else {
				setText(basePropertyCodes.getDisplayName());
				setMnemonic(basePropertyCodes.getMnemonic());
			}

			setMargin(new Insets(2, 2, 2, 2));
			setHorizontalTextPosition(0);
			setToolTipText(basePropertyCodes.getToolTip());
			setEnabled(basePropertyCodes.isEnabled());
			setMethod(basePropertyCodes.getMethod());
			this.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					buttonPressed();
				}
			});
		} catch (Exception ex) {
			_log.error(" Error instanciating Base Menu Item ", ex);
		}
	}

	/**
	 * actionPerformed() - button action performed
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @exception *
	 * 				@see
	 */
	private void buttonPressed() {
		if (getMethod() != null) {
			this.messageEvent(getMethod());
		}
	}

	/**
	 * addMessageListener() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param listener
	 *            MessageListener
	 * @exception *
	 * 				@see
	 */
	public void addMessageListener(MessageListener listener) {
		m_notifier.add(listener);
	}

	/**
	 * removeMessageListener() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param listener
	 *            MessageListener
	 * @exception *
	 * 				@see
	 */
	public void removeMessageListener(MessageListener listener) {
		m_notifier.remove(listener);
	}

	/**
	 * messageEvent() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param selection
	 *            String
	 * @exception *
	 * 				@see
	 */
	protected void messageEvent(String selection) {
		m_notifier.notifyEvent(new MessageEvent(selection), new Vector<Object>());
	}

	/**
	 * setMethod() - button action performed
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param method
	 *            String
	 * @exception *
	 * 				@see
	 */
	private void setMethod(String method) {
		m_method = method;
	}

	/**
	 * getMethod() - button action performed
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return String
	 * @exception *
	 * 				@see
	 */
	public String getMethod() {
		return m_method;
	}
}
