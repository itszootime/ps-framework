package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import org.uncertweb.api.om.exceptions.OMParsingException;
import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class OMDeserializer implements JsonDeserializer<IObservationCollection> {

	@Override
	public IObservationCollection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Gson gson = new GsonBuilder().create();
		JSONObservationParser parser = new JSONObservationParser();
		try {
			return parser.parse(gson.toJson(json));
		}
		catch (OMParsingException e) {
			throw new JsonParseException("Couldn't parse O&M JSON.", e);
		}		
	}

}
