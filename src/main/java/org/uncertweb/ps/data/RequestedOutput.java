package org.uncertweb.ps.data;

public class RequestedOutput {

	private String name;
	private boolean isReference;
	
	public RequestedOutput(String name, boolean isReference) {
		this.name = name;
		this.isReference = isReference;
	}
	
	public String getName() {
		return name;
	}

	public boolean isReference() {
		return isReference;
	}
	
}
