/**
 * Copyright 2019 University Of Southern California
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

import com.sri.panther.paintCommon.Book;
import com.sri.panther.paintCommon.util.FileUtils;
import com.sri.panther.paintServer.database.DataIO;
import com.sri.panther.paintServer.util.ConfigFile;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.IWith;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintServer.servlet.DataServlet;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;




public class AnnotUtil {
    
    private static final String PROPERTY_USER_SERVER = "server_usr_info";
    private static final String PROPERTY_SERVLET_USER_INFO = "server_usr_info_check";
    private static final String ACTION_VERIFY_USER = "VerifyUserInfo";    
    
    public static final String SUFFIX_ERR_PAINT = "_paint_err.txt";
    public static final String SUFFIX_ERR_OTHER = "_other_err.txt";    
    
    public static void outputAnnotInfo(String directory, ArrayList<Book>books, int start, int end) {
        System.out.println("Going to write information into directory " + directory);
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        for (int i = start; i < end; i++) {
            Book b = books.get(i);
            String id = b.getId();
            System.out.println("Processing " + id + " index " + (i + 1) + " of " + end);
            File dir = new File(directory + File.separator + id);
            dir.mkdirs();
            String paintErrName = dir + File.separator + id + SUFFIX_ERR_PAINT;
            String otherErrName = dir + File.separator + id + SUFFIX_ERR_OTHER;
            
            HashMap<String, Node> treeNodeLookup = new HashMap<String, Node>();
            dataIO.getAnnotationNodeLookup(id, DataServlet.CLASSIFICATION_VERSION_SID, treeNodeLookup);
            HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>> annotToPosWithLookup = new HashMap<edu.usc.ksom.pm.panther.paintCommon.Annotation, ArrayList<IWith>>();

            StringBuffer errorBuf = new StringBuffer();
            StringBuffer paintErrBuf = new StringBuffer();
            HashSet<Annotation> removeSet = new HashSet<Annotation>();
            HashSet<String> modifySet = new HashSet<String>();
            HashSet<Annotation> removedFromGOAnnot = new HashSet<Annotation>();
            try {
                HashSet<Annotation> addedAnnotSet = new HashSet<Annotation>();
                dataIO.getFullGOAnnotations(id, DataServlet.CLASSIFICATION_VERSION_SID, treeNodeLookup, annotToPosWithLookup, errorBuf, paintErrBuf, removeSet, modifySet, addedAnnotSet, removedFromGOAnnot, false);
                if (errorBuf.length() > 0) {
                    FileUtils.writeBufferToFile(otherErrName, errorBuf);
                }
                if (paintErrBuf.length() > 0) {
                    FileUtils.writeBufferToFile(paintErrName, paintErrBuf);
                }
            } 
            catch(Exception e) {
                System.out.println("Error processing book " + id);
            }
        }
    }
    
    public static void main(String argx[]) throws Exception {
        String args[] = new String[] {"dsfds", "dsfsddfs", "C:\\library\\error_info", "1", "10" };        
        DataIO dataIO = new DataIO(ConfigFile.getProperty(ConfigFile.KEY_DB_JDBC_DBSID));
        ArrayList<Book> books = dataIO.getAllBooks(DataServlet.CLASSIFICATION_VERSION_SID);        
        System.out.println("Please specify parameters as follows");
        System.out.println("First parameter is the username");
        System.out.println("Second parameter is the password associated with the user name specified by the first parameter");        
        System.out.println("Third parameter is the output directory");
        System.out.println("Fourth parameter is the index of the book to start.  Has to be 1 or above");
        System.out.println("Fifth pamameter is the index of the book to end. Has to be less than or equal to " + books.size());
        

        
        if (args.length < 5) {
            System.out.println("Invalid number of parameters specified");
            System.exit(-1);
        }
        

        
        String directory = canWriteIntoDirectory(args[2]);
        if (null == directory) {
            System.out.println("Cannot write into directory " + args[2]);
            System.exit(-1);
        }
                
        int start = -1;
        int end = -1;
        try {
            start = Integer.parseInt(args[3]);
            end = Integer.parseInt(args[4]);
        }
        catch(NumberFormatException nfe) {
            System.out.println(args[3] + " and " + args[4] + " have to be integers between 1 and " + books.size() + " inclusive");
            System.exit(-1);
        }
        
        AnnotUtil.outputAnnotInfo(directory, books, start - 1, end);
        
        
    }
    
    private static String canWriteIntoDirectory(String dir) {
        File f = new File(dir);
        if (f.canWrite()) {
            if (false == f.isDirectory()) {
                return f.getAbsoluteFile().getParentFile().getAbsolutePath();
            }
            else {
                return f.getAbsolutePath();
            }
        } 
        return null;
    }
    

}
