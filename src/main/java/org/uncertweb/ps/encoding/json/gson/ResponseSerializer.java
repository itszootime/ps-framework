package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import org.uncertml.IUncertainty;
import org.uncertml.io.JSONEncoder;
import org.uncertweb.api.om.io.JSONObservationEncoder;
import org.uncertweb.api.om.observation.AbstractObservation;
import org.uncertweb.api.om.observation.collections.IObservationCollection;
import org.uncertweb.ps.Response;
import org.uncertweb.ps.data.Output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResponseSerializer implements JsonSerializer<Response> {

	public JsonElement serialize(Response src, Type typeOfSrc, JsonSerializationContext context) {
		// create base object
		JsonObject object = new JsonObject();

		// create response object
		JsonObject response = new JsonObject();

		// add process identifier
		object.add(src.getProcessIdentifier() + "Response", response);

		// add each output
		for (Output output : src.getOutputs()) {
			// create appropriate element
			JsonElement outputElement;
			if (output.isMultipleOutput()) {
				JsonArray outputArray = new JsonArray();
				for (Object o : output.getAsMultipleOutput().getObjects()) {
					outputArray.add(encodeDataElement(o, context));
				}
				outputElement = outputArray;
			}
			else {
				outputElement = encodeDataElement(output.getAsSingleOutput().getObject(), context);
			}
			response.add(output.getIdentifier(), outputElement);
		}

		return object;
	}

	private JsonElement encodeDataElement(Object src, JsonSerializationContext context) {
		if (isOM(src.getClass())) {
			try {
				JSONObservationEncoder encoder = new JSONObservationEncoder();
				Gson gson = new GsonBuilder().create();
				if (src instanceof IObservationCollection) {
					return gson.fromJson(encoder.encodeObservationCollection((IObservationCollection) src), JsonElement.class);
				}
				else {
					return gson.fromJson(encoder.encodeObservation((AbstractObservation) src), JsonElement.class);
				}
			}
			catch (Exception e) {
				return null; // shouldn't ever happen?
			}
		}
		else {
			return context.serialize(src);
		}
	}

	// FIXME: hacky
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
