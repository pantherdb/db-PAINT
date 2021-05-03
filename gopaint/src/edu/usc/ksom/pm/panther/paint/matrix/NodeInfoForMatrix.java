/**
 *  Copyright 2019 University Of Southern California
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
package edu.usc.ksom.pm.panther.paint.matrix;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.util.ArrayList;
import java.util.HashSet;
import org.paint.datamodel.GeneNode;
import org.paint.util.GeneNodeUtil;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;
import java.util.HashMap;


public class NodeInfoForMatrix {
    private boolean nonExpBackground = false;
    private boolean expBackground = false;
    private boolean nonExpAnnotToTerm = false;
    private boolean expAnnotToTerm =  false;
    private boolean nonExpNot = false;
    private boolean expNot = false;
    private GeneNode gNode;
    private GOTerm gTerm;
    private HashSet<Qualifier> qSet;        // Experimental qualifiers
    private HashSet<Qualifier> nonQset;     // Non-experimental qualifiers
    private HashMap<String, HashSet<String>> allQualifierToListOfTerms = new HashMap<String, HashSet<String>>();      // Lookup of qualifiers to list of terms for the qualifier.
                                                                                                                // contains all qualifiers including no qualifier.
    
    private static String BLANK_QUALIFIER = "-";;
    private static String BLANK_LABEL = "-";
    private static final String STR_BRACKET_START = "(";
    private static final String STR_BRACKET_END = ")";    
    
    public NodeInfoForMatrix(GeneNode gNode, GOTerm gTerm, GOTermHelper gth) {
        this.gNode = gNode;
        this.gTerm = gTerm;
        if (true == GeneNodeUtil.inPrunedBranch(gNode)) {
            return;
        }
        Node n = gNode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return;
        }

        // Handle the NOTS first
        HashSet<Annotation> handledSet = new HashSet<Annotation>();
        ArrayList<GOTerm> notAncestors = gth.getAncestors(gTerm);
        for (Annotation a : annotList) {
            HashSet<Qualifier> qualifiers = a.getQualifierSet();
            boolean not = QualifierDif.containsNegative(qualifiers);
            if (false == not) {
                continue;
            }
            handledSet.add(a);
            String curTerm = a.getGoTerm();
            GOTerm cGOTerm = gth.getTerm(curTerm);
            String label = cGOTerm.getName();
            if (null == label) {
                label = BLANK_LABEL;
            }
            if (notAncestors.contains(cGOTerm) || cGOTerm.equals(gTerm)) {
                boolean experimental = a.isExperimental();
                if (true == experimental) {
                    expBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        expAnnotToTerm = true;
                    }

                    expNot = true;
                    if (null == qualifiers) {
                        HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                        if (null == termSet) {
                            termSet = new HashSet<String>();
                            allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                        }
                        termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                    } else {
                        for (Qualifier q : qualifiers) {
                            String text = q.getText();
                            if (null == text || 0 == text.trim().length()) {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                                }
                                termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            } else {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(text);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(text, termSet);
                                }
                                termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            }
                        }
                        if (null == qSet) {
                            qSet = new HashSet();
                        }
                        QualifierDif.addIfNotPresent(qSet, qualifiers);
                    }
                } else {
                    nonExpBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        nonExpAnnotToTerm = true;
                    }

                    nonExpNot = true;
                    if (null == qualifiers) {
                        HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                        if (null == termSet) {
                            termSet = new HashSet<String>();
                            allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                        }
                        termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                    } else {
                        for (Qualifier q : qualifiers) {
                            String text = q.getText();
                            if (null == text || 0 == text.trim().length()) {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                                }
                                termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            } else {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(text);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(text, termSet);
                                }
                                termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            }
                        }
                        if (null == nonQset) {
                            nonQset = new HashSet();
                        }
                        QualifierDif.addIfNotPresent(nonQset, a.getQualifierSet());
                    }
                }
            }
        }

        // Then the others
        for (Annotation a : annotList) {
            if (handledSet.contains(a)) {
                continue;
            }
            boolean experimental = a.isExperimental();
            String curTerm = a.getGoTerm();
            GOTerm cGOTerm = gth.getTerm(curTerm);
            String label = cGOTerm.getName();
            if (null == label) {
                label = BLANK_LABEL;
            }
            ArrayList<GOTerm> ancestors = gth.getAncestors(cGOTerm);
            if (ancestors.contains(gTerm) || cGOTerm.equals(gTerm)) {
                if (true == experimental) {
                    expBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        expAnnotToTerm = true;
                    }
                    HashSet<Qualifier> curSet = a.getQualifierSet();
                    if (null == curSet) {
                        HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                        if (null == termSet) {
                            termSet = new HashSet<String>();
                            allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                        }
                        termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                    } else {
                        for (Qualifier q : curSet) {
                            String text = q.getText();
                            if (null == text || 0 == text.trim().length()) {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                                }
                               termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            } else {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(text);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(text, termSet);
                                }
                                termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            }
                        }
                        if (null == qSet) {
                            qSet = new HashSet();
                        }
                        for (Qualifier q : curSet) {
                            if (true == gth.isQualifierValidForTerm(gTerm, q)) {
                                QualifierDif.addIfNotPresent(qSet, q);
                            }
                        }
                    }
                } else {
                    nonExpBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        nonExpAnnotToTerm = true;
                    }
                    HashSet<Qualifier> curSet = a.getQualifierSet();
                    if (null == curSet) {
                        HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                        if (null == termSet) {
                            termSet = new HashSet<String>();
                            allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                        }
                        termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                    } else {
                        for (Qualifier q : curSet) {
                            String text = q.getText();
                            if (null == text || 0 == text.trim().length()) {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(BLANK_QUALIFIER);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(BLANK_QUALIFIER, termSet);
                                }
                                termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            } else {
                                HashSet<String> termSet = allQualifierToListOfTerms.get(text);
                                if (null == termSet) {
                                    termSet = new HashSet<String>();
                                    allQualifierToListOfTerms.put(text, termSet);
                                }
                                termSet.add(label + STR_BRACKET_START + curTerm + STR_BRACKET_END);
                            }
                        }
                        if (null == nonQset) {
                            nonQset = new HashSet();
                        }

                        for (Qualifier q : curSet) {
                            if (true == gth.isQualifierValidForTerm(gTerm, q)) {
                                QualifierDif.addIfNotPresent(nonQset, q);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isNonExpBackground() {
        return nonExpBackground;
    }

    public boolean isExpBackground() {
        return expBackground;
    }

    public boolean isNonExpAnnotToTerm() {
        return nonExpAnnotToTerm;
    }

    public boolean isExpAnnotToTerm() {
        return expAnnotToTerm;
    }

    public boolean isNonExpNot() {
        return nonExpNot;
    }

    public boolean isExpNot() {
        return expNot;
    }

    public GeneNode getgNode() {
        return gNode;
    }

    public GOTerm getgTerm() {
        return gTerm;
    }

    public HashSet<Qualifier> getqSet() {
        return qSet;
    }

    public HashSet<Qualifier> getNonQset() {
        return nonQset;
    }

    public HashMap<String, HashSet<String>> getAllQualifierToListOfTerms() {
        return allQualifierToListOfTerms;
    }

    public boolean containsMultipleQualifiers() {
        if (null != allQualifierToListOfTerms && 1 < allQualifierToListOfTerms.size()) {
            return true;
        }
        return false;
    }
    
}
