/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
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
package org.trade.ui;

import java.awt.Frame;

import javax.swing.JOptionPane;

import org.trade.core.properties.ConfigProperties;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.TabbedAppPanel;

/**
 */
public class MainControllerPanel extends TabbedAppPanel {

	private static final long serialVersionUID = -7717664255656430982L;

	public final static String PROPERTIES_PROPERTY_FILE = "core.properties";

	public static String title = null;
	public static String version = null;
	public static String date = null;
	protected static MainControllerPanel m_instance = null;

	/**
	 * The main application controller which interacts between the view and the
	 * applications underlying models. This controller also listens to events
	 * from the broker model.
	 * <p>
	 * 
	 * @param frame
	 *            the main application Frame.
	 * 
	 */

	public MainControllerPanel(Frame frame) {
		super(frame);
		try {
			setMenu(new MainPanelMenu(this));
			/* This is always true as main panel needs to receive all events */
			setSelected(true);
			title = ConfigProperties.getPropAsString("component.name.base");
			version = ConfigProperties.getPropAsString("component.name.version");
			date = ConfigProperties.getPropAsString("component.name.date");
		} catch (Exception e) {
			this.setErrorMessage("Error During Initialization.", e.getMessage(), e);
		}
	}

	/**
	 * This method is fired from the main menu. It displays the application
	 * version.
	 * 
	 */
	public void doAbout() {
		try {
			StringBuffer message = new StringBuffer();
			message.append("Product version: ");
			message.append(MainControllerPanel.version);
			message.append("\nBuild Label:     ");
			message.append(MainControllerPanel.title);
			message.append("\nBuild Time:      ");
			message.append(MainControllerPanel.date);
			JOptionPane.showMessageDialog(this, message, "About Help", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			this.setErrorMessage("Could not load about help.", ex.getMessage(), ex);
		}
	}

	/**
	 * This method is fired after the tab has been created and placed in the tab
	 * controller.
	 * 
	 */

	public void doWindowOpen() {

	}

	/**
	 * This method is fired when the tab closes.
	 * 
	 */

	public void doWindowClose() {
		doExit();
	}

	/**
	 * This method is fired from the Main menu and will allow you to setup the
	 * printer setting.
	 */

	public void doPrintSetup() {

	}

	/**
	 * This method is fired from the Main menu and will allow you to preview a
	 * print of the current tab.
	 */
	public void doPrintPreview() {

	}

	/**
	 * This method is fired from the Main menu and will allow you to print the
	 * current tab.
	 */
	public void doPrint() {

	}

	/**
	 * This method is fired when a different tab is selected.
	 * 
	 * @param currBasePanel
	 *            BasePanel
	 * @param newBasePanel
	 *            BasePanel
	 */
	public void tabChanged(BasePanel currBasePanel, BasePanel newBasePanel) {
	}
}
