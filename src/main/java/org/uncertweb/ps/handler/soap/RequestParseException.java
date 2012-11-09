package org.uncertweb.ps.handler.soap;

public class RequestParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public RequestParseException(String message) {
		super(message);
	}

	public RequestParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
