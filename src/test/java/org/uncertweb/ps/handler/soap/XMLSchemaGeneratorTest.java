package org.uncertweb.ps.handler.soap;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.xml.Namespaces;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.fail;

public class XMLSchemaGeneratorTest {
	
	@Rule
	public ConfiguredService service = new ConfiguredService();
	
	private Element schema;
	
	@Before
	public void before() {
		XMLSchemaGenerator generator = new XMLSchemaGenerator();
		schema = generator.generateDocument().getRootElement();
	}
	
	public void valid() {
		
	}
	
	@Test
	public void rootElement() {
		assertThat(schema.getName(), equalTo("schema"));
		assertThat(schema.getNamespace(), equalTo(Namespaces.XSD));
	}
	
	@Test
	public void targetNamespace() {
		assertThat(schema.getAttributeValue("targetNamespace"), equalTo(Namespaces.PS.getURI()));
	}
	
	@Test
	public void elementFormDefaultQualified() {
		assertThat(schema.getAttributeValue("elementFormDefault"), equalTo("qualified"));
	}

	public void imports() {
		// check import elements exist
	}
	
	public void processNames() {
		
	}
	
	public void inputOrder() {
		
	}
	
	public void inputNames() {
		
	}
	
	public void inputSimpleTypes() {
		
	}
	
	public void inputComplexTypes() {
		
	}
	
	public void inputBinaryTypes() {
		
	}
	
	public void outputNames() {
		
	}
	
	public void outputOrder() {
		
	}
	
}
