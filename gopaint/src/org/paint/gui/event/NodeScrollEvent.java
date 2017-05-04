package org.paint.gui.event;

import java.awt.event.AdjustmentEvent;

import org.apache.log4j.Logger;



public class NodeScrollEvent extends AdjustmentEvent {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(NodeScrollEvent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private float scroll_position;
	
	public NodeScrollEvent(AdjustmentEvent orig, float scroll_position) {
		super(orig.getAdjustable(), orig.getID(), orig.getAdjustmentType(), orig.getValue());
		this.scroll_position = scroll_position;
	}

	public float getPosition() {
		return scroll_position;
	}

}
