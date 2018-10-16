/**
 * Copyright 2018 University Of Southern California
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
package com.sri.panther.paintServer.datamodel;

import java.util.HashSet;




public class NodeAnnotation {
    
    private HashSet<String> mfAnnots;
    private HashSet<String> bpAnnots;
    private HashSet<String> ccAnnots;
    
    public void addMfAnnot(String mfAnnot) {
        if (null == mfAnnot) {
            return;
        }
        if (null == mfAnnots) {
            mfAnnots = new HashSet<String>();
        }
        mfAnnots.add(mfAnnot);
    }

    public void addBpAnnot(String bpAnnot) {
        if (null == bpAnnot) {
            return;
        }
        if (null == bpAnnots) {
            bpAnnots = new HashSet<String>();
        }
        bpAnnots.add(bpAnnot);
    }    
    
    public void addCcAnnot(String ccAnnot) {
        if (null == ccAnnot) {
            return;
        }
        if (null == ccAnnots) {
            ccAnnots = new HashSet<String>();
        }
        ccAnnots.add(ccAnnot);
    }

    public HashSet<String> getMfAnnots() {
        return mfAnnots;
    }

    public void setMfAnnots(HashSet<String> mfAnnots) {
        this.mfAnnots = mfAnnots;
    }

    public HashSet<String> getBpAnnots() {
        return bpAnnots;
    }

    public void setBpAnnots(HashSet<String> bpAnnots) {
        this.bpAnnots = bpAnnots;
    }

    public HashSet<String> getCcAnnots() {
        return ccAnnots;
    }

    public void setCcAnnots(HashSet<String> ccAnnots) {
        this.ccAnnots = ccAnnots;
    }
    
    public int getMfCount() {
        if (null == mfAnnots) {
            return 0;
        }
        return mfAnnots.size();
    }

    public int getBpCount() {
        if (null == bpAnnots) {
            return 0;
        }
        return bpAnnots.size();
    }
    
    public int getCcCount() {
        if (null == ccAnnots) {
            return 0;
        }
        return ccAnnots.size();
    }
    
    public int getTotalAnnotCounts() {
        return getMfCount() + getBpCount() + getCcCount();
    }
    
}
