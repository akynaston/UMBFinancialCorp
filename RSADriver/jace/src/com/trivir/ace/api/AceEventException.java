package com.trivir.ace.api;

public class AceEventException extends Exception {
	private static final long serialVersionUID = -345506527271971268L;
	private final String event;

	public AceEventException(String event, String message) {
		super(message);
		this.event = event;
	}

	public AceEventException(String event, String message, Exception e) {
		super(message, e);
		this.event = event;
	}
	
	public String getEvent() {
		return event;
	}
}
