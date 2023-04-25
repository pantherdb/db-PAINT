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

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.Classification;
import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.GO.Evidence;
import com.sri.panther.paintCommon.GO.EvidenceSpecifier;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintCommon.util.StringUtils;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.datamodel.Annotation;
import com.sri.panther.paintServer.datamodel.Organism;
import com.sri.panther.paintServer.datamodel.PANTHERTree;
import com.sri.panther.paintServer.datamodel.PANTHERTreeNode;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationNode;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.log4j.Logger;


public class DataServer{
  protected static final String TAB_DELIM = "\t";
  protected static final String COMMA_DELIM = ",";
  protected static final String SEMI_COLON_DELIM = ";";
  protected static final String SPACE_DELIM = " ";
  protected static final String LINKS_ASSOC = "=";
  protected static final String BAR_DELIM = "|";
  protected static final String NEWLINE_DELIM = "\n";
  protected static final String QUOTE = "'";
  protected static final String PERCENT = "%";
  protected static final String STR_EMPTY = "";
  protected static final String STR_COMMA = ",";
  protected static final String STR_BRACKET_OPEN = "(";
  protected static final String STR_BRACKET_CLOSE = ")";
  protected static final String STR_COLON = ":";
  protected static final String DELIM_GENE_IDENTIFIER = "|";
  
  protected static final String ROOT = "root";
  
  public static final String FORMAT_DATE = "hh:mm:ss:SSS";
  public static final String MSG_START_SAVE_PART1 = "Start of Save Operation for book ";
  public static final String MSG_START_SAVE_PART2 = " save option ";
  public static final String MSG_SAVE_FAILED_CANNOT_VERIFY_USER = "Save Operation failed - Cannot verify user information.";
  public static final String MSG_SAVE_FAILED_BOOK_NOT_LOCKED_BY_USER = "Save Operation failed - Book not locked by user.";
  public static final String MSG_SAVE_FAILED_ERROR_ACCESSING_DATA_FROM_DB = "Error accessing data from database.";
  public static final String MSG_SAVE_FAILED_INVALID_UPL_SPECIFIED = "Invalid UPL version specified.";
  public static final String MSG_SAVE_FAILED_UPL_RELEASED = "UPL has already been released, changes can no-longer be saved.";
  public static final String MSG_SAVE_FAILED_SEQ_ACC_UNAVAILABLE = "Save Operation failed - unable to retrieve sequence accession information";
  public static final String MSG_SAVE_FAILED_SF_INFO_INAVAILABLE = "save operation failed - Unable to retrieve subfamily information";
    public static final String MSG_SAVE_FAILED_SF_NAME_UNAVAILABLE = "Save Operation failed - unable to retrieve subfamily annotation information";  
    public static final String MSG_SAVE_FAILED_INVALID_ACC_ENCOUNTERED_PART1 =  " save failed - invalid accession ";
    public static final String MSG_SAVE_FAILED_INVALID_ACC_ENCOUNTERED_PART2 =  " encountered.";
    public static final String MSG_SAVE_FAILED_INVALID_SF_AN_INFO = " invalid subfamily to annotation information.";
    public static final String MSG_SAVE_FAILED_NO_ANNOT_COL = "Save failed, new annotation column not found.";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_FAMILY_ID = " save failed - unable to retrieve family id";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_GENERATE_FAMILY_ID_RECORD = " save operation failed - Cannot generate id to add a family record";
    public static final String MSG_SAVE_FAILED_LOGIC_ERR_NUM_SF_DOES_NOT_MATCH = " save operation failed - number of subfamilies does not match.";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS = " save operation failed - unable to generate ids.";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_ANNOT_PART1 = " save operation failed - unable to retrieve annotation for node ";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_ANNOT_PART2 = " with cls id  ";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_ANNOTATION_TYPE_ID_FOR_ACC = " save operation failed - unable to retrieve annotation type id for accession ";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_SAVE_FAMILY_RECORD = " save operation failed - unable to save family record.";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_SAVE_TREE = " save operation failed - unable to save tree.";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_INSERT_SUBFAMILIES = " save operation failed - unable to insert subfamilies.";
    public static final String MSG_SAVE_FAILED_NO_CLS_RELATIONSHIP_FOR_INSERTED_SUBFAMILIES = " save operation failed - no classification relationship for inserted subfamilies.";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_INSERT_FAMILY_SUBFAMILY_RELATION = " save Operation failed - unable to insert family to subfamily relationships.";
    public static final String MSG_SAVE_FAILED_UNABLE_TO_INSERT_CLS_ASSOCIATED_WITH_FAMILY = " save Operation failed - unable to insert classifications associated to family";

    public static final String MSG_SAVE_FAILED_UNABL_TO_INSERT_ANNOTATIONS = " save operation failed - unable to insert annotations.";
    public static final String MSG_SAVE_FAILED_UNABL_TO_INSERT_ANNOTATION_QUALIFIERS = " save operation failed - unable to insert annotation qualifiers.";  
    
    public static final String MSG_SAVE_FAILED_EXCEPTION_RETURNED = " save failed, exception returned:  ";
    
    
    public static final String MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_CLS_RECORDS = " save operation failed - unable to obsolete classification records";
    public static final String MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_CLS_RLTN_RECORDS = "save operation failed - unable to obsolete classification relationship records";
    public static final String MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_ANNOT_RECORDS = " save operation failed - unable to obsolete annotation records";

    
    public static final String MSG_INSERTION_FAILED_EXCEPTION_RETURNED = " unable to insert records, exception returned:  ";  
  
  public static final String MSG_UNABLE_TO_RETRIEVE_TREE_NODES = " unable to retrieve nodes in tree.";
  public static final String MSG_UNABLE_TO_RETRIEVE_ANNOTATIONS = " Unable to retrieve annotations for book.";
  public static final String MSG_AN_HAS_NO_SF_ANCESTOR = " has no subfamily ancestor.";
  public static final String MSG_NUM_SF_NOT_EQUAL = "Number of subfamilies are not equal.";
  public static final String MSG_INVALID_SF_TO_SEQ_INFO = " invalid subfamily to sequence information.";
  public static final String MSG_INVALID_SEQUENCE_IDENTIFIER_INFO = " invalid sequence information, unable to find attribute table row for node  ";
  public static final String MSG_SF_NOT_ASSOCIATED_WITH_NODES = "Subfamilies not associated with sequences ";
  public static final String MSG_INVALID_ATTR_TABLE = " invalid attribute table.";
  public static final String MSG_UNABLE_TO_RETRIEVE_NOT_QUALIFIER_ID = "Unable to retrieve not qualifier id";
  public static final String MSG_UNABLE_TO_RETRIEVE_CLS_HIERARCHY_DATA = "Unable to retrieve classification hierarchy data";
  public static final String MSG_UNABLE_TO_RETRIEVE_ANNOT_TYPE_ID = "Unable to retrieve annotation type id";
  
  
  
  
  
//  protected static final String STR_TREE_NAN = ":nan";
//  protected static final String STR_TREE_NAN_REPLACEMENT = ":2.0";
  protected static final int    NUM_IN_STMT = 1000;
  protected static final int    CLOB_BUFFER_LENGTH = 32000;
  protected static final int    CARRYOVER_COMMENT = 0;
  protected static final int    CARRYOVER_EVIDENCE = 1;
  protected static final int    CARRYOVER_FEATURE = 2;
  
  
  // Indices of confidenc codes information in confidence list
  protected static final int INDEX_CONF_CODE_SID = 0;
  protected static final int INDEX_CONF_CODE_TYPE = 1;
  protected static final int INDEX_CONF_CODE_NAME = 2;
  
  // Indices of evidence and confidence information when it is retrieved from database
  protected static final int INDEX_CONF_EVDNCE_EV_TYPE = 0;
  protected static final int INDEX_CONF_EVDNCE_EV_VALUE = 1;
  protected static final int INDEX_CONF_EVDNCE_CC_TYPE = 2;
  protected static final int INDEX_CONF_EVDNCE_CC_SID = 3;
  
  // Define indices for classification information in classification column data
  protected static final int INDEX_CONF_EVDNCE_ATTR_CC_TYPE = 0;
  protected static final int INDEX_CONF_EVDNCE_ATTR_EV_TYPE = 1;
  protected static final int INDEX_CONF_EVDNCE_ATTR_EV_VALUE = 2;
  
  
  public static final int INDEX_TBL_GENE_ID = 0;
  public static final int INDEX_TBL_GENE_SYMBOL = 1;
  public static final int INDEX_TBL_GENE_NAME = 2;
  
  
    public static final int INFO_INDEX_AN_SF_ID = 0;
    public static final int INFO_INDEX_AN_SF_NAME = 1;
  
  

  
    protected static final String CURATION_STATUS_CHECKOUT = "panther_check_out";
    protected static final String CURATION_STATUS_NOT_CURATED = "panther_not_curated";
    protected static final String CURATION_STATUS_AUTOMATICALLY_CURATED = "panther_automatically_curated";
    protected static final String CURATION_STATUS_MANUALLY_CURATED = "panther_manually_curated";
    protected static final String CURATION_STATUS_REVIEWED = "panther_curation_reviewed";
    protected static final String CURATION_STATUS_QAED = "panther_curation_QAed";
    protected static final String CURATION_STATUS_PARTIALLY_CURATED = "panther_partially_curated";
    protected static final String CURATION_STATUS_REQUIRE_PAINT_REVIEW = "go_require_paint_review";
    protected static final String CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_NOT_MAPPED = "go_require_paint_review_ptn_not_mapped";
    protected static final String CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_CHANGE_FAMILIES = "go_require_paint_review_ptn_change_families";
    protected static final String CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_TRACKED_TO_CHILD_NODE = "go_require_paint_review_ptn_tracked_to_child_node";     
  
  protected static final String LEVEL_FAMILY = "_famLevel";
  protected static final String LEVEL_SUBFAMILY = "_subfamLevel";
  
  public static final String PROPERTY_CLASSIFICATION_VERSION_SID = "cls_version_sid";
  protected static final String PROPERTY_PANTHER_CLS_TYPE_SID = "panther_cls_type_sid";
  protected static final String PROPERTY_GO_SUPPORT_CONF_CODE = "_go_support_conf_code";
  protected static final String PROPERTY_AUTO_CURATION_USER_ID = "auto_curation_user_id";
  

  public static final String PROPERTY_ANNOTATION_TYPE_SF = "annot_type_sf";
  public static final String PROPERTY_ANNOTATION_TYPE_GO = "annot_type_go";
  public static final String PROPERTY_ANNOTATION_TYPE_PC = "annot_type_pc";
  
  
  public static final String PROPERTY_SUFFIX_IDENTIFIERS = "_identifiers";
  public static final String PROPERTY_SUFFIX_EVIDENCE = "_evidence";
  public static final String PROPERTY_SUFFIX_COL_HEADERS = "_colHeaders";
  public static final String PROPERTY_SUFFIX_COL_INFO = "_colInfo";
  public static final String PROPERTY_SUFFIX_GO_CLS_TYPE_SID = "_go_cls_type_sid";
  public static final String PROPERTY_SUFFIX_GO_SLIM = "_go_slim_rltn";
  public static final String PROPERTY_SUFFIX_PROTEIN_CLASS_CLS_TYPE_SID = "_protein_class_type_sid";
  
  public static final String PROPERTY_COL_IDENTIFIER = "identifier_col";
  public static final String PROPERTY_COL_XLINKS = "xlinks_col";
  public static final String PROPERTY_COL_EVIDENCE = "evidence_col";
  public static final String PROPERTY_COL_ACC = "acc_col";
  public static final String PROPERTY_COL_SF = "sf_col";
  public static final String PROPERTY_COL_PANTHER_PUBLIC_ID = "panther_ext_id"; 
//  public static final String PROPERTY_COL_CARRYOVER_ANNOT = "carryover_annot";
//  public static final String PROPERTY_COL_DIRECT_ANNOT = "direct_annot";  
  
    protected static final String ATTR_COLUMN_GENE_ID = "gene_id_col";
    protected static final String ATTR_COLUMN_GENE_SYMBOL = "gene_symbol_col";
    protected static final String ATTR_COLUMN_GO_ANNOTATION = "go_annotation_col";
    protected static final String ATTR_COLUMN_GO_INFERENCE = "go_inference_col";
    protected static final String ATTR_COLUMN_PANTHER_ANNOT = "panther_annot_col";
    protected static final String ATTR_COLUMN_PANTER_NEW_ANNOT = "panther_newAnnot_col";

  
  protected static final String RANK_PANTHER_CURATOR = "panther_curator_rank";
  
  protected static final String MSG_SUCCESS = Constant.STR_EMPTY;
  protected static final String MSG_CLASSIFICATION_ID_INFO_NOT_FOUND = "Classification information not found";  
  
  protected static final String QUERY_STR_RELEASE_CLAUSE_TBL = "tblName";
  protected static final String QUERY_STR_RELEASE_CLAUSE_VAR_G = "g";
  protected static final String QUERY_PARAMETER_1 = "%1";
  protected static final String QUERY_PARAMETER_2 = "%2";
  protected static final String QUERY_PARAMETER_3 = "%3";
  protected static final String QUERY_PARAMETER_4 = "%4";
  protected static final String QUERY_SUBFAMILY_MIDDLE_WILDCARD = ":%";
  protected static final String QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD = ":%";
  protected static final String QUERY_WILDCARD = "%";
  
  protected static final String COLUMN_NAME_BRANCH_LENGTH = "BRANCH_LENGTH";
  protected static final String COLUMN_NAME_PROTEIN_ID = "PROTEIN_ID";
  protected static final String COLUMN_NAME_GENE_NAME = "GENE_NAME";
  protected static final String COLUMN_NAME_GENE_SYMBOL = "GENE_SYMBOL";
  protected static final String COLUMN_NAME_GENE_PRIMARY_EXT_ACC = "PRIMARY_EXT_ACC";
  protected static final String COLUMN_NAME_ACCESSION = "ACCESSION";
  protected static final String COLUMN_NAME_ANNOTATION_ID = "ANNOTATION_ID";
  protected static final String COLUMN_NAME_ANNOTATION_TYPE_ID = "ANNOTATION_TYPE_ID";
  protected static final String COLUMN_NAME_ANNOTATION_TYPE = "ANNOTATION_TYPE";
  protected static final String COLUMN_NAME_ASPECT = "ASPECT";
  protected static final String COLUMN_NAME_CLASSIFICATION_ID = "classification_id";
  protected static final String COLUMN_NAME_GO_ACCESSION = "GO_ACCESSION";
  protected static final String COLUMN_NAME_GO_NAME = "GO_NAME";
  protected static final String COLUMN_NAME_GROUP_NAME = "GROUP_NAME";
  protected static final String COLUMN_NAME_NAME = "NAME";
  protected static final String COLUMN_NAME_NODE_ID = "NODE_ID";
  protected static final String COLUMN_NAME_NODE_TYPE = "NODE_TYPE";
  protected static final String COLUMN_NAME_EVENT_TYPE = "EVENT_TYPE";
  protected static final String COLUMN_NAME_EVIDENCE = "evidence";
  protected static final String COLUMN_TYPE = "TERM_NAME";        //"DECODE(ctt.TERM_NAME, 'molecular_function', 'F','cellular_component', 'C','biological_process','P')";
  protected static final String COLUMN_CONFIDENCE_CODE = "CONFIDENCE_CODE";
  protected static final String COLUMN_PRIMARY_EXT_ID = "PRIMARY_EXT_ID";
  protected static final String COLUMN_NAME_PRIMARY_EXT_ACC = "PRIMARY_EXT_ACC";
  protected static final String COLUMN_NAME_SOURCE_ID = "SOURCE_ID";
  protected static final String COLUMN_NAME_IDENTIFIER_TYPE_SID = "IDENTIFIER_TYPE_SID";
  protected static final String COLUMN_NAME_QUALIFIER = "QUALIFIER";
  protected static final String COLUMN_NAME_CHILD_ACCESSION = "CHILD_ACCESSION";
  protected static final String COLUMN_NAME_PARENT_ACCESSION = "PARENT_ACCESSION";
  protected static final String COLUMN_NAME_CHILD_ID = "CHILD_ID";
  protected static final String COLUMN_NAME_PARENT_ID = "PARENT_ID";
  protected static final String COLUMN_NAME_PARENT = "PARENT";
  protected static final String COLUMN_NAME_PUBLIC_ID = "PUBLIC_ID";
  protected static final String COLUMN_NAME_NODE_ACCESSION = "NODE_ACCESSION";
  protected static final String COLUMN_NAME_CREATION_DATE = "CREATION_DATE";
  protected static final String COLUMN_NAME_RANK = "RANK";
  protected static final String COLUMN_NAME_RELATIONSHIP = "RELATIONSHIP";
  protected static final String COLUMN_NAME_CREATED_BY = "CREATED_BY";
  protected static final String COLUMN_NAME_IDENTIFIER_TYPE = "identifier_type";
  
  
  protected static final String COLUMN_NAME_PRIVILEGE_RANK = "PRIVILEGE_RANK";
  protected static final String COLUMN_NAME_USER_NAME = "NAME";
  protected static final String COLUMN_NAME_LOGIN_NAME = "LOGIN_NAME";
  protected static final String COLUMN_NAME_EMAIL = "EMAIL";
  protected static final String COLUMN_NAME_QUALIFIER_ID = "QUALIFIER_ID";
  
    public static final String COLUMN_DATABASE_ID = "ORGANISM_id";
    public static final String COLUMN_ORGANISM = "organism";
    public static final String COLUMN_CONVERSION = "conversion";
    public static final String COLUMN_SHORT_NAME = "short_name";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_COMMON_NAME = "common_name";
    public static final String COLUMN_LOGICAL_ORDERING = "logical_ordering";
    public static final String COLUMN_REF_GENOME = "ref_genome";
    public static final String COLUMN_TAXON_ID = "TAXON_ID";    
  
  protected static final String GENE_IDENTIFIER_TOKEN = "=";
  protected static final String GENE_IDENTIFIER_REPLACEMENT = ":";
  
  
  protected static final String QUALIFIER_NOT = "NOT";
  

  
  

  
  
  protected static final String TBL_GO_ANNOTATION = "go_annotation";
  
  protected static final String MSG_INITIALIZING_CLS_INFO = "Going to initialize the cls to version release lookup";
  protected static final String MSG_ERROR_UNABLE_TO_VERIFY_USER = "Unable to verify user information";
  protected static final String MSG_ERROR_UNABLE_TO_RETRIEVE_USER_STATUS_INFO = "Unable to retrieve user status information";
  protected static final String MSG_ERROR_USER_DOES_NOT_HAVE_PRIVILEGE_TO_LOCK_BOOKS = "User does not have privildege to lock books";
  protected static final String MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER = "Unable to lock books for user";
  protected static final String MSG_ERROR_RETRIEVING_BOOKS_LOCKED_BY_USER = "Unable to retrieve books locked by user";
  protected static final String MSG_ERROR_INVALID_INFO_FOR_LOCKING_BOOKS = "Invalid information for locking books";
  protected static final String MSG_ERROR_UNLOCKING_BOOKS = "Unable to unlock the following books ";
  
  protected static final String MSG_ERROR_IDENTIFIER_RETRIEVAL_NULL_FOUND = " - Error during identifier retrieval for protein ";
  protected static final String MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED = "Unable to retrieve information, exception returned:  ";
  protected static final String MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION = "Unable to close connection, exception returned:  ";
  protected static final String MSG_ERROR_NULL_IDENTIFIER_RSLT_RETURNED = "Null identifier result returned for book ";
  protected static final String MSG_ERROR_DURING_CLS_OBSOLETE = "Unable to obsolete classification records, exception returned:  ";


  protected static final String MSG_ERROR_NULL_PROTEIN_INFO_ENCOUNTERED = "Null protein information encountered for book ";

  
  
  
  protected static final String ID_RSLT_ACC = "id_acc";
  protected static final String SF_RSLT_NAME = "sf_name";
  protected static final String TBL_COL_ORG = "org_col";
  
  
  protected static final String REPLACE_STR_PERCENT_1 = "%1";
    protected static final String REPLACE_STR_PERCENT_2 = "%2";
    protected static final String REPLACE_STR_PERCENT_3 = "%3";
  
  
  public static final String RELEASE_CLAUSE_TBL_NAME = "tblName";


    protected static final String TABLE_NAME_a = "a";
    protected static final String TABLE_NAME_c = "c";
    protected static final String TABLE_NAME_c1 = "c1";
    protected static final String TABLE_NAME_c2 = "c2";
    protected static final String TABLE_NAME_cp = "cp";
    protected static final String TABLE_NAME_cr = "cr";
    protected static final String TABLE_NAME_e = "e";
    protected static final String TABLE_NAME_g = "g";
    protected static final String TABLE_NAME_gn = "gn";
    protected static final String TABLE_NAME_gp = "gp";
    protected static final String TABLE_NAME_i = "i";
    protected static final String TABLE_NAME_n = "n";
    protected static final String TABLE_NAME_n1 = "n1";
    protected static final String TABLE_NAME_n2 = "n2";
    protected static final String TABLE_NAME_node = "node";
    protected static final String TABLE_NAME_nr = "nr";
    protected static final String TABLE_NAME_p = "p";
    protected static final String TABLE_NAME_pc = "pc";
    protected static final String TABLE_NAME_pn = "pn";
    protected static final String TABLE_NAME_r = "r";
    
    
    protected static final int MAX_NUM_BOOK_LIST = 1000;
  
  
  public static final int INDEX_ATTR_METHOD_ATTR_TBL = 0;
  public static final int INDEX_ATTR_METHOD_SF_AN_INFO = 1;
  
  

  
  
  protected int          uid_num = 0;
  protected Hashtable    protSrcToOrgLookup;
  protected Hashtable    userIdToRankLookup;
  protected Hashtable    dbToClsId;
  protected Hashtable    clsIdToVersionRelease;
  protected Hashtable    clsTypeIdToCatId;
  protected Hashtable    bookListTbl;
  protected Hashtable    bookToNumSeq;     // List of books to be saved
  protected Vector       confidenceCodesList;
  protected Hashtable<String, Classification>   uplToClsHierarchyData;
  protected Hashtable<String, Hashtable<String, Integer>> uplToClsAccClsIdData;
  protected String dbStr;

  // Hashtable of upl versions to Hashtables.
  // The hashtable corresponding to each upl is a hashtable
  // that can be accessed by family accession.  The value
  // corresponding to the family accession is again a
  // hashtable of family/subfamily accession to names.
  protected Hashtable    uplToBookInfo;
  
    protected static Logger log = Logger.getLogger(DataServer.class.getName());

  /**
   * Constructor declaration
   *
   *
   * @see
   */
  public DataServer(String dbStr) {
    this.dbStr = dbStr;
  }

  protected Connection getConnection() throws SQLException {
    return DBConnectionPool.getInstance().getConnection(dbStr);
  }
  
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

  /**
   * Method declaration
   *
   *
   * @see
   */
  public synchronized void initProtSrcToOrgLookup(){
    if (null != protSrcToOrgLookup){
      return;
    }
    System.out.println("Initialized organism information");
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return;
      }
      Statement stmt = con.createStatement();
      String    query = QueryString.ORGANISM_LOOKUP;

      stmt.setFetchSize(3000);
      ResultSet rst = stmt.executeQuery(query);

      protSrcToOrgLookup = new Hashtable();
      while (rst.next()){
        String[]  orgInfo = new String[2];

        orgInfo[0] = rst.getString(2);
        orgInfo[1] = rst.getString(3);
        protSrcToOrgLookup.put(Integer.toString(rst.getInt(1)), orgInfo);
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return;
        }
      }
    }
  }

  /**
   * Method declaration
   *
   *
   * @see
   */
  public void initUserIdToRankLookup(){
    if (null != userIdToRankLookup){
      return;
    }
    Connection  con = null;

    System.out.println("Initialized user to rank information");
    try{
      con = getConnection();
      if (null == con){
        return;
      }
      Statement stmt = con.createStatement();
      String    query = QueryString.USER_LOOKUP;

      //System.out.println(query);
      ResultSet rst = stmt.executeQuery(query);

      userIdToRankLookup = new Hashtable();
      while (rst.next()){
        userIdToRankLookup.put(Integer.toString(rst.getInt(1)), Integer.toString(rst.getInt(2)));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return;
        }
      }
    }
  }

  /**
   * Method declaration
   *  No need to store this information in a hashtable, since query will only return 0 or 1 row.  Kept in hashtable, 
   *  since, other code expects hashtable format.
   *
   * @see
   */
  public synchronized void initClsLookup(){
    Connection  con = null;

    if (null != clsIdToVersionRelease){
      return;
    }
    try{
      log.debug(MSG_INITIALIZING_CLS_INFO);
      con = getConnection();
      if (null == con){
        return;
      }
      Statement stmt = con.createStatement();
      String    query = QueryString.CLS_VERSION;

      query = Utils.replace(query, QUERY_PARAMETER_1, ConfigFile.getProperty(PROPERTY_PANTHER_CLS_TYPE_SID));  
      query = Utils.replace(query, QUERY_PARAMETER_2, ConfigFile.getProperty(PROPERTY_CLASSIFICATION_VERSION_SID));      
      

      // System.out.println(query);
      ResultSet rst = stmt.executeQuery(query);

      while (rst.next()){
        if (null == clsIdToVersionRelease){
          clsIdToVersionRelease = new Hashtable();
        }
        Vector  clsInfo = new Vector(2);

        clsInfo.addElement(rst.getString(1));
        String d = rst.getString(2);

        if (null != d){
          clsInfo.addElement(d);
        }
        else{
          clsInfo.addElement(null);
        }
        clsIdToVersionRelease.put(Integer.toString(rst.getInt(3)), clsInfo);
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
  }

  /**
   * Method declaration
   *
   *
   * @see
   */
  public void initCatLookup(){
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return;
      }
      if (null == clsIdToVersionRelease){
        initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        return;
      }
      clsTypeIdToCatId = new Hashtable();
      Enumeration clsIds = clsIdToVersionRelease.keys();
      Statement   stmt = null;

      while (clsIds.hasMoreElements()){
        String    uplVersion = (String) clsIds.nextElement();
        Hashtable current = new Hashtable();

        clsTypeIdToCatId.put(uplVersion, current);
        String  query = QueryString.CLS_HIERARCHY;

        query = Utils.replace(query, "%1", uplVersion);

        // If this version of the upl has been released, then add clause to ensure only records
        // created prior to the release date are retrieved
        Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
        String  dateStr = (String) clsInfo.elementAt(1);

        if (null != dateStr){
          query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c1");
          query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c2");
          query = Utils.replace(query, "%1", dateStr);
        }
        else{
          query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c1");
          query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c2");
        }
        stmt = con.createStatement();

        //System.out.println(query);
        ResultSet rst = stmt.executeQuery(query);

        while (rst.next()){
          current.put(rst.getString(1), Integer.toString(rst.getInt(5)));
        }
        rst.close();

        // Now get the top level nodes
        query = QueryString.CLS_ROOT;
        query = Utils.replace(query, "%1", uplVersion);
        if (null != dateStr){
          query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c");
          query = Utils.replace(query, "%1", dateStr);
        }
        else{
          query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
        }

        //System.out.println(query);
        ResultSet rootRst = stmt.executeQuery(query);

        while (rootRst.next()){
          current.put(rootRst.getString(1), Integer.toString(rst.getInt(3)));
        }
        rootRst.close();
      }
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return;
        }
      }
    }
    return;
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   *
   * @return
   *
   * @see
   */
  public boolean verifyUserInfo(String userName, String password){
    boolean userValid = false;

    if ((0 == userName.length()) || (0 == password.length())){
      return userValid;
    }
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return userValid;
      }
      PreparedStatement stmt = con.prepareStatement(QueryString.PREPARED_USER_VALIDATION);

      stmt.setString(1, userName);
      stmt.setString(2, password);

      //System.out.println(QueryString.PREPARED_USER_VALIDATION);
      ResultSet rst = stmt.executeQuery();

      while (rst.next()){
        userValid = true;
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return false;
        }
      }
    }
    return userValid;
  }

  public Vector getUserInfo(String userName, String password){
    Vector userInfo = null;

    if ((0 == userName.length()) || (0 == password.length())){
      return userInfo;
    }
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      PreparedStatement stmt = con.prepareStatement(QueryString.PREPARED_USER_VALIDATION);

      stmt.setString(1, userName);
      stmt.setString(2, password);

      ResultSet rst = stmt.executeQuery();
      userInfo = new Vector();
      if (rst.next()){
        userInfo.addElement(Boolean.TRUE);
        userInfo.addElement(new Integer(rst.getInt(COLUMN_NAME_PRIVILEGE_RANK)));
      }
      else {
        userInfo.addElement(Boolean.FALSE);
        userInfo.addElement(new Integer(Constant.USER_PRIVILEGE_UNLOGGED));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve user information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return userInfo;
  }


    public User getUser(String userName, String password) {
        User user = null;
        
        if (null == userName || null == password || 0 == userName.length() || 0 == password.length()) {
            return user;
        }
        Connection con = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            PreparedStatement stmt =
                con.prepareStatement(QueryString.PREPARED_USER_VALIDATION);

            stmt.setString(1, userName);
            stmt.setString(2, password);

            ResultSet rst = stmt.executeQuery();

            if (rst.next()) {
                int rank = rst.getInt(COLUMN_NAME_PRIVILEGE_RANK);
                String name = rst.getString(COLUMN_NAME_USER_NAME);
                String email = rst.getString(COLUMN_NAME_EMAIL);
                String groupName = rst.getString(COLUMN_NAME_GROUP_NAME);
                user = new User(name, null, email, userName, rank, groupName);
            }
            rst.close();
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Unable to retrieve user information from database, exception " +
                               se.getMessage() + " has been returned.");
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    System.out.println("Unable to close connection, exception " +
                                       se.getMessage() +
                                       " has been returned.");
                    return null;
                }
            }
        }
        return user;
    }


  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   *
   * @return
   *
   * @see
   */
  public String getUserId(String userName, String password){
    Integer userId = null;

    if ((0 == userName.length()) || (0 == password.length())){
      return null;
    }
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      PreparedStatement stmt = con.prepareStatement(QueryString.PREPARED_USER_VALIDATION);

      stmt.setString(1, userName);
      stmt.setString(2, password);

      //System.out.println(QueryString.PREPARED_USER_VALIDATION);
      ResultSet rst = stmt.executeQuery();

      while (rst.next()){
        userId = new Integer(rst.getInt(1));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return userId.toString();
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public Hashtable getClsLookup(){
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }
    return clsIdToVersionRelease;
  }


    protected synchronized void initClsHierarchyData(String uplVersion) {
        if (null == uplToClsHierarchyData) {
            uplToClsHierarchyData = new Hashtable<String, Classification>();
        }
        Classification root = uplToClsHierarchyData.get(uplVersion);

        if (null == root) {
            Vector info = (Vector) getClsHierarchyData(uplVersion, true);
            root = Classification.parseClassificationData(info);
            if (null != root) {
                uplToClsHierarchyData.put(uplVersion, root);
            }
        }
        
        if (null == uplToClsAccClsIdData) {
            uplToClsAccClsIdData = new Hashtable<String, Hashtable<String, Integer>>();
        }
        Hashtable<String, Integer> clsIdTbl = uplToClsAccClsIdData.get(uplVersion);
        if (null == clsIdTbl) {
            clsIdTbl = (Hashtable<String, Integer>)getClsHierarchyData(uplVersion, false);
            if (null != clsIdTbl) {
                uplToClsAccClsIdData.put(uplVersion, clsIdTbl);
            }
        }
        
    }
 
    /**
     *
     * @param uplVersion
     * @param clsRltn if true then Vector of relationship information is returned, else hashtble of clsAccession to cls id is returned
     * @return
     */
    public Object getClsHierarchyData(String uplVersion, boolean clsRltn    ){
      Connection  con = null;
        Vector      clsList = null;
        Hashtable <String, Integer> rltnTbl = null;
      if (true == clsRltn) {
        clsList = new Vector();
      }
      else {
         rltnTbl = new Hashtable<String, Integer>();
      }
      try{
        con = getConnection();
        if (null == con){
          return null;
        }
        if (null == clsIdToVersionRelease){
          initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease){
          return null;
        }
        
        String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c1);
        query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_c2);
        query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_cp);
        query = QueryString.PANTHER_GO_SLIM_HIERARCHY + query;

        query = Utils.replace(query, REPLACE_STR_PERCENT_1, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_GO_CLS_TYPE_SID));
        query = Utils.replace(query, REPLACE_STR_PERCENT_2, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_GO_SLIM));
        System.out.println(query);

        Statement     stmt = con.createStatement();

        ResultSet     rst = stmt.executeQuery(query);
        StringBuffer  sb = new StringBuffer();

        while (rst.next()){
          String childAcc = rst.getString(COLUMN_NAME_CHILD_ACCESSION);
          String parentAcc = rst.getString(COLUMN_NAME_PARENT_ACCESSION);
          if (true == clsRltn) {
              sb.setLength(0);
              sb.append(childAcc);
              sb.append(TAB_DELIM);
              sb.append(rst.getString(COLUMN_NAME_NAME));
              sb.append(TAB_DELIM);
              sb.append(parentAcc);
              sb.append(TAB_DELIM);
              sb.append(Integer.toString(rst.getInt(COLUMN_NAME_RANK)));
              sb.append(TAB_DELIM);
              sb.append(rst.getString(COLUMN_NAME_RELATIONSHIP));
              clsList.addElement(sb.toString());
          }
          else {
              Integer childId = Integer.valueOf(rst.getInt(COLUMN_NAME_CHILD_ID));
              Integer parentId = Integer.valueOf(rst.getInt(COLUMN_NAME_PARENT_ID));
              rltnTbl.put(childAcc, childId);
              rltnTbl.put(parentAcc, parentId);
          }
        }
        rst.close();
        
        
        
        // Get the root go slim records
        String goRoot2 = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_cr);
        goRoot2 = addVersionReleaseClause(uplVersion, goRoot2, TABLE_NAME_c);
        goRoot2 = QueryString.PANTHER_GO_SLIM_ROOT_PART2 + goRoot2;
        goRoot2 =   Utils.replace(goRoot2, REPLACE_STR_PERCENT_1, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_GO_CLS_TYPE_SID));
        goRoot2 =   Utils.replace(goRoot2, REPLACE_STR_PERCENT_2, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_GO_SLIM));
        
        String goRoot = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c);
        goRoot = addVersionReleaseClause(uplVersion, goRoot, TABLE_NAME_r);
        goRoot = Utils.replace(QueryString.PANTHER_GO_SLIM_ROOT_PART1, REPLACE_STR_PERCENT_3, goRoot2) + goRoot;
        
        goRoot = Utils.replace(goRoot, REPLACE_STR_PERCENT_1, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_GO_CLS_TYPE_SID));
        goRoot = Utils.replace(goRoot, REPLACE_STR_PERCENT_2, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_GO_SLIM));
        
        
        System.out.println(goRoot);
        
        ResultSet rootRst = stmt.executeQuery(goRoot);
        while (rootRst.next()) {
            if (true == clsRltn) {
                sb.setLength(0);
                sb.append(rootRst.getString(COLUMN_NAME_ACCESSION));
                sb.append(TAB_DELIM);
                sb.append(rootRst.getString(COLUMN_NAME_NAME));
                sb.append(TAB_DELIM);
                sb.append(ROOT);
                sb.append(TAB_DELIM);
                sb.append(Integer.toString(rootRst.getInt(COLUMN_NAME_RANK)));
                sb.append(TAB_DELIM);
                sb.append(rootRst.getString(COLUMN_NAME_RELATIONSHIP));
                clsList.addElement(sb.toString());
            }
        }
        rootRst.close();
        
        
        // Get the protein class hierarchy
        String pcQuery = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c1);
        pcQuery = addVersionReleaseClause(uplVersion, pcQuery, TABLE_NAME_c2);
        pcQuery = addVersionReleaseClause(uplVersion, pcQuery, TABLE_NAME_cp);
        pcQuery = QueryString.PROTEIN_CLASS_HIERARCHY + pcQuery;
        pcQuery = Utils.replace(pcQuery, REPLACE_STR_PERCENT_1, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_PROTEIN_CLASS_CLS_TYPE_SID));
        
        System.out.println(pcQuery);

        ResultSet pcRst = stmt.executeQuery(pcQuery);
        while (pcRst.next()) {
            String childAcc = pcRst.getString(COLUMN_NAME_CHILD_ACCESSION);
            String parentAcc = pcRst.getString(COLUMN_NAME_PARENT_ACCESSION);
            if (true == clsRltn) {
                sb.setLength(0);
                sb.append(childAcc);
                sb.append(TAB_DELIM);
                sb.append(pcRst.getString(COLUMN_NAME_NAME));
                sb.append(TAB_DELIM);
                sb.append(parentAcc);
                sb.append(TAB_DELIM);
                sb.append(Integer.toString(pcRst.getInt(COLUMN_NAME_RANK)));
                clsList.addElement(sb.toString());
            }
            else {
                Integer childId = Integer.valueOf(pcRst.getInt(COLUMN_NAME_CHILD_ID));
                Integer parentId = Integer.valueOf(pcRst.getInt(COLUMN_NAME_PARENT_ID));
                rltnTbl.put(childAcc, childId);
                rltnTbl.put(parentAcc, parentId);  
            }
        }
        pcRst.close();
        
        
        // Get the protein class root
        String pcRoot2 = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_cr);
        pcRoot2 = addVersionReleaseClause(uplVersion, pcRoot2, TABLE_NAME_c);
        pcRoot2 = QueryString.PROTEIN_CLASS_ROOT_PART_2 + pcRoot2;
        pcRoot2 =   Utils.replace(pcRoot2, REPLACE_STR_PERCENT_1, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_PROTEIN_CLASS_CLS_TYPE_SID));
         
        String pcRoot = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c);
        pcRoot = addVersionReleaseClause(uplVersion, pcRoot, TABLE_NAME_r);
        pcRoot = Utils.replace(QueryString.PROTEIN_CLASS_ROOT_PART_1, REPLACE_STR_PERCENT_2, pcRoot2) + pcRoot;
        
        pcRoot = Utils.replace(pcRoot, REPLACE_STR_PERCENT_1, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_PROTEIN_CLASS_CLS_TYPE_SID));        
        System.out.println(pcRoot);
        
        ResultSet pcRootRst = stmt.executeQuery(pcRoot);
        while (pcRootRst.next()) {
            if (true == clsRltn) {
                sb.setLength(0);
                sb.append(pcRootRst.getString(COLUMN_NAME_ACCESSION));
                sb.append(TAB_DELIM);
                sb.append(pcRootRst.getString(COLUMN_NAME_NAME));
                sb.append(TAB_DELIM);
                sb.append(ROOT);
                sb.append(TAB_DELIM);
                sb.append(Integer.toString(pcRootRst.getInt(COLUMN_NAME_RANK)));
                clsList.addElement(sb.toString());
            }
        }
        pcRootRst.close();
        
        
        stmt.close();
        
      }
      catch (SQLException se){
        log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
        se.printStackTrace();

      }
      finally{
        if (null != con){
          try{
            con.close();
          }
          catch (SQLException se){
            log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
            se.printStackTrace();
          }
        }
      }
        if (true == clsRltn) {
            return clsList;
        }
        return rltnTbl;
    }


  /**
   * Method declaration
   *
   *
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public Vector getClsHierarchyDataOld(String uplVersion){
    Connection  con = null;
    Vector      clsList = new Vector();

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      if (null == clsIdToVersionRelease){
        initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        return null;
      }
      String  query = QueryString.CLS_HIERARCHY;

      query = Utils.replace(query, "%1", uplVersion);

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      if (null == clsInfo) {
          return null;
      }
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c1");
        query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c2");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c1");
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c2");
      }
      Statement     stmt = con.createStatement();

      System.out.println(query);
      ResultSet     rst = stmt.executeQuery(query);
      StringBuffer  sb = new StringBuffer();

      while (rst.next()){
        if (null == clsList){
          clsList = new Vector();
        }
        sb.setLength(0);
        sb.append(rst.getString(1));
        sb.append(TAB_DELIM);
        sb.append(rst.getString(2));
        sb.append(TAB_DELIM);
        sb.append(rst.getString(3));
        sb.append(TAB_DELIM);
        sb.append(Integer.toString(rst.getInt(4)));
        clsList.addElement(sb.toString());
      }
      rst.close();

      // Now get the top level nodes
      query = QueryString.CLS_ROOT;
      query = Utils.replace(query, "%1", uplVersion);
      if (null != dateStr){
        query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }

      //System.out.println(query);
      ResultSet rootRst = stmt.executeQuery(query);

      while (rootRst.next()){
        sb.setLength(0);
        sb.append(rootRst.getString(1));
        sb.append(TAB_DELIM);
        sb.append(rootRst.getString(2));
        sb.append(TAB_DELIM);
        sb.append("root");
        sb.append(TAB_DELIM);
        sb.append("0");
        clsList.addElement(sb.toString());
      }
      rootRst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return clsList;
        }
      }
    }
    return clsList;
  }
  /**
   * Returns the confidence information as follows:
   * Each of the classification types will have an entry in the returned hashtable indexed by classification type.
   * The value associated with the classification type is also a Hashtable indexed by protein Id.  The value in the hashtable for protein id will be a hashtable
   * indexed by classification accession.  The value indexed by classification accession will be a vector.  The vector will contain a list of
   * vectors.  Each of the vectors will have 4 entries:  The evidence type, evidence value, confidence code and confidenc sid.
   * @param uplVersion
   * @param family
   * @param clsStr
   * @return
   */
  public Hashtable getClsConfEvidence(String uplVersion, String family, 
                                      String clsStr) {
    Connection con = null;
    Hashtable rsltTbl = null;

    try {
      con = getConnection();
      if (null == con) {
        return null;
      }

      // Determine the classification types
      String[] clsTypes = Utils.tokenize(clsStr, COMMA_DELIM);

      if (0 == clsTypes.length) {
        return null;
      }
      rsltTbl = new Hashtable();
      for (int i = 0; i < clsTypes.length; i++) {
        rsltTbl.put(clsTypes[i], new Hashtable());
      }
      Hashtable current;

      if (null == clsIdToVersionRelease) {
        initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease) {
        return null;
      }


      String query = QueryString.PREPARED_CLS_EVIDENCE_SEQUENCE;

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector clsInfo = (Vector)clsIdToVersionRelease.get(uplVersion);
      String dateStr = (String)clsInfo.elementAt(1);

      if (null != dateStr) {
        query += 
            Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", 
                                "c0");
        query += 
            Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", 
                                "c1");
        query += 
            Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", 
                                "e");
      }
      else {
        query += 
            Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c0");
        query += 
            Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c1");
        query += 
            Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "e");

      }

      //System.out.println(query);
      PreparedStatement stmt = con.prepareStatement(query);

      int uplVersionInt = Integer.parseInt(uplVersion);
      stmt.setInt(1, uplVersionInt);
      stmt.setString(2, family + ":%");
      stmt.setInt(3, uplVersionInt);
      stmt.setInt(4, uplVersionInt);

      if (null != dateStr) {
        stmt.setString(5, dateStr);
        stmt.setString(6, dateStr);
        stmt.setString(7, dateStr);
        stmt.setString(8, dateStr);
        stmt.setString(9, dateStr);
        stmt.setString(10, dateStr);
      }

      ResultSet rst = stmt.executeQuery();
      while (rst.next()){
        String      clsAcc = rst.getString(2);
        //System.out.println("Cls accession is " + clsAcc + " for protein id " + rst.getInt(4) + " with prot ext id " + rst.getString(6) + " " +  rst.getString(7) + " " + rst.getString(9) + " " + rst.getInt(10));
        Enumeration clsKeys = rsltTbl.keys();

        // Determine classification table
        current = null;
        while (clsKeys.hasMoreElements()){
          String  clsKey = (String) clsKeys.nextElement();

          if (true == clsAcc.startsWith(clsKey)){
            current = (Hashtable) rsltTbl.get(clsKey);
            if (null != current){
              break;
            }
          }
        }
        if (null == current){
          continue;
        }
        
        // Get the confidence code and evidence information
        Integer protId = new Integer(rst.getInt(4));
        String confCode = rst.getString(7);
        String evType = rst.getString(9);
        String evValue = rst.getString(8);
        String confSid = Integer.toString(rst.getInt(10));
        
        Hashtable protToClsTbl = (Hashtable)current.get(protId);
        if (null == protToClsTbl) {
          protToClsTbl = new Hashtable();
          current.put(protId, protToClsTbl);
        }
        Vector infoList = (Vector)protToClsTbl.get(clsAcc);
        if (null == infoList) {
          infoList = new Vector();
          protToClsTbl.put(clsAcc, infoList);
        }
        Vector confInfoList = new Vector(4);
        infoList.add(confInfoList);
        confInfoList.add(INDEX_CONF_EVDNCE_EV_TYPE, evType);
        confInfoList.add(INDEX_CONF_EVDNCE_EV_VALUE, evValue);
        confInfoList.add(INDEX_CONF_EVDNCE_CC_TYPE, confCode);
        confInfoList.add(INDEX_CONF_EVDNCE_CC_SID, confSid);        
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se) {
      System.out.println("Unable to retrieve classification evidence information from database, exception " + 
                         se.getMessage() + " has been returned.");
      se.printStackTrace();                         
    }
    finally {
      if (null != con) {
        try {
          con.close();
        }
        catch (SQLException se) {
          System.out.println("Unable to close connection, exception " + 
                             se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return rsltTbl;
  }



  /**
   * Method declaration
   *
   *
   * @param uplVersion
   * @param family
   * @param clsStr
   *
   * @return
   *
   * @see
   */
  public Hashtable getClsForSubfam(String uplVersion, String family, String clsStr){
    Connection  con = null;
    Hashtable   rsltTbl = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }

      // Determine the classification types
      String[]  clsTypes = Utils.tokenize(clsStr, COMMA_DELIM);

      if (0 == clsTypes.length){
        return null;
      }
      rsltTbl = new Hashtable();
      for (int i = 0; i < clsTypes.length; i++){
        rsltTbl.put(clsTypes[i], new Hashtable());
      }
      Hashtable current;

      if (null == clsIdToVersionRelease){
        initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        return null;
      }

      //
      String  query = QueryString.PREPARED_CLS_FOR_SUBFAM;

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "c1");
        query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "c2");
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c1");
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c2");
      }

      //System.out.println(query);
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setString(1, family + ":%");
      stmt.setInt(2, Integer.parseInt(uplVersion));
      stmt.setInt(3, Integer.parseInt(uplVersion));

      // Add values for the date clause
      if (null != dateStr){
        stmt.setString(4, dateStr);
        stmt.setString(5, dateStr);
        stmt.setString(6, dateStr);
        stmt.setString(7, dateStr);
      }

      //System.out.println(query);
      ResultSet rst = stmt.executeQuery();

      while (rst.next()){
        String      clsAcc = rst.getString(3);
        Enumeration clsKeys = rsltTbl.keys();

        // Determine classification table
        current = null;
        while (clsKeys.hasMoreElements()){
          String  clsKey = (String) clsKeys.nextElement();

          if (true == clsAcc.startsWith(clsKey)){
            current = (Hashtable) rsltTbl.get(clsKey);
            if (null != current){
              break;
            }
          }
        }
        if (null == current){
          continue;
        }

        // Add the classification information for the subfamily
        Integer sfId = new Integer(rst.getInt(1));
        String  clsName = rst.getString(2);
        String  sfCls = (String) current.get(sfId);

        if (null == sfCls){
          sfCls = "";
        }
        sfCls += clsAcc + LINKS_ASSOC + clsName + SEMI_COLON_DELIM;
        current.put(sfId, sfCls);
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve classification information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return rsltTbl;
  }

  /**
   * Method declaration
   *
   *
   * @param uplVersion
   * @param family
   * @param clsStr
   *
   * @return
   *
   * @see
   */
  public Hashtable getPreviousSfNameCat(String uplVersion, String family, String clsStr){
    Connection  con = null;
    Hashtable   rsltTbl = null;
    Hashtable   seqToSfTable = new Hashtable();
    Hashtable   sfToNameTable = new Hashtable();
    Hashtable   catTable = new Hashtable();

    try{
      con = getConnection();
      if (null == con){
        return null;
      }

      // Determine the classification types
      String[]  clsTypes = Utils.tokenize(clsStr, COMMA_DELIM);

      if (0 == clsTypes.length){
        return null;
      }
      for (int i = 0; i < clsTypes.length; i++){
        catTable.put(clsTypes[i], new Hashtable());
      }
      String            query = QueryString.PREPARED_PREVIOUS_SF_CAT;
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setString(1, family);

      // Current upl version
      stmt.setInt(2, Integer.parseInt(uplVersion));

      // Previous upl version
      stmt.setInt(3, Integer.parseInt(uplVersion) - 1);
      ResultSet rst = stmt.executeQuery();

      while (rst.next()){
        Integer protId = new Integer(rst.getInt(1));
        Vector  sfList = (Vector) seqToSfTable.get(protId);

        if (null == sfList){
          sfList = new Vector();
          seqToSfTable.put(protId, sfList);
        }
        String  sfAcc = rst.getString(3);
        String  sfName = rst.getString(4);

        // Only add unique subfamilies
        boolean sfFound = false;

        for (int i = 0; i < sfList.size(); i++){
          if (0 == ((String) sfList.elementAt(i)).compareTo(sfAcc)){
            sfFound = true;
          }
        }
        if (false == sfFound){
          sfList.addElement(sfAcc);
        }
        sfToNameTable.put(sfAcc, sfName);
        String    catAcc = rst.getString(6);
        String    catName = rst.getString(7);
        Hashtable current = null;

        for (int i = 0; i < clsTypes.length; i++){
          if (catAcc.startsWith(clsTypes[i])){
            current = (Hashtable) catTable.get(clsTypes[i]);
            break;
          }
        }
        if (null == current){
          System.out.println("Error cannot find category type " + catAcc);
          continue;
        }
        String  catLinks = (String) current.get(sfAcc);

        if (null == catLinks){
          catLinks = new String();
        }
        if (0 != catLinks.length()){
          catLinks += COMMA_DELIM;
        }

        // Only add if it is not already in the list
        if (-1 == catLinks.indexOf(catName)){
          catLinks += catName;
          current.put(sfAcc, catLinks);
        }
      }
      rst.close();
      stmt.close();

      // Combine the results
      rsltTbl = new Hashtable();
      Enumeration protIds = seqToSfTable.keys();

      while (protIds.hasMoreElements()){
        Integer       protId = (Integer) protIds.nextElement();
        StringBuffer  sb = new StringBuffer();
        Vector        sfList = (Vector) seqToSfTable.get(protId);

        for (int j = 0; j < sfList.size(); j++){
          String  sfAcc = (String) sfList.elementAt(j);

          sb.append(sfToNameTable.get(sfAcc));
          sb.append("-");
          for (int i = 0; i < clsTypes.length; i++){
            Hashtable sfCat = (Hashtable) catTable.get(clsTypes[i]);
            String    cat = (String) sfCat.get(sfAcc);

            if (null == cat){
              continue;
            }
            sb.append(clsTypes[i]);
            sb.append(LINKS_ASSOC);
            sb.append(cat);
            sb.append(SEMI_COLON_DELIM);
          }
          sb.append(NEWLINE_DELIM);
        }
        rsltTbl.put(protId, sb.toString());
      }
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return rsltTbl;
  }

  /**
   * Method declaration
   *
   *
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public String[] getBookList(String uplVersion){
    Connection  con = null;
    String[]    bookList = null;

    if (null == bookListTbl){
      bookListTbl = new Hashtable();
    }
    bookList = (String[]) bookListTbl.get(uplVersion);
    if (null != bookList){
      return bookList;
    }
    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      if (null == clsIdToVersionRelease){
        initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        return null;
      }
      String  query = QueryString.PREPARED_BOOK_LIST;

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "c");
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setInt(1, Integer.parseInt(uplVersion));
      int depth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_famLevel"));

      stmt.setInt(2, depth);

      // Add values for the date clause
      if (null != dateStr){
        stmt.setString(3, dateStr);
        stmt.setString(4, dateStr);
      }

      ResultSet rst = stmt.executeQuery();
      Vector    bookRslt = new Vector();

      while (rst.next()){
        bookRslt.addElement(rst.getString(1));
      }
      rst.close();
      stmt.close();
      bookList = new String[bookRslt.size()];
      bookRslt.copyInto(bookList);
      java.util.Arrays.sort(bookList);
      bookListTbl.put(uplVersion, bookList);
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
        return bookList;
      }
    }
    return bookList;
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public String[] getBookListForLocking(String userName, String password, String uplVersion){
    Connection  con = null;
    String[]    bookList = null;

    // Get the userid and determine if user can lock books
    String      userId = getUserId(userName, password);

    if (null == userId){
      return null;
    }
    initUserIdToRankLookup();
    if (null == userIdToRankLookup){
      return null;
    }

    // Get user rank and curator rank.
    String  userRank = (String) userIdToRankLookup.get(userId);
    String  curatorRank = ConfigFile.getProperty("panther_curator_rank");

    // Before comparing, convert to integer since a string comparison will not return valid results
    int     usRank = Integer.parseInt(userRank);
    int     curRank = Integer.parseInt(curatorRank);

    if (usRank < curRank){
      return null;
    }
    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      String            query = QueryString.PREPARED_LOCKING_BOOK_LIST;
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setInt(1, Integer.parseInt(uplVersion));
      int depth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_famLevel"));

      stmt.setInt(2, depth);
      int checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));

      stmt.setInt(3, checkOut);

      //System.out.println(query);
      ResultSet rst = stmt.executeQuery();
      Vector    bookRslt = new Vector();

      while (rst.next()){

        // System.out.println(rst.getString(1));
        bookRslt.addElement(rst.getString(1));
      }
      rst.close();
      stmt.close();
      bookList = new String[bookRslt.size()];
      bookRslt.copyInto(bookList);
      java.util.Arrays.sort(bookList);
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
        return bookList;
      }
    }
    return bookList;
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public String[] getBookListForUnLocking(String userName, String password, String uplVersion){
    Connection  con = null;
    String[]    bookList = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      String  userIdStr = getUserId(userName, password);

      if (null == userIdStr){
        return null;
      }
      String            query = QueryString.PREPARED_UNLOCKING_BOOK_LIST;
      int               checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setInt(1, Integer.parseInt(uplVersion));
      stmt.setInt(2, Integer.parseInt(userIdStr));
      stmt.setInt(3, checkOut);
      int depth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_famLevel"));

      stmt.setInt(4, depth);

      //System.out.println(query);
      ResultSet rst = stmt.executeQuery();
      Vector    bookRslt = new Vector();

      while (rst.next()){

        // System.out.println(rst.getString(1));
        bookRslt.addElement(rst.getString(1));
      }
      rst.close();
      stmt.close();
      bookList = new String[bookRslt.size()];
      bookRslt.copyInto(bookList);
      java.util.Arrays.sort(bookList);
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
        return bookList;
      }
    }
    return bookList;
  }
  
  
    public String unlockBooks(String userName, String password, String uplVersion, Vector bookList){
        Vector failedUnLocks = new Vector();
        for (int i = 0; i < bookList.size(); i++) {
            String currentBook = (String)bookList.get(i);
            String unLockMsg = unlockBook(userName, password, uplVersion, currentBook);
            if (false == unLockMsg.equals(MSG_SUCCESS)) {
                failedUnLocks.add(currentBook);
            }
        }
        if (0 != failedUnLocks.size()) {
            return MSG_ERROR_UNLOCKING_BOOKS + Utils.listToString(failedUnLocks, Constant.STR_EMPTY, Constant.STR_COMMA);
        }
        else {
            return Constant.STR_EMPTY;
        }
    }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param book
   *
   * @return
   *
   * @see
   */
  public String unlockBook(String userName, String password, String uplVersion, String book){
    String  userIdStr = getUserId(userName, password);

    if (null == userIdStr){
      return "User name cannot be found in database";
    }
    String  clsIdStr = getClsIdForBookLockedByUser(userIdStr, uplVersion, book);

    if (null == clsIdStr){
      return "Book is not locked by user";
    }

    // Attempt to unlock book for user
    return unlockBook(userIdStr, clsIdStr);
  }

  /**
   * Method declaration
   *
   *
   * @param userId
   * @param clsId
   *
   * @return
   *
   * @see
   */
  protected String unlockBook(String userId, String clsId){
    Connection  con = null;
    boolean     successfulUnlock = false;

    try{
      con = getConnection();
      if (null == con){
        return "Cannot get database connection to unlock book for user";
      }
      PreparedStatement stmt = con.prepareStatement(UpdateString.PREPARED_BOOK_UNLOCK);

      stmt.setInt(1, Integer.parseInt(clsId));
      stmt.setInt(2, Integer.parseInt(userId));
      stmt.setInt(3, Integer.parseInt(ConfigFile.getProperty("panther_check_out")));

      //System.out.println(UpdateString.PREPARED_BOOK_UNLOCK);
      stmt.executeUpdate();
      stmt.close();
      successfulUnlock = true;
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    if (true == successfulUnlock){
      return Constant.STR_EMPTY;
    }
    else{
      return "Unable to unlock book for user";
    }
  }

  /**
   * Method declaration
   *
   *
   * @param userId
   * @param uplVersion
   * @param book
   *
   * @return
   *
   * @see
   */
  protected String getClsIdForBookLockedByUser(String userId, String uplVersion, String book){
    Integer clsId = null;

    if (0 == userId.length()){
      return null;
    }
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      int               checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));
      PreparedStatement stmt = con.prepareStatement(QueryString.PREPARED_CLSID_FOR_BOOK_LOCKED_BY_USER);

      stmt.setInt(1, Integer.parseInt(userId));
      stmt.setInt(2, checkOut);
      stmt.setInt(3, Integer.parseInt(uplVersion));
      stmt.setString(4, book);

      //System.out.println(QueryString.PREPARED_CLSID_FOR_BOOK_LOCKED_BY_USER);
      ResultSet rst = stmt.executeQuery();

      while (rst.next()){
        clsId = new Integer(rst.getInt(1));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    if (null != clsId){
      return clsId.toString();
    }
    else{
      return null;
    }
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param book
   *
   * @return
   *
   * @see
   */
  public String lockBook(String userName, String password, String uplVersion, String book){
    String  userIdStr = getUserId(userName, password);

    if (null == userIdStr){
      return "Cannot verify user information";
    }
    String  clsId = getClsIdForBookToLock(userIdStr, uplVersion, book);

    if (null == clsId){
      return "Cannot lock book for user";
    }
    return lockBook(clsId, userIdStr, null);
  }
  
  

  /**
   * Method declaration
   *
   *
   * @param userId
   * @param uplVersion
   * @param book
   *
   * @return
   *
   * @see
   */
  protected String getClsIdForBookToLock(String userId, String uplVersion, String book){
    Integer clsId = null;

    if (0 == userId.length()){
      return null;
    }
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      int               checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));
      PreparedStatement stmt = con.prepareStatement(QueryString.PREPARED_CLSID_FOR_BOOK_USER_LOCK);

      stmt.setInt(1, Integer.parseInt(uplVersion));
      stmt.setString(2, book);
      stmt.setInt(3, checkOut);

      //System.out.println(QueryString.PREPARED_CLSID_FOR_BOOK_USER_LOCK);
      ResultSet rst = stmt.executeQuery();

      if (rst.next()){
        clsId = new Integer(rst.getInt(1));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    if (null == clsId){
      return null;
    }
    else{
      return clsId.toString();
    }
  }
  


    public String lockBooks(String userName, String password, String uplVersion, Vector bookList) {

        if (null == userName || null == password || null == uplVersion || null == bookList ||
            0 == userName.length() || 0 == password.length() || 0 == uplVersion.length() || 0 == bookList.size()) {
                return MSG_ERROR_INVALID_INFO_FOR_LOCKING_BOOKS;     
            }

        String userId = getUserId(userName, password);
        String errorMsg = checkUserCanLockorUnlockBooks(userId);
        // User can lock books, attempt to lock the books
        if (null != errorMsg) {
            return errorMsg;
        }
        
        // Lock books for user
        // Get clsids for books
        Vector clsIds = getClsIdsForBooksToLock(uplVersion, bookList);
        if (null == clsIds) {
            return MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER;            
        }
        
        errorMsg = lockBooks(clsIds, userId);
        if (true != errorMsg.equals(MSG_SUCCESS)) {
            return errorMsg;            
        }
        return MSG_SUCCESS;
        

    }
    
    protected String checkUserCanLockorUnlockBooks(String userId) {


        if (null == userId){
          return MSG_ERROR_UNABLE_TO_VERIFY_USER;
        }
        initUserIdToRankLookup();
        if (null == userIdToRankLookup){
          return MSG_ERROR_UNABLE_TO_RETRIEVE_USER_STATUS_INFO;
        }

        // Get user rank and curator rank.
        String  userRank = (String) userIdToRankLookup.get(userId);
        String  curatorRank = ConfigFile.getProperty(RANK_PANTHER_CURATOR);

        // Before comparing, convert to integer since a string comparison will not return valid results
        int     usRank = Integer.parseInt(userRank);
        int     curRank = Integer.parseInt(curatorRank);

        if (usRank < curRank){
          return MSG_ERROR_USER_DOES_NOT_HAVE_PRIVILEGE_TO_LOCK_BOOKS;
        }
        return null;
        
    }

    protected String lockBooks(Vector bookClsIds, String userId) {
        Connection updateCon = null;
        try {
            updateCon = getConnection();
            if (null == updateCon) {
                return MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER;
            }
            updateCon.setAutoCommit(false);
            updateCon.rollback();
            boolean failedOperation = false;
            for (int i = 0; i < bookClsIds.size(); i++) {
                String errorMsg = lockBook((String)bookClsIds.elementAt(i), userId, updateCon);
                if (false == errorMsg.equals(MSG_SUCCESS)) {
                    failedOperation = true;
                    break;
                }
            }
            
            if (true == failedOperation) {
                updateCon.rollback();    
            }
            else {

                // Success
                //Testing
                //updateCon.rollback();
                updateCon.commit();
            }

        } catch (Exception e) {
            System.out.println("Unable to save information from database, exception " +
                               e.getMessage() + " has been returned.");
            e.printStackTrace();
            try {
                if (null != updateCon) {
                    updateCon.rollback();
                }
                return MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER;
            } catch (SQLException se) {
                se.printStackTrace();
                System.out.println("Exception while rollback");
                return MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER;
            }
        } finally {
            if (null != updateCon) {
                try {
                    updateCon.close();
                } catch (SQLException se) {
                    System.out.println("Unable to close connection, exception " +
                                       se.getMessage() +
                                       " has been returned.");
                    se.printStackTrace();
                    return MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER;
                }
            }
            else {
                return MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER;
            }
            return MSG_SUCCESS;
        }


    }
    
    public Vector getMyBooks(String userName, String password, String uplVersion) {
        String userId = getUserId(userName, password);
        if (null == userId) {
            return null;
        }
        return getLockedBooks(userId, uplVersion);
    }
    
    protected Vector getLockedBooks(String userId, String uplVersion) {
        if (null == userId){
          return null;
        }
        Connection  con = null;
        Vector    bookRslt = new Vector();

        try{
          con = getConnection();
          if (null == con){
            return null;
          }



          String            query = QueryString.PREPARED_UNLOCKING_BOOK_LIST;
          int               checkOut = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_CHECKOUT));
          PreparedStatement stmt = con.prepareStatement(query);

          stmt.setInt(1, Integer.parseInt(uplVersion));
          stmt.setInt(2, Integer.parseInt(userId));
          stmt.setInt(3, checkOut);
          int depth = Integer.parseInt(ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));

          stmt.setInt(4, depth);

          //System.out.println(query);
          ResultSet rst = stmt.executeQuery();


          while (rst.next()){
            String bookId = rst.getString(1);
            String bookName = rst.getString(2);
            int status = rst.getInt(3);
            
            // Get information about user
            String firstNameLName = rst.getString(5);
            String email = rst.getString(COLUMN_NAME_EMAIL);
            String loginName = rst.getString(COLUMN_NAME_LOGIN_NAME);
            int rank = rst.getInt(COLUMN_NAME_PRIVILEGE_RANK);
            String groupName = rst.getString(COLUMN_NAME_GROUP_NAME);
            User u = new User(firstNameLName, null, email, loginName, rank, groupName);
            int statusConversion = getCurationStatusConversion(status);
            Book aBook = new Book(bookId, bookName, statusConversion, u);

            bookRslt.addElement(aBook);
          }
          rst.close();
          stmt.close();

        }
        catch (SQLException se){
          System.out.println("Unable to retrieve mybooks information from database, exception " + se.getMessage()
                             + " has been returned.");
        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            catch (SQLException se){
              System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
            }
            return bookRslt;
          }
        }
        return bookRslt;
        }


    protected Vector getClsIdsForBooksToLock(String uplVersion, Vector books){

      Connection  con = null;
      if (null == books || 0 == books.size()) {
          return null;
      }
      Vector returnInfo = new Vector();
      String bookStr = StringUtils.listToString(books, QUOTE, COMMA_DELIM);
      try{
        con = getConnection();
        if (null == con){
          return null;
        }
        int               checkOut = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_CHECKOUT));
        String query = QueryString.PREPARED_CLSIDS_FOR_BOOKS_USER_LOCK;
        query = Utils.replace(query, REPLACE_STR_PERCENT_1, bookStr);
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setInt(1, Integer.parseInt(uplVersion));
        stmt.setInt(2, checkOut);

        System.out.println(query);
        ResultSet rst = stmt.executeQuery();

        while (rst.next()){
          returnInfo.add(Integer.toString(rst.getInt(1)));
        }
        rst.close();
        stmt.close();
      }
      catch (SQLException se){
        System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                           + " has been returned.");
      }
      finally{
        if (null != con){
          try{
            con.close();
          }
          catch (SQLException se){
            System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
            return null;
          }
        }
      }

      return returnInfo;

    }




  /**
   * updateConnection - Specify connection object or null
   */
  protected String lockBook(String clsId, String userId, Connection updateConnection){
    Connection  con = null;
    boolean     successfulLock = false;

    try{
      if (null == updateConnection){
        con = getConnection();
        if (null == con){
          return "Cannot get database connection to lock book for user";
        }
      }
      else{
        con = updateConnection;
      }
      PreparedStatement stmt = con.prepareStatement(UpdateString.PREPARED_BOOK_LOCK);
      int               checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));

      stmt.setInt(1, checkOut);
      stmt.setInt(2, Integer.parseInt(clsId));
      stmt.setInt(3, Integer.parseInt(userId));

      //System.out.println(UpdateString.PREPARED_BOOK_LOCK);
      stmt.executeUpdate();
      stmt.close();
      successfulLock = true;
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{

      // Only close the connection, if this method created the connection object
      if ((null != con) && (null == updateConnection)){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    if (true == successfulLock){
      return MSG_SUCCESS;
    }
    else{
      return "Unable to lock book for user";
    }
  }

  /**
   * Method declaration
   *
   *
   * @param userId
   *
   * @return
   *
   * @see
   */
  private int getUserPrivilegeLevel(String userId){
    Connection  con = null;
    int         userPrivilege = -1;

    // check whether the user has enough privilege to do the curation
    try{
      con = getConnection();
      PreparedStatement stmt = con.prepareStatement(QueryString.USER_PRIVILEGE);

      stmt.setInt(1, Integer.parseInt(userId));
      ResultSet rst = stmt.executeQuery();

      if (rst.next()){
        userPrivilege = rst.getInt(1);
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve the user privilege from the database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return userPrivilege;
  }

  /**
   * Method declaration
   *
   *
   * @param userIdStr
   * @param refIdStr
   * @param isSequence
   *
   * @return
   *
   * @see
   */
  private String unlockSequenceOrClassification(String userIdStr, String refIdStr, boolean isSequence){
    Connection  con = null;
    int         updateCount = 0;
    String      sql = UpdateString.PREPARED_SEQUENCE_UNLOCK;

    if (!isSequence){
      sql = UpdateString.PREPARED_CLASSIFICATION_UNLOCK;
    }
    try{
      con = getConnection();
      if (null == con){
        return "Cannot get database connection to unlock sequence for user";
      }
      PreparedStatement stmt = con.prepareStatement(sql);

      stmt.setLong(1, Long.parseLong(refIdStr));
      stmt.setInt(2, Integer.parseInt(userIdStr));
      stmt.setInt(3, Integer.parseInt(ConfigFile.getProperty("panther_check_out")));
      updateCount = stmt.executeUpdate();
      stmt.close();
      System.out.println("the sequence/classification " + refIdStr + " is unlocked");
    }
    catch (SQLException se){
      System.out.println("Unable to unlock the sequence, exception " + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param userId
   * @param userPrivilegeLevel
   * @param refId
   * @param isSequence
   *
   * @return
   *
   * @see
   */
  private Vector getEvidences(String userId, int userPrivilegeLevel, String refId, boolean isSequence){
    Vector      allEvidences = new Vector();
    Hashtable   nonEditableEvidences = new Hashtable();
    Hashtable   editableEvidences = new Hashtable();
    Connection  con = null;
    String      query = QueryString.SEQUENCE_EVIDENCE;

    if (!isSequence){
      query = QueryString.CLASSIFICATION_EVIDENCE;
    }
    try{
      con = getConnection();
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setLong(1, Long.parseLong(refId));
      ResultSet rst = stmt.executeQuery();

      while (rst.next()){
        String  evidenceType = rst.getString("type");
        String  createdBy = rst.getString("created_by");
        String  evidence = rst.getString("evidence");

        if (userPrivilegeLevel > Integer.parseInt(ConfigFile.getProperty("panther_curator_rank"))
                || userId.equals(createdBy)){     // editable evidence
          Vector  temp = (Vector) editableEvidences.get(evidenceType);

          if (temp == null){
            temp = new Vector();
            editableEvidences.put(evidenceType, temp);
          }
          temp.addElement(evidence);
        }
        else{                                     // oneditable evidence
          Vector  temp = (Vector) nonEditableEvidences.get(evidenceType);

          if (temp == null){
            temp = new Vector();
            nonEditableEvidences.put(evidenceType, temp);
          }
          temp.addElement(evidence);
        }
      }
      rst.close();
      stmt.close();
      allEvidences.addElement(nonEditableEvidences);
      allEvidences.addElement(editableEvidences);

      // test whether the evidence are correct
      Hashtable temp1 = (Hashtable) allEvidences.elementAt(0);
      Hashtable temp2 = (Hashtable) allEvidences.elementAt(1);

      System.out.println("NonEditable evidence are: ");
      if (temp1 != null && temp1.size() > 0){
        for (Enumeration enu = temp1.keys(); enu.hasMoreElements(); ){
          String  type = (String) enu.nextElement();
          Vector  tmp = (Vector) temp1.get(type);

          for (int i = 0; i < tmp.size(); i++){
            System.out.println(type + ':' + tmp.elementAt(i));
          }
        }
      }
      System.out.println("Editable evidence are: ");
      if (temp2 != null && temp2.size() > 0){
        for (Enumeration enu = temp2.keys(); enu.hasMoreElements(); ){
          String  type = (String) enu.nextElement();
          Vector  tmp = (Vector) temp2.get(type);

          for (int i = 0; i < tmp.size(); i++){
            System.out.println(type + ':' + tmp.elementAt(i));
          }
        }
      }
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve sequence evidence, exception " + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return allEvidences;
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param seqAcc
   *
   * @return
   *
   * @see
   */
  public String unlockSequence(String userName, String password, String uplVersion, String seqAcc){
    Connection  con = null;
    int         updateCount = 0;
    String      userIdStr = getUserId(userName, password);

    if (userIdStr == null){
      return "Cannot verify user information";
    }
    int userPrivilege = getUserPrivilegeLevel(userIdStr);

    if (userPrivilege < Integer.parseInt(ConfigFile.getProperty("panther_curator_rank"))){
      return "The user do not have wirte privilege.";
    }

    // Make sure upl has not already been released
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      return "Error accessing data from database";
    }

    // If this version of the upl has been released, then add clause to ensure only records
    // created prior to the release date are retrieved
    Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);

    if (null == clsInfo){
      return "Invalid UPL version specified.";
    }
    if (null != clsInfo.elementAt(1)){
      return "UPL has already been released, changes can no-longer be saved.";
    }

    String  seqId = getRefId(uplVersion, seqAcc, true);

    if (seqId == null){
      return "The sequence " + seqAcc + " is not in the database/not editable.";
    }
    String  msg = checkUnlockable(userIdStr, seqId, true);

    if (msg != null && msg.length() > 0){
      return msg;
    }
    msg = unlockSequenceOrClassification(userIdStr, seqId, true);
    if (msg != null && msg.length() > 0){
      return msg;
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param clsAcc
   *
   * @return
   *
   * @see
   */
  public String unlockClassification(String userName, String password, String uplVersion, String clsAcc){
    Connection  con = null;
    int         updateCount = 0;
    String      userIdStr = getUserId(userName, password);

    if (userIdStr == null){
      return "Cannot verify user information";
    }
    int userPrivilege = getUserPrivilegeLevel(userIdStr);

    if (userPrivilege < Integer.parseInt(ConfigFile.getProperty("panther_curator_rank"))){
      return "The user do not have wirte privilege.";
    }

    // Make sure upl has not already been released
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      return "Error accessing data from database";
    }

    // If this version of the upl has been released, then add clause to ensure only records
    // created prior to the release date are retrieved
    Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);

    if (null == clsInfo){
      return "Invalid UPL version specified.";
    }
    if (null != clsInfo.elementAt(1)){
      return "UPL has already been released, changes can no-longer be saved.";
    }

    String  clsId = getRefId(uplVersion, clsAcc, false);

    if (clsId == null){
      return "The sequence " + clsAcc + " is not in the database/not editable.";
    }
    String  msg = checkUnlockable(userIdStr, clsId, false);

    if (msg != null && msg.length() > 0){
      return msg;
    }
    msg = unlockSequenceOrClassification(userIdStr, clsId, false);
    if (msg != null && msg.length() > 0){
      return msg;
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param seqAcc
   * @param familyAcc
   * @param evidences
   *
   * @return
   *
   * @see
   */
  public String saveEvidenceAndUnlockSequence(String userName, String password, String uplVersion,
          String seqAcc, String familyAcc, Vector evidences){
    Connection  con = null;
    int         updateCount = 0;
    String      userIdStr = getUserId(userName, password);

    if (null == userIdStr){
      return "Cannot verify user information";
    }
    System.out.println("user ID is " + userIdStr);
    int privilegeLevel = getUserPrivilegeLevel(userIdStr);

    if (privilegeLevel == -1){
      return "The user privilege as not been defined";
    }
    System.out.println("user privilege is " + privilegeLevel);
    if (privilegeLevel < Integer.parseInt(ConfigFile.getProperty("panther_curator_rank"))){
      System.out.println("The user do not have write privilege.");
      return "The user do not have wirte privilege.";
    }

    // Make sure upl has not already been released
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      return "Error accessing data from database";
    }

    // If this version of the upl has been released, then add clause to ensure only records
    // created prior to the release date are retrieved
    Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);

    if (null == clsInfo){
      return "Invalid UPL version specified.";
    }
    if (null != clsInfo.elementAt(1)){
      return "UPL has already been released, changes can no-longer be saved.";
    }

    // get sequence ID
    String  seqId = getRefId(uplVersion, seqAcc, true);

    if (seqId == null){
      return "The sequence " + seqAcc + " is not in the database.";
    }
    System.out.println("sequence ID is " + seqId);
    String  msg = checkUnlockable(userIdStr, seqId, true);

    if (msg != null){
      return msg;
    }

    // get family ID
    String  familyId = getRefId(uplVersion, familyAcc, false);

    if (familyId == null){
      return "The family " + familyAcc + " is not in the database.";
    }
    System.out.println("family ID is " + familyId);
    msg = saveEvidences(evidences, userIdStr, seqId, true, familyId);
    if (msg != null){
      return msg;
    }
    msg = unlockSequenceOrClassification(userIdStr, seqId, true);
    if (msg != null){
      return msg;
    }
    System.out.println("Sequence " + seqId + " is unlocked");
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param subfamilyAcc
   * @param familyAcc
   * @param evidences
   *
   * @return
   *
   * @see
   */
  public String saveEvidenceAndUnlockSubfamily(String userName, String password, String uplVersion,
          String subfamilyAcc, String familyAcc, Vector evidences){
    System.out.println("inside saveEvidenceAndUnlockSubfamily");
    Connection  con = null;
    int         updateCount = 0;
    String      userIdStr = getUserId(userName, password);

    if (null == userIdStr){
      return "Cannot verify user information";
    }
    System.out.println("user ID is " + userIdStr);
    int privilegeLevel = getUserPrivilegeLevel(userIdStr);

    if (privilegeLevel == -1){
      return "The user privilege as not been defined";
    }
    System.out.println("user privilege is " + privilegeLevel);
    if (privilegeLevel < Integer.parseInt(ConfigFile.getProperty("panther_curator_rank"))){
      System.out.println("The user do not have write privilege.");
      return "The user do not have wirte privilege.";
    }

    // Make sure upl has not already been released
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      return "Error accessing data from database";
    }

    // If this version of the upl has been released, then add clause to ensure only records
    // created prior to the release date are retrieved
    Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);

    if (null == clsInfo){
      return "Invalid UPL version specified.";
    }
    if (null != clsInfo.elementAt(1)){
      return "UPL has already been released, changes can no-longer be saved.";
    }

    // get subfamily id
    String  clsId = getRefId(uplVersion, subfamilyAcc, false);

    if (clsId == null){
      return "The sequence " + subfamilyAcc + " is not in the database.";
    }
    System.out.println("sequence ID is " + clsId);

    /*
     * String msg = checkUnlockable(userIdStr, clsId, false);
     * if (msg != null){
     * return msg;
     * }
     */

    // get family ID
    String  familyId = getRefId(uplVersion, familyAcc, false);

    if (familyId == null){
      return "The family " + familyAcc + " is not in the database.";
    }
    System.out.println("family ID is " + familyId);
    System.out.println("Before saveEvidence in dataServer");
    String  msg = saveEvidences(evidences, userIdStr, clsId, false, familyId);

    if (msg != null){
      System.out.println(msg);
      return msg;
    }

    /*
     * // DO NOT NEED TO UNLOCK SUBFAMILY ANYMORE
     * msg = unlockSequenceOrClassification(userIdStr, clsId, false);
     * if (msg != null){
     * return msg;
     * }
     * System.out.println("Sequence " + clsId +" is unlocked");
     */
    return "";
  }

  /**
   * The Vector evidences store the changes for the evidence
   * elementAt(0) is for new evidence
   * elementAt(1) is for obsoleted evidence
   * refID is either subfamily or sequence ID
   */
  private String saveEvidences(Vector evidences, String userIdStr, String refIdStr, boolean isSequence,
                                      String familyID){
    System.out.println("Inside saveEvdiences");
    if (evidences == null || evidences.size() != 2){
      return "Invalid input for saving evidences.";
    }
    Hashtable         newEvidences = (Hashtable) evidences.elementAt(0);
    Hashtable         obsoletedEvidences = (Hashtable) evidences.elementAt(1);
    Connection        con = null;
    PreparedStatement stmt = null;
    String            msg = null;

    try{
      con = getConnection();
      if (null == con){
        return "Cannot get database connection to update the evidence";
      }
      int userId = Integer.parseInt(userIdStr);

      if (newEvidences != null && newEvidences.size() > 0){
        String  sql = UpdateString.PREPARED_ADD_SEQUENCE_EVIDENCE;

        if (!isSequence){
          sql = UpdateString.PREPARED_ADD_CLASSIFICATION_EVIDENCE;
        }
        System.out.println("inside newEvidence: \n" + sql);
        stmt = con.prepareStatement(sql);
        long  refId = Long.parseLong(refIdStr);

        for (Enumeration enu = newEvidences.keys(); enu.hasMoreElements(); ){
          String  type = (String) enu.nextElement();
          int     typeId = Integer.parseInt(ConfigFile.getProperty(type));

          System.out.println("evidence type id is " + typeId);
          Vector  currEvidences = (Vector) newEvidences.get(type);

          if (currEvidences != null && currEvidences.size() > 0){
            int size = currEvidences.size();

            for (int i = 0; i < size; i++){
              stmt.setInt(1, typeId);
              stmt.setLong(2, refId);
              System.out.println("add new evidence:" + (String) currEvidences.elementAt(i));
              stmt.setString(3, (String) currEvidences.elementAt(i));
              stmt.setInt(4, userId);
              stmt.addBatch();
            }
          }
        }
        java.text.SimpleDateFormat  df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");

        System.out.println("Start of add evidence " + df.format(new java.util.Date(System.currentTimeMillis())));
        stmt.executeBatch();
        System.out.println("End of add evidence " + df.format(new java.util.Date(System.currentTimeMillis())));
        stmt.close();
      }
      if (obsoletedEvidences != null && obsoletedEvidences.size() > 0){
        String  sql = UpdateString.PREPARED_DELETE_SEQUENCE_EVIDENCE;

        if (!isSequence){
          sql = UpdateString.PREPARED_DELETE_CLASSIFICATION_EVIDENCE;
        }
        System.out.println("inside obsoletedEvidence: \n" + sql);
        stmt = con.prepareStatement(sql);
        long  refId = Long.parseLong(refIdStr);

        for (Enumeration enu = obsoletedEvidences.keys(); enu.hasMoreElements(); ){
          String  type = (String) enu.nextElement();
          int     typeId = Integer.parseInt(ConfigFile.getProperty(type));

          System.out.println("evidence type id is " + typeId);
          Vector  currEvidences = (Vector) obsoletedEvidences.get(type);

          if (currEvidences != null && currEvidences.size() > 0){
            int size = currEvidences.size();

            for (int i = 0; i < size; i++){
              stmt.setInt(1, userId);
              stmt.setLong(2, refId);
              System.out.println("obsoleted old evidence:" + (String) currEvidences.elementAt(i));
              stmt.setString(3, (String) currEvidences.elementAt(i));
              stmt.setInt(4, typeId);
              stmt.addBatch();
            }
          }
        }
        java.text.SimpleDateFormat  df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");

        System.out.println("Start of obsolete evidence " + df.format(new java.util.Date(System.currentTimeMillis())));
        stmt.executeBatch();
        System.out.println("end of obsolete evidence " + df.format(new java.util.Date(System.currentTimeMillis())));
        stmt.close();
      }

      // set the curation status now.
      msg = UpdateCurtionStatus(con, new Integer(familyID),
                                Integer.parseInt(ConfigFile.getProperty("panther_partially_curated")), userIdStr);
      if (msg != null && msg.length() == 0){
        msg = null;
      }
    }
    catch (SQLException se){
      System.out.println("SQL exception in saveEvidences()" + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return msg;
  }

  /**
   * Check whether the sequence/family/subfamily is available for locking
   */
  private String checkUnlockable(String userId, String refId, boolean isSequence){
    Connection        con = null;
    PreparedStatement stmt = null;
    ResultSet         rst = null;
    String            query = QueryString.SEQUENCE_CURRENT_LOCKING_STATUS;

    if (!isSequence){
      query = QueryString.CLASSIFICATION_CURRENT_LOCKING_STATUS;
    }
    try{
      con = getConnection();
      if (null == con){
        return "Cannot get database connection to lock sequence for user";
      }
      if (refId != null){
        stmt = con.prepareStatement(query);
        stmt.setLong(1, Long.parseLong(refId));
        stmt.setInt(2, Integer.parseInt(ConfigFile.getProperty("panther_check_out")));
        rst = stmt.executeQuery();
        if (rst.next()){    // the sequence already been locked, check whether it is locked by the same user
          int userIdLocked = rst.getInt(1);

          if (Integer.parseInt(userId) != userIdLocked){
            rst.close();
            stmt.close();
            return "Locked by another user.";
          }
        }
        else{               // the sequence/classification is not locked
          rst.close();
          stmt.close();
          return "Not locked.";
        }
        rst.close();
        stmt.close();
      }
      else{
        return "Internal error. Please report the bug to system admin.";
      }
    }
    catch (SQLException se){
      System.out.println("SQL exception in checkUnlockable()" + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return null;
  }

  /**
   * Method declaration
   *
   *
   * @param userIdStr
   * @param refId
   * @param isSequence
   *
   * @return
   *
   * @see
   */
  private String lockSequenceOrClassification(String userIdStr, String refId, boolean isSequence){
    Connection  con = null;
    int         updateCount = 0;
    String      sql = UpdateString.PREPARED_SEQUENCE_LOCK;

    if (!isSequence){
      sql = UpdateString.PREPARED_CLASSIFICATION_LOCK;
    }
    try{
      con = getConnection();
      if (null == con){
        return "Cannot get database connection to lock sequence for user";
      }
      PreparedStatement stmt = con.prepareStatement(sql);

      stmt.setLong(1, Long.parseLong(refId));
      stmt.setInt(2, Integer.parseInt(userIdStr));
      stmt.setInt(3, Integer.parseInt(ConfigFile.getProperty("panther_check_out")));
      updateCount = stmt.executeUpdate();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to lock the sequence, exception " + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    if (updateCount == 1){
      return null;
    }
    else{
      return "Unable to lock sequence ";
    }
  }

  /**
   * return a vector: first element is the error message, the second is evidences returned if no error
   */
  public Vector lockSequenceAndGetEvidences(String userName, String password, String uplVersion, String seqAcc){
    Connection  con = null;
    int         updateCount = 0;
    Vector      result = new Vector();
    String      userIdStr = getUserId(userName, password);

    if (null == userIdStr){
      result.addElement("Cannot verify user information");
      return result;
    }
    System.out.println("user ID is " + userIdStr);
    int privilegeLevel = getUserPrivilegeLevel(userIdStr);

    if (privilegeLevel == -1){
      result.addElement("The user privilege as not been defined");
      return result;
    }
    System.out.println("user privilege is " + privilegeLevel);
    if (privilegeLevel < Integer.parseInt(ConfigFile.getProperty("panther_curator_rank"))){
      result.addElement("The user do not have wirte privilege.");
      System.out.println("The user do not have write privilege.");
      return result;
    }

    // Make sure upl has not already been released
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      result.addElement("Error accessing data from database");
      return result;
    }

    // If this version of the upl has been released, then add clause to ensure only records
    // created prior to the release date are retrieved
    Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);

    if (null == clsInfo){
      result.addElement("Invalid UPL version specified.");
      return result;
    }
    if (null != clsInfo.elementAt(1)){
      result.addElement("UPL has already been released, changes can no-longer be saved.");
      return result;
    }

    String  seqId = getRefId(uplVersion, seqAcc, true);

    if (seqId == null){
      result.addElement("The sequence " + seqAcc + " is not in the database.");
      return result;
    }
    System.out.println("sequence ID is " + seqId);
    String  msg = checkLockableAndLock(userIdStr, seqId, true);

    if (msg != null){
      result.addElement(msg);
      return result;
    }

    // msg = lockSequenceOrClassification(userIdStr, seqId, true);
    // if (msg != null){
    // result.addElement(msg);
    // return result;
    // }
    System.out.println("Sequence " + seqId + " is locked");
    Vector  evidences = getEvidences(userIdStr, privilegeLevel, seqId, true);

    if (evidences == null){
      evidences = new Vector();
    }
    result.addElement("");
    result.addElement(evidences);
    return result;
  }

  /**
   * return a vector: first element is the error message, the second is evidences returned if no error
   */
  public Vector lockSubfamilyAndGetEvidences(String userName, String password, String uplVersion,
          String subfamilyAcc){
    System.out.println("inside lockSubfamilyAndGetEvidences");
//    Connection  con = null;
//    int         updateCount = 0;
    Vector      result = new Vector();
    String      userIdStr = getUserId(userName, password);

    if (null == userIdStr){
      result.addElement("Cannot verify user information");
      return result;
    }
    System.out.println("user ID is " + userIdStr);
    int privilegeLevel = getUserPrivilegeLevel(userIdStr);

    if (privilegeLevel == -1){
      result.addElement("The user privilege as not been defined");
      return result;
    }
    System.out.println("user privilege is " + privilegeLevel);
    if (privilegeLevel < Integer.parseInt(ConfigFile.getProperty("panther_curator_rank"))){
      result.addElement("The user do not have wirte privilege.");
      System.out.println("The user do not have write privilege.");
      return result;
    }

    // Make sure upl has not already been released
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      result.addElement("Error accessing data from database");
      return result;
    }

    // If this version of the upl has been released, then add clause to ensure only records
    // created prior to the release date are retrieved
    Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);

    if (null == clsInfo){
      result.addElement("Invalid UPL version specified.");
      return result;
    }
    if (null != clsInfo.elementAt(1)){
      result.addElement("UPL has already been released, changes can no-longer be saved.");
      return result;
    }

    String  subfamilyId = getRefId(uplVersion, subfamilyAcc, false);

    if (subfamilyId == null){
      result.addElement("The subfamily " + subfamilyAcc + " is not in the database.");
      return result;
    }
    System.out.println("subfamily ID is " + subfamilyId);

    /*
     * // DO NOT NEED TO LOOK SUBFAMILY ANYMORE
     * String msg = checkLockableAndLock(userIdStr, subfamilyId, false);
     * if (msg != null){
     * result.addElement(msg);
     * return result;
     * }
     * //    System.out.println("Subfamily " + subfamilyId + " is lockable");
     * //    msg = lockSequenceOrClassification(userIdStr, subfamilyId, false);
     * //    if (msg != null){
     * //      result.addElement(msg);
     * //      return result;
     * //    }
     * System.out.println("Subfamily " + subfamilyId +" is locked");
     */
    Vector  evidences = getEvidences(userIdStr, privilegeLevel, subfamilyId, false);

    if (evidences == null){
      evidences = new Vector();
    }
    result.addElement("");
    result.addElement(evidences);
    return result;
  }

  /**
   * Check whether the sequence/family/subfamily is available for locking
   */
  private String checkLockableAndLock(String userId, String refId, boolean isSequence){
    Connection        con = null;
    PreparedStatement stmt = null;
    ResultSet         rst = null;
    String            msg = null;
    String            query = QueryString.SEQUENCE_CURRENT_LOCKING_STATUS;

    if (!isSequence){
      query = QueryString.CLASSIFICATION_CURRENT_LOCKING_STATUS;
    }
    try{
      con = getConnection();
      if (null == con){
        return "Cannot get database connection to lock sequence for user";
      }
      if (refId != null){
        stmt = con.prepareStatement(QueryString.SEQUENCE_CURRENT_LOCKING_STATUS);
        stmt.setLong(1, Long.parseLong(refId));
        stmt.setInt(2, Integer.parseInt(ConfigFile.getProperty("panther_check_out")));
        rst = stmt.executeQuery();
        if (rst.next()){    // the sequence already been locked, check whether it is locked by the same user
          int userIdLocked = rst.getInt(1);

          if (Integer.parseInt(userId) != userIdLocked){
            msg = "The Sequence is currently locked by another user.";
          }
        }
        else{
          msg = lockSequenceOrClassification(userId, refId, isSequence);
        }                   // the sequence is not locked, need to lock the sequence
         rst.close();
        stmt.close();
      }
    }
    catch (SQLException se){
      System.out.println("Unable to lock the sequence, exception " + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return msg;
  }

  /**
   * Get the classification or protein ID based on their accssion
   */
  private String getRefId(String uplVersion, String accession, boolean isSequence){
    Connection  con = null;
    String      refId = null;
    String      query = QueryString.PROTEIN_INFO;

    if (!isSequence){
      query = QueryString.CLASSIFICATION_INFO;
    }
    try{
      con = getConnection();
      if (null == con){
        return "Cannot get database connection to retrieve the seqId";
      }
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setInt(1, Integer.parseInt(uplVersion));
      stmt.setString(2, accession);
      ResultSet rst = stmt.executeQuery();

      if (rst.next()){
        refId = rst.getString(1);
      }
      else{
        refId = null;
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve sequence/subfamily, exception " + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return refId;
  }

  /**
   * Method declaration
   *
   *
   * @param book
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public String[] getTree(String book, String uplVersion){
    Connection  con = null;
    String[]    treeStrings = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      if (null == clsIdToVersionRelease){
        initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        return null;
      }

      //
      String  query = QueryString.PREPARED_TREE;

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      if (null == clsInfo) {
          log.error(MSG_CLASSIFICATION_ID_INFO_NOT_FOUND);
          return null;
      }
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "td");
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "td");
      }

      // System.out.println(query);
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setInt(1, Integer.parseInt(uplVersion));
      stmt.setString(2, book);

      // Add values for the date clause
      if (null != dateStr){
        stmt.setString(3, dateStr);
        stmt.setString(4, dateStr);
      }
      ResultSet     rst = stmt.executeQuery();
      Vector        treeRslt = new Vector();

      while (rst.next()){
          treeRslt.add(rst.getString(1));
      }

        // Close statements
      rst.close();
      stmt.close();

      // break string according to new line characters
      treeStrings = Utils.tokenize((String) treeRslt.elementAt(0), NEWLINE_DELIM);
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    if (0 == treeStrings.length){
      return null;
    }
    return treeStrings;
  }
  
  public Boolean isGOUser(boolean isLogged, String userId, String password) {
      User u = getUser(userId, password);
      if (null == u) {
          return null;
      }
      return Boolean.valueOf(u.isGOUser(isLogged, u.getGroupName()));
  }
  
  
  public String addVersionReleaseClause(String uplVersion, String query, String tableName) {
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      if (null == clsInfo) {
          return null;
      }
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        String non_release_caluse = Utils.replace(QueryString.RELEASE_CLAUSE, RELEASE_CLAUSE_TBL_NAME, tableName);
        non_release_caluse = Utils.replace(non_release_caluse, REPLACE_STR_PERCENT_1, dateStr);
        query += non_release_caluse;
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, RELEASE_CLAUSE_TBL_NAME, tableName);
      }
      return query;
  }

    /**
     *
     * @param uplVersion
     * @param book
     * @return Hashtable of annotation node value pairs.  Note, Key contains family and annotation id, value only contains annotation id
     */

    public Hashtable<String, String> getAllNodesInTree(String uplVersion,
                                                       String book) {
        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease) {
            return null;
        }

        Connection con = null;
        ResultSet rst = null;
        PreparedStatement stmt = null;
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }


            // Get all nodes in tree
            Hashtable<String, String> anNodeTbl =
                new Hashtable<String, String>();
            String queryAllNodes =
                addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            queryAllNodes =
                    QueryString.PREPARED_ALL_NODES_IN_TREE + queryAllNodes;

            stmt = con.prepareStatement(queryAllNodes);
            stmt.setInt(1, Integer.parseInt(uplVersion));
            stmt.setString(2, book + QUERY_WILDCARD);

            rst = stmt.executeQuery();
            rst.setFetchSize(100);

            while (rst.next()) {
                String anId = rst.getString(COLUMN_NAME_ACCESSION);
                
                anNodeTbl.put(anId, Utils.getAnnotId(anId));
            }
            
            return anNodeTbl;


        } catch (SQLException se) {
            System.out.println(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();
        } finally {
            releaseDBResources(rst, stmt, con);
        }
        return null;


    }
    
    
    public String[] generateSfToAnInfo(Hashtable<String, String> anSfId) {
        if (null == anSfId) {
            return null;
        }
        
        String outList[] = new String[anSfId.size()];
        int counter = 0;
        StringBuffer sb = new StringBuffer();
        Enumeration <String> anEnum = anSfId.keys();
        while (anEnum.hasMoreElements()) {
            sb.setLength(0);
            String anId = anEnum.nextElement();
            String sfid = anSfId.get(anId);
            String modAnId = Utils.getAnnotId(anId);
            String modSfId = Utils.getSfIdFromBookSubfamilyId(sfid);
            sb.append(modSfId);
            sb.append(Constant.STR_TAB);
            sb.append(modAnId);
            sb.append(Constant.STR_NEWLINE);
            outList[counter] = sb.toString();
            counter ++;
        }
        return outList;
    }


    public synchronized Vector<String[]> getAttrTableAndSfInfo(String book, String uplVersion, String userIdStr, String userName, String password) {
        int userId = Integer.parseInt(userIdStr);
        int uplVersionId = Integer.parseInt(uplVersion);
        String bookWildcard = book + QUERY_WILDCARD;
     
        Boolean isGOUser = isGOUser(true, userName, password); // Need this information, since go users may get attribute table that is different from other users
        if (null == isGOUser) {
            return null;
        }
        boolean goUser = isGOUser.booleanValue();
        
        String attrTable[] = null;
         
        Hashtable<String, String> allAnnotTbl = null;                       // All annotation nodes table

        Hashtable<String, String> anToSfId = new Hashtable<String, String>(); // annotation id to Sf id
        Hashtable<String, String> anToSfName = new Hashtable<String, String>(); // annotation id to Sf name
        
//        Hashtable<Integer, String> protToProtAcc = new Hashtable<Integer, String>(); // Protein to protein accession
//        Hashtable<Integer, String> protToAn = new Hashtable<Integer, String>(); // Protein to annotation id
        Hashtable<String, Integer> anToProt = new Hashtable<String, Integer>(); // Protein to annotation id
        Hashtable<String, String> anToExtProtAcc = new Hashtable<String, String>(); // An id to external protein accession or annotation id
        
//        Hashtable<String, String> singletonAnToSf = new Hashtable<String, String>();  // Singleton an id to sf id
//        Hashtable<String, String> singletonSfToAn = new Hashtable<String, String>();  // Singleton sf id to an id
//        Hashtable<String, String> nonSingletonSf = new Hashtable<String, String>();  // Singleton an id to sf id
    
//        Hashtable sfToProt = new Hashtable(); // Subfamily id to list of associated protein ids's

        Hashtable<Integer, String> protToProtSrc = new Hashtable<Integer, String>(); // Protein to protein source -  Source id stored as String

        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease) {
            return null;
        }
        
        
        // Get all annotation nodes in tree
        allAnnotTbl = getAllNodesInTree(uplVersion, book);
        if (null == allAnnotTbl) {
            return null;
        }
        
        // Make copy of all nodes and remove leaf nodes to get table of non leaf nodes
         anToExtProtAcc = (Hashtable<String, String>) allAnnotTbl.clone();
        
        
        Connection con = null;
        try {
            con = getConnection();
            
            // Get the subfamily to annotation node relationship
            String querySfAn = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_a);
            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_n);
            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_c);
            querySfAn = QueryString.PREPARED_AN_SF_NODE + querySfAn;
            System.out.println(querySfAn);
            
            PreparedStatement stmt = con.prepareStatement(querySfAn);
            stmt.setInt(1, uplVersionId);
            stmt.setString(2, bookWildcard);
            stmt.setString(3, ConfigFile.getProperty(PROPERTY_ANNOTATION_TYPE_SF));
        

            ResultSet rst = stmt.executeQuery();
            rst.setFetchSize(100);

            while (rst.next()){
                String sfId = rst.getString(COLUMN_NAME_ACCESSION);
                String sfName = rst.getString(COLUMN_NAME_NAME);
                String anId = rst.getString(5);
                
                if (null != sfId && null != anId) {
                    anToSfId.put(anId, sfId);
                }
                else {
                    return null;
                }
                
                if (null != sfName) {
                    anToSfName.put(anId, sfName);
                }
            }
            rst.close();
            stmt.close();
            
            
            // Get the protein to protein accession relationship
            String queryAnProt = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_p);
            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_pn);
            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_g);
            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_gn);
            queryAnProt = QueryString.PREPARED_AN_PROTEIN_ID + queryAnProt;
            System.out.println(queryAnProt);
            
            
            stmt = con.prepareStatement(queryAnProt);
            stmt.setInt(1, uplVersionId);
            stmt.setString(2, bookWildcard);
            
            rst = stmt.executeQuery();
            rst.setFetchSize(100);

            while (rst.next()){
                String anId = rst.getString(COLUMN_NAME_ACCESSION);
                Integer protId = Integer.valueOf(rst.getInt(COLUMN_NAME_PROTEIN_ID));
                String protExtAcc = rst.getString(COLUMN_NAME_PRIMARY_EXT_ACC); // Actually gene ext acc
                String protSrcId = rst.getString(COLUMN_NAME_SOURCE_ID);
                
                
                if (null != protId && null != protExtAcc) {
                    //protToProtAcc.put(protId, protExtAcc);
                    anToExtProtAcc.put(anId, protExtAcc);
                }
                else {
                    rst.close();
                    stmt.close();
                    log.error(MSG_ERROR_NULL_PROTEIN_INFO_ENCOUNTERED + book);
                    return null;
                }
                
                //protToAn.put(protId, anId);
                anToProt.put(anId, protId);
                protToProtSrc.put(protId, protSrcId);
                
                // Save information about singleton subfamilies
                String sfId = anToSfId.get(anId);
                if (null != sfId) {
                    //singletonAnToSf.put(anId, sfId);
                    //singletonSfToAn.put(sfId, anId);
                }
                

            }
            rst.close();
            stmt.close();
            
            
            


        }
        catch (SQLException se){
          log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
          se.printStackTrace();
        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            catch (SQLException se){
              log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
              return null;
            }
          }
        }
        
        
        // Verify that all nodes are associated with a sf
        PANTHERTree treeStruct = getPANTHERTree(uplVersion, book);
        if (false == verifySfNodes(treeStruct, anToSfId)) {
            log.error (book + MSG_INVALID_SF_TO_SEQ_INFO);
            return null;
        }
        
        

        
        // Get the identifiers
         Hashtable<String, Hashtable<String, String>> annotRsltTable = getIdentifiers(book, (ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_IDENTIFIERS)), uplVersion);

        if (null == annotRsltTable){
          log.error(MSG_ERROR_NULL_IDENTIFIER_RSLT_RETURNED + book);
          annotRsltTable = new Hashtable<String, Hashtable<String, String>>();
        }
        

        // Add the external protein accessions to the table
        annotRsltTable.put(ID_RSLT_ACC, anToExtProtAcc);
        
        // Add the subfamily names
        annotRsltTable.put(SF_RSLT_NAME, anToSfName);

        System.out.println("Getting organism information");
        // Add the organism information
        initProtSrcToOrgLookup();
        if (null != protSrcToOrgLookup){
          String col_org = ConfigFile.getProperty(TBL_COL_ORG);
          Hashtable   orgTable = new Hashtable();
          Enumeration<String> anIdsEnum = anToProt.keys();

          while (anIdsEnum.hasMoreElements()){
            String anId = anIdsEnum.nextElement();
            Integer proteinId = anToProt.get(anId);
            String  protSrc = protToProtSrc.get(proteinId);
            String  organism[] = (String[]) protSrcToOrgLookup.get(protSrc);

            if (null != organism){
              orgTable.put(anId, organism[0]);
            }
          }
          annotRsltTable.put(col_org, orgTable);
        }
        System.out.println("Finished retrieving organism information");

        // Get evidence for annotation nodes
        Hashtable evidenceTable = null;  //getEvidence(true, false, book, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_EVIDENCE), userId, uplVersion);

        if (null != evidenceTable){
          Hashtable ht = (Hashtable) evidenceTable.get(ConfigFile.getProperty(PROPERTY_COL_XLINKS));

          if (null != ht){
            convertHashToStrForEvidence(ht);
            annotRsltTable.put(ConfigFile.getProperty(PROPERTY_COL_XLINKS), ht);
          }
        }
        Hashtable seqEvidence = null;
          if (null != evidenceTable)  {
              seqEvidence = (Hashtable) evidenceTable.get(ConfigFile.getProperty(PROPERTY_COL_EVIDENCE));
          }
          else {
              seqEvidence = new Hashtable();
          }

        //System.out.println("Retrieved evidence information");
        
        // Get the gene information
        Vector<Hashtable<String, String>> geneInfo = getGeneInfo(book, uplVersion);
        if (null != geneInfo && 2 <= geneInfo.size()) {
            annotRsltTable.put(ConfigFile.getProperty(ATTR_COLUMN_GENE_ID), geneInfo.elementAt(INDEX_TBL_GENE_ID));
            annotRsltTable.put(ConfigFile.getProperty(ATTR_COLUMN_GENE_SYMBOL), geneInfo.elementAt(INDEX_TBL_GENE_SYMBOL));
        }
        




        // Now deal with the subfamilies
        // Get list of non-singleton subfamilies
//        Enumeration <String> anEnum = anToSfId.keys();
//        while (anEnum.hasMoreElements()) {
//            String anId = anEnum.nextElement();
//            String sfId = anToSfId.get(anId);
//            if (null == singletonSfToAn.get(sfId)) {
//                nonSingletonSf.put(anId, sfId);
//            }
//        }


        // Subfamily Evidence
        // No need to check xlinks since this is sequence related information
        Hashtable allEvidence = null;  //getEvidence(false, false, book, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_EVIDENCE), userId, uplVersion);
        Hashtable nodeEvidence = null;
        if (null != allEvidence) {
          nodeEvidence = (Hashtable) allEvidence.get(ConfigFile.getProperty(PROPERTY_COL_EVIDENCE));
        }
        else {
            nodeEvidence = new Hashtable();
        }
        
        
        // Add the sequence evidence information into the nodeEvidence table

        Enumeration anIdsEnum = seqEvidence.keys();
        while (anIdsEnum.hasMoreElements()) {
            String anId = (String)anIdsEnum.nextElement();
            // This may be a singleton that has subfamily evidence - Do not overwrite
            if (null != nodeEvidence.get(anId)) {
                continue;
            }
            nodeEvidence.put(anId, seqEvidence.get(anId));
        }

        

        convertHashToStrForEvidence(nodeEvidence);
        annotRsltTable.put(ConfigFile.getProperty(PROPERTY_COL_EVIDENCE), nodeEvidence);

        Hashtable publicIdLookup = getAnnotationNodeLookup(book, uplVersion);
        if (null != publicIdLookup) {
            annotRsltTable.put(ConfigFile.getProperty(PROPERTY_COL_PANTHER_PUBLIC_ID), publicIdLookup);
        }
    

        // Get go annot
        if (false == goUser) {
            Hashtable<String, Vector<Evidence>> goAnnotTbl = getGOAnnotation(book, uplVersion);
            Hashtable<String, String> formatGoTbl = formatGoAnnot(goAnnotTbl);
            annotRsltTable.put(ConfigFile.getProperty(ATTR_COLUMN_GO_ANNOTATION), formatGoTbl);
            
        }
        
        
//        // Get the classifications associated with the node
//        Vector <Hashtable<String, String>>clsInfo = getAnnotations(book, uplVersion);
//        if (null != clsInfo) {
//            annotRsltTable.put(ConfigFile.getProperty(PROPERTY_COL_CARRYOVER_ANNOT), clsInfo.get(INDEX_TBL_CLS_ASN_CARRYOVER));
//            annotRsltTable.put(ConfigFile.getProperty(PROPERTY_COL_DIRECT_ANNOT), clsInfo.get(INDEX_TBL_CLS_ASN_REVIEWED));
//        }
        
        
        // Get the subfamily to annotation information in a string format
        String sfAnInfo[] = generateSfToAnInfo(anToSfId);

        
        String  title = null;
        if (false == goUser) {
            // TODO - Panther users get different information.  Retrieve additional information as required
            title = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_COL_HEADERS);
        }
        else {
          title = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_COL_HEADERS);   
        }
        Vector  attributeTable = outputAttrTable(new Vector(allAnnotTbl.keySet()), annotRsltTable, uplVersion);

        if (0 == attributeTable.size()){
          return null;
        }

        title = title.substring(1, title.length() - 1);
        attributeTable.insertElementAt(title, 0);
        attrTable = new String[attributeTable.size()];
        attributeTable.copyInto(attrTable);
        
        
        Vector rtnInfo = new Vector(2);
        rtnInfo.setSize(2);
        rtnInfo.add(INDEX_ATTR_METHOD_ATTR_TBL, attrTable);
        rtnInfo.add(INDEX_ATTR_METHOD_SF_AN_INFO, sfAnInfo);

        return rtnInfo;
    }
    

    
    public Vector getAnSfIdInfo(String book, String uplVersion) {
        Vector returnList = new Vector(2);
        returnList.setSize(2);
        Hashtable<String, String> anToSfId = new Hashtable<String, String>(); // annotation id to Sf id
        Hashtable<String, String> anToSfName = new Hashtable<String, String>(); // annotation id to Sf name
        returnList.add(INFO_INDEX_AN_SF_ID, anToSfId);
        returnList.add(INFO_INDEX_AN_SF_NAME, anToSfName);
        int uplVersionId = Integer.parseInt(uplVersion);
        String bookWildcard = book + QUERY_WILDCARD;
        
        Connection con = null;

        try {
            con = getConnection();
            
            // Get the subfamily to annotation node relationship
            String querySfAn = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_a);
            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_n);
            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_c);
            querySfAn = QueryString.PREPARED_AN_SF_NODE + querySfAn;
            System.out.println(querySfAn);
            
            PreparedStatement stmt = con.prepareStatement(querySfAn);
            stmt.setInt(1, uplVersionId);
            stmt.setString(2, bookWildcard);
            stmt.setString(3, ConfigFile.getProperty(PROPERTY_ANNOTATION_TYPE_SF));
        

            ResultSet rst = stmt.executeQuery();
            rst.setFetchSize(100);

            while (rst.next()){
                String sfId = rst.getString(COLUMN_NAME_ACCESSION);
                String sfName = rst.getString(COLUMN_NAME_NAME);
                String anId = rst.getString(5);
                
                if (null != sfId && null != anId) {
                    anToSfId.put(anId, sfId);
                }
                else {
                    System.out.println("Found null sfId anId for " + book);
                    continue;
                }
                
                if (null != sfName) {
                    anToSfName.put(anId, sfName);
                }
            }
            rst.close();
            stmt.close();            
            return returnList;
        }
        catch (SQLException se){
          log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
          se.printStackTrace();
        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            catch (SQLException se){
              log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
              return null;
            }
          }
        }
        return null;
    }
  

  /**
   * Method declaration
   *
   *
   * @param book
   * @param uplVersion
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
  public String[] getAttrTableOld(String book, String uplVersion, String userIdStr, String userName, String password){
//    System.out.println("Beginning of attribute table for book " + book);  
    int         userId = Integer.parseInt(userIdStr);
    Boolean     isGOUser = isGOUser(true, userName, password); // Need this information, since PANTHER users may get attribute table that is different from other users
    if (null == isGOUser) {
        return null;
    }
    boolean goUser = isGOUser.booleanValue();
    Connection  con = null;
    String      attrTable[] = null;
    Hashtable   sfIdToSfAccName = new Hashtable();    // Subfamily id to accession and name
    Hashtable   sfToProt = new Hashtable();           // Subfamily id to list of associated protein ids's
    Hashtable   protToProtAcc = new Hashtable();      // Protein to protein accession
    Hashtable   protToProtSrc = new Hashtable();      // Protein to protein source

    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      return null;
    }
    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      String  query = QueryString.ASSOCIATED_SUBFAM_AND_SEQ;

      query = Utils.replace(query, "%1", uplVersion);
      query = Utils.replace(query, "%2", book + ":%");
//      System.out.println(query);
      Statement stmt = con.createStatement();

      // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      // System.out.println("start of subfamily to seq query execution " + df.format(new java.util.Date(System.currentTimeMillis())));
      ResultSet rst = stmt.executeQuery(query);

      // System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
      rst.setFetchSize(100);
      Vector  intProtIds = new Vector();

      while (rst.next()){

        // Create a table of the subfamily id to its accession and name
        String  sfAccName[] = new String[2];

        sfAccName[0] = rst.getString(2);
        sfAccName[1] = rst.getString(3);
        if (null == sfAccName[1]){
          sfAccName[1] = STR_EMPTY;
        }
        Integer sfId = new Integer(rst.getInt(1));

        sfIdToSfAccName.put(sfId, sfAccName);

        // Create the list of associated proteins for the subfamily
        Integer protId = new Integer(rst.getInt(5));

        intProtIds.addElement(protId);
        Vector  protList = (Vector) sfToProt.get(sfId);

        if (null == protList){
          protList = new Vector();
          sfToProt.put(sfId, protList);
        }
        protList.addElement(protId);

        // Create the protein id to accession lookup
        protToProtAcc.put(protId, rst.getString(6));

        // Create the protein id to protein source lookup.  Note, protein source being stored as a string
        protToProtSrc.put(protId, Integer.toString(rst.getInt(7)));
      }
      rst.close();
      stmt.close();
//      System.out.println("Retrieved associated subfamily and sequence");
      Hashtable protRsltTable = getIdentifiers(book, (ConfigFile.getProperty(uplVersion + "_identifiers")), uplVersion);

      if (null == protRsltTable){
        return attrTable;
      }
//      System.out.println("Retrieved identifier information");      

      // Add the sequence information to the table
      protRsltTable.put("id_acc", protToProtAcc);

      // Get the organism information
      initProtSrcToOrgLookup();
      if (null != protSrcToOrgLookup){
        Hashtable   orgTable = new Hashtable();
        Enumeration proteinIds = protToProtAcc.keys();

        while (proteinIds.hasMoreElements()){
          Integer proteinId = (Integer) proteinIds.nextElement();
          String  protSrc = (String) protToProtSrc.get(proteinId);
          String  organism[] = (String[]) protSrcToOrgLookup.get(protSrc);

          if (null != organism){
            orgTable.put(proteinId, organism[0]);
          }
        }
        protRsltTable.put(ConfigFile.getProperty("org_col"), orgTable);
      }

      // Get the previous upl information
//      Hashtable prevUPLInfo = getPreviousSfNameCat(uplVersion, book, ConfigFile.getProperty(uplVersion + "_cls_types"));
//      
//      if (null != prevUPLInfo) {
//    
//        protRsltTable.put(ConfigFile.getProperty("prev_upl_col"), prevUPLInfo);
//      }
//      System.out.println("Retrieved previous upl information");
      // Get the public interpro information
      
//      Hashtable pubInterpro = getPublicInterpro(book, uplVersion, ConfigFile.getProperty(uplVersion + "_interpro_id"));
//      if (null != pubInterpro) {
//        protRsltTable.put(ConfigFile.getProperty("pub_interpro_col"), pubInterpro);   
//      }
//      System.out.println("Retrieved public interpro information");
      
//      Hashtable pantherInterpro = getPanterInterpro(book, uplVersion, ConfigFile.getProperty(uplVersion + "_interpro_id"));
//    
//      if (null != pantherInterpro) {
//        protRsltTable.put(ConfigFile.getProperty("pthr_interpro_col"), pantherInterpro);    
//      }
//      System.out.println("Retrieved panter interpro information");
      
      // Handle the xlinks information
      Hashtable evidenceTable = getEvidence(true, false, book, ConfigFile.getProperty(uplVersion + "_evidence"),
                                            userId, uplVersion);

      if (null != evidenceTable){
        Hashtable ht = (Hashtable) evidenceTable.get(ConfigFile.getProperty("xlinks_col"));

        if (null != ht){
          convertHashToStrForEvidence(ht);
          protRsltTable.put(ConfigFile.getProperty("xlinks_col"), ht);
        }
      }
      Hashtable seqEvidence = null;
        if (null != evidenceTable)  {
            seqEvidence = (Hashtable) evidenceTable.get(ConfigFile.getProperty("evidence_col"));
        }

      System.out.println("Retrieved evidence information");

      // get the comments information
//      Hashtable commentsTable = getComments(true, book, uplVersion);
//
//      if (null != commentsTable){
//        protRsltTable.put(ATTR_COLUMN_COMMENTS, commentsTable);
//      }
      
      
      // Get the gene information
      Vector geneInfo = getGeneInfo(book, uplVersion);
      if (null != geneInfo && 2 <= geneInfo.size()) {
          protRsltTable.put(ConfigFile.getProperty(ATTR_COLUMN_GENE_ID), geneInfo.elementAt(INDEX_TBL_GENE_ID));
          protRsltTable.put(ConfigFile.getProperty(ATTR_COLUMN_GENE_SYMBOL), geneInfo.elementAt(INDEX_TBL_GENE_SYMBOL));
      }
      
      // Get GO annotation information
//      Hashtable goAnnotation = getGOAnnotation(book, uplVersion);
//      if (null != goAnnotation) {
//         protRsltTable.put(ConfigFile.getProperty(ATTR_COLUMN_GO_ANNOTATION), goAnnotation);
//      }
      
//      // GO Inference is currently unavailable from database, return empty result set for now
//      Hashtable goInference = new Hashtable();
//      protRsltTable.put(ConfigFile.getProperty(ATTR_COLUMN_GO_INFERENCE), goInference);
//      System.out.println("Retrieved comments information");      

      // Now deal with the subfamilies
      // Create an array of subfamily ids and a Hashtable of subfamilies with only one sequence (singleton subfamilies)
      Hashtable   singletonSfTable = new Hashtable();
      Enumeration sfList = sfIdToSfAccName.keys();

      // Vector sfIds = new Vector();
      Vector      intSfIds = new Vector();    // Contains list of non singleton subfamilies

      while (sfList.hasMoreElements()){
        Integer sfId = (Integer) sfList.nextElement();

        // sfIds.addElement(sfId.toString());
        Vector  sequences = (Vector) sfToProt.get(sfId);

        if (1 == sequences.size()){
          singletonSfTable.put(sfId, sfId);
        }
        else{
          intSfIds.addElement(sfId);
        }
      }
      Hashtable   sfRsltTable = new Hashtable();
      Hashtable   sfAccTbl = new Hashtable();
      Hashtable   sfNameTbl = new Hashtable();
      Enumeration sfAcc = sfIdToSfAccName.keys();

      while (sfAcc.hasMoreElements()){
        Integer sfId = (Integer) sfAcc.nextElement();

        // Do not add singletons
        if (null != singletonSfTable.get(sfId)){
          continue;
        }
        String  sfAccName[] = (String[]) sfIdToSfAccName.get(sfId);
        String  sfModName = sfAccName[0].substring(sfAccName[0].indexOf(":") + 1, sfAccName[0].length());

        sfAccTbl.put(sfId, sfModName);
        sfNameTbl.put(sfId, sfAccName[1]);
      }
      sfRsltTable.put(ID_RSLT_ACC, sfAccTbl);
      sfRsltTable.put(SF_RSLT_NAME, sfNameTbl);

      // Subfamily Evidence.
      // Combine sequence related evidence with subfamily evidence for associated sequences
      // No need to check xlinks since this is sequence related information
      Hashtable allEvidence = getEvidence(false, false, book, ConfigFile.getProperty(uplVersion + "_evidence"), userId,
                                          uplVersion);
      Hashtable sfEvidence = null;
      if (null != allEvidence) {
        sfEvidence = (Hashtable) allEvidence.get(ConfigFile.getProperty("evidence_col"));
      }

      if ((null != seqEvidence) && (null != sfEvidence)){
        Enumeration sfEnumeration = sfToProt.keys();

        while (sfEnumeration.hasMoreElements()){
          Integer sfId = (Integer) sfEnumeration.nextElement();

          // Do not have to handle singletons
          if (null != singletonSfTable.get(sfId)){
            continue;
          }
          Vector  sequences = (Vector) sfToProt.get(sfId);

          for (int i = 0; i < sequences.size(); i++){
            Integer   seqId = (Integer) sequences.elementAt(i);
            Hashtable sfLinkTable = (Hashtable) sfEvidence.get(sfId);
            Hashtable seqLinkTable = (Hashtable) seqEvidence.get(seqId);

            if (null == seqLinkTable){
              continue;
            }

            // If there was no evidence associated with the subfamily, create evidence table
            // since there is evidence at the sequence level.
            if (null == sfLinkTable){
              sfLinkTable = new Hashtable();
              sfEvidence.put(sfId, sfLinkTable);
            }
            Enumeration seqTypes = seqLinkTable.keys();

            while (seqTypes.hasMoreElements()){
              String  seqType = (String) seqTypes.nextElement();
              Vector  sfLinks = (Vector) sfLinkTable.get(seqType);

              if (null == sfLinks){
                sfLinks = new Vector();
                sfLinkTable.put(seqType, sfLinks);
              }
              sfLinks.addAll((Vector) seqLinkTable.get(seqType));
            }
          }
        }
      }

      // Save the sequence information
      if (null != seqEvidence){
        convertHashToStrForEvidence(seqEvidence);
        protRsltTable.put(ConfigFile.getProperty("evidence_col"), seqEvidence);
      }

      //Remove singletons from the subfamily evidence table and save
      if (null != sfEvidence){
          Enumeration singletons = singletonSfTable.keys();
    
          while (singletons.hasMoreElements()){
            sfEvidence.remove(singletons.nextElement());
          }
          convertHashToStrForEvidence(sfEvidence);
          sfRsltTable.put(ConfigFile.getProperty("evidence_col"), sfEvidence);
      }
//
//      // Subfamily comments
//      Hashtable   sfComment = getComments(false, book, uplVersion);
//      Enumeration singletons = singletonSfTable.keys();
//
//      while (singletons.hasMoreElements()){
//        sfComment.remove(singletons.nextElement());
//      }
//      sfRsltTable.put("comments", sfComment);

      // Get the classification information and add this to the proteins
      // Get the classification evidence confidence information as well
//      Hashtable allClsTable = getClsForSubfam(uplVersion, book, ConfigFile.getProperty(uplVersion + "_cls_types"));
//      Hashtable allClsCnfTable = getClsConfEvidence(uplVersion, book, ConfigFile.getProperty(uplVersion + "_cls_types"));
//      Hashtable clsTypesTbl = new Hashtable();

//      if (null != allClsTable){
//        Enumeration key = allClsTable.keys();
//
//        while (key.hasMoreElements()){
//
//          // Get the current cls column, create a hashtable and store inside the results table
//          String    clsCol = (String) key.nextElement();
//          Hashtable currentClsTbl = new Hashtable();
//
//          protRsltTable.put(clsCol, currentClsTbl);
//
//          // Get the subfamilies in this table, for each subfamily find its associated sequences and store
//          // this information for each sequence
//          Hashtable   clsTbl = (Hashtable) allClsTable.get(clsCol);
//          Enumeration subfams = clsTbl.keys();
//
//          while (subfams.hasMoreElements()){
//            Integer sfId = (Integer) subfams.nextElement();
//            Vector  sequences = (Vector) sfToProt.get(sfId);
//            String clsStr = (String)clsTbl.get(sfId);             
//            storeClsTypesInfo(clsTypesTbl, clsStr);
//            //System.out.println("After storing cls information, there are " + clsTypesTbl.size() + " entries in the table");
//            for (int i = 0; i < sequences.size(); i++){
//              Integer protId = (Integer)sequences.elementAt(i);
//              String combinedClsConfStr = getCombinedClsConfStr(protId, clsTypesTbl, (Hashtable)allClsCnfTable.get(clsCol));
//              if (null == combinedClsConfStr) {
//                currentClsTbl.put(protId, clsStr);
//              }
//              else {
//                //System.out.println("Protein id " + protId.toString() + " has confidence information " + combinedClsConfStr);
//                currentClsTbl.put(protId, combinedClsConfStr);
//              }
//            }
//          }
//          singletons = singletonSfTable.keys();
//          while (singletons.hasMoreElements()){
//            clsTbl.remove(singletons.nextElement());
//          }
//          sfRsltTable.put(clsCol, clsTbl);
//        }
//      }   

      

      // Store the singleton subfamily names in the protein table
//      Hashtable sfTbl = new Hashtable();
//
//      protRsltTable.put("sf_name", sfTbl);
//      singletons = singletonSfTable.keys();
//      while (singletons.hasMoreElements()){
//        Integer   sfId = (Integer) singletons.nextElement();
//        Vector    sequences = (Vector) sfToProt.get(sfId);
//        Integer   singletonSeq = (Integer) sequences.elementAt(0);
//        String[]  sfAccName = (String[]) sfIdToSfAccName.get(sfId);
//
//        sfTbl.put(singletonSeq, sfAccName[1]);
//      }

      // Get the organism information for subfamilies
      Hashtable orgTable = new Hashtable();

      if (null != protSrcToOrgLookup){
        Enumeration subfams = sfToProt.keys();

        while (subfams.hasMoreElements()){
          Integer   sfId = (Integer) subfams.nextElement();

          // Use a hashtable to remove duplicates
          Hashtable uniqOrgTbl = new Hashtable();

          orgTable.put(sfId, uniqOrgTbl);
          Vector  sequences = (Vector) sfToProt.get(sfId);

          // Do not need to handle singleton subfamilies, since this is not output
          if (null != singletonSfTable.get(sfId)){
            continue;
          }
          for (int i = 0; i < sequences.size(); i++){
            Integer sequence = (Integer) sequences.elementAt(i);
            String  protSrc = (String) protToProtSrc.get(sequence);
            String  organism[] = (String[]) protSrcToOrgLookup.get(protSrc);

            if ((null != organism) && (null != organism[1])){
              uniqOrgTbl.put(organism[1], organism[1]);
            }
          }

          // Now convert the organisms hashtable into a string
          StringBuffer  sb = new StringBuffer();
          Enumeration   organisms = uniqOrgTbl.keys();

          while (organisms.hasMoreElements()){
            if (0 != sb.length()){
              sb.append(SEMI_COLON_DELIM);
            }
            sb.append((String) organisms.nextElement());
          }
          orgTable.put(sfId, sb.toString());
        }
      }
      sfRsltTable.put(ConfigFile.getProperty("org_col"), orgTable);
      
      String  title = null;
      if (false == goUser) {
        // TODO - Panther users get different information.  Retrieve additional information as required
        title = ConfigFile.getProperty(uplVersion + "_colHeaders");
      }
      else {

        title = ConfigFile.getProperty(uplVersion + "_colHeaders");   
      }
      Vector  attributeTable = outputAttrTable(intProtIds, protRsltTable, uplVersion);

      if (0 == attributeTable.size()){
        return null;
      }

      title = title.substring(1, title.length() - 1);
      attributeTable.insertElementAt(title, 0);
      Vector  subAttrTable = outputAttrTable(intSfIds, sfRsltTable, uplVersion);

      attributeTable.addAll(subAttrTable);
      attrTable = new String[attributeTable.size()];
      attributeTable.copyInto(attrTable);
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
        }
      }
    }
    return attrTable;
  }
  
  /**
   * Given a hashtable and the classification string.  Tokenizes the classification string into 
   * classifications and definitions.  Then adds the classifications and definitions into the hashtable
   * @param clsTypesTbl   classification table
   * @param clsStr  classification string
   */
  protected static void storeClsTypesInfo(Hashtable clsTypesTbl, String clsStr) {
    if (null == clsTypesTbl ||
        null == clsStr) {
      return;      
    }
    clsTypesTbl.clear();
    String clsTypes[] = Utils.tokenize(clsStr, SEMI_COLON_DELIM);
    for (int i = 0; i < clsTypes.length; i++) {
      String clsNameVal[] = Utils.tokenize(clsTypes[i], LINKS_ASSOC);
      clsTypesTbl.put(clsNameVal[0], clsNameVal[1]);
    }
    return;
  }
  
  /**
   * Given the 1.  protein id
   *           2.  Classification information in a hashtable where the index is the classification accession and
   *               value is the classification definition
   *           3.  Protein confidence evidence in a hashtable where the index is the protein id and the value
   *               is a hashtable indexed by classification accession.  The value associated with the classification accession
   *               is a vector containing a list of vectors.  Each vector in the list will contain the evidence type, evidence value, confidence code and confidence sid.
   * This, method returns the combined classification and confidence information, if the protein id is found in the
   * protein confidence evidence table.  Else null is returned.  If the protein id is found, the information will be
   * formatted as follows:
   * The information for each of the classifications is separated by ";".  The classification and classification definition is separated by "=".
   * Example:  AB0000=clsDef1;AB0004=clsDef4;..., where AB0000 is a classification and clsDef1 is the associated classification name, this method adds the evidence,
   * evidence value and confidence code for each of the classifications, if the evidence exists.
   * If confidence information exists for the classification, then the following will be added:
   * |confCodeX-EvidenceY-EvidenceValueZ,|confCodeA-EvidenceB-EvidenceValueC| where confCodeX is a
   * confidence code, EvidenceY is the evidence type and EvidenceValueZ is the evidence value
   * @param protId 
   * @param clsTypesTbl
   * @param clsConfTable
   * @return
   */
  public String getCombinedClsConfStr(Integer protId, Hashtable clsTypesTbl, Hashtable clsConfTable) {
    if (null == protId ||
        null == clsTypesTbl ||
        null == clsConfTable) {
      return null;
    }    

    Hashtable clsToConfList = (Hashtable)clsConfTable.get(protId);
    // If there is no evidence for this protein, exit.
    if (null == clsToConfList) {
      return null;
    }

    // Group the classification to confidence information    
    Hashtable clsToEvidenceStrTbl = new Hashtable();
    Enumeration clsEnum = clsToConfList.keys();
    while (clsEnum.hasMoreElements()) {
      String cls = (String)clsEnum.nextElement();
      Vector evidenceList = (Vector)clsToConfList.get(cls);
      String combinedClsStr = combineEvidenceListStr(evidenceList);      
      clsToEvidenceStrTbl.put(cls, combinedClsStr);
    }
    
    // Combine classification and confidence information
    StringBuffer clsConfBuf = new StringBuffer();
    Enumeration clsAccEnum = clsTypesTbl.keys();
    while (clsAccEnum.hasMoreElements()) {
      String clsAcc = (String)clsAccEnum.nextElement();
      clsConfBuf.append(clsAcc);
      clsConfBuf.append(LINKS_ASSOC);
      clsConfBuf.append((String)clsTypesTbl.get(clsAcc));
      String confInfo = (String)clsToEvidenceStrTbl.get(clsAcc);
      if (null != confInfo) {
        clsConfBuf.append(confInfo);
      }
      clsConfBuf.append(SEMI_COLON_DELIM);
    }
    return clsConfBuf.toString();
  }
  
  protected String combineEvidenceListStr(Vector evidenceList) {
    if (null == evidenceList) {
      return null;
    }
    
    StringBuffer sb = new StringBuffer();
    Enumeration evList = evidenceList.elements();
    while (evList.hasMoreElements()) {
      Vector v = (Vector)evList.nextElement();
      if (0 != sb.length()) {
        sb.append(COMMA_DELIM);
      }
      sb.append(BAR_DELIM);
      sb.append(v.elementAt(INDEX_CONF_EVDNCE_CC_TYPE));
      sb.append(BAR_DELIM);      
      sb.append(v.elementAt(INDEX_CONF_EVDNCE_EV_TYPE));
      sb.append(BAR_DELIM);      
      sb.append(v.elementAt(INDEX_CONF_EVDNCE_EV_VALUE));      
      sb.append(BAR_DELIM);      
    }
    return sb.toString();
  }

  /**
   * Method declaration
   *
   *
   * @param ids
   * @param rsltTbl
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  protected Vector outputAttrTable(Vector ids, Hashtable rsltTbl, String uplVersion){
    Vector      rslt = new Vector();
    String[]    colHeaders = Utils.tokenize(ConfigFile.getProperty(uplVersion + "_colInfo"), COMMA_DELIM);
    Enumeration idList = ids.elements();

    while (idList.hasMoreElements()){
      String       id = (String) idList.nextElement();
      StringBuffer  info = new StringBuffer();

      for (int i = 0; i < colHeaders.length; i++){
        if (0 != info.length()){
          info.append(TAB_DELIM);
        }
        info.append(QUOTE);
        Hashtable table = (Hashtable) rsltTbl.get(colHeaders[i]);

        if (null != table){
          String  cellContents = (String) table.get(id);

          if (null != cellContents){
            info.append(cellContents);
          }
        }
        info.append(QUOTE);
      }
      info.append(NEWLINE_DELIM);
      rslt.addElement(info.toString());
//      System.out.println(info.toString());
    }
    return rslt;
  }
  
  
    public Hashtable<String, Hashtable<String, String>> getIdentifiersSlow(String book,
                                                                       String identifiers,
                                                                       String uplVersion) {

        // Now get the identifiers for each annotation node
        if (0 == book.length()) {
            return null;
        }

        Hashtable<String, Hashtable<String, String>> rsltTable = new Hashtable<String, Hashtable<String, String>>();
        Connection con = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                return null;
            }

            String idQuery = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY,TABLE_NAME_n);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_p);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_i);
            idQuery = QueryString.PREPARED_AN_IDENTIFIER + idQuery;


            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_1, identifiers);
            System.out.println(idQuery);

            PreparedStatement istmt = con.prepareStatement(idQuery);

            istmt.setInt(1, Integer.parseInt(uplVersion));
            istmt.setString(2, book + QUERY_WILDCARD);

            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of identifier query execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            ResultSet irslt = istmt.executeQuery();

            System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            irslt.setFetchSize(100);
            while (irslt.next()) {

                // Protein Id to Identifiers
                String annotId = irslt.getString(COLUMN_NAME_ACCESSION);
                String idntifrColumn = ConfigFile.getProperty(PROPERTY_COL_IDENTIFIER) + Integer.toString(irslt.getInt(COLUMN_NAME_IDENTIFIER_TYPE_SID));
                Hashtable<String, String> identifierTable = rsltTable.get(idntifrColumn);

                if (null == identifierTable) {
                    identifierTable = new Hashtable<String, String>();
                    rsltTable.put(idntifrColumn, identifierTable);
                }
                String identifierName = irslt.getString(COLUMN_NAME_NAME);

                if (null != identifierName) {
                    identifierTable.put(annotId, identifierName);
                } else {
                    log.error(book + MSG_ERROR_IDENTIFIER_RETRIEVAL_NULL_FOUND + annotId);
                }
            }
            irslt.close();
            istmt.close();
        } catch (SQLException se) {
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
                    se.printStackTrace();
                    return null;
                }
            }
        }
        return rsltTable;
    }


    /**
     * Method declaration
     *
     *
     * @param book
     * @param identifiers
     * @param uplVersion
     *
     * @return
     *
     * @see
     */
    public Hashtable<String, Hashtable<String, String>> getIdentifiers(String book,
                                                                       String identifiers,
                                                                       String uplVersion) {

        // Now get the identifiers for each annotation node
        if (0 == book.length()) {
            return null;
        }

        Hashtable<String, Hashtable<String, String>> rsltTable = new Hashtable<String, Hashtable<String, String>>();
        Connection con = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease){
              initClsLookup();
              if (null == clsIdToVersionRelease){
                return null;
              }
            }

            String idQuery = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY,TABLE_NAME_n);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_p);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_i);
            idQuery = QueryString.IDENTIFIER_AN + idQuery;

            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_1, uplVersion);
            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_2,  book + QUERY_WILDCARD);
            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_3, identifiers);
            System.out.println(idQuery);

            Statement istmt = con.createStatement();

//            istmt.setInt(1, Integer.parseInt(uplVersion));
//            istmt.setString(2, book + QUERY_WILDCARD);

            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of identifier query execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            ResultSet irslt = istmt.executeQuery(idQuery);

            System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            irslt.setFetchSize(100);
            while (irslt.next()) {

                // Protein Id to Identifiers
                String annotId = irslt.getString(COLUMN_NAME_ACCESSION);
                String idntifrColumn = ConfigFile.getProperty(PROPERTY_COL_IDENTIFIER) + Integer.toString(irslt.getInt(COLUMN_NAME_IDENTIFIER_TYPE_SID));
                Hashtable<String, String> identifierTable = rsltTable.get(idntifrColumn);

                if (null == identifierTable) {
                    identifierTable = new Hashtable<String, String>();
                    rsltTable.put(idntifrColumn, identifierTable);
                }
                String identifierName = irslt.getString(COLUMN_NAME_NAME);

                if (null != identifierName) {
                    identifierTable.put(annotId, identifierName);
                } else {
                    log.error(book + MSG_ERROR_IDENTIFIER_RETRIEVAL_NULL_FOUND + annotId);
                }
            }
            irslt.close();
            istmt.close();
        } catch (SQLException se) {
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
                    se.printStackTrace();
                    return null;
                }
            }
        }
        return rsltTable;
    }


    /**
     * Method declaration
     *
     *
     * @param book
     * @param identifiers
     * @param uplVersion
     *
     * @return
     *
     * @see
     */
    public Hashtable getIdentifiersOld(String book, String identifiers, String uplVersion){

      // Now get the identifiers for each sequence
      if (0 == book.length()){
        return null;
      }
      Hashtable   rsltTable = new Hashtable();
      Connection  con = null;

      try{
        con = getConnection();
        if (null == con){
          return null;
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease){
          return null;
        }
        String  query = QueryString.PROTEIN_IDENTIFIER;

        query = Utils.replace(query, "%1", identifiers);

        // If this version of the upl has been released, then add clause to ensure only records
        // created prior to the release date are retrieved
        Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
        String  dateStr = (String) clsInfo.elementAt(1);

        if (null != dateStr){
          query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "i");
          query = Utils.replace(query, "%1", dateStr);
        }
        else{
          query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "i");
        }


        Statement istmt = con.createStatement();

        query = Utils.replace(query, "%2", uplVersion);
        query = Utils.replace(query, "%3", book + ":%");
        
        // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
        // System.out.println("start of identifier execution " + df.format(new java.util.Date(System.currentTimeMillis())));
        ResultSet irslt = istmt.executeQuery(query);

        // System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
        irslt.setFetchSize(100);
        while (irslt.next()){

          // Protein Id to Identifiers
          Integer   proteinId = new Integer(irslt.getInt(1));
          String    idntifrColumn = ConfigFile.getProperty("identifier_col") + Integer.toString(irslt.getInt(2));
          Hashtable identifierTable = (Hashtable) rsltTable.get(idntifrColumn);

          if (null == identifierTable){
            identifierTable = new Hashtable();
            rsltTable.put(idntifrColumn, identifierTable);
          }
          String  identifierName = irslt.getString(3);

          if (null != identifierName){
            identifierTable.put(proteinId, identifierName);
          }
          else{
            System.out.println("Error during identifier retrieval - Null identifier found for " + proteinId);
          }
        }
        irslt.close();
        istmt.close();
      }
      catch (SQLException se){
        System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                           + " has been returned.");
      }
      finally{
        if (null != con){
          try{
            con.close();
          }
          catch (SQLException se){
            System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
            return null;
          }
        }
      }
      return rsltTable;
    }

  /**
   * Method declaration
   *
   *
   * @param orig
   * @param delim
   * @param numPerString
   *
   * @return
   *
   * @see
   */
  protected String[] convertToString(String orig[], String delim, int numPerString){
    String[]  formatted;

    if (0 == orig.length % numPerString){
      formatted = new String[orig.length / numPerString];
    }
    else{
      formatted = new String[orig.length / numPerString + 1];
    }
    int startIndex = 0;
    int endIndex;

    if (orig.length > numPerString - 1){
      endIndex = numPerString - 1;
    }
    else{
      endIndex = orig.length - 1;
    }
    int           index = 0;
    StringBuffer  sb = new StringBuffer();

    do{
      sb.setLength(0);
      for (int i = startIndex; i <= endIndex; i++){
        if (0 != sb.length()){
          sb.append(delim);
        }
        sb.append(orig[i]);
      }
      formatted[index] = sb.toString();
      startIndex = endIndex + 1;
      if (orig.length > (endIndex + numPerString)){
        endIndex += numPerString;
      }
      else{
        endIndex = orig.length - 1;
      }
      index++;
    }
    while (startIndex <= endIndex);
    return formatted;
  }

  /**
   * Method declaration
   *
   *
   * @param book
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  protected Family getFamilyInfo(String book, String uplVersion){
    Connection  con = null;
    Family      famInfo = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        initClsLookup();
        if (null == clsIdToVersionRelease){
          return null;
        }
      }

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);
      String  query = QueryString.FAMILY;
      String  releaseClause = "";

      if (null != dateStr){
        query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }
      query = Utils.replace(query, "%2", uplVersion);
      query = Utils.replace(query, "%3", book);

      // System.out.println(query);
      Statement stmt = con.createStatement();
      ResultSet rst = stmt.executeQuery(query);

      if (rst.next()){
        famInfo = new Family();
        famInfo.id = new Integer(rst.getInt(1));
        famInfo.name = rst.getString(4);
        famInfo.eValue = rst.getString(11);
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return famInfo;
  }

  /**
   * Method declaration
   *
   *
   * @param book
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public String getFamilyName(String book, String uplVersion){
    Connection  con = null;
    String      famName = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        initClsLookup();
        if (null == clsIdToVersionRelease){
          return null;
        }
      }

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);
      String  query = QueryString.FAMILY;
      String  releaseClause = "";
      if (null != dateStr){
        query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }
      query = Utils.replace(query, "%2", uplVersion);
      query = Utils.replace(query, "%3", book);

      Statement stmt = con.createStatement();
      ResultSet rst = stmt.executeQuery(query);

      if (rst.next()){
        famName = rst.getString(4);
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return famName;
  }
  
  public AnnotationNode getNodeFromNodeId(String nodeId, String uplVersion){
        Connection con = null;
        String nodeAccession = null;
        String publicId = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }


            String query = Utils.replace(QueryString.NODE_ACC_PUBLIC_ID_SEARCH, QUERY_PARAMETER_1, nodeId);
            query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);

            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(query);

            if (rst.next()) {
                nodeAccession = rst.getString(COLUMN_NAME_ACCESSION);
                publicId = rst.getString(COLUMN_NAME_PUBLIC_ID);
            }
            rst.close();
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database, exception " + 
                               se.getMessage() + " has been returned.");
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
                    return null;
                }
            }
        }
        // Convert accession to family id
        if (null != nodeAccession) {
            AnnotationNode node = new AnnotationNode();
            node.setPublicId(publicId);
            node.setAccession(nodeAccession);
            int index = nodeAccession.indexOf(STR_COLON);
            if (index > 0) {
                node.setFamilyId(nodeAccession.substring(0, index));
                node.setAnnotationNodeId(nodeAccession.substring(index + 1));
            }
            return node;
        }
        return null;
    }
  
  /**
     * 
     * @param nodeId - public id such as PTN0001 or node accession such as PTHR10000:AN0
     * @return
     */
    public String getFamilyIdFromNodeId(String nodeId, String uplVersion) {
        Connection con = null;
        String nodeAccession = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }


            String query = Utils.replace(QueryString.NODE_SEARCH, QUERY_PARAMETER_1, nodeId);
            query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);

            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(query);

            if (rst.next()) {
                nodeAccession = rst.getString(COLUMN_NAME_ACCESSION);
            }
            rst.close();
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database, exception " + 
                               se.getMessage() + " has been returned.");
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
                    return null;
                }
            }
        }
        // Convert accession to family id
        if (null != nodeAccession) {
            int index = nodeAccession.indexOf(STR_COLON);
            if (index > 0) {
                nodeAccession = nodeAccession.substring(0, index);
            }
        }
        return nodeAccession;
    }
    

    /**
       * 
       * @param familyId 
       * @return
       */
      public Hashtable<String, String> getAnnotationNodeLookup(String familyId, String uplVersion) {
          Connection con = null;
          Statement stmt = null;
          ResultSet rst = null;
          Hashtable<String, String> nodeLookupTbl = new Hashtable<String, String>();

          try {
              con = getConnection();
              if (null == con) {
                  return null;
              }


              String query = Utils.replace(QueryString.ANNOTATION_NODE_PUBLIC_ID_LOOKUP, QUERY_PARAMETER_1, familyId);
              query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);

              stmt = con.createStatement();
              rst = stmt.executeQuery(query);

              while (rst.next()) {
                  nodeLookupTbl.put(rst.getString(COLUMN_NAME_ACCESSION), rst.getString(COLUMN_NAME_PUBLIC_ID));
              }

          } catch (SQLException se) {
              System.out.println("Unable to retrieve information from database, exception " + 
                                 se.getMessage() + " has been returned.");
          } finally {
              releaseDBResources(rst, stmt, con);
          }

          return nodeLookupTbl;
      }



  /**
   * Method declaration
   *
   *
   * @param book
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public Integer getFamilyId(String book, String uplVersion){
    Connection  con = null;
    Integer     famId = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        initClsLookup();
        if (null == clsIdToVersionRelease){
          return null;
        }
      }

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);
      String  query = QueryString.FAMILY;
//      String  releaseClause = "";

      if (null != dateStr){
        query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }
      query = Utils.replace(query, "%2", uplVersion);
      query = Utils.replace(query, "%3", book);

      Statement stmt = con.createStatement();
      ResultSet rst = stmt.executeQuery(query);

      if (rst.next()){
        famId = new Integer(rst.getInt(1));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return famId;
  }

  /**
   * Method declaration
   *
   *
   * @param clsId
   * @param parent
   *
   * @return
   *
   * @see
   */
  protected Vector getCurrentParentChild(Integer clsId, boolean parent){
    Connection  con = null;
    Vector      assCls = new Vector();

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      String  query;

      if (true == parent){
        query = QueryString.PREPARED_PARENT_CLS;
      }
      else{
        query = QueryString.PREPARED_CHILD_CLS;
      }
      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setInt(1, clsId.intValue());
      ResultSet rst = stmt.executeQuery();

      while (rst.next()){
        assCls.addElement(new Integer(rst.getInt(1)));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve child and parent associated classification records, exception "
                         + se.getMessage() + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return assCls;
  }

  /**
   * Method declaration
   *
   *
   * @param isProtein
   * @param book
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public Hashtable getComments(boolean isProtein, String book, String uplVersion){
      return new Hashtable();
      // No longer use comments
//    Connection  con = null;
//    Hashtable   cmntsTbl = null;
//
//    try{
//      con = getConnection();
//      if (null == con){
//        return null;
//      }
//
//      // Make sure release dates can be retrieved, else return null
//      if (null == clsIdToVersionRelease){
//        initClsLookup();
//        if (null == clsIdToVersionRelease){
//          return null;
//        }
//      }
//
//      // If this version of the upl has been released, then add clause to ensure only records
//      // created prior to the release date are retrieved
//      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
//      String  dateStr = (String) clsInfo.elementAt(1);
//
//      cmntsTbl = new Hashtable();
//
//      // PreparedStatement stmt;
//      String  query;
//
//      if (true == isProtein){
//        query = QueryString.PROTEIN_COMMENT;
//        String  creationClause = "";
//
//        if (null != dateStr){
//          creationClause = Utils.replace(QueryString.CREATION_DATE_CLAUSE, "tblName", "c");
//          creationClause = Utils.replace(creationClause, "%1", dateStr);
//        }
//        query = Utils.replace(query, "%1", creationClause);
//        query = Utils.replace(query, "%2", uplVersion);
//        query = Utils.replace(query, "%3", book + ":%");
//
//        // stmt = con.prepareStatement(query);
//        // stmt.setInt(1, Integer.parseInt(uplVersion));
//        // stmt.setString(2, book + ":%");
//        // if (null != dateStr) {
//        // stmt.setString(3, dateStr);
//        // }
//      }
//      else{
//        query = QueryString.CLS_COMMENT;
//        String  creationClause = "";
//
//        if (null != dateStr){
//          creationClause = Utils.replace(QueryString.CREATION_DATE_CLAUSE, "tblName", "c");
//          creationClause = Utils.replace(creationClause, "%1", dateStr);
//        }
//        else{
//          creationClause = Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
//        }
//        query = Utils.replace(query, "%1", creationClause);
//        query = Utils.replace(query, "%2", uplVersion);
//        query = Utils.replace(query, "%3", ConfigFile.getProperty(uplVersion + "_subfamLevel"));
//        query = Utils.replace(query, "%4", book + ":%");
//
//        // stmt = con.prepareStatement(query);
//        // stmt.setInt(1, Integer.parseInt(uplVersion));
//        // int depth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_subfamLevel"));
//        // stmt.setInt(2, depth);
//        // stmt.setString(3, book + ":%");
//        // if (null != dateStr) {
//        // stmt.setString(4, dateStr);
//        // }
//      }
//
//      // System.out.println(query);
//      Statement stmt = con.createStatement();
//      ResultSet rst = stmt.executeQuery(query);
//
//      rst.setFetchSize(100);
//      StringBuffer  commentBfr = new StringBuffer();
//
//      while (rst.next()){
//        Integer           protId = new Integer(rst.getInt(1));
//        Clob              comment = rst.getClob(2);
//        CallableStatement cstmt1 = (CallableStatement) con.prepareCall("begin ? := dbms_lob.getLength (?); end;");
//        CallableStatement cstmt2 = (CallableStatement) con.prepareCall("begin dbms_lob.read (?, ?, ?, ?); end;");
//
//        cstmt1.registerOutParameter(1, Types.NUMERIC);
//        cstmt1.setClob(2, comment);
//        cstmt1.execute();
//        long  length = cstmt1.getLong(1);
//        long  index = 0;
//
//        commentBfr.setLength(0);
//        while (index < length){
//          cstmt2.setClob(1, comment);
//          cstmt2.setLong(2, CLOB_BUFFER_LENGTH);
//          cstmt2.registerOutParameter(2, Types.NUMERIC);
//          cstmt2.setLong(3, index + 1);
//          cstmt2.registerOutParameter(4, Types.VARCHAR);
//          cstmt2.execute();
//          long  thisread = cstmt2.getLong(2);
//
//          commentBfr.append(cstmt2.getString(4));
//          index += thisread;
//        }
//
//        // // Only save string, if length is greater than zero when white space has been removed
//        // String commentStr = commentBfr.toString();
//        // commentStr.trim();
//        // if (0 < commentStr.length()) {
//        cmntsTbl.put(protId, commentBfr.toString());
//
//        // }
//        // Close statements
//        cstmt1.close();
//        cstmt2.close();
//      }
//      rst.close();
//      stmt.close();
//    }
//    catch (SQLException se){
//      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
//                         + " has been returned.");
//    }
//    finally{
//      if (null != con){
//        try{
//          con.close();
//        }
//        catch (SQLException se){
//          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
//          return null;
//        }
//      }
//    }
//    return cmntsTbl;
  }

  // public Hashtable getProtToXlinks(String protList[], String evidenceIds) {
  // Connection con = null;
  // Hashtable seqToXlinks = null;
  // try {
  // con = getConnection();
  // if (null == con) {
  // return null;
  // }
  // Statement stmt = con.createStatement();
  // String[] protStr = convertToString(protList, COMMA_DELIM, NUM_IN_STMT);
  // seqToXlinks = new Hashtable();
  // for (int i = 0; i < protStr.length; i++) {
  // String query = QueryString.XLINKS;
  // query = Utils.replace(query, "%1", protStr[i]);
  // query = Utils.replace(query, "%2", evidenceIds);
  // ResultSet rst = stmt.executeQuery(query);
  // while (rst.next()) {
  // Integer protId = new Integer(rst.getInt(1));
  // Hashtable linkLookup = (Hashtable)seqToXlinks.get(protId);
  // if (null == linkLookup) {
  // linkLookup = new Hashtable();
  // seqToXlinks.put(protId, linkLookup);
  // }
  // String xlinkType = rst.getString(2);
  // Vector xLinkList = (Vector)linkLookup.get(xlinkType);
  // if (null == xLinkList) {
  // xLinkList = new Vector();
  // linkLookup.put(xlinkType, xLinkList);
  // }
  // xLinkList.addElement(rst.getString(3));
  // }
  // rst.close();
  // }
  // stmt.close();
  //
  // // Each sequence is associated to a hashtable of link types to vector of links.  Change so that
  // // each sequence is associated to a String
  // Enumeration protIds = seqToXlinks.keys();
  // StringBuffer sb = new StringBuffer();
  // while (protIds.hasMoreElements()) {
  // sb.setLength(0);
  // Integer protId = (Integer)protIds.nextElement();
  // Hashtable linkTbl = (Hashtable)seqToXlinks.get(protId);
  // Enumeration linkTypes = linkTbl.keys();
  // while (linkTypes.hasMoreElements()) {
  // if (0 != sb.length()) {
  // sb.append(SPACE_DELIM);
  // }
  // String link = (String)linkTypes.nextElement();
  // sb.append(link);
  // sb.append(LINKS_ASSOC);
  // Vector links = (Vector)linkTbl.get(link);
  // for (int i = 0; i < links.size(); i++) {
  // if (i != 0) {
  // sb.append(COMMA_DELIM);
  // }
  // sb.append((String)links.elementAt(i));
  // }
  // }
  // seqToXlinks.put(protId, sb.toString());
  // }
  // }
  // catch (SQLException se) {
  // System.out.println("Unable to retrieve information from database, exception " + se.getMessage() + " has been returned.");
  // }
  // finally {
  // if (null != con) {
  // try {
  // con.close();
  // }
  // catch (SQLException se) {
  // System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
  // return null;
  // }
  // }
  // }
  // return seqToXlinks;
  // }

  /**
     * Method declaration
     *
     *
     * @param isProtein
     * @param separateModAndNonModEvdnce
     * @param book
     * @param evidenceIds
     * @param userId
     * @param uplVersion
     *
     * @return
     *
     * @see
     */
    public Hashtable getEvidence(boolean isProtein,
                                 boolean separateModAndNonModEvdnce,
                                 String book, String evidenceIds, int userId,
                                 String uplVersion) {
        if (null == evidenceIds || 0 == evidenceIds.length()) {
            return null;
        }
        Connection con = null;
        Hashtable xlinksTable = new Hashtable();
        Hashtable modEvdnceTable = null;
        Hashtable nonModEvdnceTable = null;
        Hashtable evidenceTable = null;

        //
        if (true == separateModAndNonModEvdnce) {
            modEvdnceTable = new Hashtable();
            nonModEvdnceTable = new Hashtable();
        } else {
            evidenceTable = new Hashtable();
        }

        // Ensure user id to rank table is initialized
        initUserIdToRankLookup();
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                return null;
            }


            Hashtable current;

            // PreparedStatement stmt;
            String query;

            if (true == isProtein) {
                query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
                query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_p);
                query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_pn);
                query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_e);
                query = QueryString.NEW_PROTEIN_EVIDENCE + query;


                query = Utils.replace(query, QUERY_PARAMETER_3, evidenceIds);

                query = Utils.replace(query, QUERY_PARAMETER_1, uplVersion);
                query = Utils.replace(query, QUERY_PARAMETER_2, book + QUERY_WILDCARD);


            }
            else {
            
                query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
                query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_c);
                query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_a);
                query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_e);
                query = QueryString.CLS_EVIDENCE + query;

                query = Utils.replace(query, QUERY_PARAMETER_1, uplVersion);
                query = Utils.replace(query, QUERY_PARAMETER_2, book + QUERY_WILDCARD);
                query = Utils.replace(query, QUERY_PARAMETER_3, ConfigFile.getProperty(uplVersion + LEVEL_SUBFAMILY ));
                query = Utils.replace(query, QUERY_PARAMETER_4, evidenceIds);

            }
            String userRank = null;

            if (null != userIdToRankLookup) {
                userRank = (String)userIdToRankLookup.get(new Integer(userId));
            }
            System.out.println(query);
            Statement stmt = con.createStatement();

            // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            // System.out.println("start of evidence execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            ResultSet rst = stmt.executeQuery(query);
        
            // System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            rst.setFetchSize(100);
            while (rst.next()) {
                String annotId = rst.getString(COLUMN_NAME_ACCESSION);

                // Determine what type of evidence this is i.e. xlinks, evidence
                // modifiable by user or evidence non modifable by user.
                int isEditable = rst.getInt(4);

                if (0 == isEditable) {
                    current = xlinksTable;
                } else if (false == separateModAndNonModEvdnce) {
                    current = evidenceTable;
                } else {

                    // If user information is unavailable, indicate evidence cannot be modified
                    if (null == userRank) {
                        current = nonModEvdnceTable;
                    } else {
                        int createdBy = rst.getInt(5);

                        if (createdBy == userId) {
                            current = modEvdnceTable;
                        } else {
                            String evdnceRank =
                                (String)userIdToRankLookup.get(Integer.toString(createdBy));

                            if ((null == evdnceRank) || (null == userRank)) {
                                current = nonModEvdnceTable;
                            } else if (Integer.parseInt(evdnceRank) <
                                       Integer.parseInt(userRank)) {
                                current = modEvdnceTable;
                            } else {
                                current = nonModEvdnceTable;
                            }
                        }
                    }
                }
                Hashtable linkLookup = (Hashtable)current.get(annotId);

                if (null == linkLookup) {
                    linkLookup = new Hashtable();
                    current.put(annotId, linkLookup);
                }
                String xlinkType = rst.getString(2);
                Vector xLinkList = (Vector)linkLookup.get(xlinkType);

                if (null == xLinkList) {
                    xLinkList = new Vector();
                    linkLookup.put(xlinkType, xLinkList);
                }
                xLinkList.addElement(rst.getString(3));
            }
            rst.close();
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database, exception " +
                               se.getMessage() + " has been returned.");
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    System.out.println("Unable to close connection, exception " +
                                       se.getMessage() +
                                       " has been returned.");
                    return null;
                }
            }
        }
        Hashtable evidnceLinks = new Hashtable();

        if (null != xlinksTable) {

            // convertHashToStr(xlinksTable);
            evidnceLinks.put(ConfigFile.getProperty("xlinks_col"),
                             xlinksTable);
        }
        if (true == separateModAndNonModEvdnce) {
            if (null != modEvdnceTable) {

                // convertHashToStr (modEvdnceTable);
                evidnceLinks.put("mod_evdnce", modEvdnceTable);
            }
            if (null != nonModEvdnceTable) {

                // convertHashToStr(nonModEvdnceTable);
                evidnceLinks.put("static_evdnce", nonModEvdnceTable);
            }
        } else {
            if (null != evidenceTable) {

                // convertHashToStr(evidenceTable);
                evidnceLinks.put(ConfigFile.getProperty("evidence_col"),
                                 evidenceTable);
            }
        }
        if (true == evidnceLinks.isEmpty()) {
            return null;
        }
        return evidnceLinks;
    }

  /**
   * Method declaration
   *
   *
   * @param clsTbl
   *
   * @see
   */
  protected void convertStrToVectorForCls(Hashtable clsTbl){
    if (null == clsTbl){
      return;
    }
    Enumeration sfList = clsTbl.keys();

    while (sfList.hasMoreElements()){
      String    sfName = (String) sfList.nextElement();
      String    clsInfo = (String) clsTbl.get(sfName);

      // System.out.println(sfName + " had text " + clsInfo);
      String    clsList[] = Utils.tokenize(clsInfo, SEMI_COLON_DELIM);
      Vector    clsLblList = new Vector();

      // Keep track of categories that have already been added, do not want to add them twice
      Hashtable alreadyProcessed = new Hashtable();

      // System.out.println("Subfamily " + sfName);
      for (int i = 0; i < clsList.length; i++){
        String  lbl = clsList[i].substring(0, clsList[i].indexOf(LINKS_ASSOC));

        if (null == alreadyProcessed.get(lbl)){
          clsLblList.addElement(lbl);
          alreadyProcessed.put(lbl, lbl);

          // System.out.print(" " + lbl);
        }
      }

      // System.out.println();
      clsTbl.put(sfName, clsLblList);
    }
  }

  /**
   * Method declaration
   *
   *
   * @param evidence
   *
   * @see
   */
  protected void convertHashToStrForEvidence(Hashtable evidence){
    if (null == evidence){
      return;
    }

    // Each id is associated to a hashtable of link types to vector of links.  Change so that
    // each id is associated to a String
    Enumeration   ids = evidence.keys();
    StringBuffer  sb = new StringBuffer();

    while (ids.hasMoreElements()){
      sb.setLength(0);
      Integer     id = (Integer) ids.nextElement();
      Hashtable   linkTbl = (Hashtable) evidence.get(id);
      Enumeration linkTypes = linkTbl.keys();

      while (linkTypes.hasMoreElements()){
        if (0 != sb.length()){
          sb.append(SPACE_DELIM);
        }
        String  link = (String) linkTypes.nextElement();

        sb.append(link);
        sb.append(LINKS_ASSOC);
        Vector  links = (Vector) linkTbl.get(link);

        for (int i = 0; i < links.size(); i++){
          if (i != 0){
            sb.append(COMMA_DELIM);
          }
          sb.append((String) links.elementAt(i));
        }
      }
      evidence.put(id, sb.toString());
    }
  }

  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param book
   * @param treeInfo
   * @param attrTable
   * @param familyName
   * @param saveSts
   *
   * @return
   *
   * @see
   */
//  public String saveBookForPANTHER(String userName, String password, String uplVersion, String book, String sfAn[],
//                                String attrTable[], String familyName, int saveSts){
//
//    addBook(book, attrTable.length);
//    String  returnInfo = syncSaveBookForPANTHER(userName, password, uplVersion, book, attrTable, sfAn, familyName, saveSts);
//
//    removeBook(book);
//    return returnInfo;
//  }

  /**
   * Method declaration
   *
   *
   * @param book
   * @param num
   *
   * @see
   */
  protected synchronized void addBook(String book, int num){
    if (null == bookToNumSeq){
      bookToNumSeq = new Hashtable();
    }
    if (null != book){
      java.text.SimpleDateFormat  df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");

      bookToNumSeq.put(book,
                       book + " with " + Integer.toString(num) + " sequences added to save queue at "
                       + df.format(new java.util.Date(System.currentTimeMillis())));
    }
  }

  /**
   * Method declaration
   *
   *
   * @param book
   *
   * @see
   */
  protected synchronized void removeBook(String book){
    if (null == bookToNumSeq){
      return;
    }
    if (null != book){
      bookToNumSeq.remove(book);
    }
  }

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public String getSaveInfo(){
    if (null == bookToNumSeq){
      return "";
    }
    StringBuffer  sb = new StringBuffer();
    Enumeration   values = bookToNumSeq.elements();

    while (values.hasMoreElements()){
      String  value = (String) values.nextElement();

      sb.append(value);
      sb.append(NEWLINE_DELIM);
    }
    return sb.toString();
  }


//    protected synchronized String syncSaveBookForPANTHER(String userName,
//                                               String password,
//                                               String uplVersion, String book,
//                                               String attrTable[], String sfAn[],
//                                               String familyName,
//                                               int saveSts) {
//                                               
//        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(FORMAT_DATE);
//
//        log.info(MSG_START_SAVE_PART1 + book + Constant.STR_SPACE + df.format(new java.util.Date(System.currentTimeMillis())) + MSG_START_SAVE_PART2 + saveSts);
//
//        Hashtable<String, String> allAnnotTbl = null; // All annotation nodes table formatted as <PTHR10000:AN0, AN0>
//        Vector <Annotation> allAnnotationsList = null; // All annotations associated with the book includes subfamily and classifications
//
//        Hashtable<String, String> origAnToSfId = new Hashtable<String, String>(); // annotation id to Sf id
//        Hashtable<String, Integer> origAnToSfClsId = new Hashtable<String, Integer>();    // annotation id to sf cls id
//        Hashtable<Integer, Integer> origClsIdToAnId = new Hashtable<Integer, Integer>();   // sf clsId to annotation_id
//        Hashtable<String, String> origAnToSfName = new Hashtable<String, String>(); // annotation id to Sf name
//
//        Hashtable<String, Integer> anToProt = new Hashtable<String, Integer>(); // Protein to annotation id
//        Hashtable<String, String> anToExtProtAcc = new Hashtable<String, String>(); // An id to external protein accession or annotation id
//        Hashtable<String, String> extProtAccToAn = new Hashtable<String, String>(); // External protein acc to annotation id
//        String[]  colHeadersTblNames = Utils.tokenize(ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_COL_INFO), COMMA_DELIM);
//        
//        
//
//        
//        
//        Hashtable<String, String[]> saveTbl = new Hashtable<String, String[]>(); // AnnotationId to attribute table row
//
//        if (null == attrTable) {
//            return book + MSG_INVALID_ATTR_TABLE;
//        }
//
//        String userIdStr = getUserId(userName, password);
//
//        if (null == userIdStr) {
//            return MSG_SAVE_FAILED_CANNOT_VERIFY_USER;
//        }
//
////      Disable saving by all users        
////        if (true != userIdStr.equals("1001")) {
////           return "User cannot update now"; 
////        }
//        String clsIdStr = getClsIdForBookLockedByUser(userIdStr, uplVersion, book);
//
//        if (null == clsIdStr) {
//            return MSG_SAVE_FAILED_BOOK_NOT_LOCKED_BY_USER;
//        }
//
//        // Make sure upl has not already been released
//        if (null == clsIdToVersionRelease) {
//            initClsLookup();
//        }
//
//        // Make sure release dates can be retrieved, else return null
//        if (null == clsIdToVersionRelease) {
//            return MSG_SAVE_FAILED_ERROR_ACCESSING_DATA_FROM_DB;
//        }
//        
//        
//        
//
//        // If this version of the upl has been released, then add clause to ensure only records
//        // created prior to the release date are retrieved
//        Vector clsInfo = (Vector)clsIdToVersionRelease.get(uplVersion);
//
//        if (null == clsInfo) {
//            return MSG_SAVE_FAILED_INVALID_UPL_SPECIFIED;
//        }
//        if (null != clsInfo.elementAt(1)) {
//            return MSG_SAVE_FAILED_UPL_RELEASED;
//        }
//        
//        
//        // Get all annotation node ids 
//        allAnnotTbl = getAllNodesInTree(uplVersion, book);
//        if (null == allAnnotTbl) {
//            return book + MSG_UNABLE_TO_RETRIEVE_TREE_NODES;
//        }
//        
//        // Get all Annotations 
//        allAnnotationsList = getAnnotationsForBook(uplVersion, book);
//        if (null == allAnnotationsList) {
//            return book + MSG_UNABLE_TO_RETRIEVE_ANNOTATIONS;
//        }
//         
//        Hashtable<String, Integer> annotTypeTbl = getAnnotationTypeInfo();
//        if (null == annotTypeTbl) {
//            return MSG_UNABLE_TO_RETRIEVE_ANNOT_TYPE_ID;
//        }
//        
//        Integer notQualifierId = getNotQualifierId();
//        int notQualifierIdInt = notQualifierId.intValue();
//        if (null == notQualifierId) {
//            return MSG_UNABLE_TO_RETRIEVE_NOT_QUALIFIER_ID;
//        }
//        
//        if (null == uplToClsAccClsIdData || null == uplToClsAccClsIdData.get(uplVersion)) {
//           initClsHierarchyData(uplVersion);
//        }
//        
//        if (null == uplToClsAccClsIdData || null == uplToClsAccClsIdData.get(uplVersion)) {
//           return MSG_UNABLE_TO_RETRIEVE_CLS_HIERARCHY_DATA;
//        }
//        
//        Hashtable<String, Integer> clsAccClsIdTbl = uplToClsAccClsIdData.get(uplVersion);
//        
//        
//        // Tables for inserting and obsoleting data
//        
//        // Tree info, if family changes
//        Hashtable treeIdToTreeStr = null;
//        
//        // Classification information for inserting
//        Hashtable<Integer, String> insertSfIdSfAcc = new Hashtable<Integer, String>();
//        Hashtable<String, String> insertSfAccSfName = new Hashtable<String, String>();
//        
//        // Classification relationship information for inserting
//         Hashtable <Integer, int[]> insertSfToCR = new Hashtable <Integer, int[]>();     // Subfamily to classification relationship id
//         Hashtable <Integer, Integer> insertCRToFam = new Hashtable <Integer, Integer>();    // Classification relationship id to family id
//
//        // Annotation information for inserting
//        Vector<Annotation> addAnnotList = new Vector<Annotation>();
//        
//        // Annotation qualifiers for inserting
//        Vector<int[]> addAnnotQualifierList = new Vector<int[]>();
//        
//        
//        
//        // Classification information for obsoleting
//        Hashtable<Integer, Integer> obsoleteClsIds = new Hashtable<Integer, Integer>(); 
//        
//        // Classification relationship information for obsoleting
//        Hashtable<Integer, Vector<Integer>> obsoleteClsRltn = new Hashtable<Integer, Vector<Integer>>();
//                
//        // Annotation information for obsoleting
//        Hashtable<Integer, Integer> obsoleteAnnotTbl = new Hashtable<Integer, Integer>();
//        
//        
//
//
//
//
//
//        int uplVersionId = Integer.parseInt(uplVersion);
//        String bookWildcard = book + QUERY_WILDCARD;
//
//
//
//
//
//        Connection con = null;
//        try {
//            con = getConnection();
//
//            // Get original information for comparing with what is to be saved
//            // Get the subfamily to annotation node relationship
//            String querySfAn = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_a);
//            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_n);
//            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_c);
//            querySfAn = QueryString.PREPARED_AN_SF_NODE + querySfAn;
//            System.out.println(querySfAn);
//
//            PreparedStatement stmt = con.prepareStatement(querySfAn);
//            stmt.setInt(1, uplVersionId);
//            stmt.setString(2, bookWildcard);
//            stmt.setString(3, ConfigFile.getProperty(PROPERTY_ANNOTATION_TYPE_SF));
//
//
//            ResultSet rst = stmt.executeQuery();
//            rst.setFetchSize(100);
//
//            while (rst.next()) {
//                String sfId = rst.getString(COLUMN_NAME_ACCESSION);
//                String sfName = rst.getString(COLUMN_NAME_NAME);
//                String anId = rst.getString(5);
//                Integer clsId = Integer.valueOf(rst.getInt(COLUMN_NAME_CLASSIFICATION_ID));
//                Integer annotId = Integer.valueOf(rst.getInt(COLUMN_NAME_ANNOTATION_ID));
//
//                if (null != sfId && null != anId) {
//                    origAnToSfId.put(anId, sfId);
//                    origAnToSfClsId.put(anId, clsId);
//                    origClsIdToAnId.put(clsId, annotId);
//                } else {
//                    return book + MSG_SAVE_FAILED_SF_INFO_INAVAILABLE;
//                }
//
//                if (null != sfName) {
//                    origAnToSfName.put(anId, sfName);
//                }
//            }
//            rst.close();
//            stmt.close();
//
//
//            // Get annotation to protein accession table
//
//            String queryAnProt = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
//            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_p);
//            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_pn);
//            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_g);
//            queryAnProt = addVersionReleaseClause(uplVersion, queryAnProt, TABLE_NAME_gn);
//            queryAnProt = QueryString.PREPARED_AN_PROTEIN_ID + queryAnProt;
//            System.out.println(queryAnProt);
//
//
//            stmt = con.prepareStatement(queryAnProt);
//            stmt.setInt(1, uplVersionId);
//            stmt.setString(2, bookWildcard);
//
//            rst = stmt.executeQuery();
//            rst.setFetchSize(100);
//
//            while (rst.next()) {
//                String anId = rst.getString(COLUMN_NAME_ACCESSION);
//                Integer protId = Integer.valueOf(rst.getInt(COLUMN_NAME_PROTEIN_ID));
//                String protExtAcc = rst.getString(COLUMN_NAME_PRIMARY_EXT_ACC); // Actually gene ext acc
//
//
//                if (null != protId && null != protExtAcc) {
//                    
//                    anToExtProtAcc.put(anId, protExtAcc);
//                    extProtAccToAn.put(protExtAcc, anId);
//                } else {
//                    rst.close();
//                    stmt.close();
//                    log.error(MSG_ERROR_NULL_PROTEIN_INFO_ENCOUNTERED + book);
//                    return null;
//                }
//
//
//                anToProt.put(anId, protId);
//
//
//            }
//            rst.close();
//            stmt.close();
//
//
//        } catch (SQLException se) {
//            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
//            se.printStackTrace();
//        } finally {
//            if (null != con) {
//                try {
//                    con.close();
//                } catch (SQLException se) {
//                    String error = MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage();
//                    log.error(error);
//                    return null;
//                }
//            }
//        }
//        
//        // Before saving, ensure information in database is valid.  Ensure all nodes in tree are assigned to a subfamily
//        PANTHERTree tree = getPANTHERTree(uplVersion, book);
//        Hashtable<String, PANTHERTreeNode> treeNodesTbl = tree.getNodesTbl();
//        
//
//
//        // Get the attribute table and associate each row in attribute table to an annotation node
//        // Get the accession column, since this will be used as a key for the hash tables
//        String accColumn = ConfigFile.getProperty(PROPERTY_COL_ACC);
//        int accIndex = -1;
//
//        for (int i = 0; i < colHeadersTblNames.length; i++) {
//            if (0 == accColumn.compareTo(colHeadersTblNames[i])) {
//                accIndex = i;
//                break;
//            }
//        }
//        if (-1 == accIndex) {
//            return MSG_SAVE_FAILED_SEQ_ACC_UNAVAILABLE;
//        }
//
//        String sfNameCol = ConfigFile.getProperty(PROPERTY_COL_SF);
//        int sfNameIndex = -1;
//
//        for (int i = 0; i < colHeadersTblNames.length; i++) {
//            if (0 == sfNameCol.compareTo(colHeadersTblNames[i])) {
//                sfNameIndex = i;
//                break;
//            }
//        }
//        if (-1 == sfNameIndex) {
//            return MSG_SAVE_FAILED_SF_NAME_UNAVAILABLE;
//        }
//
//
//        for (int i = 1; i < attrTable.length; i++) {
//            String rowData = attrTable[i];
//            String cellData[] = Utils.tokenize(rowData, TAB_DELIM);
//            // Remove quotes from data
//            for (int j = 0; j < cellData.length; j++) {
//                String aField = cellData[j];
//                if (true == aField.startsWith(Constant.STR_QUOTE_SINGLE)) {
//                    aField = aField.replaceFirst(Constant.STR_QUOTE_SINGLE, Constant.STR_EMPTY);
//                }
//                if (true == aField.endsWith(Constant.STR_QUOTE_SINGLE)) {
//                    aField = aField.substring(0, aField.lastIndexOf(Constant.STR_QUOTE_SINGLE));
//        
//                }
//                cellData[j] = aField;
//            }
//            String acc = cellData[accIndex];
//
//            acc = Utils.replace(acc, QUOTE, Constant.STR_EMPTY);
//            if (0 == acc.length()) {
//                continue;
//            }
//            
//            
//            String anId = extProtAccToAn.get(acc);
//            if (anId != null) {
//                saveTbl.put(anId, cellData);
//                continue;
//            }
//            
//            
//            String completeAnId = Utils.constructExternalAnnotId(book, acc);
//            if (null == allAnnotTbl.get(completeAnId)) {
//                return book + MSG_SAVE_FAILED_INVALID_ACC_ENCOUNTERED_PART1 + acc + MSG_SAVE_FAILED_INVALID_ACC_ENCOUNTERED_PART2;
//            }
//            saveTbl.put(completeAnId, cellData);
//        }
//        
//        
//        Hashtable<String, String>tmpAnSfTbl = Utils.parseSfAnInfo(sfAn, true);
//        if (null == tmpAnSfTbl) {
//            return book + MSG_SAVE_FAILED_INVALID_SF_AN_INFO;
//        }
//        
//        Hashtable<String, String>newAnSfTbl = new Hashtable<String, String>(tmpAnSfTbl.size());
//        // Convert annotation id and sf id to external ids
//        Enumeration <String> anIdEnum = tmpAnSfTbl.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String key = anIdEnum.nextElement();
//            String value = tmpAnSfTbl.get(key);
//            String extKey = Utils.constructExternalAnnotId(book, key);
//            String extValue = Utils.constructExternalSubfamilyId(book, value);
//            newAnSfTbl.remove(key);
//            newAnSfTbl.put(extKey, extValue);
//        }
//        
//        // Check for subfamily integrity i.e. all nodes in tree belong to a subfamily
//        if (false == verifySfNodes(tree, newAnSfTbl)) {
//            return book + MSG_INVALID_SF_TO_SEQ_INFO;
//        }
//        
//        // Determine how subfamilies have changed i.e. sf to add and remove
//        Hashtable<String, String> removeAnSfId = new Hashtable<String, String>();
//        Hashtable<String, String> addAnSfid  = new Hashtable<String, String>();
//        Hashtable<String, String> addSfIdToSfName = new Hashtable<String, String>();
//        
//        anIdEnum = newAnSfTbl.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String newAnId = anIdEnum.nextElement();
//            String newSfId = newAnSfTbl.get(newAnId);
//            String row[] = saveTbl.get(newAnId);
//            //System.out.println(newAnId + " " + newSfId);
//            if (null == row) {
//                return book + MSG_INVALID_SEQUENCE_IDENTIFIER_INFO  + newAnId;
//            }
//            String newSfName = row[sfNameIndex];
//
//            
//            String origSfId = origAnToSfId.get(newAnId);
//            if (null == origSfId) {
//                addAnSfid.put(newAnId, newSfId);
//                addSfIdToSfName.put(newSfId, newSfName);
//                continue;
//            }
//            if (true != newSfId.equals(origSfId)) {
//                addAnSfid.put(newAnId, newSfId);
//                addSfIdToSfName.put(newSfId, newSfName);
//                continue;
//            }
//            
//            // Compare subfamily name
//
//           
//           String origSfName = origAnToSfName.get(newAnId);
//           if (null == origSfName) {
//               addAnSfid.put(newAnId, newSfId);
//               addSfIdToSfName.put(newSfId, newSfName);
//               continue;
//           }
//           
//           if (true != newSfName.equals(origSfName)) {
//               addAnSfid.put(newAnId, newSfId);
//               addSfIdToSfName.put(newSfId, newSfName);
//               continue;
//           }
//            
//            
//        }
//        
//        anIdEnum = origAnToSfId.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String origAnId = anIdEnum.nextElement();
//            String origSfId = origAnToSfId.get(origAnId);
//            
//            String newSfId = newAnSfTbl.get(origAnId);
//            if (null == newSfId) {
//                removeAnSfId.put(origAnId, origSfId);
//                continue;
//            }
//            
//            if (true != origSfId.equals(newSfId)) {
//                removeAnSfId.put(origAnId, origSfId);
//                continue;
//            }
//            
//            String row[] = saveTbl.get(origAnId);
//            String newSfName = row[sfNameIndex];
//            
//            String origSfName = origAnToSfName.get(origAnId);
//            if (true != newSfName.equals(origSfName)) {
//                removeAnSfId.put(origAnId, origSfId);
//                continue;
//            }
//            
//        }
//        
//        int numSfAdded = addAnSfid.size();
//        if (numSfAdded != addSfIdToSfName.size()) {
//            return book + MSG_SAVE_FAILED_LOGIC_ERR_NUM_SF_DOES_NOT_MATCH;
//        }
//        
//        
//        // Compare annotations
//        // convert string to hashtable
//        Hashtable<String, Hashtable<String, Evidence>> anToAnnot = getAnnotTbl(colHeadersTblNames, saveTbl);
//        Hashtable<String, Hashtable<String, Evidence>> origAnnotTbl = getAnnotations(book, uplVersion);
//        
//        Hashtable<String, Hashtable<String, Evidence>> anAdd = new Hashtable<String, Hashtable<String, Evidence>>();
//        Hashtable<String, Hashtable<String, Evidence>> anRemove = new Hashtable<String, Hashtable<String, Evidence>>();
//        
//        anIdEnum = anToAnnot.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String anId = anIdEnum.nextElement();
//            Hashtable<String, Evidence> newEvidenceTbl = anToAnnot.get(anId);
//            Hashtable<String, Evidence> origEvidenceTbl = origAnnotTbl.get(anId);
//            if (null == origEvidenceTbl) {
//                anAdd.put(anId, newEvidenceTbl);
//                continue;
//            }
//            Hashtable<String, Evidence> updatedTbl = getAddedEvidence(newEvidenceTbl, origEvidenceTbl);
//            if (true == updatedTbl.isEmpty()) {
//                continue;
//            }
//            anAdd.put(anId, updatedTbl);
//        }
//        
//        anIdEnum = origAnnotTbl.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String anId = anIdEnum.nextElement();
//            Hashtable<String, Evidence> newEvidenceTbl = anToAnnot.get(anId);
//            Hashtable<String, Evidence> origEvidenceTbl = origAnnotTbl.get(anId);
//            if (null == newEvidenceTbl) {
//                anRemove.put(anId, origEvidenceTbl);
//                continue;
//            }
//            Hashtable<String, Evidence> updatedTbl = getRemovedEvidence(newEvidenceTbl, origEvidenceTbl);
//            if (true == updatedTbl.isEmpty()) {
//                continue;
//            }
//            anRemove.put(anId, updatedTbl);
//
//            
//        }
//
//
//        // Check for change in family id
//        Integer famId =
//            null; // Refers to new family id or the current family id, if the family id does not
//
//        // have to be changed
//        Integer oldFamId = null; // Refers to the current family id
//        Family famInfo = getFamilyInfo(book, uplVersion);
//        String oldFamilyName = famInfo.name;
//
//        if ((null == oldFamilyName) && (null == familyName)) {
//            famId = famInfo.id;
//            if (null == famId) {
//                return book + MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_FAMILY_ID;
//            }
//            if (null == familyName) {
//                familyName = Constant.STR_EMPTY;
//            }
//            oldFamId = famId;
//        } else if (null != oldFamilyName && null == familyName ||
//                   null == oldFamilyName && null != familyName ||
//                   0 != familyName.compareTo(oldFamilyName)) {
//            int famUids[] = getUids(1);
//
//            if (null == famUids) {
//                return book +
//                    MSG_SAVE_FAILED_UNABLE_TO_GENERATE_FAMILY_ID_RECORD;
//            } else {
//                famId = new Integer(famUids[0]);
//                oldFamId = famInfo.id;
//                if (null == oldFamId) {
//                    return book +
//                        MSG_SAVE_FAILED_UNABLE_TO_GENERATE_FAMILY_ID_RECORD;
//                }
//                if (null == familyName) {
//                    familyName = Constant.STR_EMPTY;
//                }
//            }
//        } else {
//            famId = famInfo.id;
//            if (null == famId) {
//                return book + MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_FAMILY_ID;
//            }
//            oldFamId = famId;
//        }
//         
//         
//        
//        // Check if family id will be obsoleted
//        if (famId.intValue() != oldFamId.intValue()) {
//            obsoleteClsIds.put(oldFamId, oldFamId);
//
//            // New tree, if new family id is created
//            String[] treeStrings = getTree(book, uplVersion);
//            
//            treeIdToTreeStr = new Hashtable();
//            int uids[] = getUids(1);
//
//            if (null == uids) {
//                return MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//            }
//            StringBuffer sb = new StringBuffer();
//
//            for (int i = 0; i < treeStrings.length; i++) {
//                sb.append(treeStrings[i]);
//            }
//            treeIdToTreeStr.put(new Integer(uids[0]), sb.toString());
//        }
//        
//        Vector<Integer> famIdList = new Vector<Integer> (1);
//        famIdList.add(oldFamId);
//        
//        // Get classification ids of subfamilies to be obsoleted
//        anIdEnum = removeAnSfId.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String anId = anIdEnum.nextElement();
//            Integer sfClsId = origAnToSfClsId.get(anId);
//            obsoleteClsIds.put(sfClsId, sfClsId);
//            obsoleteClsRltn.put(sfClsId, famIdList);
//        }
//        
//        
//        // Get information about classification ids to be added for the subfamilies
//        int sfClsIds[] = getUids(numSfAdded);
//        if (null == sfClsIds) {
//            return book + MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//        }
//        int anIds[] = getUids(numSfAdded);
//        if (null == anIds) {
//            return book + MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//        }
//        int crIds[] = getUids(numSfAdded);
//        if (null == crIds) {
//            return book + MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//        }        
//
//
//        String sfAnnotType = ConfigFile.getProperty(PROPERTY_ANNOTATION_TYPE_SF);
//        Integer sfAnnotTypeId = annotTypeTbl.get(sfAnnotType);
//        
//         
//        anIdEnum = addAnSfid.keys();
//        int counter = 0;
//        while (anIdEnum.hasMoreElements()) {
//            String anId = anIdEnum.nextElement();
//            String sfAcc = addAnSfid.get(anId);
//            Integer clsId = Integer.valueOf(sfClsIds[counter]);
//            insertSfIdSfAcc.put(clsId, sfAcc);
//            insertSfAccSfName.put(sfAcc, addSfIdToSfName.get(sfAcc));
//            
//            int crIdList[] = new int[1];
//            crIdList[0] = crIds[counter];
//            
//            Integer crId = Integer.valueOf(crIds[counter]);
//            insertSfToCR.put(clsId, crIdList);
//            insertCRToFam.put(crId, famId);
//             
//            PANTHERTreeNode node = treeNodesTbl.get(anId);
//            String nodeIdStr = node.getNodeId();
//
//            Annotation a = new Annotation(anIds[counter], Integer.parseInt(nodeIdStr), sfClsIds[counter], sfAnnotTypeId.intValue());
//            addAnnotList.add(a);
//            counter++;
//        }
//         
//         
//        // Handle annotations
//        // Removed annotations
//        anIdEnum = anRemove.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String anId = anIdEnum.nextElement();
//            Hashtable<String, Evidence> evTbl = anRemove.get(anId);
//            PANTHERTreeNode node = treeNodesTbl.get(anId);
//            String nodeIdStr = node.getNodeId();
//            int nodeId = Integer.parseInt(nodeIdStr);
//             
//            Enumeration<String> accEnum = evTbl.keys();
//            while (accEnum.hasMoreElements()) {
//                String acc = accEnum.nextElement();
//                Integer clsId = clsAccClsIdTbl.get(acc);
//                int clsIdInt = clsId.intValue();
//                Annotation deleteRecord = null;
//                for (int i = 0; i < allAnnotationsList.size(); i++) {
//                    Annotation a = allAnnotationsList.get(i);
//                    if (a.getNodeId() == nodeId && clsIdInt == a.getClsId()) {
//                        deleteRecord = a;
//                        break;
//                    }
//
//                }
//                
//                if (null == deleteRecord) {
//                    return book + MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_ANNOT_PART1 + anId + MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_ANNOT_PART2 + clsIdInt;
//                }
//                Integer obsoleteId = Integer.valueOf(deleteRecord.getAnnotId());
//                obsoleteAnnotTbl.put(obsoleteId, obsoleteId);
//                 
//                 
//            }
//        }
//        
//        // Added annotations
//        // Need to update annotation table and also the annotation qualifier table
//        anIdEnum = anAdd.keys();
//        while (anIdEnum.hasMoreElements()) {
//            String anId = anIdEnum.nextElement();
//            Hashtable<String, Evidence> evTbl = anAdd.get(anId);
//            PANTHERTreeNode node = treeNodesTbl.get(anId);
//            String nodeIdStr = node.getNodeId();
//            int nodeId = Integer.parseInt(nodeIdStr);
//              
//            Enumeration<String> accEnum = evTbl.keys();
//            int annotIds[] = getUids(evTbl.size());
//            if (null == annotIds) {
//                return book + MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//            }
//            counter = 0;
//            while (accEnum.hasMoreElements()) {
//                String acc = accEnum.nextElement();
//                Evidence e = evTbl.get(acc);
//                Integer clsId = clsAccClsIdTbl.get(acc);
//                int clsIdInt = clsId.intValue();
//                Integer annotTypeId = getAnnotTypeId(acc, annotTypeTbl);
//                if (null == annotTypeId) {
//                    return book + MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_ANNOTATION_TYPE_ID_FOR_ACC + acc;
//                }
//                Annotation a = new Annotation(annotIds[counter], nodeId, clsIdInt, annotTypeId.intValue());
//                addAnnotList.add(a);
//
//                counter++;
//                
//                if (true == e.isNotQualifier()) {
//                    int annotQualifierInfo[] = new int[3];
//                    int ids[] = getUids(1);
//                    if (null == ids) {
//                        return book + MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//                    }
//                    annotQualifierInfo[0] = ids[0];
//                    annotQualifierInfo[1] = a.getAnnotId();
//                    annotQualifierInfo[2] = notQualifierIdInt;
//                    addAnnotQualifierList.add(annotQualifierInfo);
//                    
//                    
//                }
//            }
//        }
//        
//        // Now do updates
//        Connection  updateCon = null;
//        boolean recordsUpdated = false;
//
//        try {
//            log.info(book + MSG_START_SAVE_PART1 +
//                     df.format(new java.util.Date(System.currentTimeMillis())));
//
//            // System.out.println("Got a connection object for writing into dababase");
//            updateCon = getConnection();
//            updateCon.setAutoCommit(false);
//            updateCon.rollback();
//
//            // Insert family
//            if (famId != oldFamId) {
//                String errMsg =
//                    insertFamily(updateCon, uplVersion, userIdStr, famId, book,
//                                 familyName, Constant.STR_EMPTY,
//                                 famInfo.eValue);
//
//                if (0 != errMsg.length()) {
//                    updateCon.rollback();
//                    log.error(errMsg);
//                    return book + MSG_SAVE_FAILED_UNABLE_TO_SAVE_FAMILY_RECORD;
//                }
//                recordsUpdated = true;
//            }
//
//            // Update tree text
//            if (null != treeIdToTreeStr) {
//                Enumeration treeIds = treeIdToTreeStr.keys();
//                Integer treeId = (Integer)treeIds.nextElement();
//                String treeStr = (String)treeIdToTreeStr.get(treeId);
//                String errMsg =
//                    obsoleteOldAndAddNewTree(updateCon, treeId, treeStr, famId,
//                                             oldFamId, userIdStr);
//
//                if (0 != errMsg.length()) {
//                    log.error(errMsg);
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_UNABLE_TO_SAVE_TREE;
//                }
//
//                recordsUpdated = true;
//            }
//            
//            // Insert new subfamilies
//            if (false == insertSfIdSfAcc.isEmpty()) {
//                String errMsg = insertSubfamilies(updateCon, uplVersion, userIdStr, insertSfIdSfAcc, insertSfAccSfName);
//                
//                if (0 != errMsg.length()){
//                    updateCon.rollback();
//                    log.error(errMsg);
//                    return book + MSG_SAVE_FAILED_UNABLE_TO_INSERT_SUBFAMILIES;
//                }
//                recordsUpdated = true;
//                
//                // Since subfamily records were inserted, ensure classification relationship table is also updated
//                if (true == insertSfToCR.isEmpty()) {
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_NO_CLS_RELATIONSHIP_FOR_INSERTED_SUBFAMILIES;
//                }
//                errMsg = insertClsRltn(updateCon, insertSfToCR, insertCRToFam, userIdStr);
//                if (0 != errMsg.length()){
//                    log.error(errMsg);
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_UNABLE_TO_INSERT_FAMILY_SUBFAMILY_RELATION;
//                }
//                recordsUpdated = true;
//
//            }
//            
//            
//            // Add annotations
//            if (false == addAnnotList.isEmpty()) {
//                String errMsg = insertAnnotations(updateCon, userIdStr, addAnnotList);
//                if (0 != errMsg.length()) {
//                    log.error(errMsg);
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_UNABL_TO_INSERT_ANNOTATIONS;
//                }
//                recordsUpdated = true;
//            }
//             
//            // Annotation qualifiers for inserting
//            if (false == addAnnotQualifierList.isEmpty()) {
//               String errMsg = insertAnnotationQualifiers(updateCon, addAnnotQualifierList);
//               if (0 != errMsg.length()) {
//                   log.error(errMsg);
//                   updateCon.rollback();
//                   return book + MSG_SAVE_FAILED_UNABL_TO_INSERT_ANNOTATION_QUALIFIERS;
//               }
//               recordsUpdated = true;
//            }
//            
//            
//            // Now handle removals
//            // Classification table
//            if (false == obsoleteClsIds.isEmpty()) {
//                String errMsg = obsoleteCls(updateCon, userIdStr, obsoleteClsIds);
//                if (0 != errMsg.length()) {
//                    log.error(errMsg);
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_CLS_RECORDS;
//                }
//                recordsUpdated = true;
//            }
//            
//            
//            // Classification relationship
//            if (false == obsoleteClsRltn.isEmpty()) {
//                String errMsg = obsoleteClsRltn(updateCon, obsoleteClsRltn, userIdStr);
//                if (0 != errMsg.length()) {
//                    log.error(errMsg);
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_CLS_RLTN_RECORDS;
//                }
//                recordsUpdated = true;
//            }
//            
//            
//            // Annotations
//            if (false == obsoleteAnnotTbl.isEmpty()) {
//                String errMsg = obsoleteAnnot(updateCon, userIdStr, obsoleteAnnotTbl);
//                if (0 != errMsg.length()) {
//                    log.error(errMsg);
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_ANNOT_RECORDS;
//                }
//                recordsUpdated = true;
//            }
//            
//            
//            
//            
//            // Now determine if the family classification record has changed, if yes all parents (categories)
//            // and all children (subfamilies) with links to this record have to be obsoleted and new
//            // links should be added with the new family record.
//            if (famId != oldFamId){
//
//              // Get list of categories associated with the family
//              Vector  catList = getCurrentParentChild(oldFamId, true);
//
//              if (0 != catList.size()){
//                Hashtable obslteCatTbl = new Hashtable();
//
//                obslteCatTbl.put(oldFamId, catList);
//
//                // Obsolete the records
//                String  errMsg = obsoleteClsRltn(updateCon, obslteCatTbl, userIdStr);
//
//                if (0 != errMsg.length()){
//                    log.error(errMsg);
//                    updateCon.rollback();
//                    return book + MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_CLS_RECORDS;
//                }
//                recordsUpdated = true;
//
//                // Add new records
//                int famUids[] = getUids(catList.size());
//
//                if (null == famUids){
//                  updateCon.rollback();
//                    log.error(errMsg);
//                  return book + MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//                }
//                recordsUpdated = true;
//                Hashtable famToCRTbl = new Hashtable();
//
//                famToCRTbl.put(famId, famUids);
//                Hashtable cRToPrnt = new Hashtable();
//
//                for (int i = 0; i < famUids.length; i++){
//                  cRToPrnt.put(new Integer(famUids[i]), catList.elementAt(i));
//                }
//                errMsg = insertClsRltn(updateCon, famToCRTbl, cRToPrnt, userIdStr);
//                if (0 != errMsg.length()){
//                  updateCon.rollback();
//                    log.error(errMsg);
//                  return book + MSG_SAVE_FAILED_UNABLE_TO_INSERT_CLS_ASSOCIATED_WITH_FAMILY;
//                }
//                recordsUpdated = true;
//              }
//
//              // Now repeat for subfamiles associated with the family.  Remove subfamilies that are being obsoleted
//              Vector  subFamList = getCurrentParentChild(oldFamId, false);
//
//              for (int i = 0; i < subFamList.size(); i++){
//                Integer subFamId = (Integer) subFamList.elementAt(i);
//
//                if (null != obsoleteClsIds.get(subFamId)){
//                  subFamList.remove(i);
//                  i--;
//                }
//              }
//              Hashtable obslteSubfamTbl = new Hashtable();
//              Vector    famElem = new Vector();
//
//              famElem.addElement(oldFamId);
//              for (int i = 0; i < subFamList.size(); i++){
//
//
//                obslteSubfamTbl.put((Integer) subFamList.elementAt(i), famElem);
//              }
//
//              // Obsolete the records
//              String  errMsg = obsoleteClsRltn(updateCon, obslteSubfamTbl, userIdStr);
//
//              if (0 != errMsg.length()){
//                updateCon.rollback();
//                log.error(errMsg);
//                return book + MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_CLS_RLTN_RECORDS;
//              }
//              recordsUpdated = true;
//
//              // Add new records
//              int subfamUids[] = getUids(subFamList.size());
//
//              if (null == subfamUids){
//                updateCon.rollback();
//                log.error(errMsg);
//                return book + MSG_SAVE_FAILED_UNABLE_TO_GENERATE_IDS;
//              }
//              recordsUpdated = true;
//              Hashtable subfamToCRTbl = new Hashtable();
//              Hashtable CRTblToFam = new Hashtable();
//
//              for (int i = 0; i < subfamUids.length; i++){
//                int crId[] = new int[1];
//
//                crId[0] = subfamUids[i];
//                subfamToCRTbl.put(subFamList.elementAt(i), crId);
//                CRTblToFam.put(new Integer(crId[0]), famId);
//              }
//              errMsg = insertClsRltn(updateCon, subfamToCRTbl, CRTblToFam, userIdStr);
//              if (0 != errMsg.length()){
//                updateCon.rollback();
//                log.error(errMsg);
//                return book + MSG_SAVE_FAILED_UNABLE_TO_INSERT_FAMILY_SUBFAMILY_RELATION;
//              }
//              recordsUpdated = true;
//
//              // Obsolete the old family record
//              errMsg = obsoleteCls(updateCon, userIdStr, oldFamId);
//              if (0 != errMsg.length()){
//                updateCon.rollback();
//                log.error(errMsg);
//                return book + MSG_SAVE_FAILED_UNABL_TO_OBSOLETE_CLS_RECORDS;
//              }
//              recordsUpdated = true;
//            }
//            
//            
//            
//            
//            // Add entry to curation status table
//            // Temporarily hard code the save options.  These should be specified in a file common to both
//            // the server and the client
//            // 0 - Mark curated and unlock
//            // 1 - Save and keep locked
//            // 2 - Save and unlock
//            if ((Constant.SAVE_OPTION_MARK_CURATED_AND_UNLOCK == saveSts) || (Constant.SAVE_OPTION_SAVE_AND_UNLOCK == saveSts)){
//              if (Constant.SAVE_OPTION_MARK_CURATED_AND_UNLOCK == saveSts){
//                int     statusId = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_MANUALLY_CURATED));
//                String  errMsg = UpdateCurtionStatus(updateCon, famId, statusId, userIdStr);
//
//                if (0 != errMsg.length()){
//                    log.error(errMsg);
//                  updateCon.rollback();
//                  return "Save Operation failed - unable to insert curation status record";
//                }
//
//                // recordsUpdated = true;
//              }
//
//              // Unlock book
//              int     checkOut = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_CHECKOUT));
//              String  errMsg = deleteCurationStatus(updateCon, oldFamId, checkOut, userIdStr);
//
//              if (0 != errMsg.length()){
//                log.error(errMsg);
//                updateCon.rollback();
//                return "Save Operation failed - unable to unlock book";
//              }
//
//              // recordsUpdated = true;
//            }
//            else if (Constant.SAVE_OPTION_SAVE_AND_KEEP_LOCKED == saveSts){
//
//              // If the family id has changed, then unlock the old family id and lock the new id
//              if (famId != oldFamId){
//                int     checkOut = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_CHECKOUT));
//                String  errMsg = deleteCurationStatus(updateCon, oldFamId, checkOut, userIdStr);
//
//                if (0 != errMsg.length()){
//                    log.error(errMsg);
//                  updateCon.rollback();
//                  return "Save Operation failed - unable to unlock old book id";
//                }
//                recordsUpdated = true;
//
//                // lock with new family id
//                errMsg = lockBook(famId.toString(), userIdStr, updateCon);
//                if (0 != errMsg.length()){
//                    log.error(errMsg);
//                  updateCon.rollback();
//                  return "Save Operation failed - unable to unlock book with previous family id";
//                }
//                recordsUpdated = true;
//              }
//            }
//
//            // Add record to curation status table indicating, book has been updated
//            if ((true == recordsUpdated) && (Constant.SAVE_OPTION_MARK_CURATED_AND_UNLOCK != saveSts)){
//              int     statusId = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_PARTIALLY_CURATED));
//              String  errMsg = UpdateCurtionStatus(updateCon, famId, statusId, userIdStr);
//
//              if (0 != errMsg.length()){
//                log.error(errMsg);
//                updateCon.rollback();
//                return "Save Operation failed - unable to insert family partially curated status record";
//              }
//            }
//            
//    
//
//    
//            
//            
//
//            
//            
////                   System.out.println("Rollback during testing");
////                   updateCon.rollback();
////                   System.out.println("Rollback during testing is complete");
//             updateCon.commit();
//        } catch (Exception e) {
//            e.printStackTrace();
//            log.error(book + MSG_SAVE_FAILED_EXCEPTION_RETURNED);
//            try {
//                if (null != updateCon) {
//                    updateCon.rollback();
//                }
//                return "Logical exception, unable to save book";
//            } catch (SQLException se) {
//                se.printStackTrace();
//                log.error("Exception while rollback");
//                return "Could not roll back the update after failed save attempt";
//            }
//        } finally {
//            if (null != updateCon) {
//                try {
//                    updateCon.close();
//                } catch (SQLException se) {
//                    System.out.println("Unable to close connection, exception " +
//                                       se.getMessage() +
//                                       " has been returned.");
//                    return "Error saving changes into database";
//                }
//            }
//        }
//        System.out.println("End of update Operation for book " + book + " " +
//                           df.format(new java.util.Date(System.currentTimeMillis())));
//
//
//        System.out.println("End of Save Operation for book " + book + " " +
//                           df.format(new java.util.Date(System.currentTimeMillis())));
//
//
//        return Constant.STR_EMPTY;
//    }
  /**
   * Method declaration
   *
   *
   * @param userName
   * @param password
   * @param uplVersion
   * @param book
   * @param treeInfo
   * @param attrTable
   * @param familyName
   * @param saveSts
   *
   * @return
   *
   * @see
   */
//  protected synchronized String syncSaveBook(String userName, String password, String uplVersion, String book,
//          Vector treeInfo, String attrTable[], String familyName, int saveSts){
//          if (1 == 1) {
//            return "Saving functionality is currently disabled";
//          }
//    java.text.SimpleDateFormat  df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
//
//    System.out.println("Start of Save Operation for book " + book + " "
//                       + df.format(new java.util.Date(System.currentTimeMillis())) + " save option " + saveSts);
//
//    String  userIdStr = getUserId(userName, password);
//
//    if (null == userIdStr){
//      return "Save Operation failed - Cannot verify user information";
//    }
//    String  clsIdStr = getClsIdForBookLockedByUser(userIdStr, uplVersion, book);
//
//    if (null == clsIdStr){
//      return "Save Operation failed - Book not locked by user";
//    }
//
//    // Make sure upl has not already been released
//    if (null == clsIdToVersionRelease){
//      initClsLookup();
//    }
//
//    // Make sure release dates can be retrieved, else return null
//    if (null == clsIdToVersionRelease){
//      return "Error accessing data from database";
//    }
//
//    // If this version of the upl has been released, then add clause to ensure only records
//    // created prior to the release date are retrieved
//    Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
//
//    if (null == clsInfo){
//      return "Invalid UPL version specified.";
//    }
//    if (null != clsInfo.elementAt(1)){
//      return "UPL has already been released, changes can no-longer be saved.";
//    }
//    String      treeStrings[] = (String[]) treeInfo.elementAt(0);
//    Hashtable   sfToSeq = (Hashtable) treeInfo.elementAt(1);
//    Hashtable   seqToSf = new Hashtable();
//    Hashtable   singletonSfTbl = new Hashtable();         // Sequence to sfName
//    Hashtable   singletonSfNameTbl = new Hashtable();     // Singleton SfName to SfName
//
//    // Since the tree does not have the book name prefix for the subfamiles.  Add this now
//    Enumeration sfs = sfToSeq.keys();
//    Vector      newSfList = new Vector();
//
//    while (sfs.hasMoreElements()){
//      newSfList.addElement((String) sfs.nextElement());
//    }
//    for (int i = 0; i < newSfList.size(); i++){
//      String  key = (String) newSfList.elementAt(i);
//      Object  o = sfToSeq.remove(key);
//
//      sfToSeq.put(book + ":" + key, o);
//    }
//
//    // Create a sequence to subfamily association table
//    sfs = sfToSeq.keys();
//    while (sfs.hasMoreElements()){
//      String  sfName = (String) sfs.nextElement();
//      Vector  sequenceList = (Vector) sfToSeq.get(sfName);
//
//      for (int i = 0; i < sequenceList.size(); i++){
//        seqToSf.put(sequenceList.elementAt(i), sfName);
//        if (1 == sequenceList.size()){
//          singletonSfTbl.put(sequenceList.elementAt(i), sfName);
//          singletonSfNameTbl.put(sfName, sfName);
//        }
//      }
//    }
//
//    // If the family name has been updated, then a new family record has to be created
//    Integer famId = null;       // Refers to new family id or the current family id, if the family id does not
//
//    // have to be changed
//    Integer oldFamId = null;    // Refers to the current family id
//    Family  famInfo = getFamilyInfo(book, uplVersion);
//    String  oldFamilyName = famInfo.name;
//
//    if ((null == oldFamilyName) && (null == familyName)){
//      famId = famInfo.id;
//      if (null == famId){
//        return "Save Operation failed - unable to get family id";
//      }
//      if (null == familyName){
//        familyName = "";
//      }
//      oldFamId = famId;
//    }
//    else if (null != oldFamilyName && null == familyName || null == oldFamilyName && null != familyName
//             || 0 != familyName.compareTo(oldFamilyName)){
//      int famUids[] = getUids(1);
//
//      if (null == famUids){
//        return "Save Operation failed - Cannot generate id to add a family record";
//      }
//      else{
//        famId = new Integer(famUids[0]);
//        oldFamId = famInfo.id;
//        if (null == oldFamId){
//          return "Save Operation failed - unable to get family id";
//        }
//        if (null == familyName){
//          familyName = "";
//        }
//      }
//    }
//    else{
//
//      // Now it is time to save the information
//      // Commit changes only if everything will be successful
//      famId = famInfo.id;
//      if (null == famId){
//        return "Save Operation failed - unable to get family id";
//      }
//      oldFamId = famId;
//    }
//    Connection  con = null;
//    Hashtable   sfIdToSfAccName = new Hashtable();    // Subfamily id to accession and name
//    Hashtable   sfAccToSfId = new Hashtable();        // Subfamily acc to Subfamily id
//    Hashtable   sfToProt = new Hashtable();           // Subfamily id to list of associated protein ids's
//    Hashtable   sfAccToProtAcc = new Hashtable();     // Subfamily Accession to protein accession
//    Hashtable   protToProtAcc = new Hashtable();      // Protein id to protein accession
//    Hashtable   protAccToProt = new Hashtable();      // Protein accession to protein id
//
//    // Vector pcList = new Vector();
//    // Vector protIds = new Vector();
//    Vector      intProtIds = new Vector();
//
//    // String protArray[] = null;
//    System.out.println("Start of Save-query Operation PART1 " + df.format(new java.util.Date(System.currentTimeMillis())));
//    try{
//      con = getConnection();
//      if (null == con){
//        return null;
//      }
//      String  query = QueryString.ASSOCIATED_SUBFAM_AND_SEQ;
//
//      query = Utils.replace(query, "%1", uplVersion);
//      query = Utils.replace(query, "%2", book + ":%");
//      Statement stmt = con.createStatement();
//
//      // First get the list of associated subfamilies
//      // PreparedStatement stmt = con.prepareStatement(query);
//      // System.out.println(query);
//      // stmt.setInt(1, Integer.parseInt(uplVersion));
//      // stmt.setString(2, book + ":%");
//      ResultSet rst = stmt.executeQuery(query);
//
//      rst.setFetchSize(100);
//      while (rst.next()){
//
//        // Create a table of the subfamily id to its accession and name
//        String  sfAccName[] = new String[2];
//
//        sfAccName[0] = rst.getString(2);
//        sfAccName[1] = rst.getString(3);
//        if (null == sfAccName[1]){
//          sfAccName[1] = "";
//        }
//        Integer sfId = new Integer(rst.getInt(1));
//
//        sfIdToSfAccName.put(sfId, sfAccName);
//        sfAccToSfId.put(sfAccName[0], sfId);
//
//        // System.out.println("Processed sf accession " + sfAccName[0] + " with sfid = " + sfId.intValue());
//        // Create a list of the protein classification ids.  This will be used to retrieve sequence
//        // related information without having to join the CLASSIFICATION table
//        // pcList.addElement(Integer.toString(rst.getInt(4)));
//        // Create the list of associated proteins for the subfamily
//        Integer protId = new Integer(rst.getInt(5));
//
//        // protIds.addElement(protId.toString());
//        intProtIds.addElement(protId);
//        Vector  protList = (Vector) sfToProt.get(sfId);
//
//        if (null == protList){
//          protList = new Vector();
//          sfToProt.put(sfId, protList);
//        }
//        protList.addElement(protId);
//        String  sfName = sfAccName[0];
//        Vector  protAccList = (Vector) sfAccToProtAcc.get(sfName);
//
//        if (null == protAccList){
//          protAccList = new Vector();
//          sfAccToProtAcc.put(sfName, protAccList);
//        }
//        String  protAcc = rst.getString(6);
//
//        protAccList.addElement(protAcc);
//
//        // Create the protein id to accession lookup
//        protToProtAcc.put(protId, protAcc);
//        protAccToProt.put(protAcc, protId);
//      }
//      rst.close();
//      stmt.close();
//
//      // Get the protein list into an array
//      // protArray = new String[protIds.size()];
//      // protIds.copyInto(protArray);
//    }
//    catch (SQLException se){
//      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
//                         + " has been returned.");
//    }
//    finally{
//      if (null != con){
//        try{
//          con.close();
//        }
//        catch (SQLException se){
//          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
//        }
//      }
//    }
//    System.out.println("End of Save-query Operation PART1 " + df.format(new java.util.Date(System.currentTimeMillis())));
//    Hashtable   singletonSfTable = new Hashtable();
//    Enumeration sfList = sfIdToSfAccName.keys();
//
//    // Vector sfIds = new Vector();
//    while (sfList.hasMoreElements()){
//      Integer sfId = (Integer) sfList.nextElement();
//
//      // sfIds.addElement(sfId.toString());
//      Vector  sequences = (Vector) sfToProt.get(sfId);
//
//      if (1 == sequences.size()){
//        singletonSfTable.put(sfId, sfId);
//      }
//    }
//
//    // String[] sfArray = new String[sfIds.size()];
//    // sfIds.copyInto(sfArray);
//    Vector    sfToSeqChangeList = null;
//    Vector    sfToNameList = null;
//    Vector    sfCmntChangeList = null;
//    Vector    seqCmntChangeList = null;
//    Vector    clsChangeList = null;
//    Hashtable clsForRemoval = new Hashtable();        // Whenever a classification is being obsoleted, save it in
//
//    // this table.  This will make it easier to remove it in one
//    // shot for all the tables that contain its id
//    // When subfamily name (annotation) changes, the comments, evidence and features have to be carried over from
//    // the old subfamily record to the new subfamily record
//    Hashtable commentCarryOver = new Hashtable();     // oldSfId to new SfId
//    Hashtable evidenceCarryOver = new Hashtable();    // oldSfId to new SfId
//    Hashtable featureCarryOver = new Hashtable();     // oldSfId to new SfId
//    String[]  colHeadersTblNames = Utils.tokenize(ConfigFile.getProperty(uplVersion + "_colInfo"), COMMA_DELIM);
//    //String[]  colHeaders = Utils.tokenize(ConfigFile.getProperty(uplVersion + "_colHeaders"), COMMA_DELIM);
//
//    // Get the accession column, since this will be used as a key for the hash tables
//    String    accColumn = ConfigFile.getProperty("acc_col");
//    int       accIndex = -1;
//
//    for (int i = 0; i < colHeadersTblNames.length; i++){
//      if (0 == accColumn.compareTo(colHeadersTblNames[i])){
//        accIndex = i;
//        break;
//      }
//    }
//    if (-1 == accIndex){
//      return "Save Operation failed - unable to retrieve sequence accession information";
//    }
//
//    // Create hashtable of subfamilies to names
//    String  sfNameCol = ConfigFile.getProperty("sf_col");
//    int     sfNameIndex = -1;
//
//    for (int i = 0; i < colHeadersTblNames.length; i++){
//      if (0 == sfNameCol.compareTo(colHeadersTblNames[i])){
//        sfNameIndex = i;
//        break;
//      }
//    }
//    if (-1 == sfNameIndex){
//      return "Save Operation failed - unable to retrieve subfamily annotation information";
//    }
//    Hashtable sfToNameTbl = new Hashtable();
//
//    for (int i = 1; i < attrTable.length; i++){
//      String  rowData = attrTable[i];
//      String  cellData[] = Utils.tokenize(rowData, TAB_DELIM);
//      String  acc = cellData[accIndex];
//
//      acc = Utils.replace(acc, QUOTE, "");
//      if (0 == acc.length()){
//        continue;
//      }
//      acc = book + ":" + acc;
//      String  sfAcc = null;
//
//      if (null != sfToSeq.get(acc)){
//        sfAcc = acc;
//      }
//      else{
//        String  singletonSf = (String) singletonSfTbl.get(acc.substring(acc.indexOf(":") + 1, acc.length()));
//
//        if (null != singletonSf){
//          sfAcc = singletonSf;
//        }
//      }
//      if (null != sfAcc){
//        String  cellContents = cellData[sfNameIndex];
//
//        cellContents = Utils.replace(cellContents, QUOTE, "");
//        Vector  v = new Vector();
//
//        v.add(cellContents);
//        sfToNameTbl.put(sfAcc, v);
//      }
//    }
//
//    // Now create a hashtable of the original subfamily to annotations
//    Hashtable   origSfToName = new Hashtable();
//    Collection  c = sfIdToSfAccName.values();
//    Object      sfInfoArray[] = c.toArray();
//
//    for (int i = 0; i < sfInfoArray.length; i++){
//      String  sfAccName[] = (String[]) sfInfoArray[i];
//
//      // String sfModName = sfAccName[0].substring(sfAccName[0].indexOf(":") + 1, sfAccName[0].length());
//      Vector  v = new Vector();
//
//      v.addElement(sfAccName[1]);
//      origSfToName.put(sfAccName[0], v);
//    }
//
//    // Determine which subfamilies to add and which to remove
//    sfToNameList = compareStringToVectorHash(sfToNameTbl, origSfToName);
//
//    // Create a vector with two hashtables.  One to indicate the list of subfamilies to sequences for adding
//    // and one for list of subfamilies to sequences for deleting
//    sfToSeqChangeList = compareStringToVectorHash(sfToSeq, sfAccToProtAcc);
//
//    // make a copy of added sequences for later use
//    Hashtable origAddSeq = new Hashtable();
//
//    if (null != sfToSeqChangeList){
//      Hashtable   seqAdded = (Hashtable) sfToSeqChangeList.elementAt(0);
//      Enumeration sfsAdded = seqAdded.keys();
//
//      while (sfsAdded.hasMoreElements()){
//        String  key = (String) sfsAdded.nextElement();
//
//        origAddSeq.put(key, key);
//      }
//    }
//
//    // Now deal with comments.
//    String  commentColumn = ConfigFile.getProperty("comment_col");
//    int     commIndex = -1;
//
//    for (int i = 0; i < colHeadersTblNames.length; i++){
//      if (0 == commentColumn.compareTo(colHeadersTblNames[i])){
//        commIndex = i;
//        break;
//      }
//    }
//    if (-1 == commIndex){
//      return "Save Operation failed - unable to handle comment information";
//    }
//    Hashtable seqToCommentTbl = new Hashtable();
//    Hashtable sfToCommentTbl = new Hashtable();
//
//    // Hashtable to keep track of empty comments
//    Hashtable tmpEmptySeqToCommentTbl = new Hashtable();
//    Hashtable tmpEmptySfToCommentTbl = new Hashtable();
//    boolean   emptyComment;     // Use to keep track of empty comments
//
//    // Start from first row, since no need to handle title row
//    for (int i = 1; i < attrTable.length; i++){
//      emptyComment = false;
//      String  rowData = attrTable[i];
//      String  cellData[] = Utils.tokenize(rowData, TAB_DELIM);
//
//      // Do not save, if empty comment has been added
//      String  cellContents = cellData[commIndex];
//
//      cellContents = Utils.replace(cellContents, QUOTE, "");
//      String  tmpStr = cellContents.trim();
//
//      if (0 == tmpStr.length()){
//        emptyComment = true;
//      }
//
//      // Put comment into a vector, so that existing method for comparison can be used
//      Vector  cmntList = new Vector();
//
//      cmntList.addElement(cellContents);
//
//      // Subfamily comment
//      String  acc = Utils.replace(cellData[accIndex], QUOTE, "");
//
//      if (0 == acc.length()){
//        continue;
//      }
//      if (null == protAccToProt.get(acc)){
//        sfToCommentTbl.put(book + ":" + acc, cmntList);
//        if (true == emptyComment){
//          tmpEmptySfToCommentTbl.put(book + ":" + acc, book + ":" + acc);
//        }
//      }
//      else{
//        seqToCommentTbl.put(acc, cmntList);
//        if (true == emptyComment){
//          tmpEmptySeqToCommentTbl.put(acc, acc);
//        }
//      }
//    }
//
//    // Get sequence comment table
//    // Since this table contains protId to comment, change this to protAcc to comment
//    // so that it can be compared with the attr table
//    // System.out.println("Start of Comment seq-query Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
//    Hashtable   dbCommentsTable = getComments(true, book, uplVersion);
//
//    // System.out.println("End of Comment seq-query Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
//    Hashtable   dbSeqToCommentsTable = new Hashtable();
//    Enumeration protKey = dbCommentsTable.keys();
//
//    while (protKey.hasMoreElements()){
//      Integer protId = (Integer) protKey.nextElement();
//      Object  o = dbCommentsTable.remove(protId);
//      String  protAcc = (String) protToProtAcc.get(protId);
//      Vector  v = new Vector();
//
//      v.addElement(o);
//      dbSeqToCommentsTable.put(protAcc, v);
//    }
//
//    // Get the subfamily comments table and remove singletons since this information is stored with
//    // the sequence
//    // System.out.println("Start of Comment subfam-query Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
//    Hashtable   dbSfComment = getComments(false, book, uplVersion);
//
//    // System.out.println("End of Comment subfam-query Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
//    Enumeration singletons = singletonSfTable.keys();
//
//    while (singletons.hasMoreElements()){
//      dbSfComment.remove(singletons.nextElement());
//    }
//
//    // Since this table contains clsId to comment, change this to sfAcc to comment
//    // so that it can be compared with the attr table
//    sfList = dbSfComment.keys();
//    Vector  sfCommentList = new Vector();
//
//    while (sfList.hasMoreElements()){
//      sfCommentList.addElement(sfList.nextElement());
//    }
//    for (int i = 0; i < sfCommentList.size(); i++){
//      Integer sfId = (Integer) sfCommentList.elementAt(i);
//      Object  o = dbSfComment.remove(sfId);
//      String  sfAccName[] = (String[]) sfIdToSfAccName.get(sfId);
//
//      // String sfModName = sfAccName[0].substring(sfAccName[0].indexOf(":") + 1, sfAccName[0].length());
//      Vector  v = new Vector();
//
//      v.addElement(o);
//      dbSfComment.put(sfAccName[0], v);
//    }
//
//    // Check for empty comments.  If the attribute table has an empty comment and that does not exist in the
//    // database, remove the empty comment
//    // Sequences
//    Enumeration emptySeqComments = tmpEmptySeqToCommentTbl.keys();
//
//    while (emptySeqComments.hasMoreElements()){
//      String  key = (String) emptySeqComments.nextElement();
//
//      if (null == dbSeqToCommentsTable.get(key)){
//        seqToCommentTbl.remove(key);
//      }
//    }
//
//    // Repeat for subfamilies
//    Enumeration emptySfComments = tmpEmptySfToCommentTbl.keys();
//
//    while (emptySfComments.hasMoreElements()){
//      String  key = (String) emptySfComments.nextElement();
//
//      if (null == dbSfComment.get(key)){
//        sfToCommentTbl.remove(key);
//      }
//    }
//
//    // Now do the comparisons.  Have to do two comparisons.  One for the sequences
//    // and one for the subfamilies
//    seqCmntChangeList = compareStringToVectorHash(seqToCommentTbl, dbSeqToCommentsTable);
//    sfCmntChangeList = compareStringToVectorHash(sfToCommentTbl, dbSfComment);
//
//    // Now do the changes.
//    // The tree information has to be changed, if the subfamily to sequence information has changed or
//    // the family id has changed
//    Hashtable treeIdToTreeStr = null;
//
//    if ((null != sfToSeqChangeList) || (famId != oldFamId)){
//      treeIdToTreeStr = new Hashtable();
//      int uids[] = getUids(1);
//
//      if (null == uids){
//        return "Save Operation failed - Cannot generate uids to save tree";
//      }
//      StringBuffer  sb = new StringBuffer();
//
//      for (int i = 0; i < treeStrings.length; i++){
//        sb.append(treeStrings[i]);
//      }
//      treeIdToTreeStr.put(new Integer(uids[0]), sb.toString());
//    }
//
//    // Deal with classification data after determining the newly added subfamily records
//    // Handle the subfamily add/remove functionality.  There are two instances when
//    // subfamilies will be added/removed.  1.  Subfamily to sequence relationship changes.
//    // 2.  Subfamily annotation/name changes.
//    // if (null != sfToSeqChangeList) {
//    // Hashtable addTable = (Hashtable)sfToSeqChangeList.elementAt(0);
//    // Hashtable removeTable = (Hashtable)sfToSeqChangeList.elementAt(1);
//    // Enumeration addKeys = addTable.keys();
//    // while (addKeys.hasMoreElements()) {
//    // String key = (String)addKeys.nextElement();
//    // System.out.println("Adding sf to sequence " + key);
//    // Vector results = (Vector)addTable.get(key);
//    // for (int i = 0; i < results.size(); i++) {
//    // System.out.print(results.elementAt(i) + " ");
//    // }
//    // System.out.println();
//    // }
//    // Enumeration removeKeys = removeTable.keys();
//    // while(removeKeys.hasMoreElements()) {
//    // String key = (String)removeKeys.nextElement();
//    // System.out.println("Removing sf to sequence " + key);
//    // Vector results = (Vector)removeTable.get(key);
//    // for (int i = 0; i < results.size(); i++) {
//    // System.out.print(results.elementAt(i) + " ");
//    // }
//    // System.out.println();
//    // }
//    // }
//    // if (null != sfToNameList) {
//    // Hashtable addTable = (Hashtable)sfToNameList.elementAt(0);
//    // Hashtable removeTable = (Hashtable)sfToNameList.elementAt(1);
//    // Enumeration addKeys = addTable.keys();
//    // while (addKeys.hasMoreElements()) {
//    // String key = (String)addKeys.nextElement();
//    // System.out.println("Adding sf name" + key);
//    // Vector results = (Vector)addTable.get(key);
//    // for (int i = 0; i < results.size(); i++) {
//    // System.out.print(results.elementAt(i) + " ");
//    // }
//    // System.out.println();
//    // }
//    // Enumeration removeKeys = removeTable.keys();
//    // while(removeKeys.hasMoreElements()) {
//    // String key = (String)removeKeys.nextElement();
//    // System.out.println("Removing  sf name" + key);
//    // Vector results = (Vector)removeTable.get(key);
//    // for (int i = 0; i < results.size(); i++) {
//    // System.out.print(results.elementAt(i) + " ");
//    // }
//    // System.out.println();
//    // }
//    // }
//    Hashtable seqAddTable;
//    Hashtable seqRemoveTable;
//
//    if (null != sfToSeqChangeList){
//      seqAddTable = (Hashtable) sfToSeqChangeList.elementAt(0);
//      seqRemoveTable = (Hashtable) sfToSeqChangeList.elementAt(1);
//    }
//    else{
//      seqAddTable = new Hashtable();
//      seqRemoveTable = new Hashtable();
//    }
//    Hashtable sfNameAddTable;
//    Hashtable sfNameRemoveTable;
//
//    if (null != sfToNameList){
//      sfNameAddTable = (Hashtable) sfToNameList.elementAt(0);
//      sfNameRemoveTable = (Hashtable) sfToNameList.elementAt(1);
//    }
//    else{
//      sfNameAddTable = new Hashtable();
//      sfNameRemoveTable = new Hashtable();
//    }
//    Hashtable   finalAddTbl = new Hashtable();
//    Hashtable   finalRemoveTbl = new Hashtable();
//    Enumeration seqList = seqAddTable.keys();
//
//    while (seqList.hasMoreElements()){
//      String  sfAcc = (String) seqList.nextElement();
//      Vector  sfAnnotInfo = (Vector) sfNameAddTable.get(sfAcc);
//
//      if (null == sfAnnotInfo){
//
//        // Check if subfamily to sequence information has changed but, the
//        // subfamily annotation has not changed
//        Vector  origSfAnnotInfo = (Vector) origSfToName.get(sfAcc);
//
//        if (null != origSfAnnotInfo){
//          String  origSfAnnot = (String) origSfAnnotInfo.elementAt(0);
//
//          finalAddTbl.put(sfAcc, origSfAnnot);
//        }
//        else{
//          finalAddTbl.put(sfAcc, "");
//        }
//      }
//      else{
//        String  sfAnnot = (String) sfAnnotInfo.elementAt(0);
//
//        finalAddTbl.put(sfAcc, sfAnnot);
//      }
//    }
//    seqList = seqRemoveTable.keys();
//    while (seqList.hasMoreElements()){
//      String  sfAcc = (String) seqList.nextElement();
//      Integer sfId = (Integer) sfAccToSfId.get(sfAcc);
//
//      finalRemoveTbl.put(sfAcc, sfId);
//    }
//    Enumeration sfAnnotList = sfNameAddTable.keys();
//
//    while (sfAnnotList.hasMoreElements()){
//      String  sfAcc = (String) sfAnnotList.nextElement();
//
//      finalAddTbl.put(sfAcc, (String) ((Vector) sfNameAddTable.get(sfAcc)).elementAt(0));
//      seqAddTable.put(sfAcc, sfToSeq.get(sfAcc));
//    }
//    sfAnnotList = sfNameRemoveTable.keys();
//    while (sfAnnotList.hasMoreElements()){
//      String  sfAcc = (String) sfAnnotList.nextElement();
//
//      // String modSfAcc = book + ":" + sfAcc;
//      Integer sfId = (Integer) sfAccToSfId.get(sfAcc);
//
//      finalRemoveTbl.put(sfAcc, sfId);
//      seqRemoveTable.put(sfAcc, sfAccToProtAcc.get(sfAcc));
//    }
//
//    /*
//     * //    System.out.println("Added subfamilies");
//     * Enumeration keys = finalAddTbl.keys();
//     * while (keys.hasMoreElements()) {
//     * //      System.out.print((String)keys.nextElement() + " ");
//     * }
//     * //    System.out.println();
//     */
//
//    // System.out.println("Removed subfamilies");
//    Enumeration keys = finalRemoveTbl.keys();
//
//    while (keys.hasMoreElements()){
//      String  sfAcc = (String) keys.nextElement();
//      Integer sfId = (Integer) sfAccToSfId.get(sfAcc);
//
//      // System.out.print(sfAcc + " ");
//      clsForRemoval.put(sfId, sfId);
//    }
//
//    // System.out.println();
//    // Do a sanity check.  Make sure that the subfamily associated sequences being added and removed is the same
//    keys = finalAddTbl.keys();
//    Vector  addSequences = new Vector();
//
//    while (keys.hasMoreElements()){
//      String  acc = (String) keys.nextElement();
//
//      // acc = acc.substring(acc.indexOf(":") + 1, acc.length());
//      Vector  sequenceList = (Vector) sfToSeq.get(acc);
//
//      addSequences.addAll(sequenceList);
//    }
//    keys = finalRemoveTbl.keys();
//    Vector  removeSequences = new Vector();
//
//    while (keys.hasMoreElements()){
//      String  acc = (String) keys.nextElement();
//
//      // acc = acc.substring(acc.indexOf(":") + 1, acc.length());
//      Vector  sequenceList = (Vector) sfAccToProtAcc.get(acc);
//
//      removeSequences.addAll(sequenceList);
//    }
//    if (false == compareStringVectors(addSequences, removeSequences)){
//      return "Save Operation failed - Logic error, cannot save book";
//    }
//
//    // Modify the subfamily name to contain the book name in the sequence add table
//    // keys = finalAddTbl.keys();
//    // Vector seqAddEntries = new Vector();
//    // while (keys.hasMoreElements()) {
//    // String sfName = (String)keys.nextElement();
//    // seqAddEntries.addElement(sfName);
//    // }
//    // for (int i = 0; i < seqAddEntries.size(); i++) {
//    // String sfName = (String)seqAddEntries.elementAt(i);
//    // String modSfName = book + ":" + sfName;
//    // Object sequences = finalAddTbl.remove(sfName);
//    // finalAddTbl.put(modSfName, sequences);
//    // }
//    // Generate subfamily ids for new subfamilies
//    int sfUids[] = getUids(finalAddTbl.size());
//
//    if (null == sfUids){
//      return "Unable to save book, cannot generate classification ids";
//    }
//    int crUids[] = getUids(finalAddTbl.size());
//
//    if (null == crUids){
//      return "Unable to save book, cannot generate classification relatioship ids";
//    }
//
//    // Generate records in the classification table for the subfamlies
//    Hashtable newSfIdToSfAcc = new Hashtable();
//    Hashtable sfAccToNewSfId = new Hashtable();
//
//    // Generate records in the classification relationship table to link families to the subfamilies
//    Hashtable sfToCR = new Hashtable();     // Subfamily to classification relationship id
//    Hashtable CRToFam = new Hashtable();    // Classification relationship id to family id
//    int       counter = 0;
//
//    keys = finalAddTbl.keys();
//    while (keys.hasMoreElements()){
//      String  sfAcc = (String) keys.nextElement();
//      Integer sfId = new Integer(sfUids[counter]);
//
//      newSfIdToSfAcc.put(sfId, sfAcc);
//      sfAccToNewSfId.put(sfAcc, sfId);
//
//      // Deal with the classificaion relationships
//      int[] crId = new int[1];
//
//      crId[0] = crUids[counter];
//      sfToCR.put(sfId, crId);
//      CRToFam.put(new Integer(crUids[counter]), famId);
//      counter++;
//    }
//
//    // If the subfamily name (annotation) changes, the old comments and evidence have to be carried over
//    // with the new subfamily name
//    if (null != sfToNameList){
//      Hashtable sfAddTable = (Hashtable) sfToNameList.elementAt(0);
//
//      // Hashtable tmpSeqAddTable;
//      // if (null != seqAddTable) {
//      // tmpSeqAddTable = (Hashtable)sfToSeqChangeList.elementAt(0);
//      // }
//      // else {
//      // tmpSeqAddTable = new Hashtable();
//      // }
//      Hashtable cmntAddTable;
//
//      if (null != sfCmntChangeList){
//        cmntAddTable = (Hashtable) sfCmntChangeList.elementAt(0);
//      }
//      else{
//        cmntAddTable = new Hashtable();
//      }
//      Enumeration sfNameList = sfAddTable.keys();
//
//      while (sfNameList.hasMoreElements()){
//
//        // Make sure this is only a subfamily name change.  Check by ensuring
//        // subfamily does not appear in the list of subfamilies with sequence changes.
//        // Do not have to handle singleton subfamilies, since the information will be stored
//        // with the sequence
//        String  sfName = (String) sfNameList.nextElement();
//        String  sequenceChange = (String) origAddSeq.get(sfName);
//
//        if (null != sequenceChange){
//          continue;
//        }
//
//        // No singletons
//        if (null != singletonSfNameTbl.get(sfName)){
//          continue;
//        }
//
//        // If there are no comment changes, then old comments have to be transferred
//        if (null == cmntAddTable.get(sfName)){
//          commentCarryOver.put(sfAccToSfId.get(sfName), sfAccToNewSfId.get(sfName));
//        }
//        evidenceCarryOver.put(sfAccToSfId.get(sfName), sfAccToNewSfId.get(sfName));
//        featureCarryOver.put(sfAccToSfId.get(sfName), sfAccToNewSfId.get(sfName));
//      }
//    }
//
//    // Now that a complete list of newly added subfamilies is available.  Handle the classification changes
//    // Handle classifications associated with subfamilies
//    // System.out.println("Start of Cls query Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
//    Hashtable origSfToCls = getClsForSubfam(uplVersion, book, ConfigFile.getProperty(uplVersion + "_cls_types"));
//
//    // System.out.println("End of Cls query Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
//    // Want to convert the string of associated labels to associated classifications.  Perhaps a different
//    // method should be used instead of the above, so that this handling will be unnecessary
//    if (null != origSfToCls){
//      Hashtable   modOrigSfToCls = new Hashtable();
//      Enumeration clsTypes = origSfToCls.keys();
//
//      while (clsTypes.hasMoreElements()){
//        String    clsType = (String) clsTypes.nextElement();
//        Hashtable current = (Hashtable) origSfToCls.get(clsType);
//
//        sfs = current.keys();
//        while (sfs.hasMoreElements()){
//          Integer sfId = (Integer) sfs.nextElement();
//          String  currentClsInfo = (String) current.get(sfId);
//          String  allClsInfoStr = (String) modOrigSfToCls.get(sfId);
//
//          if (null == allClsInfoStr){
//            allClsInfoStr = "";
//          }
//          allClsInfoStr += currentClsInfo;
//          modOrigSfToCls.put(sfId, allClsInfoStr);
//        }
//      }
//
//      // Now have sfId to clsInfoStr.  Change sfId to subfamily accession
//      sfs = modOrigSfToCls.keys();
//      Vector  sfIdList = new Vector();
//
//      while (sfs.hasMoreElements()){
//        Integer sfId = (Integer) sfs.nextElement();
//
//        sfIdList.addElement(sfId);
//      }
//      for (int i = 0; i < sfIdList.size(); i++){
//        Integer sfId = (Integer) sfIdList.elementAt(i);
//        Object  o = modOrigSfToCls.remove(sfId);
//        String  sfAccName[] = (String[]) sfIdToSfAccName.get(sfId);
//
//        modOrigSfToCls.put(sfAccName[0], o);
//      }
//      origSfToCls = modOrigSfToCls;
//
//      // Handle new classification data
//      Hashtable sfToClsTable = new Hashtable();
//
//      // Subfamily classification was defined for this UPL
//      String    clsTypeStr = ConfigFile.getProperty(uplVersion + "_cls_types");
//      String[]  clsType = Utils.tokenize(clsTypeStr, COMMA_DELIM);
//
//      for (int i = 0; i < clsType.length; i++){
//        String  currentClsColumn = clsType[i];
//        int     clsIndex = -1;
//
//        for (int j = 0; j < colHeadersTblNames.length; j++){
//          if (0 == currentClsColumn.compareTo(colHeadersTblNames[j])){
//            clsIndex = j;
//            break;
//          }
//        }
//        if (-1 == clsIndex){
//          continue;
//        }
//        for (int j = 1; j < attrTable.length; j++){
//          String  rowData = attrTable[j];
//          String  cellData[] = Utils.tokenize(rowData, TAB_DELIM);
//          String  acc = cellData[accIndex];
//
//          if (0 == acc.length()){
//            continue;
//          }
//          acc = Utils.replace(acc, QUOTE, "");
//          acc = book + ":" + acc;
//          String  sfAcc = null;
//
//          if (null != sfToSeq.get(acc)){
//            sfAcc = acc;
//          }
//          else{
//            String  singletonSf = (String) singletonSfTbl.get(acc.substring(acc.indexOf(":") + 1, acc.length()));
//
//            if (null != singletonSf){
//              sfAcc = singletonSf;
//            }
//          }
//          if (null != sfAcc){
//            String  clsStr = (String) sfToClsTable.get(sfAcc);
//
//            if (null == clsStr){
//              clsStr = "";
//            }
//            String  cellContents = cellData[clsIndex];
//
//            cellContents = Utils.replace(cellContents, QUOTE, "");
//            clsStr += cellContents;
//            if (0 < clsStr.length()){
//              sfToClsTable.put(sfAcc, clsStr);
//            }
//          }
//        }
//      }
//
//      // First convert sf to string table to sf to vector of string table
//      convertStrToVectorForCls(sfToClsTable);
//      convertStrToVectorForCls(origSfToCls);
//      clsChangeList = compareStringToVectorHash(sfToClsTable, origSfToCls);
//      if (null == clsChangeList){
//        clsChangeList = new Vector();
//        clsChangeList.addElement(new Hashtable());
//        clsChangeList.addElement(new Hashtable());
//      }
//      Hashtable clsAddTable = (Hashtable) clsChangeList.elementAt(0);
//      Hashtable clsRemoveTable = (Hashtable) clsChangeList.elementAt(1);
//
//      // Go through list of added subfamilies, if it does not appear in the list of added subfamilies for
//      // categrories.  Add it in.  This is to the case where subfamilies are being added but, the subfamily accession
//      // has not changed.
//      keys = finalAddTbl.keys();
//      while (keys.hasMoreElements()){
//        String  sfAcc = (String) keys.nextElement();
//        Vector  catList = (Vector) clsAddTable.get(sfAcc);
//
//        if (null == catList){
//          Vector  sfClsList = (Vector) sfToClsTable.get(sfAcc);
//
//          if (null != sfClsList){
//            clsAddTable.put(sfAcc, sfClsList);
//          }
//        }
//      }
//      if ((true == clsAddTable.isEmpty()) && (true == clsRemoveTable.isEmpty())){
//        clsChangeList = null;
//      }
//    }
//    Hashtable commentIdToClsId = new Hashtable();
//    Hashtable clsIdToComment = new Hashtable();
//
//    // Deal with subfamily comments
//    if (null != sfCmntChangeList){
//      Hashtable addTable = (Hashtable) sfCmntChangeList.elementAt(0);
//      int       sfCommentIds[] = getUids(addTable.size());
//
//      if (null == sfCommentIds){
//        return "Save Operation failed - unable to save book, cannot generate ids";
//      }
//
//      // Hashtable removeTable = (Hashtable)sfCmntChangeList.elementAt(1);
//      Enumeration addKeys = addTable.keys();
//
//      counter = 0;
//      while (addKeys.hasMoreElements()){
//        String  key = (String) addKeys.nextElement();
//
//        // First check the newly added subfamilies.  If it is not there then use the original
//        // System.out.println("Adding " + key);
//        Vector  results = (Vector) addTable.get(key);
//
//        // System.out.print(results.elementAt(0) + " ");
//        Integer sfId = (Integer) sfAccToNewSfId.get(key);
//
//        if (null == sfId){
//          sfId = (Integer) sfAccToSfId.get(key);
//        }
//        commentIdToClsId.put(new Integer(sfCommentIds[counter]), sfId);
//        clsIdToComment.put(sfId, results.elementAt(0));
//
//        // System.out.println();
//        counter++;
//      }
//    }
//    Hashtable commentIdToProId = new Hashtable();
//    Hashtable proIdToComment = new Hashtable();
//
//    if (null != seqCmntChangeList){
//      Hashtable   addTable = (Hashtable) seqCmntChangeList.elementAt(0);
//
//      // Hashtable removeTable = (Hashtable)seqCmntChangeList.elementAt(1);
//      int         seqCommentIds[] = getUids(addTable.size());
//      Enumeration addKeys = addTable.keys();
//
//      counter = 0;
//      while (addKeys.hasMoreElements()){
//        String  key = (String) addKeys.nextElement();
//
//        // System.out.println("Adding " + key);
//        Vector  results = (Vector) addTable.get(key);
//
//        // for (int i = 0; i < results.size(); i++) {
//        // System.out.print(results.elementAt(0));
//        // }
//        Integer protId = (Integer) protAccToProt.get(key);
//
//        commentIdToProId.put(new Integer(seqCommentIds[counter]), protId);
//        proIdToComment.put(protId, results.elementAt(0));
//
//        // System.out.println();
//        counter++;
//      }
//    }
//    Hashtable ClsToCRIds = new Hashtable();
//    Hashtable CRIdsToCatIds = new Hashtable();
//    Hashtable obsoleteCatTbl = new Hashtable();
//
//    if (null != clsChangeList){
//
//      // For categories, remove matches if only the associated categories has changed but the subfamily
//      // record has not changed.  For example, if a subfamily was associated to a set of classifications
//      // originally and now the subfamily is associated to another set of classifications, remove the intersection
//      // of classifications.
//      Hashtable   addCatTable = (Hashtable) clsChangeList.elementAt(0);
//      Hashtable   removedCatTable = (Hashtable) clsChangeList.elementAt(1);
//      Enumeration addedSfs = addCatTable.keys();
//
//      while (addedSfs.hasMoreElements()){
//        String  sf = (String) addedSfs.nextElement();
//
//        // If it is a newly added subfamily.  Do not update
//        if (null != sfAccToNewSfId.get(sf)){
//          continue;
//        }
//        Vector  removedList = (Vector) removedCatTable.get(sf);
//
//        if (null != removedList){
//          Vector  addedList = (Vector) addCatTable.get(sf);
//
//          removeStrIntersect(addedList, removedList);
//        }
//      }
//      if (null == clsTypeIdToCatId){
//        initCatLookup();
//      }
//      if (null == clsTypeIdToCatId){
//        return "Save Operation failed - unable to retrieve category information";
//      }
//      Hashtable catAccToCatIdTable = (Hashtable) clsTypeIdToCatId.get(uplVersion);
//
//      if (null == catAccToCatIdTable){
//        return "Save Operation failed - unable to retrieve category information for given UPL";
//      }
//      Hashtable   addTable = (Hashtable) clsChangeList.elementAt(0);
//      Hashtable   removeTable = (Hashtable) clsChangeList.elementAt(1);
//      Enumeration addKeys = addTable.keys();
//
//      while (addKeys.hasMoreElements()){
//        String  key = (String) addKeys.nextElement();
//        Integer sfId = (Integer) sfAccToNewSfId.get(key);
//
//        if (null == sfId){
//          sfId = (Integer) sfAccToSfId.get(key);
//        }
//        Vector  results = (Vector) addTable.get(key);
//
//        if (0 == results.size()){
//          continue;
//        }
//
//        // System.out.println("Adding " + key);
//        int uids[] = getUids(results.size());
//
//        if (null == uids){
//          return "Save Operation failed - unable to generate ids for classification relationship table";
//        }
//
//        // Vector uidList = new Vector(uids.length);
//        for (int i = 0; i < results.size(); i++){
//
//          // System.out.print(results.elementAt(i) + " ");
//          Integer newCRId = new Integer(uids[i]);
//
//          // uidList.addElement(newCRId);
//          Integer catId = new Integer((String) catAccToCatIdTable.get(results.elementAt(i)));
//
//          if (null == catId){
//            return "Save Operation failed - unable to retrieve category ids";
//          }
//          CRIdsToCatIds.put(newCRId, catId);
//        }
//        ClsToCRIds.put(sfId, uids);
//
//        // System.out.println();
//      }
//      Enumeration removeKeys = removeTable.keys();
//
//      while (removeKeys.hasMoreElements()){
//        String  key = (String) removeKeys.nextElement();
//        Integer sfId = (Integer) sfAccToSfId.get(key);
//
//        // System.out.println("Removing " + key);
//        Vector  results = (Vector) removeTable.get(key);
//        Vector  catList = (Vector) obsoleteCatTbl.get(sfId);
//
//        if (null == catList){
//          catList = new Vector();
//          obsoleteCatTbl.put(sfId, catList);
//        }
//        for (int i = 0; i < results.size(); i++){
//          Integer catId = new Integer((String) catAccToCatIdTable.get(results.elementAt(i)));
//
//          if (null == catId){
//            return "Save Operation failed - unable to retrieve category ids";
//          }
//          catList.addElement(catId);
//
//          // System.out.print(results.elementAt(i) + " ");
//        }
//
//        // System.out.println();
//      }
//    }
//    
//    // Handle classification evidence with confidence changes
//    Vector confDataDiff = getConfDataDiff(book, uplVersion, accIndex,colHeadersTblNames, attrTable, protAccToProt, protToProtAcc); 
//    Hashtable addInfoTbl = (Hashtable)confDataDiff.elementAt(0);
//    Hashtable removeInfoTbl = (Hashtable)confDataDiff.elementAt(1);   
//    
//    Connection  updateCon = null;
//    boolean     recordsUpdated = false;
//
//    try{
//      System.out.println("Start of update Operation for book " + book + " "
//                         + df.format(new java.util.Date(System.currentTimeMillis())));
//
//      // System.out.println("Got a connection object for writing into dababase");
//      updateCon = getConnection();
//      updateCon.setAutoCommit(false);
//      updateCon.rollback();
//
//      // Insert family
//      if (famId != oldFamId){
//        String  errMsg = insertFamily(updateCon, uplVersion, userIdStr, famId, book, familyName, "", famInfo.eValue);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to save family record";
//        }
//        recordsUpdated = true;
//      }
//
//      // Update tree text
//      if (null != treeIdToTreeStr){
//        Enumeration treeIds = treeIdToTreeStr.keys();
//        Integer     treeId = (Integer) treeIds.nextElement();
//        String      treeStr = (String) treeIdToTreeStr.get(treeId);
//        String      errMsg = obsoleteOldAndAddNewTree(updateCon, treeId, treeStr, famId, oldFamId, userIdStr);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to save tree";
//        }
//        recordsUpdated = true;
//      }
//
//      // Add new subfamilies (classification table and protein classification table will be updated)
//      if (false == newSfIdToSfAcc.isEmpty()){
//        String  errMsg = insertSubfamilies(updateCon, uplVersion, userIdStr, newSfIdToSfAcc, finalAddTbl, seqAddTable,
//                                           protAccToProt);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to insert subfamilies";
//        }
//        recordsUpdated = true;
//
//        // Since subfamily records were inserted, make sure classification relationship table is also updated
//        if (false == sfToCR.isEmpty()){
//          errMsg = insertClsRltn(updateCon, sfToCR, CRToFam, userIdStr);
//          if (0 != errMsg.length()){
//            updateCon.rollback();
//            return "Save Operation failed - unable to insert family to subfamily relationships";
//          }
//          recordsUpdated = true;
//        }
//      }
//
//      // Add comments for subfamilies
//      if (false == commentIdToClsId.isEmpty()){
//        String  errMsg = insertComments(updateCon, false, userIdStr, commentIdToClsId, clsIdToComment);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to insert comments for subfamilies";
//        }
//        recordsUpdated = true;
//      }
//
//      // Handle comments for sequences
//      if (false == commentIdToProId.isEmpty()){
//        String  errMsg = insertComments(updateCon, true, userIdStr, commentIdToProId, proIdToComment);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to insert comments for sequences";
//        }
//        recordsUpdated = true;
//      }
//
//      // Associate categories with subfamilies
//      if (false == ClsToCRIds.isEmpty()){
//        String  errMsg = insertClsRltn(updateCon, ClsToCRIds, CRIdsToCatIds, userIdStr);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to insert categories to subfamily relationships";
//        }
//        recordsUpdated = true;
//      }
//
//      // Handle carryovers. i.e. comment and evidence records with old subfamily id have to be
//      // copied over with new subfamily ids.  Do this before obsoleting the subfamilies
//      if (false == commentCarryOver.isEmpty()){
//        String  errMsg = carryOverCommentEvidence(updateCon, CARRYOVER_COMMENT, commentCarryOver);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to carryover comments";
//        }
//        recordsUpdated = true;
//      }
//      if (false == evidenceCarryOver.isEmpty()){
//        String  errMsg = carryOverCommentEvidence(updateCon, CARRYOVER_EVIDENCE, evidenceCarryOver);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to carryover evidence";
//        }
//        recordsUpdated = true;
//      }
//      if (false == featureCarryOver.isEmpty()){
//        String  errMsg = carryOverCommentEvidence(updateCon, CARRYOVER_FEATURE, featureCarryOver);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to carryover features";
//        }
//        recordsUpdated = true;
//      }
//      
//      if (false == addInfoTbl.isEmpty()) {
//        String errMsg = insertEvidenceWithConf(updateCon, userIdStr, addInfoTbl, uplVersion, protToProtAcc);
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to insert evidence with confidence codes";
//        }
//        recordsUpdated = true;              
//      }
//      
//      
//
//      // Now handle removals
//      // Deal with obsoleted subfamily records
//      if (false == clsForRemoval.isEmpty()){
//        String  errMsg = obsoleteSubfamilies(updateCon, clsForRemoval, userIdStr);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to obsolete subfamily records";
//        }
//        recordsUpdated = true;
//      }
//
//      // Deal with categories that are no longer associated with subfamilies
//      if (false == obsoleteCatTbl.isEmpty()){
//        String  errMsg = obsoleteClsRltn(updateCon, obsoleteCatTbl, userIdStr);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to obsolete category to subfamily relationship records";
//        }
//        recordsUpdated = true;
//      }
//
//      // Now determine if the family classification record has changed, if yes all parents (categories)
//      // and all children (subfamilies) with links to this record have to be obsoleted and new
//      // links should be added with the new family record.
//      if (famId != oldFamId){
//
//        // Get list of categories associated with the family
//        Vector  catList = getCurrentParentChild(oldFamId, true);
//
//        if (0 != catList.size()){
//          Hashtable obslteCatTbl = new Hashtable();
//
//          obslteCatTbl.put(oldFamId, catList);
//
//          // Obsolete the records
//          String  errMsg = obsoleteClsRltn(updateCon, obslteCatTbl, userIdStr);
//
//          if (0 != errMsg.length()){
//            updateCon.rollback();
//            return "Save Operation failed - unable to obsolete categories associated with the family";
//          }
//          recordsUpdated = true;
//
//          // Add new records
//          int famUids[] = getUids(catList.size());
//
//          if (null == famUids){
//            updateCon.rollback();
//            return "Save Operation failed - unable to generate uids for associating families to categories";
//          }
//          recordsUpdated = true;
//          Hashtable famToCRTbl = new Hashtable();
//
//          famToCRTbl.put(famId, famUids);
//          Hashtable cRToPrnt = new Hashtable();
//
//          for (int i = 0; i < famUids.length; i++){
//            cRToPrnt.put(new Integer(famUids[i]), catList.elementAt(i));
//          }
//          errMsg = insertClsRltn(updateCon, famToCRTbl, cRToPrnt, userIdStr);
//          if (0 != errMsg.length()){
//            updateCon.rollback();
//            return "Save Operation failed - unable to insert categories associated with the family";
//          }
//          recordsUpdated = true;
//        }
//
//        // Now repeat for subfamiles associated with the family.  Remove subfamilies that are being obsoleted
//        Vector  subFamList = getCurrentParentChild(oldFamId, false);
//
//        for (int i = 0; i < subFamList.size(); i++){
//          Integer subFamId = (Integer) subFamList.elementAt(i);
//
//          if (null != clsForRemoval.get(subFamId)){
//            subFamList.remove(i);
//            i--;
//          }
//        }
//        Hashtable obslteSubfamTbl = new Hashtable();
//        Vector    famElem = new Vector();
//
//        famElem.addElement(oldFamId);
//        for (int i = 0; i < subFamList.size(); i++){
//          Vector  v = new Vector();
//
//          obslteSubfamTbl.put((Integer) subFamList.elementAt(i), famElem);
//        }
//
//        // Obsolete the records
//        String  errMsg = obsoleteClsRltn(updateCon, obslteSubfamTbl, userIdStr);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to obsolete subfamilies associated with the family";
//        }
//        recordsUpdated = true;
//
//        // Add new records
//        int subfamUids[] = getUids(subFamList.size());
//
//        if (null == subfamUids){
//          updateCon.rollback();
//          return "Save Operation failed - unable to generate uids for associating subfamilies to family";
//        }
//        recordsUpdated = true;
//        Hashtable subfamToCRTbl = new Hashtable();
//        Hashtable CRTblToFam = new Hashtable();
//
//        for (int i = 0; i < subfamUids.length; i++){
//          int crId[] = new int[1];
//
//          crId[0] = subfamUids[i];
//          subfamToCRTbl.put(subFamList.elementAt(i), crId);
//          CRTblToFam.put(new Integer(crId[0]), famId);
//        }
//        errMsg = insertClsRltn(updateCon, subfamToCRTbl, CRTblToFam, userIdStr);
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to insert family associated with the subfamily record";
//        }
//        recordsUpdated = true;
//
//        // Obsolete the old family record
//        errMsg = obsoleteCls(updateCon, userIdStr, oldFamId);
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to obsolete old family record";
//        }
//        recordsUpdated = true;
//      }
//      
//      if (false == removeInfoTbl.isEmpty()) {
//        String errMsg = obsoleteEvidenceWithConf(updateCon, userIdStr, removeInfoTbl, uplVersion, protToProtAcc);
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to obsolete evidence with confidence codes";
//        }
//        recordsUpdated = true;              
//      }      
//
//      // Add entry to curation status table
//      // Temporarily hard code the save options.  These should be specified in a file common to both
//      // the server and the client
//      // 0 - Mark curated and unlock
//      // 1 - Save and keep locked
//      // 2 - Save and unlock
//      if ((0 == saveSts) || (2 == saveSts)){
//        if (0 == saveSts){
//          int     statusId = Integer.parseInt(ConfigFile.getProperty("panther_manually_curated"));
//          String  errMsg = UpdateCurtionStatus(updateCon, famId, statusId, userIdStr);
//
//          if (0 != errMsg.length()){
//            updateCon.rollback();
//            return "Save Operation failed - unable to insert curation status record";
//          }
//
//          // recordsUpdated = true;
//        }
//
//        // Unlock book
//        int     checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));
//        String  errMsg = deleteCurationStatus(updateCon, oldFamId, checkOut, userIdStr);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to unlock book";
//        }
//
//        // recordsUpdated = true;
//      }
//      else if (1 == saveSts){
//
//        // If the family id has changed, then unlock the old family id and lock the new id
//        if (famId != oldFamId){
//          int     checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));
//          String  errMsg = deleteCurationStatus(updateCon, oldFamId, checkOut, userIdStr);
//
//          if (0 != errMsg.length()){
//            updateCon.rollback();
//            return "Save Operation failed - unable to unlock old book id";
//          }
//          recordsUpdated = true;
//
//          // lock with new family id
//          errMsg = lockBook(famId.toString(), userIdStr, updateCon);
//          if (0 != errMsg.length()){
//            updateCon.rollback();
//            return "Save Operation failed - unable to unlock book with previous family id";
//          }
//          recordsUpdated = true;
//        }
//      }
//
//      // Add record to curation status table indicating, book has been updated
//      if ((true == recordsUpdated) && (0 != saveSts)){
//        int     statusId = Integer.parseInt(ConfigFile.getProperty("panther_partially_curated"));
//        String  errMsg = UpdateCurtionStatus(updateCon, famId, statusId, userIdStr);
//
//        if (0 != errMsg.length()){
//          updateCon.rollback();
//          return "Save Operation failed - unable to insert family partially curated status record";
//        }
//      }
//
////       System.out.println("Rollback during testing");
////       updateCon.rollback();
////       System.out.println("Rollback during testing is complete");
//      updateCon.commit();
//    }
//    catch (Exception e){
//      System.out.println("Unable to save information from database, exception " + e.getMessage()
//                         + " has been returned.");
//      try{
//        if (null != updateCon){
//          updateCon.rollback();
//        }
//        return "Logical exception, unable to save book";
//      }
//      catch (SQLException se){
//        se.printStackTrace();
//        System.out.println("Exception while rollback");
//        return "Could not roll back the update after failed save attempt";
//      }
//    }
//    finally{
//      if (null != updateCon){
//        try{
//          updateCon.close();
//        }
//        catch (SQLException se){
//          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
//          return "Error saving changes into database";
//        }
//      }
//    }
//    System.out.println("End of update Operation for book " + book + " "
//                       + df.format(new java.util.Date(System.currentTimeMillis())));
//
//    // Reset book information
//    resetUplToBookInfo(uplVersion, book);
//    System.out.println("End of Save Operation for book " + book + " "
//                       + df.format(new java.util.Date(System.currentTimeMillis())));
//    return "";
//  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param treeId
   * @param treeStr
   * @param famId
   * @param oldFamId
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
//  protected String obsoleteOldAndAddNewTree(Connection con, Integer treeId, String treeStr, Integer famId,
//          Integer oldFamId, String userIdStr){
//    PreparedStatement stmt = null;
//
//    try{
//
//      // Obsolete the old tree
//      stmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_TREE);
//      stmt.setInt(1, Integer.parseInt(userIdStr));
//      stmt.setInt(2, oldFamId.intValue());
//      stmt.executeUpdate();
//      stmt.close();
//
//      // Insert new tree
//      stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_EMPTY_TREE);
//      stmt.setInt(1, treeId.intValue());
//      stmt.setInt(2, famId.intValue());
//      stmt.setInt(3, Integer.parseInt(userIdStr));
//      int count = stmt.executeUpdate();
//
//      stmt.close();
//      if (1 != count){
//        return "Unable to insert tree string";
//      }
//
//      // Get the clob just inserted
//      stmt = con.prepareStatement(QueryString.PREPARED_TREE_CLOB);
//      stmt.setInt(1, treeId.intValue());
//      ResultSet rstClob = stmt.executeQuery();
//
//      rstClob.next();
//      CLOB  clob = ((OracleResultSet) rstClob).getCLOB(1);
//
//      rstClob.close();
//      stmt.close();
//      clob.putString(1, treeStr);
//
//      // Set the clob
//      stmt = con.prepareStatement(UpdateString.PREPARED_UPDATE_TREE_CLOB);
//      stmt.setClob(1, clob);
//      stmt.setInt(2, treeId.intValue());
//      stmt.executeUpdate();
//      stmt.close();
//    }
//    catch (SQLException se){
//      System.out.println("Exception while trying to save tree information " + se.getMessage() + " has been returned");
//      return "Exception while trying to save tree information " + se.getMessage() + " has been returned";
//    }
//    return Constant.STR_EMPTY;
//  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param uplVersion
   * @param userIdStr
   * @param famId
   * @param book
   * @param name
   * @param definition
   * @param eValue
   *
   * @return
   *
   * @see
   */
  protected String insertFamily(Connection con, String uplVersion, String userIdStr, Integer famId, String book,
                                       String name, String definition, String eValue){
    PreparedStatement stmt = null;
    int               familyLevel = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_famLevel"));

    try{

      // Insert record into the classification table
      stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_CLASSIFICATION);
      stmt.setInt(1, famId.intValue());
      stmt.setInt(2, Integer.parseInt(uplVersion));
      stmt.setString(3, name);
      stmt.setString(4, book);
      stmt.setInt(5, familyLevel);
      stmt.setInt(6, Integer.parseInt(userIdStr));
      stmt.setString(7, eValue);
      int numUpdated = stmt.executeUpdate();

      stmt.close();
      if (1 != numUpdated){
        return "Cannot inset family record";
      }
    }
    catch (SQLException se){
      System.out.println("Exception while trying to save family information " + se.getMessage() + " has been returned");
      return "Exception while trying to save family information " + se.getMessage() + " has been returned";
    }
    return Constant.STR_EMPTY;
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param userIdStr
   * @param clsId
   *
   * @return
   *
   * @see
   */
  protected String obsoleteCls(Connection con, String userIdStr, Integer clsId){
    PreparedStatement stmt = null;

    try{

      // Obsolete record in the classification table
      stmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_CLS);
      stmt.setInt(1, Integer.parseInt(userIdStr));
      stmt.setInt(2, clsId.intValue());
      stmt.executeUpdate();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Exception while trying to obsolete family information " + se.getMessage()
                         + " has been returned");
      return "Exception while trying to obsolete family information " + se.getMessage() + " has been returned";
    }
    return Constant.STR_EMPTY;
  }
  
    protected String obsoleteAnnot(Connection con, String userIdStr, Hashtable<Integer, Integer> annotTbl) {
        if (null == annotTbl || 0 == annotTbl.size()) {
            return Constant.STR_EMPTY;
        }
        try {
            PreparedStatement stmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_ANNOTATION);

            int userId = Integer.parseInt(userIdStr);
            Enumeration<Integer> annotIds = annotTbl.keys();
            while (annotIds.hasMoreElements()) {


                Integer annotId = annotIds.nextElement();
                int annotInt = annotId.intValue();
                
                // Obsolete records in the classification table
                stmt.setInt(1, userId);
                stmt.setInt(2, annotId);
                stmt.addBatch();
                
              
                
                
            }
            stmt.executeBatch();

            
            stmt.close();

        }
        catch (SQLException se) {
            String errorMsg = MSG_ERROR_DURING_CLS_OBSOLETE + se.getMessage();
            se.printStackTrace();

            return errorMsg;
        }
        return Constant.STR_EMPTY;
    }

    protected String obsoleteCls(Connection con, String userIdStr, Hashtable<Integer, Integer> clsIdTbl) {
        if (null == clsIdTbl || 0 == clsIdTbl.size()) {
            return Constant.STR_EMPTY;
        }
        try {
            PreparedStatement clsStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_CLS);
            PreparedStatement annotStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_ANNOTATION_WITH_CLS_ID);
            int userId = Integer.parseInt(userIdStr);
            Enumeration<Integer> clsIds = clsIdTbl.keys();
            while (clsIds.hasMoreElements()) {


                Integer clsId = clsIds.nextElement();
                int clsInt = clsId.intValue();
                
                // Obsolete records in the classification table
                clsStmt.setInt(1, userId);
                clsStmt.setInt(2, clsInt);
                clsStmt.addBatch();
                
                // Obsolete records in the annotation table
                 annotStmt.setInt(1, userId);
                 annotStmt.setInt(2, clsInt);
                 annotStmt.addBatch();                
                
                
            }
            clsStmt.executeBatch();
            annotStmt.executeBatch();
            
            clsStmt.close();
            annotStmt.executeBatch();
        }
        catch (SQLException se) {
            String errorMsg = MSG_ERROR_DURING_CLS_OBSOLETE + se.getMessage();
            se.printStackTrace();
            System.out.println(errorMsg);
            return errorMsg;
        }
        return Constant.STR_EMPTY;
    }

    /**
     * Method declaration
     *
     *
     * @param con
     * @param uplVersion
     * @param userIdStr
     * @param sfIdToSfAcc
     * @param sfAccToSfAnnot
     * @param sfToSeq
     * @param protAccToProtId
     *
     * @return
     *
     * @see
     */
  protected String insertSubfamilies(Connection con, String uplVersion, String userIdStr, Hashtable sfIdToSfAcc,
                                            Hashtable sfAccToSfAnnot, Hashtable sfToSeq, Hashtable protAccToProtId){
    PreparedStatement stmt = null;
    PreparedStatement pstmt = null;
    int               subfamilyLevel = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_subfamLevel"));

    try{
      stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_CLASSIFICATION);
      pstmt = con.prepareStatement(UpdateString.PREPARED_INSERT_PROTEIN_CLS);

      // Insert records into the classification table
      Enumeration sfIds = sfIdToSfAcc.keys();

      while (sfIds.hasMoreElements()){
        Integer sfId = (Integer) sfIds.nextElement();
        String  acc = (String) sfIdToSfAcc.get(sfId);
        String  sfName = (String) sfAccToSfAnnot.get(acc);

        stmt.setInt(1, sfId.intValue());
        stmt.setInt(2, Integer.parseInt(uplVersion));
        stmt.setString(3, sfName);
        stmt.setString(4, acc);
        stmt.setInt(5, subfamilyLevel);
        stmt.setInt(6, Integer.parseInt(userIdStr));
        stmt.setString(7, null);
        stmt.addBatch();

        // Insert records into the protein classification table
        Vector  seqList = (Vector) sfToSeq.get(acc);

        for (int i = 0; i < seqList.size(); i++){
          String  protAcc = (String) seqList.elementAt(i);
          Integer protId = (Integer) protAccToProtId.get(protAcc);

          pstmt.setInt(1, protId.intValue());
          pstmt.setInt(2, sfId.intValue());
          pstmt.setInt(3, Integer.parseInt(userIdStr));
          pstmt.addBatch();
        }
      }

      // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      // System.out.println("Start of insert subfamililes " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.executeBatch();

      // System.out.println("End of insert cls subfamililes " + df.format(new java.util.Date(System.currentTimeMillis())));
      pstmt.executeBatch();

      // System.out.println("End of isert pro cls subfamililes " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.close();
      pstmt.close();
    }
    catch (SQLException se){
      System.out.println("Exception while trying to save subfamily information " + se.getMessage()
                         + " has been returned");
      return "Exception while trying to save subfamily information " + se.getMessage() + " has been returned";
    }
    return "";
  }
  
    protected String insertAnnotations(Connection con, String userIdStr, Vector <Annotation> annotList) {
        if (null == annotList || 0 == annotList.size()) {
            return Constant.STR_EMPTY;
        }
        int userId = Integer.parseInt(userIdStr);
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_ANNOTATION);
            for (int i = 0; i < annotList.size(); i++) {
                Annotation a = annotList.get(i);
                stmt.setInt(1, a.getAnnotId());
                stmt.setInt(2, a.getNodeId());
                stmt.setInt(3, a.getClsId());
                stmt.setInt(4, a.getAnnotTypeId());
                stmt.setInt(5, userId);
                stmt.addBatch();
            }
            
        

            // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            // System.out.println("Start of insert subfamililes " + df.format(new java.util.Date(System.currentTimeMillis())));
            stmt.executeBatch();


            // System.out.println("End of isert pro cls subfamililes " + df.format(new java.util.Date(System.currentTimeMillis())));
            stmt.close();

        }
        catch (SQLException se){
            se.printStackTrace();
            return MSG_INSERTION_FAILED_EXCEPTION_RETURNED + se.getMessage();

        }
        return Constant.STR_EMPTY;
    }
    
    protected String insertAnnotationQualifiers(Connection con, Vector <int[]> annotQualifierList) {
        if (null == annotQualifierList || 0 == annotQualifierList.size()) {
            return Constant.STR_EMPTY;
        }

        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_ANNOTATION_QUALIFIER);
            for (int i = 0; i < annotQualifierList.size(); i++) {
                int annotQualifierInfo[]  = annotQualifierList.get(i);
                stmt.setInt(1, annotQualifierInfo[0]);
                stmt.setInt(2, annotQualifierInfo[1]);
                stmt.setInt(3, annotQualifierInfo[2]);

                stmt.addBatch();
            }

            stmt.executeBatch();

            stmt.close();

        }
        catch (SQLException se){
            se.printStackTrace();
            return MSG_INSERTION_FAILED_EXCEPTION_RETURNED + se.getMessage();

        }
        return Constant.STR_EMPTY;
    }

  
  
  
    protected String insertSubfamilies(Connection con, String uplVersion, String userIdStr, Hashtable sfIdToSfAcc,
                                              Hashtable sfAccToSfAnnot){
      PreparedStatement stmt = null;

      int               subfamilyLevel = Integer.parseInt(ConfigFile.getProperty(uplVersion + LEVEL_SUBFAMILY));
      int uplVersionInt = Integer.parseInt(uplVersion);
      int userId = Integer.parseInt(userIdStr);
      try{
        stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_CLASSIFICATION);
 

        // Insert records into the classification table
        Enumeration sfIds = sfIdToSfAcc.keys();

        while (sfIds.hasMoreElements()){
          Integer sfId = (Integer) sfIds.nextElement();
          String  acc = (String) sfIdToSfAcc.get(sfId);
          String  sfName = (String) sfAccToSfAnnot.get(acc);

          stmt.setInt(1, sfId.intValue());
          stmt.setInt(2, uplVersionInt);
          stmt.setString(3, sfName);
          stmt.setString(4, acc);
          stmt.setInt(5, subfamilyLevel);
          stmt.setInt(6, userId);
          stmt.setString(7, null);
          stmt.addBatch();


        }

        // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
        // System.out.println("Start of insert subfamililes " + df.format(new java.util.Date(System.currentTimeMillis())));
        stmt.executeBatch();


        // System.out.println("End of isert pro cls subfamililes " + df.format(new java.util.Date(System.currentTimeMillis())));
        stmt.close();

      }
      catch (SQLException se){
        System.out.println("Exception while trying to save subfamily information " + se.getMessage()
                           + " has been returned");
        return "Exception while trying to save subfamily information " + se.getMessage() + " has been returned";
      }
      return Constant.STR_EMPTY;
    }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param isProtein
   * @param userIdStr
   * @param commentIdToClsProId
   * @param clsProIdToComment
   *
   * @return
   *
   * @see
   */
//  protected String insertComments(Connection con, boolean isProtein, String userIdStr,
//                                         Hashtable commentIdToClsProId, Hashtable clsProIdToComment){
//    String  query;
//
//    try{
//
//      // Insert new comment
//      if (true == isProtein){
//        query = UpdateString.PREPARED_INSERT_EMPTY_PROTEIN_COMMENT;
//      }
//      else{
//        query = UpdateString.PREPARED_INSERT_EMPTY_CLS_COMMENT;
//      }
//      Enumeration commentIds = commentIdToClsProId.keys();
//
//      while (commentIds.hasMoreElements()){
//
//        // Insert empty comment
//        PreparedStatement stmt = con.prepareStatement(query);
//        Integer           commentId = (Integer) commentIds.nextElement();
//        Integer           clsProtId = (Integer) commentIdToClsProId.get(commentId);
//        String            comment = (String) clsProIdToComment.get(clsProtId);
//
//        stmt.setInt(1, commentId.intValue());
//        stmt.setInt(2, clsProtId.intValue());
//        stmt.setInt(3, Integer.parseInt(userIdStr));
//        int count = stmt.executeUpdate();
//
//        if (1 != count){
//          stmt.close();
//          System.out.println("Unable to insert comment string");
//          return "Unable to insert comment string";
//        }
//        stmt.close();
//
//        // Get the clob just inserted
//        stmt = con.prepareStatement(QueryString.PREPARED_COMMENTS_CLOB);
//        stmt.setInt(1, commentId.intValue());
//        ResultSet rstClob = stmt.executeQuery();
//
//        if (rstClob.next()){
//          CLOB  clob = ((OracleResultSet) rstClob).getCLOB(1);
//
//          rstClob.close();
//          stmt.close();
//          clob.putString(1, comment);
//
//          // Set the clob
//          stmt = con.prepareStatement(UpdateString.PREPARED_UPDATE_COMMENT_CLOB);
//          stmt.setClob(1, clob);
//          stmt.setInt(2, commentId.intValue());
//          stmt.executeUpdate();
//          stmt.close();
//        }
//        else{
//          rstClob.close();
//          stmt.close();
//          System.out.println("Unable to insert comment string into clob");
//          return "Unable to insert comment string into clob";
//        }
//      }
//    }
//    catch (SQLException se){
//      System.out.println("Exception while trying to save tree information " + se.getMessage() + " has been returned");
//      return "Exception while trying to save tree information " + se.getMessage() + " has been returned";
//    }
//    return "";
//  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param ClsToCRIds
   * @param CRIdsToCatIds
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
  protected String insertClsRltn(Connection con, Hashtable ClsToCRIds, Hashtable CRIdsToCatIds,
                                        String userIdStr){
    PreparedStatement stmt = null;

    try{
      stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_CLS_RLTN);

      // Insert records into the classification relationship table
      Enumeration clsIds = ClsToCRIds.keys();

      while (clsIds.hasMoreElements()){
        Integer clsId = (Integer) clsIds.nextElement();
        int     crIds[] = (int[]) ClsToCRIds.get(clsId);

        for (int i = 0; i < crIds.length; i++){
          Integer crId = new Integer(crIds[i]);
          Integer catId = (Integer) CRIdsToCatIds.get(crId);

          stmt.setInt(1, crId.intValue());
          stmt.setInt(2, clsId.intValue());
          stmt.setInt(3, catId.intValue());
          stmt.setInt(4, Integer.parseInt(userIdStr));
          stmt.addBatch();
        }
      }

      // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      // System.out.println("Start of insert cls rltn " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.executeBatch();

      // System.out.println("End of cls rltn " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Exception while trying to save category to classification information " + se.getMessage()
                         + " has been returned");
      return "Exception while trying to save category to classification information " + se.getMessage()
             + " has been returned";
    }
    return "";
  }
  
  
  protected String insertEvidenceWithConf(Connection con, String userIdStr, Hashtable addInfoTbl, String uplVersion, Hashtable protToProtAcc) {
    if (null == clsTypeIdToCatId){
      initCatLookup();
    }
    if (null == clsTypeIdToCatId){
      return "Save Operation failed - unable to retrieve category information";
    }  
    PreparedStatement stmt = null;

    try{
      stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_EVIDENCE_WITH_CONF);
  
      Hashtable catAccToCatIdTable = (Hashtable) clsTypeIdToCatId.get(uplVersion);    
      Enumeration addEnum = addInfoTbl.keys();
      while (addEnum.hasMoreElements()) {
        Integer protId = (Integer)addEnum.nextElement();
        Hashtable clsToConfTbl = (Hashtable)addInfoTbl.get(protId);
        Enumeration clsEnum = clsToConfTbl.keys();
        while(clsEnum.hasMoreElements()) {
          String clsAcc = (String)clsEnum.nextElement();
          Vector evList = (Vector)clsToConfTbl.get(clsAcc);
          Enumeration evValues = evList.elements();
          while (evValues.hasMoreElements()) {
            Vector evidence = (Vector)evValues.nextElement();
            String evType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_TYPE);
            String evValue = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_VALUE);
            String confCodeType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_TYPE);
            String confCodeSid = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_SID);
            String evTypeId = ConfigFile.getProperty(evType);
//            System.out.println("Adding evidence for protein id " + protId + " with accession " + (String)protToProtAcc.get(protId) + " and classification accession " + clsAcc + " cls id = " + catAccToCatIdTable.get(clsAcc) + " and confidence information evidence type " +
//                                evType + " with type id = " + evTypeId + " evValue " + evValue + " confidence type " + confCodeType + " conf code sid " +  confCodeSid);                                                                   
            stmt.setInt(1, Integer.parseInt(evTypeId));
            stmt.setInt(2, Integer.parseInt(confCodeSid));
            stmt.setInt(3, Integer.parseInt((String)catAccToCatIdTable.get(clsAcc)));
            stmt.setInt(4, protId.intValue());
            stmt.setString (5, evValue);
            stmt.setInt(6, Integer.parseInt(userIdStr));
            stmt.addBatch();                                
          }
        }  
      }
      int numUpdated [] = stmt.executeBatch();
//      for (int i = 0; i < numUpdated.length; i++) {
//        System.out.println("num added is " + numUpdated[i]);
//      }
      


    // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
    // System.out.println("Start of insert evidence with conf code " + df.format(new java.util.Date(System.currentTimeMillis())));


    // System.out.println("End of insert evidence with conf code " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.close();
    }
    catch (SQLException se){
      String errMsg = "Exception while trying to save evidence with confidence " + se.getMessage() + " has been returned";
      System.out.println(errMsg);
      return errMsg;
    }
    return "";
  }
  
  
  protected String obsoleteEvidenceWithConf(Connection con, String userIdStr, Hashtable removeInfoTbl, String uplVersion, Hashtable protToProtAcc) {
    if (null == clsTypeIdToCatId){
      initCatLookup();
    }
    if (null == clsTypeIdToCatId){
      return "Save Operation failed - unable to retrieve category information";
    }  
    PreparedStatement stmt = null;

    try{
      stmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_EVIDENCE_WITH_CONF_CODE);
  
      Hashtable catAccToCatIdTable = (Hashtable) clsTypeIdToCatId.get(uplVersion);    
      Enumeration removedEnum = removeInfoTbl.keys();
      while (removedEnum.hasMoreElements()) {
        Integer protId = (Integer)removedEnum.nextElement();
        Hashtable clsToConfTbl = (Hashtable)removeInfoTbl.get(protId);
        Enumeration clsEnum = clsToConfTbl.keys();
        while(clsEnum.hasMoreElements()) {
          String clsAcc = (String)clsEnum.nextElement();
          Vector evList = (Vector)clsToConfTbl.get(clsAcc);
          Enumeration evValues = evList.elements();
          while (evValues.hasMoreElements()) {
            Vector evidence = (Vector)evValues.nextElement();
            String evType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_TYPE);
            String evValue = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_VALUE);
            String confCodeType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_TYPE);
            String confCodeSid = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_SID);
            String evTypeId = ConfigFile.getProperty(evType);
//            System.out.println("obsoleting evidence for protein id " + protId + " with accession " + (String)protToProtAcc.get(protId) + " and classification accession " + clsAcc + " cls id = " + catAccToCatIdTable.get(clsAcc) + " and confidence information evidence type " +
//                                evType + " with type id = " + evTypeId + " evValue " + evValue + " confidence type " + confCodeType + " conf code sid " +  confCodeSid);                                                                   
            stmt.setInt(1, Integer.parseInt(userIdStr));
            stmt.setInt(2, Integer.parseInt((String)catAccToCatIdTable.get(clsAcc)));
            stmt.setInt(3, protId.intValue());            
            stmt.setInt(4, Integer.parseInt(evTypeId));            
            stmt.setInt(5, Integer.parseInt(confCodeSid));
            stmt.setString (6, evValue);
            stmt.addBatch();                                
          }
        }  
      }
      int numUpdated [] = stmt.executeBatch();
//      for (int i = 0; i < numUpdated.length; i++) {
//        System.out.println("num updated is " + numUpdated[i]);
//      }


    // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
    // System.out.println("Start of insert evidence with conf code " + df.format(new java.util.Date(System.currentTimeMillis())));


    // System.out.println("End of insert evidence with conf code " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.close();
    }
    catch (SQLException se){
      String errMsg = "Exception while trying to obsolete evidence with confidence " + se.getMessage() + " has been returned";
      System.out.println(errMsg);
      return errMsg;
    }
    return "";
  }  

  /**
   * Method declaration
   *
   *
   * @param con
   * @param famId
   * @param statusId
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
  protected String insertCurationStatus(Connection con, Integer famId, int statusId, String userIdStr){
    PreparedStatement stmt = null;

    try{

      // Insert record into the curation status table
      stmt = con.prepareStatement(UpdateString.PREPARED_INSERT_CURATION_STATUS);
      stmt.setInt(1, famId.intValue());
      stmt.setInt(2, Integer.parseInt(userIdStr));
      stmt.setInt(3, statusId);
      int numUpdated = stmt.executeUpdate();
      System.out.println("Number of records inserted into curation status table is " + numUpdated);
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Exception while trying to insert curation status " + se.getMessage() + " has been returned");
      return "Exception while trying to save curation status";
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param famId
   * @param statusId
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
  protected String UpdateCurtionStatus(Connection con, Integer famId, int statusId, String userIdStr){
    PreparedStatement stmt = null;
    Integer           curStsId = null;

    try{

      // Get the curated status record
      stmt = con.prepareStatement(QueryString.PREPARED_CURATION_STATUS_ID);
      stmt.setInt(1, famId.intValue());
      stmt.setInt(2, Integer.parseInt(userIdStr));
      stmt.setInt(3, statusId);
      ResultSet rst = stmt.executeQuery();

      if (rst.next()){
        curStsId = new Integer(rst.getInt(1));
      }
      rst.close();
      stmt.close();

      // No previous curated status record, insert a new record
      if (null == curStsId){
        return insertCurationStatus(con, famId, statusId, userIdStr);
      }

      // Update the creation date of the old record
      stmt = con.prepareStatement(UpdateString.PREPARED_UPDATE_CURATION_STATUS);
      stmt.setInt(1, curStsId.intValue());
      if (1 != stmt.executeUpdate()){
        stmt.close();
        return "Could not modify curated status record";
      }
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Exception while trying to modify curation status " + se.getMessage() + " has been returned");
      return "Exception while trying to save curation status";
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param famId
   * @param statusId
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
  protected String deleteCurationStatus(Connection con, Integer famId, int statusId, String userIdStr){
    try{
      PreparedStatement stmt = con.prepareStatement(UpdateString.PREPARED_BOOK_UNLOCK);

      stmt.setInt(1, famId.intValue());
      stmt.setInt(2, Integer.parseInt(userIdStr));
      stmt.setInt(3, statusId);

      // System.out.println(UpdateString.PREPARED_BOOK_UNLOCK);
      int numUpdated = stmt.executeUpdate();

      stmt.close();
      if (1 != numUpdated){
        return "Changed status of " + numUpdated + " records, instead of 1.";
      }
    }
    catch (SQLException se){
      System.out.println("Unable to delete record from database, exception " + se.getMessage() + " has been returned.");
      return "Unable to delete record from database, exception " + se.getMessage() + " has been returned.";
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param updateCon
   * @param carryover_type
   * @param oldToNewSfIds
   *
   * @return
   *
   * @see
   */
  protected String carryOverCommentEvidence(Connection updateCon, int carryover_type, Hashtable oldToNewSfIds){
    PreparedStatement stmt = null;
    String            query;

    switch (carryover_type){
      case CARRYOVER_COMMENT:{
        query = UpdateString.PREPARED_CARRYOVER_CLS_COMMENT;
        break;
      }
      case CARRYOVER_EVIDENCE:{
        query = UpdateString.PREPARED_CARRYOVER_CLS_EVIDENCE;
        break;
      }
      case CARRYOVER_FEATURE:{
        query = UpdateString.PREPARED_CARRYOVER_CLS_FEATURE;
        break;
      }
      default:{
        return "Unknown carryover type specified";
      }
    }
    try{
      stmt = updateCon.prepareStatement(query);
      Enumeration clsIds = oldToNewSfIds.keys();

      while (clsIds.hasMoreElements()){

        // carryover entries in the comments/evidence table
        Integer oldClsId = (Integer) clsIds.nextElement();
        Integer newClsId = (Integer) oldToNewSfIds.get(oldClsId);

        stmt.setInt(1, newClsId.intValue());
        stmt.setInt(2, oldClsId.intValue());
        stmt.addBatch();
      }

      // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      // System.out.println("Start of carryover " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.executeBatch();
      stmt.close();

      // System.out.println("End of carryover " + df.format(new java.util.Date(System.currentTimeMillis())));
    }
    catch (SQLException se){
      System.out.println("Exception while trying to carryover comments/evidence records " + se.getMessage()
                         + " has been returned");
      return "Exception while trying to carryover comments/evidence records " + se.getMessage() + " has been returned";
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param clsForRemoval
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
  protected String obsoleteSubfamilies(Connection con, Hashtable clsForRemoval, String userIdStr){
    try{
      PreparedStatement clsStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_CLS);
      PreparedStatement cmtStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_COMMENTS);
      PreparedStatement ptnStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_PROTEIN_CLS);
      PreparedStatement clsRltnStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_CLS_RLTN);
      PreparedStatement eviStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_EVIDENCE);
      PreparedStatement feaStmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_FEATURE);
      Enumeration       clsIds = clsForRemoval.keys();

      while (clsIds.hasMoreElements()){

        // Obsolete records in the classification table
        Integer clsId = (Integer) clsIds.nextElement();

        clsStmt.setInt(1, Integer.parseInt(userIdStr));
        clsStmt.setInt(2, clsId.intValue());
        clsStmt.addBatch();

        // Obsolete records in the comments table
        cmtStmt.setInt(1, Integer.parseInt(userIdStr));
        cmtStmt.setInt(2, clsId.intValue());
        cmtStmt.addBatch();

        // Obsolete records in the protein classification table
        ptnStmt.setInt(1, Integer.parseInt(userIdStr));
        ptnStmt.setInt(2, clsId.intValue());
        ptnStmt.addBatch();

        // Obsolete records in the classification relation table.  Both the subfamily record
        // as well as any categories pointing to the subfamily have to be obsoleted
        clsRltnStmt.setInt(1, Integer.parseInt(userIdStr));
        clsRltnStmt.setInt(2, clsId.intValue());
        clsRltnStmt.addBatch();

        // Obsolete records in the evidence table
        eviStmt.setInt(1, Integer.parseInt(userIdStr));
        eviStmt.setInt(2, clsId.intValue());
        eviStmt.addBatch();

        // Obsolete records in the feature table
        feaStmt.setInt(1, Integer.parseInt(userIdStr));
        feaStmt.setInt(2, clsId.intValue());
        feaStmt.addBatch();
      }

      // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      // System.out.println("Start of obsolete subfamilies " + df.format(new java.util.Date(System.currentTimeMillis())));
      clsStmt.executeBatch();

      // System.out.println("End of clsStmt " + df.format(new java.util.Date(System.currentTimeMillis())));
      cmtStmt.executeBatch();

      // System.out.println("End of cmtStmt " + df.format(new java.util.Date(System.currentTimeMillis())));
      ptnStmt.executeBatch();

      // System.out.println("End of ptnStmt " + df.format(new java.util.Date(System.currentTimeMillis())));
      clsRltnStmt.executeBatch();

      // System.out.println("End of clsRltnStmt " + df.format(new java.util.Date(System.currentTimeMillis())));
      eviStmt.executeBatch();

      // System.out.println("End of eviStmt " + df.format(new java.util.Date(System.currentTimeMillis())));
      feaStmt.executeBatch();

      // System.out.println("End of feaStmt " + df.format(new java.util.Date(System.currentTimeMillis())));
      clsStmt.close();
      cmtStmt.close();
      ptnStmt.close();
      clsRltnStmt.close();
      eviStmt.close();
      feaStmt.close();
    }
    catch (SQLException se){
      System.out.println("Exception while trying to obsolete classification records " + se.getMessage()
                         + " has been returned");
      return "Exception while trying to obsolete classification records " + se.getMessage() + " has been returned";
    }
    return "";
  }

  /**
   * Method declaration
   *
   *
   * @param con
   * @param clsForRemoval
   * @param userIdStr
   *
   * @return
   *
   * @see
   */
  protected String obsoleteClsRltn(Connection con, Hashtable clsForRemoval, String userIdStr){
    PreparedStatement stmt = null;

    try{
      stmt = con.prepareStatement(UpdateString.PREPARED_OBSOLETE_CLS_RLTN_FOR_PRNT);

      // Obsolete records from the classification relationship table
      Enumeration clsIds = clsForRemoval.keys();

      while (clsIds.hasMoreElements()){

        // Obsolete records in the classification table
        Integer clsId = (Integer) clsIds.nextElement();
        Vector  clsPrntList = (Vector) clsForRemoval.get(clsId);

        for (int i = 0; i < clsPrntList.size(); i++){
          stmt.setInt(1, Integer.parseInt(userIdStr));
          stmt.setInt(2, clsId.intValue());
          stmt.setInt(3, ((Integer) clsPrntList.elementAt(i)).intValue());
          stmt.addBatch();
        }
      }

      // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      // System.out.println("Start of obsolete clsRltn " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.executeBatch();

      // System.out.println("End of clsRltn " + df.format(new java.util.Date(System.currentTimeMillis())));
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Exception while trying to obsolete classification relation records " + se.getMessage()
                         + " has been returned");
      return "Exception while trying to obsolete classification relation records " + se.getMessage()
             + " has been returned";
    }
    return Constant.STR_EMPTY;
  }

  // protected static int[] getUids(int numRequired) {
  //
  // int uids[] = new int[numRequired];
  // for (int i = 0; i < numRequired; i++) {
  // uids[i] = uid_num;
  // uid_num++;
  // }
  // return uids;
  // }

  /**
   * Method declaration
   *
   *
   * @param numRequired
   *
   * @return
   *
   * @see
   */
  protected int[] getUids(int numRequired){

    // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
    // System.out.println("Start of generate uids Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
    int         uids[] = new int[numRequired];
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      Statement stmt = con.createStatement();
      ResultSet rst;

      // System.out.println(QueryString.UID_GENERATOR);
      for (int i = 0; i < numRequired; i++){
        rst = stmt.executeQuery(QueryString.UID_GENERATOR);
        while (rst.next()){
          uids[i] = rst.getInt(1);
        }
        rst.close();
      }
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }

    // System.out.println("End of generate uids Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
    return uids;
  }

  /**
   * Method declaration
   *
   *
   * @param list1
   * @param list2
   *
   * @see
   */
  protected void removeStrIntersect(Vector list1, Vector list2){
    if ((list1 == null) || (list2 == null)){
      return;
    }
    for (int i = 0; i < list1.size(); i++){
      String  s1 = (String) list1.elementAt(i);

      for (int j = 0; j < list2.size(); j++){
        String  s2 = (String) list2.elementAt(j);

        if (0 == s1.compareTo(s2)){
          list1.remove(i);
          list2.remove(j);
          i--;
          j--;
          break;
        }
      }
    }
  }

  /**
   * Method declaration
   *
   *
   * @param list1
   * @param list2
   *
   * @return
   *
   * @see
   */
  protected boolean compareStringVectors(Vector list1, Vector list2){
    if (list1.size() != list2.size()){
      return false;
    }
    String  list1Array[] = new String[list1.size()];

    list1.copyInto(list1Array);
    java.util.Arrays.sort(list1Array);
    String  list2Array[] = new String[list2.size()];

    list2.copyInto(list2Array);
    java.util.Arrays.sort(list2Array);
    for (int i = 0; i < list1Array.length; i++){
      if (0 != list1Array[i].compareTo(list2Array[i])){
        return false;
      }
    }
    return true;
  }

  /**
   * Method declaration
   *
   *
   * @param newTbl
   * @param origTbl
   *
   * @return
   *
   * @see
   */
  protected Vector compareStringToVectorHash(Hashtable newTbl, Hashtable origTbl){

    // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
    // System.out.println("Start of comparison Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
    Hashtable   toAddTbl = new Hashtable();
    Hashtable   toRemoveTbl = new Hashtable();
    Enumeration eList = newTbl.keys();

    // Create a list of subfamilies to add
    while (eList.hasMoreElements()){
      String  key = (String) eList.nextElement();
      Vector  newProtList = (Vector) newTbl.get(key);
      Vector  origProtList = (Vector) origTbl.get(key);

      if (null == origProtList){
        toAddTbl.put(key, newProtList);
        continue;
      }
      if (newProtList.size() != origProtList.size()){
        toAddTbl.put(key, newProtList);
        toRemoveTbl.put(key, origProtList);
        continue;
      }

      // The sizes of the lists are same, make sure the lists contain the same elements
      String  newProtArray[] = new String[newProtList.size()];

      newProtList.copyInto(newProtArray);
      java.util.Arrays.sort(newProtArray);
      String  origProtArray[] = new String[origProtList.size()];

      origProtList.copyInto(origProtArray);
      java.util.Arrays.sort(origProtArray);
      for (int i = 0; i < newProtArray.length; i++){
        if (0 != newProtArray[i].compareTo(origProtArray[i])){

          // Since the associated list has changed, this entry has to be removed from the old table
          // and added into the new table
          toAddTbl.put(key, newProtList);
          toRemoveTbl.put(key, origProtList);
          break;
        }
      }
    }

    // Now create list of items to remove
    eList = origTbl.keys();
    while (eList.hasMoreElements()){
      String  sfName = (String) eList.nextElement();

      if (null == newTbl.get(sfName)){
        toRemoveTbl.put(sfName, origTbl.get(sfName));
      }
    }
    if (toAddTbl.isEmpty() && (toRemoveTbl.isEmpty())){

      // System.out.println("end of comparison Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
      return null;
    }
    Vector  returnList = new Vector(2);

    returnList.add(toAddTbl);
    returnList.add(toRemoveTbl);

    // System.out.println("end of comparison Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
    return returnList;
  }

  /**
   * Class declaration
   *
   *
   * @author
   * @version %I%, %G%
   */
  protected class Family{
    public Integer  id;
    public String   name;
    public String   eValue;
  }

  /**
   * Method declaration
   *
   *
   * @param uplVersion
   * @param famSubFamAcc
   *
   * @return
   *
   * @see
   */
  public String getFamSubFamAccToName(String uplVersion, String famSubFamAcc){
    if (null == uplToBookInfo){
      initBookInfo();
      if (null == uplToBookInfo){
        return null;
      }
    }
    String  book = null;

    if (-1 == famSubFamAcc.indexOf(":")){
      book = famSubFamAcc;
    }
    else{
      book = famSubFamAcc.substring(0, famSubFamAcc.indexOf(":"));
    }
    Hashtable bookListTable = (Hashtable) uplToBookInfo.get(uplVersion);

    if (null == bookListTable){
      return null;
    }
    Hashtable bookTable = (Hashtable) bookListTable.get(book);

    if (null == bookTable){
      bookTable = (Hashtable) getBookTable(uplVersion, book);
      if (null == bookTable){
        return null;
      }
      bookListTable.put(book, bookTable);
    }
    return (String) bookTable.get(famSubFamAcc);
  }

  /**
   * Method declaration
   *
   *
   * @see
   */
  protected void initBookInfo(){

    // If book information is already initialized, return
    if (null != uplToBookInfo){
      return;
    }

    // Make sure all the upl version to release dates are initialized
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      return;
    }
    Connection  con = null;

    uplToBookInfo = new Hashtable();
    try{
      con = getConnection();
      if (null == con){
        return;
      }

      // Get information about each upl
      Enumeration uplVersions = clsIdToVersionRelease.keys();

      while (uplVersions.hasMoreElements()){
        String  uplVersion = (String) uplVersions.nextElement();
        String  query = QueryString.PREPARED_FAMILY_SUBFAMILY_LIST;

        // If this version of the upl has been released, then add clause to ensure only records
        // created prior to the release date are retrieved
        Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
        String  dateStr = (String) clsInfo.elementAt(1);

        if (null != dateStr){
          query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "c");
        }
        else{
          query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
        }



         log.info("Query family and subfamily accession to name - " + query);

        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setInt(1, Integer.parseInt(uplVersion));
        int famDepth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_famLevel"));
        int sfDepth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_subfamLevel"));

        stmt.setInt(2, famDepth);
        stmt.setInt(3, sfDepth);

        // Add values for the date clause
        if (null != dateStr){
          stmt.setString(4, dateStr);
          stmt.setString(5, dateStr);
        }
        ResultSet rst = stmt.executeQuery();

        rst.setFetchSize(200);
        Hashtable bookListTbl = new Hashtable();

        while (rst.next()){
          String  accession = rst.getString(1);
          String  name = rst.getString(2);

          if (null == name){
            name = "";
          }
          String  bookAcc = null;

          if (-1 == accession.indexOf(":")){
            bookAcc = accession;
          }
          else{
            bookAcc = accession.substring(0, accession.indexOf(":"));
          }
          Hashtable famTable = (Hashtable) bookListTbl.get(bookAcc);

          if (null == famTable){
            famTable = new Hashtable();
            bookListTbl.put(bookAcc, famTable);
          }
          famTable.put(accession, name);
        }
        if (null == uplToBookInfo){
          uplToBookInfo = new Hashtable();
        }
        uplToBookInfo.put(uplVersion, bookListTbl);
        rst.close();
        stmt.close();
      }
    }
    catch (SQLException se){

        log.error("Unable to retrieve book accession to name information, exception " + se.getMessage()
                  + " has been returned.");

    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){

            log.error("Unable to close connection, exception " + se.getMessage() + " has been returned.");

        }
      }
    }
  }

  /**
   * Method declaration
   *
   *
   * @param upl
   * @param book
   *
   * @see
   */
  public void resetUplToBookInfo(String upl, String book){
    if (null == uplToBookInfo){
      return;
    }
    Hashtable bookListTbl = (Hashtable) uplToBookInfo.get(upl);

    if (null == bookListTbl){
      return;
    }
    Hashtable bookInfo = (Hashtable) bookListTbl.get(book);

    if (null == bookInfo){
      return;
    }
    bookListTbl.remove(book);
  }

  /**
   * Method declaration
   *
   *
   * @param uplVersion
   * @param book
   *
   * @return
   *
   * @see
   */
  protected Hashtable getBookTable(String uplVersion, String book){

    // Make sure all the upl version to release dates are initialized
    if (null == clsIdToVersionRelease){
      initClsLookup();
    }

    // Make sure release dates can be retrieved, else return null
    if (null == clsIdToVersionRelease){
      return null;
    }
    Hashtable   bookTable = new Hashtable();
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      String  query = QueryString.PREPARED_FAMILY_SUBFAMILY;

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "c");
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }

        log.info("Query family and subfamily accession to name for book - " + query);

      PreparedStatement stmt = con.prepareStatement(query);

      stmt.setInt(1, Integer.parseInt(uplVersion));
      int famDepth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_famLevel"));
      int sfDepth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_subfamLevel"));

      stmt.setInt(2, famDepth);
      stmt.setInt(3, sfDepth);
      stmt.setString(4, book + "%");

      // Add values for the date clause
      if (null != dateStr){
        stmt.setString(5, dateStr);
        stmt.setString(6, dateStr);
      }
      ResultSet rst = stmt.executeQuery();

      rst.setFetchSize(200);
      while (rst.next()){
        bookTable.put(rst.getString(1), rst.getString(2));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){

        log.error("Unable to retrieve book accession to name information, exception " + se.getMessage()
                  + " has been returned.");

    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){

            log.error("Unable to close connection, exception " + se.getMessage() + " has been returned.");

        }
      }
    }
    return bookTable;
  }
  
  

  /**
   * Method declaration
   *
   *
   * @return
   *
   * @see
   */
  public Hashtable getUplToBookInfo(){
    return uplToBookInfo;
  }
  
  
  
    // Get GO inference information
    public Hashtable<String, Vector<Evidence>> getGOInference(String book, String uplVersion) {
        return new Hashtable();
    }
  
    // Sequence level annotations
    public Hashtable<String, Vector<Evidence>> getGOAnnotation(String book, String uplVersion) {
        // No longer have this data in database
        return new Hashtable<String, Vector<Evidence>>();
//        if (null == book || 0 == book.length()){
//          return null;
//        }
//        Hashtable<String, Vector<Evidence>>   rsltTable = new Hashtable<String, Vector<Evidence>>();
//        Connection  con = null;
//
//        try{
//          con = getConnection();
//          if (null == con){
//            return null;
//          }
//          
//          
//          
//            if (null == clsIdToVersionRelease){
//              initClsLookup();
//            }
//
//            // Make sure release dates can be retrieved, else return null
//            if (null == clsIdToVersionRelease){
//              return null;
//            }
//            // Get the annotation node to go relationship
//            String goAnnotQuery = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_node);
//            goAnnotQuery = addVersionReleaseClause(uplVersion, goAnnotQuery, TABLE_NAME_pn);
//            goAnnotQuery = addVersionReleaseClause(uplVersion, goAnnotQuery, TABLE_NAME_p);
//            goAnnotQuery = addVersionReleaseClause(uplVersion, goAnnotQuery, TABLE_NAME_pc);
//            goAnnotQuery = addVersionReleaseClause(uplVersion, goAnnotQuery, TABLE_NAME_c);
//            goAnnotQuery = addVersionReleaseClause(uplVersion, goAnnotQuery, TABLE_NAME_e);
//            goAnnotQuery = QueryString.PREPARED_GO_ANNOTATION + goAnnotQuery;
//            
//
//
//            goAnnotQuery = Utils.replace(goAnnotQuery, QUERY_PARAMETER_1, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_GO_CLS_TYPE_SID));
//            goAnnotQuery = Utils.replace(goAnnotQuery, QUERY_PARAMETER_2, ConfigFile.getProperty(uplVersion + PROPERTY_GO_SUPPORT_CONF_CODE));
//            System.out.println(goAnnotQuery);
//
//          PreparedStatement gstmt = con.prepareStatement(goAnnotQuery);
//          gstmt.setInt(1, Integer.parseInt(uplVersion));
//          gstmt.setString(2, book + QUERY_WILDCARD);
//
//
//          
//          // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
//          // System.out.println("start of identifier execution " + df.format(new java.util.Date(System.currentTimeMillis())));
//          ResultSet grslt = gstmt.executeQuery();
//
//          // System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
//          grslt.setFetchSize(100);
//          while (grslt.next()){
//
//            // Annot Id to GO annotation information
//            String nodeId  = grslt.getString(COLUMN_NAME_ACCESSION);
//            String primary_ext_id = grslt.getString(COLUMN_PRIMARY_EXT_ID);
//            String clsAcc = grslt.getString(COLUMN_NAME_GO_ACCESSION);
//            String clsName = grslt.getString(COLUMN_NAME_GO_NAME);
//            String evidence = grslt.getString(COLUMN_NAME_EVIDENCE);
//            String type = grslt.getString(COLUMN_NAME_ASPECT);
//            String confCode = grslt.getString(COLUMN_CONFIDENCE_CODE);
//            String qualifierStr = grslt.getString(COLUMN_NAME_QUALIFIER);
//
//            if (null == evidence || 0 == evidence.length()) {
//                evidence = EvidenceSpecifier.EVIDENCE_NOT_SPECIFIED;
//            }
//            if (null == confCode) {
//                confCode = EvidenceSpecifier.EVIDENCE_CODE_NOT_SPECIFIED;
//            }
//            Vector<Evidence> annotations = rsltTable.get(nodeId);
//            
//            if (null == annotations) {
//                annotations = new Vector<Evidence>(1);
//                rsltTable.put(nodeId, annotations);
//            }
//            EvidenceSpecifier es = new EvidenceSpecifier(primary_ext_id, null, evidence, null, confCode);
//            Vector <EvidenceSpecifier> esList = new Vector <EvidenceSpecifier>(1);
//            esList.add(es);
//            boolean notQualifier = false;
//            if (null != qualifierStr) {
//                qualifierStr = qualifierStr.trim();
//                if (true == qualifierStr.equals(QUALIFIER_NOT)) {
//                    notQualifier = true;
//                }
//            }
//            Evidence e = new Evidence(clsAcc, clsName, type, esList, notQualifier);
//            if (null != qualifierStr) {
//                e.setQualifierStr(qualifierStr);
//            }
//            annotations.add(e);
//
//            
//
//          }
//          grslt.close();
//          gstmt.close();
//        }
//        catch (SQLException se){
//          System.out.println("Unable to retrieve GO Annotation information from database, exception " + se.getMessage()
//                             + " has been returned.");
//        }
//        finally{
//          if (null != con){
//            try{
//              con.close();
//            }
//            catch (SQLException se){
//              System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
//              return null;
//            }
//          }
//        }
//        
//        Enumeration<String> keys = rsltTable.keys();
//        while (keys.hasMoreElements()) {
//            String key = keys.nextElement();
//            Vector<Evidence> value = rsltTable.get(key);
//            value = Evidence.organizeEvidence(value);
//            rsltTable.put(key, value);
//        }
//        
//        return rsltTable;

    }
    
    public Hashtable<String, Object> formatAnnotIdForTbl(Hashtable<String,Object> aTable) {
        if (null == aTable) {
            return null;
        }
        Hashtable<String, Object> rtnTbl = new Hashtable<String, Object>();
        Enumeration keys = aTable.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object o = aTable.get(key);
            if (null == key || null == o) {
                System.out.println("Here");
            }
            
            if (null == Utils.getAnnotId((String)key)) {
                System.out.println("Here");
            }
            rtnTbl.put(Utils.getAnnotId((String)key), o);
        }
        return rtnTbl;
    }



    public Hashtable<String, Vector<Evidence> > formatAnnotIdForGO(Hashtable<String,Vector<Evidence>> aTable) {
        if (null == aTable) {
            return null;
        }
        Hashtable<String, Vector<Evidence>> rtnTbl = new Hashtable<String, Vector<Evidence>>();
        Enumeration keys = aTable.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Vector<Evidence> o = aTable.get(key);

            rtnTbl.put(Utils.getAnnotId((String)key), o);
        }
        return rtnTbl;
    }
    
    
    private Hashtable<String, String> formatGoAnnot(Hashtable<String, Vector<Evidence>> goTable) {
        if (null == goTable) {
            return null;
        }
        Hashtable<String, String> rtnTbl = new Hashtable<String, String>();
        Enumeration keys = goTable.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Vector<Evidence> evidenceList = goTable.get(key);
            String formatStr = formatEvidence(evidenceList);
            rtnTbl.put((String)key, formatStr);
        }
        return rtnTbl;
    }
    
    
    private static String formatEvidence(Vector<Evidence> evidenceList) {
        if (null == evidenceList) {

            return null;
        }
        int num = evidenceList.size();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
        
            Evidence e = evidenceList.get(i);
            Vector <EvidenceSpecifier> esList = e.getEvidenceSpecifierList();
            sb.append(EvidenceSpecifier.formatEvidenceWith(esList));
            sb.append(Constant.STR_DASH);
            if (true == e.isNotQualifier()) {
                sb.append(QUALIFIER_NOT);
            }
            sb.append(e.getAccession());
            sb.append(Constant.STR_EQUAL);
            sb.append(e.getName());
            sb.append(Constant.STR_BRACKET_ROUND_OPEN);
            sb.append(EvidenceSpecifier.formatEvidenceIdInfo(esList));
            sb.append(Constant.STR_BRACKET_ROUND_CLOSE);
            sb.append(Constant.STR_SEMI_COLON);
        }
        return sb.toString();
    }
    
    public Hashtable<String, Hashtable<String, Evidence>> getAnnotations(String book, String uplVersion) {
        if (null == book || 0 == book.length()){
          return null;
        }
        
        if (null == uplToClsHierarchyData || null == uplToClsHierarchyData.get(uplVersion)) {
            initClsHierarchyData(uplVersion);
        }
        
        if (null == uplToClsHierarchyData) {
            return null;
        }
        
        Classification root = uplToClsHierarchyData.get(uplVersion);
        if (null == root) {
            return null;
        }
        
        // Get list of supported classifications
        Hashtable<String, Classification> asnToClsTbl = root.getAccToClsTbl();
        if (null == asnToClsTbl) {
            return null;
        }
        
        int autoCurationId = Integer.parseInt(ConfigFile.getProperty(PROPERTY_AUTO_CURATION_USER_ID));
        // Need to keep annotation for each node separately since, accession by itself is not unique; qualifier has to be considered
        Hashtable<String, Hashtable<String, Evidence>> annotTbl = new Hashtable<String, Hashtable<String, Evidence>>();

        
        Connection con = null;
        
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }


            if (null == clsIdToVersionRelease) {
                initClsLookup();
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                return null;
            }
            // Get the annotation node to go or pc relationship
            String assCls =
                addVersionReleaseClause(uplVersion, Constant.STR_EMPTY,
                                        TABLE_NAME_n);
            assCls = addVersionReleaseClause(uplVersion, assCls, TABLE_NAME_a);
            assCls = addVersionReleaseClause(uplVersion, assCls, TABLE_NAME_c);
            assCls = QueryString.PREPARED_ASSOCIATED_CLS + assCls;


            System.out.println(assCls);

            PreparedStatement gstmt = con.prepareStatement(assCls);
            gstmt.setInt(1, Integer.parseInt(uplVersion));
            gstmt.setString(2, book + QUERY_WILDCARD);


            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of associated annotation execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            ResultSet grslt = gstmt.executeQuery();

            System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            grslt.setFetchSize(100);
            while (grslt.next()) {

                // Only handle annotations that are supported
                String accession = grslt.getString(COLUMN_NAME_ACCESSION);
                if (null == asnToClsTbl.get(accession)) {
                    continue;
                }
                int userId = grslt.getInt(COLUMN_NAME_CREATED_BY);

                String nodeId = grslt.getString(COLUMN_NAME_NODE_ACCESSION);
                Hashtable<String, Evidence> valueTbl = annotTbl.get(nodeId);
                if (null == valueTbl) {
                    valueTbl = new Hashtable<String, Evidence>();
                    annotTbl.put(nodeId, valueTbl);
                }
                
                // If valueTbl already has an annotation that has been reviewed, do not update
                Date creationDate = grslt.getDate(COLUMN_NAME_CREATION_DATE);
                Date previousCreationDate = null;

                Evidence previous = valueTbl.get(accession);
                if (null != previous) {
                    previousCreationDate = previous.getCreationDate();
                }
                if (null != previous && true == previous.isReviewed() && null != previousCreationDate && null != creationDate && previousCreationDate.after(creationDate) ) {
                    continue;
                }
                
                Evidence e = new Evidence(accession, grslt.getString(COLUMN_NAME_NAME), grslt.getString(COLUMN_NAME_ASPECT), null, false);
                e.setCreationDate(creationDate);
                String qualifier = grslt.getString(COLUMN_NAME_QUALIFIER);
                if (null != qualifier &&
                    true == qualifier.equals(QUALIFIER_NOT)) {
                }
                if (null != qualifier) {
                    e.setQualifierStr(qualifier);
                    if (true == qualifier.equals(QUALIFIER_NOT)) {
                        e.setNotQualifier(true);
                    }
                }
                if (autoCurationId != userId) {
                    e.setReviewed(true);
                }
                valueTbl.put(accession, e);
            }
            grslt.close();
            gstmt.close();
        } catch (SQLException se) {
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED +
                      se.getMessage());
            se.printStackTrace();
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    System.out.println(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION +
                                       se.getMessage());
                    se.printStackTrace();
                    return null;

                }
            }
        }
        
        
//        // If there are identical annotation accession entries in the carryover table as well as the reviewed table.  Remove the entry from the carryover table
//        Enumeration <String> reviewedIds = reviewedTbl.keys();
//        while (reviewedIds.hasMoreElements()) {
//            String reviewedId = reviewedIds.nextElement();
//            Hashtable<String, Evidence> reviewedValueTbl = reviewedTbl.get(reviewedId);
//            Hashtable<String, Evidence> carryoverValueTbl = carryOverTbl.get(reviewedId);
//            if (null == carryoverValueTbl) {
//                continue;
//            }
//            
//            Enumeration <String> annotValEnum = reviewedValueTbl.keys();
//            while (annotValEnum.hasMoreElements()) {
//                String annotVal = annotValEnum.nextElement();
//                Evidence carryOverVal = carryoverValueTbl.get(annotVal);
//                if (null != carryOverVal) {
//                    carryoverValueTbl.remove(annotVal);
//                }
//            }
//            if (true == carryoverValueTbl.isEmpty()) {
//                carryOverTbl.remove(reviewedId);
//            }
//        }
        
        
//        Hashtable<String, String> newCarryOverTbl = new Hashtable<String, String>();
//        Hashtable<String, String> newReviewedTbl = new Hashtable<String, String>();
//        Enumeration <String> ids = carryOverTbl.keys();
//        while (ids.hasMoreElements()) {
//            String id = ids.nextElement();
//            Hashtable<String, String> valueTbl = carryOverTbl.get(id);
//            Vector v = new Vector(valueTbl.values());
//            newCarryOverTbl.put(id, Utils.listToString(v, Constant.STR_EMPTY, Constant.STR_EMPTY));
//        }
//        
//        // Repeat for newReviewedTbl
//         ids = reviewedTbl.keys();
//         while (ids.hasMoreElements()) {
//             String id = ids.nextElement();
//             Hashtable<String, String> valueTbl = reviewedTbl.get(id);
//             Vector v = new Vector(valueTbl.values());
//             newReviewedTbl.put(id, Utils.listToString(v, Constant.STR_EMPTY, Constant.STR_EMPTY));
//         }
        
        return annotTbl;
    }
    
    
    public Hashtable<String, Hashtable<String, String>> getAssociatedCls(String book, String uplVersion) {

        if (null == book || 0 == book.length()){
          return null;
        }
        Hashtable<String, Hashtable<String, String>>   rsltTable = new Hashtable<String, Hashtable<String, String>>();
        Connection  con = null;

        try{
          con = getConnection();
          if (null == con){
            return null;
          }
          
          
          
            if (null == clsIdToVersionRelease){
              initClsLookup();
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease){
              return null;
            }
            // Get the annotation node to go relationship
            String assCls = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            assCls = addVersionReleaseClause(uplVersion, assCls, TABLE_NAME_a);
            assCls = addVersionReleaseClause(uplVersion, assCls, TABLE_NAME_c);
            assCls = QueryString.PREPARED_ASSOCIATED_CLS + assCls;


            System.out.println(assCls);

          PreparedStatement gstmt = con.prepareStatement(assCls);
          gstmt.setInt(1, Integer.parseInt(uplVersion));
          gstmt.setString(2, book + QUERY_WILDCARD);


          
          // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
          // System.out.println("start of associated cls execution " + df.format(new java.util.Date(System.currentTimeMillis())));
          ResultSet grslt = gstmt.executeQuery();

          // System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
          grslt.setFetchSize(100);
          while (grslt.next()){

            // Annot Id to GO annotation information
            String type  = grslt.getString(COLUMN_NAME_ASPECT);
            Hashtable<String, String> currentTable = rsltTable.get(type);
            if (null == currentTable) {
                currentTable = new Hashtable<String, String>();
                rsltTable.put(type, currentTable);
            }
            
            String nodeId = grslt.getString(COLUMN_NAME_NODE_ACCESSION);
            String value = currentTable.get(nodeId);
            if (null == value) {
                value = Constant.STR_EMPTY;
            }
            if (0 != value.length()) {
                value += Constant.STR_SEMI_COLON;
            }
            value += grslt.getString(COLUMN_NAME_ACCESSION) + Constant.STR_EQUAL + grslt.getString(COLUMN_NAME_NAME);
            String qualifier = grslt.getString(COLUMN_NAME_QUALIFIER);
            if (null != qualifier && true == qualifier.equals(QUALIFIER_NOT)) {
                value += Constant.STR_BRACKET_ROUND_OPEN + qualifier + Constant.STR_BRACKET_ROUND_CLOSE;
            }
            currentTable.put(nodeId, value);


          }
          grslt.close();
          gstmt.close();
        }
        catch (SQLException se){
          log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
          se.printStackTrace();
        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            catch (SQLException se){
              System.out.println(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
              se.printStackTrace();
              return null;

            }
          }
        }
                
        return rsltTable;

    }

    
    

  
  /**
   * Method declaration
   *
   *
   * @param book
   * @param identifiers
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  public Hashtable getPublicInterpro(String book, String uplVersion, String clsIdStr){
    if (clsIdStr == null || 0 == clsIdStr.length()) {
        return null;
    }
  
    // Now get the public interpro information for each sequence
    if (0 == book.length()){
      return null;
    }
    Hashtable   rsltTable = new Hashtable();
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        return null;
      }
      String query = QueryString.PUBLIC_INTERPRO;

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        query += Utils.replace(QueryString.RELEASE_CLAUSE, "tblName", "c");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }
      
      query = Utils.replace(query, "%1", book + ":%");      
      query = Utils.replace(query, "%2", uplVersion);
      query = Utils.replace(query, "%3", clsIdStr);      
      //System.out.println(query);
      
      Statement stmt = con.createStatement();

      
      // java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      // System.out.println("start of identifier execution " + df.format(new java.util.Date(System.currentTimeMillis())));
      
      //java.text.SimpleDateFormat  df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
      //System.out.println("Start of add public interpro " + df.format(new java.util.Date(System.currentTimeMillis())));
      

      ResultSet rst = stmt.executeQuery(query);

      //System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
      rst.setFetchSize(100);
      while (rst.next()){
      
        // Protein Id to interpro acc and name
        Integer   proteinId = new Integer(rst.getInt(1));
        String iprAcc = rst.getString(2);
        String iprName = rst.getString(3);
        
        Vector iprStrList = (Vector)rsltTable.get(proteinId);
        if (null == iprStrList) {
          iprStrList = new Vector();
          rsltTable.put(proteinId, iprStrList);
        }
        String iprStr = iprAcc + "=" + iprName;
        iprStrList.addElement(iprStr);                  
      }
      //System.out.println("end of public interpro result retrieval execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
      
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve public interpro information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    
    // Convert vector of strings to sorted string
    Enumeration protIds = rsltTable.keys();
    while (protIds.hasMoreElements()) {
      Integer protId = (Integer)protIds.nextElement();
      Vector iprStrList = (Vector)rsltTable.get(protId);
      String iprStrArray[] = new String[iprStrList.size()];
      iprStrList.copyInto(iprStrArray);
      Arrays.sort(iprStrArray);
      rsltTable.put(protId, Utils.listToString(iprStrArray, "",";"));
    }    
    return rsltTable;
  }
  
  
  public Hashtable getPanterInterpro(String book, String uplVersion, String clsIdStr){
    if (null == clsIdStr || 0 == clsIdStr.length()) {
        return null;
    }
  
    // Now get the panther interpro information for each sequence
    if (0 == book.length()){
      return null;
    }
    Hashtable   rsltTable = new Hashtable();
    Connection  con = null;

    try{
      con = getConnection();
      if (null == con){
        return null;
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease){
        return null;
      }
      String query = QueryString.PREPARED_PANTHER_SF_INTERPRO;

      // If this version of the upl has been released, then add clause to ensure only records
      // created prior to the release date are retrieved
      Vector  clsInfo = (Vector) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.elementAt(1);

      if (null != dateStr){
        query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "c");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
      }


      PreparedStatement stmt = con.prepareStatement(query);
      stmt.setString(1, book);
      stmt.setInt(2, Integer.parseInt(uplVersion));
      stmt.setInt(3, Integer.parseInt(ConfigFile.getProperty("panther_interpro_relation_sid")));
      stmt.setInt(4, Integer.parseInt(clsIdStr));
      //System.out.println(query);      

      ResultSet rst = stmt.executeQuery();

      // System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
      rst.setFetchSize(100);
      while (rst.next()){      
        // Protein Id to interpro name
        Integer   proteinId = new Integer(rst.getInt(1));
        String iprName = rst.getString(2);
        if (null == proteinId || null == iprName) {
          continue;
        }
        rsltTable.put(proteinId, iprName);
      }
      rst.close();
      stmt.close();
      
//      // Now handle family information      
//      query = QueryString.PREPARED_PANTHER_FAM_INTERPRO;
//
//      // If this version of the upl has been released, then add clause to ensure only records
//      // created prior to the release date are retrieved
//
//      if (null != dateStr){
//        query += Utils.replace(QueryString.PREPARED_RELEASE_CLAUSE, "tblName", "c");
//        query = Utils.replace(query, "%1", dateStr);
//      }
//      else{
//        query += Utils.replace(QueryString.NON_RELEASE_CLAUSE, "tblName", "c");
//      }
//
//
//      stmt = con.prepareStatement(query);
//      stmt.setString(1, book);
//      stmt.setInt(2, Integer.parseInt(uplVersion));
//      stmt.setInt(3, Integer.parseInt(ConfigFile.getProperty("panther_interpro_relation_sid")));
//      stmt.setInt(4, Integer.parseInt(clsIdStr));
//      //System.out.println(query);      
//
//      rst = stmt.executeQuery();
//
//      // System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
//      rst.setFetchSize(100);
//      while (rst.next()){      
//      
//        // Protein Id to interpro name
//        Integer   proteinId = new Integer(rst.getInt(1));
//        String iprName = (String)rsltTable.get(proteinId);
//        if (null == iprName) {
//          rsltTable.put(proteinId, rst.getString(2));                  
//        }
//      }
//      rst.close();
//      stmt.close();      
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve panther interpro information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
      if (null != con){
        try{
          con.close();
        }
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          return null;
        }
      }
    }
    return rsltTable;
  }


  /**
   * 
   * @return Vector of strings containing confidence code information formatted as follows:
   * id\tcode\tname
   * @throws Exception
   */
  public Vector getEvidenceTypes() throws Exception {
    if (null == confidenceCodesList) {
      initConfidenceCodesList();
    } 
    Vector v = new Vector(confidenceCodesList.size());
    StringBuffer sb = new StringBuffer();
    Enumeration confInfo = confidenceCodesList.elements();
    while (confInfo.hasMoreElements()) {
      String[] confInfoStrs = (String[])confInfo.nextElement();
      sb.setLength(0);
      sb.append(confInfoStrs[INDEX_CONF_CODE_SID]);
      sb.append(TAB_DELIM);
      sb.append(confInfoStrs[INDEX_CONF_CODE_TYPE]);
      sb.append(TAB_DELIM);
      sb.append(confInfoStrs[INDEX_CONF_CODE_NAME]);
      v.add(sb.toString());      
    }
    return v;
  }
  
  public String getConfSid(String confType) {
    if (null == confidenceCodesList) {
      try {
        initConfidenceCodesList();
      }
      catch (Exception e) {
        
      }
    }
    if (null == confidenceCodesList) {
      return null;
    }
    Enumeration confCodesEnum = confidenceCodesList.elements();
    while (confCodesEnum.hasMoreElements()) {
      String confInfo[] = (String[])confCodesEnum.nextElement();
      if (0 == confInfo[INDEX_CONF_CODE_TYPE].compareTo(confType)) {
        return confInfo[INDEX_CONF_CODE_SID];
      }
    }
    return null;
  }
  
  protected synchronized void initConfidenceCodesList() throws Exception {
    if (null != confidenceCodesList) {
      return;
    }
    Vector v = new Vector();
    String query = QueryString.GETALLEVIDENCETYPES;
    ResultSet rst = null;
    Connection con = null;
    Statement stmt = null;
    try {
      con = getConnection();
      if (null == con) {
        return;
      }
      
      stmt = con.createStatement();
      rst = stmt.executeQuery(query);
      while (rst.next()) {
        String id = rst.getString(1);
        String code = rst.getString(2);
        String name = rst.getString(3);
        if (null == id || null == code || null == name) {
          System.out.println("Skipping over confidence code, due to null value,  id = " + 
                             id + " code = " + code + " name = " + name);
          continue;
        }
        String confInfo[] = new String[3];
        confInfo[INDEX_CONF_CODE_SID] = id;
        confInfo[INDEX_CONF_CODE_TYPE] = code;
        confInfo[INDEX_CONF_CODE_NAME] = name;        
        v.add(confInfo);
      }
      confidenceCodesList = v;
      rst.close();
      rst = null;
      stmt.close();
      stmt = null;
    }
    catch (SQLException e) {
      System.err.println("SQL Exception in initConfidenceCodesList()." + 
                         e.getMessage());
      throw e;
    }
    catch (Exception ex) {
      System.err.println("Other exception in initConfidenceCodesList()." + 
                         ex.getMessage());
      throw ex;
    }
    finally{
      if (null != con){
        try{
          con.close();
        }        
        catch (SQLException se){
          System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
          
        }
      }       
    }  
  }
  protected Vector getConfDataDiff(String book, String uplVersion, int accIndex, String[] colHeadersTblNames, String attrTable[], Hashtable protAccToProt, Hashtable protToProtAcc) {
    Hashtable newConfTbl = getConfData(uplVersion, accIndex,colHeadersTblNames, attrTable, protAccToProt);      
    Hashtable oldConfTbl = getClsConfEvidence(uplVersion, book, ConfigFile.getProperty(uplVersion + "_cls_types"));
    
    Hashtable newCombinedTbl = combineClsConfTbl(newConfTbl);
    Hashtable oldCombinedTbl = combineClsConfTbl(oldConfTbl);
    Vector differences = compareClsConfTbl(oldCombinedTbl, newCombinedTbl, protToProtAcc);
//    Hashtable addInfoTbl = (Hashtable)differences.elementAt(0);
//    Hashtable removeInfoTbl = (Hashtable)differences.elementAt(1);
//    
//    Hashtable catAccToCatIdTable = (Hashtable) clsTypeIdToCatId.get(uplVersion);    
//    Enumeration addEnum = addInfoTbl.keys();
//    while (addEnum.hasMoreElements()) {
//      Integer protId = (Integer)addEnum.nextElement();
//      Hashtable clsToConfTbl = (Hashtable)addInfoTbl.get(protId);
//      Enumeration clsEnum = clsToConfTbl.keys();
//      while(clsEnum.hasMoreElements()) {
//        String clsAcc = (String)clsEnum.nextElement();
//        Vector evList = (Vector)clsToConfTbl.get(clsAcc);
//        Enumeration evValues = evList.elements();
//        while (evValues.hasMoreElements()) {
//          Vector evidence = (Vector)evValues.nextElement();
//          String evType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_TYPE);
//          String evValue = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_VALUE);
//          String confCodeType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_TYPE);
//          String confCodeSid = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_SID);
//          String evTypeId = ConfigFile.getProperty(evType);
//          System.out.println("Adding evidence for protein id " + protId + " with accession " + (String)protToProtAcc.get(protId) + " and classification accession " + clsAcc + " cls id = " + catAccToCatIdTable.get(clsAcc) + " and confidence information evidence type " +
//                              evType + " with type id = " + evTypeId + " evValue " + evValue + " confidence type " + confCodeType + " conf code sid " +  confCodeSid);                                                                   
//        }
//      }  
//    }
//    
//    Enumeration removeEnum = removeInfoTbl.keys();
//    while (removeEnum.hasMoreElements()) {
//      Integer protId = (Integer)removeEnum.nextElement();
//      Hashtable clsToConfTbl = (Hashtable)removeInfoTbl.get(protId);      
//      Enumeration clsEnum = clsToConfTbl.keys();
//      while(clsEnum.hasMoreElements()) {
//        String clsAcc = (String)clsEnum.nextElement();
//        Vector evList = (Vector)clsToConfTbl.get(clsAcc);
//        Enumeration evValues = evList.elements();
//        while (evValues.hasMoreElements()) {
//          Vector evidence = (Vector)evValues.nextElement();
//          String evType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_TYPE);
//          String evValue = (String)evidence.elementAt(INDEX_CONF_EVDNCE_EV_VALUE);
//          String confCodeType = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_TYPE);
//          String confCodeSid = (String)evidence.elementAt(INDEX_CONF_EVDNCE_CC_SID);
//          String evTypeId = ConfigFile.getProperty(evType);
//          System.out.println("Removing evidence for protein id " + protId + " with accession " + (String)protToProtAcc.get(protId) + " and classification accession " + clsAcc + " cls id = " + catAccToCatIdTable.get(uplVersion) + " and confidence information evidence type " +
//                              evType + " with type id = " + evTypeId + " evValue " + evValue + " confidence type " + confCodeType + " conf code sid " +  confCodeSid);                                                                   
//        }
//      }  
//    }
    
    return differences;    
  }  

  
  /**
   * Given the attribute table, stores the applicable confidence code data into a hashtable
   * @param uplVersion
   * @param accIndex
   * @param colHeadersTblNames
   * @param attrTable
   * @param protAccToProt
   * @return
   */
  protected Hashtable getConfData(String uplVersion, int accIndex, String[] colHeadersTblNames, String attrTable[], Hashtable protAccToProt) {
  
    Hashtable confInfoTbl = new Hashtable();  
    // Handle evidence and confidence codes associated with classifications and sequences
    // First get it into a format, where it can be compared with the old data easily
    String    clsTypeStr = ConfigFile.getProperty(uplVersion + "_cls_types");
    String[]  clsType = Utils.tokenize(clsTypeStr, COMMA_DELIM);    
    for (int i = 0; i < clsType.length; i++) {
      String  currentClsColumn = clsType[i];
      int     clsIndex = -1;
  
      for (int j = 0; j < colHeadersTblNames.length; j++){
        if (0 == currentClsColumn.compareTo(colHeadersTblNames[j])){
          clsIndex = j;
          break;
        }
      }
    
      // If no classification data for this cls type, continue  
      if (-1 == clsIndex){
        continue;
      }
      Hashtable clsToProtTbl = new Hashtable();
      confInfoTbl.put(clsType[i], clsToProtTbl);
    
      for (int j = 1; j < attrTable.length; j++){
        String  rowData = attrTable[j];
        String  cellData[] = Utils.tokenize(rowData, TAB_DELIM);
        String  acc = cellData[accIndex];
        acc = Utils.replace(acc, QUOTE, "");        
      
        if (0 == acc.length()){
          continue;
        }
        
        // Only interested with sequence data
        Integer protId = (Integer)protAccToProt.get(acc);
        if (null == protId) {
          continue;
        }
        
        // Get cell contents
        String cellContents = cellData[clsIndex];
        cellContents = Utils.replace(cellContents, QUOTE, "");        
        int evidenceStartIndex = cellContents.indexOf(BAR_DELIM);
        
        // If no evidence information, continue
        if (-1 == evidenceStartIndex) {
          continue;
        }
        

        Hashtable protToClsAccTbl = (Hashtable)getClsToConfInfo(cellContents);
        clsToProtTbl.put(protId, protToClsAccTbl);        
      }  
    }
    return confInfoTbl;
  }
  
  protected Hashtable combineClsConfTbl(Hashtable confInfoTbl) {
    if (null == confInfoTbl) {
      return null;
    }
    Hashtable condensedTbl = new Hashtable();
    Enumeration protToClsTblEnum = confInfoTbl.elements();
    while (protToClsTblEnum.hasMoreElements()) {
      Hashtable protToClsTbl = (Hashtable)protToClsTblEnum.nextElement();
      Enumeration protIds = protToClsTbl.keys();
      while (protIds.hasMoreElements()) {
        Object protId = protIds.nextElement();
        Hashtable clsToConfList = (Hashtable)condensedTbl.get(protId);
        if (null == clsToConfList) {
          condensedTbl.put(protId, (Hashtable)protToClsTbl.get(protId));   
          continue;
        }
        clsToConfList.putAll((Hashtable)protToClsTbl.get(protId));        
      }
    }
    return condensedTbl;
  }
  
  /** 
   * Compares the evidence information for all the proteins given an initial and final table where 
   * the classification type data has already been combined for both tables
   * Returns list of two hashtables, where first list contains items for addition and second
   * contains items for removal.
   * @param initialTbl classification confidence table where the data has been grouped using method combineClsConfTbl
   * @param finalTbl classification confidence table where the data has been grouped using method combineClsConfTbl
   * @return 
   */
  protected Vector compareClsConfTbl(Hashtable initialTbl, Hashtable finalTbl, Hashtable protToProtAcc) {
    Hashtable addTbl = new Hashtable();
    Hashtable removeTbl = new Hashtable();
    Vector rtnList = new Vector(2);
    rtnList.add(addTbl);
    rtnList.add(removeTbl);
    
    // Go through list of items in initial table.  If final table does not contain the item, add
    // them to the removal table.
    Enumeration initialProtEnum = initialTbl.keys();
    while (initialProtEnum.hasMoreElements()) {
      Object o = initialProtEnum.nextElement();
//      System.out.println("Traversing initial list for protein " + protToProtAcc.get(o));
      Hashtable finalClsTbl = (Hashtable)finalTbl.get(o);
      if (null == finalClsTbl) {
        removeTbl.put(o, initialTbl.get(o));
//        System.out.println("Protein " + protToProtAcc.get(o) + " no longer contains evidence");        
        continue;
      }
      
      // Both tables contain the classification information.  Compare and add to addition and
      // removal table
      Hashtable initialClsTbl = (Hashtable)initialTbl.get(o);
      Vector confInfo = compareClsToConfTbl(initialClsTbl, finalClsTbl);
      Hashtable additionTbl = (Hashtable)confInfo.elementAt(0);
      Hashtable removalTbl = (Hashtable)confInfo.elementAt(1);
      if (0 != additionTbl.size()) {
        addTbl.put(o, additionTbl);
//        System.out.println("Protein " + protToProtAcc.get(o) + " has additions");        
        
      }
      if (0 != removalTbl.size()) {
        removeTbl.put(o, removalTbl);
//        System.out.println("Protein " + protToProtAcc.get(o) + " has removals");         
      }      
    }
    
    // Go through list of items in final table.  If initial table does not contain the item, add
    // them to the addition table
    Enumeration finalProtEnum = finalTbl.keys();
    while (finalProtEnum.hasMoreElements()) {
      Object o = finalProtEnum.nextElement();
      Hashtable initialClsTbl = (Hashtable)initialTbl.get(o);
      if (null == initialClsTbl) {
        addTbl.put(o, finalTbl.get(o));
 //       System.out.println("Protein " + protToProtAcc.get(o) + " has additions since it is not in initial table");         
      }
    }    
    return rtnList; 
  }
  /** 
   * Compares the evidence information for a given protein
   * Returns list of two hashtables, where first list contains items for addition and second
   * contains items for removal.
   * @param initialTbl
   * @param finalTbl
   * @return 
   */  
  protected Vector compareClsToConfTbl(Hashtable initialTbl, Hashtable finalTbl) {
    Hashtable addTbl = new Hashtable();
    Hashtable removeTbl = new Hashtable();
    Vector rtnList = new Vector(2);
    rtnList.add(addTbl);
    rtnList.add(removeTbl);
    
    Enumeration initialClsEnum = initialTbl.keys();
    while (initialClsEnum.hasMoreElements()) {
      Object o = initialClsEnum.nextElement();
      Vector finalClsConfList = (Vector)finalTbl.get(o);
      // Initial list contains element, final table does not.  Add element for removal
      if (null == finalClsConfList) {
        removeTbl.put(o, initialTbl.get(o));
        continue;
      }
      // Both initial and final list contain classification.  Compare evidence
      // associated with classifications and determine ones to remove and add. 
      Vector initialClsConfList = (Vector)initialTbl.get(o);
      Vector differences = compareConfLists(initialClsConfList, finalClsConfList);
      Vector addList = (Vector)differences.elementAt(0);
      Vector removeList = (Vector)differences.elementAt(1);
      if (0 != addList.size()) {
        addTbl.put(o, addList);
      }
      if (0 != removeList.size()) {
        removeTbl.put(o, removeList);
      }
    }
    
    // Check if final table contains elements not contained in the initial list.
    // These have to be added to the add list.  No need to check for items that
    // are common to both the final and initial list again, since, this was
    // already done.
    Enumeration finalClsEnum = finalTbl.keys();
    while (finalClsEnum.hasMoreElements()) {
      Object o = finalClsEnum.nextElement();
      Vector initialClsConfList = (Vector)initialTbl.get(o);
      if (null == initialClsConfList) {
        addTbl.put(o, finalTbl.get(o));
      }
    }
    
    return rtnList;     
  }
  
  protected Vector compareConfLists(Vector initialList, Vector finalList) {
    Vector addList = new Vector();
    Vector removeList = new Vector();
    Vector rtnList = new Vector(2);
    rtnList.add(addList);
    rtnList.add(removeList);
    boolean found = false;
    // Check for items appearing in initial list but, not in final list.  These
    // will be added to the remove list
    for (int  i = 0; i < initialList.size(); i++) {
      found = false;
      Vector inConfStrs = (Vector)initialList.elementAt(i);      
      for (int j = 0; j < finalList.size(); j++) {
        Vector fiConfStrs = (Vector)finalList.elementAt(j);        
        if (true == compareVectorOfStrings(inConfStrs, fiConfStrs)) {
          found = true;
          break;
        }
      }
      if (false == found) {
        removeList.add(inConfStrs);
      }
    }
    
    // Check for items appearing in the final list but, not in the initial list.  These
    // will be added to the add list.
     for (int  i = 0; i < finalList.size(); i++) {
      found = false;
      Vector fiConfStrs = (Vector)finalList.elementAt(i);      
      for (int j = 0; j < initialList.size(); j++) {
        Vector inConfStrs = (Vector)initialList.elementAt(j);        
        if (true == compareVectorOfStrings(fiConfStrs, inConfStrs)) {
          found = true;
          break;
        }
      }
      if (false == found) {
        addList.add(fiConfStrs);
      }
    }   
    return rtnList;
  }
  
  protected boolean compareVectorOfStrings(Vector v1, Vector v2) {
    int size = v1.size();
    if (size != v2.size()) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      String s1 = (String)v1.elementAt(i);
      String s2 = (String)v2.elementAt(i);
      if (0 != s1.compareTo(s2)) {
        return false;
      }    
    }
    return true;
  }
  
  /**
   * Given sequence classification and evidence information, returns a Hashtable
   * indexed by classification to Vector of confidence information
   */
  protected Hashtable getClsToConfInfo(String cellContents) {
    if (null == cellContents) {
      return null;
    }
    Hashtable clsToConfInfo = new Hashtable();
    String clsInfoStrs[] = Utils.tokenize(cellContents, SEMI_COLON_DELIM);
    for (int i  = 0; i < clsInfoStrs.length; i++) {
      String current = clsInfoStrs[i];
      int confInfoStartIndex = current.indexOf(BAR_DELIM);
      if (-1 == confInfoStartIndex) {
        continue;
      }
      String clsConfInfo[] = Utils.tokenize(current, LINKS_ASSOC);
      Vector allClsList = new Vector();
      clsToConfInfo.put(clsConfInfo[0], allClsList);
      String confInfo = current.substring(confInfoStartIndex);
      String confList[] = Utils.tokenize(confInfo, COMMA_DELIM);
      for (int j = 0; j < confList.length; j++) {
        String curConf = confList[j];
        String confInfoStrs[] = Utils.tokenize(curConf, BAR_DELIM);
        Vector v = new Vector(4);
        v.setSize(4);
        String confCode = confInfoStrs[0];        
        String evType = confInfoStrs[1];
        String evValue = confInfoStrs[2];
        String confSid = getConfSid(confCode);
        v.setElementAt(evType, INDEX_CONF_EVDNCE_EV_TYPE);
        v.setElementAt(evValue, INDEX_CONF_EVDNCE_EV_VALUE);
        v.setElementAt(confCode, INDEX_CONF_EVDNCE_CC_TYPE);
        v.setElementAt(confSid, INDEX_CONF_EVDNCE_CC_SID);             
        allClsList.add(v);
      }      
    }
    return clsToConfInfo;
  }
  
   
  /**
     *
     * @param geneSymbol 
     * @param uplVersion
     * @return
     */
//  public Vector searchBooksByGeneSymbol(String geneSymbol, String uplVersion) {
//    String wildcardStr = PERCENT + geneSymbol + PERCENT;
//    return SearchForBooks(QueryString.PREPARED_SEARCH_BOOKS_BY_GENE_SYMBOL, wildcardStr, uplVersion);
//  }
  
  
  public Vector searchBooksByGeneSymbol(String geneSymbol, String uplVersion) {
  
      if (null == clsIdToVersionRelease) {
          initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease) {
          return null;
      }
      
      if (null == geneSymbol) {
          return null;
      }

      String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_g);
      query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_pn);
      query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n);
      query = QueryString.SEARCH_NODE_GENE_SYMBOL + query;
      String wildcardStr = PERCENT + geneSymbol.toLowerCase() + PERCENT;
      query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
      query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
      
      return booksForQuery(query, uplVersion);

  }
  
//  public Vector searchBooksByGenePrimaryExtAcc(String genePrimaryExtAcc, String uplVersion) {
//    String wildcardStr = PERCENT + genePrimaryExtAcc + PERCENT;
//    return SearchForBooks(QueryString.PREPARED_SEARCH_BOOKS_BY_GENE_PRIMARY_EXT_ACC, wildcardStr, uplVersion);
//  }


   public Vector searchBooksByGenePrimaryExtAcc(String genePrimaryExtAcc, String uplVersion) {
       if (null == clsIdToVersionRelease) {
           initClsLookup();
       }

       // Make sure release dates can be retrieved, else return null
       if (null == clsIdToVersionRelease) {
           return null;
       }

       String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_g);
       query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_pn);
       query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n);
       String wildcardStr = PERCENT + genePrimaryExtAcc + PERCENT;
       query = QueryString.SEARCH_NODE_GENE_PRIMARY_EXT_ACC + query;
       query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
       query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
       

       return booksForQuery(query, uplVersion);
   }
   
    public Vector searchBooksByProteinPrimaryExtId(String proteinPrimaryExtId, String uplVersion) {
        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease) {
            return null;
        }

        String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_p);
        query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_pn);
        query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n);
        String wildcardStr = PERCENT + proteinPrimaryExtId + PERCENT;
        query = QueryString.SEARCH_NODE_PROTEIN_PRIMARY_EXT_ID + query;
        query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
        query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
        

        return booksForQuery(query, uplVersion);     
    }
  
//  public Vector searchBooksByProteinPrimaryExtId(String proteinPrimaryExtId, String uplVersion) {
//    String wildcardStr = PERCENT + proteinPrimaryExtId + PERCENT;
//    return SearchForBooks(QueryString.PREPARED_SEARCH_BOOKS_BY_PROTEIN_PRIMARY_EXT_ID, wildcardStr, uplVersion);      
//  }
  
  public Vector searchBooksByDefinition(String searchTerm, String uplVersion) {
      if (null == clsIdToVersionRelease) {
          initClsLookup();
      }

      // Make sure release dates can be retrieved, else return null
      if (null == clsIdToVersionRelease) {
          return null;
      }
      
      if (null == searchTerm) {
          return null;
      }

      String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_p);
      query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_pn);
      query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n);
      query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_i);
      String wildcardStr = PERCENT + searchTerm.toLowerCase() + PERCENT;
      query = QueryString.SEARCH_NODE_DEFINITION + query;
      query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
      query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
      

      return booksForQuery(query, uplVersion);     
  }

    public Vector getAllBooks(String uplVersion) {
        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease) {
            return null;
        }

        Hashtable<String, Book> bookTbl = getListOfBooksAndStatus(uplVersion);
        Vector books = new Vector(bookTbl.values());
        
        
        
        Book[] bookList = new Book[books.size()];
        books.copyInto(bookList);
        java.util.Arrays.sort(bookList);
        return new Vector(Arrays.asList(bookList));
    }
    
    public Vector getUncuratedUnlockedBooks(String uplVersion) {
    
        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease) {
            return null;
        }
        
        Hashtable<String, Book> bookTbl = getListOfBooksAndStatus(uplVersion);
        Enumeration <String> bookIds = bookTbl.keys();
        while (bookIds.hasMoreElements()) {
            String id = bookIds.nextElement();
            Book aBook = bookTbl.get(id);
            int status = aBook.getCurationStatus();
            if (true == aBook.hasStatus(Book.CURATION_STATUS_MANUALLY_CURATED) ||
                true == aBook.hasStatus(Book.CURATION_STATUS_CHECKED_OUT)) {
                bookTbl.remove(id);       
            }
        }
        
        Vector books = new Vector(bookTbl.values());        
        Book[] bookList = new Book[books.size()];
        books.copyInto(bookList);
        java.util.Arrays.sort(bookList);
        return new Vector(Arrays.asList(bookList));
    }    
    
    
    
    public Hashtable<String, Book> getListOfBooksAndStatus(String uplVersion) {
    
        String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c);
        query = QueryString.GET_LIST_OF_BOOKS + query;

        query = Utils.replace(query,QUERY_PARAMETER_1, ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
        query = Utils.replace(query,QUERY_PARAMETER_2, uplVersion);  
        
        Connection con = null;
        Hashtable<String, Book> bookTbl = new Hashtable<String, Book>();
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            
            // Get list of books
            Statement stmt = con.createStatement();
            ResultSet rst = stmt.executeQuery(query);
            while (rst.next()) {
                String accession = rst.getString(COLUMN_NAME_ACCESSION);
                String bookName = rst.getString(COLUMN_NAME_NAME);
                bookTbl.put(accession, new Book(accession, bookName, Book.CURATION_STATUS_UNKNOWN, null));
            }
            rst.close();
            
            
            // Get status and user information
             String checkOutStatus = ConfigFile.getProperty(CURATION_STATUS_CHECKOUT);
             query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c);
             query = QueryString.GET_STATUS_USER_INFO + query;

             query = Utils.replace(query,QUERY_PARAMETER_1, ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
             query = Utils.replace(query,QUERY_PARAMETER_2, uplVersion); 
             
             ResultSet irslt = stmt.executeQuery(query);
             while (irslt.next()) {
                String accession = irslt.getString(1);

                
                String curationStatusId = irslt.getString(6);
                String loginName = irslt.getString(7);

                // Get information about user who is locking the book
                User u = null;

                if (null != curationStatusId &&
                    true == curationStatusId.equals(checkOutStatus)) {
                    String firstNameLName = irslt.getString(3);
                    String email = irslt.getString(4);
                    String groupName = irslt.getString(COLUMN_NAME_GROUP_NAME);
                    u = new User(firstNameLName, null, email, loginName, Constant.USER_PRIVILEGE_NOT_SET, groupName);
                }
                
                 Book b = bookTbl.get(accession);
                 if (null != u) {
                    b.setLockedBy(u);
                 }
                 // Get status
                 int status;
                 if (null == curationStatusId) {
                     status = Book.CURATION_STATUS_UNKNOWN;
                 }
                 else {
                     status = getCurationStatusConversion(Integer.parseInt(curationStatusId));
                 }
                 int oldStatus = b.getCurationStatus();
                 int newStatus = status;
                 // If one of the statuses is unknown, do not list it
                 if (newStatus != Book.CURATION_STATUS_UNKNOWN && oldStatus != Book.CURATION_STATUS_UNKNOWN) {
                     newStatus = status | oldStatus;
                 }
                 else {
                     if (newStatus == Book.CURATION_STATUS_UNKNOWN) {
                         newStatus = oldStatus;
                     }
                 }
                 b.setCurationStatus(newStatus);

                 
                 
             }
            
            irslt.close();
            stmt.close();
        }
        catch (SQLException se){
          log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
          se.printStackTrace();
        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            catch (SQLException se){
              log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
              se.printStackTrace();
            }

          }
        }
        
        return bookTbl;

    }


    private Vector booksForQuery(String query, String uplVersion) {
        String bookList[] = getBookAccession(query);
        Hashtable <String, Book> allBookTbl = getListOfBooksAndStatus(uplVersion);
        
        int size = bookList.length;
        Vector<Book> finalList = new Vector<Book>(size);
        for (int i = 0; i < size; i++) {
            Book b = allBookTbl.get(bookList[i]);
            if (null != b) {
                finalList.add(b);
            }
//            else {
//                log.error("Did not find book for " + bookList[i]);
//            }
        }
        

        
        Object[] allList = new Object[finalList.size()];
        finalList.copyInto(allList);
        java.util.Arrays.sort(allList);
        return new Vector(Arrays.asList(allList));


    }
    
    public String[] getBookAccession(String query) {
        Connection  con = null;
        Hashtable<String, String> bookTbl = new Hashtable<String, String>();

        try{
          con = getConnection();
          if (null == con){
            return null;
          }

          Statement stmt = con.createStatement();


          System.out.println(query);
          ResultSet rst = stmt.executeQuery(query);

          while (rst.next()){
              String accession = rst.getString(COLUMN_NAME_ACCESSION);
              String bookId = Utils.getBookId(accession);
              bookTbl.put(bookId, bookId);
          }
          rst.close();
          stmt.close();
        }
        catch (SQLException se){
          System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                             + " has been returned.");
        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            catch (SQLException se){
              System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
            }

          }
        }
        
        int size = bookTbl.size();
        Vector<String> values = new Vector<String>(size);
        values = new Vector(bookTbl.values());
        String[] rtnArray = new String[size];
        values.copyInto(rtnArray);
        return rtnArray;
        
//        if (bookTbl.isEmpty()) {
//            return new String[0];
//        }
//        Vector v = new Vector(bookTbl.values());
//        
//        // Split list since, list may be too large to be used as part of "in" statement
//
//        int lastIndex = v.size() / MAX_NUM_BOOK_LIST + 1;
//        String[] strArray = new String[lastIndex];
//        for (int i = 0; i < lastIndex - 1; i++) {
//            strArray[i] = Utils.listToString(new Vector(v.subList(i * MAX_NUM_BOOK_LIST, (i + 1) * MAX_NUM_BOOK_LIST)), Constant.STR_QUOTE_SINGLE, Constant.STR_COMMA);
//        }
//        strArray[lastIndex - 1] = Utils.listToString(new Vector((v.subList((lastIndex - 1) * MAX_NUM_BOOK_LIST, v.size()))), Constant.STR_QUOTE_SINGLE, Constant.STR_COMMA);
//        return strArray;
        
    }

  
  
  
//  private Vector SearchForBooks(String preparedQuery, String searchTerm, String uplVersion) {
//  
//      Connection  con = null;
//      Vector bookList = new Vector();
//
//      try{
//        con = getConnection();
//        if (null == con){
//          return null;
//        }
//
//        PreparedStatement stmt = con.prepareStatement(preparedQuery);
//        stmt.setString(1, searchTerm);
//
//
//        int depth = Integer.parseInt(ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
//
//        stmt.setInt(2, depth);
//        stmt.setInt(3, Integer.parseInt(uplVersion));
//
//
//        //System.out.println(query);
//        ResultSet rst = stmt.executeQuery();
//
//        String checkOutStatus = ConfigFile.getProperty(CURATION_STATUS_CHECKOUT);
//        while (rst.next()){
//            String bookId = rst.getString(1);
//            String bookName = rst.getString(2);
//            String curationStatusId = rst.getString(6);
//            String loginName = rst.getString(7);
//            
//            // Get information about user who is locking the book
//            User u = null;
//            
//            if (null != curationStatusId &&
//                true == curationStatusId.equals(checkOutStatus)) {
//                String firstNameLName = rst.getString(3);
//                String email = rst.getString(4);
//                u = new User(firstNameLName, null, email, loginName, Constant.USER_PRIVILEGE_NOT_SET);
//                    
//            }
//            // Get status
//            int status;
//            if (null == curationStatusId) {
//                status = Book.CURATION_STATUS_UNKNOWN;
//            }
//            else {
//                status = getCurationStatusConversion(Integer.parseInt(curationStatusId));
//            }
//
//
//            Book aBook = new Book(bookId, bookName, status, u);
//            bookList.add(aBook);
//
////            Vector    bookInfo = new Vector(6);
////            
////          // System.out.println(rst.getString(1));
////            bookInfo.addElement(rst.getString(1));
////            bookInfo.addElement(rst.getString(2));
////            bookInfo.addElement(rst.getString(3));
////            bookInfo.addElement(rst.getString(4));
////            bookInfo.addElement(rst.getString(5));
////            bookInfo.addElement(rst.getString(6));
////            for (int i = 0; i < bookInfo.size(); i++) {
////                System.out.println((String)bookInfo.elementAt(i));
////            }
////            bookList.add(bookInfo);
//            
//            
//        }
//        rst.close();
//        stmt.close();
//      }
//      catch (SQLException se){
//        System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
//                           + " has been returned.");
//      }
//      finally{
//        if (null != con){
//          try{
//            con.close();
//          }
//          catch (SQLException se){
//            System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
//          }
//          return bookList;
//        }
//      }
//      return bookList;
//  }
  
  
//    private Vector getBooks(String query, String uplVersion) {
//    
//        Connection  con = null;
//        Hashtable<String, Book> bookTbl = new Hashtable<String, Book>();
//
//        try{
//          con = getConnection();
//          if (null == con){
//            return null;
//          }
//
//
//          Statement stmt = con.createStatement();
//
//
//          query = Utils.replace(query,QUERY_PARAMETER_1, ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
//          query = Utils.replace(query,QUERY_PARAMETER_2, uplVersion);
//
//
//
//          //System.out.println(query);
//          ResultSet rst = stmt.executeQuery(query);
//
//          String checkOutStatus = ConfigFile.getProperty(CURATION_STATUS_CHECKOUT);
//          while (rst.next()){
//              String bookId = rst.getString(1);
////              if (bookId.equals("PTHR10000")) {
////                  System.out.println("Here");
////              }
//              String bookName = rst.getString(2);
//              String curationStatusId = rst.getString(6);
//              String loginName = rst.getString(7);
//              
//              // Get information about user who is locking the book
//              User u = null;
//              
//              if (null != curationStatusId &&
//                  true == curationStatusId.equals(checkOutStatus)) {
//                  String firstNameLName = rst.getString(3);
//                  String email = rst.getString(4);
//                  String groupName = rst.getString(COLUMN_NAME_GROUP_NAME);
//                  u = new User(firstNameLName, null, email, loginName, Constant.USER_PRIVILEGE_NOT_SET, groupName);
//                      
//              }
//              // Get status
//              int status;
//              if (null == curationStatusId) {
//                  status = Book.CURATION_STATUS_UNKNOWN;
//              }
//              else {
//                  status = getCurationStatusConversion(Integer.parseInt(curationStatusId));
//              }
//              
//              
//              // There are multiple records for the same book.  Only want to return record with user not equal to null
//              Book sameBook = bookTbl.get(bookId);
//              if (null == sameBook) {
//                Book aBook = new Book(bookId, bookName, status, u);
//                bookTbl.put(bookId, aBook);
//            
//              }
//              else {
//              
//                  int newStatus = status;
//                  int oldStatus = sameBook.getCurationStatus();
//                  
//                  // If one of the statuses is unknown, do not list it
//                  if (newStatus != Book.CURATION_STATUS_UNKNOWN && oldStatus != Book.CURATION_STATUS_UNKNOWN) {
//                      newStatus = status | oldStatus;
//                  }
//                  else {
//                      if (newStatus == Book.CURATION_STATUS_UNKNOWN) {
//                          newStatus = oldStatus;
//                      }
//                  }
//                  User anotherUser = sameBook.getLockedBy();
//                  
//                  if (null == anotherUser && u != null) {
//                      Book aBook = new Book(bookId, bookName, newStatus, u);
//                      bookTbl.put(bookId, aBook);
//
//                  }
//                  else {
//                      sameBook.setCurationStatus(newStatus);
//                  }
//              }
//
//              
//              
//          }
//          rst.close();
//          stmt.close();
//        }
//        catch (SQLException se){
//          System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
//                             + " has been returned.");
//        }
//        finally{
//          if (null != con){
//            try{
//              con.close();
//            }
//            catch (SQLException se){
//              System.out.println("Unable to close connection, exception " + se.getMessage() + " has been returned.");
//            }
//            
//          }
//        }
//        return new Vector(bookTbl.values());
//    }
  
  protected int getCurationStatusConversion(int dbCurationId) {

      String curationStatusId = Integer.toString(dbCurationId);
      if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_CHECKOUT))) {
          return Book.CURATION_STATUS_CHECKED_OUT;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_NOT_CURATED))) {
          return Book.CURATION_STATUS_NOT_CURATED;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_AUTOMATICALLY_CURATED))) {
          return Book.CURATION_STATUS_AUTOMATICALLY_CURATED;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_MANUALLY_CURATED))) {
          return Book.CURATION_STATUS_MANUALLY_CURATED;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_REVIEWED))) {
          return Book.CURATION_STATUS_CURATION_REVIEWED;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_QAED))) {
          return Book.CURATION_STATUS_QAED;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_PARTIALLY_CURATED))) {
          return Book.CURATION_STATUS_PARTIALLY_CURATED;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_REQUIRE_PAINT_REVIEW))) {
          return Book.CURATION_STATUS_REQUIRE_PAINT_REVIEW;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_NOT_MAPPED))) {
          return Book.CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_NOT_MAPPED;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_CHANGE_FAMILIES))) {
          return Book.CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_CHANGE_FAMILIES;
      }
      else if (true == curationStatusId.equals(ConfigFile.getProperty(CURATION_STATUS_REQUIRE_PAINT_REVIEW_PTN_TRACKED_TO_CHILD_NODE))) {
          return Book.CURATION_STATUS_REQUIRE_PAINT_REVIEW_TRACKED_TO_CHILD_NODE;
      }       
      return Book.CURATION_STATUS_UNKNOWN;
      
  }

    public Vector<Hashtable<String, String>> getGeneInfo(String book, String uplVersion) {
        if (null == book) {
            return null;
        }

        Vector rsltList = new Vector(2);


        Hashtable geneIdentifierTbl = new Hashtable();
        Hashtable geneSymbolTbl = new Hashtable();
        Hashtable geneNameTbl = new Hashtable();
        rsltList.add(geneIdentifierTbl);
        rsltList.add(geneSymbolTbl);
        rsltList.add(geneNameTbl);

        Connection con = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                return null;
            }
            String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_pn);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_p);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_g);
            query = QueryString.PROTEIN_GENE + query;
//            System.out.println(query);



            
            query = Utils.replace(query, QUERY_PARAMETER_1, uplVersion);
            query = Utils.replace(query, QUERY_PARAMETER_2, book + QUERY_WILDCARD);

            Statement gstmt = con.createStatement();



            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of gene execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            ResultSet grslt = gstmt.executeQuery(query);

            System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            grslt.setFetchSize(100);
            while (grslt.next()) {

                // annotation id to gene information
                String annotId  = grslt.getString(COLUMN_NAME_ACCESSION);
                String geneIdentifier = grslt.getString(COLUMN_NAME_GENE_PRIMARY_EXT_ACC);
                geneIdentifier = formatGeneIdentifer(geneIdentifier);
                String geneSymbol = grslt.getString(COLUMN_NAME_GENE_SYMBOL);
                String geneName = grslt.getString(COLUMN_NAME_GENE_NAME);

                if (null != geneIdentifier) {
                    String previousGeneIdentifierInfo =
                        (String)geneIdentifierTbl.get(annotId);
                    if (null == previousGeneIdentifierInfo ||
                        0 == previousGeneIdentifierInfo.length()) {

                        geneIdentifierTbl.put(annotId, geneIdentifier);

                    } else {
                        geneIdentifierTbl.put(annotId,
                                        previousGeneIdentifierInfo + STR_COMMA +
                                        geneIdentifier);
                    }
                }
                if (null != geneSymbol) {
                    String previousGeneSymbolInfo =
                        (String)geneSymbolTbl.get(annotId);
                    if (null == previousGeneSymbolInfo ||
                        0 == previousGeneSymbolInfo.length()) {
                        geneSymbolTbl.put(annotId, geneSymbol);
                    } else {
                        if (false == previousGeneSymbolInfo.contains(geneSymbol)) {
                            geneSymbolTbl.put(annotId,
                                          previousGeneSymbolInfo + STR_COMMA +
                                          geneSymbol);
                        }
                    }
                }
                if (null != geneName) {
                    String previousGeneNameInfo = (String)geneNameTbl.get(annotId);
                    if (null == previousGeneNameInfo) {
                        geneNameTbl.put(annotId, geneName);
                    }
                    else {
                        if (false == previousGeneNameInfo.contains(geneName)) {
                            geneNameTbl.put(annotId, previousGeneNameInfo + STR_COMMA + geneName);
                        }
                    }
                }
            }
            grslt.close();
            gstmt.close();
        } catch (SQLException se) {
            System.out.println("Unable to retrieve gene information from database, exception " +
                               se.getMessage() + " has been returned.");
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    System.out.println("Unable to close connection, exception " +
                                       se.getMessage() +
                                       " has been returned.");
                    return null;
                }
            }
        }
        return rsltList;
    }
    
    
    
    public static String formatGeneIdentifer(String str) {
        if (null == str) {
            return null;
        }
        int firstIndex = str.indexOf(DELIM_GENE_IDENTIFIER);

        if (-1 == firstIndex) {
            return str;
        }
        firstIndex++;
        int secondIndex = str.indexOf(DELIM_GENE_IDENTIFIER, firstIndex + 1);
        if (-1 == secondIndex) {
            return str;
        }
        String geneStr = str.substring(firstIndex, secondIndex);   
        
        return geneStr.replaceFirst(GENE_IDENTIFIER_TOKEN, GENE_IDENTIFIER_REPLACEMENT);
        
    
    }
    
    
    public boolean verifySfNodes(PANTHERTree tree, Hashtable<String, String> anToSfId) {
        Hashtable<String, String> handledTbl = new Hashtable<String, String>();
        Hashtable<String, PANTHERTreeNode> nodesTbl = tree.getNodesTbl();
        Hashtable<String, String> anToSfIdCpy = (Hashtable<String, String>)anToSfId.clone();
        
        Enumeration <String> nodeEnum = nodesTbl.keys();
        while (nodeEnum.hasMoreElements()) {
            String anId = nodeEnum.nextElement();
            PANTHERTreeNode aNode = nodesTbl.get(anId);
            Vector children = aNode.getChildren();
            // Verify that each leaf node is associated with a subfamily
            if (null == children || 0 == children.size()) {
                //System.out.println("Processing " + anId);
                if (null != anToSfIdCpy.get(anId)) {
                    handledTbl.put(anId, anId);
                    anToSfIdCpy.remove(anId);
                    //System.out.println(anId + " is a leaf");
                    continue;
                }
                
                // Find its sf ancestor
                PANTHERTreeNode parent = aNode.getParent();
                if (null == parent) {
                    log.error(anId + MSG_AN_HAS_NO_SF_ANCESTOR);
                    return false;
                }
                
                while (null != parent) {
                    String parentAcc = parent.getAccession();
                    if (null != anToSfId.get(parentAcc)) {
                        handledTbl.put(parentAcc, parentAcc);
                        //System.out.println(parentAcc + " is sf ancestor");
                        anToSfIdCpy.remove(parentAcc);
                        break;
                    }
                    parent = parent.getParent();
                }
                
                if (null == parent) {
                    log.error(anId + MSG_AN_HAS_NO_SF_ANCESTOR);
                    return false;
                }  
            }
        }
        
        // All nodes should be removed
        if (false == anToSfIdCpy.isEmpty()) {
            Vector v = new Vector(anToSfIdCpy.values());
            log.error(MSG_SF_NOT_ASSOCIATED_WITH_NODES + Utils.listToString(v, Constant.STR_EMPTY, Constant.STR_COMMA));
            return false;
        }
        
        // Ensure both tables have same annotation ids
        if (handledTbl.size() != anToSfId.size()) {
            log.error(MSG_NUM_SF_NOT_EQUAL);
            return false;
        }
        
        Enumeration <String> anEnum = handledTbl.keys();
        while (anEnum.hasMoreElements()) {
            String anId = anEnum.nextElement();
            if (null == anToSfId.get(anId)) {
                log.error(MSG_NUM_SF_NOT_EQUAL);
                return false;
            }
        }
        return true;
    }
    

    
    
    public Hashtable<String, Hashtable<String, Evidence>> getAnnotTbl(String[] colHeadersTblNames, Hashtable<String, String[]> saveTbl) {
        String newAnnotCol = ConfigFile.getProperty(ATTR_COLUMN_PANTER_NEW_ANNOT);
        int index = -1;

        for (int i = 0; i < colHeadersTblNames.length; i++) {
            if (0 == newAnnotCol.compareTo(colHeadersTblNames[i])) {
                index = i;
                break;
            }
        }
        if (-1 == index) {
            log.error(MSG_SAVE_FAILED_NO_ANNOT_COL);
            return null;
        }
        
        Hashtable<String, Hashtable<String, Evidence>> rtnTbl = new Hashtable<String, Hashtable<String, Evidence>>();
        Enumeration<String> anEnum = saveTbl.keys();
        while (anEnum.hasMoreElements()) {
            String anId = anEnum.nextElement();
            String value = saveTbl.get(anId)[index];
            if (0 == value.length()) {
                continue;
            }
            Hashtable<String, Evidence> aTbl = Evidence.convertStringToTable(value);
            rtnTbl.put(anId, aTbl);
            
        }
        return rtnTbl;
         
    }
    
    
    public Hashtable<String, Evidence> getAddedEvidence(Hashtable<String, Evidence> newTbl, Hashtable<String, Evidence> origTbl) {
        Hashtable<String, Evidence> changedTbl = new Hashtable<String, Evidence>();
        Enumeration <String> accEnum = newTbl.keys();
        while (accEnum.hasMoreElements()) {
            String acc = accEnum.nextElement();
            Evidence newEvi = newTbl.get(acc);
            Evidence orig = origTbl.get(acc);
            if (null == orig) {
                changedTbl.put(acc, newEvi);
                continue;
            }
            
            if (true == newEvi.isReviewed() && true != orig.isReviewed()) {
                changedTbl.put(acc,newEvi);
                continue;
            }
            
            if (newEvi.isNotQualifier() != orig.isNotQualifier()) {
                changedTbl.put(acc, newEvi);
                continue;
            }
            
        }
        return changedTbl;
    }
    
    
    public Hashtable<String, Evidence> getRemovedEvidence(Hashtable<String, Evidence> newTbl, Hashtable<String, Evidence> origTbl) {
        Hashtable<String, Evidence> changedTbl = new Hashtable<String, Evidence>();
        Enumeration <String> accEnum = origTbl.keys();
        while (accEnum.hasMoreElements()) {
            String acc = accEnum.nextElement();
            Evidence newEvi = newTbl.get(acc);
            Evidence orig = origTbl.get(acc);
            if (null == newEvi) {
                changedTbl.put(acc, orig);
                continue;
            }
            if (newEvi.isNotQualifier() != orig.isNotQualifier()) {
                changedTbl.put(acc, orig);
            }
        }
        return changedTbl;
    }


    public PANTHERTree getPANTHERTree(String uplVersion, String book){
      Connection  con = null;
      Statement stmt = null;
      ResultSet rst = null;
      
      Hashtable<String, PANTHERTreeNode> nodeTbl = new Hashtable<String, PANTHERTreeNode>();
      PANTHERTreeNode root = null;
    

      try{
        con = getConnection();
        if (null == con){
          return null;
        }
        
        
        if (null == clsIdToVersionRelease){
          initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease){
          return null;
        }
        
        String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n1);
        query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n2);
        query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_nr);
        query = QueryString.PANTHER_TREE_STRUCTURE + query;

        query = Utils.replace(query, REPLACE_STR_PERCENT_1, uplVersion);
        query = Utils.replace(query, REPLACE_STR_PERCENT_2, book + QUERY_WILDCARD);


        stmt = con.createStatement();
        rst = stmt.executeQuery(query);


        while (rst.next()){
          String childAccession = rst.getString(COLUMN_NAME_CHILD_ACCESSION);
          String childId = Integer.toString(rst.getInt(COLUMN_NAME_CHILD_ID));
          String parentAccession = rst.getString(COLUMN_NAME_PARENT_ACCESSION);
          String parentId = Integer.toString(rst.getInt(COLUMN_NAME_PARENT_ID));
          PANTHERTreeNode child = nodeTbl.get(childAccession);
          if (null == child) {
              child = new PANTHERTreeNode();
              child.setAccession(childAccession);
              child.setNodeId(childId);
              nodeTbl.put(childAccession, child);
          }
          
          PANTHERTreeNode parent = nodeTbl.get(parentAccession);
          if (null == parent) {
              parent = new PANTHERTreeNode();
              parent.setAccession(parentAccession);
              parent.setNodeId(parentId);
              nodeTbl.put(parentAccession, parent);
          }
          child.setParent(parent);
          parent.addChild(child);
        }

        
        
        // Get Root
        Enumeration <PANTHERTreeNode> nodes = nodeTbl.elements();
        while (nodes.hasMoreElements()) {
            PANTHERTreeNode aNode = nodes.nextElement();
            if (null == aNode.getParent()) {
                root = aNode;
                break;
            }
        }
        
      }
      catch (SQLException se){
        log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
        se.printStackTrace();

      }
      finally{
          releaseDBResources(rst, stmt, con);

      }
      return new PANTHERTree(root, nodeTbl);
    }
    
    
    public Integer getAnnotTypeId(String acc, Hashtable<String, Integer> annotInfoTbl) {
        if (null == acc || null == annotInfoTbl) {
            return null;
        }
        Enumeration <String> typeEnum = annotInfoTbl.keys();
        while (typeEnum.hasMoreElements()) {
            String type = typeEnum.nextElement();
            if (acc.startsWith(type)) {
                return annotInfoTbl.get(type);
            }
        }
        return null;
    }
    
    public Hashtable<String, Integer> getAnnotationTypeInfo() {
        Connection con = null;
        Hashtable<String, Integer> rtnTbl = new Hashtable<String, Integer>();
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            Statement     stmt = con.createStatement();

            ResultSet     rst = stmt.executeQuery(QueryString.ANNOTATION_TYPE_INFO);
            while (rst.next()) {
                int id = rst.getInt(COLUMN_NAME_ANNOTATION_TYPE_ID);
                String type = rst.getString(COLUMN_NAME_ANNOTATION_TYPE);
                rtnTbl.put(type, id);
            }
            rst.close();
            stmt.close();

        }
        catch (SQLException se){
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();

        }
      finally{
        if (null != con){
          try{
            con.close();
          }
          catch (SQLException se){
            log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
            se.printStackTrace();
          }
        }
      }
      if (true == rtnTbl.isEmpty()) {
          return null;
      }
      return rtnTbl;

    }
    
    
    public Integer getNotQualifierId() {
        Connection con = null;
        int id = -1;
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            Statement     stmt = con.createStatement();

            ResultSet     rst = stmt.executeQuery(QueryString.NOT_QUALIFIER_ID);
            if (rst.next()) {
                id = rst.getInt(COLUMN_NAME_QUALIFIER_ID);
            }
            rst.close();
            stmt.close();

        }
        catch (SQLException se){
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();

        }
      finally{
        if (null != con){
          try{
            con.close();
          }
          catch (SQLException se){
            log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
            se.printStackTrace();
          }
        }
      }
      if (-1 == id) {
          return null;
      }
      return Integer.valueOf(id);

    }
    
    public Vector<Annotation> getAnnotationsForBook(String uplVersion, String book) {
        Connection con = null;
        Vector<Annotation> annotList = new Vector<Annotation>();
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            
            if (null == clsIdToVersionRelease) {
                initClsLookup();
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                return null;
            }
            
            
            String allAnnot = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            allAnnot = addVersionReleaseClause(uplVersion, allAnnot, TABLE_NAME_a);
            allAnnot = QueryString.ALL_ANNOTATIONS + allAnnot;
            
            allAnnot = Utils.replace(allAnnot, QUERY_PARAMETER_1, uplVersion);
            allAnnot = Utils.replace(allAnnot, QUERY_PARAMETER_2,  book + QUERY_WILDCARD);
            
            Statement     stmt = con.createStatement();

            ResultSet     rst = stmt.executeQuery(allAnnot);
            while (rst.next()) {
                int annotId = rst.getInt(COLUMN_NAME_ANNOTATION_ID);
                int nodeId = rst.getInt(COLUMN_NAME_NODE_ID);
                int annotTypeId = rst.getInt(COLUMN_NAME_ANNOTATION_TYPE_ID);
                int clsId = rst.getInt(COLUMN_NAME_CLASSIFICATION_ID);
                Annotation a = new Annotation(annotId, nodeId, clsId, annotTypeId);
                annotList.add(a);
            }
            rst.close();
            stmt.close();

        }
        catch (SQLException se){
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();
            annotList = null;

        }
      finally{
        if (null != con){
          try{
            con.close();
          }
          catch (SQLException se){
            log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
            se.printStackTrace();
          }
        }
      }
      return annotList;
    }
    
    
    public Vector<PANTHERTreeNode> getNodeInfoForBook(String book, String uplVersion) {
        Connection  con = null;
        
        Vector<PANTHERTreeNode> nodeList = new Vector<PANTHERTreeNode>();
        

        try{
          con = getConnection();
          if (null == con){
            return null;
          }
          
          
          if (null == clsIdToVersionRelease){
            initClsLookup();
          }

          // Make sure release dates can be retrieved, else return null
          if (null == clsIdToVersionRelease){
            return null;
          }
          
          String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
          query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n2);
          query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_nr);
          query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_g);
          query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_gn);
          query = QueryString.NODE_INFO + query;
          query = Utils.replace(query, REPLACE_STR_PERCENT_1, book + QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD);
          query = Utils.replace(query, REPLACE_STR_PERCENT_2, uplVersion);

          Statement     stmt = con.createStatement();
          ResultSet     rst = stmt.executeQuery(query);
          stmt.setFetchSize(200);

          while (rst.next()){
            PANTHERTreeNode node = new PANTHERTreeNode();
            node.setAccession(rst.getString(COLUMN_NAME_ACCESSION));
            node.setParentAccession(rst.getString(COLUMN_NAME_PARENT));
            node.setPublicId(rst.getString(COLUMN_NAME_PUBLIC_ID));
            Object o = rst.getObject(COLUMN_NAME_BRANCH_LENGTH);
            if (null != o) {
                node.setBranchLength(rst.getString(COLUMN_NAME_BRANCH_LENGTH));
            }
            node.setNodeType(rst.getString(COLUMN_NAME_NODE_TYPE));
            o = rst.getObject(COLUMN_NAME_EVENT_TYPE);
            if (null != o) {
                node.setEventType(rst.getString(COLUMN_NAME_EVENT_TYPE));
            }
            node.setLongGeneName(rst.getString(COLUMN_NAME_PRIMARY_EXT_ACC));
            nodeList.add(node);
          }
          rst.close();
          stmt.close();
          
                    
        }
        catch (SQLException se){
          log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
          se.printStackTrace();

        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            
            catch (SQLException se){
              log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
              
            }
          }
        }
        if (nodeList.isEmpty()) {
            return null;
        }
        return nodeList;

    }
    
    
    public Vector<PANTHERTreeNode> getAnToSFInfo(String book, String uplVersion) {
        Vector<PANTHERTreeNode> rtnList = new Vector<PANTHERTreeNode>();
        Connection con = null;
        
        try {
            con = getConnection();
            
            if (null == con){
              return null;
            }
            
            
            if (null == clsIdToVersionRelease){
              initClsLookup();
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease){
              return null;
            }
            
            // Get the subfamily to annotation node relationship
            String querySfAn = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_a);
            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_n);
            querySfAn = addVersionReleaseClause(uplVersion, querySfAn, TABLE_NAME_c);
            querySfAn = QueryString.PREPARED_AN_SF_NODE + querySfAn;
//            System.out.println(querySfAn);
            
            PreparedStatement stmt = con.prepareStatement(querySfAn);
            stmt.setString(1, uplVersion);
            stmt.setString(2, book + QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD);
            stmt.setString(3, ConfigFile.getProperty(PROPERTY_ANNOTATION_TYPE_SF));
        

            ResultSet rst = stmt.executeQuery();
            rst.setFetchSize(100);

            while (rst.next()){
                String sfId = rst.getString(COLUMN_NAME_ACCESSION);
                String sfName = rst.getString(COLUMN_NAME_NAME);
                String anId = rst.getString(5);
                PANTHERTreeNode ptn = new PANTHERTreeNode();
                ptn.setAccession(anId);
                ptn.setSfId(sfId);
                ptn.setSfName(sfName);
                rtnList.add(ptn);
            }
            rst.close();
            stmt.close();
            return rtnList;
        }
        catch (SQLException se){
          log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
          se.printStackTrace();
        }
        finally{
          if (null != con){
            try{
              con.close();
            }
            catch (SQLException se){
              log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
              return null;
            }
          }
        }
        return null;

    }
    
    
    public Vector<PANTHERTreeNode> getGeneInfoInNodeList(String book, String uplVersion) {
        if (null == book) {
            return null;
        }

        Hashtable<String, PANTHERTreeNode> accToNodeTbl = new Hashtable<String, PANTHERTreeNode>();


        Connection con = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            
            if (null == clsIdToVersionRelease) {
                initClsLookup();
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                return null;
            }
            String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_pn);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_p);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_g);
            query = QueryString.PROTEIN_GENE + query;
//            System.out.println(query);



            
            query = Utils.replace(query, QUERY_PARAMETER_1, uplVersion);
            query = Utils.replace(query, QUERY_PARAMETER_2, book + QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD);

            Statement gstmt = con.createStatement();



//            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
//            System.out.println("start of gene execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            ResultSet grslt = gstmt.executeQuery(query);

//            System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            grslt.setFetchSize(100);
            while (grslt.next()) {

                // annotation id to gene information
                String annotId  = grslt.getString(COLUMN_NAME_ACCESSION);
                String geneIdentifier = grslt.getString(COLUMN_NAME_GENE_PRIMARY_EXT_ACC);
                geneIdentifier = formatGeneIdentifer(geneIdentifier);
                String geneSymbol = grslt.getString(COLUMN_NAME_GENE_SYMBOL);
                if (null == annotId) {
                    continue;
                }
                
                PANTHERTreeNode node = accToNodeTbl.get(annotId);
                if (null == node) {
                     node = new PANTHERTreeNode();
                    accToNodeTbl.put(annotId, node);
                    node.setAccession(annotId);
                }


                if (null != geneIdentifier) {
                    node.addGeneIdentifier(geneIdentifier);
                }
                if (null != geneSymbol) {
                    node.addGeneSymbol(geneSymbol);
                }
            }
            grslt.close();
            gstmt.close();
            if (accToNodeTbl.isEmpty()) {
                return null;
            }
        } catch (SQLException se) {
            System.out.println("Unable to retrieve gene information from database, exception " +
                               se.getMessage() + " has been returned.");
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    System.out.println("Unable to close connection, exception " +
                                       se.getMessage() +
                                       " has been returned.");
                    return null;
                }
            }
        }
        return new Vector(accToNodeTbl.values());
    }
    
    
    public Vector<PANTHERTreeNode> getIdentifiersNodeInfoList(String book,
                                                                       String identifiers,
                                                                       String uplVersion) {

        // Now get the identifiers for each annotation node
        if (0 == book.length()) {
            return null;
        }

        Hashtable<String, PANTHERTreeNode> accToNodeTbl = new Hashtable<String, PANTHERTreeNode>();
        Connection con = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease){
              initClsLookup();
              if (null == clsIdToVersionRelease){
                return null;
              }
            }

            String idQuery = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY,TABLE_NAME_n);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_p);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_i);
            idQuery = QueryString.IDENTIFIER_AN + idQuery;

            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_1, uplVersion);
            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_2,  book + QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD);
            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_3, identifiers);
            System.out.println(idQuery);

            Statement istmt = con.createStatement();

    //            istmt.setInt(1, Integer.parseInt(uplVersion));
    //            istmt.setString(2, book + QUERY_WILDCARD);

            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of identifier query execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            ResultSet irslt = istmt.executeQuery(idQuery);

            System.out.println("end of query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            irslt.setFetchSize(100);
            while (irslt.next()) {
                
                String identifierName = irslt.getString(COLUMN_NAME_NAME);
                // Lots of null identifiers in database
                if (null == identifierName) {
                    continue;
                }
                
                identifierName = identifierName.trim();
                if (0 == identifierName.length()) {
                    continue;
                }


                String type = irslt.getString(COLUMN_NAME_IDENTIFIER_TYPE);
                if (null == type) {
                    continue;
                }
                
                String annotId = irslt.getString(COLUMN_NAME_ACCESSION);
                if (null == annotId) {
                    continue;
                }
                
                PANTHERTreeNode node = accToNodeTbl.get(annotId);
                if (null == node) {
                    node = new PANTHERTreeNode();
                    accToNodeTbl.put(annotId, node);
                }
                node.setAccession(annotId);
                node.addIdentifier(type, identifierName);
            }
            irslt.close();
            istmt.close();
            if (true == accToNodeTbl.isEmpty()) {
                return null;
            }
        } catch (SQLException se) {
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();
        } finally {
            if (null != con) {
                try {
                    con.close();
                } catch (SQLException se) {
                    log.error(MSG_ERROR_UNABLE_TO_CLOSE_CONNECTION + se.getMessage());
                    se.printStackTrace();
                    return null;
                }
            }
        }
        return new Vector(accToNodeTbl.values());
    }
    
    public ArrayList getOrganismList(String uplVersion) {
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;
        ArrayList orgList = null;
        try {
            con = getConnection();
            stmt = con.createStatement();
            orgList = new ArrayList();
            String query = QueryString.QUERY_ORGANISM.replaceAll(QUERY_PARAMETER_1, uplVersion);
            rst = stmt.executeQuery(query);            
            while (rst.next()) {
                int refGenome = rst.getInt(COLUMN_REF_GENOME);
                boolean isrefGenome = false;
                if (refGenome == 1) {
                    isrefGenome = true;
                }
                
                Organism organism = new Organism(rst.getString(COLUMN_DATABASE_ID), rst.getString(COLUMN_ORGANISM), rst.getString(COLUMN_CONVERSION), rst.getString(COLUMN_SHORT_NAME), rst.getString(COLUMN_NAME), rst.getString(COLUMN_COMMON_NAME), rst.getInt(COLUMN_LOGICAL_ORDERING), isrefGenome, rst.getString(COLUMN_TAXON_ID));
                orgList.add(organism);
            }
            rst.close();
            stmt.close();
            con.close();
        }
        catch(SQLException se) {
            orgList = null;
            se.printStackTrace();
        }
        finally {
            try {
                if (null != rst) {
                    rst.close();
                }
                if (null != stmt) {
                    stmt.close();
                }
                if (null != con) {
                    con.close();
                }
            }
            catch(SQLException e) {
                
            }
        }
        return orgList;

    }




  /**
   * Method declaration
   *
   *
   * @param args
   *
   * @see
   */
  public static void main(String[] args){
  DataServer ds = DataServerManager.getDataServer("dev_3_panther_upl");
  
  String treeStr[] = ds.getTree("PTHR10000", "20");
  if (null != treeStr) {
      for (int i = 0; i < treeStr.length; i++) {
          System.out.println(treeStr[i]);
      }
  }
//    Hashtable<String, Hashtable<String, Evidence>> annotationsTbl = ds.getAnnotations("PTHR10024", "13");
//
//    Enumeration <String> ids = annotationsTbl.keys();
//    while (ids.hasMoreElements()) {
//        String id = ids.nextElement();
//        System.out.println("Processing id " + id);
//        Hashtable<String, Evidence> evidenceTbl = annotationsTbl.get(id);
//        Enumeration <Evidence> evidenceEnum = evidenceTbl.elements();
//        while (evidenceEnum.hasMoreElements()) {
//            Evidence e = evidenceEnum.nextElement();
//            System.out.println(e.getAccession() + " " + e.getType());
//        }
//    }
//  ds.getAllBooks("11");
//  Hashtable annotTbl = ds.getAnnotations("PTHR10000", "11");
//  ds.formatAnnotIdForTbl(annotTbl);
//  Vector<Annotation> annotList = ds.getAnnotationsForBook("11", "PTHR10003");
//  for (int i = 0; i < annotList.size(); i++) {
//      Annotation a = annotList.get(i);
//      System.out.println(a.getAnnotId() + " " + a.getNodeId() + " " + a.getClsId() + " " + a.getAnnotTypeId());
//  }
//  System.out.println(ds.getNotQualifierId().intValue());
  
//  DataServer ds = new DataServer("dev_3_panther_upl");
//  Vector books = new Vector(2);
//  books.add("PTHR10000");
//  books.add("PTHR10010");
//  Vector ids = ds.getClsIdsForBooksToLock("8", books);
//  if (null != ids) {
//    for (int i = 0; i < ids.size(); i++) {
//      System.out.println((String)ids.elementAt(i));
//    }
//  }
//   Vector books = new Vector();
//   books.add("PTHR10000");
//   books.add("PTHR10010");
    


//    DataServer ds = new DataServer("dev_3_panther_upl");
//    Vector v = ds.searchBooksByGeneSymbol("YW", "8");
//    ds.searchBooksByGenePrimaryExtAcc("ENTREZ|419190|CHICK", "8");
//      Vector v = ds.searchBooksByProteinPrimaryExtId("9LS3", "8");
//      Vector v = ds.searchBooksByDefinition("phos", "9");
//      if (null != v) {
//          for (int i = 0; i < v.size(); i++) {
//              System.out.println(((Book)v.elementAt(i)).getId());
//          }
//      }

//    PANTHERTree theTREE = ds.getPANTHERTree("11", "PTHR10003");

//    Hashtable identTbl = ds.getIdentifiers("PTHR10000", "3, 12", "11");
//    if (null != identTbl) {
//        System.out.println("Here");
//    }

//    Hashtable clsInfo = ds.getAnnotations("PTHR10003", "11");
//    if (null != clsInfo) {
//        System.out.println("Here");
//    }

//    Vector v = (Vector)ds.getClsHierarchyData("11", true);
//    Classification root = Classification.parseClassificationData(v);
//    System.out.println("Num in vector is " + v.size() + " " + root.getName());
//    for (int i = 0; i < v.size(); i++)  {
//      System.out.println((String)v.elementAt(i));
//    }
//    
//    
//    Hashtable<String, Integer> accClsId = (Hashtable<String, Integer>) ds.getClsHierarchyData("11",false);
//      System.out.println("Num in hash is " + accClsId.size());
//    Enumeration <String> accEnum = accClsId.keys();
//    while (accEnum.hasMoreElements()) {
//        String acc = accEnum.nextElement();
//        System.out.println(acc + " " + accClsId.get(acc));
//    }
    
    
//    String treeStr[] = ds.getTree("PTHR10000", "11");
//    if (null != treeStr) {
//      for (int j = 0; j < treeStr.length; j++) {
//        System.out.print(treeStr[j]);
//      }
//    }
//    
//
    Vector<String[]> v =  ds.getAttrTableAndSfInfo("PTHR10000", "14",  "10001", "sim1001","bla bla");
    String s[] = v.get(ds.INDEX_ATTR_METHOD_ATTR_TBL);
    if (null != s) {
      for (int i = 0; i < s.length; i++) {
        System.out.print(s[i]);
      }
    }
    ds.getAnnotations("PTHR10000", "14");
    
    
//    System.out.println("Attr table for PTHR23422");
//    String s[] = ds.getAttrTable("PTHR23422", "8",  "4");
//    if (null != s) {
//      for (int i = 0; i < s.length; i++) {
//        System.out.print(s[i]);
//      }
//    }


//    Hashtable<String, Vector<Evidence>> goAnnotTbl = ds.getGOAnnotation("PTHR10000", "10");
//    System.out.println("Done");

    // getPreviousSfNameCat("2", "CF10001", "MF,BP");
    // String famName = getFamilyName("CF10000", "2");
    // System.out.println(famName);
    // System.out.println(famid);
    // initCatLookup();
    // if (null != clsTypeIdToCatId) {
    // Enumeration clsTypes = clsTypeIdToCatId.keys();
    // while (clsTypes.hasMoreElements()) {
    // String key = (String)clsTypes.nextElement();
    // Hashtable current = (Hashtable)clsTypeIdToCatId.get(key);
    // Enumeration accs = current.keys();
    // while (accs.hasMoreElements()) {
    // String accession = (String)accs.nextElement();
    // System.out.println(accession + " " + current.get(accession));
    // }
    // }
    // }
    // getClsLookup();
    // getClsHierarchyData("1");
    // String[] tree = DataServer.getTree("CF11775", "1");
    // if (null == tree) {
    // System.out.println("No tree has been returned");
    // }
    // else {
    // for (int i = 0; i < tree.length; i++) {
    // System.out.println(tree[i]);
    // }
    // }

  }

}
