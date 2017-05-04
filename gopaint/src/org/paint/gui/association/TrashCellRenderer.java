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
import org.geneontology.db.model.Term;
import org.paint.config.Preferences;
import org.paint.util.RenderUtil;

public class TrashCellRenderer extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected ScaledIcon scaledIcon = new ScaledIcon(null);
	private boolean selected;
	private Color bg_color;
	
	public TrashCellRenderer() {
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
		selected = isSelected;
		Icon icon = null;
		setText("");
		boolean deletable = ((Boolean) value).booleanValue();

		if (deletable) {
			icon = Preferences.inst().getIconByName("trash");
		}
		scaledIcon.setIcon(icon);
		if (scaledIcon != null) {
			scaledIcon.setDimension(15);
			setIcon(scaledIcon);
		} else {
			setIcon(null);
		}
		Term term = ((AssociationsTableModel) table.getModel()).getTermForRow(row);
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
