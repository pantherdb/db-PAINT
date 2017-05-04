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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author muruganu
 */
// Created specifically for GO annotations.  NOT PANTHER subfamily or GO-SLIM information
public class Annotation implements Serializable {
    private String annotationId;
    private String annotationTypeId;
    private String annotationType;      //GO SF? now only GO
    private boolean annotIsToChildTerm = false;        // gene is annotated with a more specific term.  This annotation exists so that users can annotate with a parent term
    private Annotation childAnnotation;     // For IKR and IRD, this will be the related IBA.  No other cases for now
    private Annotation parentAnnotation;    // for IBA, this is the IKR or IRD
    private boolean annotStoredInDb = false;        // Annotations that are propagated are not stored in database.  
    private AnnotationDetail annotationDetail = new AnnotationDetail();
    //Annotation propagatorAnnot = null;           // For IBA, IKR, IRD we have an annotation that was propagated.
    
    // Specific to GO annotation type
    private String goTerm;              // GO:123
    private HashSet<Qualifier> qualifierSet;        // Use annotationDetail to get and set qualifiers
    
    private Evidence evidence;

    public String getAnnotationId() {
        return annotationId;
    }

    public void setAnnotationId(String annotationId) {
        this.annotationId = annotationId;
    }

    public String getAnnotationTypeId() {
        return annotationTypeId;
    }

    public void setAnnotationTypeId(String annotationTypeId) {
        this.annotationTypeId = annotationTypeId;
    }

    public String getAnnotationType() {
        return annotationType;
    }

    public void setAnnotationType(String annotationType) {
        this.annotationType = annotationType;
    }

    public String getGoTerm() {
        return goTerm;
    }

    public void setGoTerm(String goTerm) {
        this.goTerm = goTerm;
    }

    /**
     * Use ANNOTATION DETAIL TO GET AND SET QUALIFIER
     * @return 
     */
    public HashSet<Qualifier> getQualifierSet() {
        if (null == qualifierSet) {
            return null;
        }
        return (HashSet<Qualifier>)qualifierSet.clone();
    }
    
    public HashSet<Qualifier> getApplicableQualifierSet(GOTermHelper gth) {
        if (null == qualifierSet) {
            return null;
        }
        GOTerm term = gth.getTerm(goTerm);
        
        HashSet<Qualifier> applicableSet = new HashSet<Qualifier>();
        if (applicableSet.isEmpty()) {
            return null;
        }
        
        for (Qualifier q: qualifierSet) {
            if (true == gth.isQualifierValidForTerm(term, q)) {
                applicableSet.add(q);
            }
        }
        return applicableSet;
    }

    public void setQualifierSet(HashSet<Qualifier> qualifierSet) {
        this.qualifierSet = qualifierSet;
    }
    
    public void addQualifier(Qualifier q) {
        if (null == q) {
            return;
        }
        if (null == this.qualifierSet) {
            this.qualifierSet = new HashSet<Qualifier>();
            this.qualifierSet.add(q);
            return;
        }
        for (Iterator<Qualifier> i = qualifierSet.iterator(); i.hasNext();) {
            Qualifier current = i.next();
            if (current.equals(q)) {
                return;
            }
        }
        qualifierSet.add(q);
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    public Annotation makeCopy() {
        Annotation newAnnot = new Annotation();
        //newAnnot.annotationId = this.annotationId;
        newAnnot.annotationType = this.annotationType;
        newAnnot.annotationTypeId = this.annotationTypeId;
        newAnnot.goTerm = this.goTerm;
        newAnnot.annotIsToChildTerm = this.annotIsToChildTerm;
        
        if (null != this.qualifierSet) {
            HashSet<Qualifier> newQualifierSet = new HashSet<Qualifier>(this.qualifierSet.size());
            newAnnot.qualifierSet = newQualifierSet;
            for (Qualifier q: this.qualifierSet) {
                newQualifierSet.add(q.makeCopy());
            }
        }
        
        if (null != evidence) {
            newAnnot.setEvidence(this.evidence.makeCopy());
        }
        return newAnnot;
    }

    public boolean isAnnotIsToChildTerm() {
        return annotIsToChildTerm;
    }

    public void setAnnotIsToChildTerm(boolean annotIsToChildTerm) {
        this.annotIsToChildTerm = annotIsToChildTerm;
    }

    public Annotation getChildAnnotation() {
        return childAnnotation;
    }

    public void setChildAnnotation(Annotation childAnnotation) {
        this.childAnnotation = childAnnotation;
    }

    public Annotation getParentAnnotation() {
        return parentAnnotation;
    }

    public void setParentAnnotation(Annotation parentAnnotation) {
        this.parentAnnotation = parentAnnotation;
    }

    public boolean isAnnotStoredInDb() {
        return annotStoredInDb;
    }

    public void setAnnotStoredInDb(boolean annotStoredInDb) {
        this.annotStoredInDb = annotStoredInDb;
    }

    public AnnotationDetail getAnnotationDetail() {
        return annotationDetail;
    }

    public void setAnnotationDetail(AnnotationDetail annotationDetail) {
        this.annotationDetail = annotationDetail;
    }

//    public Annotation getPropagatorAnnot() {
//        return propagatorAnnot;
//    }
//
//    public void setPropagatorAnnot(Annotation propagatorAnnot) {
//        this.propagatorAnnot = propagatorAnnot;
//    }
    
    public Annotation removeWith(Annotation a) {
        Annotation removeAnnot = annotationDetail.removeWithAnnotation(a);
        if (null == removeAnnot) {
            return null;
        }
        Set<Qualifier> qSet = annotationDetail.getQualifiers();
        if (null == qSet || true == qSet.isEmpty()) {
            qualifierSet = null;
            return removeAnnot;
        }
        qualifierSet = new HashSet<Qualifier>();
        for (Qualifier q: qSet) {
            qualifierSet.add(q);
        }
        return removeAnnot;
    }
    
    public boolean withExists(Annotation a) {
        HashSet<Annotation> withAnnots = annotationDetail.getWithAnnotSet();
        if (null == withAnnots) {
            return false;
        }
        return withAnnots.contains(a);
    }

    
}
