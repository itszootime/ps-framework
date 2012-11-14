package org.uncertweb.ps.handler.data;

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class DataReferenceGeneratorTest {
	
	private DataReferenceGenerator generator;
	
	@Before
	public void before() {
		generator = new DataReferenceGenerator();
		
		// set server url
	}
	
	@Test
	public void generate() {
		Point point = new GeometryFactory().createPoint(new Coordinate(-2.63, 51.16));
		URL url = generator.generate(point);
		Assert.assertNotNull(url);
	}
	
	@Test
	public void generateCompressed() {
		Assert.fail();
	}

}
