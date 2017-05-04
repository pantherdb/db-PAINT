/**
 *  Copyright 2016 University Of Southern California
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

//////////////////////////////// NO LONGER USED - CAN BE REMOVED
package edu.usc.ksom.pm.panther.paint.matrix;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;

/**
 *
 * @author muruganu
 */
public class NodeInfoForGroup {
    private Annotation  annotation;
    private String termAcc;
    private String termName;
    private boolean annotatedToTerm;
    
    public NodeInfoForGroup(Annotation annotation, String termAcc, String termName, boolean annotatedToTerm) {
        this.annotation = annotation;
        this.termAcc = termAcc;
        this.termName = termName;
        this.annotatedToTerm = annotatedToTerm;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public String getTermAcc() {
        return termAcc;
    }

    public String getTermName() {
        return termName;
    }

    public boolean isAnnotatedToTerm() {
        return annotatedToTerm;
    }


    
    
}
