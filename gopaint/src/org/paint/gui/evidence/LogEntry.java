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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.geneontology.db.model.Association;
import org.geneontology.db.model.Evidence;
import org.geneontology.db.model.Term;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.gui.AspectSelector.Aspect;

public class LogEntry {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String description;
	private String notes;
	private Evidence evidence;
	private List<LogAssociation> removed;
	private List<LogAssociation> added;
	private Action action;
	private LoggingPanel log_pane;

	public enum Action {
		ASSOC,
		NOT,
		PRUNE;

		public String toString() {
			return super.toString().toLowerCase();
		}
	}

	/*
	 * Used for actual annotations that have been created in PAINT
	 */
	public LogEntry(GeneNode node, Association assoc, List<LogAssociation> removed, LoggingPanel log_pane) {
		LogAssociation mini = new LogAssociation(node, assoc);
		added = new ArrayList<LogAssociation>();
		added.add(mini);
		this.removed = removed;
		action = Action.ASSOC;
		this.log_pane = log_pane;
		if (GO_Util.inst().contributesTo(assoc)) {
			description = assoc.getDate() + ": " + node.getNodeLabel() + " contributes to " + termLog(assoc.getTerm());
		} else if (GO_Util.inst().colocalizes(assoc)) {
			description = assoc.getDate() + ": " + node.getNodeLabel() + " colocated with " + termLog(assoc.getTerm());			
		} else {
			String aspect = assoc.getTerm().getCv();
			if (aspect.equals(Aspect.MOLECULAR_FUNCTION.toString())) {
				description = assoc.getDate() + ": " + node.getNodeLabel() + " has function " + termLog(assoc.getTerm());
			} else if (aspect.equals(Aspect.BIOLOGICAL_PROCESS.toString())) {
				description = assoc.getDate() + ": " + node.getNodeLabel() + " participates in " + termLog(assoc.getTerm());	
			} else {
				description = assoc.getDate() + ": " + node.getNodeLabel() + " located in " + termLog(assoc.getTerm());			
			}
		}
	}

	/*
	 * Used for NOT, negation of associations at a fixed node in the tree
	 */
	public LogEntry(GeneNode node, Evidence evidence, LoggingPanel log_pane) {
		LogAssociation mini = new LogAssociation(node, evidence.getAssociation());
		added = new ArrayList<LogAssociation>();
		added.add(mini);
		this.evidence = evidence;
		this.action = Action.NOT;
		this.log_pane = log_pane;
		description = dateNow() + ": " + node.getNodeLabel() + " lost/modified " + termLog(evidence.getAssociation().getTerm()) + " capacity";		
	}

	/*
	 * pruned list is long those with direction annotations to the pruned node
	 * or directly 'not'ted.
	 */
	public LogEntry(GeneNode node, List<LogAssociation> pruned, LoggingPanel log_pane) {
		LogAssociation mini = new LogAssociation(node);
		added = new ArrayList<LogAssociation>();
		added.add(mini);
		this.removed= pruned;
		this.action = Action.PRUNE;
		this.log_pane = log_pane;
		StringBuffer buf = new StringBuffer();
		buf.append(dateNow() + ": Pruned " + node.getNodeLabel());
		String prefix = " [";
		for (LogAssociation assoc : pruned) {
			buf.append(prefix + termLog(assoc.getTerm()));
			prefix = ", ";
		}
		if (pruned.size() > 0)
			buf.append(']');
		description = buf.toString();
		notes = "";
	}

	public LoggingPanel getLogPane() {
		return log_pane;
	}

	public Term getTerm() {
		if (added != null && added.size() == 1 && action != Action.PRUNE) {
			LogAssociation mini = added.get(0);
			return mini.getTerm();
		} else {
			return null;
		}
	}

	public GeneNode getNode() {
		if (added != null && added.size() == 1) {
			LogAssociation mini = added.get(0);
			return mini.getNode();
		} else {
			return null;
		}
	}

	public Evidence getEvidence() {
		return evidence;
	}

	public LogAssociation getLoggedAssociation() {
		if (added != null && added.size() == 1 && action != Action.PRUNE) {
			LogAssociation mini = added.get(0);
			return mini;
		} else {
			return null;
		}
	}

	public List<LogAssociation> getRemovedAssociations() {
		return removed;
	}

	public void setRemovedAssociations(List<LogAssociation> removed) {
		this.removed = removed;
	}

	public String getDescription() {
		return description;
	}

	public Action getAction() {
		return action;
	}

	private String termLog(Term term) {
		return term.getName() + " (" + term.getAcc() + ")";
	}

	private String dateNow() {
		long timestamp = System.currentTimeMillis();
		/* Date appears to be fixed?? */
		Date when = new Date(timestamp);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sdf.setTimeZone(TimeZone.getDefault()); // local time
		return sdf.format(when);
	}

	public String toString() {
		return description;
	}
}
