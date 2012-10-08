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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class TabbedCloseButton extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8543984162821384818L;
	private final static Logger _log = LoggerFactory
			.getLogger(TabbedCloseButton.class);
	protected MessageNotifier m_notifier = new MessageNotifier();
	private String m_method = null;
	private Object transferObject = null;
	private JTabbedPane pane = null;

	/**
	 * Constructor
	 * @param pane JTabbedPane
	 * @param basePanel BasePanel
	 */
	public TabbedCloseButton(final JTabbedPane pane, BasePanel basePanel) {

		this.pane = pane;

		// unset default FlowLayout' gaps
		this.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		if (pane == null) {
			throw new NullPointerException("TabbedPane is null");
		}
		setOpaque(false);

		// make JLabel read titles from JTabbedPane
		JLabel label = new JLabel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public String getText() {
				int i = pane.indexOfTabComponent(TabbedCloseButton.this);
				if (i != -1) {
					return pane.getTitleAt(i);
				}
				return null;
			}
		};

		add(label);
		// add more space between the label and the button
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		// tab button
		TabButton button = new TabButton(basePanel, BaseUIPropertyCodes.CLOSE);
		if (basePanel != null) {
			this.addMessageListener(basePanel);
		}
		add(button);

		// add more space to the top of the component
		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}

	/**
	 */
	private class TabButton extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7095664809922973665L;

		/**
		 * Constructor for TabButton.
		 * @param basePanel BasePanel
		 * @param UICode String
		 */
		public TabButton(BasePanel basePanel, String UICode) {

			try {

				BaseUIPropertyCodes basePropertyCodes = BaseUIPropertyCodes
						.newInstance(UICode);

				if (basePropertyCodes.getImage().length() > 0) {
					setIcon(ImageBuilder.getImageIcon(basePropertyCodes
							.getImage()));
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

				int size = 17;
				setPreferredSize(new Dimension(size, size));
				setToolTipText("Close tab");
				// Make the button looks the same for all Laf's
				setUI(new BasicButtonUI());
				// Make it transparent
				setContentAreaFilled(false);
				// No need to be focusable
				setFocusable(false);
				setBorder(BorderFactory.createEtchedBorder());
				setBorderPainted(false);
				// Making nice rollover effect
				// we use the same listener for all buttons
				addMouseListener(buttonMouseListener);
				setRolloverEnabled(true);
			} catch (Exception ex) {
				_log.error(" Error instanciating Base Button ", ex);
			}
		}

		// we don't want to update UI for this button
		public void updateUI() {
		}

		// paint the cross
		/**
		 * Method paintComponent.
		 * @param g Graphics
		 */
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			// shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.MAGENTA);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight()
					- delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight()
					- delta - 1);
			g2.dispose();
		}
	}

	private final static MouseListener buttonMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};

	protected void buttonPressed() {
		int i = this.pane.indexOfTabComponent(TabbedCloseButton.this);
		if (i != -1) {
			this.setTransferObject(new Integer(i));
			if (getMethod() != null) {
				this.messageEvent(getMethod());
			}
		}
	}

	/**
	 * Method addMessageListener.
	 * @param listener MessageListener
	 */
	public void addMessageListener(MessageListener listener) {
		m_notifier.add(listener);
	}

	/**
	 * Method removeMessageListener.
	 * @param listener MessageListener
	 */
	public void removeMessageListener(MessageListener listener) {
		m_notifier.remove(listener);
	}

	/**
	 * Method setTransferObject.
	 * @param transferObject Object
	 */
	public void setTransferObject(Object transferObject) {
		this.transferObject = transferObject;
	}

	/**
	 * Method getTransferObject.
	 * @return Object
	 */
	public Object getTransferObject() {
		return this.transferObject;
	}

	/**
	 * Method messageEvent.
	 * @param selection String
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
	 * @param method String
	 */
	private void setMethod(String method) {
		m_method = method;
	}

	/**
	 * Method getMethod.
	 * @return String
	 */
	public String getMethod() {
		return m_method;
	}
}
