package org.uncertweb.ps;

public class ClientException extends Exception {
	
	private static final long serialVersionUID = 1L;
	private String detail;

	public ClientException(String message) {
		super(message);
	}

	public ClientException(String message, String detail) {
		this(message);
		this.detail = detail;
	}

	public String getDetail() {
		return detail;
	}
	
}