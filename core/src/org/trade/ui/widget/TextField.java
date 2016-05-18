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
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Hashtable;

import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 */
public class TextField extends JTextField implements FocusListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 339471596367850297L;

	private static Hashtable<Integer, Character> editMask = new Hashtable<Integer, Character>();

	private static Color originalColor = null;

	/**
	 * Constructor for TextField.
	 * 
	 * @param mask
	 *            String
	 */
	public TextField(String mask) {
		super();

		originalColor = this.getBackground();

		this.addFocusListener(this);
		this.setBorder(new EmptyBorder(new Insets(2, 2, 2, 2)));

		char[] maskChars = mask.toCharArray();

		for (int i = 0; i < maskChars.length; i++) {
			editMask.put(new Integer(i), new Character(maskChars[i]));
		}
	}

	/**
	 * Method createDefaultModel.
	 * 
	 * @return Document
	 */
	protected Document createDefaultModel() {
		TextDocument doc = new TextDocument();

		/*
		 * 
		 * DocumentListener d = new DocumentListener() { public void
		 * changedUpdate (DocumentEvent evt) { } public void insertUpdate
		 * (DocumentEvent evt) { } public void removeUpdate (DocumentEvent evt)
		 * { int i = evt.getOffset(); if (evt.getOffset() == 1 ||
		 * evt.getOffset() == 4) { moveCursor(evt.getOffset()); } } };
		 * doc.addDocumentListener(d);
		 */

		return doc;
	}

	/**
	 * Method getText.
	 * 
	 * @return String
	 */
	public String getText() {
		String text;

		text = super.getText();

		return text;
	}

	/**
	 * Method focusGained.
	 * 
	 * @param evt
	 *            FocusEvent
	 * @see java.awt.event.FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent evt) {
		this.setSelectionStart(0);
		this.setSelectionEnd(0);
	}

	/**
	 * Method focusLost.
	 * 
	 * @param evt
	 *            FocusEvent
	 * @see java.awt.event.FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent evt) {
		if (!(isValid())) {
			this.setSelectionStart(0);
			this.setSelectionEnd(0);
		}
	}

	/**
	 * Method isValid.
	 * 
	 * @return boolean
	 */
	public boolean isValid() {
		boolean isValid = false;

		if (this.getText().trim().length() > 0) {
			this.setBackground(Color.red);

			isValid = true;

			this.setBackground(originalColor);
		} else {
			isValid = true;
		}

		this.repaint();

		return isValid;
	}

	/**
	 * 
	 * @version $Id: TextField.java,v 1.2 2001/12/28 21:14:55 simon Exp $
	 * @author Simon Allen
	 */
	static class TextDocument extends PlainDocument {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2258034828743548985L;

		/**
		 * Method insertString.
		 * 
		 * @param offs
		 *            int
		 * @param str
		 *            String
		 * @param a
		 *            AttributeSet
		 * @throws BadLocationException
		 * @see javax.swing.text.Document#insertString(int, String,
		 *      AttributeSet)
		 */
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if (str != null) {
				if (!(editMask.isEmpty())) {
					Character selected = editMask.get(new Integer(offs));

					if (selected != null) {
						if (Character.isLetter(selected.charValue())) {
						} else {
							str = selected.charValue() + str;
						}
					} else {
						return;
					}
				}
			} else {
				return;
			}

			char[] upper = str.toCharArray();

			for (int i = 0; i < upper.length; i++) {
				upper[i] = Character.toUpperCase(upper[i]);

				Character selected = editMask.get(new Integer(offs + i));

				if (selected != null) {
					if (Character.isLetter(selected.charValue())) {
						if (!(Character.isDigit(upper[i]))) {
							return;
						}
					}
				}
			}

			if (super.getLength() > offs) {
				super.remove(offs, upper.length);
			}

			super.insertString(offs, new String(upper), a);
		}
	}
}
