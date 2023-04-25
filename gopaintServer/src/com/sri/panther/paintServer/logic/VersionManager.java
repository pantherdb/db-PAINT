/**
 * Copyright 2021 University Of Southern California
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
package com.sri.panther.paintServer.logic;

import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.datamodel.ClassificationVersion;
import com.sri.panther.paintServer.datamodel.FullGOAnnotVersion;
import com.sri.panther.paintServer.datamodel.PantherVersion;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;


public class VersionManager {
    private static VersionManager inst;
    private static ClassificationVersion cv;
    private static FullGOAnnotVersion fgav;
    private static PantherVersion pv;
    private static DataIO dataIO = DataAccessManager.getInstance().getDataIO();
    
    private VersionManager() {
        
    }
    
    public static synchronized VersionManager getInstance() {
        if (null != inst) {
            return inst;
        }        
        inst = new VersionManager();
        initClsVersion();
        initFullGOVersion();
        initPantherersion();
        return inst;
    }
    
    public ClassificationVersion getClsVersion() {
        return cv;
    }
    
    public FullGOAnnotVersion getFullGOAnnotVersion() {
        return fgav;
    }
    
    public PantherVersion getPantherVersion() {
        return pv;
    }
    
    private static ClassificationVersion initClsVersion() {       
        cv = dataIO.getCurVersionInfo();
        return cv;
    }
    
    private static FullGOAnnotVersion initFullGOVersion() {
        fgav = dataIO.getFullGOVersion();
        return fgav;
    }
    
    private static PantherVersion initPantherersion() {
        pv = dataIO.getPantherVersion();
        return pv;
    }
}
