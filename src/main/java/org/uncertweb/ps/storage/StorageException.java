package org.uncertweb.ps.storage;

public class StorageException extends Exception {
	
	private static final long serialVersionUID = -5302667584149555881L;

	public StorageException(String message) {
		super(message);
	}
	
	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}

}
