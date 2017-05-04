/* Copyright (C) 2008 LBNL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the BSD license
 * as published by the Free Software Foundation
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 */
package org.paint.dialog;
import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.paint.datamodel.Family;
import org.paint.main.PaintManager;


public class ActiveFamily {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(ActiveFamily.class);

	private static String extension;
	private static final String USER_DIR = "user.dir";
	private static final String DATA_DIR = "data";

	private JFileChooser chooser;
	private Frame frame;

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public ActiveFamily(Frame frame) {
		super();
		this.frame = frame;
	}

	private void setCurrentDirectory() {
		if (PaintManager.inst().getCurrentDirectory() != null){
			chooser.setCurrentDirectory(PaintManager.inst().getCurrentDirectory());
		}
		else {
			StringBuffer  defaultDirectory = new StringBuffer(System.getProperty(USER_DIR));
			defaultDirectory.append(File.separator);
			defaultDirectory.append(DATA_DIR);
			File f = new File(defaultDirectory.toString());
			if (f.exists()) {
				chooser.setCurrentDirectory(f);
			}
		}
	}

	public File getSelectedFile(boolean save, String suffix) {
		chooser = new JFileChooser();
		chooser.setDialogTitle("Choose a family");
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(new PAINTFileFilter());
		Family family = PaintManager.inst().getFamily();
		if (suffix == null || suffix.equals(""))
			extension = "";
		else
			extension = suffix;

		if (family != null && family.getFamilyID() != null) {
			String filename = family.getFamilyID();
			if (extension.length() > 0)
				filename = filename + '.' + extension;
			chooser.setSelectedFile(new File(filename));
		}
		setCurrentDirectory();

		chooser.setVisible(true);
		int returned;
		if (frame == null)
			log.debug("Frame is null");
		if (save) {
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			returned = chooser.showDialog(frame, "Save");
		}
		else {
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);
			returned = chooser.showDialog(frame, "Open");
		}
		File f = null;
		if (returned == JFileChooser.APPROVE_OPTION) {
			f = chooser.getSelectedFile();
			if (f != null)
				PaintManager.inst().setCurrentDirectory(f);
		}
		return f;
	}

	/**
	 * Class declaration
	 *
	 *
	 * @author
	 * @version %I%, %G%
	 */
	public class PAINTFileFilter extends FileFilter {

		@Override
		public boolean accept(File f){
			if (f.isDirectory()){
				return true;
			}
			String  suffix = getExtension(f);

			if (suffix != null){
				if (suffix.equals(extension)){
					return true;
				}
				else{
					return false;
				}
			}
			return false;
		}

		@Override
		public String getDescription(){
			return "PAINT FILES";
		}

		public String getExtension(File f){
			String  ext = null;
			String  s = f.getName();
			int     i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1){
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}

	}
}

