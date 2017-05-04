package com.usc.panther.paintServer.webservices;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.User;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.database.DataServer;
import com.sri.panther.paintServer.database.DataServerManager;
import com.sri.panther.paintServer.util.ConfigFile;
import java.util.HashSet;

import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class BookListUtil {

    public static final String ELEMENT_SEARCH = "search";
    public static final String ELEMENT_SEARCH_STRING = "search_string";
    public static final String ELEMENT_SEARCH_DATABASE = "search_database";
    public static final String ELEMENT_SEARCH_UPLVERSION = "search_uplversion";
    public static final String ELEMENT_SEARCH_TYPE = "search_type";
    
    public static final String ELEMENT_BOOKS = "books";
    public static final String ELEMENT_BOOK = "book";
    public static final String ELEMENT_ID = "id";
    public static final String ELEMENT_NAME = "name";
    public static final String ELEMENT_STATUS_LIST = "status_list";
    public static final String ELEMENT_STATUS = "status";
    public static final String ELEMENT_LOCKED_BY = "locked_by";
//    public static final String ELEMENT_USER = "user";
    public static final String ELEMENT_USER_FNAME = "first_name";
    public static final String ELEMENT_USER_LNAME = "last_name";
    public static final String ELEMENT_USER_EMAIL = "email";
    
    public static final String STRING_NULL = "null";
    


    
    
    public static String searchBooks(String searchField, String database, String uplVersion, String searchType) {
        
        if (null == database) {
            database = WSConstants.PROPERTY_DB_STANDARD;
        }
        if (null == uplVersion) {
            uplVersion = WSConstants.PROPERTY_CLS_VERSION;
        }
        Vector <Book> books = null;
        DataServer ds = DataServerManager.getDataServer(database);
        if (null == ds || null == searchType) {
            return outputXMLInfo(books, searchField, database, uplVersion, searchType);
        }
        if (true == WSConstants.SEARCH_TYPE_STR_GENE_SYMBOL.equals(searchType)) {
            books = ds.searchBooksByGeneSymbol(searchField, uplVersion);
        }
        else if (true == WSConstants.SEARCH_TYPE_STR_GENE_ID.equals(searchType)) {
            books = ds.searchBooksByGenePrimaryExtAcc(searchField, uplVersion);
        }
        else if (true == WSConstants.SEARCH_TYPE_STR_PROTEIN_ID.equals(searchType)) {
            books = ds.searchBooksByProteinPrimaryExtId(searchField, uplVersion);
        }
        else if (true == WSConstants.SEARCH_TYPE_STR_DEFINITION.equals(searchType)) {
            books = ds.searchBooksByDefinition(searchField, uplVersion);           
        }
        else if (true == WSConstants.SEARCH_TYPE_STR_ALL_BOOKS.equals(searchType)) {
            books = ds.getAllBooks(uplVersion);
        }
        else if (true == WSConstants.SEARCH_TYPE_STR_UNCURATED_BOOKS.equals(searchType)) {
            books = ds.getUncuratedUnlockedBooks(uplVersion);
        }
        else if (true == WSConstants.SEARCH_TYPE_STR_BOOKS_WITH_EXP_EVIDENCE.equals(searchType)) {
            DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
            HashSet<String> bookSet = dataIO.getBooksWithExpEvdnceForLeaves();
            
            if (null != bookSet) {
                books = new Vector<Book>(bookSet.size());
                for (String accession : bookSet) {
                    Book b = new Book(accession, null, Book.CURATION_STATUS_UNKNOWN, null);
                    books.add(b);
                }
            }
        }        
        
        
        
        if (null == searchField) {
            searchField = STRING_NULL;
        }
        if (null == database) {
            database = STRING_NULL;
        }
        
        if (null == uplVersion) {
            uplVersion = STRING_NULL;
        }
        
        if (null == searchType) {
            searchType = STRING_NULL;
        }
        
        return outputXMLInfo(books, searchField, database, uplVersion, searchType);
    }
    
    private static String outputXMLInfo (Vector <Book> books, String searchField, String database, String uplVersion, String searchType) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  


            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchString = doc.createElement(ELEMENT_SEARCH_STRING);
            Text searchStringText = doc.createTextNode(searchField);
            searchString.appendChild(searchStringText);
            root.appendChild(searchString);
            
            Element databaseElement = doc.createElement(ELEMENT_SEARCH_DATABASE);
            Text databaseText = doc.createTextNode(database);
            databaseElement.appendChild(databaseText);
            root.appendChild(databaseElement);
            
            Element uplElement = doc.createElement(ELEMENT_SEARCH_UPLVERSION);
            Text uplText = doc.createTextNode(uplVersion);
            uplElement.appendChild(uplText);
            root.appendChild(uplElement);
            
            Element searchTypeElement = doc.createElement(ELEMENT_SEARCH_TYPE);
            Text typeText = doc.createTextNode(searchType);
            searchTypeElement.appendChild(typeText);
            root.appendChild(searchTypeElement);
            
            Element booksElement = createBookInfo(books, doc);
            if (null != booksElement) {
                root.appendChild(booksElement);
            }
            
            // Output information
            DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
            LSSerializer lsSerializer = domImplementation.createLSSerializer();
            return lsSerializer.writeToString(doc); 
//            DOMSource domSource = new DOMSource(doc);
//            StringWriter writer = new StringWriter();
//            StreamResult result = new StreamResult(writer);
//            TransformerFactory tf = TransformerFactory.newInstance();
//            Transformer transformer = tf.newTransformer();
//            transformer.transform(domSource, result);
//            return writer.toString();
            
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        
    }
    
    
    
    private static Element createBookInfo(Vector<Book> books, Document doc) {
        if (null == books || null == doc) {
            return null;
        }
        
        Element booksElement = doc.createElement(ELEMENT_BOOKS);
        for (int i = 0; i < books.size(); i++) {
            Book aBook = books.get(i);
            Element bookElement = doc.createElement(ELEMENT_BOOK);
            booksElement.appendChild(bookElement);
            Element id = WSUtil.createTextNode(doc, ELEMENT_ID, aBook.getId());
            if (null != id) {
                bookElement.appendChild(id);
            }
            Element name = WSUtil.createTextNode(doc, ELEMENT_NAME, aBook.getName());
            if (null != name) {
                bookElement.appendChild(name);
            }
            
            // Get status information
            Vector <String> statusList = getCurationStatusStrings(aBook);
            if (null != statusList) {
                Element statusListElement = doc.createElement(ELEMENT_STATUS_LIST);
                bookElement.appendChild(statusListElement);
                for (int j = 0; j < statusList.size(); j++) {
                    String statusStr = statusList.get(j);
                    Element statusElement = WSUtil.createTextNode(doc, ELEMENT_STATUS, statusStr);
                    if (null != statusElement) {
                        statusListElement.appendChild(statusElement);
                    }
                }
            }
            
            // Get locked by information
            User user = aBook.getLockedBy();
            if (null != user) {
                Element lockedByElement = doc.createElement(ELEMENT_LOCKED_BY);
                bookElement.appendChild(lockedByElement);
                Element firstNameElement = WSUtil.createTextNode(doc, ELEMENT_USER_FNAME, user.getFirstName());
                Element lastNameElement = WSUtil.createTextNode(doc, ELEMENT_USER_LNAME, user.getLastName());
                Element emailElement = WSUtil.createTextNode(doc, ELEMENT_USER_EMAIL, user.getEmail());
                if (null != firstNameElement) {
                    lockedByElement.appendChild(firstNameElement);
                }
                if (null != lastNameElement) {
                    lockedByElement.appendChild(lastNameElement);
                }
                if (null != emailElement) {
                    lockedByElement.appendChild(emailElement);
                }
            }
            
        }        
        return booksElement;
    }
    
    
    public static Vector<String> getCurationStatusStrings(Book aBook) {
        if (null == aBook) {
            return null;
        }
        Vector<String> outList = new Vector<String>();
        int status = aBook.getCurationStatus();
        if (0 != (status & Book.CURATION_STATUS_NOT_CURATED)) {
            outList.add(Book.LABEL_CURATION_STATUS_NOT_CURATED);
        }


        if (0 != (status & aBook.CURATION_STATUS_AUTOMATICALLY_CURATED)) {
            outList.add(Book.LABEL_CURATION_STATUS_AUTOMATICALLY_CURATED);
        }


        if (0 != (status & aBook.CURATION_STATUS_MANUALLY_CURATED)) {
            outList.add(Book.LABEL_CURATION_STATUS_MANUALLY_CURATED);
        }


        if (0 != (status & aBook.CURATION_STATUS_CURATION_REVIEWED)) {
            outList.add(Book.LABEL_CURATION_STATUS_CURATION_REVIEWED);
        }


        if (0 != (status & aBook.CURATION_STATUS_QAED)) {
            outList.add(Book.LABEL_CURATION_STATUS_QAED);
        }


        if (0 != (status & aBook.CURATION_STATUS_CHECKED_OUT)) {
            outList.add(Book.LABEL_CURATION_STATUS_CHECKED_OUT);
        }

        if (0 != (status & aBook.CURATION_STATUS_PARTIALLY_CURATED)) {
            outList.add(Book.LABEL_CURATION_STATUS_PARTIALLY_CURATED);
        }

        if (0 != (status & aBook.CURATION_STATUS_UNKNOWN)) {
            outList.add(Book.LABEL_CURATION_STATUS_UNKNOWN);
        }
        return outList;
    }
    
    
    public static void main (String args[]) {
        System.out.println(BookListUtil.searchBooks(null, "dev_3_panther_upl", "11", WSConstants.SEARCH_TYPE_STR_UNCURATED_BOOKS));
    }
    
    

    
    
}
