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
package com.sri.panther.paintServer.util;

import org.apache.log4j.Logger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Title:        public panther website
 * <p/>
 * Description:  This class is used to release database resources.
 * <p/>
 */
public class ReleaseResources {

    static Logger log = Logger.getLogger(ReleaseResources.class);

    /**
     * release database resources: release the resultset, statement and connection at one shot.
     *
     * @param rst  resultset to be closed
     * @param stmt statement to be closed
     * @param con  connection to be closed
     */
    public static void releaseDBResources(ResultSet rst, Statement stmt, Connection con) {

        // test and close the resultset
        try {
            if (rst != null) {
                rst.close();
            }
        } catch (SQLException e) {
            log.error("Error in closing the ResultSet " + e.getMessage());
        }

        // test and close the statement
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
            log.error("Error in closing the statement " + e.getMessage());
        }

        // test and close the database connection
        try {
            if (con != null) {
                // close the logical connection.
                con.close();
            }
        } catch (SQLException e) {
            log.error("Error in closing the pooled connection " + e.getMessage());
        }
        rst = null;
        stmt = null;
        con = null;        
    }
}
