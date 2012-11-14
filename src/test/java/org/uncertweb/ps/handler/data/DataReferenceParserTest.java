package org.uncertweb.ps.handler.data;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
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
		URL url = this.getClass().getClassLoader().getResource("xml/polygon.zip");
		Polygon polygon = parser.parse(url, Polygon.class, true);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parse() throws ParseException {
		URL url = this.getClass().getClassLoader().getResource("xml/polygon.xml");
		Polygon polygon = parser.parse(url, Polygon.class);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parseWithMime() throws ParseException {
		URL url = this.getClass().getClassLoader().getResource("xml/polygon.xml");
		Polygon polygon = parser.parse(url, Polygon.class, "text/xml");
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parseCompressedWithMime() throws ParseException {
		URL url = this.getClass().getClassLoader().getResource("xml/polygon.zip");
		Polygon polygon = parser.parse(url, Polygon.class, "text/xml", true);
		Assert.assertNotNull(polygon);
	}
	
	@Test
	public void parseNotFound() {
		ParseException exception = null;
		try {
			URL url = new URL("http://www.uncertweb.org/no/files/here/polygon.xml");
			parser.parse(url, Polygon.class, "text/xml");
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
