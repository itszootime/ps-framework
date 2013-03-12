package org.uncertweb.ps.encoding;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EncodingHelper {

	public static Set<Class<?>> getInterfaces(Class<?> type) {
		Set<Class<?>> interfaces = new HashSet<Class<?>>();
		
		// check super class
		Class<?> sclass = type.getSuperclass();
		if (sclass != null) {
			interfaces.addAll(getInterfaces(sclass));
		}
		
		// and this class
		for (Class<?> interf : type.getInterfaces()) {
			if (Modifier.isPublic(interf.getModifiers())) {
				interfaces.add(interf);
				interfaces.addAll(getInterfaces(interf));
			}			
		}
		
		return interfaces;
	}
	
}
