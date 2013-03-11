package org.uncertweb.ps.handler.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.Request;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.test.HTTPServer;

import com.vividsolutions.jts.geom.Polygon;

/**
 * Tests to ensure a document built using DocumentBuilder can be parsed by XMLRequestParser.
 * 
 * @author Richard Jones
 *
 */
// FIXME: share tests with XMLRequestParserTest
public class ParseRequestIntegrationTest {	
	
	@Rule
	public ConfiguredService service = new ConfiguredService();
		
	@Rule
	public HTTPServer server = new HTTPServer(8000);

	private DocumentBuilder builder;

	@Before
	public void before() {
		builder = new DocumentBuilder();
	}

	@Test
	public void parseWithDataReference() throws JDOMException, IOException, RequestParseException {
		// expose file
		server.addFileHandler("xml/polygon.xml");

		// parse
		Element root = builder.build(this.getClass().getClassLoader().getResourceAsStream("xml/bufferpolygon-request.xml")).getRootElement();
		Request request = XMLRequestParser.parse(root);
		ProcessInputs inputs = request.getInputs();

		// check process
		assertEquals("BufferPolygonProcess", request.getProcessIdentifier());
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		assertNotNull(polygon);
	}

}
