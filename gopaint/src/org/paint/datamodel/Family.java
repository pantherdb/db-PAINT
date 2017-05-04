/**
 * Copyright 2016 University Of Southern California
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

import edu.usc.ksom.pm.panther.paintCommon.MSA;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.SaveBookInfo;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.paint.dataadapter.FamilyAdapter;
import org.paint.dataadapter.PantherAdapter;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.paint.config.Preferences;
import org.paint.dataadapter.PantherServer;

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

    private String familyID;
    private String name;
    private String familyComment;
//	private RawComponentContainer rcc;

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

        executor.shutdown();
        // Wait until all threads finish
        while (!executor.isTerminated()) {

        }
        treeStrings = treeStringsWorker.treeStrings;
        if (null == treeStrings) {
            this.familyID = null;
        }
        nodeLookup = nodesWorker.nodeLookup;
        if (null == nodeLookup) {
            this.familyID = null;
        }
        this.name = familyNameWorker.familyName;

        MSA msa = msaWorker.msa;
        if (null != msa) {
            this.msa_content = msa.getMsaContents();
            this.wts_content = msa.getWeightsContents();
        }
        this.familyID = familyID;
        this.familyComment  = familyCommentWorker.familyComment;

        // Force garbage collection after a new book is opened
        System.gc();
        return isLoaded();
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

    public String getFamilyComment() {
        return familyComment;
    }

    public void setFamilyComment(String familyComment) {
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
        String saveInfo = PantherServer.inst().saveBook(Preferences.inst().getPantherURL(), sbi);
        System.out.println("End of save execution " + df.format(new java.util.Date(System.currentTimeMillis())));
        return saveInfo;
    }
    
    public class LoadFamilyComment implements Runnable {
        private final String familyId;
        public String familyComment = null;

        LoadFamilyComment(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get comment execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));            
            familyComment = PantherServer.inst().getFamilyComment(Preferences.inst().getPantherURL(), familyId);
            if (null != familyComment && true == familyComment.isEmpty()) {
                familyComment = null;
            }
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
            familyName = PantherServer.inst().getFamilyName(Preferences.inst().getPantherURL(), familyId);
            System.out.println("End of get family name execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
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
            treeStrings = PantherServer.inst().getTree(Preferences.inst().getPantherURL(), familyId);
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
            msa = PantherServer.inst().getMSA(Preferences.inst().getPantherURL(), familyId);
//            msa = null;
            System.out.println("End of get msa  execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));              
        }
    }

    public class LoadTreeNodes implements Runnable {

        private final String familyId;
        public HashMap<String, Node> nodeLookup = null;

        LoadTreeNodes(String familyId) {
            this.familyId = familyId;
        }

        @Override
        public void run() {
            java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("hh:mm:ss:SSS");
            System.out.println("Start of get nodes execution for " + familyId + " at " + df.format(new java.util.Date(System.currentTimeMillis())));             
            nodeLookup = PantherServer.inst().getNodes(Preferences.inst().getPantherURL(), familyId);
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
