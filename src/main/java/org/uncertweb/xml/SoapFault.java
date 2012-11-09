package org.uncertweb.xml;

import org.jdom.Element;

public class SoapFault extends Element {
	
	private static final long serialVersionUID = -9212661596909311053L;

	public enum Code { VersionMismatch, MustUnderstand, Client, Server };
		
	public SoapFault(Code code, String string) {
		super("Fault", SoapConstants.NAMESPACE);
		this.addContent(new Element("faultcode").setText(SoapConstants.NAMESPACE.getPrefix() + ":" + code.name()));
		this.addContent(new Element("faultstring").setText(string));
	}
	
	public SoapFault(Code code, String string, String detail) {
		this(code, string);
		setDetail(detail);
	}
	
	public void setDetail(String detail) {
		// TODO: proper detail structure required
		this.removeChild("detail");
		if (detail != null) {
			this.addContent(new Element("detail").addContent(new Element("exception", Namespaces.PS).setText(detail)));
		}
	}
	
}
