package org.trade.ui.widget;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.trade.ui.base.BaseButton;

/**
 */
public class ButtonRenderer extends BaseButton implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -651515202481955194L;

	/**
	 * Constructor for ButtonRenderer.
	 * 
	 * @param UICode
	 *            String
	 */
	public ButtonRenderer(String UICode) {
		super(null, UICode);
		this.setOpaque(true);
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
	public Component getTableCellRendererComponent(JTable table, Object color,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if (isSelected) {
			this.setBackground(table.getSelectionBackground());
			this.setEnabled(true);
		} else {
			this.setBackground(table.getBackground());
			this.setEnabled(false);
		}
		setToolTipText("Properties");
		return this;
	}
}