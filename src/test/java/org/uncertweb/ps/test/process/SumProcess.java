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


public class SumProcess extends AbstractProcess {

	public List<String> getInputIdentifiers() {
		return Arrays.asList(new String[] { "A", "B" });
	}

	public List<String> getOutputIdentifiers() {
		return Arrays.asList(new String[] { "Result" });
	}

	public DataDescription getInputDataDescription(String identifier) {
		if (identifier.equals("A")) {
			return new DataDescription(Double.class);
		}
		else {
			return new DataDescription(Double.class, 1, Integer.MAX_VALUE);
		}
	}

	public DataDescription getOutputDataDescription(String identifier) {
		return new DataDescription(Double.class);
	}

	public ProcessOutputs run(ProcessInputs inputs) throws ProcessException {
		// get a and b
		double a = inputs.get("A").getAsSingleInput().getObjectAs(Double.class);
		List<Double> b = inputs.get("B").getAsMultipleInput().getObjectsAs(Double.class);
		
		// calculate
		double result = a;
		for (double num : b) {
			result += num;
		}
		
		// return
		ProcessOutputs outputs = new ProcessOutputs();
		outputs.add(new SingleOutput("Result", result));
		return outputs;
	}

	@Override
	public List<Metadata> getInputMetadata(String arg0) {
		Metadata description;
		if (arg0.equals("A")) {
			description = new Metadata("description", "The original number");
		}
		else {
			description = new Metadata("description", "The number(s) to add to the original");
		}
		return Arrays.asList(new Metadata[] { description });
	}

	@Override
	public List<Metadata> getOutputMetadata(String arg0) {
		Metadata description = new Metadata("description", "The result of the sum");
		return Arrays.asList(new Metadata[] { description });
	}

	@Override
	public List<Metadata> getMetadata() {
		Metadata description = new Metadata("description", "Sum two numbers");
		return Arrays.asList(new Metadata[] { description });
	}
	
}
