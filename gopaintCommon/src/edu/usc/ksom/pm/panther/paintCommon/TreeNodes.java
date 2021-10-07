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
package edu.usc.ksom.pm.panther.paintCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class TreeNodes {
    protected Node root;
    protected HashMap<String, Node> nodesTbl;
    
    public TreeNodes(Node root, HashMap<String, Node>nodesTbl) {
        this.root = root;
        this.nodesTbl = nodesTbl;
    }    
    
    public Node getRoot() {
        return root;
    }


    public HashMap<String, Node> getNodesTbl() {
        return nodesTbl;
    }
    
    public boolean isLeaf(String acc) {
        if (null == acc || null == nodesTbl) {
            return false;
        }
        Node node = nodesTbl.get(acc);
        if (null == node) {
            return false;
        }
        NodeStaticInfo nsi = node.getStaticInfo();
        if (null == nsi) {
            return false;
        }
        ArrayList<Node> children = nsi.getChildren();
        if (null == children || true == children.isEmpty()) {
            return true;
        }
        return false;
    }    
}
