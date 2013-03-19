package org.uncertweb.ps.handler.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.uncertweb.ps.data.DataReference;
import org.uncertweb.ps.data.Output;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.handler.ResponseGenerateException;
import org.uncertweb.ps.handler.data.DataReferenceGenerator;
import org.uncertweb.ps.handler.json.gson.GsonWrapper;
import org.uncertweb.ps.storage.StorageException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JSONResponseGenerator {
	
	public static void generate(Response response, OutputStream outputStream) throws ResponseGenerateException {
		generate(response, null, outputStream);
	}

	public static void generate(Response response, List<RequestedOutput> reqOutputs, OutputStream outputStream) throws ResponseGenerateException {
		// build using gson
		Gson gson = GsonWrapper.getGson();
		JsonObject responseObject = gson.toJsonTree(response).getAsJsonObject();
		JsonObject innerObject = responseObject.get(response.getProcessIdentifier() + "Response").getAsJsonObject();

		// check requested outputs
		if (reqOutputs != null) {
			// remove outputs not in requested, modify to references where necessary
			// FIXME: horribly inefficient
			for (Output output : response.getOutputs()) {
				String outputIdentifier = output.getIdentifier();
				boolean include = false;
				boolean reference = false;
				
				// set include/reference flags
				for (RequestedOutput reqOutput : reqOutputs) {
					if (reqOutput.getName().equals(output.getIdentifier())) {
						include = true;
						reference = reqOutput.isReference();
						break;
					}
				}
				
				// check include/reference flags
				if (!include) {
					// remove if not included
					innerObject.remove(outputIdentifier);
				}
				else {
					// convert to data reference
					if (reference) {						
						try {
							JsonElement element = createReferenceElement(output);
							innerObject.add(outputIdentifier, element);
						}
						catch (EncodeException | StorageException e) {
							throw new ResponseGenerateException("Couldn't generate output data reference", e);
						}					
					}
				}
			}
		}

		// write
		try {
			Writer writer = new OutputStreamWriter(outputStream);
			gson.toJson(responseObject, writer);
			writer.flush();
		}
		catch (IOException e) {
			throw new ResponseGenerateException("Could not write response to stream", e);
		}
	}

	private static JsonElement createReferenceElement(Output output) throws EncodeException, StorageException {
		// objects to encode
		List<Object> objects;
		if (output.isSingleOutput()) {
			objects = new ArrayList<Object>();
			objects.add(output.getAsSingleOutput().getObject());
		}
		else {
			objects = output.getAsMultipleOutput().getObjects();
		}
		
		// generate references
		DataReferenceGenerator gen = new DataReferenceGenerator();
		List<JsonObject> refObjs = new ArrayList<JsonObject>();
		for (Object obj : objects) {
			DataReference ref = gen.generate(obj);
			JsonObject refObj = new JsonObject();
			refObj.addProperty("href", ref.getURL().toString());
			refObj.addProperty("mimeType", ref.getMimeType());
			JsonObject outerRefObj = new JsonObject();
			outerRefObj.add("DataReference", refObj);
			refObjs.add(outerRefObj);
		}
		
		// add to response object
		JsonElement element;
		if (output.isSingleOutput()) {
			element = refObjs.get(0);
		}
		else {
			element = new JsonArray();
			for (JsonObject refObj : refObjs) {
				((JsonArray)element).add(refObj);
			}
		}
		return element;
	}
}
