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

package org.paint.dataadapter;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.paint.config.PantherDbInfo;
import org.paint.datamodel.GeneNode;
import org.paint.util.PantherParseUtil;

import com.sri.panther.paintCommon.Constant;
import com.sri.panther.paintCommon.FixedInfo;
import com.sri.panther.paintCommon.util.Utils;


public class AttrTable {
	private static final String DELIMITER = "\t";
	private static final String DELIM_TAB = "\t";
	private static final String DELIM_QUOTE = "'";
	private static final String DELIM_NEW_LINE = "\n";

	private static final Logger log = Logger.getLogger(AttrTable.class);

	public static Vector<Vector<String>> parse(String[] tableContents) {
		if (null == tableContents) {
			return null;
		}

		if (0 == tableContents.length) {
			return null;
		}
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		String[] columns;
		Vector<String> modifiedCols;
		int numCols = Utils.tokenize(tableContents[0], DELIMITER).length;
		int i = 0;
		while (i < tableContents.length) {
			// Remove new line character from end
			if (tableContents[i].endsWith(DELIM_NEW_LINE)) {
				tableContents[i] = tableContents[i].substring(0, tableContents[i].length() - 1);
			}
			columns = Utils.tokenize(tableContents[i], DELIMITER);
			modifiedCols = removeQuotes(columns);
			if (numCols != modifiedCols.size()) {
				log.error("num columns does not match, i is " + Integer.toString(i) + " num columns in title row is " + Integer.toString(numCols));
				return null;
			}
			i++;
			rows.addElement(modifiedCols);
		}
		return rows;
	}

	private static Vector<String> removeQuotes(String[] columns) {
		Vector<String> updated = new Vector<String>();
		for (int i = 0; i < columns.length; i++) {
			String contents = columns[i];
			if (contents.startsWith(DELIM_QUOTE)) {
				contents = contents.substring(1);
				if (contents.endsWith(DELIM_QUOTE)) {
					contents = contents.substring(0, contents.length() - 1);
				}
			}
			updated.addElement(contents);
		}
		return updated;
	}

	public static String[] saveAttrTable(Vector<GeneNode> contents, Vector<String> headings) {
		if (null == contents || 0 == contents.size()) {
			return null;
		}

		if (null == headings) {
			return null;
		}

		// Any Curation Go or Non-Go
		return saveAttrTableBasedOnHeadings(contents, headings);

	}

	private static String[] saveAttrTableBasedOnHeadings(Vector<GeneNode> contents, Vector<String> headings) {
		int numHeadings = headings.size();
		int numRows = contents.size();
		FixedInfo fixedInfo = PantherDbInfo.getFixedInfo();
		int seqColIndex = PantherParseUtil.inst().getColIndex(fixedInfo.getSeqColName());
		if (seqColIndex < 0) {
			seqColIndex = fixedInfo.getOrigSeqColIndex();
		}
		Vector<String> all = new Vector<String>(numRows + 1);       // + 1 for header

		// First add the header
		String headerStr = Utils.listToString(headings, DELIM_QUOTE, DELIM_TAB) + DELIM_NEW_LINE;
		all.addElement(headerStr);
		boolean isLeaf;

		StringBuffer columnBuff = new StringBuffer();            
		for (int i = 0; i < contents.size(); i++) {
			GeneNode gene = contents.elementAt(i);
			isLeaf = gene.isLeaf();
			columnBuff.setLength(0);
			for (int j = 0; j < numHeadings; j++) {
				//String header = headings.get(j);
				String value;
				// If it is not a leaf and this is the sequence id column, output the annotation id.  
				//if (true == header.equals(seqColName) && false == isLeaf) {
				if (j == seqColIndex && false == isLeaf) {
					value = gene.getSeqId();
				}
				else {
					value = gene.getAttrLookup(headings.get(j));
				}
				if (null == value) {
					value = Constant.STR_EMPTY;
				}
				columnBuff.append(DELIM_QUOTE);
				columnBuff.append(value);
				columnBuff.append(DELIM_QUOTE);
				if (j + 1 < numHeadings) {
					columnBuff.append(DELIM_TAB);
				}

			}
			columnBuff.append(DELIM_NEW_LINE);
			all.addElement(columnBuff.toString());
		}
		String[] returnArray = new String[all.size()];
		all.copyInto(returnArray);
		return returnArray;
	}

	public AttrTable() {
	}
}