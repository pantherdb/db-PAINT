package org.paint.gui.association;

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
import org.geneontology.db.model.Term;
import org.paint.config.Preferences;
import org.paint.util.RenderUtil;

public class HyperlinkCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(HyperlinkCellRenderer.class);

	protected HyperlinkLabel label;
	private Color bg_color;
	private boolean selected;
	
	public HyperlinkCellRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		selected = isSelected;
		Preferences dp = Preferences.inst();
		setFont(dp.getFont());
		setForeground(dp.getForegroundColor());
		if (value != null) {
			label = (HyperlinkLabel) value;
			String tip = label.getToolTipText();
			setToolTipText(tip);
			UIManager.put("ToolTip.foreground", dp.getForegroundColor());
			ToolTipManager.sharedInstance().setDismissDelay(999999999);
		}
		Term term = ((AssociationsTableModel) table.getModel()).getTermForRow(row);
		bg_color = RenderUtil.getAspectColor(term.getCv());
		return this;
	}
	
	@Override
	public void paint(Graphics g) {
		Rectangle bounds = getBounds();
		Rectangle local_bounds = new Rectangle(0, 0, bounds.width, bounds.height);
		RenderUtil.paintBorder(g, local_bounds, bg_color, selected);
		if (label != null) {
			Renderer r = (Renderer) label.getClientProperty(BasicHTML.propertyKey);
			r.paint(g, local_bounds);
		}
	}

}
