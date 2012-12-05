package org.uncertweb.ps.handler.json;

import java.util.List;

import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.Metadata;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessRepository;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JSONDescriptionHelper {

	public static JsonElement generateJsonDescription() {
		// base object
		JsonObject object = new JsonObject();
		
		// get processes
		List<AbstractProcess> processes = ProcessRepository.getInstance().getProcesses();
		JsonArray processesArray = new JsonArray();
		object.add("processes", processesArray);
		
		// list details for each
		for (AbstractProcess process : processes) {
			JsonObject processObject = new JsonObject();
			processesArray.add(processObject);
			processObject.addProperty("identifier", process.getIdentifier());
			
			List<Metadata> pMdList = process.getMetadata();
			if (pMdList != null) {
				for (Metadata md : pMdList) {
					processObject.addProperty(md.getKey(), md.getValue());
				}
			}
			
			JsonArray inputsArray = new JsonArray();
			processObject.add("inputs", inputsArray);
			for (String inputIdentifier : process.getInputIdentifiers()) {
				JsonObject inputObject = new JsonObject();
				inputsArray.add(inputObject);
				inputObject.addProperty("identifier", inputIdentifier);
				
				DataDescription dataDesc = process.getInputDataDescription(inputIdentifier);
				inputObject.addProperty("type", dataDesc.getType().getSimpleName().toLowerCase());
				
				List<Metadata> mdList = process.getInputMetadata(inputIdentifier);
				if (mdList != null) {
					for (Metadata md : mdList) {
						inputObject.addProperty(md.getKey(), md.getValue());
					}
				}
			}
			
			JsonArray outputsArray = new JsonArray();
			processObject.add("outputs", outputsArray);
			for (String outputIdentifier : process.getOutputIdentifiers()) {
				JsonObject outputObject = new JsonObject();
				outputsArray.add(outputObject);
				outputObject.addProperty("identifier", outputIdentifier);
				
				DataDescription dataDesc = process.getOutputDataDescription(outputIdentifier);
				outputObject.addProperty("type", dataDesc.getType().getSimpleName().toLowerCase());
				
				List<Metadata> mdList = process.getOutputMetadata(outputIdentifier);
				if (mdList != null) {
					for (Metadata md : mdList) {
						outputObject.addProperty(md.getKey(), md.getValue());
					}
				}
			}
		}
		
		// all done
		return object;
	}
	
}
