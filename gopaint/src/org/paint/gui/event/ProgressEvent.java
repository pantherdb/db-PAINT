package org.paint.gui.event;

import java.util.EventObject;

public class ProgressEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	public enum Status {
		START,
		END,
		RUNNING,
		FAIL
	}
	
	private String message;
	private int percentageDone;
	private Status status;
	
	public ProgressEvent(Object source, String message, int percentageDone) {
		this(source, message, percentageDone, Status.RUNNING);
	}
	
	public ProgressEvent(Object source, String message, int percentageDone, Status status) {
		super(source);
		this.message = message;
		this.percentageDone = percentageDone;
		this.status = status;
	}

	public String getMessage() {
		return message;
	}
	
	public int getPercentageDone() {
		return percentageDone;
	}
	
	public Status getStatus() {
		return status;
	}
}
