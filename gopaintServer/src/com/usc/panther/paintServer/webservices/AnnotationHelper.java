/**
 *  Copyright 2017 University Of Southern California
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
package com.usc.panther.paintServer.webservices;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author muruganu
 */
public class AnnotationHelper {
    
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
            removedAnnots = (HashSet<Annotation>)newRemovedAnnotSet.clone();
        }
        while(0 != newRemovedAnnotSet.size());
        
        
        if (root.isPruned()) {
            return;
        }
        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
        // First go through all the IBA's that were created when IKR or IRD was created and make a list.
        allNonPrunedDescendents(root, descendents);
        
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
                for (Annotation a: annotList) {
                    Evidence e = a.getEvidence();
                    if (true == e.isExperimental() || true == e.isPaint()) {
                        continue;
                    }
                    //System.out.println("Removed annotation " + a.getGoTerm() + " from node " + gNode.getNode().getStaticInfo().getPublicId());
                    removeList.add(a);
                }
                removedAnnotSet.addAll(removeList);
                for (Annotation a: removeList) {
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
       for (AnnotationNode child: children) {
           removeNonExperimentalNonPaintAnnotations(child, removedAnnotSet);
       }
    }    
    
    
    private static void handleRemovedWithAnnots(AnnotationNode gNode, HashSet<Annotation> curRemovedAnnotSet, HashSet<Annotation> newRemovedAnnotSet) {
        NodeVariableInfo nvi = gNode.getNode().getVariableInfo();
        if (null != nvi) {
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null != annotList) {
                for (Annotation a: annotList) {
                   for (Annotation removedAnnot: curRemovedAnnotSet) {
                       Annotation ra = a.removeWith(removedAnnot);
                       if (null != ra) {
                           HashSet<Annotation> remainingWiths = a.getAnnotationDetail().getWithAnnotSet();
                           if (null == remainingWiths || true == remainingWiths.isEmpty()) {
                               newRemovedAnnotSet.add(a);
                           }
                           else if (Evidence.CODE_IKR.equals(a.getEvidence().getEvidenceCode()) || Evidence.CODE_IRD.equals(a.getEvidence().getEvidenceCode())) {
                               newRemovedAnnotSet.add(a);
                           }
                       }
                   }
                }
            }
        }
        List<AnnotationNode> children = gNode.getChildren();
        if (null !=  children) {
            for (AnnotationNode child: children) {
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
            for (AnnotationNode child: children) {
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
                if (false == ANCESTRAL_EVIDENCE_CODE.equals(a.getEvidence().getEvidenceCode())) {
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
                    if (true == DIVERGENT_EC.equals(compAnnot.getEvidence().getEvidenceCode())  ||
                        true == KEY_RESIDUES_EC.equals(compAnnot.getEvidence().getEvidenceCode()) ||
                        true == DESCENDANT_SEQUENCES_EC.equals(compAnnot.getEvidence().getEvidenceCode())) {
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
            ArrayList<Annotation> annotList = (ArrayList<Annotation>)nvi.getGoAnnotationList().clone();     // Clone since annotation list gets modified within loop
            if (null != annotList) {
//                System.out.println("Node has " + annotList.size() + " annotations");
                for (int i = 0; i < annotList.size(); i++) {
                    Annotation a = annotList.get(i);
//                    System.out.println("Processing " + i + " annotation");
                    Evidence e = a.getEvidence();
//                    if (true == Evidence.CODE_IBA.equals(e.getEvidenceCode()) && false == gNode.isLeaf()) {
//                        System.out.println("Here");
//                    }
                    if (true == Evidence.CODE_IBA.equals(e.getEvidenceCode()) && true == initialIBAannots.contains(a)) {
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
                        propagateIBAtoNodes(familyAcc, propagator, descendents, a.getGoTerm());
                    }
                    if (true == Evidence.CODE_IBD.equals(e.getEvidenceCode())) {
                        ArrayList<AnnotationNode> descendents = new ArrayList<AnnotationNode>();
                        allNonPrunedDescendents(gNode, descendents);
                        HashSet<AnnotationNode> nodesProvidingEvidence = getEvidenceNodesForAnnotation(a, nodeLookupTbl);
                        if (null != nodesProvidingEvidence) {
                            descendents.removeAll(nodesProvidingEvidence);
                        }
                        propagateIBAtoNodes(familyAcc, a, descendents, a.getGoTerm());
                        continue;
                    }
                    if (true == Evidence.CODE_IRD.equals(e.getEvidenceCode()) || true == Evidence.CODE_IKR.equals(e.getEvidenceCode())) {
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
                                if (false == Evidence.CODE_IBA.equals(descAnnot.getEvidence().getEvidenceCode())) {
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
                        if (true == KEY_RESIDUES_EC.equals(e.getEvidenceCode())) {
                            propagateIBAtoNodes(familyAcc, a, descendents, a.getGoTerm());
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
        }
        else {
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
        HashSet<Annotation> withAnnotSet = ad.getWithAnnotSet();
        if (null == withAnnotSet) {
            return null;
        }
        HashSet<AnnotationNode> withNodes = new HashSet<AnnotationNode>();

        for (Annotation annot : withAnnotSet) {
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
        for (Annotation withAnnot: withs) {
            if (a == withAnnot) {
                continue;
            }
            if (Evidence.CODE_IBD.equals(withAnnot.getEvidence().getEvidenceCode())) {
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
        for (Annotation withAnnot: withs) {
            if (a == withAnnot) {
                continue;
            }
            return withAnnot;
        }
        return null;        
    }
    
    private static void propagateIBAtoNodes(String familyAcc, Annotation propagator, ArrayList<AnnotationNode> nodeList, String term) {
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
            newDetail.addWith(propagator);
            newDetail.setAnnotatedNode(n);
            Evidence newEvidence = new Evidence();
            newAnnot.setEvidence(newEvidence);
            newEvidence.setEvidenceCode(ANCESTRAL_EVIDENCE_CODE);
            DBReference dbRef = new DBReference();
            dbRef.setEvidenceType(PAINT_REF);
            dbRef.setEvidenceValue(familyAcc);
            newEvidence.addDbRef(dbRef);

            newDetail.addWith(propagator);
            // Get inherited qualifiers.  Set with of qualifiers to propagator of annotation
            LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = propagator.getAnnotationDetail().getQualifierLookup();
            for (Qualifier q: qualifierLookup.keySet()) {
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
    
	public String getPaintEvidenceAcc(String familyAcc) {

			int acc = Integer.valueOf(familyAcc.substring("PTHR".length())).intValue();
			String paint_id = String.format("%1$07d", acc);
			return paint_id;

	}    
}
