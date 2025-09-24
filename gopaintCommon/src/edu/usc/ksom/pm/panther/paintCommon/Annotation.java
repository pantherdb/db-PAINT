/**
 *  Copyright 2025 University Of Southern California
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

import com.sri.panther.paintCommon.User;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;


// Created specifically for GO annotations.  NOT PANTHER subfamily or GO-SLIM information
public class Annotation implements Serializable, IWith {
    private String annotationId;
    private String annotationTypeId;
    private String date;
    private String annotationType;      //GO SF? now only GO
    
    private boolean annotStoredInDb = false;        // Annotations that are propagated are not stored in database.  
    private AnnotationDetail annotationDetail = new AnnotationDetail();
    //Annotation propagatorAnnot = null;           // For IBA, IKR, IRD we have an annotation that was propagated.
    
    // Specific to GO annotation type
    private String goTerm;              // GO:123
    private HashSet<Qualifier> qualifierSet;        // Use annotationDetail to get and set qualifiers
    private boolean expAnnotCreatedInPaint = false;

    private boolean experimental = false;

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
    
    public void addWithEvidence(WithEvidence we) {
        annotationDetail.addWithEvidence(we);
    }
    
    public String getSingleEvidenceCodeFromSet() {
        HashSet<String> codeSet = getEvidenceCodeSet();
        if (null == codeSet || 1 != codeSet.size()) {
            return null;
        }
        for (String code: codeSet) {
            return code;
        }
        return null;
    }
   
    public static Annotation getSingleWithPropagatorAnnot(Annotation a) {
        HashSet<WithEvidence> withAnnotSet = a.getAnnotationDetail().getWithEvidenceAnnotSet();
        if (null == withAnnotSet || 1 !=  withAnnotSet.size()) {
            return null;
        }
        for (WithEvidence we: withAnnotSet) {
            return (Annotation)we.getWith();
        }
        return null;
    }    
    
    
    public HashSet<String> getEvidenceCodeSet() {
        AnnotationDetail ad = this.annotationDetail;
        if (null == ad) {
            return null;
        }
        HashSet<String> codeSet = new HashSet<String>();
        if (null != ad.getWithEvidenceAnnotSet()) {
            for (WithEvidence cur: ad.getWithEvidenceAnnotSet()) {
//                IWith with = cur.getWith();
//                if (with == this) {
//                    continue;
//                }
                if (null != cur.getEvidenceCode()) {
                    codeSet.add(cur.getEvidenceCode());
                }
            }
        }
        
        if (null != ad.getWithEvidenceNodeSet()) {
            for (WithEvidence cur: ad.getWithEvidenceNodeSet()) {
                if (null != cur.getEvidenceCode()) {
                    codeSet.add(cur.getEvidenceCode());
                }
            }
        }
        
        if (null !=  ad.getWithEvidenceDBRefSet()) {
            for (WithEvidence cur: ad.getWithEvidenceDBRefSet()) {
                if (null != cur.getEvidenceCode()) {
                    codeSet.add(cur.getEvidenceCode());
                }
            }
        }
        return codeSet;
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
        for (Qualifier q: qualifierSet) {
            if (true == gth.isQualifierValidForTerm(term, q)) {
                applicableSet.add(q);
            }
        }
        if (applicableSet.isEmpty()) {
            return null;
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

//    public Evidence getEvidence() {
//        return evidence;
//    }
//
//    public void setEvidence(Evidence evidence) {
//        this.evidence = evidence;
//    }

    public Annotation makeCopy() {
        Annotation newAnnot = new Annotation();
        //newAnnot.annotationId = this.annotationId;
        newAnnot.annotationType = this.annotationType;
        newAnnot.annotationTypeId = this.annotationTypeId;
        newAnnot.goTerm = this.goTerm;
//        newAnnot.annotIsToChildTerm = this.annotIsToChildTerm;
        
        if (null != this.qualifierSet) {
            HashSet<Qualifier> newQualifierSet = new HashSet<Qualifier>(this.qualifierSet.size());
            newAnnot.qualifierSet = newQualifierSet;
            for (Qualifier q: this.qualifierSet) {
                newQualifierSet.add(q.makeCopy());
            }
        }
        newAnnot.expAnnotCreatedInPaint = this.expAnnotCreatedInPaint;
        
//        if (null != evidence) {
//            newAnnot.setEvidence(this.evidence.makeCopy());
//        }
        return newAnnot;
    }

//    public boolean isAnnotIsToChildTerm() {
//        return annotIsToChildTerm;
//    }
//
//    public void setAnnotIsToChildTerm(boolean annotIsToChildTerm) {
//        this.annotIsToChildTerm = annotIsToChildTerm;
//    }

//    public Annotation getChildAnnotation() {
//        return childAnnotation;
//    }
//
//    public void setChildAnnotation(Annotation childAnnotation) {
//        this.childAnnotation = childAnnotation;
//    }
//
//    public Annotation getParentAnnotation() {
//        return parentAnnotation;
//    }
//
//    public void setParentAnnotation(Annotation parentAnnotation) {
//        this.parentAnnotation = parentAnnotation;
//    }

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
    
    public boolean isCreatedByPaint() {
        HashSet<WithEvidence> withEvSet = this.annotationDetail.getWithEvidenceSet();
        if (null == withEvSet) {
            return false;
        }
        for (WithEvidence we: withEvSet) {
            if (true == we.isPAINTType()) {
                return true;
            }
        }
        return false;
    }
    
    public void removeFromWithEvidence(IWith iw) {
        if (null == iw) {
            return;
        }
        HashSet<WithEvidence> withEvAnnotSet = annotationDetail.getWithEvidenceAnnotSet();
        if (null != withEvAnnotSet) {
            for (Iterator<WithEvidence> iter = withEvAnnotSet.iterator(); iter.hasNext();) {
                WithEvidence we = iter.next();
                if (we.getWith() == iw) {
                    iter.remove();
                    removeWith((Annotation)iw);
                    return;
                }   
            }
        }
        
        HashSet<WithEvidence> withEvNodeSet = annotationDetail.getWithEvidenceNodeSet();
        if (null != withEvNodeSet) {
            for (Iterator<WithEvidence> iter = withEvNodeSet.iterator(); iter.hasNext();) {
                WithEvidence we = iter.next();
                if (we.getWith() == iw) {
                    iter.remove();
                    HashSet<Node> nodeSet = annotationDetail.getWithNodeSet();
                    if (null != nodeSet) {
                        nodeSet.remove((Node)iw);
                    }
                    return;
                }   
            }
        }
        HashSet<WithEvidence> withEvDBRefSet = annotationDetail.getWithEvidenceDBRefSet();
        if (null != withEvDBRefSet) {
            for (Iterator<WithEvidence> iter = withEvDBRefSet.iterator(); iter.hasNext();) {
                WithEvidence we = iter.next();
                if (we.getWith() == iw) {
                    iter.remove();
                    HashSet<DBReference> dbRefSet = annotationDetail.getWithOtherSet();
                    if (null != dbRefSet) {
                        dbRefSet.remove((DBReference)iw);
                    }
                    return;
                }   
            }
        }        
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
    
    public static boolean hasExperimentalWith(Annotation a) {
        HashSet<WithEvidence> withEvSet = a.getAnnotationDetail().getWithEvidenceSet();
        if (null == withEvSet) {
            return false;
        }
        for (WithEvidence we: withEvSet) {
            IWith with = we.getWith();
            if (with instanceof Annotation) {
                Annotation aWith = (Annotation)with;
                if (true == aWith.isExperimental()) {
                    return true;
                }
            }
        }
        return false;
    }    
    
    public boolean withExists(Annotation a) {
        HashSet<WithEvidence> withAnnotEvSet = annotationDetail.getWithEvidenceAnnotSet();
        if (null == withAnnotEvSet) {
            return false;
        }
        for (WithEvidence we: withAnnotEvSet) {
            Annotation with = (Annotation)we.getWith();
            if (with == a) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isRequired(Annotation a) {
        HashSet<WithEvidence> withAnnotEvSet = annotationDetail.getWithEvidenceAnnotSet();
        if (null == withAnnotEvSet) {
            return false;
        }
        
        for (WithEvidence we: withAnnotEvSet) {
            Annotation with = (Annotation)we.getWith();
            if (with == a) {
                if (1 == withAnnotEvSet.size()) {
                    return true;
                }
                String code = this.getSingleEvidenceCodeFromSet();
                if (Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code) || Evidence.CODE_TCV.equals(code)) {
                    return true;
                }
            }
        }
        
        // Check qualifiers
        LinkedHashMap<Qualifier, HashSet<Annotation>> qualifierLookup = annotationDetail.getInheritedQualifierLookup();
        if (null != qualifierLookup) {
            Collection <HashSet<Annotation>> values = qualifierLookup.values();
            for (HashSet<Annotation> val: values) {
                if (val.size() == 1 && val.contains(a)) {
                    return true;
                }
            }
        }
        
        qualifierLookup = annotationDetail.getAddedQualifierLookup();
        if (null != qualifierLookup) {
            Collection <HashSet<Annotation>> values = qualifierLookup.values();
            for (HashSet<Annotation> val: values) {
                if (val.size() == 1 && val.contains(a)) {
                    return true;
                }
            }
        }
        
        qualifierLookup = annotationDetail.getRemovedQualifierLookup();
        if (null != qualifierLookup) {
            Collection <HashSet<Annotation>> values = qualifierLookup.values();
            for (HashSet<Annotation> val: values) {
                if (val.size() == 1 && val.contains(a)) {
                    return true;
                }
            }
        }
        return false;
    }
    
//    public boolean isExperimental() {
//        HashSet<String> codes = annotationDetail.getEvidenceCodes();
//        if (null == codes) {
//            return false;
//        }
//        for (String code: codes) {
//            if (true == Evidence.isExperimental(code)) {
//                return true;
//            }
//        }
//        return false;
//    }
    
    public boolean isPaint() {
        HashSet<String> codes = annotationDetail.getEvidenceCodes();
        if (null == codes) {
            return false;
        }
        for (String code: codes) {
            if (true == Evidence.isPaint(code)) {
                return true;
            }
        }
        return false;        
    }

    public boolean isExperimental() {
        return experimental;
    }

    public void setExperimental(boolean experimental) {
        this.experimental = experimental;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isExpAnnotCreatedInPaint() {
        return expAnnotCreatedInPaint;
    }

    public void setExpAnnotCreatedInPaint(boolean expAnnotCreatedInPaint) {
        this.expAnnotCreatedInPaint = expAnnotCreatedInPaint;
    }
}
