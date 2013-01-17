package org.uncertweb.ps.handler.json.gson;

import java.lang.reflect.Type;

import org.uncertweb.ps.process.ProcessException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ProcessExceptionSerializer implements JsonSerializer<ProcessException> {

	public JsonElement serialize(ProcessException src, Type typeOfSrc, JsonSerializationContext context) {
		// create root
		JsonObject root = new JsonObject();
		
		// create exception
		JsonObject exception = new JsonObject();
		root.add("ProcessException", exception);
		
		// add messages
		exception.add("message", new JsonPrimitive(src.getMessage()));
		if (src.getCause() != null) {
			exception.add("detail", new JsonPrimitive("Caused by: " + src.getCause().toString()));
		}
		
		return root;
	}

}
