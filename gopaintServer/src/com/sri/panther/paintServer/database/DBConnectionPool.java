/**
 * Copyright 2024 University Of Southern California
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.sri.panther.paintServer.util.ConfigFile;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.util.ReleaseResources;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbcp2.BasicDataSource;

public class DBConnectionPool {
    private static final java.text.SimpleDateFormat DATE_FORMATTER = new java.text.SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
    private static final Logger logger = Logger.getLogger("com.sri.panther.paintServer.database.DBConnectionPool");
    private static final String KEY_DB_JDBC_URL = "jdbc.url";
    private static final String KEY_DB_JDBC_USERNAME = "jdbc.username";
    private static final String KEY_DB_JDBC_PASSWORD = "jdbc.password";
    private static final String KEY_DB_CONPOOL_MINSIZE = "connectionpool.minsize";
    private static final String KEY_DB_CONPOOL_MAXSIZE = "connectionpool.maxsize";
//    private static final String VALIDATION_ORACLE = "select 1 from dual";
    private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";
    private static final String QUERY_VALIDATION = "select 1 from dual";
    private static int TIMEOUT_ABANDONED = 300;
    private static final long TIME_BETWEEN_EVICTION_RUN_IN_MILLIS = TimeUnit.MINUTES.toMillis(5);       //Check idle connections every 5 minutes    

    private static Hashtable<String, BasicDataSource> connectionLookup = new Hashtable<String, BasicDataSource>();
    private static DBConnectionPool instance;
    private static boolean debug = false;

    private DBConnectionPool() {

    }

    public static synchronized DBConnectionPool getInstance() {
        if (null != instance) {
            return instance;
        }
        init();
        instance = new DBConnectionPool();
        return instance;
    }

    private static void init() {
        String dbListStr = ConfigFile.getProperty("db.jdbc.connectionpool");
        if (null == dbListStr) {
            System.out.println("Unable to find property " + dbListStr);
            return;
        }

        // Get lists and determine unique db names
        Hashtable<String, String> dbNameTable = new Hashtable<String, String>();
        String dbList[] = Utils.tokenize(dbListStr, ",");
        for (int i = 0; i < dbList.length; i++) {
            String listStr = ConfigFile.getProperty(dbList[i]);
            // get databases specified by list
            String list[] = Utils.tokenize(listStr, ",");
            for (int j = 0; j < list.length; j++) {
                dbNameTable.put(list[j], list[j]);
            }
        }
        // Create connection pool for each database
        Enumeration<String> dbEnum = dbNameTable.keys();
        while (dbEnum.hasMoreElements()) {
            createConnectionPool((String) dbEnum.nextElement());
        }
    }

    private static synchronized void createConnectionPool(String dbsid) {
        BasicDataSource dataSource = new BasicDataSource();
        // set properties
        dataSource.setDriverClassName(DRIVER_CLASS_NAME);
        dataSource.setUrl(ConfigFile.getProperty(dbsid + "."
                + DBConnectionPool.KEY_DB_JDBC_URL));
        dataSource.setUsername(ConfigFile.getProperty(dbsid + "."
                + DBConnectionPool.KEY_DB_JDBC_USERNAME));
        dataSource.setPassword(ConfigFile.getProperty(dbsid + "."
                + DBConnectionPool.KEY_DB_JDBC_PASSWORD));

        System.out.println("Database URL: "
                + ConfigFile.getProperty(dbsid + "."
                        + DBConnectionPool.KEY_DB_JDBC_URL)
                + " Schema:  "
                + ConfigFile.getProperty(dbsid + "." + DBConnectionPool.KEY_DB_JDBC_USERNAME));

        // set pool size
        int poolMin
                = Integer.parseInt(ConfigFile.getProperty(dbsid + "." + DBConnectionPool.KEY_DB_CONPOOL_MINSIZE));
        int poolMax
                = Integer.parseInt(ConfigFile.getProperty(dbsid + "." + DBConnectionPool.KEY_DB_CONPOOL_MAXSIZE));
        dataSource.setMinIdle(poolMin);
        dataSource.setMaxTotal(poolMax);
        dataSource.setValidationQuery(QUERY_VALIDATION);
        dataSource.setTestOnBorrow(true);
        dataSource.setRemoveAbandonedOnBorrow(true);
        dataSource.setRemoveAbandonedTimeout(TIMEOUT_ABANDONED);
        dataSource.setAbandonedUsageTracking(true);
        dataSource.setLogAbandoned(true);
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(TIME_BETWEEN_EVICTION_RUN_IN_MILLIS);
        dataSource.setLifo(false);          //Force system to use idle connections so that they are validated always
        connectionLookup.put(dbsid, dataSource);
        System.out.println("Finished initializing database connection pool for " + dbsid);
    }

    /**
     * Gets a logical connection to the database from the pool.
     *
     * @returns a logical connection to the database
     * @exception SQLException If a SQL exception occurs
     */
    public synchronized Connection getConnection(String dbsid) throws SQLException {
        if (true == debug) {
            System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Going to request connection for " + dbsid);
        }
        if (null == dbsid) {
            System.out.println("Requesting connection for null dbsid");
            Exception e = new Exception();
            e.printStackTrace();
            return null;
        }
        Connection con = null;
        BasicDataSource dataSource = null;
        try {
            // get the connection from the connection pool.
            dataSource = (BasicDataSource) connectionLookup.get(dbsid);
            if (null == dataSource) {
                System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Datasource for " +  dbsid + " is null");
                return null;
            }
            if (true == debug) {
                System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Lookup table contains entries. Going to request connection");
            }
            con = dataSource.getConnection();
            if (null == con) {
                System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Connection pool has returned null connection");
            }
            else {
                if (true == debug) {
                    System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Non-null Connection has been returned from pool");
                }
            }
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
            System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " SQLException: " + e.getMessage()); 
            e.printStackTrace();
        }
        finally {
            if (null == con) {
                System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Attempt to re-establish connection.  First close connection pool");
                closeConnectionPool(dbsid);
                System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Creating connection pool again");            
                createConnectionPool(dbsid);
                con = dataSource.getConnection();
                if (null == con) {
                    System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Connection is null after re-establishing connection pool");
                }
            }
        }
        if (null != dataSource) {
            if (true == debug) {
                System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " After connection allocation, there are " + dataSource.getNumActive() + " connections that are active");
            }
        } else {
            System.out.println("Unable to get count of active connections\n");
        }
        return con;
    }    
    /**
     * Gets a logical connection to the database from the pool.
     *
     * @return a logical connection to the database
     * @exception SQLException If a SQL exception occurs
     */
    public Connection getConnectionOrig(String dbsid) throws SQLException {
        if (null == dbsid) {
            return null;
        }
        Connection con = null;
        BasicDataSource dataSource = null;
        try {
            // get the connection from the connection pool.
            dataSource = (BasicDataSource) connectionLookup.get(dbsid);
            if (null == dataSource) {
                return null;
            }
            //System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " Before allocation, there are " + dataSource.getNumActive() + " connections that are active caller class name is " + getCallerClassName().toString());
            System.out.println(new Date() + " Going to retrieve connection from connection pool");
            con = dataSource.getConnection();
            System.out.println(new Date() + " Connection pool has returned a connection");            
            if (null == con) {
            System.out.println(new Date() + " Connection pool has returned a null connection"); 
                return con;
            }
            System.out.println(new Date() + " Connection pool has returned a non-null connection"); 
        } catch (SQLException e) {
            logger.error("SQLException: " + e.getMessage());
            ReleaseResources.releaseDBResources(null, null, con);
            System.out.println("Attempt to re-establish connection.  First close connection pool");
            closeConnectionPool(dbsid);
            System.out.println("Creating connection pool again");            
            createConnectionPool(dbsid);
            con = dataSource.getConnection();
        }
        if (null != dataSource) {
            System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " After allocation, there are " + dataSource.getNumActive() + " connections that are active");
        } else {
            System.out.println("Unable to get count of active connections");
        }
        return con;
    }
    
    public static void printConnectionStatus() {
        for (Entry <String, BasicDataSource> conToDataSrc : connectionLookup.entrySet()) {
            System.out.println(DATE_FORMATTER.format(new java.util.Date(System.currentTimeMillis())) + " " + conToDataSrc.getKey() + " has " + conToDataSrc.getValue().getNumActive() + " active connections.");
        }
    }
    

    public StringBuffer getCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < Math.min(stElements.length, 6); i++) {
            StackTraceElement ste = stElements[i];
            //if (!ste.getClassName().equals(this.getClass().getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                sb.append(ste.getClassName() + "-" + ste.getLineNumber() + " ");
            //}
        }
        return sb;
    }
    
    public static void closeConnectionPool(String dbSid) {
        BasicDataSource dataSource = (BasicDataSource) connectionLookup.get(dbSid);
        if (null == dataSource) {
            return;
        }
        try {
            dataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }    
  

    public static void main(String[] args) {
        try {
            DBConnectionPool.getInstance().getConnection("dev_3_panther_upl");
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage() + " has been returned.");
        }
    }
}
