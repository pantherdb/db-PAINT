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
package edu.usc.ksom.pm.panther.paint.annotation;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;
import java.util.ArrayList;
import java.util.HashSet;
import org.paint.datamodel.GeneNode;
import org.paint.util.GeneNodeUtil;

/**
 *
 * @author muruganu
 */
public class AnnotationForTerm {
    private GeneNode gNode;
    private GOTerm gTerm;
    private HashSet<Qualifier> qSet = new HashSet<Qualifier>();    
    private HashSet<Annotation> annotSet = new HashSet<Annotation>();
    
    public AnnotationForTerm(GeneNode gNode, GOTerm gTerm, GOTermHelper gth) {
        this.gNode = gNode;
        this.gTerm = gTerm;
        
        if (true == GeneNodeUtil.inPrunedBranch(gNode)) {
            return;
        }
        Node n = gNode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            qSet = null;
            return;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            qSet = null;
            return;
        }
        
        // Handle the NOTS first
        HashSet<Annotation> handledSet = new HashSet<Annotation>();
        ArrayList<GOTerm> notAncestors = gth.getAncestors(gTerm);
        for (Annotation a: annotList) {
            if (false == a.isExperimental()) {
                continue;
            }
            boolean not = QualifierDif.containsNegative(a.getQualifierSet());
            if (false == not) {
                continue;
            }
            handledSet.add(a);
            String curTerm = a.getGoTerm();
            GOTerm cGOTerm = gth.getTerm(curTerm);            
            if (notAncestors.contains(cGOTerm) || cGOTerm.equals(gTerm)) {
                annotSet.add(a);
                QualifierDif.addIfNotPresent(qSet, a.getQualifierSet());                       
            }       
        }

        // Then the others
        for (Annotation a: annotList) {
            if (false == a.isExperimental()) {
                continue;
            }
            if (handledSet.contains(a)) {
                continue;
            }
            String curTerm = a.getGoTerm();
            GOTerm cGOTerm = gth.getTerm(curTerm);
            ArrayList<GOTerm> ancestors = gth.getAncestors(cGOTerm);
            if (ancestors.contains(gTerm) || cGOTerm.equals(gTerm)) {
                annotSet.add(a);
                HashSet<Qualifier> curSet = a.getQualifierSet();
                if (null != curSet) {
                    for (Qualifier q: curSet) {
                        if (true == gth.isQualifierValidForTerm(gTerm, q)) {
                            QualifierDif.addIfNotPresent(qSet, q);  
                        }
                    }
                }
            }       
        }
        if (qSet.isEmpty()) {
            qSet = null;
        }
    }    

    public GeneNode getgNode() {
        return gNode;
    }

    public GOTerm getgTerm() {
        return gTerm;
    }



    public HashSet<Qualifier> getQset() {
        return qSet;
    }



    public HashSet<Annotation> getAnnotSet() {
        return annotSet;
    }
    
    public boolean annotationExists() {
        if (annotSet.isEmpty()) {
            return false;
        }
        return true;
    }
    
    public static boolean annotationApplicable(AnnotationForTerm aft, AnnotationForTerm compAft) {
        if (false == aft.annotationExists() || false == compAft.annotationExists()) {
            return false;
        }
        boolean qualifierNeg = QualifierDif.containsNegative(aft.getQset());
        if (qualifierNeg != QualifierDif.containsNegative(compAft.getQset())) {
            return false;
        }
        return true;
    }
}
