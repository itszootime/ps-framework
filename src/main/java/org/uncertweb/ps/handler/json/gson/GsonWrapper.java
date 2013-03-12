package org.uncertweb.ps.handler.json.gson;

import org.uncertweb.ps.ServiceException;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.process.ProcessException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonWrapper {
	
	private static Gson gson;

	public static Gson getGson() {
		if (gson == null) {
			gson = buildGson();
		}
		return gson;
	}
	
	private static Gson buildGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.disableHtmlEscaping();
		gsonBuilder.registerTypeAdapter(Request.class, new RequestDeserializer());
		gsonBuilder.registerTypeAdapter(Response.class, new ResponseSerializer());
		gsonBuilder.registerTypeAdapter(ServiceException.class, new ServiceExceptionSerializer());
		gsonBuilder.registerTypeAdapter(ProcessException.class, new ProcessExceptionSerializer());

		// register additional ones in config
//		for (Entry<String, List<String>> entry : Config.getInstance().getGsonTypeAdapterClasses().entrySet()) {
//			try {
//				Class<?> type = Class.forName(entry.getKey());
//				for (String adapterString : entry.getValue()) {
//					try {
//						Object typeAdapter = Class.forName(adapterString).newInstance();
//						gsonBuilder.registerTypeAdapter(type, typeAdapter);
//						logger.info("Loaded Gson type adapter " + adapterString + " for type " + entry.getKey() + ".");
//					}
//					catch (ClassNotFoundException e) {
//						logger.error("Couldn't find Gson type adapter " + adapterString + ", skipping.");
//					}
//					catch (InstantiationException e) {
//						logger.error("Couldn't instantiate Gson type adapter " + adapterString + ", skipping.");
//					}
//					catch (IllegalAccessException e) {
//						logger.error("Couldn't access Gson type adapter " + adapterString + ", skipping.");
//					}
//				}
//			}
//			catch (ClassNotFoundException e) {
//				logger.error("Couldn't find type " + entry.getKey() + ", skipping adding Gson type adapter.");
//			}
//		}		

		return gsonBuilder.create();
	}
	
}
