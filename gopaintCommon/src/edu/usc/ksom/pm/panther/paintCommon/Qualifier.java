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

/**
 *
 * @author muruganu
 */
public class Qualifier implements Serializable {
    private String qualifierId;
    private String text;
    
    public static final String QUALIFIER_NOT = "NOT";
    public static final String QUALIFIER_COLOCALIZES_WITH = "COLOCALIZES_WITH";
    public static final String QUALIFIER_CONTRIBUTES_TO = "CONTRIBUTES_TO";

    public String getQualifierId() {
        return qualifierId;
    }

    public void setQualifierId(String qualifierId) {
        this.qualifierId = qualifierId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public boolean isNot() {
        if (null != text && text.equalsIgnoreCase(QUALIFIER_NOT)) {
            return true;
        }
        return false;
    }
    
    public boolean isColocalizesWith() {
        if (null != text && text.equalsIgnoreCase(QUALIFIER_COLOCALIZES_WITH)) {
            return true;
        }
        return false;        
    }
    public boolean isContributesTo() {
        if (null != text && text.equalsIgnoreCase(QUALIFIER_CONTRIBUTES_TO)) {
            return true;
        }
        return false;        
    }    
    
    public boolean equals(Qualifier q) {
        if (this.qualifierId != null && this.qualifierId.equals(q.qualifierId) ||
            this.text != null && this.text.equals(q.text)) {
            return true;
        }
        return false;
    }
    
    
    public Qualifier makeCopy() {
        Qualifier newQualifier = new Qualifier();
        newQualifier.text = this.text;
        return newQualifier;
    }
    
}
