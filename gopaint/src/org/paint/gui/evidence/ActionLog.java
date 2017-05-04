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

import org.apache.log4j.Logger;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.Evidence;
import org.geneontology.db.model.Term;
import org.paint.datamodel.GeneNode;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.EventManager;
import org.paint.gui.evidence.LogEntry.Action;


public class ActionLog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static ActionLog singleton;

	private List<LogEntry> done_log;
	private List<LogEntry> undone_log;

	private static Logger logger = Logger.getLogger(ActionLog.class);

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
	 * Need to also log all NOTs
	 * 
	 * Challenge mechanism
	 * 	
	 */
	public ActionLog() {
		done_log = new ArrayList<LogEntry>();
		undone_log = new ArrayList<LogEntry>();
	}

	public static ActionLog inst() {
		if (singleton == null) 
			singleton = new ActionLog();
		return (ActionLog) singleton;
	}

	public void clearLog() {
		done_log.clear();
		undone_log.clear();
	}
	
	public void logAssociation(GeneNode node, Association assoc, List<LogAssociation> removed) {
//		LogEntry entry = new LogEntry(node, assoc, removed, EvidencePanel.inst().getLogPane(assoc.getTerm().getCv()));
//		EvidencePanel.inst().displayEntry(entry);
//		done_log.add(entry);
	}

	public void logDisassociation(GeneNode node, Association removed) {
//		Term term = removed.getTerm();
//		LogEntry remove = findEntry(node, term, LogEntry.Action.ASSOC);
//		if (remove != null) {
//			EvidencePanel.inst().clearEntry(remove);
//			PaintAction.inst().redoDescendentAssociations(remove.getRemovedAssociations());
//			done_log.remove(remove);
//			undone_log.add(remove);
//		} else {
//			logger.info ("Could not find log entry for " + node.getNodeLabel() + " annotation to " + term.getName());
//		}
	}

	public void logNot(GeneNode node, Evidence evidence, String ev_code) {
//		Term term = evidence.getAssociation().getTerm();
//		String cv = term.getCv();
//		LogEntry entry = new LogEntry(node, evidence, EvidencePanel.inst().getLogPane(cv));
//		EvidencePanel.inst().displayEntry(entry);
//		done_log.add(entry);
	}

	public void logUnNot(GeneNode node, Evidence evi) {
//		LogEntry remove = findEntry(node, evi.getAssociation().getTerm(), LogEntry.Action.NOT);
//		if (remove != null) {
//			EvidencePanel.inst().clearEntry(remove);
//			done_log.remove(remove);
//			undone_log.add(remove);
//		} else
//			logger.info ("Could not find log entry for " + node.getNodeLabel() + " annotation to " + evi.getAssociation().getTerm().getName());
	}

	public void logPruning(GeneNode node, List<LogAssociation> purged) {
//		LogEntry branch = findEntry(node, null, LogEntry.Action.PRUNE);
//		if (branch != null) {
//			EvidencePanel.inst().clearEntry(branch);
//			done_log.remove(branch);
//			undone_log.add(branch);			
//		} else {
//			branch = new LogEntry(node, purged, EvidencePanel.inst().getPrunePanel());
//			EvidencePanel.inst().displayEntry(branch);
//			done_log.add(branch);
//		}
	}

	public void logGrafting(GeneNode node) {
//		LogEntry branch = findEntry(node, null, LogEntry.Action.PRUNE);
//		if (branch != null) {
//			PaintAction.inst().graftBranch(branch.getNode(), branch.getRemovedAssociations(), false);
//			EvidencePanel.inst().clearEntry(branch);
//			done_log.remove(branch);
//			undone_log.add(branch);
//		} else
//			logger.info ("Could not find log entry for " + node.getNodeLabel() + " to regraft to tree ");
	}

	public String doneString() {
		return actionString(done_log);
	}

	public String undoneString() {
		return actionString(undone_log);
	}

	private String actionString(List<LogEntry> log) {
		if (log.size() > 0) {
			StringBuffer buf = new StringBuffer();
			int index = log.size() - 1;
			LogEntry entry = log.get(index);
			switch (entry.getAction()) {
			case ASSOC: {
				buf.append(entry.getAction() + " to " + entry.getTerm().getName());
				break;
			}
			case NOT: {
				buf.append(entry.getAction() + " " + entry.getTerm().getName());
				break;				
			}
			case PRUNE: {
				buf.append(entry.getAction() + " of " + entry.getNode().getNodeLabel());
				break;								
			}
			}
			return buf.toString();
		}
		else {
			return null;
		}

	}

	public void undo(Association assoc) {
		LogEntry entry = null;
		for (int i = 0; i < done_log.size() && entry  == null; i++) {
			LogEntry check = done_log.get(i);
			if (check.getNode().getGeneProduct().equals(assoc.getGene_product()) &&
					check.getTerm().equals(assoc.getTerm()))
				entry = check;
		}
		done_log.remove(entry);
		undone_log.add(entry);
		takeAction(entry, true);
	}

	public void undo() {
		int index = done_log.size() - 1;
		LogEntry entry = done_log.get(index);
		done_log.remove(index);
		undone_log.add(entry);	
		/*
		 * Get the logs right before this so that the edit menu has the most up to date information
		 */
		takeAction(entry, true);
	}

	public void redo() {
		int index = undone_log.size() - 1;
		LogEntry entry = undone_log.remove(index);
		done_log.add(entry);
		/*
		 * Get the logs right before this so that the edit menu has the most up to date information
		 */
		takeAction(entry, false);
	}

	private void takeAction(LogEntry entry, boolean undo) {
//		PaintAction stroke = PaintAction.inst();
//		switch (entry.getAction()) {
//		case ASSOC: {
//			if (undo) {
//				stroke.undoAssociation(entry.getNode(), entry.getTerm());
//				stroke.redoDescendentAssociations(entry.getRemovedAssociations());
//				EvidencePanel.inst().clearEntry(entry);
//			} else {
//				entry.getRemovedAssociations().clear();
//				stroke.redoAssociation(entry.getLoggedAssociation(), entry.getRemovedAssociations());
//				EvidencePanel.inst().displayEntry(entry);
//			}
//			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(entry.getNode()));
//			break;
//		}
//		case NOT: {
//			if (undo) {
//				stroke.unNot(entry.getEvidence(), entry.getNode(), false);
//				EvidencePanel.inst().clearEntry(entry);
//			} else {
//				stroke.setNot(entry.getEvidence(), entry.getNode(), entry.getEvidence().getCode(), false);
//				EvidencePanel.inst().displayEntry(entry);
//			}
//			EventManager.inst().fireAnnotationChangeEvent(new AnnotationChangeEvent(entry.getNode()));
//			break;
//		}
//		case PRUNE: {
//			entry.getNode().setPrune(!entry.getNode().isPruned());
//			if (undo) {
//				stroke.graftBranch(entry.getNode(), entry.getRemovedAssociations(), false);
//				EvidencePanel.inst().clearEntry(entry);
//			} else {
//				stroke.pruneBranch(entry.getNode(), false);
//				EvidencePanel.inst().displayEntry(entry);
//			}
//			break;
//		}
//		}
	}

	private LogEntry findEntry(GeneNode node, Term term, Action action) {
		LogEntry found = null;
		for (LogEntry entry : done_log) {
			if (found == null) {
				if (entry.getNode() == node && ((term != null && entry.getTerm() == term && action == entry.getAction()) || (term == null && action == entry.getAction())))
					found = entry;	
			}
		}
		return found;
	}
}
