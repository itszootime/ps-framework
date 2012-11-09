package org.uncertweb.ps.data;

public class SingleInput extends Input {

	private Object object;
	
	public SingleInput(String name, Object object) {
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
