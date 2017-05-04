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

package org.paint.gui.matrix;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;
import org.geneontology.db.model.Association;
import org.geneontology.db.model.Term;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.gui.AspectSelector;
import org.paint.gui.FamilyViews;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.CurationColorEvent;
import org.paint.gui.event.CurationColorListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneDataEvent;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.gui.event.NodeReorderListener;
import org.paint.gui.event.TermSelectEvent;
import org.paint.gui.event.TermSelectionListener;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;

public class AnnotMatrix extends JTable 
implements 
AnnotationChangeListener,
MouseListener, 
GeneSelectListener, 
NodeReorderListener, 
CurationColorListener,
TermSelectionListener, 
AspectChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MatrixHeaderRenderer header_renderer;
	private MatrixCellRenderer matrix_renderer;
	private AnnotationTransferHandler annot_handler;
	private int selectedColumn = -1;

	private Map<String, AnnotMatrixModel> models;

	private static Logger log = Logger.getLogger(AnnotMatrix.class);

	public AnnotMatrix() {
		super();

		setOpaque(true);
		setBackground(Preferences.inst().getBackgroundColor());
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setRowSelectionAllowed(true);
		setColumnSelectionAllowed(true);
		getTableHeader().setReorderingAllowed(false);
		setAutoCreateColumnsFromModel(false);

		setShowGrid(false);
		setIntercellSpacing(new Dimension(0, 0));

		this.addMouseListener(this);
		EventManager manager = EventManager.inst();
		manager.registerGeneListener(this);
		manager.registerCurationColorListener(this);
		manager.registerNodeReorderListener(this);
		manager.registerTermListener(this);
		manager.registerAspectChangeListener(this);
		manager.registerGeneAnnotationChangeListener(this);

		setFont(Preferences.inst().getFont());

		setDragEnabled(true);
		annot_handler = new AnnotationTransferHandler();
		setTransferHandler(annot_handler);

		//single cell selection
		setRowMargin(0);

		matrix_renderer = new MatrixCellRenderer();
		setDefaultRenderer(Association.class, matrix_renderer);
		header_renderer = new MatrixHeaderRenderer(getTableHeader());
	}

	public void setModels(List<GeneNode> orderedNodes) {
		if (models == null) {
			models = new HashMap<String, AnnotMatrixModel>();
		}
		models.clear();
		AnnotMatrixModel annot_model;
		annot_model = new AnnotMatrixModel(orderedNodes, AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString());
		models.put(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString(), annot_model);
		annot_model = new AnnotMatrixModel(orderedNodes, AspectSelector.Aspect.CELLULAR_COMPONENT.toString());
		models.put(AspectSelector.Aspect.CELLULAR_COMPONENT.toString(), annot_model);
		annot_model = new AnnotMatrixModel(orderedNodes, AspectSelector.Aspect.MOLECULAR_FUNCTION.toString());
		models.put(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString(), annot_model);

		String go_aspect = AspectSelector.inst().getAspect().toString();
		AnnotMatrixModel matrix = models.get(go_aspect);
		setModel(matrix);
		setSelectedColumn(0);
		revalidate();
		System.gc();
	}

	public Map<String, AnnotMatrixModel> getModels() {
		return models;
	}

	// Set the text and icon values on the second column for the icon render
	public void setModel(AnnotMatrixModel model) {
		if (model != null) {
			super.setModel(model);

			int columns = model.getColumnCount();
			TableColumnModel column_model = new DefaultTableColumnModel();
			for (int i = 0; i < columns; i++) {
				TableColumn col = new TableColumn(i);
				col.setPreferredWidth(12);
				col.setHeaderRenderer(header_renderer);
				col.setResizable(false);
				column_model.addColumn(col);
			}
			setDefaultRenderer(Association.class, matrix_renderer);
			this.setColumnModel(column_model);
		}
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		int pad = FamilyViews.inst().getBottomMargin(FamilyViews.MATRIX_PANE);
		d.height += pad;
		return d;
	}

	/*
	 * Workaround for BasicTableUI anomaly. Make sure the UI never tries to
	 * paint the editor. The UI currently uses different techniques to
	 * paint the renderers and editors and overriding setBounds() below
	 * is not the right thing to do for an editor. Returning -1 for the
	 * editing row in this case, ensures the editor is never painted.
	 */
	@Override
	public int getEditingRow(){
		return (getColumnClass(editingColumn) == AnnotMatrixModel.class) ? -1 : editingRow;
	}

	public void handleGeneSelectEvent (GeneSelectEvent e) {
		if (e.getGenes() == null) {
			log.debug("AnnotationTable: wierd, have a null gene selected");
		}
		else if (e.getSource() != this) {
			// not sure if this is the correct way to proceed
			AnnotMatrixModel genes = (AnnotMatrixModel) this.getModel();
			setSelectedColumn(-1);
			int total = genes.getRowCount();
			if (total > 0) {
				ListSelectionModel lsm = this.getSelectionModel();
				lsm.clearSelection();
				List<GeneNode> selection = e.getGenes();
				if (selection != null && !selection.isEmpty()) {
					GeneNode node = e.getAncestor();
					TreePanel tree = PaintManager.inst().getTree();		
					setSelectedRows(lsm, tree, node);
				}
				repaint();
			}
		}
	}

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
		GeneNode node = (GeneNode) event.getSource();
		String class_name = getModel().getClass().getName();
		if (class_name.equals("org.paint.gui.matrix.AnnotMatrixModel")) {
			AnnotMatrixModel model = (AnnotMatrixModel) getModel();
			model.resetAssoc(node);
			model.fireTableDataChanged();
		}
	}

	public void handleTermEvent(TermSelectEvent e) {
		if (e.getSource() != this) {
			List<GeneNode> genes = PaintManager.inst().getTree().getTerminusNodes();
			int total = genes.size();
			if (total > 0) {
				ListSelectionModel lsm = this.getSelectionModel();
				lsm.clearSelection();
				for (int i = 0; i < total; i++) {
					GeneNode node = genes.get(i);
					if (node.isSelected()) {
						lsm.addSelectionInterval(i, i);
					}
				}
				AnnotMatrixModel model = (AnnotMatrixModel) getModel();
				Term term = e.getTermSelection().get(0);
				int column = model.getTermColumn(term);
				this.setSelectedColumn(column);
			}
		}
	}

	public void handleSubFamilyEvent (GeneDataEvent e) {
		AnnotMatrixModel model = (AnnotMatrixModel) this.getModel();
		model.fireTableDataChanged();
		repaint();
	}

	@Override
	public void handleAspectChangeEvent(AspectChangeEvent event) {
		AnnotMatrixModel matrix = null;
		if (models != null) {
			String go_aspect = AspectSelector.inst().getAspect().toString();
			matrix = models.get(go_aspect);
		}
		setModel(matrix);
		setSelectedColumn(0);
		revalidate();
		repaint();
	}

	public void handleNodeReorderEvent(NodeReorderEvent e) {
		if (models != null && models.size() > 0) {
			Set<String> aspects = models.keySet();
			for (String aspect : aspects) {
				AnnotMatrixModel model = models.get(aspect);
				model.modifyRows(e.getNodes());
			}
			String go_aspect = AspectSelector.inst().getAspect().toString();
			setModel(models.get(go_aspect));
			revalidate();
			repaint();
		}
	}

	protected void setSelectedRows(ListSelectionModel lsm, TreePanel tree, GeneNode node) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		if (node.isExpanded() && !node.isPruned()) {
			GeneNode low_gene = tree.getTopLeafNode(node);
			int low_row = model.getRow(low_gene);
			GeneNode high_gene = tree.getBottomLeafNode(node);
			int high_row = model.getRow(high_gene);
			for (int row = low_row; row <= high_row; row++)
				updateRow(row);
		} else {
			int row = model.getRow(node);
			updateRow(row);
		}
	}

	private void updateRows(List<GeneNode> genes) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		for (GeneNode gene : genes) {
			int row = model.getRow(gene);
			updateRow(row);
		}
	}

	private void updateRow (int row) {
		AnnotMatrixModel model = (AnnotMatrixModel) this.getModel();
		for (int col = 0; col < this.getColumnCount() && row >= 0; col++) {
			model.fireTableCellUpdated(row, col);
		}		
	}

	public int getSelectedColumn() {
		return selectedColumn;
	}

	public void setSelectedColumn(int col) {
		updateColumn(selectedColumn);
		this.selectedColumn = col;
		updateColumn(selectedColumn);
	}

	private void updateColumn (int col) {
		for (int row = 0; row < this.getRowCount() && col >= 0; row++) {
			AnnotMatrixModel model = (AnnotMatrixModel) this.getModel();
			model.fireTableCellUpdated(row, col);
		}		
	}

	public Color getRowForegroundColor(int row) {
		GeneNode node = ((AnnotMatrixModel) this.getModel()).getNode(row);
		if (null == node) {
			return null;
		}
		return node.getSubFamilyColor();
	}

	@Override
	public void mouseClicked(MouseEvent event) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {		
	}

	@Override
	public void mousePressed(MouseEvent event) {		
		Point point = event.getPoint();
		int row = rowAtPoint(point);
		if (row >= 0 && row < getRowCount()) {
			List<GeneNode> previous_genes = EventManager.inst().getCurrentGeneSelection();
			List<GeneNode> selected_genes = null;
			if (!event.isMetaDown() && !event.isShiftDown() && !event.isAltDown() && !event.isControlDown()) {
				int column = columnAtPoint(point);
				Term term = ((AnnotMatrixModel)getModel()).getTermForColumn(column);
				if (term != null) {
					AnnotMatrixModel model = (AnnotMatrixModel) this.getModel();
					TermSelectEvent term_event = new TermSelectEvent (this, term, model.getNode(row));
					selected_genes = EventManager.inst().fireTermEvent(term_event);
					setSelectedColumn(column);
					if (previous_genes != null) {
						for (GeneNode node : previous_genes) {
							node.setSelected(false);
						}
					}
				}
			}

			if (event.isMetaDown() && !event.isShiftDown() && !event.isAltDown() && !event.isControlDown()) {
				int col = getColumnModel().getSelectionModel().getLeadSelectionIndex();
				setSelectedColumn(col);
				Term term = ((AnnotMatrixModel)getModel()).getTermForColumn(col);
				if (term != null) {
					setSelectedColumn(col);
					TermSelectEvent term_event = new TermSelectEvent (this, term);
					selected_genes = EventManager.inst().fireTermEvent(term_event);
				}
			}

			if (selected_genes != null) {
				if (previous_genes == null) {
					updateRows(selected_genes);
				}
				else if (previous_genes.size() != selected_genes.size()) {
					updateRows(previous_genes);
					updateRows(selected_genes);
				}
				else {
					boolean need_update = false;
					for (GeneNode gene : previous_genes) {
						need_update |= !selected_genes.contains(gene);
					}
					if (need_update) {
						updateRows(previous_genes);
						updateRows(selected_genes);						
					}
				}
				GeneSelectEvent ge = new GeneSelectEvent (this, selected_genes, EventManager.inst().getAncestralSelection());
				EventManager.inst().fireGeneEvent(ge);
			}
		}
		annot_handler.exportAsDrag(this, event, TransferHandler.COPY);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void handleCurationColorEvent(CurationColorEvent e) {
		repaint();
	}

	protected void modifyColumns(Term added_term [], boolean remove) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		boolean modified = model.modifyColumns(added_term, remove);
		if (modified) {
			setModel(model);
			if (remove)
				setSelectedColumn(model.getTermColumn(added_term[AnnotMatrixModel.NARROWER]));
			else
				setSelectedColumn(model.getTermColumn(added_term[AnnotMatrixModel.BROADER]));
			repaint();
		}
	}

	public void addAnnotatedColumn(Term term, List<Term> with_terms) {
		AnnotMatrixModel model = (AnnotMatrixModel) getModel();
		List<Term> terms = model.getTermList();
		if (!terms.contains(term)) {
			// sigh
			Term [] origin = new Term[2];
			boolean found = false;
			for (int i = 0; i < with_terms.size() && !found; i++) {
				Term original = with_terms.get(i);
				if (terms.contains(original)) {
					found = true;
					origin[AnnotMatrixModel.BROADER] = term;
					origin[AnnotMatrixModel.NARROWER] = original;
					modifyColumns (origin, true);
				}
			}
			if (!found)
				log.debug("Annotated to " + term + ", but can't find original evidence");
		}
	}
}