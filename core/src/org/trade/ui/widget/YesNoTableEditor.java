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

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;

import org.trade.core.valuetype.YesNo;

/**
 */
public class YesNoTableEditor extends DefaultCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2148534921779125768L;

	public YesNoTableEditor() {
		this(new JCheckBox());
	}

	/**
	 * Constructs a DefaultCellEditor object that uses a check box.
	 * 
	 * 
	 * @param checkBox
	 *            JCheckBox
	 */
	public YesNoTableEditor(final JCheckBox checkBox) {
		super(checkBox);

		checkBox.setHorizontalAlignment(SwingConstants.CENTER);

		editorComponent = checkBox;
		delegate = new EditorDelegate() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6696276657185790230L;

			public void setValue(Object value) {
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

				checkBox.setSelected(selected);
			}

			public Object getCellEditorValue() {
				YesNo yesNo = null;
				if (checkBox.isSelected()) {
					yesNo = YesNo.newInstance(YesNo.YES);
				} else {
					yesNo = YesNo.newInstance(YesNo.NO);
				}
				return yesNo;
			}
		};

		checkBox.addActionListener(delegate);
	}
}
