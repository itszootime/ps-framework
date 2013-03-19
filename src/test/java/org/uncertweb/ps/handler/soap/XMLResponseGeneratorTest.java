package org.uncertweb.ps.handler.soap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.data.RequestedOutput;
import org.uncertweb.ps.handler.ResponseGenerateException;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.test.util.TestData;
import org.uncertweb.xml.Namespaces;
public class XMLResponseGeneratorTest {

	@Rule
	public ConfiguredService service = new ConfiguredService();

	@Test
	public void generateNotNull() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());

		// check
		assertThat(responseElement, notNullValue());
	}

	@Test
	public void generateElementName() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());

		// check
		assertThat(responseElement.getName(), allOf(notNullValue(), equalTo("SumProcessResponse")));
	}

	@Test
	public void generateElementNamespace() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());

		// check
		assertThat(responseElement.getNamespace(), equalTo(Namespaces.PS));
	}

	@Test
	public void generateOutputNotNull() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());

		// check
		Element resultElement = responseElement.getChild("Result", Namespaces.PS);
		assertThat(resultElement, notNullValue());
	}

	@Test
	public void generateOutputValue() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getSumResponse());

		// check
		Element resultElement = responseElement.getChild("Result", Namespaces.PS);
		assertThat(resultElement.getText(), equalTo("101.05"));
	}	

	@Test
	public void generateWithComplex() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getBufferPolygonResponse());

		// check
		Element resultElement = responseElement.getChild("BufferedPolygon", Namespaces.PS);
		List<?> children = resultElement.getChildren();
		assertThat(children.size(), equalTo(1));
		assertThat(((Element)children.get(0)).getName(), equalTo("Polygon"));
	}

	@Test
	public void generateRequestedOutputsEmpty() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[0]));

		// check
		int resultElementCount = responseElement.getChildren().size();
		assertThat(resultElementCount, equalTo(0));
	}

	@Test
	public void generateRequestedOutputsCount() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("SHA1", false)
		}));;

		// check
		int resultElementCount = responseElement.getChildren().size();
		assertThat(resultElementCount, equalTo(1));
	}

	@Test
	public void generateRequestedOutputName() throws ResponseGenerateException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getHashResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("SHA1", false)
		}));;

		// check
		Element resultElement = responseElement.getChild("SHA1", Namespaces.PS);
		assertThat(resultElement, notNullValue());
	}

	@Test
	public void generateDataReferenceSensibleURL() throws ResponseGenerateException, IOException {
		// setup
		String expectedBaseURL = service.getBaseURL() + "/data/";

		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getBufferPolygonResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("BufferedPolygon", true)
		}));

		// check
		Element referenceElement = responseElement.getChild("BufferedPolygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		String url = referenceElement.getAttributeValue("href");
		assertThat(url, startsWith(expectedBaseURL));
	}

	@Test
	public void generateDataReferenceMimeType() throws ResponseGenerateException, IOException {
		// generate
		Element responseElement = XMLResponseGenerator.generate(TestData.getBufferPolygonResponse(), Arrays.asList(new RequestedOutput[] {
				new RequestedOutput("BufferedPolygon", true)
		}));

		// check
		Element referenceElement = responseElement.getChild("BufferedPolygon", Namespaces.PS).getChild("DataReference", Namespaces.PS);
		String mimeType = referenceElement.getAttributeValue("mimeType");
		assertThat(mimeType, equalTo("text/xml"));
	}

}
