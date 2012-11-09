package org.uncertweb.ps.data;


public class DataDescription {
	
	private Class<?> type;
	private int minimum;
	private int maximum;
	
	public DataDescription(Class<?> type) {
		this.type = type;
		this.minimum = 1;
		this.maximum = 1;
	}
	
	public DataDescription(Class<?> type, int minimum, int maximum) {
		this(type);
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	public Class<?> getType() {
		return type;
	}

	public int getMinOccurs() {
		return minimum;
	}

	public int getMaxOccurs() {
		return maximum;
	}

}
