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
import edu.usc.ksom.pm.panther.paintServer.webservices.WSConstants;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class DataValidationManager {
    private static DataValidationManager instance;
    private static List<Book> books = null;
    private static HashMap<String, HashSet<String>> booksWithIncompletTaxonLookup;   // Book with list of organisms that are not found in taxonomy lookup
    private static HashSet<String> termsNotSupportedByTaxonConstrains;
    private DataValidationManager() {
        
    }
    
    public synchronized static DataValidationManager getInstance() {
        if (null != instance) {
            return instance;
        }
        instance = new DataValidationManager();
        initLookup();
        return instance;
    }
    
    private static void initLookup() {
        DataIO dataIO = DataAccessManager.getInstance().getDataIO();
        books = dataIO.getBooksWithOrgs(WSConstants.PROPERTY_CLS_VERSION);
        if (null == books) {
            return;
        }
        
        TaxonomyHelper th =  TaxonomyConstraints.getInstance().getTaxomomyHelper();
        Set<String> taxonSpeciesList = th.getSupportedSpecies();
        booksWithIncompletTaxonLookup = new HashMap<String, HashSet<String>>();
        for (Book b: books) {
            HashSet<String> bookOrgSet = b.getOrgSet();
            if (null == bookOrgSet) {
                continue;
            }
            for (String org: bookOrgSet) {
                if (false == taxonSpeciesList.contains(org)) {
                    String id = b.getId();
                    HashSet<String> orgs = booksWithIncompletTaxonLookup.get(id);
                    if (null == orgs) {
                        orgs = new HashSet<String>();
                        booksWithIncompletTaxonLookup.put(id, orgs);
                    }
                    orgs.add(org);
                }
            }
        }
        GOTermHelper gth = CategoryLogic.getInstance().getGOTermHelper();
        Set<String> supportedTerms = th.getSupportedTerms();
        ArrayList<GOTerm> allTerms = gth.getAllTerms();
        for (GOTerm term: allTerms) {
            String termStr = term.getAcc();
            List<GOTerm> parents = term.getParents();
            if (null == parents) {
                continue;
            }
            if (gth.NON_ALLOWED_TERM_SET.contains(termStr)) {
                continue;
            }
            if (supportedTerms.contains(termStr)) {
                continue;
            }
            else {
                if (null == termsNotSupportedByTaxonConstrains) {
                    termsNotSupportedByTaxonConstrains = new HashSet<String>();
                }
                termsNotSupportedByTaxonConstrains.add(termStr);
            }
        }
    }
    
    public boolean canAnnotTerms(List<String> terms) {
        if (books == null) {
            return false;
        }
        if (null == terms) {
            return true;
        }        
        if (null == termsNotSupportedByTaxonConstrains) {
            return true;
        }
        for (String term: terms) {
            if (termsNotSupportedByTaxonConstrains.contains(term)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean existsBooksWithOrgInfo() {
        if (null == books) {
            return false;
        }
        return true;
    }
    
    public HashMap<String, HashSet<String>> getBooksWithIncompleteTaxonInfo() {
        if (null == booksWithIncompletTaxonLookup) {
            return null;
        }
        return (HashMap<String, HashSet<String>>)(booksWithIncompletTaxonLookup.clone());
    }
    
    public Set<String> getTermsNotSupportedByTaxonConstraints() {
        if (null == termsNotSupportedByTaxonConstrains) {
            return null;
        }
        return (HashSet<String>)(termsNotSupportedByTaxonConstrains.clone());
    }
    
}
