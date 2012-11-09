package org.uncertweb.ps.handler.soap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.uncertweb.ps.Config;
import org.uncertweb.ps.DataReferenceHelper;
import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.Output;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.xml.Namespaces;

public class XMLResponseGenerator {
	
	private static final Logger logger = Logger.getLogger(XMLResponseGenerator.class);

	public static Element generate(AbstractProcess process, List<RequestedOutput> reqOutputs, ProcessOutputs outputs) throws ResponseException {
		// get identifier
		String processIdentifier = process.getIdentifier();

		// generate response
		Element responseElement = new Element(processIdentifier + "Response", Namespaces.PS);

		// add outputs
		for (String outputIdentifier : process.getOutputIdentifiers()) {
			// get output from process
			Output output = outputs.get(outputIdentifier);	

			// check if output was requested and as reference
			boolean requested = false;
			boolean reference = false;
			if (reqOutputs.size() == 0) {
				requested = true;
			}
			for (RequestedOutput reqOutput : reqOutputs) {
				if (reqOutput.getName().equals(output.getIdentifier())) {
					requested = true;
					reference = reqOutput.isReference();
				}
			}

			// add output
			if (requested) {
				// get objects related to output
				List<Object> objects;
				if (output.isSingleOutput()) {
					objects = new ArrayList<Object>();
					objects.add(output.getAsSingleOutput().getObject());
				}
				else {
					objects = output.getAsMultipleOutput().getObjects();
				}

				// encode each object
				DataDescription dataDescription = process.getOutputDataDescription(outputIdentifier);
				for (Object o : objects) {			
					try {
						addData(responseElement, output.getIdentifier(), o, dataDescription, reference);
					}
					catch (EncodeException e) {
						String message = "Couldn't encode data for " + output.getIdentifier() + ".";
						logger.error(message);
						throw new ResponseException(message, e);
					}
				}
			}
		}

		return responseElement;
	}

	private static void addData(Element baseElement, String identifier, Object object, DataDescription dataDescription, boolean reference) throws EncodeException {
		// base element
		Element outputElement = new Element(identifier, Namespaces.PS);
		baseElement.addContent(outputElement);

		// get encoding
		Class<?> type = dataDescription.getType();
		EncodingRepository encodingRepo = EncodingRepository.getInstance();
		Encoding xmlEncoding = encodingRepo.getXMLEncoding(type);
		Encoding binaryEncoding = encodingRepo.getBinaryEncoding(type);

		if (xmlEncoding == null && binaryEncoding == null) {
			
		}
		else {
			if (reference || xmlEncoding == null) {
				addReferenceData(outputElement, object, dataDescription);
			}
			else {
				addInlineData(outputElement, object, type);
			}
		}
	}

	private static void addReferenceData(Element baseElement, Object object, DataDescription dataDescription) throws EncodeException {
		try {
			// generate data
			Config config = Config.getInstance();
			String href = DataReferenceHelper.generateXMLDataReference(object, dataDescription, config.getServerProperty("basePath"), config.getServerProperty("baseURL")).toString();

			// add reference url to response
			// FIXME: mimeType, compression
			// TODO: simple by reference?
			Element referenceElement = new Element("DataReference", Namespaces.PS);
			referenceElement.setAttribute("href", href);
			referenceElement.setAttribute("mimeType", "unknown");
			baseElement.addContent(referenceElement);
		}
		catch (IOException e) {
			//throw new ServiceException("Couldn't save output. File not found: ", e.getMessage());
		}
	}

	private static void addInlineData(Element baseElement, Object object, Class<?> dataClass) throws EncodeException {
		// FIXME: more to add here
		List<Class<?>> primitiveClasses = Arrays.asList(new Class<?>[] {
			Double.class, Double[].class, Integer.class, Integer[].class, String.class
		});
		if (primitiveClasses.contains(dataClass)) {
			addInlinePrimitiveData(baseElement, object, dataClass);
		}
		else {
			// complex, need to find appropriate encoding
			AbstractXMLEncoding encoding = EncodingRepository.getInstance().getXMLEncoding(dataClass);
			baseElement.addContent(encoding.encode(object).detach());
		}
	}

	private static void addInlinePrimitiveData(Element baseElement, Object object, Class<?> dataClass) {
		if (dataClass.isArray()) {
			StringBuilder string = new StringBuilder();
			for (Object o : (Iterable<?>)object) {
				string.append(o.toString() + " ");
			}
			string.deleteCharAt(string.length() - 1);
			baseElement.setText(string.toString());
		}
		else {
			baseElement.setText(object.toString());									
		}
	}	

}
