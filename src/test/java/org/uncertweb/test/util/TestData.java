package org.uncertweb.test.util;

import org.junit.Ignore;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.Response;
import org.uncertweb.ps.data.SingleOutput;
import org.uncertweb.ps.handler.ResponseGenerateException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

@Ignore
public class TestData {

	public static Response getSumResponse() {
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("Result", 101.05));
		return new Response("SumProcess", outputs);
	}
	
	public static Response getHashResponse() {
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("MD5", "084c2f7604f15207fcf115632fc4b75e"));
		outputs.add(new SingleOutput("SHA1", "e834f00fd86f2635ed6821cb979eb4f73f68b56d"));
		return new Response("HashProcess", outputs);
	}

	public static Response getBufferPolygonResponse() throws ResponseGenerateException {
		ProcessOutputs outputs = new ProcessOutputs();
		GeometryFactory factory = new GeometryFactory();
		LinearRing exterior = factory.createLinearRing(new Coordinate[] {
			new Coordinate(100.0, 0.0), 
			new Coordinate(101.0, 0.0),
			new Coordinate(101.0, 1.0),
			new Coordinate(100.0, 1.0),
			new Coordinate(100.0, 0.0)
		});
		outputs.add(new SingleOutput("BufferedPolygon", new GeometryFactory().createPolygon(exterior)));
		return new Response("BufferPolygonProcess", outputs);
	}
	
}
