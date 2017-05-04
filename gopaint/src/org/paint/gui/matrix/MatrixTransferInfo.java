/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.gui.matrix;

import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import org.paint.datamodel.GeneNode;

/**
 *
 * @author muruganu
 */
public class MatrixTransferInfo {
    TermAncestor termAncestor;
    GeneNode matrixClickedNode;
    
    
    MatrixTransferInfo(TermAncestor termAncestor, GeneNode matrixClickedNode) {
        this.termAncestor = termAncestor;
        this.matrixClickedNode = matrixClickedNode;
    }

    public TermAncestor getTermAncestor() {
        return termAncestor;
    }

    public GeneNode getMatrixClickedNode() {
        return matrixClickedNode;
    }
    
}
