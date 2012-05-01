package org.uncertweb.soap;

import org.jdom.Element;

public class SoapEnvelope extends Element {
	
	public SoapEnvelope() { 
		super("Envelope", SoapConstants.NAMESPACE);
		this.addContent(new Element("Header", SoapConstants.NAMESPACE));
	}

}
