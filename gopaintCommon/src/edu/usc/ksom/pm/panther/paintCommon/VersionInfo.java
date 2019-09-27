/**
 * Copyright 2019 University Of Southern California
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


public class VersionInfo implements Serializable {
    
    public static final String COMP_OBJ_IS_NULL = "No Comparision information";
    public static final String COMP_OBJ_DOES_NOT_HAVE_VERSION_ID = "Comparision information does not have version id";
    public static final String COMP_OBJ_DOES_NOT_HAVE_NAME = "Comparision information does not have name";
    public static final String COMP_OBJ_DOES_NOT_HAVE_RELEASE_DATE = "Comparision information does not have release date";
    public static final String REF_OBJ_DOES_NOT_HAVE_VERSION_ID = "Reference information does not have version id";
    public static final String REF_OBJ_DOES_NOT_HAVE_NAME = "Reference information does not have name";
    public static final String REF_OBJ_DOES_NOT_HAVE_RELEASE_DATE = "Reference information does not have release date"; 
    public static final String INFO_DIFF_FOR_VERSION_ID = "Information different for version id";
    public static final String INFO_DIFF_FOR_NAME = "Information different for name";
    public static final String INFO_DIFF_FOR_RELEASE_DATE = "Information different for date";    

    
    private String versionId;
    private String name;
    private String releaseDate;

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    
    public String compareTo(VersionInfo compInfo) {
        if (null == compInfo) {
            return COMP_OBJ_IS_NULL;
        }
        if (null != versionId && null == compInfo.versionId) {
            return COMP_OBJ_DOES_NOT_HAVE_VERSION_ID;
        }
        if (null != name && null == compInfo.name) {
            return COMP_OBJ_DOES_NOT_HAVE_NAME;
        }
        if (null != releaseDate && null == compInfo.releaseDate) {
            return COMP_OBJ_DOES_NOT_HAVE_RELEASE_DATE;
        }
        if (null == versionId && null != compInfo.versionId) {
            return REF_OBJ_DOES_NOT_HAVE_VERSION_ID;
        }
        if (null == name && null != compInfo.name) {
            return REF_OBJ_DOES_NOT_HAVE_NAME;
        }
        if (null == releaseDate && null != compInfo.releaseDate) {
            return REF_OBJ_DOES_NOT_HAVE_RELEASE_DATE;
        }
        if (null != versionId && null != compInfo.versionId && false == versionId.equals(compInfo.versionId)) {
            return INFO_DIFF_FOR_VERSION_ID;
        }
        if (null != name && null != compInfo.name && false == name.equals(compInfo.name)) {
            return INFO_DIFF_FOR_NAME;
        }
        if (null != releaseDate && null != compInfo.releaseDate && false == releaseDate.equals(compInfo.releaseDate)) {
            return INFO_DIFF_FOR_RELEASE_DATE;
        }
        return null;
    }
    
}
