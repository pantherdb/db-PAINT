package org.paint.gui.association;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.bbop.swing.ScaledIcon;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.Evidence;
import org.geneontology.db.model.Term;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.go.GO_Util;
import org.paint.util.RenderUtil;

public class ECOCellRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ScaledIcon scaledIcon = new ScaledIcon(null);
	private boolean selected;
	private Color bg_color;
	
	public ECOCellRenderer() {
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {

		selected = isSelected;
		Evidence evidence = ((AssociationsTableModel) table.getModel()).getEvidenceForRow(row);
		Association assoc = evidence.getAssociation();
		GeneNode node = GO_Util.inst().getGeneNode(assoc.getGene_product());

		Icon icon = null;
		String label = evidence.getCode();
		if (assoc.isMRC()) {
			icon = Preferences.inst().getIconByName("paint");
		} else {
			if (node != null) {
				if (assoc.isNot()) {
					icon = Preferences.inst().getIconByName("not");
				} else if (GO_Util.inst().isExperimental(assoc)) {
					icon = Preferences.inst().getIconByName("exp");
				} else {
					icon = Preferences.inst().getIconByName("inherited");
				}
			} else {
				label = "bug, please report database identifier.";
			}
		}
		setText (label);
		scaledIcon.setIcon(icon);
		if (scaledIcon != null) {
			scaledIcon.setDimension(15);
			setIcon(scaledIcon);
		} else {
			setIcon(null);
		}
		Term term = assoc.getTerm();
		bg_color = RenderUtil.getAspectColor(term.getCv());
		setBackground(bg_color);
		return this;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	 	RenderUtil.paintBorder(g, new Rectangle(0, 0, this.getWidth(), this.getHeight()), null, selected);
	}

}
