package org.paint.gui.event;

import java.util.EventListener;

public interface NodeColorChangeListener extends EventListener {

	public void handleNodeColorChangeEvent(NodeColorChangeEvent event);
	
}
