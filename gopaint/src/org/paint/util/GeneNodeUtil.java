/**
 * Copyright 2023 University Of Southern California
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
package org.paint.util;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.Utils;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;
import org.paint.go.GOConstants;
import org.paint.main.PaintManager;


public class GeneNodeUtil {

	public static final String DELIM = ",();";

	protected static final String SEMI_COLON = ";";
	protected static final String OPEN_PAREN = "(";
	protected static final String CLOSE_PAREN = ")";
	protected static final String COMMA = ",";
	protected static final String COLON = ":";
	protected static final String OPEN_BRACKET = "[";
	protected static final String CLOSE_BRACKET = "]";
	protected static final String NEWLINE = "\n";
	protected static final String TAB = "\t";
	protected static final String SPACE = " ";
	protected static final String PLUS = "+";

	private static final String NODE_TYPE_ANNOTATION = "ID=";
	private static final int NODE_TYPE_ANNOTATION_LENGTH = NODE_TYPE_ANNOTATION.length();

	public static final String MSG_INVALID_AN_ID = "Invalid annotation id encountered ";

	public static final String ERROR_MSG_DATA_FOR_NON_AN = "Found data for non-existant annotation node ";

	public static final String STR_EMPTY = "";

	public static final Logger logger = Logger.getLogger(GeneNodeUtil.class);

	private static GeneNodeUtil singleton = null;
        
        public static final String DATABASE_PREFIX_MGI = "MGI:";        
        public static final String SPECIAL_CASE_TAIR = "TAIR";
        public static final String SPECIAL_CASE_MGI = "MGI";
        public static final String SPECIAL_CASE_SOURCES[] = {SPECIAL_CASE_TAIR, SPECIAL_CASE_MGI};     // These evidence sources contain extra ":=", etc


	public GeneNodeUtil() {
	}

	public static GeneNodeUtil inst() {
		if (singleton == null)
			singleton = new GeneNodeUtil();
		return singleton;
	}

	/**
	 *
	 * @param treeContents
	 * @param sfAn
	 * @return
	 */
	public GeneNode parseTreeData(String[] treeContents, HashMap<String, Node> nodeLookup, String familyId) {
		if (null == treeContents) {
			return null;
		}
		if (0 == treeContents.length) {
			return null;
		}
		// Modify, if there are no line returns
		if (1 == treeContents.length) {
			treeContents = Utils.tokenize(treeContents[0], SEMI_COLON);
		}

		GeneNode root = parseTreeString(treeContents[0]);

//		// Get subfamily to annotation node relationships
//		Hashtable<String, String> AnToSFTbl = null;
//		if (sfAn != null) {
//			AnToSFTbl = SubFamilyUtil.parseSfAnInfo(sfAn, true);
//			if (AnToSFTbl == null) {
//				return null;
//			}
//			String sfName = AnToSFTbl.get(root.getSeqId());
//			if (null != sfName) {
//				root.setSubFamilyName(sfName);
//				root.setIsSubfamily(true);
//			}
//		}
//		addSfInfoToMainTree(root, AnToSFTbl);

		for (int i = 1; i < treeContents.length; i++) {
			String line = treeContents[i];
			int index = line.indexOf(COLON);
			String anId = line.substring(0, index);
			GeneNode node = PaintManager.inst().getGeneByPaintId(anId);
			if (null == node) {
				logger.error(ERROR_MSG_DATA_FOR_NON_AN + anId);
				continue;
			}
//                        if (null != nodeLookup) {
//                            String lookupId = familyId + ":" + anId;
//                            Node n = nodeLookup.get(lookupId);
//                            node.setNode(n);
//                            node.setPersistantNodeID(n.getStaticInfo().getPublicId());
//                        }
			// minus 1 to trim the semi-colon off?
			PantherParseUtil.inst().parseIDstr(node, line.substring(index+1));
			/*
			 * Both database info and sequence info should now be set in this leaf node
			 */
//			PaintManager.inst().indexNode(node);
		}
		initNodeProperties(root, nodeLookup, familyId);

		return root;

	}

	private GeneNode parseTreeString(String s) {
		GeneNode node = null;
		GeneNode root = null;
		StringTokenizer st = new StringTokenizer(s, DELIM, true);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.equals(OPEN_PAREN)) {
				if (null == node) {
					/*
					 * The real root node, first one set
					 */
					node = new GeneNode(false);
					root = node;
				}
				else {
					GeneNode newChild = new GeneNode(false);
					List<GeneNode> children = node.getChildren();
					if (null == children) {
						children = new ArrayList<GeneNode>();
					}
					children.add(newChild);
					newChild.setParent(node);
					node.setChildren(children);
					/*
					 * Move down
					 */
					node = newChild;
					node.setExpanded(true);
				}
			}
			else if ((token.equals(CLOSE_PAREN)) ||
					(token.equals(COMMA)) ||
					(token.equals(SEMI_COLON))) {
				// Do nothing
			}
			else {
				int squareIndexStart = token.indexOf(OPEN_BRACKET);
				int squareIndexEnd = token.indexOf(CLOSE_BRACKET);
				if (0 == squareIndexStart) {
					String type = token.substring(squareIndexStart, squareIndexEnd + 1);
					/* 
					 * This is when the AN number is teased out
					 */
					setTypeAndId(type, node);
				}
				else {
					int index = token.indexOf(COLON);
					if (0 == index) {
						if (-1 == squareIndexStart) {
							node.setDistanceFromParent(Float.valueOf(token.substring(index+1)).floatValue());
						}
						else {
							node.setDistanceFromParent(Float.valueOf(token.substring((index+1), squareIndexStart)).floatValue());
							String type = token.substring(squareIndexStart, squareIndexEnd + 1);
							/* 
							 * This is when the AN number is teased out
							 */
							setTypeAndId(type, node); // this use to be included in setType itself
						}
						/*
						 * Move back up
						 */
						node = (GeneNode) node.getParent();
					} else if (index > 0) {
						GeneNode newChild = new GeneNode(false);
						if (-1 == squareIndexStart) {
							newChild.setDistanceFromParent(Float.valueOf(token.substring(index+1)).floatValue());
							setTypeAndId(token.substring(0, index), newChild); // this use to be included in setType itself
						}
						else {
							newChild.setDistanceFromParent(Float.valueOf(token.substring((index+1), squareIndexStart)).floatValue());
							String type = token.substring(squareIndexStart, squareIndexEnd + 1);
							/* 
							 * This is when the AN number is teased out
							 */
							setTypeAndId(type, newChild); // this use to be included in setType itself
						}
						List<GeneNode> children = node.getChildren();
						if (null == children) {
							children = new ArrayList<GeneNode>();
						}
						/*
						 * Add siblings to current node
						 */
						children.add(newChild);
						newChild.setParent(node);
						node.setChildren(children);
						node.setExpanded(true);
					}
				}
			}
		}
		return root;
	}

//	private void addSfInfoToMainTree(GeneNode node, Hashtable<String, String> AnToSFTbl) {
//		if (null == node) {
//			return;
//		}
//		String anId = node.getSeqId();
//		if (null != anId) {
//			String sfId = AnToSFTbl.get(anId);
//			if (null != sfId) {
//				node.setSubFamilyName(sfId);
//				node.setIsSubfamily(true);
//			}
//		}
//		List<GeneNode> v = node.getChildren();
//		if (null == v) {
//			return;
//		}
//		for (int i = 0; i < v.size(); i++) {
//			addSfInfoToMainTree(v.get(i), AnToSFTbl);
//		}
//
//	}

    private void initNodeProperties(GeneNode node, HashMap<String, Node> nodeLookup, String familyId) {
        if (null == node) {
            return;
        }
        String anId = node.getPaintId();
        if (null != nodeLookup) {
            String lookupId = familyId + ":" + anId;
            Node n = nodeLookup.get(lookupId);
            if (null != n) {
                node.setNode(n);
                node.setPersistantNodeID(n.getStaticInfo().getPublicId());
                String longGeneName = n.getStaticInfo().getLongGeneName();
                if (null != longGeneName) {
                    
                    String parts[] = longGeneName.split(Pattern.quote(Constant.STR_PIPE));
                    int length = parts.length;
                    if (length < 2) {
                        return;
                    }
                    //organism = parts[0];

                    // Gene part
                    String geneParts[] = parts[1].split(Constant.STR_EQUAL);
                    if (geneParts.length < 2) {
                        return;
                    }
                    String geneSource = geneParts[0];
                    String geneId = geneParts[1];

                    /*
                     * Check for special cases
                     * MGI id is of the form MOUSE|MGI=MGI=97788|UniProtKB=Q99LS3
                     */
                     if (true == Utils.search(SPECIAL_CASE_SOURCES, geneSource)) {
                        if (geneSource.equals(SPECIAL_CASE_MGI) && !geneSource.startsWith(DATABASE_PREFIX_MGI)) {
                            if (geneParts.length >= 3) {
                                geneId = DATABASE_PREFIX_MGI + geneParts[2];
                            }
                            else {
                                geneId = DATABASE_PREFIX_MGI + geneId;
                            }
                        }
                        if (geneSource.equals(SPECIAL_CASE_TAIR)) {
                            if (geneParts.length >= 3) {
                                geneId = geneParts[2];
                            }
                        }
                     }
                     node.setGeneSource(geneSource);
                     node.setGeneId(geneId);

                    // Protein part 
                    String proteinParts[] = parts[2].split(Constant.STR_EQUAL);
                    if (proteinParts.length < 2) {
                        return;
                    }
                    node.setProteinSource(proteinParts[0]);
                    node.setProteinId(proteinParts[1]);                    

                }
                NodeVariableInfo nvi = n.getVariableInfo();
                if (null != nvi && true == nvi.isPruned()) {
                    node.setPrune(true);
                }
            }
            else {
                System.out.println("Did not find node information for " + lookupId);
            }
        }
        else {
            System.out.println("Did not find any node information to match with tree");
        }
        node.setOriginalChildrenToCurrentChildren();
        List<GeneNode> children = node.getChildren();
        if (null == children) {
            node.setExpanded(false);
        } else {
            node.setExpanded(true);
            for (int i = 0; i < children.size(); i++) {
                initNodeProperties(children.get(i), nodeLookup, familyId);
            }
        }
    }

	public void setVisibleRows(List<GeneNode> node_list, List<GeneNode> contents) {
		contents.clear();
		contents.addAll(node_list);
	}

	private void setTypeAndId(String nodeType, GeneNode node) {
		if (null == nodeType) {
			return;
		}
		String annot_id;
		if (!nodeType.startsWith("AN")) {
			node.setType(nodeType);
			// collect the species while we're at it
			int index = nodeType.indexOf("S=");
			if (index >= 0) {
				int endIndex = nodeType.indexOf(COLON, index);
				if (-1 == endIndex) {
					endIndex = nodeType.indexOf(CLOSE_BRACKET);
				}
				String species = nodeType.substring(index + "S=".length(), endIndex);
                                node.setSpecies(species);
				node.addSpeciesLabel(species);
			}
			// now pick up the node name/id
			index = nodeType.indexOf(NODE_TYPE_ANNOTATION);
			if (index >= 0) {
				int endIndex = nodeType.indexOf(COLON, index);
				if (-1 == endIndex) {
					endIndex = nodeType.indexOf(CLOSE_BRACKET);
				}
				annot_id = nodeType.substring(index + NODE_TYPE_ANNOTATION_LENGTH, endIndex);
			} else {
				annot_id = null;
			}
		} else {
			annot_id = nodeType;
		}
		// now pick up the node name/id
		if (annot_id != null) {
			if (!annot_id.startsWith("AN"))
				logger.debug(annot_id + " isn't an AN number");
			if (node.getPaintId().length() > 0) {
				logger.debug(annot_id + "AN number is already set to " + node.getPaintId());
			}
			node.setPaintId(annot_id);            
//			PaintManager.inst().indexNode(node);
		}

	}
        
        
        public static void allDescendents(GeneNode gNode, List<GeneNode> nodeList) {
            if (null == gNode || null == nodeList) {
                return;
            }
            List<GeneNode> children = gNode.getChildren();
            if (null == children) {
                return;
            }
            for (GeneNode child: children) {
                nodeList.add(child);
                allDescendents(child, nodeList);
            }
        }
        
        public static void getAncestors(GeneNode gNode, List<GeneNode> ancestors) {
            if (null == gNode || null == ancestors) {
                return;
            }
            GeneNode parent = gNode.getParent();
            if (null != parent) {
                ancestors.add(parent);
                getAncestors(parent, ancestors);
            }
        }
        
        public static void allNonPrunedDescendents(GeneNode gNode, List<GeneNode> nodeList) {
            if (null == gNode || null == nodeList) {
                return;
            }
            Node n = gNode.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null != nvi && nvi.isPruned()) {
                return;
            }
            List<GeneNode> children = gNode.getChildren();
            if (null == children) {
                return;
            }
            for (GeneNode child: children) {
                Node cn = child.getNode();
                NodeVariableInfo childNvi = cn.getVariableInfo();
                if (null != childNvi && childNvi.isPruned()) {
                    continue;
                }                
                nodeList.add(child);
                allNonPrunedDescendents(child, nodeList);
            }
        }
        
        public static boolean inPrunedBranch(GeneNode gNode) {
            if (true == gNode.isPruned()) {
                return true;
            }
            GeneNode copy = gNode;
            return hasPrunedAncestor(copy);
        }
        
        private static boolean hasPrunedAncestor(GeneNode gNode) {
            
            GeneNode parent = gNode.getParent();
            if (null == parent) {
                return false;
            }
            if (true == parent.isPruned()) {
                return true;
            }
            return hasPrunedAncestor(parent);
        }
        
        public static List<GeneNode> getAllLeaves(List<GeneNode> list) {
            if (null == list) {
                return null;
            }
            ArrayList<GeneNode> rtnList = new ArrayList<GeneNode>();
            for (GeneNode gNode: list) {
                if (true == gNode.isLeaf()) {
                    rtnList.add(gNode);
                }
            }
            return rtnList;
        }
        
        public static boolean hasDirectAnnotation(GeneNode gNode) {
            Node n = gNode.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null ==  nvi) {
                return false;
            }
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList) {
                return false;
            }
            for (edu.usc.ksom.pm.panther.paintCommon.Annotation annot: annotList) {
                //edu.usc.ksom.pm.panther.paintCommon.Evidence e = annot.getEvidence();
                String evidenceCode = annot.getSingleEvidenceCodeFromSet();
                if (GOConstants.DESCENDANT_SEQUENCES_EC.equals(evidenceCode) || GOConstants.KEY_RESIDUES_EC.equals(evidenceCode) || GOConstants.DIVERGENT_EC.equals(evidenceCode)) {
                    return true;
                }
            }
            return false;
        }
        
        public static boolean hasAllPropagatedAnnotation(GeneNode gNode) {
            Node n = gNode.getNode();
            NodeVariableInfo nvi = n.getVariableInfo();
            if (null ==  nvi) {
                return false;
            }
            ArrayList<edu.usc.ksom.pm.panther.paintCommon.Annotation> annotList = nvi.getGoAnnotationList();
            if (null == annotList || 0 == annotList.size()) {
                return false;
            }
            for (edu.usc.ksom.pm.panther.paintCommon.Annotation annot: annotList) {
                //edu.usc.ksom.pm.panther.paintCommon.Evidence e = annot.getEvidence();
                String evidenceCode = annot.getSingleEvidenceCodeFromSet();
                if (GOConstants.DESCENDANT_SEQUENCES_EC.equals(evidenceCode) || GOConstants.KEY_RESIDUES_EC.equals(evidenceCode) || GOConstants.DIVERGENT_EC.equals(evidenceCode)) {
                    return false;
                }
            }
            return true;            
        }
        
        
//        public static boolean isTermValidForNode(GeneNode gn, String term) {
//            if (null == gn || null == term) {
//                return false;
//            }
//            PaintManager pm = PaintManager.inst();
//            TaxonomyHelper th = pm.getTaxonHelper();
//            if (null == th) {
//                return true;
//            }
//            return isTermValidForNode(gn, term, th);
//        }
//        
//        private static boolean isTermValidForNode(GeneNode gn, String term, TaxonomyHelper th) {
//            String species = gn.getCalculatedSpecies();
//            if (null == species) {
//                return false;
//            }           
//            boolean rtn = th.isTermValidForSpecies(term, species);
////            if (false == rtn) {
////                System.out.println("Taxonomy violation tern " + term + " not valid for species " + species + " for node " + gn.getNode().getStaticInfo().getPublicId());
////            }
//            return rtn;
//        }
        

}
