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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.ui.widget.Clock;

/**
 * 
 * @version $Id: MDIAppPanel,v 1.28 2002/01/24 18:13:18 simon Exp $
 * @author Simon Allen
 */
public abstract class MDIAppPanel extends BasePanel implements ActionListener,
		VetoableChangeListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8405644422808736326L;

	private final static Logger _log = LoggerFactory
			.getLogger(MDIAppPanel.class);

	private final ExtendedDesktopPane m_desktopPane = new ExtendedDesktopPane();

	protected JInternalFrame m_internalFrame = null;
	private JPanel m_menuPanel = new JPanel();

	public String m_title = null;
	private static final int layer = 1;
	private static int frameCount = 0;
	protected JInternalFrame currentIFrame = null;
	private PrintController m_printJob = new PrintController();

	/**
	 * Constructor for MDIAppPanel.
	 * 
	 * @param frame
	 *            Frame
	 */
	public MDIAppPanel(Frame frame) {

		try {

			this.setLayout(new BorderLayout());

			m_menuPanel.setLayout(new BorderLayout());
			m_desktopPane.setDesktopManager(new ExtendedDesktopManager(
					m_desktopPane));

			JPanel jPanel1 = new JPanel(new BorderLayout());
			JPanel jPanelProgressBar = new JPanel(new FlowLayout(
					FlowLayout.RIGHT));
			JProgressBar progressBar = new JProgressBar(0, 0);
			jPanelProgressBar.add(progressBar);

			JPanel jPanelClock = new JPanel(new FlowLayout(FlowLayout.LEFT));
			Clock clock = new Clock();
			jPanelClock.add(clock);

			JPanel jPanelStatus = new JPanel(new GridLayout());
			JTextField jTextFieldStatus = new JTextField();
			jTextFieldStatus.setRequestFocusEnabled(false);
			jTextFieldStatus.setMargin(new Insets(5, 5, 5, 5));
			jTextFieldStatus.setBackground(Color.white);
			jTextFieldStatus
					.setBorder(BorderFactory.createLoweredBevelBorder());
			jPanelStatus.add(jTextFieldStatus);

			JPanel jPanel3 = new JPanel(new BorderLayout());
			jPanel3.add(jPanelClock, BorderLayout.WEST);
			jPanel3.add(jPanelProgressBar, BorderLayout.EAST);
			jPanel3.add(jPanelStatus, BorderLayout.CENTER);

			JPanel jPanel4 = new JPanel(new BorderLayout());
			jPanel4.add(m_desktopPane, BorderLayout.CENTER);
			jPanel1.add(jPanel4, BorderLayout.CENTER);
			jPanel1.add(jPanel3, BorderLayout.SOUTH);
			jPanel1.add(m_menuPanel, BorderLayout.NORTH);
			this.add(jPanel1, BorderLayout.CENTER);
			this.setStatusBar(jTextFieldStatus);
			this.setProgressBar(progressBar);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Method setMenu.
	 * 
	 * @param menu
	 *            BasePanelMenu
	 */
	public void setMenu(BasePanelMenu menu) {
		m_menuPanel.removeAll();
		m_menuPanel.add(menu, BorderLayout.NORTH);
		super.setMenu(menu);
	}

	/**
	 * Method addInternalFrame.
	 * 
	 * @param title
	 *            String
	 * @param innerPanel
	 *            BasePanel
	 */
	public void addInternalFrame(String title, final BasePanel innerPanel) {

		currentIFrame = new JInternalFrame(title, true, true, true, true);

		try {
			currentIFrame.setFrameIcon(ImageBuilder.getImageIcon("trade.gif"));
		} catch (Exception ex) {
			_log.error("Could not get Image trade.gif Msg: " + ex.getMessage(),
					ex);
		}
		frameCount++;
		// Increment the tile in case the same frame is open more than once
		title = title + frameCount;

		currentIFrame.setTitle(title);
		currentIFrame.setPreferredSize(innerPanel.getPreferredSize());
		// currentIFrame.setBounds(20 * (frameCount % 10), 20 * (frameCount %
		// 10),
		// currentIFrame.getPreferredSize().width,
		// currentIFrame.getPreferredSize().height);
		currentIFrame
				.setContentPane(new MyPanel(layer, frameCount, innerPanel));
		m_desktopPane.add(currentIFrame, new Integer(layer));

		try {
			currentIFrame.setSelected(true);
			currentIFrame.setMaximum(true);
		} catch (PropertyVetoException e) {
			_log.error("Could not open JInternalFrame", e);
		}
		currentIFrame.pack();
		currentIFrame.show();
		setComponentSize(currentIFrame);
		currentIFrame.addPropertyChangeListener(this);
		currentIFrame.addVetoableChangeListener(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				innerPanel.doWindowOpen();
			}
		});
	}

	public void doWindowOpen() {

	}

	public void doWindowClose() {
		doExit();
	}

	public void doWindowActivated() {
	}

	public void doLFMetal() {
		try {
			UIManager
					.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
			SwingUtilities.updateComponentTreeUI(getFrame());
		} catch (Exception eMetal) {
			_log.error("Could not load LookAndFeel: " + eMetal);
		}
	}

	public void doLFWindows() {
		try {
			UIManager
					.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
			SwingUtilities.updateComponentTreeUI(getFrame());
		} catch (Exception eMetal) {
			_log.error("Could not load LookAndFeel: " + eMetal);
		}
	}

	public void doLFMotif() {
		try {
			UIManager
					.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());
			UIManager.put("swing.boldMetal", Boolean.FALSE);
			SwingUtilities.updateComponentTreeUI(getFrame());
		} catch (Exception eMetal) {
			_log.error("Could not load LookAndFeel: " + eMetal);
		}
	}

	public void doExit() {
		System.exit(0);
	}

	public void doHelp() {

	}

	public void doPrint() {
	}

	/**
	 * Method printComponent.
	 * 
	 * @param comp
	 *            Component
	 */
	protected void printComponent(Component comp) {
		m_printJob.printComponent(getFrame(), comp, null);
	}

	public void doClose() {
		try {
			currentIFrame.setClosed(true);
		} catch (PropertyVetoException eClose) {
			// Do nothing
		}
	}

	public void doCloseAll() {
		JInternalFrame[] frames = m_desktopPane.getAllFrames();
		for (JInternalFrame frame : frames) {
			try {
				frame.setClosed(true);
			} catch (PropertyVetoException eClose) {
				// Do nothing
			}

		}
	}

	public void doCascadeAll() {
		m_desktopPane.cascadeAll();
	}

	public void doTileAll() {

		m_desktopPane.tileAll();
	}

	public void doCascade() {
		m_desktopPane.cascade(currentIFrame);
	}

	/**
	 * Method actionPerformed.
	 * 
	 * @param evt
	 *            ActionEvent
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		JMenuItem mi = (JMenuItem) evt.getSource();

		if (mi.getActionCommand().equals("Cascade")) {
			doCascade();
		} else if (mi.getActionCommand().equals("Cascade All")) {
			doCascadeAll();
		} else if (mi.getActionCommand().equals("Tile All")) {
			doTileAll();
		} else if (mi.getActionCommand().equals("Close")) {
			doClose();
		}

		this.repaint();
	}

	/**
	 * Method getSelectPanel.
	 * 
	 * @return BasePanel
	 */
	public BasePanel getSelectPanel() {
		return ((MyPanel) currentIFrame.getContentPane()).getInnerPanel();
	}

	/**
	 * Method propertyChange.
	 * 
	 * @param epc
	 *            PropertyChangeEvent
	 * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent epc) {

		if (epc.getSource() instanceof JInternalFrame) {

			String name = epc.getPropertyName();
			Object value = epc.getNewValue();

			if (name.equals(JInternalFrame.IS_CLOSED_PROPERTY)
					&& ((Boolean) value == Boolean.TRUE)) {

				currentIFrame = (JInternalFrame) epc.getSource();
				((BasePanel) currentIFrame.getContentPane()).doWindowClose();
				// Remove it from the hastable and all listeners
				currentIFrame.removePropertyChangeListener(this);
				currentIFrame.removeVetoableChangeListener(this);

			} else if (name.equals(JInternalFrame.IS_SELECTED_PROPERTY)
					&& ((Boolean) value == Boolean.TRUE)) {
				// switch current frame
				((BasePanel) currentIFrame.getContentPane()).setSelected(false);
				currentIFrame = (JInternalFrame) epc.getSource();
				((BasePanel) currentIFrame.getContentPane())
						.clearStatusBarMessage();
				((BasePanel) currentIFrame.getContentPane()).setSelected(true);
				((BasePanel) currentIFrame.getContentPane())
						.doWindowActivated();
			}
		}
	}

	/**
	 * Method vetoableChange.
	 * 
	 * @param evc
	 *            PropertyChangeEvent
	 * @throws PropertyVetoException
	 * @see java.beans.VetoableChangeListener#vetoableChange(PropertyChangeEvent)
	 */
	public void vetoableChange(PropertyChangeEvent evc)
			throws PropertyVetoException {
		if (evc.getSource() instanceof JInternalFrame) {
			currentIFrame = (JInternalFrame) evc.getSource();

			String name = evc.getPropertyName();
			Object value = evc.getNewValue();
			if (name.equals(JInternalFrame.IS_CLOSED_PROPERTY)
					&& ((Boolean) value == Boolean.TRUE)) {

			}
		}
	}

	/**
	 * Method setComponentSize.
	 * 
	 * @param component
	 *            Component
	 */
	private void setComponentSize(Component component) {
		if ((20 + component.getHeight()) > this.getVisibleRect().height) {
			component.setSize(new Dimension(component.getSize().width, (this
					.getVisibleRect().height - 20)));
		}

		if ((20 + component.getWidth()) > this.getVisibleRect().width) {
			component.setSize(new Dimension((this.getVisibleRect().width - 20),
					component.getSize().height));
		}

		component.setSize(new Dimension(this.getVisibleRect().width, this
				.getVisibleRect().height));
	}

	/**
	 */
	class MyPanel extends BasePanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1746964092801489754L;
		BasePanel pane = null;

		public MyPanel() {
			super();
		}

		/**
		 * Constructor for MyPanel.
		 * 
		 * @param layer
		 *            int
		 * @param count
		 *            int
		 */
		public MyPanel(int layer, int count) {
			super();

			pane = new MyPanel();
			JLabel layerLabel = new JLabel("Layer " + layer);

			layerLabel.setOpaque(false);
			add(pane, BorderLayout.CENTER);
		}

		/**
		 * Constructor for MyPanel.
		 * 
		 * @param layer
		 *            int
		 * @param count
		 *            int
		 * @param innerPanel
		 *            BasePanel
		 */
		public MyPanel(int layer, int count, BasePanel innerPanel) {
			super();

			pane = innerPanel;
			// pane.setOpaque(false);
			this.setLayout(new BorderLayout());
			JLabel layerLabel = new JLabel("Layer " + layer);
			layerLabel.setOpaque(false);
			add(pane, BorderLayout.CENTER);
		}

		/**
		 * Method getInnerPanel.
		 * 
		 * @return BasePanel
		 */
		public BasePanel getInnerPanel() {
			return pane;
		}

		public void doWindowClose() {

		}

		public void doWindowActivated() {
		}

		/**
		 * Method doWindowDeActivated.
		 * 
		 * @return boolean
		 */
		public boolean doWindowDeActivated() {
			return true;
		}

		public void doWindowOpen() {

		}
	}

}
