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

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;

/**
 * 
 * @version $Id: StringField.java,v 1.2 2001/12/28 21:14:55 simon Exp $
 * @author Simon Allen
 */
public class StringField extends JFormattedTextField implements FocusListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3791332898190722115L;

	/**
	 * Constructor for StringField.
	 * @param mask MaskFormatter
	 * @param validCharacters String
	 * @param placeHolder String
	 */
	public StringField(MaskFormatter mask, String validCharacters,
			String placeHolder) {
		super(mask);
		if (null != validCharacters)
			((MaskFormatter) this.getFormatter())
					.setValidCharacters(validCharacters);
		if (null != placeHolder)
			((MaskFormatter) this.getFormatter())
					.setPlaceholderCharacter(placeHolder.charAt(0));
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));
		this.addFocusListener(this);
	}

	public StringField() {
		super();
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));
	}

	/**
	 * Constructor for StringField.
	 * @param columns int
	 */
	public StringField(int columns) {
		super();
		this.setColumns(columns);
		this.setHorizontalAlignment(SwingConstants.LEFT);
		this.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));
	}

	/**
	 * Called when one of the fields gets the focus so that we can select the
	 * focused field.
	 * @param e FocusEvent
	 * @see java.awt.event.FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		Component c = e.getComponent();
		if (c instanceof JFormattedTextField) {
			selectItLater(c);
		}
	}

	// Workaround for formatted text field focus side effects.
	/**
	 * Method selectItLater.
	 * @param c Component
	 */
	protected void selectItLater(Component c) {
		if (c instanceof JFormattedTextField) {
			final JFormattedTextField ftf = (JFormattedTextField) c;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ftf.selectAll();
				}
			});
		}
	}

	// Needed for FocusListener interface.
	/**
	 * Method focusLost.
	 * @param e FocusEvent
	 * @see java.awt.event.FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		// ignore
	}
}
