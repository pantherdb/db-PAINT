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
package org.paint.dialog;

import com.sri.panther.paintCommon.Constant;
import edu.stanford.ejalbert.BrowserLauncher;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import org.bbop.framework.GUIManager;
import org.paint.datamodel.GeneNode;
import org.paint.gui.association.AssociationTable;
import org.paint.gui.association.ExpAssociationTable;
import org.paint.gui.event.EventManager;
import org.paint.gui.evidence.PaintAction;
import org.paint.main.PaintManager;
import org.paint.util.HTMLUtil;


public class AddExpEvidenceDlg extends JDialog implements ActionListener {

    private static String LABEL_TITLE = "Update Experimental Evidence to Sequence ";
    protected String expEvidence [];
    
    public static final String MSG_ANNOT_TO_TERM_ALREADY_EXISTS_PART_1 = "Annotation to term ";
    public static final String MSG_ANNOT_TO_TERM_ALREADY_EXISTS_PART_2 = " already exists.";
    public static final String MSG_ANNOT_TO_MORE_SPECIFIC_TERM_ALREADY_EXISTS_PART_1 = "Annotation to more specific term ";
    public static final String MSG_ANNOT_TO_MORE_SPECIFIC_TERM_ALREADY_EXISTS_PART_2 = " already exists.";
    
    public static final String MSG_FOUND_EXISTING_EXP_ANNOTATIONS = "Found, existing experimental annotations, continue?";
    
   
        
    protected ArrayList<String> terms;
//    protected JPanel expEvidnecePanel;
//    protected ArrayList<JRadioButton> qualifierButtonList = null;
    protected ExpEvidencePanel evidencePanel = null;
    protected boolean operationSuccess = false;
    private GeneNode geneNode;
    

    
    public AddExpEvidenceDlg(GeneNode geneNode) {
        super(GUIManager.getManager().getFrame(), false);       // Non-modal, since things have to be updated in model
        setTitle(LABEL_TITLE + geneNode.getNode().getStaticInfo().getPublicId());
        this.geneNode = geneNode;
//        HashMap<String, GOTerm> termLookup = PaintManager.inst().goTermHelper().getDisplayLookup();
//        terms = new ArrayList<String>(termLookup.keySet());
//        Collections.sort(terms);
        

        
        
        
        expEvidence = new String[Evidence.getExperimental().size()];
        Evidence.getExperimental().toArray(expEvidence);
        terms = PaintManager.inst().goTermHelper().getDisplayList();
        evidencePanel = new ExpEvidencePanel(terms, expEvidence);
        setContentPane(evidencePanel);
        pack();
        setLocationRelativeTo(GUIManager.getManager().getFrame());
        this.setVisible(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Action to be performed when the dialog is closing
                if (null != evidencePanel) {
                    evidencePanel.windowClosing();
                }
            }
        });
        
    }


//    public void actionPerformed(ActionEvent e) {
//        if (evidencePanel.jButton2 == e.getSource()) {
//            this.setVisible(false);
//            return;
//        }
//        if (okBtn != e.getSource()) {
//            return;
//        }
//        String selectedTerm = evidencePanel.getSelctedTerm();
//        if (null == selectedTerm) {
//            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Please select a valid term", "Invalid GO term", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//        GOTerm t = PaintManager.inst().goTermHelper().getTerm(selectedTerm);
//        if (null == t) {
//            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to find term " + selectedTerm + " in system", "Invalid GO term", JOptionPane.ERROR_MESSAGE);
//            return;
//        }
//        HashSet<String> qualifierSet = evidencePanel.getSelectedQualifierSet();
//        
//        for (String qualifier: qualifierSet) {
//            if (false == PaintManager.inst().goTermHelper().canTermHaveQualifier(t, qualifier)) {
//                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Term not valid with qualifier " + qualifier, "Invalid Qualifier", JOptionPane.ERROR_MESSAGE);
//                return;
//            }
//        }
//        
//        String pmid = evidencePanel.getPMID();
//        if (null == pmid) {
//            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Please enter a PubMed ID", "PMID not specified", JOptionPane.ERROR_MESSAGE);
//            return;            
//        }
//        try {
//            Long.parseLong(pmid);
//        }
//        catch(NumberFormatException ex) {
//            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "PubMed ID should only have digits", "Invalid PMID", JOptionPane.ERROR_MESSAGE);
//            return;             
//        }
//        operationSuccess = true;
//        setVisible(false);
//        return;
//    }
    
    public boolean isOperationSuccess() {
        return operationSuccess;
    }
    
    public String getSelectedTerm() {
        return evidencePanel.getSelctedTerm();
    }
    
    public HashSet<String> getQualifierSet() {
        return evidencePanel.getSelectedQualifierSet();
    }
    
    public String getEvidenceCode() {
        return evidencePanel.getSelectedEvidence();
    }
    
    public String getPMID() {
        return evidencePanel.getPMID();
    }
    
    public int display() {
        return 0;
    }
    
    public void actionPerformed(ActionEvent e) {
        this.setVisible(false);
        this.dispose();
        EventManager.inst().removeGeneAnnotationChangeListener(this.evidencePanel.jTable1);
    }   

//    protected JPanel expEvidencePanel() {
//        JPanel labelPanel = new JPanel(new GridLayout(0,1));
//        JPanel componentsPanel = new JPanel(new GridLayout(0,1));
//        
//        expEvidnecePanel = new JPanel();
//        expEvidnecePanel.setLayout(new BoxLayout(expEvidnecePanel, BoxLayout.Y_AXIS));
//        goId = new JTextField(10);
//        expEvidnecePanel.add(goId);
//        
//        JList<String> evidenceLabels = new JList<String>(EXP_EVIDENCE.toArray(new String[EXP_EVIDENCE.size()]));
//        evidenceLabels.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        
//        JScrollPane listScroller = new JScrollPane();
//        listScroller.setViewportView(evidenceLabels);
//        evidenceLabels.setLayoutOrientation(JList.VERTICAL);
//        
//        listScroller.setAlignmentX(LEFT_ALIGNMENT);
//        expEvidnecePanel.add(listScroller);
//        
//        qualifierGroup = new ButtonGroup();
//        JPanel qualifierPanel = new JPanel();
//        qualifierPanel.setLayout(new BoxLayout(qualifierPanel, BoxLayout.X_AXIS));
//        qualifierButtonList = new ArrayList<JRadioButton>();
//        for (String qualifier: QUALIFIER_LIST) {
//            JRadioButton btn = new JRadioButton(qualifier);
//            qualifierButtonList.add(btn);
//            qualifierGroup.add(btn);
//            qualifierPanel.add(btn);
//        }
//        qualifierButtonList.get(0).setSelected(true);
//        expEvidnecePanel.add(qualifierPanel);
//        
//        
//        return expEvidnecePanel;
//    }
    
    private class ExpEvidencePanel extends javax.swing.JPanel{


        private ArrayList<String> termList;
        private String[] evidenceList;


        /**
         * Creates new form ExpEvidencePanel
         */
        public ExpEvidencePanel(ArrayList<String> termList, String[] evidenceList) {
            this.termList = termList;
            this.evidenceList = evidenceList;

            initComponents();
        }
        
        public void windowClosing() {
            if (null != jTable1) {
                jTable1.windowClosing();
            }
        }

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            
            String[] arrayList = new String[ExpEvidencePanel.this.termList.size()];
            this.termList.toArray(arrayList);
            jComboBox1 = new Java2sAutoComboBox(this.termList);
            jComboBox1.setSelectedIndex(-1);

            jLabel2 = new javax.swing.JLabel();
            jComboBox2 = new javax.swing.JComboBox(this.evidenceList);
            jLabel3 = new javax.swing.JLabel();
            jPanel1 = new javax.swing.JPanel();
            jCheckBox3 = new javax.swing.JCheckBox();
            jCheckBox4 = new javax.swing.JCheckBox();
            jCheckBox2 = new javax.swing.JCheckBox();
            jLabel4 = new javax.swing.JLabel();
            jTextField1 = new javax.swing.JTextField();
//            jPanel2 = new javax.swing.JPanel();
//            jButton1 = new javax.swing.JButton();
//            jButton2 = new javax.swing.JButton();
            jScrollPane1 = new javax.swing.JScrollPane();
            jTable1 = new ExpAssociationTable(geneNode);
            jButton3 = new javax.swing.JButton();
            jButton1 = new javax.swing.JButton();

            jLabel1.setText("GO Id:");

            jLabel2.setText("Evidence Code");

        jLabel3.setText("Qualifier");

        jCheckBox3.setText("COLOCALIZES_WITH");
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        jCheckBox4.setText("CONTRIBUTES_TO");
        jCheckBox4.setToolTipText("");
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });

        jCheckBox2.setText("NOT");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jCheckBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel4.setText("PMID");

        jScrollPane1.setViewportView(jTable1);
        
        jButton3.setText("Add Experimental Annotation");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });        

       jButton1.setText("Finished Updating Experimental Annotations");
       jButton1.setToolTipText("");
       jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });       
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton3)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(462, 462, 462))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGap(445, 445, 445)
                .addComponent(jButton1)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(jLabel3))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton3)
                .addGap(23, 23, 23)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(19, 19, 19)
                .addComponent(jButton1)
                .addContainerGap())
        );
    }// </editor-fold>                     

    private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO add your handling code here:
    }                                          

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO add your handling code here:
    }                                          

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO add your handling code here:
    }
    
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        String selectedTerm = evidencePanel.getSelctedTerm();
        if (null == selectedTerm) {
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Please select a valid term", "Invalid GO term", JOptionPane.ERROR_MESSAGE);
            return;
        }
        GOTerm t = PaintManager.inst().goTermHelper().getTerm(selectedTerm);
        if (null == t) {
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to find term " + selectedTerm + " in system", "Invalid GO term", JOptionPane.ERROR_MESSAGE);
            return;
        }
        HashSet<String> qualifierSet = evidencePanel.getSelectedQualifierSet();
        
        for (String qualifier: qualifierSet) {
            if (false == PaintManager.inst().goTermHelper().canTermHaveQualifier(t, qualifier)) {
                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Term not valid with qualifier " + qualifier, "Invalid Qualifier", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        String pmid = evidencePanel.getPMID();
        if (null == pmid) {
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Please enter a PubMed ID", "PMID not specified", JOptionPane.ERROR_MESSAGE);
            return;            
        }
        try {
            Long.parseLong(pmid);
            BrowserLauncher bl = PaintManager.inst().getBrowserLauncher();
            HTMLUtil.bringUpInBrowser(bl, AssociationTable.URL_LINK_PREFIX_PMID + pmid);
        }
        catch(NumberFormatException ex) {
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "PubMed ID should only have digits", "Invalid PMID", JOptionPane.ERROR_MESSAGE);
            return;             
        }

        HashSet<Qualifier> qset = new HashSet<Qualifier>();
        for (String q : getQualifierSet()) {
            Qualifier qualifier = new Qualifier();
            qualifier.setText(q);
            qset.add(qualifier);
        }
        
        boolean isNot = QualifierDif.containsNegative(qset);
        GOTermHelper gth = PaintManager.inst().goTermHelper();
        GOTerm selectedGoTerm = gth.getTerm(selectedTerm);
        
        NodeVariableInfo nvi = geneNode.getNode().getVariableInfo();
        if (nvi == null) {
            nvi = new NodeVariableInfo();
            geneNode.getNode().setVariableInfo(nvi);
        }
        ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
        if (null == annotList) {
            annotList = new ArrayList<Annotation>();
        }
        StringBuffer sb = new StringBuffer();
        for (Annotation a: annotList) {
            if (true == a.isExperimental()) {
                if (true == isNot && (true == QualifierDif.containsNegative(a.getQualifierSet())) && QualifierDif.allQualifiersSame(qset, a.getQualifierSet())) {
                    if (selectedTerm.equals(a.getGoTerm())) {
                        if (0 != sb.length()) {
                            sb.append(Constant.STR_NEWLINE);
                        }
                        sb.append(MSG_ANNOT_TO_TERM_ALREADY_EXISTS_PART_1);
                        sb.append(selectedTerm);
                        sb.append(MSG_ANNOT_TO_TERM_ALREADY_EXISTS_PART_2);
                    }
                    HashSet<GOTerm> descTerms = gth.getDescendants(gth.getTerm(a.getGoTerm()));
                    if (descTerms.contains(selectedGoTerm)) {
                        if (0 != sb.length()) {
                            sb.append(Constant.STR_NEWLINE);
                        }
                        sb.append(MSG_ANNOT_TO_MORE_SPECIFIC_TERM_ALREADY_EXISTS_PART_1);
                        sb.append(a.getGoTerm());
                        sb.append(MSG_ANNOT_TO_MORE_SPECIFIC_TERM_ALREADY_EXISTS_PART_2);                        
                    }
                }
                if (false == isNot && (false == QualifierDif.containsNegative(a.getQualifierSet())) && QualifierDif.allQualifiersSame(qset, a.getQualifierSet())) {
                    if (selectedTerm.equals(a.getGoTerm())) {
                        if (0 != sb.length()) {
                            sb.append(Constant.STR_NEWLINE);
                        }
                        sb.append(MSG_ANNOT_TO_TERM_ALREADY_EXISTS_PART_1);
                        sb.append(selectedTerm);
                        sb.append(MSG_ANNOT_TO_TERM_ALREADY_EXISTS_PART_2);
                    }
                    ArrayList<GOTerm> ancestorTerms = gth.getAncestors(gth.getTerm(a.getGoTerm()));
                    if (ancestorTerms.contains(selectedGoTerm)) {
                        if (0 != sb.length()) {
                            sb.append(Constant.STR_NEWLINE);
                        }
                        sb.append(MSG_ANNOT_TO_MORE_SPECIFIC_TERM_ALREADY_EXISTS_PART_1);
                        sb.append(a.getGoTerm());
                        sb.append(MSG_ANNOT_TO_MORE_SPECIFIC_TERM_ALREADY_EXISTS_PART_2);                        
                    }
                }
            }
        }
        
        if (0 != sb.length()) {
            int dialogResult = JOptionPane.showConfirmDialog(GUIManager.getManager().getFrame(), sb.toString(), MSG_FOUND_EXISTING_EXP_ANNOTATIONS, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (dialogResult != JOptionPane.YES_OPTION) {
                return;
            }
        }
        PaintAction.inst().addExperimentalAnnotation(selectedTerm, geneNode, evidencePanel.getSelectedEvidence(), getPMID(), qset);
    }
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        AddExpEvidenceDlg.this.actionPerformed(evt);
    }     

//    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                         
//        // TODO add your handling code here:
//    }                                        

//    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
//        // TODO add your handling code here:
//    }  
    
    
        // Variables declaration - do not modify
    private javax.swing.JButton jButton1;    
    private javax.swing.JButton jButton3;    
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private Java2sAutoComboBox jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private ExpAssociationTable jTable1;    
    private javax.swing.JTextField jTextField1;
        // End of variables declaration
        
        public static final String QUALIFIER_NONE = "None";

        public String getPMID() {
            if (null != jTextField1 && null != jTextField1.getText()) {
                String rtn = jTextField1.getText().trim();
                if (true == rtn.isEmpty()) {
                    return null;
                }
                return rtn;
            }
            return null;
        }

        public HashSet<String> getSelectedQualifierSet() {
            HashSet<String> qualifierSet = new HashSet<String>();
            if (jCheckBox2.isSelected()) {
                qualifierSet.add(jCheckBox2.getText());
            }
            if (jCheckBox3.isSelected()) {
                qualifierSet.add(jCheckBox3.getText());
            }
            if (jCheckBox4.isSelected()) {
                qualifierSet.add(jCheckBox4.getText());
            }
            return qualifierSet;
        }

        public String getSelectedEvidence() {
            return jComboBox2.getItemAt(jComboBox2.getSelectedIndex());
        }

        public String getSelctedTerm() {
            String itemText = (String) jComboBox1.getItemAt(jComboBox1.getSelectedIndex());
            if (null == itemText) {
                return null;
            }
            int index = itemText.indexOf(GOTermHelper.DELIM_TERM);
            if (index < 0) {
                return null;
            }
            String acc; 
            if (itemText.startsWith(GOTermHelper.PREFIX_ACC)) {
                acc = itemText.substring(0, index);
            }
            else {
                acc = itemText.substring(index + 1);
            }
            if (null == PaintManager.inst().goTermHelper().getTerm(acc)) {
                return null;
            }
            return acc;
        }
    }

    
}
