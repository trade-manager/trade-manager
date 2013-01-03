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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.ui.widget.Clock;

/**
 */
public abstract class TabbedAppPanel extends BasePanel implements
		ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8405644422808736326L;

	private final static Logger _log = LoggerFactory
			.getLogger(TabbedAppPanel.class);
	private final JTabbedPane m_tabbedPane = new JTabbedPane();

	public String m_title = null;
	private JPanel m_menuPanel = new JPanel();
	private PrintController m_printJob = new PrintController();
	private int currentTab = 0;
	private BasePanel currBasePanel = null;

	/**
	 * Constructor for TabbedAppPanel.
	 * @param frame Frame
	 */
	public TabbedAppPanel(Frame frame) {

		this.setLayout(new BorderLayout());

		JPanel jPanel1 = new JPanel(new BorderLayout());
		JPanel jPanelProgressBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
		jTextFieldStatus.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanelStatus.add(jTextFieldStatus);

		JPanel jPanel3 = new JPanel(new BorderLayout());
		jPanel3.add(jPanelClock, BorderLayout.WEST);
		jPanel3.add(jPanelProgressBar, BorderLayout.EAST);
		jPanel3.add(jPanelStatus, BorderLayout.CENTER);

		JPanel jPanel2 = new JPanel(new BorderLayout());
		jPanel2.add(m_tabbedPane, BorderLayout.CENTER);
		jPanel1.add(jPanel2, BorderLayout.CENTER);
		jPanel1.add(jPanel3, BorderLayout.SOUTH);
		m_menuPanel.setLayout(new BorderLayout());
		jPanel1.add(m_menuPanel, BorderLayout.NORTH);
		this.add(jPanel1, BorderLayout.CENTER);
		this.setStatusBar(jTextFieldStatus);
		this.setProgressBar(progressBar);
		m_tabbedPane.addChangeListener(this);
	}

	/**
	 * Method setMenu.
	 * @param menu BasePanelMenu
	 */
	public void setMenu(BasePanelMenu menu) {
		m_menuPanel.removeAll();
		m_menuPanel.add(menu, BorderLayout.NORTH);
		super.setMenu(menu);
	}

	public void doWindowOpen() {
	}

	public void doWindowClose() {
		doExit();
	}

	public void doWindowActivated() {
	}

	/**
	 * Method doWindowDeActivated.
	 * @return boolean
	 */
	public boolean doWindowDeActivated() {
		return true;
	}

	/**
	 * This method is fired when a different tab is selected.
	 * @param currBasePanel BasePanel
	 * @param newBasePanel BasePanel
	 */
	
	public abstract void tabChanged(BasePanel currBasePanel,
			BasePanel newBasePanel);

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
		for (int i = 0; i < m_tabbedPane.getTabCount(); i++) {
			currBasePanel = (BasePanel) m_tabbedPane.getComponent(i);
			currBasePanel.doWindowClose();
		}
		System.exit(0);
	}

	public void doHelp() {

	}

	public void doPrint() {
		printComponent(this.getFrame());
	}

	/**
	 * Method printComponent.
	 * @param comp Component
	 */
	protected void printComponent(Component comp) {
		m_printJob.printComponent(getFrame(), comp, null);
	}

	/**
	 * Method addTab.
	 * @param title String
	 * @param panel BasePanel
	 */
	protected void addTab(String title, final BasePanel panel) {
		m_tabbedPane.add(title, panel);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.doWindowOpen();
			}
		});

	}

	/**
	 * Method getSelectPanel.
	 * @return BasePanel
	 */
	public BasePanel getSelectPanel() {
		return this.currBasePanel;
	}

	/**
	 * Method setSelectPanel.
	 * @param tabIndex int
	 * @param event MessageEvent
	 * @param parm Vector<Object>
	 */
	public void setSelectPanel(int tabIndex, MessageEvent event,
			Vector<Object> parm) {
		setSelectPanel(tabIndex);
		this.currBasePanel.handleEvent(event, parm);
	}

	/**
	 * Method setSelectPanel.
	 * @param tabIndex int
	 */
	public void setSelectPanel(int tabIndex) {
		m_tabbedPane.setSelectedIndex(tabIndex);
	}

	/**
	 * Method setSelectPanel.
	 * @param tabPanel BasePanel
	 */
	public void setSelectPanel(BasePanel tabPanel) {
		for (int i = 0; i < m_tabbedPane.getTabCount(); i++) {
			BasePanel tabBasePanel = ((BasePanel) m_tabbedPane.getComponent(i));
			if (tabBasePanel.equals(tabPanel)) {
				m_tabbedPane.setSelectedIndex(i);
			}
		}
	}

	/**
	 * Method stateChanged.
	 * @param evt ChangeEvent
	 * @see javax.swing.event.ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent evt) {

		if (evt.getSource() instanceof JTabbedPane) {
			JTabbedPane selectedTab = (JTabbedPane) evt.getSource();
			BasePanel prevBasePanel = null;
			if (selectedTab.isShowing()) {
				// switch current frame
				if (null != currBasePanel) {
					prevBasePanel = currBasePanel;
					if (!currBasePanel.doWindowDeActivated()) {
						setSelectPanel(currentTab);
						return;
					}
				}

				((BasePanel) selectedTab.getComponent(currentTab))
						.setSelected(false);
				currentTab = selectedTab.getSelectedIndex();
				currBasePanel = (BasePanel) selectedTab
						.getComponent(currentTab);
				tabChanged(prevBasePanel, currBasePanel);
				currBasePanel.clearStatusBarMessage();
				currBasePanel.setSelected(true);
				currBasePanel.doWindowActivated();
			}
		}
	}
}
