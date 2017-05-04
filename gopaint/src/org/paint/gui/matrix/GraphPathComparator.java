package org.paint.gui.matrix;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.geneontology.db.model.GraphPath;
import org.geneontology.db.model.Term;


public class GraphPathComparator implements Comparator<GraphPath> {

	public static final int LESS_THAN = -1;
	public static final int GREATER_THAN = 1;
	public static final int EQUAL_TO = 0;

	private Map<Term, List<GraphPath>> parent_groups;
	
	public GraphPathComparator(Map<Term, List<GraphPath>> parent_groups) {
		super();
		this.parent_groups = parent_groups;
	}
	
	public int compare(GraphPath fa, GraphPath fb) {
		
		int distance_a = fa.getDistance();
		int distance_b = fb.getDistance();

		int comparison = compareDistance(distance_a, distance_b);
		
		/*
		 * If the distances from the parent are the same then 
		 * sort by how many further descendants each child has
		 * more is higher in the list
		 */	
		if (comparison == EQUAL_TO) {
			comparison = compareProlific(fa.getSubject(), fb.getSubject());
		}
		
		/* 
		 * If the 2 paths are have the same # of jumps to the parent
		 * and the 2 paths have the same # of descendants
		 * then resort to a simple alphabetical sort
		 */		
		if (comparison == EQUAL_TO) {
			comparison = compareStrings(fa.getSubject().getName(), fb.getSubject().getName());
		}
		return comparison;
	}

	private int compareDistance(int a, int b) {
		int comparison = a - b;
		if (comparison < 0)
			comparison = LESS_THAN;
		else if (comparison > 0)
			comparison = GREATER_THAN;
		else
			comparison = EQUAL_TO;
		return comparison;
	}
	
	private int compareProlific(Term a, Term b) {
		List<GraphPath> a_list = parent_groups.get(a);
		int a_descendants = a_list != null ? a_list.size() : 0;
		List<GraphPath> b_list = parent_groups.get(b);
		int b_descendants = b_list != null ? b_list.size() : 0;
		int comparison = b_descendants - a_descendants;
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
