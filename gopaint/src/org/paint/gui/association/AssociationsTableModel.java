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

package org.paint.gui.association;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.bbop.swing.HyperlinkLabel;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.DBXref;
import org.geneontology.db.model.Evidence;
import org.geneontology.db.model.Term;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.gui.PaintTable;


public class AssociationsTableModel extends AbstractTableModel 
implements PaintTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final String TERM_COL_NAME = "Term";
	protected static final String CODE_COL_NAME = "ECO";
	protected static final String REFERENCE_COL_NAME= "Reference";
	protected static final String WITH_COL_NAME = "With";
	protected static final String TRASH_COL_NAME = "DEL";

	protected static final String[] column_headings = {
		CODE_COL_NAME, 
		TERM_COL_NAME, 
		REFERENCE_COL_NAME,
		WITH_COL_NAME,
		TRASH_COL_NAME
	};

	protected GeneNode node;
	protected ArrayList<Evidence> evidence;
	protected HashMap<Evidence, HyperlinkLabel> pub_labels;
	protected HashMap<Evidence, Set<HyperlinkLabel>> with_labels;

	protected static Logger log = Logger.getLogger(AssociationsTableModel.class);

	public AssociationsTableModel() {
		evidence = new ArrayList<Evidence> ();
		pub_labels = new HashMap<Evidence, HyperlinkLabel> ();
		with_labels = new HashMap<Evidence, Set<HyperlinkLabel>> ();
	}

	public void setNode(GeneNode gene) {
		this.node = gene;
		evidence.clear();
		pub_labels.clear();
		with_labels.clear();
//		if (node != null && node.getGeneProduct() != null) {
//			Collection<Association> associations = node.getAssociations();
//			if (associations != null) {
//				for (Iterator<Association> it = associations.iterator(); it.hasNext();) {
//					Association assoc = it.next();
//					Set<Evidence> evi_list = assoc.getEvidence();
//					for (Iterator<Evidence> evi_it = evi_list.iterator(); evi_it.hasNext();) {
//						Evidence evi = evi_it.next();
//						String code = evi.getCode();
//						if (code == null) {
//							log.debug("How did " + node + " association to " + assoc.getTerm().getName() + " lose its evidence code!!");
//						}
//						if (!node.isLeaf() || (node.isLeaf() && !code.equals("ND") && !code.equals("IEA"))) {
//							evidence.add(evi);
//						} else if (code.equals("ND")) {
//							log.info(node.getSeqName() + " has ND to term " + evi.getAssociation().getTerm().getName());
//						}
//					}
//				}
//			}
//
//			/*
//			 * Important not to create the table until the evidence is sorted
//			 */
//			sort();
//			for (Iterator<Evidence> it = evidence.iterator(); it.hasNext();) {
//				Evidence evi = it.next();
//				/*
//				 * This should be the link to the publication record
//				 */
//				DBXref xref = evi.getDbxref();
//				HyperlinkLabel field = new HyperlinkLabel();
//				field.setEnabled(true);
//				field.addHyperlinkListener(new TermHyperlinkListener());
//				String xref_text = HTMLUtil.getHTML(xref.getDb_name(), xref.getAccession(), false);
//				field.setText(xref_text);
//				pub_labels.put(evi, field);
//				
//				Set<DBXref> withs = evi.getWiths();
//				field = new HyperlinkLabel();
//				field.setEnabled(true);
//				field.addHyperlinkListener(new TermHyperlinkListener());
//				if (withs != null && !withs.isEmpty()) {
//					if (withs.size() == 1) {
//						DBXref with = withs.iterator().next();
//						xref_text = HTMLUtil.getHTML(with.getDb_name(), with.getAccession(), true);
//					} 
//					else {
//						xref_text = HTMLUtil.HTML_TEXT_BEGIN+GO_Util.inst().getWithText(withs)+HTMLUtil.HTML_TEXT_END;
//					}
//				}
//				else {
//					xref_text = HTMLUtil.HTML_TEXT_BEGIN+HTMLUtil.HTML_TEXT_END;
//				}
//				field.setText(xref_text);
//				Set<HyperlinkLabel> with_links = new HashSet<HyperlinkLabel>();
//				with_links.add(field);					
//				with_labels.put(evi, with_links);
//			}
//		}
	}

	public void removeAssociation(Association assoc) {
		for (int i = evidence.size() - 1; i >= 0; i--) {
			Evidence table_evi = evidence.get(i);
			if (table_evi.getAssociation().equals(assoc)) {
				pub_labels.remove(table_evi);
				with_labels.remove(table_evi);
				evidence.remove(i);
			}
		}
	}

	public String getTextAt(int row, int column) {
		if (getRowCount() == 0) {
			return null;
		}
		String tag = column_headings[column];
		Evidence evi = evidence.get(row);

		if (tag.equals(CODE_COL_NAME)) {
			return CODE_COL_NAME;
		} else if (tag.equals(TERM_COL_NAME)) {
			return evi.getAssociation().getTerm().getName();
		} else if (tag.equals(REFERENCE_COL_NAME)) {
			DBXref xref = evi.getDbxref();
			String xref_text = xref.getDb_name() + ":" + xref.getAccession();
			return xref_text;
		} else if (tag.equals(TRASH_COL_NAME)) {
			return TRASH_COL_NAME;
		} else if (tag.equals(WITH_COL_NAME)) {
			return GO_Util.inst().getWithText(evi.getWiths());
		} else {
			return "";
		}
	}

//	protected int getRowForTerm(Term term) {
//		LinkDatabase linkDb = PaintManager.inst().getGoRoot().getLinkDatabase();
//		LinkedObject potentialAncestor = (LinkedObject)GO_Util.inst().getObject(linkDb, term.getAcc());
//		for (int row = 0; row < evidence.size(); row++) {
//			Evidence evi = evidence.get(row);
//			LinkedObject potentialDescendant =
//				(LinkedObject)GO_Util.inst().getObject(linkDb, evi.getAssociation().getTerm().getAcc());
//
//			if (evi.getAssociation().getTerm().equals(term) ||
//					TermUtil.isDescendant(potentialAncestor, potentialDescendant))
//				return row;
//		}
//		return -1;
//	}

	public Term getTermForRow(int row) {
		Evidence evi = evidence.get(row);
		Term term = evi.getAssociation().getTerm();
		return term;
	}

	public Evidence getEvidenceForRow(int row) {
		return evidence.get(row);
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the last column would contain text ("true"/"false"),
	 * rather than a check box.
	 */
	@Override
	public Class getColumnClass(int columnIndex) {
		Class check = null;
		Object value = getValueAt(0, columnIndex);
		if (value != null) {
			check = value.getClass();
			if (check == null) {
				System.out.println("Table returning null for column " + columnIndex);
			}
		} else {
			String tag = column_headings[columnIndex];
			if (tag.equals(CODE_COL_NAME)) {
				check = Evidence.class;
			} else if (tag.equals(TERM_COL_NAME)) {
				// this is the column with the term in it
				check = Association.class;
			} else if (tag.equals(REFERENCE_COL_NAME)) {
				// this is the published reference for the annotation
				check = HyperlinkLabel.class;
			} else if (tag.equals(TRASH_COL_NAME)) {
				// the evidence code
				check = Boolean.class;
			} else if (tag.equals(WITH_COL_NAME)) {
				// and what (if appropriate) the inference was based on, e.g. another sequence or an interpro domain
				check = Set.class;
			}
		}
		return check;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return column_headings[columnIndex];
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (null == evidence || rowIndex >= getRowCount() || rowIndex < 0) {
			return null;
		}
		Evidence evi = evidence.get(rowIndex);
		String tag = column_headings[columnIndex];
		if (tag.equals(CODE_COL_NAME)) {
			return evi;
		} else if (tag.equals(TERM_COL_NAME)) {
			// this is the column with the term in it
			Association assoc = evi.getAssociation();
			return assoc;
		} else if (tag.equals(REFERENCE_COL_NAME)) {
			// this is the published reference for the annotation
			// It is of type Hyperlink.class
			return pub_labels.get(evi);
		} else if (tag.equals(TRASH_COL_NAME)) {
			// whether or not the annotation was done in PAINT
//			Association assoc = evi.getAssociation();
//			if (GO_Util.inst().isPAINTAnnotation(assoc)) {
//				if (assoc.isMRC() || assoc.isDirectNot()) {
//					return Boolean.TRUE;
//				} else {
//					return Boolean.FALSE;
//				}
//			} else {
                    return Boolean.FALSE;
//			}
		} else if (tag.equals(WITH_COL_NAME)) {
			// and what (if appropriate) the inference was based on, e.g. another sequence or an interpro domain
//			Set<DBXref> withs = evi.getWiths();
			return with_labels.get(evi);
//			return withs;
		} else {
			return null;
		}
	}

	public int getColumnCount() {
		return column_headings.length;
	}

	public int getRowCount() {
		return evidence.size();
	}

	public boolean isCellEditable(int rowIndex, int colIndex) {
		Object cell = getValueAt(rowIndex, colIndex);
		if (cell != null && cell instanceof Boolean) {
			return true;
		}
		return false;
	}

	private void sort() {
		Collections.sort(evidence, new EvidenceComparator());
	}

	/**
	 * All paint tables must implement this method so that the column width utility can be used
	 * 
	 */
	public boolean isSquare(int column) {
		return getColumnName(column).equals(TRASH_COL_NAME);
	}
}
