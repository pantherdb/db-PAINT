 /* Copyright (C) 2009 SRI International
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.sri.panther.paintServer.util.ConfigFile;
import com.sri.panther.paintCommon.util.Utils;

import org.apache.commons.dbcp.BasicDataSource;


public class DBConnectionPool{
    private static final Logger logger = Logger.getLogger("com.sri.panther.passwordServer.database.DBConnectionPool");
    private static final String KEY_DB_JDBC_URL = "jdbc.url";
    private static final String KEY_DB_JDBC_USERNAME = "jdbc.username";
    private static final String KEY_DB_JDBC_PASSWORD = "jdbc.password";
    private static final String KEY_DB_CONPOOL_MINSIZE = "connectionpool.minsize";
    private static final String KEY_DB_CONPOOL_MAXSIZE = "connectionpool.maxsize";
//    private static final String VALIDATION_ORACLE = "select 1 from dual";
        private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

    private static Hashtable connectionLookup = new Hashtable();


    // use static to make sure there is only one copy of the connection pool object.
    static{
      init();
    }





	private static boolean init(){

            String dbListStr = ConfigFile.getProperty("db.jdbc.connectionpool");
            if (null == dbListStr) {
                    return false;
            }

            // Get lists and determine unique db names
            Hashtable<String, String> dbNameTable = new Hashtable<String, String>();
            String dbList[] = Utils.tokenize(dbListStr, ",");
            for (int i = 0; i < dbList.length; i++) {
                    String listStr = ConfigFile.getProperty(dbList[i]);
                    // get databases specified by list
                    String list[] = Utils.tokenize(listStr, ",");
                    for (int j  = 0; j < list.length; j++) {
                            dbNameTable.put(list[j], list[j]);
                    }
            }
            // Create connection pool for each database
            Enumeration<String> dbEnum = dbNameTable.keys();
            while (dbEnum.hasMoreElements()) {
                    createConnectionPool((String)dbEnum.nextElement());
            }
            return true;


	}

    private static boolean createConnectionPool(String dbsid) {
        BasicDataSource dataSource = new BasicDataSource();
        // set properties
        dataSource.setDriverClassName(DRIVER_CLASS_NAME);
        dataSource.setUrl(ConfigFile.getProperty(dbsid + "." + 
                                                 DBConnectionPool.KEY_DB_JDBC_URL));
        dataSource.setUsername(ConfigFile.getProperty(dbsid + "." + 
                                                      DBConnectionPool.KEY_DB_JDBC_USERNAME));
        dataSource.setPassword(ConfigFile.getProperty(dbsid + "." + 
                                                      DBConnectionPool.KEY_DB_JDBC_PASSWORD));

        System.out.println("Database URL: " + 
                           ConfigFile.getProperty(dbsid + "." + 
                                                  DBConnectionPool.KEY_DB_JDBC_URL) + 
                           " Schema:  " + 
                           ConfigFile.getProperty(dbsid + "." + DBConnectionPool.KEY_DB_JDBC_USERNAME));


        // set pool size
        int poolMin = 
            Integer.parseInt(ConfigFile.getProperty(dbsid + "." + DBConnectionPool.KEY_DB_CONPOOL_MINSIZE));
        int poolMax = 
            Integer.parseInt(ConfigFile.getProperty(dbsid + "." + DBConnectionPool.KEY_DB_CONPOOL_MAXSIZE));
        dataSource.setMinIdle(poolMin);
        dataSource.setMaxActive(poolMax);
//        dataSource.setValidationQuery(VALIDATION_ORACLE);
//        dataSource.setTestOnBorrow(true);
        connectionLookup.put(dbsid, dataSource);

        return true;

    }




    
    /**
     * Gets a logical connection to the database from the pool.
     *
     * @returns a logical connection to the database
     * @exception SQLException  If a SQL exception occurs
     */
     /**
      * Gets a logical connection to the database from the pool.
      *
      * @return a logical connection to the database
      * @exception SQLException  If a SQL exception occurs
      */
      public static Connection getConnection(String dbsid) throws SQLException{
        Connection con = null;
        try{
          // get the connection from the connection pool.
          BasicDataSource dataSource = (BasicDataSource)connectionLookup.get(dbsid);
          if (null == dataSource) {
              return null;
          }
          con = dataSource.getConnection();
        }
        catch (SQLException e){
          logger.error("SQLException: " + e.getMessage());
          if (null == con)  {
              try {
                  System.out.println("Attempt to re-establish connection");
                  getConnection(dbsid);
                  BasicDataSource dataSource = (BasicDataSource)connectionLookup.get(dbsid);
                  if (null == dataSource) {
                     return null;
                  }
                  return dataSource.getConnection();
             }
            catch (SQLException exp){
               logger.error("SQLException: on attempt to re-establish connection" + e.getMessage());
               exp.printStackTrace();
            }
          }
        }
        return con;
      }


	public static void main(String[] args) {
		try {
			DBConnectionPool.getConnection("dev_3_panther_upl");
		}
		catch (Exception e) {
			System.out.println("Exception " + e.getMessage() + " has been returned.");     
		}
	}
}

