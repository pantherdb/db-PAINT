/**
 *  Copyright 2019 University Of Southern California
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
package org.paint.gui.association;

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.AnnotationDetail;
import edu.usc.ksom.pm.panther.paintCommon.DBReference;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.stanford.ejalbert.BrowserLauncher;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.paint.datamodel.GeneNode;
import static org.paint.gui.association.AssociationTableModel.column_headings;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.AspectChangeEvent;
import org.paint.gui.event.AspectChangeListener;
import org.paint.gui.event.EventManager;
import org.paint.gui.event.FamilyChangeEvent;
import org.paint.gui.event.FamilyChangeListener;
import org.paint.gui.event.GeneSelectEvent;
import org.paint.gui.event.GeneSelectListener;
import org.paint.main.PaintManager;
import org.paint.util.HTMLUtil;

/**
 *
 * @author muruganu
 */
public class AssociationTable extends JTable implements
        GeneSelectListener,
        MouseListener,
        FamilyChangeListener,
        AnnotationChangeListener,
        AspectChangeListener{
    
    private GeneNode node;
    private AssociationTableModel associationModel;
    private boolean widthsInit = false;
    
    public static final String URL_LINK_PREFIX_AMIGO = "http://amigo.geneontology.org/amigo/term/";
    public static final String URL_LINK_PREFIX_PMID = "https://www.ncbi.nlm.nih.gov/pubmed/";
    public static final String URL_LINK_PREFIX_PANTREE_NODE = "http://www.pantree.org/node/annotationNode.jsp?id=";
        
    public AssociationTable() {
        super();
        associationModel = new AssociationTableModel();
        setModel(associationModel);
        
        
		setShowGrid(false);
		setIntercellSpacing(new Dimension(1, 1));

		addMouseListener(this);
		EventManager.inst().registerGeneListener(this);
		EventManager.inst().registerFamilyListener(this);
		//EventManager.inst().registerTermListener(this);
		EventManager.inst().registerGeneAnnotationChangeListener(this);
		EventManager.inst().registerAspectChangeListener(this);

		ListSelectionModel select_model = getSelectionModel();
		select_model.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//select_model.addListSelectionListener(new TermSelectionListener (this));
		setSelectionModel(select_model);
                ((DefaultTableCellRenderer)this.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
          
        
        
    }
    
    public TableCellRenderer getCellRenderer( int row, int column ) {
        String tag = column_headings[column];
        if (tag.equals(AssociationTableModel.COL_NAME_DELETE)) {
            return new DeleteButtonRenderer();
        }
        if (tag.equals(AssociationTableModel.COL_NAME_QUALIFIER_NOT) || tag.equals(AssociationTableModel.COL_NAME_QUALIFIER_COLOCALIZES_WITH) ||
            tag.equals(AssociationTableModel.COL_NAME_QUALIFIER_CONTRIBUTES_TO)) {
            return getDefaultRenderer(Boolean.class);
        }
        return getDefaultRenderer(String.class);
    }    

    @Override
    public void handleGeneSelectEvent(GeneSelectEvent event) {
		clearSelection();           
                GeneNode ancestor = event.getAncestor();
		if (null != ancestor) {
			setAnnotations(ancestor);
                }
                else {
                    
			setAnnotations(null);
                }
     
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
	private void setAnnotations(GeneNode node) {
		this.node = node;
		associationModel.setNode(node);
		setColumnWidths();
		revalidate();
		repaint();
	}
        
	private void setColumnWidths() {
		if (false == widthsInit) {
			Insets insets = new DefaultTableCellRenderer().getInsets();
			int col_count = getColumnCount();
			TableColumnModel col_model = getColumnModel();
			FontMetrics fm = getFontMetrics(getFont());
			int remainder = getWidth();
			TableColumn term_col = null;
			for (int i = 0; i < col_count; i++) {
				String col_name = getColumnName(i);
				int col_width = -1;
				if (col_name.equals(AssociationTableModel.CODE_COL_NAME)) {
					col_width = fm.stringWidth("IDA") + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationTableModel.COL_NAME_DELETE)) {
					col_width = fm.stringWidth(col_name) + insets.left + insets.right + 2;
				} else if (col_name.equals(AssociationTableModel.REFERENCE_COL_NAME)) {
					col_width = (fm.stringWidth("PUBMED:0000000000") + insets.left + insets.right + 2) * 2;
				} else if (col_name.equals(AssociationTableModel.WITH_COL_NAME)) {
					col_width = (fm.stringWidth("XXXX0000000000") + insets.left + insets.right + 2) * 2;
				}else if (col_name.equals(AssociationTableModel.COL_NAME_QUALIFIER_NOT) ||
                                          col_name.equals(AssociationTableModel.COL_NAME_QUALIFIER_COLOCALIZES_WITH) ||
                                          col_name.equals(AssociationTableModel.COL_NAME_QUALIFIER_CONTRIBUTES_TO)) {
                                        col_width = fm.stringWidth("XXX") + insets.left + insets.right + 2;
                                } else if (col_name.equals(AssociationTableModel.TERM_COL_NAME)) {
					term_col = col_model.getColumn(i);
				}
				if (col_width > 0) {
					//log.debug("Setting width of " + col_name + " to " + col_width);
					TableColumn col = col_model.getColumn(i);
					//Get the column at index columnIndex, and set its preferred width.
					col.setPreferredWidth(col_width);
					col.setWidth(col_width);
					remainder -= col_width;
				}
			}
			//Get the column at index columnIndex, and set its preferred width.
			term_col.setPreferredWidth(remainder);
			term_col.setWidth(remainder);
			widthsInit = true;
		}
	}        
        
        

    @Override
    public void mouseClicked(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
        int row = rowAtPoint(point);
        if (row >= 0 && row <= associationModel.getRowCount()) {
            int column = columnAtPoint(point);
            String columnName = associationModel.getColumnName(column);
            
            if (associationModel.TERM_COL_NAME.equals(columnName)) {
                Annotation a = associationModel.getAnnotation(row);
                if (null == a) {
                    return;
                }
                HTMLUtil.bringUpInBrowser(PaintManager.inst().getBrowserLauncher(), URL_LINK_PREFIX_AMIGO + a.getGoTerm());
                return;
            }
            if (associationModel.REFERENCE_COL_NAME.equals(columnName)) {
                Annotation a = associationModel.getAnnotation(row);
                if (null == a) {
                    return;
                }
                ArrayList<String> links = associationModel.getLinksForReferenceCol(a);
                BrowserLauncher bl = PaintManager.inst().getBrowserLauncher();
                if (null != links) {      
                    for (String link: links) {
                        HTMLUtil.bringUpInBrowser(bl, link);
                    }  
                }
            }
            if (associationModel.WITH_COL_NAME.equals(columnName)) {
                Annotation a = associationModel.getAnnotation(row);
                if (null == a) {
                    return;
                }
                ArrayList<String> links = associationModel.getLinksForWithCol(a);
                BrowserLauncher bl = PaintManager.inst().getBrowserLauncher();
                if (null != links) {      
                    for (String link: links) {
                        HTMLUtil.bringUpInBrowser(bl, link);
                    }  
                }                

                
            }
            if (false == associationModel.isCellEditable(row, column)) {
                return;
            }

            if (associationModel.COL_NAME_DELETE.equals(columnName)) {
                // Need to delete annotation
                associationModel.deleteRow(row);
            }
            if (associationModel.COL_NAME_QUALIFIER_NOT.equals(columnName)) {
                // Need to delete annotation
                associationModel.notAnnotation(row);
            }

            
            //System.out.println("!!!! Need to create new annotation for this qualifier");
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void newFamilyData(FamilyChangeEvent e) {
        clearSelection();
        setAnnotations(null);
//		TreePanel tree = PaintManager.inst().getTree();
//		if (tree != null) {
//			GeneNode root = tree.getRoot();
//			setAnnotations(tree.getTopLeafNode(root));
//		} else {
//			setAnnotations(null);
//		}        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
        associationModel = new AssociationTableModel();
        this.node = (GeneNode)event.getSource();
        associationModel.setNode(node);
        this.setModel(associationModel);
        associationModel.fireTableDataChanged();
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleAspectChangeEvent(AspectChangeEvent event) {
        associationModel = new AssociationTableModel();  
        associationModel.setNode(node);
        this.setModel(associationModel);
        associationModel.fireTableDataChanged();        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
