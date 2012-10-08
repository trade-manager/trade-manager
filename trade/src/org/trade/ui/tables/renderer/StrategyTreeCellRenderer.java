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
package org.trade.ui.tables.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.trade.persistent.dao.Rule;
import org.trade.ui.models.StrategyTreeModel;

/**
 */
public class StrategyTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 7664391812385841364L;
	private Color backgroundSelectionColor = null;

	public StrategyTreeCellRenderer() {
		super();
		backgroundSelectionColor = this.getBackgroundSelectionColor();
	}

	/**
	 * Method getTreeCellRendererComponent.
	 * @param tree JTree
	 * @param value Object
	 * @param selected boolean
	 * @param expanded boolean
	 * @param leaf boolean
	 * @param row int
	 * @param hasFocus boolean
	 * @return Component
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		Object node = ((StrategyTreeModel) tree.getModel()).getNode(value);

		Component comp = super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
		if (selected) {
			this.setBackgroundSelectionColor(backgroundSelectionColor);
		}
		if (node != null) {
			if ((node instanceof Rule) /* leaf */) {
				this.setToolTipText("Select to open rule.");
				if (((Rule) node).isDirty()) {
					this.setBackgroundSelectionColor(Color.RED);
				}
			} else if (expanded) {

			} else {

			}
		}
		return comp;
	}
}
