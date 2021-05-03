/**
 *  Copyright 2020 University Of Southern California
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
package com.sri.panther.paintCommon.util;

import com.sri.panther.paintCommon.Constant;

import java.awt.Color;

import java.io.*;

import java.net.*;

import java.util.*;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This class has all the misc utility methods for parsing, string conversion
 *
 * @version %I%, %G%
 * @author Nan Guo and team panther
 */
public class Utils {

    public static final Logger logger =
        Logger.getLogger(Utils.class.getName());

    public static final String MSG_INVALID_SF_ID =
        "Invalid subfamily id encountered ";
    public static final String MSG_INVALID_AN_ID =
        "Invalid annotation id encountered ";
    public static final String MSG_DUPLICATE_AN_ID_ENCOUNTERED_FOR_SUBFAMILY =
        " Duplicate anotation id encountered for subfamily ";
    public static final String MSG_DUPLICATE_SF_ID_ENCOUNTERED_FOR_ANNOTATION_ID =
        " Duplicate subfamily id encountered for annotation node ";
    public static final String MSG_SF_AN_INFO_IS_NULL =
        "Subfamily annotation node relationship information is null";
    public static final String MSG_SF_AN_INFO_INVALID =
        "Subfamily annotation node relationship information is invalid ";
    
    public static final String STR_NEWLINE = "\n";

    /**
     * Replace any matching token to the replace character
     *
     * @param sOriginal  original string to parse
     * @param sToken  character to be replaced
     * @param sReplace  character to change to
     * @return a string after replacement
     */
    public static String replace(String sOriginal, char cToken,
                                 char cReplace) {
        return sOriginal.replace(cToken, cReplace);
    }

    /**
     * Method declaration
     * Replace any Matching String with the replace string
     * All multiple instance of the token which need to be replaced
     *
     * @param sOriginal original string to parse
     * @param sToken  string to be replaced
     * @param sReplace string to change to
     * @return a string after replacement
     * @see
     */
    public static String replace(String sOriginal, String sToken,
                                 String sReplace) {
        if (sToken.length() == 1 && sReplace.length() == 1) {
            return replace(sOriginal, sToken.charAt(0), sReplace.charAt(0));
        } else {
            int index;
            String result = sOriginal;

            index = result.indexOf(sToken);
            while (index != -1) {
                result =
                        result.substring(0, index) + sReplace + result.substring(index +
                                                                                 sToken.length(),
                                                                                 result.length());
                index = result.indexOf(sToken);
            }
            return result;
        }
    }

    /**
     * Method declaration
     * Replace any Matching String
     * All multiple instance of the token which need to be replaced
     *
     * @param sOriginal
     * @param sToken
     * @param sReplace
     * @param boolean to determine whether replace recusively
     *
     * @return
     *
     * @see
     */
    public static String replace(String sOriginal, String sToken,
                                 String sReplace, boolean recusive) {
        if (recusive) {
            return replace(sOriginal, sToken, sReplace);
        }
        int index;
        String result = sOriginal;

        index = result.indexOf(sToken);
        if (index != -1) {
            result =
                    result.substring(0, index) + sReplace + result.substring(index +
                                                                             sToken.length(),
                                                                             result.length());
        }
        return result;
    }

    /*
   * Convert the list of items to a string with a specified delimiter
   * @param list Item list
   * @param wrapper Wrapper for each item
   * @param delim delimiter for separating items
   * @return the String representation of the list of selected items
   */


    /**
     * transform a Vector of Strings into a delimiter-separated list in a String
     *
     * @param list -- the Vector of Strings
     * @param wrapper -- enclose each element in a pair of these characters (ie, single quotes)
     * @param delim -- the list delimiter (ie, comma)
     * @return the delimiter-separated String
     */
    public static String listToString(Vector list, String wrapper,
                                      String delim) {
        //return an empty string in case of an empty list
        if (list.isEmpty())
            return Constant.STR_EMPTY;

        int size = list.size();
        StringBuffer selection = new StringBuffer();

        // add each item in the Vector to the SB with wrapper and delimiter
        for (int i = 0; i < size - 1; i++) {
            selection.append(wrapper);
            selection.append((String)list.elementAt(i));
            selection.append(wrapper);
            selection.append(delim);
        }

        // add last item in the Vector to the SB with wrapper but no delimiter
        selection.append(wrapper);
        selection.append((String)list.elementAt(size - 1));
        selection.append(wrapper);
        return selection.toString();
    }

    /**
     * Method declaration
     *
     *
     * @param list
     * @param wrapper
     * @param delim
     *
     * @return
     *
     * @see
     */
    public static String listToString(String[] list, String wrapper,
                                      String delim) {
        int size = list.length;
        StringBuffer selection = new StringBuffer();

        // add each item in the Vector to the SB with wrapper and delimiter
        for (int i = 0; i < size - 1; i++) {
            selection.append(wrapper);
            selection.append((String)list[i]);
            selection.append(wrapper);
            selection.append(delim);
        }

        // add last item in the Vector to the SB with wrapper but no delimiter
        selection.append(wrapper);
        selection.append((String)list[size - 1]);
        selection.append(wrapper);
        return selection.toString();
    }

    /**
     * Tokenizes the original string using the whole delimiter string in a
     * case-insensitive way. Includes the matching tokens in their original cases.
     * For consistency, the first and last tokens NEVER match the delimiter string.
     * Also, back-to-back delimiters are separated. This way, the caller is guaranteed
     * that every other token, starting with the second one, matches the delimiter
     * string, ignoring case.
     * For example, this: (misspellings intentional)
     *   Utils.embeddedTokenize("Induce vomitining in case of ingestoin", "IN");
     * Would return this:
     *   ""
     *   "In"
     *   "duce vomit"
     *   "in"
     *   ""
     *   "in"
     *   "g "
     *   "in"
     *   " case of "
     *   "in"
     *   "gesto"
     *   "in"
     *   ""
     * @param original
     * @param longDelimiter
     * @return
     */
    public static String[] embeddedTokenize(String original,
                                            String longDelimiter) {
        ArrayList resultList = new ArrayList();

        int endOfLastDelimiter = 0;
        int startOfNextDelimiter =
            original.toLowerCase().indexOf(longDelimiter.toLowerCase(),
                                           endOfLastDelimiter);
        while (startOfNextDelimiter != -1) {
            String unmatchedToken =
                original.substring(endOfLastDelimiter, startOfNextDelimiter);
            String matchedToken =
                original.substring(startOfNextDelimiter, startOfNextDelimiter +
                                   longDelimiter.length());
            resultList.add(unmatchedToken);
            resultList.add(matchedToken);
            endOfLastDelimiter = startOfNextDelimiter + longDelimiter.length();
            startOfNextDelimiter =
                    original.toLowerCase().indexOf(longDelimiter.toLowerCase(),
                                                   endOfLastDelimiter);
        }
        String unmatchedToken = original.substring(endOfLastDelimiter);
        resultList.add(unmatchedToken);

        String[] result = new String[resultList.size()];
        result = (String[])resultList.toArray(result);
        return result;
    }

    /*
   * Take the given string and chop it up into a series
   * of strings based on the delimiter specified.
   * param input - The input string
   * param delim - The delimiter
   */

    /**
     * Method declaration
     *
     *
     * @param input
     * @param delim
     *
     * @return
     *
     * @see
     */
    public static String[] tokenize(String input, String delim) {
        Vector v = new Vector();
        StringTokenizer tk = new StringTokenizer(input, delim);
        String strArray[];

        while (tk.hasMoreTokens()) {
            v.addElement(tk.nextToken());
        }

        // Copy into an array of strings
        strArray = new String[v.size()];
        v.copyInto(strArray);
        return strArray;
    }

    /**
     * Given a string and a separator, return an array of containing the indices of the separators   *
     */
    public static Integer[] getSeparatorIndices(String s, String separator) {
        Vector v = new Vector();
        int length = separator.length();
        int i = 0;

        while ((i + length) < s.length()) {
            if (true == s.regionMatches(true, i, separator, 0, length)) {
                v.addElement(new Integer(i));
            }
            i++;
        }

        // Copy contents of vector into a string array
        Integer integerArray[] = new Integer[v.size()];

        v.copyInto(integerArray);
        return integerArray;
    }

    /**
     * Given two string arrays, returns if the lengths of the segments
     * within the arrays are equal
     */
    public static boolean compare(String[] s1, String[] s2) {
        if (s1.length != s2.length) {
            return false;
        }
        for (int i = 0; i < s1.length; i++) {
            if (s1[i].length() != s2[i].length()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Given two integer arrays, returns if the contents of the arrays are equal
     */
    public static boolean compare(Integer[] i1, Integer[] i2) {
        if (i1.length != i2.length) {
            return false;
        }
        for (int i = 0; i < i1.length; i++) {
            if (0 != i1[i].compareTo(i2[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reads the file specified into an array of strings[].  The strings will be
     * broken up based on the delimiter specified.
     * is - input stream
     * delim - a string containing the delimiter characters
     */
    public static String[][] readDelimitedFile(InputStream is,
                                               String delim) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(is));

        try {

            // Read the string,
            String currentLine;
            Vector Strings = new Vector();

            while (null != (currentLine = r.readLine())) {
                Strings.addElement(tokenize(currentLine, delim));
            }
            r.close();

            // Copy into an array of strings[]
            String stringArray[][] = new String[Strings.size()][];

            for (int i = 0; i < stringArray.length; i++) {
                stringArray[i] = (String[])Strings.elementAt(i);
            }
            return stringArray;
        } catch (IOException ie) {
            ie.printStackTrace();
            throw ie;
        }
    }

    /**
     *
     */
    public static String trimFromEnds(String original, String trimString) {
        String returnStr = original;

        if (returnStr.startsWith(trimString)) {
            returnStr =
                    returnStr.substring(trimString.length(), returnStr.length());
        }
        if (returnStr.endsWith(trimString)) {
            returnStr = returnStr.substring(0, returnStr.length() - 1);
        }
        return returnStr;
    }

    /**
     * To check whether a string can be convert into a number
     * @param inStr -- input string to be converted.
     */
    public static boolean isNumber(String inStr) {
        for (int i = 0; i < inStr.length(); i++) {
            char tmp = inStr.charAt(i);

            if (!Character.isDigit(tmp)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Method declaration
     *
     *
     * @param t
     * @param fileName
     * @param fileDelimiter
     *
     * @return
     *
     * @throws IOException
     *
     * @see
     */
    public static String[][] readFile(String fileName,
                                      String fileDelimiter) throws IOException {
        File inputFile = new File(fileName);
        InputStream is =
            (InputStream)Utils.class.getResourceAsStream(fileName);

        if (null == is) {
            FileInputStream fs;

            try {
                fs = new FileInputStream(inputFile);
            } catch (IOException ie) {
                throw ie;
            }
            String[][] matrix = Utils.readDelimitedFile(fs, fileDelimiter);

            fs.close();
            return matrix;
        } else {
            String[][] matrix = Utils.readDelimitedFile(is, fileDelimiter);

            is.close();
            return matrix;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param fileName
     *
     * @return
     *
     * @throws IOException
     *
     * @see
     */
    public static Vector fileToVector(String fileName,
                                      String delimiter) throws IOException {
        return fileToVector(fileName, delimiter, true);
    }

    /**
     * Method declaration
     *
     *
     * @param fileName
     * @param uniqueFlag - if set to true, then returns unique list
     * @return vector of IDs
     *
     * @throws IOException
     *
     * @see
     */
    public static Vector fileToVector(String fileName, String delimiter,
                                      boolean uniqueFlag) throws IOException {
        Vector idList = new Vector();
        Hashtable ids = new Hashtable();
        StringTokenizer st;
        try {
            File inputFile = new File(fileName);
            BufferedReader fileStream;

            InputStream is =
                (InputStream)Utils.class.getResourceAsStream(fileName);
            if (null == is) {
                FileInputStream fs = new FileInputStream(fileName);
                fileStream = new BufferedReader(new InputStreamReader(fs));
            } else
                fileStream = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = fileStream.readLine()) != null) {
                st = new StringTokenizer(line, delimiter);
                while (st.hasMoreTokens()) {
                    if (uniqueFlag)
                        ids.put(st.nextToken(), "1");
                    else
                        idList.addElement(st.nextToken());
                }
            }

            String id;
            if (uniqueFlag) {
                for (Enumeration e = ids.keys(); e.hasMoreElements(); ) {
                    id = (String)e.nextElement();
                    idList.addElement(id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return idList;
    }


    /**
     * Method declaration
     *
     *
     * @param URL
     * @param portNumber
     * @param localPath
     *
     * @return
     *
     * @see
     */
    public static BufferedReader getFile(String URL, int portNumber,
                                         String localPath) {
        try {
            Socket s = new Socket(URL, portNumber);
            BufferedReader in =
                new BufferedReader(new InputStreamReader(s.getInputStream()));
            PrintWriter out = new PrintWriter(s.getOutputStream());

            out.print("GET " + localPath + "\n\n");
            out.flush();
            return (in);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return (null);
    }

    /**
     * Method declaration
     *
     *
     * @param URL
     * @param portNumber
     * @param localPath
     *
     * @return
     *
     * @see
     */
    public static InputStream getFileAsInputStream(String URL, int portNumber,
                                                   String localPath) {
        try {
            Socket s = new Socket(URL, portNumber);
            InputStream is = s.getInputStream();
            PrintWriter out = new PrintWriter(s.getOutputStream());

            out.print("GET " + localPath + "\n\n");
            out.flush();
            return (is);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        return (null);
    }

    /**
     * description writes the array of strings into the file name speecified
     */
    public static boolean writeFile(String fileName, String[] contents) {
        BufferedWriter bufWriter = null;
        String line;
        boolean success = true;

        if (null == contents) {
            return false;
        }
        try {
            bufWriter = new BufferedWriter(new FileWriter(fileName));
            for (int i = 0; i < contents.length; i++) {
                line = contents[i];
                bufWriter.write(line);
            }
        } catch (IOException ioex) {
            success = false;
            System.out.println("Exception " + ioex.getMessage() +
                               " returned while attempting to write file " +
                               fileName);
        } finally {
            try {
                if (null != bufWriter) {
                    bufWriter.close();
                }
            } catch (IOException ioex2) {
                success = false;
                System.out.println("Exception " + ioex2.getMessage() +
                                   " returned while attempting to close file " +
                                   fileName);
            }
        }
        return success;
    }


    /**
     * get maximum value in an array of numbers
     */
    public static int getMaxValue(int[] numbers) {
        int max = numbers[0];

        for (int i = 0; i < numbers.length; i++) {
            if (max < numbers[i]) {
                max = numbers[i];
            }
        }
        return max;
    }

    /**
     * get minimum value in an array of numbers
     */
    public static int getMinValue(int[] numbers) {
        int min = numbers[0];

        for (int i = 0; i < numbers.length; i++) {
            if (min > numbers[i]) {
                min = numbers[i];
            }
        }
        return min;
    }

    /**
     * Convert a double value to percentage
     *
     * @param value  value to convert
     * @param precision  how many digits after decimal point to keep
     *                   after converted the value to percent.
     *                   The precision should be >= 0
     * @return a string for the percentage representation
     *
     */
    public static String doubleToPercent(double value, int precision) {
        long tempValue = Math.round(value * (Math.pow(10, (precision + 2))));
        String percentValue = null;
        if (precision == 0)
            percentValue = String.valueOf((double)tempValue);
        else
            percentValue =
                    String.valueOf(((double)tempValue) / (Math.pow(10, precision)));
        percentValue += '%';
        return percentValue;
    }

    /**
     *
     * @param searchList list for seaching
     * @param s search string
     * @return true if search string exists in list, false otherwise
     */
    public static boolean search(String[] searchList, String s) {
        if (null == s || null == searchList) {
            return false;
        }
        for (int i = 0; i < searchList.length; i++) {
            if (0 == searchList[i].compareTo(s)) {
                return true;
            }
        }
        return false;
    }


    /**
     *
     * @param searchList Vector for seaching
     * @param s search string
     * @return true if search string exists in list, false otherwise
     */
    public static boolean search(Vector searchList, String s) {
        if (null == s || null == searchList) {
            return false;
        }
        for (int i = 0; i < searchList.size(); i++) {
            if (true == ((String)searchList.get(i)).equals(s)) {
                return true;
            }
        }
        return false;
    }


    public static int getIndex(String[] searchList, String s) {
        if (null == s || null == searchList) {
            return -1;
        }
        for (int i = 0; i < searchList.length; i++) {
            if (0 == searchList[i].compareTo(s)) {
                return i;
            }
        }
        return -1;
    }

    public static int getIndex(List searchList, String s) {
        if (null == s || null == searchList) {
            return -1;
        }
        for (int i = 0; i < searchList.size(); i++) {
            if (true == ((String)searchList.get(i)).equals(s)) {
                return i;
            }
        }
        return -1;
    }


    public static String getHexColorString(Color color) {
        StringBuffer result = new StringBuffer();
        if (color.getRed() == 0)
            result.append("00");
        else
            result.append(Integer.toHexString(color.getRed()));
        if (color.getGreen() == 0)
            result.append("00");
        else
            result.append(Integer.toHexString(color.getGreen()));
        if (color.getBlue() == 0)
            result.append("00");
        else
            result.append(Integer.toHexString(color.getBlue()));
        return result.toString();
    }

    /**
     * Count how many time a character occur in the search string
     * @param searchString  String to search for
     * @param c character to search for
     * @return how many time the character occur in the string
     */
    public static int howOften(String searchString, char c) {
        String regexp = Constant.STR_EMPTY + c;
        String s = null;
        try {
            s = searchString.replaceAll(regexp, Constant.STR_EMPTY);
        } catch (Exception e) {
            regexp = Constant.STR_DOUBLE_SLASH + c;
            s = searchString.replaceAll(regexp, Constant.STR_EMPTY);
        }
        return searchString.length() - s.length();
    }


    public static final String constructExternalSubfamilyId(String book,
                                                            String subfamilyId) {
        if (null == book) {
            return null;
        }
        if (null == subfamilyId) {
            return book;
        }
        return book + Constant.DELIM_BOOK_ANNOT_NODE + subfamilyId;
    }


    public static final String getBookIdFromBookSubfamilyId(String bookSubfamilyId) {
        if (null == bookSubfamilyId) {
            return null;
        }
        int index = bookSubfamilyId.indexOf(Constant.DELIM_BOOK_SF_NODE);
        if (index < 0) {
            return bookSubfamilyId;
        }
        return bookSubfamilyId.substring(0, index);
    }


    public static final String getSfIdFromBookSubfamilyId(String bookSubfamilyId) {
        if (null == bookSubfamilyId) {
            return null;
        }
        int index = bookSubfamilyId.indexOf(Constant.DELIM_BOOK_SF_NODE);
        if (index < 0) {
            return null;
        }
        return bookSubfamilyId.substring(index + 1);

    }


    public static final String constructExternalAnnotId(String book,
                                                        String annotId) {
        if (null == book) {
            return null;
        }
        if (null == annotId) {
            return book;
        }
        return book + Constant.DELIM_BOOK_ANNOT_NODE + annotId;
    }


    public static final String getBookId(String bookAnnotId) {
        if (null == bookAnnotId) {
            return null;
        }
        int index = bookAnnotId.indexOf(Constant.DELIM_BOOK_ANNOT_NODE);
        if (index < 0) {
            return bookAnnotId;
        }
        return bookAnnotId.substring(0, index);
    }


    public static final String getAnnotId(String bookAnnotId) {
        if (null == bookAnnotId) {
            return null;
        }
        int index = bookAnnotId.indexOf(Constant.DELIM_BOOK_ANNOT_NODE);
        if (index < 0) {
            return null;
        }
        return bookAnnotId.substring(index + 1);

    }
    
    public static ArrayList<String> readFile(String fileName) {
        ArrayList contents = new ArrayList();
        try {        
            BufferedReader br = new BufferedReader(new FileReader(fileName));


            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                contents.add(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return contents;
    }


    /**
     *
     * @param sfAnInfo true if subfamily names confirm to subfamily naming standards
     * @return Hashtable of annotation node ids to subfamily ids
     */
    public static Hashtable<String, String> parseSfAnInfo(String[] sfAnInfo,
                                                             boolean checkSFName) {
        if (null == sfAnInfo) {
            logger.error(MSG_SF_AN_INFO_IS_NULL);
            return null;
        }
        int length = sfAnInfo.length;
        Hashtable<String, String> AnSfTbl =
            new Hashtable<String, String>(length);
        Hashtable<String, String> sfTbl =
            new Hashtable<String, String>(length);
        for (int i = 0; i < length; i++) {
            String info = sfAnInfo[i];
            info = info.trim();
            String infoStr[] =
                Utils.tokenize(info, Constant.SF_AN_INFO_SEPARATOR);
            int infoLen = infoStr.length;
            if (infoLen >= Constant.SF_AN_INDEX_AN &&
                infoLen >= Constant.SF_AN_INDEX_SF) {
                if (false == checkSFName) {
                    AnSfTbl.put(infoStr[Constant.SF_AN_INDEX_AN],
                                infoStr[Constant.SF_AN_INDEX_SF]);
                } else {

                    // Check validity of subfamily id
                    String sfName = infoStr[Constant.SF_AN_INDEX_SF];
                    if (null == sfName ||
                        false == sfName.startsWith(Constant.NODE_SUBFAMILY_PREFIX)) {
                        logger.error(MSG_INVALID_SF_ID + sfName);
                        return null;
                    }
                    String numberPart =
                        sfName.substring(Constant.NODE_SUBFAMILY_PREFIX_LENGTH,
                                         sfName.length());
                    try {
                        Integer.parseInt(numberPart);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                        logger.error(MSG_INVALID_SF_ID + sfName);
                        return null;
                    }

                    // Repeat for annotation id
                    String anId = infoStr[Constant.SF_AN_INDEX_AN];
                    if (null == anId ||
                        false == anId.startsWith(Constant.NODE_ANNOTATION_PREFIX)) {
                        logger.error(MSG_INVALID_AN_ID + anId);
                        return null;
                    }
                    numberPart =
                            anId.substring(Constant.NODE_ANNOTATION_PREFIX_LENGTH,
                                           anId.length());
                    try {
                        Integer.parseInt(numberPart);
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                        logger.error(MSG_INVALID_AN_ID + anId);
                        return null;
                    }

                    // Ensure all subfamily and annotaton node ids are unique
                    String previousSfId = AnSfTbl.put(anId, sfName);
                    if (null != previousSfId) {
                        logger.error(anId +
                                     MSG_DUPLICATE_AN_ID_ENCOUNTERED_FOR_SUBFAMILY +
                                     previousSfId);
                        return null;
                    }
                    if (null != sfTbl.put(sfName, sfName)) {
                        logger.error(sfName +
                                     MSG_DUPLICATE_SF_ID_ENCOUNTERED_FOR_ANNOTATION_ID +
                                     anId);
                        return null;
                    }


                }
            } else {
                logger.error(MSG_SF_AN_INFO_INVALID + info);
                return null;
            }
        }
        return AnSfTbl;
    }
    
    public final static String PAINT_REF = "PAINT_REF";
        
    public static String getPaintEvidenceAcc(String bookId) {

        int acc = Integer.valueOf(bookId.substring("PTHR".length())).intValue();
        String paint_id = String.format("%1$07d", acc);
        return paint_id;

    }  
    
        public static String getTextFromXml(Element elem, String tag) {
        
        NodeList nodeList = elem.getElementsByTagName(tag);
        if (null == nodeList) {
            return null;
        }
        if (null != nodeList && 0 < nodeList.getLength()) {
            Element textElement = (Element)nodeList.item(0);
            NodeList textNodes = textElement.getChildNodes();
            if (null != textNodes && textNodes.getLength() > 0) {
                Node n = textNodes.item(0);
                String s = n.getNodeValue();
                if (null != s) {
                    return s.trim();
                }
            }
        }
        return null;
    }
    
    public static String getTextFromElement(Element elem) {
        if (null == elem) {
            return null;
        }
        NodeList textNodes = elem.getChildNodes();
        if (null == textNodes) {
            return null;
        }
        if (textNodes.getLength() <= 0) {
            return null;
        }
        
        Node n = textNodes.item(0);
        String s = n.getNodeValue();
        if (null != s) {
            return s.trim();
        }
        return null;
        
    }
    
    public static Element createTextNode(Document doc, String elementTag, String text) {
        if (null == doc || null == elementTag || null == text) {
            return null;
        }
        Element elem = doc.createElement(elementTag);
        Text t = doc.createTextNode(text);
        elem.appendChild(t);
        return elem;
    }
    
    public static String readFromUrl(String urlStr, int connectionTimeout, int readTimeout) {
        if (null == urlStr || 0 == urlStr.length()) {
                return null;
        }
        StringBuffer sb = new StringBuffer();
        String nextLine;
        URL url = null;
        URLConnection urlConn = null;
        InputStreamReader inStream = null;
        BufferedReader buff = null;
        try {

            url = new URL(urlStr);
            urlConn = url.openConnection();
            if (-1 != connectionTimeout) {
                urlConn.setConnectTimeout(connectionTimeout);
            }
            if (-1 != readTimeout) {
                urlConn.setReadTimeout(readTimeout);
            }
            inStream = new InputStreamReader(urlConn.getInputStream());
            buff = new BufferedReader(inStream);


            while (true) {
                nextLine = buff.readLine();
                if (nextLine != null) {
                    sb.append(nextLine + STR_NEWLINE);
                } else {
                    break;
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Please check the URL: " +urlStr + " " + e.toString());
            return null;
        } catch (IOException e1) {
            System.out.println("Can't read  from url: " + urlStr + " " +  e1.toString());
            return null;                   
        }
        catch (Exception e) {
            System.out.println("Can't read  from url: " + urlStr + " " +  e.toString());
            //e.printStackTrace();
            return null;
        }
        
        return sb.toString();
        
        
        
    }      
}
