package org.uncertweb.ps.handler.soap;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

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