/**
 *  Copyright 2018 University Of Southern California
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
package com.usc.panther.paintServer.webservices;

import edu.usc.ksom.pm.panther.paintCommon.AnnotationNode;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationHelper;
import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.StringUtils;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.database.DataServer;
import com.sri.panther.paintServer.database.DataServerManager;

import com.sri.panther.paintServer.datamodel.Organism;
import com.sri.panther.paintServer.logic.CategoryLogic;
import com.sri.panther.paintServer.logic.OrganismManager;
import com.sri.panther.paintServer.servlet.Client2Servlet;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.IWith;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.util.ArrayList;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.forester.io.parsers.phyloxml.PhyloXmlDataFormatException;
import org.forester.io.parsers.phyloxml.PhyloXmlUtil;
import org.forester.phylogeny.Phylogeny;

import org.forester.phylogeny.PhylogenyNode;

import org.forester.phylogeny.data.Accession;
import org.forester.phylogeny.data.Annotation;
import org.forester.phylogeny.data.Event;
import org.forester.phylogeny.data.Identifier;
import org.forester.phylogeny.data.NodeData;

import org.forester.phylogeny.data.PropertiesMap;
import org.forester.phylogeny.data.Property;
import org.forester.phylogeny.data.Sequence;
import org.forester.phylogeny.data.Taxonomy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class FamilyUtil {
    private static final String ELEMENT_SEARCH = "search";
    private static final String ELEMENT_SEARCH_TYPE = "search_type";
    private static final String ELEMENT_SEARCH_FAMILY = "search_family";
    private static final String ELEMENT_SEARCH_NODE = "search_node";
    private static final String ELEMENT_FAMILY_LOOKUP = "family_lookup";
    private static final String ELEMENT_SEARCH_FAMILY_ID = "search_family_id";
    private static final String ELEMENT_SEARCH_FAMILY_NAMES = "search_family_names";
    private static final String ELEMENT_PARAMETERS = "parameters";
    private static final String ELEMENT_ID = "id";
    private static final String ELEMENT_TYPE = "type";
    
    private static final String ELEMENT_ELAPSED_TIME = "elapsed_time";
    private static final String MS = " milliseconds.";
    
    private static final String ELEMENT_NODE_LIST = "node_list";
    private static final String ELEMENT_NODE = "node";
    private static final String ELEMENT_NODES = "nodes";
    private static final String ELEMENT_ANNOTATION_LIST = "annotation_list";
    private static final String ELEMENT_ANNOTATION = "annotation";
    private static final String ELEMENT_WITH_LIST = "with_list";    
    private static final String ELEMENT_WITH = "with";
    private static final String ELEMENT_CODE = "code";
    private static final String ELEMENT_ANNOTATION_NODE = "annotation_node";
    private static final String ELEMENT_ANNOTATION_ACCESSION = "accession";
    private static final String ELEMENT_ANNOTATION_ID = "annotation_id";
    private static final String ELEMENT_ANNOTATION_PUBLIC_ID = "public_id";
    private static final String ELEMENT_NODE_PUBLIC_ID = "public_id";
    private static final String ELEMENT_ANNOTATION_PARENT = "parent";
    private static final String ELEMENT_FAMILY = "family";
    private static final String ELEMENT_FAMILY_ACCESSION = "family_accession";
    private static final String ELEMENT_FAMILY_NAME = "family_name";
    private static final String ELEMENT_FAMILY_ID = "family_id";
    private static final String ELEMENT_FAMILY_COMMENT = "family_comment";
    private static final String ELEMENT_FAMILY_ANNOTATION_INFO_OTHER = "family_annot_info_other";
    private static final String ELEMENT_FAMILY_ANNOTATION_INFO_PAINT = "family_annot_info_paint";    
    private static final String ELEMENT_NODE_NAME = "node_name";
    private static final String ELEMENT_NODE_SPECIES = "species";
    private static final String ELEMENT_REFERENCE_SPECIATION_EVENT= "reference_speciation_event";
    private static final String ELEMENT_GENE_SYMBOL = "gene_symbol";
    private static final String ELEMENT_GENE_NAME = "gene_name";
    private static final String ELEMENT_SUBFAMILY_ID = "subfamily_id";
    private static final String ELEMENT_SUBFAMILY_NAME = "subfamily_name";
    private static final String ELEMENT_ANNOTATION_NODE_TYPE = "node_type";
    private static final String ELEMENT_ANNOTATION_EVENT_TYPE = "event_type";
    private static final String ELEMENT_ANNOTATION_BRANCH_LENGTH = "branch_length";
    private static final String ELEMENT_ANNOTATION_SEQUENCE = "sequence";
    private static final String ELEMENT_CHILDREN = "children";
//    private static final String ELEMENT_SEQUENCE_LIST = "sequence_list";
//    private static final String ELEMENT_SEQUENCE_INFO = "sequence_info";
//    private static final String ELEMENT_FULL_SEQUENCE = "full_sequence";
    
    private static final String ELEMENT_TERM = "term";
    private static final String ELEMENT_TERM_NAME = "term_name";
    private static final String ELEMENT_TERM_ASPECT = "term_aspect";
    private static final String ELEMENT_EVIDENCE_CODE = "evidence_code";
    private static final String ELEMENT_ANNOT_FROM_PAINT = "annotation_from_paint";
    private static final String ELEMENT_ANNOT_DIRECT = "annotation_direct";
    private static final String ELEMENT_ANNOT_WITH_LIST = "annotation_with_list";
//    private static final String ELEMENT_ANNOT_WITH = "annotation_with";
    
    private static final String ELEMENT_ANNOT_WITH_ANNOTATION_TO_NODE = "annotation_with_annotation_to_node";
    private static final String ELEMENT_ANNOT_WITH_DB_REFERENCE = "annotation_with_db_reference";    
    private static final String ELEMENT_ANNOT_WITH_NODE = "annotation_with_node"; 
        
    private static final String ELEMENT_QUALIFIER_LIST = "qualifier_list";
    private static final String ELEMENT_QUALIFIER = "qualifier";    

    private static final String NODE_TYPE_ROOT = "ROOT";
    private static final String NODE_TYPE_INTERNAL = "INTERNAL";
    private static final String NODE_TYPE_EXTANT = "EXTANT";
    
    private static final String EVENT_TYPE_DUPLICATION = "DUPLICATION";
    private static final String EVENT_TYPE_HORIZONTAL_TRANSFER = "HORIZONTAL_TRANSFER";
    private static final String EVENT_TYPE_SPECIATION = "SPECIATION";
    
    public static final String PROVIDER_PANTHER = "PANTHER";
    public static final String TYPE_STRING = "xsd:string";
    public static final String REF_SF_ID = "PANTHER:subfamilyId";
    public static final String REF_SF_NAME = "PANTHER:subfamilyName";
//    public static final String REF_AN_ID = "PANTHER:annotationId";
    public static final String REF_LONG_ID = "PANTHER:longId";
    public static final String REF_PUBLIC_ID = "PANTHER:id";
    
    
    public static final String PROVIDER_NCBI = "NCBI";
    public static final String DELIM_PROVIDER = ":";
    
    public static final String DELIM_PROTEIN_INFO = "=";
    public static final String DELIM_DB_REF = ":";   
    public static final String STR_EMPTY = "";
    public static final String DELIM_EV_CODE = ",";
    
    private static OrganismManager organismManager = OrganismManager.getInstance();
    private static GOTermHelper goTermHelper = CategoryLogic.getInstance().getGOTermHelper();
    
    public static String getFamilyInfo(String id, String database, String uplVersion, String searchType) {
        
        if (null == database) {
            database = WSConstants.PROPERTY_DB_STANDARD;
        }
        if (null == uplVersion) {
            uplVersion = WSConstants.PROPERTY_CLS_VERSION;
        }
        
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds || null == searchType) {
            return null;
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_GENERAL)) {
            return getXmlForFamily(id, uplVersion, database);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_PHYLOXML)) {
            return getPhyloXMLForFamily(id, uplVersion, database);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_GENERAL_FROM_NODE)) {
            return getXmlForFamilyFromNodeId(id, uplVersion, database);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_LOOKUP_NODE_ID)) {
            return getXmlForFamilyNodeLookup(id, uplVersion, database);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_ID)) {
            return getXmlFamilyIdFromNodeId(id, uplVersion, database);
        }
        if(true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_NAMES_LOOKUP)) {
            return getXmlFamilyNamesLookup(id, uplVersion, database);
        }
        if(true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_DIRECT_ANNOTATIONS)) {
            return getXmlFamilyDirectAnnotations(id, uplVersion);
        }
        if(true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_DIRECT_ANNOTATIONS)) {
            return getXmlFamilyStructureDirectAnnotations(id, uplVersion);
        }
        if(true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS)) {
            return getXmlFamilyStructureAllAnnotations(id, uplVersion);
        }        

        if(true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_COMMENT)) {
            return getXmlFamilyComment(id, uplVersion);
        }
        
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_ANNOTATION_INFO)) {
            return getXMLAnnotationInfo(id, uplVersion);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_AGG_FAMILY_ANNOTATION_INFO)) {
            return getXMLAggAnnotationInfo(id, uplVersion);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_FAMILY_EVIDENCE_INFO)) {
            return getXMLOtherEvdnce(id, uplVersion);
        }
        

        return null;
    }
    
    private static String getXmlFamilyComment(String familyId, String uplVersion) {
        ArrayList<Integer> commentArray = new ArrayList<Integer>();
        long timeStart = System.currentTimeMillis();        
        DataIO di = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        String comment = null;
        try {
            comment = di.getFamilyComment(familyId, uplVersion, commentArray);
        }
        catch(Exception e) {
            
        }
        long duration = System.currentTimeMillis() - timeStart;
        return outputFamliyComment(comment, familyId, duration, WSConstants.SEARCH_TYPE_FAMILY_COMMENT);
    }
    
   private static String getXmlFamilyStructureAllAnnotations(String familyId, String uplVersion) {
        HashMap<String, Node> nodeLookup = new HashMap<String, Node>();
        long timeStart = System.currentTimeMillis();
        TreeLogic tl = new TreeLogic();
        tl.setTreeStructure(familyId, uplVersion, nodeLookup);
        Vector msaInfo = (Vector) Client2Servlet.getMSA(familyId, uplVersion);
        MSAUtil.parsePIRForNonGO(msaInfo, new Vector(tl.getIdToNodeTbl().values()));
        DataIO di = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        di.getGeneInfo(familyId, uplVersion, nodeLookup);
        String familyName = null;
        try {
            familyName = di.getFamilyName(familyId, uplVersion);
            di.getAnnotationNodeLookup(familyId, uplVersion, nodeLookup);   // This will get all nodes in tree
        }
        catch(Exception e) {
            
        }
       
        
        Hashtable<String, AnnotationNode> nodeTbl = tl.getIdToNodeTbl();        // The key does not have the family id plus ":".  Create a copy of table with
                                                                                // key that contains familyid plus ":".  Also set the associated node structure
        Hashtable<String, AnnotationNode> accToAnnotNodeLookup = new Hashtable<String, AnnotationNode>(nodeTbl.size());
        if (null != nodeTbl) {
            for (String nodeAcc: nodeTbl.keySet()) {
                AnnotationNode an = nodeTbl.get(nodeAcc);
                accToAnnotNodeLookup.put(an.getAccession(), an);
                an.setNode(nodeLookup.get(an.getAccession()));
            }
        }

        try {
            HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>> annotToPosWithLookup = new HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>>();
            StringBuffer errorBuf = new StringBuffer();
            StringBuffer paintErrBuf = new StringBuffer();
            HashSet<String> removeSet = new HashSet<String>();
            HashSet<String> modifySet = new HashSet<String>();
            di.getFullGOAnnotations(familyId, uplVersion, nodeLookup, annotToPosWithLookup, errorBuf, paintErrBuf, removeSet, modifySet, false);
        }
        catch(Exception e) {
            
        }
        // Keep list of non-propagated annotations
        HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> nonPropAnnotSet = new HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation>();
        for (Node n: nodeLookup.values()) {
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            nonPropAnnotSet.addAll(annotList);
        }
        try {
            di.addPruned(familyId, uplVersion, nodeLookup);
        }
        catch (Exception e) {
            
        }
        AnnotationHelper.propagateAndFixAnnotationsForBookOpen(familyId, tl.getRoot(), accToAnnotNodeLookup);
        
        long duration = System.currentTimeMillis() - timeStart;
        
        return outputFamilyStructureAnnotations(tl, nodeLookup, nonPropAnnotSet, familyId, familyName, duration, WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS);
    }
    
    
    
    
    private static String getXmlFamilyStructureDirectAnnotations(String familyId, String uplVersion) {
        HashMap<String, Node> nodeLookup = new HashMap<String, Node>();
        long timeStart = System.currentTimeMillis();
        TreeLogic tl = new TreeLogic();
        tl.setTreeStructure(familyId, uplVersion, nodeLookup);
        DataIO di = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        String familyName = null;
        try {
            familyName = di.getFamilyName(familyId, uplVersion);
            di.getAnnotationNodeLookup(familyId, uplVersion, nodeLookup);   // This will get all nodes in tree
        }
        catch (Exception e) {
            
        }
        
        Hashtable<String, AnnotationNode> nodeTbl = tl.getIdToNodeTbl();        // The key does not have the family id plus ":".  Create a copy of table with
                                                                                // key that contains familyid plus ":".  Also set the associated node structure
        Hashtable<String, AnnotationNode> accToAnnotNodeLookup = new Hashtable<String, AnnotationNode>(nodeTbl.size());
        for (String nodeAcc: nodeTbl.keySet()) {
            AnnotationNode an = nodeTbl.get(nodeAcc);
            accToAnnotNodeLookup.put(an.getAccession(), an);
            an.setNode(nodeLookup.get(an.getAccession()));
        }
        
        di.getFullPAINTAnnotations(familyId, uplVersion, nodeLookup);
        // Keep list of non-propagated annotations
        HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> nonPropAnnotSet = new HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation>();
        for (Node n: nodeLookup.values()) {
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            nonPropAnnotSet.addAll(annotList);
        }        
        long duration = System.currentTimeMillis() - timeStart;
        
        return outputFamilyStructureAnnotations(tl, nodeLookup, nonPropAnnotSet, familyId, familyName, duration, WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_DIRECT_ANNOTATIONS);
    }
    
    private static String getXmlFamilyDirectAnnotations(String familyId, String uplVersion) {
        HashMap<String, Node> nodeLookup = new HashMap<String, Node>();
        long timeStart = System.currentTimeMillis();        
        DataIO di = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        di.getFullPAINTAnnotations(familyId, uplVersion, nodeLookup);
        // Keep list of non-propagated annotations
        HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> nonPropAnnotSet = new HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation>();
        for (Node n: nodeLookup.values()) {
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            nonPropAnnotSet.addAll(annotList);
        }
        
        String familyName = null;
        try {
            familyName = di.getFamilyName(familyId, uplVersion);
        }
        catch (Exception e) {
            
        }
        long duration = System.currentTimeMillis() - timeStart;
        
        return outputFamilyDirectAnnotations(nodeLookup, nonPropAnnotSet, familyId, familyName, duration, WSConstants.SEARCH_TYPE_FAMILY_DIRECT_ANNOTATIONS);
    }


    private static String getXmlForFamily(String familyId, String uplVersion, String database) {
        // Get time to get information
        long timeStart = System.currentTimeMillis();
        TreeLogic tl = new TreeLogic();
        tl.setTreeInfoFromFamilyId(familyId, uplVersion, database);
        AnnotationNode treeRoot = tl.getRoot();
        long duration = System.currentTimeMillis() - timeStart;

        return outputFamilyInfo(treeRoot, familyId, familyId, tl.familyName, duration, ELEMENT_SEARCH_FAMILY);
        
    }
    
    private static String getPhyloXMLForFamily(String familyId, String uplVersion, String database) {
        // Get time to get information
        long timeStart = System.currentTimeMillis();
        TreeLogic tl = new TreeLogic();
        tl.setTreeInfoFromFamilyId(familyId, uplVersion, database);
        long duration = System.currentTimeMillis() - timeStart;
        System.out.println("Took " + duration + " ms.");
        return outputPhyloXML(tl);
        
    }
    
    private static String getXmlForFamilyFromNodeId(String nodeId, String uplVersion, String database) {
        // Get time to get information
        long timeStart = System.currentTimeMillis();
        TreeLogic tl = new TreeLogic();
        tl.setTreeInfoFromNodeId(nodeId, uplVersion, database);
        AnnotationNode treeRoot = tl.getRoot();
        long duration = System.currentTimeMillis() - timeStart;

        return outputFamilyInfo(treeRoot, nodeId, tl.familyId, tl.familyName, duration, ELEMENT_SEARCH_NODE);   
    }
    
    public static String getXmlFamilyIdFromNodeId(String nodeId, String uplVersion, String database) {
        // Get time to get information
        long timeStart = System.currentTimeMillis();
        String familyId = TreeLogic.getFamilyIdFromNodeId(nodeId, uplVersion, database);
        long duration = System.currentTimeMillis() - timeStart;

        return outputXmlFamilyId(nodeId, familyId, duration, ELEMENT_SEARCH_FAMILY_ID);   
    }
    
    private static String outputXmlFamilyId(String nodeId, String familyId, long duration, String searchtype) {

        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(nodeId);
            id.appendChild(id_text);
            
            
            Element searchString = doc.createElement(searchtype);
            Text searchStringText = doc.createTextNode(nodeId);
            searchString.appendChild(searchStringText);


            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            
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

    
    private static String getXmlForFamilyNodeLookup(String familyId, String uplVersion, String database) {
        // Get time to get information
        long timeStart = System.currentTimeMillis();
        Hashtable <String, String> nodeLookupTbl  = TreeLogic.getFamilyLookup(familyId, uplVersion, database); 
        long duration = System.currentTimeMillis() - timeStart;
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(familyId);
            id.appendChild(id_text);
            
            
            Element searchString = doc.createElement(ELEMENT_FAMILY_LOOKUP);
            Text searchStringText = doc.createTextNode(familyId);
            searchString.appendChild(searchStringText);


            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            
            if (null != nodeLookupTbl) {
                int size = nodeLookupTbl.size();
                if (0 < size) {
                    Element nodesElement = doc.createElement(ELEMENT_NODES);
                    root.appendChild(nodesElement);
                    
                    Enumeration<String> keys = nodeLookupTbl.keys();
                    while (keys.hasMoreElements()) {
                        String nodeId = keys.nextElement();
                        String value = nodeLookupTbl.get(nodeId);

                        Element annotationNodeElement = doc.createElement(ELEMENT_ANNOTATION_NODE);
                        
                        Element annotationId = doc.createElement(ELEMENT_ANNOTATION_ACCESSION);
                        Text accession_text = doc.createTextNode(nodeId);
                        annotationId.appendChild(accession_text);
                        
                        Element publicId = doc.createElement(ELEMENT_ANNOTATION_PUBLIC_ID);
                        Text publicId_text = doc.createTextNode(value);
                        publicId.appendChild(publicId_text);
                        nodesElement.appendChild(annotationNodeElement);
                        annotationNodeElement.appendChild(annotationId);
                        annotationNodeElement.appendChild(publicId);
                    }                    
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
    
    private static String getXmlFamilyNamesLookup(String ids, String uplVersion, String database) {
        // Get time to get information
        long timeStart = System.currentTimeMillis();
        Hashtable <String, String> idToNameTbl  = TreeLogic.getFamilyNamesLookup(ids, uplVersion, database); 
        long duration = System.currentTimeMillis() - timeStart;
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(ids);
            id.appendChild(id_text);

            Element searchString = doc.createElement(ELEMENT_SEARCH_FAMILY_NAMES);
            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Enumeration<String> famIds = idToNameTbl.keys();
            while (famIds.hasMoreElements()) {
                String famId = famIds.nextElement();
                String famName = idToNameTbl.get(famId);
                
                Element family = doc.createElement(ELEMENT_FAMILY);
                Element familyAccession = doc.createElement(ELEMENT_FAMILY_ACCESSION);
                familyAccession.appendChild(doc.createTextNode(famId));
                Element familyName = doc.createElement(ELEMENT_FAMILY_NAME);
                familyName.appendChild(doc.createTextNode(famName));
                family.appendChild(familyAccession);
                family.appendChild(familyName);
                root.appendChild(family);    
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
    
    private static String outputPhyloXML(TreeLogic tl) {
        if (null == organismManager) {
            organismManager = OrganismManager.getInstance();
        }
        AnnotationNode treeRoot = tl.getRoot();
        String familyId = tl.getFamilyId();
        PhylogenyNode root = new PhylogenyNode();
        addPhylogenyNode(treeRoot, root, familyId);
        Phylogeny phy = new Phylogeny();
        phy.setRoot(root);
        phy.setRooted(true);
        phy.setName(tl.getFamilyName());
        phy.setIdentifier(new Identifier(tl.getFamilyId(), PROVIDER_PANTHER));
        return phy.toPhyloXML(0);
    }
    
    private static void addPhylogenyNode(AnnotationNode node, PhylogenyNode phylogenyNode, String familyId) {
        //System.out.println("Processing " + node.getAccession());
        // Name
//        phylogenyNode.setName(PROVIDER_PANTHER + DELIM_PROVIDER + node.getPublicId());
        
        // Branch length
        if (null != node.getBranchLength()) {
            phylogenyNode.setDistanceToParent(Double.parseDouble(node.getBranchLength()));
        }
               
        NodeData nd = phylogenyNode.getNodeData();
        Sequence sequence = new Sequence();
        nd.setSequence(sequence);
        String seq   = node.getSequence();
        if (null != seq) {
            sequence.setMolecularSequence(seq);
            try {
                sequence.setType(PhyloXmlUtil.SEQ_TYPE_PROTEIN);
            }
            catch(PhyloXmlDataFormatException e) {
                
            }
            sequence.setMolecularSequenceAligned(true);
        }
        addProteinInfo(sequence, node);
        
        // Gene name
        String geneName = node.getGeneName();
        if (null != geneName) {
            sequence.setGeneName(geneName);
        }
        
        // Not all gene symbols are 'valid'.  Some of them are ids.  Do not output for now.
            
//        String geneSymbol = node.getGeneSymbol();
//        if (null != geneSymbol) {
//            try {
//                sequence.setSymbol(geneSymbol);
//            }
//            catch(PhyloXmlDataFormatException e) {
//                System.out.println("Illegal sequence symbol " + geneSymbol + " for node " + node.getAccession());
//            }
//        }

        
        
        // Species
        String species = node.getSpecies();
        if (null != species) {
            Taxonomy taxonomy = new Taxonomy();
            taxonomy.setScientificName(species);
            nd.setTaxonomy(taxonomy);
           
            // Add taxonomy id
            if (null != organismManager) {
                String taxonId = organismManager.getTaxonId(species);
                if (null != taxonId) {
                    taxonomy.setIdentifier( new Identifier(taxonId, PROVIDER_NCBI));                    
                }
            }
        }
        // Get species for leaf nodes
        String shortName = node.getSpeciesFromLongName();
        if (null != shortName && null != organismManager) {
            Organism o = organismManager.getOrganismForShortName(shortName);
            if (null != o) {
                Taxonomy taxonomy = new Taxonomy();
                String longName = o.getLongName();
                if (null != longName) {
                    taxonomy.setScientificName(longName);
                    try {
                        taxonomy.setTaxonomyCode(shortName);
                    }
                    catch (PhyloXmlDataFormatException e) {
                        e.printStackTrace();
                    }
                    nd.setTaxonomy(taxonomy);
                }
                // Add taxonomy id
                String taxonId = o.getTaxonId();
                if (null != taxonId) {
                    taxonomy.setIdentifier( new Identifier(taxonId, PROVIDER_NCBI));                    
                }
            }
        }
        
        // Event       
        if (true == node.isDuplicationNode()) {
            Event event = new Event(Event.EventType.speciation_or_duplication);
            event.setDuplications(1);
            nd.setEvent(event);
        }
        else if (true == node.isHorizontalTransferNode()) {
            // Is transfer and horizontal transfer the same?
            Event event = new Event(Event.EventType.transfer);
            nd.setEvent(event);
        }
        else {
            Event event = new Event(Event.EventType.speciation_or_duplication);
            event.setSpeciations(1);
            nd.setEvent(event);
        }

//          AN is only used internally do not output       
//        String anId = node.getAnnotationNodeId();
//        if (null != anId) {
//            PropertiesMap pm = nd.getProperties();
//            if (pm == null) {
//                pm = new PropertiesMap();
//                nd.setProperties(pm);
//            }
//            Property p = new Property(REF_AN_ID, AnnotationNode.constructFullAnnotationId(familyId, anId), null, TYPE_STRING, Property.AppliesTo.NODE);
//            pm.addProperty(p);
//            
//        }
        String longId = node.getLongName();
        if (null != longId) {
            PropertiesMap pm = nd.getProperties();
            if (pm == null) {
                pm = new PropertiesMap();
                nd.setProperties(pm);
            }
            Property p = new Property(REF_LONG_ID, longId, null, TYPE_STRING, Property.AppliesTo.NODE);
            pm.addProperty(p);
        }
        
        String publicId = node.getPublicId();
        if (null != publicId) {
            PropertiesMap pm = nd.getProperties();
            if (pm == null) {
                pm = new PropertiesMap();
                nd.setProperties(pm);
            }
            Property p = new Property(REF_PUBLIC_ID, publicId, null, TYPE_STRING, Property.AppliesTo.NODE);
            pm.addProperty(p);
        }
        
        String sfId = node.getSfId();
            if (null != sfId) {
            PropertiesMap pm = nd.getProperties();
            if (pm == null) {
                pm = new PropertiesMap();
                nd.setProperties(pm);
            }
            Property p = new Property(REF_SF_ID, sfId, null, TYPE_STRING, Property.AppliesTo.NODE);
            pm.addProperty(p);

        }
        
        String sfName = node.getSfName();
        if (null != sfName) {
            PropertiesMap pm = nd.getProperties();
            if (pm == null) {
                pm = new PropertiesMap();
                nd.setProperties(pm);
            }
            Property p = new Property(REF_SF_NAME, sfName, null, TYPE_STRING, Property.AppliesTo.NODE);
            pm.addProperty(p);            
        }
        
     
        
        
        Vector children = node.getChildren();
        if (null == children)  {
            return;
        }
        
        for (int i = 0; i < children.size(); i++) {
            AnnotationNode an = (AnnotationNode)children.get(i);
            PhylogenyNode child = new PhylogenyNode();
            phylogenyNode.addAsChild(child);
            addPhylogenyNode(an, child, familyId);
        }
    }
    
    private static void addProteinInfo(Sequence sequence, AnnotationNode node) {
        if (null == sequence || null == node) {
            return;
        }
        String proteinInfo = node.getProteinPartFromLongName();
        if (null == proteinInfo) {
            return;
        }
        
        String parts[] = proteinInfo.split(DELIM_PROTEIN_INFO);
        if (parts.length < 2) {
            return;
        }
        sequence.setAccession(new Accession(parts[1], parts[0]));
    }
    
    private static String outputFamliyComment(String comment, String familyId, long duration, String searchType) {
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(familyId);
            id.appendChild(id_text);

            Element searchString = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text searchStringText = doc.createTextNode(searchType);
            searchString.appendChild(searchStringText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            if (null != comment) {
                root.appendChild(WSUtil.createTextNode(doc, ELEMENT_FAMILY_COMMENT, comment));            
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
    
    private static String outputFamilyStructureAnnotations(TreeLogic tl, HashMap<String, Node> nodeLookup, HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> nonPropAnnotSet, String familyId, String familyName, long duration, String searchType) {
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(familyId);
            id.appendChild(id_text);

            Element searchString = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text searchStringText = doc.createTextNode(searchType);
            searchString.appendChild(searchStringText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            
            if (null != familyName) {
                root.appendChild(WSUtil.createTextNode(doc, ELEMENT_FAMILY_NAME, familyName));
            }
            
            AnnotationNode treeRoot = tl.getRoot();
            if (null != treeRoot) {
                Element treeInfo = outputTreeAnnotInformation(doc, treeRoot, null, nodeLookup, nonPropAnnotSet);
                if (null != treeInfo) {
                    root.appendChild(treeInfo);
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
    
    private static String outputFamilyDirectAnnotations(HashMap<String, Node> nodeLookup, HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> nonPropAnnotSet, String familyId, String familyName, long duration, String searchType) {
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(familyId);
            id.appendChild(id_text);

            Element searchTypeElem = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text searchTypeText = doc.createTextNode(searchType);
            searchTypeElem.appendChild(searchTypeText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchTypeElem);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            
            Element familyNameElem = doc.createElement(ELEMENT_FAMILY_NAME);
            Text familyName_text = doc.createTextNode(familyName);
            familyNameElem.appendChild(familyName_text);
            root.appendChild(familyNameElem);
            
            if (null != nodeLookup && false == nodeLookup.isEmpty()) {
                Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
                root.appendChild(nodeList);
                for (Node n: nodeLookup.values()) {
                    Element nodeElem = doc.createElement(ELEMENT_NODE);
                    nodeList.appendChild(nodeElem);
                    Element nodeAcc = doc.createElement(ELEMENT_ANNOTATION_ACCESSION);
                    nodeAcc.appendChild(doc.createTextNode(n.getStaticInfo().getNodeAcc()));
                    nodeElem.appendChild(nodeAcc);
                    
                    Element pubIdElem = doc.createElement(ELEMENT_ANNOTATION_PUBLIC_ID);
                    pubIdElem.appendChild(doc.createTextNode(n.getStaticInfo().getPublicId()));
                    nodeElem.appendChild(pubIdElem);
                        
                    addAnnotationInfo(doc, nodeElem, n, nonPropAnnotSet);
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
    
    private static String outputFamilyInfo (AnnotationNode treeRoot, String searchId, String familyId, String familyName, long duration, String searchtype) {
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(searchId);
            id.appendChild(id_text);

            Element searchString = doc.createElement(searchtype);
            Text searchStringText = doc.createTextNode(searchId);
            searchString.appendChild(searchStringText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            
            Element familyNameElem = doc.createElement(ELEMENT_FAMILY_NAME);
            Text familyName_text = doc.createTextNode(familyName);
            familyNameElem.appendChild(familyName_text);
            root.appendChild(familyNameElem);
            
            if (null != treeRoot) {
                Element treeInfo = outputTreeInformation(doc, treeRoot, null);
                if (null != treeInfo) {
                    root.appendChild(treeInfo);
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
    
    private static Element outputTreeAnnotInformation(Document doc, AnnotationNode an, String parentPublicId, HashMap<String, Node> nodeLookup, HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> nonPropAnnotSet) {
        if (null == an) {
            return null;
        }
        Element annotationNodeElement = doc.createElement(ELEMENT_ANNOTATION_NODE);
        
        Element annotationId = doc.createElement(ELEMENT_ANNOTATION_ACCESSION);
        Text accession_text = doc.createTextNode(an.getAccession());
        annotationId.appendChild(accession_text);
        
        Element publicId = doc.createElement(ELEMENT_ANNOTATION_PUBLIC_ID);
        String publicIdStr = an.getPublicId();
        Text publicId_text = doc.createTextNode(publicIdStr);
        publicId.appendChild(publicId_text);
        
        
        Element parentId = doc.createElement(ELEMENT_ANNOTATION_PARENT);
        Text parentId_text = doc.createTextNode(parentPublicId);
        parentId.appendChild(parentId_text);
        
     
        
        Element nodeName = doc.createElement(ELEMENT_NODE_NAME);
        Text nodeName_text = doc.createTextNode(an.getNodeName());
        nodeName.appendChild(nodeName_text);
        
        Element species = doc.createElement(ELEMENT_NODE_SPECIES);
        Text species_text = doc.createTextNode(an.getSpecies());
        species.appendChild(species_text);
        
        Element referenceSpeciation = doc.createElement(ELEMENT_REFERENCE_SPECIATION_EVENT);
        Text speciationText = doc.createTextNode(getReferenceSpeciationEvent(an));
        referenceSpeciation.appendChild(speciationText);
        
        Element nodeType = doc.createElement(ELEMENT_ANNOTATION_NODE_TYPE);
        Vector children = an.getChildren();
        Text nodeType_text = null;
        if (null == an.getParent()) {
            nodeType_text = doc.createTextNode(NODE_TYPE_ROOT);
        }
        else if (children != null && 0 < children.size()){
            nodeType_text = doc.createTextNode(NODE_TYPE_INTERNAL);
        }
        else {
            nodeType_text = doc.createTextNode(NODE_TYPE_EXTANT);            
        }
        nodeType.appendChild(nodeType_text);
        
        Element eventType = doc.createElement(ELEMENT_ANNOTATION_EVENT_TYPE);
        Text eventType_text = null;
        if (true == an.isDuplicationNode()) {
            eventType_text = doc.createTextNode(EVENT_TYPE_DUPLICATION);
        }
        else if (true == an.isHorizontalTransferNode()) {
            eventType_text = doc.createTextNode(EVENT_TYPE_HORIZONTAL_TRANSFER);
        }
        else {
            eventType_text = doc.createTextNode(EVENT_TYPE_SPECIATION);
        }
        eventType.appendChild(eventType_text);
        

         
        Element branchLength = doc.createElement(ELEMENT_ANNOTATION_BRANCH_LENGTH);
        Text branchLength_text = doc.createTextNode(an.getBranchLength());
        branchLength.appendChild(branchLength_text);
        
        Element sequenceInfo = doc.createElement(ELEMENT_ANNOTATION_SEQUENCE);
        Text sequenceInfo_text = doc.createTextNode(an.getSequence());
        sequenceInfo.appendChild(sequenceInfo_text);
        
        
        annotationNodeElement.appendChild(annotationId);
        annotationNodeElement.appendChild(publicId);
        annotationNodeElement.appendChild(parentId);
        annotationNodeElement.appendChild(nodeName);
        annotationNodeElement.appendChild(species);
        annotationNodeElement.appendChild(referenceSpeciation);
        annotationNodeElement.appendChild(nodeType);
        annotationNodeElement.appendChild(eventType);
        annotationNodeElement.appendChild(branchLength);
        annotationNodeElement.appendChild(sequenceInfo);        
        
        String nodeAcc = an.getAccession();
        Node n = nodeLookup.get(nodeAcc);
        NodeStaticInfo nsi = n.getStaticInfo();
        ArrayList<String> symbols = nsi.getGeneSymbol();
        if (null != symbols && 0 != symbols.size()) {
            Element geneSymbol = doc.createElement(ELEMENT_GENE_SYMBOL);
            String[] arraySymbols = new String[symbols.size()];
            symbols.toArray(arraySymbols);
            Text geneSymbolText = doc.createTextNode(Utils.listToString(arraySymbols, WSConstants.STR_EMPTY, WSConstants.STR_COMMA));
            geneSymbol.appendChild(geneSymbolText);
            annotationNodeElement.appendChild(geneSymbol);
        }
        
        ArrayList<String> geneNames = nsi.getGeneName();
        if (null != geneNames && 0 != geneNames.size()) {
            Element geneName = doc.createElement(ELEMENT_GENE_NAME);
            String[] arrayNames = new String[geneNames.size()];
            geneNames.toArray(arrayNames);
            Text geneNameText = doc.createTextNode(Utils.listToString(arrayNames, WSConstants.STR_EMPTY, WSConstants.STR_COMMA));
            geneName.appendChild(geneNameText);
            annotationNodeElement.appendChild(geneName);            
        }
        
        
        addAnnotationInfo(doc, annotationNodeElement, n, nonPropAnnotSet);

        
        
        if (null != children) {
            int num = children.size();
            if (0 != num) {
                Element childrenElem = doc.createElement(ELEMENT_CHILDREN);
                annotationNodeElement.appendChild(childrenElem);
                for (int i = 0; i < num; i++) {
                    AnnotationNode child = (AnnotationNode)children.get(i);
                    Element childElem = outputTreeAnnotInformation(doc, child, publicIdStr, nodeLookup, nonPropAnnotSet);
                    if (null != childElem) {
                        childrenElem.appendChild(childElem);
                    }
                }
                
            }
        }
        
        return annotationNodeElement;
                
    }
    
    private static boolean evidenceIsFromPAINT(Evidence e) {
        if (null == e) {
            return false;
        }
        ArrayList<DBReference> dbRefList= e.getDbReferenceList();
        if (null == dbRefList) {
            return false;
        }
        for (DBReference dbRef: dbRefList) {
            if (true == Utils.PAINT_REF.equals(dbRef.getEvidenceType())) {
                return true;
            }
        }
        return false;
    }
    
   private static void addAnnotationInfo(Document doc, Element parent, Node n, HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> nonPropAnnotSet) {
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi) {
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null != annotList && false == annotList.isEmpty()) {
                Element annotListElem = doc.createElement(ELEMENT_ANNOTATION_LIST);
                parent.appendChild(annotListElem);
                for (edu.usc.ksom.pm.panther.paintCommon.Annotation a : annotList) {
                    Element paintAnnot = doc.createElement(ELEMENT_ANNOTATION);
                    annotListElem.appendChild(paintAnnot);
                    
                    Element evidenceCde = WSUtil.createTextNode(doc, ELEMENT_EVIDENCE_CODE, StringUtils.listToString(a.getEvidenceCodeSet(), Constant.STR_EMPTY, Constant.STR_COMMA));
                    if (null != evidenceCde) {
                        paintAnnot.appendChild(evidenceCde);
                    }
                    
                    if (true == nonPropAnnotSet.contains(a)) {
                        Element directAnnot = WSUtil.createTextNode(doc, ELEMENT_ANNOT_DIRECT, Boolean.TRUE.toString());
                        paintAnnot.appendChild(directAnnot);
                    }                    
                    if (true == a.isPaint()) {
                        Element evidenceFromPaint = WSUtil.createTextNode(doc, ELEMENT_ANNOT_FROM_PAINT, Boolean.TRUE.toString());
                        paintAnnot.appendChild(evidenceFromPaint);
                    }
                    
                    AnnotationDetail ad = a.getAnnotationDetail();
                    if (null != ad) {
                        HashSet<edu.usc.ksom.pm.panther.paintCommon.WithEvidence> withEvSet = ad.getWithEvidenceSet();
                        if (null != withEvSet && 0 != withEvSet.size()) {
                            Element withList = doc.createElement(ELEMENT_ANNOT_WITH_LIST);
                            paintAnnot.appendChild(withList);
                            HashSet<String> processedNodeSet = new HashSet<String>();
                            for (edu.usc.ksom.pm.panther.paintCommon.WithEvidence withEv : withEvSet) {
                                IWith with = withEv.getWith();
                                if (with instanceof edu.usc.ksom.pm.panther.paintCommon.Annotation) {
                                    edu.usc.ksom.pm.panther.paintCommon.Annotation withAnnot = (edu.usc.ksom.pm.panther.paintCommon.Annotation) withEv.getWith();
                                    AnnotationDetail withAnnotDetail = withAnnot.getAnnotationDetail();
                                    if (null != withAnnotDetail) {
                                        Node annotatedNode = withAnnotDetail.getAnnotatedNode();
                                        if (null != annotatedNode) {
                                            if (n == annotatedNode) {
                                                continue;
                                            }
                                            String publicId = annotatedNode.getStaticInfo().getPublicId();
                                            if (true == processedNodeSet.contains(publicId)) {
                                                continue;
                                            }
                                            processedNodeSet.add(publicId);
                                            Element annotatedNodeElem = WSUtil.createTextNode(doc, ELEMENT_ANNOT_WITH_ANNOTATION_TO_NODE, publicId);
                                            withList.appendChild(annotatedNodeElem);
                                        }
                                    }
                                }
                                else if (with instanceof DBReference) {
                                    DBReference dbref = (DBReference) with;
                                    Element annotatedDBElem = WSUtil.createTextNode(doc, ELEMENT_ANNOT_WITH_DB_REFERENCE, dbref.getEvidenceType() + DELIM_DB_REF + dbref.getEvidenceValue());
                                    withList.appendChild(annotatedDBElem);
                                }
                                else if (with instanceof Node) {
                                    Node node = (Node)with;
                                    Element annotatedNodeElem = WSUtil.createTextNode(doc, ELEMENT_ANNOT_WITH_NODE, node.getStaticInfo().getPublicId());
                                    withList.appendChild(annotatedNodeElem);                                    
                                }
                            }
                        }
                    }                    
//                    if (null != ad) {
//                        HashSet<edu.usc.ksom.pm.panther.paintCommon.WithEvidence> withEvSet = ad.getWithEvidenceAnnotSet();
//                        if (null != withEvSet && 0 != withEvSet.size()) {
//                            Element withList = doc.createElement(ELEMENT_ANNOT_WITH_LIST);
//                            paintAnnot.appendChild(withList);
//                            HashSet<String> processedNodeSet = new HashSet<String>();
//                            for (edu.usc.ksom.pm.panther.paintCommon.WithEvidence withEv : withEvSet) {
//                                edu.usc.ksom.pm.panther.paintCommon.Annotation withAnnot = (edu.usc.ksom.pm.panther.paintCommon.Annotation) withEv.getWith();
//                                AnnotationDetail withAnnotDetail = withAnnot.getAnnotationDetail();
//                                if (null != withAnnotDetail) {
//                                    Node annotatedNode = withAnnotDetail.getAnnotatedNode();
//                                    if (null != annotatedNode) {
//                                        if (n == annotatedNode) {
//                                            continue;
//                                        }
//                                        String publicId = annotatedNode.getStaticInfo().getPublicId();
//                                        if (true == processedNodeSet.contains(publicId)) {
//                                            continue;
//                                        }
//                                        processedNodeSet.add(publicId);
//                                        Element annotatedNodeElem = WSUtil.createTextNode(doc, ELEMENT_ANNOT_WITH, publicId);
//                                        withList.appendChild(annotatedNodeElem);
//                                    }
//                                }
//                            }
//                        }
//                    }
                        

                    
                    String term = a.getGoTerm();
                    GOTerm gTerm = goTermHelper.getTerm(term);
                    String termName = gTerm.getName();
                    Element termElem = WSUtil.createTextNode(doc, ELEMENT_TERM, term);
                    paintAnnot.appendChild(termElem);
                    if (null != termName) {
                        Element termNameElem = WSUtil.createTextNode(doc, ELEMENT_TERM_NAME, termName);
                        paintAnnot.appendChild(termNameElem);
                    }

                    paintAnnot.appendChild(WSUtil.createTextNode(doc, ELEMENT_TERM_ASPECT, gTerm.getAspect()));
                    HashSet<Qualifier> qSet = a.getQualifierSet();
                    if (null != qSet && false == qSet.isEmpty()) {
                        Element qualifierList = doc.createElement(ELEMENT_QUALIFIER_LIST);
                        paintAnnot.appendChild(qualifierList);
                        for (Qualifier q : qSet) {
                            Element qualifier = WSUtil.createTextNode(doc, ELEMENT_QUALIFIER, q.getText());
                            qualifierList.appendChild(qualifier);
                        }
                    }

                }
            }
        }  
    }
    
    private static Element outputTreeInformation(Document doc, AnnotationNode an, String parentPublicId) {
        if (null == an) {
            return null;
        }
        Element annotationNodeElement = doc.createElement(ELEMENT_ANNOTATION_NODE);
        
        Element annotationId = doc.createElement(ELEMENT_ANNOTATION_ACCESSION);
        Text accession_text = doc.createTextNode(an.getAccession());
        annotationId.appendChild(accession_text);
        
        Element publicId = doc.createElement(ELEMENT_ANNOTATION_PUBLIC_ID);
        String publicIdStr = an.getPublicId();
        Text publicId_text = doc.createTextNode(publicIdStr);
        publicId.appendChild(publicId_text);
        
        
        Element parentId = doc.createElement(ELEMENT_ANNOTATION_PARENT);
        Text parentId_text = doc.createTextNode(parentPublicId);
        parentId.appendChild(parentId_text);
        
        Element nodeName = doc.createElement(ELEMENT_NODE_NAME);
        Text nodeName_text = doc.createTextNode(an.getNodeName());
        nodeName.appendChild(nodeName_text);
        
        Element species = doc.createElement(ELEMENT_NODE_SPECIES);
        Text species_text = doc.createTextNode(an.getSpecies());
        species.appendChild(species_text);
        
        Element referenceSpeciation = doc.createElement(ELEMENT_REFERENCE_SPECIATION_EVENT);
        Text speciationText = doc.createTextNode(getReferenceSpeciationEvent(an));
        referenceSpeciation.appendChild(speciationText);
        
        Element nodeType = doc.createElement(ELEMENT_ANNOTATION_NODE_TYPE);
        Vector children = an.getChildren();
        Text nodeType_text = null;
        if (null == an.getParent()) {
            nodeType_text = doc.createTextNode(NODE_TYPE_ROOT);
        }
        else if (children != null && 0 < children.size()){
            nodeType_text = doc.createTextNode(NODE_TYPE_INTERNAL);
        }
        else {
            nodeType_text = doc.createTextNode(NODE_TYPE_EXTANT);            
        }
        nodeType.appendChild(nodeType_text);
        
        Element eventType = doc.createElement(ELEMENT_ANNOTATION_EVENT_TYPE);
        Text eventType_text = null;
        if (true == an.isDuplicationNode()) {
            eventType_text = doc.createTextNode(EVENT_TYPE_DUPLICATION);
        }
        else if (true == an.isHorizontalTransferNode()) {
            eventType_text = doc.createTextNode(EVENT_TYPE_HORIZONTAL_TRANSFER);
        }
        else {
            eventType_text = doc.createTextNode(EVENT_TYPE_SPECIATION);
        }
        eventType.appendChild(eventType_text);
        
        Element geneSymbol = doc.createElement(ELEMENT_GENE_SYMBOL);
        Text geneSymbolText = doc.createTextNode(an.getGeneSymbol());
        geneSymbol.appendChild(geneSymbolText);
        
        Element subfamilyId = doc.createElement(ELEMENT_SUBFAMILY_ID);
        Text subfamilyIdText = doc.createTextNode(an.getSfId());
        subfamilyId.appendChild(subfamilyIdText);
        
        Element subfamilyName = doc.createElement(ELEMENT_SUBFAMILY_NAME);
        Text subfamilyNameText = doc.createTextNode(an.getSfName());
        subfamilyName.appendChild(subfamilyNameText);
         
        Element branchLength = doc.createElement(ELEMENT_ANNOTATION_BRANCH_LENGTH);
        Text branchLength_text = doc.createTextNode(an.getBranchLength());
        branchLength.appendChild(branchLength_text);
        
        Element sequenceInfo = doc.createElement(ELEMENT_ANNOTATION_SEQUENCE);
        Text sequenceInfo_text = doc.createTextNode(an.getSequence());
        sequenceInfo.appendChild(sequenceInfo_text);
        
        annotationNodeElement.appendChild(annotationId);
        annotationNodeElement.appendChild(publicId);
        annotationNodeElement.appendChild(parentId);
        annotationNodeElement.appendChild(nodeName);
        annotationNodeElement.appendChild(geneSymbol);
        annotationNodeElement.appendChild(subfamilyId);
        annotationNodeElement.appendChild(subfamilyName);
        annotationNodeElement.appendChild(species);
        annotationNodeElement.appendChild(referenceSpeciation);
        annotationNodeElement.appendChild(nodeType);
        annotationNodeElement.appendChild(eventType);
        annotationNodeElement.appendChild(branchLength);
        annotationNodeElement.appendChild(sequenceInfo);
        
        
        if (null != children) {
            int num = children.size();
            if (0 != num) {
                Element childrenElem = doc.createElement(ELEMENT_CHILDREN);
                annotationNodeElement.appendChild(childrenElem);
                for (int i = 0; i < num; i++) {
                    AnnotationNode child = (AnnotationNode)children.get(i);
                    Element childElem = outputTreeInformation(doc, child, publicIdStr);
                    if (null != childElem) {
                        childrenElem.appendChild(childElem);
                    }
                }
                
            }
        }
        
        return annotationNodeElement;
        
    }
    
    public static String getReferenceSpeciationEvent(AnnotationNode an) {
        if (true == an.isDuplicationNode()) {
            Vector children = an.getChildren();
            if (null != children) {
                HashSet tmpList = new HashSet();
                for (int i = 0; i < children.size(); i++) {
                    AnnotationNode child = (AnnotationNode)children.get(i);
                    String species = child.getSpecies();
                    if (null != species) {
                        tmpList.add(species);
                    }
                }
                int num = tmpList.size();
                if (num > 0) {
                    String[] strArray = new String[num];
                    tmpList.toArray(strArray);
                    return Utils.listToString(strArray, WSConstants.STR_EMPTY, WSConstants.STR_COMMA);
                }
            }
        }
        return an.getSpecies();
    }
    
    public static String getXMLAnnotationInfo(String familyId, String uplVersion) {
        HashMap<String, Node> treeNodeLookup = new HashMap<String, Node>();
        long start = System.currentTimeMillis();
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        dataIO.getAnnotationNodeLookup(familyId, uplVersion, treeNodeLookup);
        HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>> annotToPosWithLookup = new HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>>();
        StringBuffer errorBuf = new StringBuffer();
        StringBuffer paintErrBuf = new StringBuffer();
        HashSet<String> removeSet = new HashSet<String>();
        HashSet<String> modifySet = new HashSet<String>();
        try {
            dataIO.getFullGOAnnotations(familyId, uplVersion, treeNodeLookup, annotToPosWithLookup, errorBuf, paintErrBuf, removeSet, modifySet, false);
            return outputFamilyAnnotationInfoStr(errorBuf, paintErrBuf, familyId, System.currentTimeMillis() - start, WSConstants.SEARCH_TYPE_FAMILY_ANNOTATION_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String getXMLAggAnnotationInfo(String familyId, String uplVersion) {
        HashMap<String, Node> treeNodeLookup = new HashMap<String, Node>();
        long start = System.currentTimeMillis();
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        dataIO.getAnnotationNodeLookup(familyId, uplVersion, treeNodeLookup);
        HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>> annotToPosWithLookup = new HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>>();
        StringBuffer errorBuf = new StringBuffer();
        StringBuffer paintErrBuf = new StringBuffer();
        HashSet<String> removeSet = new HashSet<String>();
        HashSet<String> modifySet = new HashSet<String>();
        try {
            dataIO.getFullGOAnnotations(familyId, uplVersion, treeNodeLookup, annotToPosWithLookup, errorBuf, paintErrBuf, removeSet, modifySet, true);
            return outputFamilyAnnotationInfoStr(errorBuf, paintErrBuf, familyId, System.currentTimeMillis() - start, WSConstants.SEARCH_TYPE_AGG_FAMILY_ANNOTATION_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }    
    public static String getXMLOtherEvdnce(String familyId, String uplVersion) {
        HashMap<String, Node> treeNodeLookup = new HashMap<String, Node>();
        long start = System.currentTimeMillis();
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        dataIO.getAnnotationNodeLookup(familyId, uplVersion, treeNodeLookup);
        HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>> annotToPosWithLookup = new HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>>();
        StringBuffer errorBuf = new StringBuffer();
        StringBuffer paintErrBuf = new StringBuffer();
        HashSet<String> removeSet = new HashSet<String>();
        HashSet<String> modifySet = new HashSet<String>();
        try {
            dataIO.getFullGOAnnotations(familyId, uplVersion, treeNodeLookup, annotToPosWithLookup, errorBuf, paintErrBuf, removeSet, modifySet, false);
            return outputFamilyOtherEvdnce(annotToPosWithLookup, familyId, System.currentTimeMillis() - start, WSConstants.SEARCH_TYPE_FAMILY_EVIDENCE_INFO);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
   private static String outputFamilyOtherEvdnce(HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>> annotToPosWithLookup, String familyId, long duration, String searchType) {
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(familyId);
            id.appendChild(id_text);

            Element searchString = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text searchStringText = doc.createTextNode(searchType);
            searchString.appendChild(searchStringText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            if (null != annotToPosWithLookup && false == annotToPosWithLookup.isEmpty()) {
                Element annotListElem = doc.createElement(ELEMENT_ANNOTATION_LIST);
                root.appendChild(annotListElem);
                for (edu.usc.ksom.pm.panther.paintCommon.Annotation a: annotToPosWithLookup.keySet()) {
                    Element annotElem = doc.createElement(ELEMENT_ANNOTATION);
                    annotListElem.appendChild(annotElem);
                    
                    Element idElem = doc.createElement(ELEMENT_ANNOTATION_ID);
                    annotElem.appendChild(idElem);
                    idElem.appendChild(doc.createTextNode(a.getAnnotationId()));

                    Element codeElem = doc.createElement(ELEMENT_CODE);
                    annotElem.appendChild(codeElem);
                    codeElem.appendChild(doc.createTextNode( a.getSingleEvidenceCodeFromSet()));
                  
                    Element publicIdElem = doc.createElement(ELEMENT_NODE_PUBLIC_ID);
                    annotElem.appendChild(publicIdElem);
                    publicIdElem.appendChild(doc.createTextNode(a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId()));                    
                    
                    ArrayList<IWith> withList = annotToPosWithLookup.get(a);                     
                    Element withListElem = doc.createElement(ELEMENT_WITH_LIST);
                    annotElem.appendChild(withListElem);
                    for (IWith with : withList) {
                        if (with instanceof edu.usc.ksom.pm.panther.paintCommon.Annotation) {
                            edu.usc.ksom.pm.panther.paintCommon.Annotation iAnnot = (edu.usc.ksom.pm.panther.paintCommon.Annotation)with;
                            Element withElem = doc.createElement(ELEMENT_WITH);
                            withListElem.appendChild(withElem);
                            
                            Element withAnnotElem = doc.createElement(ELEMENT_ANNOTATION_ID);
                            withElem.appendChild(withAnnotElem);
                            withAnnotElem.appendChild(doc.createTextNode(iAnnot.getAnnotationId()));

                            
                            Element withNodeElem = doc.createElement(ELEMENT_NODE_PUBLIC_ID);
                            withElem.appendChild(withNodeElem);
                            withNodeElem.appendChild(doc.createTextNode(iAnnot.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId()));
                        }
                    }
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
     
    
    private static String outputFamilyAnnotationInfoStr(StringBuffer otherAnnotInfo, StringBuffer paintAnnotInfo, String familyId, long duration, String searchType) {
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element id = doc.createElement(ELEMENT_ID);
            Text id_text = doc.createTextNode(familyId);
            id.appendChild(id_text);

            Element searchString = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text searchStringText = doc.createTextNode(searchType);
            searchString.appendChild(searchStringText);

            Element elapsedTime = doc.createElement(ELEMENT_ELAPSED_TIME);
            Text time_text = doc.createTextNode(duration + MS);
            elapsedTime.appendChild(time_text);
            
            parameters.appendChild(id);
            parameters.appendChild(searchString);
            parameters.appendChild(elapsedTime);
            root.appendChild(parameters);
            
            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(familyId);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);
            if (null != otherAnnotInfo) {
                root.appendChild(WSUtil.createTextNode(doc, ELEMENT_FAMILY_ANNOTATION_INFO_OTHER, otherAnnotInfo.toString()));            
            }
            if (null != paintAnnotInfo) {
                root.appendChild(WSUtil.createTextNode(doc, ELEMENT_FAMILY_ANNOTATION_INFO_PAINT, paintAnnotInfo.toString()));            
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
    
    public static void main(String[] args) {
        System.out.println(FamilyUtil.getFamilyInfo("PTHR10000", null, null, WSConstants.SEARCH_TYPE_FAMILY_PHYLOXML));
        //System.out.println(FamilyUtil.getFamilyInfo("PTHR10000", null, null, WSConstants.SEARCH_PARAMETER_BOOKS_SEARCH_TYPE));
    }

}
