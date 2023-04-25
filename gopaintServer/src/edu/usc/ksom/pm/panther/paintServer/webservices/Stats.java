/**
 * Copyright 2021 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintServer.webservices;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.datamodel.NodeAnnotation;
import com.sri.panther.paintServer.datamodel.Organism;
import com.sri.panther.paintServer.logic.OrganismManager;
import com.sri.panther.paintServer.util.ConfigFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import com.sri.panther.paintServer.tools.ServiceUtils;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;


public class Stats {
    public static final String CLASSIFICATION_VERSION_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);
    private static DataIO dataIO = DataAccessManager.getInstance().getDataIO();
    
    public static final String ELEMENT_SEARCH = "search";
    public static final String ELEMENT_SEARCH_TYPE = "search_type";
    public static final String ELEMENT_PARAMETERS = "parameters";
    public static final String ELEMENT_TAXON_ID = "taxon_id";
    public static final String ELEMENT_ID = "id";
    public static final String ELEMENT_NODE_LIST = "node_list";
    public static final String ELEMENT_NODE = "node";
    public static final String ELEMENT_ELAPSED_TIME = "elapsed_time";
    public static final String ELEMENT_ASPECT_MF_COUNT = "Function_count";
    public static final String ELEMENT_ASPECT_BP_COUNT = "Process_count";
    public static final String ELEMENT_ASPECT_CC_COUNT = "Component_count"; 
    public static final String ELEMENT_GENOMES = "genomes";
    public static final String ELEMENT_GENOME = "genome";
    public static final String ELEMENT_NAME = "name"; 
    public static final String ELEMENT_LONG_NAME = "long_name"; 
    public static final String ELEMENT_COMMON_NAME = "common_name";   
    public static final String ELEMENT_SHORT_NAME = "short_name"; 
    public static final String ELEMENT_LOGICAL_RANK = "logical_rank";
    public static final String ELEMENT_REFERENCE_GENOME = "reference_genome";    
    
    
    
    
    
    public static final String SEARCH_TYPE_EXPERIMENTAL_ANNOTS_BY_TAXON = "Positive Experimental annots by Taxon";
    public static final String SEARCH_TYPE_SUPPORTED_GENOMES = "Supported genomes";
    

    private static final String MS = " milliseconds.";
   

    
    
    
    public static int getNumBooks() {
        ArrayList<Book> books = dataIO.getAllBooks(CLASSIFICATION_VERSION_SID);
        if (null == books) {
            return 0;
        }
        return books.size();
    }
    
    public Hashtable<String, Book> getBookLookup() {
        return dataIO.getListOfBooksAndStatus(CLASSIFICATION_VERSION_SID);
    }
    
    public static int getManuallyCuratedBooks() {
        Hashtable<String, Book> bookLookup = dataIO.getListOfBooksAndStatus(CLASSIFICATION_VERSION_SID);
        int counter = 0;
        for (Book book: bookLookup.values()) {
            if (Book.CURATION_STATUS_MANUALLY_CURATED == book.getCurationStatus()) {
                counter++;
            }
        }
        return counter;
    }
    
    public static int getPartiallyCuratedBooks() {
        Hashtable<String, Book> bookLookup = dataIO.getListOfBooksAndStatus(CLASSIFICATION_VERSION_SID);
        int counter = 0;
        for (Book book: bookLookup.values()) {
            if (Book.CURATION_STATUS_PARTIALLY_CURATED == book.getCurationStatus()) {
                counter++;
            }
        }
        return counter;
    }
    public static final String FORMAT_TXT = "txt";
    public static final String DELIM_TXT_OUTPUT = "\t";
    public static final String NEWLINE = "\n";
    public static final String HEADER = "#Id" + DELIM_TXT_OUTPUT + "Function Count"+ DELIM_TXT_OUTPUT + "Process Count" + DELIM_TXT_OUTPUT + "Cellular Component Count" + NEWLINE;
    
    public static String getExperimentalEvidence(String taxon, String format) {
        long start = System.currentTimeMillis();
        HashMap<String, NodeAnnotation> lookup = dataIO.getExperimentalAnnotsByTaxonId(CLASSIFICATION_VERSION_SID, taxon);
        if (null == lookup) {
            return null;
        }
        long duration = System.currentTimeMillis() - start;
        if (null == format) {
            return outputXMLForAnnots(taxon, lookup, duration);
        }
        else if (FORMAT_TXT.equalsIgnoreCase(format)) {
            StringBuffer sb = new StringBuffer();
            sb.append(HEADER);
            for (String id: lookup.keySet()) {
                NodeAnnotation na = lookup.get(id);
                int total = na.getTotalAnnotCounts();
                if (0 == total) {
                    continue;
                }
                sb.append(id);
                sb.append(DELIM_TXT_OUTPUT);
                sb.append(na.getMfCount());
                sb.append(DELIM_TXT_OUTPUT);
                sb.append(na.getBpCount());
                sb.append(DELIM_TXT_OUTPUT);
                sb.append(na.getCcCount());
                sb.append(NEWLINE);                              
            }
            return sb.toString();
        }
        return null;
        
    }
    
    public static String outputXMLForAnnots(String taxon, HashMap<String, NodeAnnotation> lookup, long duration) {
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element taxonId = doc.createElement(ELEMENT_TAXON_ID);
            Text id_text = doc.createTextNode(taxon);
            taxonId.appendChild(id_text);

            Element searchTypeElem = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text searchTypeText = doc.createTextNode(SEARCH_TYPE_EXPERIMENTAL_ANNOTS_BY_TAXON);
            searchTypeElem.appendChild(searchTypeText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(taxonId);
            parameters.appendChild(searchTypeElem);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
            root.appendChild(nodeList);
            
            for (String id: lookup.keySet()) {
                NodeAnnotation na = lookup.get(id);
                int total = na.getTotalAnnotCounts();
                if (0 == total) {
                    continue;
                }
                Element node = doc.createElement(ELEMENT_NODE);
                nodeList.appendChild(node);
                Element nodeId = doc.createElement(ELEMENT_ID);
                node.appendChild(nodeId);
                nodeId.appendChild(doc.createTextNode(id));
                if (0 != na.getMfCount()) {
                    Element mf = doc.createElement(ELEMENT_ASPECT_MF_COUNT);
                    node.appendChild(mf);
                    mf.appendChild(doc.createTextNode(Integer.toString(na.getMfCount())));
                }
                if (0 != na.getBpCount()) {
                    Element bp = doc.createElement(ELEMENT_ASPECT_BP_COUNT);
                    node.appendChild(bp);
                    bp.appendChild(doc.createTextNode(Integer.toString(na.getBpCount())));
                }
                if (0 != na.getCcCount()) {
                    Element cc = doc.createElement(ELEMENT_ASPECT_CC_COUNT);
                    node.appendChild(cc);
                    cc.appendChild(doc.createTextNode(Integer.toString(na.getCcCount())));
                }                
            }
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
        }
        catch(Exception e) {
             e.printStackTrace();
             return null;
        }
    }
    
    public static String getXMLForSupportedOrgs() {
        long start = System.currentTimeMillis();
        ArrayList<Organism> orgList = OrganismManager.getInstance().getOrgList();
        if (null == orgList) {
            return null;
        }
        long duration = System.currentTimeMillis() - start;
        
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            
            Element searchTypeElem = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text searchTypeText = doc.createTextNode(SEARCH_TYPE_SUPPORTED_GENOMES);
            searchTypeElem.appendChild(searchTypeText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(searchTypeElem);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element genomes = doc.createElement(ELEMENT_GENOMES);
            root.appendChild(genomes);
            
            for (Organism o: orgList) {
                Element orgElem = doc.createElement(ELEMENT_GENOME);
                genomes.appendChild(orgElem);
                ServiceUtils.appendElement(doc, orgElem, ELEMENT_TAXON_ID, o.getTaxonId());
                ServiceUtils.appendElement(doc, orgElem, ELEMENT_NAME, o.getName());
                ServiceUtils.appendElement(doc, orgElem, ELEMENT_SHORT_NAME, o.getShortName());
                ServiceUtils.appendElement(doc, orgElem, ELEMENT_LONG_NAME, o.getLongName());
                ServiceUtils.appendElement(doc, orgElem, ELEMENT_COMMON_NAME, o.getCommonName());
                ServiceUtils.appendElement(doc, orgElem, ELEMENT_REFERENCE_GENOME, Boolean.toString(o.isRefGenome()));
            }
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
        }
        catch(Exception e) {
             e.printStackTrace();
             return null;
        }
    }    
    
    
    public static String getAllAnnotsForTaxon(String taxon, String format) {
        long start = System.currentTimeMillis();
        HashMap<String, NodeAnnotation> lookup = dataIO.getAllAnnotsByTaxonId(CLASSIFICATION_VERSION_SID, taxon);
        if (null == lookup) {
            return null;
        }
        long duration = System.currentTimeMillis() - start;
        if (null == format) {
            return outputXMLForAnnots(taxon, lookup, duration);
        }
        else if (FORMAT_TXT.equalsIgnoreCase(format)) {
            StringBuffer sb = new StringBuffer();
            sb.append(HEADER);
            for (String id: lookup.keySet()) {
                NodeAnnotation na = lookup.get(id);
                int total = na.getTotalAnnotCounts();
                if (0 == total) {
                    continue;
                }
                sb.append(id);
                sb.append(DELIM_TXT_OUTPUT);
                sb.append(na.getMfCount());
                sb.append(DELIM_TXT_OUTPUT);
                sb.append(na.getBpCount());
                sb.append(DELIM_TXT_OUTPUT);
                sb.append(na.getCcCount());
                sb.append(NEWLINE);                              
            }
            return sb.toString();
        }
        return null;        
    }
}
