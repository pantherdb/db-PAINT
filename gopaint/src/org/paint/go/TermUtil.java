package org.paint.go;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.geneontology.db.model.Term;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.obo.filters.LinkFilter;
import org.paint.main.PaintManager;

public class TermUtil extends org.obo.util.TermUtil {

	private TermUtil() {
	}

	/*
	 * Tests to see if the second term ('ancestor') is among the ancestors/parents of the first term 'term'
	 * 'term' is the potential descendent/narrower/child term and 'ancestor' is its potential ancestor/broader/parent term
	 */
	public static boolean isAncestor(LinkedObject term, LinkedObject ancestor) {
		Set<LinkedObject> visitedTerms = new HashSet<LinkedObject>();
		LinkedList<LinkedObject> parents = new LinkedList<LinkedObject>();
		parents.add(term);
		for (org.obo.datamodel.Link l : term.getParents()) {
//			if (l.getType().equals(org.obo.datamodel.OBOProperty.IS_A)) {
				parents.add(l.getParent());
//			}
		}
		while (parents.size() > 0) {
			LinkedObject parent = parents.removeFirst();
			visitedTerms.add(parent);
			if (ancestor.equals(parent)) {
				return true;
			}
			for (org.obo.datamodel.Link l : parent.getParents()) {
//				if (l.getType().equals(org.obo.datamodel.OBOProperty.IS_A) && !visitedTerms.contains(l.getParent())) {
				if (!visitedTerms.contains(l.getParent())) {
					parents.add(l.getParent());
				}
			}
		}
		return false;
	}
	
	public static boolean isAncestor(LinkedObject term, LinkedObject ancestor,
			LinkDatabase linkDatabase, LinkFilter lf) {
		return isAncestor(term, ancestor);
	}

	public static LinkedObject getLinkedObject(Term term) {
		return (LinkedObject)GO_Util.inst().getObject(PaintManager.inst().getGoRoot().getLinkDatabase(), term.getAcc());
	}
	

}
