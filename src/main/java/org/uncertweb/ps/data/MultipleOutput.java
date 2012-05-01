package org.uncertweb.ps.data;

import java.util.ArrayList;
import java.util.List;

public class MultipleOutput extends Output {

	private List<Object> objects;
	
	public MultipleOutput(String name) {
		super(name);
		objects = new ArrayList<Object>();
	}
	
	public MultipleOutput(String name, List<? extends Object> objects) {
		super(name);
		this.objects = new ArrayList<Object>(objects);
	}
	
	public void addObject(Object o) {
		objects.add(o);
	}
	
	public List<Object> getObjects() {
		return objects;
	}
	
	public <T> List<T> getObjectsAs(Class<T> classOf) {
		List<T> list = new ArrayList<T>();
		for (Object o : objects) {
			list.add((T) o);
		}
		return list;
	}
	
}
