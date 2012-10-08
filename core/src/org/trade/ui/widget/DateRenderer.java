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

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.trade.core.valuetype.Date;

/**
 */
public class DateRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -7703222115247216081L;
	private SimpleDateFormat dateFormat = null;

	/**
	 * Constructor for DateRenderer.
	 * @param mask String
	 */
	public DateRenderer(String mask) {
		super();

		setHorizontalAlignment(SwingConstants.CENTER);
		dateFormat = new SimpleDateFormat(mask, Locale.getDefault());
		dateFormat.setLenient(false);
	}

	/**
	 * Method setValue.
	 * @param value Object
	 */
	protected void setValue(Object value) {

		if (value == null) {
			setText("");
		} else {
			if (value instanceof java.util.Date) {
				setText(dateFormat.format(value));
			} else if (value instanceof Date) {
				java.util.Date date = ((Date) value).getDate();
				if (null == date) {
					setText(value.toString());
				} else {
					setText(dateFormat.format(date));
				}
			} else {
				setText(value.toString());
			}
		}
	}
}
