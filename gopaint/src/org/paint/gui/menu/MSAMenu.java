/**
 *  Copyright 2022 University Of Southern California
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
package org.paint.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.dialog.MSAColorDialog;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.msa.MSA;
import org.paint.gui.msa.MSAPanel;
import org.paint.main.PaintManager;

public class MSAMenu extends JMenu
implements FamilyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger("MSAMenu");

	private static final String LABEL_FULL_ALIGN = "Entire Alignment";
	private static final String LABEL_TRIMMED_ALIGN = "Trimmed Alignment";
	private static final String LABEL_DOMAIN = "Domain";
	private static final String LABEL_DOMAIN_TRIMMED = "Condensed Domain";
        private static final String LABEL_KEY_RESIDUE = "Key Residue";
	//	private static final String match_align = "Condensed Alignment";
	//	private static final String conserved = "Subfamily conserved";
	private static final String weight = "Use weights";
        
	private static final String update = "Edit msa colors/thresholds";

	//private JMenuItem  weightedItem;
	private JMenuItem fullItem;
        private JMenuItem matchStatesItem;
        private JMenuItem domainItem;
        private JMenuItem domainTrimmedItem;
        private JMenuItem keyResidueItem;

	public MSAMenu() {
		super("MSA and Domain");
//                ButtonGroup group = new ButtonGroup();
//            MSAPanel msaPanel = PaintManager.inst().getMSAPanel();
////            if (null != msaPanel) {
//                MSA msa = msaPanel.getModel();
//                if (msa != null) {
                    fullItem = new JMenuItem(LABEL_FULL_ALIGN);
//                    group.add(fullItem);
                    add(fullItem);
                    fullItem.setEnabled(false);
                    fullItem.addActionListener(new MSAActionListener());

                    matchStatesItem = new JMenuItem(LABEL_TRIMMED_ALIGN);
//                    group.add(matchStatesItem);
                    add(matchStatesItem);
                    matchStatesItem.setEnabled(false);
                    matchStatesItem.addActionListener(new MSAActionListener());

                    domainItem = new JMenuItem(LABEL_DOMAIN);
//                    group.add(domainItem);
                    add(domainItem);
                    domainItem.setEnabled(false);
                    domainItem.addActionListener(new MSAActionListener());
                    
                    domainTrimmedItem = new JMenuItem(LABEL_DOMAIN_TRIMMED);
//                    group.add(domainTrimmedItem);
                    add(domainTrimmedItem);
                    domainTrimmedItem.setEnabled(false);
                    domainTrimmedItem.addActionListener(new MSAActionListener());      
                    
                    keyResidueItem = new JMenuItem(LABEL_KEY_RESIDUE);
                    add(keyResidueItem);
                    keyResidueItem.setEnabled(false);
                    keyResidueItem.addActionListener(new MSAActionListener());                   

//                }
//            }
                
//		boolean full_length;
//		boolean use_weight;
//		boolean weighted;

//		MSAPanel msa = PaintManager.inst().getMSA();
//		if (msa != null) {
//			full_length = msa.isFullLength();
//			use_weight = msa.haveWeights();
//			weighted = msa.isWeighted();
//		}
//		else {
//			full_length = true;;
//			use_weight = false;
//			weighted = false;
//		}

//		fullItem = new JMenuItem(full_align);
//		fullItem.setSelected(full_length);
//		fullItem.addActionListener(new MSAActionListener());
//		add(fullItem);
//
//		addSeparator();
//
//		weightedItem = new JCheckBoxMenuItem(weight);
//		weightedItem.setSelected(weighted);
//		weightedItem.setEnabled(use_weight);
//		weightedItem.addActionListener(new MSAActionListener());
//		add(weightedItem);
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
                EventManager ev = EventManager.inst();
		ev.registerFamilyListener(this);
//                ev.registerDomainChangeListener(this);
	}

	public void updateMenu(MSAPanel msaPanel) {
            MSA msa = msaPanel.getModel();
            if (null != msa) {
                if (true == msa.haveFullAlignData()) {
                    fullItem.setEnabled(true);
                    fullItem.setSelected(true);
                }
                if (true == msa.haveTrimmedAlignData()) {
                    matchStatesItem.setEnabled(true);
                    matchStatesItem.setSelected(false);
                }
                if (true == msa.haveDomainInfo()) {
                    domainItem.setEnabled(true);
                    domainItem.setSelected(false);
                    domainTrimmedItem.setEnabled(true);
                    domainTrimmedItem.setSelected(false);                    
                }
                if (true == msa.haveHaveKeyResidueInfo()) {
                    keyResidueItem.setEnabled(true);
                    keyResidueItem.setSelected(false);
                }
            }
            else {
                fullItem.setEnabled(false);
                matchStatesItem.setEnabled(false);
                domainItem.setEnabled(false);
                domainTrimmedItem.setEnabled(false);
                keyResidueItem.setEnabled(false);
            }
//		if (msaPanel != null) {
//			boolean full_length = msaPanel.isFullLength();
//			fullItem.setSelected(full_length);
//			// Only add this menu item, if sequence weights information is available
//			//			sfConHMMItem.setVisible(show);
//			weightedItem.setEnabled(msaPanel.haveWeights());
//			weightedItem.setSelected(msaPanel.isWeighted());			
//			// Allow saving of msa information, if book was opened locally
//		}
	}


//    public void handleDomainChangeEvent(DomainChangeEvent event) {
//        MSAPanel msa = PaintManager.inst().getMSAPanel();
//        if (msa != null) {
//            setVisible(true);
//            updateMenu(msa);
//        } else {
//            setVisible(false);
//        }
//    }

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
			MSAPanel msaPanel = PaintManager.inst().getMSAPanel();
                        if (null != msaPanel) {
//                            System.out.println("MSA panel is valid");
                            MSA msa = msaPanel.getModel();
                            if (null == msa) {
                                return;
                            }
//                            System.out.println("MSA model is valid");
                            if (e.getSource().equals(fullItem) && true == fullItem.isEnabled()) {
                                msa.setDisplayType(MSA.MSA_DISPLAY.ENTIRE_ALIGNMENT);
                                msaPanel.setFullLength(true);                                
//                                System.out.println("msa - switching to full length display");
                            }
                            else if (e.getSource().equals(matchStatesItem) && true == matchStatesItem.isEnabled()) {
                                msa.setDisplayType(MSA.MSA_DISPLAY.TRIMMED);
                                msaPanel.setFullLength(false);                                
//                                System.out.println("msa - switching to trimmed display");                                
                            }
                            else if (e.getSource().equals(domainItem) && true == domainItem.isEnabled()) {
                                msa.setDisplayType(MSA.MSA_DISPLAY.DOMAIN);
                                msaPanel.setFullLength(true);                                
//                                System.out.println("msa - switching to full length domain display");                 
                            }
                            else if (e.getSource().equals(domainTrimmedItem) && true == domainTrimmedItem.isEnabled()) {
                                msa.setDisplayType(MSA.MSA_DISPLAY.DOMAIN_TRIMMED);
                                msaPanel.setFullLength(true);                                
//                                System.out.println("msa - switching to trimmed domain display");                                 
                            }
                            else if (e.getSource().equals(keyResidueItem) && true == keyResidueItem.isEnabled()) {
                                msa.setDisplayType(MSA.MSA_DISPLAY.KEY_RESIDUE);
                                msaPanel.setFullLength(true);                                
//                                System.out.println("msa - switching to trimmed domain display");                                 
                            }                            
                        }

		}
	}

	private class MSAUpdateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MSAPanel msa = PaintManager.inst().getMSAPanel();
			MSAColorDialog dlg = new MSAColorDialog(GUIManager.getManager().getFrame(), msa.isWeighted());
			if (dlg.display()) {
				msa.updateColors();
			}
		}
	}

	public void newFamilyData(FamilyChangeEvent e) {
		MSAPanel msa = PaintManager.inst().getMSAPanel();
		if (msa != null) {
			setVisible(true);
			updateMenu(msa);
		} else {
			setVisible(false);
		}

	}
}
