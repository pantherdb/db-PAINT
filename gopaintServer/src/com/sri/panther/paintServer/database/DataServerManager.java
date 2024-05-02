 /**
 * Copyright 2023 University Of Southern California
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
package com.sri.panther.paintServer.database;

import com.sri.panther.paintServer.util.ConfigFile;

import java.util.Hashtable;


public class DataServerManager {

    private static final String DEFAULT_DB_SOURCE = "db.jdbc.dbsid";
    private static final String MSG_REQUESTING_MSG =
        "Requesting data from database:  ";


    private static Hashtable dBSchemaToDataSrvr;
    static {
        createDataServers();
    }

    private static synchronized void createDataServers() {
        if (null != dBSchemaToDataSrvr) {
            return;
        }
        dBSchemaToDataSrvr = new Hashtable();


        String defaultDb = ConfigFile.getProperty(DEFAULT_DB_SOURCE);

        dBSchemaToDataSrvr.put(defaultDb, new DataServer(defaultDb));
    }

    public static DataServer getDataServer() {
        return (DataServer)dBSchemaToDataSrvr.get(ConfigFile.getProperty(DEFAULT_DB_SOURCE));
    }

    public static DataServer getDataServer(String db) {
        System.out.println(MSG_REQUESTING_MSG + db);
        return (DataServer)dBSchemaToDataSrvr.get(db);
    }
    
    public static void closeConnectionPool() {
        DBConnectionPool.getInstance().closeConnectionPool(ConfigFile.getProperty(DEFAULT_DB_SOURCE));
    }    
}
