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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



/**
 *
 * @author muruganu
 */
public class GOTermHelper implements Serializable{
    
    // Until there is real data, just use the accessions
    public static final String CAT_MOLECULAR_FUNCTION = "molec";
    public static final String CAT_BIOLOGICAL_PROCESS = "biol";
    public static final String CAT_CELLULAR_COMPONENT = "cellul";
    
    public static final String ASPECT_MF = "F";
    public static final String ASPECT_BP = "P";
    public static final String ASPECT_CC = "C";       
    
    private HashMap<String, GOTerm> clsToTerm;
    private ArrayList<GOTerm> topLevelTerms;

    public GOTermHelper(HashMap<String, GOTerm> clsToTerm, ArrayList<GOTerm> topLevelTerms) {
        this.clsToTerm = clsToTerm;
        this.topLevelTerms = topLevelTerms;
    }
    
    public GOTerm getTerm(String goId) {
        if (null == goId || null == clsToTerm) {
            return null;
        }
        return clsToTerm.get(goId);
    }
    
    public ArrayList<GOTerm> getAncestors(GOTerm term) {
        ArrayList<GOTerm> parents = new ArrayList<GOTerm>();
        getUniqueAncestors(term, parents);
        return parents;
    }
  
    public void getUniqueAncestors(GOTerm term, ArrayList<GOTerm> parents) {
        ArrayList<GOTerm> curParents = term.getParents();
        if (null == curParents) {
            return;
        }
        for (GOTerm parent: curParents) {
            if (parents.contains(parent)) {
                continue;
            }
            parents.add(parent);
            getUniqueAncestors(parent, parents);
        }
    }
    
    public HashSet<GOTerm> getChildren(GOTerm term) {
        HashSet<GOTerm> children = new HashSet<GOTerm>();
        getUniqueChildren(term, children);
        return children;
    }
    
    public void getUniqueChildren(GOTerm term, HashSet<GOTerm> children) {
        ArrayList<GOTerm> curChildren = term.getChildren();
        if (null == curChildren) {
            return;
        }
        
        for (GOTerm child: curChildren) {
            if (children.contains(child)) {
                continue;
            }
            children.add(child);
            getUniqueChildren(child, children);
        }
    }
    
    public GOTermHelper() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public GOTerm getMolecularFunctionRoot() {
        for (int i = 0; i < topLevelTerms.size(); i++) {
            GOTerm c = (GOTerm) topLevelTerms.get(i);
            if (c.getAcc().equals("GO:0003674")) {
                return c;
            }
        }
        return null;
    }

    public GOTerm getBiologicalProcessRoot() {
        for (int i = 0; i < topLevelTerms.size(); i++) {
            GOTerm c = (GOTerm) topLevelTerms.get(i);
            if (c.getAcc().equals("GO:0008150")) {
                return c;
            }
        }
        return null;
    }

    public GOTerm getCellularComponentRoot() {
        for (int i = 0; i < topLevelTerms.size(); i++) {
            GOTerm c = (GOTerm) topLevelTerms.get(i);
            if (c.getAcc().equals("GO:0005575")) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * 
     * @param goTermList list of GOTerms that are already sorted by ancestor
     * @return 
     */
    public static HashMap<String, ArrayList<ArrayList<GOTerm>>> organizeTerms(ArrayList<GOTerm> goTermList) {
        if (null == goTermList) {
            return null;
        }
        HashMap<String, ArrayList<ArrayList<GOTerm>>> lookup = new HashMap<String, ArrayList<ArrayList<GOTerm>>>();
        
        // Group according to aspect
        HashMap<String, ArrayList<GOTerm>> termLookup =  new HashMap<String, ArrayList<GOTerm>>();
        for (int i = 0; i < goTermList.size(); i++) {
            GOTerm term = goTermList.get(i);
            String aspect = term.getAspect();
            ArrayList<GOTerm> termList = termLookup.get(aspect);
            if (null == termList) {
                termList = new ArrayList<GOTerm>();
                termLookup.put(aspect, termList);
            }
            termList.add(term);
        }
        
        Set<String> aspectSet = termLookup.keySet();
        for (Iterator<String> aspectIter = aspectSet.iterator(); aspectIter.hasNext();) {
           String aspect = aspectIter.next();
           ArrayList<ArrayList<GOTerm>> group = new ArrayList<ArrayList<GOTerm>>();
           lookup.put(aspect, group);
           ArrayList<GOTerm> termList = termLookup.get(aspect);
           while (termList.size() > 0) {
                ArrayList<GOTerm> groupList = new ArrayList<GOTerm>();
                GOTerm curTerm = termList.remove(0);
                groupList.add(curTerm);
                addAncestors(groupList, 0, termList);
                group.add(groupList);
            }
           
        }
        return lookup;
    }
    
    private static void addAncestors(ArrayList<GOTerm> groupList, int index, ArrayList<GOTerm> termList) {
        if (index < 0 || index > groupList.size()) {
            return;
        }
        GOTerm curTerm = groupList.get(index);
        ArrayList<GOTerm> parents = curTerm.getParents();
        if (null == parents) {
            return;
        }
        int counter = index + 1;
        boolean added = false;
        for (int i = 0; i < parents.size(); i++) {
            GOTerm parent = parents.get(i);
            int parentIndex = termList.indexOf(parent);
            if (parentIndex >= 0) {
                groupList.add(counter, termList.remove(parentIndex));
                counter++;
                added = true;
            }
        }
        if (true == added && termList.size() > 0 ) {
            index = index+1;
            addAncestors(groupList, index, termList);
        }
    }
    
    public HashSet<Qualifier> getValidQualifiersForTerm(GOTerm term, HashSet<Qualifier> qualifierSet) {
        if (null == qualifierSet) {
            return null;
        }
        HashSet<Qualifier> rtnSet = new HashSet<Qualifier>();
        for (Qualifier q: qualifierSet) {
            if (true == isQualifierValidForTerm(term, q)) {
                rtnSet.add(q);
            }
        }
        
        if (rtnSet.isEmpty()) {
            return null;
        }
        return rtnSet;
    }
    
    public boolean canTermHaveQualifier(GOTerm term, String qualifier) {
        if (null == qualifier) {
            return true;
        }
        
        if (true == Qualifier.QUALIFIER_NOT.equalsIgnoreCase(qualifier)) {
            return true;
        }
        
        String aspect = term.getAspect();
        if (null == aspect) {
            return false;
        }
        if ((false == ASPECT_MF.equalsIgnoreCase(aspect)) && (true == Qualifier.QUALIFIER_CONTRIBUTES_TO.equalsIgnoreCase(qualifier))) {
            return false;
        }
        if ((false == ASPECT_CC.equalsIgnoreCase(aspect)) && (true == Qualifier.QUALIFIER_COLOCALIZES_WITH.equalsIgnoreCase(qualifier))) {
            return false;
        }
        return true;        
    }
    
 
    public boolean isQualifierValidForTerm(GOTerm term, Qualifier q) {
        return canTermHaveQualifier(term, q.getText());
//        String text = q.getText();
//        if (null == text) {
//            return true;
//        }
//        if (q.isNot()) {
//            return true;
//        }
//        String aspect = term.getAspect();
//        
//        if (null == aspect) {
//            return false;
//        }
//        if ((false == ASPECT_MF.equalsIgnoreCase(aspect)) && (true == q.isContributesTo())) {
//            return false;
//        }
//        if ((false == ASPECT_CC.equalsIgnoreCase(aspect)) && (true == q.isColocalizesWith())) {
//            return false;
//        }
//        return true;
    }
    
    
    
}
