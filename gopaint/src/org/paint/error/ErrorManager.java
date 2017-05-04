package org.paint.error;

//dataadapter?? datamodel??

import java.util.ArrayList;
import java.util.List;

public class ErrorManager {

	// group based?? separate managers for sep groups?? for now just 1
	private static ErrorManager singleton;
	private List<ErrorListener> errorListeners = new ArrayList<ErrorListener>(2);
	// is it funny to cache errors - handy for error displays that came up too late
	// to show errors past but still wanna see them like term info
	private List<ErrorEvent> errors = new ArrayList<ErrorEvent>(3);

	public static ErrorManager inst() {
		if (singleton == null) singleton = new ErrorManager();
		return singleton;
	}

	public static void reset() {
		singleton = null;
	}

	public void error(ErrorEvent e) {
		for (ErrorListener l : errorListeners) {
			l.handleError(e);
		}
		errors.add(e);
	}

	public List<ErrorEvent> getErrors() {
		return errors;
	}

	// public void debug(e) - e.getClass() ?? or e.isDebug() e.isInfo?
	// info(d) ??

	public void addErrorListener(ErrorListener el) {
		errorListeners.add(el);
	}

}
