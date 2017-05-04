/* 
 * 
 * Copyright (c) 2017, Regents of the University of California 
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

package org.paint.config;

import com.sri.panther.paintCommon.util.ReadResources;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.obo.util.VersionNumber;
import org.paint.go.GOConstants;
import org.paint.util.SVGIcon;

/**
 * Used for reading previous or default user settings from property file and storing current user settings
 */

public class Preferences {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5472475387423113108L;

	protected static Logger log = Logger.getLogger("org.paint.config.Preferences");
        

	private String pantherURL = null;
        private double tree_distance_scaling = 50;
        protected ReadResources RR = initResources();        
        private  ReadResources initResources() {
            try {
                ReadResources rr = new ReadResources("user");
                pantherURL = rr.getKey("servlet_url");
                tree_distance_scaling = Double.parseDouble(rr.getKey("tree_scaling"));
                return rr;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }

//	private String uploadVersion = "dev_3_panther_upl|UPL 10.0";


	private boolean useDistances = true;
	private String GO_file = "http://www.geneontology.org/ontology/obo_format_1_2/gene_ontology.1_2.obo";

	private Font font = new Font("Arial", Font.PLAIN, 12); 

	private Map<String, Icon> iconIndex = new HashMap<String, Icon>();
	private Map<String, String> iconURLIndex = new HashMap<String, String>();

	private Color foregroundColor = Color.black;
	private Color backgroundColor = Color.white;
	private Color selectionColor = Color.black; //  = new Color(207,226,245);

	private VersionNumber version;

	private Color expPaintColor =   new Color(16, 128, 64);  //new Color(142, 35, 35);
	private Color curatedPaintColor = new Color(255, 127, 0); //new Color(68, 116, 179);  //new Color(255, 127, 0);
	private Color inferPaintColor = new Color(16, 64, 128);

	public final static int HIGHLIGHT_BP = 1;
	public final static int HIGHLIGHT_CC = 2;
	public final static int HIGHLIGHT_MF = 4;

	private Color mfPaintColor = new Color(232, 248, 232);
	private Color ccPaintColor = new Color(224, 248, 255);
	private Color bpPaintColor = new Color(255, 248, 220);

	private float msa_threshold[] = {
			80, 60, 40
	};

	private float  msa_weighted_threshold[] = {
			90, 75
	};

	private Color  msa_colors[] = {
			new Color(51, 102, 77), new Color(112, 153, 92), new Color(204, 194, 143)
	};

	private Color  msa_weighted_colors[] = {
			new Color(21, 138, 255), new Color(220, 233, 255)
	};

	private String high_throughput[] = {
			"PMID:10341420",
			"PMID:10662773",
			"PMID:11027285",
			"PMID:11452010",
			"PMID:11914276",
			"PMID:12077337",
			"PMID:12089449",
			"PMID:12134085",
			"PMID:12150911",
			"PMID:12192589",
			"PMID:12482937",
			"PMID:12524434",
			"PMID:12586695",
			"PMID:14562095",
			"PMID:14576278",
			"PMID:14645503",
			"PMID:14690591",
			"PMID:14690608",
			"PMID:15024427",
			"PMID:15343339",
			"PMID:15575969",
			"PMID:15632165",
			"PMID:15738404",
			"PMID:16121259",
			"PMID:16269340",
			"PMID:16319894",
			"PMID:16407407",
			"PMID:16467407",
			"PMID:16622836",
			"PMID:16702403",
			"PMID:16823372",
			"PMID:16823961",
			"PMID:17176761",
			"PMID:17443350",
			"PMID:1848238",
			"PMID:18627600",
			"PMID:19001347",
			"PMID:19040720",
			"PMID:19053807",
			"PMID:19056867",
			"PMID:19061648",
			"PMID:19111667",
			"PMID:19158363",
			"PMID:20424846",
			"PMID:2153142",
			"PMID:22842922",
			"PMID:23212245",
			"PMID:23222640",
			"PMID:23376485",
			"PMID:23533145",
			"PMID:24390141",
			"PMID:2445736",
			"PMID:3031032",
			"PMID:3065625",
			"PMID:8660468",
			"PMID:9020838",
			"PMID:9182565",
	};

	private static Preferences preferences;

	/*
	 * Get the NCBI taxon ID from their FTP-ed file dump
	 */
	private Map<String, String> taxa2IDs;
	private Map<String, String> IDs2taxa;

	/**
	 * Constructor declaration
	 * @throws Exception 
	 *
	 *
	 * @see
	 */
	public Preferences() { //throws Exception {
		// For now use this font, however, this should be loaded from the users system; 
		// just in case the font is unavailable in the users machine.
		iconURLIndex.put("trash", "resource:trash.png");
		iconURLIndex.put("paint", "resource:direct_annot.png");
		iconURLIndex.put("arrowDown", "resource:arrowDown.png");
		iconURLIndex.put("block", "resource:Emblem-question.svg");
		iconURLIndex.put("not", "resource:round-stop.png");
		iconURLIndex.put("exp", "resource:Bkchem.png");
		iconURLIndex.put("inherited", "resource:inherited_annot.png");		
		iconURLIndex.put("colocate", "resource:colocate.png");
		iconURLIndex.put("contribute", "resource:contribute.svg");
	}

	public static Preferences inst() {
		if (preferences == null) {
                    //DO NOT READ FROM FILE
                    preferences = new Preferences();
//			XMLDecoder d;
//			try {
//				d = new XMLDecoder(new BufferedInputStream(new FileInputStream(
//						Preferences.getPrefsXMLFile())));
//				Preferences p = (Preferences) d.readObject();
//				preferences = (Preferences) p;
//                                // These get overwritten when we read from xml file
//                                pantherURL = RR.getKey("servlet_url");
//                                tree_distance_scaling = Double.parseDouble(RR.getKey("tree_scaling"));
//				d.close();
//			} catch (Exception e) {
//				log.info("Could not read preferences file from "
//						+ Preferences.getPrefsXMLFile());
//			}
//			if (preferences == null)
//				preferences = new Preferences();

			GUIManager.addShutdownHook(new Runnable() {
				public void run() {
					try {
						writePreferences(inst());
					} catch (IOException ex) {
						log.info("Could not write verification settings!");
						ex.printStackTrace();
					}
				}
			});
		}
		return preferences;
	}

	protected static void writePreferences(Preferences preferences)
			throws IOException {
		XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
				new FileOutputStream(getPrefsXMLFile())));
		log.info("Writing preferences to " + getPrefsXMLFile());
		encoder.setPersistenceDelegate(Font.class,
				new DefaultPersistenceDelegate(
						new String[]{ "name",
								"style",
						"size" }) );
		encoder.setPersistenceDelegate(Color.class,
				new DefaultPersistenceDelegate(
						new String[]{ "red",
								"green",
						"blue" }) );
		encoder.writeObject(preferences);
		encoder.close();
	}

	public static File getPrefsXMLFile() {
		return new File(getPaintPrefsDir(), "preferences.xml");
	}

	public static File getPaintPrefsDir() {
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();               
		File prefsDir = new File(s + File.separator + "perspectives");
		return prefsDir;
	}
        
        public String getPAINTversion() {
            return "2017_03_23_1";
        }

	public VersionNumber getVersion() {
		if (version == null) {
			try {
				URL url = getExtensionLoader().getResource(
						"org/paint/resources/VERSION");
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream()));
				version = new VersionNumber(reader.readLine());
				reader.close();
			} catch (Exception e) {
				try {
					version = new VersionNumber("1.0");
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
		}
		return version;
	}

	public String getSpeciesName(String taxon_id) {
		if (IDs2taxa == null) {
			loadTaxaMapping();
		}
		return IDs2taxa.get(taxon_id);
	}

	public String getTaxonID(String species_name) {
		if (taxa2IDs == null) {
			loadTaxaMapping();
		}
		if (!species_name.equals("root")) 
			species_name = species_name.substring(0, 1).toUpperCase() + species_name.substring(1);
		String taxon_id = taxa2IDs.get(species_name);
		if (taxon_id == null) {
			taxon_id = taxa2IDs.get(species_name.toLowerCase());
		}
		if (taxon_id == null) {
			taxon_id = taxa2IDs.get(speciesNameHack(species_name));
		}
		return taxon_id;
	}

	private void loadTaxaMapping() {
		taxa2IDs = new HashMap<String, String>();
		IDs2taxa = new HashMap<String, String>();
		try {
			URL url = getExtensionLoader().getResource(
					"org/paint/resources/ncbi_taxa_ids.txt");
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(url.openStream()));
			String id_pair = reader.readLine();
			while (id_pair != null) {
				if (!id_pair.contains("authority")) {
					id_pair = id_pair.replace('\t', ' ');
					String ids []= id_pair.split("\\|");
					String taxon_id = GOConstants.TAXON_PREFIX+(ids[0].trim());
					String name = ids[1].trim();
					if (!ids[2].contains(name))
						name = (name + " " +  ids[2].trim()).trim();
					else if (ids[2].trim().length() > name.length())
						name = ids[2].trim();
					if (id_pair.contains("scientific name")) {
						IDs2taxa.put(taxon_id, name);
					}
					taxa2IDs.put(name, taxon_id);
				}
				id_pair = reader.readLine();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static ClassLoader getExtensionLoader() {
		return Preferences.class.getClassLoader();
	}

	protected Icon loadLibraryIconLocal(String name) {
		String dir = "org/paint/resources/";
		URL url = getExtensionLoader().getResource(
				dir + name);
		if (url == null) {
			url = getExtensionLoader().getResource(
					"org/paint/resources/icons" + name);
		}
		if (url == null)
			log.debug("Oops, could not find icon " + name);
		return getIconForURL(url);
	}

	public static Icon getIconForURL(URL url) {
		if (url == null)
			return null;

		try {
			String urlStr = url.toString();
			if (urlStr.endsWith("svg"))
				return new SVGIcon(urlStr);
		} catch (Exception e) {
			log.info("WARNING: Exception getting icon for " + url + ": " + e); // DEL
		}
		return new ImageIcon(url);
	}

	public static Image loadLibraryImage(String name) {
		URL url = getExtensionLoader().getResource(
				"org/paint/gui/resources/" + name);
		return Toolkit.getDefaultToolkit().createImage(url);
	}

	public Icon loadLibraryIcon(String name) {
		return inst().loadLibraryIconLocal(name);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public Font getFont(){
		return font;
	}

	public void setFont(Font f) {
		font = f;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color c) {
		foregroundColor = c;
	}

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color c) {
		backgroundColor = c;
	}

	public Color getSelectionColor() {
		return selectionColor;
	}

	public void setSelectionColor(Color c) {
		selectionColor = c;
	}

	public double getTree_distance_scaling() {
		return tree_distance_scaling;
	}

	public void setTree_distance_scaling(double scale) {
		tree_distance_scaling = scale;
	}

	public Object clone() throws CloneNotSupportedException {

		throw new CloneNotSupportedException();

	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public boolean isUseDistances(){
		return useDistances;
	}

	public boolean getUseDistances() {
		return isUseDistances();
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param distance
	 *
	 * @see
	 */
	public void setUseDistances(boolean distance){
		useDistances = distance;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public void toggleUseDistances(){
		setUseDistances(!useDistances);
	}

	public Icon getIconByName(String id) {
		Icon out = (Icon) iconIndex.get(id);
		if (out == null) {
			String iconURL = iconURLIndex.get(id);
			if (iconURL != null) {
				if (iconURL.startsWith("resource:")) {
					out = loadLibraryIcon(iconURL.substring(9));
				} else {
					try {
						out = getIconForURL(new URL(iconURL));
					} catch (MalformedURLException e) {
						File file = new File(iconURL);
						if (file.exists())
							try {
								out = getIconForURL(file.toURI().toURL());
							} catch (MalformedURLException e1) {
							}
					}
				}
			}
			if (out != null) {
				iconIndex.put(id, out);
			}
		}
		return out;
	}

	public String getPantherURL() {
		return pantherURL;
	}

	public void setPantherURL(String url) {
		if (url != null && url.length() > 0)
			pantherURL = url;
	}

//	public String getUploadVersion() {
//		return uploadVersion;
//	}
//
//	public void setUploadVersion(String v) {
//		uploadVersion = v;
//	}

	public String getGO_file() {
		return GO_file;
	}

	public void setGO_file(String f) {
		this.GO_file = f;
	}

	public Color getAspectColor(int aspect) {
		Color color = getBackgroundColor();
		if (aspect > 0) {
			switch (aspect) {
			case Preferences.HIGHLIGHT_MF:
				color = mfPaintColor;
				break;
			case Preferences.HIGHLIGHT_CC:
				color = ccPaintColor;
				break;
			case Preferences.HIGHLIGHT_BP:
				color = bpPaintColor;
				break;
			}
		}
		return color;
	}

	public void setAspectColor(int aspect, Color color) {
		if (aspect > 0) {
			switch (aspect) {
			case Preferences.HIGHLIGHT_MF:
				mfPaintColor = color;
				break;
			case Preferences.HIGHLIGHT_CC:
				ccPaintColor = color;
				break;
			case Preferences.HIGHLIGHT_BP:
				bpPaintColor = color;
				break;
			}
		}
	}

	public Color getMfPaintColor() {
		return mfPaintColor;
	}

	public void setMfPaintColor(Color mfColor) {
		mfPaintColor = mfColor;
	}

	public Color getCcPaintColor() {
		return ccPaintColor;
	}

	public void setCcPaintColor(Color ccColor) {
		ccPaintColor = ccColor;
	}

	public Color getBpPaintColor() {
		return bpPaintColor;
	}

	public void setBpPaintColor(Color bpColor) {
		bpPaintColor = bpColor;
	}

	public Color getExpPaintColor() {
		return expPaintColor;
	}

	public void setExpPaintColor(Color c) {
		expPaintColor = c;
	}

	public Color getCuratedPaintColor() {
		return curatedPaintColor;
	}

	public void setCuratedPaintColor(Color c) {
		curatedPaintColor = c;
	}

	public Color getInferPaintColor() {
		return inferPaintColor;
	}

	public void setInferPaintColor(Color c) {
		inferPaintColor = c;
	}

	public float[] getMSAThresholds(boolean weighted) {
		if (weighted)
			return msa_weighted_threshold;
		else
			return msa_threshold;
	}

	public Color[] getMSAColors(boolean weighted) {
		if (weighted)
			return msa_weighted_colors;
		else
			return msa_colors;
	}

	public void setMSAThresholds(boolean weighted, float[] thresholds) {
		if (weighted) 
			msa_weighted_threshold = thresholds;
		else
			msa_threshold = thresholds;
	}

	public void setMSAColors(boolean weighted, Color[] colors) {
		if (weighted) 
			msa_weighted_colors = colors;
		else
			msa_colors = colors;
	}

	public float[] getMsa_threshold() {
		return msa_threshold;
	}

	public void setMsa_threshold(float[] thresholds) {
		msa_threshold = thresholds;
	}

	public float[] getMsa_weighted_threshold() {
		return msa_weighted_threshold;
	}

	public void setMsa_weighted_threshold(float[] thresholds) {
		msa_weighted_threshold = thresholds;
	}

	public Color[] getMsa_colors() {
		return msa_colors;
	}

	public void setMsa_colors(Color[] colors) {
		msa_colors = colors;
	}

	public Color[] getMsa_weighted_colors() {
		return msa_weighted_colors;
	}

	public void setMsa_weighted_colors(Color[] colors) {
		msa_weighted_colors = colors;
	}
	
	public boolean isExcluded(String pubmedID) {
		boolean excluded = false;
		for (int i = 0; i < this.high_throughput.length && !excluded; i++) {
			excluded = pubmedID.equals(high_throughput[i]);
		}
		return excluded;
	}

	private String speciesNameHack(String name) {
		String lcName = name.toLowerCase();
		/* The GO database is not using the suffix */
		if (lcName.equals("human")) {
			name = "Homo sapiens";
		} else if (lcName.equals("pantr")) {
			name = "Pan troglodytes";
		} else if (lcName.equals("homo-pan")) {
			name = "Homininae";
		} else if (lcName.equals("mouse")) {
			name = "Mus musculus";
		} else if (lcName.equals("rat")) {
			name = "Rattus norvegicus";
		} else if (lcName.equals("bovin")) {
			name = "Bos taurus";
		} else if (lcName.equals("canis familiaris") || lcName.equals("canfa")) {
			name = "Canis lupus familiaris";
		} else if (lcName.equals("mondo")) {
			name = "Monodelphis domestica";
		} else if (lcName.equals("ornan")) {
			name = "Ornithorhynchus anatinus";
		} else if (lcName.equals("chick")) {
			name = "Gallus gallus";
		} else if (lcName.equals("xentr")) {
			name = "Xenopus (Silurana) tropicalis";
		} else if (lcName.equals("fugu rubripes") || lcName.equals("fugru")) {
			name = "Takifugu rubripes";
		} else if (lcName.equals("brachydanio rerio") || lcName.equals("danre")) {
			name = "Danio rerio";
		} else if (lcName.equals("cioin")) {
			name = "Ciona intestinalis";
		} else if (lcName.equals("strpu")) {
			name = "Strongylocentrotus purpuratus";
		} else if (lcName.equals("caenorhabditis")) {
			name = "Caenorhabditis elegans";
		} else if (lcName.equals("briggsae") || lcName.equals("caebr")) {
			name = "Caenorhabditis briggsae";
		} else if (lcName.equals("drome")) {
			name = "Drosophila melanogaster";
		} else if (lcName.equals("anopheles gambiae str. pest") || lcName.equals("anoga")) {
			name = "Anopheles gambiae";
		} else if (lcName.equals("yeast")) {
			name = "Saccharomyces cerevisiae";
		} else if (lcName.equals("ashbya gossypii") || lcName.equals("ashgo")) {
			name = "Eremothecium gossypii";
		} else if (lcName.equals("neucr")) {
			name = "Neurospora crassa";
		} else if (lcName.equals("schpo")) {
			name = "Schizosaccharomyces pombe";
		} else if (lcName.equals("dicdi")) {
			name = "Dictyostelium discoideum";
		} else if (lcName.equals("aspergillus nidulans")) {
			name = "Emericella nidulans";
		} else if (lcName.equals("chlre")) {
			name = "Chlamydomonas reinhardtii";
		} else if (lcName.equals("orysj")) {
			name = "Oryza sativa";
		} else if (lcName.equals("arath")) {
			name = "Arabidopsis thaliana";
		} else if (lcName.equals("metac")) {
			name = "Methanosarcina acetivorans";
		} else if (lcName.equals("strco")) {
			name = "Streptomyces coelicolor";
		} else if (lcName.equals("glovi")) {
			name = "Gloeobacter violaceus";
		} else if (lcName.equals("lepin")) {
			name = "Leptospira interrogans";
		} else if (lcName.equals("braja")) {
			name = "Bradyrhizobium japonicum";
		} else if (lcName.equals("escherichia coli coli str. K-12 substr. MG1655") || lcName.equals("ecoli")) {
			name = "Escherichia coli";
		} else if (lcName.equals("enthi")) {
			name = "Entamoeba histolytica";
		} else if (lcName.equals("bacsu")) {
			name = "Bacillus subtilis";
		} else if (lcName.equals("deira")) {
			name = "Deinococcus radiodurans";
		} else if (lcName.equals("thema")) {
			name = "Thermotoga maritima";
		} else if (lcName.equals("opisthokonts")) {
			name = "Opisthokonta";
		} else if (lcName.equals("bactn")) {
			name = "Bacteroides thetaiotaomicron";
		} else if (lcName.equals("leima")) {
			name = "Leishmania major";
		} else if (lcName.equals("eubacteria")) {
			name = "Bacteria <prokaryote>";
		} else if (lcName.equals("theria")) {
			name = "Theria <Mammalia>";
		} else if (lcName.equals("geobacter sufurreducens") || lcName.equals("geosl")) {
			name = "Geobacter sulfurreducens";
		} else if (lcName.equals("psea7")) {
			name = "Pseudomonas aeruginosa";
		} else if (lcName.equals("aquae") || lcName.equals("aquifex aeolicus vf5")) {
			name = "Aquifex aeolicus";		
		} else if (lcName.equals("metac") || lcName.equals("methanosarcina acetivorans c2a")) {
			name = "Methanosarcina acetivorans";		
		} else if (lcName.equals("sulso") || lcName.equals("sulfolobus solfataricus p2")) {
			name = "Sulfolobus solfataricus";		
		} else if (lcName.equals("saccharomycetaceae-candida")) {
			name = "mitosporic Nakaseomyces";
		} else if (lcName.equals("sordariomycetes-leotiomycetes")) {
			name = "Leotiomycetes";
		} else if (lcName.equals("excavates")) {
			name = "Excavarus";
		} else if (lcName.equals("metazoa-choanoflagellida")) {
			name = "Opisthokonta";
		} else if (lcName.equals("alveolata-stramenopiles")) {
			name = "Eukaryota";
		} else if (lcName.equals("pezizomycotina-saccharomycotina")) {
			name = "saccharomyceta";
		} else if (lcName.equals("unikonts")) {
			name = "Eukaryota";
		} else if (lcName.equals("archaea-eukaryota")) {
			name = "cellular organisms";
		} else if (lcName.equals("osteichthyes")) {
			name = "Euteleostomi";
			//		} else if (lcName.equals("luca")) { // last universal common ancestor
			//			name = "Notodontidae";
		} else if (lcName.equals("craniata-cephalochordata")) {
			name = "Chordata";
		} else if (lcName.equals("hexapoda-crustacea")) {
			name = "Pancrustacea";
		} else if (lcName.equals("rhabditida-chromadorea")) {
			name = "Chromadorea";
		}
		return name;
	}

}
