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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import org.trade.core.util.TradingCalendar;
import org.trade.core.valuetype.Date;
import org.trade.core.valuetype.Decimal;
import org.trade.core.valuetype.Decode;
import org.trade.core.valuetype.Money;
import org.trade.core.valuetype.Percent;
import org.trade.core.valuetype.Quantity;
import org.trade.core.valuetype.ValueTypeException;
import org.trade.core.valuetype.YesNo;
import org.trade.ui.widget.ColorEditor;
import org.trade.ui.widget.ColorRenderer;
import org.trade.ui.widget.DateEditor;
import org.trade.ui.widget.DateField;
import org.trade.ui.widget.DateRenderer;
import org.trade.ui.widget.DecimalEditor;
import org.trade.ui.widget.DecimalField;
import org.trade.ui.widget.DecimalRenderer;
import org.trade.ui.widget.DecodeTableRenderer;
import org.trade.ui.widget.IntegerEditor;
import org.trade.ui.widget.IntegerField;
import org.trade.ui.widget.IntegerRenderer;
import org.trade.ui.widget.MoneyEditor;
import org.trade.ui.widget.MoneyField;
import org.trade.ui.widget.MoneyRenderer;
import org.trade.ui.widget.PercentEditor;
import org.trade.ui.widget.PercentField;
import org.trade.ui.widget.PercentRenderer;
import org.trade.ui.widget.QuantityEditor;
import org.trade.ui.widget.QuantityField;
import org.trade.ui.widget.QuantityRenderer;
import org.trade.ui.widget.StringEditor;
import org.trade.ui.widget.StringField;
import org.trade.ui.widget.StringRenderer;
import org.trade.ui.widget.YesNoTableEditor;
import org.trade.ui.widget.YesNoTableRenderer;

/**
 * 
 * @version $Id: ConfigProperties.java 1.0 2000/08/08 00:36:40Z Simon.Allen dev
 *          $
 * @author Simon Allen
 */
public class Table extends JTable implements MouseListener, ActionListener {

	private static final long serialVersionUID = -6753202830151175785L;

	private static final String DATEFORMAT = "MM/dd/yyyy";
	private JPopupMenu popup = null;
	private boolean popupMenu = true;
	private String m_pngDefaultDir = null;

	/**
	 * Constructor for Table.
	 * 
	 * @param tableModel
	 *            TableModel
	 * @throws ValueTypeException
	 */
	public Table(TableModel tableModel) throws ValueTypeException {

		super(tableModel);

		this.setAutoCreateRowSorter(true);
		int columns = this.getTableHeader().getColumnModel().getColumnCount();

		for (int i = 0; i < columns; i++) {
			TableColumn thc = this.getTableHeader().getColumnModel()
					.getColumn(i);
			thc.setMinWidth((thc.getHeaderValue().toString().length() * 6) + 10);
		}

		DateRenderer rDate = new DateRenderer(DATEFORMAT);
		rDate.setHorizontalAlignment(SwingConstants.CENTER);
		DateEditor eDate = new DateEditor(new DateField(DATEFORMAT), new Date(
				TradingCalendar.getDateTimeNowMarketTimeZone()), DATEFORMAT,
				Calendar.DAY_OF_MONTH);
		MoneyRenderer rMoney = new MoneyRenderer();
		MoneyEditor eMoney = new MoneyEditor(new MoneyField());
		DecimalRenderer rDecimal = new DecimalRenderer();
		DecimalEditor eDecimal = new DecimalEditor(new DecimalField(2));
		PercentRenderer rPercent = new PercentRenderer();
		PercentEditor ePercent = new PercentEditor(new PercentField());
		StringEditor eString = new StringEditor(new StringField());
		StringRenderer rString = new StringRenderer();
		IntegerRenderer rInteger = new IntegerRenderer();
		IntegerEditor eInteger = new IntegerEditor(new IntegerField());
		QuantityRenderer rQuantity = new QuantityRenderer();
		QuantityEditor eQuantity = new QuantityEditor(new QuantityField());
		DecodeTableRenderer decodeRenderer = new DecodeTableRenderer();

		YesNoTableEditor eYesNoEditor = new YesNoTableEditor();
		YesNoTableRenderer rYesNoRenderer = new YesNoTableRenderer();

		this.setDefaultRenderer(Color.class, new ColorRenderer(true));
		this.setDefaultEditor(Color.class, new ColorEditor());
		this.setDefaultRenderer(Date.class, rDate);
		this.setDefaultEditor(Date.class, eDate);
		this.setDefaultRenderer(Money.class, rMoney);
		this.setDefaultEditor(Money.class, eMoney);
		this.setDefaultRenderer(Decimal.class, rDecimal);
		this.setDefaultEditor(Decimal.class, eDecimal);
		this.setDefaultRenderer(Percent.class, rPercent);
		this.setDefaultEditor(Percent.class, ePercent);
		this.setDefaultRenderer(Integer.class, rInteger);
		this.setDefaultEditor(Integer.class, eInteger);
		this.setDefaultRenderer(Quantity.class, rQuantity);
		this.setDefaultEditor(Quantity.class, eQuantity);
		this.setDefaultRenderer(String.class, rString);
		this.setDefaultEditor(String.class, eString);
		this.setDefaultRenderer(Decode.class, decodeRenderer);
		this.setDefaultRenderer(YesNo.class, rYesNoRenderer);
		this.setDefaultEditor(YesNo.class, eYesNoEditor);

		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.getTableHeader().setUpdateTableInRealTime(false);
		this.setRowSelectionAllowed(true);
		this.setColumnSelectionAllowed(false);

		this.addMouseListener(this);
	}

	/**
	 * Enable popup menus to add/delete rows.
	 * 
	 * 
	 * @param popupMenu
	 *            true or false.
	 * 
	 */

	public void enablePopupMenu(boolean popupMenu) {
		this.popupMenu = popupMenu;
	}

	/**
	 * mousePressed() -
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			createPopup(evt.getPoint());
		}
	}

	/**
	 * mouseReleased() -
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseReleased(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			createPopup(evt.getPoint());
		}
	}

	/**
	 * mouseClicked() -
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseClicked(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			createPopup(evt.getPoint());
		}
	}

	/**
	 * mouseEntered() -
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseEntered(MouseEvent evt) {
	}

	/**
	 * mouseExited() -
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseExited(MouseEvent evt) {
	}

	/**
	 * Method createDefaultTableHeader.
	 * 
	 * @return JTableHeader
	 */
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			private static final long serialVersionUID = -2667248411137081167L;

			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				if (index > -1) {
					int realIndex = columnModel.getColumn(index)
							.getModelIndex();
					return ((TableModel) this.getTable().getModel())
							.getColumnHeaderToolTip(realIndex);
				}
				return null;
			}
		};
	}

	/**
	 * createPopup() -
	 * 
	 * @param point
	 *            Point
	 * @exception * @see
	 */
	public void createPopup(Point point) {

		popup = new JPopupMenu();
		if (popupMenu) {
			JMenuItem addMenuItem = popup.add(new JMenuItem("Add"));
			addMenuItem.addActionListener(this);

			JMenuItem deleteMenuItem = popup.add(new JMenuItem("Delete"));
			deleteMenuItem.addActionListener(this);

			JMenuItem clearAllMenuItem = popup.add(new JMenuItem("ClearAll"));
			clearAllMenuItem.addActionListener(this);
		}
		JMenuItem printMenuItem = popup.add(new JMenuItem("Print"));
		printMenuItem.addActionListener(this);

		JMenuItem saveAsMenuItem = popup.add(new JMenuItem("Save As"));
		saveAsMenuItem.addActionListener(this);

		// Try to make the popup lightweight
		point = getSuitableLocation(point, popup.getPreferredSize(), this);

		popup.show(this, point.x, point.y);

	}

	/**
	 * actionPerformed() -
	 * 
	 * @param evt
	 *            ActionEvent
	 * @exception * @see
	 */
	public void actionPerformed(ActionEvent evt) {
		JMenuItem mi = (JMenuItem) evt.getSource();

		if (mi.getActionCommand().equals("Delete")) {
			delete();
		} else if (mi.getActionCommand().equals("Add")) {
			add();
		} else if (mi.getActionCommand().equals("ClearAll")) {
			clearAll();
		} else if (mi.getActionCommand().equals("Print")) {
			try {
				print();
			} catch (PrinterException e) {
				// Do nothing ...
			}
		} else if (mi.getActionCommand().equals("Save As")) {
			try {
				saveAs();
			} catch (IOException e) {
				// Do nothing ...
			}
		}
	}

	/**
	 * getSuitableLocation() -
	 * 
	 * @param point
	 *            Point
	 * @param d
	 *            Dimension
	 * @param c
	 *            Component
	 * @return Point * @exception * @see
	 */
	protected Point getSuitableLocation(Point point, Dimension d, Component c) {
		// First locate the parent JFrame or JDialog
		Component topLevel = getComponentContainer(c);

		if (topLevel != null) {
			int newX; // New proposed x co-ordinate
			int newY; // New proposed y co-ordinate
			// We have a top-level parent
			Rectangle parentBounds = topLevel.getBounds(); // Parent bounds,
			// screen-relative

			// Get proposed location, relative to the screen
			SwingUtilities.convertPointToScreen(point, c);

			if ((point.x + d.width) > (parentBounds.x + parentBounds.width)) {
				// Popup overhangs to the right
				newX = (parentBounds.x + parentBounds.width) - d.width;
			} else {
				newX = point.x;
			}

			if ((point.y + d.height) > (parentBounds.y + parentBounds.height)) {
				// Popup ends below frame
				newY = (parentBounds.y + parentBounds.height) - d.height;
			} else {
				newY = point.y;
			}

			// Change location only if necessary AND if
			// we can make the popup fit inside the frame
			if ((newX >= 0) && (newY >= 0)) {
				point.x = newX;
				point.y = newY;
			}

			// Convert back to relative co-ordinates
			SwingUtilities.convertPointFromScreen(point, c);
		}

		return point;
	}

	/**
	 * getComponentContainer() -
	 * 
	 * @param c
	 *            Component
	 * @return Component * @exception * @see
	 */
	private Component getComponentContainer(Component c) {
		Component topLevel = c;

		while ((topLevel != null) && !(topLevel instanceof JFrame)
				&& !(topLevel instanceof JDialog)) {
			topLevel = topLevel.getParent();
		}

		return topLevel;
	}

	/**
	 * delete a row() -
	 * 
	 * @exception * @see
	 */
	public void delete() {
		if (this.getSelectedRow() > -1) {
			((TableModel) this.getModel()).deleteRow(this
					.convertRowIndexToModel((this.getSelectedRow())));
		}
	}

	/**
	 * clear all rows() -
	 * 
	 * @exception * @see
	 */
	public void clearAll() {
		int rowCount = this.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			((TableModel) this.getModel()).deleteRow(0);
		}
	}

	/**
	 * print all rows() -
	 * 
	 * @exception * @see
	 */

	/**
	 * add a row() -
	 * 
	 * @param <B>
	 *            Component c </B> Returns the container of the component
	 * @return Component
	 * @exception
	 * @see
	 */
	public void add() {
		((TableModel) this.getModel()).addRow();
	}

	/**
	 * saveAs - Save the image as a PNG
	 * 
	 * @param <B>
	 *            Component c </B> Returns the container of the component
	 * @return Component
	 * @throws IOException
	 * @exception
	 * @see
	 */
	private void saveAs() throws IOException {

		int width = Math.max(this.getWidth(), this.getTableHeader().getWidth());
		int height = this.getHeight() + this.getTableHeader().getHeight();

		BufferedImage bi = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bi.createGraphics();
		this.getTableHeader().paint(g2);
		g2.translate(0, this.getTableHeader().getHeight());
		this.paint(g2);
		g2.dispose();
		ImageIO.write(bi, "png", new File(openFileChooser()));

	}

	/**
	 * Method openFileChooser.
	 * 
	 * @return String
	 */
	private String openFileChooser() {

		String fileName = null;
		ExampleFileFilter filter = new ExampleFileFilter(
				new String[] { "png" }, "PNG Files");
		// Start in the curr dir

		if (null == m_pngDefaultDir) {
			m_pngDefaultDir = System.getProperty("user.dir");
		}

		JFileChooser filer1 = new JFileChooser(m_pngDefaultDir);
		ExampleFileChooser fileView = new ExampleFileChooser();
		filer1.setFileView(fileView);
		filer1.addChoosableFileFilter(filter);
		filer1.setFileFilter(filter);
		filer1.setAccessory(new FilePreviewer(filer1));

		int returnVal = 0;

		returnVal = filer1.showSaveDialog(this);

		// Upon return, getFile() will be null if user cancelled the dialog.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Non-null file property after return implies user
			// selected a file to open.
			// Call openFile to attempt to load the text from file into TextArea
			if (filer1.getSelectedFile().exists()) {

				int result = JOptionPane.showConfirmDialog(
						getComponentContainer(this),
						"File Exists. Do you want to over write ?", "Warning",
						JOptionPane.YES_NO_OPTION);
				if (result == JOptionPane.YES_OPTION) {
					fileName = filer1.getSelectedFile().getPath();

				} else {
					// cancel
					return null;
				}
			} else {
				fileName = filer1.getSelectedFile().getPath();
			}
			if (!fileName.toUpperCase().endsWith(".PNG")) {
				return fileName + ".png";
			}
			return fileName;
		}
		return null;
	}
}
