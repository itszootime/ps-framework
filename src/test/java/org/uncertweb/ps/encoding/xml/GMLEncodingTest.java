package org.uncertweb.ps.encoding.xml;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.uncertweb.api.gml.geometry.RectifiedGrid;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GMLEncodingTest {

	private GMLEncoding encoding;

	@Before
	public void before() {
		encoding = new GMLEncoding();
	}
	
	@Test
	public void supportedTypes() {
		Class<?>[] types = { Point.class, LineString.class, Polygon.class, RectifiedGrid.class,
				MultiPoint.class, MultiLineString.class, MultiPolygon.class };
		for (Class<?> type : types) {
			Assert.assertTrue(encoding.isSupportedType(type));
		}
	}
	
	@Test
	public void supportedMimeType() {
		Assert.assertTrue(encoding.isSupportedMimeType("text/xml"));
	}
	
}
