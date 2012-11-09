package org.uncertweb.ps;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Config {

	private static Config instance;
	private List<String> encodingClasses;
	private List<String> processClasses;
	private Map<String, List<String>> gsonTypeAdapterClasses;
	private Map<String, String> serverProperties;
	private Map<String, String> additionalProperties;	

	private final Logger logger = Logger.getLogger(Config.class);

	private Config() {
		// setup collections
		encodingClasses = new ArrayList<String>();
		processClasses = new ArrayList<String>();
		gsonTypeAdapterClasses = new HashMap<String, List<String>>();
		serverProperties = new HashMap<String, String>();
		additionalProperties = new HashMap<String, String>();
		
		// parse config file
		// FIXME: can throw more exceptions
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("config.json");
		JsonParser parser = new JsonParser();
		if (is == null) {
			logger.warn("Couldn't load config file, your experience may be limited.");
		}
		else {
			// read object
			JsonObject configObject = parser.parse(new InputStreamReader(is)).getAsJsonObject();

			// read json elements
			encodingClasses.addAll(parseStringArray(configObject.get("encodingClasses").getAsJsonArray()));
			processClasses.addAll(parseStringArray(configObject.get("processClasses").getAsJsonArray()));

			// read gson type adapters
			gsonTypeAdapterClasses.putAll(parseArrayMap(configObject.get("gsonTypeAdapterClasses").getAsJsonArray()));

			// read additional properties
			additionalProperties.putAll(parseStringMap(configObject.get("additionalProperties").getAsJsonArray()));
		}
	}

	private Map<String, String> parseStringMap(JsonArray map) {
		Map<String, String> hashmap = new HashMap<String, String>();
		for (JsonElement element : map) {
			JsonObject property = element.getAsJsonObject();
			for (Entry<String, JsonElement> entry : property.entrySet()) {
				hashmap.put(entry.getKey(), entry.getValue().getAsString());
				break;
			}
		}
		return hashmap;
	}

	private Map<String, List<String>> parseArrayMap(JsonArray map) {
		Map<String, List<String>> hashmap = new HashMap<String, List<String>>();
		for (JsonElement element : map) {
			JsonObject property = element.getAsJsonObject();
			for (Entry<String, JsonElement> entry : property.entrySet()) {
				List<String> values = new ArrayList<String>();
				if (entry.getValue().isJsonArray()) {
					for (JsonElement value : entry.getValue().getAsJsonArray()) {
						values.add(value.getAsString());
					}
				}
				else {
					values.add(entry.getValue().getAsString());
				}
				hashmap.put(entry.getKey(), values);
				break;
			}
		}
		return hashmap;
	}

	private List<String> parseStringArray(JsonArray array) {
		List<String> list = new ArrayList<String>();
		for (JsonElement e : array) {
			list.add(e.getAsString());
		}
		return list;
	}

	public List<String> getEncodingClasses() {
		return encodingClasses;
	}

	public List<String> getProcessClasses() {
		return processClasses;
	}

	public Map<String, List<String>> getGsonTypeAdapterClasses() {
		return gsonTypeAdapterClasses;
	}

	public String getAdditionalProperty(String key) {
		return additionalProperties.get(key);
	}

	public String setServerProperty(String key, String value) {
		return serverProperties.put(key, value);
	}

	public String getServerProperty(String key) {
		return serverProperties.get(key);
	}

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
		}
		return instance;
	}

}
