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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 */
public class DOMTree extends JTree implements DragSourceListener,
		DragGestureListener, Autoscroll, TreeModelListener,
		TreeSelectionListener, MouseListener {

	private static final long serialVersionUID = -8742998183708844989L;

	private final static Logger _log = LoggerFactory.getLogger(DOMTree.class);
	/** Stores the selected node info */
	protected TreePath m_selectedTreePath = null;
	protected DefaultMutableTreeNode m_selectedNode = null;
	/** Variables needed for DnD */
	private DragSource m_dragSource = null;
	private static final int AUTOSCROLL_MARGIN = 12;
	private BufferedImage m_imgGhost; // The 'drag image'
	private Point m_ptOffset = new Point(); // Where, in the drag image, the
	private DOMTreeModel m_model = null;
	private JPopupMenu popup = null;
	private boolean m_dirty = false;
	private BasePanel m_basePanel = null;

	/**
	 * Constructor for DOMTree.
	 * @param basePanel BasePanel
	 */
	public DOMTree(BasePanel basePanel) {
		this(null, basePanel);
		this.addMouseListener(this);
	}

	/** Constructs a tree with the specified document. * @param document Document
	 * @param basePanel BasePanel
	 */
	public DOMTree(Document document, BasePanel basePanel) {
		super();

		m_basePanel = basePanel;
		m_model = new DOMTreeModel(document);

		this.setModel(m_model);
		m_model.addTreeModelListener(this);
		// set tree properties
		setRootVisible(false);

		XMLTreeCellRenderer xMLTreeCellRenderer = new XMLTreeCellRenderer();

		setCellRenderer(xMLTreeCellRenderer);
		setCellEditor(new XMLTreeCellEditor(this, xMLTreeCellRenderer));
		this.setEditable(true);
		addTreeSelectionListener(this);
		m_dragSource = DragSource.getDefaultDragSource();

		DragGestureRecognizer dgr = m_dragSource
				.createDefaultDragGestureRecognizer(this, // DragSource
						DnDConstants.ACTION_COPY_OR_MOVE, // specifies valid
						// actions
						this // DragGestureListener
				);

		/*
		 * Eliminates right mouse clicks as valid actions - useful especially if
		 * you implement a JPopupMenu for the JTree
		 */

		dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);

		/*
		 * First argument: Component to associate the target with Second
		 * argument: DropTargetListener
		 */

		// Also, make this JTree a drag target
		DropTarget dropTarget = new DropTarget(this, new CDropTargetListener());

		dropTarget.setDefaultActions(DnDConstants.ACTION_COPY_OR_MOVE);
		// unnecessary, but gives FileManager look
		putClientProperty("JTree.lineStyle", "Angled");
		this.addMouseListener(this);
	}

	//
	// Public methods
	//
	/** Sets the document. * @param document Document
	 */
	public void setDocument(Document document) {
		((DOMTreeModel) getModel()).setDocument(document);
		expandRow(0);
	}

	/** Returns the document. * @return Document
	 */
	public Document getDocument() {
		return ((DOMTreeModel) getModel()).getDocument();
	}

	/** get the org.w3c.Node for a MutableTreeNode. * @param treeNode Object
	 * @return Node
	 */
	public Node getNode(Object treeNode) {
		return ((DOMTreeModel) getModel()).getNode(treeNode);
	}

	/** Returns The selected node * @return DefaultMutableTreeNode
	 */
	public DefaultMutableTreeNode getSelectedNode() {
		return m_selectedNode;
	}

	/**
	 * Method isDirty.
	 * @return boolean
	 */
	public boolean isDirty() {
		return m_dirty;
	}

	// Ok, weve been told to scroll because the mouse cursor is in our
	// scroll zone.
	/**
	 * Method autoscroll.
	 * @param pt Point
	 * @see java.awt.dnd.Autoscroll#autoscroll(Point)
	 */
	public void autoscroll(Point pt) {
		// Figure out which row were on.
		int nRow = getRowForLocation(pt.x, pt.y);

		// If we are not on a row then ignore this autoscroll request
		if (nRow < 0) {
			return;
		}

		Rectangle raOuter = getBounds();

		// Now decide if the row is at the top of the screen or at the
		// bottom. We do this to make the previous row (or the next
		// row) visible as appropriate. If were at the absolute top or
		// bottom, just return the first or last row respectively.
		nRow = ((pt.y + raOuter.y) <= AUTOSCROLL_MARGIN) // Is row at top of
		// screen?
		? (nRow <= 0 ? 0 : nRow - 1) // Yes, scroll up one row
				: (nRow < (getRowCount() - 1) ? nRow + 1 : nRow); // No, scroll
		// down one row

		scrollRowToVisible(nRow);
	}

	// Calculate the insets for the *JTREE*, not the viewport
	// the tree is in. This makes it a bit messy.
	/**
	 * Method getAutoscrollInsets.
	 * @return Insets
	 * @see java.awt.dnd.Autoscroll#getAutoscrollInsets()
	 */
	public Insets getAutoscrollInsets() {
		Rectangle raOuter = getBounds();
		Rectangle raInner = getParent().getBounds();

		return new Insets((raInner.y - raOuter.y) + AUTOSCROLL_MARGIN,
				(raInner.x - raOuter.x) + AUTOSCROLL_MARGIN, (raOuter.height
						- raInner.height - raInner.y)
						+ raOuter.y + AUTOSCROLL_MARGIN, (raOuter.width
						- raInner.width - raInner.x)
						+ raOuter.x + AUTOSCROLL_MARGIN);
	}

	// Use this method if you want to see the boundaries of the
	// autoscroll active region. Toss it out, otherwise.

	/**
	 * Method paintComponent.
	 * @param g Graphics
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Rectangle raOuter = getBounds();
		Rectangle raInner = getParent().getBounds();

		g.setColor(Color.red);
		g.drawRect(-raOuter.x + 12, -raOuter.y + 12, raInner.width - 24,
				raInner.height - 24);
	}

	// TreeModelListener interface...
	/**
	 * Method treeNodesChanged.
	 * @param e TreeModelEvent
	 * @see javax.swing.event.TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e) {
	}

	/**
	 * Method treeNodesInserted.
	 * @param e TreeModelEvent
	 * @see javax.swing.event.TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e) {
		// We need to reset the selection path to the node just inserted
		int nChildIndex = e.getChildIndices()[0];
		TreePath pathParent = e.getTreePath();

		setSelectionPath(getChildPath(pathParent, nChildIndex));
	}

	/**
	 * Method treeNodesRemoved.
	 * @param e TreeModelEvent
	 * @see javax.swing.event.TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e) {
	}

	/**
	 * Method treeStructureChanged.
	 * @param e TreeModelEvent
	 * @see javax.swing.event.TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {
	}

	// More helpers...
	/**
	 * Method getChildPath.
	 * @param pathParent TreePath
	 * @param nChildIndex int
	 * @return TreePath
	 */
	private TreePath getChildPath(TreePath pathParent, int nChildIndex) {
		TreeModel model = getModel();

		return pathParent.pathByAddingChild(model.getChild(
				pathParent.getLastPathComponent(), nChildIndex));
	}

	/**
	 * Method isRootPath.
	 * @param path TreePath
	 * @return boolean
	 */
	private boolean isRootPath(TreePath path) {
		return isRootVisible() && (getRowForPath(path) == 0);
	}

	/**
	 * Method mousePressed.
	 * @param evt MouseEvent
	 * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			createPopup(evt.getPoint());
		}
	}

	/**
	 * Method mouseReleased.
	 * @param evt MouseEvent
	 * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
	 */
	public void mouseReleased(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			createPopup(evt.getPoint());
		}
	}

	/**
	 * Method mouseClicked.
	 * @param evt MouseEvent
	 * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
	 */
	public void mouseClicked(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			if (this.isEnabled()) {
				createPopup(evt.getPoint());
			}
		}
	}

	/**
	 * Method mouseEntered.
	 * @param evt MouseEvent
	 * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
	 */
	public void mouseEntered(MouseEvent evt) {
	}

	/**
	 * Method mouseExited.
	 * @param evt MouseEvent
	 * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
	 */
	public void mouseExited(MouseEvent evt) {
	}

	/**
	 * Method createPopup.
	 * @param point Point
	 */
	protected void createPopup(Point point) {

		popup = new JPopupMenu();

		BaseMenuItem copy = new BaseMenuItem(m_basePanel,
				BaseUIPropertyCodes.COPY);

		popup.add(copy);

		BaseMenuItem cut = new BaseMenuItem(m_basePanel, BaseUIPropertyCodes.CUT);

		popup.add(cut);

		BaseMenuItem paste = new BaseMenuItem(m_basePanel,
				BaseUIPropertyCodes.PASTE);

		popup.add(paste);
		// Try to make the popup lightweight
		point = getSuitableLocation(point, popup.getPreferredSize(), this);

		popup.show(this, point.x, point.y);
	}

	/**
	 * Method getComponentContainer.
	 * @param c Component
	 * @return Component
	 */
	private Component getComponentContainer(Component c) {
		Component topLevel = c;

		while ((topLevel != null) && !(topLevel instanceof javax.swing.JFrame)
				&& !(topLevel instanceof javax.swing.JDialog)) {
			topLevel = topLevel.getParent();
		}

		return topLevel;
	}

	/**
	 * Method getSuitableLocation.
	 * @param point Point
	 * @param d Dimension
	 * @param c Component
	 * @return Point
	 */
	protected Point getSuitableLocation(Point point, Dimension d, Component c) {
		// First locate the parent JFrame or JDialog
		Component topLevel = this.getComponentContainer(c);

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

	/** DragGestureListener interface method * @param e DragGestureEvent
	 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(DragGestureEvent)
	 */
	public void dragGestureRecognized(DragGestureEvent e) {
		DefaultMutableTreeNode dragNode = getSelectedNode();
		Node node = getNode(dragNode);

		if ((dragNode != null) && !(node instanceof Element)) {
			Point ptDragOrigin = e.getDragOrigin();
			TreePath path = getPathForLocation(ptDragOrigin.x, ptDragOrigin.y);

			if (path == null) {
				return;
			}

			if (isRootPath(path)) {
				return; // Ignore user trying to drag the root node
			}

			// Work out the offset of the drag point from the TreePath bounding
			// rectangle origin
			Rectangle raPath = getPathBounds(path);

			m_ptOffset.setLocation(ptDragOrigin.x - raPath.x, ptDragOrigin.y
					- raPath.y);

			// Get the cell renderer (which is a XMLTreeCellRenderer) for the
			// path being dragged
			XMLTreeCellRenderer lbl = (XMLTreeCellRenderer) getCellRenderer()
					.getTreeCellRendererComponent(this, // tree
							path.getLastPathComponent(), // value
							false, // isSelected (dont want a colored
							// background)
							isExpanded(path), // isExpanded
							getModel().isLeaf(path.getLastPathComponent()), // isLeaf
							0, // row (not important for rendering)
							false // hasFocus (dont want a focus rectangle)
					);

			lbl.setSize((int) raPath.getWidth(), (int) raPath.getHeight()); // <--

			// Get a buffered image of the selection for dragging a ghost image
			m_imgGhost = new BufferedImage((int) raPath.getWidth(),
					(int) raPath.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);

			Graphics2D g2 = m_imgGhost.createGraphics();

			// Ask the cell renderer to paint itself into the BufferedImage
			g2.setComposite(AlphaComposite
					.getInstance(AlphaComposite.SRC, 0.5f)); // Make the
			// image
			// ghostlike
			lbl.paint(g2);

			// Now paint a gradient UNDER the ghosted JLabel text (but not under
			// the icon if any)
			// Note: this will need tweaking if your icon is not positioned to
			// the left of the text
			Icon icon = lbl.getIcon();
			int nStartOfText = (icon == null) ? 0 : icon.getIconWidth()
					+ lbl.getIconTextGap();

			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER,
					0.5f)); // Make the gradient ghostlike
			g2.setPaint(new GradientPaint(nStartOfText, 0,
					SystemColor.controlShadow, getWidth(), 0, new Color(255,
							255, 255, 0)));
			g2.fillRect(nStartOfText, 0, getWidth(), m_imgGhost.getHeight());
			g2.dispose();
			setSelectionPath(path); // Select this path in the tree

			StringData stringData = (StringData) dragNode.getUserObject();

			stringData.setPath(path);
			dragNode.setUserObject(stringData);

			m_selectedTreePath = path;

			// Get the Transferable Object
			Transferable transferable = (Transferable) dragNode.getUserObject();

			e.startDrag(null, m_imgGhost, new Point(5, 5), transferable, this);
		}
	}

	/** DragSourceListener interface method * @param dsde DragSourceDropEvent
	 * @see java.awt.dnd.DragSourceListener#dragDropEnd(DragSourceDropEvent)
	 */
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	/** DragSourceListener interface method * @param dsde DragSourceDragEvent
	 * @see java.awt.dnd.DragSourceListener#dragEnter(DragSourceDragEvent)
	 */
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	/** DragSourceListener interface method * @param dsde DragSourceDragEvent
	 * @see java.awt.dnd.DragSourceListener#dragOver(DragSourceDragEvent)
	 */
	public void dragOver(DragSourceDragEvent dsde) {
	}

	/** DragSourceListener interface method * @param dsde DragSourceDragEvent
	 * @see java.awt.dnd.DragSourceListener#dropActionChanged(DragSourceDragEvent)
	 */
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	/** DragSourceListener interface method * @param dsde DragSourceEvent
	 * @see java.awt.dnd.DragSourceListener#dragExit(DragSourceEvent)
	 */
	public void dragExit(DragSourceEvent dsde) {
	}

	/** TreeSelectionListener - sets selected node * @param evt TreeSelectionEvent
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent evt) {
		m_selectedTreePath = evt.getNewLeadSelectionPath();

		if (m_selectedTreePath == null) {
			m_selectedNode = null;
			return;
		}
		m_selectedNode = (DefaultMutableTreeNode) m_selectedTreePath
				.getLastPathComponent();
	}

	/**
	 * Method testDropTarget.
	 * @param destination TreePath
	 * @param dropper TreePath
	 * @return String
	 */
	private String testDropTarget(TreePath destination, TreePath dropper) {

		boolean destinationPathIsNull = destination == null;

		if (destinationPathIsNull) {
			return "Invalid drop location.";
		}

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) destination
				.getLastPathComponent();

		if (!node.getAllowsChildren()) {
			return "This node does not allow children";
		}

		if (destination.equals(dropper)) {
			return "Destination cannot be same as source";
		}

		if (dropper.isDescendant(destination)) {
			return "Destination node cannot be a descendant.";
		}

		if (dropper.getParentPath().equals(destination)) {
			return "Destination node cannot be a parent.";
		}

		return null;
	}

	/**
	 */
	static class DOMTreeModel extends DefaultTreeModel implements Serializable {

		private static final long serialVersionUID = 558225099585471370L;

		private Document document;

		private Hashtable<MutableTreeNode, Node> nodeMap = new Hashtable<MutableTreeNode, Node>();

		public DOMTreeModel() {
			this(null);
		}

		/**
		 * Constructor for DOMTreeModel.
		 * @param document Document
		 */
		public DOMTreeModel(Document document) {
			super(new DefaultMutableTreeNode());

			setDocument(document);
		}

		/**
		 * Method setDocument.
		 * @param document Document
		 */
		public synchronized void setDocument(Document document) {
			// save document
			this.document = document;

			// clear tree and re-populate
			((DefaultMutableTreeNode) getRoot()).removeAllChildren();
			nodeMap.clear();
			buildTree();
			fireTreeStructureChanged(this, new Object[] { getRoot() },
					new int[0], new Object[0]);
		} // setDocument(Document)

		/** Returns the document. * @return Document
		 */
		public Document getDocument() {
			return document;
		}

		/** get the org.w3c.Node for a MutableTreeNode. * @param treeNode Object
		 * @return Node
		 */
		public Node getNode(Object treeNode) {
			return nodeMap.get(treeNode);
		}

		private void buildTree() {
			// is there anything to do?
			if (document == null) {
				return;
			}

			// iterate over children of this node
			NodeList nodes = document.getChildNodes();
			int len = (nodes != null) ? nodes.getLength() : 0;
			MutableTreeNode root = (MutableTreeNode) getRoot();

			for (int i = 0; i < len; i++) {
				Node node = nodes.item(i);

				switch (node.getNodeType()) {

				case Node.DOCUMENT_NODE: {
					root = insertDocumentNode(node, root);

					break;
				}
				case Node.ELEMENT_NODE: {
					insertElementNode(node, root);

					break;
				}
				default:
				}
			}
		}

		/** Inserts a node and returns a reference to the new node. * @param what String
		 * @param where MutableTreeNode
		 * @return MutableTreeNode
		 */
		private MutableTreeNode insertNode(String what, MutableTreeNode where) {
			MutableTreeNode node = new DefaultMutableTreeNode(new StringData(
					what));

			insertNodeInto(node, where, where.getChildCount());

			return node;
		} // insertNode(Node,MutableTreeNode):MutableTreeNode

		/** Inserts the document node. * @param what Node
		 * @param where MutableTreeNode
		 * @return MutableTreeNode
		 */
		private MutableTreeNode insertDocumentNode(Node what,
				MutableTreeNode where) {
			MutableTreeNode treeNode = insertNode("<" + what.getNodeName()
					+ '>', where);

			nodeMap.put(treeNode, what);

			return treeNode;
		}

		/** Inserts an element node. * @param what Node
		 * @param where MutableTreeNode
		 * @return MutableTreeNode
		 */
		private MutableTreeNode insertElementNode(Node what,
				MutableTreeNode where) {
			// build up name
			StringBuffer name = new StringBuffer();

			name.append('<');
			name.append(what.getNodeName());

			NamedNodeMap attrs = what.getAttributes();
			int attrCount = (attrs != null) ? attrs.getLength() : 0;

			for (int i = 0; i < attrCount; i++) {
				Node attr = attrs.item(i);

				name.append(' ');
				name.append(attr.getNodeName());
				name.append("=\"");
				name.append(attr.getNodeValue());
				name.append('"');
			}

			name.append('>');

			// insert element node
			MutableTreeNode element = insertNode(name.toString(), where);

			nodeMap.put(element, what);

			// gather up attributes and children nodes
			NodeList children = what.getChildNodes();
			int len = (children != null) ? children.getLength() : 0;

			for (int i = 0; i < len; i++) {
				Node node = children.item(i);

				switch (node.getNodeType()) {

				case Node.CDATA_SECTION_NODE: {
					insertCDataSectionNode(node, element); // Add a Section Node
					break;
				}
				case Node.TEXT_NODE: {
					insertTextNode(node, element);
					break;
				}
				case Node.ELEMENT_NODE: {
					insertElementNode(node, element);
					break;
				}
				}
			}
			return element;
		}

		/** Inserts a text node. * @param what Node
		 * @param where MutableTreeNode
		 * @return MutableTreeNode
		 */
		private MutableTreeNode insertTextNode(Node what, MutableTreeNode where) {
			String value = what.getNodeValue().trim();

			if (value.length() > 0) {
				MutableTreeNode treeNode = insertNode(value, where);

				nodeMap.put(treeNode, what);

				return treeNode;
			}

			return null;
		}

		/** Inserts a CData Section Node. * @param what Node
		 * @param where MutableTreeNode
		 * @return MutableTreeNode
		 */
		private MutableTreeNode insertCDataSectionNode(Node what,
				MutableTreeNode where) {
			StringBuffer CSectionBfr = new StringBuffer();

			// --- optional --- CSectionBfr.append( "<![CDATA[" );
			CSectionBfr.append(what.getNodeValue());

			// --- optional --- CSectionBfr.append( "]]>" );
			if (CSectionBfr.length() > 0) {
				MutableTreeNode treeNode = insertNode(CSectionBfr.toString(),
						where);
				nodeMap.put(treeNode, what);
				return treeNode;
			}
			return null;
		}
	}

	/**
	 */
	class CDropTargetListener implements DropTargetListener {
		// Fields...
		private TreePath _pathLast = null;
		private Rectangle2D _raCueLine = new Rectangle2D.Float();
		private Rectangle2D _raGhost = new Rectangle2D.Float();
		private Color _colorCueLine;
		private Point _ptLast = new Point();
		private Timer _timerHover;
		private int _nLeftRight = 0; // Cumulative left/right mouse movement

		private BufferedImage _imgRight = new CArrowImage(15, 15,
				CArrowImage.ARROW_RIGHT);

		private BufferedImage _imgLeft = new CArrowImage(15, 15,
				CArrowImage.ARROW_LEFT);

		public int _nShift = 0;

		public CDropTargetListener() {
			_colorCueLine = new Color(SystemColor.controlShadow.getRed(),
					SystemColor.controlShadow.getGreen(),
					SystemColor.controlShadow.getBlue(), 64);
			// Set up a hover timer, so that a node will be automatically
			// expanded or collapsed
			// if the user lingers on it for more than a short time
			_timerHover = new Timer(1000, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					_nLeftRight = 0; // Reset left/right movement trend

					if (isRootPath(_pathLast)) {
						return; // Do nothing if we are hovering over the root
						// node
					}

					if (isExpanded(_pathLast)) {
						collapsePath(_pathLast);
					} else {
						expandPath(_pathLast);
					}
				}
			});
			_timerHover.setRepeats(false); // Set timer to one-shot mode
		}

		// DropTargetListener interface
		/**
		 * Method dragEnter.
		 * @param e DropTargetDragEvent
		 * @see java.awt.dnd.DropTargetListener#dragEnter(DropTargetDragEvent)
		 */
		public void dragEnter(DropTargetDragEvent e) {
			if (!isDragAcceptable(e)) {
				e.rejectDrag();
			} else {
				e.acceptDrag(e.getDropAction());
			}
		}

		/**
		 * Method dragExit.
		 * @param e DropTargetEvent
		 * @see java.awt.dnd.DropTargetListener#dragExit(DropTargetEvent)
		 */
		public void dragExit(DropTargetEvent e) {
			if (!DragSource.isDragImageSupported()) {
				repaint(_raGhost.getBounds());
			}
		}

		/**
		 * This is where the ghost image is drawn
		 * @param e DropTargetDragEvent
		 * @see java.awt.dnd.DropTargetListener#dragOver(DropTargetDragEvent)
		 */
		public void dragOver(DropTargetDragEvent e) {
			// Even if the mouse is not moving, this method is still invoked 10
			// times per second
			Point pt = e.getLocation();

			if (pt.equals(_ptLast)) {
				return;
			}

			// Try to determine whether the user is flicking the cursor right or
			// left
			int nDeltaLeftRight = pt.x - _ptLast.x;

			if (((_nLeftRight > 0) && (nDeltaLeftRight < 0))
					|| ((_nLeftRight < 0) && (nDeltaLeftRight > 0))) {
				_nLeftRight = 0;
			}

			_nLeftRight += nDeltaLeftRight;
			_ptLast = pt;

			Graphics2D g2 = (Graphics2D) getGraphics();

			// If a drag image is not supported by the platform, then draw my
			// own drag image
			if (!DragSource.isDragImageSupported()) {
				paintImmediately(_raGhost.getBounds()); // Rub out the last
				// ghost image and cue
				// line
				// And remember where we are about to draw the new ghost image
				_raGhost.setRect(pt.x - m_ptOffset.x, pt.y - m_ptOffset.y,
						m_imgGhost.getWidth(), m_imgGhost.getHeight());
				g2.drawImage(m_imgGhost, AffineTransform.getTranslateInstance(
						_raGhost.getX(), _raGhost.getY()), null);
			} else
			// Just rub out the last cue line
			{
				paintImmediately(_raCueLine.getBounds());
			}

			TreePath path = getClosestPathForLocation(pt.x, pt.y);

			if (!(path == _pathLast)) {
				_nLeftRight = 0; // We've moved up or down, so reset
				// left/right movement trend
				_pathLast = path;

				_timerHover.restart();
			}

			// In any case draw (over the ghost image if necessary) a cue line
			// indicating where a drop will occur
			Rectangle raPath = getPathBounds(path);

			_raCueLine.setRect(0, raPath.y + (int) raPath.getHeight(),
					getWidth(), 2);
			g2.setColor(_colorCueLine);
			g2.fill(_raCueLine);

			// Now superimpose the left/right movement indicator if necessary
			if (_nLeftRight > 20) {
				g2.drawImage(
						_imgRight,
						AffineTransform.getTranslateInstance(pt.x
								- m_ptOffset.x, pt.y - m_ptOffset.y), null);

				_nShift = +1;
			} else if (_nLeftRight < -20) {
				g2.drawImage(
						_imgLeft,
						AffineTransform.getTranslateInstance(pt.x
								- m_ptOffset.x, pt.y - m_ptOffset.y), null);

				_nShift = -1;
			} else {
				_nShift = 0;
			}

			// And include the cue line in the area to be rubbed out next time
			_raGhost = _raGhost.createUnion(_raCueLine);

			// Do this if you want to prohibit dropping onto the drag source
			if (path.equals(m_selectedTreePath)) {
				e.rejectDrag();
			} else {
				e.acceptDrag(e.getDropAction());
			}
		}

		/**
		 * Method dropActionChanged.
		 * @param e DropTargetDragEvent
		 * @see java.awt.dnd.DropTargetListener#dropActionChanged(DropTargetDragEvent)
		 */
		public void dropActionChanged(DropTargetDragEvent e) {
			if (!isDragAcceptable(e)) {
				e.rejectDrag();
			} else {
				e.acceptDrag(e.getDropAction());
			}
		}

		// DropTargetListener interface method - What we do when drag is
		// released
		/**
		 * Method drop.
		 * @param e DropTargetDropEvent
		 * @see java.awt.dnd.DropTargetListener#drop(DropTargetDropEvent)
		 */
		public void drop(DropTargetDropEvent e) {
			try {
				_timerHover.stop(); // Prevent hover timer from doing an
				// unwanted expandPath or collapsePath

				Transferable tr = e.getTransferable();

				// flavor not supported, reject drop
				if (!tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					e.rejectDrop();
				}

				// cast into appropriate data type
				StringData childInfo = (StringData) tr
						.getTransferData(DataFlavor.stringFlavor);

				_log.debug("String node value " + childInfo);
				// get new parent node
				Point loc = e.getLocation();
				TreePath destinationPath = getPathForLocation(loc.x, loc.y);
				final String msg = testDropTarget(destinationPath,
						m_selectedTreePath);

				if (msg != null) {
					e.rejectDrop();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							// JOptionPane.showMessageDialog(this.Parent, msg,
							// "Error Dialog", JOptionPane.ERROR_MESSAGE);
							_log.error(msg);
						}
					});

					return;
				}

				DefaultMutableTreeNode newParent = (DefaultMutableTreeNode) destinationPath
						.getLastPathComponent();
				// get old parent node
				DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) getSelectedNode()
						.getParent();
				// DefaultMutableTreeNode child = new DefaultMutableTreeNode(
				// childInfo);
				int action = e.getDropAction();
				boolean copyAction = (action == DnDConstants.ACTION_COPY);

				// make new child node
				try {
					MutableTreeNode selectedTreeNode = getSelectedNode();

					if (null != m_model.getNode(selectedTreeNode)) {
						m_model.insertTextNode(
								m_model.getNode(selectedTreeNode), newParent);
						m_model.removeNodeFromParent(selectedTreeNode);
					}

					if (copyAction) {
						e.acceptDrop(DnDConstants.ACTION_COPY);
					} else {
						oldParent.remove(getSelectedNode());
						e.acceptDrop(DnDConstants.ACTION_MOVE);
					}
				} catch (java.lang.IllegalStateException ils) {
					e.rejectDrop();
				}

				e.getDropTargetContext().dropComplete(true);

				// expand nodes appropriately - this probably isnt the best
				// way...
				DefaultTreeModel model = (DefaultTreeModel) getModel();

				model.reload(oldParent);
				model.reload(newParent);

				TreePath parentPath = new TreePath(newParent.getPath());

				expandPath(parentPath);
			} catch (IOException io) {
				e.rejectDrop();
			} catch (UnsupportedFlavorException ufe) {
				e.rejectDrop();
			}
		} // end of method

		// Helpers...
		/**
		 * Method isDragAcceptable.
		 * @param e DropTargetDragEvent
		 * @return boolean
		 */
		public boolean isDragAcceptable(DropTargetDragEvent e) {
			// Only accept COPY or MOVE gestures (ie LINK is not supported)
			if ((e.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
				return false;
			}

			// Only accept this particular flavor
			if (!e.isDataFlavorSupported(StringData.TREEPATH_FLAVOR)) {
				return false;
			}

			// Do this if you want to prohibit dropping onto the drag source...
			Point pt = e.getLocation();
			TreePath path = getClosestPathForLocation(pt.x, pt.y);

			if (path.equals(m_selectedTreePath)) {
				return false;
			}

			// Do this if you want to select the best flavor on offer...
			DataFlavor[] flavors = e.getCurrentDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor
						.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
					return true;
				}
			}

			return true;
		}

		/**
		 * Method isDropAcceptable.
		 * @param e DropTargetDropEvent
		 * @return boolean
		 */
		public boolean isDropAcceptable(DropTargetDropEvent e) {
			// Only accept COPY or MOVE gestures (ie LINK is not supported)
			if ((e.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
				return false;
			}

			// Only accept this particular flavor
			if (!e.isDataFlavorSupported(StringData.TREEPATH_FLAVOR)) {
				return false;
			}

			// Do this if you want to prohibit dropping onto the drag source...
			Point pt = e.getLocation();
			TreePath path = getClosestPathForLocation(pt.x, pt.y);

			if (path.equals(m_selectedTreePath)) {
				return false;
			}

			// Do this if you want to select the best flavor on offer...
			DataFlavor[] flavors = e.getCurrentDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor
						.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) {
					return true;
				}
			}

			return true;
		}
	}

	/*
	 * The XMLTreeCellRenderer is an inner class which enables the highlighting
	 * of errors in the tree and shows the gender values as different icons.
	 */

	/**
	 */
	class XMLTreeCellEditor extends DefaultTreeCellEditor {
		Image openFolder = DefaultImages.createOpenFolderImage();
		Image closedFolder = DefaultImages.createClosedFolderImage();
		Image leafImage = DefaultImages.createLeafImage();

		/**
		 * Constructor for XMLTreeCellEditor.
		 * @param tree JTree
		 * @param renderer DefaultTreeCellRenderer
		 */
		public XMLTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
			super(tree, renderer);
		}

		/**
		 * Method getTreeCellEditorComponent.
		 * @param tree JTree
		 * @param value Object
		 * @param selected boolean
		 * @param expanded boolean
		 * @param leaf boolean
		 * @param row int
		 * @return Component
		 * @see javax.swing.tree.TreeCellEditor#getTreeCellEditorComponent(JTree, Object, boolean, boolean, boolean, int)
		 */
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row) {
			Node node = ((DOMTree) tree).getNode(value);
			Component comp = super.getTreeCellEditorComponent(tree, value,
					selected, expanded, leaf, row);

			_log.debug("something to edit :" + node);

			return comp;
		}
	}

	/*
	 * The XMLTreeCellRenderer is an inner class which enables the highlighting
	 * of errors in the tree and shows the gender values as different icons.
	 */

	/**
	 */
	class XMLTreeCellRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 7664391812385841364L;
		Image openFolder = DefaultImages.createOpenFolderImage();
		Image closedFolder = DefaultImages.createClosedFolderImage();
		Image leafImage = DefaultImages.createLeafImage();

		/**
		 * Method getTreeCellRendererComponent.
		 * @param tree JTree
		 * @param value Object
		 * @param selected boolean
		 * @param expanded boolean
		 * @param leaf boolean
		 * @param row int
		 * @param hasFocus boolean
		 * @return Component
		 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean)
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			Node node = ((DOMTree) tree).getNode(value);
			Component comp = super.getTreeCellRendererComponent(tree, value,
					selected, expanded, leaf, row, hasFocus);

			if (selected) {
				comp.setBackground(Color.blue);
			}

			if (node != null) {
				_log.debug("something to render :" + node + " class: "
						+ node.getClass().getName());

				if (!(node instanceof Element) /* leaf */) {
					setIcon(new ImageIcon(leafImage));
				} else if (expanded) {
					setIcon(new ImageIcon(openFolder));
				} else {
					setIcon(new ImageIcon(closedFolder));
				}
			}

			if ((node != null) && (node instanceof Element)) {
				Element txNode = (Element) node;
				Attr txAtt = txNode.getAttributeNode("gender");

				if (txAtt != null) {
					if (txAtt.getValue().equals("male")) {
						setIcon(new ImageIcon("male.gif"));
					} else if (txAtt.getValue().equals("female")) {
						setIcon(new ImageIcon("female.gif"));
					}
				}
			}
			return comp;
		}
	}
}
