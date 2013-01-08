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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URLConnection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The StreamEditorPane responsible for actaully displaying text
 * 
 * @version $Id: StreamEditorPane.java,v 1.10 2002/01/11 20:06:02 simon Exp $
 * @author Simon Allen
 */
public class StreamEditorPane extends JEditorPane implements MouseListener,
		ActionListener {

	private static final long serialVersionUID = -8068406289677664715L;

	private final static Logger _log = LoggerFactory
			.getLogger(StreamEditorPane.class);

	protected UndoableEditListener undoHandler = new UndoHandler();

	protected UndoManager undo = new UndoManager();

	private UndoAction undoAction = new UndoAction();

	private RedoAction redoAction = new RedoAction();

	private JPopupMenu popup = null;

	private String m_currFileName = null;

	private int m_startPosition = 0;

	private String m_currDir = null;

	JFileChooser m_fileChooser = new JFileChooser();

	public StreamEditorPane() {
		this("text/plain");
	}

	/**
	 * Constructor for StreamEditorPane.
	 * 
	 * @param type
	 *            String
	 * @throws IOException
	 */
	public StreamEditorPane(String type) {
		super();

		if (type == null) {
			// Unknown type - use plain text
			type = "text/plain";
		}

		DefaultStyledDocument doc = new DefaultStyledDocument(
				new StyleContext());
		StyledEditorKit styled = new StyledEditorKit();

		setContentType(type);
		setEditorKitForContentType(type, styled);
		setDocument(doc);
		this.setMinimumSize(new Dimension(300, 200));
		this.addMouseListener(this);
	}

	/**
	 * Method append.
	 * 
	 * @param text
	 *            String
	 */
	public void append(String text) {
		append(text, null);
	}

	/**
	 * Method append.
	 * 
	 * @param text
	 *            String
	 * @param attrSet
	 *            AttributeSet
	 */
	public void append(String text, AttributeSet attrSet) {
		try {
			this.getDocument().insertString(this.getDocument().getLength(),
					text, attrSet);
		} catch (BadLocationException ex) {
			_log.error("Error appending text:", ex);
		}
	}

	/**
	 * Method setInputStream.
	 * 
	 * @param is
	 *            InputStream
	 * @throws IOException
	 * @throws BadLocationException
	 */
	public void setInputStream(InputStream is) throws IOException,
			BadLocationException {
		String type = guessContentType(is);

		if (type == null) {
			// Unknown type - use plain text
			type = "text/plain";
		}

		// Set the new content type
		setContentType(type);

		// Now create the appropriate document and install it
		// Document doc = getEditorKit().createDefaultDocument();
		DefaultStyledDocument doc = new DefaultStyledDocument(
				new StyleContext());
		StyledEditorKit styled = new StyledEditorKit();

		setEditorKitForContentType(type, styled);
		setDocument(doc);
		// Finally, read the date from the stream
		getEditorKit().read(is, doc, 0);
	}

	/**
	 * Method setDocument.
	 * 
	 * @param doc
	 *            Document
	 */
	public void setDocument(Document doc) {
		if (null != getDocument()) {
			getDocument().removeUndoableEditListener(undoHandler);
		}

		super.setDocument(doc);
		getDocument().addUndoableEditListener(undoHandler);
	}

	/**
	 * Method getText.
	 * 
	 * @return String
	 */
	public String getText() {
		String text = null;

		try {
			text = super.getDocument().getText(0,
					super.getDocument().getLength());
		} catch (BadLocationException ex) {
			_log.error("Error appending getting text:", ex);
		}

		return text;
	}

	/**
	 * Save current file, asking user for new destination name. Report to
	 * statuBar.
	 * 
	 * 
	 * @return false means user cancelled the SaveAs
	 */
	public boolean saveAs() {
		boolean retSaveAs = false;
		// Make the dialog visible as a modal (default) dialog box.
		ExampleFileFilter filter = new ExampleFileFilter(new String[] { "*" },
				"All Files");

		m_fileChooser.addChoosableFileFilter(filter);

		if (null != getDirName()) {
			m_fileChooser.setSelectedFile(new File(getDirName() + "//"
					+ getFileName()));
		} else {
			m_fileChooser.setCurrentDirectory(new File(System
					.getProperty("user.dir")));
		}

		int returnVal = m_fileChooser.showSaveDialog(this.getRootPane());

		// Upon return, getFile() will be null if user cancelled the dialog.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Non-null file property after return implies user
			// selected a filename to save to.
			// Set the current file name to the user's selection,
			// then do a regular saveFile
			if (m_fileChooser.getSelectedFile().exists()) {
				int result = JOptionPane.showConfirmDialog(this.getRootPane(),
						"File Exists. Do you want to over write ? ", "Warning",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);

				if (result == JOptionPane.YES_OPTION) {
					setFileName(m_fileChooser.getSelectedFile().getPath());
					setCurrentDirectory(m_fileChooser.getCurrentDirectory()
							.toString());

					retSaveAs = saveFileWriter(getFileName());
				} else if (result == JOptionPane.NO_OPTION) {
					retSaveAs = true;
				} else if (result == JOptionPane.CANCEL_OPTION) {
					// cancel
					retSaveAs = false;
				} else {
					// cancel
					retSaveAs = false;
				}
			} else {
				setFileName(m_fileChooser.getSelectedFile().getPath());
				setCurrentDirectory(m_fileChooser.getCurrentDirectory()
						.toString());

				retSaveAs = saveFileWriter(getFileName());
			}
		} else {
			retSaveAs = false;
		}

		return retSaveAs;
	}

	/**
	 * Save current file, asking user for new destination name. Report to
	 * statuBar.
	 * 
	 * 
	 * @return false means user cancelled the SaveAs
	 */
	public boolean openFile() {
		boolean retOpenFile = false;
		// Make the dialog visible as a modal (default) dialog box.
		ExampleFileFilter filter = new ExampleFileFilter(new String[] { "*" },
				"All Files");

		m_fileChooser.addChoosableFileFilter(filter);

		if (null != getDirName()) {
			m_fileChooser.setSelectedFile(new File(getDirName() + "//"
					+ getFileName()));
		} else {
			m_fileChooser.setCurrentDirectory(new File(System
					.getProperty("user.dir")));
		}

		int returnVal = m_fileChooser.showOpenDialog(this.getRootPane());

		// Upon return, getFile() will be null if user cancelled the dialog.
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// Non-null file property after return implies user
			// selected a filename to save to.
			// Set the current file name to the user's selection,
			// then do a regular saveFile
			if (m_fileChooser.getSelectedFile().exists()) {
				setFileName(m_fileChooser.getSelectedFile().getPath());
				setCurrentDirectory(m_fileChooser.getCurrentDirectory()
						.toString());

				retOpenFile = openFileReader(getFileName());
			}
		} else {
			retOpenFile = false;
		}

		return retOpenFile;
	}

	/**
	 * Method getDirName.
	 * 
	 * @return String
	 */
	private String getDirName() {
		return m_currDir;
	}

	/**
	 * Method setCurrentDirectory.
	 * 
	 * @param dir
	 *            String
	 */
	private void setCurrentDirectory(String dir) {
		m_currDir = dir;
	}

	/**
	 * Method getFileName.
	 * 
	 * @return String
	 */
	public String getFileName() {
		return m_currFileName;
	}

	/**
	 * Method setFileName.
	 * 
	 * @param currFileName
	 *            String
	 */
	public void setFileName(String currFileName) {
		m_currFileName = currFileName;
	}

	/**
	 * Method openFileReader.
	 * 
	 * @param fileName
	 *            String
	 * @return boolean
	 */
	public boolean openFileReader(String fileName) {
		boolean result = false;

		try {
			Reader read = new FileReader(fileName);
			this.read(read, fileName);
			read.close();

			result = true;
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(this, "Could not save file "
					+ fileName, "Warning", JOptionPane.WARNING_MESSAGE);

			result = false;
		}

		return result;
	}

	/**
	 * Method saveFileWriter.
	 * 
	 * @param fileName
	 *            String
	 * @return boolean
	 */
	public boolean saveFileWriter(String fileName) {
		boolean result = false;

		try {
			Writer w = new FileWriter(fileName);

			this.write(w);
			w.close();

			result = true;
		} catch (Throwable t) {
			JOptionPane.showMessageDialog(this, "Could not save file "
					+ fileName, "Warning", JOptionPane.WARNING_MESSAGE);

			result = false;
		}

		return result;
	}

	/**
	 * Method setText.
	 * 
	 * @param text
	 *            String
	 */
	public void setText(String text) {
		super.setText(text);
		undo.discardAllEdits();
	}

	/**
	 * Method isDirty.
	 * 
	 * @return boolean
	 */
	public boolean isDirty() {
		return undo.canUndoOrRedo();
	}

	/**
	 * Method find.
	 * 
	 * @param findText
	 *            String
	 * @param startPosition
	 *            int
	 */
	public void find(String findText, int startPosition) {
		String findTextUpper = findText.toUpperCase();

		try {
			String text = getDocument().getText(0, getDocument().getLength());
			int currstartPosition = text.toUpperCase().indexOf(findTextUpper,
					startPosition);

			if (currstartPosition != -1) {
				this.setSelectionStart(currstartPosition);
				this.setSelectionEnd(currstartPosition + findTextUpper.length());
				this.setSelectionColor(Color.red);

				m_startPosition = currstartPosition + findTextUpper.length()
						+ 1;
			} else {
				m_startPosition = 0;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	// Guess the content type
	/**
	 * Method guessContentType.
	 * 
	 * @param is
	 *            InputStream
	 * @return String
	 * @throws IOException
	 */
	protected String guessContentType(InputStream is) throws IOException {
		String type = URLConnection.guessContentTypeFromStream(is);

		if (type == null) {
			is.mark(10);

			int c1 = is.read();
			int c2 = is.read();
			int c3 = is.read();
			int c4 = is.read();
			int c5 = is.read();

			is.reset();

			if ((c1 == '{') && (c2 == '\\') && (c3 == 'r') && (c4 == 't')
					&& (c5 == 'f')) {
				type = "text/rtf";
			}
			// Add more heuristics here
		}

		return type;
	}

	/**
	 * mousePressed() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			createPopup(evt.getPoint());
		}
	}

	/**
	 * mouseReleased() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseReleased(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			createPopup(evt.getPoint());
		}
	}

	/**
	 * mouseClicked() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseClicked(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			if (this.isEnabled()) {
				createPopup(evt.getPoint());
			}
		}
	}

	/**
	 * mouseEntered() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseEntered(MouseEvent evt) {
	}

	/**
	 * mouseExited() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param evt
	 *            MouseEvent
	 * @exception * @see
	 */
	public void mouseExited(MouseEvent evt) {
	}

	/**
	 * createPopup() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param point
	 *            Point
	 * @exception * @see
	 */
	protected void createPopup(Point point) {

		popup = new JPopupMenu();

		JMenuItem undo = new JMenuItem("Undo");
		undo.setMnemonic('Z');
		JMenuItem mi = popup.add(undo);
		mi.addActionListener(this);

		JMenuItem redo = new JMenuItem("Redo");
		// redo.setMnemonic('Z');
		mi = popup.add(redo);
		mi.addActionListener(this);
		popup.addSeparator();

		JMenuItem copy = new JMenuItem("Copy");
		copy.setMnemonic('C');
		mi = popup.add(copy);
		mi.addActionListener(this);

		JMenuItem cut = new JMenuItem("Cut");
		cut.setMnemonic('X');
		mi = popup.add(cut);
		mi.addActionListener(this);

		JMenuItem paste = new JMenuItem("Paste");
		paste.setMnemonic('V');
		mi = popup.add(paste);
		mi.addActionListener(this);
		popup.addSeparator();

		JMenuItem open = new JMenuItem("Open");
		open.setMnemonic('O');
		mi = popup.add(open);
		mi.addActionListener(this);

		JMenuItem save = new JMenuItem("Save");
		save.setMnemonic('S');
		mi = popup.add(save);
		mi.addActionListener(this);
		popup.addSeparator();

		JMenuItem print = new JMenuItem("Print");
		print.setMnemonic('P');
		mi = popup.add(print);
		mi.addActionListener(this);
		popup.addSeparator();

		JMenuItem find = new JMenuItem("Find");
		find.setMnemonic('F');
		mi = popup.add(find);
		mi.addActionListener(this);

		JMenuItem replace = new JMenuItem("Replace");
		replace.setMnemonic('R');
		mi = popup.add(replace);
		mi.addActionListener(this);

		// Try to make the popup lightweight
		point = getSuitableLocation(point, popup.getPreferredSize(), this);

		popup.show(this, point.x, point.y);
	}

	/**
	 * actionPerformed() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param evt
	 *            ActionEvent
	 * @exception * @see
	 */
	public void actionPerformed(ActionEvent evt) {
		JMenuItem mi = (JMenuItem) evt.getSource();

		if (mi.getActionCommand().equals("Undo")) {
			undo.undo();
		} else if (mi.getActionCommand().equals("Redo")) {
			undo.redo();
		} else if (mi.getActionCommand().equals("Copy")) {
			this.copy();
		} else if (mi.getActionCommand().equals("Cut")) {
			this.cut();
		} else if (mi.getActionCommand().equals("Paste")) {
			this.paste();
		} else if (mi.getActionCommand().equals("Save")) {
			this.saveAs();
		} else if (mi.getActionCommand().equals("Open")) {
			this.openFile();
		} else if (mi.getActionCommand().equals("Print")) {
			try {
				print();
			} catch (PrinterException e) {
				// Do nothing ....
			}
		} else if (mi.getActionCommand().equals("Find")) {
			find();
		} else if (mi.getActionCommand().equals("Replace")) {
			replace();
		}

		this.repaint();
	}

	/**
	 * getSuitableLocation() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param point
	 *            Point
	 * @param d
	 *            Dimension
	 * @param c
	 *            Component
	 * @return Point * @exception * @see
	 */
	protected Point getSuitableLocation(Point point, Dimension d, Component c) {
		// First locate the parent JFrame or JDialog
		Component topLevel = getComponentContainer(c);

		if (topLevel != null) {
			int newX; // New proposed x co-ordinate
			int newY; // New proposed y co-ordinate
			// We have a top-level parent
			Rectangle parentBounds = topLevel.getBounds(); // Parent bounds,
			// screen-relative

			// Get proposed location, relative to the screen
			SwingUtilities.convertPointToScreen(point, c);

			if ((point.x + d.width) > (parentBounds.x + parentBounds.width)) {
				// Popup overhangs to the right
				newX = (parentBounds.x + parentBounds.width) - d.width;
			} else {
				newX = point.x;
			}

			if ((point.y + d.height) > (parentBounds.y + parentBounds.height)) {
				// Popup ends below frame
				newY = (parentBounds.y + parentBounds.height) - d.height;
			} else {
				newY = point.y;
			}

			// Change location only if necessary AND if
			// we can make the popup fit inside the frame
			if ((newX >= 0) && (newY >= 0)) {
				point.x = newX;
				point.y = newY;
			}

			// Convert back to relative co-ordinates
			SwingUtilities.convertPointFromScreen(point, c);
		}

		return point;
	}

	private void replace() {

		String textToReplace = this.getSelectedText().toUpperCase();
		if (null == textToReplace)
			return;

		String replaceText = (String) JOptionPane.showInputDialog(
				this.getRootPane(), "Replace", "Replace selected text with",
				JOptionPane.INFORMATION_MESSAGE, null, null, null);

		try {
			String text = getDocument().getText(0, getDocument().getLength());
			int startPosition = text.toUpperCase().indexOf(textToReplace,
					m_startPosition);

			while (startPosition != -1) {
				this.setSelectionStart(startPosition);
				this.setSelectionEnd(startPosition + textToReplace.length());
				this.setSelectionColor(Color.red);

				int option = JOptionPane.showConfirmDialog(this.getRootPane(),
						"Do you want to replace ?", "Replace ?",
						JOptionPane.YES_NO_OPTION,
						JOptionPane.INFORMATION_MESSAGE);

				if (option == JOptionPane.YES_OPTION) {
					this.replaceSelection(replaceText);

					m_startPosition = startPosition + replaceText.length() + 1;
				} else {
					m_startPosition = startPosition + textToReplace.length()
							+ 1;
				}

				text = getDocument().getText(0, getDocument().getLength());
				startPosition = text.toUpperCase().indexOf(textToReplace,
						m_startPosition);
			}

			m_startPosition = 0;
		} catch (BadLocationException ex) {
			_log.error("Error replacing text:", ex);
		}
	}

	private void find() {
		String findText = null;
		Object findObj = JOptionPane.showInputDialog(this.getRootPane(),
				"Find ", "Find", JOptionPane.INFORMATION_MESSAGE, null, null,
				this.getSelectedText());

		if (findObj instanceof String) {
			findText = ((String) findObj).toUpperCase();

			try {
				String text = getDocument().getText(0,
						getDocument().getLength());
				int startPosition = text.toUpperCase().indexOf(findText,
						m_startPosition);

				if (startPosition != -1) {
					this.setSelectionStart(startPosition);
					this.setSelectionEnd(startPosition + findText.length());
					this.setSelectionColor(Color.red);

					m_startPosition = startPosition + findText.length() + 1;
				} else {
					m_startPosition = 0;
				}
			} catch (BadLocationException ex) {
				_log.error("Error finding text:", ex);
			}
		}
	}

	/**
	 * getComponentContainer() -
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @param c
	 *            Component
	 * @return Component * @exception * @see
	 */
	private Component getComponentContainer(Component c) {
		Component topLevel = c;

		while ((topLevel != null) && !(topLevel instanceof JFrame)
				&& !(topLevel instanceof JDialog)) {
			topLevel = topLevel.getParent();
		}

		return topLevel;
	}

	/**
	 * 
	 * @version $Id: StreamEditorPane.java,v 1.10 2002/01/11 20:06:02 simon Exp
	 *          $
	 * @author Simon Allen
	 */
	class UndoHandler implements UndoableEditListener {
		/**
		 * Messaged when the Document has created an edit, the edit is added to
		 * <code>undo</code>, an instance of UndoManager.
		 * 
		 * @param e
		 *            UndoableEditEvent
		 * @see javax.swing.event.UndoableEditListener#undoableEditHappened(UndoableEditEvent)
		 */
		public void undoableEditHappened(UndoableEditEvent e) {
			undo.addEdit(e.getEdit());
			undoAction.update();
			redoAction.update();
		}
	}

	/**
	 * 
	 * @version $Id: StreamEditorPane.java,v 1.10 2002/01/11 20:06:02 simon Exp
	 *          $
	 * @author Simon Allen
	 */
	class UndoAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1274054242153556791L;

		public UndoAction() {
			super("Undo");

			this.setEnabled(false);
		}

		/**
		 * Method actionPerformed.
		 * 
		 * @param e
		 *            ActionEvent
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				_log.error("Error undoing text:", ex);
			}

			update();
			redoAction.update();
		}

		protected void update() {
			if (undo.canUndo()) {
				this.setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else {
				this.setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	/**
	 * 
	 * @version $Id: StreamEditorPane.java,v 1.10 2002/01/11 20:06:02 simon Exp
	 *          $
	 * @author Simon Allen
	 */
	class RedoAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 519980226158345645L;

		public RedoAction() {
			super("Redo");

			this.setEnabled(false);
		}

		/**
		 * Method actionPerformed.
		 * 
		 * @param e
		 *            ActionEvent
		 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				_log.error("Error redoing text:", ex);
			}

			update();
			undoAction.update();
		}

		protected void update() {
			if (undo.canRedo()) {
				this.setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				this.setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}
}
