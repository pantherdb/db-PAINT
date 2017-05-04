package com.usc.panther.paintServer.webservices;

import com.sri.panther.paintServer.util.ConfigFile;


public class WSConstants {

    public static final String STANDARD_DECODER = "UTF-8";
    public static final String PROPERTY_DB_STANDARD = ConfigFile.getProperty("db.jdbc.dbsid");
    public static final String PROPERTY_CLS_VERSION = ConfigFile.getProperty("cls_version_sid");
    
    public static final String SEARCH_PARAMETER_BOOKS_SEARCH_VALUE = "searchValue";
    public static final String SEARCH_PARAMETER_BOOKS_SEARCH_DATABASE = "database";
    public static final String SEARCH_PARAMETER_BOOKS_SEARCH_VERSION = "version";
    public static final String SEARCH_PARAMETER_BOOKS_SEARCH_TYPE = "searchType";

    public static final String SEARCH_TYPE_STR_GENE_SYMBOL = "SEARCH_TYPE_GENE_SYMBOL";
    public static final String SEARCH_TYPE_STR_GENE_ID = "SEARCH_TYPE_GENE_ID";
    public static final String SEARCH_TYPE_STR_PROTEIN_ID = "SEARCH_TYPE_PROTEIN_ID";
    public static final String SEARCH_TYPE_STR_DEFINITION = "SEARCH_TYPE_DEFINITION";
    public static final String SEARCH_TYPE_STR_ALL_BOOKS = "SEARCH_TYPE_ALL_BOOKS";
    public static final String SEARCH_TYPE_STR_UNCURATED_BOOKS = "SEARCH_TYPE_UNCURATED_BOOKS";
    public static final String SEARCH_TYPE_STR_BOOKS_WITH_EXP_EVIDENCE = "SEARCH_TYPE_BOOKS_WITH_EXPERIMENTAL_EVIDENCE";
    
    
    
    public static final String SEARCH_PARAMETER_ANNOTATION_NODE_BOOK = "book";
    public static final String SEARCH_PARAMETER_ANNOTATION_NODE_DATABASE = "database";
    public static final String SEARCH_PARAMETER_ANNOTATION_NODE_VERSION = "version";
    public static final String SEARCH_PARAMETER_ANNOTATION_NODE_TYPE = "searchType";
    
    public static final String SEARCH_TYPE_ANNOTATION_NODE_INFO = "SEARCH_TYPE_ANNOTATION_NODE_INFO";
    public static final String SEARCH_TYPE_ANNOTATION_NODE_ANNOTATION_SF = "SEARCH_TYPE_ANNOTATION_NODE_SF";
    public static final String SEARCH_TYPE_ANNOTATION_NODE_ANNOTATION_SEQ_GO = "SEARCH_TYPE_ANNOTATION_SEQ_GO";
    public static final String SEARCH_TYPE_ANNOTATION_NODE_ANNOTATION_PANTHER = "SEARCH_TYPE_ANNOTATION_NODE_PANTHER";
    public static final String SEARCH_TYPE_ANNOTATION_NODE_GENE_INFO = "SEARCH_TYPE_ANNOTATION_NODE_GENE_INFO";
    public static final String SEARCH_TYPE_ANNOTATION_NODE_IDENTIFIER = "SEARCH_TYPE_ANNOTATION_NODE_IDENTIFIER";
    public static final String SEARCH_TYPE_ANNOTATION_NODE_PAINT = "SEARCH_TYPE_ANNOTATION_NODE_PAINT";
    
    public static final String SEARCH_TYPE_FAMILY_GENERAL = "SEARCH_TYPE_FAMILY_GENERAL";
    public static final String SEARCH_TYPE_FAMILY_GENERAL_FROM_NODE = "SEARCH_TYPE_FAMILY_GENERAL_FROM_NODE";    
    public static final String SEARCH_TYPE_FAMILY_LOOKUP_NODE_ID = "SEARCH_TYPE_FAMILY_LOOKUP_NODE_ID";
    public static final String SEARCH_TYPE_FAMILY_ID = "SEARCH_TYPE_FAMILY_ID";
    public static final String SEARCH_TYPE_FAMILY_NAMES_LOOKUP = "SEARCH_TYPE_FAMILY_NAMES_LOOKUP";
    public static final String SEARCH_TYPE_FAMILY_PHYLOXML = "SEARCH_TYPE_PHYLOXML";
    public static final String SEARCH_TYPE_FAMILY_DIRECT_ANNOTATIONS = "SEARCH_TYPE_FAMILY_DIRECT_ANNOTATIONS";
    public static final String SEARCH_TYPE_FAMILY_STRUCTURE_DIRECT_ANNOTATIONS = "SEARCH_TYPE_FAMILY_STRUCTURE_DIRECT_ANNOTATIONS";
    public static final String SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS = "SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS";    
    public static final String SEARCH_TYPE_FAMILY_COMMENT = "SEARCH_TYPE_FAMILY_COMMENT";
    
    public static final String SEARCH_TYPE_TREE = "SEARCH_TYPE_TREE";
    public static final String SEARCH_TYPE_MSA_INFO = "SEARCH_TYPE_MSA_INFO";
    
    public static final String STR_EMPTY = "";
    public static final String STR_COMMA = ",";
    

        
}
