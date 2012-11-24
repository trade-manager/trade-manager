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
package org.trade.ui.widget;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.trade.core.valuetype.YesNo;

/**
 */
public class YesNoTableRenderer extends JCheckBox implements TableCellRenderer,
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8331082128500817101L;

	protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	// We need a place to store the color the JLabel should be returned
	// to after its foreground and background colors have been set
	// to the selection background color.
	// These ivars will be made protected when their names are finalized.
	private Color unselectedForeground;

	private Color unselectedBackground;

	/**
	 * Creates a default table cell renderer.
	 */
	public YesNoTableRenderer() {
		super();

		setHorizontalAlignment(SwingConstants.CENTER);
		setOpaque(true);
		setBorder(noFocusBorder);
	}

	/**
	 * Overrides <code>JComponent.setForeground</code> to assign the
	 * unselected-foreground color to the specified color.
	 * 
	 * @param c
	 *            set the foreground color to this value
	 */
	public void setForeground(Color c) {
		super.setForeground(c);

		unselectedForeground = c;
	}

	/**
	 * Overrides <code>JComponent.setForeground</code> to assign the
	 * unselected-background color to the specified color.
	 * 
	 * @param c
	 *            set the background color to this value
	 */
	public void setBackground(Color c) {
		super.setBackground(c);

		unselectedBackground = c;
	}

	/**
	 * Notification from the <code>UIManager</code> that the look and feel [L&F]
	 * has changed. Replaces the current UI object with the latest version from
	 * the <code>UIManager</code>.
	 * 
	 * 
	 * @see JComponent#updateUI
	 */
	public void updateUI() {
		super.updateUI();
		setForeground(null);
		setBackground(null);
	}

	// implements javax.swing.table.TableCellRenderer
	/**
	 * 
	 * Returns the default table cell renderer.
	 * 
	 * @param table
	 *            the <code>JTable</code>
	 * @param value
	 *            the value to assign to the cell at <code>[row, column]</code>
	 * @param isSelected
	 *            true if cell is selected
	 * 
	 * @param row
	 *            the row of the cell to render
	 * @param column
	 *            the column of the cell to render
	 * 
	 * @param hasFocus
	 *            boolean
	 * @return the default table cell renderer * @see
	 *         javax.swing.table.TableCellRenderer
	 *         #getTableCellRendererComponent(JTable, Object, boolean, boolean,
	 *         int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			super.setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		} else {
			super.setForeground((unselectedForeground != null) ? unselectedForeground
					: table.getForeground());
			super.setBackground((unselectedBackground != null) ? unselectedBackground
					: table.getBackground());
		}

		setFont(table.getFont());

		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));

			if (table.isCellEditable(row, column)) {
				super.setForeground(UIManager
						.getColor("Table.focusCellForeground"));
				super.setBackground(UIManager
						.getColor("Table.focusCellBackground"));
			}
		} else {
			setBorder(noFocusBorder);
		}

		setValue(value);

		// ---- begin optimization to avoid painting background ----
		Color back = getBackground();
		boolean colorMatch = (back != null)
				&& (back.equals(table.getBackground())) && table.isOpaque();

		setOpaque(!colorMatch);

		// ---- end optimization to aviod painting background ----
		return this;
	}

	/*
	 * The following methods are overridden as a performance measure to to prune
	 * code-paths are often called in the case of renders but which we know are
	 * unnecessary. Great care should be taken when writing your own renderer to
	 * weigh the benefits and drawbacks of overriding methods like these.
	 */

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note</a> for more information.
	 */
	public void validate() {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note</a> for more information.
	 */
	public void revalidate() {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note</a> for more information.
	 * 
	 * @param tm
	 *            long
	 * @param x
	 *            int
	 * @param y
	 *            int
	 * @param width
	 *            int
	 * @param height
	 *            int
	 */
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note</a> for more information.
	 * 
	 * @param r
	 *            Rectangle
	 */
	public void repaint(Rectangle r) {
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note</a> for more information.
	 * 
	 * @param propertyName
	 *            String
	 * @param oldValue
	 *            Object
	 * @param newValue
	 *            Object
	 */
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		// Strings get interned...
		if (propertyName.equals("text")) {
			super.firePropertyChange(propertyName, oldValue, newValue);
		}
	}

	/**
	 * Overridden for performance reasons. See the <a
	 * href="#override">Implementation Note</a> for more information.
	 * 
	 * @param propertyName
	 *            String
	 * @param oldValue
	 *            boolean
	 * @param newValue
	 *            boolean
	 */
	public void firePropertyChange(String propertyName, boolean oldValue,
			boolean newValue) {
	}

	/**
	 * Sets the string for the cell being rendered to <code>value</code>.
	 * 
	 * @param value
	 *            the string value for this cell; if value is <code>null</code>
	 *            it sets the text value to an empty string
	 * 
	 * 
	 * @see JLabel#setText
	 */
	protected void setValue(Object value) {
		boolean selected = false;

		if (value instanceof Boolean) {
			selected = ((Boolean) value).booleanValue();
		} else if (value instanceof YesNo) {
			if (((YesNo) value).isYes()) {
				selected = true;
			}
		} else if (value instanceof String) {
			selected = value.equals("true");
		}

		setSelected(selected);
	}

}
