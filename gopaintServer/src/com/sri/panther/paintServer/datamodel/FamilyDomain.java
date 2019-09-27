/**
 * Copyright 2019 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sri.panther.paintServer.datamodel;

import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.logic.OrganismManager;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class FamilyDomain {

    private static DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
    private static final String CLASSIFICATION_VERSION_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);

    public static final Pattern NODE_DELIM_NODE_NAME = Pattern.compile("|", Pattern.LITERAL);
    public static final Pattern NODE_DELIM_PROT = Pattern.compile("=", Pattern.LITERAL);

    public static final String REPLACE_STR = "%REPLACE_STR%";
    public static final String URL_DOMAIN_INFO = ConfigFile.getProperty("domain_info") + "%REPLACE_STR%.tsv";

    public static final String PREFIX_COMMENT = "#";
    public static final Pattern SEPARATOR_DOMAIN_INFO = Pattern.compile("\t", Pattern.LITERAL);
    public static final int DOMAIN_INDEX_PROT_ID = 0;
    public static final int DOMAIN_START = 1;
    public static final int DOMAIN_END = 2;
    public static final int DOMAIN_HMM_ACC = 5;
    public static final int DOMAIN_HMM_NAME = 6;

    private String famId;
    private HashMap<String, String> nodeToTaxonLookup;
    private HashMap<String, String> nodeToProtLookup;
    private HashMap<String, HashSet<String>> taxonToProtLookup;
    private HashMap<String, HashMap<String, ArrayList<Domain>>> nodeToDomainLookup;
    
    java.text.SimpleDateFormat DF = new java.text.SimpleDateFormat("hh:mm:ss:SSS");    

    public FamilyDomain(String famId) {
        this.famId = famId;

        // Setup taxon to list of associated proteins
        initTaxonToProtLookup();
        if (null == taxonToProtLookup) {
            return;
        }

        int numProc = taxonToProtLookup.size();
        if (0 == numProc) {
            return;
        }

        // Create Threads to read from files
        ExecutorService executor = Executors.newFixedThreadPool(numProc);
        HashMap<String, LoadDomainInfo> workerLookup = new HashMap<String, LoadDomainInfo>(numProc);
        System.out.println("Start of get domain info for book " + famId + " at " + DF.format(new java.util.Date(System.currentTimeMillis())));        
        for (Entry<String, HashSet<String>> entry : taxonToProtLookup.entrySet()) {
            LoadDomainInfo domainWorker = new LoadDomainInfo(entry.getKey(), entry.getValue());
            workerLookup.put(entry.getKey(), domainWorker);
            executor.execute(domainWorker);
        }
        executor.shutdown();

        // Wait until all threads finish
        while (!executor.isTerminated()) {

        }
        System.out.println("Finished getting domain info for book " + famId + " at " + DF.format(new java.util.Date(System.currentTimeMillis())));
        nodeToDomainLookup = new HashMap<String, HashMap<String, ArrayList<Domain>>>();
        for (Entry<String, String> entry : nodeToTaxonLookup.entrySet()) {
            String nodeId = entry.getKey();
            String taxonId = entry.getValue();
            LoadDomainInfo domainWorker = workerLookup.get(taxonId);
            String protId = nodeToProtLookup.get(nodeId);
            HashMap<String, ArrayList<Domain>> domainLookup = domainWorker.getDomainInfo(protId);
            if (null != domainLookup) {
                nodeToDomainLookup.put(nodeId, domainLookup);
            }
            
        }
    }

    private void initTaxonToProtLookup() {
        // Get list of organisms and associated proteins for each organism
        HashMap<String, String> geneLookup = dataIO.getLongGeneNameLookup(famId, CLASSIFICATION_VERSION_SID);
        if (null == geneLookup) {
            return;
        }

        taxonToProtLookup = new HashMap<String, HashSet<String>>();
        nodeToTaxonLookup = new HashMap<String, String>();
        nodeToProtLookup = new HashMap<String, String>();
        OrganismManager om = OrganismManager.getInstance();

        for (HashMap.Entry<String, String> entry : geneLookup.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String[] parts = NODE_DELIM_NODE_NAME.split(value);
            if (2 > parts.length) {
                continue;
            }

            Organism o = om.getOrganismForShortName(parts[0]);
            if (null == o) {
                continue;
            }
            String taxonId = o.getTaxonId();
            if (null == taxonId) {
                continue;
            }
            String[] protParts = NODE_DELIM_PROT.split(parts[2]);
            if (protParts.length > 1) {
                nodeToTaxonLookup.put(key, taxonId);
                nodeToProtLookup.put(key, protParts[1]);

                HashSet<String> protList = taxonToProtLookup.get(taxonId);
                if (null == protList) {
                    protList = new HashSet<String>();
                    taxonToProtLookup.put(taxonId, protList);
                }
                protList.add(protParts[1]);
            }
        }
    }

    public class LoadDomainInfo implements Runnable {

        String taxonId;
        HashSet<String> proteinSet;
        HashMap<String, HashMap<String, ArrayList<Domain>>> protToDomainAccToDomainListLookup;

        LoadDomainInfo(String taxonId, HashSet<String> proteinSet) {
            this.taxonId = taxonId;
            this.proteinSet = proteinSet;
        }

        @Override
        public void run() {

//            System.out.println("Start of get taxon info for " + taxonId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));
            protToDomainAccToDomainListLookup = new HashMap<String, HashMap<String, ArrayList<Domain>>>();
            BufferedReader in = null;
            try {

                URL domainInfo = new URL(URL_DOMAIN_INFO.replace(REPLACE_STR, taxonId));
                in = new BufferedReader(new InputStreamReader(domainInfo.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith(PREFIX_COMMENT)) {
                        continue;
                    }
                    String[] parts = SEPARATOR_DOMAIN_INFO.split(inputLine);
                    if (null == parts) {
                        continue;
                    }
                    if (false == proteinSet.contains(parts[0])) {
                        continue;
                    }
                    if (parts.length <= DOMAIN_HMM_NAME) {
                        continue;
                    }
                    try {
                        Domain d = new Domain(parts[DOMAIN_INDEX_PROT_ID], Integer.parseInt(parts[DOMAIN_START]), Integer.parseInt(parts[DOMAIN_END]), parts[DOMAIN_HMM_ACC], parts[DOMAIN_HMM_NAME]);
                        HashMap<String, ArrayList<Domain>> domainAccToDomainListLookup = protToDomainAccToDomainListLookup.get(parts[DOMAIN_INDEX_PROT_ID]);
                        if (null == domainAccToDomainListLookup) {
                            domainAccToDomainListLookup = new HashMap<String, ArrayList<Domain>>();
                            protToDomainAccToDomainListLookup.put(parts[DOMAIN_INDEX_PROT_ID], domainAccToDomainListLookup);
                        }
                        ArrayList<Domain> astDomainList = domainAccToDomainListLookup.get(parts[DOMAIN_HMM_ACC]);
                        if (null == astDomainList) {
                            astDomainList = new ArrayList<Domain>();
                            domainAccToDomainListLookup.put(parts[DOMAIN_HMM_ACC], astDomainList);
                        }
                        astDomainList.add(d);
                        if (1 < astDomainList.size()) {
//                            System.out.println("Here");
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing domain information for " + taxonId + " line " + inputLine);
                        continue;
                    }

                }
                in.close();
            } catch (IOException e) {
                protToDomainAccToDomainListLookup = null;
            } catch (Exception e) {
                protToDomainAccToDomainListLookup = null;
            } finally {
                if (null != in) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        protToDomainAccToDomainListLookup = null;
                    }
                }
            }
        }

        public HashMap<String, ArrayList<Domain>> getDomainInfo(String prot) {
            if (null == prot || null == protToDomainAccToDomainListLookup) {
                return null;
            }
            return protToDomainAccToDomainListLookup.get(prot);
        }
    }

    public HashMap<String, HashMap<String, ArrayList<Domain>>> getNodeToDomainLookup() {
        return nodeToDomainLookup;
    }
    
    public static void main(String args[]) {
        FamilyDomain fd = new FamilyDomain("PTHR10000");
        HashMap<String, HashMap<String, ArrayList<Domain>>> domainLookup = fd.getNodeToDomainLookup();
        System.out.println("Here");
    }

}
