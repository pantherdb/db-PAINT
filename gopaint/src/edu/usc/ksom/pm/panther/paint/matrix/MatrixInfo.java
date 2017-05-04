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
package edu.usc.ksom.pm.panther.paint.matrix;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.paint.datamodel.Association;
import org.paint.datamodel.GeneNode;
import org.paint.util.GeneNodeUtil;

/**
 *
 * @author muruganu
 */
public class MatrixInfo {
    private GOTermHelper gth;
    private ArrayList<TermAncestor> termAncestorList;
    private HashMap<String, ArrayList<MatrixGroup>> annotTypeLookup = new HashMap<String, ArrayList<MatrixGroup>>();
    private List<GeneNode> nodes;
    private ArrayList<GOTerm> handledTerms = new ArrayList<GOTerm>();
    
    public MatrixInfo (GOTermHelper gth, List<GeneNode> treeNodes, ArrayList<TermAncestor> termAncestorList) {
        this.gth = gth;
        this.nodes = treeNodes;
        this.termAncestorList = termAncestorList;
        
        if (null == nodes) {
            return;
        }
        
        // First add items user wants added.  (This way we see it in front of list - not really we are sorting now)
        for (TermAncestor ta : termAncestorList) {
            TermToAssociation toa = ta.getTermToAssociation();
            String aspect = toa.getTerm().getAspect();
            addTermAncestor(aspect, ta);
        }
        
        
        // Get direct annotations for node
        for (GeneNode gNode: nodes) {
            Node n = gNode.getNode();
            if (n == null) {
                System.out.println("Did not find node for gn " + gNode.getPaintId());
                continue;
            }
            if (false == annotApplicable(gNode)) {
                continue;
            }
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList || 0 == annotList.size()) {
                continue;
            }
            for (Annotation a: annotList) {
                Association asn = new Association();
//                asn.setNodeAssociatedToAnnotation(true);
                asn.setAnnotation(a);
                asn.setNode(n);
//                asnLst.add(asn);
//                GOTerm term = gth.getTerm(a.getGoTerm());
//                if (a.getGoTerm().equals("GO:0016791")) {
//                    System.out.println("Here");
//                }
                //System.out.println("Processing " + a.getGoTerm() + " " + term.getName());
                addAssociation(asn);
                //printInfo();
            }
            
        }
        
        // Determine indirect annotations i.e. set information about terms that appear in matrix where the parent of the term has been annotated
        for (GeneNode gNode: nodes) {
            Node n = gNode.getNode();
            if (n == null) {
                System.out.println("Did not find node for gn " + gNode.getPaintId());
                continue;
            }
            if (false == annotApplicable(gNode)) {
                continue;
            }
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            NodeStaticInfo nsi = n.getStaticInfo();
//            if (nsi.getNodeAcc().contains("AN617")) {
//                System.out.println("Found node");
//            }
            // Go through each column (term) of the matrix and determine, if node has annotated parent term
            Collection<ArrayList<MatrixGroup>> groupListCollection  = annotTypeLookup.values();
            for (Iterator<ArrayList<MatrixGroup>> groupCollectionIter = groupListCollection.iterator(); groupCollectionIter.hasNext();) {
            ArrayList<MatrixGroup> curList = groupCollectionIter.next();
                for (Iterator<MatrixGroup> groupIter = curList.iterator(); groupIter.hasNext();) {
                    MatrixGroup mg = groupIter.next();
                    ArrayList<TermAncestor> items = mg.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        TermAncestor ta = items.get(i);
                        boolean found = false;
                        TermToAssociation toa = ta.getTermToAssociation();
                        ArrayList<Association> asnList = toa.getAsnList();
                        for (Association a: asnList) {
                            if (n.equals(a.getNode())) {
                                found = true;
                                break;
                            }
                        }
                        // Already have annotation for this node
                        if (true == found) {
                            continue;
                        }
                        GOTerm term = toa.getTerm();
                        ArrayList<GOTerm> otherTerms = new ArrayList<GOTerm>();
                        for (int j = i + 1; j < items.size(); j++) {
                            TermAncestor otherTermAncestor = items.get(i);
                            otherTerms.add(otherTermAncestor.getTermToAssociation().getTerm());
                        }
                        Annotation annot = getParentTermAssoc(nvi, term);
                        if (null != annot) {
                            Association asn = new Association();
                            asn.setAnnotation(annot);
                            //Annotation annotCopy = annot.makeCopy();
                            //annotCopy.setAnnotIsToChildTerm(true);
                            //asn.setAnnotation(annotCopy);
//                            Annotation annotCopy = annot.makeCopy();
//                            annotCopy.setGoTerm(term.getAcc());
//                            annotCopy.setAnnotIsToChildTerm(true);
//                            asn.setAnnotation(annot);
                            asn.setNode(n);
                            mg.addAssociation(term, asn);
                            //addAssociation(asn);
                        }
                    }
                }
            }
            
            
        }
        
        // Sort the matrix Groups
        for (String aspect: annotTypeLookup.keySet()) {
            ArrayList<MatrixGroup> groupList = annotTypeLookup.get(aspect);
            Collections.sort(groupList, Collections.reverseOrder());
            
            // If an ancestor term appears in another matrix group (when go term has multiple parents), move that matrix group beside current
            for (int i = 0; i < groupList.size(); i++) {
                ArrayList<MatrixGroup> moveGroups = new ArrayList<MatrixGroup>();
                MatrixGroup current = groupList.get(i);
                GOTerm term = current.getTermAtIndex(0);
                ArrayList<GOTerm> ancestors = gth.getAncestors(term);
                for (int j = i + 1; j < groupList.size(); j++) {
                    MatrixGroup compGroup = groupList.get(j);
                    GOTerm compTerm = compGroup.getTermAtIndex(0);
                    if (ancestors.contains(compTerm)) {
                        moveGroups.add(compGroup);
                    }
                }
                if (moveGroups.size() > 0) {
                    for (int j = 0; j < moveGroups.size(); j++) {
                        groupList.remove(moveGroups.get(j));
                    }
                    groupList.addAll(i + 1, moveGroups);
                    i = i + moveGroups.size() + 1;
                    continue;
                }
                else {
                    for (int j = i + 1; j < groupList.size(); j++) {
                        MatrixGroup compGroup = groupList.get(j);
                        GOTerm compTerm = compGroup.getTermAtIndex(0);
                        ArrayList<GOTerm> compAncestors = gth.getAncestors(compTerm);
                        for (int k = 0; k < current.getCount(); k++) {
                            if (true == compAncestors.contains(current.getTermAtIndex(k))) {
                                moveGroups.add(compGroup);
                                break;
                            }
                        }
                    }
                    if (moveGroups.size() > 0) {
                        for (int j = 0; j < moveGroups.size(); j++) {
                            groupList.remove(moveGroups.get(j));
                        }
                        groupList.addAll(i + 1, moveGroups);
                        i = i + moveGroups.size() + 1;
                    }
                }
            }
            
        }
        
//        Collection<ArrayList<MatrixGroup>> groupListCollection  = annotTypeLookup.values();
//        for (Iterator<ArrayList<MatrixGroup>> i = groupListCollection.iterator(); i.hasNext();) {
//            ArrayList<MatrixGroup> curList = i.next();
//            for (Iterator<MatrixGroup> groupIter = curList.iterator(); groupIter.hasNext();) {
//                MatrixGroup mg = groupIter.next();
//                ArrayList<TermAncestor> items = mg.getItems();
//                for (int j = 0; j < items.size(); j++) {
//                    TermAncestor ta1 = items.get(j);
//                    for (int k = 0; k < items.size(); k++) {
//                        TermAncestor ta2 = items.get(k);
//                        if (j == k) {
//                            continue;
//                        }
//                        TermToAssociation toa1 = ta1.getTermToAssociation();
//                        GOTerm term1 = toa1.getTerm();
//                        ArrayList<GOTerm> ancestorList1 = gth.getAncestors(term1);
//                        TermToAssociation toa2 = ta2.getTermToAssociation();
//                        GOTerm term2 = toa2.getTerm();
//                        if (false == ancestorList1.contains(term2)) {
//                            System.out.println("Did not find parent term in group");
//                            continue;
//                        }
//                        ArrayList<Association> asnList1 = toa1.getAsnList();
//                        
//                        for (int a1 = 0; a1 < asnList1.size(); a1++) {
//                            Association asn1 = asnList1.get(a1);
//                            Node n1 = asn1.getNode();
//                            boolean found = false;
//                            ArrayList<Association> asnList2 = toa2.getAsnList();
//                            for (Association asn2: asnList2) {
//                                Node n2 = asn2.getNode();
//                                if (n1.equals(n2)) {
//                                    found = true;
//                                    break;
//                                }
//                            }
//                            if (false == found) {
//                                Association newAsn = new Association();
//                                toa2.addAsn(asn1);
//                            }
//                        }
//                        
//                    }
//                }
//            }
//        }
      

        
        //System.out.println("After adding ancestors");
        //printInfo();
        
//        // Remove duplicates from groups
//        ArrayList<GOTerm> processedTerms = new ArrayList<GOTerm>();
//        Collection <ArrayList<MatrixGroup>> valuesCol = annotTypeLookup.values();
//        for (ArrayList<MatrixGroup> curList: valuesCol) {
//            for (MatrixGroup group: curList) {
//                ArrayList<TermAncestor> items = group.getItems();
//                for (TermAncestor ta: items) {
//                    ta.getAncestorList().removeAll(processedTerms);
//                    processedTerms.addAll(ta.getAncestorList());
//                }
//            }
//        }
//        
//        System.out.println("After removing duplicates");
//        printInfo();
    }
    
    public String printTermInfo(ArrayList<GOTerm> termList) {
        StringBuffer sb = new StringBuffer();
        for (GOTerm term: termList) {
            sb.append(term.getName() + "(" + term.getAcc() + ")->");
        }
        return sb.toString();
    }
    
    public void printInfo() {
        Set<String> keySet = annotTypeLookup.keySet();
        for (Iterator<String> keyIter = keySet.iterator(); keyIter.hasNext();) {
            String aspect = keyIter.next();
            System.out.println("Information for " + aspect);
            ArrayList<MatrixGroup> mgList = annotTypeLookup.get(aspect);
            for (MatrixGroup mg: mgList) {
                ArrayList<TermAncestor> termAncestorList = mg.getItems();
                for (TermAncestor ta: termAncestorList) {
                    ArrayList<GOTerm> ancestors = ta.getAncestorList();
                    TermToAssociation toa = ta.getTermToAssociation();
                    GOTerm term = toa.getTerm();
                    System.out.println(term.getAspect() + " " + term.getName() + "(" + term.getAcc() + ") has ancestors " + printTermInfo(ancestors));
                }
            }
        }
    }
    
    private Annotation getParentTermAssoc(NodeVariableInfo nvi, GOTerm term) {
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        for (Annotation a: annotList) {
            GOTerm curTerm = gth.getTerm(a.getGoTerm());
            if (gth.getAncestors(curTerm).contains(term)) {
                Evidence e = a.getEvidence();
                if (e.isExperimental()) {
                    return a;
                }
            }
        }
        return null;
    }
    
    private void addAssociation(Association a) {
        Annotation annot = a.getAnnotation();
        if (null == annot) {
            return;
        }
        String goTerm = annot.getGoTerm();
        if (null == goTerm) {
            return;
        }
//        if (true == goTerm.equals("GO:0050308")) {
//            System.out.println("Adding double");
//        }
        GOTerm gTerm = gth.getTerm(goTerm);
        
        // If term has already been added for another tree node, just add this annotation information and exit
        if (true == termExists(gTerm)) {
            addAssociationToExistingList(gTerm, a);
            return;
        }

        // Although a term only has one aspect, its ancestors can have different aspect type.  Therefore, check all aspect types for the term in question
        // If term has already been added, then, only add association to applicable terms        
        String aspect = gTerm.getAspect();
        ArrayList<GOTerm> ancestorList = gth.getAncestors(gTerm);
        //ancestorList.removeAll(handledTerms);
        ArrayList<GOTerm> differentAspectList = new ArrayList<GOTerm>();
        for (Iterator<GOTerm> ancestorIter = ancestorList.iterator(); ancestorIter.hasNext();) {
            GOTerm parent = ancestorIter.next();
            if (false == aspect.equals(parent.getAspect())) {
                differentAspectList.add(parent);
            }
        }
        ancestorList.removeAll(differentAspectList);
        TermToAssociation toa = new TermToAssociation(gTerm);
        toa.addAsn(a);
        TermAncestor ta = new TermAncestor(toa, ancestorList);
        
        addTermAncestor(aspect, ta);
        handledTerms.add(gTerm);
        if (differentAspectList.isEmpty()) {
            return;
        }
        //handledTerms.addAll(ancestorList);

        // Group ancestors from other aspects and add
        //System.out.println("Term is " + gTerm.getAcc());
        HashMap<String, ArrayList<ArrayList<GOTerm>>> otherAncestorLookup = gth.organizeTerms(differentAspectList);
        Set<String> aspectSet = otherAncestorLookup.keySet();
        for (Iterator<String> aspectIter = aspectSet.iterator(); aspectIter.hasNext();) {
            aspect = aspectIter.next();
            ArrayList<ArrayList<GOTerm>> ancestorGroupList = otherAncestorLookup.get(aspect);
            for (int i = 0; i < ancestorGroupList.size(); i++) {
                ancestorList = ancestorGroupList.get(i);
                ancestorList.removeAll(handledTerms);
                if (0 == ancestorList.size()) {
                    continue;
                }

                // If term already exists, then add association and continue
                GOTerm cur = ancestorList.get(0);
                ancestorList.remove(0);         // Remove term from ancestor list
                if (true == termExists(cur)) {
                    addAssociationToExistingList(cur, a);
                    continue;
                }
                handledTerms.add(cur);

                toa = new TermToAssociation(cur);
                toa.addAsn(a);
                ta = new TermAncestor(toa, ancestorList);
                addTermAncestor(aspect, ta);
            }
        }
    }
    
    private void addTermAncestor(String aspect, TermAncestor ta) {
        ArrayList<MatrixGroup> lookupList = annotTypeLookup.get(aspect);
        if (null == lookupList) {
            lookupList = new ArrayList<MatrixGroup>();
            annotTypeLookup.put(aspect, lookupList);
        }
        
        // Attempt to add to existing matrix groups
        boolean added = false;
        for (int i = 0; i < lookupList.size(); i++) {
            MatrixGroup mg = lookupList.get(i);
            added = mg.addToList(ta);
            if (true == added) {
                break;
            }
        }
        // Could not be added to existing groups, create a new group
        if (false == added) {
            MatrixGroup mg = new MatrixGroup(ta);
            lookupList.add(mg);
        }
    }
    
    private boolean termExists(GOTerm term) {
        Collection<ArrayList<MatrixGroup>> groupListCollection  = annotTypeLookup.values();
        if (null == groupListCollection || 0 == groupListCollection.size()) {
            return false;
        }
        for (Iterator<ArrayList<MatrixGroup>> i = groupListCollection.iterator(); i.hasNext();) {
            ArrayList<MatrixGroup> curList = i.next();
            for (Iterator<MatrixGroup> groupIter = curList.iterator(); groupIter.hasNext();) {
                MatrixGroup mg = groupIter.next();
                if (mg.termExists(term)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean  addAssociationToExistingList(GOTerm term, Association a) {
        Collection<ArrayList<MatrixGroup>> groupListCollection  = annotTypeLookup.values();
        for (Iterator<ArrayList<MatrixGroup>> i = groupListCollection.iterator(); i.hasNext();) {
            ArrayList<MatrixGroup> curList = i.next();
            for (Iterator<MatrixGroup> groupIter = curList.iterator(); groupIter.hasNext();) {
                MatrixGroup mg = groupIter.next();
                if (mg.termExists(term)) {
                    return mg.addAssociation(term, a);
                }
            }
        }
        return false;
    }
    
    private boolean annotApplicable(GeneNode gn) {
        if (null == gn) {
            return false;
        }
        if (GeneNodeUtil.inPrunedBranch(gn)) {
            return false;
        }
        Node n = gn.getNode();
        if (null == n) {
            return false;
        }
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return false;
        }
        return true;
    }
    
    public ArrayList<MatrixGroup> getGroups(String aspect) {
        if (null == aspect) {
            return null;
        }
        return annotTypeLookup.get(aspect);
    }
    
    public Set<String> getAspects() {
        return annotTypeLookup.keySet();
    }
    

    

}
