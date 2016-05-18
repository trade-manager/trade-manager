package org.trade.ui.widget;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

/**
 */
public class ColorRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -651515202481955194L;
	private Border unselectedBorder = null;
	private Border selectedBorder = null;
	private boolean isBordered = true;

	/**
	 * Constructor for ColorRenderer.
	 * 
	 * @param isBordered
	 *            boolean
	 */
	public ColorRenderer(boolean isBordered) {
		this.isBordered = isBordered;
		setOpaque(true); // MUST do this for background to show up.
	}

	/**
	 * Method getTableCellRendererComponent.
	 * 
	 * @param table
	 *            JTable
	 * @param color
	 *            Object
	 * @param isSelected
	 *            boolean
	 * @param hasFocus
	 *            boolean
	 * @param row
	 *            int
	 * @param column
	 *            int
	 * @return Component
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable,
	 *      Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Color newColor = (Color) color;
		setBackground(newColor);
		if (isBordered) {
			if (isSelected) {
				if (selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
				}
				setBorder(selectedBorder);
			} else {
				if (unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
				}
				setBorder(unselectedBorder);
			}
		}

		setToolTipText("RGB value: " + newColor.getRed() + ", " + newColor.getGreen() + ", " + newColor.getBlue());
		return this;
	}
}