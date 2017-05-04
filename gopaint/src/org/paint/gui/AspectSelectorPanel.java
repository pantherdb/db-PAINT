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

package org.paint.gui;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.geneontology.db.model.Term;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.paint.config.Preferences;
import org.paint.go.GO_Util;
import org.paint.gui.AspectSelector.Aspect;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.main.PaintManager;

public class AspectSelectorPanel extends JPanel implements AspectChangeListener, TermSelectionListener {

	private static final long serialVersionUID = 1L;

	private JRadioButton bpButton;
	private JRadioButton ccButton;
	private JRadioButton mfButton;

	private Border plainBorder;

	public AspectSelectorPanel() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		//		setOpaque(true);
		//		setBackground(Preferences.inst().getBackgroundColor());

		mfButton = new JRadioButton("  Molecular Function  ", true);
		ccButton = new JRadioButton("  Cellular Component  ", false);
		bpButton = new JRadioButton("  Biological Process  ", false);

		AspectSelectorListener aspectSelectionListener = new AspectSelectorListener();
		bpButton.addActionListener(aspectSelectionListener);
		ccButton.addActionListener(aspectSelectionListener);
		mfButton.addActionListener(aspectSelectionListener);

		add(mfButton);
		add(ccButton);
		add(bpButton);

		ButtonGroup group = new ButtonGroup();
		group.add(mfButton);
		group.add(ccButton);
		group.add(bpButton);

		mfButton.setOpaque(true);
		ccButton.setOpaque(true);
		bpButton.setOpaque(true);

		Preferences prefs = Preferences.inst();

		mfButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_MF).darker());
		ccButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_CC));
		bpButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_BP));

		plainBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		mfButton.setMargin(new Insets(2, 12, 2, 12));

		mfButton.setBorderPainted(true);
		ccButton.setBorderPainted(true);
		bpButton.setBorderPainted(true);

		mfButton.setBorder(plainBorder);
		ccButton.setBorder(plainBorder);
		bpButton.setBorder(plainBorder);

		EventManager.inst().registerAspectChangeListener(this);
		EventManager.inst().registerTermListener(this);
	}

	class AspectSelectorListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JRadioButton button = (JRadioButton)e.getSource();
			switchAspect(button);
		}
	}

	@Override
	public void handleAspectChangeEvent(AspectChangeEvent event) {
		if (event.getSource() == this)
			return;

		Preferences prefs = Preferences.inst();
		if (mfButton.isSelected()) {
			mfButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_MF).darker());
			ccButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_CC));
			bpButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_BP));
		}
		if (bpButton.isSelected()) {
			mfButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_MF));
			ccButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_CC));
			bpButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_BP).darker());
		}
		else if (ccButton.isSelected()) {
			mfButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_MF));
			ccButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_CC).darker());
			bpButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_BP));
		}
	}

	@Override
	public void handleTermEvent(TermSelectEvent e) {
		List<Term> terms = e.getTermSelection();
		if (terms != null && !terms.isEmpty()) {
			Term term = terms.get(0);
			LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
			LinkedObject obo_term = (LinkedObject) GO_Util.inst().getObject(go_root, term.getAcc());
			String aspect_str = obo_term.getNamespace().getID().toLowerCase();
			if (aspect_str.equals(Aspect.BIOLOGICAL_PROCESS.toString()) && !bpButton.isSelected()) {
				bpButton.setSelected(true);
				switchAspect(bpButton);
			}
			else if (aspect_str.equals(Aspect.MOLECULAR_FUNCTION.toString()) && !mfButton.isSelected()) {
				mfButton.setSelected(true);
				switchAspect(mfButton);
			}
			else if (aspect_str.equals(Aspect.CELLULAR_COMPONENT.toString()) && !ccButton.isSelected()) {
				ccButton.setSelected(true);
				switchAspect(ccButton);
			}
		}
	}

	private void switchAspect(JRadioButton button) {
		Preferences prefs = Preferences.inst();
		if (button == bpButton) {
			AspectSelector.inst().setAspect(AspectSelector.Aspect.BIOLOGICAL_PROCESS);
			mfButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_MF));
			ccButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_CC));
			bpButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_BP).darker());
		}
		else if (button == ccButton) {
			AspectSelector.inst().setAspect(AspectSelector.Aspect.CELLULAR_COMPONENT);
			mfButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_MF));
			ccButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_CC).darker());
			bpButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_BP));
		}
		else if (button == mfButton) {
			AspectSelector.inst().setAspect(AspectSelector.Aspect.MOLECULAR_FUNCTION);
			mfButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_MF).darker());
			ccButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_CC));
			bpButton.setBackground(prefs.getAspectColor(Preferences.HIGHLIGHT_BP));
		}
	}
}
