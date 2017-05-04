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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.util.RenderUtil;


public class TextCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GeneNode node;
	private Color bg_color;
	private Color fg_color;
	private boolean selected;
	
//	private static Logger log = Logger.getLogger(TextCellRenderer.class);

	public TextCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		
		selected = isSelected;
		
		GeneTableModel genes = (GeneTableModel) table.getModel();
		
		node = genes.getNode(row);
		Preferences prefs = Preferences.inst();
		Font f = RenderUtil.getNodeFont(node);;
		setFont(f);
		
		fg_color = RenderUtil.annotationStatusColor(node, prefs.getForegroundColor());
		if (isSelected) {
			fg_color = prefs.getForegroundColor();
			bg_color = RenderUtil.annotationStatusColor(node, prefs.getBackgroundColor()).brighter();
		} else {
			fg_color = RenderUtil.annotationStatusColor(node, prefs.getForegroundColor());
			bg_color = prefs.getBackgroundColor();
		}
		this.setText((String) value);
		setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
		return this;
	}

	@Override
	public void paint(Graphics g) {
		String text = this.getText();
		Insets insets = getInsets();
		RenderUtil.paintBorder(g, new Rectangle(0, 0, getWidth(), getHeight()), bg_color, selected);
		int boxWidth = getWidth() - (insets.left + insets.right);
		g.setFont(getFont());
		g.setColor(fg_color);
		g.drawString(RenderUtil.formatText(g, insets, boxWidth, text, getFont()), 
				insets.left, 
				getHeight() - insets.bottom - 1);
	}

}