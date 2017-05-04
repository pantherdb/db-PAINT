package org.paint.gui.matrix;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.geneontology.db.model.GraphPath;
import org.geneontology.db.model.Term;


public class TermGroupComparator implements Comparator<Term> {

	public static final int LESS_THAN = -1;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 0;

	private Map<Term, List<GraphPath>> parent_groups;
	
	public TermGroupComparator(Map<Term, List<GraphPath>> parent_groups) {
		super();
		this.parent_groups = parent_groups;
	}
	
	public int compare(Term fa, Term fb) {
		
		int a_size = parent_groups.get(fa).size();
		int b_size = parent_groups.get(fb).size();

		int comparison = compareSize(a_size, b_size);
		
		/* 
		 * If the 2 term group sizes are the same
		 * then resort to a simple alphabetical sort
		 */		
		if (comparison == EQUAL_TO) {
			comparison = compareStrings(fa.getName(), fb.getName());
		}
		return comparison;
	}

	private int compareSize(int a, int b) {
		int comparison = b - a;
		if (comparison < 0)
			comparison = LESS_THAN;
		else if (comparison > 0)
			comparison = GREATER_THAN;
		else
			comparison = EQUAL_TO;
		return comparison;
	}
	
	private int compareStrings(String a, String b) {
		int comparison = a.toLowerCase().compareTo(b.toLowerCase());
		if (comparison < 0)
			comparison = LESS_THAN;
		else if (comparison > 0)
			comparison = GREATER_THAN;
		else
			comparison = EQUAL_TO;
		return comparison;
	}

}
