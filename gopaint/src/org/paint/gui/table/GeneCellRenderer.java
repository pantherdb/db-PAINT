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
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.bbop.swing.ExtensibleLabelUI.Renderer;
import org.bbop.swing.HyperlinkLabel;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.util.RenderUtil;

public class GeneCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static Logger log = Logger.getLogger(GeneCellRenderer.class);

	private Color bg_color;
	private HyperlinkLabel label;
	private String text;
	private boolean selected;

	public GeneCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		selected = isSelected;
		GeneTableModel genes = (GeneTableModel) table.getModel();
		GeneNode gene = genes.getNode(row);
		bg_color = Preferences.inst().getBackgroundColor();
		if (isSelected)
			bg_color = RenderUtil.annotationStatusColor(gene, bg_color).brighter();
		if (value != null) {
			label = (HyperlinkLabel) value;
			String tip = label.getToolTipText();
			setToolTipText(tip);
			UIManager.put("ToolTip.foreground", Preferences.inst().getForegroundColor());
			ToolTipManager.sharedInstance().setDismissDelay(999999999);
			if (table.getColumnName(column).equals(GeneTableModel.PERMNODEID_COL_NAME)) {
				text = gene.getPersistantNodeID();
			} else {
				text = gene.getDatabaseID();
			}
		} else {
			text = "";
		}
		this.setText(text);
		return this;
	}

	@Override
	public void paint(Graphics g) {
		Rectangle bounds = getBounds();
		Rectangle local_bounds = new Rectangle(0, 0, bounds.width, bounds.height);
		RenderUtil.paintBorder(g, local_bounds, bg_color, selected);
		if (label != null) {
			Renderer r = (Renderer) label.getClientProperty(BasicHTML.propertyKey);
			if (r == null) {
				log.debug("Missing renderer");
				g.setColor(Preferences.inst().getForegroundColor());

				g.drawString(text, bounds.x, bounds.height - 3);
			}	
			else
				r.paint(g, local_bounds);
		}
	}
}