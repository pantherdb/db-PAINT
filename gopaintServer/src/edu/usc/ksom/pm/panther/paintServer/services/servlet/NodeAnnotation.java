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
package edu.usc.ksom.pm.panther.paintServer.services.servlet;

import com.sri.panther.paintServer.tools.DataAdapterAnnotation;
import edu.usc.ksom.pm.panther.paintServer.services.ServiceUtils;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;


public class NodeAnnotation extends HttpServlet {
    public static final String ID = "id";
    public static final String DESC_PARAM_ID = "Persistent id";
    public static final String PARAM_FORMAT = "format";
    private static final Pattern URL_PATH_SEPARATOR = Pattern.compile("[//]");
    
    // Path is going to be formatted as follows: /services/tree/node/annotation/go/PTN1234 - {"",services,tree,node,annotation/go,PTN1234}
    private static final int NUM_PARTS = 7;
    private static final int INDEX_ID = 6;
    private static final int INDEX_TYPE = 5;
    
    public static final String SAMPLE_LEAF_PERSISTENT_ID = "PTN001288898";
    public static final String ANNOT_TYPE_GO = "go";
    

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURL().toString();
        if (null == url) {
            request.getSession().invalidate();
            return;
        }
        URL requestUrl = new URL(url);
        String path = requestUrl.getPath();
        if (null == path) {
            request.getSession().invalidate();            
            return;
        }
        String components[] = URL_PATH_SEPARATOR.split(path);
        if (null == components || NUM_PARTS > components.length) {
            request.getSession().invalidate();            
            return;
        }
        
        if (false == ANNOT_TYPE_GO.equals(components[INDEX_TYPE])) {
            request.getSession().invalidate();            
            return;            
        }
        // Get GO annotations for node
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(ID, components[INDEX_ID]);
        Document doc = DataAdapterAnnotation.getGOAnnotationsForNode(params, ID);
        if (null == doc) {
            request.getSession().invalidate();            
            return;
        }
        try {
            String format = request.getParameter(PARAM_FORMAT);
            if (null != format && ServiceUtils.FORMAT_OUTPUT_XML.equalsIgnoreCase(format)) {
                ServiceUtils.outputDocument(doc, ServiceUtils.FORMAT_OUTPUT_XML, response);
            }
            else {
                ServiceUtils.outputDocument(doc, ServiceUtils.FORMAT_OUTPUT_JSON, response);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            request.getSession().invalidate();
        }
    }
}
