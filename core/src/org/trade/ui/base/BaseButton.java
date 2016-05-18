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

import javax.swing.JButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class BaseButton extends JButton {
	/**
	 * 
	 */
	private static final long serialVersionUID = -47827936580637959L;
	private final static Logger _log = LoggerFactory.getLogger(BaseButton.class);
	protected MessageNotifier m_notifier = new MessageNotifier();
	private String m_method = null;
	private Object transferObject = null;

	/**
	 * Constructor for BaseButton.
	 * 
	 * @param basePanel
	 *            BasePanel
	 * @param basePropertyCodes
	 *            BaseUIPropertyCodes
	 */
	public BaseButton(BasePanel basePanel, BaseUIPropertyCodes basePropertyCodes) {
		this(basePanel, basePropertyCodes, 2);
	}

	/**
	 * Constructor for BaseButton.
	 * 
	 * @param basePanel
	 *            BasePanel
	 * @param basePropertyCodes
	 *            BaseUIPropertyCodes
	 * @param margin
	 *            int
	 */
	public BaseButton(BasePanel basePanel, BaseUIPropertyCodes basePropertyCodes, int margin) {
		try {
			if (basePanel != null) {
				this.addMessageListener(basePanel);
			}

			if (basePropertyCodes.getImage().length() > 0) {
				setIcon(ImageBuilder.getImageIcon(basePropertyCodes.getImage()));
			} else {
				setText(basePropertyCodes.getDisplayName());
				setMnemonic(basePropertyCodes.getMnemonic());
			}

			setMargin(new Insets(margin, margin, margin, margin));
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
			_log.error(" Error instanciating Base Button ", ex);
		}
	}

	/**
	 * Constructor for BaseButton.
	 * 
	 * @param basePanel
	 *            BasePanel
	 * @param UICode
	 *            String
	 * @param margin
	 *            int
	 */
	public BaseButton(BasePanel basePanel, String UICode, int margin) {
		try {
			if (basePanel != null) {
				this.addMessageListener(basePanel);
			}

			BaseUIPropertyCodes basePropertyCodes = BaseUIPropertyCodes.newInstance(UICode);

			if (basePropertyCodes.getImage().length() > 0) {
				setIcon(ImageBuilder.getImageIcon(basePropertyCodes.getImage()));
			} else {
				setText(basePropertyCodes.getDisplayName());
				setMnemonic(basePropertyCodes.getMnemonic());
			}

			setMargin(new Insets(margin, margin, margin, margin));
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
			_log.error(" Error instanciating Base Button ", ex);
		}
	}

	/**
	 * Constructor for BaseButton.
	 * 
	 * @param basePanel
	 *            BasePanel
	 * @param UICode
	 *            String
	 */
	public BaseButton(BasePanel basePanel, String UICode) {
		this(basePanel, UICode, 2);
	}

	protected void buttonPressed() {
		if (getMethod() != null) {
			this.messageEvent(getMethod());
		}
	}

	/**
	 * Method addMessageListener.
	 * 
	 * @param listener
	 *            MessageListener
	 */
	public void addMessageListener(MessageListener listener) {
		m_notifier.add(listener);
	}

	/**
	 * Method remove.
	 * 
	 * @param listener
	 *            MessageListener
	 */
	public void remove(MessageListener listener) {
		m_notifier.remove(listener);
	}

	/**
	 * Method setTransferObject.
	 * 
	 * @param transferObject
	 *            Object
	 */
	public void setTransferObject(Object transferObject) {
		this.transferObject = transferObject;
	}

	/**
	 * Method getTransferObject.
	 * 
	 * @return Object
	 */
	public Object getTransferObject() {
		return this.transferObject;
	}

	/**
	 * Method messageEvent.
	 * 
	 * @param selection
	 *            String
	 */
	protected void messageEvent(String selection) {
		Vector<Object> transferObjects = new Vector<Object>();
		if (null != this.transferObject) {
			transferObjects.add(this.transferObject);
		}
		m_notifier.notifyEvent(new MessageEvent(selection), transferObjects);
	}

	/**
	 * Method setMethod.
	 * 
	 * @param method
	 *            String
	 */
	private void setMethod(String method) {
		m_method = method;
	}

	/**
	 * Method getMethod.
	 * 
	 * @return String
	 */
	public String getMethod() {
		return m_method;
	}
}
