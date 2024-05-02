/**
 *  Copyright 2023 University Of Southern California
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.paint.gui.matrix;

import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paint.matrix.MatrixBuilder;
import edu.usc.ksom.pm.panther.paint.matrix.MatrixGroup;
import edu.usc.ksom.pm.panther.paint.matrix.MatrixInfo;
import edu.usc.ksom.pm.panther.paint.matrix.NodeInfoForMatrix;
import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import edu.usc.ksom.pm.panther.paint.matrix.TermToAssociation;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.paint.config.Preferences;
import org.paint.datamodel.GeneNode;
import org.paint.gui.AspectSelector;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AnnotationDisplayEvent;
import org.paint.gui.event.AnnotationDisplayListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.gui.event.NodeReorderEvent;
import org.paint.gui.event.NodeReorderListener;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;


public class AnnotationMatrix  extends JTable implements
        AnnotationChangeListener,
        MouseListener,
        MouseMotionListener,
        GeneSelectListener,
        NodeReorderListener,         
        AspectChangeListener,
        AnnotationDisplayListener{
    
    private AnnotationMatrixHeaderRenderer header_renderer;
    private AnnotationMatrixCellRenderer matrix_renderer;
    private HashMap<String, AnnotationMatrixModel> models;
    private AnnotationTransferHndlr annot_handler;
    private int selectedColumn = -1;
    private int clickedRow = -1;
    private MatrixInfo mi;
    
    public AnnotationMatrix() {
		super();

		setOpaque(true);
		setBackground(new Color(205, 205, 255));
                
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
//		manager.registerCurationColorListener(this);
		manager.registerNodeReorderListener(this);
//		manager.registerTermListener(this);
		manager.registerAspectChangeListener(this);
		manager.registerGeneAnnotationChangeListener(this);
                manager.registerAnnotationDisplayListener(this);

		setFont(Preferences.inst().getFont());

		setDragEnabled(true);
		annot_handler = new AnnotationTransferHndlr();
		setTransferHandler(annot_handler);

		//single cell selection
		setRowMargin(0);

		matrix_renderer = new AnnotationMatrixCellRenderer();
		setDefaultRenderer(NodeInfoForMatrix.class, matrix_renderer);
		header_renderer = new AnnotationMatrixHeaderRenderer(getTableHeader());
	}
    
	public void setModels(List<GeneNode> orderedNodes, MatrixInfo mi) {
                this.mi = mi;
		if (models == null) {
			models = new HashMap<String, AnnotationMatrixModel>();
		}
		models.clear();
		AnnotationMatrixModel annot_model;
		annot_model = new AnnotationMatrixModel(orderedNodes, AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString(), mi.getGroups(AspectSelector.aspects.get(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString())));
		models.put(AspectSelector.Aspect.BIOLOGICAL_PROCESS.toString(), annot_model);
		annot_model = new AnnotationMatrixModel(orderedNodes, AspectSelector.Aspect.CELLULAR_COMPONENT.toString(), mi.getGroups(AspectSelector.aspects.get(AspectSelector.Aspect.CELLULAR_COMPONENT.toString())));
		models.put(AspectSelector.Aspect.CELLULAR_COMPONENT.toString(), annot_model);
		annot_model = new AnnotationMatrixModel(orderedNodes, AspectSelector.Aspect.MOLECULAR_FUNCTION.toString(), mi.getGroups(AspectSelector.aspects.get(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString())));
		models.put(AspectSelector.Aspect.MOLECULAR_FUNCTION.toString(), annot_model);
		String go_aspect = AspectSelector.inst().getAspect().toString();
		AnnotationMatrixModel matrix = models.get(go_aspect);
		setModel(matrix);
                if ((matrix.getColumnCount() > 0  && this.selectedColumn < 0) || (this.selectedColumn > matrix.getColumnCount())) {
                    setSelectedColumn(0);
                }
		matrix.fireTableDataChanged();
                invalidate();
		System.gc();
	}
        
    public void setModel(AnnotationMatrixModel model) {
        if (model == null) {
            return;
        }
        //setBackground(RenderUtil.getAspectColor());        
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
        this.setDefaultRenderer(NodeInfoForMatrix.class, matrix_renderer);
        this.setColumnModel(column_model);

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
                AnnotationMatrixModel model = (AnnotationMatrixModel) this.getModel();
		if (row >= 0 && row < getRowCount()) {
                        GeneNode selectedNode = model.getNode(clickedRow);
                        // This gets over-written.  Need to make a copy
			List<GeneNode> previous_genes = EventManager.inst().getCurrentGeneSelection();
                        List<GeneNode> previousCopy = null;
                        if (null != previous_genes) {
                            previousCopy = new ArrayList<GeneNode>();
                            previousCopy.addAll(previous_genes);
                        }
			List<GeneNode> selected_genes = null;
                        // Left click
			if (!event.isMetaDown() && !event.isShiftDown() && !event.isAltDown() && !event.isControlDown()) {
				int column = columnAtPoint(point);
                                if (column < 0) {
                                    return;
                                }
                                setSelectedColumn(column);
                                this.clickedRow = row;
                                selectedNode = model.getNode(clickedRow);
                                if (null != selectedNode) {
                                    selected_genes = new ArrayList<GeneNode>(1);
                                    selected_genes.add(selectedNode);
                                }
//				TermAncestor termAncestor = ((AnnotationMatrixModel)getModel()).getTermAncestorAtColumn(column);
//				if (termAncestor != null) {
					
//                                        // For now only consider column that is selected.  Do not consider node that user selected
//                                        GeneNode selectedNode = model.getNode(row);
//					TermAncestorSelectionEvent termAncestorSelectionEvent = new TermAncestorSelectionEvent (this, termAncestor, selectedNode);                                        
//					//TermAncestorSelectionEvent termAncestorSelectionEvent = new TermAncestorSelectionEvent (this, termAncestor, model.getNode(row));
//                                        System.out.println("Single node selected");
//                                        if (null != selectedNode) {
//                                            System.out.println("Left click single node selected id = " + selectedNode.getNodeLabel() + " term is " + termAncestor.getTermToAssociation().getTerm().getAcc());
//                                        }
//					selected_genes = EventManager.inst().fireTermAncestorEvent(termAncestorSelectionEvent);

//					if (previous_genes != null) {
//						for (GeneNode node : previous_genes) {
//							node.setSelected(false);
//						}
//					}
//				}
			}
                        // Right click - Given term user has selected, select MRCA and descendents
			if (event.isMetaDown() && !event.isShiftDown() && !event.isAltDown() && !event.isControlDown()) {
				int column = columnAtPoint(point);
                                if (column < 0) {
                                    return;
                                }
				setSelectedColumn(column);
                                this.clickedRow = row;
                                System.out.println("Column selected");
				TermAncestor termAncestor = ((AnnotationMatrixModel)getModel()).getTermAncestorAtColumn(column);
				if (termAncestor != null) {
                                       
                                        if (null != selectedNode) {
                                            System.out.println("Right click single node selected id = " + selectedNode.getNodeLabel() + " term is " + termAncestor.getTermToAssociation().getTerm().getAcc());
                                        }
					//TermAncestorSelectionEvent termAncestorSelectionEvent = new TermAncestorSelectionEvent (this, termAncestor, selectedNode);
					selected_genes = getMRCAForTerm(termAncestor);
                                        if (null != selected_genes && 0 < selected_genes.size()) {
                                            System.out.println("Selecting " + selected_genes.size() + " genes which include MCRA and descendents for selected term");
                                            selectedNode = selected_genes.get(selected_genes.size() - 1);       // Last entry is MRCA
                                        }
                                        
                                }
			}
                        
                        if (null != selected_genes  && null != selectedNode) {
                            
                            ArrayList<GeneNode> selection = new ArrayList<GeneNode>();
                            selection.addAll(selected_genes);
                            GeneSelectEvent ge = new GeneSelectEvent(this, selection, selectedNode);
                            EventManager.inst().fireGeneEvent(ge);
                        

                            if (selection != null) {
                                System.out.println("Going to select genes");
                                    if (previousCopy == null) {
                                            updateRows(selected_genes);
                                    }
                                    else if (previousCopy.size() != selected_genes.size()) {
                                            updateRows(previousCopy);
                                            updateRows(selected_genes);
                                    }
                                    else {
                                            boolean need_update = false;
                                            for (GeneNode gene : previousCopy) {
                                                    need_update |= !selected_genes.contains(gene);
                                            }
                                            if (need_update) {
                                                    updateRows(previousCopy);
                                                    updateRows(selected_genes);						
                                            }
                                    }
//                                    ge = new GeneSelectEvent (this, selected_genes, EventManager.inst().getAncestralSelection());
//                                    EventManager.inst().fireGeneEvent(ge);
                            }
                            else {
                                System.out.println("No genes selected");
                            }
                        }
                        
                        
		}
                System.out.println("Going to export as drag");
		annot_handler.exportAsDrag(this, event, TransferHandler.COPY);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
        
	public void handleAspectChangeEvent(AspectChangeEvent event) {
		AnnotationMatrixModel matrix = null;
		if (models != null) {
			String go_aspect = AspectSelector.inst().getAspect().toString();
			matrix = models.get(go_aspect);
		}
		setModel(matrix);
                if (matrix.getColumnCount() > 0) {
                    setSelectedColumn(0);
                }
		revalidate();
		repaint();
	}
        
	public int getSelectedColumn() {
		return selectedColumn;
	}        
        
	public void setSelectedColumn(int col) {
		updateColumn(selectedColumn);
		this.selectedColumn = col;
                this.clearSelection();
                this.addColumnSelectionInterval(col, col);
		updateColumn(selectedColumn);
	}
        
        public int getClickedRow() {
            return this.clickedRow;
        }

	private void updateColumn (int col) {
		for (int row = 0; row < this.getRowCount() && col >= 0; row++) {
			AnnotationMatrixModel model = (AnnotationMatrixModel) this.getModel();
			model.fireTableCellUpdated(row, col);
		}		
	}
        
	private void updateRows(List<GeneNode> genes) {
		AnnotationMatrixModel model = (AnnotationMatrixModel) getModel();
		for (GeneNode gene : genes) {
			int row = model.getRow(gene);
			updateRow(row);
		}
	}

	private void updateRow (int row) {
		AnnotationMatrixModel model = (AnnotationMatrixModel) this.getModel();
		for (int col = 0; col < this.getColumnCount() && row >= 0; col++) {
			model.fireTableCellUpdated(row, col);
		}		
	}        

    @Override
    public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
        PaintManager pm = PaintManager.inst();
        mi = MatrixBuilder.getMatrixInfo(pm.getTree().getTreeModel());
        setModels(pm.getTree().getTerminusNodes(), mi);
        //revalidate();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        //repaint();
    }

    @Override
    public void handleGeneSelectEvent(GeneSelectEvent event) {
		if (event.getGenes() == null) {
			System.out.println("AnnotationTable: wierd, have a null gene selected");
		}
                if (event.getSource() == this) {
                    return;
                }
                this.clearSelection();
                AnnotationMatrixModel model = (AnnotationMatrixModel) this.getModel();
                int maxRowCount = model.getRowCount();
                for (GeneNode node: event.getGenes()) {
                    int row = model.getRow(node);
                    if (0 <= row && row <= maxRowCount) {
                        this.addRowSelectionInterval(row, row);
                        updateRow(row);
                    }
                }
                repaint();
                            
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleNodeReorderEvent(NodeReorderEvent e) {
        PaintManager pm = PaintManager.inst();
        mi = MatrixBuilder.getMatrixInfo(pm.getTree().getTreeModel());
        setModels(pm.getTree().getTerminusNodes(), mi);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        repaint();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        repaint();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean termInMatrix(GOTerm term) {
        if (null == mi) {
            return false;
        }
        Set<String> keySet = mi.getAspects();
        for (String aspect: keySet) {
            ArrayList<MatrixGroup> groups = mi.getGroups(aspect);
            for (MatrixGroup group: groups) {
                ArrayList<TermAncestor> items = group.getItems();
                for (TermAncestor ta: items) {
                    if (true == ta.getTermToAssociation().getTerm().equals(term)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
        /**
         * Handle scenario - User selecting whole column of annotation matrix 
         * @param e MRCA node is last node in list.  Descendants of MRCA are listed first
         * @return 
         */
    public List<GeneNode> getMRCAForTerm(TermAncestor newTermAncestor) {
//        TermAncestor newTermAncestor = e.getTermAncestorSelection();
//            GeneNode newSelectNode = e.getNode();
//            
//            if (newTermAncestor.equals(termAncestor)) {
//                if (null != newSelectNode) {
//                    selectedNodes.add(newSelectNode);
//                }                
//            }
//            else {
//                termAncestor = newTermAncestor;
//                if (null == selectedNodes) {
//                    selectedNodes = new ArrayList<GeneNode>();
//                }
//                selectedNodes.clear();
//                if (null != newSelectNode) {
//                    selectedNodes.add(newSelectNode);
//                }
//            }
//        TermAncestor termAncestor = e.getTermAncestorSelection();
//        if (null == selectedNodes) {
//            selectedNodes = new ArrayList<GeneNode>();
//        }
//        selectedNodes.clear();

        TermToAssociation toa = newTermAncestor.getTermToAssociation();
        ArrayList<Node> nodesForAssociation = toa.getExperimentalNodesForAssociation();

//            if (false == selectedNodes.isEmpty()) {
//                for (Iterator<Node> iter = nodesForAssociation.iterator();  iter.hasNext();) {
//                    Node node = iter.next();
//                    if (false == selectedNodes.contains(node)) {
//                        iter.remove();
//                    }
//                }
//            }
        ArrayList<GeneNode> geneNodesForAsn = new ArrayList<GeneNode>(nodesForAssociation.size());

        // Clear previous selections
        // Get corresponding GeneNode for nodes in nodesForAssociation
        TreePanel treePanel = PaintManager.inst().getTree();
        List<GeneNode> allNodes = treePanel.getAllNodes();
        for (GeneNode gNode : allNodes) {
            gNode.setSelected(false);
            if (null != nodesForAssociation) {
                for (Node node : nodesForAssociation) {
                    if (gNode.getNode().equals(node)) {
                        geneNodesForAsn.add(gNode);
                    }
                }
            }
        }

        if (null == nodesForAssociation) {
            return null;
        }

        // Get MRCA for list of nodes
        GeneNode gn = getMRCA(geneNodesForAsn);
        if (null == gn) {
            return null;
        }
        ArrayList<GeneNode> descendants = new ArrayList<GeneNode>();
        ArrayList<Node> nodeDescendants = new ArrayList<Node>();
        gn.getNode().getDescendants(gn.getNode(), nodeDescendants);
        PaintManager pm = PaintManager.inst();
        for (Node descendant : nodeDescendants) {
            GeneNode curGn = pm.getGeneByPTNId(descendant.getStaticInfo().getPublicId());
            if (null != curGn) {
                descendants.add(curGn);
            } else {
                System.out.println("Unable to find gene node by id " + descendant.getStaticInfo().getPublicId() + " " + descendant.getStaticInfo().getNodeAcc());
            }
        }
        descendants.add(gn);
        return descendants;

    }

    public static GeneNode getMRCA(List<GeneNode> nodeList) {
        if (null == nodeList || 0 == nodeList.size()) {
            return null;
        }
        if (1 == nodeList.size()) {
            GeneNode gn = nodeList.get(0);
            Node n = gn.getNode();
            Node p = n.getStaticInfo().getParent();
            if (null == p) {
                return null;
            }
            return PaintManager.inst().getGeneByPTNId(p.getStaticInfo().getPublicId());
        }
        HashMap<Node, List<Node>> nodeToAncestorLookup = new HashMap<Node, List<Node>>();
        Node smallestPathNode = null;
        int size = 0;
        for (GeneNode gn : nodeList) {
            Node n = gn.getNode();
            List<Node> ancestors = Node.getAncestors(n);
            if (null == ancestors || 0 == ancestors.size()) {
                return null;
            }
            nodeToAncestorLookup.put(n, ancestors);

            // Initialize with first entry
            if (1 == nodeToAncestorLookup.size()) {
                smallestPathNode = n;
                size = ancestors.size();
            }

            if (ancestors.size() < size) {
                smallestPathNode = n;
                size = ancestors.size();
            }
        }

        if (null == smallestPathNode || 0 == size) {
            return null;
        }

        List<Node> compList = nodeToAncestorLookup.get(smallestPathNode);
        Node lastCommon = null;
        for (int i = 1; i <= size; i++) {
            Node curComp = compList.get(compList.size() - i);
            for (GeneNode gn : nodeList) {
                Node n = gn.getNode();
                if (n == smallestPathNode) {
                    continue;
                }
                List<Node> ancestors = nodeToAncestorLookup.get(n);
                Node current = ancestors.get(ancestors.size() - i);
                if (curComp != current) {
                    if (lastCommon != null) {
                        return PaintManager.inst().getGeneByPTNId(lastCommon.getStaticInfo().getPublicId());
                    }
                    return null;
                }
            }
            lastCommon = curComp;
        }
        if (null == lastCommon) {
            return null;
        }
        return PaintManager.inst().getGeneByPTNId(lastCommon.getStaticInfo().getPublicId());
    }    


    public void handleAnnotationDisplayEvent(AnnotationDisplayEvent event) {
        PaintManager pm = PaintManager.inst();
        mi = MatrixBuilder.getMatrixInfo(pm.getTree().getTreeModel());
        if (null == mi) {
            return;
        }
        setModels(pm.getTree().getTerminusNodes(), mi);
    }
    
}
