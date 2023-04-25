/**
 *  Copyright 2022 University Of Southern California
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
package org.paint.go;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JProgressBar;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.geneontology.db.factory.GOobjectFactory;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.DB;
import org.geneontology.db.model.DBXref;
import org.geneontology.db.model.GeneProduct;
import org.geneontology.db.model.GraphPath;
import org.geneontology.db.model.Species;
import org.geneontology.db.model.Term;
import org.hibernate.Session;
import org.obo.datamodel.IdentifiedObject;
import org.obo.datamodel.Link;
import org.obo.datamodel.LinkDatabase;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.MultiIDObject;
import org.paint.config.Preferences;
import org.paint.datamodel.Family;
import org.paint.datamodel.GeneNode;
import org.paint.main.PaintManager;
import org.paint.util.InternetChecker;


public class GO_Util {

	private static final HashSet<String> mod_list = new HashSet<String>();
	static {
		mod_list.add("MGI");
		mod_list.add("RGD");
		mod_list.add("FB");
		mod_list.add("SGD");
		mod_list.add("PomBase");
		mod_list.add("TAIR");
		mod_list.add("ZFIN");
		mod_list.add("WB");
		mod_list.add("dictyBase");
		mod_list.add("EcoCyc");
	};


	private static final HashSet<String> EXP_strings = new HashSet<String>();
	static { 
		EXP_strings.add("EXP");
		EXP_strings.add("IDA");
		EXP_strings.add("IEP");
		EXP_strings.add("IGI");
		EXP_strings.add("IMP");
		EXP_strings.add("IPI");
		EXP_strings.add("IKR");
		EXP_strings.add("IRD");
		EXP_strings.add("TAS");
	};

	/**
	 * The cross reference to the publication/evidence for the annotation 
	 * of this family 
	 */
	private static DBXref paint_dbxref;

	/* 
	 * Since the terms are held both from the obo file and from the go database
	 * need a way to move from the obo one to the database one
	 */
	private static HashMap<LinkedObject, Term> term2term;

	private static HashMap<String, Term> name2term;

	private HashMap<GeneProduct, GeneNode> gp2node;

	//Delay, in milliseconds before we interrupt MessageLoop
	//thread (default 5 minutes).
	private static long patience = 1000 * 60 * 5;

	public static int PENDING = 0;
	public static int SUCCESS = 1;
	public static int FAILURE = -1;

	private static JProgressBar progressBar;
	private static int finished_count;

	private static GO_Util go_util;

	private static Logger log = Logger.getLogger(GO_Util.class);

	private Map<String, LinkedObject> alternateIdObjects;

	private Map<String, DB> nameToDB;
	private Map<String, Species> taxonToSpecies;
	private Map<String, Term> accToTerm;
	private Map<String, Term> altAccToTerm;

	private GO_Util() {
	}

	public static GO_Util inst() {
		if (go_util == null) {
			go_util = new GO_Util();
		}
		return go_util;
	}

	public GOobjectFactory getGOfactory() {
		return new GOobjectFactory("config/hibernate.cfg.xml");
	}

//	public boolean getGeneProducts (List<String []> go_genes) {
//		GOobjectFactory go_factory = getGOfactory();
//		GeneProductThread thread = new GeneProductThread(go_factory);
//		thread.setGeneList(go_genes);
//
//		boolean error = false;
//
//		thread.start();
//
//		try {
//			thread.join(patience);
//		}
//		catch (InterruptedException e) {
//			log.error("Error fetching gene products");
//			error = true;
//		}
//		if (!thread.getState().equals(Thread.State.TERMINATED)) {
//			error = true;
//			thread.interrupt();
//			log.error("Timed out fetching gene products");
//		}
//		go_factory.close();
//		return error;
//	}

	public int threadFinished(Thread thread, long startTime) {
		//loop until MessageLoop thread exits
		int status = (thread == null ? PENDING : SUCCESS);
		if (thread != null && thread.isAlive()) {
			//Wait maximum of 0.001 second for MessageLoop thread to
			//finish.
			try {
				thread.join(10);
				if (((System.currentTimeMillis() - startTime) > patience) &&
						thread.isAlive()) {
					thread.interrupt();
					//Shouldn't be long now -- wait indefinitely
					thread.join();
				} else {
					status = PENDING;
				}
			} catch (InterruptedException e) {
				status = FAILURE;
			}

		}
		Frame frame = GUIManager.getManager().getFrame();
		if (status == SUCCESS) {
			finished_count++;
			progressBar.setValue(finished_count);
			progressBar.setString("Getting GO annotations...");
			progressBar.repaint();			
		} else if (frame.isAncestorOf(progressBar)) {
			frame.remove(progressBar);
		}
		return status;
	}

	public GeneNode getGeneNode(GeneProduct gp) {
		GeneNode node = null;
		if (gp2node != null) {
			node = gp2node.get(gp);
		}
		if (node == null) {
			String db_id = gp.getDbxref().getAccession();
			node = PaintManager.inst().getGeneByGP(gp.getDbxref().getDb_name(), db_id);
			if (node != null)
				gp2node.put(gp, node);
		}
		if (node == null) {
			log.debug("No node for gene_product " + gp.getSymbol() + " database id " + gp.getDbxref().getAccession());
		}
		return node;
	}

//	public boolean isExperimental(Evidence evidence) {
//		return EXP_strings.contains(evidence.getCode());
//	}
//
//	public boolean isExperimental(Association assoc) {
//		boolean experimental = false;
//		if (assoc.getEvidence() != null && assoc.getEvidence().iterator() != null) {
//			for (Iterator<Evidence> ev_set = assoc.getEvidence().iterator(); ev_set.hasNext() && !experimental;) {
//				experimental = isExperimental(ev_set.next()) && !isPAINTAnnotation(assoc);
//			}
//		}
//		return experimental;
//	}

	public synchronized Term getTermByAcc(String acc) {
		if (accToTerm == null) {
			accToTerm = new HashMap<String, Term>();
		}
		Term term = accToTerm.get(acc);
		boolean online = InternetChecker.getInstance().isConnectionPresent();
		if (term == null && online) {
			GOobjectFactory factory = getGOfactory();
			Session session = factory.startSession();
			term = (Term) factory.getTermByAcc(acc, session);
			initTerm(term);
			session.close();
			factory.close();
		}
		if (term == null) {
			/*
			 * If we're working offline the terms cannot be loaded from the GODB
			 */
			term = new Term();
			term.setAcc(acc);
			LinkDatabase linkDb = PaintManager.inst().getGoRoot().getLinkDatabase();
			LinkedObject obo_term = (LinkedObject)GO_Util.inst().getObject(linkDb, term.getAcc());
			term.setName(obo_term.getName());
			term.setCv(obo_term.getNamespace().getID());
		}
		if (term != null)
			accToTerm.put(acc, term);
		return term;
	}

	private void initTerm(Term term) {
		if (term == null)
			log.debug("Failed to get term, will have to synthesize: ");
		else {
			StringBuffer force = new StringBuffer();
			force.append(term.getName());
			force.append(term.getAcc());
			force.append(term.getCv());
			force.append(term.isObsolete());
		}
	}

	public synchronized Term getTermByName(String name) {
		if (name2term == null) {
			name2term = new HashMap<String, Term>();
		}
		Term term = name2term.get(name);
		boolean online = InternetChecker.getInstance().isConnectionPresent();
		if (term == null && online) {
			try {
				GOobjectFactory factory = getGOfactory();
				Session session = factory.startSession();
				term = (Term) factory.getTermByName(name, session);
				initTerm(term);
				session.close();
				factory.close();
			} catch (Exception e) {
				log.debug("Failed to connect to GODB to find term, will have to synthesize: " + name);
			}
		}
		if (term != null) {
			name2term.put(name, term);
		}

		return term;
	}

	public void indexTerm(Term term, String acc) {
		if (acc != null && !acc.equals("")) {
			if (accToTerm == null) {
				accToTerm = new HashMap<String, Term>();
			}
			accToTerm.put(acc, term);
			initTerm(term);
		}
	}

	public synchronized Term getTermByAlternateAcc(String acc) {
		if (altAccToTerm == null) {
			altAccToTerm = new HashMap<String, Term>();
		}
		Term term = altAccToTerm.get(acc);
		if (term == null) {
			GOobjectFactory factory = getGOfactory();
			Session session = factory.startSession();
			term = (Term) factory.getTermByAlternateAcc(acc, session);
			initTerm(term);
			session.close();
			factory.close();
			accToTerm.put(acc, term);
			accToTerm.put(term.getAcc(), term);
		}
		return term;
	}

	public synchronized GraphPath getGraphPath(Term term1, Term term2) {
		GOobjectFactory factory = getGOfactory();
		Session session = factory.startSession();
		GraphPath path = factory.getPath(term1, term2, session);
		session.close();
		factory.close();
		return path;
	}

	public Species getSpecies(GeneNode node) {
		Species species = null;
		List<String> names = node.getSpeciesList();
		if (taxonToSpecies == null) {
			taxonToSpecies = new HashMap<String, Species>();
		}
		if (names == null || names.size() == 0) {
			log.debug("No species name available for " + node.getNodeLabel() + " " + node.getSpeciesLabel());
			node.addSpeciesLabel("root");
			names = node.getSpeciesList();
		}
		List<String> taxa = new ArrayList<String>();
		for (String name : names) {
			String taxon_id = Preferences.inst().getTaxonID(name);
			if (taxon_id != null) {
				boolean duplicate = false;
				for (String t : taxa) {
					duplicate |= t.equals(taxon_id);
				}
				if (!duplicate) {
					taxa.add(taxon_id);
				}
			}
		}
		if (taxa.size() == 1) {
			String taxon_id = taxa.get(0);
			if (taxon_id != null && (taxon_id.length() > GOConstants.TAXON_PREFIX.length())) {
				species = taxonToSpecies.get(taxon_id);
				if (species == null) {
					species = createSpecies(taxon_id);
					taxonToSpecies.put(taxon_id, species);
				}
			} 
		} else if (taxa.size() > 0 ){
			log.debug("Multiple taxa for " + node + " " + names + " " + taxa);
		}
		return species;
	}

	public synchronized Species createSpecies(String taxon_id) {
		String name = Preferences.inst().getSpeciesName(taxon_id);
		String taxon_number = taxon_id.substring(GOConstants.TAXON_PREFIX.length());
		int taxon = new Integer(taxon_number).intValue();
		Species species = null;
		if (InternetChecker.getInstance().isConnectionPresent(true)) {
			GOobjectFactory factory = getGOfactory();
			Session session = factory.startSession();
			species = factory.getSpeciesByTaxa(taxon, session);
			session.close();
			factory.close();
		}
		if (species == null) {
			species = new Species();
			species.setNcbi_taxa_id(taxon);
			String gen_spe [] = name.split(" ");
			if (gen_spe.length == 1)
				species.setGenus(gen_spe[0]);
			else if (gen_spe.length == 2) {
				species.setGenus(gen_spe[0]);
				species.setSpecies(gen_spe[1]);
			} else {
				species.setGenus(gen_spe[0]);
				species.setSpecies(gen_spe[1] + " " + gen_spe[2]);					
			}
		}
		return species;
	}

	public String dbNameHack(String name) {
		/* The GO database is not using the suffix */
		String revision = name;
		if (name.equals("UniProtKB/Swiss-Prot")) {
			revision = "UniProtKB";
		}
		else if (name.equals("Uniprot")) {
			revision = "UniProtKB";			
		}
		else if (name.equals("ENTREZ")) {
			revision = "RefSeq";
		}
		else if (name.equals("ECOLI")) {
			revision = "EcoCyc";
		}
		else if (name.equals("GeneDB_Spombe")) {
			revision = "PomBase";
		}
		else if (name.equals("Gene")) {
			revision = "UniProtKB";
		}
		return revision;
	}

	public synchronized DB getOriginbyName(String name) {
		name = dbNameHack(name);
		if (nameToDB == null) {
			nameToDB = new HashMap<String, DB>();
		}
		DB db = nameToDB.get(name);
		if (db == null) {
			if (name.equals(GOConstants.PANTHER_DB)) {
				db = new DB();
				db.setName(name);
			}
			else if (name.equals(GOConstants.PAINT_AS_SOURCE) || name.equals(GOConstants.OLD_SOURCE)) {
				db = new DB();
				db.setName(GOConstants.PAINT_AS_SOURCE);
			}
			else if (InternetChecker.getInstance().isConnectionPresent()) {
				GOobjectFactory factory = getGOfactory();
				Session session = factory.startSession();
				db = (DB) factory.getDBByName(name, session);
				session.close();
				factory.close();
			} else {
				db = new DB();
				db.setName(name);
			}
			if (db != null) {
				nameToDB.put(name, db);
			}
		}
		return db;
	}
        
//        public boolean isPAINTAnnotation(edu.usc.ksom.pm.panther.paintCommon.Annotation annot) {
//            return annot.isPaint();
//        }
//
//	public boolean isPAINTAnnotation(Association assoc) {
//		String source = assoc.getSource_db().getName();
//		return (source.equals(GOConstants.PAINT_AS_SOURCE) || source.equals(GOConstants.OLD_SOURCE));
//	}

//	public GeneProduct createGeneProduct(GeneNode node) {
//		GeneProduct gene_product = new GeneProduct();
//		if (mod_list.contains(node.getDatabase())) {
//			//			if (!InternetChecker.getInstance().isConnectionPresent())
//			//				log.info("Have lost internet connection for " + node);
//			gene_product.setSymbol(node.getSeqName());
//		} else {
//			gene_product.setSymbol(node.getSeqName() != null ? node.getSeqName() : node.getDatabaseID());
//		}
//		gene_product.setDbxref(createNodeDBXref(node));
//		// properly this should be "SO:0000358", but in the GO database it isn't :-(
//		// Best to just leave SO type as null as this isn't working reliably
//		//		gene_product.setSO_type(getTermByAcc("protein"));
//		gene_product.setFull_name(node.getSeqName());
//		Species species = getSpecies(node);
//		gene_product.setSpecies(species);
//		initGP2Node(gene_product, node);
//		return gene_product;
//	}

//	public synchronized Association createAssociation(Term term, Set<GeneNode> withs, Integer date, boolean MRC, Set<Term> quals) {
//		Association assoc = new Association();
//		/* 
//		 * The association links a term to a gene product
//		 * and is made on a specific date
//		 */
//		assoc.setTerm(term);
//
//		if (date == null)
//			assoc.setDate();
//		else
//			assoc.setDate(date);
//
//		/*
//		 * This is the group who makes this association
//		 * Right now the only field being populated in the DB class is 
//		 * the name "PANTHER", but the other fields (full name, description, 
//		 * & URI information) is left blank
//		 */
//		assoc.setSource_db(getOriginbyName(GOConstants.PAINT_AS_SOURCE));
//		String code = MRC ? GOConstants.DESCENDANT_SEQUENCES_EC : GOConstants.ANCESTRAL_EVIDENCE_CODE;
//		assoc.addEvidence(createEvidence(term, withs, code));
//		assoc.setDirectMRC(MRC);
//		assoc.setDirectNot(false);
//		assoc.setQualifiers(quals);
//		return assoc;
//	}

//	private Evidence createEvidence(Term term, Set<GeneNode> withs, String code) {
//		Evidence evidence = new Evidence();
//		/*
//		 * Using inferred by sequence similarity as the default evidence code
//		 */
//		evidence.setCode(code);
//		/* 
//		 * Basically, this references states who made and is responsible for this 
//		 * association of term to gene_product
//		 */
//		evidence.setDbxref(getPAINTEvidenceDBXref());
//		/*
//		 * Add cross-references to the experimental evidence that were the basis for
//		 * this annotation
//		 */
//		for (GeneNode node : withs) {
//			GeneProduct gene_product = node.getGeneProduct();
//			evidence.addWith(gene_product.getDbxref());
//		}
//		return evidence;
//	}
//
//	/* This will return a set of Associations, with all the experimental evidence terms if the flag is true */
//	public Set<Association> getAssociations(GeneNode node, String go_aspect, boolean exp_only) {
//		GeneProduct gene_product = node.getGeneProduct();
//		Set<Association> full_set = gene_product != null ? gene_product.getAssociations() : null;
//		Set<Association> aspect_set = null;
//		if (full_set != null) {
//			for (Iterator<Association> it = full_set.iterator(); it.hasNext();) {
//				Association assoc = it.next();
//				String cv = assoc.getTerm().getCv();
//				if (cv.equals(go_aspect)) {
//					/*
//					 * isExperimental runs through all of the association's evidence to look for experimental evidence codes
//					 */
//					boolean add_it = (!exp_only || (exp_only && isExperimental(assoc)));
//					if (add_it) {
//						if (aspect_set == null) {
//							aspect_set = new HashSet<Association> ();
//						}
//						aspect_set.add(assoc);
//					}
//				}
//			}
//		}
//		return aspect_set;
//	}
//
//	/* This will return a set of Associations, with all the experimental evidence terms if the flag is true */
//	public boolean hasExperimentalAssoc(GeneNode node) {
//		boolean exp_associations = false;
//		GeneProduct gene_product = node.getGeneProduct();
//		Set<Association> full_set = gene_product != null ? gene_product.getAssociations() : null;
//		if (full_set != null) {
//			for (Iterator<Association> it = full_set.iterator(); it.hasNext() && !exp_associations;) {
//				Association assoc = it.next();
//				/*
//				 * isExperimental runs through all of the association's evidence to look for experimental evidence codes
//				 */
//				exp_associations = isExperimental(assoc);
//			}
//		}
//		return exp_associations;
//	}
        
//        public boolean isPainted(GeneNode node) {
//            boolean annotated = false;
//            Node n = node.getNode();
//            NodeVariableInfo nvi = n.getVariableInfo();
//            if (null == nvi) {
//                return false;
//            }
//            ArrayList<Annotation> annotationList = nvi.getGoAnnotationList();
//            if (null == annotationList) {
//                return false;
//            }
//            for (Annotation a: annotationList) {
//                com.sri.panther.paintCommon.Evidence e = a.getEvidence();
//                ArrayList<DBReference> dbRefList = e.getDbReferenceList();
//            }
//        }
//	public boolean isPainted(GeneNode node, boolean recurse) {
//		boolean annotated = false;
//                NodeVariableInfo nvi = node.getNode().getVariableInfo();
//                if (null == nvi) {
//                    return false;
//                }
//                ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
//                if (null == annotList) {
//                    return false;
//                }
//                for (edu.usc.ksom.pm.panther.paintCommon.Annotation annot: annotList) {
//                    annotated = GO_Util.inst().isPAINTAnnotation(annot);
//                    if (true == annotated) {
//                        break;
//                    }
//                }
//		if (recurse && !annotated) {
//			List<GeneNode> children = node.getChildren();
//			if (children != null) {
//				for (Iterator<GeneNode> node_it = children.iterator(); node_it.hasNext() && !annotated; ) {
//					GeneNode child = node_it.next();
//					annotated = isPainted(child, recurse);
//				}
//			}
//		}
//		return annotated;
//	}
//	public boolean isPaintedOld(GeneNode node, boolean recurse) {
//		boolean annotated = false;
//		GeneProduct gene_product = node.getGeneProduct();
//		if (gene_product != null) {
//			Set<Association> associations = gene_product.getAssociations();
//			for (Iterator<Association> assoc_it = associations.iterator(); assoc_it.hasNext()  && !annotated; ) {
//				Association assoc = assoc_it.next();
//				annotated = GO_Util.inst().isPAINTAnnotation(assoc);	
//			}
//		}
//		if (recurse && !annotated) {
//			List<GeneNode> children = node.getChildren();
//			if (children != null) {
//				for (Iterator<GeneNode> node_it = children.iterator(); node_it.hasNext() && !annotated; ) {
//					GeneNode child = node_it.next();
//					annotated = isPainted(child, recurse);
//				}
//			}
//		}
//		return annotated;
//	}

	public DBXref getPAINTEvidenceDBXref() {
		if (paint_dbxref == null) {
			paint_dbxref = new DBXref();
			/*
			 * The is the reference describing how this annotation was made
			 * there is only one for 
			 */
			paint_dbxref.setDb_name(GOConstants.PAINT_REF);
			/* setting the key_type, but not sure what it should really be */
			paint_dbxref.setKeytype("Personal Communication");

			/* 
			 * Augment the name with a description of the database and tool
			 */
			paint_dbxref.setDescription("PAINT protein family curation");
			/*
			 * This is not a dbxref for the definition of a term 
			 * It is a dbxref to the group doing the annotation
			 */
			paint_dbxref.setFor_definition(false);
		}
		paint_dbxref.setAccession(getPaintEvidenceAcc());
		return paint_dbxref;
	}

	public String getPaintEvidenceAcc() {
		/*
		 * Use the PANTHER family name as the reference
		 * Update every time in case a new book is loaded
		 */
		Family family = PaintManager.inst().getFamily();

		if (family != null) {
			String pthr_id = family.getFamilyID();
			int acc = Integer.valueOf(pthr_id.substring("PTHR".length())).intValue();
			String paint_id = String.format("%1$07d", acc);
			return paint_id;
		} else
			return "";
	}

	public DBXref createNodeDBXref(GeneNode node) {
		DBXref dbxref = new DBXref();
		dbxref.setDb_name(node.getDatabase());
		dbxref.setAccession(node.getDatabaseID());
		/* setting the key_type, but not sure what it should really be */
		dbxref.setKeytype("id");
		/* 
		 * Augment the name with a description of the database and tool
		 */
		dbxref.setDescription(node.getDescription());
		/*
		 * This is not a dbxref for the definition of a term 
		 * It is a dbxref to the group doing the annotation
		 */
		dbxref.setFor_definition(false);
		return dbxref;
	}

	public String getWithText(Set<DBXref> withs) {
		String prefix = null;
		String text = "";

		try {
			for (Iterator<DBXref> it = withs.iterator(); it.hasNext();) {
				DBXref xref = it.next();
				if (prefix == null) {
					text += xref.getDb_name() + ":" + xref.getAccession();
					prefix = ", ";
				} else {
					text += prefix + xref.getDb_name() + ":" + xref.getAccession();
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return text;
	}

	public Term getTerm(LinkedObject obo_term, boolean makeitup) {
		if (term2term == null)
			term2term = new HashMap<LinkedObject, Term> ();
		if (name2term == null)
			name2term = new HashMap<String, Term> ();
		Term term = term2term.get(obo_term);
		if (term == null) {
			if (makeitup) {
				term = createTerm(obo_term);
			} else {
				term = getTermByAcc(obo_term.getID());
			}
			term2term.put(obo_term, term);
			name2term.put(term.getName(), term);
		}
		return term;
	}

	/**
	 * Without a doubt this is one of the ugliest pieces of code it's ever been necessary to write
	 * Basically recapitulating the entire GO term tree ...
	 * @param obo_term
	 * @return
	 */
	private Term createTerm(LinkedObject obo_term) {
		// Basically clone the obo term into a go database version
		Term term = new Term();
		term.setAcc(obo_term.getID());
		term.setCv(obo_term.getNamespace().toString());
		term.setName(obo_term.getName());
		term.setIs_obsolete(new Integer(0));
		term.setDefinition("");
		Collection<Link> parent_links = obo_term.getParents();
		term.setIs_root((parent_links == null ? new Integer(0) : new Integer(1)));
		return term;
	}

	public void putTerm(LinkedObject obo_term, Term term) {
		if (term2term == null)
			term2term = new HashMap<LinkedObject, Term> ();
		term2term.put(obo_term, term);		
	}

	private boolean hasQualifier(Association assoc, String qual) {
		Set<Term> qualifiers = assoc.getQualifiers();
		boolean has_qual = false;
		try {
			if (qualifiers != null) {
				for (Iterator<Term> it = qualifiers.iterator(); it.hasNext() && !has_qual;) {
					Term term = it.next();
					has_qual = term.getName().equals(qual);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return has_qual;
	}

	public boolean contributesTo(Association assoc) {
		return hasQualifier(assoc, GOConstants.CONTRIBUTES);
	}

	public boolean colocalizes(Association assoc) {
		return hasQualifier(assoc, GOConstants.COLOCATES);
	}

	/*
	 * Check each association to the gene product passed as a parameter
	 * If that association is either directly to that term
	 * or the existing association is to a more specific term
	 * then return that association
	 */
//	public Association isAnnotatedToTerm(GeneNode node, Term term) {
//		Association annotated_with_term = null;
//		GeneProduct gene_product = node.getGeneProduct();
//		if (gene_product != null && term != null) {
//			LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
//			String term_acc = term.getAcc();
//			LinkedObject possible_parental = (LinkedObject) GO_Util.inst().getObject(go_root, term_acc);
//			Set<Association> associations = node.getAssociations();
//			for (Iterator<Association> assoc_it = associations.iterator(); assoc_it.hasNext() && annotated_with_term == null;) {
//				Association assoc = assoc_it.next();
//				Term assoc_term = assoc.getTerm();
//				LinkedObject possible_child = (LinkedObject) GO_Util.inst().getObject(go_root, assoc_term.getAcc());
//				if (assoc_term.getAcc().equals(term_acc) 
//						|| (TermUtil.isAncestor(possible_child, possible_parental, go_root, null) && !assoc.isDirectNot())) {
//					annotated_with_term = assoc;
//				}
//			}
//		}
//		return annotated_with_term;
//	}
//
//	public void getRelatedAssociationsToTerm(GeneNode node, Term term, List<Association> self,
//			List<Association> broader_terms, List<Association> narrower_terms, boolean experimentalOnly) {
//		GeneProduct gene_product = node.getGeneProduct();
//		if (gene_product != null) {
//			LinkDatabase go_root = PaintManager.inst().getGoRoot().getLinkDatabase();
//			String term_acc = term.getAcc();
//			LinkedObject term4column = (LinkedObject) GO_Util.inst().getObject(go_root, term_acc);
//			Set<Association> associations = node.getAssociations();
//			for (Association assoc : associations) {
//				Term assoc_term = assoc.getTerm();
//				LinkedObject annotated2term = (LinkedObject) GO_Util.inst().getObject(go_root, assoc_term.getAcc());
//				if (term.equals(assoc_term)) {
//					self.add(assoc);
//				}
//				/*
//				 * Is term4column a parent term of annotated2term?
//				 */
//				else if (TermUtil.isAncestor(annotated2term, term4column)) {
//					if (experimentalOnly) {
//						if (isExperimental(assoc)) {
//							narrower_terms.add(assoc);
//						}
//					}
//					else {
//						narrower_terms.add(assoc);
//					}
//				}
//				/*
//				 * Conversely is term4column a child term of annotated2term
//				 */
//				else if (TermUtil.isAncestor(term4column, annotated2term)) {
//					if (experimentalOnly) {
//						if (isExperimental(assoc)) {
//							broader_terms.add(assoc);
//						}
//					}
//					else {
//						broader_terms.add(assoc);
//					}
//				}
//			}
//		}
//	}
//
//	public void initGP2Node(GeneProduct gp, GeneNode node) {
//		if (gp2node == null) {
//			gp2node = new HashMap<GeneProduct, GeneNode> ();
//		}
//		if (!gp2node.containsKey(gp))
//			gp2node.put(gp, node);
//	}

	public IdentifiedObject getObject(LinkDatabase go_ont, String id) {
		LinkedObject ont_term = (LinkedObject) go_ont.getObject(id);
		if (ont_term == null) {
			if (alternateIdObjects == null) {
				alternateIdObjects = new HashMap<String, LinkedObject>();
			}
			ont_term = alternateIdObjects.get(id);
			if (ont_term == null) {
				for (IdentifiedObject o : go_ont.getObjects()) {
					if (o instanceof MultiIDObject && ((MultiIDObject)o).getSecondaryIDs().contains(id)) {
						ont_term = (LinkedObject)o;
						alternateIdObjects.put(id, ont_term);
						break;
					}
				}
			}
		}
		return ont_term;
	}

	public void clearCache() {
		if (alternateIdObjects == null) {
			alternateIdObjects = new HashMap<String, LinkedObject>();
		} else {
			alternateIdObjects.clear();
		}
		if (nameToDB == null) {
			nameToDB = new HashMap<String, DB>();
		} else {
			nameToDB.clear();
		}
		if (taxonToSpecies == null) {
			taxonToSpecies = new HashMap<String, Species>();
		} else {
			taxonToSpecies.clear();
		}
		if (accToTerm == null) {
			accToTerm = new HashMap<String, Term>();
		} else {
			accToTerm.clear();
		}
		if (altAccToTerm == null) {
			altAccToTerm = new HashMap<String, Term>();
		} else {
			altAccToTerm.clear();
		}

	}
}
