package org.paint.gui.matrix;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.geneontology.db.model.Association;
import org.geneontology.db.model.Term;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkedObject;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;


public class TermCountComparator implements Comparator<Term> {

	private static final int LESS_THAN = -1;
	private static final int GREATER_THAN = 1;
	private static final int EQUAL_TO = 0;

	private List<GeneNode> nodes;
	
	public TermCountComparator (List<GeneNode> nodes) {
		this.nodes = nodes;
	}
	
	public int compare(Term term_a, Term term_b) {
		int count_a = 0;
		int count_b = 0;
		
		if (nodes != null) {
			for (GeneNode node : nodes) {
				Association assoc_a = GO_Util.inst().isAnnotatedToTerm(node, term_a);
				Association assoc_b = GO_Util.inst().isAnnotatedToTerm(node, term_b);
				count_a += assoc_a != null && GO_Util.inst().isExperimental(assoc_a) ? 1 : 0;
				count_b += assoc_b != null && GO_Util.inst().isExperimental(assoc_b) ? 1 : 0;
			}
		}		
		if (count_b > count_a)
			return GREATER_THAN;
		else if (count_b < count_a)
			return LESS_THAN;
		else
			return EQUAL_TO;
	}

	/*
	 * Returns less_than if c is a child of p
	 * 
	 */
	protected static int isParent(LinkedObject c, LinkedObject p) {
		int comparison = EQUAL_TO;
		Collection<Link> parents = c.getParents();
		if (parents.size() > 0) {
			if (parents.contains(p)) {
				comparison = LESS_THAN;
			} else {
				for (Iterator<Link> it = parents.iterator(); it.hasNext() && comparison == EQUAL_TO;) {
					Link l = it.next();
					comparison = isParent ((LinkedObject) l.getParent(), p);
				}
			}
		}
		return comparison;
	}
}
