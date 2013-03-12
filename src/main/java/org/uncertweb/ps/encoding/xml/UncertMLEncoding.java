package org.uncertweb.ps.encoding.xml;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.uncertml.IUncertainty;
import org.uncertml.io.XMLEncoder;
import org.uncertml.io.XMLParser;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.EncodingHelper;
import org.uncertweb.ps.encoding.ParseException;

public class UncertMLEncoding extends AbstractXMLEncoding {

	@Override
	public <T> T parse(Content content, Class<T> type) throws ParseException {
		// try to parse it
		try {
			Document document = new Document();
			document.addContent(((Element)content).detach());
			String uncertml = new XMLOutputter(Format.getCompactFormat().setOmitDeclaration(true)).outputString(document);
			XMLParser parser = new XMLParser();
			return type.cast(parser.parse(uncertml));
		}
		catch (Exception e) {
			throw new ParseException("Couldn't parse UncertML.", e);
		}
	}

	@Override
	public <T> Content encode(T object) throws EncodeException {
		try {
			XMLEncoder encoder = new XMLEncoder();
			String uncertml = encoder.encode((IUncertainty) object);
			return new SAXBuilder().build(new ByteArrayInputStream(uncertml.getBytes())).getRootElement();
		}
		catch (Exception e) {
			throw new EncodeException("Couldn't encode UncertML.", e);
		}
	}

	@Override
	public String getNamespace() {
		return "http://www.uncertml.org/2.0";
	}

	@Override
	public String getSchemaLocation() {
		return "http://52north.org/schema/geostatistics/uncertweb/Schema/uncertml/uncertml2.xsd";
	}

	@Override
	public Include getInclude(Class<?> type) {
		return new IncludeRef(type.getSimpleName());
	}

	@Override
	public boolean isSupportedType(Class<?> type) {
		if (type instanceof Class) {
			Class<?> typeClass = (Class<?>) type;
			if (!typeClass.isInterface() && !Modifier.isAbstract(typeClass.getModifiers())) {
				Set<Class<?>> interfaces = EncodingHelper.getInterfaces(typeClass);
				return interfaces.contains(IUncertainty.class);
			}
		}
		return false;
	}

}
