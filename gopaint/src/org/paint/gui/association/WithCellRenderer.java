package org.paint.gui.association;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.log4j.Logger;
import org.bbop.swing.ExtensibleLabelUI.Renderer;
import org.bbop.swing.HyperlinkLabel;
import org.geneontology.db.model.Term;
import org.paint.config.Preferences;
import org.paint.util.RenderUtil;


public class WithCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	protected static Logger log = Logger.getLogger(WithCellRenderer.class);
	private boolean selected;
	private Color bg_color;
	private HyperlinkLabel with;

	public WithCellRenderer() {
		super();
		setOpaque(true);
	}

	@SuppressWarnings("unchecked")
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		selected = isSelected;
		Preferences prefs = Preferences.inst();
		setFont(prefs.getFont());
		setForeground(prefs.getForegroundColor());
		Term term = ((AssociationsTableModel) table.getModel()).getTermForRow(row);
		bg_color = RenderUtil.getAspectColor(term.getCv());
		setBackground(bg_color);

		Set<HyperlinkLabel> withs = (Set<HyperlinkLabel>) value;
		if (withs != null)
			with = withs.iterator().next();

		return this;
	}	

	@Override
	public void paint(Graphics g) {
		Rectangle bounds = getBounds();
		Rectangle local_bounds = new Rectangle(0, 0, bounds.width, bounds.height);
		RenderUtil.paintBorder(g, local_bounds, bg_color, selected);
		if (with != null) {
			Renderer r = (Renderer) with.getClientProperty(BasicHTML.propertyKey);
			r.paint(g, local_bounds);
		}
	}

}
