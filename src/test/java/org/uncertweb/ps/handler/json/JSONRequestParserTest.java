package org.uncertweb.ps.handler.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

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

import com.google.gson.JsonObject;
import com.vividsolutions.jts.geom.Polygon;

public class JSONRequestParserTest {

	@Rule
	public ConfiguredService service = new ConfiguredService();
	
	@Rule
	public HTTPFileServer server = new HTTPFileServer(8000);
	
	@Test
	public void parseWithPrimitiveMultiple() throws JDOMException, IOException, RequestParseException {
		JsonObject object = TestUtils.loadJSON("json/sum-request.json");
		Request request = JSONRequestParser.parse(object);
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
		JsonObject object = TestUtils.loadJSON("json/hash-request.json");
		Request request = JSONRequestParser.parse(object);
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
		JsonObject object = TestUtils.loadJSON("json/hash-request.json");
		object.get("HashProcessRequest").getAsJsonObject().add("RequestedOutputs", new JsonObject());
		Request request = JSONRequestParser.parse(object);
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
	public void parseWithComplex() throws IOException, RequestParseException {
		JsonObject object = TestUtils.loadJSON("json/bufferpolygon-request-inline.json");
		Request request = JSONRequestParser.parse(object);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReference() throws JDOMException, IOException, RequestParseException {
		JsonObject object = TestUtils.loadJSON("json/bufferpolygon-request.json");
		Request request = JSONRequestParser.parse(object);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReferenceMimeType() throws JDOMException, IOException, RequestParseException {
		JsonObject object = TestUtils.loadJSON("json/bufferpolygon-request.json");
		JsonObject dataRef = object.get("BufferPolygonProcessRequest").getAsJsonObject().get("Polygon").getAsJsonObject().get("DataReference").getAsJsonObject();
		dataRef.addProperty("href", "http://localhost:8000/xml/polygon.xml");
		dataRef.addProperty("mimeType", "text/xml");
		
		// parse
		Request request = JSONRequestParser.parse(object);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReferenceCompressed() throws JDOMException, IOException, RequestParseException {
		JsonObject object = TestUtils.loadJSON("json/bufferpolygon-request.json");
		JsonObject dataRef = object.get("BufferPolygonProcessRequest").getAsJsonObject().get("Polygon").getAsJsonObject().get("DataReference").getAsJsonObject();
		dataRef.addProperty("href", "http://localhost:8000/json/polygon.zip");
		dataRef.addProperty("compressed", "true");
		
		// parse
		Request request = JSONRequestParser.parse(object);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
	@Test
	public void parseWithDataReferenceMimeTypeCompressed() throws JDOMException, IOException, RequestParseException {
		JsonObject object = TestUtils.loadJSON("json/bufferpolygon-request.json"); // should assume json encoded ref
		JsonObject dataRef = object.get("BufferPolygonProcessRequest").getAsJsonObject().get("Polygon").getAsJsonObject().get("DataReference").getAsJsonObject();
		dataRef.addProperty("href", "http://localhost:8000/xml/polygon.zip");
		dataRef.addProperty("compressed", "true");
		dataRef.addProperty("mimeType", "text/xml");
		
		// parse
		Request request = JSONRequestParser.parse(object);
		ProcessInputs inputs = request.getInputs();
		
		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}
	
}
