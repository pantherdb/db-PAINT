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
package org.paint.gui.table;

import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeStaticInfo;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.paint.datamodel.GeneNode;
import org.paint.util.GeneNodeUtil;

public class GeneTableModel extends AbstractGeneTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int currentRow;
	protected Vector<GeneNode> contents = new Vector<GeneNode>();

	public static final String ACC_COL_NAME = "Accession";
	public static final String ORTHO_COL_NAME = "O";
	public static final String DB_COL_NAME = "Database";
	public static final String DBID_COL_NAME = "ID";
	public static final String SYMB_COL_NAME = "Name";
	public static final String SPEC_COL_NAME = "Species";
	public static final String DESC_COL_NAME = "Description";
	public static final String PERMNODEID_COL_NAME = "Permanent Tree ID";
	public static final String STR_EMPTY = "";

	protected static final String[] column_headings = {
		ORTHO_COL_NAME, 
		ACC_COL_NAME, 
		DB_COL_NAME,
		DBID_COL_NAME,
		SYMB_COL_NAME,
		SPEC_COL_NAME, 
		PERMNODEID_COL_NAME,
		DESC_COL_NAME,		
	};

	protected static Logger log = Logger.getLogger(GeneTableModel.class);

	public GeneTableModel() {
		super();
	}

	public GeneTableModel(List<GeneNode> orderedNodes) {
            reorderRows(orderedNodes);

	}

//	/**
//	 * The purpose of this function is to allow the two different table classes (GO and nonGO) to be initialized appropriately
//	 */
//	private void parseData(Vector<Vector<String>> data) {
//		String progressMessage = "Parsing genes";
//		PantherParseUtil parser = PantherParseUtil.inst();
//		for (int i = 1; i < data.size(); i++) {
//			fireProgressChange(progressMessage, (int)(((double)i) / data.size() * 100));
//			Vector<String> row = data.elementAt(i);
//			parser.parseAttributeRow (row, 0);
//		}
//		fireProgressChange(progressMessage, 100);
//	}

	public void reorderRows (List<GeneNode>  node_list) {
		GeneNodeUtil.inst().setVisibleRows(node_list, contents);
	}

	public int getColumnCount() {
		return column_headings.length;
	}

	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the last column would contain text ("true"/"false"),
	 * rather than a check box.
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		if (getValueAt(0, c) == null) {
			return String.class;
		}
		Class<?> check = getValueAt(0, c).getClass();
		if (null == check) {
			log.debug("Table returning null for column " + c);
		}
		return check;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return column_headings[columnIndex];
	}

	public int getRowCount() {
		if (contents != null)
			return contents.size();
		else
			return 0;
	}

	/**
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return String, but never, ever null of the find breaks.
	 */
	public String getTextAt(int rowIndex, int columnIndex) {
		if (null == contents) {
			return null;
		}
		GeneNode node = contents.elementAt(rowIndex);
		String tag = column_headings[columnIndex];

		if (tag.equals(ORTHO_COL_NAME)) {
			return node.getOrthoMCL();
		} else if (tag.equals(ACC_COL_NAME)) {
			return node.getSeqId();
		} else if (tag.equals(DB_COL_NAME)) {
			return node.getDatabase();
		} else if (tag.equals(DBID_COL_NAME)) {
			return node.getDatabaseID();
		} else if (tag.equals(SYMB_COL_NAME)) {
			if (node.getGeneProduct() != null)
				return node.getGeneProduct().getSymbol();
			else
				return node.getSeqName();
		} else if (tag.equals(SPEC_COL_NAME)) {
			return node.getSpeciesLabel();
		} else if (tag.equals(DESC_COL_NAME)) {
			Node n = node.getNode();
                        if (null != n) {
                            NodeStaticInfo sni = n.getStaticInfo();
                            if (null != sni) {
                                return sni.getDefinition();
                            }
                        }
		} else if (tag.equals(PERMNODEID_COL_NAME)) {
                        Node n = node.getNode();
                        if (null != n) {
                            NodeStaticInfo sni = n.getStaticInfo();
                            if (null != sni) {
                                //System.out.println("Public id is " + sni.getPublicId());
                                return sni.getPublicId();
                            }
                        }
		}
		return STR_EMPTY;  
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (null == contents) {
			return null;
		}
		String tag = column_headings[columnIndex];
		GeneNode node = contents.elementAt(rowIndex);
		if (tag.equals(ACC_COL_NAME)) {
			return node.getAccLabel();
		} else if (tag.equals(DBID_COL_NAME)) {
			return node.getModLabel();
		} else if (tag.equals(ORTHO_COL_NAME)) {
			return node.getOrthoCell();
		} else if (tag.equals(PERMNODEID_COL_NAME)) {
			return node.getPermaCell();
		} else {
			return getTextAt(rowIndex, columnIndex);
		}
	}

	public void resetHiddenRows() {
		for (int i = 0; i < contents.size(); i++) {
			GeneNode row = contents.elementAt(i);
			row.setVisible(false);
		}
	}

	public void setVisibleRows(List<GeneNode> visibleNodes) {
		for (int i = 0; i < contents.size(); i++) {
			GeneNode node = contents.get(i);
			boolean visible = visibleNodes.contains(node);
			node.setVisible(visible);
		}
	}

	public int getRow(GeneNode dsn) {
		try {
			return contents.indexOf(dsn);
		} catch (NullPointerException e) {
			System.out.println("Could not find gene " + dsn.getSeqName() + " in contents");
			return -1;
		}
	}

	public GeneNode getNode(int row) {
		if (row >= contents.size()) {
			System.out.println("Asking for row " + row + " which is > than the number of rows (" + contents.size() + ")");
			return null;
		} else {
			GeneNode node = contents.elementAt(row);
			return node;
		}

	}

	public boolean isSquare(int column) {
		return getColumnName(column).equals(ORTHO_COL_NAME);
	}

//	private static void fireProgressChange(String message, int percentageDone) {
//		ProgressEvent event = new ProgressEvent(GeneTableModel.class, message, percentageDone);
//		EventManager.inst().fireProgressEvent(event);
//	}

}