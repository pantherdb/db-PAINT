package org.paint.gui.event;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.geneontology.db.model.Term;
import org.paint.datamodel.GeneNode;

public class TermSelectEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//initialize logger
	private final static Logger logger = Logger.getLogger(TermSelectEvent.class);
	private List<Term> terms;
	private GeneNode select_node;
	
	public TermSelectEvent(Object source, List<Term> terms) {
		super(source);
		this.terms = terms;
		this.select_node = null;
	}

	public TermSelectEvent(Object source, Term term) {
		super(source);
		terms = new ArrayList<Term>();
		terms.add(term);
		this.select_node = null;
	}

	public TermSelectEvent(Object source, Term term, GeneNode node) {
		super(source);
		terms = new ArrayList<Term>();
		terms.add(term);
		this.select_node = node;
	}

	public List<Term> getTermSelection() {
		return terms;
	}

	public GeneNode selectNode() {
		return select_node;
	}

}

