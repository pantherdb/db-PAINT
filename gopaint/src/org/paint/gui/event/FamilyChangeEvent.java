package org.paint.gui.event;

import java.util.EventObject;

import org.paint.datamodel.Family;


/** this is for dataloading - contains the newly loaded gene family  */

public class FamilyChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Family       family;

	public FamilyChangeEvent(Object source, Family data_bag) {
		super(source);
		this.family = data_bag;
	}
}
