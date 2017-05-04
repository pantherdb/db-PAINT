package org.paint.gui.association;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.geneontology.db.model.Evidence;
import org.geneontology.db.model.Term;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOSession;
import org.paint.go.GO_Util;
import org.paint.main.PaintManager;


public class EvidenceComparator implements Comparator<Evidence> {

	public static final int LESS_THAN = -1;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 0;

	public int compare(Evidence fa, Evidence fb) {
		Term term_a = fa.getAssociation().getTerm();
		Term term_b = fb.getAssociation().getTerm();

		String aspect_a = term_a.getCv();
		String aspect_b = term_b.getCv();

		/* 
		 * Want the ordering to be MF, CC, BP, so reverse the sign by
		 * multiplying by negative 1
		 */
		int comparison = compareStrings(aspect_a, aspect_b) * -1;

		if (comparison == EQUAL_TO) {
			/*
			 * Within a single aspect we want the more general terms first
			 */
			OBOSession go_root = PaintManager.inst().getGoRoot();
			LinkDatabase ld = go_root.getLinkDatabase();
			LinkedObject go_a = (LinkedObject) GO_Util.inst().getObject(ld, term_a.getAcc());
			LinkedObject go_b = (LinkedObject) GO_Util.inst().getObject(ld, term_b.getAcc());
			
			if (go_a != null && go_b != null) {
				comparison = isParent(go_a, go_b);
				if (comparison == EQUAL_TO) {
					comparison = isParent(go_b, go_a) * -1;
				}
				if (comparison == EQUAL_TO) {
					comparison = compareStrings(go_a.getName(), go_b.getName());
				}
			}
		}
		return comparison;
	}

	private int compareStrings(String a, String b) {
		int comparison = a.compareTo(b);
		if (comparison < 0)
			comparison = LESS_THAN;
		else if (comparison > 0)
			comparison = GREATER_THAN;
		else
			comparison = EQUAL_TO;
		return comparison;
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
