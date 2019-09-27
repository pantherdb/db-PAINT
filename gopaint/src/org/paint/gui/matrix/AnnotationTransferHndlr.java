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
package org.paint.gui.matrix;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import com.sri.panther.paintCommon.Constant;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paint.annotation.AnnotationForTerm;
import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import edu.usc.ksom.pm.panther.paint.matrix.TermToAssociation;
import edu.usc.ksom.pm.panther.paint.annotation.AnnotQualifierGroup;
import edu.usc.ksom.pm.panther.paint.annotation.QualifierAnnotRltn;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.NONE;
import org.bbop.framework.GUIManager;
import org.paint.datamodel.GeneNode;
import org.paint.dialog.AnnotationQualifierDlg;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.gui.evidence.PaintAction;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;
import org.paint.util.AnnotationUtil;
import org.paint.util.GeneNodeUtil;

/**
 *
 * @author muruganu
 */
public class AnnotationTransferHndlr extends TransferHandler {
    
    private static AnnotDragGestureRecognizer recognizer = null;
    private Set<GeneNode> visitedNodes;

    public static final DataFlavor FLAVOR_MATRIX_TRANSFER_INFO = new DataFlavor(MatrixTransferInfo.class, "MatrixTransferInfo");
    
    public static final String REMOVE_MSG_MORE_SPECIFIC_PART_1 = "Annotation to descendant node ";
    public static final String REMOVE_MSG_MORE_SPECIFIC_PART_2 = " for more specific term ";
    public static final String REMOVE_MSG_MORE_SPECIFIC_PART_3 = " (";
    public static final String REMOVE_MSG_MORE_SPECIFIC_PART_4 = ") will be removed.\n";

    public static final String REMOVE_MSG_LESS_SPECIFIC_PART_1 = "Annotation to node ";
    public static final String REMOVE_MSG_LESS_SPECIFIC_PART_2 = " for less specific term ";
    public static final String REMOVE_MSG_LESS_SPECIFIC_PART_3 = " (";
    public static final String REMOVE_MSG_LESS_SPECIFIC_PART_4 = ") will be removed.\n";
    
    public static final String MORE_SPECIFIC_DESCENDENT_ANNOTATION = "More specific descendent annotation";
    public static final String LESS_SPECIFIC_ANNOTATION = "Less specific annotation";    

    public AnnotationTransferHndlr() {
        super();
        visitedNodes = new HashSet<GeneNode>();
    }
    
    public AnnotationTransferHndlr(String property) {
        super();
        visitedNodes = new HashSet<GeneNode>();
    }    

    public boolean canImport(TransferHandler.TransferSupport support) {
        boolean canImport = false;
        String because = null;
        TreePanel treePanel = null;

        if (support.isDrop() && support.isDataFlavorSupported(FLAVOR_MATRIX_TRANSFER_INFO) && support.getComponent() instanceof TreePanel) {
            canImport = true;
            treePanel = (TreePanel) support.getComponent();
        }
        GeneNode node = null;
        if (false == canImport || treePanel == null) {
            return false;
        }

        Point p = support.getDropLocation().getDropPoint();
        if (!treePanel.pointInNode(p)) {
            canImport = false;
        }
        node = treePanel.getClickedInNodeArea(p);
        if (node != null) {
            if (node.isLeaf()) {
                canImport = false;
            } else {
                try {
                    //Tree tree = PaintManager.inst().getFamily().getTree();
                    because = PaintAction.inst().isValidTerm((MatrixTransferInfo) support.getTransferable().getTransferData(FLAVOR_MATRIX_TRANSFER_INFO), node);
                    if (null != because) {
                        canImport = false;
                        System.out.println(because);
                    }
//                    if (null != because) {
//                        System.out.println(because);
//                    }
                } catch (UnsupportedFlavorException e) {
                    canImport = false;
                } catch (IOException e) {
                    canImport = false;
                }
            }
        }
        clearVisitedNodes(treePanel);

        if (treePanel != null) {
            String drop_label = null;
            if (node != null) {
                visitedNodes.add(node);
                Node n = node.getNode();
                if (canImport) {
                    node.setDropColor(Color.BLACK);

                    drop_label = n.getStaticInfo().getPublicId();
                } else {
                    node.setDropColor(Color.RED);
                    if (null != because) {
                        drop_label = n.getStaticInfo().getPublicId() + " " + because;
                    }
                }
                treePanel.repaint();
            }
            Point dropPoint = support.getDropLocation().getDropPoint();
            dropPoint.x += 10;
            dropPoint.y += 2;

            treePanel.setDropInfo(node, dropPoint, drop_label);
        }

        return canImport;
    }
    
    protected Transferable createTransferable(JComponent c) {
        	AnnotationMatrix table = (AnnotationMatrix)c;
		int column = table.getSelectedColumn();
                int row = table.getClickedRow();
		AnnotationMatrixModel model = (AnnotationMatrixModel) table.getModel();
                TermAncestor termAncestor = model.getTermAncestorAtColumn(column);
		if (termAncestor == null) {
			System.out.println("No term for column " + column);
		}
                MatrixTransferInfo mti = new MatrixTransferInfo(termAncestor, ((AnnotationMatrixModel)table.getModel()).getNode(row));
               	Transferable transferable = new AnnotationTransferable(mti);
		return transferable; 
    }
    
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}    
    
	public void exportAsDrag(JComponent comp, InputEvent e, int action) {
		int srcActions = getSourceActions(comp);

		// only mouse events supported for drag operations
		if (!(e instanceof MouseEvent)
				// only support known actions
				|| !(action == COPY || action == MOVE || action == LINK)
				// only support valid source actions
				|| (srcActions & action) == 0)
		{

			action = TransferHandler.NONE;
		}
                System.out.println("Drag move is not none");  
		if (action != TransferHandler.NONE && !GraphicsEnvironment.isHeadless())
		{
			if (recognizer == null)
			{
				recognizer = new AnnotDragGestureRecognizer(new DragHandler());
			}
			recognizer.gestured(comp, (MouseEvent) e, srcActions, action);
		}
		else
		{
			exportDone(comp, null, TransferHandler.NONE);
		}	
	}
    
    class AnnotationTransferable implements Transferable {

        private MatrixTransferInfo mti;

        public AnnotationTransferable(MatrixTransferInfo mti) {
            this.mti = mti;
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return mti;
        }
        
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{FLAVOR_MATRIX_TRANSFER_INFO};
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(FLAVOR_MATRIX_TRANSFER_INFO);
        }
    }
    
	public Icon getVisualRepresentation(Transferable t) {
		//		return (Preferences.inst().getIconByName("inherited"));

		return super.getVisualRepresentation(t);
	}
        
    public boolean importData(TransferHandler.TransferSupport support) {

        MatrixTransferInfo mti = null;
//        GeneNode selectedNodeFromMatrix;
        TreePanel treePanel = null;

        try {
            mti = (MatrixTransferInfo) support.getTransferable().getTransferData(FLAVOR_MATRIX_TRANSFER_INFO);
//            selectedNodeFromMatrix = mti.getMatrixClickedNode();
        } catch (UnsupportedFlavorException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        if (support.getComponent() instanceof TreePanel) {
            treePanel = (TreePanel) support.getComponent();
        } else {
            return false;
        }
        if (null == mti) {

            return false;
        }
        TermToAssociation toa = mti.getTermAncestor().getTermToAssociation();
        Point p = support.getDropLocation().getDropPoint();
        GeneNode gNode = treePanel.getClickedInNodeArea(p);
        GOTerm annotTerm = toa.getTerm();
        String termAcc = annotTerm.getAcc();
        
        // Taxonomy check
        if (false == GeneNodeUtil.isTermValidForNode(gNode, termAcc)) {
            String msg = Constant.STR_EMPTY;
            if (null != gNode) {
                String species = gNode.getCalculatedSpecies();
                if (null != species) {
                    msg = "Node " + gNode.getNode().getStaticInfo().getPublicId() + " of type species " + species + " cannot be annotated with term " + annotTerm.getName() + "(" + termAcc + "), do you want to continue?";
                }
                else {
                     msg = "Node " + gNode.getNode().getStaticInfo().getPublicId() + " of unknown species type cannot be annotated with term " + annotTerm.getName() + "(" + termAcc + "), do you want to continue?";                   
                }
            }
            int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), msg, "Taxonomy Constraint Violation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (dialogResult != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        
        ArrayList<AnnotationForTerm> annotTermList = AnnotationUtil.getAnnotsAndApplicableQls(gNode, annotTerm);
        if (null == annotTermList) {
            return false;
        }
        
        HashSet<Annotation> annotSet = null;
        HashSet<Qualifier> applicableQset = null;        
        QualifierAnnotRltn qar = null;
        AnnotQualifierGroup aqg = new AnnotQualifierGroup(annotTermList);
        HashMap<HashSet<Qualifier>, HashSet<Annotation>> groups = aqg.getQualifierAnnotLookup();
        if (null == groups || 0 == groups.size()) {
            return false;
        }
        
        // Need to determine list of annotations the curator wants to use

        if (1 != groups.size()) {
            AnnotationQualifierDlg qualifierDlg = new AnnotationQualifierDlg(GUIManager.getManager().getFrame(), aqg);
            qar = qualifierDlg.getAnnotationSet();
            if (true == qualifierDlg.didUserSubmitForm()) {
                annotSet = qar.getAnnotSet();
                applicableQset = qar.getqSet();
            } else {
                return false;
            }
        }
        else {
            for (HashSet<Qualifier> qset: groups.keySet()) {
                applicableQset = qset;
                annotSet = groups.get(applicableQset);
                break;
            }
        }
        PaintManager pm = PaintManager.inst();
        ArrayList<GeneNode> nodesProvidingEvidence = new ArrayList<GeneNode>();
        for (Annotation a: annotSet) {
            GeneNode gn = pm.getGeneByPTNId(a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
            if (false == nodesProvidingEvidence.contains(gn)) {
                nodesProvidingEvidence.add(gn);
            }
        }
        
        ArrayList<GeneNode> allDescendents = new ArrayList<GeneNode>();
        GeneNodeUtil.allNonPrunedDescendents(gNode, allDescendents);
        
        if (false == handleAnnotsToMoreSpecificTerms(annotTerm, allDescendents)) {
            return false;
        }
        
        if (false == handleAnnotsToLessSpecificTerms(annotTerm, applicableQset, gNode)) {
            return false;
        }
        
        ArrayList<GeneNode> nodesToBeAnnotated = (ArrayList<GeneNode>)allDescendents.clone();
        nodesToBeAnnotated.removeAll(nodesProvidingEvidence);
        PaintAction.inst().addAnnotationAndPropagate(annotTerm, gNode, nodesToBeAnnotated, nodesProvidingEvidence, annotSet, applicableQset);
        clearVisitedNodes(treePanel);
        DirtyIndicator.inst().setAnnotated(true);
        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));        
        return true;
    }
    

    /**
     * Note, no need to look at NOT annotations in this scenario
     * @param term
     * @param descList
     * @return 
     */
    private boolean handleAnnotsToMoreSpecificTerms(GOTerm term, List<GeneNode> descList) {
        // Get ancestors for term with same aspect
        GOTermHelper gth = PaintManager.inst().goTermHelper();
        ArrayList<GOTerm> ancestors = gth.getAncestors(term);
        if (null == ancestors || 0 == ancestors.size()) {
            return true;
        }
        Iterator<GOTerm> iter = ancestors.iterator();
        while (iter.hasNext()) {
            GOTerm curTerm = iter.next();
            String aspect = curTerm.getAspect();
            if (null != aspect && false == aspect.equals(term.getAspect())) {
                iter.remove();
            }
        }
        
        // Get annotations to descendants and add to remove list, if necessary
        HashSet<Annotation> removeDescSet = new HashSet<Annotation>();
        StringBuffer removeBuffer = new StringBuffer();
        for (GeneNode node: descList) {
            Node n = node.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null ==  annotList) {
                continue;
            }
            for (Annotation annot: annotList) {
                String code = annot.getSingleEvidenceCodeFromSet();
                if (null == code) {
                    continue;
                }

                GeneNode gn = PaintManager.inst().getGeneByPTNId(n.getStaticInfo().getPublicId());
                if (code.equals(Evidence.CODE_IBD) || (code.equals(Evidence.CODE_IKR) && false == gn.isLeaf()) || code.equals(Evidence.CODE_IRD)) {
                    String goTerm = annot.getGoTerm();
                    GOTerm curTerm = gth.getTerm(goTerm);
                    if (true == ancestors.contains(curTerm) || true == curTerm.equals(term)) {
                        removeDescSet.add(annot);
                        removeBuffer.append(REMOVE_MSG_MORE_SPECIFIC_PART_1 + n.getStaticInfo().getPublicId() + REMOVE_MSG_MORE_SPECIFIC_PART_2 + goTerm + REMOVE_MSG_MORE_SPECIFIC_PART_3 + curTerm.getName() + REMOVE_MSG_MORE_SPECIFIC_PART_4);
                    }
                }
                else {
                    continue;
                }
            }
        }
        if (true == removeDescSet.isEmpty()) {
            return true;
        }
        
        int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), removeBuffer.toString(), MORE_SPECIFIC_DESCENDENT_ANNOTATION, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (dialogResult != JOptionPane.YES_OPTION) {
            return false;
        }
        
        for (Annotation annot: removeDescSet) {
            Node node = annot.getAnnotationDetail().getAnnotatedNode();
            GeneNode gn = PaintManager.inst().getGeneByPTNId(node.getStaticInfo().getPublicId());
            AnnotationUtil.deleteAnnotation(gn, annot);
        }
        
        return true;
        


    }
    
    private boolean handleAnnotsToLessSpecificTerms(GOTerm term, HashSet<Qualifier> applicableQset, GeneNode gNode) {
        Node n = gNode.getNode();
        NodeVariableInfo nvi = gNode.getNode().getVariableInfo();
        if (null == nvi) {
            return true;
        }
        ArrayList<Annotation> annots = nvi.getGoAnnotationList();
        if (null == annots || 0 == annots.size()) {
            return true;
        }
        GOTermHelper gth = PaintManager.inst().goTermHelper();
        ArrayList<GOTerm> ancestors = gth.getAncestors(term);
        if (null == ancestors || 0 == ancestors.size()) {
            return true;
        }
        
        HashSet<Annotation> removeAnnotSet = new HashSet<Annotation>();
        StringBuffer removeBuffer = new StringBuffer();
        for (Annotation a: annots) {
            String code = a.getSingleEvidenceCodeFromSet();
            GeneNode gn = PaintManager.inst().getGeneByPTNId(n.getStaticInfo().getPublicId());
            if (Evidence.CODE_IBD.equals(code) || (Evidence.CODE_IKR.equals(code) && false == gn.isLeaf()) ||Evidence.CODE_IRD.equals(code)) {
                GOTerm curTerm = gth.getTerm(a.getGoTerm());
                if (ancestors.contains(curTerm)) {
                    removeAnnotSet.add(a);
                    removeBuffer.append(REMOVE_MSG_LESS_SPECIFIC_PART_1 + n.getStaticInfo().getPublicId() + REMOVE_MSG_LESS_SPECIFIC_PART_2 + a.getGoTerm() + REMOVE_MSG_LESS_SPECIFIC_PART_3 + curTerm.getName() + REMOVE_MSG_LESS_SPECIFIC_PART_4);
                    
                }

            } 
        }
        if (true == removeAnnotSet.isEmpty()) {
            return true;
        }
        
        int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), removeBuffer.toString(), LESS_SPECIFIC_ANNOTATION, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (dialogResult != JOptionPane.YES_OPTION) {
            return false;
        }
        
        for (Annotation annot: removeAnnotSet) {
            Node node = annot.getAnnotationDetail().getAnnotatedNode();
            GeneNode gn = PaintManager.inst().getGeneByPTNId(node.getStaticInfo().getPublicId());
            AnnotationUtil.deleteAnnotation(gn, annot);
        }
        
        return true;
    }
    
//    public boolean checkAncestorAnnots(GeneNode gNode) {
//        // For NOT IBD annotation, ensure parent annotation is not less specific.  If it is, it already includes annotation user wants to create
//        HashSet<Annotation> removeAncSet = new HashSet<Annotation>();
//
//            ArrayList<GeneNode> ancestorList = new ArrayList<GeneNode>();
//            GeneNodeUtil.getAncestors(gNode, ancestorList);
//            
//            
//            for (GeneNode node: ancestorList) {
//                Node n = node.getNode();
//                NodeVariableInfo nvi = n.getVariableInfo();
//                if (null == nvi) {
//                    continue;
//                }
//                ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//                if (null ==  annotList) {
//                    continue;
//                }
//                for (Annotation annot: annotList) {
//                    String code = annot.getSingleEvidenceCodeFromSet();
//                    if (null == code) {
//                        continue;
//                    }
//                    if (code.equals(Evidence.CODE_IBD) || code.equals(Evidence.CODE_IKR) || code.equals(Evidence.CODE_IRD)) {
//                        String goTerm = annot.getGoTerm();
//                        GOTerm curTerm = gth.getTerm(goTerm);
//                        if (true == ancestors.contains(curTerm)) {
//                            removeAncSet.add(annot);
//                            removeBuffer.append(REMOVE_MSG_PART_1 + n.getStaticInfo().getPublicId() + REMOVE_MSG_PART_2 + goTerm + REMOVE_MSG_PART_3 + curTerm.getName() + REMOVE_MSG_PART_4);                        
//                        }
//                    }
//                    else {
//                        continue;
//                    }
//                }
//            }            
//            
//            
//       
//    }
    
    

    
//    public HashSet<Annotation> getApplicableAnnotations(GeneNode gNode, GeneNode selectedNodeFromMatrix, GOTerm term,  HashSet<Qualifier> applicableQset) {
//        HashSet<Annotation> annotSet = new HashSet<Annotation>();        
//        GOTermHelper gth = PaintManager.inst().goTermHelper();
//        AnnotationForTerm compAFT = new AnnotationForTerm(selectedNodeFromMatrix, term, gth);
//        if (false == compAFT.annotationExists()) {
//            return null;
//        }
//        boolean isNeg = QualifierDif.containsNegative(compAFT.getQset());
//        ArrayList<GeneNode> desc = new ArrayList<GeneNode>();
//        GeneNodeUtil.allNonPrunedDescendents(gNode, desc);
//        List<GeneNode> leaves = GeneNodeUtil.getAllLeaves(desc);
//
//
//
//        for (GeneNode leaf: leaves) {
//            AnnotationForTerm aft = new AnnotationForTerm(leaf, term, gth);
//            if (false == aft.annotationExists()) {
//                continue;
//            }
//            // Qualifiers have to match i.e. both have to be positive or both have to have a NOT.
//            if (isNeg != QualifierDif.containsNegative(aft.getQset())) {
//                continue;
//            }
//            if (null != aft.getAnnotSet()) {
//                annotSet.addAll(aft.getAnnotSet());
//                QualifierDif.addIfNotPresent(applicableQset, aft.getQset());
//            }            
//        }
//        if (false == annotSet.isEmpty()) {
//            return annotSet;
//        }
//
//        return null;
//    }

//    public boolean importDataOld(TransferHandler.TransferSupport support) {
//
//        MatrixTransferInfo mti = null;
//        GeneNode selectedNodeFromMatrix;
//        TreePanel treePanel = null;
//
//        try {
//            mti = (MatrixTransferInfo) support.getTransferable().getTransferData(FLAVOR_MATRIX_TRANSFER_INFO);
//            selectedNodeFromMatrix = mti.getMatrixClickedNode();
//        } catch (UnsupportedFlavorException e) {
//            return false;
//        } catch (IOException e) {
//            return false;
//        }
//        if (support.getComponent() instanceof TreePanel) {
//            treePanel = (TreePanel) support.getComponent();
//        } else {
//            return false;
//        }
//        if (null == mti) {
//
//            return false;
//        }
//        TermToAssociation toa = mti.getTermAncestor().getTermToAssociation();
//        Point p = support.getDropLocation().getDropPoint();
//        GeneNode gNode = treePanel.getClickedInNodeArea(p);
//        
//        GOTerm annotTerm = toa.getTerm();
//        ArrayList<Association> applicableAsnList = getPotentialAssociations(gNode, annotTerm);
//        if (null == applicableAsnList) {
//            return false;
//        }
//        
//        // Import annotations
//        System.out.println("Going to import annotations");        
//        PaintManager pm = PaintManager.inst();
//        
//        // Determine if node that was dragged from matrix is providing evidence.  Also get list of nodes providing evidence for later use
//        Annotation annotFromMatrixNode = null;
//        HashSet<Qualifier> qualifierSet = null;
//        ArrayList<GeneNode> nodesProvidingEvidence = new ArrayList<GeneNode>();
//        for (Association asn: applicableAsnList) {
//            if (selectedNodeFromMatrix.getNode().equals(asn.getNode())) {
//                annotFromMatrixNode = asn.getAnnotation();
//                qualifierSet = annotFromMatrixNode.getQualifierSet();
//            }
//            nodesProvidingEvidence.add(pm.getGeneByPTNId(asn.getNode().getStaticInfo().getPublicId()));
//        }
//        
//        // Qualifiers that are going to be used for the newly created annotation
//        HashSet<Qualifier> qSetForAnnot  = null;
//        
//        // User selected a node that has been annotated to the term. Just use qualifiers from this annotation 
//        // If user selected a node that is not annotated to the term or to a more specific child term, go through list of nodes that can provide evidence.  User gets to choose
//        // list of positive qualifiers for annotating node.  Negative qualifiers are not used.
//        if (null != annotFromMatrixNode) {
//            if (null != qualifierSet) {
//                qSetForAnnot = (HashSet<Qualifier>)qualifierSet.clone();
//            }
//        }
//        else {
//            // Get list of positive qualifiers from nodes providing evidence
//            HashSet<Qualifier> otherQualifiers = new HashSet<Qualifier>();
//            for (Association asn: applicableAsnList) {
//                HashSet<Qualifier> curQualifierSet = asn.getAnnotation().getQualifierSet();
//                if (null != curQualifierSet) {
//                    for (Qualifier q: curQualifierSet) {
//                        if (true == q.isNot()) {
//                            continue;
//                        }
//                        QualifierDif.addIfNotPresent(otherQualifiers, q);
//                    }
//                }
//            }
//            if (0 < otherQualifiers.size()) {
//                AnnotationQualifierDlg qualifierDlg = new AnnotationQualifierDlg(GUIManager.getManager().getFrame(), otherQualifiers);
//                HashSet<Qualifier> results = qualifierDlg.getQualifiers();
//                if (true == qualifierDlg.didUserSubmitForm()) {
//                    qSetForAnnot = results;
//                }
//                else {
//                    return false;
//                }
//            }
//        }
//        
//
//        ArrayList<GeneNode> allDescendents = new ArrayList<GeneNode>();
//        GeneNodeUtil.allNonPrunedDescendents(gNode, allDescendents);
//        ArrayList<GeneNode> nodesToBeAnnotated = (ArrayList<GeneNode>)allDescendents.clone();
//        nodesToBeAnnotated.removeAll(nodesProvidingEvidence);
//        PaintAction.inst().addAnnotationAndPropagateOld(annotTerm, gNode, nodesToBeAnnotated, nodesProvidingEvidence, qSetForAnnot);
//        clearVisitedNodes(treePanel);
//
//        return true;
//    }
    
//    private ArrayList<Annotation> getApplicableAssociation(ArrayList<Association> asnList) {
//        if (null == asnList || 0 == asnList.size()) {
//            return null;
//        }
//        ArrayList <Annotation> rtnList = new ArrayList<Annotation>();
//        Annotation newAnnot = new Annotation();
//        HashSet<Qualifier> compareQualifierSet = asnList.get(0).getAnnotation().getQualifierSet();
//        PaintManager pm = PaintManager.inst();
//        for (Association a: asnList) {
//            Node n = a.getNode();
//            GeneNode gn = pm.getGeneByPTNId(n.getStaticInfo().getPublicId());
//            if (n == null || false == gn.isLeaf()) {
//                continue;
//            }
//            Annotation annot = a.getAnnotation();
//            Evidence e = annot.getEvidence();
//            if (false == e.isExperimental()) {
//                continue;
//            }
//            if (false == allQualifiersSame(compareQualifierSet, annot.getQualifierSet())) {
//                return null;
//            }
//        }
//        return rtnList;
//    }
    

    
//    private boolean allQualifiersSame(HashSet<Qualifier> set1, HashSet<Qualifier> set2) {
//        // Empty or null
//        if ((null == set1 && null == set2) || (0 == set1.size() && 0 == set2.size())) {
//            return true;
//        }
//        
//        // Only one is null
//        if ((null == set1 && null != set2) || (null != set1 && null == set2)) {
//            return false;
//        }
//        
//        // Sizes do not match
//        if (set1.size() != set2.size()) {
//            return false;
//        }
//        
//        // Handle case where there are duplicates
//        HashSet<Qualifier> copy2 = (HashSet<Qualifier>)set2.clone();
//        for (Qualifier q1: set1) {
//            boolean found = false;
//            for (Iterator<Qualifier> q2Iter = copy2.iterator(); q2Iter.hasNext();) {
//                if (q1.getText().equals(q2Iter.next().getText())) {
//                    found = true;
//                    q2Iter.remove();
//                    break;
//                }
//            }
//            if (false == found) {
//                return false;
//            }
//        }
//
//        return true;
//    }
    
    

    private void clearVisitedNodes(TreePanel tree) {
        for (GeneNode currentNode : visitedNodes) {
            if (currentNode != null) {
                currentNode.setDropColor(null);
            }
        }
        if (tree != null) {
            tree.repaint();
        }
        visitedNodes.clear();
    }
    
	private static class AnnotDragGestureRecognizer extends DragGestureRecognizer
	{
		AnnotDragGestureRecognizer(DragGestureListener dgl)
		{
			super(DragSource.getDefaultDragSource(), null, NONE, dgl);
		}

		void gestured(JComponent c, MouseEvent e, int srcActions, int action)
		{
			setComponent(c);
			setSourceActions(srcActions);
			appendEvent(e);
			fireDragGestureRecognized(action, e.getPoint());
		}

		/**
		 * register this DragGestureRecognizer's Listeners with the Component
		 */
		protected void registerListeners()
		{
		}

		/**
		 * unregister this DragGestureRecognizer's Listeners with the Component
		 * <p/>
		 * subclasses must override this method
		 */
		protected void unregisterListeners()
		{
		}

	}    
    
	private static class DragHandler implements DragGestureListener, DragSourceListener
	{

		private boolean scrolls;

		// --- DragGestureListener methods -----------------------------------

		/**
		 * a Drag gesture has been recognized
		 */
		public void dragGestureRecognized(DragGestureEvent dge)
		{
			JComponent c = (JComponent) dge.getComponent();
			AnnotationTransferHndlr th = (AnnotationTransferHndlr) c.getTransferHandler();
			Transferable t = th.createTransferable(c);
			if (t != null)
			{
				scrolls = c.getAutoscrolls();
				c.setAutoscrolls(false);
				FontMetrics fm = c.getGraphics().getFontMetrics();
				String term_name;
				try {
					MatrixTransferInfo mti = (MatrixTransferInfo) t.getTransferData(FLAVOR_MATRIX_TRANSFER_INFO);
                                        TermAncestor termAncestor = mti.getTermAncestor();
					term_name = termAncestor.getTermToAssociation().getTerm().getName();
					int height = fm.getHeight() + 2;
					int width = fm.stringWidth("    " + term_name);
					Image img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
					Graphics g = img.getGraphics();
					g.setColor(Color.black);
					g.drawString(term_name, 0, height - 2);
					dge.startDrag(null, img, new Point(0, -1 * img.getHeight(null)), t, this);
					//							Cursor cursor = c.getToolkit().createCustomCursor(img, new Point(0,0), "usr");
					//							dge.startDrag(cursor, t, this);
				} catch (UnsupportedFlavorException e) {
					th.exportDone(c, t, TransferHandler.NONE);
					System.out.println("Unable to get term name, bad flavor");
				} catch (IOException e) {
					th.exportDone(c, t, TransferHandler.NONE);
					//					dge.startDrag(null, t, this);
					System.out.println("Unable to get term name, io problem");
				} catch (Exception e) {
					th.exportDone(c, t, TransferHandler.NONE);
					System.out.println(e.getMessage());
				}
			} else
			{
				th.exportDone(c, t, TransferHandler.NONE);
			}
		}

		// --- DragSourceListener methods -----------------------------------

		/**
		 * as the hotspot enters a platform dependent drop site
		 */
		public void dragEnter(DragSourceDragEvent dsde)
		{
                    System.out.println("Drag enter");
		}

		/**
		 * as the hotspot moves over a platform dependent drop site
		 */
		public void dragOver(DragSourceDragEvent dsde)
		{
		}

		/**
		 * as the hotspot exits a platform dependent drop site
		 */
		public void dragExit(DragSourceEvent dsde)
		{
                    System.out.println("Drag has exitted source");
		}

		/**
		 * as the operation completes
		 */
		public void dragDropEnd(DragSourceDropEvent dsde)
		{
			DragSourceContext dsc = dsde.getDragSourceContext();
			JComponent c = (JComponent) dsc.getComponent();
			if (dsde.getDropSuccess())
			{
				((AnnotationTransferHndlr)c.getTransferHandler()).exportDone(c, dsc.getTransferable(), dsde.getDropAction());
			}
			else
			{
				((AnnotationTransferHndlr)c.getTransferHandler()).exportDone(c, dsc.getTransferable(), TransferHandler.NONE);
			}
			c.setAutoscrolls(scrolls);
		}

		public void dropActionChanged(DragSourceDragEvent dsde)
		{
		}
	}
        
//        private ArrayList<Association> getPotentialAssociations(GeneNode gNode, GOTerm annotTerm) {
//            ArrayList<Association> annotToTermList = new ArrayList<Association>();
//            ArrayList<Association> annotToChildTermList = new ArrayList<Association>();
//            GOTermHelper gth = PaintManager.inst().goTermHelper();               
//            ArrayList<GeneNode> allDescendents = new ArrayList<GeneNode>();
//            GeneNodeUtil.allNonPrunedDescendents(gNode, allDescendents);
//            List<GeneNode> leafDescendents = GeneNodeUtil.getAllLeaves(allDescendents);
//            for (GeneNode gLeaf: leafDescendents) {
//                Node leaf = gLeaf.getNode();
//                NodeVariableInfo nvi = leaf.getVariableInfo();
//                if (null ==  nvi) {
//                    continue;
//                }
//                ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
//                if (null == goAnnotList) {
//                    continue;
//                }
//                for (Annotation annot: goAnnotList) {
//                    if (false == annot.isExperimental()) {
//                        continue;
//                    }
//                    String termAcc = annot.getGoTerm();
//                    GOTerm term = gth.getTerm(termAcc);
//                    if (term.equals(annotTerm)) {
//                        Association a = new Association();
//                        a.setAnnotation(annot);
//                        a.setNode(leaf);
//                        annotToTermList.add(a);
//                        continue;
//                    }
//                    if (gth.getAncestors(term).contains(annotTerm)) {
//                        Association a = new Association();
//                        a.setAnnotation(annot);
//                        a.setNode(leaf);
//                        annotToChildTermList.add(a);
//                        continue;
//                    }
//                }
//            }
//            if (false == annotToTermList.isEmpty()) {
//                return annotToTermList;
//            }
//            if (false == annotToChildTermList.isEmpty()) {
//                return annotToChildTermList;
//            }
//            return null;
//        }
}

        

        
//        if (null != qualifierSet) {
//            setForAnnotation = (HashSet<Qualifier>)qualifierSet.clone();
//        }
//        
//        // Qualifier set from what user choose is different from what is available in other nodes providing evidence
//        QualifierDif qd = new QualifierDif(otherQualifiers, qualifierSet);        
//        if (QualifierDif.QUALIFIERS_SAME != qd.getDifference()) {
//    
//            // If user choose a node from matrix, we need to use what user choose and ask user which other positive qualifiers to add
//            if (null != annotFromMatrixNode) {
//                if (null != qualifierSet && QualifierDif.containsNegative(qualifierSet)) {
//                    // We have some positive qualifier in addition to the NOT.  Allow use to select set of positive qualifiers
//                    if (qualifierSet.size() != 1) {
//                        HashSet<Qualifier> choices = (HashSet<Qualifier>)qualifierSet.clone();
//                        for (Qualifier q: otherQualifiers) {
//                            QualifierDif.addIfNotPresent(choices, q);
//                        }
//
//                    }
//                }
//            }
//        }
//        
//
//
//
//            // Ensure qualifiers are okay
//        // Ensure we have leaf nodes
////        ArrayList<GeneNode> allDescendents = new ArrayList<GeneNode>();
////        GeneNodeUtil.allNonPrunedDescendents(gNode, allDescendents);
////        List<GeneNode> leafDescendents = GeneNodeUtil.getAllLeaves(allDescendents);
////        if (null == leafDescendents || 0 == leafDescendents.size()) {
////            return false;
////        }
////        // Ensure there are associations
////
////        ArrayList<org.paint.datamodel.Association> asnList = toa.getAsnList();
////        if (null == asnList || 0 == asnList.size()) {
////            return false;
////        }
////
////        // Get list of annotations that are applicable for the leaves
////        PaintManager pm = PaintManager.inst();
////        ArrayList<org.paint.datamodel.Association> applicableAsnList = new ArrayList<org.paint.datamodel.Association>();
////        ArrayList<GeneNode> nodesProvidingEvidence = new ArrayList<GeneNode>();
////        Association asnFromNodeClickedInMatrix = null;
////        for (org.paint.datamodel.Association a : asnList) {
////            // Only experimental evidence is applicable
////            if (false == a.getAnnotation().getEvidence().isExperimental()) {
////                continue;
////            }
////            GeneNode curGNode = pm.getGeneByPTNId(a.getNode().getStaticInfo().getPublicId());
////            if (leafDescendents.contains(curGNode)) {
////                applicableAsnList.add(a);
////                nodesProvidingEvidence.add(curGNode);
////                if (curGNode.equals(selectedNodeFromMatrix)) {
////                    asnFromNodeClickedInMatrix = a;
////                }
////            }
////        }
////
////        // Ensure there are annotations with experimental evidence
////        if (null == applicableAsnList || 0 == applicableAsnList.size()) {
////            return false;
////        }
//        
////        // Node that was selected in Matrix has association
////        if (null == asnFromNodeClickedInMatrix) {
////            return false;
////        }
//        
//        // Get information about qualifiers from node that was clicked in matrix
//        GOTermHelper gth = PaintManager.inst().goTermHelper();        
//        HashSet<Qualifier> clickedsQualifiers = asnFromNodeClickedInMatrix.getAnnotation().getQualifierSet();
//        clickedsQualifiers = gth.getValidQualifiersForTerm(gth.getTerm(asnFromNodeClickedInMatrix.getAnnotation().getGoTerm()), clickedsQualifiers);
//        Boolean clickedAllPositive = null;
////        Boolean clickedAllNegative = null;
////        Boolean clickedPositiveAndNegative = null;
//        if (null != clickedsQualifiers) {
//            if (QualifierDif.allPositive(clickedsQualifiers)) {
//                clickedAllPositive = Boolean.TRUE;
//            }
////            else if (QualifierDif.allNot(clickedsQualifiers)) {
////                clickedAllNegative = Boolean.TRUE;
////            }
////            else if (QualifierDif.containsPositive(clickedsQualifiers) && QualifierDif.containsNegative(clickedsQualifiers)) {
////                clickedPositiveAndNegative = Boolean.TRUE;
////            }
//        }
//        
//        
//
//        HashSet<Qualifier> allQualifiers = new HashSet<Qualifier>();
////        Boolean allPositive = null;
//        Boolean allNegative = null;
//        Boolean positiveAndNegative = null;
////        boolean needQualifierInput = false;
//        for (org.paint.datamodel.Association a : applicableAsnList) {
//            if (asnFromNodeClickedInMatrix.equals(a)) {
//                continue;
//            }
//            Annotation annot = a.getAnnotation();
//            HashSet<Qualifier> curSet = annot.getQualifierSet();
//            curSet = gth.getValidQualifiersForTerm(gth.getTerm(annot.getGoTerm()), curSet);
////            QualifierDif dif = new QualifierDif(clickedsQualifiers, curSet);
////            if (QualifierDif.QUALIFIERS_SAME != dif.getDifference()) {
////                needQualifierInput = true;
////            }
//            if (null == curSet) {
//                continue;
//            }
////            if (QualifierDif.allPositive(curSet)) {
////                allPositive = Boolean.TRUE;
////            }
//            if (QualifierDif.allNot(curSet)) {
//                allNegative = Boolean.TRUE;
//            }
//            else if (QualifierDif.containsPositive(curSet) && QualifierDif.containsNegative(curSet)) {
//                positiveAndNegative = Boolean.TRUE;
//            }
//            
//            // Create unique set
//            for (Qualifier curQualifier: curSet) {
//                QualifierDif.addIfNotPresent(allQualifiers, curQualifier);
//            }
//        }
//
//        
//        
//        
//        // Have combination of positive and negative - Warn user and remove negative
//        if ((null == clickedsQualifiers || Boolean.TRUE == clickedAllPositive) && (Boolean.TRUE == allNegative || Boolean.TRUE == positiveAndNegative)) {
//            int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), "Found 'NOT' qualifier from sequences providing evidence.  'NOT' qualifier will be removed", "Positive and Negative Qualfiers", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
//            if(dialogResult != JOptionPane.YES_OPTION) {
//                return false;
//            }
//
//            for (Iterator<Qualifier> qualIter = allQualifiers.iterator(); qualIter.hasNext();) {
//                Qualifier q = qualIter.next();
//                if (q.isNot()) {
//                    qualIter.remove();
//                }
//            }
//        }
//        
//        QualifierDif qd = new QualifierDif(clickedsQualifiers, allQualifiers);
//        if (QualifierDif.QUALIFIERS_SAME != qd.getDifference()) {
//        
//            // Create list with all qualifiers
//            if (null != clickedsQualifiers) {
//                for (Iterator<Qualifier> qualIter = clickedsQualifiers.iterator(); qualIter.hasNext();) {
//                    Qualifier clickedQualifier = qualIter.next();
//                    QualifierDif.addIfNotPresent(allQualifiers, clickedQualifier);
//                }
//            }
//        
//            // Really if (!((allQualifiers.size() == 1 && true == QualifierDif.allNot(allQualifiers)) && !(0 == allQualifiers.size()))
//            // Here just to understand logic
//            // There is only one qualifier which came from node in question.  It can be a not or a positive qualifier
//            if (allQualifiers.size() == 1 ) {
//                // Dialog not required
//            }
//            else if (0 == allQualifiers.size()){
//                // No qualifiers so no need to ask user for input
//                // Dialog not required
//            }
//            else {
//                AnnotationQualifierDlg qualifierDlg = new AnnotationQualifierDlg(GUIManager.getManager().getFrame(), allQualifiers);
//                HashSet<Qualifier> results = qualifierDlg.getQualifiers();
//                if (true == qualifierDlg.didUserSubmitForm()) {
//                    allQualifiers = results;
//                }
//                else {
//                    return false;
//                }
//            }
//        }
//        if (allQualifiers.isEmpty()) {
//            allQualifiers =  null;
//        }
