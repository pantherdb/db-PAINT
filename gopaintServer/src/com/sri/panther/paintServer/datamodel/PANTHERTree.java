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
