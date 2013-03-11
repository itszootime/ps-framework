package org.uncertweb.test.integration;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.junit.Before;
import org.uncertweb.ps.handler.soap.DocumentBuilder;
import org.uncertweb.ps.handler.soap.XMLRequestParserTest;

/**
 * Tests to ensure a document built using DocumentBuilder can be parsed by XMLRequestParser.
 * 
 * @author Richard Jones
 *
 */
public class ParseRequestTest extends XMLRequestParserTest {	

	private DocumentBuilder builder;

	@Before
	public void before() {
		builder = new DocumentBuilder();
	}
	
	protected Document buildDocument(String path) throws JDOMException, IOException {
		return builder.build(this.getClass().getClassLoader().getResourceAsStream(path));
	}

}
