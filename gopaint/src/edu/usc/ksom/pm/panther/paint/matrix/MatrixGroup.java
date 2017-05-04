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
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import java.util.ArrayList;
import java.util.Iterator;
import org.paint.datamodel.Association;
import org.paint.main.PaintManager;

/**
 *
 * @author muruganu
 */
public class MatrixGroup implements Comparable<MatrixGroup> {
    private ArrayList<TermAncestor> items = new ArrayList<TermAncestor>();
    GOTermHelper gth;
    PaintManager pm;
    
    public MatrixGroup(TermAncestor ta) {
        pm = PaintManager.inst();
        gth = pm.goTermHelper();
        items.add(ta);
    }
    
    public int getCount() {
        return items.size();
    }
    
    public TermAncestor getTermAncestorAtIndex(int index) {
        if (null == items || index >= items.size()) {
            return null;
        }
        return items.get(index);
    }
    
    public GOTerm getTermAtIndex(int index) {
        if (null == items || index >= items.size()) {
            return null;
        }
        TermAncestor ta = items.get(index);
        TermToAssociation toa = ta.getTermToAssociation();
        if (null == toa) {
            return null;
        }
        return toa.getTerm();
    }
    
    public int getIndexOfGroupWithAncestor(GOTerm term) {
        for (int i = 0; i < items.size(); i++) {
            TermAncestor cur = items.get(i);
            ArrayList<GOTerm> ancestorList = cur.getAncestorList();
            
            if (ancestorList.contains(term)) {
                return i;
            }
        }
        return -1;
    }
    
    public boolean addToList(TermAncestor ta) {
        String aspect = ta.getTermToAssociation().getTerm().getAspect();
//        if ("F".equals(aspect)) {
//            System.out.println("Processing mf");
//            if ("GO:0016787".equals(ta.getTermToAssociation().getTerm().getAcc()) || "GO:0016818".equals(ta.getTermToAssociation().getTerm().getAcc())) {
//                System.out.println("Found hydrolase activity");
//            }
//        }
        ArrayList<GOTerm> addAncestorList = ta.getAncestorList();
        ArrayList<GOTerm> copyAddAncestorList = (ArrayList<GOTerm>)addAncestorList.clone();
        copyAddAncestorList.add(0, ta.getTermToAssociation().getTerm());
        int insertIndex = -1;
        for (int i = 0; i < items.size(); i++) {
             TermAncestor cur = items.get(i);
             ArrayList<GOTerm> curAncestorList = cur.getAncestorList();
//             if (addAncestorList.isEmpty() && curAncestorList.contains(ta.getTermToAssociation().getTerm())) {
//                 insertIndex = i + 1;
//                 continue;
//             }
             if (true == isSubSetWithOrder(curAncestorList, copyAddAncestorList)) {
                 insertIndex = i + 1; 
             }
        }
        if (insertIndex >= 0) {
            items.add(insertIndex, ta);
            return true;        
        }
        
        for (int i = 0; i < items.size(); i++) {
            TermAncestor cur = items.get(i);
            ArrayList<GOTerm> curAncestorList = cur.getAncestorList();
            ArrayList<GOTerm> copyCurAncestorList = (ArrayList<GOTerm>)curAncestorList.clone();
            copyCurAncestorList.add(0, cur.getTermToAssociation().getTerm());
//            if (curAncestorList.isEmpty() && addAncestorList.contains(cur.getTermToAssociation().getTerm())) {
//                insertIndex = i;
//                continue;
//            }
            if (true == isSubSetWithOrder(copyAddAncestorList, copyCurAncestorList)) {
                // Check previous term ancestor
                if (i >= 1) {
                    TermAncestor previousTermAncestor = items.get(i - 1);
                    ArrayList<GOTerm> copyPreviousAncestorList = (ArrayList<GOTerm>)previousTermAncestor.getAncestorList().clone();
                    copyPreviousAncestorList.add(0, previousTermAncestor.getTermToAssociation().getTerm());
                    if (false == isSubSetWithOrder(copyPreviousAncestorList, copyAddAncestorList)) {
                        continue;
                    }
                }
                insertIndex = i;
             }
        }
        
        if (insertIndex >= 0) {
            items.add(insertIndex, ta);
            return true;        
        }
        return false;
    }    
    
    public boolean addToListOld(TermAncestor ta) {
        TermToAssociation toa = ta.getTermToAssociation();
        GOTerm addTerm = toa.getTerm();
        ArrayList<GOTerm> addAncestorList = ta.getAncestorList();
        int insertIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            TermAncestor cur = items.get(i);
            GOTerm curTerm = cur.getTermToAssociation().getTerm();
            int index = addAncestorList.indexOf(curTerm);
            if (index >= 0) {
                // Ensure this term is ancestor of previous term as well
                if (i - 1 >= 0) {
                    TermAncestor previousTermAncestor = items.get(i - 1);
                    ArrayList<GOTerm> previousAncestorList = previousTermAncestor.getAncestorList();
                    if (previousAncestorList.contains(addTerm)) {
                        insertIndex = i;
                    }
                }
                else {
                    insertIndex = i;
                }
                continue;
            }
            ArrayList<GOTerm> curAncestorList = cur.getAncestorList();
            index = curAncestorList.indexOf(addTerm);
            if (index >= 0) {
                // Ensure Ancestor list of item to be inserted, contains next items term
                if (i + 1 < items.size()) {
                    TermAncestor nextTermAncestor = items.get(i + 1);
                    if (addAncestorList.contains(nextTermAncestor.getTermToAssociation().getTerm())) {
                        insertIndex = i + 1;
                    }
                }
                else {
                    insertIndex = i + 1;
                }
            }
        }
        if (insertIndex < 0) {
            return false;
        }
        items.add(insertIndex, ta);
        return true;
    }
    
    public NodeInfoForMatrix getAnnotInfoForNode(Node n, int index) {
        if (null == n || index < 0 || index >= items.size()) {
            return null;
        }
        TermAncestor ta = items.get(index);
        TermToAssociation toa = ta.getTermToAssociation();
        GOTerm term = toa.getTerm();
        return new NodeInfoForMatrix(pm.getGeneByPTNId(n.getStaticInfo().getPublicId()), term, gth);
    }
    
//    public NodeInfoForGroup getAnnotInfoForNodeOld(Node n, int index) {
//        if (null == n || index < 0 || index >= items.size()) {
//            return null;
//        }
////        if (true == "PTHR10000:AN231".equals(n.getStaticInfo().getNodeAcc())) {
////            System.out.println("Here");
////        }
//        TermAncestor ta = items.get(index);
//        TermToAssociation toa = ta.getTermToAssociation();
//        GOTerm term = toa.getTerm();
//        ArrayList<Association> asnList = toa.getAsnList();
//        for (Association a: asnList) {
//            if (n.equals(a.getNode())) {
//                // Association may not be real i.e. node maybe annotated to a more specific child term
//                if (true == term.getAcc().equals(a.getAnnotation().getGoTerm())) {
//                    return new NodeInfoForGroup(a.getAnnotation(), term.getAcc(), term.getName(), true);//toa.isAssociationDirect(a));
//                }
//            }
//        }
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null == nvi) {
//            return null;
//        }
//        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
//        if (null == goAnnotList) {
//            return null;
//        }
//
//        ArrayList<Annotation> moreSpecificChildExpAnnot = new ArrayList<Annotation>();
//        ArrayList<Annotation> moreSpecificChildNonExpAnnot = new ArrayList<Annotation>();
//        for (Annotation annot: goAnnotList) {
//            String curTerm = annot.getGoTerm();
//            if (true == "PTHR10000:AN240".equals(n.getStaticInfo().getNodeAcc()) && true == "GO:0005575".equals(curTerm)) {
//                System.out.println("Found the node");
//            }
//            ArrayList<GOTerm> ancestors = gth.getAncestors(gth.getTerm(curTerm));
//            if (false == ancestors.contains(term)) {
//                continue;
//            }
//            Evidence e = annot.getEvidence();
//            if (e.isExperimental()) {
//                moreSpecificChildExpAnnot.add(annot);
//            }
//            else {
//                moreSpecificChildNonExpAnnot.add(annot);
//            }
//        }
//        
//        if (moreSpecificChildExpAnnot.isEmpty() && moreSpecificChildNonExpAnnot.isEmpty()) {
//            return null;
//        }
//        
//        if (false == moreSpecificChildExpAnnot.isEmpty()) {
//            return new NodeInfoForGroup(moreSpecificChildExpAnnot.get(0), term.getAcc(), term.getName(), false);
//        }
//        return new NodeInfoForGroup(moreSpecificChildNonExpAnnot.get(0), term.getAcc(), term.getName(), false);
//    }
    
//    public boolean belongsToGroup(GOTerm term, ArrayList<GOTerm> ancestorSet) {
//        for (Iterator<TermAncestor> i = items.iterator(); i.hasNext();) {
//            TermAncestor ta = i.next();
//            GOTerm curTerm = ta.getTermToAssociation().getTerm();
//            if (ancestorSet.contains(curTerm)) {
//                return true;
//            }
//            ArrayList<GOTerm> curAncestorSet = ta.getAncestorList();
//            if (curAncestorSet.contains(term)) {
//                return true;
//            }
//
//        }
//        return false;
//    }
    
    public boolean termExists(GOTerm term) {
        for (Iterator<TermAncestor> i = items.iterator(); i.hasNext();) {
            TermAncestor ta = i.next();
            if (true == ta.getTermToAssociation().getTerm().equals(term)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean addAssociation(GOTerm term, Association a) {
        boolean added = false;
        for (Iterator<TermAncestor> i = items.iterator(); i.hasNext();) {
            TermAncestor ta = i.next();
            GOTerm curTerm = ta.getTermToAssociation().getTerm();
            if (true == curTerm.equals(term)) {
                TermToAssociation trmToAsn = ta.getTermToAssociation();
                trmToAsn.addAsn(a);
                added = true;
            }
        }
        return added;
    }

    public ArrayList<TermAncestor> getItems() {
        return items;
    }

    @Override
    public int compareTo(MatrixGroup o) {
        // Compare based on count of experimental annotations
        Integer cur = this.getCountExperimentalAnnotations();
        Integer other = o.getCountExperimentalAnnotations();
        return cur.compareTo(other);
//        if ((null == this.items || 0 == this.items.size()) && (null == o.items || 0 == o.items.size())) {
//            return 0;
//        }
//        TermAncestor ta = this.items.get(0);
//        TermAncestor compTa = o.items.get(0);
//        
//        String curTerm = ta.getTermToAssociation().getTerm().getName();
//        String compTerm = compTa.getTermToAssociation().getTerm().getName();
//        return curTerm.compareTo(compTerm);
    }
    
    
    // returns if list2 is a subset of list1 and items are in same order
    public boolean isSubSetWithOrder(ArrayList<GOTerm> list1, ArrayList<GOTerm> list2) {
        if (null == list1 || null == list2) {
            return false;
        }
        if (list2.size() > list1.size()) {
            return false;
        }
        
        if (0 == list2.size()) {
            return false;
        }
        
        int indexOfFirstTerm = list1.indexOf(list2.get(0));
        if (indexOfFirstTerm < 0) {
            return false;
        }
        if (indexOfFirstTerm + list2.size() > list1.size()) {
            return false;
        }
        int counter = 1;
        for (int i = indexOfFirstTerm + 1; i < list1.size() && counter < list2.size(); i++) {
            GOTerm term1 = list1.get(i);
            GOTerm term2 = list2.get(counter);
            if (false == term1.equals(term2)) {
                return false;
            }
            counter++;
        }
        
        return true;
    }
    
    private int getCountExperimentalAnnotations() {
        int total = 0;
        if (null == items) {
            return total;
        }
        for (TermAncestor ta : items) {
            TermToAssociation toa = ta.getTermToAssociation();
            String acc = toa.getTerm().getAcc();
            ArrayList<Association> asnList = toa.getAsnList();
            for (Association a : asnList) {

                // Association may not be real i.e. node maybe annotated to a more specific child term
                Annotation annot = a.getAnnotation();
                if (true == acc.equals(annot.getGoTerm()) && true == annot.getEvidence().isExperimental()) {
                    total++;
                }
            }
        }
        return total;
    }
    
    
            
}
