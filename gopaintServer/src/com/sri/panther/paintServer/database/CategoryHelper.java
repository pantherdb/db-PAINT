/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import java.util.HashMap;

/**
 *
 * @author muruganu
 */
public class CategoryHelper {
    
    public static final String QUERY_CATEGORY_SINGLE_PARENT_GO = "select distinct c1.accession, c1.name, c1.definition, c1.classification_id child_id, c2.accession parent, c2.classification_id parent_id, r.rank, c1.term_type_sid, c2.term_type_sid,  (case when ctt1.term_name='molecular_function' THEN 'F' when ctt1.term_name='cellular_component'THEN 'C' when ctt1.term_name='biological_process' THEN 'P' END) as cAspect, (case when ctt2.term_name='molecular_function' THEN 'F' when ctt2.term_name='cellular_component'THEN 'C' when ctt2.term_name='biological_process' THEN 'P' END) as pAspect from go_classification c1, go_classification_relationship r, go_classification c2,  classification_term_type ctt1, classification_term_type ctt2 where c1.depth is null  and c1.classification_version_sid = %1 and c1.obsolescence_date is null and c1.classification_id = r.child_classification_id and r.parent_classification_id = c2.classification_id and r.obsolescence_date is null and c2.classification_version_sid = %1 and c2.obsolescence_date is null   and c1.term_type_sid = ctt1.term_type_sid and c2.term_type_sid = ctt2.term_type_sid ";
    public static final String QUERY_CATEGORY_ACC_GO = "select accession, name, depth, definition, term_type_sid, classification_id from go_classification where accession = '%1' and obsolescence_date is null and CLASSIFICATION_VERSION_SID = %2";
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
    
  public static final String GO_CLS_VERSION_SID = ConfigFile.getProperty("go.cls.version_sid"); 
    public static final String DB_CONNECTION_STR = ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID);      
    public static final String STR_EMPTY = "";
    
    public static HashMap<String, GOTerm> getGOTermLookup() {    
        String query = Utils.replace(QUERY_CATEGORY_SINGLE_PARENT_GO, PARAM_1, GO_CLS_VERSION_SID);
        //System.out.println(query);
        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;
        //List<GOTerm> catList = null;
        HashMap<String, GOTerm> clsToTermLookup = new HashMap<String, GOTerm>();
        try {
            con = DBConnectionPool.getConnection(DB_CONNECTION_STR);
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
            con = DBConnectionPool.getConnection(DB_CONNECTION_STR);
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
    
}
