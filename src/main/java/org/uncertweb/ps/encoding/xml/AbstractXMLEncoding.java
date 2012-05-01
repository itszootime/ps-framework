package org.uncertweb.ps.encoding.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.ParseException;

public abstract class AbstractXMLEncoding implements Encoding {
	
	public boolean isSupportedMimeType(String mimeType) {
		return mimeType.equals("text/xml");
	}

	public abstract Object parse(Element element, Class<?> classOf) throws ParseException;
	public abstract Element encode(Object object) throws EncodeException;

	public Object parse(InputStream is, Class<?> classOf) throws ParseException {
		try {
			// parse
			/*
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = r.readLine()) != null) {
				System.out.println(line);
			}*/
			Document document = new SAXBuilder().build(is);
			return parse(document.getRootElement(), classOf);
		}
		catch (IOException e) {
			throw new ParseException("Couldn't parse XML from stream.", e);
		}
		catch (JDOMException e) {
			throw new ParseException("Couldn't parse XML from stream.", e);
		}
	}

	public void encode(Object o, OutputStream os) throws EncodeException {
		try {
			// output
			Document document = new Document();
			document.addContent(encode(o).detach());
			new XMLOutputter().output(document, os);
		}
		catch (IOException e) {
			throw new EncodeException("Couldn't encode XML to stream.", e);
		}
	}

	public abstract String getNamespace();
	public abstract String getSchemaLocation();	
	public abstract Include getIncludeForClass(Class<?> classOf);

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
