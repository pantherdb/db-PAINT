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
package com.sri.panther.paintServer.logic;


import com.sri.panther.paintCommon.util.FileUtils;
import com.sri.panther.paintServer.database.CategoryHelper;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author muruganu
 */
public class CategoryLogic {
    private static CategoryLogic instance;
    private static GOTermHelper gth = null;
    private static HashMap<String, GOTerm> clsToTermLookup = null;

    
    private CategoryLogic() {
        
    }
    
    public static synchronized CategoryLogic getInstance() {
        if (null != instance) {
            return instance;
        }
        clsToTermLookup = CategoryHelper.getGOTermLookup();
        
        // Get top level nodes
            ArrayList<GOTerm> topLevelCats = new ArrayList<GOTerm>();
            Collection<GOTerm> catCol = clsToTermLookup.values();
            for (Iterator<GOTerm> i = catCol.iterator(); i.hasNext();) {
                GOTerm cat = i.next();
                if (null == cat.getParents() || 0 == cat.getParents().size()) {
                    topLevelCats.add(cat);
                }
            }
            
        for (int i = 0; i < topLevelCats.size(); i++) {
            GOTerm term = topLevelCats.get(i);
            String accession = term.getAcc();
            GOTerm topLevelTerm = CategoryHelper.getGOCategoryByAcc(accession);
            if (null == topLevelTerm) {
                System.out.println("Did not get information about top level GO classification node " + accession);
            }
            term.setName(topLevelTerm.getName());
            term.setDescription(topLevelTerm.getDescription());
            //term.setAspect(topLevelTerm.getAspect());
//            HashSet descList = new HashSet();
//            getDistinctDescendentList(c, descList);
//            c.setNumDistinctDescendents(descList.size());
        }
        
        
        
        
        
            
            
            
        gth = new GOTermHelper(clsToTermLookup, topLevelCats);
        instance = new CategoryLogic();
        return instance;
    }
    
    public static synchronized CategoryLogic getInstanceOld() {
        if (null == instance) {
            HashMap<String, GOTerm> clsToTermLookup = new HashMap<String, GOTerm>();
            // Temporarily read from flat file
            String[] contents = FileUtils.readFile("C:\\PAINT\\GOPAINT\\full_go_hierarchy_2_formatted.txt");
            for (int i = 0; i < contents.length; i++) {
                String content = contents[i];
                content = content.trim();
                if (content.isEmpty()) {
                    continue;
                }
                String parts[] = content.split("\t");
                if (null == parts || parts.length < 9) {
                    System.out.println("Did not find correct number of entities on line " + i + 1 + " line text is " + content);
                    continue;
                }
                String parentAcc = parts[3];
                String childAcc = parts[0];
                GOTerm parent = clsToTermLookup.get(parentAcc);
                if (null == parent) {
                    parent = new GOTerm();
                    parent.setAcc(parentAcc);
                    parent.setAspect(parts[8]);
                    clsToTermLookup.put(parentAcc, parent);
                }
                GOTerm child = clsToTermLookup.get(childAcc);
                if (null == child) {
                    child = new GOTerm();

                    child.setAcc(childAcc);
                }
                child.setName(parts[1]);
                child.setDescription(parts[2]);
                child.setAspect(parts[7]);
                clsToTermLookup.put(childAcc, child);

                child.addParent(parent);
                parent.addChild(child);
            }

            ArrayList<GOTerm> topLevelCats = new ArrayList<GOTerm>();
            Collection<GOTerm> catCol = clsToTermLookup.values();
            for (Iterator<GOTerm> i = catCol.iterator(); i.hasNext();) {
                GOTerm cat = i.next();
                if (null == cat.getParents() || 0 == cat.getParents().size()) {
                    topLevelCats.add(cat);
                }
            }
            gth = new GOTermHelper(clsToTermLookup, topLevelCats);
            instance = new CategoryLogic();

        }
        return instance;
    }
    
    public GOTermHelper getGOTermHelper() {
        return gth;
    }
}
