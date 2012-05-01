package org.uncertweb.ps.data;

public abstract class Output {

	private String identifier;
	
	public Output(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public boolean isSingleOutput() {
		return (this instanceof SingleOutput);
	}
	
	public boolean isMultipleOutput() {
		return (this instanceof MultipleOutput);
	}
	
	public SingleOutput getAsSingleOutput() {
		return (SingleOutput) this;
	}
	
	public MultipleOutput getAsMultipleOutput() {
		return (MultipleOutput) this;
	}
	
}
