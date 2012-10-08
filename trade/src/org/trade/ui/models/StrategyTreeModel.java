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
package org.trade.ui.models;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.trade.core.valuetype.ValueTypeException;
import org.trade.persistent.dao.Rule;
import org.trade.persistent.dao.Strategy;

/**
 */
public class StrategyTreeModel extends DefaultTreeModel implements Serializable {

	private static final long serialVersionUID = -5543286790183657148L;

	static DefaultMutableTreeNode m_root = new DefaultMutableTreeNode(
			"Strategies");
	private final Hashtable<MutableTreeNode, Object> m_nodeMap = new Hashtable<MutableTreeNode, Object>();

	/**
	 * Constructor for StrategyTreeModel.
	 * @param items List<Strategy>
	 * @throws ValueTypeException
	 */
	public StrategyTreeModel(List<Strategy> items) throws ValueTypeException {

		super(m_root);
		buildTree(items);
	}

	/**
	 * Method setData.
	 * @param strategies List<Strategy>
	 * @throws ValueTypeException
	 */
	public void setData(List<Strategy> strategies) throws ValueTypeException {
		((DefaultMutableTreeNode) getRoot()).removeAllChildren();
		m_nodeMap.clear();
		buildTree(strategies);
		fireTreeStructureChanged(this, new Object[] { getRoot() }, new int[0],
				new Object[0]);
	}

	/**
	 * Method buildTree.
	 * @param items List<Strategy>
	 * @throws ValueTypeException
	 */
	private void buildTree(List<Strategy> items) throws ValueTypeException {

		m_nodeMap.put(m_root, m_root.getRoot());
		for (Iterator<Strategy> iter = items.iterator(); iter.hasNext();) {
			Strategy strategy = iter.next();
			addItem(strategy);
		}
	}

	/**
	 * Method addItem.
	 * @param item Strategy
	 * @throws ValueTypeException
	 */
	private void addItem(Strategy item) throws ValueTypeException {

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
		m_root.add(node);
		m_nodeMap.put(node, item);
		for (Rule rule : item.getRules()) {
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(rule);
			m_nodeMap.put(childNode, rule);
			node.add(childNode);
		}
	}

	/**
	 * Method getNode.
	 * @param treeNode Object
	 * @return Object
	 */
	public Object getNode(Object treeNode) {
		return m_nodeMap.get(treeNode);
	}
}
