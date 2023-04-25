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
package org.paint.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.bbop.framework.LayoutMenu;
import org.bbop.framework.ViewMenus;
import org.paint.gui.menu.EditMenu;
import org.paint.gui.menu.FileMenu;
import org.paint.gui.menu.HelpMenu;
import org.paint.gui.menu.MSAMenu;
import org.paint.gui.menu.TreeMenu;

public class PaintDefaultComponentsFactory {

	public static Collection<? extends JMenuItem> createDefaultMenus() {
		Collection<JMenuItem> menus = new ArrayList<JMenuItem>();
		menus.add(new FileMenu());
		menus.add(new EditMenu());
		menus.add(new TreeMenu());
		menus.add(new MSAMenu());
		menus.add(new HelpMenu());

		List<JMenu> viewMenus = new ViewMenus().getMenus();

		for (JMenu m : viewMenus){
			menus.add(m);
		}

		menus.add(new LayoutMenu());

//		if (!OSUtil.isMacOSX()) {
////			menus.add(new HelpMenu(getAboutAction()));
//		}
                
		return menus;
	}
	
}
