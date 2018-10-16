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
package edu.usc.ksom.pm.panther.paintCommon;

import java.util.HashMap;
import java.util.HashSet;

// Groups annotations with experimental evidence by qualifier
public class AnnotQualifierGroup {
    
    private HashMap<HashSet<Qualifier>, HashSet<Annotation>> qualifierAnnotLookup = new HashMap<HashSet<Qualifier>, HashSet<Annotation>>();
    
    public AnnotQualifierGroup(HashSet<Annotation> annotSet) {
        if (null == annotSet) {
            return;
        }
        for (Annotation a: annotSet) {
            if (false == a.isExperimental()) {
                continue;
            }
            HashSet<Qualifier> qSet = a.getQualifierSet();
            if (null == qSet) {
                qSet = new HashSet<Qualifier>(0);
            }
            boolean found = false;
            for (HashSet<Qualifier> curSet: qualifierAnnotLookup.keySet()) {
                QualifierDif qd = new QualifierDif(qSet, curSet);
                if (QualifierDif.QUALIFIERS_SAME == qd.getDifference()) {
                    qualifierAnnotLookup.get(curSet).add(a);
                    found = true;
                    break;        
                }
            }
            if (false == found) {
                HashSet<Annotation> annotList = new HashSet<Annotation>();
                annotList.add(a);
                qualifierAnnotLookup.put(qSet, annotList);
            }
        }
    }

    public HashMap<HashSet<Qualifier>, HashSet<Annotation>> getQualifierAnnotLookup() {
        return qualifierAnnotLookup;
    }
    
}
