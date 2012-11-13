package org.uncertweb.ps.test.process;

import java.util.Arrays;
import java.util.List;

import org.uncertweb.ps.data.DataDescription;
import org.uncertweb.ps.data.Metadata;
import org.uncertweb.ps.data.ProcessInputs;
import org.uncertweb.ps.data.ProcessOutputs;
import org.uncertweb.ps.data.SingleOutput;
import org.uncertweb.ps.process.AbstractProcess;
import org.uncertweb.ps.process.ProcessException;

import com.vividsolutions.jts.geom.Polygon;

public class BufferPolygonProcess extends AbstractProcess {

	public List<String> getInputIdentifiers() {
		return Arrays.asList(new String[] { "Polygon", "Distance" });
	}

	public List<String> getOutputIdentifiers() {
		return Arrays.asList(new String[] { "BufferedPolygon" });
	}

	public DataDescription getInputDataDescription(String identifier) {
		if (identifier.equals("Polygon")) {
			return new DataDescription(Polygon.class);
		}
		else {
			return new DataDescription(Double.class);
		}
	}

	public DataDescription getOutputDataDescription(String identifier) {
		return new DataDescription(Polygon.class);
	}

	public List<Metadata> getInputMetadata(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Metadata> getOutputMetadata(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	public ProcessOutputs run(ProcessInputs inputs) throws ProcessException {
		// get polygon and distance
		Polygon polygon = inputs.get("Polygon").getAsSingleInput().getObjectAs(Polygon.class);
		double distance = inputs.get("Distance").getAsSingleInput().getObjectAs(Double.class);
		
		// buffer
		Polygon buffered = (Polygon)polygon.buffer(distance);
		
		// all done
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("BufferedPolygon", buffered));
		return outputs;
	}

	@Override
	public List<Metadata> getMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

}
