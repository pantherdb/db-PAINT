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
package org.paint.dataadapter;

import java.io.File;

import javax.swing.JOptionPane;

import org.bbop.framework.GUIManager;
import org.paint.datamodel.Family;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.ProgressEvent;
import org.paint.main.PaintManager;

import com.sri.panther.paintCommon.familyLibrary.PAINTFile;
import com.sri.panther.paintCommon.util.FileUtils;



public class FileAdapter extends FamilyAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Unused
	private String                paintfile;

	/**
	 * Constructor declaration
	 *
	 *
	 * @param fileName
	 *
	 * @see
	 */

	public FileAdapter(String paintfile){
		// expects the full filename, including the path, for the PTHR*****.paint file
		this.paintfile = paintfile;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	@Override
	public boolean fetchFamily(Family family) {
		System.gc();
                PAINTFile pf = new PAINTFile(paintfile);

//		PAINTFile pf = new PAINTFile(paintfile, false);
//		pf.initPAINTRead();
//
//		family.setFamilyID(pf.getBook());
//		family.setName(pf.getName());

		// Get path since this has to be appended to the tree and attribute file names)
		String  path = pf.getPAINTFilePath();

		EventManager.inst().fireProgressEvent(new ProgressEvent(this, "Processing gene family", 0, ProgressEvent.Status.START));
		String  treeContents[] = null;
		if (path != null) {
			String treeFileName = pf.getTreeFileName();
			if (treeFileName != null) {
				treeFileName = FileUtils.appendFileToPath(path, treeFileName);
				if (FileUtils.validPath(treeFileName)){
					treeContents = FileUtils.readFile(treeFileName);
				}
			}
		}

		// Now get subfamily to annotation node relationships
		String sfanInfo[] = null;
		String sfanFileName = pf.getSfanFileName();
		if (null != sfanFileName) {
			sfanFileName = FileUtils.appendFileToPath(path, sfanFileName);
			if (FileUtils.validPath(sfanFileName)) {
				sfanInfo = FileUtils.readFile(sfanFileName);
			}
		}
		else {
			// Current tree does not have subfamily annotation node relationship information
			sfanInfo = new String[0];
		}

		// Read the attribute file
		String attrFileName = pf.getAttrFileName();
		String  attrContents[] = null;
		if (null != attrFileName) {
			attrFileName = FileUtils.appendFileToPath(path, attrFileName);
			if (FileUtils.validPath(attrFileName)){
				attrContents = FileUtils.readFile(attrFileName);
			}
		} else {
			attrContents = new String[0];
		}

		//String  msaFileName = pf.getMSAFileName();
		String[]  msaContents = new String[0];
//		if (msaFileName != null){
//			msaFileName = FileUtils.appendFileToPath(path, msaFileName);
//			if (FileUtils.validPath(msaFileName)) {
//				msaContents = FileUtils.readFile(msaFileName);
//			}
//		} else {
//			msaContents = new String[0];
//		}

		// Check for wts file
//		String  wtsFileName = pf.getWtsFileName();
		String[] wtsContents = new String[0];
//		if (wtsFileName != null) {
//			wtsFileName = FileUtils.appendFileToPath(path, wtsFileName);
//			if (FileUtils.validPath(wtsFileName)) {
//				wtsContents = FileUtils.readFile(wtsFileName);
//			}
//		} else {
//			wtsContents = new String[0];
//		}

		// Parse file and create attribute table
		boolean ok = initFamily(family, treeContents, sfanInfo, attrContents, msaContents, wtsContents);

		if (ok) {
			EventManager.inst().fireProgressEvent(new ProgressEvent(this, "Processing gene family", 100, ProgressEvent.Status.RUNNING));
		}
		else {
			EventManager.inst().fireProgressEvent(new ProgressEvent(this, "Processing gene family", 100, ProgressEvent.Status.FAIL));
		}
		return ok;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public void saveOutput(){

		Family family = PaintManager.inst().getFamily();
			JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to save files in " + paintfile);
//		// Get PAINT file names
//		PAINTFile pf = new PAINTFile(paintfile, false);
//
//		boolean saved = pf.savePAINT(family.getFamilyID(), 
//				family.getName(), 
//				family.getTreeStrings(), 
//				family.getAttrTable(), 
//				family.getSfAnInfo(), 
//				family.getMSAcontent(), 
//				family.getWtsContent());
//
//    	String paintPath = FileUtils.getPath(paintfile);
//
//			/* 
//			 * record experimental annotations too
//			 * this will make it possible to work offline
//			 */
//    	saved &= GafAdapter.exportAnnotations(paintfile);
//    	saved &= EvidenceAdapter.exportEvidence(paintfile);
//
//
//		PaintManager.inst().setCurrentDirectory(new File(paintPath));
//
//		if (!saved) {
//			JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "Unable to save files in " + paintPath);
//		}
	}

}


