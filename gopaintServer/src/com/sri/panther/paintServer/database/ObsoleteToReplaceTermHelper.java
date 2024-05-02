/**
 *  Copyright 2022 University Of Southern California
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
package com.sri.panther.paintServer.database;

import com.sri.panther.paintServer.database.CategoryHelper;
import com.sri.panther.paintServer.logic.CategoryLogic;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class ObsoleteToReplaceTermHelper {

    private static GOTermHelper goTermHelper = CategoryLogic.getInstance().getGOTermHelper();
    private static final HashMap<String, GOTerm> obsoleteGOClsToReplaceGOTermLookup = initObsToReplace();

    private static synchronized HashMap<String, GOTerm> initObsToReplace() {
        HashMap<String, GOTerm> obsoleteGOClsToReplaceGOTermLookup = new HashMap<String, GOTerm>();
        
        // Get list of obsolete terms and associated replaced by terms
        HashMap<Map.Entry<String, String>, Map.Entry<String, String>> obsoleteToReplaceLookup = CategoryHelper.getGoTermCarryOverLookup();
        
        // There are cases where a replace_by term may have been replaced.  Need to determine latest replaced term.
        HashMap<Map.Entry<String, String>, Map.Entry<String, String>> nonMatchedLookup = new HashMap<Map.Entry<String, String>, Map.Entry<String, String>>();
        for (Entry<Map.Entry<String, String>, Map.Entry<String, String>> lookup: obsoleteToReplaceLookup.entrySet()) {
            Map.Entry<String, String> obsolete = lookup.getKey();            
            Map.Entry<String, String> replace = lookup.getValue();
            String replaceAcc = replace.getValue();
            GOTerm replaceTerm = goTermHelper.getTerm(replaceAcc);
            if (null != replaceTerm) {
                obsoleteGOClsToReplaceGOTermLookup.put(obsolete.getValue(), replaceTerm);
                continue;
            }
            nonMatchedLookup.put(obsolete, replace);
        }
        
        boolean match = true;
        ArrayList<Map.Entry<String, String>> removeSet = new ArrayList<Map.Entry<String, String>>();
        while (nonMatchedLookup.size() > 0 && match == true) {
            match = false;
            removeSet.clear();
            for (Entry<Map.Entry<String, String>, Map.Entry<String, String>> nonMatched: nonMatchedLookup.entrySet()) {
                Map.Entry<String ,String> obsolete = nonMatched.getKey();            
                Map.Entry<String ,String> replace = nonMatched.getValue();
                String replaceAcc = replace.getValue();
                GOTerm replaceTerm = obsoleteGOClsToReplaceGOTermLookup.get(replaceAcc);
                if (null != replaceTerm) {
                    obsoleteGOClsToReplaceGOTermLookup.put(obsolete.getValue(), replaceTerm);
                    match = true;
                    removeSet.add(obsolete);
                    continue;
                }
            }
            for (Map.Entry<String, String> remove: removeSet) {
                nonMatchedLookup.remove(remove);
            }
        }
        for (Entry<Map.Entry<String, String>, Map.Entry<String, String>> lookup: obsoleteToReplaceLookup.entrySet()) {
            Map.Entry<String, String> obsolete = lookup.getKey();            
            Map.Entry<String, String> replace = lookup.getValue();
            System.out.println("Obsoleted id " + obsolete.getKey() + " for " + obsolete.getValue() + " has been replaced by id " + replace.getKey() + " with " + replace.getValue());
        }
            
            
        if (nonMatchedLookup.size() > 0) {
            for (Entry<Map.Entry<String, String>, Map.Entry<String, String>> nonMatched: nonMatchedLookup.entrySet()) {
                Map.Entry<String, String> obsolete = nonMatched.getKey();            
                Map.Entry<String, String> replace = nonMatched.getValue();
                System.out.println("No match found for GO id " + obsolete.getKey() + " " + obsolete.getValue() + " replaced by " + replace.getKey() + " " + replace.getValue());
            }
        }
        return obsoleteGOClsToReplaceGOTermLookup;
    }
    
    public static GOTerm getReplaceForObs(String obseleteTerm) {
        return obsoleteGOClsToReplaceGOTermLookup.get(obseleteTerm);
    }
    
    // Get list of paint annotations that have references to obsoleted terms and get associated families
    public static void main(String[] args) {
        
    }
}
