/**
 *  Copyright 2023 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package org.paint.gui.familytree;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.gui.FamilyViews;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AnnotationDragEvent;
import org.paint.gui.event.AnnotationDragListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.CurationColorEvent;
import org.paint.gui.event.CurationColorListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.table.GeneTable;
import org.paint.main.PaintManager;

import com.sri.panther.paintCommon.Constant;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.AnnotationDisplayEvent;
import org.paint.gui.event.AnnotationDisplayListener;
import org.paint.gui.event.TermAncestorSelectionEvent;
import org.paint.gui.event.TermAncestorSelectionListener;
import org.paint.util.AnnotationUtil;

public class TreePanel extends JPanel 
implements MouseListener, 
MouseMotionListener, 
Scrollable, 
GeneSelectListener, 
CurationColorListener,
TermSelectionListener,
TermAncestorSelectionListener,
AnnotationChangeListener, 
AspectChangeListener, 
AnnotationDragListener,
AnnotationDisplayListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected boolean             first_time = true;
	protected int                 prevWidth = 0;
	protected static Logger logger = Logger.getLogger(TreePanel.class.getName());
	protected static final String TOOLTIP_FOREGROUND = "ToolTip.foreground";

	public static final String POPUP_MENU_COLLAPSE = "Collapse node";
	public static final String POPUP_MENU_EXPAND = "Expand node";
	public static final String POPUP_MENU_OUTPUT_SEQ = "Output seq ids for leaves";
	public static final String POPUP_MENU_REROOT = "Reroot to node";
	public static final String POPUP_MENU_RESET_ROOT = "Reset Root to Main";
	public static final String POPUP_MENU_PRUNE = "Prune";

	public static final int TREE_COLLAPSE_NONEXP_NODES = 201;
	public static final int TREE_EXPAND_ALL_NODES = 202;
	public static final int TREE_RESET_ROOT_TO_MAIN = 203;
	public static final int TREE_USE_DISTANCES = 204;
	public static final int TREE_SPECIES = 205;
	public static final int TREE_TOP = 206;
	public static final int TREE_BOTTOM = 207;

	private static final String OUTPUT_SEQ_INFO_TITLE = "#Descendant sequence information for node ";
	private static final String OUTPUT_SEQ_DELIM = Constant.STR_TAB;
	private static final String OUTPUT_SEQ_INFO_COLUMNS = "#Database id" + OUTPUT_SEQ_DELIM + "Sequence id";

	// indicates whether or not the y values need to be recalculated on next draw
	private boolean need_update = true;
	private Rectangle tree_rect = new Rectangle(0, 0, 0, 0);

	protected static final int LEFTMARGIN = 20;

	private TreeModel tree;

	private static Logger log = Logger.getLogger(TreePanel.class);

	/**
	 * Constructor declaration
	 *
	 * @see
	 */

	public TreePanel() {
		setBackground(Color.white);
		addMouseListener(this);
		addMouseMotionListener(this);
		EventManager manager = EventManager.inst();
		manager.registerGeneListener(this);
		manager.registerGeneAnnotationChangeListener(this);
		manager.registerAspectChangeListener(this);
		manager.registerTermListener(this);
                manager.registerTermAncestorListener(this);
		manager.registerCurationColorListener(this);
                manager.registerAnnotationDisplayListener(this);


		ToolTipManager.sharedInstance().registerComponent(this);
	}

	public void setTreeModel(TreeModel tree) {
		this.tree = tree;
		setNeedPositionUpdate();
	}

	public TreeModel getTreeModel() {
		return tree;
	}

	public GeneNode getRoot() {
		if (tree != null) {
			return tree.getRoot();
		} else
			return null;
	}

	public GeneNode getCurrentRoot(){
		if (tree != null) {
			return tree.getCurrentRoot();
		} else
			return null;
	}

	public List<GeneNode> getAllNodes() {
		if (tree != null) {
			return tree.getAllNodes();
		} else
			return null;
	}
        
        public void updateColoring(TreeModel.TreeColorSchema colSchema) {
            if (null == tree) {
                return;
            }
            tree.setTreeColorSchema(colSchema);
            repaint();
        }

	public void scaleTree(double scale){
		if (tree != null) {
			tree.scaleTree(scale);
			setNeedPositionUpdate();
		} else {
			Preferences.inst().setTree_distance_scaling(scale);
		}
	}

	public void speciesOrder() {
		if (tree != null) {
			tree.speciesOrder();
			setNeedPositionUpdate();
		}
	}

	public void descendentCountLadder(boolean most_leaves_at_top) {
		if (tree != null) {
			tree.descendentCountLadder(most_leaves_at_top);
			setNeedPositionUpdate();
		}
	}

	public List<GeneNode> getTerminusNodes() {
		if (tree != null) {
			return tree.getTerminusNodes();
		} else
			return null;
	}

	public void getDescendentList(GeneNode node, List<GeneNode> v) {
		if (tree != null) {
			tree.getDescendentList(node, v);
		}
	}

	public void getLeafDescendants(GeneNode node, Vector<GeneNode> leafList){
		if (tree != null) {
			tree.getLeafDescendants(node, leafList);
		}
	}

	public void adjustTree() {
		setNeedPositionUpdate();
	}

	public void expandAllNodes() {
		if (tree != null) {
			tree.expandAllNodes();
			setNeedPositionUpdate();
		}
	}

	public void collapseNonExperimental() {
		if (tree != null) {
			tree.collapseNonExperimental();
			setNeedPositionUpdate();
		}
	}

	public void resetRootToMain() {
		if (tree != null) {
			if (tree.resetRootToMain())
				setNeedPositionUpdate();
		}
	}

	public GeneNode getMRCA(GeneNode gene1, GeneNode gene2) {
		if (tree != null) {
			return tree.getMRCA(gene1, gene2);
		} else
			return null;
	}

	public GeneNode getTopLeafNode(GeneNode node) {
		if (tree != null) {
			return tree.getTopLeafNode(node);
		} else
			return null;
	}

	public GeneNode getBottomLeafNode(GeneNode node) {
		if (tree != null) {
			return tree.getBottomLeafNode(node);
		} else
			return null;
	}

	public void handlePruning(GeneNode node) {
		boolean shift = tree.handlePruning(node);
		if (shift) {
			setNeedPositionUpdate();
		}
	}

	// Override paintComponent method to draw tree image

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @see
	 */
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if (tree != null && g != null) {
			Rectangle r = ((JViewport) getParent()).getViewRect();
			paintTree(g, r);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 * @param r
	 *
	 * @see
	 */
	protected void paintTree(Graphics g, Rectangle r){
		GeneNode current_root = getCurrentRoot();
		if ((null == g) || (null == current_root)){
			return;
		}

		boolean use_distances = Preferences.inst().isUseDistances();
		if (need_update) {
			updateNodePositions(current_root, g, PaintManager.inst().getRowHeight(), use_distances);
		}
		Font  f = g.getFont();
		paintBranch(current_root, current_root, g, r, use_distances);
		g.setFont(f);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 * @param g
	 * @param r
	 *
	 * @see
	 */
	private void paintBranch(GeneNode current_root, GeneNode dsn, Graphics g, Rectangle r, boolean use_distances){
		if (null == dsn){
			return;
		}
		List<GeneNode>  topChildren = tree.getTopChildren(dsn);
		List<GeneNode>  bottomChildren = tree.getBottomChildren(dsn);

		// draw the children that are vertically above first
		if (!dsn.isTerminus()){
			if (null != topChildren){
				for (Iterator<GeneNode> it = topChildren.iterator(); it.hasNext();) {
					paintBranch(current_root, it.next(), g, r, use_distances);
				}
			}
		}

		// Add the parent
		dsn.drawMarker(current_root, g, ((current_root == dsn) && (current_root != tree.getRoot())), r);

		// Repeat for last half
		if (!dsn.isTerminus()){
			if (null != bottomChildren){
				for (Iterator<GeneNode> it = bottomChildren.iterator(); it.hasNext();) {
					paintBranch(current_root, it.next(), g, r, use_distances);
				}
			}
		}
	}

	// Methods used for node position

	/**
	 * Method declaration
	 *
	 * @param Graphics g
	 * @param int row_height
	 * @param boolean use_distances

	 * @see
	 */
	private void updateNodePositions(GeneNode current_root, Graphics g, int row_height, boolean use_distances) {
		int x = TreePanel.LEFTMARGIN + getNodeWidth(g, current_root);
		setNodeRectangle(current_root, row_height, x, 0, use_distances, g);
		tree_rect = calcTreeSize(g);
		revalidate();
		repaint();
		need_update = false;
	}

	protected void setNeedPositionUpdate() {
		need_update = true;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param g
	 *
	 * @return
	 *
	 * @see
	 */
	private Rectangle calcTreeSize(Graphics g) {
		tree_rect.setBounds(0, 0, 0, 0);
		List<GeneNode> terminus_nodes = tree.getTerminusNodes();
		GeneNode bottom_node =  terminus_nodes.get(terminus_nodes.size() - 1);
		Rectangle bottomRect = bottom_node.getScreenRectangle();

		// Set the height
		tree_rect.height = bottomRect.y;

		// Determine the widest point
		for (int i = 0; i < terminus_nodes.size(); i++){
			GeneNode dsn =  terminus_nodes.get(i);
			int x = dsn.getScreenRectangle().x;
			int width = getNodeWidth(g, dsn);
			if (tree_rect.width < x + width){
				tree_rect.width = x + width;
			}
		}
		return tree_rect;
	}

	protected Rectangle getTreeSize(Graphics g) {
		if (tree != null) {
			if (need_update) {
				updateNodePositions(getCurrentRoot(), g, PaintManager.inst().getRowHeight(), Preferences.inst().isUseDistances());
			}
		}
		return tree_rect;
	}


	private int setNodeRectangle(GeneNode node, int row_height, float base_x, float tree_depth, boolean use_distances, Graphics g) {
		int x;
		int y;
		int width;
		int height;
		/*
		 * Calculate the left-right x position first
		 */
		double scale = tree.getDistanceScaling();
		Float f;
		if (!use_distances) {
			f = new Float(base_x + (tree_depth * scale));
		}
		else {
			f = new Float(base_x + (Math.abs(node.getDistanceFromParent()) * scale));
		}
		x = f.intValue();

		/*
		 * Calculate width using a method call because it's used more than once
		 */
		width = getNodeWidth(g, node);

		/* 
		 * Then calculate vertical y position
		 */
		if (node.isTerminus()) {
			int margin = PaintManager.inst().getTopMargin() + 8;
			int row = tree.getTerminusNodes().indexOf(node);
			GeneTable mate = PaintManager.inst().getGeneTable();
			Rectangle position = mate.getCellRect(row, 0, false);
			double better_y = margin + position.getY();
			y = (int) Math.round(better_y);
		} else {
			List<GeneNode>  children = node.getChildren();

			if (null == children){
				y = 0;
			}
			int top_y = -1;
			int bottom_y = -1;
			float child_depth = tree_depth + 1;
			float child_base = f + width;
			for (GeneNode child : children) {
				int child_y = setNodeRectangle(child, row_height, child_base, child_depth, use_distances, g);
				if (top_y < 0 || child_y < top_y)
					top_y = child_y;
				if (bottom_y < 0 || child_y > bottom_y)
					bottom_y = child_y;
			}
			y = ((top_y + bottom_y) / 2) - (row_height / 2);
		}
		/*
		 * Calculate the height
		 */
		if (node.isTerminus() && node.getNodeLabel() != null && !node.getNodeLabel().equals(""))
			height = row_height;
		else
			height =  GeneNode.GLYPH_DIAMETER;

		node.setNodeArea(x, y, height, width);

		return y + (height / 2);
	}

	private int getNodeWidth(Graphics g, GeneNode node) {
		int width = getTextWidth(g, node);
		if (width == 0)
			width = GeneNode.GLYPH_DIAMETER;
		else
			width += GeneNode.GLYPH_DIAMETER + GeneNode.nodeToTextDist;
		return width;
	}

	private int getTextWidth(Graphics g, GeneNode node) {
		int width = 0;
		if (node.isTerminus()) {
			String s = node.getNodeLabel();
			if (s != null && !s.equals(Constant.STR_EMPTY)) {
				FontMetrics fm = g.getFontMetrics(Preferences.inst().getFont());
				width = (fm.stringWidth(s));
			}
		}
		return width;
	}

	// MouseListener implementation methods

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseClicked(MouseEvent e) {

		// Called just after the user clicks the listened-to component.
		setToolTipText(Constant.STR_EMPTY);
		int modifiers = e.getModifiers();
		if ((modifiers & InputEvent.BUTTON1_MASK) != 0 &&
				(modifiers      & InputEvent.BUTTON3_MASK) == 0) {
			if (tree != null) {
				GeneNode node = getClickedInNodeArea(e.getPoint());
				if (node != null) {
//                                    ArrayList<GeneNode> selection = new ArrayList<GeneNode>();
//                                    selection.add(node);
//                                    GeneSelectEvent ge = new GeneSelectEvent (this, selection, node);
//                                    EventManager.inst().fireGeneEvent(ge);		                                    
					boolean new_select = !node.isSelected();
					if (node.getParent() != null) {
						new_select |= node.isSelected() && node.getParent().isSelected();
                                        }
					if (new_select) {
						ArrayList<GeneNode> selection = new ArrayList<GeneNode>();
						selection.add(0,node);
						tree.getDescendentList(node, selection);
						GeneSelectEvent ge = new GeneSelectEvent (this, selection, node);
						EventManager.inst().fireGeneEvent(ge);
					} else {
						ArrayList<GeneNode> selection = new ArrayList<GeneNode>();
						GeneSelectEvent ge = new GeneSelectEvent (this, selection, node);
						EventManager.inst().fireGeneEvent(ge);						
					}
				}
			}
		}
		if (InputEvent.BUTTON3_MASK == (modifiers & InputEvent.BUTTON3_MASK) || 
				(((modifiers & InputEvent.BUTTON1_MASK) != 0 && (modifiers      & InputEvent.BUTTON3_MASK) == 0) && (true == e.isMetaDown())) ){
			JPopupMenu popup = createPopupMenu(e);
			if (popup != null)
				showPopup(popup, e.getComponent(), new Point(e.getX(), e.getY()));
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param popup
	 * @param comp
	 * @param position
	 *
	 * @see
	 */
	private void showPopup(JPopupMenu popup, Component comp, Point position){

		// Get root frame
		Component root = comp;

		while ((root != null) && (false == (root instanceof JFrame))){
			root = root.getParent();
		}
		if (root != null){
			SwingUtilities.convertPointToScreen(position, comp);
			Point     rootPos = root.getLocationOnScreen();
			Dimension rootSize = root.getSize();
			Dimension popSize = popup.getPreferredSize();
			int       x = position.x;
			int       y = position.y;
			Insets    insets = popup.getInsets();

			if (position.x + popSize.width + (insets.left + insets.right) > rootPos.x + rootSize.width){
				x = rootPos.x + rootSize.width - popSize.width - insets.left;
			}
			if (position.y + popSize.height + (insets.top + insets.bottom) > rootPos.y + rootSize.height){
				y = rootPos.y + rootSize.height - popSize.height - insets.top;
			}
			if (x >= rootPos.x + insets.left && y >= rootPos.y + insets.top){
				position.setLocation(x, y);
			}
			SwingUtilities.convertPointFromScreen(position, comp);
		}

		// Show popup menu.
		popup.show(comp, position.x, position.y);
	}

	private GeneNode getPopupNode(MouseEvent e) {
		if (null == tree) {
			return null;
		}
		return (getClicked(e.getPoint()));
	}

	private JPopupMenu createPopupMenu(MouseEvent e) {
		JPopupMenu  popup = null;
		GeneNode dsn = getPopupNode(e);
		if (dsn != null) {
			popup = new JPopupMenu();
			if (!dsn.isLeaf()) {
				JMenuItem menuItem;
				if (dsn.isExpanded())
					menuItem = new JMenuItem(POPUP_MENU_COLLAPSE);
				else
					menuItem = new JMenuItem(POPUP_MENU_EXPAND);
				menuItem.addActionListener(new CollapseExpandNodeActionListener(dsn));
				popup.add(menuItem);                            
			}
			// Now add the reroot to node information
			if (!dsn.isLeaf()) {
				JMenuItem menuItem;
				if (dsn != getCurrentRoot()) {
					menuItem = new JMenuItem(POPUP_MENU_REROOT);
					menuItem.addActionListener(new InternalRerootActionListener(dsn));
				} 
				else {
					menuItem = new JMenuItem(POPUP_MENU_RESET_ROOT);
					menuItem.addActionListener(new InternalRerootActionListener(tree.getRoot()));

				}
				popup.add(menuItem);
			}
			if (!dsn.isPruned()) {
				JMenuItem menuItem = new JMenuItem(POPUP_MENU_OUTPUT_SEQ);
				menuItem.addActionListener(new OutputSeqIdsActionListener(e, dsn));
				popup.add(menuItem); 
			}
			JCheckBoxMenuItem checkItem = new JCheckBoxMenuItem(POPUP_MENU_PRUNE);
			checkItem.addActionListener(new PruneActionListener(e, dsn));
			checkItem.setSelected(dsn.isPruned());
			popup.add(checkItem); 
		}
		return popup;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseEntered(MouseEvent e){
		// handleMouseEvent(e);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseExited(MouseEvent e){
		setToolTipText(Constant.STR_EMPTY);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mousePressed(MouseEvent e){
		// Called just after the user presses a mouse button while the cursor is over the listened-to component.
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseReleased(MouseEvent e) {}
	// MouseMotionListener implementation methods

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseDragged(MouseEvent e) {}

	/**
	 * Method declaration
	 *
	 *
	 * @param e
	 *
	 * @see
	 */
	public void mouseMoved(MouseEvent e){
		if (tree != null) {
			if (pointInNode(e.getPoint())){
				this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			}
			else{
				this.setCursor(Cursor.getDefaultCursor());
			}
			GeneNode  node = getClickedInNodeArea(e.getPoint());
			String tool_tip;
			if (null == node){
				tool_tip =  Constant.STR_EMPTY;
			} else {
				tool_tip = getToolTipInfo(node);
			}

			setToolTipText(tool_tip);
			if (!tool_tip.equals(Constant.STR_EMPTY)) {
				UIManager.put(TOOLTIP_FOREGROUND, new ColorUIResource(Preferences.inst().getForegroundColor()));
				ToolTipManager.sharedInstance().setEnabled(true);
				ToolTipManager.sharedInstance().mouseMoved(e);
			}
		}
	}

    public void handleAnnotationDisplayEvent(AnnotationDisplayEvent event) {
        repaint();
    }

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class InternalRerootActionListener implements ActionListener{
		GeneNode  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		InternalRerootActionListener(GeneNode node){
			this.node = node;
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e) {
			tree.nodeReroot(node);
			setNeedPositionUpdate();
			revalidate();
			repaint();
		}

	}

	private class CollapseExpandNodeActionListener implements ActionListener{
		GeneNode  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		CollapseExpandNodeActionListener(GeneNode node){
			this.node = node;
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			tree.handleCollapseExpand(node);
			setNeedPositionUpdate();
			revalidate();
			repaint();
		}

	}

	private class OutputSeqIdsActionListener implements ActionListener{
		GeneNode  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		OutputSeqIdsActionListener(MouseEvent e, GeneNode node){
			this.node = node;
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
			Vector <GeneNode> leafList = new Vector<GeneNode>();
			tree.getLeafDescendants(node, leafList);
			outputInfo(node, leafList);
		}
	}

	private class PruneActionListener implements ActionListener{
		GeneNode  node;

		/**
		 * Constructor declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		PruneActionListener(MouseEvent e, GeneNode node){
			this.node = node;
		}

		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e){
                    Node n = node.getNode();
                    NodeVariableInfo nvi = n.getVariableInfo();
                    if (null == nvi) {
                        nvi = new NodeVariableInfo();
                        n.setVariableInfo(nvi);
                    }
                    nvi.setPruned(!nvi.isPruned());
                    if (nvi.isPruned()) {
                        AnnotationUtil.pruneBranch(node);                        
                    }
                    else {
                        AnnotationUtil.graftBranch(node);        
                    }
                    DirtyIndicator.inst().setAnnotated(true);                    
                    AnnotationUtil.branchNotify(node);
//			node.setPrune(!node.isPruned());

//			if (node.isPruned()) {
//				PaintAction.inst().pruneBranch(node, true);
//			} else {
//				ActionLog.inst().logGrafting(node);
//			}

		}
	}

	protected void outputInfo(GeneNode n, Vector<GeneNode> leafList) {
		// Get the information
		StringBuffer sb =
				new StringBuffer(OUTPUT_SEQ_INFO_TITLE + n.getSeqId());
		sb.append(Constant.STR_NEWLINE);
		sb.append(OUTPUT_SEQ_INFO_COLUMNS);
		sb.append(Constant.STR_NEWLINE);
		for (int i = 0; i < leafList.size(); i++) {
			GeneNode aNode = leafList.get(i);
			sb.append(aNode.getDatabase() + ":" + aNode.getDatabaseID());
			sb.append(OUTPUT_SEQ_DELIM);
			sb.append(aNode.getSeqId());
			sb.append(Constant.STR_NEWLINE);
		}

		// Prompt user for file name
		JFileChooser dlg = new JFileChooser();
		if (null != PaintManager.inst().getCurrentDirectory()) {
			dlg.setCurrentDirectory(PaintManager.inst().getCurrentDirectory());
		}
		int rtrnVal = dlg.showSaveDialog(GUIManager.getManager().getFrame());

		if (JFileChooser.APPROVE_OPTION != rtrnVal) {
			return;
		}
		File f = dlg.getSelectedFile();

		try {
			FileWriter fstream = new FileWriter(f);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(sb.toString());
			//Close the output stream
			out.close();

		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	public void handleGeneSelectEvent(GeneSelectEvent event) {
		List<GeneNode> selection = event.getPrevious();
		repaintNodes(selection);
		selection = event.getGenes();
		repaintNodes(selection);
	}

	public void handleTermEvent(TermSelectEvent e) {
		repaintNodes (tree.getCurrentNodes());
	}
        
        public void handleTermAncestorSelectionEvent(TermAncestorSelectionEvent e) {
            repaintNodes(tree.getCurrentNodes());
        }

	private void repaintNodes(Collection<GeneNode> genes) {
		Rectangle r = null;
		int width = 0;
		int height = 0;
		if (genes != null) {
			for (GeneNode node : genes) {
				Rectangle node_rect = new Rectangle(node.getScreenRectangle());
				// add some padding to make sure the entire area is cleared
				node_rect.x -= GeneNode.GLYPH_DIAMETER;
				node_rect.width += 2 * GeneNode.GLYPH_DIAMETER;
				node_rect.y -= 1;
				node_rect.height += 2;
				if (r == null) {
					r = node_rect;
				} else {
					if (node_rect.x < r.x)
						r.x = node_rect.x;
					if (node_rect.y < r.y)
						r.y = node_rect.y;
				}
				height += node_rect.height;
				width = Math.max(width, node_rect.width);
			}
		}
		if (r != null) {
			r.height = height;
			r.width = width;
			repaint(r);
		}
	}

	public Dimension getPreferredSize() {
		Dimension d = getPreferredScrollableViewportSize();
		d.height += FamilyViews.inst().getHScrollerHeight(FamilyViews.TREE_PANE);
		d.height += FamilyViews.inst().getBottomMargin(FamilyViews.TREE_PANE) + 2;
		return d;
	}

	public Dimension getPreferredScrollableViewportSize() {
		Dimension tree_size = new Dimension();
		if (getGraphics() != null) {
			Rectangle tree_rect = getTreeSize(getGraphics());
			tree_size.width = (int) tree_rect.getWidth();
			tree_size.height = (int) tree_rect.getHeight();
		}
		return tree_size;
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		if (orientation == SwingConstants.VERTICAL) {
			int row_height = PaintManager.inst().getRowHeight();
			int rows = visibleRect.height / row_height;
			return (rows + 1) * row_height;
		} else {
			return 1;
		}
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		int currentPosition;
		int maxUnitIncrement;
		if (orientation == SwingConstants.VERTICAL) {
			currentPosition = visibleRect.y;
			maxUnitIncrement = PaintManager.inst().getRowHeight();
		} else {
			currentPosition = visibleRect.x;
			maxUnitIncrement = 1;
		}

		//Return the number of pixels between currentPosition
		//and the nearest tick mark in the indicated direction.
		int increment;
		if (direction < 0) {
			int newPosition = currentPosition -
					(currentPosition / maxUnitIncrement)
					* maxUnitIncrement;
			increment = (newPosition == 0) ? maxUnitIncrement : newPosition;
		} else {
			increment = ((currentPosition / maxUnitIncrement) + 1)
					* maxUnitIncrement
					- currentPosition;
		}
		return increment;
	}

	@Override
	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		revalidate();
		repaint();
	}

	@Override
	public void handleAspectChangeEvent(AspectChangeEvent event) {
		repaint();
	}

	@Override
	public void handleAspectChangeEvent(AnnotationDragEvent event) {
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	public void setDropInfo(GeneNode node, Point dropPoint, String dropLabel) {

		String tool_tip = (dropLabel != null ? dropLabel : Constant.STR_EMPTY);

		setToolTipText(tool_tip);

		UIManager.put(TOOLTIP_FOREGROUND, new ColorUIResource(Preferences.inst().getForegroundColor()));
		MouseEvent phantom = new MouseEvent(
				this,
				MouseEvent.MOUSE_MOVED,
				System.currentTimeMillis(),
				0,
				dropPoint.x,
				dropPoint.y,
				0,
				false);
		ToolTipManager.sharedInstance().mouseMoved(phantom);

	}

	@Override
	public void handleCurationColorEvent(CurationColorEvent e) {
		repaint();
	}
        
	public boolean pointInNode(Point p) {
		GeneNode current_root = getCurrentRoot();
		if (null == getClicked(current_root, p)){
			return false;
		}
		return true;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param p
	 *
	 * @return
	 *
	 * @see
	 */
	 protected GeneNode getClicked(Point p){
		GeneNode current_root = getCurrentRoot();
		return getClicked(current_root, p);
	 }

	 public GeneNode getClickedInNodeArea(Point p) {
		 GeneNode current_root = getCurrentRoot();
		 return getClickedInNodeArea(current_root, p);
	 }

	 /**
	  * Method declaration
	  *
	  *
	  * @param dsn
	  * @param p
	  *
	  * @return
	  *
	  * @see
	  */
	 private GeneNode getClicked(GeneNode dsn, Point p){
		 if (null == dsn || dsn.getScreenRectangle() == null){
			 return null;
		 }
		 if (dsn.getScreenRectangle().contains(p)){
			 return dsn;
		 }
		 List<GeneNode> children = dsn.getChildren();

		 if (null == children){
			 return null;
		 }
		 if (dsn.isTerminus()){
			 return null;
		 }
		 for (int i = 0; i < children.size(); i++){
			 GeneNode  gnHit = null;

			 gnHit = getClicked(children.get(i), p);
			 if (null != gnHit){
				 return gnHit;
			 }
		 }
		 return null;
	 }

	 public GeneNode getClickedInNodeArea(GeneNode dsn, Point p) {
		 if (null == dsn || dsn.getScreenRectangle() == null){
			 return null;
		 }
		 if (dsn.getScreenRectangle().contains(p)){
			 return dsn;
		 }

		 List<GeneNode>  children = dsn.getChildren();

		 if (children != null && !dsn.isTerminus()){
			 GeneNode  dsnHit = null;
			 for (int i = 0; i < children.size(); i++){
				 dsnHit = getClickedInNodeArea(children.get(i), p);
				 if (null != dsnHit){
					 return dsnHit;
				 }
			 }
		 }
		 return null;
	 }

	 /**
	  * Method declaration
	  *
	  *
	  * @param dsn
	  *
	  * @return
	  *
	  * @see
	  */
	 protected String getToolTipInfo(GeneNode node){
		 return node.getNodeLabel() + node.getNode().getStaticInfo().getPublicId();
	 }

}
