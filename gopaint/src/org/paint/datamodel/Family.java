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
package org.paint.datamodel;

import edu.usc.ksom.pm.panther.paintCommon.Comment;
import edu.usc.ksom.pm.panther.paintCommon.DataTransferObj;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import edu.usc.ksom.pm.panther.paintCommon.MSA;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.SaveBookInfo;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import org.paint.dataadapter.FamilyAdapter;
import org.paint.dataadapter.PantherAdapter;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;
import org.bbop.framework.GUIManager;
import org.paint.config.Preferences;
import org.paint.dataadapter.PantherServer;
import org.paint.main.PaintManager;

public class Family implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

//	private Vector<Vector<String>> rows;
    /**
     * Pretty much the straight content of the files or the database transfer
     * This is the raw data which is parsed
     */
    private String treeStrings[];
//	private String    sfAnInfo[];
//	private String    attrTable[];
    private String[] msa_content;
    private String[] wts_content;
//	private String[] txt_content;
    private HashMap<String, Node> nodeLookup;
    private StringBuffer nodeInfoBuf;

    private String familyID;
    private String name;
    private Comment familyComment;
    private  LoadFamilyDomain familyDomainWorker;

//	private RawComponentContainer rcc;
    
    public static final String MSG_ERROR = "Error";
    public static final String MSG_ERROR_CANNOT_ACCESS_INFORMATION_FROM_SERVER = "Error cannot access information from server";    
    public static final String MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_COMMENT = "Unable to retrieve comment information from server";
    public static final String MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_FAMILY_NAME = "Unable to retrieve family name from server";
    public static final String MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_FAMILY_DOMAIN = "Unable to retrieve family domain info from server";
    public static final String MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_TREE = "Unable to retrieve tree info from server";    
    public static final String MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_MSA = "Unable to retrieve MSA info from server"; 
    public static final String MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_NODE_INFO = "Unable to retrieve node info from server";
    public static final String MSG_ERROR_CANNOT_SAVE_BOOK = "Unable to save information";    
    
    public Family() {
    }

    /**
     * Method declaration
     *
     *
     * @param familyID
     *
     * @see
     */
    public boolean loadFamily(String familyID) {

        FamilyAdapter adapter = new PantherAdapter(familyID);

        ExecutorService executor = Executors.newFixedThreadPool(5);

        
        LoadTreeNodes nodesWorker = new LoadTreeNodes(familyID);
        executor.execute(nodesWorker);

        LoadFamilyName familyNameWorker = new LoadFamilyName(familyID);
        executor.execute(familyNameWorker);
        
       
        
        LoadFamilyComment familyCommentWorker = new LoadFamilyComment(familyID);
        executor.execute(familyCommentWorker);

        LoadTreeStrings treeStringsWorker = new LoadTreeStrings(familyID);
        executor.execute(treeStringsWorker);

        LoadMSA msaWorker = new LoadMSA(familyID);
        executor.execute(msaWorker);
        
        loadDomain(familyID);
 
        executor.shutdown();
        
        // Wait until all threads finish
        while (!executor.isTerminated()) {

        }
        treeStrings = treeStringsWorker.treeStrings;
        if (null == treeStrings) {
            this.familyID = null;
        }
        nodeLookup = nodesWorker.nodeLookup;
        nodeInfoBuf = nodesWorker.errorBuf;
        
        if (null == nodeLookup) {
            this.familyID = null;
        }
        this.name = familyNameWorker.familyName;


        MSA msa = msaWorker.msa;
        if (null != msa) {
            this.msa_content = msa.getMsaContents();
            this.wts_content = msa.getWeightsContents();
        }
        this.familyComment  = familyCommentWorker.familyComment;
        this.familyID = familyID;
        
        // Force garbage collection after a new book is opened
        System.gc();
        return isLoaded();
    }
    
    public void loadDomain(String familyID){
        ExecutorService domainExecutor = Executors.newFixedThreadPool(1);      // Do domain processing in a separate thread. The system can process the book independently
                                                                               // of the domain information. When the domain information becomes available, update as necessary
       
        familyDomainWorker = new LoadFamilyDomain(familyID);
        domainExecutor.execute(familyDomainWorker);         
        domainExecutor.shutdown();        
    }
    
    public String getDomainFamId() {
        if (null == familyDomainWorker) {
            return null;
        }
        return familyDomainWorker.familyId;
    }
    
    public HashMap<String, HashMap<String, ArrayList<Domain>>> getNodeToDomainLookup(String famId) {
        if (null == familyDomainWorker || null == famId) {
            return null;
        }
        if (false == famId.equals(familyDomainWorker.familyId)) {
            return null;
        }
        return familyDomainWorker.nodeToDomainLookup;
    }

    public boolean isLoaded() {
        return familyID != null;
    }

//	public Vector<Vector<String>> getRows() {
//		return rows;
//	}
//
//	public void setRows(Vector<Vector<String>> rows) {
//		this.rows = rows;
//	}
    public String[] getMSAcontent() {
        return msa_content;
    }

    public void setMSAcontent(String[] msa_content) {
        this.msa_content = msa_content;
    }

    public String[] getWtsContent() {
        return wts_content;
    }

    public void setWtsContent(String[] wts_content) {
        this.wts_content = wts_content;
    }


//	public String[] getTxtContent() {
//		return txt_content;
//	}
//	
//	public void setTxtContent(String[] txt_content) {
//		this.txt_content = txt_content;
//	}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Comment getFamilyComment() {
        return familyComment;
    }

    public void setFamilyComment(Comment familyComment) {
        this.familyComment = familyComment;
    }
    

    public String getFamilyID() {
        return familyID;
    }
//
//	public void setFamilyID(String familyID) {
//		this.familyID = familyID;
//	}

    public String[] getTreeStrings() {
        return treeStrings;
    }

    public HashMap<String, Node> getNodeLookup() {
        return nodeLookup;
    }

    public StringBuffer getNodeInfoBuf() {
        return nodeInfoBuf;
    }

//	public void setTreeStrings(String[] treeStrings) {
//		this.treeStrings = treeStrings;
//	}
//	public String[] getSfAnInfo() {
//		return sfAnInfo;
//	}
//
//	public void setSfAnInfo(String[] sfAnInfo) {
//		this.sfAnInfo = sfAnInfo;
//	}
//
//	public String[] getAttrTable() {
//		return attrTable;
//	}
//
//	public void setAttrTable(String[] attrTable) {
//		this.attrTable = attrTable;
//	}
//	public RawComponentContainer getRCC() {
//		return rcc;
//	}
//
//	public void setRCC(RawComponentContainer rcc) {
//		this.rcc = rcc;
//		if (rcc != null) {
//			setFamilyID(rcc.getBook());
//			setName(rcc.getName());
//		}
//	}
    
    public String saveBookToDatabase(SaveBookInfo sbi) {
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
        System.out.println("Start of save execution " + df.format(new java.util.Date(System.currentTimeMillis())));
            DataTransferObj dto = new DataTransferObj();
            dto.setVc(PaintManager.inst().getVersionContainer());
            dto.setObj(sbi);
            DataTransferObj serverOutput = PantherServer.inst().saveBook(Preferences.inst().getPantherURL(), dto);
                if (null == serverOutput) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_COMMENT, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
//                    System.exit(-1);
                }
                StringBuffer sb = serverOutput.getMsg();
                if (null != sb && 0 != sb.length()) {
                    JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_ERROR, JOptionPane.ERROR_MESSAGE);
//                    System.exit(-1);
                }             
            String saveInfo = (String)serverOutput.getObj();
        System.out.println("End of save execution " + df.format(new java.util.Date(System.currentTimeMillis())));
        return saveInfo;
    }
    
    public class LoadFamilyComment implements Runnable {
        private final String familyId;
        public Comment familyComment = null;

        LoadFamilyComment(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get comment execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));
            DataTransferObj dto = new DataTransferObj();
            dto.setVc(PaintManager.inst().getVersionContainer());
            dto.setObj(familyId);
            DataTransferObj serverOutput = PantherServer.inst().getFamilyComment(Preferences.inst().getPantherURL(), dto);
            if (null == serverOutput) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_COMMENT, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            StringBuffer sb = serverOutput.getMsg();
            if (null != sb && 0 != sb.length()) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }           
            familyComment = (Comment)serverOutput.getObj();
            System.out.println("End of get comment execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
        }
    }
    
    public class LoadFamilyName implements Runnable {

        private final String familyId;
        public String familyName = null;

        LoadFamilyName(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get family name  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));
            DataTransferObj dto = new DataTransferObj();
            dto.setVc(PaintManager.inst().getVersionContainer());
            dto.setObj(familyId);            
            DataTransferObj serverOutput = PantherServer.inst().getFamilyName(Preferences.inst().getPantherURL(), dto);
            if (null == serverOutput) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_FAMILY_NAME, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            StringBuffer sb = serverOutput.getMsg();
            if (null != sb && 0 != sb.length()) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }             
            familyName = (String)serverOutput.getObj();
            System.out.println("End of get family name execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
        }
    }
    
    public class LoadFamilyDomain implements Runnable {

        private final String familyId;
        public HashMap<String, HashMap<String, ArrayList<Domain>>> nodeToDomainLookup;

        LoadFamilyDomain(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
//            String fileName = "C:\\usc\\svn\\new_panther\\curation\\paint\\gopaint\\trunk\\gopaint\\" + familyId + ".ser";
//            Object o = null;
//            ObjectInputStream objectinputstream = null;
//            try {
//                FileInputStream streamIn = new FileInputStream(fileName);
//                objectinputstream = new ObjectInputStream(streamIn);
//                o = (HashMap<String, HashMap<String, ArrayList<Domain>>>) objectinputstream.readObject();
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (objectinputstream != null) {
//                    try {
//                        objectinputstream.close();
//                    }
//                    catch(Exception e) {
//                        
//                    }
//                }
//            }
//            
//            if (null != o) {
//                nodeToDomainLookup = (HashMap<String, HashMap<String, ArrayList<Domain>>>)o;
//                PaintManager.inst().handleDomainInfo(this.familyId, nodeToDomainLookup);
//                return;
//            }
            
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get family domain  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));
            DataTransferObj dto = new DataTransferObj();
            dto.setVc(PaintManager.inst().getVersionContainer());
            dto.setObj(familyId);            
            DataTransferObj serverOutput = PantherServer.inst().getFamilyDomain(Preferences.inst().getPantherURL(), dto);
            if (null == serverOutput) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_FAMILY_DOMAIN, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            StringBuffer sb = serverOutput.getMsg();
            if (null != sb && 0 != sb.length()) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }             
            nodeToDomainLookup = (HashMap<String, HashMap<String, ArrayList<Domain>>>)serverOutput.getObj();
            System.out.println("End of get family domain execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));
            PaintManager.inst().handleDomainInfo(this.familyId, this.nodeToDomainLookup);
//            ObjectOutputStream oos = null;
//            FileOutputStream fout = null;
//            try {
//                fout = new FileOutputStream(fileName, true);
//                oos = new ObjectOutputStream(fout);
//                oos.writeObject(nodeToDomainLookup);
//                
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            } finally {
//                if (oos != null) {
//                    try {
//                        oos.close();
//                    }
//                    catch(Exception e) {
//                        
//                    }
//                }
//            } 
        }
    }    

    public class LoadTreeStrings implements Runnable {

        private final String familyId;
        public String treeStrings[] = null;

        LoadTreeStrings(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get tree  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));
            DataTransferObj dto = new DataTransferObj();
            dto.setVc(PaintManager.inst().getVersionContainer());
            dto.setObj(familyId);
            DataTransferObj serverOutput = PantherServer.inst().getTree(Preferences.inst().getPantherURL(), dto);            
            if (null == serverOutput) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_TREE, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            StringBuffer sb = serverOutput.getMsg();
            if (null != sb && 0 != sb.length()) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }             
            treeStrings = (String[])serverOutput.getObj();
            System.out.println("End of get tree  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
        }
    }

    public class LoadMSA implements Runnable {

        private final String familyId;
        public MSA msa = null;

        LoadMSA(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get msa  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
            DataTransferObj dto = new DataTransferObj();
            dto.setVc(PaintManager.inst().getVersionContainer());
            dto.setObj(familyId);
            DataTransferObj serverOutput = PantherServer.inst().getMSA(Preferences.inst().getPantherURL(), dto);
            if (null == serverOutput) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_MSA, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            StringBuffer sb = serverOutput.getMsg();
            if (null != sb && 0 != sb.length()) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            msa = (MSA)serverOutput.getObj();
//            msa = null;
            System.out.println("End of get msa  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
        }
    }

    public class LoadTreeNodes implements Runnable {

        private final String familyId;
        public HashMap<String, Node> nodeLookup = null;
        public StringBuffer errorBuf = null;

        LoadTreeNodes(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get nodes execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));             
//            System.out.println("Start of get msa  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
            DataTransferObj serverInput = new DataTransferObj();
            serverInput.setVc(PaintManager.inst().getVersionContainer());
            serverInput.setObj(familyId);
            DataTransferObj serverOutput = PantherServer.inst().getNodes(Preferences.inst().getPantherURL(), serverInput);
            if (null == serverOutput) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), MSG_ERROR_CANNOT_ACCESS_VERSION_INFO_NODE_INFO, MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            StringBuffer sb = serverOutput.getMsg();
            if (null != sb && 0 != sb.length()) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_ERROR, JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
            Vector outputInfo = (Vector)serverOutput.getObj();
            nodeLookup = (HashMap<String, Node>)outputInfo.get(0);
            errorBuf = (StringBuffer)outputInfo.get(1);
//            System.out.println(errorBuf);
//            try {
//                FileInputStream fin = new FileInputStream("C:\\Temp\\new_paint\\" + familyId + ".ser");
//                ObjectInputStream ois = new ObjectInputStream(fin);
//                nodeLookup = (HashMap<String, Node>) ois.readObject();
//                return;
//            }
//            catch (Exception e) {
//                nodeLookup = PantherServer.inst().getNodes(Preferences.inst().getPantherURL(), familyId);
//                try {
//                    FileOutputStream fout = new FileOutputStream("C:\\Temp\\new_paint\\" + familyId + ".ser");
//                    ObjectOutputStream oos = new ObjectOutputStream(fout);
//                    oos.writeObject(nodeLookup);
//                }
//                catch (Exception ex) {
//
//                }
//            }
//            nodeLookup = PantherServer.inst().getNodes(Preferences.inst().getPantherURL(), familyId);
            System.out.println("End of get nodes info for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));  
            // Add qualifiers for testing purposes
//            System.out.println("Going to add qualifiers for testing purposes");
//            if ("PTHR10000".equals(familyId)) {
//                Node n0 = nodeLookup.get("PTHR10000:AN0");
//                ArrayList<Annotation> annotList = n0.getVariableInfo().getGoAnnotationList();
//                for (Annotation a: annotList) {
//                    if (true == "GO:0005737".equals(a.getGoTerm())) {
//                        HashSet<Annotation> withs = a.getAnnotationDetail().getWithAnnotSet();
//                        for (Annotation with: withs) {
//                            with.getEvidence().setEvidenceCode("IEA");
//                        
//                            break;
//                        }
//                    }
//                }
//            }
//                Node n240 = nodeLookup.get("PTHR10000:AN240");
//                if (null != n240) {
//                    NodeVariableInfo nvi = n240.getVariableInfo();
//                    if (nvi != null) {
//                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//                    for (Annotation a: annotList) {
//                        String goId = a.getGoTerm();
//                        // Sugar phosphosphatase activity - add a contributes-to qualifier
//                        if ("GO:0050308".equals(goId)) {
//                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
//                            if (null == qualifierSet) {
//                                qualifierSet = new HashSet<Qualifier>();
//                                a.setQualifierSet(qualifierSet);
//                            }
//                            Qualifier q = new Qualifier();
//                            q.setText(q.QUALIFIER_CONTRIBUTES_TO);
//                            a.addQualifier(q);
//                        }
//                        //phosphatase activity = add my own made up qualifier
//                        if ("GO:0016791".equals(goId)) {
//                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
//                            if (null == qualifierSet) {
//                                qualifierSet = new HashSet<Qualifier>();
//                                a.setQualifierSet(qualifierSet);
//                            }
//                            Qualifier q = new Qualifier();
//                            q.setText("A positive qualifier");
//                            a.addQualifier(q);
//                        }
//                        // magensium ion binding - add a NOT qualifier
//                        if ("GO:0000287".equals(goId)) {
//                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
//                            if (null == qualifierSet) {
//                                qualifierSet = new HashSet<Qualifier>();
//                                a.setQualifierSet(qualifierSet);
//                            }
//                            Qualifier q = new Qualifier();
//                            q.setText(Qualifier.QUALIFIER_NOT);
//                            a.addQualifier(q);
//                        }
//                    }
//                    }
//                }
//                
//                
//                
//                Node n251 = nodeLookup.get("PTHR10000:AN251");
//                if (null != n251) {
//                    NodeVariableInfo nvi = n251.getVariableInfo();
//                    if (nvi != null) {
//                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//                    for (Annotation a: annotList) {
//                        String goId = a.getGoTerm();
//                        // Sugar phosphosphatase activity - add a contributes-to qualifier
//                        if ("GO:0050308".equals(goId)) {
//                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
//                            if (null == qualifierSet) {
//                                qualifierSet = new HashSet<Qualifier>();
//                                a.setQualifierSet(qualifierSet);
//                            }
//                            Qualifier q = new Qualifier();
//                            q.setText("some positive qualifier that is valid for all aspects");
//                            a.addQualifier(q);
//                        }
//                        //phosphatase activity = add my own made up qualifier
//                        if ("GO:0016791".equals(goId)) {
//                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
//                            if (null == qualifierSet) {
//                                qualifierSet = new HashSet<Qualifier>();
//                                a.setQualifierSet(qualifierSet);
//                            }
//                            Qualifier q = new Qualifier();
//                            q.setText("A positive qualifier");
//                            a.addQualifier(q);
//                        }
//                        // magensium ion binding - add a NOT qualifier
////                        if ("GO:0000287".equals(goId)) {
////                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
////                            if (null == qualifierSet) {
////                                qualifierSet = new HashSet<Qualifier>();
////                                a.setQualifierSet(qualifierSet);
////                            }
////                            Qualifier q = new Qualifier();
////                            q.setText(Qualifier.QUALIFIER_NOT);
////                            a.addQualifier(q);
////                        }
//                    }
//                    }
//                }
//                
//                
//                Node n113 = nodeLookup.get("PTHR10000:AN113");
//                if (null != n113) {
//                    NodeVariableInfo nvi = n113.getVariableInfo();
//                    if (nvi != null) {
//                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//                    for (Annotation a: annotList) {
//                        String goId = a.getGoTerm();
//                        // Sugar phosphosphatase activity - add a contributes-to qualifier
//                        if ("GO:0016791".equals(goId)) {
//                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
//                            if (null == qualifierSet) {
//                                qualifierSet = new HashSet<Qualifier>();
//                                a.setQualifierSet(qualifierSet);
//                            }
//                            Qualifier q = new Qualifier();
//                            q.setText(Qualifier.QUALIFIER_NOT);
//                            a.addQualifier(q);
//                        }
//                    }
//                    }
//                }                 
//                Node n117 = nodeLookup.get("PTHR10000:AN117");
//                if (null != n117) {
//                    NodeVariableInfo nvi = n117.getVariableInfo();
//                    if (nvi != null) {
//                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//                    for (Annotation a: annotList) {
//                        String goId = a.getGoTerm();
//                        // Sugar phosphosphatase activity - add a contributes-to qualifier
//                        if ("GO:0016791".equals(goId)) {
//                            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
//                            if (null == qualifierSet) {
//                                qualifierSet = new HashSet<Qualifier>();
//                                a.setQualifierSet(qualifierSet);
//                            }
//                            Qualifier q = new Qualifier();
//                            q.setText(Qualifier.QUALIFIER_NOT);
//                            a.addQualifier(q);
//                        }
//                    }
//                    }
//                }                
//                
//                
//            }
        }
    }
}
