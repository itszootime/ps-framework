package org.uncertweb.ps.handler.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.handler.RequestParseException;
import org.uncertweb.ps.handler.ResponseGenerateException;
import org.uncertweb.ps.handler.soap.XMLResponseGenerator;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.test.util.TestData;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JSONResponseGeneratorTest {

	@Rule
	public ConfiguredService service = new ConfiguredService();

	private JsonObject generateResponse(Response response) throws ResponseGenerateException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			JSONResponseGenerator.generate(response, baos);
			JsonParser parser = new JsonParser();
			return parser.parse(new String(baos.toByteArray())).getAsJsonObject();
		}
		catch (IOException e) {
			return null;
		}
	}

	@Test
	public void generateNotNull() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getSumResponse());
		assertThat(response, notNullValue());
	}

	@Test
	public void generateProcessName() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getSumResponse());
		assertThat(response.has("SumProcessResponse"), equalTo(true));
	}
	
	@Test
	public void generateOutputNotNull() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getSumResponse());
		JsonObject outputs = response.get("SumProcessResponse").getAsJsonObject();
		assertThat(outputs, notNullValue());
		assertThat(outputs.has("Result"), equalTo(true));
	}
	
	@Test
	public void generateOutputValue() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getSumResponse());
		JsonObject outputs = response.get("SumProcessResponse").getAsJsonObject();
		assertThat(outputs.get("Result").getAsDouble(), equalTo(101.05));
	}	
	
	@Test
	public void generateWithComplex() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getBufferPolygonResponse());
		JsonObject outputs = response.get("BufferPolygonProcessResponse").getAsJsonObject();
		JsonElement polygon = outputs.get("Polygon");
		assertThat(polygon, notNullValue());
	}
	
	@Test
	public void generateRequestedOutputsEmpty() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[0]));
		JsonObject outputs = response.get("BufferPolygonProcessResponse").getAsJsonObject();
		assertThat(outputs.entrySet().size(), equalTo(0));
	}
	
//		@Test
	//	public void generateRequestedOutputsCount() throws ResponseGenerateException {
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[] {
	//				new RequestedOutput("SHA1", false)
	//		}));;
	//		
	//		// check
	//		int resultElementCount = responseElement.getChildren().size();
	//		assertThat(resultElementCount, equalTo(1));
	//	}
	//	
	//	@Test
	//	public void generateRequestedOutputName() throws ResponseGenerateException {
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[] {
	//				new RequestedOutput("SHA1", false)
	//		}));;
	//		
	//		// check
	//		Element resultElement = responseElement.getChild("SHA1", Namespaces.PS);
	//		assertThat(resultElement, notNullValue());
	//	}
	//
	//	@Test
	//	public void generateDataReferenceSensibleURL() throws ResponseGenerateException, IOException {
	//		// setup
	//		String expectedBaseURL = service.getBaseURL() + "/data/";
	//
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getBufferPolygonResponse(), Arrays.asList(new RequestedOutput[] {
	//				new RequestedOutput("BufferedPolygon", true)
	//		}));
	//
	//		// check
	//		Element referenceElement = responseElement.getChild("BufferedPolygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
	//		String url = referenceElement.getAttributeValue("href");
	//		assertThat(url, startsWith(expectedBaseURL));
	//	}
	//
	//	@Test
	//	public void generateDataReferenceMimeType() throws ResponseGenerateException, IOException {
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getBufferPolygonResponse(), Arrays.asList(new RequestedOutput[] {
	//				new RequestedOutput("BufferedPolygon", true)
	//		}));
	//
	//		// check
	//		Element referenceElement = responseElement.getChild("BufferedPolygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
	//		String url = referenceElement.getAttributeValue("mimeType");
	//		assertThat(url, equalTo("text/xml"));
	//	}

}
