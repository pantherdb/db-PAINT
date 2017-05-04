package org.paint.factory;

import org.bbop.framework.AbstractComponentFactory;
import org.paint.gui.StatusView;

public class StatusViewFactory extends AbstractComponentFactory<StatusView> {

	public FactoryCategory getCategory() {
		return FactoryCategory.INFO;
	}

	public String getName() {
		return "Status";
	}

	public boolean isSingleton() {
		return true;
	}

	public String getID() {
		return "status-info";
	}

	@Override
	public StatusView doCreateComponent(String id) {
		return StatusView.getSingleton();
	}

}
