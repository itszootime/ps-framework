package org.uncertweb.ps.handler.soap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.uncertweb.ps.ClientException;
import org.uncertweb.ps.ServiceException;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.handler.ResponseGenerateException;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.util.Stopwatch;
import org.uncertweb.xml.Namespaces;
import org.uncertweb.xml.SoapBody;
import org.uncertweb.xml.SoapEnvelope;
import org.uncertweb.xml.SoapFault;
import org.uncertweb.xml.SoapFault.Code;

public class SOAPHandler {

	private final Logger logger = Logger.getLogger(SOAPHandler.class);

	public void handleRequest(InputStream inputStream, OutputStream outputStream) {
		// response envelope + body
		SoapEnvelope responseEnvelope = new SoapEnvelope();
		SoapBody responseBody = new SoapBody();
		responseEnvelope.addContent(responseBody);

		try {
			// build request document
			logger.debug("Parsing request document...");
			Stopwatch stopwatch = new Stopwatch();	
			DocumentBuilder builder = new DocumentBuilder();
			Document reqSoapDocument = builder.build(inputStream);
			logger.debug("Parsed document in " + stopwatch.getElapsedTime() + ".");
			
			// validate		
			ValidationResult validationResult = builder.getValidationResult();
			if (!validationResult.isValid()) {
				throw new ClientException("Request document failed schema validation.", validationResult.getPrettierResult());
			}

			// parse request
			List<?> bodyChildren = reqSoapDocument.getRootElement().getChild("Body", Namespaces.SOAPENV).getChildren();
			Element requestElement = (Element)bodyChildren.get(0);
			Request request = XMLRequestParser.parse(requestElement);

			// find process
			AbstractProcess process = ProcessRepository.getInstance().getProcess(request.getProcessIdentifier());

			// run process		
			ProcessOutputs outputs = process.run(request.getInputs());

			// build response
			Response response = new Response(process.getIdentifier(), outputs);

			// generate response element
			Element responseElement = XMLResponseGenerator.generate(response, request.getRequestedOutputs());
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
		catch (Exception e) {
			handleException(e, responseBody);
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

	private void handleException(Exception e, SoapBody responseBody) {
		if (e instanceof IOException) {
			logger.error("Couldn't read request from stream.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, "Couldn't read request."));
		}
		else if (e instanceof ClientException) {
			logger.error("Client exception.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Client, e.getMessage(), ((ClientException)e).getDetail()));
		} else if (e instanceof ServiceException) {
			logger.error("Service exception.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, e.getMessage(), ((ServiceException)e).getDetail()));
		}
		else if (e instanceof ProcessException) {
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
		else if (e instanceof JDOMException) {
			logger.error("Problem reading/generating request/response.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, "Problem reading/generating request/response."));
		}
		else if (e instanceof RequestParseException) {
			logger.error("Couldn't parse request.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, "Couldn't parse request."));
		}
		else if (e instanceof ResponseGenerateException) {
			logger.error("Couldn't generate response.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, "Couldn't generate response."));
		}
		else if (e instanceof RuntimeException) {
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
	}

}
