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
package com.sri.panther.paintServer.database;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintCommon.util.QualifierDif;
import com.sri.panther.paintCommon.util.StringUtils;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.datamodel.ClassificationVersion;
import com.sri.panther.paintServer.logic.CategoryLogic;
import com.sri.panther.paintServer.util.ConfigFile;
import com.sri.panther.paintServer.util.ReleaseResources;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.SaveBookInfo;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 *
 * @author muruganu
 */
public class DataIO {

    private String dbStr;
    protected HashMap<String, ArrayList> clsIdToVersionRelease;
    private static final Logger log = Logger.getLogger(ReleaseResources.class);
    GOTermHelper goTermHelper = CategoryLogic.getInstance().getGOTermHelper();
    
    public static final String PANTHER_CLS_TYPE_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_PANTHER_CLS_TYPE_SID);
    public static final String CUR_CLASSIFICATION_VERSION_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);    

    protected static final String STR_TAB = "\t";
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

    private static final String TYPE_IDENTIFIER_DEFINITION = "3";
    private static final String TYPE_IDENTIFIER_ORTHOMCL = "12";
    private static final String TYPE_IDENTIFIERS = TYPE_IDENTIFIER_DEFINITION + COMMA_DELIM + TYPE_IDENTIFIER_ORTHOMCL;
    
    private static final String EVIDENCE_TYPE_ANNOT_PAINT_REF = "PAINT_REF";
    private static final String EVIDENCE_TYPE_ANNOT_PAINT_EXP = "PAINT_EXP";
    private static final String EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR = "PAINT_ANCESTOR";
    
    
    private static final String ANNOTATION_TYPE_GO_PAINT = "GO_PAINT";
    //private static final String ANNOTATION_TYPE_PAINT_PRUNED = "PAINT PRUNED";
    
    
    protected static final String MSG_ERROR_UNABLE_TO_RETRIEVE_USER_RANK_INFO = "Unable to retrieve user rank info";
    protected static final String MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED = "Unable to retrieve information, exception returned:  ";

    protected static final String MSG_INITIALIZING_CLS_INFO = "Going to initialize the cls to version release lookup";
    protected static final String MSG_INITIALIZING_EVIDENCE_INFO = "Initializing evidence info";
    protected static final String MSG_INITIALIZING_ANNOTATION_ID_INFO = "Initializing annotation id info";
    protected static final String MSG_INITIALIZING_QUALIFIER_ID_INFO = "Initializing qualifier id info";
    protected static final String MSG_INITIALIZING_CC_ID_INFO = "Initializing confidence code info";
    
    protected static final String MSG_CLASSIFICATION_ID_INFO_NOT_FOUND = "Classification information not found";
    protected static final String MSG_ERROR_IDENTIFIER_RETRIEVAL_NULL_FOUND = " - Error during identifier retrieval for protein ";
    protected static final String MSG_ERROR_INVALID_INFO_FOR_LOCKING_BOOKS = "Invalid information for locking books";    
    protected static final String MSG_ERROR_BOOK_ID_NOT_SPECIFIED = "Book id not specified";
  protected static final String MSG_ERROR_UNABLE_TO_VERIFY_USER = "Unable to verify user information";    
    protected static final String MSG_ERROR_USER_INFO_NOT_SPECIFIED = "User information not specified";
    protected static final String MSG_ERROR_USER_ID_NOT_SPECIFIED = "User id not specified";
  protected static final String MSG_ERROR_UNABLE_TO_LOCK_BOOKS_FOR_USER = "Unable to lock books for user";    
  protected static final String MSG_ERROR_USER_DOES_NOT_HAVE_PRIVILEGE_TO_LOCK_BOOKS = "User does not have privildege to lock books";
  protected static final String MSG_ERROR_SAVE_STATUS_NOT_SPECIFIED = "Save status not specified";
  protected static final String MSG_ERROR_INVALID_SAVE_STATUS_SPECIFIED = "Invalid save status specified";

  protected static final String MSG_ERROR_RETRIEVING_BOOKS_LOCKED_BY_USER = "Unable to retrieve books locked by user";

  protected static final String MSG_ERROR_UNLOCKING_BOOKS = "Unable to unlock the following books ";    
    public static final String MSG_SAVE_FAILED_PRUNED_IS_NULL = "Save operation failed - pruned list not specified";
    public static final String MSG_SAVE_FAILED_ANNOTATION_IS_NULL = "Save operation failed - annotation list not specified";
  public static final String MSG_SAVE_FAILED_BOOK_NOT_LOCKED_BY_USER = "Save Operation failed - Book not locked by user.";
  public static final String MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_BOOK_ID = "Save Operation failed - Unable to get book id";
  public static final String MSG_SAVE_FAILED_ERROR_ACCESSING_DATA_FROM_DB = "Error accessing data from database.";
  public static final String MSG_SAVE_FAILED_INVALID_UPL_SPECIFIED = "Invalid UPL version specified.";
  public static final String MSG_SAVE_FAILED_UPL_RELEASED = "UPL has already been released, changes can no-longer be saved.";
  protected static final String MSG_SUCCESS = Constant.STR_EMPTY;  
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
    public static final String MSG_SAVE_FAILED_UNABLE_TO_GET_CONNECTION_TO_DB = " save iperation failed - unable to connect to database";
    
    public static final String MSG_INSERTION_FAILED_EXCEPTION_RETURNED = " unable to insert records, exception returned:  ";      
    
  public static final String  PREPARED_RELEASE_CLAUSE =
    " and ((tblName.OBSOLESCENCE_DATE is null  and tblName.CREATION_DATE < to_date(?, 'yyyy-mm-dd, hh:mi:ss'))  or tblName.OBSOLESCENCE_DATE > to_date(?, 'yyyy-mm-dd, hh:mi:ss'))";
  public static final String  RELEASE_CLAUSE =
    " and ((tblName.OBSOLESCENCE_DATE is null and tblName.CREATION_DATE < to_date('%1', 'yyyy-mm-dd, hh:mi:ss')) or tblName.OBSOLESCENCE_DATE > to_date('%1', 'yyyy-mm-dd, hh:mi:ss'))";
  public static final String  NON_RELEASE_CLAUSE = " and tblName.OBSOLESCENCE_DATE is null";
  public static final String  PREPARED_CREATION_DATE_CLAUSE = " and tblName.CREATION_DATE < to_date(?, 'yyyy-mm-dd, hh:mi:ss') ";
  public static final String  CREATION_DATE_CLAUSE = " and tblName.CREATION_DATE < to_date('%1', 'yyyy-mm-dd, hh:mi:ss') ";    
    
    public static final String NODE_GENE = "select n.accession, g.primary_ext_acc, g.gene_symbol, g.gene_name from node n, GENE g, GENE_NODE gn where n.classification_version_sid = %1 and n.accession like '%2' and n.node_id = gn.node_id and gn.gene_id = g.gene_id";

    public static final String EVIDENCE_TYPE = "select * from evidence_type";
    public static final String ANNOTATION_TYPE = "select * from annotation_type";
    public static final String QUALIFIER_TYPE = "select * from qualifier";
    public static final String CONFIDENCE_CODE_TYPE = "select * from confidence_code";
    
    public static final String BOOK_ID = "select classification_id from classification c\n" +
                                            "where c.obsolescence_date is null and c.classification_version_sid = %1 and c.depth = %2 and c.accession = '%3'";
    
    public static final String CLS_VERSION
            = "select cv.VERSION, to_char(cv.RELEASE_DATE, 'yyyy-mm-dd, hh:mi:ss'), cv.CLASSIFICATION_VERSION_SID from CLASSIFICATION_VERSION cv, CLASSIFICATION_TYPE ct where ct.CLASSIFICATION_TYPE_SID = %1 and ct.CLASSIFICATION_TYPE_SID = cv.CLASSIFICATION_TYPE_SID  and cv.CLASSIFICATION_VERSION_SID = %2 ";

    public static final String PREPARED_TREE
            = "select TREE_TEXT from  CLASSIFICATION c, TREE_DETAIL td where c.CLASSIFICATION_VERSION_SID = ? and C.ACCESSION = ? and C.CLASSIFICATION_ID = td.CLASSIFICATION_ID";

    public static final String ANNOTATION_NODE_PUBLIC_ID_LOOKUP = "select n.accession, n.public_id, n.node_id from node n where n.accession like '%1:%' and n.classification_version_sid = %2 and n.obsolescence_date is null";

    public static final String IDENTIFIER_AN = "select n.accession, it.IDENTIFIER_TYPE_SID, i.NAME, it.name as identifier_type from node n, protein_node pn, protein p, identifier i, identifier_type it where n.classification_version_sid = %1 and n.accession like '%2' and n.node_id = pn.node_id and pn.protein_id = p.protein_id and p.PROTEIN_ID = i.primary_object_id and i.IDENTIFIER_TYPE_SID = it.IDENTIFIER_TYPE_SID and it.IDENTIFIER_TYPE_SID in (%3)";
    
    public static final String FULL_GO_ANNOTATIONS_PART_1 = "select pa.annotation_id, n.accession, clf.accession term, et.type, pe.evidence_id, pe.evidence, cc.confidence_code, q.qualifier\n" +
                                                                "from paint_evidence pe\n" +
                                                                "join paint_annotation pa\n" +
                                                                "on pe.annotation_id = pa.annotation_id\n" +
                                                                "join confidence_code cc\n" +
                                                                "on pe.confidence_code_sid = cc.confidence_code_sid\n" +
                                                                "join node n\n" +
                                                                "on pa.node_id = n.node_id\n" +
                                                                "join annotation_type ant\n" +
                                                                "on pa.annotation_type_id = ant.annotation_type_id and ant.annotation_type = 'GO_PAINT'\n" +
                                                                "join go_classification clf\n" +
                                                                "on pa.classification_id = clf.classification_id\n" +
                                                                "join evidence_type et\n" +
                                                                "on pe.evidence_type_sid = et.evidence_type_sid\n" +
                                                                "left join paint_annotation_qualifier pq\n" +
                                                                "on pa.annotation_id = pq.annotation_id\n" +
                                                                "left join qualifier q\n" +
                                                                "on pq.qualifier_id = q.qualifier_id\n" +
                                                                "where pe.obsolescence_date is null and pa.obsolescence_date is null and n.accession like '%1'and n.classification_version_sid = %2 "  ;

    public static final String FULL_GO_ANNOTATIONS_PART_2 = " union\n" +
                                                                "select * from go_aggregate\n" +
                                                                "where accession like '%1'";
    
    
    
    public static final String FULL_GO_ANNOTATIONS_TAKES_LONG_TO_RUN = "select gpa.annotation_id, n.accession, clf.accession term, et.type, gpe.evidence_id, gpe.evidence, cc.confidence_code, q.qualifier \n" +
                                                        "from (select annotation_id,confidence_code_sid,evidence_id, evidence,evidence_type_sid from go_evidence where obsolescence_date is null union select annotation_id,confidence_code_sid, evidence_id, evidence,evidence_type_sid from paint_evidence where obsolescence_date is null) gpe\n" +
                                                        "join (select annotation_id,node_id,annotation_type_id,classification_id from go_annotation where obsolescence_date is null union select annotation_id,node_id,annotation_type_id,classification_id from paint_annotation where obsolescence_date is null) gpa\n" +
                                                        "on gpe.annotation_id = gpa.annotation_id\n" +
                                                        "join confidence_code cc\n" +
                                                        "on gpe.confidence_code_sid = cc.confidence_code_sid\n" +
                                                        "join node n\n" +
                                                        "on gpa.node_id = n.node_id\n" +
                                                        "join annotation_type ant\n" +
                                                        "on gpa.annotation_type_id = ant.annotation_type_id and (ant.annotation_type = 'FULLGO' or ant.annotation_type = 'GO_PAINT')\n" +
                                                        "join go_classification clf\n" +
                                                        "on gpa.classification_id = clf.classification_id\n" +
                                                        "join evidence_type et\n" +
                                                        "on gpe.evidence_type_sid = et.evidence_type_sid\n" +
                                                        "\n" +
                                                        "\n" +
                                                        "left join (select * from go_annotation_qualifier union select * from paint_annotation_qualifier) gpq\n" +
                                                        "on gpa.annotation_id = gpq.annotation_id\n" +
                                                        "left join qualifier q\n" +
                                                        "on gpq.qualifier_id = q.qualifier_id\n" + 
                                                        "where n.accession like '%1'and n.classification_version_sid = %2 ";
    
    
    public static final String FULL_GO_ANNOTATIONS_ORIG = "select gpa.annotation_id, n.accession, clf.accession term, et.type, gpe.evidence_id,  gpe.evidence, cc.confidence_code, q.qualifier \n" +
                                                    "from (select * from go_evidence where obsolescence_date is null union select * from paint_evidence where obsolescence_date is null) gpe\n" +
                                                    "join (select * from go_annotation where obsolescence_date is null union select * from paint_annotation where obsolescence_date is null) gpa\n" +
                                                    "on gpe.annotation_id = gpa.annotation_id\n" +
                                                    "join confidence_code cc\n" +
                                                    "on gpe.confidence_code_sid = cc.confidence_code_sid\n" +
                                                    "join node n\n" +
                                                    "on gpa.node_id = n.node_id\n" +
                                                    "join annotation_type ant\n" +
                                                    "on gpa.annotation_type_id = ant.annotation_type_id and (ant.annotation_type = 'FULLGO' or ant.annotation_type = 'GO_PAINT')\n" +
                                                    "join go_classification clf\n" +
                                                    "on gpa.classification_id = clf.classification_id\n" +
                                                    "join evidence_type et\n" +
                                                    "on gpe.evidence_type_sid = et.evidence_type_sid\n" +
                                                    "\n" +
                                                    "\n" +
                                                    "left join (select * from go_annotation_qualifier union select * from paint_annotation_qualifier) gpq\n" +
                                                    "on gpa.annotation_id = gpq.annotation_id\n" +
                                                    "left join qualifier q\n" +
                                                    "on gpq.qualifier_id = q.qualifier_id\n" +
                                                    "where n.accession like '%1'and n.classification_version_sid = %2 ";
    
    public static final String FULL_PAINT_ANNOTATION = "select gpa.annotation_id, n.accession, n.public_id, clf.accession term, et.type, gpe.evidence_id, gpe.evidence, cc.confidence_code, q.qualifier\n" +
                                                        " from (select annotation_id,confidence_code_sid, evidence_id, evidence,evidence_type_sid from paint_evidence where obsolescence_date is null) gpe\n" +
                                                        " join (select annotation_id,node_id,annotation_type_id,classification_id from paint_annotation where obsolescence_date is null) gpa\n" +
                                                        "on gpe.annotation_id = gpa.annotation_id\n" +
                                                        "join confidence_code cc\n" +
                                                        "on gpe.confidence_code_sid = cc.confidence_code_sid\n" +
                                                        "join node n\n" +
                                                        "on gpa.node_id = n.node_id\n" +
                                                        "join annotation_type ant\n" +
                                                        "on gpa.annotation_type_id = ant.annotation_type_id and ant.annotation_type = 'GO_PAINT'\n" +
                                                        "join go_classification clf\n" +
                                                        "on gpa.classification_id = clf.classification_id\n" +
                                                        "join evidence_type et\n" +
                                                        "on gpe.evidence_type_sid = et.evidence_type_sid\n" +
                                                        "\n" +
                                                        "left join (select * from paint_annotation_qualifier) gpq\n" +
                                                        "on gpa.annotation_id = gpq.annotation_id\n" +
                                                        "left join qualifier q\n" +
                                                        "on gpq.qualifier_id = q.qualifier_id\n" +
                                                        "where n.accession like '%1'and n.classification_version_sid = %2 ";
    
    public static final String FAMILY_COMMENT = "select cmts.comment_id, cmts.remark from comments cmts, classification c\n" +
                                                "where c.classification_version_sid = %1 and c.depth = %2 and c.accession = '%3'\n" +
                                                " and c.classification_id = cmts.classification_id and c.obsolescence_date is null and cmts.obsolescence_date is null";
    
    
    public static final String  CLS_COMMENT_PRIMARY_OBJECT_ID = "select CLASSIFICATION_ID, REMARK from COMMENTS where (CLASSIFICATION_ID, CREATION_DATE) in (select c.CLASSIFICATION_ID, max(c.CREATION_DATE) from   (select classification_id  from classification c where c.classification_version_sid = %2 and c.depth = %3 and c.accession like '%4'  %1 ) e, COMMENTS c where e.classification_id = c.CLASSIFICATION_ID and c.PRIMARY_OBJECT_ID is null %1  group by c.CLASSIFICATION_ID )";

    public static final String GET_PRUNED = "select a.annotation_id, n.accession, n.node_id, n.public_id from node n, paint_annotation a\n" +
                                        "where n.classification_version_sid = %1\n" +
                                        "and n.accession like '%2'\n" +
                                        "and n.node_id = a.node_id\n" +
                                        "and a.annotation_type_id = %3\n" +
                                        "and a.classification_id is null";    
    
    
    public static final String GET_BOOKS_WITH_LEAF_GO_EVIDENCE = "select distinct substring(agg.accession, 1, 9) accession from go_aggregate agg, node n, node_type nt\n" +
                                                            "where n.accession = agg.accession\n" +
                                                            "and n.node_type_id = nt.node_type_id\n" +
                                                            "and nt.node_type='LEAF'\n" +
                                                            "and agg.confidence_code in ( %1 )";
    
//    public static final String GET_PRUNED = "select a.annotation_id, n.accession, n.node_id, n.public_id from node n, go_annotation a\n" +
//                                        "where n.classification_version_sid = %1\n" +
//                                        "and n.accession like '%2'\n" +
//                                        "and n.node_id = a.node_id\n" +
//                                        "and a.annotation_type_id = %3\n" +
//                                        "and a.classification_id is null";

  public static final String  FAMILY =
    "select * from CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = %2 and c.ACCESSION like '%3'";    
    public static final String GET_LIST_OF_BOOKS = " select c.CLASSIFICATION_ID, c.accession, c.name from classification c where  c.depth = %1 and c.CLASSIFICATION_VERSION_SID = %2 ";
    
    public static final String GET_STATUS_USER_INFO = " select c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name, u.group_name from classification c, curation_status cs, curation_status_type cst, users u where c.depth = %1 and c.CLASSIFICATION_VERSION_SID = %2 and c.CLASSIFICATION_ID  = cs.CLASSIFICATION_ID and cs.STATUS_TYPE_SID = cst.STATUS_TYPE_SID and cs.USER_ID = u.user_id ";
 
    
    public static final String USER_ID = "select USER_ID, PRIVILEGE_RANK from users where user_id = %1";

    public static final String UID_GENERATOR = "SELECT nextval('panther_uids') as num";
    public static final String  USER_PRIVILEGE = "select privilege_rank from users where user_id = ? ";
    public static final String OBSOLETE_COMMENT = "update comments set obsolescence_date = now(), obsoleted_by = %1 where comment_id = %2";
    public static final String PREPARED_UPDATE_FAMILY_NAME = "update classification set name = ? where classification_id = %1";
    public static final String INSERT_COMMENT = "insert into comments (comment_id, classification_id, remark, created_by, creation_date) values (%1, %2, ?, %3, now())";
    
    public static final String INSERT_ANNOTATION_INTO_PAINT_ANNOTATION_PRUNED = "insert into paint_annotation (annotation_id, node_id, annotation_type_id, created_by, creation_date) values(%1, %2, %3, %4, now())";
   
    
    public static final String OBSOLETE_ANNOTATION_FROM_PAINT_ANNOTATION = "update paint_annotation set obsolescence_date = now(), obsoleted_by = %1 where annotation_id = ? ";
    public static final String OBSOLETE_ANNOTATION_FROM_PAINT_EVIDENCE = "update paint_evidence set obsolescence_date = now(), obsoleted_by = %1 where annotation_id = ? ";
    public static final String DELETE_ANNOTATION_FROM_PAINT_ANNOTATION_QUALIFIER = "delete from paint_annotation_qualifier where annotation_id = %1";      
    
    public static final String INSERT_ANNOTATION_INTO_PAINT_ANNOTATION = "insert into paint_annotation (annotation_id, node_id, classification_id, annotation_type_id, created_by, creation_date) values(%1, %2, %3, %4, %5, now())";
    public static final String INSERT_PAINT_ANNOTATION_QUALIFIER = "insert into paint_annotation_qualifier(annotation_qualifier_id, annotation_id, qualifier_id) values (%1, %2, %3)";
    
    public static final String INSERT_ANNOTATION_INTO_PAINT_EVIDENCE = "insert into paint_evidence (evidence_id, evidence_type_sid, evidence, creation_date, created_by, confidence_code_sid, annotation_id) values (%1, %2, ?, now(), %3, %4, %5)";
    
    public static final String PREPARED_BOOK_LOCK = "insert into curation_status (CURATION_STATUS_ID, STATUS_TYPE_SID, CLASSIFICATION_ID, USER_ID, CREATION_DATE) values(?, ?, ?, ?, now())";
    public static final String PREPARED_BOOK_UNLOCK = "delete from CURATION_STATUS where CLASSIFICATION_ID = ? AND USER_ID = ? and STATUS_TYPE_SID = ?";
    public static final String PREPARED_CLSID_FOR_BOOK_LOCKED_BY_USER = "select cs.CLASSIFICATION_ID from CURATION_STATUS cs, CLASSIFICATION c where cs.USER_id = ? and cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.CLASSIFICATION_VERSION_SID = ? and c.ACCESSION = ? ";
  
    public static final String PREPARED_CURATION_STATUS_ID = "select CURATION_STATUS_ID from CURATION_STATUS where CLASSIFICATION_ID = ? and USER_ID = ? and STATUS_TYPE_SID = ?";
    public static final String PREPARED_INSERT_CURATION_STATUS = "insert into curation_status (CURATION_STATUS_ID, CLASSIFICATION_ID, USER_ID, STATUS_TYPE_SID, CREATION_DATE) values (?, ?, ?, ?, now()) ";
    public static final String  PREPARED_UNLOCKING_BOOK_LIST = "select c.ACCESSION, c.NAME, cs.STATUS_TYPE_SID, u.* from CURATION_STATUS cs, CLASSIFICATION c, users u  where c.CLASSIFICATION_VERSION_SID = ? and cs.USER_ID = ?  and cs.user_id = u.user_id AND cs.STATUS_TYPE_SID = ? AND c.DEPTH = ?  AND cs.CLASSIFICATION_ID = c.CLASSIFICATION_ID AND c.OBSOLESCENCE_DATE IS NULL order by c.accession";

    public static final String PREPARED_UPDATE_CURATION_STATUS = "update curation_status set creation_date = now() where curation_status_id = ?";

    public static final String  PREPARED_USER_VALIDATION = "select * from USERS u where u.LOGIN_NAME = ? and u.PASSWORD = ?";


  public static final String  PREPARED_BOOK_LIST =
    "select c.ACCESSION, c.NAME from CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = ? and c.DEPTH = ?";
  public static final String  PREPARED_LOCKING_BOOK_LIST =
    "select c.ACCESSION, c.NAME from CLASSIFICATION_VERSION cv, CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = ? and c.OBSOLESCENCE_DATE is null and c.CLASSIFICATION_VERSION_SID = cv.CLASSIFICATION_VERSION_SID and c.DEPTH = ? and cv.RELEASE_DATE is null and c.CLASSIFICATION_ID not in (select cs.CLASSIFICATION_ID from CURATION_STATUS cs where cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID is not null ) ";
  public static final String  PREPARED_CLSID_FOR_BOOK_USER_LOCK =
    "select c.CLASSIFICATION_ID from CLASSIFICATION_VERSION cv, CLASSIFICATION c where cv.CLASSIFICATION_VERSION_SID = ? and cv.RELEASE_DATE is null and cv.CLASSIFICATION_VERSION_SID = c.CLASSIFICATION_VERSION_SID and c.OBSOLESCENCE_DATE is null and c.ACCESSION = ? and c.CLASSIFICATION_ID not in (select cs.CLASSIFICATION_ID from CURATION_STATUS cs where cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID is not null ) ";
  public static final String  PREPARED_CLSIDS_FOR_BOOKS_USER_LOCK =
    "select c.CLASSIFICATION_ID from CLASSIFICATION_VERSION cv, CLASSIFICATION c where cv.CLASSIFICATION_VERSION_SID = ? and cv.RELEASE_DATE is null and cv.CLASSIFICATION_VERSION_SID = c.CLASSIFICATION_VERSION_SID and c.OBSOLESCENCE_DATE is null and c.ACCESSION in (%1) and c.CLASSIFICATION_ID not in (select cs.CLASSIFICATION_ID from CURATION_STATUS cs where cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID is not null ) ";
    
    
    
    
    
    public static final String SEARCH_NODE_DEFINITION = "select n.accession as accession from identifier i, protein p,  protein_node pn, node n, node_type nt where lower(i.name)  like '%1' and i.IDENTIFIER_TYPE_SID = 3 and i.primary_object_id = p.protein_id and p.protein_id = pn.protein_id and pn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
    public static final String SEARCH_NODE_PROTEIN_PRIMARY_EXT_ID = "select n.accession as accession from protein p,  protein_node pn, node n, node_type nt where p.PRIMARY_EXT_ID like '%1' and p.protein_id = pn.protein_id and pn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
    public static final String SEARCH_NODE_GENE_SYMBOL = "select n.accession as  accession from gene g, gene_node gn, node n, node_type nt where lower(g.gene_symbol) like '%1' and g.GENE_ID = gn.GENE_ID  and gn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
  
    public static final String SEARCH_NODE_GENE_PRIMARY_EXT_ACC = "select n.accession as accession from gene g, gene_node gn, node n, node_type nt where g.PRIMARY_EXT_ACC like '%1' and g.GENE_ID = gn.GENE_ID  and gn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
    
    
    protected static final String QUERY_STR_RELEASE_CLAUSE_TBL = "tblName";
    protected static final String QUERY_STR_RELEASE_CLAUSE_VAR_G = "g";
    protected static final String QUERY_PARAMETER_1 = "%1";
    protected static final String QUERY_PARAMETER_2 = "%2";
    protected static final String QUERY_PARAMETER_3 = "%3";
    protected static final String QUERY_PARAMETER_4 = "%4";
    protected static final String QUERY_PARAMETER_5 = "%5";    
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
    protected static final String COLUMN_NAME_COMMENT_ID = "comment_id";
    protected static final String COLUMN_NAME_CLASSIFICATION_ID = "classification_id";
    protected static final String COLUMN_NAME_GO_ACCESSION = "GO_ACCESSION";
    protected static final String COLUMN_NAME_GO_NAME = "GO_NAME";
    protected static final String COLUMN_NAME_GROUP_NAME = "GROUP_NAME";
    protected static final String COLUMN_NAME_NAME = "NAME";
    protected static final String COLUMN_NAME_NODE_ID = "NODE_ID";
    protected static final String COLUMN_NAME_NODE_TYPE = "NODE_TYPE";
    protected static final String COLUMN_NAME_EVENT_TYPE = "EVENT_TYPE";
    protected static final String COLUMN_NAME_EVIDENCE = "evidence";
    protected static final String COLUMN_NAME_EVIDENCE_ID = "evidence_id";
    protected static final String COLUMN_NAME_EVIDENCE_TYPE_SID = "evidence_type_sid";
    protected static final String COLUMN_TYPE = "type";
    protected static final String COLUMN_USER_ID = "user_id";
    //protected static final String COLUMN_TYPE = "TERM_NAME";        //"DECODE(ctt.TERM_NAME, 'molecular_function', 'F','cellular_component', 'C','biological_process','P')";
    protected static final String COLUMN_CONFIDENCE_CODE = "CONFIDENCE_CODE";
    protected static final String COLUMN_PRIMARY_EXT_ID = "PRIMARY_EXT_ID";
    protected static final String COLUMN_NAME_PRIMARY_EXT_ACC = "PRIMARY_EXT_ACC";
    protected static final String COLUMN_NAME_SOURCE_ID = "SOURCE_ID";
    protected static final String COLUMN_NAME_TERM = "TERM";
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
    protected static final String COMUMN_NAME_REMARK = "REMARK";
    protected static final String COLUMN_NAME_RELATIONSHIP = "RELATIONSHIP";
    protected static final String COLUMN_NAME_CONFIDENCE_CODE = "CONFIDENCE_CODE";
    protected static final String COLUMN_NAME_CONFIDENCE_CODE_SID = "CONFIDENCE_CODE_SID";    
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

    protected static final String REPLACE_STR_PERCENT_1 = "%1";
    protected static final String REPLACE_STR_PERCENT_2 = "%2";
    protected static final String REPLACE_STR_PERCENT_3 = "%3";

    public static final String RELEASE_CLAUSE_TBL_NAME = "tblName";
    protected static final String TABLE_NAME_a = "a";
    protected static final String TABLE_NAME_c = "c";
    protected static final String TABLE_NAME_c1 = "c1";
    protected static final String TABLE_NAME_c2 = "c2";
    protected static final String TABLE_NAME_clf = "clf";    
    protected static final String TABLE_NAME_cp = "cp";
    protected static final String TABLE_NAME_cr = "cr";
    protected static final String TABLE_NAME_e = "e";
    protected static final String TABLE_NAME_g = "g";
    protected static final String TABLE_NAME_ga = "ga";
    protected static final String TABLE_NAME_ge = "ge";
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
    
    protected static final String CURATION_STATUS_CHECKOUT = "panther_check_out";
    protected static final String CURATION_STATUS_NOT_CURATED = "panther_not_curated";
    protected static final String CURATION_STATUS_AUTOMATICALLY_CURATED = "panther_automatically_curated";
    protected static final String CURATION_STATUS_MANUALLY_CURATED = "panther_manually_curated";
    protected static final String CURATION_STATUS_REVIEWED = "panther_curation_reviewed";
    protected static final String CURATION_STATUS_QAED = "panther_curation_QAed";
    protected static final String CURATION_STATUS_PARTIALLY_CURATED = "panther_partially_curated";

    protected static final String LEVEL_FAMILY = "_famLevel";
    protected static final String LEVEL_SUBFAMILY = "_subfamLevel";
    protected static final String TYPE_PRUNED = "_pruned";

    protected static final String RANK_PROP_GO_CURATOR = "panther_curator_rank";
    public static final String RANK_CURATOR_GO = ConfigFile.getProperty(RANK_PROP_GO_CURATOR);
  
    // Required for saving PAINT annotations back to database
    private Hashtable<String, String> EVIDENCE_TYPE_SID_LOOKUP = null;
    private Hashtable<String, String> ANNOTATION_TYPE_ID_LOOKUP = null;
    private Hashtable<String, String> QUALIFIER_TYPE_ID_LOOKUP = null;
    private Hashtable<String, String> CONFIDENCE_CODE_TYPE_ID_LOOKUP = null;
    
  
    protected static final String RANK_PANTHER_CURATOR = "panther_curator_rank";
  
    private final Hashtable<String, String> EVIDENCE_TYPE_TO_SID_LOOKUP = new Hashtable<String, String>();      
    private final Hashtable<String, String> ANNOTATION_TYPE_TO_ID_LOOKUP = new Hashtable<String, String>();
    private final Hashtable<String, String> QUALIFIER_TYPE_TO_ID_LOOKUP = new Hashtable<String, String>();
    private final Hashtable<String, String> CONFIDENCE_CODE_TYPE_TO_ID_LOOKUP = new Hashtable<String, String>();

    public DataIO(String dbStr) {
        this.dbStr = dbStr;

    }

    protected Connection getConnection() throws SQLException {
        return DBConnectionPool.getConnection(dbStr);
    }
    
    public Hashtable<String, String> initConfidenceCodeLookup() {
        Hashtable<String, String> ccLookup = new Hashtable<String, String>();
        
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            log.debug(MSG_INITIALIZING_CC_ID_INFO);
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();
            String query = CONFIDENCE_CODE_TYPE;

            rst = stmt.executeQuery(query);

            
            while (rst.next()) {
                int id = rst.getInt(COLUMN_NAME_CONFIDENCE_CODE_SID);
                String code = rst.getString(COLUMN_NAME_CONFIDENCE_CODE);
                ccLookup.put(Integer.toString(id), code);
                CONFIDENCE_CODE_TYPE_TO_ID_LOOKUP.put(code, Integer.toString(id));

            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about confidence code type, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            if (null != con) {
                ReleaseResources.releaseDBResources(rst, stmt, con);
            }
        }

        return ccLookup;         
        
    }

    
    public Hashtable<String, String> initQualifierIdLookup() {
        Hashtable<String, String> qualifierLookup = new Hashtable<String, String>();
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            log.debug(MSG_INITIALIZING_QUALIFIER_ID_INFO);
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();
            String query = QUALIFIER_TYPE;

            rst = stmt.executeQuery(query);

            
            while (rst.next()) {
                int id = rst.getInt(COLUMN_NAME_QUALIFIER_ID);
                String type = rst.getString(COLUMN_NAME_QUALIFIER);
                qualifierLookup.put(Integer.toString(id), type);
                QUALIFIER_TYPE_TO_ID_LOOKUP.put(type, Integer.toString(id));

            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about annotation type, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            if (null != con) {
                ReleaseResources.releaseDBResources(rst, stmt, con);
            }
        }

        return qualifierLookup;         
        
    }
    
    public Hashtable<String, String> initAnnotationIdLookup() {
        Hashtable<String, String> annotationLookup = new Hashtable<String, String>();
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            log.debug(MSG_INITIALIZING_ANNOTATION_ID_INFO);
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();
            String query = ANNOTATION_TYPE;

            rst = stmt.executeQuery(query);

            
            while (rst.next()) {
                int typeId = rst.getInt(COLUMN_NAME_ANNOTATION_TYPE_ID);
                String type = rst.getString(COLUMN_NAME_ANNOTATION_TYPE);
                annotationLookup.put(Integer.toString(typeId), type);
                ANNOTATION_TYPE_TO_ID_LOOKUP.put(type, Integer.toString(typeId));

            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about annotation type, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            if (null != con) {
                ReleaseResources.releaseDBResources(rst, stmt, con);
            }
        }

        return annotationLookup;         
    }
    
    public Hashtable<String, String> initEvidenceLookup() {
        Hashtable<String, String> evidenceLookup = new Hashtable<String, String>();
        
       Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            log.debug(MSG_INITIALIZING_EVIDENCE_INFO);
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();
            String query = EVIDENCE_TYPE;

            rst = stmt.executeQuery(query);

            
            while (rst.next()) {
                int type = rst.getInt(COLUMN_NAME_EVIDENCE_TYPE_SID);
                String value = rst.getString(COLUMN_TYPE);
                evidenceLookup.put(Integer.toString(type), value);
                //if (true == EVIDENCE_TYPE_ANNOT_PAINT_REF.equals(value) || true == EVIDENCE_TYPE_ANNOT_PAINT_EXP.equals(value) || true == EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR.equals(value)) {
                EVIDENCE_TYPE_TO_SID_LOOKUP.put(value, Integer.toString(type));
                //}
            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about evidence type, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            if (null != con) {
                ReleaseResources.releaseDBResources(rst, stmt, con);
            }
        }

        return evidenceLookup;
    }
    
    public ClassificationVersion getCurVersionInfo() {
        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }
        if (null == clsIdToVersionRelease) {
            System.out.println("Unable to retrieve classification information");
            return null;
        }
        ClassificationVersion cv = new ClassificationVersion();
        ArrayList infoList = clsIdToVersionRelease.get(CUR_CLASSIFICATION_VERSION_SID);
        cv.setClsId(CUR_CLASSIFICATION_VERSION_SID);
        if (null != infoList && infoList.size() >= 1) {
            cv.setName((String)infoList.get(0));
        }
        if (infoList.size() >= 2) {
            cv.setReleaseDate((String)infoList.get(1));
        }
        return cv;
    }

    public synchronized void initClsLookup() {

        if (null != clsIdToVersionRelease) {
            return;
        }
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            log.debug(MSG_INITIALIZING_CLS_INFO);
            con = getConnection();
            if (null == con) {
                return;
            }
            stmt = con.createStatement();
            String query = CLS_VERSION;

            query = Utils.replace(query, QUERY_PARAMETER_1, PANTHER_CLS_TYPE_SID);
            query = Utils.replace(query, QUERY_PARAMETER_2, CUR_CLASSIFICATION_VERSION_SID);

            // System.out.println(query);
            rst = stmt.executeQuery(query);

            
            if (rst.next()) {
                if (null == clsIdToVersionRelease) {
                    clsIdToVersionRelease = new HashMap<String, ArrayList>();
                }
                ArrayList clsInfo = new ArrayList(2);

                clsInfo.add(rst.getString(1));
                String d = rst.getString(2);

                if (null != d) {
                    clsInfo.add(d);
                } else {
                    clsInfo.add(null);
                }
                clsIdToVersionRelease.put(Integer.toString(rst.getInt(3)), clsInfo);
            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            if (null != con) {
                ReleaseResources.releaseDBResources(rst, stmt, con);
            }
        }
    }

    public String addVersionReleaseClause(String uplVersion, String query, String tableName) {
        ArrayList clsInfo = (ArrayList) clsIdToVersionRelease.get(uplVersion);
        if (null == clsInfo) {
            return null;
        }
        String dateStr = (String) clsInfo.get(1);

        if (null != dateStr) {
            String non_release_caluse = Utils.replace(RELEASE_CLAUSE, RELEASE_CLAUSE_TBL_NAME, tableName);
            non_release_caluse = Utils.replace(non_release_caluse, REPLACE_STR_PERCENT_1, dateStr);
            query += non_release_caluse;
        } else {
            query += Utils.replace(NON_RELEASE_CLAUSE, RELEASE_CLAUSE_TBL_NAME, tableName);
        }
        return query;
    }

    public void getGeneInfo(String book, String uplVersion, HashMap<String, Node> nodeLookup) {
        if (null == book || null == uplVersion || null == nodeLookup) {
            return;
        }

        Connection con = null;
        Statement gstmt = null;
        ResultSet grslt = null;
        try {
            con = getConnection();
            if (null == con) {
                return;
            }

            // Make sure release dates can be retrieved, else return null
            initClsLookup();
            if (null == clsIdToVersionRelease) {
                return;
            }
            String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_gn);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_g);
            query = NODE_GENE + query;
//            System.out.println(query);

            query = Utils.replace(query, QUERY_PARAMETER_1, uplVersion);
            query = Utils.replace(query, QUERY_PARAMETER_2, book + QUERY_WILDCARD);

            gstmt = con.createStatement();

            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of gene execution " + df.format(new java.util.Date(System.currentTimeMillis())));

            grslt = gstmt.executeQuery(query);

            System.out.println("end of gene execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            grslt.setFetchSize(100);
            while (grslt.next()) {

                // annotation id to gene information
                String annotId = grslt.getString(COLUMN_NAME_ACCESSION);
                String geneIdentifier = grslt.getString(COLUMN_NAME_GENE_PRIMARY_EXT_ACC);
                geneIdentifier = geneIdentifier; //formatGeneIdentifer(geneIdentifier);
                String geneSymbol = grslt.getString(COLUMN_NAME_GENE_SYMBOL);
                String geneName = grslt.getString(COLUMN_NAME_GENE_NAME);
                if (null == annotId || 0 == annotId.length()) {
                    continue;
                }
                Node nodeInfo = nodeLookup.get(annotId);
                if (null == nodeInfo) {
                    nodeInfo = new Node();
                    nodeLookup.put(annotId, nodeInfo);
                }
                NodeStaticInfo staticInfo = nodeInfo.getStaticInfo();
                if (null == staticInfo) {
                    staticInfo = new NodeStaticInfo();
                    nodeInfo.setStaticInfo(staticInfo);
                }
                
                staticInfo.setNodeAcc(annotId);

                if (null != geneIdentifier) {
                    staticInfo.setLongGeneName(geneIdentifier);

                }
                if (null != geneSymbol) {
                    staticInfo.addGeneSymbol(geneSymbol);
                }
                if (null != geneName) {
                    staticInfo.addGeneName(geneName);
                }
            }
        } catch (SQLException se) {
            System.out.println("Unable to retrieve gene information from database, exception "
                    + se.getMessage() + " has been returned.");
        } finally {
            ReleaseResources.releaseDBResources(grslt, gstmt, con);
        }
    }
    
    public void getFullPAINTAnnotations(String book, String uplVersion, HashMap<String, Node> nodeLookup) {
        if (null == book || null == uplVersion || null == nodeLookup) {
            return;
        }
         Connection con = null;
        Statement gstmt = null;
        ResultSet grslt = null;
        try {
            con = getConnection();
            if (null == con) {
                return;
            }

            // Make sure release dates can be retrieved, else return null
            initClsLookup();
            if (null == clsIdToVersionRelease) {
                return;
            }
            String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_clf);
            query = FULL_PAINT_ANNOTATION + query;

            query = Utils.replace(query, QUERY_PARAMETER_1, book + QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD);
            query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
            System.out.println(query);

            gstmt = con.createStatement();
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of full GO PAINT execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            grslt = gstmt.executeQuery(query);
            System.out.println("end of full GO PAINT execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            HashMap<String, Annotation> annotLookup = new HashMap<String, Annotation>();
            HashMap<Node, HashSet<Annotation>> nodeAnnotLookup = new HashMap<Node, HashSet<Annotation>>();
            grslt.setFetchSize(1000);
            while (grslt.next()) {
                String annotationId = Long.toString(grslt.getLong(COLUMN_NAME_ANNOTATION_ID));
                String accession = grslt.getString(COLUMN_NAME_ACCESSION);
                String publicId = grslt.getString(COLUMN_NAME_PUBLIC_ID);
                String term = grslt.getString(COLUMN_NAME_TERM);
                String evidence_type = grslt.getString(COLUMN_TYPE);
//                String evidence = grslt.getString(COLUMN_NAME_EVIDENCE);
//                int evidenceId = grslt.getInt(COLUMN_NAME_EVIDENCE_ID);
                String cc = grslt.getString(COLUMN_NAME_CONFIDENCE_CODE);

                
                if (true == Evidence.CODE_IKR.equals(cc) || true == Evidence.CODE_IRD.equals(cc)) {
                    System.out.println("Doing ikr and ird");
                }
                String qualifier = grslt.getString(COLUMN_NAME_QUALIFIER);
                if (null != qualifier) {
                    qualifier = qualifier.trim();
                    if (0 == qualifier.length()) {
                        qualifier = null;
                    }
                }
                
                Annotation a = annotLookup.get(annotationId);
                if (null == a) {
                    a = new Annotation();
                    a.setAnnotationId(annotationId);
                    a.setAnnotStoredInDb(true);
                    annotLookup.put(annotationId, a);
                }
                a.setGoTerm(term);
                if (null != qualifier) {
                    Qualifier q = new Qualifier();
                    q.setText(qualifier);
                    a.addQualifier(q);
                }
                Evidence e = a.getEvidence();
                if (null == e) {
                    e = new Evidence();
                    a.setEvidence(e);
                    ArrayList<DBReference> paintRefList = new ArrayList<DBReference>(1);
                    e.setDbReferenceList(paintRefList);
                    DBReference paintRef = new DBReference();
                    paintRef.setEvidenceType(Utils.PAINT_REF);
                    paintRef.setEvidenceValue(Utils.getPaintEvidenceAcc(book));
                    paintRefList.add(paintRef);                    
                }
                
                e.setEvidenceCode(cc);
//                e.setEvidenceId(Integer.toString(evidenceId));        ///NO evidence id is different for each evidence


                Node n = nodeLookup.get(accession);
                if (null == n) {
                    n = new Node();
                    nodeLookup.put(accession, n);
                }
                NodeStaticInfo nsi = n.getStaticInfo();
                if (nsi == null) {
                    nsi = new NodeStaticInfo();
                    n.setStaticInfo(nsi);
                    nsi.setNodeAcc(accession);
                    nsi.setPublicId(publicId);
                }
                NodeVariableInfo nvi = n.getVariableInfo();
                if (nvi == null) {
                    nvi = new NodeVariableInfo();
                    n.setVariableInfo(nvi);
                }
                a.getAnnotationDetail().setAnnotatedNode(n);
                ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
                if (null == goAnnotList) {
                    goAnnotList = new ArrayList<Annotation>();
                    nvi.setGoAnnotationList(goAnnotList);
                }
                if (false == goAnnotList.contains(a)) {
                    goAnnotList.add(a);
                }
                HashSet<Annotation> annotList = nodeAnnotLookup.get(n);
                if (null == annotList) {
                    annotList = new HashSet<Annotation>();
                    nodeAnnotLookup.put(n, annotList);
                }
                if (false == annotList.contains(a)) {
                    annotList.add(a);
                }
            }


            

        } catch (SQLException se) {
            System.out.println("Unable to retrieve full go annotation information from database, exception "
                    + se.getMessage() + " has been returned.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            ReleaseResources.releaseDBResources(grslt, gstmt, con);
        }
       
    }
    
    public HashSet<String> getBooksWithExpEvdnceForLeaves() {
       Connection con = null;
        Statement gstmt = null;
        ResultSet grslt = null;
        HashSet<String> books = new HashSet<String>();
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }

            HashSet<String> experimental = Evidence.getExperimental();
            if (null == experimental) {
                return null;
            }
            String expStr = Utils.listToString(new Vector(experimental), QUOTE, STR_COMMA);
            String query = GET_BOOKS_WITH_LEAF_GO_EVIDENCE.replace(QUERY_PARAMETER_1, expStr);
            gstmt = con.createStatement();
            grslt = gstmt.executeQuery(query);
            while (grslt.next()) {
                String accession = grslt.getString(COLUMN_NAME_ACCESSION);
                books.add(accession);
            }
            return books;
            
        } catch (SQLException se) {
            System.out.println("Unable to retrieve books with experimental evidence on leaves, exception "
                    + se.getMessage() + " has been returned.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            ReleaseResources.releaseDBResources(grslt, gstmt, con);
        }
        
        return null;
    } 
    
    public void getFullGOAnnotations(String book, String uplVersion, HashMap<String, Node> nodeLookup) {
        HashMap<String, Node> nodeIdToNodeLookup = new HashMap<String, Node>();
        for (Node n: nodeLookup.values()) {
            nodeIdToNodeLookup.put(n.getStaticInfo().getNodeId(), n);
        }
        if (null == book || null == uplVersion || null == nodeLookup) {
            return;
        }

        Connection con = null;
        Statement gstmt = null;
        ResultSet grslt = null;
        try {
            con = getConnection();
            if (null == con) {
                return;
            }

            // Make sure release dates can be retrieved, else return null
            initClsLookup();
            if (null == clsIdToVersionRelease) {
                return;
            }
            String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_clf);
            query = FULL_GO_ANNOTATIONS_PART_1 + query + FULL_GO_ANNOTATIONS_PART_2;

            query = Utils.replace(query, QUERY_PARAMETER_1, book + QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD);
            query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
            System.out.println(query);

            gstmt = con.createStatement();
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of full GO annotation execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            grslt = gstmt.executeQuery(query);
            System.out.println("end of full GO annotation execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            HashMap<String, Annotation> annotLookup = new HashMap<String, Annotation>();
            HashMap<Node, HashSet<Annotation>> nodeAnnotLookup = new HashMap<Node, HashSet<Annotation>>();
            grslt.setFetchSize(1000);
            while (grslt.next()) {
                String annotationId = Long.toString(grslt.getLong(COLUMN_NAME_ANNOTATION_ID));
                String accession = grslt.getString(COLUMN_NAME_ACCESSION);
//                if ("PTHR43885:AN27".equals(accession)) {
//                    System.out.println("Here");
//                }
                String term = grslt.getString(COLUMN_NAME_TERM);
                String evidence_type = grslt.getString(COLUMN_TYPE);
                String evidence = grslt.getString(COLUMN_NAME_EVIDENCE);
//                int evidenceId = grslt.getInt(COLUMN_NAME_EVIDENCE_ID);
                String cc = grslt.getString(COLUMN_NAME_CONFIDENCE_CODE);
                // For now just do not handle non paint and non-experimental
                Evidence testEv = new Evidence();
                testEv.setEvidenceCode(cc);

                if (null != cc && (false == testEv.isPaint() && false == testEv.isExperimental())) {
                    continue;
                }                
                if (true == Evidence.CODE_IKR.equals(cc) || true == Evidence.CODE_IRD.equals(cc)) {
                    System.out.println("Doing ikr and ird");
                }
                String qualifier = grslt.getString(COLUMN_NAME_QUALIFIER);
                if (null != qualifier) {
                    qualifier = qualifier.trim();
                    if (0 == qualifier.length()) {
                        qualifier = null;
                    }
                }
                
                Annotation a = annotLookup.get(annotationId);
                if (null == a) {
                    a = new Annotation();
                    a.setAnnotationId(annotationId);
                    a.setAnnotStoredInDb(true);
                    annotLookup.put(annotationId, a);
                }
                a.setGoTerm(term);
                if (null != qualifier) {
                    Qualifier q = new Qualifier();
                    q.setText(qualifier);
                    a.addQualifier(q);
                }
                Evidence e = a.getEvidence();
                if (null == e) {
                    e = new Evidence();
                    a.setEvidence(e);
                }
                e.setEvidenceCode(cc);
//                e.setEvidenceId(Integer.toString(evidenceId));        ///NO evidence id is different for each evidence
                if (true == evidence_type.equals(EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR) || true == evidence_type.equals(EVIDENCE_TYPE_ANNOT_PAINT_EXP)) {
                    
                    ArrayList<DBReference> paintRefList = e.getDbReferenceList();
                    if (null == paintRefList) {
                        paintRefList = new ArrayList<DBReference>(1);
                        e.setDbReferenceList(paintRefList);
                        DBReference paintRef = new DBReference();
                        paintRef.setEvidenceType(Utils.PAINT_REF);
                        paintRef.setEvidenceValue(Utils.getPaintEvidenceAcc(book));
                        paintRefList.add(paintRef);
                    }
                    String ancestorAnnotId = evidence;
                    
                    Annotation ancestorAnnot = annotLookup.get(ancestorAnnotId);
                    if (null == ancestorAnnot) {
                        ancestorAnnot = new Annotation();
                        ancestorAnnot.setAnnotationId(ancestorAnnotId);
                        annotLookup.put(ancestorAnnotId, ancestorAnnot);
                    }
                    a.getAnnotationDetail().addWith(ancestorAnnot);
                }
                else if (true == evidence_type.equals(EVIDENCE_TYPE_ANNOT_PAINT_REF)) {
                    ArrayList<DBReference> paintRefList = e.getDbReferenceList();
                    if (null == paintRefList) {
                        paintRefList = new ArrayList<DBReference>(1);
                        e.setDbReferenceList(paintRefList);
                        DBReference paintRef = new DBReference();
                        paintRef.setEvidenceType(Utils.PAINT_REF);
                        paintRef.setEvidenceValue(Utils.getPaintEvidenceAcc(book));
                        paintRefList.add(paintRef);                        
                    }                    
                    Node n = null;
                    for (Node node: nodeLookup.values()) {
                        if (evidence.equals(node.getStaticInfo().getNodeId())) {
                            n = node;
                            break;
                        }
                    }
                    if (null == n) {
                        System.out.println("Did not find node id " + evidence + " for annnotation id " + annotationId + " going to create one");
                        n = new Node();
                        NodeStaticInfo nsi = new NodeStaticInfo();
                        nsi.setNodeId(evidence);
                    }
                    a.getAnnotationDetail().addNode(n);
                }
                else {
                    DBReference dbRef = new DBReference();
                    dbRef.setEvidenceType(evidence_type);
                    dbRef.setEvidenceValue(evidence);
                    e.addWith(dbRef);
                    a.getAnnotationDetail().addOther(dbRef);
                }
                Node n = nodeLookup.get(accession);
                if (null == n) {
                    n = new Node();
                    nodeLookup.put(accession, n);
                }
                NodeStaticInfo nsi = n.getStaticInfo();
                if (nsi == null) {
                    nsi = new NodeStaticInfo();
                    n.setStaticInfo(nsi);
                    nsi.setNodeAcc(accession);
                }
                NodeVariableInfo nvi = n.getVariableInfo();
                if (nvi == null) {
                    nvi = new NodeVariableInfo();
                    n.setVariableInfo(nvi);
                }
                a.getAnnotationDetail().setAnnotatedNode(n);
                ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
                if (null == goAnnotList) {
                    goAnnotList = new ArrayList<Annotation>();
                    nvi.setGoAnnotationList(goAnnotList);
                }
                if (false == goAnnotList.contains(a)) {
                    goAnnotList.add(a);
                }
                HashSet<Annotation> annotList = nodeAnnotLookup.get(n);
                if (null == annotList) {
                    annotList = new HashSet<Annotation>();
                    nodeAnnotLookup.put(n, annotList);
                }
                if (false == annotList.contains(a)) {
                    annotList.add(a);
                }
            }
            
            // Add the annotations to the node
            HashSet<Annotation> irkIrdSet = new HashSet<Annotation>();            
            for (Node n: nodeAnnotLookup.keySet()) {
                HashSet<Annotation> annotSet = nodeAnnotLookup.get(n);
                HashSet<Annotation> invalidAnnot = new HashSet<Annotation>();

                for (Annotation a: annotSet) {
//                    System.out.println("Processing annotation " + a.getAnnotationId());
                    //n.getVariableInfo().addGOAnnotation(a);
//                    a.getAnnotationDetail().setAnnotatedNode(n);
                    String code = a.getEvidence().getEvidenceCode();
                    if (Evidence.CODE_IBD.equals(code) || Evidence.CODE_IBA.equals(code)) {
                        // Determine which 'withs' give us our qualifiers
                        HashSet<Qualifier> qSet = a.getQualifierSet();
                        if (null == qSet || qSet.isEmpty()) {
                            a.setQualifierSet(null);
                            continue;
                        }
                        
                        HashSet<Qualifier> removeSet = new HashSet<Qualifier>();
                        for (Qualifier q: qSet) {
                            boolean found = false;
                            if (null == a.getAnnotationDetail().getWithAnnotSet()) {
                                System.out.println("Did not find with for annotation id " + a.getAnnotationId());
                                removeSet.add(q);
                                continue;
                            }
                            for (Annotation decAnnot: a.getAnnotationDetail().getWithAnnotSet()) {
                                if (true == QualifierDif.exists(decAnnot.getQualifierSet(), q)) {
                                    found = true;
                                    //if (Evidence.CODE_IBD.equals(code)) {
                                    //    a.getAnnotationDetail().addToAddedQualifierLookup(q, decAnnot);
                                   // }
                                    //else {
                                        // For IBA we inherit from withs
                                        a.getAnnotationDetail().addToInheritedQualifierLookup(q, decAnnot);
                                    //}
                                }
                            }
                            if (false == found) {
                                System.out.println("Did not find annotation to support qualifier " + q.getText() + " for annotation id " + a.getAnnotationId());
                                removeSet.add(q);
                            }
                        }
                        if (false == removeSet.isEmpty()) {
                            qSet.removeAll(removeSet);
                        }
                        if (0 == qSet.size()) {
                            a.setQualifierSet(null);
                        }
                    }                   
                    else if (Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code)) {
                        irkIrdSet.add(a);
                        
                        if (null == a.getAnnotationDetail().getWithAnnotSet()) {
                            System.out.println("Did not find with for IKR or IRD annotation " + a.getAnnotationId());
                            invalidAnnot.add(a);
                            continue;                            
                        }
                        Annotation propagatorAnnot = null;
                        for (Annotation with: a.getAnnotationDetail().getWithAnnotSet()) {
                            propagatorAnnot = with;
                            break;
                        }
                        if (false == QualifierDif.areOpposite(propagatorAnnot.getQualifierSet(), a.getQualifierSet())) {
                            System.out.println("NOT annotation is not opposite for " + a.getAnnotationId() + " and " + propagatorAnnot.getAnnotationId());
                            invalidAnnot.add(a);
                            continue;
                        }
                        boolean isNot = QualifierDif.containsNegative(a.getQualifierSet());
                        if (true == isNot) {
                            a.getAnnotationDetail().addToAddedQualifierLookup(QualifierDif.getNOT(a.getQualifierSet()), a);
                        }
                        else {
                            a.getAnnotationDetail().addToRemovedQualifierLookup(QualifierDif.getNOT(propagatorAnnot.getQualifierSet()), a);
                        }
                        a.getAnnotationDetail().addWith(a);         // IKR or IRD is the one that is adding or removing the NOT
                        HashSet<Qualifier> qSet = propagatorAnnot.getQualifierSet();
                        if (null != qSet) {
                            for (Qualifier q: qSet) {
                                if (QualifierDif.exists(a.getQualifierSet(), q) || q.isNot()) {
                                    a.getAnnotationDetail().addToInheritedQualifierLookup(q, propagatorAnnot);
                                }
                            }
                        }
                        qSet = a.getQualifierSet();
                        if (null != qSet) {
                            HashSet<Qualifier> removeSet = new HashSet<Qualifier>();
                            for (Qualifier q: qSet) {
                                if (false == QualifierDif.exists(propagatorAnnot.getQualifierSet(), q) && false == q.isNot()) {
                                    System.out.println("Did not find qualifier " + q.getText() + " in propagator " + propagatorAnnot.getAnnotationId() + " for annotation " + a.getAnnotationId());
                                   removeSet.add(q);
                                }
                            }
                            if (0 != removeSet.size()) {
                                qSet.removeAll(removeSet);
                                if (qSet.isEmpty()) {
                                    a.setQualifierSet(null);
                                }
                            }
                        }
                    }
                    else {
                        HashSet<Qualifier> qSet = a.getQualifierSet();
                        if (null != qSet) {
                            for (Qualifier q: qSet) {
                                a.getAnnotationDetail().addToAddedQualifierLookup(q, a);
                            }
                        }
                    }
                }
//                if (irkIrdSet.isEmpty()) {
//                    continue;
//                }
//                for (Annotation ikrird: irkIrdSet) {
//                    Node propagator = null;
//                    AnnotationDetail ad = ikrird.getAnnotationDetail();
//                    if (null == ad.getWithAnnotSet()) {
//                        System.out.println("No withs for IKR IRD " + ikrird.getAnnotationId());
//                        invalidAnnot.add(ikrird);
//                        continue;                        
//                    }
//                    for (Annotation with: ad.getWithAnnotSet()) {
//                        if (ikrird == with) {
//                            continue;
//                        }
//                        propagator = with.getAnnotationDetail().getAnnotatedNode();
//                        break;
//                    }
//                    if (propagator == null) {
//                        System.out.println("No propagator for annotation node " + ikrird.getAnnotationId());
//                        invalidAnnot.add(ikrird);
//                        continue;
//                    }
//                    String goTerm = ikrird.getGoTerm();
//                    for (Annotation a: annotSet) {
//                        if (a == ikrird) {
//                            continue;
//                        }
//                        if (Evidence.CODE_IBA.equals(a.getEvidence().getEvidenceCode()) && goTerm.equals(a.getGoTerm())) {
//                            ikrird.setChildAnnotation(a);
//                            a.setParentAnnotation(ikrird);
//                            break;
//                        }
//                    }
//                }
                n.getVariableInfo().getGoAnnotationList().removeAll(invalidAnnot);
                if (0 == n.getVariableInfo().getGoAnnotationList().size()) {
                    n.getVariableInfo().setGoAnnotationList(null);
                }
            }
            
            // Setup parent child relationship for irk_ird and iba.  This is just a temporary update.
            for (Annotation i: irkIrdSet) {
                Node annotatedNode = i.getAnnotationDetail().getAnnotatedNode();
                Annotation propagatorAnnot = null;
                if (null == i.getAnnotationDetail().getWithAnnotSet()) {
                    System.out.println("Going to remove annotation irk or ird " + i.getAnnotationId() + " .  Does not have with annotation.  Node is " + annotatedNode.getStaticInfo().getNodeAcc() + " public id " + annotatedNode.getStaticInfo().getPublicId() + " going to delete");
                    NodeVariableInfo nvi = annotatedNode.getVariableInfo();
                    if (nvi != null) {
                        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
                        if (null != annotList) {
                            annotList.remove(i);
                            if (annotList.isEmpty()) {
                                nvi.setGoAnnotationList(null);
                            }
                        }
                    }
                    continue;
                }
                for (Annotation with: i.getAnnotationDetail().getWithAnnotSet()) {
                    if (i == with) {
                        continue;
                    }
                    propagatorAnnot = with;
                    break;
                }
                if (null == propagatorAnnot) {
                    System.out.println("Did not find propagator for annotation " + i.getAnnotationId());
                    continue;
                }
                ArrayList<Annotation> annotList = annotatedNode.getVariableInfo().getGoAnnotationList();
                if (null != annotList) {
                    for (Annotation a: annotList) {
                        if (false == Evidence.CODE_IBA.equals(a.getEvidence().getEvidenceCode())) {
                            continue;
                        }
                        for (Annotation with: a.getAnnotationDetail().getWithAnnotSet()) {
                            if (propagatorAnnot == with) {
                                i.setChildAnnotation(with);
                                with.setParentAnnotation(i);
                                break;
                            }
                        }
                    }
                }
                else {
                    System.out.println("Did not find annotations for irkIrdSet node id is " + annotatedNode.getStaticInfo().getNodeAcc() + " " + annotatedNode.getStaticInfo().getPublicId());
                }
            }
            
            

        } catch (SQLException se) {
            System.out.println("Unable to retrieve full go annotation information from database, exception "
                    + se.getMessage() + " has been returned.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            ReleaseResources.releaseDBResources(grslt, gstmt, con);
        }

    }
    
    public ArrayList<Annotation> addPruned(String book, String uplVersion, HashMap<String, Node> lookup) {
        ArrayList<Annotation> rtnList = new ArrayList<Annotation>();
        Connection con = null;
        Statement stmt = null;
        ResultSet rslt = null;
        try {
            con = getConnection();
            if (null == con) {
                return rtnList;
            }

            // Make sure release dates can be retrieved, else return 
            if (null == clsIdToVersionRelease) {
                initClsLookup();
                if (null == clsIdToVersionRelease) {
                    return rtnList;
                }
            }

            String query = GET_PRUNED.replaceAll(QUERY_PARAMETER_3, ConfigFile.getProperty(uplVersion + TYPE_PRUNED));
            query = Utils.replace(query,QUERY_PARAMETER_2, book + QUERY_ANNOTATION_NODE_MIDDLE_WILDCARD);
            query = Utils.replace(query,QUERY_PARAMETER_1, uplVersion);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n);
            query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_a);

            stmt = con.createStatement();


            rslt = stmt.executeQuery(query);

            rslt.setFetchSize(100);
            while (rslt.next()) {

                // Protein Id to Identifiers

                String accId = rslt.getString(COLUMN_NAME_ACCESSION);
                Node n = lookup.get(accId);
                if (null == n) {
                    n = new Node();
                    lookup.put(accId, n);
                }
                
                NodeStaticInfo nsi = n.getStaticInfo();
                if (null == nsi) {
                    nsi = new NodeStaticInfo();
                    n.setStaticInfo(nsi);
                }
                nsi.setNodeId(rslt.getString(COLUMN_NAME_NODE_ID));
                nsi.setPublicId(rslt.getString(COLUMN_NAME_PUBLIC_ID));
                NodeVariableInfo nvi = n.getVariableInfo();
                if (null == nvi) {
                    nvi = new NodeVariableInfo();
                    n.setVariableInfo(nvi);
                }
                nvi.setPruned(true);
                String annotationId = rslt.getString(COLUMN_NAME_ANNOTATION_ID);
                Annotation a = new Annotation();
                a.setAnnotationId(annotationId);
                a.getAnnotationDetail().setAnnotatedNode(n);
                rtnList.add(a);
            }

        } catch (SQLException se) {
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();
        } finally {
            ReleaseResources.releaseDBResources(rslt, stmt, con);
        }        
        
        return rtnList;
    }

    public void getEvidence(String book, String uplVersion, HashMap<String, Node> lookup) {
        // Temporarily read from flat file
        String expPath = "C:\\PAINT_SUBMISSIONS\\paint\\gene-associations\\submission\\paint\\" + book + "\\" + book + ".exp";
        String gafPath = "C:\\PAINT_SUBMISSIONS\\paint\\gene-associations\\submission\\paint\\" + book + "\\" + book + ".gaf";

        ArrayList<String> expContents = Utils.readFile(expPath);
        if (null != expContents && expContents.size() > 0) {
            expContents.remove(0);      // Skip first line of file
        }
        ArrayList<String> gafContents = Utils.readFile(gafPath);
        if (null != gafContents && gafContents.size() > 0) {
            gafContents.remove(0);
        }

        ArrayList<String> allContents = new ArrayList<String>();
        if (null != expContents) {
            allContents.addAll(expContents);
        }
        if (null != gafContents) {
            allContents.addAll(gafContents);
        }
        // Skip first line of file
        for (int i = 0; i < allContents.size(); i++) {
            addEvidence(allContents.get(i), lookup);

        }
    }

    public void addEvidence(String evidenceInfo, HashMap<String, Node> lookup) {
        String parts[] = evidenceInfo.split(STR_TAB);
        // Use 15 although there are supposed to be 17
        if (parts.length < 15) {
            System.out.println("found " + parts.length + " number of fields - Invalid number of evidence fields in " + evidenceInfo);
            return;
        }
        String goTerm = parts[4];
        if (null == goTermHelper.getTerm(goTerm)) {
            System.out.println("Did not find goTerm " + goTerm);
            return;
        }
        Annotation a = new Annotation();
        a.setGoTerm(parts[4]);
        
        


        // Qualifier
        if (parts[3] != null && 0 != parts[3].length()) {
            String qualifierList[] = parts[3].split(BAR_DELIM);
            for (int i = 0; i < qualifierList.length; i++) {
                Qualifier q = new Qualifier();
                q.setText(qualifierList[i]);
                a.addQualifier(q);
            }
        }

        Evidence evidence = new Evidence();
        evidence.setEvidenceCode(parts[6]);
        a.setEvidence(evidence);


        // db ref usually pmid or PAINT
        if (parts[5] != null) {
            String[] dbParts = parts[5].split("\\|");
            for (int i = 0; i < dbParts.length; i++) {
                String dbPart = dbParts[i];
                String dbDetail[] = dbPart.split(STR_COLON);
                if (dbDetail.length >= 2) {
                    ArrayList<DBReference> dbRefList = evidence.getDbReferenceList();
                    if (null == dbRefList) {
                        dbRefList = new ArrayList<DBReference>(1);
                        evidence.setDbReferenceList(dbRefList);
                    }
                    DBReference dbRef = new DBReference();
                    dbRef.setEvidenceType(dbDetail[0]);
                    dbRef.setEvidenceValue(dbDetail[1]);
                    dbRefList.add(dbRef);
                }
            }
        }



        // Withs
        if (parts[7] != null) {
            String[] dbParts = parts[7].split("\\|");
            for (int i = 0; i < dbParts.length; i++) {
                String dbPart = dbParts[i];
                String dbDetail[] = dbPart.split(STR_COLON);
                if (dbDetail.length >= 2) {
                    ArrayList<DBReference> withList = evidence.getWiths();
                    if (null == withList) {
                        withList = new ArrayList<DBReference>(1);
                        evidence.setWiths(withList);
                    }
                    DBReference dbRef = new DBReference();
                    dbRef.setEvidenceType(dbDetail[0]);
                    dbRef.setEvidenceValue(dbDetail[1]);
                    withList.add(dbRef);
                }
            }
        }
        evidence.setDate(parts[13]);
        boolean found = false;
        Collection<Node> nodes = lookup.values();
        for (Iterator<Node> i = nodes.iterator(); i.hasNext();) {
            Node node = i.next();
            String modId = parts[1];         //;parts[1].replaceAll(":", "=");
            NodeStaticInfo staticInfo = node.getStaticInfo();
            String longGeneName = staticInfo.getLongGeneName();
            if (null != staticInfo.getPublicId()) {
                //System.out.println(staticInfo.getNodeAcc() + " Got public id " + staticInfo.getPublicId());
            }

            // Search for database and id or public PANTHER id
            if (null != modId) {
                if (modId.contains(staticInfo.getPublicId())) {
                    System.out.println("Found public id");
                }
            }
            if ((null != longGeneName && (longGeneName.contains(modId) && longGeneName.contains(parts[0]))) || (modId.equals(staticInfo.getPublicId()))) {
                NodeVariableInfo variableInfo = node.getVariableInfo();
                if (null == variableInfo) {
                    variableInfo = new NodeVariableInfo();
                    node.setVariableInfo(variableInfo);
                }
                variableInfo.addGOAnnotation(a);
                found = true;
                break;
            }
        }
        if (false == found) {
            System.out.println("Could not add evidence for " + parts[0] + " " + parts[1]);
        }

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
    
    
    public String getBookId(String book, String uplVersion) {
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();
            String query = BOOK_ID.replace(QUERY_PARAMETER_1, uplVersion);
            query = query.replace(QUERY_PARAMETER_2, ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
            query = query.replace(QUERY_PARAMETER_3, book);

            rst = stmt.executeQuery(query);

            
            if (rst.next()) {
                return Integer.toString(rst.getInt(COLUMN_NAME_CLASSIFICATION_ID));
            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve comment information from database, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            if (null != con) {
                ReleaseResources.releaseDBResources(rst, stmt, con);
            }
        }

        return null;
    }
    
    public synchronized String saveBook(SaveBookInfo sbi, String uplVersion) {
        Integer saveStatus = sbi.getSaveStatus();
        if (null == saveStatus) {
            return MSG_ERROR_SAVE_STATUS_NOT_SPECIFIED;
        }
        int saveSts = saveStatus.intValue();
        if (Constant.SAVE_OPTION_SAVE_AND_UNLOCK != saveSts && Constant.SAVE_OPTION_SAVE_AND_KEEP_LOCKED != saveSts && Constant.SAVE_OPTION_MARK_CURATED_AND_UNLOCK != saveSts) {
            return MSG_ERROR_INVALID_SAVE_STATUS_SPECIFIED;
        }
        
        String bookId = sbi.getBookId();
        if (null == bookId) {
            return MSG_ERROR_BOOK_ID_NOT_SPECIFIED;
        }
        User user = sbi.getUser();
        if (null == user) {
            return MSG_ERROR_USER_INFO_NOT_SPECIFIED;
        }        
        EVIDENCE_TYPE_SID_LOOKUP = initEvidenceLookup();
        ANNOTATION_TYPE_ID_LOOKUP = initAnnotationIdLookup();
        QUALIFIER_TYPE_ID_LOOKUP = initQualifierIdLookup();
        CONFIDENCE_CODE_TYPE_ID_LOOKUP = initConfidenceCodeLookup();
        

        
        ArrayList<Node> prunedList = sbi.getPrunedList();
        if (null == prunedList) {
            return MSG_SAVE_FAILED_PRUNED_IS_NULL;
        }
        
        ArrayList<Annotation> newAnnotationList = sbi.getAnnotationList();
        if (null == newAnnotationList) {
            return MSG_SAVE_FAILED_ANNOTATION_IS_NULL;
        }
        
        String userIdStr = user.getUserId();
        if (null == userIdStr) {
            return MSG_ERROR_USER_ID_NOT_SPECIFIED;
        }

        String clsIdStr = getClsIdForBookLockedByUser(userIdStr, uplVersion, bookId);

        if (null == clsIdStr) {
            return MSG_SAVE_FAILED_BOOK_NOT_LOCKED_BY_USER;
        }
        
//        if (0 == 0) {
//            return "TESTING - Return fail";
//        }
        
        String bookClsId = getBookId(bookId, uplVersion);
        if (null == bookClsId || false == clsIdStr.equals(bookClsId)) {
            return MSG_SAVE_FAILED_UNABLE_TO_RETRIEVE_BOOK_ID;
        }
        // Make sure upl has not already been released
        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease) {
            return MSG_SAVE_FAILED_ERROR_ACCESSING_DATA_FROM_DB;
        }
        
        // If this version of the upl has been released, then add clause to ensure only records
        // created prior to the release date are retrieved
        ArrayList clsInfo = clsIdToVersionRelease.get(uplVersion);

        if (null == clsInfo) {
            return MSG_SAVE_FAILED_INVALID_UPL_SPECIFIED;
        }
        if (clsInfo.size() < 1 || null != clsInfo.get(1)) {
            return MSG_SAVE_FAILED_UPL_RELEASED;
        }
        
        
        // Handle comments
        boolean commentUpdate = false;
        Integer obsoleteCommentId = null;
        Integer newCommentId = null;
        ArrayList<Integer> commentArray = new ArrayList<Integer>();
        String oldComment = getFamilyComment(bookId, uplVersion, commentArray);
        String newComment = sbi.getComment();
        if (null == oldComment && null == newComment) {
            // No Changes
//            System.out.println("comments are null");
        }
        else if (null != oldComment && oldComment.equals(newComment)) {
//            System.out.println("Comment not changed");
        }
        else {
            commentUpdate = true;
            if (false == commentArray.isEmpty()) {
                obsoleteCommentId = commentArray.get(0);
            }
            if (null != newComment && false == newComment.isEmpty()) {
                newCommentId = new Integer(getUids(1)[0]);
            }
        }
        
        // Handle family name
        boolean familyNameUpdate = false;
        String newFamilyName = sbi.getFamilyName();
        String oldFamilyName = getFamilyName(bookId, uplVersion);
        if (null == oldFamilyName && null == newFamilyName) {
            // No change
            System.out.println("famliy names still null");
        }
        else if (null != oldFamilyName && oldFamilyName.equals(newFamilyName)) {
            System.out.println("No family name change");
        }
        else {
            familyNameUpdate = true;
        }
        
        // Handle pruned
        HashMap<String, Node> prunedLookup = new HashMap<String, Node>();
        ArrayList<Annotation> prunedAnnotList = addPruned(bookId, uplVersion, prunedLookup);
        Collection<Node> curPrunedList = prunedLookup.values();
        //ArrayList<Node> obsoletePrunedList = new ArrayList<Node>();
        ArrayList<Node> newPrunedList = new ArrayList();
        HashMap<String, Annotation> prunedNodeAnnotLookup = new HashMap<String, Annotation>();
        ArrayList<Annotation> obsoleteAnnotPrunedList = new ArrayList<Annotation>();
        HashMap<Annotation, Node> newPrunedLookup = new HashMap<Annotation, Node>();
        for (Annotation a: prunedAnnotList) {
            prunedNodeAnnotLookup.put(a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId(), a);
        }
        
        for (Node n: curPrunedList) {
            boolean found = false;
            for (Node compN: prunedList) {
                if (true == n.getStaticInfo().getPublicId().equals(compN.getStaticInfo().getPublicId())) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                //obsoletePrunedList.add(n);
                obsoleteAnnotPrunedList.add(prunedNodeAnnotLookup.get(n.getStaticInfo().getPublicId()));
            }
        }
        
        for (Node n: prunedList) {
            boolean found = false;
            for (Node compN: curPrunedList) {
                if (true == n.getStaticInfo().getPublicId().equals(compN.getStaticInfo().getPublicId())) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                newPrunedList.add(n);
                Annotation a = new Annotation();
                a.setAnnotationId(new Integer(getUids(1)[0]).toString());
                newPrunedLookup.put(a, n);
            }
        }
        
        
        // Handle Annotations
        ArrayList<Annotation> obsoleteAnnotList = new ArrayList<Annotation>();
        ArrayList<Annotation> toSaveList = new ArrayList<Annotation>();
        ArrayList<Annotation> curAnnotList = getSavedAnnotations(uplVersion, bookId);  // Disable temporarily   new ArrayList<Annotation>();
        for (int i = 0 ; i < newAnnotationList.size(); i++) {
            Annotation a = newAnnotationList.get(i);
            boolean found = false;
            for (int j = 0; j < curAnnotList.size(); j++) {
                Annotation compA = curAnnotList.get(j);
//                System.out.println("Comparing annotations " + i + " of  " + newAnnotationList.size() + " with " + j + " of " + curAnnotList.size());
//                if (i == 13 && j == 15) {
//                    System.out.println("Here");
//                }
                if (true == annotationsSame(a, compA)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                if (false == toSaveList.contains(a)) {
                    toSaveList.add(a);
                }
            }
        }
        
        for (Annotation a: curAnnotList) {
            boolean found = false;
            for (Annotation compA: newAnnotationList) {
                if (true == annotationsSame(a, compA)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                if (false == obsoleteAnnotList.contains(a)) {
                    obsoleteAnnotList.add(a);
                }
            }
        } 

        // Generate annotation id
        for (Annotation a: toSaveList) {
            a.setAnnotationId(Integer.toString(getUids(1)[0]));
        }
        boolean updateAnnot = false;
        if (false == obsoleteAnnotList.isEmpty() || false == toSaveList.isEmpty()) {
            updateAnnot = true;
        }     
//        if (0 == 0) {
//            return "Testing";
//        }
        // Ready to save
        Connection updateCon = null;
        try {
            updateCon = getConnection();
            if (null == updateCon) {
                return MSG_SAVE_FAILED_UNABLE_TO_GET_CONNECTION_TO_DB;
            }
            updateCon.setAutoCommit(false);
            updateCon.rollback();
            
            // handle comments
            if (true == commentUpdate) {
                obsoleteInsertComment (updateCon, bookClsId, obsoleteCommentId, newCommentId, newComment, userIdStr);
            }
            
            // family name
            if (true == familyNameUpdate) {
                updateFamilyName(updateCon, bookClsId, newFamilyName);
            }

            // pruned and grafted nodes
            if (false != obsoleteAnnotPrunedList.isEmpty() || false != newPrunedLookup.isEmpty()) {
                obsoleteInsertPruned(updateCon, uplVersion, obsoleteAnnotPrunedList, newPrunedLookup, userIdStr);
            }
            
            // Annotations
            if (true == updateAnnot) {
                obsoleteInsertAnnotation(updateCon, bookClsId, obsoleteAnnotList, toSaveList, userIdStr);
            }
            

            // Book status
            String saveStatusInfo = handleSaveStatus(updateCon, bookClsId, saveStatus, userIdStr);
            if (null == saveStatusInfo || 0 != saveStatusInfo.length()) {
                updateCon.rollback();
                updateCon.setAutoCommit(true);
                return saveStatusInfo;
            }
            
            
//          updateCon.rollback();
            
            
//            if (0 == 0) {
//                updateCon.rollback();
//                updateCon.setAutoCommit(true);
//                return " testing";
//            }
            
            updateCon.commit();
            updateCon.setAutoCommit(true);
            return Constant.STR_EMPTY;            
        }
        catch(Exception e) {
            e.printStackTrace();
            try {
                if (null != updateCon) {
                    updateCon.rollback();
                    updateCon.setAutoCommit(true);
                    
                }

            }
            catch(Exception ex) {
                System.out.println(ex);
            }
            return "Error saving book";
        }
        finally {
            ReleaseResources.releaseDBResources(null, null, updateCon);
        }
        
        


    }
    
  protected String insertCurationStatus(Connection con, Integer famId, int statusId, String userIdStr){
    PreparedStatement stmt = null;

    try{

      // Insert record into the curation status table
      stmt = con.prepareStatement(PREPARED_INSERT_CURATION_STATUS);
      stmt.setInt(1, getUids(1)[0]);
      stmt.setInt(2, famId.intValue());
      stmt.setInt(3, Integer.parseInt(userIdStr));
      stmt.setInt(4, statusId);
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
    
    protected String UpdateCurtionStatus(Connection con, Integer famId, int statusId, String userIdStr) {
        PreparedStatement stmt = null;
        Integer curStsId = null;

        try {

            // Get the curated status record
            stmt = con.prepareStatement(PREPARED_CURATION_STATUS_ID);
            stmt.setInt(1, famId.intValue());
            stmt.setInt(2, Integer.parseInt(userIdStr));
            stmt.setInt(3, statusId);
            ResultSet rst = stmt.executeQuery();

            if (rst.next()) {
                curStsId = new Integer(rst.getInt(1));
            }
            rst.close();
            stmt.close();

            // No previous curated status record, insert a new record
            if (null == curStsId) {
                return insertCurationStatus(con, famId, statusId, userIdStr);
            }

            // Update the creation date of the old record
            stmt = con.prepareStatement(PREPARED_UPDATE_CURATION_STATUS);
            stmt.setInt(1, curStsId.intValue());
            if (1 != stmt.executeUpdate()) {
                stmt.close();
                return "Could not modify curated status record";
            }
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Exception while trying to modify curation status " + se.getMessage() + " has been returned");
            return "Exception while trying to save curation status";
        }
        return Constant.STR_EMPTY;
    }
    
    protected String deleteCurationStatus(Connection con, Integer famId, int statusId, String userIdStr) {
        try {
            PreparedStatement stmt = con.prepareStatement(PREPARED_BOOK_UNLOCK);

            stmt.setInt(1, famId.intValue());
            stmt.setInt(2, Integer.parseInt(userIdStr));
            stmt.setInt(3, statusId);

            // System.out.println(UpdateString.PREPARED_BOOK_UNLOCK);
            int numUpdated = stmt.executeUpdate();

            stmt.close();
            if (1 != numUpdated) {
                return "Changed status of " + numUpdated + " records, instead of 1.";
            }
        } catch (SQLException se) {
            System.out.println("Unable to delete record from database, exception " + se.getMessage() + " has been returned.");
            return "Unable to delete record from database, exception " + se.getMessage() + " has been returned.";
        }
        return "";
    }  
    
    protected String handleSaveStatus(Connection updateCon, String bookClsId, int saveStatus, String userIdStr) throws Exception {

            // Add entry to curation status table
            // the server and the client
            // 0 - Mark curated and unlock
            // 1 - Save and keep locked
            // 2 - Save and unlock
            if ((Constant.SAVE_OPTION_MARK_CURATED_AND_UNLOCK == saveStatus) || (Constant.SAVE_OPTION_SAVE_AND_UNLOCK == saveStatus)){
                if (Constant.SAVE_OPTION_MARK_CURATED_AND_UNLOCK == saveStatus) {
                    int statusId = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_MANUALLY_CURATED));
                    String errMsg = UpdateCurtionStatus(updateCon, Integer.parseInt(bookClsId), statusId, userIdStr);

                    if (0 != errMsg.length()) {
                        log.error(errMsg);
                        updateCon.rollback();
                        return "Save Operation failed - unable to insert curation status record";
                    }

                }

              // Unlock book
              int     checkOut = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_CHECKOUT));
              String  errMsg = deleteCurationStatus(updateCon, Integer.parseInt(bookClsId), checkOut, userIdStr);

              if (0 != errMsg.length()){
                log.error(errMsg);
                updateCon.rollback();
                return "Save Operation failed - unable to unlock book";
              }

              // recordsUpdated = true;
            }
//            else if (Constant.SAVE_OPTION_SAVE_AND_KEEP_LOCKED == saveStatus){

              // If the family id has changed, then unlock the old family id and lock the new id
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

//            // Add record to curation status table indicating, book has been updated
            if (Constant.SAVE_OPTION_MARK_CURATED_AND_UNLOCK != saveStatus){
              int     statusId = Integer.parseInt(ConfigFile.getProperty(CURATION_STATUS_PARTIALLY_CURATED));
              String  errMsg = UpdateCurtionStatus(updateCon, Integer.parseInt(bookClsId), statusId, userIdStr);

              if (0 != errMsg.length()){
                log.error(errMsg);
                updateCon.rollback();
                return "Save Operation failed - unable to insert family partially curated status record";
              }
            }
            
    
            return Constant.STR_EMPTY;
    
        
    }
    
    protected void obsoleteInsertAnnotation(Connection updateCon, String bookClsId, ArrayList<Annotation> obsoleteAnnotList, ArrayList<Annotation> toSaveList, String userIdStr) throws Exception {
        GOTermHelper gth = CategoryLogic.getInstance().getGOTermHelper();
        if (false == obsoleteAnnotList.isEmpty()) {
            PreparedStatement paintAnnotStmt = updateCon.prepareStatement(OBSOLETE_ANNOTATION_FROM_PAINT_ANNOTATION.replace(QUERY_PARAMETER_1, userIdStr));
            PreparedStatement evdnceAnnotStmt = updateCon.prepareStatement(OBSOLETE_ANNOTATION_FROM_PAINT_EVIDENCE.replace(QUERY_PARAMETER_1, userIdStr));
            
            for (Annotation a: obsoleteAnnotList) {
                int annotationId = new Integer(a.getAnnotationId()).intValue();
                paintAnnotStmt.setInt(1, annotationId);
                paintAnnotStmt.addBatch();
                evdnceAnnotStmt.setInt(1, annotationId);
                evdnceAnnotStmt.addBatch();
                Statement stmt = updateCon.createStatement();
                stmt.executeUpdate(DELETE_ANNOTATION_FROM_PAINT_ANNOTATION_QUALIFIER.replace(QUERY_PARAMETER_1, a.getAnnotationId()));
                ReleaseResources.releaseDBResources(null, stmt, null);
            }
            paintAnnotStmt.executeBatch();
            evdnceAnnotStmt.executeBatch();
            ReleaseResources.releaseDBResources(null, paintAnnotStmt, null);
            ReleaseResources.releaseDBResources(null, evdnceAnnotStmt, null);            
        }
        if (false == toSaveList.isEmpty()) {
            
            for (Annotation a: toSaveList) {
                String annotationId = a.getAnnotationId();
                String query = INSERT_ANNOTATION_INTO_PAINT_ANNOTATION.replace(QUERY_PARAMETER_1, annotationId);
                query = query.replace(QUERY_PARAMETER_2, a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getNodeId());
                query = query.replace(QUERY_PARAMETER_3, gth.getTerm(a.getGoTerm()).getId());                
                query = query.replace(QUERY_PARAMETER_4, ANNOTATION_TYPE_TO_ID_LOOKUP.get(ANNOTATION_TYPE_GO_PAINT));
                query = query.replace(QUERY_PARAMETER_5, userIdStr);                
                Statement stmt = updateCon.createStatement();
                stmt.executeUpdate(query);
                ReleaseResources.releaseDBResources(null, stmt, null);
                
                // Add qualifiers
                HashSet<Qualifier> qSet = a.getQualifierSet();
                if (null != qSet && 0 < qSet.size()) {
                    for (Qualifier q: qSet) {
                        if (null == q.getText()) {
                            continue;
                        }
                        String qId = QUALIFIER_TYPE_TO_ID_LOOKUP.get(q.getText());
                        if (null == qId) {
                            System.out.println("Got not supported qualifier type " + qId);
                            throw new Exception("Found non supported qualifier type " + qId); 
                        }
                        String qQuery = INSERT_PAINT_ANNOTATION_QUALIFIER.replace(QUERY_PARAMETER_1, Integer.toString(getUids(1)[0]));
                        qQuery = qQuery.replace(QUERY_PARAMETER_2, annotationId);
                        qQuery = qQuery.replace(QUERY_PARAMETER_3, qId);
                        Statement qStmt = updateCon.createStatement();
                        qStmt.executeUpdate(qQuery);
                        ReleaseResources.releaseDBResources(null, qStmt, null);
                    }
                }
                
                // Evidence
                String code = a.getEvidence().getEvidenceCode();
                String ccId = CONFIDENCE_CODE_TYPE_TO_ID_LOOKUP.get(code);
                if (null == code) {
                    System.out.println("Got not supported evidence code type " + code);
                    throw new Exception("Found non supported evidence code type " + code); 
                }
                AnnotationDetail ad = a.getAnnotationDetail();
                HashSet<Annotation> withSet = ad.getWithAnnotSet();
                if (null != withSet) {
                    for (Annotation with: withSet) {
                        // IKR and IRD have this case
                        if (a == with) {
                            continue;
                        }
                        String paintEQuery = INSERT_ANNOTATION_INTO_PAINT_EVIDENCE.replace(QUERY_PARAMETER_1, Integer.toString(getUids(1)[0]));
                        if (Evidence.CODE_IBA.equals(code) || Evidence.CODE_IKR.equals(code) || Evidence.CODE_IRD.equals(code)) {
                            paintEQuery = paintEQuery.replace(QUERY_PARAMETER_2, EVIDENCE_TYPE_TO_SID_LOOKUP.get(EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR));
                        }
                        else {
                            paintEQuery = paintEQuery.replace(QUERY_PARAMETER_2, EVIDENCE_TYPE_TO_SID_LOOKUP.get(EVIDENCE_TYPE_ANNOT_PAINT_EXP));
                        }
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_3, userIdStr);
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_4, ccId);
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_5, annotationId);
                        PreparedStatement ps = updateCon.prepareStatement(paintEQuery);
                        ps.setString(1, with.getAnnotationId());
                        ps.executeUpdate();
                        ReleaseResources.releaseDBResources(null, ps, null);
                    }
                }
                
                HashSet<Node> nodeSet = ad.getWithNodeSet();
                if (null != nodeSet) {
                    for (Node node: nodeSet) {
                        String paintEQuery = INSERT_ANNOTATION_INTO_PAINT_EVIDENCE.replace(QUERY_PARAMETER_1, Integer.toString(getUids(1)[0]));

                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_2, EVIDENCE_TYPE_TO_SID_LOOKUP.get(EVIDENCE_TYPE_ANNOT_PAINT_REF));

                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_3, userIdStr);
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_4, ccId);
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_5, annotationId);
                        PreparedStatement ps = updateCon.prepareStatement(paintEQuery);
                        ps.setString(1, node.getStaticInfo().getNodeId());
                        ps.executeUpdate();
                        ReleaseResources.releaseDBResources(null, ps, null);
                    }                    
                }
                
                
                // DBReference
                HashSet<DBReference> dbRefSet = ad.getWithOtherSet();
                if (null != dbRefSet) {
                    for (DBReference dbRef: dbRefSet) {
                        String paintEQuery = INSERT_ANNOTATION_INTO_PAINT_EVIDENCE.replace(QUERY_PARAMETER_1, Integer.toString(getUids(1)[0]));
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_2, EVIDENCE_TYPE_TO_SID_LOOKUP.get(dbRef.getEvidenceType()));
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_3, userIdStr);
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_4, ccId);
                        paintEQuery = paintEQuery.replace(QUERY_PARAMETER_5, annotationId);
                        PreparedStatement ps = updateCon.prepareStatement(paintEQuery);
                        ps.setString(1, dbRef.getEvidenceValue());
                        ps.executeUpdate();
                        ReleaseResources.releaseDBResources(null, ps, null);
                    }
                }
            }
        }
    }
    
    protected void obsoleteInsertPruned(Connection updateCon, String uplVersion, ArrayList<Annotation> obsoleteAnnotPrunedList, HashMap<Annotation, Node> newPrunedLookup, String userIdStr) throws Exception {
        if (false == obsoleteAnnotPrunedList.isEmpty()) {
            PreparedStatement prunedAnnotStmt = updateCon.prepareStatement(OBSOLETE_ANNOTATION_FROM_PAINT_ANNOTATION.replace(QUERY_PARAMETER_1, userIdStr));
            for (Annotation a: obsoleteAnnotPrunedList) {
                int annotationId = new Integer(a.getAnnotationId()).intValue();
                prunedAnnotStmt.setInt(1, annotationId);
                prunedAnnotStmt.addBatch();
            }
            prunedAnnotStmt.executeBatch();
            ReleaseResources.releaseDBResources(null, prunedAnnotStmt, null);
        }
        if (false == newPrunedLookup.isEmpty()) {
            for (Annotation a: newPrunedLookup.keySet()) {
                String annotationId = a.getAnnotationId();
                String query = INSERT_ANNOTATION_INTO_PAINT_ANNOTATION_PRUNED.replace(QUERY_PARAMETER_1, annotationId);
                Node n = newPrunedLookup.get(a);
                query = query.replace(QUERY_PARAMETER_2, n.getStaticInfo().getNodeId());
                query = query.replace(QUERY_PARAMETER_3, ConfigFile.getProperty(uplVersion + TYPE_PRUNED));
                query = query.replace(QUERY_PARAMETER_4, userIdStr);    
                Statement stmt = updateCon.createStatement();
                stmt.executeUpdate(query);
                ReleaseResources.releaseDBResources(null, stmt, null);
            }
        }
    }
    
    
    //// NOTE - WE ARE UPDATING THE FAMILY NAME AND NOT OBSOLETING AND INSERTING NEW RECORDS AS PER REQUEST FROM HUAIYU
    protected void updateFamilyName(Connection con, String bookClsId, String familyName) throws Exception {    
        PreparedStatement stmt = con.prepareStatement(PREPARED_UPDATE_FAMILY_NAME.replaceAll(QUERY_PARAMETER_1, bookClsId));
        stmt.setString(1, familyName);
        int rtnValue = stmt.executeUpdate();
        if (1 != rtnValue) {
            throw new Exception("Did not update family name");
        }
        ReleaseResources.releaseDBResources(null, stmt, null);   
    }
    
    
    protected void obsoleteInsertComment(Connection con, String bookClsId, Integer obsoleteCommentId, Integer newCommentId, String newComment, String userIdStr) throws Exception {
        if (null != obsoleteCommentId) {
            String query = OBSOLETE_COMMENT.replace(QUERY_PARAMETER_1, userIdStr);
            query = query.replace(QUERY_PARAMETER_2, obsoleteCommentId.toString());
            Statement stmt = con.createStatement();
            stmt.executeUpdate(query);
            ReleaseResources.releaseDBResources(null, stmt, null);
        }
        if (null != newCommentId && null != newComment) {
            String query = INSERT_COMMENT.replace(QUERY_PARAMETER_1, Integer.toString(newCommentId));
            query = query.replace(QUERY_PARAMETER_2, bookClsId);
            query = query.replace(QUERY_PARAMETER_3, userIdStr);            
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, newComment);            
            int rtnValue = stmt.executeUpdate();
            if (1 != rtnValue) {
                throw new Exception("Did not insert new comment");
            }
            ReleaseResources.releaseDBResources(null, stmt, null);
        }
    }
    
    
    //TODO - Use new query to get only IBD, IBA, IRD and IKR
    protected ArrayList<Annotation> getSavedAnnotations(String uplVersion, String book) {
        HashMap<String, Node> treeNodeLookup = new HashMap<String, Node>();
        getAnnotationNodeLookup(book, uplVersion, treeNodeLookup);
        getFullGOAnnotations(book, uplVersion, treeNodeLookup);
        
        ArrayList<Annotation> rtnList = new ArrayList<Annotation>();
        for (Node n: treeNodeLookup.values()) {
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            for (Annotation a: annotList) {
                String code = a.getEvidence().getEvidenceCode();
                if (true == Evidence.CODE_IBD.equals(code) || true == Evidence.CODE_IKR.equals(code) || true == Evidence.CODE_IRD.equals(code)) {
                    rtnList.add(a);
                    continue;
                }
                if (true == Evidence.CODE_IBA.equals(code)) {
                    if (null != getAssociatedIKRorIRDforIBA(n, a)) {
                        rtnList.add(a);
                        continue;
                    }
                }
            }
        } 
        
        
        return rtnList;
    }
    
    public static Annotation getAssociatedIKRorIRDforIBA(Node node, Annotation ibaAnnotation) {
        if (false == Evidence.CODE_IBA.equals(ibaAnnotation.getEvidence().getEvidenceCode())) {
            System.out.println("Call to get associated IRD OR IKR for non IBA");
            return null;
        }
        Annotation with = null;
        HashSet<Annotation> withSet = ibaAnnotation.getAnnotationDetail().getWithAnnotSet();
        if (null == withSet) {
            return null;
        }
        for (Annotation aWith: withSet) {
            with = aWith;
            break;
        }

        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            return null;
        }
        for (Annotation a: annotList) {
            String code = a.getEvidence().getEvidenceCode();
            if (true == Evidence.CODE_IKR.equals(code) || true == Evidence.CODE_IRD.equals(code)) {
                if (true == a.getAnnotationDetail().getWithAnnotSet().contains(with)) {
                    return a;
                }
            }
        }
        return null;
    }    
    
    
    protected int[] getUids(int numRequired) {

        int uids[] = new int[numRequired];
        Connection con = null;
        ResultSet rst = null;
        Statement stmt = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();


            // System.out.println(QueryString.UID_GENERATOR);
            for (int i = 0; i < numRequired; i++) {
                rst = stmt.executeQuery(UID_GENERATOR);
                while (rst.next()) {
                    uids[i] = rst.getInt(1);
                }
                rst.close();
            }
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Unable to retrieve next uid  from database, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }

        // System.out.println("End of generate uids Operation " + df.format(new java.util.Date(System.currentTimeMillis())));
        return uids;
    }  
    
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
      int               checkOut = Integer.parseInt(ConfigFile.getProperty("go_check_out"));
      PreparedStatement stmt = con.prepareStatement(PREPARED_CLSID_FOR_BOOK_LOCKED_BY_USER);

      stmt.setInt(1, Integer.parseInt(userId));
      stmt.setInt(2, checkOut);
      stmt.setInt(3, Integer.parseInt(uplVersion));
      stmt.setString(4, book);

      System.out.println("Get clsid of book locked by user id = " + userId + " current status = " + checkOut + " upl version is " + uplVersion + " book is " + book);
      ResultSet rst = stmt.executeQuery();

      if (rst.next()){
        clsId = new Integer(rst.getInt(1));
        System.out.println("Got cls id = " + clsId);
      }
      else {
          System.out.println("Could not get cls id of book locked by user");
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information about clisid of book from database, exception " + se.getMessage()
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
    
    return null;
    
  }    

    public HashMap<String, Node> getNodeInfo(String book, String uplVersion) {
        long startTime = System.currentTimeMillis();      
        HashMap<String, Node> treeNodeLookup = new HashMap<String, Node>();
        getAnnotationNodeLookup(book, uplVersion, treeNodeLookup);
        getIdentifierInfo(book, uplVersion, treeNodeLookup);
        getGeneInfo(book, uplVersion, treeNodeLookup);
        addPruned(book, uplVersion, treeNodeLookup);
        //getEvidence(book, uplVersion, treeNodeLookup);
        getFullGOAnnotations(book, uplVersion, treeNodeLookup);
        long endTime = System.currentTimeMillis();
        System.out.println("It took " + (endTime - startTime) / 1000 + " secs to retrieve information for book " + book);
        return treeNodeLookup;
    }

    public String[] getTree(String book, String uplVersion) {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rst = null;

        String[] treeStrings = null;

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

            //
            String query = PREPARED_TREE;

            // If this version of the upl has been released, then add clause to ensure only records
            // created prior to the release date are retrieved
            ArrayList clsInfo = clsIdToVersionRelease.get(uplVersion);
            if (null == clsInfo) {
                log.error(MSG_CLASSIFICATION_ID_INFO_NOT_FOUND);
                return null;
            }
            String dateStr = (String) clsInfo.get(1);

            if (null != dateStr) {
                query += Utils.replace(PREPARED_RELEASE_CLAUSE, "tblName", "td");
            } else {
                query += Utils.replace(NON_RELEASE_CLAUSE, "tblName", "td");
            }

            stmt = con.prepareStatement(query);

            stmt.setInt(1, Integer.parseInt(uplVersion));
            stmt.setString(2, book);

            // Add values for the date clause
            if (null != dateStr) {
                stmt.setString(3, dateStr);
                stmt.setString(4, dateStr);
            }

            rst = stmt.executeQuery();
            Vector treeRslt = new Vector();

            while (rst.next()) {
                treeRslt.add(rst.getString(1));
            }

            // break string according to new line characters
            treeStrings = Utils.tokenize((String) treeRslt.elementAt(0), NEWLINE_DELIM);

        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }
        if (0 == treeStrings.length) {
            return null;
        }
        return treeStrings;
    }

    public void getAnnotationNodeLookup(String familyId, String uplVersion, HashMap<String, Node> nodeLookup) {
        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;

        try {
            con = getConnection();
            if (null == con) {
                return;
            }

            String query = Utils.replace(ANNOTATION_NODE_PUBLIC_ID_LOOKUP, QUERY_PARAMETER_1, familyId);
            query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);

            stmt = con.createStatement();
            rst = stmt.executeQuery(query);

            while (rst.next()) {
                String accession = rst.getString(COLUMN_NAME_ACCESSION);
                if (null == accession) {
                    continue;
                }
                Node node = nodeLookup.get(accession);
                if (null == node) {
                    node = new Node();
                    nodeLookup.put(accession, node);
                }
                NodeStaticInfo staticInfo = node.getStaticInfo();
                if (null == staticInfo) {
                    staticInfo = new NodeStaticInfo();
                    node.setStaticInfo(staticInfo);
                }
                staticInfo.setPublicId(rst.getString(COLUMN_NAME_PUBLIC_ID));
                staticInfo.setNodeId(rst.getString(COLUMN_NAME_NODE_ID));
                staticInfo.setNodeAcc(accession);
            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database, exception "
                    + se.getMessage() + " has been returned.");
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }
    }

    public void getIdentifierInfo(String book, String uplVersion, HashMap<String, Node> nodeLookup) {

        // Now get the identifiers for each annotation node
        if (0 == book.length()) {
            return;
        }
        Connection con = null;
        Statement istmt = null;
        ResultSet irslt = null;
        try {
            con = getConnection();
            if (null == con) {
                return;
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                initClsLookup();
                if (null == clsIdToVersionRelease) {
                    return;
                }
            }

            String idQuery = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_n);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_p);
            idQuery = addVersionReleaseClause(uplVersion, idQuery, TABLE_NAME_i);
            idQuery = IDENTIFIER_AN + idQuery;

            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_1, uplVersion);
            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_2, book + QUERY_WILDCARD);
            idQuery = Utils.replace(idQuery, QUERY_PARAMETER_3, TYPE_IDENTIFIERS);
            System.out.println(idQuery);

            istmt = con.createStatement();

            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("start of identifier query execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            irslt = istmt.executeQuery(idQuery);

            System.out.println("end of identifier query execution, time is " + df.format(new java.util.Date(System.currentTimeMillis())));
            irslt.setFetchSize(100);
            while (irslt.next()) {

                // Protein Id to Identifiers
                String annotId = irslt.getString(COLUMN_NAME_ACCESSION);
                String identifierName = irslt.getString(COLUMN_NAME_NAME);
                String identifierType = Integer.toString(irslt.getInt(COLUMN_NAME_IDENTIFIER_TYPE_SID));

                if (null != identifierName && null != annotId) {
                    Node node = nodeLookup.get(annotId);
                    if (null == node) {
                        node = new Node();
                        nodeLookup.put(annotId, node);
                    }
                    NodeStaticInfo staticInfo = node.getStaticInfo();
                    if (null == staticInfo) {
                        staticInfo = new NodeStaticInfo();
                        node.setStaticInfo(staticInfo);
                    }
                    if (TYPE_IDENTIFIER_DEFINITION.equals(identifierType)) {
                        staticInfo.setDefinition(identifierName);
                    }
                    if (TYPE_IDENTIFIER_ORTHOMCL.equals(identifierType)) {
                        staticInfo.setOrthoMCL(identifierName);
                    }

                } else {
                    log.error(book + MSG_ERROR_IDENTIFIER_RETRIEVAL_NULL_FOUND + annotId);
                }
            }

        } catch (SQLException se) {
            log.error(MSG_ERROR_UNABLE_TO_RETRIEVE_INFO_ERROR_RETURNED + se.getMessage());
            se.printStackTrace();
        } finally {
            ReleaseResources.releaseDBResources(irslt, istmt, con);
        }

    }
    
    public User getUser(String userName, String password) {
        User user = null;
        
        if (null == userName || null == password || 0 == userName.length() || 0 == password.length()) {
            return user;
        }
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rst = null;
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.prepareStatement(PREPARED_USER_VALIDATION);

            stmt.setString(1, userName);
            stmt.setString(2, password);

            rst = stmt.executeQuery();

            if (rst.next()) {
                int rank = rst.getInt(COLUMN_NAME_PRIVILEGE_RANK);
                String name = rst.getString(COLUMN_NAME_USER_NAME);
                String email = rst.getString(COLUMN_NAME_EMAIL);
                String groupName = rst.getString(COLUMN_NAME_GROUP_NAME);
                String id = Integer.toString(rst.getInt(COLUMN_USER_ID));
                user = new User(name, null, email, userName, rank, groupName);
                user.setUserId(id);
            }
            
        } catch (SQLException se) {
            System.out.println("Unable to retrieve user information from database, exception " +
                               se.getMessage() + " has been returned.");
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }
        return user;
    }
    
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
      return Book.CURATION_STATUS_UNKNOWN;
      
  }    
    
    public Hashtable<String, Book> getListOfBooksAndStatus(String uplVersion) {
    
        String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c);
        query = GET_LIST_OF_BOOKS + query;

        query = Utils.replace(query,QUERY_PARAMETER_1, ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
        query = Utils.replace(query,QUERY_PARAMETER_2, uplVersion);  
        
        Connection con = null;
            Statement stmt = null;
            ResultSet rst = null;
             ResultSet irslt = null;         
        Hashtable<String, Book> bookTbl = new Hashtable<String, Book>();
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            
            // Get list of books
            stmt = con.createStatement();
            rst = stmt.executeQuery(query);
            while (rst.next()) {
                String accession = rst.getString(COLUMN_NAME_ACCESSION);
                String bookName = rst.getString(COLUMN_NAME_NAME);
                bookTbl.put(accession, new Book(accession, bookName, Book.CURATION_STATUS_UNKNOWN, null));
            }
            rst.close();
            
            
            // Get status and user information
             String checkOutStatus = ConfigFile.getProperty(CURATION_STATUS_CHECKOUT);
             query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_c);
             query = GET_STATUS_USER_INFO + query;

             query = Utils.replace(query,QUERY_PARAMETER_1, ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
             query = Utils.replace(query,QUERY_PARAMETER_2, uplVersion); 
             
             irslt = stmt.executeQuery(query);
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
            ReleaseResources.releaseDBResources(rst, stmt, con);
            ReleaseResources.releaseDBResources(irslt, null, null);            
        }
        
        return bookTbl;

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
    private ArrayList<Book> booksForQuery(String query, String uplVersion) {
        String bookList[] = getBookAccession(query);
        Hashtable <String, Book> allBookTbl = getListOfBooksAndStatus(uplVersion);
        
        int size = bookList.length;
        ArrayList<Book> finalList = new ArrayList<Book>(size);
        for (int i = 0; i < size; i++) {
            Book b = allBookTbl.get(bookList[i]);
            if (null != b) {
                finalList.add(b);
            }
//            else {
//                log.error("Did not find book for " + bookList[i]);
//            }
        }
        

        Collections.sort(finalList);
        return finalList;
//        Object[] allList = new Object[finalList.size()];
//        finalList.copyInto(allList);
//        java.util.Arrays.sort(allList);
//        return new Vector(Arrays.asList(allList));


    }    
  public ArrayList<Book> searchBooksByDefinition(String searchTerm, String uplVersion) {
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
      query = SEARCH_NODE_DEFINITION + query;
      query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
      query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
      

      return booksForQuery(query, uplVersion);     
  }
  
    public ArrayList<Book> searchBooksByProteinPrimaryExtId(String proteinPrimaryExtId, String uplVersion) {
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
        query = SEARCH_NODE_PROTEIN_PRIMARY_EXT_ID + query;
        query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
        query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
        

        return booksForQuery(query, uplVersion);     
    }
    
   public ArrayList<Book> searchBooksByGenePrimaryExtAcc(String genePrimaryExtAcc, String uplVersion) {
       if (null == clsIdToVersionRelease) {
           initClsLookup();
       }

       // Make sure release dates can be retrieved, else return null
       if (null == clsIdToVersionRelease) {
           return null;
       }

       String query = addVersionReleaseClause(uplVersion, Constant.STR_EMPTY, TABLE_NAME_g);
       query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_gn);
       query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n);
       String wildcardStr = PERCENT + genePrimaryExtAcc + PERCENT;
       query = SEARCH_NODE_GENE_PRIMARY_EXT_ACC + query;
       query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
       query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
       

       return booksForQuery(query, uplVersion);
   }
   
  public ArrayList<Book> searchBooksByGeneSymbol(String geneSymbol, String uplVersion) {
  
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
      query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_gn);
      query = addVersionReleaseClause(uplVersion, query, TABLE_NAME_n);
      query = SEARCH_NODE_GENE_SYMBOL + query;
      String wildcardStr = PERCENT + geneSymbol.toLowerCase() + PERCENT;
      query = Utils.replace(query, QUERY_PARAMETER_1, wildcardStr);
      query = Utils.replace(query, QUERY_PARAMETER_2, uplVersion);
      
      return booksForQuery(query, uplVersion);

  }
  
    public ArrayList<Book> getAllBooks(String uplVersion) {
        if (null == clsIdToVersionRelease) {
            initClsLookup();
        }

        // Make sure release dates can be retrieved, else return null
        if (null == clsIdToVersionRelease) {
            return null;
        }

        Hashtable<String, Book> bookTbl = getListOfBooksAndStatus(uplVersion);
        ArrayList<Book> books = new ArrayList(bookTbl.values());
        Collections.sort(books);
        return books;
//        
//        
//        Book[] bookList = new Book[books.size()];
//        books.copyInto(bookList);
//        java.util.Arrays.sort(bookList);
//        return new Vector(Arrays.asList(bookList));
    }
    
    public ArrayList<Book> getUncuratedUnlockedBooks(String uplVersion) {
    
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
//            int status = aBook.getCurationStatus();
            if (true == aBook.hasStatus(Book.CURATION_STATUS_MANUALLY_CURATED) ||
                true == aBook.hasStatus(Book.CURATION_STATUS_CHECKED_OUT)) {
                bookTbl.remove(id);       
            }
        }
        
        ArrayList<Book> books = new ArrayList(bookTbl.values());        
        Collections.sort(books);
        return books;
//        Book[] bookList = new Book[books.size()];
//        books.copyInto(bookList);
//        java.util.Arrays.sort(bookList);
//        return new Vector(Arrays.asList(bookList));
    }
    
    public String getFamilyComment(String book, String uplVersion, ArrayList<Integer> commentArray) {

        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;
        try {
            con = getConnection();
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();
            String query = FAMILY_COMMENT.replace(QUERY_PARAMETER_1, uplVersion);
            query = query.replace(QUERY_PARAMETER_2, ConfigFile.getProperty(uplVersion + LEVEL_FAMILY));
            query = query.replace(QUERY_PARAMETER_3, book);

            rst = stmt.executeQuery(query);

            
            if (rst.next()) {
                commentArray.add(new Integer(rst.getInt(COLUMN_NAME_COMMENT_ID)));
                return rst.getString(COMUMN_NAME_REMARK);
            }

        } catch (SQLException se) {
            System.out.println("Unable to retrieve classification id from book from database, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            if (null != con) {
                ReleaseResources.releaseDBResources(rst, stmt, con);
            }
        }

        return null;
    }
    
    
    public String getFamilyCommentOld(String book, String uplVersion, ArrayList<Integer> commentArray) {
        String comment = null;
        Integer commentId;
        Connection con = null;
        Statement stmt = null;
        ResultSet rst = null;

        try {
            con = getConnection();
            if (null == con) {
                return null;
            }

            // Make sure release dates can be retrieved, else return null
            if (null == clsIdToVersionRelease) {
                initClsLookup();
                if (null == clsIdToVersionRelease) {
                    return null;
                }
            }

      // If this version of the upl has been released, then add clause to ensure only records
            // created prior to the release date are retrieved
            ArrayList clsInfo = (ArrayList) clsIdToVersionRelease.get(uplVersion);
            String dateStr = (String) clsInfo.get(1);

            // PreparedStatement stmt;
            String query;

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
            query = CLS_COMMENT_PRIMARY_OBJECT_ID;
            String creationClause = "";

            if (null != dateStr) {
                creationClause = Utils.replace(CREATION_DATE_CLAUSE, "tblName", "c");
                creationClause = Utils.replace(creationClause, "%1", dateStr);
            } else {
                creationClause = Utils.replace(NON_RELEASE_CLAUSE, "tblName", "c");
            }
            query = Utils.replace(query, "%1", creationClause);
            query = Utils.replace(query, "%2", uplVersion);
            query = Utils.replace(query, "%3", ConfigFile.getProperty(uplVersion + "_famLevel"));
            query = Utils.replace(query, "%4", book);
            System.out.println("Comment query " + query);

        // stmt = con.prepareStatement(query);
            // stmt.setInt(1, Integer.parseInt(uplVersion));
            // int depth = Integer.parseInt(ConfigFile.getProperty(uplVersion + "_subfamLevel"));
            // stmt.setInt(2, depth);
            // stmt.setString(3, book + ":%");
            // if (null != dateStr) {
            // stmt.setString(4, dateStr);
            // }
//      }
            // System.out.println(query);
            stmt = con.createStatement();
            rst = stmt.executeQuery(query);

//      rst.setFetchSize(100);
//      StringBuffer  commentBfr = new StringBuffer();
            while (rst.next()) {
                comment = rst.getString(COMUMN_NAME_REMARK);
                int comment_id = rst.getInt(COLUMN_NAME_COMMENT_ID);
                commentArray.add(new Integer(comment_id));
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
            }
            rst.close();
            stmt.close();
        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                    + " has been returned.");
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }
        return comment;
    }
    
  public String getFamilyName(String book, String uplVersion){
    Connection  con = null;
    Statement stmt = null;
    ResultSet rst = null;
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
      ArrayList  clsInfo = (ArrayList) clsIdToVersionRelease.get(uplVersion);
      String  dateStr = (String) clsInfo.get(1);
      String  query = FAMILY;
      if (null != dateStr){
        query += Utils.replace(RELEASE_CLAUSE, "tblName", "c");
        query = Utils.replace(query, "%1", dateStr);
      }
      else{
        query += Utils.replace(NON_RELEASE_CLAUSE, "tblName", "c");
      }
      query = Utils.replace(query, "%2", uplVersion);
      query = Utils.replace(query, "%3", book);

      stmt = con.createStatement();
      rst = stmt.executeQuery(query);

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
      ReleaseResources.releaseDBResources(rst, stmt, con);
    }
    return famName;
  }
  
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
      PreparedStatement stmt = con.prepareStatement(PREPARED_USER_VALIDATION);

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
  
    public ArrayList<Book> getMyBooks(String userName, String password, String uplVersion) {
        String  userId  = getUserId(userName, password);
        if (null == userId) {
            return null;
        }
        return getLockedBooks(userId, uplVersion);
    }
    
    protected ArrayList<Book> getLockedBooks(String userId, String uplVersion) {
        if (null == userId){
          return null;
        }
        Connection  con = null;
        ArrayList<Book>    bookRslt = new ArrayList<Book>();

        try{
          con = getConnection();
          if (null == con){
            return null;
          }



          String            query = PREPARED_UNLOCKING_BOOK_LIST;
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

            bookRslt.add(aBook);
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
    
    
    private boolean annotationsSame(Annotation a1, Annotation a2) {
        if (false == a1.getEvidence().getEvidenceCode().equals(a2.getEvidence().getEvidenceCode())) {
            return false;
        }

        if (false == a1.getGoTerm().equals(a2.getGoTerm())) {
            return false;
        }
        QualifierDif qf = new QualifierDif(a1.getQualifierSet(), a2.getQualifierSet());
        if (qf.getDifference() != QualifierDif.QUALIFIERS_SAME) {
            return false;
        }
        AnnotationDetail ad1 = a1.getAnnotationDetail();
        AnnotationDetail ad2 = a2.getAnnotationDetail();
        
        if (false == nodeSame(ad1.getAnnotatedNode(), ad2.getAnnotatedNode())) {
            return false;
        }
        
        // IRD's and IKR have 'with annotation' that is self.  i.e. Annotation is the 'with' which is responsible for the NOT
        // In order to avoid an infinite loo here, clone and remove self from here.
        HashSet<Annotation> withAnnot1 = ad1.getWithAnnotSet();
        if (null != withAnnot1) {
            withAnnot1 = (HashSet<Annotation>)withAnnot1.clone();
            withAnnot1.remove(a1);
        }
        
        HashSet<Annotation> withAnnot2 = ad2.getWithAnnotSet();
        if (null != withAnnot2) {
            withAnnot2 = (HashSet<Annotation>)withAnnot2.clone();
            withAnnot2.remove(a2);
        }
        if (false == annotSetSame(withAnnot1, withAnnot2)) {
            return false;
        }
        
        if (false == linkedHashMapsSame(ad1.getInheritedQualifierLookup(), ad2.getInheritedQualifierLookup(), a1, a2)) {
            return false;
        }

        if (false == linkedHashMapsSame(ad1.getAddedQualifierLookup(), ad2.getAddedQualifierLookup(), a1, a2)) {
            return false;
        }
                
        if (false == linkedHashMapsSame(ad1.getRemovedQualifierLookup(), ad2.getRemovedQualifierLookup(), a1, a2)) {
            return false;
        }                
        
        
        if (false == nodeSetSame(ad1.getWithNodeSet(), ad2.getWithNodeSet())) {
            return false;
        }
        
        if (false == dbRefSetSame(ad1.getWithOtherSet(), ad2.getWithOtherSet())) {
            return false;
        }

        return true;
    }
    
    private boolean linkedHashMapsSame(LinkedHashMap<Qualifier, HashSet<Annotation>> map1, LinkedHashMap<Qualifier, HashSet<Annotation>> map2, Annotation a1, Annotation a2) {
        if (null == map1 && null == map2) {
            return true;
        }
        if ((map1 != null && map2 == null) || (map1 == null && map2 != null)) {
            return false;
        }
        if (map1.size() != map2.size()) {
            return false;
        }
        
        Set<Qualifier> set1 = map1.keySet();
        Set<Qualifier> set2 = map2.keySet();
        HashSet<Qualifier> hSet1 = new HashSet<Qualifier>(set1);
        HashSet<Qualifier> hSet2 = new HashSet<Qualifier>(set2);
        QualifierDif qd = new QualifierDif(hSet1, hSet2);
        if (QualifierDif.QUALIFIERS_SAME != qd.getDifference()) {
            return false;
        }
        
        for (Qualifier q1: hSet1) {
            Qualifier q2 = QualifierDif.find(hSet2, q1);
            if (null == q2) {
                return false;
            }
            
            // Qualifiers sometimes point to annotation that is currently being compared.  Remove to avoid infinite loop
            HashSet<Annotation> annotSet1 = map1.get(q1);
            if (null != annotSet1) {
                annotSet1 = (HashSet<Annotation>)annotSet1.clone();
                annotSet1.remove(a1);
            }
            
            HashSet<Annotation> annotSet2 = map2.get(q2);
            if (null != annotSet2) {
                annotSet2 = (HashSet<Annotation>)annotSet2.clone();
                annotSet2.remove(a2);
            }
            if (false == annotSetSame(annotSet1, annotSet2)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean nodeSame(Node n1, Node n2) {
        if (false == n1.getStaticInfo().getNodeId().equals(n2.getStaticInfo().getNodeId())) {
            return false;
        }
        return true;
    }
    
    private boolean nodeSetSame(HashSet<Node> set1, HashSet<Node> set2) {
        if (null == set1 && null == set2) {
            return true;
        }
        if ((set1 != null && set2 == null) || (set1 == null && set2 != null)) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        for (Node n1: set1) {
            boolean found = false;
            for (Node n2: set2) {
                if (true == nodeSame(n1, n2)) {
                    found = true;
                    break;
                }
                if (false == found) {
                    return false;
                }
            }
        }
        
        for (Node n2: set2) {
            boolean found = false;
            for (Node n1: set1) {
                if (true == nodeSame(n1, n2)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }
        return true;
    }
    
    private boolean dbRefSame(DBReference db1, DBReference db2) {
        if (false == StringUtils.stringsSame(db1.getEvidenceType(), db2.getEvidenceType())) {
            return false;
        }
        if (false == StringUtils.stringsSame(db1.getEvidenceValue(), db2.getEvidenceValue())) {
            return false;
        }
        return true;
    }
    
    private boolean dbRefSetSame(HashSet<DBReference> set1, HashSet<DBReference> set2) {
        if (null == set1 && null == set2) {
            return true;
        }
        if ((set1 != null && set2 == null) || (set1 == null && set2 != null)) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        for (DBReference dbRef1: set1) {
            boolean found = false;
            for (DBReference dbRef2: set2) {
                if (true == dbRefSame(dbRef1, dbRef2)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                return false;
            }
        }
        
        for (DBReference dbRef2: set2) {
            boolean found = false;
            for (DBReference dbRef1: set1) {
                if (true == dbRefSame(dbRef1, dbRef2)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                return false;
            }

        }
        
        return true;
    }
    
    private boolean annotSetSame(HashSet<Annotation> set1, HashSet<Annotation> set2) {
        if (null == set1 && null == set2) {
            return true;
        }
        if ((set1 != null && set2 == null) || (set1 == null && set2 != null)) {
            return false;
        }
        if (set1.size() != set2.size()) {
            return false;
        }
        
        for (Annotation a1: set1) {
            boolean found = false;
            for (Annotation a2: set2) {
                if (true == annotationsSame(a1, a2)) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                return false;
            }
        }
        
        for (Annotation a2: set2) {
            boolean found = false;
            for (Annotation a1: set1) {
                if (true == annotationsSame(a1, a2)) {
                    found = true;
                    break;
                }
            }
            if (false == found) {
                return false;
            }
        }
        
        return true;
    }
    
    
    
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
      PreparedStatement stmt = con.prepareStatement(PREPARED_CLSID_FOR_BOOK_USER_LOCK);

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
      PreparedStatement stmt = con.prepareStatement(PREPARED_BOOK_UNLOCK);

      stmt.setInt(1, Integer.parseInt(clsId));
      stmt.setInt(2, Integer.parseInt(userId));
      stmt.setInt(3, Integer.parseInt(ConfigFile.getProperty("go_check_out")));


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
    
  public Integer  getRank(String userId){

    Connection  con = null;
    Statement stmt = null;
    ResultSet rst = null;
    System.out.println("Getting userid to rank information");
    try{
      con = getConnection();
      if (null == con){
        return null;
      }
      stmt = con.createStatement();
      String    query = USER_ID.replace(QUERY_PARAMETER_1, userId);

      //System.out.println(query);
      rst = stmt.executeQuery(query);


      if (rst.next()){
        return new Integer(rst.getInt(COLUMN_NAME_PRIVILEGE_RANK));
      }
      rst.close();
      stmt.close();
    }
    catch (SQLException se){
      System.out.println("Unable to retrieve information from database, exception " + se.getMessage()
                         + " has been returned.");
    }
    finally{
            ReleaseResources.releaseDBResources(rst, stmt, con);
      }
    return null;
  }    
    
    protected String checkUserCanLockorUnlockBooks(String userId) {


        if (null == userId){
          return MSG_ERROR_UNABLE_TO_VERIFY_USER;
        }
        Integer rank = getRank(userId);
        if (null == rank){
          return MSG_ERROR_UNABLE_TO_RETRIEVE_USER_RANK_INFO;
        }

        // Get user rank and curator rank.
        String  curatorRank = ConfigFile.getProperty(RANK_PANTHER_CURATOR);


        int     curRank = Integer.parseInt(curatorRank);

        if (rank.intValue() < curRank){
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
            System.out.println("Unable to save lock book information from database, exception " +
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
        String query = PREPARED_CLSIDS_FOR_BOOKS_USER_LOCK;
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
      PreparedStatement stmt = con.prepareStatement(PREPARED_BOOK_LOCK);
      int               checkOut = Integer.parseInt(ConfigFile.getProperty("panther_check_out"));

      stmt.setInt(1, getUids(1)[0]);
      stmt.setInt(2, checkOut);
      stmt.setInt(3, Integer.parseInt(clsId));
      stmt.setInt(4, Integer.parseInt(userId));


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
      PreparedStatement stmt = con.prepareStatement(USER_PRIVILEGE);

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

    
    public static void main(String args[]) {
        DataIO di = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID)); 
  
        
        
        

    }   
    
    
    
    
    
    
    
}
