package org.uncertweb.ps.handler.soap;

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
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.xml.Namespaces;

public class SchemaGenerator {

	// FIXME: would be good to cache
	
	
	private ProcessRepository processRepo;
	private EncodingRepository encodingRepo;
		
	public SchemaGenerator() {
		processRepo = ProcessRepository.getInstance();
		encodingRepo = EncodingRepository.getInstance();
	}
			
	public Document generateDocument() {		
		Element schema = new Element("schema", Namespaces.XSD);
		schema.setAttribute("targetNamespace", Namespaces.PS.getURI());
		schema.setAttribute("elementFormDefault", "qualified");

		// keep things tidy
		schema.addNamespaceDeclaration(Namespaces.PS);
		schema.addNamespaceDeclaration(Namespaces.XSD);

		// get processes
		List<AbstractProcess> processes = processRepo.getProcesses();	
		
		// make a map for namespace > prefix mapping
		Map<String, String> namespaceMapping = new HashMap<String, String>();
		namespaceMapping.put(Namespaces.PS.getURI(), "ps");
		namespaceMapping.put(Namespaces.XSD.getURI(), "xsd");
		int nextNS = 0;
		for (AbstractProcess process : processes) {
			// TODO: handle encoding not found
			for (String inputIdentifier : process.getInputIdentifiers()) {
				// get things
				Class<?> dataClass = process.getInputDataDescription(inputIdentifier).getType();
				Encoding encoding = encodingRepo.getXMLEncoding(dataClass);
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
				Class<?> dataClass = process.getOutputDataDescription(outputIdentifier).getType();
				Encoding encoding = encodingRepo.getXMLEncoding(dataClass);
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
		schema.addContent(new Element("element", Namespaces.XSD)
			.setAttribute("name", "DataReference")
			.addContent(new Element("complexType", Namespaces.XSD)
				.addContent(new Element("sequence", Namespaces.XSD))
			.addContent(new Element("attribute", Namespaces.XSD)
				.setAttribute("name", "href")
				.setAttribute("type", Namespaces.XSD.getPrefix() + ":anyURI")
				.setAttribute("use", "required"))
			.addContent(new Element("attribute", Namespaces.XSD)
				.setAttribute("name", "mimeType")
				.setAttribute("type", Namespaces.XSD.getPrefix() + ":string")
				.setAttribute("use", "required"))
			.addContent(new Element("attribute", Namespaces.XSD)
				.setAttribute("name", "compressed")
				.setAttribute("type", Namespaces.XSD.getPrefix() + ":boolean")
				.setAttribute("default", "false"))));

		// add process specific info
		for (AbstractProcess process : processes) {
			// create request element
			Element requestSequence = new Element("sequence", Namespaces.XSD);
			schema.addContent(new Element("element", Namespaces.XSD)
				.setAttribute("name", process.getIdentifier() + "Request")
				.addContent(new Element("complexType", Namespaces.XSD)
					.addContent(requestSequence)));
					/*.addContent(new Element("attribute", Namespaces.XSD)
						.setAttribute("name", "asynchronous")
						.setAttribute("type", Namespaces.XSD.getPrefix() + ":boolean")
						.setAttribute("default", "false"))));*/

			// add inputs
			for (String inputIdentifier : process.getInputIdentifiers()) {
				requestSequence.addContent(createInputElement(schema, namespaceMapping, inputIdentifier, process.getInputDataDescription(inputIdentifier), process.getInputMetadata(inputIdentifier)));
			}

			// add req outputs
			// create requested outputs elements
			Element reqOutputsSequence = new Element("sequence", Namespaces.XSD);
			requestSequence.addContent(new Element("element", Namespaces.XSD)
				.setAttribute("minOccurs", "0")
				.setAttribute("name", "RequestedOutputs")
					.addContent(new Element("complexType", Namespaces.XSD)
						.addContent(reqOutputsSequence)));

			// create response
			Element responseSequence = new Element("sequence", Namespaces.XSD);
			schema.addContent(new Element("element", Namespaces.XSD)
				.setAttribute("name", process.getIdentifier() + "Response")
				.addContent(new Element("complexType", Namespaces.XSD)
					.addContent(responseSequence)));

			// for outputs
			for (String outputIdentifier : process.getOutputIdentifiers()) {
				responseSequence.addContent(createOutputElement(schema, namespaceMapping, outputIdentifier, process.getOutputDataDescription(outputIdentifier), process.getOutputMetadata(outputIdentifier)));
				reqOutputsSequence.addContent(new Element("element", Namespaces.XSD)
					.setAttribute("name", outputIdentifier)
					.setAttribute("minOccurs", "0")
					.addContent(new Element("complexType", Namespaces.XSD)
						.addContent(new Element("sequence", Namespaces.XSD))
						.addContent(new Element("attribute", Namespaces.XSD)
							.setAttribute("name", "reference")
							.setAttribute("type", Namespaces.XSD.getPrefix() + ":boolean")
							.setAttribute("default", "false"))));
			}
		}
		
		// return document
		return new Document(schema);
	}
	
	private Element createInputElement(Element schema, Map<String, String> namespaceMapping, String identifier, DataDescription dataDescription, List<Metadata> metadata) {
		return createInputOutputElement(schema, namespaceMapping, identifier, dataDescription, metadata);
	}
	
	private Element createOutputElement(Element schema, Map<String, String> namespaceMapping, String identifier, DataDescription dataDescription, List<Metadata> metadata) {
		Element element = createInputOutputElement(schema, namespaceMapping, identifier, dataDescription, metadata);
		element.setAttribute("minOccurs", "0");
		return element;
	}

	private Element createInputOutputElement(Element schema, Map<String, String> namespaceMapping, String identifier, DataDescription dataDescription, List<Metadata> metadata) {
		// get things
		Class<?> dataClass = dataDescription.getType();

		// add create element
		Element element = new Element("element", Namespaces.XSD)
			.setAttribute("name", identifier)
			.setAttribute("minOccurs", (dataDescription.getMinOccurs() >= 1 ? "1" : "0"))
			.setAttribute("maxOccurs", (dataDescription.getMaxOccurs() == Integer.MAX_VALUE ? "unbounded" : String.valueOf(dataDescription.getMaxOccurs())));
		
		// add metadata
		addMetadata(element, metadata);
		
		// add type
		List<Class<?>> supportedSimpleTypes = Arrays.asList(new Class<?>[] {
				Float.class, Double.class, Boolean.class, URL.class, String.class, Integer.class,
				Float[].class, Double[].class, Boolean.class, URL[].class, String[].class, Integer[].class, LocalDate.class, LocalDate[].class });
		if (supportedSimpleTypes.contains(dataClass)) {
			// float, double, boolean, anyURI, string, integer for now...
			String xsdType = Namespaces.XSD.getPrefix() + ":"; 
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
				element.addContent(new Element("simpleType", Namespaces.XSD)
					.addContent(new Element("list", Namespaces.XSD)
						.setAttribute("itemType", xsdType)));
			}
			else {
				element.setAttribute("type", xsdType);
			}
			
		}
		else {
			// add choice
			Element choice = new Element("choice", Namespaces.XSD);
			element.addContent(new Element("complexType", Namespaces.XSD)
				.addContent(choice));
			
			// get encoding
			Encoding encoding = encodingRepo.getXMLEncoding(dataClass);
			// FIXME: no appropriate encoding will result in encoding == null, should throw an exception for the servlet to catch
		
			if (!(encoding instanceof AbstractBinaryEncoding)) {
				// get encoding
				AbstractXMLEncoding xmlEncoding = (AbstractXMLEncoding) encoding;
				String namespace = xmlEncoding.getNamespace();
	
				// add imports
				addSchemaImport(schema, xmlEncoding);
				
				// add reference
				Include include = xmlEncoding.getInclude(dataClass);
				choice.addContent(new Element("element", Namespaces.XSD)
					.setAttribute("ref", namespaceMapping.get(namespace) + ":" + include.getName()));
			}
			
			// add reference
			choice.addContent(new Element("element", Namespaces.XSD)
				.setAttribute("ref", Namespaces.PS.getPrefix() + ":DataReference"));
		}
		
		return element;
	}

	private static void addMetadata(Element element, List<Metadata> metadata) {
		if (metadata != null && metadata.size() > 0) {
			String metadataText = "";
			for (Metadata m : metadata) {
				metadataText += "\n@" + m.getKey() + " " + m.getValue();
			}
			// looks like jdom strips whitespace at start and end of string
			element.addContent(new Element("annotation", Namespaces.XSD).addContent(new Element("documentation", Namespaces.XSD).setText(metadataText)));
		}
	}

	public static Document generateWSDL(String serviceURL) {
		// namespaces to use, keeps things tidy using set prefixes
		Namespace wsdlNS = Namespaces.WSDL;
		Namespace psNS = Namespaces.PS;
		Namespace xsdNS = Namespaces.XSD;
		Namespace wsdlsoapNS = Namespaces.WSDLSOAP;
		
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
			addMetadata(portOperation, process.getMetadata());
			
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
			List<?> elements = schema.getChildren("import", Namespaces.XSD);
			boolean imported = false;
			for (int i = 0; i < elements.size(); i++) {
				Element imp0rt = (Element) elements.get(i);
				if (imp0rt.getAttributeValue("schemaLocation") != null && imp0rt.getAttributeValue("schemaLocation").equals(schemaLocation)) {
					imported = true;
					break;
				}
			}
			if (!imported) {
				schema.addContent(0, new Element("import", Namespaces.XSD)
				.setAttribute("namespace", namespace)
				.setAttribute("schemaLocation", schemaLocation));
			}
		}
	}
	
	
}
