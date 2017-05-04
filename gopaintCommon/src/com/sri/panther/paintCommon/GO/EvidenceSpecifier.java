package com.sri.panther.paintCommon.GO;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.User;

import com.sri.panther.paintCommon.util.StringUtils;

import com.sri.panther.paintCommon.util.Utils;

import java.io.Serializable;

import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Pattern;


public class EvidenceSpecifier implements Serializable, Comparable {

    public static final String EVIDENCE_NOT_SPECIFIED = "Evidence not specified";
    public static final String EVIDENCE_CODE_NOT_SPECIFIED = "Evidence code not specified";
    public static final String EVIDENCE_LONG_BRANCH_LENGTH = "long_branch";
    public static final String EVIDENCE_MISSING_DOMAIN_SEQUENCE_FEATURE = "missing_seq_feature";

    public static Object[] options = {"Yes, enter Pubmed Id", "No, select predefined evidence type", "Cancel"};
    public static String[] EVIDENCE_POSSIBILITIES = {EVIDENCE_LONG_BRANCH_LENGTH, EVIDENCE_MISSING_DOMAIN_SEQUENCE_FEATURE};
    public static String MSG_ENTER_EVIDENCE = "Enter Pubmed id (numeric) or select from choices.";
    public static String MSG_EVIDENCE = "Evidence";
    public static String MSG_ENTER_PUBMED_ID = "Enter Pubmed id";
    public static String MSG_CHOOSE_FROM_LIST = "Choose from list";
    public static final String DELIM_EVIDENCE_SPECIFIER = Constant.STR_COMMA;
    
    
    public static final String GAF_PMID_SPECIFIER = "PMID";
    public static final String GAF_EVIDENCE_SEPARATOR = Constant.STR_PIPE;
    public static final String GAF_EVIDENCE_SEPARATOR_TYPE_ID = Constant.STR_COLON;






    User createdBy;
    // TODO with field can contain three types of information need to use a more complex data structure.  This was a last minute change
    // protein id, annotation id or annotation id|user entered evidence - protein id gets changed to gene identifier when data is sent from server to client 
    String with;
    
    String evidenceId;      // Can be pubmed id (numeric) or EVIDENCE_POSSIBILITIES
    String comment; // When evidenceId set to Constant.EVIDENCE_MISSING_DOMAIN_SEQUENCE_FEATURE 
    String evidenceCode;    
    
    public EvidenceSpecifier(String with, User createdBy, String evidenceId, String comment, String evidenceCode) {
        this.with = with;
        this.createdBy = createdBy;
        this.evidenceId = evidenceId;
        this.comment = formatCommment(comment);
        this.evidenceCode = evidenceCode;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setComment(String comment) {
        this.comment = formatCommment(comment);
    }

    public String getComment() {
        return comment;
    }
    
    /**
     *  
     * @param cmtStr
     * @return comment string after removal of special characters
     */
    public String formatCommment(String cmtStr) {
        if (null == cmtStr) {
            return null;
        }
        cmtStr = Utils.replace(cmtStr, Constant.GAF_FIELD_SEPARATOR, Constant.STR_EMPTY);
        cmtStr = Utils.replace(cmtStr, GAF_EVIDENCE_SEPARATOR, Constant.STR_EMPTY);
        cmtStr = Utils.replace(cmtStr, GAF_EVIDENCE_SEPARATOR_TYPE_ID, Constant.STR_EMPTY);
        return cmtStr;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }


    public String getWith() {
        return with;
    }

    public void setWith(String with) {
        this.with = with;
    }
    
    public int compareTo(Object o) {
        if (null == o) {
            return 1;
        }
        if (false == o instanceof EvidenceSpecifier) {
            return 1;
        }
        EvidenceSpecifier comp = (EvidenceSpecifier)o;
        int diff = evidenceId.compareTo(comp.evidenceId);
        if (0 == diff) {
            return comment.compareTo(comp.comment);
        }
        return diff;
    }
    
    
    
    public Object clone() {
        if (null == createdBy) {
            return new EvidenceSpecifier(with, null, evidenceId, comment, evidenceCode);
        }
        return new EvidenceSpecifier(with, (User)createdBy.clone(), evidenceId, comment, evidenceCode);
    }
    
    public static Vector<EvidenceSpecifier> cloneList(Vector<EvidenceSpecifier> esList) {
        if (null == esList) {
            return null;
        }
        int size = esList.size();
        Vector <EvidenceSpecifier> newList = new Vector<EvidenceSpecifier>(size);
        for (int i = 0; i < size; i++) {
            newList.add((EvidenceSpecifier)esList.get(i).clone());
        }
        
        return newList;
    }
    
    public static Vector<EvidenceSpecifier> copyList(Vector<EvidenceSpecifier> list) {
        if (null == list) {
            return null;
        }
        int size = list.size();
        Vector<EvidenceSpecifier> rtnList = new Vector<EvidenceSpecifier>(size);
        for (int i = 0; i < size; i++) {
            rtnList.add((EvidenceSpecifier)list.get(i).clone());
        }
        return rtnList;
    }
    
    
    public static  String formatEvidenceIdInfo(Vector<EvidenceSpecifier> list) {
        if (null == list) {
            return Constant.STR_EMPTY;
        }
        StringBuffer sb = new StringBuffer();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(DELIM_EVIDENCE_SPECIFIER);
            }
            sb.append(list.get(i).getEvidenceId());

        }
        return sb.toString();
    }
    
    
//    public static  String formatWithInfo(Vector<EvidenceSpecifier> list) {
//        if (null == list) {
//            return Constant.STR_EMPTY;
//        }
//        StringBuffer sb = new StringBuffer();
//        int size = list.size();
//        for (int i = 0; i < size; i++) {
//            
//            sb.append(list.get(i).getWith());
//            sb.append(DELIM_EVIDENCE_SPECIFIER);
//        }
//        return sb.toString();
//    }
    
    
    public static  String formatEvidenceCodeInfo(Vector<EvidenceSpecifier> list) {
        if (null == list) {
            return Constant.STR_EMPTY;
        }
        StringBuffer sb = new StringBuffer();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(Constant.GAF_EVIDENCE_CODE_SEPARATOR);
            }
            sb.append(list.get(i).getEvidenceCode());

        }
        return sb.toString();
    }
    
    public static  String formatEvidenceWith(Vector<EvidenceSpecifier> list) {
        if (null == list) {
            return Constant.STR_EMPTY;
        }
        StringBuffer sb = new StringBuffer();
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sb.append(DELIM_EVIDENCE_SPECIFIER);
            }
            sb.append(list.get(i).getWith());

        }
        return sb.toString();
    }
    
    
    public static boolean findWith(Vector<EvidenceSpecifier> list, String with) {
        if (null == list || null == with) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            if (true == list.get(i).with.equals(with)) {
                return true;
            }
        }
        return false;
    }
    
    public static void setWith(Vector<Evidence> evList, String with) {
        if (null == evList) {
            return;
        }
        for (int i = 0; i < evList.size(); i++) {
            Evidence e = evList.get(i);
            Vector<EvidenceSpecifier> esList = e.getEvidenceSpecifierList();
            setWithForList(esList, with);
        }
    }
    
    
    public static void setWithForList(Vector <EvidenceSpecifier> esList, String with) {
        if (null == esList || null == with) {
            return;
        }
        for (int j = 0; j < esList.size(); j++) {
            esList.get(j).with = with;
        }
    }
    
    public static void setEvidenceCodeForList(Vector <EvidenceSpecifier> esList, String evidenceCode) {
        if (null == esList || null == evidenceCode) {
            return;
        }
        for (int j = 0; j < esList.size(); j++) {
            esList.get(j).evidenceCode = evidenceCode;
        }
    }
    

    public static void setEvidenceIdForList(Vector <EvidenceSpecifier> esList, String evidenceId) {
        if (null == esList || null == evidenceId) {
            return;
        }
        for (int j = 0; j < esList.size(); j++) {
            esList.get(j).evidenceId = evidenceId;
        }
    }    
    
    public static Vector<String> getDistinctWithList(Vector<EvidenceSpecifier> esList) {
        if (null == esList) {
            return null;
        }
        Vector<String> rtnList = new Vector<String>();
        for (int i = 0; i < esList.size(); i++) {
            EvidenceSpecifier es = esList.get(i);
            String with = es.getWith();
            if (true == Utils.search(rtnList, with)) {
                continue;
            }
            rtnList.add(with);
        }
        return rtnList;
    }
    
    public static String formatUserNameForGAF(Vector<EvidenceSpecifier> esList) {
        if (null == esList || 0 == esList.size()) {
            return Constant.STR_EMPTY;
        }
        EvidenceSpecifier es = esList.get(0);
        User u = es.createdBy;
        return Utils.replace(u.getFirstName(), Constant.GAF_FIELD_SEPARATOR, Constant.STR_EMPTY);    // No last name in database

        
        
    }
    
    
    public static String getEvidenceIdForGAF(EvidenceSpecifier es) {
        String id = es.evidenceId;
        if (false == Utils.search(EVIDENCE_POSSIBILITIES, id)) {
            return GAF_PMID_SPECIFIER + GAF_EVIDENCE_SEPARATOR_TYPE_ID + id;
        }
        else {
            if (true == id.equals(EVIDENCE_MISSING_DOMAIN_SEQUENCE_FEATURE)) {
                return EVIDENCE_MISSING_DOMAIN_SEQUENCE_FEATURE + GAF_EVIDENCE_SEPARATOR_TYPE_ID + es.comment;
            }
            return id + GAF_EVIDENCE_SEPARATOR_TYPE_ID;
        }
    }
    
    
    public static String formatEvidenceIdListForGAF(Vector <EvidenceSpecifier> list) {
        if (null == list) {
            return Constant.STR_EMPTY;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            sb.append(getEvidenceIdForGAF(list.get(i)));
            sb.append(GAF_EVIDENCE_SEPARATOR);
        }
        return sb.toString();
    }
    
    
    public static String formatWithListForGAF(Vector <EvidenceSpecifier> list) {
        if (null == list) {
            return Constant.STR_EMPTY;
        }
        String withList = getWithList(list);
        if (null != withList) {
            return withList;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) {
                sb.append(Constant.GAF_SEPARATOR_WITH);
            }
            sb.append(list.get(i).with);

        }
        return sb.toString();
    }
    
    private static String getWithList(Vector <EvidenceSpecifier> list) {
        // If all the with values are the same, then only returns one value
        int size = list.size();
        String first = list.get(0).getWith();
        if (1 == size) {
            return first;
        }
        
        for (int i = 1; i < size; i++) {
            EvidenceSpecifier es = list.get(i);
            if (!first.equals(es.getWith())) {
                return null;
            }
        }
        return first;
    }
    
    /**
     *
     * @param gafEvidenceStr
     * @param withs - can contain single annotation node id or list of with
     * @param evidenceCode
     * @param firstAndLastName
     * @return
     */
    public static Vector<EvidenceSpecifier> generateEvidenceSpecifier(String gafEvidenceStr, String[] withs, String evidenceCode, String firstAndLastName) {
        User u = new User(firstAndLastName, null, null, null, Constant.USER_PRIVILEGE_NOT_SET, null);
//        Pattern withPattern = Pattern.compile(Constant.GAF_SEPARATOR_WITH, Pattern.LITERAL);
//        String[] withs = withPattern.split(with);
//        Pattern evidenceCodePattern = Pattern.compile(Constant.GAF_EVIDENCE_CODE_SEPARATOR, Pattern.LITERAL);
//        String[] codes = evidenceCodePattern.split(evidenceCode);
//        Pattern p = Pattern.compile(GAF_EVIDENCE_SEPARATOR, Pattern.LITERAL);
//        String[] evidences = p.split(gafEvidenceStr);
        int size = withs.length;
        Vector <EvidenceSpecifier> rtnList = new Vector <EvidenceSpecifier>(size);
//        Pattern splitPattern = Pattern.compile(GAF_EVIDENCE_SEPARATOR_TYPE_ID, Pattern.LITERAL);
        for (int i = 0; i < size; i++) {
//            String evidenceInfo = evidences[i];
//            String parts[] = splitPattern.split(evidenceInfo);
//            String cmtStr = null;
//            String id;
//            if (parts[0].equals(GAF_PMID_SPECIFIER)) {
//                id = parts[1];
//            }
//            else {
//                id = parts[0];
//            }
//            if (parts[0].equals(EVIDENCE_MISSING_DOMAIN_SEQUENCE_FEATURE) && parts.length > 1) {
//                cmtStr = parts[1];
//            }
//            
//            // If 'with' is from a single annotation node id, use same annotation node id for all entries
//            String with = null;
//            if (withs.length < i) {
//                with = withs[0];
//            }
//            else {
//                with = withs[i];
//            }
            rtnList.add(new EvidenceSpecifier(withs[i], u, gafEvidenceStr, null, evidenceCode));
        }
        return rtnList;
        
    }

}
