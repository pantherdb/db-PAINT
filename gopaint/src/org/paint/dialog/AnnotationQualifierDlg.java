/**
 *  Copyright 2016 University Of Southern California
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
package org.paint.dialog;

import com.sri.panther.paintCommon.util.Utils;
import edu.usc.ksom.pm.panther.paint.annotation.AnnotQualifierGroup;
import edu.usc.ksom.pm.panther.paint.annotation.QualifierAnnotRltn;
import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import org.paint.util.RenderUtil;

/**
 *
 * @author muruganu
 */
public class AnnotationQualifierDlg extends JDialog implements ActionListener{
    public static final String STR_DASH = "-";
    public static final String STR_EMPTY = "";
    public static final String STR_COMMA = ",";
    public static final String STR_BTN_TEXT = " from ";
    
    private Map<JRadioButton, QualifierAnnotRltn> selections;
    private QualifierAnnotRltn selected;
    AnnotQualifierGroup aqg;
    private boolean userClickedOk = false;
    public AnnotationQualifierDlg(Frame frame, AnnotQualifierGroup aqg) {
        	super(frame, "Different qualifiers for annotation to term", true);
                this.aqg = aqg;
		setLayout(new BorderLayout());
		setContentPane(qualifyPane(aqg));
		pack();
		setLocationRelativeTo(frame);
    }
    
    private JPanel qualifyPane( AnnotQualifierGroup aqg) {
        JPanel qualify = new JPanel();
        		qualify.setLayout(new BoxLayout(qualify, BoxLayout.PAGE_AXIS));
		
		//Create the components.
		JPanel selectionPane = createSelectionPane(aqg);
		JPanel buttonPane = new JPanel();
		buttonPane.setOpaque(true);
		buttonPane.setBackground(RenderUtil.getAspectColor());
		JButton doneButton = null;	 	 
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
    
    private String getQualifierString(HashSet<Qualifier> qSet) {
        if (null == qSet) {
            return null;
        }
        if (qSet.isEmpty()) {
            return STR_DASH;
        }
        ArrayList<String> qList = new ArrayList<String>(qSet.size());
        for (Qualifier q: qSet) {
            String text = q.getText();
            if (null == text || 0 == text.length()) {
                continue;
            }
            qList.add(text);
        }
        if (true == qList.isEmpty()) {
            return STR_DASH;
        }
        Collections.sort(qList);
        return Utils.listToString(new Vector<String>(qList), STR_EMPTY, STR_COMMA);
    }
    
    private String getAnnotationInfo(HashSet<Annotation> annotSet) {
        HashSet<String> nodeIdSet = new HashSet<String>(annotSet.size());
        for (Annotation a: annotSet) {
           nodeIdSet.add(a.getAnnotationDetail().getAnnotatedNode().getStaticInfo().getPublicId());
        }
        Vector<String> nodeList = new Vector<String>(nodeIdSet);
        return Utils.listToString(nodeList, STR_EMPTY, STR_COMMA);
    }
    
    

    private JPanel createSelectionPane(AnnotQualifierGroup aqg) {
        String description = "Found annotations to term with conflicting qualifiers.\nSelect applicable qualifier set.\nSubmit ticket to challenge annotations with qualifiers not selected";
        JPanel box = new JPanel();
        JLabel label = new JLabel(description);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.setOpaque(true);
        box.setBackground(RenderUtil.getAspectColor());

        box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
        box.add(label);

        selections = new HashMap<JRadioButton, QualifierAnnotRltn>();
        HashMap<HashSet<Qualifier>, HashSet<Annotation>> qualifierAnnotLookup = aqg.getQualifierAnnotLookup();
        Set<HashSet<Qualifier>> qKeySet = qualifierAnnotLookup.keySet();
        boolean selected = false;
        for (HashSet<Qualifier> qSet : qKeySet) {
            String qualifierStr = getQualifierString(qSet);
            HashSet<Annotation> annotSet = qualifierAnnotLookup.get(qSet);
            String annotInfoStr = getAnnotationInfo(annotSet);
            JRadioButton rb = addRadioButton(qualifierStr + STR_BTN_TEXT + annotInfoStr);
            if (false == selected) {
                rb.setSelected(selected);
                selected = true;
            }
            selections.put(rb, new QualifierAnnotRltn(qSet, annotSet));
            box.add(rb);
        }

        JPanel pane = new JPanel(new BorderLayout());
        pane.add(box, BorderLayout.PAGE_START);
        Border padding = BorderFactory.createEmptyBorder(20, 20, 5, 20);
        pane.setBorder(padding);
        pane.setOpaque(true);
        pane.setBackground(RenderUtil.getAspectColor());
        return pane;

    }
    
    private JRadioButton addRadioButton(String text) {
        JRadioButton button = new JRadioButton();
        button.setText(text);
        button.setSelected(false);
        return button;
    }
    
    public boolean didUserSubmitForm() {
        return userClickedOk;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        userClickedOk = true;
        Set<JRadioButton> buttons = selections.keySet();

        for (JRadioButton btn : buttons) {
            if (btn.isSelected()) {
                selected = selections.get(btn);
                break;
            }
        }
        this.setVisible(false);

    }
    
    public QualifierAnnotRltn getAnnotationSet() {
        setVisible(true);
        return selected;
    }
}
