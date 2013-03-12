package org.uncertweb.ps.encoding.json;

import java.lang.reflect.Modifier;
import java.util.Set;

import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.JSONEncoder;
import org.uncertml.io.JSONParser;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.EncodingHelper;
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
		JSONEncoder encoder = new JSONEncoder();
		return encoder.encode((IUncertainty)object);
	}

	@Override
	public boolean isSupportedType(Class<?> type) {
		if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
			Set<Class<?>> interfaces = EncodingHelper.getInterfaces(type);
			return interfaces.contains(IUncertainty.class);
		}
		return false;
	}

}
