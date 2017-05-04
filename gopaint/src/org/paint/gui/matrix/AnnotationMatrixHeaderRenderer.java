/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.gui.matrix;

import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.paint.config.Preferences;

/**
 *
 * @author muruganu
 */
public class AnnotationMatrixHeaderRenderer extends JLabel implements TableCellRenderer{
    
    public AnnotationMatrixHeaderRenderer(JTableHeader header) {
                setOpaque(true);
		this.setText("");
		setHorizontalAlignment(CENTER);
		setVerticalAlignment(CENTER);
		header.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				AnnotationMatrix table = (AnnotationMatrix) ((JTableHeader) e.getSource()).getTable();
				TableColumnModel columnModel = table.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				AnnotationMatrixModel model = (AnnotationMatrixModel) table.getModel();
                                HeaderAncestor ha = model.getPopup(viewColumn);
                                ha.showMenu(e);
//				if (viewColumn >= 0 && viewColumn < model.getColumnCount()) {
//                                        AncestorPopup ap = new AncestorPopup();
//					ColumnTermData td = model.getTermData(viewColumn);
//					td.showMenu(e, table);
//				}
			}
		});
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setIcon(null);        
        AnnotationMatrixModel matrix = (AnnotationMatrixModel) table.getModel();
        //Color bg_color = RenderUtil.getAspectColor();
        Color bg_color = AnnotationMatrixCellRenderer.COLOR_BASIC;
        if (false == matrix.isOdd(column)) {
            bg_color = AnnotationMatrixCellRenderer.COLOR_CONTRAST;
        }

        GOTerm goTerm = matrix.getTermForColumn(column);
        setToolTipText(goTerm.getName());
        Preferences dp = Preferences.inst();             
        UIManager.put("ToolTip.foreground", dp.getForegroundColor());
        ToolTipManager.sharedInstance().setDismissDelay(999999999);
        setBackground(bg_color);
        HeaderAncestor ha = matrix.getPopup(column);
   
        setForeground(dp.getForegroundColor());
        Border border;
        if (isSelected) {
            border = BorderFactory.createLineBorder(Color.BLACK);
        } else {
            border = BorderFactory.createEtchedBorder(); // default is lowered etched border
        }
        setBorder(border);        
        if (null == ha || false == ha.hasAncestors()) {    
            return this;
        }
        
        Icon icon = Preferences.inst().getIconByName("arrowDown");
        setIcon(icon);


        return this; 

    }
    
}
