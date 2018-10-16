package org.paint.util;
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

import java.awt.Color;
import java.awt.Font;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.HyperlinkEvent;

import org.apache.log4j.Logger;
import org.bbop.swing.HyperlinkLabel;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.go.GOConstants;
import org.paint.gui.event.TermHyperlinkListener;
import org.paint.gui.table.GeneTableModel;

import com.sri.panther.paintCommon.Constant;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;
import java.awt.Desktop;
import java.net.URI;

public class HTMLUtil {

	protected static final String COMMA_DELIM = ",";
	protected static final String SPACE_DELIM = " ";
	protected static final String SEMI_COLON_DELIM = ";";
	protected static final String REPLACE_STRING = "XXX";
	protected static final String SPECIES_SPOT = "YYY";
	protected static final String PAINT_LINK_PREFIX = "paint?id=";
	public static final String HTML_TEXT_BEGIN = "<html>";
	public static final String HTML_TEXT_END = "</html>";

	// External link identifier and URL links
	protected static final String [][] URL_primary = {
		{"ENSEMBL", "http://www.ensembl.org/YYY/Gene/Summary?g=XXX" },
		{"Ensembl", "http://www.ensembl.org/YYY/Gene/Summary?g=XXX" },
		{"EnsemblGenome", "http://www.uniprot.org/uniprot/?query=XXX&sort=score" },
		{"ENTREZ", "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id=XXX" },
		{"RefSeq", "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id=XXX" },
		{"FB", "http://flybase.org/reports/XXX" },
		{"FlyBase", "http://flybase.org/reports/XXX" },
		{"MGI", "http://www.informatics.jax.org/marker/XXX" },
		{"ZFIN", "http://zfin.org/cgi-bin/webdriver?MIval=aa-markerview.apg&OID=XXX" },
		{"WB", "http://wormbase.org/db/gene/gene?name=XXX;class=Gene" },
		{"PomBase", "http://www.pombase.org/spombe/result/XXX" },
		{"GeneDB_Spombe", "http://www.pombase.org/spombe/result/XXX" },
		{"AspGD", "http://www.aspergillusgenome.org/cgi-bin/locus.pl?locus=XXX" },
		{"dictyBase", "http://dictybase.org/gene/XXX" },
		{"TAIR", "http://www.arabidopsis.org/servlets/TairObject?type=locus&name=XXX" },
		{"CGD", "http://www.candidagenome.org/cgi-bin/locus.pl?dbid=XXX" },
		{"Xenbase", "http://www.xenbase.org/gene/showgene.do?method=display&geneId=XXX" },
		{"ECOLI", "http://biocyc.org/ECOLI/NEW-IMAGE?type=NIL&object=XXX" },
		{"SGD", "https://www.yeastgenome.org/locus/XXX" },
		{"RGD", "http://rgd.mcw.edu/tools/genes/genes_view.cgi?id=XXX" },
		{"AMIGO", "http://amigo.geneontology.org/amigo/term/XXX"},
		{"taxonomy", "http://www.ncbi.nlm.nih.gov/htbin-post/Taxonomy/wgetorg?mode=Undef&name=XXX&searchtype=1&lvl=3" },
		{"Reactome", "http://www.reactome.org/PathwayBrowser/#XXX"},
		{"mim", "http://www.ncbi.nlm.nih.gov/entrez/dispomim.cgi?id=XXX" },
		{"pdb", "http://www.rcsb.org/pdb/cgi/explore.cgi?pdbId=XXX" },
		{"pir", "http://pir.georgetown.edu/cgi-bin/nbrfget?uid=XXX" },
		{"prosite", "http://www.expasy.ch/cgi-bin/prosite-search-ac?XXX" },
		{"uniprot", "http://www.uniprot.org/uniprot/XXX" },
		{"UniProtKB", "http://www.uniprot.org/uniprot/?query=XXX" },
		{"GeneID", "http://www.uniprot.org/uniprot/?query=XXX&sort=score"},
//		{"UniProtKB", "http://www.textpresso.org/cgi-bin/celegans/query?textstring=XXX" },
//		{"uniprot", "http://www.textpresso.org/cgi-bin/celegans/query?textstring=XXX" },
		{"wormpep", "http://www.sanger.ac.uk/cgi-bin/Projects/C_elegans/wormpep_fetch.pl?what=protein&type=exact&entry=XXX" },
		{"maize_db", "http://www.maizegdb.org/cgi-bin/displaygprecord.cgi?id=XXX" },
		{"embl", "http://www.ebi.ac.uk/cgi-bin/emblfetch?style=html&id=XXX&Submit=Go" },
		{"uniprot", "http://www.pir.uniprot.org/cgi-bin/upEntry?id=XXX" },
		{"hssp", "http://srs.ebi.ac.uk/srs7bin/cgi-bin/wgetz?-e+[hssp-ID:XXX]" },
		{"pantree", "http://pantree.org/node/annotationNode.jsp?id=XXX" },
		{"PANTHER", "http://pantree.org/node/annotationNode.jsp?id=XXX" },
		{"pfam", "http://www.sanger.ac.uk/cgi-bin/Pfam/getacc?XXX" },
		{"prints", "http://umber.sbs.man.ac.uk/cgi-bin/dbbrowser/PRINTS/DoPRINTS.pl?cmd_a=Display&qua_a=none&fun_a=Text&qst_a=XXX" },
		{"prodom", "http://protein.toulouse.inra.fr/prodom/current/cgi-bin/request.pl?question=DBLI&query=XXX" },
		{"pirsf", "http://pir.georgetown.edu/cgi-bin/ipcSF?id=XXX" },
		{"WormBase", "http://www.wormbase.org/species/c_elegans/gene/XXX" },                
		{"EcoCyc", "http://biocyc.org/ECOLI/NEW-IMAGE?type=NIL&object=XXX" },
		{"MetaCyc", "http://metacyc.org/META/NEW-IMAGE?type=NIL&object=XXX&redirect=T" },
		{"Gene", "https://www.google.com/search?q=XXX" },

	};

	// External link identifier and URL links
	protected static final String [][] URL_references = {
		{"ENTREZ", "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id=XXX" },
		{"RefSeq", "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=protein&id=XXX" },
		{"FB", "http://flybase.org/reports/XXX" },
		{"MGI", "http://www.informatics.jax.org/searches/accession_report.cgi?id=XXX" },
		{"ZFIN", "http://zfin.org/cgi-bin/webdriver?MIval=aa-pubview2.apg&OID=XXX" },
		{"WB", "http://wormbase.org/db/misc/paper?name=XXX;class=Paper" },
		{"dictyBase", "http://dictybase.org/db/cgi-bin/feature_page.pl?primary_id=XXX" },
		{"TAIR", "http://www.arabidopsis.org/servlets/TairObject?accession=XXX" },
		{"ECOLI", "http://biocyc.org/ECOLI/NEW-IMAGE?type=NIL&object=XXX" },
		{"SGD_REF", "https://www.yeastgenome.org/locus/XXX" },
		{"RGD", "http://rgd.mcw.edu/tools/references/references_view.cgi?id=XXX" },
		{"PMID", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=XXX" },
		{"medline", "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=PubMed&list_uids=XXX&dopt=Abstract" },
                {"WormBase", "http://www.wormbase.org/species/c_elegans/gene/XXX" }
	};
	private static final Logger LOG = Logger.getLogger(HTMLUtil.class);

	// constructor
	public HTMLUtil() {
	}

	public static void setHyperlinkField(GeneNode node, HyperlinkLabel field, String label_txt) {
		String panelText = HTML_TEXT_BEGIN;
		String guts = getLinkForDB(node, label_txt, URL_primary);

		String id = Constant.STR_EMPTY;
		if (node != null && label_txt != null)
			if (label_txt.equals(GeneTableModel.ACC_COL_NAME))
				id = node.getSeqId();
			else if (label_txt.equals(GeneTableModel.PERMNODEID_COL_NAME))
				id = node.getPersistantNodeID();
			else {
				if (node.getDatabase().equals(GOConstants.PANTHER_DB)) {
					id = node.getPersistantNodeID();
				} else {
					id = node.getDatabaseID();
				}
			}

		// will make this linkable - internal & external
		// eventually, get smart and enable adding ontology!
		if (guts != null) {
			panelText += "<a href=\"" + guts + "\">" + id + "</a>";
			field.setToolTipText(guts);
			field.setFont(Preferences.inst().getFont());

		} else {
			field.setToolTipText("no link");
			panelText += id;
		}
		panelText += HTML_TEXT_END;
		field.setText(panelText);
	}



	public static HyperlinkLabel makeHyperlinkField(TermHyperlinkListener termHyperlinkListener){
		HyperlinkLabel field = new HyperlinkLabel();
		field.setEnabled(true);
		if (null != termHyperlinkListener) {
			field.addHyperlinkListener(termHyperlinkListener);
		}
		return field;
	}

	private static String getLinkForDB(GeneNode node, String label_txt, String [][] URL_array) {
		String id = null;
		String db = null;
		if (node != null && label_txt != null) {
			if (label_txt.equals(GeneTableModel.ACC_COL_NAME)) {
				id = node.getSeqId();
				db = node.getSeqDB();
				if (id != null && id.length() > 0) {
//					if (id.startsWith("ENS")) {
//						db = "ENSEMBL";
//					} else if (id.indexOf('_') >= 0) {
//						db = "ENTREZ";
//					} else {
//						db = "uniprot";
//					}
				}
			} else if (label_txt.equals(GeneTableModel.PERMNODEID_COL_NAME)) {
				id = node.getPersistantNodeID();
				db = "pantree";
			}
			else {
				id = node.getDatabaseID();
				db = node.getDatabase();
			}
		}
		if ((id != null && id.length() > 0) && (db != null && db.length() > 0)) {
			String temp_url = getURLLinks(id, db, URL_array);
			/*
			 * Special case for ENSEMBL which includes the species as an integral part of the URL
			 */
			String url = temp_url;
			if (temp_url != null && temp_url.indexOf(SPECIES_SPOT) > 0) {
				Pattern pattern = Pattern.compile(SPECIES_SPOT);
				String species = node.getSpeciesLabel();
				String [] two = species.split(" ");
				if (two.length > 1) {
					species = two[0] + '_' + two[1];
				}
				// Replace all occurrences of pattern in input
				Matcher matcher = pattern.matcher(temp_url);
				url = matcher.replaceAll(species);

				if (id.charAt(6) == 'P') {
					temp_url = url;
					pattern = Pattern.compile("g=");
					matcher = pattern.matcher(temp_url);
					url = matcher.replaceAll("p=");
				}
			}
			return url;
		}
		else 
			return null;
	}

	private static String getURLLinks(String id, String DBname, String [][] URL_array) {
		if (0 == id.length() || DBname.length() == 0) {
			return null;
		}
		if (DBname.equals("Accession")) {
			if (id.startsWith("ENS")) {
				DBname = "ENSEMBL";
			} else if (id.indexOf('_') >= 0) {
				DBname = "ENTREZ";
			} else {
				DBname = "uniprot";
			}
		}
		String url = null;
		for (int i = 0; i < URL_array.length && url == null; i++) {
			if (URL_array[i][0].equals(DBname)) {
				String template = URL_array[i][1];
				// Compile regular expression
				Pattern pattern = Pattern.compile(REPLACE_STRING);
				// Replace all occurrences of pattern in input
				Matcher matcher = pattern.matcher(template);
				url = matcher.replaceAll(id);
			}
		}
		return url;
	}

	public static String getHTML(String DBname, String accession, boolean basic) {
		return getHTML(DBname, accession, accession, basic);
	}

	public static String getURL(String DBname, String accession, boolean basic) {
		if (basic)
			return getURLLinks(accession, DBname, URL_primary);
		else
			return getURLLinks(accession, DBname, URL_references);
	}

	private static String getHTML(String DBname, String acc, String name, boolean basic) {
		String link;
		if (basic)
			link = getURLLinks(acc, DBname, URL_primary);
		else
			link = getURLLinks(acc, DBname, URL_references);

		String html = HTML_TEXT_BEGIN;
		if (link != null) {
			html += "<a href=\"" + link + "\">" + DBname + ":" + name + "</a>";
		} else {
			html += (DBname == null ? "" : DBname + ":") + name;
		}
		html += HTML_TEXT_END;

		return html;
	}

	public static void bringUpInBrowser(String text) {
		URL url;
		try {
			url = new URL(text);
		} catch (Exception e) {
			LOG.warn("could not create url from \"" + text + "\"");
			return;
		}
		// LOG.debug("got url " + text);
		bringUpInBrowser(url);
	}
        
    public static void bringUpInBrowser(BrowserLauncher bl, String url) {
        BrowserLauncher tmp = bl;
        if (null == tmp) {
            try {
                tmp = new BrowserLauncher();
            } catch (Exception e) {

            }
        }
        if (null != tmp) {
            try {
                tmp.openURLinBrowser(url);
                return;
            } catch (Exception e) {

            }
        }
        // Use new class
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url.toString()));
            }
            else {
                System.out.println("Can't open browser, Desktop is not supported");
            }
        } catch (Exception e) {
            System.out.println("Could not open browser using desktop browse functionality");
            e.printStackTrace();
        }
    }

	public static void bringUpInBrowser(URL url) {
		if (url == null) {
                    return;
                }

		try {
//                                    throw new Exception();
			BrowserLauncher bl = new BrowserLauncher(); // no logger
			bl.openURLinBrowser(url.toString());
                        return;
			//			BrowserLauncherRunner br = new BrowserLauncherRunner(bl,
			//					url.toString(), null);
			//			new Thread(bl).start();
		} catch (BrowserLaunchingInitializingException be) {
			LOG.error("cant launch browser ", be);
		} catch (UnsupportedOperatingSystemException ue) {
			LOG.error("cant launch browser ", ue);
		}
                catch (Exception e) {
                    LOG.error("Cannot launch browser ", e);
                    //e.printStackTrace();
                }
                
                // Try something else
                try {
                    if(Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(url.toString()));
                    }
                }
                catch(Exception e) {
                    System.out.println("Could not open browser using desktop browse functionality exception " + e.getMessage() + " has been returned");
                    //e.printStackTrace();
                }
	}

	public static String displayPropertiesToCSS(Font font, Color fg) {
		StringBuffer rule = new StringBuffer("body {");
		if (font != null) {
			rule.append(" font-family: ");
			rule.append(font.getFamily());
			rule.append(" ; ");
			rule.append(" font-size: ");
			rule.append(font.getSize());
			rule.append("pt ;");
			if (font.isBold()) {
				rule.append(" font-weight: 700 ; ");
			}
			if (font.isItalic()) {
				rule.append(" font-style: italic ; ");
			}
		}
		if (fg != null) {
			rule.append(" color: #");
			if (fg.getRed() < 16) {
				rule.append('0');
			}
			rule.append(Integer.toHexString(fg.getRed()));
			if (fg.getGreen() < 16) {
				rule.append('0');
			}
			rule.append(Integer.toHexString(fg.getGreen()));
			if (fg.getBlue() < 16) {
				rule.append('0');
			}
			rule.append(Integer.toHexString(fg.getBlue()));
			rule.append(" ; ");
		}
		rule.append(" }");
		return rule.toString();
	}

	public static boolean isInternalLink(HyperlinkEvent e) {
		return (e.getURL() == null && e.getDescription().startsWith(PAINT_LINK_PREFIX));
	}

	/** extracts id from link, returns null if fails */
	public static String getIdFromHyperlink(HyperlinkEvent e) {
		String desc = e.getDescription();
		if (desc == null || desc.equals("")) return null;
		String id = getIdFromHyperlinkDesc(desc);
		return id;
	}

	/** extract id from hyperlink description string */
	private static String getIdFromHyperlinkDesc(String desc) {
		return desc.substring(PAINT_LINK_PREFIX.length());
	}

}
