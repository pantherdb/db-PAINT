/**
 * Copyright 2016 University Of Southern California
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
import com.sri.panther.paintServer.util.ConfigFile;

/**
 *
 * @author muruganu
 */
public class VersionManager {
    private static VersionManager inst;
    private static ClassificationVersion cv;
    
    private VersionManager() {
        
    }
    
    public static synchronized VersionManager getInstance() {
        if (null != inst) {
            return inst;
        }
        
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        cv = dataIO.getCurVersionInfo();
        inst = new VersionManager();
        return inst;
    }
    
    public ClassificationVersion getCurrentVersion() {
        return cv;
    }
    
}
