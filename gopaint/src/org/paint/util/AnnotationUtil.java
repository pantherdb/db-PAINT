/**
 *  Copyright 2020 University Of Southern California
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
package org.paint.util;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import edu.usc.ksom.pm.panther.paintCommon.WithEvidence;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;;
import edu.usc.ksom.pm.panther.paint.annotation.AnnotationForTerm;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationHelper;
import edu.usc.ksom.pm.panther.paintCommon.IWith;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import org.paint.datamodel.GeneNode;
import org.paint.go.GOConstants;
import org.paint.gui.AspectSelector;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.gui.familytree.TreePanel;
import org.paint.gui.msa.MSAPanel;
import org.paint.main.PaintManager;

/**
 *
 * @author muruganu
 */
public class AnnotationUtil {

    // Called after tree and annotations are loaded from database to propagate annotations that are not stored on the server
//    public static void propagateAnnotations(GeneNode gn) {
//        Node n = gn.getNode();
//        // Do not need to propagate from leaves
//        if (true == gn.isLeaf()) {
//            return;
//        }
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null != nvi && false ==  nvi.isPruned()) {
//            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//            if (null == annotList) {
//                return;
//            }
//            
//            ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//            GeneNodeUtil.allNonPrunedDescendents(gn, descList);
//            for (Annotation annot: annotList) {
//                if (true != annot.isAnnotStoredInDb()) {
//                    continue;
//                }
//                Evidence e = annot.getEvidence();
//                String eCode = e.getEvidenceCode();
//                if (GOConstants.DESCENDANT_SEQUENCES_EC.equals(eCode)) {
//                    propagateIBD(gn, annot, descList);
//                    continue;
//                }
//                else if (GOConstants.DIVERGENT_EC.equals(eCode)) {
//                    propagateIRD(gn, annot, descList);
//                    continue;
//                }
//                else if (GOConstants.KEY_RESIDUES_EC.equals(eCode)) {
//                    propagateIKR(gn, annot, descList);                    
//                    continue;
//                }
//                else if (GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(eCode)) {
//                    Annotation parentAnnot = annot.getParentAnnotation();
//                    if (null != parentAnnot) {
//                        String parentECode = parentAnnot.getEvidence().getEvidenceCode();
//                        if (true == GOConstants.DIVERGENT_EC.equals(parentECode) || true == GOConstants.KEY_RESIDUES_EC.equals(parentECode)) {
//                            // This is okay we will handle witth the IRD or IKR
//                            continue;
//                        }
//                        else {
//                            System.out.println("Not propagating " + eCode + " for node " + gn.getPersistantNodeID());
//                        }
//                    }
//                }                
//                else {
//                    System.out.println("Not propagating " + eCode + " for node " + gn.getPersistantNodeID());
//                }
//                
//            }
//        }
//        List<GeneNode> children = gn.getChildren();
//        if (null != children) {
//            for (GeneNode child : children) {
//                propagateAnnotations(child);
//            }
//        }
//    }
//    private static void propagateIBD(GeneNode propagator, Annotation propagatorAnnot, ArrayList<GeneNode> descNodes) {
//        HashSet<Annotation> withAnnotSet = propagatorAnnot.getAnnotationDetail().getWithAnnotSet();
//        HashSet<GeneNode> withNodeSet = new HashSet<GeneNode>();
//        if (null != withAnnotSet) {
//            PaintManager pm = PaintManager.inst();
//            for (Annotation annot : withAnnotSet) {
//                Node n = annot.getAnnotationDetail().getAnnotatedNode();
//                withNodeSet.add(pm.getGeneByPTNId(n.getStaticInfo().getPublicId()));
//            }
//        }
//        for (GeneNode desc : descNodes) {
//            if (withNodeSet.contains(desc)) {
//                continue;
//            }
//            Annotation a = new Annotation();
//            Node n = desc.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                nvi = new NodeVariableInfo();
//                n.setVariableInfo(nvi);
//            }
//            nvi.addGOAnnotation(a);
//            a.setGoTerm(propagatorAnnot.getGoTerm());
//            HashSet<Qualifier> qSet = propagatorAnnot.getQualifierSet();
//            if (null != qSet) {
//                a.setQualifierSet((HashSet<Qualifier>) qSet.clone());
//            }
//            Evidence e = new Evidence();
//            a.setEvidence(e);
//            e.setEvidenceCode(Evidence.CODE_IBA);
//            AnnotationDetail ad = a.getAnnotationDetail();
//            ad.setAnnotatedNode(n);
//            ad.addWith(propagatorAnnot);
//            if (null != qSet) {
//                for (Qualifier q : qSet) {
//                    ad.addToInheritedQualifierLookup(q, propagatorAnnot);
//                }
//            }
//        }
//
//    }
//
//    private static void propagateIBDOld(GeneNode propagator, Annotation propagatorAnnot, ArrayList<GeneNode> descNodes) {
//        Evidence e = propagatorAnnot.getEvidence();
//        ArrayList<GeneNode> nodesProvidingEvidence = getNodesProvidingExperimentalEvidence(propagatorAnnot, propagator);
//        ArrayList<GeneNode> descCopy = (ArrayList<GeneNode>) descNodes.clone();
//        if (null != nodesProvidingEvidence) {
//            descCopy.removeAll(nodesProvidingEvidence);
//        }
//
//        String propagatorPublicId = propagator.getNode().getStaticInfo().getPublicId();
//        ArrayList<DBReference> withs = getWiths(e.getWiths(), propagatorPublicId);
//        if (null == withs) {
//            withs = new ArrayList<DBReference>();
//        }
//        DBReference with = new DBReference();
//        with.setEvidenceType(GOConstants.PANTHER_DB);
//        with.setEvidenceValue(propagatorPublicId);
//        withs.add(with);
//
//        propagate(propagatorAnnot.getGoTerm(), GOConstants.ANCESTRAL_EVIDENCE_CODE, propagatorAnnot.getEvidence().getDbReferenceList(), withs, descCopy, propagatorAnnot.getQualifierSet());
//
//    }

//    private static void propagateIRD(GeneNode propagator, Annotation propagatorAnnot, ArrayList<GeneNode> descNodes) {
//        // Check for annotation to ancestral term
//        Annotation ancestorAnnot = propagatorAnnot.getChildAnnotation();
//        if (null == ancestorAnnot) {
//            return;
//        }
//        Annotation with = null;
//        for (Annotation a: propagatorAnnot.getAnnotationDetail().getWithAnnotSet()) {
//            with = a;
//            break;
//        }        
//        if (null == with) {
//            System.out.println("Couldn not find propagator for IRD annotation " + propagatorAnnot.getAnnotationId());
//            return;
//        }
//        
//        
//    }    
//    private static void propagateIRDold(GeneNode propagator, Annotation propagatorAnnot, ArrayList<GeneNode> descNodes) {
//        // Get Node that created IBD and delete its annotations starting at current node and all applicable descendants
//        GeneNode origPropagator = getPAINTWithNode(propagatorAnnot);
//        Annotation origAnnotation = getPropagatorsNOTAnnotation(propagatorAnnot.getGoTerm(), origPropagator, propagatorAnnot.getQualifierSet());
//
//        ArrayList<GeneNode> descCopy = (ArrayList<GeneNode>) descNodes.clone();
//        descCopy.add(propagator);           // Delete annotation from propagator
//        deletePropagatorsAnnotationFromDescendants(origPropagator, origAnnotation, descCopy, true);
//
//        Annotation childAnnot = propagatorAnnot.getChildAnnotation();
//        Evidence e = childAnnot.getEvidence();
//        descCopy.remove(propagator);    // Add annotation to propagator
//        propagate(childAnnot.getGoTerm(), GOConstants.ANCESTRAL_EVIDENCE_CODE, e.getDbReferenceList(), e.getWiths(), descCopy, childAnnot.getQualifierSet());
//
//    }

//    private static void propagateIKR(GeneNode propagator, Annotation propagatorAnnot, ArrayList<GeneNode> descNodes) {
//        // Get Node that created IBD and delete its annotations starting at current node and all applicable descendants
//        GeneNode origPropagator = getPAINTWithNode(propagatorAnnot);
//        Annotation origAnnotation = getPropagatorsNOTAnnotation(propagatorAnnot.getGoTerm(), origPropagator, propagatorAnnot.getQualifierSet());
//
//        ArrayList<GeneNode> descCopy = (ArrayList<GeneNode>) descNodes.clone();
//        descCopy.add(propagator);           // Delete annotation from propagator of IKR
//        deletePropagatorsAnnotationFromDescendants(origPropagator, origAnnotation, descCopy, true);
//
//        // Propagate IKR to descendant nodes.  Do not propagate to nodes that provided experimental evidence for original IBD
//        descCopy.remove(propagator);        // Remove since propagator already has IKR
//        ArrayList<GeneNode> experimentalEvidenceNodes = getNodesProvidingExperimentalEvidence(origAnnotation, origPropagator);
//        if (null != experimentalEvidenceNodes) {
//            descCopy.removeAll(experimentalEvidenceNodes);
//        }
//
//        ArrayList<DBReference> withs = new ArrayList<DBReference>(1);
//        DBReference with = new DBReference();
//        with.setEvidenceType(GOConstants.PANTHER_DB);
//        with.setEvidenceValue(propagator.getNode().getStaticInfo().getPublicId());
//        withs.add(with);
//
//        ArrayList<DBReference> dbRefs = new ArrayList<DBReference>(1);
//        DBReference dbRef = new DBReference();
//        dbRef.setEvidenceType(GOConstants.PAINT_REF);
//        dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//        dbRefs.add(dbRef);
//
//        propagate(propagatorAnnot.getGoTerm(), GOConstants.ANCESTRAL_EVIDENCE_CODE, dbRefs, withs, descCopy, propagatorAnnot.getQualifierSet());
//
//        // Handle annotation to ancestor term
//        Annotation childAnnot = propagatorAnnot.getChildAnnotation();
//        Evidence ce = childAnnot.getEvidence();
//        //descCopy.remove(propagator);    // Propagator already has the annotation
//        propagate(childAnnot.getGoTerm(), GOConstants.ANCESTRAL_EVIDENCE_CODE, ce.getDbReferenceList(), ce.getWiths(), descCopy, childAnnot.getQualifierSet());
//
//    }

    public static void deletePropagatorsAnnotationFromDescendants(GeneNode origPropagator, Annotation origAnnotation, ArrayList<GeneNode> descNodes, boolean checkQualifiers) {
        //ArrayList<GeneNode> experimentalEvidenceNodes = getNodesProvidingExperimentalEvidence(origAnnotation, origPropagator);
        ArrayList<GeneNode> descCopy = (ArrayList<GeneNode>) descNodes.clone();
//        if (null != experimentalEvidenceNodes) {
//            descCopy.removeAll(experimentalEvidenceNodes);
//        }
        String goTerm = origAnnotation.getGoTerm();
        HashSet<Qualifier> propQualifierSet = origAnnotation.getQualifierSet();
        for (GeneNode gn : descCopy) {
            Node n = gn.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
            if (null == goAnnotList) {
                continue;
            }
            for (Iterator<Annotation> annotIter = goAnnotList.iterator(); annotIter.hasNext();) {
                Annotation annot = annotIter.next();
                if (true == annot.isExperimental()) {
                    continue;
                }
                GeneNode annotPropagator = getPAINTWithNode(annot);
                if (origPropagator.equals(annotPropagator) && goTerm.equals(annot.getGoTerm())) {
                    if (true == checkQualifiers) {
                        if (true == QualifierDif.allQualifiersSame(propQualifierSet, annot.getQualifierSet())) {
                            annotIter.remove();
                            break;
                        }
                    } else {
                        annotIter.remove();
                        break;
                    }

                }
            }
            if (goAnnotList.isEmpty()) {
                nvi.setGoAnnotationList(null);
            }

        }

    }

//    private static void propagate(String goTerm, String evidenceCode, ArrayList<DBReference> dbRefs, ArrayList<DBReference> withs, ArrayList<GeneNode> descNodes, HashSet<Qualifier> qualifierSet) {
//        for (GeneNode gn : descNodes) {
//            Node n = gn.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                nvi = new NodeVariableInfo();
//                n.setVariableInfo(nvi);
//            }
//            Annotation a = new Annotation();
//            nvi.addGOAnnotation(a);
//            a.setAnnotStoredInDb(false);
//            a.setGoTerm(goTerm);
//            if (null != qualifierSet) {
//                a.setQualifierSet((HashSet<Qualifier>) qualifierSet.clone());
//            }
//
//            Evidence e = new Evidence();
//            a.setEvidence(e);
//            e.setEvidenceCode(evidenceCode);
//            if (null != dbRefs) {
//                e.setDbReferenceList((ArrayList<DBReference>) dbRefs.clone());
//            }
//
//            if (null != withs) {
//                e.setWiths((ArrayList<DBReference>) withs.clone());
//            }
//        }
//    }
    
    public static Annotation getSingleWithPropagatorAnnot(Annotation a) {
        HashSet<WithEvidence> withAnnotSet = a.getAnnotationDetail().getWithEvidenceAnnotSet();
        if (null == withAnnotSet || 1 !=  withAnnotSet.size()) {
            return null;
        }
        for (WithEvidence we: withAnnotSet) {
            return (Annotation)we.getWith();
        }
        return null;
    }
    
    public static Annotation getAnnotationWithSingleWithAnnotAndEvidenceCde (ArrayList<Annotation> annotList, Annotation withAnnot, String evidenceCode) {
        for (Annotation a: annotList) {
            HashSet<WithEvidence> withEvSet = a.getAnnotationDetail().getWithEvidenceAnnotSet();
            if (null == withEvSet || 1 != withEvSet.size()) {
                continue;
            }
            for (WithEvidence we: withEvSet) {
                Annotation wa = (Annotation)we.getWith();
                if (wa != withAnnot) {
                    continue;
                }
                if (null != evidenceCode && evidenceCode.equals(wa.getSingleEvidenceCodeFromSet()) || null == evidenceCode && null == wa.getSingleEvidenceCodeFromSet()) {
                    return withAnnot;
                }
                break;
            }
        }
        return null;
    }

    public static ArrayList<DBReference> getWiths(ArrayList<DBReference> dbRefList, String skipId) {
        if (null == dbRefList || null == skipId) {
            return null;
        }
        ArrayList<DBReference> newList = new ArrayList<DBReference>();
        for (DBReference dbRef : dbRefList) {
            if (skipId.equals(dbRef.getEvidenceValue())) {
                continue;
            }
            newList.add(dbRef);
        }
        if (newList.isEmpty()) {
            newList = null;
        }
        return newList;
    }
    
    
    public static boolean isIBAFromIBDAndForIKRorIRD(Annotation ibaAnnot, GeneNode gn) {
        if (null == gn) {
            return false;
        }
        Annotation ibaPropagator = getSingleWithPropagatorAnnot(ibaAnnot);
        if (null == ibaPropagator) {
            return false;
        }
        
        // IBA can only be propagated by IBD
        if (false == Evidence.CODE_IBD.equals(ibaPropagator.getSingleEvidenceCodeFromSet())) {
            return false;
        }
        
        // Now check that the IBA term is valid. i.e. IBA can be to a less specific term if the qualifier is positive or IBA has to be to a more specific term if
        // qualifier is negative
        AnnotationDetail ad = ibaPropagator.getAnnotationDetail();
        Set<Qualifier> qualiferSet = ad.getQualifiers();
        boolean isNeg = QualifierDif.containsNegative(qualiferSet);
        PaintManager pm = PaintManager.inst();
        GOTermHelper gth = pm.goTermHelper();
        GOTerm term = gth.getTerm(ibaPropagator.getGoTerm());
        GOTerm ibaTerm = gth.getTerm(ibaAnnot.getGoTerm());
        if (null == term || null == ibaTerm) {
            return false;
        }
        if (false == isNeg) {
            // Positive qualifier - IBA term has to be less specific
            ArrayList<GOTerm> ancestors = gth.getAncestors(term);
            if (false == ancestors.contains(ibaTerm)) {
                return false;
            }
        }
        else {
            // Negative qualififier - IBA term has to be more specific
            ArrayList<GOTerm> ancestors = gth.getAncestors(ibaTerm);
            if (false == ancestors.contains(term)) {
                return false;
            }
        }
        
        Node n = gn.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return false;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return false;
        }
        for (Annotation a: annotList) {
            String code = a.getSingleEvidenceCodeFromSet();
            if ((Evidence.CODE_IKR.equals(code) && false == gn.isLeaf()) || Evidence.CODE_IRD.equals(code)) {
                HashSet<WithEvidence> withAnnotSet = a.getAnnotationDetail().getWithEvidenceAnnotSet();
                if (null == withAnnotSet) {
                    continue;
                }
                for (WithEvidence we: withAnnotSet) {
                    Annotation withAnnot = (Annotation)we.getWith();
                    if (ibaPropagator == withAnnot) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ArrayList<GeneNode> getNodesProvidingExperimentalEvidence(Annotation a, GeneNode propagator) {
        HashSet<Annotation> withs = a.getAnnotationDetail().getWithAnnotSet();
        ArrayList<GeneNode> nodeList = new ArrayList<GeneNode>();
        PaintManager pm = PaintManager.inst();        
        if (null !=  withs) {
            for (Annotation with: withs) {
                Node n = with.getAnnotationDetail().getAnnotatedNode();
                GeneNode gn = pm.getGeneByPTNId(n.getStaticInfo().getPublicId());
                if (null != gn && false == nodeList.contains(gn) && gn != propagator) {
                    nodeList.add(gn);
                }
            }
        }
        
        if (nodeList.isEmpty()) {
            return null;
        }
        return nodeList;
    }


    public static GeneNode getPAINTWithNode(Annotation a) {
        HashSet <Annotation> annotSet = a.getAnnotationDetail().getWithAnnotSet();
        PaintManager pm = PaintManager.inst();
        for (Annotation annot: annotSet) {
            if (a == annot) {
                continue;
            }
            Node n = annot.getAnnotationDetail().getAnnotatedNode();
            GeneNode gn = pm.getGeneByPTNId(n.getStaticInfo().getPublicId());
            if (null != gn && false == gn.isLeaf()) {
                return gn;
            }
            
        }
        return null;
    }

    public static GeneNode getNonLeafPaintNodeFromWiths(ArrayList<DBReference> references) {
        if (null == references) {
            return null;
        }
        PaintManager pm = PaintManager.inst();
        for (DBReference ref : references) {
            String type = ref.getEvidenceType();
            if (GOConstants.PANTHER_DB.equals(type)) {
                GeneNode gNode = pm.getGeneByPTNId(ref.getEvidenceValue());
                if (null != gNode) {
                    if (gNode.isLeaf()) {
                        continue;
                    }
                    return gNode;
                }
            }
        }
        return null;
    }

    public static Annotation getPropagatorsNOTAnnotation(String goTerm, GeneNode propagator, HashSet<Qualifier> curQualifierSet) {
        if (null == goTerm) {
            return null;
        }

        Node propNode = propagator.getNode();
        NodeVariableInfo nvi = propNode.getVariableInfo();
        if (null == nvi) {
            return null;
        }

        ArrayList<Annotation> propAnnotList = nvi.getGoAnnotationList();
        if (null == propAnnotList) {
            return null;
        }
        for (Annotation annot : propAnnotList) {
            if (goTerm.equals(annot.getGoTerm())) {
                if (true == QualifierDif.areOpposite(curQualifierSet, annot.getQualifierSet())) {
                    return annot;
                }
            }
        }
        return null;

    }

    public static Annotation getPropagatorsAnnotation(String goTerm, GeneNode propagator) {
        if (null == goTerm) {
            return null;
        }

        Node propNode = propagator.getNode();
        NodeVariableInfo nvi = propNode.getVariableInfo();
        if (null == nvi) {
            return null;
        }

        ArrayList<Annotation> propAnnotList = nvi.getGoAnnotationList();
        if (null == propAnnotList) {
            return null;
        }
        for (Annotation annot : propAnnotList) {
            if (goTerm.equals(annot.getGoTerm())) {
                return annot;
            }
        }
        return null;
    }
    
    private static boolean IBAannotationAlreadyExists(String term, Annotation propagatorAnnot, Node node) {
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return false;
        }
        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
        if (null == goAnnotList) {
            return false;
        }
        for (Annotation a: goAnnotList) {
            // Ensure we are looking at IBA with same GO Term
            if (false == Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet()) || false == a.getGoTerm().equals(term)) {
                continue;
            }
            AnnotationDetail ad = a.getAnnotationDetail();
            if (null == ad) {
                continue;
            }
            HashSet<Annotation> withs = ad.getWithAnnotSet();
            if (null == withs || 1 != withs.size()) {
                System.out.println("Found IBA with more than 1 with for annotation id " + a.getAnnotationId());
                continue;
            }
            for (Annotation with: withs) {
                if (with == propagatorAnnot) {
                    return true;
                }
            }
        }
        return false;
    }

//    public static void addIBAAnnotation(String termAcc, List<GeneNode> propagateList, Annotation propagatorAnnot, HashSet<Qualifier> qualifierSet) {
//        if (null == propagateList) {
//            return;
//        }
//        
//        // Only add qualifiers that are valid for term
//        HashSet<Qualifier> validSet = null;
//        if (null != qualifierSet) {
//            HashSet<Qualifier> qsetCopy = (HashSet<Qualifier>) qualifierSet.clone();
//            validSet = new HashSet<Qualifier>();
//            PaintManager pm = PaintManager.inst();
//            GOTermHelper gth = pm.goTermHelper();
//            GOTerm goTerm = gth.getTerm(termAcc);
//            for (Qualifier q : qsetCopy) {
//                if (true == gth.isQualifierValidForTerm(goTerm, q)) {
//                    validSet.add(q);
//                }
//            }        
//        }
//        
//        for (GeneNode gn : propagateList) {
//
//            Node n = gn.getNode();
//            if (true == IBAannotationAlreadyExists(termAcc, propagatorAnnot, n)) {
//                continue;
//            }
////            if (true == "GO:0016791".equals(termAcc) && true == "PTN001597037".equals(n.getStaticInfo().getPublicId())) {
////                System.out.println("Found adding of IBA");
////            }
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (nvi == null) {
//                nvi = new NodeVariableInfo();
//                n.setVariableInfo(nvi);
//            }
//            Annotation descAnnot = new Annotation();
//            nvi.addGOAnnotation(descAnnot);
//            descAnnot.setAnnotStoredInDb(false);
//            if (null != validSet) {
//                descAnnot.setQualifierSet((HashSet<Qualifier>) validSet.clone());
//            }
//            descAnnot.setGoTerm(termAcc);
//            WithEvidence we = new WithEvidence();
//            we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
//            we.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
//            we.setWith(propagatorAnnot);
//            descAnnot.addWithEvidence(we);
////            Evidence e = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
////            descAnnot.setEvidence(e);
////            e.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
////            DBReference dbRef = new DBReference();
////            dbRef.setEvidenceType(GOConstants.PAINT_REF);
////            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
////            e.addDbRef(dbRef);
//            AnnotationDetail ad = descAnnot.getAnnotationDetail();
//            ad.setAnnotatedNode(gn.getNode());
////            ad.addWith(propagatorAnnot);
//            if (null != validSet) {
//                for (Qualifier q : validSet) {
//                    ad.addToInheritedQualifierLookup(q, propagatorAnnot);
//                }
//            }
//        }
//    }

//    public static void addIBAAnnotationOld(GOTerm term, List<GeneNode> propagateList, List<GeneNode> nodesProvidingEvidence, HashSet<Qualifier> qualifierSet) {
//
//        ArrayList<DBReference> withs = new ArrayList<DBReference>();
//        if (null != nodesProvidingEvidence) {
//            for (GeneNode gNode : nodesProvidingEvidence) {
//                Node curNode = gNode.getNode();
//                DBReference curRef = new DBReference();
//                curRef.setEvidenceType(GOConstants.PANTHER_DB);
//                curRef.setEvidenceValue(curNode.getStaticInfo().getPublicId());
//                withs.add(curRef);
//            }
//        }
//
//        for (GeneNode gNode : propagateList) {
//            Node descNode = gNode.getNode();
//            NodeVariableInfo descNvi = descNode.getVariableInfo();
//            if (null == descNvi) {
//                descNvi = new NodeVariableInfo();
//                descNode.setVariableInfo(descNvi);
//            }
//            Annotation descAnnot = new Annotation();
//            descNvi.addGOAnnotation(descAnnot);
//
//            if (null != qualifierSet) {
//                descAnnot.setQualifierSet((HashSet<Qualifier>) qualifierSet.clone());
//            }
//            descAnnot.setGoTerm(term.getAcc());
//            edu.usc.ksom.pm.panther.paintCommon.Evidence descEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            descAnnot.setEvidence(descEvidence);
//            descEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//            DBReference descDbRef = new DBReference();
//            descDbRef.setEvidenceType(GOConstants.PAINT_REF);
//            descDbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            descEvidence.addDbRef(descDbRef);
//            descEvidence.setWiths((ArrayList<DBReference>) withs.clone());
//        }
//    }

    public static HashSet<GeneNode> getDescWithExpEvdnce(GeneNode gNode) {
        HashSet<GeneNode> rtnList = new HashSet<GeneNode>();
        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
        GeneNodeUtil.allDescendents(gNode, descList);
        List<GeneNode> leafDesc = GeneNodeUtil.getAllLeaves(descList);
        for (GeneNode gn : leafDesc) {
            Node n = gn.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
            if (null == goAnnotList) {
                continue;
            }
            for (Annotation annot : goAnnotList) {
                if (false == annot.isExperimental()) {
                    continue;
                }
                rtnList.add(gn);
            }
        }
        if (rtnList.isEmpty()) {
            rtnList = null;
        }
        return rtnList;
    }

    public boolean isQualifierForTermNegative(ArrayList<Annotation> annotList, String term) {
        if (null == annotList || null == term) {
            return false;
        }
        PaintManager pm = PaintManager.inst();
        GOTermHelper gth = pm.goTermHelper();
        GOTerm compTerm = gth.getTerm(term);
        ArrayList<GOTerm> ancestors = gth.getAncestors(compTerm);
        boolean foundNegative = false;
        for (Annotation annot : annotList) {
            String cTerm = annot.getGoTerm();
            if (true == term.equals(cTerm)) {
                return QualifierDif.containsNegative(annot.getQualifierSet());
            }
            GOTerm curTerm = gth.getTerm(cTerm);
            if (ancestors.contains(curTerm)) {
                if (true == QualifierDif.containsNegative(annot.getQualifierSet())) {
                    foundNegative = true;
                }
            }
        }
        return foundNegative;
    }
    
    private static void removeNonExperimentalNonPaintAnnotations(GeneNode gNode, PaintManager pm, HashSet<Annotation> removedAnnotSet) {
       if (null == gNode) {
           return;
       }
       Node n = gNode.getNode();
       if (null == n) {
           System.out.println("Did not find node information");
       }
       NodeVariableInfo nvi = gNode.getNode().getVariableInfo();
       if (null != nvi) {
           
           ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
           if (null != annotList) {
                ArrayList<Annotation> removeList = new ArrayList<Annotation>();
                for (Annotation a: annotList) {
                    
                    if (true == a.isExperimental() || true == a.isPaint()) {
                        continue;
                    }
                    //System.out.println("Removed annotation " + a.getGoTerm() + " from node " + gNode.getNode().getStaticInfo().getPublicId());
                    removeList.add(a);
                }
                removedAnnotSet.addAll(removeList);
                for (Annotation a: removeList) {
                    pm.addToRemovedAnnotationLookup(gNode, a);
                }
                annotList.removeAll(removeList);
                if (annotList.isEmpty()) {
                    nvi.setGoAnnotationList(null);
                }
           }
       }
       
       List<GeneNode> children = gNode.getChildren();
       if (null == children) {
           return;
       }
       for (GeneNode child: children) {
           removeNonExperimentalNonPaintAnnotations(child, pm, removedAnnotSet);
       }
    }

    /**
     * Remove non-Experimental and non-paint annotations since they confuse curators
     * Annotations are not propagated when tree information is sent from server.
     *
     * @param root
     */
//    public static void propagateAndFixAnnotationsForBookOpen(GeneNode root) {
//        HashSet<Annotation> removedAnnots = new HashSet<Annotation>();
//        // This will just remove the annotations from the nodes, does not consider
//        // annotations that may have these removed annotations as withs (i.e. dependencies)        
//        removeNonExperimentalNonPaintAnnotations(root, PaintManager.inst(), removedAnnots);
//        
//        // Other dependent annotations may get removed because of removing the above annotations
//        HashSet<Annotation> newRemovedAnnotSet = new HashSet<Annotation>();
//        do {
//            newRemovedAnnotSet.clear();
//            handleRemovedWithAnnots(root, removedAnnots, newRemovedAnnotSet);
//            removedAnnots = (HashSet<Annotation>)newRemovedAnnotSet.clone();
//        }
//        while(0 != newRemovedAnnotSet.size());
//        
//        
//        if (root.isPruned()) {
//            return;
//        }
//        ArrayList<GeneNode> descendents = new ArrayList<GeneNode>();
//        // First go through all the IBA's that were created when IKR or IRD was created and make a list.
//        GeneNodeUtil.inst().allNonPrunedDescendents(root, descendents);
//        descendents.add(0, root);
//        ArrayList<Annotation> IBAannots = existingIBAannots(descendents);
//        propagateAnnotationsForBookOpen(root, IBAannots);
//    }
    
    
//    private static void handleRemovedWithAnnots(GeneNode gNode, HashSet<Annotation> curRemovedAnnotSet, HashSet<Annotation> newRemovedAnnotSet) {
//        NodeVariableInfo nvi = gNode.getNode().getVariableInfo();
//        if (null != nvi) {
//            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//            if (null != annotList) {
//                for (Annotation a: annotList) {
//                   for (Annotation removedAnnot: curRemovedAnnotSet) {
//                       Annotation ra = a.removeWith(removedAnnot);
//                       if (null != ra) {
//                           HashSet<Annotation> remainingWiths = a.getAnnotationDetail().getWithAnnotSet();
//                           if (null == remainingWiths || true == remainingWiths.isEmpty()) {
//                               newRemovedAnnotSet.add(a);
//                           }
//                           else if ((Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet()) && false == gNode.isLeaf()) || Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet())) {
//                               newRemovedAnnotSet.add(a);
//                           }
//                       }
//                   }
//                }
//            }
//        }
//        List<GeneNode> children = gNode.getChildren();
//        if (null !=  children) {
//            for (GeneNode child: children) {
//                handleRemovedWithAnnots(child, curRemovedAnnotSet, newRemovedAnnotSet);
//            }
//        }
//    }
    
//    private static ArrayList<GeneNode> getEvidenceNodeListForAnnotation(Annotation a) {
//        HashSet<GeneNode> gSet = getEvidenceNodesForAnnotation(a);
//        if (null == gSet) {
//            return null;
//        }
//        return new ArrayList<GeneNode>(gSet);
//        
////        AnnotationDetail ad = a.getAnnotationDetail();
////        HashSet<Annotation> withAnnotSet = ad.getWithAnnotSet();
////        if (null == withAnnotSet) {
////            return null;
////        }
////        ArrayList<GeneNode> withNodes = new ArrayList<GeneNode>();
////        PaintManager pm = PaintManager.inst();
////        for (Annotation annot : withAnnotSet) {
////            Node n = annot.getAnnotationDetail().getAnnotatedNode();
////            GeneNode gn = pm.getGeneByPTNId(n.getStaticInfo().getPublicId());
////            if (false == withNodes.contains(gn)) {
////                withNodes.add(gn);
////            }
////        }
////        return withNodes;
//    }    

    private static HashSet<GeneNode> getEvidenceNodesForAnnotation(Annotation a) {
        AnnotationDetail ad = a.getAnnotationDetail();
        HashSet<Annotation> withAnnotSet = ad.getWithAnnotSet();
        if (null == withAnnotSet) {
            return null;
        }
        HashSet<GeneNode> withNodes = new HashSet<GeneNode>();
        PaintManager pm = PaintManager.inst();
        for (Annotation annot : withAnnotSet) {
            Node n = annot.getAnnotationDetail().getAnnotatedNode();
            if (null == n) {
                System.out.println("Annotation id " + a.getAnnotationId() + " does not have annotated node");
                continue;
            }
            withNodes.add(pm.getGeneByPTNId(n.getStaticInfo().getPublicId()));
        }
        return withNodes;
    }

    
//    private static void addIBAToDescendants(Annotation propagator, ArrayList<GeneNode> nodesToSkip, String term, boolean checkTaxonConstraint) {
//        Node annotator = propagator.getAnnotationDetail().getAnnotatedNode();
//        PaintManager pm = PaintManager.inst();
//        GeneNode annotGn = pm.getGeneByPTNId(annotator.getStaticInfo().getPublicId());
//        List<GeneNode> children = annotGn.getChildren();
//        if (null == children) {
//            return;
//        }
//        
//        boolean checkTaxon = true;
//        TaxonomyHelper th = pm.getTaxonHelper();
//        if (null == th) {
//            checkTaxon = false;
//        }
//        for (GeneNode child: children) {
//            if (true == child.isPruned()) {
//                continue;
//            }
//            propagateIBAToDescendants(propagator, child, nodesToSkip, term, (checkTaxon && checkTaxonConstraint), th);
//        }
//    }
//    
    private static void propagateIBAToDescendants(Annotation propagator, GeneNode gn, List<GeneNode> nodesToSkip, String term, boolean checkTaxonConstraint, TaxonomyHelper th) {
        // DO NOT DO TAXON CONSTRAINT CHECK HERE.  Only for IBD annotations
//        if ((true == checkTaxonConstraint && false == GeneNodeUtil.isTermValidForNode(gn, term)) || true == nodesToSkip.contains(gn)) {
//            return;
//        }
        if (null != nodesToSkip && true == nodesToSkip.contains(gn) || true == IBAannotationAlreadyExists(term, propagator, gn.getNode())) {
            return;
        }
        
        addIBAToNode(propagator, gn, term);
        List<GeneNode> children = gn.getChildren();
        if (null == children) {
            return;
        }
        for (GeneNode child: children) {
            if (true == child.isPruned()) {
                continue;
            }
           propagateIBAToDescendants(propagator, child, nodesToSkip, term, checkTaxonConstraint, th); 
        }
    }
    
    private static void addIBAToNode(Annotation propagator, GeneNode gn, String term) {
        Node n = gn.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            nvi = new NodeVariableInfo();
            n.setVariableInfo(nvi);
        }
        Annotation newAnnot = new Annotation();
        nvi.addGOAnnotation(newAnnot);
        newAnnot.setGoTerm(term);

        AnnotationDetail newDetail = newAnnot.getAnnotationDetail();
        newDetail.setAnnotatedNode(n);
        WithEvidence we = new WithEvidence();
        we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);        
        we.setEvidenceCode(Evidence.CODE_IBA);
        we.setWith(propagator);
        newAnnot.addWithEvidence(we);
//        Evidence newEvidence = new Evidence();
//        newAnnot.setEvidence(newEvidence);
//        newEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//        DBReference dbRef = new DBReference();
//        dbRef.setEvidenceType(GOConstants.PAINT_REF);
//        dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//        newEvidence.addDbRef(dbRef);
//
//        newDetail.addWith(propagator);
        // Get inherited qualifiers.  Set with of qualifiers to propagator of annotation
        LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = propagator.getAnnotationDetail().getQualifierLookup();
        for (Qualifier q : qualifierLookup.keySet()) {
            Qualifier newQ = new Qualifier();
            newQ.setText(q.getText());
            newDetail.addToInheritedQualifierLookup(newQ, propagator);
            newAnnot.addQualifier(newQ);
        }
    }

    /*
    // NOTE!!!!!!!!!!!!!!!!!!! Similar to AnnotationHelper package on server
    */
//    private static void propagateIBAtoNodes(Annotation propagator, ArrayList<GeneNode> nodeList, String term) {
//        for (GeneNode gn : nodeList) {
//            Node n = gn.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                nvi = new NodeVariableInfo();
//                n.setVariableInfo(nvi);
//            }
//            Annotation newAnnot = new Annotation();
//            nvi.addGOAnnotation(newAnnot);
//            newAnnot.setGoTerm(term);
//
//            AnnotationDetail newDetail = newAnnot.getAnnotationDetail();
//            newDetail.setAnnotatedNode(n);
//            WithEvidence we = new WithEvidence();
//            we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);            
//            we.setEvidenceCode(Evidence.CODE_IBA);
//            we.setWith(propagator);
//            newAnnot.addWithEvidence(we);
//            
////            newDetail.addWith(propagator);
////            newDetail.setAnnotatedNode(n);
////            Evidence newEvidence = new Evidence();
////            newAnnot.setEvidence(newEvidence);
////            newEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
////            DBReference dbRef = new DBReference();
////            dbRef.setEvidenceType(GOConstants.PAINT_REF);
////            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
////            newEvidence.addDbRef(dbRef);
//
//
//            // Get inherited qualifiers.  Set with of qualifiers to propagator of annotation
//            LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = propagator.getAnnotationDetail().getQualifierLookup();
//            for (Qualifier q: qualifierLookup.keySet()) {
//                Qualifier newQ = new Qualifier();
//                newQ.setText(q.getText());
//                newDetail.addToInheritedQualifierLookup(newQ, propagator);
//                newAnnot.addQualifier(newQ);
//            }
//            
//            
////            LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = a.getAnnotationDetail().getQualifierLookup();
////            Collection<HashSet<Annotation>> withList = qualifierLookup.values();
////            for (HashSet<Annotation> cur : withList) {
////                cur.clear();
////                cur.add(a);
////            }
////            newDetail.setInheritedQualifierLookup(qualifierLookup);
//
//        }
//    }
    
//    private static Annotation getIBDpropagator(Annotation a) {
//        HashSet<Annotation> withs = a.getAnnotationDetail().getWithAnnotSet();
//        if (null == withs) {
//            return null;
//        }
//        for (Annotation withAnnot: withs) {
//            if (a == withAnnot) {
//                continue;
//            }
//            if (Evidence.CODE_IBD.equals(withAnnot.getSingleEvidenceCodeFromSet())) {
//                return withAnnot;
//            }
//            return getIBDpropagator(withAnnot);
//        }
//        return null;
//    }
    
    private static Annotation getPropagator(Annotation a) {
        HashSet<WithEvidence> withSet = a.getAnnotationDetail().getWithEvidenceAnnotSet();
        if (null == withSet) {
            return null;
        }
        for (WithEvidence we: withSet) {
            Annotation with = (Annotation)we.getWith();
            if (a == we.getWith()) {
                continue;
            }
            return with;
        }
        return null;        
    }

//    private static void propagateAnnotationsForBookOpen(GeneNode gNode, ArrayList<Annotation> initialIBAannots) {
//        Node n = gNode.getNode();
////        System.out.println("Processing node " + n.getStaticInfo().getNodeAcc() + " public id " + n.getStaticInfo().getPublicId());
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null != nvi && null != nvi.getGoAnnotationList()) {
//            ArrayList<Annotation> annotList = (ArrayList<Annotation>)nvi.getGoAnnotationList().clone();     // Clone since annotation list gets modified within loop
//            if (null != annotList) {
////                System.out.println("Node has " + annotList.size() + " annotations");
//                for (int i = 0; i < annotList.size(); i++) {
//                    Annotation a = annotList.get(i);
////                    System.out.println("Processing " + i + " annotation");
////                    Evidence e = a.getEvidence();
////                    if (true == Evidence.CODE_IBA.equals(e.getEvidenceCode()) && false == gNode.isLeaf()) {
////                        System.out.println("Here");
////                    }
//                    if (true == Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet()) && true == initialIBAannots.contains(a)) {
//                        Annotation ibdProp = getIBDpropagator(a);
//                        Annotation propagator = getPropagator(a);
//                        if (null == ibdProp || null == propagator) {
//                            continue;
//                        }
//                        ArrayList<GeneNode> descendents = new ArrayList<GeneNode>();
//                        GeneNodeUtil.allNonPrunedDescendents(gNode, descendents);
//                        HashSet<GeneNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(ibdProp);
//                        if (null != nodesProvidingEvidence) {
//                            descendents.removeAll(nodesProvidingEvidence);
//                        }
//                        propagateIBAtoNodes(propagator, descendents, a.getGoTerm());
//                    }
//                    if (true == Evidence.CODE_IBD.equals(a.getSingleEvidenceCodeFromSet())) {
//                        ArrayList<GeneNode> descendents = new ArrayList<GeneNode>();
//                        GeneNodeUtil.allNonPrunedDescendents(gNode, descendents);
//                        HashSet<GeneNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(a);
//                        if (null != nodesProvidingEvidence) {
//                            descendents.removeAll(nodesProvidingEvidence);
//                        }
//                        propagateIBAtoNodes(a, descendents, a.getGoTerm());
//                        continue;
//                    }
//                    if (true == Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet()) || (true == Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet()) && false == gNode.isLeaf())) {
//                        // remove IBA's propagated from IBD
//                         Annotation ibdProp = getIBDpropagator(a);
//                         Annotation propagator = getPropagator(a);
//                        if (null == ibdProp) {
//                            System.out.println("Did not find Annotation that was cause of IRD or IRK for " + n.getStaticInfo().getPublicId());
//                            continue;
//                        }
//                        ArrayList<GeneNode> descendents = new ArrayList<GeneNode>();
//                        GeneNodeUtil.allNonPrunedDescendents(gNode, descendents);
//                        HashSet<GeneNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(ibdProp);
//                        if (null != nodesProvidingEvidence) {
//                            descendents.removeAll(nodesProvidingEvidence);
//                        }
//                        descendents.add(gNode);     // Add myself since, IBA has to be removed
//                        for (GeneNode descNode : descendents) {
//                            Node dn = descNode.getNode();
//                            NodeVariableInfo dvni = dn.getVariableInfo();
//                            if (null == dvni) {
//                                System.out.println("Did not find variable IBA info for node " + dn.getStaticInfo().getPublicId());
//                                continue;
//                            }
//                            ArrayList<Annotation> dAnnotList = dvni.getGoAnnotationList();
//                            if (null == dAnnotList) {
//                                System.out.println("Did not find IBA  annotation for node " + dn.getStaticInfo().getPublicId());
//                                continue;
//                            }
//                            for (Iterator<Annotation> annotIter = dAnnotList.iterator(); annotIter.hasNext();) {
//                                Annotation descAnnot = annotIter.next();
//                                if (false == Evidence.CODE_IBA.equals(descAnnot.getSingleEvidenceCodeFromSet())) {
//                                    continue;
//                                }
//                                if (false == a.getGoTerm().equals(descAnnot.getGoTerm())) {
//                                    continue;
//                                }
//                                Annotation propagatorAnnot = null;
//                                if (null != descAnnot.getAnnotationDetail().getWithAnnotSet()) {
//                                    for (Annotation prop : descAnnot.getAnnotationDetail().getWithAnnotSet()) {
//                                        propagatorAnnot = prop;
//                                        if (prop == descAnnot) {
//                                            propagatorAnnot = null;
//                                            continue;
//                                        }
//                                        break;
//                                    }
//                                }
//                                if (true == propagator.equals(propagatorAnnot)) {
//                                    annotIter.remove();
//                                }
//                            }
//                            if (dAnnotList.isEmpty()) {
//                                dvni.setGoAnnotationList(null);
//                            }
//                        }
//
//                        // For IKR, need to propagate to descendents
//                        descendents.remove(gNode);      // Remove myself since node being processed does not get IBA
//                        if (true == GOConstants.KEY_RESIDUES_EC.equals(a.getSingleEvidenceCodeFromSet())) {
//                            propagateIBAtoNodes(a, descendents, a.getGoTerm());
//                        }
//
////                        // Check for annotation to ancestor term and propagate
////                        Annotation childAnnot = a.getChildAnnotation();
////                        if (null != childAnnot && initialIBAannots.contains(childAnnot)) {
//////                            Annotation propagatorAnnot = null;
//////                            for (Annotation prop : childAnnot.getAnnotationDetail().getWithAnnotSet()) {
//////                                propagatorAnnot = prop;
//////                                break;
//////                            }
//////                            if (true == ibdProp.equals(propagatorAnnot)) {
////                                propagateIBAforIKRandIRD(childAnnot, descendents);
//////                            }
////                        }
//
//                    }
//                }
//            }
//        }
//        else {
////            System.out.println("No annotations");
//        }
//
//        // Iterate over children
//        List<GeneNode> children = gNode.getChildren();
//        if (null == children) {
//            return;
//        }
//        for (GeneNode child : children) {
//            if (child.isPruned()) {
//                continue;
//            }
//            propagateAnnotationsForBookOpen(child, initialIBAannots);
//        }
//    }

//    private static ArrayList<Annotation> existingIBAannots(ArrayList<GeneNode> descendents) {
//        ArrayList<Annotation> ibaAnnots = new ArrayList<Annotation>();
//        for (GeneNode gn : descendents) {
//            Node n = gn.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                continue;
//            }
//            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//            if (null == annotList) {
//                continue;
//            }
//            ArrayList<Annotation> removeAnnot = new ArrayList<Annotation>();    // If we find an IBA without 'matching' IKR or 'IRD' remove since this is invalid data
//            for (Annotation a : annotList) {
//                if (false == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(a.getSingleEvidenceCodeFromSet())) {
//                    continue;
//                }
////                if (true == "38109929".equals(a.getAnnotationId())) {
////                    System.out.println("Here");
////                }
//                // Have IBA without propagator
//                Annotation propagatorAnnot = null;
//                if (null == a.getAnnotationDetail().getWithAnnotSet()) {
//                    System.out.println("Annotation does not have withs for " + a.getAnnotationId());
//                    removeAnnot.add(a);
//                    continue;
//                }
//                for (Annotation prop : a.getAnnotationDetail().getWithAnnotSet()) {
//                    propagatorAnnot = prop;
//                    break;
//                }
//                if (null == propagatorAnnot) {
//                    removeAnnot.add(a);
//                    continue;
//                }
//                boolean found = false;
//                for (Annotation compAnnot : annotList) {
//                    if (a.equals(compAnnot)) {
//                        continue;
//                    }
//                    if (true == GOConstants.DIVERGENT_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())  ||
//                        true == GOConstants.KEY_RESIDUES_EC.equals(compAnnot.getSingleEvidenceCodeFromSet()) ||
//                        true == GOConstants.DESCENDANT_SEQUENCES_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())) {
//                        Annotation compPropagator = null;
//                        for (Annotation compProp : compAnnot.getAnnotationDetail().getWithAnnotSet()) {
//                            if (compAnnot == compProp) {
//                                continue;
//                            }
//                            compPropagator = compProp;
//                            break;
//                        }
//                        if (true == propagatorAnnot.equals(compPropagator)) {
//                            ibaAnnots.add(a);
//                            found = true;
//                            break;
//                        }
//                    }
//                }
//                if (false == found) {
//                    removeAnnot.add(a);
//                }
//            }
//            if (false == removeAnnot.isEmpty()) {
//                //JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Removed IBA annotation(s) from" + n.getStaticInfo().getPublicId(), "Warning", JOptionPane.WARNING_MESSAGE);
//                annotList.removeAll(removeAnnot);
//                if (annotList.isEmpty()) {
//                    nvi.setGoAnnotationList(null);
//                }
//            }
//        }
//        return ibaAnnots;
//    }

    public static HashSet<GeneNode> getWithsNodes(Annotation a) {
        AnnotationDetail ad = a.getAnnotationDetail();
        HashSet<WithEvidence> withSet = ad.getWithEvidenceAnnotSet();

        PaintManager pm = PaintManager.inst();
        HashSet<GeneNode> rtnSet = new HashSet<GeneNode>(withSet.size());
        if (null != withSet) {
            for (WithEvidence with : withSet) {
                Annotation annot = (Annotation)with.getWith();
                Node n = annot.getAnnotationDetail().getAnnotatedNode();
                rtnSet.add(pm.getGeneByPTNId(n.getStaticInfo().getPublicId()));
            }
        }
        withSet = ad.getWithEvidenceNodeSet();
        if (null != withSet) {
            for (WithEvidence with: withSet) {
                Node n = (Node)with.getWith();
                rtnSet.add(pm.getGeneByPTNId(n.getStaticInfo().getPublicId()));                
            }
        }
        return rtnSet;
    }

    public static HashSet<GeneNode> getWithsFromLastIBD(Annotation a, GeneNode gNode) {
        Annotation ibdAnnot = getLastIBD(a, gNode);
        if (null == ibdAnnot) {
            System.out.println("Did not find last ibd");
            return null;
        }
        AnnotationDetail ad = ibdAnnot.getAnnotationDetail();
        HashSet<Annotation> withSet = ad.getWithAnnotSet();
        if (null == withSet) {
            return null;
        }
        PaintManager pm = PaintManager.inst();
        HashSet<GeneNode> rtnSet = new HashSet<GeneNode>(withSet.size());
        for (Annotation with : withSet) {
            Node n = with.getAnnotationDetail().getAnnotatedNode();
            rtnSet.add(pm.getGeneByPTNId(n.getStaticInfo().getPublicId()));
        }
        return rtnSet;
    }

    public static Annotation getLastIBD(Annotation a, GeneNode gNode) {
        if (null == a) {
            return null;
        }
        if (false == Evidence.CODE_IBD.equals(a.getSingleEvidenceCodeFromSet())) {
            Annotation temp = getWithAnnotation(a, gNode);
            if (null == temp) {
                return null;
            }
            return getLastIBD(temp, PaintManager.inst().getGeneByPTNId(temp.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId()));
        }
        return a;
    }

    public static Annotation getWithAnnotation(Annotation a, GeneNode gNode) {
        AnnotationDetail ad = a.getAnnotationDetail();
        HashSet<WithEvidence> withSet = ad.getWithEvidenceAnnotSet();
        if (null == withSet) {
            return null;
        }
        for (WithEvidence with : withSet) {
            Annotation annot = (Annotation)with.getWith();
            if (true == annot.getAnnotationDetail().getAnnotatedNode().equals(gNode.getNode())) {
                continue;
            }
            return annot;
        }
        return null;
    }

    public static Annotation getWithAnnotationForIBA(Annotation a, GeneNode gNode) {
        return getWithAnnotation(a, gNode);
    }

    public static Annotation getWithIRD_IKR_Annotation(Annotation a, GeneNode node) {
        AnnotationDetail ad = a.getAnnotationDetail();
        HashSet<Annotation> withSet = ad.getWithAnnotSet();
        if (null == withSet) {
            return null;
        }
        for (Annotation with : withSet) {
            Node n = with.getAnnotationDetail().getAnnotatedNode();
            if (n.equals(node.getNode())) {
                continue;
            }
            return with;
        }
        return null;
    }
    
    public static HashSet<Annotation> deleteAnnotation(GeneNode gNode, Annotation a) {
        HashSet<Annotation> deleteAnnots = new HashSet<Annotation>();
        deleteAnnots.add(a);
        return deleteAnnotation(gNode, a, deleteAnnots);
    }
    
    public static HashSet<Annotation> deleteAnnotation(GeneNode gNode, Annotation a, HashSet<Annotation> annotsToBeDeleted) {
        HashSet<Annotation> deletedAnnots = new HashSet<Annotation>();
        Node n = gNode.getNode();

//        if (true == "PTN002467107".equals(n.getStaticInfo().getPublicId())) {
//            System.out.println("Here");
//        }
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return deletedAnnots;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return deletedAnnots;
        }
        if (false == annotList.contains(a)) {
            return deletedAnnots;
        }
        
 
        deleteAnnotation(gNode, a, /*annotsToBeDeleted, */ deletedAnnots, annotsToBeDeleted);
        for (Annotation annot : deletedAnnots) {
            Node node = annot.getAnnotationDetail().getAnnotatedNode();
            if (null == node) {
                continue;
            }
            NodeVariableInfo nodeNvi = node.getVariableInfo();
            if (null == nodeNvi) {
                continue;
            }
            ArrayList<Annotation> aList = nodeNvi.getGoAnnotationList();
            if (null == aList) {
                continue;
            }
            aList.remove(annot);
            System.out.println("Deleted annotation from node " + n.getStaticInfo().getPublicId() + " annotation to term " + annot.getGoTerm());
            if (aList.isEmpty()) {
                nodeNvi.setGoAnnotationList(null);
            }
        }
        return deletedAnnots;                
    }
    
     private static void deleteAnnotation(GeneNode gNode, Annotation a, /*HashSet<Annotation> annotsToBeDeleted, */ HashSet<Annotation> deletedAnnots, HashSet<Annotation> annotsToBeDeleted) {
        if (false == a.isCreatedByPaint()) {
            return;
        }
        annotsToBeDeleted.add(a);
        Node node = gNode.getNode();
//        if (true == "PTN002467107".equals(node.getStaticInfo().getPublicId())) {
//            System.out.println("Here");
//        }
        String code = a.getSingleEvidenceCodeFromSet();

        System.out.println("Deleting annotation for " + gNode.getNode().getStaticInfo().getPublicId() + " code " + a.getSingleEvidenceCodeFromSet());
        // First remove dependent annotations
        ArrayList<GeneNode> descendents = new ArrayList<GeneNode>();
        GeneNodeUtil.allNonPrunedDescendents(gNode, descendents);
        for (GeneNode desc : descendents) {
            Node n = desc.getNode();
//            if (true == "PTN001597050".equals(desc.getNode().getStaticInfo().getPublicId()) || true == " PTN002224260".equals(desc.getNode().getStaticInfo().getPublicId())) {
//                System.out.println("desc Here");
//            }

            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            
            Vector deleteInfo = new Vector();

            for (Iterator<Annotation> annotIter = annotList.iterator(); annotIter.hasNext(); ) {
                Annotation annot = annotIter.next();
                if (false == annot.withExists(a)) {
//                    if (Evidence.CODE_IBA.equals(annot.getEvidence().getEvidenceCode())) {
//                        System.out.println("Passing by IBA annotation");
//                    }
                    continue;
                }
                
                if (false == annot.isRequired(a)) {
                    System.out.println("Annot to " + annot.getSingleEvidenceCodeFromSet() + " does not require annot with code " + a.getSingleEvidenceCodeFromSet());
                    annot.removeWith(a);
                    HashSet<String> codes = annot.getAnnotationDetail().getEvidenceCodes();
                    if (null == codes || codes.isEmpty()) {
                        deleteAnnotation(desc, annot, annotsToBeDeleted);
                    }
                    continue;
                }

                System.out.println("Deleting descendent annotation for " + n.getStaticInfo().getPublicId() + " annot code " + annot.getSingleEvidenceCodeFromSet());
                
                // Before removing the with (aka propagator), remove annotations that are dependent on this annotation itself                
                Vector deleteLater = new Vector(2);
                deleteInfo.add(deleteLater);
                deleteLater.add(desc);
                deleteLater.add(annot);
                
            }
            for (int i = 0; i < deleteInfo.size(); i++) {
                Vector deleteLater = (Vector)deleteInfo.get(i);
                deleteAnnotation((GeneNode)deleteLater.get(0), (Annotation)deleteLater.get(1), annotsToBeDeleted);
            }
           
            
        }
        
        if (Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code)) {
            Annotation propagator = getPropagator(a);
            Annotation iba = getAssociatedIBAForIKRorIRD(gNode, a);     // Case where we annotated to ancestor term
            if (null != iba) {
                String goTerm = iba.getGoTerm();
                deleteAnnotation(gNode, iba, annotsToBeDeleted);
                for (GeneNode gn: descendents) {
                    Node n = gn.getNode();
                    NodeVariableInfo nvi = n.getVariableInfo();
                    if (null == nvi) {
                        continue;
                    }
                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
                    if (null == annotList) {
                        continue;
                    }
                    
                    Vector deleteInfo = new Vector();
                    for (Annotation annot: annotList) {
                        if (false == goTerm.equals(annot.getGoTerm())) {
                            continue;
                        }
                        // Have to have same propagator
                        Annotation curProp = getPropagator(annot);
                        if (propagator != curProp) {
                            continue;
                        }
                        
                        // For IBA qualifiers have to be the same
                        QualifierDif qd = new QualifierDif(iba.getQualifierSet(), annot.getQualifierSet());
                        String curCode = annot.getSingleEvidenceCodeFromSet();
                        if (QualifierDif.QUALIFIERS_SAME == qd.getDifference() && Evidence.CODE_IBA.equals(curCode)) {
                            Vector deleteLater = new Vector(2);
                            deleteInfo.add(deleteLater);
                            deleteLater.add(gn);
                            deleteLater.add(annot);
                        }
                        
                        // For IKR or IRD qualifiers are different
                        if (QualifierDif.areOpposite(iba.getQualifierSet(), annot.getQualifierSet()) &&  (Evidence.CODE_IKR.equals(curCode)  || Evidence.CODE_IRD.equals(curCode) || Evidence.CODE_TCV.equals(curCode))) {
                            Vector deleteLater = new Vector(2);
                            deleteInfo.add(deleteLater);
                            deleteLater.add(gn);
                            deleteLater.add(annot);                            
                        }
                    }
                    for (int i = 0; i < deleteInfo.size(); i++) {
                        Vector deleteLater = (Vector) deleteInfo.get(i);
                        deleteAnnotation((GeneNode) deleteLater.get(0), (Annotation) deleteLater.get(1), annotsToBeDeleted);
                    }
                }
            }
            
            // Remove the annotation
            deletedAnnots.add(a);
            
            // If propagator is being removed, no need to add propagated terms           
            if (annotsToBeDeleted.contains(propagator) || deletedAnnots.contains(propagator)) {
                return;
            }
            // Get nodes that will need propagation
            // Get the IBD that propagated this annotation and get the list of nodes that provided experimental evidence
            Annotation ibd = getLastIBD(a, gNode);
            Node ibdProp = ibd.getAnnotationDetail().getAnnotatedNode();
            PaintManager pm = PaintManager.inst();
            ArrayList<GeneNode> evNodes = getNodesProvidingExperimentalEvidence(ibd, pm.getGeneByPTNId(ibdProp.getStaticInfo().getPublicId()));

            System.out.println("Creating IBA's for " + propagator.getGoTerm());
            propagateIBAToDescendants (propagator, gNode, evNodes, propagator.getGoTerm(), false, pm.getTaxonHelper());
            return;
        }
        deletedAnnots.add(a);
        
     }   

//    public static HashSet<Annotation> deleteAnnotationOld(GeneNode gNode, Annotation a, boolean createNewAnnots) {
//        //HashSet<Annotation> annotsToBeDeleted = new HashSet<Annotation>();
//        //annotsToBeDeleted.add(a);
//        // Check that node has associated annotation
//        HashSet<Annotation> deletedAnnots = new HashSet<Annotation>();
//        Node n = gNode.getNode();
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null == nvi) {
//            return deletedAnnots;
//        }
//        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//        if (null == annotList) {
//            return deletedAnnots;
//        }
//        if (false == annotList.contains(a)) {
//            return deletedAnnots;
//        }
//        
//      
//
//        deleteAnnotationOld(gNode, a, /*annotsToBeDeleted, */ deletedAnnots, createNewAnnots);
//        for (Annotation annot : deletedAnnots) {
//            n = annot.getAnnotationDetail().getAnnotatedNode();
//            nvi = n.getVariableInfo();
//            if (null == nvi) {
//                continue;
//            }
//            annotList = nvi.getGoAnnotationList();
//            if (null == annotList) {
//                continue;
//            }
//            annotList.remove(annot);
//            if (annotList.isEmpty()) {
//                nvi.setGoAnnotationList(null);
//            }
//        }
//        return deletedAnnots;
//    }

//    private static void deleteAnnotationOld(GeneNode gNode, Annotation a, /*HashSet<Annotation> annotsToBeDeleted, */ HashSet<Annotation> deletedAnnots, boolean createNewAnnots) {
//        if (false == a.isCreatedByPaint()) {
//            return;
//        }
//        
//        String code = a.getSingleEvidenceCodeFromSet();
////        if (false == Evidence.CODE_IBD.equals(code) && false == false == Evidence.CODE_IBA.equals(code) && false == Evidence.CODE_IKR.equals(code) && false == Evidence.CODE_IRD.equals(code)) {
////            return;
////        }
////        if (true == "PTN002224259".equals(gNode.getNode().getStaticInfo().getPublicId()) || true == " PTN002224260".equals(gNode.getNode().getStaticInfo().getPublicId())) {
////            System.out.println("Here");
////        }
//        System.out.println("Deleting annotation for " + gNode.getNode().getStaticInfo().getPublicId() + " code " + a.getSingleEvidenceCodeFromSet());
//        // First remove dependent annotations
//        ArrayList<GeneNode> descendents = new ArrayList<GeneNode>();
//        GeneNodeUtil.allNonPrunedDescendents(gNode, descendents);
//        for (GeneNode desc : descendents) {
//            Node n = desc.getNode();
////            if (true == "PTN001597050".equals(desc.getNode().getStaticInfo().getPublicId()) || true == " PTN002224260".equals(desc.getNode().getStaticInfo().getPublicId())) {
////                System.out.println("desc Here");
////            }
//
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                continue;
//            }
//            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//            if (null == annotList) {
//                continue;
//            }
////            System.out.println("node " + n.getStaticInfo().getPublicId() + " has " + annotList.size() + " annotations ");
////            HashSet<Annotation> removedSet = new HashSet<Annotation>();
//            for (Annotation annot : annotList) {
//                if (false == annot.withExists(a)) {
////                    if (Evidence.CODE_IBA.equals(annot.getEvidence().getEvidenceCode())) {
////                        System.out.println("Passing by IBA annotation");
////                    }
//                    continue;
//                }
//                
//                if (false == annot.isRequired(a)) {
//                    annot.removeWith(a);
//                    continue;
//                }
//
//                System.out.println("Deleting descendent annotation for " + n.getStaticInfo().getPublicId() + " annot code " + annot.getSingleEvidenceCodeFromSet());
//                
//                // Before removing the with (aka propagator), remove annotations that are dependent on this annotation itself                
//                deleteAnnotationOld(desc, annot, /*annotsToBeDeleted, */ deletedAnnots, false);
////                if (true == "PTN002224259".equals(gNode.getNode().getStaticInfo().getPublicId())) {
////                        System.out.println("Processing special");
////                }
////                if (null != annot.getParentAnnotation()) {
////                    continue;
////                }
//
//                if (Evidence.CODE_IKR == code || Evidence.CODE_IRD == code) {
//                    HashSet<WithEvidence> withEvSet = annot.getAnnotationDetail().getWithEvidenceAnnotSet();
//                    if (null != withEvSet) {
//                        HashSet<Annotation> withs = new HashSet<Annotation>();
//                        for (WithEvidence we: withEvSet) {
//                            withs.add((Annotation)we.getWith());
//                        }
//                        
//                        
//                        withs.remove(a);
//                        if (null != withs && 1 == withs.size()) {
//                        Annotation with = null;
//                            for (Annotation w : withs) {
//                                with = w;
//                                break;
//                            }
//                            if (true == with.getAnnotationDetail().getAnnotatedNode().equals(n)) {
//                                Annotation removedAnnot = annot.removeWith(a);
//                                //annotsToBeDeleted.add(annot);
//                                deletedAnnots.add(annot);
//                                System.out.println("Deleting annotation code " + annot.getSingleEvidenceCodeFromSet() + " term " + annot.getGoTerm() + " associated with node " + annot.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
//                            }
//
//                        }                        
//                        
//                        
//                        
//                        
//                        
//                    }
//                    HashSet<Annotation> withs = (HashSet<Annotation>) (annot.getAnnotationDetail().getWithAnnotSet()).clone();
//                    withs.remove(a);
//                    if (null != withs && 1 == withs.size()) {
//                        Annotation with = null;
//                        for (Annotation w : withs) {
//                            with = w;
//                            break;
//                        }
//                        if (true == with.getAnnotationDetail().getAnnotatedNode().equals(n)) {
//                            Annotation removedAnnot = annot.removeWith(a);
//                            //annotsToBeDeleted.add(annot);
//                            deletedAnnots.add(annot);
//                            System.out.println("Deleting annotation code " + annot.getSingleEvidenceCodeFromSet() + " term " + annot.getGoTerm() + " associated with node " + annot.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
//                        }
////                        Annotation childAnnot = annot.getChildAnnotation();
////                        if (null != childAnnot && Evidence.CODE_IBA.equals(childAnnot.getEvidence().getEvidenceCode())) {
////                            //annotsToBeDeleted.add(childAnnot);
////                            deletedAnnots.add(childAnnot);
////                            System.out.println("Deleting annotation code " + childAnnot.getEvidence().getEvidenceCode() + " term " + childAnnot.getGoTerm() + " associated with node " + childAnnot.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
////                            //removedSet.add(childAnnot);                            
////                        }
//                        //deleteAnnotation(desc, with, /*annotsToBeDeleted, */deletedAnnots, false);
//                    }
//
//                } else {
//                    Annotation removedAnnot = annot.removeWith(a);
//                    if (null == removedAnnot) {
//                        System.out.println("Cannot remove annotation with code " + a.getSingleEvidenceCodeFromSet() + " from annotation with evidence code " + annot.getSingleEvidenceCodeFromSet());
//                    }
//                    HashSet<WithEvidence> remainingWiths = annot.getAnnotationDetail().getWithEvidenceAnnotSet();
//                    if (null == remainingWiths || 0 == remainingWiths.size()) {
//                        if (null == annot.getSingleEvidenceCodeFromSet()) {
//                            System.out.println("Here");
//                        }
//                        //annotsToBeDeleted.add(annot);
//                        deletedAnnots.add(annot);
//                        System.out.println("Deleting annotation code " + annot.getSingleEvidenceCodeFromSet() + " term " + annot.getGoTerm() + " associated with node " + annot.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
//                    }
//                }
//
//            }
//        }
//        String evCode = a.getSingleEvidenceCodeFromSet();
//        if (true == Evidence.CODE_IBD.equals(evCode) || true == Evidence.CODE_IBA.equals(evCode)) {
//            deletedAnnots.add(a);
//            return;
//        } else if ((true == Evidence.CODE_IKR.equals(evCode) && false == gNode.isLeaf()) || true == Evidence.CODE_IRD.equals(evCode)) {
//            Annotation lastIBD = getLastIBD(a, gNode);
////            Annotation child = a.getChildAnnotation();
////            if (null != child && true == Evidence.CODE_IBA.equals(child.getEvidence().getEvidenceCode())) {
//                // Need to delete ancestral annotations from descendents
//                descendents.add(gNode);
//                for (GeneNode desc : descendents) {
//                    Node descNode = desc.getNode();
//                    NodeVariableInfo nvi = descNode.getVariableInfo();
//                    if (null != nvi) {
//                        ArrayList<Annotation> descAnnotList = nvi.getGoAnnotationList();
//                        if (null != descAnnotList) {
//                            for (Annotation descAnnot : descAnnotList) {
//                                String descEvCode = descAnnot.getSingleEvidenceCodeFromSet();
//                                if (true == Evidence.CODE_IBA.equals(descEvCode) /*&& true == child.getGoTerm().equals(descAnnot.getGoTerm() ) */) {
//                                    deletedAnnots.add(descAnnot);
//                                }
//                                
//                                // Handle case where the descAnnotation has been "NOTTED" - Need to remove these as well
//                                else if ((true == Evidence.CODE_IKR.equals(descEvCode) && false == gNode.isLeaf()) || true == Evidence.CODE_IRD.equals(descEvCode)) {
//                                    if (true == descAnnot.withExists(lastIBD)) {
//                                        deletedAnnots.add(descAnnot);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            //}
//            deletedAnnots.add(a);
//
//            if (null != lastIBD && true == createNewAnnots && ((true == Evidence.CODE_IKR.equals(evCode) && false == gNode.isLeaf()) || true == Evidence.CODE_IRD.equals(evCode))) {
//                ArrayList<GeneNode> withNodes = new ArrayList<GeneNode>();
//                HashSet<Annotation> withSet = lastIBD.getAnnotationDetail().getWithAnnotSet();
//                PaintManager pm = PaintManager.inst();
//                for (Annotation with : withSet) {
//                    withNodes.add(pm.getGeneByPTNId(with.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId()));
//                }
//                ArrayList<GeneNode> propagateList = new ArrayList<GeneNode>();
//                GeneNodeUtil.allNonPrunedDescendents(gNode, propagateList);
//                propagateList.removeAll(withNodes);
//                propagateList.add(gNode);
//                addIBAAnnotation(lastIBD.getGoTerm(), propagateList, lastIBD, lastIBD.getQualifierSet());
//            }
//
//        }
//    }

    public static boolean hasDirectNot(GeneNode gNode, GOTermHelper gth) {
        Node node = gNode.getNode();
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return false;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return false;
        }
        String go_aspect = AspectSelector.aspects.get(AspectSelector.inst().getAspect().toString());
        for (Annotation annot : annotList) {
            if (false == go_aspect.equals(gth.getTerm(annot.getGoTerm()).getAspect())) {
                continue;
            }
            String code = annot.getSingleEvidenceCodeFromSet();
            if (Evidence.CODE_IBD.equals(code) || (Evidence.CODE_IKR.equals(code) && false == gNode.isLeaf()) || Evidence.CODE_IRD.equals(code) || Evidence.CODE_TCV.equals(code)) {
                LinkedHashMap<Qualifier, HashSet<Annotation>> map = annot.getAnnotationDetail().getQualifierLookup();
                if (true == QualifierDif.containsNegative(map.keySet())) {
                    return true;
                }
            }
        }
        return false;
    }

  
    public static void propagateIBD(Annotation ibd) {
        HashSet<Node> modifiedSet = new HashSet<Node>();
        StringBuffer errorMsgBuf = new StringBuffer();
        PaintManager pm = PaintManager.inst();
        AnnotationHelper.propagateIBD(ibd, pm.getTaxonHelper(), pm.goTermHelper(), errorMsgBuf, modifiedSet, new HashSet<Annotation>());
    }
    
    public static HashSet<Annotation> getApplicableAnnotations(ArrayList<GeneNode> leaves, GOTerm goTerm, GOTermHelper gth, HashSet<Qualifier> qSet) {
        //boolean neg = QualifierDif.containsNegative(qSet);
        HashSet<Annotation> annots = new HashSet<Annotation>();
        for (GeneNode leaf : leaves) {
            AnnotationForTerm aft = new AnnotationForTerm(leaf, goTerm, gth);
            if (false == aft.annotationExists()) {
                continue;
            }
            for (Annotation a: aft.getAnnotSet()) {
                QualifierDif qd = new QualifierDif(qSet, a.getQualifierSet());
                if (QualifierDif.QUALIFIERS_SAME != qd.getDifference()) {
                    continue;
                }
                annots.add(a);
            }
        }
        return annots;
    }

    public static void graftBranch(GeneNode gNode) {
        PaintManager pm = PaintManager.inst();
        AnnotationHelper.fixAnnotationsForGraftPruneOperation(pm.getTree().getRoot().getNode(), gNode.getNode(), pm.getTaxonHelper(), pm.goTermHelper());
        branchNotify(gNode);
    }

    public static void pruneBranch(GeneNode gNode) {
        ArrayList<GeneNode> descendents = new ArrayList<GeneNode>();
        GeneNodeUtil.allDescendents(gNode, descendents);
        descendents.add(0, gNode);
        for (GeneNode desc : descendents) {
            Node node = desc.getNode();
            NodeVariableInfo nvi = node.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            for (Iterator<Annotation> annotIter = annotList.iterator(); annotIter.hasNext();) {
                Annotation a = annotIter.next();
                if (false == a.isExperimental()) {
                    annotIter.remove();
                }
            }
            if (true == annotList.isEmpty()) {
                nvi.setGoAnnotationList(null);
            }
        }

        PaintManager pm = PaintManager.inst();
        AnnotationHelper.fixAnnotationsForGraftPruneOperation(pm.getTree().getRoot().getNode(), gNode.getNode(), pm.getTaxonHelper(), pm.goTermHelper());
        
        branchNotify(gNode);
    }
    
    


    public static void branchNotify(GeneNode node) {
        TreePanel tree = PaintManager.inst().getTree();
        tree.handlePruning(node);
        if (node.isLeaf()) {
            MSAPanel msa = PaintManager.inst().getMSAPanel();
            msa.handlePruning(node);
        }
        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
    }
    
    public static Annotation getAssociatedIBAForIKRorIRD(GeneNode gNode, Annotation IKRorIRDAnnotation) {
        Node n = gNode.getNode();
//        if (true == "PTN002467107".equals(n.getStaticInfo().getPublicId())) {
//            System.out.println("Here");
//        }        
        String code = IKRorIRDAnnotation.getSingleEvidenceCodeFromSet();
        if (false == Evidence.CODE_IRD.equals(code) && (false == Evidence.CODE_IKR.equals(code)) || false == gNode.isLeaf()) {
            System.out.println("Call to get associated IBA for non IKR or IRD");
            return null;
        }
        Annotation with = null;

        HashSet<Annotation> ikrIrdwithSet = IKRorIRDAnnotation.getAnnotationDetail().getWithAnnotSet();
        if (null == ikrIrdwithSet) {
            return null;
        }
        for (Annotation iw : ikrIrdwithSet) {
            HashSet<Annotation> withSet = iw.getAnnotationDetail().getWithAnnotSet();
            if (null == withSet) {
                continue;
            }
            if (iw == IKRorIRDAnnotation) {
                continue;
            }
            with = iw;
            break;
        }

        
        Node node = gNode.getNode();
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        for (Annotation a: annotList) {
            if (a == IKRorIRDAnnotation) {
                continue;
            }
            if (true == Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet())) {
                if (true == a.getAnnotationDetail().getWithAnnotSet().contains(with)) {
                    return a;
                }
            }
        }
        return null;        
        
    }
    
    
    public static Annotation getAssociatedIKRorIRDforIBA(GeneNode gNode, Annotation ibaAnnotation) {
        if (false == Evidence.CODE_IBA.equals(ibaAnnotation.getSingleEvidenceCodeFromSet())) {
            System.out.println("Call to get associated IRD OR IKR for non IBA");
            return null;
        }
        Annotation with = null;
        for (Annotation aWith: ibaAnnotation.getAnnotationDetail().getWithAnnotSet()) {
            with = aWith;
            break;
        }
        Node node = gNode.getNode();
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        for (Annotation a: annotList) {
            String code = a.getSingleEvidenceCodeFromSet();
            if (true == Evidence.CODE_IKR.equals(code) || true == Evidence.CODE_IRD.equals(code)) {
                if (true == a.getAnnotationDetail().getWithAnnotSet().contains(with)) {
                    return a;
                }
            }
        }
        return null;
    }
       
    public static HashSet<Annotation> getAnnotationsForTerm(GeneNode gNode, GOTerm term) {
        HashSet<Annotation> annotSet = new HashSet<Annotation>();
        
        GOTermHelper gth = PaintManager.inst().goTermHelper();
        
        ArrayList<GeneNode> desc = new ArrayList<GeneNode>();
        GeneNodeUtil.allNonPrunedDescendents(gNode, desc);
        List<GeneNode> leaves = GeneNodeUtil.getAllLeaves(desc);
        
        for (GeneNode leaf: leaves) {        
            AnnotationForTerm aft = new AnnotationForTerm(leaf, term, gth);
            if (false == aft.annotationExists()) {
                continue;
            }
            annotSet.addAll(aft.getAnnotSet());
        }
        if (true == annotSet.isEmpty()) {
            return null;
        }
        return annotSet;
    }
    
    public static ArrayList<AnnotationForTerm> getAnnotsAndApplicableQls(GeneNode gNode, GOTerm term) {
        ArrayList<AnnotationForTerm> annotForTerm = new ArrayList<AnnotationForTerm>();
        GOTermHelper gth = PaintManager.inst().goTermHelper();
        
        ArrayList<GeneNode> desc = new ArrayList<GeneNode>();
        GeneNodeUtil.allNonPrunedDescendents(gNode, desc);
        List<GeneNode> leaves = GeneNodeUtil.getAllLeaves(desc);
        
        for (GeneNode leaf: leaves) {        
            AnnotationForTerm aft = new AnnotationForTerm(leaf, term, gth);
            if (false == aft.annotationExists()) {
                continue;
            }
            annotForTerm.add(aft);
        }
        if (true == annotForTerm.isEmpty()) {
            return null;
        }        
        return annotForTerm;
    }
    
    public static boolean qualifiersMatch(HashSet<Annotation> annotSet) {
        if (null == annotSet || true == annotSet.isEmpty()) {
            return false;
        }
        
        Boolean positive = null;
        Boolean negative = null;
        for (Annotation a: annotSet) {
            if (null != positive && null != negative) {
                break;
            }
            if (QualifierDif.containsNegative(a.getQualifierSet())) {
                negative = true;
            }
            else {
                positive = true;
            }
        }
        if (null != positive && null != negative) {
            return false;
        }
        return true;
    }
    
    public static boolean contains(HashSet<WithEvidence> withEvSet, IWith with) {
        if (null == withEvSet || null == with) {
            return false;
        }
        for (WithEvidence we: withEvSet) {
            if (we.getWith() == with) {
                return true;
            }
        }
        return false;
        
    }
    


}
