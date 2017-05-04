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
package com.sri.panther.paintCommon;




import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.sri.panther.paintCommon.util.ReadResources;
import com.sri.panther.paintCommon.util.Utils;


public class FixedInfo implements Serializable {
 	/**
	 * 
	 */
	private static final long serialVersionUID = 4968764874968361727L;
	protected String seqColName;
    protected String subfamPrefix;
    protected int origSeqColIndex;
    protected String subfamilyAnnotName;
    protected String commentColName;
    protected String evidenceColName;
    protected String geneIdColumnName;
    protected String geneSymbolColumnName;
    protected String GOAnnotationColumnName;
    protected String GOInferenceColumnName;
    protected String orthoMCLColumnName;
    protected String pantherAnnotColumnName, pantherNewAnnotColumnName;
    protected Vector classificationColNames;

//    protected GoSlimManager goSlimManager;      // Currently not used
    protected Classification clsRoot;              // Panther classification information

    // Members associated with URL links
    protected Hashtable columnToLinksTable;
    protected Hashtable linksTable;
    protected Hashtable specialOrgs;

    // Members associated with UPL version
    // Hashtable of hashtables.  First hashtable level contains database names to upl hashtable.  Second
    // level contains upl version number to vector of upl version string and release date.
    protected Hashtable dbToUPLInfo;


    protected static final String pairs_delim = ",";
    protected static final String delim = ",";
    protected transient static Logger log = Logger.getLogger(FixedInfo.class.getName());
    protected static final String GENE_ID = "gene_id";
    protected static final String GENE_SYMBOL = "gene_symbol";
    protected static final String STR_EMPTY = "";
    protected static final String GO_ANNOT = "go_annot";
    protected static final String GO_INFERENCE = "go_infer";
    protected static final String ORTHO_MCL = "orthoMCL";
    protected static final String PROPERTY_ANNOT_INFO = "annotation_info";
    protected static final String PROPERTY_ANNOT_NEW = "annotation_new";
    
    
    public  static final String PROPERTY_FILE_TREE = "treeViewer";
    
   

    public FixedInfo(ReadResources rr) throws Exception {
        if (null == rr) {
            return;
        }
        
        String key = STR_EMPTY;
        String[] values;
        try {
            key = rr.getKey("seq_id");
            if ((null != key) && (0 != key.length())) {
                // Remove quotes
                key = key.substring(1, key.length() - 1);
                values = Utils.tokenize(key, pairs_delim);
                seqColName = values[0];
                origSeqColIndex = Integer.parseInt(values[1]);
            }
            key = rr.getKey("subfamiy_annot");
            if ((null != key) && (0 != key.length())) {
                // Remove quotes
                key = key.substring(1, key.length() - 1);
                subfamilyAnnotName = key;
            }
            key = rr.getKey("classification_info");
            if ((null != key) && (0 != key.length())) {
                // Remove quotes
                key = key.substring(1, key.length() - 1);
                values = Utils.tokenize(key, pairs_delim);
                classificationColNames = new Vector();
                for (int i = 0; i < values.length; i++) {
                    classificationColNames.addElement(values[i]);
                }
            }
            key = rr.getKey("comment");
            if ((null != key) && (0 != key.length())) {
                // Remove quotes
                key = key.substring(1, key.length() - 1);
                commentColName = key;
            }
            key = rr.getKey("evidence");
            if ((null != key) && (0 != key.length())) {
                // Remove quotes
                key = key.substring(1, key.length() - 1);
                evidenceColName = key;
            }
            key = rr.getKey("subfam_prefix");
            if ((null != key) && (0 != key.length())) {
                // Remove quotes
                key = key.substring(1, key.length() - 1);
                subfamPrefix = key;
            }
            
            key = rr.getKey(GENE_ID);
            if (null != key) {
                int length = key.length();
                if (0 != length) {
                    //Remove quotes
                    geneIdColumnName = key.substring(1, length - 1);
                }
            }
                
            
            key = rr.getKey(GENE_SYMBOL);
            if (null != key) {
                int length = key.length();
                if (0 != length) {
                    //Remove quotes
                    geneSymbolColumnName = key.substring(1, length - 1);
                }
             }
            
            
            key = rr.getKey(GO_ANNOT);
            if (null != key){
                int length = key.length();
                if (2 < length) {
                //Remove quotes
                    GOAnnotationColumnName = key.substring(1, length - 1);
                }
            }
            
            
            key = rr.getKey(GO_INFERENCE);
            if (null != key) {
                int length = key.length();
                if (2 < length) {
                //Remove quotes
                    GOInferenceColumnName = key.substring(1, length - 1);
                }
            }
            
            
            key = rr.getKey(ORTHO_MCL);
            if (null != key) {
                int length = key.length();
                if (2 < length) {
                //Remove quotes
                    orthoMCLColumnName = key.substring(1, length - 1);
                }
            }
            
            
            key = rr.getKey(PROPERTY_ANNOT_INFO);
            if (null != key) {
                int length = key.length();
                if (2 < length) {
                //Remove quotes
                    pantherAnnotColumnName = key.substring(1, length - 1);
                }
            }
            key = rr.getKey(PROPERTY_ANNOT_NEW);
            if (null != key) {
                int length = key.length();
                if (2 < length) {
                //Remove quotes
                    pantherNewAnnotColumnName = key.substring(1, length - 1);
                }
            }
            

            // Get the url information
            saveURLInfo(rr);


        } catch (Exception e) {
            log.error("Error reading fixed property after value:  " + key);

            throw e;
        }
    }

    protected void saveURLInfo(ReadResources rr) throws Exception {
        String key = "";
        String[] values;
        try {
            // Deal with URL's now
            // No links for some entries in organism column.
            specialOrgs = new Hashtable();
            key = rr.getKey("sp_org");
            // Remove quotes
            key = key.substring(1, key.length() - 1);
            values = Utils.tokenize(key, delim);
            for (int i = 0; i < values.length; i++) {
                specialOrgs.put(values[i], values[i]);
            }

            // Get applicable list of links and add to hashtable
            Hashtable urlTable = new Hashtable();
            key = rr.getKey("url_list");
            if (null == key) {
                return;
            }

            String urlTypeValues[] = Utils.tokenize(key, delim);
            for (int i = 0; i < urlTypeValues.length; i++) {
                urlTable.put(urlTypeValues[i], urlTypeValues[i]);
            }

            // Add list of special organisms to table
            key = rr.getKey("sp_org");
            values = Utils.tokenize(key, delim);
            for (int i = 0; i < values.length; i++) {
                urlTable.put(values[i], values[i]);
            }

            String supportedTypes = rr.getKey("supported_types");
            String supValues[] = Utils.tokenize(supportedTypes, delim);


            // Initialize tables
            columnToLinksTable = new Hashtable();
            linksTable = new Hashtable();
            for (int i = 0; i < supValues.length; i++) {
                key = rr.getKey(supValues[i]);
                Vector urlValues = new Vector();
                values = Utils.tokenize(key, delim);
                for (int j = 0; j < values.length; j++) {
                    if (null != urlTable.get(values[j])) {
                        urlValues.addElement(values[j]);
                        key = rr.getKey(values[j]);
                        linksTable.put(values[j], key);
                    }
                }
                if (false == urlValues.isEmpty()) {
                    columnToLinksTable.put(supValues[i], urlValues);
                }
            }

        } catch (Exception e) {

            log.error("Error reading fixed property after value:  " + key);

            throw e;
        }
    }

    public String getSeqColName() {
        return seqColName;
    }

    public int getOrigSeqColIndex() {
        return origSeqColIndex;
    }

    public String getSubfamilyAnnotColName() {
        return subfamilyAnnotName;
    }


    public String getCommentColName() {
        return commentColName;
    }

    public String getEvidenceColName() {
        return evidenceColName;
    }
    
    public String getGeneIdColumnName() {
        return geneIdColumnName;
    }
    
    public String getGeneSymbolColumnName() {
        return geneSymbolColumnName;
    }

    public String getSubfamPrefix() {
        return subfamPrefix;
    }

    public Vector getClassification_col_names() {
        return classificationColNames;
    }

//    public void setGOSlimManager(GoSlimManager goSlimManager) {
//        this.goSlimManager = goSlimManager;
//    }
//
//    public GoSlimManager getGOSlimMananger() {
//        return goSlimManager;
//    }
    
    public void setClsRoot(Classification root) {
        clsRoot = root;
    }
    
    public Classification getClsRoot() {
        return clsRoot;
    }

    public Hashtable getColumnToLinksTable() {
        return columnToLinksTable;
    }

    public Hashtable getLinksTable() {
        return linksTable;
    }

    public Hashtable getSpecialOrgsTable() {
        return specialOrgs;
    }

    public void setDbToUPLInfo(Hashtable ht) {
        dbToUPLInfo = ht;
    }

    public Hashtable getDbToUploadInfo() {
        return dbToUPLInfo;
    }
    
    public String getGOAnnotationColName() {
        return GOAnnotationColumnName;
    }
    
    public String getGOInferenceColName() {
        return GOInferenceColumnName;
    }
    
    public String getOrthoMCLColName() {
        return orthoMCLColumnName;
    }
    
    public String getPANTHERAnnotColName() {
        return pantherAnnotColumnName;
    }
    
    public String getNewPANTHERAnnotColName() {
        return pantherNewAnnotColumnName;
    }

    public static String getDb(String dbUplVersion) {
        String dbUplInfo[] = Utils.tokenize(dbUplVersion, "|");
        if (2 != dbUplInfo.length) {
            return null;
        } else
            return dbUplInfo[0];
    }

    public static String getCls(String dbUplVersion) {
        String dbUplInfo[] = Utils.tokenize(dbUplVersion, "|");
        if (2 != dbUplInfo.length) {
            return null;
        } else
            return dbUplInfo[1];
    }


    public boolean UPLReleased(String dbUplVersion) {
        if ((null == dbToUPLInfo) || (null == dbUplVersion)) {
            return false;
        }
        // Get database and upl information user is interested in
        String dbUplInfo[] = Utils.tokenize(dbUplVersion, "|");
        if (2 != dbUplInfo.length) {
            return false;
        }
        Enumeration dbs = dbToUPLInfo.keys();
        while (dbs.hasMoreElements()) {
            String key = (String)dbs.nextElement();
            if (0 != key.compareTo(dbUplInfo[0])) {
                continue;
            }
            Hashtable uplToUPLInfo = (Hashtable)dbToUPLInfo.get(key);
            Enumeration e = uplToUPLInfo.keys();
            while (e.hasMoreElements()) {
                String upl = (String)e.nextElement();
                Vector info = (Vector)uplToUPLInfo.get(upl);
                if (0 == dbUplInfo[0].compareTo((String)info.elementAt(0))) {
                    if (null == info.elementAt(1)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Integer getClsVersion(String dbUplVersion) {
        if ((null == dbToUPLInfo) || (null == dbUplVersion)) {
            return null;
        }

        // Get database and upl information user is interested in
        String dbUplInfo[] = Utils.tokenize(dbUplVersion, "|");
        if (2 != dbUplInfo.length) {
            return null;
        }
        Enumeration dbs = dbToUPLInfo.keys();
        while (dbs.hasMoreElements()) {
            String key = (String)dbs.nextElement();
            if (0 != key.compareTo(dbUplInfo[0])) {
                continue;
            }
            Hashtable uplToUPLInfo = (Hashtable)dbToUPLInfo.get(key);
            Enumeration e = uplToUPLInfo.keys();
            while (e.hasMoreElements()) {
                String clsId = (String)e.nextElement();
                Vector info = (Vector)uplToUPLInfo.get(clsId);
                if (0 == dbUplInfo[1].compareTo((String)info.elementAt(0))) {
                    return new Integer(clsId);
                }
            }
        }
        return null;
    }

    public String getClsLabel(String dbUpl) {
        if ((null == dbToUPLInfo) || (null == dbUpl)) {
            return null;
        }

        // Get database and upl information user is interested in
        String dbUplInfo[] = Utils.tokenize(dbUpl, "|");
        if (2 != dbUplInfo.length) {
            return null;
        }
        Enumeration dbs = dbToUPLInfo.keys();
        while (dbs.hasMoreElements()) {
            String key = (String)dbs.nextElement();
            if (0 != key.compareTo(dbUplInfo[0])) {
                continue;
            }
            Hashtable uplToUPLInfo = (Hashtable)dbToUPLInfo.get(key);
            Enumeration clsIds = uplToUPLInfo.keys();
            while (clsIds.hasMoreElements()) {
                String clsId = (String)clsIds.nextElement();
                Vector uplInfo = (Vector)uplToUPLInfo.get(clsId);
                if (0 ==
                    ((String)uplInfo.elementAt(0)).compareTo(dbUplInfo[1])) {
                    return clsId;
                }
            }
        }
        return null;
    }

    public String getClsVersion(String db, String clsId) {
        if (null == dbToUPLInfo) {
            return null;
        }
        Hashtable clsTbl = (Hashtable)dbToUPLInfo.get(db);
        if (null == clsTbl) {
            return null;
        }
        Vector clsInfo = (Vector)clsTbl.get(clsId);
        if (null == clsInfo) {
            return null;
        }
        return (String)clsInfo.elementAt(0);
    }

    public String getDbUpl(String db, String clsId) {
        String clsVersion = getClsVersion(db, clsId);
        if (null == clsVersion) {
            return null;
        }
        return db + "|" + clsVersion;
    }

    public boolean dbUploadValid(String dbUpl) {
        if (null == getClsLabel(dbUpl)) {
            return false;
        }
        return true;
    }

    public String getDbClsId(String dbUplVersion) {
        if (null == dbUplVersion) {
            return null;
        }
        Integer clsId = getClsVersion(dbUplVersion);
        if (null != clsId) {
            return getDb(dbUplVersion) + "|" + clsId.toString();
        }
        return null;
    }


    public String combineDBAndClsId(String db, String clsId) {
        return db + "|" + clsId;
    }
    
    
    public static FixedInfo createFixedInfo() {
        ReadResources rr = new ReadResources();
        try {
            rr.setResource(PROPERTY_FILE_TREE);
            return new FixedInfo(rr);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
            
        }
        
    }
    



    public static void main(String[] args) {
        FixedInfo.createFixedInfo();
    }


}
