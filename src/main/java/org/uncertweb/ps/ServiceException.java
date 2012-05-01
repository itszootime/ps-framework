package org.uncertweb.ps;

public class ServiceException extends Throwable {
	
	private static final long serialVersionUID = 1L;
	private String detail;

	public ServiceException(String message) {
		super(message);
	}

	public ServiceException(String message, String detail) {
		this(message);
		this.detail = detail;
	}

	public String getDetail() {
		return detail;
	}
	
}