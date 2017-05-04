/* 
 * 
 * Copyright (c) 2010, Regents of the University of California 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
 * or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package org.paint.dialog;

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

import org.geneontology.db.model.Term;
import org.paint.datamodel.GeneNode;
import org.paint.util.RenderUtil;

public class QualifierDialog extends JDialog implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Map<JCheckBox, Term> selections;
	Set<Term> qual_list;

	/** Creates the GUI shown inside the frame's content pane. */
	public QualifierDialog(Frame frame, Map<Term, Set<GeneNode>> qual2node) {
		super(frame, true);
		setLayout(new BorderLayout());
		setContentPane(qualifyPane(qual2node));
		qual_list = new HashSet<Term>();
		pack();
		setLocationRelativeTo(frame);
	}

	public Set<Term> getQualifiers() {
		setVisible(true);
		// this.paintComponents(this.getGraphics());
		return qual_list;
	}

	private JPanel qualifyPane (Map<Term, Set<GeneNode>> qual2node) {
		JPanel qualify = new JPanel();
		qualify.setLayout(new BoxLayout(qualify, BoxLayout.PAGE_AXIS));
		
		//Create the components.
		JPanel selectionPane = createSelectionPane(qual2node);

		//Lay them out.

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

	/** Creates the panel shown by the first tab. */
	private JPanel createSelectionPane(Map<Term, Set<GeneNode>> qual2node) {
		String description;
		if (qual2node.size() == 1) {
			description = "Check the box if you also want to propagate this qualifier.";
		}
		else {
			description = "Check the boxes if you also want to propagate any of these qualifiers.";
		}

		JPanel box = new JPanel();
		JLabel label = new JLabel(description);
		label.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		box.setOpaque(true);
		box.setBackground(RenderUtil.getAspectColor());

		box.setLayout(new BoxLayout(box, BoxLayout.PAGE_AXIS));
		box.add(label);

		selections = new HashMap<JCheckBox, Term>();
		Set<Term> qualifiers = qual2node.keySet();
		for (Term qual : qualifiers) {
			Set<GeneNode> nodes = qual2node.get(qual);
			JCheckBox check = new JCheckBox();
			StringBuffer qual_label = new StringBuffer (qual.getName() + " (from ");
			String prefix = "";
			for (GeneNode node : nodes) {
				qual_label.append(prefix + node.getNodeLabel());
				prefix = ", ";
			}
			check.setText(qual_label.toString() + ')');
			check.setSelected(false);
			selections.put(check, qual);
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

	public void actionPerformed(ActionEvent e) {
		Set<JCheckBox> checkboxes = selections.keySet();
		for (JCheckBox check : checkboxes) {
			if (check.isSelected()) {
				Term qual = selections.get(check);
				qual_list.add(qual);
			}
		}
		this.setVisible(false);				
	}
}
