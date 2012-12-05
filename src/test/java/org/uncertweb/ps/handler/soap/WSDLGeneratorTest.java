package org.uncertweb.ps.handler.soap;

import static org.junit.Assert.fail;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.test.ConfiguredService;

public class WSDLGeneratorTest {

	@Rule
	public ConfiguredService service = new ConfiguredService();
	
	private Element wsdl;
	
	@Before
	public void before() {
		WSDLGenerator generator = new WSDLGenerator();
		System.out.println(service.getBaseURL());
		wsdl = generator.generateDocument(service.getBaseURL() + "/service").getRootElement();
	}
	
	@Test
	public void valid() {
		fail();
	}
	
}
