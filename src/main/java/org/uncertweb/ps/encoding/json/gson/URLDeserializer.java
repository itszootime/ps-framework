package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class URLDeserializer implements JsonDeserializer<URL> {

	public URL deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			return new URL(json.getAsString());
		}
		catch (MalformedURLException e) {
			throw new JsonParseException(e);
		}
	}

	
	
}
