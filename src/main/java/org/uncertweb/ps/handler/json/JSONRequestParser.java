package org.uncertweb.ps.handler.json;

import org.uncertweb.ps.data.Request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class JSONRequestParser {

	public static Request parse(JsonObject object) {
		// delegate to gson
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(object, Request.class);
	}
	
}
