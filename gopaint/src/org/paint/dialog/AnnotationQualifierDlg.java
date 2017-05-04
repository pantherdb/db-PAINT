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

import edu.usc.ksom.pm.panther.paintCommon.Qualifier;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import org.paint.util.RenderUtil;

/**
 *
 * @author muruganu
 */
public class AnnotationQualifierDlg extends JDialog implements ActionListener{
    
    private Map<JCheckBox, Qualifier> selections;
    private HashSet<Qualifier> selected = new HashSet<Qualifier>();
    private boolean userClickedOk = false;
    public AnnotationQualifierDlg(Frame frame, HashSet<Qualifier> qualifierSet) {
        		super(frame, "Qualifier", true);
		setLayout(new BorderLayout());
		setContentPane(qualifyPane(qualifierSet));
		pack();
		setLocationRelativeTo(frame);
    }
    
    private JPanel qualifyPane(HashSet<Qualifier> qualifierSet) {
        JPanel qualify = new JPanel();
        		qualify.setLayout(new BoxLayout(qualify, BoxLayout.PAGE_AXIS));
		
		//Create the components.
		JPanel selectionPane = createSelectionPane(qualifierSet);
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
    
    private JPanel createSelectionPane(HashSet<Qualifier> qualifierSet) {
        String description = "Check qualifiers you wish propagated.";
        		JPanel box = new JPanel();
		JLabel label = new JLabel(description);
		label.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		box.setOpaque(true);
		box.setBackground(RenderUtil.getAspectColor());

		box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
		box.add(label);

		selections = new HashMap<JCheckBox, Qualifier>();
		JCheckBox check;
                for (Qualifier q: qualifierSet) {
                    if (null == q.getText()) {
                        continue;
                    }
                    check = addCheckbox(q);
                    selections.put(check, q);
                    box.add(check);
                }

		JPanel pane = new JPanel(new BorderLayout());
		pane.add(box, BorderLayout.PAGE_START);
		Border padding = BorderFactory.createEmptyBorder(20,20,5,20);
		pane.setBorder(padding);
		pane.setOpaque(true);
		pane.setBackground(RenderUtil.getAspectColor());
		return pane;
        
    }
    
    private JCheckBox addCheckbox(Qualifier q) {
        			JCheckBox check = new JCheckBox();
			check.setText(q.getText());
                        // User cannot modify NOT qualifier
                        if (q.isNot()) {
                            check.setSelected(true);
                            check.setEnabled(false);
                        }
                        else {
                            check.setSelected(false);
                        }
			return(check);
    }
    
    public boolean didUserSubmitForm() {
        return userClickedOk;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        userClickedOk = true;
		Set<JCheckBox> checkboxes = selections.keySet();

		for (JCheckBox check : checkboxes) {
			if (check.isSelected()) {
				selected.add(selections.get(check));
			}
		}
		this.setVisible(false);		        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public HashSet<Qualifier> getQualifiers() {
        setVisible(true);
        return selected;
    }
}
