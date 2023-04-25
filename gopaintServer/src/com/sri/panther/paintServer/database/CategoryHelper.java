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
package com.sri.panther.paintServer.database;


import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.util.ConfigFile;
import com.sri.panther.paintServer.util.ReleaseResources;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import org.apache.log4j.Logger;

public class CategoryHelper {
    
    public static final String QUERY_CATEGORY_SINGLE_PARENT_GO_ACROSS_ASPECT = "select distinct c1.accession, c1.name, c1.definition, c1.classification_id child_id, c2.accession parent, c2.classification_id parent_id, r.rank, c1.term_type_sid, c2.term_type_sid,  (case when ctt1.term_name='molecular_function' THEN 'F' when ctt1.term_name='cellular_component'THEN 'C' when ctt1.term_name='biological_process' THEN 'P' END) as cAspect, (case when ctt2.term_name='molecular_function' THEN 'F' when ctt2.term_name='cellular_component'THEN 'C' when ctt2.term_name='biological_process' THEN 'P' END) as pAspect from go_classification c1, go_classification_relationship r, go_classification c2,  classification_term_type ctt1, classification_term_type ctt2 where c1.depth is null  and c1.classification_version_sid = %1 and c1.obsolescence_date is null and c1.classification_id = r.child_classification_id and r.parent_classification_id = c2.classification_id and r.obsolescence_date is null and c2.classification_version_sid = %1 and c2.obsolescence_date is null   and c1.term_type_sid = ctt1.term_type_sid and c2.term_type_sid = ctt2.term_type_sid ";
    
    // Update to only find relationships where the aspects are same
    public static final String QUERY_CATEGORY_SINGLE_PARENT_GO = "select distinct c1.accession, c1.name, c1.definition, c1.classification_id child_id, c2.accession parent, c2.classification_id parent_id, r.rank, c1.term_type_sid, c2.term_type_sid,  (case when ctt1.term_name='molecular_function' THEN 'F' when ctt1.term_name='cellular_component'THEN 'C' when ctt1.term_name='biological_process' THEN 'P' END) as cAspect, (case when ctt2.term_name='molecular_function' THEN 'F' when ctt2.term_name='cellular_component'THEN 'C' when ctt2.term_name='biological_process' THEN 'P' END) as pAspect from go_classification c1, go_classification_relationship r, go_classification c2,  classification_term_type ctt1, classification_term_type ctt2 where c1.depth is null  and c1.classification_version_sid = %1 and c1.obsolescence_date is null and c1.classification_id = r.child_classification_id and r.parent_classification_id = c2.classification_id and r.obsolescence_date is null and c2.classification_version_sid = %1 and c2.obsolescence_date is null and c1.term_type_sid = c2.term_type_sid  and c1.term_type_sid = ctt1.term_type_sid and c2.term_type_sid = ctt2.term_type_sid ";

    public static final String QUERY_CATEGORY_ACC_GO = "select accession, name, depth, definition, term_type_sid, classification_id from go_classification where accession = '%1' and obsolescence_date is null and CLASSIFICATION_VERSION_SID = %2";
    
    
    
    protected static final String QUERY_VERSION_INFO = "select * from FULLGO_VERSION";
    protected static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    
    public static final String PARAM_1 = "%1";
    public static final String PARAM_2 = "%2";
    
    public static final String COLUMN_ACCESSION = "accession";
    public static final String COLUMN_ID = "classification_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DEFINITION = "definition";
    public static final String COLUMN_PARENT = "parent";
    public static final String COLUMN_RANK = "rank";
    public static final String COLUMN_DEPTH = "depth";
    public static final String COLUMN_ASPECT_CHILD = "cAspect";
    public static final String COLUMN_ASPECT_PARENT = "pAspect";
    public static final String COLUMN_CHILD_ID = "child_id";
    public static final String COLUMN_PARENT_ID = "parent_id";    



    public static final String COLUMN_GO_ANNOTATION_FORMAT_VERSION = "GO_ANNOTATION_FORMAT_VERSION";
    public static final String COLUMN_GO_ANNOTATION_RELEASE_DATE = "GO_ANNOTATION_RELEASE_DATE";
    public static final String COLUMN_PANTHER_VERSION = "PANTHER_VERSION";
    public static final String COLUMN_PANTHER_RELEASE_DATE = "PANTHER_RELEASE_DATE";    
    
    public static final String GO_CLS_VERSION_SID = ConfigFile.getProperty("go.cls.version_sid"); 
    public static final String DB_CONNECTION_STR = ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID);      
    public static final String STR_EMPTY = "";
    private static final Logger log = Logger.getLogger(CategoryHelper.class);
    
    public static HashMap<String, GOTerm> getGOTermLookup() {    
        String query = Utils.replace(QUERY_CATEGORY_SINGLE_PARENT_GO, PARAM_1, GO_CLS_VERSION_SID);
        //System.out.println(query);
        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;
        //List<GOTerm> catList = null;
        HashMap<String, GOTerm> clsToTermLookup = new HashMap<String, GOTerm>();
        try {
            con = DBConnectionPool.getInstance().getConnection(DB_CONNECTION_STR);
            stmt = con.createStatement();
            //catList = new ArrayList<GOTerm>();
            rst = stmt.executeQuery(query);            
            while (rst.next()) {
                String childAcc = rst.getString(COLUMN_ACCESSION);
//                if (true == "GO:0061631".equals(childAcc)) {
//                    System.out.println("Here");
//                }
                GOTerm child = clsToTermLookup.get(childAcc);
                if (null == child) {
                    child = new GOTerm();
                    clsToTermLookup.put(childAcc, child);
                }
                
                child.setAcc(childAcc);
                child.setName(rst.getString(COLUMN_NAME));
                String definition = rst.getString(COLUMN_DEFINITION);
                if (null == definition) {
                    definition = STR_EMPTY;
                }
                child.setDescription(definition);
                child.setAspect(rst.getString(COLUMN_ASPECT_CHILD));
                child.setId(Integer.toString(rst.getInt(COLUMN_CHILD_ID)));
                String parentAcc = rst.getString(COLUMN_PARENT);
//                if (true == "GO:0061631".equals(parentAcc)) {
//                    System.out.println("Here");
//                }                
                GOTerm parent = clsToTermLookup.get(parentAcc);
                if (null == parent) {
                    parent = new GOTerm();
                    clsToTermLookup.put(parentAcc, parent);
                }
                parent.setAcc(parentAcc);
                parent.setId(Integer.toString(rst.getInt(COLUMN_PARENT_ID)));
                parent.setAspect(rst.getString(COLUMN_ASPECT_PARENT));
                child.addParent(parent);
                parent.addChild(child);
//                term.setSingleParentAccession(rst.getString(COLUMN_PARENT)); 
//                term.setRank(rst.getInt(COLUMN_RANK));
//                catList.add(child);
//                catList.add(parent);
            }
            rst.close();
        }
        catch(SQLException se) {
            se.printStackTrace();
        }
        finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }
        return clsToTermLookup;

    }    
        
        
    public static GOTerm getGOCategoryByAcc(String acc) {
        if (null == acc) {
            return null;
        }
        String query = Utils.replace(QUERY_CATEGORY_ACC_GO, PARAM_1, acc);
        query = Utils.replace(query, PARAM_2, GO_CLS_VERSION_SID);
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;
        GOTerm term = null;
        try {
            con = DBConnectionPool.getInstance().getConnection(DB_CONNECTION_STR);
            stmt = con.createStatement();
            rst = stmt.executeQuery(query);

            while (rst.next()) {
                term = new GOTerm();
                term.setAcc(rst.getString(COLUMN_ACCESSION));
                term.setName(rst.getString(COLUMN_NAME));
                term.setId(Integer.toString(rst.getInt(COLUMN_ID)));
                String definition = rst.getString(COLUMN_DEFINITION);
                if (null == definition) {
                    definition = STR_EMPTY;
                }
                term.setDescription(definition);
//                cat.setDepth(rst.getInt(COLUMN_DEPTH));
            }
            rst.close();
        }
        catch(SQLException se) {
            se.printStackTrace();
        }
        finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }
        return term;

    }
    
    public static Hashtable getCategoryReleaseInfo() {
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;
        Hashtable rtnTbl = new Hashtable();
        try {
            con = DBConnectionPool.getInstance().getConnection(DB_CONNECTION_STR);
            if (null == con) {
                log.error("Error unable to get database connection");
                return null;
            }
        
            stmt = con.createStatement();
            rst = stmt.executeQuery(QUERY_VERSION_INFO);
            while(rst.next()) {
                String goVersion = rst.getString(COLUMN_GO_ANNOTATION_FORMAT_VERSION);
                Date goDate = rst.getDate(COLUMN_GO_ANNOTATION_RELEASE_DATE);
                String pantherVersion = rst.getString(COLUMN_PANTHER_VERSION);
                Date pantherDate = rst.getDate(COLUMN_PANTHER_RELEASE_DATE);
                if (null != goVersion) {
                    rtnTbl.put(COLUMN_GO_ANNOTATION_FORMAT_VERSION, goVersion);
                }
                if (null != goDate) {
                    rtnTbl.put(COLUMN_GO_ANNOTATION_RELEASE_DATE, DATE_FORMATTER.format(goDate));
                }
                if (null != pantherVersion) {
                    rtnTbl.put(COLUMN_PANTHER_VERSION, pantherVersion);
                }
                if (null != pantherDate) {
                    rtnTbl.put(COLUMN_PANTHER_RELEASE_DATE, DATE_FORMATTER.format(pantherDate));
                } 
                break;
            }
            if (rtnTbl.isEmpty()) {
                return null;
            }
        }
        catch(Exception e) {
            log.error(e.getMessage());
            rtnTbl = null;
        }
        finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }
        


        return rtnTbl;        
    }    
    
}
