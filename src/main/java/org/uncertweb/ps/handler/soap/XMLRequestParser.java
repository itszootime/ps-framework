package org.uncertweb.ps.handler.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.DataReference;
import org.uncertweb.ps.data.Input;
import org.uncertweb.ps.data.MultipleInput;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.data.SingleInput;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.ParseException;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.handler.data.DataReferenceParser;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.xml.Namespaces;

public class XMLRequestParser {

	private static final Logger logger = Logger.getLogger(XMLRequestParser.class);

	public static Request parse(Element requestElement) throws RequestParseException {
		// check if process or description request
		if (requestElement == null || !requestElement.getNamespace().equals(Namespaces.PS) || !requestElement.getName().endsWith("Request")) {
			throw new RequestParseException("No request element found.");
		}

		// extract process id
		String processIdentifier = requestElement.getName().substring(0, requestElement.getName().length() - "Request".length());

		// create request object
		Request request = new Request(processIdentifier);

		// get process
		AbstractProcess process = ProcessRepository.getInstance().getProcess(processIdentifier);

		// if we can't find process
		if (process == null) {
			throw new RequestParseException("Process with identifier " + processIdentifier + " could not be found.");
		}

		// parse inputs
		ProcessInputs inputs = request.getInputs();
		List<Input> parsedInputs = parseInputs(requestElement, process);
		for (Input input : parsedInputs) {
			inputs.add(input);
		}

		// parse requested outputs
		request.setRequestedOutputs(parseRequestedOutputs(requestElement));

		return request;
	}

	private static List<Input> parseInputs(Element baseElement, AbstractProcess process) throws RequestParseException {
		// list of parsed inputs
		List<Input> inputs = new ArrayList<Input>();

		// lookup inputs for process name
		for (String inputIdentifier : process.getInputIdentifiers()) {
			// get data class for type from process
			DataDescription dataDescription = process.getInputDataDescription(inputIdentifier);
			if (dataDescription == null) {
				// FIXME: not a client problem
				throw new RequestParseException("Couldn't find description for input " + inputIdentifier + ".");
			}
			Class<?> type = dataDescription.getType();

			// get input element
			List<?> inputElements = baseElement.getChildren(inputIdentifier, Namespaces.PS);

			// parse if present
			if (inputElements.size() > 0) {
				// hold data
				List<Object> dataList = new ArrayList<Object>();

				// get each element
				for (Object o : inputElements) {
					// cast to element
					Element inputElement = (Element) o;

					// parse
					try {
						dataList.add(parseData(inputElement, type));
					}
					catch (ParseException e) {
						throw new RequestParseException("Couldn't parse data for " + inputIdentifier + ".", e);
					}

					// add to request
					if (dataDescription.getMaxOccurs() > 1) {							
						inputs.add(new MultipleInput(inputIdentifier, dataList));
					}
					else {
						inputs.add(new SingleInput(inputIdentifier, dataList.get(0)));
					}
				}
			}
		}

		// all done
		return inputs;
	}

	private static <T> T parseData(Element element, Class<T> type) throws ParseException, RequestParseException {
		// get the content
		Content content = element.getContent(0);
		
		// special case for reference
		if (content instanceof Element && ((Element)content).getName().equals("DataReference")) {			
			return parseDataReference((Element)content, type);
		}
		else {
			// get xml encoding
			EncodingRepository encodingRepo = EncodingRepository.getInstance();
			AbstractXMLEncoding encoding = encodingRepo.getXMLEncoding(type);
			
			// couldn't find suitable encoding
			if (encoding == null) {
				// FIXME: not a client problem
				throw new RequestParseException("No encoding found for type " + type.getName() + ".");
			}
			
			return encoding.parse(content, type);
		}
	}

	private static <T> T parseDataReference(Element element, Class<T> type) throws ParseException, RequestParseException {
		// get attributes
		String href = element.getAttributeValue("href");
		String mimeType = element.getAttributeValue("mimeType");
		String compressedString = element.getAttributeValue("compressed");
		boolean compressed = false;
		if (compressedString != null) {
			compressed = (compressedString.equalsIgnoreCase("true") || compressedString.equals("1"));
		}
		
		// parse
		DataReferenceParser refParser = new DataReferenceParser();
		try {
			DataReference ref = new DataReference(new URL(href), mimeType, compressed);
			return refParser.parse(ref, type);
		}
		catch (MalformedURLException e) {
			throw new RequestParseException("Malformed data reference URL.", e);
		}
	}

	private static List<RequestedOutput> parseRequestedOutputs(Element requestElement) {
		// create list
		List<RequestedOutput> reqOutputs = new ArrayList<RequestedOutput>();

		// get element
		Element reqOutputsElement = requestElement.getChild("RequestedOutputs", Namespaces.PS);
		if (reqOutputsElement != null) {
			for (Object o : reqOutputsElement.getChildren()) {
				Element reqOutputElement = (Element)o;
				String name = reqOutputElement.getName();
				boolean reference = false;
				Attribute refAttr = reqOutputElement.getAttribute("reference");
				if (refAttr != null && (refAttr.getValue().equals("true") || refAttr.equals("1"))) {
					reference = true;
				}
				reqOutputs.add(new RequestedOutput(name, reference));
			}
		}
		else {
			reqOutputs = null;
		}

		// all done
		return reqOutputs;
	}
	
}
