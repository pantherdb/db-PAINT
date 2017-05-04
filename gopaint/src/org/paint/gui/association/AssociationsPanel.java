package org.paint.gui.association;

import java.awt.BorderLayout;

import org.paint.gui.AbstractPaintGUIComponent;

public class AssociationsPanel extends AbstractPaintGUIComponent { //, FamilyChangeListener, GeneSelectListener, TermSelectionListener {

	private static final long serialVersionUID = 1L;
	private static AssociationsPanel singleton;
	
	private AssociationsPanel() {
		super("associations:associations");
		initLayout();
	}
	
	private void initLayout() {
		BorderLayout layout = new BorderLayout();
		setLayout(layout);

		setOpaque(true);
				
		AssociationList listPanel = new AssociationList();

		add(listPanel, BorderLayout.CENTER);
	}

	public static AssociationsPanel inst() {
		if (singleton == null) {
			singleton = new AssociationsPanel();
		}
		return singleton;
	}

}
