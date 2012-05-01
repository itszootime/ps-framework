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

	public abstract boolean isSupportedClass(Class<?> classOf);

	public Object parse(InputStream is, Class<?> classOf) throws ParseException {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringWriter writer = new StringWriter();
			char[] buf = new char[1024];
			int n;
			while ((n = reader.read(buf)) != -1) {
				writer.write(buf, 0, n);
			}
			return parse(writer.toString(), classOf);
		}
		catch (IOException e) {
			throw new ParseException("Couldn't parse JSON.", e);
		}
	}

	public void encode(Object o, OutputStream os) throws EncodeException {
		try {
			StringReader reader = new StringReader(encode(o));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
			char[] buf = new char[1024];
			int n;
			while ((n = reader.read(buf)) != -1) {
				writer.write(buf, 0, n);
			}
		}
		catch (IOException e) {
			throw new EncodeException("Couldn't encode JSON.", e);
		}
	}

	public abstract Object parse(String s, Class<?> classOf) throws ParseException;

	public abstract String encode(Object o) throws EncodeException;

}
