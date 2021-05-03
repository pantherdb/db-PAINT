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
package org.paint.dialog;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import com.sri.panther.paintCommon.Constant;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationHelper;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import edu.usc.ksom.pm.panther.paintCommon.QualifierDif;;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import org.paint.datamodel.GeneNode;
import org.paint.go.GOConstants;
import org.paint.main.PaintManager;
import org.paint.util.GeneNodeUtil;
import org.paint.util.RenderUtil;

/**
 *
 * @author muruganu
 */
public class EvdnceCdeAndNewAnnotDlg extends JDialog implements ActionListener {

    private static final HashMap<String, String> evidenceCdeToString = initEvidenceCdeToDisplayString();
    private Map<JRadioButton, String> selections;
    private Map<JCheckBox, String> leavesLookup;
    private String selected = null;

    JTextField pmidTextField = null;
    JComboBox<GeneNode> leavesComboBox = null;
    JList<String> leafLabels = null;
    JPanel residuesPane = null;

    private Map<JRadioButton, GOTerm> ancestorLookup;
    private GOTerm ancestor;
//    private Map<JCheckBox, Qualifier> qualifierMap = null;
//    private HashSet<Qualifier> qualifierSet;

    private JRadioButton residuesBtn = null;
    private JRadioButton divergentBtn = null;
    private JButton doneButton = null;

    private List<GeneNode> leaves;
    private Annotation annotation;
    private List<GeneNode> defaultEvdnceLeaves;
    private boolean ancestorsApplicable = false;

    public static HashMap<String, String> initEvidenceCdeToDisplayString() {
        HashMap<String, String> rtnTbl = new HashMap<String, String>();
        rtnTbl.put(GOConstants.KEY_RESIDUES_EC, GOConstants.KEY_RESIDUES);
        rtnTbl.put(GOConstants.DIVERGENT_EC, GOConstants.DIVERGENT);
        return rtnTbl;
    }

    public EvdnceCdeAndNewAnnotDlg(Frame frame, Annotation annotation, List<GeneNode> leaves, List<GeneNode> defaultEvdnceLeaves) {
        super(frame, "Evidence Code for NOT and New Annotation", true);
        this.leaves = leaves;
        this.annotation = annotation;
        this.defaultEvdnceLeaves = defaultEvdnceLeaves;
        setLayout(new BorderLayout());
        setContentPane(evidenceAndNewAnnot(annotation));
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    private JPanel evidenceAndNewAnnot(Annotation annotation) {
        JPanel qualify = new JPanel();
        qualify.setLayout(new BoxLayout(qualify, BoxLayout.PAGE_AXIS));

        //Create the components.
        JPanel selectionPane = createSelectionPane(annotation);
        JPanel buttonPane = new JPanel();
        buttonPane.setOpaque(true);
        buttonPane.setBackground(RenderUtil.getAspectColor());
        //JButton doneButton = null;	 	 
        doneButton = new JButton("Continue");
        doneButton.addActionListener(this);
        getRootPane().setDefaultButton(doneButton);
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(doneButton);

        qualify.add(selectionPane);
        qualify.add(buttonPane);
        return qualify;
    }

    private JPanel createResiduesPane() {
        JLabel residueLabel = new JLabel("Please enter PMID and also select sequence(s) from descendents providing evidence");
        JPanel box = new JPanel();
        residueLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.setOpaque(true);
        box.setBackground(RenderUtil.getAspectColor());
        box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
        box.add(residueLabel);

        JPanel pmidPane = new JPanel();
        pmidPane.setAlignmentX(LEFT_ALIGNMENT);
        pmidPane.setBackground(RenderUtil.getAspectColor());
        pmidPane.setLayout(new BoxLayout(pmidPane, BoxLayout.X_AXIS));
        JLabel pmidLabel = new JLabel("PMID:  ");
        pmidLabel.setAlignmentX(LEFT_ALIGNMENT);
        pmidPane.add(pmidLabel);
        pmidTextField = new JTextField(10);
        pmidTextField.setMaximumSize(pmidTextField.getPreferredSize());
        pmidPane.add(pmidTextField);
        box.add(pmidPane);

        //leavesComboBox = getLeavesComboBox();
        JScrollPane leavesScrollPane = getLeavesListBox();
        //leavesComboBox.setSelectedIndex(0);
        leavesScrollPane.setAlignmentX(LEFT_ALIGNMENT);

        JLabel defaultLeavesLabel = null;
        if (defaultEvdnceLeaves.isEmpty()) {
            defaultLeavesLabel = new JLabel("No leaves selected by default");
        } else {
            StringBuffer leavesLabel = new StringBuffer();
            int counter = 0;
            int size = defaultEvdnceLeaves.size();
            for (GeneNode geneNode : defaultEvdnceLeaves) {
                leavesLabel.append(geneNode.getNodeLabel());
                counter++;
                if (counter < size) {
                    leavesLabel.append(Constant.STR_COMMA);
                    leavesLabel.append(Constant.STR_SPACE);
                }
            }
            if (size > 1) {
                defaultLeavesLabel = new JLabel("The following leaves will be included as evidence: " + leavesLabel.toString());
            } else {
                defaultLeavesLabel = new JLabel(leavesLabel.toString() + " will be included as evidence");
            }

        }
        defaultLeavesLabel.setAlignmentX(LEFT_ALIGNMENT);
        defaultLeavesLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel listBoxLabel = new JLabel("Select additional leaf node(s) if necessary...  ");
        listBoxLabel.setAlignmentX(LEFT_ALIGNMENT);
        listBoxLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        listBoxLabel.setBackground(RenderUtil.getAspectColor());
        JPanel leavesPanel = new JPanel();
        leavesPanel.setAlignmentX(LEFT_ALIGNMENT);
        leavesPanel.setBackground(RenderUtil.getAspectColor());
        leavesPanel.setLayout(new BoxLayout(leavesPanel, BoxLayout.Y_AXIS));
        leavesPanel.add(defaultLeavesLabel);
        leavesPanel.add(listBoxLabel);
        leavesPanel.add(leavesScrollPane);
        box.add(leavesPanel);

        box.setBorder(BorderFactory.createLineBorder(Color.black));

        return box;
    }

    private JScrollPane getLeavesListBox() {
        List<String> labels = new ArrayList<>(leaves.size());
        for (GeneNode geneNode : leaves) {
            labels.add(geneNode.getNodeLabel());
        }
        leafLabels = new JList<String>(labels.toArray(new String[labels.size()]));
        leafLabels.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JScrollPane listScroller = new JScrollPane();
        listScroller.setViewportView(leafLabels);
        leafLabels.setLayoutOrientation(JList.VERTICAL);
        //leafLabels.setFixedCellWidth(100);
//        Dimension d = leafLabels.getPreferredSize();
//        d.width = 50;
//        listScroller.setPreferredSize(d);        
        return listScroller;
    }

    private JComboBox<GeneNode> getLeavesComboBox() {
        final List<GeneNode> nodes = leaves;

        final JComboBox<GeneNode> comboBox = new JComboBox<>(new Vector<>(nodes));

        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(final JList<?> list,
                    final Object value,
                    final int index,
                    final boolean isSelected,
                    final boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof GeneNode) {
                    setText(((GeneNode) value).getNodeLabel());
                    Dimension d = this.getPreferredSize();
                    d.height = 40;
                    this.setMaximumSize(d);
                }

                return this;
            }
        });
        comboBox.setSelectedItem(null);
        return comboBox;
    }

    private JPanel createLeavesPane(boolean currentNegative) {
        String description = "Select leaf nodes";
        JPanel box = new JPanel();
        JLabel label = new JLabel(description);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label.setAlignmentX(LEFT_ALIGNMENT);
        box.setOpaque(true);
        box.setBackground(RenderUtil.getAspectColor());

        box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
        box.add(label);

        leavesLookup = new HashMap<JCheckBox, String>();

        return box;
    }

    private JPanel createSelectionPane(Annotation annotation) {
        String description = null;
        boolean isNegative = true;
        Set<Qualifier> qSet = annotation.getQualifierSet();
        if (true == QualifierDif.containsNegative(qSet)) {
            description = "Select evidence code for positive annotation";
        } else {
            description = "Select evidence code for NOT annotation";
            isNegative = false;
        }
        JPanel box = new JPanel();
        JLabel label = new JLabel(description);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label.setAlignmentX(LEFT_ALIGNMENT);
        box.setOpaque(true);
        box.setBackground(RenderUtil.getAspectColor());

        box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
        box.add(label);

        selections = new HashMap<JRadioButton, String>();
        divergentBtn = addRadioButton(GOConstants.DIVERGENT_EC);
        divergentBtn.setAlignmentX(LEFT_ALIGNMENT);
        divergentBtn.addActionListener(this);
        selections.put(divergentBtn, GOConstants.DIVERGENT_EC);
        ButtonGroup bG = new ButtonGroup();
        bG.add(divergentBtn);
        box.add(divergentBtn);

//        if (leaves != null && leaves.size() > 0) {
        residuesBtn = addRadioButton(GOConstants.KEY_RESIDUES_EC);
        residuesBtn.addActionListener(this);
        selections.put(residuesBtn, GOConstants.KEY_RESIDUES_EC);
        bG.add(residuesBtn);
        box.add(residuesBtn);
        residuesBtn.setAlignmentX(LEFT_ALIGNMENT);
        residuesBtn.setSelected(true);
        residuesPane = createResiduesPane();
        residuesPane.setAlignmentX(LEFT_ALIGNMENT);
        box.add(residuesPane);
//        }
//        else {
//            divergentBtn.setSelected(true);
//        }

        if (false == isNegative) {
            ancestorsApplicable = true;
            String term = annotation.getGoTerm();
            GOTermHelper gth = PaintManager.inst().goTermHelper();
            GOTerm gterm = gth.getTerm(term);
            String aspect = gterm.getAspect();
            ArrayList<GOTerm> ancestors = gth.getAncestors(gterm);
            GOTermHelper goTermHelper = PaintManager.inst().goTermHelper();
            for (Iterator<GOTerm> termIter = ancestors.iterator(); termIter.hasNext();) {
                GOTerm aTerm = termIter.next();
                List<GOTerm> curParents = aTerm.getParents();
                if (null == curParents || 0 == curParents.size()) {
                    // No top level terms
                    termIter.remove();
                }
                if (false == goTermHelper.isAnnotAllowedForTerm(term)) {
                    // No unallowed terms
                    termIter.remove();
                    continue;
                }
                if (null != AnnotationHelper.existingAnnotsAllowNewAnnotation(annotation.getQualifierSet(), aTerm.getId(), annotation.getAnnotationDetail().getAnnotatedNode(), goTermHelper)) {
                    System.out.println(aTerm.getId() + " cannot be used as ancestor term");
                    termIter.remove();
                }
            }

            if (ancestors.size() > 0) {
                JLabel ancestorLabel = new JLabel("Annotate with an ancestor term?");
                ancestorLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                ancestorLabel.setAlignmentX(LEFT_ALIGNMENT);
                box.add(ancestorLabel);
                ancestorLookup = new HashMap<JRadioButton, GOTerm>();
                ButtonGroup ancestorBtnGrp = new ButtonGroup();
                for (GOTerm curTerm : ancestors) {
                    JRadioButton btn = addRadioButton(curTerm);
                    btn.setAlignmentX(LEFT_ALIGNMENT);
                    ancestorBtnGrp.add(btn);
                    box.add(btn);
                    ancestorLookup.put(btn, curTerm);
                }
            }
        }

//        // Figure out qualifiers - If there is a NOT, we want to remove it.  If there isn't a NOT, we want to add it.
//        HashSet<Qualifier> applicableQualifiers = gth.getValidQualifiersForTerm(gterm, annotation.getQualifierSet());
//        Qualifier notQualifier = null;
//        if (null != applicableQualifiers) {
//            for (Qualifier q: applicableQualifiers) {
//                if (q.isNot()) {
//                    notQualifier = q;
//                    break;
//                }
//            }
//        }
//        if (notQualifier != null) {
//            applicableQualifiers.remove(notQualifier);
//        }
//        else {
//            notQualifier = new Qualifier();
//            notQualifier.setText(Qualifier.QUALIFIER_NOT);
//            if (null == applicableQualifiers) {
//                applicableQualifiers = new HashSet<Qualifier>();
//                applicableQualifiers.add(notQualifier);
//            }
//        }
//        
//        if (null != applicableQualifiers) {
//            
//            JLabel qualifierLabel = new JLabel("Qualifier");
//            qualifierLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//            box.add(qualifierLabel);
//            qualifierMap = new HashMap<JCheckBox, Qualifier>();
//            for (Qualifier q: applicableQualifiers) {
//                JCheckBox check = addCheckBox(q);
//                check.setSelected(true);                
//                if (q.isNot()) {
//                   check.setEnabled(false);
//                }
//                box.add(check);
//                qualifierMap.put(check, q);
//
//            }
//        }
        JPanel pane = new JPanel(new BorderLayout());
        pane.add(box, BorderLayout.PAGE_START);
        Border padding = BorderFactory.createEmptyBorder(20, 20, 5, 20);
        pane.setBorder(padding);
        pane.setOpaque(true);
        pane.setBackground(RenderUtil.getAspectColor());
        return pane;

    }

    private JCheckBox getCheckBox(GeneNode gNode, boolean curNegative, GOTerm term) {
        JCheckBox check = new JCheckBox();
        check.setText(gNode.getNodeLabel());
        AnnotationStatus as = new AnnotationStatus(gNode);
        if (AnnotationStatus.ANNOT_NONE == as.status) {
            return check;
        }
        if ((true == curNegative && true == as.containsNegative) || (false == curNegative && true == as.containsPositive)) {
            return check;
        }
        if (true == curNegative && true == as.containsPositive && false == as.containsNegative) {
            check.setSelected(true);
            check.setEnabled(false);
            return check;
        }
        if (false == curNegative && true == as.containsNegative && false == as.containsPositive) {
            check.setSelected(true);
            check.setEnabled(false);
            return check;
        }
        return check;
    }

    private JRadioButton addRadioButton(String evidenceCode) {
        JRadioButton radio = new JRadioButton();
        radio.setText(evidenceCdeToString.get(evidenceCode));
        radio.setSelected(false);
        return radio;
    }

    private JRadioButton addRadioButton(GOTerm goTerm) {
        JRadioButton radio = new JRadioButton();
        radio.setText(goTerm.getName() + " (" + goTerm.getAcc() + ")");
        radio.setSelected(false);
        return radio;
    }

//    private JCheckBox addCheckBox(Qualifier qualifier) {
//        JCheckBox check = new JCheckBox();
//        check.setText(qualifier.getText());
//        check.setSelected(false);
//        return check;
//    }
    @Override
    public void actionPerformed(ActionEvent e) {
//        if (residuesBtn == e.getSource()) {
//            residuesPane.setVisible(true);
//        }
//        if (divergentBtn == e.getSource()) {
//            residuesPane.setVisible(false);            
//        }
        if (doneButton == e.getSource()) {
            // If user has selected IKR, ensure a valid PMID has been entered
//            String pmid = pmidTextField.getText();
//            if (residuesBtn.isSelected() && (null == pmid || 0 == pmid.trim().length())) {
//                JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "PMID cannot be empty", "Please enter a valid PMID", JOptionPane.ERROR_MESSAGE);
//                return;
//            }

            Set<JRadioButton> buttons = selections.keySet();

            for (JRadioButton radio : buttons) {
                if (radio.isSelected()) {
                    selected = selections.get(radio);
                    break;
                }
            }
            if (null != ancestorLookup) {
                Set<JRadioButton> ancestorButtons = ancestorLookup.keySet();
                for (JRadioButton radio : ancestorButtons) {
                    if (radio.isSelected()) {
                        ancestor = ancestorLookup.get(radio);
                        break;
                    }
                }
            }

//                if (null != qualifierMap) {
//                    qualifierSet = new HashSet<Qualifier>();
//                    Set<JCheckBox> qualifierChecks = qualifierMap.keySet();
//                    for (JCheckBox check: qualifierChecks) {
//                        if (check.isSelected()) {
//                            qualifierSet.add(qualifierMap.get(check));
//                        }
//                    }
//                }
            this.setVisible(false);
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    public String getSelectedEvidenceCode() {
        return selected;
    }

    public boolean areAncestorsApplicable() {
        return ancestorsApplicable;
    }

    public GOTerm getAncestor() {
        if (true == ancestorsApplicable) {
            return ancestor;
        }
        return null;
    }

    public String getPMID() {
        if (null != pmidTextField && null != pmidTextField.getText()) {
            String rtn = pmidTextField.getText().trim();
            if (true == rtn.isEmpty()) {
                return null;
            }
            return rtn;
        }
        return null;
    }

    public int[] getSelectedLeafIndices() {
        return leafLabels.getSelectedIndices();
    }

//    public HashSet<Qualifier> getQualifierSet() {
//        return qualifierSet;
//    }
    private class AnnotationStatus {

        public static final int ANNOT_NONE = 0;
        public static final int ANNOT_POS = 1;
        public static final int ANNOT_NEG = 2;
        public int status = ANNOT_NONE;
        public boolean containsPositive = false;
        public boolean containsNegative = false;

        private AnnotationStatus(GeneNode geneNode) {

            if (true == GeneNodeUtil.inPrunedBranch(geneNode)) {
                status = ANNOT_NONE;
            }
            Node node = geneNode.getNode();
            NodeVariableInfo nvi = node.getVariableInfo();
            if (null == nvi) {
                status = ANNOT_NONE;
            }
            ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                status = ANNOT_NONE;
            }
            for (Annotation a : annotList) {
                if (true == QualifierDif.containsNegative(a.getQualifierSet())) {
                    containsNegative = true;
                }
                if (null == a.getQualifierSet() || true == QualifierDif.containsPositive(a.getQualifierSet())) {
                    containsPositive = true;
                }
            }
        }

    }

}
