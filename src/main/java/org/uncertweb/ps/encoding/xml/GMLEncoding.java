package org.uncertweb.ps.encoding.xml;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
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
	
	private static final String NAMESPACE = "http://www.opengis.net/gml/3.2";
	private static final String SCHEMA_LOCATION = "http://52north.org/schema/geostatistics/uncertweb/Profiles/GML/UncertWeb_GML.xsd";
	private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(new Class<?>[] {
			Point.class, LineString.class, Polygon.class, RectifiedGrid.class, MultiPoint.class,
			MultiPolygon.class, MultiLineString.class
	});
	
	private XmlBeansGeometryParser parser;
	private XmlBeansGeometryEncoder encoder;
	
	public GMLEncoding() {
		parser = new XmlBeansGeometryParser();
		encoder = new XmlBeansGeometryEncoder();
	}
		
	@Override
	public <T> T parse(Content content, Class<T> type) throws ParseException {
		try {
			// convert to string for external parsing
			String gml = new XMLOutputter().outputString((Element)content);
			return type.cast(parser.parseUwGeometry(gml));
		}
		catch (IllegalArgumentException e) {
			throw new ParseException("Couldn't parse GML: " + e.getMessage(), e);
		}
		catch (XmlException e) {
			throw new ParseException("Couldn't parse GML: " + e.getMessage(), e);
		}
	}
	
	@Override
	public <T> Content encode(T object) throws EncodeException {
		try {
			// TODO: this may need an ID generating system, like the O&M parser
			String gml = encoder.encodeGeometry((Geometry) object);
			Document document = new SAXBuilder().build(new ByteArrayInputStream(gml.getBytes()));
			return document.getRootElement();
		}
		catch (XmlException e) {
			throw new EncodeException("Couldn't encode GML: " + e.getMessage(), e);
		}
		catch (IOException e) {
			throw new EncodeException("Couldn't encode GML: " + e.getMessage(), e);
		}
		catch (JDOMException e) {
			throw new EncodeException("Couldn't encode GML: " + e.getMessage(), e);
		}
	}
	
	@Override
	public String getNamespace() {
		return NAMESPACE;
	}
	
	@Override
	public String getSchemaLocation() {
		return SCHEMA_LOCATION;
	}
	
	@Override
	public boolean isSupportedType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type);
	}

	@Override
	public Include getInclude(Class<?> type) {
		return new IncludeRef(type.getSimpleName());
	}

}
