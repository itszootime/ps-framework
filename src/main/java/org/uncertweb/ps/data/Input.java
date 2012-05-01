package org.uncertweb.ps.data;

public abstract class Input {

	private String identifier;
	
	public Input(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public boolean isSingleInput() {
		return (this instanceof SingleInput);
	}
	
	public boolean isMultipleInput() {
		return (this instanceof MultipleInput);
	}
	
	public SingleInput getAsSingleInput() {
		return (SingleInput) this;
	}
	
	public MultipleInput getAsMultipleInput() {
		return (MultipleInput) this;
	}
	
}
