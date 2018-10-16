///* 
// * 
// * Copyright (c) 2010, Regents of the University of California 
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
// * 
// * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
// * Neither the name of the Lawrence Berkeley National Lab nor the names of its contributors may be used to endorse 
// * or promote products derived from this software without specific prior written permission.
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
// * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
// * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
// * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
// * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// * 
// */
//package org.paint.dataadapter;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.TimeZone;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import javax.swing.JOptionPane;
//
//import org.apache.log4j.Logger;
//import org.bbop.framework.GUIManager;
//import org.geneontology.db.model.Association;
//import org.geneontology.db.model.DB;
//import org.geneontology.db.model.DBXref;
//import org.geneontology.db.model.Evidence;
//import org.geneontology.db.model.GeneProduct;
//import org.geneontology.db.model.Species;
//import org.geneontology.db.model.Term;
//import org.paint.datamodel.GeneNode;
//import org.paint.go.GOConstants;
//import org.paint.go.GO_Util;
//import org.paint.gui.AspectSelector;
//import org.paint.gui.DirtyIndicator;
//import org.paint.gui.event.EventManager;
//import org.paint.gui.event.ProgressEvent;
//import org.paint.gui.evidence.PaintAction;
//import org.paint.gui.evidence.WithEvidence;
//import org.paint.gui.familytree.TreePanel;
//import org.paint.main.PaintManager;
//import org.paint.util.SeqMapper;
//
//import com.sri.panther.paintCommon.Constant;
//import com.sri.panther.paintCommon.util.FileUtils;
//
//public class GafAdapter {
//
//	private static final String GAF_COMMENT = "!";
//	private static final String GAF_VERSION = GAF_COMMENT + "gaf-version:";
//	protected static Logger log = Logger.getLogger(GafAdapter.class);
//
//	/**
//	 * Method declaration
//	 *
//	 * @param File gaf_file
//	 * @throws IOException 
//	 *
//	 * @see
//	 */
//	private static boolean importGAF(File gaf_file, boolean curated) throws IOException {
//		StringBuffer errors = new StringBuffer();
//		StringBuffer missing = new StringBuffer();
//		StringBuffer unsupported = new StringBuffer();
//		StringBuffer obsolete = new StringBuffer();
//		String message = curated ? "Initializing PAINT annotations" : "Initializing experimental annotations from local file";
//		EventManager.inst().fireProgressEvent(new ProgressEvent(GafAdapter.class, message, 0, ProgressEvent.Status.START));
//
//		if (null != gaf_file && gaf_file.isFile()) {
//			String file_name = gaf_file.getCanonicalPath();
//			if (FileUtils.validPath(file_name)) {
//				// Read contents of GO annotation file
//				PaintManager.inst().setCurrentDirectory(gaf_file);
//				String[]  gaf_contents = FileUtils.readFile(file_name);
//				double gafVersion = 0;
//
//				HashSet<GeneNode> pruned_list = new HashSet<GeneNode>();
//				HashMap<GeneNode, Set<String []>> negate_list = new HashMap<GeneNode, Set<String []>>();
//
//				int expectedNumCols = 15; // default to GAF version 1.0
//				if ((null != gaf_contents && gaf_contents.length > 0)) {
//					for (int i = 0; i < gaf_contents.length; i++) {
//						String row = gaf_contents[i];
//						if (row.startsWith(GAF_COMMENT)) {
//							if (isFormatDeclaration(row)) {
//								gafVersion = parseGafVersion(row);
//								if (gafVersion == 2.0) {
//									expectedNumCols = 17;
//								}
//							}
//							continue;
//						}
//						/**
//						 * Columns are as follows
//						 * 1  	DB  	 required  	 SGD
//						 * 2 	DB_Object_ID 	required 	S000000296
//						 * 3 	DB_Object_Symbol 	required 	PHO3
//						 * 4 	Qualifier 	optional 	NOT
//						 * 5 	GO ID 	required 	GO:0003993
//						 * 6 	DB:Reference (|DB:Reference) 	required 	SGD_REF:S000047763|PMID:2676709
//						 * 7 	Evidence code 	required 	IMP
//						 * 8 	With (or) From 	optional 	GO:0000346
//						 * 9 	Aspect 	required 	F
//						 * 10 	DB_Object_Name 	optional 	acid phosphatase
//						 * 11 	DB_Object_Synonym (|Synonym) 	optional 	YBR092C
//						 * 12 	DB_Object_Type 	required 	gene
//						 * 13 	taxon(|taxon) 	required 	taxon:4932
//						 * 14 	Date 	required 	20010118
//						 * 15 	Assigned_by 	required 	SGD
//						 * 16	Annotation_Extension	optional	part_of(CL:0000576) 
//						 * 17	Gene_Product_Form_ID	optional	UniProtKB:P12345-2
//						 */
//						String [] columns = row.split("\\t", -1);
//						if (columns.length != expectedNumCols) {
//							errors.append("Got invalid number of columns for row (expected " + expectedNumCols + ", got " +
//									columns.length + "): " + row + "\n"); 
//							continue;
//						}
//						/*
//						 * Next step is to find the corresponding gene node
//						 */
//
//						List<GeneNode> seqs = locateNode(columns[0], columns[1], columns[2], errors);
//						if (seqs == null || (seqs != null && seqs.size() == 0)) {
//							/*
//							 * If a node can't be found it is likely not a big deal
//							 * The only really important nodes are the ancestors from which everything else is propagated.
//							 */
//							String acc = lastResort(columns);
//							if (acc != null && acc.length() > 0) {
//								seqs = locateNode("UniProtKB", acc, columns[2], errors);
//							}
//							if (seqs == null || (seqs != null && seqs.size() == 0)) {
//								if (columns[0].equals(GOConstants.PANTHER_DB)) {
//									addMissing (missing, columns[0] + ":" + columns[1] + "\n");
//								}
//								continue;
//							}
//						}
//						for (GeneNode node : seqs) {
//							if (curated) {
//								parseAnnotations(node, columns, pruned_list, negate_list, unsupported, obsolete, errors);
//							} else {
//								parseExperimental(node, columns, pruned_list, errors);
//							}
//						}
//						String tmpMessage = message + " (" + (i + 1) + " / " + gaf_contents.length + ")";
//						int percent = (int)(((double)(i + 1) / gaf_contents.length) * 100);
//						EventManager.inst().fireProgressEvent(new ProgressEvent(GafAdapter.class, tmpMessage, percent, ProgressEvent.Status.RUNNING));
//					} // end for loop going through gaf file contents
//					for (GeneNode node : pruned_list) {
//						node.setPrune(true);
//						EventManager.inst().fireProgressEvent(new ProgressEvent(GafAdapter.class, message + pruned_list.size() + " Pruning ", 99, ProgressEvent.Status.RUNNING));
//						PaintAction.inst().pruneBranch(node, true);
//					}
//					if (!negate_list.isEmpty()) {
//						EventManager.inst().fireProgressEvent(new ProgressEvent(GafAdapter.class, message + " - " + negate_list.size() + " NOTs ", 99, ProgressEvent.Status.RUNNING));
//						applyNots(negate_list, errors);
//					}
//				} else {
//					errors.append(file_name + " is empty");
//				}
//			} else {
//				errors.append(file_name + " cannot be found");
//			}
//		} else {
//			errors.append(gaf_file + " is not a readable file.");
//		}
//
//		if (missing.length() > 0) {
////			String warning = "WARNING: Tree updated!\nThe following nodes are no longer in this tree:\n\n" + missing.toString();
////			EvidencePanel.inst().addWarning(warning);
//		}
//
//		if (unsupported.length() > 0) {
////			String warning = "WARNING: The following annotations have been removed:\n\n" + unsupported.toString();
////			EvidencePanel.inst().addWarning(warning);
//		}
//
//		if (obsolete.length() > 0) {
////			String warning = "WARNING: The following terms are obsolete and annotations using them have been removed:\n\n" + obsolete.toString();
////			EvidencePanel.inst().addWarning(warning);
//		}
//
//		boolean success = errors.length() == 0;
//		if (!success) {
//			JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), errors.toString(), "GAF errors", JOptionPane.ERROR_MESSAGE);
//			EventManager.inst().fireProgressEvent(new ProgressEvent(GafAdapter.class, null, 100, ProgressEvent.Status.FAIL));
//		}
//		return success;
//	}
//
//	private static List<GeneNode> locateNode(String name, String db_id, String symbol, StringBuffer errors) {
//		List<GeneNode> seqs = null;
//		String db = GO_Util.inst().dbNameHack(name);
//		PaintManager manager = PaintManager.inst();
//		if (db_id != null && db_id.length() > 0) {
//			GeneNode node = manager.getGeneByGP(db, db_id);
//			if (node == null) {
//				node = manager.getGeneByPTNId(db_id);
//				if (node == null)
//					node = manager.getGeneByDbId(db, db_id);
//				if (node == null)
//					seqs = manager.getGenesBySeqId(db, db_id);
//				if (node == null && seqs == null && (symbol != null && symbol.length() > 0)) {
//					node = manager.getGeneByDbId(db, symbol);
//				}
//			}
//			if (node != null) {
//				if (seqs == null)
//					seqs = new ArrayList<GeneNode>();
//				seqs.add(node);
//			}
//		}
//		return seqs;
//	}
//
//	private static DBXref parseDBXref(String xref_string) {
//		String [] db_acc = xref_string.split(":");
//		DBXref dbxref = new DBXref();
//		String db_name = db_acc[0];
//		DB db = GO_Util.inst().getOriginbyName(db_name);
//		if ( db != null) {
//			dbxref.setDb_name(db.getName());
//			if (db_acc.length > 2) {
//				dbxref.setAccession(db_acc[1] + ":" + db_acc[2]);	
//			} else {
//				dbxref.setAccession(db_acc[1]);
//			}
//			return dbxref;
//		}
//		else {
//			return null;
//		}
//	}
//
//
//	private static void parseAnnotations (GeneNode node, 
//			String [] columns, 
//			HashSet<GeneNode> pruned_list, 
//			HashMap<GeneNode, Set<String []>> negate_list, 
//			StringBuffer unsupported,
//			StringBuffer obsolete,
//			StringBuffer errors) {
////		boolean blocked = isBlocked(columns[3]);
////		if (blocked) {
////			pruned_list.add(node);
////		}
////		/*
////		 * Ignore the rows (from older GAFs) that are for descendant nodes 
////		 * in the tree. These will be propagated from the ancestral nodes
////		 * that were directly annotated
////		 */
////		else if (!columns[6].equals(GOConstants.ANCESTRAL_EVIDENCE_CODE)) {
////			boolean negation = isNot(columns[3]);
////			if (negation) {
////				Set<String []> row_list = negate_list.get(node);
////				if (row_list == null) {
////					row_list = new HashSet<String []>();
////					negate_list.put(node, row_list);
////				}
////				row_list.add(columns);
////			} 
////			else {
////				Set<Term> qual_list = parseQualifiers(columns[3]);
////				Term term = GO_Util.inst().getTermByAcc(columns[4]);
////				if (term == null) {
////					term = GO_Util.inst().getTermByAlternateAcc(columns[4]);
////					if (term == null) {
////						errors.append("Unable to fetch term: " + columns[4]);
////					}
////					return;
////				}
////				Integer date = Integer.decode(columns[13]);
////				if (term.isObsolete()) {
////					obsolete.append(term.getName() + " no longer used to annotate " + node.getNodeLabel() + "\n");
////				} else {
////					if (GO_Util.inst().isAnnotatedToTerm(node, term) == null) {
////						String invalid = PaintAction.inst().isValidTerm(term, node);
////						if (invalid == null) {
////							PaintAction.inst().propagateAssociation(node, term, date, qual_list);
////						} else {
////							unsupported.append(node.getNodeLabel() + " to " + term.getName() + " invalid - " + invalid + '\n');
////						}
////					}
////				}
////			}
////		}
//	}
//
//	private static void parseExperimental (GeneNode node, String [] columns, HashSet<GeneNode> blocked_list, StringBuffer errors) {
//		boolean blocked = isBlocked(columns[3]);
//		if (blocked) {
//			if (blocked_list == null)
//				blocked_list = new HashSet<GeneNode>();
//			blocked_list.add(node);
//		}
//
//		GeneProduct gp = node.getGeneProduct();
//		if (gp == null) {
//			gp = GO_Util.inst().createGeneProduct(node);
//			node.setGeneProduct(gp);
//			//			node.setDatabase(columns[0]);
//			//			node.setDatabaseID(columns[1]);
//			gp.setSymbol(columns[2]);
//			gp.setFull_name(columns[9]);
//			String [] syns = columns[10].split("\\|");
//
//			Set<String> all_syns = new HashSet<String>();
//			for (String syn : syns) {
//				all_syns.add(syn);
//			}
//			gp.setSynonyms(all_syns);
//			gp.setDbxref(GO_Util.inst().createNodeDBXref(node));
//			gp.setSpecies(GO_Util.inst().createSpecies(columns[12]));
//		}
//		/** alternate labels for the gene or gene product (column 11 in the gene-association file) */
//		boolean negation = isNot(columns[3]);
//		Set<Term> qual_list = parseQualifiers(columns[3]);
//
//		Term term = GO_Util.inst().getTermByAcc(columns[4]);
//		if (term == null) {
//			term = GO_Util.inst().getTermByAlternateAcc(columns[4]);
//			if (term == null) {
//				errors.append("Unable to fetch term: " + columns[4]);
//			}
//			return;
//		}
//		//				* 6 	DB:Reference (|DB:Reference) 	required 	SGD_REF:S000047763|PMID:2676709
//		//				* 7 	Evidence code 	required 	IMP
//		//				* 8 	With (or) From 	optional 	GO:0000346
//
//		//		source_db, Set<Evidence> evidence, Set<Term> qualifiers;
//
//		Association assoc = new Association();
//
//		/* 
//		 * The association links a term to a gene product
//		 * and is made on a specific date
//		 */
//		assoc.setTerm(term);
//		assoc.setNot(negation);
//		if (qual_list != null) {
//			assoc.setQualifiers(qual_list);
//		}
//		Integer date = Integer.decode(columns[13]);
//		if (date == null)
//			assoc.setDate();
//		else
//			assoc.setDate(date);
//
//		assoc.setDirectMRC(false);
//		assoc.setDirectNot(negation);
//
//		Evidence evidence = new Evidence();
//		evidence.setCode(columns[6]);
//		/* 
//		 * Basically, this references states who made and is responsible for this 
//		 * association of term to gene_product
//		 */
//		evidence.setDbxref(parseDBXref(columns[5]));
//		/*
//		 * Using inferred by sequence similarity as the default evidence code
//		 */
//		String withs = columns[7];
//		Set<DBXref> withset = new HashSet<DBXref>();
//		if (withs != null && !withs.equals("")) {
//			String [] with_string = withs.split("\\|");
//			for (int i = 0; i < with_string.length; i++) {
//				DBXref with_ref = parseDBXref(with_string[i]);
//				if (with_ref != null) {
//					withset.add(with_ref);
//				} 
//				else {
//					errors.append("Could not parse dbxref from with column \"" + with_string[i] + "\"\n");
//				}
//			}
//		}
//		evidence.setWiths(withset);
//		evidence.setAssociation(assoc);
//		assoc.addEvidence(evidence);
//
//		String aspect = columns[8];
//		if (aspect.equals("F")) {
//			node.setMolecularFunctionCount(node.getMolecularFunctionCount() + 1);
//		} else if (aspect.equals("P")) {
//			node.setBiologicalProcessCount(node.getBiologicalProcessCount() + 1);
//		} else if (aspect.equals("C")) {
//			node.setCellularComponentCount(node.getCellularComponentCount() + 1);
//		}
//
//		/*
//		 * This is the group who makes this association
//		 * Right now the only field being populated in the DB class is 
//		 * the name "PANTHER", but the other fields (full name, description, 
//		 * & URI information) is left blank
//		 */
//		assoc.setSource_db(GO_Util.inst().getOriginbyName(columns[14]));
//		gp.addAssociation(assoc);
//	}
//
//	private static void addMissing (StringBuffer missing, String missing_seq) {
//		if (missing.indexOf(missing_seq) < 0)
//			missing.append(missing_seq);
//	}
//
//	/**
//	 * Method declaration
//	 *
//	 * @param File gaf_file
//	 * @throws IOException 
//	 *
//	 * @see
//	 */
//	private static boolean exportGAF(File gaf_file, boolean curated) {
//		boolean success = true;
//		try{
//			String file_name = gaf_file.getCanonicalPath();
//			// Read contents of GO annotation file
//			PaintManager.inst().setCurrentDirectory(gaf_file);
//			/* 
//			 * we have to traverse the entire tree to do this
//			 */
//			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(file_name));
//			bufWriter.write(GAF_COMMENT + "gaf-version: 2.0\n");
//			TreePanel tree = PaintManager.inst().getTree();
//			exportAssociations(tree.getRoot(), bufWriter, curated);
//
//			if (null != bufWriter) {
//				bufWriter.close();
//			}
//		}
//		catch (IOException ie){
//			log.error("Exception " + ie.getMessage() + " returned while attempting to export GAF file " + gaf_file.getName());
//			success = false;
//		}
//		return success;
//
//	}
//
//	private static void exportAssociations(GeneNode node, BufferedWriter bufWriter, boolean curated) throws IOException {
//		if (node.isPruned()) {
//			/* Write out one row to record the pruned branch */
//			if (curated)
//				exportStump(node, bufWriter);
//			return;
//		}
//		GeneProduct gene_product = node.getGeneProduct();
//		if (gene_product != null) {
//			Set<Association> all_assocs = new HashSet<Association> ();
//			all_assocs.addAll(gene_product.getAssociations());
//			for (Iterator<Association> assoc_it = all_assocs.iterator(); assoc_it.hasNext();) {
//				StringBuffer row = new StringBuffer();
//				Association assoc = assoc_it.next();
//				/**
//				 * Only save those associations made within the context of PAINT
//				 */
//				boolean write_row;
//				if (curated) {
//					write_row = GO_Util.inst().isPAINTAnnotation(assoc);
//				} else {
//					write_row = GO_Util.inst().isExperimental(assoc);
//				}
//
//				/**
//				 * And to save some space, only those 
//				 * direct associations to the ancestral nodes
//				 * Or inherited associations to the leaves
//				 */
//				write_row &= (assoc.isMRC() || node.isLeaf() || assoc.isDirectNot());
//				if (write_row) {
//					/**
//					 * Columns as as follows
//					 * 1  	DB  	 required  	 SGD
//					 * 2 	DB_Object_ID 	required 	S000000296
//					 * 3 	DB_Object_Symbol 	required 	PHO3
//					 * 4 	Qualifier 	optional 	NOT
//					 * 5 	GO ID 	required 	GO:0003993
//					 * 6 	DB:Reference (|DB:Reference) 	required 	SGD_REF:S000047763|PMID:2676709
//					 * 7 	Evidence code 	required 	IMP
//					 * 8 	With (or) From 	optional 	GO:0000346
//					 * 9 	Aspect 	required 	F
//					 * 10 	DB_Object_Name 	optional 	acid phosphatase
//					 * 11 	DB_Object_Synonym (|Synonym) 	optional 	YBR092C
//					 * 12 	DB_Object_Type 	required 	gene
//					 * 13 	taxon(|taxon) 	required 	taxon:4932
//					 * 14 	Date 	required 	20010118
//					 * 15 	Assigned_by 	required 	SGD 
//					 */
//
//					writeDBcolumns(node, row);
//					/*
//					 * The Qualifiers in column 4
//					 */
//					row.append(getQualifiers(assoc));
//					row.append(assoc.getTerm().getAcc() + "\t"); // col 5 GO_ID
//
//					Set<Evidence> evi_list = assoc.getEvidence();
//					// Assuming this will only enter the loop once, but lets check to be sure
//					int count = evi_list.size();
//					if (count != 1 && curated) {
//						log.info(node.getDatabase() + ":" + node.getDatabaseID() + " has " + count + " pieces of evidence!");
//						continue;
//					}
//					// it's safe so onwards
//					Evidence evidence = null;
//					for (Iterator<Evidence> evi_it = evi_list.iterator(); evi_it.hasNext(); ) {
//						evidence = evi_it.next();
//						StringBuffer evi_row = writeRow(node, gene_product, new StringBuffer(row), assoc, evidence, curated);
//						if (evi_row != null)
//							bufWriter.write(evi_row.toString() + "\n");
//					}
//				}
//			}
//		}
//		/*
//		 * And all the children as well, if this branch hasn't been pruned
//		 */
//		List<GeneNode> children = node.getChildren();
//		if (children != null) {
//			for (Iterator<GeneNode> node_it = children.iterator(); node_it.hasNext(); ) {
//				GeneNode child = node_it.next();
//				exportAssociations(child, bufWriter, curated);
//			}
//		}
//	}
//
//	private static void exportStump(GeneNode node, BufferedWriter bufWriter) throws IOException {
//		StringBuffer row = new StringBuffer();
//		writeDBcolumns(node, row);
//		/*
//		 * The Qualifiers in column 4
//		 */
//		row.append("CUT\t");
//		row.append("\t"); // col 5 GO_ID
//
//		DBXref xref = GO_Util.inst().getPAINTEvidenceDBXref();
//		row.append(xref.getDb_name() + ":" + xref.getAccession() + "\t"); // col 6 evidence reference
//
//		// it's safe so onwards
//		row.append("\t"); // col 7 evidence code
//		row.append("\t"); // col 8 withs
//		row.append("\t"); // col 9 Aspect
//		row.append("\t"); /* skipping full name column 10 */
//		row.append("\t"); // col 11 synonyms
//		row.append("protein\t"); // col 12 SO type		
//		row.append("\t"); // col 13 synonyms
//		long timestamp = System.currentTimeMillis();
//		/* Date appears to be fixed?? */
//		Date when = new Date(timestamp);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//		sdf.setTimeZone(TimeZone.getDefault()); // local time
//		String date_str = sdf.format(when);
//		row.append(date_str + "\t"); // col 14 date
//		row.append(GOConstants.PAINT_AS_SOURCE + "\t"); // col 15 who made the annotation
//
//		// column 16 (annotation extension) -- empty for now
//		row.append("" + "\t");
//
//		// column 17 (gene product form id) -- empty for now
//		row.append("");
//
//		bufWriter.write(row.toString() + "\n");		
//	}
//
//	private static void writeDBcolumns(GeneNode node, StringBuffer row) {
//		GeneProduct gene_product = node.getGeneProduct();
//		String DB_name = "";
//		String DB_Object_ID = "";
//		String symbol = "";
//		if (gene_product != null) {
//			DB_name = gene_product.getDbxref().getDb_name();
//			if (DB_name.equals(GOConstants.PANTHER_DB)) {
//				DB_Object_ID = node.getPersistantNodeID();
//				symbol = DB_Object_ID;
//			} 
//			else {
//				DB_Object_ID = gene_product.getDbxref().getAccession(); // col 2 DB_Object_ID
//				if (gene_product.getSymbol().length() > 0)					
//					symbol = gene_product.getSymbol(); // col 3 DB_Object_Symbol
//				else {
//					symbol = node.getSeqId();
//					log.info("No symbol for " + node.getSeqName() + " (" + DB_name + ":" + DB_Object_ID + ")");
//				}
//			}
//		} 
//		else {
//			DB_name = node.getDatabase();
//			if (DB_name.equals(GOConstants.PANTHER_DB)) {
//				DB_Object_ID = node.getPersistantNodeID();
//				symbol = DB_Object_ID;
//			}
//			else {
//				DB_Object_ID = node.getDatabaseID();
//				symbol = node.getSeqId();
//			}
//		}
//		row.append(DB_name + "\t");
//		row.append(DB_Object_ID + "\t"); // col 2 DB_Object_ID
//		row.append(symbol + "\t");
//	}
//
//	private static void writeWiths (Set<DBXref> withs, StringBuffer row) {
//		String prefix = "";
//		for (Iterator<DBXref> it = withs.iterator(); it.hasNext();) {
//			DBXref dbxref = it.next();
//			if (dbxref.getDb_name().equals(GOConstants.PANTHER_DB)) {
//				GeneNode node = PaintManager.inst().getGeneByDbId(dbxref.getDb_name(), dbxref.getAccession());
//				if (node == null) {
//					log.error("Crap: could not find node for " + dbxref.getDb_name() + ':' + dbxref.getAccession());
//				} else {
//					row.append(prefix + dbxref.getDb_name() + ":" + node.getPersistantNodeID()); // col 8 With
//				}
//			}
//			else
//				row.append(prefix + dbxref.getDb_name() + ":" + dbxref.getAccession()); // col 8 With
//			prefix = Constant.STR_PIPE;
//		}
//	}
//
//	private static String getAspect(Term term) {
//		String cv = term.getCv();
//		String aspect = AspectSelector.aspects.get(cv);
//		return aspect;
//	}
//
//	public static boolean exportAnnotations(String path) {
//		boolean success = true; 
//		success = exportGAF(FileUtil.inst().getFile(path, "gaf"), true);
//		success &= exportGAF(FileUtil.inst().getFile(path, "exp"), false);			
//		DirtyIndicator.inst().dirtyGenes(false);
//		return success;
//	}
//
//	public static boolean importAnnotations(String path) {
//		boolean imported = false;
//		if (path != null) {
//			File f = FileUtil.inst().getFile(path, "gaf");
//			try {
//				imported = importGAF(f, true);
//			}
//			catch (IOException ie){
//				ie.printStackTrace();
//				log.error("IO exception while opening file");
//			}
//		}
//		return imported;
//	}
//
//	public static boolean importExpAnnotations(String path) {
//		boolean imported = false;
//		if (path != null) {
//			File f = FileUtil.inst().getFile(path, "exp");
//			try {
//				imported = importGAF(f, false);
//			}
//			catch (IOException ie){
//				ie.printStackTrace();
//				log.error("IO exception while opening file");
//			}
//		}
//		return imported;
//	}
//
//	private static boolean isFormatDeclaration(String line) {
//		return line.startsWith(GAF_VERSION);
//	}
//
//	private static double parseGafVersion(String line) {
//		Pattern p = Pattern.compile(GAF_VERSION + "\\s*(\\d+\\.*\\d+)");
//		Matcher m = p.matcher(line);
//		if (m.matches()) {
//			return Double.parseDouble(m.group(1));
//		}
//		return 0;
//	}
//
//	private static void applyNots(Map<GeneNode, Set<String []>> negate_list, StringBuffer errors) {
//		Map<GeneNode, Set<String []>> toBeSkipped = new HashMap<GeneNode, Set<String []>>();
//		for (GeneNode node : negate_list.keySet()) {
//			Set<String []> row_list = negate_list.get(node);
//			Set<String []> skipList = new HashSet<String []>();
//			toBeSkipped.put(node, skipList);
//			for (String [] columns : row_list) {
//				String withs = columns[7];
//				if (withs != null && !withs.equals("")) {
//					String [] with_string = withs.split("\\|");
//					for (int i = 0; i < with_string.length; i++) {
//						DBXref with_ref = parseDBXref(with_string[i]);
//						if (with_ref != null) {
//							GeneNode with_node = PaintManager.inst().getGeneByPTNId(with_ref.getAccession());
//							if (with_node == null) {
//								with_node = PaintManager.inst().getGeneByDbId(with_ref.getDb_name(), with_ref.getAccession());
//							}
//							if (with_node == null) {
//								List<GeneNode> seqs = PaintManager.inst().getGenesBySeqId(with_ref.getDb_name(), with_ref.getAccession());
//								if (seqs != null && seqs.size() > 0) {
//									with_node = seqs.get(0);
//									if (seqs.size() > 1) {
//										log.error("Should handle double seqs better for " + with_ref.getAccession());
//									}
//								}
//							}
//
//							if (with_node != null && negate_list.containsKey(with_node)) {
//								for (String [] withRows : negate_list.get(with_node)) {
//									if (withRows[4].equals(columns[4])) {
//										skipList.add(columns);
//										log.debug("Skipping NOT for " + with_node);
//									}
//								}
//							}
//						} 
//						else {
//							errors.append("Could not parse dbxref from with column \"" + with_string[i] + "\"\n");
//						}
//					}
//				}
//			}
//		}
//		for (GeneNode node : negate_list.keySet()) {
//			Set<String []> row_list = negate_list.get(node);
//			Set<String []> skipList = toBeSkipped.get(node);
//			List<Association> orig_assoc = new ArrayList<Association>();
//			Set<Association> associations = node.getGeneProduct().getAssociations();
//			for (Association assoc : associations) {
//				orig_assoc.add(assoc);
//			}
//			for (String [] columns : row_list) {
//				if (skipList != null && skipList.contains(columns)) {
//					continue;
//				}
//				/* 
//				 * Need to propagate this change to all descendants
//				 */
//				Term term = GO_Util.inst().getTermByAcc(columns[4]);
//				String acc = term.getAcc();
//				for (Association assoc : orig_assoc) {
//					String check = assoc.getTerm().getAcc();
//					if (check.equals(acc)) {
//						Set<Evidence> all_evidence = assoc.getEvidence();
//						/*
//						 * Should just be one piece of evidence
//						 */
//						if (all_evidence.size() == 1) {
//							Evidence evidence = all_evidence.iterator().next();
//							PaintAction.inst().setNot(evidence, node, columns[6], true);
//						}
//					}
//				}
//			}
//		}
//	}
//
//	private static boolean isBlocked(String qualifiers) {
//		return isQualifiedBy(qualifiers, GOConstants.CUT);
//	}
//
//	private static boolean isNot(String qualifiers) {
//		return isQualifiedBy(qualifiers, GOConstants.NOT);
//	}
//
//	private static Set<Term> parseQualifiers(String qualifiers) {
//		Set<Term> qual_list = new HashSet<Term>();
//		if (isQualifiedBy(qualifiers, GOConstants.COLOCATES)) {
//			Term term = GO_Util.inst().getTermByName(GOConstants.COLOCATES);
//			qual_list.add(term);
//		}
//		if (isQualifiedBy(qualifiers, GOConstants.CONTRIBUTES)) {
//			Term term = GO_Util.inst().getTermByName(GOConstants.CONTRIBUTES);
//			qual_list.add(term);
//		}
//		return qual_list;
//	}
//
//	private static String getQualifiers(Association assoc) {
//		StringBuffer qual_string = new StringBuffer();
//		String prefix = "";
//		if (assoc.isNot()) { 
//			qual_string.append(GOConstants.NOT);
//			prefix = "|";
//		}
//		Set<Term> qual_terms = assoc.getQualifiers();
//		if (qual_terms != null && qual_terms.size() > 0) {
//			for (Term qual_term : qual_terms) {
//				qual_string.append(prefix + qual_term.getName());
//				prefix = "|";
//			}
//		}
//		qual_string.append('\t');
//		return qual_string.toString();
//	}
//
//	private static boolean isQualifiedBy(String qualifiers, String qual) {
//		boolean is_qualified = false;
//		String [] qual_string = qualifiers.split("\\|");
//		for (int j = 0; j < qual_string.length; j++) {
//			String check = qual_string[j];
//			if (check.length() > 0) {
//				is_qualified |= (check.equals(qual));
//			}
//		}
//		return is_qualified;
//	}
//
//	private static StringBuffer writeRow(GeneNode node, GeneProduct gene_product, StringBuffer row, Association assoc, Evidence evidence, boolean curated) {
//		DBXref xref; 
//		if (curated) {
//			xref = GO_Util.inst().getPAINTEvidenceDBXref();
//		} else {
//			xref = evidence.getDbxref();
//		}
//		row.append(xref.getDb_name() + ":" + xref.getAccession() + "\t"); // col 6 evidence reference
//
//		row.append(evidence.getCode() + "\t"); // col 7 evidence code
//		String prefix = "";
//		if (!node.isLeaf()) {
//			/*
//			 * This is the experimental evidence supporting the annotation
//			 * of the ancestral node
//			 */
//			writeWiths(evidence.getWiths(), row);
//		} else {
//			/*
//			 * This is the ancestral evidence supporting the annotation
//			 * to current-day protein nodes
//			 */
//			Set<DBXref> withs = evidence.getWiths();
//			DBXref ancestor_xref = null;
//			if (withs.size() != 1 && curated) {
//				log.debug(node.getDatabase() + ":" + node.getDatabaseID() + " has " + withs.size() + " ancestors for with!");
//				return null;
//			} else {
//				writeWiths(evidence.getWiths(), row);
//				for (Iterator<DBXref> it = withs.iterator(); it.hasNext();) {
//					ancestor_xref = it.next();
//				}
//			}
//			/* 
//			 * Seems to just be the one ancestral node, so proceed
//			 * First indicate the ancestral node
//			 */
//
//			/*
//			 * Then add the ancestor's withs to feed back to PANTREE
//			 */
//			if (curated) {
//				GeneNode ancestor = PaintManager.inst().getGeneByDbId(ancestor_xref.getDb_name(), ancestor_xref.getAccession());
//				if (ancestor != null) {
//					Term term = assoc.getTerm();
//					WithEvidence with_evidence = new WithEvidence(term, ancestor);
//					Set<GeneNode> with_nodes = with_evidence.getExpWiths();
//					prefix = Constant.STR_PIPE;
//					for (Iterator<GeneNode> it = with_nodes.iterator(); it.hasNext();) {
//						GeneNode with = it.next();
//						DBXref dbxref = with.getGeneProduct().getDbxref();
//						row.append(prefix + dbxref.getDb_name() + ":" + dbxref.getAccession()); // col 8 With
//					}
//				}
//			}
//		}
//		row.append("\t");
//		/* aspect column */
//		row.append(getAspect(assoc.getTerm()) + "\t"); // col 9 Aspect
//		/* skipping full name column 10 */
//		//					row.append(gene_product.getFull_name() + "\t");
//		row.append("\t");
//		prefix = "";
//		Set<String> synonyms = gene_product.getSynonyms();
//		if (synonyms != null) {
//			for (String synonym : synonyms) {
//				row.append(prefix + synonym); // col 11 synonyms
//				prefix = Constant.STR_PIPE;
//			}
//		}
//		row.append("\t");
//		/*
//		 * Sanity checking because at some point there were examples of these being corrupt in the GAF files
//		 */
//		if (gene_product.getSO_type() == null) {
//			row.append("protein\t");			
//		} else {
//			if (gene_product.getSO_type().getAcc().startsWith("GO")) {
//				log.error("SO type for " + gene_product.getDbxref().getDb_name() + ":" + gene_product.getDbxref().getAccession() + " is " + gene_product.getSO_type().getAcc());
//				row.append("protein\t");						
//			} else {
//				row.append(gene_product.getSO_type().getAcc() + "\t"); // col 12 GO branch
//			}
//		}
//		Species species = gene_product.getSpecies(); // col 13 taxon ID
//		if (species != null) {
//			String species1 = node.getSpeciesLabel();
//			String taxon_id = "";
//			try {
//				taxon_id = taxon_id + species.getNcbi_taxa_id();
//			} catch (Exception e) {
//				log.error(e.getCause());
//			}
//			row.append(GOConstants.TAXON_PREFIX + taxon_id + "\t");
//			if ((species1.contains("homo") && !taxon_id.equals("9606")) ||
//					(species1.contains("mus") && !taxon_id.equals("10090")) ||
//					(species1.contains("rattus") && !taxon_id.equals("10116")) ||
//					(species1.contains("cerevisae") && !taxon_id.equals("4932")) ||
//					(species1.contains("dicty") && !taxon_id.equals("44689")) ||
//					(species1.contains("drosophila") && !taxon_id.equals("7227")) ||
//					(species1.contains("pombe") && !taxon_id.equals("4896")) ||
//					(species1.contains("arabidopsis") && !taxon_id.equals("3702")) ||
//					(species1.contains("elegans") && !taxon_id.equals("6239")) ||
//					(species1.contains("danio") && !taxon_id.equals("7955")) ||
//					(species1.contains("coli") && !(taxon_id.equals("511145") || taxon_id.equals("83333"))) ||
//					(species1.contains("gallus") && !taxon_id.equals("9031"))) {
//			}
//		}
//		else {
//			row.append("\t");
//		}
//		row.append(assoc.getDate() + "\t"); // col 14 date
//		row.append(assoc.getSource_db().getName() + "\t"); // col 15 who made the annotation
//
//		// column 16 (annotation extension) -- empty for now
//		row.append("" + "\t");
//
//		// column 17 (gene product form id) -- empty for now
//		row.append("");
//
//		return row;
//	}
//
//	private static String lastResort(String [] columns ) {
//		String seqID = null;
//		String db = columns[0];
//		String db_id = columns[1];
//		String qual = columns[3];
//		if (!db.equals(GOConstants.PANTHER_DB) && (qual.equals(GOConstants.CUT) || qual.equals(GOConstants.NOT))) {
//			SeqMapper mapper = SeqMapper.inst();
//			seqID = mapper.getSeqID(db, db_id);
//		}
//		return seqID;
//	}
//}
