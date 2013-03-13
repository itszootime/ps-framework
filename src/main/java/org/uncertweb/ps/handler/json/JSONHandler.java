package org.uncertweb.ps.handler.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.uncertweb.ps.Config;
import org.uncertweb.ps.ServiceException;
import org.uncertweb.ps.data.DataReference;
import org.uncertweb.ps.data.Output;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.handler.data.DataReferenceGenerator;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.ps.storage.StorageException;
import org.uncertweb.util.Stopwatch;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
			JsonObject responseObject = gson.toJsonTree(response).getAsJsonObject();
			JsonObject innerObject = responseObject.get(response.getProcessIdentifier() + "Response").getAsJsonObject();
			
			// check requested outputs
			List<RequestedOutput> reqOutputs = request.getRequestedOutputs();
			if (reqOutputs.size() > 0) {
				// horrible inefficient
				for (Output output : response.getOutputs()) {
					String outputIdentifier = output.getIdentifier();
					boolean include = false;
					for (RequestedOutput reqOutput : reqOutputs) {
						if (reqOutput.getName().equals(output.getIdentifier())) {
							// set include flag
							include = true;
							
							// check for ref
							if (reqOutput.isReference()) {
								// get config
								Config config = Config.getInstance();
								String basePath = config.getServerProperty("basePath");
								String baseURL = config.getServerProperty("baseURL");
								
								JsonElement dataElement;
								if (output.isMultipleOutput()) {
									JsonArray array = new JsonArray();
									for (JsonElement element : innerObject.get(outputIdentifier).getAsJsonArray()) {
										array.add(generateReferenceObject(element, basePath, baseURL));
									}
									dataElement = array;
								}
								else {
									dataElement = generateReferenceObject(innerObject.get(outputIdentifier), basePath, baseURL);
								}
								innerObject.add(outputIdentifier, dataElement);
							}
							
							// all done
							break;
						}
					}
					
					// if not included, remove
					if (!include) {
						innerObject.remove(outputIdentifier);
					}
				}
			}
			
			// write
			gson.toJson(responseObject, writer);
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
		catch (EncodeException e) {
			logger.error("Couldn't generate data reference.", e);
			gson.toJson(new ServiceException("Couldn't generate data reference."), writer);
		}
		catch (IOException e) {
			logger.error("Couldn't generate data reference.", e);
			gson.toJson(new ServiceException("Couldn't generate data reference."), writer);
		}
		catch (StorageException e) {
			logger.error("Couldn't store reference data.", e);
			gson.toJson(new ServiceException("Couldn't store reference data."), writer);
		}

		try {
			writer.close();
		}
		catch (IOException e) {
			// all hope is lost
		}
	}
	
	private JsonObject generateReferenceObject(JsonElement element, String basePath, String baseURL) throws IOException, EncodeException, StorageException {
		// generate reference
		DataReferenceGenerator generator = new DataReferenceGenerator();
		DataReference ref = generator.generate(element);
		
		// return as object
		JsonObject refObj = new JsonObject();
		JsonObject innerRefObj = new JsonObject();
		refObj.add("DataReference", innerRefObj);
		innerRefObj.addProperty("href", ref.getURL().toString());
		innerRefObj.addProperty("mimeType", ref.getMimeType());
		return refObj;
	}

}
