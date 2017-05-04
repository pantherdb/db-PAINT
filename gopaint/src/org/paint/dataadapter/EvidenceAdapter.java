package org.paint.dataadapter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.paint.go.GOConstants;
import org.paint.gui.evidence.EvidencePanel;

import com.sri.panther.paintCommon.util.FileUtils;

public class EvidenceAdapter {

	protected static Logger log = Logger.getLogger(EvidenceAdapter.class);

	/**
	 * Method declaration
	 *
	 * @param File evidence_file
	 * @throws IOException 
	 *
	 * @see
	 */
	public static void importEvidence(String path) {
		StringBuffer errors = new StringBuffer();
		String text = null;
		if (path != null) {
			File evi_file = FileUtil.inst().getFile(path, "txt");
			if (evi_file.isFile() && evi_file.canRead()) {
				try {
					String file_name = evi_file.getCanonicalPath();
					// Read contents of GO annotation file
					String[]  evidence = FileUtils.readFile(file_name);
					if ((null != evidence && evidence.length > 0)) {
						text = "";
						for (int i = 0; i < evidence.length; i++) {
							text += evidence[i] + "\n";
						}
					} else {
						errors.append(file_name + " is empty");
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), errors.toString(), "Failed to import evidence file", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		if (text != null) {
			int start = text.indexOf(GOConstants.GO_REF_TITLE);
			if (start >= 0) {
				String prefix = text.substring(0, start);
				int end = start + GOConstants.go_ref.length();
				String suffix = (end >= text.length()) ? "" : text.substring(end - 1);
				text = prefix + suffix;
			}
		}
//		EvidencePanel.inst().setComment(text);
	}

	public static boolean exportEvidence(String path) {
		boolean success = false;
		try {
			if (path != null){
				File f = FileUtil.inst().getFile(path, "txt");
				String file_name = f.getCanonicalPath();
				// Read contents of GO annotation file
				/* 
				 * we have to traverse the entire tree to do this
				 */
//				BufferedWriter bufWriter = new BufferedWriter(new FileWriter(file_name));
//				String evi_text = EvidencePanel.inst().getEvidenceText() + "\n\n" + GOConstants.go_ref;
//				if (null != bufWriter) {
//					bufWriter.write(evi_text + "\n");
//					bufWriter.close();
//				}

				String warnings = EvidencePanel.inst().getWarnings();
				if (warnings != null && warnings.length() > 0) {
//					long timestamp = System.currentTimeMillis();
//					Date when = new Date(timestamp);
//					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//					sdf.setTimeZone(TimeZone.getDefault()); // local time
//					String date_str = sdf.format(when);
//
//					f = FileUtil.inst().getFile(path, date_str + "-txt");
//					file_name = f.getCanonicalPath();
//					bufWriter = new BufferedWriter(new FileWriter(file_name));
//					if (null != bufWriter) {
//						bufWriter.write(warnings);
//						bufWriter.close();
//					}
				}
				success = true;
			}
		}
		catch (IOException ie){
			log.error("IO exception " + ie.getMessage() + " returned while attempting to export evidence file " + path);
		}
		return success;
	}

}
