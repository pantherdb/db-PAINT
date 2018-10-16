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

import com.sri.panther.paintCommon.util.StringUtils;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;


public class WithEvidence implements Serializable{

    private String evidenceCodeid;          
    private String evidenceCode;   //IBD, IEA, etc
    private String evidenceType;    //PAINT EXP, GO REF
    private String evidenceId;
    
    public static final String EVIDENCE_TYPE_ANNOT_PAINT_REF = "PAINT_REF";
    public static final String EVIDENCE_TYPE_ANNOT_PAINT_EXP = "PAINT_EXP";
    public static final String EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR = "PAINT_ANCESTOR";    
    
    private static final HashSet<String> PAINT_EVIDENCE_TYPE_SET= initPaintEvidenceTypes();
    
    // with for an annotation.  Can be one of the following:  annotation, dbReference or node.
    private IWith with;

    public String getEvidenceCodeid() {
        return evidenceCodeid;
    }

    public void setEvidenceCodeid(String evidenceCodeid) {
        this.evidenceCodeid = evidenceCodeid;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }
    

    public IWith getWith() {
        return with;
    }

    public void setWith(IWith with) {
        this.with = with;
    }
    
    public boolean isExperimental() {
        return Evidence.isExperimental(evidenceCode);
//        Evidence tstEv = new Evidence();
//        tstEv.setEvidenceCode(evidenceCode);
//        return tstEv.isExperimental();
        // return Evidence.isExperimental(evidenceCode);
    }
    
    public boolean equals(WithEvidence we) {
        if (null != this.evidenceCode && null != we.evidenceCode && false == evidenceCode.equals(we.evidenceCode) || (null != this.evidenceCode && null == we.evidenceCode) || (null == this.evidenceCode && null != we.evidenceCode)) {
            return false;
        }
        if ((null == this.with && null != we.with) || (null != this.with && null == we.with)) {
            return false;
        }
        if (null == this.with && null == we.with) {
            return true;
        }
        if (false == this.with.getClass().toString().equals(we.with.getClass().toString())) {
            return false;
        }
        if (with instanceof Node) {
            return equals((Node)with, (Node)we.with);
        }
        if (with instanceof DBReference) {
            return equals((DBReference)with, (DBReference)we.with);
        }
        return equals((Annotation)with, (Annotation)we.with);
    }
    
    public boolean equals(Node n1, Node n2) {
        if (n1 == null && n2 == null) {
            return true;
        }
        if ((n1 == null && n2 != null) || (n1 != null && n2 == null)) {
            return false;
        }

        NodeStaticInfo nsi1 = n1.getStaticInfo();
        NodeStaticInfo nsi2 = n2.getStaticInfo();
        if (null == nsi1 && null == nsi2) {
            return true;
        }
        if ((null == nsi1 && null != nsi2) || (null != nsi1 && null == nsi2)) {
            return false;
        }
        String id = nsi1.getPublicId();
        if (null !=  id && true == id.equals(nsi2.getPublicId())) {
            return true;
        }
        String acc = nsi1.getNodeAcc();
        if (null != acc && true == acc.equals(nsi2.getNodeAcc())) {
            return true;
        }
        return false;
    }
    
    private boolean equals(DBReference db1, DBReference db2) {
        if (false == StringUtils.stringsSame(db1.getEvidenceType(), db2.getEvidenceType())) {
            return false;
        }
        if (false == StringUtils.stringsSame(db1.getEvidenceValue(), db2.getEvidenceValue())) {
            return false;
        }
        return true;
    }    
    
    private boolean equals(Annotation a1, Annotation a2) {
        if (null != a1.getGoTerm() && null != a2) {
            if (false == a1.getGoTerm().equals(a2.getGoTerm())) {
                return false;
            }
        }
        QualifierDif qf = new QualifierDif(a1.getQualifierSet(), a2.getQualifierSet());
        if (qf.getDifference() != QualifierDif.QUALIFIERS_SAME) {
            return false;
        }
        AnnotationDetail ad1 = a1.getAnnotationDetail();
        AnnotationDetail ad2 = a2.getAnnotationDetail();
        
        if (false == equals(ad1.getAnnotatedNode(), ad2.getAnnotatedNode())) {
            return false;
        }
        
        // IRD's and IKR have 'with annotation' that is self.  i.e. Annotation is the 'with' which is responsible for the NOT
        // In order to avoid an infinite loo here, clone and remove self from here.
        HashSet<Annotation> withAnnot1 = ad1.getWithAnnotSet();
        if (null != withAnnot1) {
            withAnnot1 = (HashSet<Annotation>)withAnnot1.clone();
            withAnnot1.remove(a1);
        }
        
        HashSet<Annotation> withAnnot2 = ad2.getWithAnnotSet();
        if (null != withAnnot2) {
            withAnnot2 = (HashSet<Annotation>)withAnnot2.clone();
            withAnnot2.remove(a2);
        }
        if (false == annotSetEquals(withAnnot1, withAnnot2)) {
            return false;
        }
        
        if (false == linkedHashMapsSame(ad1.getInheritedQualifierLookup(), ad2.getInheritedQualifierLookup(), a1, a2)) {
            return false;
        }

        if (false == linkedHashMapsSame(ad1.getAddedQualifierLookup(), ad2.getAddedQualifierLookup(), a1, a2)) {
            return false;
        }
                
        if (false == linkedHashMapsSame(ad1.getRemovedQualifierLookup(), ad2.getRemovedQualifierLookup(), a1, a2)) {
            return false;
        }                
        
        
        if (false == nodeSetEquals(ad1.getWithNodeSet(), ad2.getWithNodeSet())) {
            return false;
        }
        
        if (false == dbRefSetEquals(ad1.getWithOtherSet(), ad2.getWithOtherSet())) {
            return false;
        }

        return true;
    }
    
    private boolean nodeSetEquals(HashSet<Node> set1, HashSet<Node> set2) {
        if (null == set1 && null == set2) {
            return true;
        }
        if ((set1 != null && set2 == null) || (set1 == null && set2 != null)) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        for (Node n1: set1) {
            boolean found = false;
            for (Node n2: set2) {
                if (true == equals(n1, n2)) {
                    found = true;
                    break;
                }
                if (false == found) {
                    return false;
                }
            }
        }
        
        for (Node n2: set2) {
            boolean found = false;
            for (Node n1: set1) {
                if (true == equals(n1, n2)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }
        return true;
    }
    

    
    private boolean dbRefSetEquals(HashSet<DBReference> set1, HashSet<DBReference> set2) {
        if (null == set1 && null == set2) {
            return true;
        }
        if ((set1 != null && set2 == null) || (set1 == null && set2 != null)) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        for (DBReference dbRef1: set1) {
            boolean found = false;
            for (DBReference dbRef2: set2) {
                if (true == equals(dbRef1, dbRef2)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                return false;
            }
        }
        
        for (DBReference dbRef2: set2) {
            boolean found = false;
            for (DBReference dbRef1: set1) {
                if (true == equals(dbRef1, dbRef2)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                return false;
            }

        }
        
        return true;
    }
    
    private boolean annotSetEquals(HashSet<Annotation> set1, HashSet<Annotation> set2) {
        if (null == set1 && null == set2) {
            return true;
        }
        if ((set1 != null && set2 == null) || (set1 == null && set2 != null)) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        
        for (Annotation a1: set1) {
            boolean found = false;
            for (Annotation a2: set2) {
                if (true == equals(a1, a2)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }
        
        for (Annotation a2: set2) {
            boolean found = false;
            for (Annotation a1: set1) {
                if (true == equals(a1, a2)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                return false;
            }
        }
        
        return true;
    }    
    
    private boolean linkedHashMapsSame(LinkedHashMap<Qualifier, HashSet<Annotation>> map1, LinkedHashMap<Qualifier, HashSet<Annotation>> map2, Annotation a1, Annotation a2) {
        if (null == map1 && null == map2) {
            return true;
        }
        if ((map1 != null && map2 == null) || (map1 == null && map2 != null)) {
            return false;
        }
        if (map1.size() != map2.size()) {
            return false;
        }
        
        Set<Qualifier> set1 = map1.keySet();
        Set<Qualifier> set2 = map2.keySet();
        HashSet<Qualifier> hSet1 = new HashSet<Qualifier>(set1);
        HashSet<Qualifier> hSet2 = new HashSet<Qualifier>(set2);
        QualifierDif qd = new QualifierDif(hSet1, hSet2);
        if (QualifierDif.QUALIFIERS_SAME != qd.getDifference()) {
            return false;
        }
        
        for (Qualifier q1: hSet1) {
            Qualifier q2 = QualifierDif.find(hSet2, q1);
            if (null == q2) {
                return false;
            }
            
            // Qualifiers sometimes point to annotation that is currently being compared.  Remove to avoid infinite loop
            HashSet<Annotation> annotSet1 = map1.get(q1);
            if (null != annotSet1) {
                annotSet1 = (HashSet<Annotation>)annotSet1.clone();
                annotSet1.remove(a1);
            }
            
            HashSet<Annotation> annotSet2 = map2.get(q2);
            if (null != annotSet2) {
                annotSet2 = (HashSet<Annotation>)annotSet2.clone();
                annotSet2.remove(a2);
            }
            if (false == annotSetEquals(annotSet1, annotSet2)) {
                return false;
            }
        }
        
        return true;
    }
    
    public static HashSet<String> initPaintEvidenceTypes() {
        HashSet<String> rtnSet = new HashSet<String>(3);
        rtnSet.add(EVIDENCE_TYPE_ANNOT_PAINT_REF);
        rtnSet.add(EVIDENCE_TYPE_ANNOT_PAINT_EXP);
        rtnSet.add(EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);                     
        return rtnSet;
    }
    
    public static final HashSet<String> getPAINTEvidenceTypeSet() {
        return PAINT_EVIDENCE_TYPE_SET;
    }
    
    public static boolean isPAINTEvidenceType(HashSet<WithEvidence> withEvidenceSet) {
        if (null == withEvidenceSet) {
            return false;
        }
        for (WithEvidence we: withEvidenceSet) {
            String evidenceType = we.getEvidenceType();
            if (null != evidenceType && PAINT_EVIDENCE_TYPE_SET.contains(evidenceType)) {
                return true;
            }
        }
        return false;
    }
    
    public  boolean isPAINTType() {
        if (null == evidenceType) {
            return false;
        }
        return PAINT_EVIDENCE_TYPE_SET.contains(evidenceType);
    }
}
