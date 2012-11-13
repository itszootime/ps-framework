package org.uncertweb.ps.handler;

public class RequestParseException extends Exception {
	
	private static final long serialVersionUID = -1169084283417448299L;

	public RequestParseException(String message) {
		super(message);
	}

	public RequestParseException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
