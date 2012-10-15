package org.uncertweb.ps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.MultipleInput;
import org.uncertweb.ps.data.Output;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.SingleInput;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.Encoding;
import org.uncertweb.ps.encoding.EncodingRepository;
import org.uncertweb.ps.encoding.ParseException;
import org.uncertweb.ps.encoding.binary.AbstractBinaryEncoding;
import org.uncertweb.ps.encoding.xml.AbstractXMLEncoding;
import org.uncertweb.ps.encoding.xml.Namespaces;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;
import org.uncertweb.ps.process.ProcessRepository;
import org.uncertweb.soap.SoapBody;
import org.uncertweb.soap.SoapEnvelope;
import org.uncertweb.soap.SoapFault;
import org.uncertweb.soap.SoapFault.Code;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SOAPRequestHandler {

	private final Logger logger = Logger.getLogger(SOAPRequestHandler.class);

	// TODO: more speedups needed here (ie. cache schemas)
	private static final XMLGrammarPool POOL = new XMLGrammarPoolImpl();	

	public void handleRequest(InputStream is, OutputStream os, String basePath, String baseURL) {
		// here's our response
		SoapEnvelope responseEnvelope = new SoapEnvelope();
		SoapBody responseBody = new SoapBody();
		responseEnvelope.addContent(responseBody);

		try {
			// namespaces for use
			Namespace soapNamespace = Namespace.getNamespace(Namespaces.SOAPENV);
			Namespace psNamespace = Namespace.getNamespace(Namespaces.PS);

			// create builder
			//SAXBuilder builder = new SAXBuilder();
			SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
			builder.setFeature("http://apache.org/xml/features/validation/schema", true);
			builder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
				"http://www.w3.org/XML/1998/namespace " + this.getClass().getClassLoader().getResource("schemas/xml.xsd")
				+ " http://www.w3.org/1999/xlink " + this.getClass().getClassLoader().getResource("schemas/xlink.xsd")
				+ " http://schemas.xmlsoap.org/soap/envelope/ " + this.getClass().getClassLoader().getResource("schemas/envelope.xsd")
				+ " http://www.uncertweb.org/ProcessingService " + baseURL + "/service?schema" 
			);
			builder.setProperty("http://apache.org/xml/properties/internal/grammar-pool", POOL);

			// set validation
			ValidationErrorHandler handler = new ValidationErrorHandler();
			builder.setErrorHandler(handler);

			// build from input
			logger.debug("Building document from request...");
			long start = System.currentTimeMillis();	
			Document reqSoapDocument = builder.build(is);
			logger.debug("Built document in " + (System.currentTimeMillis() - start) / 1000.0 + "s.");

			// check if validated ok
			if (!handler.getResult().isValid()) {
				throw new ClientException("Request document failed schema validation.", handler.getResult().getPrettierResult());
			}

			// get request element
			List<?> bodyChildren = reqSoapDocument.getRootElement().getChild("Body", soapNamespace).getChildren();
			Element requestElement = (Element) bodyChildren.get(0);

			// check if process or description request
			if (requestElement == null || !requestElement.getNamespace().equals(psNamespace)) {
				// TODO: validation doesn't work properly -> seems to validate ok if my request is just in the envlope (not body), hence check here
				throw new ClientException("soap:Body must contain only a processing service request element.");
			}
			else if (requestElement.getName().endsWith("Request")) {
				// extract process id
				String processIdentifier = requestElement.getName().substring(0, requestElement.getName().length() - "Request".length());

				// create request object
				Request request = new Request(processIdentifier);

				// get process
				AbstractProcess process = ProcessRepository.getInstance().getProcess(processIdentifier);

				// if we can't find process
				if (process == null) {
					throw new ClientException("Process with identifier '" + processIdentifier + "' could not be found.");
				}

				// get encoding respository
				EncodingRepository encodingFactory = EncodingRepository.getInstance();

				// lookup inputs for process name
				for (String inputIdentifier : process.getInputIdentifiers()) {
					// get data class for type from process
					DataDescription dataDescription = process.getInputDataDescription(inputIdentifier);
					Class<?> dataClass = dataDescription.getClassOf();

					// get input element
					List<?> inputElements = requestElement.getChildren(inputIdentifier, psNamespace);

					// parse if present
					if (inputElements.size() > 0) {
						// hold data
						List<Object> dataList = new ArrayList<Object>();

						// get each element
						for (Object o : inputElements) {
							// cast to element
							Element inputElement = (Element) o;

							// get value
							try {
								if (inputElement.getChildren().size() == 0) {
									// must be simple data
									// FIXME: may not be though
									addSimpleData(dataClass, dataList, inputElement.getText());
								}
								else {
									Element inputValue = (Element) inputElement.getChildren().get(0);
									if (inputValue.getName().equals("DataReference")) {
										// parse referenced data
										try {
											String href = inputValue.getAttributeValue("href");
											String mimeType = inputValue.getAttributeValue("mimeType");
											String compressedString = inputValue.getAttributeValue("compressed");
											boolean compressed = false;
											if (compressedString != null) {
												if (compressedString.equalsIgnoreCase("true") || compressedString.equals("1")) {
													compressed = true;
												}
											}
											dataList.add(DataReferenceHelper.parseDataReference(href, mimeType, compressed, dataDescription));
										}
										catch (IOException e) {
											throw new ClientException("Couldn't load referenced data for " + inputIdentifier + ".", e.getClass().getSimpleName() + ": " + e.getMessage());
										}
									}
									else {
										// complex data, get xml encoding
										AbstractXMLEncoding encoding = encodingFactory.getXMLEncoding(dataClass);

										// couldn't find suitable encoding
										if (encoding == null) {
											throw new ServiceException("Couldn't find suitable encoding to parse request.", "Encoding could not be found for input with identifier " + inputIdentifier + " and class " + dataClass);
										}

										Object data = (encoding).parse(inputValue, dataClass);
										dataList.add(data);
									}
								}
							}
							catch (ParseException e) {
								throw new ClientException("Couldn't parse input " + inputIdentifier + ".", e.getMessage());
							}
						}

						// add to request
						if (dataDescription.getMaxOccurs() > 1) {							
							request.getInputs().add(new MultipleInput(inputIdentifier, dataList));
						}
						else {
							request.getInputs().add(new SingleInput(inputIdentifier, dataList.get(0)));
						}
					}
				}			

				// requested outputs
				Element reqOutputsElement = requestElement.getChild("RequestedOutputs", psNamespace);
				if (reqOutputsElement != null) {
					for (Object o : reqOutputsElement.getChildren()) {
						Element reqOutputElement = (Element) o;
						String name = reqOutputElement.getName();
						boolean reference = false;
						// FIXME: what if the output name used isn't a real output?
						if (reqOutputElement.getAttributeValue("reference").equals("true") || reqOutputElement.getAttributeValue("reference").equals("1")) {
							reference = true;
						}
						request.getRequestedOutputs().add(new RequestedOutput(name, reference));
					}
				}

				// run process
				// TODO: this should probably be in some sort of service runner, this could also check for correct number of inputs (more appropriate for json)
				// FIXME: pretty sure simultaneous requests will fail
				ProcessOutputs outputs = process.run(request.getInputs());

				// generate response
				Element responseElement = new Element(processIdentifier + "Response", psNamespace);
				responseBody.addContent(responseElement);

				// add outputs
				for (String outputIdentifier : process.getOutputIdentifiers()) {
					// get data description
					DataDescription dataDescription = process.getOutputDataDescription(outputIdentifier);
					Class<?> dataClass = dataDescription.getClassOf();
					
					// get output from process
					Output output = outputs.get(outputIdentifier);

					// get encoding
					Encoding encoding = EncodingRepository.getInstance().getEncoding(dataClass);
					// FIXME: no appropriate encoding will result in encoding == null

					// check if output was requested and whether to return reference
					boolean requested = false;
					boolean reference = false;
					if (request.getRequestedOutputs().size() == 0) {
						requested = true;
					}
					for (RequestedOutput reqOutput : request.getRequestedOutputs()) {
						if (reqOutput.getName().equals(output.getIdentifier())) {
							requested = true;
							reference = reqOutput.isReference();
						}
					}
					if (encoding instanceof AbstractBinaryEncoding) {
						reference = true;
					}

					// add output
					if (requested) {
						List<Object> objects;
						if (output.isSingleOutput()) {
							objects = new ArrayList<Object>();
							objects.add(output.getAsSingleOutput().getObject());
						}
						else {
							objects = output.getAsMultipleOutput().getObjects();
						}

						for (Object o : objects) {
							// generate
							try {
								// base element
								Element outputElement = new Element(output.getIdentifier(), psNamespace);
								responseElement.addContent(outputElement);

								if (!reference) {
									// data
									// FIXME: lots to do here
									if (dataClass.equals(Double.class)) {
										outputElement.setText(o.toString());
									}									
									else if (dataClass.equals(Double[].class)) {
										StringBuilder sb = new StringBuilder();										
										Double[] arr = (Double[]) o;
										for (double d : arr) {
											sb.append(d + " ");
										}
										sb.deleteCharAt(sb.length() - 1);
										outputElement.setText(sb.toString());
									}
									else if (dataClass.equals(Integer.class)) {
										outputElement.setText(o.toString());
									}									
									else if (dataClass.equals(Integer[].class)) {
										StringBuilder sb = new StringBuilder();										
										Integer[] arr = (Integer[]) o;
										for (int d : arr) {
											sb.append(d + " ");
										}
										sb.deleteCharAt(sb.length() - 1);
										outputElement.setText(sb.toString());
									}									
									else if (dataClass.equals(String.class)) {
										outputElement.setText((String) o);
									}
									else {
										outputElement.addContent(((AbstractXMLEncoding) encoding).encode(o).detach());
									}
								}
								else {
									// reference
									try {
										String href = DataReferenceHelper.generateXMLDataReference(o, dataDescription, basePath, baseURL).toString();

										// add reference url to response
										Element referenceElement = new Element("DataReference", psNamespace);
										referenceElement.setAttribute("href", href);
										referenceElement.setAttribute("mimeType", "unknown");
										// FIXME: mimeType, compression
										// TODO: simple by reference
										outputElement.addContent(referenceElement);
									}
									catch (IOException e) {
										throw new ServiceException("Couldn't save output " + output.getIdentifier() + ". File not found: ", e.getMessage());
									}
								}
							}
							catch (EncodeException e) {
								throw new ServiceException("Couldn't generate output " + output.getIdentifier(), e.getMessage());
							}
						}
					}
				}

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
			else {
				// handle GetStatus requests.
				// handle DescribeProcess requests.
			}
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
		catch (ServiceException e) {
			logger.error("Service exception.", e);
			responseBody.removeContent();
			responseBody.addContent(new SoapFault(Code.Server, e.getMessage(), e.getDetail()));
		}
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

		// output
		try {	 
			XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
			outputter.output(new Document(responseEnvelope), os);
		}
		catch (Throwable t) {
			// TODO: panic, soapmessage or transformer could be null as well!
		}
		finally {
			try {
				os.close();
			}
			catch (IOException e) {
				// not much else we can do here
			}
		}
	}

	private void addSimpleData(Class<?> dataClass, List<Object> dataList, String data) throws MalformedURLException {
		String dataClassName = dataClass.getName(); //[L
		String shortDataClassName = dataClassName.substring(dataClassName.lastIndexOf(".") + 1, dataClassName.length());
		if (shortDataClassName.endsWith(";")) {
			shortDataClassName = shortDataClassName.substring(0, shortDataClassName.length() - 1);
		}

		// float, string, integer, boolean, double, url
		// FIXME: cheating by not using full java package name
		String[] values = data.split("\\s+");
		if (shortDataClassName.equals("LocalDate")) {
			// FIXME: no support for timezones here!
			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
			if (!dataClassName.startsWith("[L")) {
				DateTime dateTime = formatter.parseDateTime(values[0]);
				dataList.add(new LocalDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth()));
			}
			else {
				LocalDate[] dates = new LocalDate[values.length];
				for (int i = 0; i < values.length; i++) {
					DateTime dateTime = formatter.parseDateTime(values[i]);
					dates[i] = new LocalDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
				}
				dataList.add(dates);
			}
		}
		else if (shortDataClassName.equals("URL")) {
			if (!dataClassName.startsWith("[L")) {
				dataList.add(new URL(values[0]));
			}
			else {
				URL[] urls = new URL[values.length];
				for (int i = 0; i < values.length; i++) {
					urls[i] = new URL(values[i]);
				}
				dataList.add(urls);
			}
		}
		else if (shortDataClassName.equals("Float")) {
			if (!dataClassName.startsWith("[L")) {
				dataList.add(Float.parseFloat(values[0]));
			}
			else {
				Float[] floats = new Float[values.length];
				for (int i = 0; i < values.length; i++) {
					floats[i] = Float.parseFloat(values[i]);
				}
				dataList.add(floats);
			}
		}
		else if (shortDataClassName.equals("Double")) {
			if (!dataClassName.startsWith("[L")) {
				dataList.add(Double.parseDouble(values[0]));
			}
			else {
				Double[] doubles = new Double[values.length];
				for (int i = 0; i < values.length; i++) {
					doubles[i] = Double.parseDouble(values[i]);
				}
				dataList.add(doubles);
			}
		}
		else if (shortDataClassName.equals("Boolean")) {
			if (!dataClassName.startsWith("[L")) {
				dataList.add(Boolean.parseBoolean(values[0]));
			}
			else {
				Boolean[] bools = new Boolean[values.length];
				for (int i = 0; i < values.length; i++) {
					bools[i] = Boolean.parseBoolean(values[i]);
				}
				dataList.add(bools);
			}
		}
		else if (shortDataClassName.equals("Integer")) {
			if (!dataClassName.startsWith("[L")) {
				dataList.add(Integer.parseInt(values[0]));
			}
			else {
				Integer[] ints = new Integer[values.length];
				for (int i = 0; i < values.length; i++) {
					ints[i] = Integer.parseInt(values[i]);
				}
				dataList.add(ints);
			}
		}
		else if (shortDataClassName.equals("String")) {
			if (!dataClassName.startsWith("[L")) {
				dataList.add(data);
			}
			else {
				String[] strings = new String[values.length];
				for (int i = 0; i < values.length; i++) {
					strings[i] = values[i];
				}
				dataList.add(strings);
			}
		}
	}

	public class ValidationErrorHandler implements ErrorHandler {
		private ValidationResult result;

		public ValidationErrorHandler() {
			result = new ValidationResult();
		}

		public void error(SAXParseException arg0) throws SAXException {
			result.addErrorException(arg0);
		}

		public void fatalError(SAXParseException arg0) throws SAXException {
			result.addFatalException(arg0);			
		}

		public void warning(SAXParseException arg0) throws SAXException {
			result.addWarningException(arg0);			
		}

		public ValidationResult getResult() {
			return result;
		}
	}

	public class ValidationResult {
		private List<SAXParseException> warningExceptions;
		private List<SAXParseException> errorExceptions;
		private List<SAXParseException> fatalExceptions;

		public ValidationResult() {
			warningExceptions = new ArrayList<SAXParseException>();
			errorExceptions = new ArrayList<SAXParseException>();
			fatalExceptions = new ArrayList<SAXParseException>();
		}

		public boolean isValid() {
			if (errorExceptions.size() == 0 && fatalExceptions.size() == 0) {
				return true;
			}
			return false;
		}

		public void addErrorException(SAXParseException exception) {
			errorExceptions.add(exception);
		}

		public void addFatalException(SAXParseException exception) {
			fatalExceptions.add(exception);
		}

		public void addWarningException(SAXParseException exception) {
			warningExceptions.add(exception);
		}

		public String getPrettierResult() {
			StringBuilder builder = new StringBuilder();
			for (SAXParseException exception : fatalExceptions) {
				builder.append("FATAL:" + exception.getLineNumber() + " " + exception.getMessage() + "\n");
			}
			for (SAXParseException exception : errorExceptions) {
				builder.append("ERROR:" + exception.getLineNumber() + " " + exception.getMessage() + "\n");
			}
			for (SAXParseException exception : warningExceptions) {
				builder.append("WARNING:" + exception.getLineNumber() + " " + exception.getMessage() + "\n");
			}
			return builder.toString().trim();
		}
	}

}
