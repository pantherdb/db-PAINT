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
package org.paint.gui.matrix;

import edu.usc.ksom.pm.panther.paintCommon.GOTerm;
import edu.usc.ksom.pm.panther.paint.matrix.TermAncestor;
import edu.usc.ksom.pm.panther.paint.matrix.TermToAssociation;
import edu.usc.ksom.pm.panther.paintCommon.GOTermHelper;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.bbop.framework.GUIManager;
import org.paint.datamodel.Association;
import org.paint.main.PaintManager;

public class HeaderAncestor extends JPopupMenu implements ActionListener{
    private TermAncestor termAncestor;
    
    public HeaderAncestor(TermAncestor termAncestor) {
        super("");
        this.termAncestor = termAncestor;
    }
    
    public void showMenu(MouseEvent e) {
        PaintManager pm = PaintManager.inst();
        AnnotationMatrix am = pm.getMatrix();
        GOTermHelper gth = pm.goTermHelper();
        ArrayList<GOTerm> termList = termAncestor.getAncestorList();
        boolean added = false;
        for (GOTerm term : termList) {
            boolean termInMatrix = am.termInMatrix(term);
            if (true == termInMatrix) {
                continue;
            }
            // Skip top level terms
            List<GOTerm> parents = term.getParents();
            if (null == parents || 0 == parents.size()) {
//                System.out.println("Skipping for header " + term.getName() + " " + term.getAcc());
                continue;
            }
            
            // Skip terms that are not allowed
            if (false == gth.isAnnotAllowedForTerm(term.getAcc())) {
                continue;
            }
            
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(term.getName());
            menuItem.setSelected(false);
            menuItem.setActionCommand(term.getAcc());
            menuItem.addActionListener(this);
            add(menuItem);
            added = true;
        }
        if (true == added) {
            show(e.getComponent(), e.getX(), e.getY());
        }
        else {
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "No Ancestors", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public boolean hasAncestors() {
        PaintManager pm = PaintManager.inst();
        AnnotationMatrix am = pm.getMatrix();
        ArrayList<GOTerm> termList = termAncestor.getAncestorList();
        for (GOTerm term : termList) {
            boolean termInMatrix = am.termInMatrix(term);
            if (true == termInMatrix) {
                continue;
            }
            return true;
        }
        return false;        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String acc = e.getActionCommand();
        if (null == acc) {
            return;
        }
        ArrayList<GOTerm> termList = termAncestor.getAncestorList();
        ArrayList<GOTerm> newAncestorList = new ArrayList<GOTerm>();
        GOTerm selected = null;
        for (GOTerm term : termList) {
            if (null == selected && acc.equals(term.getAcc())) {
                System.out.println("Selected term " + term.getAcc() + " " + term.getName());
                selected = term;
            }
            else if (null != selected) {
                newAncestorList.add(term);
            }
        }
        if (null == selected) {
            setVisible(false);
            return;
        }

        TermToAssociation toa = new TermToAssociation(selected);
        ArrayList<Association> associations = termAncestor.getTermToAssociation().getAsnList();
        for (Association a : associations) {
            toa.addAsn(a);
        }
        TermAncestor ta = new TermAncestor(toa, newAncestorList);
        ta.setTermToAssociation(toa);
        PaintManager.inst().addTermAncestor(ta);

        setVisible(false);
        return;

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
