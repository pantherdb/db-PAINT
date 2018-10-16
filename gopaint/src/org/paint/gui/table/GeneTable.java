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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.bbop.swing.HyperlinkLabel;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.gui.FamilyViews;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneDataEvent;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.gui.event.NodeReorderListener;
import org.paint.gui.event.SubFamilyListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;
import org.paint.util.HTMLUtil;
import org.paint.util.TableUtil;

public class GeneTable extends JTable 
implements 
MouseListener,
GeneSelectListener, 
TermSelectionListener, 
SubFamilyListener, 
NodeReorderListener,
AspectChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ListSelectionModel geneSelectionModel;
	private boolean is_adjusting = false;

	private static Logger log = Logger.getLogger(GeneTable.class);

	public GeneTable() {
		super();

		this.setBackground(Preferences.inst().getBackgroundColor());
		this.setSelectionBackground(Preferences.inst().getSelectionColor());

		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(false);
		setAutoscrolls(false);

		setDefaultRenderer(String.class, new TextCellRenderer());
		setDefaultRenderer(HyperlinkLabel.class, new GeneCellRenderer());
		setDefaultRenderer(OrthoCell.class, new OrthoCellRenderer());

		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));

		EventManager manager = EventManager.inst();
		manager.registerGeneListener(this);
		manager.registerNodeReorderListener(this);
		manager.registerSubFamilyListener(this);
		manager.registerAspectChangeListener(this);
		manager.registerTermListener(this);

		geneSelectionModel = getSelectionModel();
		geneSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		addMouseListener(this);
		geneSelectionModel.addListSelectionListener(new GeneSelectionHandler(this));
		setSelectionModel(geneSelectionModel);

		Preferences user_settings = Preferences.inst();
		Font f = user_settings.getFont();

		this.setFont(f);

		//single cell selection
		setRowMargin(0);
		getTableHeader().setReorderingAllowed(false);
		setColumnSelectionAllowed(false);
		setRowSelectionAllowed(true);

	}

	public void setModel(GeneTableModel grid) {
		super.setModel(grid);
		if (grid != null) {
			TableUtil.setColumnWidths(grid, grid.getColumnCount(), getFontMetrics(getFont()), getColumnModel());
		}
	}

	/**
	 * Invoked when the mouse button has been clicked (pressed
	 * and released) on a component.
	 */
	public void mouseClicked(MouseEvent e) {
		int modifiers = e.getModifiers();
//                System.out.println("Mouse click event modifiers  " + modifiers);
		Point p = e.getPoint();
		int clickRow = rowAtPoint(p);
		int clickCol = columnAtPoint(p);

		GeneTableModel grid = (GeneTableModel) this.getModel();
		grid.getNode(clickRow).setSelected(true);
                if (SwingUtilities.isLeftMouseButton(e) || false == (SwingUtilities.isRightMouseButton(e) || e.isControlDown())) {
//                    System.out.println("Left click");                    
//		if ((modifiers & InputEvent.BUTTON1_MASK) != 0 &&
//				(modifiers      & InputEvent.BUTTON3_MASK) == 0) {

			Object o = getValueAt(clickRow, clickCol);
			if (o instanceof HyperlinkLabel) {
				HTMLUtil.bringUpInBrowser(((HyperlinkLabel)o).getToolTipText());
			}
		}
                else if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
                    System.out.println("Right click");
//		else if (InputEvent.BUTTON3_MASK == (modifiers & InputEvent.BUTTON3_MASK) ||
//				(((modifiers & InputEvent.BUTTON1_MASK) != 0 &&
//				(modifiers & InputEvent.BUTTON3_MASK) == 0) &&
//				(true == e.isMetaDown()))) {

			ListSelectionModel lsm = this.getSelectionModel();
			int min = lsm.getMinSelectionIndex();
			int max = lsm.getMaxSelectionIndex();
			log.debug("min index is " + min + " and max is " + max);

			if (clickCol < 0) {
				return;
			}
		}
	}

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed(MouseEvent e) {

	}

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased(MouseEvent e) {

	}

	/**
	 * Invoked when the mouse enters a component.
	 */
	public void mouseEntered(MouseEvent e) {

	}

	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited(MouseEvent e) {

	}

	/**
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
	 * paint the editor. The UI currently uses different techniques to
	 * paint the renderers and editors and overriding setBounds() below
	 * is not the right thing to do for an editor. Returning -1 for the
	 * editing row in this case, ensures the editor is never painted.
	 */
	@Override
	public int getEditingRow(){
		return (getColumnClass(editingColumn) == GeneTableModel.class) ? -1 : editingRow;
	}

	public void handleTermEvent(TermSelectEvent e) {
		GeneTableModel genes = (GeneTableModel) this.getModel();
		int total = genes.getRowCount();
		if (total > 0) {
			is_adjusting = true;
			ListSelectionModel lsm = this.getSelectionModel();
			lsm.clearSelection();
			for (int i = 0; i < total; i++) {
				GeneNode node = genes.getNode(i);
				if (node.isSelected()) {
					lsm.addSelectionInterval(i, i);
				}
			}
			GeneNode mrca = EventManager.inst().getAncestralSelection();
			if (mrca.isLeaf()) {
				int row = genes.getRow(mrca);
				if (row >= 0)
					scrollToVisible(row);
				else
					log.debug("Missing row for " + mrca.getSeqName());
			} else {
				scrollToVisible(mrca.getScreenRectangle());
			}
			is_adjusting = false;
		}		
	}

    public void handleGeneSelectEvent(GeneSelectEvent e) {
        if (e.getGenes() == null) {
            System.out.println("GeneTable: wierd, have a null");
        } else if (e.getSource() != this) {
            // Clear current selections
            ListSelectionModel lsm = this.getSelectionModel();
            lsm.clearSelection();
            GeneTableModel geneTableModel = (GeneTableModel) this.getModel();
            this.clearSelection();

            // Select
            int total = geneTableModel.getRowCount();
            if (total > 0) {
                List<GeneNode> selection = e.getGenes();

                if (selection != null && !selection.isEmpty()) {
                    for (Iterator<GeneNode> nodeIter = selection.iterator(); nodeIter.hasNext();) {
                        GeneNode node = nodeIter.next();
                        int row = geneTableModel.getRow(node);
                        if (row >= 0 && row < total) {
                            this.addRowSelectionInterval(row, row);
                        }
                                        //GeneNode node = e.getAncestor();
                        //TreePanel tree = PaintManager.inst().getTree();		
                        //setSelectedRows(lsm, tree, node);
                    }
                }
            }
        }
    }

	public void handleSubFamilyEvent (GeneDataEvent e) {
		GeneTableModel model = (GeneTableModel) this.getModel();
		model.fireTableDataChanged();
		repaint();
	}

	class GeneSelectionHandler implements ListSelectionListener {
		GeneTable table;

		public GeneSelectionHandler (GeneTable table) {
			this.table = table;
		}

		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting() && !is_adjusting) { 
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				int min_index = lsm.getMinSelectionIndex();

				if (!lsm.isSelectionEmpty() && min_index >= 0) {
					// Find out which indexes are selected.
					int max_index = lsm.getMaxSelectionIndex();
					GeneTableModel genes = (GeneTableModel) table.getModel();
					if (min_index == max_index) {
						GeneNode gene = genes.getNode(min_index);
						if (gene != null) {
							setSelectedRows(lsm, PaintManager.inst().getTree(), gene);
							ArrayList<GeneNode> selection = new ArrayList<GeneNode>();
							selection.add(gene);
							GeneSelectEvent ge = new GeneSelectEvent (table, selection, gene);
							EventManager.inst().fireGeneEvent(ge);
						}
					} else {
						/* 
						 * Now have to figure out the most recent common ancestor of these 2
						 * and expand the selection to any siblings
						 */
						TreePanel tree = PaintManager.inst().getTree();
						GeneNode min_gene = (genes.getNode(min_index));
						GeneNode max_gene = (genes.getNode(max_index));
						GeneNode mrca = tree.getMRCA(min_gene, max_gene);
						if (mrca == null) {
							/*
							 * This shouldn't be possible, but check to be sure
							 */
							log.error("Couldn't find ancestor of " + min_gene.getSeqId() + " and " + max_gene.getSeqId());
						}
						setSelectedRows(lsm, tree, mrca);
						List<GeneNode> selection = new ArrayList<GeneNode> ();
						tree.getDescendentList(mrca, selection);
						GeneSelectEvent ge = new GeneSelectEvent (table, selection, mrca);
						EventManager.inst().fireGeneEvent(ge);
					}
				}
			}
		}
	}

	private void setSelectedRows(ListSelectionModel lsm, TreePanel tree, GeneNode node) {
		is_adjusting = true;
		lsm.setValueIsAdjusting(true);
		int [] current_rows = getSelectedRows();
		GeneTableModel model = (GeneTableModel) getModel();
		if (current_rows.length > 0) {
			removeRowSelectionInterval(current_rows[0], current_rows[current_rows.length - 1]);
		}
		if (node.isExpanded() && !node.isPruned()) {
			GeneNode low_gene = tree.getTopLeafNode(node);
			int low_row = model.getRow(low_gene);
			GeneNode high_gene = tree.getBottomLeafNode(node);
			int high_row = model.getRow(high_gene);
			setRowSelectionInterval(low_row, high_row);
			scrollToVisible(node.getScreenRectangle());
		} else {
			int row = model.getRow(node);
			if (row >= 0 && row < getRowCount()) {
				setRowSelectionInterval(row, row);
				scrollToVisible(row);
			} else {
				log.debug("Row out of bounds: " + row);
			}
		}
		lsm.setValueIsAdjusting(false);
		is_adjusting = false;
	}

	public void handleNodeReorderEvent(NodeReorderEvent e) {
		GeneTableModel model = (GeneTableModel) this.getModel();
		model.reorderRows(e.getNodes());
		model.fireTableDataChanged();
	}

	@Override
	public void handleAspectChangeEvent(AspectChangeEvent event) {
		repaint();
	}

	// Assumes table is contained in a JScrollPane. Scrolls the 
	// cell (rowIndex, vColIndex) so that it is visible within the viewport. 
	public void scrollToVisible(int rowIndex) { 
		Rectangle row_rect = getCellRect(rowIndex, 0, true); 
		scrollToVisible(row_rect);
	}

	private void scrollToVisible(Rectangle rect) {
		if (!(getParent() instanceof JViewport)) { 
			return; 
		} 
		JViewport viewport = (JViewport)getParent(); 
		// This rectangle is relative to the table where the 
		// northwest corner of cell (0,0) is always (0,0).

		Rectangle visible = viewport.getViewRect();

		if (visible.y <= rect.y && (visible.y + visible.height) >= (rect.y + rect.height))
			return;

		Point point_of_view;
		if ((rect.y + rect.height) > (visible.y + visible.height)) {
			int view_bottom = visible.y + visible.height;
			int row_bottom = rect.y + rect.height;
			int diff = row_bottom - view_bottom;
			point_of_view = new Point(rect.x, visible.y + diff);
		} else {
			point_of_view = new Point(rect.x, rect.y);
		}
		//		log.debug("Scrolling to pixel position " + point_of_view.y);

		// Scroll the area into view, upper left hand part.
		viewport.setViewPosition(point_of_view);		
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		int pad = FamilyViews.inst().getBottomMargin(FamilyViews.TABLE_PANE);
		d.height += pad;
		return d;
	}
}