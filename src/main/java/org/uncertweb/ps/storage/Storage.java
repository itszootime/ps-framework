package org.uncertweb.ps.storage;

public abstract class Storage {
	
	public static Storage getInstance() {
		// return whatever is configured for storage
	}
	
	public abstract String put(Object object);	
	public abstract Object get(String id);

}
