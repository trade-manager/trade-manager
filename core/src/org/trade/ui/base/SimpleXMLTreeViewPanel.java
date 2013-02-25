package org.trade.ui.base;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import org.trade.core.xml.DOMParserSaveEncoding;
import org.w3c.dom.Document;

public class SimpleXMLTreeViewPanel extends BasePanel {

	private static final long serialVersionUID = -6919104650081619931L;

	/** Default parser name. */
	static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.DOMParser";

	static int WARNING = 0;
	static int ERROR = 1;
	static int FATAL_ERROR = 2;

	static final String title = "TreeViewer";
	static final String openString = "Open";
	static final String quitString = "Quit";
	static final String reloadString = "Reload current XML file";
	static final String expandString = "Expand Tree";
	static final String collapseString = "Collapse Tree";

	//
	// Data
	//

	// ErrorStorer ef = null;
	String fname = null;

	// DOMTree m_tree = null;
	Vector<String> textLine = null;

	// DOMParserSaveEncoding parser = null;
	Image openFolder = null;

	Image closedFolder = null;

	Image leafImage = null;

	BorderLayout borderLayout1 = new BorderLayout();

	// JTextArea messageText = new JTextArea();

	/**
	 * Constructor
	 */
	public SimpleXMLTreeViewPanel(String xml) {
		this.setLayout(borderLayout1);

		openFolder = DefaultImages.createOpenFolderImage();
		closedFolder = DefaultImages.createClosedFolderImage();
		leafImage = DefaultImages.createLeafImage();

		this.add(createUI(xml), BorderLayout.CENTER);

	}

	/**
	 * create and return the entire UI from the root TreeNode
	 */
	JComponent createUI(String xml) {

		// create the JTree and scroll pane.
		final JPanel treePanel = new JPanel(new BorderLayout());

		final DOMTree m_tree = new DOMTree(this);

		m_tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);

		// Listen for when the selection changes, call nodeSelected(node)
		m_tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {

				// nodeSelected(node, m_tree);
			}
		});
		m_tree.setRowHeight(18);
		m_tree.setFont(new Font("dialog", Font.PLAIN, 12));

		treePanel.add(new JScrollPane(m_tree) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8726326914798956438L;

			@Override
			public Dimension getPreferredSize() {
				final Dimension size = SimpleXMLTreeViewPanel.this.getSize();
				return new Dimension(size.width / 2, (size.height * 3) / 5);
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(100, 10);
			}
		}, BorderLayout.CENTER);

		treePanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Tree View"),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)));

		// refreshUI loads everthything !
		refreshUI(xml, m_tree);

		return treePanel;
	}

	public void doOpen(String xml) {
		// refreshUI(xml);
	}

	/**
	 * refreshUI is called when we have a new filename to parse.
	 */
	void refreshUI(String xml, DOMTree m_tree) {

		if ((xml == null) || xml.equals("")) {
			setErrorMessage("Error", "No input XML  specified \n");
			return;
		}

		final Document newRoot = getRoot(xml);
		if (newRoot == null) {
			setErrorMessage("Error", "Unable to get new DOM Tree \n");
			return;
		}
		m_tree.setDocument(newRoot);

	}

	/**
	 * Invoke the Parser on fname and return the root TreeNode.
	 */
	public Document getRoot(String xml) {

		if ((xml == null) || xml.equals("")) {
			setErrorMessage("Error:",
					" Invalid XML document could not get ROOT");
			return null;
		}

		try {
			//
			// Reset the Error Storage and handling
			//
			final DOMParserSaveEncoding parser = new DOMParserSaveEncoding();
			final Document document = parser.parse(xml);
			/***/
			return document;
		} catch (final Exception e) {
			setErrorMessage("Error: Invalid XML document could not get ROOT", e);
		}
		return null;
	}

	public void setErrorMessage(String title, Throwable t) {
		JOptionPane.showMessageDialog(this.getRootPane(), t.getMessage(),
				title, JOptionPane.ERROR_MESSAGE);
	}

	public void setErrorMessage(String title, String message) {
		JOptionPane.showMessageDialog(this.getRootPane(), message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	public void doWindowOpen() {
	}

	public void doWindowClose() {

	}

	public void doWindowActivated() {
	}

	public boolean doWindowDeActivated() {
		return true;
	}
}
