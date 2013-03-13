package org.uncertweb.ps.handler.json;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.encoding.ParseException;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.handler.json.gson.GsonWrapper;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class JSONRequestParser {

	public static Request parse(InputStream in) throws RequestParseException {
		Gson gson = GsonWrapper.getGson();
		try {
			return gson.fromJson(new InputStreamReader(in), Request.class);
		}
		catch (JsonParseException e) {
			Throwable cause = e.getCause();
			String message = e.getMessage();
			if (cause instanceof ParseException) {
				message = cause.getMessage();
			}
			throw new RequestParseException(message);
		}
	}
	
}
