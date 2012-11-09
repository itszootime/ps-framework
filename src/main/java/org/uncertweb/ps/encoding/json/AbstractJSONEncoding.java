package org.uncertweb.ps.encoding.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.ParseException;

public abstract class AbstractJSONEncoding implements Encoding {
	
	public abstract <T> T parse(String json, Class<T> type) throws ParseException;
	public abstract <T> String encode(T object) throws EncodeException;
	
	public <T> T parse(InputStream inputStream, Class<T> type) throws ParseException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			StringWriter writer = new StringWriter();
			char[] buf = new char[1024];
			int n;
			while ((n = reader.read(buf)) != -1) {
				writer.write(buf, 0, n);
			}
			return type.cast(parse(writer.toString(), type));
		}
		catch (IOException e) {
			throw new ParseException("Couldn't read JSON.", e);
		}
	}

	public <T> void encode(T object, OutputStream outputStream) throws EncodeException {
		try {
			StringReader reader = new StringReader(encode(object));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
			char[] buf = new char[1024];
			int n;
			while ((n = reader.read(buf)) != -1) {
				writer.write(buf, 0, n);
			}
		}
		catch (IOException e) {
			throw new EncodeException("Couldn't write JSON.", e);
		}
	}
	
	public boolean isSupportedMimeType(String mimeType) {
		return mimeType.equals("application/json");
	}
	
	public abstract boolean isSupportedType(Class<?> type);

}
