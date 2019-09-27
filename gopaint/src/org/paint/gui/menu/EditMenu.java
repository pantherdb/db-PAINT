/**
 *  Copyright 2019 University Of Southern California
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
import java.awt.event.KeyEvent;
import javax.swing.JMenu;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.dialog.CurationStatusColorDialog;
import org.paint.dialog.FindDialog;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.evidence.ActionLog;
import org.paint.main.PaintManager;

public class EditMenu extends JMenu
implements FamilyChangeListener, AnnotationChangeListener  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(EditMenu.class.getName());
	protected JMenuItem undoItem ;
	protected JMenuItem redoItem ;
	protected JMenuItem searchItem ;
	protected FindDialog     findDialog;

	private static final String undo = "Undo";
	private static final String redo = "Redo";
	private static final String find = "Find...";
	private static final String curation_status_color = "Curation status colors... (Does not Work!!!)";

	public EditMenu() {
		super("Edit");
		this.setMnemonic('e');

		undoItem = new JMenuItem(undo);
		undoItem.setMnemonic(KeyEvent.VK_Z);
		undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.META_MASK));
		undoItem.addActionListener(new undoActionListener());
		add(undoItem);
		undoItem.setEnabled(false);
		
		redoItem = new JMenuItem(redo);
		redoItem.setMnemonic(KeyEvent.VK_Y);
		redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.META_MASK));
		redoItem.addActionListener(new redoActionListener());
		add(redoItem);
		redoItem.setEnabled(false);
		
		// Separator line
		this.addSeparator();

		searchItem = new JMenuItem(find);
		searchItem.setMnemonic(KeyEvent.VK_F);
		//Setting the accelerator:
		searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.META_MASK));
		searchItem.addActionListener(new SearchActionListener());
		this.add(searchItem);
		searchItem.setEnabled(PaintManager.inst().getFamily() != null);

		// Separator line
		this.addSeparator();

		JMenuItem curationStatusColor = new JMenuItem(curation_status_color);
		curationStatusColor.addActionListener(new CurationStatusColorListener());
		this.add(curationStatusColor);

		/* So we can hide and show this menu based on what data is available */
		EventManager.inst().registerFamilyListener(this);
		EventManager.inst().registerGeneAnnotationChangeListener(this);
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	private class undoActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			ActionLog.inst().undo();
		}
	}

	private class redoActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			ActionLog.inst().redo();
		}
	}

	private class SearchActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if (null == findDialog){
				findDialog = new FindDialog(GUIManager.getManager().getFrame(), "Find");
			} else 
				findDialog.setVisible(true);
		}
	}

	public void newFamilyData(FamilyChangeEvent e) {
		searchItem.setEnabled(true);
		updateLogItems();
	}

	private class CurationStatusColorListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new CurationStatusColorDialog(GUIManager.getManager().getFrame());
		}
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		updateLogItems();
	}

	private void updateLogItems() {
		String item_label;
		item_label = ActionLog.inst().doneString();
		if (item_label != null) {
			undoItem.setText(undo + ' ' + item_label);
			undoItem.setEnabled(true);
		}
		else {
			undoItem.setText(undo);
			undoItem.setEnabled(false);
		}
		item_label = ActionLog.inst().undoneString();
		if (item_label != null) {
			redoItem.setText(redo + ' ' + item_label);
			redoItem.setEnabled(true);
		}
		else {
			redoItem.setText(redo);
			redoItem.setEnabled(false);	
		}
	}
}
