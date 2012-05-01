package org.uncertweb.ps.encoding.xml;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.uncertml.IUncertainty;
import org.uncertml.io.XMLEncoder;
import org.uncertml.io.XMLParser;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

public class UncertMLEncoding extends AbstractXMLEncoding {

	public Object parse(Element element, Class<?> classOf) throws ParseException {
		// try to parse it
		try {
			Document document = new Document();
			document.addContent(element.detach());
			String uncertml = new XMLOutputter(Format.getCompactFormat().setOmitDeclaration(true)).outputString(document);
			XMLParser parser = new XMLParser();
			return parser.parse(uncertml);
		}
		catch (Exception e) {
			throw new ParseException("Couldn't parse UncertML.", e);
		}
	}

	public Element encode(Object object) throws EncodeException {
		try {
			XMLEncoder encoder = new XMLEncoder();
			String uncertml = encoder.encode((IUncertainty) object);
			return new SAXBuilder().build(new ByteArrayInputStream(uncertml.getBytes())).getRootElement();
		}
		catch (Exception e) {
			throw new EncodeException("Couldn't encode UncertML.", e);
		}
	}

	public String getNamespace() {
		return "http://www.uncertml.org/2.0";
	}

	public String getSchemaLocation() {
		return "http://v-mars.uni-muenster.de/uncertweb/schema/uncertml/uncertml.xsd";
	}

	public Include getIncludeForClass(Class<?> classOf) {
		return new IncludeRef(classOf.getSimpleName());
	}

	public boolean isSupportedClass(Class<?> classOf) {
		if (classOf instanceof Class) {
			Class<?> typeClass = (Class<?>) classOf;
			if (!typeClass.isInterface() && !Modifier.isAbstract(typeClass.getModifiers())) {
				List<Class<?>> interfaces = getInterfaces(typeClass);
				return interfaces.contains(IUncertainty.class);
			}
		}
		return false;
	}

	private List<Class<?>> getInterfaces(Class<?> clazz) {
		ArrayList<Class<?>> interfaces = new ArrayList<Class<?>>();
		for (Class<?> interf : clazz.getInterfaces()) {
			interfaces.add(interf);
			interfaces.addAll(getInterfaces(interf));
		}
		return interfaces;
	}

}
