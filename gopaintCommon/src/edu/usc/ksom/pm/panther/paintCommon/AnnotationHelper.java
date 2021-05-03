/**
 *  Copyright 2021 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class AnnotationHelper implements Serializable {

    public final static String KEY_RESIDUES_EC = "IKR";

    public final static String DIVERGENT_EC = "IRD";

    public final static String DESCENDANT_SEQUENCES_EC = "IBD"; // was IDS
    public static final String ANCESTRAL_EVIDENCE_CODE = "IBA"; // was IAS
    public final static String PAINT_REF = "PAINT_REF";

    public static final String TCV_MSG_PART_1 = "Info - Inserting " + Evidence.CODE_TCV + " to prevent propagation of term ";
    public static final String TCV_MSG_PART_2 = " to node ";
    public static final String TCV_MSG_PART_3 = " with species ";
    public static final String TCV_MSG_PART_4 = ".\n";
    public static final String STR_COMMA = ",";

//    public static void propagateAndFixAnnotationsForBookOpen(String familyAcc, AnnotationNode root, Hashtable<String, AnnotationNode> nodeLookupTbl) {
//        HashSet<Annotation> removedAnnots = new HashSet<Annotation>();
//        // This will just remove the annotations from the nodes, does not consider
//        // annotations that may have these removed annotations as withs (i.e. dependencies)        
//        removeNonExperimentalNonPaintAnnotations(root, removedAnnots);
//
//        // Other dependent annotations may get removed because of removing the above annotations
//        HashSet<Annotation> newRemovedAnnotSet = new HashSet<Annotation>();
//        do {
//            newRemovedAnnotSet.clear();
//            handleRemovedWithAnnots(root, removedAnnots, newRemovedAnnotSet);
//            removedAnnots = (HashSet<Annotation>) newRemovedAnnotSet.clone();
//        } while (0 != newRemovedAnnotSet.size());
//
//        if (root.isPruned()) {
//            return;
//        }
//        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
//        // First go through all the IBA's that were created when IKR or IRD was created and make a list.
//        allNonPrunedDescendents(root, descendents);
//        descendents.add(0, root);
//        ArrayList<Annotation> IBAannots = existingIBAannots(descendents);
//        propagateAnnotationsForBookOpen(familyAcc, root, IBAannots, nodeLookupTbl);
//    }
    private static void removeNonExperimentalNonPaintAnnotations(AnnotationNode gNode, HashSet<Annotation> removedAnnotSet) {
        if (null == gNode) {
            return;
        }
        NodeVariableInfo nvi = gNode.getNode().getVariableInfo();
        if (null != nvi) {

            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null != annotList) {
                ArrayList<Annotation> removeList = new ArrayList<Annotation>();
                for (Annotation a : annotList) {

                    if (true == a.isExperimental() || true == a.isPaint()) {
                        continue;
                    }
                    //System.out.println("Removed annotation " + a.getGoTerm() + " from node " + gNode.getNode().getStaticInfo().getPublicId());
                    removeList.add(a);
                }
                removedAnnotSet.addAll(removeList);
                for (Annotation a : removeList) {
//                    pm.addToRemovedAnnotationLookup(gNode, a);
                }
                annotList.removeAll(removeList);
                if (annotList.isEmpty()) {
                    nvi.setGoAnnotationList(null);
                }
            }
        }

        List<AnnotationNode> children = gNode.getChildren();
        if (null == children) {
            return;
        }
        for (AnnotationNode child : children) {
            removeNonExperimentalNonPaintAnnotations(child, removedAnnotSet);
        }
    }

    private static void handleRemovedWithAnnots(AnnotationNode gNode, HashSet<Annotation> curRemovedAnnotSet, HashSet<Annotation> newRemovedAnnotSet) {
        NodeVariableInfo nvi = gNode.getNode().getVariableInfo();
        if (null != nvi) {
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null != annotList) {
                for (Annotation a : annotList) {
                    for (Annotation removedAnnot : curRemovedAnnotSet) {
                        Annotation ra = a.removeWith(removedAnnot);
                        if (null != ra) {
                            HashSet<Annotation> remainingWiths = a.getAnnotationDetail().getWithAnnotSet();
                            if (null == remainingWiths || true == remainingWiths.isEmpty()) {
                                newRemovedAnnotSet.add(a);
                            } else if (Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet()) || Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet()) || Evidence.CODE_TCV.equals(a.getSingleEvidenceCodeFromSet())) {
                                newRemovedAnnotSet.add(a);
                            }
                        }
                    }
                }
            }
        }
        List<AnnotationNode> children = gNode.getChildren();
        if (null != children) {
            for (AnnotationNode child : children) {
                handleRemovedWithAnnots(child, curRemovedAnnotSet, newRemovedAnnotSet);
            }
        }
    }

    public static void allNonPrunedDescendents(AnnotationNode gNode, List<AnnotationNode> nodeList) {
        if (null == gNode || null == nodeList) {
            return;
        }
        Node n = gNode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi && nvi.isPruned()) {
            return;
        }
        List<AnnotationNode> children = gNode.getChildren();
        if (null == children) {
            return;
        }
        for (AnnotationNode child : children) {
            Node cn = child.getNode();
            NodeVariableInfo childNvi = cn.getVariableInfo();
            if (null != childNvi && childNvi.isPruned()) {
                continue;
            }
            nodeList.add(child);
            allNonPrunedDescendents(child, nodeList);
        }
    }

    public static Annotation getSingleWithPropagatorAnnot(Annotation a) {
        HashSet<WithEvidence> withAnnotSet = a.getAnnotationDetail().getWithEvidenceAnnotSet();
        if (null == withAnnotSet || 1 != withAnnotSet.size()) {
            return null;
        }
        for (WithEvidence we : withAnnotSet) {
            return (Annotation) we.getWith();
        }
        return null;
    }

    public static boolean isIBAForIKRorIRD(Annotation ibaAnnot, Node node) {
        if (null == node) {
            return false;
        }
        Annotation ibaPropagator = getSingleWithPropagatorAnnot(ibaAnnot);
        if (null == ibaPropagator) {
            return false;
        }

        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return false;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return false;
        }
        for (Annotation a : annotList) {
            String code = a.getSingleEvidenceCodeFromSet();
            if (Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code)) {
                HashSet<Annotation> withAnnotSet = a.getAnnotationDetail().getWithAnnotSet();
                if (null == withAnnotSet) {
                    continue;
                }
                for (Annotation withAnnot : withAnnotSet) {
                    if (ibaPropagator == withAnnot) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

//    private static ArrayList<Annotation> existingIBAannots(ArrayList<AnnotationNode> descendents) {
//        ArrayList<Annotation> ibaAnnots = new ArrayList<Annotation>();
//        for (AnnotationNode an : descendents) {
//            Node n = an.getNode();
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
//                if (false == ANCESTRAL_EVIDENCE_CODE.equals(a.getSingleEvidenceCodeFromSet())) {
//                    continue;
//                }
////                if (true == "38109929".equals(a.getAnnotationId())) {
////                    System.out.println("Here");
////                }
//                // Have IBA without propagator
//                Annotation propagatorAnnot = null;
//                if (null == a.getAnnotationDetail().getWithAnnotSet()) {
////                    System.out.println("Annotation does not have withs for " + a.getAnnotationId());
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
//                    if (true == DIVERGENT_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())
//                            || true == KEY_RESIDUES_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())
//                            || true == DESCENDANT_SEQUENCES_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())) {
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

//    private static void propagateAnnotationsForBookOpen(String familyAcc, AnnotationNode gNode, ArrayList<Annotation> initialIBAannots, Hashtable<String, AnnotationNode> nodeLookupTbl) {
//        Node n = gNode.getNode();
//        // Iterate over children
//        List<AnnotationNode> children = gNode.getChildren();        
////        System.out.println("Processing node " + n.getStaticInfo().getNodeAcc() + " public id " + n.getStaticInfo().getPublicId());
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null != nvi && null != nvi.getGoAnnotationList()) {
//            ArrayList<Annotation> annotList = (ArrayList<Annotation>) nvi.getGoAnnotationList().clone();     // Clone since annotation list gets modified within loop
//            if (null != annotList) {
////                System.out.println("Node has " + annotList.size() + " annotations");
//                for (int i = 0; i < annotList.size(); i++) {
//                    Annotation a = annotList.get(i);
////                    System.out.println("Processing " + i + " annotation");
//                    //Evidence e = a.getEvidence();
////                    if (true == Evidence.CODE_IBA.equals(e.getEvidenceCode()) && false == gNode.isLeaf()) {
////                        System.out.println("Here");
////                    }
//                    if (true == Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet()) && true == initialIBAannots.contains(a)) {
//                        Annotation ibdProp = getIBDpropagator(a);
//                        Annotation propagator = getPropagator(a);
//                        if (null == ibdProp || null == propagator) {
//                            continue;
//                        }
//                        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
//                        allNonPrunedDescendents(gNode, descendents);
//                        HashSet<AnnotationNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(ibdProp, nodeLookupTbl);
//                        if (null != nodesProvidingEvidence) {
//                            descendents.removeAll(nodesProvidingEvidence);
//                        }
//                        propagateIBAtoNodes(propagator, descendents, a.getGoTerm());
//                    }
//                    if (true == Evidence.CODE_IBD.equals(a.getSingleEvidenceCodeFromSet())) {
//                        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
//                        allNonPrunedDescendents(gNode, descendents);
//                        HashSet<AnnotationNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(a, nodeLookupTbl);
//                        if (null != nodesProvidingEvidence) {
//                            descendents.removeAll(nodesProvidingEvidence);
//                        }
//                        propagateIBAtoNodes(a, descendents, a.getGoTerm());
//                        continue;
//                    }
//                    if (true == Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet()) || true == Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet())) {
//                        // IKR at leaf can be valid
//                        if (true == Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet()) && (null == children || 0 == children.size())) {
//                            continue;
//                        }
//                        // remove IBA's propagated from IBD
//                        Annotation ibdProp = getIBDpropagator(a);
//                        Annotation propagator = getPropagator(a);
//                        if (null == ibdProp) {
//                            System.out.println("Did not find Annotation that was cause of IRD or IRK for " + n.getStaticInfo().getPublicId());
//                            continue;
//                        }
//                        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
//                        allNonPrunedDescendents(gNode, descendents);
//                        HashSet<AnnotationNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(ibdProp, nodeLookupTbl);
//                        if (null != nodesProvidingEvidence) {
//                            descendents.removeAll(nodesProvidingEvidence);
//                        }
//                        descendents.add(gNode);     // Add myself since, IBA has to be removed
//                        for (AnnotationNode descNode : descendents) {
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
//                        if (true == KEY_RESIDUES_EC.equals(a.getSingleEvidenceCodeFromSet())) {
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
//                    }
//                }
//            }
//        } else {
////            System.out.println("No annotations");
//        }
//
//        
//        if (null == children) {
//            return;
//        }
//        for (AnnotationNode child : children) {
//            if (child.isPruned()) {
//                continue;
//            }
//            propagateAnnotationsForBookOpen(familyAcc, child, initialIBAannots, nodeLookupTbl);
//        }
//    }
//    private static HashSet<AnnotationNode> getEvidenceNodesForAnnotation(Annotation a, Hashtable<String, AnnotationNode> nodeLookupTbl) {
//        AnnotationDetail ad = a.getAnnotationDetail();
//        HashSet<WithEvidence> withAnnotSet = ad.getWithEvidenceAnnotSet();
//        if (null == withAnnotSet) {
//            return null;
//        }
//        HashSet<AnnotationNode> withNodes = new HashSet<AnnotationNode>();
//
//        for (WithEvidence we : withAnnotSet) {
//            Annotation annot = (Annotation) we.getWith();
//            Node n = annot.getAnnotationDetail().getAnnotatedNode();
//            AnnotationNode with = nodeLookupTbl.get(n.getStaticInfo().getNodeAcc());
//            withNodes.add(with);
//        }
//        return withNodes;
//    }

    public static Annotation getIBDpropagator(Annotation a) {
        HashSet<Annotation> withs = a.getAnnotationDetail().getWithAnnotSet();
        if (null == withs) {
            return null;
        }
        for (Annotation withAnnot : withs) {
            if (a == withAnnot) {
                continue;
            }
            if (Evidence.CODE_IBD.equals(withAnnot.getSingleEvidenceCodeFromSet())) {
                return withAnnot;
            }
        }
        return null;
    }

    public static Annotation getPropagator(Annotation a) {
        HashSet<Annotation> withs = a.getAnnotationDetail().getWithAnnotSet();
        if (null == withs) {
            return null;
        }
        for (Annotation withAnnot : withs) {
            // This could be an IKR, that is why we have this clause
            if (a == withAnnot) {
                continue;
            }
            return withAnnot;
        }
        return null;
    }

    /*
     // NOTE!!!!!!!!!!!!!!!!!!!!! similar as AnnotationUtil in client package.
     // Only reason for this is due to client working off of GeneNode object that is not created on server
     // Eventually update so that only one copy of routine is in software
     */
//    private static void propagateIBAtoNodes(Annotation propagator, ArrayList<AnnotationNode> nodeList, String term) {
//        for (AnnotationNode gn : nodeList) {
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
////            Evidence newEvidence = new Evidence();
//////            newAnnot.setEvidence(newEvidence);
////            newEvidence.setEvidenceCode(ANCESTRAL_EVIDENCE_CODE);
////            DBReference dbRef = new DBReference();
////            dbRef.setEvidenceType(PAINT_REF);
////            dbRef.setEvidenceValue(familyAcc);
////            newEvidence.addDbRef(dbRef);
//
//            // Get inherited qualifiers.  Set with of qualifiers to propagator of annotation
//            LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = propagator.getAnnotationDetail().getQualifierLookup();
//            for (Qualifier q : qualifierLookup.keySet()) {
//                Qualifier newQ = new Qualifier();
//                newQ.setText(q.getText());
//                newDetail.addToInheritedQualifierLookup(newQ, propagator);
//                newAnnot.addQualifier(newQ);
//            }
//
////            LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = a.getAnnotationDetail().getQualifierLookup();
////            Collection<HashSet<Annotation>> withList = qualifierLookup.values();
////            for (HashSet<Annotation> cur : withList) {
////                cur.clear();
////                cur.add(a);
////            }
////            newDetail.setInheritedQualifierLookup(qualifierLookup);
//        }
//    }

    public static Annotation getPropagatorAnnotForIBA(Annotation iba, Node n) {
        if (false == Evidence.CODE_IBA.equals(iba.getSingleEvidenceCodeFromSet())) {
            return null;
        }
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        Annotation propagator = getPropagator(iba);
        if (null == propagator) {
            return null;
        }
        for (Annotation a : annotList) {
            String code = a.getSingleEvidenceCodeFromSet();
            if (null == code) {
                continue;
            }
            if ((Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code)) && (propagator == getPropagator(a))) {
                return a;
            }

        }
        return null;
    }

    public String getPaintEvidenceAcc(String familyAcc) {

        int acc = Integer.valueOf(familyAcc.substring("PTHR".length())).intValue();
        String paint_id = String.format("%1$07d", acc);
        return paint_id;

    }

    public static boolean isQualifierSetValidForTerm(GOTerm term, Set<Qualifier> qSet, GOTermHelper gth) {
        if (null == term) {
            return false;
        }
        if (null == qSet) {
            return true;
        }
        for (Qualifier q : qSet) {
            if (false == gth.isQualifierValidForTerm(term, q)) {
                return false;
            }
        }
        return true;
    }

    // Return if a given term and associted qualifiers can be annotated using a with term and associated qualifiers
    public static boolean canCreateIBDAnnotUsingWith(GOTerm toTerm, Set<Qualifier> toQualifierSet, Annotation withAnnot, GOTermHelper gth) {
        if (false == withAnnot.isExperimental()) {
            return false;
        }
        GOTerm withTerm = gth.getTerm(withAnnot.getGoTerm());
        if (null == withTerm) {
            return false;
        }
        HashSet<Qualifier> withQualifierSet = withAnnot.getQualifierSet();

        // Ensure qualifier is valid for both to and with terms
        if (null != toQualifierSet) {
            for (Qualifier q : toQualifierSet) {
                if (false == gth.isQualifierValidForTerm(toTerm, q)) {
                    return false;
                }
            }
        }

        if (null != withQualifierSet) {
            for (Qualifier q : withQualifierSet) {
                if (false == gth.isQualifierValidForTerm(withTerm, q)) {
                    return false;
                }
            }
        }
        // if terms and qualifiers match, then annotation is okay
        if (true == toTerm.equals(withTerm) && true == QualifierDif.allQualifiersSame(toQualifierSet, withQualifierSet)) {
            return true;
        }

        boolean toIsNegative = false;
        boolean withIsNegative = false;
        if (null != toQualifierSet) {
            toIsNegative = QualifierDif.containsNegative(toQualifierSet);
        }

        if (null != withQualifierSet) {
            withIsNegative = QualifierDif.containsNegative(withQualifierSet);
        }

        // Qualifiers cannot be positive and negative
        if (toIsNegative != withIsNegative) {
            return false;
        }

        // Handle positive annotation
        // We can annotate if:
        // 1. ancestor of with term is same as term and qualifiers match
        // 2. both terms match and qualifiers also match
        if (false == toIsNegative) {
            if (false == toTerm.equals(withTerm)) {
                ArrayList<GOTerm> withAncestors = gth.getAncestors(withTerm);
                if (false == withAncestors.contains(toTerm)) {
                    return false;
                }
                if (null != toQualifierSet) {
                    for (Qualifier q : toQualifierSet) {
                        if (null == QualifierDif.find(withQualifierSet, q)) {
                            return false;
                        }
                    }
                }
            } else {
                // Both terms are same, ensure qualifiers are also same
                if (false == QualifierDif.allQualifiersSame(toQualifierSet, withQualifierSet)) {
                    return false;
                }
            }
        } else {
            // Negative annotation
            // Here we can annotate if:
            // 1. Ancestor of term being annotated is same as with term and qualifiers match 
            // 2. both terms match and qualifiers also match
            if (false == toTerm.equals(withTerm)) {
                ArrayList<GOTerm> toAncestors = gth.getAncestors(toTerm);
                if (false == toAncestors.contains(toTerm)) {
                    return false;
                }
                if (null != toQualifierSet) {
                    for (Qualifier q : toQualifierSet) {
                        if (null == QualifierDif.find(withQualifierSet, q)) {
                            return false;
                        }
                    }
                }

            } else {
                // Both terms are same, ensure qualifiers are also same
                if (false == QualifierDif.allQualifiersSame(toQualifierSet, withQualifierSet)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ArrayList<Annotation> getPossibleExperimentalAnnots(String term, Set<Qualifier> qSet, ArrayList<Node> leaves, GOTermHelper gth) {
        if (null == term || null == leaves) {
            return null;
        }
//        if (true == "GO:0005813".equals(term)) {
//            System.out.println("Here");
//        }
        GOTerm toTerm = gth.getTerm(term);
        
        ArrayList<Annotation> annots = new ArrayList<Annotation>();
        for (Node leaf : leaves) {
            NodeVariableInfo nvi = leaf.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> curAnnotList = nvi.getGoAnnotationList();
            if (null == curAnnotList) {
                continue;
            }

            for (Annotation cur : curAnnotList) {
//                if (true == "GO:0015179".equals(cur.getGoTerm()) && "GO:0015179".equals(term)) {
//                    System.out.println("Here");
//                }
                if (true == canCreateIBDAnnotUsingWith(toTerm, qSet, cur, gth)) {
                    annots.add(cur);
                }
            }
        }
        if (annots.isEmpty()) {
            return annots;
        }
        
        // There may be duplicate annotations.  Need to remove these
        // Create an IBD annotation and add the withs.  The duplicated withs will not be added. Just return the withs that are added
        Annotation temp = new Annotation();
        temp.setGoTerm(term);
        if (null != qSet) {
            temp.setQualifierSet(new HashSet<Qualifier>(qSet));
        }
        AnnotationDetail ad = temp.getAnnotationDetail();
        for (Annotation with: annots) {
            WithEvidence we = new WithEvidence();
                we.setEvidenceCode(Evidence.CODE_IBD);
                we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_EXP);
                we.setWith(with);
                we.setEvidenceId(with.getAnnotationId());
                ad.addWithEvidence(we);     // Note, Evidence is added only if there does not exist another evidence with same values
        }
        
        return new ArrayList(ad.getWithAnnotSet());
    }

    public static String checkValidity(ArrayList<Annotation> annotList, GOTermHelper gth) {
        if (null == annotList) {
            return null;
        }
        for (Annotation a : annotList) {
            String err = checkValidity(a, gth);
            if (null != err) {
                return err;
            }
        }
        return null;
    }

    public static final String ERROR_CDE_UNKNOWN_EVIDENCE_TYPE = "Unknown evidence type";
    public static final String ERROR_CDE_NO_ANNOTATION_DETAIL = "No annotation detail found";
    public static final String ERROR_CDE_INVALID_TERM = "Invalid term";
    public static final String ERROR_CDE_CANNOT_CREATE_IBD_USING_WITH = "Found invalid with annotation for IBD using with id ";
    public static final String ERROR_CDE_NO_WITH_FOUND = "No with found for annotation";
    public static final String ERROR_CDE_NO_IBD_PROP = "NO IBD propagator for IKR/IRD/TCV annotation";
    public static final String ERROR_CDE_IKR_IRD_TCV_ANNOT_QUALIFIER_NOT_OPPOSITE_IBD = "IKR/IRD/TCV annotation qualifier not opposite of IBD";
    public static final String ERROR_CDE_IBA_NO_PROPAGATOR = "No propagator for IBA";

    public static String checkValidity(Annotation a, GOTermHelper gth) {
        AnnotationDetail ad = a.getAnnotationDetail();
        String term = a.getGoTerm();
        if (null == term) {
            return ERROR_CDE_INVALID_TERM;
        }
        GOTerm goTerm = gth.getTerm(term);

        if (null == goTerm) {
            return ERROR_CDE_INVALID_TERM;
        }
        Set<Qualifier> qSet = ad.getQualifiers();
        String evCde = a.getSingleEvidenceCodeFromSet();
        if (Evidence.CODE_IBD.equals(evCde)) {
            if (null == ad) {
                return ERROR_CDE_NO_ANNOTATION_DETAIL;
            }
            HashSet<Annotation> withs = ad.getWithAnnotSet();
            if (null == withs || 0 == withs.size()) {
                return ERROR_CDE_NO_WITH_FOUND;
            }
            for (Annotation with : withs) {
                if (false == canCreateIBDAnnotUsingWith(goTerm, qSet, with, gth)) {
                    return ERROR_CDE_CANNOT_CREATE_IBD_USING_WITH + with.getAnnotationId();
                }
            }
        } else if (Evidence.CODE_IKR.equals(evCde) || Evidence.CODE_IRD.equals(evCde) || Evidence.CODE_TCV.equals(evCde)) {
            HashSet<Annotation> withs = ad.getWithAnnotSet();
            if (null == withs || 0 == withs.size()) {
                return ERROR_CDE_NO_WITH_FOUND;
            }
            Annotation IBDPropagator = getIBDpropagator(a);
            if (null == IBDPropagator) {
                return ERROR_CDE_NO_IBD_PROP;
            }

            if (false == QualifierDif.areOpposite(IBDPropagator.getQualifierSet(), qSet)) {
                return ERROR_CDE_IKR_IRD_TCV_ANNOT_QUALIFIER_NOT_OPPOSITE_IBD;
            }
        } else if (Evidence.CODE_IBA.equals(evCde)) {
            Annotation propagator = getPropagator(a);
            if (null == propagator) {
                return ERROR_CDE_IBA_NO_PROPAGATOR;
            }
        } else {
            return ERROR_CDE_UNKNOWN_EVIDENCE_TYPE;
        }
        return null;
    }

    public static void propagateIBD(Annotation ibd, TaxonomyHelper taxonomyHelper, GOTermHelper goTermHelper, StringBuffer errorBuf, HashSet<Node> modifiedNodeSet, HashSet<Annotation> addedAnnotSet) {
        AnnotationDetail ad = ibd.getAnnotationDetail();
        Node annotNode = ad.getAnnotatedNode();
        HashSet<Annotation> annotWiths = ad.getWithAnnotSet();
        HashSet<Node> withNodeSet = new HashSet<Node>();
        for (Annotation with : annotWiths) {
            withNodeSet.add(with.getAnnotationDetail().getAnnotatedNode());
        }
        HashSet<Qualifier> qSet = ibd.getQualifierSet();
        ArrayList<Node> children = annotNode.getStaticInfo().getChildren();
        if (null != children) {
            for (Node child : children) {
                propagateIBA(child, ibd.getGoTerm(), qSet, ibd, withNodeSet, taxonomyHelper, goTermHelper, errorBuf, modifiedNodeSet, addedAnnotSet);
            }
        }
    }

    /*
    // doNotAnnotSet - Do not annotate any node that is in withNodeSet
     */
    public static void propagateIBA(Node n, String term, HashSet<Qualifier> qSet, Annotation with, HashSet<Node> withNodeSet, TaxonomyHelper taxonomyHelper, GOTermHelper goTermHelper, StringBuffer errorBuf, HashSet<Node> modifiedNodeSet, HashSet<Annotation> addedAnnotSet) {
        NodeStaticInfo nsi = n.getStaticInfo();
//        if (true == "PTN002346951".equals(nsi.getPublicId()) && ("GO:0005814".equals(term))) {
//            System.out.println("Here");
//        }
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi && nvi.isPruned()) {
            return;
        }
        if (withNodeSet != null && withNodeSet.contains(n)) {
            return;
        }

        // Ensure IBA can be created for this node        
        if (nvi != null && false == isIBABlockedViaIkrIrdTcv(with, term, qSet, nvi.getGoAnnotationList(), goTermHelper)) {
            System.out.println("Not propagating IBA for " + term + " to node " + n.getStaticInfo().getPublicId() + " since it is blocked via IKR, IRD or TCV");
//            ArrayList<Node> children = nsi.getChildren();
//            if (null != children) {
//                for (Node child : children) {
//                    propagateIBA(child, term, qSet, with, withNodeSet, taxonomyHelper, goTermHelper, errorBuf, modifiedNodeSet);
//                }
//            }
            return;
        }
        // IBA may not be valid for node if it is already annotated with IBA to term. However descendants need to be checked
        if (nvi != null && false == IBAValid(with, term, nvi.getGoAnnotationList())) {
            ArrayList<Node> children = nsi.getChildren();
            if (null != children) {
                for (Node child : children) {
                    propagateIBA(child, term, qSet, with, withNodeSet, taxonomyHelper, goTermHelper, errorBuf, modifiedNodeSet, addedAnnotSet);
                }
            }
            return;
        }
        
        // Add TCV annotation and stop propagating for any taxonomy constraints violation        
        if (false == taxonomyHelper.isTermAndQualifierValidForSpecies(term, nsi.getCalculatedSpecies(), qSet)) {
            errorBuf.insert(0, generateTCVMsg(term, nsi.getPublicId(), nsi.getCalculatedSpecies()));
            // First get qualifiers
            HashSet<Qualifier> newSet = new HashSet<Qualifier>();
            if (null == qSet) {
                Qualifier notQualifier = new Qualifier();
                notQualifier.setText(Qualifier.QUALIFIER_NOT);
                newSet.add(notQualifier);
            } else {
                boolean containsNot = false;
                for (Qualifier q : qSet) {
                    if (q.isNot()) {
                        containsNot = true;
                        continue;
                    }
                    newSet.add(q);
                }
                if (false == containsNot) {
                    Qualifier notQualifier = new Qualifier();
                    notQualifier.setText(Qualifier.QUALIFIER_NOT);
                    newSet.add(notQualifier);
                }
            }

            if (null == nvi) {
                nvi = new NodeVariableInfo();
                n.setVariableInfo(nvi);
            }
            Annotation newAnnot = new Annotation();
            nvi.addGOAnnotation(newAnnot);
            modifiedNodeSet.add(n);
            newAnnot.setGoTerm(term);
            newAnnot.setQualifierSet(newSet);

            AnnotationDetail newDetail = newAnnot.getAnnotationDetail();
            newDetail.setAnnotatedNode(n);
            WithEvidence propWithEv = new WithEvidence();
            propWithEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_TCV);
            propWithEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
            propWithEv.setWith(with);
            newDetail.addWithEvidence(propWithEv);

            WithEvidence newAnnotWithEv = new WithEvidence();
            newAnnotWithEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_TCV);
            newAnnotWithEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
            newAnnotWithEv.setWith(newAnnot);
            newDetail.addWithEvidence(newAnnotWithEv);

            if (null != qSet) {
                for (Qualifier q : qSet) {
                    newDetail.addToInheritedQualifierLookup(q, with);
                }
            }
            if (QualifierDif.containsNegative(newSet)) {
                newDetail.addToAddedQualifierLookup(QualifierDif.getNOT(newSet), newAnnot);
            } else {
                newDetail.addToRemovedQualifierLookup(QualifierDif.getNOT(qSet), newAnnot);
            }
            addedAnnotSet.add(newAnnot);
            return;
        }        

        // If node is leaf, do not want to add an IBA that would negate existing experimental annotation
        if (true == nsi.isLeaf() && null != nvi && null != nvi.getGoAnnotationList()) {
            ArrayList<Annotation> annots = nvi.getGoAnnotationList();
            boolean negAnnot = QualifierDif.containsNegative(qSet);
            GOTerm gTerm = goTermHelper.getTerm(term);
            ArrayList<GOTerm> ancestors = goTermHelper.getAncestors(gTerm);
            // Positive annotation
            if (false == negAnnot) {
                for (Annotation a : annots) {
                    if (a.isExperimental() && true == QualifierDif.containsNegative(a.getQualifierSet())) {
                        if (term.equals(a.getGoTerm()) || ancestors.contains(goTermHelper.getTerm(a.getGoTerm()))) {
                            errorBuf.insert(0, "Info - " + Evidence.CODE_IBA + " to term " + term + " not propagated to node " + nsi.getPublicId() + " because it has already been annotated with 'NOT' experimental evidence for term " + a.getGoTerm() + " that would negate IBA.\n");
                            return;
                        }
                    }
                }

            } else {
                for (Annotation a : annots) {
                    if (a.isExperimental() && false == QualifierDif.containsNegative(a.getQualifierSet())) {
                        ArrayList<GOTerm> curAncestors = goTermHelper.getAncestors(goTermHelper.getTerm(a.getGoTerm()));
                        if (term.equals(a.getGoTerm()) || curAncestors.contains(gTerm)) {
                            errorBuf.insert(0, "Info - " + Evidence.CODE_IBA + " to term " + term + " not propagated to node " + nsi.getPublicId() + " because it has already been annotated with experimental evidence for term " + a.getGoTerm() + " that would negate IBA.\n");
                            return;
                        }
                    }
                }
            }
        }

        if (null == nvi) {
            nvi = new NodeVariableInfo();
            n.setVariableInfo(nvi);
        }
        Annotation newAnnot = new Annotation();
        nvi.addGOAnnotation(newAnnot);
        modifiedNodeSet.add(n);
        newAnnot.setGoTerm(term);

        AnnotationDetail newDetail = newAnnot.getAnnotationDetail();
        newDetail.setAnnotatedNode(n);
        WithEvidence we = new WithEvidence();
        we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
        we.setEvidenceCode(Evidence.CODE_IBA);
        we.setWith(with);
        newAnnot.addWithEvidence(we);

        // Get inherited qualifiers.  Set with of qualifiers to propagator of annotation
        LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = with.getAnnotationDetail().getQualifierLookup();
        for (Qualifier q : qualifierLookup.keySet()) {
            Qualifier newQ = new Qualifier();
            newQ.setText(q.getText());
            newDetail.addToInheritedQualifierLookup(newQ, with);
            newAnnot.addQualifier(newQ);
        }

        ArrayList<Node> children = nsi.getChildren();
        if (null != children) {
            for (Node child : children) {
                propagateIBA(child, term, qSet, with, withNodeSet, taxonomyHelper, goTermHelper, errorBuf, modifiedNodeSet, addedAnnotSet);
            }
        }

    }

    public static String generateTCVMsg(String term, String publicId, String species) {
        return TCV_MSG_PART_1 + term + TCV_MSG_PART_2 + publicId + TCV_MSG_PART_3 + species + TCV_MSG_PART_4;
    }

    public static AnnotQualifierGroup possibleToAnnotateWithIBD(String term, Node node, StringBuffer errorMsgBuf, TaxonomyHelper taxonomyHelper, GOTermHelper goTermHelper) {
        HashSet<Annotation> expWiths = new HashSet<Annotation>();
        HashSet<String> errorMsgSet = new HashSet<String>();
        ArrayList<Node> nodeList = Node.getAllNonPrunedLeaves(node);
        for (Node leaf : nodeList) {
            NodeVariableInfo nvi = leaf.getVariableInfo();
            if (null == nvi) {
                continue;
            }

            ArrayList<Annotation> leafAnnots = nvi.getGoAnnotationList();
            if (null == leafAnnots) {
                continue;
            }
            for (Annotation leafAnnot : leafAnnots) {
                HashSet<Annotation> possibleWiths = new HashSet<Annotation>();
                String errorMsg = canNodeBeAnnotatedWithIBD(term, leafAnnot.getQualifierSet(), node, possibleWiths, taxonomyHelper, goTermHelper);
                if (null != errorMsg) {
                    errorMsgSet.add(errorMsg);
                    continue;
                }
                expWiths.add(leafAnnot);
            }
        }
        if (false == expWiths.isEmpty()) {
            return new AnnotQualifierGroup(expWiths);
        }

        for (String err : errorMsgSet) {
            errorMsgBuf.append(err);
        }
        return null;
    }
    
    public static String existingAnnotsAllowNewAnnotation(Set<Qualifier> qSet, String term, Node node, GOTermHelper goTermHelper) {
        // Check if annotation is valid given existing annotations in ancestor nodes as well as current node
        boolean negQualifier = QualifierDif.containsNegative(qSet);
        GOTerm cur = goTermHelper.getTerm(term);
        if (null != node.getVariableInfo() && null != node.getVariableInfo().getGoAnnotationList()) {
            ArrayList<Annotation> curAnnotList = node.getVariableInfo().getGoAnnotationList();
            for (Annotation a : curAnnotList) {
                String existing = a.getGoTerm();
                if (term.equals(existing)) {
                    return " to " + node.getStaticInfo().getPublicId() + " for annotation to term " + term + " not allowed, since node is already annotated to the term.\n";
                }
                // If this is a positive annotation, ensure that node has not already been annotated to a more specific term
                if (false == negQualifier) {
                    GOTerm existingGo = goTermHelper.getTerm(existing);
                    ArrayList<GOTerm> ancestors = goTermHelper.getAncestors(existingGo);
                    if (ancestors.contains(cur)) {
                        return " to " + node.getStaticInfo().getPublicId() + " for annotation to term " + term + " not allowed, since node is already annotated to more specific term " + existing + ".\n";
                    }
                    // More specific term is allowed for descendant node
//                    if (curAncestors.contains(existingGo)) {
//                        return Evidence.CODE_IBD + " to " + node.getStaticInfo().getPublicId() + " for annotation to term " + term + " not allowed, since node is already annotated to less specific term " + existing + ".\n";                       
//                    }
                }
            }
        }
        if (false == negQualifier) {
            // Annotation with positive qualifier.  This node is allowed to have a more specific annotation than its ancestor
            List<Node> ancestors = Node.getAncestors(node);
            if (null != ancestors) {
                for (Node ancestor : ancestors) {
                    if (null != ancestor.getVariableInfo() && null != ancestor.getVariableInfo().getGoAnnotationList()) {
                        ArrayList<Annotation> ancesAnnotList = ancestor.getVariableInfo().getGoAnnotationList();
                        for (Annotation a : ancesAnnotList) {
                            if (false == QualifierDif.containsNegative(a.getQualifierSet())) {
                                GOTerm existingGo = goTermHelper.getTerm(a.getGoTerm());
                                ArrayList<GOTerm> ancestorAnnots = goTermHelper.getAncestors(existingGo);
                                if (ancestorAnnots.contains(cur)) {
                                    return Evidence.CODE_IBD + " to " + node.getStaticInfo().getPublicId() + " for annotation to term " + term + " not allowed, since ancestor node " + ancestor.getStaticInfo().getPublicId() + " is already annotated to more specific term " + a.getGoTerm() + ".\n";
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    

    /**
     *
     * @param term
     * @param qSet
     * @param node
     * @param pantherTree
     * @param expWiths - Send in empty HashSet and it will be filled with list
     * of all possible experimental annotations
     * @param taxonomyHelper
     * @param goTermHelper
     * @return
     */
    public static String canNodeBeAnnotatedWithIBD(String term, Set<Qualifier> qSet, Node node, HashSet<Annotation> expWiths, TaxonomyHelper taxonomyHelper, GOTermHelper goTermHelper) {
        // Check for pruned branch
        Node copy = node;
        if (true == inPrunedBranch(copy)) {
            return Evidence.CODE_IBD + " not allowed to node " + node.getStaticInfo().getPublicId() + " in pruned branch.\n";
        }

        if (true == node.getStaticInfo().isLeaf()) {
            return Evidence.CODE_IBD + " not allowed to leaf node " + node.getStaticInfo().getPublicId() + ".\n";
        }

        // Check taxonomy constraints
        if (false == taxonomyHelper.isTermAndQualifierValidForSpecies(term, node.getStaticInfo().getCalculatedSpecies(), qSet)) {
            return Evidence.CODE_IBD + " to " + node.getStaticInfo().getPublicId() + " for annotation to term " + term + " for species " + node.getStaticInfo().getCalculatedSpecies() + " violates taxonomy constraints.\n";

        }
        
        String checkExisting = existingAnnotsAllowNewAnnotation(qSet, term, node, goTermHelper);
        if (null != checkExisting) {
            return Evidence.CODE_IBD + checkExisting;
        }

        // Check for experimental annotations to leaves
        ArrayList<Node> leaves = Node.getAllNonPrunedLeaves(node);
        ArrayList<Annotation> withs = AnnotationHelper.getPossibleExperimentalAnnots(term, qSet, leaves, goTermHelper);
        if (null == withs || withs.isEmpty()) {
            return Evidence.CODE_IBD + " to " + node.getStaticInfo().getPublicId() + " for annotation to term " + term + " does not have associated experimental evidence.\n";
        }
        for (Annotation with : withs) {
            expWiths.add(with);
        }
        return null;
    }

    public static boolean inPrunedBranch(Node node) {
        if (node.getVariableInfo() != null && node.getVariableInfo().isPruned()) {
            return true;
        }
        Node parent = node.getStaticInfo().getParent();
        if (null == parent) {
            return false;
        }
        return inPrunedBranch(parent);
    }

    
    public static void getIBAWithSameTermForNode(Node n, Annotation propagator, HashSet<Annotation> annotSet, String term) {
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi) {
            if (nvi.isPruned()) {
                return;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null != annotList) {
                for (Annotation a : annotList) {
                    if (Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet()) && null != a.getGoTerm() && a.getGoTerm().equals(term)) {
                        HashSet<Annotation> withSet = a.getAnnotationDetail().getWithAnnotSet();
                        if (null != withSet && withSet.contains(propagator)) {
                            annotSet.add(a);
                        }
                    }
                }
            }
        }
        if (null == annotSet) {
            System.out.println("Missing IBA from propagator for node");
        }
        return;
//        if (n == propagator.getAnnotationDetail().getAnnotatedNode()) {
//            return;
//        }
//        Node parent = n.getStaticInfo().getParent();
//        if (null == parent) {
//            return;
//        }
//        getIBAWithSameTermForNode(parent, propagator, annotSet);
    }

    public static void removeAnnotFromNodeAndDescendants(Node n, Annotation remove, String term) {
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi) {
            if (nvi.isPruned()) {
                return;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null != annotList) {
                HashSet<Annotation> removeSet = new HashSet<Annotation>();
                for (Annotation a : annotList) {
                    if (null != a.getGoTerm() && a.getGoTerm().equals(term)) {
                        HashSet<Annotation> withSet = a.getAnnotationDetail().getWithAnnotSet();
                        if (null != withSet && withSet.contains(remove)) {
                            removeSet.add(a);
                        }
                    }
                }
                annotList.removeAll(removeSet);
                if (annotList.isEmpty()) {
                    nvi.setGoAnnotationList(null);
                }
            }
        }
        NodeStaticInfo nsi = n.getStaticInfo();
        if (null != nsi) {
            ArrayList<Node> children = nsi.getChildren();
            if (null != children) {
                for (Node child : children) {
                    removeAnnotFromNodeAndDescendants(child, remove, term);
                }
            }
        }
    }

    public static Annotation getIKRIRDTCVforPropagator(Node n, Annotation propagator) {
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        for (Annotation a : annotList) {
            String evCode = a.getSingleEvidenceCodeFromSet();
            if (false == Evidence.CODE_IKR.equals(evCode) && false == Evidence.CODE_IRD.equals(evCode) && false == Evidence.CODE_TCV.equals(evCode)) {
                continue;
            }

            HashSet<Annotation> withSet = a.getAnnotationDetail().getWithAnnotSet();
            if (null != withSet && withSet.contains(propagator)) {
                if (false == QualifierDif.differenceIsNOTQualifier(a.getQualifierSet(), propagator.getQualifierSet())) {
                    continue;
                }
                return a;
            }
        }
        return null;
    }

    public static Annotation getAssociatedIBAForIKRorIRD(Annotation IKRorIRDAnnotation, Node n) {
        String code = IKRorIRDAnnotation.getSingleEvidenceCodeFromSet();
        if (false == Evidence.CODE_IRD.equals(code) && false == Evidence.CODE_IKR.equals(code)) {
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
        if (null == with) {
            return null;
        }

        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        for (Annotation a : annotList) {
            if (true == Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet())) {
                if (true == a.getAnnotationDetail().getWithAnnotSet().contains(with)) {
                    return a;
                }
            }
        }
        return null;

    }

    public static boolean isDirectAnnotation(Annotation a) {
        String code = a.getSingleEvidenceCodeFromSet();
        if (Evidence.CODE_IBD.equals(code)
                || Evidence.CODE_IRD.equals(code)
                || (Evidence.CODE_TCV.equals(code))) {
            return true;
        }
        if (Evidence.CODE_IKR.equals(code)) {
            // IKR can be experimental leaf or due to IBD.  Ensure it has 2 with annotations (itself and IBD).
            AnnotationDetail ad = a.getAnnotationDetail();
            if (null != ad) {
                HashSet<Annotation> withAnnotSet = ad.getWithAnnotSet();
                if (null != withAnnotSet) {
                    // One of the withs is itself.
                    if (true == withAnnotSet.contains(a)) {
                        for (Annotation with: withAnnotSet) {
                            if (Evidence.CODE_IBD.equals(with.getSingleEvidenceCodeFromSet())) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
        if (Evidence.CODE_IBA.equals(code)) {
            return isIBAForIKRorIRD(a, a.getAnnotationDetail().getAnnotatedNode());
        }
        return false;
    }

    /*
    // This module can be called when the node already has an annotation that is referring to the 'with' annotation.
    // In this case, an IBA should not be created unless it is due to IKR or IRD.  In which case it has to be positive and less specific term
    // NOTE: NOT CHECKING EXISTING IBA's on purpose.  This is checked using IBAValid later in the code.
     */    
    private static boolean isIBABlockedViaIkrIrdTcv(Annotation with, String term, HashSet<Qualifier> qSet, ArrayList<Annotation> goAnnotList, GOTermHelper goTermHelper) {
        if (null == goAnnotList) {
            return true;
        }
        for (Annotation a : goAnnotList) {
            String code = a.getSingleEvidenceCodeFromSet();
            if (true == Evidence.CODE_IKR.equals(code)
                    || true == Evidence.CODE_IRD.equals(code)
                    || true == Evidence.CODE_TCV.equals(code)) {
                HashSet<Annotation> withSet = a.getAnnotationDetail().getWithAnnotSet();
                if (null != withSet && withSet.contains(with)) {
                    if (true == Evidence.CODE_IKR.equals(code) || true == Evidence.CODE_IRD.equals(code)) {
                       if (false == QualifierDif.containsNegative(qSet)) {
                           ArrayList<GOTerm> ancestors = goTermHelper.getAncestors(goTermHelper.getTerm(a.getGoTerm()));
                           if (null != ancestors && ancestors.contains(goTermHelper.getTerm(term))) {
                               continue;
                           }
                       }
                    }
                    System.out.println("IBA not valid since there already exists annotation to " + a.getGoTerm() + " with " + a.getSingleEvidenceCodeFromSet());
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean IBAValid(Annotation with, String term, ArrayList<Annotation> goAnnotList) {
        if (null == goAnnotList) {
            return true;
        }
        for (Annotation a : goAnnotList) {
            String code = a.getSingleEvidenceCodeFromSet();
            if (true == Evidence.CODE_IBA.equals(code)) {
                HashSet<Annotation> withSet = a.getAnnotationDetail().getWithAnnotSet();
                if (null != withSet && withSet.contains(with)) {
                    
                    System.out.println("IBA to " + term + " not valid since there already exists annotation to " + a.getGoTerm() + " with " + a.getSingleEvidenceCodeFromSet());
                    return false;
                }
            }
        }
        return true;
    }    

    public static void fixNodesProvidingEvdnceForIKRIRD(Annotation ikrIrd, Node node, GOTermHelper goTermHelper, HashSet<String> modifiedAnnotSet, StringBuffer errorBuf) {
        ArrayList<Node> leaves = Node.getAllNonPrunedLeaves(node);
        AnnotationDetail ad = ikrIrd.getAnnotationDetail();
        HashSet<Node> origWithSet = ad.getWithNodeSet();
        
        ArrayList<Annotation> withs = AnnotationHelper.getPossibleExperimentalAnnots(ikrIrd.getGoTerm(), ikrIrd.getQualifierSet(), leaves, goTermHelper);
        if (null == withs || withs.isEmpty()) {
            ad.setWithEvidenceNodeSet(null);
        } else {
            HashSet<Node> includeSet = new HashSet<Node>();
            for (Annotation with : withs) {
                includeSet.add(with.getAnnotationDetail().getAnnotatedNode());
            }

            HashSet<WithEvidence> removeEvSet = new HashSet<WithEvidence>();
            if (null != origWithSet) {
                for (WithEvidence we : ad.getWithEvidenceNodeSet()) {
                    Node with = (Node) we.getWith();
                    if (false == leaves.contains(with)) {
                        errorBuf.insert(0, ikrIrd.getSingleEvidenceCodeFromSet() + " for term " + ikrIrd.getGoTerm() + " to " + node.getStaticInfo().getPublicId() + " contains invalid node " + with.getStaticInfo().getPublicId() + " as evidence.\n");
                        removeEvSet.add(we);
                    } else {
                        includeSet.remove(with);
                    }
                }
            }
            for (Node include : includeSet) {
                WithEvidence we = new WithEvidence();
                we.setEvidenceCode(ikrIrd.getSingleEvidenceCodeFromSet());
                we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_REF);
                we.setWith(include);
                ad.addWithEvidence(we);
            }
        }
        HashSet<Node> newNodeSet = ad.getWithNodeSet();
        if (null != origWithSet && null != newNodeSet) {
            if (false == origWithSet.equals(newNodeSet)) {
                modifiedAnnotSet.add(ikrIrd.getAnnotationId());
                errorBuf.insert(0, ikrIrd.getSingleEvidenceCodeFromSet() + " for term " + ikrIrd.getGoTerm() + " to " + node.getStaticInfo().getPublicId() + " - Modified list of nodes  providing evidence.\n");

            }
        }
        else if ((null != origWithSet && null == newNodeSet) || (null == origWithSet && null != newNodeSet)) {
            modifiedAnnotSet.add(ikrIrd.getAnnotationId());
            errorBuf.insert(0, String.join(STR_COMMA, ikrIrd.getEvidenceCodeSet()) + " for term " + ikrIrd.getGoTerm() + " to " + node.getStaticInfo().getPublicId() + " - Modified list of nodes  providing evidence.\n");
        }
    }
    /**
     * Not only do annotations have to be restored after graft or prune operations, but also when annotations are deleted.  IBA's etc have to be propagated to
     * descendant nodes
     * @param n
     * @param graftPruneNode
     * @param taxonHelper
     * @param gth 
     */
    public static void fixAnnotationsForGraftPruneOperation(Node n, Node graftPruneNode, TaxonomyHelper taxonHelper, GOTermHelper gth) {
        NodeVariableInfo nvi = n.getVariableInfo();

        if (null != nvi) {
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null != annotList) {
                ArrayList<Annotation> annotCopy = (ArrayList<Annotation>) annotList.clone(); // Make a copy and go through this list, since, other subroutines may delete some of the annotations                
                for (Annotation a : annotCopy) {
                    if (true == Evidence.CODE_IBD.equals(a.getSingleEvidenceCodeFromSet())) {
                        String term = a.getGoTerm();
                        HashSet<Qualifier> qSet = a.getQualifierSet();
                        ArrayList<Node> curLeaves = n.getAllNonPrunedLeaves(n);
                        ArrayList<Annotation> newWiths = AnnotationHelper.getPossibleExperimentalAnnots(term, qSet, curLeaves, gth);
                        if (newWiths.isEmpty()) {
                            deleteAnnotation(a, taxonHelper, gth);
                            continue;
                        } else {
                            AnnotationDetail ad = a.getAnnotationDetail();
                            ad.setWithEvidenceAnnotSet(null);
                            ad.getInheritedQualifierLookup().clear();
                            for (Annotation newAnnot : newWiths) {
                                WithEvidence we = new WithEvidence();
                                we.setEvidenceCode(Evidence.CODE_IBD);
                                we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_EXP);
                                we.setWith(newAnnot);
                                a.getAnnotationDetail().addWithEvidence(we);

                                if (null != qSet && null != newAnnot.getQualifierSet() && QualifierDif.contains(qSet, newAnnot.getQualifierSet())) {
                                    for (Qualifier q : newAnnot.getQualifierSet()) {
                                        a.getAnnotationDetail().addToInheritedQualifierLookup(q, newAnnot);
                                    }
                                }
                            }
                            propagateIBD(a, taxonHelper, gth, new StringBuffer(), new HashSet<Node>(), new HashSet<Annotation>());
                        }
                    } else if (true == Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet())) {
                        Annotation ibdPropagator = AnnotationHelper.getIBDpropagator(a);
                        // Propagate IBA's
                        // First get list of nodes that provided evidence for original IBD annotation.  These do not get the IBA annotation
                        AnnotationDetail ibdDetail = ibdPropagator.getAnnotationDetail();
                        HashSet<Annotation> withSet = ibdDetail.getWithAnnotSet();
                        HashSet<Node> evidenceNodes = new HashSet<Node>();
                        for (Annotation with : withSet) {
                            evidenceNodes.add(with.getAnnotationDetail().getAnnotatedNode());
                        }
                        ArrayList<Node> children = n.getStaticInfo().getChildren();
                        if (null != children) {
                            for (Node child : children) {
                                propagateIBA(child, a.getGoTerm(), a.getQualifierSet(), a, evidenceNodes, taxonHelper, gth, new StringBuffer(), new HashSet<Node>(), new HashSet<Annotation>());
                            }
                        }

                        // Fix list of nodes providing evidence for annotation - Just in case graft/prune operation changed something
                        AnnotationHelper.fixNodesProvidingEvdnceForIKRIRD(a, n, gth, new HashSet<String>(), new StringBuffer());
                    } else if (true == Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet())) {
                        // Fix list of nodes providing evidence for annotation - Just in case graft/prune operation changed something
                        AnnotationHelper.fixNodesProvidingEvdnceForIKRIRD(a, n, gth, new HashSet<String>(), new StringBuffer());
                    } else if (true == Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet())) {
                        // If this IBA was created as a result of IKR or IRD
                        if (true == AnnotationHelper.isDirectAnnotation(a)) {
                            // Propagate IBA's
                            // First get list of nodes that provided evidence for original IBD annotation.  These do not get the IBA annotation
                            Annotation ibdPropagator = AnnotationHelper.getIBDpropagator(a);
                            AnnotationDetail ibdDetail = ibdPropagator.getAnnotationDetail();
                            HashSet<Annotation> withSet = ibdDetail.getWithAnnotSet();
                            HashSet<Node> evidenceNodes = new HashSet<Node>();
                            for (Annotation with : withSet) {
                                evidenceNodes.add(with.getAnnotationDetail().getAnnotatedNode());
                            }
                            ArrayList<Node> children = n.getStaticInfo().getChildren();
                            if (null != children) {
                                for (Node child : children) {
                                    propagateIBA(child, a.getGoTerm(), a.getQualifierSet(), ibdPropagator, evidenceNodes, taxonHelper, gth, new StringBuffer(), new HashSet<Node>(), new HashSet<Annotation>());
                                }
                            }
                        }
                    }
                    // Else TCV - already handled by IBA annotation propagation
                }
            }
        }

        if (n == graftPruneNode) {
            return;
        }
        ArrayList<Node> children = n.getStaticInfo().getChildren();
        if (null != children) {
            for (Node child : children) {
                fixAnnotationsForGraftPruneOperation(child, graftPruneNode, taxonHelper, gth);
            }
        }
    }
    
    public static void deletePropagatedIBA(Annotation iba, TaxonomyHelper taxonHelper, GOTermHelper gth) {
        Node n = iba.getAnnotationDetail().getAnnotatedNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi && false == nvi.isPruned()) {
            Annotation propagator = getPropagator(iba);
            ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
            if (null == goAnnotList || false == goAnnotList.contains(iba)) {
                return;
            }
            
            deleteAnnotation(iba, n);
            
            NodeStaticInfo nsi = n.getStaticInfo();
            ArrayList<Node> children = nsi.getChildren();
            if (null != children) {
                for (Node child : children) {
                    NodeVariableInfo childNvi = child.getVariableInfo();
                    if (null == childNvi || nvi.isPruned()) {
                        continue;
                    }
                    ArrayList<Annotation> childAnnotList = childNvi.getGoAnnotationList();
                    if (null == childAnnotList) {
                        continue;
                    }
                    ArrayList<Annotation> deleteDirect = new ArrayList<Annotation>();
                    ArrayList<Annotation> deleteIba = new ArrayList<Annotation>();
                    for (Annotation childAnnot: childAnnotList) {
                        Annotation childProp = getPropagator(childAnnot);
                        boolean isDirect = isDirectAnnotation(childAnnot);
                        if (childProp == propagator && true == isDirect) {
                            deleteDirect.add(childAnnot);
                        }
                        else if (Evidence.CODE_IBA.equals(childAnnot.getSingleEvidenceCodeFromSet()) &&
                                childProp == propagator && false == isDirect &&
                                iba.getGoTerm().equals(childAnnot.getGoTerm())) {
                            deleteIba.add(childAnnot);
                        }
                    }
                    for (Annotation delete: deleteIba) {
                        deletePropagatedIBA(delete, taxonHelper, gth);
                    }
                    for (Annotation delete: deleteDirect) {
                        deleteAnnotation(delete, taxonHelper, gth);
                    }
                    
                }
            }

        }
    }
    
    public static void deleteAnnotationAndRepropagate(Annotation a, TaxonomyHelper taxonHelper, GOTermHelper gth) {
        Annotation propAnnot = getPropagator(a);
        deleteAnnotation(a, taxonHelper, gth);
        // When deleting annotations, it is necessary to re-propagate IBA's that may  have been deleted
        if (false == Evidence.CODE_IBD.equals(a.getSingleEvidenceCodeFromSet())) {
            Node n = a.getAnnotationDetail().getAnnotatedNode();
            Node propNode = propAnnot.getAnnotationDetail().getAnnotatedNode();
            fixAnnotationsForGraftPruneOperation(propNode, n, taxonHelper, gth);
        }
    }

    /*
    // Before deleting, delete annotations that are dependant on the annotation itself. First delete direct annotations followed by others.
    // For IBD, delete IKR, IRD's, TCV's followed by propagated IBA's, lastly IBD
    // For IKR, delete ancestor IBA's, TCV's from ancestor IBA's, TCV's from propagation of IBA, propagated IBAs from IKR, lastly IKR
    // For IRD, delete ancestor IBA's, TCV's from ancestor IBA's, lastly IRD
    // For Ancestor IBA's delete dependant IKR, IRD.... followed by propagated IBA's, IBA itself
    // For TCV, just delete TCV
     */
    private static void deleteAnnotation(Annotation a, TaxonomyHelper taxonHelper, GOTermHelper gth) {
        System.out.println("Attempting to remove " + a.getSingleEvidenceCodeFromSet() + " to term " + a.getGoTerm() + " from " + a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
        Node n = a.getAnnotationDetail().getAnnotatedNode();
        String code = a.getSingleEvidenceCodeFromSet();
        if (Evidence.CODE_IBD.equals(code)) {
            // Delete dependant annotations
            ArrayList<Annotation> directDepAnnotSet = getDirectDependantAnnots(a, n);
            for (Annotation dep : directDepAnnotSet) {
                deleteAnnotation(dep, taxonHelper, gth);
            }
            deletePropagatorFromAll(a, a.getGoTerm(), n);
            deleteAnnotation(a, n);
        } else if (Evidence.CODE_IKR.equals(code)) {
            // Delete dependant annotations
            ArrayList<Annotation> directDepAnnotSet = getDirectDependantAnnots(a, n);         
            for (Annotation dep : directDepAnnotSet) {
                deleteAnnotation(dep, taxonHelper, gth);
            }
            deletePropagatorFromAll(a, a.getGoTerm(), n);
            deleteAnnotation(a, n);            
        } else if (Evidence.CODE_IRD.equals(code)) {
            // Delete dependant annotations
            ArrayList<Annotation> directDepAnnotSet = getDirectDependantAnnots(a, n);          
            for (Annotation dep : directDepAnnotSet) {
                deleteAnnotation(dep, taxonHelper, gth);
            }
            deleteAnnotation(a, n);         
        } else if (Evidence.CODE_TCV.equals(code)) {
            deleteAnnotation(a, n);
        } else if (Evidence.CODE_IBA.equals(code) && isDirectAnnotation(a)) {
            // IBA
            Annotation propAnnot = getPropagator(a);
            // Delete dependant annotations
            ArrayList<Annotation> directDepAnnotSet = getDirectDependantAnnots(a, n);            
            for (Annotation dep : directDepAnnotSet) {
                deleteAnnotation(dep, taxonHelper, gth);
            }
            deletePropagatorFromAll(propAnnot, a.getGoTerm(), n);       // IBA is not propagator, it is IBD 
            deleteAnnotation(a, n);
        }else {
            System.out.println("Unable to handle delete of " + code + " to node " + n.getStaticInfo().getPublicId());
            return;
        }
    }

    /**
     * 
     * @param propagator
     * @param n
     * @return Ordered list of direct dependent annotations 
     */
    public static ArrayList<Annotation> getDirectDependantAnnots(Annotation propagator, Node n) {

        ArrayList<Annotation> directDepAnnots = new ArrayList<Annotation>();
        String code = propagator.getSingleEvidenceCodeFromSet();
        System.out.println("Getting direct dependant annots for " + n.getStaticInfo().getPublicId() + " with code " + code + " for term " + propagator.getGoTerm());        

        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi && null != nvi.getGoAnnotationList() && false == nvi.isPruned()) {
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            for (Annotation annot : annotList) {
                if (false == isDirectAnnotation(annot)) {
                    continue;
                }
                String annotCode = annot.getSingleEvidenceCodeFromSet();
                if (Evidence.CODE_IBD.equals(code)) {
                    HashSet<Annotation> withSet = annot.getAnnotationDetail().getWithAnnotSet();
                    // IKR , IRD and TCV for IBD
                   
                    if (null != withSet && withSet.contains(propagator) && propagator.getGoTerm().equals(annot.getGoTerm()) &&
                            (Evidence.CODE_IKR.equals(annotCode) || Evidence.CODE_IRD.equals(annotCode) || Evidence.CODE_TCV.equals(annotCode))) {
                         directDepAnnots.add(annot);
                         continue;
                    }
                }
                if ((Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code))) {
                    if (false == Evidence.CODE_IBA.equals(annotCode)) {
                        continue;
                    }
                    Annotation iba = getAssociatedIBAForIKRorIRD(propagator, n);
                    if (annot == iba) {
                        directDepAnnots.add(annot);
                        continue;
                    } 
                }
                if (Evidence.CODE_IBA.equals(code)) {
                    // IBA can be negated by ikr, ird or tcv.  Find it
                    if ((Evidence.CODE_IKR.equals(annotCode) || Evidence.CODE_IRD.equals(annotCode) || Evidence.CODE_TCV.equals(annotCode))) {
                        Annotation ibdPropagator = AnnotationHelper.getPropagator(propagator);
                        Annotation ikrIrdTcv = AnnotationHelper.getIKRIRDTCVforPropagator(n, ibdPropagator);
                        if (annot == ikrIrdTcv && propagator.getGoTerm().equals(annot.getGoTerm())) {
                            directDepAnnots.add(annot);
                            continue;
                        }
                    }
                }

            }
        }

        
        NodeStaticInfo nsi = n.getStaticInfo();
        ArrayList<Node> children = nsi.getChildren();
        if (null != children) {
            for (Node child : children) {
                ArrayList<Annotation> childAnnotList = getDirectDependantAnnots(propagator, child);
                if (null != childAnnotList) {
                    directDepAnnots.addAll(childAnnotList);
                } 
            }
        }
        return directDepAnnots;
    }

    private static void deletePropagatorFromAll(Annotation propagator, String term, Node n) {
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi && null != nvi.getGoAnnotationList() && false == nvi.isPruned()) {
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            for (Iterator<Annotation> annotIter = annotList.iterator(); annotIter.hasNext();) {
                Annotation a = annotIter.next();
                if (false == a.getGoTerm().equals(term)) {
                    continue;
                }
                HashSet<Annotation> withSet = a.getAnnotationDetail().getWithAnnotSet();
                if (null != withSet && withSet.contains(propagator)) {
                    System.out.println("Removing " + a.getSingleEvidenceCodeFromSet() + " to term " + a.getGoTerm() + " from " + a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
                    annotIter.remove();
                }
            }
            if (annotList.isEmpty()) {
                nvi.setGoAnnotationList(null);
            }
        }
        ArrayList<Node> children = n.getStaticInfo().getChildren();
        if (null != children) {
            for (Node child : children) {
                deletePropagatorFromAll(propagator, term, child);
            }
        }
    }

    private static boolean deleteAnnotation(Annotation a, Node n) {
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi && null != nvi.getGoAnnotationList()) {
            boolean success = nvi.getGoAnnotationList().remove(a);
            if (true == success) {
                System.out.println("Removing " + a.getSingleEvidenceCodeFromSet() + " to term " + a.getGoTerm() + " from " + a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
            }
            if (nvi.getGoAnnotationList().isEmpty()) {
                nvi.setGoAnnotationList(null);
            }
            return success;
        }
        return false;
    }

}

//                else if (Evidence.CODE_IBA.equals(code)) {
//                    // Only dependant annotation can be an IKR or IRD
//                    Annotation ibaPropagator = AnnotationHelper.getPropagator(propagator);
//                    Annotation ikrird = getIKRIRDTCVforPropagator(n, ibaPropagator);
//                    if (null != ikrird && ikrird.getGoTerm().equals(propagator.getGoTerm())) {
//                        directDepAnnots.add(annot);
//                    }
//                }

                    
//                    }
//                    continue;
//                    
//                }
//                else if ((Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code)) && propagator.getGoTerm().equals(annot.getGoTerm())) {
//                    directDepAnnots.add(annot);
//                    Annotation iba = getAssociatedIBAForIKRorIRD(propagator, n);
//                    if (null != iba) {
//                        directDepAnnots.add(iba);
//                    }
//                    continue;
//                }
//                else if (Evidence.CODE_IBA.equals(code) && propagator.getGoTerm().equals(annot.getGoTerm())) {
//                    // IBA can only be created with an IKR or IRD
//                    Annotation ibdPropagator = AnnotationHelper.getPropagator(annot);
//                    Annotation ikrIrd = AnnotationHelper.getIKRIRDTCVforPropagator(n, ibdPropagator);
//                    if (null != ikrIrd && ikrIrd.getGoTerm().equals(annot.getGoTerm())) {
//                        Annotation ikrirdPropagator = getPropagator(propagator);
//                        if (ibdPropagator == ikrirdPropagator) {
//                            directDepAnnots.add(annot);
//                        }
//                    }
//                }