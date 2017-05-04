package org.paint.gui.event;

import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;


public class GeneDataEvent extends EventObject {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(GeneDataEvent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Set<GeneNode> nodes;

	public GeneDataEvent(Object source, Set<GeneNode> nodes) {
		super(source);
		this.nodes = nodes;
	}

	public GeneDataEvent(Object source, GeneNode node) {
		super(source);
		nodes = new HashSet<GeneNode> ();
		nodes.add(node);
	}

	public Set<GeneNode> getGenes() {
		return nodes;
	}

}
