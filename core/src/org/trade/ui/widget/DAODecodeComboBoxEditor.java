/* ===========================================================
 * TradeManager : An application to trade strategies for the Java(tm) platform
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
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.trade.core.valuetype.DAODecode;
import org.trade.core.valuetype.Decode;

/**
 * 
 * @version $Id: DecodeComboBoxEditor.java,v 1.2 2001/11/06 17:14:47 simon Exp $
 * @author Simon Allen
 */

public class DAODecodeComboBoxEditor extends JComboBox<Decode> implements
		ComboBoxEditor, ItemListener, FocusListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1626795772462262674L;

	protected transient DAODecode originalValue;
	protected transient Vector<ActionListener> listeners;

	/**
	 * Constructor for DAODecodeComboBoxEditor.
	 * 
	 * @param model
	 *            Vector<?>
	 */
	public DAODecodeComboBoxEditor(Vector<Decode> model) {
		super(model);
		this.addItemListener(this);
		this.addFocusListener(this);
		listeners = new Vector<ActionListener>();
	}

	/**
	 * Return the component that should be added to the tree hierarchy for this
	 * editor
	 * 
	 * @return Component
	 * @see javax.swing.ComboBoxEditor#getEditorComponent()
	 */
	public Component getEditorComponent() {
		return this;
	}

	/**
	 * Set the item that should be edited. Cancel any editing if necessary * @param
	 * anObject Object
	 * 
	 * @see javax.swing.ComboBoxEditor#setItem(Object)
	 */
	public void setItem(Object anObject) {
		for (int i = 0; i < this.getItemCount(); i++) {
			DAODecode d = (DAODecode) this.getItemAt(i);
			if (d.getCode().equals(((DAODecode) anObject).getCode())) {
				setSelectedItem(d);
				break;
			}
		}
	}

	/**
	 * Return the edited item * @return Object
	 * 
	 * @see javax.swing.ComboBoxEditor#getItem()
	 */
	public Object getItem() {
		return getSelectedItem();
	}

	/**
	 * Ask the editor to start editing and to select everything * @see
	 * javax.swing.ComboBoxEditor#selectAll()
	 */
	public void selectAll() {
	}

	/**
	 * Add an ActionListener. An action event is generated when the edited item
	 * changes
	 * 
	 * @param l
	 *            ActionListener
	 * @see javax.swing.ComboBoxEditor#addActionListener(ActionListener)
	 */
	public void addActionListener(ActionListener l) {
		listeners.addElement(l);
	}

	/**
	 * Remove an ActionListener * @param l ActionListener
	 * 
	 * @see javax.swing.ComboBoxEditor#removeActionListener(ActionListener)
	 */
	public void removeActionListener(ActionListener l) {
		listeners.removeElement(l);
	}

	protected void fireEditingCanceled() {
		for (int i = 0; i < this.getItemCount(); i++) {
			DAODecode d = (DAODecode) this.getItemAt(i);
			if (d.equals(originalValue)) {
				setSelectedItem(originalValue);
				break;
			}
		}

		ChangeEvent ce = new ChangeEvent(this);
		for (int i = listeners.size(); i >= 0; i--) {
			((CellEditorListener) listeners.elementAt(i)).editingCanceled(ce);
		}
	}

	protected void fireEditingStopped() {
		ChangeEvent ce = new ChangeEvent(this);
		for (int i = listeners.size() - 1; i >= 0; i--) {
			((CellEditorListener) listeners.elementAt(i)).editingStopped(ce);
		}
	}

	/**
	 * Method itemStateChanged.
	 * 
	 * @param evt
	 *            ItemEvent
	 * @see java.awt.event.ItemListener#itemStateChanged(ItemEvent)
	 */
	public void itemStateChanged(ItemEvent evt) {
		fireEditingStopped();
	}

	/**
	 * Method focusGained.
	 * 
	 * @param evt
	 *            FocusEvent
	 * @see java.awt.event.FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent evt) {
	}

	/**
	 * Method focusLost.
	 * 
	 * @param evt
	 *            FocusEvent
	 * @see java.awt.event.FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent evt) {
	}
}
