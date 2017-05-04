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

import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.paint.gui.PaintTable;

public class TableUtil {
	/**
	 * 
	 */

	public TableUtil(){
	}
	
	public static void setColumnWidths(PaintTable grid, int col_count, FontMetrics fm, TableColumnModel colModel) {
		Insets insets = new DefaultTableCellRenderer().getInsets();
		for (int i = 0; i < col_count; i++) {
			int optimalColumnWidth = 0;
			/*
			 * Fixed this so that it works generally for any column that is just an icon
			 * e.g. other homology programs, etc.
			 */
			if (grid.isSquare(i)) {
				optimalColumnWidth = fm.getHeight();
			}
//			Set column width to max size required to fit text                        
			else {
				for (int j = 0; j < grid.getRowCount(); j++) {
					String value = grid.getTextAt(j, i);
					if (null == value) {
						value = "";
					}
					int optimalCellWidth = fm.stringWidth(value) + insets.left + insets.right + 2;
					optimalColumnWidth = Math.max(optimalColumnWidth, optimalCellWidth);
				}
			}
			TableColumn col = colModel.getColumn(i);
			//Get the column at index columnIndex, and set its preferred width.
			col.setPreferredWidth(optimalColumnWidth);
			col.setWidth(optimalColumnWidth);
		}
	}
	
}