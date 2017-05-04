package com.sri.panther.paintCommon.GO;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.GO.EvidenceSpecifier;

import com.sri.panther.paintCommon.util.Utils;

import java.io.Serializable;

import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class Evidence implements Serializable, Comparable {
    protected String accession;
    protected String name;
    protected Date creationDate;

    protected Vector<EvidenceSpecifier> evidenceSpecifierList;
    protected String type;        // GO aspect
    boolean notQualifier = false;       // True if NOT condition is true
    protected String qualifierStr = null;
    protected boolean reviewed = false; // Used by PANTHER to differentiate evidences from auto curation
    
    protected static Logger logger = Logger.getLogger(Evidence.class);
    
    public static final String GO_TYPE_MOLECULAR_FUNCTION = "F";
    public static final String GO_TYPE_BIOLOGICAL_PROCESS = "P";
    public static final String GO_TYPE_CELLULAR_COMPONENT = "C";
    
    public static final String MSG_INVALID_COMPARISON = " is current and is being compared with ";
    
    public static final String NOT = "NOT";
    public static final String REVIEWED = "REVIEWED";
    public static final String REVIEWED_STR = Constant.STR_BRACKET_ROUND_OPEN + Evidence.REVIEWED + Constant.STR_BRACKET_ROUND_CLOSE + Constant.STR_DASH;
    public static final int LENGTH_REVIEWED_STR = REVIEWED_STR.length();
    

    
    
    public Evidence(String accession, String name, String type, Vector<EvidenceSpecifier> evidenceSpecifierList, boolean qualifier) {
        this.accession = accession;
        this.name = name;
        this.type = type;
        this.evidenceSpecifierList = evidenceSpecifierList;
        this.notQualifier = qualifier;
        
    }


    public String getAccession() {
        return accession;
    }

    public String getName() {
        return name;
    }

    
    public void setEvidenceSpecifierList(Vector<EvidenceSpecifier> evidenceSpecifierList) {
        this.evidenceSpecifierList = evidenceSpecifierList;
    }

    public Vector<EvidenceSpecifier> getEvidenceSpecifierList() {
        return evidenceSpecifierList;
    }
    
    public void addEvidenceSpecifier(EvidenceSpecifier es) {
        if (null == evidenceSpecifierList) {
            evidenceSpecifierList = new Vector<EvidenceSpecifier>(1);
        }
        evidenceSpecifierList.add(es);
    }
    
    
    public void addEvidenceSpecifierList(Vector <EvidenceSpecifier> list) {
        if (null == list) {
            return;
        }
        if (null == evidenceSpecifierList) {
            evidenceSpecifierList = new Vector<EvidenceSpecifier>(list.size());
        }
        evidenceSpecifierList.addAll(list);
    }
    
    

    public boolean isNotQualifier() {
        return notQualifier;
    }
    
    public void setNotQualifier(boolean qualifier) {
        this.notQualifier = qualifier;
    }
    
    
    public String getQualifierStr() {
        return qualifierStr;
    }
    
    public void setQualifierStr(String s) {
        qualifierStr = s;
    }
    
    public int getIndex(Vector<Evidence> list, Evidence search) {
        if (null == list || null == search) {
            return -1;
        }
        for (int i = 0; i < list.size(); i++) {
            Evidence current = list.get(i);
            if (0 == current.compareTo(search)) {
                return i;
            }
        }
        return -1;
    }
    
    public int compareTo(Object o) {
        if (null == o) {
            return 1;
        }
        if (false == o instanceof Evidence) {
            return 1;
        }
        Evidence comp = (Evidence)o;
        int diff = type.compareTo(comp.type);
        //System.out.println("CompARING " + type + " to " + comp.type);
        // Want to order as follows:    molecular function, biological process followed by cellular component
        if (diff != 0) {
            if (type.equals(GO_TYPE_MOLECULAR_FUNCTION)) {
                //System.out.println("Returning positive");
                return 1;
            }
            else if (comp.type.equals(GO_TYPE_MOLECULAR_FUNCTION)) {
                //System.out.println("Returning negatinve ");
                return -1;
            }
            else if (type.equals(GO_TYPE_CELLULAR_COMPONENT)) {
                //System.out.println("Returning negatinve ");
                return -1;
            }
            else if (comp.type.equals(GO_TYPE_CELLULAR_COMPONENT)) {
                //System.out.println("Returning positive");
                return 1;
            }
            else {
                // Should not be in here
                logger.error(type + MSG_INVALID_COMPARISON + comp.type);
                return 0;
            }
        }
        else {
            return accession.compareTo(comp.accession);
        }
    }
    
    
    public static boolean qualifiersSame(Evidence e1, Evidence e2) {
        String qualifier1 = e1.getQualifierStr();
        String qualifier2 = e2.getQualifierStr();
        if (null == qualifier1 && null == qualifier2) {
            return true;
        }
        if (null != qualifier1 && null != qualifier2) {
            return qualifier1.equals(qualifier2);
        }
        return false;
    }
    
//    public boolean equals (Object o) {
//        if (null == o) {
//            return false;
//        }
//        if (false == o instanceof Evidence) {
//            return false;
//        }
//        Evidence comp = (Evidence)o;
//        if (0 == accession.compareTo(comp.accession) &&
//             0 == name.compareTo(comp.name) &&
//             0 == with.compareTo(comp.with) &&
//             0 == evidenceId.compareTo(comp.evidenceId) &&
//             notQualifier == comp.notQualifier) {
//            return true;
//        }
//        return false;
//        
//    }
    
    
    public Object clone() {
        Evidence e = new Evidence(accession, name, type, EvidenceSpecifier.copyList(evidenceSpecifierList), notQualifier);
        e.qualifierStr = qualifierStr;
        e.reviewed = reviewed;
        return e;
    }
    
    
    public static void copy (Evidence to, Evidence from) {
        to.accession = from.accession;
        to.name = from.name;
        to.type = from.type;
        to.evidenceSpecifierList = EvidenceSpecifier.copyList(from.getEvidenceSpecifierList());
        to.notQualifier = from.notQualifier;
        to.qualifierStr = from.qualifierStr;
        to.reviewed = from.reviewed;
    }




    public String getType() {
        return type;
    }
    
    
    /*
     * Given a list of Evidence, where there are items with the same evidence accession and qualifier (duplicates), remove the duplicate
     * and add the evidence specifier from the duplicate into the EvidenceSpecifierList
     */
    public static Vector <Evidence> organizeEvidence(Vector <Evidence> list) {
        Hashtable<String, Evidence> accToEvidence = new Hashtable<String, Evidence>();
        for (int i = 0; i < list.size(); i++) {
            Evidence e = list.get(i);
            String key = null;
            if (true == e.isNotQualifier()) {
                key = NOT + e.getAccession();
            }
            else {
                key = e.getAccession();
            }
            Evidence value = accToEvidence.get(key);
            if (null == value) {
                accToEvidence.put (key, e);
                continue;
            }
            Vector <EvidenceSpecifier> esList = value.getEvidenceSpecifierList();
            esList.addAll(e.getEvidenceSpecifierList());
            value.setEvidenceSpecifierList(esList);
            
            
        }
        return new Vector(accToEvidence.values());
        
    }
    
    
    public void setReviewed(boolean b) {
        reviewed = b;
    }
    
    public boolean isReviewed() {
        return reviewed;
    }
    
    
    
    public static String convertAnnotTblToString(Hashtable<String, com.sri.panther.paintCommon.GO.Evidence> annotTbl) {
        if (null == annotTbl) {
            return null;
        }
        
        int size = annotTbl.size();
        String keys[] = new String[size];
        annotTbl.keySet().toArray(keys);
        Arrays.sort(keys);
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            Evidence e = annotTbl.get(keys[i]);
            if (true == e.isReviewed()) {
                sb.append(REVIEWED_STR);
            }
            sb.append(e.getType() + Constant.STR_DASH + e.getAccession() + Constant.STR_EQUAL + e.getName());
            String qualifierStr = e.getQualifierStr();
            if (null != qualifierStr) {
                sb.append(Constant.STR_BRACKET_ROUND_OPEN + qualifierStr + Constant.STR_BRACKET_ROUND_CLOSE);
            }
            sb.append(Constant.STR_SEMI_COLON);
        }
        return sb.toString();
        
    }
    
    
    
    public static Hashtable<String, com.sri.panther.paintCommon.GO.Evidence> convertStringToTable(String annotStr) {
        if (null == annotStr) {
            return null;
        }
        
        Hashtable<String, com.sri.panther.paintCommon.GO.Evidence> annotTbl = new Hashtable<String, com.sri.panther.paintCommon.GO.Evidence>();
        String annotList[] = Utils.tokenize(annotStr, Constant.STR_SEMI_COLON);
        for (int i = 0; i < annotList.length; i++) {
            String anAnnot = annotList[i];
            System.out.println("Processing " + anAnnot);
            boolean reviewed = false;
            
            if (true == anAnnot.startsWith(REVIEWED_STR)) {
                reviewed = true;
                anAnnot = anAnnot.substring(LENGTH_REVIEWED_STR);
            }
            int index = anAnnot.indexOf(Constant.STR_DASH);
            String type = anAnnot.substring(0, index);
            anAnnot = anAnnot.substring(index + 1);
            
            index = anAnnot.indexOf(Constant.STR_EQUAL);
            String accession = anAnnot.substring(0, index);
            
            anAnnot = anAnnot.substring(index + 1);
            int beginIndex = anAnnot.indexOf(Constant.STR_BRACKET_ROUND_OPEN);
            int endIndex = anAnnot.indexOf(Constant.STR_BRACKET_ROUND_CLOSE);
            String qualifier = null;
            String nameStr = null;
            if (beginIndex < endIndex && anAnnot.endsWith(Constant.STR_BRACKET_ROUND_CLOSE)) {
                qualifier = anAnnot.substring(beginIndex + 1, endIndex);
                nameStr = anAnnot.substring(0, beginIndex);
            }
            else {
                nameStr = anAnnot;
            }
            
            boolean isNot = false;
            if (null != qualifier && true == qualifier.equals(NOT)) {
                isNot = true;
            }
            
            Evidence e = new Evidence(accession, nameStr, type, null, isNot);
            e.setQualifierStr(qualifier);
            e.setReviewed(reviewed);
            annotTbl.put(accession, e);
            System.out.println("values are reviewed - " + Boolean.toString(reviewed) + " type " + type + " accession " + accession + " name is " + nameStr + " qualifier str " + qualifier + " isnot " + Boolean.toString(isNot));
            
            
            

            
            
        }
        
        
        return annotTbl;
    }
    
    
    public static void main(String[] args){
        String annotStr = "BP-GO:0002376=immune system process;(REVIEWED)-BP-GO:0006810=transport;BP-GO:0006811=ion transport(NOT);BP-GO:0006812=cation transport;MF-GO:0008324=cation transmembrane transporter activity(a qualifier);MF-GO:0016491=oxidoreductase activity;MF-GO:0022857=transmembrane transporter activity;(REVIEWED)-BP-GO:0051179=localization(NOT);PC-PC00068=cation transporter;PC-PC00072=chaperone;PC-PC00176=oxidoreductase;PC-PC00227=transporter;";
        Hashtable annotTbl = Evidence.convertStringToTable(annotStr);
        annotTbl = Evidence.convertStringToTable("");
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }
}
