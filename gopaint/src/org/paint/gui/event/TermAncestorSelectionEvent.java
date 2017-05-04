/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.gui.event;

import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import java.util.EventObject;
import org.paint.datamodel.GeneNode;

/**
 *
 * @author muruganu
 */
public class TermAncestorSelectionEvent extends EventObject {
    	private TermAncestor termAncestor;
	private GeneNode gNode;
        
        public TermAncestorSelectionEvent(Object source, TermAncestor termAncestor, GeneNode gNode) {
            super(source);
            this.termAncestor = termAncestor;
            this.gNode = gNode;
        }
        
        public TermAncestor getTermAncestorSelection() {
		return termAncestor;
	}

	public GeneNode getNode() {
		return gNode;
	}
}
