package org.uncertweb.ps.handler.soap;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.uncertweb.ps.test.ConfiguredService;
import org.uncertweb.xml.Namespaces;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class SchemaGeneratorTest {
	
	@Rule
	public ConfiguredService service = new ConfiguredService();
	
	private Element schema;
	
	@Before
	public void before() {
		SchemaGenerator generator = new SchemaGenerator();
		schema = generator.generateDocument().getRootElement();
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
