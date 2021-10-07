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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;


public class TaxonomyHelper implements Serializable {
    private LinkedHashMap<String, Integer> speciesToIndex;
    private LinkedHashMap<String, Integer> termToIndex;
    private int[][] valuesLookup;
    private boolean checkTaxonomy = true;
            
    public TaxonomyHelper(LinkedHashMap<String, Integer> speciesToIndex, LinkedHashMap<String, Integer> termToIndex, int[][] valuesLookup, boolean checkTaxonomy) {
        this.speciesToIndex = speciesToIndex;
        this.termToIndex = termToIndex;
        this.valuesLookup = valuesLookup;
        this.checkTaxonomy = checkTaxonomy;
    }
    
    public Set<String> getSupportedSpecies() {
        return new HashSet(speciesToIndex.keySet());
    }

    public Set<String> getSupportedTerms() {
        return new HashSet(termToIndex.keySet());
    }
    
    public TaxonomyHelper() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }
    
    public boolean isTermAndQualifierValidForSpecies (String term, String species, Set<Qualifier> qSet) {
        if (false == checkTaxonomy) {
            return true;
        }
        if (true == QualifierDif.containsNegative(qSet)) {
            return true;
        }
        return isTermValidForSpecies(term, species, true);
    }
    
    public boolean termAndQualifierValidForSpeciesCheckTaxonomy(String term, String species, Set<Qualifier> qSet) {
        if (true == QualifierDif.containsNegative(qSet)) {
            return true;
        }
        
        return isTermValidForSpecies(term, species, false);        
    }
    
    private boolean isTermValidForSpecies(String term, String species, boolean useTaxonomyFlg) {
        if (true == useTaxonomyFlg) {
            if (false == checkTaxonomy) {
                return true;
            }
        }
        if (null == species) {
            return true;
        }
        if (null == term) {
            return false;
        }
        Integer row  = termToIndex.get(term);
        if (null == row) {
            // For now assume Taxon constraints rules are incomplete. 
            System.out.println("TAXON ERROR - DID NOT FIND TERM " + term);
            return true;
        }
        Integer column = speciesToIndex.get(species);
        if (null == column) {
            System.out.println("TAXON ERROR - DID NOT FIND SPECIES " + species);            
            return false;
        }
        if (row >= valuesLookup.length || column >= valuesLookup[row].length) {
            return false;
        }
        int value = valuesLookup[row][column];
        if (value > 0) {
            return true;
        }
        return false;
    }
        
    public boolean isCheckingTaxonomy() {
        return this.checkTaxonomy;
    }
    
    
//    public ArrayList<String> getValidSpeciesForTerm(String term) {
//        return speciesForTerm(term, true);
//    }
//    
//    public ArrayList<String> getInvalidSpeciesForTerm(String term) {
//        return speciesForTerm(term, false);
//    }
//    
//    private ArrayList<String> speciesForTerm(String term, boolean valid) {
//        if (null == term) {
//            return null;
//        }
//        
//        Integer row  = termToIndex.get(term);
//        if (null == row || row >= valuesLookup.length) {
//            return null;
//        }
//        ArrayList<String> speciesList = new ArrayList<String>();
//        Set<String> speciesSet = speciesToIndex.keySet();
//        for (String species: speciesSet) {
//            Integer column = speciesToIndex.get(species);
//            if (column >= valuesLookup[row].length) {
//                return null;
//            }
//            int value = valuesLookup[row][column];
//            if  (true == valid && value > 0) {
//                if (false == speciesList.contains(species)) {
//                    speciesList.add(species);
//                }
//                continue;
//            }
//            if (false == valid && value <= 0) {
//                if (false == speciesList.contains(species)) {
//                    speciesList.add(species);
//                }
//                continue;                
//            }
//        }
//        return speciesList;
//    }
    
}

