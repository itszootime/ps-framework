package org.uncertweb.ps.encoding.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.ParseException;

public abstract class AbstractXMLEncoding implements Encoding {

	public abstract <T> T parse(Content content, Class<T> type) throws ParseException;
	public abstract <T> Content encode(T object) throws EncodeException;

	public <T> T parse(InputStream inputStream, Class<T> type) throws ParseException {
		try {
			Document document = new SAXBuilder().build(inputStream);
			return parse(document.getRootElement(), type);
		}
		catch (IOException e) {
			throw new ParseException("Couldn't read XML from stream.", e);
		}
		catch (JDOMException e) {
			throw new ParseException("Couldn't parse XML from stream.", e);
		}
	}

	public <T> void encode(T object, OutputStream outputStream) throws EncodeException {
		try {
			// output
			Document document = new Document();
			document.addContent(encode(object).detach());
			new XMLOutputter().output(document, outputStream);
		}
		catch (IOException e) {
			throw new EncodeException("Couldn't write XML to stream.", e);
		}
	}
	
	public boolean isSupportedMimeType(String mimeType) {
		return mimeType.equals("text/xml");
	}
	
	public String getDefaultMimeType() {
		return "text/xml";
	}

	public abstract String getNamespace();
	public abstract String getSchemaLocation();	
	public abstract Include getInclude(Class<?> type);

	public abstract class Include {
		private String name;

		public Include(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public class IncludeType extends Include {
		public IncludeType(String name) {
			super(name);
		}
	}

	public class IncludeRef extends Include {
		public IncludeRef(String name) {
			super(name);
		}
	}

	public class IncludeList extends Include {
		public IncludeList(String name) {
			super(name);
		}
	}

}
