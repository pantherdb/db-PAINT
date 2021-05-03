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
package edu.usc.ksom.pm.panther.paintServer.services;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;


public class ServiceUtils {
    public static final String OUTPUT_TYPE_JSON = "application/json";
    public static final String OUTPUT_TYPE_XML = "application/xml";
    public static final String ENCODING_UTF_8 = "UTF-8";
    public static final int PRETTY_PRINT_INDENT_FACTOR = 4;    
    
    public static final String FORMAT_OUTPUT_XML = "xml";
    public static final String FORMAT_OUTPUT_JSON = "json";

    public static final String ELEMENT_SEARCH = "search";
    public static final String ELEMENT_SEARCH_TYPE = "type";
    public static final String ELEMENT_ERROR = "error";    
    
    public static void outputDocument(Document doc, String format, HttpServletResponse response) throws Exception {
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(ENCODING_UTF_8);
            if (FORMAT_OUTPUT_XML.equalsIgnoreCase(format)) {
                response.setContentType(OUTPUT_TYPE_XML);
                DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
                LSSerializer lsSerializer = domImplementation.createLSSerializer();
                PrintWriter pw = response.getWriter();
                pw.write(lsSerializer.writeToString(doc));
                pw.flush();
                pw.close();
            }
            else if (FORMAT_OUTPUT_JSON.equalsIgnoreCase(format)) {
                response.setContentType(OUTPUT_TYPE_JSON);                                               
                DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
                LSSerializer lsSerializer = domImplementation.createLSSerializer();
                String xmlStr = lsSerializer.writeToString(doc);
                JSONObject xmlJSONObj = XML.toJSONObject(xmlStr);
                String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
                PrintWriter pw = response.getWriter();
                pw.print(jsonPrettyPrintString);
                pw.flush();
                pw.close();               
            }            
        }
        catch (Exception e) {
            throw e;
        }
    } 
    
    public static boolean outputErrorMsg(String message, String format, String searchTypeStr, HttpServletResponse response) {
        if (null == response || null == message || null == searchTypeStr) {
            return false;
        }

        try {        
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  
         
            Element root = doc.createElement(ELEMENT_SEARCH);
            doc.appendChild(root);
            
            Element searchType = doc.createElement(ELEMENT_SEARCH_TYPE);
            root.appendChild(searchType);
            searchType.appendChild(doc.createTextNode(searchTypeStr));
            
            Element error = doc.createElement(ELEMENT_ERROR);
            root.appendChild(error);
            error.appendChild(doc.createTextNode(message));
            
            outputDocument(doc, format, response);
            return true;
 
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
        catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean outputString(String content, String format, HttpServletResponse response) throws Exception {
        if (null == content || null == format || null == response) {
            return false;
        }
        try {
            response.setStatus(HttpServletResponse.SC_OK);
            if (FORMAT_OUTPUT_JSON.equals(format)) {
                response.setContentType(OUTPUT_TYPE_JSON);
            }
            else if (FORMAT_OUTPUT_XML.equals(format)) {
                response.setContentType(OUTPUT_TYPE_XML);
                
            }

            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.print(content);
            outputStream.flush();
            outputStream.close();
            return true;

        } catch (Exception e) {
            throw e;
        }
    }
        

    public static void appendElement(Document doc, Element parent, String elementTag, String text) {
        Element elem = doc.createElement(elementTag);
        parent.appendChild(elem);
        elem.appendChild(doc.createTextNode(text));
    }       

}

