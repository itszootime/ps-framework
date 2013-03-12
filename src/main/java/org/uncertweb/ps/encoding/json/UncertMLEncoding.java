package org.uncertweb.ps.encoding.json;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.JSONEncoder;
import org.uncertml.io.JSONParser;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

import com.google.gson.JsonParseException;

public class UncertMLEncoding extends AbstractJSONEncoding {

	@Override
	public <T> T parse(String json, Class<T> type) throws ParseException {
		try {
			JSONParser parser = new JSONParser();
			return type.cast(parser.parse(json));
		}
		catch (UncertaintyParserException e) {
			throw new JsonParseException("Couldn't parse UncertML JSON.", e);
		}
	}

	@Override
	public <T> String encode(T object) throws EncodeException {
		// encode to json
		JSONEncoder encoder = new JSONEncoder();
		return encoder.encode((IUncertainty)object);
	}

	@Override
	public boolean isSupportedType(Class<?> classOf) {
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
