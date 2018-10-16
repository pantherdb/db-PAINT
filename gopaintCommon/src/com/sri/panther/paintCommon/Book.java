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
package com.sri.panther.paintCommon;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

public class Book implements Comparable, Serializable {
    protected String id;
    protected String name;
    protected int curationStatus;
    protected Date curationStatusUpdateDate;        // Does not include check out date
    protected User lockedBy;
    protected int numLeaves;
    protected HashSet<String> orgSet;
    
    // A book can have multiple status.  Instead of using a hashtable or vector which will use up lots of memory when users retrieve
    // books from searches, use binary bits for each status.  Most importantly, this makes it easier to check for status
    //Note:  To add curation status, convert the number into binary and ensure that
    //       the bit is not already used.  
    public static final int CURATION_STATUS_NOT_CURATED = 0x01;
    public static final int CURATION_STATUS_AUTOMATICALLY_CURATED = 0x02;
    public static final int CURATION_STATUS_MANUALLY_CURATED = 0x04;
    public static final int CURATION_STATUS_CURATION_REVIEWED = 0x08;
    public static final int CURATION_STATUS_QAED = 0x10;                // 16
    public static final int CURATION_STATUS_CHECKED_OUT = 0x20;         // 32
    public static final int CURATION_STATUS_PARTIALLY_CURATED = 0x40;   // 64
    public static final int CURATION_STATUS_UNKNOWN = 0x80;             // 128
    public static final int CURATION_STATUS_REQUIRE_PAINT_REVIEW = 0x100;   // 256    
    
    public static final int[] availableStatuses = {CURATION_STATUS_NOT_CURATED, CURATION_STATUS_AUTOMATICALLY_CURATED, 
    CURATION_STATUS_MANUALLY_CURATED, CURATION_STATUS_CURATION_REVIEWED, CURATION_STATUS_QAED, CURATION_STATUS_CHECKED_OUT, CURATION_STATUS_PARTIALLY_CURATED, CURATION_STATUS_UNKNOWN, CURATION_STATUS_REQUIRE_PAINT_REVIEW};
    
    public static final String LABEL_CURATION_STATUS_NOT_CURATED = "Not Curated";
    public static final String LABEL_CURATION_STATUS_AUTOMATICALLY_CURATED = "Automatically Curated";
    public static final String LABEL_CURATION_STATUS_MANUALLY_CURATED = "Manually Curated";
    public static final String LABEL_CURATION_STATUS_CURATION_REVIEWED = "Curation Reviewed";
    public static final String LABEL_CURATION_STATUS_QAED = "Curation QAed";
    public static final String LABEL_CURATION_STATUS_CHECKED_OUT = "Locked";
    public static final String LABEL_CURATION_STATUS_PARTIALLY_CURATED = "Partially Curated";
    public static final String LABEL_CURATION_STATUS_UNKNOWN = "Unknown";
    public static final String LABEL_CURATION_STATUS_REQUIRE_PAINT_REVIEW = "Require PAINT review";    
    
    
    public Book(String id, String name, int curationStatus, User lockedBy) {
        this.id = id;
        this.name = name;
        this.curationStatus = curationStatus;
        this.lockedBy = lockedBy;
    
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getCurationStatus() {
        return curationStatus;
    }
    
    public void setCurationStatus(int curationStatus) {
        this.curationStatus = curationStatus;
    }
    
    public User getLockedBy() {
        return lockedBy;
    }
    
    public void setLockedBy(User lockedBy) {
        this.lockedBy = lockedBy;
    }
    
    
    public int compareTo(Object o) {
        Book comp = (Book)o;
        return id.compareTo(comp.id);
    }
    
    public static String getCurationStatusString(int status) {
        StringBuffer sb = new StringBuffer();
        
        if (0 != (status & CURATION_STATUS_NOT_CURATED)) {
            sb.append(LABEL_CURATION_STATUS_NOT_CURATED);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }


        if (0 != (status & CURATION_STATUS_AUTOMATICALLY_CURATED)) {
            sb.append(LABEL_CURATION_STATUS_AUTOMATICALLY_CURATED);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }


        if (0 != (status & CURATION_STATUS_MANUALLY_CURATED)) {
            sb.append(LABEL_CURATION_STATUS_MANUALLY_CURATED);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }


        if (0 != (status & CURATION_STATUS_CURATION_REVIEWED)) {
            sb.append(LABEL_CURATION_STATUS_CURATION_REVIEWED);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }


        if (0 != (status & CURATION_STATUS_QAED)) {
            sb.append(LABEL_CURATION_STATUS_QAED);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }


        if (0 != (status & CURATION_STATUS_CHECKED_OUT)) {
            sb.append(LABEL_CURATION_STATUS_CHECKED_OUT);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }

        if (0 != (status & CURATION_STATUS_PARTIALLY_CURATED)) {
            sb.append(LABEL_CURATION_STATUS_PARTIALLY_CURATED);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }

        if (0 != (status & CURATION_STATUS_UNKNOWN)) {
            sb.append(LABEL_CURATION_STATUS_UNKNOWN);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }
        
        if (0 != (status & CURATION_STATUS_REQUIRE_PAINT_REVIEW)) {
            sb.append(LABEL_CURATION_STATUS_REQUIRE_PAINT_REVIEW);
            sb.append(Constant.STR_COMMA);
            sb.append(Constant.STR_SPACE);
        }        
        return sb.toString();
        
    }
        
    public boolean hasStatus(int compStatus) {
        if (0 != (curationStatus & compStatus)) {
            return true;
        }
        return false;
    }

    public Date getCurationStatusUpdateDate() {
        return curationStatusUpdateDate;
    }

    public void setCurationStatusUpdateDate(Date curationStatusUpdateDate) {
        this.curationStatusUpdateDate = curationStatusUpdateDate;
    }

    public int getNumLeaves() {
        return numLeaves;
    }

    public void setNumLeaves(int numLeaves) {
        this.numLeaves = numLeaves;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<String> getOrgSet() {
        return orgSet;
    }

    public void setOrgSet(HashSet<String> orgSet) {
        this.orgSet = orgSet;
    }

}
