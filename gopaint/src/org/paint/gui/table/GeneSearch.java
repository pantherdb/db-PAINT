/**
 * Copyright 2022 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.paint.gui.table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;


public class GeneSearch {

	private static GeneSearch singleton;

	private static Logger log = Logger.getLogger(GeneTable.class);

	private GeneSearch() {
	}

	public static synchronized GeneSearch inst() {
		if (singleton == null) {
			singleton = new GeneSearch();
		}
		return singleton;
	}

	public List<GeneNode> search(List<GeneNode> all_nodes, String searchStr) {
		List<GeneNode> matches = new ArrayList<GeneNode> ();
		if (all_nodes != null && searchStr != null && !searchStr.equals("")) {
			if (searchStr.charAt(0) == '*')
				searchStr = searchStr.length() > 1 ? searchStr.substring(1) : "";
			if (searchStr.length() > 0 && searchStr.endsWith("*"))
				searchStr = searchStr.length() > 1 ? searchStr.substring(0, searchStr.length() - 1) : "";
			Pattern p = Pattern.compile(".*"+searchStr+".*", Pattern.CASE_INSENSITIVE);
			for (int i = 0; i < all_nodes.size(); i++) {
				GeneNode node = all_nodes.get(i);
				boolean matched = false;
				matched = check4match(node.getSeqId(), p);
				if (!matched) {
					matched = check4match(node.getNodeLabel(), p);
				}			
				if (!matched) {
					matched = check4match(node.getDatabaseID(), p);
				}
				if (!matched) {
					matched = check4match(node.getSeqId(), p);
				}
				if (!matched) {
					matched = check4match(node.getSeqName(), p);
				}
				if (!matched) {
					matched = check4match(node.getPersistantNodeID(), p);
				}
				if (!matched) {
					matched = check4match(node.getSpeciesLabel(), p);
				}
				if (!matched) {
					matched = check4match(node.getDatabase(), p);
				}
				if (!matched) {
					matched = check4match(node.getSeqDB(), p);
				}
				if (!matched) {
					matched = check4match(node.getDescription(), p);
				}
//				if (!matched && node.getGeneProduct() != null) {
//					Set<Association> associations = node.getGeneProduct().getAssociations();
//					if (associations != null) {
//						for (Association assoc : associations) {
//							Set<Evidence> evi_set = assoc.getEvidence();
//							if (evi_set != null) {
//								for (Evidence evi : evi_set) {
//									matched |= check4match(evi.getDbxref().getAccession(), p);
//								}
//							}
//						}
//					}
//				}
				if (matched) {
					matches.add(node);
				}
			}
		}
		return matches;
	}

	private boolean check4match(String value, Pattern p) {
		boolean matched = false;
		if (value != null) {
			Matcher m = p.matcher(value);
			matched = m.matches();
		}
		return matched;
	}
}
