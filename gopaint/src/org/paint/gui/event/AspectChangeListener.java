package org.paint.gui.event;

import java.util.EventListener;

public interface AspectChangeListener extends EventListener {

	public void handleAspectChangeEvent(AspectChangeEvent event);
	
}
