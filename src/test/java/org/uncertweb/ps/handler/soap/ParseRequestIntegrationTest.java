package org.uncertweb.ps.handler.soap;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.junit.Before;

/**
 * Tests to ensure a document built using DocumentBuilder can be parsed by XMLRequestParser.
 * 
 * @author Richard Jones
 *
 */
public class ParseRequestIntegrationTest extends XMLRequestParserTest {	

	private DocumentBuilder builder;

	@Before
	public void before() {
		builder = new DocumentBuilder();
	}
	
	protected Document buildDocument(String path) throws JDOMException, IOException {
		return builder.build(this.getClass().getClassLoader().getResourceAsStream(path));
	}

}
