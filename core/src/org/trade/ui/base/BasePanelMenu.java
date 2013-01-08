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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

/**
 */
public class BasePanelMenu extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9043085427010337514L;

	private static JFrame frame = null;

	protected JMenuBar menuBar = new JMenuBar();
	protected JPanel buttonPanel = new JPanel();

	protected JMenu windowMenu = new JMenu();
	protected JMenu fileMenu = new JMenu();
	protected JMenu editMenu = new JMenu();
	protected JMenu helpMenu = new JMenu();
	protected JMenu viewMenu = new JMenu();
	protected JMenu menuItemUtils = new JMenu();

	protected JMenuItem menuItemNew = new JMenuItem();
	protected JMenuItem menuItemOpen = new JMenuItem();
	protected JMenuItem menuItemSave = new JMenuItem();
	protected JMenuItem menuItemSaveAs = new JMenuItem();
	protected JMenuItem menuItemPrint = new JMenuItem();
	protected JMenuItem menuItemPrintPreview = new JMenuItem();
	protected JMenuItem menuItemPrintSetUp = new JMenuItem();
	protected JMenuItem menuItemExit = new JMenuItem();
	protected JMenuItem menuItemUndo = new JMenuItem();
	protected JMenuItem menuItemRedo = new JMenuItem();
	protected JMenuItem menuItemCut = new JMenuItem();
	protected JMenuItem menuItemCopy = new JMenuItem();
	protected JMenuItem menuItemPaste = new JMenuItem();
	protected JMenuItem menuItemFind = new JMenuItem();
	protected JMenuItem menuItemReplace = new JMenuItem();
	protected JMenuItem menuItemGoto = new JMenuItem();
	protected JMenuItem menuItemContents = new JMenuItem();
	protected JMenuItem menuItemAboutHelp = new JMenuItem();

	protected BaseButton saveButton = null;
	protected BaseButton openFileButton = null;
	protected BaseButton helpButton = null;
	protected BaseButton printButton = null;

	protected MessageNotifier m_notifier = new MessageNotifier();

	/**
	 * Constructor for BasePanelMenu.
	 * 
	 * @param p
	 *            BasePanel
	 */
	public BasePanelMenu(BasePanel p) {
		this();

		if (p != null) {
			this.addMessageListener(p);
		}
	}

	public BasePanelMenu() {
		this.setLayout(new BorderLayout());
		JPanel jPanel1 = new JPanel();
		jPanel1.setLayout(new BorderLayout());
		jPanel1.addContainerListener(new java.awt.event.ContainerAdapter() {
			public void componentAdded(ContainerEvent e) {
				jPanelToolPanel_componentChanged();
			}

			public void componentRemoved(ContainerEvent e) {
				jPanelToolPanel_componentChanged();
			}
		});

		FlowLayout flowLayout1 = new FlowLayout();
		flowLayout1.setVgap(0);
		flowLayout1.setHgap(0);
		buttonPanel.setLayout(flowLayout1);

		JPanel jPanelMenuPanel = new JPanel();
		jPanelMenuPanel.setLayout(new BorderLayout());
		JPanel jPanelToolPanel = new JPanel();
		jPanelToolPanel.setLayout(new BorderLayout());

		JToolBar jToolBarMain = new JToolBar();
		jToolBarMain.setLayout(new BorderLayout());

		fileMenu.setText("File");
		fileMenu.setMnemonic('F');
		editMenu.setText("Edit");
		editMenu.setMnemonic('E');
		helpMenu.setText("Help");
		helpMenu.setMnemonic('H');
		viewMenu.setText("View");
		viewMenu.setMnemonic('V');
		windowMenu.setText("Window");
		viewMenu.setMnemonic('A');

		openFileButton = new BaseButton(null, BaseUIPropertyCodes.OPEN_FILE);
		openFileButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(openFileButton.getMethod());
			}
		});
		buttonPanel.add(openFileButton, null);
		saveButton = new BaseButton(null, BaseUIPropertyCodes.SAVE);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(saveButton.getMethod());
			}
		});
		buttonPanel.add(saveButton, null);

		helpButton = new BaseButton(null, BaseUIPropertyCodes.HELP);
		helpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(helpButton.getMethod());
			}
		});
		buttonPanel.add(helpButton, null);
		printButton = new BaseButton(null, BaseUIPropertyCodes.PRINT);
		printButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(printButton.getMethod());
			}
		});
		buttonPanel.add(printButton, null);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		final BaseMenuItem menuItemNew = new BaseMenuItem(null,
				BaseUIPropertyCodes.NEW);
		menuItemNew.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemNew.getMethod());
			}
		});
		final BaseMenuItem menuItemOpen = new BaseMenuItem(null,
				BaseUIPropertyCodes.OPEN_FILE);
		menuItemOpen.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemOpen.getMethod());
			}
		});
		final BaseMenuItem menuItemSave = new BaseMenuItem(null,
				BaseUIPropertyCodes.SAVE);
		menuItemSave.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemSave.getMethod());
			}
		});
		final BaseMenuItem menuItemSaveAs = new BaseMenuItem(null,
				BaseUIPropertyCodes.SAVE_AS);
		menuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemSaveAs.getMethod());
			}
		});
		final BaseMenuItem menuItemPrint = new BaseMenuItem(null,
				BaseUIPropertyCodes.PRINT);
		menuItemPrint.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemPrint.getMethod());
			}
		});
		final BaseMenuItem menuItemPrintPreview = new BaseMenuItem(null,
				BaseUIPropertyCodes.PRINT_PREVIEW);
		menuItemPrintPreview
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						messageEvent(menuItemPrintPreview.getMethod());
					}
				});
		final BaseMenuItem menuItemPrintSetUp = new BaseMenuItem(null,
				BaseUIPropertyCodes.PRINT_OPTIONS);
		menuItemPrintSetUp
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						messageEvent(menuItemPrintSetUp.getMethod());
					}
				});
		final BaseMenuItem menuItemExit = new BaseMenuItem(null,
				BaseUIPropertyCodes.EXIT);
		menuItemExit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemExit.getMethod());
			}
		});
		final BaseMenuItem menuItemUndo = new BaseMenuItem(null,
				BaseUIPropertyCodes.UNDO);
		menuItemUndo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemUndo.getMethod());
			}
		});
		final BaseMenuItem menuItemRedo = new BaseMenuItem(null,
				BaseUIPropertyCodes.REDO);
		menuItemRedo.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemRedo.getMethod());
			}
		});
		final BaseMenuItem menuItemCut = new BaseMenuItem(null,
				BaseUIPropertyCodes.CUT);
		menuItemCut.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemCut.getMethod());
			}
		});
		final BaseMenuItem menuItemCopy = new BaseMenuItem(null,
				BaseUIPropertyCodes.COPY);
		menuItemCopy.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemCopy.getMethod());
			}
		});
		final BaseMenuItem menuItemPaste = new BaseMenuItem(null,
				BaseUIPropertyCodes.PASTE);
		menuItemPaste.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemPaste.getMethod());
			}
		});
		final BaseMenuItem menuItemFind = new BaseMenuItem(null,
				BaseUIPropertyCodes.FIND);
		menuItemFind.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemFind.getMethod());
			}
		});

		final BaseMenuItem menuItemReplace = new BaseMenuItem(null,
				BaseUIPropertyCodes.REPLACE);
		menuItemReplace.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemReplace.getMethod());
			}
		});
		final BaseMenuItem menuItemContents = new BaseMenuItem(null,
				BaseUIPropertyCodes.CONTENTS);
		menuItemContents.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				messageEvent(menuItemContents.getMethod());
			}
		});
		final BaseMenuItem menuItemAboutHelp = new BaseMenuItem(null,
				BaseUIPropertyCodes.ABOUT);
		menuItemAboutHelp
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						messageEvent(menuItemAboutHelp.getMethod());
					}
				});
		menuItemUtils.setText("Utils");
		menuItemUtils.setEnabled(false);
		fileMenu.add(menuItemNew);
		fileMenu.add(menuItemOpen);
		fileMenu.add(menuItemSave);
		fileMenu.add(menuItemSaveAs);
		fileMenu.addSeparator();
		fileMenu.add(menuItemPrint);
		fileMenu.add(menuItemPrintPreview);
		fileMenu.add(menuItemPrintSetUp);
		fileMenu.addSeparator();
		fileMenu.add(menuItemExit);
		editMenu.add(menuItemUndo);
		editMenu.add(menuItemRedo);
		editMenu.addSeparator();
		editMenu.add(menuItemCut);
		editMenu.add(menuItemCopy);
		editMenu.add(menuItemPaste);
		editMenu.addSeparator();
		editMenu.add(menuItemFind);
		editMenu.add(menuItemReplace);
		editMenu.add(menuItemGoto);
		helpMenu.add(menuItemContents);
		helpMenu.add(menuItemAboutHelp);
		this.add(jPanelMenuPanel, BorderLayout.NORTH);
		this.add(jPanelToolPanel, BorderLayout.SOUTH);
		jPanelToolPanel.add(jPanel1, BorderLayout.WEST);
		jPanel1.add(jToolBarMain, BorderLayout.CENTER);
		jToolBarMain.add(buttonPanel, BorderLayout.NORTH);
		jPanelMenuPanel.add(menuBar, BorderLayout.NORTH);

	}

	/**
	 * getFrame ()
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return JFrame * @exception * @see
	 */
	private JFrame getFrame() {
		if (frame == null) {
			Component parent = this;

			while ((parent != null) && !(parent instanceof JFrame)) {
				parent = parent.getParent();
			}

			frame = (JFrame) parent;
		}

		return frame;
	}

	/**
	 * addMessageListener (MessageListener listener)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param listener
	 *            MessageListener
	 * @exception * @see
	 */
	public void addMessageListener(MessageListener listener) {
		m_notifier.add(listener);
	}

	/**
	 * removeMessageListener (MessageListener listener)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param listener
	 *            MessageListener
	 * @exception * @see
	 */
	public void removeMessageListener(MessageListener listener) {
		m_notifier.remove(listener);
	}

	/**
	 * getWindowsOpenMenu (MessageListener listener)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @return JMenuItem * @exception * @see
	 */
	public JMenu getWindowsOpenMenu() {
		return windowMenu;
	}

	/**
	 * Method getButtonPanel.
	 * 
	 * @return JPanel
	 */
	public JPanel getButtonPanel() {
		return buttonPanel;
	}

	/**
	 * jPanelToolPanel_componentChanged(ContainerEvent e)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @exception * @see
	 */
	private void jPanelToolPanel_componentChanged() {
		if (getFrame() != null) {
			getFrame().validate();
		}
	}

	/**
	 * messageEvent(String selection)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param selection
	 *            String
	 * @exception * @see
	 */
	public void messageEvent(String selection) {
		m_notifier.notifyEvent(new MessageEvent(selection),
				new Vector<Object>());
	}

	/**
	 * messageEvent(String selection)
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param enable
	 *            boolean
	 * @exception * @see
	 */
	public void enableSave(boolean enable) {
		menuItemSave.setEnabled(enable);
		menuItemSaveAs.setEnabled(enable);
		saveButton.setEnabled(enable);
	}
}
