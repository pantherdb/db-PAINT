package org.paint.gui.event;

import java.util.EventListener;

public interface AnnotationChangeListener extends EventListener {

	public void handleAnnotationChangeEvent(AnnotationChangeEvent event);
	
}
