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
import java.util.LinkedHashMap;
import java.util.Set;


public class TaxonomyHelper implements Serializable {
    private LinkedHashMap<String, Integer> speciesToIndex;
    private LinkedHashMap<String, Integer> termToIndex;
    private int[][] valuesLookup;
    
    public TaxonomyHelper(LinkedHashMap<String, Integer> speciesToIndex, LinkedHashMap<String, Integer> termToIndex, int[][] valuesLookup) {
        this.speciesToIndex = speciesToIndex;
        this.termToIndex = termToIndex;
        this.valuesLookup = valuesLookup;
    }

    public TaxonomyHelper() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean isTermValidForSpecies(String term, String species) {
        if (null == term || null == species) {
            return false;
        }
        Integer row  = termToIndex.get(term);
        if (null == row) {
            // New version does not define terms for which all organisms are valid
            return true;
//            System.out.println("TAXON ERROR - DID NOT FIND TERM " + term);
//            return false;
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
    
    public ArrayList<String> getValidSpeciesForTerm(String term) {
        return speciesForTerm(term, true);
    }
    
    public ArrayList<String> getInvalidSpeciesForTerm(String term) {
        return speciesForTerm(term, false);
    }
    
    private ArrayList<String> speciesForTerm(String term, boolean valid) {
        if (null == term) {
            return null;
        }
        
        Integer row  = termToIndex.get(term);
        if (null == row || row >= valuesLookup.length) {
            return null;
        }
        ArrayList<String> speciesList = new ArrayList<String>();
        Set<String> speciesSet = speciesToIndex.keySet();
        for (String species: speciesSet) {
            Integer column = speciesToIndex.get(species);
            if (column >= valuesLookup[row].length) {
                return null;
            }
            int value = valuesLookup[row][column];
            if  (true == valid && value > 0) {
                if (false == speciesList.contains(species)) {
                    speciesList.add(species);
                }
                continue;
            }
            if (false == valid && value <= 0) {
                if (false == speciesList.contains(species)) {
                    speciesList.add(species);
                }
                continue;                
            }
        }
        return speciesList;
    }
    
}

