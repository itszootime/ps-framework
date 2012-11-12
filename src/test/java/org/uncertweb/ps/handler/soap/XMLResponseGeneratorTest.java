package org.uncertweb.ps.handler.soap;

import junit.framework.Assert;

import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Test;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.data.SingleOutput;
import org.uncertweb.ps.test.Utilities;
import org.uncertweb.xml.Namespaces;

public class XMLResponseGeneratorTest {

	@BeforeClass
	public static void setUpClass() {
		Utilities.setupProcessRepository();
	}

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
	public void generateWithDataReference() throws ResponseGenerateException {
		Assert.fail();
	}

}
