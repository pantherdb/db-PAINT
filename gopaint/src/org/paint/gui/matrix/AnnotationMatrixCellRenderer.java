/**
 *  Copyright 2021 University Of Southern California
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
package org.paint.gui.matrix;

import com.sri.panther.paintCommon.util.Utils;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paint.matrix.NodeInfoForMatrix;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;

/**
 *
 * @author muruganu
 */
public class AnnotationMatrixCellRenderer extends JLabel implements TableCellRenderer{
 
    public static final String STR_EMPTY = "";
    public static final String STR_BRACKET_START = "(";
    public static final String STR_BRACKET_END = ")";
    public static final String STR_COMMA = ",";
    public static final String STR_COLON = ":";
    public static final String STR_ROW = "Row ";
    public static final String STR_COL = " Col ";
    public static final String STR_SPACE = " ";
    public static final String STR_HTML_START = "<HTML>";
    public static final String STR_HTML_END = "</HTML>";
    public static final String STR_HTML_BREAK = "<BR>";    
    
    private static final Color PAINT_COLOR_EXP = Preferences.inst().getExpPaintColor();//new Color(16, 128, 64);
//    private static final Color curatedPaintColor = new Color(255, 127, 0);
    private static final Color PAINT_COLOR_INFER = Preferences.inst().getInferPaintColor();//new Color(68, 116, 179);
    public static final Color COLOR_BASIC = new Color(155, 205, 255);
    public static final Color COLOR_CONTRAST = new Color(233, 236, 242);
    
    NodeInfoForMatrix nodeInfo;
    Qualifier qualifier;
    boolean selected;
    Color backgroundColor;
    String label;
    GeneNode node;
    int row;
    int column;
    
    public AnnotationMatrixCellRenderer() {
		setText("");
                nodeInfo = null;
                qualifier = null;
                selected = false;
                backgroundColor = null;
                label = null;
                node = null;
		//setOpaque(true); //MUST do this for background to show up.        
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(STR_EMPTY);
        nodeInfo = null;
        qualifier = null;
        selected = isSelected;
        backgroundColor = null;
        label = null;
        node = null;
        this.row = row;
        this.column = column;
                
        AnnotationMatrix annot_table = (AnnotationMatrix) table;
        AnnotationMatrixModel model = (AnnotationMatrixModel) table.getModel();
        nodeInfo = (NodeInfoForMatrix) value;
        if (null == nodeInfo) {
            System.out.println("Could not find cell Renderer component for row " + row + " column " + column);
            Exception e = new Exception();
            e.printStackTrace();
            return null;
        }
        node = nodeInfo.getgNode();
        if (node.isSelected() || annot_table.getSelectedColumn() == column) {
            selected = true;
        }
//        if (true == "GO:0004842".equals(nodeInfo.getgTerm().getAcc()) && true == "PTN001295675".equals(node.getNode().getStaticInfo().getPublicId())) {
//            System.out.println("Here");
//        }        
        
//        if (annot_table.getSelectedColumn() == column || true == selected) {
//            this.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
//        } else {
//            this.setBorder(null);
//        }     
        
        backgroundColor = COLOR_BASIC;

        if (false == model.isOdd(column)) {
            backgroundColor = COLOR_CONTRAST;  //    Don't want to use  Color.LIGHT_GRAY else cannot differentiate cells when border is painted using light grey as well 
        }
        // Handle no annotations
        if (false == nodeInfo.isExpBackground() && false == nodeInfo.isNonExpBackground()) {
            String tooltip = nodeInfo.getgTerm().getName() + STR_BRACKET_START + nodeInfo.getgTerm().getAcc() + STR_BRACKET_END;
            if (null == tooltip || tooltip.isEmpty()) {
                tooltip = nodeInfo.getgTerm().getAcc();
            }
            setToolTipText(STR_HTML_START + STR_ROW + (this.row + 1)  + STR_COL + (this.column + 1) + STR_SPACE + tooltip + STR_HTML_END);
            return this;
        }
        else if (true == nodeInfo.isExpBackground()) {
            // Experimental evidence
            backgroundColor = PAINT_COLOR_EXP;
            String termStr = nodeInfo.getgTerm().getName() + STR_BRACKET_START + nodeInfo.getgTerm().getAcc() + STR_BRACKET_END;
            if (null == termStr || termStr.isEmpty()) {
                termStr = nodeInfo.getgTerm().getAcc();
            }
            //setToolTipText(STR_ROW + (this.row + 1)  + STR_COL + (this.column + 1) + STR_SPACE + node.getNodeLabel() + STR_BRACKET_START + node.getNode().getStaticInfo().getNodeAcc() + STR_SPACE +  node.getNode().getStaticInfo().getPublicId() + STR_BRACKET_END + STR_SPACE + termStr + STR_SPACE + getQualifierString(nodeInfo.getqSet()));
            setToolTipText(STR_HTML_START + STR_ROW + (this.row + 1)  + STR_COL + (this.column + 1) + STR_SPACE + node.getNodeLabel() + STR_BRACKET_START + node.getNode().getStaticInfo().getNodeAcc() + STR_SPACE +  node.getNode().getStaticInfo().getPublicId() + STR_BRACKET_END + STR_SPACE + termStr + STR_HTML_BREAK + getAllQualifierInfo(nodeInfo, STR_HTML_BREAK) + STR_HTML_END);
            return this;
            
        }
        else if (false == nodeInfo.isExpBackground() && true == nodeInfo.isNonExpBackground()) {
            backgroundColor = PAINT_COLOR_INFER;
        
            String termStr = nodeInfo.getgTerm().getName() + STR_BRACKET_START + nodeInfo.getgTerm().getAcc() + STR_BRACKET_END;
            if (null == termStr || termStr.isEmpty()) {
                termStr = nodeInfo.getgTerm().getAcc();
            }
            //setToolTipText(STR_ROW + (this.row + 1) + STR_COL + (this.column + 1) + STR_SPACE + node.getNodeLabel() + STR_BRACKET_START + node.getNode().getStaticInfo().getNodeAcc() + STR_SPACE +  node.getNode().getStaticInfo().getPublicId() + STR_BRACKET_END + STR_SPACE + termStr + STR_SPACE + getQualifierString(nodeInfo.getNonQset()));
            setToolTipText(STR_HTML_START + STR_ROW + (this.row + 1) + STR_COL + (this.column + 1) + STR_SPACE + node.getNodeLabel() + STR_BRACKET_START + node.getNode().getStaticInfo().getNodeAcc() + STR_SPACE +  node.getNode().getStaticInfo().getPublicId() + STR_BRACKET_END + STR_SPACE + termStr + STR_HTML_BREAK + getAllQualifierInfo(nodeInfo, STR_HTML_BREAK) + STR_HTML_END);
            return this;
        }
        return this;

    }

//    @Override
//    public Component getTableCellRendererComponentOld(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//        setText("");
//        nodeInfo = null;
//        qualifier = null;
//        selected = false;
//        backgroundColor = null;
//        label = null;
//        node = null;
//                
//        AnnotationMatrix annot_table = (AnnotationMatrix) table;
//        AnnotationMatrixModel model = (AnnotationMatrixModel) table.getModel();
//        nodeInfo = (NodeInfoForGroup) value;
//   
//        
//        backgroundColor = Color.WHITE;
//
//        if (false == model.isOdd(column)) {
//            backgroundColor = Color.LIGHT_GRAY;
//        }        
//        //setBackground(backgroundColor);
//        
//        //selected = false;
//        if (null == nodeInfo) {
//            GOTerm goTerm = model.getTermForColumn(column);
//            String tooltip = goTerm.getName();
//            if (null == tooltip || tooltip.isEmpty()) {
//                tooltip = goTerm.getAcc();
//            }
//            setToolTipText(tooltip);
//            return this;
//        }
//        node = model.getNode(row);
//        if (true == GeneNodeUtil.inPrunedBranch(node)) {
//            GOTerm goTerm = model.getTermForColumn(column);
//            String tooltip = goTerm.getName();
//            if (null == tooltip || tooltip.isEmpty()) {
//                tooltip = goTerm.getAcc();
//            }
//            setToolTipText(tooltip);          
//            return this;
//        }
//        Annotation annot = nodeInfo.getAnnotation();
//        //selected = true;
//
//
//        Color foregroundColor = PAINT_COLOR_INFER;  //Preferences.inst().getInferPaintColor();
//        Evidence e = annot.getEvidence();
//        if (e.isExperimental()) {
//            foregroundColor = PAINT_COLOR_EXP;//Preferences.inst().getExpPaintColor();
//        }
//        backgroundColor = foregroundColor;
//        //setBackground(foregroundColor);
//        //ScaledIcon scaledIcon = null;
//        GOTermHelper gth = PaintManager.inst().goTermHelper();
//        HashSet<Qualifier> qualifierSet = annot.getQualifierSet();
//        qualifierSet = gth.getValidQualifiersForTerm(gth.getTerm(annot.getGoTerm()), qualifierSet);
//        if (null != qualifierSet) {
//            for (Iterator<Qualifier> iter = qualifierSet.iterator(); iter.hasNext();) {
//                Qualifier q = iter.next();
//                qualifier = q;
////                if (q.isColocalizesWith()) {
////                    scaledIcon = new ScaledIcon(null);
////                    scaledIcon.setIcon(Preferences.inst().getIconByName("colocate"));
////                    scaledIcon.setDimension(15);                    
////                }
////                if (q.isContributesTo()) {
////                    scaledIcon = new ScaledIcon(null);
////                    scaledIcon.setIcon(Preferences.inst().getIconByName("contribute"));
////                    scaledIcon.setDimension(15);                    
////                }
//                // NOT overrides all, hence, break
//                if (q.isNot()) {
//                    qualifier = q;
////                    scaledIcon = new ScaledIcon(null);
////                    scaledIcon.setIcon(Preferences.inst().getIconByName("not"));
////                    scaledIcon.setDimension(15);
//                    break;
//                }
//            }
//        }
//        else {
//            qualifier = null;
//        }
////        setIcon(scaledIcon);        
//        String name = nodeInfo.getTermName();
//        if (null == name) {
//            name = nodeInfo.getTermAcc();
//        }
//
//        if (false == node.isLeaf()) {
//            System.out.println("Here");
//        }
//        if (null !=  qualifier) {
//            //System.out.println(name + getQualifierString(qualifierSet) + " " + node.getNodeLabel() + "(" + node.getNode().getStaticInfo().getNodeAcc() + " " +  node.getNode().getStaticInfo().getPublicId() + ")");
//            label = name + getQualifierString(qualifierSet) + " " + node.getNodeLabel() + "(" + node.getNode().getStaticInfo().getNodeAcc() + " " +  node.getNode().getStaticInfo().getPublicId() + ")";
//        }
//        else {
//            label = null;
//        }
//        setToolTipText(name + getQualifierString(qualifierSet) + " " + node.getNodeLabel() + "(" + node.getNode().getStaticInfo().getNodeAcc() + " " +  node.getNode().getStaticInfo().getPublicId() + ")");
//
//        //foregroundColor = RenderUtil.selectedColor(selected, foregroundColor, backgroundColor);
//        //setBackground(foregroundColor);
//        if (annot_table.getSelectedColumn() == column) {
//            //this.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, Color.black));
//        } else {
//            //this.setBorder(null);
//        }
//
//        return this;
//
//    }
    

    
    private String getQualifierString(HashSet<Qualifier> qualifierSet) {
        if (null == qualifierSet || 0 == qualifierSet.size()) {
            return STR_EMPTY;
        }
        StringBuffer sb = new StringBuffer(STR_BRACKET_START);
        boolean added = false;
        for (Iterator<Qualifier> iter = qualifierSet.iterator(); iter.hasNext();) {
            if (true == added) {
                sb.append(STR_COMMA);
            }
                   Qualifier q = iter.next();
                   sb.append(q.getText().toUpperCase());
                   added = true;
        }
        sb.append(STR_BRACKET_END);
        return sb.toString();
    }
    
    private String getAllQualifierInfo(NodeInfoForMatrix nodeInfo, String delim) {
        if (null == nodeInfo) {
            return null;
        }
        HashMap<String, HashSet<String>> qualifierLookup = nodeInfo.getAllQualifierToListOfTerms();
        if (null == qualifierLookup) {
            return null;
        }
        Vector<String> allStrs = new Vector(qualifierLookup.size());
        for (Entry<String, HashSet<String>> entry: qualifierLookup.entrySet()) {
            String qualifier = entry.getKey();
            HashSet<String> terms = entry.getValue();
            allStrs.add(qualifier + STR_COLON + Utils.listToString(new Vector(terms), STR_EMPTY, STR_COMMA));
        }
        return Utils.listToString(allStrs, STR_EMPTY, delim);
    }
    
    public void paintComponent(Graphics g) {
	super.paintComponent(g);

//        if (true == "GO:0004842".equals(nodeInfo.getgTerm().getAcc()) && true == "PTN001295675".equals(node.getNode().getStaticInfo().getPublicId())) {
//            System.out.println("Here");
//            System.out.println("Background color is " + backgroundColor.getRed() + " " + backgroundColor.getGreen() + " " + backgroundColor.getBlue());
//        }         
        int width = this.getWidth();
        int height = this.getHeight();
        //RenderUtil.paintBorder(g, new Rectangle(0, 0, width, height), null, selected);
        if (true == selected) {
            g.setColor(backgroundColor.brighter());
        }
        else {
            g.setColor(backgroundColor);
        }
        g.fillRect(1, 1, width - 1, height - 1);
        boolean multipleQualifiers = nodeInfo.containsMultipleQualifiers();
        if (false == nodeInfo.isExpBackground() && false == nodeInfo.isNonExpBackground()) {
            return;
        }
        else if (true == nodeInfo.isExpBackground()) {
            if (true == multipleQualifiers && (false == nodeInfo.isExpNot() && false == nodeInfo.isNonExpNot())) {
                g.setColor(Color.yellow);
                g.fillOval(1 , 1, width - 3, height - 3);                
            }
            else if (true == multipleQualifiers && (true == nodeInfo.isExpNot() || true == nodeInfo.isNonExpNot())) {
                g.setColor(Color.pink);
                g.fillOval(1 , 1, width - 3, height - 3);                
            }            
            else if (false == multipleQualifiers && true == nodeInfo.isExpNot()) {
                g.setColor(Color.red);
                g.fillOval(1 , 1, width - 3, height - 3);
            }
            if (true == nodeInfo.isExpAnnotToTerm()) {
                g.setColor(Color.BLACK);
            }
            else {
                g.setColor(Color.WHITE);
            }
            g.fillRect((getWidth()/2) - 2, (getHeight()/2)-2, 4, 4);
            return;
        }
        else if (false == nodeInfo.isExpBackground() && true == nodeInfo.isNonExpBackground()) {
            if (true == multipleQualifiers && (false == nodeInfo.isExpNot() && false == nodeInfo.isNonExpNot())) {
                g.setColor(Color.yellow);
                g.fillOval(1 , 1, width - 3, height - 3);                
            }
            else if (true == multipleQualifiers && ((true == nodeInfo.isExpNot() || true == nodeInfo.isNonExpNot()))) {
                g.setColor(Color.pink);
                g.fillOval(1 , 1, width - 3, height - 3);                 
            }
            else if (false == multipleQualifiers && true == nodeInfo.isNonExpNot()) {
                g.setColor(Color.red);
                g.fillOval(1 , 1, width - 3, height - 3);
            }            
            if (true == nodeInfo.isNonExpAnnotToTerm()) {
                g.setColor(Color.BLACK);
            }
            else {
                g.setColor(Color.WHITE);
            }
            g.fillRect((getWidth()/2) - 2, (getHeight()/2)-2, 4, 4);            
        }                
    }
    public void paintComponentOrig(Graphics g) {
	super.paintComponent(g);

//        if (true == "GO:0004842".equals(nodeInfo.getgTerm().getAcc()) && true == "PTN001295675".equals(node.getNode().getStaticInfo().getPublicId())) {
//            System.out.println("Here");
//            System.out.println("Background color is " + backgroundColor.getRed() + " " + backgroundColor.getGreen() + " " + backgroundColor.getBlue());
//        }         
        int width = this.getWidth();
        int height = this.getHeight();
        //RenderUtil.paintBorder(g, new Rectangle(0, 0, width, height), null, selected);
        if (true == selected) {
            g.setColor(backgroundColor.brighter());
        }
        else {
            g.setColor(backgroundColor);
        }
        g.fillRect(1, 1, width - 1, height - 1);                
                
        if (false == nodeInfo.isExpBackground() && false == nodeInfo.isNonExpBackground()) {
            return;
        }
        else if (true == nodeInfo.isExpBackground()) {
            if (true == nodeInfo.isExpNot()) {
                g.setColor(Color.red);
                g.fillOval(1 , 1, width - 3, height - 3);
            }
            if (true == nodeInfo.isExpAnnotToTerm()) {
                g.setColor(Color.BLACK);
            }
            else {
                g.setColor(Color.WHITE);
            }
            g.fillRect((getWidth()/2) - 2, (getHeight()/2)-2, 4, 4);
            return;
        }
        else if (false == nodeInfo.isExpBackground() && true == nodeInfo.isNonExpBackground()) {
            if (true == nodeInfo.isNonExpNot()) {
                g.setColor(Color.red);
                g.fillOval(1 , 1, width - 3, height - 3);
            }            
            if (true == nodeInfo.isNonExpAnnotToTerm()) {
                g.setColor(Color.BLACK);
            }
            else {
                g.setColor(Color.WHITE);
            }
            g.fillRect((getWidth()/2) - 2, (getHeight()/2)-2, 4, 4);            
        }                
    }    
    
}
