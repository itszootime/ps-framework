package org.uncertweb.ps.storage;

import org.joda.time.DateTime;

public class StorageEntry {
	
	private byte[] content;
	private String mimeType;
	private String storedBy;
	private DateTime storedAt;
	
	public StorageEntry(byte[] content, String mimeType, String storedBy, DateTime storedAt) {
		super();
		this.content = content;
		this.mimeType = mimeType;
		this.storedBy = storedBy;
		this.storedAt = storedAt;
	}

	public byte[] getContent() {
		return content;
	}
	
	public String getMimeType() {
		return mimeType;
	}
	
	public DateTime getStoredAt() {
		return storedAt;
	}
	
	public String getStoredBy() {
		return storedBy;
	}
	
}
