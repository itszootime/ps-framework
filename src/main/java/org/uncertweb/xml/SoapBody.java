package org.uncertweb.xml;

import org.jdom.Element;

public class SoapBody extends Element {
	
	private static final long serialVersionUID = -937611907543351450L;

	public SoapBody() {
		super("Body", SoapConstants.NAMESPACE);
	}
	
}
