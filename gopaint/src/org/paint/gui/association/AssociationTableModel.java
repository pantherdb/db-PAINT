/**
 *  Copyright 2018 University Of Southern California
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
package org.paint.gui.association;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import com.sri.panther.paintCommon.Constant;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;;
import com.sri.panther.paintCommon.util.StringUtils;
import edu.usc.ksom.pm.panther.paint.matrix.NodeInfoForMatrix;
import edu.usc.ksom.pm.panther.paintCommon.WithEvidence;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import org.paint.datamodel.GeneNode;
import org.paint.go.GOConstants;
import org.paint.go.GO_Util;
import org.paint.gui.AspectSelector;
import org.paint.gui.DirtyIndicator;
import org.paint.gui.PaintTable;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.gui.evidence.PaintAction;
import org.paint.main.PaintManager;
import org.paint.util.AnnotationUtil;
import org.paint.util.GeneNodeUtil;
import org.paint.util.RenderUtil;

/**
 *
 * @author muruganu
 */
public class AssociationTableModel extends AbstractTableModel implements PaintTable{
    
	protected static final String TERM_COL_NAME = "Term";
	protected static final String CODE_COL_NAME = "ECO";
	protected static final String REFERENCE_COL_NAME= "Reference";
	protected static final String WITH_COL_NAME = "With";
        protected static final String COL_NAME_QUALIFIER_NOT = "NOT";
        protected static final String COL_NAME_QUALIFIER_COLOCALIZES_WITH = "Colocalizes with";
        protected static final String COL_NAME_QUALIFIER_CONTRIBUTES_TO = "Contributes to";
        protected static final String COL_NAME_DELETE = "Delete";
        
        public static final String INFERRED_NOT = "inferred NOT";
        public static final String INFERRED = "inferred";
	//protected static final String TRASH_COL_NAME = "DEL";

	protected static final String[] column_headings = {
		CODE_COL_NAME, 
		TERM_COL_NAME, 
		REFERENCE_COL_NAME,
		WITH_COL_NAME,
		COL_NAME_QUALIFIER_NOT,
                COL_NAME_QUALIFIER_COLOCALIZES_WITH,
                COL_NAME_QUALIFIER_CONTRIBUTES_TO,
                COL_NAME_DELETE
	};
        
        protected GeneNode gNode;
        protected ArrayList<Annotation> annotList;
        protected String aspect;
        protected GOTermHelper gth;
        protected PaintManager pm;
        protected AnnotationComparator ac = new AnnotationComparator();
        
        
        public AssociationTableModel() {
            
        }
        
        public Annotation getAnnotation(int row) {
            if (null == annotList) {
                return null;
            }
            if (row >= 0 && row < annotList.size()) {
                return annotList.get(row);
            }
            return null;
        }
        
        public void setNode(GeneNode gene) {
          
            gNode = gene;
            if (null == gene) {
                annotList = null;
                return;
            }              
//            aspect = AspectSelector.aspects.get(AspectSelector.inst().getAspect().toString());
            if (true == GeneNodeUtil.inPrunedBranch(gNode)) {
                annotList = null;
                return;                
            }
            pm = PaintManager.inst();
            gth = pm.goTermHelper();
            

            Node n = gNode.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null == nvi) {
                annotList = null;
                return;
            }
            ArrayList<Annotation> allAnnot = nvi.getGoAnnotationList();
            if (null == allAnnot) {
                annotList = null;
                return;
            }
            annotList = new ArrayList<Annotation>();
            annotList.addAll(allAnnot);
//            for (Annotation a: allAnnot) {
//                if (false == aspect.equals(gth.getTerm(a.getGoTerm()).getAspect())) {
//                    continue;
//                }
//                Evidence e = a.getEvidence();
//                if (true == e.isExperimental() || true == e.isPaint()) {
//                    annotList.add(a);
//                }
//               
//                
//            }
            Collections.sort(annotList, ac);
        }
        
        
        
        
    private class AnnotationComparator implements Comparator  {

        @Override
        public int compare(Object o1, Object o2) {
            String term1 = ((Annotation)o1).getGoTerm();
            String term2 = ((Annotation)o2).getGoTerm();
            if (null == term1 && null == term2) {
                return 0;
            }
            else if (null != term1 && null == term2) {
                return 1;
            }
            else if (null == term1 && null != term2) {
                return -1;
            }
            
            GOTerm a1 = AssociationTableModel.this.gth.getTerm(term1);
            GOTerm a2 = AssociationTableModel.this.gth.getTerm(term2);
            if (a1.equals(a2)) {
                return 0;
            }
            int comp = a1.getAspect().compareTo(a2.getAspect());
            if (0 == comp) {
                return a1.getAcc().compareTo(a2.getAcc());
            }
            return comp;
        }
    }

    @Override
    public int getRowCount() {
        if (null == annotList) {
            return 0;
        }
        return annotList.size();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public String getColumnName(int columnIndex) {
		return column_headings[columnIndex];
    }
    
    public Class getColumnClass(int columnIndex) {
        String tag = column_headings[columnIndex];
        if (tag.equals(COL_NAME_QUALIFIER_NOT) || tag.equals(COL_NAME_QUALIFIER_COLOCALIZES_WITH) ||
            tag.equals(COL_NAME_QUALIFIER_CONTRIBUTES_TO)) {
            return Boolean.class;
        }
        if (tag.equals(COL_NAME_DELETE)) {
            return DeleteButtonRenderer.class;
        }
        return String.class;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        String tag = column_headings[columnIndex];
        // Non-editable columns
        if ((CODE_COL_NAME.equals(tag)) || (TERM_COL_NAME.equals(tag)) || (REFERENCE_COL_NAME.equals(tag)) || (WITH_COL_NAME.equals(tag)) ||
            (COL_NAME_QUALIFIER_CONTRIBUTES_TO.equals(tag))  || (COL_NAME_QUALIFIER_COLOCALIZES_WITH.equals(tag))) {
            return false;
        }
        Object cell = getValueAt(rowIndex, columnIndex);
        if (null == cell || false == cell instanceof Boolean) {
            return false;
        }

        Annotation a = annotList.get(rowIndex);
        String code = a.getSingleEvidenceCodeFromSet();
//        Evidence e = a.getEvidence();
//        String code = e.getEvidenceCode();
        
        // Cannot un NOT a NOT - i.e. when we have a NOT (from IKR) and its descendants get IBA's with 'NOT', these IBA's cannot be updated.
        AnnotationDetail ad = a.getAnnotationDetail();
        if (true == tag.equals(COL_NAME_QUALIFIER_NOT) && true == Evidence.CODE_IBA.equals(code) && null != ad) {
            HashSet<Annotation> withAnnots = ad.getWithAnnotSet();
            if (null != withAnnots) {
                for (Annotation with: withAnnots) {
                    String evCodeWith = with.getSingleEvidenceCodeFromSet();
                    if (Evidence.CODE_IKR.equals(evCodeWith)) {
                        return false;
                    }
                }
            }
        }


        // NOT for IBA only.  IBA cannot be the result of an IKR or IRD (i.e. has parent)
        if (true == tag.equals(COL_NAME_QUALIFIER_NOT) && true == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(code) && false == AnnotationUtil.isIBAForIKRorIRD(a, gNode)) {
//        if (true == tag.equals(COL_NAME_QUALIFIER_NOT) && (true == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(code) || true == GOConstants.KEY_RESIDUES_EC.equals(code) || true == GOConstants.DIVERGENT_EC.equals(code))) {
            return true;
        }
                        // getvalueAt should return null
//                        else if (true == tag.equals(COL_NAME_QUALIFIER_NOT) && false == GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(code)) {
//                            return false;
//                        }
        // Cannot update contributes to or colocalizes with
//        if (tag.equals(COL_NAME_QUALIFIER_CONTRIBUTES_TO) || tag.equals(COL_NAME_QUALIFIER_COLOCALIZES_WITH)) {
//            return false;
//        }
        if (tag.equals(COL_NAME_DELETE)) {
            return true;
        }

//                        Aspect curAspect = AspectSelector.inst().getAspect();                        
//                        
//                        // No qualifiers for biological process
//                        if (curAspect.equals(Aspect.BIOLOGICAL_PROCESS)) {
//                            return false;
//                        }
//                        
//                        if (curAspect.equals(Aspect.MOLECULAR_FUNCTION) && true == tag.equals(COL_NAME_QUALIFIER_CONTRIBUTES_TO) && GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(code)) {
//                            return true;
//                        }
//                        if (curAspect.equals(Aspect.CELLULAR_COMPONENT) && true == tag.equals(COL_NAME_QUALIFIER_COLOCALIZES_WITH) && GOConstants.ANCESTRAL_EVIDENCE_CODE.equals(code)) {
//                            return true;
//                        }
//			return false;
        return false;
    }

    @Override
    public int getColumnCount() {
        return column_headings.length;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (null == annotList) {
            return null;
        }
        Annotation a = annotList.get(rowIndex);
	String tag = column_headings[columnIndex];

        HashSet<String> actualSet = a.getEvidenceCodeSet();
        HashSet<String> displayEvSet = new HashSet<String>();
        boolean isExperimental = false;
        boolean isPaint = false;
        for (String code: actualSet) {
            if (Evidence.isExperimental(code)) {
                displayEvSet.add(code);
                isExperimental = true;
                continue;
            }
            else if (Evidence.isPaint(code)) {
                displayEvSet.add(code);
                isPaint = true;   
            }
        }
        String evidenceCode = StringUtils.listToString(displayEvSet, Constant.STR_EMPTY, Constant.STR_COMMA);
        
        if (CODE_COL_NAME.equals(tag)) {
            return evidenceCode;
        }
        if (TERM_COL_NAME.equals(tag)) {
            GOTermHelper gth = PaintManager.inst().goTermHelper();
            GOTerm term = gth.getTerm(a.getGoTerm());
            
            NodeInfoForMatrix nifm = new NodeInfoForMatrix(gNode, term, gth);
            String inferred = null;
            boolean containsNeg = QualifierDif.containsNegative(gth.getValidQualifiersForTerm(term, a.getQualifierSet()));
            if (false == containsNeg && (true == nifm.isExpNot() || true == nifm.isNonExpNot())) {
                inferred = INFERRED_NOT;
            }
            else if (true == containsNeg && (false == nifm.isExpNot() || false == nifm.isNonExpNot())) {
                inferred = INFERRED;
            }
            
            
            Color aspectColor = RenderUtil.getAspectColor(AspectSelector.LETTER_TO_ASPECT.get(term.getAspect()));

            String hexColor = String.format("#%02X%02X%02X", aspectColor.getRed(), aspectColor.getGreen(), aspectColor.getBlue());
//            return term.getName() + "(" + term.getAcc() + ")";
            
            String style = "<head><style> " + 
                            " body { " + 
                            " background-color: " + hexColor + " ; " + 
                            "} " + 
                            "</style> </head>" ;
            
            if (QualifierDif.containsNegative(a.getQualifierSet())) {
                return "<html>" + style + "<body><s>" + term.getName() + "</s> (<a href=\"" + AssociationTable.URL_LINK_PREFIX_AMIGO + term.getAcc() + "\" >" + term.getAcc() + "</a>)</body></html>";
            }
            else if (true == INFERRED_NOT.equals(inferred)) {
                return "<html>" + style + "<body><s>" + term.getName() + "</s> (<a href=\"" + AssociationTable.URL_LINK_PREFIX_AMIGO + term.getAcc() + "\" >" + term.getAcc() + "</a>) " + inferred + " </body></html>";                
            }
            else if (true == INFERRED.equals(inferred)) {
                return "<html>" + style + "<body>" + term.getName() + " (<a href=\"" + AssociationTable.URL_LINK_PREFIX_AMIGO + term.getAcc() + "\" >" + term.getAcc() + "</a>) " + inferred + " </body></html>";
                
            }
            else {
                return "<html>" + style + "<body>" + term.getName() + " (<a href=\"" + AssociationTable.URL_LINK_PREFIX_AMIGO + term.getAcc() + "\" >" + term.getAcc() + "</a>)</body></html>";
            }
//            String rtn =  "<html>" + term.getName() + " (<a href=\"http://amigo.geneontology.org/amigo/term/" + term.getAcc() + "\" >" + term.getAcc() + "</a>)</html>";
//            System.out.println(rtn);
//            return rtn;
        }
        if (REFERENCE_COL_NAME.equals(tag)) {
            if (true == isExperimental && false == isPaint) {
                // Experimental and non-paint annotations get "with" information in reference column
                return getTextForWith(a);
            }

            HashSet<WithEvidence> withSet = a.getAnnotationDetail().getWithEvidenceSet();
            if (null == withSet || 0 == withSet.size()) {
                return Constant.STR_EMPTY;
            }
            StringBuffer sb = new StringBuffer();            
            HashMap<String, String> lookup = new HashMap<String, String>();
            for (WithEvidence we: withSet) {
                String type = we.getEvidenceType();
                String value = null;
                if (true == we.isPAINTType()) {
                    type = GOConstants.PAINT_REF;
                    value = GO_Util.inst().getPaintEvidenceAcc();
                }
                else {
                    continue;
                }
                String key = type + value;
                if (true == lookup.containsKey(key)) {
                    continue;
                }
                lookup.put(key, key);
                sb.append(type);
                sb.append(Constant.STR_COLON);
                sb.append(value);
                sb.append(Constant.STR_SPACE);
            }
            return sb.toString().trim();
        }
        if (WITH_COL_NAME.equals(tag)) {
            if (true == isExperimental && false == isPaint) {
                return Constant.STR_EMPTY;
            }
            return getTextForWith(a);
        }
        if (COL_NAME_QUALIFIER_NOT.equals(tag)) {
            // Cannot change not for non sequence annotations
//            Evidence e = a.getEvidence();
//            if (false == GOConstants.DESCENDANT_SEQUENCES_EC.equals(e.getEvidenceCode())) {
//                return null;
//            }
            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
            qualifierSet = gth.getValidQualifiersForTerm(gth.getTerm(a.getGoTerm()), qualifierSet);
            if (null == qualifierSet) {
                return Boolean.FALSE;
            }
            for (Iterator<Qualifier> iter = qualifierSet.iterator(); iter.hasNext();) {
                Qualifier q = iter.next();
                if (true == q.isNot()) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        if (COL_NAME_QUALIFIER_COLOCALIZES_WITH.equals(tag)) {
            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
            GOTerm term = gth.getTerm(a.getGoTerm());
            if (false == gth.canTermHaveQualifier(term, Qualifier.QUALIFIER_COLOCALIZES_WITH)) {
                return null;
            }
            qualifierSet = gth.getValidQualifiersForTerm(term, qualifierSet);            
            if (null == qualifierSet) {
                return Boolean.FALSE;
            }
            for (Iterator<Qualifier> iter = qualifierSet.iterator(); iter.hasNext();) {
                Qualifier q = iter.next();
                if (true == q.isColocalizesWith()) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        if (COL_NAME_QUALIFIER_CONTRIBUTES_TO.equals(tag)) {
            HashSet<Qualifier> qualifierSet = a.getQualifierSet();
            GOTerm term = gth.getTerm(a.getGoTerm());
            if (false == gth.canTermHaveQualifier(term, Qualifier.QUALIFIER_CONTRIBUTES_TO)) {
                return null;
            }            
            qualifierSet = gth.getValidQualifiersForTerm(gth.getTerm(a.getGoTerm()), qualifierSet);            
            if (null == qualifierSet) {
                return Boolean.FALSE;
            }
            for (Iterator<Qualifier> iter = qualifierSet.iterator(); iter.hasNext();) {
                Qualifier q = iter.next();
                if (true == q.isContributesTo()) {
                    return Boolean.TRUE;
                }
            }
            return Boolean.FALSE;
        }
        if (COL_NAME_DELETE.equals(tag)) {
            //Evidence e = a.getEvidence();
            String code = a.getSingleEvidenceCodeFromSet();
            if (true == GOConstants.DESCENDANT_SEQUENCES_EC.equals(code) || true == GOConstants.KEY_RESIDUES_EC.equals(code) || true == GOConstants.DIVERGENT_EC.equals(code)) {
                return Boolean.TRUE;
            }
            return null;
        }
        
        return Constant.STR_EMPTY;        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public Vector<WithEvidence> sortWithEvidence(HashSet<WithEvidence> origList) {
        if (null == origList) {
            return null;
        }
        Vector<WithEvidence> rtnList = new Vector<WithEvidence>(origList.size());
        for (WithEvidence withEv: origList) {
            String code = withEv.getEvidenceCode();
            if (false == Evidence.isExperimental(code) && false == Evidence.isPaint(code)) {
                rtnList.add(withEv);
                continue;
            }
            DBReference withDBRef = (DBReference) withEv.getWith();
            if (DBReference.TYPE_PMID.equals(withDBRef.getEvidenceType())) {
                rtnList.insertElementAt(withEv, 0);
            }
        }
        return rtnList;
    }
    
    public String getTextForWith(Annotation a) {
        AnnotationDetail ad = a.getAnnotationDetail();
        HashSet<WithEvidence> withEvSet = ad.getWithEvidenceDBRefSet();
        StringBuffer sb = new StringBuffer();        
        if (null != withEvSet) {
            Vector<WithEvidence> withDBRefSorted = sortWithEvidence(withEvSet);
            for (WithEvidence withEv : withDBRefSorted) {
                // Only display experimental and PAINT codes
                String code = withEv.getEvidenceCode();
                if (false == Evidence.isExperimental(code) && false == Evidence.isPaint(code)) {
                    continue;
                }
                DBReference withDBRef = (DBReference) withEv.getWith();
                sb.append(getDBValueFormatPMID(withDBRef).toString());
            }
        }
        LinkedHashSet<Node> addedList = new LinkedHashSet<Node>();
        withEvSet = ad.getWithEvidenceAnnotSet();
        if (null != withEvSet) {
            for (WithEvidence withEv : withEvSet) {
                Annotation withAnnot = (Annotation) withEv.getWith();
                if ((Evidence.CODE_IKR.equals(withAnnot.getSingleEvidenceCodeFromSet()) || Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet())) && withAnnot == a) {
                    continue;
                }
                addedList.add(withAnnot.getAnnotationDetail().getAnnotatedNode());
            }
        }

        HashSet<Node> nodeSet = ad.getWithNodeSet();
        if (null != nodeSet) {
            for (Node n : nodeSet) {
                addedList.add(n);
            }
        }

        for (Node node : addedList) {
                //sb.append(pm.getGeneByPTNId(node.getStaticInfo().getPublicId()).getNodeLabel() + node.getStaticInfo().getPublicId());

            sb.append(getNodeHttpLink(pm.getGeneByPTNId(node.getStaticInfo().getPublicId())));
            sb.append(Constant.STR_SPACE);
        }

        return "<html><body>" + sb.toString().trim() + "</body></html>";
    }

    public ArrayList<String> getLinksForReferenceCol(Annotation a) {
        HashSet<String> actualSet = a.getEvidenceCodeSet();
        HashSet<String> displayEvSet = new HashSet<String>();
        boolean isExperimental = false;
        boolean isPaint = false;
        for (String code : actualSet) {
            if (Evidence.isExperimental(code)) {
                displayEvSet.add(code);
                isExperimental = true;
                continue;
            } else if (Evidence.isPaint(code)) {
                displayEvSet.add(code);
                isPaint = true;
            }
        }
        if (true == isExperimental && false == isPaint) {
            // Experimental and non-paint annotations get "with" information in reference column
            return getLinksForAnnot(a);
        }
        return null;
    }
    
    public ArrayList<String> getLinksForWithCol(Annotation a) {
        return getLinksForAnnot(a);
    }
    
    public ArrayList<String> getLinksForAnnot(Annotation a) {
        ArrayList<String> rtnList = new ArrayList<String>();
        AnnotationDetail ad = a.getAnnotationDetail();

        LinkedHashSet<Node> addedList = new LinkedHashSet<Node>();
        HashSet<WithEvidence> withEvSet = ad.getWithEvidenceAnnotSet();
        if (null != withEvSet) {
            for (WithEvidence withEv : withEvSet) {
                Annotation withAnnot = (Annotation) withEv.getWith();
                if ((Evidence.CODE_IKR.equals(withAnnot.getSingleEvidenceCodeFromSet()) || Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet())) && withAnnot == a) {
                    continue;
                }
                addedList.add(withAnnot.getAnnotationDetail().getAnnotatedNode());
            }
        }

        HashSet<Node> nodeSet = ad.getWithNodeSet();
        if (null != nodeSet) {
            for (Node n : nodeSet) {
                addedList.add(n);
            }
        }

        for (Node node : addedList) {
            rtnList.add(AssociationTable.URL_LINK_PREFIX_PANTREE_NODE + node.getStaticInfo().getPublicId());
        }
        HashSet<DBReference> dbSet = ad.getWithOtherSet();
        if (null != dbSet) {
            for (DBReference dbRef : dbSet) {
                if (DBReference.TYPE_PMID.equals(dbRef.getEvidenceType())) {
                    rtnList.add(AssociationTable.URL_LINK_PREFIX_PMID + dbRef.getEvidenceValue());
                }
            }
        }
        if (rtnList.isEmpty()) {
            return null;
        }
        return rtnList;
    }
    

    @Override
    public String getTextAt(int row, int column) {
        if (null == annotList) {
            return null;
        }
        Annotation a = annotList.get(row);
	String tag = column_headings[column];
        if (CODE_COL_NAME.equals(tag)) {
            HashSet<String> evidenceCodeSet = a.getEvidenceCodeSet();
            return StringUtils.listToString(evidenceCodeSet, Constant.STR_EMPTY, Constant.STR_COMMA);
        }
        if (TERM_COL_NAME.equals(tag)) {
            GOTerm term = PaintManager.inst().goTermHelper().getTerm(a.getGoTerm());
            return term.getName() + "(" + term.getAcc() + ")";
        }
        if (REFERENCE_COL_NAME.equals(tag)) {
            HashSet<WithEvidence> withSet = a.getAnnotationDetail().getWithEvidenceSet();
            if (null == withSet || 0 == withSet.size()) {
                return Constant.STR_EMPTY;
            }
            StringBuffer sb = new StringBuffer();            
            HashMap<String, String> lookup = new HashMap<String, String>();
            for (WithEvidence we: withSet) {
                String type = we.getEvidenceType();
                String value = we.getEvidenceId();
                if (true == we.isPAINTType()) {
                    type = GOConstants.PAINT_REF;
                    value = GO_Util.inst().getPaintEvidenceAcc();
                }
                String key = type + value;
                if (true == lookup.containsKey(key)) {
                    continue;
                }
                lookup.put(key, key);
                sb.append(type);
                sb.append(Constant.STR_COLON);
                sb.append(value);
                sb.append(Constant.STR_SPACE);
            }
            return sb.toString().trim();
        }
        if (WITH_COL_NAME.equals(tag)) {
            AnnotationDetail ad = a.getAnnotationDetail();
            LinkedHashSet<Node> addedList = new LinkedHashSet<Node>();
            HashSet<Annotation> withs = ad.getWithAnnotSet();
            if (null != withs) {
                for (Annotation with: withs) {
                    if ((Evidence.CODE_IKR.equals(a.getSingleEvidenceCodeFromSet()) || Evidence.CODE_IRD.equals(a.getSingleEvidenceCodeFromSet())) && with == a) {
                        continue;
                    }
                    addedList.add(with.getAnnotationDetail().getAnnotatedNode());
                }
            }
            
            HashSet<Node> nodeSet = ad.getWithNodeSet();
            if (null != nodeSet) {
                for (Node n: nodeSet) {
                    addedList.add(n);
                }
            }
            StringBuffer sb = new StringBuffer();
            
            for (Node node: addedList) {
                sb.append(pm.getGeneByPTNId(node.getStaticInfo().getPublicId()).getNodeLabel()+ node.getStaticInfo().getPublicId());
                sb.append(Constant.STR_SPACE);
            }
            HashSet<DBReference> dbSet = ad.getWithOtherSet();
            if (null != dbSet) {
                for (DBReference dbref: dbSet) {
                    sb.append(getDBValue(dbref).toString());
                }
            }
            return sb.toString().trim();
        }
        return Constant.STR_EMPTY;

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private StringBuffer getDBValue(DBReference dbRef) {
        StringBuffer sb = new StringBuffer();
        if (GOConstants.PANTHER_DB.equals(dbRef.getEvidenceType())) {
            String value = dbRef.getEvidenceValue();
            GeneNode gn = PaintManager.inst().getGeneByPTNId(value);
            if (null != gn && gn.isLeaf()) {
                sb.append(gn.getNodeLabel());
                sb.append(Constant.STR_SPACE);                
                return sb;
            }
        }

        sb.append(dbRef.getEvidenceType());
        sb.append(Constant.STR_COLON);
        sb.append(dbRef.getEvidenceValue());
        sb.append(Constant.STR_SPACE);

        return sb;
    }
    
 
    private StringBuffer getNodeHttpLink(GeneNode gNode) {
        StringBuffer sb = new StringBuffer();
        String nodeLabel = gNode.getNodeLabelWithPTN();
        if (null == nodeLabel || 0 == nodeLabel.trim().length()) {
            nodeLabel = gNode.getNode().getStaticInfo().getPublicId();
        }
        sb.append("<a href=\"" + AssociationTable.URL_LINK_PREFIX_PANTREE_NODE + gNode.getNode().getStaticInfo().getPublicId() + "\">" + nodeLabel + "</a>");
        sb.append(Constant.STR_SPACE);
        return sb;
    }
    
    private StringBuffer getDBValueFormatPMID(DBReference dbRef) {
        StringBuffer sb = new StringBuffer();
        String evidenceType = dbRef.getEvidenceType();
        if (GOConstants.PANTHER_DB.equals(evidenceType)) {
            System.out.println("Dont expect to get here anymore");
            String value = dbRef.getEvidenceValue();
            GeneNode gn = PaintManager.inst().getGeneByPTNId(value);
            if (null != gn && gn.isLeaf()) {
                sb.append(gn.getNodeLabel());
                sb.append(Constant.STR_SPACE);                
                return sb;
            }
        }
        if (DBReference.TYPE_PMID.equals(evidenceType)) {
            sb.append("<a href=\"" + AssociationTable.URL_LINK_PREFIX_PMID + dbRef.getEvidenceValue() + "\"> PMID:" + dbRef.getEvidenceValue() + "</a>");
            sb.append(Constant.STR_SPACE);
            return sb;
        }

        sb.append(evidenceType);
        sb.append(Constant.STR_COLON);
        sb.append(dbRef.getEvidenceValue());
        sb.append(Constant.STR_SPACE);

        return sb;
    }    

    @Override
    public boolean isSquare(int column) {
        String tag = getColumnName(column);
        if (tag.equals(COL_NAME_QUALIFIER_NOT) || tag.equals(COL_NAME_QUALIFIER_COLOCALIZES_WITH) || 
            tag.equals(COL_NAME_QUALIFIER_CONTRIBUTES_TO) || tag.equals(COL_NAME_DELETE)) {
            return true;
        }
        return false;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void deleteRow(int rowIndex) {
        if (null == annotList || rowIndex > annotList.size() || rowIndex < 0) {
            return;
        }
        Annotation a = annotList.get(rowIndex);
        AnnotationUtil.deleteAnnotation(gNode, a);
        DirtyIndicator.inst().setAnnotated(true);        
        EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode)); 
//        Annotation delAnnot = PaintAction.inst().deleteAnnotation(gNode, a);
//        if (null != delAnnot) {
//            annotList.remove(delAnnot);
//            EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode)); 
//        }
//        String code = a.getEvidence().getEvidenceCode();
//        if (GOConstants.DESCENDANT_SEQUENCES_EC.equals(code)) {
//            PaintAction.inst().deleteIBDAnnotation(gNode, a);        
//        }
//        else if (GOConstants.DIVERGENT_EC.equals(code)) {
//            PaintAction.inst().deleteIRD(gNode, a);
//        }
//        else if (GOConstants.KEY_RESIDUES_EC.equals(code)) {
//            PaintAction.inst().deleteIKR(gNode, a);
//        }
//        else {
//            System.out.println("Delete operation not supported");
//        }
    }
    
    public void notAnnotation(int rowIndex) {
        if (null == annotList || rowIndex > annotList.size() || rowIndex < 0) {
            return;
        }
        Annotation a = annotList.get(rowIndex);
        PaintAction.inst().notAnnotation(gNode, a);              
    }
    
}
