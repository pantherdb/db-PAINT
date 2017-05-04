package org.paint.gui.event;

import java.util.EventObject;

import org.apache.log4j.Logger;


public class CurationColorEvent extends EventObject {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(CurationColorEvent.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int action;

	public CurationColorEvent(Object source) {
		super(source);
	}
}
