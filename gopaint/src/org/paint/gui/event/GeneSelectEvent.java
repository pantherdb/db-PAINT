package org.paint.gui.event;

import java.util.EventObject;
import java.util.List;

import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;


public class GeneSelectEvent extends EventObject {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(GeneSelectEvent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected List<GeneNode> genes;
	protected GeneNode ancestor;
	protected List<GeneNode> previous;

	public GeneSelectEvent(Object source, List<GeneNode> genes, GeneNode ancestor) {
		super(source);
		this.genes = genes;
		this.ancestor = ancestor;
	}

	public List<GeneNode> getGenes() {
		return genes;
	}

	public void setPrevious(List<GeneNode> previous) {
		this.previous = previous;
	}

	public List<GeneNode> getPrevious() {
		return previous;
	}

	public GeneNode getAncestor() {
		return ancestor;
	}

	public void setAncestor(GeneNode ancestor) {
		this.ancestor = ancestor;
	}
}
