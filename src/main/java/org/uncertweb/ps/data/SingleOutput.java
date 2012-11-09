package org.uncertweb.ps.data;

public class SingleOutput extends Output {

	private Object object;
	
	public SingleOutput(String name, Object object) {
		super(name);
		this.object = object;
	}
	
	public Object getObject() {
		return object;
	}
	
	public <T> T getObjectAs(Class<T> type) {
		return type.cast(object);
	}
	
}
