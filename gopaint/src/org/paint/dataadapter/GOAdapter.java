package org.paint.dataadapter;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.bbop.dataadapter.DataAdapterException;
import org.obo.dataadapter.OBOAdapter;
import org.obo.dataadapter.OBOFileAdapter;
import org.obo.dataadapter.OBOMetaData;
import org.obo.datamodel.OBOSession;
import org.paint.config.Preferences;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.ProgressEvent;
import org.paint.util.InternetChecker;

public class GOAdapter {

	protected static OBOMetaData metaData;

	public static OBOSession loadGO() {
		OBOSession session = null;
		try {
			session = getOboSession();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return session;
	}

	private static void fireProgressChange(String message, int percentageDone, ProgressEvent.Status status) {
		ProgressEvent event = new ProgressEvent(GOAdapter.class, message, percentageDone, status);
		EventManager.inst().fireProgressEvent(event);
	}

	private static void fireProgressChange(String message, int percentageDone) {
		fireProgressChange(message, percentageDone, ProgressEvent.Status.RUNNING);
	}

	/** file list is a list of file/url Strings */
	private static OBOSession getOboSession() throws Exception {
		OBOFileAdapter fa = new OBOFileAdapter();
		OBOFileAdapter.OBOAdapterConfiguration cfg = new OBOFileAdapter.OBOAdapterConfiguration();
		// takes strings not urls!
		Collection<String> fileList = new ArrayList<String>();
		String filename;
		if (InternetChecker.getInstance().isConnectionPresent()) {
			// Use the most current version from the GO web site
			filename = Preferences.inst().getGO_file();
		} else {
			// Use the local version in config directory
			filename = "config/gene_ontology.1_2.obo";
		}
		fileList.add(filename);
		//		fileList.add("/Users/suzi/projects/go/ontology/gene_ontology.obo");
		cfg.setReadPaths(fileList);
		cfg.setBasicSave(false);     //i think i need this for dangling references
		cfg.setAllowDangling(true);  //setting this to true for now!  should be configurable

		String progressMessage = "Loading Gene Ontology";

		try { // throws data adapter exception
			fireProgressChange(progressMessage, 0, ProgressEvent.Status.RUNNING);
			OBOSession os = fa.doOperation(OBOAdapter.READ_ONTOLOGY,cfg,null);
			fireProgressChange(progressMessage, 50);
			//			Collection<IdentifiedObject> IO = os.getLinkDatabase().getObjects();
			metaData = fa.getMetaData(); // check for null?
			fireProgressChange(progressMessage, 100, ProgressEvent.Status.RUNNING);
			return os;
		}
		catch (DataAdapterException e) {
			fileList.clear();
			// Use the local version in config directory
			filename = "config/gene_ontology.1_2.obo";
			fileList.add(filename);
			cfg.setReadPaths(fileList);
			cfg.setBasicSave(false);
			cfg.setAllowDangling(true);
			try {
				fireProgressChange("Loading GO from local file", 40);
				OBOSession os = fa.doOperation(OBOAdapter.READ_ONTOLOGY,cfg,null);
				metaData = fa.getMetaData(); // check for null?
				fireProgressChange(progressMessage, 100, ProgressEvent.Status.END);
				return os;
			}
			catch (DataAdapterException e2) {
				String m = "ERROR: Could not load ontology -- got obo data adapter exception: "+e2+" "+e2.getMessage()
				+" cause "+e2.getCause();
				JOptionPane.showMessageDialog(null,m,"Load failure",JOptionPane.ERROR_MESSAGE);
				fireProgressChange(progressMessage, 100, ProgressEvent.Status.END);
				return null;
			}
		}
	}

}
