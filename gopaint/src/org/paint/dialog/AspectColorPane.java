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

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.paint.config.Preferences;
import org.paint.gui.AspectSelector;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.EventManager;

public class AspectColorPane extends JPanel {

	private static final long serialVersionUID = 1L;

	Color oldBPcolor = Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_BP);
	Color oldMFcolor = Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_MF);
	Color oldCCcolor = Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_CC);

	JColorChooser chooser = new JColorChooser();

	JRadioButton mf = new JRadioButton(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString());
	JRadioButton cc = new JRadioButton(AspectSelector.Aspect.CELLULAR_COMPONENT.toString());
	JRadioButton bp = new JRadioButton(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString());
	
	public AspectColorPane() {
		super();

		final CustomPreviewPanel previewPanel = new CustomPreviewPanel(chooser);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		mf.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					previewPanel.setOldColor(oldMFcolor);
					chooser.setColor(Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_MF));
				}
			}
		});
		mf.setSelected(true);

		cc.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					previewPanel.setOldColor(oldCCcolor);
					chooser.setColor(Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_CC));
				}
			}
		});

		bp.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					previewPanel.setOldColor(oldBPcolor);
					chooser.setColor(Preferences.inst().getAspectColor(Preferences.HIGHLIGHT_BP));
				}
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(mf);
		group.add(cc);
		group.add(bp);

		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
		radioPanel.add(mf);
		radioPanel.add(cc);
		radioPanel.add(bp);

		previewPanel.setSize(previewPanel.getPreferredSize());
		previewPanel.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));
		chooser.setPreviewPanel(previewPanel);
		chooser.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Preferences preferences = Preferences.inst();
				if (mf.isSelected()) {
					preferences.setAspectColor(Preferences.HIGHLIGHT_MF, chooser.getColor());
				}
				else if (cc.isSelected()) {
					preferences.setAspectColor(Preferences.HIGHLIGHT_CC, chooser.getColor());
				}
				else if (bp.isSelected()) {
					preferences.setAspectColor(Preferences.HIGHLIGHT_BP, chooser.getColor());
				}
				AspectChangeEvent aspEvent = new AspectChangeEvent(this);
				EventManager.inst().fireAspectChangeEvent(aspEvent);
			}
		});
		
		add(radioPanel);
		add(chooser);
	}	

	public void cancelColorChange() {
		Preferences preferences = Preferences.inst();
		preferences.setAspectColor(Preferences.HIGHLIGHT_BP, oldBPcolor);
		preferences.setAspectColor(Preferences.HIGHLIGHT_MF, oldMFcolor);
		preferences.setAspectColor(Preferences.HIGHLIGHT_CC, oldCCcolor);
		AspectChangeEvent aspEvent = new AspectChangeEvent(this);
		EventManager.inst().fireAspectChangeEvent(aspEvent);
	}

	public void resetColorChange() {
		Preferences preferences = Preferences.inst();
		preferences.setAspectColor(Preferences.HIGHLIGHT_BP, oldBPcolor);
		preferences.setAspectColor(Preferences.HIGHLIGHT_MF, oldMFcolor);
		preferences.setAspectColor(Preferences.HIGHLIGHT_CC, oldCCcolor);
		Color c = null;
		if (bp.isSelected()) {
			c = oldBPcolor;
		}
		else if (mf.isSelected()) {
			c = oldMFcolor;
		}
		else if (cc.isSelected()) {
			c = oldCCcolor;
		}
		chooser.setColor(c);
		AspectChangeEvent aspEvent = new AspectChangeEvent(this);
		EventManager.inst().fireAspectChangeEvent(aspEvent);
	}
}
