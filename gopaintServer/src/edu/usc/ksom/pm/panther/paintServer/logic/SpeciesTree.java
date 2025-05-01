/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * Copyright 2025 University Of Southern California
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

package edu.usc.ksom.pm.panther.paintServer.logic;


import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.util.ConfigFile;
import com.sri.panther.paintServer.logic.OrganismManager;
import edu.usc.ksom.pm.panther.paintCommon.Organism;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class SpeciesTree {
    
    private static SpeciesTree instance;

    public static final String URL_SPECIES_TREE =  ConfigFile.getProperty("species_tree");
    public static final String treeBuf = Utils.readFromUrl(URL_SPECIES_TREE, -1, -1);
    // public static final AnnotationNode SPECIES_TREE_ROOT = generateSpeciesTree(treeBuf.toString().trim()); Does not work on NWX format
    public static final NewickTree SPECIES_TREE_NEWICK = NewickTree.readNewickFormat(treeBuf.trim());
    private static OrganismManager organismManager = OrganismManager.getInstance();
    
    private Organism root;
    private static HashMap<String, Organism> longNameOrgLookup;
    private static HashMap<String, Organism> shortOrgLookup;
    private static HashMap<String, Organism> taxonToOrganism;
    private static final String CLADE_PLANT = ConfigFile.getProperty("clade.plant");
    private static final String CLADE_BACTERIA = ConfigFile.getProperty("clade.bacteria");    
    private static ArrayList<String> speciesListPlant;
    private static ArrayList<String> speciesListBacteria;
   

    public static final String ELEMENT_SPECIES_TREE = "species_tree";
    public static final String ELEMENT_NODE = "node";
    public static final String ELEMENT_NAME = "name";
    public static final String ELEMENT_SHORT_NAME = "short_name";
    public static final String ELEMENT_TAXON_ID = "taxon_id";
    public static final String ELEMENT_CHILDREN = "children";
    public static final String ELEMENT_DESC = "desc";
    public static final int PRETTY_PRINT_INDENT_FACTOR = 4;
    
    public static final String DELIM_TAXONOMY = ",";
    
    public enum FORMAT {
        XML,
        JSON
    };
    
    private SpeciesTree() {

    }
    
    public static synchronized SpeciesTree getInstance() {
        if (null != instance) {
            return instance;
        }
        longNameOrgLookup = new HashMap<String, Organism>();
        shortOrgLookup = new HashMap<String, Organism>();
        taxonToOrganism = new HashMap<String, Organism>();
        for (Organism org: organismManager.getOrgList()) {
            String longName = org.getLongName();
            if (null != longName) {
                longNameOrgLookup.put(longName, org);
            }
            String shortName = org.getShortName();
            if (null != shortName) {
                shortOrgLookup.put(shortName, org);
            }
            String taxon = org.getTaxonId();
            if (null != taxon) {
                taxonToOrganism.put(taxon, org);
            }
        }
        speciesListPlant = getDescendantLeaves(CLADE_PLANT);
        speciesListBacteria = getDescendantLeaves(CLADE_BACTERIA);
        
        instance = new SpeciesTree();
        return instance;
    }

    public String getSpeciesTreeStr(FORMAT format) {
        String xmlTree = getXMLForSpeciesTreeStr();
        if (FORMAT.XML == format) {
            return xmlTree;
        }        
        JSONObject xmlJSONObj = XML.toJSONObject(xmlTree);
        return xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
    }
    

    
    public String getXMLForSpeciesTreeStr() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement(ELEMENT_SPECIES_TREE);
            doc.appendChild(root);
            addNodeInfo(doc, root, SPECIES_TREE_NEWICK.root);
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    

    
    
    public static void addNodeInfo(Document doc, Element parent, NewickTree.Node node) {
        if (null == node) {
            return;
        }

        Element e = doc.createElement(ELEMENT_NODE);
        Element name = doc.createElement(ELEMENT_NAME);
//        Element level = doc.createElement(ELEMENT_LEVEL);
        e.appendChild(name);
//        e.appendChild(level);
        String nodeName = node.getName();

//        level.appendChild(doc.createTextNode(Integer.toString(node.level)));
        if (null != nodeName) {
            Organism o = shortOrgLookup.get(nodeName);
            if (null != o) {
                if (nodeName.equals(o.getLongName())) {
                    name.appendChild(doc.createTextNode(nodeName));
                }
                else if (null != o.getShortName()) {
                    name.appendChild(doc.createTextNode(o.getLongName()));
                    Element shortNameElem = doc.createElement(ELEMENT_SHORT_NAME);
                    e.appendChild(shortNameElem);
                    shortNameElem.appendChild(doc.createTextNode(o.getShortName()));
                }
                else {
                    name.appendChild(doc.createTextNode(nodeName));
                }
                String taxon = o.getTaxonId();
                if (null != taxon) {
                    int taxonId = Integer.parseInt(taxon);
                    if (taxonId > 0) {
                        Element taxonIdElem = doc.createElement(ELEMENT_TAXON_ID);
                        e.appendChild(taxonIdElem);
                        taxonIdElem.appendChild(doc.createTextNode(Integer.toString(taxonId)));
                    }
                }
                StringBuffer info = new StringBuffer();
                String conversion = o.getConversion();                
                if (conversion != null) {
                    info.append(conversion);
                }
                String commonName = o.getCommonName();
                if (null != commonName) {
                    if (0 == info.length()) {
                        info.append(commonName);
                    }
                    else {
                        info.append(Constant.STR_SPACE);
                        info.append(Constant.STR_DASH);
                        info.append(Constant.STR_SPACE);
                        info.append(commonName);
                    }
                }
                if (0 != info.length()) {
                    Element desc = doc.createElement(ELEMENT_DESC);
                    e.appendChild(desc);
                    desc.appendChild(doc.createTextNode(info.toString()));
                }
                            
            }
            else {
                name.appendChild(doc.createTextNode(nodeName));
            }
        }
        parent.appendChild(e);

        ArrayList<NewickTree.Node> children = node.children;
        if (null != children && 0 != children.size()) {
            Element childElem = doc.createElement(ELEMENT_CHILDREN);
            e.appendChild(childElem);
            for (NewickTree.Node child: children) {
                addNodeInfo(doc, childElem, child);
            }
        }
    }
    
    public static String getApplicableTaxons(String orgList) {
        if (null == orgList) {
            return null;
        }
        String orgs[] = orgList.split(DELIM_TAXONOMY);
        if (null == orgs) {
            return null;
        }
        HashSet<String> taxonSet = new HashSet<String>();
        for (String org: orgs) {
            Organism o = longNameOrgLookup.get(org);
            if (null != o && o.getTaxonId() != null && null != organismManager.getOrganismForShortName(org)) {
                taxonSet.add(o.getTaxonId());
            }
        }
        return Utils.listToString(new Vector(taxonSet), Constant.STR_EMPTY, DELIM_TAXONOMY);
    }
     
    public static ArrayList<String> getShortOrgsForTaxons(String taxonList) {
        if (null == taxonList) {
            return null;
        }
        ArrayList<String> orgList = new ArrayList<String>();
        String taxonArray[] = taxonList.split(DELIM_TAXONOMY);
        if (null == taxonArray) {
            return null;
        }

        for (String taxonId: taxonArray) { 
            Organism o = taxonToOrganism.get(taxonId);
            if (null == o) {
                continue;
            }
            orgList.add(o.getShortName());
        }
        return orgList;
    }
    
    public static ArrayList<String> getDescendantLeaves(String nodeName) {
        if (null == nodeName) {
            return null;
        }
        ArrayList<NewickTree.Node> nodes = SPECIES_TREE_NEWICK.nodeList;
        NewickTree.Node node = null;
        for (NewickTree.Node n: nodes) {
            if (nodeName.equals(n.name)) {
                node = n;
                break;
            }
        }
        if (null == node) {
            return null;
        }
        ArrayList<NewickTree.Node> descendants = new ArrayList<NewickTree.Node>();
        getDescendents(node, descendants);
        ArrayList<String> leafDescList = new ArrayList<String>();
        for (NewickTree.Node desc: descendants) {
            ArrayList<NewickTree.Node> children = desc.children;
            if (null == children || 0 == children.size()) {
                if (null != desc.name) {
                    leafDescList.add(desc.name);
                }
            }
        }
        return leafDescList;
    }
    
    private static void getDescendents (NewickTree.Node node, ArrayList<NewickTree.Node> descList) {
        ArrayList<NewickTree.Node> children = node.children;
        if (null == children || 0 == children.size()) {
            return;
        }
        for (NewickTree.Node child: children) {
            descList.add(child);
            getDescendents(child, descList);
        }
    }

    public ArrayList<String> getSpeciesListPlant() {
        return speciesListPlant;
    }

    public ArrayList<String> getSpeciesListBacteria() {
        return speciesListBacteria;
    }
    

    public static void main(String args[]) {
        NewickTree nt = SPECIES_TREE_NEWICK;
        System.out.println(SpeciesTree.getInstance().getXMLForSpeciesTreeStr());
    }
}

