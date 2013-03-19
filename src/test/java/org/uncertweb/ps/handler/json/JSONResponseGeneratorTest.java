package org.uncertweb.ps.handler.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.handler.ResponseGenerateException;
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
	
	private JsonObject generateResponse(Response response, List<RequestedOutput> reqOutputs) throws ResponseGenerateException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			JSONResponseGenerator.generate(response, reqOutputs, baos);
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
		JsonElement polygon = outputs.get("BufferedPolygon");
		assertThat(polygon, notNullValue());
		assertThat(polygon.getAsJsonObject().get("type").getAsString(), equalTo("Polygon"));
	}
	
	@Test
	public void generateRequestedOutputsEmpty() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[0]));
		JsonObject outputs = response.get("HashProcessResponse").getAsJsonObject();
		assertThat(outputs.entrySet().size(), equalTo(0));
	}
	
	@Test
	public void generateRequestedOutputsCount() throws ResponseGenerateException {
		JsonObject response = generateResponse(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("SHA1", false)
		}));
		JsonObject outputs = response.get("HashProcessResponse").getAsJsonObject();
		assertThat(outputs.entrySet().size(), equalTo(1));
	}
	
	
	@Test
	public void generateRequestedOutputName() throws ResponseGenerateException {
		// generate
		JsonObject response = generateResponse(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("SHA1", false)
		}));
		JsonObject outputs = response.get("HashProcessResponse").getAsJsonObject();
		assertThat(outputs.has("SHA1"), equalTo(true));
	}	
	
	@Test
	public void generateDataReferenceExists() throws ResponseGenerateException {
		// generate
		JsonObject response = generateResponse(TestData.getBufferPolygonResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("BufferedPolygon", true)
		}));

		// check
		JsonObject outputs = response.get("BufferPolygonProcessResponse").getAsJsonObject();
		assertThat(outputs.get("BufferedPolygon").getAsJsonObject().has("DataReference"), equalTo(true));
	}

	
	@Test
	public void generateDataReferenceSensibleURL() throws ResponseGenerateException {
		// setup
		String expectedBaseURL = service.getBaseURL() + "/data/";
		
		// generate
		JsonObject response = generateResponse(TestData.getBufferPolygonResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("BufferedPolygon", true)
		}));

		// check
		JsonObject outputs = response.get("BufferPolygonProcessResponse").getAsJsonObject();
		JsonObject ref = outputs.get("BufferedPolygon").getAsJsonObject().get("DataReference").getAsJsonObject();
		assertThat(ref.get("href").getAsString(), startsWith(expectedBaseURL));
	}

	@Test
	public void generateDataReferenceMimeType() throws ResponseGenerateException {
		// generate
		JsonObject response = generateResponse(TestData.getBufferPolygonResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("BufferedPolygon", true)
		}));

		// check
		JsonObject outputs = response.get("BufferPolygonProcessResponse").getAsJsonObject();
		JsonObject ref = outputs.get("BufferedPolygon").getAsJsonObject().get("DataReference").getAsJsonObject();
		assertThat(ref.get("mimeType").getAsString(), equalTo("application/json"));
	}

}
