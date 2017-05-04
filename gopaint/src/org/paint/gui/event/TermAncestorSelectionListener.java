/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.paint.gui.event;

import java.util.EventListener;

/**
 *
 * @author muruganu
 */
public interface TermAncestorSelectionListener extends EventListener {
    public void handleTermAncestorSelectionEvent(TermAncestorSelectionEvent e);
}
