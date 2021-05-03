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
package org.paint.main;

import edu.usc.ksom.pm.panther.paintCommon.TaxonomyHelper;
import com.sri.panther.paintCommon.User;
import edu.usc.ksom.pm.panther.paintCommon.VersionInfo;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.usc.ksom.pm.panther.paint.matrix.MatrixBuilder;
import edu.usc.ksom.pm.panther.paint.matrix.MatrixInfo;
import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationHelper;
import edu.usc.ksom.pm.panther.paintCommon.Comment;
import edu.usc.ksom.pm.panther.paintCommon.Domain;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.SaveBookInfo;
import edu.usc.ksom.pm.panther.paintCommon.VersionContainer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.geneontology.db.model.Term;
import org.obo.datamodel.OBOSession;
import org.paint.dataadapter.GOAdapter;
import org.paint.datamodel.Family;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.DomainChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.ProgressEvent;
import org.paint.gui.evidence.ActionLog;
import org.paint.gui.familytree.TreeModel;
import org.paint.gui.familytree.TreePanel;
import org.paint.gui.matrix.AnnotationMatrix;
import org.paint.gui.msa.MSA;
import org.paint.gui.msa.MSAPanel;
import org.paint.gui.table.GeneTable;
import org.paint.gui.table.GeneTableModel;
import org.paint.util.GeneNodeUtil;

public class PaintManager {

    /**
     *
     */
    private static PaintManager INSTANCE = null;

    private static final long serialVersionUID = 1L;

    private TreePanel tree_pane;
    private GeneTable genes_pane;
    private MSAPanel msa_pane;
    private AnnotationMatrix annot_matrix;

    private HashMap<String, List<GeneNode>> seqIdtoGene;
    private HashMap<String, GeneNode> paintIdtoGene;
    private HashMap<String, GeneNode> DbIdtoGene;
    private HashMap<String, GeneNode> ptnIdtoGene;
    private HashMap<String, GeneNode> GPtoGene;

    private static Hashtable<String, Vector<GeneNode>> origTreeTable; // Subfamily

    // Set when user chooses file from local file system
    private static File currentDirectory;

    private static Family family;

    private static final Logger log = Logger.getLogger(PaintManager.class);

    private static final int go_max = 25;

    /*
     * The GO term root
     */
    private static OBOSession go_root;

    // Static information from server
    private VersionContainer versinContainer;
    private VersionInfo versionInfo;
    private GOTermHelper goTermHelper;
    private TaxonomyHelper taxonHelper;
    private HashSet<String> curatableBookSet;
    private User user;
    private Vector userInfo;
    private HashMap<GeneNode, HashSet<Annotation>> removedAnnotLookup = new HashMap<GeneNode, HashSet<Annotation>>();
    private BrowserLauncher browserLauncher;

    private ArrayList<TermAncestor> termAncestorList;      // termAncestor information user wants to see in matrix
    private MatrixInfo matrixInfo;
    

    private PaintManager() {
        // Exists only to defeat instantiation.
    }

    public static synchronized PaintManager inst() {
        if (INSTANCE == null) {
            INSTANCE = new PaintManager();
        }
        return INSTANCE;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public Family getFamily() {
        return family;
    }

    public void setFamily(Family fam) {
        family = fam;
    }

    public void closeCurrent() {
        GUIManager.getManager().getFrame().setTitle("");
        DirtyIndicator.inst().dirtyGenes(false);

        clearGeneIDs();
        if (null != tree_pane) {
            // Need to add things for listeners                
            tree_pane.removeAll();
            tree_pane.revalidate();
        }
        if (null != genes_pane) {
            // Need to add things for listeners
            genes_pane.removeAll();
            genes_pane.revalidate();
        }

        if (null != msa_pane) {
            msa_pane.removeAll();
            msa_pane.revalidate();
        }

        if (null != annot_matrix) {
            // Need to add things for listeners                
            annot_matrix.removeAll();
            annot_matrix.revalidate();
        }

        family = null;
        termAncestorList = null;
        matrixInfo = null;
        removedAnnotLookup.clear();
    }

    private void clearGeneIDs() {
        if (paintIdtoGene == null) {
            paintIdtoGene = new HashMap<String, GeneNode>();
        } else {
            paintIdtoGene.clear();
        }
        if (seqIdtoGene == null) {
            seqIdtoGene = new HashMap<String, List<GeneNode>>();
        } else {
            seqIdtoGene.clear();
        }
        if (DbIdtoGene == null) {
            DbIdtoGene = new HashMap<String, GeneNode>();
        } else {
            DbIdtoGene.clear();
        }
        if (ptnIdtoGene == null) {
            ptnIdtoGene = new HashMap<String, GeneNode>();
        } else {
            ptnIdtoGene.clear();
        }
        if (GPtoGene == null) {
            GPtoGene = new HashMap<String, GeneNode>();
        } else {
            GPtoGene.clear();
        }
        GO_Util.inst().clearCache();
    }

    public void indexByPaintID(GeneNode node) {
        String an_number = node.getPaintId();
        if (an_number == null || an_number.length() == 0) {
            log.error("Paint ID for node is missing!");
        } else if (paintIdtoGene.get(an_number) != null) {
            log.error("We've already indexed this node by annotation id: " + an_number);
        } else {
            paintIdtoGene.put(an_number, node);
        }
    }

//    public void indexBySeqID(GeneNode node) {
//        indexBySeqID(node, node.getSeqDB(), node.getSeqId());
//    }

//    private void indexBySeqID(GeneNode node, String db, String db_id) {
//        if (db_id != null && db_id.length() > 0) {
//            String key = db + ':' + db_id.toUpperCase();
//            if (db_id.equals("ENSPTRG00000033944")) {
//                log.debug("Give us a moment to follow along");
//            }
//            List<GeneNode> genes = seqIdtoGene.get(key);
//            if (genes == null) {
//                genes = new ArrayList<GeneNode>();
//                seqIdtoGene.put(key, genes);
//            }
//            if (!genes.contains(node)) {
//                genes.add(node);
//            } else {
//                log.debug("Already indexed by seqId " + key);
//            }
//        }
//    }

//    public void indexByDBID(GeneNode node) {
//        String db_name = node.getDatabase();
//        String key = db_name + ':' + node.getDatabaseID().toUpperCase();
//        if (key != null && key.length() > 0) {
//            if (DbIdtoGene.get(key) != null) {
//                log.debug("Already indexed by dbid " + key);
//            } else {
//                DbIdtoGene.put(key, node);
//            }
//        }
//    }

    public void indexByGP(GeneNode node) {
        String db = node.getGeneProduct().getDbxref().getDb_name();
        String db_id = node.getGeneProduct().getDbxref().getAccession();
        if (db_id != null && db_id.length() > 0) {
            GPtoGene.put(db + ':' + db_id, node);
        }
    }

    public void indexNodeByPTN(GeneNode node) {
        String ptn_id = node.getPersistantNodeID();
        ptnIdtoGene.put(ptn_id, node);
    }

//    private void initRefGenome() {
//        List<String[]> go_genes = new ArrayList<String[]>();
//        List<GeneNode> treeNodes = tree_pane.getTerminusNodes();
//        int max = Math.min(go_max, treeNodes.size());
//        for (int i = 0; i < treeNodes.size(); i++) {
//            GeneNode node = treeNodes.get(i);
//            if (node != null && node.isLeaf()) {
//                String[] xref = new String[2];
//                xref[0] = GO_Util.inst().dbNameHack(node.getDatabase());
//                xref[1] = node.getDatabaseID();
//                go_genes.add(xref);
//            } else {
//                log.error("Should never have a null or non-leaf terminus node");
//            }
//            if (go_genes.size() == max) {
//                GO_Util.inst().getGeneProducts(go_genes);
//                go_genes = new ArrayList<String[]>();
//                max = Math.min(go_max, treeNodes.size() - i - 1);
//            }
//        }
//        if (go_genes.size() > 0) {
//            log.debug("Odd have some remaining gene products to fetch");
//            GO_Util.inst().getGeneProducts(go_genes);
//        }
//    }

    public GeneNode getGeneByPTNId(String id) {
        GeneNode node = null;
        if (id != null && ptnIdtoGene != null) {
            node = ptnIdtoGene.get(id);
        }
        if (null == node) {
            System.out.println( "did not find node for " + id);
        }
        return node;
    }

    public GeneNode getGeneByPaintId(String id) {
        if (id.length() == 0) {
            return null;
        }
        GeneNode node = paintIdtoGene.get(id);
        if (node == null && id.startsWith(getFamily().getFamilyID())) {
            id = id.substring(id.indexOf(':') + 1);
            node = paintIdtoGene.get(id);
        }
        return node;
    }

//    public List<GeneNode> getGenesBySeqId(String db, String id) {
//        String key = db + ':' + id.toUpperCase();
//        List<GeneNode> node = seqIdtoGene.get(key);
//        String family = getFamily().getFamilyID();
//        if (node == null && id.startsWith(family)) {
//            id = id.substring(id.indexOf('_') + 1);
//            node = seqIdtoGene.get(id.toUpperCase());
//        }
//        return node;
//    }

//    public GeneNode getGeneByDbId(String db, String id) {
//        String db_name = GO_Util.inst().dbNameHack(db);
//        String key = db_name + ':' + id.toUpperCase();
//        return DbIdtoGene.get(key);
//    }

    public GeneNode getGeneByGP(String db, String db_id) {
        GeneNode node = null;
        if (db != null && db_id != null && GPtoGene != null) {
            node = GPtoGene.get(db + ':' + db_id);
        }
        return node;
    }

    public void setGeneTable(GeneTable gp) {
        genes_pane = gp;
    }

    public void setTreePane(TreePanel tree) {
        this.tree_pane = tree;
    }

    public void setMSAPane(MSAPanel msa_pane) {
        this.msa_pane = msa_pane;
    }

    public GeneTable getGeneTable() {
        return genes_pane;
    }

    public int getTopMargin() {
        int margin = genes_pane.getTableHeader().getHeight();
        if (margin > 0) {
            return margin;
        } else {
            return genes_pane.getRowHeight();
        }
    }

    public int getRowHeight() {
        return genes_pane.getRowHeight();
    }

    private void addPruned(ArrayList<Node> prunedList, GeneNode gNode) {
        if (null == gNode) {
            return;
        }
        if (gNode.isPruned() && false == prunedList.contains(gNode.getNode())) {
            prunedList.add(gNode.getNode());
            return;
        }
        List<GeneNode> children = gNode.getChildren();
        if (null != children) {
            for (GeneNode child : children) {
                addPruned(prunedList, child);
            }
        }
    }

    public ArrayList<Node> getPrunedList() {
        ArrayList<Node> prunedList = new ArrayList<Node>();
        addPruned(prunedList, tree_pane.getRoot());
        return prunedList;
    }

    public ArrayList<Annotation> getAnnotatedList() {
        ArrayList<Annotation> annotationList = new ArrayList<Annotation>();
        List<GeneNode> allNodes = tree_pane.getAllNodes();
        for (GeneNode gNode : allNodes) {
            Node n = gNode.getNode();
            if (true == GeneNodeUtil.inPrunedBranch(gNode)) {
                continue;
            }
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            for (Annotation a : annotList) {
                if (AnnotationHelper.isDirectAnnotation(a)) {
                    annotationList.add(a);
                }
//                String code = a.getSingleEvidenceCodeFromSet();
//                GeneNode gn = PaintManager.inst().getGeneByPTNId(n.getStaticInfo().getPublicId());
//                if (edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBD.equals(code) ||
//                        (edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR.equals(code) && false == gn.isLeaf()) ||
//                        edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD.equals(code)||
//                        edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_TCV.equals(code)) {
////                        ArrayList<Annotation> astdList = saveNodeLookup.get(n);
////                        if (null == astdList) {
////                            astdList = new ArrayList<Annotation>();
////                            saveNodeLookup.put(n, astdList);
////                        }
////                        astdList.add(a);
//                    annotationList.add(a);
////                    Annotation childAnnot = a.getChildAnnotation();
////                    if (null != childAnnot && edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA.equals(childAnnot.getSingleEvidenceCodeFromSet()) && (edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR.equals(code) || edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD.equals(code))) {
////                        //astdList.add(childAnnot);
////                        annotationList.add(childAnnot);
////                    }
//
//                }
//                else if (edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA.equals(code)) {
//                    if (true == AnnotationUtil.isIBAFromIBDAndForIKRorIRD(a, gNode)) {
//                        annotationList.add(a);
//                    }
//                }
            }
        }
        return annotationList;
    }

    public boolean saveCurrent() {

        // Get list of pruned and annotations
        ArrayList<Node> prunedList = getPrunedList();
        ArrayList<Annotation> annotationList = getAnnotatedList();

        // Ask user about save status
        int n = 3;
        Object[] options = {
            "Save, unlock & set curated", "Save", "Save & unlock", "Cancel"
        };

        n = JOptionPane.showOptionDialog(GUIManager.getManager().getFrame(), "Save Options", "", JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[3]);

        // Return if cancel option or window close option is selected
        if ((n > 2) || (n < 0)) {
            return false;
        }

        SaveBookInfo sbi = new SaveBookInfo();
        sbi.setUser(user);
        sbi.setBookId(getFamily().getFamilyID());
        sbi.setPrunedList(prunedList);
        sbi.setAnnotationList(annotationList);
        sbi.setComment(getFamily().getFamilyComment());
        sbi.setFamilyName(getFamily().getName());
        sbi.setSaveStatus(new Integer(n));

//            // Save locally for testing purposes
//                try {
//                FileOutputStream fout = new FileOutputStream("C:\\Temp\\new_paint\\" + getFamily().getFamilyID() + "_to_save.ser");
//                ObjectOutputStream oos = new ObjectOutputStream(fout);
//                oos.writeObject(sbi);
//                oos.flush();
//            } catch (Exception ex) {
//
//            }        
        String saveStatus = getFamily().saveBookToDatabase(sbi);
        if (null == saveStatus || false == saveStatus.isEmpty()) {
            String errMsg = saveStatus;
            if (null == errMsg) {
                errMsg = "Unable to save information to server";
            }
            errMsg += ".  Will attempt to save locally.  Please send locally saved file to system administrator for analysis and saving.";
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), errMsg, "Save Error", JOptionPane.ERROR_MESSAGE);
            JFileChooser chooser = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("Serialized file", "ser");
            chooser.setFileFilter(filter);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setCurrentDirectory(PaintManager.inst().getCurrentDirectory());
            int returned = chooser.showDialog(GUIManager.getManager().getFrame(), "Save");
            if (JFileChooser.APPROVE_OPTION == returned) {
                File f = chooser.getSelectedFile();
                if (null != f) {
                    try {
                        FileOutputStream fout = new FileOutputStream(f);
                        ObjectOutputStream oos = new ObjectOutputStream(fout);
                        oos.writeObject(sbi);
                        oos.flush();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Error saving file", "Save Error", JOptionPane.ERROR_MESSAGE);
                        DirtyIndicator.inst().setAnnotated(false);
                        return true;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Changes not saved", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
            DirtyIndicator.inst().setAnnotated(false);
            return true;
        }
        JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Book has been saved", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
        DirtyIndicator.inst().setAnnotated(false);
        return true;
    }
    
    public void handleDomainInfo(String famId, HashMap<String, HashMap<String, ArrayList<Domain>>> nodeToDomainLookup) {
        if (null == family) {
            return;
        }
        String curId = family.getFamilyID();
        if (null != curId && true == curId.equals(famId)) {
            if (null == msa_pane) {
                return;
            }
            msa_pane.handleDomainData(nodeToDomainLookup);
            EventManager.inst().fireDomainChangeEvent(new DomainChangeEvent(this));
        }
    }

    /**
     * Method declaration
     *
     * @param familyID
     * @param useServer
     *
     * @see
     */
    public void openNewFamily(String familyID) {
        closeCurrent();
        family = new Family();
        boolean success = family.loadFamily(familyID);
        if (success) {
            setTitle();

            String progressMessage = "Initializing tree, attributes and MSA";
            fireProgressChange(progressMessage, 0, ProgressEvent.Status.START);
            
            if (null == family.getTreeStrings() || 0 == family.getTreeStrings().length || null == family.getNodeLookup() || 0 == family.getNodeLookup().size()) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Please contact administrator about book " + familyID, "Failed to load information for book", JOptionPane.ERROR_MESSAGE);
                closeCurrent();
                return;
            }

            // Parse file and create tree
            GeneNode root = GeneNodeUtil.inst().parseTreeData(family.getTreeStrings(), family.getNodeLookup(), familyID);

            TreeModel treeModel = null;
            if (root != null) {
                treeModel = new TreeModel(root);
                tree_pane.setTreeModel(treeModel);
            } else {
                System.out.println("Could not parse data");
                closeCurrent();
                return;
            }

            GeneTableModel geneTblModel = new GeneTableModel(tree_pane.getTerminusNodes());
            genes_pane.setModel(geneTblModel);

            if (family.getMSAcontent() != null) {
                progressMessage = "Initializing multiple sequence alignment";
                fireProgressChange(progressMessage, 50, ProgressEvent.Status.START);
                MSA msa = new MSA(family.getMSAcontent(), family.getWtsContent(), family.getNodeToDomainLookup(familyID));
                msa_pane.setModel(msa);
            }

            int nodes = treeModel.getNumNodes();
            if (0 == nodes) {
                nodes = 1;
            }
            int progress_inc = 100 / nodes;

            progressMessage = "Initializing evidence log";
            fireProgressChange(progressMessage, progress_inc * 2, ProgressEvent.Status.START);
            //EvidenceAdapter.importEvidence(path);
            ActionLog.inst().clearLog();

            progressMessage = "Initializing GO experimental annotations";
            fireProgressChange(progressMessage, progress_inc, ProgressEvent.Status.START);
//			boolean connected = InternetChecker.getInstance().isConnectionPresent();
//			if (connected) {
//				initRefGenome();
//			} else {
//				// load experimentals from the local file
//				GafAdapter.importExpAnnotations(path);
//			}
//
//			// Keep a copy of the latest subfamily to sequence information
//			// loaded from database.
//			// This is to check when trying to determine if
//			// tree should be saved when modifying subfamily evidence.
//			if (null == origTreeTable) {
//				origTreeTable = SubFamilyUtil.getSubfamilyRelations(tree_pane);
//			}
//			progressMessage = "Initializing PAINT annotations";
//			fireProgressChange(progressMessage, progress_inc * 2, ProgressEvent.Status.START);
//			GafAdapter.importAnnotations(path);

            progressMessage = "Initializing annotation matrix";
			//fireProgressChange(progressMessage, 90, ProgressEvent.Status.START);

            MatrixInfo mi = MatrixBuilder.getMatrixInfo(treeModel);

            annot_matrix.setModels(getTree().getTerminusNodes(), mi);

            fireProgressChange(progressMessage, 100, ProgressEvent.Status.END);

			//DirtyIndicator.inst().dirtyGenes(false);
            EventManager.inst().fireNewFamilyEvent(this, family);

        } else {
            family = null;
            fireProgressChange("Unable to open family", 100, ProgressEvent.Status.END);
        }
    }

    private static void fireProgressChange(String message, int percentageDone,
            ProgressEvent.Status status) {
//		ProgressEvent event = new ProgressEvent(GOAdapter.class, message,
//				percentageDone, status);
//		EventManager.inst().fireProgressEvent(event);
    }

    public List<Term> findTerm(String cue) {
//		Map<String, AnnotMatrixModel> annots = annot_matrix.getModels();
//		List<Term> term_list = new ArrayList<Term>();
//		if (annots != null) {
//			for (String aspect : annots.keySet()) {
//				AnnotMatrixModel annot_table = annots.get(aspect);
//				List<Term> partial_list = annot_table.searchForTerm(cue);
//				term_list.addAll(partial_list);
//			}
//		}
//		return term_list;
        return null;
    }
    

    public String getCuratorNotes() {
        if (null == family) {
            return null;
        }
        Comment c = family.getFamilyComment();
        if (null == c) {
            return null;
        }
        return c.getCommentUserNotes();
    }

    public void setCuratorNotes(String curatorNotes) {
        if (null == family || null == curatorNotes) {
            return;
        }
        Comment c = family.getFamilyComment();
        if (null == c) {
            c = new Comment(null, null, null);
            family.setFamilyComment(c);
        }
        c.setCommentUserNotes(curatorNotes);
    }
    
    public String getUpdateHistory() {
        if (null == family) {
            return null;
        }
        Comment c = family.getFamilyComment();
        if (null == c) {
            return null;
        }
        return c.getRevisionHistoryInfo();        
    }
    
    public String getFullComment() {
        if (null == family) {
            return null;
        }
        Comment c = family.getFamilyComment();
        if (null == c) {
            return null;
        }
        return c.getFormattedComment();        
    }

    /**
     * Method declaration
     *
     *
     * @param book
     *
     * @see
     */
    public void setTitle() {
        String title;

        if (getFamily().getName() != null) {
            title = getFamily().getName() + " (" + getFamily().getFamilyID()
                    + ")";
        } else {
            title = "";
        }
        GUIManager.getManager().getFrame().setTitle(title);
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDir) {
        currentDirectory = currentDir;
    }

    public OBOSession getGoRoot() {
        /* get all of the GO terms up front */
        if (go_root == null) {
            go_root = GOAdapter.loadGO();
        }
        return go_root;
    }

    public TreePanel getTree() {
        return tree_pane;
    }

    public MSAPanel getMSAPanel() {
        return msa_pane;
    }

    public void setMatrix(AnnotationMatrix annot_table) {
        annot_matrix = annot_table;
    }

    public AnnotationMatrix getMatrix() {
        return annot_matrix;
    }

    public void setupFixedInfo(GOTermHelper goTermHelper, TaxonomyHelper taxonHelper, VersionContainer vc, HashSet<String> curatableBookSet) {
        this.goTermHelper = goTermHelper;
        this.taxonHelper = taxonHelper;
        this.versinContainer = vc;
        this.curatableBookSet = curatableBookSet;
    }
   
    public VersionContainer getVersionContainer() {
        return versinContainer;
    }

    public GOTermHelper goTermHelper() {
        return goTermHelper;
    }

    public TaxonomyHelper getTaxonHelper() {
        return taxonHelper;
    }
    
    public HashSet<String> getCuratableBookSet() {
        return curatableBookSet;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUserInfo(Vector userInfo) {
        this.userInfo = userInfo;
    }

    public Vector getUserInfo() {
        return this.userInfo;
    }

    public void addToRemovedAnnotationLookup(GeneNode gNode, Annotation a) {
        if (null == gNode || null == a) {
            return;
        }
        HashSet<Annotation> annotSet = removedAnnotLookup.get(gNode);
        if (null == annotSet) {
            annotSet = new HashSet<Annotation>();
            removedAnnotLookup.put(gNode, annotSet);
        }
        annotSet.add(a);

    }

    public void addTermAncestor(TermAncestor termAncestor) {
        if (null == termAncestorList) {
            termAncestorList = new ArrayList<TermAncestor>();
        }
        termAncestorList.add(termAncestor);
        TreeModel treeModel = tree_pane.getTreeModel();
        MatrixInfo mi = MatrixBuilder.getMatrixInfo(treeModel);
        annot_matrix.setModels(getTree().getTerminusNodes(), mi);
    }
 
    public ArrayList<TermAncestor> getTermAncestorList() {
        return termAncestorList;
    }
    
    public void setMatrixInfo(MatrixInfo mi) {
        this.matrixInfo = mi;
    }
    
    public MatrixInfo getMatrixInnfo() {
        return matrixInfo;
    }

    public BrowserLauncher getBrowserLauncher() {
        if (null == browserLauncher) {
            try {
                browserLauncher = new BrowserLauncher();
                browserLauncher.setNewWindowPolicy(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return browserLauncher;
    }

}
