package org.uncertweb.ps.handler.soap;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.uncertweb.ps.data.Metadata;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.xml.Namespaces;

public class WSDLGenerator {
	
	public WSDLGenerator() {
		
	}

	public Document generateDocument(String serviceURL) {
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
	
}
