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
package edu.usc.ksom.pm.panther.paintServer.logic;


// Temporarily disable retrieving items from saved structure
import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintServer.database.DataIO;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationNode;
import edu.usc.ksom.pm.panther.paintCommon.IWith;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet;
import edu.usc.ksom.pm.panther.paintServer.webservices.FamilyUtil;
import edu.usc.ksom.pm.panther.paintServer.webservices.MSAUtil;
import edu.usc.ksom.pm.panther.paintServer.webservices.TreeLogic;
import edu.usc.ksom.pm.panther.paintServer.webservices.WSConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Vector;

public class BookManager {

    private static BookManager instance = null;

    private static DataIO dataIO = DataAccessManager.getInstance().getDataIO();

    private static HashSet<String> BOOKS_WOTH_EXP_LEAVES;
    private static Hashtable<String, Book> ACC_TO_BOOK_STATIC_INFO;

    private static final int MAX_FAMILY_INFO = 12;        // Store information for maximum of ~10 genomes (12 * 0.9)
    private static final float FREQUENCY = 0.9f;

    //private static volatile LFUCache<String, String> familyStrAllAnnotLookup = new LFUCache<String, String>(MAX_FAMILY_INFO, FREQUENCY);      
    public static final String STR_UNDERSCORE = "_";

    private BookManager() {

    }

    public static BookManager getInstance() {
        if (null != instance) {
            return instance;
        }
        init();
        return instance;
    }

    private static synchronized void init() {
        BOOKS_WOTH_EXP_LEAVES = dataIO.getBooksWithExpEvdnceForLeaves();
        ACC_TO_BOOK_STATIC_INFO = dataIO.getStaticInfoForAllBooks(WSConstants.PROPERTY_CLS_VERSION);
        instance = new BookManager();
    }

    public HashSet<String> getBooksWihtExpLeaves() {
        if (null == BOOKS_WOTH_EXP_LEAVES) {
            System.out.println("Initializing books with leaves again");
            BOOKS_WOTH_EXP_LEAVES = dataIO.getBooksWithExpEvdnceForLeaves();
        }
        if (null == BOOKS_WOTH_EXP_LEAVES) {
            return null;
        }
        return (HashSet<String>) BOOKS_WOTH_EXP_LEAVES.clone();
    }

    public Hashtable<String, Book> allBooksStaticInfo() {
        if (null == ACC_TO_BOOK_STATIC_INFO) {
            System.out.println("Initializing static information for all books again");
            ACC_TO_BOOK_STATIC_INFO = dataIO.getStaticInfoForAllBooks(WSConstants.PROPERTY_CLS_VERSION);
        }
        if (null == ACC_TO_BOOK_STATIC_INFO) {
            return null;
        }
        Hashtable<String, Book> rtnTbl = new Hashtable<String, Book>(ACC_TO_BOOK_STATIC_INFO.size());
        for (Entry<String, Book> accToBook : ACC_TO_BOOK_STATIC_INFO.entrySet()) {
            Book from = accToBook.getValue();
            Book b = new Book(from.getId(), from.getName(), from.getCurationStatus(), null);
            b.setNumLeaves(from.getNumLeaves());
            b.setOrgSet(from.getOrgSet());
            rtnTbl.put(accToBook.getKey(), b);
        }
        return rtnTbl;
    }

    public String getFamilyStructureAllAnnot(String family, String uplVersion, String request) {
        if (false == WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS.equals(request) && false == WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS_NO_MSA.equals(request)) {
            return null;
        }
        if (null == family || null == uplVersion) {
            return null;
        }
        String xml = null;
        if (WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS.equals(request)) {
            xml = getXmlFamilyStructureAllAnnotations(family, uplVersion, true);
        } else {
            xml = getXmlFamilyStructureAllAnnotations(family, uplVersion, false);
        }
        return xml;
//                        
//        String key = family + STR_UNDERSCORE + request;
//
//        synchronized (familyStrAllAnnotLookup) {
//            xml = familyStrAllAnnotLookup.get(key);
//        }
//        
//        if (null == xml) {
//            synchronized (familyStrAllAnnotLookup) {
//                if (WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS.equals(request)) {
//                    xml = getXmlFamilyStructureAllAnnotations(family, uplVersion, true);
//                } else {
//                    xml = getXmlFamilyStructureAllAnnotations(family, uplVersion, false);
//                }
//            
//                if (null != xml) {
//                    familyStrAllAnnotLookup.put(key, xml);
//                }
//            }
//        }
//        return xml;
    }

    public void clearFamilyStructureAllAnnotNoMSA(String family) {
//        if (null == family) {
//            return;
//        }
//        synchronized (familyStrAllAnnotLookup) {
//            String key = family + STR_UNDERSCORE + WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS;
//            familyStrAllAnnotLookup.remove(key);
//            key = family + STR_UNDERSCORE + WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS_NO_MSA;
//            familyStrAllAnnotLookup.remove(key);
//        }
    }

    private static String getXmlFamilyStructureAllAnnotations(String familyId, String uplVersion, boolean includeMSA) {
        HashMap<String, Node> nodeLookup = new HashMap<String, Node>();
        long timeStart = System.currentTimeMillis();
        TreeLogic tl = new TreeLogic();
        if (false == tl.setTreeStructure(familyId, uplVersion, nodeLookup)) {
            return FamilyUtil.getErrorDoc(WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS, FamilyUtil.MSG_INVALID_ID_SPECIFIED);
        }
        if (true == includeMSA) {
            Vector msaInfo = (Vector) DataServlet.getMSAStrs(familyId, uplVersion);
            MSAUtil.parsePIRForNonGO(msaInfo, new Vector(tl.getIdToNodeTbl().values()), true);
        }
        dataIO.getGeneInfo(familyId, uplVersion, nodeLookup);
        String familyName = null;
        try {
            familyName = dataIO.getFamilyName(familyId, uplVersion);
            dataIO.getAnnotationNodeLookup(familyId, uplVersion, nodeLookup);   // This will get all nodes in tree
        } catch (Exception e) {

        }

        Hashtable<String, AnnotationNode> nodeTbl = tl.getIdToNodeTbl();        // The key does not have the family id plus ":".  Create a copy of table with
        // key that contains familyid plus ":".  Also set the associated node structure
        Hashtable<String, AnnotationNode> accToAnnotNodeLookup = new Hashtable<String, AnnotationNode>(nodeTbl.size());
        if (null != nodeTbl) {
            for (String nodeAcc : nodeTbl.keySet()) {
                AnnotationNode an = nodeTbl.get(nodeAcc);
                accToAnnotNodeLookup.put(an.getAccession(), an);
                an.setNode(nodeLookup.get(an.getAccession()));
            }
        }

        try {
            dataIO.addPruned(familyId, uplVersion, nodeLookup);
            HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>> annotToPosWithLookup = new HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>>();
            StringBuffer errorBuf = new StringBuffer();
            StringBuffer paintErrBuf = new StringBuffer();
            HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> removeSet = new HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation>();
            HashSet<String> modifySet = new HashSet<String>();
            HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation> removedFromGOAnnot = new HashSet<edu.usc.ksom.pm.panther.paintCommon.Annotation>();
            dataIO.getFullGOAnnotations(familyId, uplVersion, nodeLookup, annotToPosWithLookup, errorBuf, paintErrBuf, removeSet, modifySet, new HashSet<Annotation>(), removedFromGOAnnot, false);
        } catch (Exception e) {

        }
        long duration = System.currentTimeMillis() - timeStart;

        return FamilyUtil.outputFamilyStructureAnnotations(tl, nodeLookup, familyId, familyName, duration, WSConstants.SEARCH_TYPE_FAMILY_STRUCTURE_ALL_ANNOTATIONS);
    }
}
