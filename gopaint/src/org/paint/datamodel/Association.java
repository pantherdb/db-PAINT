/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.datamodel;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.Node;

/**
 *
 * @author muruganu
 */
public class Association {
    private Annotation annotation;
    private Node node;
//    private boolean nodeAssociatedToAnnotation = false;                 // We create associations for parent terms where child term is annotated.  Use this field
//                                                                        // to indicate association is not 'real'

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
//        if (true == annotation.isAnnotIsToChildTerm()) {
//            nodeAssociatedToAnnotation = false;
//        }
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

//    public boolean isNodeAssociatedToAnnotation() {
//        return nodeAssociatedToAnnotation;
//    }

//    public void setNodeAssociatedToAnnotation(boolean nodeAssociatedToAnnotation) {
//        this.nodeAssociatedToAnnotation = nodeAssociatedToAnnotation;
//    }
    
}
