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
package edu.usc.ksom.pm.panther.paintServer.webservices;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.GO.Evidence;
import com.sri.panther.paintCommon.GO.EvidenceSpecifier;
import com.sri.panther.paintCommon.util.StringUtils;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.database.DataServer;
import com.sri.panther.paintServer.database.DataServerManager;
import com.sri.panther.paintServer.datamodel.PANTHERTreeNode;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class BookUtil {

    public static final String ELEMENT_SEARCH = "search";
    public static final String ELEMENT_SEARCH_BOOK = "search_book";
    public static final String ELEMENT_SEARCH_DATABASE = "search_database";
    public static final String ELEMENT_SEARCH_UPLVERSION = "search_uplversion";
    public static final String ELEMENT_SEARCH_TYPE = "search_type";
    
    public static final String ELEMENT_NODE_LIST = "node_list";
    public static final String ELEMENT_NODE = "node";
    public static final String ELEMENT_ACCESSION = "accession";
    public static final String ELEMENT_ANNOTATION_LIST = "annotation_list";
    public static final String ELEMENT_ANNOTATION = "annotation";
    public static final String ELEMENT_ANNOTATION_ID = "annotation_id";
    public static final String ELEMENT_ANNOTATION_TERM = "annotation_term";
    public static final String ELEMENT_ANNOTATION_QUALIFIER_LIST = "annotation_qualifier_list";
    public static final String ELEMENT_ANNOTATION_QUALIFIER = "annotation_qualifier";    
    public static final String ELEMENT_ANNOTATION_EVIDENCE = "annotation_evidence";    
    public static final String ELEMENT_ANNOTATION_EVIDENCE_CODE = "annotation_evidence_code";
    public static final String ELEMENT_ANNOTATION_WITH_LIST = "annotation_with_list";
    public static final String ELEMENT_ANNOTATION_WITH = "annotation_with";
    public static final String ELEMENT_ANNOTATION_TYPE = "annotation_type";
    public static final String ELEMENT_ANNOTATION_VALUE = "annotation_value";    
    
    public static final String ELEMENT_PARENT_ACCESSION = "parent_accession";
    public static final String ELEMENT_PUBLIC_ID = "public_id";
    public static final String ELEMENT_BRANCH_LENGTH = "branch_length";
    public static final String ELEMENT_NODE_TYPE = "node_type";
    public static final String ELEMENT_EVENT_TYPE = "event_type";
    public static final String ELEMENT_LONG_GENE_NAME = "long_gene_name";
    public static final String ELEMENT_GENE_DEFINITION = "gene_definition";
    public static final String ELEMENT_SFID = "node_sf_Id";
    public static final String ELEMENT_SFNAME = "node_sf_name";
    public static final String ELEMENT_GENE_IDENTIFIER_LIST = "gene_identifier_list";
    public static final String ELEMENT_GENE_IDENTIFIER = "gene_identifier";
    public static final String ELEMENT_GENE_SYMBOL_LIST = "gene_symbol_list";
    public static final String ELEMENT_GENE_SYMBOL = "gene_symbol";
    public static final String ELEMENT_GENE_NAME_LIST = "gene_name_list";
    public static final String ELEMENT_GENE_NAME = "gene_name";
    public static final String ELEMENT_IDENTIFIER_LIST = "identifier_list";
    public static final String ELEMENT_IDENTIFIER_INFO = "identifier_info";
    public static final String ELEMENT_IDENTIFIER_TYPE = "identifier_type";
    public static final String ELEMENT_IDENTIFIER = "identifier";

    public static final String ELEMENT_EVIDENCE_LIST = "evidence_list";
    public static final String ELEMENT_EVIDENCE = "evidence";
    public static final String ELEMENT_EVIDENCE_ACCESSION = "evidence_accession";
    public static final String ELEMENT_EVIDENCE_NAME = "evidence_name";
    public static final String ELEMENT_EVIDENCE_TYPE = "evidence_type";
    public static final String ELEMENT_EVIDENCE_QUALIFIER = "qualifier";
    public static final String ELEMENT_EVIDENCE_SPECIFIER_LIST = "evidence_specifier_list";
    public static final String ELEMENT_EVIDENCE_SPECIFIER = "evidence_specifier";
    public static final String ELEMENT_EVIDENCE_SPECIFIER_WITH = "evidence_specifier_with";
    public static final String ELEMENT_EVIDENCE_SPECIFIER_ID = "evidence_specifier_id";
    public static final String ELEMENT_EVIDENCE_SPECIFIER_CODE = "evidence_specifier_code";
    
    public static final String ELEMENT_TREE = "tree";
    public static final String ELEMENT_MSA_INFO = "msa_info";
    public static final String ELEMENT_MSA_DATA = "msa_data";
    public static final String ELEMENT_MSA_WTS_INFO = "wts_info";
    
    public static final String ANNOTATION_TYPE_PANTHER_ANNOTATION = "PANTHER_ANNOTATION";
    public static final String ANNOTATION_TYPE_PANTHER_NODE = "PANTHER_NODE";    

    public static String getAnnotationNodeInfo(String book, String database, String uplVersion, String searchType) {
        
        if (null == database) {
            database = WSConstants.PROPERTY_DB_STANDARD;
        }
        if (null == uplVersion) {
            uplVersion = WSConstants.PROPERTY_CLS_VERSION;
        }
        
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds || null == searchType) {
            return outputInvalidSearchInfo(book, database, uplVersion, searchType);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_ANNOTATION_NODE_INFO)) {
            Vector<PANTHERTreeNode> nodes = ds.getNodeInfoForBook(book, uplVersion);
            return outputAnnotationNodeXMLInfo(nodes, book, database, uplVersion, searchType);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_ANNOTATION_NODE_ANNOTATION_SEQ_GO)) {
            Hashtable<String, Vector<Evidence>> nodeEvidenceTbl = ds.getGOAnnotation(book, uplVersion);
            return outputAnnotationInfoXML(nodeEvidenceTbl, book, database, uplVersion, searchType);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_ANNOTATION_NODE_ANNOTATION_PANTHER)) {
            Hashtable<String, Hashtable<String, Evidence>> nodeEvidenceTbl = ds.getAnnotations(book, uplVersion);
            return outputPANTHERAnnotationsXML(nodeEvidenceTbl, book, database, uplVersion, searchType);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_ANNOTATION_NODE_ANNOTATION_SF)) {
            Vector<PANTHERTreeNode> nodes = ds.getAnToSFInfo(book, uplVersion);
            return outputAnnotationNodeSFXMLInfo(nodes, book, database, uplVersion, searchType);
        }

        if (true == searchType.equals(WSConstants.SEARCH_TYPE_ANNOTATION_NODE_GENE_INFO)) {
            Vector<PANTHERTreeNode> nodes = ds.getGeneInfoInNodeList(book, uplVersion);
            return outputAnnotationNodeGeneXMLInfo(nodes, book, database, uplVersion, searchType);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_ANNOTATION_NODE_IDENTIFIER)) {
            String identifierStr = ConfigFile.getProperty(uplVersion + "_identifiers");
            Vector<PANTHERTreeNode> nodes = ds.getIdentifiersNodeInfoList(book, identifierStr, uplVersion);
            return outputAnnotationNodeIdentifierXMLInfo(nodes, book, database, uplVersion, searchType);
        }
        
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_TREE)) {
            String treeStr[] = DataServlet.getTree(ds, book, uplVersion);
            if (null == treeStr) {
                return outputInvalidSearchInfo(book, database, uplVersion, searchType);
            }
            String oneString = Utils.listToString(treeStr, Constant.STR_EMPTY, Constant.STR_NEWLINE);
            return outputTreeXMLInfo(oneString, book, database, uplVersion, searchType);
        }
        if (true == searchType.equals(WSConstants.SEARCH_TYPE_MSA_INFO)) {
            Vector vect = DataServlet.getMSAStrs(book, uplVersion);
            if (null == vect) {
                return outputInvalidSearchInfo(book, database, uplVersion, searchType);
            }
            
            return outputMSAXMLInfo(vect, book, database, uplVersion, searchType);
        }
        return null;
    }
    
    
    private static String outputInvalidSearchInfo(String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 

            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    
    }
    
    private static String outputAnnotationNodeXMLInfo (Vector <PANTHERTreeNode> nodes, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element nodesElement = createNodeInfo(nodes, doc);
            if (null != nodesElement) {
                root.appendChild(nodesElement);
            }
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
    //            DOMSource domSource = new DOMSource(doc);
    //            StringWriter writer = new StringWriter();
    //            StreamResult result = new StreamResult(writer);
    //            TransformerFactory tf = TransformerFactory.newInstance();
    //            Transformer transformer = tf.newTransformer();
    //            transformer.transform(domSource, result);
    //            return writer.toString();
            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }

    
    private static String outputAnnotationNodeSFXMLInfo (Vector <PANTHERTreeNode> nodes, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element nodesElement = createNodeSFInfo(nodes, doc);
            if (null != nodesElement) {
                root.appendChild(nodesElement);
            }
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
    //            DOMSource domSource = new DOMSource(doc);
    //            StringWriter writer = new StringWriter();
    //            StreamResult result = new StreamResult(writer);
    //            TransformerFactory tf = TransformerFactory.newInstance();
    //            Transformer transformer = tf.newTransformer();
    //            transformer.transform(domSource, result);
    //            return writer.toString();
            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }
    
//    private static  String getNodeInfo(String book, String uplVersion, String searchType) {
//        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
//        HashMap<String, Node> nodeLookup = null;
//        try {
//            StringBuffer eb = new StringBuffer();
//            StringBuffer paintErrBuf = new StringBuffer();
//            nodeLookup = dataIO.getNodeInfo(book, uplVersion, eb, paintErrBuf);
//        }
//        catch(Exception e) {
//            
//        }
//         try {
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            Document doc = builder.newDocument();  
//
//
//            Element root = doc.createElement(ELEMENT_SEARCH);
//            doc.appendChild(root);
//            
//            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
//            Text searchStringText = doc.createTextNode(book);
//            searchString.appendChild(searchStringText);
//            root.appendChild(searchString);
//            
//            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
//            Text uplText = doc.createTextNode(uplVersion);
//            uplElement.appendChild(uplText);
//            root.appendChild(uplElement);
//            
//            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
//            Text typeText = doc.createTextNode(searchType);
//            searchTypeElement.appendChild(typeText);
//            root.appendChild(searchTypeElement);
//            
//            Element nodesElement = createPAINTnodesInfo(nodeLookup, doc);
//            if (null != nodesElement) {
//                root.appendChild(nodesElement);
//            }
//            
//            // Output information
//            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
//            LSSerializer lsSerializer = domImplementation.createLSSerializer();
//            return lsSerializer.writeToString(doc);             
//            
//        }
//        catch (Exception e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }            
//        
//        return null;
//    }
    
    
    private static String outputAnnotationNodeGeneXMLInfo (Vector <PANTHERTreeNode> nodes, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element nodesElement = createNodeGeneInfo(nodes, doc);
            if (null != nodesElement) {
                root.appendChild(nodesElement);
            }
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
    //            DOMSource domSource = new DOMSource(doc);
    //            StringWriter writer = new StringWriter();
    //            StreamResult result = new StreamResult(writer);
    //            TransformerFactory tf = TransformerFactory.newInstance();
    //            Transformer transformer = tf.newTransformer();
    //            transformer.transform(domSource, result);
    //            return writer.toString();
            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }
    
    
    private static String outputAnnotationNodeIdentifierXMLInfo (Vector <PANTHERTreeNode> nodes, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element nodesElement = createNodeIdentifierInfo(nodes, doc);
            if (null != nodesElement) {
                root.appendChild(nodesElement);
            }
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
    //            DOMSource domSource = new DOMSource(doc);
    //            StringWriter writer = new StringWriter();
    //            StreamResult result = new StreamResult(writer);
    //            TransformerFactory tf = TransformerFactory.newInstance();
    //            Transformer transformer = tf.newTransformer();
    //            transformer.transform(domSource, result);
    //            return writer.toString();
            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }


    private static String outputTreeXMLInfo (String treeStr, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element treeElement = doc.createElement(ELEMENT_TREE);
            CDATASection treeCData = doc.createCDATASection(treeStr);
            treeElement.appendChild(treeCData);
            root.appendChild(treeElement);
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
//            System.out.println(treeStr);
            System.out.println(lsSerializer.writeToString(doc));
            return lsSerializer.writeToString(doc); 
    //            DOMSource domSource = new DOMSource(doc);
    //            StringWriter writer = new StringWriter();
    //            StreamResult result = new StreamResult(writer);
    //            TransformerFactory tf = TransformerFactory.newInstance();
    //            Transformer transformer = tf.newTransformer();
    //            transformer.transform(domSource, result);
    //            return writer.toString();
            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }


    
    private static String outputMSAXMLInfo (Vector msaInfo, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element msaElement = doc.createElement(ELEMENT_MSA_INFO);
            root.appendChild(msaElement);
            Vector <String[]>  msaInfoVector = (Vector)msaInfo;
            int num = msaInfoVector.size();
            if (num > 0 ) {
                String msaList[] = msaInfoVector.get(0);        // First element is the mas information
                if (null != msaList) {
                    String fullMsa = Utils.listToString(msaList, Constant.STR_EMPTY, Constant.STR_NEWLINE);
                    Element msaElem = doc.createElement(ELEMENT_MSA_DATA);
                    CDATASection msacData = doc.createCDATASection(fullMsa);
                    msaElem.appendChild(msacData);
                    msaElement.appendChild(msaElem);
                    
                }
            }
            if (num >= 2 ) {

                String wtsList[] = msaInfoVector.get(1);
                if (null != wtsList) {
                    String fullWts = Utils.listToString(wtsList, Constant.STR_EMPTY, Constant.STR_NEWLINE);
                    Element msaElem = doc.createElement(ELEMENT_MSA_WTS_INFO);
                    CDATASection wtscData = doc.createCDATASection(fullWts);
                    msaElem.appendChild(wtscData);
                    msaElement.appendChild(msaElem);
                    
                }
            }

            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
    //            DOMSource domSource = new DOMSource(doc);
    //            StringWriter writer = new StringWriter();
    //            StreamResult result = new StreamResult(writer);
    //            TransformerFactory tf = TransformerFactory.newInstance();
    //            Transformer transformer = tf.newTransformer();
    //            transformer.transform(domSource, result);
    //            return writer.toString();
            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }
    
    private static Element createNodeInfo(Vector <PANTHERTreeNode> nodes, Document doc) {
        if (null == nodes || null == doc) {
            return null;
        }
        
        Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
        for (int i = 0; i < nodes.size(); i++) {
            Element node = doc.createElement(ELEMENT_NODE);
            nodeList.appendChild(node);
            PANTHERTreeNode treeNode = nodes.get(i);
            
            addTextElement(doc, node, ELEMENT_ACCESSION, treeNode.getAccession());
            addTextElement(doc, node, ELEMENT_PARENT_ACCESSION, treeNode.getParentAccession());
            addTextElement(doc, node, ELEMENT_PUBLIC_ID, treeNode.getPublicId());
            addTextElement(doc, node, ELEMENT_BRANCH_LENGTH, treeNode.getBranchLength());
            addTextElement(doc, node, ELEMENT_NODE_TYPE, treeNode.getNodeType());
            addTextElement(doc, node, ELEMENT_EVENT_TYPE, treeNode.getEventType());
            addTextElement(doc, node, ELEMENT_LONG_GENE_NAME, treeNode.getLongGeneName());
        }
        
        return nodeList;
        
    }
    
    
    private static Element createNodeSFInfo(Vector <PANTHERTreeNode> nodes, Document doc) {
        if (null == nodes || null == doc) {
            return null;
        }
        
        Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
        for (int i = 0; i < nodes.size(); i++) {
            Element node = doc.createElement(ELEMENT_NODE);
            nodeList.appendChild(node);
            PANTHERTreeNode treeNode = nodes.get(i);
            
            addTextElement(doc, node, ELEMENT_ACCESSION, treeNode.getAccession());
            addTextElement(doc, node, ELEMENT_SFID, treeNode.getSfId());
            addTextElement(doc, node, ELEMENT_SFNAME, treeNode.getSfName());
        }
        
        return nodeList;
        
    }
    
    private static Element createPAINTnodesInfo(HashMap<String, Node> nodeLookup, Document doc) {
        if (null == nodeLookup || doc == null) {
            return null;
        }
        Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
        for (Node n: nodeLookup.values()) {
            NodeStaticInfo nsi = n.getStaticInfo();
            Element node = doc.createElement(ELEMENT_NODE);
            nodeList.appendChild(node);            
            addTextElement(doc, node, ELEMENT_ACCESSION, nsi.getNodeAcc());
            addTextElement(doc, node, ELEMENT_PUBLIC_ID, nsi.getPublicId());
            
            if (null != nsi.getDefinition()) {
                addTextElement(doc, node, ELEMENT_GENE_DEFINITION, nsi.getDefinition());
            }
            
            if (null != nsi.getLongGeneName()) {
                addTextElement(doc, node, ELEMENT_LONG_GENE_NAME, nsi.getLongGeneName());
            }
            
            ArrayList<String> geneNameList = nsi.getGeneName();
            if (null != geneNameList && 0 != geneNameList.size()) {
                Element eGeneNameList = doc.createElement(ELEMENT_GENE_NAME_LIST);
                node.appendChild(eGeneNameList);
                for (String geneName: geneNameList) {
                    Element eGeneName = doc.createElement(ELEMENT_GENE_NAME);
                    eGeneNameList.appendChild(eGeneName);
                    addTextElement(doc, eGeneName, ELEMENT_GENE_NAME, geneName);
                }
            }
            
            ArrayList<String> geneSymbolList = nsi.getGeneSymbol();
            if (null != geneSymbolList && 0 != geneSymbolList.size()) {
                Element eGeneSymbolList = doc.createElement(ELEMENT_GENE_SYMBOL_LIST);
                node.appendChild(eGeneSymbolList);
                for (String geneSymbol: geneSymbolList) {
                    Element eGeneSymbol = doc.createElement(ELEMENT_GENE_SYMBOL);
                    eGeneSymbolList.appendChild(eGeneSymbol);
                    addTextElement(doc, eGeneSymbol, ELEMENT_GENE_SYMBOL, geneSymbol);
                }
            }
            
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null != nvi) {
                ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
                if (null != annotList && 0 !=  annotList.size()) {
                    Element eAnnotationList = doc.createElement(ELEMENT_ANNOTATION_LIST);
                    node.appendChild(eAnnotationList);
                    for (Annotation a: annotList) {
                        Element eAnnotation = doc.createElement(ELEMENT_ANNOTATION);
                        eAnnotationList.appendChild(eAnnotation);
                        addTextElement(doc, eAnnotation, ELEMENT_ANNOTATION_ID, a.getAnnotationId());                        
                        addTextElement(doc, eAnnotation, ELEMENT_ANNOTATION_TERM, a.getGoTerm());
                        AnnotationDetail ad = a.getAnnotationDetail();
                        if (null != ad) {
                            Set<Qualifier> qualifierSet = ad.getQualifiers();
                            if (null != qualifierSet && 0 != qualifierSet.size()) {
                                Element eQualifierList = doc.createElement(ELEMENT_ANNOTATION_QUALIFIER_LIST);
                                eAnnotation.appendChild(eQualifierList);
                                for (Qualifier q: qualifierSet) {
                                    addTextElement(doc, eQualifierList, ELEMENT_ANNOTATION_QUALIFIER, q.getText());
                                }
                            }
                        }                        
                        HashSet<String> evSet = a.getEvidenceCodeSet();
                        if (null != evSet && 0 != evSet.size()) {
                            Element eEvidence = doc.createElement(ELEMENT_ANNOTATION_EVIDENCE);
                            eAnnotation.appendChild(eEvidence);
                            addTextElement(doc, eEvidence, ELEMENT_ANNOTATION_EVIDENCE_CODE, StringUtils.listToString(evSet, Constant.STR_EMPTY, Constant.STR_COMMA));
                        }
                        

                        if (null != ad) {
                            HashSet<Annotation> withAnnotSet = ad.getWithAnnotSet();
                            HashSet<Node> nodeAnnotSet = ad.getWithNodeSet();
                            HashSet<DBReference> withDbRefSet = ad.getWithOtherSet();
                            if (null != withAnnotSet || null != nodeAnnotSet || null != withDbRefSet) {
                                Element eWithList = doc.createElement(ELEMENT_ANNOTATION_WITH_LIST);
                                eAnnotation.appendChild(eWithList);
                                if (null != withAnnotSet) {
                                    for (Annotation withA: withAnnotSet) {
                                        Element withAe = doc.createElement(ELEMENT_ANNOTATION_WITH);
                                        eWithList.appendChild(withAe);
                                        addTextElement(doc, withAe, ELEMENT_ANNOTATION_TYPE, ANNOTATION_TYPE_PANTHER_ANNOTATION);
                                        addTextElement(doc, withAe, ELEMENT_ANNOTATION_VALUE, withA.getAnnotationId());                                        
                                    }
                                }
                                if (null != nodeAnnotSet) {
                                    for (Node withN: nodeAnnotSet) {
                                        Element withAe = doc.createElement(ELEMENT_ANNOTATION_WITH);
                                        eWithList.appendChild(withAe);
                                        addTextElement(doc, withAe, ELEMENT_ANNOTATION_TYPE, ANNOTATION_TYPE_PANTHER_NODE);
                                        addTextElement(doc, withAe, ELEMENT_ANNOTATION_VALUE, withN.getStaticInfo().getPublicId());                                         
                                    }
                                }
                                if (null != withDbRefSet) {
                                    for (DBReference dbRef: withDbRefSet) {
                                        Element withAe = doc.createElement(ELEMENT_ANNOTATION_WITH);
                                        eWithList.appendChild(withAe);
                                        addTextElement(doc, withAe, ELEMENT_ANNOTATION_TYPE, dbRef.getEvidenceType());
                                        addTextElement(doc, withAe, ELEMENT_ANNOTATION_VALUE, dbRef.getEvidenceValue());                                         
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        return nodeList;
    }
    
    
    private static Element createNodeGeneInfo(Vector <PANTHERTreeNode> nodes, Document doc) {
        if (null == nodes || null == doc) {
            return null;
        }
        
        Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
        for (int i = 0; i < nodes.size(); i++) {
            Element node = doc.createElement(ELEMENT_NODE);
            nodeList.appendChild(node);
            PANTHERTreeNode treeNode = nodes.get(i);
            
            addTextElement(doc, node, ELEMENT_ACCESSION, treeNode.getAccession());
            Vector <String> geneIdentifierList = treeNode.getGeneIdentifierList();
            if (null != geneIdentifierList) {
                Element geneListElement = doc.createElement(ELEMENT_GENE_IDENTIFIER_LIST);
                node.appendChild(geneListElement);
                for (int j = 0; j < geneIdentifierList.size(); j++) {
                    String identifier = geneIdentifierList.get(j);
                    addTextElement(doc, node, ELEMENT_GENE_IDENTIFIER, identifier);
                }
            }
            Vector <String> geneSymbolList = treeNode.getGeneSymbolList();
            if (null != geneSymbolList) {
                Element geneSymbolElement = doc.createElement(ELEMENT_GENE_SYMBOL_LIST);
                node.appendChild(geneSymbolElement);
                for (int j = 0; j < geneSymbolList.size(); j++) {
                    String symbol = geneSymbolList.get(j);
                    addTextElement(doc, node, ELEMENT_GENE_SYMBOL, symbol);
                }
            }
        }
        
        return nodeList;
        
    }


    private static Element createNodeIdentifierInfo(Vector <PANTHERTreeNode> nodes, Document doc) {
        if (null == nodes || null == doc) {
            return null;
        }
        
        Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
        for (int i = 0; i < nodes.size(); i++) {
            Element node = doc.createElement(ELEMENT_NODE);
            nodeList.appendChild(node);
            PANTHERTreeNode treeNode = nodes.get(i);
            
            
            Hashtable<String, Vector <String>> identifierTbl  = treeNode.getIdentifierTbl();
            if (null == identifierTbl || 0 == identifierTbl.size()) {
                continue;
            }
            addTextElement(doc, node, ELEMENT_ACCESSION, treeNode.getAccession());
            Element idList = doc.createElement(ELEMENT_IDENTIFIER_LIST);
            node.appendChild(idList);
            Enumeration<String> types = identifierTbl.keys();
            while (types.hasMoreElements()) {
                String type = types.nextElement();
                Element idInfo = doc.createElement(ELEMENT_IDENTIFIER_INFO);
                idList.appendChild(idInfo);
                addTextElement(doc, idInfo, ELEMENT_IDENTIFIER_TYPE, type);
                Vector<String> identifiers = identifierTbl.get(type);
                for (int j = 0; j < identifiers.size(); j++) {
                    addTextElement(doc, idInfo, ELEMENT_IDENTIFIER, identifiers.get(j));
                }
                
            }

        }
        
        return nodeList;
        
    }

    private static void addTextElement (Document doc, Element parent, String type, String text) {
        if (null == doc || null == parent || null == type || null == text) {
            return;
        }
        
        Element elem = doc.createElement(type);
        Text textElem = doc.createTextNode(text);
        elem.appendChild(textElem);
        parent.appendChild(elem);
    }
    
    
    private static String outputAnnotationInfoXML (Hashtable<String, Vector<Evidence>>  evidenceTbl, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element nodesElement = outputNodeEvidence(evidenceTbl, doc);
            if (null != nodesElement) {
                root.appendChild(nodesElement);
            }
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }
    
    
    private static Element outputPANTHERNodeEvidence (Hashtable<String, Hashtable<String, Evidence>>  evidenceTbl, Document doc) {
        if (null == evidenceTbl || null == doc) {
            return null;
        }
        
        Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
        Enumeration <String> nodes = evidenceTbl.keys();
        while (nodes.hasMoreElements()) {
            String nodeName = nodes.nextElement();
            Hashtable<String, Evidence> evidenceForNodeTbl = evidenceTbl.get(nodeName);
            Element node = doc.createElement(ELEMENT_NODE);
            nodeList.appendChild(node);
            
            
            addTextElement(doc, node, ELEMENT_ACCESSION, nodeName);
            
            Element evidenceLst = doc.createElement(ELEMENT_EVIDENCE_LIST);
            node.appendChild(evidenceLst);
            
            Enumeration <Evidence> evidenceEnum = evidenceForNodeTbl.elements();
            while (evidenceEnum.hasMoreElements()) {
                Evidence evdnce = evidenceEnum.nextElement();
                Element evidence = doc.createElement(ELEMENT_EVIDENCE);
                evidenceLst.appendChild(evidence);
                
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_ACCESSION, evdnce.getAccession());
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_NAME, evdnce.getName());
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_TYPE, evdnce.getType());
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_QUALIFIER, evdnce.getQualifierStr());
            
            
                Vector <EvidenceSpecifier> esList = evdnce.getEvidenceSpecifierList();
                if (null == esList) {
                    continue;
                }
            
                Element evidenceSpecifierList = doc.createElement(ELEMENT_EVIDENCE_SPECIFIER_LIST);
                evidence.appendChild(evidenceSpecifierList);
                for (int j = 0; j < esList.size(); j++) {
                    EvidenceSpecifier es = esList.get(j);
                    Element evidenceSpecifier = doc.createElement(ELEMENT_EVIDENCE_SPECIFIER);
                    evidenceSpecifierList.appendChild(evidenceSpecifier);
                    addTextElement(doc, evidenceSpecifier, ELEMENT_EVIDENCE_SPECIFIER_ID, es.getEvidenceId());
                    addTextElement(doc, evidenceSpecifier, ELEMENT_EVIDENCE_SPECIFIER_WITH, es.getWith());
                    addTextElement(doc, evidenceSpecifier, ELEMENT_EVIDENCE_SPECIFIER_CODE, es.getEvidenceCode());
                }
                
            }
        }
        
        return nodeList;
    }

    
    private static Element outputNodeEvidence (Hashtable<String, Vector<Evidence>>  evidenceTbl, Document doc) {
        if (null == evidenceTbl || null == doc) {
            return null;
        }
        
        Element nodeList = doc.createElement(ELEMENT_NODE_LIST);
        Enumeration <String> nodes = evidenceTbl.keys();
        while (nodes.hasMoreElements()) {
            String nodeName = nodes.nextElement();
            Vector<Evidence> evidenceList = evidenceTbl.get(nodeName);
            Element node = doc.createElement(ELEMENT_NODE);
            nodeList.appendChild(node);
            
            
            addTextElement(doc, node, ELEMENT_ACCESSION, nodeName);
            
            Element evidenceLst = doc.createElement(ELEMENT_EVIDENCE_LIST);
            node.appendChild(evidenceLst);
            
            for (int i = 0; i < evidenceList.size(); i++) {
                Element evidence = doc.createElement(ELEMENT_EVIDENCE);
                evidenceLst.appendChild(evidence);
                Evidence evdnce = evidenceList.get(i);
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_ACCESSION, evdnce.getAccession());
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_NAME, evdnce.getName());
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_TYPE, evdnce.getType());
                addTextElement(doc, evidence, ELEMENT_EVIDENCE_QUALIFIER, evdnce.getQualifierStr());
                
                
                Vector <EvidenceSpecifier> esList = evdnce.getEvidenceSpecifierList();
                if (null == esList) {
                    continue;
                }
                
                Element evidenceSpecifierList = doc.createElement(ELEMENT_EVIDENCE_SPECIFIER_LIST);
                evidence.appendChild(evidenceSpecifierList);
                for (int j = 0; j < esList.size(); j++) {
                    EvidenceSpecifier es = esList.get(j);
                    Element evidenceSpecifier = doc.createElement(ELEMENT_EVIDENCE_SPECIFIER);
                    evidenceSpecifierList.appendChild(evidenceSpecifier);
                    addTextElement(doc, evidenceSpecifier, ELEMENT_EVIDENCE_SPECIFIER_ID, es.getEvidenceId());
                    addTextElement(doc, evidenceSpecifier, ELEMENT_EVIDENCE_SPECIFIER_WITH, es.getWith());
                    addTextElement(doc, evidenceSpecifier, ELEMENT_EVIDENCE_SPECIFIER_CODE, es.getEvidenceCode());
                }
            }
            

        }
        
        return nodeList;
    }
    
    
    
    private static String outputPANTHERAnnotationsXML (Hashtable<String, Hashtable<String, Evidence>>  evidenceTbl, String book, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_BOOK);
            Text searchStringText = doc.createTextNode(book);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element nodesElement = outputPANTHERNodeEvidence(evidenceTbl, doc);
            if (null != nodesElement) {
                root.appendChild(nodesElement);
            }
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }



    
    
    
    /*
     * select *
    from node n, node_type nt, event_type et
    where n.accession like 'PTHR10000%'
    and n.node_type_id = nt.NODE_TYPE_ID
    and n.EVENT_TYPE_ID = et.EVENT_TYPE_ID
    
    
    
    
    
    
    
     select n.accession, n.branch_length, nt.node_type, et.event_type
         from node n, node_type nt, event_type et
         where n.accession like 'PTHR10000%'
         and n.CLASSIFICATION_VERSION_SID = 11
         and n.node_type_id = nt.NODE_TYPE_ID (+)
         and n.EVENT_TYPE_ID = et.EVENT_TYPE_ID (+)
         and n.OBSOLESCENCE_DATE is null
         order by n.accession;
         
         
         
         select n.accession, n.branch_length
         from node n
         where n.accession like 'PTHR10000:%'
         and n.OBSOLESCENCE_DATE is null
         order by n.accession;
         
         
         select n.accession, n2.accession as parent, n.PUBLIC_ID, n.branch_length, nt.node_type, et.event_type, g.PRIMARY_EXT_ACC
         from node n, node_type nt, event_type et, gene g, gene_node gn, node_relationship nr, node n2
         where n.accession like 'PTHR10000%'
         and n.CLASSIFICATION_VERSION_SID = 11
         and n.node_type_id = nt.NODE_TYPE_ID (+)
         and n.EVENT_TYPE_ID = et.EVENT_TYPE_ID (+)
         and n.node_id = gn.node_id (+)
         and gn.gene_id = g.gene_id (+)
         and n.node_id = nr.child_node_id (+)
         and nr.parent_node_id = n2.node_id (+);
    
    
    
    
     select node.accession as accession, p.PRIMARY_EXT_ID, c.accession
     as go_accession, c.name as go_name, DECODE(ctt.TERM_NAME, 'molecular_function', 'F','cellular_component', 'C','biological_process','P') as aspect,
     e.EVIDENCE, cc.CONFIDENCE_CODE, q.QUALIFIER
     from node , protein_node pn, protein p, protein_classification pc, classification c, evidence e, confidence_code cc, classification_term_type ctt, pc_qualifier pcq, qualifier q
     where node.classification_version_sid = ? and node.accession like ? and 
     node.node_id = pn.node_id and pn.protein_id = p.protein_id and p.protein_id = pc.PROTEIN_ID and
     pc.classification_id = c.classification_id and c.CLASSIFICATION_VERSION_SID = %1
     and c.TERM_TYPE_SID = ctt.TERM_TYPE_SID and pc.protein_classification_id = e.protein_classification_id
     and e.CONFIDENCE_CODE_SID = cc.CONFIDENCE_CODE_SID  and cc.CONFIDENCE_CODE_SID in (%2)
     and pc.PROTEIN_CLASSIFICATION_ID = pcq.PROTEIN_CLASSIFICATION_ID (+) and pcq.QUALIFIER_ID = q.QUALIFIER_ID (+) ";
    
    
    
    
     */

}
