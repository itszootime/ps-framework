package org.uncertweb.ps.encoding.json.gson;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.uncertweb.api.om.io.JSONObservationParser;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.DataReference;
import org.uncertweb.ps.data.MultipleInput;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.data.SingleInput;
import org.uncertweb.ps.encoding.ParseException;
import org.uncertweb.ps.handler.data.DataReferenceParser;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessRepository;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class RequestDeserializer implements JsonDeserializer<Request> {

	public Request deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		// get our object
		Entry<String, JsonElement> root = json.getAsJsonObject().entrySet().iterator().next();
		JsonObject object = root.getValue().getAsJsonObject();

		// get process identifier
		String processIdentifier = root.getKey().substring(0, root.getKey().length() - "Request".length());

		// create request
		Request request = new Request(processIdentifier);

		// for each input in process get class
		AbstractProcess process = ProcessRepository.getInstance().getProcess(processIdentifier); // FIXME: could be null
		for (String inputIdentifier : process.getInputIdentifiers()) {
			// get description
			DataDescription inputDescription = process.getInputDataDescription(inputIdentifier);

			// get input from json
			if (object.has(inputIdentifier)) {
				JsonElement inputElement = object.get(inputIdentifier);

				// data could be array or single object
				ArrayList<Object> objects = new ArrayList<Object>();

				// get encoding
				try {
					if (inputElement.isJsonArray()) {
						try {
							JsonArray dataArray = inputElement.getAsJsonArray();
							for (JsonElement dataElement : dataArray) {
								objects.add(parseDataElement(dataElement, inputDescription, context));
							}
						}
						catch (Exception e) {
							objects.add(parseDataElement(inputElement, inputDescription, context));
						}
					}
					else {
						objects.add(parseDataElement(inputElement, inputDescription, context));
					}
				}
				catch (Exception e) {
					throw new JsonParseException(e);
				}

				// create appropriate input
				if (objects.size() > 0) {
					if (inputDescription.getMaxOccurs() > 1) {
						request.getInputs().add(new MultipleInput(inputIdentifier, objects));
					}
					else {
						request.getInputs().add(new SingleInput(inputIdentifier, objects.get(0)));
					}
				}
			}
		}
		
		// check for requested outputs
		if (object.has("RequestedOutputs")) {
			List<RequestedOutput> reqOutputs = request.getRequestedOutputs();
			JsonObject outputsObj = object.get("RequestedOutputs").getAsJsonObject();
			for (Entry<String, JsonElement> entry : outputsObj.entrySet()) {
				String outputIdentifier = entry.getKey();
				JsonObject outputObj = entry.getValue().getAsJsonObject();
				boolean asReference = false;
				if (outputObj.has("asReference")) {
					asReference = outputObj.get("asReference").getAsBoolean();
				}
				reqOutputs.add(new RequestedOutput(outputIdentifier, asReference));
			}
		}

		return request;
	}

	/**
	 * This needs to be fixed!
	 * 
	 * 
	 * The class should find an appropriate parser for the data type,
	 * then pass the string to that parser. Like the XML parser system.
	 * 
	 * This would lose the benefit of having a shared deserialization context.
	 * e.g. if I have UncertML in O&M, my O&M parser cannot simply refer the UncertML to
	 * the context. It'd have to explicitly parse it.
	 * 
	 * Might not be that much of an issue. Classes could use Gson underlying if they wanted too,
	 * then register type adapters themselves. Keeps things decoupled from Gson for parsing.
	 * 
	 * Maybe there a way to pass the deserialization context too. Is this useful?
	 *  
	 * If we can't find a parser, maybe give Gson a go at doing it automatically? But catch
	 * the exceptions properly.
	 * 
	 * 
	 * @param dataElement
	 * @param dataDescription
	 * @param context
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	private Object parseDataElement(JsonElement dataElement, DataDescription dataDescription, JsonDeserializationContext context) throws ParseException, IOException {
		if (dataElement.isJsonObject() && dataElement.getAsJsonObject().has("DataReference")) {
			JsonObject dataReference = dataElement.getAsJsonObject().get("DataReference").getAsJsonObject();
			// TODO: exceptions required if href and mimeType not set, compression is optional
			boolean compression = false;
			if (dataReference.has("compressed")) {
				compression = dataReference.get("compressed").getAsBoolean();
			}
			
			// create url from string
			URL dataURL = new URL(dataReference.get("href").getAsString());
			
			// parse data reference
			DataReference ref = new DataReference(dataURL, dataReference.get("mimeType").getAsString(), compression);
			DataReferenceParser parser = new DataReferenceParser();
			return parser.parse(ref, dataDescription.getClass());
		}
		else if (isOM(dataDescription.getType())) { // FIXME: bit of a workaround
			JSONObservationParser parser = new JSONObservationParser();
			try {
				return parser.parse(dataElement.toString());
			}
			catch (Exception e) {
				throw new ParseException("Couldn't parse O&M JSON.", e);
			}
		}
		else {
			return context.deserialize(dataElement, dataDescription.getType());
		}
	}

	private boolean isOM(Class<?> classOf) {
		for (Class<?> interf : classOf.getInterfaces()) {
			if (interf.equals(IObservationCollection.class)) {
				return true;
			}
		}
		Class<?> superClass = classOf.getSuperclass();
		if (superClass != null) {
			return superClass.equals(AbstractObservation.class);
		}
		return false;
	}

}
