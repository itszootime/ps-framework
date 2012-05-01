package org.uncertweb.soap;

import org.jdom.Element;

public class SoapBody extends Element {
	
	public SoapBody() {
		super("Body", SoapConstants.NAMESPACE);
	}
	
}
