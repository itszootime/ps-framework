package org.uncertweb.ps.handler.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.test.HTTPFileServer;
import org.uncertweb.test.util.TestUtils;
import org.uncertweb.xml.Namespaces;

import com.vividsolutions.jts.geom.Polygon;

public class XMLRequestParserTest {
	
	@Rule
	public ConfiguredService service = new ConfiguredService();
	
	@Rule
	public HTTPFileServer server = new HTTPFileServer(8000);
	
	protected Document buildDocument(String path) throws JDOMException, IOException {
		return TestUtils.loadXML(path);
	}
	
	@Test
	public void parseWithPrimitiveMultiple() throws JDOMException, IOException, RequestParseException {
		Document document = buildDocument("xml/sum-request.xml");
		Request request = XMLRequestParser.parse(document.getRootElement());
		ProcessInputs inputs = request.getInputs();

		// check process
		assertEquals("SumProcess", request.getProcessIdentifier());

		// check inputs
		TestUtils.testSingleInput(inputs, "A", new Double(4.002));
		TestUtils.testMultipleInput(inputs, "B", new Double[] {
				1000d, 2000d, 3000d
		});
		
		// check requested outputs
		List<RequestedOutput> requestedOutputs = request.getRequestedOutputs();
		assertNull(requestedOutputs);
	}

	@Test
	public void parseWithRequestedOutputs() throws JDOMException, IOException, RequestParseException {
		Document document = buildDocument("xml/hash-request.xml");
		Request request = XMLRequestParser.parse(document.getRootElement());
		ProcessInputs inputs = request.getInputs();

		// check process
		assertEquals("HashProcess", request.getProcessIdentifier());

		// check inputs
		TestUtils.testSingleInput(inputs, "String", "i am a string to be hashed");
		
		// check requested outputs
		List<RequestedOutput> requestedOutputs = request.getRequestedOutputs();
		assertNotNull(requestedOutputs);
		assertEquals(1, requestedOutputs.size());
		RequestedOutput sha1Output = requestedOutputs.get(0);
		assertEquals("SHA1", sha1Output.getName());
		assertFalse(sha1Output.isReference());
	}
	
	@Test
	public void parseWithRequestedOutputsEmpty() throws JDOMException, IOException, RequestParseException {
		Element root = buildDocument("xml/hash-request.xml").getRootElement();
		root.getChild("RequestedOutputs", Namespaces.PS).removeContent();
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();

		// check process
		assertEquals("HashProcess", request.getProcessIdentifier());

		// check inputs
		TestUtils.testSingleInput(inputs, "String", "i am a string to be hashed");
		
		// check requested outputs
		List<RequestedOutput> requestedOutputs = request.getRequestedOutputs();
		assertNotNull(requestedOutputs);
		assertEquals(0, requestedOutputs.size());
	}
	
	@Test
	public void parseWithComplex() throws IOException, JDOMException, RequestParseException {
		Element root = buildDocument("xml/bufferpolygon-request-inline.xml").getRootElement();
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReference() throws JDOMException, IOException, RequestParseException {
		// parse
		Element root = buildDocument("xml/bufferpolygon-request.xml").getRootElement();
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReferenceMimeType() throws JDOMException, IOException, RequestParseException {
		// load request and change ref url to zip
		Element root = buildDocument("xml/bufferpolygon-request.xml").getRootElement();
		Element dataRef = root.getChild("Polygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		dataRef.setAttribute("href", "http://localhost:8000/xml/polygon.xml");
		dataRef.setAttribute("mimeType", "text/xml");
		
		// parse
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReferenceCompressed() throws JDOMException, IOException, RequestParseException {
		// load request and change ref url to zip
		Element root = buildDocument("xml/bufferpolygon-request.xml").getRootElement();
		Element dataRef = root.getChild("Polygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		dataRef.setAttribute("href", "http://localhost:8000/xml/polygon.zip");
		dataRef.setAttribute("compressed", "true");
		
		// parse
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReferenceMimeTypeCompressed() throws JDOMException, IOException, RequestParseException {
		// load request and change ref url to zip
		Element root = buildDocument("xml/bufferpolygon-request.xml").getRootElement();
		Element dataRef = root.getChild("Polygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		dataRef.setAttribute("href", "http://localhost:8000/xml/polygon.zip");
		dataRef.setAttribute("mimeType", "text/xml");
		dataRef.setAttribute("compressed", "true");
		
		// parse
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}

}
