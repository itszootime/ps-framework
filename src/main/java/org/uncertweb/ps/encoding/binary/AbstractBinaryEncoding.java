package org.uncertweb.ps.encoding.binary;

import org.uncertweb.ps.encoding.Encoding;

public abstract class AbstractBinaryEncoding implements Encoding {
	
	public abstract boolean isSupportedMimeType(String mimeType);
	
}
