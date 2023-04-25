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
package com.sri.panther.paintCommon.familyLibrary;

import java.io.File;
import java.util.Vector;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.FileUtils;

public class FileNameGenerator {

//  protected static final String BETE_SUFFIX = "bete";
    protected static final String ATTR_SUFFIX = "attr";
    protected static final String TREE_SUFFIX = "tree";
    public static final String PAINT_SUFFIX = "paint";
    protected static final String SFAN_SUFFIX = "sfan";
    protected static final String TAB_SUFFIX = "tab";
    //protected static final String CLS_SUFFIX = "classification";
    protected static final String CLS_FILE_NAME = "PANTHER_GO_Slim";
    protected static final String BETE_TREE = "tree.";
    protected static final String PAINT_TREE = "tree.";
    protected static final String SFAN_RELATIONSHIP = "sfan";
    protected static final String PIR = "pir";
    protected static final String MIA = "mia";
    protected static final String PIR_FILE = "pir";
    protected static final String CLUSTER_FILE = "cluser.";
    protected static final String CLUSTER_WTS_FILE = "cluster.wts";
    protected static final String WTS = ".wts";
    protected static final String DIR_BOOKS = "books";
    protected static final String DIR_GLOBAL = "globals";
    protected static final String INFO = "info";
//  protected static final String DIR_BETE = "bete";
    protected static final String DIR_PAINT = "tree";     // Since paint file is in tree directory and trees are found in the tree directory
    protected static final String DIR_CLUSTS = "clusts";
    protected static final String DIR_WTS = "seqwt";
    protected static final String DOT = ".";
    protected static final String WEB_DIR_SEPARATOR = "/";
    protected static final String NAME_SF_SEQ = ".sfToSeq";
    protected static final String NAME_SF_TO_ONTOLOGY = ".sfToOnt";
    protected static final String NAME_ATTRIB = ".attr.tab";
    protected static final String NAME_ATTR_TAB = "attr.tab";
    protected static final String DIR_TREE = "tree";
    protected static final String DOT_TSV = ".tsv";
    protected static final String PREFIX_PFAM = "pfam";    

    public static final int MSA_INDEX_PIR = 0;
    public static final int MSA_INDEX_MIA = 1;

    protected static final String STR_EMPTY = "";

    /**
     * Constructor declaration
     *
     *
     * @see
     */
    public FileNameGenerator() {
    }

    public static boolean IsPAINTFileNameValid(String PAINTFileName) {
        if (PAINTFileName.endsWith(DOT + PAINT_SUFFIX)) {
            return true;
        }
        return false;
    }

    public static String formatPAINTFileName(String PAINTFileName) {
        if (true == IsPAINTFileNameValid(PAINTFileName)) {
            return PAINTFileName;
        } else {
            return PAINTFileName + DOT + PAINT_SUFFIX;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param BETEFileName
     *
     * @return
     *
     * @see
     */
//	public static String getAttribForBETE(String BETEFileName){
//	if (IsBETEFileNameValid(BETEFileName)){
//	return (BETEFileName.substring(0, BETEFileName.length() - BETE_SUFFIX.length())) + ATTR_SUFFIX;
//	}
//	else{
//	return null;
//	}
//	}
    public static String getAttribForPAINT(String PAINTFileName) {
        if (IsPAINTFileNameValid(PAINTFileName)) {
            return (PAINTFileName.substring(0, PAINTFileName.length() - PAINT_SUFFIX.length())) + ATTR_SUFFIX;
        } else {
            return null;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param BETEFileName
     *
     * @return
     *
     * @see
     */
//	public static String getTreeForBETE(String BETEFileName){
//	if (IsBETEFileNameValid(BETEFileName)){
//	return (BETEFileName.substring(0, BETEFileName.length() - BETE_SUFFIX.length())) + TREE_SUFFIX;
//	}
//	else{
//	return null;
//	}
//	}
    public static String getTreeForPAINT(String PAINTFileName) {
        if (IsPAINTFileNameValid(PAINTFileName)) {
            return (PAINTFileName.substring(0, PAINTFileName.length() - PAINT_SUFFIX.length())) + TREE_SUFFIX;
        } else {
            return null;
        }
    }

    public static String getSfAnForPAINT(String PAINTFileName) {
        if (IsPAINTFileNameValid(PAINTFileName)) {
            return (PAINTFileName.substring(0, PAINTFileName.length() - PAINT_SUFFIX.length())) + SFAN_SUFFIX;
        } else {
            return null;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
//	public static String getInfoForBETE(LibrarySettings librarySettings){
//	String  path = generatePath(librarySettings, DIR_BETE);
//	String  fileName = INFO + DOT + TAB_SUFFIX;
//	path = FileUtils.appendFileToPath(path, fileName);
//	return path;
//	}
    /**
     * Method declaration
     *
     *
     * @param librarySettings
     * @param dir
     *
     * @return
     *
     * @see
     */
    protected static String generatePath(LibrarySettings librarySettings, String dir) {
        String path = null;
        String libraryName = librarySettings.getLibraryName();
        if (null == libraryName || 0 == libraryName.length()) {
            path = librarySettings.getLibraryRoot();
        } else {
            path = FileUtils.appendPathToPath(librarySettings.getLibraryRoot(), libraryName);
        }

        path = FileUtils.appendPathToPath(path, DIR_BOOKS);
        path = FileUtils.appendPathToPath(path, librarySettings.getBookName());
        if ((null != librarySettings.getEntryType()) && (true == librarySettings.getEntryType().entry)) {

            // Handle build library structure
            path = FileUtils.appendPathToPath(path, librarySettings.getEntryType().entryType);
            if (null != dir) {
                path = FileUtils.appendPathToPath(path, dir);
            }
        }

        // System.out.println("FileNameGenerator is returning path " + path);
        return path;
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
    protected static String generateInitialFileName(LibrarySettings librarySettings) {
        String fileName = STR_EMPTY;

        if ((null != librarySettings.getEntryType()) && (true == librarySettings.getEntryType().entry)) {

            // Handle build library structure
            fileName = librarySettings.getBookName() + DOT + librarySettings.getEntryType().getEntryType() + DOT;

            String bookSuffix = librarySettings.getBookSufix();
            if (null != bookSuffix) {
                fileName += bookSuffix + DOT;
            }
        }
        return fileName;
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
//	public static String getBETEPathName(LibrarySettings librarySettings){
//	String  path = generatePath(librarySettings, DIR_BETE);
//	String  fileName = generateInitialFileName(librarySettings);
//	if (0 == fileName.length()){
//	fileName = BETE_TREE;
//	}
//	fileName = fileName + BETE_SUFFIX;
//	path = FileUtils.appendFileToPath(path, fileName);
//	return path;
//	}
    public static String getPAINTPathName(LibrarySettings librarySettings) {
        String path = generatePath(librarySettings, DIR_PAINT);
        String fileName = generateInitialFileName(librarySettings);

        if (0 == fileName.length()) {
            fileName = PAINT_TREE;
        }
        fileName = fileName + PAINT_SUFFIX;
        path = FileUtils.appendFileToPath(path, fileName);
        return path;
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
    public static String getTreeName(LibrarySettings librarySettings) {
        if ((null != librarySettings.getEntryType()) && (true == librarySettings.getEntryType().entry)) {

            return librarySettings.bookName + DOT + librarySettings.getEntryType().entryType + DOT + TREE_SUFFIX;
        } else {
            return BETE_TREE + TREE_SUFFIX;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
//	public static String getAttrName(LibrarySettings librarySettings){
//	if ((null != librarySettings.getEntryType()) && (true == librarySettings.getEntryType().entry)){
//	return librarySettings.bookName + DOT + librarySettings.getEntryType().entryType + DOT + ATTR_SUFFIX;
//	}
//	else{
//	return BETE_TREE + ATTR_SUFFIX;
//	}
//	}
    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
    public static String getClassificationName(LibrarySettings librarySettings) {
        String path = FileUtils.appendPathToPath(librarySettings.getLibraryRoot(), librarySettings.getLibraryName());

        path = FileUtils.appendPathToPath(path, DIR_GLOBAL);
        //String  fileName = CLS_SUFFIX + DOT + TAB_SUFFIX;
        //String fileName = CLS_FILE_NAME

        path = FileUtils.appendFileToPath(path, CLS_FILE_NAME);
        return path;
    }

    /**
     * List the name of directories which are existing in the specified
     * directory.
     *
     * @param dir File for a directory
     * @return Array of String if any; null if the arg does not specify a
     * directory.
     */
    protected static String[] listDir(File dir) {
        String[] families = null;
        Vector<String> dirVector;

        if (dir.isDirectory()) {
            families = dir.list();
            dirVector = new Vector<String>(families.length);
            for (int i = 0; i < families.length; i++) {
                if ((new File(dir.getAbsolutePath() + File.separator + families[i])).isDirectory()) {
                    dirVector.addElement(families[i]);
                }
            }
            families = new String[dirVector.size()];
            dirVector.copyInto(families);

        }
        return families;
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
    public static String[] getBooks(LibrarySettings librarySettings) {
        String path = FileUtils.appendPathToPath(librarySettings.getLibraryRoot(), librarySettings.getLibraryName());

        path = FileUtils.appendPathToPath(path, DIR_BOOKS);
        File bookPath = new File(path);

        return listDir(bookPath);
    }

    public static String getANSeqFileForPaint(String paintFilePath) {
        String path = FileUtils.getPath(paintFilePath);
        String paintName = FileUtils.getFileName(paintFilePath);
        return path + File.separator + paintName.substring(0, paintName.length() - PAINT_SUFFIX.length()) + MIA;
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
//	public static String getMSA(LibrarySettings librarySettings){
//		if (null == librarySettings){
//			return null;
//		}
//		EntryType et = librarySettings.getEntryType();
//		String libURL = null;
//
//		if (null != librarySettings.getLibURL()) {
//			libURL = librarySettings.getLibURL();
//		}
//		else {
//			String libraryName = librarySettings.getLibraryName();
//			if (null != libraryName) {
//				libURL = FileUtils.appendFileToPath(librarySettings.getLibraryRoot(), libraryName);
//			}
//			else {
//				libURL = librarySettings.getLibraryRoot(); 
//			}
//		}
//		if (null == et){
//			return libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
//			+ librarySettings.getBookName() + WEB_DIR_SEPARATOR + PIR_FILE;
//		}
//		else{
//			return libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
//			+ librarySettings.getBookName() + WEB_DIR_SEPARATOR + et.getEntryType() + WEB_DIR_SEPARATOR + DIR_CLUSTS
//			+ WEB_DIR_SEPARATOR + librarySettings.getBookName() + DOT + et.getEntryType() + DOT + PIR_FILE;
//		}
//
//	}
    public static String[] getMSAFilesForPaint(LibrarySettings librarySettings) {
        if (null == librarySettings) {
            return null;
        }
        String urlList[] = new String[2];
        EntryType et = librarySettings.getEntryType();
        String libURL = null;

        if (null != librarySettings.getLibURL()) {
            libURL = librarySettings.getLibURL();
        } else {
            String libraryName = librarySettings.getLibraryName();
            if (null != libraryName) {
                libURL = FileUtils.appendFileToPath(librarySettings.getLibraryRoot(), libraryName);
            } else {
                libURL = librarySettings.getLibraryRoot();
            }
        }
        String bookName = librarySettings.getBookName();
        if (null == et) {
            urlList[0] = libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + bookName + WEB_DIR_SEPARATOR + CLUSTER_FILE + PIR_FILE;
            urlList[1] = libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + bookName + WEB_DIR_SEPARATOR + PAINT_TREE + MIA;
        } else {
            String bookSuffix = librarySettings.getBookSufix();
            if (null == bookSuffix || 0 == bookSuffix.length()) {
                bookSuffix = Constant.STR_EMPTY;
            } else {
                bookSuffix += DOT;
            }
            urlList[0] = libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + bookName + WEB_DIR_SEPARATOR + et.getEntryType() + WEB_DIR_SEPARATOR + DIR_CLUSTS
                    + WEB_DIR_SEPARATOR + librarySettings.getBookName() + DOT + et.getEntryType() + DOT + PIR_FILE;
            urlList[1] = libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + librarySettings.getBookName() + WEB_DIR_SEPARATOR + et.getEntryType() + WEB_DIR_SEPARATOR + DIR_PAINT
                    + WEB_DIR_SEPARATOR + bookName + DOT + et.getEntryType() + DOT + bookSuffix + MIA;
        }

        return urlList;

    }

    public static String getAttrFile(LibrarySettings librarySettings) {
        if (null == librarySettings) {
            return null;
        }
        EntryType et = librarySettings.getEntryType();
        String libURL = null;
        if (null != et) {
            return null;
        }

        if (null != librarySettings.getLibURL()) {
            libURL = librarySettings.getLibURL();
        } else {
            String libraryName = librarySettings.getLibraryName();
            if (null != libraryName) {
                libURL = FileUtils.appendFileToPath(librarySettings.getLibraryRoot(), libraryName);
            } else {
                libURL = librarySettings.getLibraryRoot();
            }
        }

        String bookName = librarySettings.getBookName();
        return libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR + bookName + WEB_DIR_SEPARATOR + NAME_ATTR_TAB;
    }

    /**
     * Method declaration
     *
     *
     * @param librarySettings
     *
     * @return
     *
     * @see
     */
    public static String getMSAWts(LibrarySettings librarySettings) {
        if (null == librarySettings) {
            return null;
        }
        String libURL = null;

        if (null != librarySettings.getLibURL()) {
            libURL = librarySettings.getLibURL();
        } else {
            libURL = FileUtils.appendPathToPath(librarySettings.getLibraryRoot(), librarySettings.getLibraryName());
        }

        EntryType et = librarySettings.getEntryType();

        if (null == et) {
            return libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + librarySettings.getBookName() + WEB_DIR_SEPARATOR + CLUSTER_WTS_FILE;
        } else {
            return libURL + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + librarySettings.getBookName() + WEB_DIR_SEPARATOR + et.getEntryType() + WEB_DIR_SEPARATOR + DIR_WTS
                    + WEB_DIR_SEPARATOR + librarySettings.getBookName() + DOT + et.getEntryType() + DOT + "mag" + WTS;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param beteFilePath
     *
     * @return
     *
     * @see public static Vector<String> getMSAFiles(String beteFilePath){
     * Vector<String> v = new Vector<String> (); String path =
     * FileUtils.getPath(beteFilePath); String beteName =
     * FileUtils.getFileName(beteFilePath);
     *
     * v.addElement(path + File.separator + PIR); File parent = new File(path);
     *
     * if (null != parent){ parent = new File(parent.getParent()); if (null !=
     * parent){ v.addElement(parent.getPath() + File.separator + DIR_CLUSTS +
     * File.separator + beteName.substring(0, beteName.length() -
     * BETE_SUFFIX.length()) + PIR); return v; } } v.addElement(STR_EMPTY);
     * return v; }
     */
    public static Vector<String> getMSAFilesForPAINT(String paintFilePath) {
        Vector<String> v = new Vector<String>();
        String path = FileUtils.getPath(paintFilePath);
        String paintName = FileUtils.getFileName(paintFilePath);

        v.addElement(path + File.separator + paintName.substring(0, paintName.length() - PAINT_SUFFIX.length()) + PIR);
        //System.out.println("path is " + path + File.separator + paintName.substring(0, paintName.length() - PAINT_SUFFIX.length()) + PIR);
        File parent = new File(path);

        if (null != parent) {
            parent = new File(parent.getParent());
            if (null != parent) {
                v.addElement(parent.getPath() + File.separator + DIR_CLUSTS + File.separator
                        + paintName.substring(0, paintName.length() - PAINT_SUFFIX.length()) + PIR);
                //System.out.println("Path is " + parent.getPath() + File.separator + DIR_CLUSTS + File.separator
                //+ paintName.substring(0, paintName.length() - PAINT_SUFFIX.length()) + PIR);
                return v;
            }
        }
        v.addElement(STR_EMPTY);
        return v;
    }

    /**
     * Method declaration
     *
     *
     * @param f
     *
     * @return
     *
     * @see
     */
    public static String getWtsFilePathForMSA(File f) {
        if (0 == PIR_FILE.compareTo(f.getName())) {
            return FileUtils.getPath(f.getPath()) + File.separator + WTS;
        }
        File parent = f.getParentFile();

        if (null != parent) {
            parent = parent.getParentFile();
            if (null == parent) {
                return Constant.STR_EMPTY;
            }
            return parent.getPath() + File.separator + DIR_WTS + File.separator
                    + f.getName().substring(0, f.getName().length() - PIR_FILE.length()) + "mag" + WTS;
        }
        return Constant.STR_EMPTY;
    }

    public static String getSfToSeqFileName(String directory, String id) {
        return FileUtils.appendFileToPath(directory, id + NAME_SF_SEQ);
    }

    public static String getSfToOntologyFileName(String directory, String id) {
        return FileUtils.appendFileToPath(directory, id + NAME_SF_TO_ONTOLOGY);
    }

    public static String getAttrFileName(String directory, String id) {
        return FileUtils.appendFileToPath(directory, id + NAME_ATTRIB);
    }

    public static String getPfamDomain(LibrarySettings librarySettings) {
        if (null == librarySettings) {
            return null;
        }
        EntryType et = librarySettings.getEntryType();
        String bookName = librarySettings.getBookName();

        if (null == et || null == et.getEntryType() || 0 == et.getEntryType().length()) {
            return librarySettings.getLibURL() + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + bookName + WEB_DIR_SEPARATOR + PREFIX_PFAM + DOT_TSV;
        } else {
            return librarySettings.getLibURL() + WEB_DIR_SEPARATOR + DIR_BOOKS + WEB_DIR_SEPARATOR
                    + bookName + WEB_DIR_SEPARATOR + et.getEntryType() + WEB_DIR_SEPARATOR + DIR_TREE + WEB_DIR_SEPARATOR
                    + bookName + DOT + et.getEntryType() + DOT_TSV;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param args
     *
     * @see
     */
    public static void main(String[] args) {

        // System.out.println(FileNameGenerator.getAttribForBETE("mytree.bete"));
        // System.out.println(FileNameGenerator.getAttribForBETE("dfsd"));
        EntryType et = new EntryType();
        et.setEntryType("orig");
        LibrarySettings ls = new LibrarySettings("PTHR10000", et, Constant.STR_EMPTY, "http://23423/com/devupl4.0/");

        String msaFiles[] = getMSAFilesForPaint(ls);
        System.out.println("Files are " + msaFiles[0] + " " + msaFiles[1]);
//		LibrarySettings ls = new LibrarySettings("CF10000", null, "Temp", "http://localhost:8080/treeViewer/Library", true);
//		String beteName = FileNameGenerator.getBETEPathName(ls);
//		String  path = FileUtils.getURLPath(beteName);
        //String[] fileNames = getContents(beteName);
        // System.out.println(getMSA(ls));
    }

}
