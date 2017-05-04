package org.paint.gui.event;

import java.util.EventListener;

public interface TermSelectionListener extends EventListener {
	public void handleTermEvent(TermSelectEvent e);
}
