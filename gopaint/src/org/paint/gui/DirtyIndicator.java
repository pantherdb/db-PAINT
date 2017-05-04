package org.paint.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.framework.VetoableShutdownListener;
import org.paint.dataadapter.EvidenceAdapter;
import org.paint.dataadapter.GafAdapter;
import org.paint.datamodel.Family;
import org.paint.dialog.ActiveFamily;
import org.paint.go.GO_Util;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;

/**
 * Listens for editing by the user to indicate that the document needs saving before it's closed. Editing state is reset anytime a user saves
 * the document.  Implements VetoableShutdownListener to be used by GUIManager when a user attempts to quit.
 * @author Jim Balhoff
 */
public class DirtyIndicator implements VetoableShutdownListener {

	@SuppressWarnings("unused")
	private Logger log() {
		return Logger.getLogger(this.getClass());
	}

	private static DirtyIndicator singleton;

	private static boolean dirty_genes = false;
        private static boolean annotated = false;

	private DirtyIndicator() {
	}

	public static DirtyIndicator inst() {
		if (singleton == null) 
			singleton = new DirtyIndicator();
		return singleton;
	}

	/** from VetoableShutdownListener interface, this method is called from plus/obo/bbop
      framework on exit, if document is dirty it calls up user dialog to query about
      saving changes and canceling exit, if false is returned(cancel) then shutdown
      is cancelled/vetoed */
	public boolean willShutdown() {
		boolean shut_down;
		if (familyLoaded() && dirty_genes) {
			shut_down = this.runDirtyDialog("quitting?");
		} else {
			shut_down = true;
		}
		return shut_down;
	}

	/**
	 * This is called from the filemenu to determine whether or not to enable the "Save" menu item
	 * we're not talking about saving to the database, just saving to local files
	 * To save:
	 * 	something must be loaded
	 * 		and they are working locally
	 * 
	 */
	public boolean familyLoaded() {
		Family family = PaintManager.inst().getFamily();
		return (family != null && family.isLoaded());
	}

	/** brings up dialog asking user if they want to save changes or cancel operation
      (shutdown or new), returning false indicates cancellation
      shutdown is true if this is for shutdown, and false for clearing out data
	 */
	public boolean runDirtyDialog(String prompt) {
		if (genesAreDirty()) {
			String save = "Save";
			String cancel = "Cancel";
			String dontSave = "Don't Save";
			final String[] options = {save, cancel, dontSave};
			String m = "You have unsaved GO annotations.  Would you like to export a GAF file before " + prompt;
			final int result = 
				JOptionPane.showOptionDialog(GUIManager.getManager().getFrame(),m, "",
						JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, "Save");
			if (options[result] == save) {
				ActiveFamily dlg = new ActiveFamily(GUIManager.getManager().getFrame());
				File f = dlg.getSelectedFile(true, "gaf");
				if (null != f){
					String familyID = PaintManager.inst().getFamily().getFamilyID();
					try {
						String path = f.getCanonicalPath();
						GafAdapter.exportAnnotations(path);
						EvidenceAdapter.exportEvidence(path);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						log().error("Unable to save " + familyID);
					}
				}	
			}
			return (options[result] != cancel);
		} else {
			return true;
		}
	}

	public boolean genesAreDirty() {
		return dirty_genes;
	}

	public void dirtyGenes(boolean soiled) {
		dirty_genes = soiled;
	}

	public boolean isPainted() {
		if (familyLoaded()) {
			TreePanel tree = PaintManager.inst().getTree();
			return GO_Util.inst().isPainted(tree.getRoot(), true);
		} else {
			return false;
		}
	}
        
        public void setAnnotated(boolean annotated) {
            this.annotated = annotated;
        }
        
        public boolean bookUpdated() {
            return annotated;
        }

}
