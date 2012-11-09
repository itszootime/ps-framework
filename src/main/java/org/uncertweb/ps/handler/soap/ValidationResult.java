package org.uncertweb.ps.handler.soap;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXParseException;

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