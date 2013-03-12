package org.uncertweb.ps.handler.json;

import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.handler.json.gson.GsonWrapper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JSONRequestParser {

	public static Request parse(JsonObject object) {
		Gson gson = GsonWrapper.getGson();
		return gson.fromJson(object, Request.class);
	}
	
}
