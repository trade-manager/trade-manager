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
package org.trade.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;

import org.trade.ui.base.BaseButton;
import org.trade.ui.base.BaseMenuItem;
import org.trade.ui.base.BasePanel;
import org.trade.ui.base.BasePanelMenu;
import org.trade.ui.base.BaseUIPropertyCodes;

/**
 */
public class TradeMainPanelMenu extends BasePanelMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2716722655140661891L;

	private final BaseMenuItem searchMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.SEARCH);
	private final BaseButton searchButton = new BaseButton(null,
			BaseUIPropertyCodes.SEARCH);
	private final BaseMenuItem refreshMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.REFRESH);
	private final BaseButton refreshButton = new BaseButton(null,
			BaseUIPropertyCodes.REFRESH);
	private final BaseButton deleteButton = new BaseButton(null,
			BaseUIPropertyCodes.DELETE);
	private final BaseMenuItem deleteMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.DELETE);
	private final BaseMenuItem brokerDataMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.DATA);
	private final BaseButton brokerDataButton = new BaseButton(null,
			BaseUIPropertyCodes.DATA);
	private final BaseButton runStrategyButton = new BaseButton(null,
			BaseUIPropertyCodes.RUN);
	private final BaseMenuItem runStrategyMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.RUN);
	private final BaseButton testStrategyButton = new BaseButton(null,
			BaseUIPropertyCodes.TEST);
	private final BaseMenuItem testStrategyMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.TEST);
	private final BaseButton cancelButton = new BaseButton(null,
			BaseUIPropertyCodes.CANCEL);
	private final BaseMenuItem cancelMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.CANCEL);
	private final BaseMenuItem propertiesMenu = new BaseMenuItem(null,
			BaseUIPropertyCodes.PROPERTIES);
	private final BaseMenuItem connect = new BaseMenuItem(null,
			BaseUIPropertyCodes.CONNECT);
	private final BaseMenuItem disconnect = new BaseMenuItem(null,
			BaseUIPropertyCodes.DISCONNECT);

	/**
	 * Constructor for TradeMainPanelMenu.
	 * @param basePanel BasePanel
	 */
	public TradeMainPanelMenu(BasePanel basePanel) {
		super(basePanel);

		cancelButton.setToolTipText("Cancel strategies & data");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(cancelButton.getMethod());
			}
		});
		cancelMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(cancelMenu.getMethod());
			}
		});
		runStrategyButton.setToolTipText("Run Strategy");
		runStrategyButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						messageEvent(runStrategyButton.getMethod());
					}
				});
		runStrategyMenu.setText("Run Strategy");
		runStrategyMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(runStrategyMenu.getMethod());
			}
		});
		testStrategyButton.setToolTipText("Test Strategy");
		testStrategyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(testStrategyButton.getMethod());
			}
		});
		testStrategyMenu.setText("Test Strategy");
		testStrategyMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(testStrategyMenu.getMethod());
			}
		});
		brokerDataButton.setToolTipText("Get Chart Data");
		brokerDataButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(brokerDataButton.getMethod());
			}
		});
		brokerDataMenu.setText("Get Chart Data");
		brokerDataMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(brokerDataMenu.getMethod());
			}
		});
		searchMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(searchMenu.getMethod());
			}
		});
		searchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(searchButton.getMethod());
			}
		});
		
		refreshMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(refreshMenu.getMethod());
			}
		});
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(refreshButton.getMethod());
			}
		});
		deleteMenu.setText("Delete All Orders");
		deleteMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(deleteMenu.getMethod());
			}
		});
		deleteButton.setToolTipText("Delete all Orders");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(deleteButton.getMethod());
			}
		});
		propertiesMenu.setText("Contract Details");
		propertiesMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(propertiesMenu.getMethod());
			}
		});
		connect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(connect.getMethod());
			}
		});
		disconnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(disconnect.getMethod());
			}
		});

		final BaseMenuItem close = new BaseMenuItem(null,
				BaseUIPropertyCodes.CLOSE);
		close.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(close.getMethod());
			}
		});

		final BaseMenuItem closeAll = new BaseMenuItem(null,
				BaseUIPropertyCodes.CLOSE_ALL);
		closeAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(closeAll.getMethod());
			}
		});

		final BaseMenuItem cascade = new BaseMenuItem(null,
				BaseUIPropertyCodes.CASCADE);
		cascade.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(cascade.getMethod());
			}
		});

		final BaseMenuItem cascadeAll = new BaseMenuItem(null,
				BaseUIPropertyCodes.CASCADE_ALL);
		cascadeAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(cascadeAll.getMethod());
			}
		});

		final BaseMenuItem tileAll = new BaseMenuItem(null,
				BaseUIPropertyCodes.TILE_ALL);
		tileAll.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(tileAll.getMethod());
			}
		});
		JMenu actionMenu = new JMenu("Action");
		menuBar.add(actionMenu, 2);
		this.editMenu.setVisible(false);
		fileMenu.insertSeparator(4);
		fileMenu.add(connect, 5);
		fileMenu.add(disconnect, 6);

		getButtonPanel().add(brokerDataButton, 3);
		getButtonPanel().add(testStrategyButton, 4);
		getButtonPanel().add(runStrategyButton, 5);
		getButtonPanel().add(cancelButton, 6);
		getButtonPanel().add(searchButton, 7);
		getButtonPanel().add(refreshButton, 8);
		getButtonPanel().add(deleteButton, 9);

		actionMenu.add(brokerDataMenu, 0);
		actionMenu.add(testStrategyMenu, 1);
		actionMenu.add(runStrategyMenu, 2);
		actionMenu.add(cancelMenu, 3);
		actionMenu.add(searchMenu, 4);
		actionMenu.add(refreshMenu, 5);
		actionMenu.add(deleteMenu, 6);
		actionMenu.add(propertiesMenu, 7);

		// windowMenu.add(close, 0);
		// windowMenu.add(closeAll, 1);
		// windowMenu.add(cascade, 2);
		// windowMenu.add(cascadeAll, 3);
		// windowMenu.add(tileAll, 4);
		// windowMenu.insertSeparator(5);

	}

	/**
	 * Method setEnabledBrokerData.
	 * @param enabled boolean
	 */
	public void setEnabledBrokerData(boolean enabled) {
		brokerDataMenu.setEnabled(enabled);
		brokerDataButton.setEnabled(enabled);
	}

	/**
	 * Method setEnabledRunStrategy.
	 * @param enabled boolean
	 */
	public void setEnabledRunStrategy(boolean enabled) {
		runStrategyMenu.setEnabled(enabled);
		runStrategyButton.setEnabled(enabled);
	}

	/**
	 * Method setEnabledTestStrategy.
	 * @param enabled boolean
	 */
	public void setEnabledTestStrategy(boolean enabled) {
		testStrategyMenu.setEnabled(enabled);
		testStrategyButton.setEnabled(enabled);
	}

	/**
	 * Method setEnabledSearchDeleteRefreshSave.
	 * @param enabled boolean
	 */
	public void setEnabledSearchDeleteRefreshSave(boolean enabled) {
		refreshMenu.setEnabled(enabled);
		refreshButton.setEnabled(enabled);
		searchMenu.setEnabled(enabled);
		searchButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
		deleteMenu.setEnabled(enabled);
		this.enableSave(enabled);
	}
	/**
	 * Method setEnabledDeleteSave.
	 * @param enabled boolean
	 */
	public void setEnabledDeleteSave(boolean enabled) {
		deleteButton.setEnabled(enabled);
		deleteMenu.setEnabled(enabled);
		this.enableSave(enabled);
	}
}
