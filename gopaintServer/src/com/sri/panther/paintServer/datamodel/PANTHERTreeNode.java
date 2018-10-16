/**
 * Copyright 2018 University Of Southern California
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

import java.util.Hashtable;
import java.util.Vector;

public class PANTHERTreeNode {
    protected PANTHERTreeNode parent;
    Vector<PANTHERTreeNode> children;
    String accession;
    String nodeId;
    String parentAccession;
    String publicId;
    String branchLength;
    String nodeType;
    String eventType;
    String longGeneName;
    String sfId;
    String sfName;
    Vector <String> geneIdentifierList;
    Vector <String> geneSymbolList;
    Hashtable <String, Vector<String>> identifierTbl;
    
    
    
    public PANTHERTreeNode() {
    }

    public void setParent(PANTHERTreeNode parent) {
        this.parent = parent;
    }

    public PANTHERTreeNode getParent() {
        return parent;
    }

    public void setChildren(Vector<PANTHERTreeNode> children) {
        this.children = children;
    }
    
    public void addChild(PANTHERTreeNode child) {
        if (null == child) {
            return;
        }
        if (null == children) {
            children = new Vector<PANTHERTreeNode>(1);
        }
        children.add(child);
        
    }

    public Vector<PANTHERTreeNode> getChildren() {
        return children;
    }


    
    
    public static void getLeafDescendants(PANTHERTreeNode node, Vector<PANTHERTreeNode> descendants) {
        if (null == node) {
            return;
        }
        
        Vector<PANTHERTreeNode> nodeChildren  = node.getChildren();
        if (null == nodeChildren) {
            return;
        }
        
        for (int i = 0; i < nodeChildren.size(); i++ ) {
           PANTHERTreeNode child = nodeChildren.get(i);
           if (null == child.getChildren()) {
               descendants.add(child);
               continue;
           }
           getLeafDescendants(child, descendants);
        }
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getAccession() {
        return accession;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setParentAccession(String parentAccession) {
        this.parentAccession = parentAccession;
    }

    public String getParentAccession() {
        return parentAccession;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setBranchLength(String branchLength) {
        this.branchLength = branchLength;
    }

    public String getBranchLength() {
        return branchLength;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setLongGeneName(String longGeneName) {
        this.longGeneName = longGeneName;
    }

    public String getLongGeneName() {
        return longGeneName;
    }

    public void setSfId(String sfId) {
        this.sfId = sfId;
    }

    public String getSfId() {
        return sfId;
    }

    public void setSfName(String sfName) {
        this.sfName = sfName;
    }

    public String getSfName() {
        return sfName;
    }

    public void setGeneSymbolList(Vector<String> geneSymbolList) {
        this.geneSymbolList = geneSymbolList;
    }

    public Vector<String> getGeneSymbolList() {
        return geneSymbolList;
    }
    
    public void addGeneSymbol(String geneSymbol) {
        if (null == geneSymbol) {
            return;
        }
        
        if (null == geneSymbolList) {
            geneSymbolList = new Vector<String>();
        }
        geneSymbolList.add(geneSymbol);
    }

    public void setGeneIdentifierList(Vector<String> geneIdentifierList) {
        this.geneIdentifierList = geneIdentifierList;
    }

    public Vector<String> getGeneIdentifierList() {
        return geneIdentifierList;
    }
    
    public void addGeneIdentifier(String geneIdentifier) {
        if (null == geneIdentifier) {
            return;
        }
        
        if (null == geneIdentifierList) {
            geneIdentifierList = new Vector<String>();
        }
        geneIdentifierList.add(geneIdentifier);
    }

    public void setIdentifierTbl(Hashtable<String, Vector<String>> identifierTbl) {
        this.identifierTbl = identifierTbl;
    }

    public Hashtable<String, Vector<String>> getIdentifierTbl() {
        return identifierTbl;
    }
    public void addIdentifier(String key, String value) {
        if (null == key || null == value) {
            return;
        }
        Vector<String> list = null;
        if (null == identifierTbl) {
            identifierTbl = new Hashtable<String, Vector<String>>();
            list = new Vector<String>();
            identifierTbl.put(key, list);
        }
        else {
            list = identifierTbl.get(key);
            if (null == list) {
                list = new Vector<String>();
                identifierTbl.put(key, list);
            }   
        }
        list.add(value);
    }
}
