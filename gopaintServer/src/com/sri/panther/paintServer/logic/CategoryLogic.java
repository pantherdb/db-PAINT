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
package com.sri.panther.paintServer.logic;


import com.sri.panther.paintServer.database.CategoryHelper;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;


public class CategoryLogic {
    private static CategoryLogic instance;
    private static GOTermHelper gth = null;
    private static HashMap<String, GOTerm> clsToTermLookup = null;
    private static Hashtable<String, String> releaseInfoTbl = null;
    
    public static final String RELEASE_VERSION_PANTHER = CategoryHelper.COLUMN_PANTHER_VERSION;
    public static final String RELEASE_DATE_PANTHER = CategoryHelper.COLUMN_PANTHER_RELEASE_DATE;
    public static final String RELEASE_VERSION_GO = CategoryHelper.COLUMN_GO_ANNOTATION_FORMAT_VERSION;
    public static final String RELEASE_DATE_GO = CategoryHelper.COLUMN_GO_ANNOTATION_RELEASE_DATE;
     
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
        }

        gth = new GOTermHelper(clsToTermLookup, topLevelCats);
        
        releaseInfoTbl = CategoryHelper.getCategoryReleaseInfo();
        
        instance = new CategoryLogic();
        return instance;
    }
    
    public GOTermHelper getGOTermHelper() {
        return gth;
    }
    
    public String getReleaseInfo(String type) {
        if (null == releaseInfoTbl) {
            return null;
        }
        return (String) releaseInfoTbl.get(type);
    }    
}
