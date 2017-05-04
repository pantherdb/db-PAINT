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
package org.paint.gui.evidence;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.Term;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.paint.datamodel.GeneNode;
import org.paint.dialog.QualifierDialog;
import org.paint.go.GOConstants;
import org.paint.go.GO_Util;
import org.paint.go.TermUtil;
import org.paint.main.PaintManager;


public class WithEvidence {

	private static Logger log = Logger.getLogger(WithEvidence.class);

	private Set<GeneNode> exp_withs;
	private Set<GeneNode> notted_withs;
	private Map<Term, Set<GeneNode>> qual2nodes;
	private Set<Term> quals;

	public WithEvidence(Term add_term, GeneNode node) {
		initWiths(TermUtil.getLinkedObject(add_term), node, add_term.getCv());
	}

	protected Set<Term> getWithQualifiers() {
		quals = new HashSet<Term> ();
		if (qual2nodes != null && !qual2nodes.isEmpty()) {
			QualifierDialog qual_dialog = new QualifierDialog(GUIManager.getManager().getFrame(), qual2nodes);
			quals = qual_dialog.getQualifiers();
		}
		return quals;
	}

	public Set<GeneNode> getExpWiths() {
		if (exp_withs.size() > 0)
			return exp_withs;
		else
			return notted_withs;
	}

	public boolean isExperimentalNot() {
		return notted_withs.size() > 0 && exp_withs.size() == 0;
	}

	public boolean lacksEvidence() {
		return exp_withs.size() == 0 && notted_withs.size() == 0;
	}

//	public boolean isContradictory() {
//		return exp_withs.size() > 0 && notted_withs.size() > 0;
//	}
//
	public Map<Term, Set<GeneNode>> getTermQualifiers() {
		return qual2nodes;
	}

	private void initWiths(LinkedObject add_term, GeneNode node, String go_aspect) {
		/*
		 * First gather all of the gene nodes leaves that may have provided this term
		 */
		org.paint.gui.familytree.TreePanel tree = PaintManager.inst().getTree();
		Vector<GeneNode> leafList = new Vector<GeneNode>();
		tree.getLeafDescendants(node, leafList);
		exp_withs = new HashSet<GeneNode> ();
		notted_withs = new HashSet<GeneNode> ();
		qual2nodes = new HashMap<Term, Set<GeneNode>>();
		LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
		for (GeneNode leaf : leafList) {
			// Second argument is true because only the experimental codes can be used for inferencing
			Set<Association> assocs = GO_Util.inst().getAssociations(leaf, go_aspect, true);
			if (assocs != null) {
				for (Iterator<Association> it_assoc = assocs.iterator(); it_assoc.hasNext() && !exp_withs.contains(leaf);) {
					Association leaf_assoc = it_assoc.next();
					Term leaf_term = leaf_assoc.getTerm();
					boolean add = GO_Util.inst().isExperimental(leaf_assoc);
					if (leaf_term.getAcc().equals(add_term.getID())) {
						add &= true;
					} else {
						IdentifiedObject go_leaf_term = GO_Util.inst().getObject(go_root, leaf_term.getAcc());
						/*
						 * Is the term in question (add_term) a parental/broader term than 
						 * the term associated to the leaf node (go_leaf_term)
						 */
						add &= TermUtil.isAncestor((LinkedObject) go_leaf_term, add_term, go_root, null);
					}
					if (add) {
						exp_withs.add(leaf);
						/*
						 * The code below is carrying out an unrelated function
						 * Namely to see if any of the experimental nodes that provide the supporting evidence are qualified
						 * Doing this here, rather than as a separate function to avoid recursing down the tree twice
						 */
						Set<Term> exp_quals = leaf_assoc.getQualifiers(); 
						for (Term qual : exp_quals) {
							String name = qual.getName().toUpperCase();
							if (!name.equals(GOConstants.NOT) && !name.equals(GOConstants.CUT) && qual2nodes != null) {
								Set<GeneNode> qualified_nodes = qual2nodes.get(qual);
								if (qualified_nodes == null) {
									qualified_nodes = new HashSet<GeneNode>();
									qual2nodes.put(qual, qualified_nodes);
								}
								if (!qualified_nodes.contains(leaf)) {
									qualified_nodes.add(leaf);
								}
							} else if (name.equals(GOConstants.NOT)) {
								notted_withs.add(leaf);
								if (leaf_term.getAcc().equals(add_term.getID())) {
									exp_withs.remove(leaf);
								}
							}
						}
					}
				}
			}
		}
	}

}
