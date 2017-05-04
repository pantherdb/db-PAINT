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


package org.paint.go;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.geneontology.db.model.Species;
import org.obo.datamodel.LinkedObject;
import org.paint.datamodel.GeneNode;
import org.paint.gui.ErrorManager;

/**
 * @author suzi
 *
 */
public class TaxonChecker {

	private static TaxonChecker taxon_check;
	//	private static final String TAXON_SERVER_URL = "http://localhost:9999/isClassApplicableForTaxon?format=txt&idstyle=obo";
	//	private static final String TAXON_SERVER_URL = "http://toaster.lbl.gov:9999/isClassApplicableForTaxon?format=txt&idstyle=obo";
	//	id=GO:0007400&id=GO:0048658&id=GO:0090127&taxid=NCBITaxon:3702&taxid=NCBITaxon:9606&
	private static final String TAXON_SERVER_URL = "http://toaster.lbl.gov:9999/isClassApplicableForTaxon?format=txt&idstyle=obo";

	private boolean not_running = false;

	private static Logger log = Logger.getLogger(TaxonChecker.class);

	private TaxonChecker() {
	}

	public static TaxonChecker inst() {
		if (taxon_check == null) {
			taxon_check = new TaxonChecker();
		}
		return taxon_check;
	}
        
        public boolean checkTaxons(GeneNode node, String goId) {
            boolean okay = true;
            
                StringBuffer taxon_query = new StringBuffer(TAXON_SERVER_URL + "&id=" + goId);
		okay = addTaxIDs (node, taxon_query);
		if (okay) {
			URL servlet;
			StringBuffer taxon_reply = new StringBuffer();
			try {
				servlet = new URL(taxon_query.toString());
			}
			catch (MalformedURLException muex){
				//			muex.printStackTrace();
				log.error("Attempted to create URL: " + muex.getLocalizedMessage()  + " " + taxon_query);
				return false;
			}
			BufferedReader in;
			try {
				URLConnection conn = servlet.openConnection();
				conn.setConnectTimeout(1000); // 1 second timeout
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					taxon_reply.append(inputLine + ' ');
				}
				in.close();
				okay = !(taxon_reply.toString().contains("false"));
				if (!okay) {
					log.debug("Invalid taxon for term: " + goId + " " + taxon_reply);
				}
			} catch (IOException e1) {
				if (!not_running) {
					log.error("Attempted to open URL: " + e1.getLocalizedMessage() +  " " + taxon_query);
					ErrorManager.showWarning(
							"Apologies, but the taxon checker is currently unavailable.\n"
							+ "Please contact go-software -at- mailman.stanford.edu\n"
							+ "for assistance in investigating the problem.\n\n"
							+ " You can continue working, with due caution for taxa.", 
							"Taxon Check");
					not_running = true;
				}
                                okay = false;
			}
		} 
		return okay;

        }

	public boolean checkTaxons(GeneNode node, LinkedObject termObject) {
		boolean okay = true;
		if (not_running) {
			return okay;
		}
		StringBuffer taxon_query = new StringBuffer(TAXON_SERVER_URL + "&id=" + termObject.getID());
		okay = addTaxIDs (node, taxon_query);
		if (okay) {
			URL servlet;
			StringBuffer taxon_reply = new StringBuffer();
			try {
				servlet = new URL(taxon_query.toString());
			}
			catch (MalformedURLException muex){
				//			muex.printStackTrace();
				log.error("Attempted to create URL: " + muex.getLocalizedMessage()  + " " + taxon_query);
				return okay;
			}
			BufferedReader in;
			try {
				URLConnection conn = servlet.openConnection();
				conn.setConnectTimeout(1000); // 1 second timeout
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					taxon_reply.append(inputLine + ' ');
				}
				in.close();
				okay = !(taxon_reply.toString().contains("false"));
				if (!okay) {
					log.debug("Invalid taxon for term: " + termObject + " " + taxon_reply);
				}
			} catch (IOException e1) {
				if (!not_running) {
					log.error("Attempted to open URL: " + e1.getLocalizedMessage() +  " " + taxon_query);
					ErrorManager.showWarning(
							"Apologies, but the taxon checker is currently unavailable.\n"
							+ "Please contact go-software -at- mailman.stanford.edu\n"
							+ "for assistance in investigating the problem.\n\n"
							+ " You can continue working, with due caution for taxa.", 
							"Taxon Check");
					not_running = true;
				}
			}
		} else {
			okay = true;
		}
		return okay;
	}

	private boolean addTaxIDs(GeneNode node, StringBuffer taxon_query) {
		Species species = GO_Util.inst().getSpecies(node);
		if (species != null) {
			int taxon_id = species.getNcbi_taxa_id();
			if (taxon_id > 1) 
				taxon_query.append("&taxid=NCBITaxon:" + taxon_id);
			else 
				species = null;
		}
		return species != null;
	}

}
