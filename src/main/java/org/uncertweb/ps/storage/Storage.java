package org.uncertweb.ps.storage;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.uncertweb.ps.Config;


public abstract class Storage {
	
	public static Storage getInstance() {
		// return whatever is configured for storage
		// or write to current dir if no other settings
		Path base = FileSystems.getDefault().getPath(Config.getInstance().getStorageProperty("baseFolder"));
		return new FlatFileStorage(base);
	}
	
	public abstract String put(byte[] content, String mimeType, String storedBy) throws StorageException;	
	public abstract StorageEntry get(String id) throws StorageException;
	public abstract boolean remove(String id) throws StorageException;

}
