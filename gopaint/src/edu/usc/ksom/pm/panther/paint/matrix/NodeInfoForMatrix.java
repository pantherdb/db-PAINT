/**
 *  Copyright 2018 University Of Southern California
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
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;;

/**
 *
 * @author muruganu
 */
public class NodeInfoForMatrix {
    private boolean nonExpBackground = false;
    private boolean expBackground = false;
    private boolean nonExpAnnotToTerm = false;
    private boolean expAnnotToTerm =  false;
    private boolean nonExpNot = false;
    private boolean expNot = false;
    private GeneNode gNode;
    private GOTerm gTerm;
    private HashSet<Qualifier> qSet;
    private HashSet<Qualifier> nonQset;
    
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
        for (Annotation a: annotList) {
            boolean not = QualifierDif.containsNegative(a.getQualifierSet());
            if (false == not) {
                continue;
            }
            handledSet.add(a);
            String curTerm = a.getGoTerm();
            GOTerm cGOTerm = gth.getTerm(curTerm);            
            if (notAncestors.contains(cGOTerm) || cGOTerm.equals(gTerm)) {
                boolean experimental = a.isExperimental();
                if (true == experimental) {
                    expBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        expAnnotToTerm = true;
                    }

                    expNot = true;

                    if (null != a.getQualifierSet()) {
                        if (null == qSet) {
                            qSet = new HashSet();
                        }
                        QualifierDif.addIfNotPresent(qSet, a.getQualifierSet());
                    }
                }
                else {
                    nonExpBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        nonExpAnnotToTerm = true;
                    }

                    nonExpNot = true;

                    if (null != a.getQualifierSet()) {
                        if (null == nonQset) {
                            nonQset = new HashSet();
                        }
                        QualifierDif.addIfNotPresent(nonQset, a.getQualifierSet());
                    }                    
                }                
            }       
        }

        // Then the others
        for (Annotation a: annotList) {
            if (handledSet.contains(a)) {
                continue;
            }
            boolean experimental = a.isExperimental();
            String curTerm = a.getGoTerm();
            GOTerm cGOTerm = gth.getTerm(curTerm);
            ArrayList<GOTerm> ancestors = gth.getAncestors(cGOTerm);
            if (ancestors.contains(gTerm) || cGOTerm.equals(gTerm)) {
                if (true == experimental) {
                    expBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        expAnnotToTerm = true;
                    }
                    HashSet<Qualifier> curSet = a.getQualifierSet();
                    if (null != curSet) {
                        if (null == qSet) {
                            qSet = new HashSet();
                        }
                        for (Qualifier q: curSet) {
                            if (true == gth.isQualifierValidForTerm(gTerm, q)) {
                                QualifierDif.addIfNotPresent(qSet, q);  
                            }
                        }
                    }
                }
                else {
                    nonExpBackground = true;
                    if (cGOTerm.equals(gTerm)) {
                        nonExpAnnotToTerm = true;
                    }
                    HashSet<Qualifier> curSet = a.getQualifierSet();
                    if (curSet != a.getQualifierSet()) {
                        if (null == nonQset) {
                            nonQset = new HashSet();
                        }
                        
                        for (Qualifier q: curSet) {
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
    
}
