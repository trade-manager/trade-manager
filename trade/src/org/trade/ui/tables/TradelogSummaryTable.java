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
package org.trade.ui.tables;

import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import org.trade.core.valuetype.ValueTypeException;

import org.trade.ui.base.Table;
import org.trade.ui.base.TableModel;

/**
 */
public class TradelogSummaryTable extends Table {

	private static final long serialVersionUID = 1132297931453070904L;
	private String[] columnToolTips = { null, "% wins vs loss",
			"Simple sharpe ratio (sum $wins/#wins)/(sum $loss/#loss)",
			null, null, null, null, null, null, null, null, null, };

	/**
	 * Constructor for TradelogSummaryTable.
	 * @param model TableModel
	 * @throws ValueTypeException
	 */
	public TradelogSummaryTable(TableModel model) throws ValueTypeException {
		super(model);
		this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.enablePopupMenu(false);
	}

	// Implement table header tool tips.
	/**
	 * Method createDefaultTableHeader.
	 * @return JTableHeader
	 */
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
			private static final long serialVersionUID = 4317932796467789524L;

			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int index = columnModel.getColumnIndexAtX(p.x);
				if (index > -1) {
					int realIndex = columnModel.getColumn(index)
							.getModelIndex();
					return columnToolTips[realIndex];
				}
				return null;
			}
		};
	}
}
