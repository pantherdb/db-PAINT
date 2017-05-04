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
}
