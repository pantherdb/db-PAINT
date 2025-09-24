/**
 * Copyright 2025 University Of Southern California
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
package org.paint.gui.association;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.StringUtils;
import edu.usc.ksom.pm.panther.paint.matrix.NodeInfoForMatrix;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;
import edu.usc.ksom.pm.panther.paintCommon.WithEvidence;
import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.paint.go.GOConstants;
import org.paint.go.GO_Util;
import org.paint.gui.AspectSelector;
import static org.paint.gui.association.AssociationTableModel.column_headings;
import org.paint.gui.evidence.PaintAction;
import org.paint.main.PaintManager;
import org.paint.util.RenderUtil;

/**
 *
 * @author Precision
 */
public class ExpAssociationTableModel extends AssociationTableModel {
    
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
        if (columnIndex < 0 || columnIndex >= column_headings.length) {
            return false;
        }
        String tag = column_headings[columnIndex];
        // Non-editable columns
        if (false == tag.equals(COL_NAME_DELETE)) {
            return false;
        }
        Annotation a = annotList.get(rowIndex);
        if (false == a.isExpAnnotCreatedInPaint()) {
            return false;
        }
        return true;
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
//        boolean isPaint = false;
        for (String code: actualSet) {

            displayEvSet.add(code);

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
            if (true == a.isExperimental()) {
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
            if (a.isExperimental()) {
                return Constant.STR_EMPTY;
            }
            return getTextForWith(a);
        }
        if (COL_NAME_QUALIFIER_NOT.equals(tag)) {
            // Cannot update TCV
            if (Evidence.CODE_TCV.equals(a.getSingleEvidenceCodeFromSet())) {
                return false;
            }            
            
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
            if (true == isCellEditable(rowIndex, columnIndex)) {
                return Boolean.TRUE;
            }
            return null;
        }
        
        return Constant.STR_EMPTY;        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
    
    public void deleteRow(int rowIndex) {
        if (null == annotList || rowIndex > annotList.size() || rowIndex < 0) {
            return;
        }
        Annotation a = annotList.get(rowIndex);
        PaintAction.inst().removeExperimentalAnnotation(this.gNode, a);
        
        //AnnotationHelper.deleteAnnotationAndRepropagate(a, pm.getTaxonHelper(), pm.goTermHelper());
//        AnnotationUtil.deleteAnnotation(gNode, a);
        //DirtyIndicator.inst().setAnnotated(true);        
        //EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(gNode)); 
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
}
