package org.uncertweb.ps.handler.soap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.xml.Namespaces;
public class SOAPHandlerTest {
	
	@Rule
	public ConfiguredService service = new ConfiguredService();
	
	private SOAPHandler handler;
	
	@Before
	public void before() {
		handler = new SOAPHandler();
	}
	
	@Test
	public void returnsEnvelope() throws JDOMException, IOException {
		Element envelope = handleTestSumRequest();
		assertThat(envelope.getName(), equalTo("Envelope"));
		assertThat(envelope.getNamespace(), equalTo(Namespaces.SOAPENV));
	}
	
	@Test
	public void returnsBody() throws JDOMException, IOException {
		Element envelope = handleTestSumRequest();
		Element body = envelope.getChild("Body", Namespaces.SOAPENV);
		assertThat(body, notNullValue());
	}
	
	@Test
	public void returnsResponse() throws JDOMException, IOException {
		Element envelope = handleTestSumRequest();
		Element body = envelope.getChild("Body", Namespaces.SOAPENV);
		Element response = body.getChild("SumProcessResponse", Namespaces.PS);
		assertThat(response, notNullValue());
	}
	
	@Test
	public void returnsFaultForInvalidDocument() throws JDOMException, IOException {
		Element envelope = handleTestBufferPolygonInvalidRequest();
		Element body = envelope.getChild("Body", Namespaces.SOAPENV);
		Element fault = body.getChild("Fault", Namespaces.SOAPENV);
		assertThat(fault, notNullValue());
	}
	
	@Test
	public void returnsFaultForWrongDataTypes() throws JDOMException, IOException {
		Element envelope = handleTestSumInvalidRequest();
		Element body = envelope.getChild("Body", Namespaces.SOAPENV);
		Element fault = body.getChild("Fault", Namespaces.SOAPENV);
		assertThat(fault, notNullValue());
	}
	
	private Element handleTestSumRequest() throws JDOMException, IOException {
		return handleTestRequest("xml/sum-soaprequest.xml");
	}
	
	private Element handleTestBufferPolygonInvalidRequest() throws JDOMException, IOException {
		return handleTestRequest("xml/bufferpolygon-invalid-soaprequest.xml");
	}
	
	private Element handleTestSumInvalidRequest() throws JDOMException, IOException {
		return handleTestRequest("xml/sum-invalid-soaprequest.xml");
	}
	
	private Element handleTestRequest(String filename) throws JDOMException, IOException {
		// handle request
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(filename);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		handler.handleRequest(is, os);
		
		// read output
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(new ByteArrayInputStream(os.toByteArray()));
		return document.getRootElement();
	}
	
}
