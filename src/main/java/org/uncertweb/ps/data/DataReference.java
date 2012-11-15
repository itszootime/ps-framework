package org.uncertweb.ps.data;

import java.net.URL;

public class DataReference {
	
	private URL url;
	private String mimeType;
	private boolean compressed;

	public DataReference(URL url, String mimeType, boolean compressed) {
		this.url = url;
		this.mimeType = mimeType;
		this.compressed = compressed;
	}

	public DataReference(URL url, boolean compressed) {
		this(url, null, compressed);
	}

	public DataReference(URL url, String mimeType) {
		this(url, mimeType, false);
	}

	public DataReference(URL url) {
		this(url, null, false);
	}

	public URL getURL() {
		return url;
	}

	public String getMimeType() {
		return mimeType;
	}

	public boolean isCompressed() {
		return compressed;
	}

}
