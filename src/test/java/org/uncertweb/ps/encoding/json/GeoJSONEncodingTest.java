package org.uncertweb.ps.encoding.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJSONEncodingTest {

	private GeoJSONEncoding encoding;

	@Before
	public void before() {
		encoding = new GeoJSONEncoding();
	}
	
	@Test
	public void supportedTypes() {
		Class<?>[] types = { Point.class, LineString.class, Polygon.class,
				MultiPoint.class, MultiLineString.class, MultiPolygon.class, GeometryCollection.class };
		for (Class<?> type : types) {
			Assert.assertTrue(encoding.isSupportedType(type));
		}
	}
	
	@Test
	public void supportedMimeType() {
		Assert.assertTrue(encoding.isSupportedMimeType("application/json"));
	}
	
	@Test
	public void defaultMimeType() {
		assertEquals("application/json", encoding.getDefaultMimeType());
	}
	
	@Test
	public void encode() throws EncodeException {
		Point point = new GeometryFactory().createPoint(new Coordinate(100.0, -100.0));
		String encoded = encoding.encode(point);
		assertThat(encoded, notNullValue());
		assertThat(encoded, containsString("Point"));
	}
	
	@Test
	public void parse() throws ParseException {
		String encoded = "{ \"type\": \"Point\", \"coordinates\": [100.0, -100.0] }";
		Point point = encoding.parse(encoded, Point.class);
		assertThat(point, notNullValue());
		assertThat(point.getX(), equalTo(100.0));
		assertThat(point.getY(), equalTo(-100.0));
	}
	
}
