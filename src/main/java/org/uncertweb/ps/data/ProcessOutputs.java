package org.uncertweb.ps.data;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class ProcessOutputs implements Iterable<Output> {

	private LinkedHashMap<String, Output> outputs;
	
	public ProcessOutputs() {
		outputs = new LinkedHashMap<String, Output>();
	}
	
	public void add(Output output) {
		outputs.put(output.getIdentifier(), output);
	}
	
	public Output get(String name) {
		return outputs.get(name);
	}

	public Iterator<Output> iterator() {
		return outputs.values().iterator();
	}
	
}
