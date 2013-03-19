package org.uncertweb.ps.handler.json.gson;

import java.lang.reflect.Type;

import org.uncertweb.ps.data.Output;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.json.AbstractJSONEncoding;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResponseSerializer implements JsonSerializer<Response> {

	public JsonElement serialize(Response src, Type typeOfSrc, JsonSerializationContext context) {
		// create base object
		JsonObject object = new JsonObject();

		// create response object
		JsonObject response = new JsonObject();

		// add process identifier
		object.add(src.getProcessIdentifier() + "Response", response);

		// add each output
		for (Output output : src.getOutputs()) {
			try {
				// create appropriate element
				JsonElement outputElement;
				if (output.isMultipleOutput()) {
					JsonArray outputArray = new JsonArray();
					for (Object o : output.getAsMultipleOutput().getObjects()) {
						outputArray.add(encodeDataElement(o, context));
					}
					outputElement = outputArray;
				}
				else {
					outputElement = encodeDataElement(output.getAsSingleOutput().getObject(), context);
				}
				response.add(output.getIdentifier(), outputElement);
			}
			catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}

		return object;
	}

	private JsonElement encodeDataElement(Object src, JsonSerializationContext context) throws EncodeException {
		// get type of object
		Class<?> type = src.getClass();

		// look in the factory first
		AbstractJSONEncoding encoding = EncodingRepository.getInstance().getJSONEncoding(type);
		if (encoding != null) {
			// encode to string
			String json = encoding.encode(src);
			// return as element
			JsonParser parser = new JsonParser();
			return parser.parse(json);
		}
		else {
			// try and encode with gson
			try {
				return context.serialize(src);
			}
			catch (JsonParseException e) {
				throw new EncodeException("Couldn't automatically encode " + type.getSimpleName() + " type.");
			}
		}
	}

}
