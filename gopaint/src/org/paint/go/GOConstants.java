package org.paint.go;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GOConstants {
	public static final String BP_ID = "GO:0008150";
	public static final String CC_ID = "GO:0005575";
	public static final String MF_ID = "GO:0003674";
	
	public static final String NOT = "NOT";
	public static final String CUT = "CUT";

	//public final static String PUBMED = "PUBMED:";
	public final static String KEY_RESIDUES = "NOT due to change in key residue(s)";
	public final static String KEY_RESIDUES_EC = "IKR";
	public final static String DIVERGENT = "NOT due to rapid divergence";
	public final static String DIVERGENT_EC = "IRD";
	public final static String DESCENDANT_SEQUENCES = "NOT due to descendant sequence(s)";
	public final static String DESCENDANT_SEQUENCES_EC = "IBD"; // was IDS
	public static final String ANCESTRAL_EVIDENCE_CODE = "IBA"; // was IAS
	public final static String PAINT_REF = "PAINT_REF";
	public final static String PANTHER_DB = "PANTHER";
	public final static String OLD_SOURCE = "RefGenome";
	public final static String PAINT_AS_SOURCE = "GO_Central";

	public final static String CONTRIBUTES = "contributes_to";
	public final static String COLOCATES = "colocalizes_with";
	
	public final static String TAXON_PREFIX = "taxon:";

	public final static ArrayList<String> not_quals = new ArrayList<String> ();
	static {
		not_quals.add(KEY_RESIDUES);
		not_quals.add(DIVERGENT);
		not_quals.add(DESCENDANT_SEQUENCES);
	}

	public static final String[] Not_Strings = { 
		DIVERGENT, 
		KEY_RESIDUES
	};

	public static final String[] Not_Strings_Ext = {
		DIVERGENT,
		KEY_RESIDUES,
		DESCENDANT_SEQUENCES
	};

	public static final String[] Qual_Strings = { 
		CONTRIBUTES,
		COLOCATES 
	};

	public static final Map<String, String> NOT_QUALIFIERS_TO_EVIDENCE_CODES = new HashMap<String, String>();
	static {
		NOT_QUALIFIERS_TO_EVIDENCE_CODES.put(DIVERGENT, DIVERGENT_EC);
		NOT_QUALIFIERS_TO_EVIDENCE_CODES.put(DESCENDANT_SEQUENCES, DESCENDANT_SEQUENCES_EC);
		NOT_QUALIFIERS_TO_EVIDENCE_CODES.put(KEY_RESIDUES, KEY_RESIDUES_EC);
	}

	public static final Map<String, String> NOT_EVIDENCE_CODES_TO_QUALIFIERS = new HashMap<String, String>();
	static {
		NOT_EVIDENCE_CODES_TO_QUALIFIERS.put(DIVERGENT_EC, DIVERGENT);
		NOT_EVIDENCE_CODES_TO_QUALIFIERS.put(DESCENDANT_SEQUENCES_EC, DESCENDANT_SEQUENCES);
		NOT_EVIDENCE_CODES_TO_QUALIFIERS.put(KEY_RESIDUES_EC, KEY_RESIDUES);
	}

	public final static ArrayList<String> EXPECTED_SPECIES = new ArrayList<String> ();
	static {
		EXPECTED_SPECIES.add("homo sapiens");
		EXPECTED_SPECIES.add("mus musculus");
		EXPECTED_SPECIES.add("rattus norwegicus");
		EXPECTED_SPECIES.add("gallus gallus");
		EXPECTED_SPECIES.add("danio rerio");
		EXPECTED_SPECIES.add("drosophila melanogaster");
		EXPECTED_SPECIES.add("caenorhabditis elegans");
		EXPECTED_SPECIES.add("arabidopsis thaliana");
		EXPECTED_SPECIES.add("dictyostelium discoideum");
		EXPECTED_SPECIES.add("escherichia coli");
		EXPECTED_SPECIES.add("saccharomyces cerevisae");
		EXPECTED_SPECIES.add("schizosaccharomyces pombe");
	};

	public final static String GO_REF_TITLE = "Annotation inferences using phylogenetic trees";
	public final static String GO_REF_SW = "PAINT (Phylogenetic Annotation and INference Tool).";
	public final static String go_ref = GO_REF_TITLE + "\n\n"
		+ "The goal of the GO Reference Genome Project, described in PMID 19578431, "
		+ "is to provide accurate, complete and consistent GO annotations for all genes in twelve model organism genomes. "
		+ "To this end, GO curators are annotating evolutionary trees from the PANTHER database with GO terms "
		+ "describing molecular function, biological process and cellular component. "
		+ "GO terms based on experimental data from the scientific literature are used "
		+ "to annotate ancestral genes in the phylogenetic tree by sequence similarity (ISS), "
		+ "and unannotated descendants of these ancestral genes are inferred to have inherited these same GO annotations by descent. "
		+ "The annotations are done using a tool called " + GO_REF_SW + "\n";

}
