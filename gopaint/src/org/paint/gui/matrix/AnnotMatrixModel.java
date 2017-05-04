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
package org.paint.gui.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.Term;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOSession;
import org.paint.config.CustomTermList;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.go.TermUtil;
import org.paint.main.PaintManager;
import org.paint.util.GeneNodeUtil;

public class AnnotMatrixModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<GeneNode> nodes;
	private Map<Term, ColumnTermData> term2menu;
	private List<Term> term_list;
	private List<Term[]> added_term_list;

	protected static final int BROADER = 0;
	protected static final int NARROWER = 1;

	private AssociationData [][]associationMatrix;

	private String go_aspect;

	private static Logger log = Logger.getLogger(AnnotMatrixModel.class);

	public AnnotMatrixModel(List<GeneNode> orderedNodes, String go_aspect) {

		this.go_aspect = go_aspect;

		nodes = new ArrayList<GeneNode>();
		term_list = new ArrayList<Term> ();
		added_term_list = new ArrayList<Term []> ();	
		term2menu = new HashMap<Term, ColumnTermData> ();

		/* get the table headers from the from the gene experimental annotations */
		if (orderedNodes != null) {
			GeneNodeUtil.inst().setVisibleRows(orderedNodes, nodes);
		}

		List<Term> exp_list = possibleTerms();		
		initTerms(exp_list, added_term_list);
	}

	private List<Term> possibleTerms() {
		List<Term> temp_list = new ArrayList<Term> ();
		for (GeneNode node : nodes) {
			Set<Association> assocs = GO_Util.inst().getAssociations(node, go_aspect, true);
			Set<String> exclusionTerms = CustomTermList.getSingleton().getExclusionList();
			if (assocs != null) {
				for (Iterator<Association> assoc_it = assocs.iterator(); assoc_it.hasNext();) {
					Term term = assoc_it.next().getTerm();
					if (!temp_list.contains(term) && !exclusionTerms.contains(term.getAcc())) {
						temp_list.add(term);
					}
				}
			}
		}
		return temp_list;
	}

	protected void initTerms(List<Term> exp_list, List<Term []> added_term_list) {
		term_list.clear();
		term2menu.clear();
		List<Term> temp_list = new ArrayList<Term> ();

		if (nodes != null) {
			if (!added_term_list.isEmpty()) {
				for (Term [] added_term : added_term_list) {
					temp_list.add(added_term[BROADER]);
				}
			}
			temp_list.addAll(exp_list);
			associationMatrix = new AssociationData[nodes.size()][temp_list.size()];
			sortTerms(nodes, temp_list);
		}

		for (Term t : term_list) {
			ColumnTermData td = term2menu.get(t);
			for (Term [] added_term : added_term_list) {
				if (added_term[BROADER] == t)
					td.setNarrowTerm(added_term[NARROWER]);
			}
			td.initTermMenu(t, term_list);
		}
		fireTableDataChanged();
	}

	private void sortTerms(List<GeneNode> nodes, List<Term> temp_list) {
		/* 
		 * This sort
		 * 	Puts all cellular processes first
		 *  Sorts by number of annotated genes
		 *  Integrates child terms
		 *  Appends singlets alphabetically
		 * 
		 */
		List<Term> cellular_list = new ArrayList<Term> ();

		OBOSession go_root = PaintManager.inst().getGoRoot();
		LinkDatabase ld = go_root.getLinkDatabase();
		/* Hack alert - hard coded the GO id for cellular process here */
		LinkedObject cellular = (LinkedObject) GO_Util.inst().getObject(ld, "GO:0009987");
		for (Term term : temp_list) {
			LinkedObject go_term = (LinkedObject) GO_Util.inst().getObject(ld, term.getAcc());
			if (TermUtil.isAncestor(go_term, cellular)) {
				cellular_list.add(term);
			}
		}

		for (Term term : cellular_list) {
			temp_list.remove(term);
		}

		/*
		 * First an alphabetic sort
		 */
		Collections.sort(cellular_list, new TermComparator());
		Collections.sort(temp_list, new TermComparator());
		/*
		 * Then sort by the number of genes annotated to each term
		 * The more genes annotated the higher in the list the term will be
		 */
		Collections.sort(cellular_list, new TermCountComparator(nodes));
		Collections.sort(temp_list, new TermCountComparator(nodes));
		/*
		 * But then insert the related terms immediately after their child
		 */
		boolean odd_column = true;
		odd_column = groupParentTerms(cellular_list, ld, odd_column);
		groupParentTerms(temp_list, ld, odd_column);
	}

	private boolean groupParentTerms(List<Term> orig_termlist, LinkDatabase ld, boolean odd_column) {
		while (orig_termlist.size() > 0) {
			Term cur_term = orig_termlist.remove(0);
			term_list.add(cur_term);
			ColumnTermData td = new ColumnTermData();
			td.setOddColumn(odd_column);
			term2menu.put(cur_term, td);
			LinkedObject term1 = (LinkedObject) GO_Util.inst().getObject(ld, cur_term.getAcc());
			for (int i = 0; i < orig_termlist.size();) {
				Term other_term = orig_termlist.get(i);
				LinkedObject term2 = (LinkedObject) GO_Util.inst().getObject(ld, other_term.getAcc());
				if (TermUtil.isAncestor(term1, term2) || TermUtil.isDescendant(term1, term2)) {
					orig_termlist.remove(i);
					term_list.add(other_term);
					td = new ColumnTermData();
					td.setOddColumn(odd_column);
					term2menu.put(other_term, td);
				} else {
					i++;
				}
			}
			odd_column = !odd_column;
		}
		return !odd_column;
	}

	protected boolean modifyColumns(Term added_term[], boolean remove) {
		boolean modify = false;
		if (remove) {
			for (int i = 0; i < added_term_list.size() && !modify; i++) {
				Term [] check = added_term_list.get(i);
				if (check[BROADER] == added_term[BROADER] && check[NARROWER] == added_term[NARROWER]) {
					added_term_list.remove(i);
					modify = true;
				}
			}
		} else {
			modify = true;
			for (int i = 0; i < added_term_list.size() && modify; i++) {
				Term [] check = added_term_list.get(i);
				modify &= !(check[BROADER] == added_term[BROADER] 
						&& check[NARROWER] == added_term[NARROWER]);
			}
			if (modify) 
				added_term_list.add(added_term);
		}
		if (modify)
			initTerms(possibleTerms(), added_term_list);
		return modify;
	}

	protected void modifyRows(List<GeneNode> orderedNodes) {
		nodes.clear();
		/* get the table headers from the from the gene experimental annotations */
		if (orderedNodes != null) {
			GeneNodeUtil.inst().setVisibleRows(orderedNodes, nodes);
		}

		/* 
		 * Need to save this, but possibly may need to trim it down
		 * if the relevant narrower terms are no longer included for these nodes
		 */
		List<Term []> allowable_terms = new ArrayList<Term []> ();
		List<Term> exp_terms = possibleTerms();
		for (Term [] check : added_term_list) {
			boolean allowable = false;
			for (int i = 0; i < exp_terms.size() && !allowable; i++) {
				Term exp_term = exp_terms.get(i);
				if (check[NARROWER] == exp_term 
						&& !exp_terms.contains(check[BROADER]) 
						&& !allowable_terms.contains(check)) {
					allowable_terms.add(check);
					allowable = true;
				}
			}
		}
		initTerms(exp_terms, allowable_terms);
	}

	public int getColumnCount() {
		return term_list.size();
	}

	protected List<Term> getTermList() {
		return term_list;
	}
	
	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return Association.class;
	}

	@Override
	public String getColumnName(int columnIndex) {
		String name = term_list.get(columnIndex).getName();
		return name;
	}

	public int getRowCount() {
		return nodes.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (nodes == null || term2menu == null) {
			return null;
		}
		if ((rowIndex > (nodes.size() - 1)) || (columnIndex >= (term2menu.size()))) {
			return null;
		}
		AssociationData data = associationMatrix[rowIndex][columnIndex];
		if (data == null) {
			Term term = term_list.get(columnIndex);
			GeneNode node = nodes.get(rowIndex);
			data = this.getCellAssoc(term, node);
			associationMatrix[rowIndex][columnIndex] = data;
		}
		return data;
	}

	private AssociationData getCellAssoc(Term term, GeneNode node) {
		List<Association> self = new ArrayList<Association>();
		List<Association> broader_terms = new ArrayList<Association>();
		List<Association> narrower_terms = new ArrayList<Association>();
		AssociationData cell;

		if (node.getNodeLabel().contains("HUMAN_ANKRD10")) {
			log.debug("Pause to check");
		}
		/*
		 * First look only at experimental annotations
		 */
		GO_Util.inst().getRelatedAssociationsToTerm(node, term, self, broader_terms, narrower_terms, true);
		cell = annotationToTerm(self);
		if (cell == null) {
			cell = annotationToFinerTerm(broader_terms);
		}
		if (cell == null) {
			cell = annotationToCoarserTerm(narrower_terms);
		}
		/* 
		 * If no experimental annotations to the term check for PAINT annotations
		 */
		if (cell == null) {
			self.clear();
			broader_terms.clear();
			narrower_terms.clear();
			GO_Util.inst().getRelatedAssociationsToTerm(node, term, self, broader_terms, narrower_terms, false);
			cell = annotationToTerm(self);
			if (cell == null) {
				cell = annotationToFinerTerm(broader_terms);
			}
			if (cell == null) {
				cell = annotationToCoarserTerm(narrower_terms);
			}
		}
		if (cell == null)
			cell = new AssociationData(null, false, false);
		return cell;
	}

	private AssociationData annotationToTerm(List<Association> self) {
		Association assoc = null;
		boolean isNot = false;
		if (!self.isEmpty()) {
			assoc = self.get(0);
			for (Association a : self) {
				isNot |= a.isNot();
				if (a.isNot())
					assoc = a;
			}
		}
		if (assoc != null)
			return (new AssociationData(assoc, false, isNot));
		else
			return null;
	}

	private AssociationData annotationToFinerTerm(List<Association> descendants) {
		Association assoc = null;
		boolean isNot = false;
		if (!descendants.isEmpty()) {
			for (Association a : descendants) {
				isNot |= a.isNot();
				if (a.isNot() && assoc == null) {
					assoc = a;
				}
			}
		}
		if (assoc != null)
			return (new AssociationData(assoc, true, isNot));
		else
			return null;
	}

	private AssociationData annotationToCoarserTerm(List<Association> ancestors) {
		Association assoc = null;
		if (!ancestors.isEmpty()) {
			for (Association a : ancestors) {
				if (!a.isNot()) {
					assoc = a;
				}
			}
		}
		if (assoc != null)
			return (new AssociationData(assoc, true, false));
		else
			return null;
	}

	public void resetAssoc(GeneNode parent) {
		List<GeneNode> leaves = new ArrayList<GeneNode>();
		parent.getTermini(leaves);
		if (leaves == null || leaves.size() == 0) {
			return;
		}
		int offset = getRow(leaves.get(0));
		for (int i = 0; i < leaves.size(); i++) {
			GeneNode node = leaves.get(i);
			if (nodes.contains(node)) {
				int j = 0;
				for (Term term : term_list) {
					AssociationData data = this.getCellAssoc(term, node);
					associationMatrix[offset + i][j++] = data;
				}
			}
		}
	}

	public void resetHiddenRows() {
		for (int i = 0; i < nodes.size(); i++) {
			GeneNode row = nodes.get(i);
			row.setVisible(false);
		}
	}

	public int getRow(GeneNode dsn) {
		try {
			return nodes.indexOf(dsn);
		} catch (NullPointerException e) {
			log.debug("Could not find gene " + dsn.getSeqName() + " in contents");
			return -1;
		}
	}

	public GeneNode getNode(int row) {
		if (row >= nodes.size()) {
			System.out.println("Asking for row " + row + " which is > than the number of rows (" + nodes.size() + ")");
			return null;
		} else {
			GeneNode node = nodes.get(row);
			return node;
		}
	}

	public List<Term> searchForTerm(String term) {
		List<Term> matches = new ArrayList<Term> ();
		if (term != null && term.length() > 0) {
			if (term.charAt(0) == '*')
				term = term.length() > 1 ? term.substring(1) : "";
			if (term.length() > 0 && term.endsWith("*"))
				term = term.length() > 1 ? term.substring(0, term.length() - 1) : "";
			Pattern p = Pattern.compile(".*" + term + ".*");
			for (Term check : term_list) {
				String s = check.toString();
				if (p.matcher(s).matches()) {
					matches.add(check);
				}
			}
		}
		return matches;
	}

	ColumnTermData getTermData(int column) {
		if (column >= 0 && column < getColumnCount()) {
			Term column_term = term_list.get(column);
			return term2menu.get(column_term);
		} else 
			return null;
	}

	public int getTermColumn(Term term){
		return term_list.indexOf(term);
	}

	public Term getTermForColumn(int column) {
		if (column >= 0 && column < term_list.size())
			return term_list.get(column);
		else
			return null;
	}

	public class AssociationData {

		private Association association;
		private boolean is_implied;
		private boolean not;

		public AssociationData(Association association, boolean is_implied,
				boolean not) {
			super();
			this.association = association;
			this.is_implied = is_implied;
			this.not = not;
		}

		public Association getAssociation() {
			return association;
		}

		public void setAssociation(Association association) {
			this.association = association;
		}

		public boolean isAncestor() {
			return is_implied;
		}

		public void setAncestor(boolean ancestor) {
			this.is_implied = ancestor;
		}

		public boolean isNot() {
			return not;
		}

		public void setNot(boolean not) {
			this.not = not;
		}
	}

}
