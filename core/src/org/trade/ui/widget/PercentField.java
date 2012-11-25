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

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.trade.core.valuetype.Percent;

/**
 * 
 * @version $Id: PercentField.java,v 1.2 2001/12/28 21:14:55 simon Exp $
 * @author Simon Allen
 */
public class PercentField extends JFormattedTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3445299380677561974L;

	public PercentField() {
		super();
		NumberFormat displayFormat = NumberFormat.getPercentInstance();
		displayFormat.setMinimumFractionDigits(2);
		NumberFormat editFormat = NumberFormat.getNumberInstance();
		editFormat.setMinimumFractionDigits(2);
		this.setFormatterFactory(new DefaultFormatterFactory(
				new NumberFormatter(displayFormat), new NumberFormatter(
						displayFormat), new NumberFormatter(editFormat)));
		this.setHorizontalAlignment(SwingConstants.RIGHT);
		this.setValue(new Double(0));
		this.setColumns(10);

	}

	/**
	 * Method getPercent.
	 * 
	 * @return Percent
	 */
	public Percent getPercent() {
		try {
			this.setValue(new Double(this.getText()));
		} catch (Exception ex) {
			// Do nothing will return the current value.
		}
		return new Percent(((Number) this.getValue()).doubleValue());
	}

	/**
	 * Method setPercent.
	 * 
	 * @param number
	 *            Percent
	 */
	public void setPercent(Percent number) {
		this.setValue(number.doubleValue());
	}
}
