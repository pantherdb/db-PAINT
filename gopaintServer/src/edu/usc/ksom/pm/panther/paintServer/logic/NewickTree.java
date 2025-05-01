/**
 * Copyright 2019 University Of Southern California
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

import java.util.ArrayList;

/**
 * Created on 12/18/16
 *
 * @author @author <a href="mailto:knapp@american.edu">Adam Knapp</a>
 * @version 0.1
 * Modified for PANTHER NHX format tree
 */
public class NewickTree {

    private static int node_uuid = 0;
    ArrayList<Node> nodeList = new ArrayList<>();
    public Node root;

    static NewickTree readNewickFormat(String newick) {
        return new NewickTree().innerReadNewickFormat(newick);
    }

    private static String[] split(String s) {

        ArrayList<Integer> splitIndices = new ArrayList<>();

        int rightParenCount = 0;
        int leftParenCount = 0;
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case '(':
                    leftParenCount++;
                    break;
                case ')':
                    rightParenCount++;
                    break;
                case ',':
                    if (leftParenCount == rightParenCount) splitIndices.add(i);
                    break;
            }
        }

        int numSplits = splitIndices.size() + 1;
        String[] splits = new String[numSplits];

        if (numSplits == 1) {
            splits[0] = s;
        } else {

            splits[0] = s.substring(0, splitIndices.get(0));

            for (int i = 1; i < splitIndices.size(); i++) {
                splits[i] = s.substring(splitIndices.get(i - 1) + 1, splitIndices.get(i));
            }

            splits[numSplits - 1] = s.substring(splitIndices.get(splitIndices.size() - 1) + 1);
        }

        return splits;
    }

    private NewickTree innerReadNewickFormat(String newick) {

        // single branch = subtree (?)
        this.root = readSubtree(newick.substring(0, newick.length() - 1), 0);

        return this;
    }

    private Node readSubtree(String s, int level) {

        int leftParen = s.indexOf('(');
        int rightParen = s.lastIndexOf(')');

        if (leftParen != -1 && rightParen != -1) {

            String name = s.substring(rightParen + 1);
            String[] childrenString = split(s.substring(leftParen + 1, rightParen));

            Node node = new Node(name);
            node.level = level;
            node.children = new ArrayList<>();
            for (String sub : childrenString) {
                Node child = readSubtree(sub, level + 1);
                child.level = node.level + 1;
                node.children.add(child);
                child.parent = node;
            }

            nodeList.add(node);
            return node;
        } else if (leftParen == rightParen) {

            Node node = new Node(s);
            nodeList.add(node);
            return node;

        } else throw new RuntimeException("unbalanced ()'s");
    }
    
    public Node getByName(String name) {
        if (null == name || null == nodeList) {
            return null;
        }
        for (Node n: nodeList) {
            if (name.equals(n.name)) {
                return n;
            }
        }
        return null;
    }
    
    public int getLevel(String name) {
        Node n = getByName(name);
        if (null == n) {
            return -1;
        }
        return n.level;
    }

    static class Node {
        final String name;
        final int weight = 0;
        int level = 0;      // To keep track of ancestoral level
        boolean realName = false;
        ArrayList<Node> children;
        Node parent;
        String namePrefix = "[&&NHX:S=";

        /**
         * @param name name in "actualName:weight" format, weight defaults to zero if colon absent
         */
        Node(String name) {

            int colonIndex = name.indexOf(':');
            String actualNameText = "";
            if (colonIndex == -1) {
//                System.out.println("actual name text is " + name);                
                actualNameText = name;
                //weight = 0;
            } else {
//                System.out.println("Name is " + name);
                if (name.startsWith(namePrefix)) {
                    if (name.endsWith("]")) {
                    actualNameText = name.substring(namePrefix.length(), name.length() - 1);
                    }
                    else {
                        actualNameText = name.substring(namePrefix.length(), name.length());
                    }
//                                    System.out.println("setting actual name text to  " + actualNameText);
                //weight = Integer.parseInt(name.substring(colonIndex + 1, name.length()));
                }
            }

            if (actualNameText.equals("")) {
                this.realName = false;
                this.name = Integer.toString(node_uuid);
                node_uuid++;
            } else {
                this.realName = true;
                this.name = actualNameText;
            }
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Node)) return false;
            Node other = (Node) o;
            return this.name.equals(other.name);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (children != null && children.size() > 0) {
                sb.append("(");
                for (int i = 0; i < children.size() - 1; i++) {
                    sb.append(children.get(i).toString());
                    sb.append(",");
                }
                sb.append(children.get(children.size() - 1).toString());
                sb.append(")");
            }
            if (name != null) sb.append(this.getName());
            return sb.toString();
        }

        String getName() {
            if (realName)
                return name;
            else
                return "";
        }
    }

    @Override
    public String toString() {
        return root.toString() + ";";
    }

}
