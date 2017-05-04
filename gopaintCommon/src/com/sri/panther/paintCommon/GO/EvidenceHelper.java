package com.sri.panther.paintCommon.GO;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.Utils;

import java.io.Serializable;

import java.util.Vector;

public class EvidenceHelper implements Serializable{

    
    public static Vector<Evidence> parseAnnotationData(String annotStr) {
        Vector <Evidence> rtnList = new Vector<Evidence>();
        String annotList[] = Utils.tokenize(annotStr, Constant.STR_SEMI_COLON);
        for (int i = 0; i < annotList.length; i++) {
            String anAnnot = annotList[i];
            int index_equal = anAnnot.indexOf(Constant.STR_EQUAL);
            String typeAndAcc = anAnnot.substring(0, anAnnot.indexOf(index_equal + 1));
            String nameAndDef = anAnnot.substring(index_equal + 1);
            int index_dash = typeAndAcc.indexOf(Constant.STR_DASH);
            String type = typeAndAcc.substring(0, index_dash + 1);
            String acc = typeAndAcc.substring(index_dash);
            String qualifier = null;
            String name = null;
            boolean qualifierIsNot = false;
            int beginIndex = nameAndDef.lastIndexOf(Constant.STR_BRACKET_ROUND_OPEN);
            int endIndex = nameAndDef.lastIndexOf(Constant.STR_BRACKET_ROUND_CLOSE);
            if (beginIndex < endIndex && (beginIndex > 0 && endIndex > 0)) {
                qualifier = nameAndDef.substring(beginIndex + 1, endIndex);
                if (true == qualifier.equals(Evidence.NOT)) {
                    qualifierIsNot = true;
                }
                name = nameAndDef.substring(0, beginIndex);
            }
            else {
                name = nameAndDef;
            }
            Evidence e = new Evidence(acc, name, type, null, qualifierIsNot);
            if (null != qualifier) {
                e.setQualifierStr(qualifier);
            }
            rtnList.add(e);
        }
        return rtnList;
        
        
    }
}
