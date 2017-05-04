/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.gui.matrix;

import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paint.matrix.MatrixGroup;
import edu.usc.ksom.pm.panther.paint.matrix.NodeInfoForMatrix;
import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.paint.datamodel.GeneNode;

/**
 *
 * @author muruganu
 */
public class AnnotationMatrixModel extends AbstractTableModel {
    private String aspect;
    private List<GeneNode> orderedNodes;
    private ArrayList<MatrixGroup> matrixGroupList;
    
    public AnnotationMatrixModel(List<GeneNode> orderedNodes, String aspect, ArrayList<MatrixGroup> matrixGroupList) {
        this.orderedNodes = orderedNodes;
        this.aspect = aspect;
        this.matrixGroupList = matrixGroupList;
    }
	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return NodeInfoForMatrix.class;
	}    

    @Override
    public int getRowCount() {
        if (null == orderedNodes) {
            return 0;
        }
        return orderedNodes.size();
    }

    @Override
    public int getColumnCount() {
        if (null == matrixGroupList) {
            return 0;
        }
        int total = 0;
        for (MatrixGroup group: matrixGroupList) {
            total += group.getCount();
        }
        return total;
    }
    
    public HeaderAncestor getPopup(int columnIndex) {
        int current = 0;
        for (MatrixGroup group: matrixGroupList) {
            if (current <= columnIndex && columnIndex < current + group.getCount()) {
                return new HeaderAncestor(group.getTermAncestorAtIndex(columnIndex - current));
            }
            
            current += group.getCount();
        }
        return null;
    }
    
    public TermAncestor getTermAncestorAtColumn(int columnIndex) {
               int current = 0;
        for (MatrixGroup group: matrixGroupList) {
            if (current <= columnIndex && columnIndex < current + group.getCount()) {
                return group.getTermAncestorAtIndex(columnIndex - current);
            }
            
            current += group.getCount();
        }
        return null; 
    }
    
    public GOTerm getTermForColumn(int columnIndex) {
            int current = 0;
            for (MatrixGroup group: matrixGroupList) {
            if (current <= columnIndex && columnIndex < current + group.getCount()) {
                return group.getTermAtIndex(columnIndex - current);
            }
            
            current += group.getCount();
        }
        return null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (null == orderedNodes || rowIndex > orderedNodes.size()) {
            return null;
        }
        
        int current = 0;
        for (int i = 0; i < matrixGroupList.size(); i++) {
            MatrixGroup group = matrixGroupList.get(i);
            if (current <= columnIndex && columnIndex < current + group.getCount()) {
                return group.getAnnotInfoForNode(orderedNodes.get(rowIndex).getNode(), columnIndex - current);
            }
            
            current += group.getCount();
        }
        System.out.println("Invalid row or column " + rowIndex + " col index = " + columnIndex);
        return null;
        
    }
    
    public boolean isOdd(int columnIndex) {
        int current = 0;
        for (int i = 0; i < matrixGroupList.size(); i++) {
            MatrixGroup group = matrixGroupList.get(i);
            if (current <= columnIndex && columnIndex < current + group.getCount()) {
                if (i % 2 == 0) {
                    return false;
                }
                return true;
            }
            
            current += group.getCount();
        }
        return false;
    }
	public GeneNode getNode(int row) {
		if (row >= orderedNodes.size()) {
			System.out.println("Asking for row " + row + " which is > than the number of rows (" + orderedNodes.size() + ")");
			return null;
		}
                else if (row < 0) {
                    System.out.println("Asking for negative row");
                    return null;
                }
                else {
			return orderedNodes.get(row);
		}
	}
	public int getRow(GeneNode dsn) {
		try {
			return orderedNodes.indexOf(dsn);
		} catch (NullPointerException e) {
			//log.debug("Could not find gene " + dsn.getSeqName() + " in contents");
			return -1;
		}
	}        
    
}
