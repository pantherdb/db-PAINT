package org.paint.factory;

import org.bbop.framework.AbstractComponentFactory;
import org.paint.gui.FamilyViews;


public class GeneTreeFactory extends AbstractComponentFactory<FamilyViews> {

		public String getName() {
			return "Protein Family";
		}

		public boolean isSingleton() {
			return true;
		}

		public String getID() {
			return "tree-info";
		}

		@Override
		public FamilyViews doCreateComponent(String id) {
			return FamilyViews.inst();
		}
				
		
		public FactoryCategory getCategory() {
			return FactoryCategory.VIEWERS;
		}


}

