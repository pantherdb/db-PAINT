/**
 * Copyright 2022 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintServer.webservices;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.datamodel.Organism;
import com.sri.panther.paintServer.logic.OrganismManager;
import edu.usc.ksom.pm.panther.paintCommon.CurationStatus;
import edu.usc.ksom.pm.panther.paintServer.logic.BookManager;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;
import edu.usc.ksom.pm.panther.paintServer.services.ServiceUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class LibraryStats {
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_FORMAT = "format";    
    
    private static final String ELEMENT_SEARCH = "search";
    private static final String ELEMENT_PARAMETERS = "parameters";
    
    public static final String ELEMENT_BOOKS = "books";
    public static final String ELEMENT_BOOK = "book";
    public static final String ELEMENT_ID = "id";
    public static final String ELEMENT_NAME = "name";
    public static final String ELEMENT_ANNOTATABLE = "annotatable";
    public static final String ELEMENT_NUM_LEAVES = "num_leaves";
    public static final String ELEMENT_STATUS_LIST = "status_list";
    public static final String ELEMENT_FAMILY_COMMENT = "family_comment";    
    public static final String ELEMENT_DETAILED_STATUS_LIST = "detailed_status_list";
    public static final String ELEMENT_STATUS = "status";
    public static final String ELEMENT_STATUS_DETAILS = "status_details";
    public static final String ELEMENT_STATUS_DETAIL = "status_detail";    
    public static final String ELEMENT_TIME_IN_MILLIS = "time_in_millis";
    public static final String ELEMENT_USER = "user";
    public static final String ELEMENT_USER_LOGIN_NAME = "user_login_name";
    public static final String ELEMENT_USER_FIRST_NAME = "user_first_name";
    public static final String ELEMENT_USER_LAST_NAME = "user_last_name";    
    public static final String ELEMENT_ORG_LIST = "organism_list";
    public static final String ELEMENT_ORG = "organism";    
    private static DataIO dataIO = DataAccessManager.getInstance().getDataIO();    
    private static final OrganismManager OM = OrganismManager.getInstance();
    private static final HashSet<String> BOOKS_WITH_LEAF_EXP_ANNOTS = BookManager.getInstance().getBooksWihtExpLeaves();

    
    public static String getStats(String requestType, String format) {
        if (WSConstants.SEARCH_TYPE_BOOKS_STATUS.equals(requestType)) {
            return booksStatus(format);
        }
        return null;
    }
    
    private static String booksStatus(String format) {
        ArrayList<Book> bookList = dataIO.getAllBooks(dataIO.CUR_CLASSIFICATION_VERSION_SID);

        if (null == bookList) {
            return null;
        }

        //HashSet<String> annotatableBooks = dataIO.getBooksWithExpEvdnceForLeaves();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);

            // Search parameters and time
            Element parameters = doc.createElement(ELEMENT_PARAMETERS);
            Element searchString = doc.createElement(WSConstants.SEARCH_TYPE_BOOKS_STATUS);

            parameters.appendChild(searchString);
            root.appendChild(parameters);

            Element books = doc.createElement(ELEMENT_BOOKS);
            root.appendChild(books);
            for (Book b : bookList) {
                Element bookElem = doc.createElement(ELEMENT_BOOK);
                books.appendChild(bookElem);
                Element idElem = Utils.createTextNode(doc, ELEMENT_ID, b.getId());
                bookElem.appendChild(idElem);

                bookElem.appendChild(Utils.createTextNode(doc, ELEMENT_NAME, b.getName()));
                boolean isAnnotatable = false;
                if (BOOKS_WITH_LEAF_EXP_ANNOTS.contains(b.getId())) {
                    isAnnotatable = true;
                }
                Element isAnnotElem = Utils.createTextNode(doc, ELEMENT_ANNOTATABLE, Boolean.toString(isAnnotatable));
                bookElem.appendChild(isAnnotElem);
                bookElem.appendChild(Utils.createTextNode(doc, ELEMENT_NUM_LEAVES, Integer.toString(b.getNumLeaves())));

                Vector<String> statusList = BookListUtil.getCurationStatusStrings(b);
                if (null != statusList) {
                    Element statusListElem = doc.createElement(ELEMENT_STATUS_LIST);
                    bookElem.appendChild(statusListElem);
                    for (String status : statusList) {
                        statusListElem.appendChild(Utils.createTextNode(doc, ELEMENT_STATUS, status));
                    }
                }

                ArrayList<CurationStatus> statusDetailList = b.getCurationStatusList();
                if (null != statusDetailList) {
                    Element detailedStatusList = doc.createElement(ELEMENT_DETAILED_STATUS_LIST);
                    bookElem.appendChild(detailedStatusList);
                    for (CurationStatus cs : statusDetailList) {
                        Element statusDetails = doc.createElement(ELEMENT_STATUS_DETAILS);
                        detailedStatusList.appendChild(statusDetails);
                        String statusStr = Book.getCurationStatusString(cs.getStatusId());
                        if (null != statusStr) {
                            statusStr = statusStr.trim();
                            statusStr = statusStr.replaceAll(Constant.STR_COMMA, Constant.STR_EMPTY);
                            statusDetails.appendChild(Utils.createTextNode(doc, ELEMENT_STATUS_DETAIL, statusStr));
                        }
                        statusDetails.appendChild(Utils.createTextNode(doc, ELEMENT_TIME_IN_MILLIS, Long.toString(cs.getTimeInMillis())));
                        User u = cs.getUser();
                        if (null != u) {
                            Element user = doc.createElement(ELEMENT_USER);
                            statusDetails.appendChild(user);
                            if (null != u.getLoginName()) {
                                user.appendChild(Utils.createTextNode(doc, ELEMENT_USER_LOGIN_NAME, u.getLoginName()));
                            }
                        }
                    }
                }

                HashSet<String> orgSet = b.getOrgSet();
                if (null != orgSet) {
                    Element orgListElem = doc.createElement(ELEMENT_ORG_LIST);
                    bookElem.appendChild(orgListElem);
                    for (String org : orgSet) {
                        Organism o = OM.getOrganismForShortName(org);
                        orgListElem.appendChild(Utils.createTextNode(doc, ELEMENT_ORG, o.getLongName()));
                    }
                }
                if (null != b.getComment()) {
                    bookElem.appendChild(WSUtil.createTextNode(doc, ELEMENT_FAMILY_COMMENT, b.getComment()));
                }
            }
            if (null != format && ServiceUtils.FORMAT_OUTPUT_XML.equalsIgnoreCase(format)) {
                // Output information
                DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
                LSSerializer lsSerializer = domImplementation.createLSSerializer();
                return lsSerializer.writeToString(doc);
            } else {
                DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
                LSSerializer lsSerializer = domImplementation.createLSSerializer();
                String xmlStr = lsSerializer.writeToString(doc);

                JSONObject xmlJSONObj = XML.toJSONObject(xmlStr);
                return xmlJSONObj.toString(ServiceUtils.PRETTY_PRINT_INDENT_FACTOR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return null;
    }
    
}
