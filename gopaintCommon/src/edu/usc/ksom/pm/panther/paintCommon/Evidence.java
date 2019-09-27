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
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class Evidence implements Serializable {

    private String evidenceId;
    private ArrayList<DBReference> dbReferenceList;     // Who is associating this evidence with the GO term PAINT, PubMed article, 

    private String evidenceCodeid;          
    private String evidenceCode;                        // EXP, IC, ...

    private ArrayList<DBReference> withs;               // Proof for the annotation
    private String date;                    //20090118
    public static final String CODE_IKR = "IKR";
    public static final String CODE_IRD = "IRD";
    public static final String CODE_IBA = "IBA";
    public static final String CODE_IBD = "IBD";
    public static final String CODE_IEA = "IEA";
    
    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }



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

    public ArrayList<DBReference> getDbReferenceList() {
        return dbReferenceList;
    }

    public void setDbReferenceList(ArrayList<DBReference> dbReferenceList) {
        this.dbReferenceList = dbReferenceList;
    }
    
    public void addDbRef(DBReference reference) {
        if (null == this.dbReferenceList) {
            this.dbReferenceList = new ArrayList<DBReference>();
        }
        this.dbReferenceList.add(reference);
    }

    public ArrayList<DBReference> getWiths() {
        return withs;
    }

    public void setWiths(ArrayList<DBReference> withs) {
        this.withs = withs;
    }
    
    public void addWith(DBReference with) {
        if (null == this.withs) {
            this.withs = new ArrayList<DBReference>();
        }
        withs.add(with);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

//    public static final String[][] evidenceCodes = {
//        {"EXP", "Experimental data"},
//        {"IC", "Curator"},
//        {"IDA", "Direct Assay"},
//        {"IEA", "Electronic Analysis"},
//        {"IEP", "Expression Pattern"},
//        {"IGC", "Genomic Context"},
//        {"IGI", "Genetic Interaction"},
//        {"IMP", "Mutant Phenotype"},
//        {"IPI", "Physical Interaction"},
//        {"ISA", "Sequence Alignment"},
//        {"ISM", "Sequence Model Similarity"},
//        {"ISO", "Sequence Orthology"},
//        {"ISS", "Sequence or Structural Similarity"},
//        {"NAS", "Not-traceable Author Statement"},
//        {"ND", "No data available"},
//        {"NR", "Not Recorded"},
//        {"RCA", "Reviewed Computational Analysis"},
//        {"TAS", "Traceable Author Statement"},};

    private static final HashSet<String> experimentalCodes = initExperimental();
    private static final HashSet<String> paintCodes = initPaintCodes();

    private static final HashSet<String> initExperimental() {
        HashSet expSet = new HashSet();
        expSet.add("EXP");
        expSet.add("IDA");
        expSet.add("IPI");
        expSet.add("IMP");
        expSet.add("IGI");
        expSet.add("IEP");
        expSet.add("HTP");
        expSet.add("HDA");  
        expSet.add("HMP");  
        expSet.add("HGI");
        expSet.add("HEP");
        expSet.add("IKR");      // NOTE, CAN HAVE IKR as experimental evidence as well as PAINT evidence       
        return expSet;
    }
    
    public static HashSet<String> getExperimental() {
        if (null == experimentalCodes) {
            return null;
        }
        return (HashSet<String>)experimentalCodes;
    }
    
    private static final HashSet<String> initPaintCodes() {
        HashSet<String> paintSet = new HashSet<String>();
        paintSet.add(CODE_IBD);
        paintSet.add(CODE_IRD);
        paintSet.add(CODE_IKR);
        paintSet.add(CODE_IBA);        
        return paintSet;
    }

//    public boolean isExperimental() {
//        if (null == this.evidenceCode) {
//            return false;
//        }
//        return experimentalCodes.contains(this.evidenceCode.toUpperCase());
//    }
    
    public static boolean isExperimental(String code) {
        if (null == code || null == experimentalCodes) {
            return false;
        }
        return experimentalCodes.contains(code);
    }
    
    public static boolean isExperimentalCodeValidForNode(String code, boolean isLeaf) {
        if (false == isLeaf) {
            return false;
        }
        if (null == code || null == experimentalCodes) {
            return false;
        }
        return experimentalCodes.contains(code);        
    } 
    
//    public boolean isPaint() {
//        if (null == this.evidenceCode) {
//            return false;
//        }
//        return paintCodes.contains(this.evidenceCode.toUpperCase());
//    }
    
    public static boolean isPaint(String code) {
        if (null == code || null == paintCodes) {
            return false;
        }
        return paintCodes.contains(code);        
    }
    
    public static boolean isPAINTCodeValidForNode(String code, boolean isLeaf) {
        if (null == code || null == paintCodes) {
            return false;
        }
        if (true == isLeaf)  {
            if (true == CODE_IKR.equals(code)) {
                return true;
            }
            else if (true == CODE_IRD.equals(code)) {
                return false;
            }
            else if (true == CODE_IBD.equals(code)) {
                return false;
            }            
        }
        return paintCodes.contains(code);
    }
    
    public Evidence makeCopy() {
        Evidence newEvidence = new Evidence();
        newEvidence.date = this.date;
        newEvidence.evidenceCode = this.evidenceCode;
        
        if (null != dbReferenceList) {
            ArrayList<DBReference> newDBRefList = new ArrayList<DBReference>(dbReferenceList.size());
            newEvidence.setDbReferenceList(newDBRefList);
            for (DBReference dbRef: dbReferenceList) {
                newDBRefList.add(dbRef.makeCopy());
            }
        }
        
        if (null != withs) {
            ArrayList<DBReference> newWithsList = new ArrayList<DBReference>(withs.size());
            newEvidence.setDbReferenceList(newWithsList);
            for (DBReference dbRef: withs) {
                newWithsList.add(dbRef.makeCopy());
            }
        }
        
        return newEvidence;
    }
}

