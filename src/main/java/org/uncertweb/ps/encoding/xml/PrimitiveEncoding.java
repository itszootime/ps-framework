package org.uncertweb.ps.encoding.xml;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.jdom.Content;
import org.jdom.Text;
import org.joda.time.Chronology;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

public class PrimitiveEncoding extends AbstractXMLEncoding {

	private static final String NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(new Class<?>[] {
			Double.class, Double[].class, Integer.class, Integer[].class, String.class, String[].class,
			Float.class, Float[].class, DateMidnight.class, DateMidnight[].class, URI.class, URI[].class,
			Boolean.class, Boolean[].class
	});

	public <T> T parse(Content content, Class<T> type) throws ParseException {		
		// get text to parse
		if (!(content instanceof Text)) {
			throw new ParseException("Can't parse non-text content.");
		}
		Text textContent = (Text)content;
		String[] elementText = textContent.getText().split("\\s+");

		// parse class name
		boolean isArrayClass = type.isArray();
		String typeName = parseTypeName(type);	
		String simpleTypeName = parseSimpleTypeName(type);

		// special case
		if (simpleTypeName.equals("String") && !isArrayClass) {
			return type.cast(textContent.getText());
		}

		// parse
		// TODO: not using full class names could be risky
		List<Object> objects = new ArrayList<Object>();
		for (String text : elementText) {
			switch (simpleTypeName) {
				case "DateMidnight":
					try {
						DateTime dateTime = new DateTime(DatatypeConverter.parseDate(text));
						Chronology chronology = ISOChronology.getInstance(dateTime.getZone());
						DateMidnight date = new DateMidnight(dateTime, chronology);
						objects.add(date);
					}
					catch (IllegalArgumentException e) {
						throw new ParseException("Invalid date format.", e);
					}
					break;
				case "URI":
					try {
						objects.add(new URI(text));
					}
					catch (URISyntaxException e) {
						throw new ParseException("Couldn't parse URI with bad syntax.", e);
					}
					break;
				case "Float":
					objects.add(Float.parseFloat(text));
					break;
				case "Double":
					objects.add(Double.parseDouble(text));
					break;
				case "Boolean":
					Boolean bool;
					if (text.equals("0")) {
						bool = false;
					}
					else if (text.equals("1")) {
						bool = true;
					}
					else {
						bool = Boolean.parseBoolean(text);
					}
					objects.add(bool);
					break;
				case "Integer":
					objects.add(Integer.parseInt(text));
					break;
				case "String":
					objects.add(text);
					break;
			}
		}

		if (isArrayClass) {
			try {
				Object array = Array.newInstance(Class.forName(typeName), objects.size());
				for (int i = 0; i < objects.size(); i++) {
					Array.set(array, i, objects.get(i));
				}
				return type.cast(array);
			}
			catch (ClassNotFoundException e) {
				// shouldn't happen, only using java primitive classes
				return null;
			}
		}
		else {
			return type.cast(objects.get(0));
		}
	}
	
	@Override
	public boolean isSupportedMimeType(String mimeType) {
		return super.isSupportedMimeType(mimeType) || mimeType.equals(getDefaultMimeType());
	}
	
	@Override
	public String getDefaultMimeType() {
		return "text/plain";
	}
	
	@Override
	public <T> Text encode(T object) throws EncodeException {
		Class<?> type = object.getClass();
		StringBuilder string = new StringBuilder();
		if (type.isArray()) {
			int length = Array.getLength(object);
			for (int i = 0; i < length; i++) {
				Text encoded = encode(Array.get(object, i));
				string.append(encoded.getText());
				string.append(" ");
			}
			string.deleteCharAt(string.length() - 1);
		}
		else {
			if (object instanceof DateMidnight) {
				DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-ddZZ");
				string.append(format.print((DateMidnight)object));
			}
			else {
				string.append(object.toString());
			}
		}
		return new Text(string.toString());
	}

	@Override
	public boolean isSupportedType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type);
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String getSchemaLocation() {
		return null;
	}

	@Override
	public Include getInclude(Class<?> type) {
		// get name for include
		// TODO: not using full class names could be risky
		String name = parseSimpleTypeName(type);
		switch (name) {
			case "URI":
				name = "anyURI";
				break;
			case "DateMidnight":
				name = "date";
				break;
			default:
				name = name.toLowerCase();
				break;
		}
		
		// return include
		if (type.isArray()) {
			return new IncludeList(name);
		}
		else {
			return new IncludeType(name);
		}
	}
	
	private String parseTypeName(Class<?> type) {
		String typeName = type.getName();
		if (typeName.startsWith("[L")) {
			typeName = typeName.substring(2);
		}
		if (typeName.endsWith(";")) {
			typeName = typeName.substring(0, typeName.length() - 1);
		}
		return typeName;
	}
	
	private String parseSimpleTypeName(Class<?> type) {
		String typeName = parseTypeName(type);
		return typeName.substring(typeName.lastIndexOf(".") + 1);
	}

}
