/**
 *  Copyright 2018 University Of Southern California
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author muruganu
 */
public class AnnotationHelper implements Serializable{

    public final static String KEY_RESIDUES_EC = "IKR";

    public final static String DIVERGENT_EC = "IRD";

    public final static String DESCENDANT_SEQUENCES_EC = "IBD"; // was IDS
    public static final String ANCESTRAL_EVIDENCE_CODE = "IBA"; // was IAS
    public final static String PAINT_REF = "PAINT_REF";

    public static void propagateAndFixAnnotationsForBookOpen(String familyAcc, AnnotationNode root, Hashtable<String, AnnotationNode> nodeLookupTbl) {
        HashSet<Annotation> removedAnnots = new HashSet<Annotation>();
        // This will just remove the annotations from the nodes, does not consider
        // annotations that may have these removed annotations as withs (i.e. dependencies)        
        removeNonExperimentalNonPaintAnnotations(root, removedAnnots);

        // Other dependent annotations may get removed because of removing the above annotations
        HashSet<Annotation> newRemovedAnnotSet = new HashSet<Annotation>();
        do {
            newRemovedAnnotSet.clear();
            handleRemovedWithAnnots(root, removedAnnots, newRemovedAnnotSet);
            removedAnnots = (HashSet<Annotation>) newRemovedAnnotSet.clone();
        } while (0 != newRemovedAnnotSet.size());

        if (root.isPruned()) {
            return;
        }
        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
        // First go through all the IBA's that were created when IKR or IRD was created and make a list.
        allNonPrunedDescendents(root, descendents);
        descendents.add(0, root);
        ArrayList<Annotation> IBAannots = existingIBAannots(descendents);
        propagateAnnotationsForBookOpen(familyAcc, root, IBAannots, nodeLookupTbl);
    }

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
                            } else if (Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet()) || Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet())) {
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

    private static ArrayList<Annotation> existingIBAannots(ArrayList<AnnotationNode> descendents) {
        ArrayList<Annotation> ibaAnnots = new ArrayList<Annotation>();
        for (AnnotationNode gn : descendents) {
            Node n = gn.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            ArrayList<Annotation> removeAnnot = new ArrayList<Annotation>();    // If we find an IBA without 'matching' IKR or 'IRD' remove since this is invalid data
            for (Annotation a : annotList) {
                if (false == ANCESTRAL_EVIDENCE_CODE.equals(a.getSingleEvidenceCodeFromSet())) {
                    continue;
                }
//                if (true == "38109929".equals(a.getAnnotationId())) {
//                    System.out.println("Here");
//                }
                // Have IBA without propagator
                Annotation propagatorAnnot = null;
                if (null == a.getAnnotationDetail().getWithAnnotSet()) {
//                    System.out.println("Annotation does not have withs for " + a.getAnnotationId());
                    removeAnnot.add(a);
                    continue;
                }
                for (Annotation prop : a.getAnnotationDetail().getWithAnnotSet()) {
                    propagatorAnnot = prop;
                    break;
                }
                if (null == propagatorAnnot) {
                    removeAnnot.add(a);
                    continue;
                }
                boolean found = false;
                for (Annotation compAnnot : annotList) {
                    if (a.equals(compAnnot)) {
                        continue;
                    }
                    if (true == DIVERGENT_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())
                            || true == KEY_RESIDUES_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())
                            || true == DESCENDANT_SEQUENCES_EC.equals(compAnnot.getSingleEvidenceCodeFromSet())) {
                        Annotation compPropagator = null;
                        for (Annotation compProp : compAnnot.getAnnotationDetail().getWithAnnotSet()) {
                            if (compAnnot == compProp) {
                                continue;
                            }
                            compPropagator = compProp;
                            break;
                        }
                        if (true == propagatorAnnot.equals(compPropagator)) {
                            ibaAnnots.add(a);
                            found = true;
                            break;
                        }
                    }
                }
                if (false == found) {
                    removeAnnot.add(a);
                }
            }
            if (false == removeAnnot.isEmpty()) {
                //JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Removed IBA annotation(s) from" + n.getStaticInfo().getPublicId(), "Warning", JOptionPane.WARNING_MESSAGE);
                annotList.removeAll(removeAnnot);
                if (annotList.isEmpty()) {
                    nvi.setGoAnnotationList(null);
                }
            }
        }
        return ibaAnnots;
    }

    private static void propagateAnnotationsForBookOpen(String familyAcc, AnnotationNode gNode, ArrayList<Annotation> initialIBAannots, Hashtable<String, AnnotationNode> nodeLookupTbl) {
        Node n = gNode.getNode();
//        System.out.println("Processing node " + n.getStaticInfo().getNodeAcc() + " public id " + n.getStaticInfo().getPublicId());
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi && null != nvi.getGoAnnotationList()) {
            ArrayList<Annotation> annotList = (ArrayList<Annotation>) nvi.getGoAnnotationList().clone();     // Clone since annotation list gets modified within loop
            if (null != annotList) {
//                System.out.println("Node has " + annotList.size() + " annotations");
                for (int i = 0; i < annotList.size(); i++) {
                    Annotation a = annotList.get(i);
//                    System.out.println("Processing " + i + " annotation");
                    //Evidence e = a.getEvidence();
//                    if (true == Evidence.CODE_IBA.equals(e.getEvidenceCode()) && false == gNode.isLeaf()) {
//                        System.out.println("Here");
//                    }
                    if (true == Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet()) && true == initialIBAannots.contains(a)) {
                        Annotation ibdProp = getIBDpropagator(a);
                        Annotation propagator = getPropagator(a);
                        if (null == ibdProp || null == propagator) {
                            continue;
                        }
                        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
                        allNonPrunedDescendents(gNode, descendents);
                        HashSet<AnnotationNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(ibdProp, nodeLookupTbl);
                        if (null != nodesProvidingEvidence) {
                            descendents.removeAll(nodesProvidingEvidence);
                        }
                        propagateIBAtoNodes(propagator, descendents, a.getGoTerm());
                    }
                    if (true == Evidence.CODE_IBD.equals(a.getSingleEvidenceCodeFromSet())) {
                        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
                        allNonPrunedDescendents(gNode, descendents);
                        HashSet<AnnotationNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(a, nodeLookupTbl);
                        if (null != nodesProvidingEvidence) {
                            descendents.removeAll(nodesProvidingEvidence);
                        }
                        propagateIBAtoNodes(a, descendents, a.getGoTerm());
                        continue;
                    }
                    if (true == Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet()) || true == Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet())) {
                        // remove IBA's propagated from IBD
                        Annotation ibdProp = getIBDpropagator(a);
                        Annotation propagator = getPropagator(a);
                        if (null == ibdProp) {
                            System.out.println("Did not find Annotation that was cause of IRD or IRK for " + n.getStaticInfo().getPublicId());
                            continue;
                        }
                        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
                        allNonPrunedDescendents(gNode, descendents);
                        HashSet<AnnotationNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(ibdProp, nodeLookupTbl);
                        if (null != nodesProvidingEvidence) {
                            descendents.removeAll(nodesProvidingEvidence);
                        }
                        descendents.add(gNode);     // Add myself since, IBA has to be removed
                        for (AnnotationNode descNode : descendents) {
                            Node dn = descNode.getNode();
                            NodeVariableInfo dvni = dn.getVariableInfo();
                            if (null == dvni) {
                                System.out.println("Did not find variable IBA info for node " + dn.getStaticInfo().getPublicId());
                                continue;
                            }
                            ArrayList<Annotation> dAnnotList = dvni.getGoAnnotationList();
                            if (null == dAnnotList) {
                                System.out.println("Did not find IBA  annotation for node " + dn.getStaticInfo().getPublicId());
                                continue;
                            }
                            for (Iterator<Annotation> annotIter = dAnnotList.iterator(); annotIter.hasNext();) {
                                Annotation descAnnot = annotIter.next();
                                if (false == Evidence.CODE_IBA.equals(descAnnot.getSingleEvidenceCodeFromSet())) {
                                    continue;
                                }
                                if (false == a.getGoTerm().equals(descAnnot.getGoTerm())) {
                                    continue;
                                }
                                Annotation propagatorAnnot = null;
                                if (null != descAnnot.getAnnotationDetail().getWithAnnotSet()) {
                                    for (Annotation prop : descAnnot.getAnnotationDetail().getWithAnnotSet()) {
                                        propagatorAnnot = prop;
                                        if (prop == descAnnot) {
                                            propagatorAnnot = null;
                                            continue;
                                        }
                                        break;
                                    }
                                }
                                if (true == propagator.equals(propagatorAnnot)) {
                                    annotIter.remove();
                                }
                            }
                            if (dAnnotList.isEmpty()) {
                                dvni.setGoAnnotationList(null);
                            }
                        }

                        // For IKR, need to propagate to descendents
                        descendents.remove(gNode);      // Remove myself since node being processed does not get IBA
                        if (true == KEY_RESIDUES_EC.equals(a.getSingleEvidenceCodeFromSet())) {
                            propagateIBAtoNodes(a, descendents, a.getGoTerm());
                        }

//                        // Check for annotation to ancestor term and propagate
//                        Annotation childAnnot = a.getChildAnnotation();
//                        if (null != childAnnot && initialIBAannots.contains(childAnnot)) {
////                            Annotation propagatorAnnot = null;
////                            for (Annotation prop : childAnnot.getAnnotationDetail().getWithAnnotSet()) {
////                                propagatorAnnot = prop;
////                                break;
////                            }
////                            if (true == ibdProp.equals(propagatorAnnot)) {
//                                propagateIBAforIKRandIRD(childAnnot, descendents);
////                            }
//                        }
                    }
                }
            }
        } else {
//            System.out.println("No annotations");
        }

        // Iterate over children
        List<AnnotationNode> children = gNode.getChildren();
        if (null == children) {
            return;
        }
        for (AnnotationNode child : children) {
            if (child.isPruned()) {
                continue;
            }
            propagateAnnotationsForBookOpen(familyAcc, child, initialIBAannots, nodeLookupTbl);
        }
    }

    private static HashSet<AnnotationNode> getEvidenceNodesForAnnotation(Annotation a, Hashtable<String, AnnotationNode> nodeLookupTbl) {
        AnnotationDetail ad = a.getAnnotationDetail();
        HashSet<WithEvidence> withAnnotSet = ad.getWithEvidenceAnnotSet();
        if (null == withAnnotSet) {
            return null;
        }
        HashSet<AnnotationNode> withNodes = new HashSet<AnnotationNode>();

        for (WithEvidence we : withAnnotSet) {
            Annotation annot = (Annotation) we.getWith();
            Node n = annot.getAnnotationDetail().getAnnotatedNode();
            AnnotationNode with = nodeLookupTbl.get(n.getStaticInfo().getNodeAcc());
            withNodes.add(with);
        }
        return withNodes;
    }

    private static Annotation getIBDpropagator(Annotation a) {
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
            return getIBDpropagator(withAnnot);
        }
        return null;
    }

    private static Annotation getPropagator(Annotation a) {
        HashSet<Annotation> withs = a.getAnnotationDetail().getWithAnnotSet();
        if (null == withs) {
            return null;
        }
        for (Annotation withAnnot : withs) {
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
    private static void propagateIBAtoNodes(Annotation propagator, ArrayList<AnnotationNode> nodeList, String term) {
        for (AnnotationNode gn : nodeList) {
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
//            Evidence newEvidence = new Evidence();
////            newAnnot.setEvidence(newEvidence);
//            newEvidence.setEvidenceCode(ANCESTRAL_EVIDENCE_CODE);
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(PAINT_REF);
//            dbRef.setEvidenceValue(familyAcc);
//            newEvidence.addDbRef(dbRef);

            // Get inherited qualifiers.  Set with of qualifiers to propagator of annotation
            LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = propagator.getAnnotationDetail().getQualifierLookup();
            for (Qualifier q : qualifierLookup.keySet()) {
                Qualifier newQ = new Qualifier();
                newQ.setText(q.getText());
                newDetail.addToInheritedQualifierLookup(newQ, propagator);
                newAnnot.addQualifier(newQ);
            }

//            LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = a.getAnnotationDetail().getQualifierLookup();
//            Collection<HashSet<Annotation>> withList = qualifierLookup.values();
//            for (HashSet<Annotation> cur : withList) {
//                cur.clear();
//                cur.add(a);
//            }
//            newDetail.setInheritedQualifierLookup(qualifierLookup);
        }
    }

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
        for (Qualifier q: qSet) {
            if (false == gth.isQualifierValidForTerm(term, q)) {
                return false;
            }
        }
        return true;
    }

    // Return if a given term and associted qualifiers can be annotated using a with term and associated qualifiers
    public static boolean canCreateIBDAnnotUsingWith(GOTerm toTerm, Set<Qualifier> toQualifierSet, Annotation withAnnot, GOTermHelper gth) {
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

    public static ArrayList<Annotation> getPossibleAnnotsForIBD(String term, Set<Qualifier> qSet, ArrayList<Node> leaves, GOTermHelper gth) {
        if (null == term || null == leaves) {
            return null;
        }
//        if (true == "GO:0015179".equals(term)) {
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
        return annots;
    }
    
    public static String checkValidity(ArrayList<Annotation> annotList, GOTermHelper gth) {
        if (null == annotList) {
            return null;
        }
        for (Annotation a: annotList) {
            String err = checkValidity(a, gth);
            if (null != err) 
                return err;
        }
        return null;
    }
    

    public static final String ERROR_CDE_UNKNOWN_EVIDENCE_TYPE = "Unknown evidence type";
    public static final String ERROR_CDE_NO_ANNOTATION_DETAIL = "No annotation detail found";
    public static final String ERROR_CDE_INVALID_TERM = "Invalid term";
    public static final String ERROR_CDE_CANNOT_CREATE_IBD_USING_WITH = "Found invalid with annotation for IBD using with id ";
    public static final String ERROR_CDE_NO_WITH_FOUND = "No with found for annotation";
    public static final String ERROR_CDE_NO_IBD_PROP = "NO IBD propagator for IKR/IRD annotation";
    public static final String ERROR_CDE_IKR_IRD_ANNOT_QUALIFIER_NOT_OPPOSITE_IBD = "IKR/IRD annotation qualifier not opposite of IBD";
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
            HashSet<Qualifier> qSet = a.getQualifierSet();
            String evCde = a.getSingleEvidenceCodeFromSet();
            if (Evidence.CODE_IBD.equals(evCde)) {            
                if (null == ad) {
                    return ERROR_CDE_NO_ANNOTATION_DETAIL;
                }
                HashSet<Annotation> withs = ad.getWithAnnotSet();
                if (null == withs || 0 == withs.size()) {
                    return ERROR_CDE_NO_WITH_FOUND;
                }
                for (Annotation with: withs) {
                    if (false == canCreateIBDAnnotUsingWith(goTerm, qSet, with, gth)) {
                        return ERROR_CDE_CANNOT_CREATE_IBD_USING_WITH + with.getAnnotationId();
                    }
                }
            }
            else if (Evidence.CODE_IKR.equals(evCde) || Evidence.CODE_IRD.equals(evCde)) {
                HashSet<Annotation> withs = ad.getWithAnnotSet();
                if (null == withs || 0 == withs.size()) {
                    return ERROR_CDE_NO_WITH_FOUND;
                }
                Annotation IBDPropagator = getIBDpropagator(a);
                if (null == IBDPropagator) {
                    return ERROR_CDE_NO_IBD_PROP;
                }

                if (false == QualifierDif.areOpposite(IBDPropagator.getQualifierSet(), qSet)) {
                    return ERROR_CDE_IKR_IRD_ANNOT_QUALIFIER_NOT_OPPOSITE_IBD;
                }                
            }
            else if (Evidence.CODE_IBA.equals(evCde)) {
                Annotation propagator  = getPropagator(a);
                if (null == propagator) {
                    return ERROR_CDE_IBA_NO_PROPAGATOR;
                }
            }
            else {
                return ERROR_CDE_UNKNOWN_EVIDENCE_TYPE;
            }
            return null;
    }    
    

}
