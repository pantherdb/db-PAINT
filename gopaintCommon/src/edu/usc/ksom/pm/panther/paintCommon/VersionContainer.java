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
import java.util.HashMap;


public class VersionContainer implements Serializable{

    public static final String COMP_OBJ_IS_NULL = "No Comparision information";
    public static final String COMP_OBJ_HAS_FEWER_ENTRIES = "Comparision information has fewer entries";
    public static final String COMP_OBJ_HAS_MORE_ENTRIES = "Comparision information has more entries";
    public static final String COMP_OBJ_DOES_NOT_HAVE_ENTRY_FOR = "Comparision information does not have version information for ";
    public static final String REF_OBJ_HAS_ENTRY_FOR = "Reference information does not have version information for ";
    public static final String DOWNLOAD_NEW_VERSION = "Download new version from server, ";    
    public static final String COMP_OBJ_HAS_DIFFERENT_VERSION_INFORMATION_FOR = "Download new version from server, Found different version information for ";
    
    public static final String NOT_FOUND = " not found";
    public static final VersionedObj serverCompObjs[] = {VersionedObj.CLIENT_SERVER_COMMON_VERSION, VersionedObj.FULL_GO_VERSION, VersionedObj.PANTHER_VERSION, VersionedObj.CLS_VERSION};    

    public enum VersionedObj {
        CLIENT_VERSION,
        CLIENT_SERVER_COMMON_VERSION,
        SERVER_VERSION,
        CLS_VERSION,
        FULL_GO_VERSION,
        PANTHER_VERSION;

        public String toString() {
            return super.toString().toLowerCase();
        }
    }
    
    HashMap<VersionedObj, VersionInfo> versionLookup = new HashMap<VersionedObj, VersionInfo>();
    
    public boolean addInfo(VersionedObj vo, VersionInfo vi) {
        if (null == vo || null == vi) {
            return false;
        }
        versionLookup.put(vo, vi);
        return true;
    }
    
    public VersionInfo get(VersionedObj vo) {
        if (null == vo) {
            return null;
        }
        return versionLookup.get(vo);
    }

    
    public String compareTo(Object o) {
        if (null == o) {
            return COMP_OBJ_IS_NULL;
        }
        VersionContainer comp = (VersionContainer)o;
        int curSize = versionLookup.size();
        int compSize = comp.versionLookup.size();
        if (curSize > compSize) {
            return COMP_OBJ_HAS_FEWER_ENTRIES;
        }
        if (curSize < compSize) {
            return COMP_OBJ_HAS_MORE_ENTRIES;
        }
        // They are both the same size.  Compare contents of version information
        for (VersionedObj vo: versionLookup.keySet()) {
            VersionInfo vi = versionLookup.get(vo);
            VersionInfo compObj = comp.versionLookup.get(vo);
            if (null == compObj) {
                return COMP_OBJ_DOES_NOT_HAVE_ENTRY_FOR + vo.name();
            }
            String compInfo = vi.compareTo(compObj);
            if (null != compInfo) {
                return vo.name() + " " + compInfo;
            }
        }
        return null;
    }
    

    public String compareForServerOps(VersionContainer comp) {
        for (VersionedObj vo: serverCompObjs) {
            String compInfo = compareVersoinedObj(comp, vo);
            if (null != compInfo) {
                return compInfo;
            }
        }
        return null;
    
    }
    
    public String compareVersoinedObj(VersionContainer comp, VersionedObj vo) {
        VersionInfo versionObj = versionLookup.get(vo);
        if (null == versionObj) {
            return DOWNLOAD_NEW_VERSION + vo.name() + NOT_FOUND;
        }
        if (null != versionObj && null != versionObj.compareTo(comp.get(vo))) {
            return COMP_OBJ_HAS_DIFFERENT_VERSION_INFORMATION_FOR + vo.name();
        }
        return null;
    }
    
    public static boolean VersionContainersEqual(VersionContainer vc1, VersionContainer vc2) {
        if (null == vc1 && null == vc2) {
            return true;
        }
        if ((null == vc1 && null != vc2) || (null != vc1 && null == vc2)) {
            return false;
        }
        if (vc1.equals(vc2)) {
            return true;
        }
        return false;
    }    
}
