package org.uncertweb.ps.handler.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.handler.ResponseGenerateException;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.test.util.TestData;

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

	//	@Test
	//	public void generateElementNamespace() throws ResponseGenerateException {
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());
	//
	//		// check
	//		assertThat(responseElement.getNamespace(), equalTo(Namespaces.PS));
	//	}
	//	
	//	@Test
	//	public void generateOutputNotNull() throws ResponseGenerateException {
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());
	//
	//		// check
	//		Element resultElement = responseElement.getChild("Result", Namespaces.PS);
	//		assertThat(resultElement, notNullValue());
	//	}
	//	
	//	@Test
	//	public void generateOutputValue() throws ResponseGenerateException {
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());
	//
	//		// check
	//		Element resultElement = responseElement.getChild("Result", Namespaces.PS);
	//		assertThat(resultElement.getText(), equalTo("101.05"));
	//	}
	//	
	//	@Test
	//	public void generateRequestedOutputsEmpty() throws ResponseGenerateException {
	//		// generate
	//		Element responseElement = XMLResponseGenerator.generate(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[0]));
	//		
	//		// check
	//		int resultElementCount = responseElement.getChildren().size();
	//		assertThat(resultElementCount, equalTo(0));
	//	}
	//
	//	@Test
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
