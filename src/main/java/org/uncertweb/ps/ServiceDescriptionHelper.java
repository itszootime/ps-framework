package org.uncertweb.ps;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.joda.time.LocalDate;
import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.Metadata;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.binary.AbstractBinaryEncoding;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding.Include;
import org.uncertweb.ps.encoding.xml.Namespaces;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessRepository;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ServiceDescriptionHelper {

	// FIXME: would be good to cache
	// namespaces to use, keeps things tidy using set prefixes
	private static final Namespace PS_NS = Namespace.getNamespace("ps", Namespaces.PS);
	private static final Namespace XSD_NS = Namespace.getNamespace("xsd", Namespaces.XSD);
	private static final EncodingRepository ENCODING_FACTORY = EncodingRepository.getInstance();
	
	public static Document generateSchema() {
		// doesn't exist, so create it
		Element schema = new Element("schema", XSD_NS);
		schema.setAttribute("targetNamespace", PS_NS.getURI());
		schema.setAttribute("elementFormDefault", "qualified");

		// keep things tidy
		schema.addNamespaceDeclaration(PS_NS);
		schema.addNamespaceDeclaration(XSD_NS);

		// get processes
		List<AbstractProcess> processes = ProcessRepository.getInstance().getProcesses();	
		
		// make a map for namespace > prefix mapping
		Map<String, String> namespaceMapping = new HashMap<String, String>();
		namespaceMapping.put(PS_NS.getURI(), "ps");
		namespaceMapping.put(XSD_NS.getURI(), "xsd");
		int nextNS = 0;
		for (AbstractProcess process : processes) {
			// TODO: handle encoding not found
			for (String inputIdentifier : process.getInputIdentifiers()) {
				// get things
				Class<?> dataClass = process.getInputDataDescription(inputIdentifier).getClassOf();
				Encoding encoding = ENCODING_FACTORY.getEncoding(dataClass);
				if (encoding instanceof AbstractXMLEncoding) {
					String namespace = ((AbstractXMLEncoding) encoding).getNamespace();
					if (!namespaceMapping.containsKey(namespace)) {
						namespaceMapping.put(namespace, "ns" + nextNS);
						schema.addNamespaceDeclaration(Namespace.getNamespace("ns" + nextNS, namespace));
						nextNS++;					
					}
				}
			}
			for (String outputIdentifier : process.getOutputIdentifiers()) {
				// get things
				Class<?> dataClass = process.getOutputDataDescription(outputIdentifier).getClassOf();
				Encoding encoding = ENCODING_FACTORY.getEncoding(dataClass);
				if (encoding instanceof AbstractXMLEncoding) {
					String namespace = ((AbstractXMLEncoding) encoding).getNamespace();
					if (!namespaceMapping.containsKey(namespace)) {
						namespaceMapping.put(namespace, "ns" + nextNS);
						schema.addNamespaceDeclaration(Namespace.getNamespace("ns" + nextNS, namespace));
						nextNS++;
					}
				}
			}
		}
		
		// add data reference element
		schema.addContent(new Element("element", XSD_NS)
			.setAttribute("name", "DataReference")
			.addContent(new Element("complexType", XSD_NS)
				.addContent(new Element("sequence", XSD_NS))
			.addContent(new Element("attribute", XSD_NS)
				.setAttribute("name", "href")
				.setAttribute("type", XSD_NS.getPrefix() + ":anyURI")
				.setAttribute("use", "required"))
			.addContent(new Element("attribute", XSD_NS)
				.setAttribute("name", "mimeType")
				.setAttribute("type", XSD_NS.getPrefix() + ":string")
				.setAttribute("use", "required"))
			.addContent(new Element("attribute", XSD_NS)
				.setAttribute("name", "compressed")
				.setAttribute("type", XSD_NS.getPrefix() + ":boolean")
				.setAttribute("default", "false"))));

		// add process specific info
		for (AbstractProcess process : processes) {
			// create request element
			Element requestSequence = new Element("sequence", XSD_NS);
			schema.addContent(new Element("element", XSD_NS)
				.setAttribute("name", process.getIdentifier() + "Request")
				.addContent(new Element("complexType", XSD_NS)
					.addContent(requestSequence)));
					/*.addContent(new Element("attribute", XSD_NS)
						.setAttribute("name", "asynchronous")
						.setAttribute("type", XSD_NS.getPrefix() + ":boolean")
						.setAttribute("default", "false"))));*/

			// add inputs
			for (String inputIdentifier : process.getInputIdentifiers()) {
				requestSequence.addContent(createInputElement(schema, namespaceMapping, inputIdentifier, process.getInputDataDescription(inputIdentifier), process.getInputMetadata(inputIdentifier)));
			}

			// add req outputs
			// create requested outputs elements
			Element reqOutputsSequence = new Element("sequence", XSD_NS);
			requestSequence.addContent(new Element("element", XSD_NS)
				.setAttribute("minOccurs", "0")
				.setAttribute("name", "RequestedOutputs")
					.addContent(new Element("complexType", XSD_NS)
						.addContent(reqOutputsSequence)));

			// create response
			Element responseSequence = new Element("sequence", XSD_NS);
			schema.addContent(new Element("element", XSD_NS)
				.setAttribute("name", process.getIdentifier() + "Response")
				.addContent(new Element("complexType", XSD_NS)
					.addContent(responseSequence)));

			// for outputs
			for (String outputIdentifier : process.getOutputIdentifiers()) {
				responseSequence.addContent(createOutputElement(schema, namespaceMapping, outputIdentifier, process.getOutputDataDescription(outputIdentifier), process.getOutputMetadata(outputIdentifier)));
				reqOutputsSequence.addContent(new Element("element", XSD_NS)
					.setAttribute("name", outputIdentifier)
					.setAttribute("minOccurs", "0")
					.addContent(new Element("complexType", XSD_NS)
						.addContent(new Element("sequence", XSD_NS))
						.addContent(new Element("attribute", XSD_NS)
							.setAttribute("name", "reference")
							.setAttribute("type", XSD_NS.getPrefix() + ":boolean")
							.setAttribute("default", "false"))));
			}
		}
		
		// return document
		return new Document(schema);
	}
	
	private static Element createInputElement(Element schema, Map<String, String> namespaceMapping, String identifier, DataDescription dataDescription, List<Metadata> metadata) {
		return createInputOutputElement(schema, namespaceMapping, identifier, dataDescription, metadata);
	}
	
	private static Element createOutputElement(Element schema, Map<String, String> namespaceMapping, String identifier, DataDescription dataDescription, List<Metadata> metadata) {
		Element element = createInputOutputElement(schema, namespaceMapping, identifier, dataDescription, metadata);
		element.setAttribute("minOccurs", "0");
		return element;
	}

	private static Element createInputOutputElement(Element schema, Map<String, String> namespaceMapping, String identifier, DataDescription dataDescription, List<Metadata> metadata) {
		// get things
		Class<?> dataClass = dataDescription.getClassOf();

		// add create element
		Element element = new Element("element", XSD_NS)
			.setAttribute("name", identifier)
			.setAttribute("minOccurs", (dataDescription.getMinOccurs() >= 1 ? "1" : "0"))
			.setAttribute("maxOccurs", (dataDescription.getMaxOccurs() == Integer.MAX_VALUE ? "unbounded" : String.valueOf(dataDescription.getMaxOccurs())));
		
		// add metadata
		if (metadata != null && metadata.size() > 0) {
			String metadataText = "";
			for (Metadata m : metadata) {
				metadataText += "\n@" + m.getKey() + " " + m.getValue();
			}
			// looks like jdom strips whitespace at start and end of string
			element.addContent(new Element("annotation", XSD_NS).addContent(new Element("documentation", XSD_NS).setText(metadataText)));
		}
		
		// add type
		List<Class<?>> supportedSimpleTypes = Arrays.asList(new Class<?>[] {
				Float.class, Double.class, Boolean.class, URL.class, String.class, Integer.class,
				Float[].class, Double[].class, Boolean.class, URL[].class, String[].class, Integer[].class, LocalDate.class, LocalDate[].class });
		if (supportedSimpleTypes.contains(dataClass)) {
			// float, double, boolean, anyURI, string, integer for now...
			String xsdType = XSD_NS.getPrefix() + ":"; 
			if (dataClass.equals(URL.class) || dataClass.equals(URL[].class)) {
				xsdType += "anyURI";
			}
			else if (dataClass.equals(LocalDate.class) || dataClass.equals(LocalDate[].class)) {
				xsdType += "date";
			}
			else {
				String dataClassName = dataClass.getName();
				xsdType += dataClassName.substring(dataClassName.lastIndexOf(".") + 1, dataClassName.length()).toLowerCase();
			}
			if (xsdType.endsWith(";")) {
				xsdType = xsdType.substring(0, xsdType.length() - 1);
			}
			if (dataClass.isArray()) {
				element.addContent(new Element("simpleType", XSD_NS)
					.addContent(new Element("list", XSD_NS)
						.setAttribute("itemType", xsdType)));
			}
			else {
				element.setAttribute("type", xsdType);
			}
			
		}
		else {
			// add choice
			Element choice = new Element("choice", XSD_NS);
			element.addContent(new Element("complexType", XSD_NS)
				.addContent(choice));
			
			// get encoding
			Encoding encoding = ENCODING_FACTORY.getEncoding(dataClass);
			// FIXME: no appropriate encoding will result in encoding == null, should throw an exception for the servlet to catch
		
			if (!(encoding instanceof AbstractBinaryEncoding)) {
				// get encoding
				AbstractXMLEncoding xmlEncoding = (AbstractXMLEncoding) encoding;
				String namespace = xmlEncoding.getNamespace();
	
				// add imports
				addSchemaImport(schema, xmlEncoding);
				
				// add reference
				Include include = xmlEncoding.getIncludeForClass(dataClass);
				choice.addContent(new Element("element", XSD_NS)
					.setAttribute("ref", namespaceMapping.get(namespace) + ":" + include.getName()));
			}
			
			// add reference
			choice.addContent(new Element("element", XSD_NS)
				.setAttribute("ref", PS_NS.getPrefix() + ":DataReference"));
		}
		
		return element;
	}

	public static Document generateWSDL(String serviceURL) {
		// namespaces to use, keeps things tidy using set prefixes
		Namespace wsdlNS = Namespace.getNamespace("wsdl", Namespaces.WSDL);
		Namespace psNS = Namespace.getNamespace("ps", Namespaces.PS);
		Namespace xsdNS = Namespace.getNamespace("xsd", Namespaces.XSD);
		Namespace wsdlsoapNS = Namespace.getNamespace("soap", Namespaces.WSDLSOAP);
		
		// get processes
		List<AbstractProcess> processes = ProcessRepository.getInstance().getProcesses();

		// create document
		Document document = new Document();
		Element definitions = new Element("definitions", wsdlNS);
		definitions.setAttribute("targetNamespace", psNS.getURI());
		document.addContent(definitions);

		// keep things tidy
		definitions.addNamespaceDeclaration(psNS);
		definitions.addNamespaceDeclaration(xsdNS);
		definitions.addNamespaceDeclaration(wsdlsoapNS);

		// create types element
		Element types = new Element("types", wsdlNS);
		definitions.addContent(types);
		Element schema = new Element("schema", xsdNS)
		.setAttribute("targetNamespace", psNS.getURI())
		.addContent(new Element("include", xsdNS)
		.setAttribute("schemaLocation", serviceURL + "?schema"));
		types.addContent(schema);

		// create port type
		Element portType = new Element("portType", wsdlNS).setAttribute("name", "ProcessingService");

		// create binding
		Element binding = new Element("binding", wsdlNS)
		.setAttribute("name", "ProcessingServiceSOAP")
		.setAttribute("type", psNS.getPrefix() + ":ProcessingService")
		.addContent(
			new Element("binding", wsdlsoapNS).setAttribute("style", "document").setAttribute("transport",
			"http://schemas.xmlsoap.org/soap/http"));

		// add process specific info
		for (AbstractProcess process : processes) {
			// create messages
			Element requestMessage = new Element("message", wsdlNS);
			definitions.addContent(requestMessage);
			requestMessage.setAttribute("name", process.getIdentifier() + "Request");
			Element requestPart = new Element("part", wsdlNS);
			requestMessage.addContent(requestPart);
			requestPart.setAttribute("name", process.getIdentifier().substring(0, 1).toLowerCase() + process.getIdentifier().substring(1) + "Request");
			requestPart.setAttribute("element", psNS.getPrefix() + ":" + process.getIdentifier() + "Request");
			Element responseMessage = new Element("message", wsdlNS);
			definitions.addContent(responseMessage);
			responseMessage.setAttribute("name", process.getIdentifier() + "Response");
			Element responsePart = new Element("part", wsdlNS);
			responseMessage.addContent(responsePart);
			responsePart.setAttribute("name", process.getIdentifier().substring(0, 1).toLowerCase() + process.getIdentifier().substring(1) + "Response");
			responsePart.setAttribute("element", psNS.getPrefix() + ":" + process.getIdentifier() + "Response");

			// port type operations
			Element portOperation = new Element("operation", wsdlNS);
			portType.addContent(portOperation);
			portOperation.setAttribute("name", process.getIdentifier());
			Element portInput = new Element("input", wsdlNS);
			portOperation.addContent(portInput);
			portInput.setAttribute("message", psNS.getPrefix() + ":" + process.getIdentifier() + "Request");
			Element portOutput = new Element("output", wsdlNS);
			portOperation.addContent(portOutput);
			portOutput.setAttribute("message", psNS.getPrefix() + ":" + process.getIdentifier() + "Response");

			// add metadata
			if (process.getDetail() != null) {
				portOperation.addContent(new Element("annotation", XSD_NS).addContent(new Element("documentation", XSD_NS).setText(process.getDetail())));
			}
			
			// binding operations
			Element bindingOperation = new Element("operation", wsdlNS);
			binding.addContent(bindingOperation);
			bindingOperation.setAttribute("name", process.getIdentifier());
			Element soapOperation = new Element("operation", wsdlsoapNS);
			bindingOperation.addContent(soapOperation);
			soapOperation.setAttribute("soapAction", definitions.getAttributeValue("targetNamespace") + "/" + process.getIdentifier());
			Element bindingInput = new Element("input", wsdlNS);
			bindingOperation.addContent(bindingInput);
			Element bodyInput = new Element("body", wsdlsoapNS);
			bindingInput.addContent(bodyInput);
			bodyInput.setAttribute("use", "literal");
			Element bindingOutput = new Element("output", wsdlNS);
			bindingOperation.addContent(bindingOutput);
			Element bodyOutput = new Element("body", wsdlsoapNS);
			bindingOutput.addContent(bodyOutput);
			bodyOutput.setAttribute("use", "literal");
		}

		// add other elements
		definitions.addContent(portType);
		definitions.addContent(binding);

		// create service
		Element service = new Element("service", wsdlNS);
		service.setAttribute("name", "ProcessingService");
		definitions.addContent(service);
		Element port = new Element("port", wsdlNS);
		service.addContent(port);
		port.setAttribute("name", "ProcessingServiceSOAP");
		port.setAttribute("binding", psNS.getPrefix() + ":ProcessingServiceSOAP");
		Element address = new Element("address", wsdlsoapNS);
		port.addContent(address);
		address.setAttribute("location", serviceURL + "/soap");

		// done
		return document;
	}

	private static void addSchemaImport(Element schema, AbstractXMLEncoding encoding) {
		// get import namespace and location
		String namespace = encoding.getNamespace();
		String schemaLocation = encoding.getSchemaLocation();

		// only add if there is a schema location 
		if (schemaLocation != null) {
			// find if already imported
			List<?> elements = schema.getChildren("import", Namespace.getNamespace(Namespaces.XSD));
			boolean imported = false;
			for (int i = 0; i < elements.size(); i++) {
				Element imp0rt = (Element) elements.get(i);
				if (imp0rt.getAttributeValue("schemaLocation") != null && imp0rt.getAttributeValue("schemaLocation").equals(schemaLocation)) {
					imported = true;
					break;
				}
			}
			if (!imported) {
				schema.addContent(0, new Element("import", Namespace.getNamespace(Namespaces.XSD))
				.setAttribute("namespace", namespace)
				.setAttribute("schemaLocation", schemaLocation));
			}
		}
	}
	
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
			
			JsonArray inputsArray = new JsonArray();
			processObject.add("inputs", inputsArray);
			for (String inputIdentifier : process.getInputIdentifiers()) {
				JsonObject inputObject = new JsonObject();
				inputsArray.add(inputObject);
				inputObject.addProperty("identifier", inputIdentifier);
				
				DataDescription dataDesc = process.getInputDataDescription(inputIdentifier);
				inputObject.addProperty("type", dataDesc.getClassOf().getSimpleName().toLowerCase());
				
				// more
			}
			
			JsonArray outputsArray = new JsonArray();
			processObject.add("outputs", outputsArray);
			for (String outputIdentifier : process.getOutputIdentifiers()) {
				JsonObject outputObject = new JsonObject();
				outputsArray.add(outputObject);
				outputObject.addProperty("identifier", outputIdentifier);
				
				DataDescription dataDesc = process.getOutputDataDescription(outputIdentifier);
				outputObject.addProperty("type", dataDesc.getClassOf().getSimpleName().toLowerCase());
			}
		}
		
		// all done
		return object;
	}
}
