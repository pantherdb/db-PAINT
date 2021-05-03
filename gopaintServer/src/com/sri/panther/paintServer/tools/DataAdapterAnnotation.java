/**
 * Copyright 2020 University Of Southern California
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
package com.sri.panther.paintServer.tools;

import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.logic.CategoryLogic;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.IWith;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.WithEvidence;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class DataAdapterAnnotation {
    
    public static final String ELEMENT_SEARCH = "search";
    public static final String ELEMENT_PARAMETERS = "parameters";
    
    
    private static final String ELEMENT_ANNOTATION_LIST = "annotation_list";
    private static final String ELEMENT_ANNOTATION = "annotation";
    private static final String ELEMENT_TERM = "term";
    private static final String ELEMENT_TERM_NAME = "term_name";
    private static final String ELEMENT_TERM_ASPECT = "term_aspect";    
    private static final String ELEMENT_QUALIFIER_LIST = "qualifier_list";
    private static final String ELEMENT_QUALIFIER = "qualifier";
    private static final String ELEMENT_EVIDENCE_LIST = "evidence_list";
    private static final String ELEMENT_EVIDENCE = "evidence";
    private static final String ELEMENT_EVIDENCE_TYPE = "evidence_type";    
    private static final String ELEMENT_EVIDENCE_VALUE = "evidence_value";
    private static final String ELEMENT_EVIDENCE_CODE = "evidence_code";
    private static final String ELEMENT_VERSION = "VERSION";
    private static final String ELEMENT_VERSION_PANTHER = "VERSION_PANTHER";
    private static final String ELEMENT_RELEASE_DATE_PANTHER = "RELEASE_DATE_PANTHER";    
    private static final String ELEMENT_VERSION_GO = "VERSION_GO";
    private static final String ELEMENT_RELEASE_DATE_GO = "RELEASE_DATE_GO";    
    
    
    public static final String WHITE_SPACE = "\\s+";
    public static final String UNDERSCORE = "_";
    public static final CategoryLogic cl = CategoryLogic.getInstance();     
    public static final GOTermHelper goTermHelper = cl.getGOTermHelper();
   
    public static Document getGOAnnotationsForNode(HashMap<String, String> parameterLookup, String publicNodeIdParam) {
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        ArrayList<Annotation> annotList = dataIO.getAllGOAnnotationsForNode(DataIO.CUR_CLASSIFICATION_VERSION_SID, parameterLookup.get(publicNodeIdParam));
        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            // Search parameters and time
            if (false == parameterLookup.isEmpty()) {
                Element parameters = doc.createElement(ELEMENT_PARAMETERS);
                root.appendChild(parameters);
                for (Entry<String, String> entry: parameterLookup.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    key.replaceAll(WHITE_SPACE, UNDERSCORE);
                    Element param = doc.createElement(key);
                    param.appendChild(doc.createTextNode(value));
                    parameters.appendChild(param);
                }
                
            }
            
            Element version = doc.createElement(ELEMENT_VERSION);
            root.appendChild(version);
            // PANTHER VERSION
            String pantherVersion = cl.getReleaseInfo(CategoryLogic.RELEASE_VERSION_PANTHER);
            if (null != pantherVersion) {
                Element pantherVersionElem = doc.createElement(ELEMENT_VERSION_PANTHER);
                version.appendChild(pantherVersionElem);
                pantherVersionElem.appendChild(doc.createTextNode(pantherVersion));
                String pantherRelDate = cl.getReleaseInfo(CategoryLogic.RELEASE_DATE_PANTHER);
                if (null != pantherRelDate) {
                    Element relDateElem = doc.createElement(ELEMENT_RELEASE_DATE_PANTHER);
                    version.appendChild(relDateElem);
                    relDateElem.appendChild(doc.createTextNode(pantherRelDate));
                }
            }
            // GO VERSION
            String goVersion = cl.getReleaseInfo(CategoryLogic.RELEASE_VERSION_GO);
            if (null != goVersion) {
                Element goVersionElem = doc.createElement(ELEMENT_VERSION_GO);
                version.appendChild(goVersionElem);
                goVersionElem.appendChild(doc.createTextNode(goVersion));
                String goRelDate = cl.getReleaseInfo(CategoryLogic.RELEASE_DATE_GO);
                if (null != goRelDate) {
                    Element relDateElem = doc.createElement(ELEMENT_RELEASE_DATE_GO);
                    version.appendChild(relDateElem);
                    relDateElem.appendChild(doc.createTextNode(goRelDate));
                }
            }
            if (null != annotList && 0 != annotList.size()) {
                Element annotationList = doc.createElement(ELEMENT_ANNOTATION_LIST);
                root.appendChild(annotationList);
                for (Annotation a: annotList) {
                    Element annotElem = doc.createElement(ELEMENT_ANNOTATION);
                    annotationList.appendChild(annotElem);
                    Element term = doc.createElement(ELEMENT_TERM);
                    annotElem.appendChild(term);
                    String termText = a.getGoTerm();
                    term.appendChild(doc.createTextNode(termText));
                    GOTerm gTerm = goTermHelper.getTerm(termText);
                    if (null != gTerm) {
                        String name = gTerm.getName();
                        if (null != name) {
                            Element eName = doc.createElement(ELEMENT_TERM_NAME);
                            annotElem.appendChild(eName);
                            eName.appendChild(doc.createTextNode(name));
                        }
                        String aspect = gTerm.getAspect();
                        if (null != aspect) {
                            Element eAspect = doc.createElement(ELEMENT_TERM_ASPECT);
                            annotElem.appendChild(eAspect);
                            eAspect.appendChild(doc.createTextNode(aspect));
                        }
                    }
                    HashSet<Qualifier> qSet = a.getQualifierSet();
                    if (null != qSet) {
                        Element qualList = doc.createElement(ELEMENT_QUALIFIER_LIST);
                        annotElem.appendChild(qualList);
                        for (Qualifier q: qSet) {
                            Element qualifier = doc.createElement(ELEMENT_QUALIFIER);
                            qualList.appendChild(qualifier);
                            qualifier.appendChild(doc.createTextNode(q.getText()));
                        }
                    }
                    AnnotationDetail ad = a.getAnnotationDetail();
                    HashSet<WithEvidence> withSet = ad.getWithEvidenceDBRefSet();
                    if (null != withSet) {
                        Element withList = doc.createElement(ELEMENT_EVIDENCE_LIST);
                        annotElem.appendChild(withList);
                        for (WithEvidence we : withSet) {
                            IWith with = we.getWith();
                            if (with instanceof DBReference) {
                                Element evidence = doc.createElement(ELEMENT_EVIDENCE);
                                withList.appendChild(evidence);
                                Element code = doc.createElement(ELEMENT_EVIDENCE_CODE);
                                evidence.appendChild(code);
                                code.appendChild(doc.createTextNode(we.getEvidenceCode()));

                                Element type = doc.createElement(ELEMENT_EVIDENCE_TYPE);
                                evidence.appendChild(type);
                                type.appendChild(doc.createTextNode(((DBReference) with).getEvidenceType()));

                                Element value = doc.createElement(ELEMENT_EVIDENCE_VALUE);
                                evidence.appendChild(value);
                                value.appendChild(doc.createTextNode(((DBReference) with).getEvidenceValue()));
                            }             
                        }
                    }
                }
            }
            return doc;
        }
        catch(Exception e) {
             e.printStackTrace();
             return null;
        }
    }
}
