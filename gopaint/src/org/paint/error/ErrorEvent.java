package org.paint.error;

import java.util.EventObject;

public class ErrorEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String message;

	public ErrorEvent(Object source,String m) {
		super(source);
		message = m;
	}

	public String getMsg() { return message; }

}
