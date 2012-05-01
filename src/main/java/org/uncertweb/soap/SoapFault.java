package org.uncertweb.soap;

import org.jdom.Element;
import org.uncertweb.ps.encoding.xml.Namespaces;

public class SoapFault extends Element {

	public enum Code { VersionMismatch, MustUnderstand, Client, Server };
		
	public SoapFault(Code code, String string) {
		super("Fault", SoapConstants.NAMESPACE);
		this.addContent(new Element("faultcode").setText(SoapConstants.NAMESPACE.getPrefix() + ":" + code.name()));
		this.addContent(new Element("faultstring").setText(string));
	}
	
	public SoapFault(Code code, String string, String detail) {
		this(code, string);
		// FIXME: proper detail structure required
		this.addContent(new Element("detail").addContent(new Element("exception", Namespaces.PS).setText(detail)));
	}
	
}
