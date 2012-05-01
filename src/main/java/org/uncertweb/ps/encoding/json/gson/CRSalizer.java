package org.uncertweb.ps.encoding.json.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

public class CRSalizer {

	public static JsonElement serialize(int srid) {
		if (srid == 0) return new JsonNull();
		else {
			JsonObject json = new JsonObject();
			json.addProperty("type", "name");
			JsonObject props = new JsonObject();
			props.addProperty("name", "urn:ogc:crs:EPSG::" + srid);
			json.add("properties", props);
			return json;
		}
	}

	public static int deserialize(JsonElement json) {
		String name = json.getAsJsonObject().get("properties").getAsJsonObject().get("name").getAsString();
		if (name.contains(":")) {
			String[] tokens = name.split(":");
			return Integer.valueOf(tokens[tokens.length - 1]);
		}
		// must be just a number
		return Integer.valueOf(name);
	}

}
