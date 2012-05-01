package org.uncertweb.ps.data;


public class DataDescription {

	private String name;
	private Class<?> classOf;
	private int minOccurs;
	private int maxOccurs;
	private boolean raw;
	
	public DataDescription(Class<?> classOf) {
		this.classOf = classOf;
		this.minOccurs = 1;
		this.maxOccurs = 1;
		this.raw = false;
	}
	
	public DataDescription(Class<?> classOf, boolean raw) {
		this(classOf);
		this.raw = raw;
	}
	
	public DataDescription(Class<?> classOf, int minOccurs, int maxOccurs) {
		this(classOf);
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
	}
	
	public String getName() {
		return name;
	}
	
	public Class<?> getClassOf() {
		return classOf;
	}

	public int getMinOccurs() {
		return minOccurs;
	}

	public int getMaxOccurs() {
		return maxOccurs;
	}

	public boolean isRaw() {
		return raw;
	}

}
