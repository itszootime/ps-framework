package org.uncertweb.ps.handler.soap;

import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.Input;
import org.uncertweb.ps.data.MultipleInput;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.data.SingleInput;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.test.Utilities;
import org.uncertweb.test.HTTPServerResource;
import org.uncertweb.test.SupAssert;
import org.uncertweb.xml.Namespaces;

import com.vividsolutions.jts.geom.Polygon;

public class XMLRequestParserTest {
	
	@Rule
	public HTTPServerResource server = new HTTPServerResource(8000);

	@BeforeClass
	public static void setUp() {
		Utilities.setupProcessRepository();
	}
	
	@Test
	public void parseWithPrimitiveMultiple() throws JDOMException, IOException, RequestParseException {
		Document document = Utilities.loadXML("sum-request.xml");
		Request request = XMLRequestParser.parse(document.getRootElement());
		ProcessInputs inputs = request.getInputs();

		// check process
		Assert.assertEquals("SumProcess", request.getProcessIdentifier());

		// check inputs
		testSingleInput(inputs, "A", new Double(4.002));
		testMultipleInput(inputs, "B", new Double[] {
				1000d, 2000d, 3000d
		});
		
		// check requested outputs
		List<RequestedOutput> requestedOutputs = request.getRequestedOutputs();
		Assert.assertNull(requestedOutputs);
	}

	@Test
	public void parseWithRequestedOutputs() throws JDOMException, IOException, RequestParseException {
		Document document = Utilities.loadXML("hash-request.xml");
		Request request = XMLRequestParser.parse(document.getRootElement());
		ProcessInputs inputs = request.getInputs();

		// check process
		Assert.assertEquals("HashProcess", request.getProcessIdentifier());

		// check inputs
		testSingleInput(inputs, "String", "i am a string to be hashed");
		
		// check requested outputs
		List<RequestedOutput> requestedOutputs = request.getRequestedOutputs();
		Assert.assertNotNull(requestedOutputs);
		Assert.assertEquals(1, requestedOutputs.size());
		RequestedOutput sha1Output = requestedOutputs.get(0);
		Assert.assertEquals("SHA1", sha1Output.getName());
		Assert.assertFalse(sha1Output.isReference());
	}
	
	@Test
	public void parseWithRequestedOutputsEmpty() throws JDOMException, IOException, RequestParseException {
		Element root = Utilities.loadXML("hash-request.xml").getRootElement();
		root.getChild("RequestedOutputs", Namespaces.PS).removeContent();
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();

		// check process
		Assert.assertEquals("HashProcess", request.getProcessIdentifier());

		// check inputs
		testSingleInput(inputs, "String", "i am a string to be hashed");
		
		// check requested outputs
		List<RequestedOutput> requestedOutputs = request.getRequestedOutputs();
		Assert.assertNotNull(requestedOutputs);
		Assert.assertEquals(0, requestedOutputs.size());
	}
	
	@Test
	public void parseWithDataReference() throws JDOMException, IOException, RequestParseException {
		// expose file
		server.addFileHandler("polygon.xml");
		
		// parse
		Element root = Utilities.loadXML("bufferpolygon-request.xml").getRootElement();
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		Assert.assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReferenceCompressed() throws JDOMException, IOException, RequestParseException {
		// expose file
		server.addFileHandler("polygon.zip");
		
		// load request and change ref url to zip
		Element root = Utilities.loadXML("bufferpolygon-request.xml").getRootElement();
		Element dataRef = root.getChild("Polygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		dataRef.setAttribute("href", "http://localhost:8000/polygon.zip");
		dataRef.setAttribute("compressed", "true");
		
		// parse
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		Assert.assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		Assert.assertNotNull(polygon);
	}

	private void testSingleInput(ProcessInputs inputs, String identifier, Object expected) {
		Input input = inputs.get(identifier); 
		Assert.assertNotNull(input);
		Assert.assertEquals(identifier, input.getIdentifier());
		Assert.assertTrue(input instanceof SingleInput);
		Object actual = input.getAsSingleInput().getObjectAs(Object.class);
		Assert.assertEquals(expected, actual);
	}

	private void testMultipleInput(ProcessInputs inputs, String identifier, Object[] expected) {
		Input input = inputs.get(identifier);
		Assert.assertNotNull(input);
		Assert.assertEquals(identifier, input.getIdentifier());
		Assert.assertTrue(input instanceof MultipleInput);
		Object[] actual = input.getAsMultipleInput().getObjectsAs(Object.class).toArray(new Object[0]);
		SupAssert.assertArrayEquals(expected, actual);
	}

}
