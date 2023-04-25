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
package com.sri.panther.paintServer.filemanager;

import com.sri.panther.paintCommon.familyLibrary.EntryType;
import com.sri.panther.paintCommon.familyLibrary.FileNameGenerator;
import com.sri.panther.paintCommon.familyLibrary.LibrarySettings;
import com.sri.panther.paintCommon.util.FileUtils;
import com.sri.panther.paintServer.datamodel.Organism;
import com.sri.panther.paintServer.logic.OrganismManager;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationNode;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class TreeNodeUtility {
    private static OrganismManager ORGANISM_MANAGER = OrganismManager.getInstance();
    public static final String HEADER_PUBLIC_ID = "'Public_node_id'";
    public static final String HEADER_LONG_GENE_NAME_ID = "'Protein_Id'";

    public static final String HEADER_GENE_SYMBOL = "'gene_symbol'";
    public static final String HEADER_DEFINITION = "'definition'";
    public static final String DELIM_ATTR_TAB = "\t";
    public static final String DELIM_GENE = ",";
    
    public static final String WRAPPER = "'";
    public static HashMap<String, Node> getDefnIdGeneSymbol(String uplVersion, String book) {
        LibrarySettings libSettings = null;
        EntryType et = null;
        String msaEntryType = ConfigFile.getProperty(uplVersion + DataServlet.PROPERTY_SUFFIX_LIB_ENTRY_TYPE);

        if ((null == msaEntryType) || (0 == msaEntryType.length())) {
        } else {
            et = new EntryType();
            et.setEntryType(msaEntryType);
        }
        libSettings = new LibrarySettings(book, et, ConfigFile.getProperty(uplVersion + DataServlet.PROPERTY_SUFFIX_BOOK_TYPE), ConfigFile.getProperty(uplVersion + DataServlet.PROPERTY_SUFFIX_LIB_ROOT));
        String url = FileNameGenerator.getAttrFile(libSettings);
        if (null != url) {
            try {
                String[] contents = FileUtils.readFileFromURL(new URL(url));
                int numEntries = contents.length;
                int numTitleParts = -1;

                int public_id = -1;
                int longId = -1;

                int gene_symbol = -1;
                int definition = -1;
                if (null != contents && 0 != numEntries) {
                    String title = contents[0];
                    if (null != title) {
                        String titleParts[] = title.split(DELIM_ATTR_TAB);
                        numTitleParts = titleParts.length;
                        for (int i = 0; i < numTitleParts; i++) {
                            String header = titleParts[i];
                            if (null != header && header.equalsIgnoreCase(HEADER_PUBLIC_ID)) {
                                public_id = i;
                            }

                            if (null != header && header.equalsIgnoreCase(HEADER_GENE_SYMBOL)) {
                                gene_symbol = i;
                            }
                            if (null != header && header.equalsIgnoreCase(HEADER_DEFINITION)) {
                                definition = i;
                            }
                            if (null != header && header.equalsIgnoreCase(HEADER_LONG_GENE_NAME_ID)) {
                                longId = i;
                            }
                            
                        }
                        if (public_id < 0 || gene_symbol < 0 || definition < 0 || longId < 0) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
                HashMap<String, Node> pubIdToNode = new HashMap<String, Node>();
                for (int i = 0; i < numEntries; i++) {
                    String part = contents[i];
                    String parts[] = part.split(DELIM_ATTR_TAB);
                    if (parts.length < numTitleParts) {
                        return null;
                    }

                    String publicId = parts[public_id];
                    if (null == publicId || 0 == publicId.length()) {
                        return null;
                    }
                    Node n = new Node();
                    NodeStaticInfo nsi = new NodeStaticInfo();
                    n.setStaticInfo(nsi);

                    if (null != publicId && 2 < publicId.length()) {
                        publicId = publicId.substring(1, publicId.length() - 1);
                        nsi.setPublicId(publicId);
                    }
                    pubIdToNode.put(publicId, n);
                    
                    String longGeneId = parts[longId];
                    if (null != longGeneId && 2 < longGeneId.length()) {
                        // This can either be an AN# or long gene id.
                        longGeneId = longGeneId.substring(1, longGeneId.length() - 1);
                        boolean isAn = false;
                        try {
                            String anId = longGeneId.substring(2);
                            Integer.parseInt(anId);
                            isAn = true;
                            nsi.setNodeAcc(book + NodeStaticInfo.DELIM_BOOK_ACC + anId);
                        }
                        catch(NumberFormatException e) {
                           
                        }
                        if (false == isAn) {
                            nsi.setLongGeneName(longGeneId);
                            String shortName = AnnotationNode.getShortSpeciesFromLongName(longGeneId);
                            nsi.setShortOrg(shortName);
                            Organism org = ORGANISM_MANAGER.getOrganismForShortName(shortName);
                            if (null != org) {
                                nsi.setSpeciesConversion(org.getConversion());
                            }
                        }
                    }
                    
                    String definitionStr = parts[definition];
                    if (null != definitionStr && 2 < definitionStr.length()) {
                        definitionStr = definitionStr.substring(1, definitionStr.length() - 1);
                        nsi.setDefinition(definitionStr);
                        nsi.addGeneName(definitionStr);
                    }
                    String geneSymbol = parts[gene_symbol];
                    if (null != geneSymbol && 2 < geneSymbol.length()) {
                        geneSymbol = geneSymbol.substring(1, geneSymbol.length() - 1);
                        String geneSymbols[] = geneSymbol.split(DELIM_GENE);
                        ArrayList<String> symbols = new ArrayList<String>();
                        symbols.addAll(Arrays.asList(geneSymbols));
//                        if (1 < symbols.size()) {
//                            System.out.println("Here");
//                        }                        
                        nsi.setGeneSymbol(symbols);
                    }

                }
                return pubIdToNode;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
