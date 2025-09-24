/**
 * Copyright 2025 University Of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.paint.gui.association;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.paint.datamodel.GeneNode;
import org.paint.gui.event.AnnotationChangeEvent;
import org.paint.gui.event.AnnotationChangeListener;
import org.paint.gui.event.EventManager;


public class ExpAssociationTable extends JTable implements AnnotationChangeListener, MouseListener{
    private GeneNode node;
    private ExpAssociationTableModel associationModel;       
    
    @Override
    public void handleAnnotationChangeEvent(AnnotationChangeEvent event) {
        associationModel = new ExpAssociationTableModel();
        this.node = (GeneNode)event.getSource();
        associationModel.setNode(node);
        this.setModel(associationModel);
        associationModel.fireTableDataChanged();
    }
    

    
    public ExpAssociationTable(GeneNode node) {
        super();
        this.node = node;
        associationModel = new ExpAssociationTableModel();
        setModel(associationModel);
        getTableHeader().setReorderingAllowed(false);
        associationModel.setNode(node);
        EventManager.inst().registerGeneAnnotationChangeListener(this);
        addMouseListener(this);

    }
    
    public void windowClosing() {
        EventManager.inst().removeGeneAnnotationChangeListener(this);
    }
    
//    private void setAnnotations(GeneNode node) {
//        associationModel.setNode(node);
//        revalidate();
//        repaint();
//    }
    
    public TableCellRenderer getCellRenderer( int row, int column ) {
        String tag = ExpAssociationTableModel.column_headings[column];
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
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = e.getPoint();
        int row = rowAtPoint(point);
        if (row >= 0 && row <= associationModel.getRowCount()) {
            int column = columnAtPoint(point);
            String columnName = associationModel.getColumnName(column);
            
            if (associationModel.COL_NAME_DELETE.equals(columnName)) {
                // Need to delete annotation
                associationModel.deleteRow(row);
            }


            
            //System.out.println("!!!! Need to create new annotation for this qualifier");
        }
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    
}
