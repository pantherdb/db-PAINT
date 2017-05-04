package org.paint.gui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class HelpMenu extends JMenu {
	
	private JMenuItem aboutMenuItem;
	
	public HelpMenu(Action aboutAction) {
		super("Help");
		aboutMenuItem = new JMenuItem(aboutAction);
		add(aboutMenuItem);
	}

}
