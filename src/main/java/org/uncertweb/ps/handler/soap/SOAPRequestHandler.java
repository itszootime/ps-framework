package org.uncertweb.ps.handler.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.uncertweb.ps.ClientException;
import org.uncertweb.ps.Config;
import org.uncertweb.ps.ServiceException;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.util.Stopwatch;
import org.uncertweb.xml.Namespaces;
import org.uncertweb.xml.SoapBody;
import org.uncertweb.xml.SoapEnvelope;
import org.uncertweb.xml.SoapFault;
import org.uncertweb.xml.SoapFault.Code;

public class SOAPRequestHandler {
	
	// TODO: more speedups needed here (ie. cache schemas)
	private static final XMLGrammarPool POOL = new XMLGrammarPoolImpl();

	private final Logger logger = Logger.getLogger(SOAPRequestHandler.class);

	public void handleRequest(InputStream inputStream, OutputStream outputStream) {
		// response envelope + body
		SoapEnvelope responseEnvelope = new SoapEnvelope();
		SoapBody responseBody = new SoapBody();
		responseEnvelope.addContent(responseBody);

		try {
			// validate
			// create builder
			SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
			
			// schema locations map
			Map<String, String> schemaMap = new HashMap<String, String>();
			schemaMap.put("http://www.w3.org/XML/1998/namespace", this.getClass().getClassLoader().getResource("schemas/xml.xsd").toString());
			schemaMap.put("http://www.w3.org/1999/xlink", this.getClass().getClassLoader().getResource("schemas/xlink.xsd").toString());
			schemaMap.put("http://schemas.xmlsoap.org/soap/envelope/", this.getClass().getClassLoader().getResource("schemas/envelope.xsd").toString());
			schemaMap.put("http://www.uncertweb.org/ProcessingService", Config.getInstance().getServerProperty("baseURL") + "/service?schema");
			
			// set validation properties
			String schemaLocation = new String();
			for (Map.Entry<String, String> entry : schemaMap.entrySet()) {
				schemaLocation += " " + entry.getKey() + " " + entry.getValue();
			}
			builder.setFeature("http://apache.org/xml/features/validation/schema", true);
			builder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaLocation.toString().trim());
			builder.setProperty("http://apache.org/xml/properties/internal/grammar-pool", POOL);
			
			// set validation
			ValidationErrorHandler handler = new ValidationErrorHandler();
			builder.setErrorHandler(handler);

			// build
			logger.debug("Building request document...");
			Stopwatch stopwatch = new Stopwatch();	
			Document reqSoapDocument = builder.build(inputStream);
			logger.debug("Built document in " + stopwatch.getElapsedTime() + ".");

			// check if validated ok
			if (!handler.getResult().isValid()) {
				throw new ClientException("Request document failed schema validation.", handler.getResult().getPrettierResult());
			}
			
			// get request element
			List<?> bodyChildren = reqSoapDocument.getRootElement().getChild("Body", Namespaces.SOAPENV).getChildren();
			Element requestElement = (Element) bodyChildren.get(0);
			
			// build a request
			Request request = XMLRequestParser.parse(requestElement);
			
			// find process
			AbstractProcess process = ProcessRepository.getInstance().getProcess(request.getProcessIdentifier());
			
			// run process
			// TODO: this should probably be in some sort of service runner, this could also check for correct number of inputs (more appropriate for json)
			// FIXME: pretty sure simultaneous requests will fail			
			ProcessOutputs outputs = process.run(request.getInputs());
			
			// build response
			Element responseElement = XMLResponseGenerator.generate(process, request.getRequestedOutputs(), outputs);
			responseBody.addContent(responseElement);

			// TODO: remove post validation
			/*
			try {
				// create validator and set handler
				ValidationErrorHandler respHandler = new ValidationErrorHandler();
				builder.setErrorHandler(respHandler);

				// validate and return result
				builder.build(new StringReader(new XMLOutputter().outputString(new Document(responseEnvelope))));
				ValidationResult respValResult = respHandler.getResult();
				if (!respValResult.isValid()) {
					LOGGER.error("Generated an invalid response document (" + respValResult.getPrettierResult() + ").");
					//throw new ServiceException("Generated an invalid response document.", respValResult.getPrettierResult());
				}
			}
			catch (IOException e) {
				LOGGER.error("Couldn't validate response document", e);
				//throw new ServiceException("Couldn't validate response document.", e.getClass().getSimpleName() + ": " + e.getMessage());
			}
			catch (JDOMException e) {
				LOGGER.error("Couldn't validate response document", e);
				//throw new ServiceException("Couldn't validate response document.", e.getClass().getSimpleName() + ": " + e.getMessage());
			}*/
		}
		catch (IOException e) {
			logger.error("Couldn't read request from stream.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, "Couldn't read request."));
		}
		catch (ClientException e) {
			logger.error("Client exception.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Client, e.getMessage(), e.getDetail()));
		}
//		catch (ServiceException e) {
//			logger.error("Service exception.", e);
//			responseBody.removeContent();
//			responseBody.addContent(new SoapFault(Code.Server, e.getMessage(), e.getDetail()));
//		}
		catch (ProcessException e) {
			logger.error("Failed to execute process.", e);
			responseBody.removeContent();
			String message = "Failed to execute process";
			if (e.getMessage() != null) {
				message += ": " + e.getMessage();
			}
			else {
				message += ".";
			}
			SoapFault fault = new SoapFault(Code.Server, message);
			if (e.getCause() != null) {
				fault.setDetail(e.getCause().getMessage());
			}
			responseBody.addContent(fault);
		}
		catch (JDOMException e) {
			logger.error("Problem reading/generating request/response.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, "Problem reading/generating request/response."));
		}
		catch (RuntimeException e) {
			logger.error("Failed to handle request.", e);
			responseBody.removeContent();
			String message = "Failed to handle request.";
			if (e.getMessage() != null) {
				message += ": " + e.getMessage();
			}
			else {
				message += ".";
			}
			responseBody.addContent(new SoapFault(Code.Server, message));
		}
		catch (RequestParseException e) {
			logger.error("Couldn't parse request.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, "Couldn'parse request."));
		}

		// output
		try {	 
			XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
			outputter.output(new Document(responseEnvelope), outputStream);
		}
		catch (Throwable t) {
			// TODO: panic, soapmessage or transformer could be null as well!
		}
		finally {
			try {
				outputStream.close();
			}
			catch (IOException e) {
				// not much else we can do here
			}
		}
	}

}
