package org.uncertweb.ps.encoding;

import java.io.InputStream;
import java.io.OutputStream;

public interface Encoding {

	public abstract boolean isSupportedClass(Class<?> classOf);
	public abstract Object parse(InputStream is, Class<?> classOf) throws ParseException;
	public abstract void encode(Object o, OutputStream os) throws EncodeException;
	public abstract boolean isSupportedMimeType(String mimeType);
	
}
