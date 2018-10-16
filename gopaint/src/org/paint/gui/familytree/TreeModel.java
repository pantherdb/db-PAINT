/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.paint.gui.familytree;

import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.main.PaintManager;
import org.paint.util.DuplicationColor;

public class TreeModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = Logger.getLogger(TreeModel.class);

	private GeneNode root = null;
	private GeneNode currentRoot = null;
	private List<GeneNode> allNodes = null; // same content as above, but may have a different ordering
	private List<GeneNode> currentNodes = null; // only the nodes that are visible, as some may be collapsed or pruned away
	private List<GeneNode> terminusNodes = null; // only the terminal nodes (i.e. leaves, collapsed or pruned stubs)

	private double tree_distance_scaling = -1;
	/*
	 * For ordering operations on the tree
	 */
	private Map<GeneNode, Integer> descendent_count;
	private int species_count;
	private Map<GeneNode, Integer> species_index;
        
        
        private TreeColorSchema treeColorSchema = Preferences.inst().getColorSchema();
        public static final Set<String>  SPECIES_CLASSIFICATION =  new HashSet<String>(Arrays.asList("BACTERIA", "ARCHEA", "FUNGUS", "PLANTS"));
        
        public  enum TreeColorSchema  {
            DUPLICATION,
            SPECIES_CLS;
        }

	/**
	 * Constructor declaration
	 *
	 *
	 * @param dsn
	 *
	 * @see
	 */
	public TreeModel(GeneNode dsn){
		if (null == dsn){
			return;
		}
		root = dsn;
		currentRoot = dsn;

		descendent_count = new HashMap<GeneNode, Integer>();
		species_index = new HashMap<GeneNode, Integer>();
		species_count = 0;
		initSortGuides(root);

		allNodes = new ArrayList<GeneNode>();
		addChildNodesInOrder(root, allNodes);
		currentNodes = new ArrayList<GeneNode>();
		terminusNodes = new ArrayList<GeneNode>();
		initCurrentNodes(false);

		setSubtreeColor(root, Color.BLACK);

		log.info("There are " + allNodes.size() + " nodes in " + PaintManager.inst().getFamily().getFamilyID() + " tree");
	}

	// Distance methods


	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	protected double getDistanceScaling(){
		if (tree_distance_scaling < 0) {
			tree_distance_scaling = Preferences.inst().getTree_distance_scaling();
		}
		return tree_distance_scaling;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param scale
	 *
	 * @see
	 */
	protected void setDistance(double scale){
		tree_distance_scaling = scale;
		Preferences.inst().setTree_distance_scaling(scale);
	}

	// Creation of nodes in vector

	/**
	 * Method declaration
	 *
	 *
	 * @see
	 */
	/**
	 * Method declaration
	 *
	 * Order the list of all nodes
	 * @param dsn
	 * @param orderedNodes
	 *
	 * @see
	 */
	private void addChildNodesInOrder(GeneNode dsn, List<GeneNode> node_list) {
		if (dsn != null) {
			if (dsn.isTerminus()) {
				node_list.add(dsn);
			} else {
				/* 
				 * Don't add the parent until the children above it have been added first
				 */
				List<GeneNode> topChildren = getTopChildren(dsn);
				for (Iterator<GeneNode> it = topChildren.iterator(); it.hasNext();) {
					addChildNodesInOrder(it.next(), node_list);
				}

				// Add the parent
				node_list.add(dsn);

				List<GeneNode> bottomChildren = getBottomChildren(dsn);
				for (Iterator<GeneNode> it = bottomChildren.iterator(); it.hasNext();) {
					addChildNodesInOrder(it.next(), node_list);
				}
			}
		}
	}

	/* 
	 * This is only called during initialization of a new family
	 */
	private int initSortGuides(GeneNode dsn) {
		int count = 0;
		species_index.put(dsn, new Integer(species_count++));
		if (dsn != null) {
			if (!dsn.isTerminus()) {
				count = dsn.getChildren().size();
				/* 
				 * Don't add the parent until the children above it have been added first
				 */
				List<GeneNode> children = dsn.getChildren();
				for (int i = 0; i < children.size(); i++) {
					GeneNode child = children.get(i);
					count += initSortGuides(child);
				}
			}
			descendent_count.put(dsn, new Integer(count));
		} 
		return count;
	}
	// Methods for getting children of a given node
	// Gets the children displayed above the current node

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
	protected List<GeneNode> getTopChildren(GeneNode dsn){
		return getChildren(true, dsn);
	}

	// Gets the children displayed at the same level as the node and below the current node

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
	protected List<GeneNode> getBottomChildren(GeneNode dsn){
		return getChildren(false, dsn);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param top
	 * @param dsn
	 *
	 * @return
	 *
	 * @see
	 */
	private List<GeneNode> getChildren(boolean top, GeneNode dsn){
		List<GeneNode>  children = dsn.getChildren();
		if (null == children){
			return null;
		}

		// Add remainder to handle case where there are an odd number of children
		int     half = children.size() / 2 + children.size() % 2;
		List<GeneNode>  returnList = new ArrayList<GeneNode>();

		if (top) {
			for (int i = 0; i < half; i++) {
				GeneNode node = children.get(i);
				if (!returnList.contains(node))
					returnList.add(node);
			}
		}
		else {
			for (int i = half; i < children.size(); i++) {
				GeneNode node = children.get(i);
				if (!returnList.contains(node))
					returnList.add(node);
			}
		}
		return returnList;
	}

	/**
	 * Gets the node which is currently displayed at the top of
	 * the list (i.e. the first row) for a given clade/ancestral node 
	 * @param GeneNode, where to start from
	 * @returns GeneNode
	 */
	protected GeneNode getTopLeafNode(GeneNode node) {
		GeneNode top_leaf = null;
		if (node != null) {
			if (!terminusNodes.contains(node) && node.getChildren() != null) {
				top_leaf = getTopLeafNode(node.getChildren().get(0));
			} else {
				top_leaf = node;
			}
		}
		return top_leaf;
	}

	protected GeneNode getBottomLeafNode(GeneNode node) {
		GeneNode bottom_leaf = null;
		if (node != null) {
			if (!terminusNodes.contains(node) && node.getChildren() != null) {
				List<GeneNode> children = node.getChildren();
				bottom_leaf = getBottomLeafNode(children.get(children.size() - 1));
			} else {
				bottom_leaf = node;
			}
		}
		return bottom_leaf;
	}


	// Updates to display for default view

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	protected void expandAllNodes(){
		expandAllNodes(currentRoot);
	}

	// Method to set subtree color

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 * @param c
	 *
	 * @see
	 */
	private void setSubtreeColor(GeneNode dsn, Color c){
		if (null == dsn){
			return;
		}

		// Set values for node
		dsn.setSubFamilyColor(c);

		// Set values for children
		List<GeneNode>  children = dsn.getChildren();

		if (null == children){
			return;
		}
		for (Iterator<GeneNode> it = children.iterator(); it.hasNext();) {
			setSubtreeColor(it.next(), c);
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 *
	 * @see
	 */
	private void resetExpansion(GeneNode dsn){
		if (!dsn.isExpanded()){
			setNodeExpanded(dsn);
		}
		List<GeneNode>  children = dsn.getChildren();

		if (null != children){
			for (Iterator<GeneNode> it = children.iterator(); it.hasNext();) {
				resetExpansion(it.next());
			}
		}
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
	private void expandAllNodes(GeneNode dsn){
		resetExpansion(dsn);
		initCurrentNodes(true);
	}

	protected void collapseNonExperimental() {
		resetExpansion(root);
		collapseMRC (root);
		initCurrentNodes(true);
	}

	private void collapseMRC(GeneNode mrc) {
		Vector<GeneNode> twigList = new Vector<GeneNode>();
		getLeafDescendants(mrc, twigList);
		boolean no_exp = true;
		for (int i = 0; i < twigList.size() && no_exp; i++) {
			GeneNode check = twigList.get(i);
			no_exp &= !GO_Util.inst().hasExperimentalAssoc(check);
		}
		if (no_exp) {
			mrc.setExpanded(false);
		} else {			
			List<GeneNode> children = mrc.getChildren();
			if (children != null) {
				for (GeneNode child : children) {
					collapseMRC(child);
				}
			}
		}
	}

	// Method to set number of leaves in tree
	private void initCurrentNodes(boolean notify) {
		currentNodes.clear();
		terminusNodes.clear();
		addChildNodesInOrder(currentRoot, currentNodes);
		setTerminusNodes();
		DuplicationColor.inst().initColorIndex();
                setSpeciesClassification();
		setDupColorIndex(currentRoot, 0);
		if (notify) {
			NodeReorderEvent event = new NodeReorderEvent(this);
			event.setNodes(getTerminusNodes());
			EventManager.inst().fireNodeReorderEvent(event);
		}
	}
        
    private void setSpeciesClassification() {
        for (GeneNode leaf : terminusNodes) {
            setSpeciesClassification(leaf, leaf);
        }
    }

    private void setSpeciesClassification(GeneNode leaf, GeneNode current) {
        if (null == current) {
            return;
        }
        Node n = current.getNode();
        if (n != null) {
            NodeStaticInfo nsi = n.getStaticInfo();
            if (null != nsi) {
               leaf.setSpeciesClassification(nsi.getSpeciesConversion());
            }
        }
    }

	private void setDupColorIndex(GeneNode node, int color_index) {
		node.setDupColorIndex(color_index);
		if (!node.isLeaf()) {
			List<GeneNode> children = node.getChildren();
			if (node.isDuplication()) {
				boolean only_leaves = true;
				for (GeneNode child : children) {
					only_leaves &= child.isLeaf();;
				}
				if (!only_leaves) {
					List<GeneNode> ordered_by_distance = new ArrayList<GeneNode>();
					ordered_by_distance.addAll(children);
					Collections.sort(ordered_by_distance, new DistanceSort());				
					for (GeneNode child : ordered_by_distance) {
						int index = ordered_by_distance.indexOf(child);
						if (index < ordered_by_distance.size() - 2) {
							GeneNode sib = ordered_by_distance.get(index+1);
							if (sib.getDistanceFromParent() == child.getDistanceFromParent()) {
								color_index = DuplicationColor.inst().getNextIndex();
								log.info(child + " and " + sib + " are equally distance from parent");
							}
						}
						setDupColorIndex(child, color_index);
						if (ordered_by_distance.indexOf(child) < ordered_by_distance.size() - 1)
							color_index = DuplicationColor.inst().getNextIndex();
					}
				}
				else {
					for (GeneNode child : children) {
						setDupColorIndex(child, color_index);
					}
				}
			}
			else {
				for (GeneNode child : children) {
					setDupColorIndex(child, color_index);
				}
			}
		}
	}

	/**
	 * Method declaration
	 *
	 * 
	 * @param dsn
	 *
	 * @see
	 */
	private void setTerminusNodes() {
		for (GeneNode node : currentNodes) {
			if (node.isTerminus()) {
				terminusNodes.add(node);
			}
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 *
	 * @see
	 */
	private void setNodeExpanded(GeneNode dsn){
		if (!dsn.isLeaf() && !dsn.isPruned()){
			dsn.setExpanded(!dsn.isExpanded());
		}
	}

	protected void nodeReroot(GeneNode node){
		if ((node != null) && !node.isTerminus()){
			resetToRoot(node);
		}
	}

	protected void handleCollapseExpand(GeneNode node) {
		boolean change = (node != null && !node.isLeaf() && !node.isPruned());
		if (change) {
			setNodeExpanded(node);
			initCurrentNodes(true);
		}
	}

	protected boolean handlePruning(GeneNode node) {
		boolean change = (node != null && !node.isLeaf());
		if (change) {
			initCurrentNodes(true);
		}
		return change;
	}

	protected void scaleTree(double scale){
		if (this.getDistanceScaling() != scale) {
			this.setDistance(scale);
		}
	}

	/**
	 * getMRCA
	 * 
	 * @param GeneNode - one leaf in the tree
	 * @param GeneNode - a second leaf in the tree
	 * 
	 * @return GeneNode - the node that is an ancestor to both of these two leaves
	 */

	protected GeneNode getMRCA(GeneNode gene1, GeneNode gene2) {
		GeneNode ancestor = null;
		if (gene1.isLeaf() && gene2.isLeaf()) {
			if (gene1 == gene2) {
				ancestor = gene1;
			} else {
				while (ancestor == null && gene1 != null) {
					GeneNode ancestor1 =  gene1.getParent();
					if (isDescendentOf(ancestor1, gene2)) {
						ancestor = ancestor1;
					} else {
						gene1 = ancestor1;
					}
				}
			}
		}

		return ancestor;
	}

	private boolean isDescendentOf(GeneNode ancestor, GeneNode gene) {
		List<GeneNode> children = ancestor.getChildren();
		boolean is_descendent = false;
		if (children != null) {
			if (children.contains(gene)) {
				is_descendent = true;
			} else {
				for (Iterator<GeneNode> it = children.iterator(); it.hasNext() && !is_descendent; ) {
					is_descendent = isDescendentOf(it.next(), gene);
				}
			}
		}
		return is_descendent;
	}

	protected boolean resetRootToMain(){
		return resetToRoot(root);
	}

	protected boolean resetToRoot(GeneNode dsn){
		// If it is not the current root, make it the current root
		if (currentRoot != dsn){
			currentRoot =  dsn;
			initCurrentNodes(true);
			return true;
		} else 
			return false;
	}

	// Tree operations

	protected GeneNode getRoot() {
		return root;
	}

	protected List<GeneNode> getAllNodes() {
		return allNodes;
	}

	protected List<GeneNode> getTerminusNodes() {
		return terminusNodes;
	}

	public List<GeneNode> getCurrentNodes() {
		return currentNodes;
	}

	protected GeneNode getCurrentRoot(){
		return currentRoot;
	}

	// Methods used for laddering functionality
	protected void speciesOrder() {
		/* 
		 * Sort each nodes list of children accordingly
		 */
		Comparator<GeneNode> comp = new SpeciesSort();
		ladder(currentRoot, comp);

		/* 
		 * Reinitialize the full list of nodes
		 */
		allNodes.clear();
		addChildNodesInOrder(currentRoot, allNodes);
		// Save new ordering that are visible as well
		initCurrentNodes(true);
	}

	protected void descendentCountLadder(boolean most_leaves_at_top) {
		/* 
		 * Sort each nodes list of children accordingly
		 */
		Comparator<GeneNode> comp = new LeafCountSort(most_leaves_at_top);
		ladder(currentRoot, comp);

		/* 
		 * Reinitialize the full list of nodes
		 */
		allNodes.clear();
		addChildNodesInOrder(currentRoot, allNodes);
		// Save new ordering that are visible as well
		initCurrentNodes(true);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 *
	 * @see
	 */
	private void ladder(GeneNode dsn, Comparator<GeneNode> comp){
		List<GeneNode>  children = dsn.getChildren();
		if (null == children){
			return;
		}

		// start at the bottom and work the way back up
		for (Iterator<GeneNode> it = children.iterator(); it.hasNext();) {
			GeneNode child = it.next();
			ladder(child, comp);
		}

		// Sort the children
		Collections.sort(children, comp);

	}

	private class LeafCountSort implements Comparator<GeneNode> {
		private boolean most_leaves_at_top;

		private LeafCountSort(boolean most_leaves_at_top) {
			this.most_leaves_at_top = most_leaves_at_top;
		}
		public int compare(GeneNode o1, GeneNode o2) {
			int o1_descendents = descendent_count.get(o1).intValue();
			int o2_descendents = descendent_count.get(o2).intValue();
			int sort_value = o1_descendents - o2_descendents;
			if (most_leaves_at_top)
				sort_value = sort_value * -1;
			return sort_value;
		}
	}

	private class SpeciesSort implements Comparator<GeneNode> {

		public int compare(GeneNode o1, GeneNode o2) {
			int o1_descendents = species_index.get(o1).intValue();
			int o2_descendents = species_index.get(o2).intValue();
			return (o1_descendents - o2_descendents);
		}
	}

	private class DistanceSort implements Comparator<GeneNode> {

		private DistanceSort() {
		}

		public int compare(GeneNode o1, GeneNode o2) {
			float o1_distance = o1.getDistanceFromParent();
			float o2_distance = o2.getDistanceFromParent();
			if (o1_distance < o2_distance)
				return -1;
			else if (o1_distance < o2_distance)
				return 1;
			else
				return 0;
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param node
	 * @param v
	 *
	 * @see
	 */
	protected void getDescendentList(GeneNode node, List<GeneNode> v){
		if (!node.isTerminus()) {
			List<GeneNode>  children = node.getChildren();
			for (int i = 0; i < children.size(); i++){
				GeneNode  child = children.get(i);
				v.add(child);
				getDescendentList(child, v);
			}
		}
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 * @param leafList
	 *
	 * @see
	 */
	protected void getLeafDescendants(GeneNode node, Vector<GeneNode> leafList){
		if (null == node){
			return;
		}
		if (node.isLeaf() && !node.isPruned()){
			leafList.addElement(node);
		}
		else {
			List<GeneNode>  children = node.getChildren();
			for (int i = 0; (children != null && i < children.size()); i++){
				GeneNode  child = children.get(i);
				if (!child.isPruned())
					getLeafDescendants(child, leafList);
			}
		}
	}
        
        public int getNumNodes() {
            return allNodes.size();
        }

    public TreeColorSchema getTreeColorSchema() {
        return treeColorSchema;
    }

    public void setTreeColorSchema(TreeColorSchema treeColorSchema) {
        this.treeColorSchema = treeColorSchema;
        Preferences.inst().setTreeColorSchema(treeColorSchema);
    }
        
        
        
}
