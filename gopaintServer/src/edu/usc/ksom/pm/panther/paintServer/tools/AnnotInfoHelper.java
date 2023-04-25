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

import com.sri.panther.paintCommon.util.FileUtils;
import com.sri.panther.paintCommon.util.Utils;
import com.sri.panther.paintServer.datamodel.Organism;
import com.sri.panther.paintServer.logic.OrganismManager;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class AnnotInfoHelper {
    public static final String BOOK_EXT = ".xml";
    public static final String MSG_EXT = ".txt";
    public static final String BOOK_PATTERN_STR = "PTHR[0-9]{5}" + BOOK_EXT;
    public static final Pattern BOOK_PATTERN = Pattern.compile(BOOK_PATTERN_STR);
    
    public static final String TAG_FAMILY_ANNOT_INFO_PAINT = "family_annot_info_paint";
    public static final String TAG_FAMILY_ANNOT_INFO_OTHER = "family_annot_info_other";
    public static final String TAG_MESSAGE = "message";
    public static final String DELIM_INFO = "\n";    
    
    public static final String PATTERN_PTN = "PTN[0-9]{9}";
    public static final String PATTERN_IBD = "IBD";
    public static final String PATTERN_IKR = "IKR";
    public static final String PATTERN_IRD = "IRD";
    public static final String PATTERN_TCV = "TCV";
    public static final String PATTERN_IBA = "IBA";    
    public static final String PATTERN_QUALIFIER_NOT = Qualifier.QUALIFIER_NOT;
    public static final String PATTERN_QUALIFIER_COLOCALIZES = Qualifier.QUALIFIER_COLOCALIZES_WITH;
    public static final String PATTERN_QUALIFIER_CONTRIBUTES = Qualifier.QUALIFIER_CONTRIBUTES_TO; 
    public static final String PATTERN_ID = "[0-9]+";
    public static final String PATTERN_GO_ID = "GO:[0-9]+";
    public static final String PATTERN_FULL_PANTHER_AN_ID = "PTHR[0-9]{5}:AN[0-9]+";
    
    public static final String[] PATTERN_LIST = getPatterns();
    
    public static final String PATTERN_SUBSTITUTE = "_";
    private static HashMap<String, ArrayList<String>> patternMsgLookup = new HashMap<String, ArrayList<String>>();

    public static final String DIR_INFO = "/info";    
    public static final String FILE_SEPARATOR = "/";
    
    public static final String STR_EMPTY = "";
    public static final String PATTERN_NON_ALPHANUMERIC = "[^a-zA-Z0-9]";
    public static final String STR_UNDERSCORE = "_";
    public static final String STR_WORD_BOUNDARY = "\\b";
    
    public static final String[] getPatterns() {
        ArrayList<String> patternList = new ArrayList<String>();
        patternList.add(PATTERN_PTN);
        patternList.add(PATTERN_QUALIFIER_NOT);
        patternList.add(PATTERN_QUALIFIER_COLOCALIZES);
        patternList.add(PATTERN_QUALIFIER_CONTRIBUTES);
        patternList.add(PATTERN_ID);
        patternList.add(PATTERN_GO_ID);
        patternList.add(PATTERN_FULL_PANTHER_AN_ID);
        HashSet<String> expCodes = Evidence.getExperimental();
        if (null != expCodes) {
            for (String code: expCodes)
            if (false == patternList.contains(code)) {
                patternList.add(code);
            }
        }        
        HashSet<String> paintCodes = Evidence.getPaintCodes();
        if (null != paintCodes) {
            for (String code: paintCodes)
            if (false == patternList.contains(code)) {
                patternList.add(code);
            }            
        }

        ArrayList<Organism> allOrgs = OrganismManager.getInstance().getOrgList();
        for (Organism org: allOrgs) {
            patternList.add(STR_WORD_BOUNDARY + org.getLongName() + STR_WORD_BOUNDARY);
        }
        
        String[] rtnList = new String[patternList.size()];
        patternList.toArray(rtnList);
        return rtnList;
    }
    
    public static void outputInfo(String bookDir) {
        try {
            File folder = new File(bookDir);

            File[] files = folder.listFiles();
            if (null == files) {
                return;
            }
            
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                System.out.println("Processing file " + file.getName() + " " + (i + 1) + " of " +  files.length);
                if (file.isDirectory()) {
                    continue;
                }
                String fileName = file.getName();
                if (null == fileName) {
                    continue;
                }
                if (false == fileName.endsWith(BOOK_EXT)) {
                    continue;
                }
                Matcher matcher = BOOK_PATTERN.matcher(fileName);
                if (false == matcher.matches()) {
                    continue;
                }
                processFile(file, fileName.substring(0, fileName.length() - BOOK_EXT.length()));
                
            }
            int counter = 1;
            for (Entry<String, ArrayList<String>> msgEntry : patternMsgLookup.entrySet()) {
                String key = msgEntry.getKey();
                String fileName = key.replaceAll(PATTERN_NON_ALPHANUMERIC, STR_UNDERSCORE);
                if (fileName.length() > 50) {
                    fileName = fileName.substring(0,50);
                }
                String filePrefix = bookDir + DIR_INFO + FILE_SEPARATOR + fileName + counter;
                Path tmpFileStatusPath = Paths.get(filePrefix + MSG_EXT);
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
                Files.write(statusFilePath, msgEntry.getValue(), StandardCharsets.UTF_8);
                counter++;
            }
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }
    

    private static void processFile(File f, String book) {
        String[] contents = FileUtils.readFile(f.getAbsolutePath());
        if (null == contents) {
            System.out.println("Unable to process file " + f.getAbsolutePath());
            return;
        }
        String xml = String.join(STR_EMPTY, contents);
        
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));

            // normalize text representation
            doc.getDocumentElement().normalize();
            String tags[] = {TAG_MESSAGE};
            for (String tag: tags) {
                NodeList infoList = doc.getElementsByTagName(tag);
                if (null == infoList || 0 == infoList.getLength()) {
                    continue;
                }
                
                for (int i = 0; i < infoList.getLength(); i++) {
                    Node n = infoList.item(i);
                    if (Node.ELEMENT_NODE == n.getNodeType()) {
                        String text = Utils.getTextFromElement((Element) n);
                        if (null == text) {
                            continue;
                        }
                        text = text.trim();
                        if (0 == text.length()) {
                            continue;
                        }
                        String parts[] = text.split(Pattern.quote(DELIM_INFO));
                        addToBins(parts, book);
                    }
                }
            }
            
        } catch (SAXParseException err) {
            System.out.println("Exception while processing file " + f.getName());
            err.printStackTrace();

        } catch (SAXException e) {
            System.out.println("Exception while processing file " + f.getName());            
            e.printStackTrace();

        } catch (Throwable t) {
            System.out.println("Exception while processing file " + f.getName());            
            t.printStackTrace();

        }
        
        
        
        
        
        
        
        
    }
    

    
    public static void addToBins(String[] msgList, String book) {
        for (String msg: msgList) {
            // Remove node and id specific information
            String working = msg;
            for (String parttern: PATTERN_LIST) {
                working = working.replaceAll(parttern, PATTERN_SUBSTITUTE);
            }
            ArrayList<String> msgs = patternMsgLookup.get(working);
            if (null == msgs) {
                msgs = new ArrayList<String>();
                patternMsgLookup.put(working, msgs);
            }
            msgs.add(book + " " + msg);
        }
    }
    

    
    public static void main(String args[]) {
        System.out.println("Specify name of directory with book files");
        outputInfo("C:\\paint\\generate_iba_20220713\\leafIBAInfo");
        if (args.length >= 1) {
            outputInfo(args[0]);
            return;
        }
    }
}
