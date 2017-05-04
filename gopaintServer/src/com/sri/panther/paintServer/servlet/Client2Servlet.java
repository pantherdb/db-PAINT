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
package com.sri.panther.paintServer.servlet;


import com.sri.panther.paintCommon.Classification;
import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.ReadResources;
import com.sri.panther.paintServer.util.ConfigFile;
import com.sri.panther.paintCommon.FixedInfo;
import com.sri.panther.paintCommon.RawComponentContainer;
import com.sri.panther.paintCommon.TransferInfo;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintServer.database.DataServer;
import com.sri.panther.paintServer.database.DataServerManager;
import com.sri.panther.paintCommon.familyLibrary.EntryType;
import com.sri.panther.paintCommon.familyLibrary.FileNameGenerator;
import com.sri.panther.paintCommon.familyLibrary.LibrarySettings;
import com.sri.panther.paintCommon.familyLibrary.PAINTFile;
import com.sri.panther.paintCommon.util.FileUtils;

import com.sri.panther.paintCommon.util.Utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.log4j.Logger;


public class Client2Servlet extends HttpServlet{
    public static final String CHAR_ENCODING = "UTF-8";
    public static final String REQUEST_LOCK_BOOKS = "LockBooks";
    public static final String REQUEST_UNLOCK_BOOKS = "UnlockBooks";
    public static final String REQUEST_SEARCH_GENE_NAME = "searchGeneName";
    public static final String REQUEST_SEARCH_GENE_EXT_ID = "searchGeneExtId";
    public static final String REQUEST_SEARCH_PROTEIN_EXT_ID = "searchProteinExtId";
    public static final String REQUEST_SEARCH_DEFINITION = "searchDefinition";
    public static final String REQUEST_SEARCH_ALL_BOOKS = "allBooks";
    public static final String REQUEST_SEARCH_UNCURATED_BOOKS = "uncuratedBooks";
    public static final String REQUEST_MY_BOOKS = "MyBooks";
    public static final String REQUEST_OPEN_BOOK_FOR_GO_USR = "openBookForGOUsr";
    public static final String REQUEST_GET_ATTRIBUTE_TABLE = "getAttributeTable";
    public static final String REQUEST_LOCK_UNLOCK_BOOKS = "LockUnLockBooks";
    public static final String REQUEST_GET_EVIDENCE_SF_LOCK = "getEvidenceSubfamilyLock";
    public static final String REQUEST_GET_EVIDENCE_LEAF_LOCK = "getEvidenceALeafLock";
    public static final String REQUEST_EVIDENCE_SAVE_SUBFAMILY = "saveSubFamilyEvidence";
    public static final String REQUEST_EVIDENCE_SAVE_SEQUENCE = "saveSequenceEvidence";
    public static final String REQUEST_UNLOCK_SEQUENCE = "unlockSequence";
    public static final String REQUEST_UNLOCK_SUBFAMILY = "unlockSubFamily";
    
    public static final int SEARCH_TYPE_GENE_SYMBOL = 0;
    public static final int SEARCH_TYPE_GENE_ID = 1;
    public static final int SEARCH_TYPE_PROTEIN_ID = 2;
    public static final int SEARCH_TYPE_DEFINITION = 3;
    public static final int SEARCH_TYPE_ALL_BOOKS = 4;
    public static final int SEARCH_TYPE_UNCURATED_BOOKS = 5;
    
    public static final int REQUEST_TYPE_LOCK = 0;
    public static final int REQUEST_TYPE_UNLOCK = 1;
    
    
    
    public static final String STRING_EMPTY = "";
    protected static final String MSG_ERR_INVALID_DB = " database is invalid";
    protected static final String MSG_LOADED_PROPERTY_FILE = "Loaded property file";
    protected static final String MSG_RESOURCE_SET_PAINT = "set paint resource type";
    protected static final String MSG_RESOURCE_SET_TREE = "set tree resource type";
    
    public static final String MSG_UNABLE_TO_RETRIEVE_LOCKED_BOOKS = "Unable to retrieve locked books.";
    
    protected static final String APP_SCOPE_RESOURCE_PAINT_PROPERTIES = "paintProperties";
    protected static final String APP_SCOPE_RESOURCE_TREE_PROPERTIES = "treeViewerProperties";
    protected static final String PROPERTY_FILE_PAINT = "paint";

    
    protected static final String APP_SCOPE_PAINT_FIXED_RESOURCE = "fixedTreeInfo";
    //protected static final String APP_SCOPE_PAINT_PANTHER_GO_SLIM = "panther_go_slim";
    protected static final String APP_SCOPE_CLS_TABLE = "clsTable";
    
    protected static final Logger logger = Logger.getLogger(Client2Servlet.class.getName());
    
    protected static final String PROPERTY_TREE_DB = "db.jdbc.dbsid";
    protected static final String PROPERTY_USER_SERVER = "server_usr_info";
    protected static final String PROPERTY_SERVLET_USER_INFO = "server_usr_info_check";
    
    protected static final String ACTION_VERIFY_USER = "VerifyUserInfo";
    
    private static final String SERVLET_CONNECTION_CONTENT_TYPE = "Content-Type";
    private static final String SERVLET_CONNECTION_OBJECT_TYPE_JAVA = "java/object";
    private static final String SERVLET_REQUEST_PROPERTY_COOKIE = "Cookie";
    protected static final String DELIM_TREE_DB = ",";

    protected static final String STR_EMPTY = "";
    
    
    protected static final String PROPERTY_SUFFIX_MSA_LIB_ROOT = "_msa_lib_root";
    protected static final String PROPERTY_SUFFIX_LIB_ROOT = "_lib_root";
    protected static final String PROPERTY_SUFFIX_LIB_ENTRY_TYPE = "_entry_type";
    protected static final String PROPERTY_SUFFIX_BOOK_TYPE = "_book_type";
    
    
    protected static final String PROPERTY_SUFFIX_FLAT_LIB_ROOT = "_lib_flat_trees";
    protected static final String PROPERTY_SUFFIX_FLAT_LIB_ENTRY_TYPE = "_lib_flat_entry_type";

    
    protected static final String FILE_PANTHER_GO_SLIM = "PANTHER_GO_Slim";
    

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   *
   * @see
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    String  actionParam;
    if (null == request){
      System.out.println("request is null");
      return;
    }
    actionParam = request.getParameter("action");
    if (null == actionParam){
      System.out.println("No action parameter specified");
      return;
    }
    if (actionParam.compareTo("FixedInfo") == 0){
      fixedInfo(request, response);
      return;
    }

//    else if (actionParam.compareTo("getConfInfo") == 0) {
//      getConfCodeInfo(request, response);
//      return;
//    }
    else if (actionParam.compareTo("setFixedInfo") == 0) {
      setFixedInfo(request, response);
      return;
    }
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   *
   * @see
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
    String  actionParam;
    if (null == request){
      System.out.println("request is null");
      return;
    }
    actionParam = request.getParameter("action");
    if (null == actionParam){
      System.out.println("No action parameter specified");
      return;
    }
    if (actionParam.compareTo("VerifyUserInfo") == 0){
      verifyUser(request, response);
      return;
    }
    if (actionParam.compareTo("GetUserInfo") == 0){
      getUserInfo(request, response);
      return;
    }
    else if (actionParam.compareTo("BookListForLocking") == 0){
      //bookListForLocking(request, response);
      return;
    }
    else if (actionParam.compareTo("LockBook") == 0){
      //lockBook(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_LOCK_BOOKS) == 0) {
       //lockBooks(request, response); 
    }
//    // Temporarily all open books are going to open book file from flat file system
//    else if (actionParam.compareTo("OpenBook") == 0){
//      openBookFile(request, response);
//      return;
//    }
      else if (actionParam.compareTo("OpenBook") == 0){
        openBook(request, response, false);
        return;
      }
      else if (actionParam.equals(REQUEST_OPEN_BOOK_FOR_GO_USR)) {
          openBook(request, response, true);
          return;
      }
      else if (actionParam.equals(REQUEST_GET_ATTRIBUTE_TABLE)) {
          getAttrTable(request, response);
          return;
      }
    else if (actionParam.compareTo("OpenBookFile") == 0){
      openBookFile(request, response);
      return;
    }
    else if (actionParam.compareTo("BookListForUnlocking") == 0){
      //bookListForUnlocking(request, response);
      return;
    }

    else if (actionParam.compareTo(REQUEST_UNLOCK_BOOKS) == 0){
        //unlockBooks(request, response);
    return;
    }
    else if (actionParam.compareTo(REQUEST_LOCK_UNLOCK_BOOKS) == 0){
        //lockUnlockBooks(request, response);
        return;
    }
      
    else if (actionParam.compareTo("saveBook") == 0){
      //saveBook(request, response);
      return;
    }
    else if (actionParam.compareTo("BookList") == 0){
      bookList(request, response);
      return;
    }
    else if (actionParam.compareTo("WriteMSAInfo") == 0){
      //writeMSA(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_GET_EVIDENCE_SF_LOCK) == 0){
      //getSubFamilyEvidences(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_GET_EVIDENCE_LEAF_LOCK) == 0){
      //getSequenceEvidences(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_EVIDENCE_SAVE_SUBFAMILY) == 0){
      //saveSubfamilyEvidences(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_EVIDENCE_SAVE_SEQUENCE) == 0){
      //saveSequenceEvidences(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_UNLOCK_SEQUENCE) == 0){
      //unlockSequence(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_UNLOCK_SUBFAMILY) == 0){
      //unlockSubfamily(request, response);
      return;
    }
    else if (actionParam.compareTo("setVM") == 0){
      setVM(request, response);
      return;
    }
    else if (actionParam.compareTo("requestClsInfo") == 0){
        getPantherClsData(request, response);
        return;
    }
    else if (actionParam.compareTo(REQUEST_SEARCH_GENE_NAME) == 0){
        searchGeneName(request, response);
    return;
    }
    else if (actionParam.compareTo(REQUEST_SEARCH_GENE_EXT_ID) == 0){
       searchGeneId(request, response);
       return;
    }
    else if (actionParam.compareTo(REQUEST_SEARCH_PROTEIN_EXT_ID) == 0){
      searchProteinId(request, response);
      return;
    }
    else if (actionParam.compareTo(REQUEST_SEARCH_DEFINITION) == 0) {
        searchDefinition(request, response);
        return;
    }
    else if (actionParam.compareTo(REQUEST_SEARCH_ALL_BOOKS) == 0) {
        searchAllBooks(request, response);
    }
    else if (actionParam.compareTo(REQUEST_SEARCH_UNCURATED_BOOKS) == 0) {
        searchUncuratedBooks(request, response);
    }
    else if (actionParam.compareTo(REQUEST_MY_BOOKS) == 0) {
        getMyBooks(request, response);
        return;
    }
      else if (actionParam.compareTo("getSubFamilyEvidence") == 0){
        getSubFamilyEvidences(request, response);
        return;
      }
      else if (actionParam.compareTo("getSequenceEvidence") == 0){
        getSequenceEvidences(request, response);
        return;
      }
      else if (actionParam.compareTo("saveSubFamilyEvidence") == 0){
        //saveSubfamilyEvidences(request, response);
        return;
      }
      else if (actionParam.compareTo("saveSequenceEvidence") == 0){
        //saveSequenceEvidences(request, response);
        return;
      }
      else if (actionParam.compareTo("unlockSequence") == 0){
        //unlockSequence(request, response);
        return;
      }
      else if (actionParam.compareTo("unlockSubFamily") == 0){
        //unlockSubfamily(request, response);
        return;
      }

    else{
      System.out.println("No action handler for " + actionParam);
    }
  }

  /**
   * Create the application scope objects.
   */
  protected void initAppObjects(){
    ReadResources            paintProperties =
      (ReadResources) getServletContext().getAttribute(APP_SCOPE_RESOURCE_PAINT_PROPERTIES);

      ReadResources            treeProperties =
        (ReadResources) getServletContext().getAttribute(APP_SCOPE_RESOURCE_TREE_PROPERTIES);


    // have not created paintProperties object
    if (null == paintProperties){
      paintProperties = new ReadResources();
      getServletContext().setAttribute(APP_SCOPE_RESOURCE_PAINT_PROPERTIES, paintProperties);
      logger.debug(MSG_LOADED_PROPERTY_FILE);
    }

    try{
      if (false == paintProperties.isInitialized()){
        paintProperties.setResource(PROPERTY_FILE_PAINT);
        logger.debug(MSG_RESOURCE_SET_PAINT);
      }
      
      // have not created treeProperties object
      if (null == treeProperties) {
          treeProperties = new ReadResources();
          treeProperties.setResource(FixedInfo.PROPERTY_FILE_TREE);
          logger.debug(MSG_RESOURCE_SET_TREE);
          getServletContext().setAttribute(APP_SCOPE_RESOURCE_TREE_PROPERTIES, treeProperties);          
      }

        com.sri.panther.paintCommon.FixedInfo  fixedInfo =
          (com.sri.panther.paintCommon.FixedInfo) getServletContext().getAttribute(APP_SCOPE_PAINT_FIXED_RESOURCE);
      // FixedInfo object
      if (null == fixedInfo){
        fixedInfo = new FixedInfo(treeProperties);

        // Query database and get upl's as well as if they are frozen, etc
        // For now create some fake data
        // Hashtable ht = new Hashtable();
        // Vector v1 = new Vector();
        // v1.addElement(new Boolean(true));
        // v1.addElement(new Integer(21));
        // Vector v2 = new Vector();
        // v2.addElement(new Boolean(true));
        // v2.addElement(new Integer(22));
        // Vector v3 = new Vector();
        // v3.addElement(new Boolean(true));
        // v3.addElement(new Integer(23));
        // Vector v4 = new Vector();
        // v4.addElement(new Boolean(false));
        // v4.addElement(new Integer(24));
        // ht.put("1.0", v1);
        // ht.put("2.0", v2);
        // ht.put("3.0", v3);
        // ht.put("4.0", v4);

        Hashtable dbToUPL = new Hashtable();
        String dbListStr = ConfigFile.getProperty(PROPERTY_TREE_DB);
        String dbList[] = Utils.tokenize(dbListStr, DELIM_TREE_DB);
        String supportedClsId = ConfigFile.getProperty(DataServer.PROPERTY_CLASSIFICATION_VERSION_SID);
        for (int i = 0; i < dbList.length; i++) {
          
          DataServer ds = DataServerManager.getDataServer(dbList[i]);
          Hashtable clsLookupTbl = ds.getClsLookup();
          
          //Remove cls information for classificaion versions that are not handled
          Enumeration <String> clsIdEnum =  (Enumeration <String>)clsLookupTbl.keys();
          while (clsIdEnum.hasMoreElements()) {
              String currentClsId = clsIdEnum.nextElement();
              if (false == currentClsId.equals(supportedClsId)) {
                  clsLookupTbl.remove(currentClsId);
              }
          }
          dbToUPL.put(dbList[i], clsLookupTbl);
        }
        fixedInfo.setDbToUPLInfo(dbToUPL);
        
        getServletContext().setAttribute(APP_SCOPE_PAINT_FIXED_RESOURCE, fixedInfo);
        
        Hashtable clsTable = (Hashtable)getServletContext().getAttribute(APP_SCOPE_CLS_TABLE);
        // Get PANTHER GO Slim data from database.  Temporarily read from flat file system
//        if (null == clsTable) {
//            clsTable = new Hashtable();
//            
//            
//              for (int i = 0; i < dbList.length; i++) {
//                Hashtable uplInfoLookup = (Hashtable)dbToUPL.get(dbList[i]);
//                Hashtable clsVerToHrchyTbl = new Hashtable();
//    
//                if (null == uplInfoLookup){
//                  System.out.println("Upl info lookup is null");
//                  return;
//                }
//                Enumeration keys = uplInfoLookup.keys();
//    
//                while (keys.hasMoreElements()){
//                  String  clsVerId = (String) keys.nextElement();
//                  
//                  GoSlimManager slimManager = readGOSlim(clsVerId);
//                  if (null == slimManager) {
//                      System.out.println("Unable to read PANTHER GO SLIM data for " + clsVerId);
//                      continue;
//                  }
//                  clsVerToHrchyTbl.put(clsVerId, slimManager);
//                  System.out.println("Retrieved classification for cls version " + clsVerId);
//                }
//                clsTable.put(dbList[i], clsVerToHrchyTbl);
//              }
//              getServletContext().setAttribute(APP_SCOPE_PAINT_PANTHER_GO_SLIM, clsTable);
//              System.out.println("Added clsTable to application scope object");
//        }

            
        
        
        
    
        if (null == clsTable){
          clsTable = new Hashtable();

          for (int i = 0; i < dbList.length; i++) {
            Hashtable uplInfoLookup = (Hashtable)dbToUPL.get(dbList[i]);
            Hashtable clsVerToHrchyTbl = new Hashtable();

            if (null == uplInfoLookup){
              System.out.println("Upl info lookup is null");
              return;
            }
            Enumeration keys = uplInfoLookup.keys();

            while (keys.hasMoreElements()){
              String  clsVerId = (String) keys.nextElement();
              DataServer ds = (DataServer)DataServerManager.getDataServer(dbList[i]);
              Vector hierarchyData = (Vector)ds.getClsHierarchyData(clsVerId, true);
              Classification root = Classification.parseClassificationData(hierarchyData);
              if (null != root) {
                clsVerToHrchyTbl.put(clsVerId, root);
              }
              System.out.println("Retrieved classification for cls version " + clsVerId);
            }
            clsTable.put(dbList[i], clsVerToHrchyTbl);
          }
          getServletContext().setAttribute(APP_SCOPE_CLS_TABLE, clsTable);
          System.out.println("Added clsTable to application scope object");
        }
      }
      
      
//      // Confidence code information has not been retrieved
//      if (null == confCodeTbl) {
//        confCodeTbl = new Hashtable();
//        
//        // Get list of databases
//        String dbListStr = ConfigFile.getProperty("treeSrvrDb");
//        String dbList[] = Utils.tokenize(dbListStr, ",");
//        
//        // Retrieve confidence codes for each database
//        for (int i = 0; i < dbList.length; i++) {
//          DataServer ds = DataServerManager.getDataServer(dbList[i]);
//          confCodeTbl.put(dbList[i], ds.getEvidenceTypes());
//          System.out.println("Storing confidence code for " + dbList[i]);          
//        }
//        
//        // Add to servlet context
//        getServletContext().setAttribute("confCodeTbl", confCodeTbl);
//        System.out.println("Added confidence code table to application scope object");        
//      }
    }
    catch (Exception e){
      System.out.println("Exception while initializing application scope objects, exception " + e.getMessage()
                         + " has been returned.");
    }
  }
  
  
//  public GoSlimManager readGOSlim(String clsId) {
//    String path = FileUtils.appendFileToPath(ConfigFile.getProperty(clsId + PROPERTY_SUFFIX_LIB_ROOT), FILE_PANTHER_GO_SLIM);
//    try {
//        URL pathURL = new URL(path);
//        String goInfo[] = FileUtils.readFileFromURL(pathURL);
//        
//        Vector<PantherGoSlim> roots = PantherGoSlim.parseData(goInfo);
//        if (null == roots || 0 == roots.size()) {
//            System.out.println("Unable to parse go data from " + path);
//            return null;
//        }
//        return new GoSlimManager(roots);
//
//    }
//    catch(Exception e) {
//        e.printStackTrace();
//        return null;
//    }
//  }

  /**
   * Method declaration
   *
   *
   * @param userInfo Contains two elements.  First element is a
   * vector containing the userid and password and Second element
   * is the database/clsid string.
   *
   * @return
   *
   * @see
   */
  protected String verifyUserInfo(Vector userInfo){
    Vector user = (Vector)userInfo.elementAt(0);

    String  userName = (String) user.elementAt(0);
    String  password = String.copyValueOf((char[]) user.elementAt(1));
    String db = FixedInfo.getDb((String)userInfo.elementAt(1));
    
    password = convertPassword(userName, password);
    // Handling to verify user information from database
    DataServer ds = DataServerManager.getDataServer(db);
    boolean userInfoOK = ds.verifyUserInfo(userName, password);

    if (true == userInfoOK){
      return Constant.STR_EMPTY;
    }
    else{
      return "Invalid login information";
    }
  }

  /**
   * Method declaration
   *
   *
   * @param userInfo
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  protected String[] getBookListForLockingByUser(Vector userInfo, String dbClsId){
    String  userName = (String) userInfo.elementAt(0);
    String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
    password = convertPassword(userName, password);
    String db = FixedInfo.getDb(dbClsId);
    String clsId = FixedInfo.getCls(dbClsId);


    return DataServerManager.getDataServer(db).getBookListForLocking(userName, password, clsId);
  }
  
  public void lockBooks(HttpServletRequest request, HttpServletResponse response){
     lockUnlockBooks(request, response, REQUEST_TYPE_LOCK);
  }
  
  
  public void unlockBooks(HttpServletRequest request, HttpServletResponse response){
      lockUnlockBooks(request, response, REQUEST_TYPE_UNLOCK);
  }
  
 private void lockUnlockBooks (HttpServletRequest request, HttpServletResponse response, int requestType){
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
     String dbUplVersion = (String)applnRequest.elementAt(1);
     Vector bookList = (Vector)applnRequest.elementAt(2);
     
     String db = FixedInfo.getDb(dbUplVersion);
     DataServer ds = DataServerManager.getDataServer(db);
     String uplVersion = FixedInfo.getCls(dbUplVersion);
     
     String operationInfo = null;
     if (REQUEST_TYPE_LOCK == requestType) {
        if (null == ds) {
            System.out.println("ds is null");
        }
        else if (null == userInfo) {
            System.out.println("user info is null");
        }
         operationInfo = ds.lockBooks(userName, password, uplVersion, bookList);
     }
     else if (REQUEST_TYPE_UNLOCK == requestType){
         operationInfo = ds.unlockBooks(userName, password, uplVersion, bookList);
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
        String dbUplVersion = (String)applnRequest.elementAt(1);
        Vector lockBookList = (Vector)applnRequest.elementAt(2);
        Vector unlockBookList = (Vector)applnRequest.elementAt(3);
        if (null == lockBookList || null == unlockBookList) {
            return;
        }
        
        String db = FixedInfo.getDb(dbUplVersion);
        DataServer ds = DataServerManager.getDataServer(db);
        String uplVersion = FixedInfo.getCls(dbUplVersion);
        
        String operationInfo = null;
        if (0 != lockBookList.size()) {
            operationInfo = ds.lockBooks(userName, password, uplVersion, lockBookList);
        }
        else {
            operationInfo = Constant.STR_EMPTY;
        }
        if (0 != unlockBookList.size()) {
            if (0 == operationInfo.length()) {
                operationInfo = ds.unlockBooks(userName, password, uplVersion, unlockBookList);
            }
            else {
                operationInfo += Constant.STR_NEWLINE + ds.unlockBooks(userName, password, uplVersion, unlockBookList);
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


  

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void lockBook(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to lock book for user");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

    // Verify user information first
    Vector  userInfo = (Vector) applnRequest.elementAt(0);

    // String userInfoValid = verifyUserInfo(userInfo);
    String  dbClsId = (String) applnRequest.elementAt(1);
    String  book = (String) applnRequest.elementAt(2);    // Book user requesting to lock
    Vector  outputInfo = new Vector();

    // User information is valid
    // if (0 == userInfoValid.length()) {
    // lock book for user
    String  lockMsg = lockBook(userInfo, dbClsId, book);

    if (0 == lockMsg.length()){
      outputInfo.addElement(new TransferInfo(""));    // Indicate success
    }
    else{
      outputInfo.addElement(new TransferInfo(lockMsg));
    }

    // }
    // else {
    // // user information is not valid, indicate error
    // outputInfo.addElement(new TransferInfo(userInfoValid));
    // }
    try{
      System.out.println("Sending back lock book operation information now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
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
    
    public void searchAllBooks(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_ALL_BOOKS);
        
    }
    public void searchUncuratedBooks(HttpServletRequest request, HttpServletResponse response) {
        searchBooks(request, response, SEARCH_TYPE_UNCURATED_BOOKS);
        
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
        String searchField = (String)applnRequest.elementAt(0);
        String dbUplVersion = (String)applnRequest.elementAt(1);

        String db = FixedInfo.getDb(dbUplVersion);
        DataServer ds = DataServerManager.getDataServer(db);
        String uplVersion = FixedInfo.getCls(dbUplVersion);
        Vector books;
        if (SEARCH_TYPE_GENE_SYMBOL == type) {
            books = ds.searchBooksByGeneSymbol(searchField, uplVersion);
        } else if (SEARCH_TYPE_GENE_ID == type) {
            books = ds.searchBooksByGenePrimaryExtAcc(searchField, uplVersion);
        } else if (SEARCH_TYPE_PROTEIN_ID == type){
            books = ds.searchBooksByProteinPrimaryExtId(searchField, uplVersion);
        }
        else if (SEARCH_TYPE_DEFINITION == type) {
            books = ds.searchBooksByDefinition(searchField, uplVersion);           
        }
        else if (SEARCH_TYPE_ALL_BOOKS == type) {
            books = ds.getAllBooks(uplVersion);
        }
        else if (SEARCH_TYPE_UNCURATED_BOOKS == type) {
            books = ds.getUncuratedUnlockedBooks(uplVersion);
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


  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void setVM(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to set vm information");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    String            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (String) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    Vector  objs = new Vector();

    if (null == applnRequest){
      objs.addElement(new TransferInfo("VM information is null"));
    }
    else{
      String  systemVM = (String) request.getSession().getAttribute("vmStr");

      systemVM = applnRequest;
      request.getSession().setAttribute("vmStr", systemVM);
      objs.addElement(new TransferInfo(""));
    }
    try{
      System.out.println("Sending back vm operation information now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(objs);
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
    System.out.println("Exitting servlet now");
  }
  
  public Vector getAttrInfo(Vector requestInfo) {
      // Verify user information first
      Vector  userInfo = (Vector) requestInfo.elementAt(0);
      String  dbUplVersion = (String) requestInfo.elementAt(1);
      String  book = (String) requestInfo.elementAt(2);    // Book user requesting to lock
      Vector  objs = new Vector();

      String db = FixedInfo.getDb(dbUplVersion);
      DataServer ds = DataServerManager.getDataServer(db);
      String uplVersion = FixedInfo.getCls(dbUplVersion);

      try{
        String  userName = (String) userInfo.elementAt(0);
        String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
        password = convertPassword(userName, password);
        String  userIdStr = ds.getUserId(userName, password);

        if (null == userIdStr){
          objs.addElement(new TransferInfo("Unable to get user id for user"));
        }
        else{
          System.out.println("Validated user information");
          System.out.println("User info is valid, going to try and get book information now");
          RawComponentContainer rc = null;

          System.out.println(" book is " + book + " db and upl version is " + dbUplVersion + " user id is " + userIdStr);
          Vector <String[]>         attrInfo = ds.getAttrTableAndSfInfo(book, uplVersion, userIdStr, userName, password);
          String          attrTable[] = null;
          String          sfAnInfo[] = null;
          if (null != attrInfo) {
              attrTable = attrInfo.get(ds.INDEX_ATTR_METHOD_ATTR_TBL);
              sfAnInfo = attrInfo.get(ds.INDEX_ATTR_METHOD_SF_AN_INFO);
          }
          
          if (null == attrTable){
            System.out.println("AttrTable is null");
          }


          if (null != attrTable){
            rc = new RawComponentContainer();
            rc.setAttributeTable(attrTable);
            rc.setBook(book);
          }
          
          if (null == rc){
            objs.addElement(new TransferInfo("Server is unable to access book"));
            rc = new RawComponentContainer();
          }
          else{
            objs.addElement(new TransferInfo(""));
          }
          objs.addElement(rc);
          System.out.println("Added attribute object for transferring");
        }
      }
      catch (Exception e){
        System.out.println("Exception while reading book " + e.getMessage() + " has been returned.");
        e.printStackTrace();
        objs.addElement(new TransferInfo("Error reading book"));
      }
      return objs;

  }

  
  
  public Vector getBookInfo(Vector requestInfo, boolean isGOUser) {
      // Verify user information first
      Vector  userInfo = (Vector) requestInfo.elementAt(0);
      String  dbUplVersion = (String) requestInfo.elementAt(1);
      String  book = (String) requestInfo.elementAt(2);    // Book user requesting to lock
      Vector  objs = new Vector();

      String db = FixedInfo.getDb(dbUplVersion);
      DataServer ds = DataServerManager.getDataServer(db);
      String uplVersion = FixedInfo.getCls(dbUplVersion);

      try{
        String  userName = (String) userInfo.elementAt(0);
        String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
        password = convertPassword(userName, password);
        String  userIdStr = ds.getUserId(userName, password);

        if (null == userIdStr){
          objs.addElement(new TransferInfo("Unable to get user id for user"));
        }
        else{
          System.out.println("Validated user information");
          System.out.println("User info is valid, going to try and get book information now");
          RawComponentContainer rc = null;

          System.out.println(" book is " + book + " db and upl version is " + dbUplVersion + " user id is " + userIdStr);
          Vector <String[]>         attrInfo = ds.getAttrTableAndSfInfo(book, uplVersion, userIdStr, userName, password);
          String          attrTable[] = null;
          String          sfAnInfo[] = null;
          if (null != attrInfo) {
              attrTable = attrInfo.get(ds.INDEX_ATTR_METHOD_ATTR_TBL);
              sfAnInfo = attrInfo.get(ds.INDEX_ATTR_METHOD_SF_AN_INFO);
          }
          
          // Check for flat file tree - if it exists, then use the flat file.  Else retrieve from database
          Object tree = getTree(ds, book, uplVersion);
          String          famName = ds.getFamilyName(book, uplVersion);

          Object  MSAInfo = getMSA(book, uplVersion);

          if (null == MSAInfo){
            System.out.println("MSA information is null");
          }
          if (null == tree){
            System.out.println("Tree is null");
          }
          if (null == attrTable){
            System.out.println("AttrTable is null");
          }
          
          // Put tree info into a vector
          Vector treeObjectList = new Vector(3);
          treeObjectList.setSize(3);
          treeObjectList.add(RawComponentContainer.INDEX_TREE_STR, tree);
          treeObjectList.add(RawComponentContainer.INDEX_SF_AN, sfAnInfo);

          if ((null != attrTable) && (null != tree)){
            rc = new RawComponentContainer();
            rc.setAttributeTable(attrTable);
            rc.setTree(treeObjectList);
            rc.setBook(book);
            rc.setName(famName);
            rc.setMSA(MSAInfo);
            
            
            if (true == isGOUser) {
                Hashtable goInferenceTbl = ds.formatAnnotIdForGO(ds.getGOInference(book, uplVersion));
                Hashtable goAnnotTbl = ds.formatAnnotIdForGO(ds.getGOAnnotation(book, uplVersion));
                Vector v = new Vector(2);
                v.setSize(2);
                v.add(RawComponentContainer.INDEX_GO_ANNOT, goAnnotTbl);
                v.add(RawComponentContainer.INDEX_GO_INFER, goInferenceTbl);
                rc.setGOInfo(v);
            }
            else {
                // Get PANTHER annotation information
                Hashtable annotTbl = ds.getAnnotations(book, uplVersion);
                 Vector v = new Vector(3);
                v.setSize(3);
                v.add(RawComponentContainer.INDEX_ANNOT_CURRENT, ds.formatAnnotIdForTbl(annotTbl));
                rc.setPANTHERInfo(v);
            }
          }
          if (null == rc){
            objs.addElement(new TransferInfo("Server is unable to access book"));
            rc = new RawComponentContainer();
          }
          else{
            objs.addElement(new TransferInfo(""));
          }
          objs.addElement(rc);
          System.out.println("Added objects for transferring");
        }
      }
      catch (Exception e){
        System.out.println("Exception while reading book " + e.getMessage() + " has been returned.");
        e.printStackTrace();
        objs.addElement(new TransferInfo("Error reading book"));
      }
      return objs;

  }
  
  public static Object getMSA(String book, String uplVersion) {
      LibrarySettings libSettings = null;
      EntryType       et = null;
      String          msaEntryType = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ENTRY_TYPE);

      if ((null == msaEntryType) || (0 == msaEntryType.length())) {}
      else{
        et = new EntryType();
        et.setEntryType(msaEntryType);
      }
      libSettings = new LibrarySettings(book, et, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_BOOK_TYPE), ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ROOT));
      return getMSA(libSettings);
  }
  
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
          String  path = FileUtils.getURLPath(paintName);
          String treeName = pf.getTreeFileName();
            
          tree = getContents(FileUtils.appendFileToPath(path, treeName));
      }
      catch (Exception e) {
          // Tree is not available from flat file - get it from database
          if (null == tree) {
            tree = ds.getTree(book, uplVersion);
          }

      }
      return tree;

  }
  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void openBook(HttpServletRequest request, HttpServletResponse response, boolean isGOUser){
    System.out.println("Going to attempt to open book for user");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    
    Vector objs = getBookInfo(applnRequest, isGOUser);

    try{
      System.out.println("Sending back open book operation information now");

      // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(objs);
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
    System.out.println("Exitting servlet now");
  }
  
  
  public void getAttrTable(HttpServletRequest request, HttpServletResponse response){
      System.out.println("Going to attempt to get attribute table for user");

      response.setContentType("java/object");
      ObjectInputStream in = null;
      Vector            applnRequest = null;

      try{
        System.out.println("Before opening stream");
        in = new ObjectInputStream(request.getInputStream());
        applnRequest = (Vector) in.readObject();
        in.close();
        System.out.println("Finished with stream and closed");
      }
      catch (ClassNotFoundException cnfe){
        cnfe.printStackTrace();
        return;
      }
      catch (IOException ie){
        ie.printStackTrace();
        return;
      }
      
      Vector objs = getAttrInfo(applnRequest);

      try{
        System.out.println("Sending back get attribute table operation information now");

        // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
        ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

        System.out.println("Output stream is " + outputToApplet.toString());
        System.out.println("Sending back information");
        outputToApplet.writeObject(objs);
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
      System.out.println("Exitting servlet now");
  }

  public void openBookFile(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to open book file for user");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

        // Verify user information first
        Vector userInfo = (Vector)applnRequest.elementAt(0);
        String dbUplVersion = (String)applnRequest.elementAt(1);
        String book =
            (String)applnRequest.elementAt(2); // Book user requesting to lock

        String db = FixedInfo.getDb(dbUplVersion);
        DataServer ds = DataServerManager.getDataServer(db);
        String uplVersion = FixedInfo.getCls(dbUplVersion);

        Vector  objs = new Vector();

    try{
      String  userName = (String) userInfo.elementAt(0);
      String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
      password = convertPassword(userName, password);
      String  userIdStr = ds.getUserId(userName, password);

      if (null == userIdStr){
        objs.addElement(new TransferInfo("Unable to get user id for user"));
      }
      else{
        System.out.println("Validated user information");
        System.out.println("User info is valid, going to try and get book information now");
        RawComponentContainer rc = null;

        System.out.println(" book is " + book + " user id is " + userIdStr);
        // Get the book information from library
        LibrarySettings libSettings = null;
        EntryType       et = null;
        String          libEntryType = ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ENTRY_TYPE);
        if ((null == libEntryType) || (0 == libEntryType.length())) {}
        else{
          et = new EntryType();
          et.setEntryType(libEntryType);
        }
        libSettings = new LibrarySettings(book, et, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_BOOK_TYPE ), null, ConfigFile.getProperty(uplVersion + PROPERTY_SUFFIX_LIB_ROOT), true);
        String paintName = FileNameGenerator.getPAINTPathName(libSettings);
        PAINTFile pf = PAINTFile.readPAINTFileURL(paintName);
        String  path = FileUtils.getURLPath(paintName);
        String treeName = pf.getTreeFileName();
        String attrName = pf.getAttrFileName();
        String sfAnName = pf.getSfanFileName();
    


        Object treeInfo = getContents(FileUtils.appendFileToPath(path, treeName));
        Object sfAnInfo = null;
        if (null != sfAnName) {
            sfAnInfo = getContents(FileUtils.appendFileToPath(path, sfAnName));
        }
        Object attrInfo = getContents(FileUtils.appendFileToPath(path, attrName));
        
        Vector treeObjectList = new Vector(3);
        treeObjectList.setSize(3);
        treeObjectList.add(RawComponentContainer.INDEX_TREE_STR, treeInfo);
        if (null != sfAnInfo) {
            treeObjectList.add(RawComponentContainer.INDEX_SF_AN, sfAnInfo);
        }
        Object  MSAInfo = getMSA(libSettings);
        String          famName = book;

        if (null == MSAInfo){
          System.out.println("MSA information is null");
        }
        if (null == treeInfo){
          System.out.println("Tree is null");
        }
        if (null == attrInfo){
          System.out.println("AttrTable is null");
        }
        if ((null != attrInfo) && (null != treeInfo)){
          rc = new RawComponentContainer();
          rc.setAttributeTable(attrInfo);
          rc.setTree(treeObjectList);
          rc.setBook(book);
          rc.setName(famName);
          rc.setMSA(MSAInfo);
        }
        if (null == rc){
          objs.addElement(new TransferInfo("Server is unable to access book"));
          rc = new RawComponentContainer();
        }
        else{
          objs.addElement(new TransferInfo(""));
        }
        objs.addElement(rc);
        System.out.println("Added objects for transferring");
      }
    }
    catch (Exception e){
      System.out.println("Exception while reading book " + e.getMessage() + " has been returned.");
      e.printStackTrace();
      objs.addElement(new TransferInfo("Error reading book"));
    }
    try{
      System.out.println("Sending back open book operation information now");

      // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
      ObjectOutputStream  outputToClient = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToClient.toString());
      System.out.println("Sending back information");
      outputToClient.writeObject(objs);
      System.out.println("After write object");
      outputToClient.flush();
      System.out.println("After flush object");
      outputToClient.reset();
      System.out.println("After reset object");
      outputToClient.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param userInfo
   * @param uplVersion
   *
   * @return
   *
   * @see
   */
  protected String[] getBookListForUnlockingByUser(Vector userInfo, String dbClsId){
    String  userName = (String) userInfo.elementAt(0);
    String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
    password = convertPassword(userName, password);
    String db = FixedInfo.getDb(dbClsId);
    String clsId = FixedInfo.getCls(dbClsId);

    return DataServerManager.getDataServer(db).getBookListForUnLocking(userName, password, clsId);

    // This code to be replaced by code that connects to database
    // user information is valid, get the book list
    // com.sri.panther.jdk1_1_util.ReadResources treeProperties =
    // (com.sri.panther.jdk1_1_util.ReadResources) getServletContext().getAttribute("treeProperties");
    //
    // // have not created treeProperties object
    // if (null == treeProperties) {
    // initAppObjects();
    // treeProperties = (com.sri.panther.jdk1_1_util.ReadResources) getServletContext().getAttribute("treeProperties");
    // }
    // EntryType entryType = null;
    // LibrarySettings libSettings = null;
    // try {
    // entryType = new EntryType();
    // entryType.setEntryType(ConfigFile.getProperty("java_entryType"));
    // System.out.println("Got entry type");
    // libSettings = new LibrarySettings("", entryType, ConfigFile.getProperty("java_lib"), ConfigFile.getProperty("java_lib_root"), false);
    // return FileNameGenerator.getBooks(libSettings);
    // }
    // catch (Exception e) {
    // System.out.println("Exception while retrieving book list for unlocking " + e.getMessage());
    // e.printStackTrace();
    // return null;
    // }
  }

  /**
   * Method declaration
   *
   *
   * @param version
   *
   * @return
   *
   * @see
   */
  protected String[] getBookList(String dbClsId){
    String db = FixedInfo.getDb(dbClsId);
    String clsId = FixedInfo.getCls(dbClsId);
    return DataServerManager.getDataServer(db).getBookList(clsId);

    // This code to be replaced by code that connects to database
    // user information is valid, get the book list
    // com.sri.panther.jdk1_1_util.ReadResources treeProperties =
    // (com.sri.panther.jdk1_1_util.ReadResources) getServletContext().getAttribute("treeProperties");
    // // have not created treeProperties object
    // if (null == treeProperties) {
    // initAppObjects();
    // treeProperties = (com.sri.panther.jdk1_1_util.ReadResources) getServletContext().getAttribute("treeProperties");
    // }
    // EntryType entryType = null;
    // LibrarySettings libSettings = null;
    // try {
    // entryType = new EntryType();
    // entryType.setEntryType(ConfigFile.getProperty("java_entryType"));
    // System.out.println("Got entry type");
    // libSettings = new LibrarySettings("", entryType, ConfigFile.getProperty("java_lib"), ConfigFile.getProperty("java_lib_root"), false);
    // return FileNameGenerator.getBooks(libSettings);
    // }
    // catch (Exception e) {
    // System.out.println("Exception while retrieving book list " + e.getMessage());
    // e.printStackTrace();
    // return null;
    // }
  }

  /**
   * Method declaration
   *
   *
   * @param userInfo
   * @param uplVersion
   * @param book
   *
   * @return
   *
   * @see
   */
  protected String lockBook(Vector userInfo, String dbClsId, String book){
    String  userName = (String) userInfo.elementAt(0);
    String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
    password = convertPassword(userName, password);
    String db = FixedInfo.getDb(dbClsId);
    String clsId = FixedInfo.getCls(dbClsId);

    return DataServerManager.getDataServer(db).lockBook(userName, password, clsId, book);
  }

  /**
   * Method declaration
   *
   *
   * @param userInfo
   * @param uplVersion
   * @param book
   *
   * @return
   *
   * @see
   */
  protected String unLockBook(Vector userInfo, String dbClsId, String book){
    String  userName = (String) userInfo.elementAt(0);
    String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
    password = convertPassword(userName, password);
    String db = FixedInfo.getDb(dbClsId);
    String clsId = FixedInfo.getCls(dbClsId);
    return DataServerManager.getDataServer(db).unlockBook(userName, password, clsId, book);
  }

  /**
   * Method declaration
   *
   *
   * @param userInfo
   * @param uplVersion
   * @param book
   * @param rcc
   * @param saveStatus
   *
   * @return
   *
   * @see
   */
//  protected String save(Vector userInfo, String dbUplVersion, String book, RawComponentContainer rcc, int saveStatus){
//    String    userName = (String) userInfo.elementAt(0);
//    String    password = String.copyValueOf((char[]) userInfo.elementAt(1));
//    password = convertPassword(userName, password);
//    Vector    treeInfo = (Vector) rcc.getTree();
//    String[]  attrTable = rcc.getAttributeTable();
//
//    String db = FixedInfo.getDb(dbUplVersion);
//    DataServer ds = DataServerManager.getDataServer(db);
//    String uplVersion = FixedInfo.getCls(dbUplVersion);
//
//    return ds.saveBookForPANTHER(userName, password, uplVersion, book, (String[])treeInfo.get(rcc.INDEX_SF_AN), attrTable, rcc.getName(), saveStatus);
//
//    // if (false == isBookLockedByUser(userInfo, book)) {
//    // return "User " + (String)userInfo.elementAt(0) + " does not have book " + book + " locked.";
//    // }
//    // com.sri.panther.jdk1_1_util.ReadResources treeProperties =
//    // (com.sri.panther.jdk1_1_util.ReadResources) getServletContext().getAttribute("treeProperties");
//    // // have not created treeProperties object
//    // if (null == treeProperties) {
//    // initAppObjects();
//    // }
//    // EntryType entryType = null;
//    // LibrarySettings libSettings = null;
//    //
//    // try {
//    // entryType = new EntryType();
//    // entryType.setEntryType(ConfigFile.getProperty("java_entryType"));
//    // System.out.println("Got entry type");
//    // libSettings = new LibrarySettings(book, entryType, ConfigFile.getProperty("java_lib"), ConfigFile.getProperty("java_lib_root"), false);
//    // BETEDataTransfer.saveOutputToFile(libSettings, rcc);
//    // return "";
//    // }
//    // catch(Exception e) {
//    // e.printStackTrace();
//    // return "Error while saving book, please contact the system administrator";
//    // }
//  }

  // GET METHODS

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void fixedInfo(HttpServletRequest request, HttpServletResponse response){
    com.sri.panther.paintCommon.FixedInfo  fixedInfo =
      (com.sri.panther.paintCommon.FixedInfo) getServletContext().getAttribute(APP_SCOPE_PAINT_FIXED_RESOURCE);

    if (null == fixedInfo){
      initAppObjects();
    }
    fixedInfo =
      (com.sri.panther.paintCommon.FixedInfo) getServletContext().getAttribute(APP_SCOPE_PAINT_FIXED_RESOURCE);
    Vector  outputObj = new Vector();

    outputObj.addElement(new TransferInfo(STR_EMPTY));     // Indicate success
    outputObj.addElement(fixedInfo);
    try{
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending fixed information");
      outputToApplet.writeObject(outputObj);
      System.out.println("After write object");
      outputToApplet.flush();
      System.out.println("After flush object");
      outputToApplet.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
  }
  
  
    public void getPantherClsData(HttpServletRequest request, HttpServletResponse response){
    
        System.out.println("get cls info");
        response.setContentType("java/object");
        ObjectInputStream in = null;
        Vector            applnRequest = null;

        try{
          System.out.println("Before opening stream");
          in = new ObjectInputStream(request.getInputStream());
          applnRequest = (Vector) in.readObject();
          in.close();
          System.out.println("Finished with stream and closed");
        }
        catch (ClassNotFoundException cnfe){
          cnfe.printStackTrace();
          return;
        }
        catch (IOException ie){
          ie.printStackTrace();
          return;
        }
        
        Vector  outObjs = new Vector();
        String dbClsId = (String)applnRequest.get(0);
        String db = FixedInfo.getDb(dbClsId);
        String clsId = FixedInfo.getCls(dbClsId);
        if (null == db || null == clsId) {
          outObjs.addElement(new TransferInfo("Database and upl information not specified in correct format"));
        }
        else {
          System.out.println("Requesting information from database " + db + " upl version " + clsId);
          Hashtable clsTable = (Hashtable)getServletContext().getAttribute(APP_SCOPE_CLS_TABLE);
          if (null == clsTable) {
              initAppObjects();
              clsTable = (Hashtable)getServletContext().getAttribute(APP_SCOPE_CLS_TABLE);
          }
          Hashtable dbToUplVer = (Hashtable)clsTable.get(db);
          if (null == dbToUplVer){
            outObjs.addElement(new TransferInfo("Invalid database specified"));
          }
          else{
            Object clsInfo = dbToUplVer.get(clsId);

            if (null == clsInfo) {
              outObjs.addElement(new TransferInfo("Invalid UPL version specified or no cls data for upl"));
            }
            else {
              outObjs.addElement(new TransferInfo(STR_EMPTY));
              outObjs.addElement(clsInfo);
            }
          }
        }


        try{
          System.out.println("Sending back cls operation information now");

          // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
          ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

          System.out.println("Output stream is " + outputToApplet.toString());
          System.out.println("Sending back information");
          outputToApplet.writeObject(outObjs);
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
        System.out.println("Exitting servlet now");

    }


  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
//  public void getGoClsInfo(HttpServletRequest request, HttpServletResponse response){
//  
//      System.out.println("get go cls info");
//      response.setContentType("java/object");
//      ObjectInputStream in = null;
//      Vector            applnRequest = null;
//
//      try{
//        System.out.println("Before opening stream");
//        in = new ObjectInputStream(request.getInputStream());
//        applnRequest = (Vector) in.readObject();
//        in.close();
//        System.out.println("Finished with stream and closed");
//      }
//      catch (ClassNotFoundException cnfe){
//        cnfe.printStackTrace();
//        return;
//      }
//      catch (IOException ie){
//        ie.printStackTrace();
//        return;
//      }
//      
//      Vector  outObjs = new Vector();
//      String dbClsId = (String)applnRequest.get(0);
//      String db = FixedInfo.getDb(dbClsId);
//      String clsId = FixedInfo.getCls(dbClsId);
//      if (null == db || null == clsId) {
//        outObjs.addElement(new TransferInfo("Database and upl information not specified in correct format"));
//      }
//      else {
//        System.out.println("Requesting information from database " + db + " upl version " + clsId);
//        Hashtable clsTable = (Hashtable)getServletContext().getAttribute(APP_SCOPE_PAINT_PANTHER_GO_SLIM);
//        if (null == clsTable) {
//            initAppObjects();
//            clsTable = (Hashtable)getServletContext().getAttribute(APP_SCOPE_PAINT_PANTHER_GO_SLIM);
//        }
//        Hashtable dbToUplVer = (Hashtable)clsTable.get(db);
//        if (null == dbToUplVer){
//          outObjs.addElement(new TransferInfo("Invalid database specified"));
//        }
//        else{
//          Object clsInfo = dbToUplVer.get(clsId);
//          if (null == clsInfo) {
//            outObjs.addElement(new TransferInfo("Invalid UPL version specified"));
//          }
//          else {
//            outObjs.addElement(new TransferInfo(STR_EMPTY));
//            outObjs.addElement(clsInfo);
//          }
//        }
//      }
//
//
//      try{
//        System.out.println("Sending back open book operation information now");
//
//        // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
//        ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));
//
//        System.out.println("Output stream is " + outputToApplet.toString());
//        System.out.println("Sending back information");
//        outputToApplet.writeObject(outObjs);
//        System.out.println("After write object");
//        outputToApplet.flush();
//        System.out.println("After flush object");
//        outputToApplet.reset();
//        System.out.println("After reset object");
//        outputToApplet.close();
//        System.out.println("Data transmission complete.");
//      }
//      catch (IOException ioex){
//        ioex.printStackTrace();
//      }
//      catch (Exception ex){
//        ex.printStackTrace();
//      }
//      System.out.println("Exitting servlet now");
//
//  }
//  
//  public void getConfCodeInfo(HttpServletRequest request, HttpServletResponse response){
//    System.out.println("Going to get confidence code information");
//    Vector  outObjs = new Vector();    
//    Hashtable confTable = (Hashtable) getServletContext().getAttribute("confCodeTbl");
//
//    if (null == confTable){
//      initAppObjects();
//      confTable = (Hashtable) getServletContext().getAttribute("confCodeTbl");
//    }
//    if (null == confTable) {
//      outObjs.addElement(new TransferInfo("Unable to retrieve confidence code information"));
//    }
//    else {    
//      String  db = request.getParameter("var");
//      Object confTableItem = confTable.get(db);
//      if (null == confTableItem) {    
//        outObjs.addElement(new TransferInfo("Database information not specified in correct format"));
//      }
//      else {
//        outObjs.addElement(new TransferInfo(""));
//        outObjs.addElement(confTableItem);
//      }
//    }
//    try{
//      ObjectOutputStream  outputToClient = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));
//
//      System.out.println("Output stream is " + outputToClient.toString());
//      System.out.println("Sending confidence information to client");
//      outputToClient.writeObject(outObjs);
//      System.out.println("After write object");
//      outputToClient.flush();
//      System.out.println("After flush object");
//      outputToClient.close();
//      System.out.println("Data transmission complete.");
//    }
//    catch (IOException ioex){
//      ioex.printStackTrace();
//    }
//    catch (Exception ex){
//      ex.printStackTrace();
//    }    
//  }  

  // POST METHODS

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void verifyUser(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to verify user information");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    Vector  outputInfo = new Vector();

    outputInfo.addElement(new TransferInfo(verifyUserInfo(applnRequest)));
    try{
      System.out.println("Going to send back status of user verification now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void getUserInfo(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to get user information");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

    Vector  userInfo = (Vector) applnRequest.elementAt(0);
    String db = (String)applnRequest.elementAt(1);
    Vector  objs = new Vector();
    try{
      String  userName = (String) userInfo.elementAt(0);
      String  password = String.copyValueOf((char[]) userInfo.elementAt(1));
      password = convertPassword(userName, password);
      DataServer ds = DataServerManager.getDataServer(db);
      if (null == ds) {
          objs.addElement(new TransferInfo(db + MSG_ERR_INVALID_DB));
      }
      User user = ds.getUser(userName, password);

      if (null == user){
        objs.addElement(new TransferInfo("Unable to get user id for user"));
      }
      else{
        objs.addElement(new TransferInfo(""));
        objs.addElement(user);
        System.out.println("Added objects for transferring");
      }
    }
    catch (Exception e){
      System.out.println("Exception while getting user info " + e.getMessage() + " has been returned.");
      e.printStackTrace();
      objs.addElement(new TransferInfo("Error getting user info"));
    }
    try{
      System.out.println("Sending back get user info now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(objs);
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
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void bookListForLocking(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to get list of books user can lock");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

    // Verify user information first
    // String userInfoValid = verifyUserInfo((Vector)applnRequest.elementAt(0));
    Vector  outputInfo = new Vector();

    // User information is valid
    // if (0 == userInfoValid.length()) {
    // get books and put into a vector
    String  books[] = getBookListForLockingByUser((Vector) applnRequest.elementAt(0),
            (String) applnRequest.elementAt(1));

    if (null == books){
      outputInfo.addElement(new TransferInfo("Server cannot get list of books available for user to lock"));
    }
    else{
      outputInfo.addElement(new TransferInfo(""));    // Indicate success
      Vector  bookList = new Vector();

      for (int i = 0; i < books.length; i++){
        bookList.addElement(books[i]);
      }
      outputInfo.addElement(bookList);
    }

    // }
    // else {
    // // user information is not valid, indicate error
    // outputInfo.addElement(new TransferInfo(userInfoValid));
    // }
    try{
      System.out.println("Sending back lock book operation information now");

      // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
  }

  // Get list of all books

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void bookList(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to get list of all books");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

    // Verify user information first
//    String dbUplInfo = (String)applnRequest.elementAt(1);
    String  userInfoValid = verifyUserInfo(applnRequest);
    Vector  outputInfo = new Vector();

    // User information is valid
    if (0 == userInfoValid.length()){

      // get books and put into a vector
      String  books[] = getBookList((String) applnRequest.elementAt(1));

      if (null == books){
        outputInfo.addElement(new TransferInfo("Server cannot get list of books"));
      }
      else{
        outputInfo.addElement(new TransferInfo(""));    // Indicate success
        Vector  bookList = new Vector();

        for (int i = 0; i < books.length; i++){
          bookList.addElement(books[i]);
        }
        outputInfo.addElement(bookList);
      }
    }
    else{

      // user information is not valid, indicate error
      outputInfo.addElement(new TransferInfo(userInfoValid));
    }
    try{
      System.out.println("Sending back books operation information now");

      // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
  }

  // Write MSA information to server

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  protected void writeMSA(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to write MSA info to server application scope object");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

    // Verify user information first
    String    book = (String) applnRequest.elementAt(0);
    String    clsVersion = (String) applnRequest.elementAt(1);
    Object    MSAInfo = (Object) applnRequest.elementAt(2);

    // Check if the application scope object for storing this information has
    // already been created, if no create one
    Hashtable MSATbl = (Hashtable) getServletContext().getAttribute("MSATbl");

    if (null == MSATbl){
      MSATbl = new Hashtable();
    }
    Hashtable bookTbl = (Hashtable) MSATbl.get(clsVersion);

    if (null == bookTbl){
      bookTbl = new Hashtable();
      MSATbl.put(clsVersion, bookTbl);
    }
    bookTbl.put(book, MSAInfo);
    Vector  outputInfo = new Vector();

    outputInfo.addElement(new TransferInfo(""));
    try{
      System.out.println("Sending back MSA operation success information now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
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

  /**
   * Method declaration
   *
   *
   * @param ls
   *
   * @return
   *
   * @see
   */
  public static Object getMSA(LibrarySettings ls){
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
      msaURL = FileNameGenerator.getMSAWts(ls);
      String[] msaWts = FileUtils.readFileFromURL(new URL(msaURL));
      if (null != msaWts) {
        v.addElement(msaWts);
      }
      else {
        System.out.println("Cannot read msa wts file" + msaURL);
      }
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
      String dbUplVersion = (String)applnRequest.elementAt(1);


      String db = FixedInfo.getDb(dbUplVersion);
      DataServer ds = DataServerManager.getDataServer(db);
      String uplVersion = FixedInfo.getCls(dbUplVersion);

      Vector outputInfo = new Vector();      
      Vector books = ds.getMyBooks(userName, password, uplVersion);

      if (null == books) {
          outputInfo.addElement(new TransferInfo(MSG_UNABLE_TO_RETRIEVE_LOCKED_BOOKS));
      }
      else {
          outputInfo.addElement(new TransferInfo(STRING_EMPTY));
          outputInfo.addElement(books);         
      }




      try {
          System.out.println("Going to send back locked books");

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

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void bookListForUnlocking(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to get list of books user can unlock");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

    // Verify user information first
    String  userInfoValid = verifyUserInfo(applnRequest);
    Vector  outputInfo = new Vector();

    // User information is valid
    if (0 == userInfoValid.length()){

      // get books and put into a vector
      String  books[] = getBookListForUnlockingByUser((Vector) applnRequest.elementAt(0),
              (String) applnRequest.elementAt(1));

      if (null == books){
        outputInfo.addElement(new TransferInfo("Server cannot get list of books available for user to unlock"));
      }
      else{
        outputInfo.addElement(new TransferInfo(""));    // Indicate success
        Vector  bookList = new Vector();

        for (int i = 0; i < books.length; i++){
          bookList.addElement(books[i]);
        }
        outputInfo.addElement(bookList);
      }
    }
    else{

      // user information is not valid, indicate error
      outputInfo.addElement(new TransferInfo(userInfoValid));
    }
    try{
      System.out.println("Sending back unlock book list operation information now");

      // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void unLockBook(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to unlock book for user");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }

    // Verify user information first
    Vector  userInfo = (Vector) applnRequest.elementAt(0);
    String  dbClsId = (String) applnRequest.elementAt(1);
    Vector v = new Vector();
    v.addElement(userInfo);
    v.addElement(dbClsId);
    String  userInfoValid = verifyUserInfo(v);
    String  book = (String) applnRequest.elementAt(2);    // Book user requesting to unlock
    Vector  outputInfo = new Vector();

    // User information is valid
    if (0 == userInfoValid.length()){

      // Unlock book for user
      String  unLockMsg = unLockBook(userInfo, dbClsId, book);

      if (0 == unLockMsg.length()){
        outputInfo.addElement(new TransferInfo(""));    // Indicate success
      }
      else{
        outputInfo.addElement(new TransferInfo(unLockMsg));
      }
    }
    else{

      // user information is not valid, indicate error
      outputInfo.addElement(new TransferInfo(userInfoValid));
    }
    try{
      System.out.println("Sending back unlock book operation information now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void saveBook(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to save book and unlock/mark curated, etc");
    response.setContentType("java/object");
    ObjectInputStream in = null;
    Vector            applnRequest = null;

    try{
      System.out.println("Before opening stream");

      // in = new ObjectInputStream(request.getInputStream());
      in = new ObjectInputStream(new GZIPInputStream(request.getInputStream()));
      applnRequest = (Vector) in.readObject();
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    Vector                userInfo = (Vector) applnRequest.elementAt(0);
    String                uplVersion = (String) applnRequest.elementAt(1);
    String                book = (String) applnRequest.elementAt(2);
    RawComponentContainer rcc = (RawComponentContainer) applnRequest.elementAt(3);
    Integer               saveStatus = (Integer) applnRequest.elementAt(4);
    String                operationInfo = "Save operation not supported";//save(userInfo, uplVersion, book, rcc, saveStatus.intValue());
    Vector                outputInfo = new Vector();

    outputInfo.addElement(new TransferInfo(operationInfo));
    try{
      System.out.println("Going to send back info about the save and unlock operation");

      // ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(new GZIPOutputStream(response.getOutputStream()));

      System.out.println("Output stream is " + outputToApplet.toString());
      System.out.println("Sending back information");
      outputToApplet.writeObject(outputInfo);
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
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void getSubFamilyEvidences(HttpServletRequest request, HttpServletResponse response){
    Vector  outputInfo = new Vector();

    response.setContentType("java/object");
    ObjectInputStream in = null;
    String            subfamilyAcc = null;
    String            userName = null;
    String            password = null;
    String            dbUplVersion = null;

    try{
      System.out.println("Before opening stream in getSubFamilyEvidences");
      in = new ObjectInputStream(request.getInputStream());
      Vector  temp = (Vector) in.readObject();

      userName = (String) temp.elementAt(0);
      password = String.copyValueOf((char[]) temp.elementAt(1));
      password = convertPassword(userName, password);
      dbUplVersion = (String) temp.elementAt(2);
      subfamilyAcc = (String) temp.elementAt(3);
      in.close();

      System.out.println("User:" + userName + " uplVersion:" + dbUplVersion + " SubfamilyAcc"
                         + subfamilyAcc);
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    String db = FixedInfo.getDb(dbUplVersion);
    DataServer ds = DataServerManager.getDataServer(db);
    String uplVersion = FixedInfo.getCls(dbUplVersion);

    Vector  evidenceInfo = ds.lockSubfamilyAndGetEvidences(userName, password, uplVersion, subfamilyAcc);

    outputInfo.addElement(new TransferInfo((String) evidenceInfo.elementAt(0)));
    if (evidenceInfo.size() > 1){
      outputInfo.addElement((Vector) evidenceInfo.elementAt(1));
    }
    try{
      System.out.println("Sending back subfamily evidences now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      outputToApplet.writeObject(outputInfo);
      outputToApplet.flush();
      outputToApplet.reset();
      outputToApplet.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void getSequenceEvidences(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to getSubfamilyEvidence");
    Vector  outputInfo = new Vector();

    response.setContentType("java/object");
    ObjectInputStream in = null;
    String            seqAcc = null;
    String            userName = null;
    String            password = null;
    String            dbUplVersion = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      Vector  temp = (Vector) in.readObject();

      userName = (String) temp.elementAt(0);
      password = String.copyValueOf((char[]) temp.elementAt(1));
      password = convertPassword(userName, password);
      dbUplVersion = (String) temp.elementAt(2);
      seqAcc = (String) temp.elementAt(3);
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    String db = FixedInfo.getDb(dbUplVersion);
    DataServer ds = DataServerManager.getDataServer(db);
    String uplVersion = FixedInfo.getCls(dbUplVersion);

    Vector  evidenceInfo = DataServerManager.getDataServer(db).lockSequenceAndGetEvidences(userName, password, uplVersion, seqAcc);

    outputInfo.addElement(new TransferInfo((String) evidenceInfo.elementAt(0)));
    if (evidenceInfo.size() > 1){
      outputInfo.addElement((Vector) evidenceInfo.elementAt(1));
    }
    try{
      System.out.println("Sending back subfamily evidences now");
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      outputToApplet.writeObject(outputInfo);
      outputToApplet.flush();
      outputToApplet.reset();
      outputToApplet.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void saveSequenceEvidences(HttpServletRequest request, HttpServletResponse response){
    Vector  outputInfo = new Vector();

    response.setContentType("java/object");
    ObjectInputStream in = null;
    String            seqAcc = null;
    String            familyAcc = null;
    String            userName = null;
    String            password = null;
    String            dbUplVersion = null;
    Vector            evidences = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      Vector  temp = (Vector) in.readObject();

      userName = (String) temp.elementAt(0);
      password = String.copyValueOf((char[]) temp.elementAt(1));
      password = convertPassword(userName, password);
      dbUplVersion = (String) temp.elementAt(2);
      seqAcc = (String) temp.elementAt(3);
      familyAcc = (String) temp.elementAt(4);
      evidences = (Vector) temp.elementAt(5);
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    String db = FixedInfo.getDb(dbUplVersion);
    DataServer ds = DataServerManager.getDataServer(db);
    String uplVersion = FixedInfo.getCls(dbUplVersion);
    String  outputMsg = DataServerManager.getDataServer(db).saveEvidenceAndUnlockSequence(userName, password, uplVersion, seqAcc, familyAcc,
            evidences);

    outputInfo.addElement(new TransferInfo(outputMsg));
    try{
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      outputToApplet.writeObject(outputInfo);
      outputToApplet.flush();
      outputToApplet.reset();
      outputToApplet.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void saveSubfamilyEvidences(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to saveSubfamilyEvidence");
    Vector  outputInfo = new Vector();

    response.setContentType("java/object");
    ObjectInputStream in = null;
    String            subfamilyAcc = null;
    String            familyAcc = null;
    String            userName = null;
    String            password = null;
    String            dbUplVersion = null;
    Vector            evidences = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      Vector  temp = (Vector) in.readObject();

      userName = (String) temp.elementAt(0);
      password = String.copyValueOf((char[]) temp.elementAt(1));
      password = convertPassword(userName, password);
      dbUplVersion = (String) temp.elementAt(2);
      subfamilyAcc = (String) temp.elementAt(3);
      familyAcc = (String) temp.elementAt(4);
      evidences = (Vector) temp.elementAt(5);
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    System.out.println("in client2Servlt saveSubfamilyEvidences");
    System.out.println("username is " + userName);
    System.out.println("uplVersion is " + dbUplVersion);
    System.out.println("subfamily acc is " + subfamilyAcc);
    System.out.println("family acc is " + familyAcc);
    System.out.println("evidences are " + evidences.size());
    String db = FixedInfo.getDb(dbUplVersion);
    DataServer ds = DataServerManager.getDataServer(db);
    String uplVersion = FixedInfo.getCls(dbUplVersion);
    String  outputMsg = DataServerManager.getDataServer(db).saveEvidenceAndUnlockSubfamily(userName, password, uplVersion, subfamilyAcc,
            familyAcc, evidences);

    outputInfo.addElement(new TransferInfo(outputMsg));
    try{
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      outputToApplet.writeObject(outputInfo);
      outputToApplet.flush();
      outputToApplet.reset();
      outputToApplet.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void unlockSequence(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to unlockSequence");
    Vector  outputInfo = new Vector();

    response.setContentType("java/object");
    ObjectInputStream in = null;
    String            seqAcc = null;
    String            userName = null;
    String            password = null;
    String            dbUplVersion = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      Vector  temp = (Vector) in.readObject();

      userName = (String) temp.elementAt(0);
      password = String.copyValueOf((char[]) temp.elementAt(1));
      password = convertPassword(userName, password);
      dbUplVersion = (String) temp.elementAt(2);
      seqAcc = (String) temp.elementAt(3);
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    System.out.println("in client2Servlt unlockSequence");
    System.out.println("username is " + userName);
    System.out.println("uplVersion is " + dbUplVersion);
    System.out.println("sequence acc is " + seqAcc);
    String db = FixedInfo.getDb(dbUplVersion);
    DataServer ds = DataServerManager.getDataServer(db);
    String uplVersion = FixedInfo.getCls(dbUplVersion);
    String  outputMsg = DataServerManager.getDataServer(db).unlockSequence(userName, password, uplVersion, seqAcc);

    outputInfo.addElement(new TransferInfo(outputMsg));
    try{
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      outputToApplet.writeObject(outputInfo);
      outputToApplet.flush();
      outputToApplet.reset();
      outputToApplet.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
    System.out.println("Exitting servlet now");
  }

  /**
   * Method declaration
   *
   *
   * @param request
   * @param response
   *
   * @see
   */
  public void unlockSubfamily(HttpServletRequest request, HttpServletResponse response){
    System.out.println("Going to attempt to unlockSubfamily");
    Vector  outputInfo = new Vector();

    response.setContentType("java/object");
    ObjectInputStream in = null;
    String            subfamilyAcc = null;
    String            userName = null;
    String            password = null;
    String            dbUplVersion = null;

    try{
      System.out.println("Before opening stream");
      in = new ObjectInputStream(request.getInputStream());
      Vector  temp = (Vector) in.readObject();

      userName = (String) temp.elementAt(0);
      password = String.copyValueOf((char[]) temp.elementAt(1));
      password = convertPassword(userName, password);
      dbUplVersion = (String) temp.elementAt(2);
      subfamilyAcc = (String) temp.elementAt(3);
      in.close();
      System.out.println("Finished with stream and closed");
    }
    catch (ClassNotFoundException cnfe){
      cnfe.printStackTrace();
      return;
    }
    catch (IOException ie){
      ie.printStackTrace();
      return;
    }
    System.out.println("in client2Servlt unlockSubfamily");
    System.out.println("username is " + userName);
    System.out.println("uplVersion is " + dbUplVersion);
    System.out.println("subfamily acc is " + subfamilyAcc);
    String db = FixedInfo.getDb(dbUplVersion);
    DataServer ds = DataServerManager.getDataServer(db);
    String uplVersion = FixedInfo.getCls(dbUplVersion);

    String  outputMsg = DataServerManager.getDataServer(db).unlockClassification(userName, password, uplVersion, subfamilyAcc);

    outputInfo.addElement(new TransferInfo(outputMsg));
    try{
      ObjectOutputStream  outputToApplet = new ObjectOutputStream(response.getOutputStream());

      outputToApplet.writeObject(outputInfo);
      outputToApplet.flush();
      outputToApplet.reset();
      outputToApplet.close();
      System.out.println("Data transmission complete.");
    }
    catch (IOException ioex){
      ioex.printStackTrace();
    }
    catch (Exception ex){
      ex.printStackTrace();
    }
    System.out.println("Exitting servlet now");
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

  protected void setFixedInfo(HttpServletRequest request, HttpServletResponse response) {
    com.sri.panther.paintCommon.FixedInfo  fixedInfo =
      (com.sri.panther.paintCommon.FixedInfo) getServletContext().getAttribute(APP_SCOPE_PAINT_FIXED_RESOURCE);

    if (null == fixedInfo){
      initAppObjects();
    }
  }


    protected static Object sendAndReceiveZip(String servletURL,
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
    
    


}
