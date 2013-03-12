package org.uncertweb.ps.handler.json.gson;

import java.lang.reflect.Type;

import org.uncertweb.ps.ServiceException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ServiceExceptionSerializer implements JsonSerializer<ServiceException> {

	public JsonElement serialize(ServiceException src, Type typeOfSrc, JsonSerializationContext context) {
		// create root
		JsonObject root = new JsonObject();
		
		// create exception
		JsonObject exception = new JsonObject();
		root.add("ServiceException", exception);
		
		// add messages
		exception.add("message", new JsonPrimitive(src.getMessage()));
		exception.add("detail", new JsonPrimitive(src.getDetail()));
		
		return root;
	}	

}
