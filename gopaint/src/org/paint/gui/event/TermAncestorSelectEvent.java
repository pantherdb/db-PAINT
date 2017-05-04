/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.gui.event;

import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import org.paint.datamodel.GeneNode;

/**
 *
 * @author muruganu
 */
public class TermAncestorSelectEvent extends EventObject {
    	private List<TermAncestor> termAncestorList;
	private GeneNode select_node;
        
        
        	public TermAncestorSelectEvent(Object source, TermAncestor termAncestor) {
		super(source);
		termAncestorList = new ArrayList<TermAncestor>();
		termAncestorList.add(termAncestor);
		this.select_node = null;
	}

	public TermAncestorSelectEvent(Object source, TermAncestor termAncestor, GeneNode node) {
		super(source);
		termAncestorList = new ArrayList<TermAncestor>();
		termAncestorList.add(termAncestor);
		this.select_node = node;
	}
    
}
