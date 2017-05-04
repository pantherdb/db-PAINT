package org.paint.gui;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.View;

import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.ComponentManager;
import org.bbop.framework.dock.idw.IDWDriver;
import org.paint.main.PaintDefaultComponentsFactory;

public abstract class AbstractPaintGUIComponent extends AbstractGUIComponent {

	private static final long serialVersionUID = 1L;

	public AbstractPaintGUIComponent(String id) {
		super(id);

		addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentResized(ComponentEvent arg0) {
				final View view = getIdwView();
				view.addListener(new DockingWindowAdapter() {

					@Override
					public void windowUndocked(DockingWindow window) {
						if (window.getRootPane().getJMenuBar() == null) {
							JMenuBar menu = new JMenuBar();
							for (JMenuItem item : PaintDefaultComponentsFactory.createDefaultMenus()) {
								menu.add(item);
							}
							window.getRootPane().setJMenuBar(menu);
						}
					}
					
				});
			}
			
		});
	}
	
	protected View getIdwView() {
		View view = null;
		while (view == null) {
			view = ((IDWDriver)ComponentManager.getManager().getDriver()).getView(this);
		}
		return view;
	}

}
