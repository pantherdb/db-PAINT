/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.gui.matrix;

import edu.usc.ksom.pm.panther.paint.matrix.MatrixGroup;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPopupMenu;

/**
 * NOT USED!!!! Use HeaderAncestor instead
 * @author muruganu
 */
public class AncestorPopup extends JPopupMenu implements ActionListener {
    MatrixGroup mg;
    int index;
    public AncestorPopup(MatrixGroup mg, int index) {
        super("");
    
        this.mg = mg;
        this.index = index;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
