/* Copyright (C) 2008 SRI International
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
package com.sri.panther.paintCommon;

import java.io.Serializable;

public class Book implements Comparable, Serializable {
    protected String id;
    protected String name;
    protected int curationStatus;
    protected User lockedBy;
    
    // A book can have multiple status.  Instead of using a hashtable or vector which will use up lots of memory when users retrieve
    // books from searches, use binary bits for each status.
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
    
    public static final int[] availableStatuses = {CURATION_STATUS_NOT_CURATED, CURATION_STATUS_AUTOMATICALLY_CURATED, 
    CURATION_STATUS_MANUALLY_CURATED, CURATION_STATUS_CURATION_REVIEWED, CURATION_STATUS_QAED, CURATION_STATUS_CHECKED_OUT, CURATION_STATUS_PARTIALLY_CURATED, CURATION_STATUS_UNKNOWN};
    
    public static final String LABEL_CURATION_STATUS_NOT_CURATED = "Not Curated";
    public static final String LABEL_CURATION_STATUS_AUTOMATICALLY_CURATED = "Automatically Curated";
    public static final String LABEL_CURATION_STATUS_MANUALLY_CURATED = "Manually Curated";
    public static final String LABEL_CURATION_STATUS_CURATION_REVIEWED = "Curation Reviewed";
    public static final String LABEL_CURATION_STATUS_QAED = "Curation QAed";
    public static final String LABEL_CURATION_STATUS_CHECKED_OUT = "Locked";
    public static final String LABEL_CURATION_STATUS_PARTIALLY_CURATED = "Partially Curated";
    public static final String LABEL_CURATION_STATUS_UNKNOWN = "Unknown";
    
    
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
        return sb.toString();
        
    }
    
    
    
    public boolean hasStatus(int compStatus) {
        if (0 != (curationStatus & compStatus)) {
            return true;
        }
        return false;
    }



}
