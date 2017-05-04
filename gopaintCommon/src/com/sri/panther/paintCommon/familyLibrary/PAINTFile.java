package com.sri.panther.paintCommon.familyLibrary;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.FileUtils;

import java.io.FileInputStream;
import java.io.IOException;

import java.io.FileOutputStream;

import java.io.InputStream;

import java.net.URL;

import java.util.Properties;

import org.apache.log4j.Logger;


public class PAINTFile {
    protected String fileName;
    protected Properties prop = null;
    protected static Logger log = Logger.getLogger(PAINTFile.class.getName());
    
    protected static final String FILE_TREE = "tree";
    protected static final String FILE_SFAN = "sfan";
    protected static final String FILE_ATTR = "attr";
    
    protected String PAINTFilePath;
    
    protected static final String MSG_ERROR_LOADING_PAINT_FILE = "Error loading paint file ";
    protected static final String MSG_ERROR_WRITING_FILE = "Error writing file ";

    
    
    public PAINTFile (String paintFileName) {

        PAINTFilePath = FileUtils.getPath(paintFileName);
        this.fileName = FileUtils.getFileName(paintFileName);
        
        prop = new Properties();
        setProperty(FILE_TREE, FileNameGenerator.getTreeForPAINT(fileName));
        setProperty(FILE_ATTR, FileNameGenerator.getAttribForPAINT(fileName));
        setProperty(FILE_SFAN, FileNameGenerator.getSfAnForPAINT(fileName));
    }
    
    private PAINTFile() {
        
    }
    
    public static PAINTFile readPAINTFile(String fileName) {
        PAINTFile pf = new PAINTFile();
        //pf.fileName = fileName;
        
        
        
        if (false == FileUtils.validPath(fileName)){
          return null;
        }
        
        // Read file into property object and set member variables
        try {
            FileInputStream in = new FileInputStream(fileName);
            pf.prop = new Properties();
            pf.prop.loadFromXML(in);
            in.close();
     
            pf.PAINTFilePath = FileUtils.getPath(fileName);
            
            pf.fileName = FileUtils.getFileName(fileName);
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(MSG_ERROR_LOADING_PAINT_FILE + fileName);
            
        }
        
        return pf;

    }
    
    public static PAINTFile readPAINTFileURL(String url) {
        PAINTFile pf = new PAINTFile();

        
        
        
        // Read file into property object and set member variables
        try {
            URL aUrl = new URL(url);
            InputStream in = aUrl.openStream();
            pf.prop = new Properties();
            pf.prop.loadFromXML(in);
            in.close();
     
            pf.PAINTFilePath = FileUtils.getURLPath(url);
            
            pf.fileName = aUrl.getFile();

        }
        catch (Exception e) {
            e.printStackTrace();
            log.error(MSG_ERROR_LOADING_PAINT_FILE + url);
            
        }
        
        return pf;

    }
    
    protected void setProperty(String key, String value) {
        prop.setProperty(key, value);
    }
    
    
    public boolean savePAINT(String treeStr[], String attr[], String sfAn[]) {
        Properties tmp = new Properties();
        if (null != treeStr) {
            String treeFileName = prop.getProperty(FILE_TREE);
            if (null == treeFileName) {
                treeFileName = FileNameGenerator.getTreeForPAINT(fileName);
                setProperty(FILE_TREE, treeFileName);
            }
            tmp.put(FILE_TREE, treeFileName);
            String completePath = FileUtils.appendFileToPath(PAINTFilePath, treeFileName);
            try {
				FileUtils.writeFile(completePath, treeStr);
			} catch (IOException e) {
				return false;
			}
        }
        if (null != sfAn) {
            String sfAnFileName = prop.getProperty(FILE_SFAN);
            if (null == sfAnFileName) {
                sfAnFileName = FileNameGenerator.getSfAnForPAINT(fileName);
                setProperty(FILE_SFAN, sfAnFileName);
            }
            tmp.put(FILE_SFAN, sfAnFileName);
            String completePath = FileUtils.appendFileToPath(PAINTFilePath, sfAnFileName);
            try {
				FileUtils.writeFile(completePath, sfAn);
			} catch (IOException e) {
				log.error(MSG_ERROR_WRITING_FILE + completePath); 
				return false;
			}
        }
        if (null != attr) {
            String attrName = prop.getProperty(FILE_ATTR);
            if (null == attrName) {
                attrName = FileNameGenerator.getAttribForPAINT(fileName);
                setProperty(FILE_ATTR, attrName);
            }
            tmp.put(FILE_ATTR, attrName);
            String completePath = FileUtils.appendFileToPath(PAINTFilePath, attrName);
            try {
				FileUtils.writeFile(completePath, attr);
			} catch (IOException e) {
                log.error(MSG_ERROR_WRITING_FILE + completePath);
                return false;
			}
         }
        
        
        // Now write back the paint file
        String completePath = FileUtils.appendFileToPath(PAINTFilePath, fileName);
        try {
            FileOutputStream out = new FileOutputStream(completePath);
            tmp.storeToXML(out, Constant.STR_EMPTY);
            out.close();
            return true;
        }
        catch(Exception e) {
            e.printStackTrace();
            log.error(MSG_ERROR_WRITING_FILE + completePath);
            return false;
        }
    }


    public String getTreeFileName() {
        return prop.getProperty(FILE_TREE);
    }

    public String getSfanFileName() {
        return prop.getProperty(FILE_SFAN);
    }

    public String getAttrFileName() {
        return prop.getProperty(FILE_ATTR);
    }

    public String getPAINTFilePath() {
        return PAINTFilePath;
    }
    
    public String getPAINTFileName() {
        return fileName;
    }
    
    
    public static void main(String[] args){
        PAINTFile pf = new PAINTFile("/Users/muruganu/svnTest/paint/data/test.paint");

        pf.savePAINT(new String[]{"((seq1,seq2)"}, new String[]{"gi name"} , new String[]{"sf1  an0"});
        
    }
}