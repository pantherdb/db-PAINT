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
import org.paint.gui.familytree.TreePanel;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.util.Utils;

public class SubFamilyUtil {
	private static final String MSG_INVALID_SF_ID = "Invalid subfamily id encountered ";
	private static final String MSG_DUPLICATE_AN_ID_ENCOUNTERED_FOR_SUBFAMILY = " Duplicate anotation id encountered for subfamily ";
	private static final String MSG_DUPLICATE_SF_ID_ENCOUNTERED_FOR_ANNOTATION_ID  = " Duplicate subfamily id encountered for annotation node ";

	private static final String MSG_SF_AN_INFO_IS_NULL = "Subfamily annotation node relationship information is null";
	private static final String MSG_SF_AN_INFO_INVALID = "Subfamily annotation node relationship information is invalid ";

	private static final Logger logger = Logger.getLogger(SubFamilyUtil.class.getName());

	public SubFamilyUtil() {
	}

	public static String[] saveSFToAN(TreePanel tree) {
		GeneNode root = tree.getRoot();
		Vector <String> sfList = new Vector<String>();
		generateSFToAN(root, sfList);
		String[] rtnArray = new String[sfList.size()];
		sfList.copyInto(rtnArray);
		return rtnArray;
	}

	private static void generateSFToAN(GeneNode node, Vector<String> sfList) {
		if (null == node) {
			return;
		}

		if (true == node.isSubfamily()) {
			StringBuffer sb = new StringBuffer();
			sb.append(node.getSubFamilyName());
			sb.append(Constant.SF_AN_INFO_SEPARATOR);
			sb.append(node.getSeqId());
			sb.append(GeneNodeUtil.NEWLINE);
			sfList.add(sb.toString());
		}
		List<GeneNode> children = node.getChildren();
		if (null == children) {
			return;
		}
		for (int i = 0; i < children.size(); i++) {
			GeneNode child = children.get(i);
			generateSFToAN(child, sfList);
		}
	}

	public static Hashtable<String, Vector<GeneNode>> getSubfamilyRelations(TreePanel tree) {
		Hashtable<String, Vector<GeneNode>> sfToLeafTbl = new Hashtable<String, Vector<GeneNode>>();
		List<GeneNode> nodes = tree.getAllNodes();

		for (int i = 0; i < nodes.size(); i++) {

			GeneNode node = nodes.get(i);
			if (false == node.isLeaf()) {
				continue;
			}
			GeneNode sfAncestor = getSubfamilyAncestor(node);
			if (null == sfAncestor) {
				//logger.error(nodeName + MSG_LEAF_NODE_DOES_NOT_HAVE_SF_ANCESTOR);
				continue;
			}
			String sfName = sfAncestor.getSubFamilyName();
			Vector <GeneNode> leaves = sfToLeafTbl.get(sfName);
			if (null == leaves) {
				leaves = new Vector<GeneNode>();
				sfToLeafTbl.put(sfName, leaves);
			}
			leaves.add(node);
		}
		return sfToLeafTbl;

	}

	/**
	 * Method declaration
	 *
	 *
	 * @param dsn
	 *
	 * @return
	 *
	 * @see
	 */
	public static GeneNode getSubfamilyAncestor(GeneNode node){
		if (null == node){
			return null;
		}
		if (node.isSubfamily()){
			return node;
		}
		return getSubfamilyAncestor(node.getParent());
	}

	/**
	 *
	 * @param sfAnInfo true if subfamily names confirm to subfamily naming standards
	 * @return Hashtable of annotation node ids to subfamily ids
	 */
	public static Hashtable<String, String> parseSfAnInfo(String[] sfAnInfo, boolean checkSFName) {
		if (null == sfAnInfo) {
			logger.error(MSG_SF_AN_INFO_IS_NULL);
			return null;
		}
		int length = sfAnInfo.length;
		Hashtable<String, String> AnSfTbl = new Hashtable<String, String>(length);
		Hashtable<String, String> sfTbl = new Hashtable<String, String>(length);
		for (int i = 0; i < length; i++) {
			String info = sfAnInfo[i];
			info = info.trim();
			String infoStr[] = Utils.tokenize(info, Constant.SF_AN_INFO_SEPARATOR);
			int infoLen = infoStr.length;
			if (infoLen >= Constant.SF_AN_INDEX_AN && infoLen >= Constant.SF_AN_INDEX_SF) {
				if (false == checkSFName) {
					AnSfTbl.put(infoStr[Constant.SF_AN_INDEX_AN], infoStr[Constant.SF_AN_INDEX_SF]);
				}
				else {

					// Check validity of subfamily id
					String sfName = infoStr[Constant.SF_AN_INDEX_SF];
					if (null == sfName || false == sfName.startsWith(Constant.NODE_SUBFAMILY_PREFIX)) {
						logger.error(MSG_INVALID_SF_ID + sfName);
						return null;
					}
					String numberPart = sfName.substring(Constant.NODE_SUBFAMILY_PREFIX_LENGTH, sfName.length());
					try {
						Integer.parseInt(numberPart);
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						logger.error(MSG_INVALID_SF_ID + sfName);
						return null;
					}

					// Repeat for annotation id
					String anId = infoStr[Constant.SF_AN_INDEX_AN];
					if (null == anId || false == anId.startsWith(Constant.NODE_ANNOTATION_PREFIX)) {
						logger.error(GeneNodeUtil.MSG_INVALID_AN_ID + anId);
						return null;
					}
					numberPart = anId.substring(Constant.NODE_ANNOTATION_PREFIX_LENGTH, anId.length());
					try {
						Integer.parseInt(numberPart);
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						logger.error(GeneNodeUtil.MSG_INVALID_AN_ID + anId);
						return null;
					}

					// Ensure all subfamily and annotaton node ids are unique
					String previousSfId = AnSfTbl.put(anId, sfName);
					if (null != previousSfId) {
						logger.error (anId + MSG_DUPLICATE_AN_ID_ENCOUNTERED_FOR_SUBFAMILY + previousSfId);
						return null;
					}
					if (null != sfTbl.put(sfName, sfName)) {
						logger.error(sfName + MSG_DUPLICATE_SF_ID_ENCOUNTERED_FOR_ANNOTATION_ID + anId);
						return null;
					}
				}
			}
			else {
				logger.error(MSG_SF_AN_INFO_INVALID + info);
				return null;
			}
		}
		return AnSfTbl;
	}

}
