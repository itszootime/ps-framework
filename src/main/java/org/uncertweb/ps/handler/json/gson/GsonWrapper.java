package org.uncertweb.ps.handler.json.gson;

import java.awt.Point;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import org.uncertml.distribution.multivariate.DirichletDistribution;
import org.uncertml.statistic.DiscreteProbability;
import org.uncertweb.ps.Config;
import org.uncertweb.ps.ServiceException;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.encoding.json.gson.GeometryDeserializer;
import org.uncertweb.ps.encoding.json.gson.GeometrySerializer;
import org.uncertweb.ps.encoding.json.gson.URLDeserializer;
import org.uncertweb.ps.encoding.json.gson.UncertaintyDeserializer;
import org.uncertweb.ps.encoding.json.gson.UncertaintySerializer;
import org.uncertweb.ps.process.ProcessException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonWrapper {
	
	private Gson gson;

	public Gson getGson() {
		if (gson == null) {
			gson = buildGson();
		}
		return gson;
	}
	
	private Gson buildGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.disableHtmlEscaping();
		gsonBuilder.registerTypeAdapter(Request.class, new RequestDeserializer());
		gsonBuilder.registerTypeAdapter(Response.class, new ResponseSerializer());
		gsonBuilder.registerTypeAdapter(ServiceException.class, new ServiceExceptionSerializer());
		gsonBuilder.registerTypeAdapter(ProcessException.class, new ProcessExceptionSerializer());

		// some built in support here, should be much more!
		gsonBuilder.registerTypeAdapter(URL.class, new URLDeserializer());
		gsonBuilder.registerTypeAdapter(Point.class, new GeometryDeserializer());
		gsonBuilder.registerTypeAdapter(Point.class, new GeometrySerializer());	
		gsonBuilder.registerTypeAdapter(DirichletDistribution.class, new UncertaintyDeserializer());
		gsonBuilder.registerTypeAdapter(DirichletDistribution.class, new UncertaintySerializer());
		gsonBuilder.registerTypeAdapter(DiscreteProbability.class, new UncertaintyDeserializer());
		gsonBuilder.registerTypeAdapter(DiscreteProbability.class, new UncertaintySerializer());

		// register additional ones in config
		for (Entry<String, List<String>> entry : Config.getInstance().getGsonTypeAdapterClasses().entrySet()) {
			try {
				Class<?> type = Class.forName(entry.getKey());
				for (String adapterString : entry.getValue()) {
					try {
						Object typeAdapter = Class.forName(adapterString).newInstance();
						gsonBuilder.registerTypeAdapter(type, typeAdapter);
						logger.info("Loaded Gson type adapter " + adapterString + " for type " + entry.getKey() + ".");
					}
					catch (ClassNotFoundException e) {
						logger.error("Couldn't find Gson type adapter " + adapterString + ", skipping.");
					}
					catch (InstantiationException e) {
						logger.error("Couldn't instantiate Gson type adapter " + adapterString + ", skipping.");
					}
					catch (IllegalAccessException e) {
						logger.error("Couldn't access Gson type adapter " + adapterString + ", skipping.");
					}
				}
			}
			catch (ClassNotFoundException e) {
				logger.error("Couldn't find type " + entry.getKey() + ", skipping adding Gson type adapter.");
			}
		}		

		return gsonBuilder.create();
	}
	
}
