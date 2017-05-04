package org.paint.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.bbop.framework.LayoutMenu;
import org.bbop.framework.ViewMenus;
import org.bbop.util.OSUtil;
import org.paint.gui.menu.EditMenu;
import org.paint.gui.menu.FileMenu;
import org.paint.gui.menu.MSAMenu;
import org.paint.gui.menu.TreeMenu;

public class PaintDefaultComponentsFactory {

	public static Collection<? extends JMenuItem> createDefaultMenus() {
		Collection<JMenuItem> menus = new ArrayList<JMenuItem>();
		menus.add(new FileMenu());
		menus.add(new EditMenu());
		menus.add(new TreeMenu());
		menus.add(new MSAMenu());

		List<JMenu> viewMenus = new ViewMenus().getMenus();

		for (JMenu m : viewMenus){
			menus.add(m);
		}

		menus.add(new LayoutMenu());

		if (!OSUtil.isMacOSX()) {
//			menus.add(new HelpMenu(getAboutAction()));
		}
		return menus;
	}
	
}
