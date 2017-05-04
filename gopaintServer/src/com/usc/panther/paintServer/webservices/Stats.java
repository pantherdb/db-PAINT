/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.usc.panther.paintServer.webservices;

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.util.ConfigFile;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author muruganu
 */
public class Stats {
    public static final String CLASSIFICATION_VERSION_SID = ConfigFile.getProperty(ConfigFile.PROPERTY_CLASSIFICATION_VERSION_SID);
    private static DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
    
    public static int getNumBooks() {
        ArrayList<Book> books = dataIO.getAllBooks(CLASSIFICATION_VERSION_SID);
        if (null == books) {
            return 0;
        }
        return books.size();
    }
    
    public Hashtable<String, Book> getBookLookup() {
        return dataIO.getListOfBooksAndStatus(CLASSIFICATION_VERSION_SID);
    }
    
    public static int getManuallyCuratedBooks() {
        Hashtable<String, Book> bookLookup = dataIO.getListOfBooksAndStatus(CLASSIFICATION_VERSION_SID);
        int counter = 0;
        for (Book book: bookLookup.values()) {
            if (Book.CURATION_STATUS_MANUALLY_CURATED == book.getCurationStatus()) {
                counter++;
            }
        }
        return counter;
    }
    
    public static int getPartiallyCuratedBooks() {
        Hashtable<String, Book> bookLookup = dataIO.getListOfBooksAndStatus(CLASSIFICATION_VERSION_SID);
        int counter = 0;
        for (Book book: bookLookup.values()) {
            if (Book.CURATION_STATUS_PARTIALLY_CURATED == book.getCurationStatus()) {
                counter++;
            }
        }
        return counter;
    }    
}
