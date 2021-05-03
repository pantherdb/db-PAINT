/**
 *  Copyright 2020 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintServer.webservices;

import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.database.DataServer;
import com.sri.panther.paintServer.database.DataServerManager;
import com.sri.panther.paintServer.servlet.Client2Servlet;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationNode;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


public class TreeLogic {
    protected String familyId;
    protected String familyName;
    protected boolean invalidFamilyId = false;
    protected AnnotationNode root;
    protected Hashtable<String, AnnotationNode> idToNodeTbl = new Hashtable<String, AnnotationNode>();
    public static final String ERROR_MSG_DATA_FOR_NON_AN =
        "Found data for non-existent annotation node ";
    //public static final String UPL_VERSION = ConfigFile.getProperty("cls_version_sid");
    protected static final String DELIM_SEMI_COLON = ";";
    protected static final String DELIM_BRACKET_OPEN = "(";
    protected static final String DELIM_BRACKET_CLOSE = ")";
    protected static final String DELIM_COMMA = ",";
    protected static final String DELIM_COLON = ":";
    protected static final String DELIM_SQUARE_BRACKET_OPEN = "[";
    protected static final String DELIM_SQUARE_BRACKET_CLOSE = "]";
    public static final String DELIM_TREE = ",();";
    
    public TreeLogic() {
        
    }

    
    public static Hashtable<String, String> getFamilyLookup(String familyId, String uplVersion, String database) {
        if (null == familyId) {
            return null;
        }

        if (null == database) {
            database = WSConstants.PROPERTY_DB_STANDARD;
        }
        
        if (null == uplVersion) {
            uplVersion = WSConstants.PROPERTY_CLS_VERSION;
        }
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds) {
            return null;
        }
        return ds.getAnnotationNodeLookup(familyId, uplVersion);
    }


    public static Hashtable<String, String> getFamilyNamesLookup(String ids, String uplVersion, String database) {
        if (null == ids) {
            return null;
        }

        if (null == database) {
            database = WSConstants.PROPERTY_DB_STANDARD;
        }
        
        if (null == uplVersion) {
            uplVersion = WSConstants.PROPERTY_CLS_VERSION;
        }
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds) {
            return null;
        }
        
        String idList[] = Utils.tokenize(ids, DELIM_COMMA);
        int length = idList.length;
        Hashtable<String, String>  idLookup = new Hashtable<String, String> (length);
        for (int i = 0; i < length; i++) {
            String id = idList[i];
            if (null == id) {
                continue;
            }
            String value = ds.getFamSubFamAccToName(uplVersion, id);
            if (null == value) {
                continue;
            }
            idLookup.put(id, value);
        }
        return idLookup;

    }
    
    public boolean setTreeStructure(String familyId, String uplVersion, HashMap <String, Node> nodeLookupTbl) {
        this.familyId = familyId;
        if (null == familyId) {
            invalidFamilyId = true;
            return false;
        }
        
        if (null == uplVersion) {
            uplVersion = WSConstants.PROPERTY_CLS_VERSION;
        }
        DataIO di = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        String[] treeStrings = di.getTree(familyId, uplVersion);
        
        if (null == treeStrings) {
            invalidFamilyId = true;            
            return false;
        }
        root = parse(treeStrings);
        di.getAnnotationNodeLookup(familyId, uplVersion, nodeLookupTbl);
        // Save public id gene symbol
        Vector <AnnotationNode> nodeList = new Vector(idToNodeTbl.values());

        for (int i = 0; i < nodeList.size(); i++) {
            AnnotationNode an = nodeList.get(i);
            String accession = an.getAccession();
            an.setPublicId(nodeLookupTbl.get(accession).getStaticInfo().getPublicId());
            an.setLongName(an.getNodeName());       // Do not clobber the long name

        }
        return true;
    }


    
    public boolean setTreeInfoFromFamilyId(String familyId, String uplVersion, String database) {
        this.familyId = familyId;
        if (null == familyId) {
            invalidFamilyId = true;
            return false;
        }

        if (null == database) {
            database = WSConstants.PROPERTY_DB_STANDARD;
        }
        
        if (null == uplVersion) {
            uplVersion = WSConstants.PROPERTY_CLS_VERSION;
        }
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds) {
            return false;
        }
        String treeStrings[] = Client2Servlet.getTree(ds, familyId, uplVersion);
        
        if (null == treeStrings) {
            invalidFamilyId = true;
            return false;
        }
        root = parse(treeStrings);
        Hashtable<String, String> nodeLookupTbl = ds.getAnnotationNodeLookup(familyId, uplVersion);
        Vector<Hashtable<String, String>> geneInfo = ds.getGeneInfo(familyId, uplVersion);
        Hashtable<String, String> geneSymbolTbl = null; 
        Hashtable<String, String> longGeneTbl = null;
        Hashtable<String, String> geneNameTbl = null;
        if (null != geneInfo) {
            geneSymbolTbl = geneInfo.get(ds.INDEX_TBL_GENE_SYMBOL);
            longGeneTbl = geneInfo.get(ds.INDEX_TBL_GENE_ID);
            geneNameTbl = geneInfo.get(ds.INDEX_TBL_GENE_NAME);
        }
        
        
        // Save public id gene symbol
        Vector <AnnotationNode> nodeList = new Vector(idToNodeTbl.values());
        if (null != nodeLookupTbl && null != geneInfo) {
            for (int i = 0; i < nodeList.size(); i++) {
                AnnotationNode an = nodeList.get(i);
                String accession = an.getAccession();
                an.setPublicId(nodeLookupTbl.get(accession));
//                System.out.println(an.getAccession() + " " + an.getPublicId());
                an.setGeneSymbol(geneSymbolTbl.get(accession));
                an.setLongName(an.getNodeName());       // Do not clobber the long name
                an.setNodeName(longGeneTbl.get(accession));
                an.setGeneName(geneNameTbl.get(accession));
            }
        }
        
        // Save subfamily information
        Vector <Hashtable<String, String>> sfInfo = ds.getAnSfIdInfo(familyId, uplVersion);
        if (null != sfInfo) {
            Hashtable<String, String> anSfIdLookup = sfInfo.get(ds.INFO_INDEX_AN_SF_ID);
            Hashtable<String, String> anSfNameLookup = sfInfo.get(ds.INFO_INDEX_AN_SF_NAME);
            for (int i = 0; i < nodeList.size(); i++) {
                AnnotationNode an = nodeList.get(i);
                String accession = an.getAccession();
                an.setSfId(anSfIdLookup.get(accession));
                //                System.out.println(an.getAccession() + " " + an.getPublicId());
                an.setSfName(anSfNameLookup.get(accession));
            }            
        }
        
        
        // Save family name
        familyName = ds.getFamilyName(familyId, uplVersion);
        Vector msaInfo = (Vector) Client2Servlet.getMSA(familyId, uplVersion);
        
        
        MSAUtil.parsePIRForNonGO(msaInfo, nodeList, true);
        return true;
    }

    /**
     * 
     * @param annotId   Accession or public id
     * @param uplVersion
     * @param database
     * @return
     */    
    public boolean  setTreeInfoFromNodeId(String annotId, String uplVersion, String database) {
        if (null == annotId || null == uplVersion || null == database) {
            return false;
        }
        
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds) {
            return false;
        }
        String familyId = ds.getFamilyIdFromNodeId(annotId, uplVersion);
        return setTreeInfoFromFamilyId(familyId, uplVersion, database);
    }
    
    /**
     * 
     * @param annotId   Accession or public id
     * @param uplVersion
     * @param database
     * @return
     */
    public static String getFamilyIdFromNodeId(String annotId, String uplVersion, String database) {
        if (null == annotId || null == uplVersion || null == database) {
            return null;
        }
        
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds) {
            return null;
        }
        return ds.getFamilyIdFromNodeId(annotId, uplVersion);        
    }
    
    public static String getSequenceForNodeId (String annotPubId, String uplVersion, String database) {
        if (null == annotPubId || null == uplVersion || null == database) {
            return null;
        }
        
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds) {
            return null;
        }
        AnnotationNode node = ds.getNodeFromNodeId(annotPubId, uplVersion);
        if (null == node) {
            return null;
        }
        String familyId = node.getFamilyId();
        if (null == familyId) {
            return null;
        }
        Vector <AnnotationNode> nodeList = new Vector<AnnotationNode>();
        nodeList.add(node);
        Vector msaInfo = (Vector) Client2Servlet.getMSA(familyId, uplVersion);
        MSAUtil.parsePIRForNonGO(msaInfo, nodeList, false);
        return node.getSequence();
    }

    
    public AnnotationNode getRoot() {
        return root;
    }

    public AnnotationNode parse(String[] treeContents) {
        if (null == treeContents) {
            invalidFamilyId = true;
            return null;
        }
        if (0 == treeContents.length) {
            invalidFamilyId = true;
            return null;
        }
        // Modify, if there are no line returns
        if (1 == treeContents.length) {
            treeContents = Utils.tokenize(treeContents[0], DELIM_SEMI_COLON);
        }

        //          // Get subfamily to annotation node relationships
        //          Hashtable AnToSFTbl = parseSfAnInfo(sfAn, true);
        //          if (null == AnToSFTbl) {
        //              return null;
        //          }


        AnnotationNode mainTree = parse(treeContents[0]);
        //          String mainTreeAnnotationNodeName = mainTree.getAnnotationId();
        //          String sfName = (String)AnToSFTbl.get(mainTreeAnnotationNodeName);
        //          if (null != sfName) {
        //              mainTree.setSubFamilyName(sfName);
        //              mainTree.setIsSubfamily(true);
        //          }
        Hashtable subTreeTable = new Hashtable();
        setNodes(mainTree, subTreeTable);
        //          addSfInfoToMainTree(mainTree, AnToSFTbl);
        int index;
        String anName;
        AnnotationNode subtree;
        AnnotationNode annotationNode;
        for (int i = 1; i < treeContents.length; i++) {
            index = treeContents[i].indexOf(DELIM_COLON);
            anName = treeContents[i].substring(0, index);
            subtree =
                    parse(treeContents[i].substring(index + 1, treeContents[i].length()));
            annotationNode = (AnnotationNode)subTreeTable.get(anName);
            if (null == annotationNode) {
                System.out.println(ERROR_MSG_DATA_FOR_NON_AN + anName);
                continue;
            }
            subtree.setAnnotationNodeId(annotationNode.getNodeName());
            subtree.setParent(annotationNode.getParent());

            String branchLength = annotationNode.getBranchLength();
            if (null != branchLength) {
                subtree.setBranchLength(branchLength);
            }
            //              subtree.setDistanceFromRoot(annotationNode.getDistanceFromRoot());

            //              String annotationNodeId = subtree.getAnnotationNodeId();
            //              sfName = (String)AnToSFTbl.get(annotationNodeId);
            //              if (null != sfName) {
            //                  subtree.setSubFamilyName(sfName);
            //                  subtree.setIsSubfamily(true);
            //              }

            AnnotationNode parent = annotationNode.getParent();
            if (null != parent) {
                Vector children = parent.getChildren();
                int j = 0;
                while (j < children.size()) {
                    AnnotationNode tmp = (AnnotationNode)children.elementAt(j);
                    if (tmp.equals(annotationNode)) {
                        children.setElementAt(subtree, j);
                        break;
                    }
                    j++;
                }
            }
            // Single subfamily tree with root as subfamily
            else {
                mainTree = subtree;
            }
        }
        initNodeProperties(mainTree);
        subTreeTable = null;

        return mainTree;

    }


    protected AnnotationNode parse(String s) {
        AnnotationNode node = null;
        AnnotationNode root = null;
        StringTokenizer st = new StringTokenizer(s, DELIM_TREE, true);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.equals(DELIM_BRACKET_OPEN)) {
                if (null == node) {
                    node = new AnnotationNode();
                    root = node;
                }
                else {
                    AnnotationNode newChild = new AnnotationNode();
                    Vector children = node.getChildren();
                    if (null == children) {
                        children = new Vector();
                    }
                    children.addElement(newChild);
                    newChild.setParent(node);
                    node.setChildren(children);
                    node = newChild;
                }
            } else if ((token.equals(DELIM_BRACKET_CLOSE)) ||
                       (token.equals(DELIM_COMMA)) ||
                       (token.equals(DELIM_SEMI_COLON))) {
                // Do nothing
            } else {
                int index = token.indexOf(DELIM_COLON);
                int squareIndexStart =
                    token.indexOf(DELIM_SQUARE_BRACKET_OPEN);
                int squareIndexEnd = token.indexOf(DELIM_SQUARE_BRACKET_CLOSE);
                if (0 == squareIndexStart) {
                    node.setType(token.substring(squareIndexStart, squareIndexEnd + 1));
                } else {
                    if (-1 == index) {
                        if (null == node) {
                            node = new AnnotationNode();
                            node.setNodeName(token);
                            root = node;
                        }
                    }
                    else if (0 == index) {
                        if (-1 == squareIndexStart) {
                            node.setBranchLength(token.substring(index + 1));
                            //                                                Distance d = node.getDistance();
                            //                                                if (d == null) {
                            //                                                    d = new Distance();
                            //                                                    node.setDistance(d);
                            //                                                }
                            //                                                d.setDistanceFromParent(Float.valueOf(token.substring(index+1)).floatValue());
                        }
                        else {
                            //                                             Distance d = node.getDistance();
                            //                                             if (null == d) {
                            //                                                 d = new Distance();
                            //                                                 node.setDistance(d);
                            //                                             }
                            node.setBranchLength(token.substring((index + 1), squareIndexStart));
                            //                                             d.setDistanceFromParent(Float.valueOf(token.substring((index+1), squareIndexStart)).floatValue());
                            node.setType(token.substring(squareIndexStart, squareIndexEnd + 1));
                        }
                        //                                        node = (DrawableSequenceNode)node.getParent();
                        node = node.getParent();
                    }
                    else {
                        AnnotationNode newChild = new AnnotationNode();
                        newChild.setNodeName(token.substring(0, index));
                        //                                        newChild.setTerminus(true);
                        if (-1 == squareIndexStart) {
                            //                                            Distance d = newChild.getDistance();
                            //                                            if (null == d) {
                            //                                                d = new Distance();
                            //                                                newChild.setDistance(d);
                            //                                            }
                            newChild.setBranchLength(token.substring(index + 1));
                            //                                            d.setDistanceFromParent(Float.valueOf(token.substring(index+1)).floatValue());
                        }
                        else {
                            //                                            Distance d = newChild.getDistance();
                            //                                            if (null == d) {
                            //                                                d = new Distance();
                            //                                                newChild.setDistance(d);
                            //                                            }
                            newChild.setBranchLength(token.substring((index + 1), squareIndexStart));
                            //                                            d.setDistanceFromParent(Float.valueOf(token.substring((index+1), squareIndexStart)).floatValue());
                            newChild.setType(token.substring(squareIndexStart, squareIndexEnd + 1));
                        }

                        Vector children = node.getChildren();
                        if (null == children) {
                            children = new Vector();
                        }
                        children.addElement(newChild);
                        newChild.setParent(node);
                        node.setChildren(children);
                        //                                        node.setIsExpanded(true);
                        //                                        node.setTerminus(false);
                    }
                }
            }
        }
        return root;
    }


    protected void setNodes(AnnotationNode node, Hashtable nodesTable) {
        if (null == node) {
            return;
        }
        String nodeName = node.getNodeName();
        if (null != nodeName) {
            nodesTable.put(nodeName, node);
        }
        Vector v = node.getChildren();
        if (null == v) {
            return;
        }
        for (int i = 0; i < v.size(); i++) {
            setNodes((AnnotationNode)v.elementAt(i), nodesTable);
        }
    }


    protected void initNodeProperties(AnnotationNode n) {
        String id = n.getAnnotationNodeId();
        n.setAccession(familyId + DELIM_COLON + id);
//        System.out.println(id + "\t" + n.getBranchLength() + "\t" + n.getNodeType() + "\t" + n.getNodeName());
        if (null != id) {
            idToNodeTbl.put(id, n);
        }
        if (null == n.getBranchLength()) {
            n.setBranchLength(Integer.toString(0));
        }
        Vector children = n.getChildren();
        if (null == children) {
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            AnnotationNode child = (AnnotationNode)children.get(i);
            initNodeProperties(child);
        }

    }
    
    public AnnotationNode getNode(String annotId) {
        if (null == annotId) {
            return null;
        }
        AnnotationNode an = (AnnotationNode)idToNodeTbl.get(annotId);
        if (null != an || null == familyId) {
            return an;
        }
        
        // Check by removing family portion of id
        int index = annotId.indexOf(DELIM_COLON);
        index++;
        if (index >= 0 && index < annotId.length()) {
            return (AnnotationNode)idToNodeTbl.get(annotId.substring(index));
        }
        return null;
        
    }


    public static void main(String[] args) {
        TreeLogic tl = new TreeLogic();
        tl.setTreeInfoFromFamilyId("PTHR10000", null, null);
    }


    public Hashtable<String, AnnotationNode> getIdToNodeTbl() {
        return idToNodeTbl;
    }

    public String getFamilyId() {
        return familyId;
    }

    public String getFamilyName() {
        return familyName;
    }
}

