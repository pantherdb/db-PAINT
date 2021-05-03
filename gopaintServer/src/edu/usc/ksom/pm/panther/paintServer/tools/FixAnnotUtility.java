/**
 *  Copyright 2021 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.usc.ksom.pm.panther.paintServer.tools;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.logic.DataValidationManager;
import com.sri.panther.paintServer.util.ConfigFile;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class FixAnnotUtility {
    
    public static final String ENCODING = "UTF-8";
    private static final String SERVLET_CONNECTION_CONTENT_TYPE = "Content-Type";
    private static final String SERVLET_CONNECTION_OBJECT_TYPE_JAVA = "java/object";
    private static final String SERVLET_REQUEST_PROPERTY_COOKIE = "Cookie";    
    private static final String SERVLET_PATH = "/servlet/edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet?action=";
    public static final String URL_GET_LEAF_IBA_INFO = "/webservices/family.jsp?searchValue=%REPLACE_STR%&searchType=SEARCH_TYPE_FAMILY_LEAF_IBA_ANNOTATION_INFO_DETAILS";    
    public static final String REPLACE_STR = "%REPLACE_STR%";    
    
    public static final String REQUEST_LOCK_UNLOCK_BOOKS = "LockUnLockBooks";
    public static final String ACTION_SAVE_BOOK = "saveBook";    
    
    public static final String MSG_COMMENT = " Annotation Validity Update.\n";
    private static final java.text.SimpleDateFormat DATE_FORMATTER = new java.text.SimpleDateFormat("yyyy-MM-dd");
    
    public static final int SAVE_OPTION_SAVE_UNLOCK = 2;
        
    private HashMap<String, HashSet<String>> bookToOrgLookup = null;
    
    public static final String DIR_INFO = "/info";
    public static final String DIR_PRE_UPDATE_INFO = "/preUpdate";
    public static final String DIR_LEAF_IBA_ANNOT = "/leafIBAInfo";    
    public static final String FILE_STATUS = "status.txt"; 
    
    public static final String FILE_SEPARATOR = "/";



    private static final String ELEMENT_SEARCH = "search";
    private static final String ELEMENT_FAMILY_ID = "family_id";
    private static final String ELEMENT_FAMILY_ANNOTATION_INFO_OTHER = "family_annot_info_other";
    private static final String ELEMENT_FAMILY_ANNOTATION_INFO_PAINT = "family_annot_info_paint";
    private static final String ELEMENT_MESSAGE = "message";    
    
    private String serverUrl;
    private String bookListFile;
    private User user = null;
    private String userName;
    private String password;
    private List<String> books;
    private String saveDirPath;
    
    public FixAnnotUtility() {
        
    }
    
    public void fixBooksInFile(String serverUrl, String userName, String password, String saveDirPath, String bookListFile) {
        this.serverUrl = serverUrl;
        this.bookListFile = bookListFile;
        this.userName = userName;
        this.password = password;
        this.saveDirPath = saveDirPath;
        
        // Get the user information
        Vector userInfo = new Vector();
        userInfo.add(userName);
        char[] passwordArray = null;
        if (null != password) {
            password.trim();
            passwordArray = password.toCharArray();
        }
        userInfo.add(passwordArray);
        Vector userInfoList = (Vector) sendAndReceiveZip(serverUrl, "userInfo", userInfo, null, null);
        if (null == userInfoList || 0 == userInfoList.size()) {
            System.out.println("Unable to get user information");
            return;
        }
        user = (User) userInfoList.get(0);
        System.out.println("User id is " + user.getUserId());
        books = getBooks();
        processBooks();
        
    }
    
    public void fixBooksInSystem(String serverUrl, String userName, String password, String saveDirPath) {
        this.serverUrl = serverUrl;
        this.userName = userName;
        this.password = password;
        this.saveDirPath = saveDirPath;        
        
        // Get the user information
        Vector userInfo = new Vector();
        userInfo.add(userName);
        char[] passwordArray = null;
        if (null != password) {
            password.trim();
            passwordArray = password.toCharArray();
        }
        userInfo.add(passwordArray);
        Vector userInfoList = (Vector) sendAndReceiveZip(serverUrl, "userInfo", userInfo, null, null);
        if (null == userInfoList || 0 == userInfoList.size()) {
            System.out.println("Unable to get user information");
            return;
        }
        user = (User) userInfoList.get(0);
        System.out.println("User id is " + user.getUserId());        
        books = getBooksWithPaintAnnotations();
        processBooks();
    }
    
    public List<String> getBooks() {
        try {
            Path filePath = Paths.get(bookListFile);
            List<String> books = Files.readAllLines(filePath, Charset.forName(ENCODING));
            if (null == books) {
                return null;
            }
            for (int i = 0; i < books.size(); i++) {
                String temp = books.get(i);
                String mod = temp.trim();
                if (true == mod.isEmpty()) {
                    books.remove(i);
                    i--;
                    continue;
                }
                books.set(i, mod);
            }
            Collections.sort(books);
            return books;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }    
    
    public List<String> getBooksWithPaintAnnotations() {
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        List<Book> books = dataIO.getBooksWithPAINTAnnotations(DataIO.CUR_CLASSIFICATION_VERSION_SID);

        ArrayList<String> famList = new ArrayList<String>();
        for (Book b: books) {
            famList.add(b.getId());
        }
        Collections.sort(famList);
        return famList;
    }    
    
    public void processBooks() {
        if (null == books) {
            System.out.println("Nothing to process");
            return;
        }
        DataValidationManager dvm = DataValidationManager.getInstance();
        bookToOrgLookup = dvm.getBooksWithIncompleteTaxonInfo();
        if (null == bookToOrgLookup) {
            System.out.println("Unable to retrieve organism information to validate taxonomy constraints");
            return;
        }
        
        // Process locked books first.  If a book gets locked after process starts, then it will not be validated.
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        ArrayList<Book> allBooks = dataIO.getAllBooks(DataIO.CUR_CLASSIFICATION_VERSION_SID); 
        ArrayList<String> lockedBooks = new ArrayList<String>();
        ArrayList<String> unLockedBooks = new ArrayList<String>();        
        for (Book b : allBooks) {
            String id = b.getId();
            int index = Utils.getIndex(books, id);
            if (index < 0) {
                continue;
            }            
            if (null == b.getLockedBy()) {
                unLockedBooks.add(id);
            }
            else {
                lockedBooks.add(id);
            }            
        }
        try {
            String filePrefix = saveDirPath + DIR_INFO ;
            Path tmpFileStatusPath = Paths.get(filePrefix +  FILE_SEPARATOR + FILE_STATUS);
            Path statusFilePath = tmpFileStatusPath;
            if (false == Files.exists(tmpFileStatusPath)) {
                if (false == Files.exists(tmpFileStatusPath.getParent())) {
                    Files.createDirectories(tmpFileStatusPath.getParent());
                }
                statusFilePath = Files.createFile(tmpFileStatusPath);
            }
            else {
                ArrayList<String> fileInfo = new ArrayList<String>();
                Files.write(statusFilePath, fileInfo, StandardCharsets.UTF_8);
            }
            updateBooks(dataIO, statusFilePath, lockedBooks, true);
            updateBooks(dataIO, statusFilePath, unLockedBooks, false);            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }             
        
       
    }
    
    protected void updateBooks(DataIO dataIO, Path statusFilePath, List<String> updateList, boolean unLockIfNecessary) {
        String extraInfo = "no unlock ";
        if (true == unLockIfNecessary) {
            extraInfo = "unlock if necessary ";
        }
        try {
            int size = updateList.size();
            for (int i = 0; i < size; i++) {
                String msg = "Processing " + extraInfo + (i + 1) + " of " + size + " book " + updateList.get(i) + "\n";
                Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                
                HashSet<String> unhandledOrgSet = bookToOrgLookup.get(updateList.get(i));
                if (null != unhandledOrgSet && false == unhandledOrgSet.isEmpty()) {
                    msg = "Warning book " + updateList.get(i) + " has organisms " + String.join(", ",  unhandledOrgSet) + " without taxonomy information\n";
                    Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);                    
                }
                
                try {
                    Date date = new Date(System.currentTimeMillis());
                    String comment = DATE_FORMATTER.format(date) + " " + userName + " " + MSG_COMMENT;
                    StringBuffer errBuf = new StringBuffer();
                    StringBuffer paintErrBuf = new StringBuffer();
                    int rtnCode = dataIO.fixBook(updateList.get(i), user, DataIO.CUR_CLASSIFICATION_VERSION_SID, comment, unLockIfNecessary, errBuf, paintErrBuf);
                    if (DataIO.CODE_FIX_BOOK_UPDATE_UNNECESSARY == rtnCode) {
                        msg = "No update was necessary for book " + updateList.get(i) + "\n";
                    }
                    else if (DataIO.CODE_FIX_BOOK_UPDATE_SUCCESSFUL == rtnCode) {
                        msg = "Updatate successful for " + updateList.get(i) + "\n";
                    }
                    else {
                        if (DataIO.CODE_FIX_BOOK_UPDATE_UNSUCCESSFUL_BOOK_LOCKED == rtnCode) {
                            msg = "Updatate unsuccessful for " + updateList.get(i) + ", since the book is locked\n";                            
                        }
                        else {
                            msg = "Updatate unsuccessful for " + updateList.get(i) + "\n";
                        }
                    }
                    Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                    
                    // If there is any information about the book before the fix operation, write it out here
                    if (0 != errBuf.length() || 0 != paintErrBuf.length()) {
                        outputPreUpdateInfo(updateList.get(i), errBuf, paintErrBuf);
                    }
                    // Output leaf GAF info
                    outputLeafGAFInfo(updateList.get(i));
                }
                catch(Exception e) {
                    msg = "Exception while processing book " + updateList.get(i) + "\n";
                    Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                }
            }
        
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }         
    }
    
    protected Object sendAndReceiveZip(String servletURL, String actionRequest, Object sendInfo, String sessionIdName, String sessionIdValue) {
        String message = null;
        Object outputFromServlet = null;
        try {

            String progressMessage = "Fetching zip data";

            // connect to the servlet
            URL servlet
                    = new URL(servletURL + SERVLET_PATH + actionRequest);
            java.net.URLConnection servletConnection = servlet.openConnection();

            servletConnection.setRequestProperty(SERVLET_CONNECTION_CONTENT_TYPE, SERVLET_CONNECTION_OBJECT_TYPE_JAVA);

            // Set the session id, if necessary
            if ((null != sessionIdName) && (null != sessionIdValue)) {
                servletConnection.setRequestProperty(SERVLET_REQUEST_PROPERTY_COOKIE, sessionIdName + "=".concat(sessionIdValue));
            }

            // Connection should ignore caches if any
            servletConnection.setUseCaches(false);
            servletConnection.setRequestProperty("Accept-Encoding", "gzip");        // Indicate we are accepting gzip format

            // Indicate sending and receiving information from the server
            servletConnection.setDoInput(true);
            servletConnection.setDoOutput(true);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new GZIPOutputStream(servletConnection.getOutputStream()));

            objectOutputStream.writeObject(sendInfo);
            objectOutputStream.flush();
            objectOutputStream.close();
            ObjectInputStream servletOutput = new ObjectInputStream(new GZIPInputStream(servletConnection.getInputStream()));
            System.out.println("Received object from server for action request " + actionRequest);
            System.out.println("Going to start reading the object");
//                        System.out.println("Number of bytes available for reading " + servletOutput.available());
            outputFromServlet = servletOutput.readObject();
            System.out.println("Finished reading the object from the server");

            servletOutput.close();
            return outputFromServlet;
        } catch (MalformedURLException muex) {
            message = ("MalformedURLException " + muex.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
            muex.printStackTrace();
        } catch (IOException ioex) {
            message = ("IOException " + ioex.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
            ioex.printStackTrace();
        } catch (Exception e) {
            message = ("Exception " + e.getMessage()
                    + " has been returned while sending and receiving information from server");
            System.out.println(message);
            e.printStackTrace();
        }
        if (message != null) {
        }
        return outputFromServlet;
    }
    
    public void outputLeafGAFInfo(String book) {
        try {
            Path curFilePath = Paths.get(this.saveDirPath + DIR_LEAF_IBA_ANNOT + FILE_SEPARATOR, book + ".xml");
            Path curFile = curFilePath;
            if (false == Files.exists(curFilePath)) {
                if (false == Files.exists(curFilePath.getParent())) {
                    Files.createDirectories(curFilePath.getParent());
                }
                curFile = Files.createFile(curFilePath);
            } else {
                ArrayList<String> fileInfo = new ArrayList<String>();
                Files.write(curFile, fileInfo, StandardCharsets.UTF_8);
            }
            
            String url = serverUrl + URL_GET_LEAF_IBA_INFO.replace(REPLACE_STR, book);
            String IBAInfo = Utils.readFromUrl(url, -1, -1);
            ArrayList<String> IBAInfoList = new ArrayList<String>();
            IBAInfoList.add(IBAInfo);
            Files.write(curFile, IBAInfoList, StandardCharsets.UTF_8);
        }
        catch(Exception e) {
            
        }
    }
    
    
    public void outputPreUpdateInfo(String book, StringBuffer otherAnnotInfo, StringBuffer paintAnnotInfo) {
        try {
            Path curFilePath = Paths.get(this.saveDirPath + DIR_PRE_UPDATE_INFO + FILE_SEPARATOR, book + ".xml");
            Path curFile = curFilePath;
            if (false == Files.exists(curFilePath)) {
                if (false == Files.exists(curFilePath.getParent())) {
                    Files.createDirectories(curFilePath.getParent());
                }
                curFile = Files.createFile(curFilePath);
            } else {
                ArrayList<String> fileInfo = new ArrayList<String>();
                Files.write(curFile, fileInfo, StandardCharsets.UTF_8);
            }

            // Create an xml document and write out the messages
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);

            Element familyIdElem = doc.createElement(ELEMENT_FAMILY_ID);
            Text familyId_text = doc.createTextNode(book);
            familyIdElem.appendChild(familyId_text);
            root.appendChild(familyIdElem);            
            if (null != otherAnnotInfo) {
                Element annot_Info = doc.createElement(ELEMENT_FAMILY_ANNOTATION_INFO_OTHER);
                root.appendChild(annot_Info);
                String parts[] = otherAnnotInfo.toString().split(Pattern.quote(Constant.STR_NEWLINE));
                if (0 < parts.length) {
                    for (String part : parts) {
                        if (null != part) {
                            part = part.trim();
                        }
                        annot_Info.appendChild(Utils.createTextNode(doc, ELEMENT_MESSAGE, part));
                    }
                }           
            }
            if (null != paintAnnotInfo) {
                Element annot_Info = doc.createElement(ELEMENT_FAMILY_ANNOTATION_INFO_PAINT);
                root.appendChild(annot_Info);
                String parts[] = paintAnnotInfo.toString().split(Pattern.quote(Constant.STR_NEWLINE));
                if (0 < parts.length) {
                    for (String part : parts) {
                        if (null != part) {
                            part = part.trim();
                        }
                        annot_Info.appendChild(Utils.createTextNode(doc, ELEMENT_MESSAGE, part));
                    }
                }           
            }
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            String outputInfo = lsSerializer.writeToString(doc);
            if (null == outputInfo) {
                outputInfo = Constant.STR_EMPTY;
            }
            
            ArrayList<String> bookInfoList = new ArrayList<String>();
            bookInfoList.add(outputInfo);
            Files.write(curFile, bookInfoList, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        System.out.println("This utility has 2 options - Fix annotations for all books with PAINT annotations in system or Fix annotations for specified books");
        System.out.println("Specify server name, username, password, directory for storing output information and optionally absolute pathname for file containing list of books to be updated.");
        System.out.println("For example http://paintcuration.usc.edu username password C:/note/unix/dir/style or http://paintcuration.usc.edu username password /home/mydir/save_info_dir /home/mydir/save_info_dir/book_list_file");
//        if (args.length < 4) {
//            System.out.println("At least 4 parameters are required.  See description above");
//            return;
//        }
        FixAnnotUtility fau = new FixAnnotUtility();
//        fau.fixBooksInFile("http://localhost:8080", "paint_user", "password", "C:/usc/svn/new_panther/curation/paint/gopaint/branches/TaxConstraint/update_books/update_status", "C:/usc/svn/new_panther/curation/paint/gopaint/branches/TaxConstraint/update_books/updateList.txt");
//        fau.fixBooksInSystem("http://localhost:8080", "paint_user", "password", "C:/usc/svn/new_panther/curation/paint/gopaint/branches/TaxConstraint/update_books/update_status");
        if (args.length == 4) {
            fau.fixBooksInSystem(args[0], args[1], args[2], args[3]);
        }
        if (args.length == 5) {
            fau.fixBooksInFile(args[0], args[1], args[2], args[3], args[4]);
        }
    }

}




