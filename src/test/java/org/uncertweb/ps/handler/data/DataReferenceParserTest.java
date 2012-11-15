package org.uncertweb.ps.handler.data;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.uncertweb.ps.data.DataReference;
import org.uncertweb.ps.encoding.ParseException;

import com.vividsolutions.jts.geom.Polygon;

public class DataReferenceParserTest {
	
	private DataReferenceParser parser;
	
	@Before
	public void setUp() {
		parser = new DataReferenceParser();
	}
	
	@Test
	public void parseCompressed() throws ParseException {
		DataReference ref = new DataReference(this.getClass().getClassLoader().getResource("xml/polygon.zip"), true);
		Polygon polygon = parser.parse(ref, Polygon.class);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parse() throws ParseException {
		DataReference ref = new DataReference(this.getClass().getClassLoader().getResource("xml/polygon.xml"));
		Polygon polygon = parser.parse(ref, Polygon.class);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parseWithMime() throws ParseException {
		DataReference ref = new DataReference(this.getClass().getClassLoader().getResource("xml/polygon.xml"), "text/xml");
		Polygon polygon = parser.parse(ref, Polygon.class);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parseCompressedWithMime() throws ParseException {
		DataReference ref = new DataReference(this.getClass().getClassLoader().getResource("xml/polygon.zip"), "text/xml", true);
		Polygon polygon = parser.parse(ref, Polygon.class);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parseNotFound() {
		ParseException exception = null;
		try {
			DataReference ref = new DataReference(new URL("http://www.uncertweb.org/no/files/here/polygon.xml"), "text/xml");
			parser.parse(ref, Polygon.class);
		}
		catch (MalformedURLException e) {
			// nothing wrong with that
		}
		catch (ParseException e) {
			exception = e;
		}		
		Assert.assertNotNull(exception);
	}

}
