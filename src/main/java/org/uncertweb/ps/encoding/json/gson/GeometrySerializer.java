package org.uncertweb.ps.encoding.json.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * GeoJSON compliant.
 * 
 * @author Richard Jones
 *
 */
public class GeometrySerializer implements JsonSerializer<Geometry> {

	public JsonElement serialize(Geometry geom, Type typeOfObj, JsonSerializationContext context) {
		if (geom instanceof Point) {
			return serializePoint((Point) geom, context);
		}
		return new JsonNull();
	}

	private JsonElement serializePoint(Point point, JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "Point");
		json.add("coordinates", generateCoordinateElement(point.getCoordinate(), context));
		json.add("crs", CRSalizer.serialize(point.getSRID()));
		return json;
	}

	private JsonElement generateCoordinateElement(Coordinate coordinate, JsonSerializationContext context) {
		double[] coords = { coordinate.x, coordinate.y };
		return context.serialize(coords);
	}
}
