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
package org.trade.ui.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * 
 * @version $Id: ErrorPanel.java,v 1.2 2001/11/06 22:37:27 simon Exp $
 * @author Simon Allen
 */
public class ErrorPanel extends BasePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2624890325384057649L;

	JPanel jPanel1 = new JPanel();

	JPanel jPanel4 = new JPanel();

	JPanel jPanel2 = new JPanel();

	JPanel jPanel3 = new JPanel();

	BorderLayout borderLayout1 = new BorderLayout();

	JPanel jPanel5 = new JPanel();

	JPanel jPanel13 = new JPanel();

	BorderLayout borderLayout2 = new BorderLayout();

	BorderLayout borderLayout3 = new BorderLayout();

	BorderLayout borderLayout5 = new BorderLayout();

	GridLayout gridLayout1 = new GridLayout();

	BorderLayout borderLayout6 = new BorderLayout();

	JTextField statusBar = new JTextField();

	GridLayout gridLayout2 = new GridLayout();

	JPanel jPanel15 = new JPanel();

	JScrollPane jScrollPane1 = new JScrollPane();

	BorderLayout borderLayout7 = new BorderLayout();

	StreamEditorPane jTextArea1 = new StreamEditorPane();

	JPanel jPanel16 = new JPanel();

	JPanel jPanel17 = new JPanel();

	JPanel jPanel10 = new JPanel();

	FlowLayout flowLayout5 = new FlowLayout();

	JPanel jPanel7 = new JPanel();

	GridLayout gridLayout5 = new GridLayout();

	GridLayout gridLayout6 = new GridLayout();

	FlowLayout flowLayout4 = new FlowLayout();

	JPanel jPanel8 = new JPanel();

	JPanel jPanel9 = new JPanel();

	GridLayout gridLayout7 = new GridLayout();

	BorderLayout borderLayout8 = new BorderLayout();

	BorderLayout borderLayout9 = new BorderLayout();

	JPanel jPanel18 = new JPanel();

	JPanel jPanel = new JPanel();

	FlowLayout flowLayout1 = new FlowLayout();

	GridLayout gridLayout8 = new GridLayout();

	GridLayout gridLayout9 = new GridLayout();

	BaseButton jButtonPrint = new BaseButton(this, BaseUIPropertyCodes.PRINT);

	BaseButton jButtonClose = new BaseButton(this, BaseUIPropertyCodes.CLOSE);

	BaseButton jButtonNew = new BaseButton(this, BaseUIPropertyCodes.NEW);

	BaseButton jButtonSave = new BaseButton(this, BaseUIPropertyCodes.SAVE);

	BaseButton jButtonOpen = new BaseButton(this, BaseUIPropertyCodes.OPEN_FILE);

	BaseButton jButtonSaveAs = new BaseButton(this, BaseUIPropertyCodes.SAVE_AS);

	BaseButton jButtonHelp = new BaseButton(this, BaseUIPropertyCodes.HELP);

	BaseButton jButtonCut = new BaseButton(this, BaseUIPropertyCodes.CUT);

	BaseButton jButtonCopy = new BaseButton(this, BaseUIPropertyCodes.COPY);

	BaseButton jButtonPaste = new BaseButton(this, BaseUIPropertyCodes.PASTE);

	public ImageIcon jpgIcon;

	public ImageIcon gifIcon;

	JColorChooser colorChooser1 = new JColorChooser();

	JFileChooser filer1 = null;

	JFrame mainFrame = null;

	String currFileName = null; // path plus filename. null means new / untitled

	boolean dirty = false; // true means modified text

	public ErrorPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Method jbInit.
	 * @throws Exception
	 */
	void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jPanel1.setLayout(borderLayout6);
		jPanel4.setLayout(gridLayout1);
		jPanel2.setLayout(borderLayout3);
		jPanel3.setLayout(borderLayout5);
		jPanel4.setBackground(Color.orange);
		jPanel2.setBackground(Color.red);
		jPanel3.setBackground(Color.pink);
		gridLayout1.setVgap(5);
		jPanel17.setLayout(borderLayout9);
		jPanel16.setLayout(borderLayout8);
		jPanel10.setLayout(gridLayout5);
		flowLayout5.setVgap(0);
		jPanel7.setLayout(flowLayout5);
		flowLayout4.setHgap(2);
		flowLayout4.setAlignment(0);
		flowLayout4.setVgap(0);
		jPanel8.setLayout(flowLayout4);
		jPanel9.setLayout(gridLayout9);
		jPanel18.setLayout(flowLayout1);
		jPanel.setLayout(gridLayout8);
		flowLayout1.setVgap(2);
		flowLayout1.setHgap(2);
		flowLayout5.setAlignment(0);
		flowLayout5.setHgap(1);
		jPanel15.setLayout(borderLayout7);
		gridLayout1.setHgap(2);
		jPanel13.setLayout(gridLayout2);
		jPanel5.setLayout(borderLayout2);
		this.add(jPanel1, BorderLayout.CENTER);
		jPanel1.add(jPanel2, BorderLayout.NORTH);
		jPanel2.add(jPanel5, BorderLayout.NORTH);
		jPanel5.add(jPanel16, BorderLayout.NORTH);
		jPanel16.add(jPanel8, BorderLayout.EAST);
		jPanel8.add(jPanel9, null);
		jPanel9.add(jButtonHelp, null);
		jPanel16.add(jPanel7, BorderLayout.WEST);
		jPanel7.add(jPanel10, null);
		jPanel10.add(jButtonNew, null);
		jPanel10.add(jButtonOpen, null);
		jPanel10.add(jButtonSaveAs, null);
		jPanel10.add(jButtonSave, null);
		jPanel10.add(jButtonPrint, null);
		jPanel10.add(jButtonClose, null);
		jPanel5.add(jPanel17, BorderLayout.SOUTH);
		jPanel17.add(jPanel18, BorderLayout.WEST);
		jPanel18.add(jPanel, null);
		jPanel.add(jButtonCopy, null);
		jPanel.add(jButtonCut, null);
		jPanel.add(jButtonPaste, null);
		jPanel1.add(jPanel3, BorderLayout.CENTER);
		jPanel3.add(jPanel15, BorderLayout.CENTER);
		jPanel15.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(jTextArea1, null);
		jPanel1.add(jPanel4, BorderLayout.SOUTH);
		jPanel4.add(jPanel13, null);
		jPanel13.add(statusBar, null);
		jTextArea1.setPreferredSize(new Dimension(200, 100));
	}

	/**
	 * doNew() -
	 * 
	
	 * 
	
	
	
	 * @exception * @see */
	public void doNew() {
		// Handle the File|New menu item.
		if (okToAbandon()) {
			// clear the text of the TextArea
			jTextArea1.setText("");

			// clear the current filename and set the file as clean:
			currFileName = null;
			dirty = false;

			updateCaption();
		}
	}

	/**
	 * doOpen() -
	 * 
	
	 * 
	
	
	
	 * @exception * @see */
	public void doOpen() {
		if (!okToAbandon()) {
			return;
		}

		jpgIcon = new ImageIcon("images/jpgIcon.jpg");
		gifIcon = new ImageIcon("images/gifIcon.gif");

		ExampleFileFilter filter = new ExampleFileFilter(new String[] { "jpg",
				"gif" }, "JPEG and GIF Image Files");
		ExampleFileChooser fileView = new ExampleFileChooser();

		fileView.putIcon("jpg", jpgIcon);
		fileView.putIcon("gif", gifIcon);

		if (filer1 == null) {
			filer1 = new JFileChooser();
		}

		filer1.setFileView(fileView);
		filer1.addChoosableFileFilter(filter);
		filer1.setFileFilter(filter);

		// filer1.setAccessory(new ExampleFileView());
		// File swingFile = new File("images/swing-64.gif");
		int returnVal = filer1.showOpenDialog(this);

		// Upon return, getFile() will be null if user cancelled the dialog.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Non-null file property after return implies user
			// selected a file to open.
			// Call openFile to attempt to load the text from file into TextArea
			currFileName = filer1.getSelectedFile().getPath();

			openFile(currFileName);
		}
	}

	/**
	 * openFile() -
	 * 
	
	 * @param fileName
	 *            the name of the text file on disk
	 * 
	
	
	
	 * @exception * @see */
	public void openFile(String fileName) {
		Cursor oldCursor = getFrame().getCursor();

		getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			InputStream is = new BufferedInputStream(new FileInputStream(
					currFileName));

			// Load the data from the stream
			jTextArea1.setInputStream(is);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this, "Failed to open file "
					+ fileName, "Warning", JOptionPane.WARNING_MESSAGE);
			statusBar.setText("Error opening " + fileName);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Error while reading file "
					+ fileName, "Warning", JOptionPane.WARNING_MESSAGE);
			statusBar.setText("Error opening " + fileName);
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(this, "Unexpected exception: " + t,
					"Warning", JOptionPane.WARNING_MESSAGE);
			statusBar.setText("Error opening " + fileName);
		}

		// ...and mark the edit session as being clean
		this.dirty = false;

		// Display the name of the opened directory+file in the statusBar.
		statusBar.setText("Opened " + fileName);
		updateCaption();
		getFrame().setCursor(oldCursor);
	}

	/**
	 * doSave() -
	 * 
	
	
	 * 
	
	
	
	 * @exception * @see */
	public void doSave() {
		doSaveFile();
	}

	/**
	 * doSave() -
	 * 
	
	
	 * 
	
	
	
	 * @return false if save did not occur. * @exception * @see */
	protected boolean doSaveFile() {
		// Handle the case where we don't have a file name yet.
		if (currFileName == null) {
			return saveAs();
		}

		if (saveFileStream()) {
			this.dirty = false;

			updateCaption();

			return true;
		} else {
			return false;
		}
	}

	/**
	 * doSaveAs() -
	 * 
	
	
	 * 
	
	
	
	 * @exception * @see */
	public void doSaveAs() {
		saveAs();
	}

	/**
	 * saveAs() -
	 * 
	
	
	 * 
	
	
	 * @return false means user cancelled the SaveAs * @exception * @see */
	public boolean saveAs() {
		if (filer1 == null) {
			filer1 = new JFileChooser();
		}

		// Make the dialog visible as a modal (default) dialog box.
		int returnVal = filer1.showSaveDialog(this);

		// Upon return, getFile() will be null if user cancelled the dialog.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Non-null file property after return implies user
			// selected a filename to save to.
			// Set the current file name to the user's selection,
			// then do a regular saveFile
			currFileName = filer1.getSelectedFile().getPath();

			return doSaveFile();
		} else {
			return false;
		}
	}

	/**
	 * okToAbandon() -
	 * 
	
	
	
	
	
	 * @return true if user saved here (Yes), or didn't care (No) * @exception * @see */
	private boolean okToAbandon() {
		if (!dirty) {
			return true;
		}

		int result = JOptionPane.showInternalConfirmDialog(this,
				"Save changes ? ", "Warning", JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.WARNING_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			return saveAs();
		} else if (result == JOptionPane.NO_OPTION) {
			return true;
		} else if (result == JOptionPane.CANCEL_OPTION) {
			// cancel
			return false;
		} else {
			// cancel
			return false;
		}
	}

	/**
	 * updateCaption() -
	 * 
	
	
	
	
	 * @exception * @see */
	private void updateCaption() {
		String caption;

		if (currFileName == null) {
			// synthesize the "Untitled" name if no name yet.
			caption = "Untitled";
		} else {
			caption = currFileName;
		}

		// add a "*" in the caption if the file is dirty.
		if (dirty) {
			caption = "* " + caption;
		}

		caption = "TextEdit - " + caption;
	}

	/**
	 * doColor() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see */
	public void doColor() {
		// Handle the "Foreground Color" menu item
		// Pick up the existing text (foreground) color from the TextArea
		// and put it into the ColorChooser before showing
		// the ColorChooser, so that we are editing the
		// existing text color.
		// Before showing it, set the title of the dialog for its
		// particular use in this event (for setting the text color)
		// Show the ColorChooser.
		// Since the ColorChooser is modal by default,
		// the program will not return from the call
		// to show until the user dismisses the ColorChooser
		// using OK or Cancel.
		// Now that the user has dismissed the ColorChooser,
		// obtain the new color from the ColorChooser's
		// value property. First test the result property to see if the
		// user pressed OK.
		Color color = JColorChooser.showDialog(this, "Set Text Color",
				jTextArea1.getForeground());

		if (color != null) {
			// set the foreground of textArea1 to the color
			// value that can be obtained from the
			// value property of colorChooser1. This
			// color value is what the user set
			// before pressing the OK button
			jTextArea1.setBackground(color);
			this.repaint();
		}
	}

	/**
	 * doDataChanged() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see
	 * 
	 *      private void doDataChanged() { if (!dirty) { dirty = true;
	 * 
	 *      updateCaption(); } } */

	/**
	 * doPrint()() -
	 * 
	 * @param <B>
	 *            </B>
	 * 
	 * 
	 * @return
	 * @exception
	 * @see
	 */
	public void doPrint() {
		PrintController printer = new PrintController();

		printer.printComponent(getFrame(), jTextArea1, "Print Errors");
	}

	/**
	 * saveFileWriter() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @return boolean
	 * @exception * @see */
	public boolean saveFileWriter() {
		boolean result = false;

		try {
			Writer w = new FileWriter(currFileName);

			jTextArea1.write(w);
			w.close();
			statusBar.setText("Saving " + currFileName);

			result = true;
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(this, "Could not save file "
					+ currFileName, "Warning", JOptionPane.WARNING_MESSAGE);

			result = false;
		}

		return result;
	}

	/**
	 * saveFileWriter() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @return boolean
	 * @exception * @see */
	public boolean saveFileStream() {
		boolean result = false;

		try {
			OutputStream o = new FileOutputStream(currFileName);

			jTextArea1.getEditorKit().write(o, jTextArea1.getDocument(), 0,
					jTextArea1.getDocument().getLength());
			o.close();
			statusBar.setText("Saving " + currFileName);

			result = true;
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(this, "Could not save file "
					+ currFileName, "Warning", JOptionPane.WARNING_MESSAGE);

			result = false;
		}

		return result;
	}

	/**
	 * doWindowActivated() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see */
	public void doWindowActivated() {
	}

	/**
	 * doWindowDeActivated() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @return boolean * @exception * @see */
	public boolean doWindowDeActivated() {
		return true;
	}

	/**
	 * doWindowClose() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see */
	public void doWindowClose() {
		okToAbandon();
	}

	public void doWindowOpen() {
	}

	/**
	 * doWindowOpen() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @param parm Object
	 * @exception * @see */
	public void doWindowOpen(Object parm) {
		if (parm instanceof String) {
			jTextArea1.setText((String) parm);
		}
	}

	/**
	 * doCopy() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see */
	public void doCopy() {
		jTextArea1.copy();
	}

	/**
	 * doCut() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see */
	public void doCut() {
		jTextArea1.cut();
	}

	/**
	 * doPaste() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see */
	public void doPaste() {
		jTextArea1.paste();
	}

	/**
	 * doDelete() -
	 * 
	
	 * 
	 * 
	
	
	
	 * @exception * @see */
	public void doDelete() {
		jTextArea1.setText("");
	}
}
