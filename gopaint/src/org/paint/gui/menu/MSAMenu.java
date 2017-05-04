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
package org.paint.gui.menu;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.swing.DynamicMenu;
import org.paint.dialog.MSAColorDialog;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.msa.MSA;
import org.paint.gui.msa.MSAPanel;
import org.paint.main.PaintManager;

public class MSAMenu extends DynamicMenu
implements FamilyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger("MSAMenu");

	private static final String full_align = "Entire Alignment";
	//	private static final String match_align = "Condensed Alignment";
	//	private static final String conserved = "Subfamily conserved";
	private static final String weight = "Use weights";
	private static final String update = "Edit msa colors/thresholds";

	private JCheckBoxMenuItem weightedItem;
	private JCheckBoxMenuItem fullItem;

	public MSAMenu() {
		super("Alignment");
		boolean full_length;
		boolean use_weight;
		boolean weighted;

		MSAPanel msa = PaintManager.inst().getMSA();
		if (msa != null) {
			full_length = msa.isFullLength();
			use_weight = msa.haveWeights();
			weighted = msa.isWeighted();
		}
		else {
			full_length = true;;
			use_weight = false;
			weighted = false;
		}

		fullItem = new JCheckBoxMenuItem(full_align);
		fullItem.setSelected(full_length);
		fullItem.addActionListener(new MSAActionListener());
		add(fullItem);

		addSeparator();

		weightedItem = new JCheckBoxMenuItem(weight);
		weightedItem.setSelected(weighted);
		weightedItem.setEnabled(use_weight);
		weightedItem.addActionListener(new MSAActionListener());
		add(weightedItem);
		//
		//		wtsItem = new JCheckBoxMenuItem(coloring);
		//		wtsItem.setSelected(displayType == MSA.DISPLAY_TYPE_WTS);
		//		wtsItem.addActionListener(new MSAActionListener(MSA.DISPLAY_TYPE_WTS, this));
		//		add(wtsItem);

		addSeparator();

		JMenuItem updateMSA = new JMenuItem(update);
		updateMSA.addActionListener(new MSAUpdateActionListener());
		add(updateMSA);

		/* So we can hide and show this menu based on what data is available */
		EventManager.inst().registerFamilyListener(this);
	}

	public void updateMenu(MSAPanel msa) {
		if (msa != null) {
			boolean full_length = msa.isFullLength();
			fullItem.setSelected(full_length);
			// Only add this menu item, if sequence weights information is available
			//			sfConHMMItem.setVisible(show);
			weightedItem.setEnabled(msa.haveWeights());
			weightedItem.setSelected(msa.isWeighted());			
			// Allow saving of msa information, if book was opened locally
		}
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class MSAActionListener implements ActionListener{
		MSAActionListener() {
		}
		/**
		 * Method declaration
		 *
		 *
		 * @param e
		 *
		 * @see
		 */
		public void actionPerformed(ActionEvent e) {
			MSAPanel msa = PaintManager.inst().getMSA();
			if (e.getSource().equals(fullItem)) {
				msa.setFullLength(fullItem.isSelected());
			} else if (e.getSource().equals(weightedItem)) {
				msa.setWeighted(weightedItem.isSelected());
			}
		}
	}

	private class MSAUpdateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MSAPanel msa = PaintManager.inst().getMSA();
			MSAColorDialog dlg = new MSAColorDialog(GUIManager.getManager().getFrame(), msa.isWeighted());
			if (dlg.display()) {
				msa.updateColors();
			}
		}
	}

	public void newFamilyData(FamilyChangeEvent e) {
		MSAPanel msa = PaintManager.inst().getMSA();
		if (msa != null) {
			setVisible(true);
			updateMenu(msa);
		} else {
			setVisible(false);
		}

	}
}
