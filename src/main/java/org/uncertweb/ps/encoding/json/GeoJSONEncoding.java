package org.uncertweb.ps.encoding.json;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import me.itszooti.geojson.GeoGeometry;
import me.itszooti.geojson.GeoJsonEncoder;
import me.itszooti.geojson.GeoJsonParser;
import me.itszooti.geojson.GeoObject;
import me.itszooti.geojson.jts.GeoJtsConverter;

import org.uncertweb.ps.encoding.EncodeException;
import org.uncertweb.ps.encoding.ParseException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeoJSONEncoding extends AbstractJSONEncoding {
	
	private static final List<Class<?>> SUPPORTED_TYPES = Arrays.asList(new Class<?>[] {
		Point.class, LineString.class, Polygon.class, MultiPoint.class,
		MultiPolygon.class, MultiLineString.class, GeometryCollection.class
	});

	@Override
	public <T> T parse(String json, Class<T> type) throws ParseException {
		GeoJsonParser parser = GeoJsonParser.create();
		GeoObject geo = parser.parse(new ByteArrayInputStream(json.getBytes()));
		if (geo instanceof GeoGeometry) {
			GeoJtsConverter converter = new GeoJtsConverter();
			return type.cast(converter.toJts((GeoGeometry)geo));
		}
		else {
			throw new ParseException("Unsupported GeoJSON type: " + geo.getClass().getSimpleName());
		}
	}

	@Override
	public <T> String encode(T object) throws EncodeException {		
		if (object instanceof Geometry) {
			GeoJtsConverter converter = new GeoJtsConverter();
			GeoObject geo = converter.fromJts((Geometry)object);
			GeoJsonEncoder encoder = GeoJsonEncoder.create();
			return encoder.encode(geo);
		}
		else {
			throw new EncodeException("Cannot encode " + object.getClass().getSimpleName() + " as GeoJSON");
		}
	}

	@Override
	public boolean isSupportedType(Class<?> type) {
		return SUPPORTED_TYPES.contains(type);
	}	
	
}
