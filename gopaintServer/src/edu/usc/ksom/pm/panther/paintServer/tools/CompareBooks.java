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
import com.sri.panther.paintCommon.util.Utils;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.TreeNodes;
import edu.usc.ksom.pm.panther.paintCommon.WithEvidence;
import static edu.usc.ksom.pm.panther.paintServer.tools.FixAnnotUtility.ENCODING;
import edu.usc.ksom.pm.panther.paintServer.webservices.WSConstants;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class CompareBooks {
    protected String UrlPreviousVersion;
    protected String UrlCurrentVersion;
    protected String saveDirPath;
    protected String bookListFile;
    
    protected HashSet<String> previousFamSet;
    protected HashSet<String> currentFamSet;
    protected HashSet<String> booksForProcessing;
    
    private static final String ELEMENT_FAMILY_ID = "family_id";
    private static final String ELEMENT_FAMILY_NAME = "family_name";
    private static final String ELEMENT_ANNOTATION_NODE = "annotation_node";
    private static final String ELEMENT_ANNOTATION_ACCESSION = "accession";
    private static final String ELEMENT_ANNOTATION_PUBLIC_ID = "public_id";
//    private static final String ELEMENT_ANNOTATION_BRANCH_LENGTH = "branch_length";
    private static final String ELEMENT_NODE_NAME = "node_name";
//    private static final String ELEMENT_ANNOTATION_GENE_SYMBOL = "gene_symbol";
//    private static final String ELEMENT_REFERENCE_SPECIATION_EVENT= "reference_speciation_event";
    private static final String ELEMENT_ANNOTATION_NODE_TYPE = "node_type";
//    private static final String ELEMENT_ANNOTATION_EVENT_TYPE = "event_type";
    private static final String ELEMENT_ANNOTATION_CHILDREN = "children";
//    private static final String ELEMENT_ANNOTATION_SEQUENCE = "sequence";
    
    public static final String ELEMENT_ANNOTATION_ANNOTATION_LIST = "annotation_list";
    public static final String ELEMENT_ANNOTATION_PAINT_ANNOTATION = "paint_annotation";
    public static final String ELEMENT_ANNOTATION_TERM = "term";
    public static final String ELEMENT_ANNOTATION_EVIDENCE_CODE = "evidence_code";
    public static final String ELEMENT_ANNOTATION_FROM_PAINT = "annotation_from_paint";
    public static final String ELEMENT_ANNOTATION_DIRECT = "annotation_direct";
    public static final String ELEMENT_ANNOTATION_WITH = "annotation_with";
    public static final String ELEMENT_ANNOTATION_TERM_NAME = "term_name";
    public static final String ELEMENT_ANNOTATION_TERM_ASPECT = "term_aspect";
    private static final String ELEMENT_QUALIFIER_LIST = "qualifier_list";
    
  
    
//    private static final String ELEMENT_QUALIFIER = "qualifier";
    private static final String ELEMENT_ANNOTATION_WITH_LIST = "annotation_with_list";
    private static final String ELEMENT_ANNOT_WITH_ANNOTATION_TO_NODE = "annotation_with_annotation_to_node";
    private static final String ELEMENT_ANNOT_WITH_DB_REFERENCE = "annotation_with_db_reference";    
    private static final String ELEMENT_ANNOT_WITH_NODE = "annotation_with_node";     
    
    private static final String NODE_TYPE_ROOT = "root";        
    
    public static final String REPLACE_STR = "%REPLACE_STR%";
    private static final String SUFFIX_URL_ANNOT_INFO = "/webservices/family.jsp?searchValue=%REPLACE_STR%&searchType=" + WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS_NO_MSA;
    
    private static final Logger LOGGER = Logger.getLogger(CompareBooks.class);
    
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static final String DIR_INFO = "/info";
    public static final String DIR_PREVIOUS_INFO = "/previousInfo";
    public static final String DIR_CURRENT_INFO = "/currentInfo";

    public static final String FILE_STATUS = "status.txt";

    public static final String FILE_SEPARATOR = "/";

    public CompareBooks(String UrlPreviousVersion, String UrlCurrentVersion, String saveDirPath, String bookListFile) {
        this.UrlPreviousVersion = UrlPreviousVersion;
        this.UrlCurrentVersion = UrlCurrentVersion;
        this.saveDirPath = saveDirPath;
        this.bookListFile = bookListFile;
        setFamiliesForComparison();
//        if (1 == 1) {
//            return;
//        }


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
            process(statusFilePath);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        
    }
    
    public void setFamiliesForComparison() {
        previousFamSet = AnnotationInfo.familiesWithExpEvidence(UrlPreviousVersion);
        if (null == previousFamSet) {
            System.out.println("Nothing to previous from previous comparison set");
            return;
        }

        currentFamSet = AnnotationInfo.familiesWithExpEvidence(UrlCurrentVersion);
        if (null == currentFamSet) {
            System.out.println("Nothing to previous from current comparison set");
            return;            
        }
        booksForProcessing = new HashSet<String>();
        for (String book: previousFamSet) {
            if (currentFamSet.contains(book)) {
                booksForProcessing.add(book);
            }
//            else {
//                System.out.println("Skipping over book that is no longer in currrent set " + book);
//            }
        }
        
//        for (String book: currentFamSet) {
//            if (false == previousFamSet.contains(book)) {
//                System.out.println("Skipping over book that was not part of previous set " + book);                
//            }
//        }
        
        
        
        if (null != bookListFile) {
            List<String> bookList = getBooks();
            // Remove any duplicates
            HashSet<String> bookSet = null;
            if (null != bookList) {
                bookSet = new HashSet<String>(bookList);
            }
            if (null == bookSet) {
                System.out.println("No books in set for processing");
                return;
            }
            for (Iterator<String> curBooksIter = booksForProcessing.iterator(); curBooksIter.hasNext();) {
                if (false == bookSet.contains(curBooksIter.next())) {
                    curBooksIter.remove();
                }
            }
        }
    }
    
    public void process(Path statusFilePath) {
        ArrayList<String> processList = new ArrayList<String>(booksForProcessing);
        Collections.sort(processList);
        int size = processList.size();
        for (int i = 0; i < size; i++) {
            processBook(i + 1, size, processList.get(i), statusFilePath);
        }
    }

    public void processBook(int num, int size, String book, Path statusFilePath) {
        try {
            String msg = "Processing book - " + book + " " + num + " of " + size + "\n";
            Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
            if (null == book) {
                return;
            }

            ExecutorService executor = Executors.newFixedThreadPool(2);
            ProcessAnnotInfoWorker piwp = new ProcessAnnotInfoWorker(book, this.UrlPreviousVersion + Utils.replace(SUFFIX_URL_ANNOT_INFO, REPLACE_STR, book));
            ProcessAnnotInfoWorker piwc = new ProcessAnnotInfoWorker(book, this.UrlCurrentVersion + Utils.replace(SUFFIX_URL_ANNOT_INFO, REPLACE_STR, book));
            executor.execute(piwp);
            executor.execute(piwc);

            executor.shutdown();

            // Wait for both threads to finish
            while (!executor.isTerminated()) {
                
            }
            try {
                if (null != piwp.outputBuf) {
                    writeFile(book, DIR_PREVIOUS_INFO, piwp.outputBuf);
                }
                else {
                     msg = "No information found for previous version - book " + book + "\n";
                    Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                }
                if (null != piwc.outputBuf) {

                    writeFile(book, DIR_CURRENT_INFO, piwc.outputBuf);
                } else {
                    msg = "No information found for current version - book " + book + "\n";
                    Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
                }
                        
            } catch (Exception e) {
                msg = "Exception while processing book " + book + "\n";
                Files.write(statusFilePath, msg.getBytes(), StandardOpenOption.APPEND);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

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
    
    public void writeFile(String book, String directory, StringBuffer contentBuf) {
        try {
            Path curFilePath = Paths.get(this.saveDirPath + directory + FILE_SEPARATOR, book + ".txt");
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
            
            ArrayList<String> infoList = new ArrayList<String>();
            infoList.add(contentBuf.toString());
            Files.write(curFile, infoList, StandardCharsets.UTF_8);
        }
        catch(Exception e) {
            e.printStackTrace();
        }        
    }    
    
    public static void main(String args[]) {
        System.out.println("Enter url to access previous version, current version, path for storing information and optional parameter to file containing list of books to compare.  Else all books common to both systems will be compared");
            new CompareBooks("http://paintcuration.usc.edu", "http://panthertest4.med.usc.edu:8084", "C://temp//test", "C://temp//test//unique");
//        if (args.length < 3) {
//            return;
//        }
//        if (args.length == 3) {
//            new CompareBooks(args[0], args[1], args[2], null);
//            return;
//        }
//        new CompareBooks(args[0], args[1], args[2], args[3]);
    }
    
    private class ProcessAnnotInfoWorker implements Runnable {
        public String bookAcc;
        public String url;
        public Book book = null;
        public StringBuffer outputBuf;
        
        private ProcessAnnotInfoWorker(String bookAcc, String url) {
            this.bookAcc = bookAcc;
            this.url = url;
        }

       
        public void run() {
            String xml = Utils.readFromUrl(url, -1, -1);
            if (null == xml) {
                return;
            }
            String accession = null;
            String name = null;
            try {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
    
                // normalize text representation
                doc.getDocumentElement().normalize();
                
                NodeList ids = doc.getElementsByTagName(ELEMENT_FAMILY_ID);
                if (null != ids) {
                    for (int i = 0; i < ids.getLength(); i++) {
                        Node aNode = ids.item(i);
                        if (Node.ELEMENT_NODE != aNode.getNodeType()) {
                            continue;
                        }

                        Element idElement = (Element)aNode;
                        accession = Utils.getTextFromElement( idElement);
                        //family.setAccession(idElement.getNodeValue());
                        break;
                    }
                }
                
                NodeList names = doc.getElementsByTagName(ELEMENT_FAMILY_NAME);
                if (null != names) {
                    for (int i = 0; i < names.getLength(); i++) {
                        Node aNode = names.item(i);
                        if (Node.ELEMENT_NODE != aNode.getNodeType()) {
                            continue;
                        }
                        Element nameElement = (Element)aNode;
                        name = Utils.getTextFromElement( nameElement);
                        //family.setName(nameElement.getNodeValue());
                        break;
                    }
                }
                
                book = new Book(accession, name, Book.CURATION_STATUS_UNKNOWN, null);
                if (null == name) {
                    name = WSConstants.STR_HYPHEN;
                }
                
                Element rootNode = null;
                NodeList annotationNodes = doc.getElementsByTagName(ELEMENT_ANNOTATION_NODE);
                if (null  == annotationNodes || 0 == annotationNodes.getLength()) {
                    return;
                }
                
                // First node is the root
                Node aNode = annotationNodes.item(0);
                if (null != aNode) {
                    if (Node.ELEMENT_NODE != aNode.getNodeType()) {
                        return;
                    }
                    Element anElement = (Element)aNode;
                    String nodeType = Utils.getTextFromXml(anElement, ELEMENT_ANNOTATION_NODE_TYPE);
                    if (null != nodeType && 0 == nodeType.compareToIgnoreCase(NODE_TYPE_ROOT)) {
                        rootNode = anElement;
                    }
                }
    
                if (null == rootNode) {
                    return;
                }
                TreeNodes treeNodes = parseTreeStructure(rootNode);
                System.out.println(DATE_TIME_FORMAT.format(new Date(System.currentTimeMillis())) + " Got family structure information for " + bookAcc);
                if (null == treeNodes) {
                    return;
                }
                HashMap<String, edu.usc.ksom.pm.panther.paintCommon.Node> nodesTbl = treeNodes.getNodesTbl();
                if (null == nodesTbl) {
                    return;
                }
                
                // Sort by Public id
                ArrayList<edu.usc.ksom.pm.panther.paintCommon.Node> nodes = new ArrayList<edu.usc.ksom.pm.panther.paintCommon.Node>(nodesTbl.values());
                Collections.sort(nodes, new Comparator<edu.usc.ksom.pm.panther.paintCommon.Node>() {
                    public int compare(edu.usc.ksom.pm.panther.paintCommon.Node s1, edu.usc.ksom.pm.panther.paintCommon.Node s2) {
                        return s1.getStaticInfo().getPublicId().compareTo(s2.getStaticInfo().getPublicId());
                    }
                });
                
                // output the information
                outputBuf = new StringBuffer();
                outputBuf.append(book.getId());
                outputBuf.append(WSConstants.STR_NEWLINE);
                for (edu.usc.ksom.pm.panther.paintCommon.Node node: nodes) {
                    String publicId = node.getStaticInfo().getPublicId();
                    NodeVariableInfo nvi = node.getVariableInfo();
                    if (null != nvi) {
                        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
                        if (null != goAnnotList) {
                            for (Annotation a : goAnnotList) {
                                AnnotationDetail ad = a.getAnnotationDetail();
                                outputBuf.append(publicId);
                                outputBuf.append(WSConstants.STR_TAB);
                                outputBuf.append(a.getGoTerm());
                                outputBuf.append(WSConstants.STR_TAB);
                                outputBuf.append(String.join(WSConstants.STR_COMMA, ad.getEvidenceCodes()));
                                outputBuf.append(WSConstants.STR_TAB);
                                Set<Qualifier> qSet = a.getQualifierSet();
                                if (null == qSet || qSet.isEmpty()) {
                                    outputBuf.append(WSConstants.STR_HYPHEN);
                                } else {
                                    HashSet<String> uniqQualifier = new HashSet<String>();
                                    for (Qualifier q: qSet) {
                                        if (null == q.getText() || 0 == q.getText().trim().length()) {
                                            continue;
                                        }
                                        uniqQualifier.add(q.getText().trim());
                                    }
                                    outputBuf.append(String.join(WSConstants.STR_COMMA, uniqQualifier));
                                }
                                outputBuf.append(WSConstants.STR_NEWLINE);
                            }
                        }
                    }
                }
                
                
                
            }
            catch (SAXParseException err) {
                LOGGER.error("** Parsing error" + ", line " + err.getLineNumber() + ", uri " +err.getSystemId());
                LOGGER.error(" " + err.getMessage());
    
            } catch (SAXException e) {
                Exception x = e.getException();
                ((x == null) ? e : x).printStackTrace();
    
            } catch (Throwable t) {
                t.printStackTrace();
                System.out.println("Error reading family information " + this.bookAcc);
            } 
            
        }
        
        
    }
    public TreeNodes parseTreeStructure(Element root) {
        HashMap<String, edu.usc.ksom.pm.panther.paintCommon.Node> nodesTbl = new HashMap<String, edu.usc.ksom.pm.panther.paintCommon.Node>();
        edu.usc.ksom.pm.panther.paintCommon.Node rootNode = parseTreeNodes(nodesTbl, root);
        TreeNodes t = new TreeNodes(rootNode, nodesTbl);
        return t;

    }
        
    private edu.usc.ksom.pm.panther.paintCommon.Node parseTreeNodes(HashMap<String, edu.usc.ksom.pm.panther.paintCommon.Node> nodesTbl, Element node) {
        if (null == nodesTbl || null == node) {
            return null;
        }
        String accession = Utils.getTextFromXml(node, ELEMENT_ANNOTATION_ACCESSION);
        if (null == accession) {
            return null;
        }
        edu.usc.ksom.pm.panther.paintCommon.Node anAnnotationNode = new edu.usc.ksom.pm.panther.paintCommon.Node();
        NodeStaticInfo nsi = new NodeStaticInfo();
        anAnnotationNode.setStaticInfo(nsi);
        nsi.setNodeAcc(accession);
        String publicId = Utils.getTextFromXml(node, ELEMENT_ANNOTATION_PUBLIC_ID);
        nsi.setPublicId(publicId);
//        if ("PTN000135975".equals(publicId)) {
//            System.out.println("Here");
//        }
        nodesTbl.put(accession, anAnnotationNode);

        nsi.setLongGeneName(Utils.getTextFromXml(node, ELEMENT_NODE_NAME));

        ArrayList<Node> childAnnotList = getChildNodesByTagName(node, ELEMENT_ANNOTATION_ANNOTATION_LIST);
        if (null != childAnnotList) {
            Node annotList = (Element) childAnnotList.get(0);

            NodeList annotL = annotList.getChildNodes();
            for (int i = 0; i < annotL.getLength(); i++) {
                Node anAnnot = annotL.item(i);
                if (Node.ELEMENT_NODE != anAnnot.getNodeType()) {
                    continue;
                }
                Element annotElem = (Element) anAnnot;
                // Only interested in direct annotations from PAINT
                String annotationDirect = Utils.getTextFromXml(annotElem, ELEMENT_ANNOTATION_DIRECT);
                String annotationFromPaint = Utils.getTextFromXml(annotElem, ELEMENT_ANNOTATION_FROM_PAINT);
                if (null != annotationDirect && null != annotationFromPaint && Boolean.TRUE.booleanValue() == Boolean.parseBoolean(annotationDirect)) {
                    NodeVariableInfo nvi = anAnnotationNode.getVariableInfo();
                    if (null == nvi) {
                        nvi = new NodeVariableInfo();
                        anAnnotationNode.setVariableInfo(nvi);
                    }
                    Annotation a = new Annotation();
                    nvi.addGOAnnotation(a);

                    a.setGoTerm(Utils.getTextFromXml(annotElem, ELEMENT_ANNOTATION_TERM));
                    AnnotationDetail ad = new AnnotationDetail();
                    a.setAnnotationDetail(ad);
                    WithEvidence we = new WithEvidence();
                    NodeList withList = annotElem.getElementsByTagName(ELEMENT_ANNOTATION_WITH_LIST);
                    if (null != withList && 0 != withList.getLength()) {
                        Node withListItem = withList.item(0);
                        if (null != withListItem) {
                            NodeList withListItems = withListItem.getChildNodes();
                            for (int j = 0; j < withListItems.getLength(); j++) {
                                Node childWith = withListItems.item(j);
                                if (Node.ELEMENT_NODE != childWith.getNodeType()) {
                                    continue;
                                }
                                Element withItem = (Element)childWith;
                                String type = withItem.getTagName();
                                if (ELEMENT_ANNOT_WITH_ANNOTATION_TO_NODE.equals(type)) {
                                    we.setWith(new Annotation());
                                }
                                else if (ELEMENT_ANNOT_WITH_DB_REFERENCE.equals(type)) {
                                    we.setWith(new DBReference());
                                }
                                else if (ELEMENT_ANNOT_WITH_NODE.equals(type)) {
                                    we.setWith(new edu.usc.ksom.pm.panther.paintCommon.Node());
                                }
                            }
                        }
                    }

//                a.setTermName(Utils.getTextFromXml(annotElem, ELEMENT_ANNOTATION_TERM_NAME));
//                a.setAspect(Utils.getTextFromXml(annotElem, ELEMENT_ANNOTATION_TERM_ASPECT));
                    we.setEvidenceCode(Utils.getTextFromXml(annotElem, ELEMENT_ANNOTATION_EVIDENCE_CODE));
                    ad.addWithEvidence(we);

                    NodeList qualifierList = annotElem.getElementsByTagName(ELEMENT_QUALIFIER_LIST);
                    if (null != qualifierList && 0 != qualifierList.getLength()) {
                        Node qualList = qualifierList.item(0);
                        if (null != qualList) {
                            NodeList qualL = qualList.getChildNodes();
                            for (int j = 0; j < qualL.getLength(); j++) {
                                Node aQual = qualL.item(j);
                                if (Node.ELEMENT_NODE != aQual.getNodeType()) {
                                    continue;
                                }
                                Element qualElem = (Element) aQual;
                                Qualifier q = new Qualifier();
                                q.setText(Utils.getTextFromElement(qualElem));
                                a.addQualifier(q);
                            }
                        }
                    }

                } else {
                    continue;
                }

                

//
//                Vector<Node> withAnnotList = getChildNodesByTagName(annotElem, ELEMENT_ANNOTATION_WITH_LIST);
//                if (null != withAnnotList) {
//                    Node withList = (Element) withAnnotList.get(0);
//
//                    NodeList withL = withList.getChildNodes();
//                    for (int j = 0; j < withL.getLength(); j++) {
//                        Node with = withL.item(j);
//                        if (Node.ELEMENT_NODE != with.getNodeType()) {
//                            continue;
//                        }
//                        Element withElem = (Element) with;
//                        String tagName = withElem.getTagName();
//                        if (true == ELEMENT_ANNOT_WITH_ANNOTATION_TO_NODE.equals(tagName)) {
//                            a.addWithAnnotToNode(Utils.getTextFromElement(withElem));
//                        } else if (ELEMENT_ANNOT_WITH_NODE.equals(tagName)) {
//                            a.addWithNode(Utils.getTextFromElement(withElem));
//                        } else if (ELEMENT_ANNOT_WITH_DB_REFERENCE.equals(tagName)) {
//                            a.addWithDbRef(Utils.getTextFromElement(withElem));
//                        }
//                    }
//                }

            }
            NodeVariableInfo nvi = anAnnotationNode.getVariableInfo();
            if (null != nvi) {
                ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
                if (null != goAnnotList) {
                    Collections.sort(goAnnotList, new Comparator<Annotation>() {
                        public int compare(Annotation a1, Annotation a2) {
                            return a1.getGoTerm().compareTo(a2.getGoTerm());
                        }
                    });
                }
            }
        }
        NodeList childrenList = node.getElementsByTagName(ELEMENT_ANNOTATION_CHILDREN);
        if (null != childrenList) {
            if (0 != childrenList.getLength()) {
                Node childList = childrenList.item(0);
                if (null != childList) {
                    NodeList children = childList.getChildNodes();
                    for (int i = 0; i < children.getLength(); i++) {
                        Node aChild = children.item(i);
                        if (Node.ELEMENT_NODE != aChild.getNodeType()) {
                            continue;
                        }
                        edu.usc.ksom.pm.panther.paintCommon.Node childAnnot = parseTreeNodes(nodesTbl, (Element) aChild);
                        if (null != childAnnot) {
                            nsi.addChild(childAnnot);
                            nsi.setParent(anAnnotationNode);
                            //childAnnot.setParentId(anAnnotationNode.getPublicId());
                        }
                    }
                }
            }
        }

        return anAnnotationNode;

    }

    public ArrayList<Node> getChildNodesByTagName(Element elem, String tag) {
        if (null == elem || null == tag) {
            return null;
        }
        NodeList childNodes = elem.getChildNodes();
        if (null == childNodes) {
            return null;
        }
        ArrayList<Node> rtnList = new ArrayList<Node>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (tag.equals(childNodes.item(i).getNodeName())) {
                rtnList.add(childNodes.item(i));
            }
        }
        if (true == rtnList.isEmpty()) {
            return null;
        }
        return rtnList;
    }   
    
}
