/**
 *  Copyright 2025 University Of Southern California
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

import edu.usc.ksom.pm.panther.paintCommon.Annotation;
import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
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
import org.paint.datamodel.GeneNode;
import org.paint.gui.familytree.TreePanel;
import org.paint.main.PaintManager;
import edu.usc.ksom.pm.panther.paintCommon.Node;
import edu.usc.ksom.pm.panther.paintCommon.NodeVariableInfo;
import java.util.ArrayList;

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
        // Highlight Experimental PAINT annotation columns
        if (true == isPaintExpTerm(goTerm)) {
            bg_color = Preferences.EXP_ANNOT_ADDED_IN_PAINT;
        }
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
    
    private boolean isPaintExpTerm(GOTerm goTerm) {
        if (null == goTerm) {
            return false;
        }
        String acc = goTerm.getAcc();
        if (null == acc) {
            return false;
        }
        PaintManager pm = PaintManager.inst();
        TreePanel tp = pm.getTree();
        if (null != tp) {
            List<GeneNode> nodes = tp.getTerminusNodes();
            if (null != nodes) {
                for (GeneNode gn: nodes) {
                    Node n = gn.getNode();
                    NodeVariableInfo nvi = n.getVariableInfo();
                    if (null == nvi) {
                        continue;
                    }
                    ArrayList<Annotation> annotList = nvi.getGoAnnotationList();
                    if (null != annotList) {
                        for (Annotation a: annotList) {
                            if (true == a.isExpAnnotCreatedInPaint()) {
                                if (acc.equals(a.getGoTerm())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
}
