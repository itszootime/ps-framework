package org.uncertweb.ps;

import java.awt.Point;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.uncertml.distribution.multivariate.DirichletDistribution;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.encoding.json.gson.GeometryDeserializer;
import org.uncertweb.ps.encoding.json.gson.GeometrySerializer;
import org.uncertweb.ps.encoding.json.gson.ProcessExceptionSerializer;
import org.uncertweb.ps.encoding.json.gson.RequestDeserializer;
import org.uncertweb.ps.encoding.json.gson.ResponseSerializer;
import org.uncertweb.ps.encoding.json.gson.ServiceExceptionSerializer;
import org.uncertweb.ps.encoding.json.gson.URLDeserializer;
import org.uncertweb.ps.encoding.json.gson.UncertaintyDeserializer;
import org.uncertweb.ps.encoding.json.gson.UncertaintySerializer;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;
import org.uncertweb.ps.process.ProcessRepository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class JSONRequestHandler {

	private final Logger logger = Logger.getLogger(JSONRequestHandler.class);

	public void handleRequest(Reader reader, Writer writer, String basePath, String baseURL) {
		GsonBuilder gsonBuilder = new GsonBuilder();
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

		Gson gson = gsonBuilder.create();

		/*
		StringWriter w = new StringWriter();
		int read;
		char[] buf = new char[1024];
		try {
			while ((read = reader.read(buf, 0, buf.length)) != -1) {
				w.write(buf, 0, read);
			}
		}
		catch (IOException e) {

		}
		System.out.println(w);
		*/

		try {
			// parse request
			logger.debug("Building object from request...");
			long start = System.currentTimeMillis();
			Request request = gson.fromJson(reader, Request.class);
			logger.debug("Built object in " + (System.currentTimeMillis() - start) / 1000.0 + "s.");

			// execute request
			AbstractProcess process = ProcessRepository.getInstance().getProcess(request.getProcessIdentifier());
			ProcessOutputs outputs = process.run(request.getInputs());

			// generate response
			Response response = new Response(process.getIdentifier(), outputs);

			// generate response
			gson.toJson(response, writer);
		}
		catch (JsonParseException e) {
			// invalid requests end up here!
			logger.error("Couldn't parse request.", e);
			gson.toJson(new ServiceException("Couldn't parse request."), writer);
		}
		catch (ProcessException e) {
			logger.error("Failed to execute process.", e);
			gson.toJson(e, writer);
		}
		catch (RuntimeException e) {
			logger.error("Failed to handle request.", e);
			gson.toJson(new ProcessException("Couldn't execute process.", e), writer);
		}

		try {
			writer.close();
		}
		catch (IOException e) {
			// all hope is lost
		}
	}

}
