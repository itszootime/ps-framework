package org.uncertweb.ps.handler.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.log4j.Logger;
import org.uncertweb.ps.ServiceException;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.handler.json.gson.GsonWrapper;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.ps.storage.StorageException;
import org.uncertweb.util.Stopwatch;

import com.google.gson.Gson;

public class JSONHandler {

	private final Logger logger = Logger.getLogger(JSONHandler.class);

	public void handleRequest(InputStream inputStream, OutputStream outputStream) {		
		try {
			// parse request
			logger.debug("Parsing request document...");
			Stopwatch stopwatch = new Stopwatch();	
			Request request = JSONRequestParser.parse(inputStream);
			logger.debug("Parsed document in " + stopwatch.getElapsedTime() + ".");

			// execute request
			AbstractProcess process = ProcessRepository.getInstance().getProcess(request.getProcessIdentifier());
			ProcessOutputs outputs = process.run(request.getInputs());

			// generate response
			Response response = new Response(process.getIdentifier(), outputs);

			// generate response
			JSONResponseGenerator.generate(response, request.getRequestedOutputs(), outputStream);
		}
		catch (Exception e) {
			handleException(e, outputStream);
		}

		// all done, close stream
		try {
			outputStream.close();
		}
		catch (IOException e) {
			// can't do anything!
		}
	}

	private void handleException(Exception e, OutputStream outputStream) {
		Gson gson = GsonWrapper.getGson();
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		if (e instanceof ProcessException) {
			logger.error("Failed to execute process.", e);
			gson.toJson(e, writer);
		}
		else if (e instanceof EncodeException) {
			logger.error("Couldn't generate data reference.", e);
			gson.toJson(new ServiceException("Couldn't generate data reference."), writer);
		}
		else if (e instanceof IOException) {
			logger.error("Couldn't generate data reference.", e);
			gson.toJson(new ServiceException("Couldn't generate data reference."), writer);
		}
		else if (e instanceof StorageException) {
			logger.error("Couldn't store reference data.", e);
			gson.toJson(new ServiceException("Couldn't store reference data."), writer);
		}
		else if (e instanceof RuntimeException) {
			logger.error("Failed to handle request.", e);
			gson.toJson(new ProcessException("Couldn't execute process.", e), writer);
		}
	}

}
