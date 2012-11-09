package org.uncertweb.ps.encoding;

import java.io.InputStream;
import java.io.OutputStream;

public interface Encoding {
	
	public abstract <T> T parse(InputStream inputStream, Class<T> type) throws ParseException;
	public abstract <T> void encode(T object, OutputStream outputStream) throws EncodeException;
	public abstract boolean isSupportedType(Class<?> type);
	public abstract boolean isSupportedMimeType(String mimeType);
	
}
