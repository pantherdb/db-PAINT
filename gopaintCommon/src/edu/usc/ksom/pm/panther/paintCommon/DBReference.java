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
import java.util.ArrayList;

/**
 *
 * @author muruganu
 */
public class DBReference implements Serializable {
    private String evidenceTypeId;          
    private String evidenceType;            // Pubmed, Reactome, website, PANTHER... 
    private String evidenceValue;           // REACT_736, PTN1234
    
    public static final String TYPE_PMID = "PMID";          // DO NOT CHANGE

    public String getEvidenceTypeId() {
        return evidenceTypeId;
    }

    public void setEvidenceTypeId(String evidenceTypeId) {
        this.evidenceTypeId = evidenceTypeId;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }    

    public String getEvidenceValue() {
        return evidenceValue;
    }

    public void setEvidenceValue(String evidenceValue) {
        this.evidenceValue = evidenceValue;
    }
    
    public DBReference makeCopy() {
        DBReference newDbRef = new DBReference();
        newDbRef.evidenceType = this.evidenceType;
        newDbRef.evidenceValue = this.evidenceValue;
        return newDbRef;
    }
    
    public static void addIfNotPresent(ArrayList<DBReference> refList, DBReference dbRef) {
        String type = dbRef.getEvidenceType();
        String value = dbRef.getEvidenceValue();
        if (null == type || null == value) {
            return;
        }
        for (DBReference cur: refList) {
            if ((cur.evidenceType == null && type != null) || (cur.evidenceType != null && type == null)) {
                continue;
            }
            if (null != cur.evidenceType && null != type && false == type.equals(cur.evidenceType)) {
                continue;
            }
            if ((cur.evidenceValue == null && value != null) || (cur.evidenceValue != null && value == null)) {
                continue;
            }
            if (null != cur.evidenceValue && null != value && false == value.equals(cur.evidenceValue)) {
                continue;
            }
            if (true == type.equals(cur.evidenceType) && true == value.equals(cur.evidenceValue)) {
                return;
            }
            
        }
        refList.add(dbRef);
    }
}
