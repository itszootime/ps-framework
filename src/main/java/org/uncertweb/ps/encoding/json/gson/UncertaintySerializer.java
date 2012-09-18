package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import org.uncertml.IUncertainty;
import org.uncertml.io.JSONEncoder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class UncertaintySerializer implements JsonSerializer<IUncertainty> {

	public JsonElement serialize(IUncertainty src, Type typeOfSrc, JsonSerializationContext context) {
		// encode to json
		JSONEncoder encoder = new JSONEncoder();
		String json = encoder.encode(src);

		// convert to element
		JsonParser parser = new JsonParser();			
		return parser.parse(json);
	}

}
