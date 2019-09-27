/**
 *  Copyright 2019 University Of Southern California
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
package org.paint.gui.evidence;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.WithEvidence;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;;
import edu.usc.ksom.pm.panther.paint.annotation.AnnotationForTerm;

import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import edu.usc.ksom.pm.panther.paint.matrix.TermToAssociation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.DBXref;
import org.geneontology.db.model.Evidence;
import org.geneontology.db.model.GeneProduct;
import org.geneontology.db.model.Term;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.paint.datamodel.GeneNode;
import org.paint.dialog.EvdnceCdeAndNewAnnotDlg;
import org.paint.go.GOConstants;
import org.paint.go.GO_Util;
import org.paint.go.TermUtil;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.gui.familytree.TreePanel;
import org.paint.gui.matrix.MatrixTransferInfo;
import org.paint.gui.msa.MSAPanel;
import org.paint.main.PaintManager;
import org.paint.util.AnnotationUtil;
import org.paint.util.GeneNodeUtil;


public class PaintAction {

    private static PaintAction stroke;

    private static Logger log = Logger.getLogger(PaintAction.class);
        
    private static final String MSG_DESC_MORE_SPECIFIC_PART1 = "Descendent node ";
    private static final String MSG_DESC_MORE_SPECIFIC_PART2 = " has direct annotation to term ";
    private static final String MSG_DESC_MORE_SPECIFIC_PART3 = "(";    
    private static final String MSG_DESC_MORE_SPECIFIC_PART4 = ") which is less specific than term "; 
    private static final String MSG_DESC_MORE_SPECIFIC_PART5 = " ("; 
    private static final String MSG_DESC_MORE_SPECIFIC_PART6 = "). If you want to annotate the node with this term, delete annotation to descendent term." ;     
        

	private PaintAction() {
	}

	public static PaintAction inst() {
		if (stroke == null) {
			stroke = new PaintAction();
		}
		return stroke;
	}
        
    public String isValidTerm(MatrixTransferInfo matrixTransferInfo, GeneNode gnode) {
        TermAncestor termAncestor = matrixTransferInfo.getTermAncestor();
        TermToAssociation toa = termAncestor.getTermToAssociation();       
        PaintManager pm = PaintManager.inst();
        GOTermHelper gth = pm.goTermHelper();        
        GOTerm term = toa.getTerm();
        
        // Ensure node is not in pruned branch
        if (true == GeneNodeUtil.inPrunedBranch(gnode)) {
                return "PRUNED";
        }
        
        // Get list of applicable annotations for node
        HashSet<Annotation> annotsForTerm = AnnotationUtil.getAnnotationsForTerm(gnode, term);
        if (null == annotsForTerm) {
            return "NO ANNOTATIONS TO TERM";
        }
        
        // Check for ancestor node with less specific annotation for NOT annotation
//        HashSet<Qualifier> applicableQset = new HashSet<Qualifier>();
//        for (Annotation a: annotsForTerm) {
//            QualifierDif.addIfNotPresent(applicableQset, a.getQualifierSet());
//        }        
//        if (true == QualifierDif.containsNegative(applicableQset)) {
//            
//        }
        
        // As per discussions with Huaiyu, prompt user to select annotation with associated qualifier(s).
//        if (false == AnnotationUtil.qualifiersMatch(annotsForTerm)) {
//            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "If the experimental annotation or annotations are incorrect, please submit a ticket to challenge.", "Found conflicting experimental annotations", JOptionPane.ERROR_MESSAGE);
//            return "FOUND ANNOTATIONS WITH BOTH POSITIVE AND NEGATIVE QUALIFIERS";
//        }
        

//        String termAcc = term.getAcc();
        Node n = gnode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi) {


            ArrayList<Annotation> annotationList = nvi.getGoAnnotationList();
            if (null != annotationList) {
                for (Annotation annot : annotationList) {
                    String curTerm = annot.getGoTerm();
                    if (term.getAcc().equals(curTerm)) {
                        return "IS ALREADY ANNOTATED TO " + term.getName() + "(" + term.getAcc() + ")";
                    }
//                    HashSet<Qualifier> qSet = annot.getQualifierSet();
//                    if (null ==  qSet|| true == QualifierDif.containsPositive(qSet)) {
                        GOTerm curGoTerm = gth.getTerm(curTerm);
                        ArrayList<GOTerm> ancestorList = gth.getAncestors(curGoTerm);
                        if (ancestorList.contains(term)) {
                            return "NODE ALREADY ANNOTATED TO MORE SPECIFIC TERM (" + curGoTerm.getName() + "(" + curGoTerm.getAcc() + ") is more specific than " + term.getName() + "(" + term.getAcc() + ")";
                        }
//                    }

                    // TODO - CHECK WITH PAUL AND OR HUAIYU!!!!!   - Skip for now according to Paul      
                    // Term being annotated is related to terms in descendents



                }
            }

            ArrayList<GeneNode> allDescendents = new ArrayList<GeneNode>();
            GeneNodeUtil.allNonPrunedDescendents(gnode, allDescendents);
            
            // check to make sure that this term is more generic than directly annotated descendants
            // if all of them are more general, then disallow the annotation
            // Allow annotation.  When user makes annotation, remove annotation to more specific terms found in descendant nodes
//            StringBuffer sb = new StringBuffer();
//            if (true == termIsMoreSpecifictThanDirectAnnotations(allDescendents, gth, term, sb)) {
//                return sb.toString(); 
//            }
            
            
            List<GeneNode> leafDescendents = GeneNodeUtil.getAllLeaves(allDescendents);
            if (null == leafDescendents || 0 == leafDescendents.size()) {
                return "NO LEAVES IN DESCENDENTS";
            }
            
            
//            
//            // Ensure there are associations
//            ArrayList<org.paint.datamodel.Association> asnList = toa.getAsnList();
//            if (null == asnList || 0 == asnList.size()) {
//                return "NO NODES WITH ASSOCIATIONS";
//            }
//
//            // Get list of annotations that are applicable i.e. only from descendents of node
//            ArrayList<org.paint.datamodel.Association> applicableAsnList = new ArrayList<org.paint.datamodel.Association>();
//            //ArrayList<GeneNode> nodesProvidingEvidence = new ArrayList<GeneNode>();
//            for (org.paint.datamodel.Association a : asnList) {
//                
////                String annotatedToTermAcc = a.getAnnotation().getGoTerm();
////                if (false == termAcc.equals(annotatedToTermAcc)) {
////                    
////                }
//                // Only experimental evidence is applicable
//                if (false == a.getAnnotation().getEvidence().isExperimental()) {
//                    continue;
//                }
//                GeneNode curGNode = pm.getGeneByPTNId(a.getNode().getStaticInfo().getPublicId());
//                if (leafDescendents.contains(curGNode)) {
//                    applicableAsnList.add(a);
//                    //nodesProvidingEvidence.add(curGNode);
//                }
//            }
//
//            // Ensure there are annotations with experimental evidence
//            if (null == applicableAsnList || 0 == applicableAsnList.size()) {
//                return "NO ANNOTATIONS WITH EXPERIMENTAL EVIDENCE";
//            }

//            HashSet<Qualifier> allQualifiers = new HashSet<Qualifier>();
//            Boolean allPositive = null;
//            Boolean allNegative = null;
//            for (org.paint.datamodel.Association a : applicableAsnList) {
//                Annotation curAnnot = a.getAnnotation();
//                HashSet<Qualifier> curSet = curAnnot.getQualifierSet();
//                curSet = gth.getValidQualifiersForTerm(gth.getTerm(curAnnot.getGoTerm()), curSet);
//                if (null == curSet || QualifierDif.allPositive(curSet)) {
//                    allPositive = Boolean.TRUE;
//                }
//                else if (QualifierDif.allNot(curSet)) {
//                    allNegative = Boolean.TRUE;
//                }
//
//                if (null == curSet) {
//                    continue;
//                }
//                allQualifiers.addAll(curSet);
//            }
            
            // Can have both positive and negative - Just warn user before operation

//            // Have combination of positive and negative
//            if (allPositive != null && allNegative != null) {
//                return "HAVE COMBINATION OF POSITIVE AND NEGATIVE QUALIFIERS";
//            }
//
//            if (false == QualifierDif.qualifierSetOkayForAnnotation(allQualifiers)) {
//                return "QUALIFIERS NOT ACCEPTABLE FOR ANNOTATION";
//            }

        }



            // TaxonCheck - Handled elsewhere
        return null;

    }        
        
    public String isValidTermOld(MatrixTransferInfo matrixTransferInfo, GeneNode gnode) {
        TermAncestor termAncestor = matrixTransferInfo.getTermAncestor();
        GeneNode nodeClickedInMatrix = matrixTransferInfo.getMatrixClickedNode();
        TermToAssociation toa = termAncestor.getTermToAssociation();
        
        PaintManager pm = PaintManager.inst();
        GOTermHelper gth = pm.goTermHelper();        
        GOTerm term = toa.getTerm();
        AnnotationForTerm aft = new AnnotationForTerm(nodeClickedInMatrix, term, gth);
        if (false == aft.annotationExists()) {
            // Although user did not click on a node in the matrix with applicable annotation, check if any of the leaf descendants of the tree node have
            // applicable descendants.
            // Need to implement later
            
            
            return "NO ANNOTATIONS TO TERM";
        }
        boolean negativeAnnot = QualifierDif.containsNegative(aft.getQset());
//        String termAcc = term.getAcc();
        Node n = gnode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi) {
            if (true == GeneNodeUtil.inPrunedBranch(gnode)) {
                return "PRUNED";
            }

            ArrayList<Annotation> annotationList = nvi.getGoAnnotationList();
            if (null != annotationList) {
                for (Annotation annot : annotationList) {
                    String curTerm = annot.getGoTerm();
                    if (term.getAcc().equals(curTerm)) {
                        return "IS ALREADY ANNOTATED TO " + term.getName() + "(" + term.getAcc() + ")";
                    }
                    GOTerm curGoTerm = gth.getTerm(curTerm);
                    ArrayList<GOTerm> ancestorList = gth.getAncestors(curGoTerm);
                    if (ancestorList.contains(term)) {
                        return "NODE ALREADY ANNOTATED TO MORE SPECIFIC TERM (" + curGoTerm.getName() + "(" + curGoTerm.getAcc() + ") is more specific than " + term.getName() + "(" + term.getAcc() + ")";
                    }

                // TODO - CHECK WITH PAUL AND OR HUAIYU!!!!!        
                    // Term being annotated is related to terms in descendents



                }
            }

            ArrayList<GeneNode> allDescendents = new ArrayList<GeneNode>();
            GeneNodeUtil.allNonPrunedDescendents(gnode, allDescendents);
            
            // check to make sure that this term is more generic than directly annotated descendants
            // if all of them are more general, then disallow the annotation
            StringBuffer sb = new StringBuffer();
            if (true == termIsMoreSpecifictThanDirectAnnotations(allDescendents, gth, term, sb)) {
                return "DESCENDENT ANNOTATED WITH LESS SPECIFIC TERM"; 
            }
            
            
            List<GeneNode> leafDescendents = GeneNodeUtil.getAllLeaves(allDescendents);
            if (null == leafDescendents || 0 == leafDescendents.size()) {
                return "NO LEAVES IN DESCENDENTS";
            }
            
            
            // Go through all the leaves and get list of all experimental annotations
            boolean foundAnnot = false;            
            for (GeneNode gNode: leafDescendents) {
                if (true == AnnotationForTerm.annotationApplicable(aft, new AnnotationForTerm(gNode, term, gth))) {
                    foundAnnot = true;
                    break;
                }
                
            }

            if (false == foundAnnot) {
                return "NO LEAVES WITH EXPERIMENTAL ANNOTATIONS FOR TERM";
            }
            
//            
//            // Ensure there are associations
//            ArrayList<org.paint.datamodel.Association> asnList = toa.getAsnList();
//            if (null == asnList || 0 == asnList.size()) {
//                return "NO NODES WITH ASSOCIATIONS";
//            }
//
//            // Get list of annotations that are applicable i.e. only from descendents of node
//            ArrayList<org.paint.datamodel.Association> applicableAsnList = new ArrayList<org.paint.datamodel.Association>();
//            //ArrayList<GeneNode> nodesProvidingEvidence = new ArrayList<GeneNode>();
//            for (org.paint.datamodel.Association a : asnList) {
//                
////                String annotatedToTermAcc = a.getAnnotation().getGoTerm();
////                if (false == termAcc.equals(annotatedToTermAcc)) {
////                    
////                }
//                // Only experimental evidence is applicable
//                if (false == a.getAnnotation().getEvidence().isExperimental()) {
//                    continue;
//                }
//                GeneNode curGNode = pm.getGeneByPTNId(a.getNode().getStaticInfo().getPublicId());
//                if (leafDescendents.contains(curGNode)) {
//                    applicableAsnList.add(a);
//                    //nodesProvidingEvidence.add(curGNode);
//                }
//            }
//
//            // Ensure there are annotations with experimental evidence
//            if (null == applicableAsnList || 0 == applicableAsnList.size()) {
//                return "NO ANNOTATIONS WITH EXPERIMENTAL EVIDENCE";
//            }

//            HashSet<Qualifier> allQualifiers = new HashSet<Qualifier>();
//            Boolean allPositive = null;
//            Boolean allNegative = null;
//            for (org.paint.datamodel.Association a : applicableAsnList) {
//                Annotation curAnnot = a.getAnnotation();
//                HashSet<Qualifier> curSet = curAnnot.getQualifierSet();
//                curSet = gth.getValidQualifiersForTerm(gth.getTerm(curAnnot.getGoTerm()), curSet);
//                if (null == curSet || QualifierDif.allPositive(curSet)) {
//                    allPositive = Boolean.TRUE;
//                }
//                else if (QualifierDif.allNot(curSet)) {
//                    allNegative = Boolean.TRUE;
//                }
//
//                if (null == curSet) {
//                    continue;
//                }
//                allQualifiers.addAll(curSet);
//            }
            
            // Can have both positive and negative - Just warn user before operation

//            // Have combination of positive and negative
//            if (allPositive != null && allNegative != null) {
//                return "HAVE COMBINATION OF POSITIVE AND NEGATIVE QUALIFIERS";
//            }
//
//            if (false == QualifierDif.qualifierSetOkayForAnnotation(allQualifiers)) {
//                return "QUALIFIERS NOT ACCEPTABLE FOR ANNOTATION";
//            }

        }



            // TaxonCheck - Handled elsewhere
        return null;

    }
    
    
    public boolean termIsMoreSpecifictThanDirectAnnotations(ArrayList<GeneNode> nodeList, GOTermHelper gth, GOTerm term, StringBuffer sb) {
        if (null == nodeList || null == term) {
            return false;
        }
        for (GeneNode node: nodeList) {
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
                if (code.equals(GOConstants.DESCENDANT_SEQUENCES_EC) || code.equals(GOConstants.KEY_RESIDUES_EC) || code.equals(GOConstants.DIVERGENT_EC)) {
                    String goTerm = annot.getGoTerm();
                    GOTerm curTerm = gth.getTerm(goTerm);
                    if (true == gth.getAncestors(term).contains(curTerm)) {
                        sb.append(MSG_DESC_MORE_SPECIFIC_PART1 + n.getStaticInfo().getPublicId() + MSG_DESC_MORE_SPECIFIC_PART2 + goTerm + MSG_DESC_MORE_SPECIFIC_PART3 + curTerm.getName() + MSG_DESC_MORE_SPECIFIC_PART4 + term.getAcc() + MSG_DESC_MORE_SPECIFIC_PART5 + term.getName() + MSG_DESC_MORE_SPECIFIC_PART6);
                        return true;
                    }
                }
                else {
                    continue;
                }
            }
        }
        return false;
    }

//	public String isValidTerm(Term term, GeneNode node) {
//		/*
//		 * Can't drop onto a pruned node
//		 */
//		if (node.isPruned())
//			return "PRUNED";
//
//		// check to make sure that this term is more specific than any inherited terms
//		// and the node is not annotated to this term already
//		if (GO_Util.inst().isAnnotatedToTerm(node, term) != null) {
//			return ("is already annotated to this term");
//		}
//
//		// make sure that the term being annotated is related to terms in the descendants
//		WithEvidence withs = new WithEvidence(term, node);
//		if (withs.lacksEvidence()) {
//			return ("lacks supporting evidence");
//		}
//		// check to make sure that this term is more generic than directly annotated descendants
//		// if all of them are more general, then disallow the annotation
//		LinkedObject termObject = TermUtil.getLinkedObject(term);
//		String go_aspect = term.getCv();
//		if (descendantsAllBroader(node, termObject, go_aspect, true)) {
//			return ("all descendent's annotations are to more general terms than " + term.getName());
//		}
//
//		if (!TaxonChecker.inst().checkTaxons(node, termObject)) {
//			return ("doesn't pass taxon checks");
//		}
//		
//		return null;
//	}

	private boolean descendantsAllBroader(GeneNode node, LinkedObject startingTerm, String go_aspect, boolean all_broader) {
		Set<Association> associations = GO_Util.inst().getAssociations(node, go_aspect, false);
		
		if (associations != null) {
			for (Association association : associations) {
				// since we've decided to always do positive annotations with NOTs being added afterwards, should make sure that
				// the association is positive
				if (!association.isNot()) {
					if (association.isMRC() || (node.isLeaf() && GO_Util.inst().isExperimental(association))) {
						LinkedObject term = TermUtil.getLinkedObject(association.getTerm());
						/*
						 * First argument is the parent term, second term is the descendant
						 * returns true if 2nd argument is a descendant of the 1st argument 
						 */
						all_broader &= TermUtil.isDescendant(term, startingTerm);
					}
				}
			}
		}
		List<GeneNode> children = node.getChildren();
		if (all_broader && children != null) {
			for (GeneNode child : children) {
				all_broader &= descendantsAllBroader(child, startingTerm, go_aspect, all_broader);
			}
		}
		return all_broader;
	}

	/*
	 * Called when parsing a GAF file
	 */
//	public Association propagateAssociation(GeneNode node, Term term, Integer date, Set<Term> quals) {	
//		WithEvidence withs = new WithEvidence(term, node);
//		Set<GeneNode> exp_withs = withs.getExpWiths();
//		boolean negate = withs.isExperimentalNot();
//
//		Set<GeneNode> top_with = new HashSet<GeneNode> ();
//		top_with.add(node);
//		Association assoc = _propagateAssociation(node, term, top_with, exp_withs, negate, date, quals);
//
//		List<LogAssociation> removed = new ArrayList<LogAssociation>();
//		removeMoreGeneralTerms(node, term, removed);
//
//		ActionLog.inst().logAssociation(node, assoc, removed);
//
//		return assoc;
//	}

	/*
	 * Called after a drop of a term onto a node in the tree
	 */
//	public Association propagateAssociation(GeneNode node, Term term) {
//		WithEvidence withs = new WithEvidence(term, node);
//		Set<GeneNode> exp_withs = withs.getExpWiths();
//		boolean negate = withs.isExperimentalNot();
//		Set<Term> quals = withs.getWithQualifiers();
//
//		Set<GeneNode> top_with = new HashSet<GeneNode> ();
//		top_with.add(node);
//		Association assoc = _propagateAssociation(node, term, top_with, exp_withs, negate, null, quals);
//
//		List<LogAssociation> removed = new ArrayList<LogAssociation>();
//		removeMoreGeneralTerms(node, term, removed);
//
//		ActionLog.inst().logAssociation(node, assoc, removed);
//
//		EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
//		return assoc;
//	}

//	private Association _propagateAssociation(GeneNode node, 
//			Term term, 
//			Set<GeneNode> top_with, 
//			Set<GeneNode> exp_withs, 
//			boolean negate,
//			Integer date, 
//			Set<Term> quals) {
//
//		Association top_assoc = null;
//		if (node.getGeneProduct() == null) {
//			GeneProduct gp = GO_Util.inst().createGeneProduct(node);
//			node.setGeneProduct(gp);
//		}
//		/**
//		 * Only proceed if this is not one of the original sources of information for this association
//		 * and this node is not yet annotated to either this term
//		 */
//		if (!exp_withs.contains(node) && GO_Util.inst().isAnnotatedToTerm(node, term) == null) {
//			Association assoc;
//			if (top_with.contains(node)) {
//				assoc = GO_Util.inst().createAssociation(term, exp_withs, date, true, quals);
//				// not dirty if this is restoring annotations from a saved file
//				DirtyIndicator.inst().dirtyGenes(date == null);
//				top_assoc = assoc;
//			} else {
//				assoc = GO_Util.inst().createAssociation(term, top_with, date, false, quals);				
//			}
//
//			/*
//			 * Doing this afterwards to avoid examining it in the above operation
//			 */
//			node.getGeneProduct().addAssociation(assoc);
//
//			/*
//			 * propagate negation...
//			 */
//			assoc.setNot(negate);
//
//			/*
//			 * Make the top ancestral gene in this branch of the gene family the source of information
//			 */
//			List<GeneNode> children = node.getChildren();
//			if (children != null) {
//				for (GeneNode child : children) {
//					if (!child.isPruned()) {
//						_propagateAssociation(child, term, top_with, exp_withs, negate, date, quals);
//					}
//				}
//			}
//		}
//		return top_assoc;
//	}

	private void removeMoreGeneralTerms(GeneNode node, Term term, List<LogAssociation> removed) {
		removeMoreGeneralTermsFromNode(node, term, removed);
		List<GeneNode> children = node.getChildren();
		if (children != null) {
			for (GeneNode child : children) {
				removeMoreGeneralTerms(child, term, removed);
			}
		}
	}

	private void removeMoreGeneralTermsFromNode(GeneNode node, Term term, List<LogAssociation> removed) {
//		/*
//		 * remove any redundant annotations that have previously been done by PAINT curators
//		 * that are less specific than the new association that is being added.
//		 * That is: a descendant protein was annotated earlier to a more general term
//		 * and now the curator is adding a more specific term to a more ancestral branch of the family
//		 */
//		/* Removing this restriction as requested by Paul to allow NOT-ting of more generic inherited
//		 * terms
//		 */
//		GeneProduct gene_product = node.getGeneProduct();
//		Set<Association> current_set = gene_product != null ? gene_product.getAssociations() : null;		
//		LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
//		LinkedObject possible_child = (LinkedObject) GO_Util.inst().getObject(go_root, term.getAcc());
//		List<Association> removal = new ArrayList<Association> ();
//		if (current_set != null) {
//			for (Association assoc : current_set) {
//				if (GO_Util.inst().isPAINTAnnotation(assoc)) {
//					Term check_term = assoc.getTerm();
//					LinkedObject possible_parental = (LinkedObject) GO_Util.inst().getObject(go_root, check_term.getAcc());
//					if (!term.getAcc().equals(check_term.getAcc()) && TermUtil.isAncestor(possible_child, possible_parental, go_root, null)) {
//						removal.add(assoc);
//						LogAssociation note = new LogAssociation(node, assoc);
//						if (removed != null)
//							removed.add(note);
//					}
//				}
//			}
//			for (Association remove : removal) {
//				_removeAssociation(node, remove.getTerm());				
//			}
//		}
	}
        
//        public void deleteAnnotations(GeneNode node) {
//            Node n = node.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//            ArrayList<Annotation> removeAnnotList = new ArrayList<Annotation>();
//            if (null != annotList) {
//                for (Iterator<Annotation> annotIter = annotList.iterator(); annotIter.hasNext();) {
//                    Annotation a = annotIter.next();
//                    if (true == a.isExperimental()) {
//                        continue;
//                    }
//                    if (null != deleteAnnotation(node, a)) {
//                        if (false == removeAnnotList.contains(a)) {
//                            removeAnnotList.add(a);
//                        }
//                    }
////                    ArrayList<DBReference> dbRefList = e.getDbReferenceList();
////                    if (null != dbRefList) {
////                        for (DBReference ref: dbRefList) {
////                            if (false == GOConstants.PAINT_REF.equals(ref.getEvidenceType())) {
////                                continue;
////                            }
////                            if (null != deleteAnnotation(node, a)) {
////                                removeAnnotList.add(a);
////                            }
////                        }
////                    }
//                    
//
//                }
//            }
//            annotList.removeAll(removeAnnotList);
//            if (annotList.isEmpty()) {
//                nvi.setGoAnnotationList(null);
//            }
//            
////            NodeStaticInfo nsi = n.getStaticInfo();
////            String publicId = nsi.getPublicId();
////            String longGeneName = nsi.getLongGeneName();
////            
////            String sequenceDb = node.getSeqDB();
////            String sequenceId = node.getSeqId();
////            PaintManager pm = PaintManager.inst();
////            GOTermHelper gth = pm.goTermHelper();
////            List<GeneNode> allNodes = pm.getTree().getAllNodes();
////            for (GeneNode gn : allNodes) {
////                Node curNode = gn.getNode();
////                NodeVariableInfo curNvi = curNode.getVariableInfo();
////                if (null == curNvi) {
////                    continue;
////                }
////                ArrayList<Annotation> curAnnotList = curNvi.getGoAnnotationList();
////                if (null == curAnnotList) {
////                    continue;
////                }
////                for (Iterator<Annotation> annotIter = curAnnotList.iterator(); annotIter.hasNext(); ) {
////                    Annotation curAnnot = annotIter.next();
////                    if (curAnnot.getEvidence().isExperimental()) {
////                        continue;
////                    }
////                    String cTerm = curAnnot.getGoTerm();
////                    GeneNode annotPropagator = getPropagator(curAnnot);
////                    Annotation propAnnot = AnnotationUtil.getPropagatorsAnnotation(curAnnot.getGoTerm(), annotPropagator);
////                    if (null == propAnnot) {
////                        annotIter.remove();
////                        continue;
////
////                    }
////                    HashSet<Qualifier> curQSet = curAnnot.getQualifierSet();
////                    boolean containsNot = QualifierDif.containsNegative(curQSet);
////                    HashSet<Qualifier> newSet = new HashSet<Qualifier>();
////                    edu.usc.ksom.pm.panther.paintCommon.Evidence curEvidence = curAnnot.getEvidence();
////                    ArrayList<DBReference> withs = curEvidence.getWiths();
////                    if (null == withs || 0 == withs.size()) {
////                        System.out.println("Found no withs for " + cTerm + " for node " + curNode.getStaticInfo().getPublicId());
////                        continue;
////                    }
////                    boolean haveWiths = true;
////                    HashSet<Qualifier> directAnnotQSet = new HashSet<Qualifier>();
////                    HashSet<Qualifier> impliedAnnotQSet = new HashSet<Qualifier>();
////                    for (Iterator<DBReference> withIter = withs.iterator(); withIter.hasNext(); ) {
////                        DBReference dbRef = withIter.next();
////                        String db = dbRef.getEvidenceType();
////                        String value = dbRef.getEvidenceValue();
////                        if (null == db || null == value) {
////                            continue;
////                        }
////                        if (db.equals(GOConstants.PANTHER_DB) && value.equals(publicId) || (db.equals(sequenceDb) && value.equals(sequenceId))) {
////                            withIter.remove();
////                            continue;
////                        }
////                        if (true == db.equals(GOConstants.PANTHER_DB)) {
////                            GeneNode evidenceNode = pm.getGeneByPTNId(publicId);
////                            if (null == evidenceNode || false == evidenceNode.isLeaf()) {
////                                continue;
////                            }
////                            Node en = evidenceNode.getNode();
////                            NodeVariableInfo eNvi = en.getVariableInfo();
////                            if (null == eNvi) {
////                                continue;
////                            }
////                            ArrayList<Annotation> aList = eNvi.getGoAnnotationList();
////                            if (null == aList) {
////                                continue;
////                            }
////
////                            for (Annotation a: aList) {
////                                if (null == a.getQualifierSet()) {
////                                    continue;
////                                }
////                                String term = a.getGoTerm();
////                                if (cTerm.equals(term)) {
////
////                                    for (Qualifier q : a.getQualifierSet()) {
////                                        if (false == containsNot && q.isNot()) {
////                                            continue;
////                                        }
////                                        QualifierDif.addIfNotPresent(directAnnotQSet, q);
////                                    }
////                                    continue;
////                                }
////                                ArrayList<GOTerm> ancestors = gth.getAncestors(gth.getTerm(term));
////                                if (true == ancestors.contains(gth.getTerm(cTerm))) {
////                                    for (Qualifier q : a.getQualifierSet()) {
////                                        if (false == containsNot && q.isNot()) {
////                                            continue;
////                                        }
////                                        QualifierDif.addIfNotPresent(impliedAnnotQSet, q);
////                                    }
////                                }
////                            }
////                            if (null != directAnnotQSet) {
////                                for (Qualifier q: directAnnotQSet) {
////                                    QualifierDif.addIfNotPresent(newSet, q);
////                                }
////                            }
////                            else if (null != impliedAnnotQSet) {
////                                for (Qualifier q: impliedAnnotQSet) {
////                                    QualifierDif.addIfNotPresent(newSet, q);
////                                }                                
////                            }
////                        }
////
////                    }
////                    if ((true == containsNot && false == QualifierDif.containsNegative(newSet)) || (true == haveWiths && withs.isEmpty()) ) {
////                        annotIter.remove();
////                    }
////                    if (curAnnotList.isEmpty()) {
////                        curNvi.setGoAnnotationList(null);
////                    }
////                }
////            }
//            List<GeneNode> children = node.getChildren();
//            if (null != children) {
//                for (GeneNode child: children) {
//                    deleteAnnotations(child);
//                }
//            }
//        }
        

            
            
            
            
        
                
//        public void pruneBranchOld(GeneNode gNode) {
//            // Delete annotations for node and its descendants
//            deleteAnnotations(gNode);
//            
//            removeWiths(gNode);
//            
////            // Update annotations for ancestors and nodes that do not belong to the clade being pruned.
////            PruneInfo pruneInfo = new PruneInfo(gNode);
////            
////            // First delete annotations
////            HashMap<GeneNode, ArrayList<Annotation>> deleteLookup = pruneInfo.getDeleteAnnotationLookup();
////            if (null != deleteLookup) {
////                Set<GeneNode> deleteSet = deleteLookup.keySet();
////                for (GeneNode gn: deleteSet) {
////                    ArrayList<Annotation> deleteList = deleteLookup.get(gn);
////                    ArrayList<Annotation> annotToRemove = new  ArrayList<Annotation>();
////                    for (Annotation annot: deleteList) {
////                        Annotation delAnnot = deleteAnnotation(gn, annot);
////                        if (null != delAnnot) {
////                            annotToRemove.add(delAnnot);
////                        }
////                    }
////                    deleteList.removeAll(annotToRemove);
////                }
////            }
////            
////            ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
////            GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
////            updateAnnotation(PaintManager.inst().getTree().getRoot(), false, descList);
//            branchNotify(gNode);
//        }
        
//        public void graftBranch(GeneNode node) {
//            new GraftBranch(node);
//            branchNotify(node);
//        }
//
	public void pruneBranch(GeneNode node, boolean log_it) {
//		List<LogAssociation> purged = new ArrayList<LogAssociation>();
//		if (node.getGeneProduct() != null) {
//			Object[] initial_assocs = node.getAssociations().toArray();
//			for (Object o : initial_assocs) {
//				try {
//					if (o.getClass() != Class.forName("org.geneontology.db.model.Association")) {
//						log.debug(("Class is not Association but is " + o.getClass().getName()));
//					}
//				} catch (ClassNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				Association assoc = (Association) o;
//				Association removed = _removeAssociation(node, assoc.getTerm());
//				/*
//				 * Keep track of any associations that would need to be
//				 * restored if the user changes their mind
//				 */
//				if (log_it && (removed != null && (removed.isMRC() || removed.isDirectNot()))) {
//					LogAssociation note = new LogAssociation(node, assoc);
//					purged.add(note);
//				}
//			}
//		}
//		if (log_it)
//			ActionLog.inst().logPruning(node, purged);
//		branchNotify(node);
	}

//	public void graftBranch(GeneNode node, List<LogAssociation> archive, boolean log) {
//		restoreInheritedAssociations(node);
//		for (LogAssociation note : archive) {
//			Association replacement = redoAssociation(note, null);
//			if (note.isDirectNot()) {
//				/* Now what? */
//				setNot(replacement.getEvidence().iterator().next(), node, note.getEvidenceCode(), log);
//			}
//		}
//		branchNotify(node);
//	}
        
        /**
         * Removes withs and updates or removes annotations based on new withs
         * @param gNode 
         */
//        private void removeWiths(GeneNode gNode) {
//            ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//            GeneNodeUtil.allDescendents(gNode, descList);
//            HashSet<String> removeSet = new HashSet<String>(descList.size());
//            for (GeneNode gn: descList) {
//                removeSet.add(gn.getNode().getStaticInfo().getPublicId());
//            }
//            
//            List<GeneNode> allNodes = PaintManager.inst().getTree().getAllNodes();
//            Hashtable<GeneNode, ArrayList<Annotation>> removeLookup = new Hashtable<GeneNode, ArrayList<Annotation>>();
//            for (GeneNode gn: allNodes) {
//                Node n = gn.getNode();
//                NodeVariableInfo nvi = n.getVariableInfo();
//                if (null == nvi) {
//                    continue;
//                }
//                ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//                if (null == annotList) {
//                    continue;
//                }
//                for (Annotation annot: annotList) {
//                    if (true == annot.isExperimental()) {
//                        continue;
//                    }
//                    boolean withsUpdated = false;
//                    ArrayList<DBReference> withs = e.getWiths();
//                    if (null == withs || 0 == withs.size()) {
//                        System.out.println("Found annotation for node " + n.getStaticInfo().getPublicId() + " with annotation for term " + annot.getGoTerm() +  " with no withs");
//                        continue;
//                    }
//                    for (Iterator<DBReference> withIter = withs.iterator(); withIter.hasNext();) {
//                        DBReference with = withIter.next();
//                        if (true == GOConstants.PANTHER_DB.equals(with.getEvidenceType()) && removeSet.contains(with.getEvidenceValue())) {
//                            withIter.remove();
//                            withsUpdated = true;
//                        }
//                    }
//                    if ((true == withs.isEmpty()) || (true == withsUpdated && true == removeAnnotation(gn, annot))) {
//                        ArrayList<Annotation> removeList = removeLookup.get(gn);
//                        if (null == removeList) {
//                            removeList = new ArrayList<Annotation>();
//                            removeLookup.put(gn, removeList);
//                        }
//                        if (false == removeList.contains(annot)) {
//                            removeList.add(annot);
//                        }
//                    }
//                }
//            }
//            
//            HashSet<Annotation> removedAnnot = new HashSet<Annotation>();
//            Set<GeneNode> geneSet = removeLookup.keySet();
//            for (GeneNode gn: geneSet) {
//                ArrayList<Annotation> removeAnnotList = removeLookup.get(gn);
//                for (Annotation annot: removeAnnotList) {
//                    if (true == removedAnnot.contains(annot)) {
//                        continue;
//                    }
//                    if (null != deleteAnnotation(gn, annot)) {
//                        removedAnnot.add(annot);
//                    }
//                }
//            }
//        }
        
        /**
         * Returns true if annotation should be deleted based on evidence from withs.  Updates qualifiers based on withs for annotation and nodes descendants
         * @param a
         * @return 
         */
//        private boolean removeAnnotation(GeneNode gNode, Annotation a) {
//            if (a.isExperimental()) {
//                return false;
//            }
//            if (false == GOConstants.DESCENDANT_SEQUENCES_EC.equals(a.getSingleEvidenceCodeFromSet())) {
//                return false;
//            }
//            String cTerm = a.getGoTerm();
//            HashSet<Qualifier> newSet = new HashSet<Qualifier>();
//            boolean containsNot = QualifierDif.containsNegative(a.getQualifierSet());
//            GOTermHelper gth = PaintManager.inst().goTermHelper();
//            HashSet<Qualifier> directAnnotQSet = new HashSet<Qualifier>();
//            HashSet<Qualifier> impliedAnnotQSet = new HashSet<Qualifier>();
//            for (Iterator<DBReference> withIter = a.getEvidence().getWiths().iterator(); withIter.hasNext();) {
//                DBReference dbRef = withIter.next();
//                String db = dbRef.getEvidenceType();
//                String value = dbRef.getEvidenceValue();
//                if (null == db || null == value) {
//                    continue;
//                }
//
//                if (true == db.equals(GOConstants.PANTHER_DB)) {
//                    GeneNode evidenceNode = PaintManager.inst().getGeneByPTNId(value);
//                    if (null == evidenceNode || false == evidenceNode.isLeaf()) {
//                        continue;
//                    }
//                    if (true == GeneNodeUtil.inst().inPrunedBranch(evidenceNode)) {
//                        continue;
//                    }
//                    Node en = evidenceNode.getNode();
//                    NodeVariableInfo eNvi = en.getVariableInfo();
//                    if (null == eNvi) {
//                        continue;
//                    }
//                    ArrayList<Annotation> aList = eNvi.getGoAnnotationList();
//                    if (null == aList) {
//                        continue;
//                    }
//
//                    for (Annotation annot : aList) {
//                        if (false == annot.isExperimental() || null == annot.getQualifierSet()) {
//                            continue;
//                        }
//                        String term = a.getGoTerm();
//                        if (cTerm.equals(term)) {
//
//                            for (Qualifier q : annot.getQualifierSet()) {
//                                if (false == containsNot && q.isNot()) {
//                                    continue;
//                                }
//                                QualifierDif.addIfNotPresent(directAnnotQSet, q);
//                            }
//                            continue;
//                        }
//                        ArrayList<GOTerm> ancestors = gth.getAncestors(gth.getTerm(term));
//                        if (true == ancestors.contains(gth.getTerm(cTerm))) {
//                            for (Qualifier q : annot.getQualifierSet()) {
//                                if (false == containsNot && q.isNot()) {
//                                    continue;
//                                }                                
//                                QualifierDif.addIfNotPresent(impliedAnnotQSet, q);
//                            }
//                        }
//                    }
//                    if (null != directAnnotQSet) {
//                        for (Qualifier q : directAnnotQSet) {
//                            if (false == containsNot && q.isNot()) {
//                                continue;
//                            }
//                            QualifierDif.addIfNotPresent(newSet, q);
//                        }
//                    } else if (null != impliedAnnotQSet) {
//                        for (Qualifier q : impliedAnnotQSet) {
//                            if (false == containsNot && q.isNot()) {
//                                continue;
//                            }                            
//                            QualifierDif.addIfNotPresent(newSet, q);
//                        }
//                    }
//                }
//
//            }
//            if (true == containsNot && false == QualifierDif.containsNegative(newSet)) {
//                return true;
//            }
//            // Update qualifiers for node and descendants
//            if (false == QualifierDif.allQualifiersSame(newSet, a.getQualifierSet())) {
//                a.setQualifierSet(newSet);
//                if (newSet.isEmpty()) {
//                    a.setQualifierSet(null);
//                }
//                
//                // Need to update IBA annotations for nodes descendants
//                ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//                GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
//                String term = a.getGoTerm();
//                for (GeneNode gn: descList) {
//                    Node n = gn.getNode();
//                    NodeVariableInfo nvi = n.getVariableInfo();
//                    if (null == nvi) {
//                        continue;
//                    }
//                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//                    if (null == annotList) {
//                        continue;
//                    }
//                    for (Annotation annot: annotList) {
//                        if (true == term.equals(annot.getGoTerm()) && GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(annot.getSingleEvidenceCodeFromSet())) {
//                            GeneNode propagator = AnnotationUtil.getNonLeafPaintNodeFromWiths(annot.getEvidence().getWiths());
//                            if (null == propagator) {
//                                continue;
//                            }
//                            if (propagator.equals(gNode)) {
//                                if (null == newSet) {
//                                    annot.setQualifierSet(null);
//                                }
//                                else {
//                                    annot.setQualifierSet((HashSet<Qualifier>)newSet.clone());
//                                }
//                            }
//                        }
//                    }
//                }
//                
//                
//            }
//            return false;
//            
//        }
        
        

	private void branchNotify(GeneNode node) {
		TreePanel tree = PaintManager.inst().getTree();
		tree.handlePruning(node);
		if (node.isLeaf()) {
			MSAPanel msa = PaintManager.inst().getMSAPanel();
			msa.handlePruning(node);
		}
		EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
	}

//	private boolean restoreInheritedAssociations(GeneNode node) {
//		/*
//		 * First collect all of the ancestral annotations that should be applied to this node.
//		 */
//		Set<Association> ancestral_collection = new HashSet<Association>();
//		LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
//		if (node.getParent() != null) {
//			collectAncestorTerms(node.getParent(), ancestral_collection, go_root);
//		}
//		return restoreInheritedAssociations(node, ancestral_collection);
//	}
//	
//	private boolean restoreInheritedAssociations(GeneNode node, Term not_term) {
//		/*
//		 * First collect all of the ancestral annotations that should be applied to this node.
//		 */
//		
//		Set<Association> ancestral_collection = new HashSet<Association>();
//		LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
//		LinkedObject negated_term = (LinkedObject) GO_Util.inst().getObject(go_root, not_term.getAcc());
//		if (node.getParent() != null) {
//			collectBroaderAncestorTerms(node.getParent(), ancestral_collection, negated_term, go_root);
//		}
//		return restoreInheritedAssociations(node, ancestral_collection);
//
//	}
	
//	private boolean restoreInheritedAssociations(GeneNode node, Set<Association> ancestral_collection) {
//		GeneProduct gene_product = node.getGeneProduct();
//		boolean restoration = false;
//		LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
//		for (Association ancestral_assoc : ancestral_collection) {
//			Term ancestral_term = ancestral_assoc.getTerm();
//			LinkedObject obo_ancestral_term = (LinkedObject) GO_Util.inst().getObject(go_root, ancestral_term.getAcc());
//			boolean covered = false;
//			Set<Association> check_set = gene_product != null ? gene_product.getAssociations() : null;		
//			if (check_set != null) {
//				for (Iterator<Association> it = check_set.iterator(); it.hasNext() && !covered;) {
//					Association assoc = it.next();
//					// is first term/argument (from ancestral protein) 
//					// is a broader term for the second term/argument (from descendant protein)
//					// then there is no need to re-associate the broader term.
//					LinkedObject obo_check_term = (LinkedObject) GO_Util.inst().getObject(go_root, assoc.getTerm().getAcc());
//					covered |= (obo_check_term == obo_ancestral_term) || TermUtil.isDescendant(obo_ancestral_term, obo_check_term) &&
//							!assoc.isDirectNot();
//				}
//				if (!covered) {
//					GeneNode top = GO_Util.inst().getGeneNode(ancestral_assoc.getGene_product());
//					Term term = ancestral_assoc.getTerm();
//					WithEvidence withs = new WithEvidence(term, top);
//					Set<GeneNode> exp_withs = withs.getExpWiths();
//					boolean negate = withs.isExperimentalNot();
//					Set<GeneNode> top_with = new HashSet<GeneNode> ();
//					top_with.add(top);
//					Set<Term> old_quals = ancestral_assoc.getQualifiers();
//					Set<Term> quals = new HashSet<Term>();
//					for (Term qual : old_quals) {
//						String name = qual.getName().toUpperCase();
//						if (!name.equals(GOConstants.NOT) && !name.equals(GOConstants.CUT)) {
//							quals.add(qual);
//						}
//					}
//					_propagateAssociation(node, term, top_with, exp_withs, negate, ancestral_assoc.getDate(), quals);
//					restoration = true;
//				}
//			}
//		}
//		return restoration;
//	}
	
	private void collectBroaderAncestorTerms(GeneNode node, Set<Association> ancestral_collection, LinkedObject not_term, LinkDatabase go_root) {
		GeneProduct gene_product = node.getGeneProduct();
		Set<Association> ancestral_assocs = gene_product != null ? gene_product.getAssociations() : null;		
		if (ancestral_assocs != null) {
			/*
			 * For each term
			 * If it is a direct annotation
			 * and
			 * If there are no current annotations to that term or any of its child terms
			 * 
			 * Then an association to this term needs to be restored
			 */
			for (Association ancestral_assoc : ancestral_assocs) {
				// Did a curator annotate this ancestor?
				if (ancestral_assoc.isMRC()) {
						// Is a child term of this already in the list?
						// if yes then don't need to add it.
					LinkedObject ancestral_term = (LinkedObject) GO_Util.inst().getObject(go_root, ancestral_assoc.getTerm().getAcc());
						// is first term/argument (from ancestral protein) 
						// is a broader term for the second term/argument (from descendant protein)
						// then there is no need to re-associate the broader term.
					boolean broader = !not_term.getID().equals(ancestral_term.getID());
					broader &= TermUtil.isDescendant(ancestral_term, not_term);					
					if (broader) {
						ancestral_collection.add(ancestral_assoc);
					}
				}
			}	
			if (node.getParent() != null) {
				collectBroaderAncestorTerms(node.getParent(), ancestral_collection, not_term, go_root);
			}
		}
	}

//	public void redoAssociations(List<LogAssociation> archive, List<LogAssociation> removed) {
//		removed.clear();
//		for (LogAssociation note : archive) {
//			redoAssociation(note, removed);
//		}
//	}
//
//	public void redoDescendentAssociations(List<LogAssociation> archive) {
//		for (LogAssociation note : archive) {
//			redoAssociation(note, null);
//		}
//	}

//	public Association redoAssociation(LogAssociation note, List<LogAssociation> removed) {
//		GeneNode node = note.getNode();
//		Term term = note.getTerm();
//		Set<Term> quals = note.getQuals();
//		Integer date = note.getDate();
//
//		/**
//		 * Only proceed if this is not one of the original sources of information for this association
//		 * and this node is not yet annotated to either this term
//		 */
//		WithEvidence withs = new WithEvidence(term, node);
//		boolean negate = withs.isExperimentalNot();
//		Set<GeneNode> exp_withs = withs.getExpWiths();
//
//		Set<GeneNode> top_with = new HashSet<GeneNode> ();
//		top_with.add(node);
//		Association assoc = _propagateAssociation(node, term, top_with, exp_withs, negate, date, quals);
//
//		removeMoreGeneralTerms(node, term, removed);
//
//		return assoc;
//	}

	private void collectAncestorTerms(GeneNode ancestral_node, Set<Association> ancestral_collection, LinkDatabase go_root) {
		GeneProduct gene_product = ancestral_node.getGeneProduct();
		Set<Association> ancestral_assocs = gene_product != null ? gene_product.getAssociations() : null;		
		if (ancestral_assocs != null) {
			/*
			 * For each term
			 * If it is a direct annotation
			 * and
			 * If there are no current annotations to that term or any of its child terms
			 * 
			 * Then an association to this term needs to be restored
			 */
			for (Association ancestral_assoc : ancestral_assocs) {
				// Did a curator annotate this ancestor?
				if (ancestral_assoc.isMRC()) {
					// Is a child term of this already in the list?
					// if yes then don't need to add it.
					LinkedObject ancestral_term = (LinkedObject) GO_Util.inst().getObject(go_root, ancestral_assoc.getTerm().getAcc());
					boolean covered = false;
					for (Association check_assoc : ancestral_collection) {
						Term check_term = check_assoc.getTerm();
						// is first term/argument (from ancestral protein) 
						// is a broader term for the second term/argument (from descendant protein)
						// then there is no need to re-associate the broader term.
						LinkedObject dup_check = (LinkedObject) GO_Util.inst().getObject(go_root, check_term.getAcc());
						covered |= TermUtil.isDescendant(ancestral_term, dup_check);
					}
					if (!covered) {
						ancestral_collection.add(ancestral_assoc);
					}
				}
			}
		}	
		if (ancestral_node.getParent() != null) {
			collectAncestorTerms(ancestral_node.getParent(), ancestral_collection, go_root);
		}
	}

	/**
	 * This is called when the remove term button is clicked
	 */
//	public void removeAssociation(GeneNode node, Term term) {
//		Association removed = _removeAssociation(node, term);
//		restoreInheritedAssociations(node);
//		ActionLog.inst().logDisassociation(node, removed);
//		DirtyIndicator.inst().dirtyGenes(true);
//	}

//	private synchronized Association _removeAssociation(GeneNode node, Term term) {
//		GeneProduct gene_product = node.getGeneProduct();
//		Association removed = null;
//		if (gene_product != null) {
//			Set<Association> current = gene_product.getAssociations();
//			Set<Association> revised = new HashSet<Association>();
//			for (Association a : current) {
//				if (!(a.getTerm().getAcc().equals(term.getAcc()) && GO_Util.inst().isPAINTAnnotation(a))) {
//					revised.add(a);
//				} else
//					removed = a;
//			}
//			gene_product.setAssociations(revised);
//
//			List<GeneNode> children = node.getChildren();
//			if (children != null) {
//				for (Iterator<GeneNode> it = children.iterator(); it.hasNext();) {
//					GeneNode child = it.next();
//					_removeAssociation(child, term);
//				}
//			}
//		}
//		return removed;
//	}
//
//	public synchronized void undoAssociation(GeneNode node, Term term) {
//		_removeAssociation(node, term);
//		restoreInheritedAssociations(node);
//	}
//
//	public synchronized void undoAssociation(List<LogAssociation> remove_list) {
//		for (LogAssociation entry : remove_list) {
//			_removeAssociation(entry.getNode(), entry.getTerm());
//		}
//	}

//	public boolean isPainted(GeneNode node, boolean recurse) {
//		boolean annotated = false;
//		GeneProduct gene_product = node.getGeneProduct();
//		if (gene_product != null) {
//			Set<Association> associations = gene_product.getAssociations();
//			for (Iterator<Association> assoc_it = associations.iterator(); assoc_it.hasNext()  && !annotated; ) {
//				Association assoc = assoc_it.next();
//				annotated = GO_Util.inst().isPAINTAnnotation(assoc);	
//			}
//		}
//		if (recurse && !annotated) {
//			List<GeneNode> children = node.getChildren();
//			if (children != null) {
//				for (Iterator<GeneNode> node_it = children.iterator(); node_it.hasNext() && !annotated; ) {
//					GeneNode child = node_it.next();
//					annotated = isPainted(child, recurse);
//				}
//			}
//		}
//		return annotated;
//	}

	public void setNot(Evidence evidence, GeneNode node, String evi_code, boolean log) {
//		Association assoc = evidence.getAssociation();
//
//		if (!assoc.isNot()) {
//			assoc.setNot(true);
//			assoc.setDirectNot(true);
//
//			evidence.setCode(evi_code);
//
//			evidence.getWiths().clear();
//			if (evi_code.equals(GOConstants.DIVERGENT_EC) || evi_code.equals(GOConstants.KEY_RESIDUES_EC)) {
//				evidence.addWith(node.getParent().getGeneProduct().getDbxref());
//			}
//			else if (evi_code.equals(GOConstants.DESCENDANT_SEQUENCES_EC)) {
//				org.paint.gui.familytree.TreePanel tree = PaintManager.inst().getTree();
//				Vector<GeneNode> leafList = new Vector<GeneNode>();
//				tree.getLeafDescendants(node, leafList);
//				for (GeneNode leaf : leafList) {
//					Set<Association> leafAssocs = GO_Util.inst().getAssociations(leaf, AspectSelector.inst().getAspect().toString(), true);
//					if (leafAssocs != null) {
//						for (Association leafAssoc : leafAssocs) {
//							if (leafAssoc.getTerm().equals(assoc.getTerm()) && leafAssoc.isNot()) {
//								evidence.addWith(leaf.getGeneProduct().getDbxref());
//							}
//						}
//					}
//				}
//			}
//
//			/* 
//			 * Need to propagate this change to all descendants
//			 */
//			LinkDatabase all_terms = PaintManager.inst().getGoRoot().getLinkDatabase();
//			propagateNegationDown(node, assoc.getGene_product().getDbxref(), assoc, evi_code, true, all_terms);
//
//			restoreInheritedAssociations(node, assoc.getTerm());
//
//			if (log)
//				ActionLog.inst().logNot(node, evidence, evi_code);
//		}
	}

	public void unNot (Evidence evidence, GeneNode node, boolean log) {
		Association assoc = evidence.getAssociation();
		assoc.setNot(false);
		assoc.setDirectNot(false);
		evidence.setCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
		evidence.getWiths().clear();
		Association a = getAncestralNodeWithPositiveEvidenceForTerm(node.getParent(), assoc.getTerm());
		if (a != null)
			evidence.addWith(a.getGene_product().getDbxref());
		LinkDatabase all_terms = PaintManager.inst().getGoRoot().getLinkDatabase();
		propagateNegationDown(node, a.getGene_product().getDbxref(), assoc, evidence.getCode(), false, all_terms);
		List<LogAssociation> removed = new ArrayList<LogAssociation>();
		removeMoreGeneralTerms(node, assoc.getTerm(), removed);
		if (log)
			ActionLog.inst().logUnNot(node, evidence);
		DirtyIndicator.inst().dirtyGenes(true);
	}

	private Association getAncestralNodeWithPositiveEvidenceForTerm(GeneNode node, Term term) {
		Set<Association> node_assocs = GO_Util.inst().getAssociations(node, term.getCv(), false);
		if (node_assocs != null) {
			for (Association a : node_assocs) {
				if (a.getTerm().equals(term) && !a.isNot() && a.isMRC()) {
					return a;
				}
			}
		}
		if (node.getParent() == null) {
			return null;
		} else {
			return getAncestralNodeWithPositiveEvidenceForTerm(node.getParent(), term);
		}
	}

	public Association getAncestralNegation(Association assoc, Term term) {
		GeneNode node = GO_Util.inst().getGeneNode(assoc.getGene_product());
		return getAncestralNegation(node, term);
	}

	private Association getAncestralNegation(GeneNode node, Term term) {
		Set<Association> node_assocs = GO_Util.inst().getAssociations(node, term.getCv(), false);
		if (node_assocs != null) {
			for (Association a : node_assocs) {
				if (a.getTerm().equals(term) && a.isNot() && a.isDirectNot()) {
					return a;
				}
			}
		}
		if (node.getParent() == null) {
			return null;
		} else {
			return getAncestralNegation(node.getParent(), term);
		}
	}

	private void propagateNegationDown(GeneNode node, DBXref with, Association assoc, String code, boolean is_not, LinkDatabase all_terms) {
		List<GeneNode> children = node.getChildren();
		if (children == null)
			return;
		for (Iterator<GeneNode> node_it = children.iterator(); node_it.hasNext();) {
			GeneNode child = node_it.next();
			Set<Association> assoc_list = child.getGeneProduct().getAssociations();
			for (Iterator<Association> assoc_it = assoc_list.iterator(); assoc_it.hasNext();) {
				Association child_assoc = assoc_it.next();
				// Should not modify any experimental evidence
				if (GO_Util.inst().isExperimental(child_assoc)) {
					continue;
				}
				/*
				 * Better to see if the child term is_a (or is part_of) the parent term, rather than an exact match
				 */
				LinkedObject ancestor_obo = (LinkedObject) GO_Util.inst().getObject(all_terms, assoc.getTerm().getAcc());
				LinkedObject child_obo = (LinkedObject) GO_Util.inst().getObject(all_terms, child_assoc.getTerm().getAcc());
				if (TermUtil.isAncestor(child_obo, ancestor_obo, all_terms, null)) {
					Set<Evidence> child_evidence = child_assoc.getEvidence();
					child_assoc.setNot(is_not);
					child_assoc.setDirectNot(false);
					for (Evidence child_evi : child_evidence) {
						// all inherited annotations should have evidence code of "IBA", including
						// NOT annotations
						//						child_evi.setCode(code);
						child_evi.setCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
						child_evi.getWiths().clear();
						child_evi.addWith(with);
					}
					/*
					 * Hope this is safe enough to do. Essentially saying that all descendant proteins must have 
					 * exactly the same set of qualifiers as the ancestral protein for the association to this
					 * particular term
					 */
					propagateNegationDown(child, with, assoc, code, is_not, all_terms);
				}
			}
		}
	}
        
//    private void updateAnnotation(GeneNode gNode, boolean add, ArrayList<GeneNode> descList) {
//        if (true == gNode.isPruned()) {
//            return;
//        }
//
//        Node node = gNode.getNode();
//        NodeVariableInfo nvi = node.getVariableInfo();
//        if (true == add) {
//            return;
//        }
//        else {
//            if (null == nvi) {
//                return;
//            }
//            ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
//            if (null == goAnnotList) {
//                return;
//            }
//            for (Annotation annot: goAnnotList) {
//                String code = annot.getSingleEvidenceCodeFromSet();
//                if (true == GOConstants.DESCENDANT_SEQUENCES_EC.equals(code)) {
//                    UpdateAnnotation ua =  new UpdateAnnotation(gNode, annot, add, descList, PaintManager.inst());
//                    if (false == ua.isUpdateRequired()) {
//                        continue;
//                    }
//                    ua.updateForIBDAfterPrune(gNode, annot);
//                    return;
//                }
//                if (true == GOConstants.DIVERGENT_EC.equals(code) || true == GOConstants.KEY_RESIDUES_EC.equals(code)) {
//                    UpdateAnnotation ua =  new UpdateAnnotation(gNode, annot, add, descList, PaintManager.inst());
//                    if (false == ua.isUpdateRequired()) {
//                        continue;
//                    }
//                    ua.updateForIRDandIKRAfterPrune(gNode, annot);
//                }
//            }
//
//        }
//        List<GeneNode> children = gNode.getChildren();
//        if (null != children) {
//            for (GeneNode child: children) {
//                updateAnnotation(child, add, descList);
//            }
//        }
//    }
    
    // Adds IBD and IBA for descendants
    public void addAnnotationAndPropagate(GOTerm term, GeneNode node, List<GeneNode> propagateList, List<GeneNode> nodesProvidingEvidence, HashSet<Annotation> annotWithSet, HashSet<Qualifier> qualifierSet) {
        Annotation a = new Annotation();
        Node n = node.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            nvi = new NodeVariableInfo();
            n.setVariableInfo(nvi);
        }
        nvi.addGOAnnotation(a);      
        a.setAnnotStoredInDb(true);
        a.setQualifierSet(qualifierSet);
        a.setGoTerm(term.getAcc());
//        edu.usc.ksom.pm.panther.paintCommon.Evidence e = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//        a.setEvidence(e);
//        e.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBD);
//        DBReference dbRef = new DBReference();
//        dbRef.setEvidenceType(GOConstants.PAINT_REF);
//        dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//        e.addDbRef(dbRef);
        
        AnnotationDetail ad = a.getAnnotationDetail();
        ad.setAnnotatedNode(node.getNode());
        for (Annotation with: annotWithSet) {
            WithEvidence we = new WithEvidence();
            we.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBD);
            we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_EXP);
            we.setWith(with);
            ad.addWithEvidence(we);
//            ad.addWith(with);
            if (null != qualifierSet) {
                HashSet<Qualifier> withQset = with.getQualifierSet();
                if (null != withQset) {
                    for(Qualifier wq: withQset) {
                        if (QualifierDif.exists(qualifierSet, wq)) {
                            ad.addToInheritedQualifierLookup(wq, with);
                        }
                    }
                }
            }
        }
        AnnotationUtil.addIBAAnnotation(term.getAcc(), propagateList, a, qualifierSet);
        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
//        if (null == propagateList) {
//            return;
//        }
//        
//        for (GeneNode gn: propagateList) {
//            Annotation descAnnot = new Annotation();
//            n = gn.getNode();
//            nvi = n.getVariableInfo();
//            if (nvi == null) {
//                nvi = new NodeVariableInfo();
//                n.setVariableInfo(nvi);
//            }
//            nvi.addGOAnnotation(descAnnot);            
//            descAnnot.setAnnotStoredInDb(false);
//            if (null != qualifierSet) {
//                descAnnot.setQualifierSet((HashSet<Qualifier>)qualifierSet.clone());
//            }
//            descAnnot.setGoTerm(term.getAcc());
//            e = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            descAnnot.setEvidence(e);
//            e.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
//            dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            e.addDbRef(dbRef);
//            ad = descAnnot.getAnnotationDetail();
//            ad.setAnnotatedNode(gn.getNode());
//            ad.addWith(a);
//            if (null != qualifierSet) {
//                for (Qualifier q : qualifierSet) {
//                    ad.addToInheritedQualifierLookup(q, a);
//                }
//            }
//        } 
    }
        

        
        
        // Adds IBD and IBA for descendants
//        public void addAnnotationAndPropagateOld(GOTerm term, GeneNode node, List<GeneNode> propagateList, List<GeneNode> nodesProvidingEvidence, HashSet<Qualifier> qualifierSet) {
//            Annotation a = new Annotation();
//            a.setAnnotStoredInDb(true);
//            a.setQualifierSet(qualifierSet);
//            a.setGoTerm(term.getAcc());
//            edu.usc.ksom.pm.panther.paintCommon.Evidence e = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            e.setEvidenceCode(GOConstants.DESCENDANT_SEQUENCES_EC);
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            e.addDbRef(dbRef);
//            
//
//            for (GeneNode gNode: nodesProvidingEvidence) {
//                Node curNode = gNode.getNode();
//                DBReference curRef = new DBReference();
//                curRef.setEvidenceType(GOConstants.PANTHER_DB);
//                curRef.setEvidenceValue(curNode.getStaticInfo().getPublicId());
//                e.addWith(curRef);
//            }
////            if (null == e.getWiths() || e.getWiths().isEmpty()) {
////                System.out.println("No supporting evidence found for " + node.getNode().getStaticInfo().getPublicId());
//////                return;
////            }
//            a.setEvidence(e);
//            Node n = node.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                nvi = new NodeVariableInfo();
//                n.setVariableInfo(nvi);
//            }
//            nvi.addGOAnnotation(a);
//            
//            if (null == propagateList) {
//                return;
//            }
//            String propagatorPublicId = n.getStaticInfo().getPublicId();
//            for (GeneNode gNode: propagateList) {
//                Node descNode = gNode.getNode();
//                NodeVariableInfo descNvi = descNode.getVariableInfo();
//                if (null == descNvi) {
//                    descNvi = new NodeVariableInfo();
//                    descNode.setVariableInfo(descNvi);
//                }
//                Annotation descAnnot = new Annotation();
//                descNvi.addGOAnnotation(descAnnot);
//                
//                if (null != qualifierSet) {
//                    descAnnot.setQualifierSet((HashSet<Qualifier>)qualifierSet.clone());
//                }
//                descAnnot.setGoTerm(term.getAcc());
//                edu.usc.ksom.pm.panther.paintCommon.Evidence descEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//                descAnnot.setEvidence(descEvidence);
//                descEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//                DBReference descDbRef = new DBReference();
//                descDbRef.setEvidenceType(GOConstants.PAINT_REF);
//                descDbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//                descEvidence.addDbRef(descDbRef);
//                DBReference with = new DBReference();
//                with.setEvidenceType(GOConstants.PANTHER_DB);
//                with.setEvidenceValue(propagatorPublicId);
//                descEvidence.addWith(with);
//                if (null != e.getWiths()) {
//                    descEvidence.getWiths().addAll((ArrayList<DBReference>)e.getWiths().clone());
//                }
//            }
//            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(node));
//            
//    }
        
    public GeneNode getPropagator(Annotation a) {
        HashSet<WithEvidence> withEvSet = a.getAnnotationDetail().getWithEvidenceAnnotSet();
        if (null == withEvSet) {
            return null;
        }
        PaintManager pm = PaintManager.inst();
        for (WithEvidence we: withEvSet) {
            Annotation annot = (Annotation)we.getWith();
            if (annot == a) {
                continue;
            }
            Node n = a.getAnnotationDetail().getAnnotatedNode();
            GeneNode gn = pm.getGeneByPTNId(n.getStaticInfo().getPublicId());
            if (null == gn) {
                continue;
            }
            if (false == gn.isLeaf()) {
                return gn;
            }
        }
        return null;
        
//        edu.usc.ksom.pm.panther.paintCommon.Evidence e = a.getEvidence();
//        return AnnotationUtil.getNonLeafPaintNodeFromWiths(e.getWiths());
    }
    

    

    
//    public Annotation getAnnotation(GeneNode gNode, String goTerm, GeneNode propagator) {
//        if (null == goTerm) {
//            return null;
//        }
//        Node n = gNode.getNode();
//        NodeVariableInfo nvi = n.getVariableInfo();
//        if (null == nvi) {
//            return null;
//        }
//        
//        Node propNode = propagator.getNode();
//        String publicId = propNode.getStaticInfo().getPublicId();
//        
//        
//        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
//        if (null == annotList) {
//            return null;
//        }
//        
//        for (Annotation annot: annotList) {
//            if (goTerm.equals(annot.getGoTerm())) {
//                ArrayList<DBReference> withs = annot.getEvidence().getWiths();
//                if (null == withs) {
//                    continue;
//                }
//                for (DBReference dbRefs: withs) {
//                    if (true == publicId.equals(dbRefs.getEvidenceValue()) && GOConstants.PANTHER_DB.equals(dbRefs.getEvidenceType())) {
//                        return annot;
//                    } 
//                }
//            }
//        }
//        return null;
//    }
    

    

    
    /*
    // Annotation with opposite qualifier
    */
    public Annotation annotationWithOppositeQualifier(Node node, String gTerm, HashSet<Qualifier> qualifierSet) {
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
        if (null == goAnnotList) {
            return null;
        }

        
        for (Annotation annot: goAnnotList) {
            String annotTerm = annot.getGoTerm();
            if (true == annotTerm.equals(gTerm)) {
                HashSet<Qualifier> curSet = annot.getQualifierSet();
                if (false == QualifierDif.areOpposite(curSet, qualifierSet)) {
                    continue;
                }
                return annot;
            }
        }
        return null;
        
    }
    
    /*
    Ancestor annotations with same qualifier
    */
    public Annotation getAncestorAnnotationWithSameQualifier(Node node, String gTerm, HashSet<Qualifier> propQualifierSet) {
        NodeVariableInfo nvi = node.getVariableInfo();
        if (null == nvi) {
            return null;
        }
        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
        if (null == goAnnotList) {
            return null;
        }

        GOTermHelper gth = PaintManager.inst().goTermHelper(); 
        GOTerm term = gth.getTerm(gTerm);
        ArrayList<GOTerm> ancestorList = gth.getAncestors(term);
        
        if (null == ancestorList) {
            return null;
        }
        for (Iterator<GOTerm> iter = ancestorList.iterator(); iter.hasNext();) {
            GOTerm t = iter.next();
            if (false == term.getAspect().equals(t.getAspect())) {
                iter.remove();
            }
        }
        
        for (Annotation annot: goAnnotList) {
//            edu.usc.ksom.pm.panther.paintCommon.Evidence e = annot.getEvidence();
            HashSet<Qualifier> curSet = annot.getQualifierSet();
            if (false == QualifierDif.allQualifiersSame(curSet, propQualifierSet)) {
                continue;
            }
            if (GOConstants.DESCENDANT_SEQUENCES_EC.equals(annot.getSingleEvidenceCodeFromSet())) {
                GOTerm currentTerm = gth.getTerm(annot.getGoTerm());
                if (ancestorList.contains(currentTerm)) {
                    return annot;
                }
            }
        }
        return null;
    }
    
    public Annotation getPropagatorForIKR_IRD(Annotation ikrIrd) {
        HashSet<WithEvidence> withEvSet = ikrIrd.getAnnotationDetail().getWithEvidenceSet();
        if (null == withEvSet) {
            return null;
        }
        for (WithEvidence we : withEvSet) {
            Annotation a = (Annotation) we.getWith();
            Node n = a.getAnnotationDetail().getAnnotatedNode();
            GeneNode gn = PaintManager.inst().getGeneByPTNId(n.getStaticInfo().getPublicId());
            if (gn.isLeaf()) {
                continue;
            }
            if (a == ikrIrd) {
                continue;
            }
            if (true == QualifierDif.areOpposite(a.getQualifierSet(), ikrIrd.getQualifierSet())) {
                return a;
            }
        }
        return null;
    }
    
    public boolean deleteIKR(GeneNode gNode, Annotation a) {
        System.out.println("Deleting IKR for " + gNode.getNode().getStaticInfo().getPublicId());

        Annotation propagatorsAnnotation = getPropagatorForIKR_IRD(a);
        if (null == propagatorsAnnotation) {
            return false;
        }
        
        // Get information about the propagator
        Node propagatorNode = propagatorsAnnotation.getAnnotationDetail().getAnnotatedNode();
        GeneNode propagator = PaintManager.inst().getGeneByPTNId(propagatorNode.getStaticInfo().getPublicId());
        if (null == propagator || true == propagator.isLeaf()) {
            return false;
        }
//        String goTerm = a.getGoTerm();
//        Annotation propagatorsAnnotation = AnnotationUtil.getPropagatorsNOTAnnotation(goTerm, propagator, a.getQualifierSet());
//        if (null == propagatorsAnnotation) {
//            System.out.println("Cannot delete annotation, propagator's annotation not found");
//            return false;
//        }
        
//        // Get withs for the propagators annotation without the propagator
//        HashSet<WithEvidence> withEvSet = propagatorsAnnotation.getAnnotationDetail().getWithEvidenceSet();
//        HashSet<WithEvidence> withEvSetCopy = (HashSet<WithEvidence>)withEvSet.clone();
//        for (WithEvidence we: withEvSetCopy) {
//            if (we.getWith() == propagatorsAnnotation) {
//                withEvSetCopy.remove(we);
//                break;
//            }
//        }
        //ArrayList<DBReference> propWiths = AnnotationUtil.getWiths(propagatorsAnnotation.getEvidence().getWiths(), propagator.getNode().getStaticInfo().getPublicId());
//        if (null == propWiths) {
//            System.out.println("Cannot delete annotation, cannot find withs for propagator");
//            return false;
//        }        
        
        String term = a.getGoTerm();
        
        // Get ready for removing annotations
        Node n = gNode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return false;
        }
//        NodeStaticInfo nsi = n.getStaticInfo();
//        String publicId = nsi.getPublicId();

        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
        if (null == goAnnotList || false == goAnnotList.contains(a)) {
            return false;
        }        
        
        // Get list of nodes whose annotations were removed due to IKR.  Need to add propagator's annotation back to them
        ArrayList<GeneNode> withs = AnnotationUtil.getNodesProvidingExperimentalEvidence(propagatorsAnnotation, propagator);
        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
        GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
        if (null != withs) {
            descList.removeAll(withs);
        }
        
        
        
        
        // We want to restore.  Do in opposite order.
        // Check if an ancestor term was annotated and remove if necessary
        Annotation ibaAnnot = AnnotationUtil.getAnnotationWithSingleWithAnnotAndEvidenceCde(goAnnotList, propagatorsAnnotation, edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
        if (null != ibaAnnot) {
            deleteChildAndDescendentIBAAnnotation(gNode, ibaAnnot);
            //a.setChildAnnotation(null);
            //childAnnot.setParentAnnotation(null);
            goAnnotList.remove(ibaAnnot);
        }
          
        HashSet<Qualifier> propQualifierSet = propagatorsAnnotation.getQualifierSet();
        // delete IBA annotations for descendents due to IKR
        for (GeneNode descNode: descList) {
            Node dNode = descNode.getNode();
            if (null == dNode) {
                continue;
            }
            NodeVariableInfo descNvi = dNode.getVariableInfo();
            if (null == descNvi) {
                continue;
            }
            ArrayList<Annotation> descAnnotList = descNvi.getGoAnnotationList();
            if (null == descAnnotList) {
                continue;
            }
            for (Iterator<Annotation> descAnnotIter = descAnnotList.iterator(); descAnnotIter.hasNext();) {
                Annotation descAnnot = descAnnotIter.next();
                //edu.usc.ksom.pm.panther.paintCommon.Evidence descEvi = descAnnot.getEvidence();
                if (false == term.equals(descAnnot.getGoTerm())) {
                    continue;
                }
                
                Annotation annotPropagator = AnnotationUtil.getSingleWithPropagatorAnnot(descAnnot);
                if (annotPropagator != a) {
                    continue;
                }
                                
                descAnnotIter.remove();
            }
        }
        // Remove the IKR annotation
        goAnnotList.remove(a);
        
        // add annotations that were deleted when IKR was added
        descList.add(gNode);        // Need to add this node as well, since its annotation was deleted


//        DBReference propWith = new DBReference();
//        propWith.setEvidenceType(GOConstants.PANTHER_DB);
//        propWith.setEvidenceValue(propagator.getNode().getStaticInfo().getPublicId());
//        if (null == propWiths) {
//            propWiths = new ArrayList<DBReference>(1);
//        }
//        propWiths.add(propWith);
        
        for (GeneNode gn: descList) {
            Annotation newAnnot = new Annotation();
            newAnnot.setGoTerm(propagatorsAnnotation.getGoTerm());
            if (null != propQualifierSet) {
                newAnnot.setQualifierSet((HashSet<Qualifier>)propQualifierSet.clone());
            }
            
            WithEvidence we = new WithEvidence();
            we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
            we.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
            we.setWith(propagatorsAnnotation);
            newAnnot.addWithEvidence(we);
            
            //edu.usc.ksom.pm.panther.paintCommon.Evidence pe = propagatorAnnotation.getEvidence();
//            edu.usc.ksom.pm.panther.paintCommon.Evidence newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            newEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//            newEvidence.setWiths((ArrayList<DBReference>)propWiths.clone());
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            newEvidence.addDbRef(dbRef);
//            newAnnot.setEvidence(newEvidence);
            n = gn.getNode();
            nvi = n.getVariableInfo();
            if (null == nvi) {
                nvi = new NodeVariableInfo();
                n.setVariableInfo(nvi);
            }
            nvi.addGOAnnotation(newAnnot);
        }
        
        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
        return true;
    }
    
    public void deleteChildAndDescendentIBAAnnotation(GeneNode gNode, Annotation childAnnot) {
        Annotation propagatorAnnot = AnnotationUtil.getSingleWithPropagatorAnnot(childAnnot);
        
        HashSet<Qualifier> chidQualifierSet = childAnnot.getQualifierSet();
        String goTerm = childAnnot.getGoTerm();
        
        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
        GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
        descList.add(gNode);           // Need to delete annotation from this node as well
        for (GeneNode gn: descList) {
            NodeVariableInfo nvi = gn.getNode().getVariableInfo();
            if (null == nvi) {
                continue;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                continue;
            }
            for (Iterator<Annotation> annotIter = annotList.iterator(); annotIter.hasNext(); ) {
                Annotation annot = annotIter.next();
                if (false == goTerm.equals(annot.getGoTerm())) {
                    continue;
                }
                if (false == QualifierDif.allQualifiersSame(chidQualifierSet, annot.getQualifierSet())) {
                    continue;
                }

                if (false == edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA.equals(annot.getSingleEvidenceCodeFromSet())) {
                    continue;
                }
                
                Annotation curProp = AnnotationUtil.getSingleWithPropagatorAnnot(annot);
                if (propagatorAnnot == curProp) {
                    annotIter.remove();
                }
            }
//            if (annotList.isEmpty()) {
//                nvi.setGoAnnotationList(null);
//            }
        }
    }
        
    public boolean deleteIRD(GeneNode gNode, Annotation a) {
        System.out.println("Deleting IRD for " + gNode.getNode().getStaticInfo().getPublicId());
        
        Annotation propagatorsAnnotation = getPropagatorForIKR_IRD(a);
        if (null == propagatorsAnnotation) {
            return false;
        }        
        
        // Get information about the propagator
        Node propagatorNode = propagatorsAnnotation.getAnnotationDetail().getAnnotatedNode();
        GeneNode propagator = PaintManager.inst().getGeneByPTNId(propagatorNode.getStaticInfo().getPublicId());
        if (null == propagator || true == propagator.isLeaf()) {
            return false;
        }
        
//        String goTerm = a.getGoTerm();
//        Annotation propagatorsAnnotation = AnnotationUtil.getPropagatorsNOTAnnotation(goTerm, propagator, a.getQualifierSet());
//        if (null == propagatorsAnnotation) {
//            return false;
//        }
        
        
        
//        // Get withs for the propagators annotation without the propagator
//        HashSet<WithEvidence> withEvSet = propagatorsAnnotation.getAnnotationDetail().getWithEvidenceSet();
//        HashSet<WithEvidence> withEvSetCopy = (HashSet<WithEvidence>)withEvSet.clone();
//        for (WithEvidence we: withEvSetCopy) {
//            if (we.getWith() == propagatorsAnnotation) {
//                withEvSetCopy.remove(we);
//                break;
//            }
//        }
        
//        ArrayList<DBReference> propWiths = AnnotationUtil.getWiths(propagatorsAnnotation.getEvidence().getWiths(), propagator.getNode().getStaticInfo().getPublicId());
//        if (null == propWiths) {
//            return false;
//        }        
        
        

        Node n = gNode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null == nvi) {
            return false;
        }

        ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
        if (null == goAnnotList || false == goAnnotList.contains(a)) {
            return false;
        }

        HashSet<Qualifier> propQualifierSet = propagatorsAnnotation.getQualifierSet();
        
        // We want to restore.  Do in opposite order.
        // Check if an ancestor term was annotated and remove if necessary
        Annotation ibaAnnot = AnnotationUtil.getAnnotationWithSingleWithAnnotAndEvidenceCde(goAnnotList, propagatorsAnnotation, edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
        if (null != ibaAnnot) {
            deleteChildAndDescendentIBAAnnotation(gNode, ibaAnnot);
            //a.setChildAnnotation(null);
            //childAnnot.setParentAnnotation(null);
            goAnnotList.remove(ibaAnnot);
        }       
//        Annotation ancestorAnnotation = getAncestorAnnotationWithSameQualifier(n, goTerm, propQualifierSet);
//        if (null != ancestorAnnotation) {
////            deleteIBAAnnotation(gNode, ancestorAnnotation);
//            deleteAnnotation(gNode, ancestorAnnotation);
//        }
//        
        // Remove the IRD annotation
        goAnnotList.remove(a);
//        if (true == goAnnotList.isEmpty()) {
//            goAnnotList = null;
//            nvi.setGoAnnotationList(goAnnotList);
//        }
        // Get list of nodes whose annotations were removed due to IRD.  Need to add propagator's annotation back to them
        ArrayList<GeneNode> withs = AnnotationUtil.getNodesProvidingExperimentalEvidence(propagatorsAnnotation, propagator);
        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
        GeneNodeUtil.allDescendents(gNode, descList);
        descList.add(gNode);
        if (null != withs) {
            descList.removeAll(withs);
        }
        
        
        
        for (GeneNode gn: descList) {
            Annotation newAnnot = new Annotation();
            newAnnot.setGoTerm(propagatorsAnnotation.getGoTerm());
            if (null != propQualifierSet) {
                newAnnot.setQualifierSet((HashSet<Qualifier>)propQualifierSet.clone());
            }
            WithEvidence we = new WithEvidence();
            we.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
            we.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
            we.setWith(propagatorsAnnotation);
            newAnnot.addWithEvidence(we);
            
            n = gn.getNode();
            NodeVariableInfo descNvi = n.getVariableInfo();
            if (null == descNvi) {
                descNvi = new NodeVariableInfo();
                n.setVariableInfo(nvi);
            }
            descNvi.addGOAnnotation(newAnnot);
        }
        
        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
        
        return true;
    }
    
//    private void deleteIRDandIKRFromDescendants(ArrayList<GeneNode> descList, GeneNode gNode) {
//        HashMap<GeneNode, HashSet<Annotation>> deleteLookup = new HashMap<GeneNode, HashSet<Annotation>>();
//        for (GeneNode gn: descList) {
//            Node n = gn.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null ==  nvi) {
//                continue;
//            }
//            ArrayList<Annotation> goAnnotList = nvi.getGoAnnotationList();
//            if (null == goAnnotList) {
//                continue;
//            }
//            for (Annotation annot: goAnnotList) {
//                String code = annot.getEvidence().getEvidenceCode();
//                if (true == GOConstants.DIVERGENT_EC.equals(code) || true == GOConstants.KEY_RESIDUES_EC.equals(code)) {
//                    GeneNode propagator = getPropagator(annot);
//                    if (null == propagator) {
//                        continue;
//                    }
//                    if (gNode.equals(propagator)) {
//                        HashSet<Annotation> deleteSet = deleteLookup.get(annot);
//                        if (null == deleteSet) {
//                            deleteSet = new HashSet<Annotation>();
//                            deleteLookup.put(gNode, deleteSet);
//                        }
//                        deleteSet.add(annot);
//                    }
//                }
//            }
//        }
//        
//        for (GeneNode gn: deleteLookup.keySet()) {
//            HashSet<Annotation> deleteList = deleteLookup.get(gn);
//            for (Annotation annot: deleteList) {
//                String code = annot.getEvidence().getEvidenceCode();
//                if (true == GOConstants.DIVERGENT_EC.equals(code)) {
//                    deleteIRD(gn, annot);
//                    continue;
//                }
//                if (true == GOConstants.KEY_RESIDUES_EC.equals(code)) {
//                    deleteIKR(gn, annot);
//                    continue;
//                }
//                else {
//                    System.out.println("ERROR - FOUND annotaiton that could not be deleted for node " + gn.getNode().getStaticInfo().getPublicId() + " evidence code is " + code);
//                }
//            }
//        }        
//    }
    
    private void deleteDependantAnnotationsFromDescendants(GeneNode propagator, GeneNode gNode) {
        Node n = gNode.getNode();
        NodeVariableInfo nvi = n.getVariableInfo();
        if (null != nvi) {
            if (true == nvi.isPruned()) {
                return;
            }
        }
        // Delete annotations from descendants
        List<GeneNode> childList = gNode.getChildren();
        if (null != childList) {
            for (GeneNode child : childList) {
                deleteDependantAnnotationsFromDescendants(propagator, child);
            }
        }
        
        // Delete annotation for propagator
        if (null == nvi) {
            return;
        }    
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null != annotList) {
            //System.out.println("Propagator is " + n.getStaticInfo().getPublicId() + " Deleting dependant annotation from " + gNode.getNode().getStaticInfo().getPublicId());
            boolean annotRemoved = false;
            for (int i = 0; i < annotList.size(); i++) {
                Annotation annot = annotList.get(i);
                if (true == annot.isExperimental()) {
                    continue;
                }
                GeneNode annotPropagator = AnnotationUtil.getPAINTWithNode(annot);
                if (propagator.equals(annotPropagator)) {
                    //System.out.println("Deleting dependant annotation for " + gNode.getNode().getStaticInfo().getPublicId());
                    Annotation a = deleteAnnotation(gNode, annot);
                    if (null != a) {
                        annotList.remove(a);
                        annotRemoved = true;
                    }
                }
            }
            if (true == annotRemoved) {
                EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));    
            }
            if (0 == annotList.size()) {
                nvi.setGoAnnotationList(null);
            }
        }
 
    }
    

    
    public Annotation deleteAnnotation(GeneNode gNode, Annotation a) {
        //System.out.println("Attempt to delete annotation from " + gNode.getNode().getStaticInfo().getPublicId());
        // Before deleting this annotation, remove annotations from descendents where gNode is the propagator.
        List<GeneNode> childList = gNode.getChildren();
        if (null != childList) {
            for (GeneNode child : childList) {
                deleteDependantAnnotationsFromDescendants(gNode, child);
                
            }
        }        
//        GeneNode propagator = this.getPropagator(a);
//        if (gNode != propagator) {
//            System.out.println("Attempt to delete annotation that was not created by propagator");
//        }
//        edu.usc.ksom.pm.panther.paintCommon.Evidence e = a.getEvidence();
        if (a.isExperimental()) {
            return null;
        }
        String evidenceCode = a.getSingleEvidenceCodeFromSet();
        if (GOConstants.DESCENDANT_SEQUENCES_EC.equals(evidenceCode)) {
            deleteIBDAnnotation(gNode, a);
//            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));            
            return a;
        }
        else if (GOConstants.DIVERGENT_EC.equals(evidenceCode)) {
            if (false != deleteIRD(gNode, a)) {
//            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));            
            return a;
            }
            return null;
        }
        else if (edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR.equals(evidenceCode)) {
            if (false != deleteIKR(gNode, a)) {
//            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));            
                return a;
            }
            return null;
        }
        else if (edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA.equals(evidenceCode)) {
            if (false == AnnotationUtil.isIBAForIKRorIRD(a, gNode)) {
                Node n = gNode.getNode();
                NodeVariableInfo nvi = n.getVariableInfo();
                if (null != nvi) {
                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
                    if (null != annotList && annotList.contains(a)) {
//                        annotList.remove(a);
//                        if (annotList.isEmpty()) {
//                            nvi.setGoAnnotationList(null);
//                        }
//                        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
                        return a;
                    }
                }
            }
            else {
                System.out.println("Not deleting the IBA");
            }
            //IBA with parent annotation is okay.  We will deal with it when we delete the parent's annotation
            return null;
        }
        else {
            System.out.println("Cannot delete annotation of type " + evidenceCode);
            return null;
        }
        
        //EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
        
    }
    
    private void deleteIBDAnnotation(GeneNode gNode, Annotation a) {
        // First check if IRD or IKR was added to this annotation.  If yes, remove these first.
        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
        GeneNodeUtil.allDescendents(gNode, descList);
        //deleteIRDandIKRFromDescendants(descList, gNode);
        
//        edu.usc.ksom.pm.panther.paintCommon.Evidence e = a.getEvidence();
        String term = a.getGoTerm();
        // Can only delete IBD
        if (false == GOConstants.DESCENDANT_SEQUENCES_EC.equals(a.getSingleEvidenceCodeFromSet())) {
            return;
        }
        Node node = gNode.getNode();
        NodeVariableInfo nvi = node.getVariableInfo();
        String ptnId = node.getStaticInfo().getPublicId();
        
//        // Ensure this annotation was added by PAINT
//        ArrayList<DBReference> dbRefList = e.getDbReferenceList();
//        if (null == dbRefList) {
//            return;
//        }
//        boolean found = false;
//        for (DBReference dbRef: dbRefList) {
//            if (true == GOConstants.PAINT_REF.equals(dbRef.getEvidenceType()) && true == GO_Util.inst().getPaintEvidenceAcc().equals(dbRef.getEvidenceValue())) {
//                found = true;
//                break;
//            }
//        }
//        if (false == found) {
//            return;
//        }
        
        boolean removed = nvi.getGoAnnotationList().remove(a);
        if (false == removed) {
            return;
        }

        for (GeneNode descNode: descList) {
            Node n = descNode.getNode();
            if (null == n) {
                continue;
            }
            NodeVariableInfo descNvi = n.getVariableInfo();
            if (null == descNvi) {
                continue;
            }
            ArrayList<Annotation> descAnnotList = descNvi.getGoAnnotationList();
            if (null == descAnnotList) {
                continue;
            }
            for (Iterator<Annotation> descIter = descAnnotList.iterator(); descIter.hasNext();) {
                Annotation descAnnot = descIter.next();
//                edu.usc.ksom.pm.panther.paintCommon.Evidence descEvi = descAnnot.getEvidence();
                if (false == term.equals(descAnnot.getGoTerm())) {
                    continue;
                }
                
                Annotation propagatorAnnot = AnnotationUtil.getSingleWithPropagatorAnnot(descAnnot);
                if (propagatorAnnot != a) {
                    continue;
                }

//                
//                // Check ancestor from whome this annotation was propagated
//                GeneNode gn = AnnotationUtil.getNonLeafPaintNodeFromWiths(descEvi.getWiths());
//                if (null == gn) {
//                    continue;
//                }
//                if (false == ptnId.equals(gn.getNode().getStaticInfo().getPublicId())) {
//                    continue;
//                }
                
                descIter.remove();
            }
        }
       
    }

        
//    private void deleteIBAAnnotation(GeneNode gNode, Annotation a) {
//        // First check if IRD or IKR was added to this annotation.  If yes, remove these first.
//        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//        GeneNodeUtil.allDescendents(gNode, descList);
//        deleteIRDandIKRFromDescendants(descList, gNode);
//        
//        edu.usc.ksom.pm.panther.paintCommon.Evidence e = a.getEvidence();
//        String term = a.getGoTerm();
//        // Can only delete IBD
//        if (false == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(e.getEvidenceCode())) {
//            return;
//        }
//        Node node = gNode.getNode();
//        NodeVariableInfo nvi = node.getVariableInfo();
//        String ptnId = node.getStaticInfo().getPublicId();
//        
//        for (GeneNode descNode: descList) {
//            Node n = descNode.getNode();
//            if (null == n) {
//                continue;
//            }
//            NodeVariableInfo descNvi = n.getVariableInfo();
//            if (null == descNvi) {
//                continue;
//            }
//            ArrayList<Annotation> descAnnotList = descNvi.getGoAnnotationList();
//            if (null == descAnnotList) {
//                continue;
//            }
//            for (Iterator<Annotation> descIter = descAnnotList.iterator(); descIter.hasNext();) {
//                Annotation descAnnot = descIter.next();
//                edu.usc.ksom.pm.panther.paintCommon.Evidence descEvi = descAnnot.getEvidence();
//                if (false == term.equals(descAnnot.getGoTerm())) {
//                    continue;
//                }
//                
//                // Ensure this annotation was added by PAINT
//                ArrayList<DBReference> descDbRefList = descEvi.getDbReferenceList();
//                if (null == descDbRefList) {
//                    continue;
//                }
//                
//                boolean found = false;
//                for (DBReference descDbRef: descDbRefList) {
//                    if (true == GOConstants.PAINT_REF.equals(descDbRef.getEvidenceType()) && true == GO_Util.inst().getPaintEvidenceAcc().equals(descDbRef.getEvidenceValue())) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (false == found) {
//                    continue;
//                }
//                
//                // Check ancestor from whome this annotation was propagated
//                GeneNode gn = getNonLeafPaintNodeFromWiths(descEvi.getWiths());
//                if (null == gn) {
//                    continue;
//                }
//                if (false == ptnId.equals(gn.getNode().getStaticInfo().getPublicId())) {
//                    continue;
//                }
//                
//                descIter.remove();
//            }
//        }
//        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
//    }
    
    public void notAnnotation(GeneNode gNode, Annotation a) {
//        edu.usc.ksom.pm.panther.paintCommon.Evidence e = a.getEvidence();

        // Get qualifier
        HashSet<Qualifier> origQualifierSet = a.getQualifierSet();
        HashSet<Qualifier> newSet = new HashSet<Qualifier>();
        if (null == origQualifierSet) {
            Qualifier notQualifier = new Qualifier();
            notQualifier.setText(Qualifier.QUALIFIER_NOT);
            newSet.add(notQualifier);
        } else {
            boolean containsNot = false;
            for (Qualifier q : origQualifierSet) {
                if (q.isNot()) {
                    containsNot = true;
                    continue;
                }
                newSet.add(q);
            }
            if (false == containsNot) {
                Qualifier notQualifier = new Qualifier();
                notQualifier.setText(Qualifier.QUALIFIER_NOT);
                newSet.add(notQualifier);
            }
        }
        if (newSet.isEmpty()) {
            newSet = null;
        }

        String term = a.getGoTerm();
        // Can only update IBA
        if (false == edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA.equals(a.getSingleEvidenceCodeFromSet())) {
            return;
        }
        Node node = gNode.getNode();
        NodeVariableInfo nvi = node.getVariableInfo();
        NodeStaticInfo nsi = node.getStaticInfo();

        // Ensure this annotation was added by PAINT
        // All the WithEvidence should be from paint

        HashSet<WithEvidence> withEvSet = a.getAnnotationDetail().getWithEvidenceSet();
        if (null == withEvSet) {
            return;
        }
        for (WithEvidence we: withEvSet) {
            if (false == we.isPAINTType()) {
                return;
            }
        }
        

        // Get node that propagated this annotation
        
        Annotation propagatorAnnot = AnnotationUtil.getWithAnnotationForIBA(a, gNode);
        Node annotatorNode = propagatorAnnot.getAnnotationDetail().getAnnotatedNode();
        GeneNode annotator = PaintManager.inst().getGeneByPTNId(annotatorNode.getStaticInfo().getPublicId());

        if (null == annotator) {
            return;
        }
        Annotation lastIBD = AnnotationUtil.getLastIBD(propagatorAnnot, annotator);
        HashSet<GeneNode> getWithsFromLastIBD = AnnotationUtil.getWithsNodes(lastIBD);
        
//        edu.usc.ksom.pm.panther.paintCommon.Evidence evidence = a.getEvidence();
//        ArrayList<DBReference> withs = evidence.getWiths();
//        GeneNode annotator = AnnotationUtil.getNonLeafPaintNodeFromWiths(withs);
//        if (null == annotator) {
//            return;
//        }
        
        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
        GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
        ArrayList<GeneNode> leaves = new ArrayList<GeneNode>();
        ArrayList<GeneNode> defaultEvdnceLeaves = new ArrayList<GeneNode>();
        GOTermHelper gth = PaintManager.inst().goTermHelper(); 
        for (GeneNode cur : descList) {
            if (cur.isLeaf()) {
                AnnotationForTerm aft = new AnnotationForTerm(cur, gth.getTerm(a.getGoTerm()), gth);
//                if (false == aft.annotationExists()) {
//                    continue;
//                }
                if (true == aft.annotationExists() && true == QualifierDif.areOpposite(a.getQualifierSet(), aft.getQset())) {
                    defaultEvdnceLeaves.add(cur);
                }
                else {
                    leaves.add(cur);
                }
            }
        }
        descList.removeAll(getWithsFromLastIBD);
        EvdnceCdeAndNewAnnotDlg dlg = new EvdnceCdeAndNewAnnotDlg(GUIManager.getManager().getFrame(), a, leaves, defaultEvdnceLeaves);
        String evidenceCode = dlg.getSelectedEvidenceCode();
        if (null == evidenceCode) {
            return;
        }
        DirtyIndicator.inst().setAnnotated(true);
        NodeStaticInfo annotatorInfo = annotator.getNode().getStaticInfo();
        //String ptnId = annotatorInfo.getPublicId();

        // Remove annotation from node and applicable descendents
        descList.add(gNode);        // Remove annotation from the node as well
        ArrayList<GeneNode> deletedAnnotNodes = new ArrayList<GeneNode>();
        for (GeneNode descNode : descList) {
            Node n = descNode.getNode();
            if (null == n) {
                continue;
            }
            NodeVariableInfo descNvi = n.getVariableInfo();
            if (null == descNvi) {
                continue;
            }
            ArrayList<Annotation> descAnnotList = descNvi.getGoAnnotationList();
            if (null == descAnnotList) {
                continue;
            }
            
            HashSet<Annotation> removeAnnotList = new HashSet<Annotation>();
            for (Iterator<Annotation> descIter = descAnnotList.iterator(); descIter.hasNext();) {
                Annotation descAnnot = descIter.next();
                Annotation descPropAnnot = AnnotationUtil.getWithAnnotationForIBA(descAnnot, descNode);
                if (propagatorAnnot.equals(descPropAnnot)) {
                    removeAnnotList.add(descAnnot);
                }

                if (false == deletedAnnotNodes.contains(descNode)) {
                    deletedAnnotNodes.add(descNode);
                }
               
            }
            descAnnotList.removeAll(removeAnnotList);
            if (descAnnotList.isEmpty()) {
                descNvi.setGoAnnotationList(null);
            }
        }
        // deletedAnnotNodes has node that is propagating.  Remove since we will be using this list later
        deletedAnnotNodes.remove(gNode);        
        System.out.println("Notting annotation for " + nsi.getPublicId());
        // IRD 
        if (GOConstants.DIVERGENT_EC.equals(evidenceCode)) {
            Annotation newAnnotation = new Annotation();
            newAnnotation.setAnnotStoredInDb(true);
            newAnnotation.setGoTerm(term);              
            AnnotationDetail ad = newAnnotation.getAnnotationDetail();
            ad.setAnnotatedNode(node);
            newAnnotation.setQualifierSet(newSet);
            
            WithEvidence propWithEv = new WithEvidence();
            propWithEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD);
            propWithEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
            propWithEv.setWith(propagatorAnnot);
            ad.addWithEvidence(propWithEv);
            
            WithEvidence newAnnotWithEv = new WithEvidence();
            newAnnotWithEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD);
            newAnnotWithEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
            newAnnotWithEv.setWith(newAnnotation);
            ad.addWithEvidence(newAnnotWithEv);            
            
            if (null != origQualifierSet) {
                for (Qualifier origQ: origQualifierSet) {
                    ad.addToInheritedQualifierLookup(origQ, propagatorAnnot);
                }
            }
            if (QualifierDif.containsNegative(newSet)) {
                ad.addToAddedQualifierLookup(QualifierDif.getNOT(newSet), newAnnotation);
            }
            else {
                ad.addToRemovedQualifierLookup(QualifierDif.getNOT(origQualifierSet), newAnnotation);
            }
            


//            edu.usc.ksom.pm.panther.paintCommon.Evidence newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            newEvidence.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD);
//
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            newEvidence.addDbRef(dbRef);
//
//            DBReference with = new DBReference();
//            with.setEvidenceType(GOConstants.PANTHER_DB);
//            with.setEvidenceValue(ptnId);
//            newEvidence.addWith(with);
            
            if (null != dlg.getPMID()) {
                WithEvidence otherWith = new WithEvidence();
                otherWith.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD);
                otherWith.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
                
                DBReference pmidRef = new DBReference();
                pmidRef.setEvidenceType(DBReference.TYPE_PMID);
                pmidRef.setEvidenceValue(dlg.getPMID());
                otherWith.setWith(pmidRef);
                ad.addWithEvidence(otherWith);
            }
            
            for (GeneNode  defaultLeaf: defaultEvdnceLeaves) {
                WithEvidence nodeWith = new WithEvidence();
                nodeWith.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD);
                nodeWith.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
                nodeWith.setWith(defaultLeaf.getNode());
                ad.addWithEvidence(nodeWith);
            }            
            int selectedIndices [] = selectedIndices = dlg.getSelectedLeafIndices();
            if (0 < selectedIndices.length) {
                for (int i = 0; i < selectedIndices.length; i++) {
                    WithEvidence nodeWith = new WithEvidence();
                    nodeWith.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IRD);
                    nodeWith.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
                    nodeWith.setWith(leaves.get(selectedIndices[i]).getNode());
                    ad.addWithEvidence(nodeWith);
                }
            }            
            
            
            
            
//            newAnnotation.setEvidence(newEvidence);
            nvi.addGOAnnotation(newAnnotation);
            
            // Annotate with ancestor term
            if (true == dlg.areAncestorsApplicable()) {
                GOTerm ancestorTerm = dlg.getAncestor();
                if (null != ancestorTerm) {
                    descList.remove(gNode);
                    descList.removeAll(getWithsFromLastIBD);
                    //ArrayList<GeneNode> withsForAncestor = new ArrayList<GeneNode>(1);//(ArrayList<GeneNode>)withsWithoutPropagator.clone();
                    //withsForAncestor.add(gNode);
                    //AnnotationUtil.addIBAAnnotation(ancestorTerm, descList, withsForAncestor, origQualifierSet);
                    AnnotationUtil.addIBAAnnotation(ancestorTerm.getAcc(), descList, propagatorAnnot, origQualifierSet);

                    // Create IBA for the node
                    Annotation ibaAnnot = new Annotation();
                    ad = ibaAnnot.getAnnotationDetail();
                    WithEvidence withAnnotEv = new WithEvidence();
                    withAnnotEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
                    withAnnotEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
                    withAnnotEv.setWith(propagatorAnnot);
                    ad.addWithEvidence(withAnnotEv);


                    ad.setAnnotatedNode(gNode.getNode());
                    ibaAnnot.setAnnotStoredInDb(true);                
                    if (null != origQualifierSet) {
                        // Only add qualifiers that are valid for term
                        HashSet<Qualifier> validSet = null;
                        HashSet<Qualifier> qsetCopy = (HashSet<Qualifier>) origQualifierSet.clone();
                        validSet = new HashSet<Qualifier>();
                        for (Qualifier q : qsetCopy) {
                            if (true == gth.isQualifierValidForTerm(ancestorTerm, q)) {
                                validSet.add(q);
                            }
                        }
                        if (validSet.isEmpty()) {
                            validSet = null;
                        }

                        ibaAnnot.setQualifierSet(validSet);
                        if (null != validSet) {
                            for (Qualifier q : validSet) {
                                ad.addToInheritedQualifierLookup(q, propagatorAnnot);
                            }
                        }
                    }
                    ibaAnnot.setGoTerm(ancestorTerm.getAcc());
                    nvi.addGOAnnotation(ibaAnnot);

    //                nvi.addGOAnnotation(newAnnotation);
                }
            }
            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
            return;
        }

        // IKR
        Annotation newAnnotation = new Annotation();
        newAnnotation.setAnnotStoredInDb(true);
        newAnnotation.setGoTerm(term);
        AnnotationDetail ad = newAnnotation.getAnnotationDetail();
        ad.setAnnotatedNode(node);
        newAnnotation.setQualifierSet(newSet);
                
        WithEvidence propWithEv = new WithEvidence();
        propWithEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR);
        propWithEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
        propWithEv.setWith(propagatorAnnot);
        ad.addWithEvidence(propWithEv);

        WithEvidence newAnnotWithEv = new WithEvidence();
        newAnnotWithEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR);
        newAnnotWithEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
        newAnnotWithEv.setWith(newAnnotation);
        ad.addWithEvidence(newAnnotWithEv);
            

        if (null != origQualifierSet) {
            for (Qualifier origQ : origQualifierSet) {
                ad.addToInheritedQualifierLookup(origQ, propagatorAnnot);
            }
        }
        if (QualifierDif.containsNegative(newSet)) {
            ad.addToAddedQualifierLookup(QualifierDif.getNOT(newSet), newAnnotation);
        } else {
            ad.addToRemovedQualifierLookup(QualifierDif.getNOT(origQualifierSet), newAnnotation);
        }



        if (null != dlg.getPMID()) {
            WithEvidence otherWith = new WithEvidence();
            otherWith.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR);
            otherWith.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);

            DBReference pmidRef = new DBReference();
            pmidRef.setEvidenceType(DBReference.TYPE_PMID);
            pmidRef.setEvidenceValue(dlg.getPMID());
            otherWith.setWith(pmidRef);
            ad.addWithEvidence(otherWith);
        }
        
        for (GeneNode defaultLeaf : defaultEvdnceLeaves) {
            WithEvidence nodeWith = new WithEvidence();
            nodeWith.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR);
            nodeWith.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
            nodeWith.setWith(defaultLeaf.getNode());
            ad.addWithEvidence(nodeWith);
        }
        
        int selectedIndices[] = selectedIndices = dlg.getSelectedLeafIndices();
        if (0 < selectedIndices.length) {
            for (int i = 0; i < selectedIndices.length; i++) {
                WithEvidence nodeWith = new WithEvidence();
                nodeWith.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IKR);
                nodeWith.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
                nodeWith.setWith(leaves.get(selectedIndices[i]).getNode());
                ad.addWithEvidence(nodeWith);
            }
        }

        nvi.addGOAnnotation(newAnnotation);

        // Propagate to descendents
        AnnotationUtil.addIBAAnnotation(term, deletedAnnotNodes, newAnnotation, newSet);

        // Annotate with ancestor term
        if (true == dlg.areAncestorsApplicable()) {        
            GOTerm ancestorTerm = dlg.getAncestor();
            if (null != ancestorTerm) {
                descList.remove(gNode);
                descList.removeAll(getWithsFromLastIBD);
                //ArrayList<GeneNode> withsForAncestor = new ArrayList<GeneNode>(1);//(ArrayList<GeneNode>)withsWithoutPropagator.clone();
                //withsForAncestor.add(gNode);
                //AnnotationUtil.addIBAAnnotation(ancestorTerm, descList, withsForAncestor, origQualifierSet);
                AnnotationUtil.addIBAAnnotation(ancestorTerm.getAcc(), descList, propagatorAnnot, origQualifierSet);

                // Create IBA for the node
                Annotation ibaAnnot = new Annotation();
                ad = ibaAnnot.getAnnotationDetail();
    //            ad.addWith(propagatorAnnot);
                ad.setAnnotatedNode(gNode.getNode());
                ibaAnnot.setAnnotStoredInDb(true);
                WithEvidence withAnnotEv = new WithEvidence();
                withAnnotEv.setEvidenceCode(edu.usc.ksom.pm.panther.paintCommon.Evidence.CODE_IBA);
                withAnnotEv.setEvidenceType(WithEvidence.EVIDENCE_TYPE_ANNOT_PAINT_ANCESTOR);
                withAnnotEv.setWith(propagatorAnnot);
                ad.addWithEvidence(withAnnotEv);

                if (null != origQualifierSet) {
                    // Only add qualifiers that are valid for term
                    HashSet<Qualifier> validSet = null;
                    HashSet<Qualifier> qsetCopy = (HashSet<Qualifier>) origQualifierSet.clone();
                    validSet = new HashSet<Qualifier>();
                    for (Qualifier q : qsetCopy) {
                        if (true == gth.isQualifierValidForTerm(ancestorTerm, q)) {
                            validSet.add(q);
                        }
                    }
                    if (validSet.isEmpty()) {
                        validSet = null;
                    }

                    ibaAnnot.setQualifierSet(validSet);
                    if (null != validSet) {
                        for (Qualifier q : validSet) {
                            ad.addToInheritedQualifierLookup(q, propagatorAnnot);
                        }
                    }
                }
                ibaAnnot.setGoTerm(ancestorTerm.getAcc());
    //            newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
    //            newEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
    //            dbRef = new DBReference();
    //            dbRef.setEvidenceType(GOConstants.PAINT_REF);
    //            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
    //            newEvidence.addDbRef(dbRef);
    //
    //            with = new DBReference();
    //            with.setEvidenceType(GOConstants.PANTHER_DB);
    //            with.setEvidenceValue(gNode.getNode().getStaticInfo().getPublicId());
    //            newEvidence.addWith(with);
    //            ibaAnnot.setEvidence(newEvidence);
                nvi.addGOAnnotation(ibaAnnot);
    //            newAnnotation.setChildAnnotation(ibaAnnot);
    //            ibaAnnot.setParentAnnotation(newAnnotation);
    //                nvi.addGOAnnotation(newAnnotation);
            }
        }
        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
    }
    
//    public void notAnnotationOld(GeneNode gNode, Annotation a) {
//        edu.usc.ksom.pm.panther.paintCommon.Evidence e = a.getEvidence();
//
//        // Get qualifier
//        HashSet<Qualifier> origQualifierSet = a.getQualifierSet();
//        HashSet<Qualifier> newSet = new HashSet<Qualifier>();
//        if (null == origQualifierSet) {
//            Qualifier notQualifier = new Qualifier();
//            notQualifier.setText(Qualifier.QUALIFIER_NOT);
//            newSet.add(notQualifier);
//        } else {
//            boolean containsNot = false;
//            for (Qualifier q : origQualifierSet) {
//                if (q.isNot()) {
//                    containsNot = true;
//                    continue;
//                }
//                newSet.add(q);
//            }
//            if (false == containsNot) {
//                Qualifier notQualifier = new Qualifier();
//                notQualifier.setText(Qualifier.QUALIFIER_NOT);
//                newSet.add(notQualifier);
//            }
//        }
//        if (newSet.isEmpty()) {
//            newSet = null;
//        }
//
//        String term = a.getGoTerm();
//        // Can only update IBA
//        if (false == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(a.getSingleEvidenceCodeFromSet())) {
//            return;
//        }
//        Node node = gNode.getNode();
//        NodeVariableInfo nvi = node.getVariableInfo();
//        NodeStaticInfo nsi = node.getStaticInfo();
//
//        // Ensure this annotation was added by PAINT
//        ArrayList<DBReference> dbRefList = e.getDbReferenceList();
//        if (null == dbRefList) {
//            return;
//        }
//        boolean found = false;
//        for (DBReference dbRef : dbRefList) {
//            if (true == GOConstants.PAINT_REF.equals(dbRef.getEvidenceType()) && true == GO_Util.inst().getPaintEvidenceAcc().equals(dbRef.getEvidenceValue())) {
//                found = true;
//                break;
//            }
//        }
//        if (false == found) {
//            return;
//        }
//
//        // Get node that propagated this annotation
//        edu.usc.ksom.pm.panther.paintCommon.Evidence evidence = a.getEvidence();
//        ArrayList<DBReference> withs = evidence.getWiths();
//        GeneNode annotator = AnnotationUtil.getNonLeafPaintNodeFromWiths(withs);
//        if (null == annotator) {
//            return;
//        }
//        
//        ArrayList<GeneNode> withsWithoutPropagator = getReferencesWithoutNode(withs, annotator);
//        ArrayList<GeneNode> descList = new ArrayList<GeneNode>();
//        GeneNodeUtil.allNonPrunedDescendents(gNode, descList);
//        ArrayList<GeneNode> leaves = new ArrayList<GeneNode>();
//        ArrayList<GeneNode> defaultEvdnceLeaves = new ArrayList<GeneNode>();
//        GOTermHelper gth = PaintManager.inst().goTermHelper(); 
//        for (GeneNode cur : descList) {
//            if (cur.isLeaf()) {
//                AnnotationForTerm aft = new AnnotationForTerm(cur, gth.getTerm(a.getGoTerm()), gth);
////                if (false == aft.annotationExists()) {
////                    continue;
////                }
//                if (true == aft.annotationExists() && true == QualifierDif.areOpposite(a.getQualifierSet(), aft.getQset())) {
//                    defaultEvdnceLeaves.add(cur);
//                }
//                else {
//                    leaves.add(cur);
//                }
//            }
//        }
//
//        EvdnceCdeAndNewAnnotDlg dlg = new EvdnceCdeAndNewAnnotDlg(GUIManager.getManager().getFrame(), a, leaves, defaultEvdnceLeaves);
//        String evidenceCode = dlg.getSelectedEvidenceCode();
//        if (null == evidenceCode) {
//            return;
//        }
//
//        NodeStaticInfo annotatorInfo = annotator.getNode().getStaticInfo();
//        String ptnId = annotatorInfo.getPublicId();
//
//        // Remove annotation from node and applicable descendents
//        descList.add(gNode);        // Remove annotation from the node as well
//        HashSet<GeneNode> deletedAnnotNodes = new HashSet<GeneNode>();
//        for (GeneNode descNode : descList) {
//            Node n = descNode.getNode();
//            if (null == n) {
//                continue;
//            }
//            NodeVariableInfo descNvi = n.getVariableInfo();
//            if (null == descNvi) {
//                continue;
//            }
//            ArrayList<Annotation> descAnnotList = descNvi.getGoAnnotationList();
//            if (null == descAnnotList) {
//                continue;
//            }
//            
//            HashSet<Annotation> removeAnnotList = new HashSet<Annotation>();
//            for (Iterator<Annotation> descIter = descAnnotList.iterator(); descIter.hasNext();) {
//                Annotation descAnnot = descIter.next();
//                edu.usc.ksom.pm.panther.paintCommon.Evidence descEvi = descAnnot.getEvidence();
//                if (false == term.equals(descAnnot.getGoTerm())) {
//                    continue;
//                }
//
//                // Ensure this annotation was added by PAINT
//                ArrayList<DBReference> descDbRefList = descEvi.getDbReferenceList();
//                if (null == descDbRefList) {
//                    continue;
//                }
//
//                found = false;
//                for (DBReference descDbRef : descDbRefList) {
//                    if (true == GOConstants.PAINT_REF.equals(descDbRef.getEvidenceType()) && true == GO_Util.inst().getPaintEvidenceAcc().equals(descDbRef.getEvidenceValue())) {
//                        found = true;
//                        break;
//                    }
//                }
//                if (false == found) {
//                    continue;
//                }
//
//                // Check ancestor from whome this annotation was propagated
//                GeneNode gn = AnnotationUtil.getNonLeafPaintNodeFromWiths(descEvi.getWiths());
//                if (null == gn) {
//                    continue;
//                }
//                if (false == ptnId.equals(gn.getNode().getStaticInfo().getPublicId())) {
//                    continue;
//                }
//                
//                String code = descAnnot.getSingleEvidenceCodeFromSet();
//                if (true == GOConstants.DIVERGENT_EC.equals(code) || true == GOConstants.KEY_RESIDUES_EC.equals(code)) {
//                    Annotation childAnnot = descAnnot.getChildAnnotation();
//                    if (null != childAnnot) {
//                        ArrayList<GeneNode> childDesc = new ArrayList<GeneNode>();
//                        GeneNodeUtil.allNonPrunedDescendents(descNode, childDesc);
//                        AnnotationUtil.deletePropagatorsAnnotationFromDescendants(descNode, childAnnot, childDesc, false);
//                        removeAnnotList.add(childAnnot);
//                    }
//                }
//
//                
//                removeAnnotList.add(descAnnot);
//                if (false == deletedAnnotNodes.contains(descNode)) {
//                    deletedAnnotNodes.add(descNode);
//                }
//               
//            }
//            descAnnotList.removeAll(removeAnnotList);
//            if (descAnnotList.isEmpty()) {
//                descNvi.setGoAnnotationList(null);
//            }
//        }
//        // deletedAnnotNodes has node that is propagating.  Remove since we will be using this list later
//        deletedAnnotNodes.remove(gNode);        
//        System.out.println("Notting annotation for " + nsi.getPublicId());
//        // IRD 
//        if (GOConstants.DIVERGENT_EC.equals(evidenceCode)) {
//            Annotation newAnnotation = new Annotation();
//            newAnnotation.setAnnotStoredInDb(true);
//            newAnnotation.setQualifierSet(newSet);
//
//            newAnnotation.setGoTerm(term);  
//            edu.usc.ksom.pm.panther.paintCommon.Evidence newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            newEvidence.setEvidenceCode(GOConstants.DIVERGENT_EC);
//
//            DBReference dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            newEvidence.addDbRef(dbRef);
//
//            DBReference with = new DBReference();
//            with.setEvidenceType(GOConstants.PANTHER_DB);
//            with.setEvidenceValue(ptnId);
//            newEvidence.addWith(with);
//            
//            if (null != dlg.getPMID()) {
//                DBReference pmidRef = new DBReference();
//                pmidRef.setEvidenceType(DBReference.TYPE_PMID);
//                pmidRef.setEvidenceValue(dlg.getPMID());
//                newEvidence.addWith(pmidRef);
//            }
//            int selectedIndices[] = selectedIndices = dlg.getSelectedLeafIndices();
//            if (0 < selectedIndices.length) {
//                for (int i = 0; i < selectedIndices.length; i++) {
//                    newAnnotation.getAnnotationDetail().addNode(leaves.get(selectedIndices[i]).getNode());
//                }
//            }           
//            
//            
//            
//            
//            newAnnotation.setEvidence(newEvidence);
//            nvi.addGOAnnotation(newAnnotation);
//            
//            // Annotate with ancestor term
//            GOTerm ancestorTerm = dlg.getAncestor();
//            if (null != ancestorTerm) {
//                descList.remove(gNode);
//                descList.removeAll(withsWithoutPropagator);
//                ArrayList<GeneNode> withsForAncestor = new ArrayList<GeneNode>(1);//(ArrayList<GeneNode>)withsWithoutPropagator.clone();
//                withsForAncestor.add(gNode);
//                AnnotationUtil.addIBAAnnotationOld(ancestorTerm, descList, withsForAncestor, origQualifierSet);
//                
//                // Create IBA for the node
//                Annotation ibaAnnot = new Annotation();
//                ibaAnnot.setAnnotStoredInDb(true);                
//                if (null != origQualifierSet) {
//                    ibaAnnot.setQualifierSet((HashSet<Qualifier>)origQualifierSet.clone());
//                }
//                ibaAnnot.setGoTerm(ancestorTerm.getAcc());
//                newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//                newEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//                dbRef = new DBReference();
//                dbRef.setEvidenceType(GOConstants.PAINT_REF);
//                dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//                newEvidence.addDbRef(dbRef);
//
//                with = new DBReference();
//                with.setEvidenceType(GOConstants.PANTHER_DB);
//                with.setEvidenceValue(gNode.getNode().getStaticInfo().getPublicId());
//                newEvidence.addWith(with);
//                ibaAnnot.setEvidence(newEvidence);
//                nvi.addGOAnnotation(ibaAnnot);
//                newAnnotation.setChildAnnotation(ibaAnnot);
//                ibaAnnot.setParentAnnotation(newAnnotation);
////                nvi.addGOAnnotation(newAnnotation);
//            }
//            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
//            return;
//        }
//
//        // IKR
//        Annotation newAnnotation = new Annotation();
//        newAnnotation.setAnnotStoredInDb(true);
//        if (null != newSet) {
//            newAnnotation.setQualifierSet((HashSet<Qualifier>)newSet.clone());
//        }
//        newAnnotation.setGoTerm(term);                
//
//        edu.usc.ksom.pm.panther.paintCommon.Evidence newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//        newEvidence.setEvidenceCode(GOConstants.KEY_RESIDUES_EC);
//
//        DBReference dbRef = new DBReference();
//        dbRef.setEvidenceType(GOConstants.PAINT_REF);
//        dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//        newEvidence.addDbRef(dbRef);
//
//        // Withs  (PMID and leaf sequence)
//        DBReference propagatorRef = new DBReference();
//        propagatorRef.setEvidenceType(GOConstants.PANTHER_DB);
//        propagatorRef.setEvidenceValue(ptnId);
//        newEvidence.addWith(propagatorRef);
//
//        if (null != dlg.getPMID()) {
//            DBReference pmidRef = new DBReference();
//            pmidRef.setEvidenceType(DBReference.TYPE_PMID);
//            pmidRef.setEvidenceValue(dlg.getPMID());
//            newEvidence.addWith(pmidRef);
//        }
//        int selectedIndices[] = selectedIndices = dlg.getSelectedLeafIndices();
//        if (0 < selectedIndices.length) {
//            for (int i = 0; i < selectedIndices.length; i++) {
//                newAnnotation.getAnnotationDetail().addNode(leaves.get(selectedIndices[i]).getNode());
//            }
//        }
//        newAnnotation.setEvidence(newEvidence);
//        nvi.addGOAnnotation(newAnnotation);
//
//        // Set annotations for descendents
//        for (GeneNode desc : deletedAnnotNodes) {
//            Node n = desc.getNode();
//            NodeVariableInfo descNvi = n.getVariableInfo();
//            if (null == descNvi) {
//                descNvi = new NodeVariableInfo();
//                n.setVariableInfo(descNvi);
//            }
//
//            Annotation descAnnotation = new Annotation();
//            HashSet<Qualifier> qSet = null;
//            if (null != newSet) {
//                qSet = (HashSet<Qualifier>) newSet.clone();
//            }
//            descAnnotation.setQualifierSet(qSet);
//            descAnnotation.setGoTerm(term);
//            newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            newEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//
//            dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            newEvidence.addDbRef(dbRef);
//
//            // With (Ancestor that is propagating)
//            DBReference with = new DBReference();
//            with.setEvidenceType(GOConstants.PANTHER_DB);
//            with.setEvidenceValue(nsi.getPublicId());
//            newEvidence.addWith(with);
//            descAnnotation.setEvidence(newEvidence);
//            descNvi.addGOAnnotation(descAnnotation);
//
//        }
//        // Annotate with ancestor term
//        GOTerm ancestorTerm = dlg.getAncestor();
//        if (null != ancestorTerm) {
//            descList.remove(gNode);
//            descList.removeAll(withsWithoutPropagator);
//            ArrayList<GeneNode> withsForAncestor = new ArrayList<GeneNode>(1);//(ArrayList<GeneNode>)withsWithoutPropagator.clone();
//            withsForAncestor.add(gNode);
//            AnnotationUtil.addIBAAnnotationOld(ancestorTerm, descList, withsForAncestor, origQualifierSet);
//            
//            
//            // Create IBA for the node
//            Annotation ibaAnnot = new Annotation();
//            ibaAnnot.setAnnotStoredInDb(true);
//            if (null != origQualifierSet) {
//                ibaAnnot.setQualifierSet((HashSet<Qualifier>)origQualifierSet.clone());
//            }
//            ibaAnnot.setGoTerm(ancestorTerm.getAcc());
//            newEvidence = new edu.usc.ksom.pm.panther.paintCommon.Evidence();
//            newEvidence.setEvidenceCode(GOConstants.ANCESTRAL_EVIDENCE_CODE);
//            dbRef = new DBReference();
//            dbRef.setEvidenceType(GOConstants.PAINT_REF);
//            dbRef.setEvidenceValue(GO_Util.inst().getPaintEvidenceAcc());
//            newEvidence.addDbRef(dbRef);
//
//            DBReference with = new DBReference();
//            with.setEvidenceType(GOConstants.PANTHER_DB);
//            with.setEvidenceValue(gNode.getNode().getStaticInfo().getPublicId());
//            newEvidence.addWith(with);
//            ibaAnnot.setEvidence(newEvidence);
//            nvi.addGOAnnotation(ibaAnnot);
//            newAnnotation.setChildAnnotation(ibaAnnot);
//            ibaAnnot.setParentAnnotation(newAnnotation);            
//            
//            
//        }
//
//        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode));
//    }
    

    
    public ArrayList<GeneNode> getReferencesWithoutNode(ArrayList<DBReference> references, GeneNode gNode) {
        if (null == references || null == gNode) {
            return null;
        }
        ArrayList<GeneNode> outList = new ArrayList<GeneNode>();
        for (DBReference ref: references) {
            String type = ref.getEvidenceType();
            if (GOConstants.PANTHER_DB.equals(type)) {
                GeneNode curNode = PaintManager.inst().getGeneByPTNId(ref.getEvidenceValue());
                if (null != gNode) {
                    if (curNode == gNode) {
                        continue;
                    }

                    outList.add(curNode);
                }
            }
        }
        return outList; 
    }

}
