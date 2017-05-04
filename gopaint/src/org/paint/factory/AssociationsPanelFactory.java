package org.paint.factory;

import org.bbop.framework.AbstractComponentFactory;
import org.paint.gui.association.AssociationsPanel;

public class AssociationsPanelFactory extends AbstractComponentFactory<AssociationsPanel> {

	public FactoryCategory getCategory() {
		return FactoryCategory.VIEWERS;
	}

	public String getName() {
		return "Annotations";
	}

	public boolean isSingleton() {
		return true;
	}

	public String getID() {
		return "associations";
	}

	@Override
	public AssociationsPanel doCreateComponent(String id) {
		return AssociationsPanel.inst();
	}

}
