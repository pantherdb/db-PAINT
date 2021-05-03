/**
 *  Copyright 2020 University Of Southern California
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

import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.logic.DataValidationManager;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class AnnotationInfo {

    public static final String URL_ALL_FAMILIES_WITH_EXPERIMENTAL_EVIDENCE = "/webservices/searchBooks.jsp?searchType=SEARCH_TYPE_BOOKS_WITH_EXPERIMENTAL_EVIDENCE";
    //public static final String URL_SEARCH_FAMILY_ANNOT_INFO = "/webservices/family.jsp?searchValue=%REPLACE_STR%&searchType=SEARCH_TYPE_FAMILY_ANNOTATION_INFO";
    public static final String URL_SEARCH_FAMILY_ANNOT_INFO_DETAILS = "/webservices/family.jsp?searchValue=%REPLACE_STR%&searchType=SEARCH_TYPE_FAMILY_ANNOTATION_INFO_DETAILS";    
    public static final String REPLACE_STR = "%REPLACE_STR%";
        
    public static final String DIR_INFO = "/info";
    public static final String FILE_STATUS = "status.txt";
    public static final String STR_EMPTY = "";

    private static final String ELEMENT_BOOK = "book";
    private static final String ELEMENT_ID = "id";
    
    public static final String FILE_SEPARATOR = "/";
    
    public static void processBooks(String urlCuration, String saveDir) {
        try {
            String filePrefix = saveDir + DIR_INFO ;
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
            
            if (null == DataValidationManager.getInstance().getBooksWithIncompleteTaxonInfo()) {
                String msg = "Unable to retrieve organism information from database for validating taxonomy constraints \n";
                Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                return;
            }
            
            HashSet<String> familySet = familiesWithExpEvidence(urlCuration);
            ArrayList<String> sorted = new ArrayList(familySet);
            Collections.sort(sorted);
            int size = sorted.size();
            for (int i = 0; i < size; i++) {
                String msg = "Processing " + (i + 1) + " of " + size + " book " + sorted.get(i) + "\n";
                Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                String url = urlCuration + URL_SEARCH_FAMILY_ANNOT_INFO_DETAILS.replace(REPLACE_STR, sorted.get(i));
                String bookInfo = Utils.readFromUrl(url, -1, -1);
                if (null == bookInfo) {
                    msg = "Error reading information for " + (i + 1) + " of " + size + " book " + sorted.get(i) + "\n";
                    Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                    continue;
                }

                Path curFilePath = Paths.get(saveDir, sorted.get(i) + ".xml");
                Path curFile = curFilePath;
                if (false == Files.exists(curFilePath)) {
                    if (false == Files.exists(curFilePath.getParent())) {
                        Files.createDirectories(curFilePath.getParent());
                    }
                    curFile = Files.createFile(curFilePath);
                }
                else {
                    ArrayList<String> fileInfo = new ArrayList<String>();
                    Files.write(curFile, fileInfo, StandardCharsets.UTF_8);                    
                }
                ArrayList<String> bookInfoList = new ArrayList<String>();
                bookInfoList.add(bookInfo);
                Files.write(curFile, bookInfoList, StandardCharsets.UTF_8);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static HashSet<String> familiesWithExpEvidence(String urlCuration) {
        HashSet<String> familySet = new HashSet<String>();

        try {
            String xml = Utils.readFromUrl(urlCuration + URL_ALL_FAMILIES_WITH_EXPERIMENTAL_EVIDENCE, -1, -1);
            if (null == xml) {
                return null;
            }
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));

            // normalize text representation
            doc.getDocumentElement().normalize();

            NodeList books = doc.getElementsByTagName(ELEMENT_BOOK);
            if (null != books) {

                for (int i = 0; i < books.getLength(); i++) {
                    Node aNode = books.item(i);
                    if (Node.ELEMENT_NODE != aNode.getNodeType()) {
                        continue;
                    }

                    NodeList ids = ((Element) aNode).getElementsByTagName(ELEMENT_ID);
                    if (null != ids && 0 != ids.getLength()) {
                        for (int j = 0; j < ids.getLength(); j++) {
                            Node anId = ids.item(j);
                            if (Node.ELEMENT_NODE != anId.getNodeType()) {
                                continue;
                            }
                            familySet.add(Utils.getTextFromElement((Element) anId));
                            break;
                        }
                    }

                }

            }

            return familySet;
        } catch (SAXParseException err) {
            err.printStackTrace();

        } catch (SAXException e) {
            e.printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();

        }
        return null;

    }
    
    public static void main(String[] args) {
        System.out.println("Specify server path followed by path for storing information.");
        System.out.println("If running on Windows, path should be as C:/somedir.  For example http://paintcuration.usc.edu /home/panther/mydir/samples or http://paintcuration.usc.edu C:/somedir/otherdir");
//        processBooks("http://localhost:8080/", "C:/usc/svn/new_panther/curation/paint/gopaint/branches/TaxConstraint/samples");
        
//        processBooks("http://paintcuration.usc.edu", "/home/joeBlow/someDir/samples");        
        processBooks(args[0], args[1]);
    }
    
    public static void main2(String[] args) {
        try {
            //initialize Path object
            Path path = Paths.get("C:/data/file.txt");
            Files.createDirectories(path.getParent());
            //create file

            Path createdFilePath = Files.createFile(path);
            System.out.println("File Created at Path : " + createdFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
