package org.paint.main;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import org.bbop.framework.MainFrame;
import org.bbop.swing.EnhancedMenuBar;

public class PaintFrame extends MainFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	private static final String logoFile = "images/paints.jpg";    
	private EnhancedMenuBar menubar;
	protected JToolBar mainToolBar;
	protected boolean lockDock;

//	private static Logger LOG = Logger.getLogger(PaintFrame.class);

	public PaintFrame(String title) {
		super(title);
		try {
			menubar = new EnhancedMenuBar();
			mainToolBar = new JToolBar(); // StandardToolbar().getComponent();
			lockDock=true;  //by default, the doc will not be locked
			setJMenuBar(menubar);
			setToolBar(mainToolBar);
			addMenus();
			createListeners();
                        
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addMenu(JMenu menu) {
		menubar.add(menu);
	}

	public void setHelpMenu(JMenu menu) {
		menubar.add(menu);
	}

	protected void addMenus() {
	}
	
	//right now this is one toolbar.  but could conceiveably be a panel of toolbars
	public void setToolBar(JToolBar toolbar) {
		mainToolBar = toolbar;
	}
	
	public void setLockDoc(boolean lock) {
		lockDock = lock;
	}
	
	public boolean getLockDoc() {
		return lockDock;
	}

	protected void createListeners() {
	}

	public Component add(Component c) {
		Component x = this.add(c);
		System.out.println ("Added component to main frame: " + c.getName());
		return x;
	}
}
