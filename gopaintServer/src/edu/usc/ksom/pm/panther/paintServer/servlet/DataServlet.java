/**
 * Copyright 2018 University Of Southern California
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
package edu.usc.ksom.pm.panther.paintServer.servlet;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.TransferInfo;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintCommon.familyLibrary.EntryType;
import com.sri.panther.paintCommon.familyLibrary.FileNameGenerator;
import com.sri.panther.paintCommon.familyLibrary.LibrarySettings;
import com.sri.panther.paintCommon.util.FileUtils;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.datamodel.ClassificationVersion;
import com.sri.panther.paintServer.logic.CategoryLogic;
import com.sri.panther.paintServer.logic.TaxonomyConstraints;
import com.sri.panther.paintServer.logic.VersionManager;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.DataTransferObj;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.MSA;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.SaveBookInfo;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import edu.usc.ksom.pm.panther.paintCommon.VersionInfo;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 * @author muruganu
 */
public class DataServlet extends HttpServlet {
    
    public static final String CLASSIFICATION_VERSION_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);
    public static final Boolean UPDATE_ALLOWED = new Boolean(ConfigFile.getProperty("updates_allowed"));

    public static final String ACTION_GET_GO_HIERARCHY = "goHierarchy";
    public static final String ACTION_GET_TAXONOMY_CONSTRAINTS = "taxonomyConstraints";
    public static final String ACTION_GET_VERSION_INFO = "versionInfo";
    public static final String ACTION_GET_BOOKS_WITH_EXP_EVDNCE = "booksWithExpEvdnce";
    public static final String ACTION_GET_MSA = "msa";
    public static final String ACTION_GET_TREE = "tree";
    public static final String ACTION_GET_NODES = "nodes";
    public static final String ACTION_GET_FAMILY_NAME = "familyName";
    public static final String ACTION_FAMILY_COMMENT = "familyComment";
    public static final String ACTION_GET_USER_INFO = "userInfo";
    private static final String ACTION_VERIFY_USER = "VerifyUserInfo";
    public static final String ACTION_SAVE_BOOK = "saveBook";
    
    public static final String REQUEST_LOCK_BOOKS = "LockBooks";
    public static final String REQUEST_UNLOCK_BOOKS = "UnlockBooks";
    public static final String REQUEST_LOCK_UNLOCK_BOOKS = "LockUnLockBooks";    
    public static final String REQUEST_SEARCH_GENE_NAME = "searchGeneName";
    public static final String REQUEST_SEARCH_GENE_EXT_ID = "searchGeneExtId";
    public static final String REQUEST_SEARCH_PROTEIN_EXT_ID = "searchProteinExtId";
    public static final String REQUEST_SEARCH_DEFINITION = "searchDefinition";
    public static final String REQUEST_SEARCH_BOOK_ID = "searchBookId";
    public static final String REQUEST_SEARCH_BOOK_PTN = "searchBookPTN";
    public static final String REQUEST_SEARCH_ALL_BOOKS = "allBooks";
    public static final String REQUEST_SEARCH_UNCURATED_BOOKS = "uncuratedBooks";
    public static final String REQUEST_MY_BOOKS = "MyBooks";
    public static final String REQUEST_SEARCH_REQUIRE_PAINT_REVIEW_UNLOCKED = "requirePaintReviewUnlocked";    
    
    protected static final String PROPERTY_SUFFIX_MSA_LIB_ROOT = "_msa_lib_root";
    protected static final String PROPERTY_SUFFIX_LIB_ROOT = "_lib_root";
    protected static final String PROPERTY_SUFFIX_LIB_ENTRY_TYPE = "_entry_type";
    protected static final String PROPERTY_SUFFIX_BOOK_TYPE = "_book_type";

    protected static final String PROPERTY_SUFFIX_FLAT_LIB_ROOT = "_lib_flat_trees";
    protected static final String PROPERTY_SUFFIX_FLAT_LIB_ENTRY_TYPE = "_lib_flat_entry_type";
    
    protected static final String PROPERTY_USER_SERVER = "server_usr_info";
    protected static final String PROPERTY_SERVLET_USER_INFO = "server_usr_info_check";
    

    
    private static final String SERVLET_CONNECTION_CONTENT_TYPE = "Content-Type";
    private static final String SERVLET_CONNECTION_OBJECT_TYPE_JAVA = "java/object";
    private static final String SERVLET_REQUEST_PROPERTY_COOKIE = "Cookie";    

    public static final String STRING_EMPTY = "";
    public static final String MSG_UNABLE_TO_RETRIEVE_LOCKED_BOOKS = "Unable to retrieve locked books.";    
    public static final int SEARCH_TYPE_GENE_SYMBOL = 0;
    public static final int SEARCH_TYPE_GENE_ID = 1;
    public static final int SEARCH_TYPE_PROTEIN_ID = 2;
    public static final int SEARCH_TYPE_DEFINITION = 3;
    public static final int SEARCH_TYPE_ALL_BOOKS = 4;
    public static final int SEARCH_TYPE_UNCURATED_BOOKS = 5;
    public static final int SEARCH_TYPE_BOOK_ID = 6;
    public static final int SEARCH_TYPE_BOOK_PTN = 7;
    public static final int SEARCH_TYPE_REQUIRE_PAINT_REVIEW_UNLOCKED_BOOKS = 8;
    
    public static final int REQUEST_TYPE_LOCK = 0;
    public static final int REQUEST_TYPE_UNLOCK = 1;    
    
    public static final String MSG_ERROR_RETRIEVING_BOOK_INFO = "Error retrieving book information";
    public static final String MSG_ERROR_OPERATION_NOT_PERMITTED = "Operation not permitted";
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String actionParam;
        if (null == request) {
            System.out.println("request is null");
            return;
        }
        actionParam = request.getParameter("action");
        if (null == actionParam) {
            System.out.println("No action parameter specified");
            return;
        }
        if (ACTION_GET_GO_HIERARCHY.equals(actionParam)) {
            getHierarchy(request, response);
            return;
        }
        if (ACTION_GET_TAXONOMY_CONSTRAINTS.equals(actionParam)) {
            getTaxonomyHelper(request, response);
            return;
        }
        if (ACTION_GET_BOOKS_WITH_EXP_EVDNCE.equals(actionParam)) {
            getBooksWithExperimentalEvidence(request, response);
            return;
        }
        if (ACTION_GET_FAMILY_NAME.equals(actionParam)) {
            getFamilyName(request, response);
            return;
        }
        if (ACTION_FAMILY_COMMENT.equals(actionParam)) {
            getFamilyComment(request, response);
            return;
        }
        if (ACTION_GET_MSA.equals(actionParam)) {
            getMSA(request, response);
            return;
        }
        if (ACTION_GET_TREE.equals(actionParam)) {
            getTree(request, response);
            return;
        }
        if (ACTION_GET_NODES.equals(actionParam)) {
            getNodes(request, response);
            return;
        }
        if (ACTION_SAVE_BOOK.equals(actionParam)) {
            saveBook(request, response);
            return;
        }
        if (ACTION_GET_VERSION_INFO.equals(actionParam)) {
            getVersionInfo(request, response);
            return;
        }
        if (ACTION_GET_USER_INFO.equals(actionParam)) {
            getUserInfo(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_SEARCH_GENE_NAME) == 0) {
            searchGeneName(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_SEARCH_GENE_EXT_ID) == 0) {
            searchGeneId(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_SEARCH_PROTEIN_EXT_ID) == 0) {
            searchProteinId(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_SEARCH_DEFINITION) == 0) {
            searchDefinition(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_SEARCH_BOOK_ID) == 0) {
            searchBookId(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_SEARCH_BOOK_PTN) == 0) {
            searchBookPTN(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_SEARCH_ALL_BOOKS) == 0) {
            searchAllBooks(request, response);
        }
        if (actionParam.compareTo(REQUEST_SEARCH_UNCURATED_BOOKS) == 0) {
            searchUncuratedBooks(request, response);
        }
        if (actionParam.compareTo(REQUEST_SEARCH_REQUIRE_PAINT_REVIEW_UNLOCKED) == 0) {
            searchRequirePaintReviewUnlockedBooks(request, response);
        }
        if (actionParam.compareTo(REQUEST_MY_BOOKS) == 0) {
            getMyBooks(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_LOCK_UNLOCK_BOOKS) == 0) {
            lockUnlockBooks(request, response);
            return;
        }
        if (actionParam.compareTo(REQUEST_UNLOCK_BOOKS) == 0) {
            unlockBooks(request, response);
            return;
        }
    }
    
    private void getTaxonomyHelper(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get Taxonomy information");
        response.setContentType("java/object");
        TaxonomyConstraints tc = TaxonomyConstraints.getInstance();
        TaxonomyHelper th = tc.getTaxomomyHelper();

        sendGZIP(response, th);        
    }

    private void getHierarchy(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get Hierarchy");
        response.setContentType("java/object");
//        ObjectInputStream in = null;
//        try {
//            System.out.println("Before opening stream");
//
//            // in = new ObjectInputStream(request.getInputStream());
//            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
//            in.readObject();
//            in.close();
//            System.out.println("Finished with stream and closed");
//        } catch (ClassNotFoundException cnfe) {
//            cnfe.printStackTrace();
//            return;
//        } catch (IOException ie) {
//            ie.printStackTrace();
//            return;
//        }
        CategoryLogic ci = CategoryLogic.getInstance();
        GOTermHelper gth = ci.getGOTermHelper();

        sendGZIP(response, gth);
    }

    private void getVersionInfo(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get version information");
        response.setContentType("java/object");
        
        VersionManager vm = VersionManager.getInstance();
        ClassificationVersion cv = vm.getCurrentVersion();
        if (null == cv) {
            cv = new ClassificationVersion();
        }
        
        VersionInfo vi = new VersionInfo();
        vi.setVersionId(cv.getClsId());
        vi.setName(cv.getName());
        vi.setReleaseDate(cv.getReleaseDate());
        sendGZIP(response, vi);
    }
    
    private void saveBook(HttpServletRequest request, HttpServletResponse response) {
        if (false == UPDATE_ALLOWED.booleanValue()) {
            sendGZIP(response, MSG_ERROR_OPERATION_NOT_PERMITTED);
            return;
        }
        System.out.println("Going to save book");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        SaveBookInfo sbi = null;
        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            sbi  = (SaveBookInfo) in.readObject();


            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
             
        if (null == sbi) {
            sendGZIP(response, "request for save book info is null");
            return;
        }
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        String returnInfo = null;
        try {
            returnInfo = dataIO.saveBook(sbi, CLASSIFICATION_VERSION_SID);
        }
        catch(Exception e) {
            
        }
        sendGZIP(response, returnInfo);
        
    }    

    
    private void getNodes(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get node information");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        String book = null;
        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            book  = (String) in.readObject();


            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
             
        if (null == book) {
            sendGZIP(response, null);
            return;
        }
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        HashMap<String, Node> nodeLookup = null;
        StringBuffer errorBuf = new StringBuffer(); 
        StringBuffer paintErrBuf = new StringBuffer();
        try {           
            nodeLookup = dataIO.getNodeInfo(book, CLASSIFICATION_VERSION_SID, errorBuf, paintErrBuf);
        }
        catch (Exception e) {
            errorBuf.append(MSG_ERROR_RETRIEVING_BOOK_INFO);
            nodeLookup = null;
            e.printStackTrace();
        }
        DataTransferObj dto = new DataTransferObj(nodeLookup, errorBuf.append(paintErrBuf));
        sendGZIP(response, dto);
        
    }
    
    private void getFamilyComment(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get family name");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        String book = null;
        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            book = (String) in.readObject();


            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        
        if (null == book) {
            sendGZIP(response, null);
            return;
        }
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        String familyComment = null;
        try {
            familyComment = dataIO.getFamilyComment(book, CLASSIFICATION_VERSION_SID, new ArrayList<Integer>());
        }
        catch (Exception e) {
            
        }
        if (null == familyComment) {
            familyComment = new String();
        }
        sendGZIP(response, familyComment);

     
    }    

    private void getFamilyName(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get family name");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        String book = null;
        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            book = (String) in.readObject();


            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        
        if (null == book) {
            sendGZIP(response, null);
            return;
        }
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        String familyName = null;
        try {
            familyName = dataIO.getFamilyName(book, CLASSIFICATION_VERSION_SID);
        }
        catch(Exception e) {
            
        }
        
        if (null == familyName) {
            familyName = new String();
        }
        sendGZIP(response, familyName);

     
    }    
    private void getTree(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get tree");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        String book = null;
        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            book = (String) in.readObject();


            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        
        if (null == book) {
            sendGZIP(response, null);
            return;
        }
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        String treeStrs[] = dataIO.getTree(book, CLASSIFICATION_VERSION_SID);
        if (null == treeStrs) {
            treeStrs = new String[0];
        }
        sendGZIP(response, treeStrs);

     
    }

    private void getMSA(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get msa");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        String book = null;
        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            book = (String) in.readObject();


            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        if (null == book) {
            sendGZIP(response, null);
            return;
        }
        MSA msa = getMSA(book, CLASSIFICATION_VERSION_SID);
        sendGZIP(response, msa);     
    }

    public static MSA getMSA(String book, String uplVersion) {
        LibrarySettings libSettings = null;
        EntryType et = null;
        String msaEntryType = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ENTRY_TYPE);

        if ((null == msaEntryType) || (0 == msaEntryType.length())) {
        } else {
            et = new EntryType();
            et.setEntryType(msaEntryType);
        }
        libSettings = new LibrarySettings(book, et, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_BOOK_TYPE), ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ROOT));
        return getMSA(libSettings);
    }

    public static MSA getMSA(LibrarySettings ls) {
        String msaURLs[] = FileNameGenerator.getMSAFilesForPaint(ls);

        String msaURL = msaURLs[1];
        MSA msa = new MSA();
        try {
            String[] msaContents = FileUtils.readFileFromURL(new URL(msaURL));
            if (null == msaContents) {
                System.out.println("Unable to read msa information from url " + msaURL);
                return null;
            }
            msa.setMsaContents(msaContents);
            msaURL = FileNameGenerator.getMSAWts(ls);
            String[] msaWts = FileUtils.readFileFromURL(new URL(msaURL));
            if (null != msaWts) {
                msa.setWeightsContents(msaWts);
            } else {
                System.out.println("Cannot read msa wts file" + msaURL);
            }
            return msa;
        } catch (IOException ie) {

            return null;

        } catch (Exception e) {
            if (null != msa.getMsaContents()) {
                return msa;
            }
            System.out.println("Unable to read msa information from url " + msaURL);

            return null;

        }
    }
    
    public void getUserInfo(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to attempt to get user information");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        Vector clientRequest = null;

        try {
            System.out.println("Before opening stream");
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            clientRequest = (Vector) in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }

        Vector objs = new Vector();
        try {
            String userName = (String) clientRequest.elementAt(0);
            String password = String.copyValueOf((char[]) clientRequest.elementAt(1));
            password = convertPassword(userName, password);
            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));

            User user = dataIO.getUser(userName, password);

            objs.addElement(user);
            System.out.println("Added objects for transferring");

        } catch (Exception e) {
            System.out.println("Exception while getting user info " + e.getMessage() + " has been returned.");
            e.printStackTrace();

        }
        sendGZIP(response, objs);

    }
  
    private String convertPassword(String userName, String password) {
        Vector objs = new Vector(2);
        objs.add(userName);
        objs.add(password);
        Object o = sendAndReceiveZip(ConfigFile.getProperty(PROPERTY_USER_SERVER), ConfigFile.getProperty(PROPERTY_SERVLET_USER_INFO), ACTION_VERIFY_USER, objs, null, null);
        if (null == o) {
            return null;
        }
        if (false == o instanceof Vector) {
            return null;
        }
        Vector output = (Vector)o;
        if (false == output.isEmpty()) {
            return (String)output.get(0);
        }
        return null;
    }
    
    public static Object sendAndReceiveZip(String servletURL,
                                              String servletPath,
                                              String actionRequest,
                                              Object sendInfo,
                                              String sessionIdName,
                                              String sessionIdValue) {
        try {

            // connect to the servlet
            URL servlet = new URL(servletURL + servletPath + actionRequest);
            java.net.URLConnection servletConnection =
                servlet.openConnection();

            servletConnection.setRequestProperty(SERVLET_CONNECTION_CONTENT_TYPE,
                                                 SERVLET_CONNECTION_OBJECT_TYPE_JAVA);

            // Set the session id, if necessary
            if ((null != sessionIdName) && (null != sessionIdValue)) {
                servletConnection.setRequestProperty(SERVLET_REQUEST_PROPERTY_COOKIE,
                                                     sessionIdName +
                                                     "=".concat(sessionIdValue));
            }

            // Connection should ignore caches if any
            servletConnection.setUseCaches(false);

            // Indicate sending and receiving information from the server
            servletConnection.setDoInput(true);
            servletConnection.setDoOutput(true);
            ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(new GZIPOutputStream(servletConnection.getOutputStream()));

            objectOutputStream.writeObject(sendInfo);
            objectOutputStream.flush();
            objectOutputStream.close();
            ObjectInputStream servletOutput =
                new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
            Object outputFromServlet = servletOutput.readObject();

            servletOutput.close();
            return outputFromServlet;
        } catch (MalformedURLException muex) {
            System.out.println("MalformedURLException " + muex.getMessage() +
                               " has been returned while sending and receiving information from server");
            muex.printStackTrace();
            return null;
        } catch (IOException ioex) {
            System.out.println("IOException " + ioex.getMessage() +
                               " has been returned while sending and receiving information from server");
            return null;
        } catch (Exception e) {
            System.out.println("Exception " + e.getMessage() +
                               " has been returned while sending and receiving information from server");
            return null;
        }
    }
   
    public void getBooksWithExperimentalEvidence(HttpServletRequest request, HttpServletResponse response) {
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        HashSet<String> bookSet = dataIO.getBooksWithExpEvdnceForLeaves();
        sendGZIP(response, bookSet);
    }    
    
    public void searchGeneName(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_GENE_SYMBOL);
        
    }
    
    public void searchGeneId(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_GENE_ID);
        
    }
    
    public void searchProteinId(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_PROTEIN_ID);
        
    }
    
    public void searchDefinition(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_DEFINITION);
        
    }
    public void searchBookId(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_BOOK_ID);
        
    }
    public void searchBookPTN(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_BOOK_PTN);
        
    }    

    public void searchAllBooks(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_ALL_BOOKS);
        
    }
    public void searchUncuratedBooks(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_UNCURATED_BOOKS);
        
    }
    public void searchRequirePaintReviewUnlockedBooks(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_REQUIRE_PAINT_REVIEW_UNLOCKED_BOOKS);
        
    }    

    

    public void searchBooks(HttpServletRequest request,
                            HttpServletResponse response, int type) {
        System.out.println("Going to search for books");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        Vector applnRequest = null;

        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in =
  new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            applnRequest = (Vector)in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        String searchField = null;
        if (applnRequest.size() > 0) {
                searchField = (String)applnRequest.elementAt(0);
        }


               DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        ArrayList<Book> books;
        if (SEARCH_TYPE_GENE_SYMBOL == type) {
            books = dataIO.searchBooksByGeneSymbol(searchField, CLASSIFICATION_VERSION_SID);
        } else if (SEARCH_TYPE_GENE_ID == type) {
            books = dataIO.searchBooksByGenePrimaryExtAcc(searchField, CLASSIFICATION_VERSION_SID);
        } else if (SEARCH_TYPE_PROTEIN_ID == type){
            books = dataIO.searchBooksByProteinPrimaryExtId(searchField, CLASSIFICATION_VERSION_SID);
        }
        else if (SEARCH_TYPE_DEFINITION == type) {
            books = dataIO.searchBooksByDefinition(searchField, CLASSIFICATION_VERSION_SID);           
        }
        else if (SEARCH_TYPE_BOOK_ID == type) {
            books = dataIO.searchBooksById(searchField, CLASSIFICATION_VERSION_SID);
        }
        else if (SEARCH_TYPE_BOOK_PTN == type) {
            books = dataIO.searchBooksByPTN(searchField, CLASSIFICATION_VERSION_SID);
        }        
        else if (SEARCH_TYPE_ALL_BOOKS == type) {
            books = dataIO.getAllBooks(CLASSIFICATION_VERSION_SID);
        }
        else if (SEARCH_TYPE_UNCURATED_BOOKS == type) {
            books = dataIO.getUncuratedUnlockedBooks(CLASSIFICATION_VERSION_SID);
        }
        else if (SEARCH_TYPE_REQUIRE_PAINT_REVIEW_UNLOCKED_BOOKS == type) {
            books = dataIO.getRequirePAINTReviewUnlockedBooks(CLASSIFICATION_VERSION_SID);
        }
        else {
            System.out.println("Invalid search type specified");
            return;
            
        }

        Vector outputInfo = new Vector();

        outputInfo.addElement(new TransferInfo(STRING_EMPTY));
        outputInfo.addElement(books);
        try {
            System.out.println("Going to send back books from search");

            // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
            ObjectOutputStream outputToApplet =
                new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

            System.out.println("Output stream is " +
                               outputToApplet.toString());
            System.out.println("Sending back information");
            outputToApplet.writeObject(outputInfo);
            System.out.println("After write object");
            outputToApplet.flush();
            System.out.println("After flush object");
            outputToApplet.reset();
            System.out.println("After reset object");
            outputToApplet.close();
            System.out.println("Data transmission complete.");
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Exitting servlet now");
    }    
    
  public void getMyBooks(HttpServletRequest request, HttpServletResponse response){
      System.out.println("Going to get my books list");
      response.setContentType("java/object");
      ObjectInputStream in = null;
      Vector applnRequest = null;

      try {
          System.out.println("Before opening stream");

          // in = new ObjectInputStream(request.getInputStream());
          in =
      new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
          applnRequest = (Vector)in.readObject();
          in.close();
          System.out.println("Finished with stream and closed");
      } catch (ClassNotFoundException cnfe) {
          cnfe.printStackTrace();
          return;
      } catch (IOException ie) {
          ie.printStackTrace();
          return;
      }
      
      Vector userInfo = (Vector)applnRequest.elementAt(0);
      String userName = (String)userInfo.elementAt(0);
      String password = String.copyValueOf((char[]) userInfo.elementAt(1));
      password = convertPassword(userName, password);
      
      DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));

      
      ArrayList<Book> books = dataIO.getMyBooks(userName, password, CLASSIFICATION_VERSION_SID);
      sendGZIP(response, books);

    }
  
    public void lockBooks(HttpServletRequest request, HttpServletResponse response) {
        if (false == UPDATE_ALLOWED.booleanValue()) {
            Vector outputInfo = new Vector(1);
            outputInfo.add(new TransferInfo(MSG_ERROR_OPERATION_NOT_PERMITTED));
            sendGZIP(response, outputInfo);
            return;
        }
        lockUnlockBooks(request, response, REQUEST_TYPE_LOCK);
    }
  
  
  public void unlockBooks(HttpServletRequest request, HttpServletResponse response){
      if (false == UPDATE_ALLOWED.booleanValue()) {
            Vector outputInfo = new Vector(1);
            outputInfo.add(new TransferInfo(MSG_ERROR_OPERATION_NOT_PERMITTED));
            sendGZIP(response, outputInfo);
            return;
      }     
      lockUnlockBooks(request, response, REQUEST_TYPE_UNLOCK);
  }
  
 private void lockUnlockBooks (HttpServletRequest request, HttpServletResponse response, int requestType){
     if (false == UPDATE_ALLOWED.booleanValue()) {
         Vector outputInfo = new Vector(1);
         outputInfo.add(new TransferInfo(MSG_ERROR_OPERATION_NOT_PERMITTED));
         sendGZIP(response, outputInfo);
         return;
     }   
     System.out.println("Going to lock/unlock books");
     response.setContentType("java/object");
     ObjectInputStream in = null;
     Vector applnRequest = null;

     try {
         System.out.println("Before opening stream");

         in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
         applnRequest = (Vector)in.readObject();
         in.close();
         System.out.println("Finished with stream and closed");
     } catch (ClassNotFoundException cnfe) {
         cnfe.printStackTrace();
         return;
     } catch (IOException ie) {
         ie.printStackTrace();
         return;
     }
     Vector userInfo = (Vector)applnRequest.elementAt(0);
     String userName = (String)userInfo.elementAt(0);
     String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
     password = convertPassword(userName, password);
     //String dbUplVersion = (String)applnRequest.elementAt(1);
     Vector bookList = (Vector)applnRequest.elementAt(1);
     
     //String db = FixedInfo.getDb(dbUplVersion);
     DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
     //String uplVersion = FixedInfo.getCls(dbUplVersion);
     
     String operationInfo = null;
     if (REQUEST_TYPE_LOCK == requestType) {

        if (null == userInfo) {
            System.out.println("user info is null");
        }
        try {
           operationInfo = dataIO.lockBooks(userName, password, CLASSIFICATION_VERSION_SID, bookList); 
        }
        catch (Exception e) {
            
        }
         
     }
     else if (REQUEST_TYPE_UNLOCK == requestType){
         try {
            operationInfo = dataIO.unlockBooks(userName, password, CLASSIFICATION_VERSION_SID, bookList);
         }
         catch (Exception e) {
             
         }
     }
     else {
         System.out.println("Invalid lock/unlock operation specified");
         return;
     }


    Vector outputInfo = new Vector(1);
    outputInfo.add(new TransferInfo(operationInfo));



     try {
         System.out.println("Going to send back books from locking / unlocking operation");

         // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
         ObjectOutputStream outputToApplet =
             new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

         System.out.println("Output stream is " +
                            outputToApplet.toString());
         System.out.println("Sending back information");
         outputToApplet.writeObject(outputInfo);
         System.out.println("After write object");
         outputToApplet.flush();
         System.out.println("After flush object");
         outputToApplet.reset();
         System.out.println("After reset object");
         outputToApplet.close();
         System.out.println("Data transmission complete.");
     } catch (IOException ioex) {
         ioex.printStackTrace();
     } catch (Exception ex) {
         ex.printStackTrace();
     }
     System.out.println("Exitting servlet now");
     }
     
    private void lockUnlockBooks (HttpServletRequest request, HttpServletResponse response){
        if (false == UPDATE_ALLOWED.booleanValue()) {
         Vector outputInfo = new Vector(1);
         outputInfo.add(new TransferInfo(MSG_ERROR_OPERATION_NOT_PERMITTED));
         sendGZIP(response, outputInfo);
         return;
        }        
        System.out.println("Going to both lock and unlock books");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        Vector applnRequest = null;

        try {
            System.out.println("Before opening stream");

            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            applnRequest = (Vector)in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        Vector userInfo = (Vector)applnRequest.elementAt(0);
        if (null == userInfo) {
            System.out.println("user info is null");
            return;
        }
        String userName = (String)userInfo.elementAt(0);
        String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
        password = convertPassword(userName, password);
        //String dbUplVersion = (String)applnRequest.elementAt(1);
        Vector lockBookList = (Vector)applnRequest.elementAt(1);
        Vector unlockBookList = (Vector)applnRequest.elementAt(2);
        if (null == lockBookList || null == unlockBookList) {
            return;
        }
        
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));        
//
//        String uplVersion = FixedInfo.getCls(dbUplVersion);
        
        String operationInfo = null;
        if (0 != lockBookList.size()) {
            try {
                operationInfo = dataIO.lockBooks(userName, password, CLASSIFICATION_VERSION_SID, lockBookList);
            }
            catch(Exception e) {
                
            }
        }
        else {
            operationInfo = Constant.STR_EMPTY;
        }
        if (0 != unlockBookList.size()) {
            if (0 == operationInfo.length()) {
                try {
                    operationInfo = dataIO.unlockBooks(userName, password, CLASSIFICATION_VERSION_SID, unlockBookList);
                }
                catch(Exception e) {
                    
                }
            }
            else {
                try {
                    operationInfo += Constant.STR_NEWLINE + dataIO.unlockBooks(userName, password, CLASSIFICATION_VERSION_SID, unlockBookList);
                }
                catch(Exception e) {
                    
                }
            }
        }
       Vector outputInfo = new Vector(1);
       outputInfo.add(new TransferInfo(operationInfo));



        try {
            System.out.println("Going to send back books from lock and  unlock operation");

            // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
            ObjectOutputStream outputToApplet =
                new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

            System.out.println("Output stream is " +
                               outputToApplet.toString());
            System.out.println("Sending back information");
            outputToApplet.writeObject(outputInfo);
            System.out.println("After write object");
            outputToApplet.flush();
            System.out.println("After flush object");
            outputToApplet.reset();
            System.out.println("After reset object");
            outputToApplet.close();
            System.out.println("Data transmission complete.");
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Exitting servlet now");
    }
  
  
  
  
  
    private void sendGZIP(HttpServletResponse response, Object sendObject) {
        try{
          System.out.println("Sending back info in gzip format now");

          // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
          response.setHeader("Content-Encoding", "gzip");
          ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

          System.out.println("Output stream is " + outputToApplet.toString());
          System.out.println("Sending back information");
          if (null != sendObject) {
            outputToApplet.writeObject(sendObject);
          }
          System.out.println("After write object");
          outputToApplet.flush();
          System.out.println("After flush object");
          outputToApplet.reset();
          System.out.println("After reset object");
          outputToApplet.close();
          System.out.println("Data transmission complete.");
        }
        catch (IOException ioex){
          ioex.printStackTrace();
        }
        catch (Exception ex){
          ex.printStackTrace();
        }
        System.out.println("Sent without exception");        
    }
}