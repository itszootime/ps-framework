package org.uncertweb.ps.storage;

public abstract class Storage {
	
	public static Storage getInstance() {
		// return whatever is configured for storage
		return null;
	}
	
	public abstract String put(byte[] content, String mimeType, String storedBy) throws StorageException;	
	public abstract StorageEntry get(String id) throws StorageException;
	public abstract boolean remove(String id) throws StorageException;

}
