/**
 * Copyright 2022 University Of Southern California
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

import com.sri.panther.paintCommon.familyLibrary.EntryType;
import com.sri.panther.paintCommon.familyLibrary.FileNameGenerator;
import com.sri.panther.paintCommon.familyLibrary.LibrarySettings;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import com.sri.panther.paintCommon.util.FileUtils;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class FamilyDomain {
    
    private static DataIO dataIO = DataAccessManager.getInstance().getDataIO();
    private static final String CLASSIFICATION_VERSION_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);
    
    public static final String PROPERTY_SUFFIX_LIB_ROOT = "_lib_root";
    public static final String PROPERTY_SUFFIX_LIB_ENTRY_TYPE = "_entry_type";
    public static final String PROPERTY_SUFFIX_BOOK_TYPE = "_book_type";    

    public static final Pattern NODE_DELIM_NODE_NAME = Pattern.compile("|", Pattern.LITERAL);
    public static final Pattern NODE_DELIM_PROT = Pattern.compile("=", Pattern.LITERAL);
    
    public static final String SUFFIX_ENTRY_TYPE = "_entry_type";
    public static final String SUFFIX_LIB_ROOT = "_lib_root";    

    public static final String REPLACE_STR = "%REPLACE_STR%";

    public static final String PREFIX_COMMENT = "#";
    public static final Pattern SEPARATOR_DOMAIN_INFO = Pattern.compile("\t", Pattern.LITERAL);
    public static final int DOMAIN_INDEX_PROT_ID = 0;
    public static final int DOMAIN_START = 1;
    public static final int DOMAIN_END = 2;
    public static final int DOMAIN_HMM_ACC = 5;
    public static final int DOMAIN_HMM_NAME = 6;

    private String famId;
//    private HashMap<String, String> nodeToTaxonLookup;
    private HashMap<String, String> nodeToProtLookup;
//    private HashMap<String, HashSet<String>> taxonToProtLookup;
    private HashMap<String, HashMap<String, ArrayList<Domain>>> nodeToDomainLookup;
    
    java.text.SimpleDateFormat DF = new java.text.SimpleDateFormat("hh:mm:ss:SSS");    

    public FamilyDomain(String familyAcc) {
        this.famId = familyAcc;
        initNodeToProteinLookup();
        initNodeToDomainLookup();
    }
    
    private void initNodeToProteinLookup() {
        HashMap<String, String> nodeAccToLongGeneName = dataIO.getLongGeneNameLookup(famId, CLASSIFICATION_VERSION_SID);
        if (null == nodeAccToLongGeneName) {
            return;
        }        


        nodeToProtLookup = new HashMap<String, String>();
        for (Entry<String, String> geneEntry: nodeAccToLongGeneName.entrySet()) {
            String longGeneName = geneEntry.getValue();
            String acc = geneEntry.getKey();
            String[] parts = NODE_DELIM_NODE_NAME.split(longGeneName);
            if (2 > parts.length) {
                continue;
            }
            String[] protParts = NODE_DELIM_PROT.split(parts[2]);
            if (protParts.length > 1) {
                nodeToProtLookup.put(acc, protParts[1]);
            }
        }
    }
    
    public void initNodeToDomainLookup() {
        if (null == famId) {
            return;
        }
        
        String uplVersion = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);   
        LibrarySettings libSettings;
        EntryType et = null;


        String msaEntryType = ConfigFile.getProperty(uplVersion + SUFFIX_ENTRY_TYPE);
        if ((null == msaEntryType) || (0 == msaEntryType.length())) {
        } else {
            et = new EntryType();
            et.setEntryType(msaEntryType);
        }

      libSettings = new LibrarySettings(famId, et, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_BOOK_TYPE), ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ROOT));
        String pfamUrl = FileNameGenerator.getPfamDomain(libSettings);
        String[] contents = null;
        try {
            contents = FileUtils.readFileFromURL(new URL(pfamUrl));
            if (null == contents || 0 == contents.length) {
                System.out.println("Did not get any domain information from " + pfamUrl);
                return;
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        HashMap<String, HashMap<String, ArrayList<Domain>>> protToDomainAccToDomainListLookup = new HashMap<String, HashMap<String, ArrayList<Domain>>>();
        for (String part : contents) {
            String[] parts = SEPARATOR_DOMAIN_INFO.split(part);

            if (parts.length <= DOMAIN_HMM_NAME) {
                continue;
            }

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
        }
        
        if (protToDomainAccToDomainListLookup.isEmpty()) {
            System.out.println("No matching information for pfam domain " + pfamUrl);
            return;
        }
        nodeToDomainLookup = new HashMap<String, HashMap<String, ArrayList<Domain>>>();
        for (Entry<String, String> nodeToProt: nodeToProtLookup.entrySet()) {
            String node = nodeToProt.getKey();
            String prot = nodeToProt.getValue();
            HashMap<String, ArrayList<Domain>> domainAccToDomainList = protToDomainAccToDomainListLookup.get(prot);
            if (null != domainAccToDomainList ) {
                nodeToDomainLookup.put(node, domainAccToDomainList);
            }
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
