 /* Copyright (C) 2019 University of Southern California
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


public class QueryString{

  /**
   * Constructor declaration
   *
   *
   * @see
   */
  public QueryString() {}

  // Append this clause to only retrieve records that were created before the release date
  public static final String  PREPARED_RELEASE_CLAUSE =
    " and ((tblName.OBSOLESCENCE_DATE is null  and tblName.CREATION_DATE < to_date(?, 'yyyy-mm-dd, hh:mi:ss'))  or tblName.OBSOLESCENCE_DATE > to_date(?, 'yyyy-mm-dd, hh:mi:ss'))";
  public static final String  RELEASE_CLAUSE =
    " and ((tblName.OBSOLESCENCE_DATE is null and tblName.CREATION_DATE < to_date('%1', 'yyyy-mm-dd, hh:mi:ss')) or tblName.OBSOLESCENCE_DATE > to_date('%1', 'yyyy-mm-dd, hh:mi:ss'))";
  public static final String  NON_RELEASE_CLAUSE = " and tblName.OBSOLESCENCE_DATE is null";
  public static final String  PREPARED_CREATION_DATE_CLAUSE = " and tblName.CREATION_DATE < to_date(?, 'yyyy-mm-dd, hh:mi:ss') ";
  public static final String  CREATION_DATE_CLAUSE = " and tblName.CREATION_DATE < to_date('%1', 'yyyy-mm-dd, hh:mi:ss') ";
  public static final String  FAMILY =
    "select * from CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = %2 and c.ACCESSION like '%3'";
  public static final String  PREPARED_USER_VALIDATION =
    "select * from USERS u where u.LOGIN_NAME = ? and u.PASSWORD = ?";
  public static final String  PREPARED_BOOK_LIST =
    "select c.ACCESSION, c.NAME from CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = ? and c.DEPTH = ?";
  public static final String  PREPARED_LOCKING_BOOK_LIST =
    "select c.ACCESSION, c.NAME from CLASSIFICATION_VERSION cv, CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = ? and c.OBSOLESCENCE_DATE is null and c.CLASSIFICATION_VERSION_SID = cv.CLASSIFICATION_VERSION_SID and c.DEPTH = ? and cv.RELEASE_DATE is null and c.CLASSIFICATION_ID not in (select cs.CLASSIFICATION_ID from CURATION_STATUS cs where cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID is not null ) ";
  public static final String  PREPARED_UNLOCKING_BOOK_LIST =
    "select c.ACCESSION, c.NAME, cs.STATUS_TYPE_SID, u.* from UPL_CURATION_STATUS cs, UPL_CLASSIFICATION c, users u  where c.CLASSIFICATION_VERSION_SID = ? and cs.USER_ID = ?  and cs.user_id = u.user_id AND cs.STATUS_TYPE_SID = ? AND c.DEPTH = ?  AND cs.CLASSIFICATION_ID = c.CLASSIFICATION_ID AND c.OBSOLESCENCE_DATE IS NULL order by c.accession";
  public static final String  PREPARED_CLSID_FOR_BOOK_LOCKED_BY_USER =
    "select cs.CLASSIFICATION_ID from CURATION_STATUS cs, CLASSIFICATION c where cs.USER_id = ? and cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.CLASSIFICATION_VERSION_SID = ? and c.ACCESSION = ?";
  public static final String  PREPARED_CLSID_FOR_BOOK_USER_LOCK =
    "select c.CLASSIFICATION_ID from CLASSIFICATION_VERSION cv, CLASSIFICATION c where cv.CLASSIFICATION_VERSION_SID = ? and cv.RELEASE_DATE is null and cv.CLASSIFICATION_VERSION_SID = c.CLASSIFICATION_VERSION_SID and c.OBSOLESCENCE_DATE is null and c.ACCESSION = ? and c.CLASSIFICATION_ID not in (select cs.CLASSIFICATION_ID from CURATION_STATUS cs where cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID is not null ) ";
  public static final String  PREPARED_CLSIDS_FOR_BOOKS_USER_LOCK =
    "select c.CLASSIFICATION_ID from CLASSIFICATION_VERSION cv, CLASSIFICATION c where cv.CLASSIFICATION_VERSION_SID = ? and cv.RELEASE_DATE is null and cv.CLASSIFICATION_VERSION_SID = c.CLASSIFICATION_VERSION_SID and c.OBSOLESCENCE_DATE is null and c.ACCESSION in (%1) and c.CLASSIFICATION_ID not in (select cs.CLASSIFICATION_ID from CURATION_STATUS cs where cs.STATUS_TYPE_SID = ? and cs.CLASSIFICATION_ID is not null ) ";
  
 
  public static final String  PREPARED_SEQID_FOR_LOCK = "select c.PROTEIN_ID "
          + "from CLASSIFICATION_VERSION cv, PROTEIN p " + "where cv.CLASSIFICATION_VERSION_SID = ? "
          + "  and cv.RELEASE_DATE is null " + "  and cv.CLASSIFICATION_VERSION_SID = p.CLASSIFICATION_VERSION_SID "
          + "  and p.OBSOLESCENCE_DATE is null and p.PRIMARY_EXT_ID = ? ";
  public static final String  PREPARED_CURATION_STATUS_ID =
    "select CURATION_STATUS_ID from CURATION_STATUS where CLASSIFICATION_ID = ? and USER_ID = ? and STATUS_TYPE_SID = ?";
  public static final String  PREPARED_TREE =
    "select TREE_TEXT from  CLASSIFICATION c, TREE_DETAIL td where c.CLASSIFICATION_VERSION_SID = ? and C.ACCESSION = ? and C.CLASSIFICATION_ID = td.CLASSIFICATION_ID";
  public static final String  PREPARED_PARENT_CLS =
    "select c.CLASSIFICATION_ID from classification c, classification_relationship cr where cr.CHILD_CLASSIFICATION_ID = ? and cr.OBSOLESCENCE_DATE is null and cr.PARENT_CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.OBSOLESCENCE_DATE is null";
  public static final String  PREPARED_CHILD_CLS =
    "select c.CLASSIFICATION_ID from classification c, classification_relationship cr where cr.PARENT_CLASSIFICATION_ID = ? and cr.OBSOLESCENCE_DATE is null and cr.CHILD_CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.OBSOLESCENCE_DATE is null";
  public static final String  CLS_VERSION_OLD =
    "select cv.VERSION, to_char(cv.RELEASE_DATE, 'yyyy-mm-dd, hh:mi:ss'), cv.CLASSIFICATION_VERSION_SID from CLASSIFICATION_VERSION cv, CLASSIFICATION_TYPE ct where ct.CLASSIFICATION_TYPE_SID = %1 and ct.CLASSIFICATION_TYPE_SID = cv.CLASSIFICATION_TYPE_SID";
    public static final String  CLS_VERSION =
      "select cv.VERSION, to_char(cv.RELEASE_DATE, 'yyyy-mm-dd, hh:mi:ss'), cv.CLASSIFICATION_VERSION_SID from CLASSIFICATION_VERSION cv, CLASSIFICATION_TYPE ct where ct.CLASSIFICATION_TYPE_SID = %1 and ct.CLASSIFICATION_TYPE_SID = cv.CLASSIFICATION_TYPE_SID  and cv.CLASSIFICATION_VERSION_SID = %2 ";
  
  public static final String  CLS_HIERARCHY =
    "select c1.ACCESSION, c1.NAME, c2.ACCESSION, cp.RANK, c1.CLASSIFICATION_ID from CLASSIFICATION c1, CLASSIFICATION c2, CLASSIFICATION_RELATIONSHIP cp where c1.CLASSIFICATION_VERSION_SID = %1 and c1.DEPTH is null and c1.CLASSIFICATION_ID = cp.CHILD_CLASSIFICATION_ID and cp.PARENT_CLASSIFICATION_ID = c2.CLASSIFICATION_ID and c2.CLASSIFICATION_VERSION_SID = %1";
  public static final String  CLS_ROOT =
    "select c.ACCESSION, c.NAME, c.CLASSIFICATION_ID from CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = %1 and c.DEPTH = 0";
  public static final String  PREPARED_CLS_FOR_SUBFAM =
    "select c1.CLASSIFICATION_ID, c2.NAME, c2.ACCESSION from CLASSIFICATION c1, CLASSIFICATION c2, CLASSIFICATION_RELATIONSHIP cp where c1.accession like ? and c1.CLASSIFICATION_ID = cp.CHILD_CLASSIFICATION_ID and cp.PARENT_CLASSIFICATION_ID = c2.CLASSIFICATION_ID and c1.CLASSIFICATION_VERSION_SID = ? and c2.CLASSIFICATION_VERSION_SID = ? and (c2.DEPTH is null) and cp.OBSOLESCENCE_DATE is null ";
  public static final String  PREPARED_PREVIOUS_SF_CAT =
    " select f.protein_id, m.* from family_to_sequence f, previous_info p, most_specific_category m where f.family_acc = ?   and f.classification_version_sid = ?   and f.protein_ext_acc = p.current_sequence_acc   and m.classification_version_sid = ?    and p.subfamily_id = m.subfamily_id order by f.PROTEIN_ID  ";

  // public static final String CLS_COMMENT = "select CLASSIFICATION_ID, REMARK from COMMENTS where (CLASSIFICATION_ID, CREATION_DATE) in (select c.CLASSIFICATION_ID, max(c.CREATION_DATE)  from COMMENTS c   where c.CLASSIFICATION_ID in (%1) and c.PROTEIN_ID is null %2 group by c.CLASSIFICATION_ID )";
  // public static final String PREPARED_CLS_COMMENT = "select CLASSIFICATION_ID, REMARK from COMMENTS where (CLASSIFICATION_ID, CREATION_DATE) in (select c.CLASSIFICATION_ID, max(c.CREATION_DATE) from   (select classification_id  from classification c where c.classification_version_sid = ? and c.depth = ? and c.accession like ?) e, COMMENTS c where e.classification_id = c.CLASSIFICATION_ID and c.PROTEIN_ID is null %1  group by c.CLASSIFICATION_ID )";
  public static final String  CLS_COMMENT =
    "select CLASSIFICATION_ID, REMARK from COMMENTS where (CLASSIFICATION_ID, CREATION_DATE) in (select c.CLASSIFICATION_ID, max(c.CREATION_DATE) from   (select classification_id  from classification c where c.classification_version_sid = %2 and c.depth = %3 and c.accession like '%4'  %1 ) e, COMMENTS c where e.classification_id = c.CLASSIFICATION_ID and c.PROTEIN_ID is null %1  group by c.CLASSIFICATION_ID )";

  // public static final String CLS_EVIDENCE = "select e.CLASSIFICATION_ID, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from EVIDENCE e, EVIDENCE_TYPE ep where e.CLASSIFICATION_ID in (%1) and e.PROTEIN_ID is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%2)";
  // public static final String PREPARED_CLS_EVIDENCE = "select e.CLASSIFICATION_ID, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from  (select classification_id  from classification c    where classification_version_sid = ?  and c.depth = ? and c.accession like ?  %2) cls, EVIDENCE e, EVIDENCE_TYPE ep where cls.classification_id = e.CLASSIFICATION_ID and e.PROTEIN_ID is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%1) ";
//  public static final String  CLS_EVIDENCE =
//    "select e.CLASSIFICATION_ID, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from  (select classification_id  from classification c    where classification_version_sid = %3  and c.depth = %4 and c.accession like '%5'  %2) cls, EVIDENCE e, EVIDENCE_TYPE ep where cls.classification_id = e.CLASSIFICATION_ID and e.PROTEIN_ID is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%1) ";

 public static final String  CLS_EVIDENCE = "select n.accession, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from node n, classification c, annotation a, EVIDENCE e, EVIDENCE_TYPE ep  where n.CLASSIFICATION_VERSION_SID = %1 and n.accession like '%2' and n.node_id = a.node_id and a.classification_id = c.classification_id and c.classification_version_sid = n.CLASSIFICATION_VERSION_SID and c.depth = %3 and c.classification_id = e.CLASSIFICATION_ID and e.PROTEIN_ID is null and e.annotation_id is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%4)";

  public static final String  PREPARED_TREE_CLOB = "select TREE_TEXT from TREE_DETAIL where TREE_ID = ?";
  public static final String  PREPARED_COMMENTS_CLOB = "select REMARK from Comments where COMMENT_ID = ?";

  // public static final String ASSOCIATED_SUBFAM_AND_SEQ = "select c.CLASSIFICATION_ID, c.ACCESSION, c.NAME,  pc.PROTEIN_CLASSIFICATION_ID, p.PROTEIN_ID, p.PRIMARY_EXT_ACC, p.SOURCE_ID  from CLASSIFICATION c, PROTEIN_CLASSIFICATION pc, PROTEIN p  where c.CLASSIFICATION_VERSION_SID = ? and c.DEPTH = ?  and c.ACCESSION like ? and c.CLASSIFICATION_ID = pc.CLASSIFICATION_ID and pc.PROTEIN_ID = p.PROTEIN_ID";
  public static final String  ASSOCIATED_SUBFAM_AND_SEQ =
    "select vpc.CLASSIFICATION_ID, vpc.ACCESSION, vpc.NAME,  vpc.PROTEIN_CLASSIFICATION_ID, vpc.PROTEIN_ID, vpc.PRIMARY_EXT_ID, vpc.SOURCE_ID from view_pro_cls vpc where vpc.CLASSIFICATION_VERSION_SID = %1 and vpc.ACCESSION like '%2'";

  // public static final String PREPARED_ASSOCIATED_SUBFAM_AND_SEQ = "select vpc.CLASSIFICATION_ID, vpc.ACCESSION, vpc.NAME,  vpc.PROTEIN_CLASSIFICATION_ID, vpc.PROTEIN_ID, vpc.PRIMARY_EXT_ACC, vpc.SOURCE_ID from view_pro_cls vpc where vpc.CLASSIFICATION_VERSION_SID = ? and vpc.ACCESSION like ?";
  // public static final String IDENTIFIER_TYPES = "select it.NAME from IDENTIFIER_TYPE it where it.IDENTIFIER_TYPE_SID in (%1)";
  // public static final String PROTEIN_IDENTIFIER = "select p.PROTEIN_ID, it.IDENTIFIER_TYPE_SID, i.NAME from PROTEIN p, IDENTIFIER i, IDENTIFIER_TYPE it where p.PROTEIN_ID in (%1) and p.PROTEIN_ID = i.protein_id and i.IDENTIFIER_TYPE_SID = it.IDENTIFIER_TYPE_SID and it.IDENTIFIER_TYPE_SID in (%2)";
  // public static final String PREPARED_PROTEIN_IDENTIFIER = "select p.PROTEIN_ID, it.IDENTIFIER_TYPE_SID, i.NAME from (select PROTEIN_ID from view_pro_cls where CLASSIFICATION_VERSION_SID = ?  and ACCESSION like ?) e, PROTEIN p, IDENTIFIER i, IDENTIFIER_TYPE it where e.protein_id = p.PROTEIN_ID   and p.PROTEIN_ID = i.protein_id   and i.IDENTIFIER_TYPE_SID = it.IDENTIFIER_TYPE_SID and it.IDENTIFIER_TYPE_SID in (%1)";
  public static final String  PROTEIN_IDENTIFIER =
    "select p.PROTEIN_ID, it.IDENTIFIER_TYPE_SID, i.NAME from (select PROTEIN_ID from view_pro_cls where CLASSIFICATION_VERSION_SID = %2  and ACCESSION like '%3') e, PROTEIN p, IDENTIFIER i, IDENTIFIER_TYPE it where e.protein_id = p.PROTEIN_ID   and p.PROTEIN_ID = i.protein_id   and i.IDENTIFIER_TYPE_SID = it.IDENTIFIER_TYPE_SID and it.IDENTIFIER_TYPE_SID in (%1)";
    
  public static final String PROTEIN_GENE = "select n.accession, g.primary_ext_acc, g.gene_symbol, g.gene_name from node n, protein_node pn, PROTEIN p, GENE g, GENE_PROTEIN gp where n.classification_version_sid = %1 and n.accession like '%2' and n.node_id = pn.node_id and pn.protein_id = p.protein_id and p.PROTEIN_ID = gp.protein_id and gp.gene_id = g.gene_id and gp.obsolescence_date is null";  



  // public static final String PROTEIN_COMMENT = "select c.PROTEIN_ID, c.REMARK  from COMMENTS c where (PROTEIN_ID, CREATION_DATE) in (select c.protein_ID, max(c.CREATION_DATE)  from COMMENTS c   where c.PROTEIN_ID in (%1)    and c.CLASSIFICATION_ID is null %2 group by c.PROTEIN_ID )";
  // public static final String PREPARED_PROTEIN_COMMENT = "select c.PROTEIN_ID, c.REMARK  from COMMENTS c where (PROTEIN_ID, CREATION_DATE) in (   select c.protein_ID, max(c.CREATION_DATE)    from (select PROTEIN_ID  from VIEW_PRO_CLS where CLASSIFICATION_VERSION_SID = ? and ACCESSION like ?) e,  COMMENTS c where e.protein_id = c.PROTEIN_ID and c.CLASSIFICATION_ID is null %1 group by c.PROTEIN_ID )";
  public static final String  PROTEIN_COMMENT =
    "select c.PROTEIN_ID, c.REMARK  from COMMENTS c where (PROTEIN_ID, CREATION_DATE) in (   select c.protein_ID, max(c.CREATION_DATE)    from (select PROTEIN_ID  from VIEW_PRO_CLS where CLASSIFICATION_VERSION_SID = %2 and ACCESSION like '%3') e,  COMMENTS c where e.protein_id = c.PROTEIN_ID and c.CLASSIFICATION_ID is null %1 group by c.PROTEIN_ID )";

  // public static final String PROTEIN_EVIDENCE = "select e.protein_id, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from EVIDENCE e, EVIDENCE_TYPE ep where e.PROTEIN_ID in (%1) and e.CLASSIFICATION_ID is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%2)";
  public static final String  PROTEIN_EVIDENCE =
    " select e.protein_id, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from (select PROTEIN_ID from view_pro_cls where CLASSIFICATION_VERSION_SID = %2 and ACCESSION like '%3') c, EVIDENCE e, EVIDENCE_TYPE ep where c.protein_id = e.PROTEIN_ID and e.CLASSIFICATION_ID is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%1) ";

  // public static final String PREPARED_PROTEIN_EVIDENCE = " select e.protein_id, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from (select PROTEIN_ID from view_pro_cls where CLASSIFICATION_VERSION_SID = ? and ACCESSION like ?) c, EVIDENCE e, EVIDENCE_TYPE ep where c.protein_id = e.PROTEIN_ID and e.CLASSIFICATION_ID is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%1) ";
  // public static final String XLINKS = "select e.protein_id, ep.TYPE, e.EVIDENCE from EVIDENCE e, EVIDENCE_TYPE ep where e.PROTEIN_ID in (%1) and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.IS_EDITABLE = 0 and e.EVIDENCE_TYPE_SID in (%2)";
  public static final String  ORGANISM_LOOKUP =
    "select ps.SOURCE_ID, o.ORGANISM, o.CONVERSION from PROTEIN_SOURCE ps, ORGANISM o where ps.ORGANISM_ID = o.ORGANISM_ID";
  public static final String  USER_LOOKUP = "select USER_ID, PRIVILEGE_RANK from users";
  public static final String  UID_GENERATOR = "select uids.nextval from dual";
  public static final String  USER_PRIVILEGE = "select privilege_rank from users where user_id = ? ";
  public static final String  SEQUENCE_CURRENT_LOCKING_STATUS =
    "select user_id from curation_status where protein_id = ? and status_type_sid = ?";
  public static final String  CLASSIFICATION_CURRENT_LOCKING_STATUS =
    "select user_id from curation_status where classification_id = ? and status_type_sid = ?";
  public static final String  PROTEIN_INFO =
    "select protein_id from classification_version cv, protein p "
    + "where cv.release_date is null and cv.obsolescence_date is null "
    + "  and cv.classification_version_sid = ? and cv.classification_version_sid = p.classification_version_sid "
    + "  and p.primary_ext_id = ? and p.obsolescence_date is null";
  public static final String  CLASSIFICATION_INFO =
    "select classification_id from classification_version cv, classification c "
    + "where cv.release_date is null and cv.obsolescence_date is null "
    + "  and cv.classification_version_sid = ? and cv.classification_version_sid = c.classification_version_sid "
    + "  and c.accession = ? and c.obsolescence_date is null";
  public static final String  SEQUENCE_EVIDENCE = "select et.type, e.evidence_id, e.evidence, e.created_by "
          + "from evidence e, evidence_type et "
          + "where e.protein_id = ? and e.obsolescence_date is null and e.is_editable = 1 "
          + "  and e.evidence_type_sid = et.evidence_type_sid";
  public static final String  CLASSIFICATION_EVIDENCE = "select et.type, e.evidence_id, e.evidence, e.created_by "
          + "from evidence e, evidence_type et "
          + "where e.classification_id = ? and e.obsolescence_date is null and e.is_editable = 1 "
          + "  and e.evidence_type_sid = et.evidence_type_sid";

  public static final String  PREPARED_FAMILY_SUBFAMILY_LIST =
    "select c.ACCESSION, c.NAME from CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = ? and (c.DEPTH = ? or c.DEPTH = ?)";

  public static final String  PREPARED_FAMILY_SUBFAMILY=
    "select c.ACCESSION, c.NAME from CLASSIFICATION c where c.CLASSIFICATION_VERSION_SID = ? and (c.DEPTH = ? or c.DEPTH = ?) and c.ACCESSION like ?";

  public static final String  PUBLIC_INTERPRO=
    "SELECT p.protein_id, c.ACCESSION interpro_acc, c.NAME interpro_name FROM view_pro_cls p, protein_classification pc, classification c WHERE p.accession LIKE '%1' AND p.classification_version_sid = %2 AND p.PROTEIN_ID = pc.protein_id AND pc.classification_id = c.classification_id AND c.classification_version_sid = %3 AND c.obsolescence_date IS NULL";
  
  public static final String PREPARED_PANTHER_SF_INTERPRO=
    "SELECT f.protein_id, c.name interpro_name FROM family_to_sequence f, previous_UPL_info i, classification_relationship r, classification c WHERE f.FAMILY_ACC = ? AND f.CLASSIFICATION_VERSION_SID = ? AND f.PROTEIN_ID = i.CURRENT_UPL_SEQUENCE_ID AND i.SUBFAMILY_ID = r.CHILD_CLASSIFICATION_ID AND r.RELATIONSHIP_TYPE_SID = ? AND r.obsolescence_date IS NULL AND r.parent_classification_id = c.classification_id AND c.classification_version_sid = ? AND c.obsolescence_date IS NULL";
    
  public static final String PREPARED_PANTHER_FAM_INTERPRO=
    "SELECT f.protein_id, c.name interpro_name FROM family_to_sequence f, previous_UPL_info i, classification_relationship r0, classification_relationship r, classification c WHERE f.FAMILY_ACC = ? AND f.CLASSIFICATION_VERSION_SID = ? AND f.PROTEIN_ID = i.CURRENT_UPL_SEQUENCE_ID AND i.SUBFAMILY_ID = r0.child_classification_id AND  r0.obsolescence_date IS NULL AND r0.parent_classification_id = r.child_classification_id AND r.RELATIONSHIP_TYPE_SID = ? AND r.obsolescence_date IS NULL  AND r.parent_classification_id = c.classification_id AND c.classification_version_sid = ? AND c.obsolescence_date IS NULL";    

/*    
  public static final String PREPARED_CLS_EVIDENCE_SEQUENCE="SELECT c1.classification_id,  category_id, c1.accession, category_acc, \n"
         + " c0.accession,\n"
         + " p.protein_id, p.primary_ext_acc,\n"
         + " p.primary_ext_id, cc.confidence_code, e.evidence, et.type\n"
         + " FROM classification c0, protein_classification pc, protein p,  classification_relationship r, classification c1, evidence e,\n"
         + "     evidence_type et, confidence_code cc\n"
         + " WHERE c0.classification_version_sid = ?  -- upl version id\n"
         + " AND c0.accession like ? \n"
         + " AND c0.obsolescence_date is null\n"
         + " AND c0.classification_id = pc.classification_id\n"
         + " AND pc.obsolescence_date is null\n"
         + " AND pc.protein_id = p.protein_id\n"
         + " AND p.classification_version_sid = ? \n"
         + " AND p.obsolescence_date is null\n"
         + " AND c0.classification_id = r.child_classification_id\n"
         + " AND r.obsolescence_date is null\n"
         + " AND r.parent_classification_id = c1.classification_id\n"
         + " AND c1.classification_version_sid = ?  -- upl version id\n"
         + " AND c1.depth is null\n"
         + " AND c1.obsolescence_date is null\n"
         + " AND c1.classification_id = e.classification_id (+)\n"
         + " AND e.protein_id = p.protein_id\n"
         + " AND e.obsolescence_date (+) is null\n"
         + " AND e.evidence_type_sid = et.evidence_type_sid\n"
         + " AND e.confidence_code_sid = cc.confidence_code_sid";  
*/

 public static final String PREPARED_CLS_EVIDENCE_SEQUENCE="SELECT c1.classification_id  category_id, c1.accession category_acc, \n"
        + " c0.accession,\n"
        + " p.protein_id, p.primary_ext_acc,\n"
        + " p.primary_ext_id, cc.confidence_code, e.evidence, et.type, cc.confidence_code_sid \n"
        + " FROM classification c0, protein_classification pc, protein p,  classification_relationship r, classification c1, evidence e,\n"
        + "     evidence_type et, confidence_code cc\n"
        + " WHERE c0.classification_version_sid = ?  -- upl version id\n"
        + " AND c0.accession like ? \n"
        + " AND c0.obsolescence_date is null\n"
        + " AND c0.classification_id = pc.classification_id\n"
        + " AND pc.obsolescence_date is null\n"
        + " AND pc.protein_id = p.protein_id\n"
        + " AND p.classification_version_sid = ? \n"
        + " AND p.obsolescence_date is null\n"
        + " AND c0.classification_id = r.child_classification_id\n"
        + " AND r.obsolescence_date is null\n"
        + " AND r.parent_classification_id = c1.classification_id\n"
        + " AND c1.classification_version_sid = ?  -- upl version id\n"
        + " AND c1.depth is null\n"
        + " AND c1.obsolescence_date is null\n"
        + " AND c1.classification_id = e.classification_id (+)\n"
        + " AND e.protein_id = p.protein_id\n"
        + " AND e.obsolescence_date (+) is null\n"
        + " AND e.evidence_type_sid = et.evidence_type_sid\n"
        + " AND e.confidence_code_sid = cc.confidence_code_sid";  
    
  /*
    Returns available confindence code evidence type
    */
    public final static String GETALLEVIDENCETYPES =
    "select confidence_code_sid, confidence_code, name, evidence_requirement, description from confidence_code " ;
    
// Does not work if there is no corresponding entry in the curation status table    
//    public static final String GET_ALL_BOOKS = "select c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name, u.group_name from classification c, curation_status cs, curation_status_type cst, users u where c.depth = %1 and c.CLASSIFICATION_VERSION_SID = %2 and c.CLASSIFICATION_ID = cs.CLASSIFICATION_ID and cs.STATUS_TYPE_SID = cst.STATUS_TYPE_SID and cs.USER_ID = u.user_id (+) ";    


    public static final String GET_LIST_OF_BOOKS = " select c.CLASSIFICATION_ID, c.accession, c.name from classification c where  c.depth = %1 and c.CLASSIFICATION_VERSION_SID = %2 ";
    
    public static final String GET_STATUS_USER_INFO = " select c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name, u.group_name from classification c, curation_status cs, curation_status_type cst, users u where c.depth = %1 and c.CLASSIFICATION_VERSION_SID = %2 and c.CLASSIFICATION_ID  = cs.CLASSIFICATION_ID and cs.STATUS_TYPE_SID = cst.STATUS_TYPE_SID and cs.USER_ID = u.user_id ";
    
//    public static final String PREPARED_SEARCH_BOOKS_BY_GENE_NAME = "select distinct c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name from gene g, gene_protein gp, view_protein_classification vpc, classification_relationship cr, classification c, curation_status cs, curation_status_type cst, users u where g.gene_name like ? and g.obsoleted_by is null and g.GENE_ID = gp.GENE_ID and gp.protein_id = vpc.PROTEIN_ID and vpc.CLASSIFICATION_ID = cr.CHILD_CLASSIFICATION_ID and cr.PARENT_CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.DEPTH = ? and c.CLASSIFICATION_VERSION_SID = ? and c.CLASSIFICATION_ID = cs.CLASSIFICATION_ID (+) and cs.CURATION_STATUS_ID = cst.STATUS_TYPE_SID (+) and cs.USER_ID = u.USER_ID (+)";
//
//
//    public static final String PREPARED_SEARCH_BOOKS_BY_GENE_SYMBOL = "select distinct c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name   from gene g, gene_protein gp, view_protein_classification vpc, classification_relationship cr, classification c, curation_status cs, curation_status_type cst, users u where g.gene_symbol like ? and g.obsoleted_by is null and g.GENE_ID = gp.GENE_ID and gp.protein_id = vpc.PROTEIN_ID and vpc.CLASSIFICATION_ID = cr.CHILD_CLASSIFICATION_ID and cr.PARENT_CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.DEPTH = ? and c.CLASSIFICATION_VERSION_SID = ? and c.CLASSIFICATION_ID = cs.CLASSIFICATION_ID (+) and cs.CURATION_STATUS_ID = cst.STATUS_TYPE_SID (+) and cs.USER_ID = u.USER_ID (+)";
//
//
//    public static final String PREPARED_SEARCH_BOOKS_BY_GENE_PRIMARY_EXT_ACC = "select distinct c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name   from gene g, gene_protein gp, view_protein_classification vpc, classification_relationship cr, classification c, curation_status cs, curation_status_type cst, users u where g.PRIMARY_EXT_ACC like ? and g.obsoleted_by is null and g.GENE_ID = gp.GENE_ID and gp.protein_id = vpc.PROTEIN_ID and vpc.CLASSIFICATION_ID = cr.CHILD_CLASSIFICATION_ID and cr.PARENT_CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.DEPTH = ? and c.CLASSIFICATION_VERSION_SID = ? and c.CLASSIFICATION_ID = cs.CLASSIFICATION_ID (+) and cs.CURATION_STATUS_ID = cst.STATUS_TYPE_SID (+) and cs.USER_ID = u.USER_ID (+)";
//    
//    
//    public static final String PREPARED_SEARCH_BOOKS_BY_PROTEIN_PRIMARY_EXT_ID = "select distinct c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name   from view_protein_classification vpc, classification_relationship cr, classification c, curation_status cs, curation_status_type cst, users u where vpc.PRIMARY_EXT_ID like ? and vpc.CLASSIFICATION_ID = cr.CHILD_CLASSIFICATION_ID  and cr.PARENT_CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.DEPTH = ? and c.CLASSIFICATION_VERSION_SID = ? and c.CLASSIFICATION_ID = cs.CLASSIFICATION_ID (+) and cs.CURATION_STATUS_ID = cst.STATUS_TYPE_SID (+) and cs.USER_ID = u.USER_ID (+) order by c.ACCESSION";
//    
//    public static final String PREPARED_SEARCH_BOOKS_BY_DEFINITION = "select distinct c.ACCESSION, c.NAME, u.NAME, u.EMAIL, cst.STATUS, cst.STATUS_TYPE_SID, u.login_name  from view_protein_classification vpc, classification_relationship cr, classification c, curation_status cs, curation_status_type cst, users u where (vpc.PROTEIN_ID) in (select protein_id from identifier i where i.identifier_type_sid = 3 and i.name like ?) and vpc.CLASSIFICATION_ID = cr.CHILD_CLASSIFICATION_ID  and cr.PARENT_CLASSIFICATION_ID = c.CLASSIFICATION_ID and c.DEPTH = ? and c.CLASSIFICATION_VERSION_SID = ? and c.CLASSIFICATION_ID = cs.CLASSIFICATION_ID (+) and cs.CURATION_STATUS_ID = cst.STATUS_TYPE_SID (+) and cs.USER_ID = u.USER_ID (+) order by c.ACCESSION";


    public static final String PREPARED_ALL_NODES_IN_TREE = "select distinct n.accession from node n where n.classification_version_sid = ? and n.accession like ? ";

    public static final String PREPARED_AN_SF_NODE = "select c.classification_id, c.accession, c.name, n.node_id, n.accession, a.annotation_id from node n, annotation a, annotation_type at, classification c where n.classification_version_sid = ?  and n.accession like ? and n.node_id = a.node_id and a.annotation_type_id = at.annotation_type_id and at.annotation_type = ? and a.classification_id = c.classification_id and c.classification_version_sid = n.classification_version_sid ";
    
        public static final String PREPARED_AN_PROTEIN_ID = "select n.accession, p.protein_id, g.PRIMARY_EXT_ACC, p.source_id from node n, protein_node pn, protein p, gene g, gene_node gn where n.classification_version_sid = ? and n.accession like ? and n.node_id = gn.node_id and gn.gene_id = g.gene_id and n.node_id = pn.node_id and pn.protein_id = p.protein_id ";

    
    
    public static final String PREPARED_AN_IDENTIFIER = "select n.accession, it.IDENTIFIER_TYPE_SID, i.NAME from node n, protein_node pn, protein p, identifier i, identifier_type it where n.classification_version_sid = ? and n.accession like ? and n.node_id = pn.node_id and pn.protein_id = p.protein_id and p.PROTEIN_ID = i.protein_id and i.IDENTIFIER_TYPE_SID = it.IDENTIFIER_TYPE_SID and it.IDENTIFIER_TYPE_SID in (%1)";
    
    public static final String IDENTIFIER_AN = "select n.accession, it.IDENTIFIER_TYPE_SID, i.NAME, it.name as identifier_type from node n, protein_node pn, protein p, identifier i, identifier_type it where n.classification_version_sid = %1 and n.accession like '%2' and n.node_id = pn.node_id and pn.protein_id = p.protein_id and p.PROTEIN_ID = i.protein_id and i.IDENTIFIER_TYPE_SID = it.IDENTIFIER_TYPE_SID and it.IDENTIFIER_TYPE_SID in (%3)";

    
    public static final String NEW_PROTEIN_EVIDENCE =  "select  n.accession, ep.TYPE, e.EVIDENCE, e.IS_EDITABLE, e.CREATED_BY from node n, protein_node pn, protein p, EVIDENCE e, EVIDENCE_TYPE ep where n.classification_version_sid = %1 and n.accession like '%2' and n.node_id = pn.node_id and pn.protein_id = p.protein_id and p.PROTEIN_ID = e.PROTEIN_ID and e.CLASSIFICATION_ID is null and e.ANNOTATION_ID is null and e.EVIDENCE_TYPE_SID = ep.EVIDENCE_TYPE_SID and e.EVIDENCE_TYPE_SID in (%3)";

    //public static final String PREPARED_GO_ANNOTATION = "select node.accession as accession, p.PRIMARY_EXT_ID, c.accession as go_accession, c.name as go_name, DECODE(ctt.TERM_NAME, 'molecular_function', 'F','cellular_component', 'C','biological_process','P') as aspect, e.EVIDENCE, cc.CONFIDENCE_CODE, q.QUALIFIER from node , protein_node pn, protein p, protein_classification pc, classification c, evidence e, confidence_code cc, classification_term_type ctt, pc_qualifier pcq, qualifier q where node.classification_version_sid = ? and node.accession like ? and node.node_id = pn.node_id and pn.protein_id = p.protein_id and p.protein_id = pc.PROTEIN_ID and pc.classification_id = c.classification_id and c.CLASSIFICATION_VERSION_SID = %1 and c.TERM_TYPE_SID = ctt.TERM_TYPE_SID and pc.protein_classification_id = e.protein_classification_id  and e.CONFIDENCE_CODE_SID = cc.CONFIDENCE_CODE_SID  and cc.CONFIDENCE_CODE_SID in (%2) and pc.PROTEIN_CLASSIFICATION_ID = pcq.PROTEIN_CLASSIFICATION_ID (+) and pcq.QUALIFIER_ID = q.QUALIFIER_ID (+) ";

    public static final String PREPARED_GO_ANNOTATION = "select node.accession as accession, p.PRIMARY_EXT_ID, c.accession as go_accession, c.name as go_name, (case when ctt.term_name='molecular_function' THEN 'F' when ctt.term_name='cellular_component'THEN 'C' when ctt.term_name='biological_process' THEN 'P' END) as aspect, e.EVIDENCE, cc.CONFIDENCE_CODE, q.QUALIFIER from node , protein_node pn, protein p, classification c, evidence e, confidence_code cc ,\n" +
        "     classification_term_type ctt, pc_qualifier pcq\n" +
        "     RIGHT OUTER JOIN protein_classification pc on pc.protein_classification_id = pcq.protein_classification_id\n" +
        "     RIGHT OUTER JOIN qualifier q on pcq.qualifier_id = q.qualifier_id" +
        "     where node.classification_version_sid = ? and node.accession like ? and node.node_id = pn.node_id and pn.protein_id = p.protein_id and p.protein_id = pc.PROTEIN_ID and pc.classification_id = c.classification_id and c.CLASSIFICATION_VERSION_SID = %1 and c.TERM_TYPE_SID = ctt.TERM_TYPE_SID and pc.protein_classification_id = e.protein_classification_id  and e.CONFIDENCE_CODE_SID = cc.CONFIDENCE_CODE_SID  and cc.CONFIDENCE_CODE_SID in (%2)  ";
    
    
    public static final String PANTHER_GO_SLIM_HIERARCHY = "select c1.ACCESSION as child_Accession, c1.NAME, c2.ACCESSION as parent_Accession, cp.RANK, rt.name as relationship, c1.CLASSIFICATION_ID as child_id, c2.CLASSIFICATION_ID as parent_id from CLASSIFICATION c1, CLASSIFICATION c2, CLASSIFICATION_RELATIONSHIP cp, relationship_type rt where c1.CLASSIFICATION_VERSION_SID = %1 and c1.DEPTH is null and c1.CLASSIFICATION_ID = cp.CHILD_CLASSIFICATION_ID and cp.PARENT_CLASSIFICATION_ID = c2.CLASSIFICATION_ID and c2.CLASSIFICATION_VERSION_SID = c1.CLASSIFICATION_VERSION_SID and cp.RELATIONSHIP_TYPE_SID in (%2) and cp.RELATIONSHIP_TYPE_SID = rt.RELATIONSHIP_TYPE_SID ";
    
    
    public static final String PANTHER_GO_SLIM_ROOT_PART1 = "select distinct c.accession, c.name, r.rank, rt.name as relationship from classification_relationship r, classification c, relationship_type rt where c.CLASSIFICATION_VERSION_SID = %1  and r.relationship_type_sid in (%2) and r.RELATIONSHIP_TYPE_SID = rt.RELATIONSHIP_TYPE_SID and r.parent_classification_id = c.classification_id and r.parent_classification_id not in (%3 ) ";
    
    public static final String PANTHER_GO_SLIM_ROOT_PART2 = "select cr.child_classification_id from classification_relationship cr, classification c where c.classification_version_sid = %1 and c.classification_id = cr.child_classification_id and cr.relationship_type_sid in (%2) ";
    
    public static final String PROTEIN_CLASS_HIERARCHY = "select c1.ACCESSION as child_accession, c1.NAME, c2.ACCESSION as parent_accession, cp.RANK, c1.CLASSIFICATION_ID as child_id, c2.CLASSIFICATION_ID as parent_id from CLASSIFICATION c1, CLASSIFICATION c2, CLASSIFICATION_RELATIONSHIP cp where c1.CLASSIFICATION_VERSION_SID = %1 and c1.DEPTH is null and c1.CLASSIFICATION_ID = cp.CHILD_CLASSIFICATION_ID and cp.PARENT_CLASSIFICATION_ID = c2.CLASSIFICATION_ID and c2.CLASSIFICATION_VERSION_SID = %1 ";
    
    public static final String PROTEIN_CLASS_ROOT_PART_1 = "select distinct c.accession, c.name, r.rank  from classification_relationship r, classification c where c.CLASSIFICATION_VERSION_SID = %1 and r.parent_classification_id = c.classification_id and r.parent_classification_id not in ( %2 ) ";
    
    public static final String PROTEIN_CLASS_ROOT_PART_2 = "select cr.child_classification_id from classification_relationship cr, classification c where c.CLASSIFICATION_VERSION_SID = %1 and c.CLASSIFICATION_ID = cr.child_classification_id ";
    
    
    public static final String SEARCH_NODE_GENE_SYMBOL = "select n.accession as  accession from gene g, gene_node gn, node n, node_type nt where lower(g.gene_symbol) like '%1' and g.GENE_ID = gn.GENE_ID  and gn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
  
    public static final String SEARCH_NODE_GENE_PRIMARY_EXT_ACC = "select n.accession as accession from gene g, gene_node gn, node n, node_type nt where g.PRIMARY_EXT_ACC like '%1' and g.GENE_ID = gn.GENE_ID  and gn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
    
    public static final String SEARCH_NODE_PROTEIN_PRIMARY_EXT_ID = "select n.accession as accession from protein p,  protein_node pn, node n, node_type nt where p.PRIMARY_EXT_ID like '%1' and p.protein_id = pn.protein_id and pn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
    
    public static final String SEARCH_NODE_DEFINITION = "select n.accession as accession from identifier i, protein p,  protein_node pn, node n, node_type nt where lower(i.name)  like '%1' and i.IDENTIFIER_TYPE_SID = 3 and i.primary_object_id = p.protein_id and p.protein_id = pn.protein_id and pn.node_id = n.node_id and n.node_type_id = nt.NODE_TYPE_ID and nt.NODE_TYPE = 'LEAF' and n.CLASSIFICATION_VERSION_SID = %2";
    
    public static final String SEARCH_PART2 = " and c.accession in (%1)";
    
    public static final String SEARCH_UNLOCKED_UNCURATED = " select c.ACCESSION  from classification c, curation_status cs, curation_status_type cst where c.depth = %1 and c.CLASSIFICATION_VERSION_SID = %2 and c.CLASSIFICATION_ID  = cs.CLASSIFICATION_ID and cs.STATUS_TYPE_SID = cst.STATUS_TYPE_SID  and cst.STATUS_TYPE_SID not in (%3) ";
 
    public static final String NOT_QUALIFIER_ID = "select * from qualifier where qualifier = 'NOT'";   
    
    public static final String ANNOTATION_TYPE_INFO = "select * from annotation_type";
    
    public static final String ALL_ANNOTATIONS = "select * from annotation a, node n where n.CLASSIFICATION_VERSION_SID = %1 and n.accession like '%2' and n.node_id = a.node_id ";
    
    public static final String PREPARED_ASSOCIATED_CLS = 
            "select n.accession as node_accession,  n.classification_version_sid, c.ACCESSION, c.name, (CASE WHEN ctt.term_name='molecular_function' THEN 'MF' WHEN ctt.term_name='cellular_component' THEN 'CC' WHEN ctt.term_name='biological_process' THEN 'BP' ELSE 'PC' END) as aspect,  q.qualifier, a.CREATED_BY, a.creation_date\n" +
            "from  node n   JOIN annotation a on   n.node_id = a.NODE_ID\n" +
            "  JOIN classification c on a.CLASSIFICATION_ID = c.classification_id \n" +
            "  JOIN annotation_type at   on a.ANNOTATION_TYPE_ID = at.ANNOTATION_TYPE_ID  \n" +
            "  LEFT OUTER JOIN classification_term_type ctt on  c.TERM_TYPE_SID = ctt.TERM_TYPE_SID\n" +
            "  LEFT OUTER JOIN annotation_qualifier aq on a.ANNOTATION_ID = aq.annotation_id\n" +
            "  LEFT OUTER JOIN qualifier q on aq.qualifier_id = q.qualifier_id \n" +
            "where n.CLASSIFICATION_VERSION_SID = ? and n.accession like ? and at.annotation_type in ('GO', 'PC') ";
    


    public static final String PANTHER_TREE_STRUCTURE = "select n1.accession as child_accession, n1.node_id as child_id, n2.accession as parent_accession, n2.node_id as parent_id from node n1, node n2, node_relationship nr where n1.CLASSIFICATION_VERSION_SID = %1 and n1.CLASSIFICATION_VERSION_SID = n2.CLASSIFICATION_VERSION_SID and n1.accession like '%2' and n1.node_id = nr.CHILD_NODE_ID and nr.parent_node_id = n2.NODE_ID ";

    public static final String NODE_INFO =  "select n.accession, n2.accession as parent, n.PUBLIC_ID, n.branch_length, nt.node_type, et.event_type, g.PRIMARY_EXT_ACC from node n, node_type nt, event_type et, gene g, gene_node gn, node_relationship nr, node n2 where n.accession like '%1' and n.CLASSIFICATION_VERSION_SID = %2 and n.node_type_id = nt.NODE_TYPE_ID (+) and n.EVENT_TYPE_ID = et.EVENT_TYPE_ID (+) and n.node_id = gn.node_id (+) and gn.gene_id = g.gene_id (+) and n.node_id = nr.child_node_id (+) and nr.parent_node_id = n2.node_id (+) ";
    
    public static final String NODE_SEARCH = "select n.accession from node n where (n.accession = '%1' OR n.public_id = '%1') and n.classification_version_sid = %2  and n.obsolescence_date is null";

    public static final String NODE_ACC_PUBLIC_ID_SEARCH = "select n.accession, n.public_id from node n where (n.accession = '%1' OR n.public_id = '%1') and n.classification_version_sid = %2  and n.obsolescence_date is null";

    public static final String ANNOTATION_NODE_PUBLIC_ID_LOOKUP = "select n.accession, n.public_id from node n where n.accession like '%1:%' and n.classification_version_sid = %2 and n.obsolescence_date is null";
    
    public static final String QUERY_ORGANISM = "select * from organism where classification_version_sid = %1";
    
}
