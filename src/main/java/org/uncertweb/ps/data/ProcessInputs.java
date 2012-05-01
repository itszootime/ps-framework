package org.uncertweb.ps.data;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class ProcessInputs implements Iterable<Input> {

	private LinkedHashMap<String, Input> inputs;
	
	public ProcessInputs() {
		inputs = new LinkedHashMap<String, Input>();
	}
	
	public void add(Input input) {
		inputs.put(input.getIdentifier(), input);
	}
	
	public boolean has(String name) {
		return inputs.containsKey(name); 
	}
	
	public Input get(String name) {
		return inputs.get(name);
	}

	public Iterator<Input> iterator() {
		return inputs.values().iterator();
	}
	
}
