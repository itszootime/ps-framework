package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import org.uncertweb.api.om.exceptions.OMEncodingException;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.observation.collections.IObservationCollection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OMSerializer implements JsonSerializer<IObservationCollection> {

	@Override
	public JsonElement serialize(IObservationCollection src, Type typeOfSrc, JsonSerializationContext context) {
		// encode to json
		JSONObservationEncoder encoder = new JSONObservationEncoder();
		try {
			String json = encoder.encodeObservationCollection(src);
			
			// convert to element
			JsonParser parser = new JsonParser();			
			return parser.parse(json);
		}
		catch (OMEncodingException e) {
			// unlikely
			return null;
		}
	}

}
