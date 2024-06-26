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
package com.sri.panther.paintServer.logic;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.datamodel.ClassificationVersion;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;
import java.util.HashMap;
import java.util.HashSet;


public class FamilyManager {
    private static FamilyManager instance;
    private static HashMap<String, Book> bookLookup = null;
    private static HashMap<String, HashSet<String>> orgLookup = null;
    
    private FamilyManager() {
        
    }
    
    public static synchronized FamilyManager getInstance() {
        if (null == instance) {
            initInfo();
        }
        return instance;
    }
    
    private static void initInfo() {
        ClassificationVersion cv = VersionManager.getInstance().getClsVersion();
        DataIO dataIO = DataAccessManager.getInstance().getDataIO();
        bookLookup = dataIO.getLeafCountsForFamily(cv.getId());
        
        // Get organism information for leaves
        
        orgLookup = dataIO.getSpeciesForFamily(cv.getId());
        instance = new FamilyManager();
        

    }

    public HashMap<String, Book> getBookLookup() {
        return bookLookup;
    }
    
    public HashMap<String, HashSet<String>> getOrgLookup() {
        return orgLookup;
    }
    
}
