package org.uncertweb.ps.data;

public class Metadata {

	private String key;
	private String value;	

	public Metadata(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public String getValue() {
		return value;
	}	
	
}
