package org.paint.gui.event;

import java.util.EventListener;

public interface AnnotationDragListener extends EventListener {

	public void handleAspectChangeEvent(AnnotationDragEvent event);
	
}
