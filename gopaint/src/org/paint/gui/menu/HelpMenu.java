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
package org.paint.gui.menu;

import edu.usc.ksom.pm.panther.paintCommon.PAINTVersion;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.bbop.framework.GUIManager;
import org.paint.config.Preferences;
import org.paint.main.PaintManager;
import org.paint.util.HTMLUtil;

public class HelpMenu extends JMenu {

    private static final long serialVersionUID = 1L;
    protected JMenuItem onlineResourcesItem;
    protected JMenuItem aboutItem;

    private static final String MENU_ITEM_ONLINE_RESOURCES = "Online Docs";
    private static final String MENU_ITEM_ABOUT = "About";

    public HelpMenu() {
        super("Help");

        onlineResourcesItem = new JMenuItem(MENU_ITEM_ONLINE_RESOURCES);
        onlineResourcesItem.addActionListener(new OnlineResourcesActionListener());
        this.add(onlineResourcesItem);

        aboutItem = new JMenuItem(MENU_ITEM_ABOUT);
        aboutItem.addActionListener(new AboutActionListener());
        this.add(aboutItem);
    }

    private class  OnlineResourcesActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            HTMLUtil.bringUpInBrowser(PaintManager.inst().getBrowserLauncher(), Preferences.inst().getPantherURL());
        }
    }

    private class AboutActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(GUIManager.getManager().getFrame(), "GO PAINT Version:  " + PAINTVersion.getPAINTClientversion());
        }
    }
}
