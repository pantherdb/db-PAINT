/**
 *  Copyright 2022 University Of Southern California
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
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.logic.DataValidationManager;
import edu.usc.ksom.pm.panther.paintServer.logic.DataAccessManager;
import edu.usc.ksom.pm.panther.paintServer.webservices.FamilyUtil;
import edu.usc.ksom.pm.panther.paintServer.webservices.WSConstants;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GenerateIBA {

    public static final String ENCODING = "UTF-8";
    private String serverUrl;
    private String bookListFile;
    private List<String> books;
    private String saveDirPath;

    private HashMap<String, HashSet<String>> bookToOrgLookup = null;

    public static final String DIR_INFO = "/info";
    public static final String DIR_PRE_UPDATE_INFO = "/preUpdate";
    public static final String DIR_LEAF_IBA_ANNOT = "/leafIBAInfo";
    public static final String DIR_INTERNAL_IBA_ANNOT = "/internalIBAInfo";      
    public static final String FILE_STATUS = "status.txt";

    public static final String FILE_SEPARATOR = "/";

    public static final String URL_GET_LEAF_IBA_INFO = "/webservices/family.jsp?searchValue=%REPLACE_STR%&searchType=SEARCH_TYPE_FAMILY_LEAF_IBA_ANNOTATION_INFO_DETAILS";
    public static final String REPLACE_STR = "%REPLACE_STR%";
    private static DataIO dataIO = DataAccessManager.getInstance().getDataIO();

    public void generateInfoForSpecifiedBooks(String serverUrl, String saveDirPath, String bookListFile) {
        this.serverUrl = serverUrl;
        this.bookListFile = bookListFile;
        this.saveDirPath = saveDirPath;

        books = getBooks();
        processBooks();

    }

    public void generateInfoForAllBooks(String serverUrl, String saveDirPath) {
        this.serverUrl = serverUrl;
        this.saveDirPath = saveDirPath;

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getBooksWithPaintAnnotations() {
        List<Book> books = dataIO.getBooksWithPAINTAnnotations(DataIO.CUR_CLASSIFICATION_VERSION_SID);

        ArrayList<String> famList = new ArrayList<String>();
        for (Book b : books) {
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
        
        try {
            String filePrefix = saveDirPath + DIR_INFO;
            Path tmpFileStatusPath = Paths.get(filePrefix + FILE_SEPARATOR + FILE_STATUS);
            Path statusFilePath = tmpFileStatusPath;
            if (false == Files.exists(tmpFileStatusPath)) {
                if (false == Files.exists(tmpFileStatusPath.getParent())) {
                    Files.createDirectories(tmpFileStatusPath.getParent());
                }
                statusFilePath = Files.createFile(tmpFileStatusPath);
            } else {
                ArrayList<String> fileInfo = new ArrayList<String>();
                Files.write(statusFilePath, fileInfo, StandardCharsets.UTF_8);
            }
            int size = books.size();
            for (int i = 0; i < size; i++) {
                String book = books.get(i);
                String msg = "Processing " + (i + 1) + " of " + size + " book " + book + "\n";
                Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);

                HashSet<String> unhandledOrgSet = bookToOrgLookup.get(books);
                if (null != unhandledOrgSet && false == unhandledOrgSet.isEmpty()) {
                    msg = "Warning book " + books + " has organisms " + String.join(", ", unhandledOrgSet) + " without taxonomy information\n";
                    Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                }
                outputGAFInfo(book);

            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void outputGAFInfo(String book) {
        StringBuffer leafNodeBuf = new StringBuffer();
        StringBuffer internalNodeBuf = new StringBuffer();
        FamilyUtil.getXMLForIBAAnnotationInfoDetails(book, WSConstants.PROPERTY_CLS_VERSION, leafNodeBuf, internalNodeBuf);
        writeFile(book, DIR_LEAF_IBA_ANNOT, leafNodeBuf);
        writeFile(book, DIR_INTERNAL_IBA_ANNOT, internalNodeBuf);
//        try {
//            Path curFilePath = Paths.get(this.saveDirPath + DIR_LEAF_IBA_ANNOT + FILE_SEPARATOR, book + ".xml");
//            Path curFile = curFilePath;
//            if (false == Files.exists(curFilePath)) {
//                if (false == Files.exists(curFilePath.getParent())) {
//                    Files.createDirectories(curFilePath.getParent());
//                }
//                curFile = Files.createFile(curFilePath);
//            } else {
//                ArrayList<String> fileInfo = new ArrayList<String>();
//                Files.write(curFile, fileInfo, StandardCharsets.UTF_8);
//            }
//            
//            String url = serverUrl + URL_GET_LEAF_IBA_INFO.replace(REPLACE_STR, book);
//            String IBAInfo = Utils.readFromUrl(url, -1, -1);
//            ArrayList<String> IBAInfoList = new ArrayList<String>();
//            IBAInfoList.add(IBAInfo);
//            Files.write(curFile, IBAInfoList, StandardCharsets.UTF_8);
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
    }
    
    
    public void writeFile(String book, String directory, StringBuffer contentBuf) {
        try {
            Path curFilePath = Paths.get(this.saveDirPath + directory + FILE_SEPARATOR, book + ".xml");
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
            
            ArrayList<String> IBAInfoList = new ArrayList<String>();
            IBAInfoList.add(contentBuf.toString());
            Files.write(curFile, IBAInfoList, StandardCharsets.UTF_8);
        }
        catch(Exception e) {
            e.printStackTrace();
        }        
    }    

//    public void outputLeafGAFInfo(String book) {
//        try {
//            Path curFilePath = Paths.get(this.saveDirPath + DIR_LEAF_IBA_ANNOT + FILE_SEPARATOR, book + ".xml");
//            Path curFile = curFilePath;
//            if (false == Files.exists(curFilePath)) {
//                if (false == Files.exists(curFilePath.getParent())) {
//                    Files.createDirectories(curFilePath.getParent());
//                }
//                curFile = Files.createFile(curFilePath);
//            } else {
//                ArrayList<String> fileInfo = new ArrayList<String>();
//                Files.write(curFile, fileInfo, StandardCharsets.UTF_8);
//            }
//
//            String url = serverUrl + URL_GET_LEAF_IBA_INFO.replace(REPLACE_STR, book);
//            String IBAInfo = Utils.readFromUrl(url, -1, -1);
//            ArrayList<String> IBAInfoList = new ArrayList<String>();
//            IBAInfoList.add(IBAInfo);
//            Files.write(curFile, IBAInfoList, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void main(String[] args) {

        System.out.println("This utility has 2 options - Generate Leaf IBA annotation information for all books with PAINT annotations in system or generate IBA annotations for specified books");
        System.out.println("Specify server name, directory for storing output information and optionally absolute pathname for file containing list of books to be updated with one line per book");
        System.out.println("For example http://paintcuration.usc.edu C:/note/unix/dir/style or http://paintcuration.usc.edu /home/mydir/save_info_dir /home/mydir/save_info_dir/book_list_file");

        GenerateIBA giba = new GenerateIBA();
//        giba.generateInfoForSpecifiedBooks("http://localhost:8080", "C:/temp/test", "C:/temp/test/updateList.txt");
//        giba.generateInfoForAllBooks("http://panthertest4.med.usc.edu:8084", "C:/temp/test");
        if (args.length == 2) {
            giba.generateInfoForAllBooks(args[0], args[1]);
        }
        if (args.length == 3) {
            giba.generateInfoForSpecifiedBooks(args[0], args[1], args[2]);
        }
    }
}
