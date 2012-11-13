package org.uncertweb.ps.handler.soap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uncertweb.ps.test.Utilities;
import org.uncertweb.xml.Namespaces;

public class SOAPRequestHandlerTest {
	
	private SOAPRequestHandler handler;

	@BeforeClass
	public static void setUp() {
		Utilities.setupProcessRepository();
	}
	
	@Before
	public void before() {
		handler = new SOAPRequestHandler();
	}
	
	@Test
	public void handleRequest() throws JDOMException, IOException {
		// handle request
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("sum-soap-request.xml");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		handler.handleRequest(is, os);
		
		// read output
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(new ByteArrayInputStream(os.toByteArray()));
		
		// check
		Assert.assertNotNull(doc);
		Element envelope = doc.getRootElement();
		Assert.assertNotNull(envelope);
		Assert.assertEquals("Envelope", envelope.getName());
		Assert.assertEquals(Namespaces.SOAPENV, envelope.getNamespace());
		Element body = envelope.getChild("Body", Namespaces.SOAPENV);
		Assert.assertNotNull(body);
		Element response = body.getChild("SumProcessResponse", Namespaces.PS);
		Assert.assertNotNull(response);
		Assert.assertTrue(response.getChildren().size() > 0);
	}
	
}
