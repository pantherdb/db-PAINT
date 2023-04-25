/**
 *  Copyright 2021 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintServer.logic;

import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.util.ConfigFile;


public class DataAccessManager {
    private static DataAccessManager instance;
    private static DataIO dataIO;
    
    private DataAccessManager() {
        dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        System.out.println("Initialized data access manager");        
    }
    
    public static synchronized DataAccessManager getInstance() {
        if (null != instance) {
            return instance;
        }
        instance = new DataAccessManager();
        return instance;
    }
    
    public DataIO getDataIO() {
        return dataIO;
    }
}
