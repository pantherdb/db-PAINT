/**
 * Copyright 2023 University Of Southern California
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
import com.sri.panther.paintCommon.familyLibrary.PAINTFile;
import com.sri.panther.paintCommon.util.FileUtils;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.database.DataServer;
import com.sri.panther.paintServer.datamodel.ClassificationVersion;
import com.sri.panther.paintServer.datamodel.FamilyDomain;
import com.sri.panther.paintServer.datamodel.FullGOAnnotVersion;
import edu.usc.ksom.pm.panther.paintCommon.Organism;
import com.sri.panther.paintServer.datamodel.PantherVersion;
import com.sri.panther.paintServer.logic.CategoryLogic;
import com.sri.panther.paintServer.logic.KeyResiduesManager;
import com.sri.panther.paintServer.logic.OrganismManager;
import com.sri.panther.paintServer.logic.TaxonomyConstraints;
import com.sri.panther.paintServer.logic.VersionManager;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.Comment;
import edu.usc.ksom.pm.panther.paintCommon.DataTransferObj;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.KeyResidue;
import edu.usc.ksom.pm.panther.paintCommon.MSA;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.PAINTVersion;
import edu.usc.ksom.pm.panther.paintCommon.SaveBookInfo;
import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import edu.usc.ksom.pm.panther.paintCommon.VersionContainer;
import edu.usc.ksom.pm.panther.paintCommon.VersionInfo;
import edu.usc.ksom.pm.panther.paintServer.logic.BookManager;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DataServlet extends HttpServlet {   
    public static final String CLASSIFICATION_VERSION_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);
    public static final Boolean UPDATE_ALLOWED = new Boolean(ConfigFile.getProperty("updates_allowed"));
    public static final VersionContainer versionContainer = getVersionContainer();

    public static final String ACTION_GET_GO_HIERARCHY = "goHierarchy";
    public static final String ACTION_GET_TAXONOMY_CONSTRAINTS = "taxonomyConstraints";
    public static final String ACTION_GET_VERSION_INFO = "versionInfo";
    public static final String ACTION_GET_BOOKS_WITH_EXP_EVDNCE = "booksWithExpEvdnce";
    public static final String ACTION_GET_MSA = "msa";
    public static final String ACTION_GET_TREE = "tree";
    public static final String ACTION_GET_NODES = "nodes";
    public static final String ACTION_GET_FAMILY_NAME = "familyName";
    public static final String ACTION_GET_FAMILY_DOMAIN = "familyDomain";    
    public static final String ACTION_FAMILY_COMMENT = "familyComment";
    public static final String ACTION_MSA_KEY_RESIDUE = "msaKeyResidue";
    public static final String ACTION_GET_USER_INFO = "userInfo";
    private static final String ACTION_VERIFY_USER = "VerifyUserInfo";
    public static final String ACTION_SAVE_BOOK = "saveBook";
    public static final String ACTION_GET_VERSIONS = "versions";
    
    public static final String REQUEST_ALL_ORGANISMS = "allOrganisms";
    
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
    
    public static final String PROPERTY_SUFFIX_LIB_ROOT = "_lib_root";
    public static final String PROPERTY_SUFFIX_LIB_ENTRY_TYPE = "_entry_type";
    public static final String PROPERTY_SUFFIX_BOOK_TYPE = "_book_type";

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
    public static final String MSG_ERROR_CANNOT_VERIFY_VERSION_INFO = "Operation rejected, cannot verify version information";
    
    DataIO dataIO = DataAccessManager.getInstance().getDataIO();
    
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
        if (ACTION_GET_FAMILY_DOMAIN.equals(actionParam)) {
            getFamilyDomain(request, response);
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
        if (ACTION_MSA_KEY_RESIDUE.equals(actionParam)) {
            getMsaResidue(request, response);
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
        if (ACTION_GET_VERSIONS.equals(actionParam)) {
            getVersions(request, response);
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
        
        if (actionParam.compareTo(REQUEST_ALL_ORGANISMS) == 0) {
            allOrganisms(request, response);
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
    
    private void getVersions(HttpServletRequest request, HttpServletResponse response) {
        VersionContainer vc = getVersionContainer();
        DataTransferObj dto = new DataTransferObj(vc, null, null);
        sendGZIP(response, dto);
    }
    
    private static VersionContainer getVersionContainer() {
        VersionManager vm = VersionManager.getInstance();
        VersionContainer vc = new VersionContainer();
        
        ClassificationVersion cv = vm.getClsVersion();
        VersionInfo clsInfo = new VersionInfo();
        clsInfo.setVersionId(cv.getId());
        clsInfo.setReleaseDate(cv.getReleaseDate());
        vc.addInfo(VersionContainer.VersionedObj.CLS_VERSION, clsInfo);
        
        PantherVersion pv = vm.getPantherVersion();
        VersionInfo pvInfo = new VersionInfo();
        pvInfo.setVersionId(pv.getId());
        pvInfo.setReleaseDate(pv.getReleaseDate());
        vc.addInfo(VersionContainer.VersionedObj.PANTHER_VERSION, pvInfo);
        
        FullGOAnnotVersion fgv = vm.getFullGOAnnotVersion();
        VersionInfo fgvInfo = new VersionInfo();
        fgvInfo.setVersionId(fgv.getId());
        fgvInfo.setReleaseDate(fgv.getReleaseDate());
        vc.addInfo(VersionContainer.VersionedObj.FULL_GO_VERSION, fgvInfo);
        
        VersionInfo clientVersion = new VersionInfo();
        clientVersion.setVersionId(PAINTVersion.getPAINTClientversion());
        vc.addInfo(VersionContainer.VersionedObj.CLIENT_VERSION, clientVersion);
        
        VersionInfo clientServerCommon = new VersionInfo();
        clientServerCommon.setVersionId(PAINTVersion.getPAINTCommonVersion());
        vc.addInfo(VersionContainer.VersionedObj.CLIENT_SERVER_COMMON_VERSION, clientServerCommon);
        
        VersionInfo serverVersion = new VersionInfo();
        serverVersion.setVersionId(PAINTVersion.getPAINTServerVersion());        
        vc.addInfo(VersionContainer.VersionedObj.SERVER_VERSION, serverVersion);
        
        return vc;
    }

    private void getVersionInfo(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get version information");
        response.setContentType("java/object");
        
        VersionManager vm = VersionManager.getInstance();
        ClassificationVersion cv = vm.getClsVersion();
        if (null == cv) {
            cv = new ClassificationVersion();
        }
        
        VersionInfo vi = new VersionInfo();
        vi.setVersionId(cv.getId());
        vi.setName(cv.getName());
        vi.setReleaseDate(cv.getReleaseDate());
        sendGZIP(response, vi);
    }
    
    private void saveBook(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("Going to save book");
        response.setContentType("java/object");
        DataTransferObj serverOut = new DataTransferObj();
        try {
            System.out.println("Before opening stream");
            // in = new ObjectInputStream(request.getInputStream());
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();

            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }

            if (false == UPDATE_ALLOWED.booleanValue()) {
                Vector outputInfo = new Vector(1);
                serverOut.setObj(MSG_ERROR_OPERATION_NOT_PERMITTED);
                sendGZIP(response, outputInfo);
                return;
            }

            SaveBookInfo sbi = (SaveBookInfo) clientRequest.getObj();

            in.close();
            System.out.println("Finished with stream and closed");

            if (null == sbi) {
                serverOut.setMsg(new StringBuffer("request for save book info is null"));
                sendGZIP(response, serverOut);
                return;
            }
//            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
            String returnInfo = dataIO.saveBook(sbi, CLASSIFICATION_VERSION_SID);
            serverOut.setObj(returnInfo);
            sendGZIP(response, serverOut);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            serverOut.setMsg(new StringBuffer("Unable to save book"));
            sendGZIP(response, serverOut);
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            serverOut.setMsg(new StringBuffer("Unable to save book"));
            sendGZIP(response, serverOut);
            return;
        } catch (Exception e) {
            serverOut.setMsg(new StringBuffer("Unable to save book"));
            sendGZIP(response, serverOut);
            return;
        }

    }   

    
    private void getNodes(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get node information");
        HashMap<String, Node> nodeLookup = null;
        StringBuffer errorBuf = new StringBuffer();
        StringBuffer paintErrBuf = new StringBuffer();
        DataTransferObj serverOut = new DataTransferObj();
        try {
            response.setContentType("java/object");
            System.out.println("Before opening stream");
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();

            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }
            String book = (String) clientRequest.getObj();
            in.close();
            System.out.println("Finished with stream and closed");

            if (null == book) {
                serverOut.setMsg(new StringBuffer("Book id not specified"));
                sendGZIP(response, serverOut);
                return;
            }
//            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));

            nodeLookup = dataIO.getNodeInfo(book, CLASSIFICATION_VERSION_SID, errorBuf, paintErrBuf);
            Vector outputInfo = new Vector();
            outputInfo.add(nodeLookup);
            outputInfo.add(errorBuf.insert(0, paintErrBuf));
            serverOut.setObj(outputInfo);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        } catch (Exception e) {
            errorBuf.setLength(0);
            errorBuf.insert(0, MSG_ERROR_RETRIEVING_BOOK_INFO);
            nodeLookup = null;
            e.printStackTrace();
        }
        sendGZIP(response, serverOut);
    }
    
    private void getFamilyComment(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get family comment");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        DataTransferObj clientRequest = null;
        String book = null;
        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            clientRequest = (DataTransferObj) in.readObject();
            // Ensure client has correct version of data objects
            DataTransferObj serverOut = new DataTransferObj();
            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }
            book = (String) clientRequest.getObj();
            in.close();
            System.out.println("Finished with stream and closed");
            if (null == book) {
                serverOut.setObj("blank specified for book");
                sendGZIP(response, serverOut);
                return;
            }
//            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
            String familyComment = null;

                familyComment = dataIO.getFamilyComment(book, CLASSIFICATION_VERSION_SID, new ArrayList<Integer>());

            if (null == familyComment) {
                familyComment = new String();
            }

            serverOut.setObj(new Comment(null, null, familyComment));
            sendGZIP(response, serverOut);

        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }             
    }    
    private void getFamilyName(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get family name");
        response.setContentType("java/object");
        ObjectInputStream in = null;

        try {
            System.out.println("Before opening stream");
            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();
            DataTransferObj serverOut = new DataTransferObj();
            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }            
            


            in.close();
            System.out.println("Finished with stream and closed");

            String book = (String)clientRequest.getObj();
            if (null == book) {
                serverOut.setMsg(new StringBuffer("No book id specified"));
                sendGZIP(response, serverOut);
                return;
            }
//            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
            String familyName = dataIO.getFamilyName(book, CLASSIFICATION_VERSION_SID);

            if (null == familyName) {
                familyName = new String();
            }
            serverOut.setObj(familyName);
            sendGZIP(response, serverOut);
        }
        catch(Exception e) {
            
        }
     
    }
    private void getFamilyDomain(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get family domain");
        response.setContentType("java/object");
        ObjectInputStream in = null;

        try {
            System.out.println("Before opening stream");
            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();
            DataTransferObj serverOut = new DataTransferObj();
            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }            
            


            in.close();
            System.out.println("Finished with stream and closed");

            String book = (String)clientRequest.getObj();
            if (null == book) {
                serverOut.setMsg(new StringBuffer("No book id specified"));
                sendGZIP(response, serverOut);
                return;
            }
            FamilyDomain fd = new FamilyDomain(book);
            HashMap<String, HashMap<String, ArrayList<Domain>>> domainLookup = fd.getNodeToDomainLookup();
            if (null == domainLookup) {
                domainLookup = new HashMap<String, HashMap<String, ArrayList<Domain>>>();
            }
            serverOut.setObj(domainLookup);
            sendGZIP(response, serverOut);
        }
        catch(Exception e) {
            
        }
     
    }
    
    private void getTree(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get tree");
        try {
            response.setContentType("java/object");

            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();
            DataTransferObj serverOut = new DataTransferObj();
            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }
            String book = (String) clientRequest.getObj();
            in.close();
            System.out.println("Finished with stream and closed");

            if (null == book) {
                serverOut.setMsg(new StringBuffer("Book id not specified"));
                sendGZIP(response, serverOut);
                return;
            }
//            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
            String treeStrs[] = dataIO.getTree(book, CLASSIFICATION_VERSION_SID);
            if (null == treeStrs) {
                treeStrs = new String[0];
            }
            serverOut.setObj(treeStrs);
            sendGZIP(response, serverOut);

        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
    }
    
//    public static Object getMSA(String book, String uplVersion) {
//        LibrarySettings libSettings = null;
//        EntryType et = null;
//        String msaEntryType = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ENTRY_TYPE);
//
//        if ((null == msaEntryType) || (0 == msaEntryType.length())) {
//        } else {
//            et = new EntryType();
//            et.setEntryType(msaEntryType);
//        }
//        libSettings = new LibrarySettings(book, et, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_BOOK_TYPE), ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ROOT));
//        return getMSA(libSettings);
//    }

    // Check for flat file else retrieve from database
    public static String[] getTree(DataServer ds, String book, String uplVersion) {
        String tree[] = null;
        LibrarySettings tmpLibSettings = null;
        EntryType tmpEt = null;
        String tmpEntryType = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_FLAT_LIB_ENTRY_TYPE);
        if (null != tmpEntryType && 0 != tmpEntryType.length()) {
            tmpEt = new EntryType();
            tmpEt.setEntryType(tmpEntryType);
        }

        tmpLibSettings = new LibrarySettings(book, tmpEt, null, null, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_FLAT_LIB_ROOT), true);
        String paintName = FileNameGenerator.getPAINTPathName(tmpLibSettings);
        try {
            PAINTFile pf = PAINTFile.readPAINTFileURL(paintName);
            String path = FileUtils.getURLPath(paintName);
            String treeName = pf.getTreeFileName();

            tree = getContents(FileUtils.appendFileToPath(path, treeName));
        } catch (Exception e) {
            // Tree is not available from flat file - get it from database
            if (null == tree) {
                tree = ds.getTree(book, uplVersion);
            }

        }
        return tree;

    }  

    private void getMSA(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get msa");
        response.setContentType("java/object");
        try {
            System.out.println("Before opening stream");
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();
            DataTransferObj serverOut = new DataTransferObj();
            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }             
            String book = (String) clientRequest.getObj();
            if (null == book) {
                serverOut.setMsg(new StringBuffer("Book id not specified"));
                sendGZIP(response, serverOut);
                return;
            }            

            in.close();
            System.out.println("Finished with stream and closed");
            MSA msa = getMSA(book, CLASSIFICATION_VERSION_SID);
            serverOut.setObj(msa);
            sendGZIP(response, serverOut);              
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }

    }

    private void getMsaResidue(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get msa and key residue");
        response.setContentType("java/object");
        try {
            System.out.println("Before opening stream");
            ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();
            DataTransferObj serverOut = new DataTransferObj();
            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }             
            String book = (String) clientRequest.getObj();
            if (null == book) {
                serverOut.setMsg(new StringBuffer("Book id not specified"));
                sendGZIP(response, serverOut);
                return;
            }            

            in.close();
            System.out.println("Finished with stream and closed");

            // Execute retrieval of MSA and key residue information in parallel
            ExecutorService executor = Executors.newFixedThreadPool(2);
            LoadKeyResidueWorker keyResidueWorker = new LoadKeyResidueWorker(book);
            executor.execute(keyResidueWorker);
            LoadMSAWorker msaWorker = new LoadMSAWorker(book);
            executor.execute(msaWorker);
            executor.shutdown();

            // Wait until all threads finish
            while (!executor.isTerminated()) {

            }
            MSA msa = msaWorker.msa;
            if (null != msa) {
                msa.setKeyResidueList(keyResidueWorker.residueList);
            }
            serverOut.setObj(msa);
            sendGZIP(response, serverOut);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }

    }
    
  public static Vector<String[]> getMSAStrs(String book, String uplVersion) {
      LibrarySettings libSettings = null;
      EntryType       et = null;
      String          msaEntryType = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ENTRY_TYPE);

      if ((null == msaEntryType) || (0 == msaEntryType.length())) {}
      else{
        et = new EntryType();
        et.setEntryType(msaEntryType);
      }
      libSettings = new LibrarySettings(book, et, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_BOOK_TYPE), ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ROOT));
      return getMSAVect(libSettings);
  }
  
  public static Vector<String[]> getMSAVect(LibrarySettings ls){
    String  msaURLs[] = FileNameGenerator.getMSAFilesForPaint(ls);
    

    String msaURL = msaURLs[1];
    // For now put the msa file information into a vector.  There may be a need to add other information
    // to the MSA later
    Vector  v = new Vector();    
    try{
      String[] msaContents = FileUtils.readFileFromURL(new URL(msaURL));
      if (null == msaContents) {
        System.out.println("Unable to read msa information from url " + msaURL);
        return null;
      }
      v.addElement(msaContents);
//      msaURL = FileNameGenerator.getMSAWts(ls);
//      String[] msaWts = FileUtils.readFileFromURL(new URL(msaURL));
//      if (null != msaWts) {
//        v.addElement(msaWts);
//      }
//      else {
//        System.out.println("Cannot read msa wts file" + msaURL);
//      }
      return v;
    }
    catch (IOException ie){
      System.out.println(ie.getMessage() + " returned while attempting to read from url " + msaURL);
      ie.printStackTrace();
      if (0 != v.size()) {
        return v;
      }
      else {
        return null;
      }  
    }
    catch (Exception e) {
      System.out.println("Unable to read msa information from url " + msaURL);
      if (0 != v.size()) {
        return v;
      }
      else {
        return null;
      }  
    }
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
            // weights file is no longer generated for library
//            msaURL = FileNameGenerator.getMSAWts(ls);
//            String[] msaWts = FileUtils.readFileFromURL(new URL(msaURL));
//            if (null != msaWts) {
//                msa.setWeightsContents(msaWts);
//            } else {
//                System.out.println("Cannot read msa wts file" + msaURL);
//            }
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
//            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
            System.out.println(new Date() + " Attampt to verify user name and password");
            User user = dataIO.getUser(userName, password);
            if (null == user) {
                System.out.println("User object is null");
            }
            else {
                System.out.println("User object is not null");                
            }
            objs.addElement(user);
            System.out.println("Added objects for transferring");

        } catch (Exception e) {
            System.out.println("Exception while getting user info " + e.getMessage() + " has been returned.");
            e.printStackTrace();
        }
        sendGZIP(response, objs);

    }
    
//    public static User getUser(String userName, String password) {
//        String convertedPass = convertPassword(userName, password);
//        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
//        return dataIO.getUser(userName, convertedPass);
//    }    
  
    private static String convertPassword(String userName, String password) {
        Vector objs = new Vector(2);
        objs.add(userName);
        objs.add(password);
        Object o = sendAndReceiveZip(ConfigFile.getProperty(PROPERTY_USER_SERVER), ConfigFile.getProperty(PROPERTY_SERVLET_USER_INFO), ACTION_VERIFY_USER, objs, null, null);
        System.out.println("Converted password");
        if (null == o) {
            System.out.println("Converted password is null");
            return null;
        }
        if (false == o instanceof Vector) {
            System.out.println("Converted password is not instance of vector");
            return null;
        }
        Vector output = (Vector)o;
        if (false == output.isEmpty()) {
            System.out.println("Converted password is not empty");
            return (String)output.get(0);
        }
        System.out.println("Returning null, converted password is null");
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
        response.setContentType("java/object");
        ObjectInputStream in = null;
        DataTransferObj clientRequest = null;

        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            clientRequest = (DataTransferObj)in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }

        // Ensure client has correct version of data objects
        DataTransferObj serverOut = new DataTransferObj();
        VersionContainer vc = clientRequest.getVc();
        if (null == vc) {
            serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
            sendGZIP(response, serverOut);
            return;
        }

        String versionComp = versionContainer.compareForServerOps(vc);
        if (null != versionComp) {
            serverOut.setMsg(new StringBuffer(versionComp));
            sendGZIP(response, serverOut);
            return;
        }         
//        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        HashSet<String> bookSet = BookManager.getInstance().getBooksWihtExpLeaves();
        serverOut.setObj(bookSet);
        sendGZIP(response, serverOut);
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
        DataTransferObj clientRequest = null;

        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            clientRequest = (DataTransferObj)in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }

        // Ensure client has correct version of data objects
        DataTransferObj serverOut = new DataTransferObj();
        VersionContainer vc = clientRequest.getVc();
        if (null == vc) {
            serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
            sendGZIP(response, serverOut);
            return;
        }

        String versionComp = versionContainer.compareForServerOps(vc);
        if (null != versionComp) {
            serverOut.setMsg(new StringBuffer(versionComp));
            sendGZIP(response, serverOut);
            return;
        }         
        Vector searchList = (Vector)clientRequest.getObj();
        String searchField = null;
        if (searchList.size() > 0) {
                searchField = (String)searchList.elementAt(0);
        }        

        
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
        serverOut.setObj(outputInfo);
        try {
            System.out.println("Going to send back books from search");

            // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
            ObjectOutputStream outputToApplet =
                new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

            System.out.println("Output stream is " +
                               outputToApplet.toString());
            System.out.println("Sending back information");
            outputToApplet.writeObject(serverOut);
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

    public void getMyBooks(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get my books list");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        DataTransferObj dto = null;

        try {
            System.out.println("Before opening stream");

            // in = new ObjectInputStream(request.getInputStream());
            in
                    = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            dto = (DataTransferObj) in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }

        DataTransferObj serverOut = new DataTransferObj();
        VersionContainer vc = dto.getVc();
        if (null == vc) {
            serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
            sendGZIP(response, serverOut);
            return;
        }

        String versionComp = versionContainer.compareForServerOps(vc);
        if (null != versionComp) {
            serverOut.setMsg(new StringBuffer(versionComp));
            sendGZIP(response, serverOut);
            return;
        }

        Vector inputFromClient = (Vector) dto.getObj();
        Vector userInfo = (Vector)inputFromClient.get(0);
        String userName = (String) userInfo.elementAt(0);
        String password = String.copyValueOf((char[]) userInfo.elementAt(1));
        password = convertPassword(userName, password);

//        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));

        ArrayList<Book> books = dataIO.getMyBooks(userName, password, CLASSIFICATION_VERSION_SID);
        serverOut.setObj(books);
        sendGZIP(response, serverOut);

    }
    
  public void getMyBooksOrig(HttpServletRequest request, HttpServletResponse response){
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
      
//      DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));

      
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
        ObjectInputStream in = null;
        DataTransferObj dto = null;

        try {
            System.out.println("Before opening stream");

            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            dto = (DataTransferObj)in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        
        DataTransferObj serverOut = new DataTransferObj();
        VersionContainer vc = dto.getVc();
        if (null == vc) {
            serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
            sendGZIP(response, serverOut);
            return;
        }

        String versionComp = versionContainer.compareForServerOps(vc);
        if (null != versionComp) {
            serverOut.setMsg(new StringBuffer(versionComp));
            sendGZIP(response, serverOut);
            return;
        }
        
        if (false == UPDATE_ALLOWED.booleanValue()) {
            Vector outputInfo = new Vector(1);
            serverOut.setObj(new TransferInfo(MSG_ERROR_OPERATION_NOT_PERMITTED));
            sendGZIP(response, outputInfo);
            return;
        }        
        Vector clientInput = (Vector) dto.getObj();

     Vector userInfo = (Vector)clientInput.elementAt(0);
     String userName = (String)userInfo.elementAt(0);
     String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
     password = convertPassword(userName, password);
     //String dbUplVersion = (String)applnRequest.elementAt(1);
     Vector bookList = (Vector)clientInput.elementAt(1);
     
     //String db = FixedInfo.getDb(dbUplVersion);
//     DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
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
    serverOut.setObj(outputInfo);



     try {
         System.out.println("Going to send back books from locking / unlocking operation");

         // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
         ObjectOutputStream outputToApplet =
             new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

         System.out.println("Output stream is " +
                            outputToApplet.toString());
         System.out.println("Sending back information");
         outputToApplet.writeObject(serverOut);
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
       
        System.out.println("Going to both lock and unlock books");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        DataTransferObj dto = null;

        try {
            System.out.println("Before opening stream");

            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            dto = (DataTransferObj)in.readObject();
            in.close();
            System.out.println("Finished with stream and closed");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            return;
        } catch (IOException ie) {
            ie.printStackTrace();
            return;
        }
        
        DataTransferObj serverOut = new DataTransferObj();
        VersionContainer vc = dto.getVc();
        if (null == vc) {
            serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
            sendGZIP(response, serverOut);
            return;
        }

        String versionComp = versionContainer.compareForServerOps(vc);
        if (null != versionComp) {
            serverOut.setMsg(new StringBuffer(versionComp));
            sendGZIP(response, serverOut);
            return;
        }
        
        if (false == UPDATE_ALLOWED.booleanValue()) {
            Vector outputInfo = new Vector(1);
            serverOut.setObj(new TransferInfo(MSG_ERROR_OPERATION_NOT_PERMITTED));
            serverOut.setObj(outputInfo);
            sendGZIP(response, serverOut);
            return;
        }        
        Vector clientInput = (Vector) dto.getObj();
        Vector userInfo = (Vector)clientInput.elementAt(0);
        if (null == userInfo) {
            System.out.println("user info is null");
            return;
        }
        String userName = (String)userInfo.elementAt(0);
        String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
        password = convertPassword(userName, password);
        //String dbUplVersion = (String)applnRequest.elementAt(1);
        Vector lockBookList = (Vector)clientInput.elementAt(1);
        Vector unlockBookList = (Vector)clientInput.elementAt(2);
        if (null == lockBookList || null == unlockBookList) {
            return;
        }
        
//        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));        
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
       serverOut.setObj(outputInfo);



        try {
            System.out.println("Going to send back books from lock and  unlock operation");

            // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
            ObjectOutputStream outputToApplet =
                new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

            System.out.println("Output stream is " +
                               outputToApplet.toString());
            System.out.println("Sending back information");
            outputToApplet.writeObject(serverOut);
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
  
    private void allOrganisms(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Going to get all organisms");
        response.setContentType("java/object");
        ObjectInputStream in = null;

        try {
            System.out.println("Before opening stream");
            // in = new ObjectInputStream(request.getInputStream());
            in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
            DataTransferObj clientRequest = (DataTransferObj) in.readObject();
            DataTransferObj serverOut = new DataTransferObj();
            VersionContainer vc = clientRequest.getVc();
            if (null == vc) {
                serverOut.setMsg(new StringBuffer(MSG_ERROR_CANNOT_VERIFY_VERSION_INFO));
                sendGZIP(response, serverOut);
                return;
            }

            String versionComp = versionContainer.compareForServerOps(vc);
            if (null != versionComp) {
                serverOut.setMsg(new StringBuffer(versionComp));
                sendGZIP(response, serverOut);
                return;
            }            

            in.close();
            System.out.println("Finished with stream and closed");

            ArrayList<Organism> orgList = OrganismManager.getInstance().getOrgList();
            serverOut.setObj(orgList);
            sendGZIP(response, serverOut);
        }
        catch(Exception e) {
            
        }    
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
    
  public static String[] getContents(String url){

    try{
      String[] contents = FileUtils.readFileFromURL(new URL(url));
      if (null == contents) {
        System.out.println("Unable to read information from url " + url);
        return null;
      }
      return contents;
    }
    catch (IOException ie){
      System.out.println(ie.getMessage() + " returned while attempting to read from url " + url);
      ie.printStackTrace();
      return null;
    }
    catch (Exception e) {
      System.out.println("Unable to read msa information from url " + url);
      return null;
    }
  }    
    
    public class LoadKeyResidueWorker implements Runnable {        
        public String book;
        public ArrayList<KeyResidue> residueList;
        public LoadKeyResidueWorker(String book) {
            this.book = book;
        }

        public void run() {
            residueList = KeyResiduesManager.getSitesForFamily(book);
        }        
    }
    
    public class LoadMSAWorker implements Runnable {
        public String book;
        MSA msa;
        
        public LoadMSAWorker(String book) {
            this.book = book;
        }
        public void run() {
            msa = DataServlet.getMSA(book, DataServlet.CLASSIFICATION_VERSION_SID);
        }
    }
}
