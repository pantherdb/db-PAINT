///**
// *  Copyright 2018 University Of Southern California
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *  http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//package edu.usc.ksom.pm.panther.paint.annotation;
//
//import edu.usc.ksom.pm.panther.paintCommon.Annotation;
//import edu.usc.ksom.pm.panther.paintCommon.DBReference;
//import edu.usc.ksom.pm.panther.paintCommon.Evidence;
//import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
//import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
//import edu.usc.ksom.pm.panther.paintCommon.Node;
//import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
//import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import org.paint.datamodel.GeneNode;
//import org.paint.go.GOConstants;
//import org.paint.go.GO_Util;
//import org.paint.main.PaintManager;
//import org.paint.util.GeneNodeUtil;
//import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;;
//
///**
// *
// * @author muruganu
// */
//public class UpdateAnnotation {
//    private boolean graft = true;
//    ArrayList<GeneNode> descList;
//    PaintManager pm;
//    private ArrayList<DBReference> newWiths = new ArrayList<DBReference>();
//    private HashSet<GeneNode> withNodes = new HashSet<GeneNode>();
//    private HashSet<Qualifier> newQualifierSet = new HashSet<Qualifier>();    
//    private boolean updateRequired = false;
//    
//    public UpdateAnnotation(GeneNode gn, Annotation origAnnot, boolean graft, ArrayList<GeneNode> descList, PaintManager pm) {
//        this.graft = graft;
//        this.descList = descList;
//        this.pm = this.pm;
//        Evidence e = origAnnot.getEvidence();
//        String term = origAnnot.getGoTerm();
//        String code = e.getEvidenceCode();
//        ArrayList<DBReference> withs = e.getWiths();
//        
//        ArrayList<GeneNode> leaves = new ArrayList<GeneNode>();
//        for (GeneNode desc: descList) {
//            if (true == desc.isLeaf()) {
//                leaves.add(desc);
//            }
//        }
//        
//        if (true == graft) {
//            if (true == code.equals(GOConstants.DESCENDANT_SEQUENCES_EC)) {
//                calculateWiths(origAnnot, leaves, pm.goTermHelper());
//                if (true == updateRequired) {
//                    updateAnnotationForIBDafterGraft(gn, origAnnot);
//                }
//                return;
//            }
//            if (true == code.equals(GOConstants.KEY_RESIDUES_EC)) {
//                updateIKRafterGraft(gn, origAnnot);
//                return;
//            }
//            if (true == code.equals(GOConstants.DIVERGENT_EC)) {
//                updateIRDafterGraft(gn, origAnnot);
//                // no changes required
//                return;
//            }
//            return;
//        }
//        // Removing descendents
//
//        
//        if (true == code.equals(GOConstants.DESCENDANT_SEQUENCES_EC)) {
//            if (null == withs) {
//                return;
//            }
//
//            for (DBReference with: withs) {
//                if (false == GOConstants.PANTHER_DB.equals(with.getEvidenceType())) {
//                    newWiths.add(with);
//                    continue;
//                }
//                GeneNode gNode = pm.getGeneByPaintId(with.getEvidenceValue());
//                if (null == gNode) {
//                    newWiths.add(with);
//                    continue;                    
//                }
//                HashSet<Qualifier> curSet = new HashSet<Qualifier>();
//                ArrayList<Annotation> annots = PruneInfo.getExpAnnotForTerm(gNode, term, origAnnot.getQualifierSet(),curSet, pm.goTermHelper());
//                if (false == descList.contains(gNode) && null != annots) {
//                    newWiths.add(with);
//                    for (Qualifier q: curSet) {
//                        QualifierDif.addIfNotPresent(newQualifierSet, q);
//                    }                    
//                    continue;                       
//                }
//                else {
//                    updateRequired = true;
//                }
//            }
//            if (false == QualifierDif.allQualifiersSame(origAnnot.getQualifierSet(), newQualifierSet)) {
//                updateRequired = true;
//            }
//            if (newQualifierSet.isEmpty()) {
//                newQualifierSet = null;
//            }
//            return;
//        }
//        if (true == code.equals(GOConstants.KEY_RESIDUES_EC) || true == code.equals(GOConstants.DIVERGENT_EC)) {
//            HashSet<Qualifier> qSet = origAnnot.getQualifierSet();
//            if (null !=  qSet) {
//                newQualifierSet = (HashSet<Qualifier>)qSet.clone();
//            }
//            for (DBReference with: withs) {
//                if (false == GOConstants.PANTHER_DB.equals(with.getEvidenceType())) {
//                    newWiths.add(with);
//                    continue;
//                }
//                GeneNode gNode = pm.getGeneByPaintId(with.getEvidenceValue());
//                if (null == gNode) {
//                    newWiths.add(with);
//                    continue;                    
//                }
//                if (true == descList.contains(gNode)) {
//                    updateRequired = true;
//                    continue;
//                }
//                newWiths.add(with);
//            }
//            
//            if (newQualifierSet.isEmpty()) {
//                newQualifierSet = null;
//            }
//        }
//    }
//
//    
//    private Annotation getIBAIKR(GeneNode gNode, String propagatorId, String termAcc, String searchCode) {
//        Node n = gNode.getNode();
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null == nvi) {
//            return null;
//        }
//        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
//        if (null == goAnnotList) {
//            return null;
//        }
//        for (Annotation a: goAnnotList) {
//            if (false == termAcc.equals(a.getGoTerm())) {
//                continue;
//            }
//            if (null != a.getParentAnnotation()) {
//                continue;
//            }
//            Evidence e = a.getEvidence();
//            String code = e.getEvidenceCode();
//            if (false == searchCode.equals(code)) {
//                continue;
//            }
//            for (DBReference dbRef: e.getWiths()) {
//                if (true == dbRef.getEvidenceType().equals(GOConstants.PANTHER_DB) && true == dbRef.getEvidenceValue().equals(propagatorId)) {
//                    return a;
//                }
//            }
//        }
//        return null;        
//    }
//    
//    private boolean hasIRD_IKRforPropagator(GeneNode gNode, String propagatorId, String termAcc) {
//        Node n = gNode.getNode();
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null == nvi) {
//            return false;
//        }
//        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
//        if (null == goAnnotList) {
//            return false;
//        }
//        for (Annotation a: goAnnotList) {
//            if (false == termAcc.equals(a.getGoTerm())) {
//                continue;
//            }
//            Evidence e = a.getEvidence();
//            String code = e.getEvidenceCode();
//            if (false == GOConstants.DIVERGENT_EC.equals(code) && false == GOConstants.KEY_RESIDUES_EC.equals(code)) {
//                continue;
//            }
//            for (DBReference dbRef: e.getWiths()) {
//                if (true == dbRef.getEvidenceType().equals(GOConstants.PANTHER_DB) && true == dbRef.getEvidenceValue().equals(propagatorId)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//   
//    
//    private void updateAnnotationForIBDafterGraft(GeneNode gNode, Annotation annot) {
//        ArrayList<DBReference> curWith = (ArrayList<DBReference>)newWiths.clone();
//        annot.getEvidence().setWiths(curWith);
//        if (null != newQualifierSet) {
//            annot.setQualifierSet((HashSet<Qualifier>)newQualifierSet.clone());
//            if (0 == annot.getQualifierSet().size()) {
//                annot.setQualifierSet(null);
//            }
//        }
//        else {
//            annot.setQualifierSet(null);
//        }
//        List<GeneNode> children = gNode.getChildren();
//        if (null == children) {
//            return;
//        }
//        String propagatorId = gNode.getNode().getStaticInfo().getPublicId();
//        DBReference dbRef = new DBReference();
//        dbRef.setEvidenceType(GOConstants.PANTHER_DB);
//        dbRef.setEvidenceValue(propagatorId);
//        ArrayList<DBReference> withList = (ArrayList<DBReference>)newWiths.clone();
//        withList.add(dbRef);
//        for (GeneNode child: children) {
//            if (child.isPruned()) {
//                continue;
//            }
//            if (withNodes.contains(child)) {
//                continue;
//            }
//            if (true == hasIRD_IKRforPropagator(child, propagatorId, annot.getGoTerm())) {
//                continue;
//            }
//            updateDescIBAafterGraft(propagatorId, child, annot.getGoTerm(), withList);
//        }
//        
//        
//    }
//    
//    private void updateDescIBAafterGraft(String propagatorId, GeneNode gn, String termAcc, ArrayList<DBReference> withList) {
//        Annotation a = getIBAIKR(gn, propagatorId, termAcc, GOConstants.ANCESTRAL_EVIDENCE_CODE);
//
//        if (null == a) {
//            a = new Annotation();
//            a.setGoTerm(termAcc);
//            Evidence e = new Evidence();
//            e.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            e.addDbRef(dbRef);
//            a.setEvidence(e);
//            Node n = gn.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                nvi = new NodeVariableInfo();
//                n.setVariableInfo(nvi);
//            }
//            nvi.addGOAnnotation(a);
//        }
//        Evidence e = a.getEvidence();
//        e.setWiths((ArrayList<DBReference>)withList.clone());
//        if (null == newQualifierSet) {
//            a.setQualifierSet(null);
//        }
//        else {
//            a.setQualifierSet((HashSet<Qualifier>)newQualifierSet.clone());
//            if (0 == a.getQualifierSet().size()) {
//                a.setQualifierSet(null);
//            }
//        }
//        List<GeneNode> children = gn.getChildren();
//        if (null == children) {
//            return;
//        }
//        for (GeneNode child: children) {
//            if (child.isPruned()) {
//                continue;
//            }
//            if (withNodes.contains(child)) {
//                continue;
//            }
//            if (true == hasIRD_IKRforPropagator(child, propagatorId, termAcc)) {
//                continue;
//            }
//            updateDescIBAafterGraft(propagatorId, child, termAcc, withList);
//        }        
//        
//    }
//    
//    public void updateIRDafterGraft(GeneNode gNode, Annotation annot) {
//        String propagatorId = null;
//        ArrayList<DBReference> withs = annot.getEvidence().getWiths();
//        if (null == withs) {
//            System.out.println("Did not find withs for IRD after graft");
//            return;
//        }
//        
//        for (DBReference dbRef: withs) {
//            if (true == GOConstants.PANTHER_DB.equals(dbRef.getEvidenceType())) {
//                GeneNode node = pm.getGeneByPaintId(dbRef.getEvidenceValue());
//                if (null != node && false == node.isLeaf()) {
//                    propagatorId = dbRef.getEvidenceValue();
//                    break;
//                }
//            }
//        }
//        if (null == propagatorId) {
//            System.out.println("Did not find propagator for IRD after graft");            
//            return;
//        }
//        ArrayList<GeneNode> leaves = getLeavesFromIBDpropagator(propagatorId, annot.getGoTerm());
//        Node node = gNode.getNode();
//        leaves.add(gNode);
//        if (null != annot.getChildAnnotation() && true == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(annot.getChildAnnotation().getEvidence().getEvidenceCode())) {
//            updateDescAfterGraft(gNode, node.getStaticInfo().getPublicId(), GOConstants.ANCESTRAL_EVIDENCE_CODE, annot.getGoTerm(), annot.getQualifierSet(), leaves);
//        }        
//    }
//    
//    public void updateIKRafterGraft(GeneNode gNode, Annotation annot) {
//        String propagatorId = null;
//        ArrayList<DBReference> withs = annot.getEvidence().getWiths();
//        if (null == withs) {
//            System.out.println("Did not find withs for IKR after graft");
//            return;
//        }
//        
//        for (DBReference dbRef: withs) {
//            if (true == GOConstants.PANTHER_DB.equals(dbRef.getEvidenceType())) {
//                GeneNode node = pm.getGeneByPaintId(dbRef.getEvidenceValue());
//                if (null != node && false == node.isLeaf()) {
//                    propagatorId = dbRef.getEvidenceValue();
//                    break;
//                }
//            }
//        }
//        if (null == propagatorId) {
//            System.out.println("Did not find propagator for IKR after graft");            
//            return;
//        }
//        ArrayList<GeneNode> leaves = getLeavesFromIBDpropagator(propagatorId, annot.getGoTerm());
//        
//        
//        Node node = gNode.getNode();
//        leaves.add(gNode);
//        updateDescAfterGraft(gNode, node.getStaticInfo().getPublicId(), GOConstants.KEY_RESIDUES_EC, annot.getGoTerm(), annot.getQualifierSet(), leaves);
//        if (null != annot.getChildAnnotation() && true == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(annot.getChildAnnotation().getEvidence().getEvidenceCode())) {
//            updateDescAfterGraft(gNode, node.getStaticInfo().getPublicId(), GOConstants.ANCESTRAL_EVIDENCE_CODE, annot.getGoTerm(), annot.getQualifierSet(), leaves);
//        }
//        
//    }
//    
//    public void updateDescAfterGraft(GeneNode gNode, String propagatorId, String code, String term, HashSet<Qualifier> qualifierSet, ArrayList<GeneNode> noUpdateNodes) {
//        if (false == noUpdateNodes.contains(gNode)) {
//            Annotation annot = getIBAIKR(gNode, propagatorId, term, code);
//            if (null == annot) {
//                annot = new Annotation();
//                if (null != qualifierSet) {
//                    annot.setQualifierSet((HashSet<Qualifier>)qualifierSet.clone());
//                }
//                Evidence e = new Evidence();
//                e.setEvidenceCode(GOConstants.KEY_RESIDUES_EC);
//                annot.setEvidence(e);
//                DBReference dbRef = new DBReference();
//                dbRef.setEvidenceType(GOConstants.PANTHER_DB);
//                dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//                e.addDbRef(dbRef);
//                DBReference with = new DBReference();
//                with.setEvidenceType(GOConstants.PANTHER_DB);
//                dbRef.setEvidenceValue(propagatorId);
//            }
//        }
//        List<GeneNode> children  = gNode.getChildren();
//        if (null == children) {
//            return;
//        }
//        for (GeneNode child: children) {
//            if (true == child.isPruned()) {
//                continue;
//            }
//            updateDescAfterGraft(child, propagatorId, code, term, qualifierSet, noUpdateNodes);
//        }
//        
//    }
//    
//    
//    public void updateForIRDandIKRAfterPrune(GeneNode gNode, Annotation annot) {
//        ArrayList<DBReference> withs = (ArrayList<DBReference>)newWiths.clone();
//        annot.getEvidence().setDbReferenceList(withs);
//    }
//    
//    public void updateForIBDAfterPrune(GeneNode gNode, Annotation annot) {
//        if (null == newQualifierSet) {
//            annot.setQualifierSet(newQualifierSet);
//        }
//        else {
//            annot.setQualifierSet((HashSet<Qualifier>)newQualifierSet.clone());
//        }
//        ArrayList<DBReference> withs = (ArrayList<DBReference>)newWiths.clone();
//        annot.getEvidence().setDbReferenceList(withs);
//        DBReference dbRef = new DBReference();
//        dbRef.setEvidenceType(GOConstants.PANTHER_DB);
//        String publicId = gNode.getNode().getStaticInfo().getPublicId();
//        dbRef.setEvidenceValue(publicId);
//        ArrayList<DBReference> withsDesc = (ArrayList<DBReference>)withs.clone();
//        withsDesc.add(dbRef);
//        
//        
//        String term = annot.getGoTerm();
//        ArrayList<Annotation> updateAnnotList = getAnnotList(gNode, publicId, term);
//        for (Annotation a: updateAnnotList) {
//            Evidence e = a.getEvidence();
//            String code = e.getEvidenceCode();
//            if (false == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(code)) {
//                continue;
//            }
//            a.setQualifierSet((HashSet<Qualifier>)annot.getQualifierSet().clone());
//            e.setWiths((ArrayList<DBReference>)withsDesc.clone());
//        }
//        
//        
//    }
//    
//    public ArrayList<Annotation> getAnnotList(GeneNode gNode, String publicId, String term) {
//        ArrayList<Annotation> updateList = new ArrayList<Annotation>();
//        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//        GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
//        
//        for (GeneNode gn: descList) {
//            Node n = gn.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                continue;
//            }
//            ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
//            if (null == goAnnotList) {
//                continue;
//            }
//            for (Annotation annot: goAnnotList) {
//                if (false == annot.getGoTerm().equals(term)) {
//                    continue;
//                }
//                ArrayList<DBReference> dbRefList = annot.getEvidence().getWiths();
//                for (DBReference dbRef: dbRefList) {
//                    if (false == GOConstants.PANTHER_DB.equals(dbRef.getEvidenceType())) {
//                        continue;
//                    }
//                    if (dbRef.getEvidenceValue().equals(publicId)) {
//                        updateList.add(annot);
//                        break;
//                    }
//                }
//            }
//                
//        }
//        return updateList;
//    }
//    
//    public void calculateWiths(Annotation annot, ArrayList<GeneNode> leaves, GOTermHelper gth) {
//        boolean containsNot = QualifierDif.containsNegative(annot.getQualifierSet());
////        ArrayList<DBReference> withs = new ArrayList<DBReference>();
//        String term = annot.getGoTerm();
//        GOTerm gTerm = gth.getTerm(term);
//        HashSet<Qualifier> qSet = new HashSet<Qualifier>();        
//        ArrayList<DBReference> dbRefList = new ArrayList<DBReference>();
//            
//        for (GeneNode leaf: leaves) {
//            Node n = leaf.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                continue;
//            }
//            
//            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//            if (null == annotList) {
//                continue;
//            }
//
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PANTHER_DB);
//            dbRef.setEvidenceValue(n.getStaticInfo().getPublicId());
//            for (Annotation a: annotList) {
//                //Evidence e = a.getEvidence();
//                if (false == a.isExperimental()) {
//                    continue;
//                }
//                String curTermStr = a.getGoTerm();
//                if (true == term.equals(curTermStr)) {
//                    DBReference.addIfNotPresent(dbRefList, dbRef);
//                    withNodes.add(leaf);
//                    if (null == a.getQualifierSet()) {
//                        continue;
//                    }
//                    for (Qualifier q: a.getQualifierSet()) {
//                        if (false == containsNot && q.isNot()) {
//                            continue;
//                        }
//                        QualifierDif.addIfNotPresent(qSet, q);
//                    }
//                    continue;
//                }
//                ArrayList<GOTerm> ancestors = gth.getAncestors(gth.getTerm(curTermStr));
//                if (ancestors.contains(gTerm)) {
//                    DBReference.addIfNotPresent(dbRefList, dbRef);
//                    withNodes.add(leaf);
//                    
//                    if (null == a.getQualifierSet()) {
//                        continue;
//                    }
//                    for (Qualifier q: a.getQualifierSet()) {
//                        if (false == containsNot && q.isNot()) {
//                            continue;
//                        }
//                        QualifierDif.addIfNotPresent(qSet, q);
//                    }
//                    continue;
//                }
//                
//            }
//        }
//
//        
//        if (withNodes.isEmpty()) {
//            updateRequired = false;
//            return;
//           
//        }
//        newWiths = dbRefList;
//        newQualifierSet = qSet;
//        updateRequired = true;
//    }
//    
//    private static ArrayList<GeneNode> getLeavesFromIBDpropagator(String propagatorId, String term) {
//        // First get annotation in question
//        GeneNode propagator = PaintManager.inst().getGeneByPaintId(propagatorId);
//        if (null == propagator) {
//            return null;
//        }
//        Node n = propagator.getNode();
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null == nvi) {
//            return null;
//        }
//        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//        if (null == annotList) {
//            return null;
//        }
//        for (Annotation annot: annotList) {
//            if (false == term.equals(annot.getGoTerm()) || false == GOConstants.DESCENDANT_SEQUENCES_EC.equals(annot.getEvidence().getEvidenceCode())) {
//                continue;
//            }
//            ArrayList<GeneNode> rtnList = new ArrayList<GeneNode>();
//            for (DBReference dbRef: annot.getEvidence().getWiths()) {
//                if (false == GOConstants.PANTHER_DB.equals(dbRef.getEvidenceType())) {
//                    continue;
//                }
//                GeneNode gn = PaintManager.inst().getGeneByPaintId(dbRef.getEvidenceValue());
//                rtnList.add(gn);
//            }
//            return rtnList;
//        }
//        return null;
//    } 
//
//    public ArrayList<DBReference> getNewWiths() {
//        return newWiths;
//    }
//
//    public HashSet<Qualifier> getNewQualifierSet() {
//        return newQualifierSet;
//    }
//
//    public boolean isUpdateRequired() {
//        return updateRequired;
//    }
//    
//}
