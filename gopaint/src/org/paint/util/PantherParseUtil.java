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
package org.paint.util;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.main.PaintManager;

import com.sri.panther.paintCommon.Constant;

public class PantherParseUtil {

	private static final String PIPE = "\\|";

	/* 
	 * The names of the headers in the attribute/matrix returned from the Panther database
	 */
	private static final String ACC_TAG = "gi";
	private static final String PROT_TAG = "Protein Id";
	private static final String SF_TAG = "sf_name";
	private static final String ORG_TAG = "organism";
	private static final String DEF_TAG = "definition";
	private static final String GENE_TAG = "gene id";
	private static final String SYMB_TAG = "gene symbol";
	private static final String OMCL_TAG = "OrthoMCL";
	private static final String PERM_NODE_TAG = "public Id";

	private static final String MGI_DATABASE= "MGI";

	// For saving purposes, may need to keep track of original column names when GeneTableModel was initially created
	private Vector<String> origColumnHeadings;

	private Vector<String> currentColHeadings;

	/** perhaps singleton pattern is overkill here, but what the hell
    it is gonna bring up a gui at some point */
	private static PantherParseUtil singleton = null;

	private static Logger log = Logger.getLogger(PantherParseUtil.class);

	public PantherParseUtil () {

	}

	public static PantherParseUtil inst() {
		if (singleton == null)
			singleton = new PantherParseUtil();
		return singleton;
	}

	private String [] getParts(String row) {
		if (row.charAt(row.length() - 1) == ';') {
			row = row.substring(0, row.length() - 1);
		}
		String [] parts = row.split(PIPE);
		return parts;
	}
	private String parseANid(String name) {
		String [] parts = getParts(name);
		String paint_id = null;
		if (parts.length < 1) {
			paint_id = name;
		} else if (parts.length < 2) {
			paint_id = parts[0];
		}
		return paint_id;
	}

	private String [] getDBparts(String row) {
		String [] parts = getParts(row);
		String [] db_source;
		/*
		 * Standard order from PANTHER Database is
		 * SPECI|DbGene=gene_id|ProteinSeq=protein_id
		 * There may be multiple genes all with the same Protein ID
		 */
		if (parts.length >= 3) {
			if (parts[2].contains("=ENSTRUG") || parts[1].contains("=ENSTRUP")) {
				db_source = parts[2].split(Constant.STR_EQUAL);
				String [] seq_source = parts[1].split(Constant.STR_EQUAL);
				log.debug("Gene " + db_source[1] + " and protein " + seq_source[1] + " appear reversed in " + row);
			} else {
				db_source = parts[1].split(Constant.STR_EQUAL);
				if (db_source.length == 3) {
					if (db_source[0].equals(MGI_DATABASE) || db_source[0].equals("TAIR") || db_source[0].equals("ECOLI")) {
						/*
						 * MOUSE|MGI=MGI=97788|UniProtKB=Q99LS3
						 * ARATH|TAIR=locus=2015008|NCBI=NP_176687
						 */
						db_source[1] = db_source[1] + ':' + db_source[2];
					} else if (db_source[0].equals("Gene")) {
						// repairs for CGD & XenBase
						db_source[0] = db_source[1];
						db_source[1] = db_source[2];
					}
					else {
						log.debug("Too many parts in " + parts[1]);
					}
				}
			}
			return db_source;
		} else {
			return null;
		}
	}

	public String [] getSeqParts(String row) {
		String [] parts = getParts(row);
		String [] seq_source;
		if (parts.length >= 3) {
			if (parts[2].contains("=ENSTRUG") || parts[1].contains("=ENSTRUP")) {
				seq_source = parts[1].split(Constant.STR_EQUAL);
			} else {
				seq_source = parts[2].split(Constant.STR_EQUAL);
			}
			return seq_source;
		}
		else {
			return null;
		}
	}

	public void parseIDstr(GeneNode node, String name) {
		String paint_id = parseANid(name);
		if (paint_id != null) 
			node.setPaintId(paint_id);
		else {
			String [] parts = getParts(name);
			if (parts != null && parts.length > 0) {
                            node.addSpeciesLabel(parts[0]);
                            node.setSpecies(parts[0]);
                        }
			String [] db_source = getDBparts(name);
			String [] seq_source = getSeqParts(name);
			/*
			 * Standard order from PANTHER Database is
			 * SPECI|DbGene=gene_id|ProteinSeq=protein_id
			 * There may be multiple genes all with the same Protein ID
			 */
			if (db_source != null && db_source.length >= 2) {
				node.setDatabaseID(db_source[0], db_source[1]);				
			}
			else 
				log.debug("Couldn't get db from " + name);
			node.setSeqId(seq_source[0], seq_source[1]);
			// Setting the seq database & name must come -after- the seqId is set since setSeqId may override
			node.setSeqName(node.getSpeciesLabel() + "_" + node.getSeqId());
		}
	}

	public void setOrigAttributeHeadings(Vector<String> headings) {
		inst().setHeadings(headings);
	}

	private void setHeadings(Vector<String> headings) {
		origColumnHeadings = headings;
	}

	public Vector<String> getOrigAttributeHeadings() {
		return inst().getHeadings();
	}

	private Vector<String> getHeadings() {
		return origColumnHeadings;
	}

	public Vector<String> getCurrentHeadings() {
		return inst().currentColHeadings;
	}

//	public GeneNode parseAttributeRow(Vector<String> row, int idColIndex) {
//		String id = row.elementAt(idColIndex);
//		String ptn = row.elementAt(row.size() - 1);
//		GeneNode gene = findThatNode(id);
//		if (gene.getPersistantNodeID() != null && !gene.getPersistantNodeID().equals(ptn)) {
//			/*
//			 * This should never happen!
//			 */
//			log.error("Yikes, " + gene.getPersistantNodeID() + " does not equal " + ptn);	
//		}
//		for (int j = 0; j < row.size(); j++) {
//			String tag = origColumnHeadings.elementAt(j);
//			String value = row.elementAt(j);
//			gene.setAttrLookup(tag, value);
//
//			if (tag.equals(ACC_TAG) || tag.equals(PROT_TAG)) {
//				if (gene.getSeqId() == null) {
//					gene.setSeqId(gene.getSeqDB(), value);
//					log.error("Set accession after the fact for: " + value);
//				}
//			} else if (tag.equals(DEF_TAG)) {
//				gene.setDescription(cleanDefinition(value));
//			} else if (tag.equals(ORG_TAG)) {
//				gene.addSpeciesLabel(value);
//                                gene.setSpecies(value);
//			} else if (tag.equals(SYMB_TAG)) {
//				gene.setSeqName(value);
//			} else if (tag.equals(GENE_TAG)) {
//				/* 
//				 * First eliminate the weird comma separated values
//				 */
//				String [] doublet = value.split(Constant.STR_COMMA);
//				if (doublet.length == 2) {
//					value = doublet[0];
//				}
//			} else if (tag.equals(OMCL_TAG)) {
//				/*
//				 * If this is a member of an OrthoMCL family the value like this is provided: ORTHOMCL1010
//				 */
//				gene.setOrthoMCL(value);
//			} else if (tag.equals(PERM_NODE_TAG)) {
//				gene.setPersistantNodeID(value);
//			}
//		}
//		return gene;
//	}

//	public GeneNode findThatNode(String row) {
//		PaintManager manager = PaintManager.inst();
//		GeneNode gene = null;
//		String paint_id = parseANid(row);
//
//		if (paint_id != null)
//			gene = manager.getGeneByPaintId(paint_id);
//		if (gene == null) {
//			String [] seq_source = 	getSeqParts(row);
//
//			if (seq_source != null && seq_source.length >= 2) {
//				List<GeneNode> genes = manager.getGenesBySeqId(seq_source[0], seq_source[1]);
//				String [] db_source = getDBparts(row);
//				if (genes == null) {
//					gene = manager.getGeneByDbId(db_source[0], db_source[1]);
//				} else {
//					for (GeneNode check : genes) {
//						String db = GO_Util.inst().dbNameHack(db_source[0]);
//						if (check.getDatabase().equals(db) && check.getDatabaseID().equals(db_source[1])) {
//							gene = check;
////							if (genes.size() > 1)
////							log.debug("Multiple genes for " + seq_source[0] + ':' + seq_source[1] + ", but found match to " + gene.getDatabase() + ':' + gene.getDatabaseID());
//						}
//					}
//				}
//			}
//		}
//		if (gene == null)
//			log.debug("Unable to locate node for " + row);
//		return gene;
//	}
	/**
	 * Add Attributes for non GO user
	 * @param orderedNodes
	 * @param data
	 */
	public void addGeneAttributes(GeneNode gene, Vector<String> row) {
		Vector <String> origColHeadings = inst().origColumnHeadings;
		for (int i = 0; i < row.size(); i++) {
			gene.setAttrLookup(origColHeadings.get(i), row.get(i));
		}
	}

	public Hashtable<String, String> getAttributes(GeneNode node) {
		Hashtable<String, String> gene_data = new Hashtable<String, String>(3);
		gene_data.put(ACC_TAG, node.getSeqId());
		gene_data.put(SF_TAG, Constant.STR_EMPTY);
		gene_data.put(DEF_TAG, node.getDescription());
		return gene_data;
	}


	public int getColIndex(String colName) {
		if (null == currentColHeadings) {
			return -1;
		}
		for (int i = 0; i < currentColHeadings.size(); i++) {
			if (true == colName.equals(currentColHeadings.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * For reasons unstated it was decided to concoct some concatenated string of alternate names,
	 * rather than simply use the normal "definition" as it comes from GenBank of EMBL.
	 * In any case these are ugly, impenetrable and should never, under any circumstances be seen by a user
	 * This function is an sadly needed hack/kludge to work around this weirdness
	 * 
	 */
	private String cleanDefinition(String def) {
		String better_def = def;
		if (!def.equals("")) {
			String [] parts = def.split(Constant.STR_SEMI_COLON);
			String [] wierd = parts[0].split(Constant.STR_EQUAL);
			if (wierd.length == 2)
				better_def = wierd[1];
		}
		return better_def;
	}

}
