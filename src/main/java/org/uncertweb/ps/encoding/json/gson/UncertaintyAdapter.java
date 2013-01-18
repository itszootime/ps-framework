package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.JSONEncoder;
import org.uncertml.io.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class UncertaintyAdapter implements JsonSerializer<IUncertainty>, JsonDeserializer<IUncertainty> {
	
	public JsonElement serialize(IUncertainty src, Type typeOfSrc, JsonSerializationContext context) {
		// encode to json
		JSONEncoder encoder = new JSONEncoder();
		String json = encoder.encode(src);

		// convert to element
		JsonParser parser = new JsonParser();			
		return parser.parse(json);
	}

	public IUncertainty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			// convert element to string
			Gson gson = new GsonBuilder().create();
			String string = gson.toJson(json);

			JSONParser parser = new JSONParser();
			return parser.parse(string);
		}
		catch (UncertaintyParserException e) {
			throw new JsonParseException("Couldn't parse UncertML JSON.", e);
		}
	}

}
