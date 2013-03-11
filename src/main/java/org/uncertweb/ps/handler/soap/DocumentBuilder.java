package org.uncertweb.ps.handler.soap;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.uncertweb.ps.Config;

/**
 * An XML document builder that validates too.
 * 
 * @author Richard Jones
 */
public class DocumentBuilder {

	private SAXBuilder builder;
	private ValidationErrorHandler errorHandler;
	private static final XMLGrammarPool POOL = new XMLGrammarPoolImpl();
	
	public DocumentBuilder() {
		// might need to use something else for large documents here?
		builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", true);
		builder.setIgnoringElementContentWhitespace(true);
		
		// get schema locations
		String sls = getSchemaLocationString();
		
		// set builder properties
		builder.setFeature("http://apache.org/xml/features/validation/schema", true);
		builder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", sls.toString().trim());
		builder.setProperty("http://apache.org/xml/properties/internal/grammar-pool", POOL);
	}
	
	public Document build(InputStream stream) throws JDOMException, IOException {
		errorHandler = new ValidationErrorHandler();
		builder.setErrorHandler(errorHandler);
		return builder.build(stream);
	}
	
	public ValidationResult getValidationResult() {
		return errorHandler.getResult();
	}
	
	private String getSchemaLocationString() {
		// map containing the schemas we use
		// namespace and schema location
		Map<String, String> schemaMap = new HashMap<String, String>();
		schemaMap.put("http://www.w3.org/XML/1998/namespace", this.getClass().getClassLoader().getResource("schemas/xml.xsd").toString());
		schemaMap.put("http://www.w3.org/1999/xlink", this.getClass().getClassLoader().getResource("schemas/xlink.xsd").toString());
		schemaMap.put("http://schemas.xmlsoap.org/soap/envelope/", this.getClass().getClassLoader().getResource("schemas/envelope.xsd").toString());
		schemaMap.put("http://www.uncertweb.org/ProcessingService", Config.getInstance().getServerProperty("baseURL") + "/service?schema");
		
		// build string
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : schemaMap.entrySet()) {
			// add each schema namespace, location (space separated)
			sb.append(" " + entry.getKey() + " " + entry.getValue());
		}
		
		return sb.toString().trim();
	}
	
}
