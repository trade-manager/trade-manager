package org.trade.ui.base;

import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 */
public class Tree extends JTree {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7628661248589428064L;

	/**
	 * Constructor for Tree.
	 * 
	 * @param model
	 *            DefaultTreeModel
	 */
	public Tree(DefaultTreeModel model) {
		super(model);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setRowHeight(18);
		this.setFont(new Font("dialog", Font.PLAIN, 12));
		this.setExpandsSelectedPaths(true);
	}

	/**
	 * Method findTreePathByObject.
	 * 
	 * @param tofind
	 *            Object
	 * @return TreePath
	 */
	public TreePath findTreePathByObject(Object tofind) {
		return findTreePathByObject(this.getModel(), (TreeNode) this.getModel().getRoot(), this.getPathForRow(0),
				tofind);
	}

	/**
	 * Method findTreePathByObject.
	 * 
	 * @param model
	 *            TreeModel
	 * @param base
	 *            TreeNode
	 * @param parent
	 *            TreePath
	 * @param tofind
	 *            Object
	 * @return TreePath
	 */
	private TreePath findTreePathByObject(TreeModel model, TreeNode base, TreePath parent, Object tofind) {
		int childCount = model.getChildCount(base);
		for (int i = 0; i < childCount; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) model.getChild(base, i);
			if (child.getUserObject().equals(tofind)) {
				return parent.pathByAddingChild(child);
			}

			if (!model.isLeaf(child)) {
				TreePath foundTreePath = findTreePathByObject(model, child, parent.pathByAddingChild(child), tofind);
				if (null != foundTreePath)
					return foundTreePath;
			}
		}
		return null;
	}
}
