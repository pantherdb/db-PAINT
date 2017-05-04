package org.paint.go;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bbop.dataadapter.AdapterConfiguration;
import org.bbop.dataadapter.DataAdapter;
import org.bbop.dataadapter.IOOperation;
import org.bbop.swing.BackgroundEventQueue;
import org.bbop.util.AbstractTaskDelegate;
import org.bbop.util.TaskDelegate;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.DBXref;
import org.geneontology.db.model.Evidence;
import org.geneontology.db.model.GeneProduct;
import org.geneontology.db.model.Term;
import org.geneontology.db.model.TermSynonym;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.gui.AspectSelector;


/**
 * A {@link TaskDelegate} that wraps a call to
 * {@link DataAdapter#doOperation(IOOperation, AdapterConfiguration, Object)}. This
 * task can then be scheduled with a {@link BackgroundEventQueue}.
 * 
 * @author jrichter
 * 
 * @param <IN> The input type of the expected io operation
 * @param <OUT> The output type of the expected io operation
 */

public class AssociationsTask extends AbstractTaskDelegate<GeneNode> {

	protected double progress;
	protected GeneNode node;

	//initialize logger
	protected final static Logger logger = Logger.getLogger(AssociationsTask.class);

	public void setNode(GeneNode node) {
		this.node = node;
	}

	@Override
	public void execute() throws Exception {
		GeneProduct gp = node.getGeneProduct();
		GO_Util.inst().initGP2Node(gp, node);
		Set<Association> associations = gp.getAssociations();
		Set<Association> remove = new HashSet<Association>();
		for (Iterator<Association> ait = associations.iterator(); ait.hasNext();) {
			Association assoc = ait.next();
			if (assoc.getTerm().isObsolete()) {
				remove.add(assoc);
			} 
			else { 
				/* Don't include the non-experimental annotations */
				Set<Evidence> non_exp_evidence = new HashSet<Evidence>();
				Set<Evidence> all_evidence = assoc.getEvidence();
				if (all_evidence != null && all_evidence.iterator() != null) {
					/*
					 * Need to check (and possibly remove) each piece of evidence separately
					 * because a given association may have a mix of exp. and other types of evidence
					 * If no evidence is left, then remove the association to the term
					 */
					for (Iterator<Evidence> eit = all_evidence.iterator(); eit.hasNext();) {
						Evidence evidence = eit.next();
						boolean cleared = false;
						if (!GO_Util.inst().isExperimental(evidence)) {
							non_exp_evidence.add(evidence);
							cleared = true;
						}
						if (!cleared) {						
							/*
							 * Clear out withs that come from PANTHER as these will be
							 * automatically generated
							 */
							cleared = clearPantherWiths(non_exp_evidence, evidence);
						}
						if (!cleared) {
							if (evidence.getDbxref().getDb_name().equals("Reactome")) {
								non_exp_evidence.add(evidence);
								cleared = true;
							}
						}
						if (!cleared) {
							DBXref reference = evidence.getDbxref();
							String pub_id = reference.getDb_name() + ':' + reference.getAccession();
							if (Preferences.inst().isExcluded(pub_id)) {
								non_exp_evidence.add(evidence);
								cleared = true;
							}
						}
					}
					for (Evidence evidence : non_exp_evidence) {
						all_evidence.remove(evidence);
					}
				}
				if (assoc.getEvidence().isEmpty()) {
					remove.add(assoc);
				}
			}
		}
		for (Association irrelevant : remove) {
			associations.remove(irrelevant);
		}
		for (Association assoc : associations) {
			/* 
			 * Make sure these are made persistent before the session with the go-db is closed
			 */
			grabInfo(node, assoc);
		}
		setResults(node);
	}

	private boolean clearPantherWiths(Set<Evidence> non_exp_evidence, Evidence evidence) { 
		Set<DBXref> withs = evidence.getWiths();
		List<DBXref> panther_irrelevant = new ArrayList<DBXref>();
		for (DBXref w : withs) {
			if (w.getDb_name().equals("PANTHER")) {
				panther_irrelevant.add(w);
			}
		}
		if (!panther_irrelevant.isEmpty()) {
			non_exp_evidence.add(evidence);
			return true;
		}
		else
			return false;
	}

	private void grabInfo(GeneNode node, Association assoc) {
		Set<Evidence> exp_evidence = assoc.getEvidence();
		for (Evidence evidence : exp_evidence) {
			Set<DBXref> withs = evidence.getWiths();
			for (DBXref xref : withs) {
				xref.getAccession();
				xref.getDb_name();
			}
		}
		Set<Term> quals = assoc.getQualifiers();
		if (quals != null) {
			for (Term qual : quals) {
				qual.getName();
			}
		}

		Term term = assoc.getTerm();
		String termCv = term.getCv();
		if (termCv.equals(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString())) {
			node.setBiologicalProcessCount(node.getBiologicalProcessCount() + 1);;
		}
		else if (termCv.equals(AspectSelector.Aspect.CELLULAR_COMPONENT.toString())){
			node.setCellularComponentCount(node.getCellularComponentCount() + 1);
		}
		else if (termCv.equals(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString())) {
			node.setMolecularFunctionCount(node.getMolecularFunctionCount() + 1);
		}
		GO_Util.inst().indexTerm(term, term.getAcc());
		Set<TermSynonym> syns = term.getSynonyms();
		for (TermSynonym syn : syns) {
			String see = syn.getAlternateAcc();
			if (see != null) {
				GO_Util.inst().indexTerm(term, see);
			}
		}
	}
}

