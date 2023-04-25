/**
 * Copyright 2022 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.usc.ksom.pm.panther.paintCommon;

import java.io.Serializable;


public class Domain implements Serializable {
    private String proteinId;
    private int start;
    private int end;
    private String hmmAcc;
    private String hmmName;
    public static final String URL_PFAM_DOMAIN_PREFIX = "https://www.ebi.ac.uk/interpro/entry/pfam/";
    
    public static String getPFAMDomainUrl(String hmmAcc) {
        return URL_PFAM_DOMAIN_PREFIX + hmmAcc;
    }
    
    public Domain(String proteinId, int start, int end, String hmmAcc, String hmmName) {
        this.proteinId = proteinId;
        this.start = start;
        this.end = end;
        this.hmmAcc = hmmAcc;
        this.hmmName = hmmName;
    }

    public String getProteinId() {
        return proteinId;
    }

    public void setProteinId(String proteinId) {
        this.proteinId = proteinId;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getHmmAcc() {
        return hmmAcc;
    }

    public void setHmmAcc(String hmmAcc) {
        this.hmmAcc = hmmAcc;
    }

    public String getHmmName() {
        return hmmName;
    }

    public void setHmmName(String hmmName) {
        this.hmmName = hmmName;
    }
    
    
    
}
