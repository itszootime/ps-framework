package org.uncertweb.ps.handler.soap;

import java.io.IOException;

import junit.framework.Assert;

import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.data.SingleOutput;
import org.uncertweb.ps.handler.ResponseGenerateException;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.xml.Namespaces;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class XMLResponseGeneratorTest {
	
	@Rule
	public ConfiguredService service = new ConfiguredService();

	@Test
	public void generateWithPrimitive() throws ResponseGenerateException {
		// create some outputs
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("Result", 101.05));
		
		// create response
		Response response = new Response("SumProcess", outputs);
		
		// generate
		Element responseElement = XMLResponseGenerator.generate(response);
		
		// check
		Assert.assertNotNull(responseElement);
		Assert.assertEquals("SumProcessResponse", responseElement.getName());
		Assert.assertEquals(Namespaces.PS, responseElement.getNamespace());
		Element resultElement = responseElement.getChild("Result", Namespaces.PS);
		Assert.assertNotNull(resultElement);
		Assert.assertNotNull(resultElement.getText());
	}
	
	@Test
	public void generateWithRequestedOutputs() throws ResponseGenerateException {
		
	}
	
	@Test
	public void generateDataReferenceSensibleURL() throws ResponseGenerateException, IOException {
		// setup
		String expectedBaseURL = service.getBaseURL() + "/data/";
		
		// create some outputs
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("BufferedPolygon", new GeometryFactory().createPoint(new Coordinate(-2.63, 51.16))));
		Assert.fail();
	}
	
	@Test
	public void generateDataReferenceMimeType() throws ResponseGenerateException, IOException {
		// create some outputs
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("Result", 101.05));
		Assert.fail();
	}

}
