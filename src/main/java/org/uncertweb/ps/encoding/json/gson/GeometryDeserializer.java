package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * GeoJSON compliant.
 * 
 * @author Richard Jones
 *
 */
public class GeometryDeserializer implements JsonDeserializer<Geometry> {

	public Geometry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();		
		String type = obj.get("type").getAsString();
		
		// get common properties
		int srid = 0;
		if (obj.has("crs")) srid = CRSalizer.deserialize(obj.get("crs"));
		
		// deserialize depending on type
		if (type.equals("Point")) {		
			double[] coordinates = context.deserialize(obj.get("coordinates"), double[].class);
			Point point = new GeometryFactory().createPoint(new Coordinate(coordinates[0], coordinates[1]));
			point.setSRID(srid);
			return point;
		}
		throw new JsonParseException("Unable to deserialize geometry type " + type + ".");
	}
}
