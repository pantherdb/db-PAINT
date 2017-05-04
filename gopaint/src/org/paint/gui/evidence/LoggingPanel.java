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

package org.paint.gui.evidence;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.apache.log4j.Logger;
import org.paint.config.Preferences;


public class LoggingPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static LoggingPanel singleton;

	private JTextArea annotation_log;
	private List<String> entries;

	private static Logger logger = Logger.getLogger(EvidencePanel.class);

	/*
	 * Separated into sections by aspect ?
	 * Include dates?
	 * Section for References
	 * 	http://en.wikipedia.org/wiki/SKI_protein
	 * 	PMID: 19114989 
	 * Phylogeny
	 * 	Two main clades, SKOR and SKI/SKIL, plus an outlier from Tetrahymena which aligns poorly, so I have not annotated AN0.

	 * Propagate GO:0004647 "phosphoserine phosphatase activity" to AN1 and GO:0016791 "phosphatase activity" to AN0.
	 * -Propagate "cytoplasm" to AN1 based on 3 annotations.
	 * -Propagate "chloroplast" to plants/chlamy.
	 * 
	 * Challenge mechanism
	 * 	
	 */
	public LoggingPanel(Border border) {
		super ();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		entries = new ArrayList<String>();
		
		annotation_log = new JTextArea();
		annotation_log.setEditable(false);
		annotation_log.setLineWrap(true);
		annotation_log.setWrapStyleWord(true);

		setOpaque(true);
	
		setBorder(border);
		setBackground(Preferences.inst().getBackgroundColor());
		add(annotation_log);
	}

	public String getEntry() {
		return annotation_log.getText();
	}

	public void logEntry(String text) {
		entries.add(0, text);
		setLogText();
	}
	
	public boolean logErase(String text) {
		boolean removed = false;
		for (int i = entries.size() -1; i >= 0 && !removed; i--) {
			String check = entries.get(i);
			if (removed = check.contains(text)) {
				entries.remove(i);
				removed = true;
			}
		}
		setLogText();
		return removed;
	}
	
	private void setLogText() {
		annotation_log.setText("");
		String prefix = "";
		for (String entry : entries) {
			annotation_log.setText(annotation_log.getText() + prefix + entry);
			prefix = "\n";
		}
		repaint();
	}
	
	protected void clearLog() {
		entries.clear();
		annotation_log.setText("");
	}
}
