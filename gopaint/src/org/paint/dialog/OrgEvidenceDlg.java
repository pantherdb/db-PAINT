/**
 * Copyright 2023 University Of Southern California
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
package org.paint.dialog;

import com.sri.panther.paintCommon.Constant;
import edu.usc.ksom.pm.panther.paintCommon.Evidence;
import edu.usc.ksom.pm.panther.paintCommon.Organism;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Vector;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.paint.main.PaintManager;


public class OrgEvidenceDlg extends JDialog {
    protected Frame frame;
    protected JPanel orgEvdnecePanel;
    protected String servletUrl;
    
    public static final String COLUMN_NAME_ORGANISM = "Organism";
    
    protected static final ArrayList<String> EXP_EVIDENCE = new ArrayList<String>(Evidence.getExperimental());
    
    private String LABEL_TITLE = "Select Organisms with Experimental Evidence codes that are not to be displayed in the annotation matrix";
    private String LABEL_INST_SELECT_CLEAR_ALL = "Click on an organism label or an evidence code for option to select all or clear all";
    
    public static final String LABEL_SUBMIT = "Submit";
    public static final String LABEL_CLEAR = "Clear All";
    
    private ArrayList<String> colHeaders = null;
    private ArrayList<Class> colTypes = null;
    private JTable orgTable = null;
    private boolean changes = false;
    
    public OrgEvidenceDlg(Frame frame, String servletUrl, Vector userInfo) {
        super(frame, true);
        setTitle(LABEL_TITLE);
        this.frame = frame;
        this.servletUrl = servletUrl;
        initializePanel();
    }
    
    protected void initializePanel() {
        orgEvdnecePanel = new JPanel();
        
        orgEvdnecePanel.setLayout(new BoxLayout(orgEvdnecePanel, BoxLayout.Y_AXIS));
        JLabel instLbl = new JLabel(LABEL_INST_SELECT_CLEAR_ALL);
        instLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        orgEvdnecePanel.add(instLbl);
        
        // Initialize table
        Collections.sort(EXP_EVIDENCE);
        colHeaders = new ArrayList<String>(EXP_EVIDENCE);
        colHeaders.add(0, COLUMN_NAME_ORGANISM);
        colTypes = new ArrayList<Class>(colHeaders.size());
        
        // Initialize column types.  First entry string.  Rest are boolean
        colTypes.add(String.class);
        for (int i = 1; i < colHeaders.size(); i++) {
            colTypes.add(Boolean.class);
        }
        
        PaintManager pm = PaintManager.inst();
        ArrayList<Organism> allOrgs = pm.getAllOrgList();
        Collections.sort(allOrgs, new  Comparator<Organism>() {
            public int compare(Organism org1, Organism org2) {
                Organism comp1 = org1;
                Organism comp2 = org2;
                int rank1 = comp1.getLogicalRank();
                int rank2 = comp2.getLogicalRank();
                if (rank1 == rank2) {
                    return comp1.getLongName().compareTo(comp2.getLongName());
                }
                if (rank1 > rank2) {
                    return 1;
                }
                return -1;

            }
        
        });
        Hashtable<Organism, HashSet<String>> nonDisplay = pm.getnonDisplayedAnnotMatrixOrgToEvdnceLookup();
        if (null == nonDisplay) {
            nonDisplay = new Hashtable<Organism, HashSet<String>>();
        }
        ArrayList<HashMap<Organism, LinkedHashMap<String, Boolean>>> allOrgsList = new ArrayList<HashMap<Organism, LinkedHashMap<String, Boolean>>>();
        for (Organism o: allOrgs) {
            if (null == o.getName()) {
                continue;
            }
            HashSet<String> nonDisplaySet = nonDisplay.get(o);
            LinkedHashMap<String, Boolean> lookup = new LinkedHashMap<String, Boolean>();
            for (String s: colHeaders) {
                boolean value = false;
                if (null  != nonDisplaySet && nonDisplaySet.contains(s)) {
                    value = true;
                }
                lookup.put(s, value);
            }
            HashMap<Organism, LinkedHashMap<String, Boolean>> currentLookup = new HashMap<Organism, LinkedHashMap<String, Boolean>>();
            currentLookup.put(o, lookup);
            allOrgsList.add(currentLookup);
        }
        
        orgTable = new JTable(new OrganismTableModel(allOrgsList, colHeaders, colTypes)) {
            
            public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
                Component comp = super.prepareRenderer(renderer, rowIndex, vColIndex);
                int row = OrgEvidenceDlg.this.orgTable.convertRowIndexToModel(rowIndex);
                int selectedRow = super.getSelectedRow();
                if (row == selectedRow) {
                    return comp;
                }
                comp.setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
                return comp;
            }
                
        };
        orgTable.addMouseListener(new MouseAdapter() {
            // Handle mouse click on organism column
            public void mouseClicked(MouseEvent e) {
                if (false == SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                java.awt.Point p = e.getPoint();
                int rowIndex = orgTable.rowAtPoint(p);
                int colIndex = orgTable.columnAtPoint(p);
                int realRowIndex = orgTable.convertRowIndexToModel(rowIndex);
                int selectedRow = orgTable.getSelectedRow();
                if (selectedRow != realRowIndex) {
                    return;
                }
                int realColumnIndex = orgTable.convertColumnIndexToModel(colIndex);
                if (realColumnIndex > 0) {
                    return;
                }

                SelectClearPopUpMenu pm = new SelectClearPopUpMenu(realRowIndex, -1, false);
                pm.showMenu(e);
            }
        });
        
        orgTable.getTableHeader().addMouseListener(new MouseAdapter() {
            // Handle mouse click on organism column
            public void mouseClicked(MouseEvent e) {
                if (false == SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                java.awt.Point p = e.getPoint();
                int colIndex = orgTable.columnAtPoint(p);
                int realColumnIndex = orgTable.convertColumnIndexToModel(colIndex);
                if (realColumnIndex == 0) {
                    return;
                }

                SelectClearPopUpMenu pm = new SelectClearPopUpMenu(-1, realColumnIndex, true);
                pm.showMenu(e);
            }
        });        
        
        

//        TableColumnModel column_model = new DefaultTableColumnModel();
//        TableColumn firstCol = new TableColumn();
//        firstCol.setHeaderValue(COLUMN_NAME_ORGANISM);
//        firstCol.setCellRenderer(new OrgEvidenceDlgTitleRenderer(-1, 0, colHeaders.get(0), null));
//        column_model.addColumn(firstCol);
//        for (int i = 1; i < colHeaders.size(); i++) {
//            TableColumn col = new TableColumn(i);
//            OrgEvidenceDlgTitleRenderer hColRenderer =  new OrgEvidenceDlgTitleRenderer(-1, i, colHeaders.get(i), orgTable.getTableHeader());
//            col.setHeaderRenderer(hColRenderer);
//            column_model.addColumn(col);
//        }
//        orgTable.setColumnModel(column_model);
        
        setColumnWidthForOrgColumn(orgTable);
        orgTable.setPreferredScrollableViewportSize(new Dimension(750, 90));
        JScrollPane orgScrollPane = new JScrollPane(orgTable);
        orgScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        orgEvdnecePanel.add(orgScrollPane);
        
        JButton submitBtn = new JButton(LABEL_SUBMIT);
        submitBtn.addActionListener(new UpdateChoices());
        JPanel choicesPanel = new JPanel();
        choicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        choicesPanel.add(submitBtn);       
        JButton clearBtn = new JButton(LABEL_CLEAR);
        clearBtn.addActionListener(new ClearChoices());
        choicesPanel.add(clearBtn);        
        
        orgEvdnecePanel.add(choicesPanel);
        setContentPane(orgEvdnecePanel);
        Rectangle r = frame.getBounds();

        setBounds(r.x + r.width / 2, r.y + r.height / 2, 1000, 800);
        pack();
        setLocationRelativeTo(frame);
    }
    
    public class OrganismTableModel extends AbstractTableModel {
        protected ArrayList<HashMap<Organism, LinkedHashMap<String, Boolean>>> data =  null;
        protected ArrayList<String> columnNames = null;
        protected ArrayList<Class> columnTypes;
        
        OrganismTableModel(ArrayList<HashMap<Organism, LinkedHashMap<String, Boolean>>> data, ArrayList<String> columnNames, ArrayList<Class> columnTypes) {
            this.data = data;
            this.columnNames = columnNames;
            this.columnTypes = columnTypes;
        }
        
        public HashMap<Organism, LinkedHashMap<String, Boolean>> getInfoAtRot(int row) {
            return data.get(row);
        }
        
        public int getColumnCount() {
            return columnNames.size();
        }


        public String getColumnName(int col) {
            return columnNames.get(col);
        }
        
        
        // If this method is not implemented, the check box entry will appear as true or false text
        public Class getColumnClass(int c) {
            return columnTypes.get(c);
        }


        public int getRowCount() {
            return data.size();
        }
        
        public void setValueForCol(int col, Boolean value) {
            for (int i = 0; i < data.size(); i++) {
                setValueAt(value, i, col);
            }
        }
        
        public void setValueForRow(int row, Boolean value) {
            for (int i = 0; i < columnNames.size(); i++) {
                setValueAt(value, row, i);
            }
        }        

        public void setValueAt(Object value, int row, int col) {
            //if (DEBUG) {
//                System.out.println("Setting value at " + row + "," + col +
//                                   " to " + value + " (an instance of " +
//                                   value.getClass() + ")");
        //    }
            Object oldValue = getValueAt(row, col);
            if (false == oldValue instanceof Boolean) {
                return;
            }
            
            HashMap<Organism, LinkedHashMap<String, Boolean>> current = data.get(row);
            String colHeader = columnNames.get(col);
            for (Entry<Organism, LinkedHashMap<String, Boolean>> entry: current.entrySet()) {
                LinkedHashMap<String, Boolean> lookup = entry.getValue();
                lookup.put(colHeader, (Boolean)value);
                break;
            }

            fireTableCellUpdated(row, col);

        }        

        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex <0 || columnIndex < 0) {
                return null;
            }
            HashMap<Organism, LinkedHashMap<String, Boolean>> currentRow = data.get(rowIndex);
            String colHeader = columnNames.get(columnIndex);
            
            for (Entry<Organism, LinkedHashMap<String, Boolean>> entry: currentRow.entrySet()) {
                if (COLUMN_NAME_ORGANISM.equals(colHeader)) {
                    Organism o = entry.getKey();
                    return o.getLongName() + Constant.STR_SPACE + Constant.STR_BRACKET_ROUND_OPEN + o.getShortName() + Constant.STR_BRACKET_ROUND_CLOSE;
                }               
                LinkedHashMap<String, Boolean> lookup = entry.getValue();
                return lookup.get(colHeader);
            }
            return null;
        }
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            String colHeader = columnNames.get(columnIndex);
            if (COLUMN_NAME_ORGANISM.equals(colHeader)) {
                    return false;
            }
            return true;
        }
    }
    
    /*
    // Not working?
    */
    public void setColumnWidthForOrgColumn(JTable orgTable) {
        if (null == orgTable) {
            return;
        }
        TableColumnModel col_model = orgTable.getColumnModel();
        FontMetrics fm = orgTable.getFontMetrics(orgTable.getFont());
	int numRows = orgTable.getRowCount();
        int colIndex = 0;   // First column with organisms
        int maxWidth = 0;
        for (int i = 0; i < numRows; i++) {
            int curWidth = fm.stringWidth((String)orgTable.getValueAt(i, colIndex));
            if (maxWidth < curWidth) {
                maxWidth = curWidth;
            }
        }
        TableColumn orgColumn = col_model.getColumn(colIndex);
        orgColumn.setPreferredWidth(maxWidth);
	orgColumn.setWidth(maxWidth);
        
        int numCols = orgTable.getColumnCount();
        for (int i = 1; i < numCols;i++) {
            int width = fm.stringWidth(colHeaders.get(i));
            TableColumn curModel = col_model.getColumn(i);
            curModel.setWidth(width);
            curModel.setPreferredWidth(width);
        }
    }
    
    
    
    public class UpdateChoices implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            OrganismTableModel model = (OrganismTableModel)orgTable.getModel();
            ArrayList<HashMap<Organism, LinkedHashMap<String, Boolean>>> dataLookup = model.data;
            Hashtable<Organism, HashSet<String>> nonDisplayedAnnotMatrixOrgToEvdnceLookup = new Hashtable<Organism, HashSet<String>>();
            
            for (HashMap<Organism, LinkedHashMap<String, Boolean>> nonDisplayItem: dataLookup) {
                for (Entry<Organism, LinkedHashMap<String, Boolean>> current: nonDisplayItem.entrySet()) {
                    Organism o = current.getKey();
                    LinkedHashMap<String, Boolean> lookups = current.getValue();
                    HashSet<String> nonDisplay = new HashSet<String>();
                    for (Entry<String, Boolean> lookup: lookups.entrySet()) {
                        if (true == lookup.getValue()) {
                            nonDisplay.add(lookup.getKey());
                        }
                    }
                    if (false == nonDisplay.isEmpty()) {
                        nonDisplayedAnnotMatrixOrgToEvdnceLookup.put(o, nonDisplay);
                    }
                }
            }
            if (true == nonDisplayedAnnotMatrixOrgToEvdnceLookup.isEmpty()) {
                nonDisplayedAnnotMatrixOrgToEvdnceLookup = null;
            }
            PaintManager.inst().setnonDisplayedAnnotMatrixOrgToEvdnceLookup(nonDisplayedAnnotMatrixOrgToEvdnceLookup);
            changes = true;
            OrgEvidenceDlg.this.setVisible(false);
        }
    }
    
    public class ClearChoices implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            OrganismTableModel model = (OrganismTableModel)orgTable.getModel();
            ArrayList<HashMap<Organism, LinkedHashMap<String, Boolean>>> dataLookup = model.data;
            for (HashMap<Organism, LinkedHashMap<String, Boolean>> nonDisplayItem: dataLookup) {
                for (Entry<Organism, LinkedHashMap<String, Boolean>> current: nonDisplayItem.entrySet()) {
                    Organism o = current.getKey();
                    LinkedHashMap<String, Boolean> lookups = current.getValue();
                    
                    for (Entry<String, Boolean> lookup: lookups.entrySet()) {
                        if (true == lookup.getValue()) {
                            lookups.put(lookup.getKey(), Boolean.FALSE);
                        }
                    }
                }
            }
            model.fireTableDataChanged();
        }
    }
    
    public boolean display() {
        setVisible(true);
        return changes;
    }
    

    
    public class SelectClearPopUpMenu extends JPopupMenu implements ActionListener {
        int row;
        int col;
        boolean isHeader;
        public static final String STR_SELECT_ALL = "Select All";
        public static final String STR_CLEAR_ALL = "Clear All";
        
        public SelectClearPopUpMenu(int row, int col, boolean isHeader) {
            super("");
            this.row = row;
            this.col = col;
            this.isHeader = isHeader;
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            OrganismTableModel otm = (OrganismTableModel)OrgEvidenceDlg.this.orgTable.getModel();
            if (STR_SELECT_ALL.equals(command)) {
                if (true == isHeader) {
                    otm.setValueForCol(col, Boolean.TRUE);
                }
                else {
                    otm.setValueForRow(row, Boolean.TRUE);
                }
            }
            else if (STR_CLEAR_ALL.equals(command)) {
                if (true == isHeader) {
                    otm.setValueForCol(col, Boolean.FALSE);
                }
                else {
                    otm.setValueForRow(row, Boolean.FALSE);
                }
            }
        }
        
        public void showMenu(MouseEvent e) {
            JMenuItem mi = new JMenuItem(STR_SELECT_ALL);
            mi.setActionCommand(STR_SELECT_ALL);
            mi.addActionListener(this);
            add(mi);
            mi = new JMenuItem(STR_CLEAR_ALL);
            mi.setActionCommand(STR_CLEAR_ALL);
            mi.addActionListener(this);
            add(mi);
            show(e.getComponent(), e.getX(), e.getY());
        }
        
    }    
}
