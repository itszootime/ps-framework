package org.uncertweb.ps.encoding.xml;


import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.uncertweb.api.gml.geometry.RectifiedGrid;
import org.uncertweb.api.gml.io.XmlBeansGeometryEncoder;
import org.uncertweb.api.gml.io.XmlBeansGeometryParser;
import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GMLEncoding extends AbstractXMLEncoding {

	// constants
	private static final String NAMESPACE = "http://www.opengis.net/gml/3.2";
	private static final String SCHEMA_LOCATION = "http://v-mars.uni-muenster.de/uncertweb/schema/profiles/gml/UncertWeb_GML.xsd";
	private static final List<Class<?>> SUPPORTED_CLASSES = Arrays.asList(new Class<?>[] {
		Point.class, LineString.class, Polygon.class, RectifiedGrid.class, MultiPoint.class,
		MultiPolygon.class, MultiLineString.class
	});
	
	private XmlBeansGeometryParser parser;
	private XmlBeansGeometryEncoder encoder;
	
	public GMLEncoding() {
		parser = new XmlBeansGeometryParser();
		encoder = new XmlBeansGeometryEncoder();
	}
		
	public Object parse(Element element, Class<?> classOf) throws ParseException {
		try {
			// convert to string for external parsing
			String gml = new XMLOutputter().outputString(element);
			return parser.parseUwGeometry(gml);
		}
		catch (Exception e) {
			throw new ParseException("Couldn't parse GML: " + e.getMessage(), e);
		}
	}
	
	public Element encode(Object object) throws EncodeException {
		try {
			// TODO: not sure whether this needs an ID generating system, like the O&M parser
			String gml = encoder.encodeGeometry((Geometry) object);
			Document document = new SAXBuilder().build(new ByteArrayInputStream(gml.getBytes()));
			return document.getRootElement();
		}
		catch (Exception e) {
			throw new EncodeException("Couldn't encode GML: " + e.getMessage(), e);
		}
	}
	
	public String getNamespace() {
		return NAMESPACE;
	}
	
	public String getSchemaLocation() {
		return SCHEMA_LOCATION;
	}
	
	public boolean isSupportedClass(Class<?> classOf) {
		return SUPPORTED_CLASSES.contains(classOf);
	}

	public Include getIncludeForClass(Class<?> classOf) {
		return new IncludeRef(classOf.getSimpleName());
	}

}
