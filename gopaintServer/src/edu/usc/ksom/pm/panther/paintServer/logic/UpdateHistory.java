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
package edu.usc.ksom.pm.panther.paintServer.logic;

import com.sri.panther.paintCommon.User;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.database.DBConnectionPool;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.datamodel.ClassificationVersion;
import com.sri.panther.paintServer.util.ConfigFile;
import com.sri.panther.paintServer.util.ReleaseResources;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class UpdateHistory {   
    public static final String RELEASE_ID = "release_id";
    public static final String ID = "id";
    public static final String DATE_CREATION = "creation_date";
    public static final String USER_CREATED = "created_by";    
    public static final String DATE_OBSOLESCENCE = "obsolescence_date"; 
    public static final String USER_OBSOLETED = "obsoleted_by";
    
    public enum Operation {
        // Define these in the order we want to order them
        NODE_ANNOTATION("Node annotation"),
        NODE_GRAFT_PRUNE("Node grafting or pruning"),        
        FAMILY_COMMENT("Family comment"), 
        FAMILY_STATUS("Famiy status"); 
        
        public final String label;
        private Operation(String label) {
            this.label = label;
        }
    };
    
    
    public static final String QUERY_NODE_ANNOTATION = "select distinct n.classification_version_sid, n.accession , n.public_id, a.created_by , a.creation_date, a.obsoleted_by , a.obsolescence_date, cc.confidence_code \n" +
                                                            "from node n, paint_annotation a, annotation_type at, paint_evidence pe, confidence_code cc \n" +
                                                            "where n.accession like '%1'\n" +
                                                            "and n.node_id = a.node_id \n" +
                                                            "and a.annotation_type_id = at.annotation_type_id \n" +
                                                            "and at.annotation_type = 'GO_PAINT'\n" +
                                                            "and a.annotation_id = pe.annotation_id\n" +
                                                            "and pe.confidence_code_sid = cc.confidence_code_sid ";
    
    public static final String QUERY_NODE_GRAFT_PRUNE = "select distinct n.classification_version_sid, n.accession , n.public_id, a.created_by , a.creation_date, a.obsoleted_by , a.obsolescence_date\n" +
                                                            "from node n, paint_annotation a, annotation_type at\n" +
                                                            "where n.accession like '%1'\n" +
                                                            "and n.node_id = a.node_id \n" +
                                                            "and a.annotation_type_id = at.annotation_type_id \n" +
                                                            "and at.annotation_type like 'PAINT_PRUNED'";
    
    public static final String QUERY_FAMILY_COMMENT = "select c.classification_version_sid, ct.created_by, ct.creation_date, ct.obsoleted_by , ct.obsolescence_date, ct.remark \n" +
                                                            "from classification c, comments ct\n" +
                                                            "where c.depth = 5\n" +
                                                            "and c.accession like '%1'\n" +
                                                            "and c.classification_id = ct.classification_id ";
    
    public static final String QUERY_FAMILY_STATUS = "select c.classification_version_sid, cs.creation_date, cs.user_id, cst.status \n" +
                                                        "from classification c, curation_status cs, curation_status_type cst \n" +
                                                        "where c.depth = 5\n" +
                                                        "and c.accession like '%1'\n" +
                                                        "and c.classification_id = cs.classification_id \n" +
                                                        "and cs.status_type_sid = cst.status_type_sid ";
    
    
    
    public static final String WHITE_SPACE = "\\s+";
    public static final String UNDERSCORE = "_";    
    public static final String WILDCARD = "%";
    public static final String QUERY_PARAMETER_1 = "%1";    
    public static final String COLUMN_CLASSIFICATION_VERSION_SID = "classification_version_sid";
    public static final String COLUMN_ACCESSION = "accession";    
    public static final String COLUMN_PUBLIC_ID = "public_id";    
    public static final String COLUMN_CREATED_BY = "created_by";
    public static final String COLUMN_CREATION_DATE = "creation_date";
    public static final String COLUMN_OBSOLETED_BY = "obsoleted_by";
    public static final String COLUMN_OBSOLESCENCE_DATE = "obsolescence_date";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_CONFIDENCE_CODE = "confidence_code";
    public static final String COLUMN_REMARK = "remark";
    public static final String COLUMN_STATUS = "status";
       
    public static final String ELEMENT_SEARCH = "search";
    public static final String ELEMENT_PARAMETERS = "parameters";
    public static final String ELEMENT_OPERATION_LIST = "operation_list";
    public static final String ELEMENT_OPERATION = "operation";
    public static final String ELEMENT_RELEASE = "release";    
    public static final String ELEMENT_TYPE = "type";
    public static final String ELEMENT_ID = "id";    
    public static final String ELEMENT_CREATED_BY = "created_by";    
    public static final String ELEMENT_CREATION_DATE = "creation_date";
    public static final String ELEMENT_OBSOLETED_BY = "obsoleted_by";    
    public static final String ELEMENT_OBSOLESCENCE_DATE = "obsolescence_date";
    public static final String ELEMENT_CONFIDENCE_CODE = "confidence_code"; 
    public static final String ELEMENT_COMMENT = "comment";  
    public static final String ELEMENT_STATUS = "status";  
    
    public static final String DB_STR_CURRENT = ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID);
    public static DataIO dataIO = DataAccessManager.getInstance().getDataIO();
    public static ClassificationVersion currentVersion = null;    
    public static final HashMap<String, ClassificationVersion> CLS_VERSION_SID_LOOKUP = initClsVersions();
    private static UserManager USER_MANAGER = UserManager.getInstance();

    
    public static final java.text.SimpleDateFormat DATE_FORMATTER = new java.text.SimpleDateFormat("yyyy-MM-dd");    
    
    public static HashMap<String, ClassificationVersion> initClsVersions() {
        ArrayList<ClassificationVersion> clsVersions = dataIO.getAllClsVersions();
        if (null == clsVersions) {
            System.out.println("Unable to retrieve classification version sid information");
            return null;
        }
        currentVersion = dataIO.getCurVersionInfo();
        HashMap<String, ClassificationVersion> rtnList = new HashMap<String, ClassificationVersion>();
        for (ClassificationVersion cv: clsVersions) {
            rtnList.put(cv.getId(), cv);
        }
        
        return rtnList;
    }
    
    /**
     * 
     * @param parameterLookup
     * @param bookIdParam
     * @param includeAllReleases - true will include all classification version sids, false will only include current release version
     * @return 
     */
    public ArrayList<OperationInfoDetail> getUpdateHistoryForBook(HashMap<String, String> parameterLookup, String bookIdParam, boolean includeAllReleases) {
        if (null == CLS_VERSION_SID_LOOKUP) {
            return null;
        }
        String book = parameterLookup.get(bookIdParam);
        ArrayList<OperationInfoDetail> finalList = new ArrayList<OperationInfoDetail>();
        
        ArrayList<OperationInfoDetail> nodeInfoList = getNodeInfo(book);
        if (null != nodeInfoList) {
            finalList.addAll(nodeInfoList);
        }
        ArrayList<OperationInfoDetail> graftPruneInfoList = getGraftPruneInfo(book);
        if (null != graftPruneInfoList) {
            finalList.addAll(graftPruneInfoList);
        }
        ArrayList<OperationInfoDetail> commentInfoList = getFamilyCommentInfo(book);
        if (null != commentInfoList) {
            finalList.addAll(commentInfoList);
        }
        ArrayList<OperationInfoDetail> statusInfoList = getFamilyStatusInfo(book);
        if (null != statusInfoList) {
            finalList.addAll(statusInfoList);
        }
        
        // Remove non-current entries
        if (false == includeAllReleases) {
            String id = currentVersion.getId();
            if (null != id) {
                Iterator<OperationInfoDetail> iter = finalList.iterator();
                while (iter.hasNext()) {
                    OperationInfoDetail oid = iter.next();
                    if (false == id.equals(oid.releaseId)) {
                        iter.remove();
                    }
                }
            }
            
        }

        Collections.sort(finalList, new Comparator<OperationInfoDetail>() {
            @Override
            public int compare(OperationInfoDetail o1, OperationInfoDetail o2) {
                int revisionComp = new Integer(o1.releaseId).compareTo(new Integer(o2.releaseId));
                if (0 != revisionComp) {
                    return revisionComp;
                }
                
                // Order by creation date next
                int creationComp = 0;
                if (null != o1.creationDate && null != o2.creationDate) {
                    creationComp = o1.creationDate.compareTo(o2.creationDate);
                }
                if (0 != creationComp) {
                    return creationComp;
                }
                
                // Order by operation 
                return  Integer.compare(o1.operation.ordinal(), o2.operation.ordinal());

            }

        });
        Collections.reverse(finalList);
        return finalList;
    }
    
    public Document getUpdateHistoryForBookDoc(HashMap<String, String> parameterLookup, String bookIdParam, boolean includeAllReleases) {
        ArrayList<OperationInfoDetail> finalList = getUpdateHistoryForBook(parameterLookup, bookIdParam, includeAllReleases);
        if (null == finalList) {
            return null;
        }
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            if (false == parameterLookup.isEmpty()) {
                Element parameters = doc.createElement(ELEMENT_PARAMETERS);
                root.appendChild(parameters);
                for (Entry<String, String> entry: parameterLookup.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    key.replaceAll(WHITE_SPACE, UNDERSCORE);
                    Element param = doc.createElement(key);
                    param.appendChild(doc.createTextNode(value));
                    parameters.appendChild(param);
                }
                
            }
            if (null != finalList) {
                Element opList = doc.createElement(ELEMENT_OPERATION_LIST);
                root.appendChild(opList);
                for (OperationInfoDetail oid: finalList) {
                    Element operation = doc.createElement(ELEMENT_OPERATION);
                    opList.appendChild(operation);
                    
                    operation.appendChild(Utils.createTextNode(doc, ELEMENT_RELEASE, CLS_VERSION_SID_LOOKUP.get(oid.releaseId).getName()));
                    
                    operation.appendChild(Utils.createTextNode(doc, ELEMENT_TYPE, oid.operation.label));
                    
                    switch(oid.operation) {
                        case NODE_ANNOTATION: {
                            operation.appendChild(Utils.createTextNode(doc, ELEMENT_ID, oid.id));
                            operation.appendChild(Utils.createTextNode(doc, ELEMENT_CONFIDENCE_CODE, oid.confidenceCode));
                            break;
                        }
                        case NODE_GRAFT_PRUNE: {
                            operation.appendChild(Utils.createTextNode(doc, ELEMENT_ID, oid.id));
                            break;
                        }
                        case FAMILY_COMMENT: {
                            operation.appendChild(Utils.createTextNode(doc, ELEMENT_COMMENT, oid.comment));
                            break;
                        }
                        case FAMILY_STATUS: {
                            operation.appendChild(Utils.createTextNode(doc, ELEMENT_STATUS, oid.status));
                            break;
                        }
                    }
                                  
                    if (null != oid.createdBy) {
                        operation.appendChild(Utils.createTextNode(doc, ELEMENT_CREATED_BY, oid.createdBy));                        
                    }
                    if (null != oid.creationDate) {
                        operation.appendChild(Utils.createTextNode(doc, ELEMENT_CREATION_DATE, DATE_FORMATTER.format(oid.creationDate)));     
                    }
                    if (null != oid.obsoletedBy) {
                        operation.appendChild(Utils.createTextNode(doc, ELEMENT_OBSOLETED_BY, oid.obsoletedBy));                        
                    }
                    if (null != oid.obsolescenceDate) {
                        operation.appendChild(Utils.createTextNode(doc, ELEMENT_OBSOLESCENCE_DATE, DATE_FORMATTER.format(oid.obsolescenceDate)));     
                    }                    
                }
            }
            return doc;
        }
        catch(Exception e) {
             e.printStackTrace();
             return null;
        }                
    }    
    
    public ArrayList<OperationInfoDetail> getNodeInfo(String book) {
        ArrayList<OperationInfoDetail> rtnList = new ArrayList<OperationInfoDetail>();
        
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            con = DBConnectionPool.getInstance().getConnection(DB_STR_CURRENT);
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();

            rst = stmt.executeQuery(QUERY_NODE_ANNOTATION.replace(QUERY_PARAMETER_1, book + WILDCARD));

            
            while (rst.next()) {
                OperationInfoDetail ih = new OperationInfoDetail();
                ih.operation = Operation.NODE_ANNOTATION;
                ih.releaseId = Integer.toString(rst.getInt(COLUMN_CLASSIFICATION_VERSION_SID));
                ih.id = rst.getString(COLUMN_PUBLIC_ID);
                int createdBy = rst.getInt(COLUMN_CREATED_BY);
                User u = USER_MANAGER.getUser(Integer.toString(createdBy));
                if (null != u) {
                    ih.createdBy = u.getName();
                }
                ih.creationDate = rst.getDate(COLUMN_CREATION_DATE);
                int obsoletedBy = rst.getInt(COLUMN_OBSOLETED_BY);
                u = USER_MANAGER.getUser(Integer.toString(obsoletedBy));
                if (null != u) {
                    ih.obsoletedBy = u.getName();
                }
                ih.obsolescenceDate = rst.getDate(COLUMN_OBSOLESCENCE_DATE);
                ih.confidenceCode = rst.getString(COLUMN_CONFIDENCE_CODE);
                rtnList.add(ih);
            }


        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about confidence code type, exception " + se.getMessage()
                    + " has been returned.");
            rtnList = null;
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }

        return rtnList;            
    }

    public ArrayList<OperationInfoDetail> getGraftPruneInfo(String book) {
        ArrayList<OperationInfoDetail> rtnList = new ArrayList<OperationInfoDetail>();
        
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            con = DBConnectionPool.getInstance().getConnection(DB_STR_CURRENT);
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();

            rst = stmt.executeQuery(QUERY_NODE_GRAFT_PRUNE.replace(QUERY_PARAMETER_1, book + WILDCARD));

            
            while (rst.next()) {
                OperationInfoDetail ih = new OperationInfoDetail();
                ih.operation = Operation.NODE_GRAFT_PRUNE;
                ih.releaseId = Integer.toString(rst.getInt(COLUMN_CLASSIFICATION_VERSION_SID));
                ih.id = rst.getString(COLUMN_PUBLIC_ID);
                int createdBy = rst.getInt(COLUMN_CREATED_BY);
                User u = USER_MANAGER.getUser(Integer.toString(createdBy));
                if (null != u) {
                    ih.createdBy = u.getName();
                }
                ih.creationDate = rst.getDate(COLUMN_CREATION_DATE);
                int obsoletedBy = rst.getInt(COLUMN_OBSOLETED_BY);
                u = USER_MANAGER.getUser(Integer.toString(obsoletedBy));
                if (null != u) {
                    ih.obsoletedBy = u.getName();
                }
                ih.obsolescenceDate = rst.getDate(COLUMN_OBSOLESCENCE_DATE);                
                rtnList.add(ih);
            }


        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about confidence code type, exception " + se.getMessage()
                    + " has been returned.");
            rtnList = null;
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }

        return rtnList;            
    }
    
    public ArrayList<OperationInfoDetail> getFamilyCommentInfo(String book) {
        ArrayList<OperationInfoDetail> rtnList = new ArrayList<OperationInfoDetail>();
        
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            con = DBConnectionPool.getInstance().getConnection(DB_STR_CURRENT);
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();

            rst = stmt.executeQuery(QUERY_FAMILY_COMMENT.replace(QUERY_PARAMETER_1, book));

            
            while (rst.next()) {
                OperationInfoDetail ih = new OperationInfoDetail();
                ih.operation = Operation.FAMILY_COMMENT;
                ih.releaseId = Integer.toString(rst.getInt(COLUMN_CLASSIFICATION_VERSION_SID));
                ih.id = book;
                int createdBy = rst.getInt(COLUMN_CREATED_BY);
                User u = USER_MANAGER.getUser(Integer.toString(createdBy));
                if (null != u) {
                    ih.createdBy = u.getName();
                }
                ih.creationDate = rst.getDate(COLUMN_CREATION_DATE);
                int obsoletedBy = rst.getInt(COLUMN_OBSOLETED_BY);
                u = USER_MANAGER.getUser(Integer.toString(obsoletedBy));
                if (null != u) {
                    ih.obsoletedBy = u.getName();
                }
                ih.obsolescenceDate = rst.getDate(COLUMN_OBSOLESCENCE_DATE);                
                ih.comment = rst.getString(COLUMN_REMARK);
                rtnList.add(ih);
            }


        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about confidence code type, exception " + se.getMessage()
                    + " has been returned.");
            rtnList = null;
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }

        return rtnList;            
    }
    
    
    public ArrayList<OperationInfoDetail> getFamilyStatusInfo(String book) {
        ArrayList<OperationInfoDetail> rtnList = new ArrayList<OperationInfoDetail>();
        
        Connection con = null;

        Statement stmt = null;
        ResultSet rst = null;
        try {
            con = DBConnectionPool.getInstance().getConnection(DB_STR_CURRENT);
            if (null == con) {
                return null;
            }
            stmt = con.createStatement();

            rst = stmt.executeQuery(QUERY_FAMILY_STATUS.replace(QUERY_PARAMETER_1, book));

            
            while (rst.next()) {
                OperationInfoDetail ih = new OperationInfoDetail();
                ih.operation = Operation.FAMILY_STATUS;
                ih.releaseId = Integer.toString(rst.getInt(COLUMN_CLASSIFICATION_VERSION_SID));
                ih.id = book;
                int createdBy = rst.getInt(COLUMN_USER_ID);
                User u = USER_MANAGER.getUser(Integer.toString(createdBy));
                if (null != u) {
                    ih.createdBy = u.getName();
                }
                ih.creationDate = rst.getDate(COLUMN_CREATION_DATE);
                ih.status = rst.getString(COLUMN_STATUS);
                rtnList.add(ih);
            }


        } catch (SQLException se) {
            System.out.println("Unable to retrieve information from database about confidence code type, exception " + se.getMessage()
                    + " has been returned.");
            rtnList = null;
        } finally {
            ReleaseResources.releaseDBResources(rst, stmt, con);
        }

        return rtnList;            
    }    

    
    
    public class OperationInfoDetail {
        public String releaseId;        // Classification version sid
        public Operation operation;        
        public String id;               // Node persistent id or family id
        public Date creationDate;
        public String createdBy;
        public Date obsolescenceDate;
        public String obsoletedBy;
        public String confidenceCode;   // Specific to operation node annotation
        public String comment;          // Specific to operation comment
        public String status;           // Specific to operation status

        
        public OperationInfoDetail() {           
        }
    }
}
