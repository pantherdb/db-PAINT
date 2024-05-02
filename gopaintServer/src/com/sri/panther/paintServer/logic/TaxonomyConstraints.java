/**
 *  Copyright 2023 University Of Southern California
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

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.FileUtils;
import edu.usc.ksom.pm.panther.paintCommon.Organism;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 *
 * Singleton
 */
public class TaxonomyConstraints implements Serializable{
    private static TaxonomyConstraints instance;
    public static final String TAXONOMY_FILE_PATH = ConfigFile.getProperty("taxonomy_constraints");
    public static final Boolean CHECK_TAXONOMY = new Boolean(ConfigFile.getProperty("check_taxomomy"));
        
    private static LinkedHashMap<String, Integer> speciesToIndex;
    private static LinkedHashMap<String, Integer> termToIndex;
    private static int[][] valuesLookup;
    private static TaxonomyHelper taxomomyHelper;
    
    private TaxonomyConstraints() {
        
    }
    
    public static synchronized TaxonomyConstraints getInstance() {
        if (null == instance || null == speciesToIndex || null == termToIndex || null == valuesLookup || null == taxomomyHelper) {
            instance = new TaxonomyConstraints();
            initLookup();
        }
        return instance;
    }
    
    private static void initLookup() {
        try {
            String[] taxonFileContents = FileUtils.readFileFromURL(new URL(TAXONOMY_FILE_PATH));
            int numLines = taxonFileContents.length;
            if (numLines < 2) {
                return;
            }
            String speciesList = taxonFileContents[0];
            speciesList.trim();
            String [] parts = speciesList.split(Constant.STR_TAB);
            // Taxonomy file has combination of short and long names names such as (GOterm	STRCO	MOUSE	Rattus norvegicus	TRIAD	...).
            // Convert to long names.
            // Index 0 is the GO id. So start with next item
            OrganismManager om = OrganismManager.getInstance();
            for (int i = 1; i < parts.length;i++) {
                String org = parts[i];
                Organism o = om.getOrganismForShortName(org);
                if (null == o || null == o.getLongName()) {
                    continue;
                }
                parts[i] = o.getLongName();
            }
            int partsLength = parts.length;
            speciesToIndex = new LinkedHashMap<String, Integer>();
            for (int i = 1; i < partsLength; i++) {
                speciesToIndex.put(parts[i], i);
            }
            
            termToIndex = new LinkedHashMap<String, Integer>();
            valuesLookup = new int[numLines][parts.length];
            for (int i = 1; i < numLines; i++) {
                parts = taxonFileContents[i].split(Constant.STR_TAB);
                if (parts.length < partsLength) {
                    System.out.println("Skipping - Encountered invalid data during parsing of taxonomy constraints file line " + i + " contents " + taxonFileContents[i] + " expected " + partsLength + " entries, found " + parts.length + " entries");
                    continue;
                }
                termToIndex.put(parts[0], i);
                for (int j = 1; j < parts.length; j++) {
                    valuesLookup[i][j] = Integer.parseInt(parts[j]);
                }
            }
            
            taxomomyHelper = new TaxonomyHelper(speciesToIndex, termToIndex, valuesLookup, CHECK_TAXONOMY.booleanValue());
        }
        catch(IOException e) {
            speciesToIndex = null;
            termToIndex = null;
            valuesLookup = null;
            taxomomyHelper = null;
            e.printStackTrace();
        }
    }

    public static TaxonomyHelper getTaxomomyHelper() {
        return taxomomyHelper;
    }
    
    public static void main(String args[]) {
        TaxonomyConstraints tc = TaxonomyConstraints.getInstance();
        TaxonomyHelper th = tc.getTaxomomyHelper();
        boolean valid = th.isTermAndQualifierValidForSpecies("GO:1903097", "Deuterostomia", null);
        valid = th.isTermAndQualifierValidForSpecies("GO:1903097", "Cyanobacteria", null);
        //ArrayList<String> speciesList = th.getValidSpeciesForTerm("GO:0021727");
        //speciesList = th.getInvalidSpeciesForTerm("GO:0021727");
    }
}
