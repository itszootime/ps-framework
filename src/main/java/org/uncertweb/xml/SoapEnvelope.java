package org.uncertweb.xml;

import org.jdom.Element;

public class SoapEnvelope extends Element {
	
	private static final long serialVersionUID = 4546854105287133403L;

	public SoapEnvelope() { 
		super("Envelope", SoapConstants.NAMESPACE);
		this.addContent(new Element("Header", SoapConstants.NAMESPACE));
	}

}
