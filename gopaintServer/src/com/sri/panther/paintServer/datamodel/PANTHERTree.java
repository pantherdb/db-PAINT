package com.sri.panther.paintServer.datamodel;

import java.util.Hashtable;

public class PANTHERTree {
    protected PANTHERTreeNode root;
    protected Hashtable<String, PANTHERTreeNode> nodesTbl;
    
    public PANTHERTree(PANTHERTreeNode root, Hashtable<String, PANTHERTreeNode>nodesTbl) {
        this.root = root;
        this.nodesTbl = nodesTbl;
    }


    public PANTHERTreeNode getRoot() {
        return root;
    }


    public Hashtable<String, PANTHERTreeNode> getNodesTbl() {
        return nodesTbl;
    }
}
