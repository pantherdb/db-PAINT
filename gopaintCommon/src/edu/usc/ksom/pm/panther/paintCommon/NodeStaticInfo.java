/**
 * Copyright 2020 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;
import java.util.ArrayList;


public class NodeStaticInfo implements Serializable {

    private String nodeId;              // id in database or server.  DO NOT USE THIS
    private String nodeAcc;             // PTHR10000:AN0
    private String publicId;            // PTN123
    private String longGeneName;
    private String definition;
    private String orthoMCL;
    private String shortOrg;            // HUMAN
    private String speciesConversion;   // Homo sapiens
    private String species;            // Organism from tree topology definition
    private ArrayList<String> geneName;
    private ArrayList<String> geneSymbol;
    private Node parent;
    private ArrayList<Node> children;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeAcc() {
        return nodeAcc;
    }

    public void setNodeAcc(String nodeAcc) {
        this.nodeAcc = nodeAcc;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getLongGeneName() {
        return longGeneName;
    }

    public void setLongGeneName(String longGeneName) {
        this.longGeneName = longGeneName;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getOrthoMCL() {
        return orthoMCL;
    }

    public void setOrthoMCL(String orthoMCL) {
        this.orthoMCL = orthoMCL;
    }


    public String getShortOrg() {
        return shortOrg;
    }

    public void setShortOrg(String shortOrg) {
        this.shortOrg = shortOrg;
    }

    public ArrayList<String> getGeneName() {
        return geneName;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public void setGeneName(ArrayList<String> geneName) {
        this.geneName = geneName;
    }

    public void addGeneName(String geneName) {
        if (null == this.geneName) {
            this.geneName = new ArrayList<String>(1);
        }
        this.geneName.add(geneName);
    }

    public ArrayList<String> getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(ArrayList<String> geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public void addGeneSymbol(String geneSymbol) {
        if (null == this.geneSymbol) {
            this.geneSymbol = new ArrayList<String>(1);
        }
        this.geneSymbol.add(geneSymbol);
    }

    public String getSpeciesConversion() {
        return speciesConversion;
    }

    public void setSpeciesConversion(String speciesConversion) {
        this.speciesConversion = speciesConversion;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }
    
    public void addChild(Node child) {
        if (null == children) {
            children = new ArrayList<Node>();
        }
        if (false == children.contains(child)) {
            children.add(child);
        }
    }
    
    public boolean isLeaf() {
        if (null != children && 0 < children.size()) {
            return false;
        }
        return true;
    }
    
    
    public String getCalculatedSpecies() {
        if (null != species) {
            return species;
        }       
        if (null != parent) {
            NodeStaticInfo pNsi = parent.getStaticInfo();
            if (null != pNsi) {
                return pNsi.getCalculatedSpecies();
            }
        }
        return null;
    }



}
