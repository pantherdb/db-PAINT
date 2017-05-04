package org.paint.gui.event;

import java.util.EventListener;

public interface ProgressListener extends EventListener {

	public void handleProgressEvent(ProgressEvent event);
	
}
