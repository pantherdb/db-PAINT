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

// NOT CONVERTED TO SUPPORT POSTGRES

public class UpdateString {

  public static final String PREPARED_OBSOLETE_TREE = "update upl_TREE_DETAIL set OBSOLESCENCE_DATE = sysdate, OBSOLETED_BY = ? where CLASSIFICATION_ID = ? and OBSOLESCENCE_DATE is null";
  public static final String PREPARED_INSERT_EMPTY_TREE = "insert into upl_TREE_DETAIL (tree_id, classification_id, tree_text, created_by, creation_date) values (?, ?, EMPTY_CLOB(), ?, sysdate)";
  public static final String PREPARED_UPDATE_TREE_CLOB = "update upl_TREE_DETAIL set TREE_TEXT = ? where TREE_ID = ?";
  public static final String PREPARED_INSERT_CLASSIFICATION = "insert into upl_classification (classification_id, classification_version_sid, name, accession, depth, created_by, EVALUE_CUTOFF, creation_date) values (?, ?, ?, ?, ?, ?, ?, sysdate)";
  public static final String PREPARED_INSERT_ANNOTATION = "insert into annotation (annotation_id, node_id, classification_id, annotation_type_id, created_by, creation_date) values (?, ?, ?, ?, ?, sysdate)";
  public static final String PREPARED_INSERT_ANNOTATION_QUALIFIER = "insert into annotation_qualifier (annotation_qualifier_id, annotation_id, qualifier_id) values (?, ?, ?)";
  

  public static final String PREPARED_INSERT_PROTEIN_CLS = "insert into upl_protein_classification (protein_classification_id, protein_id, classification_id, created_by, creation_date) values (UIDs.nextval, ?, ?, ?, sysdate)";

  public static final String PREPARED_INSERT_EMPTY_CLS_COMMENT = "insert into upl_comments (comment_id, classification_id, remark, created_by, creation_date) values (?, ?, EMPTY_CLOB(), ?, sysdate)";
  public static final String PREPARED_INSERT_EMPTY_PROTEIN_COMMENT = "insert into upl_comments (comment_id, protein_id, remark, created_by, creation_date) values (?, ?, EMPTY_CLOB(), ?, sysdate)";
  public static final String PREPARED_UPDATE_COMMENT_CLOB = "update upl_comments set remark = ? where comment_id = ?";

  public static final String PREPARED_INSERT_CLS_RLTN = "insert into upl_classification_relation (classification_relationship_id, child_classification_id, parent_classification_id, created_by, creation_date) values(?, ?, ?, ?, sysdate)";

  public static final String PREPARED_OBSOLETE_CLS = "update upl_classification set obsolescence_date = sysdate, obsoleted_by = ? where classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_COMMENTS = "update upl_comments set obsolescence_date = sysdate, obsoleted_by = ? where classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_PROTEIN_CLS = "update upl_protein_classification set obsolescence_date = sysdate , obsoleted_by = ? where classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_CLS_RLTN = "update upl_classification_relation set obsolescence_date = sysdate , obsoleted_by = ? where child_classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_CLS_RLTN_FOR_PRNT = "update upl_classification_relation set obsolescence_date = sysdate , obsoleted_by = ? where child_classification_id = ? and parent_classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_EVIDENCE = "update upl_evidence set obsolescence_date = sysdate, obsoleted_by = ? where classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_FEATURE = "update upl_feature set obsolescence_date = sysdate, obsoleted_by = ? where classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_ANNOTATION_WITH_CLS_ID = "update annotation set obsolescence_date = sysdate, obsoleted_by = ? where classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_OBSOLETE_ANNOTATION = "update annotation set obsolescence_date = sysdate, obsoleted_by = ? where annotation_id = ? and obsolescence_date is null";


  public static final String PREPARED_INSERT_CURATION_STATUS = "insert into upl_curation_status (CURATION_STATUS_ID, CLASSIFICATION_ID, USER_ID, STATUS_TYPE_SID, CREATION_DATE) values (UIDs.nextval, ?, ?, ?, sysdate) ";
  public static final String PREPARED_UPDATE_CURATION_STATUS = "update upl_curation_status set creation_date = sysdate where curation_status_id = ?";

  public static final String PREPARED_BOOK_UNLOCK = "delete from upl_CURATION_STATUS where CLASSIFICATION_ID = ? AND USER_ID = ? and STATUS_TYPE_SID = ?";

  public static final String PREPARED_SEQUENCE_UNLOCK =
    "delete from upl_CURATION_STATUS " +
    "where PROTEIN_ID = ? AND USER_ID = ? and STATUS_TYPE_SID = ?";

  public static final String PREPARED_CLASSIFICATION_UNLOCK =
    "delete from upl_CURATION_STATUS " +
    "where CLASSIFICATION_ID = ? AND USER_ID = ? and STATUS_TYPE_SID = ?";

  public static final String PREPARED_SEQUENCE_LOCK =
    "insert into upl_curation_status (CURATION_STATUS_ID, PROTEIN_ID, USER_ID, STATUS_TYPE_SID, CREATION_DATE) " +
    "values (UIDs.nextval, ?, ?, ?, sysdate)";

  public static final String PREPARED_CLASSIFICATION_LOCK =
    "insert into upl_curation_status (CURATION_STATUS_ID, CLASSIFICATION_ID, USER_ID, STATUS_TYPE_SID, CREATION_DATE) " +
    "values (UIDs.nextval, ?, ?, ?, sysdate)";

  public static final String PREPARED_BOOK_LOCK = "insert into upl_curation_status(CURATION_STATUS_ID, STATUS_TYPE_SID, CLASSIFICATION_ID, USER_ID, CREATION_DATE) values(UIDS.NEXTVAL, ?, ?, ?, SYSDATE)";

  public static final String PREPARED_ADD_SEQUENCE_EVIDENCE =
    "insert into upl_evidence(evidence_id, evidence_type_sid, protein_id, evidence, is_editable, created_by, creation_date) " +
    "values (UIDS.nextval, ?, ?, ?, 1, ?, sysdate)";

  public static final String PREPARED_ADD_CLASSIFICATION_EVIDENCE =
    "insert into upl_evidence(evidence_id, evidence_type_sid, classification_id, evidence, is_editable, created_by, creation_date) " +
    "values (UIDS.nextval, ?, ?, ?, 1, ?, sysdate)";

  public static final String PREPARED_DELETE_SEQUENCE_EVIDENCE =
    "update evidence set obsolescence_date = sysdate, obsoleted_by = ? where protein_id = ? and evidence = ? and evidence_type_sid = ? and is_editable = 1 and obsolescence_date is null";

  public static final String PREPARED_DELETE_CLASSIFICATION_EVIDENCE =
    "update evidence set obsolescence_date = sysdate, obsoleted_by = ? where classification_id = ? and evidence = ? and evidence_type_sid = ? and is_editable = 1 and obsolescence_date is null";

  public static final String PREPARED_CARRYOVER_CLS_COMMENT = "insert into upl_comments (comment_id, classification_id, remark, created_by, creation_date)  select uids.nextval, ?, remark, created_by, creation_date  from upl_comments  where classification_id = ?  and obsolescence_date is null";
  public static final String PREPARED_CARRYOVER_CLS_EVIDENCE = "insert into upl_evidence (evidence_id, evidence_type_sid, classification_id, evidence, is_editable, created_by, creation_date) select uids.nextval, evidence_type_sid, ?, evidence, is_editable, created_by, creation_date from upl_evidence where classification_id = ? and obsolescence_date is null";
  public static final String PREPARED_CARRYOVER_CLS_FEATURE = "insert into upl_feature (feature_id, feature_type_sid, classification_id, primary_ext_id, primary_ext_acc, name, definition, mod_range, creation_date, obsolescence_date, created_by, obsoleted_by) select uids.nextval, feature_type_sid, ?, primary_ext_id, primary_ext_acc, name, definition, mod_range, creation_date, obsolescence_date, created_by, obsoleted_by from upl_feature where classification_id = ? and obsolescence_date is null";
  
//  public static final String PREPARED_INSERT_EVIDENCE_WITH_CONF = "insert into upl_evidence (evidence_id, evidence_type_sid, confidence_code_sid, classification_id, protein_id, evidence, is_editable, created_by, creation_date) " 
//                                                                + " SELECT UIDS.nextval, ?,  --evidence type  ?, -- confidence code  c.classification_id, --class id  p.protein_id  ?, --evidence value  1, --is_editabel  ?, --created by  sysdate " 
//                                                                + " FROM upl_classification c, upl_protein p " 
//                                                                + " WHERE c.classification_version_sid = ? and " 
//                                                                + " c.accession = ? and c.obsolescence_date is null " 
//                                                                + " and p.primary_ext_id = ? -- pass in a protein ext id " 
//                                                                + " and p.classification_version_sid = ? "
//                                                                + " and p.obsolescence_date is null;";  
                                                                
  public static final String PREPARED_INSERT_EVIDENCE_WITH_CONF = "insert into upl_evidence (evidence_id, evidence_type_sid, confidence_code_sid, classification_id, protein_id, evidence, is_editable, created_by, creation_date)\n"
                                                                + " values\n"
                                                                + "(UIDS.nextval,\n"
                                                                + " ?,  --evidence type sid\n"
                                                                + " ?, -- confidence code sid\n"
                                                                + " ?, -- classification_id,\n"
                                                                + " ?, -- protein_id\n"
                                                                + " ?, --evidence value\n"
                                                                + " 1, --is_editabel\n"
                                                                + " ?, --created by\n"
                                                                + " sysdate)";
                                                                
  public static final String PREPARED_OBSOLETE_EVIDENCE_WITH_CONF_CODE = " update upl_evidence\n"
                                                                       + " set obsolescence_date = sysdate, obsoleted_by = ? -- pass in user id\n"
                                                                       + " where\n"
                                                                       + " classification_id = ?\n"
                                                                       + " and protein_id = ?\n"
                                                                       + " and evidence_type_sid = ?\n"
                                                                       + " and confidence_code_sid = ? \n"
                                                                       + " and evidence = ?\n"
                                                                       + " and obsolescence_date is null";
}