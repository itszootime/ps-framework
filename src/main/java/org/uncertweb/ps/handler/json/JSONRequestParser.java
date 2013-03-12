package org.uncertweb.ps.handler.json;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.handler.json.gson.GsonWrapper;

import com.google.gson.Gson;

public class JSONRequestParser {

	public static Request parse(InputStream in) throws RequestParseException {
		Gson gson = GsonWrapper.getGson();
		return gson.fromJson(new InputStreamReader(in), Request.class);
	}
	
}
