package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import org.uncertml.IUncertainty;
import org.uncertml.exception.UncertaintyParserException;
import org.uncertml.io.JSONParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class UncertaintyDeserializer implements JsonDeserializer<IUncertainty> {

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
