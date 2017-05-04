/**
 *  Copyright 2016 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.usc.ksom.pm.panther.paint.annotation;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.paint.datamodel.GeneNode;
import org.paint.main.PaintManager;
import com.sri.panther.paintCommon.util.QualifierDif;

/**
 *
 * @author muruganu
 */
public class PruneInfo {
//    private GeneNode gNode;
    HashSet<GeneNode> leavesWithExpEvdnce;
    HashMap<String, GeneNode> leafLookup;
    HashMap<GeneNode, ArrayList<Annotation>> deleteAnnotationLookup;
//    HashMap<GeneNode, ArrayList<Annotation>> updateAnnotationQualifierChangeLookup;
//    HashMap<GeneNode, ArrayList<Annotation>> updateAnnotationWithChangeLookup; // No qualifier change
    Annotation updateAnnotation;
    PaintManager pm = PaintManager.inst();
    GOTermHelper gth = pm.goTermHelper();
    
//    private static final int ANNOT_DELETE = 0;
//    private static final int ANNOT_UPDATE_QUALIFIER = 1;            // Qualifier and with has changed
//    private static final int ANNOT_UPDATE_WITH = 2;
//    private static final int ANNOT_NO_CHANGE = 3;
    
//    public PruneInfo(GeneNode gNode) {
//        this.gNode = gNode;
//        
//        // Get nodes that provide experimental evidence.  Create lookup for later use  
//        leavesWithExpEvdnce = AnnotationUtil.getDescWithExpEvdnce(gNode);
//        if (null == leavesWithExpEvdnce) {
//            return;
//        }
//        leafLookup = new HashMap<String, GeneNode>(leavesWithExpEvdnce.size());
//        for (GeneNode leaf: leavesWithExpEvdnce) {
//            leafLookup.put(leaf.getNode().getStaticInfo().getPublicId(), leaf);
//        }
//        
//        // 1.  Get All nodes in tree with the exception of clade from branch we want to prune
//        // 2.  Need to go through all these nodes and get list of annotations that will be removed or updated after we prune the node
//        
//        // 1. Get list of nodes we need to check
//        // Get list of descendents for node in question
//        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//        GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
//        
//        // Get list of descendents for root node
//        ArrayList<GeneNode> allDesc = new ArrayList<GeneNode>();
//        GeneNodeUtil.allNonPrunedDescendents(pm.getTree().getRoot(), allDesc);
//        allDesc.removeAll(descList);
//        allDesc.remove(gNode);
//        
//        // 2.  Go through annotation list and determine if annotation will be removed or updated
//        for (GeneNode gn: allDesc) {
//            Node n = gn.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                continue;
//            }
//            ArrayList<Annotation> goAnnot = nvi.getGoAnnotationList();
//            if (null == goAnnot) {
//                continue;
//            }
//            for (Annotation annot: goAnnot) {
//                int updateStatus = getAnnotUpdateStatus(annot);
//                switch(updateStatus) {
//                    case ANNOT_DELETE: {
//                        ArrayList<Annotation> deleteList = deleteAnnotationLookup.get(gn);
//                        if (null == deleteList) {
//                            deleteList = new ArrayList<Annotation>();
//                            deleteAnnotationLookup.put(gn, deleteList);
//                        }
//                        deleteList.add(annot);
//                        break;
//                    }
////                    case ANNOT_UPDATE_QUALIFIER: {
////                        ArrayList<Annotation> updateList = updateAnnotationQualifierChangeLookup.get(gn);
////                        if (null == updateAnnotationQualifierChangeLookup) {
////                            updateList = new ArrayList<Annotation>();
////                            updateAnnotationQualifierChangeLookup.put(gn, updateList);
////                        }
////                        updateList.add(annot);
////                        break;
////                    }
////                    case ANNOT_UPDATE_WITH: {
////                        ArrayList<Annotation> updateList = updateAnnotationWithChangeLookup.get(gn);
////                        if (null == updateAnnotationWithChangeLookup) {
////                            updateList = new ArrayList<Annotation>();
////                            updateAnnotationWithChangeLookup.put(gn, updateList);
////                        }
////                        updateList.add(annot);
////                        break;
////                    }
//                }
//            }
//        }
//        
//
//        deleteAnnotations();
//        
//        updateAnnotations();
//        
//        
//        
//        
//        
//    }
    
//    private void deleteAnnotations() {
//        HashMap<GeneNode, ArrayList<Annotation>> deleteLookup = getDeleteAnnotationLookup();
//        if (null != deleteLookup) {
//            Set<GeneNode> deleteSet = deleteLookup.keySet();
//            for (GeneNode gn: deleteSet) {
//                ArrayList<Annotation> deleteList = deleteLookup.get(gn);
//                ArrayList<Annotation> removeList = new ArrayList<Annotation>();
//                for (Annotation annot: deleteList) {
//                    Annotation delAnnot = PaintAction.inst().deleteAnnotation(gn, annot);
//                    if (null != delAnnot) {
//                        removeList.add(delAnnot);
//                    }
//                }
//                deleteList.removeAll(removeList);
//            }
//        }
//    }
//    
//    private void updateAnnotations() {
//        HashSet<GeneNode> allDesc = new HashSet<GeneNode>();
//        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//        GeneNodeUtil.allDescendents(gNode, descList);
//        allDesc.add(gNode);
//    }
//    
//    private int getAnnotUpdateStatus(Annotation annot){
//        Evidence e = annot.getEvidence();
//        HashSet<Qualifier> curSet = annot.getQualifierSet();
//        HashSet<Qualifier> qualifiersToBeRemoved = new HashSet<Qualifier>();
//        HashSet<Qualifier> qualifiersToBeKept = new HashSet<Qualifier>();
//
//        String term = annot.getGoTerm();
//        String code = e.getEvidenceCode();
//        ArrayList<DBReference> withs = (ArrayList<DBReference>)e.getWiths().clone();
//
//        HashSet<Qualifier> updatedSet = new HashSet<Qualifier>();
//        ArrayList <DBReference> updatedDBRef = new ArrayList<DBReference>();
//
//        int initialSize = withs.size();
//        if (GOConstants.DESCENDANT_SEQUENCES_EC.equals(code)) {
//            for (Iterator<DBReference> dbRefIter = withs.iterator(); dbRefIter.hasNext();) {
//                DBReference dbRef = dbRefIter.next();
//                if (false == GOConstants.PANTHER_DB.equals(dbRef.getEvidenceType())) {
//                    updatedDBRef.add(dbRef);
//                    continue;
//                }
//                String ptnId = dbRef.getEvidenceValue();
//                GeneNode leafNode = pm.getGeneByPTNId(ptnId);
//                
//                HashSet<Qualifier> qSet = new HashSet<Qualifier>();
//                ArrayList<Annotation> expAnnotForTerm = getExpAnnotForTerm(leafNode, term, curSet, qSet, gth);
//                if (null == leafLookup.get(ptnId)) {
//                    for (Qualifier q: qSet) {
//                        QualifierDif.addIfNotPresent(qualifiersToBeKept, q);
//                    }
//                    updatedDBRef.add(dbRef);
//                    continue;
//                }
//                else {
//                    if (null != expAnnotForTerm) {
//                        for (Qualifier q: qSet) {
//                            QualifierDif.addIfNotPresent(qualifiersToBeRemoved, q);
//                        }
//                        dbRefIter.remove();
//                    }
//                }
//
//            }
//            if (withs.isEmpty()) {
//                return ANNOT_DELETE;
//            }
//            if (withs.size() == initialSize) {
//                return ANNOT_NO_CHANGE;
//            }
//            
//            // Deal with qualifiers
//            if (null != curSet) {
//                for (Qualifier q: curSet) {
//                    if (true == QualifierDif.exists(qualifiersToBeKept, q)) {
//                        QualifierDif.addIfNotPresent(updatedSet, q);
//                    }
//                }
//                
//
//            }
//            
//            // Size has changed.  If this is a NOT annotation and leaf (or leaves) providing NOT evidence is going to be removed,
//            // we have to remove the NOT annotation.  If qualifiers other than NOT are getting removed, then only qualifiers will be updated, but, annotation
//            // can stay.            
//            if (true == QualifierDif.containsNegative(curSet) && false == QualifierDif.containsNegative(updatedSet)) {
//                    return ANNOT_DELETE;
//            }            
//            
//            updateAnnotation = new Annotation();
//            updateAnnotation.setGoTerm(term);
//            updateAnnotation.setAnnotIsToChildTerm(annot.isAnnotIsToChildTerm());
//
//            Evidence evidence = new Evidence();
//            evidence.setEvidenceCode(annot.getEvidence().getEvidenceCode());
//            evidence.setWiths(updatedDBRef);
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            evidence.addDbRef(dbRef);            
//            updateAnnotation.setEvidence(evidence);          
//            if (false == updatedSet.isEmpty() && null != curSet) {
//                updateAnnotation.setQualifierSet(updatedSet);
//            }    
//            if (null == curSet) {
//                return ANNOT_UPDATE_WITH;
//            }
//          
//            return ANNOT_UPDATE_QUALIFIER;
//        }
//        if (GOConstants.KEY_RESIDUES_EC.equals(code) || GOConstants.DIVERGENT_EC.equals(code) || GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(code)) {
//            boolean refRemoved = false;
//            for (Iterator<DBReference> dbRefIter = withs.iterator(); dbRefIter.hasNext();) {
//                DBReference dbRef = dbRefIter.next();
//                if (false == GOConstants.PANTHER_DB.equals(dbRef.getEvidenceType())) {
//                    updatedDBRef.add(dbRef);
//                    continue;
//                }
//                String ptnId = dbRef.getEvidenceValue();
//                GeneNode leafNode = leafLookup.get(ptnId);
//                if (null == leafNode) {
//                    updatedDBRef.add(dbRef);
//                    continue;
//                }
//                // Skip over the leaf node since, it is part of the pruned set.
//                refRemoved = true;
//            }
//            if (false == refRemoved) {
//                return ANNOT_NO_CHANGE;
//            }
//            
//            updateAnnotation = new Annotation();
//            updateAnnotation.setGoTerm(term);
//            updateAnnotation.setAnnotIsToChildTerm(annot.isAnnotIsToChildTerm());
//            if (null != annot.getQualifierSet()) {
//                updateAnnotation.setQualifierSet((HashSet<Qualifier>)annot.getQualifierSet().clone());
//            }
//            Evidence evidence = new Evidence();
//            evidence.setEvidenceCode(annot.getEvidence().getEvidenceCode());
//            evidence.setWiths(updatedDBRef);
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            evidence.addDbRef(dbRef);            
//            updateAnnotation.setEvidence(evidence);          
//            return ANNOT_UPDATE_WITH;
//        }
//                
//        return ANNOT_NO_CHANGE;
//    }
//    
    public static ArrayList<Annotation> getExpAnnotForTerm(GeneNode leaf, String term, HashSet<Qualifier> oldSet, HashSet<Qualifier> newSet, GOTermHelper gth) {
        ArrayList<Annotation> directAnnot = new ArrayList<Annotation>();
        ArrayList<Annotation> moreSpecificChildAnnot = new ArrayList<Annotation>();
        
        Node n = leaf.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null ==  nvi) {
            return null;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        HashSet<Qualifier> qFromChildAnnot = new HashSet<Qualifier>();
        boolean oldSetNegative = QualifierDif.containsNegative(oldSet);
        for (Annotation annot: annotList) {
            Evidence e = annot.getEvidence();
            if (false == e.isExperimental()) {
                continue;
            }
//            HashSet<Qualifier> curSet = annot.getQualifierSet();
//            // This has a negative qualifier and we are only looking for positive qualifiers
//            if (null != qSet && QualifierDif.allPositive(qSet) && null != curSet && QualifierDif.containsOnlyOneNOT(curSet)) {
//                continue;
//            }
//            if (null != qSet && QualifierDif.containsOnlyOneNOT(qSet) && null != curSet && QualifierDif.allPositive(curSet)) {
//                continue;
//            }
            if (true == annot.getGoTerm().equals(term)) {
                directAnnot.add(annot);
                HashSet<Qualifier> curSet = annot.getQualifierSet();
                if (null != curSet && null != oldSet) {
                    for (Qualifier q: curSet) {
                        // Only add qualifiers that were part of the old set
                        if (false == QualifierDif.exists(oldSet, q)) {
                            continue;
                        }
                        if (false == oldSetNegative && q.isNot()) {
                            continue;
                        }
                        QualifierDif.addIfNotPresent(newSet, q);
                    }
                }
                continue;
            }

            GOTerm gTerm = gth.getTerm(annot.getGoTerm());
            ArrayList<GOTerm> ancestors = gth.getAncestors(gTerm);
            if (ancestors.contains(gth.getTerm(term))) {
                moreSpecificChildAnnot.add(annot);
                HashSet<Qualifier> curSet = annot.getQualifierSet();
                if (null != curSet && null != oldSet) {
                    for (Qualifier q: curSet) {
                        // Only add qualifiers that were part of the old set
                        if (false == QualifierDif.exists(oldSet, q)) {
                            continue;
                        }
                        if (false == oldSetNegative && q.isNot()) {
                            continue;
                        }
                        QualifierDif.addIfNotPresent(qFromChildAnnot, q);
                    }
                }
            }
        }
        if (false == directAnnot.isEmpty()) {
            return directAnnot;
        }
        if (false == moreSpecificChildAnnot.isEmpty()) {
            // Get qualifiers from more specific child term
            if (false == qFromChildAnnot.isEmpty() && null != oldSet) {
                for (Qualifier q: qFromChildAnnot) {
                        // Only add qualifiers that were part of the old set
                        if (false == QualifierDif.exists(oldSet, q)) {
                            continue;
                        }
                        if (false == oldSetNegative && q.isNot()) {
                            continue;
                        }
                        QualifierDif.addIfNotPresent(newSet, q);
                    }
            }
            return moreSpecificChildAnnot;
        }
        return null;
    }

    public HashMap<GeneNode, ArrayList<Annotation>> getDeleteAnnotationLookup() {
        return deleteAnnotationLookup;
    }

//    public HashMap<GeneNode, ArrayList<Annotation>> getUpdateAnnotationQualifierChangeLookup() {
//        return updateAnnotationQualifierChangeLookup;
//    }
//
//    public HashMap<GeneNode, ArrayList<Annotation>> getUpdateAnnotationWithChangeLookup() {
//        return updateAnnotationWithChangeLookup;
//    }
    
}
