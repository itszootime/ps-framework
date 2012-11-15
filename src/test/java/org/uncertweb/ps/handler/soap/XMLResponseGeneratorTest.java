package org.uncertweb.ps.handler.soap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.Arrays;

import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.RequestedOutput;
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
	public void generateNotNull() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestSumResponse();

		// check
		assertThat(responseElement, notNullValue());
	}

	@Test
	public void generateElementName() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestSumResponse();

		// check
		assertThat(responseElement.getName(), allOf(notNullValue(), equalTo("SumProcessResponse")));
	}

	@Test
	public void generateElementNamespace() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestSumResponse();

		// check
		assertThat(responseElement.getNamespace(), equalTo(Namespaces.PS));
	}
	
	@Test
	public void generateOutputNotNull() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestSumResponse();

		// check
		Element resultElement = responseElement.getChild("Result", Namespaces.PS);
		assertThat(resultElement, notNullValue());
	}
	
	@Test
	public void generateOutputValue() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestSumResponse();

		// check
		Element resultElement = responseElement.getChild("Result", Namespaces.PS);
		assertThat(resultElement.getText(), equalTo("101.05"));
	}
	
	@Test
	public void generateRequestedOutputsEmpty() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestHashResponseNoOutputs();
		
		// check
		int resultElementCount = responseElement.getChildren().size();
		assertThat(resultElementCount, equalTo(0));
	}

	@Test
	public void generateRequestedOutputsCount() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestHashResponse();
		
		// check
		int resultElementCount = responseElement.getChildren().size();
		assertThat(resultElementCount, equalTo(1));
	}
	
	@Test
	public void generateRequestedOutputName() throws ResponseGenerateException {
		// generate
		Element responseElement = generateTestHashResponse();
		
		// check
		Element resultElement = responseElement.getChild("SHA1", Namespaces.PS);
		assertThat(resultElement, notNullValue());
	}

	@Test
	public void generateDataReferenceSensibleURL() throws ResponseGenerateException, IOException {
		// setup
		String expectedBaseURL = service.getBaseURL() + "/data/";

		// generate
		Element responseElement = generateTestBufferPolygonResponse();

		// check
		Element referenceElement = responseElement.getChild("BufferedPolygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		String url = referenceElement.getAttributeValue("href");
		assertThat(url, startsWith(expectedBaseURL));
	}

	@Test
	public void generateDataReferenceMimeType() throws ResponseGenerateException, IOException {
		// generate
		Element responseElement = generateTestBufferPolygonResponse();

		// check
		Element referenceElement = responseElement.getChild("BufferedPolygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		String url = referenceElement.getAttributeValue("mimeType");
		assertThat(url, equalTo("text/xml"));
	}

	private Element generateTestSumResponse() throws ResponseGenerateException {
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("Result", 101.05));
		Response response = new Response("SumProcess", outputs);
		return XMLResponseGenerator.generate(response);
	}
	
	private Response createTestHashResponse() {
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("MD5", "084c2f7604f15207fcf115632fc4b75e"));
		outputs.add(new SingleOutput("SHA1", "e834f00fd86f2635ed6821cb979eb4f73f68b56d"));
		return new Response("HashProcess", outputs);
	}
	
	private Element generateTestHashResponse() throws ResponseGenerateException {
		return XMLResponseGenerator.generate(createTestHashResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("SHA1", false)
		}));
	}
	
	private Element generateTestHashResponseNoOutputs() throws ResponseGenerateException {
		return XMLResponseGenerator.generate(createTestHashResponse(), Arrays.asList(new RequestedOutput[0]));
	}

	private Element generateTestBufferPolygonResponse() throws ResponseGenerateException {
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("BufferedPolygon", new GeometryFactory().createPoint(new Coordinate(-2.63, 51.16))));
		Response response = new Response("BufferPolygonProcess", outputs);
		return XMLResponseGenerator.generate(response, Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("BufferedPolygon", true)
		}));
	}

}
