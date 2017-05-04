///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package edu.usc.ksom.pm.panther.paint.annotation;
//
//import com.sri.panther.paintCommon.Annotation;
//import com.sri.panther.paintCommon.Evidence;
//import com.sri.panther.paintCommon.Node;
//import com.sri.panther.paintCommon.NodeVariableInfo;
//import java.util.ArrayList;
//import java.util.List;
//import org.paint.datamodel.GeneNode;
//import org.paint.main.PaintManager;
//import org.paint.util.GeneNodeUtil;
//
///**
// *
// * @author muruganu 
// */
//public class GraftBranch {
//    private GeneNode gNode;
//    private PaintManager pm;
//    public GraftBranch(GeneNode gNode) {
//        this.gNode = gNode;
//        pm = PaintManager.inst();
//        
//        updateAnnotations(pm.getTree().getRoot());
//    }
//    
//    private void updateAnnotations(GeneNode gNode) {
//        if (true == gNode.isPruned()) {
//            return;
//        }
//        
//        Node node = gNode.getNode();
//        NodeVariableInfo nvi = node.getVariableInfo();
//        if (null != nvi) {
//
//            ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//            GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
//            
//            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//            if (null != annotList) {
//                for (Annotation annot: annotList) {
//                    UpdateAnnotation ua = new UpdateAnnotation(gNode, annot, true, descList, pm);
//                }
//            }
//        }
//        
//        
//        
//        
//        List<GeneNode> children = gNode.getChildren();
//        if (null != children) {
//            for (GeneNode child: children) {
//                updateAnnotations(child);
//            }
//        }
//    }
//}
