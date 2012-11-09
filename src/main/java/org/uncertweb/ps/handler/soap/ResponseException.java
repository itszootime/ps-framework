package org.uncertweb.ps.handler.soap;

public class ResponseException extends Exception {

	private static final long serialVersionUID = 1L;

	public ResponseException(String message) {
		super(message);
	}

	public ResponseException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
